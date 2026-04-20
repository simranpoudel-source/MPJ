package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.LotBatch;
import com.enterprise.wms.domain.entity.Product;
import com.enterprise.wms.domain.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Page<Inventory> findAll(Pageable pageable);
    List<Inventory> findByWarehouse(Warehouse warehouse);
    List<Inventory> findByProduct(Product product);
    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);
    List<Inventory> findByProductAndWarehouseAndQuantityGreaterThan(Product product, Warehouse warehouse, Integer quantity);
    Optional<Inventory> findByProductAndWarehouseAndLotBatch(Product product, Warehouse warehouse, LotBatch lotBatch);
}
