# 버거킹 키오스크 시스템 - 프론트엔드 기능 명세서 (Frontend.md)

> 본 문서는 [Backend.md](./Backend.md)(API 계약), [DB.md](./DB.md)(도메인 규칙), [Design.md](./Design.md)(컬러/타이포/스페이싱/컴포넌트 토큰)를 전제로 작성되었다. 화면 구성·상태·API 매핑은 이 세 문서와 100% 일치해야 하며, 충돌 시 Backend.md/DB.md가 우선한다.

---

## 1. 개요

### 1.1 기술 스택

| 영역 | 선택 | 근거 |
|---|---|---|
| 언어 | TypeScript | Backend.md의 DTO 계약을 타입으로 고정해 런타임 오류를 컴파일 타임에 차단 |
| 프레임워크 | React 18 | 함수형 컴포넌트 + Hooks 표준 |
| 번들러 | Vite | 개발 서버 속도, ESM 네이티브 |
| 라우팅 | React Router v6 (`createBrowserRouter`) | 중첩 라우트로 `KioskLayout`/`AdminLayout` 분리에 적합 |
| 전역 상태 | Zustand | 요구된 "슬라이스" 단위가 각각 독립적이고 단순(action 몇 개)해 Redux의 보일러플레이트가 불필요. 스토어 간 의존이 거의 없어 개별 `create()` 스토어로 충분 |
| 서버 상태 캐싱 | TanStack Query (React Query) v5 | 카테고리/메뉴 목록처럼 "조회 후 캐시, 필요시 무효화"하는 데이터에 적합. 장바구니처럼 세션에 강하게 묶인 상태는 React Query 대신 `cartStore`(Zustand)로 직접 관리(캐시 키 설계보다 명시적 갱신이 더 간단) |
| HTTP 클라이언트 | Axios | 인터셉터로 세션/인증 헤더 자동 첨부, 공통 에러 처리 |
| 스타일링 | CSS Modules + Design.md 토큰을 CSS 커스텀 프로퍼티로 이식한 `tokens.css` | 컴포넌트 단위 스코프, 별도 CSS-in-JS 런타임 비용 없음 |
| 폼 | React Hook Form + Zod | 카테고리/메뉴 등록 폼의 필드 검증(Backend.md 4장 에러코드와 1:1 대응하는 클라이언트 사이드 1차 검증) |
| 애니메이션 | 순수 CSS Transition + `requestAnimationFrame`(플라잉 이미지 전용) | 이 프로젝트에서 복잡한 애니메이션은 장바구니 담기 1건뿐이라 별도 라이브러리(Framer Motion 등) 도입 비용이 이득보다 큼 |

### 1.2 아키텍처 원칙

- **기능(도메인) 우선 폴더 구조**: `pages/kiosk`, `pages/admin`으로 최상위를 나누고, 그 아래 화면별 폴더. 공통 요소만 `components/common`, `stores`, `api`로 분리한다.
- **API 호출은 컴포넌트에 직접 넣지 않는다**: `api/` 계층의 함수(예: `getCategories()`, `createMenu()`)를 통해서만 호출하고, 컴포넌트는 React Query 훅 또는 스토어 액션을 통해서만 그 함수를 사용한다.
- **서버 응답 타입은 Backend.md DTO와 1:1 대응하는 TypeScript 인터페이스로 `types/`에 고정한다.** 필드명은 Backend.md의 camelCase를 그대로 사용한다(백엔드가 이미 camelCase로 내려줌).
- **정렬 규칙과 버튼 배치 규칙(5장, 6장)은 예외 없이 모든 화면에 적용**하며, 이를 재구현하지 않도록 `<DataTable>` 공통 컴포넌트가 컬럼별 `align`을 강제한다.

---

## 2. 라우팅 구조

```text
/                              KioskLayout
  (index)                      MenuListPage              — 인증 불필요
/cart                          KioskLayout > CartPage     — 인증 불필요
/order/complete                KioskLayout > OrderCompletePage — 인증 불필요

/admin/login                   AdminLoginPage             — 인증 불필요(레이아웃 없음, 단독 화면)

/admin                         PrivateRoute > AdminLayout — 이하 전부 JWT 인증 필요
  /admin/dashboard             DashboardPage
  /admin/categories            CategoriesPage
  /admin/menus                 MenusPage
  /admin/menus/new             MenuFormPage (단품 등록)
  /admin/menus/:id/edit        MenuFormPage (단품 수정)
  /admin/menus/sets/new        SetMenuWizardPage (2단계)
  /admin/inventory             InventoryPage
  /admin/orders                OrdersPage
  /admin/orders/:id            OrderDetailPage
  /admin/sales                 SalesPage
  /admin/audit-logs            AuditLogsPage
```

**라우터 정의 (`src/routes/router.tsx`)**

```tsx
import { createBrowserRouter } from "react-router-dom";
import { KioskLayout } from "@/layouts/KioskLayout";
import { AdminLayout } from "@/layouts/AdminLayout";
import { PrivateRoute } from "@/routes/PrivateRoute";
import { MenuListPage } from "@/pages/kiosk/MenuListPage";
import { CartPage } from "@/pages/kiosk/CartPage";
import { OrderCompletePage } from "@/pages/kiosk/OrderCompletePage";
import { AdminLoginPage } from "@/pages/admin/AdminLoginPage";
import { DashboardPage } from "@/pages/admin/DashboardPage";
// ...나머지 admin 페이지 import

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
```

**`<PrivateRoute>` (`src/routes/PrivateRoute.tsx`)**

```tsx
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "@/stores/authStore";

export function PrivateRoute() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? <Outlet /> : <Navigate to="/admin/login" replace />;
}
```

---

## 3. 전역 상태 설계

Zustand 스토어 3개. 각 스토어는 `create<T>()`로 독립 생성하고, `persist` 미들웨어는 `authStore`에만 적용한다(장바구니는 서버가 진실 공급원이므로 클라이언트에 영구 저장하지 않고 `sessionId`만 `sessionStorage`에 보관 — 6.1절 세션 관리 참조).

### 3.1 `cartStore`

```ts
interface CartLineItem {
  cartItemId: number;
  menuId: number;
  menuName: string;
  price: number;
  imageUrl: string | null;
  quantity: number;
  subtotal: number;
  isSoldOut: boolean;
}

interface CartState {
  sessionId: string | null;
  cartItems: CartLineItem[];
  totalPrice: number;

  setSessionId: (id: string) => void;
  setCart: (cartId: number | null, items: CartLineItem[], totalPrice: number) => void;
  addItem: (item: CartLineItem) => void;          // 낙관적 업데이트용 로컬 반영
  removeItem: (cartItemId: number) => void;
  updateQuantity: (cartItemId: number, quantity: number) => void;
  clearCart: () => void;
}
```

