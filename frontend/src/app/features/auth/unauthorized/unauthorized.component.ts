import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="unauthorized-container">
      <div class="content">
        <h1>Access Denied</h1>
        <p>You do not have permission to view this section.</p>
        <a routerLink="/dashboard/home" class="link">Return to dashboard</a>
      </div>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      padding: 2rem;
    }
    .content {
      text-align: center;
      background: white;
      padding: 3rem 4rem;
      border-radius: 20px;
      box-shadow: 0 10px 40px rgba(15, 23, 42, 0.08);
    }
    h1 {
      margin: 0 0 1rem;
      font-size: 2.25rem;
      color: #dc2626;
    }
    p {
      margin: 0 0 2rem;
      color: #4b5563;
    }
    .link {
      color: #2563eb;
      font-weight: 600;
    }
  `]
})
export class UnauthorizedComponent {}

