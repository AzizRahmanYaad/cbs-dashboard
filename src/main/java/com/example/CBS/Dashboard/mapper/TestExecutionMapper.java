package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.TestExecutionDto;
import com.example.CBS.Dashboard.entity.TestExecution;
import org.springframework.stereotype.Component;

@Component
public class TestExecutionMapper {
    
    public TestExecutionDto toDto(TestExecution execution) {
        if (execution == null) return null;
        
        TestExecutionDto dto = new TestExecutionDto();
        dto.setId(execution.getId());
        
        // Safely access lazy-loaded relationships
        try {
            dto.setTestCaseId(execution.getTestCase() != null ? execution.getTestCase().getId() : null);
            dto.setTestCaseTitle(execution.getTestCase() != null ? execution.getTestCase().getTitle() : null);
        } catch (Exception e) {
            dto.setTestCaseId(null);
            dto.setTestCaseTitle(null);
        }
        
        try {
            dto.setExecutedById(execution.getExecutedBy() != null ? execution.getExecutedBy().getId() : null);
            dto.setExecutedByUsername(execution.getExecutedBy() != null ? execution.getExecutedBy().getUsername() : null);
        } catch (Exception e) {
            dto.setExecutedById(null);
            dto.setExecutedByUsername(null);
        }
        dto.setStatus(execution.getStatus());
        dto.setComments(execution.getComments());
        dto.setAttachments(execution.getAttachments());
        dto.setExecutedAt(execution.getExecutedAt());
        dto.setUpdatedAt(execution.getUpdatedAt());
        return dto;
    }
}

