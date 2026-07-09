# 버거킹 키오스크 시스템 바이브 코딩

카테고리별 메뉴 조회, 장바구니 담기, 주문까지 가능한 키오스크 웹 애플리케이션입니다.  
고객용 키오스크 화면과 관리자용 백오피스 화면으로 구성되어 있습니다.

---

## 주요 기능 (Key Features)

### 고객 화면 (키오스크)
- **카테고리별 메뉴 탐색** : 버거 / 사이드 / 음료 카테고리를 탭으로 전환하며 메뉴를 확인할 수 있습니다.
- **단품 / 세트 메뉴 구분** : 세트 메뉴는 구성 단품을 확인할 수 있습니다.
- **품절 표시** : 재고가 0인 메뉴에는 자동으로 "품절" 배지가 표시됩니다.
- **장바구니** : 메뉴 추가 / 수량 변경 / 개별 삭제 / 전체 비우기가 가능합니다.
- **주문 완료** : 장바구니에 담긴 메뉴를 주문하면 주문 번호(101번부터 순차 채번)를 발급합니다.

### 관리자 백오피스 (`/admin`)
- **로그인 / JWT 인증** : 관리자 계정으로 로그인하면 JWT 토큰이 발급되고, 이후 모든 관리 API에 자동 첨부됩니다.
- **대시보드** : 오늘의 매출 · 주문 건수 · 재고 부족 메뉴 현황을 한눈에 확인합니다.
- **카테고리 관리** : 카테고리 추가 / 수정 / 순서 변경 / 활성·비활성 전환이 가능합니다.
- **메뉴 관리** : 단품 메뉴 등록·수정·삭제 및 세트 메뉴 구성(2단계 위저드)을 지원합니다.
- **이미지 업로드** : 메뉴 이미지를 서버에 직접 업로드하고 URL을 자동으로 연결합니다.
- **재고 관리** : 메뉴별 재고 수량을 전용 화면에서 수정합니다.
- **주문 관리** : 주문 목록 조회 / 상세 확인 / 주문 취소가 가능합니다.
- **매출 조회** : 일별 · 월별 · 연별 매출 통계를 확인합니다.
- **감사 로그** : 관리자의 모든 쓰기 행위(등록·수정·삭제)가 자동으로 기록되고 조회할 수 있습니다.

---

## 기술 스택 (Tech Stack)

### 백엔드

| 항목 | 내용 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.0 |
| 데이터베이스 | MySQL 8.x |
| ORM | Spring Data JPA + QueryDSL 5.1.0 |
| 인증 | Spring Security + JWT (jjwt 0.12.5) |
| 문서화 | SpringDoc OpenAPI (Swagger UI) |
| 로깅 | Log4j2 |
| 빌드 도구 | Gradle |
| 기타 | Lombok, Bean Validation |

### 프론트엔드

| 항목 | 내용 |
|---|---|
| 언어 | JavaScript (JSX) |
| 프레임워크 | React 19 |
| 번들러 | Vite 8 |
| 라우팅 | React Router v7 |
| 전역 상태 | Zustand v5 |
| 서버 상태 | TanStack Query (React Query) v5 |
| HTTP 클라이언트 | Axios |
| 폼 검증 | React Hook Form + Zod |
| 스타일링 | CSS Modules |

---

## 프로젝트 구조 (Project Structure)

```
Kiosk/
├── Kiosk_Backend/                   # Spring Boot 백엔드
│   └── src/main/java/com/example/kiosk_backend/
│       ├── config/                  # 보안·JPA·Swagger·파일 업로드 설정
│       ├── controller/              # REST API 컨트롤러
│       │   ├── CartController.java
│       │   ├── CategoryController.java
│       │   ├── MenuController.java
│       │   ├── OrderController.java
│       │   └── (admin) AdminAuthController, AdminMenuController ...
│       ├── service/                 # 비즈니스 로직
│       ├── repository/              # DB 접근 (JPA Repository)
│       ├── entity/                  # JPA 엔티티 (Category, Menu, Order 등)
│       ├── dto/                     # 요청·응답 DTO
│       ├── security/                # JWT 필터·인증 처리
│       └── common/                  # 공통 응답 포맷, 예외 처리, 유틸리티
│
└── Kiosk_Frontend/                  # React 프론트엔드
    └── src/
        ├── api/                     # Axios 기반 API 호출 함수
        ├── components/              # 재사용 공통 컴포넌트
        ├── hooks/                   # 커스텀 React Hooks
        ├── layouts/                 # KioskLayout, AdminLayout
        ├── pages/
        │   ├── kiosk/               # 고객용 화면
        │   │   ├── MenuListPage.jsx     (메인 메뉴 화면)
        │   │   ├── CartPage.jsx         (장바구니)
        │   │   └── OrderCompletePage.jsx (주문 완료)
        │   └── admin/               # 관리자용 화면
        │       ├── AdminLoginPage.jsx
        │       ├── DashboardPage.jsx
        │       ├── CategoriesPage.jsx
        │       ├── MenusPage.jsx / MenuFormPage.jsx / SetMenuWizardPage.jsx
        │       ├── InventoryPage.jsx
        │       ├── OrdersPage.jsx / OrderDetailPage.jsx
        │       ├── SalesPage.jsx
        │       └── AuditLogsPage.jsx
        ├── routes/                  # 라우터 정의 및 PrivateRoute
        ├── stores/                  # Zustand 전역 상태 스토어
        └── styles/                  # 전역 CSS, 디자인 토큰
```

