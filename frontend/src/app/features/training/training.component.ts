import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TrainingService } from '../../core/services/training.service';
import { MasterSetupService } from '../../core/services/master-setup.service';
import { 
  TrainingProgram, 
  CreateTrainingProgramRequest, 
  TrainingStatus, 
  TrainingLevel, 
  TrainingType, 
  ExamType,
  SessionAttendanceReport,
  SingleSessionReport,
  DateBasedGroupedReport
} from '../../core/models/training';
import { TrainingTopic, TrainingName, TrainingCategory, Coordinator } from '../../core/models/master';

@Component({
  selector: 'app-training',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './training.component.html',
  styleUrls: ['./training.component.scss']
})
export class TrainingComponent implements OnInit {
  private trainingService = inject(TrainingService);
  private masterSetupService = inject(MasterSetupService);
  private fb = inject(FormBuilder);

  activeTab: 'programs' | 'sessions' | 'enrollments' | 'materials' | 'assessments' | 'reports' = 'programs';
  tabs: ('programs' | 'sessions' | 'enrollments' | 'materials' | 'assessments' | 'reports')[] = 
    ['programs', 'sessions', 'enrollments', 'materials', 'assessments', 'reports'];
  
  // Programs data
  programs: TrainingProgram[] = [];
  filteredPrograms: TrainingProgram[] = [];
  selectedProgram: TrainingProgram | null = null;
  showProgramModal = false;
  programForm: FormGroup;
  loading = false;
  searchTerm = '';
  
  // Master data for dropdowns
  trainingTopics: TrainingTopic[] = [];
  trainingCategories: TrainingCategory[] = [];
  coordinators: Coordinator[] = [];
  
  // Enums for template
  TrainingStatus = TrainingStatus;
  TrainingLevel = TrainingLevel;
  TrainingType = TrainingType;
  ExamType = ExamType;
  
  // Filters
  statusFilter: string | null = null;
  categoryFilter: string | null = null;

  // Advanced Reports
  reportForm: FormGroup;
  sessionReportRows: SessionAttendanceReport[] = [];
  selectedSessionReport: SingleSessionReport | null = null;
  groupedReport: DateBasedGroupedReport | null = null;
  reportsLoading = false;
  pdfDownloading = false;

  constructor() {
    this.programForm = this.fb.group({
      // Required fields
      trainingTopicId: [null, Validators.required],
      trainingName: ['', Validators.required],
      trainingDate: [null, Validators.required],
      trainingLevel: [null, Validators.required],
      trainingCategoryId: [null, Validators.required],
      coordinatorId: [null, Validators.required],
      trainingType: [null, Validators.required],
      examType: [null, Validators.required],
      
      // Optional fields
      facultyName: [''],
      hasArticleMaterial: [false],
      hasVideoMaterial: [false],
      hasSlideMaterial: [false]
    });

    this.reportForm = this.fb.group({
      fromDate: [null, Validators.required],
      toDate: [null, Validators.required],
      sessionId: [null]
    });
  }

  ngOnInit(): void {
    this.loadPrograms();
    this.loadMasterData();
  }
  
  loadMasterData(): void {
    // Load training topics
    this.masterSetupService.getAllTrainingTopics(true).subscribe({
      next: (topics) => this.trainingTopics = topics,
      error: (err) => console.error('Error loading training topics:', err)
    });
    
    // Load training categories
    this.masterSetupService.getAllTrainingCategories(true).subscribe({
      next: (categories) => this.trainingCategories = categories,
      error: (err) => console.error('Error loading training categories:', err)
    });
    
    // Load coordinators
    this.masterSetupService.getAllCoordinators(true).subscribe({
      next: (coordinators) => this.coordinators = coordinators,
      error: (err) => console.error('Error loading coordinators:', err)
    });
  }

  loadPrograms(): void {
    this.loading = true;
    // Only pass filters if they have valid values
    const status = (this.statusFilter && this.statusFilter.trim() !== '') ? this.statusFilter.trim() : undefined;
    const category = (this.categoryFilter && this.categoryFilter.trim() !== '') ? this.categoryFilter.trim() : undefined;
    
    this.trainingService.getAllPrograms(status, category)
      .subscribe({
        next: (programs) => {
          console.log('Programs loaded:', programs);
          this.programs = programs || [];
          this.filteredPrograms = programs || [];
          this.applySearch();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading programs:', error);
          this.programs = [];
          this.filteredPrograms = [];
          this.loading = false;
          const errorMsg = error.error?.message || error.message || 'Unknown error';
          console.error('Full error details:', error);
          alert('Failed to load programs: ' + errorMsg);
        }
      });
  }

