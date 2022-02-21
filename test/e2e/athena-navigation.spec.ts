import { expect } from '@playwright/test';
import { test } from './electron-test';
import { deleteCurrentPage, inputInAthena } from "./utils";


test('athena create new page then enter', async ({ page }) => {
  const title = 'create-enter'
  await inputInAthena(page, title);

  // Press Enter
  await Promise.all([
    page.press('[placeholder="Find or Create Page"]', 'Enter')]);
  await expect(page.locator(".node-page > header > h1 > textarea")).toHaveValue(title);

  await deleteCurrentPage(page);
});

test('athena create new page then click create page', async ({ page }) => {
  const title = 'create-click';
  await inputInAthena(page, title);
  // Click text=Create Page: arst
  await Promise.all([
    page.click('text=Create Page: ' + title)]);
  await expect(page.locator(".node-page > header > h1 > textarea")).toHaveValue(title);

  await deleteCurrentPage(page);
});

test('athena search block then enter on result', async ({ page }) => {
  await inputInAthena(page, 'welcome');

  // Press ArrowDown
  await page.press('[placeholder="Find or Create Page"]', 'ArrowDown');
  // Press ArrowDown
  await page.press('[placeholder="Find or Create Page"]', 'ArrowDown');
  // Press Enter
  await page.press('[placeholder="Find or Create Page"]', 'Enter');

  await expect(page.locator(".block-page > h1 > textarea")).toHaveValue('Welcome to Athens, Open-Source Networked Thought!');
});

test('athena search block then click on result', async ({ page }) => {
  await inputInAthena(page, 'welcome');
  // Click text=WelcomeWelcome to Athens, Open-Source Networked Thought!
  await page.click('text=WelcomeWelcome to Athens, Open-Source Networked Thought!');
  await expect(page.locator(".block-page > h1 > textarea")).toHaveValue('Welcome to Athens, Open-Source Networked Thought!');

});

