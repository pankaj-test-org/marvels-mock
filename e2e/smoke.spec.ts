import { test, expect } from '@playwright/test';

test('browser launches and evaluates script', async ({ page }) => {
  await page.setContent('<div id="app">ready</div>');
  const text = await page.locator('#app').innerText();
  expect(text).toBe('ready');
});

test('mock api response parses as json', () => {
  const body =
    '{"code":200,"status":"Ok","data":{"results":[{"id":1011334,"name":"3-D Man"}]}}';
  const parsed = JSON.parse(body);

  expect(parsed.code).toBe(200);
  expect(parsed.data.results).toHaveLength(1);
  expect(parsed.data.results[0].name).toBe('3-D Man');
});
