package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.SalesOrder;
import com.enterprise.wms.domain.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    List<SalesOrderLine> findByOrderRef(SalesOrder order);
}
