package com.example.CBS.Dashboard.dto.master;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingTopicRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private Boolean isActive;
}
