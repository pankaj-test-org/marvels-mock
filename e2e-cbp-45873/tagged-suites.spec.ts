import { test, expect } from '@playwright/test';

// Tagged to mirror the cbp-test-automation pods, which invoke `playwright test`
// once per tag (@Login, then @cleanup, then the main suite) inside a single step.
// Each invocation rewrites results.json, so only the last tag's tests survive
// unless the runs are merged from blob reports. See CBP-45873.

test('signs in with a seeded account', { tag: '@Login' }, async ({ page }) => {
  await page.setContent('<div id="user">pankaj@example.com</div>');
  await expect(page.locator('#user')).toHaveText('pankaj@example.com');
});

test('stores the session token', { tag: '@Login' }, () => {
  const session = { token: 'abc123', expiresIn: 3600 };
  expect(session.token).toHaveLength(6);
  expect(session.expiresIn).toBeGreaterThan(0);
});

test('removes seeded characters', { tag: '@cleanup' }, () => {
  const seeded = ['3-D Man', 'Spider-Man', 'Iron-Man'];
  const remaining = seeded.filter((name) => !seeded.includes(name));
  expect(remaining).toHaveLength(0);
});

test('releases the mock service port', { tag: '@cleanup' }, () => {
  const released: number[] = [];
  released.push(8080);
  expect(released).toContain(8080);
});

test('lists characters with a limit', { tag: '@regression' }, () => {
  const results = Array.from({ length: 10 }, (_, i) => ({ id: 1011334 + i }));
  expect(results.slice(0, 5)).toHaveLength(5);
});

test('renders the character grid', { tag: '@regression' }, async ({ page }) => {
  await page.setContent(
    '<ul id="grid"><li>3-D Man</li><li>Spider-Man</li></ul>',
  );
  await expect(page.locator('#grid li')).toHaveCount(2);
});

test('reports a not-found character', { tag: '@regression' }, () => {
  const lookup = (name: string) => (name === 'Galactus' ? null : { name });
  expect(lookup('Galactus')).toBeNull();
});
