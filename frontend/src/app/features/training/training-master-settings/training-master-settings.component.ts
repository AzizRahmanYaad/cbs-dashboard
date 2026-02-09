import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MasterSetupService } from '../../../core/services/master-setup.service';
import { ToastrService } from 'ngx-toastr';
import { TrainingCategory, TrainingName, TrainingModule, Department } from '../../../core/models/master';

@Component({
  selector: 'app-training-master-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './training-master-settings.component.html',
  styleUrls: ['./training-master-settings.component.scss']
})
export class TrainingMasterSettingsComponent implements OnInit {
  private masterSetupService = inject(MasterSetupService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);

  activeTab: 'names' | 'modules' | 'departments' | 'categories' = 'names';
  
  // Training Names
  trainingNames: TrainingName[] = [];
  selectedTrainingName: TrainingName | null = null;
  showTrainingNameModal = false;
  trainingNameForm: FormGroup;
  
  // Training Modules
  trainingModules: TrainingModule[] = [];
  selectedTrainingModule: TrainingModule | null = null;
  showTrainingModuleModal = false;
  trainingModuleForm: FormGroup;
  
  // Departments
  departments: Department[] = [];
  selectedDepartment: Department | null = null;
  showDepartmentModal = false;
  departmentForm: FormGroup;
  
  // Training Categories
  categories: TrainingCategory[] = [];
  selectedCategory: TrainingCategory | null = null;
  showCategoryModal = false;
  categoryForm: FormGroup;
  
  loading = false;

