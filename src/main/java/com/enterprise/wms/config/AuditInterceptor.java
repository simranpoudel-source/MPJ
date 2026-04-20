package com.enterprise.wms.config;

import com.enterprise.wms.domain.entity.AuditLog;
import com.enterprise.wms.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    private final AuditLogRepository auditLogRepository;

    public AuditInterceptor(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuditLog log = new AuditLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.setUsername(authentication == null ? "anonymous" : authentication.getName());
        log.setMethod(request.getMethod());
        log.setPath(request.getRequestURI());
        log.setStatusCode(response.getStatus());
        log.setEventTime(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
