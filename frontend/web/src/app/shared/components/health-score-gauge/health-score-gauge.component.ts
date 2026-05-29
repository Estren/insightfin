import { Component, computed, effect, Inject, input, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ApexChart, ApexFill, ApexPlotOptions, ApexStroke, NgApexchartsModule } from 'ng-apexcharts';
import { HealthScoreMetadata } from '../../../core/models/health-score.model';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-health-score-gauge',
  templateUrl: './health-score-gauge.component.html',
  imports: [TranslateModule, NgApexchartsModule],
})
export class HealthScoreGaugeComponent {
  data = input.required<HealthScoreMetadata>();

  // The gauge follows the theme's --primary (read live on theme change) so it
  // stays within the palette instead of using traffic-light colors. The score
  // value and band label communicate the level.
  private readonly primary = signal<string>('#2490FF');

  readonly series = computed(() => [this.data().score]);
  readonly labelKey = computed(() => bandLabelKey(this.data().score));

  // Static — the dynamic bits (score value, color) come from inputs/computed.
  readonly chart: ApexChart = {
    type: 'radialBar',
    height: 240,
    fontFamily: 'Poppins, sans-serif',
    sparkline: { enabled: true },
  };

  readonly stroke: ApexStroke = { lineCap: 'round' };

  readonly plotOptions = computed<ApexPlotOptions>(() => ({
    radialBar: {
      hollow: { size: '62%' },
      track: { background: '#e5e7eb', strokeWidth: '100%' },
      dataLabels: {
        name: { show: false },
        value: {
          show: true,
          fontSize: '36px',
          fontWeight: 700,
          color: this.primary(),
          offsetY: 8,
          formatter: (val) => `${Math.round(Number(val))}`,
        },
      },
    },
  }));

  readonly fill = computed<ApexFill>(() => ({
    type: 'solid',
    colors: [this.primary()],
  }));

  // Sub-score breakdown rendered as small cards next to the gauge.
  readonly breakdownItems = computed(() => {
    const b = this.data().breakdown;
    return [
      { key: 'savingsRate', value: b.savingsRate, labelKey: 'dashboard.healthScore.savingsRate' },
      { key: 'budgetAdherence', value: b.budgetAdherence, labelKey: 'dashboard.healthScore.budgetAdherence' },
      { key: 'goalProgress', value: b.goalProgress, labelKey: 'dashboard.healthScore.goalProgress' },
      { key: 'expenseConsistency', value: b.expenseConsistency, labelKey: 'dashboard.healthScore.expenseConsistency' },
    ];
  });

  constructor(
    private readonly themeService: ThemeService,
    @Inject(DOCUMENT) private readonly document: Document,
  ) {
    this.applyPrimary();
    effect(() => {
      this.themeService.theme();
      queueMicrotask(() => this.applyPrimary());
    });
  }

  private applyPrimary(): void {
    const value = getComputedStyle(this.document.documentElement).getPropertyValue('--primary').trim();
    this.primary.set(value || '#2490FF');
  }
}

function bandLabelKey(score: number): string {
  if (score < 40) return 'dashboard.healthScore.bandLow';
  if (score < 70) return 'dashboard.healthScore.bandMedium';
  return 'dashboard.healthScore.bandHigh';
}
