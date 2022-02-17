import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitReady, todaysDate, getTitle } from "./utils";

test("page is ready because current page after boot is today's date", async ({ page }) => {
  expect(await todaysDate(page)).toBe(await getTitle(page));
});

