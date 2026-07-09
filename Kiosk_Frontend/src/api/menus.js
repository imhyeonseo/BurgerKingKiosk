import { apiClient } from "./client";

export const getCategoryMenus = (categoryId) =>
  apiClient.get(`/categories/${categoryId}/menus`).then((r) => r.data);

export const getMenuDetail = (menuId) => apiClient.get(`/menus/${menuId}`).then((r) => r.data);

export const searchMenus = (keyword) =>
  apiClient.get("/menus/search", { params: { keyword } }).then((r) => r.data);

export const getAdminMenus = (params) =>
  apiClient.get("/admin/menus", { params }).then((r) => r.data);

export const getAdminMenuDetail = (menuId) =>
  apiClient.get(`/admin/menus/${menuId}`).then((r) => r.data);

/**
 * 메뉴 등록은 백엔드가 multipart/form-data(consumes)로만 받는다: "request" 파트(JSON)와
 * 선택적 "image" 파일 파트. imageFile이 있으면 서버가 저장 후 발급한 URL이 request.imageUrl을 덮어쓴다.
 */
function buildMenuFormData(payload, imageFile) {
  const formData = new FormData();
  formData.append("request", new Blob([JSON.stringify(payload)], { type: "application/json" }));
  if (imageFile) {
    formData.append("image", imageFile);
  }
  return formData;
}

export const createMenu = (payload, imageFile) =>
  apiClient
    .post("/admin/menus", buildMenuFormData(payload, imageFile), { headers: { "Content-Type": undefined } })
    .then((r) => r.data);

export const createSetMenu = (payload, imageFile) =>
  apiClient
    .post("/admin/menus/sets", buildMenuFormData(payload, imageFile), { headers: { "Content-Type": undefined } })
    .then((r) => r.data);

export const updateMenu = (menuId, payload) =>
  apiClient.put(`/admin/menus/${menuId}`, payload).then((r) => r.data);

export const deleteMenu = (menuId) => apiClient.delete(`/admin/menus/${menuId}`).then((r) => r.data);

export const addSetComponent = (setMenuId, payload) =>
  apiClient.post(`/admin/menus/sets/${setMenuId}/components`, payload).then((r) => r.data);

export const removeSetComponent = (setMenuId, componentMenuId) =>
  apiClient.delete(`/admin/menus/sets/${setMenuId}/components/${componentMenuId}`).then((r) => r.data);
