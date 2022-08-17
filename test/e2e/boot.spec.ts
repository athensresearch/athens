import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitForBoot, pageTitleLocator, todaysDate } from './utils';

test('boot test', async ({ page }) => {
  await waitForBoot(page);
  // The search button on the toolbar is visible.
  await expect(page.locator('[aria-label="Show navigation"]').first()).toBeVisible();
  // Todays daily note is visible.
  // TODO: see comment on todaysDate fn.
  // await expect(page.locator(pageTitleLocator)).toHaveText(await todaysDate(page));
});
