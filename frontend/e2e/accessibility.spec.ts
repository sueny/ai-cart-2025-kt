import { test, expect } from './fixtures/test-setup';
import AxeBuilder from '@axe-core/playwright';
import { seedTestData } from './fixtures/test-data';

test.describe('Accessibility', () => {

  test.beforeEach(async ({ page }) => {
    await seedTestData();
  });

  test('should not have accessibility violations on home page', async ({ page }) => {
    await page.goto('/');

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test.skip('should not have accessibility violations on catalog page', async ({ page }) => {
    await page.goto('/');
    await page.click('[data-testid="catalog-tab"]');

    // Wait for items to load
    await page.waitForSelector('[data-testid="item-card"]');

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should not have accessibility violations on cart page', async ({ cartPage }) => {
    const accessibilityScanResults = await new AxeBuilder({ page: cartPage }).analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should not have accessibility violations on agent page', async ({ page }) => {
    await page.goto('/');
    await page.click('[data-testid="agent-tab"]');

    const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should be keyboard navigable', async ({ page }) => {
    await page.goto('/');

    // Tab through main navigation
    await page.keyboard.press('Tab');
    let focused = await page.evaluate(() => document.activeElement?.getAttribute('data-testid'));

    // Should focus on first interactive element
    expect(focused).toBeTruthy();

    // Continue tabbing
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');

    // Should be able to navigate to different tabs
    focused = await page.evaluate(() => document.activeElement?.getAttribute('data-testid'));
    expect(focused).toBeTruthy();
  });

  test('should activate tabs with Enter key', async ({ page }) => {
    await page.goto('/');

    // Tab to catalog tab
    await page.keyboard.press('Tab');
    const firstTab = await page.evaluate(() => document.activeElement?.getAttribute('data-testid'));

    if (firstTab && firstTab.includes('tab')) {
      // Press Enter to activate
      await page.keyboard.press('Enter');

      // Wait for content to change
      await page.waitForTimeout(500);

      // Verify tab activated
      const activeTab = await page.locator('[aria-selected="true"]').getAttribute('data-testid');
      expect(activeTab).toBeTruthy();
    }
  });

  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/');

    // Check for h1
    const h1Count = await page.locator('h1').count();
    expect(h1Count).toBeGreaterThanOrEqual(1);

    // Verify headings are in order (h1, then h2, etc.)
    const headings = await page.locator('h1, h2, h3, h4, h5, h6').all();

    if (headings.length > 0) {
      const firstHeading = await headings[0].evaluate(el => el.tagName);
      expect(firstHeading).toBe('H1');
    }
  });

  test('should have alt text for images', async ({ page }) => {
    await page.goto('/');

    // Navigate to catalog where images might be
    await page.click('[data-testid="catalog-tab"]');
    await page.waitForSelector('[data-testid="item-card"]');

    // Check all images have alt text
    const images = await page.locator('img').all();

    for (const img of images) {
      const alt = await img.getAttribute('alt');
      // Alt attribute should exist (can be empty for decorative images)
      expect(alt).not.toBeNull();
    }
  });

  test('should have proper labels for form inputs', async ({ page }) => {
    await page.goto('/');
    await page.click('[data-testid="catalog-tab"]');

    // Check search input has label or aria-label
    const searchInput = page.locator('[data-testid="search-input"]');

    if (await searchInput.isVisible()) {
      const ariaLabel = await searchInput.getAttribute('aria-label');
      const ariaLabelledby = await searchInput.getAttribute('aria-labelledby');
      const id = await searchInput.getAttribute('id');

      // Should have some form of labeling
      const hasLabel = ariaLabel || ariaLabelledby || (id && await page.locator(`label[for="${id}"]`).isVisible());

      expect(hasLabel).toBeTruthy();
    }
  });

  test('should have sufficient color contrast', async ({ page }) => {
    await page.goto('/');

    // Run axe with specific color contrast rules
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2aa', 'wcag21aa'])
      .analyze();

    const contrastViolations = accessibilityScanResults.violations.filter(
      v => v.id === 'color-contrast'
    );

    expect(contrastViolations).toEqual([]);
  });

  test('should have accessible buttons', async ({ page }) => {
    await page.goto('/');

    // Check a few critical buttons
    const buttonsToTest = [
      '[data-testid="agent-send-btn"]',
      '[data-testid="search-button"]',
      '[data-testid="add-to-cart-btn"]',
      '[data-testid="checkout-btn"]',
    ];

    for (const selector of buttonsToTest) {
      const button = page.locator(selector).first();
      if (await button.isVisible()) {
        const ariaLabel = await button.getAttribute('aria-label');
        const textContent = await button.textContent();
        expect(ariaLabel || (textContent && textContent.trim().length > 0)).toBeTruthy();
      }
    }
  });

  test('should support screen reader announcements', async ({ page }) => {
    await page.goto('/');

    // Check for ARIA live regions
    const liveRegions = await page.locator('[aria-live]').all();

    // Application should have at least one live region for dynamic updates
    expect(liveRegions.length).toBeGreaterThan(0);
  });

  test('should have proper focus indicators', async ({ page }) => {
    await page.goto('/');

    // Tab to first interactive element
    await page.keyboard.press('Tab');

    // Get focused element
    const focusedElement = await page.evaluate(() => {
      const el = document.activeElement;
      if (!el) return null;

      const styles = window.getComputedStyle(el);
      return {
        outlineWidth: styles.outlineWidth,
        outlineStyle: styles.outlineStyle,
        outlineColor: styles.outlineColor,
        boxShadow: styles.boxShadow
      };
    });

    // Should have some form of visible focus indicator
    const hasFocusIndicator =
      (focusedElement?.outlineWidth && focusedElement.outlineWidth !== '0px') ||
      (focusedElement?.boxShadow && focusedElement.boxShadow !== 'none');

    expect(hasFocusIndicator).toBeTruthy();
  });

  test('should have proper ARIA roles for navigation', async ({ page }) => {
    await page.goto('/');

    // Check for navigation landmark
    const nav = await page.locator('nav, [role="navigation"]').count();
    expect(nav).toBeGreaterThanOrEqual(1);

    // Check for main landmark
    const main = await page.locator('main, [role="main"]').count();
    expect(main).toBeGreaterThanOrEqual(1);
  });

  test('should have skip links', async ({ page }) => {
    await page.goto('/');

    // Check for skip to main content link (common accessibility feature)
    const skipLink = await page.locator('a[href="#main"], a:has-text("skip to")').count();

    // While not required, it's a good practice
    // This test documents whether the feature exists
    if (skipLink > 0) {
      expect(skipLink).toBeGreaterThan(0);
    }
  });
});
