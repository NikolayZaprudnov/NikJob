package ru.netology.nikjob.repository


import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nikjob.api.AuthApiService
import ru.netology.nikjob.api.MediaApiService
import ru.netology.nikjob.api.PostsApiService
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.dao.PostDao
import ru.netology.nikjob.dao.PostRemoteKeyDao
import ru.netology.nikjob.db.AppDb
import ru.netology.nikjob.dto.*
import ru.netology.nikjob.entity.*
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.error.ApiError
import ru.netology.nikjob.error.AppError
import ru.netology.nikjob.error.NetworkError
import ru.netology.nikjob.model.PhotoModel
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostsApiService,
    private val authApiService: AuthApiService,
    private val mediaApiService: MediaApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {


    @Inject
    lateinit var appAuth: AppAuth

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            postDao.getPagingSourse()
        },
        remoteMediator = PostRemoteMediator(apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,)
    ).flow
        .map {
            it.map(PostEntity::toDto)
                .insertSeparators { previos, _  ->
                    if (previos?.id?.rem(5) == 0L){
                        Ad(Random.nextLong(), "figma.jpg")
                    }else{
                        null
                }
                }
        }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val posts = response.body().orEmpty()
            postDao.insert(posts.toEntity().map {
                it.copy()
            })
            emit(posts.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun getAllAsynch() {
        val response = apiService.getAll()
        if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
        response.body() ?: throw RuntimeException("Body is null")
        postDao.insert(response.body()!!.map { PostEntity.fromDto(it) })

    }

    override suspend fun likeById(id: Long) {
        postDao.likeById(id)
        val response = apiService.likeById(id)
        if (!response.isSuccessful) {
            postDao.unlikeById(id)
        }
    }

    override suspend fun unlikeById(id: Long) {
        postDao.unlikeById(id)
        val response = apiService.unlikeById(id)
        if (!response.isSuccessful) {
            postDao.likeById(id)
        };
    }



    override suspend fun repostById(id: Long) {
        TODO("Not yet implemented")
    }


    override suspend fun showAll() {
        postDao.showAll()
    }

    override suspend fun save(postS: Post) {
        try {
            val response = apiService.save(postS)
            if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
            val body = response.body() ?: throw RuntimeException("Body is null")
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }


    override suspend fun saveWithAttachment(postS: Post, photoModel: PhotoModel) {
        try {
            val media = upload(photoModel)
            val response = apiService.save(postS.copy(
                attachment = Attachment(media.id, AttachmentType.IMAGE)
            ))
            if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
            val body = response.body() ?: throw RuntimeException("Body is null")
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun updateUser(login: String, pass: String) {
        val response = authApiService.updateUser(login, pass)
        if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
        val userId = response.body()!!.id
        val userToken = response.body()?.token
        appAuth.setAuth(userId, userToken)
    }

    override suspend fun registerUser(login: String, pass: String, name: String) {
        val response = authApiService.registerUser(login, pass, name)
        if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
        val userId = response.body()!!.id
        val userToken = response.body()?.token
        appAuth.setAuth(userId, userToken)
    }

    override suspend fun registerWithPhoto(
        login: String,
        pass: String,
        name: String,
        avatar: PhotoModel,
    ) {
        val response = authApiService.registerWithPhoto(
            login.toRequestBody(),
            pass.toRequestBody(),
            name.toRequestBody(),
            MultipartBody.Part.createFormData("file",
                avatar.file.name,
                avatar.file.asRequestBody()),
        )
        val userId = response.body()!!.id
        val userToken = response.body()?.token
        appAuth.setAuth(userId, userToken)
    }

    private suspend fun upload(photo: PhotoModel): Media {
        val response = mediaApiService.uploadPhoto(
            MultipartBody.Part.createFormData("file", photo.file.name, photo.file.asRequestBody())
        )
        return response.body() ?: throw  RuntimeException("Body is null")
    }

    override suspend fun removeById(id: Long) {
        val response = apiService.removeById(id)
        if (response.isSuccessful) {
            postDao.removeById(id)
        }; throw RuntimeException("API SERVICE ERROR")
    }
    override suspend fun playVideo(post: Post, videoPlayer: VideoView) {
                videoPlayer.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(Uri.parse(post.attachment?.url))
                    setOnPreparedListener { start() }
                    setOnCompletionListener { stopPlayback() }
                }
            }

    override suspend fun playAudio(post: Post, player: MediaPlayer) {
        player.apply {
            if (isPlaying) {
                pause()
            } else {
                reset()
                setDataSource(post.attachment?.url)
                prepare()
                start()
            }
        }
    }

}

