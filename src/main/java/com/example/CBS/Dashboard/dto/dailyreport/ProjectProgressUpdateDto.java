package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressUpdateDto {
    private Long id;
    private String projectName;
    private String taskOrMilestone;
    private String progressDetail;
    private String roadblocksIssues;
    private LocalDate estimatedCompletionDate;
    private String comments;
}

