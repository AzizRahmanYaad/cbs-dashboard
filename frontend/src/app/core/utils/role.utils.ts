/**
 * Utility functions for role-based access control
 */

export interface RolePermission {
  role: string;
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
  canApprove: boolean;
  canViewAll: boolean;
  isViewOnly: boolean;
}

/**
 * Daily Report role definitions
 */
export const DAILY_REPORT_ROLES = {
  INDIVIDUAL_REPORT: 'ROLE_INDIVIDUAL_REPORT',
  ADMIN: 'ROLE_ADMIN'
} as const;

/**
 * Role hierarchy for Daily Report module
 * Higher number = more permissions
 */
export const ROLE_HIERARCHY: Record<string, number> = {
  [DAILY_REPORT_ROLES.INDIVIDUAL_REPORT]: 1,
  [DAILY_REPORT_ROLES.ADMIN]: 5
};

/**
 * Get role level from hierarchy
 */
export function getRoleLevel(role: string): number {
  return ROLE_HIERARCHY[role] || 0;
}

/**
 * Check if role1 has higher or equal permissions than role2
 */
export function hasHigherOrEqualRole(role1: string, role2: string): boolean {
  return getRoleLevel(role1) >= getRoleLevel(role2);
}

/**
 * Get role display name
 */
export function getRoleDisplayName(role: string): string {
  const roleMap: Record<string, string> = {
    [DAILY_REPORT_ROLES.INDIVIDUAL_REPORT]: 'Individual Report',
    [DAILY_REPORT_ROLES.ADMIN]: 'Administrator'
  };
  
  return roleMap[role] || role.replace('ROLE_', '').replace(/_/g, ' ');
}

/**
 * Check if user has any of the specified roles
 */
export function hasAnyRole(userRoles: string[], requiredRoles: string[]): boolean {
  if (!userRoles || !requiredRoles || requiredRoles.length === 0) {
    return false;
  }
  return requiredRoles.some(role => userRoles.includes(role));
}

/**
 * Check if user has all of the specified roles
 */
export function hasAllRoles(userRoles: string[], requiredRoles: string[]): boolean {
  if (!userRoles || !requiredRoles || requiredRoles.length === 0) {
    return false;
  }
  return requiredRoles.every(role => userRoles.includes(role));
}

