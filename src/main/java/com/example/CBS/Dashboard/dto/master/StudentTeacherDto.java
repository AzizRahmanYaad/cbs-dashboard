package com.example.CBS.Dashboard.dto.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTeacherDto {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String type; // STUDENT or TEACHER
    private String employeeId;
    private String studentId;
    private String department;
    private String phone;
    private String qualification;
    private String specialization;
    private Boolean isActive;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
