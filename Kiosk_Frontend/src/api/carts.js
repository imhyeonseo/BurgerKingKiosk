import { apiClient } from "./client";

export const getCart = () => apiClient.get("/carts").then((r) => r.data);

export const addCartItem = (payload) => apiClient.post("/carts/items", payload).then((r) => r.data);

export const updateCartItem = (cartItemId, payload) =>
  apiClient.patch(`/carts/items/${cartItemId}`, payload).then((r) => r.data);

export const deleteCartItem = (cartItemId) =>
  apiClient.delete(`/carts/items/${cartItemId}`).then((r) => r.data);

export const clearCart = () => apiClient.delete("/carts").then((r) => r.data);
