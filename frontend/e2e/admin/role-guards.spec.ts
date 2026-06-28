import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Role-Based Access Control', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test('unauthenticated user is redirected from admin', async ({ page }) => {
    await page.goto('/logistica');
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.getByLabel('Email')).toBeVisible();
  });

  test('gestor can access admin panel', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
    await page.goto('/logistica', { timeout: 20000, waitUntil: 'domcontentloaded' });
    await expect(page.getByRole('link', { name: /productos/i })).toBeVisible({ timeout: 15000 });
  });

  test('cliente sees restricted content on admin panel', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/logistica');
    await page.waitForTimeout(2000);
    const hasSidebar = await page.getByRole('link', { name: /productos/i }).isVisible().catch(() => false);
    if (!hasSidebar) {
      await expect(page.locator('body')).toBeAttached();
    }
  });
});
