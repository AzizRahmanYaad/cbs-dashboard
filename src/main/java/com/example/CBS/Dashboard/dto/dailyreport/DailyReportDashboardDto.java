package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

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

    public Long getTotalReports() { return totalReports; }
    public void setTotalReports(Long totalReports) { this.totalReports = totalReports; }
    public Long getPendingReports() { return pendingReports; }
    public void setPendingReports(Long pendingReports) { this.pendingReports = pendingReports; }
    public Long getApprovedReports() { return approvedReports; }
    public void setApprovedReports(Long approvedReports) { this.approvedReports = approvedReports; }
    public Long getRejectedReports() { return rejectedReports; }
    public void setRejectedReports(Long rejectedReports) { this.rejectedReports = rejectedReports; }
    public Long getDraftReports() { return draftReports; }
    public void setDraftReports(Long draftReports) { this.draftReports = draftReports; }
    public Long getTotalEscalations() { return totalEscalations; }
    public void setTotalEscalations(Long totalEscalations) { this.totalEscalations = totalEscalations; }
    public Long getTotalPendingActivities() { return totalPendingActivities; }
    public void setTotalPendingActivities(Long totalPendingActivities) { this.totalPendingActivities = totalPendingActivities; }
    public Long getTotalQrmisIssues() { return totalQrmisIssues; }
    public void setTotalQrmisIssues(Long totalQrmisIssues) { this.totalQrmisIssues = totalQrmisIssues; }
    public Map<String, Long> getReportsByStatus() { return reportsByStatus; }
    public void setReportsByStatus(Map<String, Long> reportsByStatus) { this.reportsByStatus = reportsByStatus; }
    public Map<String, Long> getReportsByEmployee() { return reportsByEmployee; }
    public void setReportsByEmployee(Map<String, Long> reportsByEmployee) { this.reportsByEmployee = reportsByEmployee; }
    public Map<String, Long> getEscalationsByType() { return escalationsByType; }
    public void setEscalationsByType(Map<String, Long> escalationsByType) { this.escalationsByType = escalationsByType; }
}

