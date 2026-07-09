import { apiClient } from "./client";

export const getInventory = (params) => apiClient.get("/admin/inventory", { params }).then((r) => r.data);

export const updateInventory = (menuId, quantity) =>
  apiClient.patch(`/admin/inventory/${menuId}`, { quantity }).then((r) => r.data);
