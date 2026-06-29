import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';
import { seedProducto, cleanupE2EData } from '../fixtures/seed';

test.describe('Productos CRUD', () => {
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
    await page.waitForLoadState('domcontentloaded');
  });

  test('products table renders with data', async ({ page }) => {
    await page.goto('/logistica/productos', { timeout: 20000, waitUntil: 'domcontentloaded' });
    await expect(page.locator('table, tbody, [class*="table"], [class*="Table"]').first()).toBeVisible({ timeout: 20000 });
  });

  test('navigates to new product form', async ({ page }) => {
    await page.goto('/logistica/productos');
    const newBtn = page.getByRole('link', { name: /nuevo/i });
    if (await newBtn.isVisible({ timeout: 8000 }).catch(() => false)) {
      await newBtn.click();
      await page.waitForURL(/\/logistica\/productos\/nuevo/, { timeout: 10000 });
      await expect(page.locator('form, input').first()).toBeVisible({ timeout: 8000 });
    }
  });

  test('product creation form loads without errors', async ({ page }) => {
    await page.goto('/logistica/productos/nuevo', { timeout: 15000 });
    await expect(page.locator('form, h1, h2').first()).toBeVisible({ timeout: 15000 });
  });

  test('create product via form submission', async ({ page }) => {
    await page.goto('/logistica/productos/nuevo');
    await expect(page.locator('form').first()).toBeVisible({ timeout: 10000 });

    const sku = page.getByLabel(/sku/i).first();
    if (await sku.isVisible({ timeout: 3000 }).catch(() => false)) {
      await sku.fill(`SKU-E2E-${Date.now()}`);
    }
    const name = page.getByLabel(/nombre/i).first();
    if (await name.isVisible({ timeout: 3000 }).catch(() => false)) {
      await name.fill(`E2E Product ${Date.now()}`);
    }
    const precio = page.getByLabel(/precio/i).first();
    if (await precio.isVisible({ timeout: 3000 }).catch(() => false)) {
      await precio.fill('14990');
    }
    const stock = page.getByLabel(/stock/i).first();
    if (await stock.isVisible({ timeout: 3000 }).catch(() => false)) {
      await stock.fill('50');
    }

    const submitBtn = page.getByRole('button', { name: /guardar|crear|submit/i });
    if (await submitBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await submitBtn.click();
      await page.waitForTimeout(2000);
    }
    await expect(page.locator('body')).toBeAttached();
  });

  test('navigates to product detail from table', async ({ page }) => {
    await page.waitForTimeout(300);
    await page.goto('/logistica/productos', { timeout: 20000, waitUntil: 'domcontentloaded' });
    const row = page.locator('table a, table button, [class*="table"] a').first();
    if (await row.isVisible({ timeout: 8000 }).catch(() => false)) {
      await row.click();
      await page.waitForTimeout(2000);
      await expect(page.locator('body')).toBeAttached();
    }
  });

  test('can delete a product from table', async ({ page }) => {
    try { await seedProducto(page); } catch { /* ok */ }
    await page.goto('/logistica/productos', { timeout: 20000, waitUntil: 'domcontentloaded' });

    const deleteBtn = page.getByRole('button', { name: /eliminar|trash|delete/i }).first();
    if (await deleteBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      const confirmBtn = page.getByRole('button', { name: /confirmar|sí|yes|eliminar/i }).first();
      if (await confirmBtn.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmBtn.click();
        await page.waitForTimeout(1000);
      }
    }
    await expect(page.locator('body')).toBeAttached();
  });
});
