# CBS Dashboard API Documentation

## Base URL
```
http://0.0.0.0:5000
```

## Authentication Endpoints

### 1. Login
Authenticate user and receive access token and refresh token.

**Endpoint**: `POST /api/auth/login`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Success Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000,
  "tokenType": "Bearer"
}
```

**Error Response (401 Unauthorized)**:
```json
{
  "status": 401,
  "message": "Invalid username or password",
  "timestamp": "2025-11-16T05:40:00"
}
```

**Error Response (400 Bad Request)** - Validation Failed:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-11-16T05:40:00",
  "fieldErrors": {
    "username": "must not be blank",
    "password": "must not be blank"
  }
}
```

**cURL Example**:
```bash
curl -X POST http://0.0.0.0:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

### 2. Refresh Token
Get new access token and refresh token using existing refresh token.

**Endpoint**: `POST /api/auth/refresh`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Success Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000,
  "tokenType": "Bearer"
}
```

**Error Response (401 Unauthorized)**:
```json
{
  "status": 401,
  "message": "Invalid or expired refresh token",
  "timestamp": "2025-11-16T05:40:00"
}
```

**cURL Example**:
```bash
curl -X POST http://0.0.0.0:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

---

### 3. Get Current User
Retrieve currently authenticated user's information.

**Endpoint**: `GET /api/auth/me`

**Request Headers**:
```
Authorization: Bearer {accessToken}
```

**Success Response (200 OK)**:
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@cbsdashboard.com",
  "roles": ["ROLE_ADMIN"]
}
```

**Error Response (401 Unauthorized)** - Missing or invalid token:
```json
{
  "status": 401,
  "message": "Unauthorized",
  "timestamp": "2025-11-16T05:40:00"
}
```

**Error Response (404 Not Found)** - User not found:
```json
{
  "status": 404,
  "message": "User not found with username: xyz",
  "timestamp": "2025-11-16T05:40:00"
}
```

**cURL Example**:
```bash
curl -X GET http://0.0.0.0:5000/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## JWT Token Details

### Access Token
- **Purpose**: Authenticate API requests
- **Expiration**: 1 hour (3600000 ms)
- **Usage**: Include in `Authorization` header as `Bearer {token}`
- **Format**: JWT with claims (username, roles, expiration)

### Refresh Token
- **Purpose**: Obtain new access tokens without re-login
- **Expiration**: 24 hours (86400000 ms)
- **Usage**: Send to `/api/auth/refresh` endpoint
- **Format**: JWT with username and expiration

---

## HTTP Status Codes

| Code | Description | Use Case |
|------|-------------|----------|
| 200  | OK | Successful request |
| 400  | Bad Request | Validation errors, malformed request |
| 401  | Unauthorized | Invalid credentials, expired/invalid token |
| 404  | Not Found | Resource (user) not found |
| 500  | Internal Server Error | Unexpected server error |

---

## Error Response Format

All errors follow a consistent structure:

**Standard Error**:
```json
{
  "status": <HTTP_STATUS_CODE>,
  "message": "<ERROR_MESSAGE>",
  "timestamp": "<ISO_8601_TIMESTAMP>"
}
```

**Validation Error** (includes field-specific errors):
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-11-16T05:40:00",
  "fieldErrors": {
    "fieldName": "error message"
  }
}
```

---

## Complete Authentication Flow Example

### 1. Login and Store Tokens
```bash
# Login
curl -X POST http://0.0.0.0:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response:
# {
#   "accessToken": "eyJhbGci...",
#   "refreshToken": "eyJhbGci...",
#   "expiresIn": 3600000,
#   "tokenType": "Bearer"
# }
```

### 2. Access Protected Endpoint
```bash
# Get current user info
curl -X GET http://0.0.0.0:5000/api/auth/me \
  -H "Authorization: Bearer eyJhbGci..."

# Response:
# {
#   "id": 1,
#   "username": "admin",
#   "email": "admin@cbsdashboard.com",
#   "roles": ["ROLE_ADMIN"]
# }
```

### 3. Refresh Access Token (when expired)
```bash
# Refresh token
curl -X POST http://0.0.0.0:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGci..."}'

# Response:
# {
#   "accessToken": "newToken...",
#   "refreshToken": "newRefreshToken...",
#   "expiresIn": 3600000,
#   "tokenType": "Bearer"
# }
```

---

## Default Test Account

**Credentials**:
- Username: `admin`
- Password: `admin123`
- Email: `admin@cbsdashboard.com`
- Roles: `ROLE_ADMIN`

---

## Security Notes

1. **HTTPS**: In production, always use HTTPS to protect tokens in transit
2. **Token Storage**: Store tokens securely (e.g., httpOnly cookies or secure storage)
3. **Secret Key**: Change `jwt.secret` in production to a strong 256-bit key
4. **CORS**: CORS is currently enabled for all origins (`*`) - restrict in production
5. **Token Expiration**: Access tokens expire in 1 hour, refresh tokens in 24 hours
6. **Password Security**: Passwords are hashed using BCrypt with strength 10
