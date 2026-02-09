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
  Attendance
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
