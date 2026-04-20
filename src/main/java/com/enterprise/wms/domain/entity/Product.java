package com.enterprise.wms.domain.entity;

import com.enterprise.wms.domain.WmsEnums.VelocityClass;
import com.enterprise.wms.domain.WmsEnums.WeightClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String sku;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String barcode;
    private Integer reorderLevel;
    @Enumerated(EnumType.STRING)
    private VelocityClass velocityClass;
    @Enumerated(EnumType.STRING)
    private WeightClass weightClass;
}
