# Daily Report Role Setup - ROLE_INDIVIDUAL_REPORT

## Summary
The Daily Report module now uses a single role: **ROLE_INDIVIDUAL_REPORT**

## Verification Steps

### 1. Database Verification
Run this SQL to verify the role exists:
```sql
SELECT id, name FROM roles WHERE name = 'ROLE_INDIVIDUAL_REPORT';
```

### 2. User Assignment Verification
Run this SQL to verify users have the role:
```sql
SELECT u.id, u.username, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ROLE_INDIVIDUAL_REPORT';
```

### 3. Backend Verification
The role should appear in the User Management module under:
- **Module**: DAILY
- **Display Name**: Daily Report Module
- **Role**: ROLE_INDIVIDUAL_REPORT
- **Description**: Full access to own daily reports (create, edit, view, download)

### 4. Frontend Verification
In the User Management module:
1. Go to User Management
2. Click on "DAILY" module section
3. You should see "ROLE_INDIVIDUAL_REPORT" checkbox
4. When editing a user (like admin), the checkbox should be checked if they have the role

## Troubleshooting

### If the role doesn't appear in User Management:

1. **Restart the backend application** - Code changes require a restart
2. **Clear browser cache** - Refresh the frontend
3. **Verify the role exists in database** - Run the verification SQL above
4. **Check backend logs** - Look for any errors when calling `/api/admin/roles/by-module`

### If admin user doesn't show the role:

1. **Verify assignment** - Run the user assignment SQL above
2. **Check user edit form** - When editing admin, the role checkbox should be checked
3. **Verify role is in user.roles** - The UserDto should include ROLE_INDIVIDUAL_REPORT in the roles set

## Code Changes Made

1. ✅ **DataLoader.java** - Only creates ROLE_INDIVIDUAL_REPORT (removed old roles)
2. ✅ **AdminUserService.java** - Maps ROLE_INDIVIDUAL_REPORT to "DAILY" module
3. ✅ **Frontend permission service** - Updated to use ROLE_INDIVIDUAL_REPORT
4. ✅ **All controllers** - Updated authorization to use new role structure

## Expected Behavior

- Users with `ROLE_INDIVIDUAL_REPORT` can:
  - ✅ Create their own daily reports
  - ✅ Edit their own daily reports
  - ✅ View their own daily reports
  - ✅ Download their own daily reports (FIXED)

- The role appears in User Management under "Daily Report Module" section
- Only one role exists for Daily Report module: ROLE_INDIVIDUAL_REPORT

