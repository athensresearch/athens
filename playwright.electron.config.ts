import type { PlaywrightTestConfig } from '@playwright/test';
import { baseConfig } from './playwright.config';

// Electron setup taken from https://gist.github.com/UberMouse/facbe751c3ecb9b31e8b4f6221567b7a
// mentioned in https://github.com/microsoft/playwright/issues/8208#issuecomment-948093888.
//
// Some more useful sources on this topic:
// - replacing spectron with playwright: https://github.com/electron-userland/spectron/issues/896
// - playwright repo electron tests: https://github.com/microsoft/playwright/tree/main/tests/electron
// - playwright electron support: https://playwright.dev/docs/api/class-electronapplication
//   and https://playwright.dev/docs/api/class-electron
// - playwright+electron starter: https://github.com/spaceagetv/electron-playwright-example

// Set env as using electron config, will be picked up by isElectron in utils.
process.env.ELECTRON_PLAYWRIGHT_CONFIG = "true";

const config: PlaywrightTestConfig = {
  ...baseConfig,
  workers: 1,
  use: {},
  projects: [ {
    name: 'chromium',
    use: {
      browserName: 'chromium',
      trace: process.env.CI ? 'on-first-retry' : 'on'
    },
    metadata: {
      platform: process.platform,
      headful: true,
      browserName: 'electron',
      channel: undefined,
      mode: 'default',
      video: false,
    }
  } ],
};

export default config;
