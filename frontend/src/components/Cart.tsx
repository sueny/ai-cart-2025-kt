import { useState, useEffect } from 'react';
import { ShoppingCart, Trash2, Plus, Minus, CreditCard } from 'lucide-react';
import { toast } from 'react-toastify';
import { cartApi, orderApi } from '../lib/api';
import type { Cart as CartType, Order } from '../lib/types';

interface CartProps {
  cartId: string | null;
  refreshTrigger: number;
  onOrderComplete: (order: Order) => void;
}

export function Cart({ cartId, refreshTrigger, onOrderComplete }: CartProps) {
  const [cart, setCart] = useState<CartType | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isCheckingOut, setIsCheckingOut] = useState(false);

  useEffect(() => {
    if (cartId) {
      loadCart();
    }
  }, [cartId, refreshTrigger]);

  const loadCart = async () => {
    if (!cartId) return;

    setIsLoading(true);
    try {
      const response = await cartApi.get(cartId);
      setCart(response.data);
    } catch (error) {
      console.error('Error loading cart:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateQuantity = async (itemId: string, newQuantity: number) => {
    if (!cartId) return;

    try {
      if (newQuantity <= 0) {
        await cartApi.removeItem(cartId, itemId);
      } else {
        await cartApi.updateItem(cartId, itemId, newQuantity);
      }
      loadCart();
    } catch (error) {
      console.error('Error updating quantity:', error);
    }
  };

  const handleRemoveItem = async (itemId: string) => {
    if (!cartId) return;

    try {
      await cartApi.removeItem(cartId, itemId);
      loadCart();
    } catch (error) {
      console.error('Error removing item:', error);
    }
  };

  const handleCheckout = async () => {
    if (!cartId || !cart || cart.items.length === 0) return;

    setIsCheckingOut(true);
    try {
      const response = await orderApi.create(cartId);
      onOrderComplete(response.data);
    } catch (error) {
      console.error('Error during checkout:', error);
      toast.error('Failed to complete checkout. Please try again.');
    } finally {
      setIsCheckingOut(false);
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-lg p-6 text-center">
        <div className="inline-block w-8 h-8 border-4 border-blue-800 border-t-transparent rounded-full animate-spin" />
        <p className="mt-2 text-gray-600">Loading cart...</p>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-lg p-8 text-center">
        <ShoppingCart className="w-16 h-16 mx-auto text-gray-400 mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Your cart is empty</h3>
        <p className="text-gray-600">Start adding items to your cart!</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg">
      {/* Header */}
      <div className="px-6 py-4 border-b">
        <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <ShoppingCart className="w-6 h-6" />
          Shopping Cart ({cart.items.length} {cart.items.length === 1 ? 'item' : 'items'})
        </h2>
      </div>

      {/* Cart Items */}
      <div className="divide-y">
        {cart.items.map((cartItem) => (
          <div key={cartItem.id} data-testid="cart-item" className="p-6 hover:bg-gray-50">
            <div className="flex gap-4">
              <div className="flex-1">
                <h3 data-testid="cart-item-name" className="font-semibold text-lg text-gray-900">{cartItem.item.name}</h3>
                <p className="text-sm text-gray-600 mt-1">{cartItem.item.category}</p>
                <p className="text-sm text-gray-500 mt-1 line-clamp-2">
                  {cartItem.item.description}
                </p>
              </div>

              <div className="flex flex-col items-end gap-3">
                <div className="text-right">
                  <div className="text-sm text-gray-600">
                    ${cartItem.item.price.toFixed(2)} each
                  </div>
                  <div data-testid="cart-item-price" className="text-lg font-bold text-blue-800">
                    ${cartItem.subtotal.toFixed(2)}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleUpdateQuantity(cartItem.id, cartItem.quantity - 1)}
                    aria-label="Decrease quantity"
                    className="p-1 rounded hover:bg-gray-200"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span data-testid="cart-item-quantity" className="w-12 text-center font-medium">{cartItem.quantity}</span>
                  <button
                    data-testid="increase-quantity-btn"
                    onClick={() => handleUpdateQuantity(cartItem.id, cartItem.quantity + 1)}
                    aria-label="Increase quantity"
                    className="p-1 rounded hover:bg-gray-200"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                  <button
                    data-testid="remove-item-btn"
                    onClick={() => handleRemoveItem(cartItem.id)}
                    aria-label="Remove item"
                    className="p-1 rounded hover:bg-red-100 text-red-600 ml-2"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div className="px-6 py-4 bg-gray-50 border-t">
        <div className="flex items-center justify-between mb-4">
          <span className="text-lg font-semibold text-gray-900">Total:</span>
          <span data-testid="cart-total" className="text-3xl font-bold text-blue-800">
            ${cart.total.toFixed(2)}
          </span>
        </div>
        <button
          data-testid="checkout-btn"
          onClick={handleCheckout}
          disabled={isCheckingOut}
          className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-blue-800 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed font-semibold"
        >
          <CreditCard className="w-5 h-5" />
          {isCheckingOut ? 'Processing...' : 'Proceed to Checkout'}
        </button>
      </div>
    </div>
  );
}