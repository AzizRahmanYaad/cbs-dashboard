import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MasterSetupService } from '../../../core/services/master-setup.service';
import { TrainingService } from '../../../core/services/training.service';
import { AdminUserService } from '../../../core/services/admin-user.service';
import { ToastrService } from 'ngx-toastr';
import {
  StudentTeacherDto,
  TrainingTopic,
  TrainingCategory,
  TrainingName,
  TrainingModule,
  Department
} from '../../../core/models/master';
import { TrainingProgram, TrainingLevel, TrainingType, ExamType } from '../../../core/models/training';
import { User } from '../../../core/models';
import { timeout, catchError, of } from 'rxjs';

@Component({
  selector: 'app-training-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './training-admin-settings.component.html',
  styleUrls: ['./training-admin-settings.component.scss']
})
export class TrainingAdminSettingsComponent implements OnInit {
  private masterSetupService = inject(MasterSetupService);
  private trainingService = inject(TrainingService);
  private userService = inject(AdminUserService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);

  activeTab: 'overview' | 'programs' | 'students' | 'teachers' | 'assign-teachers' | 'assign-students' = 'overview';

  // Programs
  programs: TrainingProgram[] = [];
  selectedProgram: TrainingProgram | null = null;
  showProgramModal = false;
  programForm: FormGroup;

  users: User[] = [];

  // Students
  students: StudentTeacherDto[] = [];
  selectedStudent: StudentTeacherDto | null = null;
  showStudentModal = false;
  studentForm: FormGroup;
  studentPrograms: Map<number, TrainingProgram[]> = new Map();
  expandedStudentId: number | null = null;

  // Teachers
  teachers: StudentTeacherDto[] = [];
  selectedTeacher: StudentTeacherDto | null = null;
  showTeacherModal = false;
  teacherForm: FormGroup;
  teacherPrograms: Map<number, TrainingProgram[]> = new Map();
  expandedTeacherId: number | null = null;

  // Assign Teachers
  assignTeacherForm: FormGroup;

  // Assign Students
  assignStudentForm: FormGroup;
  selectedStudents: number[] = [];
  /** Student IDs already enrolled in the currently selected program (for Assign Students) */
  assignedStudentIdsForProgram: number[] = [];
  loadingAssignedStudents = false;

  loading = false;
  skeletonLoading = false;

  // Search and Filters
  searchTerm = '';
  statusFilter: string | null = null;
  filteredPrograms: TrainingProgram[] = [];
  filteredStudents: StudentTeacherDto[] = [];
  filteredTeachers: StudentTeacherDto[] = [];

  // Pagination
  currentPage = {
    programs: 1,
    students: 1,
    teachers: 1
  };
  itemsPerPage = 10;
  totalPages = {
    programs: 1,
    students: 1,
    teachers: 1
  };

  // Confirmation dialog
  showDeleteConfirm = false;
  itemToDelete: { type: 'program' | 'student' | 'teacher', item: any } | null = null;

  // Program view modal
  showProgramViewModal = false;
  viewProgram: TrainingProgram | null = null;

  // Master data for dropdowns
  trainingTopics: TrainingTopic[] = [];
  trainingCategories: TrainingCategory[] = [];
  trainingNames: TrainingName[] = [];
  trainingModules: TrainingModule[] = [];
  departments: Department[] = [];

  // Filtered lists for program form (only active items, sorted for display)
  get activeTrainingTopics(): TrainingTopic[] {
    return this.trainingTopics.filter(t => t.isActive);
  }

  /** Topics sorted by name for the program form dropdown */
  get activeTrainingTopicsSorted(): TrainingTopic[] {
    return [...this.activeTrainingTopics].sort((a, b) =>
      (a.name || '').localeCompare(b.name || '', undefined, { sensitivity: 'base' })
    );
  }

  get activeTrainingCategories(): TrainingCategory[] {
    return this.trainingCategories.filter(c => c.isActive);
  }

  get activeTrainingNames(): TrainingName[] {
    return this.trainingNames.filter(n => n.isActive);
  }

  get activeTrainingModules(): TrainingModule[] {
    return this.trainingModules.filter(m => m.isActive);
  }

  get activeDepartments(): Department[] {
    return this.departments.filter(d => d.isActive);
  }

  // Overview dashboard: summary stats
  get overviewProgramsCount(): number {
    return this.programs.length;
  }

