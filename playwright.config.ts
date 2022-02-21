import type { PlaywrightTestConfig } from '@playwright/test';
import * as path from 'path';

// Electron setup taken from https://gist.github.com/UberMouse/facbe751c3ecb9b31e8b4f6221567b7a
// mentioned in https://github.com/microsoft/playwright/issues/8208#issuecomment-948093888.
//
// Some more useful sources on this topic:
// - replacing spectron with playwright: https://github.com/electron-userland/spectron/issues/896
// - playwright repo electron tests: https://github.com/microsoft/playwright/tree/main/tests/electron
// - playwright electron support: https://playwright.dev/docs/api/class-electronapplication
//   and https://playwright.dev/docs/api/class-electron
// - playwright+electron starter: https://github.com/spaceagetv/electron-playwright-example

const outputDir = path.join(__dirname, 'test-results');
const config: PlaywrightTestConfig = {
  outputDir,
  testDir: './test/e2e',
  timeout: 10000,
  globalTimeout: 5400000,
  workers: process.env.CI ? 1 : undefined,
  forbidOnly: !!process.env.CI,
  preserveOutput: process.env.CI ? 'failures-only' : 'always',
  retries: process.env.CI ? 3 : 0,
  reporter: process.env.CI ? [
    [ 'dot' ],
    [ 'json', { outputFile: path.join(outputDir, 'report.json') } ],
  ] : 'line',
  projects: [{
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
  }],
};

export default config;
