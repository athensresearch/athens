import { expect } from '@playwright/test';
import { test } from './electron-test';

test('basic test', async ({ page }) => {
  // Wait for an element that signals the app has finished loading.
  // Normally on e2e tests we'd load a page, but on a electron app we should rely
  // only on visible behaviour.
  // TODO: This isn't necessary on production builds, but is necessary for dev
  // builds, not sure why. Maybe because the app runs slower?
  await page.waitForSelector("text=Find");
  // The search button on the toolbar is visible.
  await expect(page.locator('text=Find or create a page').first()).toBeVisible();
});
