package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingReportDto {
    private Long totalPrograms;
    private Long totalSessions;
    private Long totalEnrollments;
    private Long totalParticipants;
    private Long totalAssessments;
    private Long totalCertifications;
    
    private Map<String, Long> programsByStatus;
    private Map<String, Long> sessionsByStatus;
    private Map<String, Long> enrollmentsByStatus;
    private Map<String, Long> programsByCategory;
    
    private Double averageCompletionRate;
    private Double averageAttendanceRate;
    private Double averageAssessmentScore;
    
    private Long upcomingSessions;
    private Long activePrograms;
    private Long pendingEnrollments;
}
