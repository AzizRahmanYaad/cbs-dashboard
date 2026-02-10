package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingSessionRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "End date/time is required")
    private LocalDateTime endDateTime;
    
    private String location;
    private String sessionType;
    private String status;
    private Integer maxCapacity;
    private String notes;
    /**
     * Optional free-text topic/description for this specific session.
     * This is shown on teacher/student dashboards and in reports.
     */
    private String topic;
    private Long instructorId;
}
