import { expect } from '@playwright/test';
import { test } from './electron-test';
import { saveLastBlockAndEnter, waitForBoot, createPage, deleteCurrentPage } from "./utils";


test('copy-refs', async ({ page }) => {
  // Setup
  await waitForBoot(page);
  await createPage(page, 'copy-refs');

  await saveLastBlockAndEnter(page, "one");
  await saveLastBlockAndEnter(page, "two");
  await saveLastBlockAndEnter(page, "three");

  // Drag and drop up from three to one
  await page.dragAndDrop("text=three", "text=one");

  // Right click the first bullets anchor
  await page.click('button.anchor', {button: 'right'});

  // Should see an option to copy all selected refs
  await expect(page.locator('text="Copy block refs"')).toBeVisible();

  // Teardown
  await deleteCurrentPage(page);
});
