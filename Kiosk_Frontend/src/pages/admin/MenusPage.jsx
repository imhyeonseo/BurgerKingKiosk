import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAdminMenus, deleteMenu } from "@/api/menus";
import { getAdminCategories } from "@/api/categories";
import { useUiStore } from "@/stores/uiStore";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { SetComponentsModal } from "@/pages/admin/SetComponentsModal";
import { IconPlus, IconPencil, IconTrash } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

const PAGE_SIZE = 20;

export function MenusPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);

  const [categoryId, setCategoryId] = useState("");
  const [isSet, setIsSet] = useState("");
  const [isActive, setIsActive] = useState("");
  const [page, setPage] = useState(0);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [componentsTarget, setComponentsTarget] = useState(null);

  const { data: categories = [] } = useQuery({ queryKey: ["admin-categories"], queryFn: getAdminCategories });

  const filters = {
    categoryId: categoryId === "" ? undefined : Number(categoryId),
    isSet: isSet === "" ? undefined : isSet === "true",
    isActive: isActive === "" ? undefined : isActive === "true",
    page,
    size: PAGE_SIZE,
  };

  const { data, isLoading } = useQuery({
    queryKey: ["admin-menus", filters],
    queryFn: () => getAdminMenus(filters),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => deleteMenu(id),
    onSuccess: () => {
      showToast("success", "메뉴가 삭제되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-menus"] });
    },
    onSettled: () => setDeleteTarget(null),
  });

  const columns = [
    { key: "no", label: "No.", align: "id", render: (_r, i) => page * PAGE_SIZE + i + 1 },
    { key: "name", label: "메뉴명", align: "center" },
    { key: "categoryName", label: "카테고리", align: "center" },
    { key: "price", label: "가격", align: "right", render: (row) => formatCurrency(row.price) },
    { key: "isSet", label: "구분", align: "center", render: (row) => <StatusBadge status={row.isSet ? "secondary" : "neutral"} label={row.isSet ? "세트" : "단품"} showIcon={false} /> },
    { key: "quantity", label: "재고", align: "right" },
    { key: "isActive", label: "활성", align: "center", render: (row) => <StatusBadge status={row.isActive ? "success" : "neutral"} label={row.isActive ? "판매중" : "숨김"} /> },
    {
      key: "actions",
      label: "",
      align: "right",
      render: (row) => (
        <div className="actions-right">
          {row.isSet && (
            <button className="btn-ghost btn-sm" onClick={() => setComponentsTarget(row)}>
              구성품
            </button>
          )}
          <button className="btn-ghost btn-sm" onClick={() => navigate(`/admin/menus/${row.id}/edit`)}>
            <IconPencil size={14} />
            수정
          </button>
          <button className="btn-danger btn-sm" onClick={() => setDeleteTarget(row)}>
            <IconTrash size={14} />
            삭제
          </button>
        </div>
      ),
    },
  ];

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <PageHeader
        title="메뉴 관리"
        subtitle="단품/세트 메뉴 등록 및 관리 (재고 수정은 재고 관리 메뉴 이용)"
        actions={
          <>
            <button className="btn-secondary btn-sm" onClick={() => navigate("/admin/menus/sets/new")}>
              <IconPlus size={14} />
              세트 등록
            </button>
            <button className="btn-primary btn-sm" onClick={() => navigate("/admin/menus/new")}>
              <IconPlus size={14} />
              단품 등록
            </button>
          </>
        }
      />

      <div className="panel">
        <div className="panel-body">
          <div className="toolbar">
            <select className="field-input" value={categoryId} onChange={(e) => { setCategoryId(e.target.value); setPage(0); }}>
              <option value="">전체 카테고리</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <select className="field-input" value={isSet} onChange={(e) => { setIsSet(e.target.value); setPage(0); }}>
              <option value="">전체(단품/세트)</option>
              <option value="false">단품만</option>
              <option value="true">세트만</option>
            </select>
            <select className="field-input" value={isActive} onChange={(e) => { setIsActive(e.target.value); setPage(0); }}>
              <option value="">전체 상태</option>
              <option value="true">판매중</option>
              <option value="false">숨김</option>
            </select>
          </div>

          <DataTable columns={columns} rows={data?.content ?? []} rowKey={(row) => row.id} isLoading={isLoading} />

          {totalPages > 1 && (
            <div className="pagination">
              {Array.from({ length: totalPages }).map((_, i) => (
                <button key={i} className={i === page ? "active" : ""} onClick={() => setPage(i)}>
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {deleteTarget && (
        <ConfirmDialog
          title="메뉴를 삭제할까요?"
          description={`"${deleteTarget.name}" 메뉴를 삭제합니다.`}
          confirmLabel="삭제"
          danger
          onConfirm={() => deleteMutation.mutate(deleteTarget.id)}
          onCancel={() => setDeleteTarget(null)}
        />
      )}

      {componentsTarget && (
        <SetComponentsModal setMenu={componentsTarget} onClose={() => setComponentsTarget(null)} />
      )}
    </>
  );
}
