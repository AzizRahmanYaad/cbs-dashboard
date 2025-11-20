package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "pending_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "title", length = 500, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "status", length = 100, nullable = false)
    private String status;
    
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;
    
    @Column(name = "responsible_person", length = 200)
    private String responsiblePerson;
}

