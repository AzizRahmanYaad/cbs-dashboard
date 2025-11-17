import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminUserService } from '../../../core/services/admin-user.service';
import { CreateUserRequest, Role, UpdateUserRequest, User } from '../../../core/models';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  private adminUserService = inject(AdminUserService);
  private fb = inject(FormBuilder);
  private toastr = inject(ToastrService);

  users: User[] = [];
  roles: Role[] = [];
  isSubmitting = false;
  isLoading = false;
  editingUser: User | null = null;

  userForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
    enabled: [true],
    roles: this.fb.control<string[]>([], { nonNullable: true })
  });

  ngOnInit(): void {
    this.startCreate();
    this.loadRoles();
    this.loadUsers();
  }

  get rolesControl(): FormControl<string[]> {
    return this.userForm.get('roles') as FormControl<string[]>;
  }

  get isEditing(): boolean {
    return !!this.editingUser;
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminUserService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.toastr.error('Failed to load users', 'Error');
      }
    });
  }

  loadRoles(): void {
    this.adminUserService.getRoles().subscribe({
      next: (roles) => (this.roles = roles),
      error: () => this.toastr.error('Failed to load roles', 'Error')
    });
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
          this.toastr.success('User updated');
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
        this.toastr.success('User created');
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
}

