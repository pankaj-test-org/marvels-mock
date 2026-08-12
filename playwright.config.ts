import { defineConfig } from '@playwright/test';

// The JSON reporter writes to a fixed path, so a job that invokes `playwright
// test` more than once overwrites its report each time and only the last run
// survives. Set PLAYWRIGHT_JSON_OUTPUT_FILE per invocation to give each run its
// own report; Playwright reads that variable ahead of the `outputFile` below.
export default defineConfig({
  reporter: [
    ['list'],
    ['json', { outputFile: 'results.json' }],
  ],
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
  ],
});
