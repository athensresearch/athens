import { expect } from '@playwright/test';
import { test } from './electron-test';
import { indentLastBlock, saveLastBlockAndEnter, unindentLastBlock, waitReady } from "./utils";

const rightClickFirstBullet = async (page:Page) => {
    await page.click('.block-body >> nth=0 >> svg', {
        button: 'right'
    });
};

test.describe("no blocks selected", () => {

    const testSetup = async (page:Page) => {
        await waitReady(page);
        await saveLastBlockAndEnter(page, "alice");
        await rightClickFirstBullet(page);
    }

    test('right-click one block', async ({ page }) => {
        await testSetup(page);
        expect(await page.isVisible("text='Copy block ref'")).toBe(true);
    });

    test('clicking out of the context menu onto the surrounding page closes context menu', async ({ page }) => {
        await testSetup(page)
        await page.click('#daily-notes');
        expect(await page.isVisible("text='Copy block ref'")).toBe(false);
    });

    test('clicking out of the context menu on the block itself closes context menu', async ({ page }) => {
        await testSetup(page)
        await page.click('text=alice');
        expect(await page.isVisible("text='Copy block ref'")).toBe(false);
    });

})

test('right-click when one block is selected', async ({ page }) => {
    expect(await page.isVisible("text='Copy block ref'"));
});

test.describe("multiple blocks selected", () => {
    const testSetup = async (page:Page) => {
        await saveLastBlockAndEnter(page, "alice");
        await page.waitForTimeout(100);
        await saveLastBlockAndEnter(page, "bob");
        await page.waitForTimeout(100);
        await page.dragAndDrop("textarea >> text=bob", "textarea >> text=alice");
        await rightClickFirstBullet(page);
    };

    test('right-click when multiple blocks selected', async ({ page }) => {
//         await page.pause()
        await testSetup(page);
        expect(await page.isVisible("text='Copy block refs'")).toBe(true);
    });

})
