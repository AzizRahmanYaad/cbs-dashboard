package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@Data
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
    
    // CBS Time Tracking
    @Column(name = "cbs_end_time")
    private LocalTime cbsEndTime;
    
    @Column(name = "cbs_start_time_next_day")
    private LocalTime cbsStartTimeNextDay;
    
    // Report Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;
    
    // Approval Information
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;
    
    @Column(name = "reporting_line", length = 200)
    private String reportingLine;
    
    // One-to-Many relationships for report sections
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
    
    public enum ReportStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        RETURNED_FOR_CORRECTION
    }
}

