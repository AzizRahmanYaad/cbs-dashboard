package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDashboardDto {
    private Long totalReports;
    private Long pendingReports;
    private Long approvedReports;
    private Long rejectedReports;
    private Long draftReports;
    
    private Long totalEscalations;
    private Long totalPendingActivities;
    private Long totalQrmisIssues;
    
    private Map<String, Long> reportsByStatus;
    private Map<String, Long> reportsByEmployee;
    private Map<String, Long> escalationsByType;
}

