import { defineConfig, type ReporterDescription } from '@playwright/test';

// The JSON reporter writes to a fixed path, so a job that invokes `playwright
// test` more than once overwrites its report each time and only the last run
// survives. Set PLAYWRIGHT_JSON_OUTPUT_FILE per invocation to give each run its
// own report; Playwright reads that variable ahead of the `outputFile` below.
//
// The blob reporter is only attached when JOB_NAME is set, which is what a
// sharded run needs in order to merge reports across jobs.
const jobName = process.env.JOB_NAME;

const reporter: ReporterDescription[] = [
  ['list'],
  ['json', { outputFile: 'results.json' }],
];

if (jobName) {
  reporter.push(['blob', { outputFile: `./blob-report/report-${jobName}.zip` }]);
}

export default defineConfig({
  testDir: './e2e',
  reporter,
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
  ],
});
