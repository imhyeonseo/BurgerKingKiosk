import { apiClient } from "./client";

export const login = (payload) => apiClient.post("/admin/auth/login", payload).then((r) => r.data);

export const logout = () => apiClient.post("/admin/auth/logout").then((r) => r.data);
