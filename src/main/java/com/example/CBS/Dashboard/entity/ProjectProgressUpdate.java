package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "project_progress_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressUpdate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "project_name", length = 200, nullable = false)
    private String projectName;
    
    @Column(name = "task_or_milestone", length = 500)
    private String taskOrMilestone;
    
    @Column(name = "progress_detail", columnDefinition = "TEXT", nullable = false)
    private String progressDetail;
    
    @Column(name = "roadblocks_issues", columnDefinition = "TEXT")
    private String roadblocksIssues;
    
    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}

