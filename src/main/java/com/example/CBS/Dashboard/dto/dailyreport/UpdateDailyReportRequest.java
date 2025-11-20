package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDailyReportRequest {
    private LocalTime cbsEndTime;
    private LocalTime cbsStartTimeNextDay;
    private String reportingLine;
    
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
}

