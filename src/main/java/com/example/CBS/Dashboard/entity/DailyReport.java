package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"business_date", "employee_id"})
})
@NoArgsConstructor
@AllArgsConstructor
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "cbs_end_time")
    private LocalTime cbsEndTime;

    @Column(name = "cbs_start_time_next_day")
    private LocalTime cbsStartTimeNextDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reporting_line", length = 200)
    private String reportingLine;

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatCommunication> chatCommunications = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailCommunication> emailCommunications = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemEscalation> problemEscalations = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingCapacityBuilding> trainingCapacityBuildings = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectProgressUpdate> projectProgressUpdates = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CbsTeamActivity> cbsTeamActivities = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PendingActivity> pendingActivities = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AfpayCardRequest> afpayCardRequests = new ArrayList<>();

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QrmisIssue> qrmisIssues = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }
    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }
    public LocalTime getCbsEndTime() { return cbsEndTime; }
    public void setCbsEndTime(LocalTime cbsEndTime) { this.cbsEndTime = cbsEndTime; }
    public LocalTime getCbsStartTimeNextDay() { return cbsStartTimeNextDay; }
    public void setCbsStartTimeNextDay(LocalTime cbsStartTimeNextDay) { this.cbsStartTimeNextDay = cbsStartTimeNextDay; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }
    public String getReportingLine() { return reportingLine; }
    public void setReportingLine(String reportingLine) { this.reportingLine = reportingLine; }
    public List<ChatCommunication> getChatCommunications() { return chatCommunications; }
    public void setChatCommunications(List<ChatCommunication> chatCommunications) { this.chatCommunications = chatCommunications; }
    public List<EmailCommunication> getEmailCommunications() { return emailCommunications; }
    public void setEmailCommunications(List<EmailCommunication> emailCommunications) { this.emailCommunications = emailCommunications; }
    public List<ProblemEscalation> getProblemEscalations() { return problemEscalations; }
    public void setProblemEscalations(List<ProblemEscalation> problemEscalations) { this.problemEscalations = problemEscalations; }
    public List<TrainingCapacityBuilding> getTrainingCapacityBuildings() { return trainingCapacityBuildings; }
    public void setTrainingCapacityBuildings(List<TrainingCapacityBuilding> trainingCapacityBuildings) { this.trainingCapacityBuildings = trainingCapacityBuildings; }
    public List<ProjectProgressUpdate> getProjectProgressUpdates() { return projectProgressUpdates; }
    public void setProjectProgressUpdates(List<ProjectProgressUpdate> projectProgressUpdates) { this.projectProgressUpdates = projectProgressUpdates; }
    public List<CbsTeamActivity> getCbsTeamActivities() { return cbsTeamActivities; }
    public void setCbsTeamActivities(List<CbsTeamActivity> cbsTeamActivities) { this.cbsTeamActivities = cbsTeamActivities; }
    public List<PendingActivity> getPendingActivities() { return pendingActivities; }
    public void setPendingActivities(List<PendingActivity> pendingActivities) { this.pendingActivities = pendingActivities; }
    public List<Meeting> getMeetings() { return meetings; }
    public void setMeetings(List<Meeting> meetings) { this.meetings = meetings; }
    public List<AfpayCardRequest> getAfpayCardRequests() { return afpayCardRequests; }
    public void setAfpayCardRequests(List<AfpayCardRequest> afpayCardRequests) { this.afpayCardRequests = afpayCardRequests; }
    public List<QrmisIssue> getQrmisIssues() { return qrmisIssues; }
    public void setQrmisIssues(List<QrmisIssue> qrmisIssues) { this.qrmisIssues = qrmisIssues; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum ReportStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        RETURNED_FOR_CORRECTION
    }
}

