package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEngagementDto {
    private Long participantId;
    private String fullName;
    private String email;
    private String status;           // PRESENT, ABSENT, LATE, EXCUSED
    private String notes;
    private Double attendancePercent; // Overall for this session/program
    /** Base64 signature image for attended students (PRESENT, LATE, EXCUSED). */
    private String signatureData;
}
