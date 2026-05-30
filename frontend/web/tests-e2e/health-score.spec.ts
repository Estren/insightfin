import { expect, test } from '@playwright/test';
import { mockApi, seedAuthSession, type E2EAiFeedback } from './helpers';

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

const HEALTH_SCORE_FEEDBACK: E2EAiFeedback = {
  id: 'fb-hs-1',
  type: 'HEALTH_SCORE',
  title: 'Financial Health',
  content: 'Your overall financial health is good.',
  metadata: JSON.stringify({
    score: 78,
    breakdown: {
      savingsRate: 80,
      budgetAdherence: 75,
      goalProgress: 70,
      expenseConsistency: 87,
    },
  }),
  referenceMonth: currentMonth(),
  read: false,
  createdAt: new Date().toISOString(),
};

test.describe('Health score gauge', () => {
  test('renders the gauge and breakdown when metadata is present', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, { feedbacks: [HEALTH_SCORE_FEEDBACK] });

    await page.goto('/dashboard');

    const gauge = page.locator('app-health-score-gauge');
    await expect(gauge).toBeVisible();
    // ApexCharts renders the score as text inside the radial bar's SVG.
    await expect(gauge.getByText('78', { exact: true })).toBeVisible();
    // All four breakdown sub-scores appear by value.
    await expect(gauge.getByText('80', { exact: true })).toBeVisible();
    await expect(gauge.getByText('75', { exact: true })).toBeVisible();
    await expect(gauge.getByText('70', { exact: true })).toBeVisible();
    await expect(gauge.getByText('87', { exact: true })).toBeVisible();
  });

  test('shows the empty state when there is no HEALTH_SCORE feedback', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page); // no feedbacks at all

    await page.goto('/dashboard');

    await expect(page.locator('app-health-score-gauge')).toHaveCount(0);
    // Empty state title comes from i18n dashboard.healthScore.empty.
    await expect(page.getByText('Score ainda não gerado')).toBeVisible();
  });

  test('falls back to the empty state when the HEALTH_SCORE metadata is malformed', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, {
      feedbacks: [
        {
          ...HEALTH_SCORE_FEEDBACK,
          metadata: '{"broken": true}', // missing score and breakdown fields
        },
      ],
    });

    await page.goto('/dashboard');

    await expect(page.locator('app-health-score-gauge')).toHaveCount(0);
    await expect(page.getByText('Score ainda não gerado')).toBeVisible();
  });
});
