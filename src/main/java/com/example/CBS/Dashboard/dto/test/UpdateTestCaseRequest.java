package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.TestCase.Priority;
import com.example.CBS.Dashboard.entity.TestCase.TestCaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestCaseRequest {
    private String title;
    private String preconditions;
    private List<String> steps;
    private String expectedResult;
    private Priority priority;
    private TestCaseStatus status;
    private Long moduleId;
    private Long assignedToId;
}

