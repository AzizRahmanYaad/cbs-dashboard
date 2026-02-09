package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "training_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TrainingProgram program;
    
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    
    @Column(nullable = false)
    private LocalDateTime endDateTime;
    
    @Column(name = "location", length = 200)
    private String location; // Physical location or virtual meeting link
    
    @Column(name = "session_type", length = 50)
    private String sessionType; // In-Person, Virtual, Hybrid
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;
    
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    
    @Column(name = "topic", length = 500)
    private String topic;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "sequence_order")
    private Integer sequenceOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();
    
    public enum SessionStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
    }
}
