import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DailyReport,
  CreateDailyReportRequest,
  ReviewReportRequest,
  DailyReportDashboard,
  PageResponse,
  ReportStatus
} from '../models/daily-report/daily-report.model';

@Injectable({
  providedIn: 'root'
})
export class DailyReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/daily-reports`;

  createReport(request: CreateDailyReportRequest): Observable<DailyReport> {
    return this.http.post<DailyReport>(this.baseUrl, request);
  }

  updateReport(id: number, request: CreateDailyReportRequest): Observable<DailyReport> {
    return this.http.put<DailyReport>(`${this.baseUrl}/${id}`, request);
  }

  submitReport(id: number): Observable<DailyReport> {
    return this.http.post<DailyReport>(`${this.baseUrl}/${id}/submit`, {});
  }

  reviewReport(id: number, request: ReviewReportRequest): Observable<DailyReport> {
    return this.http.post<DailyReport>(`${this.baseUrl}/${id}/review`, request);
  }

  getReport(id: number): Observable<DailyReport> {
    return this.http.get<DailyReport>(`${this.baseUrl}/${id}`);
  }

  getReportByDate(date: string): Observable<DailyReport> {
    return this.http.get<DailyReport>(`${this.baseUrl}/date/${date}`);
  }

  getMyReports(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'businessDate',
    sortDir: string = 'DESC'
  ): Observable<PageResponse<DailyReport>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PageResponse<DailyReport>>(`${this.baseUrl}/my-reports`, { params });
  }

  getAllReports(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'businessDate',
    sortDir: string = 'DESC',
    startDate?: string,
    endDate?: string,
    employeeId?: number,
    status?: ReportStatus
  ): Observable<PageResponse<DailyReport>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    if (employeeId) {
      params = params.set('employeeId', employeeId.toString());
    }
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PageResponse<DailyReport>>(this.baseUrl, { params });
  }

  getDashboard(): Observable<DailyReportDashboard> {
    return this.http.get<DailyReportDashboard>(`${this.baseUrl}/dashboard`);
  }

  deleteReport(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  downloadEmployeeReport(
    employeeId: number,
    startDate?: string,
    endDate?: string
  ): Observable<Blob> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get(`${this.baseUrl}/download/employee/${employeeId}`, {
      params,
      responseType: 'blob',
      headers: { 'Accept': 'application/pdf' }
    });
  }
  
  downloadMyReport(reportId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/download/my-report/${reportId}`, {
      responseType: 'blob',
      headers: { 'Accept': 'application/pdf' }
    });
  }

  downloadCombinedReport(date: string, cbsEndTime?: string, cbsStartTimeNextDay?: string): Observable<Blob> {
    if (!date) {
      throw new Error('Date is required for combined report download');
    }
    
    let params = new HttpParams();
    params = params.set('date', date);
    
    if (cbsEndTime) {
      params = params.set('cbsEndTime', cbsEndTime);
    }
    if (cbsStartTimeNextDay) {
      params = params.set('cbsStartTimeNextDay', cbsStartTimeNextDay);
    }

    return this.http.get(`${this.baseUrl}/download/combined`, {
      params,
      responseType: 'blob',
      headers: { 'Accept': 'application/pdf' }
    });
  }

  getReportsByDate(date: string): Observable<DailyReport[]> {
    return this.http.get<DailyReport[]>(`${this.baseUrl}/by-date/${date}`);
  }
}

