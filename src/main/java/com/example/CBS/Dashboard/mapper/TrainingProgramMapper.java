package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.training.TrainingProgramDto;
import com.example.CBS.Dashboard.entity.TrainingProgram;
import org.springframework.stereotype.Component;

@Component
public class TrainingProgramMapper {
    
    public TrainingProgramDto toDto(TrainingProgram program) {
        if (program == null) {
            return null;
        }
        
        try {
            TrainingProgramDto dto = new TrainingProgramDto();
            dto.setId(program.getId());
            dto.setTitle(program.getTitle() != null ? program.getTitle() : "");
            dto.setDescription(program.getDescription());
        
        // New fields
        if (program.getTrainingTopic() != null) {
            dto.setTrainingTopicId(program.getTrainingTopic().getId());
            dto.setTrainingTopicName(program.getTrainingTopic().getName());
        }
        if (program.getTrainingName() != null) {
            dto.setTrainingNameId(program.getTrainingName().getId());
            dto.setTrainingNameName(program.getTrainingName().getName());
        }
        if (program.getTrainingNameString() != null) {
            dto.setTrainingName(program.getTrainingNameString());
        } else if (program.getTrainingName() != null) {
            dto.setTrainingName(program.getTrainingName().getName());
        }
        dto.setTrainingDate(program.getTrainingDate());
        dto.setTrainingLevel(program.getTrainingLevel() != null ? program.getTrainingLevel().name() : null);
        if (program.getTrainingCategory() != null) {
            dto.setTrainingCategoryId(program.getTrainingCategory().getId());
            dto.setTrainingCategoryName(program.getTrainingCategory().getName());
        }
        if (program.getDepartment() != null) {
            dto.setDepartmentId(program.getDepartment().getId());
            dto.setDepartmentName(program.getDepartment().getName());
        }
        if (program.getTrainingModule() != null) {
            dto.setTrainingModuleId(program.getTrainingModule().getId());
            dto.setTrainingModuleName(program.getTrainingModule().getName());
        }
        dto.setFacultyName(program.getFacultyName());
        if (program.getCoordinator() != null) {
            dto.setCoordinatorId(program.getCoordinator().getId());
            if (program.getCoordinator().getUser() != null) {
                dto.setCoordinatorName(program.getCoordinator().getUser().getUsername());
            }
        }
        dto.setTrainingType(program.getTrainingType() != null ? program.getTrainingType().name() : null);
        dto.setExamType(program.getExamType() != null ? program.getExamType().name() : null);
        dto.setHasArticleMaterial(program.getHasArticleMaterial());
        dto.setHasVideoMaterial(program.getHasVideoMaterial());
        dto.setHasSlideMaterial(program.getHasSlideMaterial());
        dto.setThumbnailImagePath(program.getThumbnailImagePath());
        
        // Legacy fields
        dto.setCategory(program.getCategory());
        dto.setDurationHours(program.getDurationHours());
        dto.setStatus(program.getStatus() != null ? program.getStatus().name() : null);
        dto.setMaxParticipants(program.getMaxParticipants());
        dto.setPrerequisites(program.getPrerequisites());
        dto.setLearningObjectives(program.getLearningObjectives());
        
        if (program.getCreatedBy() != null) {
            dto.setCreatedById(program.getCreatedBy().getId());
            dto.setCreatedByUsername(program.getCreatedBy().getUsername());
        }
        
        if (program.getInstructor() != null) {
            dto.setInstructorId(program.getInstructor().getId());
            dto.setInstructorUsername(program.getInstructor().getUsername());
        }
        
        dto.setCreatedAt(program.getCreatedAt());
        dto.setUpdatedAt(program.getUpdatedAt());
        
        // Safely get sessions count - avoid lazy loading issues
        try {
            if (program.getSessions() != null) {
                dto.setSessionsCount((long) program.getSessions().size());
            } else {
                dto.setSessionsCount(0L);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not get sessions count for program " + program.getId() + ": " + e.getMessage());
            dto.setSessionsCount(0L);
        }
        
        // Safely get enrollments count - avoid lazy loading issues
        try {
            if (program.getEnrollments() != null) {
                dto.setEnrollmentsCount((long) program.getEnrollments().size());
            } else {
                dto.setEnrollmentsCount(0L);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not get enrollments count for program " + program.getId() + ": " + e.getMessage());
            dto.setEnrollmentsCount(0L);
        }
        
        return dto;
        } catch (Exception e) {
            System.err.println("Error mapping TrainingProgram to DTO (ID: " + (program != null ? program.getId() : "null") + "): " + e.getMessage());
            e.printStackTrace();
            // Return a minimal DTO instead of null to prevent breaking the list
            TrainingProgramDto dto = new TrainingProgramDto();
            if (program != null) {
                dto.setId(program.getId());
                dto.setTitle(program.getTitle() != null ? program.getTitle() : "Error loading program");
            }
            return dto;
        }
    }
}
