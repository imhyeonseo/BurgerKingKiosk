import { create } from "zustand";

function generateId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

export const useUiStore = create((set) => ({
  isLoading: false,
  toasts: [],

  setLoading: (loading) => set({ isLoading: loading }),

  showToast: (type, message) =>
    set((state) => ({ toasts: [...state.toasts, { id: generateId(), type, message }] })),

  hideToast: (id) => set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
}));
