# Team2 Auth Service

한화솔루션 인증 및 사용자 관리 마이크로서비스

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Security | Spring Security + JJWT 0.12.6 |
| ORM | Spring Data JPA |
| DB (개발) | H2 In-Memory (MariaDB 모드) |
| DB (운영) | MariaDB |
| Build | Gradle |
| Test Coverage | JaCoCo (100% 라인 커버리지) |

## 프로젝트 구조

```
src/main/java/com/team2/auth/
├── config/          # SecurityConfig (CORS, CSRF, BCrypt)
├── controller/      # REST API 컨트롤러 5개
├── dto/             # 요청/응답 DTO 10개
├── entity/          # JPA 엔티티 5개 + Enum 2개
├── repository/      # Spring Data JPA 리포지토리 5개
├── security/        # JwtProvider (토큰 생성/검증)
└── service/         # 비즈니스 로직 서비스 5개
```

## API 엔드포인트

### 인증 (`/api/auth`)

| Method | URI | 설명 | 응답 |
|--------|-----|------|------|
| POST | `/api/auth/login` | 이메일/비밀번호 로그인 | `{accessToken, refreshToken}` |
| POST | `/api/auth/refresh` | 토큰 갱신 | `{accessToken, refreshToken}` |
| POST | `/api/auth/logout` | 로그아웃 (리프레시 토큰 삭제) | 200 OK |

### 사용자 (`/api/users`)

| Method | URI | 설명 | 응답 |
|--------|-----|------|------|
| POST | `/api/users` | 사용자 생성 | 201 Created |
| GET | `/api/users` | 전체 사용자 조회 | 200 OK |
| GET | `/api/users/{id}` | 사용자 상세 조회 | 200 OK |
| PUT | `/api/users/{id}` | 사용자 정보 수정 | 200 OK |
| PATCH | `/api/users/{id}/status` | 재직 상태 변경 | 200 OK |

### 회사 (`/api/company`)

| Method | URI | 설명 | 응답 |
|--------|-----|------|------|
| GET | `/api/company` | 회사 정보 조회 | 200 OK |
| PUT | `/api/company` | 회사 정보 수정 | 200 OK |

### 부서 (`/api/departments`)

| Method | URI | 설명 | 응답 |
|--------|-----|------|------|
| POST | `/api/departments` | 부서 생성 | 201 Created |
| GET | `/api/departments` | 전체 부서 조회 | 200 OK |
| DELETE | `/api/departments/{id}` | 부서 삭제 | 204 No Content |

### 직급 (`/api/positions`)

| Method | URI | 설명 | 응답 |
|--------|-----|------|------|
| POST | `/api/positions` | 직급 생성 | 201 Created |
| GET | `/api/positions` | 전체 직급 조회 | 200 OK |

## 핵심 비즈니스 로직

### 인증 흐름

```
로그인 요청 → 이메일로 사용자 조회 → BCrypt 비밀번호 검증 → 재직 상태 확인
→ Access Token(JWT) + Refresh Token(UUID) 발급 → Refresh Token DB 저장
```

- **Access Token**: HMAC-SHA 서명 JWT, 3시간 만료, 클레임에 userId/email/name/role 포함
- **Refresh Token**: UUID 기반, 7일 만료, DB에 저장하여 무효화 가능
- **토큰 갱신**: 기존 Refresh Token 삭제 후 새 토큰 쌍 발급 (Token Rotation)
- **로그아웃**: 해당 사용자의 Refresh Token을 DB에서 삭제

### 사용자 상태 관리

| 상태 | 로그인 | 상태 변경 |
|------|--------|----------|
| 재직 | 가능 | 휴직/퇴직으로 변경 가능 |
| 휴직 | 불가 | 재직/퇴직으로 변경 가능 |
| 퇴직 | 불가 | 변경 불가 (IllegalStateException) |

### 결재 권한

- 직급 level이 1인 경우 결재 권한 보유 (팀장)
- `User.hasApprovalAuthority()` → Position level 확인

### 부서 삭제 제약

- 소속 사용자가 있는 부서는 삭제 불가 (데이터 무결성 보장)

## 엔티티 관계

```
Company (1)

User (N) ──── Department (1)
  │
  └──────── Position (1)
  │
  └──────── RefreshToken (1)
```

| 엔티티 | 주요 필드 |
|--------|----------|
| User | employeeNo(사번), name, email, pw, role(ADMIN/SALES/PRODUCTION/SHIPPING), status(재직/휴직/퇴직) |
| Company | name, addressEn, addressKr, tel, fax, email, website, sealImageUrl |
| Department | name, createdAt |
| Position | name, level (1=결재권한), createdAt |
| RefreshToken | token, expiresAt, user(FK) |

## JWT 토큰 구조

```json
// Header
{"alg": "HS384", "typ": "JWT"}

// Payload
{
  "sub": "1",           // userId
  "email": "user@test.com",
  "name": "홍길동",
  "role": "SALES",
  "iat": 1711756800,    // 발급 시간
  "exp": 1711767600     // 만료 시간 (3시간 후)
}
```

## 테스트 구조

블로그 [단위 테스트 vs 통합 테스트](https://curiousjinan.tistory.com/entry/integration-vs-unit-testing) 기준으로 분리:

| 계층 | 테스트 유형 | 어노테이션 | 파일 수 |
|------|-----------|-----------|--------|
| Entity | 단위 테스트 | 순수 JUnit 5 | 5 |
| Service | 단위 테스트 | @ExtendWith(MockitoExtension) | 5 |
| Repository | 통합 테스트 | @DataJpaTest | 5 |
| Controller | 슬라이스 테스트 | @WebMvcTest | 5 |
| Security | 단위 테스트 | 순수 JUnit 5 + ReflectionTestUtils | 2 |
| 통합 테스트 | E2E API 테스트 | @SpringBootTest + MockMvc | 1 |

- **총 테스트**: 152개 전체 통과
- **커버리지**: JaCoCo 100% 라인 커버리지 (DTO, Enum, Application 제외)
- **JWT 테스트**: Base64 디코딩 + ObjectMapper로 토큰 payload JSON 직접 파싱 검증

## 초기 데이터

| 구분 | 내용 |
|------|------|
| 회사 | 한화솔루션 (연락처, 주소 포함) |
| 부서 | 영업부, 생산부, 출하부, 경영지원부 |
| 직급 | 팀장(level 1, 결재권한), 팀원(level 2) |
| 사용자 | 8명 (관리자 1, 영업 2, 생산 2, 출하 1, 퇴직 1, 휴직 1) |

샘플 비밀번호: `password123`

## 실행 방법

```bash
# 개발 서버 실행 (H2 인메모리 DB, 포트 8011)
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 기준 검증
./gradlew jacocoTestCoverageVerification
```

## 설정 프로파일

| 프로파일 | DB | 포트 | 용도 |
|---------|-----|------|------|
| default | H2 (MariaDB 모드) | 8011 | 로컬 개발 |
| dev | MariaDB (localhost:3306) | 8011 | 개발 서버 |
| test | H2 (create-drop) | 랜덤 | 테스트 자동화 |
