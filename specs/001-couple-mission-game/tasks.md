---
description: "Task list for 힐끔 MVP Android client"
---

# Tasks: 힐끔 MVP — 연인 미션·추리 데이트 게임

**Input**: `/specs/001-couple-mission-game/` 아래의 문서
- plan.md, spec.md, research.md, data-model.md, quickstart.md
- contracts/api.md, contracts/realtime-and-push.md, contracts/navigation.md
- 원본 API 명세: `docs/API명세서.md`

**Prerequisites**: plan.md, spec.md (필수). 코드 규칙은 `docs/RULES.md`를 따른다.

**Tests**: 다음 테스트만 포함했다. TDD는 강제하지 않는다.
- `docs/RULES.md` §14가 요구하는 Policy·UseCase·Repository 단위 테스트
- quickstart.md "자동 검증 대상"에 적힌 테스트

**Organization**: 사용자 스토리(spec.md US1–US7) 단위로 묶었다. US1–US5는 게임 흐름상 순서대로 이어지지만, `fake` 빌드 변형의 Fake Repository를 쓰면 각 스토리의 화면을 따로 개발하고 검증할 수 있다.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 다른 파일이고 아직 끝나지 않은 작업에 의존하지 않아 병렬로 진행할 수 있음
- **[Story]**: 해당 사용자 스토리 (US1–US7)
- **⚠️G#**: [contracts/api.md §7](./contracts/api.md)의 서버 갭. 필드는 optional로 파싱하고, 적힌 임시 처리를 따른다.

## Path Conventions

| 별칭 | 실제 경로 |
|------|-----------|
| `main/` | `app/src/main/java/kr/co/cmc/hil/` |
| `res/` | `app/src/main/res/` |
| `fake/` | `app/src/fake/java/kr/co/cmc/hil/` (fake 빌드 변형 전용) |
| `remote/` | `app/src/remote/java/kr/co/cmc/hil/` (remote 빌드 변형 전용) |
| `test/` | `app/src/test/java/kr/co/cmc/hil/` |
| `androidTest/` | `app/src/androidTest/java/kr/co/cmc/hil/` |

공통 규칙:
- 화면 패키지마다 `XxxScreen.kt`(Screen + private Content), `XxxViewModel.kt`(`MviViewModel` 상속), `XxxAction.kt`, `XxxEvent.kt`, `XxxUiState.kt` 5개 파일을 만든다(RULES §8·§9).
- 모든 UI 문구는 `res/values/strings.xml`에 둔다.

## 역할 분담 (레이어별: 앱콩 · 엠키 · 뭉뭉)

| 담당 | 레이어 | 맡는 범위 | 작업 수 |
|------|--------|-----------|---------|
| **앱콩** | 도메인 (`main/domain/**`, `fake/**`) | 모델, Policy, Repository 인터페이스, 유스케이스와 그 단위 테스트. 화면 개발이 데이터 구현을 기다리지 않도록 **Fake Repository와 FakeServer**도 맡는다(도메인 계약을 실제로 실행해 보는 구현) | 42 |
| **엠키** | 데이터 (`main/data/**`, `main/di/**`, `remote/**`, 빌드 설정) | Gradle, DTO, Retrofit 서비스(Spring·Node), 매퍼, Repository 구현, DataStore, 폴링, FCM과 그 테스트. 서버팀 소통 담당 | 45 |
| **뭉뭉** | 프레젠테이션 (`main/presentation/**`, `res/**`, `MainActivity`) | Route·NavHost, 화면(Screen·Content·ViewModel·Action·Event·UiState), 문구, 접근성, ViewModel·UI 테스트 | 41 |

### 병렬 작업 규칙: 계약 우선

1. **스토리마다 앱콩이 계약 PR을 먼저 올린다.** 계약은 모델, Repository 인터페이스, 유스케이스 시그니처, Fake 구현이다. 반나절 안에 머지하는 것을 목표로 한다.
2. **엠키는 계약을 기다리지 않는다.** DTO·API 서비스는 `contracts/api.md` 기준으로 바로 시작하고, 계약이 머지되면 Repository 구현을 붙인다.
3. **뭉뭉도 계약을 기다리지 않는다.** 문구와 Content(Preview)를 먼저 만들고, 계약이 머지되면 ViewModel을 유스케이스에 연결해 `fake` 변형으로 확인한다.
4. **레이어 경계 = 파일 경계**라서 세 사람이 같은 파일을 동시에 고치는 일이 거의 없다. 예외는 아래 표에 정리했다.

### 충돌 주의 파일

| 파일 | 담당 | 규칙 |
|------|------|------|
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | 엠키 | 다른 사람이 의존성이 필요하면 엠키에게 요청한다 |
| `fake/di/FakeRepositoryModule.kt`, `fake/data/fake/**` | 앱콩 | Fake 동작이 더 필요하면 앱콩에게 요청한다 |
| `remote/di/RemoteRepositoryModule.kt`, `main/di/**` | 엠키 | — |
| `main/domain/repository/GameRepository.kt` | 앱콩 | 엠키는 구현체(`GameRepositoryImpl.kt`)만 고친다. 시그니처를 바꿔야 하면 앱콩에게 요청한다 |
| `main/presentation/navigation/**`, `res/values/strings.xml` | 뭉뭉 | — |
| `app/src/main/AndroidManifest.xml` | 엠키 | 딥링크(T126 계열)는 뭉뭉이 엠키와 조율한 뒤 추가한다 |

### 동기화 지점

1. **Day 1 오전**: 엠키는 Gradle 설정(T001–T003)을, 앱콩은 도메인 공통 타입(T007–T011)을 머지한다.
2. **Phase 2 종료**: 세 사람이 함께 `assembleFakeDebug assembleRemoteDebug testFakeDebugUnitTest`를 통과시킨다.
3. **각 스토리 시작**: 앱콩의 계약 PR을 머지한다(위 규칙 1).
4. **각 스토리 종료**: 뭉뭉이 fake 변형에서 해당 스토리의 Independent Test를 시연하고, 엠키가 remote 변형에서 연결을 확인한다(서버 갭이 해결된 범위까지).
5. **US5 종료 (MVP)**: fake 변형으로 한 판 전체 데모. 엠키가 서버 갭 G1·G2·G3·G8의 반영 여부를 공유한다.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 의존성과 빌드 설정, 앱 진입점 준비

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | — |
| 엠키 (데이터) | T001, T002, T003, T004, T005 |
| 뭉뭉 (프레젠테이션) | T006 |

> 엠키가 Gradle·Manifest·백업 규칙을 맡는다(빌드 파일은 엠키만 수정). 뭉뭉은 공통 문구를 준비한다. 앱콩은 Gradle이 필요 없는 순수 Kotlin 도메인 타입(Phase 2)을 바로 시작한다.

- [ ] T001 `gradle/libs.versions.toml`에 버전과 라이브러리 별칭을 추가한다. 버전은 Kotlin 2.2.10, AGP 8.10.0과 호환되는 것으로 고른다.
  - 라이브러리: hilt-android/compiler, hilt-navigation-compose, navigation-compose, kotlinx-serialization-json, retrofit, retrofit-kotlinx-serialization converter, okhttp, okhttp-logging-interceptor, datastore-preferences, lifecycle-runtime-compose, lifecycle-viewmodel-compose, lifecycle-process, kotlinx-coroutines-test, turbine, okhttp-mockwebserver
  - 플러그인: ksp, hilt, kotlin-serialization
- [ ] T002 `build.gradle.kts`(루트)에 ksp, hilt, kotlin-serialization 플러그인을 `apply false`로 추가한다.
- [ ] T003 `app/build.gradle.kts`를 설정한다.
  - 플러그인 적용, T001의 의존성 추가, `buildFeatures.buildConfig = true`
  - `flavorDimensions("backend")`에 `fake`와 `remote` productFlavor 추가
  - `local.properties`의 `hil.springBaseUrl`, `hil.nodeBaseUrl`을 `BuildConfig.SPRING_BASE_URL`, `BuildConfig.NODE_BASE_URL`로 주입. 값이 없으면 빈 문자열
