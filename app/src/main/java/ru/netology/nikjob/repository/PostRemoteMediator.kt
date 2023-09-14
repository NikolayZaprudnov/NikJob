package ru.netology.nikjob.repository

import androidx.paging.*
import androidx.room.withTransaction
import okio.IOException
import retrofit2.HttpException
import ru.netology.nikjob.api.PostsApiService
import ru.netology.nikjob.dao.PostDao
import ru.netology.nikjob.dao.PostRemoteKeyDao
import ru.netology.nikjob.db.AppDb
import ru.netology.nikjob.entity.PostEntity
import ru.netology.nikjob.entity.PostRemoteKeyEntity
import ru.netology.nikjob.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostsApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    postRemoteKeyDao.max()?.let { key -> apiService.getAfter(key, state.config.pageSize)
                    } ?: apiService.getLatest(state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }
            }
            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val body = result.body() ?: throw ApiError(result.code(), result.message())
            if (body.isEmpty()) return MediatorResult.Success(false)
            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id,
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id,
                                )
                            )
                        )
                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id,
                            ),
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            )
                        )
                    }
                }

                postDao.insert(body.map { PostEntity.fromDto(it) })
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }


}