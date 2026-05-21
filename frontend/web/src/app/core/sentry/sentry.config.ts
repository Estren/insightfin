import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler } from '@angular/core';
import * as Sentry from '@sentry/angular';

import { environment } from '../../../environments/environment';

const EMAIL_REGEX = /[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/g;

/** Replaces e-mail addresses with a placeholder so PII never leaves the browser. */
function redactEmails(value: string | undefined): string | undefined {
  return value?.replace(EMAIL_REGEX, '[redacted-email]');
}

/**
 * Initializes Sentry error monitoring. No-op when no DSN is configured, so
 * local dev and any environment without a DSN keep working untouched.
 */
export function initSentry(): void {
  const dsn = environment.sentryDsn;
  if (!dsn) {
    return;
  }

  Sentry.init({
    dsn,
    environment: environment.sentryEnvironment,
    sendDefaultPii: false,
    // Performance monitoring is out of scope for the MVP.
    tracesSampleRate: 0,
    beforeSend(event, hint) {
      // Drop expected 4xx HTTP errors (validation, not-found, conflict, ...).
      // 5xx and network failures (status 0) are kept — those are real signal.
      const original = hint?.originalException;
      if (original instanceof HttpErrorResponse && original.status >= 400 && original.status < 500) {
        return null;
      }

      // Strip PII before the event leaves the browser.
      if (event.request) {
        delete event.request.data;
        const headers = event.request.headers;
        if (headers) {
          delete headers['Authorization'];
          delete headers['authorization'];
          delete headers['Cookie'];
          delete headers['cookie'];
        }
      }
      event.message = redactEmails(event.message);
      for (const value of event.exception?.values ?? []) {
        value.value = redactEmails(value.value);
      }
      return event;
    },
    beforeBreadcrumb(breadcrumb) {
      breadcrumb.message = redactEmails(breadcrumb.message);
      return breadcrumb;
    },
  });
}

/**
 * Angular ErrorHandler that forwards uncaught errors to Sentry. Safe to provide
 * even when Sentry is not initialized — capture simply no-ops in that case.
 */
export const sentryErrorHandler: ErrorHandler = Sentry.createErrorHandler();
