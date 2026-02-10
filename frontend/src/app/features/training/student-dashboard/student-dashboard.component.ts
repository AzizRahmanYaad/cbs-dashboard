import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TrainingService } from '../../../core/services/training.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserProfileService } from '../../../core/services/user-profile.service';
import { ToastrService } from 'ngx-toastr';
import { 
  TrainingProgram, 
  TrainingSession, 
  TrainingMaterial,
  Enrollment,
  Attendance,
  MaterialType
} from '../../../core/models/training';
import { User } from '../../../core/models';
import { SignaturePadComponent } from '../shared/signature-pad/signature-pad.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SignaturePadComponent],
  templateUrl: './student-dashboard.component.html',
  styleUrls: ['./student-dashboard.component.scss']
})
export class StudentDashboardComponent implements OnInit {
  @ViewChild('signaturePad') signaturePadRef!: SignaturePadComponent;
  @ViewChild('ackSignaturePad') ackSignaturePadRef!: SignaturePadComponent;

  private trainingService = inject(TrainingService);
  private authService = inject(AuthService);
  private userProfileService = inject(UserProfileService);
  private toastr = inject(ToastrService);

  // Expose Math for template expressions (e.g., pagination ranges)
  Math = Math;

  currentUser: User | null = null;
  activeTab: 'overview' | 'programs' | 'sessions' | 'materials' | 'attendance' | 'signature' | 'progress' = 'overview';
  tabs: ('overview' | 'programs' | 'sessions' | 'materials' | 'attendance' | 'signature' | 'progress')[] = 
    ['overview', 'programs', 'sessions', 'materials', 'attendance', 'signature', 'progress'];
  
  // Programs
  programs: TrainingProgram[] = [];
  selectedProgram: TrainingProgram | null = null;
  
  // Sessions
  sessions: TrainingSession[] = [];
  programSessions: TrainingSession[] = [];
  
  // Materials
  materials: TrainingMaterial[] = [];
  
  // Enrollments
  enrollments: Enrollment[] = [];
  
  // Attendance
  attendanceRecords: Attendance[] = [];

  // E-Signature
  hasSignature = false;
  signatureLoading = false;

  // Acknowledge modal (Review & Sign for Absent/Excused)
  showAcknowledgeModal = false;
  selectedAttendanceForAck: Attendance | null = null;
  acknowledgeMaterials: TrainingMaterial[] = [];
  acknowledgeLoading = false;

  // Sessions: filters & pagination
  sessionFilter = {
    search: '',
    status: 'ALL' as 'ALL' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED',
    programId: null as number | null,
    sequenceOrder: null as number | null
  };
  sessionStatusOptions: Array<'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED'> = [
    'SCHEDULED',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
    'POSTPONED'
  ];
  sessionPageIndex = 0;
  sessionPageSize = 6;
  sessionPageSizeOptions = [6, 12, 24];

  // Materials: filters & pagination
  materialFilter = {
    search: '',
    materialType: 'ALL' as 'ALL' | MaterialType | string,
    isRequired: 'ALL' as 'ALL' | 'REQUIRED' | 'OPTIONAL',
    displayOrder: null as number | null
  };
  materialTypeOptions: string[] = [
    MaterialType.PDF,
    MaterialType.VIDEO,
    MaterialType.DOCUMENT,
    MaterialType.LINK,
    MaterialType.PRESENTATION
  ];
  materialPageIndex = 0;
  materialPageSize = 6;
  materialPageSizeOptions = [6, 12, 24];

