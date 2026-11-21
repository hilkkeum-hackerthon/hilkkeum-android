# UI Navigation Contract: 힐끔 MVP

근거: spec 화면 구성(PRD §5), RULES §3. 라우트는 `presentation/navigation/HilRoute.kt`에서 kotlinx.serialization 타입 안전 Route로 한 곳에 정의한다.

## 1. 진입 가드 (`EntryPolicy`)

앱 시작 시 `AppViewModel`이 다음 순서로 시작 목적지를 정한다(FR-008).

1. 로컬 `MemberSession`이 없으면 `StartRoute`
2. `GET /rooms/{roomId}`가 401 또는 404면 세션을 지우고 `StartRoute`
3. 방이 `PENDING` 상태면 `RoomCreatedRoute`(방장)
4. 방이 `ACTIVE`이고 진행 중인 게임이 없으면(`currentGameId` 없음 ⚠️G1, 또는 게임이 `FINISHED`·`CANCELLED`) `LobbyRoute`
5. 게임이 진행 중이면 `GamePhasePolicy` 결과에 맞는 라운드 라우트(§3)

## 2. 라우트 목록

| # | Route | 화면 (PRD §5) | 인자 |
|---|-------|--------------|------|
| 1 | `StartRoute` | 시작/메인 | — |
| 2 | `RoomCreatedRoute` | 방 생성 완료 | — |
| 3 | `JoinRoute` | 코드 입력 | — |
| 4 | `LobbyRoute` | 홈/대기실 (하단 토글: 홈 ↔ 스탬프) | `tab: HOME \| STAMP` |
| 5 | `HowToRoute` | 이용 방법 | — |
| 6 | `RoundConfigRoute` | 미션 유형·개수 | — |
| 7 | `MissionPickRoute` | 내 미션 선택 | — |
| 8 | `PenaltyPickRoute` | 내 벌칙 선택 | — |
| 9 | `InProgressRoute` | 진행 (종료 확인 다이얼로그 #10 포함) | — |
| 11 | `GuessRoute` | 추리 입력 | — |
| 12 | `ScoringRoute` | 채점 | — |
| 13 | `TieBreakRoute` | 동점 승부 | — |
| 14 | `SettlementRoute` | 결산 | — |
| 15 | (`LobbyRoute` tab=STAMP) | 스탬프/소원권 | — |
| 16 | `CouponUseRoute` / `CouponApprovalRoute` | 소원권 사용 확인 / 승인 | `couponId` |

- #10 종료 확인은 `InProgressRoute` 안의 다이얼로그로 둔다. 표시 여부는 Screen이 소유한다(RULES §9).

## 3. 화면 단계 → 화면 (`RoundRoutePolicy`)

입력은 [data-model.md §3.1](../data-model.md)의 `RoundPhase`다.

| RoundPhase | Route | 대기 표시 |
|------------|-------|-----------|
| `LOBBY_CAN_START` | `LobbyRoute` | — ('미션 시작' 활성) |
| `LOBBY_WAITING_HOST` | `LobbyRoute` | "방장이 라운드를 설정 중" (FR-013) |
| (방장이 '미션 시작'을 누름) | `RoundConfigRoute` | — |
| `PICK_MISSION` | `MissionPickRoute` | — |
| `PICK_PENALTY` | `PenaltyPickRoute` | — |
| `WAIT_PARTNER_SETUP` | `PenaltyPickRoute` | "상대 설정 대기" (FR-017) |
| `IN_PROGRESS` / `END_REQUEST_SENT` / `END_REQUEST_RECEIVED` | `InProgressRoute` (+ 종료 확인 다이얼로그) | 요청자: "응답 대기" (미접속 구분은 ⚠️G7) |
| `GUESS` / `WAIT_PARTNER_GUESS` | `GuessRoute` | 제출 후 "상대 추리 대기" |
| `SCORE` / `WAIT_PARTNER_SCORE` | `ScoringRoute` | 채점 후 "상대 채점 대기" |
| `TIE_BREAK` | `TieBreakRoute` | 내가 고른 뒤 "상대 선택 대기" |
| `SETTLEMENT` | `SettlementRoute` | — |
| `CANCELLED` | `LobbyRoute` + 취소 안내 1회 | — |

- `RoundConfigRoute`는 서버 상태가 아니라 방장의 화면 이동으로 들어간다. `POST /games`가 성공하면 `PICK_MISSION`으로 넘어간다.
- 폴링 결과로 phase가 바뀌면 상위 `RoundNavHost`가 이 표에 따라 `popUpTo(LobbyRoute)` 후 해당 Route로 이동한다. 그래서 뒤로 가기로 이전 단계로 돌아갈 수 없다.
- 라운드 화면에서 뒤로 가기를 누르면 대기실로 가고, 게임은 그대로 유지된다.
