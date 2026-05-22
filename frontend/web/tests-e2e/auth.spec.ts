import { expect, test } from '@playwright/test';
import { mockApi } from './helpers';

test.describe('Authentication', () => {
  test('signs in with email and password', async ({ page }) => {
    await mockApi(page);
    await page.goto('/auth/sign-in');

    await page.locator('#email').fill('e2e@insightfin.test');
    await page.locator('#password').fill('Password123');
    await page.locator('button[type="submit"]').click();

    await expect(page).not.toHaveURL(/\/auth\//);
    await expect(page.locator('app-navbar')).toBeVisible();
  });

  test('registers a new account and lands on email verification', async ({ page }) => {
    await mockApi(page);
    await page.goto('/auth/sign-up');

    await page.locator('#name').fill('E2E User');
    await page.locator('#email').fill('new@insightfin.test');
    await page.locator('#password').fill('Password123');
    await page.locator('#confirm-password').fill('Password123');
    await page.locator('button[type="submit"]').click();

    await expect(page).toHaveURL(/\/auth\/verify-email/);
  });
});
