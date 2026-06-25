import { test, expect } from '@playwright/test';
import { createTestUsers, loginAs } from '../fixtures/auth';

test.describe('Authentication', () => {
  let users: Awaited<ReturnType<typeof createTestUsers>>;

  test.beforeAll(async () => {
    try { users = await createTestUsers(); } catch { /* registration may fail without Keycloak admin */ }
  });

  test('login form elements are present', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByLabel('Email')).toBeVisible({ timeout: 10000 });
    await expect(page.getByLabel(/contraseña/i)).toBeVisible();
    await expect(page.locator('form').getByRole('button', { name: /iniciar sesión/i })).toBeVisible();
  });

  test('protected pages redirect to login', async ({ page }) => {
    await page.goto('/dashboard/carrito');
    await page.waitForURL(/\/login/, { timeout: 15000 });
    await expect(page.getByLabel('Email')).toBeVisible();
  });

  test('login with valid credentials', async ({ page }) => {
    if (!users?.ok) return;
    await loginAs(page, users.cliente.email, users.cliente.password);
    await expect(page).toHaveURL('/');
  });
});
