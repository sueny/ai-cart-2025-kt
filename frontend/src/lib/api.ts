import axios from 'axios';
import type { Item, Cart, Order, ChatRequest, ChatResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Items API
export const itemsApi = {
  getAll: () => api.get<Item[]>('/api/v1/items'),
  search: (query: string, category?: string) =>
    api.get<Item[]>('/api/v1/items/search', { params: { q: query, category } }),
  getById: (id: string) => api.get<Item>(`/api/v1/items/${id}`),
  create: (data: { name: string; category: string; description?: string; price: number; status?: string }) =>
    api.post<Item>('/api/v1/items', data),
  update: (id: string, data: Partial<Item>) => api.put<Item>(`/api/v1/items/${id}`, data),
  delete: (id: string) => api.delete(`/api/v1/items/${id}`),
};

// Cart API
export const cartApi = {
  create: (userId?: string) => api.post<Cart>('/api/v1/carts', null, { params: { userId } }),
  get: (id: string) => api.get<Cart>(`/api/v1/carts/${id}`),
  addItem: (cartId: string, itemId: string, quantity: number = 1) => {
    console.log('cartApi.addItem called with:', { cartId, itemId, quantity });
    return api.post(`/api/v1/carts/${cartId}/items`, { itemId, quantity });
  },
  updateItem: (cartId: string, itemId: string, quantity: number) =>
    api.put(`/api/v1/carts/${cartId}/items/${itemId}`, { quantity }),
  removeItem: (cartId: string, itemId: string) =>
    api.delete(`/api/v1/carts/${cartId}/items/${itemId}`),
  clear: (id: string) => api.delete(`/api/v1/carts/${id}`),
};

// Order API
export const orderApi = {
  create: (cartId: string) => api.post<Order>('/api/v1/orders', { cartId }),
  get: (id: string) => api.get<Order>(`/api/v1/orders/${id}`),
  getAll: () => api.get<Order[]>('/api/v1/orders'),
};

// Agent API
export const agentApi = {
  chat: (request: ChatRequest) => api.post<ChatResponse>('/api/v1/agent/chat', request),
};
