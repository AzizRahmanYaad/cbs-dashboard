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
    if (this.isController) {
      this.initializeCombinedReportForm();
    }
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
      unifiedActivities: this.fb.array([]) // Will be populated from tempActivities
    });
    
    this.selectedDay = dayName;
    this.tempActivities = [];
    this.newActivityType = '';
    this.newActivityDescription = '';
    this.newActivityBranch = '';
    this.editingActivityIndex = null;
    this.editingActivityType = '';
    this.editingActivityBranch = '';
  }
  
  // Controller combined report form
  combinedReportForm!: FormGroup;
  cbsEndTime = '';
  cbsStartTimeNextDay = '';
  
  initializeCombinedReportForm() {
    this.combinedReportForm = this.fb.group({
      date: [this.selectedDateForDownload || this.today, Validators.required],
      cbsEndTime: ['', Validators.required],
      cbsStartTimeNextDay: ['', Validators.required]
    });
  }
  
  // Simplified Activity Input
  newActivityType = '';
  newActivityDescription = '';
  newActivityBranch = '';
  tempActivities: { id: number; activityType: string; description: string; branch?: string }[] = [];
  editingActivityIndex: number | null = null;
  editingActivityType = '';
  editingActivityText = '';
  editingActivityBranch = '';
  private activityIdCounter = 1;
  
  // Branch List organized by zones
  branchZones: { zone: string; branches: string[] }[] = [
    {
      zone: 'Center Zone Department',
      branches: [
        'Logar Provincial Branch',
        'Panjshir Provincial Branch',
        'Bamyan Provincial Branch',
        'Kapisa Provincial Branch',
        'Parwan Provincial Branch',
        'Wardak / Maidan Shahr Branch',
        'First City Branch',
        'Second City Branch',
        'Third City Branch',
        'Fourth City Branch',
        'Fifth City Branch',
        'Sixth City Branch',
        'Seventh City Branch',
        'Teller Counter – Kabul International Airport'
      ]
    },
    {
      zone: 'West Zone Department',
      branches: [
        'Herat Provincial Branch',
        'Nimruz Provincial Branch',
        'Farah Provincial Branch',
        'Badghis Provincial Branch',
        'Ghor Provincial Branch',
        'Islam Qala Branch',
        'Torghonde Branch',
        'Herat City Branch',
        'Nimruz Teller Counter'
      ]
    },
    {
      zone: 'South West Zone Department',
      branches: [
        'Kandahar Provincial Branch',
        'Uruzgan Provincial Branch',
        'Helmand Provincial Branch',
        'Zabul Provincial Branch',
        'Daikundi Provincial Branch',
        'Speen Boldak Branch',
        'Kandahar City Branch'
      ]
    },
    {
      zone: 'East Zone Department',
      branches: [
        'Jalalabad Provincial Branch',
        'Laghman Provincial Branch',
        'Kunar Provincial Branch',
        'Nooristan Provincial Branch',
        'Torkham Branch',
        'Jalalabad City Branch'
      ]
    },
    {
      zone: 'South East Zone Department',
      branches: [
        'Gardiz Provincial Branch',
        'Khost Provincial Branch',
        'Ghazni Provincial Branch',
        'Paktika Provincial Branch',
        'Dand Patan (Paktia) Branch',
        'Gholam Khan (Khost) Branch',
        'Argon (Paktika) Branch'
      ]
    },
    {
      zone: 'North East Zone Department',
      branches: [
        'Kunduz Provincial Branch',
        'Pole Khumri Provincial Branch',
        'Badakhshan Provincial Branch',
        'Takhar Provincial Branch',
        'Sher Khan Port Branch'
      ]
    },
    {
      zone: 'North Zone Department',
      branches: [
        'Mazar-e-Sharif Provincial Branch',
        'Maimana Provincial Branch',
        'Sheberghan Provincial Branch',
        'Sar-e-Pul Provincial Branch',
        'Samangan Provincial Branch',
        'Hayratan Branch',
        'Aqeena Branch'
      ]
    }
  ];
  
  // Unified Activities (for backend conversion)
  get unifiedActivities(): FormArray {
    return this.reportForm.get('unifiedActivities') as FormArray;
  }
  
  addActivity() {
    const activityType = this.newActivityType.trim();
    const description = this.newActivityDescription.trim();
    
    if (!activityType) {
      this.errorMessage = 'Please select an activity type';
      return;
    }
    
    if (!description) {
      this.errorMessage = 'Please enter an activity description';
      return;
    }
    
    if (this.editingActivityIndex !== null) {
      // Update existing activity
      this.tempActivities[this.editingActivityIndex].activityType = activityType;
      this.tempActivities[this.editingActivityIndex].description = description;
      this.tempActivities[this.editingActivityIndex].branch = this.newActivityBranch || undefined;
      this.editingActivityIndex = null;
      this.editingActivityType = '';
      this.editingActivityText = '';
      this.editingActivityBranch = '';
    } else {
      // Add new activity
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: activityType,
        description: description,
        branch: this.newActivityBranch || undefined
      });
    }
    
    this.newActivityType = '';
    this.newActivityDescription = '';
    this.newActivityBranch = '';
    this.errorMessage = '';
  }
  
  editActivity(index: number) {
    this.editingActivityIndex = index;
    this.editingActivityType = this.tempActivities[index].activityType;
    this.editingActivityText = this.tempActivities[index].description;
    this.editingActivityBranch = this.tempActivities[index].branch || '';
    this.newActivityType = this.tempActivities[index].activityType;
    this.newActivityDescription = this.tempActivities[index].description;
    this.newActivityBranch = this.tempActivities[index].branch || '';
  }
  
  cancelEdit() {
    this.editingActivityIndex = null;
    this.editingActivityType = '';
    this.editingActivityText = '';
    this.editingActivityBranch = '';
    this.newActivityType = '';
    this.newActivityDescription = '';
    this.newActivityBranch = '';
  }
  
  removeActivity(index: number) {
    this.tempActivities.splice(index, 1);
    if (this.editingActivityIndex === index) {
      this.cancelEdit();
    } else if (this.editingActivityIndex !== null && this.editingActivityIndex > index) {
      this.editingActivityIndex--;
    }
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

  // Convert temp activities to backend format
  convertTempActivitiesToBackendFormat(): CreateDailyReportRequest {
    const request: CreateDailyReportRequest = {
      businessDate: this.reportForm.get('businessDate')?.value,
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
    
    // Convert activities based on their type
    this.tempActivities.forEach(activity => {
      const desc = activity.description;
      const branch = activity.branch || '';
      
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
            branch: branch
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
      dayOfWeek: dayName
    });
    
    this.selectedDay = dayName;

    // Convert all activities to temp activities format
    this.tempActivities = [];
    this.activityIdCounter = 1;
    
    // Convert CBS Team Activities
    report.cbsTeamActivities.forEach(activity => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: activity.activityType || 'CBS Team Activity',
        description: activity.description,
        branch: activity.branch
      });
    });
    
    // Convert other activities
    report.chatCommunications.forEach(chat => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: 'Chat Communication',
        description: chat.summary
      });
    });
    
    report.emailCommunications.forEach(email => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: 'Email Communication',
        description: email.summary
      });
    });
    
    report.problemEscalations.forEach(escalation => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: 'Problem Escalation',
        description: escalation.reason
      });
    });
    
    report.pendingActivities.forEach(pending => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: 'Pending Activity',
        description: pending.description
      });
    });
    
    report.meetings.forEach(meeting => {
      this.tempActivities.push({
        id: this.activityIdCounter++,
        activityType: 'Meeting',
        description: meeting.summary
      });
    });
  }

  async saveDraft() {
    if (this.reportForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    if (this.tempActivities.length === 0) {
      this.errorMessage = 'Please add at least one activity';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const request = this.convertTempActivitiesToBackendFormat();

      if (this.currentReport?.id) {
        const updatedReport = await this.reportService.updateReport(this.currentReport.id, request).toPromise();
        this.currentReport = updatedReport!;
        this.successMessage = 'Report saved successfully';
        this.loadMyReports();
      } else {
        const report = await this.reportService.createReport(request).toPromise();
        this.currentReport = report!;
        this.successMessage = 'Report saved successfully';
        this.loadMyReports();
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to save report';
    } finally {
      this.saving = false;
    }
  }

  // Submit is removed for individual users - only Save is available
  
  resetForm() {
    this.currentReport = null;
    this.selectedDate = this.today;
    const todayDate = new Date();
    this.selectedDay = this.daysOfWeek[todayDate.getDay() === 0 ? 6 : todayDate.getDay() - 1];
    this.initializeForm();
    this.successMessage = '';
    this.errorMessage = '';
    this.tempActivities = [];
    this.newActivityType = '';
    this.newActivityDescription = '';
    this.newActivityBranch = '';
    this.editingActivityIndex = null;
    this.editingActivityType = '';
    this.editingActivityBranch = '';
  }
  
  async downloadMyReport(report: DailyReport) {
    if (!report.id) {
      this.errorMessage = 'Report ID is missing';
      return;
    }

    this.downloading = true;
    this.errorMessage = '';
    try {
      const blob = await this.reportService.downloadMyReport(report.id).toPromise();
      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const dateStr = report.businessDate.split('T')[0];
        const employeeName = report.employeeUsername || 'report';
        link.download = `Daily_Report_${employeeName}_${dateStr}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.successMessage = 'Report downloaded successfully';
      }
    } catch (error: any) {
      console.error('Download error:', error);
      console.error('Error details:', {
        status: error.status,
        statusText: error.statusText,
        message: error.message,
        error: error.error,
        headers: error.headers
      });
      
      // Check for error message in response headers
      const errorHeader = error.headers?.get('X-Error-Message');
      
      if (error.status === 404) {
        this.errorMessage = errorHeader || 'Report not found. Please refresh and try again.';
      } else if (error.status === 403) {
        this.errorMessage = errorHeader || 'You can only download your own reports. Please ensure you are logged in with the correct account.';
      } else if (error.status === 500) {
        this.errorMessage = errorHeader || 'Server error occurred. Please contact support.';
      } else if (error.error instanceof Blob) {
        // Try to read error message from blob
        const reader = new FileReader();
        reader.onload = () => {
          try {
            const errorText = reader.result as string;
            const errorJson = JSON.parse(errorText);
            this.errorMessage = errorJson.message || 'Failed to download report.';
          } catch {
            this.errorMessage = errorHeader || 'Failed to download report. Please try again.';
          }
          this.downloading = false;
        };
        reader.onerror = () => {
          this.errorMessage = errorHeader || 'Failed to download report. Please try again.';
          this.downloading = false;
        };
        reader.readAsText(error.error);
        return; // Don't set downloading to false yet, wait for reader
      } else {
        this.errorMessage = errorHeader || error.error?.message || error.message || 'Failed to download report. Please try again.';
      }
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
  
  async downloadCombinedReport(date?: string, cbsEndTime?: string, cbsStartTimeNextDay?: string) {
    const downloadDate = date || this.selectedDateForDownload;
    
    if (!downloadDate) {
      this.errorMessage = 'Please select a date to download the combined report';
      return;
    }
    
    if (!cbsEndTime || !cbsStartTimeNextDay) {
      this.errorMessage = 'Please provide CBS End Time and CBS Start Time (Next Day)';
      return;
    }
    
    this.downloading = true;
    this.errorMessage = '';
    try {
      const blob = await this.reportService.downloadCombinedReport(downloadDate, cbsEndTime, cbsStartTimeNextDay).toPromise();
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
