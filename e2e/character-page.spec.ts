import { test, expect } from '@playwright/test';

const CHARACTER = {
  id: 1011334,
  name: 'Spider-Man',
  description: 'A mock character from Marvel API',
};

function renderCharacter(c: typeof CHARACTER): string {
  return `
    <html>
      <head><title>Marvel Mock - ${c.name}</title></head>
      <body>
        <h1>${c.name}</h1>
        <p data-testid="description">${c.description}</p>
        <span data-testid="id">${c.id}</span>
      </body>
    </html>
  `;
}

test.describe('character detail page', () => {
  test.beforeEach(async ({ page }) => {
    await page.setContent(renderCharacter(CHARACTER));
  });

  test('shows the character name as the heading', async ({ page }) => {
    await expect(page.locator('h1')).toHaveText(CHARACTER.name);
  });

  test('shows the description and id', async ({ page }) => {
    await expect(page.getByTestId('description')).toHaveText(CHARACTER.description);
    await expect(page.getByTestId('id')).toHaveText(String(CHARACTER.id));
  });

  test('sets the page title from the character name', async ({ page }) => {
    await expect(page).toHaveTitle(`Marvel Mock - ${CHARACTER.name}`);
  });
});

test('falls back to the default character when no name is given', async ({ page }) => {
  await page.setContent(renderCharacter({ ...CHARACTER, name: '3-D Man' }));
  await expect(page.locator('h1')).toHaveText('3-D Man');
});
