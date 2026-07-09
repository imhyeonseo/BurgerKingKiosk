import { apiClient } from "./client";

export const getCategories = () => apiClient.get("/categories").then((r) => r.data);

export const getAdminCategories = () => apiClient.get("/admin/categories").then((r) => r.data);

export const getAdminCategoryDetail = (id) =>
  apiClient.get(`/admin/categories/${id}`).then((r) => r.data);

export const createCategory = (payload) =>
  apiClient.post("/admin/categories", payload).then((r) => r.data);

export const updateCategory = (id, payload) =>
  apiClient.put(`/admin/categories/${id}`, payload).then((r) => r.data);

export const deleteCategory = (id) => apiClient.delete(`/admin/categories/${id}`).then((r) => r.data);
