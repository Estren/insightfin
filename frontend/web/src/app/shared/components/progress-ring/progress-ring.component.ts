import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-progress-ring',
  templateUrl: './progress-ring.component.html',
})
export class ProgressRingComponent {
  percentage = input.required<number>();
  size = input<number>(56);
  strokeWidth = input<number>(5);

  protected readonly center = computed(() => this.size() / 2);
  protected readonly radius = computed(() => (this.size() - this.strokeWidth()) / 2);
  protected readonly circumference = computed(() => 2 * Math.PI * this.radius());

  protected readonly dashOffset = computed(() => {
    const clamped = Math.min(Math.max(this.percentage(), 0), 100);
    return this.circumference() * (1 - clamped / 100);
  });

  // Always the theme primary; as a CSS var it resolves live, so the ring
  // tracks theme changes without any JS.
  protected readonly strokeColor = 'var(--primary)';

  protected readonly transform = computed(() => `rotate(-90 ${this.center()} ${this.center()})`);

  protected readonly fontSize = computed((): string => {
    const s = this.size();
    if (s <= 48) return '9px';
    if (s <= 64) return '10px';
    if (s <= 80) return '12px';
    return '14px';
  });

  protected readonly label = computed(() => `${Math.round(Math.min(this.percentage(), 999))}%`);
}
