# Daily Report Module - User Management & Role-Based Access Control

## Overview
This document describes the user management and role-based access control (RBAC) implementation for the Daily Report module.

## Roles Implemented

### 1. ROLE_DAILY_REPORT_EMPLOYEE
- **Purpose**: Basic employee access
- **Permissions**:
  - Can create and submit their own daily reports
  - Can edit their own reports (before approval)
  - Can view their own reports
  - Cannot approve or reject reports
  - Cannot view other employees' reports

### 2. ROLE_DAILY_REPORT_SUPERVISOR
- **Purpose**: Supervisor-level access with review and approval capabilities
- **Permissions**:
  - All employee permissions
  - Can review all reports
  - Can approve reports
  - Can reject/send back reports for correction
  - Can view dashboard and analytics
  - Can edit any report (before approval)
  - Can delete reports

### 3. ROLE_DAILY_REPORT_DIRECTOR
- **Purpose**: Director-level access (view-only or full access)
- **Permissions**:
  - Can view all reports
  - Can view dashboard and analytics
  - Can create reports
  - View-only mode (no approval/rejection by default, but can be configured)

### 4. ROLE_DAILY_REPORT_MANAGER
- **Purpose**: Manager-level access (view-only or full access)
- **Permissions**:
  - Can view all reports
  - Can view dashboard and analytics
  - Can create reports
  - View-only mode (no approval/rejection by default, but can be configured)

### 5. ROLE_DAILY_REPORT_TEAM_LEAD
- **Purpose**: Team Lead-level access (view-only or full access)
- **Permissions**:
  - Can view all reports
  - Can view dashboard and analytics
  - Can create reports
  - View-only mode (no approval/rejection by default, but can be configured)

### 6. ROLE_DAILY_REPORT
- **Purpose**: General daily report module access (legacy/compatibility)
- **Permissions**:
  - Basic access to daily report module
  - Can create and submit reports

### 7. ROLE_ADMIN
- **Purpose**: Full administrative access
- **Permissions**:
  - All permissions across all roles
  - Can manage users and roles
  - Full access to all modules

## Implementation Details

### Backend Changes

#### 1. DataLoader.java
- Added new Daily Report roles to default role initialization:
  - `ROLE_DAILY_REPORT_EMPLOYEE`
  - `ROLE_DAILY_REPORT_SUPERVISOR`
  - `ROLE_DAILY_REPORT_DIRECTOR`
  - `ROLE_DAILY_REPORT_MANAGER`
  - `ROLE_DAILY_REPORT_TEAM_LEAD`

#### 2. AdminUserService.java
- Updated `buildRoleDescription()` method to provide specific descriptions for Daily Report roles
- Enhanced role descriptions for better clarity in user management interface

### Frontend Changes

#### 1. DailyReportPermissionService
**Location**: `frontend/src/app/core/services/daily-report-permission.service.ts`

A comprehensive service that provides permission checking methods:

- `canAccessModule()`: Check if user can access the module
- `canCreateReport()`: Check if user can create reports
- `canEditOwnReport()`: Check if user can edit their own reports
- `canReviewReports()`: Check if user can review reports
- `canApproveReports()`: Check if user can approve reports
- `canRejectReports()`: Check if user can reject reports
- `canViewAllReports()`: Check if user can view all reports
- `canViewDashboard()`: Check if user can view dashboard
- `isViewOnly()`: Check if user has view-only access
- `hasFullAccess()`: Check if user has full access
- `canEditReport(reportEmployeeId, isApproved)`: Check if user can edit a specific report
- `canDeleteReport()`: Check if user can delete reports
- `getUserRoleLevel()`: Get user's role level

#### 2. DailyReportGuard
**Location**: `frontend/src/app/core/guards/daily-report.guard.ts`

Route guard that protects Daily Report module routes:
- Checks if user has appropriate role to access the module
- Redirects to unauthorized page if access is denied

#### 3. Role Utilities
**Location**: `frontend/src/app/core/utils/role.utils.ts`

Utility functions for role management:
- Role hierarchy definitions
- Role comparison functions
- Role display name mapping
- Permission checking utilities

