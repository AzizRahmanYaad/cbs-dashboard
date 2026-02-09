package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long sessionId;
    private String sessionTitle;
    private Long participantId;
    private String participantUsername;
    private String participantFullName;
    private String participantEmail;
    private String status;
    private LocalDateTime attendanceDate;
    private String notes;
    private Long markedById;
    private String markedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** E-signature image (base64) for audit and reporting. */
    private String signatureData;
    /** PRESENT or ACKNOWLEDGMENT */
    private String signatureType;
    private LocalDateTime signedAt;
}