- `totalQuantity`는 상태로 따로 두지 않고 `cartItems.reduce((sum, i) => sum + i.quantity, 0)`로 파생시켜 셀렉터에서 계산한다(이중 소스 방지).
- `addItem`/`updateQuantity`/`removeItem`은 **API 호출 성공 이후 서버 응답으로 갱신하는 것이 원칙**이나, 4.3절(장바구니 화면) 수량 변경만 예외적으로 낙관적 업데이트를 적용한다(사양 명시 사항).

### 3.2 `authStore`

```ts
interface AuthState {
  adminUsername: string | null;
  accessToken: string | null;
  isAuthenticated: boolean;

  login: (username: string, token: string) => void;
  logout: () => void;
}
```

- `persist` 미들웨어로 `localStorage` 키 `kiosk-admin-auth`에 저장한다(새로고침 시 로그인 유지). Axios 인스턴스는 이 스토어를 구독하지 않고, **요청 시점에 `useAuthStore.getState().accessToken`을 직접 읽는다**(React 컴포넌트 바깥인 인터셉터에서 훅을 쓸 수 없으므로 Zustand의 non-hook 접근 API 사용).

### 3.3 `uiStore`

```ts
interface ToastMessage {
  id: string;
  type: "success" | "error" | "warning";
  message: string;
}

interface UiState {
  isLoading: boolean;
  toasts: ToastMessage[];

  setLoading: (loading: boolean) => void;
  showToast: (type: ToastMessage["type"], message: string) => void;
  hideToast: (id: string) => void;
}
```

- `showToast`는 내부적으로 `crypto.randomUUID()`로 id를 생성해 `toasts` 배열에 push하고, `<Toast>` 컴포넌트가 3초 뒤 자동으로 `hideToast`를 호출한다(9장 참조).

---

## 4. API 연동 규칙

### 4.1 Axios 인스턴스 (`src/api/client.ts`)

```ts
import axios from "axios";
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

  if (isAdminRequest) {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } else {
    const sessionId = useCartStore.getState().sessionId;
    if (sessionId) {
      config.headers["X-Session-Id"] = sessionId;
    }
  }
  return config;
});

// ── 응답 인터셉터: 공통 성공/에러 언랩 + 세션 발급 + 401 처리 ────────────
apiClient.interceptors.response.use(
  (response) => {
    // 장바구니 담기 응답에 새 X-Session-Id가 오면 저장(3.3.2 최초 세션 발급)
    const issuedSessionId = response.headers["x-session-id"];
    if (issuedSessionId) {
      useCartStore.getState().setSessionId(issuedSessionId);
      sessionStorage.setItem("kiosk-session-id", issuedSessionId);
    }

    const body = response.data;
    if (body && body.success === false) {
      useUiStore.getState().showToast("error", body.message ?? "요청 처리 중 오류가 발생했습니다.");
      return Promise.reject(new Error(body.message));
    }
    return body?.data !== undefined ? { ...response, data: body.data } : response;
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      useUiStore.getState().showToast("warning", "세션이 만료되었습니다. 다시 로그인해주세요.");
      window.location.href = "/admin/login";
      return Promise.reject(error);
    }

    const message = error.response?.data?.message ?? "네트워크 오류가 발생했습니다.";
    useUiStore.getState().showToast("error", message);
    return Promise.reject(error);
  }
);
```

**규칙 요약**

| 규칙 | 구현 위치 |
|---|---|
| 키오스크 요청에 `X-Session-Id` 자동 첨부 | 요청 인터셉터, `url`이 `/admin`으로 시작하지 않으면 적용 |
| 관리자 요청에 `Authorization: Bearer {token}` 자동 첨부 | 요청 인터셉터, `url`이 `/admin`으로 시작하면 적용(`/admin/auth/login` 포함 — 로그인 자체는 토큰이 없으므로 헤더 미첨부, 문제 없음) |
| 401 시 `/admin/login` 리다이렉트 | 응답 인터셉터 에러 콜백 |
| `success: false` 시 토스트 표시 | 응답 인터셉터 성공 콜백(HTTP 200이지만 `success:false`인 케이스 포함) |

### 4.2 도메인별 API 함수 예시 (`src/api/menus.ts`)

```ts
import { apiClient } from "./client";
import type { CategoryMenuResponse, MenuDetailResponse, PageResponse, AdminMenuListItem } from "@/types";

export const getCategoryMenus = (categoryId: number) =>
  apiClient.get<CategoryMenuResponse[]>(`/categories/${categoryId}/menus`).then((r) => r.data);

export const getMenuDetail = (menuId: number) =>
  apiClient.get<MenuDetailResponse>(`/menus/${menuId}`).then((r) => r.data);

export const searchMenus = (keyword: string) =>
  apiClient.get(`/menus/search`, { params: { keyword } }).then((r) => r.data);

export const getAdminMenus = (params: { categoryId?: number; isSet?: boolean; isActive?: boolean; page: number; size: number }) =>
  apiClient.get<PageResponse<AdminMenuListItem>>("/admin/menus", { params }).then((r) => r.data);
```

React Query 훅은 이 함수를 감싸는 형태로 각 페이지 옆에 `useCategoryMenus.ts`처럼 둔다:

```ts
export function useCategoryMenus(categoryId: number | null) {
  return useQuery({
    queryKey: ["category-menus", categoryId],
    queryFn: () => getCategoryMenus(categoryId as number),
    enabled: categoryId !== null,
    staleTime: 30_000,
  });
}
```

---

## 5. 정렬 규칙 (Alignment Rules)

Design.md 6장과 동일한 원칙을 프론트엔드 구현 레벨로 강제한다. `<DataTable>`(9장)의 컬럼 정의에서 `align`을 필수 prop으로 받아, 헤더(`<th>`)와 셀(`<td>`)에 동일하게 적용한다.

| 데이터 유형 | 정렬 | 적용 예시 |
|---|---|---|
| 금액, 수량, 건수 등 자릿수가 다른 수치 | **우측(`right`)** | 매출액, 주문 금액, 재고 수량, "N건" |
| 순번, ID, 코드 등 자릿수가 일정한 일련번호 | **가운데(`center`)** | No., 주문번호, 메뉴 ID, 로그 ID |
| 단순 텍스트, 이름, 설명 | **좌측(`left`)** | 메뉴명, 카테고리명, 관리자명 |
| 상태 뱃지, 아이콘만 있는 셀 | **가운데(`center`)** | 활성/비활성, 품절 뱃지 |

### 5.1 화면별 컬럼 정의 예시

