package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.MovementType;
import com.enterprise.wms.domain.entity.*;
import com.enterprise.wms.dto.InboundDtos.GoodsReceiptRequest;
import com.enterprise.wms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InboundService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LotBatchRepository lotBatchRepository;
    private final InventoryRepository inventoryRepository;
    private final MovementHistoryRepository movementHistoryRepository;

    public InboundService(ProductRepository productRepository,
                          WarehouseRepository warehouseRepository,
                          LotBatchRepository lotBatchRepository,
                          InventoryRepository inventoryRepository,
                          MovementHistoryRepository movementHistoryRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.lotBatchRepository = lotBatchRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementHistoryRepository = movementHistoryRepository;
    }

    @Transactional
    public Inventory receiveGoods(GoodsReceiptRequest request) {
        Product product = productRepository.findByBarcode(request.barcode())
                .orElseThrow(() -> new IllegalArgumentException("Barcode not found: " + request.barcode()));
        Warehouse warehouse = warehouseRepository.findByCode(request.warehouseCode())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + request.warehouseCode()));

        LotBatch lotBatch = new LotBatch();
        lotBatch.setProduct(product);
        lotBatch.setLotNo(request.lotNo());
        lotBatch.setExpiryDate(request.expiryDate());
        lotBatch.setReceivedAt(LocalDateTime.now());
        lotBatchRepository.save(lotBatch);

        Inventory inventory = inventoryRepository.findByProductAndWarehouseAndLotBatch(product, warehouse, lotBatch)
                .orElseGet(Inventory::new);
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setLotBatch(lotBatch);
        inventory.setQuantity((inventory.getQuantity() == null ? 0 : inventory.getQuantity()) + request.quantity());
        inventory.setReservedQty(inventory.getReservedQty() == null ? 0 : inventory.getReservedQty());
        inventoryRepository.save(inventory);

        MovementHistory history = new MovementHistory();
        history.setProduct(product);
        history.setQuantity(request.quantity());
        history.setMovementType(MovementType.RECEIVE);
        history.setEventTime(LocalDateTime.now());
        history.setReferenceNo(request.grnNo());
        history.setPerformedBy(request.performedBy());
        movementHistoryRepository.save(history);

        return inventory;
    }
}
