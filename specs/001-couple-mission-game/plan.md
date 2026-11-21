# Implementation Plan: 힐끔 MVP — 연인 미션·추리 데이트 게임

**Branch**: `001-couple-mission-game` | **Date**: 2026-11-21 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-couple-mission-game/spec.md`

## Summary

연인 2명이 각자 기기에서 방 생성·초대 → 비공개 미션·벌칙 설정 → 진행·종료 합의 → 추리 → 채점 → (동점 가위바위보) → 결산 → 스탬프·소원권까지 한 라운드를 완주하는 Android 클라이언트를 만든다.

백엔드는 서버팀이 **Spring Boot(방·스탬프·소원권)**와 **Node.js(게임 진행)** 두 서버로 제공한다. 명세는 [`docs/API명세서.md`](../../docs/API명세서.md)이고, 앱 관점 정리는 [contracts/api.md](./contracts/api.md)에 있다. 앱은 다음 원칙으로 동작한다.
- 방 생성·입장 때 받은 `memberToken`으로 인증한다.
- 사용자 행동은 REST로 보낸다.
- 화면 단계는 `GET /games/{gameId}` 상태와 로컬 진행 기록을 합쳐 `GamePhasePolicy`로 판정한다.
- 상대 행동은 **3초 폴링**으로 반영한다.
- 중복 방지는 서버의 409 응답과 상태 재조회로 처리한다.
- 기기 알림(⚠️G11)과 24시간 자동 취소(⚠️G12)는 서버 지원 후 연동한다.
- 서버 명세의 공백 G1–G14는 `fake` 빌드 변형으로 우회해 개발한다.

자세한 근거는 [research.md](./research.md)에 있다.

## Technical Context

**Language/Version**: Kotlin 2.2.10, JVM target 11, AGP 8.10.0

**Primary Dependencies**:
- UI: Jetpack Compose (BOM 2026.02.01), Material3, Navigation Compose(타입 안전 Route)
- 상태·DI: lifecycle-runtime-compose / viewmodel-compose, Hilt(+KSP), Coroutines/Flow
- 네트워크: kotlinx.serialization, Retrofit + OkHttp(REST), Retrofit 인스턴스 2개(`@SpringApi`, `@NodeApi`)
- 저장·알림: DataStore, Firebase Cloud Messaging(`data/push` 한정, ⚠️G11 서버 지원 후)

새 의존성은 `gradle/libs.versions.toml`에 추가하고, Kotlin 2.2.10과 호환되는 KSP·Hilt 버전으로 맞춘다.

**Storage**: 원격 서버가 단일 진실원이다. 로컬(DataStore, 백업 제외)에는 다음만 둔다.
- `MemberSession`: roomId, memberId, memberToken, role, inviteCode
- 현재 게임의 로컬 진행 기록: 고른 미션·벌칙, 추리·채점 제출 여부, 종료 요청 여부(⚠️G2·G4 보완용)

**Testing**: JUnit4, kotlinx-coroutines-test, Turbine, OkHttp MockWebServer, Compose UI Test(`ui-test-junit4`)

**Target Platform**: Android 8.0 이상(minSdk 26), target/compileSdk 36, 세로 모드 스마트폰

**Project Type**: mobile-app (Android 클라이언트 전용. 서버는 별도 저장소·팀)

**Performance Goals**: 상대 행동이 3초 이내 화면에 반영(SC-004), 방 생성부터 둘 다 입장까지 2분 이내(SC-001), 화면 전환과 입력 시 끊김 없음(60fps)

**Constraints**:
- 상대의 비공개 데이터는 결산 전 메모리·로그·알림 어디에도 들어오지 않는다(SC-003).
- 중복 제출은 서버 409 + 재조회로 무해하게 처리된다(SC-006).
- 프로세스가 재생성되거나 앱을 다시 열어도 현재 단계로 복귀한다(SC-007).
- 알림 권한이 없어도 진행할 수 있다.

**Scale/Scope**: 사용자 1쌍 = 방 1개, 동시에 진행 중인 라운드 1개, 미션 최대 3개, 화면 약 16개(PRD §5), 해커톤 MVP

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md`는 아직 템플릿 상태라 원칙이 비준되지 않았다. 대신 저장소의 구속력 있는 규칙인 **`docs/RULES.md`**를 게이트로 적용한다.

