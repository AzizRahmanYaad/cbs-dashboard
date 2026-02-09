package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDto {
    private Long id;
    private Long enrollmentId;
    private Long programId;
    private String programTitle;
    private Long participantId;
    private String participantUsername;
    private String participantFullName;
    private String certificateNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isValid;
    private String certificateType;
    private String verificationUrl;
    private String filePath;
    private Long issuedById;
    private String issuedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
