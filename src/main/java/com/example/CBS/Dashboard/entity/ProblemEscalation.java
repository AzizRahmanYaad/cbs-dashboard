package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_escalations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemEscalation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "escalated_to", length = 200, nullable = false)
    private String escalatedTo; // person or department
    
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;
    
    @Column(name = "escalation_date_time", nullable = false)
    private LocalDateTime escalationDateTime;
    
    @Column(name = "follow_up_status", length = 100)
    private String followUpStatus;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}

