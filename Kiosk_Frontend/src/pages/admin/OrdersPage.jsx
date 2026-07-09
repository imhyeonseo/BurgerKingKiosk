import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getAdminOrders } from "@/api/orders";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { SegmentedControl } from "@/components/common/SegmentedControl";
import { formatCurrency } from "@/utils/formatCurrency";
import { formatDateTime } from "@/utils/formatDate";

const PAGE_SIZE = 20;
const STATUS_OPTIONS = [
  { value: "", label: "전체" },
  { value: "COMPLETED", label: "완료" },
  { value: "CANCELLED", label: "취소" },
];

export function OrdersPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);

  const filters = { status: status || undefined, page, size: PAGE_SIZE };
  const { data, isLoading } = useQuery({ queryKey: ["admin-orders", filters], queryFn: () => getAdminOrders(filters) });

  const columns = [
    { key: "no", label: "No.", align: "id", render: (_r, i) => page * PAGE_SIZE + i + 1 },
    { key: "orderNumber", label: "주문번호", align: "id" },
    { key: "totalPrice", label: "총액", align: "right", render: (row) => formatCurrency(row.totalPrice) },
    { key: "status", label: "상태", align: "center", render: (row) => <StatusBadge status={row.status === "COMPLETED" ? "success" : "danger"} label={row.status} /> },
    { key: "createdAt", label: "주문일시", align: "center", render: (row) => formatDateTime(row.createdAt) },
    {
      key: "actions",
      label: "",
      align: "right",
      render: (row) => (
        <div className="actions-right">
          <button className="btn-ghost btn-sm" onClick={() => navigate(`/admin/orders/${row.id}`)}>상세 보기</button>
        </div>
      ),
    },
  ];

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <PageHeader title="주문 내역" subtitle="최신순 정렬" />

      <div className="panel">
        <div className="panel-body">
          <div className="toolbar">
            <SegmentedControl options={STATUS_OPTIONS} value={status} onChange={(v) => { setStatus(v); setPage(0); }} />
          </div>

          <DataTable columns={columns} rows={data?.content ?? []} rowKey={(row) => row.id} isLoading={isLoading} />

          {totalPages > 1 && (
            <div className="pagination">
              {Array.from({ length: totalPages }).map((_, i) => (
                <button key={i} className={i === page ? "active" : ""} onClick={() => setPage(i)}>{i + 1}</button>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
