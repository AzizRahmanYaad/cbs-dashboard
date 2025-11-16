import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  children?: MenuItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @Output() sidebarToggle = new EventEmitter<boolean>();

  isCollapsed = false;
  
  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'home',
      route: '/dashboard/home'
    },
    {
      label: 'Training Module',
      icon: 'academic-cap',
      route: '/dashboard/training'
    },
    {
      label: 'Drill Testing',
      icon: 'clipboard-document-check',
      route: '/dashboard/drill-testing'
    },
    {
      label: 'Daily Report',
      icon: 'document-text',
      route: '/dashboard/daily-report'
    }
  ];

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
    this.sidebarToggle.emit(this.isCollapsed);
  }
}
