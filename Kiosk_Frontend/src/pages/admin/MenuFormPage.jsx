import { useEffect, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { getAdminCategories } from "@/api/categories";
import { getAdminMenuDetail, createMenu, updateMenu } from "@/api/menus";
import { uploadMenuImage } from "@/api/images";
import { useUiStore } from "@/stores/uiStore";
import { PageHeader } from "@/components/common/PageHeader";
import { IconUtensils } from "@/components/common/Icon";

const ACCEPTED_IMAGE_TYPES = "image/jpeg,image/png,image/webp";

export function MenuFormPage({ mode }) {
  const isEdit = mode === "edit";
  const { id } = useParams();
  const navigate = useNavigate();
  const showToast = useUiStore((s) => s.showToast);

  const { data: categories = [] } = useQuery({ queryKey: ["admin-categories"], queryFn: getAdminCategories });
  // 단품 등록/수정 시 세트 전용 카테고리는 선택 불가하도록 제외
  const singleCategories = categories.filter((c) => !c.name.includes("세트"));
  const { data: menu } = useQuery({
    queryKey: ["admin-menu-detail", id],
    queryFn: () => getAdminMenuDetail(Number(id)),
    enabled: isEdit,
  });

  const { register, handleSubmit, reset, watch, formState: { isSubmitting } } = useForm({
    defaultValues: { categoryId: "", name: "", description: "", price: 0, quantity: 0, imageUrl: "" },
  });

  useEffect(() => {
    if (isEdit && menu) {
      reset({
        categoryId: menu.categoryId,
        name: menu.name,
        description: menu.description ?? "",
        price: menu.price,
        quantity: menu.quantity,
        imageUrl: menu.imageUrl ?? "",
      });
    }
  }, [isEdit, menu, reset]);

  const selectedFile = watch("imageFile")?.[0];
  const currentImageUrl = watch("imageUrl");

  // 새로 고른 파일이 있으면 그 미리보기를, 없으면 기존/입력된 imageUrl을 보여준다.
  const previewSrc = useMemo(() => {
    if (selectedFile) return URL.createObjectURL(selectedFile);
    return currentImageUrl || null;
  }, [selectedFile, currentImageUrl]);

  useEffect(() => {
    return () => {
      if (selectedFile && previewSrc) URL.revokeObjectURL(previewSrc);
    };
  }, [selectedFile, previewSrc]);

  async function onSubmit(values) {
    const imageFile = values.imageFile?.[0];
    const base = {
      categoryId: Number(values.categoryId),
      name: values.name,
      description: values.description || undefined,
      price: Number(values.price),
      imageUrl: values.imageUrl || undefined,
    };
    try {
      if (isEdit) {
        // PUT /admin/menus/{id}는 JSON만 받으므로, 파일을 골랐다면 먼저 업로드해서 URL을 받아온 뒤 그 URL로 교체한다.
        const imageUrl = imageFile ? (await uploadMenuImage(imageFile)).imageUrl : base.imageUrl;
        await updateMenu(Number(id), { ...base, imageUrl, isActive: true });
      } else {
        // POST /admin/menus는 멀티파트라 파일을 함께 첨부하면 서버가 업로드까지 한 번에 처리한다.
        await createMenu({ ...base, quantity: Number(values.quantity) }, imageFile);
      }
      showToast("success", "저장되었습니다.");
      navigate("/admin/menus");
    } catch {
      // 토스트는 인터셉터가 표시
    }
  }

  return (
    <>
      <PageHeader title={isEdit ? "단품 메뉴 수정" : "단품 메뉴 등록"} />

      <div className="panel">
        <div className="panel-body">
          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="form-grid">
              <div>
                <label>카테고리</label>
                <select {...register("categoryId", { required: true })}>
                  <option value="">선택</option>
                  {singleCategories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label>메뉴명</label>
                <input type="text" {...register("name", { required: true })} />
              </div>
              <div className="full">
                <label>설명</label>
                <textarea {...register("description")} />
              </div>
              <div>
                <label>가격</label>
                <input type="number" min={1} {...register("price", { required: true, valueAsNumber: true, min: 1 })} />
              </div>
              {!isEdit && (
                <div>
                  <label>수량</label>
                  <input type="number" min={0} {...register("quantity", { required: true, valueAsNumber: true, min: 0 })} />
                </div>
              )}

              <div className="full">
                <label>이미지</label>
                <div style={{ display: "flex", gap: "var(--sp-4)", alignItems: "flex-start" }}>
                  <div className="placeholder" style={{ width: 96, height: 96, flex: "none" }}>
                    <IconUtensils size={28} />
                    {previewSrc && (
                      <img className="thumb-img" src={previewSrc} alt="미리보기" onError={(e) => e.currentTarget.remove()} />
                    )}
                  </div>
                  <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: "var(--sp-2)" }}>
                    <input type="file" accept={ACCEPTED_IMAGE_TYPES} {...register("imageFile")} />
                    <span className="hint">JPG/PNG/WEBP, 5MB 이하. 파일을 첨부하면 아래 URL 입력은 무시됩니다.</span>
                    <input type="text" placeholder="/images/menu/example.jpg" {...register("imageUrl")} />
                  </div>
                </div>
              </div>
            </div>

            <div className="form-actions">
              <button type="button" className="btn-ghost btn-md" onClick={() => navigate("/admin/menus")}>
                취소
              </button>
              <button type="submit" className="btn-primary btn-md" disabled={isSubmitting}>
                {isSubmitting ? <span className="spinner" /> : "저장"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