  // ------------------- Reports Tab Logic -------------------

  setActiveTab(tab: typeof this.activeTab): void {
    this.activeTab = tab;
    if (tab === 'programs') {
      this.loadPrograms();
    } else if (tab === 'reports') {
      // Initialize default date range: current month
      if (!this.reportForm.get('fromDate')?.value || !this.reportForm.get('toDate')?.value) {
        const today = new Date();
        const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        this.reportForm.patchValue({
          fromDate: firstOfMonth.toISOString().substring(0, 10),
          toDate: today.toISOString().substring(0, 10)
        }, { emitEvent: false });
      }
    }
  }

  private getReportDateRange(): { from: string; to: string } | null {
    const from = this.reportForm.get('fromDate')?.value;
    const to = this.reportForm.get('toDate')?.value;
    if (!from || !to) {
      alert('Please select both From and To dates.');
      return null;
    }
    return { from, to };
  }

  loadSessionReportRows(): void {
    const range = this.getReportDateRange();
    if (!range) return;
    this.reportsLoading = true;
    this.trainingService.getTeacherSessionReports(range.from, range.to).subscribe({
      next: rows => {
        this.sessionReportRows = rows || [];
        this.reportsLoading = false;
      },
      error: err => {
        console.error('Error loading session reports', err);
        alert(err.error?.message || 'Failed to load session reports');
        this.reportsLoading = false;
      }
    });
  }

  viewSingleSessionReport(): void {
    const sessionId = this.reportForm.get('sessionId')?.value;
    if (!sessionId) {
      alert('Please select a session to view its report.');
      return;
    }
    this.reportsLoading = true;
    this.trainingService.getSingleSessionReport(sessionId).subscribe({
      next: report => {
        this.selectedSessionReport = report;
        this.reportsLoading = false;
      },
      error: err => {
        console.error('Error loading single session report', err);
        alert(err.error?.message || 'Failed to load session report');
        this.reportsLoading = false;
      }
    });
  }

