# Phase 0 Research: 힐끔 MVP

**Feature**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Updated**: 2026-11-22 ([`docs/API명세서.md`](../../docs/API명세서.md) 반영)

전제: 백엔드는 서버팀이 **Spring Boot(방·스탬프·소원권)**와 **Node.js(게임 진행)** 두 서버로 제공한다. 이 저장소는 Android 클라이언트만 담당한다. 서버 명세와 spec이 다른 부분은 [contracts/api.md §7](./contracts/api.md)에 정리했다.

---

## R1. 앱 아키텍처와 기본 스택

- **Decision**: 단일 `app` 모듈 + 패키지 계층(`domain`/`data`/`presentation`/`di`). Kotlin 2.2.10, Compose(BOM 2026.02.01), Material3, MVI(기존 `MviViewModel` 재사용), Hilt, Navigation Compose(타입 안전 Route), Coroutines/Flow.
- **Rationale**: `docs/RULES.md` §2·§3·§8·§18-1을 따른다. 해커톤 규모에서는 단일 모듈이 적합하다.
- **Alternatives considered**: 멀티 모듈(과도함), Koin(RULES 예시와 불일치).

## R2. 서버 2개 연동

- **Decision**: Retrofit 인스턴스를 2개 만들고 Hilt `@Qualifier`로 구분한다(`@SpringApi`, `@NodeApi`). 공통 `OkHttpClient`는 `AuthInterceptor`(memberToken 주입)를 공유한다. base URL은 `BuildConfig`로 주입하며, 값은 `local.properties`의 `hil.springBaseUrl`, `hil.nodeBaseUrl`에서 읽는다.
- **Rationale**: 서버 명세의 담당 분리를 그대로 따른다. Repository가 어느 서버를 쓰는지는 data 계층에만 드러난다.
- **Alternatives considered**: API 게이트웨이 하나(서버팀이 제공하지 않음).

## R3. 실시간 반영 → 폴링

- **Decision**: 서버에 실시간 채널이 없다. 앱 사용 중에는 화면별로 상태 API를 **3초 주기로 폴링**한다(가위바위보는 2초). 폴링은 Repository의 공유 Flow로 제공하고, 생명주기(STARTED)에 묶는다. 실패하면 백오프한다. 자세한 규칙은 [contracts/realtime-and-push.md](./contracts/realtime-and-push.md)에 있다.
- **Rationale**: 서버 명세 3번이 폴링을 명시한다. 3초 주기는 SC-004(3초 반영)에 가장 가깝게 맞출 수 있는 비용 대비 현실적인 값이다.
- **Alternatives considered**: WebSocket·SSE(서버 미지원. 나중에 `PollingDataSource`만 교체하면 됨).

## R4. 사용자 식별·세션

- **Decision**: 방 생성(API 1)이나 입장(API 2) 응답으로 받은 `memberToken`, `memberId`, `roomId`, `role`, (방장이면) `inviteCode`를 `MemberSessionStore`(DataStore, Keystore 암호화)에 저장한다. 이 저장소는 백업에서 제외한다. 앱이 시작되면 세션 유무로 시작 화면을 정한다. 401을 받으면 세션을 지운다.
- **Rationale**: 서버 명세 1번 "재접속할 수 있도록 멤버 토큰 발급"을 따르며, 기기 등록 API는 없다. PRD 4.1 "재설치하면 방 소실"도 자연스럽게 지켜진다.
- **Alternatives considered**: 이전 초안의 `POST /devices`(서버 명세에 없음).

## R5. 비공개 데이터 보호 (FR-019)

- **Decision**: 서버 명세 5·7·8·9번이 "비밀 정보 제외"를 보장한다. 앱은 내가 고른 미션·벌칙만 로컬에 저장하고, 상대 데이터는 결산(API 16) 전에는 받지 않는다. 로그 인터셉터는 debug 빌드에서만 켜고, 본문 대신 헤더 수준으로만 남긴다.
- **Rationale**: 클라이언트는 보안 경계가 아니다(RULES §10). 그래도 앱이 노출 경로를 늘려서는 안 된다.

## R6. 중복 방지 (FR-024, FR-030, FR-034, FR-043)

