import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAdminMenuDetail, getAdminMenus, addSetComponent, removeSetComponent } from "@/api/menus";
import { useUiStore } from "@/stores/uiStore";
import { IconPlus, IconTrash } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function SetComponentsModal({ setMenu, onClose }) {
  const queryClient = useQueryClient();
  const showToast = useUiStore((s) => s.showToast);
  const [selectedComponentId, setSelectedComponentId] = useState("");
  const [componentQuantity, setComponentQuantity] = useState(1);

  const { data: detail } = useQuery({
    queryKey: ["admin-menu-detail", setMenu.id],
    queryFn: () => getAdminMenuDetail(setMenu.id),
  });

  const { data: singleMenus } = useQuery({
    queryKey: ["admin-menus", "single-active"],
    queryFn: () => getAdminMenus({ isSet: false, isActive: true, page: 0, size: 100 }),
  });

  const addMutation = useMutation({
    mutationFn: () => addSetComponent(setMenu.id, { componentMenuId: Number(selectedComponentId), quantity: componentQuantity }),
    onSuccess: () => {
      showToast("success", "구성품이 추가되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-menu-detail", setMenu.id] });
      setSelectedComponentId("");
      setComponentQuantity(1);
    },
  });

  const removeMutation = useMutation({
    mutationFn: (componentMenuId) => removeSetComponent(setMenu.id, componentMenuId),
    onSuccess: () => {
      showToast("success", "구성품이 제거되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["admin-menu-detail", setMenu.id] });
    },
  });

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-dialog wide" onClick={(e) => e.stopPropagation()}>
        <h2>세트 구성 관리 - {setMenu.name}</h2>

        <table>
          <thead>
            <tr>
              <th className="col-text">구성 단품</th>
              <th className="col-amount">수량</th>
              <th className="col-actions"></th>
            </tr>
          </thead>
          <tbody>
            {(detail?.setComponents ?? []).map((c) => (
              <tr key={c.id}>
                <td className="col-text">{c.name}</td>
                <td className="col-amount">{c.quantity}</td>
                <td className="col-actions">
                  <div className="actions-right">
                    <button className="btn-danger btn-sm" onClick={() => removeMutation.mutate(c.id)}>
                      <IconTrash size={14} />
                      제거
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {(detail?.setComponents ?? []).length === 0 && (
              <tr>
                <td colSpan={3} className="table-empty">
                  구성품이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        <div className="toolbar" style={{ marginTop: "var(--sp-4)" }}>
          <select className="field-input" value={selectedComponentId} onChange={(e) => setSelectedComponentId(e.target.value)}>
            <option value="">추가할 단품 메뉴 선택</option>
            {(singleMenus?.content ?? []).map((m) => (
              <option key={m.id} value={m.id}>
                {m.name} ({formatCurrency(m.price)})
              </option>
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
          <button className="btn-ghost btn-md" onClick={onClose}>
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}
