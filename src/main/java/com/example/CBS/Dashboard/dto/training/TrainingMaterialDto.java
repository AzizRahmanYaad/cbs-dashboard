package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterialDto {
    private Long id;
    private Long programId;
    private String programTitle;
    private String title;
    private String description;
    private String materialType;
    private String filePath;
    private Long fileSize;
    private String fileName;
    private Boolean isRequired;
    private Integer displayOrder;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
