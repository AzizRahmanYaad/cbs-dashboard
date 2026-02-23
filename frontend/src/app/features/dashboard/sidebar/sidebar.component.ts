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
  /** Route of the parent item that is expanded (e.g. Training Module) */
  expandedParentRoute: string | null = '/dashboard/training';
  private authService = inject(AuthService);
  currentUser$ = this.authService.currentUser$;
  readonly logoPath = 'assets/DAB.png';
  readonly institutionName = 'Da Afghanistan Bank';
  readonly officeShort = 'CFO';
  readonly motto = 'Integrity • Stability • Transparency';
  
  menuItems: MenuItem[] = [
    // Student: only "Training" (links to student training content)
    {
      label: 'Training',
      icon: 'academic-cap',
      route: '/dashboard/training/student-dashboard',
      requiredRoles: ['ROLE_STUDENT']
    },
    // Teacher: only "Training" (links to teacher training content)
    {
      label: 'Training',
      icon: 'academic-cap',
      route: '/dashboard/training/teacher-dashboard',
      requiredRoles: ['ROLE_TEACHER']
    },
    {
      label: 'Dashboard',
      icon: 'home',
      route: '/dashboard/home',
      excludeRoles: ['ROLE_TEACHER', 'ROLE_STUDENT']
    },
    // Admin / Training admin: single "Training Module" link (no Student/Teacher Dashboard sub-items)
    {
      label: 'Training Module',
      icon: 'academic-cap',
      route: '/dashboard/training',
      requiredRoles: ['ROLE_TRAINING', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN']
    },
    {
      label: 'CFO Dashboard',
      icon: 'chart-bar',
      route: '/dashboard/cfo',
      requiredRoles: ['ROLE_CFO']
    },
    // CFO view-only access to training analytics
    {
      label: 'Training',
      icon: 'academic-cap',
      route: '/dashboard/training/cfo-dashboard',
      requiredRoles: ['ROLE_CFO']
    },
    {
      label: 'Drill Test',
      icon: 'clipboard-document-check',
      route: '/dashboard/drill-testing',
      requiredRoles: ['ROLE_DRILL_TESTING', 'ROLE_QA_LEAD', 'ROLE_TESTER', 'ROLE_MANAGER', 'ROLE_CFO']
    },
    {
      label: 'Daily Report',
      icon: 'document-text',
      route: '/dashboard/daily-report',
      requiredRoles: ['ROLE_INDIVIDUAL_REPORT', 'ROLE_ADMIN', 'ROLE_QUALITY_CONTROL', 'ROLE_CFO']
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

  /** True if user has access to the parent item or any of its children (so we show the parent row). */
  hasAccessToItemOrChildren(item: MenuItem): boolean {
    if (this.hasAccess(item)) return true;
    if (item.children?.length) {
      return item.children.some(child => this.hasAccess(child));
    }
    return false;
  }

  isExpanded(item: MenuItem): boolean {
    return this.expandedParentRoute === item.route;
  }

  toggleExpand(route: string): void {
    this.expandedParentRoute = this.expandedParentRoute === route ? null : route;
  }
}
