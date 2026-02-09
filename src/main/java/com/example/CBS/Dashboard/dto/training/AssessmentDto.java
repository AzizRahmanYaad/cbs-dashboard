package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {
    private Long id;
    private Long programId;
    private String programTitle;
    private String title;
    private String description;
    private String assessmentType;
    private Double passingScore;
    private Double maxScore;
    private Integer timeLimitMinutes;
    private Boolean isRequired;
    private String status;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long resultsCount;
}
