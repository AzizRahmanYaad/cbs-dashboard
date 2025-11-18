export interface TestReport {
  totalTestCases: number;
  totalExecutions: number;
  passedCount: number;
  failedCount: number;
  blockedCount: number;
  retestCount: number;
  totalDefects: number;
  statusDistribution: { [key: string]: number };
  priorityDistribution: { [key: string]: number };
  moduleDistribution: { [key: string]: number };
  defectStatusDistribution: { [key: string]: number };
  defectSeverityDistribution: { [key: string]: number };
}

