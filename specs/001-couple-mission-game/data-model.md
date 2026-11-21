# Data Model: 힐끔 MVP (클라이언트 도메인 모델)

**Feature**: [spec.md](./spec.md) | **Research**: [research.md](./research.md) | **API**: [contracts/api.md](./contracts/api.md)

이 문서는 `domain/model`에 둘 **불변 도메인 모델**, 검증 규칙, 상태 전이를 정의한다.
- DTO ↔ 도메인 변환은 `data/mapper`에서만 한다(RULES §7).
- ID는 서버 명세를 따라 Long 값 클래스로 감싼다(`RoomId`, `MemberId`, `GameId`, `PlayerMissionId`, `GuessId`, `CouponId`, `MissionId`, `PenaltyId`).
- `⚠️G#`는 [contracts/api.md §7](./contracts/api.md)의 갭 항목을 가리킨다.

---

## 1. MemberSession (로컬 저장)

| 필드 | 타입 | 규칙 |
|------|------|------|
| roomId | RoomId | 방 생성(API 1)·입장(API 2) 응답 |
| memberId | MemberId | |
| memberToken | String | 모든 요청의 Bearer 토큰. 로그에 남기지 않는다 |
| role | `MemberRole` | `CREATOR` \| `JOINER` |

- DataStore에 저장하고 백업에서 제외한다. 앱을 재설치하면 사라진다(PRD 4.1).
- 서버가 401을 돌려주면 세션을 지우고 시작 화면으로 보낸다.

## 2. Room

| 필드 | 타입 | 규칙 |
|------|------|------|
| roomId | RoomId | |
| inviteCode | `InviteCode`? | 방장에게만 있다(API 1 응답, 로컬에 저장). 6자리 영숫자. 입력은 대문자로 바꾸고 공백을 없앤 뒤 검증한다 |
| status | `RoomStatus` | `PENDING` → `ACTIVE` |
| members | List<{memberId, role}> | 최대 2명 |
| currentGameId | GameId? | ⚠️G1 (서버 추가 요청 중) |
| isPartnerJoined | Boolean (파생) | `memberCount == 2` |

## 3. Game (= spec의 "라운드")

| 필드 | 타입 | 출처 |
|------|------|------|
| gameId | GameId | API 4, 5 |
| status | `GameStatus` | `SETTING`, `SELECTING`, `PLAYING`, `SCORING`, `TIEBREAK`, `FINISHED`, `CANCELLED`(⚠️G12), `UNKNOWN`(처음 보는 값) |
| dateType | `DateType` | `ONLINE` \| `OFFLINE` |
| missionCount | Int | 1..3 (⚠️G3: 서버는 3 또는 5) |
| mySelectionCompleted / partnerSelectionCompleted | Boolean | API 5 |
| endRequestPending | Boolean | API 5 |
| endRequestedByMe | Boolean? | 서버 필드(⚠️G4)가 오면 그 값을 쓴다. 아직 없으면 로컬 기록(내가 API 10을 호출했는지)으로 판단 |
| progress | `GameProgress` | 서버 필드(⚠️G4)와 로컬 기록을 합친 값: myMissionPicked, myPenaltyPicked, myGuessSubmitted, partnerGuessSubmitted, myReviewSubmitted, myJudgmentSubmitted |

### 3.1 화면 단계 판정: `GamePhasePolicy`

서버 `status`와 `progress`를 받아 화면 단계(`RoundPhase`)를 정한다. 이 판정은 domain의 순수 함수로 두고 단위 테스트한다.

| 서버 status | 조건 | RoundPhase |
|-------------|------|-----------|
| (게임 없음) | 내가 CREATOR | `LOBBY_CAN_START` |
| (게임 없음) | 내가 JOINER | `LOBBY_WAITING_HOST` (FR-013) |
| `SETTING` / `SELECTING` | 미션을 아직 안 고름 | `PICK_MISSION` |
| 〃 | 미션은 골랐고 벌칙을 안 고름 | `PICK_PENALTY` |
| 〃 | 둘 다 고름 | `WAIT_PARTNER_SETUP` (FR-017) |
| `PLAYING` | endRequestPending = false | `IN_PROGRESS` |
| `PLAYING` | endRequestPending이고 내가 요청함 | `END_REQUEST_SENT` |
| `PLAYING` | endRequestPending이고 상대가 요청함 | `END_REQUEST_RECEIVED` |
| `SCORING` | 내 추리 미제출 | `GUESS` |
| `SCORING` | 내 추리 제출, 상대 추리 미제출 | `WAIT_PARTNER_GUESS` (FR-029) |
| `SCORING` | 둘 다 추리 제출, 내 채점(수행 여부 + 정답 판정) 미완료 | `SCORE` |
| `SCORING` | 내 채점 완료 | `WAIT_PARTNER_SCORE` (FR-033) |
| `TIEBREAK` | | `TIE_BREAK` |
| `FINISHED` | | `SETTLEMENT` |
| `CANCELLED` | | `CANCELLED` → 대기실로 보내고 안내 1회 |

상태 전이(서버 기준):

```text
(없음) ─POST /games─▶ SETTING/SELECTING ─(양측 미션·벌칙 완료)─▶ PLAYING
PLAYING ─end-request─▶ PLAYING(endRequestPending) ─거절─▶ PLAYING
                                                 └─수락─▶ SCORING
SCORING ─(양측 추리·수행·판정 완료)─▶ FINISHED (점수 다름) | TIEBREAK (동점)
TIEBREAK ─(DRAW: 다음 roundNo)─▶ TIEBREAK ─(DECIDED)─▶ FINISHED
결산 전 모든 단계 ─(24시간 무활동, ⚠️G12)─▶ CANCELLED
```

