import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAdminCategories } from "@/api/categories";
import { getAdminMenus, createSetMenu, addSetComponent, removeSetComponent } from "@/api/menus";
import { useUiStore } from "@/stores/uiStore";
import { PageHeader } from "@/components/common/PageHeader";
import { StepWizard } from "@/components/common/StepWizard";
import { IconPlus, IconTrash } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function SetMenuWizardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);
  const [step, setStep] = useState(1);
  const [setMenu, setSetMenu] = useState(null);
  const [components, setComponents] = useState([]);
  const [selectedComponentId, setSelectedComponentId] = useState("");
  const [componentQuantity, setComponentQuantity] = useState(1);

  const { data: categories = [] } = useQuery({ queryKey: ["admin-categories"], queryFn: getAdminCategories });
  const { data: singleMenus } = useQuery({
    queryKey: ["admin-menus", "single-active"],
    queryFn: () => getAdminMenus({ isSet: false, isActive: true, page: 0, size: 100 }),
    enabled: step === 2,
  });

  // "세트" 이름을 포함한 카테고리를 세트 전용으로 간주
  const setCategory = categories.find((c) => c.name.includes("세트"));

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm({
    defaultValues: { categoryId: "", name: "", description: "", price: 0, quantity: 0, imageUrl: "" },
  });

  // 카테고리가 로드되면 세트 카테고리를 기본값으로 세팅
  useEffect(() => {
    if (setCategory) {
      reset((prev) => ({ ...prev, categoryId: setCategory.id }));
    }
  }, [setCategory?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  async function onSubmitStep1(values) {
    try {
      const created = await createSetMenu(
        {
          categoryId: Number(values.categoryId),
          name: values.name,
          description: values.description || undefined,
          price: Number(values.price),
          imageUrl: values.imageUrl || undefined,
          quantity: Number(values.quantity),
        },
        values.imageFile?.[0]
      );
      setSetMenu(created);
      setStep(2);
    } catch {
      // 토스트는 인터셉터가 표시
    }
  }

  const addMutation = useMutation({
    mutationFn: () => addSetComponent(setMenu.id, { componentMenuId: Number(selectedComponentId), quantity: componentQuantity }),
    onSuccess: (mapping) => {
      const menuInfo = singleMenus?.content.find((m) => m.id === mapping.componentMenuId);
      setComponents((prev) => [...prev, { ...mapping, name: menuInfo?.name ?? `#${mapping.componentMenuId}` }]);
      setSelectedComponentId("");
      setComponentQuantity(1);
    },
  });

  const removeMutation = useMutation({
    mutationFn: (componentMenuId) => removeSetComponent(setMenu.id, componentMenuId),
    onSuccess: (_data, componentMenuId) => {
      setComponents((prev) => prev.filter((c) => c.componentMenuId !== componentMenuId));
    },
  });

  function handleComplete() {
    showToast("success", "세트 메뉴가 등록되었습니다.");
    queryClient.invalidateQueries({ queryKey: ["admin-menus"] });
    navigate("/admin/menus");
  }

  return (
    <>
      <PageHeader title="세트 메뉴 등록" />
      <StepWizard steps={["기본 정보", "구성품 추가"]} currentStep={step} />

      {step === 1 ? (
        <div className="panel">
          <div className="panel-body">
            <form onSubmit={handleSubmit(onSubmitStep1)}>
              <div className="form-grid">
                <div>
                  <label>카테고리</label>
                  {/* 세트 카테고리가 있으면 고정 표시, 없으면 전체 선택 가능 */}
                  {setCategory ? (
                    <>
                      <input type="text" value={setCategory.name} readOnly disabled />
                      <input type="hidden" {...register("categoryId")} />
                    </>
                  ) : (
                    <select {...register("categoryId", { required: true })}>
                      <option value="">선택</option>
                      {categories.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  )}
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
                <div>
                  <label>수량</label>
                  <input type="number" min={0} {...register("quantity", { required: true, valueAsNumber: true, min: 0 })} />
                </div>
                <div className="full">
                  <label>이미지</label>
                  <input type="file" accept="image/jpeg,image/png,image/webp" {...register("imageFile")} />
                  <span className="hint">JPG/PNG/WEBP, 5MB 이하. 파일을 첨부하면 아래 URL 입력은 무시됩니다.</span>
                  <input type="text" placeholder="/images/menu/example-set.jpg" {...register("imageUrl")} />
                </div>
              </div>
              <div className="form-actions">
                <button type="button" className="btn-ghost btn-md" onClick={() => navigate("/admin/menus")}>
                  취소
                </button>
                <button type="submit" className="btn-primary btn-md" disabled={isSubmitting}>
                  {isSubmitting ? <span className="spinner" /> : "다음"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : (
        <div className="panel">
          <div className="panel-header">
            <h2>구성 단품 추가 — {setMenu?.name}</h2>
          </div>
          <div className="panel-body">
            <table>
              <thead>
                <tr>
                  <th className="col-text">구성 단품</th>
                  <th className="col-amount">수량</th>
                  <th className="col-actions"></th>
                </tr>
              </thead>
              <tbody>
                {components.map((c) => (
                  <tr key={c.componentMenuId}>
                    <td className="col-text">{c.name}</td>
                    <td className="col-amount">{c.quantity}</td>
                    <td className="col-actions">
                      <div className="actions-right">
                        <button className="btn-danger btn-sm" onClick={() => removeMutation.mutate(c.componentMenuId)}>
                          <IconTrash size={14} />
                          제거
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {components.length === 0 && (
                  <tr>
                    <td colSpan={3} className="table-empty">아직 추가된 구성품이 없습니다.</td>
                  </tr>
                )}
              </tbody>
            </table>

            <div className="toolbar" style={{ marginTop: "var(--sp-4)" }}>
              <select className="field-input" value={selectedComponentId} onChange={(e) => setSelectedComponentId(e.target.value)}>
                <option value="">추가할 단품 메뉴 선택</option>
                {(singleMenus?.content ?? []).map((m) => (
                  <option key={m.id} value={m.id}>{m.name} ({formatCurrency(m.price)})</option>
                ))}
              </select>
              <input
                className="field-input"
                type="number"
                min={1}
                style={{ width: 70 }}
                value={componentQuantity}
                onChange={(e) => setComponentQuantity(Number(e.target.value))}
              />
              <div className="spacer" />
              <button className="btn-primary btn-sm" disabled={!selectedComponentId} onClick={() => addMutation.mutate()}>
                <IconPlus size={14} />
                구성품 추가
              </button>
            </div>

            <div className="form-actions">
              <button className="btn-primary btn-md" disabled={components.length === 0} onClick={handleComplete}>
                세트 등록 완료
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