- [ ] T004 [P] `main/HilApplication.kt`를 만든다(`@HiltAndroidApp`). `app/src/main/AndroidManifest.xml`에 `android:name=".HilApplication"`과 `INTERNET` 권한을 추가한다.
- [ ] T005 [P] `res/xml/backup_rules.xml`과 `res/xml/data_extraction_rules.xml`에서 DataStore 파일(`datastore/member_session.preferences_pb`, `datastore/game_progress.preferences_pb`)을 백업 대상에서 제외한다. 재설치하면 세션이 사라져야 한다(research R4).
- [ ] T006 [P] `res/values/strings.xml`에 공통 문구를 추가한다: 로딩, 재시도, 네트워크 오류, 알 수 없는 오류, 연결 불안정, 상대 대기.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 모든 스토리가 공유하는 결과 타입, 세션, 네트워크, 폴링, 네비게이션 뼈대

**⚠️ CRITICAL**: 이 단계를 마쳐야 사용자 스토리 작업을 시작할 수 있다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T007, T008, T009, T010, T011, T021, T025 |
| 엠키 (데이터) | T012, T013, T014, T015, T016, T017, T018, T019, T020, T028, T029 |
| 뭉뭉 (프레젠테이션) | T022, T023, T024, T026, T027 |

> **Day 1 오전에 앱콩이 T007–T011(결과·오류 타입, ID, 세션 모델, SessionRepository)을 먼저 머지한다.** 그다음 엠키는 세션 저장소·네트워크·폴링을, 뭉뭉은 공통 컴포넌트·Route·NavHost를 병렬로 진행한다. T025(EntryPolicy)는 앱콩, T026(AppViewModel)은 그 위에서 뭉뭉이 맡는다.

- [ ] T007 [P] `main/domain/common/AppResult.kt`를 만든다: `sealed interface AppResult<out T>` + `Success` / `Failure(error: AppError)`.
- [ ] T008 [P] `main/domain/common/AppError.kt`와 `main/domain/common/ErrorReason.kt`를 만든다.
  - `AppError`: `Unauthorized`, `PermissionDenied`, `ValidationFailed(reason)`, `NetworkError(isRetryable)`, `NotFound`, `Conflict(reason)`, `Unknown`
  - `ErrorReason`: data-model.md §10의 사유 enum
- [ ] T009 [P] `main/domain/model/Ids.kt`에 Long 값 클래스를 만든다: `RoomId`, `MemberId`, `GameId`, `PlayerMissionId`, `GuessId`, `CouponId`, `MissionId`, `PenaltyId`. 밀접하게 묶인 작은 타입이라 한 파일에 둔다.
- [ ] T010 [P] `main/domain/model/MemberRole.kt`(`CREATOR` / `JOINER`)와 `main/domain/model/MemberSession.kt`(roomId, memberId, memberToken, role, inviteCode?)를 만든다.
- [ ] T011 `main/domain/repository/SessionRepository.kt`를 만든다: `observeSession(): Flow<MemberSession?>`, `restoreSession()`, `saveSession()`, `clearSession()`.
- [ ] T012 `main/data/database/MemberSessionStore.kt`를 만든다. DataStore 파일명은 `member_session`이다. 토큰은 Android Keystore AES로 암호화해 저장하고, 토큰을 로그에 남기지 않는다.
- [ ] T013 `main/data/repositoryimpl/SessionRepositoryImpl.kt`를 만든다. T012의 `MemberSessionStore`에 위임한다.
- [ ] T014 [P] `main/data/api/AuthInterceptor.kt`를 만든다. `SessionRepository`의 현재 토큰을 `Authorization: Bearer`로 넣는다. 401을 받으면 `clearSession()`을 호출한다.
- [ ] T015 [P] `main/data/api/SafeApiCall.kt`를 만든다. `suspend fun <T> safeApiCall(block): AppResult<T>`로, contracts/api.md §0 오류 표에 따라 HTTP 상태를 `AppError`로 바꾼다. `CancellationException`은 다시 던진다(RULES §11).
- [ ] T016 `main/di/ApiQualifiers.kt`(`@SpringApi`, `@NodeApi`)와 `main/di/NetworkModule.kt`를 만든다.
  - `Json { ignoreUnknownKeys = true }` 제공
  - OkHttpClient: `AuthInterceptor` + debug 빌드에서만 HEADERS 레벨 로깅
  - Retrofit 2개: `BuildConfig.SPRING_BASE_URL`, `BuildConfig.NODE_BASE_URL`
- [ ] T017 [P] `main/di/DispatcherModule.kt`를 만든다. `@IoDispatcher`, `@ApplicationScope CoroutineScope`(SupervisorJob)를 제공한다. GlobalScope는 쓰지 않는다.
- [ ] T018 [P] `main/data/polling/PollingFlow.kt`를 만든다: `fun <T> pollingFlow(intervalMs, fetch: suspend () -> AppResult<T>): Flow<AppResult<T>>`. 실패하면 2배 백오프(최대 30초)하고, 성공하면 기본 주기로 돌아온다(contracts/realtime-and-push.md §1).
- [ ] T019 [P] `main/di/SessionModule.kt`를 만든다. `SessionRepository` → `SessionRepositoryImpl` 바인딩. 두 빌드 변형이 공통으로 쓴다.
- [ ] T020 [P] 빌드 변형별 Repository 바인딩 모듈의 빈 뼈대를 만든다: `fake/di/FakeRepositoryModule.kt`, `remote/di/RemoteRepositoryModule.kt`. 바인딩 내용은 각 스토리에서 채운다.
- [ ] T021 `fake/data/fake/FakeServer.kt`를 만든다. 메모리 안에서 방·게임·스탬프·소원권 상태와 두 번째 참가자(파트너)를 시뮬레이션하는 `@Singleton`이다. 각 스토리에서 기능을 덧붙인다.
- [ ] T022 [P] `main/presentation/common/ErrorMessageMapper.kt`를 만든다. `AppError`와 `ErrorReason`을 `@StringRes`로 매핑한다.
- [ ] T023 [P] 공통 컴포넌트를 만든다. 색상·치수는 `ui/theme` 토큰만 쓴다.
  - `main/presentation/common/component/LoadingContent.kt`
  - `main/presentation/common/component/ErrorRetryContent.kt`
  - `main/presentation/common/component/WaitingPartnerBanner.kt`
- [ ] T024 `main/presentation/navigation/HilRoute.kt`를 만든다. contracts/navigation.md §2의 모든 Route를 `@Serializable`로 정의한다(`LobbyRoute(tab)`, `CouponUseRoute(couponId)`, `CouponApprovalRoute(couponId)` 포함).
- [ ] T025 `main/domain/policy/EntryPolicy.kt`를 만든다. 세션과 방 상태로 시작 목적지를 정하는 순수 함수다(contracts/navigation.md §1의 1–4단계. 5단계는 US2에서 추가).
- [ ] T026 `main/presentation/app/AppViewModel.kt`를 만든다. `SessionRepository.observeSession()`과 `EntryPolicy`로 시작 목적지를 `StateFlow`로 노출한다.
- [ ] T027 `main/presentation/navigation/HilNavHost.kt`를 만들고 `main/MainActivity.kt`를 고친다.
  - `MainActivity`: `@AndroidEntryPoint`를 붙이고 `setContent`에서 `HilNavHost`를 호출
  - `HilNavHost`: `AppViewModel`의 시작 목적지로 `NavHost`를 구성하고, 각 Route 자리에 placeholder composable을 둔다
- [ ] T028 [P] `test/data/api/SafeApiCallTest.kt`: MockWebServer로 400·401·403·404·409·500 응답과 IOException이 각각 맞는 `AppError`로 바뀌는지 확인한다.
- [ ] T029 [P] `test/data/polling/PollingFlowTest.kt`: 기본 주기, 실패 시 백오프, 성공 후 기본 주기 복귀를 `runTest`의 가상 시간으로 확인한다.

