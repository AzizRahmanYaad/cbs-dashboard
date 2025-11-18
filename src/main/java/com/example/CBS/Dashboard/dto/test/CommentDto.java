package com.example.CBS.Dashboard.dto.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private Long createdById;
    private String createdByUsername;
    private Long testCaseId;
    private Long defectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

