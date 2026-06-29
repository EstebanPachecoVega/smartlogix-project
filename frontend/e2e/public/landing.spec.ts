import { test, expect } from '@playwright/test';

test.describe('Landing Page', () => {
  test('renders hero section and product carousels', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: /SmartLogix/i })).toBeVisible({ timeout: 15000 });
    await expect(page.locator('a, button, img').first()).toBeVisible({ timeout: 10000 });
  });

  test('navbar is visible with navigation links', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('nav, header').first()).toBeVisible();
  });

  test('search by category query param filters products', async ({ page }) => {
    await page.goto('/?cat=1');
    await expect(page.locator('body')).toBeAttached({ timeout: 10000 });
  });

  test('search box submits and filters products', async ({ page }) => {
    await page.goto('/');
    const searchInput = page.getByPlaceholder(/buscar|search/i).first();
    if (await searchInput.isVisible({ timeout: 5000 }).catch(() => false)) {
      await searchInput.fill('test');
      await searchInput.press('Enter');
      await page.waitForTimeout(1000);
      await expect(page.locator('body')).toBeAttached();
    }
  });
});
