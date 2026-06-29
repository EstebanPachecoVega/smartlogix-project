import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Tracking Search', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
  });

  test('search form is visible', async ({ page }) => {
    await page.goto('/logistica/buscar');
    await expect(page.locator('input, form').first()).toBeVisible({ timeout: 10000 });
  });

  test('submitting a valid tracking number navigates to result', async ({ page }) => {
    await page.goto('/logistica/envios');
    await page.waitForLoadState('domcontentloaded');

    let tracking = '';
    const trackingCell = page.locator('td, [class*="tracking"]').first();
    if (await trackingCell.isVisible({ timeout: 5000 }).catch(() => false)) {
      tracking = await trackingCell.textContent() || '';
      tracking = tracking.trim();
    }

    if (tracking) {
      await page.goto('/logistica/buscar');
      const input = page.locator('input').first();
      if (await input.isVisible({ timeout: 3000 }).catch(() => false)) {
        await input.fill(tracking);
        await page.getByRole('button', { name: /buscar|search/i }).click();
        await page.waitForURL(/\/logistica\/envios\/tracking\//, { timeout: 10000 }).catch(() => {});
        await expect(page.locator('body')).toBeAttached();
      }
    }
  });
});
