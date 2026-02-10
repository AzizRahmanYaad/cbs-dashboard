package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Executive-level CFO view of the training portfolio.
 *
 * This DTO is intentionally aggregated so the frontend can:
 * - Render KPI tiles
 * - Drive multiple chart types (bars, lines, heatmaps)
 * - Show risk/alert cards
 *
 * Most numeric fields are percentages in the 0–100 range unless otherwise stated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfoTrainingDashboardDto {

    private SummaryKpis summary;

    /** Per-program performance rows for bar / table views. */
    private List<ProgramPerformanceRow> programPerformance;

    /** Monthly or quarterly trend metrics for line charts. */
    private List<TimeSeriesPoint> performanceTrend;

    /** Department-level training and attendance metrics (for bars / pies / heatmaps). */
    private List<CategoryValue> departmentAttendance;

    /** Instructor and coordinator productivity metrics. */
    private List<InstructorMetric> instructorProductivity;

    /** High-level material usage / engagement metrics. */
    private List<CategoryValue> materialUsage;

    /** Aggregated process efficiency metrics. */
    private ProcessEfficiency processEfficiency;

    /** Risk and alert items with severity flags for red/yellow/green cards. */
    private List<RiskItem> risks;

    // ---- Nested DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryKpis {
        private long totalPrograms;
        private long activePrograms;
        private long completedPrograms;
        private long totalSessions;
        private double sessionCompletionRate;     // %
        private double studentAttendanceRate;     // %
        private double teacherParticipationRate;  // %
        private double materialEngagementScore;   // 0–100 synthetic index
        private double trainingUtilizationRate;   // %
        private Double averageCompletionTimeDays; // nullable
        private Long underperformingPrograms;
        private Long delayedOrCancelledSessions;
        // Financial / ROI placeholders – can be wired once cost data exists.
        private Double programCostPerformanceIndex;
        private Double trainingRoiIndicator;
        private Double operationalEfficiencyScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramPerformanceRow {
        private Long programId;
        private String programTitle;
        private String departmentName;
        private String status;              // ONGOING, COMPLETED, etc.
        private long sessionsPlanned;
        private long sessionsCompleted;
        private long totalEnrollments;
        private double completionRate;      // %
        private double attendanceRate;      // %
        private Double averageSessionDurationHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        /** Label such as 2025-01, 2025-Q1, etc. */
        private String period;
        private double attendanceRate;      // %
        private double completionRate;      // %
        private double engagementScore;     // %
        private Double costEfficiencyIndex; // nullable
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryValue {
        private String category;        // e.g. Department name, Material type
        private double value;           // primary metric (rate, count, score)
        private Double secondaryValue;  // optional secondary metric (e.g. cost, impact)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorMetric {
        private Long instructorId;
        private String instructorName;
        private long sessionsConducted;
        private double reliabilityRate;      // % completed vs cancelled/postponed
        private double engagementScore;      // synthetic index from attendance
        private Double coordinatorExecutionRate; // optional coordinator-related metric
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessEfficiency {
        private Double avgDaysProgramToFirstSession;
        private Double avgDaysProgramToCompletion;
        private double schedulingEfficiencyScore;  // %
        private double completionTimelineCompliance; // %
        private double processWasteIndex;          // 0–100 where higher = worse
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {
        private String type;      // PROGRAM_UNDERPERFORMING, LOW_ATTENDANCE, HIGH_COST_LOW_IMPACT, etc.
        private String label;     // Human-readable label (program name, department, etc.)
        private String severity;  // GREEN, YELLOW, RED
        private String summary;   // Short description of the issue
    }
}

