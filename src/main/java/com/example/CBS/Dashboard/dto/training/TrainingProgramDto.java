package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramDto {
    private Long id;
    private String title;
    private String description;
    
    // New fields
    private Long trainingTopicId;
    private String trainingTopicName;
    private Long trainingNameId;
    private String trainingNameName;
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingLevel; // BASIC, INTERMEDIATE, ADVANCED
    private Long trainingCategoryId;
    private String trainingCategoryName;
    private Long departmentId;
    private String departmentName;
    private Long trainingModuleId;
    private String trainingModuleName;
    private String facultyName;
    private Long coordinatorId;
    private String coordinatorName;
    private String trainingType; // ON_SITE, ONLINE, ON_JOB
    private String examType; // PRE_TRAINING_EXAM, POST_TRAINING_EXAM
    private Boolean hasArticleMaterial;
    private Boolean hasVideoMaterial;
    private Boolean hasSlideMaterial;
    private String thumbnailImagePath;
    
    // Legacy fields
    private String category;
    private Integer durationHours;
    private String status;
    private Integer maxParticipants;
    private String prerequisites;
    private String learningObjectives;
    private Long createdById;
    private String createdByUsername;
    private Long instructorId;
    private String instructorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long sessionsCount;
    private Long enrollmentsCount;
}
