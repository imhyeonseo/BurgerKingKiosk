import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getAuditLogs, getAuditLogDetail } from "@/api/auditLogs";
import { ADMIN_ACTIONS } from "@/constants/adminActions";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { formatDateTime } from "@/utils/formatDate";

const PAGE_SIZE = 20;

function AuditLogDetailModal({ logId, onClose }) {
  const { data: log, isLoading } = useQuery({ queryKey: ["audit-log-detail", logId], queryFn: () => getAuditLogDetail(logId) });

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-dialog wide" onClick={(e) => e.stopPropagation()}>
        <h2>로그 상세 {log ? `- ${log.action} #${log.id}` : ""}</h2>
        {isLoading || !log ? (
          <div className="skeleton" style={{ height: 160 }} />
        ) : (
          <>
            <div className="form-grid" style={{ marginBottom: "var(--sp-2)" }}>
              <div><label>관리자</label><div className="value">{log.adminUsername ?? "-"}</div></div>
              <div><label>액션</label><div className="value">{log.action}</div></div>
              <div><label>대상</label><div className="value">{log.targetType ?? "-"} {log.targetId != null ? `#${log.targetId}` : ""}</div></div>
              <div><label>시각</label><div className="value">{formatDateTime(log.createdAt)}</div></div>
            </div>
            <div className="form-grid">
              <div className="full">
                <label>before_value</label>
                <div className="code-box">{log.beforeValue ? JSON.stringify(log.beforeValue, null, 2) : "null"}</div>
              </div>
              <div className="full">
                <label>after_value</label>
                <div className="code-box">{log.afterValue ? JSON.stringify(log.afterValue, null, 2) : "null"}</div>
              </div>
            </div>
          </>
        )}
        <div className="form-actions">
          <button className="btn-ghost btn-md" onClick={onClose}>닫기</button>
        </div>
      </div>
    </div>
  );
}

export function AuditLogsPage() {
  const [action, setAction] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [page, setPage] = useState(0);
  const [detailId, setDetailId] = useState(null);

  const filters = {
    action: action || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined,
    page,
    size: PAGE_SIZE,
  };
  const { data, isLoading } = useQuery({ queryKey: ["audit-logs", filters], queryFn: () => getAuditLogs(filters) });

  const columns = [
    { key: "no", label: "No.", align: "id", render: (_r, i) => page * PAGE_SIZE + i + 1 },
    { key: "action", label: "액션코드", align: "center" },
    { key: "targetType", label: "대상타입", align: "center", render: (r) => r.targetType ?? "-" },
    { key: "targetId", label: "대상ID", align: "id", render: (r) => r.targetId ?? "-" },
    { key: "ipAddress", label: "IP", align: "center", render: (r) => r.ipAddress ?? "-" },
    { key: "createdAt", label: "일시", align: "center", render: (r) => formatDateTime(r.createdAt) },
    {
      key: "actions",
      label: "",
      align: "right",
      render: (row) => (
        <div className="actions-right">
          <button className="btn-ghost btn-sm" onClick={() => setDetailId(row.id)}>상세</button>
        </div>
      ),
    },
  ];

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <PageHeader title="감사 로그" subtitle="관리자의 모든 쓰기 행위(POST/PUT/PATCH/DELETE)가 자동 기록됨" />

      <div className="panel">
        <div className="panel-body">
          <div className="toolbar">
            <select className="field-input" value={action} onChange={(e) => { setAction(e.target.value); setPage(0); }}>
              <option value="">전체 액션</option>
              {ADMIN_ACTIONS.map((a) => (
                <option key={a} value={a}>{a}</option>
              ))}
            </select>
            <input className="field-input" type="date" value={startDate} onChange={(e) => { setStartDate(e.target.value); setPage(0); }} />
            <span>~</span>
            <input className="field-input" type="date" value={endDate} onChange={(e) => { setEndDate(e.target.value); setPage(0); }} />
          </div>

          <DataTable columns={columns} rows={data?.content ?? []} rowKey={(row) => row.id} isLoading={isLoading} onRowClick={(row) => setDetailId(row.id)} />

          {totalPages > 1 && (
            <div className="pagination">
              {Array.from({ length: totalPages }).map((_, i) => (
                <button key={i} className={i === page ? "active" : ""} onClick={() => setPage(i)}>{i + 1}</button>
              ))}
            </div>
          )}
        </div>
      </div>

      {detailId != null && <AuditLogDetailModal logId={detailId} onClose={() => setDetailId(null)} />}
    </>
  );
}
