import { expect, Page } from '@playwright/test';
import { test } from './electron-test';
import {
    indentLastBlock, saveLastBlock, saveLastBlockAndEnter, unindentLastBlock,
    waitForBoot, createPage, deleteCurrentPage
} from "./utils";

const testSetup = async (page:Page) => {
    await saveLastBlockAndEnter(page, "test block 1");
    await saveLastBlockAndEnter(page, "test block 2");
    await indentLastBlock(page);
    await saveLastBlockAndEnter(page, "test block 3");
    await unindentLastBlock(page);
    await saveLastBlock(page, "test block 4");

    await page.press('text=test block 3', 'ArrowUp');
    await page.press('text=test block 2', 'ArrowUp');
};

test('delete-merge-looses-children', async ({ page }) => {
    await waitForBoot(page);
    await createPage(page, 'test delete doesn\'t merge');

    await testSetup(page);

    await page.click('text=test block 1');
    await page.press('text=test block 1', "End");
    await page.press('text=test block 1', 'Delete');
    // await page.pause();
    // console.log(page.locator('.block:has-text("test block 3")'));
    await expect(page.locator('.block:has-text("test block 3")')).toBeVisible();

    await deleteCurrentPage(page);
});
