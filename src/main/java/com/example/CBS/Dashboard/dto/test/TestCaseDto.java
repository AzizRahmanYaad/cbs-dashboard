package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.TestCase.Priority;
import com.example.CBS.Dashboard.entity.TestCase.TestCaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDto {
    private Long id;
    private String title;
    private String preconditions;
    private List<String> steps = new ArrayList<>();
    private String expectedResult;
    private Priority priority;
    private TestCaseStatus status;
    private Long moduleId;
    private String moduleName;
    private Long createdById;
    private String createdByUsername;
    private Long assignedToId;
    private String assignedToUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long executionCount;
    private Long defectCount;
}

