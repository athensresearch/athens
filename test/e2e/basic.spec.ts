import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitForBoot } from './utils';

test('basic test', async ({ page }) => {
  await waitForBoot(page);
  // The search button on the toolbar is visible.
  await expect(page.locator('text=Find or create a page').first()).toBeVisible();
});
