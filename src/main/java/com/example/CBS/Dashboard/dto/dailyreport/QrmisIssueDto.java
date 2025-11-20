package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrmisIssueDto {
    private Long id;
    private String problemType;
    private String problemDescription;
    private String solutionProvided;
    private String postedBy;
    private String authorizedBy;
    private String supportingDocumentsArchived;
    private String operator;
}

