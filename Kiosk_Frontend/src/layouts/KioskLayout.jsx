import { Outlet } from "react-router-dom";

export function KioskLayout() {
  return (
    <div className="kiosk">
      <Outlet />
    </div>
  );
}
