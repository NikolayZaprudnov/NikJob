package ru.netology.nikjob.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import okio.IOException
import retrofit2.HttpException
import ru.netology.nikjob.api.EventApiService
import ru.netology.nikjob.dao.EventDao
import ru.netology.nikjob.dao.EventRemoteKeyDao
import ru.netology.nikjob.db.AppDb
import ru.netology.nikjob.entity.EventRemoteKeyEntity
import ru.netology.nikjob.entity.EventsEntity
import ru.netology.nikjob.entity.PostEntity
import ru.netology.nikjob.entity.PostRemoteKeyEntity
import ru.netology.nikjob.error.ApiError


@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: EventApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, EventsEntity>() {



    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventsEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    eventRemoteKeyDao.max()?.let { key -> apiService.getAfter(key, state.config.pageSize)
                    } ?: apiService.getLatest(state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
            }
            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val body = result.body() ?: throw ApiError( result.message())
            if (body.isEmpty()) return MediatorResult.Success(false)
            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        eventRemoteKeyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id,
                                ),
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id,
                                )
                            )
                        )
                    }
                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                        )
                    }
                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            )
                        )
                    }
                }

                eventDao.insertEvents(body.map { EventsEntity.fromDto(it) })
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}


