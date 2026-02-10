package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionDto {
    private Long id;
    private Long programId;
    private String programTitle;
    /**
     * Session topic/description text to display in dashboards.
     */
    private String topicName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String sessionType;
    private String status;
    private Integer maxCapacity;
    private String notes;
    private Long instructorId;
    private String instructorUsername;
    private String instructorFullName;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer enrollmentsCount;
    private Integer sequenceOrder;
}
