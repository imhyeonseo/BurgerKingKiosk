import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { getCart, updateCartItem, deleteCartItem, clearCart as clearCartApi } from "@/api/carts";
import { createOrder } from "@/api/orders";
import { useCartStore } from "@/stores/cartStore";
import { QuantityStepper } from "@/components/kiosk/QuantityStepper";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { EmptyState } from "@/components/common/EmptyState";
import { IconArrowLeft, IconUtensils, IconX, IconTrash, IconCart } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function CartPage() {
  const navigate = useNavigate();
  const sessionId = useCartStore((s) => s.sessionId);
  const cartItems = useCartStore((s) => s.cartItems);
  const totalPrice = useCartStore((s) => s.totalPrice);
  const setCart = useCartStore((s) => s.setCart);
  const removeItemLocal = useCartStore((s) => s.removeItem);
  const updateQuantityLocal = useCartStore((s) => s.updateQuantityLocal);
  const clearCartLocal = useCartStore((s) => s.clearCart);

  // 세션이 없으면 애초에 불러올 장바구니가 없으므로 로딩 상태 자체가 필요 없다.
  const [isLoading, setIsLoading] = useState(!!sessionId);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  useEffect(() => {
    if (!sessionId) return;
    getCart()
      .then((cart) => setCart(cart.cartId, cart.items, cart.totalPrice))
      .finally(() => setIsLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionId]);

  async function handleQuantityChange(cartItemId, nextQty) {
    const prevItems = cartItems;
    updateQuantityLocal(cartItemId, nextQty); // 1) 낙관적 업데이트
    try {
      await updateCartItem(cartItemId, { quantity: nextQty }); // 2) 서버 반영
    } catch {
      setCart(null, prevItems, prevItems.reduce((sum, i) => sum + i.subtotal, 0)); // 3) 롤백
    }
  }

  async function handleRemove(cartItemId) {
    try {
      await deleteCartItem(cartItemId);
      removeItemLocal(cartItemId);
    } catch {
      // 토스트는 인터셉터가 처리
    }
  }

  async function handleClearCart() {
    setShowClearConfirm(false);
    try {
      await clearCartApi();
      clearCartLocal();
    } catch {
      // 토스트는 인터셉터가 처리
    }
  }

  async function handleOrder() {
    setIsSubmitting(true);
    try {
      const order = await createOrder();
      clearCartLocal();
      navigate("/order/complete", { state: order });
    } catch {
      // 실패 토스트는 인터셉터가 표시(품절 메뉴명 등 서버 메시지 그대로)
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <header className="kiosk-header">
        <Link className="side" to="/">
          <IconArrowLeft size={24} />
        </Link>
        <span className="title">장바구니</span>
        <span className="side right" />
      </header>

      <main className="kiosk-body">
        {isLoading ? (
          <div className="skeleton" style={{ height: 200 }} />
        ) : cartItems.length === 0 ? (
          <EmptyState icon={IconCart} message="장바구니가 비어있습니다" actionLabel="메뉴로 돌아가기" onAction={() => navigate("/")} />
        ) : (
          <>
            <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "var(--sp-2)" }}>
              <button className="btn-ghost btn-sm" onClick={() => setShowClearConfirm(true)}>
                <IconTrash size={14} />
                장바구니 비우기
              </button>
            </div>

            {cartItems.map((item) => (
              <div className="cart-item" key={item.cartItemId}>
                <div className="placeholder">
                  <IconUtensils size={24} />
                  {item.imageUrl && (
                    <img className="thumb-img" src={item.imageUrl} alt={item.menuName} onError={(e) => e.currentTarget.remove()} />
                  )}
                </div>
                <div className="meta">
                  <div className="top-row">
                    <span className="name">{item.menuName}</span>
                    <span className="price">{formatCurrency(item.subtotal)}</span>
                  </div>
                  <div className="bottom-row">
                    <QuantityStepper
                      size="sm"
                      value={item.quantity}
                      onChange={(next) => handleQuantityChange(item.cartItemId, next)}
                      min={1}
                    />
                    <button className="remove" onClick={() => handleRemove(item.cartItemId)}>
                      <IconX size={14} />
                      빼기
                    </button>
                  </div>
                </div>
              </div>
            ))}

            <div className="summary">
              <div className="row">
                <span>상품 수</span>
                <span className="value">{cartItems.length}종 · {cartItems.reduce((s, i) => s + i.quantity, 0)}개</span>
              </div>
              <div className="row total">
                <span>총 결제금액</span>
                <span className="value">{formatCurrency(totalPrice)}</span>
              </div>
            </div>
          </>
        )}
      </main>

      {cartItems.length > 0 && (
        <div className="bottom-bar">
          <button className="btn-primary btn-xl" style={{ flex: 1 }} disabled={isSubmitting} onClick={handleOrder}>
            {isSubmitting ? <span className="spinner" /> : `주문하기 · ${formatCurrency(totalPrice)}`}
          </button>
        </div>
      )}

      {showClearConfirm && (
        <ConfirmDialog
          title="장바구니를 비울까요?"
          description="담긴 메뉴가 모두 삭제됩니다."
          confirmLabel="비우기"
          danger
          onConfirm={handleClearCart}
          onCancel={() => setShowClearConfirm(false)}
        />
      )}
    </>
  );
}
