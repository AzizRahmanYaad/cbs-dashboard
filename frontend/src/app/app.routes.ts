import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardShellComponent } from './features/dashboard/dashboard-shell/dashboard-shell.component';
import { HomeComponent } from './features/dashboard/home/home.component';
import { TrainingComponent } from './features/training/training.component';
import { DrillTestingComponent } from './features/drill-testing/drill-testing.component';
import { DailyReportComponent } from './features/daily-report/daily-report.component';
import { UnauthorizedComponent } from './features/auth/unauthorized/unauthorized.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';

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
    component: DashboardShellComponent,
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
        component: HomeComponent
      },
      {
        path: 'training',
        component: TrainingComponent,
        data: { roles: ['ROLE_TRAINING'] }
      },
      {
        path: 'drill-testing',
        component: DrillTestingComponent,
        data: { roles: ['ROLE_DRILL_TESTING'] }
      },
      {
        path: 'daily-report',
        component: DailyReportComponent,
        data: { roles: ['ROLE_DAILY_REPORT'] }
      },
      {
        path: 'admin/users',
        component: UserManagementComponent,
        data: { roles: ['ROLE_ADMIN'] }
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
