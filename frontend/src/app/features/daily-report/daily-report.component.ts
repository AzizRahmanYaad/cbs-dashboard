import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-daily-report',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="module-placeholder">
      <div class="placeholder-icon">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
        </svg>
      </div>
      <h1>Daily Report Module</h1>
      <p>This module is coming soon! Generate and review daily operational reports and analytics.</p>
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
      background: linear-gradient(135deg, #9c27b0, #7b1fa2);
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
export class DailyReportComponent {}
