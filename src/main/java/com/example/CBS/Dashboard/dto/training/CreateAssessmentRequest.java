package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String assessmentType;
    private Double passingScore;
    private Double maxScore;
    private Integer timeLimitMinutes;
    private Boolean isRequired;
    private String status;
}
