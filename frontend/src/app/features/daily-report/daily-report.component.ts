import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { DailyReportService } from '../../core/services/daily-report.service';
import { DailyReportPermissionService } from '../../core/services/daily-report-permission.service';
import { AuthService } from '../../core/services/auth.service';
import {
  DailyReport,
  CreateDailyReportRequest,
  ReviewReportRequest,
  ReportStatus,
  ChatCommunication,
  EmailCommunication,
  ProblemEscalation,
  TrainingCapacityBuilding,
  ProjectProgressUpdate,
  CbsTeamActivity,
  PendingActivity,
  Meeting,
  AfpayCardRequest,
  QrmisIssue,
  UnifiedActivity,
  ACTIVITY_TYPES
} from '../../core/models/daily-report/daily-report.model';

@Component({
  selector: 'app-daily-report',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './daily-report.component.html',
  styleUrls: ['./daily-report.component.scss']
})
export class DailyReportComponent implements OnInit {
  private reportService = inject(DailyReportService);
  private permissionService = inject(DailyReportPermissionService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  activeTab: 'create' | 'my-reports' | 'dashboard' = 'create';
  
  reportForm!: FormGroup;
  currentReport: DailyReport | null = null;
  myReports: DailyReport[] = [];
  viewReportModal: DailyReport | null = null;
  showViewModal = false;
  
  loading = false;
  saving = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';
  
  today = new Date().toISOString().split('T')[0];
  selectedDate = this.today;
  selectedDay = '';
  
  // Activity types for dropdown
  activityTypes = ACTIVITY_TYPES;
  
  // Days of week
  daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  
  // Permission flags
  canCreate = false;
  canViewAll = false;
  canViewDashboard = false;
  canApprove = false;
  isController = false;
  isCFO = false;
  
  // Download state
  downloading = false;
  
  // Reports organized by date
  reportsByDate: Map<string, DailyReport[]> = new Map();
  selectedDateForDownload = '';

  ngOnInit() {
    this.checkPermissions();
    this.initializeForm();
    this.loadMyReports();
    if (this.canViewDashboard) {
      this.loadDashboard();
    }
  }

  checkPermissions() {
    this.canCreate = this.permissionService.canCreateReport();
    this.canViewAll = this.permissionService.canViewAllReports();
    this.canViewDashboard = this.permissionService.canViewDashboard();
    this.canApprove = this.permissionService.canApproveReports();
    this.isController = this.permissionService.isController();
    this.isCFO = this.permissionService.isCFO();
  }

  initializeForm() {
    const todayDate = new Date();
    const dayName = this.daysOfWeek[todayDate.getDay() === 0 ? 6 : todayDate.getDay() - 1];
    
    this.reportForm = this.fb.group({
      businessDate: [this.today, Validators.required],
      dayOfWeek: [dayName, Validators.required],
      cbsEndTime: ['', Validators.required],
      cbsStartTimeNextDay: ['', Validators.required],
      reportingLine: [''],
      unifiedActivities: this.fb.array([], Validators.required) // Simplified unified activities
    });
    
    this.selectedDay = dayName;
  }
  
  // Unified Activities
  get unifiedActivities(): FormArray {
    return this.reportForm.get('unifiedActivities') as FormArray;
  }
  
  addUnifiedActivity() {
    const group = this.fb.group({
      activityType: ['', Validators.required],
      description: ['', Validators.required],
      branch: [''],
      accountNumber: ['']
    });
    this.unifiedActivities.push(group);
  }
  
  removeUnifiedActivity(index: number) {
    this.unifiedActivities.removeAt(index);
  }
  
  onDateChange() {
    if (this.reportForm.get('businessDate')?.value) {
      const date = new Date(this.reportForm.get('businessDate')?.value);
      const dayName = this.daysOfWeek[date.getDay() === 0 ? 6 : date.getDay() - 1];
      this.selectedDay = dayName;
      this.reportForm.patchValue({ dayOfWeek: dayName });
      this.loadReportByDate();
    }
  }

  // Convert unified activities to backend format
  convertUnifiedActivitiesToBackendFormat(unifiedActivities: UnifiedActivity[]): CreateDailyReportRequest {
    const request: CreateDailyReportRequest = {
      businessDate: this.reportForm.get('businessDate')?.value,
      cbsEndTime: this.reportForm.get('cbsEndTime')?.value,
      cbsStartTimeNextDay: this.reportForm.get('cbsStartTimeNextDay')?.value,
      reportingLine: this.reportForm.get('reportingLine')?.value,
      chatCommunications: [],
      emailCommunications: [],
      problemEscalations: [],
      trainingCapacityBuildings: [],
      projectProgressUpdates: [],
      cbsTeamActivities: [],
      pendingActivities: [],
      meetings: [],
      afpayCardRequests: [],
      qrmisIssues: []
    };
    
    unifiedActivities.forEach(activity => {
      const desc = activity.description;
      const branch = activity.branch || '';
      const account = activity.accountNumber || '';
      
      switch(activity.activityType) {
        case 'CBS Team Activity':
        case 'Allowing without check number':
        case 'Reversals':
        case 'System enhancements':
        case 'Email confirmations':
        case 'Ticket submissions':
        case 'Branch coordination':
        case 'Manual entry work':
        case 'Other':
          request.cbsTeamActivities!.push({
            description: desc,
            activityType: activity.activityType,
            branch: branch,
            accountNumber: account
          });
          break;
        case 'Chat Communication':
          request.chatCommunications!.push({
            platform: 'General',
            summary: desc,
            actionTaken: ''
          });
          break;
        case 'Email Communication':
          request.emailCommunications!.push({
            isInternal: true,
            sender: '',
            receiver: '',
            subject: desc,
            summary: desc,
            followUpRequired: false
          });
          break;
        case 'Problem Escalation':
          request.problemEscalations!.push({
            escalatedTo: '',
            reason: desc,
            escalationDateTime: new Date().toISOString()
          });
          break;
        case 'Training & Capacity Building':
          request.trainingCapacityBuildings!.push({
            trainingType: 'Internal',
            topic: desc
          });
          break;
        case 'Project Progress Update':
          request.projectProgressUpdates!.push({
            projectName: '',
            progressDetail: desc
          });
          break;
        case 'Pending Activity':
          request.pendingActivities!.push({
            title: desc,
            description: desc,
            status: 'Pending',
            followUpRequired: false
          });
          break;
        case 'Meeting':
          request.meetings!.push({
            meetingType: 'Internal',
            topic: desc,
            summary: desc
          });
          break;
        case 'AFPay Card Request':
          request.afpayCardRequests!.push({
            requestType: 'Issue',
            requestedBy: '',
            requestDate: this.today
          });
          break;
        case 'QRMIS Issue':
          request.qrmisIssues!.push({
            problemType: '',
            problemDescription: desc
          });
          break;
      }
    });
    
    return request;
  }


  async loadReportByDate() {
    if (!this.selectedDate) return;
    
    this.loading = true;
    this.errorMessage = '';
    
    try {
      const report = await this.reportService.getReportByDate(this.selectedDate).toPromise();
      if (report) {
        this.currentReport = report;
        this.loadFormFromReport(report);
      } else {
        this.initializeForm();
        this.reportForm.patchValue({ businessDate: this.selectedDate });
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to load report';
      this.initializeForm();
      this.reportForm.patchValue({ businessDate: this.selectedDate });
    } finally {
      this.loading = false;
    }
  }

  loadFormFromReport(report: DailyReport) {
    const date = new Date(report.businessDate);
    const dayName = this.daysOfWeek[date.getDay() === 0 ? 6 : date.getDay() - 1];
    
    this.reportForm.patchValue({
      businessDate: report.businessDate,
      dayOfWeek: dayName,
      cbsEndTime: report.cbsEndTime,
      cbsStartTimeNextDay: report.cbsStartTimeNextDay,
      reportingLine: report.reportingLine
    });
    
    this.selectedDay = dayName;

    // Convert all activities to unified format
    this.unifiedActivities.clear();
    
    // Convert CBS Team Activities
    report.cbsTeamActivities.forEach(activity => {
      this.unifiedActivities.push(this.fb.group({
        activityType: [activity.activityType || 'CBS Team Activity', Validators.required],
        description: [activity.description, Validators.required],
        branch: [activity.branch || ''],
        accountNumber: [activity.accountNumber || '']
      }));
    });
    
    // Convert other activities
    report.chatCommunications.forEach(chat => {
      this.unifiedActivities.push(this.fb.group({
        activityType: ['Chat Communication', Validators.required],
        description: [chat.summary, Validators.required],
        branch: [''],
        accountNumber: ['']
      }));
    });
    
    report.emailCommunications.forEach(email => {
      this.unifiedActivities.push(this.fb.group({
        activityType: ['Email Communication', Validators.required],
        description: [email.summary, Validators.required],
        branch: [''],
        accountNumber: ['']
      }));
    });
    
    report.problemEscalations.forEach(escalation => {
      this.unifiedActivities.push(this.fb.group({
        activityType: ['Problem Escalation', Validators.required],
        description: [escalation.reason, Validators.required],
        branch: [''],
        accountNumber: ['']
      }));
    });
    
    report.pendingActivities.forEach(pending => {
      this.unifiedActivities.push(this.fb.group({
        activityType: ['Pending Activity', Validators.required],
        description: [pending.description, Validators.required],
        branch: [''],
        accountNumber: ['']
      }));
    });
    
    report.meetings.forEach(meeting => {
      this.unifiedActivities.push(this.fb.group({
        activityType: ['Meeting', Validators.required],
        description: [meeting.summary, Validators.required],
        branch: [''],
        accountNumber: ['']
      }));
    });
  }

  async saveDraft() {
    if (this.reportForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    if (this.unifiedActivities.length === 0) {
      this.errorMessage = 'Please add at least one activity';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const formValue = this.reportForm.value;
      const request = this.convertUnifiedActivitiesToBackendFormat(formValue.unifiedActivities);
      
      // Set basic fields
      request.businessDate = formValue.businessDate;
      request.cbsEndTime = formValue.cbsEndTime;
      request.cbsStartTimeNextDay = formValue.cbsStartTimeNextDay;
      request.reportingLine = formValue.reportingLine;

      if (this.currentReport?.id) {
        const updatedReport = await this.reportService.updateReport(this.currentReport.id, request).toPromise();
        this.currentReport = updatedReport!;
        this.successMessage = 'Report saved successfully';
        this.loadMyReports();
      } else {
        const report = await this.reportService.createReport(request).toPromise();
        this.currentReport = report!;
        this.successMessage = 'Report created successfully';
        this.loadMyReports();
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to save report';
    } finally {
      this.saving = false;
    }
  }

  async submitReport() {
    if (this.reportForm.invalid) {
      this.errorMessage = 'Please fill in all required fields before submitting';
      return;
    }

    if (this.unifiedActivities.length === 0) {
      this.errorMessage = 'Please add at least one activity before submitting';
      return;
    }

    // Save first, then submit
    await this.saveDraft();
    
    if (!this.currentReport?.id) {
      this.errorMessage = 'Failed to create report';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const report = await this.reportService.submitReport(this.currentReport.id).toPromise();
      this.currentReport = report!;
      this.successMessage = 'Report submitted successfully';
      this.loadMyReports();
      
      // Auto-reset form after successful submission
      setTimeout(() => {
        this.resetForm();
      }, 1000);
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to submit report';
    } finally {
      this.submitting = false;
    }
  }
  
  resetForm() {
    this.currentReport = null;
    this.selectedDate = this.today;
    const todayDate = new Date();
    this.selectedDay = this.daysOfWeek[todayDate.getDay() === 0 ? 6 : todayDate.getDay() - 1];
    this.initializeForm();
    this.successMessage = '';
    this.errorMessage = '';
  }
  
  async downloadMyReport(report: DailyReport) {
    if (!report.id || !report.employeeId) return;
    
    this.downloading = true;
    try {
      const blob = await this.reportService.downloadEmployeeReport(report.employeeId).toPromise();
      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const dateStr = report.businessDate.split('T')[0];
        link.download = `daily_report_${dateStr}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }
    } catch (error: any) {
      this.errorMessage = 'Failed to download report';
    } finally {
      this.downloading = false;
    }
  }

  async loadMyReports() {
    this.loading = true;
    this.errorMessage = '';
    this.myReports = []; // Clear existing reports
    
    try {
      console.log('Loading my reports...');
      const response = await this.reportService.getMyReports(0, 100).toPromise();
      console.log('Full API response:', JSON.stringify(response, null, 2));
      
      if (response) {
        if (response.content && Array.isArray(response.content)) {
          this.myReports = response.content;
          this.organizeReportsByDate(this.myReports);
          console.log('My reports loaded successfully. Count:', this.myReports.length);
          console.log('First report:', this.myReports[0]);
        } else if (Array.isArray(response)) {
          // Handle case where API returns array directly instead of PageResponse
          this.myReports = response;
          this.organizeReportsByDate(this.myReports);
          console.log('My reports loaded (array format). Count:', this.myReports.length);
        } else {
          console.warn('Unexpected response format:', response);
          this.myReports = [];
        }
      } else {
        console.warn('Empty response received');
        this.myReports = [];
      }
      
      if (this.myReports.length === 0) {
        console.log('No reports found for current user');
      }
    } catch (error: any) {
      console.error('Error loading reports:', error);
      console.error('Error details:', {
        status: error.status,
        statusText: error.statusText,
        message: error.message,
        error: error.error
      });
      this.errorMessage = error.error?.message || error.message || 'Failed to load reports';
      this.myReports = [];
    } finally {
      this.loading = false;
      console.log('Loading completed. Reports count:', this.myReports.length);
    }
  }

  async loadDashboard() {
    // Dashboard loading will be implemented in dashboard tab
  }

  getStatusColor(status: ReportStatus): string {
    const colors: Record<ReportStatus, string> = {
      [ReportStatus.DRAFT]: 'bg-gray-100 text-gray-800',
      [ReportStatus.SUBMITTED]: 'bg-blue-100 text-blue-800',
      [ReportStatus.APPROVED]: 'bg-green-100 text-green-800',
      [ReportStatus.REJECTED]: 'bg-red-100 text-red-800',
      [ReportStatus.RETURNED_FOR_CORRECTION]: 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  canEditReport(report: DailyReport): boolean {
    if (!report) return false;
    return this.permissionService.canEditReport(
      report.employeeId || 0,
      report.status === ReportStatus.APPROVED
    );
  }

  setActiveTab(tab: 'create' | 'my-reports' | 'dashboard') {
    this.activeTab = tab;
    if (tab === 'my-reports') {
      // Small delay to ensure tab is visible before loading
      setTimeout(() => {
        this.loadMyReports();
      }, 100);
    }
  }

  loadReportForEdit(report: DailyReport) {
    this.currentReport = report;
    this.selectedDate = report.businessDate;
    this.loadFormFromReport(report);
    this.setActiveTab('create');
  }

  async viewReport(report: DailyReport) {
    this.loading = true;
    try {
      const fullReport = await this.reportService.getReport(report.id!).toPromise();
      this.viewReportModal = fullReport!;
      this.showViewModal = true;
    } catch (error: any) {
      this.errorMessage = 'Failed to load report';
    } finally {
      this.loading = false;
    }
  }
  
  closeViewModal() {
    this.showViewModal = false;
    this.viewReportModal = null;
  }
  
  async downloadEmployeeReport(employeeId: number, employeeName: string) {
    this.downloading = true;
    try {
      const blob = await this.reportService.downloadEmployeeReport(employeeId).toPromise();
      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `daily_report_${employeeName}_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }
    } catch (error: any) {
      this.errorMessage = 'Failed to download report';
    } finally {
      this.downloading = false;
    }
  }
  
  async downloadCombinedReport(date?: string) {
    const downloadDate = date || this.selectedDateForDownload;
    
    if (!downloadDate) {
      this.errorMessage = 'Please select a date to download the combined report';
      return;
    }
    
    this.downloading = true;
    this.errorMessage = '';
    try {
      const blob = await this.reportService.downloadCombinedReport(downloadDate).toPromise();
      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        // Format date for filename (YYYY-MM-DD)
        const dateStr = downloadDate.split('T')[0]; // Remove time if present
        const filename = `CBS_Daily_Report_${dateStr}.pdf`;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.successMessage = `Report downloaded successfully for ${dateStr}`;
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to download combined report. Please ensure a date is selected and reports exist for that date.';
    } finally {
      this.downloading = false;
    }
  }
  
  async loadReportsByDate(date: string) {
    this.loading = true;
    try {
      const reports = await this.reportService.getReportsByDate(date).toPromise();
      if (reports) {
        this.organizeReportsByDate(reports);
      }
    } catch (error: any) {
      this.errorMessage = 'Failed to load reports by date';
    } finally {
      this.loading = false;
    }
  }
  
  organizeReportsByDate(reports: DailyReport[]) {
    this.reportsByDate.clear();
    reports.forEach(report => {
      const dateKey = report.businessDate;
      if (!this.reportsByDate.has(dateKey)) {
        this.reportsByDate.set(dateKey, []);
      }
      this.reportsByDate.get(dateKey)!.push(report);
    });
  }
  
  getDateKeys(): string[] {
    return Array.from(this.reportsByDate.keys()).sort().reverse();
  }
  
  getReportsForDate(date: string): DailyReport[] {
    return this.reportsByDate.get(date) || [];
  }
  
  async confirmReport(report: DailyReport) {
    if (!this.isCFO) return;
    
    this.loading = true;
    try {
      const reviewRequest = {
        status: ReportStatus.APPROVED,
        reviewComments: 'Confirmed by CFO'
      };
      const updatedReport = await this.reportService.reviewReport(report.id!, reviewRequest).toPromise();
      this.successMessage = 'Report confirmed successfully';
      if (this.activeTab === 'dashboard') {
        this.loadReportsByDate(this.selectedDateForDownload);
      } else {
        this.loadMyReports();
      }
    } catch (error: any) {
      this.errorMessage = 'Failed to confirm report';
    } finally {
      this.loading = false;
    }
  }
}
