export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum TestCaseStatus {
  DRAFT = 'DRAFT',
  APPROVED = 'APPROVED',
  ARCHIVED = 'ARCHIVED'
}

export interface TestCase {
  id: number;
  title: string;
  preconditions?: string;
  steps: string[];
  expectedResult?: string;
  priority: Priority;
  status: TestCaseStatus;
  moduleId?: number;
  moduleName?: string;
  createdById: number;
  createdByUsername?: string;
  assignedToId?: number;
  assignedToUsername?: string;
  createdAt: string;
  updatedAt: string;
  executionCount?: number;
  defectCount?: number;
}

export interface CreateTestCaseRequest {
  title: string;
  preconditions?: string;
  steps: string[];
  expectedResult?: string;
  priority: Priority;
  moduleId?: number;
  assignedToId?: number;
}

export interface UpdateTestCaseRequest {
  title?: string;
  preconditions?: string;
  steps?: string[];
  expectedResult?: string;
  priority?: Priority;
  status?: TestCaseStatus;
  moduleId?: number;
  assignedToId?: number;
}

