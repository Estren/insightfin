import { Component, computed, input } from '@angular/core';
import { AngularSvgIconModule } from 'angular-svg-icon';

type Tone = 'neutral' | 'positive' | 'negative';

@Component({
  selector: 'app-stat-card',
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.css',
  imports: [AngularSvgIconModule],
})
export class StatCardComponent {
  label = input.required<string>();
  value = input.required<string>();
  iconSrc = input<string>('');
  tone = input<Tone>('neutral');
  delta = input<number | null>(null);
  deltaLabel = input<string>('vs last month');

  readonly deltaDirection = computed<'up' | 'down' | 'flat'>(() => {
    const d = this.delta();
    if (d === null || d === 0) return 'flat';
    return d > 0 ? 'up' : 'down';
  });
}