  get overviewStudentsCount(): number {
    return this.students.length;
  }

  get overviewTeachersCount(): number {
    return this.teachers.length;
  }

  get overviewProgramsWithTeacher(): number {
    return this.programs.filter(p => p.instructorId != null).length;
  }

  get overviewProgramsWithStudents(): number {
    const programIdsWithStudents = new Set<number>();
    this.studentPrograms.forEach((progs) => {
      progs.forEach(p => programIdsWithStudents.add(p.id));
    });
    return programIdsWithStudents.size;
  }

  get overviewActiveStudents(): number {
    return this.students.filter(s => s.isActive).length;
  }

  get overviewActiveTeachers(): number {
    return this.teachers.filter(t => t.isActive).length;
  }

  /** For bar chart: programs count by department */
  get overviewProgramsByDepartment(): { label: string; value: number }[] {
    const map = new Map<string, number>();
    this.programs.forEach(p => {
      const name = p.departmentName || 'Unassigned';
      map.set(name, (map.get(name) || 0) + 1);
    });
    return Array.from(map.entries())
      .map(([label, value]) => ({ label, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8);
  }

  /** For bar chart: programs count by training type */
  get overviewProgramsByType(): { label: string; value: number }[] {
    const map = new Map<string, number>();
    this.programs.forEach(p => {
      const raw = p.trainingType || 'OTHER';
      const label = raw === 'ONLINE' ? 'Online' : raw === 'ON_JOB' ? 'On Job' : raw === 'ON_SITE' ? 'Both' : raw;
      map.set(label, (map.get(label) || 0) + 1);
    });
    return Array.from(map.entries())
      .map(([label, value]) => ({ label, value }))
      .sort((a, b) => b.value - a.value);
  }

  /** Max value for normalizing overview bar charts */
  get overviewChartMax(): number {
    const byDept = this.overviewProgramsByDepartment;
    const byType = this.overviewProgramsByType;
    const maxDept = byDept.length ? Math.max(...byDept.map(d => d.value)) : 1;
    const maxType = byType.length ? Math.max(...byType.map(d => d.value)) : 1;
    return Math.max(maxDept, maxType, 1);
  }

  // Add topic inline (in program modal)
  // (legacy support, no longer used in New Program UI but kept to avoid breaking references)
  showAddTopicInline = false;
  addTopicForm!: FormGroup;
  addingTopic = false;

  // Enums for template
  TrainingLevel = TrainingLevel;
  TrainingType = TrainingType;
  ExamType = ExamType;

  constructor() {
    const today = new Date().toISOString().split('T')[0];

    // Program form
    this.programForm = this.fb.group({
      // trainingTopicId kept for backend compatibility but no longer required/visible in the form
      trainingTopicId: [null],
      trainingNameId: [null], // Removed required validator to allow clearing/deleting
      trainingDate: [today, Validators.required],
      trainingLevel: [this.TrainingLevel.BASIC, Validators.required],
      trainingCategoryId: [null, Validators.required],
      departmentId: [null],
      trainingModuleId: [null],
      coordinatorId: [null],
      trainingType: [this.TrainingType.ONLINE, Validators.required],
      // Exam type is optional - if not set, treated as "Not applicable"
      examType: [null],
      hasArticleMaterial: [false],
      hasVideoMaterial: [false],
      hasSlideMaterial: [false]
    });

    // Student form
    this.studentForm = this.fb.group({
      userId: [null, Validators.required],
      employeeId: [''],
      departmentId: [null],
      phone: [''],
      isActive: [true]
    });

    // Teacher form
    this.teacherForm = this.fb.group({
      userId: [null, Validators.required],
      departmentId: [null],
      phone: [''],
      qualification: [''],
      specialization: [''],
      isActive: [true]
    });

    // Assign Teacher form
    this.assignTeacherForm = this.fb.group({
      programId: [null, Validators.required],
      teacherId: [null, Validators.required]
    });

    // Assign Students form
    this.assignStudentForm = this.fb.group({
      programId: [null, Validators.required]
    });

    // Add Topic form (for inline topic creation)
    this.addTopicForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadData();
    // Initialize filtered arrays
    this.filteredPrograms = [];
    this.filteredStudents = [];
    this.filteredTeachers = [];
    this.setupAssignmentFormSubscriptions();
  }

  /** When program changes in Assign Teacher/Student, update available lists and clear invalid selections. */
  private setupAssignmentFormSubscriptions(): void {
    this.assignTeacherForm.get('programId')?.valueChanges.subscribe(() => {
      this.assignTeacherForm.patchValue({ teacherId: null }, { emitEvent: false });
    });

    this.assignStudentForm.get('programId')?.valueChanges.subscribe(programId => {
      this.selectedStudents = [];
      if (programId) {
        this.loadingAssignedStudents = true;
        this.trainingService.getProgramStudents(Number(programId)).subscribe({
          next: (enrollments) => {
            this.assignedStudentIdsForProgram = enrollments.map(e => Number(e.participantId));
            this.loadingAssignedStudents = false;
          },
          error: () => {
            this.assignedStudentIdsForProgram = [];
            this.loadingAssignedStudents = false;
          }
        });
      } else {
        this.assignedStudentIdsForProgram = [];
      }
    });
  }

  /** Teachers available for assignment: only those not already assigned to any program. */
  get teachersForAssignDropdown(): StudentTeacherDto[] {
    const assignedTeacherIds = new Set(
      this.programs
        .filter(p => p.instructorId != null)
        .map(p => Number(p.instructorId))
    );
    return this.teachers.filter(t => !assignedTeacherIds.has(Number(t.userId)));
  }

  /** Students available for assignment: exclude those already enrolled in the selected program. */
  get studentsForAssignList(): StudentTeacherDto[] {
    const assignedIds = this.assignedStudentIdsForProgram;
    return this.students.filter(s => !assignedIds.includes(Number(s.userId)));
  }

  loadData(): void {
    this.loadPrograms();
    this.loadStudents();
    this.loadTeachers();
    this.loadUsers();
    this.loadMasterData();
  }

  loadMasterData(): void {
    this.loadTrainingTopics();
    this.loadTrainingCategories();
    this.loadTrainingNames();
    this.loadTrainingModules();
    this.loadDepartments();
  }

  loadTrainingTopics(): void {
    this.masterSetupService.getAllTrainingTopics(false).subscribe({
      next: (topics) => this.trainingTopics = topics || [],
      error: () => this.toastr.error('Failed to load training topics')
    });
  }

  toggleAddTopicInline(): void {
    this.showAddTopicInline = !this.showAddTopicInline;
    if (!this.showAddTopicInline) {
      this.addTopicForm.reset({ name: '', description: '' });
    }
  }

  cancelAddTopic(): void {
    this.showAddTopicInline = false;
    this.addTopicForm.reset({ name: '', description: '' });
  }

  addTopicInline(): void {
    if (this.addTopicForm.invalid) {
      this.addTopicForm.markAllAsTouched();
      this.toastr.error('Please enter topic name');
      return;
    }
    this.addingTopic = true;
    const request = { ...this.addTopicForm.value, isActive: true };
    this.masterSetupService.createTrainingTopic(request).subscribe({
      next: (created) => {
        this.toastr.success('Topic added. You can select it below.');
        this.loadTrainingTopics();
        this.programForm.patchValue({ trainingTopicId: created.id });
        this.showAddTopicInline = false;
        this.addTopicForm.reset({ name: '', description: '' });
        this.addingTopic = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to add topic');
        this.addingTopic = false;
      }
    });
  }

  loadTrainingCategories(): void {
    this.masterSetupService.getAllTrainingCategories(false).subscribe({
      next: (categories) => this.trainingCategories = categories,
      error: () => this.toastr.error('Failed to load training categories')
    });
  }

  loadTrainingNames(): void {
    this.masterSetupService.getAllTrainingNames(false).subscribe({
      next: (names) => this.trainingNames = names,
      error: () => this.toastr.error('Failed to load training names')
    });
  }

  loadTrainingModules(): void {
    this.masterSetupService.getAllTrainingModules(false).subscribe({
      next: (modules) => this.trainingModules = modules || [],
      error: () => this.toastr.error('Failed to load training modules')
    });
  }

  loadDepartments(): void {
    this.masterSetupService.getAllDepartments(false).subscribe({
      next: (departments) => this.departments = departments || [],
      error: () => this.toastr.error('Failed to load departments')
    });
  }

  loadPrograms(): void {
    this.skeletonLoading = true;
    this.trainingService.getAllPrograms().subscribe({
      next: (programs) => {
        this.programs = programs || [];
        this.applyFilters();
        this.skeletonLoading = false;
      },
      error: (err) => {
        this.skeletonLoading = false;
        this.toastr.error('Failed to load programs: ' + (err.error?.message || err.message || 'Unknown error'));
        this.programs = [];
        this.filteredPrograms = [];
      }
    });
  }

  loadStudents(): void {
    this.skeletonLoading = true;
    this.masterSetupService.getAllStudentTeachers(false, 'STUDENT').subscribe({
      next: (students) => {
        this.students = students;
        this.applyFilters();
        // Load programs for each student
        students.forEach(student => {
          if (student.userId) {
            this.loadStudentPrograms(student.userId);
          }
        });
        this.skeletonLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load students');
        this.skeletonLoading = false;
      }
    });
  }

  loadTeachers(): void {
    this.skeletonLoading = true;
    this.masterSetupService.getAllStudentTeachers(false, 'TEACHER').subscribe({
      next: (teachers) => {
        this.teachers = teachers;
        this.applyFilters();
        // Load programs for each teacher
        teachers.forEach(teacher => {
          if (teacher.userId) {
            this.loadTeacherPrograms(teacher.userId);
          }
        });
        this.skeletonLoading = false;
      },
      error: () => {
        this.toastr.error('Failed to load teachers');
        this.skeletonLoading = false;
      }
    });
  }

  loadStudentPrograms(studentId: number): void {
    this.trainingService.getProgramsByStudent(studentId).subscribe({
      next: (programs) => {
        this.studentPrograms.set(studentId, programs);
      },
      error: () => {
        // Silently fail - student might not have programs yet
        this.studentPrograms.set(studentId, []);
      }
    });
  }

  loadTeacherPrograms(teacherId: number): void {
    this.trainingService.getProgramsByInstructor(teacherId).subscribe({
      next: (programs) => {
        this.teacherPrograms.set(teacherId, programs);
      },
      error: () => {
        // Silently fail - teacher might not have programs yet
        this.teacherPrograms.set(teacherId, []);
      }
    });
  }

  toggleStudentPrograms(studentId: number): void {
    this.expandedStudentId = this.expandedStudentId === studentId ? null : studentId;
  }

  toggleTeacherPrograms(teacherId: number): void {
    this.expandedTeacherId = this.expandedTeacherId === teacherId ? null : teacherId;
  }

  getStudentPrograms(studentId: number): TrainingProgram[] {
    return this.studentPrograms.get(studentId) || [];
  }

  getTeacherPrograms(teacherId: number): TrainingProgram[] {
    return this.teacherPrograms.get(teacherId) || [];
  }

  loadUsers(): void {
    // Only users not yet assigned as student or teacher (and not admin) — for create student/teacher dropdowns
    this.userService.getAvailableUsersForTraining().subscribe({
      next: (users) => {
        this.users = users || [];
      },
      error: () => this.toastr.error('Failed to load users')
    });
  }

  // Program methods
  openProgramModal(program?: TrainingProgram): void {
    if (program) {
      this.selectedProgram = program;
      const trainingDate = program.trainingDate ? program.trainingDate.split('T')[0] : null;
      this.programForm.patchValue({
        trainingTopicId: program.trainingTopicId,
        trainingNameId: program.trainingNameId || null,
        trainingDate: trainingDate,
        trainingLevel: program.trainingLevel,
        trainingCategoryId: program.trainingCategoryId,
        departmentId: program.departmentId || null,
        trainingModuleId: program.trainingModuleId || null,
        trainingType: program.trainingType,
        examType: program.examType,
        hasArticleMaterial: program.hasArticleMaterial || false,
        hasVideoMaterial: program.hasVideoMaterial || false,
        hasSlideMaterial: program.hasSlideMaterial || false
      });
    } else {
      this.selectedProgram = null;
      this.programForm.reset({
        trainingDate: new Date().toISOString().split('T')[0],
        trainingLevel: this.TrainingLevel.BASIC,
        departmentId: null,
        trainingModuleId: null,
        trainingType: this.TrainingType.ONLINE,
        examType: null,
        hasArticleMaterial: false,
        hasVideoMaterial: false,
        hasSlideMaterial: false
      });
    }
    this.showProgramModal = true;
    this.onModalOpen();
  }

  closeProgramModal(): void {
    this.showProgramModal = false;
    this.selectedProgram = null;
    this.showAddTopicInline = false;
    if (this.addTopicForm) {
      this.addTopicForm.reset({ name: '', description: '' });
    }
  }

  saveProgram(): void {
    if (this.programForm.invalid) {
      this.toastr.error('Please fill all required fields');
      Object.keys(this.programForm.controls).forEach(key => {
        const control = this.programForm.get(key);
        if (control && control.invalid) {
          control.markAsTouched();
        }
      });
      return;
    }

    this.loading = true;
    const formValue = this.programForm.value;

    // Convert enum values to strings if they contain dots
    let trainingLevel = formValue.trainingLevel;
    if (trainingLevel && typeof trainingLevel === 'string' && trainingLevel.includes('.')) {
      trainingLevel = trainingLevel.split('.').pop();
    }

    let trainingType = formValue.trainingType;
    if (trainingType && typeof trainingType === 'string' && trainingType.includes('.')) {
      trainingType = trainingType.split('.').pop();
    }

    let examType = formValue.examType;
    if (examType && typeof examType === 'string' && examType.includes('.')) {
      examType = examType.split('.').pop();
    }

    // Resolve training name from master list
    const selectedTrainingName = this.trainingNames.find(n => n.id === formValue.trainingNameId);

    // Ensure title is set - use training name (master) or a default
    const title = selectedTrainingName?.name?.trim() || 'Training Program';

    const request: any = {
      title: title,
      trainingTopicId: formValue.trainingTopicId || null,
      trainingNameId: formValue.trainingNameId || null,
      trainingName: selectedTrainingName?.name || null,
      trainingDate: formValue.trainingDate || null,
      trainingLevel: trainingLevel || null,
      trainingCategoryId: formValue.trainingCategoryId || null,
      departmentId: formValue.departmentId || null,
      trainingModuleId: formValue.trainingModuleId || null,
      trainingType: trainingType || null,
      examType: examType || null,
      hasArticleMaterial: formValue.hasArticleMaterial || false,
      hasVideoMaterial: formValue.hasVideoMaterial || false,
      hasSlideMaterial: formValue.hasSlideMaterial || false,
      status: 'DRAFT'
    };

    // Only include coordinatorId if it has a value (it's optional)
    if (formValue.coordinatorId) {
      request.coordinatorId = formValue.coordinatorId;
    }

    const cleanedRequest: any = {};
    Object.keys(request).forEach(key => {
      const value = request[key];
      if (value !== null && value !== undefined) {
        cleanedRequest[key] = value;
      } else if (value === false) {
        cleanedRequest[key] = false;
      } else if (key === 'trainingNameId' || key === 'trainingName') {
        cleanedRequest[key] = null;
      }
    });

    const operation = this.selectedProgram
      ? this.trainingService.updateProgram(this.selectedProgram.id, cleanedRequest)
      : this.trainingService.createProgram(cleanedRequest);

    operation.pipe(
      timeout(30000),
      catchError((err) => {
        let errorMessage = 'Failed to save program';
        if (err.name === 'TimeoutError') {
          errorMessage = 'Request timed out. Please try again.';
        } else if (err.error) {
          if (err.error.message) {
            errorMessage = err.error.message;
          } else if (typeof err.error === 'string') {
            errorMessage = err.error;
          } else if (err.error.error) {
            errorMessage = err.error.error;
          } else if (Array.isArray(err.error) && err.error.length > 0) {
            errorMessage = err.error.map((e: any) => e.message || e).join(', ');
          }
        } else if (err.message) {
          errorMessage = err.message;
        }
        this.toastr.error(errorMessage);
        this.loading = false;
        return of(null);
      })
    ).subscribe({
      next: (response) => {
        if (response) {
          this.toastr.success(`Program ${this.selectedProgram ? 'updated' : 'created'} successfully`);
          this.loadPrograms();
          this.closeProgramModal();
        }
        this.loading = false;
      },
      error: () => {
        this.toastr.error('An unexpected error occurred');
        this.loading = false;
      }
    });
  }

  deleteProgram(program: TrainingProgram): void {
    this.itemToDelete = { type: 'program', item: program };
    this.showDeleteConfirm = true;
  }

  confirmDelete(): void {
    if (!this.itemToDelete) return;

    this.loading = true;
    const { type, item } = this.itemToDelete;

    if (type === 'program') {
      const programId = Number(item.id);
      if (!programId || isNaN(programId)) {
        this.toastr.error('Invalid program ID');
        this.loading = false;
        this.cancelDelete();
        return;
      }

      this.trainingService.deleteProgram(programId).pipe(
        timeout(30000),
        catchError((err) => {
          let errorMessage = 'Failed to delete program';
          if (err.name === 'TimeoutError') {
            errorMessage = 'Request timed out. Please try again.';
          } else if (err.error?.message) {
            errorMessage = err.error.message;
          } else if (err.message) {
            errorMessage = err.message;
          }
          this.toastr.error(errorMessage);
          this.loading = false;
          this.cancelDelete();
          return of(null);
        })
      ).subscribe({
        next: () => {
          this.toastr.success('Program deleted successfully');
          this.loadPrograms();
          this.loading = false;
          this.cancelDelete();
        },
        error: () => {
          this.toastr.error('An unexpected error occurred');
          this.loading = false;
          this.cancelDelete();
        }
      });
    } else if (type === 'student') {
      this.masterSetupService.deleteStudentTeacher(item.id).subscribe({
        next: () => {
          this.toastr.success('Student deleted successfully');
          this.loadStudents();
          this.loadUsers();
          this.loading = false;
          this.cancelDelete();
        },
        error: () => {
          this.toastr.error('Failed to delete student');
          this.loading = false;
          this.cancelDelete();
        }
      });
    } else if (type === 'teacher') {
      this.masterSetupService.deleteStudentTeacher(item.id).subscribe({
        next: () => {
          this.toastr.success('Teacher deleted successfully');
          this.loadTeachers();
          this.loadUsers();
          this.loading = false;
          this.cancelDelete();
        },
        error: () => {
          this.toastr.error('Failed to delete teacher');
          this.loading = false;
          this.cancelDelete();
        }
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.itemToDelete = null;
  }

  getDeleteMessage(): string {
    if (!this.itemToDelete) return '';
    const { type, item } = this.itemToDelete;
    const name = type === 'program'
      ? (item.title || item.trainingName || 'this program')
      : (item.username || item.fullName || `this ${type}`);
    return `Are you sure you want to delete "${name}"? This action cannot be undone.`;
  }

  // Program view methods
  openProgramView(program: TrainingProgram): void {
    this.viewProgram = program;
    this.showProgramViewModal = true;
  }

  closeProgramView(): void {
    this.showProgramViewModal = false;
    this.viewProgram = null;
  }

  // Student methods
  openStudentModal(student?: StudentTeacherDto): void {
    if (student) {
      this.selectedStudent = student;
      const department = this.departments.find(d => d.name === student.department);
      this.studentForm.patchValue({
        userId: student.userId,
        employeeId: student.employeeId,
        departmentId: department?.id || null,
        phone: student.phone,
        isActive: student.isActive
      });
    } else {
      this.selectedStudent = null;
      this.studentForm.reset({ isActive: true });
    }
    this.showStudentModal = true;
    this.onModalOpen();
  }

  closeStudentModal(): void {
    this.showStudentModal = false;
    this.selectedStudent = null;
  }

  saveStudent(): void {
    if (this.studentForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const formValue = this.studentForm.value;
    const selectedDepartment = this.departments.find(d => d.id === formValue.departmentId);
    const request: any = {
      userId: formValue.userId,
      type: 'STUDENT',
      employeeId: formValue.employeeId || null,
      studentId: this.selectedStudent?.studentId || null,
      department: selectedDepartment?.name || null,
      phone: formValue.phone || null,
      isActive: formValue.isActive !== undefined ? formValue.isActive : true
    };

    const operation = this.selectedStudent
      ? this.masterSetupService.updateStudentTeacher(this.selectedStudent.id, request)
      : this.masterSetupService.createStudentTeacher(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Student ${this.selectedStudent ? 'updated' : 'created'} successfully`);
        this.loadStudents();
        this.loadUsers();
        this.loadPrograms();
        this.closeStudentModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save student');
        this.loading = false;
      }
    });
  }

  deleteStudent(student: StudentTeacherDto): void {
    this.itemToDelete = { type: 'student', item: student };
    this.showDeleteConfirm = true;
  }

  // Teacher methods
  openTeacherModal(teacher?: StudentTeacherDto): void {
    if (teacher) {
      this.selectedTeacher = teacher;
      const department = teacher.department && teacher.department !== 'Not Applicable'
        ? this.departments.find(d => d.name === teacher.department)
        : null;
      this.teacherForm.patchValue({
        userId: teacher.userId,
        departmentId: department?.id || null,
        phone: teacher.phone,
        qualification: teacher.qualification,
        specialization: teacher.specialization,
        isActive: teacher.isActive
      });
    } else {
      this.selectedTeacher = null;
      this.teacherForm.reset({ isActive: true, departmentId: null });
    }
    this.showTeacherModal = true;
    this.onModalOpen();
  }

  closeTeacherModal(): void {
    this.showTeacherModal = false;
    this.selectedTeacher = null;
  }

  saveTeacher(): void {
    if (this.teacherForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const formValue = this.teacherForm.value;
    const selectedDepartment = formValue.departmentId
      ? this.departments.find(d => d.id === formValue.departmentId)
      : null;
    const request: any = {
      userId: formValue.userId,
      type: 'TEACHER',
      employeeId: this.selectedTeacher?.employeeId || null,
      department: selectedDepartment?.name || (formValue.departmentId === null ? 'Not Applicable' : null),
      phone: formValue.phone || null,
      qualification: formValue.qualification || null,
      specialization: formValue.specialization || null,
      isActive: formValue.isActive !== undefined ? formValue.isActive : true
    };

    const operation = this.selectedTeacher
      ? this.masterSetupService.updateStudentTeacher(this.selectedTeacher.id, request)
      : this.masterSetupService.createStudentTeacher(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Teacher ${this.selectedTeacher ? 'updated' : 'created'} successfully`);
        this.loadTeachers();
        this.loadUsers();
        this.loadPrograms();
        this.closeTeacherModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save teacher');
        this.loading = false;
      }
    });
  }

  deleteTeacher(teacher: StudentTeacherDto): void {
    this.itemToDelete = { type: 'teacher', item: teacher };
    this.showDeleteConfirm = true;
  }

  // Assign Teacher
  assignTeacher(): void {
    if (this.assignTeacherForm.invalid) {
      this.toastr.error('Please select a program and teacher');
      return;
    }

    this.loading = true;
    this.trainingService.assignTeacher(this.assignTeacherForm.value).subscribe({
      next: () => {
        this.toastr.success('Teacher assigned successfully');
        this.assignTeacherForm.reset();
        this.loadPrograms();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to assign teacher');
        this.loading = false;
      }
    });
  }

  // Assign Students
  toggleStudentSelection(studentId: number): void {
    const index = this.selectedStudents.indexOf(studentId);
    if (index > -1) {
      this.selectedStudents.splice(index, 1);
    } else {
      this.selectedStudents.push(studentId);
    }
  }

  assignStudents(): void {
    if (this.assignStudentForm.invalid || this.selectedStudents.length === 0) {
      this.toastr.error('Please select a program and at least one student');
      return;
    }

    this.loading = true;
    const request = {
      programId: this.assignStudentForm.value.programId,
      studentIds: this.selectedStudents
    };

    this.trainingService.assignStudents(request).subscribe({
      next: () => {
        this.toastr.success('Students assigned successfully');
        this.assignStudentForm.reset();
        this.selectedStudents = [];
        this.loadPrograms();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to assign students');
        this.loading = false;
      }
    });
  }

  setActiveTab(tab: 'overview' | 'programs' | 'students' | 'teachers' | 'assign-teachers' | 'assign-students'): void {
    this.activeTab = tab;
    this.currentPage = { programs: 1, students: 1, teachers: 1 };
    this.searchTerm = '';
    this.statusFilter = null;

    // Always refresh lists when switching tabs so newly created users, students,
    // and teachers appear without a full page reload.
    if (tab === 'overview') {
      this.loadPrograms();
      this.loadStudents();
      this.loadTeachers();
    } else if (tab === 'programs') {
      this.loadPrograms();
    } else if (tab === 'students') {
      this.loadStudents();
      this.loadUsers();
    } else if (tab === 'teachers') {
      this.loadTeachers();
      this.loadUsers();
    } else {
      // For assignment tabs we still want up-to-date programs and people
      this.loadPrograms();
      this.loadStudents();
      this.loadTeachers();
      this.loadUsers();
    }

    this.applyFilters();
  }

  // Search and Filter Methods
  onSearchChange(): void {
    this.currentPage = { programs: 1, students: 1, teachers: 1 };
    this.applyFilters();
  }

  onFilterChange(): void {
    this.currentPage = { programs: 1, students: 1, teachers: 1 };
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = null;
    this.currentPage = { programs: 1, students: 1, teachers: 1 };
    this.applyFilters();
  }

  applyFilters(): void {
    if (this.activeTab === 'programs') {
      this.filteredPrograms = this.programs.filter(program => {
        const matchesSearch = !this.searchTerm ||
          (program.title || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (program.trainingName || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (program.trainingCategoryName || '').toLowerCase().includes(this.searchTerm.toLowerCase());

        const matchesStatus =
          !this.statusFilter ||
          this.statusFilter.startsWith('DEPT:') ||
          this.statusFilter.startsWith('MOD:');

        return matchesSearch && matchesStatus;
      });
      this.totalPages.programs = Math.ceil(this.filteredPrograms.length / this.itemsPerPage) || 1;
    } else if (this.activeTab === 'students') {
      this.filteredStudents = this.students.filter(student => {
        const matchesSearch = !this.searchTerm ||
          (student.username || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (student.fullName || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (student.email || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (student.studentId || '').toLowerCase().includes(this.searchTerm.toLowerCase());

        const matchesStatus = !this.statusFilter ||
          (this.statusFilter === 'active' && student.isActive) ||
          (this.statusFilter === 'inactive' && !student.isActive);

        return matchesSearch && matchesStatus;
      });
      this.totalPages.students = Math.ceil(this.filteredStudents.length / this.itemsPerPage) || 1;
    } else if (this.activeTab === 'teachers') {
      this.filteredTeachers = this.teachers.filter(teacher => {
        const matchesSearch = !this.searchTerm ||
          (teacher.username || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (teacher.fullName || '').toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          (teacher.email || '').toLowerCase().includes(this.searchTerm.toLowerCase());

        const matchesStatus = !this.statusFilter ||
          (this.statusFilter === 'active' && teacher.isActive) ||
          (this.statusFilter === 'inactive' && !teacher.isActive);

        return matchesSearch && matchesStatus;
      });
      this.totalPages.teachers = Math.ceil(this.filteredTeachers.length / this.itemsPerPage) || 1;
    }
  }

  // Pagination Methods
  getPaginatedItems(items: any[], type: 'programs' | 'students' | 'teachers'): any[] {
    const start = (this.currentPage[type] - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return items.slice(start, end);
  }

  changePage(page: number, type: 'programs' | 'students' | 'teachers'): void {
    if (page >= 1 && page <= this.totalPages[type]) {
      this.currentPage[type] = page;
      const tableContainer = document.querySelector('.table-container');
      if (tableContainer) {
        tableContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }
  }

  changePageSize(size: number): void {
    this.itemsPerPage = size;
    this.currentPage = { programs: 1, students: 1, teachers: 1 };
    this.applyFilters();
  }

  getTotalRecords(type: 'programs' | 'students' | 'teachers'): number {
    if (type === 'programs') return this.filteredPrograms.length;
    if (type === 'students') return this.filteredStudents.length;
    if (type === 'teachers') return this.filteredTeachers.length;
    return 0;
  }

  getPageNumbers(type: 'programs' | 'students' | 'teachers'): number[] {
    const total = this.totalPages[type];
    const current = this.currentPage[type];
    const pages: number[] = [];

    if (total <= 7) {
      for (let i = 1; i <= total; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      if (current > 3) pages.push(-1);

      const start = Math.max(2, current - 1);
      const end = Math.min(total - 1, current + 1);

      for (let i = start; i <= end; i++) {
        if (i !== 1 && i !== total) {
          pages.push(i);
        }
      }

      if (current < total - 2) pages.push(-1);
      pages.push(total);
    }

    return pages;
  }

  Math = Math;

  // Modal auto-focus helper
  onModalOpen(): void {
    setTimeout(() => {
      const firstInput = document.querySelector('.modal-content input, .modal-content select');
      if (firstInput instanceof HTMLElement) {
        firstInput.focus();
      }
    }, 100);
  }
}

