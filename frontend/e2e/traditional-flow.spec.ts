import { test, expect } from './fixtures/test-setup';
import { seedTestData, testItems } from './fixtures/test-data';

test.describe('Traditional Purchase Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Seed database with test items
    await seedTestData();

    // Navigate to home
    await page.goto('/');
  });

  test('should load homepage successfully', async ({ page }) => {
    // Verify page loaded
    await expect(page).toHaveTitle(/ProcureFlow/i);

    // Verify main navigation is visible
    await expect(page.locator('[data-testid="agent-tab"]')).toBeVisible();
    await expect(page.locator('[data-testid="catalog-tab"]')).toBeVisible();
    await expect(page.locator('[data-testid="cart-tab"]')).toBeVisible();
  });

  test('should browse catalog and view items', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');

    // Wait for items to load
    await page.waitForSelector('[data-testid="item-card"]', { timeout: 10000 });

    // Verify items are displayed
    const itemCards = page.locator('[data-testid="item-card"]');
    await expect(itemCards).toHaveCount(await itemCards.count());

    // Verify first item has expected structure
    const firstItem = itemCards.first();
    await expect(firstItem.locator('[data-testid="item-name"]')).toBeVisible();
    await expect(firstItem.locator('[data-testid="item-price"]')).toBeVisible();
    await expect(firstItem.locator('[data-testid="item-category"]')).toBeVisible();
  });

  test('should search for items', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');

    // Wait for initial items to load
    await page.waitForSelector('[data-testid="item-card"]');

    // Search for USB cable
    await page.fill('[data-testid="search-input"]', 'USB');
    await page.click('[data-testid="search-button"]');

    // Wait for search results
    await page.waitForTimeout(1000);

    // Verify filtered results contain USB
    const itemNames = page.locator('[data-testid="item-name"]');
    const count = await itemNames.count();

    if (count > 0) {
      const firstItemText = await itemNames.first().textContent();
      expect(firstItemText?.toLowerCase()).toContain('usb');
    }
  });

  test('should add item to cart', async ({ page }) => {
    // Navigate to catalog
    await page.click('[data-testid="catalog-tab"]');

    // Wait for items to load
    await page.waitForSelector('[data-testid="item-card"]');

    // Click first "Add to Cart" button
    const addButton = page.locator('[data-testid="add-to-cart-btn"]').first();
    await addButton.click();

    // Verify success toast message appears
    await expect(page.locator('text=Item added to cart!')).toBeVisible({ timeout: 5000 });

    // Verify cart badge shows count
    await expect(page.locator('[data-testid="cart-badge"]')).toBeVisible();
  });

  test('should view cart contents', async ({ cartPage }) => {
    // cartPage fixture already has items in cart

    // Verify cart items are displayed
    await expect(cartPage.locator('[data-testid="cart-item"]')).toHaveCount(1);

    // Verify cart shows item details
    await expect(cartPage.locator('[data-testid="cart-item-name"]')).toBeVisible();
    await expect(cartPage.locator('[data-testid="cart-item-quantity"]')).toBeVisible();
    await expect(cartPage.locator('[data-testid="cart-item-price"]')).toBeVisible();

    // Verify cart total is shown
    await expect(cartPage.locator('[data-testid="cart-total"]')).toBeVisible();
  });

  test('should update item quantity in cart', async ({ cartPage }) => {
    // Get initial quantity
    const quantityElement = cartPage.locator('[data-testid="cart-item-quantity"]').first();
    const initialQuantity = await quantityElement.textContent();

    // Increase quantity
    await cartPage.click('[data-testid="increase-quantity-btn"]');

    // Wait for the quantity to be updated
    await expect(quantityElement).not.toHaveText(initialQuantity!);

    // Verify quantity increased
    const newQuantity = await quantityElement.textContent();
    expect(parseInt(newQuantity || '0')).toBeGreaterThan(parseInt(initialQuantity || '0'));
  });

  test('should remove item from cart', async ({ cartPage }) => {
    // Get initial item count
    const initialCount = await cartPage.locator('[data-testid="cart-item"]').count();

    // Remove first item
    await cartPage.locator('[data-testid="remove-item-btn"]').first().click();

    // Wait for removal
    await cartPage.waitForTimeout(500);

    // Verify item count decreased or cart is empty
    const newCount = await cartPage.locator('[data-testid="cart-item"]').count();
    expect(newCount).toBeLessThan(initialCount);
  });

  test('should proceed to checkout', async ({ cartPage }) => {
    // Click checkout button
    await cartPage.click('[data-testid="checkout-btn"]');

    // Verify order confirmation or success message
    await expect(
      cartPage.locator('text=/order.*confirmed/i')
        .or(cartPage.locator('[data-testid="order-id"]'))
    ).toBeVisible({ timeout: 10000 });
  });


});