  constructor() {
    this.trainingNameForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isActive: [true]
    });
    
    this.trainingModuleForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isActive: [true]
    });
    
    this.departmentForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isActive: [true]
    });
    
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.loadTrainingNames();
    this.loadTrainingModules();
    this.loadDepartments();
    this.loadCategories();
  }

  loadTrainingNames(): void {
    this.loading = true;
    this.masterSetupService.getAllTrainingNames(false).subscribe({
      next: (names) => {
        this.trainingNames = names;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load training names');
        this.loading = false;
      }
    });
  }

  loadTrainingModules(): void {
    this.loading = true;
    this.masterSetupService.getAllTrainingModules(false).subscribe({
      next: (modules) => {
        this.trainingModules = modules;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load training modules');
        this.loading = false;
      }
    });
  }

  loadDepartments(): void {
    this.loading = true;
    this.masterSetupService.getAllDepartments(false).subscribe({
      next: (departments) => {
        this.departments = departments;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load departments');
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.loading = true;
    this.masterSetupService.getAllTrainingCategories(false).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Failed to load categories');
        this.loading = false;
      }
    });
  }

  // Topic methods
  openTrainingNameModal(name?: TrainingName): void {
    if (name) {
      this.selectedTrainingName = name;
      this.trainingNameForm.patchValue({
        name: name.name,
        description: name.description,
        isActive: name.isActive
      });
    } else {
      this.selectedTrainingName = null;
      this.trainingNameForm.reset({ isActive: true });
    }
    this.showTrainingNameModal = true;
  }

  closeTrainingNameModal(): void {
    this.showTrainingNameModal = false;
    this.selectedTrainingName = null;
  }

  saveTrainingName(): void {
    if (this.trainingNameForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const request = this.trainingNameForm.value;

    const operation = this.selectedTrainingName
      ? this.masterSetupService.updateTrainingName(this.selectedTrainingName.id, request)
      : this.masterSetupService.createTrainingName(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Training Name ${this.selectedTrainingName ? 'updated' : 'created'} successfully`);
        this.loadTrainingNames();
        this.closeTrainingNameModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save training name');
        this.loading = false;
      }
    });
  }

  deleteTrainingName(name: TrainingName): void {
    if (confirm(`Are you sure you want to delete training name "${name.name}"?`)) {
      this.loading = true;
      this.masterSetupService.deleteTrainingName(name.id).subscribe({
        next: () => {
          this.toastr.success('Training name deleted successfully');
          this.loadTrainingNames();
          this.loading = false;
        },
        error: () => {
          this.toastr.error('Failed to delete training name');
          this.loading = false;
        }
      });
    }
  }

  // Training Module methods
  openTrainingModuleModal(module?: TrainingModule): void {
    if (module) {
      this.selectedTrainingModule = module;
      this.trainingModuleForm.patchValue({
        name: module.name,
        description: module.description,
        isActive: module.isActive
      });
    } else {
      this.selectedTrainingModule = null;
      this.trainingModuleForm.reset({ isActive: true });
    }
    this.showTrainingModuleModal = true;
  }

  closeTrainingModuleModal(): void {
    this.showTrainingModuleModal = false;
    this.selectedTrainingModule = null;
  }

  saveTrainingModule(): void {
    if (this.trainingModuleForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const request = this.trainingModuleForm.value;

    const operation = this.selectedTrainingModule
      ? this.masterSetupService.updateTrainingModule(this.selectedTrainingModule.id, request)
      : this.masterSetupService.createTrainingModule(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Module ${this.selectedTrainingModule ? 'updated' : 'created'} successfully`);
        this.loadTrainingModules();
        this.closeTrainingModuleModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save module');
        this.loading = false;
      }
    });
  }

  deleteTrainingModule(module: TrainingModule): void {
    if (confirm(`Are you sure you want to delete module "${module.name}"?`)) {
      this.loading = true;
      this.masterSetupService.deleteTrainingModule(module.id).subscribe({
        next: () => {
          this.toastr.success('Module deleted successfully');
          this.loadTrainingModules();
          this.loading = false;
        },
        error: () => {
          this.toastr.error('Failed to delete module');
          this.loading = false;
        }
      });
    }
  }

  // Department methods
  openDepartmentModal(department?: Department): void {
    if (department) {
      this.selectedDepartment = department;
      this.departmentForm.patchValue({
        name: department.name,
        description: department.description,
        isActive: department.isActive
      });
    } else {
      this.selectedDepartment = null;
      this.departmentForm.reset({ isActive: true });
    }
    this.showDepartmentModal = true;
  }

  closeDepartmentModal(): void {
    this.showDepartmentModal = false;
    this.selectedDepartment = null;
  }

  saveDepartment(): void {
    if (this.departmentForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const request = this.departmentForm.value;

    const operation = this.selectedDepartment
      ? this.masterSetupService.updateDepartment(this.selectedDepartment.id, request)
      : this.masterSetupService.createDepartment(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Department ${this.selectedDepartment ? 'updated' : 'created'} successfully`);
        this.loadDepartments();
        this.closeDepartmentModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save department');
        this.loading = false;
      }
    });
  }

  deleteDepartment(department: Department): void {
    if (confirm(`Are you sure you want to delete department "${department.name}"?`)) {
      this.loading = true;
      this.masterSetupService.deleteDepartment(department.id).subscribe({
        next: () => {
          this.toastr.success('Department deleted successfully');
          this.loadDepartments();
          this.loading = false;
        },
        error: () => {
          this.toastr.error('Failed to delete department');
          this.loading = false;
        }
      });
    }
  }

  // Category methods
  openCategoryModal(category?: TrainingCategory): void {
    if (category) {
      this.selectedCategory = category;
      this.categoryForm.patchValue({
        name: category.name,
        description: category.description,
        isActive: category.isActive
      });
    } else {
      this.selectedCategory = null;
      this.categoryForm.reset({ isActive: true });
    }
    this.showCategoryModal = true;
  }

  closeCategoryModal(): void {
    this.showCategoryModal = false;
    this.selectedCategory = null;
  }

  saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.loading = true;
    const request = this.categoryForm.value;

    const operation = this.selectedCategory
      ? this.masterSetupService.updateTrainingCategory(this.selectedCategory.id, request)
      : this.masterSetupService.createTrainingCategory(request);

    operation.subscribe({
      next: () => {
        this.toastr.success(`Category ${this.selectedCategory ? 'updated' : 'created'} successfully`);
        this.loadCategories();
        this.closeCategoryModal();
        this.loading = false;
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Failed to save category');
        this.loading = false;
      }
    });
  }

  deleteCategory(category: TrainingCategory): void {
    if (confirm(`Are you sure you want to delete category "${category.name}"?`)) {
      this.loading = true;
      this.masterSetupService.deleteTrainingCategory(category.id).subscribe({
        next: () => {
          this.toastr.success('Category deleted successfully');
          this.loadCategories();
          this.loading = false;
        },
        error: () => {
          this.toastr.error('Failed to delete category');
          this.loading = false;
        }
      });
    }
  }

  setActiveTab(tab: 'names' | 'modules' | 'departments' | 'categories'): void {
    this.activeTab = tab;
  }
}
