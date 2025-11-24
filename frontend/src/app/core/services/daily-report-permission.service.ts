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

  // Role constants - Only IndividualReport role for Daily Report module
  readonly ROLES = {
    INDIVIDUAL_REPORT: 'ROLE_INDIVIDUAL_REPORT' // Single role for Daily Report module
  };

  /**
   * Check if user can access Daily Report module
   */
  canAccessModule(): boolean {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) {
      return false;
    }

    // Only IndividualReport role has access to Daily Report module
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }

  /**
   * Check if user can create and submit their own daily reports
   */
  canCreateReport(): boolean {
    // IndividualReport role has full access to create reports
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }

  /**
   * Check if user can edit their own reports (before approval)
   */
  canEditOwnReport(): boolean {
    // IndividualReport role has full access to edit own reports
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }
  
  /**
   * Check if user has IndividualReport role
   */
  hasIndividualReportAccess(): boolean {
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }
  
  /**
   * Check if user can download their own reports
   * IndividualReport role has full download access
   */
  canDownloadOwnReport(): boolean {
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]);
  }

  /**
   * Check if user can review reports (supervisor and above)
   */
  // All other role checks return false - only IndividualReport role exists
  canReviewReports(): boolean {
    return false; // IndividualReport users can only manage their own reports
  }

  canApproveReports(): boolean {
    return false; // IndividualReport users can only manage their own reports
  }

  canRejectReports(): boolean {
    return false; // IndividualReport users can only manage their own reports
  }

  canViewAllReports(): boolean {
    return false; // IndividualReport users can only view their own reports
  }

  isController(): boolean {
    return false; // IndividualReport users are not controllers
  }

  isCFO(): boolean {
    return false; // IndividualReport users are not CFOs
  }

  canViewDashboard(): boolean {
    return false; // IndividualReport users don't have dashboard access
  }

  isViewOnly(): boolean {
    return false;
  }

  hasFullAccess(): boolean {
    return false; // IndividualReport users only have access to their own reports
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

    // IndividualReport users can only edit their own reports
    return this.hasAnyRole([this.ROLES.INDIVIDUAL_REPORT]) 
      && currentUser.id === reportEmployeeId;
  }

  /**
   * Check if user can delete a report
   * Only supervisors and admins can delete reports
   */
  canDeleteReport(): boolean {
    return false; // IndividualReport users cannot delete reports
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

