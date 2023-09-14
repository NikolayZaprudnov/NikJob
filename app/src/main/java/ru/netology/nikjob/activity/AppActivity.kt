package ru.netology.nikjob.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

import ru.netology.nikjob.R
import ru.netology.nikjob.activity.NewPostFragment.Companion.textArg
import ru.netology.nikjob.viewmodel.AuthViewModel


@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }
            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(androidx.navigation.fragment.R.id.nav_host_fragment_container).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArg = text
                })
        }

    }
}


