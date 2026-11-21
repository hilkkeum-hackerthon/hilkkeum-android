package kr.co.cmc.hil.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.cmc.hil.domain.repository.RoomRepository
import kr.co.cmc.hil.domain.repository.WishStampRepository
import kr.co.cmc.hil.domain.usecase.GetWishStampProgressUseCase
import kr.co.cmc.hil.domain.usecase.room.CreateRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.GetRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.JoinRoomUseCase
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
}
