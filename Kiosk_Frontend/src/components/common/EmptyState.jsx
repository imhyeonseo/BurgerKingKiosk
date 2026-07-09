import { IconInbox } from "@/components/common/Icon";

export function EmptyState({ icon: Icon = IconInbox, message, actionLabel, onAction }) {
  return (
    <div className="empty-state">
      <Icon size={40} />
      <div className="message">{message}</div>
      {actionLabel && onAction && (
        <button className="btn-primary btn-md" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
