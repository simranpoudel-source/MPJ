package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.MovementType;
import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.MovementHistory;
import com.enterprise.wms.domain.entity.Product;
import com.enterprise.wms.domain.entity.Warehouse;
import com.enterprise.wms.repository.InventoryRepository;
import com.enterprise.wms.repository.MovementHistoryRepository;
import com.enterprise.wms.repository.ProductRepository;
import com.enterprise.wms.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class TransferService {
    private final InventoryRepository inventoryRepository;
    private final MovementHistoryRepository movementHistoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public TransferService(InventoryRepository inventoryRepository,
                           MovementHistoryRepository movementHistoryRepository,
                           ProductRepository productRepository,
                           WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.movementHistoryRepository = movementHistoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public Inventory transfer(String fromWarehouseCode, String toWarehouseCode, String sku, Integer qty, String performedBy) {
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than zero");
        }
        if (fromWarehouseCode == null || toWarehouseCode == null || fromWarehouseCode.equalsIgnoreCase(toWarehouseCode)) {
            throw new IllegalArgumentException("Source and destination warehouses must be different");
        }

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("SKU not found: " + sku));
        Warehouse fromWarehouse = warehouseRepository.findByCode(fromWarehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Source warehouse not found: " + fromWarehouseCode));
        Warehouse toWarehouse = warehouseRepository.findByCode(toWarehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Destination warehouse not found: " + toWarehouseCode));

        Inventory source = inventoryRepository.findByProductAndWarehouseAndQuantityGreaterThan(product, fromWarehouse, 0).stream()
                .filter(item -> availableQty(item) >= qty)
                .min(Comparator.comparing(item -> item.getLotBatch() == null || item.getLotBatch().getExpiryDate() == null
                        ? java.time.LocalDate.MAX
                        : item.getLotBatch().getExpiryDate()))
                .orElseThrow(() -> new IllegalArgumentException("Insufficient available quantity for SKU " + sku + " in " + fromWarehouseCode));

        source.setQuantity((source.getQuantity() == null ? 0 : source.getQuantity()) - qty);
        inventoryRepository.save(source);

        Inventory destination = inventoryRepository.findByProductAndWarehouseAndLotBatch(product, toWarehouse, source.getLotBatch())
                .orElseGet(Inventory::new);
        destination.setProduct(product);
        destination.setWarehouse(toWarehouse);
        destination.setLotBatch(source.getLotBatch());
        destination.setLocation(destination.getLocation() == null ? source.getLocation() : destination.getLocation());
        destination.setReservedQty(destination.getReservedQty() == null ? 0 : destination.getReservedQty());
        destination.setQuantity((destination.getQuantity() == null ? 0 : destination.getQuantity()) + qty);
        Inventory savedDestination = inventoryRepository.save(destination);

        String referenceNo = "XFER-" + fromWarehouseCode + "-" + toWarehouseCode + "-" + System.currentTimeMillis();
        LocalDateTime eventTime = LocalDateTime.now();

        MovementHistory transferOut = new MovementHistory();
        transferOut.setProduct(product);
        transferOut.setFromLocation(source.getLocation());
        transferOut.setQuantity(qty);
        transferOut.setMovementType(MovementType.TRANSFER_OUT);
        transferOut.setEventTime(eventTime);
        transferOut.setReferenceNo(referenceNo);
        transferOut.setPerformedBy(performedBy == null || performedBy.isBlank() ? "system" : performedBy);
        movementHistoryRepository.save(transferOut);

        MovementHistory transferIn = new MovementHistory();
        transferIn.setProduct(product);
        transferIn.setToLocation(savedDestination.getLocation());
        transferIn.setQuantity(qty);
        transferIn.setMovementType(MovementType.TRANSFER_IN);
        transferIn.setEventTime(eventTime);
        transferIn.setReferenceNo(referenceNo);
        transferIn.setPerformedBy(performedBy == null || performedBy.isBlank() ? "system" : performedBy);
        movementHistoryRepository.save(transferIn);

        return savedDestination;
    }

    private int availableQty(Inventory inventory) {
        return (inventory.getQuantity() == null ? 0 : inventory.getQuantity())
                - (inventory.getReservedQty() == null ? 0 : inventory.getReservedQty());
    }
}
