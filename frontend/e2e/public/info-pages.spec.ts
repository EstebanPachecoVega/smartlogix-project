import { test, expect } from '@playwright/test';

const INFO_PAGES = [
  '/quienes-somos',
  '/terminos-y-condiciones',
  '/privacidad-y-seguridad',
  '/devolucion-y-reembolso',
  '/entrega-y-envios',
  '/centro-de-ayuda',
];

test.describe('Static Info Pages', () => {
  for (const path of INFO_PAGES) {
    test(`${path} renders without error`, async ({ page }) => {
      await page.goto(path);
      await expect(page.locator('h1').first()).toBeVisible({ timeout: 10000 });
    });
  }
});
