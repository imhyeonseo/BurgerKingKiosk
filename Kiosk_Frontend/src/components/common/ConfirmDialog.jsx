export function ConfirmDialog({ title, description, confirmLabel = "확인", danger = false, onConfirm, onCancel }) {
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        {description && <p className="description">{description}</p>}
        <div className="form-actions">
          <button className="btn-ghost btn-md" onClick={onCancel}>
            취소
          </button>
          <button className={danger ? "btn-danger btn-md" : "btn-primary btn-md"} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
