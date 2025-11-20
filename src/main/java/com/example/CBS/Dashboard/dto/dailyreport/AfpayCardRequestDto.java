package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AfpayCardRequestDto {
    private Long id;
    private String requestType;
    private String requestedBy;
    private LocalDate requestDate;
    private String resolutionDetails;
    private String supportingDocumentPath;
    private LocalDate archivedDate;
    private String operator;
}

