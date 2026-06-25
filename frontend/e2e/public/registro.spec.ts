import { test, expect } from '@playwright/test';

test.describe('User Registration', () => {
  test('registration page loads without errors', async ({ page }) => {
    await page.goto('/registro');
    await expect(page.locator('form, input, h1').first()).toBeVisible({ timeout: 10000 });
  });

  test('registration form has submit button', async ({ page }) => {
    await page.goto('/registro');
    await expect(page.getByRole('button', { name: /crear cuenta|registrarse/i }).first()).toBeVisible();
  });
});
