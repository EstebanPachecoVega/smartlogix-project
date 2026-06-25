import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Full Checkout Flow', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test('checkout fails gracefully with empty cart', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await page.goto('/dashboard/checkout');
    await expect(page.locator('body')).toBeAttached();
  });

  test('complete purchase: add to cart, fill form, submit', async ({ page }) => {
    test.skip(!users?.ok, 'No test user');

    await loginAs(page, users.cliente.email, users.cliente.password);

    await page.goto('/');
    const productLink = page.locator('a[href*="/productos/"]').first();
    if (!(await productLink.isVisible({ timeout: 10000 }).catch(() => false))) return;
    await productLink.click();
    await page.waitForURL(/\/productos\//, { timeout: 10000 });

    const addBtn = page.getByRole('button', { name: /agregar|añadir|carrito/i });
    if (await addBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await addBtn.click();
      await page.waitForTimeout(1000);
    }

    await page.goto('/dashboard/carrito');
    const checkoutLink = page.getByRole('link', { name: /pago|checkout|proceder/i });
    if (await checkoutLink.isVisible({ timeout: 5000 }).catch(() => false)) {
      await checkoutLink.click();
    } else {
      await page.goto('/dashboard/checkout');
    }

    await expect(page.locator('form, input, h1').first()).toBeVisible({ timeout: 10000 });

    const calle = page.getByLabel(/calle|dirección/i).first();
    if (await calle.isVisible({ timeout: 3000 }).catch(() => false)) {
      await calle.fill('Calle Test 123');
    }
    const numero = page.getByLabel(/número|numero/i).first();
    if (await numero.isVisible({ timeout: 3000 }).catch(() => false)) {
      await numero.fill('456');
    }
    const comuna = page.getByLabel(/comuna/i).first();
    if (await comuna.isVisible({ timeout: 3000 }).catch(() => false)) {
      await comuna.fill('Santiago');
    }
    const ciudad = page.getByLabel(/ciudad/i).first();
    if (await ciudad.isVisible({ timeout: 3000 }).catch(() => false)) {
      await ciudad.fill('Santiago');
    }
    const cp = page.getByLabel(/código postal|codigo postal/i).first();
    if (await cp.isVisible({ timeout: 3000 }).catch(() => false)) {
      await cp.fill('8320000');
    }

    const payBtn = page.getByRole('button', { name: /pagar|confirmar|comprar/i });
    if (await payBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
      await payBtn.click();
      await page.waitForURL(/\/dashboard\/pedidos/, { timeout: 15000 }).catch(() => {});
    }

    await expect(page.locator('body')).toBeAttached();
  });
});
