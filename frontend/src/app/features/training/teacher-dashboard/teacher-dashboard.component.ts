import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TrainingService } from '../../../core/services/training.service';
import { MasterSetupService } from '../../../core/services/master-setup.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { 
  TrainingProgram, 
  TrainingSession, 
  CreateTrainingSessionRequest,
  TrainingMaterial,
  CreateTrainingMaterialRequest,
  Enrollment,
  Attendance,
  MarkAttendanceRequest,
  StudentAttendance,
  SessionStatus,
  AttendanceStatus,
  MaterialType,
  SingleSessionReport,
  DateBasedGroupedReport
} from '../../../core/models/training';
import { StudentTeacher } from '../../../core/models/master';
import { User } from '../../../core/models';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './teacher-dashboard.component.html',
  styleUrls: ['./teacher-dashboard.component.scss']
})
export class TeacherDashboardComponent implements OnInit {
  private trainingService = inject(TrainingService);
  private masterSetupService = inject(MasterSetupService);
  private authService = inject(AuthService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);

  currentUser: User | null = null;
  activeTab: 'overview' | 'programs' | 'sessions' | 'materials' | 'students' | 'attendance' | 'reports' = 'overview';
  tabs: ('overview' | 'programs' | 'sessions' | 'materials' | 'students' | 'attendance' | 'reports')[] = 
    ['overview', 'programs', 'sessions', 'materials', 'students', 'attendance', 'reports'];
  
  // Programs
  programs: TrainingProgram[] = [];
  selectedProgram: TrainingProgram | null = null;
  
  // Sessions
  sessions: TrainingSession[] = [];
  filteredSessions: TrainingSession[] = [];
  visibleSessions: TrainingSession[] = [];
  sessionFiltersForm: FormGroup;
  sessionPage = 1;
  sessionPageSize = 5;
  sessionPageSizeOptions: number[] = [5, 10, 20];
  sessionTotalPages = 1;
  sessionPageNumbers: number[] = [];
  showSessionModal = false;
  sessionForm: FormGroup;
  selectedSession: TrainingSession | null = null;
  showDeleteSessionModal = false;
  sessionPendingDelete: TrainingSession | null = null;
  
  // Materials
  materials: TrainingMaterial[] = [];
  showMaterialModal = false;
  materialForm: FormGroup;
  selectedMaterial: TrainingMaterial | null = null;
  filteredMaterials: TrainingMaterial[] = [];
  visibleMaterials: TrainingMaterial[] = [];
  materialFiltersForm: FormGroup;
  materialPage = 1;
  materialPageSize = 5;
  materialPageSizeOptions: number[] = [5, 10, 20];
  materialTotalPages = 1;
  materialPageNumbers: number[] = [];
  showDeleteMaterialModal = false;
  materialPendingDelete: TrainingMaterial | null = null;
  
  // Students
  students: StudentTeacher[] = [];
  programStudents: Enrollment[] = [];
  selectedProgramForStudents: TrainingProgram | null = null;
  selectedStudentIds: Set<number> = new Set();
  
  // Attendance
  attendanceRecords: Attendance[] = [];
  selectedSessionForAttendance: TrainingSession | null = null;
  showAttendanceModal = false;
  attendanceForm: FormGroup;
  attendanceStudents: Enrollment[] = [];
  savingAttendance = false;
  
  // Reports
  reportsMode: 'session' | 'dateRange' = 'session';
  sessionReportForm: FormGroup;
  dateRangeReportForm: FormGroup;
  singleSessionReport: SingleSessionReport | null = null;
  groupedReport: DateBasedGroupedReport | null = null;
  reportsLoading = false;
  
  loading = false;
  
  // Enums for template
  SessionStatus = SessionStatus;
  AttendanceStatus = AttendanceStatus;
  MaterialType = MaterialType;

