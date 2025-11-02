import { test as base, expect, Page } from '@playwright/test';
import { createTestCart } from './test-data';

type TestFixtures = {
  authenticatedPage: Page;
  cartPage: Page;
};

/**
 * Extended test fixtures with common setup
 */
export const test = base.extend<TestFixtures>({
  /**
   * Page with pre-created cart
   */
  authenticatedPage: async ({ page }, use) => {
    // Create a cart via API
    const cartId = await createTestCart();

    // Navigate to home page
    await page.goto('/');

    // Store cart ID in localStorage
    await page.evaluate((id) => {
      localStorage.setItem('cartId', id);
    }, cartId);

    await use(page);
  },

  /**
   * Page already on cart view with items
   */
  cartPage: async ({ page }, use) => {
    await page.goto('/');
    // 1. Go to catalog
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]', { timeout: 10000 });

    // 2. Add an item to the cart
    await page.locator('[data-testid="add-to-cart-btn"]').first().click();

    // 3. Wait for toast notification to appear and disappear (or at least show)
    await page.waitForSelector('.Toastify__toast', { timeout: 5000 }).catch(() => {
      // Toast might not appear, that's ok
    });

    // 4. Go to the cart page
    await page.click('[data-testid="cart-tab"]');

    // 5. Wait for cart items to load with increased timeout
    await page.waitForSelector('[data-testid="cart-item"]', { timeout: 15000 });

    await use(page);
  },
});

export { expect };

/**
 * Helper to wait for API response
 */
export async function waitForApiResponse(
  page: Page,
  urlPattern: string | RegExp,
  action: () => Promise<void>
) {
  const responsePromise = page.waitForResponse(urlPattern);
  await action();
  return await responsePromise;
}

/**
 * Helper to get cart ID from localStorage
 */
export async function getCartId(page: Page): Promise<string | null> {
  return await page.evaluate(() => localStorage.getItem('cartId'));
}

/**
 * Helper to clear cart from localStorage
 */
export async function clearCart(page: Page): Promise<void> {
  await page.evaluate(() => localStorage.removeItem('cartId'));
}
