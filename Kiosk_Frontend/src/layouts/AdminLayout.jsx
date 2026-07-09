import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";
import { logout as logoutApi } from "@/api/auth";
import {
  IconDashboard,
  IconFolder,
  IconUtensils,
  IconPackage,
  IconReceipt,
  IconTrendingUp,
  IconClipboard,
  IconLogout,
} from "@/components/common/Icon";

const RAW_NAV_ITEMS = [
  { group: null, to: "/admin/dashboard", label: "대시보드", icon: IconDashboard },
  { group: "메뉴 관리", to: "/admin/categories", label: "카테고리", icon: IconFolder },
  { group: "메뉴 관리", to: "/admin/menus", label: "메뉴", icon: IconUtensils },
  { group: "메뉴 관리", to: "/admin/inventory", label: "재고", icon: IconPackage },
  { group: "운영", to: "/admin/orders", label: "주문", icon: IconReceipt },
  { group: "운영", to: "/admin/sales", label: "매출", icon: IconTrendingUp },
  { group: "운영", to: "/admin/audit-logs", label: "감사 로그", icon: IconClipboard },
];

// 그룹 라벨은 정적 데이터라 모듈 로드 시 한 번만 계산해두고, 렌더링 중에는 값을 재할당하지 않는다.
const NAV_ITEMS = RAW_NAV_ITEMS.map((item, index) => ({
  ...item,
  showGroupLabel: !!item.group && item.group !== RAW_NAV_ITEMS[index - 1]?.group,
}));

export function AdminLayout() {
  const adminUsername = useAuthStore((s) => s.adminUsername);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      await logoutApi();
    } catch {
      // 로그아웃 API 실패해도 클라이언트 측 로그아웃은 그대로 진행한다.
    } finally {
      logout();
      navigate("/admin/login", { replace: true });
    }
  }

  return (
    <div className="admin">
      <aside className="admin-sidebar">
        <div className="brand">
          <IconUtensils size={20} />
          BURGER KIOSK
        </div>
        <nav>
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <div key={item.to}>
                {item.showGroupLabel && <div className="group-label">{item.group}</div>}
                <NavLink to={item.to} className={({ isActive }) => (isActive ? "active" : "")}>
                  <Icon size={20} />
                  {item.label}
                </NavLink>
              </div>
            );
          })}
          <div className="group-label">계정</div>
          <button className="logout-btn" onClick={handleLogout}>
            <IconLogout size={20} />
            로그아웃
          </button>
        </nav>
      </aside>

      <main className="admin-main">
        <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "var(--sp-2)" }}>
          <div className="admin-user">
            <div className="avatar">{adminUsername ? adminUsername[0].toUpperCase() : "A"}</div>
            {adminUsername ?? "admin"}
          </div>
        </div>
        <Outlet />
      </main>
    </div>
  );
}
