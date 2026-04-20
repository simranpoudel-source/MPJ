package com.enterprise.wms.domain.entity;

import com.enterprise.wms.domain.WmsEnums.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MovementHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Product product;
    @ManyToOne
    private Location fromLocation;
    @ManyToOne
    private Location toLocation;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    private MovementType movementType;
    private LocalDateTime eventTime;
    private String referenceNo;
    private String performedBy;
}
