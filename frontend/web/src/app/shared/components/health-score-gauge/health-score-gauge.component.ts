import { Component, computed, input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ApexChart, ApexFill, ApexPlotOptions, ApexStroke, NgApexchartsModule } from 'ng-apexcharts';
import { HealthScoreMetadata } from '../../../core/models/health-score.model';

const RED = '#ef4444';
const AMBER = '#f59e0b';
const GREEN = '#22c55e';

@Component({
  selector: 'app-health-score-gauge',
  templateUrl: './health-score-gauge.component.html',
  imports: [TranslateModule, NgApexchartsModule],
})
export class HealthScoreGaugeComponent {
  data = input.required<HealthScoreMetadata>();

  readonly series = computed(() => [this.data().score]);
  readonly color = computed(() => bandColor(this.data().score));
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
          color: this.color(),
          offsetY: 8,
          formatter: (val) => `${Math.round(Number(val))}`,
        },
      },
    },
  }));

  readonly fill = computed<ApexFill>(() => ({
    type: 'solid',
    colors: [this.color()],
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

  bandColorFor(value: number): string {
    return bandColor(value);
  }
}

function bandColor(score: number): string {
  if (score < 40) return RED;
  if (score < 70) return AMBER;
  return GREEN;
}

function bandLabelKey(score: number): string {
  if (score < 40) return 'dashboard.healthScore.bandLow';
  if (score < 70) return 'dashboard.healthScore.bandMedium';
  return 'dashboard.healthScore.bandHigh';
}
