export enum DefectSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum DefectStatus {
  NEW = 'NEW',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export interface Defect {
  id: number;
  title: string;
  description?: string;
  severity: DefectSeverity;
  status: DefectStatus;
  testCaseId?: number;
  testCaseTitle?: string;
  testExecutionId?: number;
  reportedById: number;
  reportedByUsername?: string;
  assignedToId?: number;
  assignedToUsername?: string;
  attachments: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateDefectRequest {
  title: string;
  description?: string;
  severity: DefectSeverity;
  testCaseId?: number;
  testExecutionId?: number;
  assignedToId?: number;
  attachments?: string[];
}

export interface UpdateDefectRequest {
  title?: string;
  description?: string;
  severity?: DefectSeverity;
  status?: DefectStatus;
  assignedToId?: number;
}

