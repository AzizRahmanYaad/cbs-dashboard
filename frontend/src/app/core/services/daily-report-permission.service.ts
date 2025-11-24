import { Injectable, inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Service for managing Daily Report module permissions
 * Based on role-based access control requirements:
 * - Employee: Can create and submit their own daily reports
 * - Supervisor: Can review, approve, or send back reports for correction
 * - Director/Manager/Team Lead: View-only or full-access permissions
 */
@Injectable({
  providedIn: 'root'
})
export class DailyReportPermissionService {
  private authService = inject(AuthService);

  // Role constants
  readonly ROLES = {
    EMPLOYEE: 'ROLE_DAILY_REPORT_EMPLOYEE',
    SUPERVISOR: 'ROLE_DAILY_REPORT_SUPERVISOR',
    DIRECTOR: 'ROLE_DAILY_REPORT_DIRECTOR',
    MANAGER: 'ROLE_DAILY_REPORT_MANAGER',
    TEAM_LEAD: 'ROLE_DAILY_REPORT_TEAM_LEAD',
    ADMIN: 'ROLE_ADMIN',
    DAILY_REPORT: 'ROLE_DAILY_REPORT', // General daily report access
    INDIVIDUAL_REPORT_ACCESS: 'ROLE_INDIVIDUAL_REPORT_ACCESS', // Individual report access only
    CONTROLLER: 'ROLE_DAILY_REPORT_SUPERVISOR', // Controller maps to Supervisor
    CFO: 'ROLE_DAILY_REPORT_DIRECTOR' // CFO maps to Director
  };

  /**
   * Check if user can access Daily Report module
   */
  canAccessModule(): boolean {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) {
      return false;
    }

    return this.hasAnyRole([
      this.ROLES.EMPLOYEE,
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN,
      this.ROLES.DAILY_REPORT,
      this.ROLES.INDIVIDUAL_REPORT_ACCESS
    ]);
  }

  /**
   * Check if user can create and submit their own daily reports
   */
  canCreateReport(): boolean {
    return this.hasAnyRole([
      this.ROLES.EMPLOYEE,
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN,
      this.ROLES.DAILY_REPORT,
      this.ROLES.INDIVIDUAL_REPORT_ACCESS
    ]);
  }

  /**
   * Check if user can edit their own reports (before approval)
   */
  canEditOwnReport(): boolean {
    return this.hasAnyRole([
      this.ROLES.EMPLOYEE,
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN,
      this.ROLES.DAILY_REPORT,
      this.ROLES.INDIVIDUAL_REPORT_ACCESS
    ]);
  }
  
  /**
   * Check if user has Individual Report Access role (limited to own reports only)
   */
  hasIndividualReportAccess(): boolean {
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT_ACCESS]);
  }
  
  /**
   * Check if user can download their own reports
   */
  canDownloadOwnReport(): boolean {
    return this.hasAnyRole([
      this.ROLES.EMPLOYEE,
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN,
      this.ROLES.DAILY_REPORT,
      this.ROLES.INDIVIDUAL_REPORT_ACCESS
    ]);
  }

  /**
   * Check if user can review reports (supervisor and above)
   */
  canReviewReports(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can approve reports
   */
  canApproveReports(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can reject/send back reports
   */
  canRejectReports(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can view all reports (not just their own)
   */
  canViewAllReports(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN,
      this.ROLES.CONTROLLER,
      this.ROLES.CFO
    ]);
  }

  /**
   * Check if user is a Controller (can generate and download reports)
   */
  isController(): boolean {
    return this.hasAnyRole([
      this.ROLES.CONTROLLER,
      this.ROLES.SUPERVISOR,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user is a CFO (can view and confirm reports)
   */
  isCFO(): boolean {
    return this.hasAnyRole([
      this.ROLES.CFO,
      this.ROLES.DIRECTOR,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can view dashboard/analytics
   */
  canViewDashboard(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user has view-only access (Director, Manager, Team Lead)
   */
  isViewOnly(): boolean {
    return this.hasAnyRole([
      this.ROLES.DIRECTOR,
      this.ROLES.MANAGER,
      this.ROLES.TEAM_LEAD
    ]) && !this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user has full access (Supervisor, Admin)
   */
  hasFullAccess(): boolean {
    return this.hasAnyRole([
      this.ROLES.SUPERVISOR,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can edit a specific report
   * - Employees can only edit their own reports if not approved
   * - Supervisors can edit any report if not approved
   */
  canEditReport(reportEmployeeId: number, isApproved: boolean): boolean {
    if (isApproved) {
      // Approved reports require supervisor re-approval
      return this.hasFullAccess();
    }

    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      return false;
    }

    // Supervisors and admins can edit any report
    if (this.hasFullAccess()) {
      return true;
    }

    // Employees and individual report access users can only edit their own reports
    return this.hasAnyRole([this.ROLES.EMPLOYEE, this.ROLES.DAILY_REPORT, this.ROLES.INDIVIDUAL_REPORT_ACCESS]) 
      && currentUser.id === reportEmployeeId;
  }

  /**
   * Check if user can delete a report
   * Only supervisors and admins can delete reports
   */
  canDeleteReport(): boolean {
    return this.hasFullAccess();
  }

  /**
   * Get user's role level for Daily Report module
   * Returns: 'employee' | 'supervisor' | 'director' | 'manager' | 'team_lead' | 'admin' | null
   */
  getUserRoleLevel(): string | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) {
      return null;
    }

    if (user.roles.includes(this.ROLES.ADMIN)) {
      return 'admin';
    }
    if (user.roles.includes(this.ROLES.SUPERVISOR)) {
      return 'supervisor';
    }
    if (user.roles.includes(this.ROLES.DIRECTOR)) {
      return 'director';
    }
    if (user.roles.includes(this.ROLES.MANAGER)) {
      return 'manager';
    }
    if (user.roles.includes(this.ROLES.TEAM_LEAD)) {
      return 'team_lead';
    }
    if (user.roles.includes(this.ROLES.EMPLOYEE) || user.roles.includes(this.ROLES.DAILY_REPORT) || user.roles.includes(this.ROLES.INDIVIDUAL_REPORT_ACCESS)) {
      return 'employee';
    }

    return null;
  }

  /**
   * Helper method to check if user has any of the specified roles
   */
  private hasAnyRole(roles: string[]): boolean {
    return this.authService.hasAnyRole(roles);
  }
}