**Checkpoint**: `./gradlew assembleFakeDebug assembleRemoteDebug testFakeDebugUnitTest`가 통과하고, 앱이 `StartRoute` placeholder로 시작한다.

---

## Phase 3: User Story 1 - 방 만들고 연인 초대하기 (Priority: P1) 🎯 MVP

**Goal**: 시작하기 → 방 생성·코드 복사 → 상대가 코드로 입장 → 두 기기의 대기실에 2명이 표시된다. 앱을 다시 열면 같은 방으로 복구된다.

**Independent Test**: quickstart.md E2E 1단계. 잘못된 코드와 정원 초과 안내를 포함한다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T030, T031, T036, T038, T045 |
| 엠키 (데이터) | T032, T033, T034, T035, T037, T046 |
| 뭉뭉 (프레젠테이션) | T039, T040, T041, T042, T043, T044, T047 |

> **계약 우선**: 앱콩이 T030·T031·T038(모델, 인터페이스, 유스케이스)과 T036(Fake)를 먼저 머지한다. 엠키의 DTO·API 서비스(T032·T033)와 뭉뭉의 화면 Content는 계약 머지를 기다리지 않고 바로 시작한다.

- [ ] T030 [P] [US1] `main/domain/model/RoomStatus.kt`(`PENDING` / `ACTIVE`), `main/domain/model/InviteCode.kt`, `main/domain/model/Room.kt`를 만든다.
  - `InviteCode`: 입력을 대문자로 바꾸고 공백을 없앤 뒤 6자리 영숫자인지 검증. 실패하면 `ValidationFailed(INVALID_INVITE_CODE)`
  - `Room`: roomId, status, members, currentGameId?(⚠️G1), 파생 속성 `isPartnerJoined`
- [ ] T031 [P] [US1] `main/domain/repository/RoomRepository.kt`를 만든다: `createRoom()`, `joinRoom(code)`, `observeRoom(): Flow<AppResult<Room>>`.
- [ ] T032 [P] [US1] DTO를 만든다(contracts/api.md §1).
  - `main/data/dto/room/CreateRoomResponseDto.kt`
  - `main/data/dto/room/JoinRoomRequestDto.kt`
  - `main/data/dto/room/JoinRoomResponseDto.kt`
  - `main/data/dto/room/RoomResponseDto.kt` (`currentGameId: Long? = null`, ⚠️G1)
- [ ] T033 [US1] `main/data/api/SpringApiService.kt`를 만들고 API 1–3(`POST /api/v1/rooms`, `POST /api/v1/rooms/join`, `GET /api/v1/rooms/{roomId}`)을 정의한다. `main/di/NetworkModule.kt`에 `@SpringApi` Retrofit으로 제공하는 코드를 추가한다.
- [ ] T034 [P] [US1] `main/data/mapper/RoomMapper.kt`를 만든다. DTO → `Room`, `MemberSession` 변환이다.
- [ ] T035 [US1] `main/data/repositoryimpl/RoomRepositoryImpl.kt`를 만든다.
  - 방 생성·입장에 성공하면 `SessionRepository.saveSession()`을 호출한다(방장은 inviteCode도 저장)
  - join 404 → `ValidationFailed(INVALID_INVITE_CODE)`, join 409 → `Conflict(ROOM_FULL)`
  - `observeRoom`은 `pollingFlow(3000)`에 `shareIn(WhileSubscribed(5000))`
- [ ] T036 [P] [US1] `fake/data/fake/FakeRoomRepository.kt`를 만든다. `FakeServer`로 방 생성·입장·조회를 흉내 내고, 파트너 입장을 트리거할 수 있게 한다. `fake/di/FakeRepositoryModule.kt`에 바인딩을 추가한다.
- [ ] T037 [US1] `remote/di/RemoteRepositoryModule.kt`에 `RoomRepository` → `RoomRepositoryImpl` 바인딩을 추가한다.
- [ ] T038 [P] [US1] 유스케이스를 만든다: `main/domain/usecase/room/CreateRoomUseCase.kt`, `JoinRoomUseCase.kt`(InviteCode 검증 포함), `ObserveRoomUseCase.kt`.
- [ ] T039 [US1] `main/presentation/start/`에 Start 화면 5개 파일을 만든다. '시작하기'는 CreateRoom 후 `RoomCreatedRoute`로, '초대 코드 입력'은 `JoinRoute`로 이동한다. 로딩·오류 상태를 포함한다.
- [ ] T040 [US1] `main/presentation/roomcreated/`에 RoomCreated 화면 5개 파일을 만든다.
  - 초대 코드 표시, 복사 버튼(`ClipboardManager`는 Screen에서만 사용)
  - `ObserveRoomUseCase`로 '상대 대기'와 '상대 입장'을 표시하고, 입장하면 `LobbyRoute`로 이동(FR-007)
- [ ] T041 [US1] `main/presentation/join/`에 Join 화면 5개 파일을 만든다. 코드 입력·입장 버튼이 있고, 빈 코드·잘못된 코드·만원·성공 상태를 처리한다. 성공하면 `LobbyRoute`로 이동한다(FR-003~005).
- [ ] T042 [US1] `main/presentation/lobby/`에 Lobby 화면 5개 파일을 만든다.
  - 참가자 2명과 진행 현황 표시
  - '미션 시작' 아이콘은 방장일 때만 활성
  - 우측 상단 ? 아이콘, 좌측 하단 홈↔스탬프 토글(스탬프 탭 내용은 US6)
- [ ] T043 [US1] `main/presentation/navigation/HilNavHost.kt`의 Start, RoomCreated, Join, Lobby placeholder를 실제 화면으로 교체한다. 화면 이동 이벤트는 Screen의 `onNavigate` 콜백으로 처리한다.
- [ ] T044 [P] [US1] `res/values/strings.xml`에 US1 문구를 추가한다: 시작하기, 초대 코드 입력, 복사됨, 상대 대기, 상대 입장, 잘못된 코드, 이미 사용된 코드.
- [ ] T045 [P] [US1] `test/domain/model/InviteCodeTest.kt`: 소문자·공백 정규화, 5자, 7자, 특수문자 입력을 검증한다.
- [ ] T046 [P] [US1] `test/data/repositoryimpl/RoomRepositoryImplTest.kt`: MockWebServer에 `docs/API명세서.md` 1–3번 응답 예시를 넣고 확인한다.
  - 세션이 저장되는지
  - 404와 409가 올바른 사유로 바뀌는지
  - `currentGameId`가 없어도 파싱되는지
- [ ] T047 [P] [US1] `test/presentation/join/JoinViewModelTest.kt`: 빈 입력, 잘못된 코드, 만원일 때 uiState의 오류 사유와 성공 시 네비게이션 event를 Turbine으로 확인한다.

**Checkpoint**: fake 변형에서 방 생성 → 파트너 입장(Fake 트리거) → 대기실까지 진행된다. 앱을 다시 실행하면 대기실로 복귀한다.

---

## Phase 4: User Story 2 - 비공개로 미션·벌칙 설정하기 (Priority: P1)

**Goal**: 방장이 유형·개수를 정해 게임을 만들고, 두 사람이 각자 미션 N개와 벌칙 1개를 고른다. 둘 다 마치면 진행 단계로 넘어간다.

**Independent Test**: quickstart.md E2E 2단계. 상대의 선택이 어떤 화면에도 나타나지 않아야 한다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T048, T049, T050, T051, T052, T058, T060, T061, T068, T069 |
| 엠키 (데이터) | T053, T054, T055, T056, T057, T059 |
| 뭉뭉 (프레젠테이션) | T062, T063, T064, T065, T066, T067 |

> **계약 우선**: 앱콩이 T048–T052, T060, T058(Fake)을 먼저 머지한다. 엠키는 DTO·API·저장소(T053–T056)를 바로 시작하고, 뭉뭉은 fake 변형으로 설정·선택 화면과 RoundNavHost를 붙인다.

