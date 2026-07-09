# 버거킹 키오스크 시스템 바이브 코딩 with Claude - 프롬프트 모음

명세서 작성, 코드 작성 등에 사용한 프롬프트를 모아놓았습니다.

---

AI에게 주로 부여한 페르소나 : 상위 1%의 ~~ 아키텍쳐 설계자
AI를 주로 활용한 방법 : Claude 데스크탑을 통해 명세서를 작성하기 위한 프롬프트를 작성하고 그 프롬프트를 한번 검수한 뒤 vscode Claude에게 출력해 명세서 작성. ex) Frontend.md, Backend.md

## 바이브 코딩의 시작

<img width="550" height="558" alt="스크린샷 2026-07-09 오전 11 03 34" src="https://github.com/user-attachments/assets/34a3ab88-7136-4f4e-a117-687b8e310085" />

## DB 명세서 작성

> DB.md(DB 명세서)를 적을 수 있는 프롬프트를 작성해줘. 테이블은 주문, 주문 구성품, 메뉴, 카테고리, 관리자, 관리자의 업데이트 기록, 장바구니가 있어야해. 메뉴를 등록할땐 수량도 입력해야하고, 메뉴는 세트 메뉴인지 단품 메뉴인지, 주문 번호는 101번부터 시작해야해. 상위 1%의 DB 아키텍쳐 설계자로써 DB 개발자가 봤을때 바로 DB 개발을 시작할 수 있을 정도의 DB.md를 작성 할 수 있는 프롬프트를 작성해줘. 또한 결정을 해야할때 나에게 물어보고 결정을 해.

