import { expect } from '@playwright/test';
import { test } from './electron-test';
import { indentLastBlock, saveLastBlockAndEnter, unindentLastBlock, waitReady } from "./utils";

const testSetup = async (page:Page) => {

    await waitReady(page);
    await saveLastBlockAndEnter(page, `Deserunt id debitis atque. Eaque consequatur et animi omnis error laudantium excepturi occaecati. In et nam quia ut dicta. Ratione deleniti voluptas dolorum odio cumque.`);
    await indentLastBlock(page);
    await saveLastBlockAndEnter(page, "alice");
    await saveLastBlockAndEnter(page, "bob");

    // before scrolling down
    const alice = await page.$("text=alice");
    const aliceBb1 = await alice.boundingBox();
    const dailyNotes = await page.$("#daily-notes");
    const dailyNotesBB1 = await dailyNotes.boundingBox();

    // scroll down approximately the height of alice block
    await page.mouse.wheel(0, aliceBb1.y - aliceBb1.height);
    // wait until scroll event finishes
    await page.waitForTimeout(1000);
    // get boundingBox after scrolling down
    const dailyNotesBB2 = await dailyNotes.boundingBox();
    // y values should be different because of scrolling down
    expect(dailyNotesBB1.y).not.toBe(dailyNotesBB2.y);
    // drag and drop up from bob to alice
    await page.dragAndDrop("textarea >> text=bob", "textarea >> text=alice");
    // wait in the case that there is a scroll event
    await page.waitForTimeout(1000);
    // get boundingBox after scrolling down in the case that there is a scroll event
    const dailyNotesBB3 = await dailyNotes.boundingBox();
    // height should be unchanged
    expect(dailyNotesBB2.y).toBe(dailyNotesBB3.y);
}

test('new-test-template', async ({ page }) => {
  await testSetup(page)

});