package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemEscalationDto {
    private Long id;
    private String escalatedTo;
    private String reason;
    private LocalDateTime escalationDateTime;
    private String followUpStatus;
    private String comments;
}

