import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { TestManagementService } from '../../core/services/test-management.service';
import { AdminUserService } from '../../core/services/admin-user.service';
import {
  TestModule,
  TestCase,
  TestExecution,
  Defect,
  Comment,
  TestReport,
  Priority,
  TestCaseStatus,
  ExecutionStatus,
  DefectStatus,
  DefectSeverity,
  CreateTestCaseRequest,
  CreateTestExecutionRequest,
  CreateDefectRequest,
  CreateCommentRequest,
  CreateTestModuleRequest
} from '../../core/models/test';
import { User } from '../../core/models';

@Component({
  selector: 'app-drill-testing',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './drill-testing.component.html',
  styleUrls: ['./drill-testing.component.scss']
})
export class DrillTestingComponent implements OnInit {
  private testService = inject(TestManagementService);
  private userService = inject(AdminUserService);
  private fb = inject(FormBuilder);

  activeTab: 'modules' | 'test-cases' | 'executions' | 'defects' | 'reports' = 'test-cases';
  
  // Data
  modules: TestModule[] = [];
  testCases: TestCase[] = [];
  executions: TestExecution[] = [];
  defects: Defect[] = [];
  users: User[] = [];
  report: TestReport | null = null;
  
  // Selected items
  selectedModule: TestModule | null = null;
  selectedTestCase: TestCase | null = null;
  selectedExecution: TestExecution | null = null;
  selectedDefect: Defect | null = null;
  
  // Forms
  moduleForm: FormGroup;
  testCaseForm: FormGroup;
  executionForm: FormGroup;
  defectForm: FormGroup;
  commentForm: FormGroup;
  
  // UI States
  showModuleModal = false;
  showTestCaseModal = false;
  showExecutionModal = false;
  showDefectModal = false;
  showCommentModal = false;
  loading = false;
  searchTerm = '';
  errorMessages: { [key: string]: string } = {};
  
  // Comments
  comments: Comment[] = [];
  commentContext: 'test-case' | 'defect' | null = null;
  commentContextId: number | null = null;
  
  // Enums for templates
  Priority = Priority;
  TestCaseStatus = TestCaseStatus;
  ExecutionStatus = ExecutionStatus;
  DefectStatus = DefectStatus;
  DefectSeverity = DefectSeverity;

