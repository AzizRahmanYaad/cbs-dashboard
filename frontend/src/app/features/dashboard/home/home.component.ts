import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface ModuleCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  stats?: { label: string; value: string };
  requiredRoles?: string[];
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  modules: ModuleCard[] = [
    {
      title: 'CFO Dashboard',
      description: 'View-only access to all modules, reports, dashboards, and analytics',
      icon: 'chart-bar',
      route: '/dashboard/cfo',
      color: 'blue',
      stats: { label: 'View Only', value: 'All modules' },
      requiredRoles: ['ROLE_CFO']
    },
    {
      title: 'Training Module',
      description: 'Manage training programs, schedules, and employee development',
      icon: 'academic-cap',
      route: '/dashboard/training',
      color: 'blue',
      stats: { label: 'Active Programs', value: '12' },
      requiredRoles: ['ROLE_TRAINING']
    },
    {
      title: 'Drill Testing',
      description: 'Conduct and manage safety drills and emergency preparedness tests',
      icon: 'clipboard-check',
      route: '/dashboard/drill-testing',
      color: 'green',
      stats: { label: 'Scheduled Tests', value: '8' },
      requiredRoles: ['ROLE_DRILL_TESTING']
    },
    {
      title: 'Daily Report',
      description: 'Generate and review daily operational reports and analytics',
      icon: 'document',
      route: '/dashboard/daily-report',
      color: 'purple',
      stats: { label: 'Reports Today', value: '24' },
      requiredRoles: ['ROLE_INDIVIDUAL_REPORT', 'ROLE_ADMIN']
    }
  ];

  visibleModules: ModuleCard[] = [];

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (!user) {
        this.visibleModules = [];
        return;
      }
      // Student and Teacher users should only use Training; redirect them from home to their training page
      const isAdminOrTrainingAdmin = this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_TRAINING_ADMIN']);
      if (this.authService.hasAnyRole(['ROLE_STUDENT']) && !isAdminOrTrainingAdmin) {
        this.router.navigate(['/dashboard/training/student-dashboard']);
        return;
      }
      if (this.authService.hasAnyRole(['ROLE_TEACHER']) && !isAdminOrTrainingAdmin) {
        this.router.navigate(['/dashboard/training/teacher-dashboard']);
        return;
      }
      this.visibleModules = this.modules.filter(module =>
        !module.requiredRoles || this.authService.hasAnyRole(module.requiredRoles)
      );
    });
  }
}
