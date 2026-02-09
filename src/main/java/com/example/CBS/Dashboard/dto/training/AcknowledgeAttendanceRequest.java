package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcknowledgeAttendanceRequest {
    @NotBlank(message = "Signature is required")
    private String signatureData;
}
