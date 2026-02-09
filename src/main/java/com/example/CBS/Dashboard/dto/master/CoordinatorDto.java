package com.example.CBS.Dashboard.dto.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatorDto {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String employeeId;
    private String department;
    private String phone;
    private Boolean isActive;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
