import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-drill-testing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="module-placeholder">
      <div class="placeholder-icon">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" d="M11.35 3.836c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m8.9-4.414c.376.023.75.05 1.124.08 1.131.094 1.976 1.057 1.976 2.192V16.5A2.25 2.25 0 0118 18.75h-2.25m-7.5-10.5H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V18.75m-7.5-10.5h6.375c.621 0 1.125.504 1.125 1.125v9.25m-12 0A2.25 2.25 0 005.25 18.75h13.5A2.25 2.25 0 0021 16.5v-13.5" />
        </svg>
      </div>
      <h1>Drill Testing Module</h1>
      <p>This module is coming soon! Conduct and manage safety drills and emergency preparedness tests.</p>
    </div>
  `,
  styles: [`
    .module-placeholder {
      text-align: center;
      padding: 4rem 2rem;
      background: white;
      border-radius: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
    .placeholder-icon {
      width: 120px;
      height: 120px;
      margin: 0 auto 2rem;
      background: linear-gradient(135deg, #4caf50, #388e3c);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      svg {
        width: 60px;
        height: 60px;
        color: white;
      }
    }
    h1 {
      font-size: 2rem;
      color: #1f2937;
      margin: 0 0 1rem 0;
    }
    p {
      font-size: 1.1rem;
      color: #6b7280;
      margin: 0;
    }
  `]
})
export class DrillTestingComponent {}
