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
@Table(name = "defects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Defect {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectSeverity severity = DefectSeverity.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefectStatus status = DefectStatus.NEW;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_execution_id")
    private TestExecution testExecution;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;
    
    @ElementCollection
    @CollectionTable(name = "defect_attachments", joinColumns = @JoinColumn(name = "defect_id"))
    @Column(name = "file_path")
    private List<String> attachments = new ArrayList<>();
    
    @OneToMany(mappedBy = "defect", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DefectSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum DefectStatus {
        NEW, IN_PROGRESS, RESOLVED, CLOSED
    }
}

