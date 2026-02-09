package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultDto {
    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private Long participantId;
    private String participantUsername;
    private String participantFullName;
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
    private Long gradedById;
    private String gradedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