- [ ] T048 [P] [US2] 게임 도메인 모델을 만든다.
  - `main/domain/model/DateType.kt`
  - `main/domain/model/GameStatus.kt`: `SETTING`, `SELECTING`, `PLAYING`, `SCORING`, `TIEBREAK`, `FINISHED`, `CANCELLED`, `UNKNOWN`
  - `main/domain/model/GameProgress.kt`
  - `main/domain/model/Game.kt`
  - `main/domain/model/RoundPhase.kt` (data-model.md §3.1의 enum)
- [ ] T049 [P] [US2] 카탈로그 모델을 만든다: `main/domain/model/Mission.kt`, `main/domain/model/Penalty.kt`, `main/domain/model/MyMission.kt`(playerMissionId?, ⚠️G2).
- [ ] T050 [P] [US2] `main/domain/policy/MissionSelectionPolicy.kt`를 만든다. 선택 수 불일치, 중복, 빈 목록, 벌칙 1개 규칙을 검증한다(data-model.md §4). `MISSION_COUNT_RANGE = 1..3`은 spec 기준이다(⚠️G3).
- [ ] T051 [P] [US2] `main/domain/policy/GamePhasePolicy.kt`를 만든다. `(Game?, MemberRole, GameProgress) → RoundPhase`이며, data-model.md §3.1 표 전체를 구현한다. 이후 단계 phase도 여기서 모두 정의한다.
- [ ] T052 [P] [US2] `main/domain/repository/CatalogRepository.kt`(`getMissions(dateType)`, `getPenalties()`)와 `main/domain/repository/GameRepository.kt`를 만든다.
  - `GameRepository` 1차 메서드: `createGame(dateType, count)`, `observeGame(): Flow<AppResult<Game>>`, `selectMissions(ids)`, `selectPenalty(id)`, `getMyMissions(): List<MyMission>`
- [ ] T053 [P] [US2] DTO를 만든다(contracts/api.md §2·§3).
  - `main/data/dto/game/CreateGameRequestDto.kt`, `CreateGameResponseDto.kt`, `GameStatusResponseDto.kt` (⚠️G4 필드는 모두 nullable)
  - `main/data/dto/catalog/MissionListResponseDto.kt`, `PenaltyListResponseDto.kt`
  - `main/data/dto/game/SelectMissionsRequestDto.kt`, `SelectMissionsResponseDto.kt`, `SelectPenaltyRequestDto.kt`, `SelectPenaltyResponseDto.kt`
- [ ] T054 [US2] `main/data/api/NodeApiService.kt`를 만들고 API 4–9를 정의한다. 벌칙 경로는 `/api/v1/games/{gameId}/penalty`다(⚠️G10). `main/di/NetworkModule.kt`에 `@NodeApi` Retrofit으로 제공하는 코드를 추가한다.
- [ ] T055 [P] [US2] `main/data/mapper/GameMapper.kt`와 `main/data/mapper/CatalogMapper.kt`를 만든다. 처음 보는 status 문자열은 `UNKNOWN`으로 바꾼다.
- [ ] T056 [US2] `main/data/database/GameProgressStore.kt`를 만든다. DataStore 파일명은 `game_progress`이고, gameId 기준으로 다음을 저장한다(⚠️G2·G4 보완).
  - 내가 고른 미션(id, content), 벌칙 선택 여부
  - 추리·수행·판정 제출 여부, 종료 요청 여부
  - 새 gameId가 들어오면 이전 기록을 지운다
- [ ] T057 [US2] `main/data/repositoryimpl/CatalogRepositoryImpl.kt`와 `main/data/repositoryimpl/GameRepositoryImpl.kt`를 만든다.
  - 현재 gameId는 방장의 경우 `createGame` 응답에서, 참여자의 경우 `Room.currentGameId`에서 얻는다(⚠️G1)
  - `observeGame`은 `pollingFlow(3000)`이며, 응답과 `GameProgressStore`를 합쳐 `Game.progress`를 만든다
  - 선택 API가 409를 돌려주면 성공으로 보고 다시 조회한다(research R6)
- [ ] T058 [P] [US2] `fake/data/fake/FakeGameRepository.kt`와 `fake/data/fake/FakeCatalogRepository.kt`를 만든다.
  - 고정 미션 목록: 온라인 5개, 오프라인 5개 / 고정 벌칙 목록 5개
  - 파트너의 선택 완료를 트리거할 수 있게 한다
  - `fake/di/FakeRepositoryModule.kt`에 바인딩을 추가한다
- [ ] T059 [US2] `remote/di/RemoteRepositoryModule.kt`에 `CatalogRepository`와 `GameRepository` 바인딩을 추가한다.
- [ ] T060 [P] [US2] 유스케이스를 `main/domain/usecase/game/` 아래에 만든다.
  - `CreateGameUseCase.kt`, `ObserveGamePhaseUseCase.kt`(Game + Session → `GamePhasePolicy`)
  - `GetMissionsUseCase.kt`, `GetPenaltiesUseCase.kt`
  - `SelectMissionsUseCase.kt`, `SelectPenaltyUseCase.kt` (둘 다 `MissionSelectionPolicy`로 검증)
- [ ] T061 [US2] `main/domain/policy/EntryPolicy.kt`에 5단계를 추가한다. 진행 중인 게임이 있으면 `GamePhasePolicy` 결과를 시작 목적지로 쓴다(contracts/navigation.md §1).
- [ ] T062 [US2] `main/presentation/navigation/RoundRoutePolicy.kt`와 `main/presentation/navigation/RoundNavHost.kt`를 만든다.
  - `RoundRoutePolicy`: contracts/navigation.md §3 표에 따라 `RoundPhase → HilRoute`
  - `RoundNavHost`: `ObserveGamePhaseUseCase`를 구독하다가 phase가 바뀌면 `popUpTo(LobbyRoute)` 후 이동
- [ ] T063 [US2] `main/presentation/round/config/`에 RoundConfig 화면 5개 파일을 만든다. 온라인/오프라인 선택, 미션 수 1–3 선택이 있고, 선택하지 않으면 안내한다. 완료하면 `CreateGameUseCase`를 호출한다(FR-011, FR-012).
- [ ] T064 [US2] `main/presentation/round/missionpick/`에 MissionPick 화면 5개 파일을 만든다. 목록에서 정확히 N개를 골라야 완료 버튼이 활성화된다. 빈 목록이면 안내한다(FR-014, FR-016).
- [ ] T065 [US2] `main/presentation/round/penaltypick/`에 PenaltyPick 화면 5개 파일을 만든다. 벌칙 1개를 선택하고, 완료한 뒤에는 `WaitingPartnerBanner`를 표시한다(FR-015, FR-017).
- [ ] T066 [US2] `main/presentation/lobby/LobbyViewModel.kt`와 `LobbyScreen.kt`를 고친다. `LOBBY_WAITING_HOST`면 "방장이 설정 중"을 표시하고, 방장이 '미션 시작'을 누르면 `RoundConfigRoute`로 이동한다(FR-013).
- [ ] T067 [P] [US2] `res/values/strings.xml`에 US2 문구를 추가한다: 유형, 미션 수, 미선택 안내, 목록 부족, 상대 설정 대기.
- [ ] T068 [P] [US2] `test/domain/policy/GamePhasePolicyTest.kt`: data-model.md §3.1 표의 모든 행을 확인한다(CREATOR/JOINER, 처음 보는 status 포함).
- [ ] T069 [P] [US2] `test/domain/policy/MissionSelectionPolicyTest.kt`: 개수 불일치, 중복, 빈 목록, 벌칙 누락, 범위 밖 개수를 확인한다.

**Checkpoint**: fake 변형에서 방장 설정 → 양측 선택 → `IN_PROGRESS` phase까지 진행되고, 진행 화면 placeholder로 자동 이동한다.

---

## Phase 5: User Story 3 - 데이트 진행과 종료 합의 (Priority: P1)

