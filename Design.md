# 버거킹 키오스크 시스템 - 디자인 명세서 (Design.md)

> 본 문서는 `Kiosk/ui/*.html` 와이어프레임(고객 키오스크 4화면 + 관리자 백오피스 8화면, 공통 `css/style.css`)을 분석해 도출한 실제 프로덕션 디자인 명세서다. 와이어프레임은 레이아웃/정렬/컴포넌트 구조만 정의된 상태(색상·타이포·보더 거의 없음)이며, 본 문서가 그 위에 브랜드 비주얼 시스템을 얹는다.

---

## 1. 개요

### 1.1 디자인 철학

- **기능이 형태를 이끈다**: 와이어프레임에서 이미 확정된 정보 구조(카테고리 → 메뉴 → 상세 → 장바구니 → 주문완료, 그리고 사이드바 기반 8개 관리 화면)를 그대로 유지하고, 그 위에 신뢰감 있는 그린 브랜드 톤을 입힌다.
- **키오스크는 "빠르고 크고 명확하게"**: 고객은 매장에 서서 짧은 시간 안에 의사결정을 해야 한다. 터치 영역은 넉넉하게, 가격/수량 같은 숫자는 즉시 읽히게 설계한다.
- **백오피스는 "조용하고 조밀하게"**: 관리자는 매일 반복적으로 표를 훑고 값을 수정한다. 화려한 장식보다 스캔 속도(정렬 규칙)와 예측 가능한 버튼 위치가 우선이다.
- **그린 베이스의 일관된 톤앤매너**: 프랭크 버거(Frank Burger)의 브랜드 그린을 Primary로 채택해, 기존에 흔한 버거 프랜차이즈의 레드/옐로 톤과 차별화된 신선하고 신뢰감 있는 무드를 만든다.

### 1.2 브랜드 아이덴티티

| 항목 | 내용 |
|---|---|
| 서비스명 | 버거킹 키오스크 시스템 |
| 대표 컬러 | Frank Burger 브랜드 그린 (`#1F6B47`) |
| 톤앤매너 | 그린 베이스 · 미니멀 · 고대비 타이포 · 넉넉한 여백 |
| 보조 무드 | 따뜻한 톤(Secondary Tan)으로 그린의 차가움을 상쇄, 그릴/베이커리 느낌 보강 |

### 1.3 적용 화면 목록 (와이어프레임 기준)

| 구분 | 화면 | 파일 |
|---|---|---|
| 키오스크(고객) | 메뉴 목록 | `index.html` |
| 키오스크(고객) | 메뉴 상세 | `menu-detail.html` |
| 키오스크(고객) | 장바구니 | `cart.html` |
| 키오스크(고객) | 주문 완료 | `order-complete.html` |
| 관리자 | 로그인 | `admin-login.html` |
| 관리자 | 대시보드 | `admin-dashboard.html` |
| 관리자 | 카테고리 관리 | `admin-categories.html` |
| 관리자 | 메뉴 관리 (세트 구성 포함) | `admin-menus.html` |
| 관리자 | 재고 관리 | `admin-inventory.html` |
| 관리자 | 주문 관리 | `admin-orders.html` |
| 관리자 | 매출 조회 | `admin-sales.html` |
| 관리자 | 감사 로그 | `admin-audit-logs.html` |

---

## 2. 컬러 시스템

### 2.1 팔레트

| 역할 | 토큰명 | HEX | 사용처 |
|---|---|---|---|
| Primary | `--color-primary` | `#1F6B47` | 메인 액션 버튼(담기/저장/로그인), 사이드바 활성 메뉴, 활성 탭, 체크박스/토글 On, 포커스 강조 |
| Primary Dark | `--color-primary-dark` | `#154A31` | Primary 버튼 hover/active, 관리자 헤더·사이드바 배경, 키오스크 헤더 배경 |
| Primary Light | `--color-primary-light` | `#E3F1EA` | 배경 틴트(선택된 카테고리 탭 배경), 테이블 선택 행 하이라이트, Success 뱃지 배경 |
| Secondary | `--color-secondary` | `#B98A46` | 서브 액션 버튼, "SET" 뱃지, 세컨더리 태그, 장식 포인트(주문번호 박스 보더 등) |
| Secondary Light | `--color-secondary-light` | `#F6ECDC` | Secondary 뱃지 배경 |
| Danger | `--color-danger` | `#D6403B` | 삭제·취소·주문취소·유효성 에러 텍스트/보더 |
| Danger Light | `--color-danger-light` | `#FBE9E8` | Danger 뱃지/알림 배경, CANCELLED 뱃지 배경 |
| Warning | `--color-warning` | `#E8A93B` | 품절 뱃지, 재고 임박 경고, 주의 알림 |
| Warning Light | `--color-warning-light` | `#FBF1DD` | Warning 뱃지 배경 |
| Success | `--color-success` | `#2E8B57` | 완료(COMPLETED) 뱃지, 저장 성공 토스트 — Primary와 같은 계열이나 살짝 밝은 그린으로 구분 |
| Success Light | `--color-success-light` | `#E3F1EA` | Success 뱃지 배경 (Primary Light와 동일 토큰 재사용) |
| Neutral 50 | `--color-neutral-50` | `#F7F8F7` | 페이지 배경(연그레이, 아주 옅은 그린 틴트) |
| Neutral 100 | `--color-neutral-100` | `#EFF1EF` | 구분선 배경, 비활성 뱃지 배경, hover된 회색 행 |
| Neutral 200 | `--color-neutral-200` | `#E1E4E1` | 테두리(보더) 기본값 |
| Neutral 300 | `--color-neutral-300` | `#C9CDC9` | 비활성 보더, placeholder 텍스트 |
| Neutral 400 | `--color-neutral-400` | `#A6ABA6` | 아이콘 비활성색, 캡션 보조 텍스트 |
| Neutral 500 | `--color-neutral-500` | `#7C817C` | 보조 텍스트(subtitle, hint) |
| Neutral 600 | `--color-neutral-600` | `#5B605B` | 라벨 텍스트 |
| Neutral 700 | `--color-neutral-700` | `#40453F` | 본문 텍스트 서브 |
| Neutral 800 | `--color-neutral-800` | `#262A25` | 본문 텍스트 기본 |
| Neutral 900 | `--color-neutral-900` | `#14170F` | 헤드라인 텍스트, 최고 대비 텍스트 |
| Background | `--color-bg` | `#F7F8F7` (Neutral 50) | 페이지 전체 배경 |
| Surface | `--color-surface` | `#FFFFFF` | 카드, 패널, 모달, 테이블, 인풋 배경 |

### 2.2 사용 원칙

