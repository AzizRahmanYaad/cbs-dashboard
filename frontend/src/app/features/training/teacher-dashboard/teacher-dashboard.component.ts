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
  MaterialType
} from '../../../core/models/training';
import { TrainingTopic, StudentTeacher } from '../../../core/models/master';
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
  activeTab: 'overview' | 'programs' | 'sessions' | 'materials' | 'topics' | 'students' | 'attendance' = 'overview';
  tabs: ('overview' | 'programs' | 'sessions' | 'materials' | 'topics' | 'students' | 'attendance')[] = 
    ['overview', 'programs', 'sessions', 'materials', 'topics', 'students', 'attendance'];
  
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
  
  // Topics
  topics: TrainingTopic[] = [];
  showTopicModal = false;
  topicForm: FormGroup;
  selectedTopic: TrainingTopic | null = null;
  
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
  
  loading = false;
  
  // Enums for template
  SessionStatus = SessionStatus;
  AttendanceStatus = AttendanceStatus;
  MaterialType = MaterialType;

  constructor() {
    this.sessionForm = this.fb.group({
      programId: [null, Validators.required],
      startDateTime: ['', Validators.required],
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
    
    this.topicForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
    
    this.attendanceForm = this.fb.group({});

    this.sessionFiltersForm = this.fb.group({
      programId: [null],
      status: [''],
      sessionType: [''],
      fromDate: [''],
      toDate: [''],
      search: ['']
    });

    this.materialFiltersForm = this.fb.group({
      materialType: [''],
      status: [''],
      fromDate: [''],
      toDate: [''],
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
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user?.id) {
        this.loadPrograms();
        this.loadTopics();
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

    const { programId, status, sessionType, fromDate, toDate, search } = this.sessionFiltersForm.value;

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
        (s.location || '').toLowerCase().includes(term) ||
        (s.sessionType || '').toLowerCase().includes(term) ||
        (s.status || '').toLowerCase().includes(term)
      );
    }

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

    // Program duration alignment
    const program = this.programs.find(p => p.id === formValue.programId);
    if (program && program.trainingDate) {
      const programStart = new Date(program.trainingDate);
      // Normalize to start of day
      const programStartDay = new Date(programStart.getFullYear(), programStart.getMonth(), programStart.getDate(), 0, 0, 0, 0);

      let programEndLimit: Date;
      if (program.durationHours && program.durationHours > 0) {
        programEndLimit = new Date(programStartDay.getTime() + program.durationHours * 60 * 60 * 1000);
      } else {
        // Default: same calendar day (24h window)
        programEndLimit = new Date(programStartDay.getTime() + 24 * 60 * 60 * 1000);
      }

      if (start < programStartDay || start > programEndLimit) {
        const programDateStr = programStartDay.toLocaleDateString();
        this.toastr.error(
          `Session start must be within the program timeline (starting ${programDateStr}${program.durationHours ? ' with duration ' + program.durationHours + ' hour(s)' : ''}).`
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

    const { materialType, status, fromDate, toDate, search } = this.materialFiltersForm.value;
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

  // Topics
  loadTopics(): void {
    this.loading = true;
    this.masterSetupService.getAllTrainingTopics(true).subscribe({
      next: (topics) => {
        this.topics = topics;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load topics');
        this.loading = false;
      }
    });
  }

  openTopicModal(topic?: TrainingTopic): void {
    this.selectedTopic = topic || null;
    if (topic) {
      this.topicForm.patchValue({
        name: topic.name,
        description: topic.description
      });
    } else {
      this.topicForm.reset();
    }
    this.showTopicModal = true;
  }

  closeTopicModal(): void {
    this.showTopicModal = false;
    this.topicForm.reset();
    this.selectedTopic = null;
  }

  saveTopic(): void {
    if (this.topicForm.valid) {
      this.loading = true;
      const request = this.topicForm.value;
      
      const save$ = this.selectedTopic
        ? this.masterSetupService.updateTrainingTopic(this.selectedTopic.id, request)
        : this.masterSetupService.createTrainingTopic(request);
      
      save$.subscribe({
        next: () => {
          this.toastr.success('Topic saved successfully');
          this.closeTopicModal();
          this.loadTopics();
        },
        error: () => {
          this.toastr.error('Failed to save topic');
          this.loading = false;
        }
      });
    }
  }

  deleteTopic(topic: TrainingTopic): void {
    if (confirm('Are you sure you want to delete this topic?')) {
      this.loading = true;
      this.masterSetupService.deleteTrainingTopic(topic.id).subscribe({
        next: () => {
          this.toastr.success('Topic deleted successfully');
          this.loadTopics();
        },
        error: () => {
          this.toastr.error('Failed to delete topic');
          this.loading = false;
        }
      });
    }
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
    this.loading = true;
    this.trainingService.getAttendanceBySession(sessionId).subscribe({
      next: (attendance) => {
        this.attendanceRecords = attendance;
        this.selectedSessionForAttendance = this.sessions.find(s => s.id === sessionId) || null;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load attendance');
        this.loading = false;
      }
    });
  }

  openAttendanceModal(session: TrainingSession): void {
    this.selectedSessionForAttendance = session;
    
    // Load enrollments for this session's program
    if (session.programId) {
      this.trainingService.getProgramStudents(session.programId).subscribe({
        next: (enrollments) => {
          this.attendanceStudents = enrollments;
          // Load existing attendance
          this.loadAttendance(session.id);
          
          // Build attendance form with all enrolled students
          const formControls: any = {};
          enrollments.forEach(enrollment => {
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
        },
        error: () => {
          this.toastr.error('Failed to load students for attendance');
        }
      });
    }
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

    this.loading = true;
    this.trainingService.markAttendance(request).subscribe({
      next: () => {
        this.toastr.success('Attendance marked successfully');
        this.closeAttendanceModal();
        this.loadAttendance(this.selectedSessionForAttendance!.id);
      },
      error: (err) => {
        const msg = err?.error?.message || err?.error?.error || err?.message || 'Failed to mark attendance';
        this.toastr.error(msg);
        this.loading = false;
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
    } else if (tab === 'topics') {
      this.loadTopics();
    } else if (tab === 'students') {
      this.loadStudents();
    } else if (tab === 'attendance') {
      this.loadSessions();
    }
  }
}
