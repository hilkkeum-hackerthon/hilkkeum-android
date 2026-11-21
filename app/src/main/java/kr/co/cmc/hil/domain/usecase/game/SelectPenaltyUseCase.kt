package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.PenaltySelection
import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 내가 이기면 상대가 수행할 벌칙 1개를 고른다.
 */
class SelectPenaltyUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        gameId: GameId,
        penaltyId: PenaltyId,
    ): AppResult<PenaltySelection, AppError.Network> =
        gameRepository.selectPenalty(gameId, penaltyId)
}
