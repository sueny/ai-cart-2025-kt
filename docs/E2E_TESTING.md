# End-to-End Testing Guide

## Overview

This project uses **Playwright** for end-to-end testing of the full-stack ProcureFlow application. E2E tests validate complete user workflows from frontend through backend to database.

## Test Structure

```
frontend/
├── e2e/
│   ├── fixtures/
│   │   ├── test-setup.ts       # Custom test fixtures and helpers
│   │   └── test-data.ts        # Test data and seed functions
│   ├── traditional-flow.spec.ts # Traditional UI flow tests
│   ├── agent-flow.spec.ts       # AI agent interaction tests
│   ├── error-handling.spec.ts   # Error scenarios and edge cases
│   └── accessibility.spec.ts    # Accessibility compliance tests
├── playwright.config.ts         # Playwright configuration
└── package.json                # Test scripts
```

## Prerequisites

### 1. Install Dependencies

```bash
cd frontend
npm install
```

This will install:
- `@playwright/test` - Playwright test runner
- `@axe-core/playwright` - Accessibility testing
- `dotenv` - Environment variables

### 2. Install Playwright Browsers

```bash
npx playwright install
```

### 3. Start Backend Server

The backend must be running on `http://localhost:8080`:

```bash
cd backend
./gradlew bootRun
```

Or use Docker:

```bash
docker-compose up backend postgres
```

## Running Tests

### Run All Tests

```bash
cd frontend
npm run test:e2e
```

### Run Tests in UI Mode (Recommended for Development)

```bash
npm run test:e2e:ui
```

This opens the Playwright UI where you can:
- Select which tests to run
- Watch tests execute in real-time
- Debug failures
- View traces and screenshots

### Run Tests in Headed Mode (See Browser)

```bash
npm run test:e2e:headed
```

### Run Specific Test File

```bash
npx playwright test traditional-flow.spec.ts
```

### Run Tests in Debug Mode

```bash
npm run test:e2e:debug
```

This opens the Playwright Inspector for step-by-step debugging.

### Run Tests in Specific Browser

```bash
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit
```

## Test Suites

### 1. Traditional Flow Tests (`traditional-flow.spec.ts`)

Tests standard UI interactions without AI agent:

- **Homepage Loading** - Verify main navigation and layout
- **Catalog Browsing** - View and search items
- **Item Search** - Search functionality
- **Add to Cart** - Add items from catalog
- **Cart Management** - View, update, and remove items
- **Checkout** - Complete order process
- **Empty Search** - Handle no results

**Example:**
```bash
npx playwright test traditional-flow.spec.ts
```

### 2. Agent Flow Tests (`agent-flow.spec.ts`)

Tests AI agent-driven interactions:

- **Agent Interface** - Load and interact with agent
- **Natural Language Search** - Find items via conversational queries
- **Typing Indicators** - Show agent thinking
- **Add to Cart via Agent** - Purchase through conversation
- **Conversation History** - Maintain context
- **Agent Checkout** - Complete orders via agent
- **Error Handling** - Graceful failure recovery

**Example:**
```bash
npx playwright test agent-flow.spec.ts
```

### 3. Error Handling Tests (`error-handling.spec.ts`)

Tests error scenarios and edge cases:

- **API Errors** - Handle 500/503 responses
- **Network Timeouts** - Handle slow/failed requests
- **Empty Cart Checkout** - Prevent invalid operations
- **Invalid Cart ID** - Handle corrupted state
- **Agent API Failures** - Graceful degradation
- **Invalid Quantities** - Input validation
- **Large Quantities** - Handle edge cases
- **Failed Checkout** - Recovery from failures
- **Concurrent Modifications** - Handle race conditions

**Example:**
```bash
npx playwright test error-handling.spec.ts
```

### 4. Accessibility Tests (`accessibility.spec.ts`)

Tests WCAG 2.1 AA compliance:

- **No Violations** - Pages pass axe accessibility scan
- **Keyboard Navigation** - Full keyboard support
- **Tab Activation** - Enter key activates elements
- **Heading Hierarchy** - Proper h1-h6 structure
- **Alt Text** - All images have alt attributes
- **Form Labels** - Inputs have proper labels
- **Color Contrast** - Sufficient contrast ratios
- **Accessible Buttons** - Buttons have accessible names
- **Screen Reader Support** - ARIA live regions
- **Focus Indicators** - Visible focus states
- **ARIA Roles** - Proper semantic landmarks
- **Skip Links** - Navigation shortcuts

**Example:**
```bash
npx playwright test accessibility.spec.ts
```

## Test Fixtures

### Custom Fixtures

Located in `e2e/fixtures/test-setup.ts`:

- **`authenticatedPage`** - Page with pre-created cart
- **`cartPage`** - Page on cart view with items

**Usage:**
```typescript
test('my test', async ({ authenticatedPage }) => {
  // Page already has a cart set up
  await authenticatedPage.click('[data-testid="catalog-tab"]');
});
```

### Test Data

Located in `e2e/fixtures/test-data.ts`:

- **`testItems`** - Sample items for testing
- **`seedTestData()`** - Populate database with test data
- **`createTestCart()`** - Create a cart via API
- **`addItemToCart()`** - Add item to cart via API

