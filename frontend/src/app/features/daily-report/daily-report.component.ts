import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { DailyReportService } from '../../core/services/daily-report.service';
import { DailyReportPermissionService } from '../../core/services/daily-report-permission.service';
import { AuthService } from '../../core/services/auth.service';
import {
  DailyReport,
  CreateDailyReportRequest,
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
  QrmisIssue
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
  
  loading = false;
  saving = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';
  
  today = new Date().toISOString().split('T')[0];
  selectedDate = this.today;
  
  // Permission flags
  canCreate = false;
  canViewAll = false;
  canViewDashboard = false;
  canApprove = false;

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
  }

  initializeForm() {
    this.reportForm = this.fb.group({
      businessDate: [this.today, Validators.required],
      cbsEndTime: ['', Validators.required],
      cbsStartTimeNextDay: ['', Validators.required],
      reportingLine: [''],
      chatCommunications: this.fb.array([]),
      emailCommunications: this.fb.array([]),
      problemEscalations: this.fb.array([]),
      trainingCapacityBuildings: this.fb.array([]),
      projectProgressUpdates: this.fb.array([]),
      cbsTeamActivities: this.fb.array([], Validators.required),
      pendingActivities: this.fb.array([]),
      meetings: this.fb.array([]),
      afpayCardRequests: this.fb.array([]),
      qrmisIssues: this.fb.array([])
    });
  }

  // Chat Communications
  get chatCommunications(): FormArray {
    return this.reportForm.get('chatCommunications') as FormArray;
  }

  addChatCommunication() {
    const group = this.fb.group({
      platform: ['', Validators.required],
      summary: ['', Validators.required],
      actionTaken: [''],
      actionPerformed: [''],
      referenceNumber: ['']
    });
    this.chatCommunications.push(group);
  }

  removeChatCommunication(index: number) {
    this.chatCommunications.removeAt(index);
  }

  // Email Communications
  get emailCommunications(): FormArray {
    return this.reportForm.get('emailCommunications') as FormArray;
  }

  addEmailCommunication() {
    const group = this.fb.group({
      isInternal: [true],
      sender: ['', Validators.required],
      receiver: ['', Validators.required],
      subject: ['', Validators.required],
      summary: ['', Validators.required],
      actionTaken: [''],
      followUpRequired: [false]
    });
    this.emailCommunications.push(group);
  }

  removeEmailCommunication(index: number) {
    this.emailCommunications.removeAt(index);
  }

  // Problem Escalations
  get problemEscalations(): FormArray {
    return this.reportForm.get('problemEscalations') as FormArray;
  }

  addProblemEscalation() {
    const group = this.fb.group({
      escalatedTo: ['', Validators.required],
      reason: ['', Validators.required],
      escalationDateTime: [new Date().toISOString(), Validators.required],
      followUpStatus: [''],
      comments: ['']
    });
    this.problemEscalations.push(group);
  }

  removeProblemEscalation(index: number) {
    this.problemEscalations.removeAt(index);
  }

  // Training & Capacity Building
  get trainingCapacityBuildings(): FormArray {
    return this.reportForm.get('trainingCapacityBuildings') as FormArray;
  }

  addTrainingCapacityBuilding() {
    const group = this.fb.group({
      trainingType: ['', Validators.required],
      topic: ['', Validators.required],
      duration: [''],
      skillsGained: [''],
      trainerName: [''],
      participants: ['']
    });
    this.trainingCapacityBuildings.push(group);
  }

  removeTrainingCapacityBuilding(index: number) {
    this.trainingCapacityBuildings.removeAt(index);
  }

  // Project Progress Updates
  get projectProgressUpdates(): FormArray {
    return this.reportForm.get('projectProgressUpdates') as FormArray;
  }

  addProjectProgressUpdate() {
    const group = this.fb.group({
      projectName: ['', Validators.required],
      taskOrMilestone: [''],
      progressDetail: ['', Validators.required],
      roadblocksIssues: [''],
      estimatedCompletionDate: [''],
      comments: ['']
    });
    this.projectProgressUpdates.push(group);
  }

  removeProjectProgressUpdate(index: number) {
    this.projectProgressUpdates.removeAt(index);
  }

  // CBS Team Activities
  get cbsTeamActivities(): FormArray {
    return this.reportForm.get('cbsTeamActivities') as FormArray;
  }

  addCbsTeamActivity() {
    const group = this.fb.group({
      description: ['', Validators.required],
      branch: [''],
      accountNumber: [''],
      actionTaken: [''],
      finalStatus: [''],
      activityType: ['']
    });
    this.cbsTeamActivities.push(group);
  }

  removeCbsTeamActivity(index: number) {
    this.cbsTeamActivities.removeAt(index);
  }

  // Pending Activities
  get pendingActivities(): FormArray {
    return this.reportForm.get('pendingActivities') as FormArray;
  }

  addPendingActivity() {
    const group = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      status: ['', Validators.required],
      amount: [null],
      followUpRequired: [false],
      responsiblePerson: ['']
    });
    this.pendingActivities.push(group);
  }

  removePendingActivity(index: number) {
    this.pendingActivities.removeAt(index);
  }

  // Meetings
  get meetings(): FormArray {
    return this.reportForm.get('meetings') as FormArray;
  }

  addMeeting() {
    const group = this.fb.group({
      meetingType: ['', Validators.required],
      topic: ['', Validators.required],
      summary: ['', Validators.required],
      actionTaken: [''],
      nextStep: [''],
      participants: ['']
    });
    this.meetings.push(group);
  }

  removeMeeting(index: number) {
    this.meetings.removeAt(index);
  }

  // AFPay Card Requests
  get afpayCardRequests(): FormArray {
    return this.reportForm.get('afpayCardRequests') as FormArray;
  }

  addAfpayCardRequest() {
    const group = this.fb.group({
      requestType: ['', Validators.required],
      requestedBy: ['', Validators.required],
      requestDate: [this.today, Validators.required],
      resolutionDetails: [''],
      supportingDocumentPath: [''],
      archivedDate: [''],
      operator: ['']
    });
    this.afpayCardRequests.push(group);
  }

  removeAfpayCardRequest(index: number) {
    this.afpayCardRequests.removeAt(index);
  }

  // QRMIS Issues
  get qrmisIssues(): FormArray {
    return this.reportForm.get('qrmisIssues') as FormArray;
  }

  addQrmisIssue() {
    const group = this.fb.group({
      problemType: ['', Validators.required],
      problemDescription: ['', Validators.required],
      solutionProvided: [''],
      postedBy: [''],
      authorizedBy: [''],
      supportingDocumentsArchived: [''],
      operator: ['']
    });
    this.qrmisIssues.push(group);
  }

  removeQrmisIssue(index: number) {
    this.qrmisIssues.removeAt(index);
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
    this.reportForm.patchValue({
      businessDate: report.businessDate,
      cbsEndTime: report.cbsEndTime,
      cbsStartTimeNextDay: report.cbsStartTimeNextDay,
      reportingLine: report.reportingLine
    });

    // Load arrays
    this.loadArrayToFormArray(this.chatCommunications, report.chatCommunications);
    this.loadArrayToFormArray(this.emailCommunications, report.emailCommunications);
    this.loadArrayToFormArray(this.problemEscalations, report.problemEscalations);
    this.loadArrayToFormArray(this.trainingCapacityBuildings, report.trainingCapacityBuildings);
    this.loadArrayToFormArray(this.projectProgressUpdates, report.projectProgressUpdates);
    this.loadArrayToFormArray(this.cbsTeamActivities, report.cbsTeamActivities);
    this.loadArrayToFormArray(this.pendingActivities, report.pendingActivities);
    this.loadArrayToFormArray(this.meetings, report.meetings);
    this.loadArrayToFormArray(this.afpayCardRequests, report.afpayCardRequests);
    this.loadArrayToFormArray(this.qrmisIssues, report.qrmisIssues);
  }

  loadArrayToFormArray(formArray: FormArray, data: any[]) {
    formArray.clear();
    data.forEach(item => {
      const group = this.fb.group(item);
      formArray.push(group);
    });
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
      const formValue = this.reportForm.value;
      const request: CreateDailyReportRequest = {
        businessDate: formValue.businessDate,
        cbsEndTime: formValue.cbsEndTime,
        cbsStartTimeNextDay: formValue.cbsStartTimeNextDay,
        reportingLine: formValue.reportingLine,
        chatCommunications: formValue.chatCommunications || [],
        emailCommunications: formValue.emailCommunications || [],
        problemEscalations: formValue.problemEscalations || [],
        trainingCapacityBuildings: formValue.trainingCapacityBuildings || [],
        projectProgressUpdates: formValue.projectProgressUpdates || [],
        cbsTeamActivities: formValue.cbsTeamActivities || [],
        pendingActivities: formValue.pendingActivities || [],
        meetings: formValue.meetings || [],
        afpayCardRequests: formValue.afpayCardRequests || [],
        qrmisIssues: formValue.qrmisIssues || []
      };

      if (this.currentReport?.id) {
        await this.reportService.updateReport(this.currentReport.id, request).toPromise();
        this.successMessage = 'Report saved successfully';
      } else {
        const report = await this.reportService.createReport(request).toPromise();
        this.currentReport = report!;
        this.successMessage = 'Report created successfully';
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

    if (this.cbsTeamActivities.length === 0) {
      this.errorMessage = 'At least one CBS Team Activity is required';
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
    } catch (error: any) {
      this.errorMessage = error.error?.message || 'Failed to submit report';
    } finally {
      this.submitting = false;
    }
  }

  async loadMyReports() {
    this.loading = true;
    try {
      const response = await this.reportService.getMyReports(0, 100).toPromise();
      this.myReports = response?.content || [];
    } catch (error: any) {
      this.errorMessage = 'Failed to load reports';
    } finally {
      this.loading = false;
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
      this.loadMyReports();
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
      this.currentReport = fullReport!;
      // Could open a view modal here
    } catch (error: any) {
      this.errorMessage = 'Failed to load report';
    } finally {
      this.loading = false;
    }
  }
}
