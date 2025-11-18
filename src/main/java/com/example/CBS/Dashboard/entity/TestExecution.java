package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_id", nullable = false)
    private User executedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @ElementCollection
    @CollectionTable(name = "execution_attachments", joinColumns = @JoinColumn(name = "execution_id"))
    @Column(name = "file_path")
    private List<String> attachments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ExecutionStatus {
        PASSED, FAILED, BLOCKED, RETEST
    }
}

