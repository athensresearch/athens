import * as path from 'path';
import type { Fixtures } from "@playwright/test";
import { test as base } from "@playwright/test";
import { BrowserContext, ElectronApplication, Page, _electron } from "playwright";

// athens.listeners/prevent-save checks for this variable in local storage
// and will not present the confirmation window that stops closing athens.
const disableSaveCheck = () => window.localStorage.setItem("E2E_IGNORE_SAVE_CHECK", "true");
const enableSaveCheck = () => window.localStorage.removeItem("E2E_IGNORE_SAVE_CHECK");

type ElectronTestFixtures = {
  electronApp: ElectronApplication;
  page: Page;
  context: BrowserContext;
};

export const electronFixtures: Fixtures<ElectronTestFixtures> = {
  electronApp: async ({}, run) => {
    // This env prevents 'Electron Security Policy' console message.
    process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = "true";
    const electronApp = await _electron.launch({
      // matches ../../package.json#main
      args: [path.join(__dirname, '../../resources/main.js')],
    });
    await run(electronApp);
    if (process.env.CI) {
      // TODO: Should await on close here but it doesn't seem to work on the CI.
      // If you edit the client:e2e:only:verbose script to debug on pw:* you can see
      // more detailed logs on CI and it seems like the electron process responds
      // that it closed, but playwright does not recognize. Maybe we need to
      // update electron beyond v12.
      electronApp.close();
      await new Promise(resolve => setTimeout(resolve, 1000));
    } else {
      await electronApp.close();
    }
  },
  page: async ({ electronApp }, run) => {
    const page = await electronApp.firstWindow();

    // Always disable the save check on exit, because it needs special handling and
    // would delay every test by 15s.
    // If you want to test it, call `page.evaluate(enableSaveCheck)` on your test.
    page.evaluate(disableSaveCheck);

    await run(page);
  },
  context: async ({ electronApp }, run) => {
    const context = electronApp.context();

    await run(context);
  },
};

// @ts-ignore some error about a string type now having `undefined` as part of it's union
export const test = base.extend<ElectronTestFixtures>(electronFixtures);

