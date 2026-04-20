package com.enterprise.wms.domain.entity;

import com.enterprise.wms.domain.WmsEnums.LocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "zone", "rack", "shelf", "bin"}))
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @Column(nullable = false)
    private String zone;
    @Column(nullable = false)
    private String rack;
    @Column(nullable = false)
    private String shelf;
    @Column(nullable = false)
    private String bin;
    @Enumerated(EnumType.STRING)
    private LocationType locationType;
    private Integer capacity;
    private Integer occupied;
}
