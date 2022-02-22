import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitForBoot, pageTitleLocator, todaysDate } from './utils';

test('boot test', async ({ page }) => {
  await waitForBoot(page);
  // The search button on the toolbar is visible.
  await expect(page.locator('text=Find or create a page').first()).toBeVisible();
  // Todays daily note is visible.
  await expect(page.locator(pageTitleLocator)).toHaveText(await todaysDate(page));
});
