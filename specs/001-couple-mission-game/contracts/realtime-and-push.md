# Realtime & Push Contract

**근거**: [`docs/API명세서.md`](../../../docs/API명세서.md) 3번 "안드로이드에서는 주기적 조회를 통해 상대 입장을 확인할 수 있다", 10번 "폴링 또는 실시간 통신 방식 협의 필요", research R3·R7

## 1. 앱 사용 중: 폴링

서버 명세에 실시간 채널이 없으므로, 앱은 **화면별 상태 조회 API를 주기적으로 호출**한다.

| 화면 상태 | 조회 API | 주기 | 멈추는 조건 |
|-----------|----------|------|-------------|
| 방 생성 완료 (상대 대기) | `GET /rooms/{roomId}` | 3초 | `status == ACTIVE` |
| 대기실 (참여자, 게임 대기) | `GET /rooms/{roomId}` → `currentGameId` (⚠️G1) | 3초 | 게임 발견 |
| 라운드 진행 중 모든 단계 | `GET /games/{gameId}` | 3초 | `FINISHED` |
| 채점 (상대 추리 대기) | `GET /games/{gameId}/received-guesses` | 3초 | `partnerSubmissionCompleted` |
| 가위바위보 (상대 대기) | `GET /games/{gameId}/rps/result` | 2초 | `DRAW` 또는 `DECIDED` |
| 스탬프/소원권 (승인 대기) | `GET /coupons` | 3초 | `status`가 `PENDING`이 아님 |
| 대기실 (받은 소원권 요청) | `GET /coupons/incoming` (⚠️G8) | 5초 | — |

규칙:
- 폴링은 `Lifecycle.State.STARTED` 이상에서만 돈다(`repeatOnLifecycle`). 앱이 백그라운드로 가면 멈추고, 포그라운드로 돌아오면 즉시 한 번 조회한 뒤 다시 시작한다.
- 같은 API를 화면 여러 곳에서 폴링하지 않도록 Repository가 `SharedFlow(WhileSubscribed(5s))`로 공유한다.
- 실패하면 2배씩 백오프한다(최대 30초). 성공하면 기본 주기로 돌아온다. 화면에는 "연결이 불안정해요"를 표시한다.
- 3초 주기라서 SC-004(3초 이내 반영)는 평균적으로는 만족하지만 최악의 경우 3초를 조금 넘을 수 있다. 서버가 SSE나 WebSocket을 제공하면 `GamePollingDataSource`를 교체한다.

## 2. 앱 미사용 시: 기기 알림 (⚠️G11, 서버 미지원)

spec의 Clarification(알림 3종)은 유지하되, **서버 API가 생길 때까지 구현을 보류**한다. 앱 측 제안 계약은 다음과 같다.

- 토큰 등록: `PUT /api/v1/members/me/push-token { fcmToken }` (Spring Boot)
- FCM data message: `{ "type": "PARTNER_JOINED" | "END_REQUESTED" | "WISH_APPROVAL_REQUESTED", "gameId"?, "couponId"? }`
- 알림 문구는 앱 문자열 리소스로 만든다. 미션·벌칙·추리 텍스트는 payload에 넣지 않는다(FR-050b).
- 알림을 누르면 딥링크 `hilkkeum://game/current` 또는 `hilkkeum://coupon/incoming`으로 이동한다. 진입 후 상태를 다시 조회해 화면을 결정한다.
- 알림 권한이 없어도 폴링으로 진행할 수 있다(FR-050c).
