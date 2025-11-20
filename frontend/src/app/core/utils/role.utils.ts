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
  EMPLOYEE: 'ROLE_DAILY_REPORT_EMPLOYEE',
  SUPERVISOR: 'ROLE_DAILY_REPORT_SUPERVISOR',
  DIRECTOR: 'ROLE_DAILY_REPORT_DIRECTOR',
  MANAGER: 'ROLE_DAILY_REPORT_MANAGER',
  TEAM_LEAD: 'ROLE_DAILY_REPORT_TEAM_LEAD',
  ADMIN: 'ROLE_ADMIN',
  GENERAL: 'ROLE_DAILY_REPORT'
} as const;

/**
 * Role hierarchy for Daily Report module
 * Higher number = more permissions
 */
export const ROLE_HIERARCHY: Record<string, number> = {
  [DAILY_REPORT_ROLES.EMPLOYEE]: 1,
  [DAILY_REPORT_ROLES.GENERAL]: 1,
  [DAILY_REPORT_ROLES.TEAM_LEAD]: 2,
  [DAILY_REPORT_ROLES.MANAGER]: 3,
  [DAILY_REPORT_ROLES.DIRECTOR]: 3,
  [DAILY_REPORT_ROLES.SUPERVISOR]: 4,
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
    [DAILY_REPORT_ROLES.EMPLOYEE]: 'Employee',
    [DAILY_REPORT_ROLES.SUPERVISOR]: 'Supervisor',
    [DAILY_REPORT_ROLES.DIRECTOR]: 'Director',
    [DAILY_REPORT_ROLES.MANAGER]: 'Manager',
    [DAILY_REPORT_ROLES.TEAM_LEAD]: 'Team Lead',
    [DAILY_REPORT_ROLES.ADMIN]: 'Administrator',
    [DAILY_REPORT_ROLES.GENERAL]: 'Daily Report User'
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

