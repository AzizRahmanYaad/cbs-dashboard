package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.entity.AuditLog;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logAction(String entityType, Long entityId, String action, User user, 
                         String oldValue, String description) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setUser(user);
        log.setOldValue(oldValue);
        log.setDescription(description);
        auditLogRepository.save(log);
    }
}

