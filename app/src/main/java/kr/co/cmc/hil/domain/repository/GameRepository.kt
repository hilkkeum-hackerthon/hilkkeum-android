package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.EmptyAppResult
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GameResult
import kr.co.cmc.hil.domain.model.game.GuessId
import kr.co.cmc.hil.domain.model.game.GuessJudgment
import kr.co.cmc.hil.domain.model.game.GuessSubmission
import kr.co.cmc.hil.domain.model.game.MissionSelection
import kr.co.cmc.hil.domain.model.game.PenaltySelection
import kr.co.cmc.hil.domain.model.game.ReceivedGuesses
import kr.co.cmc.hil.domain.model.mission.MissionId
import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.model.room.RoomId

interface GameRepository {

    /**
     * 방장이 데이트 유형과 미션 개수를 정해 새 게임을 만든다.
     * 방에 두 명이 입장한 경우에만 생성된다.
     *
     * @param roomId 게임을 만들 방
     * @param dateType 데이트 유형(온라인/오프라인)
     * @param missionCount 각자 고를 미션 개수
     * @return 성공하면 생성된 게임 정보, 실패하면 [AppError.Network]
     */
    suspend fun createGame(
        roomId: RoomId,
        dateType: DateType,
        missionCount: Int,
    ): AppResult<CreatedGame, AppError.Network>

    /**
     * 게임 상태와 두 참가자의 진행 현황을 조회한다. 상대의 미션·벌칙 내용은 포함하지 않는다.
     * 참가자가 아니면 [AppError.Network.FORBIDDEN], 게임이 없으면 [AppError.Network.NOT_FOUND]로 실패한다.
     *
     * @param gameId 조회할 게임
     * @return 성공하면 게임 상태와 진행 현황, 실패하면 [AppError.Network]
     */
    suspend fun getGame(gameId: GameId): AppResult<Game, AppError.Network>

    /**
     * 방에서 진행 중인 게임의 ID를 조회한다. 게임을 만들지 않은 상대가 gameId를 얻을 때 쓴다.
     * 끝난(FINISHED) 게임은 반환하지 않으므로, 결과 화면에서는 이미 받은 gameId를 보관해 써야 한다.
     *
     * @param roomId 조회할 방
     * @return 성공하면 진행 중인 게임 ID(없으면 null), 실패하면 [AppError.Network]
     */
    suspend fun getActiveGameId(roomId: RoomId): AppResult<GameId?, AppError.Network>

    /**
     * 내가 수행할 비밀 미션을 고른다. 게임에 설정된 미션 개수와 정확히 같은 수만큼 골라야 하며 다시 제출할 수 없다.
     * 양쪽 모두 미션·벌칙 선택을 마치면 게임이 진행 단계로 넘어간다.
     *
     * @param gameId 미션을 고를 게임
     * @param missionIds 고른 미션(중복 없이 게임의 미션 개수만큼)
     * @return 성공하면 선택 결과, 실패하면 [AppError.Network]
     */
    suspend fun selectMissions(
        gameId: GameId,
        missionIds: List<MissionId>,
    ): AppResult<MissionSelection, AppError.Network>

    /**
     * 내가 이기면 상대가 수행할 벌칙 1개를 고른다. 다시 제출하면 [AppError.Network.CONFLICT]로 실패한다.
     * 양쪽 모두 미션·벌칙 선택을 마치면 게임이 진행 단계로 넘어간다.
     *
     * @param gameId 벌칙을 고를 게임
     * @param penaltyId 고른 벌칙
     * @return 성공하면 선택 결과, 실패하면 [AppError.Network]
     */
    suspend fun selectPenalty(
        gameId: GameId,
        penaltyId: PenaltyId,
    ): AppResult<PenaltySelection, AppError.Network>

    /**
     * 진행 중인 게임을 즉시 끝내고 추리 단계로 넘긴다. 상대의 승인 없이 바로 처리된다.
     * 진행 단계가 아니면 [AppError.Network.CONFLICT]로 실패한다.
     *
     * @param gameId 끝낼 게임
     * @return 성공하면 [kr.co.cmc.hil.domain.common.AppResult.Success], 실패하면 [AppError.Network]
     */
    suspend fun endGame(gameId: GameId): EmptyAppResult<AppError.Network>

    /**
     * 상대의 미션을 추리한 문장을 한 번에 제출한다. 추리 단계에서만 가능하며 제출 후에는 고칠 수 없다.
     *
     * @param gameId 추리를 제출할 게임
     * @param guesses 추리 문장 목록. 게임의 미션 개수만큼, 각 1~30자의 공백이 아닌 문장이어야 하며 순서대로 1번부터 번호가 매겨진다.
     * @return 성공하면 제출 결과, 실패하면 [AppError.Network]
     */
    suspend fun submitGuesses(
        gameId: GameId,
        guesses: List<String>,
    ): AppResult<GuessSubmission, AppError.Network>

    /**
     * 상대가 내 미션에 대해 보낸 추리 목록을 조회한다. 상대가 아직 제출하지 않았으면 빈 목록이 올 수 있다.
     * 미션·벌칙 선택 단계나 진행 단계에서는 조회할 수 없다.
     *
     * @param gameId 조회할 게임
     * @return 성공하면 받은 추리 목록, 실패하면 [AppError.Network]
     */
    suspend fun getReceivedGuesses(gameId: GameId): AppResult<ReceivedGuesses, AppError.Network>

    /**
     * 상대가 보낸 추리 1건의 정답 여부를 확인한다. 정답이면 추리 작성자가 즉시 승리하고 게임이 끝난다.
     * 이미 확인한 추리를 다시 판정할 수 없다.
     *
     * @param gameId 추리가 속한 게임
     * @param guessId 판정할 추리
     * @param isCorrect 정답 여부
     * @return 성공하면 판정 결과, 실패하면 [AppError.Network]
     */
    suspend fun judgeGuess(
        gameId: GameId,
        guessId: GuessId,
        isCorrect: Boolean,
    ): AppResult<GuessJudgment, AppError.Network>

    /**
     * 승패가 확정된 게임의 승자·패자와 승자가 고른 벌칙을 조회한다.
     * 승패가 아직 정해지지 않았으면 [AppError.Network.CONFLICT]로 실패한다.
     *
     * @param gameId 조회할 게임
     * @return 성공하면 최종 결과, 실패하면 [AppError.Network]
     */
    suspend fun getGameResult(gameId: GameId): AppResult<GameResult, AppError.Network>
}
