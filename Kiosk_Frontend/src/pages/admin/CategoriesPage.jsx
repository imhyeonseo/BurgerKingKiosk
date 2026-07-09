import { useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from "@/api/categories";
import { useUiStore } from "@/stores/uiStore";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { IconPlus, IconPencil, IconTrash } from "@/components/common/Icon";
import { formatDate } from "@/utils/formatDate";

function CategoryFormModal({ category, onClose }) {
  const isEdit = !!category;
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    defaultValues: {
      name: category?.name ?? "",
      displayOrder: category?.displayOrder ?? 0,
      isActive: category?.isActive ?? true,
    },
  });

  async function onSubmit(values) {
    const payload = {
      name: values.name,
      displayOrder: Number(values.displayOrder),
      isActive: values.isActive === true || values.isActive === "true",
    };
    try {
      if (isEdit) {
        await updateCategory(category.id, payload);
      } else {
        await createCategory({ name: payload.name, displayOrder: payload.displayOrder });
      }
      showToast("success", "저장되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-categories"] });
      onClose();
    } catch {
      // 토스트는 인터셉터가 표시
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        <h2>{isEdit ? "카테고리 수정" : "카테고리 등록"}</h2>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="field">
            <label>카테고리명</label>
            <input type="text" {...register("name", { required: true })} />
          </div>
          <div className="field">
            <label>노출 순서</label>
            <input type="number" {...register("displayOrder", { required: true, valueAsNumber: true })} />
          </div>
          {isEdit && (
            <div className="field">
              <label>노출 여부</label>
              <select {...register("isActive")}>
                <option value="true">노출</option>
                <option value="false">숨김</option>
              </select>
            </div>
          )}
          <div className="form-actions">
            <button type="button" className="btn-ghost btn-md" onClick={onClose}>
              취소
            </button>
            <button type="submit" className="btn-primary btn-md" disabled={isSubmitting}>
              {isSubmitting ? <span className="spinner" /> : "저장"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export function CategoriesPage() {
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);
  const [formTarget, setFormTarget] = useState(undefined); // undefined: 닫힘, null: 등록, obj: 수정
  const [deleteTarget, setDeleteTarget] = useState(null);

  const { data: categories = [], isLoading } = useQuery({
    queryKey: ["admin-categories"],
    queryFn: getAdminCategories,
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => deleteCategory(id),
    onSuccess: () => {
      showToast("success", "카테고리가 삭제되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-categories"] });
    },
    onSettled: () => setDeleteTarget(null),
  });

  const columns = [
    { key: "no", label: "No.", align: "id", render: (_r, i) => i + 1 },
    { key: "name", label: "카테고리명", align: "center" },
    { key: "displayOrder", label: "노출순서", align: "id" },
    {
      key: "isActive",
      label: "활성여부",
      align: "center",
      render: (row) => <StatusBadge status={row.isActive ? "success" : "neutral"} label={row.isActive ? "노출중" : "숨김"} />,
    },
    { key: "createdAt", label: "등록일", align: "center", render: (row) => formatDate(row.createdAt) },
    {
      key: "actions",
      label: "",
      align: "right",
      render: (row) => (
        <div className="actions-right">
          <button className="btn-ghost btn-sm" onClick={() => setFormTarget(row)}>
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

  return (
    <>
      <PageHeader
        title="카테고리 관리"
        subtitle="비활성 카테고리를 포함한 전체 목록"
        actions={
          <button className="btn-primary btn-sm" onClick={() => setFormTarget(null)}>
            <IconPlus size={14} />
            카테고리 등록
          </button>
        }
      />

      <div className="panel">
        <div className="panel-body">
          <DataTable columns={columns} rows={categories} rowKey={(row) => row.id} isLoading={isLoading} />
        </div>
      </div>

      {formTarget !== undefined && (
        <CategoryFormModal category={formTarget} onClose={() => setFormTarget(undefined)} />
      )}

      {deleteTarget && (
        <ConfirmDialog
          title="카테고리를 삭제할까요?"
          description={`"${deleteTarget.name}" 카테고리를 삭제합니다.`}
          confirmLabel="삭제"
          danger
          onConfirm={() => deleteMutation.mutate(deleteTarget.id)}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </>
  );
}
