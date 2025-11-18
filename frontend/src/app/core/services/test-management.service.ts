import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TestModule,
  CreateTestModuleRequest,
  TestCase,
  CreateTestCaseRequest,
  UpdateTestCaseRequest,
  TestExecution,
  CreateTestExecutionRequest,
  Defect,
  CreateDefectRequest,
  UpdateDefectRequest,
  Comment,
  CreateCommentRequest,
  TestReport,
  Priority,
  TestCaseStatus,
  ExecutionStatus,
  DefectStatus,
  DefectSeverity
} from '../models/test';

@Injectable({
  providedIn: 'root'
})
export class TestManagementService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/test`;

  // Test Module APIs
  createModule(request: CreateTestModuleRequest): Observable<TestModule> {
    return this.http.post<TestModule>(`${this.baseUrl}/modules`, request);
  }

  getAllModules(): Observable<TestModule[]> {
    return this.http.get<TestModule[]>(`${this.baseUrl}/modules`);
  }

  getModuleById(id: number): Observable<TestModule> {
    return this.http.get<TestModule>(`${this.baseUrl}/modules/${id}`);
  }

  updateModule(id: number, request: CreateTestModuleRequest): Observable<TestModule> {
    return this.http.put<TestModule>(`${this.baseUrl}/modules/${id}`, request);
  }

  deleteModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/modules/${id}`);
  }

  // Test Case APIs
  createTestCase(request: CreateTestCaseRequest): Observable<TestCase> {
    return this.http.post<TestCase>(`${this.baseUrl}/test-cases`, request);
  }

  getAllTestCases(filters?: {
    moduleId?: number;
    status?: TestCaseStatus;
    assignedToId?: number;
    priority?: Priority;
  }): Observable<TestCase[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.moduleId) params = params.set('moduleId', filters.moduleId);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.assignedToId) params = params.set('assignedToId', filters.assignedToId);
      if (filters.priority) params = params.set('priority', filters.priority);
    }
    return this.http.get<TestCase[]>(`${this.baseUrl}/test-cases`, { params });
  }

  searchTestCases(searchTerm: string): Observable<TestCase[]> {
    return this.http.get<TestCase[]>(`${this.baseUrl}/test-cases/search`, {
      params: new HttpParams().set('searchTerm', searchTerm)
    });
  }

  getTestCaseById(id: number): Observable<TestCase> {
    return this.http.get<TestCase>(`${this.baseUrl}/test-cases/${id}`);
  }

  updateTestCase(id: number, request: UpdateTestCaseRequest): Observable<TestCase> {
    return this.http.put<TestCase>(`${this.baseUrl}/test-cases/${id}`, request);
  }

  deleteTestCase(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/test-cases/${id}`);
  }

  // Test Execution APIs
  createExecution(request: CreateTestExecutionRequest): Observable<TestExecution> {
    return this.http.post<TestExecution>(`${this.baseUrl}/executions`, request);
  }

  getAllExecutions(filters?: {
    testCaseId?: number;
    userId?: number;
    status?: ExecutionStatus;
  }): Observable<TestExecution[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.testCaseId) params = params.set('testCaseId', filters.testCaseId);
      if (filters.userId) params = params.set('userId', filters.userId);
      if (filters.status) params = params.set('status', filters.status);
    }
    return this.http.get<TestExecution[]>(`${this.baseUrl}/executions`, { params });
  }

  getExecutionById(id: number): Observable<TestExecution> {
    return this.http.get<TestExecution>(`${this.baseUrl}/executions/${id}`);
  }

  // Defect APIs
  createDefect(request: CreateDefectRequest): Observable<Defect> {
    return this.http.post<Defect>(`${this.baseUrl}/defects`, request);
  }

  getAllDefects(filters?: {
    status?: DefectStatus;
    severity?: DefectSeverity;
    assignedToId?: number;
  }): Observable<Defect[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.status) params = params.set('status', filters.status);
      if (filters.severity) params = params.set('severity', filters.severity);
      if (filters.assignedToId) params = params.set('assignedToId', filters.assignedToId);
    }
    return this.http.get<Defect[]>(`${this.baseUrl}/defects`, { params });
  }

  getDefectById(id: number): Observable<Defect> {
    return this.http.get<Defect>(`${this.baseUrl}/defects/${id}`);
  }

  updateDefect(id: number, request: UpdateDefectRequest): Observable<Defect> {
    return this.http.put<Defect>(`${this.baseUrl}/defects/${id}`, request);
  }

  deleteDefect(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/defects/${id}`);
  }

  // Comment APIs
  createComment(request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.baseUrl}/comments`, request);
  }

  getCommentsByTestCase(testCaseId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.baseUrl}/comments/test-case/${testCaseId}`);
  }

  getCommentsByDefect(defectId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.baseUrl}/comments/defect/${defectId}`);
  }

  deleteComment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/comments/${id}`);
  }

  // Report APIs
  generateReport(moduleId?: number): Observable<TestReport> {
    let params = new HttpParams();
    if (moduleId) params = params.set('moduleId', moduleId);
    return this.http.get<TestReport>(`${this.baseUrl}/reports`, { params });
  }
}

