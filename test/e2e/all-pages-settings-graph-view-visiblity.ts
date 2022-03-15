import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitForBoot, pageTitleLocator, todaysDate } from './utils';

test('boot test', async ({ page }) => {
  await waitForBoot(page);

  // All pages
  await page.click('button:nth-child(4)');
  await expect(page.locator("text=MODIFIED")).toBeVisible();

 // Graph view
 await page.click('button:nth-child(5)');
 await expect(page.locator("text=Forces")).toBeVisible();

 // Settings page
 await page.click('.AppToolbar__MainControls-sc-1t9n21n-3.AppToolbar__SecondaryControls-sc-1t9n21n-4 button:nth-child(2)');
 await expect(page.locator("text=You are using the free version of Athens. You are hosting your own data. Please be careful!")).toBeVisible();
});
