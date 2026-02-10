package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleSessionReportDto {
    private Long sessionId;
    private String programTitle;
    private String sessionTopic;
    private LocalDateTime startDateTime;
    private String instructorName;
    /** Instructor's e-signature (base64) for this session. */
    private String instructorSignatureData;
    private String sessionType;
    private String notes;
    private List<String> contentCoverage;      // Topic points / materials covered
    private List<StudentEngagementDto> studentEngagement;
    private List<AttendeeSignatureDto> attendedStudentSignatures;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int excusedCount;
    private int totalEnrolled;
}