**Goal**: 진행 화면에 내 미션만 보이고, 종료 요청 → 상대 동의 또는 거절에 따라 두 기기가 함께 전환된다.

**Independent Test**: quickstart.md E2E 3단계. 거절하면 진행 화면으로 돌아가고, 동의하면 추리 단계로 넘어간다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T070, T074 |
| 엠키 (데이터) | T071, T072, T073 |
| 뭉뭉 (프레젠테이션) | T075, T076, T077 |

> **계약 우선**: 앱콩의 T070(인터페이스 + Fake)과 T074(유스케이스)이 먼저 들어가면, 엠키의 T073 구현과 뭉뭉의 T075 화면을 병렬로 진행한다.

- [ ] T070 [US3] `main/domain/repository/GameRepository.kt`에 `requestEnd(): AppResult<Unit>`, `respondEnd(accepted: Boolean): AppResult<Unit>` 시그니처를 추가한다. `fake/data/fake/FakeGameRepository.kt`에 Fake 동작을 구현한다(요청 기록, 파트너 수락·거절 트리거).
- [ ] T071 [P] [US3] DTO를 만든다: `main/data/dto/game/EndRequestResponseDto.kt`, `EndResponseRequestDto.kt`, `EndResponseResponseDto.kt`.
- [ ] T072 [US3] `main/data/api/NodeApiService.kt`에 API 10(`POST /games/{gameId}/end-request`)과 11(`POST /games/{gameId}/end-response`)을 추가한다.
- [ ] T073 [US3] `main/data/repositoryimpl/GameRepositoryImpl.kt`에 `requestEnd()`, `respondEnd(accepted)`를 구현한다.
  - 요청에 성공하면 `GameProgressStore`에 `endRequestedByMe = true`를 기록한다(⚠️G4)
  - 409가 오면 다시 조회한다
- [ ] T074 [P] [US3] 유스케이스를 만든다: `main/domain/usecase/game/RequestEndUseCase.kt`, `main/domain/usecase/game/RespondEndUseCase.kt`.
- [ ] T075 [US3] `main/presentation/round/inprogress/`에 InProgress 화면 5개 파일을 만든다(FR-020~024).
  - 내 미션 목록(`getMyMissions`), 진행 상태, 종료 버튼
  - `END_REQUEST_SENT`면 '응답 대기' 다이얼로그
  - `END_REQUEST_RECEIVED`면 동의/거절 다이얼로그
  - 거절된 경우(요청 보냄 → `IN_PROGRESS`로 복귀) 1회 안내 event
  - 다이얼로그 표시 여부는 Screen이 소유한다
  - 철회 버튼은 두지 않는다(⚠️G6). 미접속 구분은 하지 않는다(⚠️G7)
- [ ] T076 [P] [US3] `res/values/strings.xml`에 US3 문구를 추가한다: 종료 요청, 응답 대기, 종료 동의/거절, 거절됨 안내.
- [ ] T077 [P] [US3] `test/presentation/round/inprogress/InProgressViewModelTest.kt`: 요청 중 중복 클릭 무시(in-flight 가드), 거절 시 안내 event, phase별 다이얼로그 상태를 확인한다.

**Checkpoint**: fake 변형에서 종료 요청 → 파트너 거절/동의 트리거 → 진행 화면 복귀 또는 `GUESS` phase로 이동한다.

---

## Phase 6: User Story 4 - 상대 미션 추리와 채점 (Priority: P1)

**Goal**: 미션 수만큼 추리(각 30자)를 한 번에 제출하고, 내 미션 수행 여부 O/X와 받은 추리의 정답 여부 O/X를 제출한다.

**Independent Test**: quickstart.md E2E 4단계. 빈 칸이면 제출할 수 없고, 31자째는 입력되지 않으며, 재시도해도 한 번만 저장된다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T078, T079, T080, T084, T088, T089 |
| 엠키 (데이터) | T081, T082, T083 |
| 뭉뭉 (프레젠테이션) | T085, T086, T087 |

> **계약 우선**: 앱콩의 T078·T079·T080·T080이 먼저 들어간다. 엠키는 DTO·API(T081·T082)를 바로 시작하고, 뭉뭉은 추리·채점 화면을 fake로 개발한다.

- [ ] T078 [P] [US4] 모델을 만든다.
  - `main/domain/model/GuessText.kt`: trim 후 1..30자, `BreakIterator`로 grapheme 단위 계산
  - `main/domain/model/Guess.kt`: guessNo, content
  - `main/domain/model/ReceivedGuess.kt`: guessId, guessNo, content
  - `main/domain/model/ScoringInput.kt`: reviews `Map<PlayerMissionId, Boolean>`, judgments `Map<GuessId, Boolean>`
- [ ] T079 [P] [US4] `main/domain/policy/GuessPolicy.kt`를 만든다. 모든 칸이 유효한지 판정하고, 입력을 30자에서 자른다(FR-027).
- [ ] T080 [US4] `main/domain/repository/GameRepository.kt`에 `submitGuesses(List<Guess>)`, `observeReceivedGuesses(): Flow<AppResult<ReceivedGuesses>>`, `submitScoring(ScoringInput)` 시그니처를 추가한다. `fake/data/fake/FakeGameRepository.kt`에 Fake 동작을 구현한다(파트너 추리 제출·채점 완료 트리거).
- [ ] T081 [P] [US4] DTO를 만든다.
  - `main/data/dto/game/SubmitGuessesRequestDto.kt`, `SubmitGuessesResponseDto.kt`
  - `main/data/dto/game/ReceivedGuessesResponseDto.kt`
  - `main/data/dto/game/MissionReviewsRequestDto.kt`, `MissionReviewsResponseDto.kt`
  - `main/data/dto/game/JudgmentsRequestDto.kt`, `JudgmentsResponseDto.kt`
- [ ] T082 [US4] `main/data/api/NodeApiService.kt`에 API 12–15를 추가한다. 판정 경로는 `/api/v1/games/{gameId}/judgments`다(⚠️G10).
- [ ] T083 [US4] `main/data/repositoryimpl/GameRepositoryImpl.kt`에 다음을 구현한다.
  - `submitGuesses(List<Guess>)`
  - `observeReceivedGuesses(): Flow<AppResult<ReceivedGuesses>>` (3초 폴링, `partnerSubmissionCompleted`이면 멈춤)
  - `submitScoring(ScoringInput)`: reviews → judgments 순서로 호출하고, 409는 성공으로 본다
  - 각 제출이 성공하면 `GameProgressStore`에 기록한다(⚠️G4)
- [ ] T084 [P] [US4] 유스케이스를 `main/domain/usecase/game/` 아래에 만든다: `SubmitGuessesUseCase.kt`(`GuessPolicy`로 검증), `ObserveReceivedGuessesUseCase.kt`, `SubmitScoringUseCase.kt`(모든 미션·추리에 값이 있는지 검증).
- [ ] T085 [US4] `main/presentation/round/guess/`에 Guess 화면 5개 파일을 만든다(FR-026~029).
  - 미션 수만큼 입력 칸을 한 화면에 두고 각각 글자 수(n/30)를 표시
  - 빈 칸이 있으면 제출 비활성
  - 제출 후에는 입력을 잠그고 '상대 추리 대기'를 표시
- [ ] T086 [US4] `main/presentation/round/scoring/`에 Scoring 화면 5개 파일을 만든다(FR-031~033).
  - 위: 내 미션 목록 + 수행 O/X 토글 (`playerMissionId`가 없으면 제출 버튼을 비활성화하고 오류 안내, ⚠️G2)
  - 아래: 받은 추리 목록 + 정답 O/X 토글
  - 모두 입력해야 제출할 수 있고, 제출 후에는 '상대 채점 대기'를 표시
