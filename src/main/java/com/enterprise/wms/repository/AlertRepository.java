package com.enterprise.wms.repository;

import com.enterprise.wms.domain.WmsEnums.AlertType;
import com.enterprise.wms.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAlertTypeAndResolvedFalse(AlertType type);
    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();
}
