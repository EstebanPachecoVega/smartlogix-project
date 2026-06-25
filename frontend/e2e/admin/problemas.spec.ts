import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Envios con Problemas', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
    await page.goto('/logistica');
  });

  test('problemas page loads', async ({ page }) => {
    await page.goto('/logistica/problemas');
    await expect(page.locator('table, h1, h2').first()).toBeVisible({ timeout: 15000 });
  });
});
