package com.example.CBS.Dashboard.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingMaterialRequest {
    @NotNull(message = "Program ID is required")
    private Long programId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String materialType;
    private String filePath;
    private Long fileSize;
    private String fileName;
    private Boolean isRequired;
    private Integer displayOrder;
}
