import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getCategories } from "@/api/categories";
import { getCategoryMenus, searchMenus } from "@/api/menus";
import { getCart } from "@/api/carts";
import { useCartStore, cartSelectors } from "@/stores/cartStore";
import { useDebounce } from "@/hooks/useDebounce";
import { MenuCard } from "@/components/kiosk/MenuCard";
import { MenuDetailModal } from "@/components/kiosk/MenuDetailModal";
import { CartButton } from "@/components/kiosk/CartButton";
import { FlyingImage } from "@/components/kiosk/FlyingImage";
import { EmptyState } from "@/components/common/EmptyState";
import { IconCart, IconSearch } from "@/components/common/Icon";
import { cartButtonRectRef, triggerCartButtonPulse } from "@/hooks/cartButtonBus";

export function MenuListPage() {
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [selectedMenuId, setSelectedMenuId] = useState(null);
  const [flying, setFlying] = useState(null); // { src, startRect }
  const debouncedKeyword = useDebounce(keyword.trim(), 300);
  const isSearching = debouncedKeyword.length > 0;

  const sessionId = useCartStore((s) => s.sessionId);
  const totalQuantity = useCartStore(cartSelectors.totalQuantity);
  const setCart = useCartStore((s) => s.setCart);

  const { data: categories = [] } = useQuery({
    queryKey: ["categories"],
    queryFn: getCategories,
  });

  const effectiveCategoryId = selectedCategoryId ?? categories[0]?.id ?? null;

  const { data: categoryMenus = [], isLoading: isCategoryMenusLoading } = useQuery({
    queryKey: ["category-menus", effectiveCategoryId],
    queryFn: () => getCategoryMenus(effectiveCategoryId),
    enabled: effectiveCategoryId != null && !isSearching,
  });

  const { data: searchResults = [], isLoading: isSearchLoading } = useQuery({
    queryKey: ["menu-search", debouncedKeyword],
    queryFn: () => searchMenus(debouncedKeyword),
    enabled: isSearching,
  });

  useEffect(() => {
    if (!sessionId) return;
    getCart()
      .then((cart) => setCart(cart.cartId, cart.items, cart.totalPrice))
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const menus = isSearching ? searchResults : categoryMenus;
  const isLoading = isSearching ? isSearchLoading : isCategoryMenusLoading;

  function handleAddedToCart({ src, startRect }) {
    setFlying({ src, startRect });
  }

  return (
    <>
      <header className="kiosk-header">
        <span className="side title">BURGER KIOSK</span>
        <Link className="side right" to="/cart">
          <IconCart size={24} />
          {totalQuantity > 0 && <span className="cart-count">{totalQuantity}</span>}
        </Link>
      </header>

      <div style={{ padding: "0 var(--sp-5)" }}>
        <div className="search-bar">
          <input
            type="text"
            placeholder="메뉴 이름 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
      </div>

      <nav className={`tabs ${isSearching ? "disabled" : ""}`}>
        {categories.map((category) => (
          <button
            key={category.id}
            type="button"
            className={`tab ${category.id === effectiveCategoryId ? "active" : ""}`}
            onClick={() => setSelectedCategoryId(category.id)}
          >
            {category.name}
          </button>
        ))}
      </nav>

      <main className="kiosk-body">
        {!isLoading && menus.length === 0 ? (
          <EmptyState icon={IconSearch} message={isSearching ? "검색 결과가 없습니다" : "등록된 메뉴가 없습니다"} />
        ) : (
          <div className="menu-grid">
            {isLoading
              ? Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="skeleton" style={{ aspectRatio: "1 / 1.3", borderRadius: "var(--radius-lg)" }} />
                ))
              : menus.map((menu) => (
                  <MenuCard key={menu.id} menu={menu} onClick={() => setSelectedMenuId(menu.id)} />
                ))}
          </div>
        )}
      </main>

      <div className="bottom-bar">
        <CartButton />
      </div>

      {selectedMenuId != null && (
        <MenuDetailModal
          menuId={selectedMenuId}
          onClose={() => setSelectedMenuId(null)}
          onAddedToCart={handleAddedToCart}
        />
      )}

      {flying && (
        <FlyingImage
          src={flying.src}
          startRect={flying.startRect}
          endRect={cartButtonRectRef.current}
          onComplete={() => {
            setFlying(null);
            triggerCartButtonPulse();
          }}
        />
      )}
    </>
  );
}
