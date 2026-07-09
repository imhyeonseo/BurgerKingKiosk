import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCartStore, cartSelectors } from "@/stores/cartStore";
import { cartButtonRectRef, onCartPulse } from "@/hooks/cartButtonBus";
import { formatCurrency } from "@/utils/formatCurrency";

export function CartButton() {
  const navigate = useNavigate();
  const totalQuantity = useCartStore(cartSelectors.totalQuantity);
  const totalPrice = useCartStore((s) => s.totalPrice);
  const ref = useRef(null);
  const [isPulsing, setIsPulsing] = useState(false);

  useEffect(() => {
    cartButtonRectRef.current = ref.current?.getBoundingClientRect() ?? null;
  });

  useEffect(() => {
    return onCartPulse(() => {
      setIsPulsing(true);
      const timer = setTimeout(() => setIsPulsing(false), 400);
      return () => clearTimeout(timer);
    });
  }, []);

  const disabled = totalQuantity === 0;

  return (
    <button
      ref={ref}
      type="button"
      className={`btn-primary btn-xl cart-button ${isPulsing ? "pulse" : ""}`}
      style={{ flex: 1 }}
      disabled={disabled}
      onClick={() => navigate("/cart")}
    >
      장바구니 보기{disabled ? "" : ` · ${totalQuantity}개 · ${formatCurrency(totalPrice)}`}
    </button>
  );
}
