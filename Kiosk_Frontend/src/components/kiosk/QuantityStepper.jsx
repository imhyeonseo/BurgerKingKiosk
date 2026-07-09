import { IconMinus, IconPlus } from "@/components/common/Icon";

export function QuantityStepper({ value, onChange, min = 1, max, size = "md" }) {
  const canDecrease = value > min;
  const canIncrease = max === undefined || value < max;

  return (
    <div className={`qty-row ${size === "sm" ? "sm" : ""}`}>
      <button type="button" disabled={!canDecrease} onClick={() => onChange(value - 1)} aria-label="수량 감소">
        <IconMinus size={size === "sm" ? 14 : 16} />
      </button>
      <span className="qty">{value}</span>
      <button type="button" disabled={!canIncrease} onClick={() => onChange(value + 1)} aria-label="수량 증가">
        <IconPlus size={size === "sm" ? 14 : 16} />
      </button>
    </div>
  );
}
