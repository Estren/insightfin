import { Component, computed, input } from '@angular/core';
import { NgClass } from '@angular/common';

export type SkeletonVariant = 'text' | 'pill' | 'circle' | 'block';

/**
 * Loading skeleton with theme-aware shimmer. Rounding is baked per variant so
 * skeletons stay consistent with their content counterparts; the consumer
 * controls size with the usual Tailwind classes on the host (e.g. `h-4 w-32`).
 *
 * Cores e animação vêm do `.shimmer` utility em styles.css — fonte única.
 */
@Component({
  selector: 'app-skeleton',
  standalone: true,
  template: `<div
    aria-busy="true"
    aria-hidden="true"
    class="shimmer block h-full w-full"
    [ngClass]="roundingClass()"></div>`,
  host: { class: 'block' },
  imports: [NgClass],
})
export class SkeletonComponent {
  variant = input<SkeletonVariant>('block');

  protected readonly roundingClass = computed(() => {
    switch (this.variant()) {
      case 'text':
        return 'rounded-md';
      case 'pill':
        return 'rounded-full';
      case 'circle':
        return 'rounded-full';
      case 'block':
        return 'rounded-lg';
    }
  });
}
