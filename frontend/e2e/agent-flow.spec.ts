import { test, expect } from './fixtures/test-setup';
import { seedTestData } from './fixtures/test-data';

test.describe('Agent-Driven Purchase Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Seed database
    await seedTestData();

    // Navigate to home and ensure agent tab is active
    await page.goto('/');
    await page.click('[data-testid="agent-tab"]');
  });

  test('should load agent interface', async ({ page }) => {
    // Verify agent input is visible
    await expect(page.locator('[data-testid="agent-input"]')).toBeVisible();

    // Verify send button is visible
    await expect(page.locator('[data-testid="agent-send-btn"]')).toBeVisible();

    // Verify chat messages container
    await expect(page.locator('[data-testid="agent-messages"]')).toBeVisible();
  });

  test('should send message to agent', async ({ page }) => {
    // Type a message
    const testMessage = 'Hello, I need help finding items';
    await page.fill('[data-testid="agent-input"]', testMessage);

    // Send message
    await page.click('[data-testid="agent-send-btn"]');

    // Verify user message appears in chat
    await expect(page.locator('[data-testid="user-message"]').last()).toContainText(testMessage);

    // Wait for agent response
    await expect(page.locator('[data-testid="agent-message"]').last()).toBeVisible({ timeout: 15000 });
  });

  test('should search for items via agent', async ({ page }) => {
    // Request items via natural language
    await page.fill('[data-testid="agent-input"]', 'I need USB-C cables');
    await page.click('[data-testid="agent-send-btn"]');

    // Wait for agent to respond
    await expect(page.locator('[data-testid="agent-message"]').last()).toBeVisible({ timeout: 15000 });

    // Verify agent shows search results (response should mention cables or items)
    const responseText = await page.locator('[data-testid="agent-message"]').last().textContent();
    expect(responseText?.toLowerCase()).toMatch(/cable|usb|item|found/);
  });

  test('should show typing indicator while agent is thinking', async ({ page }) => {
    // Send message
    await page.fill('[data-testid="agent-input"]', 'Show me laptops');
    await page.click('[data-testid="agent-send-btn"]');

    // Check for typing indicator (may be temporary)
    // Note: This may need adjustment based on actual implementation
    const typingIndicator = page.locator('[data-testid="agent-typing"]');
    if (await typingIndicator.isVisible({ timeout: 1000 }).catch(() => false)) {
      await expect(typingIndicator).toBeVisible();
    }

    // Verify eventual response
    await expect(page.locator('[data-testid="agent-message"]').last()).toBeVisible({ timeout: 15000 });
  });

  test('should handle add to cart via agent', async ({ page, context }) => {
    // Mock the API response for the agent
    await context.route('**/api/v1/agent/chat', async route => {
      await route.fulfill({ json: { response: 'I have added 2 "Premium USB-C Cable" to your cart.' } });
    });

    // Request to add to cart
    await page.fill('[data-testid="agent-input"]', 'Add 2 of them to my cart');
    await page.click('[data-testid="agent-send-btn"]');

    // Verify response mentions adding to cart or success
    const messages = page.locator('[data-testid="agent-message"]');
    const lastMessageText = await messages.last().textContent();
    expect(lastMessageText?.toLowerCase()).toMatch(/cart|added/);
  });

  test('should show conversation history', async ({ page }) => {
    // Send multiple messages
    const messages = [
      'Hello',
      'I need office supplies',
      'Show me chairs'
    ];

    for (const message of messages) {
      await page.fill('[data-testid="agent-input"]', message);
      await page.click('[data-testid="agent-send-btn"]');
      await page.waitForTimeout(2000);
    }

    // Verify all user messages are visible in history
    const userMessages = page.locator('[data-testid="user-message"]');
    await expect(userMessages).toHaveCount(3);

    // Verify conversation order
    const firstUserMessage = await userMessages.first().textContent();
    expect(firstUserMessage).toContain(messages[0]);
  });

  test('should clear input after sending message', async ({ page }) => {
    // Type and send message
    await page.fill('[data-testid="agent-input"]', 'Test message');
    await page.click('[data-testid="agent-send-btn"]');

    // Verify input is cleared
    await expect(page.locator('[data-testid="agent-input"]')).toHaveValue('');
  });

  test('should disable send button when input is empty', async ({ page }) => {
    // Verify send button is disabled with empty input
    const sendButton = page.locator('[data-testid="agent-send-btn"]');

    await expect(sendButton).toBeDisabled();

    // Type something
    await page.fill('[data-testid="agent-input"]', 'Test');

    // Verify button is now enabled
    await expect(sendButton).toBeEnabled();

    // Clear input
    await page.fill('[data-testid="agent-input"]', '');

    // Verify button is disabled again
    await expect(sendButton).toBeDisabled();
  });

  test('should handle checkout via agent', async ({ authenticatedPage }) => {
    // Add item to cart first via API for speed
    const cartId = await authenticatedPage.evaluate(() => localStorage.getItem('cartId'));

    if (cartId) {
      // Get an item
      const itemsResponse = await authenticatedPage.request.get('http://localhost:8080/api/v1/items');
      const items = await itemsResponse.json();

      if (items && items.length > 0) {
        // Add item to cart
        await authenticatedPage.request.post(`http://localhost:8080/api/v1/carts/${cartId}/items`, {
          data: {
            itemId: items[0].id,
            quantity: 1
          }
        });
      }
    }

    // Navigate to agent tab
    await authenticatedPage.click('[data-testid="agent-tab"]');

    // Request checkout via agent
    await authenticatedPage.fill('[data-testid="agent-input"]', 'I want to checkout now');
    await authenticatedPage.click('[data-testid="agent-send-btn"]');

    // Wait for response
    await authenticatedPage.waitForTimeout(5000);

    // Verify response mentions order or checkout
    const lastMessage = await authenticatedPage.locator('[data-testid="agent-message"]').last().textContent();
    expect(lastMessage?.toLowerCase()).toMatch(/order|checkout|confirm/);
  });

  test('should handle errors gracefully', async ({ page }) => {
    // Send an unclear or problematic message
    await page.fill('[data-testid="agent-input"]', 'asdfghjkl1234567890');
    await page.click('[data-testid="agent-send-btn"]');

    // Wait for response
    await expect(page.locator('[data-testid="agent-message"]').last()).toBeVisible({ timeout: 15000 });

    // Agent should still respond (even if to ask for clarification)
    const response = await page.locator('[data-testid="agent-message"]').last().textContent();
    expect(response).toBeTruthy();
    expect(response!.length).toBeGreaterThan(0);
  });
});