- 그린(Primary)은 화면당 **핵심 액션 1곳**에만 강하게 사용한다(예: 화면에 버튼이 여러 개면 그 중 Primary는 1개). 나머지는 Secondary/Ghost로 낮춘다.
- Danger는 삭제·취소류에만 배타적으로 쓴다. 다른 용도로 빨강을 쓰지 않는다.
- 뱃지는 배경(Light 톤) + 텍스트(진한 톤) 조합만 사용하고, 배경색 단독 채움(Solid fill)은 쓰지 않는다 — 낮은 채도로 표 스캔 시 피로도를 낮춘다.
- Success와 Primary가 같은 그린 계열인 이유: 브랜드가 이미 그린이므로 "정상/완료" 상태를 별도 색으로 분리하지 않고 브랜드 컬러를 재사용해 일관성을 높인다.

---

## 3. 타이포그래피

### 3.1 폰트 패밀리

| 용도 | 폰트 | 비고 |
|---|---|---|
| 한글 + 라틴 UI 전체 | **Pretendard** (Variable) | 가변 폰트, 웨이트 100~900 지원, 한글 가독성 우수 |
| 숫자 전용(금액/수량/ID) | **Pretendard** + `font-variant-numeric: tabular-nums` | 별도 폰트를 섞지 않고 Pretendard의 표 숫자(tabular figures) 기능으로 자릿수 정렬 |
| 코드/JSON (감사 로그 상세) | **JetBrains Mono** | `.code-box`(before_value/after_value) 전용 |

```css
font-family: "Pretendard Variable", Pretendard, -apple-system, "Segoe UI", sans-serif;
```

### 3.2 타입 스케일

| 레벨 | 크기 (px / rem) | Weight | Line-height | 용도 |
|---|---|---|---|---|
| Display | 32px / 2rem | 700 | 1.25 | 주문완료 화면 주문번호(`.order-number-box .number`) |
| H1 | 24px / 1.5rem | 700 | 1.3 | 관리자 페이지 타이틀(`.admin-topbar h1`), 키오스크 상세 타이틀 |
| H2 | 18px / 1.125rem | 600 | 1.4 | 패널 헤더(`.panel-header h2`) |
| H3 | 16px / 1rem | 600 | 1.4 | 섹션 라벨 강조, 카드 제목 |
| Body Large | 16px / 1rem | 400 | 1.6 | 키오스크 본문(가격, 메뉴 설명) — 키오스크는 원거리 시인성을 위해 기본 Body보다 한 단계 크게 |
| Body | 14px / 0.875rem | 400 | 1.6 | 관리자 테이블 본문, 폼 입력값 |
| Body Small | 13px / 0.8125rem | 400 | 1.5 | 서브 텍스트(`.subtitle`, `.tag-row`) |
| Caption | 12px / 0.75rem | 500 | 1.4 | 뱃지 텍스트, 테이블 헤더(`th`), `.hint` |
| Label | 11px / 0.6875rem | 600 | 1.3 | 폼 라벨(`label`), 그룹 라벨(`.group-label`) — 대문자 + `letter-spacing: 0.04em` |

### 3.3 숫자 표기 규칙

- 모든 금액/수량/건수/ID 컬럼은 `font-variant-numeric: tabular-nums;`를 강제 적용해 자릿수가 바뀌어도 열이 흔들리지 않게 한다.
- 금액은 천 단위 콤마 + "원" 접미사(`27,700원`), 수량은 정수만(`14건`, `x2`).
- Display/H1 레벨의 숫자(주문번호, KPI 값)는 `font-weight: 700` 고정.

---

## 4. 스페이싱 시스템

4px 베이스 스케일(`spacing-N = N × 4px`).

| 토큰 | 값 | 대표 용도 |
|---|---|---|
| `spacing-1` | 4px | 아이콘-텍스트 간격, 뱃지 내부 세로 패딩 |
| `spacing-2` | 8px | 버튼 내부 세로 패딩(SM), 폼 라벨-인풋 간격 |
| `spacing-3` | 12px | 버튼 내부 세로 패딩(MD), 테이블 셀 패딩 |
| `spacing-4` | 16px | 카드 내부 패딩, 컴포넌트 간 기본 간격, 폼 필드 간 간격 |
| `spacing-5` | 20px | 키오스크 바디 패딩, 메뉴 그리드 gap |
| `spacing-6` | 24px | 패널 내부 패딩, 섹션 간 간격(소) |
| `spacing-8` | 32px | 섹션 간 간격(대), KPI 카드 그리드 gap |
| `spacing-10` | 40px | 관리자 사이드바 상단 여백 |
| `spacing-12` | 48px | 로그인 카드 내부 패딩 |
| `spacing-16` | 64px | 빈 상태(Empty State) 상하 패딩, 주문완료 화면 상단 여백 |

**적용 원칙**

- 컴포넌트 내부 패딩: `spacing-2` ~ `spacing-4` (버튼/인풋/뱃지)
- 컴포넌트 간 간격(같은 그룹 내): `spacing-3` ~ `spacing-5` (테이블 행, 폼 필드, 카드 그리드 gap)
- 섹션 간 간격(패널과 패널, 블록과 블록): `spacing-6` ~ `spacing-8`
- 페이지 레벨 여백(사이드바-콘텐츠, 상단 여백): `spacing-8` ~ `spacing-16`

---

## 5. 그리드 & 레이아웃

반응형 미지원(키오스크 전용 해상도 고정 / 관리자 데스크탑 전용).

### 5.1 키오스크(고객) 레이아웃

| 항목 | 값 |
|---|---|
| 기준 해상도 | 1080 × 1920 (세로, 터치스크린 키오스크 표준) |
| 콘텐츠 최대 폭 | 480pt 논리 폭 기준 설계 후 1080px로 2.25배 스케일(디자인 시안은 480pt 그리드로 작업, 개발 시 실기기 배율 적용) |
| 헤더 높이 (`.kiosk-header`) | 64px, 좌우 패딩 `spacing-5`(20px) |
| 탭 바 높이 (`.tabs`) | 48px |
| 콘텐츠 영역 패딩 (`.kiosk-body`) | `spacing-5`(20px), 하단은 바텀바에 가리지 않도록 `spacing-16`(64px)+ 확보 |
| 바텀 액션 바 높이 (`.bottom-bar`) | 80px, 항상 화면 하단 고정(sticky) |
| 메뉴 그리드 (`.menu-grid`) | 2열 그리드, 컬럼 gap `spacing-5`(20px), 카드 비율 1:1 이미지 + 정보 영역 |
| **최소 터치 영역** | **44×44px 이상** — 수량 스테퍼 버튼, 탭, 카드 전체, 아이콘 버튼 모두 최소 44px 확보. 주요 CTA(담기/주문하기)는 56px 이상 높이 권장 |
| 주요 CTA 높이 | 56px (바텀바 내 `.btn`) |

