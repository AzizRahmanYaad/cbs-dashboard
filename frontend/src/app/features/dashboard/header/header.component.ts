import { Component, inject, OnInit, HostListener, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models';
import { UserProfileModalComponent } from './user-profile-modal/user-profile-modal.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, UserProfileModalComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  @Output() mobileMenuToggle = new EventEmitter<void>();

  currentUser: User | null = null;
  showUserMenu = false;
  showProfileModal = false;
  
  get isTrainingAdmin(): boolean {
    return this.authService.hasAnyRole(['ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']);
  }
  readonly logoPath = 'assets/DAB.png';
  readonly institutionName = 'Da Afghanistan Bank';
  readonly officeName = 'Chief Finance Office';
  readonly officeLocation = 'Islamic Emirate of Afghanistan';

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  toggleMobileMenu(): void {
    this.mobileMenuToggle.emit();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu-container')) {
      this.showUserMenu = false;
    }
  }

  logout(): void {
    this.authService.logout();
  }

  getInitials(name: string | null | undefined): string {
    if (!name) return 'CF';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  openTrainingMasterSettings(): void {
    this.router.navigate(['/dashboard/training/master-settings']);
    this.showUserMenu = false;
  }

  openProfile(): void {
    this.showProfileModal = true;
    this.showUserMenu = false;
  }

  closeProfileModal(): void {
    this.showProfileModal = false;
  }

  onProfileSaved(updatedUser: User): void {
    this.currentUser = updatedUser;
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
      }
    });
  }
}
