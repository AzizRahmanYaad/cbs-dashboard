# Troubleshooting: Daily Report Module Not Displaying

## Quick Fix Steps

### 1. **RESTART THE BACKEND** (CRITICAL)
The code changes require a backend restart. Stop and restart your Spring Boot application.

### 2. **Check Backend Logs**
After restarting, when you access User Management, you should see in the backend console:

```
=== DEBUG: All roles from database ===
Role: ROLE_INDIVIDUAL_REPORT (ID: 16)
...
=== DEBUG: ROLE_INDIVIDUAL_REPORT found: true
Role: ROLE_INDIVIDUAL_REPORT -> Module: DAILY (Daily Report Module)
=== DEBUG: Module map contents ===
Module: DAILY has 1 roles:
  - ROLE_INDIVIDUAL_REPORT
=== DEBUG: DAILY module exists in map: true
=== DEBUG: Final result ===
Module: DAILY (Daily Report Module) - 1 roles
  DAILY module roles:
    - ROLE_INDIVIDUAL_REPORT
```

### 3. **Check Frontend Console**
Open browser DevTools (F12) → Console tab. You should see:

```
=== FRONTEND DEBUG: Module roles received === [array of modules]
=== FRONTEND DEBUG: DAILY module found === {moduleName: "DAILY", ...}
DAILY module roles: [{name: "ROLE_INDIVIDUAL_REPORT", ...}]
```

### 4. **Test API Directly**
Open browser and go to:
```
http://localhost:8080/api/admin/roles/by-module
```

You should see JSON with a "DAILY" entry like:
```json
{
  "moduleName": "DAILY",
  "moduleDisplayName": "Daily Report Module",
  "roles": [
    {
      "name": "ROLE_INDIVIDUAL_REPORT",
      "description": "Full access to own daily reports (create, edit, view, download)",
      "module": "DAILY"
    }
  ]
}
```

## Common Issues

### Issue 1: Backend Not Restarted
**Symptom**: No DEBUG logs appear
**Solution**: Restart Spring Boot application

### Issue 2: Role Not Found in Database
**Symptom**: Backend logs show "ROLE_INDIVIDUAL_REPORT found: false"
**Solution**: Run this SQL:
```sql
INSERT INTO roles (name) VALUES ('ROLE_INDIVIDUAL_REPORT') ON CONFLICT (name) DO NOTHING;
```

### Issue 3: Role Not Mapped to DAILY
**Symptom**: Backend logs show role mapped to "OTHER" instead of "DAILY"
**Solution**: Check the `extractModuleFromRole` method is working correctly

### Issue 4: Frontend Not Receiving Data
**Symptom**: Frontend console shows error or empty array
**Solution**: 
- Check Network tab for API call to `/api/admin/roles/by-module`
- Verify CORS settings
- Check authentication (must be logged in as admin)

### Issue 5: Module Filtered Out
**Symptom**: Backend shows DAILY module but frontend doesn't
**Solution**: Check if module is being filtered in frontend code

## Verification Checklist

- [ ] Backend restarted
- [ ] Backend logs show ROLE_INDIVIDUAL_REPORT found
- [ ] Backend logs show role mapped to DAILY module
- [ ] API endpoint returns DAILY module in JSON
- [ ] Frontend console shows DAILY module received
- [ ] User Management UI shows "Daily Report Module" section
- [ ] ROLE_INDIVIDUAL_REPORT checkbox appears in the section

## Still Not Working?

1. **Clear browser cache** - Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
2. **Check database** - Verify role exists: `SELECT * FROM roles WHERE name = 'ROLE_INDIVIDUAL_REPORT';`
3. **Check user assignment** - Verify admin has role: `SELECT u.username, r.name FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_INDIVIDUAL_REPORT';`
4. **Check backend logs** - Look for any errors or exceptions
5. **Check frontend console** - Look for JavaScript errors

