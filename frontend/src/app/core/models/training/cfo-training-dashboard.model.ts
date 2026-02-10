export interface CfoTrainingDashboard {
  summary: CfoSummaryKpis;
  programPerformance: ProgramPerformanceRow[];
  performanceTrend: TimeSeriesPoint[];
  departmentAttendance: CategoryValue[];
  instructorProductivity: InstructorMetric[];
  materialUsage: CategoryValue[];
  processEfficiency: ProcessEfficiency;
  risks: RiskItem[];
}

export interface CfoSummaryKpis {
  totalPrograms: number;
  activePrograms: number;
  completedPrograms: number;
  totalSessions: number;
  sessionCompletionRate: number;
  studentAttendanceRate: number;
  teacherParticipationRate: number;
  materialEngagementScore: number;
  trainingUtilizationRate: number;
  averageCompletionTimeDays?: number | null;
  underperformingPrograms?: number | null;
  delayedOrCancelledSessions?: number | null;
  programCostPerformanceIndex?: number | null;
  trainingRoiIndicator?: number | null;
  operationalEfficiencyScore: number;
}

export interface ProgramPerformanceRow {
  programId: number;
  programTitle: string;
  departmentName?: string | null;
  status?: string | null;
  sessionsPlanned: number;
  sessionsCompleted: number;
  totalEnrollments: number;
  completionRate: number;
  attendanceRate: number;
  averageSessionDurationHours?: number | null;
}

export interface TimeSeriesPoint {
  period: string;              // e.g. '2025-01'
  attendanceRate: number;
  completionRate: number;
  engagementScore: number;
  costEfficiencyIndex?: number | null;
}

export interface CategoryValue {
  category: string;
  value: number;
  secondaryValue?: number | null;
}

export interface InstructorMetric {
  instructorId: number;
  instructorName: string;
  sessionsConducted: number;
  reliabilityRate: number;
  engagementScore: number;
  coordinatorExecutionRate?: number | null;
}

export interface ProcessEfficiency {
  avgDaysProgramToFirstSession?: number | null;
  avgDaysProgramToCompletion?: number | null;
  schedulingEfficiencyScore: number;
  completionTimelineCompliance: number;
  processWasteIndex: number;
}

export interface RiskItem {
  type: string;      // PROGRAM_UNDERPERFORMING, DEPARTMENT_LOW_ATTENDANCE, etc.
  label: string;     // Program / department name
  severity: string;  // GREEN | YELLOW | RED
  summary: string;
}