### 5.2 관리자(백오피스) 레이아웃

| 항목 | 값 |
|---|---|
| 전체 구조 | 사이드바 + 콘텐츠 2단 그리드 (`.admin { grid-template-columns: 240px 1fr }`) |
| 사이드바 너비 | 240px, 배경 Primary Dark, 상단 고정(sticky, `height:100vh`) |
| 헤더(topbar) 높이 | `.admin-topbar` 콘텐츠 영역 내부 상단 배치, 실질 높이 72px (제목+서브타이틀+하단 보더 포함) — 별도 전역 고정 헤더 없음(사이드바가 항상 노출되므로 topbar는 각 콘텐츠 영역 상단에 위치) |
| 콘텐츠 좌우 패딩 (`.admin-main`) | 40px |
| 콘텐츠 최대 너비 | 1440px (그 이상 넓은 모니터에서는 좌우 여백으로 흡수, 중앙 정렬하지 않고 좌측 정렬 유지 — 사이드바 고정형이므로) |
| KPI 카드 그리드 | 4열 (`.kpi-grid`), gap `spacing-8`(32px). 매출 화면처럼 2개만 필요할 때는 2열로 축소 |
| 패널(Panel) 간 간격 | `spacing-6`(24px) |
| 테이블 행 높이 | 44px (패딩 `spacing-3` 10px 상하 기준) |

---

## 6. 정렬 규칙 (Alignment Rules)

모든 테이블/목록/폼에 예외 없이 아래 규칙을 적용한다. 헤더 셀과 데이터 셀 정렬은 항상 동일해야 한다.

| 데이터 유형 | 정렬 | 유틸리티 클래스 | 적용 예시(와이어프레임 기준) |
|---|---|---|---|
| 금액, 수량, 건수 등 자릿수 가변 수치 | **우측 정렬** | `.text-right` / 기존 `.num` | 결제금액, 가격, 재고, 소계, "N건", 매출 KPI 값 |
| 순번, ID, 코드 등 자릿수 고정 일련번호 | **가운데 정렬** | `.text-center` | 주문번호(228), 메뉴 ID, 로그 ID, 대상 ID |
| 단순 텍스트, 이름, 설명 | **좌측 정렬** | `.text-left` (기본값) | 메뉴명, 카테고리명, 관리자명(admin), 세션 ID, 설명 |
| 상태 뱃지 / 아이콘 전용 셀 | **가운데 정렬** | `.text-center` | 상태(판매중/품절/COMPLETED/CANCELLED), 활성 여부, 수정·삭제 아이콘 버튼 |

### 6.1 화면별 적용 지점

| 화면 | 컬럼 | 정렬 |
|---|---|---|
| `admin-dashboard.html` 최근 주문 테이블 | 주문번호 | 가운데 |
| | 결제금액 | 우측 |
| | 상태(뱃지) | 가운데 |
| | 주문시각 | 좌측 |
| `admin-categories.html` 목록 | 노출순서 | 가운데 |
| | 이름 | 좌측 |
| | 소속 메뉴(건수) | 우측 |
| | 상태(뱃지) | 가운데 |
| | 등록일 | 좌측 |
| | 수정/삭제 아이콘 | 가운데 |
| `admin-menus.html` 목록 | 이름 | 좌측 |
| | 카테고리 | 좌측 |
| | 구분(단품/세트 뱃지) | 가운데 |
| | 가격 | 우측 |
| | 재고 | 우측 |
| | 상태(뱃지) | 가운데 |
| | 액션(구성품/수정/삭제) | 가운데 |
| `admin-inventory.html` | 메뉴명 | 좌측 |
| | 카테고리 | 좌측 |
| | 현재 재고 | 우측 |
| | 상태(뱃지) | 가운데 |
| | 수정 입력값 | 우측(숫자 입력이므로 입력 텍스트도 우측 정렬) |
| `admin-orders.html` 목록 | 주문번호 | 가운데 |
| | 결제금액 | 우측 |
| | 상태 | 가운데 |
| | 주문시각 | 좌측 |
| | 상세 버튼 | 가운데 |
| `admin-orders.html` 상세 라인 | 메뉴명 | 좌측 |
| | 단가/수량/소계 | 우측 |
| `admin-sales.html` | 주문번호 | 가운데 |
| | 결제금액 / 일 매출 / 주문건수 | 우측 |
| | 날짜/주문시각 | 좌측 |
| `admin-audit-logs.html` | 관리자명 | 좌측 |
| | 액션(뱃지) | 가운데 |
| | 대상 | 좌측 |
| | 대상 ID | 가운데 |
| | IP | 좌측 |
| | 시각 | 좌측 |
| | 상세 버튼 | 가운데 |
| 키오스크 `cart.html` 요약 | "상품 수" 값, "총 결제금액" 값 | 우측 |

---

## 7. 버튼 배치 규칙 (Button Placement Rules)

1. **추가(등록) 버튼**은 해당 섹션(Panel)의 **우측 상단**, 즉 `.panel-header` 오른쪽에 배치한다. (예: "카테고리 등록", "단품 메뉴 등록"/"세트 메뉴 등록")
2. **수정 버튼**은 해당 행(테이블 row) 또는 상세 영역의 **우측**에 배치한다.
3. **삭제 버튼**은 수정 버튼의 **오른쪽**(같은 행) 또는 **아래**(상세 화면에서 별도 액션 그룹일 때)에 배치한다. 행 내 순서는 항상 `[수정] [삭제]`.
4. **폼 제출 버튼(저장/확인)**은 폼 영역의 **우측 하단**에 배치한다.
5. **취소/닫기 버튼**은 제출 버튼의 **왼쪽**에 배치한다.
6. 버튼 그룹 내 시각적 순서는 항상 `[취소/보조] [Primary]` — 오른쪽 끝에 Primary가 오도록 정렬한다.
7. 파괴적 액션(삭제, 주문취소)은 위 순서 규칙과 별개로 **Danger 색상 + 확인 모달**을 거치도록 한다(8.6 Modal 참조).

### 7.1 화면별 적용 지점

