package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.Defect.DefectSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDefectRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Severity is required")
    private DefectSeverity severity;
    private Long testCaseId;
    private Long testExecutionId;
    private Long assignedToId;
    private List<String> attachments = new ArrayList<>();
}

