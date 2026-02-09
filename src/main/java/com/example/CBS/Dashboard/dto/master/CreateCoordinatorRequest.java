package com.example.CBS.Dashboard.dto.master;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCoordinatorRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private String employeeId;
    private String department;
    private String phone;
    private Boolean isActive;
}