- [ ] T087 [P] [US4] `res/values/strings.xml`에 US4 문구를 추가한다: 추리 입력 안내, 글자 수, 빈 값, 제출 완료, 상대 추리 대기, 수행 O/X, 정답 O/X, 상대 채점 대기.
- [ ] T088 [P] [US4] `test/domain/model/GuessTextTest.kt`: 공백만 입력, 30자, 31자, 이모지 30개, 앞뒤 공백 trim을 확인한다.
- [ ] T089 [P] [US4] `test/domain/usecase/game/SubmitScoringUseCaseTest.kt`: 값 누락 시 검증 실패, reviews 409 후 judgments가 호출되는지, 둘 다 성공하는 경우를 Fake Repository로 확인한다.

**Checkpoint**: fake 변형에서 추리 제출 → 파트너 제출 트리거 → 채점 제출 → 파트너 채점 트리거 → `SETTLEMENT` 또는 `TIE_BREAK` phase로 이동한다.

---

## Phase 7: User Story 5 - 결산 (Priority: P1)

**Goal**: 두 기기에 같은 점수·승패·승자 벌칙이 표시된다.

**Independent Test**: quickstart.md E2E 5단계. 40 대 35로 A 승리.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T090, T091, T092, T096, T099 |
| 엠키 (데이터) | T093, T094, T095 |
| 뭉뭉 (프레젠테이션) | T097, T098 |

> **계약 우선**: 앱콩의 T090·T091·T092·T091이 먼저 들어간다. 이 단계가 끝나면 MVP 데모를 할 수 있다. 세 사람이 함께 fake 변형으로 한 판을 끝까지 돌려 본다.

- [ ] T090 [P] [US5] 모델을 만든다.
  - `main/domain/model/Settlement.kt`: winner, myScore, partnerScore, penalty, reveal?(⚠️G5), 파생 속성 isMyWin
- [ ] T091 [P] [US5] `main/domain/policy/ScorePolicy.kt`를 만든다: `performedCount × 15 + correctGuessCount × 10` (FR-035).
- [ ] T092 [US5] `main/domain/repository/GameRepository.kt`에 `getResult()` 시그니처를 추가한다. `fake/data/fake/FakeGameRepository.kt`에 Fake 동작을 구현한다(점수 계산).
- [ ] T093 [P] [US5] DTO를 만든다: `main/data/dto/game/GameResultResponseDto.kt`(⚠️G5 필드는 nullable).
- [ ] T094 [US5] `main/data/api/NodeApiService.kt`에 API 16을 추가한다.
- [ ] T095 [US5] `main/data/repositoryimpl/GameRepositoryImpl.kt`에 `getResult()`를 구현한다.
- [ ] T096 [P] [US5] 유스케이스를 `main/domain/usecase/game/` 아래에 만든다: `GetResultUseCase.kt`.
- [ ] T097 [US5] `main/presentation/round/settlement/`에 Settlement 화면 5개 파일을 만든다(FR-036~039).
  - 승패, 양측 점수, 공개 벌칙(승자가 고른 것) 표시. reveal이 있으면 미션·추리·O/X 목록도 표시
  - 이긴 경우 스탬프 반영 안내
  - '대기실로' 버튼
- [ ] T098 [P] [US5] `res/values/strings.xml`에 US5 문구를 추가한다: 승리/패배, 점수, 벌칙 안내, 스탬프 +1.
- [ ] T099 [P] [US5] `test/domain/policy/ScorePolicyTest.kt`: 미션 1·2·3개의 최고점 25/50/75점, 스펙 예시 40 대 35, 0점을 확인한다.

**Checkpoint**: fake 변형에서 한 라운드 전체(US1–US5)를 끝까지 진행할 수 있다. **← MVP 데모 가능**

---

## Phase 8: User Story 6 - 승리 스탬프와 소원권 (Priority: P2)

**Goal**: 스탬프 n/3 표시 → 3개면 교환 → 소원권 D-day 표시 → 사용 요청 → 상대가 승인하면 소진, 거절하면 다시 보유.

**Independent Test**: quickstart.md E2E 6단계.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T100, T101, T106, T107, T114 |
| 엠키 (데이터) | T102, T103, T104, T105, T115 |
| 뭉뭉 (프레젠테이션) | T108, T109, T110, T111, T112, T113 |

> US3–US5와 일부 병렬로 진행할 수 있다. 앱콩은 US5 계약을 마친 뒤 곧바로 T100·T101·T107·T101을 머지한다. 엠키는 Spring 소원권 API, 뭉뭉은 스탬프·소원권 화면을 맡는다.

- [X] T100 [P] [US6] 모델을 만든다. ✅ 구현: 기존 `domain/model/stamp/Stamps.kt`(isFull·showFullWarning 추가), `coupon/CouponStatus.kt`·`Coupon.kt`, 신규 `IncomingCouponRequest.kt`
  - `main/domain/model/StampStatus.kt`: heartCount, maxHearts, canExchange, hasActiveCoupon, 파생 속성 showFullWarning
  - `main/domain/model/CouponStatus.kt`: `AVAILABLE` / `PENDING` / `USED`
  - `main/domain/model/WishCoupon.kt`, `main/domain/model/IncomingCouponRequest.kt`
- [X] T101 [P] [US6] `main/domain/repository/StampRepository.kt`(`observeStampStatus()`)와 `main/domain/repository/CouponRepository.kt`를 만든다. ✅ 구현: 기존 `StampRepository`/`CouponRepository`(suspend 조회 방식 유지)에 `getIncomingCouponRequest()`(⚠️G8) 추가. 폴링은 ViewModel에서 수행
  - `CouponRepository`: `observeMyCoupon()`, `exchange()`, `requestUse(id)`, `observeIncomingRequest()`(⚠️G8), `respond(id, approved)`
- [X] T102 [P] [US6] DTO를 만든다. ✅ 구현: 기존 `data/dto/coupon/*`, `dto/stamp/*` 재사용 + `IncomingCouponResponseDto.kt` 추가
  - `main/data/dto/reward/StampStatusResponseDto.kt`, `CouponExchangeResponseDto.kt`, `CouponResponseDto.kt`
  - `main/data/dto/reward/CouponUseRequestResponseDto.kt`, `CouponRespondRequestDto.kt`, `CouponRespondResponseDto.kt`
  - `main/data/dto/reward/IncomingCouponResponseDto.kt` (⚠️G8 제안 형식)
- [X] T103 [US6] `main/data/api/SpringApiService.kt`에 API 19, 21–24와 ⚠️G8 제안 엔드포인트 `GET /api/v1/coupons/incoming`을 추가한다. ✅ 구현: Retrofit 서비스 대신 Ktor로 `CouponRepositoryImpl`에서 직접 호출. G8 `GET api/v1/coupons/incoming` 추가(404면 요청 없음으로 처리)
- [X] T104 [P] [US6] `main/data/mapper/RewardMapper.kt`를 만든다. 시각은 `OffsetDateTime`으로 파싱하고, `remainingDays`는 서버 값을 그대로 쓴다. ✅ 구현: 기존 `CouponMapper.kt`/`StampMapper.kt`에 G8 매퍼 추가
- [X] T105 [US6] `main/data/repositoryimpl/StampRepositoryImpl.kt`와 `main/data/repositoryimpl/CouponRepositoryImpl.kt`를 만든다. ✅ 구현: Repository 구현 완료. 3초(승인 대기)/5초(받은 요청) 폴링과 409 사유 문구 매핑은 `WishCouponViewModel`이 담당. DI는 `di/RepositoryModule.kt`
  - 승인 대기 중에는 `observeMyCoupon`을 3초 폴링하고, `observeIncomingRequest`는 5초 폴링한다
  - 409 사유를 `COUPON_HELD`, `COUPON_PENDING`으로 바꾼다
  - `remote/di/RemoteRepositoryModule.kt`에 바인딩을 추가한다
- [ ] T106 [P] [US6] `fake/data/fake/FakeStampRepository.kt`와 `fake/data/fake/FakeCouponRepository.kt`를 만든다. ⏸️ 보류: 프로젝트에 `fake` 빌드 변형이 없어 미구현. 단위 테스트의 Fake Repository로 대체 검증
  - `FakeServer`에서 승리하면 스탬프가 쌓이게 연동
  - 파트너의 승인·거절과 파트너 쪽 사용 요청을 트리거할 수 있게 한다
  - `fake/di/FakeRepositoryModule.kt`에 바인딩을 추가한다
