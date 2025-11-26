import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
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
  private cdr = inject(ChangeDetectorRef);

  activeTab: 'create' | 'my-reports' | 'my-dashboard' | 'dashboard' | 'quality-control' = 'create';
  
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
  
  // Current user's full name from session
  currentUserFullName: string = '';
  
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
  isQualityControl = false;
  
  // Download state
  downloading = false;
  
  // Reports organized by date
  reportsByDate: Map<string, DailyReport[]> = new Map();
  selectedDateForDownload = '';
  
  // Dashboard Statistics
  dashboardStats = {
    totalReports: 0,
    totalActivities: 0,
    approvedReports: 0,
    draftReports: 0,
    activitiesByCategory: new Map<string, number>()
  };

  ngOnInit() {
    this.checkPermissions();
    this.loadCurrentUserFullName();
    this.initializeForm();
    if (this.isController) {
      this.initializeCombinedReportForm();
    }
    this.loadMyReports();
    if (this.canViewDashboard) {
      this.loadDashboard();
    }
    // Load draft for today's date if it exists
    if (this.activeTab === 'create') {
      this.loadDraftForDate();
    }
  }

  loadCurrentUserFullName() {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUserFullName = user?.fullName || '';
    });
  }

  checkPermissions() {
    this.canCreate = this.permissionService.canCreateReport();
    this.canViewAll = this.permissionService.canViewAllReports();
    this.canViewDashboard = this.permissionService.canViewDashboard();
    this.canApprove = this.permissionService.canApproveReports();
    this.isController = this.permissionService.isController();
    this.isCFO = this.permissionService.isCFO();
    this.isQualityControl = this.permissionService.isQualityControl();
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
    const activityType = this.newActivityType?.trim() || '';
    const description = this.newActivityDescription?.trim() || '';
    const branch = this.newActivityBranch?.trim() || '';
    
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
      const activity = this.tempActivities[this.editingActivityIndex];
      activity.activityType = activityType;
      activity.description = description;
      activity.branch = branch || undefined;
      
      // Reset editing state
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
        branch: branch || undefined
      });
    }
    
    // Clear form fields
    this.newActivityType = '';
    this.newActivityDescription = '';
    this.newActivityBranch = '';
    this.errorMessage = '';
  }
  
  editActivity(index: number) {
    this.editingActivityIndex = index;
    const activity = this.tempActivities[index];
    this.editingActivityType = activity.activityType;
    this.editingActivityText = activity.description;
    // Set form values - ensure branch is set properly
    this.newActivityType = activity.activityType;
    this.newActivityDescription = activity.description;
    this.newActivityBranch = activity.branch || '';
    // Force change detection to ensure select updates
    this.cdr.detectChanges();
  }
  
  cancelEdit() {
    this.editingActivityIndex = null;
    this.editingActivityType = '';
    this.editingActivityText = '';
    this.editingActivityBranch = '';
    // Clear form fields
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
      this.selectedDate = this.reportForm.get('businessDate')?.value;
      this.loadDraftForDate();
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
      const desc = activity.description?.trim() || '';
      const branch = activity.branch?.trim() || '';
      
      if (!desc) return; // Skip empty descriptions
      
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
            branch: branch || undefined
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
            requestDate: this.reportForm.get('businessDate')?.value || this.today,
            resolutionDetails: desc
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
    this.successMessage = '';
    
    try {
      const report = await this.reportService.getReportByDate(this.selectedDate).toPromise();
      if (report) {
        this.currentReport = report;
        // Clear form first, then load report data
        this.tempActivities = [];
        this.activityIdCounter = 1;
        this.loadFormFromReport(report);
      } else {
        // No report exists for this date - reset form for new report
        this.currentReport = null;
        this.resetForm();
        this.reportForm.patchValue({ businessDate: this.selectedDate });
        const date = new Date(this.selectedDate);
        const dayName = this.daysOfWeek[date.getDay() === 0 ? 6 : date.getDay() - 1];
        this.selectedDay = dayName;
        this.reportForm.patchValue({ dayOfWeek: dayName });
      }
    } catch (error: any) {
      // If 404 or no report found, initialize empty form
      if (error.status === 404 || error.status === 0) {
        this.currentReport = null;
        this.resetForm();
        this.reportForm.patchValue({ businessDate: this.selectedDate });
        const date = new Date(this.selectedDate);
        const dayName = this.daysOfWeek[date.getDay() === 0 ? 6 : date.getDay() - 1];
        this.selectedDay = dayName;
        this.reportForm.patchValue({ dayOfWeek: dayName });
      } else {
        this.errorMessage = error.error?.message || 'Failed to load report';
      }
    } finally {
      this.loading = false;
    }
  }

  // Load draft automatically when component initializes or date changes
  async loadDraftForDate() {
    if (!this.selectedDate) return;
    
    try {
      const report = await this.reportService.getReportByDate(this.selectedDate).toPromise();
      if (report && (report.status === 'DRAFT' || report.status === ReportStatus.DRAFT)) {
        // Auto-load draft if it exists
        this.currentReport = report;
        this.tempActivities = [];
        this.activityIdCounter = 1;
        this.loadFormFromReport(report);
        this.successMessage = 'Draft loaded automatically';
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      }
    } catch (error: any) {
      // Silently fail if no draft exists - this is expected
      if (error.status !== 404 && error.status !== 0) {
        console.error('Error loading draft:', error);
      }
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
    // Clear existing activities first
    this.tempActivities = [];
    this.activityIdCounter = 1;
    
    // Convert CBS Team Activities (including all subtypes)
    if (report.cbsTeamActivities && report.cbsTeamActivities.length > 0) {
      report.cbsTeamActivities.forEach(activity => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: activity.activityType || 'CBS Team Activity',
          description: activity.description,
          branch: activity.branch
        });
      });
    }
    
    // Convert Chat Communications
    if (report.chatCommunications && report.chatCommunications.length > 0) {
      report.chatCommunications.forEach(chat => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Chat Communication',
          description: chat.summary || ''
        });
      });
    }
    
    // Convert Email Communications
    if (report.emailCommunications && report.emailCommunications.length > 0) {
      report.emailCommunications.forEach(email => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Email Communication',
          description: email.summary || email.subject || ''
        });
      });
    }
    
    // Convert Problem Escalations
    if (report.problemEscalations && report.problemEscalations.length > 0) {
      report.problemEscalations.forEach(escalation => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Problem Escalation',
          description: escalation.reason || ''
        });
      });
    }
    
    // Convert Training & Capacity Building
    if (report.trainingCapacityBuildings && report.trainingCapacityBuildings.length > 0) {
      report.trainingCapacityBuildings.forEach(training => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Training & Capacity Building',
          description: training.topic || ''
        });
      });
    }
    
    // Convert Project Progress Updates
    if (report.projectProgressUpdates && report.projectProgressUpdates.length > 0) {
      report.projectProgressUpdates.forEach(project => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Project Progress Update',
          description: project.progressDetail || ''
        });
      });
    }
    
    // Convert Pending Activities
    if (report.pendingActivities && report.pendingActivities.length > 0) {
      report.pendingActivities.forEach(pending => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Pending Activity',
          description: pending.description || pending.title || ''
        });
      });
    }
    
    // Convert Meetings
    if (report.meetings && report.meetings.length > 0) {
      report.meetings.forEach(meeting => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'Meeting',
          description: meeting.summary || meeting.topic || ''
        });
      });
    }
    
    // Convert AFPay Card Requests
    if (report.afpayCardRequests && report.afpayCardRequests.length > 0) {
      report.afpayCardRequests.forEach(afpay => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'AFPay Card Request',
          description: afpay.resolutionDetails || ''
        });
      });
    }
    
    // Convert QRMIS Issues
    if (report.qrmisIssues && report.qrmisIssues.length > 0) {
      report.qrmisIssues.forEach(qrmis => {
        this.tempActivities.push({
          id: this.activityIdCounter++,
          activityType: 'QRMIS Issue',
          description: qrmis.problemDescription || ''
        });
      });
    }
    
    // Force change detection to ensure UI updates
    this.cdr.detectChanges();
  }

  async saveDraft() {
    if (this.reportForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      const request = this.convertTempActivitiesToBackendFormat();
      const reportId = this.currentReport?.id;

      if (reportId) {
        // Update existing report (keeps current status, or sets to DRAFT if needed)
        const updatedReport = await this.reportService.updateReport(reportId, request).toPromise();
        this.currentReport = updatedReport!;
        this.successMessage = 'Draft saved successfully';
        this.loadMyReports();
        
        // Refresh view modal if it's open
        if (this.showViewModal && this.viewReportModal?.id === reportId) {
          this.refreshViewModal();
        }
      } else {
        // Create new report as DRAFT
        const report = await this.reportService.createReport(request).toPromise();
        this.currentReport = report!;
        this.successMessage = 'Draft saved successfully';
        this.loadMyReports();
      }
    } catch (error: any) {
      console.error('Error saving draft:', error);
      this.errorMessage = error.error?.message || 'Failed to save draft';
    } finally {
      this.saving = false;
    }
  }

  async submitReport() {
    if (this.reportForm.invalid) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    if (this.tempActivities.length === 0) {
      this.errorMessage = 'Please add at least one activity before submitting';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    try {
      // First ensure draft is saved
      const request = this.convertTempActivitiesToBackendFormat();
      let reportId = this.currentReport?.id;

      if (!reportId) {
        // Create new report first
        const report = await this.reportService.createReport(request).toPromise();
        this.currentReport = report!;
        reportId = report!.id!;
      } else {
        // Update existing report
        const updatedReport = await this.reportService.updateReport(reportId, request).toPromise();
        this.currentReport = updatedReport!;
      }

      // Now submit the report
      if (reportId) {
        const submittedReport = await this.reportService.submitReport(reportId).toPromise();
        this.currentReport = submittedReport!;
        this.successMessage = 'Report submitted successfully';
        this.loadMyReports();
        
        // Refresh view modal if it's open
        if (this.showViewModal && this.viewReportModal?.id === reportId) {
          this.refreshViewModal();
        }
      }
    } catch (error: any) {
      console.error('Error submitting report:', error);
      this.errorMessage = error.error?.message || 'Failed to submit report';
    } finally {
      this.submitting = false;
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
        const employeeName = report.employeeFullName || report.employeeUsername || 'report';
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

  setActiveTab(tab: 'create' | 'my-reports' | 'my-dashboard' | 'dashboard' | 'quality-control') {
    this.activeTab = tab;
    if (tab === 'my-reports') {
      // Small delay to ensure tab is visible before loading
      setTimeout(() => {
        this.loadMyReports();
      }, 100);
    } else if (tab === 'my-dashboard') {
      setTimeout(() => {
        this.loadMyDashboard();
      }, 100);
    } else if (tab === 'create') {
      // Load draft when switching to create tab
      setTimeout(() => {
        this.loadDraftForDate();
      }, 100);
    } else if (tab === 'quality-control') {
      setTimeout(() => {
        this.loadQualityControlReports();
      }, 100);
    }
  }

  async loadReportForEdit(report: DailyReport) {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    try {
      // Always fetch fresh data to ensure we have the latest version
      const fullReport = await this.reportService.getReport(report.id!).toPromise();
      if (fullReport) {
        this.currentReport = fullReport;
        this.selectedDate = fullReport.businessDate;
        
        // Clear existing form data first
        this.tempActivities = [];
        this.activityIdCounter = 1;
        this.newActivityType = '';
        this.newActivityDescription = '';
        this.newActivityBranch = '';
        this.editingActivityIndex = null;
        
        // Load the report data into the form
        this.loadFormFromReport(fullReport);
        this.setActiveTab('create');
        
        // Scroll to top of form
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    } catch (error: any) {
      console.error('Error loading report for edit:', error);
      this.errorMessage = error.error?.message || 'Failed to load report for editing';
    } finally {
      this.loading = false;
    }
  }

  async viewReport(report: DailyReport) {
    this.loading = true;
    try {
      // Always fetch fresh data to ensure latest changes are shown
      const fullReport = await this.reportService.getReport(report.id!).toPromise();
      this.viewReportModal = fullReport!;
      this.showViewModal = true;
    } catch (error: any) {
      this.errorMessage = 'Failed to load report';
    } finally {
      this.loading = false;
    }
  }
  
  // Refresh view modal after updates
  refreshViewModal() {
    if (this.viewReportModal && this.viewReportModal.id) {
      this.viewReport(this.viewReportModal);
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

  // Get all activities grouped by type for display
  getAllActivities(report: DailyReport | null): Array<{activityName: string, description: string, branch?: string, accountNumber?: string}> {
    if (!report) return [];
    
    const activities: Array<{activityName: string, description: string, branch?: string, accountNumber?: string}> = [];
    
    // CBS Team Activities
    if (report.cbsTeamActivities && report.cbsTeamActivities.length > 0) {
      report.cbsTeamActivities.forEach(activity => {
        activities.push({
          activityName: activity.activityType || 'CBS Team Activity',
          description: activity.description,
          branch: activity.branch,
          accountNumber: activity.accountNumber
        });
      });
    }
    
    // Email Communications
    if (report.emailCommunications && report.emailCommunications.length > 0) {
      report.emailCommunications.forEach(email => {
        activities.push({
          activityName: 'Email Communication',
          description: email.summary || email.subject || ''
        });
      });
    }
    
    // Chat Communications
    if (report.chatCommunications && report.chatCommunications.length > 0) {
      report.chatCommunications.forEach(chat => {
        activities.push({
          activityName: 'Chat Communication',
          description: chat.summary || ''
        });
      });
    }
    
    // Problem Escalations
    if (report.problemEscalations && report.problemEscalations.length > 0) {
      report.problemEscalations.forEach(escalation => {
        activities.push({
          activityName: 'Problem Escalation',
          description: escalation.reason || ''
        });
      });
    }
    
    // Pending Activities
    if (report.pendingActivities && report.pendingActivities.length > 0) {
      report.pendingActivities.forEach(pending => {
        activities.push({
          activityName: 'Pending Activity',
          description: pending.description || pending.title || ''
        });
      });
    }
    
    // Meetings
    if (report.meetings && report.meetings.length > 0) {
      report.meetings.forEach(meeting => {
        activities.push({
          activityName: 'Meeting',
          description: meeting.summary || meeting.topic || ''
        });
      });
    }
    
    // Training & Capacity Building
    if (report.trainingCapacityBuildings && report.trainingCapacityBuildings.length > 0) {
      report.trainingCapacityBuildings.forEach(training => {
        activities.push({
          activityName: 'Training & Capacity Building',
          description: training.topic || ''
        });
      });
    }
    
    // Project Progress Updates
    if (report.projectProgressUpdates && report.projectProgressUpdates.length > 0) {
      report.projectProgressUpdates.forEach(project => {
        activities.push({
          activityName: 'Project Progress Update',
          description: project.progressDetail || ''
        });
      });
    }
    
    // AFPay Card Requests
    if (report.afpayCardRequests && report.afpayCardRequests.length > 0) {
      report.afpayCardRequests.forEach(afpay => {
        activities.push({
          activityName: 'AFPay Card Request',
          description: afpay.resolutionDetails || ''
        });
      });
    }
    
    // QRMIS Issues
    if (report.qrmisIssues && report.qrmisIssues.length > 0) {
      report.qrmisIssues.forEach(qrmis => {
        activities.push({
          activityName: 'QRMIS Issue',
          description: qrmis.problemDescription || ''
        });
      });
    }
    
    return activities;
  }
  
  // Get activities grouped by type for display
  getGroupedActivities(report: DailyReport | null): Map<string, Array<{description: string, branch?: string, accountNumber?: string}>> {
    const allActivities = this.getAllActivities(report);
    const grouped = new Map<string, Array<{description: string, branch?: string, accountNumber?: string}>>();
    
    allActivities.forEach(activity => {
      const key = activity.activityName;
      if (!grouped.has(key)) {
        grouped.set(key, []);
      }
      grouped.get(key)!.push({
        description: activity.description,
        branch: activity.branch,
        accountNumber: activity.accountNumber
      });
    });
    
    return grouped;
  }
  
  // Get activity type names for iteration
  getActivityTypes(report: DailyReport | null): string[] {
    return Array.from(this.getGroupedActivities(report).keys());
  }
  
  // Get activities for a specific type
  getActivitiesForType(report: DailyReport | null, activityType: string): Array<{description: string, branch?: string, accountNumber?: string}> {
    return this.getGroupedActivities(report).get(activityType) || [];
  }

  // TrackBy function for activity list to ensure proper rendering
  trackByActivityId(index: number, activity: { id: number; activityType: string; description: string; branch?: string }): number {
    return activity.id;
  }

  async loadMyDashboard() {
    this.loading = true;
    try {
      // Load all user reports to calculate statistics
      const response = await this.reportService.getMyReports(0, 1000).toPromise();
      const reports = response?.content || (Array.isArray(response) ? response : []);
      
      // Calculate statistics
      this.dashboardStats.totalReports = reports.length;
      this.dashboardStats.totalActivities = 0;
      this.dashboardStats.approvedReports = 0;
      this.dashboardStats.draftReports = 0;
      this.dashboardStats.activitiesByCategory.clear();
      
      reports.forEach(report => {
        // Count activities
        const activities = this.getAllActivities(report);
        this.dashboardStats.totalActivities += activities.length;
        
        // Count by status
        if (report.status === 'APPROVED') {
          this.dashboardStats.approvedReports++;
        } else if (report.status === 'DRAFT') {
          this.dashboardStats.draftReports++;
        }
        
        // Count by category
        activities.forEach(activity => {
          const category = activity.activityName || 'Other';
          const currentCount = this.dashboardStats.activitiesByCategory.get(category) || 0;
          this.dashboardStats.activitiesByCategory.set(category, currentCount + 1);
        });
      });
    } catch (error: any) {
      console.error('Error loading dashboard:', error);
      this.errorMessage = 'Failed to load dashboard statistics';
    } finally {
      this.loading = false;
    }
  }

  getActivityCategories(): Array<{name: string, count: number}> {
    const categories: Array<{name: string, count: number}> = [];
    this.dashboardStats.activitiesByCategory.forEach((count, name) => {
      categories.push({ name, count });
    });
    return categories.sort((a, b) => b.count - a.count);
  }

  getCategoryPercentage(count: number): number {
    if (this.dashboardStats.totalActivities === 0) return 0;
    return (count / this.dashboardStats.totalActivities) * 100;
  }

  getCategoryColor(categoryName: string): string {
    const colors: Record<string, string> = {
      'CBS Team Activity': 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
      'Chat Communication': 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
      'Email Communication': 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
      'Problem Escalation': 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
      'Training & Capacity Building': 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
      'Project Progress Update': 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
      'Pending Activity': 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)',
      'Meeting': 'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
      'AFPay Card Request': 'linear-gradient(135deg, #14b8a6 0%, #0d9488 100%)',
      'QRMIS Issue': 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
      'Reversals': 'linear-gradient(135deg, #D34E4E 0%, #b83d3d 100%)',
      'Branch coordination': 'linear-gradient(135deg, #84cc16 0%, #65a30d 100%)'
    };
    return colors[categoryName] || 'linear-gradient(135deg, #6b7280 0%, #4b5563 100%)';
  }

  getLast30Days(): Array<{date: string, day: string, count: number}> {
    const days: Array<{date: string, day: string, count: number}> = [];
    const today = new Date();
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];
      const dayStr = date.toLocaleDateString('en-US', { weekday: 'short' });
      
      // Count reports for this date
      const count = this.myReports.filter(r => {
        const reportDate = r.businessDate.split('T')[0];
        return reportDate === dateStr;
      }).length;
      
      days.push({ date: dateStr, day: dayStr, count });
    }
    
    return days;
  }

  getDayPercentage(count: number): number {
    const maxCount = Math.max(...this.getLast30Days().map(d => d.count), 1);
    if (maxCount === 0) return 0;
    return (count / maxCount) * 100;
  }

  // Quality Control features
  qualityControlSelectedDate = this.today;
  qualityControlReports: DailyReport[] = [];
  qualityControlLoading = false;
  selectedReportForReview: DailyReport | null = null;
  showReviewModal = false;
  reviewFeedback: { [key: string]: string } = {};
  confirmedActivities: Set<string> = new Set();
  qualityControlCbsEndTime = '';
  qualityControlCbsStartTime = '';
  qualityControlReportLine = '';
  
  // Task management for Quality Control
  pendingTasks: Array<{id: number, description: string, date: string}> = [];
  achievementTasks: Array<{id: number, description: string, date: string}> = [];
  plannedTasks: Array<{id: number, description: string, date: string}> = [];
  newPendingTask = '';
  newAchievementTask = '';
  newPlannedTask = '';
  private taskIdCounter = 1;

  async loadQualityControlReports() {
    if (!this.qualityControlSelectedDate) return;
    
    this.qualityControlLoading = true;
    this.errorMessage = '';
    
    try {
      const reports = await this.reportService.getReportsByDate(this.qualityControlSelectedDate).toPromise();
      if (reports) {
        // Filter to only show SUBMITTED reports
        this.qualityControlReports = reports.filter(r => r.status === 'SUBMITTED' || r.status === ReportStatus.SUBMITTED);
      } else {
        this.qualityControlReports = [];
      }
    } catch (error: any) {
      console.error('Error loading quality control reports:', error);
      this.errorMessage = error.error?.message || 'Failed to load reports';
      this.qualityControlReports = [];
    } finally {
      this.qualityControlLoading = false;
    }
  }

  async reviewReportActivity(report: DailyReport, activityId: string, action: 'confirm' | 'feedback') {
    if (action === 'confirm') {
      this.confirmedActivities.add(activityId);
    } else if (action === 'feedback') {
      // Show feedback input modal or inline input
      const feedback = prompt('Enter feedback for this activity:');
      if (feedback) {
        this.reviewFeedback[activityId] = feedback;
      }
    }
  }

  async updateReportForQualityControl(report: DailyReport) {
    if (!report.id) return;
    
    this.qualityControlLoading = true;
    try {
      const updateRequest: UpdateDailyReportRequest = {
        cbsEndTime: this.qualityControlCbsEndTime || report.cbsEndTime,
        cbsStartTimeNextDay: this.qualityControlCbsStartTime || report.cbsStartTimeNextDay,
        reportingLine: this.qualityControlReportLine || report.reportingLine,
        chatCommunications: report.chatCommunications || [],
        emailCommunications: report.emailCommunications || [],
        problemEscalations: report.problemEscalations || [],
        trainingCapacityBuildings: report.trainingCapacityBuildings || [],
        projectProgressUpdates: report.projectProgressUpdates || [],
        cbsTeamActivities: report.cbsTeamActivities || [],
        pendingActivities: report.pendingActivities || [],
        meetings: report.meetings || [],
        afpayCardRequests: report.afpayCardRequests || [],
        qrmisIssues: report.qrmisIssues || []
      };
      
      const updated = await this.reportService.updateReport(report.id, updateRequest).toPromise();
      if (updated) {
        this.successMessage = 'Report updated successfully';
        this.loadQualityControlReports();
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to update report';
    } finally {
      this.qualityControlLoading = false;
    }
  }

  addPendingTask() {
    if (this.newPendingTask.trim()) {
      this.pendingTasks.push({
        id: this.taskIdCounter++,
        description: this.newPendingTask.trim(),
        date: this.qualityControlSelectedDate
      });
      this.newPendingTask = '';
    }
  }

  addAchievementTask() {
    if (this.newAchievementTask.trim()) {
      this.achievementTasks.push({
        id: this.taskIdCounter++,
        description: this.newAchievementTask.trim(),
        date: this.qualityControlSelectedDate
      });
      this.newAchievementTask = '';
    }
  }

  addPlannedTask() {
    if (this.newPlannedTask.trim()) {
      this.plannedTasks.push({
        id: this.taskIdCounter++,
        description: this.newPlannedTask.trim(),
        date: this.qualityControlSelectedDate
      });
      this.newPlannedTask = '';
    }
  }

  removePendingTask(index: number) {
    this.pendingTasks.splice(index, 1);
  }

  removeAchievementTask(index: number) {
    this.achievementTasks.splice(index, 1);
  }

  removePlannedTask(index: number) {
    this.plannedTasks.splice(index, 1);
  }

  async generateTeamDailyReport() {
    if (!this.qualityControlSelectedDate) {
      this.errorMessage = 'Please select a date';
      return;
    }

    if (!this.qualityControlCbsEndTime || !this.qualityControlCbsStartTime) {
      this.errorMessage = 'Please enter CBS Start Time and CBS End Time';
      return;
    }

    this.downloading = true;
    this.errorMessage = '';
    
    try {
      const blob = await this.reportService.downloadCombinedReport(
        this.qualityControlSelectedDate,
        this.qualityControlCbsEndTime,
        this.qualityControlCbsStartTime
      ).toPromise();
      
      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const dateStr = this.qualityControlSelectedDate.split('T')[0];
        link.download = `CBS_Team_Daily_Report_${dateStr}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.successMessage = 'Team Daily Report generated successfully';
      }
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to generate team report';
    } finally {
      this.downloading = false;
    }
  }

  openReviewModal(report: DailyReport) {
    this.selectedReportForReview = report;
    this.showReviewModal = true;
    this.qualityControlCbsEndTime = report.cbsEndTime || '';
    this.qualityControlCbsStartTime = report.cbsStartTimeNextDay || '';
    this.qualityControlReportLine = report.reportingLine || '';
  }

  closeReviewModal() {
    this.showReviewModal = false;
    this.selectedReportForReview = null;
    this.reviewFeedback = {};
    this.confirmedActivities.clear();
  }

  async confirmAllReportsForDate() {
    if (!this.qualityControlSelectedDate) return;
    
    this.qualityControlLoading = true;
    try {
      // Confirm all reports for the selected date
      for (const report of this.qualityControlReports) {
        if (report.id) {
          const reviewRequest: ReviewReportRequest = {
            status: ReportStatus.APPROVED,
            reviewComments: 'Confirmed by Quality Control'
          };
          await this.reportService.reviewReport(report.id, reviewRequest).toPromise();
        }
      }
      this.successMessage = 'All reports confirmed successfully';
      this.loadQualityControlReports();
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to confirm reports';
    } finally {
      this.qualityControlLoading = false;
    }
  }

}
