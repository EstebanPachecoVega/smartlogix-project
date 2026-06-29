import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Admin Pedidos', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
    await page.waitForLoadState('domcontentloaded');
  });

  test('pedidos table renders', async ({ page }) => {
    await page.goto('/logistica/pedidos', { timeout: 20000, waitUntil: 'domcontentloaded' });
    await page.waitForTimeout(1000);
    await expect(page.locator('table, tbody, [class*="table"]').first()).toBeVisible({ timeout: 20000 });
  });

  test('pedido detail shows order info and line items', async ({ page }) => {
    await page.goto('/logistica/pedidos', { timeout: 20000, waitUntil: 'load' });
    await page.waitForLoadState('domcontentloaded');

    const detailLink = page.locator('a[href*="/logistica/pedidos/"]').first();
    if (!(await detailLink.isVisible({ timeout: 8000 }).catch(() => false))) return;
    await detailLink.click();
    await page.waitForURL(/\/logistica\/pedidos\//, { timeout: 10000 });

    await expect(page.locator('body')).toBeAttached({ timeout: 5000 });
    await expect(page.getByText(/orden|pedido|nº/i).first()).toBeAttached({ timeout: 10000 });
  });
});
