import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardShellComponent } from './features/dashboard/dashboard-shell/dashboard-shell.component';
import { HomeComponent } from './features/dashboard/home/home.component';
import { TrainingComponent } from './features/training/training.component';
import { DrillTestingComponent } from './features/drill-testing/drill-testing.component';
import { DailyReportComponent } from './features/daily-report/daily-report.component';

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
    path: 'dashboard',
    component: DashboardShellComponent,
    canActivate: [authGuard],
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
        component: TrainingComponent
      },
      {
        path: 'drill-testing',
        component: DrillTestingComponent
      },
      {
        path: 'daily-report',
        component: DailyReportComponent
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
