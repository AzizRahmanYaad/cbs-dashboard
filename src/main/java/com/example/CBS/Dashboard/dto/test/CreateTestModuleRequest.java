package com.example.CBS.Dashboard.dto.test;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestModuleRequest {
    @NotBlank(message = "Module name is required")
    private String name;
    private String description;
}

