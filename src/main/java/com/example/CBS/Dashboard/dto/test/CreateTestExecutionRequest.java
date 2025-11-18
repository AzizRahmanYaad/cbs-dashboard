package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.TestExecution.ExecutionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestExecutionRequest {
    @NotNull(message = "Test case ID is required")
    private Long testCaseId;
    @NotNull(message = "Execution status is required")
    private ExecutionStatus status;
    private String comments;
    private List<String> attachments = new ArrayList<>();
}

