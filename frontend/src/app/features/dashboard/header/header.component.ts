import { Component, inject, OnInit, HostListener, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  private authService = inject(AuthService);

  @Output() mobileMenuToggle = new EventEmitter<void>();

  currentUser: User | null = null;
  showUserMenu = false;
  readonly logoPath = 'assets/DAB.png';
  readonly institutionName = 'Da Afghanistan Bank';
  readonly officeName = 'Chief Finance Office';
  readonly officeLocation = 'Islamic Republic of Afghanistan';

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
}
