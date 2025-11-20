package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "afpay_card_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AfpayCardRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "request_type", length = 100, nullable = false)
    private String requestType; // Issue, Renew, PIN reissue, Withdrawal issue, Registration
    
    @Column(name = "requested_by", length = 200, nullable = false)
    private String requestedBy;
    
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;
    
    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;
    
    @Column(name = "supporting_document_path", length = 500)
    private String supportingDocumentPath; // Path to scanned document
    
    @Column(name = "archived_date")
    private LocalDate archivedDate;
    
    @Column(name = "operator", length = 200)
    private String operator;
}