| 화면 | 배치 |
|---|---|
| `admin-categories.html` | 목록 패널 헤더 우측: `+ 카테고리 등록`(Primary). 각 행 우측: `[수정(Ghost)] [삭제(Danger)]`. 하단 폼 패널: 우측 하단에 `[취소(Secondary)] [저장(Primary)]` |
| `admin-menus.html` | 목록 패널 헤더 우측: `[세트 메뉴 등록(Secondary)] [단품 메뉴 등록(Primary)]` — Primary는 가장 오른쪽. 각 행 우측: `[구성품(Ghost, 세트만)] [수정(Ghost)] [삭제(Danger)]`. 세트 구성 패널 하단: `[구성품 추가(Primary)]` 단독 우측 배치 |
| `admin-inventory.html` | 행별 인라인 수정이므로 각 행 우측에 `[저장(Primary, Small)]` 단독 배치 (삭제 없음) |
| `admin-orders.html` | 상세 패널 헤더 우측: `주문 취소`(Danger, 목록 삭제와 달리 상세 화면 상단 배치) |
| `admin-sales.html` / `admin-audit-logs.html` | 등록/수정 없음 → 버튼 배치 규칙 미적용, 필터 토글(일별/월별/연도별)만 좌측 툴바에 위치 |
| 키오스크 `menu-detail.html`, `cart.html` | 바텀바는 예외적으로 좌측 Secondary(계속 담기/뒤로) + 우측 Primary(담기/주문하기) — 모바일 키오스크 UX 관례상 "앞으로 나아가는" 액션을 항상 오른쪽(엄지 도달 영역)에 배치 |
| `admin-login.html` | 단일 Primary 버튼("로그인")만 존재, 폼 하단 전체 너비 배치(취소 버튼 없음 — 로그인은 되돌릴 대상이 없는 첫 화면이므로) |

---

## 8. 컴포넌트 명세

### 8.1 Button

**용도**: 모든 화면의 주요/보조 액션.

| Variant | 배경 | 텍스트 | 보더 | 용도 |
|---|---|---|---|---|
| Primary | `--color-primary` | `#FFFFFF` | none | 담기, 주문하기, 저장, 로그인, 구성품 추가 |
| Secondary | `--color-surface` | `--color-primary-dark` | 1px solid `--color-primary` | 계속 담기, 세트 메뉴 등록(보조 등록) |
| Danger | `--color-danger-light` | `--color-danger` | 1px solid `--color-danger` | 삭제, 주문 취소 |
| Ghost | `transparent` | `--color-neutral-700` | 1px solid `--color-neutral-200` | 수정, 상세, 전체 보기, 필터 토글 |

**상태(States)**

| 상태 | 처리 |
|---|---|
| Default | 위 표 기준 |
| Hover | 배경 8% 어둡게(Primary→Primary Dark로 치환), Ghost/Secondary는 배경을 `--color-neutral-50`/`--color-primary-light`로 채움 |
| Active(누름) | 배경 추가 6% 어둡게 + `transform: scale(0.98)` |
| Focus | `outline: 2px solid --color-primary; outline-offset: 2px;` |
| Disabled | `opacity: 0.4; cursor: not-allowed;` 배경/텍스트 색은 유지(투명도로만 표현) |

**크기(Size Variants)**

| 크기 | Height | Padding(수평) | Font | 사용처 |
|---|---|---|---|---|
| SM | 32px | 12px | Caption(12px)/600 | 테이블 행 내 액션(`.btn-admin.small`) |
| MD | 40px | 16px | Body(14px)/600 | 관리자 일반 버튼(`.btn-admin`) |
| LG | 48px | 20px | Body Large(16px)/700 | 로그인 버튼, 폼 저장 버튼 |
| XL(키오스크 전용) | 56~64px | 24px | H3(16~18px)/700 | 키오스크 바텀바 CTA (`.btn`) |

**공통 스타일**: `border-radius: 10px`(키오스크는 12px로 살짝 크게), `box-shadow: none`(플랫 디자인, 그림자는 모달/카드 부상 요소에만 사용).

### 8.2 Input Field (Text / Number / Search)

- 사용 화면: 로그인(`.field input`), 폼(`.form-grid input`), 인라인 재고 수정, 검색창.
- 스타일: 배경 `--color-surface`, 보더 1px solid `--color-neutral-200`, `border-radius: 8px`, padding `10px 12px`, Body(14px)/400.
- 상태: Hover(보더 `--color-neutral-300`), Focus(보더 `--color-primary` + `box-shadow: 0 0 0 3px --color-primary-light`), Disabled(배경 `--color-neutral-100`, 텍스트 `--color-neutral-400`), Error(보더 `--color-danger` + 하단에 Danger 텍스트 Caption 크기 에러 메시지).
- Number 타입: 텍스트 정렬 **우측**(6장 정렬 규칙과 동일 원칙 적용), 재고 인라인 입력이 대표 사례.
- Search 타입: 좌측에 검색 아이콘(16px) 내장, placeholder는 `--color-neutral-400`.

### 8.3 Select / Dropdown

- 사용 화면: 카테고리/상태/기간 필터(`.field-input` select), 폼의 노출 여부 선택.
- 스타일: Input Field와 동일한 박스 스타일 + 우측 Chevron-down 아이콘(16px, `--color-neutral-500`).
- 상태: Input Field와 동일(Hover/Focus/Disabled).
- 목록(Option Panel) 스타일: `--color-surface` 배경, `box-shadow: 0 4px 12px rgba(20,23,15,0.12)`, 각 옵션 hover 시 `--color-primary-light` 배경.

### 8.4 Table (헤더/행/페이지네이션)

- 사용 화면: 관리자 8화면 전체 중 6개(대시보드/카테고리/메뉴/재고/주문/매출/감사로그).
- 헤더(`th`): 배경 `--color-neutral-50`, 텍스트 `--color-neutral-500`, Caption(12px)/600, 대문자+`letter-spacing:0.04em`, 하단 보더 2px solid `--color-neutral-800`(헤더만 굵게 강조), 높이 40px, 정렬은 6장 규칙 준수.
- 행(`tr`): 높이 44px, 짝수 행 배경 `--color-neutral-50`(zebra, 옵션) 또는 전 행 흰색 + 1px 구분선(`--color-neutral-100`) 중 택1 — 본 서비스는 **구분선 방식**(zebra 미사용, 데이터 밀도가 낮아 zebra는 과함).
- 행 Hover: 배경 `--color-primary-light`(선택된 행 하이라이트 겸용).
- 셀(`td`): padding `10px 12px`, Body(14px)/400, 정렬 6장 규칙.
- 페이지네이션(`.pagination`): 버튼 28×28px, `border-radius: 6px`, 기본 보더 `--color-neutral-200`, 활성 페이지는 배경 `--color-primary` + 텍스트 흰색.
- 빈 상태: 표 내부에 행 대신 중앙 정렬된 안내 문구 + 아이콘(예: "표시할 데이터가 없습니다") 1행 병합 처리.

### 8.5 Badge / Tag

