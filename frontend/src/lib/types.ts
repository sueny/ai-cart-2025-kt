export interface Item {
  id: string;
  name: string;
  category: string;
  description: string | null;
  price: number;
  status: string;
  createdAt: string;
}

export interface CartItem {
  id: string;
  item: Item;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  id: string;
  userId: string | null;
  items: CartItem[];
  total: number;
  createdAt: string;
}

export interface Order {
  id: string;
  cartId: string;
  items: CartItem[];
  total: number;
  status: string;
  createdAt: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  messages: ChatMessage[];
  cartId: string | null;
}

export interface ChatResponse {
  response: string;
  cartId: string | null;
}
