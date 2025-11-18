package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.TestExecution.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionDto {
    private Long id;
    private Long testCaseId;
    private String testCaseTitle;
    private Long executedById;
    private String executedByUsername;
    private ExecutionStatus status;
    private String comments;
    private List<String> attachments = new ArrayList<>();
    private LocalDateTime executedAt;
    private LocalDateTime updatedAt;
}