- **Decision**: 서버가 상태 기반으로 중복을 막고 409를 돌려준다(명세 8·10·12·14·15·17·21·23·24번). 앱은 다음 세 가지로 대응한다.
  - 요청 중에는 버튼을 비활성화한다(ViewModel의 in-flight 가드).
  - 제출 계열 API가 409를 돌려주면 **이미 반영된 것으로 보고** 상태를 다시 조회한다.
  - 네트워크 오류 후 재시도는 같은 본문으로 보낸다.
- **Rationale**: 서버 명세에 Idempotency-Key가 없으므로 상태 기반 방식에 맞춘다.
- **Alternatives considered**: Idempotency-Key 헤더(서버 미지원).

## R7. 기기 알림

- **Decision**: spec Clarification의 알림 3종(상대 입장, 종료 요청, 소원권 승인 요청)은 **서버 API가 생길 때까지 보류**한다(⚠️G11). 앱 쪽에서는 FCM 수신 서비스, 알림 채널, 딥링크 처리를 마지막 단계 작업으로 준비해 둔다. 서버 반영 전에는 앱을 열면 폴링으로 상태가 반영된다(FR-050c).
- **Rationale**: 서버 명세에 토큰 등록·발송 기능이 없다. 앱만 구현하면 동작하지 않는다.

## R8. 점수·승패 계산의 권위

- **Decision**: 서버(API 16)가 점수·승패를 계산한다. 클라이언트 `ScorePolicy`(15×n + 10×m)는 테스트 기준과 점수 내역 표시에만 쓴다.
- **Rationale**: 두 기기 결과가 일치해야 한다(SC-005).

## R9. 시간 규칙

- **Decision**: 소원권 만료일(`expiresAt`)과 남은 일수(`remainingDays`)는 **서버 값을 그대로 표시**한다. 서버 예시 기준으로 만료 시각은 발급 시각 + 1개월(같은 시각)이다. 24시간 자동 취소는 서버가 판정해야 하며, 앱은 `CANCELLED` 상태 수신만 준비한다(⚠️G12).
- **Rationale**: 기기 시계 오차로 판정이 갈리는 것을 막는다. 앞서 spec 가정에 적었던 "다음 달 전날 23:59"는 서버 명세에 맞춰 고쳤다.

## R10. 미션·벌칙 목록

- **Decision**: `GET /missions?dateType=`, `GET /penalties`(Node.js)로 받는다. 앱에 번들 목록은 두지 않는다. 내가 고른 미션 내용은 로컬에 저장해 진행 화면에 쓴다(⚠️G2를 서버가 반영하면 서버 조회로 바꾼다).

## R11. 테스트 전략

- **Decision**:
  - JUnit4 + kotlinx-coroutines-test + Turbine으로 Policy·UseCase·ViewModel 테스트
  - OkHttp MockWebServer로 API 매핑 테스트(fixture는 `docs/API명세서.md`의 응답 예시)
  - Compose UI 테스트는 Fake Repository로 핵심 흐름을 확인
  - 2기기 E2E는 [quickstart.md](./quickstart.md)의 수동 시나리오로 확인
- **Rationale**: RULES §14를 따른다.

## R12. 서버 준비 전 개발

- **Decision**: 빌드 변형(flavor)을 `fake`/`remote` 두 가지로 둔다. `fake`는 메모리 기반 Fake Repository가 두 번째 참가자를 시뮬레이션하며, 디버그 메뉴에서 "상대 행동"을 실행할 수 있다.
- **Rationale**: 서버 갭(G1, G2, G8)이 해결되기 전에도 모든 화면을 개발하고 데모할 수 있다.

---

## 서버팀 협의 항목

[contracts/api.md §7](./contracts/api.md)의 G1–G14를 참고한다. 그중 다음 4개는 **앱 MVP 완주를 막는 항목**이다.

| ID | 내용 |
|----|------|
| G1 | 참여자가 게임 ID를 알 수 없음 |
| G2 | 내 미션 조회 API, `playerMissionId` 없음 |
| G3 | 미션 개수 범위 불일치 |
| G8 | 상대가 받은 소원권 요청을 조회할 API 없음 |
