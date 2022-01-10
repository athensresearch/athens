import { expect, Page } from '@playwright/test';
import { test } from './electron-test';
import { createLocalAthensDB, indentLastBlock, saveLastBlock, saveLastBlockAndEnter, unindentLastBlock } from "./utils";

const testSetup = async (page:Page) => {
    // NOTE this ain't working, and that's ok
    // await createLocalAthensDB(page, "amazing");

    // await page.pause();
    // Navigate to daily pages, Click button:nth-child(6)
    await Promise.all ([
        page.click('button:nth-child(6)'),
        page.waitForNavigation()
    ]);

    // Click textarea
    await page.click('textarea');

    // Invoke Athena, Press k with modifiers
    await page.click('button:has-text("Find or create a page")');

    // Fill [placeholder="Find or Create Page"]
    await page.fill('[placeholder="Find or Create Page"]', 'test delete doesn\'t merge');

    // Press Enter
    await Promise.all([
        page.press('[placeholder="Find or Create Page"]', 'Enter'),
        page.waitForNavigation()
    ]);

    await saveLastBlockAndEnter(page, "test block 1");
    await saveLastBlockAndEnter(page, "test block 2");
    await indentLastBlock(page);
    await saveLastBlockAndEnter(page, "test block 3");
    await unindentLastBlock(page);
    await saveLastBlock(page, "test block 4");

    await page.press('text=test block 3', 'ArrowUp');
    await page.press('text=test block 2', 'ArrowUp');
},
testCleanup = async (page:Page) => {
    await page.click(".node-page > header > button");
    // Click button:has-text("Delete Page")
    await page.click('button:has-text("Delete Page")');
    // Click button:nth-child(6)
    await page.click('button:nth-child(6)');

};

test('delete-merge-looses-children', async ({ page }) => {
    await testSetup(page);

    await page.click('text=test block 1');
    await page.press('text=test block 1', "End");
    await page.press('text=test block 1', 'Delete');
    // await page.pause();
    // console.log(page.locator('.block:has-text("test block 3")'));
    await expect(page.locator('.block:has-text("test block 3")')).toBeVisible();

    await testCleanup(page);
});