- 사용 화면: 모든 관리자 목록의 상태 컬럼(`.tag`), 키오스크 메뉴 카드의 `[품절]`/`[SET]` 표시.

| 상태값 | 배경 | 텍스트 | 아이콘 동반(권장) |
|---|---|---|---|
| 노출중 / 판매중 / 판매 가능 / 활성 | `--color-success-light` | `--color-success` | Check(12px) |
| 숨김 / 비활성 | `--color-neutral-100` | `--color-neutral-500` | EyeOff(12px) |
| 품절 | `--color-warning-light` | `#8A5A00`(Warning 대비 강화용 다크 톤) | AlertTriangle(12px) |
| COMPLETED | `--color-success-light` | `--color-success` | Check(12px) |
| CANCELLED | `--color-danger-light` | `--color-danger` | X(12px) |
| SET (세트 뱃지) | `--color-secondary-light` | `#7A5A22`(Secondary 다크 톤) | — |
| 액션 코드(MENU_CREATE 등, 감사로그) | `--color-neutral-100` | `--color-neutral-700` | — (모노스페이스 Caption) |

- 공통 스타일: `border-radius: 999px`(pill), padding `2px 10px`, Caption(12px)/600, `display:inline-flex; align-items:center; gap:4px;`.
- **접근성 원칙**: 색만으로 상태를 구분하지 않는다 — 반드시 텍스트 또는 아이콘을 함께 표기한다(위 표의 "아이콘 동반" 참조, WCAG 1.4.1 준수).

### 8.6 Card

두 가지 변형이 존재한다.

**Menu Card** (`index.html` `.menu-card`)
- 구조: 1:1 이미지 영역(상단, `border-radius: 12px 12px 0 0`) → 정보 영역(패딩 12px: 뱃지 행 → 이름 → 가격).
- 배경 `--color-surface`, 보더 1px solid `--color-neutral-200`, `border-radius: 12px`, `box-shadow: 0 1px 3px rgba(20,23,15,0.06)`.
- 품절 상태: 이미지에 `filter: grayscale(60%); opacity: 0.6;` + 좌상단 품절 뱃지, 카드 전체 클릭 비활성화(`pointer-events: none` + 커서 not-allowed 안내는 부모 컨테이너에서 처리).
- Hover(터치 기기이므로 Active 위주): 눌렀을 때 `transform: scale(0.98)`.

**KPI Card** (관리자 대시보드/매출 `.kpi-card`)
- 배경 `--color-surface`, 보더 1px solid `--color-neutral-200`, `border-radius: 12px`, padding 20px, `box-shadow: 0 1px 3px rgba(20,23,15,0.06)`.
- 구성: Label(Caption, `--color-neutral-500`) → Value(H1, `--color-neutral-900`, 강조가 필요하면 `--color-primary`) → Delta(Caption, `--color-neutral-500` 또는 상태에 따라 Success/Danger).

### 8.7 Modal / Dialog

와이어프레임에는 없으나 삭제/취소 등 파괴적 액션의 안전장치로 반드시 필요.

- 사용처: 카테고리/메뉴 삭제 확인, 주문 취소 확인, 세트 구성품 제거 확인, 로그아웃 확인(선택).
- 구조: Overlay(`rgba(20,23,15,0.45)`) → Dialog(`--color-surface`, `border-radius: 16px`, 최대 폭 400px, padding 24px, `box-shadow: 0 20px 50px rgba(20,23,15,0.25)`).
- 구성 요소: 아이콘(경고성이면 Warning/Danger 색 원형 배경) → 타이틀(H2) → 본문 설명(Body, `--color-neutral-600`) → 버튼 그룹(우측 정렬, `[취소(Ghost)] [확인(Danger 또는 Primary)]`, 7장 버튼 규칙 그대로 적용).
- 등장 애니메이션: 11장 참조(fade-in + scale-up, 200ms).

### 8.8 Sidebar Navigation

- 사용 화면: 관리자 7화면 공통(`admin-login.html` 제외).
- 배경 `--color-primary-dark`, 텍스트 기본 `rgba(255,255,255,0.72)`.
- 그룹 라벨(`.group-label`): Label 스타일(11px/600/대문자), `rgba(255,255,255,0.45)`.
- 메뉴 아이템(`nav a`): 높이 40px, `border-radius: 8px`, 아이콘(20px) + 텍스트(Body, 500), 좌측 패딩 12px.
- 상태:
  - Default: 배경 transparent, 텍스트 `rgba(255,255,255,0.72)`
  - Hover: 배경 `rgba(255,255,255,0.08)`
  - Active(현재 화면): 배경 `--color-primary`(밝은 그린으로 대비), 텍스트 `#FFFFFF`, 좌측 3px 바(`--color-secondary`)로 이중 강조

### 8.9 Toast / Alert

와이어프레임에는 없으나 모든 저장/삭제/오류 피드백에 필수.

- 위치: 화면 상단 중앙(관리자) / 화면 상단(키오스크, 장바구니 담기 완료 피드백용).
- 구조: 아이콘(상태별 Success/Danger/Warning 색) + 메시지(Body) + 닫기 버튼(X, 16px).
- 스타일: 배경 `--color-surface`, `border-left: 4px solid` (상태색), `border-radius: 8px`, `box-shadow: 0 8px 24px rgba(20,23,15,0.15)`, padding 14px 16px.
- 유형: Success("저장되었습니다"), Danger("삭제할 수 없습니다: 소속된 메뉴가 있습니다"), Warning("재고가 얼마 남지 않았습니다").

### 8.10 Pagination

- 8.4 Table 항목 참조. 추가로 "이전/다음" 화살표 버튼(28×28px, Chevron 아이콘)을 페이지 번호 양옆에 배치, 첫/마지막 페이지에서는 Disabled 처리.

### 8.11 Form Layout

- 2컬럼 그리드(`.form-grid`, gap 16px), 라벨은 항상 인풋 **위쪽**(좌측 정렬), `.full` 클래스로 특정 필드(설명, 노출 여부, JSON 블록)만 2컬럼 전체 폭 사용.
- 필드 간 세로 간격 `spacing-4`(16px), 폼 하단 버튼 그룹까지 `spacing-6`(24px) 여백.
- 읽기 전용 필드(주문 상세, 감사 로그 상세): 라벨 아래 값만 Body 텍스트로 표시(인풋 보더 없이) — 이미 와이어프레임에서 `.form-grid`에 `<div>` 값으로 구현된 패턴을 그대로 유지.

---

## 9. 화면별 디자인 가이드

### 9.1 키오스크 — 메뉴 목록 (`index.html`)

