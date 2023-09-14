package ru.netology.nikjob.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nikjob.api.PostsApiService
import ru.netology.nikjob.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authStateFlow = MutableStateFlow(AuthState())
    val authStateFlow = _authStateFlow.asStateFlow()

    init {
        var id: Long = 0L
        var token: String? = null
        token = prefs.getString(TOKEN, null)
        id = prefs.getLong(ID, 0L)
        if (id == 0L || token == null) {
            _authStateFlow.value = AuthState()
            prefs.edit {
                clear()
            }
        } else {
            _authStateFlow.value = AuthState(id = id, token = token)
        }
        sendPushToken()
    }

    @Synchronized
    fun clear() {
        _authStateFlow.value = AuthState()
        prefs.edit {
            clear()
        }
        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String?) {
        _authStateFlow.value = AuthState(id = id, token = token)
        prefs.edit {
            putLong(ID, id)
            putString(TOKEN, token)
        }
        sendPushToken()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppAuthEntryPoint {
        fun getApiService(): PostsApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().saveToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        const val ID = "id"
        const val TOKEN = "token"

//        @Volatile
//        private var INSTANCE: AppAuth? = null
//
//        fun init(context: Context) {
//            synchronized(this) {
//                INSTANCE = AppAuth(context)
//            }
//        }
//
//        fun getInstance(): AppAuth {
//            return synchronized(this) {
//                requireNotNull(INSTANCE)
//            }
//        }

    }

}

data class AuthState(val id: Long = 0L, val token: String? = null)