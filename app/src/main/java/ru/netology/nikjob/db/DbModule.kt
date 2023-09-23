package ru.netology.nikjob.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nikjob.dao.EventDao
import ru.netology.nikjob.dao.EventRemoteKeyDao
import ru.netology.nikjob.dao.PostDao
import ru.netology.nikjob.dao.PostRemoteKeyDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context,
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePostDao(
        appDb: AppDb,
    ): PostDao = appDb.postDao()

    @Provides
    fun provideEventDao(
        appDb: AppDb,
    ): EventDao = appDb.eventDao()

    @Provides
    fun providePostRemoteKeyDao(
        appDb: AppDb
    ): PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    fun provideEventRemoteKeyDao(
        appDb: AppDb
    ): EventRemoteKeyDao = appDb.eventRemoteKeyDao()
}