package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.DefectDto;
import com.example.CBS.Dashboard.entity.Defect;
import org.springframework.stereotype.Component;

@Component
public class DefectMapper {
    
    public DefectDto toDto(Defect defect) {
        if (defect == null) return null;
        
        DefectDto dto = new DefectDto();
        dto.setId(defect.getId());
        dto.setTitle(defect.getTitle());
        dto.setDescription(defect.getDescription());
        dto.setSeverity(defect.getSeverity());
        dto.setStatus(defect.getStatus());
        
        // Safely access lazy-loaded relationships
        try {
            dto.setTestCaseId(defect.getTestCase() != null ? defect.getTestCase().getId() : null);
            dto.setTestCaseTitle(defect.getTestCase() != null ? defect.getTestCase().getTitle() : null);
        } catch (Exception e) {
            dto.setTestCaseId(null);
            dto.setTestCaseTitle(null);
        }
        
        try {
            dto.setTestExecutionId(defect.getTestExecution() != null ? defect.getTestExecution().getId() : null);
        } catch (Exception e) {
            dto.setTestExecutionId(null);
        }
        
        try {
            dto.setReportedById(defect.getReportedBy() != null ? defect.getReportedBy().getId() : null);
            dto.setReportedByUsername(defect.getReportedBy() != null ? defect.getReportedBy().getUsername() : null);
        } catch (Exception e) {
            dto.setReportedById(null);
            dto.setReportedByUsername(null);
        }
        
        try {
            dto.setAssignedToId(defect.getAssignedTo() != null ? defect.getAssignedTo().getId() : null);
            dto.setAssignedToUsername(defect.getAssignedTo() != null ? defect.getAssignedTo().getUsername() : null);
        } catch (Exception e) {
            dto.setAssignedToId(null);
            dto.setAssignedToUsername(null);
        }
        dto.setAttachments(defect.getAttachments());
        dto.setCreatedAt(defect.getCreatedAt());
        dto.setUpdatedAt(defect.getUpdatedAt());
        return dto;
    }
}