- [X] T107 [P] [US6] 유스케이스를 `main/domain/usecase/reward/` 아래에 만든다: `ObserveStampStatusUseCase.kt`, `ObserveCouponUseCase.kt`, `ExchangeCouponUseCase.kt`, `RequestCouponUseUseCase.kt`, `ObserveIncomingCouponRequestUseCase.kt`, `RespondCouponUseCase.kt`. ✅ 구현: `domain/usecase/coupon/`의 기존 유스케이스 + `GetIncomingCouponRequestUseCase.kt` (`di/UseCaseModule.kt`)
- [X] T108 [US6] `main/presentation/stamp/`에 Stamp 화면 5개 파일을 만든다. 대기실의 스탬프 탭에 들어가는 내용이다(FR-043~047). ✅ 구현: `presentation/stamp/WishCoupon{Screen,ViewModel,Action,Event,UiState}.kt` — 기존 UI를 실제 API에 연결, 샘플 데이터 제거
  - 스탬프 n/3, 교환 버튼(canExchange일 때 활성)
  - 소원권과 D-day, 사용 버튼(`AVAILABLE`일 때 활성), `PENDING`이면 '승인 대기'
  - 스탬프가 가득 찬 경우의 경고 문구
- [X] T109 [US6] `main/presentation/coupon/use/`에 CouponUse 화면 5개 파일을 만든다(FR-048). 사용 확인 → 요청 → 처리 중·완료(승인 대기)·실패와 재시도 상태를 보여준다. ✅ 구현: 별도 화면 대신 `presentation/stamp/WishTicketDialog.kt`가 사용 확인→요청 중→승인 대기/실패·다시 시도를 표시
- [X] T110 [US6] `main/presentation/coupon/approval/`에 CouponApproval 화면 5개 파일을 만든다(FR-048a). 받은 요청에 승인/거절으로 응답하고 결과를 안내한다. ✅ 구현: `presentation/coupon/approval/CouponApproval*` 5개 파일
- [X] T111 [US6] `main/presentation/lobby/LobbyScreen.kt`와 `LobbyViewModel.kt`를 고친다. ✅ 구현: 대기실 = `TemporaryHomeScreen`: 받은 요청 배너(`presentation/common/CouponRequestBanner.kt`), `HilkkeumNavHost`에 `CouponApprovalRoute` 연결, 홈·스탬프 화면 표시 중에만 폴링
  - 스탬프 탭에 Stamp 화면 내용을 연결
  - 받은 소원권 요청이 있으면 배너를 표시하고, 누르면 `CouponApprovalRoute`로 이동
  - `main/presentation/navigation/HilNavHost.kt`에 Coupon 라우트 2개를 연결
- [X] T112 [US6] `main/presentation/round/settlement/SettlementViewModel.kt`를 고친다. 결산을 확인한 뒤 `ObserveStampStatusUseCase`를 다시 조회해 n/3을 반영한다. ✅ 구현: 결산 화면(`OperationResultScreen`) 종료 시 스탬프 화면으로 이동하며 `Refresh`로 n/3 재조회
- [X] T113 [P] [US6] `res/values/strings.xml`에 US6 문구를 추가한다: 스탬프 n/3, 교환, 소원권, D-%d, 사용, 승인 대기, 승인/거절, 가득 참 경고, 사용 완료, 거절됨. ✅ 구현: `res/values/wish_coupon.xml`, `res/values/coupon_approval.xml`
- [X] T114 [P] [US6] `test/domain/model/StampStatusTest.kt`: `showFullWarning` 진리표와 `canExchange` 표시 조건을 확인한다. ✅ 구현: `test/.../domain/model/stamp/StampsTest.kt`
- [X] T115 [P] [US6] `test/data/repositoryimpl/CouponRepositoryImplTest.kt`: MockWebServer에 `docs/API명세서.md` 21–24번 응답 예시를 넣고 확인한다. ✅ 구현: `test/.../data/repositoryimpl/CouponRepositoryImplTest.kt` (Ktor MockEngine). ViewModel 테스트 2종도 추가
  - 상태 전이 `AVAILABLE` → `PENDING` → `USED`, 거절 시 `AVAILABLE`
  - 409 사유 매핑
  - `coupon: null`인 응답

**Checkpoint**: fake 변형에서 3번 이긴 뒤 교환 → 사용 요청 → 파트너 승인/거절 트리거 → 상태가 반영된다.

---

## Phase 9: User Story 7 - 이용 방법 확인 (Priority: P3)

**Goal**: 대기실 ? → 게임 규칙 → 뒤로 가기

**Independent Test**: 대기실에서 ? → 규칙 화면 → 뒤로 가기로 대기실에 돌아온다.

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | — |
| 엠키 (데이터) | — |
| 뭉뭉 (프레젠테이션) | T116, T117, T118 |

> 화면 전용 단계라 뭉뭉만 작업한다. US6 화면 사이사이에 진행한다.

- [X] T116 [P] [US7] `res/values/strings.xml`에 게임 규칙 문구를 추가한다: 미션·관찰·추리·점수(15점/10점)·벌칙·스탬프 규칙. ✅ 구현: `res/values/howto.xml` (미션·관찰·추리·채점과 점수 15/10점·벌칙·스탬프와 소원권)
- [X] T117 [US7] `main/presentation/howto/HowToScreen.kt`를 만든다. 상태가 없는 단순 화면이라 ViewModel은 생략할 수 있다(RULES §9). 뒤로 가기 버튼을 둔다. ✅ 구현: `presentation/howto/HowToScreen.kt` (Screen + private Content, ViewModel 생략, 뒤로 가기 버튼)
- [X] T118 [US7] `main/presentation/navigation/HilNavHost.kt`에 `HowToRoute`를 연결하고, `main/presentation/lobby/LobbyScreen.kt`의 ? 아이콘에서 이동하게 한다. ✅ 구현: `HilkkeumNavHost`에 `HowToRoute` 연결. 대기실(`TemporaryHomeScreen`)의 ? 아이콘이 기존 인라인 도움말 대신 이용 방법 화면으로 이동

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: 서버 갭 연동, 품질, 통합 검증

**👥 담당**:

| 담당 | 작업 |
|------|------|
| 앱콩 (도메인) | T119, T128 |
| 엠키 (데이터) | T123, T124, T127 |
| 뭉뭉 (프레젠테이션) | T120, T121, T122, T125, T126 |

> 앱콩은 T119(Fake 디버그 패널, FakeServer 연동)과 T128(quickstart 전체 검증)를 주도한다. T122는 세 사람이 기기 2대로 함께 수행한다.

- [ ] T119 [P] `fake/presentation/debug/FakePartnerPanel.kt`를 만든다. fake 변형에서만 보이는 디버그 오버레이로, 파트너 입장·선택 완료·종료 요청/응답·추리·채점·소원권 승인/거절을 버튼으로 실행한다(research R12).
- [ ] T120 `main/presentation/lobby/LobbyViewModel.kt`를 고친다. `CANCELLED` phase를 받으면 취소 안내 event를 한 번 내보내고, 방장의 '미션 시작'을 다시 활성화한다(FR-052b, ⚠️G12 서버 반영 전에는 fake에서만 확인).
- [ ] T121 [P] `androidTest/flow/FullRoundFlowTest.kt`를 만든다. Fake Repository를 주입한 Compose UI 테스트로, 방 생성 → 설정 → 종료 동의 → 추리 → 채점 → 결산 흐름을 확인한다.
- [ ] T122 [P] 모든 화면에 접근성을 점검한다: contentDescription, 최소 48dp 터치 영역, 큰 글꼴에서의 레이아웃, 다크 모드 토큰(`main/ui/theme/Color.kt`, `Theme.kt`).
- [ ] T123 [P] 로그를 점검한다. 토큰과 미션·추리 텍스트가 로그에 남지 않아야 하고, 로깅 인터셉터가 release 빌드에서 꺼져 있어야 한다. 대상은 `main/di/NetworkModule.kt`다(research R5).
- [ ] T124 **[⚠️G11 서버 지원 후]** FCM 연동을 추가한다.
  - `firebase-messaging` 의존성과 `app/google-services.json`
  - `main/data/push/HilMessagingService.kt`, `main/data/push/NotificationFactory.kt`(알림 채널, 문구 리소스)
  - 토큰 등록 API를 `main/data/api/SpringApiService.kt`에 추가
  - `app/src/main/AndroidManifest.xml`에 `POST_NOTIFICATIONS` 권한 선언
