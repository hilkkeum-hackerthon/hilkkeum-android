package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * 서버가 처음 보는 값을 보내면 역직렬화가 실패해 `safeApiCall`이 SERIALIZATION 에러로 돌려준다.
 */
@Serializable
enum class GameStatusDto {
    SETTING,
    PLAYING,
    GUESSING,
    FINISHED,
}
