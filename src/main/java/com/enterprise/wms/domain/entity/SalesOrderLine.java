package com.enterprise.wms.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SalesOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private SalesOrder orderRef;
    @ManyToOne(optional = false)
    private Product product;
    private Integer requestedQty;
    private Integer allocatedQty;
}
