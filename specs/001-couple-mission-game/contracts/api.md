# API Contract: 힐끔 MVP (서버 명세 기준)

**원본**: [`docs/API명세서.md`](../../../docs/API명세서.md) (Notion 기준, 2026-11-22 반영)

이 문서는 서버 명세를 **앱 구현 관점에서 정리**한 것이다. 원본과 다른 점이 있으면 원본을 따른다. 아래 §7 "갭·충돌"은 서버팀과 협의해야 하는 항목이다.

## 0. 공통

| 항목 | 내용 |
|------|------|
| 서버 | **Spring Boot** (방·소원권·스탬프), **Node.js** (게임·미션·벌칙·추리·채점·결산·가위바위보). base URL이 서로 다르다 |
| 인증 | `Authorization: Bearer {memberToken}`. 토큰은 방 생성·입장 응답으로 받는다(기기 등록 API 없음) |
| ID | 모든 ID는 정수(Long): `roomId`, `memberId`, `gameId`, `playerMissionId`, `guessId`, `couponId` |
| 시각 | ISO-8601, 오프셋 포함(예: `2026-11-21T20:00:00+09:00`) |
| 실시간 | **없음**. 앱이 주기적으로 조회(폴링)한다([realtime-and-push.md](./realtime-and-push.md)) |
| 중복 방지 | 서버가 상태 기준으로 막고 409를 돌려준다. 앱은 409를 받으면 상태를 다시 조회해 화면을 맞춘다 |

### 오류 → `AppError` 매핑

| HTTP | 의미 (원본 명세) | AppError |
|------|-----------------|----------|
| 400 | 잘못된 입력, 교환 조건 미충족 | `ValidationFailed` |
| 401 | 인증 실패 | `Unauthorized` → 로컬 세션 삭제 후 시작 화면 |
| 403 | 참가자·소유자 아님 | `PermissionDenied` |
| 404 | 코드·방·게임·소원권 없음 | `NotFound` (초대 코드 입장에서는 `ValidationFailed(INVALID_INVITE_CODE)`) |
| 409 | 정원 초과, 상태 불일치, 이미 제출함, 소원권 보유 중·요청 중 | `Conflict(reason)` → 상태 재조회 |
| 5xx·네트워크 | | `NetworkError(isRetryable = true)` |

오류 본문 형식은 원본에 정의되어 있지 않다(§7 G9). 앱은 HTTP 상태와 호출한 API 맥락으로 사유를 정한다.

---

## 1. 방·사용자 — Spring Boot

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 1 | POST | `/api/v1/rooms` | — | `201 { roomId, inviteCode, memberId, memberToken, status: "PENDING" }` | FR-002 |
| 2 | POST | `/api/v1/rooms/join` | `{ inviteCode }` | `200 { roomId, memberId, memberToken, role: "JOINER", roomStatus: "ACTIVE" }` · `404` 잘못된 코드 · `409` 정원 초과 | FR-003~006 |
| 3 | GET | `/api/v1/rooms/{roomId}` | — | `200 { roomId, status, memberCount, capacity, members: [{ memberId, role }] }` | FR-007, FR-008 |

- `role`: `CREATOR`(방장) | `JOINER`(참여자)
- 방 `status`: `PENDING` → `ACTIVE`
- 앱은 `roomId`, `memberId`, `memberToken`, `role`을 로컬(DataStore, 백업 제외)에 저장한다. 이 정보로 재접속한다.

## 2. 게임 설정·상태 — Node.js

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 4 | POST | `/api/v1/games` | `{ roomId, dateType: "ONLINE"\|"OFFLINE", missionCount, themeId? }` | `201 { gameId, roomId, dateType, missionCount, status: "SETTING" }` | FR-011 |
| 5 | GET | `/api/v1/games/{gameId}` | — | `200 { gameId, status, dateType, missionCount, mySelectionCompleted, partnerSelectionCompleted, endRequestPending }` | 모든 라운드 화면 |

게임 `status`(원본에 나온 값): `SETTING`, `SELECTING`, `PLAYING`, `SCORING`, `TIEBREAK`, `FINISHED`. 앱 `RoundPhase`와의 대응은 [data-model.md](../data-model.md) §3.1에 있다.

