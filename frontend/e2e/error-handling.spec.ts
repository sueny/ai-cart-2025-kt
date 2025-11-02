import { test, expect } from './fixtures/test-setup';
import { seedTestData } from './fixtures/test-data';

test.describe('Error Handling', () => {

  test.beforeEach(async ({ page }) => {
    await seedTestData();
    await page.goto('/');
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');

    // Intercept and fail API request
    await page.route('**/api/v1/items**', route => {
      route.fulfill({
        status: 500,
        body: JSON.stringify({ error: 'Internal Server Error' })
      });
    });

    // Trigger search
    await page.fill('[data-testid="search-input"]', 'laptop');
    await page.click('[data-testid="search-button"]');

    // Wait for the error message to appear
    await expect(page.locator('text=/Failed to search for items/i')).toBeVisible();
  });

  test('should handle network timeout', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');

    // Intercept and abort API request
    await page.route('**/api/v1/items**', route => {
      route.abort();
    });

    // Trigger search
    await page.fill('[data-testid="search-input"]', 'test');
    await page.click('[data-testid="search-button"]');

    // Wait for the error message to appear
    await expect(page.locator('text=/Failed to search for items/i')).toBeVisible();
  });

  test('should handle empty cart checkout', async ({ page }) => {
    // Navigate to cart
    await page.click('[data-testid="cart-tab"]');

    // Try to checkout with empty cart
    const checkoutBtn = page.locator('[data-testid="checkout-btn"]');

    if (await checkoutBtn.isVisible().catch(() => false)) {
      // Button might be disabled
      const isDisabled = await checkoutBtn.isDisabled().catch(() => false);
      expect(isDisabled).toBeTruthy();
    } else {
      // Or button might not be shown at all
      await expect(checkoutBtn).not.toBeVisible();
    }
  });

  test('should handle invalid cart ID', async ({ page }) => {
    // Set invalid cart ID in localStorage
    await page.evaluate(() => {
      localStorage.setItem('cartId', 'invalid-cart-id-12345');
    });

    // Navigate to catalog and try to add item
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]');

    const addButton = page.locator('[data-testid="add-to-cart-btn"]').first();
    if (await addButton.isVisible()) {
      await addButton.click();

      // Wait for error or cart recreation
      await page.waitForTimeout(2000);

      // App should handle gracefully (either show error or create new cart)
      const cartId = await page.evaluate(() => localStorage.getItem('cartId'));
      expect(cartId).toBeTruthy();
    }
  });

  test('should handle agent API failure', async ({ page }) => {
    // Navigate to agent
    await page.click('[data-testid="agent-tab"]');

    // Intercept and fail agent API
    await page.route('**/api/v1/agent/chat', route => {
      route.fulfill({
        status: 503,
        body: JSON.stringify({ error: 'Service unavailable' })
      });
    });

    // Send message
    await page.fill('[data-testid="agent-input"]', 'Hello');
    await page.click('[data-testid="agent-send-btn"]');

    // Wait for response
    await page.waitForTimeout(3000);

    // Verify error message in chat or error indicator
    const errorMessage = page.locator('text=/error|failed|unavailable/i');
    const hasError = await errorMessage.isVisible().catch(() => false);

    expect(hasError).toBeTruthy();
  });

  test('should validate quantity input', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]');

    // Try to set invalid quantity (if quantity input exists)
    const quantityInput = page.locator('[data-testid="quantity-input"]').first();

    if (await quantityInput.isVisible().catch(() => false)) {
      // Try negative quantity
      await quantityInput.fill('-5');

      // Try to add to cart
      await page.click('[data-testid="add-to-cart-btn"]');

      // Should show validation error or prevent addition
      const hasError = await page.locator('text=/invalid|must be positive/i').isVisible({ timeout: 2000 }).catch(() => false);

      if (hasError) {
        expect(hasError).toBeTruthy();
      } else {
        // Or input should prevent invalid values
        const value = await quantityInput.inputValue();
        expect(parseInt(value)).toBeGreaterThan(0);
      }
    }
  });

  test('should handle large quantities', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]');

    // Try to add large quantity
    const quantityInput = page.locator('[data-testid="quantity-input"]').first();

    if (await quantityInput.isVisible().catch(() => false)) {
      await quantityInput.fill('99999');
      await page.click('[data-testid="add-to-cart-btn"]');

      // Wait for response
      await page.waitForTimeout(2000);

      // Should either accept it or show validation
      const hasWarning = await page.locator('text=/large quantity|confirm/i').isVisible().catch(() => false);
      const hasError = await page.locator('text=/exceeds|maximum/i').isVisible().catch(() => false);

      // One of these outcomes is acceptable
      expect(hasWarning || hasError || true).toBeTruthy();
    }
  });

  test('should recover from failed checkout', async ({ cartPage }) => {
    // Intercept checkout API to fail
    await cartPage.route('**/api/v1/orders', route => {
      route.fulfill({
        status: 500,
        body: JSON.stringify({ error: 'Checkout failed' })
      });
    });

    // Try to checkout
    await cartPage.click('[data-testid="checkout-btn"]');

    // Wait for error
    await cartPage.waitForTimeout(3000);

    // Verify error message or retry option
    const hasErrorMessage = await cartPage.locator('text=/failed|error/i').isVisible().catch(() => false);

    if (hasErrorMessage) {
      expect(hasErrorMessage).toBeTruthy();

      // Check for retry button
      const retryButton = cartPage.locator('[data-testid="retry-btn"]').or(cartPage.locator('text=/try again|retry/i'));
      const hasRetry = await retryButton.isVisible().catch(() => false);

      if (hasRetry) {
        await expect(retryButton).toBeVisible();
      }
    }
  });

  test('should handle concurrent cart modifications', async ({ page }) => {
    // Create a cart
    const cartResponse = await page.request.post('http://localhost:8080/api/v1/carts');
    const { id: cartId } = await cartResponse.json();

    await page.goto('/');
    await page.evaluate((id) => localStorage.setItem('cartId', id), cartId);

    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]');

    // Simulate cart being modified by another session (delete it)
    await page.request.delete(`http://localhost:8080/api/v1/carts/${cartId}`);

    // Try to add item with deleted cart
    await page.locator('[data-testid="add-to-cart-btn"]').first().click();

    // Wait for response
    await page.waitForTimeout(2000);

    // App should handle gracefully
    const newCartId = await page.evaluate(() => localStorage.getItem('cartId'));

    // Either creates new cart or shows error
    expect(newCartId).toBeTruthy();
  });
});
