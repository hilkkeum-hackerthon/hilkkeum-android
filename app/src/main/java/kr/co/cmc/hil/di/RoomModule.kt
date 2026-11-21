package kr.co.cmc.hil.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.cmc.hil.data.repositoryimpl.RoomRepositoryImpl
import kr.co.cmc.hil.domain.repository.RoomRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface RoomModule {
    @Binds
    fun bindRoomRepository(implementation: RoomRepositoryImpl): RoomRepository
}
