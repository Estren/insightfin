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

  test('Carregar mais appends the next page and hides the button on the last one', async ({ page }) => {
    // 25 items > PAGE_SIZE (20) → first page shows 20 with a Load more button;
    // after clicking, the remaining 5 append and the button disappears.
    const many: E2ENotification[] = Array.from({ length: 25 }, (_, i) => ({
      ...AI_FEEDBACK,
      id: `fb-${i}`,
      title: `Item ${i}`,
      // Timestamps strictly descending so the mock pagination order is deterministic.
      createdAt: new Date(Date.parse('2026-05-23T10:00:00Z') - i * 60_000).toISOString(),
    }));

    await seedAuthSession(page);
    await mockApi(page, { notifications: many });

    await page.goto('/notifications');

    const cards = page.locator('app-notification-card');
    await expect(cards).toHaveCount(20);

    const loadMore = page.getByRole('button', { name: 'Carregar mais' });
    await expect(loadMore).toBeVisible();
    await loadMore.click();

    await expect(cards).toHaveCount(25);
    await expect(loadMore).toHaveCount(0);
  });

  test.describe('bell dropdown', () => {
    test('clicking the bell opens a popover with the preview', async ({ page }) => {
      await seedAuthSession(page);
      await mockApi(page, {
        notifications: [BUDGET_ALERT, AI_FEEDBACK],
        unreadCount: { aiFeedbacks: 1, budgetAlerts: 1, total: 2 },
      });
      await page.goto('/dashboard');

      const dropdown = page.getByRole('dialog');
      await expect(dropdown).toHaveCount(0);

      await page.locator('app-notifications-badge button').click();

      await expect(dropdown).toBeVisible();
      await expect(dropdown.locator('app-notification-card')).toHaveCount(2);
      await expect(dropdown.getByRole('link', { name: 'Ver todas as notificações' })).toBeVisible();
    });

    test('clicking an item marks it read, keeps the dropdown open and decrements the badge', async ({ page }) => {
      await seedAuthSession(page);
      await mockApi(page, {
        notifications: [BUDGET_ALERT],
        unreadCount: { aiFeedbacks: 0, budgetAlerts: 1, total: 1 },
      });
      await page.goto('/dashboard');

      await page.locator('app-notifications-badge button').click();
      const dropdown = page.getByRole('dialog');
      await expect(dropdown).toBeVisible();

      const patchRequest = page.waitForRequest(
        (req) => req.url().includes('/api/budget-alerts/alert-1/read') && req.method() === 'PATCH',
      );
      await dropdown.locator('app-notification-card').first().click();
      await patchRequest;

      // Dropdown stays open (per spec) and the badge zeroes out optimistically.
      await expect(dropdown).toBeVisible();
      await expect(page.locator('app-notifications-badge span[aria-hidden="true"]')).toHaveCount(0);
    });

    test('"Ver todas" navigates to /notifications and closes the dropdown', async ({ page }) => {
      await seedAuthSession(page);
      await mockApi(page, {
        notifications: [BUDGET_ALERT],
        unreadCount: { aiFeedbacks: 0, budgetAlerts: 1, total: 1 },
      });
      await page.goto('/dashboard');

      await page.locator('app-notifications-badge button').click();
      await page.getByRole('link', { name: 'Ver todas as notificações' }).click();

      await expect(page).toHaveURL(/\/notifications$/);
      await expect(page.getByRole('dialog')).toHaveCount(0);
    });

    test('clicking outside the dropdown closes it', async ({ page }) => {
      await seedAuthSession(page);
      await mockApi(page);
      await page.goto('/dashboard');

      await page.locator('app-notifications-badge button').click();
      await expect(page.getByRole('dialog')).toBeVisible();

      // Click somewhere outside the badge — the page header is a safe target.
      await page.locator('app-page-header').first().click();

      await expect(page.getByRole('dialog')).toHaveCount(0);
    });

    test('pressing Escape closes the dropdown', async ({ page }) => {
      await seedAuthSession(page);
      await mockApi(page);
      await page.goto('/dashboard');

      await page.locator('app-notifications-badge button').click();
      await expect(page.getByRole('dialog')).toBeVisible();

      await page.keyboard.press('Escape');

      await expect(page.getByRole('dialog')).toHaveCount(0);
    });
  });
});
