package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificationRequest {
    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;
    
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isValid;
    private String certificateType;
    private String verificationUrl;
    private String filePath;
}