- **목적**: 카테고리 탐색 및 메뉴 그리드 열람, 장바구니 진입.
- **레이아웃**: 헤더(로고+장바구니 카운트) → 카테고리 탭(가로 스크롤 가능) → 2열 메뉴 카드 그리드 → 하단 고정 바텀바.
- **주요 컴포넌트**: 카테고리 탭(Sidebar Navigation의 가로형 버전), Menu Card ×N, 바텀바(검색/장바구니 버튼).
- **정렬 포인트**: 카드 내부는 좌측 정렬(이름) + 가격은 카드 폭에 맞춰 좌측 정렬 유지(테이블이 아니므로 6장의 "우측 정렬"은 표에만 적용, 카드형은 예외) — 단, 향후 리스트형 뷰로 바뀔 경우 가격은 우측 정렬 원칙 적용.
- **버튼 배치**: 바텀바 좌측 Secondary("메뉴 검색"), 우측 Primary("장바구니 보기") — 7장 예외 규칙(전진 액션 우측) 적용.
- **색상 포인트**: 활성 탭 밑줄/배경에 Primary, 품절 뱃지 Warning, SET 뱃지 Secondary.
- **UX 주의사항**: 품절 메뉴는 카드 전체를 흐리게(그레이스케일 60%) 처리하고 탭 불가 상태로 만들되, 정보는 계속 노출(가격 비교는 가능하게). 카테고리에 메뉴가 없을 때는 그리드 대신 빈 상태 일러스트+"등록된 메뉴가 없습니다" 문구. 최초 진입 시 카드 그리드에 스켈레톤 로딩(회색 블록 펄스 애니메이션) 적용.

### 9.2 키오스크 — 메뉴 상세 (`menu-detail.html`)

- **목적**: 단품/세트 상세 확인 및 수량 선택 후 담기.
- **레이아웃**: 헤더(뒤로가기) → 대표 이미지(4:3) → 타이틀/가격/설명 → 세트 구성 리스트(세트일 때만) → 수량 스테퍼 → 바텀바.
- **주요 컴포넌트**: 이미지 히어로, List Box(세트 구성), Qty Stepper.
- **정렬 포인트**: 세트 구성 리스트는 좌측(구성품명) / 우측(수량 "x1") — 표가 아니어도 6장의 수치 우측 정렬 원칙을 적용한 사례.
- **버튼 배치**: 바텀바 전체 폭 Primary 1개("담기 - 가격"), 취소/이전은 헤더의 뒤로가기 아이콘이 대신함.
- **색상 포인트**: 담기 버튼 Primary, 세트 구성 리스트 배경은 Neutral 50로 살짝 구분.
- **UX 주의사항**: 품절 메뉴로 직접 진입 시(딥링크 등) 담기 버튼을 Disabled 처리하고 상단에 Warning 톤 안내 배너("현재 품절된 메뉴입니다") 노출. 수량은 재고 초과 시 스테퍼 +버튼 Disabled.

### 9.3 키오스크 — 장바구니 (`cart.html`)

- **목적**: 담은 항목 검토/수량 조절/삭제 및 주문 확정.
- **레이아웃**: 헤더 → 장바구니 아이템 리스트 → 합계 요약 박스 → 전체 비우기 링크 → 바텀바.
- **주요 컴포넌트**: Cart Item Row(이미지+이름+가격+스테퍼+삭제), Summary Box.
- **정렬 포인트**: 각 아이템의 가격(top-row 우측), 요약 박스의 "상품 수"/"총 결제금액" 값 모두 **우측 정렬**.
- **버튼 배치**: 바텀바 좌측 Secondary("계속 담기"), 우측 Primary("주문하기 - 금액"). 항목 삭제("빼기")는 각 행 우측 텍스트 링크로 축소 배치(7장의 "행 우측 삭제" 원칙 유지, 다만 시각적 무게는 Danger 컬러의 텍스트 링크로 최소화).
- **색상 포인트**: 주문하기 버튼 Primary, 빼기/비우기 링크는 Danger 톤 텍스트(배경 없음)로 은은하게.
- **UX 주의사항**: 장바구니가 비어있으면 리스트/요약 박스 대신 빈 상태(아이콘+"장바구니가 비어있습니다"+"메뉴 담으러 가기" 버튼)를 중앙 배치하고 바텀바의 "주문하기"는 Disabled. 수량 변경/삭제 시 합계는 즉시(낙관적) 갱신하되 실패 시 원복 + Toast 에러.

### 9.4 키오스크 — 주문 완료 (`order-complete.html`)

- **목적**: 주문 확정 안내 및 픽업 대기번호 고지.
- **레이아웃**: 중앙 정렬 단일 컬럼 — 체크 아이콘 → 안내문 → 주문번호 박스(강조) → 영수증 요약 → 바텀바.
- **주요 컴포넌트**: Circle Icon, Order Number Box(Display 타이포), Receipt List.
- **정렬 포인트**: 영수증 라인 항목은 좌측(메뉴명 x수량) / 우측(금액), 합계 행도 우측 정렬.
- **버튼 배치**: 바텀바 Primary 단일 버튼("처음 화면으로") — 되돌아갈 이전 액션이 없으므로 Secondary 버튼 없음.
- **색상 포인트**: 체크 원과 주문번호 박스 보더/텍스트에 Primary를 사용해 "완료"라는 성취감을 강조. 주문번호 자체는 Display 크기 + Success 컬러로 시선을 즉시 붙잡는다.
- **UX 주의사항**: 주문번호는 매장 전광판과 동일하게 크게(최소 32px, 권장 40px+) 표시. 화면은 일정 시간(예: 15초) 후 자동으로 `index.html`로 복귀하는 타임아웃을 두어 다음 고객이 바로 이용할 수 있게 한다(카운트다운 텍스트를 버튼 아래 Caption으로 노출 권장).

### 9.5 관리자 — 로그인 (`admin-login.html`)

- **레이아웃**: 화면 전체 중앙 정렬 카드(사이드바 없음).
- **정렬 포인트**: 브랜드/서브텍스트 중앙 정렬, 폼 필드는 좌측 정렬 라벨 + 전체 폭 인풋.
- **버튼 배치**: Primary 버튼 전체 폭, 폼 하단 배치(취소 버튼 없음 — 예외 케이스로 7장에 명시).
- **색상 포인트**: 배경은 Neutral 50, 카드 자체는 Surface + 옅은 그림자로 부상감 부여(다른 관리자 화면과 달리 로그인만 그림자 카드 사용 가능 — 첫인상 강조).
- **UX 주의사항**: 로그인 실패 시 비밀번호 필드 하단에 Danger 텍스트("아이디 또는 비밀번호가 올바르지 않습니다"), 필드 보더도 Danger로 전환. 로딩 중에는 버튼 텍스트를 스피너로 교체하고 Disabled.

