import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface ModuleCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  stats?: { label: string; value: string };
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  modules: ModuleCard[] = [
    {
      title: 'Training Module',
      description: 'Manage training programs, schedules, and employee development',
      icon: 'academic-cap',
      route: '/dashboard/training',
      color: 'blue',
      stats: { label: 'Active Programs', value: '12' }
    },
    {
      title: 'Drill Testing',
      description: 'Conduct and manage safety drills and emergency preparedness tests',
      icon: 'clipboard-check',
      route: '/dashboard/drill-testing',
      color: 'green',
      stats: { label: 'Scheduled Tests', value: '8' }
    },
    {
      title: 'Daily Report',
      description: 'Generate and review daily operational reports and analytics',
      icon: 'document',
      route: '/dashboard/daily-report',
      color: 'purple',
      stats: { label: 'Reports Today', value: '24' }
    }
  ];
}
