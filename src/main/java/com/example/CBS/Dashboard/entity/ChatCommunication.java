package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCommunication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "platform", length = 50, nullable = false)
    private String platform; // WhatsApp, Signal, etc.
    
    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;
    
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;
    
    @Column(name = "action_performed", columnDefinition = "TEXT")
    private String actionPerformed;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
}

