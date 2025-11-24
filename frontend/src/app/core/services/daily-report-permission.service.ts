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
    INDIVIDUAL_REPORT: 'ROLE_INDIVIDUAL_REPORT', // Individual report access - full access to own reports
    ADMIN: 'ROLE_ADMIN'
  };

  /**
   * Check if user can access Daily Report module
   */
  canAccessModule(): boolean {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) {
      return false;
    }

    // Primary role for daily report module
    return this.hasAnyRole([
      this.ROLES.INDIVIDUAL_REPORT,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can create and submit their own daily reports
   */
  canCreateReport(): boolean {
    // IndividualReport role has full access to create reports
    return this.hasAnyRole([
      this.ROLES.INDIVIDUAL_REPORT,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can edit their own reports (before approval)
   */
  canEditOwnReport(): boolean {
    // IndividualReport role has full access to edit own reports
    return this.hasAnyRole([
      this.ROLES.INDIVIDUAL_REPORT,
      this.ROLES.ADMIN
    ]);
  }
  
  /**
   * Check if user has IndividualReport role (limited to own reports only)
   */
  hasIndividualReportAccess(): boolean {
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }
  
  /**
   * Check if user can download their own reports
   * IndividualReport role has full download access to own reports
   */
  canDownloadOwnReport(): boolean {
    return this.hasAnyRole([
      this.ROLES.INDIVIDUAL_REPORT,
      this.ROLES.ADMIN
    ]);
  }

  /**
   * Check if user can review reports (admin only)
   */
  canReviewReports(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user can approve reports (admin only)
   */
  canApproveReports(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user can reject/send back reports (admin only)
   */
  canRejectReports(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user can view all reports (not just their own) - admin only
   */
  canViewAllReports(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user is a Controller (can generate and download reports) - admin only
   */
  isController(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user is a CFO (can view and confirm reports) - admin only
   */
  isCFO(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user can view dashboard/analytics - admin only
   */
  canViewDashboard(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user has view-only access - not applicable with new role structure
   */
  isViewOnly(): boolean {
    return false;
  }

  /**
   * Check if user has full access (Admin only)
   */
  hasFullAccess(): boolean {
    return this.hasAnyRole([this.ROLES.ADMIN]);
  }

  /**
   * Check if user can edit a specific report
   * - IndividualReport role users can only edit their own reports if not approved
   * - Admins can edit any report
   */
  canEditReport(reportEmployeeId: number, isApproved: boolean): boolean {
    if (isApproved) {
      // Approved reports require admin re-approval
      return this.hasFullAccess();
    }

    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      return false;
    }

    // Admins can edit any report
    if (this.hasFullAccess()) {
      return true;
    }

    // IndividualReport role users can only edit their own reports
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]) 
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
   * Returns: 'individual_report' | 'admin' | null
   */
  getUserRoleLevel(): string | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) {
      return null;
    }

    if (user.roles.includes(this.ROLES.ADMIN)) {
      return 'admin';
    }
    if (user.roles.includes(this.ROLES.INDIVIDUAL_REPORT)) {
      return 'individual_report';
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