  constructor() {
    this.sessionForm = this.fb.group({
      programId: [null, Validators.required],
      startDateTime: ['', Validators.required],
      topic: [''],
      location: [''],
      sessionType: [''],
      status: ['SCHEDULED'],
      sessionLink: [''],
      notes: ['']
    });
    
    this.materialForm = this.fb.group({
      programId: [null, Validators.required],
      title: ['', Validators.required],
      description: [''],
      materialType: [''],
      linkUrl: ['', Validators.required],
      isRequired: [false],
      displayOrder: [null]
    });
    
    this.attendanceForm = this.fb.group({});

    this.sessionReportForm = this.fb.group({
      sessionId: [null, Validators.required]
    });

    this.dateRangeReportForm = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required]
    });

    this.sessionFiltersForm = this.fb.group({
      programId: [null],
      status: [''],
      sessionType: [''],
      fromDate: [''],
      toDate: [''],
      search: [''],
      sequenceOrder: ['']
    });

    this.materialFiltersForm = this.fb.group({
      materialType: [''],
      status: [''],
      fromDate: [''],
      toDate: [''],
      search: [''],
      displayOrder: ['']
    });

    this.sessionFiltersForm.valueChanges.subscribe(() => {
      this.sessionPage = 1;
      this.applySessionFilters();
    });

    this.materialFiltersForm.valueChanges.subscribe(() => {
      this.materialPage = 1;
      this.applyMaterialFilters();
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user?.id) {
        this.loadPrograms();
        this.loadStudents();
        this.loadSessions();
      }
    });
  }

  // Programs
  loadPrograms(): void {
    if (!this.currentUser?.id) return;
    this.loading = true;
    this.trainingService.getProgramsByInstructor(this.currentUser.id).subscribe({
      next: (programs) => {
        this.programs = programs || [];
        // If user is on Materials tab and no program selected yet, auto-select first to show materials list
        if (this.activeTab === 'materials' && !this.selectedProgram && this.programs.length > 0) {
          this.selectedProgram = this.programs[0];
          this.loadMaterials(this.selectedProgram.id);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading programs:', err);
        this.toastr.error('Failed to load programs');
        this.programs = [];
        this.loading = false;
      }
    });
  }

  selectProgram(program: TrainingProgram): void {
    this.selectedProgram = program;
    this.loadSessions(program.id);
    this.loadMaterials(program.id);
    this.loadProgramStudents(program.id);
  }

  // Sessions
  loadSessions(programId?: number): void {
    this.loading = true;
    this.trainingService.getAllSessions(programId).subscribe({
      next: (sessions) => {
        this.sessions = sessions || [];
        this.applySessionFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading sessions:', err);
        this.toastr.error('Failed to load sessions');
        this.sessions = [];
        this.loading = false;
      }
    });
  }

  private applySessionFilters(): void {
    if (!this.sessions) {
      this.filteredSessions = [];
      this.visibleSessions = [];
      return;
    }

    const { programId, status, sessionType, fromDate, toDate, search, sequenceOrder } = this.sessionFiltersForm.value;

    let data = [...this.sessions];

    if (programId) {
      data = data.filter(s => s.programId === programId);
    }

    if (status) {
      data = data.filter(s => s.status === status);
    }

    if (sessionType) {
      const typeLower = sessionType.toLowerCase();
      data = data.filter(s => (s.sessionType || '').toLowerCase() === typeLower);
    }

    if (fromDate) {
      const from = new Date(fromDate);
      data = data.filter(s => new Date(s.startDateTime) >= from);
    }

    if (toDate) {
      const to = new Date(toDate);
      // Include the full "to" day
      to.setHours(23, 59, 59, 999);
      data = data.filter(s => new Date(s.startDateTime) <= to);
    }

    if (search) {
      const term = (search as string).toLowerCase();
      data = data.filter(s =>
        (s.programTitle || '').toLowerCase().includes(term) ||
        (s.topicName || '').toLowerCase().includes(term) ||
        (s.location || '').toLowerCase().includes(term) ||
        (s.sessionType || '').toLowerCase().includes(term) ||
        (s.status || '').toLowerCase().includes(term)
      );
    }
    
    if (sequenceOrder !== null && sequenceOrder !== undefined && sequenceOrder !== '') {
      const seq = Number(sequenceOrder);
      if (!Number.isNaN(seq)) {
        data = data.filter(s => (s.sequenceOrder ?? null) === seq);
      }
    }

    // Sort by sequence order (desc), then by start date (desc)
    data.sort((a, b) => {
      const aSeq = a.sequenceOrder ?? 0;
      const bSeq = b.sequenceOrder ?? 0;
      if (aSeq !== bSeq) {
        return bSeq - aSeq;
      }
      const aTime = a.startDateTime ? new Date(a.startDateTime).getTime() : 0;
      const bTime = b.startDateTime ? new Date(b.startDateTime).getTime() : 0;
      return bTime - aTime;
    });

    this.filteredSessions = data;
    this.updateSessionPageData();
  }

  private updateSessionPageData(): void {
    const total = this.filteredSessions.length;
    this.sessionTotalPages = Math.max(1, Math.ceil(total / this.sessionPageSize));
    if (this.sessionPage > this.sessionTotalPages) {
      this.sessionPage = this.sessionTotalPages;
    }

    const startIndex = (this.sessionPage - 1) * this.sessionPageSize;
    this.visibleSessions = this.filteredSessions.slice(startIndex, startIndex + this.sessionPageSize);
    this.sessionPageNumbers = Array.from({ length: this.sessionTotalPages }, (_, i) => i + 1);
  }

  onSessionPageSizeChange(value: string): void {
    const size = parseInt(value, 10);
    if (!isNaN(size) && size > 0) {
      this.sessionPageSize = size;
      this.sessionPage = 1;
      this.updateSessionPageData();
    }
  }

  goToSessionPage(page: number): void {
    if (page < 1 || page > this.sessionTotalPages) return;
    this.sessionPage = page;
    this.updateSessionPageData();
  }

  nextSessionPage(): void {
    if (this.sessionPage < this.sessionTotalPages) {
      this.sessionPage++;
      this.updateSessionPageData();
    }
  }

  prevSessionPage(): void {
    if (this.sessionPage > 1) {
      this.sessionPage--;
      this.updateSessionPageData();
    }
  }

  openSessionModal(programId?: number): void {
    if (programId) {
      this.sessionForm.patchValue({ programId });
    }
    this.showSessionModal = true;
  }

  closeSessionModal(): void {
    this.showSessionModal = false;
    this.sessionForm.reset();
    this.selectedSession = null;
  }

  saveSession(): void {
    if (!this.sessionForm.valid) {
      this.toastr.error('Please fill all required fields for the session');
      return;
    }

    const formValue = this.sessionForm.value;

    // Basic date validation
    const start = new Date(formValue.startDateTime);

    if (isNaN(start.getTime())) {
      this.toastr.error('Please provide a valid start date/time');
      return;
    }

    // Program date alignment – session cannot start before program start date
    const program = this.programs.find(p => p.id === formValue.programId);
    if (program && program.trainingDate) {
      const programStart = new Date(program.trainingDate);
      // Normalize to start of day
      const programStartDay = new Date(programStart.getFullYear(), programStart.getMonth(), programStart.getDate(), 0, 0, 0, 0);
      // Only enforce lower bound – allow sessions any time on/after program start
      if (start < programStartDay) {
        const programDateStr = programStartDay.toLocaleDateString();
        this.toastr.error(
          `Session start must be on or after the program start date (${programDateStr}).`
        );
        return;
      }
    }

    // Map form values to request, handling session link vs location
    const request: CreateTrainingSessionRequest = {
      programId: formValue.programId,
      startDateTime: formValue.startDateTime,
      // Backend requires endDateTime – use same as start when only a single point is provided
      endDateTime: formValue.startDateTime,
      location: (formValue.sessionType === 'Virtual' || formValue.sessionType === 'Hybrid')
        ? (formValue.sessionLink || formValue.location || '')
        : (formValue.location || ''),
      sessionType: formValue.sessionType,
      status: formValue.status,
      notes: formValue.notes,
      topic: formValue.topic || undefined,
      instructorId: this.currentUser?.id
    };

    this.loading = true;
    const save$ = this.selectedSession
      ? this.trainingService.updateSession(this.selectedSession.id, request)
      : this.trainingService.createSession(request);

    save$.subscribe({
      next: () => {
        this.toastr.success('Session saved successfully');
        this.closeSessionModal();
        this.loadSessions(request.programId);
        if (this.selectedProgram) {
          this.loadPrograms();
        }
      },
      error: () => {
        this.toastr.error('Failed to save session');
        this.loading = false;
      }
    });
  }

  editSession(session: TrainingSession): void {
    this.selectedSession = session;
    
    // Determine if session link should be populated from location
    // For Virtual or Hybrid sessions, the link is stored in location field
    const isVirtualOrHybrid = session.sessionType === 'Virtual' || session.sessionType === 'Hybrid';
    const sessionLinkValue = isVirtualOrHybrid ? (session.location || '') : '';
    const locationValue = isVirtualOrHybrid ? '' : (session.location || '');
    
    this.sessionForm.patchValue({
      programId: session.programId,
      startDateTime: new Date(session.startDateTime).toISOString().slice(0, 16),
      topic: session.topicName || '',
      location: locationValue,
      sessionType: session.sessionType,
      status: session.status,
      sessionLink: sessionLinkValue,
      notes: session.notes || ''
    });
    this.showSessionModal = true;
  }

  openDeleteSessionModal(session: TrainingSession): void {
    this.sessionPendingDelete = session;
    this.showDeleteSessionModal = true;
  }

  closeDeleteSessionModal(): void {
    this.showDeleteSessionModal = false;
    this.sessionPendingDelete = null;
  }

  deleteSession(): void {
    if (!this.sessionPendingDelete) {
      return;
    }

    this.loading = true;
    const session = this.sessionPendingDelete;
    this.trainingService.deleteSession(session.id).subscribe({
      next: () => {
        this.toastr.success('Session deleted successfully');
        this.closeDeleteSessionModal();
        this.loadSessions();
      },
      error: () => {
        this.toastr.error('Failed to delete session');
        this.loading = false;
      }
    });
  }

  // Materials
  loadMaterials(programId?: number): void {
    if (!programId) return;
    this.loading = true;
    this.trainingService.getMaterialsByProgram(programId).subscribe({
      next: (materials) => {
        this.materials = materials;
        this.applyMaterialFilters();
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load materials');
        this.loading = false;
      }
    });
  }

  openMaterialModal(programId?: number): void {
    this.materialForm.reset({
      programId: programId || this.selectedProgram?.id || null,
      title: '',
      description: '',
      materialType: 'Link',
      linkUrl: '',
      isRequired: false,
      displayOrder: null
    });
    this.showMaterialModal = true;
  }

  closeMaterialModal(): void {
    this.showMaterialModal = false;
    this.materialForm.reset();
    this.selectedMaterial = null;
  }

  saveMaterial(): void {
    if (!this.materialForm.valid) {
      this.toastr.error('Please fill all required material fields');
      return;
    }

    const formValue = this.materialForm.value;

    if (!formValue.programId) {
      this.toastr.error('Please select a program from the Programs tab to manage materials.');
      return;
    }

    const request: CreateTrainingMaterialRequest = {
      programId: formValue.programId,
      title: formValue.title,
      description: formValue.description,
      materialType: formValue.materialType || 'Link',
      filePath: formValue.linkUrl,
      isRequired: formValue.isRequired,
      displayOrder: formValue.displayOrder
    };

    this.loading = true;
    const save$ = this.selectedMaterial
      ? this.trainingService.updateMaterial(this.selectedMaterial.id, request)
      : this.trainingService.createMaterial(request);
    
    save$.subscribe({
      next: () => {
        this.toastr.success('Material saved successfully');
        this.closeMaterialModal();
        this.loadMaterials(request.programId);
      },
      error: () => {
        this.toastr.error('Failed to save material');
        this.loading = false;
      }
    });
  }

  editMaterial(material: TrainingMaterial): void {
    this.selectedMaterial = material;
    this.materialForm.patchValue({
      programId: material.programId,
      title: material.title,
      description: material.description,
      materialType: material.materialType,
      linkUrl: material.filePath,
      isRequired: material.isRequired,
      displayOrder: material.displayOrder
    });
    this.showMaterialModal = true;
  }

  private applyMaterialFilters(): void {
    if (!this.materials) {
      this.filteredMaterials = [];
      this.visibleMaterials = [];
      return;
    }

    const { materialType, status, fromDate, toDate, search, displayOrder } = this.materialFiltersForm.value;
    let data = [...this.materials];

    if (materialType) {
      const typeLower = (materialType as string).toLowerCase();
      data = data.filter(m => (m.materialType || '').toLowerCase() === typeLower);
    }

    if (status === 'REQUIRED') {
      data = data.filter(m => !!m.isRequired);
    } else if (status === 'OPTIONAL') {
      data = data.filter(m => !m.isRequired);
    }

    if (fromDate) {
      const from = new Date(fromDate);
      data = data.filter(m => new Date(m.createdAt) >= from);
    }

    if (toDate) {
      const to = new Date(toDate);
      to.setHours(23, 59, 59, 999);
      data = data.filter(m => new Date(m.createdAt) <= to);
    }

    if (search) {
      const term = (search as string).toLowerCase();
      data = data.filter(m =>
        (m.title || '').toLowerCase().includes(term) ||
        (m.description || '').toLowerCase().includes(term) ||
        (m.filePath || '').toLowerCase().includes(term)
      );
    }
    
    if (displayOrder !== null && displayOrder !== undefined && displayOrder !== '') {
      const order = Number(displayOrder);
      if (!Number.isNaN(order)) {
        data = data.filter(m => (m.displayOrder ?? null) === order);
      }
    }

    // Sort by display order (desc), then created date (desc)
    data.sort((a, b) => {
      const aOrder = a.displayOrder ?? 0;
      const bOrder = b.displayOrder ?? 0;
      if (aOrder !== bOrder) {
        return bOrder - aOrder;
      }
      const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return bTime - aTime;
    });

    this.filteredMaterials = data;
    this.updateMaterialPageData();
  }

  private updateMaterialPageData(): void {
    const total = this.filteredMaterials.length;
    this.materialTotalPages = Math.max(1, Math.ceil(total / this.materialPageSize));
    if (this.materialPage > this.materialTotalPages) {
      this.materialPage = this.materialTotalPages;
    }

    const startIndex = (this.materialPage - 1) * this.materialPageSize;
    this.visibleMaterials = this.filteredMaterials.slice(startIndex, startIndex + this.materialPageSize);
    this.materialPageNumbers = Array.from({ length: this.materialTotalPages }, (_, i) => i + 1);
  }

  onMaterialPageSizeChange(value: string): void {
    const size = parseInt(value, 10);
    if (!isNaN(size) && size > 0) {
      this.materialPageSize = size;
      this.materialPage = 1;
      this.updateMaterialPageData();
    }
  }

  goToMaterialPage(page: number): void {
    if (page < 1 || page > this.materialTotalPages) return;
    this.materialPage = page;
    this.updateMaterialPageData();
  }

  nextMaterialPage(): void {
    if (this.materialPage < this.materialTotalPages) {
      this.materialPage++;
      this.updateMaterialPageData();
    }
  }

  prevMaterialPage(): void {
    if (this.materialPage > 1) {
      this.materialPage--;
      this.updateMaterialPageData();
    }
  }

  openDeleteMaterialModal(material: TrainingMaterial): void {
    this.materialPendingDelete = material;
    this.showDeleteMaterialModal = true;
  }

  closeDeleteMaterialModal(): void {
    this.showDeleteMaterialModal = false;
    this.materialPendingDelete = null;
  }

  deleteMaterial(): void {
    if (!this.materialPendingDelete) {
      return;
    }

    this.loading = true;
    const material = this.materialPendingDelete;
    this.trainingService.deleteMaterial(material.id).subscribe({
      next: () => {
        this.toastr.success('Material deleted successfully');
        this.closeDeleteMaterialModal();
        this.loadMaterials(material.programId);
      },
      error: () => {
        this.toastr.error('Failed to delete material');
        this.loading = false;
      }
    });
  }

  openMaterialLink(material: TrainingMaterial): void {
    if (!material.filePath) {
      this.toastr.warning('No link has been configured for this material.');
      return;
    }
    window.open(material.filePath, '_blank');
  }

  openSessionLink(session: TrainingSession): void {
    if (session.location && (session.location.startsWith('http://') || session.location.startsWith('https://'))) {
      window.open(session.location, '_blank');
    } else {
      this.toastr.warning('No valid session link configured for this session.');
    }
  }

  // Reports helpers

  setReportsMode(mode: 'session' | 'dateRange'): void {
    if (this.reportsMode === mode) return;
    this.reportsMode = mode;
    this.singleSessionReport = null;
    this.groupedReport = null;
  }

  viewSingleSessionReport(): void {
    if (!this.sessionReportForm.valid) {
      this.toastr.error('Please select a session to view the report.');
      return;
    }
    const sessionId = this.sessionReportForm.value.sessionId;
    if (!sessionId) {
      this.toastr.error('Please select a valid session.');
      return;
    }
    this.reportsLoading = true;
    this.trainingService.getSingleSessionReport(sessionId).subscribe({
      next: (report) => {
        this.singleSessionReport = report;
        this.reportsLoading = false;
      },
      error: (err) => {
        this.toastr.error(err?.error?.message || 'Failed to load session report');
        this.reportsLoading = false;
      }
    });
  }

  downloadSingleSessionReportPdf(): void {
    if (!this.sessionReportForm.valid) {
      this.toastr.error('Please select a session before downloading the report.');
      return;
    }
    const sessionId = this.sessionReportForm.value.sessionId;
    if (!sessionId) return;

    this.trainingService.downloadSingleSessionReportPdf(sessionId).subscribe({
      next: (blob) => {
        this.saveBlob(blob, `session_report_${sessionId}.pdf`);
      },
      error: () => {
        this.toastr.error('Failed to download session report PDF');
      }
    });
  }

  viewDateRangeReport(): void {
    if (!this.dateRangeReportForm.valid) {
      this.toastr.error('Please select both From and To dates.');
      return;
    }
    const { from, to } = this.dateRangeReportForm.value;
    if (!from || !to) {
      this.toastr.error('Please select both From and To dates.');
      return;
    }
    if (new Date(from) > new Date(to)) {
      this.toastr.error('From date cannot be after To date.');
      return;
    }

    this.reportsLoading = true;
    this.trainingService.getDateBasedGroupedReport(from, to).subscribe({
      next: (report) => {
        this.groupedReport = report;
        this.reportsLoading = false;
      },
      error: (err) => {
        this.toastr.error(err?.error?.message || 'Failed to load date-range report');
        this.reportsLoading = false;
      }
    });
  }

  downloadDateRangeReportPdf(): void {
    if (!this.dateRangeReportForm.valid) {
      this.toastr.error('Please select both From and To dates.');
      return;
    }
    const { from, to } = this.dateRangeReportForm.value;
    if (!from || !to) return;

    this.trainingService.downloadDateBasedGroupedReportPdf(from, to).subscribe({
      next: (blob) => {
        this.saveBlob(blob, `training_grouped_report_${from}_to_${to}.pdf`);
      },
      error: () => {
        this.toastr.error('Failed to download date-range report PDF');
      }
    });
  }

  getSignatureImageSrc(data?: string | null): string | null {
    if (!data || !data.trim()) {
      return null;
    }
    const trimmed = data.trim();
    if (trimmed.startsWith('data:')) {
      return trimmed;
    }
    return `data:image/png;base64,${trimmed}`;
  }

  private saveBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  // Students
  loadStudents(): void {
    this.masterSetupService.getAllStudentTeachers(true, 'STUDENT').subscribe({
      next: (students) => {
        this.students = students;
      },
      error: () => {
        this.toastr.error('Failed to load students');
      }
    });
  }

  loadProgramStudents(programId: number): void {
    this.loading = true;
    this.trainingService.getProgramStudents(programId).subscribe({
      next: (enrollments) => {
        this.programStudents = enrollments;
        this.selectedProgramForStudents = this.programs.find(p => p.id === programId) || null;
        this.selectedStudentIds = new Set(enrollments.map(e => e.participantId));
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load program students');
        this.loading = false;
      }
    });
  }

  toggleStudentAssignment(studentId: number, event: any): void {
    if (event.target.checked) {
      this.selectedStudentIds.add(studentId);
    } else {
      this.selectedStudentIds.delete(studentId);
    }
  }

  saveStudentAssignments(): void {
    if (!this.selectedProgramForStudents) return;
    
    const studentIds = Array.from(this.selectedStudentIds);
    this.assignStudents(this.selectedProgramForStudents.id, studentIds);
  }

  assignStudents(programId: number, studentIds: number[]): void {
    this.loading = true;
    this.trainingService.assignStudents({ programId, studentIds }).subscribe({
      next: () => {
        this.toastr.success('Students assigned successfully');
        this.loadProgramStudents(programId);
      },
      error: () => {
        this.toastr.error('Failed to assign students');
        this.loading = false;
      }
    });
  }

  removeStudent(programId: number, studentId: number): void {
    if (confirm('Are you sure you want to remove this student from the program?')) {
      this.loading = true;
      this.trainingService.removeStudentFromProgram(programId, studentId).subscribe({
        next: () => {
          this.toastr.success('Student removed successfully');
          this.loadProgramStudents(programId);
        },
        error: () => {
          this.toastr.error('Failed to remove student');
          this.loading = false;
        }
      });
    }
  }

  // Attendance
  loadAttendance(sessionId: number): void {
    // Kept for potential future use where only a refresh of records is needed.
    // For building the attendance marking sheet (modal), see openAttendanceModal which
    // coordinates loading both enrollments and existing records together.
    this.trainingService.getAttendanceBySession(sessionId).subscribe({
      next: (attendance) => {
        this.attendanceRecords = attendance || [];
        this.selectedSessionForAttendance = this.sessions.find(s => s.id === sessionId) || null;
      },
      error: () => {
        this.toastr.error('Failed to load attendance');
        this.attendanceRecords = [];
      }
    });
  }

  openAttendanceModal(session: TrainingSession): void {
    this.selectedSessionForAttendance = session;

    if (!session.programId) {
      this.toastr.error('Program information is missing for this session.');
      return;
    }

    this.loading = true;

    // Step 1: load enrolled students for the session's program
    this.trainingService.getProgramStudents(session.programId).subscribe({
      next: (enrollments) => {
        this.attendanceStudents = enrollments || [];

        // Step 2: load any existing attendance records for this session
        this.trainingService.getAttendanceBySession(session.id).subscribe({
          next: (attendance) => {
            this.attendanceRecords = attendance || [];

            // Build attendance form with all enrolled students, seeding from existing records
            const formControls: any = {};
            this.attendanceStudents.forEach(enrollment => {
              const existingAttendance = this.attendanceRecords.find(
                a => a.participantId === enrollment.participantId
              );
              formControls[`student_${enrollment.participantId}`] = this.fb.group({
                participantId: [enrollment.participantId],
                status: [existingAttendance?.status || 'PRESENT'],
                notes: [existingAttendance?.notes || '']
              });
            });

            this.attendanceForm = this.fb.group(formControls);
            this.showAttendanceModal = true;
            this.loading = false;
          },
          error: () => {
            // If attendance records cannot be loaded, allow marking fresh attendance.
            this.toastr.error('Failed to load existing attendance records. You can still mark new attendance.');
            this.attendanceRecords = [];

            const formControls: any = {};
            this.attendanceStudents.forEach(enrollment => {
              formControls[`student_${enrollment.participantId}`] = this.fb.group({
                participantId: [enrollment.participantId],
                status: ['PRESENT'],
                notes: ['']
              });
            });

            this.attendanceForm = this.fb.group(formControls);
            this.showAttendanceModal = true;
            this.loading = false;
          }
        });
      },
      error: () => {
        this.toastr.error('Failed to load students for attendance');
        this.attendanceStudents = [];
        this.loading = false;
      }
    });
  }

  closeAttendanceModal(): void {
    this.showAttendanceModal = false;
    this.attendanceForm = this.fb.group({});
    this.selectedSessionForAttendance = null;
    this.attendanceStudents = [];
  }

  saveAttendance(): void {
    if (!this.selectedSessionForAttendance) return;
    
    const attendances: StudentAttendance[] = [];
    Object.keys(this.attendanceForm.controls).forEach(key => {
      const control = this.attendanceForm.get(key);
      if (control) {
        const value = control.value;
        attendances.push({
          participantId: value.participantId,
          status: value.status,
          notes: value.notes
        });
      }
    });

    const request: MarkAttendanceRequest = {
      sessionId: this.selectedSessionForAttendance.id,
      attendances
    };

    this.savingAttendance = true;
    this.trainingService.markAttendance(request).subscribe({
      next: () => {
        this.toastr.success('Attendance marked successfully');
        this.closeAttendanceModal();
        this.activeTab = 'attendance';
        this.savingAttendance = false;
      },
      error: (err) => {
        const msg = err?.error?.message || err?.error?.error || err?.message || 'Failed to mark attendance';
        this.toastr.error(msg);
        this.savingAttendance = false;
      }
    });
  }

  getStudentName(participantId: number): string {
    const enrollment = this.programStudents.find(e => e.participantId === participantId);
    return enrollment?.participantFullName || enrollment?.participantUsername || 'Unknown';
  }

  getAttendanceControl(participantId: number, field: string): any {
    const control = this.attendanceForm.get(`student_${participantId}`);
    return control ? control.get(field) : null;
  }

  // ---------- Overview analytics helpers ----------

  get totalPrograms(): number {
    return this.programs?.length || 0;
  }

  get totalStudents(): number {
    return this.students?.length || 0;
  }

  get totalSessions(): number {
    return this.sessions?.length || 0;
  }

  get upcomingSessions(): number {
    const now = new Date();
    return (this.sessions || []).filter(s => {
      const start = new Date(s.startDateTime);
      return !isNaN(start.getTime()) && start >= now;
    }).length;
  }

  get completedPrograms(): number {
    return (this.programs || []).filter(p => p.status === 'COMPLETED').length;
  }

  get ongoingPrograms(): number {
    return (this.programs || []).filter(p => p.status === 'ONGOING').length;
  }

  get programCompletionRate(): number {
    const total = this.totalPrograms;
    if (!total) return 0;
    return Math.round((this.completedPrograms / total) * 100);
  }

  get mostActivePrograms(): { title: string; sessions: number }[] {
    const list = (this.programs || []).map(p => ({
      title: p.trainingName || p.trainingNameName || p.title || 'Program',
      sessions: p.sessionsCount || 0
    }));
    return list
      .sort((a, b) => b.sessions - a.sessions)
      .slice(0, 5);
  }

  get maxSessionsPerProgram(): number {
    return this.mostActivePrograms.reduce((max, p) => Math.max(max, p.sessions), 0) || 1;
  }

  // ---------- Programs tab navigation helpers ----------

  goToSessionsForSelectedProgram(): void {
    if (!this.selectedProgram) {
      return;
    }
    const programId = this.selectedProgram.id;
    this.activeTab = 'sessions';
    this.loadSessions(programId);
    // Pre-filter by this program in the sessions list
    if (this.sessionFiltersForm) {
      this.sessionFiltersForm.patchValue({ programId }, { emitEvent: true });
    }
  }

  goToMaterialsForSelectedProgram(): void {
    if (!this.selectedProgram) {
      return;
    }
    const programId = this.selectedProgram.id;
    this.activeTab = 'materials';
    this.loadMaterials(programId);
  }

  goToAttendanceForSelectedProgram(): void {
    if (!this.selectedProgram) {
      return;
    }
    const programId = this.selectedProgram.id;
    this.activeTab = 'attendance';
    this.loadSessions(programId);
  }

  goToReportsForSelectedProgram(): void {
    if (!this.selectedProgram) {
      return;
    }
    const programId = this.selectedProgram.id;
    this.activeTab = 'reports';
    this.reportsMode = 'session';
    // Restrict initial report dropdown to sessions from this program
    this.loadSessions(programId);
  }

  switchTab(tab: typeof this.activeTab): void {
    this.activeTab = tab;
    if (tab === 'overview') {
      this.loadPrograms();
      this.loadSessions();
      this.loadStudents();
    } else if (tab === 'sessions') {
      this.loadSessions();
    } else if (tab === 'materials') {
      if (this.selectedProgram?.id) {
        this.loadMaterials(this.selectedProgram.id);
      } else if (this.programs.length > 0) {
        this.selectedProgram = this.programs[0];
        this.loadMaterials(this.selectedProgram.id);
      }
    } else if (tab === 'students') {
      this.loadStudents();
    } else if (tab === 'attendance') {
      this.loadSessions();
    } else if (tab === 'reports') {
      // Ensure latest sessions are available for reports
      this.loadSessions();
    }
  }
}