```ts
// admin/menus 목록
const columns: DataTableColumn<AdminMenuListItem>[] = [
  { key: "no",       label: "No.",   align: "center", render: (_, i) => i + 1 },
  { key: "name",     label: "메뉴명", align: "left" },
  { key: "category", label: "카테고리", align: "left" },
  { key: "price",    label: "가격",   align: "right", render: (row) => formatCurrency(row.price) },
  { key: "isSet",    label: "구분",   align: "center", render: (row) => (row.isSet ? "세트" : "단품") },
  { key: "quantity", label: "재고",   align: "right" },
  { key: "isActive", label: "활성",   align: "center", render: (row) => <StatusBadge active={row.isActive} /> },
  { key: "actions",  label: "",      align: "right", render: (row) => <RowActions row={row} /> },
];
```

다른 11개 화면의 테이블 컬럼 정렬은 7장 화면별 명세에 각각 명시되어 있다(각 표에 정렬 열 포함).

---

## 6. 버튼 배치 규칙 (Button Placement Rules)

1. **추가(등록) 버튼**: 섹션/테이블 영역의 **우측 상단**.
2. **수정 버튼**: 해당 행 또는 상세 영역의 **우측**.
3. **삭제 버튼**: 수정 버튼의 **오른쪽**(같은 행) 또는 **아래**(상세 화면 별도 액션 그룹).
4. **폼 제출(저장/확인)**: 폼 영역의 **우측 하단**.
5. **취소/닫기**: 제출 버튼의 **왼쪽**.
6. 버튼 그룹 순서: `[취소] [저장/확인(Primary)]` — 오른쪽 끝이 항상 Primary.

이 규칙은 `<ConfirmDialog>`, 모든 모달 폼, `<DataTable>`의 액션 컬럼에 공통 적용되며, 개별 컴포넌트가 임의로 순서를 바꾸지 않도록 8장의 `<PageHeader actions={...}>` 패턴을 사용해 "등록 버튼은 항상 헤더 우측"을 구조적으로 강제한다.

---

## 7. 화면별 기능 명세

### 7.1 [키오스크] 메뉴 목록 화면 (`/`)

**진입 조건**: 인증 불필요, 앱 최초 진입 시 바로 렌더링.

**컴포넌트 트리**
```
MenuListPage
├─ SearchInput (디바운스 300ms)
├─ CategoryTabs (검색 중 disabled)
├─ MenuGrid
│   └─ MenuCard × N
├─ MenuDetailModal (카드 클릭 시 오픈, 라우트 변경 없음)
└─ CartButton (하단 sticky)
```

**상태/데이터 흐름**
- `selectedCategoryId`: `useState`, 카테고리 목록 로드 후 첫 번째 항목으로 초기화.
- `searchKeyword`: `useState` + `useDebounce(keyword, 300)`.
- 카테고리 목록: `useQuery(["categories"], getCategories)`.
- 선택된 카테고리 메뉴: `useCategoryMenus(selectedCategoryId)` — `enabled: !isSearching`.
- 검색 결과: `useQuery(["menu-search", debouncedKeyword], () => searchMenus(debouncedKeyword), { enabled: debouncedKeyword.length > 0 })`.
- 검색어가 있으면 `CategoryTabs`는 `disabled` prop을 받아 클릭 불가 + 시각적으로 흐리게 처리.
- 검색 결과 0건: `<EmptyState icon="search" message="검색 결과가 없습니다" />`.

**기능별 처리**
| 기능 | 처리 |
|---|---|
| 카테고리 탭 클릭 | `setSelectedCategoryId(id)`, React Query가 캐시 없으면 재요청·있으면 즉시 표시 |
| 메뉴 카드 클릭 | 품절이면 무시(`pointer-events: none` + `aria-disabled`), 아니면 `setSelectedMenuId(menu.id)`로 `MenuDetailModal` 오픈 |
| 장바구니 버튼 클릭 | `cartItems.length === 0`이면 disabled, 아니면 `navigate("/cart")` |

**세션 관리**: 6.1절 참조.

---

### 7.2 [키오스크] 메뉴 상세 팝업 (`<MenuDetailModal>`)

**트리거**: 메뉴 카드 클릭. 라우트는 바뀌지 않고 `MenuListPage`가 로컬 상태(`selectedMenuId`)로 모달을 제어한다.

**구성**: 이미지(상단, `loading="eager"` — 모달은 사용자가 직접 연 콘텐츠라 lazy 불필요) → 메뉴명/가격 → 설명 → (세트인 경우) 구성 단품 목록 → `<QuantityStepper min={1} />` → `[취소] [장바구니 담기]`.

**장바구니 담기 인터랙션(핵심 애니메이션)**: 8장에서 단계별 코드로 상세 기술.

**실패 처리**: `POST /api/carts/items`가 400(`MENU_SOLD_OUT`, `MENU_INACTIVE`)을 반환하면 모달은 닫지 않고 유지한 채 에러 토스트만 표시(모달이 열려 있는 동안 다른 손님이 마지막 재고를 채간 경쟁 상황 대응).

**닫기**: 오버레이 클릭 또는 `[취소]` → `setSelectedMenuId(null)`.

---

### 7.3 [키오스크] 장바구니 화면 (`/cart`)

**컴포넌트 트리**
```
CartPage
├─ CartHeader ("장바구니 비우기" 버튼 — 우측 상단)
├─ CartItemList
│   └─ CartItemRow × N (이미지/메뉴명/단가/QuantityStepper/소계(우측 정렬)/삭제 버튼(행 우측))
├─ OrderSummary (총 금액, 우측 정렬)
└─ PrimaryActionBar ("주문하기" 버튼, sticky bottom)
```

**데이터 로드**: 진입 시 `GET /api/carts`(`X-Session-Id` 자동 첨부) → `cartStore.setCart(...)`.

**수량 변경(낙관적 업데이트)**
```ts
async function handleQuantityChange(cartItemId: number, nextQty: number) {
  const prevItems = cartStore.cartItems;
  cartStore.updateQuantity(cartItemId, nextQty); // 1) 즉시 로컬 반영
  try {
    await patchCartItem(cartItemId, { quantity: nextQty }); // 2) 서버 반영
  } catch {
    cartStore.setCart(null, prevItems, recalcTotal(prevItems)); // 3) 실패 시 롤백
  }
}
```

**항목 삭제**: 확인 다이얼로그 없이 즉시 `DELETE /api/carts/items/{id}` 호출 → 성공 시 로컬에서도 제거(사양에 "확인 없이 즉시 삭제"로 명시됨 — `<ConfirmDialog>` 미사용).

**장바구니 비우기**: 우측 상단 버튼 → `<ConfirmDialog>` 오픈 → 확인 시 `DELETE /api/carts` → `cartStore.clearCart()`.

**주문하기**
1. 버튼 클릭 → `uiStore.setLoading(true)`, 버튼 자체도 `disabled` + 스피너로 전환.
2. `POST /api/orders` 호출.
3. 성공(201): `cartStore.clearCart()` → `navigate("/order/complete", { state: orderResponse })`.
4. 실패(400 `MENU_UNAVAILABLE`/`INSUFFICIENT_STOCK`): 에러 토스트에 서버 메시지(문제 메뉴명 포함)를 그대로 노출 — 별도 파싱 없이 `error.message` 표시.

