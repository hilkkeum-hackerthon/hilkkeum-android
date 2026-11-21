package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.MissionSelection
import kr.co.cmc.hil.domain.model.mission.MissionId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 내가 수행할 비밀 미션을 게임에 설정된 개수만큼 고른다.
 */
class SelectMissionsUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        gameId: GameId,
        missionIds: List<MissionId>,
    ): AppResult<MissionSelection, AppError.Network> =
        gameRepository.selectMissions(gameId, missionIds)
}
