import { inject, Injectable } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import posthog from 'posthog-js';

import { environment } from '../../../environments/environment';

/**
 * Wraps PostHog product analytics. Every method is a no-op when no API key is
 * configured, so dev and any unconfigured environment stay untouched.
 */
@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly router = inject(Router);
  private enabled = false;

  /** Initializes PostHog and starts page-view tracking. No-op without a key. */
  init(): void {
    const key = environment.posthogKey;
    if (!key) {
      return;
    }

    posthog.init(key, {
      api_host: environment.posthogHost,
      autocapture: false,
      capture_pageview: false,
      disable_session_recording: true,
      persistence: 'memory',
      person_profiles: 'identified_only',
    });
    this.enabled = true;

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(() => posthog.capture('$pageview'));
  }

  /** Records a product event. */
  capture(event: string, properties?: Record<string, unknown>): void {
    if (this.enabled) {
      posthog.capture(event, properties);
    }
  }

  /** Associates subsequent events with a user (call on login/register). */
  identify(userId: string): void {
    if (this.enabled) {
      posthog.identify(userId);
    }
  }

  /** Clears the identified user (call on logout). */
  reset(): void {
    if (this.enabled) {
      posthog.reset();
    }
  }
}
