import { apiClient } from "./client";

export const createOrder = () => apiClient.post("/orders").then((r) => r.data);

export const getAdminOrders = (params) => apiClient.get("/admin/orders", { params }).then((r) => r.data);

export const getAdminOrderDetail = (orderId) =>
  apiClient.get(`/admin/orders/${orderId}`).then((r) => r.data);

export const cancelOrder = (orderId) =>
  apiClient.patch(`/admin/orders/${orderId}/cancel`).then((r) => r.data);
