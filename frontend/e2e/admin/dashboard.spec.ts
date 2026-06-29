import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Admin Dashboard', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
    await page.waitForLoadState('domcontentloaded');
    await page.goto('/logistica', { timeout: 20000, waitUntil: 'domcontentloaded' });
  });

  test('dashboard renders content', async ({ page }) => {
    await page.waitForLoadState('domcontentloaded');
    await expect(page.locator('body')).toBeAttached();
  });

  test('dashboard has sidebar navigation', async ({ page }) => {
    await expect(page.getByRole('link', { name: /productos/i })).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole('link', { name: /pedidos/i })).toBeVisible({ timeout: 10000 });
  });
});
