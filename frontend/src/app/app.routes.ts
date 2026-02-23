import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { dailyReportGuard } from './core/guards/daily-report.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { UnauthorizedComponent } from './features/auth/unauthorized/unauthorized.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'unauthorized',
    component: UnauthorizedComponent
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard-shell/dashboard-shell.component').then(
        (m) => m.DashboardShellComponent
      ),
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./features/dashboard/home/home.component').then((m) => m.HomeComponent)
      },
      {
        path: 'cfo',
        loadComponent: () =>
          import('./features/cfo-unified-dashboard/cfo-unified-dashboard.component').then(
            (m) => m.CfoUnifiedDashboardComponent
          ),
        data: { roles: ['ROLE_CFO'] }
      },
      {
        path: 'training',
        loadComponent: () =>
          import('./features/training/training-admin-settings/training-admin-settings.component').then(
            (m) => m.TrainingAdminSettingsComponent
          ),
        data: { roles: ['ROLE_TRAINING', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN'] }
      },
      {
        path: 'training/admin',
        redirectTo: 'training',
        pathMatch: 'full'
      },
      {
        path: 'training/master-settings',
        loadComponent: () =>
          import('./features/training/training-master-settings/training-master-settings.component').then(
            (m) => m.TrainingMasterSettingsComponent
          ),
        data: { roles: ['ROLE_TRAINING_ADMIN', 'ROLE_ADMIN'] }
      },
      {
        path: 'training/teacher-dashboard',
        loadComponent: () =>
          import('./features/training/teacher-dashboard/teacher-dashboard.component').then(
            (m) => m.TeacherDashboardComponent
          ),
        data: { roles: ['ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO'] }
      },
      {
        path: 'training/student-dashboard',
        loadComponent: () =>
          import('./features/training/student-dashboard/student-dashboard.component').then(
            (m) => m.StudentDashboardComponent
          ),
        data: { roles: ['ROLE_STUDENT', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO'] }
      },
      {
        path: 'training/cfo-dashboard',
        loadComponent: () =>
          import('./features/training/cfo-dashboard/cfo-dashboard.component').then(
            (m) => m.CfoTrainingDashboardComponent
          ),
        data: { roles: ['ROLE_CFO'] }
      },
      {
        path: 'drill-testing',
        loadComponent: () =>
          import('./features/drill-testing/drill-testing.component').then(
            (m) => m.DrillTestingComponent
          ),
        data: { roles: ['ROLE_DRILL_TESTING', 'ROLE_QA_LEAD', 'ROLE_TESTER', 'ROLE_MANAGER', 'ROLE_CFO'] }
      },
      {
        path: 'daily-report',
        loadComponent: () =>
          import('./features/daily-report/daily-report.component').then(
            (m) => m.DailyReportComponent
          ),
        canActivate: [dailyReportGuard],
        data: {
          roles: [
            'ROLE_INDIVIDUAL_REPORT',
            'ROLE_ADMIN',
            'ROLE_QUALITY_CONTROL',
            'ROLE_CFO'
          ]
        }
      },
      {
        path: 'admin/users',
        loadComponent: () =>
          import('./features/admin/user-management/user-management.component').then(
            (m) => m.UserManagementComponent
          ),
        data: { roles: ['ROLE_ADMIN'] }
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
