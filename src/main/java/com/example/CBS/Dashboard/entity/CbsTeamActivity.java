package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cbs_team_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbsTeamActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "branch", length = 100)
    private String branch;
    
    @Column(name = "account_number", length = 100)
    private String accountNumber;
    
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;
    
    @Column(name = "final_status", length = 100)
    private String finalStatus;
    
    @Column(name = "activity_type", length = 100)
    private String activityType; // Allowing without check number, Reversals, etc.
}

