# CBS Dashboard - Unified Dashboard Application

## Project Overview
A unified dashboard application built with Spring Boot (backend) and Angular (frontend), featuring JWT-based authentication and multiple modules (Training, Drill Testing, Daily Report).

## Technology Stack
- **Backend**: Spring Boot 3.5.7 (Java 19)
- **Frontend**: Angular (to be set up)
- **Database**: PostgreSQL (Replit/Neon managed)
- **Build Tool**: Gradle  
- **Security**: JWT-based authentication (no Keycloak)
- **ORM**: JPA/Hibernate

## Current Architecture

### Backend Structure
```
src/main/java/com/example/CBS/Dashboard/
├── config/
│   ├── SecurityConfig.java - Security configuration (JWT, CORS, stateless sessions)
│   └── DataLoader.java - Seeds admin user and roles on startup
├── controller/auth/
│   └── AuthController.java - Auth endpoints (login, refresh, me)
├── service/
│   ├── auth/AuthService.java - Authentication business logic
│   └── user/
│       ├── UserService.java - User profile management
│       └── CustomUserDetailsService.java - Spring Security UserDetailsService implementation
├── dto/
│   ├── auth/ - LoginRequest, LoginResponse, RefreshTokenRequest
│   └── user/ - UserDto
├── entity/
│   ├── User.java - User entity with roles (ManyToMany)
│   └── Role.java - Role entity
├── repository/
│   ├── UserRepository.java - User data access
│   └── RoleRepository.java - Role data access
└── security/
    ├── JwtTokenProvider.java - JWT token creation/validation
    └── JwtAuthenticationFilter.java - JWT authentication filter

```

## Authentication Endpoints
- **POST /api/auth/login** - Login with username/password, returns JWT tokens
- **POST /api/auth/refresh** - Refresh access token using refresh token
- **GET /api/auth/me** - Get current authenticated user profile (requires JWT)

## Default Credentials
- **Username**: admin
- **Password**: admin123
- **Role**: ROLE_ADMIN

## Database Configuration
- Using Replit's managed PostgreSQL (Neon)
- Connection via individual properties (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)
- Auto-creation of tables via Hibernate DDL
- SSL mode: required

## JWT Configuration
- Access Token Expiration: 24 hours (86400000 ms)
- Refresh Token Expiration: 7 days (604800000 ms)
- Algorithm: HMAC-SHA with secret key

## Recent Changes (Nov 16, 2025)
1. ✅ Set up Spring Boot backend with clean architecture
2. ✅ Created User and Role entities with ManyToMany relationship
3. ✅ Implemented JWT security (provider, filter, security config)
4. ✅ Created auth services and controllers
5. ✅ Database connectivity configured with Replit PostgreSQL
6. ✅ Admin user seeding on first run
7. 🔄 Backend successfully compiles and runs on port 5000

## Pending Tasks
- Set up Angular frontend
- Create login UI component
- Implement JWT interceptor and route guards
- Build main dashboard shell
- Create module placeholders (Training, Drill Testing, Daily Report)
- Deploy configuration

## Notes
- Application runs on port 5000 (bound to 0.0.0.0 for Replit environment)
- CORS enabled for frontend integration
- Stateless session management (no server-side sessions)
- Connection pool warnings from Neon are normal (serverless PostgreSQL closes idle connections)
