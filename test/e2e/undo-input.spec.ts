import { expect } from '@playwright/test';
import { test } from './electron-test';
import {saveLastBlockAndEnter, waitForBoot, createPage, deleteCurrentPage, isMac} from "./utils";

const modifier = isMac ? 'Meta' : 'Control';
const undoShortcut = `${modifier}+Z`;
const redoShortcut = `${modifier}+Shift+Z`;

test('undo input test', async ({ page }) => {
    // Setup.
    await waitForBoot(page);
    await createPage(page, 'undo input');

    // Make three blocks, but don't make a fourth.
    // Wait a bit for transaction and focus events to be resolved between input.
    await saveLastBlockAndEnter(page, "one");
    await saveLastBlockAndEnter(page, "two");
    await saveLastBlockAndEnter(page, "three");

    // All three blocks should be there.
    await expect(page.locator('.block:has-text("one")')).toBeVisible();
    await expect(page.locator('.block:has-text("two")')).toBeVisible();
    await expect(page.locator('.block:has-text("three")')).toBeVisible();

    // Undo once, removing the last block input and split.
    await page.keyboard.press(undoShortcut);

    // Last block should be gone.
    await expect(page.locator('.block:has-text("one")')).toBeVisible();
    await expect(page.locator('.block:has-text("two")')).toBeVisible();
    await expect(page.locator('.block:has-text("three")')).not.toBeVisible();

    // Undo two more times to remove the first two blocks.
    await page.keyboard.press(undoShortcut);
    await page.keyboard.press(undoShortcut);

    // All blocks should be gone.
    await expect(page.locator('.block:has-text("one")')).not.toBeVisible();
    await expect(page.locator('.block:has-text("two")')).not.toBeVisible();
    await expect(page.locator('.block:has-text("three")')).not.toBeVisible();

    // Redo three times to restore blocks.
    await page.keyboard.press(redoShortcut);
    await page.keyboard.press(redoShortcut);
    await page.keyboard.press(redoShortcut);

    // All three blocks should be back.
    await expect(page.locator('.block:has-text("one")')).toBeVisible();
    await expect(page.locator('.block:has-text("two")')).toBeVisible();
    await expect(page.locator('.block:has-text("three")')).toBeVisible();

    // Teardown.
    await deleteCurrentPage(page);
});

