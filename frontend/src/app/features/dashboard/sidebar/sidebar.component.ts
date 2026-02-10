import { Component, Output, EventEmitter, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  children?: MenuItem[];
  requiredRoles?: string[];
  /** Hide this item when user has any of these roles (e.g. Teachers/Students use their own panels) */
  excludeRoles?: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @Output() sidebarToggle = new EventEmitter<boolean>();
  @Output() mobileMenuClose = new EventEmitter<void>();
  @Input() isMobileOpen = false;

  isCollapsed = false;
  private authService = inject(AuthService);
  currentUser$ = this.authService.currentUser$;
  readonly logoPath = 'assets/DAB.png';
  readonly institutionName = 'Da Afghanistan Bank';
  readonly officeShort = 'CFO';
  readonly motto = 'Integrity • Stability • Transparency';
  
  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'home',
      route: '/dashboard/home',
      excludeRoles: ['ROLE_TEACHER', 'ROLE_STUDENT']
    },
    {
      label: 'Training Module',
      icon: 'academic-cap',
      route: '/dashboard/training',
      requiredRoles: ['ROLE_TRAINING']
    },
    {
      label: 'Training Admin',
      icon: 'cog-6-tooth',
      route: '/dashboard/training/admin',
      requiredRoles: ['ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']
    },
    {
      label: 'Teacher Dashboard',
      icon: 'user-circle',
      route: '/dashboard/training/teacher-dashboard',
      requiredRoles: ['ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']
    },
    {
      label: 'Student Dashboard',
      icon: 'academic-cap',
      route: '/dashboard/training/student-dashboard',
      requiredRoles: ['ROLE_STUDENT', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']
    },
    {
      label: 'CFO Training Analytics',
      icon: 'chart-bar',
      route: '/dashboard/training/cfo-dashboard',
      requiredRoles: ['ROLE_CFO']
    },
    {
      label: 'Drill Test',
      icon: 'clipboard-document-check',
      route: '/dashboard/drill-testing',
      requiredRoles: ['ROLE_DRILL_TESTING', 'ROLE_QA_LEAD', 'ROLE_TESTER', 'ROLE_MANAGER']
    },
    {
      label: 'Daily Report',
      icon: 'document-text',
      route: '/dashboard/daily-report',
      requiredRoles: ['ROLE_INDIVIDUAL_REPORT', 'ROLE_ADMIN', 'ROLE_QUALITY_CONTROL']
    },
    {
      label: 'User Management',
      icon: 'shield-check',
      route: '/dashboard/admin/users',
      requiredRoles: ['ROLE_ADMIN']
    }
  ];

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
    this.sidebarToggle.emit(this.isCollapsed);
  }

  onNavItemClick(): void {
    // Close mobile sidebar when a nav item is clicked
    if (this.isMobileOpen && window.innerWidth <= 768) {
      this.mobileMenuClose.emit();
    }
  }

  hasAccess(item: MenuItem): boolean {
    if (item.excludeRoles?.length && this.authService.hasAnyRole(item.excludeRoles)) {
      return false;
    }
    if (!item.requiredRoles?.length) return true;
    return this.authService.hasAnyRole(item.requiredRoles);
  }
}