**빈 장바구니**: `cartItems.length === 0`이면 목록/요약 대신 `<EmptyState icon="cart" message="장바구니가 비어있습니다" actionLabel="메뉴로 돌아가기" onAction={() => navigate("/")} />`.

---

### 7.4 [키오스크] 주문 완료 화면 (`/order/complete`)

- 진입 시 `location.state`로 전달된 주문 생성 응답(`{ orderNumber, totalPrice, items, createdAt }`)을 사용한다. `state`가 없으면(새로고침 등으로 유실) `navigate("/", { replace: true })`로 즉시 이탈시킨다(뒤로가기로 이 화면에 직접 진입하는 것을 방지).
- 주문번호는 Design.md Display 타이포(32px/700)로 중앙 정렬.
- `[처음으로]` 클릭 시: `sessionStorage.removeItem("kiosk-session-id")` + `cartStore.setSessionId(null)` 후 `navigate("/", { replace: true })` — 다음 고객을 위한 세션 초기화.

---

### 7.5 [관리자] 로그인 화면 (`/admin/login`)

- `useForm` (React Hook Form) + Zod 스키마(`username`, `password` 모두 필수).
- 진입 시 `useEffect`로 `isAuthenticated`가 이미 true면 `navigate("/admin/dashboard", { replace: true })`.
- 제출: `POST /api/admin/auth/login` → 성공 시 `authStore.login(username, accessToken)` → `navigate("/admin/dashboard")`.
- 실패(401 `INVALID_CREDENTIALS`, 403 `ACCOUNT_INACTIVE`): 폼 하단에 인라인 에러 메시지 표시(토스트가 아니라 폼 컨텍스트 내 표시 — 로그인 실패는 "그 화면에 머물러야 하는" 대표 케이스).

---

### 7.6 [관리자] 대시보드 (`/admin/dashboard`)

- 진입 시 `useQuery(["dashboard"], getDashboard)` 1회 호출(폴링 없음, `refetchOnWindowFocus: true`로 탭 복귀 시에는 자동 갱신).
- `<SummaryCard>` × 4: 오늘 매출/오늘 주문 건수/이번 달 매출/품절 메뉴 수 — 금액·건수 모두 값 영역 우측 정렬(라벨은 좌측).
- 최근 주문 5건 테이블 컬럼: `No.(center) / 주문번호(center) / 금액(right) / 상태(center, StatusBadge) / 일시(left)`.
- 로딩 중: `<SummaryCard skeleton />` 4개 + 테이블 skeleton row 5개.

---

### 7.7 [관리자] 카테고리 관리 (`/admin/categories`)

- 목록: `useQuery(["admin-categories"], getAdminCategories)`.
- 컬럼: `No.(center) / 카테고리명(left) / 노출순서(center) / 활성여부(center, StatusBadge) / 등록일(left) / 액션(right)`.
- `[카테고리 추가]`(우측 상단) 및 각 행의 `[수정]` 클릭 → 동일한 `<CategoryFormModal mode="create"|"edit">` 오픈(모달 폼, 인라인 편집 아님).
- 모달 제출 성공 시 `queryClient.invalidateQueries(["admin-categories"])`로 목록 갱신 + `uiStore.showToast("success", "저장되었습니다")`.
- 삭제: `[삭제]` → `<ConfirmDialog title="카테고리를 삭제할까요?">` → 확인 시 `DELETE /api/admin/categories/{id}`.
  - 실패(409 `CATEGORY_HAS_MENUS`): 다이얼로그를 닫고 에러 토스트에 서버 메시지(소속 메뉴 수 포함)를 그대로 노출.

---

### 7.8 [관리자] 메뉴 관리 (`/admin/menus`)

**목록 화면**
- 좌측 상단 필터 툴바: 카테고리 Select, 단품/세트 Select, 활성여부 Select — 값 변경 시 React Query `queryKey`에 필터를 포함시켜 자동 재요청(`["admin-menus", filters, page]`).
- 우측 상단: `[단품 등록]`(Secondary) `[세트 등록]`(Primary) — Primary가 가장 오른쪽(6장 규칙).
- 테이블 컬럼: `No.(center) / 메뉴명(left) / 카테고리(left) / 가격(right) / 구분(center) / 재고(right) / 활성(center) / 액션(right, 수정·삭제)`.
- 서버 사이드 페이지네이션(`page`, `size` 쿼리 파라미터, 11장 참조).
- `[수정]` → `navigate(`/admin/menus/${id}/edit`)`. `[삭제]` → `<ConfirmDialog>` → `DELETE /api/admin/menus/{id}` → 실패(409 `MENU_IN_USE_AS_SET_COMPONENT`) 시 사용 중인 세트 메뉴명을 토스트에 노출.

**단품 등록/수정 (`/admin/menus/new`, `/admin/menus/:id/edit`)**
- 동일한 `<MenuForm mode="create"|"edit">` 컴포넌트 재사용. `mode="edit"`이면 `useQuery(["admin-menu", id], () => getAdminMenuDetail(id))`로 초기값 채움.
- 필드: 카테고리(Select), 메뉴명, 설명(Textarea), 가격(number), 수량(number, `mode==="create"`일 때만 노출 — Backend.md 규칙상 수정 API는 quantity를 다루지 않음), 이미지 URL.
- 수량 필드: `min={0}`, 정수만(`step={1}`), Zod `.int().min(0)`.
- `[취소]`(좌, Ghost) `[저장]`(우, Primary) — 폼 우측 하단, 6장 규칙.
- 제출 성공 시 `navigate("/admin/menus")` + 성공 토스트.

---

### 7.9 [관리자] 세트 메뉴 등록 — 2단계 Wizard (`/admin/menus/sets/new`)

`<StepWizard currentStep={step} steps={["기본 정보", "구성품 추가"]} />`로 상단에 진행 단계 표시.

**Step 1 — 기본 정보**
- `<MenuForm mode="create" variant="set">`과 동일한 필드(카테고리/메뉴명/설명/가격/수량/이미지 URL).
- `[다음]` 클릭 → `POST /api/admin/menus/sets` 호출 → 성공 시 응답의 `id`를 로컬 상태 `setMenuId`에 저장하고 `setStep(2)`로 전환(라우트는 유지, 같은 페이지 내 단계 전환).
- **주의**: Step 1 API가 이미 세트 메뉴 레코드를 생성하므로, 사용자가 Step 2에서 이탈하면 "구성품 없는 빈 세트"가 남는다. 이탈 시도(브라우저 뒤로가기/탭 닫기) 감지 시 `beforeunload` + `useBlocker`로 "구성품을 추가하지 않고 나가면 빈 세트로 등록됩니다" 확인창을 띄운다.

