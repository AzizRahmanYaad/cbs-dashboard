package com.example.CBS.Dashboard.dto.test;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    private Long testCaseId;
    private Long defectId;
}

