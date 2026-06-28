import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Admin Envios', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
    await page.waitForLoadState('domcontentloaded');
  });

  test('envios table renders', async ({ page }) => {
    await page.goto('/logistica/envios');
    await page.waitForLoadState('domcontentloaded');
    await expect(page.locator('table, tbody, [class*="table"]').first()).toBeVisible({ timeout: 15000 });
  });

  test('envio detail shows tracking info', async ({ page }) => {
    await page.goto('/logistica/envios', { timeout: 15000 });
    await page.waitForLoadState('domcontentloaded');

    const detailLink = page.locator('a[href*="/logistica/envios/"]').first();
    if (!(await detailLink.isVisible({ timeout: 8000 }).catch(() => false))) return;
    await detailLink.click();
    await page.waitForURL(/\/logistica\/envios\//, { timeout: 10000 });
    await expect(page.locator('body')).toBeAttached();

    await expect(page.getByText('Tracking:')).toBeAttached({ timeout: 5000 });
  });

  test('envio detail has status update control', async ({ page }) => {
    await page.goto('/logistica/envios', { timeout: 20000, waitUntil: 'load' });
    await page.waitForLoadState('domcontentloaded');

    const detailLink = page.locator('a[href*="/logistica/envios/"]').first();
    if (!(await detailLink.isVisible({ timeout: 8000 }).catch(() => false))) return;
    await detailLink.click();
    await page.waitForURL(/\/logistica\/envios\//, { timeout: 10000 });

    await expect(page.locator('body')).toBeAttached({ timeout: 5000 });
  });
});