  downloadSingleSessionPdf(): void {
    const sessionId = this.reportForm.get('sessionId')?.value;
    if (!sessionId) {
      alert('Please select a session to download its report.');
      return;
    }
    this.pdfDownloading = true;
    this.trainingService.downloadSingleSessionReportPdf(sessionId).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.pdfDownloading = false;
      },
      error: err => {
        console.error('Error downloading single session pdf', err);
        alert(err.error?.message || 'Failed to download session PDF');
        this.pdfDownloading = false;
      }
    });
  }

  viewDateRangeReport(): void {
    const range = this.getReportDateRange();
    if (!range) return;
    this.reportsLoading = true;
    this.trainingService.getDateBasedGroupedReport(range.from, range.to).subscribe({
      next: report => {
        this.groupedReport = report;
        this.reportsLoading = false;
      },
      error: err => {
        console.error('Error loading date-range report', err);
        alert(err.error?.message || 'Failed to load date-range report');
        this.reportsLoading = false;
      }
    });
  }

  downloadDateRangePdf(): void {
    const range = this.getReportDateRange();
    if (!range) return;
    this.pdfDownloading = true;
    this.trainingService.downloadTeacherSessionReportsPdf(range.from, range.to).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        this.pdfDownloading = false;
      },
      error: err => {
        console.error('Error downloading date-range pdf', err);
        alert(err.error?.message || 'Failed to download date-range PDF');
        this.pdfDownloading = false;
      }
    });
  }

  openProgramModal(program?: TrainingProgram): void {
    if (program) {
      this.selectedProgram = program;
      // Format date for input field (YYYY-MM-DD)
      const trainingDate = program.trainingDate ? program.trainingDate.split('T')[0] : null;
      
      this.programForm.patchValue({
        // New fields
        trainingTopicId: program.trainingTopicId,
        trainingName: program.trainingName || program.trainingNameName || '',
        trainingDate: trainingDate,
        trainingLevel: program.trainingLevel,
        trainingCategoryId: program.trainingCategoryId,
        facultyName: program.facultyName,
        coordinatorId: program.coordinatorId,
        trainingType: program.trainingType,
        examType: program.examType,
        hasArticleMaterial: program.hasArticleMaterial || false,
        hasVideoMaterial: program.hasVideoMaterial || false,
        hasSlideMaterial: program.hasSlideMaterial || false
      });
    } else {
      this.selectedProgram = null;
      this.programForm.reset({
        hasArticleMaterial: false,
        hasVideoMaterial: false,
        hasSlideMaterial: false
      });
    }
    this.showProgramModal = true;
  }

  closeProgramModal(): void {
    this.showProgramModal = false;
    this.selectedProgram = null;
    this.programForm.reset({
      hasArticleMaterial: false,
      hasVideoMaterial: false,
      hasSlideMaterial: false
    });
  }

  saveProgram(): void {
    if (this.programForm.invalid) {
      // Mark all fields as touched to show validation errors
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
    
    // Convert enum values to strings if they contain dots (e.g., TrainingLevel.BASIC -> BASIC)
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
    
    // Ensure title is set - use trainingName or a default
    const title = (formValue.trainingName && formValue.trainingName.trim()) || 'Training Program';
    
    // Build request object
    const request: CreateTrainingProgramRequest = {
      title: title,
      trainingTopicId: formValue.trainingTopicId || null,
      trainingName: formValue.trainingName || null,
      trainingDate: formValue.trainingDate || null,
      trainingLevel: trainingLevel || null,
      trainingCategoryId: formValue.trainingCategoryId || null,
      facultyName: formValue.facultyName || null,
      coordinatorId: formValue.coordinatorId || null,
      trainingType: trainingType || null,
      examType: examType || null,
      hasArticleMaterial: formValue.hasArticleMaterial || false,
      hasVideoMaterial: formValue.hasVideoMaterial || false,
      hasSlideMaterial: formValue.hasSlideMaterial || false,
      status: 'DRAFT'
    };

    const operation = this.selectedProgram
      ? this.trainingService.updateProgram(this.selectedProgram.id, request)
      : this.trainingService.createProgram(request);

    operation.subscribe({
      next: (response) => {
        console.log('Program saved successfully:', response);
        this.loadPrograms();
        this.closeProgramModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error saving program:', error);
        let errorMessage = 'Failed to save program';
        if (error.error) {
          if (error.error.message) {
            errorMessage = error.error.message;
          } else if (typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error.error) {
            errorMessage = error.error.error;
          } else if (Array.isArray(error.error) && error.error.length > 0) {
            errorMessage = error.error.map((e: any) => e.message || e).join(', ');
          }
        } else if (error.message) {
          errorMessage = error.message;
        }
        alert(errorMessage); // You can replace with toastr if available
        this.loading = false;
      }
    });
  }

  deleteProgram(program: TrainingProgram): void {
    if (confirm(`Are you sure you want to delete "${program.title}"?`)) {
      this.loading = true;
      this.trainingService.deleteProgram(program.id).subscribe({
        next: () => {
          this.loadPrograms();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error deleting program:', error);
          this.loading = false;
        }
      });
    }
  }

  applySearch(): void {
    if (!this.searchTerm.trim()) {
      this.filteredPrograms = this.programs;
      return;
    }

    const term = this.searchTerm.toLowerCase();
    this.filteredPrograms = this.programs.filter(p =>
      p.title.toLowerCase().includes(term) ||
      p.description?.toLowerCase().includes(term) ||
      p.category?.toLowerCase().includes(term)
    );
  }

  onSearchChange(): void {
    this.applySearch();
  }

  applyFilters(): void {
    this.loadPrograms();
  }

  clearFilters(): void {
    this.statusFilter = null;
    this.categoryFilter = null;
    this.searchTerm = '';
    this.loadPrograms();
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'DRAFT': 'bg-gray-100 text-gray-800',
      'PUBLISHED': 'bg-blue-100 text-blue-800',
      'ONGOING': 'bg-green-100 text-green-800',
      'COMPLETED': 'bg-purple-100 text-purple-800',
      'CANCELLED': 'bg-red-100 text-red-800',
      'ARCHIVED': 'bg-gray-100 text-gray-600'
    };
    return statusMap[status] || 'bg-gray-100 text-gray-800';
  }

}
