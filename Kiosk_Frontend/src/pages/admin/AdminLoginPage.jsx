import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { login } from "@/api/auth";
import { useAuthStore } from "@/stores/authStore";

export function AdminLoginPage() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const storeLogin = useAuthStore((s) => s.login);
  const [errorMessage, setErrorMessage] = useState(null);
  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm({ defaultValues: { username: "", password: "" } });

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/admin/dashboard", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  async function onSubmit(values) {
    setErrorMessage(null);
    try {
      const result = await login(values);
      storeLogin(values.username, result.accessToken);
      navigate("/admin/dashboard");
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    }
  }

  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="brand">BURGER KIOSK</div>
        <div className="sub">관리자 백오피스</div>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="field">
            <label htmlFor="username">아이디</label>
            <input id="username" type="text" autoComplete="username" {...register("username", { required: true })} />
          </div>
          <div className="field">
            <label htmlFor="password">비밀번호</label>
            <input id="password" type="password" autoComplete="current-password" {...register("password", { required: true })} />
          </div>

          {errorMessage && <div className="field-error" style={{ marginBottom: "var(--sp-4)" }}>{errorMessage}</div>}

          <button className="btn-primary btn-lg btn-block" type="submit" disabled={isSubmitting}>
            {isSubmitting ? <span className="spinner" /> : "로그인"}
          </button>
        </form>
        <div className="form-note">단일 관리자 계정 · 권한 등급 구분 없음</div>
      </div>
    </div>
  );
}