새 게임은 `FINISHED`나 `CANCELLED` 이후에 방장이 `POST /games`로 다시 만든다.

## 4. Catalog

| 모델 | 필드 |
|------|------|
| `Mission` | id: MissionId, content: String |
| `Penalty` | id: PenaltyId, content: String |
| `MyMission` | playerMissionId: PlayerMissionId? (⚠️G2), missionId, content. 진행 화면과 채점 화면에서 쓴다 |

`MissionSelectionPolicy` 검증 실패 사유:
- 선택 수가 missionCount와 다름: `COUNT_MISMATCH`
- 같은 미션을 두 번 고름: `DUPLICATE`
- 목록이 비었거나 missionCount보다 적음: `EMPTY_CATALOG` (FR-016)
- 벌칙이 정확히 1개가 아님: `PENALTY_REQUIRED`

## 5. Guess

| 필드 | 타입 | 규칙 |
|------|------|------|
| guessNo | Int | 1..missionCount (API 12) |
| content | `GuessText` | trim 후 1..30자, grapheme 단위로 셈(이모지 1개 = 1자) (FR-027) |
| guessId | GuessId? | 받은 추리(API 13)에만 있음 |

`GuessPolicy`:
- 모든 칸이 유효해야 제출할 수 있다.
- 입력은 30자에서 자른다.

## 6. Scoring (내 채점 입력)

| 필드 | 타입 | 규칙 |
|------|------|------|
| reviews | Map<PlayerMissionId, Boolean> | 내 미션 전부에 값이 있어야 함 → API 14 (FR-031) |
| judgments | Map<GuessId, Boolean> | 받은 추리 전부에 값이 있어야 함 → API 15 (FR-032) |

- 두 API를 차례로 호출한다. 하나가 이미 성공했다면(409) 성공으로 보고 다음으로 넘어간다.
- 정답 판정은 **추리 단위**다(서버 명세). spec의 "맞힌 미션 수"는 "정답 판정된 추리 수"로 계산하며, 같은 미션을 여러 번 맞혔을 때의 처리는 ⚠️G13이다.

## 7. Settlement (API 16)

| 필드 | 타입 |
|------|------|
| decidedBy | `SCORE` \| `RPS` |
| winner | MemberId |
| myScore / partnerScore | Int |
| isMyWin | Boolean (파생) |
| penalty | Penalty: 승자가 고른 벌칙 = 패자가 수행할 벌칙 (FR-037) |
| reveal | `SettlementReveal`? (⚠️G5): 미션, 수행 여부, 추리, 정답 여부, 점수 내역 |

`ScorePolicy`(domain): `performedCount × 15 + correctGuessCount × 10`. 서버 값이 정답이며, 이 정책은 테스트 기준과 내역 표시에 쓴다.

## 8. TieBreak (API 17, 18)

| 필드 | 타입 |
|------|------|
| roundNo | Int |
| state | `WAITING`(myMoveSubmitted, partnerMoveSubmitted) \| `DRAW`(myChoice, partnerChoice, nextRoundNo) \| `DECIDED`(myChoice, partnerChoice, winner) |
| choice | `RpsChoice`: `ROCK` \| `PAPER` \| `SCISSORS` |

- `DRAW`이면 결과를 2초 보여준 뒤 다음 라운드 선택 화면으로 초기화한다(FR-042).
- `DECIDED`이면 결산을 조회한다.

## 9. StampWallet / WishCoupon

`StampStatus` (API 19):

| 필드 | 타입 | 규칙 |
|------|------|------|
| heartCount | Int | 0..maxHearts(3). spec의 "스탬프"와 같은 개념 |
| canExchange | Boolean | 서버 값 (FR-044) |
| hasActiveCoupon | Boolean | |
| showFullWarning | Boolean (파생) | heartCount == maxHearts이고 hasActiveCoupon일 때 (FR-047) |

`WishCoupon` (API 21, 22):

| 필드 | 타입 | 규칙 |
|------|------|------|
| couponId | CouponId | |
| status | `AVAILABLE` \| `PENDING` \| `USED` | 만료된 소원권은 응답에 없다(FR-049) |
| issuedAt / expiresAt | OffsetDateTime | **서버 값을 그대로 쓴다.** 예: 11/21 20:00 발급 → 12/21 20:00 만료 |
| remainingDays | Int | 서버 값. D-day 표시에 쓴다(FR-046) |

상태 전이:

```text
AVAILABLE ─use-request─▶ PENDING ─respond(approved=true)─▶ USED
PENDING ─respond(approved=false)─▶ AVAILABLE
만료 ─▶ 조회 결과에서 빠짐
```

`IncomingCouponRequest`: couponId, requestedAt (⚠️G8)

## 10. AppError (RULES §12 + Conflict)

`Unauthorized`, `PermissionDenied`, `ValidationFailed(reason)`, `NetworkError(isRetryable)`, `NotFound`, `Conflict(reason)`, `Unknown`

| 사유 | 연결된 요구사항 |
|------|-----------------|
| `INVALID_INVITE_CODE` | FR-004 |
| `ROOM_FULL` | FR-005 |
| `EMPTY_CATALOG`, `COUNT_MISMATCH`, `DUPLICATE`, `PENALTY_REQUIRED` | FR-014~016 |
| `GUESS_EMPTY`, `GUESS_TOO_LONG` | FR-027 |
| `ALREADY_SUBMITTED`, `PHASE_MISMATCH` | 공통 |
| `COUPON_HELD`, `COUPON_PENDING` | FR-045, FR-048a |

사유 → 사용자 문구는 `presentation/common/ErrorMessageMapper`에서 문자열 리소스로 매핑한다.
