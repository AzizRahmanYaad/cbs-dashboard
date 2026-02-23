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
  DateBasedGroupedReport,
  AttendeeSignature,
  SessionAttendanceReport
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
  activeTab: 'overview' | 'programs' | 'sessions' | 'materials' | 'attendance' | 'reports' = 'overview';
  readonly allTabs: ('overview' | 'programs' | 'sessions' | 'materials' | 'attendance' | 'reports')[] = 
    ['overview', 'programs', 'sessions', 'materials', 'attendance', 'reports'];

  /** CFO view-only: only Reports tab; no create/edit/delete. */
  get isCfoViewOnly(): boolean {
    return this.authService.hasAnyRole(['ROLE_CFO']) &&
      !this.authService.hasAnyRole(['ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']);
  }

  get tabs(): ('overview' | 'programs' | 'sessions' | 'materials' | 'attendance' | 'reports')[] {
    return this.isCfoViewOnly ? ['reports'] : this.allTabs;
  }

  /** Sessions for report session dropdown when CFO (from date-range report). */
  cfoReportSessions: SessionAttendanceReport[] = [];

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

  // Attendance Tab - Session-based flow
  selectedAttendanceSession: TrainingSession | null = null;
  attendanceSessionsFiltered: TrainingSession[] = [];
  attendanceSessionsVisible: TrainingSession[] = [];
  attendanceSessionPage = 1;
  attendanceSessionPageSize = 10;
  attendanceSessionPageSizeOptions: number[] = [5, 10, 20];
  attendanceSessionTotalPages = 1;
  attendanceSessionPageNumbers: number[] = [];
  attendanceFiltersForm: FormGroup;

  // Attendance Tab - Student list
  attendanceStudentsFiltered: Enrollment[] = [];
  attendanceStudentsVisible: Enrollment[] = [];
  attendanceStudentPage = 1;
  attendanceStudentPageSize = 10;
  attendanceStudentPageSizeOptions: number[] = [5, 10, 20, 50];
  attendanceStudentTotalPages = 1;
  attendanceStudentPageNumbers: number[] = [];
  attendanceStudentFiltersForm: FormGroup;
  attendanceTabLoading = false;
  
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
  Math = Math;

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

    this.attendanceFiltersForm = this.fb.group({
      programId: [null],
      status: [''],
      search: [''],
      fromDate: [''],
      toDate: ['']
    });

    this.attendanceStudentFiltersForm = this.fb.group({
      search: ['']
    });

    this.sessionFiltersForm.valueChanges.subscribe(() => {
      this.sessionPage = 1;
      this.applySessionFilters();
    });

    this.materialFiltersForm.valueChanges.subscribe(() => {
      this.materialPage = 1;
      this.applyMaterialFilters();
    });

    this.attendanceFiltersForm.valueChanges.subscribe(() => {
      this.attendanceSessionPage = 1;
      this.applyAttendanceSessionFilters();
    });

    this.attendanceStudentFiltersForm.valueChanges.subscribe(() => {
      this.attendanceStudentPage = 1;
      this.applyAttendanceStudentFilters();
    });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user?.id) {
        if (this.isCfoViewOnly) {
          this.activeTab = 'reports';
          this.loadCfoReportSessions();
        } else {
          this.loadPrograms();
          this.loadStudents();
          this.loadSessions();
        }
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
        if (this.activeTab === 'attendance') {
          this.applyAttendanceSessionFilters();
        }
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

    // Map form values to request: persist session link when provided, otherwise location (venue).
    // Backend has a single "location" field used for both venue and meeting URL.
    let normalizedLocation: string = (formValue.sessionLink ?? '').toString().trim();
    if (normalizedLocation) {
      if (!/^https?:\/\//i.test(normalizedLocation)) {
        normalizedLocation = 'https://' + normalizedLocation;
      }
    } else {
      normalizedLocation = (formValue.location ?? '').toString().trim();
    }

    const request: CreateTrainingSessionRequest = {
      programId: formValue.programId,
      startDateTime: formValue.startDateTime,
      endDateTime: formValue.startDateTime,
      location: normalizedLocation,
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

    const rawLocation = session.location || '';
    const typeLower = (session.sessionType || '').toLowerCase();
    const isVirtualOrHybrid =
      typeLower === 'virtual'.toLowerCase() ||
      typeLower === 'hybrid'.toLowerCase();

    // If the stored location already looks like a URL, prefer to treat it as the session link.
    const looksLikeUrl = !!rawLocation && /^https?:\/\//i.test(rawLocation);

    const sessionLinkValue = (isVirtualOrHybrid && rawLocation) ? rawLocation : (looksLikeUrl ? rawLocation : '');
    const locationValue = (isVirtualOrHybrid && looksLikeUrl) ? '' : rawLocation;

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

  // Attendance Tab - Session-based flow
  private applyAttendanceSessionFilters(): void {
    if (!this.sessions) {
      this.attendanceSessionsFiltered = [];
      this.attendanceSessionsVisible = [];
      return;
    }

    const { programId, status, search, fromDate, toDate } = this.attendanceFiltersForm?.value || {};
    let data = [...this.sessions];

    if (programId) {
      data = data.filter(s => s.programId === programId);
    }
    if (status) {
      data = data.filter(s => s.status === status);
    }
    if (search) {
      const term = (search as string).toLowerCase();
      data = data.filter(s =>
        (s.programTitle || '').toLowerCase().includes(term) ||
        (s.topicName || '').toLowerCase().includes(term) ||
        (s.location || '').toLowerCase().includes(term)
      );
    }
    if (fromDate) {
      const from = new Date(fromDate);
      data = data.filter(s => new Date(s.startDateTime) >= from);
    }
    if (toDate) {
      const to = new Date(toDate);
      to.setHours(23, 59, 59, 999);
      data = data.filter(s => new Date(s.startDateTime) <= to);
    }

    data.sort((a, b) => {
      const aTime = a.startDateTime ? new Date(a.startDateTime).getTime() : 0;
      const bTime = b.startDateTime ? new Date(b.startDateTime).getTime() : 0;
      return bTime - aTime;
    });

    this.attendanceSessionsFiltered = data;
    this.updateAttendanceSessionPageData();
  }

  private updateAttendanceSessionPageData(): void {
    const total = this.attendanceSessionsFiltered.length;
    this.attendanceSessionTotalPages = Math.max(1, Math.ceil(total / this.attendanceSessionPageSize));
    if (this.attendanceSessionPage > this.attendanceSessionTotalPages) {
      this.attendanceSessionPage = this.attendanceSessionTotalPages;
    }
    const startIndex = (this.attendanceSessionPage - 1) * this.attendanceSessionPageSize;
    this.attendanceSessionsVisible = this.attendanceSessionsFiltered.slice(startIndex, startIndex + this.attendanceSessionPageSize);
    this.attendanceSessionPageNumbers = Array.from({ length: this.attendanceSessionTotalPages }, (_, i) => i + 1);
  }

  onAttendanceSessionPageSizeChange(value: string): void {
    const size = parseInt(value, 10);
    if (!isNaN(size) && size > 0) {
      this.attendanceSessionPageSize = size;
      this.attendanceSessionPage = 1;
      this.updateAttendanceSessionPageData();
    }
  }

  goToAttendanceSessionPage(page: number): void {
    if (page < 1 || page > this.attendanceSessionTotalPages) return;
    this.attendanceSessionPage = page;
    this.updateAttendanceSessionPageData();
  }

  nextAttendanceSessionPage(): void {
    if (this.attendanceSessionPage < this.attendanceSessionTotalPages) {
      this.attendanceSessionPage++;
      this.updateAttendanceSessionPageData();
    }
  }

  prevAttendanceSessionPage(): void {
    if (this.attendanceSessionPage > 1) {
      this.attendanceSessionPage--;
      this.updateAttendanceSessionPageData();
    }
  }

  selectAttendanceSession(session: TrainingSession): void {
    this.selectedAttendanceSession = session;
    this.loadAttendanceForSession(session);
  }

  clearAttendanceSession(): void {
    this.selectedAttendanceSession = null;
    this.attendanceStudents = [];
    this.attendanceRecords = [];
    this.attendanceForm = this.fb.group({});
    this.attendanceStudentsFiltered = [];
    this.attendanceStudentsVisible = [];
    this.attendanceStudentPage = 1;
  }

  loadAttendanceForSession(session: TrainingSession): void {
    if (!session.programId) {
      this.toastr.error('Program information is missing for this session.');
      return;
    }

    this.attendanceTabLoading = true;

    this.trainingService.getProgramStudents(session.programId).subscribe({
      next: (enrollments) => {
        this.attendanceStudents = enrollments || [];
        this.trainingService.getAttendanceBySession(session.id).subscribe({
          next: (attendance) => {
            this.attendanceRecords = attendance || [];
            const formControls: Record<string, any> = {};
            this.attendanceStudents.forEach(enrollment => {
              const existing = this.attendanceRecords.find(a => a.participantId === enrollment.participantId);
              formControls[`student_${enrollment.participantId}`] = this.fb.group({
                participantId: [enrollment.participantId],
                status: [existing?.status || 'PRESENT'],
                notes: [existing?.notes || '']
              });
            });
            this.attendanceForm = this.fb.group(formControls);
            this.applyAttendanceStudentFilters();
            this.attendanceTabLoading = false;
          },
          error: () => {
            this.attendanceRecords = [];
            const formControls: Record<string, any> = {};
            this.attendanceStudents.forEach(enrollment => {
              formControls[`student_${enrollment.participantId}`] = this.fb.group({
                participantId: [enrollment.participantId],
                status: ['PRESENT'],
                notes: ['']
              });
            });
            this.attendanceForm = this.fb.group(formControls);
            this.applyAttendanceStudentFilters();
            this.attendanceTabLoading = false;
          }
        });
      },
      error: () => {
        this.toastr.error('Failed to load students for this session.');
        this.attendanceStudents = [];
        this.attendanceTabLoading = false;
      }
    });
  }

  private applyAttendanceStudentFilters(): void {
    if (!this.attendanceStudents) {
      this.attendanceStudentsFiltered = [];
      this.attendanceStudentsVisible = [];
      return;
    }

    const search = (this.attendanceStudentFiltersForm?.value?.search || '').trim().toLowerCase();
    let data = [...this.attendanceStudents];

    if (search) {
      data = data.filter(e =>
        (e.participantFullName || '').toLowerCase().includes(search) ||
        (e.participantUsername || '').toLowerCase().includes(search) ||
        (e.participantEmail || '').toLowerCase().includes(search)
      );
    }

    this.attendanceStudentsFiltered = data;
    this.updateAttendanceStudentPageData();
  }

  private updateAttendanceStudentPageData(): void {
    const total = this.attendanceStudentsFiltered.length;
    this.attendanceStudentTotalPages = Math.max(1, Math.ceil(total / this.attendanceStudentPageSize));
    if (this.attendanceStudentPage > this.attendanceStudentTotalPages) {
      this.attendanceStudentPage = this.attendanceStudentTotalPages;
    }
    const startIndex = (this.attendanceStudentPage - 1) * this.attendanceStudentPageSize;
    this.attendanceStudentsVisible = this.attendanceStudentsFiltered.slice(startIndex, startIndex + this.attendanceStudentPageSize);
    this.attendanceStudentPageNumbers = Array.from({ length: this.attendanceStudentTotalPages }, (_, i) => i + 1);
  }

  onAttendanceStudentPageSizeChange(value: string): void {
    const size = parseInt(value, 10);
    if (!isNaN(size) && size > 0) {
      this.attendanceStudentPageSize = size;
      this.attendanceStudentPage = 1;
      this.updateAttendanceStudentPageData();
    }
  }

  goToAttendanceStudentPage(page: number): void {
    if (page < 1 || page > this.attendanceStudentTotalPages) return;
    this.attendanceStudentPage = page;
    this.updateAttendanceStudentPageData();
  }

  nextAttendanceStudentPage(): void {
    if (this.attendanceStudentPage < this.attendanceStudentTotalPages) {
      this.attendanceStudentPage++;
      this.updateAttendanceStudentPageData();
    }
  }

  prevAttendanceStudentPage(): void {
    if (this.attendanceStudentPage > 1) {
      this.attendanceStudentPage--;
      this.updateAttendanceStudentPageData();
    }
  }

  getExistingAttendanceForStudent(participantId: number): Attendance | undefined {
    return this.attendanceRecords?.find(a => a.participantId === participantId);
  }

  saveAttendanceInline(): void {
    if (!this.selectedAttendanceSession) return;

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
      sessionId: this.selectedAttendanceSession.id,
      attendances
    };

    this.savingAttendance = true;
    this.trainingService.markAttendance(request).subscribe({
      next: () => {
        this.toastr.success('Attendance saved successfully');
        this.loadAttendanceForSession(this.selectedAttendanceSession!);
        this.savingAttendance = false;
      },
      error: (err) => {
        const msg = err?.error?.message || err?.error?.error || err?.message || 'Failed to mark attendance';
        this.toastr.error(msg);
        this.savingAttendance = false;
      }
    });
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
    // Fire-and-forget completion tracking for material engagement.
    if (material.id != null) {
      this.trainingService.confirmMaterialReview(material.id).subscribe({
        error: (err) => {
          // Do not block access to the resource if tracking fails.
          // Optionally log to console for debugging.
          console.warn('Failed to record material review for current user.', err);
        }
      });
    }
    window.open(material.filePath, '_blank');
  }

  getSessionUrl(session: TrainingSession): string | null {
    const raw = (session.location || '').trim();
    if (!raw) {
      return null;
    }
    if (raw.toLowerCase().startsWith('http://') || raw.toLowerCase().startsWith('https://')) {
      return raw;
    }
    // Allow stored values without protocol to still be opened as HTTPS links.
    return `https://${raw}`;
  }

  openSessionLink(session: TrainingSession): void {
    const url = this.getSessionUrl(session);
    if (url) {
      window.open(url, '_blank');
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

  /** Returns only attendees who have provided an e-signature (with displayable image data). */
  getSignaturesWithImage(signatures: AttendeeSignature[] | undefined): AttendeeSignature[] {
    if (!signatures?.length) return [];
    return signatures.filter(s => !!this.getSignatureImageSrc(s.signatureData));
  }

  /**
   * Returns the first available instructor e-signature for the currently
   * displayed report context (single-session takes precedence over date-range).
   * Used to visually surface the session's e-signature alongside on-screen reports.
   */
  get instructorReportSignature(): string | null {
    if (this.singleSessionReport?.instructorSignatureData) {
      return this.singleSessionReport.instructorSignatureData;
    }
    if (this.groupedReport?.sessionsByDate?.length) {
      const withSig = this.groupedReport.sessionsByDate.find(
        s => !!s.instructorSignatureData
      );
      return withSig?.instructorSignatureData || null;
    }
    return null;
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

  /**
   * Quickly approves an absence by marking the student's status as EXCUSED
   * for the current session. This is treated as an approved absence in
   * downstream reports and analytics.
   */
  approveAbsenceForParticipant(participantId: number): void {
    const statusControl = this.getAttendanceControl(participantId, 'status');
    if (!statusControl) {
      return;
    }
    statusControl.setValue('EXCUSED');
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

  /** Load session list for CFO report dropdown (from date-range report). */
  loadCfoReportSessions(): void {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 365);
    const fromStr = from.toISOString().slice(0, 10);
    const toStr = to.toISOString().slice(0, 10);
    this.trainingService.getDateBasedGroupedReport(fromStr, toStr).subscribe({
      next: (report) => {
        this.groupedReport = report;
        this.cfoReportSessions = report?.sessionsByDate ?? [];
      },
      error: () => { this.cfoReportSessions = []; }
    });
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
    } else if (tab === 'attendance') {
      this.loadSessions();
      this.applyAttendanceSessionFilters();
    } else if (tab === 'reports') {
      // Ensure latest sessions are available for reports
      this.loadSessions();
    }
  }
}
