export enum ReportStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  RETURNED_FOR_CORRECTION = 'RETURNED_FOR_CORRECTION'
}

export interface ChatCommunication {
  id?: number;
  platform: string;
  summary: string;
  actionTaken?: string;
  actionPerformed?: string;
  referenceNumber?: string;
}

export interface EmailCommunication {
  id?: number;
  isInternal: boolean;
  sender: string;
  receiver: string;
  subject: string;
  summary: string;
  actionTaken?: string;
  followUpRequired: boolean;
}

export interface ProblemEscalation {
  id?: number;
  escalatedTo: string;
  reason: string;
  escalationDateTime: string;
  followUpStatus?: string;
  comments?: string;
}

export interface TrainingCapacityBuilding {
  id?: number;
  trainingType: string;
  topic: string;
  duration?: string;
  skillsGained?: string;
  trainerName?: string;
  participants?: string;
}

export interface ProjectProgressUpdate {
  id?: number;
  projectName: string;
  taskOrMilestone?: string;
  progressDetail: string;
  roadblocksIssues?: string;
  estimatedCompletionDate?: string;
  comments?: string;
}

export interface CbsTeamActivity {
  id?: number;
  description: string;
  branch?: string;
  accountNumber?: string;
  actionTaken?: string;
  finalStatus?: string;
  activityType?: string;
}

export interface PendingActivity {
  id?: number;
  title: string;
  description: string;
  status: string;
  amount?: number;
  followUpRequired: boolean;
  responsiblePerson?: string;
}

export interface Meeting {
  id?: number;
  meetingType: string;
  topic: string;
  summary: string;
  actionTaken?: string;
  nextStep?: string;
  participants?: string;
}

export interface AfpayCardRequest {
  id?: number;
  requestType: string;
  requestedBy: string;
  requestDate: string;
  resolutionDetails?: string;
  supportingDocumentPath?: string;
  archivedDate?: string;
  operator?: string;
}

export interface QrmisIssue {
  id?: number;
  problemType: string;
  problemDescription: string;
  solutionProvided?: string;
  postedBy?: string;
  authorizedBy?: string;
  supportingDocumentsArchived?: string;
  operator?: string;
}

export interface DailyReport {
  id?: number;
  businessDate: string;
  employeeId?: number;
  employeeUsername?: string;
  employeeEmail?: string;
  cbsEndTime?: string;
  cbsStartTimeNextDay?: string;
  status: ReportStatus;
  reviewedById?: number;
  reviewedByUsername?: string;
  reviewedAt?: string;
  reviewComments?: string;
  reportingLine?: string;
  chatCommunications: ChatCommunication[];
  emailCommunications: EmailCommunication[];
  problemEscalations: ProblemEscalation[];
  trainingCapacityBuildings: TrainingCapacityBuilding[];
  projectProgressUpdates: ProjectProgressUpdate[];
  cbsTeamActivities: CbsTeamActivity[];
  pendingActivities: PendingActivity[];
  meetings: Meeting[];
  afpayCardRequests: AfpayCardRequest[];
  qrmisIssues: QrmisIssue[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateDailyReportRequest {
  businessDate?: string;
  cbsEndTime?: string;
  cbsStartTimeNextDay?: string;
  reportingLine?: string;
  chatCommunications: ChatCommunication[];
  emailCommunications: EmailCommunication[];
  problemEscalations: ProblemEscalation[];
  trainingCapacityBuildings: TrainingCapacityBuilding[];
  projectProgressUpdates: ProjectProgressUpdate[];
  cbsTeamActivities: CbsTeamActivity[];
  pendingActivities: PendingActivity[];
  meetings: Meeting[];
  afpayCardRequests: AfpayCardRequest[];
  qrmisIssues: QrmisIssue[];
}

export interface ReviewReportRequest {
  status: ReportStatus;
  reviewComments?: string;
}

export interface DailyReportDashboard {
  totalReports: number;
  pendingReports: number;
  approvedReports: number;
  rejectedReports: number;
  draftReports: number;
  totalEscalations: number;
  totalPendingActivities: number;
  totalQrmisIssues: number;
  reportsByStatus: Record<string, number>;
  reportsByEmployee: Record<string, number>;
  escalationsByType: Record<string, number>;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Unified Activity for simplified form
export interface UnifiedActivity {
  id?: number;
  activityType: string; // Selected from dropdown
  description: string; // Full description of the action
  branch?: string;
  accountNumber?: string;
}

// Activity type options for dropdown
export const ACTIVITY_TYPES = [
  'CBS Team Activity',
  'Chat Communication',
  'Email Communication',
  'Problem Escalation',
  'Training & Capacity Building',
  'Project Progress Update',
  'Pending Activity',
  'Meeting',
  'AFPay Card Request',
  'QRMIS Issue',
  'Allowing without check number',
  'Reversals',
  'System enhancements',
  'Email confirmations',
  'Ticket submissions',
  'Branch coordination',
  'Manual entry work',
  'Other'
] as const;

