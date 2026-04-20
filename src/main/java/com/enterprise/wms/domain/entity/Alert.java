package com.enterprise.wms.domain.entity;

import com.enterprise.wms.domain.WmsEnums.AlertType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    private String message;
    private String severity;
    private Boolean resolved;
    private LocalDateTime createdAt;
}
