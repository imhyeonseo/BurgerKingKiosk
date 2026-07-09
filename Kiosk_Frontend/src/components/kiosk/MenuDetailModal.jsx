import { useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getMenuDetail } from "@/api/menus";
import { addCartItem } from "@/api/carts";
import { getCart } from "@/api/carts";
import { useCartStore } from "@/stores/cartStore";
import { QuantityStepper } from "@/components/kiosk/QuantityStepper";
import { IconUtensils } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function MenuDetailModal({ menuId, onClose, onAddedToCart }) {
  const modalImageRef = useRef(null);
  const [quantity, setQuantity] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const setCart = useCartStore((s) => s.setCart);

  const { data: menu, isLoading } = useQuery({
    queryKey: ["menu-detail", menuId],
    queryFn: () => getMenuDetail(menuId),
    enabled: menuId != null,
  });

  async function handleAddToCart() {
    if (!menu || menu.isSoldOut || isSubmitting) return;
    setIsSubmitting(true);
    try {
      await addCartItem({ menuId: menu.id, quantity });
      const cart = await getCart();
      setCart(cart.cartId, cart.items, cart.totalPrice);

      // 모달을 닫기 전에 startRect를 캡처해서 부모에 전달한다.
      // onClose() 이후에는 ref가 언마운트되어 getBoundingClientRect를 쓸 수 없다.
      const startRect = modalImageRef.current?.getBoundingClientRect() ?? null;
      onClose();
      onAddedToCart?.({ src: menu.imageUrl ?? null, startRect });
    } catch {
      // 실패 시 토스트는 axios 인터셉터가 표시.
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-dialog wide" onClick={(e) => e.stopPropagation()}>
        {isLoading || !menu ? (
          <div className="skeleton" style={{ height: 240 }} />
        ) : (
          <>
            <div className="placeholder detail-hero" ref={modalImageRef}>
              <IconUtensils size={48} />
              {menu.imageUrl && (
                <img className="thumb-img" src={menu.imageUrl} alt={menu.name} onError={(e) => e.currentTarget.remove()} />
              )}
            </div>
            <div className="detail-title">{menu.name}</div>
            <div className="detail-price">{formatCurrency(menu.price)}</div>
            {menu.description && <div className="detail-desc">{menu.description}</div>}

            {menu.isSet && menu.setComponents && (
              <>
                <div className="section-label">세트 구성</div>
                <div className="list-box">
                  {menu.setComponents.map((c) => (
                    <div className="row" key={c.id}>
                      <span>{c.name}</span>
                      <span>x{c.quantity}</span>
                    </div>
                  ))}
                </div>
              </>
            )}

            {menu.isSoldOut ? (
              <div className="hint warning">현재 품절된 메뉴입니다.</div>
            ) : (
              <>
                <div className="section-label">수량</div>
                <QuantityStepper value={quantity} onChange={setQuantity} min={1} />
              </>
            )}

            <div className="form-actions">
              <button className="btn-ghost btn-md" onClick={onClose}>
                취소
              </button>
              <button
                className="btn-primary btn-md"
                disabled={menu.isSoldOut || isSubmitting}
                onClick={handleAddToCart}
              >
                {isSubmitting ? <span className="spinner" /> : `장바구니 담기 · ${formatCurrency(menu.price * quantity)}`}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