**Step 2 — 구성 단품 추가**
- 상단: 등록된 단품 메뉴 검색용 `<Select>`(비동기 옵션, `GET /api/admin/menus?isSet=false&isActive=true` 결과) + 수량 입력 + `[구성품 추가]` 버튼(우측).
- `[구성품 추가]` → `POST /api/admin/menus/sets/{setMenuId}/components` → 성공 시 하단 목록에 즉시 추가(React Query invalidate).
- 구성품 목록: 각 행에 `[제거]`(우측) → `DELETE /api/admin/menus/sets/{setMenuId}/components/{componentMenuId}`.
- `[세트 등록 완료]` 버튼: 구성품이 1개 이상일 때만 활성화(`components.length > 0`). 클릭 시 이미 모든 구성품이 서버에 반영된 상태이므로 추가 API 호출 없이 바로 `navigate("/admin/menus")` + 성공 토스트.

---

### 7.10 [관리자] 재고 관리 (`/admin/inventory`)

- 상단: 품절 필터 토글 버튼(`전체`/`품절만`/`판매 가능만` — `<SegmentedControl>`).
- 컬럼: `No.(center) / 메뉴명(left) / 카테고리(left) / 현재 재고(right) / 상태(center) / 액션(right)`.
- `[수정]` 클릭 시 해당 행이 인라인 편집 모드로 전환(수량 input + `[저장]`/`[취소]` 버튼이 액션 컬럼 자리에 대체 렌더링) — 별도 모달을 띄우지 않는다(빈번한 반복 작업이므로 마찰 최소화, Design.md 9.9 UX 주의사항과 동일한 이유).
- 저장: `PATCH /api/admin/inventory/{menuId}` → 성공 시 해당 행만 갱신 + `uiStore.showToast("success", "저장되었습니다")`, 편집 모드 종료.
- 수량 입력 검증: `min={0}`, 정수.

---

### 7.11 [관리자] 주문 내역 (`/admin/orders`, `/admin/orders/:id`)

**목록**
- 상태 필터 탭(`<SegmentedControl>`): 전체 / 완료(`COMPLETED`) / 취소(`CANCELLED`) — 선택값을 쿼리 파라미터 `status`로 반영.
- 컬럼: `No.(center) / 주문번호(center) / 총액(right) / 상태(center) / 주문일시(left) / 액션(right, 상세만 — 목록에서는 취소 버튼 없음)`.
- `[상세 보기]` → `navigate(`/admin/orders/${id}`)`.

**상세**
- 상단 정보 카드: 주문번호/상태/총액/주문일시(4.1절 Design.md `.form-grid` 패턴 재사용).
- 주문 항목 테이블: `No.(center) / 메뉴명(left) / 단가(right) / 수량(center) / 소계(right)`.
- `[주문 취소]`: 우측 상단, `status === "CANCELLED"`면 `disabled` + 툴팁 "이미 취소된 주문입니다". 활성 상태면 클릭 시 `<ConfirmDialog danger>` → 확인 시 `PATCH /api/admin/orders/{id}/cancel` → 성공 시 상태를 즉시 `CANCELLED`로 갱신(React Query invalidate).

---

### 7.12 [관리자] 매출 조회 (`/admin/sales`)

- 상단 탭(`<SegmentedControl>`): 일별/월별/연도별. 탭에 따라 `<DatePicker mode="day"|"month"|"year">`로 전환.
- `[조회]` 클릭 시에만 API 호출(탭/날짜 변경 즉시 자동조회 아님 — 사양에 "조회 버튼 클릭 시 호출"로 명시).
- 공통: 상단 `<SalesSummary totalSales totalOrderCount />`(금액·건수 모두 우측 정렬 값).
- 일별: 주문 목록 테이블 `주문번호(center) / 금액(right) / 주문시각(left)`.
- 월별: 일자별 테이블 `일자(center) / 일 매출(right) / 일 주문건수(right)` + 하단 합계 행(`<DataTable summaryRow>`, 배경 `--color-primary-light`, 폰트 굵게).
- 연도별: 월별 테이블 `월(center) / 월 매출(right) / 월 주문건수(right)` + 동일한 합계 행 패턴.
- 합계 행은 `<DataTable>`의 `footerRow` prop으로 전달해 `<tfoot>`에 렌더링한다(9장 `<DataTable>` 명세 참조).

---

### 7.13 [관리자] 감사 로그 (`/admin/audit-logs`)

- 필터: 액션 코드 `<Select>`(Backend.md 5장 12개 액션 코드 하드코딩 옵션) + 기간 `<DatePicker range>`.
- 컬럼: `No.(center) / 액션코드(center, code-style badge) / 대상타입(center) / 대상ID(center) / IP(left) / 일시(left) / 상세(right)`.
- 행 클릭 또는 `[상세]` 버튼 → `<AuditLogDetailModal logId={id}>` 오픈, `GET /api/admin/audit-logs/{id}` 호출.
- 모달 내부: `before_value`/`after_value`를 `<pre><code>` 블록(JetBrains Mono, Design.md `.code-box` 스타일)으로 나란히 표시. `null`이면 "—" 로 표시.

---

## 8. 핵심 애니메이션 명세 — 장바구니 담기 플라잉 이미지

**목표**: 메뉴 상세 팝업에서 담기 성공 시, 팝업 이미지가 화면 위에서 하단 장바구니 버튼으로 날아가듯 축소 이동한 뒤 장바구니 버튼이 펄스로 반응한다.

### 8.1 왜 별도 라이브러리 없이 구현하는가

애니메이션이 이 프로젝트 전체에서 이 1건뿐이라, Framer Motion 같은 범용 애니메이션 라이브러리를 추가하면 번들 크기 대비 이득이 적다. `position: fixed` + CSS `transition` + `getBoundingClientRect()` 좌표 계산만으로 충분히 구현 가능하다.

### 8.2 `<FlyingImage>` 컴포넌트

```tsx
// src/components/kiosk/FlyingImage.tsx
import { useEffect, useRef, useState } from "react";
import styles from "./FlyingImage.module.css";

interface FlyingImageProps {
  src: string;
  startRect: DOMRect;   // 팝업 이미지의 시작 위치/크기
  endRect: DOMRect;     // 장바구니 버튼의 목표 위치/크기
  onComplete: () => void;
}

export function FlyingImage({ src, startRect, endRect, onComplete }: FlyingImageProps) {
  const imgRef = useRef<HTMLImageElement>(null);
  const [isFlying, setIsFlying] = useState(false);

  useEffect(() => {
    // 다음 프레임에 목표 위치로 전환시켜야 CSS transition이 발동한다(같은 프레임에서
    // 시작값과 종료값을 동시에 주면 브라우저가 transition을 생략한다).
    const raf = requestAnimationFrame(() => setIsFlying(true));
    return () => cancelAnimationFrame(raf);
  }, []);

  const handleTransitionEnd = () => onComplete();

  const targetX = endRect.left + endRect.width / 2 - startRect.width / 2;
  const targetY = endRect.top + endRect.height / 2 - startRect.height / 2;

  return (
    <img
      ref={imgRef}
      src={src}
      className={styles.flyingImage}
      style={{
        left: startRect.left,
        top: startRect.top,
        width: startRect.width,
        height: startRect.height,
        transform: isFlying
          ? `translate(${targetX - startRect.left}px, ${targetY - startRect.top}px) scale(0.15)`
          : "translate(0, 0) scale(1)",
      }}
      onTransitionEnd={handleTransitionEnd}
    />
  );
}
```

