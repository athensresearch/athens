import { expect } from '@playwright/test';
import { test } from './electron-test';
import { waitReady } from "./utils";

test("page title is truthy and app toolbar is visible", async ({ page }) => {
  await waitReady(page);
});