| # | 게이트 (RULES) | Phase 0 이전 | Phase 1 이후 | 근거 |
|---|---------------|--------------|--------------|------|
| G1 | 계층 의존 `presentation → domain ← data`. domain은 Android·Firebase에 의존하지 않음 (§2, §6) | ✅ | ✅ | FCM·OkHttp·DataStore는 `data`·`di`에만 둔다. domain 모델은 순수 Kotlin([data-model.md](./data-model.md)) |
| G2 | MVI: `uiState`/`event`/`onAction`, Screen + Content 분리 (§8, §9) | ✅ | ✅ | 기존 `presentation/common/MviViewModel` 재사용 |
| G3 | 타입 안전 Route, 라우트 정책은 한 곳에 (§3) | ✅ | ✅ | [contracts/navigation.md](./contracts/navigation.md)의 `HilRoute`, `RoundRoutePolicy` |
| G4 | `AppResult`/`AppError` 반환. UI에 예외나 서버 원문을 노출하지 않음 (§1, §12) | ✅ | ✅ | `Conflict`를 추가했다(아래 Complexity 참조) |
| G5 | 진입·세션 정책 분리, `observeCurrentUser` 스트림 (§10) | ✅ | ✅ | 로그인이 없으므로 `AuthRepository` 대신 `SessionRepository`(`restoreSession`, `observeSession`, `clearSession`)로 대체. memberToken 기반. 401이면 세션 삭제 |
| G6 | 하드코딩 금지: 문자열·색상·치수는 리소스·토큰 (§13) | ✅ | ✅ | 알림 문구도 앱 리소스로 만든다 |
| G7 | 테스트: UseCase·Policy 우선, Fake/Mock, 실서버 의존 금지 (§14) | ✅ | ✅ | [quickstart.md](./quickstart.md) 자동 검증 표 |
| G8 | 생명주기: `collectAsStateWithLifecycle`, `viewModelScope`, GlobalScope 금지 (§11, §15) | ✅ | ✅ | 폴링은 Repository의 공유 Flow(`WhileSubscribed`)로 제공하고, 화면의 생명주기 수집에 묶는다 |
| G9 | 최소 권한, 외부 입력 검증 (§19) | ✅ | ✅ | `INTERNET`만 선언한다. `POST_NOTIFICATIONS`는 ⚠️G11 연동 시 추가. 딥링크는 상태 재조회로 재검증 |

**결과**: 통과. 정당화가 필요한 항목은 아래 Complexity Tracking에 적었다.

## Project Structure

### Documentation (this feature)

```text
specs/001-couple-mission-game/
├── plan.md              # 이 파일
├── research.md          # Phase 0
├── data-model.md        # Phase 1: 도메인 모델·상태 전이
├── quickstart.md        # Phase 1: 검증 가이드
├── contracts/
│   ├── api.md                 # 서버 명세 정리 + 갭 G1–G14
│   ├── realtime-and-push.md   # 폴링 규칙·FCM payload(보류)
│   └── navigation.md          # Route·phase → 화면 매핑
├── checklists/requirements.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/main/java/kr/co/cmc/hil/
├── HilApplication.kt                 # @HiltAndroidApp, 실시간 채널 생명주기 연결
├── MainActivity.kt                   # 기존 파일 수정: NavHost 진입, 딥링크 수신
├── di/                               # NetworkModule, DataStoreModule, RepositoryModule, DispatcherModule
├── domain/
│   ├── common/                       # AppResult, AppError
│   ├── model/                        # Room, Round, RoundPhase, Guess, Scoring, Settlement, TieBreak, StampWallet, WishTicket …
│   ├── policy/                       # ScorePolicy, MissionSelectionPolicy, GuessPolicy, WishTicketPolicy, RoundRoutePolicy, EntryPolicy
│   ├── repository/                   # SessionRepository, RoomRepository, CatalogRepository, GameRepository, StampRepository, CouponRepository
│   └── usecase/
│       ├── room/                     # CreateRoomUseCase, JoinRoomUseCase, ObserveRoomUseCase
│       ├── game/                     # CreateGameUseCase, ObserveGamePhaseUseCase, SelectMissionsUseCase, SelectPenaltyUseCase,
│       │                             # RequestEndUseCase, RespondEndUseCase, SubmitGuessesUseCase, ObserveReceivedGuessesUseCase,
│       │                             # SubmitScoringUseCase, GetResultUseCase, SubmitRpsMoveUseCase, ObserveRpsResultUseCase
│       └── reward/                   # ObserveStampStatusUseCase, ObserveCouponUseCase, ExchangeCouponUseCase,
│                                     # RequestCouponUseUseCase, RespondCouponUseCase
├── data/
│   ├── api/                          # SpringApiService, NodeApiService (Retrofit), AuthInterceptor, ApiCall(safeApiCall → AppResult)
│   ├── polling/                      # PollingFlow (주기·백오프·공유)
│   ├── push/                         # HilMessagingService (FCM), NotificationFactory — ⚠️G11 서버 지원 후
│   ├── database/                     # MemberSessionStore, GameProgressStore (DataStore)
│   ├── dto/                          # 계약 JSON ↔ DTO
│   ├── mapper/                       # DTO → domain (비공개 필드 방어 포함), ErrorMapper → AppError
│   ├── repositoryimpl/               # 원격 구현
│   └── fake/                         # 메모리 Fake 구현 (fake 변형 전용, research R12)
├── presentation/
│   ├── common/                       # MviViewModel(기존), 대기·오류·재시도 공통 컴포넌트, AppError → 문자열 리소스
│   ├── navigation/                   # HilRoute, HilNavHost, RoundNavHost
│   ├── app/                          # AppViewModel (EntryPolicy 기반 시작 목적지)
│   ├── start/  roomcreated/  join/
│   ├── lobby/  howto/  stamp/
│   ├── round/
│   │   ├── config/  missionpick/  penaltypick/  inprogress/
│   │   ├── guess/  scoring/  tiebreak/  settlement/
│   └── coupon/                       # CouponUseScreen, CouponApprovalScreen
│       # 각 화면 패키지: XxxScreen.kt, XxxViewModel.kt, XxxAction.kt, XxxEvent.kt, XxxUiState.kt
└── ui/theme/                         # 기존 디자인 토큰 (Color/Theme/Type)

app/src/main/res/values/strings.xml   # 모든 UI·알림 문구
app/src/main/res/xml/backup_rules.xml, data_extraction_rules.xml   # 토큰 백업 제외

app/src/test/java/kr/co/cmc/hil/
├── domain/policy/   domain/usecase/  # 정책·유스케이스 단위 테스트
├── data/                             # MockWebServer 계약·매퍼·재시도 테스트 (fixture = contracts 예시 JSON)
└── presentation/                     # ViewModel uiState/event 테스트 (Turbine)

app/src/androidTest/java/kr/co/cmc/hil/
└── flow/                             # Fake Repository로 Compose UI 핵심 흐름 테스트
```

