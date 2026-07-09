import { create } from "zustand";

const SESSION_STORAGE_KEY = "kiosk-session-id";

function recalcTotal(items) {
  return items.reduce((sum, item) => sum + item.subtotal, 0);
}

export const useCartStore = create((set) => ({
  sessionId: sessionStorage.getItem(SESSION_STORAGE_KEY),
  cartId: null,
  cartItems: [],
  totalPrice: 0,

  setSessionId: (id) => {
    sessionStorage.setItem(SESSION_STORAGE_KEY, id);
    set({ sessionId: id });
  },

  setCart: (cartId, items, totalPrice) => set({ cartId, cartItems: items, totalPrice }),

  removeItem: (cartItemId) =>
    set((state) => {
      const items = state.cartItems.filter((item) => item.cartItemId !== cartItemId);
      return { cartItems: items, totalPrice: recalcTotal(items) };
    }),

  updateQuantityLocal: (cartItemId, quantity) =>
    set((state) => {
      const items = state.cartItems.map((item) =>
        item.cartItemId === cartItemId
          ? { ...item, quantity, subtotal: item.price * quantity }
          : item
      );
      return { cartItems: items, totalPrice: recalcTotal(items) };
    }),

  clearCart: () => {
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    set({ sessionId: null, cartId: null, cartItems: [], totalPrice: 0 });
  },
}));

export const cartSelectors = {
  totalQuantity: (state) => state.cartItems.reduce((sum, item) => sum + item.quantity, 0),
};
