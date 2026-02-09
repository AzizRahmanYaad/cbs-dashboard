package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "max_score")
    private Double maxScore;
    
    @Column(name = "percentage_score")
    private Double percentageScore;
    
    @Column(name = "is_passed")
    private Boolean isPassed;
    
    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;
    
    @Column(name = "answers", columnDefinition = "TEXT")
    private String answers; // JSON string of answers
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    private User gradedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
