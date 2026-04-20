package com.enterprise.wms.service;

import com.enterprise.wms.domain.entity.AuditLog;
import com.enterprise.wms.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> allLogs() {
        return allLogs(PageRequest.of(0, 50, Sort.by(Sort.Order.desc("id")))).getContent();
    }

    public Page<AuditLog> allLogs(Pageable pageable) {
        Pageable resolved = (pageable == null || pageable.isUnpaged())
                ? PageRequest.of(0, 50, Sort.by(Sort.Order.desc("id")))
                : pageable;
        return auditLogRepository.findAll(resolved);
    }
}
