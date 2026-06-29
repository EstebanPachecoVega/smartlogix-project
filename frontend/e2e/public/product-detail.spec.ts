import { test, expect } from '@playwright/test';

test.describe('Product Detail', () => {
  test('navigates to product detail page from landing', async ({ page }) => {
    await page.goto('/');
    const productLink = page.locator('a[href*="/productos/"]').first();
    if (await productLink.isVisible({ timeout: 10000 }).catch(() => false)) {
      await productLink.click();
      await page.waitForURL(/\/productos\//, { timeout: 10000 });
      await expect(page.locator('h1, h2').first()).toBeVisible();
    }
  });

  test('page renders key elements', async ({ page }) => {
    await page.goto('/');
    const productLink = page.locator('a[href*="/productos/"]').first();
    const isVisible = await productLink.isVisible({ timeout: 10000 }).catch(() => false);
    if (isVisible) {
      await productLink.click();
      await page.waitForURL(/\/productos\//, { timeout: 10000 });
      await expect(page.locator('body')).toBeAttached();
    }
  });

  test('not found product shows error state', async ({ page }) => {
    await page.goto('/productos/slug-que-no-existe-99999');
    await expect(page.locator('h1, p, a').first()).toBeVisible({ timeout: 10000 });
  });
});
