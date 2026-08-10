import { test, expect } from '@playwright/test';

// Mirrors MarvelMockController.getQueryParam semantics: pairs without a
// non-empty value are dropped, so ?name= resolves to null.
function getQueryParam(url: string, param: string): string | null {
  const query = new URL(url).search.replace(/^\?/, '');
  if (!query) return null;

  const params = new Map<string, string>();
  for (const pair of query.split('&')) {
    const [key, value] = pair.split('=');
    if (value) params.set(key, value);
  }
  return params.get(param) ?? null;
}

const BASE = 'http://localhost:8080/v1/public/characters';

test('resolves a single query param', () => {
  expect(getQueryParam(`${BASE}?name=Spider-Man`, 'name')).toBe('Spider-Man');
});

test('returns null when the query string is absent', () => {
  expect(getQueryParam(BASE, 'name')).toBeNull();
});

test('resolves each of multiple query params', () => {
  const url = `${BASE}?name=Iron-Man&limit=10`;
  expect(getQueryParam(url, 'name')).toBe('Iron-Man');
  expect(getQueryParam(url, 'limit')).toBe('10');
});

test('treats an empty value as absent', () => {
  expect(getQueryParam(`${BASE}?name=`, 'name')).toBeNull();
});
