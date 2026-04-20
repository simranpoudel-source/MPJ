package com.enterprise.wms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(name = "uq_inventory_product_warehouse_lot", columnNames = {"product_id", "warehouse_id", "lot_batch_id"}))
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Product product;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @ManyToOne
    private Location location;
    @ManyToOne
    private LotBatch lotBatch;
    private Integer quantity;
    private Integer reservedQty;
}
