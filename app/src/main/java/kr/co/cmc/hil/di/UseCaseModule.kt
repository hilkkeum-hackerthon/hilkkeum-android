package kr.co.cmc.hil.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
object UseCaseModule {

}
