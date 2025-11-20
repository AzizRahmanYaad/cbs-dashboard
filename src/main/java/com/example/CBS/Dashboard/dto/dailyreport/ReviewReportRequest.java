package com.example.CBS.Dashboard.dto.dailyreport;

import com.example.CBS.Dashboard.entity.DailyReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportRequest {
    private DailyReport.ReportStatus status; // APPROVED, REJECTED, RETURNED_FOR_CORRECTION
    private String reviewComments;
}