**Usage:**
```typescript
import { seedTestData, testItems } from './fixtures/test-data';

test.beforeEach(async () => {
  await seedTestData();
});
```

## Viewing Test Results

### HTML Report

After test run:

```bash
npm run test:e2e:report
```

This opens an HTML report with:
- Test results summary
- Failure details
- Screenshots on failure
- Video recordings on failure
- Execution traces

### CI/CD Reports

Tests generate multiple report formats:
- **HTML**: `playwright-report/index.html`
- **JSON**: `e2e-results.json`
- **JUnit**: (configurable in playwright.config.ts)

## Writing New Tests

### Basic Test Structure

```typescript
import { test, expect } from './fixtures/test-setup';

test.describe('Feature Name', () => {

  test.beforeEach(async ({ page }) => {
    // Setup before each test
    await page.goto('/');
  });

  test('should do something', async ({ page }) => {
    // Arrange
    await page.click('[data-testid="some-button"]');

    // Act
    await page.fill('[data-testid="input"]', 'value');

    // Assert
    await expect(page.locator('[data-testid="result"]')).toBeVisible();
  });
});
```

### Best Practices

1. **Use data-testid attributes** for stable selectors
   ```html
   <button data-testid="add-to-cart-btn">Add to Cart</button>
   ```

2. **Wait for elements** before interacting
   ```typescript
   await page.waitForSelector('[data-testid="item-card"]');
   ```

3. **Use fixtures** for common setup
   ```typescript
   test('my test', async ({ authenticatedPage }) => {
     // Already set up
   });
   ```

4. **Seed data** before tests
   ```typescript
   test.beforeEach(async () => {
     await seedTestData();
   });
   ```

5. **Clean assertions** with proper timeout
   ```typescript
   await expect(element).toBeVisible({ timeout: 5000 });
   ```

## Debugging Tests

### 1. Playwright UI Mode (Best for Development)

```bash
npm run test:e2e:ui
```

Features:
- Pick and run specific tests
- Time travel through test execution
- View screenshots and DOM at each step
- Edit and re-run tests

### 2. Debug Mode with Inspector

```bash
npm run test:e2e:debug
```

Features:
- Step through test line by line
- Pause on errors
- Inspect page state
- Run Playwright commands in console

### 3. Headed Mode

```bash
npm run test:e2e:headed
```

Watch tests run in a visible browser window.

### 4. Screenshots and Videos

Automatically captured on failure:
- Screenshots: `test-results/` directory
- Videos: `test-results/` directory
- Traces: `test-results/` directory

### 5. Console Logs

View console logs from the browser:
```typescript
page.on('console', msg => console.log(msg.text()));
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        run: |
          cd frontend
          npm ci

      - name: Install Playwright Browsers
        run: npx playwright install --with-deps

      - name: Start Backend
        run: |
          cd backend
          ./gradlew bootRun &
          sleep 30

      - name: Run E2E Tests
        run: |
          cd frontend
          npm run test:e2e

      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: frontend/playwright-report/
```

## Configuration

### Playwright Config (`playwright.config.ts`)

Key settings:
- **testDir**: `./e2e` - Test directory
- **timeout**: 60000 (60s) - Test timeout
- **retries**: 2 on CI, 0 locally
- **reporters**: HTML, JSON, List
- **browsers**: Chromium, Firefox, WebKit (configurable)
- **webServer**: Auto-start dev server

### Environment Variables

Create `.env` in frontend directory:

```env
BASE_URL=http://localhost:5173
API_URL=http://localhost:8080
```

## Test Coverage

Current test coverage includes:

- ✅ **Traditional Flows**: 10 tests
- ✅ **Agent Interactions**: 10 tests
- ✅ **Error Scenarios**: 10 tests
- ✅ **Accessibility**: 15 tests

**Total**: 45+ end-to-end tests

## Common Issues

### Backend Not Running

**Error**: `net::ERR_CONNECTION_REFUSED`

**Solution**: Start the backend server:
```bash
cd backend
./gradlew bootRun
```

### Playwright Browsers Not Installed

**Error**: `Executable doesn't exist`

**Solution**: Install browsers:
```bash
npx playwright install
```

### Port Already in Use

**Error**: `EADDRINUSE: address already in use`

**Solution**: Kill existing process or change port in config

### Test Timeouts

**Error**: `Test timeout of 60000ms exceeded`

**Solution**: Increase timeout in test or config:
```typescript
test('slow test', async ({ page }) => {
  test.setTimeout(120000); // 2 minutes
});
```

### Flaky Tests

**Solution**:
1. Add explicit waits: `await page.waitForSelector()`
2. Use `waitForLoadState()`: `await page.waitForLoadState('networkidle')`
3. Increase retry count in CI
4. Use fixtures for consistent setup

## Resources

- [Playwright Documentation](https://playwright.dev)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [axe Accessibility Testing](https://github.com/dequelabs/axe-core)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

## Contributing

When adding new features:

1. Add corresponding E2E tests
2. Use `data-testid` attributes for test selectors
3. Follow existing test patterns
4. Run tests locally before committing
5. Ensure accessibility tests pass

## Support

For issues or questions:
- Check Playwright docs: https://playwright.dev
- Review test examples in `e2e/` directory
- Check `playwright.config.ts` for configuration options