### 9.6 관리자 — 대시보드 (`admin-dashboard.html`)

- **레이아웃**: Topbar → KPI 카드 4열 → 최근 주문 패널(표).
- **정렬 포인트**: 6장 표 참조(주문번호 가운데, 금액 우측, 상태 가운데, 시각 좌측).
- **버튼 배치**: 패널 헤더 우측 Ghost 버튼("전체 보기") — 등록 액션이 없는 화면이라 Primary 버튼 없음.
- **색상 포인트**: KPI "오늘 매출" 값에만 Primary 강조색 부여(가장 중요한 숫자), 나머지 KPI는 Neutral 900 기본 텍스트.
- **UX 주의사항**: 데이터 로딩 중 KPI 카드는 스켈레톤(회색 블록), 최근 주문 0건이면 표 대신 "오늘 아직 주문이 없습니다" 빈 상태. 매출 수치는 실시간성이 중요하므로 우측 상단에 마지막 갱신 시각 Caption 텍스트 권장.

### 9.7 관리자 — 카테고리 관리 (`admin-categories.html`)

- **레이아웃**: 목록 패널 + 등록/수정 폼 패널(하단 고정형, 모달 아님 — 와이어프레임 그대로 인라인 폼 유지).
- **정렬 포인트**: 6.1절 표 참조.
- **버튼 배치**: 목록 패널 헤더 우측 Primary("카테고리 등록"), 행 우측 `[수정(Ghost)][삭제(Danger)]`, 폼 패널 우측 하단 `[취소(Ghost)][저장(Primary)]`.
- **색상 포인트**: 노출중/숨김 뱃지는 Success/Neutral, 삭제 버튼만 Danger.
- **UX 주의사항**: 삭제 시도 시 소속 메뉴가 있으면(Backend `409 CATEGORY_HAS_MENUS`) 삭제 버튼을 막지 않고 클릭은 허용하되, 확인 모달 대신 즉시 Danger Toast로 실패 사유("소속된 메뉴가 3건 있어 삭제할 수 없습니다")를 안내(모달까지 띄우고 실패하면 2단계 실망이므로, 이 케이스는 Toast가 더 적합). 삭제 가능한 경우에만 확인 모달(8.7) 노출.

### 9.8 관리자 — 메뉴 관리 (`admin-menus.html`)

- **레이아웃**: 필터 툴바 → 목록 패널(페이지네이션 포함) → 세트 구성 관리 패널.
- **정렬 포인트**: 6.1절 표 참조. 구분(단품/세트)과 상태는 가운데, 가격·재고는 우측.
- **버튼 배치**: 목록 헤더 우측 `[세트 메뉴 등록(Secondary)][단품 메뉴 등록(Primary)]`. 세트 행에만 "구성품" Ghost 버튼이 수정/삭제보다 왼쪽에 추가(정보 조회 성격이라 수정보다 낮은 강조). 세트 구성 패널 하단 우측에 "구성품 추가" Primary 단독.
- **색상 포인트**: 세트/단품 구분 뱃지는 Secondary, 품절은 Warning.
- **UX 주의사항**: 세트 메뉴 등록은 2단계 플로우이므로, Step 1(기본정보) 저장 직후 자동으로 세트 구성 관리 패널로 스크롤/포커스 이동시켜 "구성품을 추가해야 판매 가능"임을 안내 배너(Warning Light 배경)로 알린다. 구성품이 0개인 세트는 목록에서 상태 뱃지를 "구성 필요"(Warning)로 별도 표기 권장.

### 9.9 관리자 — 재고 관리 (`admin-inventory.html`)

- **레이아웃**: 필터 툴바 → 인라인 편집 테이블.
- **정렬 포인트**: 현재 재고/수정 입력값 모두 우측 정렬(숫자), 상태 가운데.
- **버튼 배치**: 등록/삭제 없음 — 행마다 우측 끝에 Primary Small "저장"만 존재(변경 시에만 활성화, 미변경 시 Disabled로 실수 저장 방지).
- **색상 포인트**: 재고 0(품절) 행은 배경을 `--color-warning-light`로 살짝 틴트해 스캔 시 즉시 인지되게 한다.
- **UX 주의사항**: 저장 성공 시 해당 행에 짧은 Success 배경 플래시(300ms) 후 원복하는 마이크로 인터랙션으로 "저장됨"을 알린다(별도 Toast 없이도 충분). 재고를 0으로 낮추는 경우 확인 모달 없이 즉시 반영(빈번한 작업이므로 마찰 최소화).

### 9.10 관리자 — 주문 관리 (`admin-orders.html`)

- **레이아웃**: 목록 패널 + 상세 패널(같은 화면 하단, 목록에서 "상세" 클릭 시 하단 패널 내용 갱신).
- **정렬 포인트**: 6.1절 표 참조. 상세 라인 테이블은 단가/수량/소계 우측.
- **버튼 배치**: 상세 패널은 헤더 우측에 예외적으로 Danger("주문 취소")를 배치 — 일반 규칙(수정/삭제는 행 단위)과 달리 상세 화면 전체에 대한 단일 파괴적 액션이므로 패널 헤더 레벨에 둔다.
- **색상 포인트**: 상태 뱃지 COMPLETED=Success, CANCELLED=Danger. 취소 버튼은 항상 Danger.
- **UX 주의사항**: 이미 취소된 주문(`CANCELLED`)의 상세를 열면 "주문 취소" 버튼을 아예 숨기거나 Disabled + Caption("이미 취소된 주문입니다")으로 대체한다. 주문 취소는 반드시 8.7 확인 모달을 거친다(금전적 영향이 크므로 카테고리 삭제보다 마찰을 더 준다).

### 9.11 관리자 — 매출 조회 (`admin-sales.html`)

- **레이아웃**: 기간 토글 툴바 → KPI 카드(2열) → 상세 테이블 2개(일별 상세, 월별 추이).
- **정렬 포인트**: 금액/건수 컬럼 우측, 날짜/주문번호는 6.1절 규칙대로(주문번호 가운데, 날짜 좌측).
- **버튼 배치**: 등록/삭제 없음. 기간 토글(`일별/월별/연도별`)은 Segmented Control 형태로 좌측 배치, 현재 선택된 토글만 Primary, 나머지는 Ghost.
- **색상 포인트**: KPI "총 매출" 값에 Primary 강조.
- **UX 주의사항**: 데이터 없는 기간 조회 시 테이블 대신 빈 상태("해당 기간에 매출 데이터가 없습니다"). 월별/연도별 표는 향후 막대 차트로 확장 가능하도록 컴포넌트를 분리 설계(표 ↔ 차트 토글은 향후 과제로 명시만 해둠).

