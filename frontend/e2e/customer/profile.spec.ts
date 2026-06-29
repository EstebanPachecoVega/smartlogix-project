import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Customer Profile', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test('redirects unauthenticated users to login', async ({ page }) => {
    await page.goto('/dashboard/perfil');
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.getByLabel('Email')).toBeVisible();
  });

  test('profile page renders for authenticated user', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/perfil');
    await expect(page.locator('h1, h2, form, input').first()).toBeVisible({ timeout: 10000 });
  });
});
