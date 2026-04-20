package com.enterprise.wms.repository;

import com.enterprise.wms.domain.entity.Location;
import com.enterprise.wms.domain.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByWarehouseAndOccupiedLessThan(Warehouse warehouse, Integer occupied);
}