---

## ▶ 실행 방법 (How to Run)

> 전공 지식 없이도 따라 할 수 있도록 단계별로 설명합니다.  
> 총 3가지 프로그램(Java, MySQL, Node.js)을 설치한 뒤 백엔드와 프론트엔드를 순서대로 실행합니다.

---

### 1단계 : 필수 프로그램 설치

아래 세 가지가 이미 설치되어 있다면 이 단계는 건너뛰세요.

#### Java 21

1. [https://adoptium.net](https://adoptium.net) 에 접속합니다.
2. **Temurin 21 (LTS)** 를 선택하고 운영체제에 맞는 설치 파일을 다운로드합니다.
3. 설치 후 터미널(명령 프롬프트)을 열고 아래 명령어로 설치를 확인합니다.
   ```
   java -version
   ```
   `openjdk version "21"` 이 출력되면 성공입니다.

#### MySQL 8

1. [https://dev.mysql.com/downloads/installer](https://dev.mysql.com/downloads/installer) 에서 MySQL Installer를 다운로드합니다.
2. 설치 과정에서 **MySQL Server** 항목만 선택해도 됩니다.
3. 설치 시 포트는 **3307**, 비밀번호는 자유롭게 설정하세요.  
   (포트를 3306으로 바꾸면 아래 설정 파일도 함께 수정해야 합니다.)

#### Node.js 20 이상

1. [https://nodejs.org](https://nodejs.org) 에 접속해 **LTS** 버전을 다운로드합니다.
2. 설치 후 터미널에서 확인합니다.
   ```
   node -v
   ```

---

### 2단계 : 데이터베이스 준비

MySQL에 접속해 데이터베이스와 사용자를 만듭니다.  
MySQL Workbench 또는 터미널에서 아래 SQL을 실행하세요.

```sql
-- 데이터베이스 생성
CREATE DATABASE Kiosk_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON Kiosk_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
```

> 비밀번호를 다르게 설정하려면 `Kiosk_Backend/src/main/resources/application.properties` 파일의  
> `spring.datasource.password` 값도 함께 수정하세요.

---

### 3단계 : 프로젝트 다운로드

GitHub에서 ZIP 파일로 다운로드한 경우 압축을 해제합니다.  
폴더 구조가 `Kiosk/Kiosk_Backend/` 와 `Kiosk/Kiosk_Frontend/` 형태가 맞는지 확인하세요.

---

### 4단계 : 백엔드 실행

터미널을 열고 `Kiosk_Backend` 폴더로 이동합니다.

**Mac / Linux**
```bash
cd 경로/Kiosk/Kiosk_Backend
./gradlew bootRun
```

**Windows**
```cmd
cd 경로\Kiosk\Kiosk_Backend
gradlew.bat bootRun
```

처음 실행 시 Gradle이 필요한 파일을 자동으로 다운로드합니다 (시간이 조금 걸릴 수 있습니다).  
터미널에 아래 메시지가 출력되면 백엔드가 정상적으로 실행된 것입니다.

```
Started KioskBackendApplication in X.XXX seconds
```

백엔드가 실행되면 초기 데이터(카테고리, 메뉴, 관리자 계정)가 자동으로 생성됩니다.

> API 문서(Swagger UI) : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### 5단계 : 프론트엔드 실행

새 터미널 창을 열고 `Kiosk_Frontend` 폴더로 이동합니다.

```bash
cd 경로/Kiosk/Kiosk_Frontend

# 최초 1회 : 패키지 설치
npm install

# 개발 서버 실행
npm run dev
```

터미널에 아래와 같이 출력되면 성공입니다.

```
  VITE v8.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

---

### 6단계 : 접속 확인

| 화면 | 주소 |
|---|---|
| 고객용 키오스크 | [http://localhost:5173](http://localhost:5173) |
| 관리자 백오피스 | [http://localhost:5173/admin/login](http://localhost:5173/admin/login) |

**관리자 초기 계정**

| 항목 | 값 |
|---|---|
| 아이디 | `admin` |
| 비밀번호 | `password123` |

> 실제 서비스에 배포할 때는 반드시 비밀번호를 변경하세요.

---

### 자주 발생하는 문제

| 증상 | 해결 방법 |
|---|---|
| `gradlew: Permission denied` (Mac/Linux) | 터미널에서 `chmod +x gradlew` 실행 후 재시도 |
| 백엔드 실행 시 DB 연결 오류 | MySQL이 실행 중인지 확인, `application.properties`의 포트·계정 정보 확인 |
| 프론트엔드에서 API 연결 안 됨 | 백엔드(8080 포트)가 먼저 실행 중인지 확인 |
| `node_modules` 관련 오류 | `Kiosk_Frontend` 폴더에서 `npm install` 재실행 |