```css
/* FlyingImage.module.css */
.flyingImage {
  position: fixed;
  z-index: 9999;
  border-radius: var(--radius-lg);
  object-fit: cover;
  pointer-events: none;
  transition: transform 600ms cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 600ms ease;
  will-change: transform;
}
```

- 포물선처럼 "느껴지게" 하려는 목적이면 `transform`에 중간 경유점을 추가하는 대신, `cubic-bezier(0.25, 0.46, 0.45, 0.94)`("ease-out-quad" 계열) 하나로 시작은 빠르고 끝은 느려지는 곡선을 만든다 — 실제 포물선 궤적(2점 베지어 곡선 경로)까지 구현하려면 CSS `offset-path`를 쓸 수 있으나, 이 프로젝트 범위에서는 과도한 복잡도이므로 직선 + easing으로 "떨어지는" 느낌만 낸다.

### 8.3 `MenuDetailModal`에서의 호출 흐름

```tsx
function MenuDetailModal({ menu, onClose }: MenuDetailModalProps) {
  const modalImageRef = useRef<HTMLImageElement>(null);
  const [flying, setFlying] = useState<{ src: string; startRect: DOMRect } | null>(null);
  const cartButtonRef = useCartButtonRect(); // CartButton이 자신의 DOMRect를 전역에 등록해두는 훅(8.4 참조)

  async function handleAddToCart(quantity: number) {
    try {
      await addCartItem({ menuId: menu.id, quantity }); // 1) 서버 호출 먼저
    } catch {
      return; // 실패 시 여기서 종료 — 모달 유지, 토스트는 인터셉터가 이미 표시함
    }

    // 2) 성공한 뒤에만 시작 좌표를 기록하고 모달을 닫는다
    const startRect = modalImageRef.current!.getBoundingClientRect();
    setFlying({ src: menu.imageUrl, startRect });
    onClose(); // 팝업 즉시 닫기
  }

  return (
    <>
      <Modal onClose={onClose}>
        <img ref={modalImageRef} src={menu.imageUrl} alt={menu.name} />
        {/* ...나머지 상세 내용... */}
      </Modal>

      {flying &&
        createPortal(
          <FlyingImage
            src={flying.src}
            startRect={flying.startRect}
            endRect={cartButtonRef.current!}
            onComplete={() => {
              setFlying(null);
              triggerCartButtonPulse(); // 8.4 참조 — 펄스 + 수량/금액 갱신
            }}
          />,
          document.body
        )}
    </>
  );
}
```

**단계 요약(사양 4단계와 1:1 대응)**
1. `[장바구니 담기]` 클릭 → `addCartItem` 호출.
2. 성공 시 `modalImageRef.getBoundingClientRect()`로 시작 위치 기록.
3. `onClose()`로 팝업 즉시 닫힘 → 동시에 `<FlyingImage>`를 `document.body`에 포탈로 렌더링(`position: fixed`이므로 모달 언마운트와 무관하게 화면에 남는다).
4. `onComplete`(transitionend) 콜백에서 `<FlyingImage>` 제거 + `<CartButton>` 펄스 애니메이션 트리거 + `cartStore` 수량/금액 갱신(서버 응답으로 이미 받은 최신 장바구니 항목을 `cartStore.addItem`으로 반영).

### 8.4 `<CartButton>`의 목표 좌표 등록과 펄스

```tsx
// src/components/kiosk/CartButton.tsx
export const cartButtonRectRef = { current: null as DOMRect | null };

export function CartButton() {
  const ref = useRef<HTMLButtonElement>(null);
  const [isPulsing, setIsPulsing] = useState(false);

  useLayoutEffect(() => {
    cartButtonRectRef.current = ref.current?.getBoundingClientRect() ?? null;
  });

  useEffect(() => {
    const handler = () => {
      setIsPulsing(true);
      setTimeout(() => setIsPulsing(false), 400);
    };
    cartButtonEvents.on("pulse", handler);
    return () => cartButtonEvents.off("pulse", handler);
  }, []);

  return (
    <button ref={ref} className={clsx(styles.cartButton, isPulsing && styles.pulse)}>
      {/* 총 수량 / 총 금액 */}
    </button>
  );
}

export function triggerCartButtonPulse() {
  cartButtonEvents.emit("pulse");
}
```

```css
@keyframes pulse {
  0%   { transform: scale(1); }
  30%  { transform: scale(1.08); }
  100% { transform: scale(1); }
}
.pulse { animation: pulse 400ms ease-out; }
```

- `cartButtonEvents`는 간단한 이벤트 이미터(예: `mitt` 라이브러리 1개 함수 또는 자체 구현 10줄)로, 전역 상태(Zustand)에 "펄스 트리거" 같은 일회성 UI 이벤트를 넣는 것은 과하므로 별도 이벤트 버스를 둔다.
- `cartButtonRectRef`는 모듈 스코프 변수로 좌표를 들고 있다가 `FlyingImage`의 `endRect`로 쓰인다. 리사이즈에 대응하려면 `useLayoutEffect` 의존성에 윈도우 리사이즈 리스너를 추가할 수 있으나, 키오스크는 해상도 고정(Design.md 5.1)이므로 생략 가능.

---

## 9. 공통 컴포넌트 명세

