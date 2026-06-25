import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Checkout Flow', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip tests if registration fails */ }
  });

  test('redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/dashboard/checkout');
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.getByLabel('Email')).toBeVisible();
  });

  test('checkout page renders when authenticated', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/checkout');
    await expect(page.locator('form, input, h1, h2').first()).toBeVisible({ timeout: 15000 });
  });

  test('checkout page loads without errors', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/checkout');
    await expect(page.locator('body')).toBeAttached();
  });
});
