import { useState, useEffect } from 'react';
import { Search, ShoppingCart, Package } from 'lucide-react';
import { toast } from 'react-toastify';
import { itemsApi, cartApi } from '../lib/api';
import type { Item } from '../lib/types';

interface CatalogProps {
  cartId: string | null;
  onCartUpdate: () => void;
}

export function Catalog({ cartId, onCartUpdate }: CatalogProps) {
  const [items, setItems] = useState<Item[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  useEffect(() => {
    loadItems();
  }, []);

  const loadItems = async () => {
    setIsLoading(true);
    try {
      const response = await itemsApi.getAll();
      setItems(response.data);
      setError(null);
    } catch (error) {
      console.error('Error loading items:', error);
      setError('Failed to load items. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadItems();
      return;
    }

    setIsLoading(true);
    try {
      const response = await itemsApi.search(searchQuery, selectedCategory || undefined);
      setItems(response.data);
      setError(null);
    } catch (error) {
      console.error('Error searching items:', error);
      setError('Failed to search for items. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddToCart = async (itemId: string) => {
    if (!cartId) {
      console.error('Cart ID is not initialized:', cartId);
      toast.error('Cart not initialized. Please refresh the page.');
      return;
    }

    try {
      console.log('Adding item to cart:', { cartId, itemId });
      await cartApi.addItem(cartId, itemId, 1);
      onCartUpdate();
      toast.success('Item added to cart!', {
        icon: '🛒',
      });
    } catch (error) {
      console.error('Error adding to cart:', error);
      toast.error('Failed to add item to cart');
    }
  };

  const categories = Array.from(new Set(items.map(item => item.category)));

  return (
    <div className="space-y-6">
      {/* Search Bar */}
      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                data-testid="search-input"
                type="text"
                placeholder="Search for items..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                aria-label="Search for items"
                className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          <select
            aria-label="Category filter"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Categories</option>
            {categories.map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
          <button
            data-testid="search-button"
            onClick={handleSearch}
            className="px-6 py-2 bg-blue-800 text-white rounded-lg hover:bg-blue-700"
          >
            Search
          </button>
        </div>
      </div>

      {/* Items Grid */}
      {isLoading ? (
        <div className="text-center py-12">
          <div className="inline-block w-8 h-8 border-4 border-blue-800 border-t-transparent rounded-full animate-spin" />
          <p className="mt-2 text-gray-600">Loading items...</p>
        </div>
      ) : error ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <p className="text-red-500">{error}</p>
        </div>
      ) : items.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <Package className="w-16 h-16 mx-auto text-gray-400 mb-4" />
          <p className="text-gray-600">No items found</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {items.map((item) => (
            <div key={item.id} data-testid="item-card" className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow">
              <div className="p-6">
                <div className="flex items-start justify-between mb-3">
                  <h3 data-testid="item-name" className="font-semibold text-lg text-gray-900 flex-1">{item.name}</h3>
                  <span data-testid="item-category" className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded">
                    {item.category}
                  </span>
                </div>
                <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                  {item.description || 'No description available'}
                </p>
                <div className="flex items-center justify-between">
                  <span data-testid="item-price" className="text-2xl font-bold text-blue-800">
                    ${item.price.toFixed(2)}
                  </span>
                  <button
                    data-testid="add-to-cart-btn"
                    onClick={() => handleAddToCart(item.id)}
                    disabled={!cartId}
                    className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                      cartId
                        ? 'bg-blue-800 text-white hover:bg-blue-700 cursor-pointer'
                        : 'bg-gray-400 text-gray-200 cursor-not-allowed'
                    }`}
                    title={!cartId ? 'Cart is initializing...' : 'Add to cart'}
                  >
                    <ShoppingCart className="w-4 h-4" />
                    Add
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}