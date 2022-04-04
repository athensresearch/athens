import { expect, Page } from '@playwright/test';
import { test } from './electron-test';
import { saveLastBlockAndEnter, waitForBoot, createPage, deleteCurrentPage } from "./utils";


const rightClickFirstBullet = async (page: Page) => {
  await page.click('.block-body >> nth=0 >> [aria-label="Block anchor"]', {
    button: 'right'
  });
};

test.describe("no blocks selected", () => {

  test.beforeEach(async ({ page }) => {
    await waitForBoot(page);
    await createPage(page, 'copy-refs-no-selection');
    await saveLastBlockAndEnter(page, "alice");
    await rightClickFirstBullet(page);
  });

  test.afterEach(async ({ page }) => {
    await deleteCurrentPage(page);
  });

  test('right-click one block', async ({ page }) => {
    await expect(page.locator('text="Copy block refs"')).toBeVisible();
  });

  test('clicking out of the context menu onto the surrounding page closes context menu', async ({ page }) => {
    await page.click('.node-page');
    await expect(page.locator('text="Copy block refs"')).not.toBeVisible();
  });

  // This should close the context menu but doesn't yet.
  test('clicking out of the context menu on the block itself closes context menu', async ({ page }) => {
    await page.click('text=alice');
    await expect(page.locator('text="Copy block refs"')).not.toBeVisible();
  });
})


test('copy-refs with multiple blocks selected', async ({ page }) => {
  // Setup
  await waitForBoot(page);
  await createPage(page, 'copy-refs-multiple');

  await saveLastBlockAndEnter(page, "one");
  await saveLastBlockAndEnter(page, "two");
  await saveLastBlockAndEnter(page, "three");

  // Drag and drop up from three to one
  await page.dragAndDrop("text=three", "text=one");

  await rightClickFirstBullet(page);

  // Should see an option to copy all selected refs
  await expect(page.locator('text="Copy block refs"')).toBeVisible();

  // Teardown
  await deleteCurrentPage(page);
});
