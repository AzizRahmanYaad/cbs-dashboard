package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "training_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgram {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // New fields as per requirements
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_topic_id")
    private TrainingTopic trainingTopic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_name_id")
    private TrainingName trainingName;
    
    @Column(name = "training_name", length = 200)
    private String trainingNameString;
    
    @Column(name = "training_date")
    private java.time.LocalDate trainingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "training_level", length = 20)
    private TrainingLevel trainingLevel; // BASIC, INTERMEDIATE, ADVANCED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_category_id")
    private TrainingCategoryMaster trainingCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_module_id")
    private TrainingModule trainingModule;
    
    @Column(name = "faculty_name", length = 200)
    private String facultyName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private Coordinator coordinator;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", length = 20)
    private TrainingType trainingType; // ON_SITE, ONLINE, ON_JOB
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 30)
    private ExamType examType; // PRE_TRAINING_EXAM, POST_TRAINING_EXAM
    
    @Column(name = "has_article_material")
    private Boolean hasArticleMaterial = false;
    
    @Column(name = "has_video_material")
    private Boolean hasVideoMaterial = false;
    
    @Column(name = "has_slide_material")
    private Boolean hasSlideMaterial = false;
    
    @Column(name = "thumbnail_image_path", length = 500)
    private String thumbnailImagePath;
    
    // Legacy fields (keeping for backward compatibility)
    @Column(name = "category", length = 100)
    private String category; // e.g., Technical, Soft Skills, Compliance, Safety
    
    @Column(name = "duration_hours")
    private Integer durationHours;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status = TrainingStatus.DRAFT;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;
    
    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainingSession> sessions = new HashSet<>();
    
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();
    
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingMaterial> materials = new ArrayList<>();
    
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assessment> assessments = new ArrayList<>();
    
    public enum TrainingStatus {
        DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED, ARCHIVED
    }
    
    public enum TrainingLevel {
        BASIC, INTERMEDIATE, ADVANCED
    }
    
    public enum TrainingType {
        ON_SITE, ONLINE, ON_JOB
    }
    
    public enum ExamType {
        PRE_TRAINING_EXAM, POST_TRAINING_EXAM
    }
}
