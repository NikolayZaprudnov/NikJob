package ru.netology.nikjob.repository


import android.media.MediaPlayer
import android.widget.VideoView
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nikjob.dto.FeedItem
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun repostById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(postS: Post)
    suspend fun saveWithAttachment(postS: Post, photoModel: PhotoModel)
    suspend fun getAllAsynch()
    suspend fun showAll()
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String)
    suspend fun registerWithPhoto(login: String, pass: String, name: String, avatar: PhotoModel)
    suspend fun playVideo(post: Post, videoPlayer: VideoView)
    suspend fun playAudio(post: Post, player: MediaPlayer)
}