- [ ] T125 [P] **[⚠️G11 서버 지원 후]** `main/presentation/lobby/LobbyScreen.kt`에서 대기실에 처음 들어올 때 `POST_NOTIFICATIONS` 권한을 요청한다(Android 13 이상). 거부해도 게임 진행에는 영향이 없다(FR-050c).
- [ ] T126 **[⚠️G11 서버 지원 후]** `main/MainActivity.kt`와 `app/src/main/AndroidManifest.xml`에 딥링크(`hilkkeum://game/current`, `hilkkeum://coupon/incoming`)를 추가한다. 알림을 누르면 상태를 다시 조회한 뒤 이동한다.
- [ ] T127 서버 갭(G1–G14)이 해결될 때마다 다음을 고친다.
  - optional 필드를 필수 필드로 바꾸고, 로컬 보완 기록(`GameProgressStore`)의 해당 부분을 제거
  - `specs/001-couple-mission-game/contracts/api.md §7`을 갱신
- [ ] T128 quickstart.md를 실행한다. `testFakeDebugUnitTest`, `connectedFakeDebugAndroidTest`, 2기기 `remote` E2E 1–7단계를 진행하고 결과를 기록한다.

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Setup(1) ─▶ Foundational(2) ─▶ US1(3) ─▶ US2(4) ─▶ US3(5) ─▶ US4(6) ─▶ US5(7) ─▶ Polish(10)
                                 │                                          ▲
                                 ├──────────▶ US6(8) (US5 결산 연동 T107만 US5 이후)
                                 └──────────▶ US7(9) (US1의 대기실만 있으면 됨)
```

- **US1**: Foundational 이후 바로 시작
- **US2**: US1의 대기실(T042)과 세션에 의존. Fake로 방 상태를 미리 채우면 병렬로 진행 가능
- **US3–US5**: 각각 바로 앞 스토리가 만든 `GameRepository`, `RoundNavHost`, `GamePhasePolicy`를 확장한다. 레이어별 담당이 각자 자기 파일만 고치므로 스토리 안에서는 세 사람이 병렬로 진행한다. 스토리끼리는 앱콩의 계약 작업(T070 → T080 → T092) 순서만 지키면 된다
- **US6**: Foundational과 US1 이후 독립적으로 진행 가능. T112(결산 연동)만 US5 이후
- **US7**: US1 이후 언제든 가능

### 서버 의존 (remote 검증 차단 요소)

| 갭 | 막히는 작업 |
|----|-------------|
| G1 | 참여자의 US2 이후 전체 흐름(T057) |
| G2 | US4 채점 제출(T086) |
| G8 | US6 승인 화면(T110) |
| G11 | T124, T126 |

fake 변형으로는 모두 진행할 수 있다.

### Within Each User Story

```text
앱콩(도메인): 모델·Policy → Repository 인터페이스 → 유스케이스 → Fake  ──(계약 머지)──┐
엠키(데이터): DTO·API 서비스 (contracts/api.md 기준, 계약 머지 전 시작) → Repository 구현 ◀┤
뭉뭉(화면)  : 문구·Content + Preview (계약 머지 전 시작) → ViewModel 연결 ◀─────────────┘
```

---

## Parallel Examples

스토리마다 세 레인이 동시에 진행된다. `→`는 레인 안의 순서이고, `‖`는 레인 사이의 병렬이다.

**Foundational**:

```text
앱콩 (도메인)      : T007 → T008 → T009 → T010 → T011 → T021 → T025
엠키 (데이터)      : T012 → T013 → T014 → T015 → T016 → T017 → T018 → T019 → T020 → T028 → T029
뭉뭉 (프레젠테이션)   : T022 → T023 → T024 → T026 → T027
```

**US1**:

```text
앱콩 (도메인)      : T030 → T031 → T036 → T038 → T045
엠키 (데이터)      : T032 → T033 → T034 → T035 → T037 → T046
뭉뭉 (프레젠테이션)   : T039 → T040 → T041 → T042 → T043 → T044 → T047
```

**US2**:

```text
앱콩 (도메인)      : T048 → T049 → T050 → T051 → T052 → T058 → T060 → T061 → T068 → T069
엠키 (데이터)      : T053 → T054 → T055 → T056 → T057 → T059
뭉뭉 (프레젠테이션)   : T062 → T063 → T064 → T065 → T066 → T067
```

**US4**:

```text
앱콩 (도메인)      : T078 → T079 → T080 → T084 → T088 → T089
엠키 (데이터)      : T081 → T082 → T083
뭉뭉 (프레젠테이션)   : T085 → T086 → T087
```

**US6**:

```text
앱콩 (도메인)      : T100 → T101 → T106 → T107 → T114
엠키 (데이터)      : T102 → T103 → T104 → T105 → T115
뭉뭉 (프레젠테이션)   : T108 → T109 → T110 → T111 → T112 → T113
```

---

## Implementation Strategy

### MVP First

1. Phase 1–2를 마친다(빌드와 기반).
2. Phase 3(US1)을 마친 뒤 **검증한다**: fake 변형에서 방 생성·입장·복구가 되는지 확인한다. 서버팀 Spring 방 API와 연결도 확인한다.
3. Phase 4–7(US2–US5)을 마치면 **라운드 한 판을 끝까지 진행하는 MVP 데모**가 가능하다(fake 변형).
4. 서버 갭 G1·G2·G3이 반영되면 remote 변형에서 2기기 E2E를 진행한다.

### Incremental Delivery

US1 → US2 → US3 → US4 → US5(MVP 완성) → US6(보상) → US7(도움말) → 푸시(G11 서버 준비 후)

### 팀 진행 순서 (레이어별 3명)

자세한 내용은 위 "역할 분담" 섹션과 각 Phase의 **👥 담당** 표를 따른다.

| 기간 | 앱콩 (도메인) | 엠키 (데이터) | 뭉뭉 (프레젠테이션) |
|------|---------------|---------------|---------------------|
| Setup·Foundational | AppResult·AppError·ID·세션 모델, EntryPolicy, FakeServer | Gradle, 세션 저장소, 네트워크·인증, 폴링, DI | 공통 문구·컴포넌트, Route, NavHost, AppViewModel |
| US1 | 방 모델·InviteCode, RoomRepository, 유스케이스, Fake | 방 DTO·Spring API·RoomRepositoryImpl | 시작·방 생성·코드 입력·대기실 화면 |
| US2 | 게임 모델, GamePhasePolicy, MissionSelectionPolicy, 유스케이스, Fake | 게임·카탈로그 DTO, Node API, GameProgressStore, 구현 | RoundNavHost, 설정·미션·벌칙 화면 |
| US3–US5 | 종료·추리·채점·결산 계약과 Fake, ScorePolicy | 해당 Node API와 Repository 구현 | 진행·추리·채점·결산 화면 |
| US6 | 스탬프·소원권 모델, 유스케이스, Fake | Spring 스탬프·소원권 API와 구현 | 스탬프·소원권 화면, 대기실·결산 연동 |
| US7 | — | — | 이용 방법 화면 |
| Polish | Fake 디버그 패널, quickstart 검증 주도 | 로그 점검, FCM(G11), 서버 갭 반영 | 취소 안내, UI 흐름 테스트, 접근성, 딥링크·권한 요청 |

