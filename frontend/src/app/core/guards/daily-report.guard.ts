import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { DailyReportPermissionService } from '../services/daily-report-permission.service';

/**
 * Guard to protect Daily Report module routes
 * Ensures user has appropriate role to access the module
 */
export const dailyReportGuard: CanActivateFn = (route, state) => {
  const permissionService = inject(DailyReportPermissionService);
  const router = inject(Router);

  if (permissionService.canAccessModule()) {
    return true;
  }

  // Redirect to unauthorized page or home
  router.navigate(['/unauthorized']);
  return false;
};

