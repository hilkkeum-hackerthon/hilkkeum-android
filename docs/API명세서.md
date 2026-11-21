# API명세서

> 출처: Notion 데이터베이스 ([원본](https://app.notion.com/p/41f01dc41a0e801dbbcbc1818cbecd24?v=41f01dc41a0e80418570000c7e9fb29c))

## 목록

| 기능 | 구분 | Method | URL | Parameter | 설명 | 우선순위 | 담당 | 호출 경로 |
|---|---|---|---|---|---|---|---|---|
| 방 생성 | 방·사용자 | POST | `/api/v1/rooms` | - | 초대 코드 6자리 발급, 생성자 room_members 등록, 개인 memberToken 발급 | P0 | Spring Boot | Android → Spring Boot |
| 초대 코드 입장 | 방·사용자 | POST | `/api/v1/rooms/join` | Body: inviteCode | 유효한 코드 검사, 2명 정원 검증, 참여자 등록 및 개인 토큰 발급 | P0 | Spring Boot | Android → Spring Boot |
| 입장 상태 조회 | 방·사용자 | GET | `/api/v1/rooms/{roomdId}` | Path: roomId; Header: memberToken | 방 상태 및 두 번째 참가자 입장 여부 조회(재접속·대기 화면) | P0 | Spring Boot | Android → Spring Boot |
| 게임 생성 및 기본 설정 | 게임 설정 | POST | `/api/v1/games` | Body: roomId, dateType, missionCount; themeId? | 방장이 온라인/오프라인 및 미션 개수를 설정하여 라운드 생성 | P0 | Node.js | Android → Node.js |
| 게임 상태·진행현황 조회 | 게임 설정 | GET | `/api/v1/games/{gameId}` | Path: gameId; Header: memberToken | 현재 라운드 status, 본인/상대 설정·제출 여부 반환; 비밀 정보는 제외 | P0 | Node.js | Android → Node.js |
| 미션 목록 조회 | 미션·벌칙 | GET | `/api/v1/missions` | Query: dateType=ONLINE\|OFFLINE; themeId? | 선택한 데이트 유형에 맞는 활성 미션 목록 제공 | P0 | Node.js | Android → Node.js |
| 벌칙 옵션 목록 조회 | 미션·벌칙 | GET | `/api/v1/penalties` | Header: memberToken | DB의 활성 벌칙 옵션 목록 조회 | P0 | Node.js | Android → Node.js |
| 내 비밀 미션 선택 | 미션·벌칙 | POST | `/api/v1/games/{gameId}/missions` | Path: gameId; Body: missionIds[] | 미션 개수와 유형 검증 후 내 미션 저장; 상대에게 비공개 | P0 | Node.js | Android → Node.js |
| 상대 벌칙 선택 | 미션·벌칙 | POST | `/api/v1/games/{gameId}/pen` | Path: gameId; Body: penaltyId | DB 옵션 중 상대에게 줄 벌칙 1개 저장; 결산 전 비공개 | P0 | Node.js | Android → Node.js |
| 데이트 종료 요청 | 데이트 진행 | POST | `/api/v1/games/{gameId}/end-request` | Path: gameId; Header: memberToken | 상대에게 종료 승인 대기 상태 생성; 중복 요청 방지 | P0 | Node.js | Android → Node.js |
| 데이트 종료 요청에 응답 | 데이트 진행 | POST | `/api/v1/games/{gameId}/end-response` | Path: gameId; Body: accepted (boolean) | 수락 시 추리/채점 단계 전환, 거절 시 PLAYING 복귀 | P0 | Node.js | Android → Node.js |
| 상대 미션 추리 일괄 제출 | 추리·채점 | POST | `/api/v1/games/{gameId}/guesses` | Path: gameId; Body: guesses[{guessNo, content}] | 미션 개수만큼 텍스트 추리 일괄 제출(각 1~30자); 제출 후 수정 불가 | P0 | Node.js | Android → Node.js |
| 상대가 보낸 추리 조회 | 추리·채점 | GET | `/api/v1/games/{gameId}/received-guesses` | Path: gameId; Header: memberToken | 상대방이 내 미션을 추측한 답 목록 반환(채점용) | P0 | Node.js | Android → Node.js |
| 내 미션 수행 O/X 제출 | 추리·채점 | POST | `/api/v1/games/{gameId}/mission-reviews` | Path: gameId; Body: reviews[{playerMissionId,isCompleted}] | 내 미션별 수행 여부 O/X 일괄 저장, 중복 제출 방지 | P0 | Node.js | Android → Node.js |
| 상대 추리 정답 O/X 판정 | 추리·채점 | POST | `api/v1/games/{gameId}/judgement` | Path: gameId; Body: judgments[{guessId,isCorrect}] | 미션 소유자가 상대 추리의 정답 여부를 판정, 중복 판정 방지 | P0 | Node.js | Android → Node.js |
| 결산 결과 조회 | 결산 | GET | `/api/v1/games/{gameId}/result` | Path: gameId; Header: memberToken | 양쪽 채점 완료 후 점수·승패·미션·벌칙 공개; 동점이면 TIEBREAK | P0 | Node.js | Android → Node.js |
| 가위바위보 선택 | 동점처리 | POST | `/api/v1/games/{gameId}/rps/moves` | Path: gameId; Body: choice | 동점 시 가위바위보 선택 제출 | P0 | Node.js | Android → Node.js |
| 가위바위보 결과 조회 | 동점처리 | GET | `/api/v1/games/{gameId}/rps/result` | Path: gameId; Query: roundNo? | 양측 제출 후 결과·재대결 여부 조회 | P0 | Node.js | Android → Node.js |
| 스탬프 조회 | 스탬프·소원권 | GET | `/api/v1/stamps` | Header: memberToken | 보유 스탬프 및 교환 가능 여부 조회 | P0 | Node.js | Android → Node.js |
| 스탬프 적립 연동 | 내부 서버연동 | POST | `/internal/v1/stamps/award` | Body: gameId, winnerMemberId | 결산 승자에게 스탬프 1개 적립	 | P0 | Spring Boot | Android → Spring Boot |
| 소원권 교환 | 스탬프·소원권 | POST | `/api/v1/coupons/exchange` | Header: memberToken | 스탬프 3개 차감 후 소원권 발급 | P0 | Spring Boot | Android → Spring Boot |
| 소원권 조회 | 스탬프·소원권 | GET | `/api/v1/coupons` | Header: memberToken | 소원권 상태 및 만료일 조회 | P0 | Spring Boot | Android → Spring Boot |
| 소원권 사용 요청 | 스탬프·소원권 | POST | `/api/v1/coupons/{couponId}/use-request` | Path: couponId | 상대에게 사용 확인 요청 | P0 | Spring Boot | Android → Spring Boot |
| 소원권 사용 응답 | 스탬프·소원권 | POST | `/api/v1/coupons/{couponId}/respond` | Path: couponId; Body: approved | 상대의 사용 확인 처리 | P0 | Spring Boot | Android → Spring Boot |

## 상세

### 1. 방 생성

- **구분**: 방·사용자
- **Method**: POST
- **URL**: `/api/v1/rooms`
- **Parameter**: -
- **설명**: 초대 코드 6자리 발급, 생성자 room_members 등록, 개인 memberToken 발급
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 방 생성

#### 엔드포인트

**POST** `/api/v1/rooms`

#### 기능 요구사항

기능명세서 4.1 초기 설정 / 4.2 방 생성
새로운 2인용 방을 생성하고 6자리 초대 코드와 방장 식별 토큰을 발급한다.

#### Request

Body

```
없음
```

Response
201 Created

```json
{
  "roomId": 1,
  "inviteCode": "ABC123",
  "memberId": 1,
  "memberToken": "발급된_개인_토큰",
  "status": "PENDING"
}
```

#### 처리 규칙

- 초대 코드는 중복되지 않도록 생성한다.
- 방 상태는 `PENDING`으로 저장한다.
- 방 생성자는 `CREATOR` 역할을 갖는다.
- 잘못된 입력은 400, 코드 발급 실패는 적절한 서버 오류를 반환한다.
- 재접속할 수 있도록 멤버 토큰을 발급한다.

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot / **우선순위:** P0

---

### 2. 초대 코드 입장

- **구분**: 방·사용자
- **Method**: POST
- **URL**: `/api/v1/rooms/join`
- **Parameter**: Body: inviteCode
- **설명**: 유효한 코드 검사, 2명 정원 검증, 참여자 등록 및 개인 토큰 발급
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 초대 코드 입장

#### 엔드포인트

**POST** `/api/v1/rooms/join`

#### 기능 요구사항

기능명세서 4.1 초기 설정
방장이 공유한 6자리 초대 코드를 입력하여 동일한 방에 참여한다.

#### Request

Body

```json
{
  "inviteCode": "ABC123",
}
```

#### Response

200 OK

```json
{
  "roomId": 1,
  "memberId": 2,
  "memberToken": "참여자_개인_토큰",
  "role": "JOINER",
  "roomStatus": "ACTIVE"
}
```

#### 처리 규칙

- 존재하지 않는 초대 코드는 404를 반환한다.
- 이미 2명이 참여한 방은 409를 반환한다.
- 입장 성공 시 `JOINER`로 저장한다.
- 두 명이 모두 참여하면 방을 `ACTIVE`로 전환한다.
- 참가자 추가는 최대 2명까지만 허용한다.
- 동시 입장 시에도 정원을 초과하지 않도록 처리한다.
- 초대 코드 만료 정책은 기능명세서와 ERD 간 최종 합의가 필요하다.

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot / **우선순위:** P0

---

### 3. 입장 상태 조회

- **구분**: 방·사용자
- **Method**: GET
- **URL**: `/api/v1/rooms/{roomdId}`
- **Parameter**: Path: roomId; Header: memberToken
- **설명**: 방 상태 및 두 번째 참가자 입장 여부 조회(재접속·대기 화면)
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 방 정보 조회

#### 엔드포인트

**GET** `/api/v1/rooms/{roomId}`

#### 기능 요구사항

기능명세서 4.1 초기 설정 / 화면 구성 2, 4
현재 방의 상태와 상대방 입장 여부를 조회한다.

#### Request

Path: `roomId`
Header: `Authorization: Bearer {memberToken}`

#### Response

200 OK

```json
{
  "roomId": 1,
  "status": "ACTIVE",
  "memberCount": 2,
  "capacity": 2,
  "members": [
    {
      "memberId": 1,
      "role": "CREATOR"
    },
    {
      "memberId": 2,
      "role": "JOINER"
    }
  ]
}
```

#### 처리 규칙

- 해당 방의 참가자만 조회할 수 있다.
- 방이 존재하지 않으면 404를 반환한다.
- 참가자가 한 명이면 상대 입장 대기 상태를 표시한다.
- 상대방의 식별 토큰은 응답에 포함하지 않는다.
- 안드로이드에서는 주기적 조회를 통해 상대 입장을 확인할 수 있다.

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot / **우선순위:** P0

---

### 4. 게임 생성 및 기본 설정

- **구분**: 게임 설정
- **Method**: POST
- **URL**: `/api/v1/games`
- **Parameter**: Body: roomId, dateType, missionCount; themeId?
- **설명**: 방장이 온라인/오프라인 및 미션 개수를 설정하여 라운드 생성
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 게임 생성

#### 엔드포인트

**POST** `/api/v1/games`

#### 기능 요구사항

기능명세서 4.2 방 생성 / 4.3 미션·벌칙 설정
방장이 데이트 유형과 미션 개수를 선택하여 새로운 게임을 생성한다.

#### Request

Body

```json
{
  "roomId": 1,
  "dateType": "OFFLINE",
  "missionCount": 3
}
```

#### Response

201 Created

```json
{
  "gameId": 1,
  "roomId": 1,
  "dateType": "OFFLINE",
  "missionCount": 3,
  "status": "SETTING"
}
```

#### 처리 규칙

- 방에 두 명이 입장한 경우에만 설정을 진행한다.
- 데이트 유형은 `ONLINE` 또는 `OFFLINE`이다.
- 미션 개수는 3 or 5개로 제한한다.
- 동일 방에서 진행 중인 게임의 중복 생성을 방지한다.
- 설정 완료 후 각자 미션 및 벌칙을 선택한다.
- 양측 선택 완료 전에는 진행 상태로 전환하지 않는다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 5. 게임 상태·진행현황 조회

- **구분**: 게임 설정
- **Method**: GET
- **URL**: `/api/v1/games/{gameId}`
- **Parameter**: Path: gameId; Header: memberToken
- **설명**: 현재 라운드 status, 본인/상대 설정·제출 여부 반환; 비밀 정보는 제외
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 게임 상태 조회

#### 엔드포인트

**GET** `/api/v1/games/{gameId}`

#### 기능 요구사항

기능명세서 3. 용어와 상태 / 4.3 미션·벌칙 설정 / 4.5 진행 및 종료 확인
현재 게임 단계와 두 참가자의 준비 및 제출 상태를 조회한다.

#### Request

Path: `gameId`

#### Response

200 OK

```json
{
  "gameId": 1,
  "status": "SELECTING",
  "dateType": "OFFLINE",
  "missionCount": 3,
  "mySelectionCompleted": true,
  "partnerSelectionCompleted": false,
  "endRequestPending": false
}
```

#### 처리 규칙

- 해당 게임의 참가자만 조회할 수 있다.
- 미션과 벌칙의 실제 내용은 상대방에게 노출하지 않는다.
- 한쪽만 선택 완료한 경우 상대 대기 상태를 반환한다.
- 종료 요청이 있다면 요청 여부를 반환한다.
- 게임 상태에 따라 안드로이드가 적절한 화면을 표시한다.
- 상대 미접속 여부는 접속 상태 판별 정책이 필요하다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0
**협의 필요:** `endRequestPending`을 저장할 종료 요청 정보가 현재 ERD에 부족하다.

---

### 6. 미션 목록 조회

- **구분**: 미션·벌칙
- **Method**: GET
- **URL**: `/api/v1/missions`
- **Parameter**: Query: dateType=ONLINE\|OFFLINE; themeId?
- **설명**: 선택한 데이트 유형에 맞는 활성 미션 목록 제공
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 미션 목록 조회

#### 엔드포인트

**GET** `/api/v1/missions`

#### 기능 요구사항

기능명세서 4.2 방 생성 / 4.3 미션·벌칙 설정
사용자가 선택한 온라인·오프라인 유형에 따라 시스템에서 등록한 미션 목록을 제공한다.

#### Request

Query: `dateType`
예시: `/api/v1/missions?dateType=OFFLINE`

#### Response

200 OK

```json
{
  "missions": [
    {
      "id": 1,
      "content": "평소와 다른 애칭으로 불러보기"
    },
    {
      "id": 2,
      "content": "먼저 사진 찍자고 제안하기"
    }
  ]
}
```

#### 처리 규칙

- `missions` 테이블에서 활성화된 미션만 조회한다.
- `date_type`이 요청한 유형과 일치해야 한다.
- 사용자는 설정된 개수만큼 목록에서 직접 선택한다.
- 선택 가능한 미션이 부족하면 안내 메시지를 반환한다.
- 미션 유형 선택값이 유효하지 않으면 400을 반환한다.
- 미션 테마 선택은 Should 기능이므로 MVP에서는 필수로 요구하지 않는다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 7. 벌칙 옵션 목록 조회

- **구분**: 미션·벌칙
- **Method**: GET
- **URL**: `/api/v1/penalties`
- **Parameter**: Header: memberToken
- **설명**: DB의 활성 벌칙 옵션 목록 조회
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 벌칙 목록 조회

#### 엔드포인트

**GET** `/api/v1/penalties`

#### 기능 요구사항

기능명세서 4.3 미션·벌칙 설정
시스템에서 준비한 벌칙 목록을 제공하여 상대방에게 적용할 벌칙 하나를 선택하도록 한다.

#### Request

Body: 없음

#### Response

200 OK

```json
{
  "penalties": [
    {
      "id": 1,
      "content": "상대방의 장점 3가지 말하기"
    },
    {
      "id": 2,
      "content": "10초 동안 눈을 마주치며 칭찬하기"
    }
  ]
}
```

#### 처리 규칙

- `penalties` 테이블에서 활성화된 항목만 조회한다.
- 선택 가능한 벌칙이 없으면 빈 목록과 안내 메시지를 반환한다.
- 각 사용자는 상대방이 수행할 벌칙 1개를 선택한다.
- 벌칙 목록은 공개할 수 있지만, 상대가 실제 선택한 벌칙은 결산 전까지 숨긴다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 8. 내 비밀 미션 선택

- **구분**: 미션·벌칙
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/missions`
- **Parameter**: Path: gameId; Body: missionIds[]
- **설명**: 미션 개수와 유형 검증 후 내 미션 저장; 상대에게 비공개
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 내 미션 선택

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/missions`

#### 기능 요구사항

기능명세서 4.3 미션·벌칙 설정
각 사용자가 시스템 미션 목록에서 정해진 개수만큼 자신의 미션을 선택한다.

#### Request

Path: `gameId`
Body

```json
{
  "missionIds": [1, 3, 5]
}
```

#### Response

200 OK

```json
{
  "gameId": 1,
  "selectedCount": 3,
  "selectionCompleted": true
}
```

#### 처리 규칙

- 선택 미션 개수는 게임 설정값과 일치해야 한다.
- 동일 미션을 중복 선택할 수 없다.
- 선택한 미션이 해당 데이트 유형에 적합한지 검사한다.
- 선택 결과를 `player_missions`에 저장한다.
- 자신의 미션은 상대방에게 공개하지 않는다.
- 중복 제출 및 반복 요청으로 인한 데이터 중복을 방지한다.
- 한쪽만 완료했으면 상대방 대기 상태를 유지한다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 9. 상대 벌칙 선택

- **구분**: 미션·벌칙
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/pen`
- **Parameter**: Path: gameId; Body: penaltyId
- **설명**: DB 옵션 중 상대에게 줄 벌칙 1개 저장; 결산 전 비공개
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 상대방 벌칙 선택

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/penalty`

#### 기능 요구사항

기능명세서 4.3 미션·벌칙 설정
각 사용자가 상대방이 패배했을 때 수행할 벌칙 하나를 선택한다.

#### Request

Path: `gameId`
Body

```json
{
  "penaltyId": 2
}
```

#### Response

200 OK

```json
{
  "gameId": 1,
  "penaltySelectionCompleted": true
}
```

#### 처리 규칙

- 시스템 벌칙 목록에 존재하는 ID만 허용한다.
- 사용자당 벌칙은 1개만 선택한다.
- 선택한 벌칙을 해당 사용자의 게임 기록에 저장한다.
- 상대방이 선택한 벌칙은 결산 전까지 공개하지 않는다.
- 한쪽만 완료하면 상대방 선택을 기다린다.
- 두 사용자 모두 미션과 벌칙 선택을 끝내면 진행 단계로 전환한다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0
**ERD 확인:** `game_players`에서 자유 입력 벌칙 대신 `penalty_id` 형태로 저장하도록 팀원과 합의한 구조를 사용한다.

---

### 10. 데이트 종료 요청

- **구분**: 데이트 진행
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/end-request`
- **Parameter**: Path: gameId; Header: memberToken
- **설명**: 상대에게 종료 승인 대기 상태 생성; 중복 요청 방지
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 데이트 종료 요청

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/end-request`

#### 기능 요구사항

기능명세서 4.5 진행 및 종료 확인
한 사용자가 데이트 종료 버튼을 누르면 상대방에게 종료 동의 요청을 전달한다.

#### Request

Path: `gameId`
Body: 없음

#### Response

200 OK

```json
{
  "gameId": 1,
  "endRequestPending": true,
  "requestedByMemberId": 1
}
```

#### 처리 규칙

- 게임이 `PLAYING` 상태일 때만 요청할 수 있다.
- 이미 종료 요청이 존재한다면 중복 생성하지 않는다.
- 요청자와 상대방을 구분한다.
- 상대방에게 종료 확인 상태를 제공한다.
- 상대방 동의 전에는 게임을 종료하지 않는다.
- 상대방 응답 대기 상태를 유지한다.

#### 담당 호출 흐름

Android → Node.js → DB → Android(상대방 상태 조회)
**담당:** Node.js / **우선순위:** P0
**ERD 확인:** 종료 요청자, 요청 상태를 저장할 구조 보완 필요.
**알림 방식:** 폴링 또는 실시간 통신 방식 협의 필요.

---

### 11. 데이트 종료 요청에 응답

- **구분**: 데이트 진행
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/end-response`
- **Parameter**: Path: gameId; Body: accepted (boolean)
- **설명**: 수락 시 추리/채점 단계 전환, 거절 시 PLAYING 복귀
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 데이트 종료 요청 응답

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/end-response`

#### 기능 요구사항

기능명세서 4.5 진행 및 종료 확인
상대방이 데이트 종료 요청에 동의하거나 거절한다.

#### Request

Path: `gameId`
Body

```json
{
  "accepted": true
}
```

#### Response

200 OK

```json
{
  "gameId": 1,
  "accepted": true,
  "status": "SCORING"
}
```

#### 처리 규칙

- 종료를 요청한 본인은 자신의 요청에 응답할 수 없다.
- `accepted = true`: 데이트 종료 및 추리·채점 단계로 전환한다.
- `accepted = false`: 종료 요청을 취소하고 `PLAYING` 상태를 유지한다.
- 대기 중인 종료 요청이 없으면 409를 반환한다.
- 중복 승인 또는 거절 요청을 방지한다.
- 상대방이 응답하지 않으면 기존 진행 상태를 유지하면서 대기 정보를 제공한다.

#### 담당 호출 흐름

Android → Node.js → DB → Android(상태 반영)
**담당:** Node.js / **우선순위:** P0

---

### 12. 상대 미션 추리 일괄 제출

- **구분**: 추리·채점
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/guesses`
- **Parameter**: Path: gameId; Body: guesses[{guessNo, content}]
- **설명**: 미션 개수만큼 텍스트 추리 일괄 제출(각 1~30자); 제출 후 수정 불가
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 상대 미션 추리 제출

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/guesses`

#### 기능 요구사항

기능명세서 4.6 추리 입력
데이트 종료 후 상대방이 수행했을 것으로 생각하는 미션을 텍스트로 입력하여 일괄 제출한다.

#### Request

Path: `gameId`
Body

```json
{
  "guesses": [
    {
      "guessNo": 1,
      "content": "평소보다 나를 자주 칭찬하기"
    },
    {
      "guessNo": 2,
      "content": "먼저 사진 찍자고 하기"
    }
  ]
}
```

#### Response

201 Created

```json
{
  "gameId": 1,
  "submittedCount": 2,
  "submissionCompleted": true
}
```

#### 처리 규칙

- 게임이 추리 단계일 때만 제출할 수 있다.
- 추리 개수는 설정된 미션 개수와 일치해야 한다.
- 각 추리 내용은 1~30자로 제한한다.
- 빈 문자열은 허용하지 않는다.
- 여러 미션의 추리를 한 번에 제출한다.
- 제출 완료 후 수정할 수 없다.
- 중복 요청으로 동일 추리가 추가 저장되지 않도록 한다.
- 실제 정답 여부는 상대방이 O/X로 판정한다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0
**ERD 확인:** `guesses`에 추리 순서를 구분할 `guess_no` 추가 필요.

---

### 13. 상대가 보낸 추리 조회

- **구분**: 추리·채점
- **Method**: GET
- **URL**: `/api/v1/games/{gameId}/received-guesses`
- **Parameter**: Path: gameId; Header: memberToken
- **설명**: 상대방이 내 미션을 추측한 답 목록 반환(채점용)
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 받은 추리 조회

#### 엔드포인트

**GET** `/api/v1/games/{gameId}/received-guesses`

#### 기능 요구사항

기능명세서 4.6 채점
상대방이 제출한 추리 목록을 조회하여 미션 소유자가 O/X로 판정할 수 있도록 한다.

#### Request

Path: `gameId`
Body: 없음

#### Response

200 OK

```json
{
  "gameId": 1,
  "guesses": [
    {
      "guessId": 21,
      "guessNo": 1,
      "content": "평소보다 나를 자주 칭찬하기"
    },
    {
      "guessId": 22,
      "guessNo": 2,
      "content": "먼저 사진 찍자고 하기"
    }
  ],
  "partnerSubmissionCompleted": true
}
```

#### 처리 규칙

- 해당 게임의 참가자만 조회할 수 있다.
- 자신의 미션에 대한 상대방의 추리만 제공한다.
- 상대방이 아직 제출하지 않았다면 대기 상태를 반환한다.
- 추리 내용은 원본 그대로 반환한다.
- O/X 판정 완료 전에는 최종 결산 결과를 공개하지 않는다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 14. 내 미션 수행 O/X 제출

- **구분**: 추리·채점
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/mission-reviews`
- **Parameter**: Path: gameId; Body: reviews[{playerMissionId,isCompleted}]
- **설명**: 내 미션별 수행 여부 O/X 일괄 저장, 중복 제출 방지
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 내 미션 수행 여부 제출

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/mission-reviews`

#### 기능 요구사항

기능명세서 4.6 채점
사용자가 자신의 미션을 실제 수행했는지 O/X로 체크하여 제출한다.

#### Request

Path: `gameId`
Body

```json
{
  "reviews": [
    {
      "playerMissionId": 11,
      "isCompleted": true
    },
    {
      "playerMissionId": 12,
      "isCompleted": false
    }
  ]
}
```

#### Response

200 OK

```json
{
  "gameId": 1,
  "submittedCount": 2,
  "submissionCompleted": true
}
```

#### 처리 규칙

- 본인에게 배정된 미션만 평가할 수 있다.
- 미션별 O/X 값을 모두 입력해야 한다.
- 실제 수행 여부는 자기 보고 방식으로 판정한다.
- 제출 결과를 `player_missions.is_completed`에 저장한다.
- 반복 요청으로 점수가 중복 반영되지 않도록 한다.
- 양측 채점 완료 전까지 결산을 공개하지 않는다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0

---

### 15. 상대 추리 정답 O/X 판정

- **구분**: 추리·채점
- **Method**: POST
- **URL**: `api/v1/games/{gameId}/judgement`
- **Parameter**: Path: gameId; Body: judgments[{guessId,isCorrect}]
- **설명**: 미션 소유자가 상대 추리의 정답 여부를 판정, 중복 판정 방지
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 상대 추리 O/X 판정

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/judgments`

#### 기능 요구사항

기능명세서 4.6 채점
미션 소유자가 상대방이 제출한 추리의 정답 여부를 O/X로 판정한다.

#### Request

Path: `gameId`
Body

```json
{
  "judgments": [
    {
      "guessId": 21,
      "isCorrect": true
    },
    {
      "guessId": 22,
      "isCorrect": false
    }
  ]
}
```

#### Response

200 OK

```json
{
  "gameId": 1,
  "judgedCount": 2,
  "judgmentCompleted": true
}
```

#### 처리 규칙

- 해당 미션의 소유자만 추리를 판정할 수 있다.
- 본인이 작성한 추리는 스스로 판정할 수 없다.
- 모든 추리에 O/X 판정이 입력되어야 한다.
- 판정 결과를 `guesses.is_correct`에 저장한다.
- 동일 판정의 반복 저장과 중복 점수 반영을 방지한다.
- 양측의 수행 평가 및 추리 판정이 완료되면 결산 준비 상태로 전환한다.

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js / **우선순위:** P0
**협의 필요:** 여러 추리가 동일한 미션을 맞혔을 때의 중복 정답 인정 규칙.

---

### 16. 결산 결과 조회

- **구분**: 결산
- **Method**: GET
- **URL**: `/api/v1/games/{gameId}/result`
- **Parameter**: Path: gameId; Header: memberToken
- **설명**: 양쪽 채점 완료 후 점수·승패·미션·벌칙 공개; 동점이면 TIEBREAK
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 최종 결산 결과 조회

#### 엔드포인트

**GET** `/api/v1/games/{gameId}/result`

#### 기능 요구사항

기능명세서 4.7 결산 / 4.7.1 동점 처리
양측의 미션 수행 여부와 추리 정답 판정 결과를 바탕으로 최종 점수 및 승패를 조회한다.

#### Request

Path: `gameId`
Body: 없음

#### Response

200 OK — 결산 완료 예시

```json
{
  "gameId": 1,
  "status": "FINISHED",
  "decidedBy": "SCORE",
  "winnerMemberId": 1,
  "players": [
    {
      "memberId": 1,
      "score": 20,
      "result": "WIN"
    },
    {
      "memberId": 2,
      "score": 10,
      "result": "LOSE"
    }
  ],
  "penalty": {
    "id": 3,
    "content": "상대방의 장점 3가지 말하기"
  }
}
```

※ 위 점수는 응답 구조를 보여주기 위한 예시이며 실제 점수 공식은 미확정이다.

#### 처리 규칙

- 양측 미션 수행 O/X 및 추리 판정이 모두 완료되어야 결산 가능하다.
- 서버에서 합의된 점수 공식으로 점수를 계산한다.
- 수행 점수와 추리 점수를 각 사용자에게 정확하게 귀속시킨다.
- 동점이면 `TIEBREAK`로 전환하고 가위바위보 승부가 끝난 뒤 최종 결과를 확정한다.
- 승자와 패자를 결정한 후 승자가 선택했던 벌칙을 공개한다.
- 결산 시 각자의 미션, 수행 여부, 추리 및 정답 여부를 확인할 수 있도록 한다.
- 결산 완료 후 승자의 스탬프 +1 적립을 Spring Boot에서 한 번만 처리하도록 연동한다.
- 재조회 시 점수나 스탬프가 중복 반영되지 않아야 한다.

#### 담당 호출 흐름

Android → Node.js → DB
결산 확정 후: Node.js → Spring Boot (스탬프 적립 연동)
**담당:** Node.js / **우선순위:** P0

#### 구현 전 결정 사항

- 점수 공식 및 여러 미션 합산 방법
- 추리 정답의 중복 인정 여부
- 가위바위보 결과 확정 및 연결 방식
- 스탬프 적립 API 호출 시점과 중복 방지 방법
- 서버 간 인증 및 결산 결과 검증 방식

---

### 17. 가위바위보 선택

- **구분**: 동점처리
- **Method**: POST
- **URL**: `/api/v1/games/{gameId}/rps/moves`
- **Parameter**: Path: gameId; Body: choice
- **설명**: 동점 시 가위바위보 선택 제출
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 가위바위보 선택

#### 엔드포인트

**POST** `/api/v1/games/{gameId}/rps/moves`

#### 기능 요구사항

기능명세서 4.7.1 동점 처리 / 화면 구성 13
최종 점수가 동일한 경우 각 사용자가 가위·바위·보 중 하나를 선택하여 제출한다. 양측 선택 완료 후 승패를 결정하며, 비긴 경우 다음 라운드에서 재대결한다.

#### Request

Path: `gameId`
Header: `Authorization: Bearer {memberToken}`
Body

```json
{
  "choice": "ROCK"
}
```

선택값:
- `ROCK`: 바위
- `PAPER`: 보
- `SCISSORS`: 가위

#### Response

200 OK — 선택 완료, 상대방 대기

```json
{
  "gameId": 1,
  "roundNo": 1,
  "myMoveSubmitted": true,
  "partnerMoveSubmitted": false,
  "status": "WAITING"
}
```

#### 처리 규칙

- 게임의 두 참가자만 요청할 수 있다.
- 최종 점수가 동점이고 게임 상태가 `TIEBREAK`인 경우에만 제출할 수 있다.
- 선택값은 `ROCK`, `PAPER`, `SCISSORS`만 허용한다.
- 서버가 현재 라운드 번호를 결정한다.
- 사용자당 한 라운드에 한 번만 제출할 수 있다.
- 양측 제출 전에는 상대방의 선택을 공개하지 않는다.
- 같은 요청을 재전송해도 선택이 중복 저장되지 않도록 한다.
- 선택 기록은 `rps_moves` 테이블에 저장한다.

#### 예외 처리

- 400: 유효하지 않은 선택값
- 401: 인증 실패
- 403: 게임에 참여하지 않은 사용자
- 404: 게임을 찾을 수 없음
- 409: 동점 처리 상태가 아니거나 해당 라운드에 이미 제출함

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js
**제안 우선순위:** P0

---

### 18. 가위바위보 결과 조회

- **구분**: 동점처리
- **Method**: GET
- **URL**: `/api/v1/games/{gameId}/rps/result`
- **Parameter**: Path: gameId; Query: roundNo?
- **설명**: 양측 제출 후 결과·재대결 여부 조회
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 가위바위보 결과 조회

#### 엔드포인트

**GET** `/api/v1/games/{gameId}/rps/result`

#### 기능 요구사항

기능명세서 4.7.1 동점 처리 / 화면 구성 13
두 사용자의 가위바위보 제출 상태와 승부 결과를 조회한다. 비긴 경우 재대결을 안내하고, 승자가 결정되면 최종 결산 단계로 전환한다.

#### Request

Path: `gameId`
Query: `roundNo` (선택)
Header: `Authorization: Bearer {memberToken}`

#### Response

200 OK — 상대방 선택 대기

```json
{
  "gameId": 1,
  "roundNo": 1,
  "status": "WAITING",
  "myMoveSubmitted": true,
  "partnerMoveSubmitted": false
}
```

200 OK — 비긴 경우

```json
{
  "gameId": 1,
  "roundNo": 1,
  "status": "DRAW",
  "myChoice": "ROCK",
  "partnerChoice": "ROCK",
  "nextRoundNo": 2
}
```

200 OK — 승패 결정

```json
{
  "gameId": 1,
  "roundNo": 2,
  "status": "DECIDED",
  "myChoice": "PAPER",
  "partnerChoice": "ROCK",
  "winnerMemberId": 1,
  "decidedBy": "RPS"
}
```

#### 처리 규칙

- 두 사용자 모두 제출하기 전에는 선택 내용을 공개하지 않는다.
- 가위바위보 규칙에 따라 서버에서 승패를 판정한다.
- 비긴 경우 `roundNo`를 증가시켜 다음 라운드를 진행한다.
- 승자가 결정될 때까지 재대결할 수 있다.
- 승자 확정 시 `games`의 승자 정보 및 `decided_by = RPS`를 반영한다.
- 양측은 동일한 최종 승자를 조회해야 한다.
- 승자가 결정된 뒤 결산 결과를 조회할 수 있도록 한다.
- 결과 조회만으로 스탬프가 중복 적립되어서는 안 된다.

#### 예외 처리

- 401: 인증 실패
- 403: 게임 접근 권한 없음
- 404: 게임 또는 라운드 없음
- 409: 가위바위보 진행이 불가능한 게임 상태

#### 담당 호출 흐름

Android → Node.js → DB
**담당:** Node.js
**제안 우선순위:** P0

#### ERD 관련 사항

`rps_moves`에는 `game_id`, `round_no`, `game_player_id`, `choice`가 정의되어 있다.
가위바위보 판정은 두 사용자가 같은 라운드에 제출한 선택을 비교해 진행한다.

---

### 19. 스탬프 조회

- **구분**: 스탬프·소원권
- **Method**: GET
- **URL**: `/api/v1/stamps`
- **Parameter**: Header: memberToken
- **설명**: 보유 스탬프 및 교환 가능 여부 조회
- **우선순위**: P0
- **담당**: Node.js
- **호출 경로**: Android → Node.js


### 스탬프 조회

#### 엔드포인트

**GET** `/api/v1/stamps`

#### 기능 요구사항

기능명세서 4.8 스탬프·소원권 / 화면 구성 15
사용자가 현재 보유한 스탬프 개수와 소원권 교환 가능 여부를 조회한다.

#### Request

Header: `Authorization: Bearer {memberToken}`
Body: 없음

#### Response

200 OK — 스탬프 2개 보유

```json
{
  "memberId": 1,
  "heartCount": 2,
  "maxHearts": 3,
  "canExchange": false,
  "hasActiveCoupon": false
}
```

200 OK — 교환 가능한 경우

```json
{
  "memberId": 1,
  "heartCount": 3,
  "maxHearts": 3,
  "canExchange": true,
  "hasActiveCoupon": false
}
```

#### 처리 규칙

- 승리 시 스탬프 1개를 적립한다.
- 스탬프 보유량은 최대 3개다.
- 스탬프 3개를 보유하고 소원권이 없으면 교환할 수 있다.
- 소원권을 보유 중이라면 스탬프 3개가 있어도 교환할 수 없다.
- 스탬프가 3개인 상태에서 추가 승리해도 초과 적립하지 않는다.
- 스탬프는 해당 사용자의 정보만 조회할 수 있다.
- 잔여 스탬프가 부족하면 교환 가능 여부를 `false`로 반환한다.

#### 예외 처리

- 401: 인증 실패
- 404: 회원 정보를 찾을 수 없음
- 500: 스탬프 조회 실패

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot
**제안 우선순위:** P0

#### ERD 관련 사항

`room_members.heart_count`를 사용한다.
스탬프 적립은 최종 승자 확정 이후에만 진행한다.
**주의:** 팀원 ERD의 자동 소원권 발급 설명과 달리, 최종 기능명세서는 사용자가 교환 버튼을 눌러야 소원권을 발급하는 방식이다.

---

### 20. 스탬프 적립 연동

- **구분**: 내부 서버연동
- **Method**: POST
- **URL**: `/internal/v1/stamps/award`
- **Parameter**: Body: gameId, winnerMemberId
- **설명**: 결산 승자에게 스탬프 1개 적립	
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 스탬프 적립 연동

#### 엔드포인트

**POST** `/internal/v1/stamps/award`

#### 기능 요구사항

기능명세서 4.7 결산 / 4.8 스탬프·소원권
게임 승자가 최종 확정되면 해당 사용자에게 스탬프 1개를 적립한다.
Node.js에서 게임 결과를 확정한 뒤 Spring Boot에 적립 요청을 전달하는 방식의 제안이다.

#### Request

서버 간 인증 헤더: 별도 협의
Body

```json
{
  "gameId": 1,
  "winnerMemberId": 2
}
```

#### Response

200 OK — 정상 적립

```json
{
  "gameId": 1,
  "memberId": 2,
  "awarded": true,
  "heartCount": 2
}
```

200 OK — 이미 적립된 게임

```json
{
  "gameId": 1,
  "memberId": 2,
  "awarded": false,
  "heartCount": 2
}
```

#### 처리 규칙

- 게임의 최종 승자가 확정된 경우에만 요청할 수 있다.
- Spring Boot는 DB에 확정된 승자와 요청 정보가 일치하는지 검증한다.
- 한 게임에서 승자에게 스탬프를 한 번만 지급한다.
- 중복 요청이 들어와도 스탬프를 중복 적립하지 않는다.
- 현재 보유량이 3개면 추가 적립하지 않는다.
- 스탬프 적립과 중복 처리 기록은 하나의 트랜잭션으로 관리한다.
- 소원권은 자동 발급하지 않는다.

#### 예외 처리

- 400: 요청 정보 누락
- 401/403: 서버 간 인증 또는 권한 오류
- 404: 게임 또는 승자 정보를 찾을 수 없음
- 409: 결산이 확정되지 않았거나 승자 정보가 일치하지 않음
- 500: 적립 처리 실패

#### 담당 호출 흐름

Node.js → Spring Boot → DB
**담당:** Spring Boot (API 구현), Node.js (호출)
**제안 우선순위:** P0

#### 구현 전 확인

- 내부 API의 인증 방식
- `winnerMemberId`와 `games.winner_player_id`의 연결 기준
- 중복 적립 방지 기록 저장 방식
현재 팀원 ERD에는 게임별 적립 완료를 확실히 기록할 구조가 부족하다. `stamp_awards` 테이블 또는 `games.stamp_awarded_at` 등으로 보완하는 것을 제안한다.

---

### 21. 소원권 교환

- **구분**: 스탬프·소원권
- **Method**: POST
- **URL**: `/api/v1/coupons/exchange`
- **Parameter**: Header: memberToken
- **설명**: 스탬프 3개 차감 후 소원권 발급
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 소원권 교환

#### 엔드포인트

**POST** `/api/v1/coupons/exchange`

#### 기능 요구사항

기능명세서 4.8 스탬프·소원권 / 화면 구성 15
스탬프 3개를 보유한 사용자가 교환 버튼을 클릭하면 스탬프를 차감하고 소원권 1장을 발급한다.

#### Request

Header: `Authorization: Bearer {memberToken}`
Body: 없음

#### Response

201 Created

```json
{
  "couponId": 1,
  "memberId": 1,
  "heartCount": 0,
  "coupon": {
    "status": "AVAILABLE",
    "issuedAt": "2026-11-21T20:00:00+09:00",
    "expiresAt": "2026-12-21T20:00:00+09:00"
  }
}
```

#### 처리 규칙

- 보유 스탬프가 정확히 3개인 경우 교환할 수 있다.
- 사용 가능한 소원권은 최대 1장만 보유할 수 있다.
- 소원권을 이미 보유한 경우 교환할 수 없다.
- 교환 성공 시 스탬프 3개를 차감한다.
- 소원권 1장을 발급한다.
- 발급일로부터 1개월 동안 유효하다.
- 스탬프 차감과 소원권 발급은 하나의 DB 트랜잭션으로 처리한다.
- 중복 요청으로 소원권이 여러 장 발급되지 않도록 한다.

#### 예외 처리

- 400: 교환 조건 미충족
- 401: 인증 실패
- 409: 스탬프 부족 또는 소원권 보유 중
- 500: 교환 처리 실패

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot
**제안 우선순위:** P0

#### ERD 관련 사항

- `room_members.heart_count`: 스탬프 보유량
- `wish_coupons`: 소원권 발급 및 만료 정보
교환 시 `heart_count`를 3에서 0으로 갱신하고 `wish_coupons`에 새 기록을 생성한다.

---

### 22. 소원권 조회

- **구분**: 스탬프·소원권
- **Method**: GET
- **URL**: `/api/v1/coupons`
- **Parameter**: Header: memberToken
- **설명**: 소원권 상태 및 만료일 조회
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 소원권 조회

#### 엔드포인트

**GET** `/api/v1/coupons`

#### 기능 요구사항

기능명세서 4.8 스탬프·소원권 / 화면 구성 15
현재 보유한 소원권과 사용 가능 여부, 만료일, 남은 유효기간을 조회한다.

#### Request

Header: `Authorization: Bearer {memberToken}`
Body: 없음

#### Response

200 OK — 소원권 보유

```json
{
  "hasActiveCoupon": true,
  "coupon": {
    "couponId": 1,
    "status": "AVAILABLE",
    "issuedAt": "2026-11-21T20:00:00+09:00",
    "expiresAt": "2026-12-21T20:00:00+09:00",
    "remainingDays": 30
  }
}
```

200 OK — 소원권 없음

```json
{
  "hasActiveCoupon": false,
  "coupon": null
}
```

#### 처리 규칙

- 본인이 보유한 소원권만 조회한다.
- 사용 가능한 소원권은 최대 1장이다.
- 발급일과 만료일을 반환한다.
- 안드로이드에서 D-day를 표시할 수 있도록 남은 일수를 제공한다.
- 만료된 소원권은 사용 가능 목록에 포함하지 않는다.
- 만료 시 별도의 경고 알림을 보내지 않는다.
- 소원권 사용 요청이 대기 중인 경우 해당 상태를 반환한다.

#### 예외 처리

- 401: 인증 실패
- 404: 회원 정보를 찾을 수 없음
- 500: 조회 실패

#### 담당 호출 흐름

Android → Spring Boot → DB
**담당:** Spring Boot
**제안 우선순위:** P0

#### ERD 관련 사항

`wish_coupons`의 다음 컬럼을 활용한다.
- `owner_member_id`: 보유자
- `issued_at`: 발급일
- `expires_at`: 만료일
- `status`: 사용 상태
- `used_at`: 사용일
**협의 필요:** 기능명세서의 '만료 시 삭제'를 실제 DB 삭제로 처리할지, `EXPIRED` 상태로 기록을 보존하고 조회에서 제외할지 결정해야 한다.

---

### 23. 소원권 사용 요청

- **구분**: 스탬프·소원권
- **Method**: POST
- **URL**: `/api/v1/coupons/{couponId}/use-request`
- **Parameter**: Path: couponId
- **설명**: 상대에게 사용 확인 요청
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 소원권 사용 요청

#### 엔드포인트

**POST** `/api/v1/coupons/{couponId}/use-request`

#### 기능 요구사항

기능명세서 4.8 스탬프·소원권 / 화면 구성 16
소원권 보유자가 사용 버튼을 클릭하면 상대방에게 소원권 사용 확인 요청을 전달한다.

#### Request

Path: `couponId`
Header: `Authorization: Bearer {memberToken}`
Body: 없음

#### Response

200 OK

```json
{
  "couponId": 1,
  "status": "PENDING",
  "requestedByMemberId": 1,
  "waitingForPartner": true
}
```

#### 처리 규칙

- 본인이 소유한 소원권만 사용할 수 있다.
- 소원권 상태가 `AVAILABLE`인 경우에만 요청할 수 있다.
- 만료된 소원권은 사용을 요청할 수 없다.
- 상대방에게 사용 확인 요청을 전달한다.
- 상대방 응답 전에는 소원권을 사용 완료 처리하지 않는다.
- 사용 요청이 중복 생성되지 않도록 한다.
- 요청 대기 상태에서는 중복 사용을 제한한다.
- 상대방이 사용 확인을 진행할 수 있도록 요청 상태를 조회 가능하게 한다.

#### 예외 처리

- 400: 잘못된 사용 요청
- 401: 인증 실패
- 403: 소원권 소유자가 아님
- 404: 소원권이 존재하지 않음
- 409: 만료, 사용 완료 또는 이미 요청 중
- 500: 요청 처리 실패

#### 담당 호출 흐름

Android → Spring Boot → DB → Android(상대방 상태 조회 또는 알림)
**담당:** Spring Boot
**제안 우선순위:** P0

#### ERD 관련 사항

현재 `wish_coupons.status`에는 `AVAILABLE`, `USED`, `EXPIRED`가 정의되어 있다.
상대방 확인 대기를 별도로 처리하려면 `PENDING` 상태와 `use_requested_at` 등의 요청 정보를 추가하는 방식을 제안한다.
실시간 알림 방식은 아직 미확정이다.

---

### 24. 소원권 사용 응답

- **구분**: 스탬프·소원권
- **Method**: POST
- **URL**: `/api/v1/coupons/{couponId}/respond`
- **Parameter**: Path: couponId; Body: approved
- **설명**: 상대의 사용 확인 처리
- **우선순위**: P0
- **담당**: Spring Boot
- **호출 경로**: Android → Spring Boot


### 소원권 사용 응답

#### 엔드포인트

**POST** `/api/v1/coupons/{couponId}/respond`

#### 기능 요구사항

기능명세서 4.8 스탬프·소원권 / 화면 구성 16
상대방이 소원권 사용 요청을 확인하고 응답한다.

#### Request

Path: `couponId`
Header: `Authorization: Bearer {memberToken}`
Body

```json
{
  "approved": true
}
```

#### Response

200 OK — 사용 승인

```json
{
  "couponId": 1,
  "status": "USED",
  "approved": true,
  "usedAt": "2026-11-22T10:00:00+09:00"
}
```

200 OK — 사용 거절(제안)

```json
{
  "couponId": 1,
  "status": "AVAILABLE",
  "approved": false,
  "usedAt": null
}
```

#### 처리 규칙

- 소원권 사용 요청을 받은 상대방만 응답할 수 있다.
- 소원권 소유자는 자기 요청을 승인할 수 없다.
- 사용 요청이 대기 중인 경우에만 응답할 수 있다.
- 승인 시 상태를 `USED`로 변경한다.
- 승인 시 사용 시각을 저장한다.
- 사용 완료된 소원권은 재사용할 수 없다.
- 중복 응답으로 여러 번 사용 처리되지 않도록 한다.
- 승인 결과를 소원권 소유자의 화면에 반영한다.

#### 예외 처리

- 400: 잘못된 응답값
- 401: 인증 실패
- 403: 응답 권한 없음
- 404: 해당 소원권 없음
- 409: 처리할 사용 요청이 없거나 이미 완료됨
- 500: 처리 실패

#### 담당 호출 흐름

Android → Spring Boot → DB → Android(요청자 상태 반영)
**담당:** Spring Boot
**제안 우선순위:** P0

#### 구현 전 협의 사항

기능명세서는 상대방의 '사용 승인 알림'을 요구하지만, 거절 시 처리 규칙은 상세히 확정되지 않았다.
위 `approved = false` 처리와 `AVAILABLE` 복귀는 개발 제안이며 팀 합의가 필요하다.
현재 ERD의 `wish_coupons`에 요청 및 승인 상태를 표현할 컬럼 보완이 필요하다.

---
