import { Component } from '@angular/core';
import { SkeletonComponent } from '../skeleton/skeleton.component';

/**
 * Recipe skeleton for {@link StatCardComponent}. Mirrors its wrapper, padding
 * and the label/value rhythm so the two never drift visually. Used at the call
 * site as `@if (loading) { <app-stat-card-skeleton /> } @else { <app-stat-card ...
 * /> }` because the real card has required inputs (label/value).
 */
@Component({
  selector: 'app-stat-card-skeleton',
  imports: [SkeletonComponent],
  template: `
    <div class="bg-background rounded-lg p-6">
      <app-skeleton variant="text" class="h-4 w-24" />
      <app-skeleton variant="text" class="mt-2 h-7 w-32" />
    </div>
  `,
})
export class StatCardSkeletonComponent {}
