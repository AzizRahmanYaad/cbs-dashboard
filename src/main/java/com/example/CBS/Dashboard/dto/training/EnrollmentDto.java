package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    private Long id;
    private Long programId;
    private String programTitle;
    private Long sessionId;
    private LocalDateTime sessionStartDateTime;
    private Long participantId;
    private String participantUsername;
    private String participantFullName;
    private String participantEmail;
    private String status;
    private LocalDateTime enrollmentDate;
    private LocalDateTime completionDate;
    private Double attendancePercentage;
    private Double finalScore;
    private String notes;
    private Long enrolledById;
    private String enrolledByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
