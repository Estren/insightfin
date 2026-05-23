import { expect, test } from '@playwright/test';
import { mockApi, seedAuthSession, type E2ENotification } from './helpers';

const AI_FEEDBACK: E2ENotification = {
  id: 'fb-1',
  kind: 'AI_FEEDBACK',
  read: false,
  createdAt: '2026-05-22T10:00:00',
  aiFeedbackType: 'MONTHLY_REPORT',
  title: 'Monthly report',
  content: 'You spent 30% more on food this month.',
  referenceMonth: '2026-05',
  budgetId: null,
  categoryName: null,
  thresholdPercentage: null,
  amountSpent: null,
  budgetAmount: null,
  triggeredAt: null,
};

const BUDGET_ALERT: E2ENotification = {
  id: 'alert-1',
  kind: 'BUDGET_ALERT',
  read: false,
  createdAt: '2026-05-23T14:30:00',
  aiFeedbackType: null,
  title: null,
  content: null,
  referenceMonth: null,
  budgetId: 'budget-1',
  categoryName: 'Transport',
  thresholdPercentage: 80,
  amountSpent: 400,
  budgetAmount: 500,
  triggeredAt: '2026-05-23T14:30:00',
};

test.describe('Notifications', () => {
  test('badge shows the unread total from the API', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, {
      unreadCount: { aiFeedbacks: 1, budgetAlerts: 2, total: 3 },
    });

    await page.goto('/dashboard');

    const badge = page.locator('app-notifications-badge span[aria-hidden="true"]');
    await expect(badge).toHaveText('3');
  });

  test('opening /notifications lists the mixed feed sorted desc', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, {
      // Server returns sorted DESC; the budget alert (later createdAt) comes first.
      notifications: [BUDGET_ALERT, AI_FEEDBACK],
      unreadCount: { aiFeedbacks: 1, budgetAlerts: 1, total: 2 },
    });

    await page.goto('/notifications');

    const cards = page.locator('app-notification-card');
    await expect(cards).toHaveCount(2);
    await expect(cards.first()).toContainText('Transport');
    await expect(cards.first()).toContainText('80');
    await expect(cards.nth(1)).toContainText('Monthly report');
  });

  test('clicking an unread item decrements the badge optimistically', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, {
      notifications: [BUDGET_ALERT],
      unreadCount: { aiFeedbacks: 0, budgetAlerts: 1, total: 1 },
    });

    await page.goto('/notifications');

    const badge = page.locator('app-notifications-badge span[aria-hidden="true"]');
    await expect(badge).toHaveText('1');

    // Wait for the request before clicking, so we can assert the right endpoint
    // was hit instead of just the UI side-effect.
    const patchRequest = page.waitForRequest(
      (req) => req.url().includes('/api/budget-alerts/alert-1/read') && req.method() === 'PATCH',
    );

    await page.locator('app-notification-card').first().click();
    await patchRequest;

    // Badge zeroes out → element no longer rendered (template hides span when count is 0).
    await expect(badge).toHaveCount(0);
  });
});
