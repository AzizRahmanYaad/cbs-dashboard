# API Endpoints for Postman Testing

## Base Configuration
- **Base URL**: `http://72.61.116.191:8090` (or `http://localhost:8090` for local)
- **Port**: `8090`
- **Authentication**: JWT Bearer Token (required for all endpoints except `/api/auth/**`)

## Authentication

### 1. Login (Get Token)
**POST** `/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}
```

### 2. Get Current User
**GET** `/api/auth/me`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Test Module Endpoints

### 1. Create Module
**POST** `/api/test/modules`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Module 1",
  "description": "Test module description"
}
```

### 2. Get All Modules
**GET** `/api/test/modules`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 3. Get Module by ID
**GET** `/api/test/modules/{id}`

**Example:** `/api/test/modules/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 4. Update Module
**PUT** `/api/test/modules/{id}`

**Example:** `/api/test/modules/1`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Updated Module Name",
  "description": "Updated description"
}
```

### 5. Delete Module
**DELETE** `/api/test/modules/{id}`

**Example:** `/api/test/modules/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Test Case Endpoints

### 1. Create Test Case
**POST** `/api/test/test-cases`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Test Case 1",
  "preconditions": "Preconditions here",
  "steps": ["Step 1", "Step 2", "Step 3"],
  "expectedResult": "Expected result",
  "priority": "HIGH",
  "moduleId": 1,
  "assignedToId": 1
}
```

**Priority values:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

### 2. Get All Test Cases
**GET** `/api/test/test-cases`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Optional Query Parameters:**
- `moduleId` - Filter by module ID
- `status` - Filter by status (DRAFT, APPROVED, ARCHIVED)
- `assignedToId` - Filter by assigned user ID
- `priority` - Filter by priority (LOW, MEDIUM, HIGH, CRITICAL)

**Example:** `/api/test/test-cases?moduleId=1&status=APPROVED`

### 3. Search Test Cases
**GET** `/api/test/test-cases/search?searchTerm=test`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 4. Get Test Case by ID
**GET** `/api/test/test-cases/{id}`

**Example:** `/api/test/test-cases/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 5. Update Test Case
**PUT** `/api/test/test-cases/{id}`

**Example:** `/api/test/test-cases/1`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Updated Test Case",
  "status": "APPROVED",
  "priority": "MEDIUM"
}
```

### 6. Delete Test Case
**DELETE** `/api/test/test-cases/{id}`

**Example:** `/api/test/test-cases/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Test Execution Endpoints

### 1. Create Test Execution
**POST** `/api/test/executions`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "testCaseId": 1,
  "status": "PASSED",
  "comments": "Execution comments",
  "attachments": ["file1.pdf", "file2.png"]
}
```

**Status values:** `PASSED`, `FAILED`, `BLOCKED`, `RETEST`

### 2. Get All Executions
**GET** `/api/test/executions`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Optional Query Parameters:**
- `testCaseId` - Filter by test case ID
- `userId` - Filter by user ID
- `status` - Filter by status (PASSED, FAILED, BLOCKED, RETEST)

**Example:** `/api/test/executions?testCaseId=1&status=PASSED`

### 3. Get Execution by ID
**GET** `/api/test/executions/{id}`

**Example:** `/api/test/executions/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Defect Endpoints

### 1. Create Defect
**POST** `/api/test/defects`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Defect Title",
  "description": "Defect description",
  "severity": "HIGH",
  "testCaseId": 1,
  "testExecutionId": 1,
  "assignedToId": 1
}
```

**Severity values:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

### 2. Get All Defects
**GET** `/api/test/defects`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Optional Query Parameters:**
- `status` - Filter by status (NEW, IN_PROGRESS, RESOLVED, CLOSED)
- `severity` - Filter by severity (LOW, MEDIUM, HIGH, CRITICAL)
- `assignedToId` - Filter by assigned user ID

**Example:** `/api/test/defects?status=NEW&severity=HIGH`

### 3. Get Defect by ID
**GET** `/api/test/defects/{id}`

**Example:** `/api/test/defects/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 4. Update Defect
**PUT** `/api/test/defects/{id}`

