package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbsTeamActivityDto {
    private Long id;
    private String description;
    private String branch;
    private String accountNumber;
    private String actionTaken;
    private String finalStatus;
    private String activityType;
}

