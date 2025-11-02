/**
 * Test data fixtures for E2E tests
 */

export const testItems = {
  usbCable: {
    name: 'Premium USB-C Cable',
    category: 'Electronics',
    description: '6ft USB-C to USB-C cable, high-speed data transfer',
    price: 15.99,
    status: 'ACTIVE'
  },
  laptop: {
    name: 'Business Laptop',
    category: 'Electronics',
    description: 'Intel i7, 16GB RAM, 512GB SSD',
    price: 1299.99,
    status: 'ACTIVE'
  },
  chair: {
    name: 'Ergonomic Office Chair',
    category: 'Furniture',
    description: 'Adjustable lumbar support, breathable mesh',
    price: 449.99,
    status: 'ACTIVE'
  },
  monitor: {
    name: '27-inch 4K Monitor',
    category: 'Electronics',
    description: '4K Ultra HD, IPS panel, USB-C',
    price: 599.99,
    status: 'ACTIVE'
  },
  keyboard: {
    name: 'Mechanical Keyboard',
    category: 'Electronics',
    description: 'Cherry MX switches, RGB backlight',
    price: 129.99,
    status: 'ACTIVE'
  }
};

export const testUsers = {
  buyer: {
    id: 'test-buyer-1',
    name: 'Test Buyer',
    email: 'buyer@test.com'
  },
  admin: {
    id: 'test-admin-1',
    name: 'Test Admin',
    email: 'admin@test.com'
  }
};

/**
 * Helper to seed database with test data via API
 */
export async function seedTestData(baseUrl: string = 'http://localhost:8080'): Promise<any> {
  try {
    // Register test items
    const items = Object.values(testItems);
    const registeredItems = [];

    for (const item of items) {
      const response = await fetch(`${baseUrl}/api/v1/items`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(item)
      });

      if (response.ok) {
        const registeredItem = await response.json();
        registeredItems.push(registeredItem);
      }
    }

    return { items: registeredItems };
  } catch (error) {
    console.error('Failed to seed test data:', error);
    throw error;
  }
}

/**
 * Helper to clean up test data
 */
export async function cleanupTestData(baseUrl: string = 'http://localhost:8080'): Promise<void> {
  try {
    // Note: In a real implementation, you'd want a dedicated cleanup endpoint
    // For now, we'll rely on database reset between test runs
    console.log('Cleanup test data (not implemented yet)');
  } catch (error) {
    console.error('Failed to cleanup test data:', error);
  }
}

/**
 * Helper to create a test cart
 */
export async function createTestCart(baseUrl: string = 'http://localhost:8080'): Promise<string> {
  const response = await fetch(`${baseUrl}/api/v1/carts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  });

  if (!response.ok) {
    throw new Error('Failed to create test cart');
  }

  const { id } = await response.json();
  return id;
}

/**
 * Helper to add item to cart
 */
export async function addItemToCart(
  cartId: string,
  itemId: string,
  quantity: number = 1,
  baseUrl: string = 'http://localhost:8080'
): Promise<void> {
  const response = await fetch(`${baseUrl}/api/v1/carts/${cartId}/items`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ itemId, quantity })
  });

  if (!response.ok) {
    throw new Error('Failed to add item to cart');
  }
}
