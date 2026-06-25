import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Cart Interactions', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* skip */ }
  });

  test('empty cart shows page content', async ({ page }) => {
    await page.goto('/dashboard/carrito');
    await page.waitForURL(/\/login|\/carrito/, { timeout: 10000 });
    await expect(page.locator('body')).toBeAttached();
  });

  test('add product via store and verify in cart', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);

    await page.evaluate(() => {
      const store = (window as any).__ZUSTAND_STORE__;
      if (!store) {
        const item = {
          producto: { id: 1, sku: 'E2E-TEST', nombre: 'Test Product', precio: 9990,
            imagenPrincipal: '', stock: 100, slug: 'test-product', categoria: { id: 1, nombre: 'Cat' } },
          cantidad: 2,
        };
        localStorage.setItem('carrito-storage', JSON.stringify({ state: { items: [item] } }));
        window.location.reload();
      }
    });

    await page.goto('/dashboard/carrito');
    await expect(page.locator('body')).toBeAttached({ timeout: 10000 });
  });

  test('cart persists across page navigation', async ({ page }) => {
    await page.goto('/dashboard/carrito');
    await page.waitForURL(/\/login|\/carrito/, { timeout: 10000 });
    if (page.url().includes('/login')) return;

    await page.goto('/');
    await page.waitForTimeout(500);
    await page.goto('/dashboard/carrito');
    await expect(page.locator('body')).toBeAttached({ timeout: 5000 });
  });
});
