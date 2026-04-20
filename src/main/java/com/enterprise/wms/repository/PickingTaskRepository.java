package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.PickingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickingTaskRepository extends JpaRepository<PickingTask, Long> {
    Optional<PickingTask> findById(Long id);
    List<PickingTask> findAllByOrderByCreatedAtDesc();
}
