import * as path from 'path';
import type { Fixtures } from "@playwright/test";
import { test as base } from "@playwright/test";
import { BrowserContext, ElectronApplication, Page, _electron } from "playwright";
import { isElectron } from './utils';

// athens.listeners/prevent-save and athens.electron.fs/write-db check for this
// variable in local storage and will not perform saves or warn about unsaved changes.
const disableSave = () => window.localStorage.setItem("E2E_IGNORE_SAVE", "true");
const enableSave = () => window.localStorage.removeItem("E2E_IGNORE_SAVE");

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

    // Always disable save and save check on exit, because it needs special handling and
    // would delay every test by 15s.
    // If you want to test it, call `page.evaluate(enableSave)` on your test.
    page.evaluate(disableSave);

    await run(page);
  },
  context: async ({ electronApp }, run) => {
    const context = electronApp.context();

    await run(context);
  },
};

// @ts-ignore some error about a string type now having `undefined` as part of it's union
// export const test = base.extend<ElectronTestFixtures>(electronFixtures);
const electronTest = base.extend<ElectronTestFixtures>(electronFixtures);
const browserTest = base;
export const test = isElectron ? electronTest : browserTest;