### 9.12 관리자 — 감사 로그 (`admin-audit-logs.html`)

- **레이아웃**: 필터 툴바(액션/기간) → 목록 패널(페이지네이션) → 상세 패널(before/after JSON).
- **정렬 포인트**: 6.1절 표 참조. 대상 ID는 가운데, 관리자명/대상/IP/시각은 좌측.
- **버튼 배치**: 등록/수정/삭제 없음(읽기 전용 화면) — "상세" Ghost 버튼만 행 우측에 존재.
- **색상 포인트**: 액션 뱃지는 파괴적 액션(MENU_DELETE, ORDER_CANCEL)만 Danger 톤, 나머지 생성/수정류는 Neutral 톤으로 낮춰 시각적 소음을 줄인다.
- **UX 주의사항**: before_value/after_value의 `.code-box`는 JetBrains Mono + 문법 강조 없이 단색(Neutral 800) 처리해 감사 목적의 "원본 그대로"라는 신뢰감을 준다. JSON이 길 경우 내부 스크롤(`max-height: 240px; overflow-y:auto;`) 적용.

---

## 10. 아이콘 시스템

### 10.1 라이브러리

**Lucide Icons** 채택 — 오픈소스, 라인 스타일(스트로크 기반)로 Pretendard의 모던한 인상과 잘 어울리고, 24px 그리드 기반이라 아래 크기 체계와 정확히 맞는다.

### 10.2 크기 기준

| 크기 | 사용처 |
|---|---|
| 16px | 뱃지 내 아이콘, 인풋 내 검색 아이콘, 테이블 행 내 작은 액션 |
| 20px | 사이드바 메뉴 아이콘, 버튼(MD) 내 아이콘, 폼 필드 보조 아이콘 |
| 24px | 키오스크 헤더/바텀바 아이콘, 모달 타이틀 아이콘, 페이지 타이틀 보조 아이콘 |

### 10.3 주요 아이콘 매핑

| 기능 | Lucide 아이콘 | 사용 화면 |
|---|---|---|
| 추가(등록) | `Plus` | 카테고리/메뉴 등록 버튼 |
| 수정 | `Pencil` | 모든 목록의 수정 버튼 |
| 삭제 | `Trash2` | 모든 목록/상세의 삭제·취소 버튼 |
| 검색 | `Search` | 메뉴 검색, 관리자 검색 인풋 |
| 닫기 | `X` | 모달, 토스트, CANCELLED 뱃지 |
| 메뉴(햄버거) | `Menu` | (관리자 사이드바 고정형이라 기본 미사용, 추후 접기 기능 대비 예약) |
| 뒤로가기 | `ArrowLeft` | 키오스크 헤더 back 버튼 |
| 장바구니 | `ShoppingCart` | 키오스크 헤더/바텀바 |
| 완료/체크 | `Check` | 주문완료 화면, Success 뱃지 |
| 경고 | `AlertTriangle` | 품절 뱃지, Warning 토스트, 확인 모달 |
| 대시보드 | `LayoutDashboard` | 사이드바 |
| 카테고리 | `FolderTree` | 사이드바 |
| 메뉴 | `UtensilsCrossed` | 사이드바 |
| 재고 | `Package` | 사이드바 |
| 주문 | `Receipt` | 사이드바 |
| 매출 | `TrendingUp` | 사이드바 |
| 감사 로그 | `ClipboardList` | 사이드바 |
| 로그아웃 | `LogOut` | 사이드바 하단 |
| 수량 증가/감소 | `Plus` / `Minus` | 키오스크 수량 스테퍼 |
| 드롭다운 화살표 | `ChevronDown` | Select 컴포넌트 |
| 페이지 이동 | `ChevronLeft` / `ChevronRight` | Pagination |

---

## 11. 인터랙션 & 애니메이션

| 요소 | 효과 | 지속시간 / 타이밍 |
|---|---|---|
| 버튼 Hover/Active | 배경색 전환 | 150ms ease |
| 모달 등장 | fade-in + scale-up(0.95→1) | 200ms ease-out |
| 모달 퇴장 | fade-out + scale-down(1→0.97) | 150ms ease-in |
| 토스트 등장 | 상단(관리자)/하단(키오스크)에서 slide-in + fade-in | 200ms ease-out |
| 토스트 퇴장 | 자동 사라짐 | 3초 후 fade-out 200ms |
| 페이지 전환 | 없음(콘텐츠 영역만 즉시 교체, SPA 기준) | — |
| 카드 Press(키오스크) | `transform: scale(0.98)` | 100ms ease |
| 재고 저장 성공 플래시 | 배경 `--color-success-light` → 원복 | 300ms ease |
| 사이드바 항목 전환 | 배경/텍스트 컬러 전환 | 150ms ease |
| 스켈레톤 로딩 | 배경 펄스(옅은 회색 ↔ 진한 회색) | 1.2s ease-in-out infinite |

---

## 12. 접근성 (Accessibility)

- **색상 대비**: 모든 본문 텍스트는 배경 대비 **4.5:1 이상**, Display/H1처럼 24px 이상 굵은 텍스트는 **3:1 이상**을 만족해야 한다. 위 컬러 시스템의 Neutral 700~900을 본문/헤드라인에 사용하면 흰색/Neutral 50 배경 기준으로 기준을 충족한다. Primary(`#1F6B47`) 위 흰 텍스트도 4.5:1 이상 확보.
- **색맹 대응**: 8.5 Badge에서 정의한 대로 상태 표현은 색상 단독이 아닌 아이콘+텍스트 조합을 기본으로 한다.
- **포커스 링**: 키보드 탐색 시 모든 인터랙티브 요소(버튼/링크/인풋/탭/사이드바 항목)에 `outline: 2px solid --color-primary; outline-offset: 2px;`를 명확히 표시한다. `outline: none`으로 임의 제거 금지.
- **비활성(Disabled) 상태**: `opacity: 0.4; cursor: not-allowed;`를 전 컴포넌트 공통 규칙으로 적용(버튼, 인풋, 카드, 페이지네이션 화살표 등).
- **터치 접근성(키오스크)**: 5.1절에 명시한 최소 44×44px 터치 영역을 모든 인터랙티브 요소(스테퍼 버튼, 카드, 탭)에 예외 없이 적용.
- **스크린 리더/시맨틱**: 테이블은 `<th scope="col">` 사용, 상태 뱃지는 `aria-label`로 의미를 명시(예: `aria-label="상태: 품절"`), 모달은 `role="dialog"` + `aria-modal="true"` + 포커스 트랩 적용, 토스트는 `aria-live="polite"`로 스크린 리더에 자동 안내.
