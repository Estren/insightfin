import { expect, test } from '@playwright/test';
import { mockApi, seedAuthSession, today, type E2ETransaction } from './helpers';

const SEED_TX: E2ETransaction = {
  id: 'tx-1',
  type: 'EXPENSE',
  categoryId: 'cat-1',
  categoryName: 'Alimentação',
  amount: 50,
  description: 'Almoço de teste',
  date: today,
};

test.describe('Transactions', () => {
  test('creates a transaction', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page);
    await page.goto('/transactions');

    await page.locator('app-page-header button').click();
    const modal = page.locator('app-modal');
    await expect(modal).toBeAttached();

    await modal.locator('select').selectOption('cat-1');
    await modal.locator('input[type="number"]').fill('123.45');
    await modal.locator('input[type="date"]').fill(today);
    await modal.locator('button[type="submit"]').click();

    await expect(modal).not.toBeAttached();
    await expect(page.getByText('Alimentação').first()).toBeVisible();
  });

  test('edits a transaction', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, { transactions: [SEED_TX] });
    await page.goto('/transactions');

    await expect(page.getByText('Almoço de teste').first()).toBeVisible();
    await page.getByRole('button', { name: 'Editar' }).first().click();

    const modal = page.locator('app-modal');
    await expect(modal).toBeAttached();
    await modal.locator('input[type="number"]').fill('99.90');
    await modal.locator('button[type="submit"]').click();

    await expect(modal).not.toBeAttached();
  });

  test('deletes a transaction', async ({ page }) => {
    await seedAuthSession(page);
    await mockApi(page, { transactions: [SEED_TX] });
    await page.goto('/transactions');

    await expect(page.getByText('Almoço de teste').first()).toBeVisible();
    await page.getByRole('button', { name: 'Excluir' }).first().click();

    await page.locator('app-confirm-dialog button').last().click();

    await expect(page.getByText('Almoço de teste').first()).toBeHidden();
  });
});
