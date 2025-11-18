package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.TestCaseDto;
import com.example.CBS.Dashboard.entity.TestCase;
import org.springframework.stereotype.Component;

@Component
public class TestCaseMapper {
    
    public TestCaseDto toDto(TestCase testCase) {
        if (testCase == null) return null;
        
        TestCaseDto dto = new TestCaseDto();
        dto.setId(testCase.getId());
        dto.setTitle(testCase.getTitle());
        dto.setPreconditions(testCase.getPreconditions());
        dto.setSteps(testCase.getSteps());
        dto.setExpectedResult(testCase.getExpectedResult());
        dto.setPriority(testCase.getPriority());
        dto.setStatus(testCase.getStatus());
        dto.setModuleId(testCase.getModule() != null ? testCase.getModule().getId() : null);
        dto.setModuleName(testCase.getModule() != null ? testCase.getModule().getName() : null);
        dto.setCreatedById(testCase.getCreatedBy() != null ? testCase.getCreatedBy().getId() : null);
        dto.setCreatedByUsername(testCase.getCreatedBy() != null ? testCase.getCreatedBy().getUsername() : null);
        dto.setAssignedToId(testCase.getAssignedTo() != null ? testCase.getAssignedTo().getId() : null);
        dto.setAssignedToUsername(testCase.getAssignedTo() != null ? testCase.getAssignedTo().getUsername() : null);
        dto.setCreatedAt(testCase.getCreatedAt());
        dto.setUpdatedAt(testCase.getUpdatedAt());
        
        // Safely get execution and defect counts to avoid LazyInitializationException
        try {
            dto.setExecutionCount(testCase.getExecutions() != null ? (long) testCase.getExecutions().size() : 0L);
        } catch (Exception e) {
            dto.setExecutionCount(0L);
        }
        
        try {
            dto.setDefectCount(testCase.getDefects() != null ? (long) testCase.getDefects().size() : 0L);
        } catch (Exception e) {
            dto.setDefectCount(0L);
        }
        
        return dto;
    }
}