  loading = false;

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user?.id) {
        this.loadPrograms();
        this.loadEnrollments();
        this.loadAttendance();
      }
    });
  }

  // Programs
  loadPrograms(): void {
    if (!this.currentUser?.id) return;
    this.loading = true;
    this.trainingService.getProgramsByStudent(this.currentUser.id).subscribe({
      next: (programs) => {
        this.programs = programs || [];
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
  }

  // Sessions
  loadSessions(programId?: number): void {
    this.loading = true;
    this.trainingService.getAllSessions(programId).subscribe({
      next: (sessions) => {
        if (programId) {
          this.programSessions = sessions || [];
        } else {
          // Load sessions for all enrolled programs
          this.sessions = sessions || [];
          // Filter to only show sessions for enrolled programs
          const enrolledProgramIds = this.programs.map(p => p.id);
          this.sessions = this.sessions.filter(s => enrolledProgramIds.includes(s.programId));
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading sessions:', err);
        this.toastr.error('Failed to load sessions');
        this.sessions = [];
        this.programSessions = [];
        this.loading = false;
      }
    });
  }

  // Materials
  loadMaterials(programId: number): void {
    this.loading = true;
    this.trainingService.getMaterialsByProgram(programId).subscribe({
      next: (materials) => {
        this.materials = materials || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading materials:', err);
        this.toastr.error('Failed to load materials');
        this.loading = false;
      }
    });
  }

  // Enrollments
  loadEnrollments(): void {
    if (!this.currentUser?.id) return;
    // Load enrollments through programs
    this.loadPrograms();
  }

  // Attendance
  loadAttendance(): void {
    if (!this.currentUser?.id) return;
    this.loading = true;
    this.trainingService.getAttendanceByParticipant(this.currentUser.id).subscribe({
      next: (attendance) => {
        this.attendanceRecords = attendance || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading attendance:', err);
        // Don't show error - attendance might not exist yet
        this.attendanceRecords = [];
        this.loading = false;
      }
    });
  }

  getProgramSessions(programId: number): TrainingSession[] {
    return this.sessions.filter(s => s.programId === programId);
  }

  getProgramMaterials(programId: number): TrainingMaterial[] {
    return this.materials.filter(m => m.programId === programId);
  }

  getEnrollmentStatus(programId: number): string {
    const enrollment = this.enrollments.find(e => e.programId === programId);
    return enrollment?.status || 'PENDING';
  }

  getAttendanceForSession(sessionId: number): Attendance | undefined {
    return this.attendanceRecords.find(a => a.sessionId === sessionId);
  }

  getAttendanceStatus(sessionId: number): string {
    const attendance = this.getAttendanceForSession(sessionId);
    return attendance?.status || 'NOT_MARKED';
  }

  // ---------- Sessions: filtering & pagination ----------

  get filteredSessions(): TrainingSession[] {
    let data = [...(this.sessions || [])];
    const { search, status, programId, sequenceOrder } = this.sessionFilter;

    if (programId) {
      data = data.filter(s => s.programId === programId);
    }
    if (status && status !== 'ALL') {
      data = data.filter(s => s.status === status);
    }
    if (sequenceOrder !== null && sequenceOrder !== undefined && sequenceOrder !== ('' as any)) {
      const seq = Number(sequenceOrder);
      if (!Number.isNaN(seq)) {
        data = data.filter(s => (s.sequenceOrder ?? null) === seq);
      }
    }
    if (search && search.trim()) {
      const term = search.toLowerCase();
      data = data.filter(s =>
        (s.programTitle || '').toLowerCase().includes(term) ||
        (s.topicName || '').toLowerCase().includes(term) ||
        (s.location || '').toLowerCase().includes(term)
      );
    }

    // Sort by sequence order (desc), then by start date (desc)
    return data.sort((a, b) => {
      const aSeq = a.sequenceOrder ?? 0;
      const bSeq = b.sequenceOrder ?? 0;
      if (aSeq !== bSeq) {
        return bSeq - aSeq;
      }
      const aTime = a.startDateTime ? new Date(a.startDateTime).getTime() : 0;
      const bTime = b.startDateTime ? new Date(b.startDateTime).getTime() : 0;
      return bTime - aTime;
    });
  }

  get pagedSessions(): TrainingSession[] {
    const start = this.sessionPageIndex * this.sessionPageSize;
    return this.filteredSessions.slice(start, start + this.sessionPageSize);
  }

  get totalSessionPages(): number {
    const total = this.filteredSessions.length;
    return total === 0 ? 1 : Math.ceil(total / this.sessionPageSize);
  }

  onSessionFilterChange(): void {
    this.sessionPageIndex = 0;
  }

  onSessionPageSizeChange(): void {
    this.sessionPageIndex = 0;
  }

  goToSessionPage(index: number): void {
    if (index < 0 || index >= this.totalSessionPages) return;
    this.sessionPageIndex = index;
  }

  changeSessionPage(delta: number): void {
    this.goToSessionPage(this.sessionPageIndex + delta);
  }

  onSessionClicked(session: TrainingSession): void {
    // Treat clicking a session as navigating to its learning content:
    // focus the related program and open its materials.
    const program = this.programs.find(p => p.id === session.programId) || null;
    if (program) {
      this.selectedProgram = program;
      this.activeTab = 'materials';
      this.loadMaterials(program.id);
    }
  }

  hasSessionLink(session: TrainingSession): boolean {
    return !!(session.location && (session.location.startsWith('http://') || session.location.startsWith('https://')));
  }

  openSessionLink(session: TrainingSession): void {
    if (this.hasSessionLink(session)) {
      window.open(session.location!, '_blank');
    }
  }

  // ---------- Materials: filtering & pagination ----------

  get filteredMaterials(): TrainingMaterial[] {
    let data = [...(this.materials || [])];

    if (this.selectedProgram) {
      data = data.filter(m => m.programId === this.selectedProgram!.id);
    }

    const { search, materialType, isRequired, displayOrder } = this.materialFilter;

    if (materialType && materialType !== 'ALL') {
      data = data.filter(m => (m.materialType || '') === materialType);
    }

    if (isRequired === 'REQUIRED') {
      data = data.filter(m => !!m.isRequired);
    } else if (isRequired === 'OPTIONAL') {
      data = data.filter(m => !m.isRequired);
    }

    if (displayOrder !== null && displayOrder !== undefined && displayOrder !== ('' as any)) {
      const order = Number(displayOrder);
      if (!Number.isNaN(order)) {
        data = data.filter(m => (m.displayOrder ?? null) === order);
      }
    }

    if (search && search.trim()) {
      const term = search.toLowerCase();
      data = data.filter(m =>
        m.title.toLowerCase().includes(term) ||
        (m.description || '').toLowerCase().includes(term) ||
        (m.materialType || '').toLowerCase().includes(term)
      );
    }

    // Sort by display order (desc) then created date (desc)
    return data.sort((a, b) => {
      const aOrder = a.displayOrder ?? 0;
      const bOrder = b.displayOrder ?? 0;
      if (aOrder !== bOrder) {
        return bOrder - aOrder;
      }
      const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return bTime - aTime;
    });
  }

  get pagedMaterials(): TrainingMaterial[] {
    const start = this.materialPageIndex * this.materialPageSize;
    return this.filteredMaterials.slice(start, start + this.materialPageSize);
  }

  get totalMaterialPages(): number {
    const total = this.filteredMaterials.length;
    return total === 0 ? 1 : Math.ceil(total / this.materialPageSize);
  }

  onMaterialFilterChange(): void {
    this.materialPageIndex = 0;
  }

  onMaterialPageSizeChange(): void {
    this.materialPageIndex = 0;
  }

  goToMaterialPage(index: number): void {
    if (index < 0 || index >= this.totalMaterialPages) return;
    this.materialPageIndex = index;
  }

  changeMaterialPage(delta: number): void {
    this.goToMaterialPage(this.materialPageIndex + delta);
  }

  // ---------- Overview helpers ----------

  get totalPrograms(): number {
    return this.programs?.length || 0;
  }

  get totalSessions(): number {
    return this.sessions?.length || 0;
  }

  get attendedSessions(): number {
    return (this.attendanceRecords || []).filter(a => a.status === 'PRESENT').length;
  }

  get attendanceRate(): number {
    const total = (this.attendanceRecords || []).length;
    if (!total) return 0;
    return Math.round((this.attendedSessions / total) * 100);
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ONGOING': 'bg-green-100 text-green-800',
      'COMPLETED': 'bg-blue-100 text-blue-800',
      'DRAFT': 'bg-gray-100 text-gray-800',
      'PUBLISHED': 'bg-purple-100 text-purple-800',
      'CANCELLED': 'bg-red-100 text-red-800',
      'SCHEDULED': 'bg-blue-100 text-blue-800',
      'POSTPONED': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-green-100 text-green-800',
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'IN_PROGRESS': 'bg-blue-100 text-blue-800',
      'PRESENT': 'bg-green-100 text-green-800',
      'ABSENT': 'bg-red-100 text-red-800',
      'LATE': 'bg-orange-100 text-orange-800',
      'EXCUSED': 'bg-gray-100 text-gray-800',
      'NOT_MARKED': 'bg-gray-100 text-gray-800'
    };
    return statusMap[status] || 'bg-gray-100 text-gray-800';
  }

  switchTab(tab: typeof this.activeTab): void {
    this.activeTab = tab;
    if (tab === 'overview') {
      this.loadPrograms();
      this.loadAttendance();
    } else if (tab === 'materials') {
      // When student opens Materials, automatically select a program and load its materials
      if (this.selectedProgram?.id) {
        this.loadMaterials(this.selectedProgram.id);
      } else if (this.programs.length > 0) {
        this.selectedProgram = this.programs[0];
        this.loadMaterials(this.selectedProgram.id);
      }
    } else if (tab === 'sessions') {
      this.loadSessions();
    } else if (tab === 'attendance') {
      this.loadAttendance();
      this.loadSessions(); // so we can resolve programId for acknowledge materials
    } else if (tab === 'signature') {
      this.loadSignatureStatus();
    } else if (tab === 'programs') {
      this.loadPrograms();
    }
  }

  loadSignatureStatus(): void {
    this.signatureLoading = true;
    this.userProfileService.getSignatureStatus().subscribe({
      next: (res) => {
        this.hasSignature = res.hasSignature ?? !!res.signatureData;
        this.signatureLoading = false;
      },
      error: () => {
        this.hasSignature = false;
        this.signatureLoading = false;
      }
    });
  }

  saveSignature(): void {
    if (!this.signaturePadRef || this.signaturePadRef.isEmpty()) {
      this.toastr.warning('Please draw your signature before saving.');
      return;
    }
    const data = this.signaturePadRef.getSignatureData();
    if (!data) return;
    this.signatureLoading = true;
    this.userProfileService.saveSignature(data).subscribe({
      next: () => {
        this.toastr.success('E-Signature saved successfully. It will be used for attendance and acknowledgments.');
        this.hasSignature = true;
        this.signatureLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to save signature');
        this.signatureLoading = false;
      }
    });
  }

  canAcknowledge(attendance: Attendance): boolean {
    return (attendance.status === 'ABSENT' || attendance.status === 'EXCUSED') && !attendance.signedAt;
  }

  openAcknowledgeModal(attendance: Attendance): void {
    this.selectedAttendanceForAck = attendance;
    this.acknowledgeMaterials = [];
    const session = this.sessions.find(s => s.id === attendance.sessionId);
    if (session?.programId) {
      this.trainingService.getMaterialsByProgram(session.programId).subscribe({
        next: (mats) => this.acknowledgeMaterials = mats || [],
        error: () => this.acknowledgeMaterials = []
      });
    }
    this.showAcknowledgeModal = true;
  }

  closeAcknowledgeModal(): void {
    this.showAcknowledgeModal = false;
    this.selectedAttendanceForAck = null;
    this.acknowledgeMaterials = [];
    this.ackSignaturePadRef?.clear();
  }

  submitAcknowledge(): void {
    if (!this.selectedAttendanceForAck || !this.ackSignaturePadRef) return;
    if (this.ackSignaturePadRef.isEmpty()) {
      this.toastr.warning('Please sign to confirm you have reviewed the session materials.');
      return;
    }
    const data = this.ackSignaturePadRef.getSignatureData();
    if (!data) return;
    this.acknowledgeLoading = true;
    this.trainingService.acknowledgeAttendance(this.selectedAttendanceForAck.id, data).subscribe({
      next: () => {
        this.toastr.success('Acknowledgment saved. Your signature has been recorded for audit.');
        this.closeAcknowledgeModal();
        this.loadAttendance();
        this.acknowledgeLoading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || err.message || 'Failed to submit acknowledgment');
        this.acknowledgeLoading = false;
      }
    });
  }

  openMaterialLink(material: TrainingMaterial): void {
    if (material.filePath) window.open(material.filePath, '_blank');
  }

  downloadMaterial(material: TrainingMaterial): void {
    if (material.filePath) {
      window.open(material.filePath, '_blank');
    } else {
      this.toastr.warning('File path not available for this material');
    }
  }
}
