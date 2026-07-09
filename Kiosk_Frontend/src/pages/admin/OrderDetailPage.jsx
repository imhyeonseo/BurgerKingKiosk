import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAdminOrderDetail, cancelOrder } from "@/api/orders";
import { useUiStore } from "@/stores/uiStore";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { EmptyState } from "@/components/common/EmptyState";
import { IconX } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";
import { formatDateTime } from "@/utils/formatDate";

const COLUMNS = [
  { key: "no", label: "No.", align: "id", render: (_r, i) => i + 1 },
  { key: "menuName", label: "메뉴명", align: "center" },
  { key: "menuPrice", label: "단가", align: "right", render: (row) => formatCurrency(row.menuPrice) },
  { key: "quantity", label: "수량", align: "center" },
  { key: "subtotal", label: "소계", align: "right", render: (row) => formatCurrency(row.subtotal) },
];

export function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  const { data: order, isLoading, isError } = useQuery({
    queryKey: ["admin-order-detail", id],
    queryFn: () => getAdminOrderDetail(Number(id)),
  });

  const cancelMutation = useMutation({
    mutationFn: () => cancelOrder(Number(id)),
    onSuccess: () => {
      showToast("success", "주문이 취소되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-order-detail", id] });
      queryClient.invalidateQueries({ queryKey: ["admin-orders"] });
    },
    onSettled: () => setShowCancelConfirm(false),
  });

  if (isError) {
    return <EmptyState message="주문을 찾을 수 없습니다." actionLabel="목록으로 돌아가기" onAction={() => navigate("/admin/orders")} />;
  }

  return (
    <>
      <PageHeader
        title={`주문 상세 - #${order?.orderNumber ?? ""}`}
        actions={
          order && (
            <button className="btn-danger btn-sm" disabled={order.status === "CANCELLED"} onClick={() => setShowCancelConfirm(true)}>
              <IconX size={14} />
              주문 취소
            </button>
          )
        }
      />

      <div className="panel">
        <div className="panel-body">
          {isLoading || !order ? (
            <div className="skeleton" style={{ height: 100 }} />
          ) : (
            <div className="form-grid" style={{ marginBottom: "var(--sp-2)" }}>
              <div><label>상태</label><div className="value"><StatusBadge status={order.status === "COMPLETED" ? "success" : "danger"} label={order.status} /></div></div>
              <div><label>세션 ID</label><div className="value text-left">{order.sessionId}</div></div>
              <div><label>주문 시각</label><div className="value">{formatDateTime(order.createdAt)}</div></div>
              <div><label>총 결제금액</label><div className="value text-right">{formatCurrency(order.totalPrice)}</div></div>
            </div>
          )}

          <DataTable columns={COLUMNS} rows={order?.items ?? []} rowKey={(_row, i) => i} isLoading={isLoading} />
        </div>
      </div>

      {showCancelConfirm && (
        <ConfirmDialog
          title="주문을 취소할까요?"
          description="취소된 주문은 되돌릴 수 없으며, 재고는 복구되지 않습니다."
          confirmLabel="주문 취소"
          danger
          onConfirm={() => cancelMutation.mutate()}
          onCancel={() => setShowCancelConfirm(false)}
        />
      )}
    </>
  );
}
