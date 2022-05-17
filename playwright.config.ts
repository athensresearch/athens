import type { PlaywrightTestConfig } from '@playwright/test';
import * as path from 'path';

const outputDir = path.join(__dirname, 'test-results');
export const baseConfig: PlaywrightTestConfig = {
  outputDir,
  testDir: './test/e2e',
  timeout: 30000,
  globalTimeout: 5400000,
  forbidOnly: !!process.env.CI,
  preserveOutput: process.env.CI ? 'failures-only' : 'always',
  retries: process.env.CI ? 3 : 0,
};

const config: PlaywrightTestConfig = {
  ...baseConfig,
  webServer: {
    command: 'yarn client:e2e:server',
    // NB: This is the same port as the shadow-cljs web app server,
    // so it will be reused if available.
    port: 3000,
    timeout: 120 * 1000,
    reuseExistingServer: !process.env.CI,
  },
  use: {
    baseURL: 'http://localhost:3000',
    browserName: 'chromium',
    headless: true,
  }
};

export default config;
