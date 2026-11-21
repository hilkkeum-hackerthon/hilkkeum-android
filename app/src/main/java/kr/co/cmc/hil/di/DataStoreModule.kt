package kr.co.cmc.hil.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [kr.co.cmc.hil.data.datastore.TokenStorage]가 사용하는 Preferences [DataStore]를 제공한다.
 *
 * 파일 이름을 바꾸면 `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`의 제외 경로도 함께 바꿔야 한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val TOKEN_DATASTORE_NAME = "token_storage"

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(TOKEN_DATASTORE_NAME) },
    )
}
