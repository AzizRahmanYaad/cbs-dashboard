package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCommunication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = true;
    
    @Column(name = "sender", length = 200, nullable = false)
    private String sender;
    
    @Column(name = "receiver", length = 200, nullable = false)
    private String receiver;
    
    @Column(name = "subject", length = 500, nullable = false)
    private String subject;
    
    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;
    
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;
    
    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;
}

