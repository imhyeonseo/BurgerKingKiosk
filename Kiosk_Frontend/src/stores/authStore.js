import { create } from "zustand";
import { persist } from "zustand/middleware";

export const useAuthStore = create(
  persist(
    (set) => ({
      adminUsername: null,
      accessToken: null,
      isAuthenticated: false,

      login: (username, token) =>
        set({ adminUsername: username, accessToken: token, isAuthenticated: true }),

      logout: () => set({ adminUsername: null, accessToken: null, isAuthenticated: false }),
    }),
    { name: "kiosk-admin-auth" }
  )
);
