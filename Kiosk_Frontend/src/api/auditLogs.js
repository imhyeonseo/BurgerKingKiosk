import { apiClient } from "./client";

export const getAuditLogs = (params) =>
  apiClient.get("/admin/audit-logs", { params }).then((r) => r.data);

export const getAuditLogDetail = (logId) =>
  apiClient.get(`/admin/audit-logs/${logId}`).then((r) => r.data);
