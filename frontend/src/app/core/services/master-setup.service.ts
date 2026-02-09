import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TrainingTopic,
  TrainingName,
  TrainingModule,
  TrainingCategory,
  Department,
  Coordinator,
  StudentTeacher,
  CreateTrainingTopicRequest,
  CreateTrainingNameRequest,
  CreateTrainingModuleRequest,
  CreateTrainingCategoryRequest,
  CreateDepartmentRequest,
  CreateCoordinatorRequest,
  CreateStudentTeacherRequest
} from '../models/master';

@Injectable({
  providedIn: 'root'
})
export class MasterSetupService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/master`;

  // Training Topics
  createTrainingTopic(request: CreateTrainingTopicRequest): Observable<TrainingTopic> {
    return this.http.post<TrainingTopic>(`${this.baseUrl}/training-topics`, request);
  }

  getAllTrainingTopics(activeOnly: boolean = true): Observable<TrainingTopic[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<TrainingTopic[]>(`${this.baseUrl}/training-topics`, { params });
  }

  updateTrainingTopic(id: number, request: CreateTrainingTopicRequest): Observable<TrainingTopic> {
    return this.http.put<TrainingTopic>(`${this.baseUrl}/training-topics/${id}`, request);
  }

  deleteTrainingTopic(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/training-topics/${id}`);
  }

  // Training Names
  createTrainingName(request: CreateTrainingNameRequest): Observable<TrainingName> {
    return this.http.post<TrainingName>(`${this.baseUrl}/training-names`, request);
  }

  getAllTrainingNames(activeOnly: boolean = true): Observable<TrainingName[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<TrainingName[]>(`${this.baseUrl}/training-names`, { params });
  }

  updateTrainingName(id: number, request: CreateTrainingNameRequest): Observable<TrainingName> {
    return this.http.put<TrainingName>(`${this.baseUrl}/training-names/${id}`, request);
  }

  deleteTrainingName(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/training-names/${id}`);
  }

  // Training Modules
  createTrainingModule(request: CreateTrainingModuleRequest): Observable<TrainingModule> {
    return this.http.post<TrainingModule>(`${this.baseUrl}/training-modules`, request);
  }

  getAllTrainingModules(activeOnly: boolean = true): Observable<TrainingModule[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<TrainingModule[]>(`${this.baseUrl}/training-modules`, { params });
  }

  updateTrainingModule(id: number, request: CreateTrainingModuleRequest): Observable<TrainingModule> {
    return this.http.put<TrainingModule>(`${this.baseUrl}/training-modules/${id}`, request);
  }

  deleteTrainingModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/training-modules/${id}`);
  }

  // Training Categories
  createTrainingCategory(request: CreateTrainingCategoryRequest): Observable<TrainingCategory> {
    return this.http.post<TrainingCategory>(`${this.baseUrl}/training-categories`, request);
  }

  getAllTrainingCategories(activeOnly: boolean = true): Observable<TrainingCategory[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<TrainingCategory[]>(`${this.baseUrl}/training-categories`, { params });
  }

  updateTrainingCategory(id: number, request: CreateTrainingCategoryRequest): Observable<TrainingCategory> {
    return this.http.put<TrainingCategory>(`${this.baseUrl}/training-categories/${id}`, request);
  }

  deleteTrainingCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/training-categories/${id}`);
  }

  // Departments
  createDepartment(request: CreateDepartmentRequest): Observable<Department> {
    return this.http.post<Department>(`${this.baseUrl}/departments`, request);
  }

  getAllDepartments(activeOnly: boolean = true): Observable<Department[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<Department[]>(`${this.baseUrl}/departments`, { params });
  }

  updateDepartment(id: number, request: CreateDepartmentRequest): Observable<Department> {
    return this.http.put<Department>(`${this.baseUrl}/departments/${id}`, request);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/departments/${id}`);
  }

  // Coordinators
  createCoordinator(request: CreateCoordinatorRequest): Observable<Coordinator> {
    return this.http.post<Coordinator>(`${this.baseUrl}/coordinators`, request);
  }

  getAllCoordinators(activeOnly: boolean = true): Observable<Coordinator[]> {
    const params = new HttpParams().set('activeOnly', activeOnly.toString());
    return this.http.get<Coordinator[]>(`${this.baseUrl}/coordinators`, { params });
  }

  updateCoordinator(id: number, request: CreateCoordinatorRequest): Observable<Coordinator> {
    return this.http.put<Coordinator>(`${this.baseUrl}/coordinators/${id}`, request);
  }

  deleteCoordinator(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/coordinators/${id}`);
  }

  // Student/Teacher
  createStudentTeacher(request: CreateStudentTeacherRequest): Observable<StudentTeacher> {
    return this.http.post<StudentTeacher>(`${this.baseUrl}/student-teachers`, request);
  }

  getAllStudentTeachers(activeOnly: boolean = true, type?: string): Observable<StudentTeacher[]> {
    let params = new HttpParams().set('activeOnly', activeOnly.toString());
    if (type) params = params.set('type', type);
    return this.http.get<StudentTeacher[]>(`${this.baseUrl}/student-teachers`, { params });
  }

  updateStudentTeacher(id: number, request: CreateStudentTeacherRequest): Observable<StudentTeacher> {
    return this.http.put<StudentTeacher>(`${this.baseUrl}/student-teachers/${id}`, request);
  }

  deleteStudentTeacher(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/student-teachers/${id}`);
  }
}
