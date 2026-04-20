package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.OrderStatus;
import com.enterprise.wms.domain.WmsEnums.PickingStrategy;
import com.enterprise.wms.domain.WmsEnums.PickingTaskStatus;
import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.PickingTask;
import com.enterprise.wms.domain.entity.SalesOrder;
import com.enterprise.wms.domain.entity.SalesOrderLine;
import com.enterprise.wms.domain.entity.Warehouse;
import com.enterprise.wms.dto.OrderDtos.CreateOrderRequest;
import com.enterprise.wms.repository.InventoryRepository;
import com.enterprise.wms.repository.PickingTaskRepository;
import com.enterprise.wms.repository.ProductRepository;
import com.enterprise.wms.repository.SalesOrderLineRepository;
import com.enterprise.wms.repository.SalesOrderRepository;
import com.enterprise.wms.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OutboundService {
    private static final Logger log = LoggerFactory.getLogger(OutboundService.class);

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final PickingTaskRepository pickingTaskRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    public OutboundService(SalesOrderRepository salesOrderRepository,
                           SalesOrderLineRepository salesOrderLineRepository,
                           PickingTaskRepository pickingTaskRepository,
                           ProductRepository productRepository,
                           WarehouseRepository warehouseRepository,
                           InventoryRepository inventoryRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.pickingTaskRepository = pickingTaskRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public SalesOrder createOrder(CreateOrderRequest request) {
        Warehouse warehouse = warehouseRepository.findByCode(request.warehouseCode())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        SalesOrder order = new SalesOrder();
        order.setOrderNo(request.orderNo());
        order.setWarehouse(warehouse);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        SalesOrder saved = salesOrderRepository.save(order);

        for (var line : request.lines()) {
            SalesOrderLine orderLine = new SalesOrderLine();
            orderLine.setOrderRef(saved);
            orderLine.setProduct(productRepository.findBySku(line.sku())
                    .orElseThrow(() -> new IllegalArgumentException("SKU not found: " + line.sku())));
            orderLine.setRequestedQty(line.quantity());
            orderLine.setAllocatedQty(0);
            salesOrderLineRepository.save(orderLine);
        }
        return saved;
    }

    @Transactional
    public PickingTask createPickingTask(Long orderId, PickingStrategy strategy, String workerUsername) {
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow();
        return createTaskForOrder(order, strategy, workerUsername, null);
    }

    @Transactional
    public List<PickingTask> createWavePickingTasks(String warehouseCode, String waveNo, List<Long> orderIds, String workerUsername) {
        Warehouse warehouse = warehouseRepository.findByCode(warehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        List<PickingTask> tasks = new ArrayList<>();
        for (Long orderId : orderIds) {
            SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow();
            if (!order.getWarehouse().getId().equals(warehouse.getId())) {
                throw new IllegalArgumentException("Order " + order.getOrderNo() + " belongs to another warehouse");
            }
            tasks.add(createTaskForOrder(order, PickingStrategy.WAVE, workerUsername, waveNo));
        }
        return tasks;
    }

    public List<PickingTask> activePickingTasks() {
        return pickingTaskRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SalesOrder> listOrders() {
        return salesOrderRepository.findAll();
    }

    @Transactional
    public PickingTask updatePickingStatus(Long taskId, PickingTaskStatus newStatus, Integer progressPct) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Picking task status is required");
        }

        PickingTask task = pickingTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Picking task not found: " + taskId));
        validatePickingTransition(task.getStatus(), newStatus);

        task.setStatus(newStatus);
        if (newStatus == PickingTaskStatus.COMPLETED) {
            task.setPickingProgressPct(100);
            SalesOrder order = task.getOrderRef();
            order.setStatus(OrderStatus.PICKED);
            salesOrderRepository.save(order);
        } else if (newStatus == PickingTaskStatus.IN_PROGRESS) {
            int requestedProgress = progressPct == null ? (task.getPickingProgressPct() == null ? 0 : task.getPickingProgressPct()) : progressPct;
            task.setPickingProgressPct(Math.max(0, Math.min(99, requestedProgress)));
        } else {
            task.setPickingProgressPct(0);
        }
        return pickingTaskRepository.save(task);
    }

    @Transactional
    public SalesOrder markShipped(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.SHIPPED);
        return salesOrderRepository.save(order);
    }

    private PickingTask createTaskForOrder(SalesOrder order, PickingStrategy strategy, String workerUsername, String waveNo) {
        boolean fullyAllocated = allocateFefo(order);
        order.setStatus(fullyAllocated ? OrderStatus.ALLOCATED : OrderStatus.PARTIALLY_ALLOCATED);
        salesOrderRepository.save(order);

        PickingTask task = new PickingTask();
        task.setOrderRef(order);
        task.setStrategy(strategy);
        task.setWorkerUsername(workerUsername);
        task.setWaveNo(waveNo);
        task.setStatus(PickingTaskStatus.CREATED);
        task.setPickingProgressPct(0);
        task.setCreatedAt(LocalDateTime.now());
        return pickingTaskRepository.save(task);
    }

    private boolean allocateFefo(SalesOrder order) {
        boolean fullyAllocated = true;
        List<SalesOrderLine> lines = salesOrderLineRepository.findByOrderRef(order);
        for (SalesOrderLine line : lines) {
            int requestedQty = line.getRequestedQty() == null ? 0 : line.getRequestedQty();
            int alreadyAllocated = line.getAllocatedQty() == null ? 0 : line.getAllocatedQty();
            int toAllocate = Math.max(0, requestedQty - alreadyAllocated);
            int allocated = alreadyAllocated;

            List<Inventory> pools = inventoryRepository.findByProductAndWarehouseAndQuantityGreaterThan(
                    line.getProduct(), order.getWarehouse(), 0);
            pools.sort(Comparator.comparing(
                    inventory -> inventory.getLotBatch() == null || inventory.getLotBatch().getExpiryDate() == null
                            ? LocalDate.MAX
                            : inventory.getLotBatch().getExpiryDate()));

            for (Inventory inventory : pools) {
                int available = (inventory.getQuantity() == null ? 0 : inventory.getQuantity())
                        - (inventory.getReservedQty() == null ? 0 : inventory.getReservedQty());
                if (available <= 0 || toAllocate <= 0) {
                    continue;
                }
                int pickQty = Math.min(available, toAllocate);
                inventory.setReservedQty((inventory.getReservedQty() == null ? 0 : inventory.getReservedQty()) + pickQty);
                inventoryRepository.save(inventory);
                allocated += pickQty;
                toAllocate -= pickQty;
            }

            line.setAllocatedQty(allocated);
            salesOrderLineRepository.save(line);
            if (allocated < requestedQty) {
                fullyAllocated = false;
                log.warn("Partial FEFO allocation for order {} / SKU {}: allocated {} of {}",
                        order.getOrderNo(), line.getProduct().getSku(), allocated, requestedQty);
            }
        }
        return fullyAllocated;
    }

    private void validatePickingTransition(PickingTaskStatus currentStatus, PickingTaskStatus newStatus) {
        if (currentStatus == null) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case CREATED -> newStatus == PickingTaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == PickingTaskStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Invalid picking task transition: " + currentStatus + " -> " + newStatus);
        }
    }
}