#### 4. Route Configuration
**Location**: `frontend/src/app/app.routes.ts`

Updated daily-report route to use `dailyReportGuard`:
```typescript
{
  path: 'daily-report',
  component: DailyReportComponent,
  canActivate: [dailyReportGuard],
  data: { 
    roles: [
      'ROLE_DAILY_REPORT',
      'ROLE_DAILY_REPORT_EMPLOYEE',
      'ROLE_DAILY_REPORT_SUPERVISOR',
      'ROLE_DAILY_REPORT_DIRECTOR',
      'ROLE_DAILY_REPORT_MANAGER',
      'ROLE_DAILY_REPORT_TEAM_LEAD',
      'ROLE_ADMIN'
    ] 
  }
}
```

## Usage Examples

### In Components

```typescript
import { DailyReportPermissionService } from '@core/services/daily-report-permission.service';

export class DailyReportComponent {
  constructor(private permissionService: DailyReportPermissionService) {}

  ngOnInit() {
    if (this.permissionService.canCreateReport()) {
      // Show create report button
    }
    
    if (this.permissionService.canApproveReports()) {
      // Show approve/reject buttons
    }
    
    if (this.permissionService.canViewDashboard()) {
      // Load dashboard data
    }
  }
  
  canEdit(report: DailyReport): boolean {
    return this.permissionService.canEditReport(
      report.employeeId, 
      report.isApproved
    );
  }
}
```

### In Templates

```html
<button 
  *ngIf="permissionService.canCreateReport()" 
  (click)="createReport()">
  Create Report
</button>

<button 
  *ngIf="permissionService.canApproveReports()" 
  (click)="approveReport(report)">
  Approve
</button>

<div *ngIf="permissionService.canViewDashboard()">
  <!-- Dashboard content -->
</div>
```

## Permission Matrix

| Action | Employee | Supervisor | Director | Manager | Team Lead | Admin |
|--------|----------|------------|----------|---------|-----------|-------|
| Create own report | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Edit own report (not approved) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| View own reports | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| View all reports | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Review reports | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Approve reports | ❌ | ✅ | ⚠️* | ⚠️* | ⚠️* | ✅ |
| Reject reports | ❌ | ✅ | ⚠️* | ⚠️* | ⚠️* | ✅ |
| Edit any report | ❌ | ✅ | ⚠️* | ⚠️* | ⚠️* | ✅ |
| Delete reports | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| View dashboard | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |

*⚠️ View-only by default, but can be configured for full access

## Security Considerations

1. **Backend Validation**: All permission checks should be validated on the backend
2. **JWT Tokens**: User roles are included in JWT tokens
3. **Route Protection**: Routes are protected using guards
4. **Method Security**: Backend endpoints should use `@PreAuthorize` annotations
5. **Report Ownership**: Employees can only edit their own reports (unless supervisor/admin)

## Next Steps

When implementing the Daily Report module backend:

1. Add `@PreAuthorize` annotations to controller methods:
   ```java
   @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_ADMIN')")
   @PostMapping("/reports/{id}/approve")
   public ResponseEntity<?> approveReport(@PathVariable Long id) {
       // Implementation
   }
   ```

2. Implement report ownership validation:
   ```java
   private boolean canEditReport(User user, DailyReport report) {
       if (user.getRoles().stream().anyMatch(r -> 
           r.getName().equals("ROLE_ADMIN") || 
           r.getName().equals("ROLE_DAILY_REPORT_SUPERVISOR"))) {
           return true;
       }
       return report.getEmployeeId().equals(user.getId()) && !report.isApproved();
   }
   ```

3. Add audit logging for report approval/rejection actions

## Testing

To test the role-based access:

1. Create test users with different roles
2. Verify each user can only access permitted features
3. Test report creation, editing, and approval workflows
4. Verify unauthorized access attempts are blocked

## Notes

- The `ROLE_DAILY_REPORT` role is kept for backward compatibility
- Directors, Managers, and Team Leads have view-only access by default but can be configured for full access
- All role checks should be performed both on frontend (UX) and backend (security)

