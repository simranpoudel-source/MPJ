package com.enterprise.wms.repository;

import com.enterprise.wms.domain.WmsEnums.RoleName;
import com.enterprise.wms.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
