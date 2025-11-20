package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "meeting_type", length = 100, nullable = false)
    private String meetingType; // Internal/External
    
    @Column(name = "topic", length = 500, nullable = false)
    private String topic;
    
    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;
    
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;
    
    @Column(name = "next_step", columnDefinition = "TEXT")
    private String nextStep;
    
    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants; // comma-separated or JSON
}

