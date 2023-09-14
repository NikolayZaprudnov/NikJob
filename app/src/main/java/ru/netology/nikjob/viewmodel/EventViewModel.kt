package ru.netology.nikjob.viewmodel


import android.media.MediaPlayer
import android.widget.VideoView
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
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.dto.Event
import ru.netology.nikjob.dto.FeedItem
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.model.FeedModelState
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.repository.PostRepository
import util.SingleLiveEvent
import javax.inject.Inject

private val empty = Event(

    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = "",

    )

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = event.authorId == myId)
                    } else {
                        event
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

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    init {
        loadEvents()
    }


    fun loadEvents() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            repository.getAllAsynch()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun likeById(event: Event) = viewModelScope.launch {
        repository.likeByIdEvents(event)
    }


    fun removeById(id: Long) = viewModelScope.launch {
        repository.removeEventsById(id)
    }

    fun playVideo(post: Post, videoPlayer: VideoView) = viewModelScope.launch {
        repository.playVideo(post, videoPlayer)
    }

    fun playAudio(post: Post, player: MediaPlayer) = viewModelScope.launch {
        repository.playAudio(post, player)
    }


    fun save() {
        edited.value?.let { event ->
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    photoState.value?.let {
                        repository.saveEventWithAttachment(event, it)
                    } ?: repository.saveEvents(event)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty.copy()
    }

    fun edit(event: Event) {
        edited.value = event
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

