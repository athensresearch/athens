import { Page } from '@playwright/test';

export const isMac = process.platform === "darwin";

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

export const lastBlockSelector = '.textarea >> nth=-1';

export const inputInLastBlock = async (page:Page, text:string) => {
    await page.click(lastBlockSelector);
    await page.fill(lastBlockSelector, text);
};

export const saveLastBlock = async (page:Page, text:string) => {
    await inputInLastBlock(page, text);
    // Move away from the block to force a save.
    return page.press(lastBlockSelector, 'ArrowUp');
};

export const saveLastBlockAndEnter = async (page:Page, text:string) => {
    await inputInLastBlock(page, text);
    await page.press(lastBlockSelector, 'Enter');
    // Wait a bit for transaction and focus events to be resolved between saves.
    // Without waiting, it's possible for multiple calls to end up in the same block instead of
    // different blocks, because by the time the second call runs the new block hasn't appeared yet.
    // TODO: we shouldn't need to do this, instead we should have deterministic states from input.
    await page.waitForTimeout(200);
};

export const indentLastBlock = async (page:Page) => {
    await page.click(lastBlockSelector);
    return page.press(lastBlockSelector, 'Tab');
};

export const unindentLastBlock = async (page:Page) => {
    await page.click(lastBlockSelector);
    return page.press(lastBlockSelector, 'Shift+Tab');
};


export const goToDailyPages = async (page:Page) => {
    // The sixth button is the daily notes button.
    // TODO: find a better way to address this button, maybe tooltip?
    await page.click('button:nth-child(6)');
}

export const waitForBoot = async (page:Page) => {
    // Wait for an element that signals the app has finished loading.
    // Normally on e2e tests we'd load a page, but on a electron app we should rely
    // only on visible behaviour.
    // TODO: This isn't necessary on production builds, but is necessary for dev
    // builds, not sure why. Maybe because the app runs slower?
    await page.waitForSelector("text=Find");
}

export const inputInAthena = async (page:Page, query:string) => {
    await page.click('button:has-text("Find or create a page")');
    await page.fill('[placeholder="Find or Create Page"]', query);
}

export const createPage = async (page:Page, title:string) => {
    await inputInAthena(page, title);

    // Press Enter
    await Promise.all([
        page.press('[placeholder="Find or Create Page"]', 'Enter'),
        page.waitForNavigation()
    ]);
}

export const deleteCurrentPage = async (page:Page) => {
    // Open page elipsis menu
    await page.click(".node-page > header > button");
    await page.click('button:has-text("Delete Page")');
    await page.click('button:nth-child(6)');
}
