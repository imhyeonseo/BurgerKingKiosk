# 버거킹 키오스크 시스템 - 백엔드 기능 명세서 (Backend.md)

> 본 문서는 [DB.md](./DB.md)에 정의된 스키마(`categories`, `menus`, `set_menu_items`, `orders`, `order_items`, `carts`, `cart_items`, `admins`, `admin_audit_logs`, `order_number_sequence`)를 전제로 작성되었다. 컬럼명·FK·제약조건은 DB.md를 단일 진실 공급원(SSOT)으로 삼는다.

---

## 1. 개요

### 1.1 기술 스택

| 항목 | 내용 |
|---|---|
| 언어/프레임워크 | Java, Spring Boot |
| DB | MySQL 8.x |
| 아키텍처 | REST API (JSON) |
| 인증 | JWT (관리자 API만 해당) |
| Base URL | `/api` |
| 관리자 Base URL | `/api/admin` |

### 1.2 아키텍처 원칙

- **고객(키오스크) API는 완전히 무인증**이다. 신원 확인 대신 `X-Session-Id` 헤더로 장바구니·세션 상태를 구분한다.
- **관리자 API는 전부 JWT 인증**이 필요하다(로그인 API 자체는 예외).
- 계층은 `Controller → Service → Repository`로 고정하며, 비즈니스 로직과 트랜잭션 경계는 Service에만 둔다(자세한 내용은 [7. 레이어 구조 가이드](#7-레이어-구조-가이드)).
- 관리자의 모든 쓰기(POST/PUT/PATCH/DELETE) 요청은 성공 시 `admin_audit_logs`에 1건씩 기록된다(예외: 로그인 실패는 감사 로그 대상 아님).
- 재고(`menus.quantity`) 변경은 오직 재고 관리 API(`/api/admin/inventory`)를 통해서만 가능하다. 일반 메뉴 수정 API(`PUT /api/admin/menus/{menuId}`)는 `quantity`를 받지 않는다.
- 매출 집계는 항상 `orders.status = 'COMPLETED'`인 주문만 포함하고, `CANCELLED`는 제외한다.
- 주문 시 메뉴 옵션(커스터마이징)은 지원하지 않는다. 장바구니/주문 항목의 단위는 항상 "메뉴 1건"이다.

### 1.3 인증 방식

- 관리자 로그인(`POST /api/admin/auth/login`) 성공 시 JWT를 발급한다.
- JWT payload: `{ "sub": "{username}", "iat": ..., "exp": ... }` — `admins.username`(PK)을 subject로 사용한다(별도 admin PK가 없으므로 username 자체가 식별자).
- 관리자 API는 `Authorization: Bearer {accessToken}` 헤더가 없거나 유효하지 않으면 `401 UNAUTHORIZED`, 유효하지만 계정이 비활성/삭제 상태면 `403 FORBIDDEN`을 반환한다.
- 토큰 만료 시간(`exp`)은 짧게 유지(예: 2시간)하고, 로그아웃은 **클라이언트 측 토큰 폐기 방식**을 채택한다(근거는 [5-2. 로그아웃](#5-2-로그아웃) 참조).

### 1.4 공통 응답 포맷

**성공**

```json
{
  "success": true,
  "data": { },
  "message": null
}
```

**실패**

```json
{
  "success": false,
  "data": null,
  "message": "메뉴를 찾을 수 없습니다."
}
```

- 아래 모든 API의 "응답 Body 예시"는 편의상 `data` 필드의 내용만 표기한다. 실제 HTTP 응답은 위 포맷으로 감싸서 반환한다.
- HTTP 상태 코드는 의미에 맞춰 `200`(성공/조회/수정), `201`(생성), `204`(본문 없는 성공, 삭제 등), `400`(요청 검증 실패), `401`(인증 실패), `403`(권한 없음), `404`(리소스 없음), `409`(충돌, 중복, 상태 불일치), `500`(서버 오류)을 사용한다.

---

## 2. API 그룹 목록

| 그룹명 | Base URL | 인증 여부 | 설명 |
|---|---|---|---|
| 카테고리(고객) | `/api/categories` | 불필요 | 활성 카테고리 및 카테고리별 메뉴 조회 |
| 메뉴(고객) | `/api/menus` | 불필요 | 메뉴 상세 조회, 이름 검색 |
| 장바구니 | `/api/carts` | 불필요(세션 기반) | 세션 기반 장바구니 CRUD |
| 주문(고객) | `/api/orders` | 불필요(세션 기반) | 장바구니 → 주문 전환 |
| 관리자 인증 | `/api/admin/auth` | 로그인만 불필요, 이후 필요 | 로그인/로그아웃, JWT 발급 |
| 대시보드 | `/api/admin/dashboard` | 필요 | 매출/주문/재고 요약 |
| 관리자 카테고리 관리 | `/api/admin/categories` | 필요 | 카테고리 CRUD |
| 관리자 메뉴 관리 | `/api/admin/menus` | 필요 | 단품/세트 메뉴 CRUD, 세트 구성 관리 |
| 관리자 이미지 업로드 | `/api/admin/images` | 필요 | 메뉴 이미지 파일 업로드, URL 발급 |
| 이미지 조회 | `/api/images` | 불필요 | 업로드된 메뉴 이미지 파일 바이너리 조회 |
| 재고 관리 | `/api/admin/inventory` | 필요 | 메뉴 재고 수량 전용 관리 |
| 관리자 주문 관리 | `/api/admin/orders` | 필요 | 주문 조회, 취소 |
| 매출 조회 | `/api/admin/sales` | 필요 | 일/월/연 매출 집계 |
| 감사 로그 조회 | `/api/admin/audit-logs` | 필요 | 관리자 행위 이력 조회 |

---

## 3. API 상세 명세

### [키오스크 - 고객 시점] 인증 불필요

---

### 3.1 카테고리 API (`/api/categories`)

#### 3.1.1 `GET /api/categories` — 카테고리 전체 조회

**설명**: 키오스크 첫 화면에 노출할 활성 카테고리 목록을 노출 순서대로 반환한다.

**요청**: 헤더/파라미터 없음

**비즈니스 규칙**
- `is_active = true`인 카테고리만 반환한다.
- `display_order` 오름차순 정렬한다.

**응답 (200)**

```json
[
  { "id": 1, "name": "버거", "displayOrder": 1 },
  { "id": 2, "name": "사이드", "displayOrder": 2 },
  { "id": 3, "name": "음료", "displayOrder": 3 }
]
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 (활성 카테고리가 없으면 빈 배열) |
| 500 | 서버 오류 |

---

#### 3.1.2 `GET /api/categories/{categoryId}/menus` — 카테고리별 메뉴 전체 조회

**설명**: 특정 카테고리에 속한 판매 가능한 메뉴 목록을 반환한다. 품절 메뉴도 목록에는 포함하되 `isSoldOut` 플래그로 구분해 프론트엔드가 "품절" 배지를 표시할 수 있게 한다.

**경로 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | Y | 카테고리 ID |

**비즈니스 규칙**
- 대상 카테고리가 존재하지 않거나 `is_active = false`이면 `404 CATEGORY_NOT_FOUND`.
- 메뉴 필터: `is_active = true AND deleted_at IS NULL`. (`quantity = 0`인 품절 메뉴는 제외하지 않고 포함)
- `isSoldOut = (quantity == 0)`

**응답 (200)**

```json
[
  { "id": 10, "name": "와퍼", "price": 7100, "imageUrl": "/api/images/menu/whopper.jpg", "isSet": false, "isSoldOut": false },
  { "id": 16, "name": "와퍼 세트", "price": 9900, "imageUrl": "/api/images/menu/whopper-set.jpg", "isSet": true, "isSoldOut": true }
]
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 카테고리 없음/비활성 (`CATEGORY_NOT_FOUND`) |

---

### 3.2 메뉴 API (`/api/menus`)

#### 3.2.1 `GET /api/menus/{menuId}` — 메뉴 상세 조회

**설명**: 메뉴 상세 화면용 데이터를 반환한다. 세트 메뉴면 구성 단품 목록도 함께 내려준다.

**경로 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| menuId | Long | Y | 메뉴 ID |

**비즈니스 규칙**
- `is_active = true AND deleted_at IS NULL`인 메뉴만 조회 가능. 아니면 `404 MENU_NOT_FOUND`.
- `is_set = true`이면 `set_menu_items`를 조인해 `setComponents`를 채운다.

**응답 (200) — 단품**

```json
{
  "id": 10,
  "name": "와퍼",
  "description": "불맛 그릴에 구운 100% 순쇠고기 패티",
  "price": 7100,
  "imageUrl": "/api/images/menu/whopper.jpg",
  "isSet": false,
  "isSoldOut": false,
  "setComponents": null
}
```

**응답 (200) — 세트**

```json
{
  "id": 16,
  "name": "와퍼 세트",
  "description": "와퍼 + 어니언링 + 콜라",
  "price": 9900,
  "imageUrl": "/api/images/menu/whopper-set.jpg",
  "isSet": true,
  "isSoldOut": false,
  "setComponents": [
    { "id": 10, "name": "와퍼", "price": 7100, "quantity": 1 },
    { "id": 13, "name": "어니언링", "price": 3200, "quantity": 1 },
    { "id": 14, "name": "콜라", "price": 2200, "quantity": 1 }
  ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 메뉴 없음/비활성/삭제됨 (`MENU_NOT_FOUND`) |

---

#### 3.2.2 `GET /api/menus/search?keyword={keyword}` — 메뉴 이름 검색

**설명**: 메뉴명 부분 일치(대소문자 무관) 검색.

**쿼리 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| keyword | String | Y | 검색어 (공백/빈 문자열이면 `400 VALIDATION_ERROR`) |

**비즈니스 규칙**
- `is_active = true AND deleted_at IS NULL AND LOWER(name) LIKE LOWER('%keyword%')`
- MySQL 기본 콜레이션(`utf8mb4_unicode_ci`)은 대소문자 비구분이므로 `LOWER()` 없이 `LIKE`만으로도 대소문자 무관 검색이 되지만, 명시적으로 서비스 레이어에서 트림(trim) 처리한다.

**응답 (200)**

```json
[
  { "id": 10, "name": "와퍼", "price": 7100, "imageUrl": "/api/images/menu/whopper.jpg", "isSet": false, "categoryName": "버거", "isSoldOut": false }
]
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 검색 성공 (결과 없으면 빈 배열) |
| 400 | keyword 누락/공백 (`VALIDATION_ERROR`) |

---

### 3.3 장바구니 API (`/api/carts`)

장바구니는 `carts`(헤더) + `cart_items`(상세)로 관리된다. 세션 식별은 `X-Session-Id` 헤더를 사용하며, 세션이 없는 최초 요청 시 서버가 새 `session_id`(UUID)를 생성해 `carts` 행을 만들고 응답 헤더로 돌려준다.

#### 3.3.1 `GET /api/carts` — 장바구니 조회

**요청 헤더**

| 이름 | 필수 | 설명 |
|---|---|---|
| X-Session-Id | Y | 세션 식별자. 없으면 `400 SESSION_ID_REQUIRED` |

**비즈니스 규칙**
- 세션에 해당하는 `carts` 행이 없으면(한 번도 담은 적 없음) 빈 장바구니(`items: []`, `totalPrice: 0`)를 200으로 반환한다(404가 아님 — 장바구니 부재는 정상 상태).
- `cart_items`와 `menus`를 조인해 현재 메뉴명/가격/이미지/품절 여부를 실시간으로 반영한다(장바구니는 스냅샷을 쓰지 않음 — 스냅샷은 주문 확정 시점에만 의미가 있다).

**응답 (200)**

```json
{
  "cartId": 5,
  "sessionId": "b3c1e2b0-...",
  "items": [
    { "cartItemId": 21, "menuId": 10, "menuName": "와퍼", "price": 7100, "imageUrl": "/api/images/menu/whopper.jpg", "quantity": 2, "subtotal": 14200, "isSoldOut": false }
  ],
  "totalPrice": 14200
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공(빈 장바구니 포함) |
| 400 | `X-Session-Id` 누락 (`SESSION_ID_REQUIRED`) |

---

#### 3.3.2 `POST /api/carts/items` — 메뉴 장바구니 담기

**요청 헤더**

| 이름 | 필수 | 설명 |
|---|---|---|
| X-Session-Id | N | 없으면 서버가 신규 세션을 생성하고 응답 헤더 `X-Session-Id`로 반환 |

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| menuId | Long | Y | 담을 메뉴 ID |
| quantity | Integer | Y | 담을 수량 (1 이상) |

**처리 순서**
1. `X-Session-Id`가 없으면 `carts` 신규 행 생성 후 새 `session_id` 발급.
2. `menuId` 존재 여부 확인 → 없으면 `404 MENU_NOT_FOUND`.
3. `is_active = true AND deleted_at IS NULL` 확인 → 아니면 `400 MENU_INACTIVE`.
4. `menus.quantity > 0` 확인(재고 있음) → `0`이면 `400 MENU_SOLD_OUT`.
5. `requestedQuantity <= 1` 등 기본 검증(`quantity < 1`이면 `400 VALIDATION_ERROR`).
6. `cart_items`에 동일 `(cart_id, menu_id)`가 있으면 `quantity += requestedQuantity`로 UPSERT, 없으면 새 행 INSERT.

**응답 (201)**

```json
{ "cartItemId": 21, "menuId": 10, "menuName": "와퍼", "quantity": 2, "subtotal": 14200 }
```

| 상태 코드 | 케이스 |
|---|---|
| 201 | 담기 성공 |
| 400 | `quantity < 1` (`VALIDATION_ERROR`), 비활성 메뉴(`MENU_INACTIVE`), 품절(`MENU_SOLD_OUT`) |
| 404 | 메뉴 없음 (`MENU_NOT_FOUND`) |

---

#### 3.3.3 `PATCH /api/carts/items/{cartItemId}` — 장바구니 항목 수량 수정

**요청 헤더**: `X-Session-Id` (필수)

**경로 파라미터**: `cartItemId` (Long, 필수)

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| quantity | Integer | Y | 변경할 수량 (1 이상) |

**비즈니스 규칙**
- `cartItemId`가 존재하지 않으면 `404 CART_ITEM_NOT_FOUND`.
- `cart_items.cart_id`가 요청 세션의 `carts.id`와 다르면 `403 CART_ACCESS_FORBIDDEN`(타 세션 접근 차단).
- `quantity <= 0`이면 삭제 처리로 전환하지 않고 `400 VALIDATION_ERROR`를 반환한다(삭제는 반드시 DELETE 엔드포인트를 사용).

**응답 (200)**

```json
{ "cartItemId": 21, "menuId": 10, "menuName": "와퍼", "quantity": 3, "subtotal": 21300 }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 수정 성공 |
| 400 | `quantity <= 0` (`VALIDATION_ERROR`) |
| 403 | 타 세션 항목 접근 (`CART_ACCESS_FORBIDDEN`) |
| 404 | 항목 없음 (`CART_ITEM_NOT_FOUND`) |

---

#### 3.3.4 `DELETE /api/carts/items/{cartItemId}` — 장바구니 항목 삭제

**요청 헤더**: `X-Session-Id` (필수)

**비즈니스 규칙**: 본인 세션 소유 항목만 삭제 가능(`403 CART_ACCESS_FORBIDDEN`), 존재하지 않으면 `404 CART_ITEM_NOT_FOUND`.

| 상태 코드 | 케이스 |
|---|---|
| 204 | 삭제 성공 (본문 없음) |
| 403 | 타 세션 항목 접근 |
| 404 | 항목 없음 |

---

#### 3.3.5 `DELETE /api/carts` — 장바구니 전체 비우기

**요청 헤더**: `X-Session-Id` (필수)

**비즈니스 규칙**: 해당 세션의 `carts` 행과 하위 `cart_items`를 전부 삭제한다(`cart_items`는 `ON DELETE CASCADE`로 자동 정리되지만, 서비스 레이어에서도 명시적으로 삭제 흐름을 문서화한다). 장바구니가 이미 없으면(비어 있음) 멱등하게 `204`를 반환한다.

| 상태 코드 | 케이스 |
|---|---|
| 204 | 성공(이미 비어 있어도 204) |
| 400 | `X-Session-Id` 누락 |

---

### 3.4 주문 API (`/api/orders`)

#### 3.4.1 `POST /api/orders` — 주문 생성

**설명**: 현재 세션의 장바구니 전체를 하나의 주문으로 전환한다. 결제 절차가 없으므로 이 API 호출 자체가 곧 결제 완료를 의미하며, 트랜잭션 성공 시 즉시 `status = COMPLETED`로 확정된다.

**요청 헤더**: `X-Session-Id` (필수)

**요청 Body**: 없음

**처리 순서 (단일 트랜잭션)**

1. `X-Session-Id`로 `carts` → `cart_items`(+`menus` 조인) 전체 조회.
2. 장바구니가 없거나 항목이 0건이면 `400 CART_EMPTY`로 즉시 종료.
3. 각 `cart_items` 행에 대해 연결된 `menus`를 **재검증**한다: `deleted_at IS NULL`, `is_active = true`, `quantity > 0`(요청 수량 이상). 하나라도 위반하면 `400 MENU_UNAVAILABLE`, 메시지에 문제 메뉴명을 포함해 반환하고 트랜잭션을 롤백한다(부분 주문 금지 — 전량 성공 또는 전량 실패).
4. `order_number_sequence`에서 `UPDATE ... SET next_value = LAST_INSERT_ID(next_value) + 1` 패턴으로 원자적으로 `order_number`를 채번한다(DB.md 6장 참조).
5. `orders` 행을 INSERT한다 (`order_number`, `status='COMPLETED'`, `total_price=Σ(subtotal)`, `session_id`).
6. 각 장바구니 항목마다 `order_items`를 INSERT한다(`menu_name`, `menu_price`는 이 시점의 `menus` 값을 스냅샷).
7. 각 메뉴의 `menus.quantity`를 주문 수량만큼 차감한다. 차감 쿼리는 `UPDATE menus SET quantity = quantity - :qty WHERE id = :menuId AND quantity >= :qty`로 실행해 동시 주문에 의한 음수 재고를 방지하고, 영향받은 행이 0이면(동시 주문으로 재고가 방금 소진된 경우) `400 INSUFFICIENT_STOCK`으로 트랜잭션 전체를 롤백한다.
8. 해당 세션의 `carts`/`cart_items`를 전체 삭제한다.
9. 커밋 후 주문 결과를 반환한다.

**응답 (201)**

```json
{
  "orderNumber": 101,
  "totalPrice": 14200,
  "items": [
    { "menuName": "와퍼", "menuPrice": 7100, "quantity": 2, "subtotal": 14200 }
  ],
  "createdAt": "2026-07-08T12:30:00"
}
```

| 상태 코드 | 케이스 |
|---|---|
| 201 | 주문 생성 성공 |
| 400 | 장바구니 비어있음 (`CART_EMPTY`), 품절/비활성 메뉴 포함 (`MENU_UNAVAILABLE`), 동시성으로 인한 재고 부족 (`INSUFFICIENT_STOCK`) |

**비고**: 결제가 없으므로 `PENDING` 상태는 존재하지 않는다. 옵션(커스터마이징) 관련 필드는 요청/응답 어디에도 존재하지 않는다.

---

### [관리자 - 관리자 시점] JWT 인증 필요

---

### 3.5 관리자 인증 API (`/api/admin/auth`)

#### 3.5.1 `POST /api/admin/auth/login` — 로그인

**인증**: 불필요(로그인 자체이므로)

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| username | String | Y | 관리자 아이디 |
| password | String | Y | 평문 비밀번호 (전송 구간은 HTTPS로 보호) |

**처리 순서**
1. `admins.username`으로 계정 조회 → 없으면 `401 INVALID_CREDENTIALS`(계정 존재 여부를 노출하지 않기 위해 아이디 오류와 비밀번호 오류를 동일 메시지로 응답).
2. `deleted_at IS NOT NULL` 또는 `is_active = false`이면 `403 ACCOUNT_INACTIVE`.
3. bcrypt로 `password`와 `password_hash` 비교 → 불일치 시 `401 INVALID_CREDENTIALS`.
4. 성공 시 JWT 발급(`sub=username`, `iat`, `exp`).
5. `admins.last_login_at = NOW()` 갱신.
6. 감사 로그 기록: `action=LOGIN`, `target_type=admin`, `target_id=null`(대상 PK가 문자열이라 `target_id BIGINT`에 담을 수 없으므로 `null` 처리하고 `admin_username`으로 행위자를 식별).

**응답 (200)**

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", "expiresIn": 7200 }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 로그인 성공 |
| 401 | 아이디/비밀번호 불일치 (`INVALID_CREDENTIALS`) |
| 403 | 비활성/삭제된 계정 (`ACCOUNT_INACTIVE`) |

---

#### 3.5.2 `POST /api/admin/auth/logout` — 로그아웃

**인증**: 필요

**토큰 무효화 방식 결정**: **클라이언트 측 토큰 삭제 방식**을 채택한다.

- 근거: 블랙리스트 방식은 로그아웃된(그러나 아직 만료되지 않은) 토큰 목록을 저장할 별도 테이블/캐시(Redis 등)가 필요하지만, DB.md에는 그러한 테이블이 정의되어 있지 않고 관리자가 1인뿐인 소규모 백오피스 특성상 그 정도의 인프라 비용은 과도하다.
- 대신 `expiresIn`을 짧게(예: 2시간) 유지해 탈취된 토큰의 유효 기간 자체를 최소화하고, 로그아웃 API는 **감사 로그 기록** 및 **클라이언트에게 토큰 삭제 지시(의미상 확인 응답)** 역할만 수행한다.
- 서버는 이 요청이 유효한 JWT로 인증되었는지만 확인(만료/변조 여부)하고, 실제 토큰 무효화는 클라이언트가 로컬 저장소에서 토큰을 제거하는 것으로 완결된다.

**처리 순서**: JWT 검증(정상 인증) → 감사 로그 기록(`action=LOGOUT`) → 200 반환.

**응답 (200)**

```json
{ "message": "로그아웃 되었습니다." }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 성공 |
| 401 | 토큰 없음/만료/위조 |

---

### 3.6 대시보드 API (`/api/admin/dashboard`)

#### 3.6.1 `GET /api/admin/dashboard` — 대시보드 요약 조회

**비즈니스 규칙**
- "오늘"은 서버 타임존(Asia/Seoul) 기준 자정~현재.
- "이번 달"은 해당 월 1일 00:00~현재.
- 매출/주문 건수 집계는 모두 `status = 'COMPLETED'`만 포함.
- `totalMenuCount`: `deleted_at IS NULL AND is_active = true`인 메뉴 수.
- `soldOutMenuCount`: 위 조건을 만족하면서 `quantity = 0`인 메뉴 수.
- `recentOrders`: 전체 주문(상태 무관) 중 `created_at` 내림차순 5건.

**응답 (200)**

```json
{
  "todaySales": 152300,
  "todayOrderCount": 14,
  "monthSales": 3021900,
  "totalMenuCount": 24,
  "soldOutMenuCount": 2,
  "recentOrders": [
    { "orderNumber": 128, "totalPrice": 14200, "status": "COMPLETED", "createdAt": "2026-07-08T12:30:00" }
  ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 401 / 403 | 인증 실패 / 비활성 계정 |

---

### 3.7 관리자 - 카테고리 관리 API (`/api/admin/categories`)

#### 3.7.1 `GET /api/admin/categories` — 카테고리 전체 조회

**비즈니스 규칙**: 비활성 카테고리를 포함해 전체를 반환한다(고객용 API와 달리 필터 없음).

**응답 (200)**

```json
[
  { "id": 1, "name": "버거", "displayOrder": 1, "isActive": true, "createdAt": "2026-01-01T00:00:00" }
]
```

---

#### 3.7.2 `GET /api/admin/categories/{categoryId}` — 카테고리 상세 조회

**응답 (200)**

```json
{ "id": 1, "name": "버거", "displayOrder": 1, "isActive": true, "createdAt": "2026-01-01T00:00:00", "menuCount": 5 }
```

- `menuCount`: 해당 카테고리 소속 메뉴 중 `deleted_at IS NULL`인 메뉴 수(활성/비활성 무관, 삭제되지 않은 전체 메뉴 수).

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 카테고리 없음 (`CATEGORY_NOT_FOUND`) |

---

#### 3.7.3 `POST /api/admin/categories` — 카테고리 등록

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | String | Y | 카테고리명 (중복 불가) |
| displayOrder | Integer | N | 노출 순서 (기본값 0) |

**비즈니스 규칙**: `name` 중복 시 `409 CATEGORY_NAME_DUPLICATE`.

**감사 로그**: `action=CATEGORY_CREATE`, `target_type=category`, `target_id={생성된 id}`, `after_value={등록된 카테고리 전체 필드 JSON}`, `before_value=null`.

**응답 (201)**

```json
{ "id": 4, "name": "디저트", "displayOrder": 4, "isActive": true, "createdAt": "2026-07-08T10:00:00" }
```

| 상태 코드 | 케이스 |
|---|---|
| 201 | 생성 성공 |
| 400 | `name` 누락 등 검증 실패 |
| 409 | 이름 중복 (`CATEGORY_NAME_DUPLICATE`) |

---

#### 3.7.4 `PUT /api/admin/categories/{categoryId}` — 카테고리 수정

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | String | Y | 카테고리명 |
| displayOrder | Integer | Y | 노출 순서 |
| isActive | Boolean | Y | 활성 여부 |

**비즈니스 규칙**: `name`을 다른 카테고리와 중복되게 변경하면 `409 CATEGORY_NAME_DUPLICATE`.

**감사 로그**: `action=CATEGORY_UPDATE`, `before_value={수정 전 전체 필드}`, `after_value={수정 후 전체 필드}`.

**응답 (200)**: 수정된 카테고리 전체 필드.

| 상태 코드 | 케이스 |
|---|---|
| 200 | 수정 성공 |
| 404 | 카테고리 없음 |
| 409 | 이름 중복 |

---

#### 3.7.5 `DELETE /api/admin/categories/{categoryId}` — 카테고리 삭제

**비즈니스 규칙**
- 해당 카테고리에 속한 메뉴 중 `deleted_at IS NULL`(활성이든 비활성이든 소프트 삭제되지 않은 메뉴)이 1건이라도 있으면 `409 CATEGORY_HAS_MENUS`. (DB의 `fk_menus_category ON DELETE RESTRICT`가 최후 방어선이지만, 서비스 레이어에서 먼저 검증해 사유가 담긴 메시지를 제공한다.)
- 소속 메뉴가 없거나 모두 소프트 삭제된 경우에만 `categories` 행을 하드 삭제한다.

**감사 로그**: `action=CATEGORY_DELETE`, `before_value={삭제된 카테고리 전체 필드}`, `after_value=null`.

**응답 (200)**

```json
{ "message": "카테고리가 삭제되었습니다." }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 삭제 성공 |
| 404 | 카테고리 없음 |
| 409 | 소속 활성 메뉴 존재 (`CATEGORY_HAS_MENUS`, 메시지에 소속 메뉴 수 포함) |

---

### 3.8 관리자 - 메뉴 관리 API (`/api/admin/menus`)

#### 3.8.1 `GET /api/admin/menus` — 메뉴 전체 조회

**쿼리 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | N | 카테고리 필터 |
| isSet | Boolean | N | 세트/단품 필터 |
| isActive | Boolean | N | 활성 여부 필터 |
| page | Integer | N | 0-based, 기본값 0 |
| size | Integer | N | 기본값 20 |

**비즈니스 규칙**: `deleted_at IS NULL`인 메뉴만 대상으로 하고, 쿼리 파라미터가 주어지면 AND 조건으로 추가 필터링한다.

**응답 (200)**

```json
{
  "content": [
    { "id": 10, "name": "와퍼", "categoryName": "버거", "price": 7100, "isSet": false, "quantity": 98, "isActive": true }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 24,
  "totalPages": 2
}
```

---

#### 3.8.2 `GET /api/admin/menus/{menuId}` — 메뉴 상세 조회

**비즈니스 규칙**: 소프트 삭제된 메뉴도 조회 가능(관리자 전용, 이력 확인 목적). `is_set=true`면 `setComponents` 포함.

**응답 (200)**

```json
{
  "id": 16, "categoryId": 1, "name": "와퍼 세트", "description": "...", "price": 9900,
  "imageUrl": "/api/images/menu/whopper-set.jpg", "isSet": true, "quantity": 50, "isActive": true,
  "deletedAt": null, "createdAt": "2026-01-01T00:00:00", "updatedAt": "2026-01-01T00:00:00",
  "setComponents": [ { "id": 10, "name": "와퍼", "price": 7100, "quantity": 1 } ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 메뉴 없음 (`MENU_NOT_FOUND`) |

---

#### 3.8.3 `POST /api/admin/menus` — 단품 메뉴 등록

**요청**: `multipart/form-data`, 두 개의 파트로 구성

| 파트 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `request` | JSON (`application/json`) | Y | 아래 필드를 담은 JSON 문자열 파트 |
| `image` | 파일 | N | 메뉴 이미지 파일(jpg/png/webp, 5MB 이하). 첨부 시 서버가 저장하고 발급한 URL이 `imageUrl`을 덮어쓴다 |

**`request` 파트 필드**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | Y | 소속 카테고리 |
| name | String | Y | 메뉴명 |
| description | String | N | 설명 |
| price | Integer | Y | 가격 (0보다 커야 함) |
| imageUrl | String | N | 이미지 경로. `image` 파일 파트가 함께 오면 무시되고 서버 업로드 결과 URL로 대체된다. `image`도 없고 이 값도 비어 있으면 이미지 없이 등록된다 |
| quantity | Integer | Y | 초기 재고 (0 이상) |

**비즈니스 규칙**
- `isSet`은 요청에서 받지 않고 서버에서 `false`로 고정한다(이 엔드포인트는 단품 전용).
- `categoryId`가 존재하지 않으면 `404 CATEGORY_NOT_FOUND`.
- `price <= 0`이면 `400 VALIDATION_ERROR`, `quantity < 0`이면 `400 VALIDATION_ERROR`.
- `image` 파트가 있으면 3.8.7의 이미지 업로드 검증(허용 확장자, 5MB 제한)이 그대로 적용된다(`INVALID_IMAGE_FILE`, `FILE_TOO_LARGE`).
- 이미지를 미리 업로드해 URL만 알고 있는 경우(3.8.7을 먼저 호출한 경우)에는 `image` 파트 없이 `request.imageUrl`만 채워 보내도 된다 — 두 방식 중 하나를 선택해서 사용한다.

**감사 로그**: `action=MENU_CREATE`, `target_type=menu`, `target_id={생성된 id}`, `after_value={등록된 메뉴 JSON}`.

**응답 (201)**: 생성된 메뉴 전체 필드.

| 상태 코드 | 케이스 |
|---|---|
| 201 | 생성 성공 |
| 400 | `price`/`quantity` 검증 실패, 허용되지 않는 이미지 형식(`INVALID_IMAGE_FILE`), 용량 초과(`FILE_TOO_LARGE`) |
| 404 | 카테고리 없음 |

---

#### 3.8.4 세트 메뉴 등록 (2단계)

**Step 1 — `POST /api/admin/menus/sets`: 세트 메뉴 기본 정보 등록**

요청 형식(멀티파트 `request`+`image` 파트)은 3.8.3과 동일하되 `isSet`은 서버에서 `true`로 고정한다. 이 시점에는 구성품이 없는 "빈 세트"가 생성된다.

**감사 로그**: `action=MENU_CREATE`, `target_type=menu`, `after_value={ ..., "isSet": true }`.

**응답 (201)**: 생성된 세트 메뉴(`id` 포함, `setComponents: []`).

---

**Step 2 — `POST /api/admin/menus/sets/{setMenuId}/components`: 세트 구성품 추가**

**경로 파라미터**: `setMenuId` (Long)

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| componentMenuId | Long | Y | 구성품으로 추가할 단품 메뉴 ID |
| quantity | Integer | Y | 세트 내 수량 (1 이상) |

**검증 순서**
1. `setMenuId`가 존재하고 `is_set = true`인지 확인 → 아니면 `404 MENU_NOT_FOUND` 또는 `400 NOT_A_SET_MENU`.
2. `componentMenuId`가 존재하고 `is_set = false`인지 확인 → 세트를 세트에 넣으려는 시도는 `400 COMPONENT_MUST_BE_SINGLE_ITEM`.
3. `setMenuId == componentMenuId`이면 `400 SELF_REFERENCE_NOT_ALLOWED`.
4. 이미 동일 `(setMenuId, componentMenuId)` 조합이 존재하면 `409 SET_COMPONENT_DUPLICATE`.

**감사 로그**: `action=SET_COMPONENT_ADD`, `target_type=set_menu_items`, `target_id={생성된 set_menu_items.id}`, `after_value={setMenuId, componentMenuId, quantity}`.

**응답 (201)**

```json
{ "id": 30, "setMenuId": 16, "componentMenuId": 10, "quantity": 1 }
```

| 상태 코드 | 케이스 |
|---|---|
| 201 | 추가 성공 |
| 400 | 세트 아님/단품 아님/자기 참조 |
| 404 | `setMenuId` 또는 `componentMenuId` 없음 |
| 409 | 중복 구성품 (`SET_COMPONENT_DUPLICATE`) |

---

**구성품 삭제 — `DELETE /api/admin/menus/sets/{setMenuId}/components/{componentMenuId}`**

**비즈니스 규칙**: 해당 매핑이 없으면 `404 SET_COMPONENT_NOT_FOUND`.

**감사 로그**: `action=SET_COMPONENT_REMOVE`, `target_type=set_menu_items`, `before_value={setMenuId, componentMenuId, quantity}`.

| 상태 코드 | 케이스 |
|---|---|
| 204 | 삭제 성공 |
| 404 | 매핑 없음 |

---

#### 3.8.5 `PUT /api/admin/menus/{menuId}` — 메뉴 수정

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | Y | 소속 카테고리 |
| name | String | Y | 메뉴명 |
| description | String | N | 설명 |
| price | Integer | Y | 가격 |
| imageUrl | String | N | 이미지 경로 |
| isActive | Boolean | Y | 판매 활성 여부 |

**비즈니스 규칙**: `quantity`는 이 API의 요청/응답에 포함되지 않는다(포함되어 와도 무시). 재고 변경은 반드시 `/api/admin/inventory`를 사용한다. `isSet` 여부는 등록 이후 변경 불가(별도 필드 없음).

**감사 로그**: `action=MENU_UPDATE`, `before_value`/`after_value`(quantity 제외 필드만).

**응답 (200)**: 수정된 메뉴 전체 필드.

| 상태 코드 | 케이스 |
|---|---|
| 200 | 수정 성공 |
| 400 | 검증 실패 |
| 404 | 메뉴/카테고리 없음 |

---

#### 3.8.6 `DELETE /api/admin/menus/{menuId}` — 메뉴 삭제 (소프트 삭제)

**비즈니스 규칙**
- `set_menu_items.component_menu_id`로 해당 메뉴를 참조하는 살아있는 세트가 1건이라도 있으면 `409 MENU_IN_USE_AS_SET_COMPONENT`(사용 중인 세트 메뉴명 목록을 메시지에 포함).
- 통과하면 `deleted_at = NOW()`, `is_active = false`로 갱신한다(하드 삭제 아님).

**감사 로그**: `action=MENU_DELETE`, `before_value={삭제 전 메뉴 전체 필드}`.

**응답 (200)**

```json
{ "message": "메뉴가 삭제되었습니다." }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 삭제 성공 |
| 404 | 메뉴 없음 |
| 409 | 세트 구성품으로 사용 중 (`MENU_IN_USE_AS_SET_COMPONENT`) |

---

### 3.8.7 관리자 - 이미지 업로드 API (`/api/admin/images`)

메뉴 등록/수정 화면에서 이미지 파일을 서버에 업로드하고, 반환된 URL을 `MenuCreateRequest`/`MenuUpdateRequest`의 `imageUrl` 필드에 담아 그대로 사용한다(DB.md 설계 원칙: "서버에 파일 업로드 후 URL 경로만 DB에 저장"). 이 API 자체는 `menus` 테이블을 건드리지 않으며, 순수하게 파일을 저장하고 접근 URL만 발급한다.

메뉴 신규 등록 시에는 이 API를 미리 호출할 필요 없이 3.8.3/3.8.4의 `image` 파트로 파일을 바로 첨부해도 된다(내부적으로 동일한 `ImageStorageService`를 사용). 이 API는 ① 메뉴 수정(`PUT /api/admin/menus/{id}`) 시 이미지만 먼저 교체하고 싶을 때, ② 등록 전에 미리보기용으로 이미지를 올려두고 싶을 때처럼 등록 흐름과 분리해서 이미지를 다뤄야 하는 경우에 사용하는 보조 수단이다.

#### `POST /api/admin/images/menu` — 메뉴 이미지 업로드

**요청**: `multipart/form-data`, 파트 이름 `file`

**비즈니스 규칙**
- 허용 형식: `image/jpeg`, `image/png`, `image/webp`만 허용(그 외 `400 INVALID_IMAGE_FILE`).
- 최대 용량 5MB(초과 시 `400 FILE_TOO_LARGE`).
- 빈 파일은 `400 EMPTY_FILE`.
- 저장 파일명은 원본 파일명을 신뢰하지 않고 서버에서 UUID로 새로 채번한다(경로 조작/충돌 방지).
- 저장 위치는 `app.upload.dir`(기본 `uploads/menu`) 로컬 디스크이며, `app.upload.url-prefix`(기본 `/api/images/menu`) 경로의 `GET` 엔드포인트(3.8.8)로 조회된다.

**응답 (201)**

```json
{ "imageUrl": "/api/images/menu/3f1c2b9a-....jpg" }
```

| 상태 코드 | 케이스 |
|---|---|
| 201 | 업로드 성공 |
| 400 | 빈 파일(`EMPTY_FILE`), 허용되지 않는 형식(`INVALID_IMAGE_FILE`), 용량 초과(`FILE_TOO_LARGE`) |

**비고**: 별도 이미지 메타데이터 테이블 없이 파일 자체만 저장하므로, 메뉴 등록/수정 API 호출 없이 업로드만 하고 끝나면 고아 파일이 남을 수 있다(운영 편의를 위해 정기적인 미사용 파일 정리 배치는 향후 과제로 남긴다).

---

### 3.8.8 이미지 조회 API (`/api/images/menu`) — 인증 불필요

`imageUrl` 필드에 담겨 내려오는 값이 가리키는 실제 엔드포인트다. 정적 리소스 매핑이 아니라 일반 컨트롤러로 구현해 OpenAPI 문서에 정식으로 노출되므로, Swagger UI의 "Try it out"으로 실행하면 응답이 `image/jpeg`·`image/png`·`image/webp`로 내려와 결과창에 이미지가 바로 미리보기로 렌더링된다.

#### `GET /api/images/menu/{filename}` — 메뉴 이미지 파일 조회

**경로 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| filename | String | Y | 저장된 파일명(업로드 응답의 `imageUrl` 마지막 경로 조각) |

**비즈니스 규칙**
- 인증이 필요 없다(고객 키오스크 화면에서도 `<img src="...">`로 바로 사용해야 하므로 공개 엔드포인트).
- `filename`에 `/`, `\`, `..`가 포함되어 있거나 실제 저장된 파일이 아니면 `404 IMAGE_NOT_FOUND`(경로 조작 방지).
- 응답 `Content-Type`은 파일 확장자를 기준으로 `image/jpeg`/`image/png`/`image/webp` 중 하나로 설정된다.

**응답 (200)**: 이미지 바이너리(`Content-Type: image/*`)

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 파일 없음/잘못된 경로 (`IMAGE_NOT_FOUND`) |

---

### 3.9 재고 관리 API (`/api/admin/inventory`)

#### 3.9.1 `GET /api/admin/inventory` — 재고 현황 전체 조회

**쿼리 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| isSoldOut | Boolean | N | `true`면 `quantity = 0`만, `false`면 `quantity > 0`만 |
| page | Integer | N | 기본값 0 |
| size | Integer | N | 기본값 20 |

**대상 범위**: `deleted_at IS NULL`인 메뉴 전체(비활성 메뉴 포함 — 재고는 판매 상태와 무관하게 관리 대상).

**응답 (200)**

```json
{
  "content": [
    { "menuId": 10, "menuName": "와퍼", "categoryName": "버거", "quantity": 98, "isSoldOut": false }
  ],
  "page": 0, "size": 20, "totalElements": 24, "totalPages": 2
}
```

---

#### 3.9.2 `PATCH /api/admin/inventory/{menuId}` — 메뉴 재고 수량 수정

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| quantity | Integer | Y | 새 재고 수량 (0 이상) |

**비즈니스 규칙**: `quantity < 0`이면 `400 VALIDATION_ERROR`. 이 API는 `menus.quantity`만 수정하며 다른 필드는 건드리지 않는다.

**감사 로그**: `action=INVENTORY_UPDATE`, `target_type=menu`, `target_id=menuId`, `before_value={"quantity": 이전값}`, `after_value={"quantity": 새값}`.

**응답 (200)**

```json
{ "menuId": 10, "menuName": "와퍼", "quantity": 50 }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 수정 성공 |
| 400 | 음수 수량 (`VALIDATION_ERROR`) |
| 404 | 메뉴 없음 |

---

### 3.10 관리자 - 주문 관리 API (`/api/admin/orders`)

#### 3.10.1 `GET /api/admin/orders` — 주문 전체 조회

**쿼리 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | String | N | `COMPLETED` \| `CANCELLED` |
| page | Integer | N | 기본값 0 |
| size | Integer | N | 기본값 20 |

**비즈니스 규칙**: `created_at` 내림차순(최신순) 정렬.

**응답 (200)**

```json
{
  "content": [
    { "id": 128, "orderNumber": 228, "status": "COMPLETED", "totalPrice": 14200, "createdAt": "2026-07-08T12:30:00" }
  ],
  "page": 0, "size": 20, "totalElements": 300, "totalPages": 15
}
```

---

#### 3.10.2 `GET /api/admin/orders/{orderId}` — 주문 상세 조회

**응답 (200)**

```json
{
  "id": 128, "orderNumber": 228, "status": "COMPLETED", "totalPrice": 14200,
  "sessionId": "b3c1e2b0-...", "createdAt": "2026-07-08T12:30:00",
  "items": [ { "menuName": "와퍼", "menuPrice": 7100, "quantity": 2, "subtotal": 14200 } ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 주문 없음 (`ORDER_NOT_FOUND`) |

---

#### 3.10.3 `PATCH /api/admin/orders/{orderId}/cancel` — 주문 취소

**비즈니스 규칙**
- 대상 주문이 이미 `status = 'CANCELLED'`이면 `409 ORDER_ALREADY_CANCELLED`.
- 취소 시 재고를 복구하지 않는다(요구사항상 단순 상태 변경만 수행 — 재고 복구 로직 없음).
- 취소 즉시 해당 주문은 이후 모든 매출 집계(대시보드, 일/월/연 매출)에서 자동 제외된다(집계 쿼리가 `status='COMPLETED'`만 필터링하기 때문).

**감사 로그**: `action=ORDER_CANCEL`, `target_type=order`, `target_id=orderId`, `before_value={"status":"COMPLETED"}`, `after_value={"status":"CANCELLED"}`.

**응답 (200)**

```json
{ "id": 128, "orderNumber": 228, "status": "CANCELLED", "totalPrice": 14200, "createdAt": "2026-07-08T12:30:00" }
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 취소 성공 |
| 404 | 주문 없음 |
| 409 | 이미 취소됨 (`ORDER_ALREADY_CANCELLED`) |

---

### 3.11 매출 조회 API (`/api/admin/sales`)

모든 집계는 `status = 'COMPLETED'`만 포함한다.

#### 3.11.1 `GET /api/admin/sales/daily?date={YYYY-MM-DD}`

**쿼리 파라미터**: `date` (필수, ISO 날짜)

**응답 (200)**

```json
{
  "date": "2026-07-08",
  "totalSales": 152300,
  "orderCount": 14,
  "orders": [ { "orderNumber": 228, "totalPrice": 14200, "createdAt": "2026-07-08T12:30:00" } ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공(해당일 매출 없으면 0/빈 배열) |
| 400 | `date` 형식 오류 |

---

#### 3.11.2 `GET /api/admin/sales/monthly?year={YYYY}&month={MM}`

**응답 (200)**

```json
{
  "year": 2026, "month": 7, "totalSales": 3021900, "orderCount": 210,
  "dailyBreakdown": [ { "date": "2026-07-01", "dailySales": 98000, "dailyOrderCount": 9 } ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 400 | `year`/`month` 범위 오류 (`month`는 1~12) |

---

#### 3.11.3 `GET /api/admin/sales/yearly?year={YYYY}`

**응답 (200)**

```json
{
  "year": 2026, "totalSales": 36500000, "orderCount": 2500,
  "monthlyBreakdown": [ { "month": 1, "monthlySales": 3000000, "monthlyOrderCount": 210 } ]
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 400 | `year` 형식 오류 |

---

### 3.12 감사 로그 조회 API (`/api/admin/audit-logs`)

#### 3.12.1 `GET /api/admin/audit-logs` — 감사 로그 전체 조회

**쿼리 파라미터**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| action | String | N | 액션 코드 필터 (예: `MENU_CREATE`) |
| startDate | String | N | 조회 시작일 (`YYYY-MM-DD`, `created_at >= startDate 00:00:00`) |
| endDate | String | N | 조회 종료일 (`created_at <= endDate 23:59:59`) |
| page | Integer | N | 기본값 0 |
| size | Integer | N | 기본값 20 |

**비즈니스 규칙**: `created_at` 내림차순 정렬.

**응답 (200)**

```json
{
  "content": [
    { "id": 501, "adminUsername": "admin", "action": "MENU_CREATE", "targetType": "menu", "targetId": 24, "ipAddress": "127.0.0.1", "createdAt": "2026-07-08T09:00:00" }
  ],
  "page": 0, "size": 20, "totalElements": 501, "totalPages": 26
}
```

---

#### 3.12.2 `GET /api/admin/audit-logs/{logId}` — 감사 로그 상세 조회

**응답 (200)**

```json
{
  "id": 501, "adminUsername": "admin", "action": "MENU_CREATE", "targetType": "menu", "targetId": 24,
  "beforeValue": null,
  "afterValue": { "id": 24, "name": "치즈스틱", "price": 3500, "categoryId": 2 },
  "ipAddress": "127.0.0.1", "createdAt": "2026-07-08T09:00:00"
}
```

| 상태 코드 | 케이스 |
|---|---|
| 200 | 조회 성공 |
| 404 | 로그 없음 (`AUDIT_LOG_NOT_FOUND`) |

---

## 4. 에러 코드 정의

| 에러 코드 | HTTP 상태 | 메시지 예시 | 발생 위치 |
|---|---|---|---|
| `VALIDATION_ERROR` | 400 | "요청 값이 올바르지 않습니다." | 공통 (Bean Validation 실패 시 GlobalExceptionHandler) |
| `SESSION_ID_REQUIRED` | 400 | "X-Session-Id 헤더가 필요합니다." | 장바구니 조회/수정/삭제 API |
| `MENU_INACTIVE` | 400 | "판매 중이 아닌 메뉴입니다." | 장바구니 담기 |
| `MENU_SOLD_OUT` | 400 | "품절된 메뉴입니다: {menuName}" | 장바구니 담기, 주문 생성 |
| `MENU_UNAVAILABLE` | 400 | "주문할 수 없는 메뉴가 포함되어 있습니다: {menuName}" | 주문 생성(품절/비활성 재검증) |
| `CART_EMPTY` | 400 | "장바구니가 비어 있습니다." | 주문 생성 |
| `INSUFFICIENT_STOCK` | 400 | "재고가 부족합니다: {menuName}" | 주문 생성(재고 차감 동시성 실패) |
| `NOT_A_SET_MENU` | 400 | "세트 메뉴가 아닙니다." | 세트 구성품 추가 |
| `COMPONENT_MUST_BE_SINGLE_ITEM` | 400 | "세트 구성품은 단품 메뉴여야 합니다." | 세트 구성품 추가 |
| `SELF_REFERENCE_NOT_ALLOWED` | 400 | "자기 자신을 구성품으로 추가할 수 없습니다." | 세트 구성품 추가 |
| `EMPTY_FILE` | 400 | "업로드할 파일이 비어 있습니다." | 이미지 업로드 |
| `INVALID_IMAGE_FILE` | 400 | "이미지 파일(JPG, PNG, WEBP)만 업로드할 수 있습니다." | 이미지 업로드 |
| `FILE_TOO_LARGE` | 400 | "파일 크기는 5MB를 초과할 수 없습니다." | 이미지 업로드 |
| `INVALID_CREDENTIALS` | 401 | "아이디 또는 비밀번호가 올바르지 않습니다." | 관리자 로그인 |
| `UNAUTHORIZED` | 401 | "인증이 필요합니다." | JWT 누락/만료/위조 |
| `ACCOUNT_INACTIVE` | 403 | "비활성화된 관리자 계정입니다." | 관리자 로그인/인증 필터 |
| `CART_ACCESS_FORBIDDEN` | 403 | "본인 세션의 장바구니만 접근할 수 있습니다." | 장바구니 항목 수정/삭제 |
| `CATEGORY_NOT_FOUND` | 404 | "카테고리를 찾을 수 없습니다." | 카테고리 조회/수정/삭제 |
| `MENU_NOT_FOUND` | 404 | "메뉴를 찾을 수 없습니다." | 메뉴 조회/수정/삭제 |
| `CART_ITEM_NOT_FOUND` | 404 | "장바구니 항목을 찾을 수 없습니다." | 장바구니 항목 수정/삭제 |
| `ORDER_NOT_FOUND` | 404 | "주문을 찾을 수 없습니다." | 관리자 주문 조회/취소 |
| `SET_COMPONENT_NOT_FOUND` | 404 | "세트 구성품을 찾을 수 없습니다." | 세트 구성품 삭제 |
| `AUDIT_LOG_NOT_FOUND` | 404 | "감사 로그를 찾을 수 없습니다." | 감사 로그 상세 조회 |
| `IMAGE_NOT_FOUND` | 404 | "이미지를 찾을 수 없습니다." | 이미지 조회(`GET /api/images/menu/{filename}`) |
| `CATEGORY_NAME_DUPLICATE` | 409 | "이미 존재하는 카테고리명입니다." | 카테고리 등록/수정 |
| `CATEGORY_HAS_MENUS` | 409 | "소속된 메뉴가 있어 삭제할 수 없습니다. ({count}건)" | 카테고리 삭제 |
| `SET_COMPONENT_DUPLICATE` | 409 | "이미 추가된 구성품입니다." | 세트 구성품 추가 |
| `MENU_IN_USE_AS_SET_COMPONENT` | 409 | "다음 세트 메뉴에서 사용 중입니다: {setMenuNames}" | 메뉴 삭제 |
| `ORDER_ALREADY_CANCELLED` | 409 | "이미 취소된 주문입니다." | 주문 취소 |
| `INTERNAL_SERVER_ERROR` | 500 | "일시적인 오류가 발생했습니다." | 공통 (예기치 못한 예외) |

---

## 5. 감사 로그 액션 코드 정의

| action 코드 | 기록 시점 | target_type | target_id |
|---|---|---|---|
| `LOGIN` | 관리자 로그인 성공 시 | `admin` | `null` |
| `LOGOUT` | 관리자 로그아웃 요청 시 | `admin` | `null` |
| `CATEGORY_CREATE` | 카테고리 등록 성공 시 | `category` | 생성된 카테고리 ID |
| `CATEGORY_UPDATE` | 카테고리 수정 성공 시 | `category` | 수정된 카테고리 ID |
| `CATEGORY_DELETE` | 카테고리 삭제 성공 시 | `category` | 삭제된 카테고리 ID |
| `MENU_CREATE` | 단품/세트 메뉴 등록 성공 시 | `menu` | 생성된 메뉴 ID |
| `MENU_UPDATE` | 메뉴 정보 수정 성공 시 | `menu` | 수정된 메뉴 ID |
| `MENU_DELETE` | 메뉴 소프트 삭제 성공 시 | `menu` | 삭제된 메뉴 ID |
| `SET_COMPONENT_ADD` | 세트 구성품 추가 성공 시 | `set_menu_items` | 생성된 매핑 ID |
| `SET_COMPONENT_REMOVE` | 세트 구성품 삭제 성공 시 | `set_menu_items` | 삭제된 매핑 ID |
| `INVENTORY_UPDATE` | 재고 수량 수정 성공 시 | `menu` | 대상 메뉴 ID |
| `ORDER_CANCEL` | 주문 취소 성공 시 | `order` | 취소된 주문 ID |

- `admin_username`은 요청을 처리한 JWT의 `sub` 클레임에서 추출해 모든 로그 행에 기록한다.
- 감사 로그 기록은 각 API의 비즈니스 트랜잭션 **커밋 직전**에 함께 이루어져야 한다(행위와 로그가 원자적으로 함께 성공/실패해야 함 — 별도 비동기 처리 금지).
- 조회(GET) API는 감사 로그 대상이 아니다.

---

## 6. 주요 비즈니스 규칙 요약

### 6.1 재고 처리
- 재고(`menus.quantity`)는 오직 `/api/admin/inventory` API로만 직접 수정 가능하다. `PUT /api/admin/menus/{menuId}`는 재고를 다루지 않는다.
- 재고는 주문 생성 성공 시에만 차감되며, 차감은 `UPDATE ... WHERE quantity >= :qty` 조건부 쿼리로 원자적으로 수행해 동시 주문 경쟁 상태(race condition)를 방지한다.
- 주문 취소는 재고를 복구하지 않는다(단순 상태 변경).
- `quantity = 0`인 메뉴는 장바구니 담기·주문 생성 시 모두 차단된다(품절).

### 6.2 매출 집계 제외 조건
- 대시보드, 일/월/연 매출 API는 모두 `orders.status = 'COMPLETED'`인 주문만 집계 대상으로 포함한다.
- `status = 'CANCELLED'`인 주문은 취소 시점부터 소급 없이 즉시 모든 매출 집계에서 제외된다(별도 배치 없이 조회 쿼리 조건으로 자연스럽게 반영됨).

### 6.3 세트 메뉴 등록 흐름
1. `POST /api/admin/menus/sets`로 세트 메뉴 기본 정보(가격 포함)를 먼저 등록한다 — 이 시점에는 구성품이 없는 상태.
2. `POST /api/admin/menus/sets/{setMenuId}/components`를 구성품 수만큼 반복 호출해 기존 단품 메뉴들을 하나씩 연결한다.
3. 세트 가격은 구성 단품 가격의 합과 무관하게 독립적으로 관리된다(Step 1에서 별도 지정).
4. 구성품은 언제든 추가/삭제 가능하며, 세트 자체가 삭제되지 않는 한 구성 단품은 `ON DELETE RESTRICT`로 보호되어 실수로 하드 삭제될 수 없다(먼저 구성품 삭제 API로 매핑을 제거해야 함).

### 6.4 주문 처리 흐름
1. 고객이 여러 화면을 오가며 `/api/carts/items`로 장바구니를 구성한다(세션 기반, 로그인 없음).
2. `/api/orders` 호출 한 번으로 장바구니 전체가 하나의 주문으로 전환된다.
3. 결제 절차가 없으므로 주문 생성 트랜잭션이 커밋되는 순간이 곧 "결제 완료" 시점이며 `status`는 항상 `COMPLETED`로 시작한다.
4. 메뉴명/단가는 주문 시점 스냅샷으로 `order_items`에 저장되어, 이후 메뉴 가격이 바뀌거나 메뉴가 삭제되어도 주문 이력은 변하지 않는다.
5. 주문 완료 시 장바구니는 비워진다. 이후 취소는 관리자만 가능하며(`PATCH /api/admin/orders/{orderId}/cancel`), 고객에게는 별도 취소 API를 제공하지 않는다.

---

## 7. 레이어 구조 가이드

Spring Boot 기준 `Controller → Service → Repository` 3계층을 원칙으로 하며, 각 계층의 책임은 다음과 같이 엄격히 분리한다.

### 7.1 Controller

- HTTP 요청/응답 매핑, 경로·쿼리 파라미터 바인딩, `@Valid` 기반 요청 Body 검증(형식 검증: null, 길이, 범위 등)만 담당한다.
- 비즈니스 로직(존재 여부 확인, 상태 검증, 트랜잭션 처리)을 절대 포함하지 않는다 — Service에 위임만 한다.
- 요청 DTO(`XxxRequest`) → Service 호출 → 응답 DTO(`XxxResponse`) 변환 후 공통 응답 포맷(`ApiResponse<T>`)으로 감싸 반환한다.
- 인증이 필요한 관리자 API는 `HandlerMethodArgumentResolver` 또는 `SecurityContext`를 통해 현재 로그인한 `admin_username`을 주입받아 Service에 전달한다(감사 로그 기록용).
- 예외는 던지기만 하고, 실제 HTTP 상태 코드/메시지 매핑은 `@RestControllerAdvice` 기반 `GlobalExceptionHandler`가 전담한다.

### 7.2 Service

- 실질적인 비즈니스 로직 전부(존재 검증, 상태 검증, 계산, 채번, 재고 차감, 트랜잭션 경계)를 담당하는 계층.
- `@Transactional` 경계는 이 계층에 선언한다(예: `OrderService.createOrder()` 전체가 하나의 트랜잭션).
- 여러 Repository를 조합해 도메인 규칙을 구현한다(예: `MenuService`가 `MenuRepository` + `CategoryRepository`를 함께 사용해 카테고리 유효성 검증).
- 관리자 쓰기 API를 처리하는 Service 메서드는 트랜잭션 커밋 직전에 `AuditLogService.record(...)`를 호출해 `admin_audit_logs`에 행위를 기록한다(관점 지향 프로그래밍(AOP) 기반 공통 처리도 가능하나, `before_value`/`after_value`처럼 도메인별로 다른 스냅샷 구성 로직이 필요하므로 명시적 호출을 기본 원칙으로 한다).
- 도메인 예외(`MenuNotFoundException`, `InsufficientStockException` 등)를 정의해 던지며, Controller/Repository를 몰라도 되도록 순수 비즈니스 로직에 집중한다.

### 7.3 Repository

- Spring Data JPA `JpaRepository<Entity, ID>`를 상속한 인터페이스로 구성하며, 단순 CRUD 이상의 조회는 쿼리 메서드(`findByCategoryIdAndIsActiveTrue` 등) 또는 `@Query`로 표현한다.
- 비즈니스 로직·검증·예외 처리를 포함하지 않는다. 순수하게 영속성 접근만 담당한다.
- 페이지네이션이 필요한 목록 조회는 `Pageable`을 파라미터로 받는 메서드로 구현하고, Service가 `page`/`size` 쿼리 파라미터를 `PageRequest.of(page, size)`로 변환해 전달한다.
- 재고 차감처럼 동시성이 중요한 쓰기는 `@Modifying @Query("UPDATE Menu m SET m.quantity = m.quantity - :qty WHERE m.id = :id AND m.quantity >= :qty")` 형태로 조건부 UPDATE를 구현해 원자성을 보장한다.

### 7.4 공통 인프라 구성 요소

| 구성 요소 | 책임 |
|---|---|
| `JwtAuthenticationFilter` | `Authorization` 헤더에서 JWT 추출/검증, `SecurityContext`에 인증 정보 설정. `/api/admin/**` 경로에만 적용(`/api/admin/auth/login` 제외) |
| `GlobalExceptionHandler` (`@RestControllerAdvice`) | 도메인 예외 → 에러 코드/HTTP 상태 매핑, 공통 실패 응답 포맷 생성 |
| `AuditLogService` | `admin_audit_logs` INSERT 전담. `before_value`/`after_value`를 JSON 직렬화 |
| `OrderNumberSequenceService` | `order_number_sequence` 원자적 채번 전담(DB.md 6.3의 `UPDATE ... LAST_INSERT_ID` 패턴 캡슐화) |
| `SessionCartResolver` | `X-Session-Id` 헤더 파싱, 신규 세션 생성 및 응답 헤더 설정 공통 처리 |
| `ImageStorageService` | 메뉴 이미지 파일을 로컬 디스크에 저장(UUID 파일명 채번, 확장자 화이트리스트 검증)하고 공개 URL 경로를 반환 |
