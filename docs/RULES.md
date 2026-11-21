# hilkkeum 코드 패턴 규칙

이 문서는 **hilkkeum Android 앱** 구현 시 적용하는 코드 작성 및 아키텍처
규칙이다.
적용 범위: **순수 Android 앱 모듈** (`app`) 및 앱 내부의 기능/레이어
패키지

프로젝트:
[hilkkeum-android](https://github.com/hilkkeum-hackerthon/hilkkeum-android)

## 1. 공통 원칙

-   기능 구현보다 코드 구조와 패턴의 일관성을 우선한다.
-   한 파일/클래스는 하나의 책임만 가진다.
-   예외(`throw`)를 UI까지 전파하지 않고 `AppResult`/`AppError`로
    변환한다.
-   로그인/권한/검증 로직은 화면이 아닌 도메인 정책 또는 라우트 가드에
    둔다.
-   하드코딩 문자열과 매직 넘버를 최소화하고 상수 및 리소스로 분리한다.
-   Android 플랫폼 API는 필요한 계층에서만 사용하고, 도메인 계층으로
    누수시키지 않는다.
-   화면 회전, 프로세스 재생성, 백그라운드 복귀 등 Android 생명주기를
    고려한다.

## 2. 모듈 및 계층 경계 규칙

-   프로젝트는 순수 Android 앱을 기준으로 구성한다.
-   기본 진입 모듈은 `app`이며, 기능이 커질 경우 필요에 따라 `core`,
    `feature` 등의 Android Library 모듈로 분리할 수 있다.
-   `presentation`은 `domain`의 모델과 유스케이스를 참조한다.
-   `domain`은 Android UI 프레임워크, Jetpack Compose, Android Context,
    Firebase SDK 등 외부 구현 기술에 직접 의존하지 않는다.
-   `data`는 `domain`에 정의된 Repository 계약을 구현하고 외부 데이터
    소스와 통신한다.
-   플랫폼 및 외부 서비스 구현(Firebase, 네트워크, 로컬 저장소 등)은
    `data` 또는 적절한 `core` 모듈에 둔다.
-   의존성 방향은 기본적으로 `presentation -> domain`,
    `data -> domain`을 따른다. `domain`에서 `data` 또는 `presentation`을
    참조하지 않는다.
-   모듈을 분리하더라도 모듈 간 의존성 방향과 계층 규칙은 동일하게
    유지한다.

권장 패키지 구조 예시:

``` text
com.hilkkeum.app
├── di
├── data
│   ├── database 
│   ├── mapper
│   ├── repositoryimpl
│   ├── dto
│   └── api
├── domain
│   ├── common
│   ├── model
│   ├── usecase
│   └── repository
└── presentation
		├── common
    ├── auth
		│   ├── login
		│   │   ├── LoginScreen.kt
		│   │   ├── LoginViewModel.kt
		│   │   ├── LoginAction.kt
		│   │   ├── LoginEvent.kt
		│		│   └── LoginUiState.kt
    │   └── register
    │   
    │   
    ├── ...
```

위 구조는 권장 예시이며, 실제 저장소의 Gradle 모듈 구성과 현재 패키지
구조를 우선 확인한 뒤 적용한다. 작은 기능은 단일 `app` 모듈 안에서
기능별 패키지로 시작해도 된다.

## 3. Android 네비게이션 규칙

-   Jetpack Navigation Compose를 사용하며 `NavHost`와 `NavController`를
    통해 화면 이동을 관리한다.
-   라우트는 문자열을 화면 곳곳에서 직접 조합하지 않고 타입 안전한 Route
    정의로 관리한다.
-   상세 화면은 별도 요구사항이 없는 한 다이얼로그/모달이 아닌 route
    push 방식으로 이동한다.
-   인증이 필요한 라우트 진입은 공통 인증 가드 또는 내비게이션
    진입점에서 처리한다.
-   인증이 필요한 액션이 로그인으로 연결된 경우, 로그인 성공 후 원래
    목적지 또는 액션을 복원할 수 있도록 `pendingRoute`/`pendingAction`
    정책을 적용한다.
-   화면 이동 로직은 Composable 내부의 비즈니스 로직과 분리하고, 화면
    이벤트를 상위 Navigation Host에서 수집해 처리한다.
-   뒤로 가기 동작과 백 스택 정리는 각 인증/온보딩 흐름에 맞게
    명시적으로 정의한다.
-   메인 탭의 순서와 역할별 노출 규칙은 한 곳에서 관리하며 화면마다 중복
    구현하지 않는다.

### 3-1. 메인 라우트 규격

-   루트 라우트 및 메인 탭은 프로젝트에서 사용하는 실제 화면 흐름에 맞춰
    단일 Route 정의로 관리한다.
-   기존 ConCafe에만 해당하는 `Cast`, `Cafe`, `cafeManagement`,
    `fanManagement`, `checkin` 등의 라우트는 hilkkeum 기능에 존재하지
    않는다면 가져오지 않는다.
-   메인 탭 순서, 로그인 필요 여부, 진입 가능한 사용자 상태는 Navigation
    정책 또는 별도 정책 클래스로 관리한다.
-   설정 화면처럼 특정 화면에서 진입하는 상세 화면은 해당 화면의 하위
    push 라우트로 취급한다.
-   아직 기능 연동이 없는 화면은 UI 범위와 미구현 범위를 구분하고,
    임의의 도메인/데이터 동작을 추가하지 않는다.

## 4. 패키지/파일 규칙

-   패키지 구조는 기능(feature)과 레이어(domain/data/presentation)를
    기준으로 유지한다.
-   파일명은 대표 타입명과 1:1 매칭한다.
-   하나의 파일에 public 타입은 1개를 기본으로 한다. 밀접하게 결합된
    작은 private 타입은 같은 파일에 둘 수 있다.
-   확장 함수와 매퍼는 `*Extensions.kt`, `*Mapper.kt`로 분리한다.
-   Composable 함수는 화면 단위로 `*Screen.kt`, 재사용 UI는
    `*Component.kt` 또는 디자인 시스템 패키지에 둔다.
-   Android 리소스 문자열, 색상, dimens, drawable 등은 `res/`에서
    관리하고 코드에 직접 하드코딩하지 않는다.
-   DI 모듈은 기능 또는 계층별로 구분하고, 제공하는 의존성의 책임이
    드러나도록 이름을 지정한다.

## 5. 네이밍 규칙

-   `UseCase`: 동사 + 대상 (`CreatePostUseCase`, `GetHomeFeedUseCase`)
-   `Repository`: 도메인명 + `Repository` (`PostRepository`)
-   `DataSource`: 데이터 소스명 + `DataSource` (`PostRemoteDataSource`)
-   `UiState`: 화면명 + `UiState` (`HomeUiState`)
-   `ViewModel`: 화면명 + `ViewModel` (`HomeViewModel`)
-   `Action`: 화면명 + `Action` (`HomeAction`)
-   `Event`: 화면명 + `Event` (`HomeEvent`)
-   `Route`: 화면명 또는 목적지명 + `Route` (`HomeRoute`)
-   Boolean은 `is`/`has`/`can` 접두어를 사용한다.
-   함수는 동사형, 클래스와 객체는 명사형으로 작성한다.
-   의미가 불명확한 축약어를 사용하지 않는다. 단, `ui`, `id`, `url` 등
    널리 쓰이는 약어는 허용한다.

## 6. 도메인 계층 규칙 (`domain`)

-   UseCase는 `operator fun invoke(...)`를 기본으로 한다.
-   UseCase는 다른 UseCase를 직접 호출하지 않고 Repository 및 Policy
    중심으로 구성한다.
-   도메인 모델은 불변(`data class` + `val`)을 기본으로 한다.
-   날짜/시간/좌표 등 검증이 필요한 값은 Policy 또는 도메인 전용 값
    객체에서 검증한다.
-   도메인 계층에는 Android `Context`, `Activity`, `Fragment`, `View`,
    `Composable`, `NavController`를 전달하지 않는다.
-   도메인 계층은 Firebase SDK, Retrofit, Room 등 데이터 구현
    라이브러리를 직접 참조하지 않는다.
-   도메인 반환 타입은 성공/실패를 명확히 표현하고, UI 표시용 문구나
    Android 리소스 ID를 포함하지 않는다.

## 7. 데이터 계층 규칙 (`data`)

-   외부 응답 DTO, 로컬 Entity와 도메인 모델을 분리한다.
-   DTO/Entity와 Domain 간 변환은 Mapper에서만 수행한다.
-   Repository 구현은 I/O와 데이터 조합을 담당하고 비즈니스 정책 판단은
    최소화한다.
-   실패는 항상 `AppError`로 매핑해 반환한다.
-   원격/로컬 데이터 소스 접근은 DataSource로 캡슐화한다.
-   화면 전용 집계 모델이 필요하면 `Repository -> UseCase -> ViewModel`
    순서로 전달한다.
-   네트워크, DB, Firebase 호출을 Composable 또는 ViewModel에서 직접
    수행하지 않는다.
-   Flow를 반환하는 데이터 스트림은 취소와 생명주기를 고려해 제공한다.

## 8. 프레젠테이션 규칙 (`presentation`)

-   화면은 상태를 렌더링하고 사용자 입력을 전달하는 역할을 담당한다.
    비즈니스 정책을 직접 판단하지 않는다.
-   화면 상태 관리는 MVI 패턴을 기본으로 한다.
-   ViewModel은 아래 3가지 요소를 기본 규격으로 사용한다.
    -   `uiState`: `StateFlow<ScreenUiState>`
    -   `event`: `SharedFlow<ScreenEvent>`
    -   `onAction`: `onAction(ScreenAction)`
-   UI는 `onAction(...)`만 호출하고 상태 변화는 `uiState` 구독으로
    반영한다.
-   단발성 효과(토스트, 네비게이션, 외부 화면 실행, 로그인 유도)는
    `event`로 전달한다.
-   `event`는 `MutableSharedFlow(replay = 0)`을 기본으로 사용한다.
    이벤트 유실을 허용할 수 없는 경우에는 버퍼 및 소비 정책을 명시한다.
-   `onAction` 내부에서만 호출되는 처리 메서드는 반드시 `private`로
    캡슐화한다.
-   외부(UI/다른 클래스)에서 직접 호출하면 안 되는 액션 핸들러를
    public으로 노출하지 않는다.
-   로그인 필요 액션은 공통 인증 정책과 `pendingRoute`/`pendingAction`
    규칙으로 처리한다.
-   Composable은 ViewModel의 내부 구현이나 Repository를 직접 참조하지
    않는다.

## 9. Android UI 및 화면 작성 규칙

-   Jetpack Compose를 UI 기본 기술로 사용한다.
-   화면은 상태/이벤트를 연결하는 Screen과 순수 UI를 렌더링하는
    Content로 분리한다.
-   `*Screen`은 ViewModel을 연결하고 상태 수집, 이벤트 수집, 화면
    생명주기와 관련된 effect 및 Navigation 연결을 담당한다.
-   `*Content`는 ViewModel을 직접 참조하지 않고 `UiState`와 Action
    콜백을 입력으로 받는다.
-   배너 페이지, 선택 인덱스 등 순수 UI 상태가 필요하면 Content에 필요한
    최소 상태만 전달한다.
-   `ModalBottomSheet`, `Dialog`, 풀스크린 오버레이 등 화면 바깥 계층
    UI의 표시 여부와 생명주기는 상위 Screen이 소유한다.
-   Content는 단일 화면 본문 렌더링만 담당한다.
-   Preview는 Content 기준으로 작성하며 ViewModel이나 실제 네트워크/DB
    없이 렌더링 가능해야 한다.
-   화면 렌더링, 섹션 배치, 스타일링은 Content에 두고 이벤트 수집 및
    라우팅 연결은 Screen에 둔다.
-   새 화면은 `Screen + Content` 2단 구성을 우선 적용한다. 단순하고
    재사용 필요가 없는 화면은 과도하게 분리하지 않아도 된다.
-   상태 수집에는 `collectAsStateWithLifecycle()`을 우선 사용한다.
-   `LaunchedEffect`와 `DisposableEffect`는 key와 정리 동작을 명확히
    지정하고, Composable 재구성 때 불필요한 작업이 반복되지 않도록 한다.
-   Activity/Context는 필요한 Android 플랫폼 작업에만 사용하고
    ViewModel이나 도메인 모델에 보관하지 않는다.

### 9-1. Compose 기준 예시

``` kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (HomeNavigationEvent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> {
                    onNavigate(HomeNavigationEvent.Detail(event.id))
                }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit
) {
    LazyColumn {
        items(
            items = uiState.items,
            key = { item -> item.id }
        ) { item ->
            Text(
                text = item.title,
                modifier = Modifier.clickable {
                    onAction(HomeAction.ClickItem(item.id))
                }
            )
        }
    }
}
```

핵심 패턴: 상위 Screen은 상태 수집과 effect/Navigation 연결을 담당하고,
하위 Content는 `UiState + Action` 기반의 렌더링만 담당한다.

## 10. 인증/권한 패턴 규칙

-   앱 시작 시 진입 화면과 초기 세션 복원 흐름을 명확히 정의한다.
-   게스트 허용/로그인 필수 기능을 기능 단위로 구분하고 인증 정책에
    명시한다.
-   인증이 필요한 화면 및 액션은 공통 라우트 가드 또는 인증 정책을
    거친다.
-   서버 권한 검증과 클라이언트 가드를 함께 적용한다. 클라이언트 가드는
    UX 목적이며 보안 경계로 간주하지 않는다.
-   `AuthRepository`는 프로젝트 인증 방식에 맞춰 `signIn`, `signUp`,
    `signOut`, `restoreSession`, `getCurrentUser`와
    `observeCurrentUser()`를 제공하는 것을 기본으로 한다.
-   세션 변경 감지는 화면 재생성이나 임의 강제 이동으로 해결하지 않고
    `observeCurrentUser()` 기반 스트림으로 처리한다.
-   앱 수준 ViewModel 또는 인증 상태 관리자에서 세션 변화를 구독하고,
    감지 시 Navigation 및 사용자 상태를 재계산한다.
-   권한별 화면 노출 여부는 UI 곳곳에 하드코딩하지 않고 Role/Permission
    정책으로 관리한다.
-   서버 권한 검증은 실제 백엔드가 제공하는 인증/인가 방식에 맞춰
    구현한다. Firebase를 사용하지 않는 환경에서 Firestore
    Rules/Functions를 전제로 작성하지 않는다.

## 11. 비동기 및 상태 처리 규칙

-   suspend 함수는 취소 가능성을 보장하고 `CancellationException`을 일반
    오류로 변환하거나 삼키지 않는다.
-   로딩/성공/실패 상태를 명시적으로 분리한다.
-   재시도 가능한 실패와 재시도 불가능한 실패를 구분한다.
-   화면 생명주기에 종속된 Flow 수집은 `repeatOnLifecycle` 또는
    `collectAsStateWithLifecycle()` 등 생명주기 인식 API를 사용한다.
-   ViewModel의 비동기 작업은 `viewModelScope`에서 실행한다.
-   작업 취소가 필요한 경우 Job을 관리하고 중복 요청 정책을 명시한다.
-   페이징은 Paging 3 사용을 우선 검토하며, 별도 페이징 모델을 사용할
    경우 `nextCursor`/`hasNext` 등 다음 페이지 조건을 명확히 정의한다.
-   UI 상태는 화면에서 필요한 최소 정보만 보유하고, 변경은 불변 객체의
    `copy()`를 통해 수행한다.

## 12. 오류 처리 규칙

-   에러 타입은 `Unauthorized`, `PermissionDenied`, `ValidationFailed`,
    `NetworkError`, `NotFound`, `Unknown`을 기본으로 한다.
-   사용자 액션에서 발생한 오류는 사용자에게 표시할 수 있는 안전한
    문구로 변환 가능해야 한다.
-   로그에는 내부 원인(cause)을 남기되 비밀번호, 토큰, 개인정보 등 민감
    정보를 기록하지 않는다.
-   UI에는 서버 응답 원문이나 내부 예외 메시지를 그대로 노출하지 않는다.
-   예상 가능한 실패는 결과 타입으로 표현하고, 예기치 못한 예외는 공통
    경계에서 처리한다.

## 13. UI 컴포넌트 및 디자인 시스템 규칙

-   공통 UI 컴포넌트와 디자인 토큰을 우선 재사용한다.
-   여러 화면에서 반복되는 UI는 공통 컴포넌트 또는
    `core/designsystem`으로 승격한다.
-   특정 화면에서만 쓰이는 컴포넌트는 해당 feature 내부에 둔다.
-   빈 상태, 로딩 상태, 에러 상태를 화면 요구사항에 맞게 제공한다.
-   접근성(contentDescription, 클릭 영역, 색 대비, 글자 크기)을 기본
    준수한다.
-   색상/타이포그래피/간격은 디자인 시스템 토큰으로 관리하고 화면 코드에
    임의 값을 반복해서 작성하지 않는다.
-   문자열은 `stringResource()`와 Android string resource를 사용한다.
-   다크 모드 및 시스템 설정 반영 정책을 디자인 시스템에서 일관되게
    관리한다.

### 13-1. 색상 토큰 규칙

-   화면 코드에 hex 색상 하드코딩을 금지하고 hilkkeum 디자인 시스템의
    시맨틱 색상 토큰을 사용한다.
-   라이트/다크 테마 값은 단일 토큰 정의를 기준으로 관리한다.
-   브랜드 가이드 또는 외부 서비스의 공식 브랜드 색상처럼 명확한 사유가
    있는 경우에만 별도 색상을 허용하고, 사용 위치를 제한한다.
-   색상 토큰의 이름과 사용 기준은 프로젝트 디자인 시스템 문서에
    정의한다.

## 14. 테스트 규칙

-   도메인 UseCase 단위 테스트를 우선 작성한다.
-   Repository 구현은 데이터 소스를 Fake/Mock으로 대체해 성공/실패/빈
    데이터/경계 조건을 검증한다.
-   ViewModel은 `uiState` 변화와 `event` 발생을 테스트한다.
-   인증/권한 시나리오와 로그인 가드 시나리오를 테스트한다.
-   핵심 사용자 흐름은 Compose UI 테스트 또는 통합 테스트 대상으로
    지정한다.
-   Coroutine/Flow 테스트는 테스트용 Dispatcher와 공식 테스트 도구를
    사용해 실행 순서를 제어한다.
-   테스트는 실제 네트워크나 운영 계정에 의존하지 않도록 구성한다.

## 15. 금지 규칙

-   ViewModel 또는 UI에서 Retrofit/Room/Firebase 등 데이터 소스를 직접
    호출하는 구현 금지
-   Composable에서 비즈니스 정책 및 권한 판단을 하드코딩하는 구현 금지
-   domain/model에 mutable 상태(`var`)를 남용하는 구현 금지
-   에러를 `Exception` 문자열로만 처리하는 방식 금지
-   인증 필요 기능을 인증 가드 없이 노출하는 구현 금지
-   `event`를 상태처럼 재소비 가능한 구조로 저장하는 구현 금지
-   `onAction` 전용 내부 메서드를 `public`으로 노출하는 구현 금지
-   `GlobalScope` 사용 금지
-   Activity/Fragment/Context를 ViewModel 또는 Domain 모델에 장기간
    보관하는 구현 금지
-   Composable 본문에서 직접 네트워크/DB I/O를 실행하는 구현 금지
-   `!!`를 통한 무분별한 null 강제 해제 금지
-   UI 코드에 색상/문자열/치수 값을 반복 하드코딩하는 구현 금지

## 16. 코드 리뷰 체크리스트

-   계층 및 모듈 의존성 방향을 위반하지 않았는가?
-   인증/권한 가드가 필요한 기능에 적용되어 있는가?
-   에러 매핑이 `AppError`로 일관되는가?
-   상태 모델(`UiState`)이 로딩/실패/빈 상태를 필요한 범위에서
    포함하는가?
-   `uiState`, `event`, `onAction` 패턴을 준수하는가?
-   `onAction` 전용 처리 메서드가 `private`로 캡슐화되어 있는가?
-   Screen과 Content의 책임이 분리되어 있는가?
-   Flow 수집이 Android 생명주기를 고려하는가?
-   ViewModel에서 Android UI 객체나 Context를 보관하지 않는가?
-   테스트 가능한 구조이며 핵심 성공/실패 시나리오가 검증되는가?
-   중복 코드가 공통화 가능한 수준인지 검토했는가?
-   문자열/색상/치수 등 리소스와 디자인 토큰을 준수하는가?

## 17. 적용 우선순위

1.  인증/권한/라우트 가드 규칙
2.  도메인 반환 타입(`AppResult`/`AppError`/페이징 모델) 일관성
3.  계층 분리 및 의존성 방향
4.  UI 상태/생명주기 처리 규칙
5.  테스트 규칙 및 디자인 시스템 적용

## 18. 코드 배치 규칙 (Kotlin)

-   클래스 내부 프로퍼티는 한 줄씩 선언하고 프로퍼티 사이를 개행한다.
-   메서드 내부에서는 지역 변수 선언과 초기값 설정을 상단에 모은다.
-   지역 변수 선언부는 줄 사이 개행 없이 연속 배치한다.
-   비즈니스 실행 및 외부 메서드 호출은 가능한 한 메서드 하단에
    배치한다.
-   변수 선언 블록과 `invoke()` 등 실제 실행 호출 블록 사이에는
    개행한다.
-   `private` 메서드는 클래스 상단에 배치한다.
-   외부에서 호출하는 public 메서드는 클래스 하단에 배치한다.
-   `init` 블록은 클래스 하단(메서드 아래)에 배치한다.
-   조건 분기는 `if-else`를 기본으로 사용한다.
-   `if (condition) return` 형태의 조기 반환보다 `if-else`로 명시적으로
    분기한다.
-   단, 중첩 인덴트가 3단계 이상이 될 경우 가독성을 위해 조기 반환으로
    평탄화할 수 있다.
-   Kotlin 공식 코딩 컨벤션과 프로젝트의 ktlint/detekt 설정이 있는 경우
    이를 함께 준수한다.

### 18-1. Kotlin 예시

``` kotlin
@HiltViewModel
class SampleViewModel @Inject constructor(
    private val getHomeFeedUseCase: GetHomeFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val event: SharedFlow<HomeEvent> = _event.asSharedFlow()

    private fun buildRequest(userId: String?): HomeRequest {
        val resolvedUserId = userId.orEmpty()
        val timestamp = System.currentTimeMillis()

        return HomeRequest(
            userId = resolvedUserId,
            timestamp = timestamp
        )
    }

    private suspend fun executeLoad(request: HomeRequest) {
        val result = getHomeFeedUseCase(request.userId)

        _uiState.update {
            it.copy(isLoading = false)
        }

        _event.emit(HomeEvent.LoadCompleted(result))
    }

    private fun onEnter(userId: String?) {
        val request = buildRequest(userId)
        val isFirstLoad = _uiState.value.items.isEmpty()

        if (isFirstLoad) {
            _uiState.update {
                it.copy(isLoading = true)
            }
        } else {
            // 기존 데이터가 있는 경우의 처리
        }

        viewModelScope.launch {
            executeLoad(request)
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.Enter -> onEnter(action.userId)
            is HomeAction.Refresh -> onEnter(action.userId)
        }
    }
}
```

### 18-2. Kotlin 조건문 예시

``` kotlin
// 권장
if (isLoggedIn) {
    submitPost()
} else {
    showLoginRequired()
}

// 비권장
if (!isLoggedIn) return
submitPost()

// 예외 허용: 중첩이 깊어지는 경우 조기 반환으로 평탄화
if (!isLoggedIn) return
if (!hasPermission) return
if (!isValid) return

submitPost()
```

------------------------------------------------------------------------

## 19. Android 구현 시 추가 유의사항

-   ViewModel은 화면 구성 변경에도 유지되어야 하는 UI 상태와 화면 로직을
    소유한다.
-   화면 회전이나 프로세스 종료 후에도 복원해야 하는 최소 상태는
    `SavedStateHandle` 사용을 검토한다.
-   화면에 표시할 수 있는 일시적 UI 상태와 영구 저장 데이터의 책임을
    구분한다.
-   앱 전역 초기화는 `Application`에서 수행하고, Activity 또는
    Composable에 중복 초기화 코드를 두지 않는다.
-   백그라운드에서 보장되어야 하는 지연 작업은 일반 Coroutine 대신
    WorkManager 사용을 검토한다.
-   민감 정보는 일반 로그, 평문 파일, 하드코딩 상수에 저장하지 않는다.
-   Manifest 권한은 실제 사용하는 기능에 필요한 최소 권한만 선언한다.
-   딥 링크와 외부 Intent 입력값은 신뢰하지 않고 유효성 검증 후
    처리한다.
