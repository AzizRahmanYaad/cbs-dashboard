export interface TrainingTopic {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

export interface TrainingName {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

export interface TrainingModule {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

export interface TrainingCategory {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

export interface Coordinator {
  id: number;
  userId: number;
  username: string;
  fullName?: string;
  email?: string;
  employeeId?: string;
  department?: string;
  phone?: string;
  isActive: boolean;
}

export interface CreateTrainingTopicRequest {
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateTrainingNameRequest {
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateTrainingModuleRequest {
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateTrainingCategoryRequest {
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateDepartmentRequest {
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CreateCoordinatorRequest {
  userId: number;
  employeeId?: string;
  department?: string;
  phone?: string;
  isActive?: boolean;
}

export interface StudentTeacher {
  id: number;
  userId: number;
  username: string;
  fullName?: string;
  email?: string;
  type: 'STUDENT' | 'TEACHER';
  employeeId?: string;
  studentId?: string;
  department?: string;
  phone?: string;
  qualification?: string;
  specialization?: string;
  isActive: boolean;
}

export interface CreateStudentTeacherRequest {
  userId: number;
  type: 'STUDENT' | 'TEACHER';
  employeeId?: string;
  studentId?: string;
  department?: string;
  phone?: string;
  qualification?: string;
  specialization?: string;
  isActive?: boolean;
}

// Export DTOs for compatibility
export type CoordinatorDto = Coordinator;
export type StudentTeacherDto = StudentTeacher;
