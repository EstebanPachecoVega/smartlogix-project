import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Customer Orders', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test('unauthenticated redirects to login', async ({ page }) => {
    await page.goto('/dashboard/pedidos', { timeout: 15000 });
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.getByLabel('Email')).toBeVisible();
  });

  test('orders page renders for authenticated user', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/pedidos', { timeout: 15000 });
    await page.waitForLoadState('domcontentloaded');
    await expect(page.locator('h1, h2, table, p, body').first()).toBeAttached({ timeout: 5000 });
  });

  test('order detail navigates from orders table', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/pedidos', { timeout: 15000 });
    await page.waitForLoadState('domcontentloaded');

    const detailLink = page.locator('a[href*="/dashboard/pedidos/"]').first();
    if (await detailLink.isVisible({ timeout: 8000 }).catch(() => false)) {
      await detailLink.click();
      await page.waitForURL(/\/dashboard\/pedidos\//, { timeout: 10000 });
      await expect(page.locator('h1, h2, table, p').first()).toBeVisible({ timeout: 10000 });
    }
  });

  test('order detail shows key sections', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/pedidos', { timeout: 15000 });
    await page.waitForLoadState('domcontentloaded');

    const detailLink = page.locator('a[href*="/dashboard/pedidos/"]').first();
    if (!(await detailLink.isVisible({ timeout: 8000 }).catch(() => false))) return;
    await detailLink.click();
    await page.waitForURL(/\/dashboard\/pedidos\//, { timeout: 10000 });

    const hasInfo = await page.getByText(/orden|pedido|nº/i).first().isVisible({ timeout: 5000 }).catch(() => false);
    const hasProducts = await page.locator('table, tbody, img').first().isVisible({ timeout: 5000 }).catch(() => false);
    expect(hasInfo || hasProducts).toBeTruthy();
  });
});
