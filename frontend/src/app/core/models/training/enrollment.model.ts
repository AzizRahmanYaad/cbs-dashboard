export interface Enrollment {
  id: number;
  programId: number;
  programTitle?: string;
  sessionId?: number;
  sessionStartDateTime?: string; // ISO datetime string
  participantId: number;
  participantUsername?: string;
  participantFullName?: string;
  participantEmail?: string;
  status: string; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, FAILED, WITHDRAWN
  enrollmentDate?: string; // ISO datetime string
  completionDate?: string; // ISO datetime string
  attendancePercentage?: number;
  finalScore?: number;
  notes?: string;
  enrolledById?: number;
  enrolledByUsername?: string;
  createdAt: string;
  updatedAt: string;
}
