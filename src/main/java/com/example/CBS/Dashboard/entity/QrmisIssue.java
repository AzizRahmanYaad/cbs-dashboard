package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qrmis_issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrmisIssue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "problem_type", length = 200, nullable = false)
    private String problemType;
    
    @Column(name = "problem_description", columnDefinition = "TEXT", nullable = false)
    private String problemDescription;
    
    @Column(name = "solution_provided", columnDefinition = "TEXT")
    private String solutionProvided;
    
    @Column(name = "posted_by", length = 200)
    private String postedBy;
    
    @Column(name = "authorized_by", length = 200)
    private String authorizedBy;
    
    @Column(name = "supporting_documents_archived", length = 500)
    private String supportingDocumentsArchived; // Path or reference
    
    @Column(name = "operator", length = 200)
    private String operator;
}

