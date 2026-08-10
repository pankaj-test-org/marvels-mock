import { defineConfig, type ReporterDescription } from '@playwright/test';

// Separate config so the CBP-45873 repro suite stays out of the main `e2e/`
// run — playwright-test-results.yaml keeps publishing its own 10 tests.
//
// The blob reporter is only attached when JOB_NAME is set. That mirrors
// cbp-test-automation: pods that export JOB_NAME per invocation get one blob
// per run and can merge them; pods that don't collapse every run onto the same
// results.json and lose all but the last.
const jobName = process.env.JOB_NAME;

const reporter: ReporterDescription[] = [
  ['list'],
  ['json', { outputFile: 'results.json' }],
];

if (jobName) {
  reporter.push(['blob', { outputFile: `./blob-report/report-${jobName}.zip` }]);
}

export default defineConfig({
  testDir: './e2e-cbp-45873',
  reporter,
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
  ],
});