## 3. 미션·벌칙 — Node.js

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 6 | GET | `/api/v1/missions?dateType=` | — | `200 { missions: [{ id, content }] }` | FR-014, FR-016 |
| 7 | GET | `/api/v1/penalties` | — | `200 { penalties: [{ id, content }] }` | FR-015 |
| 8 | POST | `/api/v1/games/{gameId}/missions` | `{ missionIds: [..] }` | `200 { gameId, selectedCount, selectionCompleted }` | FR-014, FR-017 |
| 9 | POST | `/api/v1/games/{gameId}/penalty` | `{ penaltyId }` | `200 { gameId, penaltySelectionCompleted }` | FR-015, FR-018 |

원본 표에는 9번 경로가 `/pen`, 상세에는 `/penalty`로 적혀 있다. 앱은 상세의 `/penalty`를 쓴다(§7 G10).

## 4. 진행·종료 — Node.js

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 10 | POST | `/api/v1/games/{gameId}/end-request` | — | `200 { gameId, endRequestPending: true, requestedByMemberId }` | FR-021, FR-024 |
| 11 | POST | `/api/v1/games/{gameId}/end-response` | `{ accepted }` | `200 { gameId, accepted, status }`. 수락하면 `SCORING`, 거절하면 `PLAYING` | FR-022 |

## 5. 추리·채점·결산·동점 — Node.js

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 12 | POST | `/api/v1/games/{gameId}/guesses` | `{ guesses: [{ guessNo, content }] }` | `201 { gameId, submittedCount, submissionCompleted }` | FR-026~030 |
| 13 | GET | `/api/v1/games/{gameId}/received-guesses` | — | `200 { gameId, guesses: [{ guessId, guessNo, content }], partnerSubmissionCompleted }` | FR-032 |
| 14 | POST | `/api/v1/games/{gameId}/mission-reviews` | `{ reviews: [{ playerMissionId, isCompleted }] }` | `200 { gameId, submittedCount, submissionCompleted }` | FR-031 |
| 15 | POST | `/api/v1/games/{gameId}/judgments` | `{ judgments: [{ guessId, isCorrect }] }` | `200 { gameId, judgedCount, judgmentCompleted }` | FR-032 |
| 16 | GET | `/api/v1/games/{gameId}/result` | — | `200 { gameId, status, decidedBy, winnerMemberId, players: [{ memberId, score, result }], penalty: { id, content } }` | FR-035~039 |
| 17 | POST | `/api/v1/games/{gameId}/rps/moves` | `{ choice: "ROCK"\|"PAPER"\|"SCISSORS" }` | `200 { gameId, roundNo, myMoveSubmitted, partnerMoveSubmitted, status: "WAITING" }` | FR-040, FR-041 |
| 18 | GET | `/api/v1/games/{gameId}/rps/result?roundNo=` | — | `WAITING` \| `DRAW { myChoice, partnerChoice, nextRoundNo }` \| `DECIDED { myChoice, partnerChoice, winnerMemberId, decidedBy: "RPS" }` | FR-041, FR-042 |

- 원본 표에는 15번 경로가 `judgement`, 상세에는 `/judgments`로 적혀 있다. 앱은 `/judgments`를 쓴다(§7 G10).
- 결산 `decidedBy`는 16번이 `SCORE`, 18번이 `RPS`다.

## 6. 스탬프·소원권 — Spring Boot

| # | Method | Path | Request | Response | FR |
|---|--------|------|---------|----------|----|
| 19 | GET | `/api/v1/stamps` | — | `200 { memberId, heartCount, maxHearts, canExchange, hasActiveCoupon }` | FR-043, FR-044, FR-047 |
| 21 | POST | `/api/v1/coupons/exchange` | — | `201 { couponId, memberId, heartCount, coupon: { status, issuedAt, expiresAt } }` · `409` | FR-045 |
| 22 | GET | `/api/v1/coupons` | — | `200 { hasActiveCoupon, coupon: { couponId, status, issuedAt, expiresAt, remainingDays } \| null }` | FR-046, FR-049 |
| 23 | POST | `/api/v1/coupons/{couponId}/use-request` | — | `200 { couponId, status: "PENDING", requestedByMemberId, waitingForPartner }` · `409` | FR-048 |
| 24 | POST | `/api/v1/coupons/{couponId}/respond` | `{ approved }` | `200 { couponId, status: "USED"\|"AVAILABLE", approved, usedAt }` | FR-048a |

- 20번(`/internal/v1/stamps/award`)은 서버 간 내부 API라서 앱은 호출하지 않는다.
- 19번 경로의 담당 서버가 원본 표(Node.js)와 상세(Spring Boot)에서 다르다. 앱은 상세를 따라 Spring Boot로 호출한다(§7 G10).
- 소원권 `status`: `AVAILABLE` → `PENDING` → `USED`. 거절되면 `AVAILABLE`로 돌아간다. 만료된 소원권은 조회 결과에 나오지 않는다.

