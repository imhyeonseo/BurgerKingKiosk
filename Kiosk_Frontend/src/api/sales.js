import { apiClient } from "./client";

export const getDailySales = (date) =>
  apiClient.get("/admin/sales/daily", { params: { date } }).then((r) => r.data);

export const getMonthlySales = (year, month) =>
  apiClient.get("/admin/sales/monthly", { params: { year, month } }).then((r) => r.data);

export const getYearlySales = (year) =>
  apiClient.get("/admin/sales/yearly", { params: { year } }).then((r) => r.data);
