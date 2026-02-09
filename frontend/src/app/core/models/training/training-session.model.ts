export interface TrainingSession {
  id: number;
  programId: number;
  programTitle?: string;
  topicName?: string;
  startDateTime: string;
  endDateTime: string;
  location?: string;
  sessionType?: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED';
  maxCapacity?: number;
  notes?: string;
  instructorId?: number;
  instructorUsername?: string;
  instructorFullName?: string;
  createdById?: number;
  createdByUsername?: string;
  createdAt?: string;
  updatedAt?: string;
  enrollmentsCount?: number;
  sequenceOrder?: number;
}

export interface CreateTrainingSessionRequest {
  programId: number;
  startDateTime: string;
  endDateTime: string;
  location?: string;
  sessionType?: string;
  status?: string;
  maxCapacity?: number;
  notes?: string;
  topic?: string;
  instructorId?: number;
}
