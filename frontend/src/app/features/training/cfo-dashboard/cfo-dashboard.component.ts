import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TrainingService } from '../../../core/services/training.service';
import {
  CfoTrainingDashboard,
  CfoSummaryKpis,
  ProgramPerformanceRow,
  TimeSeriesPoint,
  InstructorMetric,
  CategoryValue,
  RiskItem
} from '../../../core/models/training';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-cfo-training-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cfo-dashboard.component.html',
  styleUrls: ['./cfo-dashboard.component.scss']
})
export class CfoTrainingDashboardComponent implements OnInit {
  private trainingService = inject(TrainingService);
  private fb = inject(FormBuilder);
  private toastr = inject(ToastrService);

  filterForm: FormGroup;
  dashboard: CfoTrainingDashboard | null = null;
  loading = false;

  constructor() {
    this.filterForm = this.fb.group({
      from: [''],
      to: ['']
    });
  }

  ngOnInit(): void {
    const today = new Date();
    const fromDefault = new Date();
    fromDefault.setDate(today.getDate() - 90);

    this.filterForm.patchValue({
      from: this.toInputDate(fromDefault),
      to: this.toInputDate(today)
    });

    this.loadDashboard();
  }

  get summary(): CfoSummaryKpis | null {
    return this.dashboard?.summary ?? null;
  }

  get topPrograms(): ProgramPerformanceRow[] {
    if (!this.dashboard?.programPerformance) return [];
    return this.dashboard.programPerformance
      .slice()
      .sort((a, b) => b.attendanceRate - a.attendanceRate)
      .slice(0, 5);
  }

  get underperformingPrograms(): ProgramPerformanceRow[] {
    if (!this.dashboard?.programPerformance) return [];
    return this.dashboard.programPerformance
      .filter(p => p.completionRate < 60 || p.attendanceRate < 60)
      .slice(0, 6);
  }

  get performanceTrend(): TimeSeriesPoint[] {
    return this.dashboard?.performanceTrend ?? [];
  }

  get instructorLeaderboard(): InstructorMetric[] {
    return (this.dashboard?.instructorProductivity ?? []).slice(0, 5);
  }

  get departmentAttendance(): CategoryValue[] {
    return this.dashboard?.departmentAttendance ?? [];
  }

  get risks(): RiskItem[] {
    return this.dashboard?.risks ?? [];
  }

  refresh(): void {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    const { from, to } = this.filterForm.value;
    this.loading = true;
    this.trainingService.getCfoDashboard(from || undefined, to || undefined).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load CFO dashboard', err);
        this.toastr.error(err?.error?.message || err?.message || 'Failed to load CFO dashboard');
        this.loading = false;
      }
    });
  }

  private toInputDate(d: Date): string {
    return d.toISOString().slice(0, 10);
  }

  // ---- UI helpers ----

  getTrendDirection(metric: 'attendance' | 'completion' | 'engagement'): 'up' | 'down' | 'flat' {
    const trend = this.performanceTrend;
    if (!trend || trend.length < 2) return 'flat';
    const last = trend[trend.length - 1];
    const prev = trend[trend.length - 2];
    let lastVal = 0;
    let prevVal = 0;
    if (metric === 'attendance') {
      lastVal = last.attendanceRate;
      prevVal = prev.attendanceRate;
    } else if (metric === 'completion') {
      lastVal = last.completionRate;
      prevVal = prev.completionRate;
    } else {
      lastVal = last.engagementScore;
      prevVal = prev.engagementScore;
    }
    const delta = lastVal - prevVal;
    if (delta > 0.5) return 'up';
    if (delta < -0.5) return 'down';
    return 'flat';
  }

  getRiskSeverityClass(severity: string | null | undefined): string {
    const value = (severity || '').toUpperCase();
    if (value === 'RED') return 'risk-card--red';
    if (value === 'YELLOW') return 'risk-card--yellow';
    return 'risk-card--green';
  }

  /**
   * Safely format a KPI value as percentage with one decimal place.
   */
  asPercent(value: number | null | undefined): string {
    if (value === null || value === undefined || isNaN(value)) return '0%';
    return `${value.toFixed(1)}%`;
  }
}