---

## 7. 갭·충돌 (서버팀 협의 필요)

앱 구현 시 **임시 처리** 열의 방식으로 진행하고, 서버 반영 후 바꾼다. tasks.md에서 `⚠️G#`로 참조한다.

| ID | 내용 | 영향 FR | 요청 사항 | 앱 임시 처리 |
|----|------|---------|-----------|--------------|
| **G1** | **참여자가 `gameId`를 알 방법이 없음.** 방 조회 응답에 현재 게임 정보가 없다 | FR-013, FR-018, 전체 | `GET /rooms/{roomId}` 응답에 `currentGameId` 추가 (또는 `GET /api/v1/rooms/{roomId}/games/current`) | `currentGameId` 필드를 optional로 파싱한다. 서버 반영 전에는 참여자 흐름이 막힌다 |
| **G2** | **내 미션 목록 조회 API 없음.** 진행 화면에 내 미션을 보여줘야 하고, 수행 여부 제출(14번)에 `playerMissionId`가 필요하다 | FR-020, FR-031 | `GET /api/v1/games/{gameId}/my-missions` → `[{ playerMissionId, missionId, content }]` | 8번에서 선택한 미션 내용을 로컬에 저장해 진행 화면에 쓴다. `playerMissionId`는 서버 반영이 필요하다 |
| **G3** | 미션 개수: 원본 4번은 **"3 or 5개"**, 기능명세(spec FR-011)는 **1–3개** | FR-011 | 1–3개로 맞출 것 | 앱은 spec 기준인 1–3개를 쓴다 |
| **G4** | 게임 상태에 추리·채점 진행 정보가 없음: `myGuessSubmitted`, `partnerGuessSubmitted`, `myReviewCompleted`, `myJudgmentCompleted`, `partnerScoringCompleted`, `endRequestedByMemberId` | FR-029, FR-033, FR-022 | 5번 응답에 필드 추가 | 일부는 12–15번 응답과 13번의 `partnerSubmissionCompleted`로 추정한다. 제출 성공 여부는 로컬에 저장한다 |
| **G5** | 결산 응답에 공개 정보가 부족함: 미션 내용, 수행 여부, 추리, 정답 여부, 점수 내역 | FR-036 | 16번 응답에 `missions[]`, `guesses[]`, `players[].performedCount/correctGuessCount` 추가 | 점수·승패·벌칙만 표시한다 |
| **G6** | 종료 요청 철회 API 없음 | FR-025 | `DELETE /api/v1/games/{gameId}/end-request` | 철회 버튼을 숨긴다 |
| **G7** | 상대 접속 여부를 알 방법 없음 (원본: "정책 필요") | FR-023 | 5번 응답에 `partnerLastSeenAt` | '응답 대기'만 표시한다 |
| **G8** | **상대가 받은 소원권 사용 요청을 조회할 API 없음** (응답하려면 `couponId`가 필요) | FR-048a | `GET /api/v1/coupons/incoming` → `{ couponId, requestedAt } \| null` | 서버 반영 전에는 승인 화면 진입 불가 |
| **G9** | 오류 응답 본문 형식 미정 | 공통 | `{ code, message }` | HTTP 상태와 호출 맥락으로 매핑 |
| **G10** | 경로·담당 서버 표기 불일치 (`/pen` vs `/penalty`, `judgement` vs `/judgments`, 스탬프 조회 서버) | — | 표기 통일 | 원본 상세 섹션 기준 |
| **G11** | **기기 알림(푸시) API 없음**: FCM 토큰 등록, 발송 | FR-050a | `PUT /api/v1/members/me/push-token` + 서버 FCM 발송 | 푸시 작업은 마지막 단계에 두고 서버 준비를 기다린다. 앱 사용 중에는 폴링으로 반영 |
| **G12** | 24시간 무활동 자동 취소가 명세에 없음 | FR-052a~b | 게임 status에 `CANCELLED` 추가, 서버 배치 판정 | `CANCELLED` 값을 파싱할 수 있게만 준비 |
| **G13** | 추리 여러 개가 같은 미션을 맞혔을 때의 정답 인정 규칙 (원본 15번 협의 필요) | FR-035 | 미션 1개당 정답은 최대 1개로 인정 | 앱은 서버 점수를 그대로 표시 |
| **G14** | `SETTING`과 `SELECTING` 상태의 차이가 불명확함 | — | 정의 명시 | 둘 다 '미션·벌칙 선택 단계'로 처리 |
