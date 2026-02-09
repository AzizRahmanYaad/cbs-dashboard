export interface TrainingProgram {
  id: number;
  title: string;
  description?: string;
  
  // New fields
  trainingTopicId?: number;
  trainingTopicName?: string;
  trainingNameId?: number;
  trainingNameName?: string;
  trainingName?: string;
  trainingDate?: string; // ISO date string
  trainingLevel?: string; // BASIC, INTERMEDIATE, ADVANCED
  trainingCategoryId?: number;
  trainingCategoryName?: string;
  departmentId?: number;
  departmentName?: string;
  trainingModuleId?: number;
  trainingModuleName?: string;
  facultyName?: string;
  coordinatorId?: number;
  coordinatorName?: string;
  trainingType?: string; // ON_SITE, ONLINE, ON_JOB
  examType?: string; // PRE_TRAINING_EXAM, POST_TRAINING_EXAM
  hasArticleMaterial?: boolean;
  hasVideoMaterial?: boolean;
  hasSlideMaterial?: boolean;
  thumbnailImagePath?: string;
  
  // Legacy fields
  category?: string;
  durationHours?: number;
  status: string;
  maxParticipants?: number;
  prerequisites?: string;
  learningObjectives?: string;
  createdById: number;
  createdByUsername?: string;
  instructorId?: number;
  instructorUsername?: string;
  createdAt: string;
  updatedAt: string;
  sessionsCount?: number;
  enrollmentsCount?: number;
}

export interface CreateTrainingProgramRequest {
  title: string;
  description?: string;
  
  // New fields
  trainingTopicId?: number;
  trainingNameId?: number;
  trainingName?: string;
  trainingDate?: string; // ISO date string
  trainingLevel?: string; // BASIC, INTERMEDIATE, ADVANCED
  trainingCategoryId?: number;
  departmentId?: number;
  trainingModuleId?: number;
  facultyName?: string;
  coordinatorId?: number;
  trainingType?: string; // ON_SITE, ONLINE, ON_JOB
  examType?: string; // PRE_TRAINING_EXAM, POST_TRAINING_EXAM
  hasArticleMaterial?: boolean;
  hasVideoMaterial?: boolean;
  hasSlideMaterial?: boolean;
  thumbnailImagePath?: string;
  
  // Legacy fields
  category?: string;
  durationHours?: number;
  status?: string;
  maxParticipants?: number;
  prerequisites?: string;
  learningObjectives?: string;
  instructorId?: number;
}

export enum TrainingLevel {
  BASIC = 'BASIC',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum TrainingType {
  ON_SITE = 'ON_SITE',
  ONLINE = 'ONLINE',
  ON_JOB = 'ON_JOB'
}

export enum ExamType {
  PRE_TRAINING_EXAM = 'PRE_TRAINING_EXAM',
  POST_TRAINING_EXAM = 'POST_TRAINING_EXAM'
}

export enum TrainingStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ONGOING = 'ONGOING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  ARCHIVED = 'ARCHIVED'
}
