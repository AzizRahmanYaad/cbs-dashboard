import {
  Directive,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import ApexCharts from 'apexcharts';
import type { ApexOptions } from 'apexcharts';

@Directive({
  selector: '[appApexChart]',
  standalone: true,
})
export class ApexChartDirective implements OnInit, OnChanges, OnDestroy {
  @Input({ required: true }) appApexChart!: ApexOptions | null;

  private chart: ApexCharts | null = null;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    this.render();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['appApexChart'] && !changes['appApexChart'].firstChange) {
      this.update();
    }
  }

  ngOnDestroy(): void {
    this.destroy();
  }

  private hasData(opts: ApexOptions | null): boolean {
    if (!opts?.series) return false;
    const s = opts.series;
    if (Array.isArray(s) && s.length > 0) {
      if (typeof s[0] === 'number') return (s as number[]).some(v => v > 0);
      return true;
    }
    return false;
  }

  private async render(): Promise<void> {
    const opts = this.appApexChart;
    if (!opts || !this.hasData(opts)) {
      this.el.nativeElement.innerHTML = '<div class="chart-empty" style="padding:2rem;text-align:center;color:#94a3b8">No data</div>';
      return;
    }
    this.destroy();
    this.el.nativeElement.innerHTML = '';
    const container = document.createElement('div');
    const h = opts.chart?.height;
    container.style.minHeight = typeof h === 'number' ? `${h}px` : '280px';
    this.el.nativeElement.appendChild(container);
    this.chart = new ApexCharts(container, opts);
    await this.chart.render();
  }

  private async update(): Promise<void> {
    if (!this.chart) {
      this.render();
      return;
    }
    const opts = this.appApexChart;
    if (!opts) return;
    await this.chart.updateOptions(opts);
  }

  private destroy(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }
}
