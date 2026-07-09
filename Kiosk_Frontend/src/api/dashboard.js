import { apiClient } from "./client";

export const getDashboard = () => apiClient.get("/admin/dashboard").then((r) => r.data);
