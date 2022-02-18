import { Page } from '@playwright/test';

// NOTE: this is not supported by Playwright right now.
export const createLocalAthensDB = async (page:Page, dbName:string) => {
    // click db picker
    await page.click('button:nth-child(1)');
    // click "Add Database"
    await page.click('button:has-text("Add Database")');

    // click "New"
    await page.click('button:has-text("New")');

    // Click [placeholder="DB Name"]
    await page.click('[placeholder="DB Name"]');

    // Fill [placeholder="DB Name"]
    await page.fill('[placeholder="DB Name"]', dbName);

    const [fileChooser] = await Promise.all([
        // TODO this is broken in Playwright for electron, no fix in sight
        page.waitForEvent('filechooser'),
        page.click('text=Browse')
      ]);
    await fileChooser.setFiles('~/my-e2e-dbs');

    await page.pause()
};

export const getTitle = async (page:Page) => {
    const titleLocator = await page.locator("h1 >> nth=0");
    const titleText = await titleLocator.innerText();
    return titleText;
};

export const waitReady = async (page:Page) => {
    await getTitle(page);
};

export const saveLastBlock = async (page:Page, text:string) => {
    await page.click('.textarea >> nth=-1');
    await page.fill('.textarea >> nth=-1', text);
    return page.press('.textarea >> nth=-1', 'ArrowUp');
};

export const saveLastBlockAndEnter = async (page:Page, text:string) => {
    await page.click('.textarea >> nth=-1');
    await page.fill('.textarea >> nth=-1', text);
    return page.press('.textarea  >> nth=-1', 'Enter');
};

export const indentLastBlock = async (page:Page) => {
    await page.click('.textarea >> nth=-1');
    return page.press('.textarea  >> nth=-1', 'Tab');
};

export const unindentLastBlock = async (page:Page) => {
    await page.click('.textarea >> nth=-1');
    return page.press('.textarea  >> nth=-1', 'Shift+Tab');
};
