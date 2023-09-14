package ru.netology.nikjob.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.repository.PostRepository
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    val state = appAuth.authStateFlow.asLiveData()
    val authorized: Boolean
        get() = state.value?.id != 0L

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

    fun updateUser(login: String, pass: String) = viewModelScope.launch {
        repository.updateUser(login, pass)
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }

    fun reggistrationUser(login: String, pass: String, name: String) = viewModelScope.launch {
        repository.registerUser(login, pass, name)
    }

    fun registrationUserWithPhoto(login: String, pass: String, name: String, avatar: PhotoModel) =
        viewModelScope.launch {
            repository.registerWithPhoto(login, pass, name, avatar)
        }

}