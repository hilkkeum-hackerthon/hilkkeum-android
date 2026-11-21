package kr.co.cmc.hil.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.cmc.hil.domain.repository.CouponRepository
import kr.co.cmc.hil.domain.repository.GameRepository
import kr.co.cmc.hil.domain.repository.MissionRepository
import kr.co.cmc.hil.domain.repository.RoomRepository
import kr.co.cmc.hil.domain.repository.StampRepository
import kr.co.cmc.hil.domain.repository.WishStampRepository
import kr.co.cmc.hil.domain.usecase.GetWishStampProgressUseCase
import kr.co.cmc.hil.domain.usecase.coupon.ExchangeCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.GetCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.RequestCouponUseUseCase
import kr.co.cmc.hil.domain.usecase.coupon.RespondCouponUseUseCase
import kr.co.cmc.hil.domain.usecase.game.CreateGameUseCase
import kr.co.cmc.hil.domain.usecase.game.EndGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetActiveGameIdUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameResultUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetReceivedGuessesUseCase
import kr.co.cmc.hil.domain.usecase.game.JudgeGuessUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectMissionsUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectPenaltyUseCase
import kr.co.cmc.hil.domain.usecase.game.SubmitGuessesUseCase
import kr.co.cmc.hil.domain.usecase.mission.GetMissionsUseCase
import kr.co.cmc.hil.domain.usecase.mission.GetPenaltiesUseCase
import kr.co.cmc.hil.domain.usecase.room.CreateRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.GetRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.JoinRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.RoomSessionUseCase
import kr.co.cmc.hil.domain.usecase.stamp.GetStampsUseCase
import javax.inject.Singleton

/**
 * UseCase 인스턴스를 제공한다.
 *
 * 추가 방법: 아래 예시처럼 `@Provides` 함수를 추가한다.
 *
 * ```
 * @Provides
 * @Singleton
 * fun provideCreatePostUseCase(repository: PostRepository): CreatePostUseCase =
 *     CreatePostUseCase(repository)
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
internal object UseCaseModule {
    @Provides
    fun provideGetWishStampProgressUseCase(repository: WishStampRepository): GetWishStampProgressUseCase =
        GetWishStampProgressUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateRoomUseCase(repository: RoomRepository): CreateRoomUseCase =
        CreateRoomUseCase(repository)

    @Provides
    @Singleton
    fun provideJoinRoomUseCase(repository: RoomRepository): JoinRoomUseCase =
        JoinRoomUseCase(repository)

    @Provides
    @Singleton
    fun provideGetRoomUseCase(repository: RoomRepository): GetRoomUseCase =
        GetRoomUseCase(repository)

    @Provides
    @Singleton
    fun provideRoomSessionUseCase(repository: RoomRepository): RoomSessionUseCase =
        RoomSessionUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateGameUseCase(repository: GameRepository): CreateGameUseCase =
        CreateGameUseCase(repository)

    @Provides
    @Singleton
    fun provideGetGameUseCase(repository: GameRepository): GetGameUseCase =
        GetGameUseCase(repository)

    @Provides
    @Singleton
    fun provideGetActiveGameIdUseCase(repository: GameRepository): GetActiveGameIdUseCase =
        GetActiveGameIdUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMissionsUseCase(repository: MissionRepository): GetMissionsUseCase =
        GetMissionsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetPenaltiesUseCase(repository: MissionRepository): GetPenaltiesUseCase =
        GetPenaltiesUseCase(repository)

    @Provides
    @Singleton
    fun provideSelectMissionsUseCase(repository: GameRepository): SelectMissionsUseCase =
        SelectMissionsUseCase(repository)

    @Provides
    @Singleton
    fun provideSelectPenaltyUseCase(repository: GameRepository): SelectPenaltyUseCase =
        SelectPenaltyUseCase(repository)

    @Provides
    @Singleton
    fun provideEndGameUseCase(repository: GameRepository): EndGameUseCase =
        EndGameUseCase(repository)

    @Provides
    @Singleton
    fun provideSubmitGuessesUseCase(repository: GameRepository): SubmitGuessesUseCase =
        SubmitGuessesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetReceivedGuessesUseCase(repository: GameRepository): GetReceivedGuessesUseCase =
        GetReceivedGuessesUseCase(repository)

    @Provides
    @Singleton
    fun provideJudgeGuessUseCase(repository: GameRepository): JudgeGuessUseCase =
        JudgeGuessUseCase(repository)

    @Provides
    @Singleton
    fun provideGetGameResultUseCase(repository: GameRepository): GetGameResultUseCase =
        GetGameResultUseCase(repository)

    @Provides
    @Singleton
    fun provideGetStampsUseCase(repository: StampRepository): GetStampsUseCase =
        GetStampsUseCase(repository)

    @Provides
    @Singleton
    fun provideExchangeCouponUseCase(repository: CouponRepository): ExchangeCouponUseCase =
        ExchangeCouponUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCouponUseCase(repository: CouponRepository): GetCouponUseCase =
        GetCouponUseCase(repository)

    @Provides
    @Singleton
    fun provideRequestCouponUseUseCase(repository: CouponRepository): RequestCouponUseUseCase =
        RequestCouponUseUseCase(repository)

    @Provides
    @Singleton
    fun provideRespondCouponUseUseCase(repository: CouponRepository): RespondCouponUseUseCase =
        RespondCouponUseUseCase(repository)
}