| 컴포넌트 | Props | 상태 | 사용 화면 | 동작 방식 |
|---|---|---|---|---|
| `<PrivateRoute>` | 없음(Outlet 렌더링) | 없음(스토어 구독만) | 모든 `/admin/*` (로그인 제외) | `authStore.isAuthenticated`가 false면 `<Navigate to="/admin/login">` |
| `<AdminLayout>` | 없음(`<Outlet>` 사용) | 없음 | 관리자 전 화면 | 사이드바(Design.md 8.8) + 상단 topbar + `<Outlet>` 콘텐츠 영역. 사이드바 활성 메뉴는 `useLocation().pathname` 기준으로 결정 |
| `<KioskLayout>` | 없음 | 없음 | 키오스크 전 화면 | 480px 고정 폭 컨테이너 + 헤더(뒤로가기/장바구니 아이콘은 각 페이지가 `<Outlet context>`로 커스터마이즈) |
| `<MenuCard>` | `menu: CategoryMenuResponse`, `onClick` | 없음 | 메뉴 목록 | 이미지는 `loading="lazy"`. `menu.isSoldOut`이면 반투명 오버레이 + "품절" 텍스트, `onClick` 무시 |
| `<MenuDetailModal>` | `menu: MenuDetailResponse`, `onClose`, `onAdded` | `quantity`, `flying` | 메뉴 목록 | 8장 참조 |
| `<FlyingImage>` | `src`, `startRect`, `endRect`, `onComplete` | `isFlying` | 메뉴 상세 팝업 | 8.2 참조. `document.body`에 포탈 렌더링, 완료 시 자동 언마운트 |
| `<CartButton>` | 없음(내부에서 `cartStore` 구독) | `isPulsing` | 메뉴 목록(하단 sticky) | `totalQuantity === 0`이면 disabled. 클릭 시 `/cart` 이동 |
| `<QuantityStepper>` | `value`, `onChange`, `min = 1`, `max?` | 없음(controlled) | 메뉴 상세 팝업, 장바구니 항목 | `-` 버튼은 `value <= min`이면 disabled |
| `<DataTable>` | `columns: DataTableColumn<T>[]`, `rows: T[]`, `rowKey`, `footerRow?`, `emptyMessage?`, `isLoading?` | 없음(순수 표시) | 관리자 전 목록 화면 | `columns[].align`로 `<th>`/`<td>` 정렬 강제(5장). `isLoading`이면 skeleton row, `rows.length === 0`이면 `emptyMessage` 1행 병합 표시. `footerRow`가 있으면 `<tfoot>`에 `--color-primary-light` 배경으로 렌더링 |
| `<SummaryCard>` | `label`, `value`, `icon?`, `accent?`, `isLoading?` | 없음 | 대시보드, 매출 조회 | `value`는 항상 우측 정렬 컨테이너 안에서 렌더링(라벨은 좌측) |
| `<StatusBadge>` | `status: "success" \| "danger" \| "warning" \| "neutral"`, `label`, `icon?` | 없음 | 전 화면 상태 표시 | Design.md 8.5 뱃지 스타일. 색상만이 아니라 아이콘 또는 텍스트를 항상 함께 표시(접근성) |
| `<ConfirmDialog>` | `title`, `description?`, `confirmLabel = "확인"`, `danger?`, `onConfirm`, `onCancel` | 없음 | 삭제/취소류 전체 | `[취소]`(좌, Ghost) `[확인]`(우, `danger`면 Danger 아니면 Primary) — 6장 버튼 규칙 그대로 |
| `<Toast>` | 없음(내부에서 `uiStore.toasts` 구독) | 없음 | 전역(레이아웃 최상단 1개만 마운트) | 각 토스트는 등장 200ms, 3초 후 자동 `hideToast`. 여러 개면 세로로 스택 |
| `<StepWizard>` | `steps: string[]`, `currentStep: number` | 없음 | 세트 메뉴 등록 | 원형 인디케이터 + 연결선, 완료된 단계는 체크 아이콘 |
| `<SalesSummary>` | `totalSales`, `orderCount`, `isLoading?` | 없음 | 매출 조회(일/월/연 공통) | `<SummaryCard>` 2개를 가로로 배치하는 래퍼 |
| `<EmptyState>` | `icon`, `message`, `actionLabel?`, `onAction?` | 없음 | 검색 결과 없음, 빈 장바구니, 빈 테이블 | 중앙 정렬 아이콘 + 메시지 + (선택) 액션 버튼 |

**`DataTableColumn<T>` 타입**

```ts
interface DataTableColumn<T> {
  key: string;
  label: string;
  align: "left" | "center" | "right";
  render?: (row: T, index: number) => React.ReactNode; // 없으면 row[key] 그대로 출력
}
```

---

## 10. 에러 처리 및 로딩 상태

| 케이스 | 처리 |
|---|---|
| API 호출 중 | 버튼/폼: `disabled` + 내부 스피너 아이콘으로 텍스트 대체. 목록류: `<DataTable isLoading>` skeleton row 5개 |
| 일반 에러(400/409/500) | Axios 응답 인터셉터가 자동으로 `uiStore.showToast("error", message)` 호출 — 개별 컴포넌트에서 추가로 `try/catch` 토스트를 띄우지 않는다(중복 방지). 컴포넌트는 실패 시 상태 롤백(낙관적 업데이트 케이스)이나 화면 전환 취소 같은 "부수 효과"만 처리 |
| 404 (존재하지 않는 리소스) | 상세 화면(`/admin/orders/:id` 등)에서 React Query `onError`로 감지 → 목록 대신 "리소스를 찾을 수 없습니다" 메시지 + `[목록으로 돌아가기]` 버튼을 페이지 콘텐츠 영역에 렌더링(토스트 아님 — 페이지 자체가 비어있는 상태이므로) |
| 401 | Axios 인터셉터가 `authStore.logout()` + `/admin/login` 리다이렉트 + "세션이 만료되었습니다" 토스트까지 전부 처리. 컴포넌트는 아무것도 하지 않아도 된다 |
| 로그인 실패(401/403) | 예외적으로 토스트가 아니라 로그인 폼 내부에 인라인 에러 표시(7.5절) — 응답 인터셉터의 공통 토스트 처리와 별개로, `AdminLoginPage`는 이 요청만 `apiClient` 대신 에러를 그대로 던지는 별도 처리를 하거나, 인터셉터의 토스트 표시 후에도 컴포넌트 레벨에서 추가로 폼 에러 상태를 세팅한다 |

---

## 11. 성능 고려사항

- **이미지 지연 로딩**: `<MenuCard>` 이미지에 `loading="lazy"` (모달처럼 사용자가 즉시 보려는 이미지는 `eager` 유지).
- **서버 상태 캐싱**: TanStack Query 사용. 카테고리/메뉴 목록은 `staleTime: 30_000`(30초) 정도로 설정해 탭 전환 시 매번 재요청하지 않게 한다. 관리자 목록(주문/감사로그 등)은 `staleTime: 0` + `refetchOnWindowFocus: true`로 최신성을 우선한다(운영 데이터 특성상 캐시보다 정확도가 중요).
- **검색 디바운스**: `useDebounce(keyword, 300)` 커스텀 훅으로 300ms 이내 연속 입력은 마지막 값만 반영.
- **서버사이드 페이지네이션**: `admin-menus`, `admin-orders`, `admin-audit-logs`, `admin-inventory` 전부 `page`/`size` 쿼리 파라미터를 React Query `queryKey`에 포함시켜, 페이지 이동 시 해당 페이지만 요청(클라이언트에 전체 데이터를 들고 있지 않음).

---

## 12. 폴더 구조

