import { test, expect } from '@playwright/test';

// The final invocation in the sequence, and the interesting one.
//
// When RUN_TEARDOWN is unset the describe block is never registered, so this
// file contributes no tests at all. The run collects nothing, exits 0 (with
// --pass-with-no-tests), and the JSON reporter still writes a report holding
// {"suites": [], "expected": 0}.
//
// If every run shares one report path, that empty report replaces everything the
// earlier runs recorded, and a green pipeline publishes no results at all. Give
// each run its own report file and the empty one simply contributes nothing.
//
// Set RUN_TEARDOWN=true and the same invocation contributes 2 real tests, so
// the last run is no longer blank.
const RUN_TEARDOWN = process.env.RUN_TEARDOWN === 'true';

if (RUN_TEARDOWN) {
  test.describe('teardown', { tag: '@teardown' }, () => {
    test('drops the seeded database', () => {
      expect(true).toBe(true);
    });

    test('revokes the service account token', () => {
      expect(true).toBe(true);
    });
  });
}
