package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    private Long sessionId;
    
    @NotNull(message = "Participant ID is required")
    private Long participantId;
    
    private String status;
    private LocalDateTime enrollmentDate;
    private String notes;
}