```text
src/
├─ api/
│   ├─ client.ts              # Axios 인스턴스 + 인터셉터
│   ├─ categories.ts
│   ├─ menus.ts
│   ├─ carts.ts
│   ├─ orders.ts
│   ├─ auth.ts
│   ├─ dashboard.ts
│   ├─ inventory.ts
│   ├─ sales.ts
│   └─ auditLogs.ts
├─ stores/
│   ├─ cartStore.ts
│   ├─ authStore.ts
│   └─ uiStore.ts
├─ routes/
│   ├─ router.tsx
│   └─ PrivateRoute.tsx
├─ layouts/
│   ├─ KioskLayout.tsx
│   └─ AdminLayout.tsx
├─ pages/
│   ├─ kiosk/
│   │   ├─ MenuListPage/
│   │   ├─ CartPage/
│   │   └─ OrderCompletePage/
│   └─ admin/
│       ├─ AdminLoginPage/
│       ├─ DashboardPage/
│       ├─ CategoriesPage/
│       ├─ MenusPage/
│       ├─ MenuFormPage/
│       ├─ SetMenuWizardPage/
│       ├─ InventoryPage/
│       ├─ OrdersPage/
│       ├─ OrderDetailPage/
│       ├─ SalesPage/
│       └─ AuditLogsPage/
├─ components/
│   ├─ common/
│   │   ├─ DataTable/
│   │   ├─ SummaryCard/
│   │   ├─ StatusBadge/
│   │   ├─ ConfirmDialog/
│   │   ├─ Toast/
│   │   ├─ StepWizard/
│   │   ├─ SalesSummary/
│   │   ├─ EmptyState/
│   │   ├─ SegmentedControl/
│   │   └─ PageHeader/          # 우측 상단 액션 버튼 슬롯을 강제하는 레이아웃 헬퍼(6장 규칙 구조화)
│   └─ kiosk/
│       ├─ MenuCard/
│       ├─ MenuDetailModal/
│       ├─ FlyingImage/
│       ├─ CartButton/
│       └─ QuantityStepper/
├─ hooks/
│   ├─ useDebounce.ts
│   └─ useCartButtonRect.ts
├─ types/
│   ├─ menu.ts
│   ├─ order.ts
│   ├─ category.ts
│   ├─ cart.ts
│   └─ admin.ts
├─ styles/
│   ├─ tokens.css              # Design.md 컬러/스페이싱/타이포 변수 이식
│   └─ globals.css
├─ utils/
│   ├─ formatCurrency.ts
│   └─ formatDate.ts
├─ App.tsx
└─ main.tsx
```

---

## 13. 개발 시 주의사항

### 13.1 세션 관리(키오스크)

- 앱 최초 진입 시 `sessionStorage.getItem("kiosk-session-id")`가 없으면 `cartStore.sessionId`도 `null` 상태로 둔다 — **먼저 세션을 발급받으려고 빈 장바구니 조회를 선제 호출하지 않는다.** Backend.md 3.3.1에 따르면 `GET /api/carts`는 세션이 없어도 400을 던지지 않지만(`X-Session-Id` 필수 헤더 검증에 걸림), 신규 세션은 **`POST /api/carts/items`(장바구니 담기)에서만 발급**되므로 실제 세션 발급은 "첫 담기 시도" 시점에 자연스럽게 이루어진다.
- 따라서 `MenuListPage`는 초기 진입 시 장바구니를 굳이 조회하지 않고, `cartStore.sessionId`가 있을 때만 `GET /api/carts`로 기존 장바구니를 복원한다.
- 응답 인터셉터가 `X-Session-Id` 응답 헤더를 감지해 자동으로 `sessionStorage` + `cartStore`에 반영하므로(4.1절), 컴포넌트 코드에서 직접 세션 발급을 처리할 필요가 없다.
- `/order/complete`에서 `[처음으로]`를 누르면 세션을 명시적으로 초기화한다(7.4절) — 백엔드가 주문 완료 시 장바구니를 이미 삭제하지만, 세션 ID 자체는 재사용 가능한 상태로 남아있어 다음 고객이 이전 고객의 세션을 이어받는 것을 방지하기 위함.

### 13.2 낙관적 업데이트 범위

- **장바구니 수량 변경만** 낙관적 업데이트를 적용한다(7.3절). 담기/삭제/주문 생성은 서버 응답을 기다린 뒤 상태를 반영한다 — 담기는 재고 체크가 필요해 실패 가능성이 높고, 삭제/주문은 되돌리기 어려운 액션이라 낙관적으로 처리할 실익이 적다.
- 낙관적 업데이트 롤백 시 반드시 "직전 스냅샷"(요청 전 `cartItems` 배열)을 캡처해뒀다가 그대로 복원한다. 서버에서 다시 조회(`refetch`)하는 방식은 네트워크 왕복이 한 번 더 필요해 UX상 느리다.

### 13.3 애니메이션 타이밍

- `<FlyingImage>`의 `transition-duration`(600ms)과 `<CartButton>` 펄스(400ms)는 **순차 실행**이지 동시 실행이 아니다 — `onTransitionEnd`(600ms 시점)에서 펄스를 트리거하므로 전체 인터랙션 체감 시간은 약 1초. 이 값을 줄이고 싶다면 두 애니메이션을 겹치게(`onComplete`를 `transitionend` 대신 `setTimeout(400ms)`로 앞당겨 호출) 만들 수 있으나, 이 경우 이미지가 도착하기 전에 버튼이 반응해 어색해 보이므로 권장하지 않는다.
- `requestAnimationFrame`으로 한 프레임 늦게 `isFlying`을 `true`로 바꾸는 부분(8.2절)을 생략하면 안 된다 — 마운트와 동시에 최종 `transform` 값을 넣으면 브라우저가 "처음부터 그 상태였다"고 판단해 transition 자체가 발동하지 않는다(React의 `useEffect`가 paint 이후 실행되는 것과 별개로, 초기 스타일과 목표 스타일이 같은 커밋에 반영되면 트랜지션이 생략되는 브라우저 표준 동작).
- 키오스크는 저사양 임베디드 브라우저에서 돌아갈 수 있으므로 `transform`/`opacity`만 애니메이션하고(`left`/`top`처럼 레이아웃을 유발하는 속성은 초기 위치 지정에만 사용, 애니메이션 자체는 `transform`으로) GPU 합성을 유도한다.

### 13.4 정렬/버튼 규칙 위반 방지

- 새 화면을 추가할 때 테이블을 직접 만들지 않고 반드시 `<DataTable>`을 사용한다 — `align`을 누락하면 타입 에러가 나도록 `DataTableColumn.align`을 필수 필드로 강제했다(9장).
- 등록 버튼을 임의 위치에 두지 않도록, 페이지 상단 영역은 항상 `<PageHeader title={...} actions={<Button/>} />` 패턴을 통해서만 구성한다(`actions`가 자동으로 헤더 우측에 렌더링됨).