**Example:** `/api/test/defects/1`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Updated Defect",
  "status": "IN_PROGRESS",
  "severity": "CRITICAL"
}
```

### 5. Delete Defect
**DELETE** `/api/test/defects/{id}`

**Example:** `/api/test/defects/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Comment Endpoints

### 1. Create Comment
**POST** `/api/test/comments`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body (for Test Case):**
```json
{
  "content": "This is a comment",
  "testCaseId": 1
}
```

**Request Body (for Defect):**
```json
{
  "content": "This is a comment",
  "defectId": 1
}
```

### 2. Get Comments by Test Case
**GET** `/api/test/comments/test-case/{testCaseId}`

**Example:** `/api/test/comments/test-case/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 3. Get Comments by Defect
**GET** `/api/test/comments/defect/{defectId}`

**Example:** `/api/test/comments/defect/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 4. Delete Comment
**DELETE** `/api/test/comments/{id}`

**Example:** `/api/test/comments/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Test Report Endpoints

### 1. Generate Report
**GET** `/api/test/reports`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Optional Query Parameter:**
- `moduleId` - Filter report by module ID

**Example:** `/api/test/reports?moduleId=1`

---

## Admin User Endpoints (Requires ADMIN Role)

### 1. Get All Users
**GET** `/api/admin/users`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Note:** Requires ADMIN role. Returns 403 if user doesn't have ADMIN role.

### 2. Get User by ID
**GET** `/api/admin/users/{id}`

**Example:** `/api/admin/users/1`

**Headers:**
```
Authorization: Bearer {accessToken}
```

### 3. Create User
**POST** `/api/admin/users`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "roleIds": [1, 2]
}
```

### 4. Update User
**PUT** `/api/admin/users/{id}`

**Example:** `/api/admin/users/1`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "updated@example.com",
  "roleIds": [1]
}
```

### 5. Get All Roles
**GET** `/api/admin/roles`

**Headers:**
```
Authorization: Bearer {accessToken}
```

---

## Postman Collection Setup

### Step 1: Create Environment Variables
In Postman, create an environment with:
- `baseUrl`: `http://72.61.116.191:8090` (or `http://localhost:8090`)
- `accessToken`: (will be set after login)

### Step 2: Login First
1. Create a POST request to `{{baseUrl}}/api/auth/login`
2. Use the request body shown above
3. In the Tests tab, add:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("accessToken", jsonData.accessToken);
}
```

### Step 3: Use Token in Other Requests
For all other requests, add header:
```
Authorization: Bearer {{accessToken}}
```

### Step 4: Test the Failing Endpoints
Test these endpoints that were giving 500 errors:
1. `GET {{baseUrl}}/api/test/modules`
2. `GET {{baseUrl}}/api/test/test-cases`
3. `GET {{baseUrl}}/api/test/executions`
4. `GET {{baseUrl}}/api/test/defects`
5. `GET {{baseUrl}}/api/test/reports`
6. `GET {{baseUrl}}/api/admin/users` (will give 403 if not ADMIN)

---

## Common Error Responses

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Authentication required",
  "timestamp": "2025-01-XX..."
}
```

### 403 Forbidden
```json
{
  "status": 403,
  "message": "Access denied",
  "timestamp": "2025-01-XX..."
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2025-01-XX..."
}
```

### Validation Error (400)
```json
{
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": {
    "name": "Name is required",
    "title": "Title must be at least 3 characters"
  },
  "timestamp": "2025-01-XX..."
}
```

---

## Quick Test Checklist

After logging in, test these endpoints in order:

1. ✅ `GET /api/test/modules` - Should return empty array or list of modules
2. ✅ `POST /api/test/modules` - Create a module first
3. ✅ `GET /api/test/test-cases` - Should return list (may be empty)
4. ✅ `GET /api/test/executions` - Should return list (may be empty)
5. ✅ `GET /api/test/defects` - Should return list (may be empty)
6. ✅ `GET /api/test/reports` - Should return report object
7. ⚠️ `GET /api/admin/users` - Will return 403 if not ADMIN role (this is expected)

If any of these return 500, check the backend logs for the actual error message.

