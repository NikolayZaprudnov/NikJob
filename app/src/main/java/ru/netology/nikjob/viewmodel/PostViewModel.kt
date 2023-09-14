package ru.netology.nikjob.viewmodel

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nikjob.activity.AppActivity
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.dto.FeedItem
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.model.FeedModelState
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.repository.PostRepository
import util.SingleLiveEvent
import java.net.URL
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    likeOwnerIds = emptyList(),
    countShared = 0,
    mentionIds = emptyList(),
    published = ""
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
        }

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }


    fun loadPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            repository.getAllAsynch()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        repository.likeById(id)
    }

    fun showAll() = viewModelScope.launch {
        repository.showAll()
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        repository.unlikeById(id)
    }

    fun repostById(id: Long) = viewModelScope.launch {
        repository.repostById(id)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        repository.removeById(id)
    }

    fun playVideo(post: Post, videoPlayer: VideoView) = viewModelScope.launch{
        repository.playVideo(post, videoPlayer)
    }

    fun playAudio(post: Post, player: MediaPlayer) = viewModelScope.launch {
        repository.playAudio(post, player)
    }


    fun save() {
        edited.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    photoState.value?.let {
                        repository.saveWithAttachment(post, it)
                    } ?: repository.save(post)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty.copy()
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }


}

