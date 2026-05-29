import { Component } from '@angular/core';
import { SkeletonComponent } from '../skeleton/skeleton.component';

/**
 * Recipe skeleton for {@link NotificationCardComponent}. Same wrapper, badge
 * pill, right-aligned date and title rhythm; used at the call site because
 * the real card has a required `notification` input.
 */
@Component({
  selector: 'app-notification-card-skeleton',
  imports: [SkeletonComponent],
  template: `
    <div class="bg-muted/30 rounded-md p-3">
      <div class="mb-1 flex items-center gap-2">
        <app-skeleton variant="pill" class="h-5 w-20" />
        <app-skeleton variant="text" class="ml-auto h-3 w-12" />
      </div>
      <app-skeleton variant="text" class="h-4 w-3/4" />
      <app-skeleton variant="text" class="mt-1 h-3 w-1/3" />
    </div>
  `,
})
export class NotificationCardSkeletonComponent {}