  constructor() {
    this.moduleForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
    
    this.testCaseForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      preconditions: [''],
      steps: ['', [Validators.required, DrillTestingComponent.stepsValidator]],
      expectedResult: [''],
      priority: [Priority.MEDIUM, Validators.required],
      moduleId: [null],
      assignedToId: [null]
    });
    
    this.executionForm = this.fb.group({
      testCaseId: [null, Validators.required],
      status: [ExecutionStatus.PASSED, Validators.required],
      comments: [''],
      attachments: [[]]
    });
    
    this.defectForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      severity: [DefectSeverity.MEDIUM, Validators.required],
      testCaseId: [null],
      testExecutionId: [null],
      assignedToId: [null]
    });
    
    this.commentForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadData();
    this.loadUsers();
  }

  loadData(): void {
    this.loading = true;
    this.testService.getAllModules().subscribe({
      next: (modules) => {
        this.modules = modules;
        this.loading = false;
        this.errorMessages['modules'] = '';
      },
      error: (err) => {
        console.error('Error loading modules:', err);
        this.loading = false;
        this.handleError('modules', err, 'Failed to load modules');
      }
    });
    
    this.loadTestCases();
    this.loadExecutions();
    this.loadDefects();
    this.loadReport();
  }

  loadTestCases(): void {
    this.testService.getAllTestCases().subscribe({
      next: (testCases) => {
        this.testCases = testCases;
        this.errorMessages['testCases'] = '';
      },
      error: (err) => {
        console.error('Error loading test cases:', err);
        this.handleError('testCases', err, 'Failed to load test cases');
      }
    });
  }

  loadExecutions(): void {
    this.testService.getAllExecutions().subscribe({
      next: (executions) => {
        this.executions = executions;
        this.errorMessages['executions'] = '';
      },
      error: (err) => {
        console.error('Error loading executions:', err);
        this.handleError('executions', err, 'Failed to load executions');
      }
    });
  }

  loadDefects(): void {
    this.testService.getAllDefects().subscribe({
      next: (defects) => {
        this.defects = defects;
        this.errorMessages['defects'] = '';
      },
      error: (err) => {
        console.error('Error loading defects:', err);
        this.handleError('defects', err, 'Failed to load defects');
      }
    });
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.errorMessages['users'] = '';
      },
      error: (err) => {
        console.error('Error loading users:', err);
        if (err.status === 403) {
          this.errorMessages['users'] = 'You do not have permission to access user data';
        } else {
          this.handleError('users', err, 'Failed to load users');
        }
      }
    });
  }

  loadReport(): void {
    this.testService.generateReport().subscribe({
      next: (report) => {
        this.report = report;
        this.errorMessages['report'] = '';
      },
      error: (err) => {
        console.error('Error loading report:', err);
        this.handleError('report', err, 'Failed to load report');
      }
    });
  }

  // Module operations
  openModuleModal(): void {
    this.moduleForm.reset();
    this.showModuleModal = true;
  }

  createModule(): void {
    if (this.moduleForm.valid) {
      this.loading = true;
      const request: CreateTestModuleRequest = this.moduleForm.value;
      this.testService.createModule(request).subscribe({
        next: () => {
          this.loadData();
          this.showModuleModal = false;
          this.loading = false;
          this.moduleForm.reset();
        },
        error: (err) => {
          console.error('Error creating module:', err);
          this.loading = false;
          this.handleError('createModule', err, 'Failed to create module');
          let errorMessage = this.errorMessages['createModule'] || 'Unknown error';
          alert('Error creating module: ' + errorMessage);
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.moduleForm.controls).forEach(key => {
        this.moduleForm.get(key)?.markAsTouched();
      });
    }
  }

  // Test Case operations
  openTestCaseModal(testCase?: TestCase): void {
    if (testCase) {
      this.selectedTestCase = testCase;
      this.testCaseForm.patchValue({
        title: testCase.title,
        preconditions: testCase.preconditions,
        steps: testCase.steps?.join('\n') || '',
        expectedResult: testCase.expectedResult,
        priority: testCase.priority,
        moduleId: testCase.moduleId,
        assignedToId: testCase.assignedToId
      });
    } else {
      this.selectedTestCase = null;
      this.testCaseForm.reset({
        priority: Priority.MEDIUM,
        steps: ''
      });
    }
    this.showTestCaseModal = true;
  }

  createTestCase(): void {
    // Mark all fields as touched to show validation errors
    Object.keys(this.testCaseForm.controls).forEach(key => {
      this.testCaseForm.get(key)?.markAsTouched();
    });

    if (!this.testCaseForm.valid) {
      console.error('Form is invalid:', this.testCaseForm.errors);
      console.error('Form controls:', this.testCaseForm.controls);
      // Log each control's errors
      Object.keys(this.testCaseForm.controls).forEach(key => {
        const control = this.testCaseForm.get(key);
        if (control && control.invalid) {
          console.error(`Control ${key} is invalid:`, control.errors);
        }
      });
      return;
    }

    const stepsValue = this.testCaseForm.value.steps;
    const stepsArray = typeof stepsValue === 'string' 
      ? stepsValue.split('\n').filter(s => s.trim())
      : (stepsValue || []);
    
    // Ensure steps is an array (required by backend)
    if (!Array.isArray(stepsArray) || stepsArray.length === 0) {
      alert('Please enter at least one test step.');
      return;
    }
    
    const request: CreateTestCaseRequest = {
      title: this.testCaseForm.value.title,
      preconditions: this.testCaseForm.value.preconditions || undefined,
      steps: stepsArray,
      expectedResult: this.testCaseForm.value.expectedResult || undefined,
      priority: this.testCaseForm.value.priority,
      moduleId: this.testCaseForm.value.moduleId ? Number(this.testCaseForm.value.moduleId) : undefined,
      assignedToId: this.testCaseForm.value.assignedToId ? Number(this.testCaseForm.value.assignedToId) : undefined
    };

    this.loading = true;
    this.testService.createTestCase(request).subscribe({
      next: () => {
        this.loadTestCases();
        this.showTestCaseModal = false;
        this.loading = false;
        this.testCaseForm.reset({
          priority: Priority.MEDIUM,
          steps: ''
        });
      },
      error: (err) => {
        console.error('Error creating test case:', err);
        this.loading = false;
        this.handleError('createTestCase', err, 'Failed to create test case');
        let errorMessage = this.errorMessages['createTestCase'] || 'Unknown error';
        alert('Error creating test case: ' + errorMessage);
      }
    });
  }

  // Execution operations
  openExecutionModal(testCaseId?: number): void {
    this.executionForm.reset({
      testCaseId: testCaseId || null,
      status: ExecutionStatus.PASSED
    });
    this.showExecutionModal = true;
  }

  createExecution(): void {
    if (this.executionForm.valid) {
      const request: CreateTestExecutionRequest = this.executionForm.value;
      this.testService.createExecution(request).subscribe({
        next: () => {
          this.loadExecutions();
          this.showExecutionModal = false;
        },
        error: (err) => {
          console.error('Error creating execution:', err);
          this.handleError('createExecution', err, 'Failed to create execution');
          alert('Error creating execution: ' + (this.errorMessages['createExecution'] || 'Unknown error'));
        }
      });
    }
  }

  // Defect operations
  openDefectModal(testCaseId?: number, executionId?: number): void {
    this.defectForm.reset({
      testCaseId: testCaseId || null,
      testExecutionId: executionId || null,
      severity: DefectSeverity.MEDIUM
    });
    this.showDefectModal = true;
  }

  createDefect(): void {
    if (this.defectForm.valid) {
      const request: CreateDefectRequest = this.defectForm.value;
      this.testService.createDefect(request).subscribe({
        next: () => {
          this.loadDefects();
          this.showDefectModal = false;
        },
        error: (err) => {
          console.error('Error creating defect:', err);
          this.handleError('createDefect', err, 'Failed to create defect');
          alert('Error creating defect: ' + (this.errorMessages['createDefect'] || 'Unknown error'));
        }
      });
    }
  }

  // Comment operations
  openCommentModal(context: 'test-case' | 'defect', id: number): void {
    this.commentContext = context;
    this.commentContextId = id;
    this.commentForm.reset();
    this.loadComments(context, id);
    this.showCommentModal = true;
  }

  loadComments(context: 'test-case' | 'defect', id: number): void {
    if (context === 'test-case') {
      this.testService.getCommentsByTestCase(id).subscribe({
        next: (comments) => {
          this.comments = comments;
          this.errorMessages['comments'] = '';
        },
        error: (err) => {
          console.error('Error loading comments:', err);
          this.handleError('comments', err, 'Failed to load comments');
        }
      });
    } else {
      this.testService.getCommentsByDefect(id).subscribe({
        next: (comments) => {
          this.comments = comments;
          this.errorMessages['comments'] = '';
        },
        error: (err) => {
          console.error('Error loading comments:', err);
          this.handleError('comments', err, 'Failed to load comments');
        }
      });
    }
  }

  createComment(): void {
    if (this.commentForm.valid && this.commentContext && this.commentContextId) {
      const request: CreateCommentRequest = {
        content: this.commentForm.value.content,
        [this.commentContext === 'test-case' ? 'testCaseId' : 'defectId']: this.commentContextId
      };
      this.testService.createComment(request).subscribe({
        next: () => {
          this.loadComments(this.commentContext!, this.commentContextId!);
          this.commentForm.reset();
        },
        error: (err) => {
          console.error('Error creating comment:', err);
          this.handleError('createComment', err, 'Failed to create comment');
          alert('Error creating comment: ' + (this.errorMessages['createComment'] || 'Unknown error'));
        }
      });
    }
  }

  searchTestCases(): void {
    if (this.searchTerm.trim()) {
      this.testService.searchTestCases(this.searchTerm).subscribe({
        next: (testCases) => this.testCases = testCases,
        error: (err) => {
          console.error('Error searching:', err);
          this.handleError('search', err, 'Failed to search test cases');
        }
      });
    } else {
      this.loadTestCases();
    }
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PASSED': 'bg-green-100 text-green-800',
      'FAILED': 'bg-red-100 text-red-800',
      'BLOCKED': 'bg-yellow-100 text-yellow-800',
      'RETEST': 'bg-blue-100 text-blue-800',
      'NEW': 'bg-gray-100 text-gray-800',
      'IN_PROGRESS': 'bg-blue-100 text-blue-800',
      'RESOLVED': 'bg-green-100 text-green-800',
      'CLOSED': 'bg-gray-100 text-gray-800',
      'DRAFT': 'bg-gray-100 text-gray-800',
      'APPROVED': 'bg-green-100 text-green-800',
      'ARCHIVED': 'bg-gray-100 text-gray-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  getPriorityColor(priority: string): string {
    const colors: { [key: string]: string } = {
      'LOW': 'bg-blue-100 text-blue-800',
      'MEDIUM': 'bg-yellow-100 text-yellow-800',
      'HIGH': 'bg-orange-100 text-orange-800',
      'CRITICAL': 'bg-red-100 text-red-800'
    };
    return colors[priority] || 'bg-gray-100 text-gray-800';
  }

  // Custom validator for steps
  static stepsValidator(control: any): { [key: string]: any } | null {
    if (!control.value || !control.value.trim()) {
      return { required: true };
    }
    const steps = typeof control.value === 'string' 
      ? control.value.split('\n').filter((s: string) => s.trim())
      : (control.value || []);
    
    if (!Array.isArray(steps) || steps.length === 0) {
      return { required: true };
    }
    return null;
  }

  // Error handling helper
  private handleError(key: string, err: any, defaultMessage: string): void {
    let errorMessage = defaultMessage;
    
    if (err.status === 403) {
      errorMessage = 'You do not have permission to access this resource';
    } else if (err.status === 500) {
      errorMessage = 'Server error. Please try again later or contact support';
    } else if (err.status === 404) {
      errorMessage = 'Resource not found';
    } else if (err.status === 401) {
      errorMessage = 'Authentication required. Please log in again';
    } else if (err.error?.message) {
      errorMessage = err.error.message;
    } else if (err.message) {
      errorMessage = err.message;
    }
    
    this.errorMessages[key] = errorMessage;
    
    // Show alert for critical errors (500, 401)
    if (err.status === 500 || err.status === 401) {
      setTimeout(() => {
        alert(errorMessage);
      }, 100);
    }
  }

  // Clear error message
  clearError(key: string): void {
    this.errorMessages[key] = '';
  }
}
