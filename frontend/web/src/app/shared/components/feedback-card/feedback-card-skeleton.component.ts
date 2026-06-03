import { Component } from '@angular/core';
import { SkeletonComponent } from '../skeleton/skeleton.component';

/**
 * Recipe skeleton for {@link FeedbackCardComponent}. Same wrapper, badge pill
 * and title rhythm; used at the call site because the real card has a required
 * `feedback` input.
 */
@Component({
  selector: 'app-feedback-card-skeleton',
  imports: [SkeletonComponent],
  host: { class: 'block' },
  template: `
    <div class="bg-muted/30 rounded-md p-3">
      <div class="mb-1">
        <app-skeleton variant="pill" class="h-5 w-20" />
      </div>
      <app-skeleton variant="text" class="h-4 w-3/4" />
      <app-skeleton variant="text" class="mt-1 h-3 w-1/3" />
    </div>
  `,
})
export class FeedbackCardSkeletonComponent {}
