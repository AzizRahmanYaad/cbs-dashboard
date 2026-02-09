package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;
    
    @NotNull(message = "Attendances are required")
    private List<StudentAttendance> attendances;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendance {
        @NotNull(message = "Participant ID is required")
        private Long participantId;
        
        @NotNull(message = "Status is required")
        private String status; // PRESENT, ABSENT, LATE, EXCUSED
        
        private String notes;
        private LocalDateTime attendanceDate;
    }
}
