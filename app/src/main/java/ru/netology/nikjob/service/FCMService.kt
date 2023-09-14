package ru.netology.nikjob.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val userId = appAuth.authStateFlow.value.id
        val bodyNotification = gson.fromJson(message.data[content], UserNotification::class.java)
        val recipientId = bodyNotification.recipientId
        if (recipientId == userId || recipientId == null) {
            showNotification(bodyNotification)
        } else if (recipientId == 0L && recipientId != userId) {
            appAuth.sendPushToken()
        }

//        message.data[action]?.let {
//            if (it == "LIKE" || it == "POST") {
//                when (Action.valueOf(it)) {
//                    Action.LIKE -> handleLike(gson.fromJson(message.data[content],
//                        Like::class.java))
//                    Action.POST -> handlePost(gson.fromJson(message.data[content],
//                        Post::class.java))
//                }
//            } else {  handleExeption(gson.fromJson(message.data[content], Error::class.java)) }
//        }
    }


    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
        println(token)
    }

    private fun showNotification(content: UserNotification) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                content.content
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

//    private fun handleLike(content: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handleExeption(content: Error) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(R.string.notification_error)
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
//
//    private fun handlePost(content: Post) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_new_post,
//                    content.postAuthor,
//                )
//            )
//            .setStyle(NotificationCompat.BigTextStyle()
//                .bigText(content.postContent))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        NotificationManagerCompat.from(this)
//            .notify(Random.nextInt(100_000), notification)
//    }
}

enum class Action {
    LIKE,
    POST,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class Post(
    val userId: Long,
    val postId: Long,
    val postAuthor: String,
    val postContent: String,
)

data class UserNotification(
    val recipientId: Long,
    val content: String,
)



