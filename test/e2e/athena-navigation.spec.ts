import { expect } from '@playwright/test';
import { test } from './electron-test';
import { deleteCurrentPage, inputInAthena, pageTitleLocator, waitForBoot } from "./utils";


test('athena create new page then enter', async ({ page }) => {
  const title = 'create-enter';
  await waitForBoot(page);
  await inputInAthena(page, title);

  // Press Enter
  await Promise.all([
    page.press('[placeholder="Find or Create Page"]', 'Enter'),
    page.waitForNavigation()
  ]);
  await expect(page.locator(pageTitleLocator)).toHaveText(title);

  await deleteCurrentPage(page);
});

test('athena create new page then click create page', async ({ page }) => {
  const title = 'create-click';
  await waitForBoot(page);
  await inputInAthena(page, title);

  await Promise.all([
    page.click('text=Create page' + title),
    page.waitForNavigation()
  ]);
  await expect(page.locator(pageTitleLocator)).toHaveText(title);

  await deleteCurrentPage(page);
});

test('athena search block then enter on result', async ({ page }) => {
  const title = 'Welcome to Athens, Open-Source Networked Thought!';
  await waitForBoot(page);
  await inputInAthena(page, 'welcome');

  // Press ArrowDown
  await page.press('[placeholder="Find or Create Page"]', 'ArrowDown');
  // Press ArrowDown
  await page.press('[placeholder="Find or Create Page"]', 'ArrowDown');
  // Press Enter
  await page.press('[placeholder="Find or Create Page"]', 'Enter');
  await expect(page.locator(pageTitleLocator)).toHaveText(title);
});

test('athena search block then click on result', async ({ page }) => {
  const title = 'Welcome to Athens, Open-Source Networked Thought!';
  await waitForBoot(page);
  await inputInAthena(page, 'welcome');
  // Click text=WelcomeWelcome to Athens, Open-Source Networked Thought!
  await page.click('text=' + title);
  await expect(page.locator(pageTitleLocator)).toHaveText(title);
});

