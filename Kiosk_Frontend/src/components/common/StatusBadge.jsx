import { IconCheck, IconX, IconAlert, IconEyeOff } from "@/components/common/Icon";

const VARIANTS = {
  success: { className: "badge-success", Icon: IconCheck },
  danger: { className: "badge-danger", Icon: IconX },
  warning: { className: "badge-warning", Icon: IconAlert },
  neutral: { className: "badge-neutral", Icon: IconEyeOff },
  secondary: { className: "badge-secondary", Icon: null },
};

export function StatusBadge({ status, label, showIcon = true }) {
  const variant = VARIANTS[status] ?? VARIANTS.neutral;
  const Icon = variant.Icon;
  return (
    <span className={`badge ${variant.className}`}>
      {showIcon && Icon && <Icon size={12} />}
      {label}
    </span>
  );
}
