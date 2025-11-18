export enum ExecutionStatus {
  PASSED = 'PASSED',
  FAILED = 'FAILED',
  BLOCKED = 'BLOCKED',
  RETEST = 'RETEST'
}

export interface TestExecution {
  id: number;
  testCaseId: number;
  testCaseTitle?: string;
  executedById: number;
  executedByUsername?: string;
  status: ExecutionStatus;
  comments?: string;
  attachments: string[];
  executedAt: string;
  updatedAt: string;
}

export interface CreateTestExecutionRequest {
  testCaseId: number;
  status: ExecutionStatus;
  comments?: string;
  attachments?: string[];
}

