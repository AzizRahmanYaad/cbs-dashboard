package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentResultRequest {
    @NotNull(message = "Assessment ID is required")
    private Long assessmentId;
    
    @NotNull(message = "Participant ID is required")
    private Long participantId;
    
    private Double score;
    private Double maxScore;
    private Double percentageScore;
    private Boolean isPassed;
    private Integer attemptNumber;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeTakenMinutes;
    private String answers;
    private String feedback;
}
