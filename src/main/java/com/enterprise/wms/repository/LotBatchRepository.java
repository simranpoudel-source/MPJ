package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.LotBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LotBatchRepository extends JpaRepository<LotBatch, Long> {
    List<LotBatch> findByExpiryDateLessThanEqual(LocalDate date);
}
