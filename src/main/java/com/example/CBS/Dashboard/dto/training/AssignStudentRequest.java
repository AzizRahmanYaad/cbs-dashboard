package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignStudentRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    @NotNull(message = "Student IDs are required")
    private List<Long> studentIds;
}
