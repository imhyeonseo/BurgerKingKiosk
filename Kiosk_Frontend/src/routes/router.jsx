import { createBrowserRouter } from "react-router-dom";
import { KioskLayout } from "@/layouts/KioskLayout";
import { AdminLayout } from "@/layouts/AdminLayout";
import { PrivateRoute } from "@/routes/PrivateRoute";

import { MenuListPage } from "@/pages/kiosk/MenuListPage";
import { CartPage } from "@/pages/kiosk/CartPage";
import { OrderCompletePage } from "@/pages/kiosk/OrderCompletePage";

import { AdminLoginPage } from "@/pages/admin/AdminLoginPage";
import { DashboardPage } from "@/pages/admin/DashboardPage";
import { CategoriesPage } from "@/pages/admin/CategoriesPage";
import { MenusPage } from "@/pages/admin/MenusPage";
import { MenuFormPage } from "@/pages/admin/MenuFormPage";
import { SetMenuWizardPage } from "@/pages/admin/SetMenuWizardPage";
import { InventoryPage } from "@/pages/admin/InventoryPage";
import { OrdersPage } from "@/pages/admin/OrdersPage";
import { OrderDetailPage } from "@/pages/admin/OrderDetailPage";
import { SalesPage } from "@/pages/admin/SalesPage";
import { AuditLogsPage } from "@/pages/admin/AuditLogsPage";

export const router = createBrowserRouter([
  {
    element: <KioskLayout />,
    children: [
      { path: "/", element: <MenuListPage /> },
      { path: "/cart", element: <CartPage /> },
      { path: "/order/complete", element: <OrderCompletePage /> },
    ],
  },
  { path: "/admin/login", element: <AdminLoginPage /> },
  {
    element: <PrivateRoute />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { path: "/admin/dashboard", element: <DashboardPage /> },
          { path: "/admin/categories", element: <CategoriesPage /> },
          { path: "/admin/menus", element: <MenusPage /> },
          { path: "/admin/menus/new", element: <MenuFormPage mode="create" /> },
          { path: "/admin/menus/:id/edit", element: <MenuFormPage mode="edit" /> },
          { path: "/admin/menus/sets/new", element: <SetMenuWizardPage /> },
          { path: "/admin/inventory", element: <InventoryPage /> },
          { path: "/admin/orders", element: <OrdersPage /> },
          { path: "/admin/orders/:id", element: <OrderDetailPage /> },
          { path: "/admin/sales", element: <SalesPage /> },
          { path: "/admin/audit-logs", element: <AuditLogsPage /> },
        ],
      },
    ],
  },
]);
