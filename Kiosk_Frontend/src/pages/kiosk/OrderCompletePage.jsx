import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useCartStore } from "@/stores/cartStore";
import { IconCheck } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function OrderCompletePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const clearCart = useCartStore((s) => s.clearCart);
  const order = location.state;

  useEffect(() => {
    if (!order) {
      navigate("/", { replace: true });
    }
  }, [order, navigate]);

  if (!order) return null;

  function handleGoHome() {
    clearCart(); // 다음 손님을 위해 세션 초기화(Frontend.md 13.1)
    navigate("/", { replace: true });
  }

  return (
    <>
      <header className="kiosk-header">
        <span className="side" />
        <span className="title">주문 완료</span>
        <span className="side right" />
      </header>

      <main className="kiosk-body">
        <div className="center-block">
          <div className="circle">
            <IconCheck size={36} />
          </div>
          <div className="headline">주문이 완료되었습니다</div>
          <div className="subline">화면의 번호가 호출되면 카운터에서 수령해주세요</div>

          <div className="order-number-box">
            <div className="label">주문 번호</div>
            <div className="number">{order.orderNumber}</div>
          </div>

          <div className="receipt">
            {order.items.map((item, i) => (
              <div className="row" key={i}>
                <span>{item.menuName} x{item.quantity}</span>
                <span className="value">{formatCurrency(item.subtotal)}</span>
              </div>
            ))}
            <div className="row total">
              <span>총 결제금액</span>
              <span className="value">{formatCurrency(order.totalPrice)}</span>
            </div>
          </div>
        </div>
      </main>

      <div className="bottom-bar">
        <button className="btn-primary btn-xl" style={{ flex: 1 }} onClick={handleGoHome}>
          처음으로
        </button>
      </div>
    </>
  );
}
