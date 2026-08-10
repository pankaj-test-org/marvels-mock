import { test, expect } from '@playwright/test';

// The final invocation in the pod sequence, and the one that empties the tab.
//
// When RUN_TEARDOWN is unset the describe block is never registered, so this
// file contributes no tests at all. The run collects nothing, exits 0 (with
// --pass-with-no-tests), and the JSON reporter still writes results.json -
// blanking everything the earlier runs recorded. That is the empty-tab case of
// CBP-45873: green pipeline, {"suites": [], "expected": 0}.
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
