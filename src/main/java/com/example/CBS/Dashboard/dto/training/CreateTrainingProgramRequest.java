package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingProgramRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    // New required fields
    private Long trainingTopicId;
    private Long trainingNameId;
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingLevel; // BASIC, INTERMEDIATE, ADVANCED
    private Long trainingCategoryId;
    private Long departmentId;
    private Long trainingModuleId;
    private String facultyName;
    private Long coordinatorId;
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
    private Long instructorId;
}
