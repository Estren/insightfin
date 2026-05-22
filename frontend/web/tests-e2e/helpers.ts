import type { Page } from '@playwright/test';

const ACCESS_TOKEN_KEY = 'insightfin_access_token';
const REFRESH_TOKEN_KEY = 'insightfin_refresh_token';

function base64url(value: string): string {
  return Buffer.from(value).toString('base64url');
}

/**
 * Builds a JWT the frontend can decode. The client only reads the payload
 * (sub / email / email_verified) — it never verifies the signature.
 */
export function fakeJwt(claims: Record<string, unknown> = {}): string {
  const header = base64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = base64url(
    JSON.stringify({
      sub: 'e2e-user-id',
      email: 'e2e@insightfin.test',
      email_verified: true,
      role: 'USER',
      exp: Math.floor(Date.now() / 1000) + 3600,
      ...claims,
    }),
  );
  return `${header}.${payload}.e2e-signature`;
}

/** Seeds an authenticated session into localStorage before the app boots. */
export async function seedAuthSession(page: Page): Promise<void> {
  await page.addInitScript(
    ([accessKey, refreshKey, token, refresh]) => {
      localStorage.setItem(accessKey, token);
      localStorage.setItem(refreshKey, refresh);
    },
    [ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, fakeJwt(), 'e2e-refresh-token'],
  );
}

/** Today as YYYY-MM-DD — fixtures must fall in the current month to be listed. */
export const today = new Date().toISOString().slice(0, 10);

export interface E2ETransaction {
  id: string;
  type: 'INCOME' | 'EXPENSE';
  categoryId: string;
  categoryName: string;
  amount: number;
  description: string;
  date: string;
}

const CATEGORY = { id: 'cat-1', name: 'Alimentação', type: 'EXPENSE', color: '#E11D48', icon: '🍔' };

const EMPTY_DASHBOARD = {
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  recentTransactions: [],
  budgetStatuses: [],
  activeGoals: [],
};

const USER_PROFILE = {
  id: 'e2e-user-id',
  name: 'E2E User',
  email: 'e2e@insightfin.test',
  avatarUrl: null,
  emailVerified: true,
  hasPassword: true,
  linkedWithGoogle: false,
};

/**
 * Intercepts every /api/** call and serves deterministic fixtures, so the E2E
 * suite runs with no backend. Mutating endpoints echo their input back.
 */
export async function mockApi(page: Page, options: { transactions?: E2ETransaction[] } = {}): Promise<void> {
  const transactions = options.transactions ?? [];

  await page.route('**/api/**', async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname.replace(/^.*\/api/, '');
    const method = request.method();
    const json = (status: number, body: unknown) =>
      route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });

    if (path === '/auth/login' && method === 'POST') {
      return json(200, { accessToken: fakeJwt(), refreshToken: 'e2e-refresh-token' });
    }
    if (path === '/auth/register' && method === 'POST') {
      return json(201, { accessToken: fakeJwt(), refreshToken: 'e2e-refresh-token' });
    }
    if (path === '/transactions' && method === 'GET') {
      return json(200, transactions);
    }
    if (path === '/transactions' && method === 'POST') {
      return json(201, { id: 'tx-new', categoryName: CATEGORY.name, description: '', ...request.postDataJSON() });
    }
    if (path.startsWith('/transactions/') && method === 'PUT') {
      return json(200, {
        id: path.split('/').pop(),
        categoryName: CATEGORY.name,
        description: '',
        ...request.postDataJSON(),
      });
    }
    if (path.startsWith('/transactions/') && method === 'DELETE') {
      return route.fulfill({ status: 204, body: '' });
    }
    if (path === '/categories') {
      return json(200, [CATEGORY]);
    }
    if (path === '/dashboard') {
      return json(200, EMPTY_DASHBOARD);
    }
    if (path.startsWith('/users')) {
      return json(200, USER_PROFILE);
    }
    // Everything else (feedbacks badge, budgets, goals, ...) → empty list.
    return json(200, []);
  });
}
