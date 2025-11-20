package com.example.CBS.Dashboard.dto.dailyreport;

import com.example.CBS.Dashboard.entity.DailyReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDto {
    private Long id;
    private LocalDate businessDate;
    private Long employeeId;
    private String employeeUsername;
    private String employeeEmail;
    
    // CBS Time Tracking
    private LocalTime cbsEndTime;
    private LocalTime cbsStartTimeNextDay;
    
    // Report Status
    private DailyReport.ReportStatus status;
    
    // Approval Information
    private Long reviewedById;
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;
    private String reviewComments;
    private String reportingLine;
    
    // Report Sections
    private List<ChatCommunicationDto> chatCommunications = new ArrayList<>();
    private List<EmailCommunicationDto> emailCommunications = new ArrayList<>();
    private List<ProblemEscalationDto> problemEscalations = new ArrayList<>();
    private List<TrainingCapacityBuildingDto> trainingCapacityBuildings = new ArrayList<>();
    private List<ProjectProgressUpdateDto> projectProgressUpdates = new ArrayList<>();
    private List<CbsTeamActivityDto> cbsTeamActivities = new ArrayList<>();
    private List<PendingActivityDto> pendingActivities = new ArrayList<>();
    private List<MeetingDto> meetings = new ArrayList<>();
    private List<AfpayCardRequestDto> afpayCardRequests = new ArrayList<>();
    private List<QrmisIssueDto> qrmisIssues = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

