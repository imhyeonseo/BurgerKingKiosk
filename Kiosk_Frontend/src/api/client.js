import axios, { AxiosHeaders } from "axios";
import { useAuthStore } from "@/stores/authStore";
import { useCartStore } from "@/stores/cartStore";
import { useUiStore } from "@/stores/uiStore";

export const apiClient = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

// ── 요청 인터셉터: 세션/인증 헤더 자동 첨부 ──────────────────────────────
apiClient.interceptors.request.use((config) => {
  const isAdminRequest = config.url?.startsWith("/admin");
  const headers = config.headers instanceof AxiosHeaders ? config.headers : new AxiosHeaders(config.headers);

  if (isAdminRequest) {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  } else {
    const sessionId = useCartStore.getState().sessionId;
    if (sessionId) {
      headers.set("X-Session-Id", sessionId);
    }
  }

  config.headers = headers;
  return config;
});

// ── 응답 인터셉터: 공통 성공/에러 언랩 + 세션 발급 + 401 처리 ────────────
apiClient.interceptors.response.use(
  (response) => {
    // 장바구니 담기 응답에 새 X-Session-Id가 오면 저장(최초 세션 발급, Backend.md 3.3.2)
    const issuedSessionId = response.headers["x-session-id"];
    if (issuedSessionId && typeof issuedSessionId === "string") {
      useCartStore.getState().setSessionId(issuedSessionId);
    }

    const body = response.data;
    if (body && typeof body === "object" && "success" in body) {
      if (body.success === false) {
        useUiStore.getState().showToast("error", body.message ?? "요청 처리 중 오류가 발생했습니다.");
        return Promise.reject(new Error(body.message ?? "요청 처리 중 오류가 발생했습니다."));
      }
      response.data = body.data;
    }
    return response;
  },
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const wasAuthenticated = useAuthStore.getState().isAuthenticated;
      useAuthStore.getState().logout();
      if (wasAuthenticated) {
        useUiStore.getState().showToast("warning", "세션이 만료되었습니다. 다시 로그인해주세요.");
      }
      if (window.location.pathname !== "/admin/login") {
        window.location.href = "/admin/login";
      }
      return Promise.reject(error);
    }

    const message =
      (axios.isAxiosError(error) && error.response?.data?.message) || "네트워크 오류가 발생했습니다.";
    useUiStore.getState().showToast("error", message);
    return Promise.reject(error instanceof Error ? error : new Error(message));
  }
);
