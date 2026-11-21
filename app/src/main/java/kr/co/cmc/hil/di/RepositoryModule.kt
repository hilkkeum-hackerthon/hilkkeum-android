package kr.co.cmc.hil.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository 인터페이스(domain)와 구현체(data)를 연결한다.
 *
 * 추가 방법:
 * 1. 구현체 생성자에 `@Inject constructor`를 붙인다.
 * 2. 아래 예시처럼 `@Binds` 함수를 추가한다.
 *
 * ```
 * @Binds
 * @Singleton
 * abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

}
