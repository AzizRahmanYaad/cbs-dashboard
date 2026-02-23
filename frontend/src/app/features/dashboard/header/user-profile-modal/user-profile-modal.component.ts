import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { User } from '../../../../core/models';
import { UserProfileService } from '../../../../core/services/user-profile.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-user-profile-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile-modal.component.html',
  styleUrls: ['./user-profile-modal.component.scss']
})
export class UserProfileModalComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private userProfileService = inject(UserProfileService);
  private toastr = inject(ToastrService);

  @Input() user: User | null = null;
  @Input() open = false;
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<User>();

  form: FormGroup;
  isLoading = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor() {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.maxLength(200)]],
      currentPassword: [''],
      newPassword: ['', [Validators.minLength(6)]],
      confirmNewPassword: ['']
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user'] && this.user) {
      this.form.patchValue({
        fullName: this.user.fullName ?? '',
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      });
    }
  }

  get fullNameControl() {
    return this.form.get('fullName');
  }

  get currentPasswordControl() {
    return this.form.get('currentPassword');
  }

  get newPasswordControl() {
    return this.form.get('newPassword');
  }

  get confirmNewPasswordControl() {
    return this.form.get('confirmNewPassword');
  }

  get wantsToChangePassword(): boolean {
    const newP = this.form.get('newPassword')?.value?.trim();
    const confirm = this.form.get('confirmNewPassword')?.value?.trim();
    return !!(newP || confirm);
  }

  close(): void {
    if (!this.isLoading) {
      this.closed.emit();
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }

  toggleCurrentPassword(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPassword(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(): void {
    const fullName = this.form.get('fullName')?.value?.trim();
    const currentPassword = this.form.get('currentPassword')?.value ?? '';
    const newPassword = this.form.get('newPassword')?.value?.trim() ?? '';
    const confirmNewPassword = this.form.get('confirmNewPassword')?.value?.trim() ?? '';

    if (!fullName) {
      this.form.get('fullName')?.markAsTouched();
      this.toastr.warning('Full name is required.', 'Validation');
      return;
    }

    if (this.wantsToChangePassword) {
      if (!currentPassword) {
        this.toastr.warning('Current password is required to set a new password.', 'Validation');
        this.form.get('currentPassword')?.markAsTouched();
        return;
      }
      if (newPassword.length < 6) {
        this.toastr.warning('New password must be at least 6 characters.', 'Validation');
        this.form.get('newPassword')?.markAsTouched();
        return;
      }
      if (newPassword !== confirmNewPassword) {
        this.toastr.warning('New password and confirmation do not match.', 'Validation');
        this.form.get('confirmNewPassword')?.markAsTouched();
        return;
      }
    }

    this.isLoading = true;

    this.userProfileService.updateProfile({ fullName }).subscribe({
      next: (updatedUser) => {
        if (this.wantsToChangePassword && newPassword) {
          this.userProfileService
            .changePassword({ currentPassword, newPassword })
            .subscribe({
              next: () => {
                this.isLoading = false;
                this.toastr.success('Profile and password updated successfully.');
                this.saved.emit(updatedUser);
                this.closed.emit();
              },
              error: (err) => {
                this.isLoading = false;
                const msg =
                  err.error?.message || 'Current password is incorrect. Please try again.';
                this.toastr.error(msg, 'Password Update Failed');
              }
            });
        } else {
          this.isLoading = false;
          this.toastr.success('Profile updated successfully.');
          this.saved.emit(updatedUser);
          this.closed.emit();
        }
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.message || 'Failed to update profile.';
        this.toastr.error(msg, 'Update Failed');
      }
    });
  }
}
