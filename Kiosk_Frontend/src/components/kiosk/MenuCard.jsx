import { IconUtensils } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function MenuCard({ menu, onClick }) {
  const isSoldOut = menu.isSoldOut;

  return (
    <button
      type="button"
      className={`menu-card ${isSoldOut ? "is-sold-out" : ""}`}
      onClick={isSoldOut ? undefined : onClick}
      aria-disabled={isSoldOut}
    >
      <div className="placeholder">
        <IconUtensils size={28} />
        {menu.imageUrl && (
          <img className="thumb-img" src={menu.imageUrl} alt={menu.name} loading="lazy" onError={(e) => e.currentTarget.remove()} />
        )}
        {isSoldOut && (
          <div className="sold-out-overlay">
            <span className="sold-out-label">품절됨</span>
          </div>
        )}
      </div>
      <div className="info">
        <div className="tag-row">
          <span />
          <span>{menu.isSet && <span className="badge badge-secondary">SET</span>}</span>
        </div>
        <div className="name">{menu.name}</div>
        <div className="price">{formatCurrency(menu.price)}</div>
      </div>
    </button>
  );
}
