package com.enterprise.wms.domain.entity;

import com.enterprise.wms.domain.WmsEnums.PickingStrategy;
import com.enterprise.wms.domain.WmsEnums.PickingTaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "picking_task")
public class PickingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private SalesOrder orderRef;
    private String workerUsername;
    private String waveNo;
    @Enumerated(EnumType.STRING)
    private PickingStrategy strategy;
    @Enumerated(EnumType.STRING)
    private PickingTaskStatus status;
    @Column(name = "progress_pct", nullable = false)
    private Integer pickingProgressPct = 0;
    private LocalDateTime createdAt;
}
