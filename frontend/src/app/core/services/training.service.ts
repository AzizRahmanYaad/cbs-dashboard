import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  TrainingProgram, 
  CreateTrainingProgramRequest,
  TrainingSession,
  CreateTrainingSessionRequest,
  TrainingMaterial,
  CreateTrainingMaterialRequest,
  Enrollment,
  Attendance,
  MarkAttendanceRequest
} from '../models/training';

@Injectable({
  providedIn: 'root'
})
export class TrainingService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/training`;

  // Training Program APIs
  createProgram(request: CreateTrainingProgramRequest): Observable<TrainingProgram> {
    return this.http.post<TrainingProgram>(`${this.baseUrl}/programs`, request);
  }

  getAllPrograms(status?: string, category?: string): Observable<TrainingProgram[]> {
    let params = new HttpParams();
    // Only add parameters if they have valid values (not null, undefined, or empty string)
    if (status && status.trim() !== '') {
      params = params.set('status', status.trim());
    }
    if (category && category.trim() !== '') {
      params = params.set('category', category.trim());
    }
    
    console.log('Calling getAllPrograms with:', { status, category, url: `${this.baseUrl}/programs` });
    
    return this.http.get<TrainingProgram[]>(`${this.baseUrl}/programs`, { 
      params,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
  }

  getProgramById(id: number): Observable<TrainingProgram> {
    return this.http.get<TrainingProgram>(`${this.baseUrl}/programs/${id}`);
  }

  updateProgram(id: number, request: CreateTrainingProgramRequest): Observable<TrainingProgram> {
    return this.http.put<TrainingProgram>(`${this.baseUrl}/programs/${id}`, request);
  }

  deleteProgram(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/programs/${id}`);
  }

  assignTeacher(request: { programId: number; teacherId: number }): Observable<TrainingProgram> {
    return this.http.post<TrainingProgram>(`${this.baseUrl}/programs/assignments/teacher`, request);
  }

  assignStudents(request: { programId: number; studentIds: number[] }): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/programs/assignments/students`, request);
  }

  removeStudentFromProgram(programId: number, studentId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/programs/${programId}/students/${studentId}`);
  }

  getProgramsByInstructor(instructorId: number): Observable<TrainingProgram[]> {
    return this.http.get<TrainingProgram[]>(`${this.baseUrl}/programs/instructor/${instructorId}`);
  }

  getProgramStudents(programId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/programs/${programId}/students`);
  }

  getProgramsByStudent(studentId: number): Observable<TrainingProgram[]> {
    return this.http.get<TrainingProgram[]>(`${this.baseUrl}/programs/student/${studentId}`);
  }

  // Training Session APIs
  createSession(request: CreateTrainingSessionRequest): Observable<TrainingSession> {
    return this.http.post<TrainingSession>(`${this.baseUrl}/sessions`, request);
  }

  getAllSessions(programId?: number): Observable<TrainingSession[]> {
    let params = new HttpParams();
    if (programId) params = params.set('programId', programId.toString());
    return this.http.get<TrainingSession[]>(`${this.baseUrl}/sessions`, { params });
  }

  getSessionById(id: number): Observable<TrainingSession> {
    return this.http.get<TrainingSession>(`${this.baseUrl}/sessions/${id}`);
  }

  updateSession(id: number, request: CreateTrainingSessionRequest): Observable<TrainingSession> {
    return this.http.put<TrainingSession>(`${this.baseUrl}/sessions/${id}`, request);
  }

  deleteSession(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/sessions/${id}`);
  }

  // Training Material APIs
  createMaterial(request: CreateTrainingMaterialRequest): Observable<TrainingMaterial> {
    return this.http.post<TrainingMaterial>(`${this.baseUrl}/materials`, request);
  }

  getMaterialsByProgram(programId: number): Observable<TrainingMaterial[]> {
    return this.http.get<TrainingMaterial[]>(`${this.baseUrl}/materials?programId=${programId}`);
  }

  getMaterialById(id: number): Observable<TrainingMaterial> {
    return this.http.get<TrainingMaterial>(`${this.baseUrl}/materials/${id}`);
  }

  updateMaterial(id: number, request: CreateTrainingMaterialRequest): Observable<TrainingMaterial> {
    return this.http.put<TrainingMaterial>(`${this.baseUrl}/materials/${id}`, request);
  }

  deleteMaterial(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/materials/${id}`);
  }

  // Attendance APIs
  markAttendance(request: MarkAttendanceRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/attendance/mark`, request);
  }

  getAttendanceBySession(sessionId: number): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.baseUrl}/attendance/session/${sessionId}`);
  }

  getAttendanceByParticipant(participantId: number): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.baseUrl}/attendance/participant/${participantId}`);
  }
}
