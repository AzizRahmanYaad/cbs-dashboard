package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTeacherRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
