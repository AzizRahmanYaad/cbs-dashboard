import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TrainingService } from '../../../core/services/training.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastrService } from 'ngx-toastr';
import { 
  TrainingProgram, 
  TrainingSession, 
  TrainingMaterial,
  Enrollment,
  Attendance
} from '../../../core/models/training';
import { User } from '../../../core/models';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './student-dashboard.component.html',
  styleUrls: ['./student-dashboard.component.scss']
})
export class StudentDashboardComponent implements OnInit {
  private trainingService = inject(TrainingService);
  private authService = inject(AuthService);
  private toastr = inject(ToastrService);

  currentUser: User | null = null;
  activeTab: 'programs' | 'sessions' | 'materials' | 'attendance' | 'progress' = 'programs';
  tabs: ('programs' | 'sessions' | 'materials' | 'attendance' | 'progress')[] = 
    ['programs', 'sessions', 'materials', 'attendance', 'progress'];
  
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
    if (tab === 'sessions') {
      // Load all sessions for enrolled programs
      this.loadSessions();
    } else if (tab === 'attendance') {
      this.loadAttendance();
    } else if (tab === 'programs') {
      this.loadPrograms();
    }
  }

  downloadMaterial(material: TrainingMaterial): void {
    if (material.filePath) {
      window.open(material.filePath, '_blank');
    } else {
      this.toastr.warning('File path not available for this material');
    }
  }
}
