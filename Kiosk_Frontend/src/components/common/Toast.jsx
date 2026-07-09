import { useEffect } from "react";
import { useUiStore } from "@/stores/uiStore";
import { IconX } from "@/components/common/Icon";

function ToastItem({ toast }) {
  const hideToast = useUiStore((s) => s.hideToast);

  useEffect(() => {
    const timer = setTimeout(() => hideToast(toast.id), 3000);
    return () => clearTimeout(timer);
  }, [toast.id, hideToast]);

  return (
    <div className={`toast ${toast.type}`}>
      <span>{toast.message}</span>
      <button className="close-btn" onClick={() => hideToast(toast.id)} aria-label="닫기">
        <IconX size={14} />
      </button>
    </div>
  );
}

export function Toast() {
  const toasts = useUiStore((s) => s.toasts);
  if (toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} />
      ))}
    </div>
  );
}
