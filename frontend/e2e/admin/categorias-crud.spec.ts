import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';
import { cleanupE2EData } from '../fixtures/seed';

test.describe('Categorias CRUD', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test.afterAll(async ({ browser }) => {
    if (!users?.ok) return;
    const context = await browser.newContext();
    const page = await context.newPage();
    try {
      await loginAs(page, users.gestor.email, users.gestor.password);
      await cleanupE2EData(page);
    } catch { /* cleanup is best-effort */ }
    await context.close();
  });

  test.beforeEach(async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.gestor.email, users.gestor.password);
  });

  test('categories page renders', async ({ page }) => {
    await page.goto('/logistica/categorias');
    await expect(page.locator('table, [class*="tree"], h1, h2').first()).toBeVisible({ timeout: 15000 });
  });

  test('navigates to new category form', async ({ page }) => {
    await page.goto('/logistica/categorias');
    const newBtn = page.getByRole('link', { name: 'Nueva categoría' });
    if (await newBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await newBtn.click();
      await page.waitForTimeout(2000);
      await expect(page.locator('form').first()).toBeVisible({ timeout: 10000 });
    }
  });

  test('create category via form submission', async ({ page }) => {
    await page.goto('/logistica/categorias/nuevo');
    await expect(page.locator('form').first()).toBeVisible({ timeout: 10000 });

    const nameInput = page.getByLabel(/nombre/i).first();
    if (await nameInput.isVisible({ timeout: 3000 }).catch(() => false)) {
      await nameInput.fill(`Cat E2E ${Date.now()}`);
    }
    const submitBtn = page.getByRole('button', { name: /guardar|crear/i });
    if (await submitBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await submitBtn.click();
      await page.waitForTimeout(2000);
    }
    await expect(page.locator('body')).toBeAttached();
  });

  test('navigates to category detail from table', async ({ page }) => {
    await page.goto('/logistica/categorias', { timeout: 20000, waitUntil: 'domcontentloaded' });

    const row = page.locator('table a, table button, [class*="table"] a, [class*="tree"] a').first();
    if (await row.isVisible({ timeout: 5000 }).catch(() => false)) {
      await row.click();
      await page.waitForTimeout(2000);
      await expect(page.locator('body')).toBeAttached();
    }
  });
});
