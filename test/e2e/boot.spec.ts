import { expect } from '@playwright/test';
import { test } from './electron-test';
import { getTitle } from "./utils";

test("page title exists", async ({ page }) => {
  expect(await getTitle(page)).toBeTruthy();
});
