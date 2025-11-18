package com.example.CBS.Dashboard.dto.test;

import com.example.CBS.Dashboard.entity.Defect.DefectSeverity;
import com.example.CBS.Dashboard.entity.Defect.DefectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDefectRequest {
    private String title;
    private String description;
    private DefectSeverity severity;
    private DefectStatus status;
    private Long assignedToId;
}

