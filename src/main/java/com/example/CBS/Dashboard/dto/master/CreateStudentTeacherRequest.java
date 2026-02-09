package com.example.CBS.Dashboard.dto.master;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentTeacherRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Type is required (STUDENT or TEACHER)")
    private String type; // STUDENT or TEACHER
    
    private String employeeId;
    private String studentId;
    private String department;
    private String phone;
    private String qualification;
    private String specialization;
    private Boolean isActive;
}