**API 갭 대응**: [contracts/api.md §7](./contracts/api.md)의 G1–G14는 서버팀 협의 대상이다. 앱은 해당 필드를 optional로 파싱하고 로컬 기록으로 보완한다. MVP 완주를 막는 G1·G2·G3·G8은 서버 반영이 필요하다.

**Structure Decision**: 단일 `app` 모듈 안에서 계층 패키지(domain/data/presentation/di)로 나눈다. 기존 `kr.co.cmc.hil` 네임스페이스와 `presentation/common/MviViewModel`, `ui/theme`를 그대로 쓴다. 빌드 변형은 `fake`(서버 없이 개발·데모·UI 테스트)와 `remote`(실서버) 두 가지를 둔다. 서버 코드는 이 저장소 범위가 아니다.

## Phase 계획 요약

- **Phase 0 (완료)**: [research.md](./research.md)에 R1–R12 결정을 정리했다. Technical Context에 남은 NEEDS CLARIFICATION은 없다. 서버팀과 합의할 항목은 research.md 하단 표에 있으며, 구현을 막지는 않는다(Fake로 진행).
- **Phase 1 (완료)**:
  - [data-model.md](./data-model.md): 도메인 모델, 검증 규칙, RoundPhase·WishTicket 상태 전이
  - [contracts/](./contracts/): REST, 실시간·푸시, 내비게이션
  - [quickstart.md](./quickstart.md): 검증 가이드
- **Phase 2**: `/speckit-tasks`에서 진행한다. 사용자 스토리 순서(US1 → US7)로 작업을 나누되, 공통 기반 작업(빌드 의존성, DI, AppResult, 네비게이션, 실시간 채널)을 먼저 둔다.

## Complexity Tracking

| Violation / 추가 | Why Needed | Simpler Alternative Rejected Because |
|------------------|-----------|--------------------------------------|
| `AppError.Conflict` 추가 (RULES §12 기본 6종 외) | 동시 요청이나 이미 제출한 상태 같은 상태 경합이 이 게임의 핵심 경로이고, 이 오류가 오면 앱은 "스냅샷 재조회"라는 별도 동작을 해야 한다 | `ValidationFailed`로 합치면 사용자 입력 오류와 상태 경합을 구분하지 못해 잘못된 안내가 표시된다 |
| `AuthRepository` 대신 `SessionRepository` (RULES §10) | 로그인·회원가입 없이 방 단위 memberToken으로 식별한다 | `signIn`·`signUp` 메서드를 빈 구현으로 두면 의미 없는 API만 생긴다 |
| `fake` 빌드 변형 | 서버 갭(G1·G2·G8)이 해결되기 전에도 개발하고, 실서버 없이 UI 테스트하기 위해(RULES §14) | 테스트 소스셋에만 Fake를 두면 데모와 수동 검증을 할 수 없다 |
