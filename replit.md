# CBS Dashboard - Unified Enterprise Application

## Overview
A unified dashboard application built with **Spring Boot 3.5** (backend) + **Angular** (frontend) + **PostgreSQL** (database) featuring JWT-based Single Sign-On (SSO) authentication. The system supports multiple modules accessed through centralized authentication:
- Training Module
- Drill Testing Module  
- Daily Report Module

**Current Phase**: Angular Frontend ✅ Complete & Running

## Recent Changes
- **2025-11-16 (Latest)**: Completed Angular 18 frontend with professional UI
  - Built complete authentication system with JWT integration
  - Created professional glassmorphism login page
  - Implemented dashboard shell with sidebar navigation and header
  - Added module placeholders (Training, Drill Testing, Daily Report)
  - Configured routing with lazy loading and auth guards
  - Set up HTTP interceptor for automatic token injection
  - Integrated Angular Material 18 and Tailwind CSS
  - Configured dev server proxy to Spring Boot backend
  - Frontend running on port 5000 (Replit webview compatible)

- **2025-11-16**: Completed production-ready Spring Boot backend with JWT authentication
  - Implemented JPA entities (User, Role) with ManyToMany relationship
  - Created JWT security infrastructure (token provider, filter, security config)
  - Built Auth API endpoints (login, refresh token, current user)
  - Added unified error handling with proper HTTP status codes
  - Seeded admin user (username: admin, password: admin123)
  - Database configured via Replit PostgreSQL environment variables

## Project Architecture

### Backend Structure (Spring Boot)
```
src/main/java/com/example/CBS/Dashboard/
├── config/
│   ├── SecurityConfig.java          # Spring Security + JWT configuration
│   └── DataLoader.java               # Database seeding (admin user)
├── controller/
│   └── auth/
│       └── AuthController.java       # Authentication endpoints
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── RefreshTokenRequest.java
│   └── user/
│       └── UserDto.java
├── entity/
│   ├── User.java                     # User entity with roles
│   └── Role.java                     # Role entity
├── exception/
│   ├── GlobalExceptionHandler.java   # Unified error handling
│   └── InvalidRefreshTokenException.java
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── security/
│   ├── JwtTokenProvider.java         # JWT generation & validation
│   └── JwtAuthenticationFilter.java  # Request filter
└── service/
    ├── auth/
    │   └── AuthService.java          # Authentication business logic
    └── user/
        ├── UserService.java
        └── CustomUserDetailsService.java
```

### Database Schema
**PostgreSQL** (via Replit Neon)

**users** table:
- id (primary key)
- username (unique)
- email
- password (BCrypt hashed)
- enabled
- created_at

**roles** table:
- id (primary key)
- name (ROLE_ADMIN, ROLE_USER)

**users_roles** (join table):
- user_id
- role_id

### API Endpoints

#### Authentication
- **POST** `/api/auth/login`
  - Request: `{"username": "admin", "password": "admin123"}`
  - Response: `{"accessToken": "...", "refreshToken": "...", "expiresIn": 3600000, "tokenType": "Bearer"}`
  - Status: 200 OK / 401 Unauthorized

- **POST** `/api/auth/refresh`
  - Request: `{"refreshToken": "..."}`
  - Response: `{"accessToken": "...", "refreshToken": "...", "expiresIn": 3600000, "tokenType": "Bearer"}`
  - Status: 200 OK / 401 Unauthorized

- **GET** `/api/auth/me`
  - Headers: `Authorization: Bearer {accessToken}`
  - Response: `{"id": 1, "username": "admin", "email": "admin@cbsdashboard.com", "roles": ["ROLE_ADMIN"]}`
  - Status: 200 OK / 401 Unauthorized

### Error Response Format
All API errors return consistent JSON:
```json
{
  "status": 401,
  "message": "Invalid or expired refresh token",
  "timestamp": "2025-11-16T05:40:00"
}
```

Validation errors include field details:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-11-16T05:40:00",
  "fieldErrors": {
    "username": "must not be blank"
  }
}
```

## Configuration

### Environment Variables (Auto-configured by Replit)
- `DATABASE_URL` - PostgreSQL connection string
- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`

### application.properties
```properties
server.port=5000
server.address=0.0.0.0
spring.application.name=CBS-Dashboard

# PostgreSQL (using Replit env vars)
spring.datasource.url=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-in-production
jwt.expiration=3600000
jwt.refresh.expiration=86400000
```

## Startup Instructions

### Backend (Spring Boot)
The application runs on **port 5000** and takes approximately **10-11 seconds** to start.

**Method 1: Use the Replit Workflow** (Recommended)
- Click the "Run" button in Replit
- Wait 10-11 seconds for "Started CbsDashboardApplication" message

**Method 2: Manual Start**
```bash
./gradlew bootRun
```

**Note**: The workflow may show "failed" status due to the ~10s startup time exceeding Replit's default timeout, but the application is fully functional once started.

### Build & Test
```bash
# Build the project
./gradlew build

# Build without tests
./gradlew build -x test

# Clean build
./gradlew clean build
```

## Default Credentials
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@cbsdashboard.com`
- **Role**: `ROLE_ADMIN`

## Technology Stack
- **Backend**: Spring Boot 3.5.7
- **Security**: Spring Security 6.x + JWT (io.jsonwebtoken)
- **Database**: PostgreSQL 16.9 (Neon via Replit)
- **ORM**: JPA/Hibernate 6.6.33
- **Build Tool**: Gradle 8.11.1
- **Java Version**: 19

## Next Steps
1. ✅ **Phase 1 Complete**: Spring Boot backend with JWT authentication
2. 🔄 **Phase 2 (Next)**: Angular frontend
   - Set up Angular project with Replit configuration
   - Create auth module (login, guards, interceptors)
   - Build dashboard shell with navigation
   - Add module placeholders (Training, Drill Testing, Daily Report)
   - Configure CORS in Spring Boot
   - Integrate frontend with backend APIs
3. 📋 **Phase 3**: Module development
   - Training module implementation
   - Drill testing module implementation
   - Daily report module implementation

## User Preferences
- Technology choices: Spring Boot + Angular + PostgreSQL (per user requirements)
- Build tool: Gradle (user specified)
- ORM: JPA (user specified)
- Authentication: JWT-based stateless authentication with refresh tokens
- Database: Replit's built-in PostgreSQL (Neon-backed)
- Port: Backend on 5000 (frontend will also serve from 5000 when integrated)

## Project Goals
Build a unified enterprise dashboard with:
- Centralized authentication (SSO via JWT)
- Multiple business modules accessed through single dashboard
- Role-based access control
- Clean architecture with separation of concerns
- Production-ready error handling
- Scalable and maintainable codebase
