import { expect } from '@playwright/test';
import { test } from './electron-test';
import { indentLastBlock, saveLastBlockAndEnter, unindentLastBlock, waitForBoot, createPage } from "./utils";

const testSetup = async (page:Page) => {

    await waitForBoot(page);
    await createPage(page, 'selection test');
    await saveLastBlockAndEnter(page, "first");
    await page.waitForTimeout(1000);
    await indentLastBlock(page);
    const bigBlock = "big block\n".repeat(50)

    await saveLastBlockAndEnter(page, bigBlock);
    await saveLastBlockAndEnter(page, "alice");
    await saveLastBlockAndEnter(page, "bob");

    const boundingBoxBefore = await page.locator('.block:has-text("first")').boundingBox()
    // Less than 0 means it is above the top of the window.
    expect(boundingBoxBefore.y).toBeLessThan(0);

    await page.dragAndDrop("text=bob",  "text=alice");
    await page.waitForTimeout(1000);

    const boundingBoxAfter = await page.locator('.block:has-text("first")').boundingBox()
    expect(boundingBoxAfter.y).toBeLessThan(0);
}
