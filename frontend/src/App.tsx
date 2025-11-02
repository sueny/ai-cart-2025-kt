import { useState, useEffect } from 'react';
import { Bot, Package, ShoppingCart as CartIcon, CheckCircle } from 'lucide-react';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AgentChat } from './components/AgentChat';
import { Catalog } from './components/Catalog';
import { Cart } from './components/Cart';
import { cartApi } from './lib/api';
import type { Order } from './lib/types';

type View = 'agent' | 'catalog' | 'cart';

function App() {
  const [currentView, setCurrentView] = useState<View>('agent');
  const [cartId, setCartId] = useState<string | null>(null);
  const [cartRefreshTrigger, setCartRefreshTrigger] = useState(0);
  const [completedOrder, setCompletedOrder] = useState<Order | null>(null);
  const [cartItemCount, setCartItemCount] = useState(0);

  useEffect(() => {
    initializeCart();
  }, []);

  useEffect(() => {
    if (cartId) {
      fetchCartCount();
    }
  }, [cartId, cartRefreshTrigger]);

  const fetchCartCount = async () => {
    if (!cartId) return;
    try {
      const response = await cartApi.get(cartId);
      setCartItemCount(response.data.items?.length || 0);
    } catch (error) {
      console.error('Error fetching cart count:', error);
      setCartItemCount(0);
    }
  };

  const initializeCart = async () => {
    // Check if cart ID exists in localStorage
    const existingCartId = localStorage.getItem('cartId');
    console.log('Initializing cart, existing cartId:', existingCartId);

    if (existingCartId) {
      try {
        // Verify cart still exists
        const response = await cartApi.get(existingCartId);
        console.log('Verified existing cart:', response.data);
        setCartId(existingCartId);
      } catch (error) {
        console.error('Existing cart verification failed, creating new one:', error);
        // Cart doesn't exist, create new one
        createNewCart();
      }
    } else {
      console.log('No existing cart found, creating new one');
      createNewCart();
    }
  };

  const createNewCart = async () => {
    try {
      const response = await cartApi.create();
      const newCartId = response.data.id;
      console.log('New cart created:', newCartId);
      setCartId(newCartId);
      if (newCartId) {
        localStorage.setItem('cartId', newCartId);
      }
    } catch (error) {
      console.error('Error creating cart:', error);
    }
  };

  const handleCartUpdate = () => {
    setCartRefreshTrigger(prev => prev + 1);
  };

  const handleOrderComplete = (order: Order) => {
    setCompletedOrder(order);
    // Create new cart after successful order
    createNewCart();
  };

  const closeOrderModal = () => {
    setCompletedOrder(null);
    setCurrentView('catalog');
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>, view: View) => {
    if (e.key === 'Enter') {
      setCurrentView(view);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-blue-800 rounded-lg flex items-center justify-center">
                <Package className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">ProcureFlow</h1>
                <p className="text-xs text-gray-600">AI-Native Procurement</p>
              </div>
            </div>

            {cartId && (
              <div className="text-sm text-gray-600">
                Cart ID: <span className="font-mono text-xs">{cartId.slice(0, 8)}...</span>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex gap-1" role="tablist">
            <button
              data-testid="agent-tab"
              role="tab"
              aria-selected={currentView === 'agent'}
              onClick={() => setCurrentView('agent')}
              onKeyDown={(e) => handleKeyDown(e, 'agent')}
              className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors ${
                currentView === 'agent'
                  ? 'text-blue-800 border-b-2 border-blue-800'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Bot className="w-5 h-5" />
              AI Assistant
            </button>
            <button
              data-testid="catalog-tab"
              role="tab"
              aria-selected={currentView === 'catalog'}
              onClick={() => setCurrentView('catalog')}
              onKeyDown={(e) => handleKeyDown(e, 'catalog')}
              className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors ${
                currentView === 'catalog'
                  ? 'text-blue-800 border-b-2 border-blue-800'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Package className="w-5 h-5" />
              Catalog
            </button>
            <button
              data-testid="cart-tab"
              role="tab"
              aria-selected={currentView === 'cart'}
              onClick={() => setCurrentView('cart')}
              onKeyDown={(e) => handleKeyDown(e, 'cart')}
              className={`flex items-center gap-2 px-6 py-3 font-medium transition-colors relative ${
                currentView === 'cart'
                  ? 'text-blue-800 border-b-2 border-blue-800'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <CartIcon className="w-5 h-5" />
              Cart
              {cartItemCount > 0 && (
                <span
                  data-testid="cart-badge"
                  className="absolute -top-1 -right-1 bg-blue-800 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center"
                >
                  {cartItemCount}
                </span>
              )}
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {currentView === 'agent' && (
          <div className="h-[calc(100vh-280px)]">
            <AgentChat cartId={cartId} onCartUpdate={handleCartUpdate} />
          </div>
        )}

        {currentView === 'catalog' && (
          <Catalog cartId={cartId} onCartUpdate={handleCartUpdate} />
        )}

        {currentView === 'cart' && (
          <Cart
            cartId={cartId}
            refreshTrigger={cartRefreshTrigger}
            onOrderComplete={handleOrderComplete}
          />
        )}
      </main>

      {/* Order Confirmation Modal */}
      {completedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Order Confirmed!</h2>
              <p className="text-gray-600 mb-6">
                Your order has been successfully placed.
              </p>

              <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left">
                <div className="flex justify-between mb-2">
                  <span className="text-gray-600">Order ID:</span>
                  <span className="font-mono text-sm">{completedOrder.id.slice(0, 13)}...</span>
                </div>
                <div className="flex justify-between mb-2">
                  <span className="text-gray-600">Items:</span>
                  <span className="font-medium">{completedOrder.items.length}</span>
                </div>
                <div className="flex justify-between text-lg font-bold">
                  <span>Total:</span>
                  <span className="text-blue-800">${completedOrder.total.toFixed(2)}</span>
                </div>
              </div>

              <button
                onClick={closeOrderModal}
                className="w-full px-6 py-3 bg-blue-800 text-white rounded-lg hover:bg-blue-700 font-semibold"
              >
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Toast Notifications */}
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
      />
    </div>
  );
}

export default App;