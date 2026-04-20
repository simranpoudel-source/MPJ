package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.MovementHistory;
import com.enterprise.wms.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementHistoryRepository extends JpaRepository<MovementHistory, Long> {
    Page<MovementHistory> findAll(Pageable pageable);
    List<MovementHistory> findByEventTimeAfter(LocalDateTime eventTime);
    List<MovementHistory> findByProduct(Product product);
}
