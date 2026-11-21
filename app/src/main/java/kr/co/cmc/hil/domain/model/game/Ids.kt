package kr.co.cmc.hil.domain.model.game

/** 게임(라운드) 식별자. */
@JvmInline
value class GameId(val value: Long)

/** 게임 참가자(game_players) 식별자. 방 멤버 식별자인 [kr.co.cmc.hil.domain.model.room.MemberId]와 다르다. */
@JvmInline
value class PlayerId(val value: Long)

/** 추리 식별자. */
@JvmInline
value class GuessId(val value: Long)
