package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.Defect.DefectSeverity;
import com.example.CBS.Dashboard.entity.Defect.DefectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefectDto {
    private Long id;
    private String title;
    private String description;
    private DefectSeverity severity;
    private DefectStatus status;
    private Long testCaseId;
    private String testCaseTitle;
    private Long testExecutionId;
    private Long reportedById;
    private String reportedByUsername;
    private Long assignedToId;
    private String assignedToUsername;
    private List<String> attachments = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

