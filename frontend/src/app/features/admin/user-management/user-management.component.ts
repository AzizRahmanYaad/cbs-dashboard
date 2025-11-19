import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminUserService, PageResponse } from '../../../core/services/admin-user.service';
import { CreateUserRequest, ModuleRole, UpdateUserRequest, User } from '../../../core/models';
import { ToastrService } from 'ngx-toastr';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit, OnDestroy {
  private adminUserService = inject(AdminUserService);
  private fb = inject(FormBuilder);
  private toastr = inject(ToastrService);
  private destroy$ = new Subject<void>();

  users: User[] = [];
  moduleRoles: ModuleRole[] = [];
  expandedModules: Set<string> = new Set();
  isSubmitting = false;
  isLoading = false;
  editingUser: User | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [5, 10, 20, 50];

  // Search and filter
  searchTerm = '';
  searchSubject = new Subject<string>();
  statusFilter: 'all' | 'active' | 'inactive' = 'all';

  userForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
    enabled: [true],
    roles: this.fb.control<string[]>([], { nonNullable: true })
  });

  ngOnInit(): void {
    this.startCreate();
    this.loadModuleRoles();
    this.loadUsers();

    // Debounce search input
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(search => {
      this.searchTerm = search;
      this.currentPage = 0;
      this.loadUsers();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get rolesControl(): FormControl<string[]> {
    return this.userForm.get('roles') as FormControl<string[]>;
  }

  get isEditing(): boolean {
    return !!this.editingUser;
  }

  loadUsers(): void {
    this.isLoading = true;
    const search = this.searchTerm.trim() || undefined;
    this.adminUserService.getUsers(this.currentPage, this.pageSize, 'id', 'desc', search).subscribe({
      next: (response: PageResponse<User>) => {
        let filteredUsers = response.content;
        
        // Apply status filter
        if (this.statusFilter !== 'all') {
          filteredUsers = filteredUsers.filter(user => 
            this.statusFilter === 'active' ? user.enabled : !user.enabled
          );
        }

        this.users = filteredUsers;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.toastr.error('Failed to load users', 'Error');
      }
    });
  }

  loadModuleRoles(): void {
    this.adminUserService.getRolesByModule().subscribe({
      next: (moduleRoles) => {
        this.moduleRoles = moduleRoles;
        // Expand all modules by default
        moduleRoles.forEach(mr => this.expandedModules.add(mr.moduleName));
      },
      error: () => this.toastr.error('Failed to load roles', 'Error')
    });
  }

  toggleModule(moduleName: string): void {
    if (this.expandedModules.has(moduleName)) {
      this.expandedModules.delete(moduleName);
    } else {
      this.expandedModules.add(moduleName);
    }
  }

  isModuleExpanded(moduleName: string): boolean {
    return this.expandedModules.has(moduleName);
  }

  onRoleToggle(roleName: string, checked: boolean): void {
    const selectedRoles = new Set(this.rolesControl.value ?? []);
    if (checked) {
      selectedRoles.add(roleName);
    } else {
      selectedRoles.delete(roleName);
    }
    this.rolesControl.setValue(Array.from(selectedRoles));
  }

  isRoleSelected(roleName: string): boolean {
    return this.rolesControl.value?.includes(roleName) ?? false;
  }

  onSearchChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  onStatusFilterChange(filter: string): void {
    this.statusFilter = filter as 'all' | 'active' | 'inactive';
    this.currentPage = 0;
    this.loadUsers();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  onPageSizeChange(event: Event): void {
    const size = +(event.target as HTMLSelectElement).value;
    this.pageSize = size;
    this.currentPage = 0;
    this.loadUsers();
  }

  startCreate(): void {
    this.editingUser = null;
    this.userForm.reset({
      username: '',
      email: '',
      password: '',
      enabled: true,
      roles: []
    });
    this.userForm.get('username')?.enable();
    this.configurePasswordValidators(false);
  }

  editUser(user: User): void {
    this.editingUser = user;
    this.userForm.reset({
      username: user.username,
      email: user.email,
      password: '',
      enabled: user.enabled ?? true,
      roles: [...(user.roles ?? [])]
    });
    this.userForm.get('username')?.disable();
    this.configurePasswordValidators(true);
    
    // Scroll to form
    setTimeout(() => {
      document.querySelector('.form-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
  }

  submitForm(): void {
    if (this.userForm.invalid) {
      this.toastr.warning('Please fix validation errors', 'Validation');
      return;
    }

    if (!this.rolesControl.value.length) {
      this.toastr.warning('Select at least one role', 'Validation');
      return;
    }

    const formValue = this.userForm.getRawValue();
    this.isSubmitting = true;

    if (this.editingUser) {
      const payload: UpdateUserRequest = {
        email: formValue.email,
        password: formValue.password ? formValue.password : undefined,
        enabled: formValue.enabled,
        roles: formValue.roles
      };

      this.adminUserService.updateUser(this.editingUser.id, payload).subscribe({
        next: () => {
          this.toastr.success('User updated successfully', 'Success');
          this.isSubmitting = false;
          this.startCreate();
          this.loadUsers();
        },
        error: (error) => {
          this.isSubmitting = false;
          this.toastr.error(error.error?.message || 'Failed to update user', 'Error');
        }
      });
      return;
    }

    const createPayload: CreateUserRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      enabled: formValue.enabled,
      roles: formValue.roles
    };

    this.adminUserService.createUser(createPayload).subscribe({
      next: () => {
        this.toastr.success('User created successfully', 'Success');
        this.isSubmitting = false;
        this.startCreate();
        this.loadUsers();
      },
      error: (error) => {
        this.isSubmitting = false;
        this.toastr.error(error.error?.message || 'Failed to create user', 'Error');
      }
    });
  }

  private configurePasswordValidators(isEditing: boolean): void {
    const control = this.userForm.get('password');
    if (!control) {
      return;
    }
    control.clearValidators();
    const validators = [Validators.minLength(8)];
    if (!isEditing) {
      validators.push(Validators.required);
    }
    control.addValidators(validators);
    control.updateValueAndValidity();
  }

  trackByUserId(_: number, user: User): number {
    return user.id;
  }

  trackByModuleName(_: number, moduleRole: ModuleRole): string {
    return moduleRole.moduleName;
  }

  trackByRoleName(_: number, role: { name: string }): string {
    return role.name;
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = Math.min(this.totalPages, 5);
    const startPage = Math.max(0, Math.min(this.currentPage - 2, this.totalPages - maxPages));
    
    for (let i = 0; i < maxPages; i++) {
      pages.push(startPage + i);
    }
    return pages;
  }

  getSelectedCountForModule(moduleRole: ModuleRole): number {
    return moduleRole.roles.filter(role => this.isRoleSelected(role.name)).length;
  }

  // Expose Math to template
  Math = Math;

  // Helper method to format role names for display
  formatRoleName(roleName: string): string {
    return roleName.replace('ROLE_', '').replace(/_/g, ' ');
  }
}
