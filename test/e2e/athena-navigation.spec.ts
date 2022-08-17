import { expect } from '@playwright/test';
import { test } from './electron-test';
import { deleteCurrentPage, inputInAthena, pageTitleLocator, athenaInputFieldLocator, waitForPageNavigation, waitForBoot } from "./utils";


test('athena create new page then enter', async ({ page }) => {
  const title = 'create-enter';
  await waitForBoot(page);
  await inputInAthena(page, title);

  // Press Enter
  page.press(athenaInputFieldLocator, 'Enter'),
  await waitForPageNavigation(page, title);

  await deleteCurrentPage(page);
});

test('athena create new page then click create page', async ({ page }) => {
  const title = 'create-click';
  await waitForBoot(page);
  await inputInAthena(page, title);

  page.click('text=Create page' + title),
  await waitForPageNavigation(page, title);

  await deleteCurrentPage(page);
});

test('athena search block then enter on result', async ({ page }) => {
  const title = 'Welcome to Athens, Open-Source Networked Thought!';
  await waitForBoot(page);
  await inputInAthena(page, 'welcome');

  // Press ArrowDown
  await page.press(athenaInputFieldLocator, 'ArrowDown');
  // Press ArrowDown
  await page.press(athenaInputFieldLocator, 'ArrowDown');
  // Press Enter
  await page.press(athenaInputFieldLocator, 'Enter');

  await waitForPageNavigation(page, title);
});

test('athena search block then click on result', async ({ page }) => {
  const title = 'Welcome to Athens, Open-Source Networked Thought!';
  await waitForBoot(page);
  await inputInAthena(page, 'welcome');
  // Click text=WelcomeWelcome to Athens, Open-Source Networked Thought!
  await page.click('text=' + title);
  await waitForPageNavigation(page, title);
});
