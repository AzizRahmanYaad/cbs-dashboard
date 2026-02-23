import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface ModuleCard {
  title: string;
  description: string;
  route: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-cfo-unified-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cfo-unified-dashboard.component.html',
  styleUrls: ['./cfo-unified-dashboard.component.scss']
})
export class CfoUnifiedDashboardComponent {
  readonly viewOnlyNotice = 'Your access is view-only. You can view, filter, search, and export reports (PDF/Excel). You cannot create, edit, delete, or approve records.';

  modules: ModuleCard[] = [
    {
      title: 'Training Analytics',
      description: 'Executive view of training performance, attendance, and efficiency across the organization.',
      route: '/dashboard/training/cfo-dashboard',
      icon: 'chart-bar',
      color: 'blue'
    },
    {
      title: 'Training Reports',
      description: 'Session-based and date-range attendance reports. View and export PDF for audit.',
      route: '/dashboard/training/teacher-dashboard',
      icon: 'document-chart-bar',
      color: 'indigo'
    },
    {
      title: 'Daily Report',
      description: 'View and export daily operational reports and dashboard analytics.',
      route: '/dashboard/daily-report',
      icon: 'document-text',
      color: 'purple'
    },
    {
      title: 'Drill Test',
      description: 'View drill testing and emergency preparedness tracking and reports.',
      route: '/dashboard/drill-testing',
      icon: 'clipboard-document-check',
      color: 'green'
    }
  ];
}