[DB_명세서_프롬프트 복사본.md](https://github.com/user-attachments/files/29829750/DB_._.md)


# DB 명세서 작성 프롬프트

아래 프롬프트를 Claude에 붙여넣어 `DB.md`를 생성하세요.

---

## 프롬프트

```
당신은 상위 1%의 데이터베이스 아키텍처 설계자입니다.
아래 요구사항을 바탕으로, 실제 DB 개발자가 보고 즉시 개발을 시작할 수 있는 수준의 완성도 높은 DB.md 명세서를 작성하세요.

---

## 프로젝트 개요

- 서비스명: 버거킹 키오스크 시스템
- DBMS: MySQL 8.x
- 목적: 고객이 키오스크에서 메뉴를 선택·주문하고, 관리자가 메뉴를 관리하는 시스템

---

## 설계 원칙 및 공통 규칙

- 모든 테이블은 `created_at`, `updated_at` 컬럼을 포함한다 (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- 논리 삭제(soft delete)가 필요한 테이블은 `deleted_at` (TIMESTAMP, NULLABLE) 컬럼을 포함한다
- PK는 별도 명시가 없으면 `BIGINT UNSIGNED AUTO_INCREMENT`로 설정한다
- 외래 키는 명시적으로 정의하고, ON DELETE / ON UPDATE 정책을 반드시 명시한다
- 문자열은 가변 길이(`VARCHAR`)를 원칙으로 하며, 고정 길이가 필요한 경우 `CHAR`를 사용한다
- 금액은 `DECIMAL(10, 0)` (원 단위, 소수점 없음)으로 저장한다
- ENUM은 향후 확장성을 고려해 컬럼별 허용 값과 기본값을 명시한다
- 인덱스는 조회 패턴을 분석하여 필요한 곳에 명시한다
- 모든 컬럼에 명확한 `COMMENT`를 달아 역할을 설명한다

---

## 요구 테이블 목록 및 상세 요구사항

### 1. `categories` (카테고리)
- 메뉴를 분류하는 카테고리 테이블
- 필드: id, name(카테고리명), display_order(노출 순서, 기본값 0), is_active(활성 여부), created_at, updated_at
- 논리 삭제 불필요 (is_active로 관리)

### 2. `menus` (메뉴)
- 버거킹의 개별 상품을 저장하는 테이블
- 필드: id, category_id(FK), name(메뉴명), description(설명, NULLABLE), price, image_url(NULLABLE), is_set(세트 메뉴 여부, BOOLEAN), quantity(재고 수량, 0 이상), is_active(판매 활성 여부), created_at, updated_at, deleted_at
- `is_set = TRUE`인 메뉴는 `set_menu_items` 테이블을 통해 구성 단품들을 정의함
- `quantity = 0`이면 품절 처리 (재고 소진 시 주문 불가)
- category_id는 categories 테이블 참조 (ON DELETE RESTRICT)

### 3. `set_menu_items` (세트 메뉴 구성)
- 세트 메뉴와 그 구성 단품들의 관계를 정의하는 매핑 테이블
- 필드: id, set_menu_id(FK → menus.id, is_set=TRUE인 메뉴), component_menu_id(FK → menus.id, 단품 메뉴), quantity(해당 구성품 수량, 기본값 1), created_at, updated_at
- set_menu_id와 component_menu_id에 복합 UNIQUE 제약 조건 적용
- 세트 삭제 시 구성 항목도 함께 삭제 (ON DELETE CASCADE)

### 4. `orders` (주문)
- 고객의 주문 건을 저장하는 테이블
- 주문 번호(`order_number`)는 고객에게 노출되는 번호로, **101번부터 시작**하고 1씩 증가한다
  - order_number는 AUTO_INCREMENT가 아니라 애플리케이션 레벨에서 채번 (또는 별도 시퀀스 테이블 활용 방안 명시)
  - order_number는 UNIQUE 제약 조건 적용
- 결제 기능은 구현하지 않으므로, 주문 생성 즉시 상태가 `COMPLETED`로 설정된다
- 필드: id, order_number(SMALLINT UNSIGNED, 101~999 순환 또는 무한 증가 방식 선택하여 명시), status(ENUM: COMPLETED / CANCELLED, 기본값 COMPLETED), total_price, session_id(비회원 세션 식별자, VARCHAR), created_at, updated_at
- order_number가 999를 넘을 경우 처리 방침도 명시할 것

### 5. `order_items` (주문 구성품)
- 주문 1건에 포함된 개별 메뉴 항목
- 필드: id, order_id(FK), menu_id(FK), menu_name(주문 시점 메뉴명 스냅샷), menu_price(주문 시점 단가 스냅샷), quantity, subtotal(= menu_price × quantity), created_at
- menu_name, menu_price는 주문 후 메뉴 정보가 변경되어도 주문 이력이 보존되도록 스냅샷으로 저장한다
- order_id: ON DELETE CASCADE

### 6. `carts` (장바구니 세션)
- 비회원 세션 기반의 장바구니 헤더 테이블
- 필드: id, session_id(VARCHAR, UNIQUE, 키오스크 세션 식별자), created_at, updated_at
- 하나의 세션 = 하나의 cart (session_id에 UNIQUE 제약 조건)
- 주문 완료 또는 세션 만료 시 해당 cart 및 연결된 cart_items 전체 삭제
- session_id에 인덱스 필수

### 7. `cart_items` (장바구니 항목)
- cart에 담긴 개별 메뉴 항목 테이블
- 필드: id, cart_id(FK → carts.id), menu_id(FK → menus.id), quantity(1 이상), created_at, updated_at
- cart_id + menu_id에 복합 UNIQUE 제약 조건 적용 (동일 메뉴는 수량만 변경)
- cart 삭제 시 cart_items도 함께 삭제 (ON DELETE CASCADE)
- cart_id에 인덱스 필수

### 8. `admins` (관리자)
- 키오스크 백오피스 관리자 계정 테이블 (단일 관리자, 권한 구분 없음)
- PK는 `username` (VARCHAR)으로 설정한다. 별도 숫자 id 컬럼을 두지 않는다.
- 필드: username(PK, VARCHAR), password_hash(bcrypt 해시), name(실명), last_login_at(NULLABLE), is_active(계정 활성 여부), created_at, updated_at, deleted_at
- role 컬럼 없음 (단일 관리자 구조이므로 권한 분리 불필요)
- admin_audit_logs의 FK는 admins.username을 참조한다
- 논리 삭제 적용 (deleted_at)

### 9. `admin_audit_logs` (관리자 액션 로그)
- 모든 관리자 액션을 기록하는 감사 로그 테이블
- 기록 대상: 로그인/로그아웃, 메뉴 등록/수정/삭제, 카테고리 변경, 주문 취소 등 모든 관리자 액션
- 필드: id, admin_id(FK → admins.id, ON DELETE SET NULL), action(VARCHAR, 액션 코드 예: MENU_CREATE / MENU_UPDATE / MENU_DELETE / LOGIN / LOGOUT 등), target_type(VARCHAR, 대상 리소스 타입 예: menu / category / order), target_id(BIGINT, 대상 리소스 ID, NULLABLE), before_value(JSON, 변경 전 데이터, NULLABLE), after_value(JSON, 변경 후 데이터, NULLABLE), ip_address(VARCHAR, NULLABLE), created_at
- 이 테이블은 수정/삭제하지 않으므로 updated_at, deleted_at 없음
- admin_id, action, created_at에 각각 인덱스 적용

---

## 명세서에 포함해야 할 항목

명세서는 아래 구조를 따라 작성한다:

1. **개요**: 프로젝트 목적, DBMS 버전, 설계 원칙 요약
2. **ERD 다이어그램**: Mermaid `erDiagram` 문법으로 전체 테이블 관계 표현
3. **테이블 목록**: 전체 테이블 일람표 (테이블명 / 한글명 / 설명)
4. **테이블 상세 명세**: 각 테이블마다
   - 테이블 설명 (용도, 설계 의도)
   - 컬럼 명세표: 컬럼명 / 타입 / NULL 여부 / 기본값 / 설명
   - 제약 조건 (PK, UK, FK, CHECK)
   - 인덱스 목록
   - CREATE TABLE DDL (실행 가능한 MySQL 8.x 문법)
5. **관계 정의**: 테이블 간 관계 설명 (1:N, N:M 등)
6. **order_number 채번 전략**: 101번 시작, 순환 또는 무한 증가 방식 선택 및 근거 설명
7. **초기 데이터 (Seed SQL)**: 카테고리 3개 이상, 단품 메뉴 5개 이상, 세트 메뉴 2개 이상, 관리자 계정 1개 (SUPER_ADMIN)
8. **설계 시 주요 결정 사항**: 스냅샷 저장 이유, 세션 기반 장바구니 이유 등 설계 근거

---

출력 형식은 마크다운(.md)으로 작성하고, 실제 개발자가 문서만 보고 DB를 구축할 수 있도록 빠짐없이 상세하게 작성하세요.
```
