package ru.netology.nikjob.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.activity.NewPostFragment.Companion.textArg
import ru.netology.nikjob.adapter.EventOnInteractionListener
import ru.netology.nikjob.adapter.EventsAdapter
import ru.netology.nikjob.databinding.FragmentOnePostBinding
import ru.netology.nikjob.dialog.CheckRegistrationDialog
import ru.netology.nikjob.dto.Event
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.viewmodel.AuthViewModel
import ru.netology.nikjob.viewmodel.EventViewModel

@AndroidEntryPoint
class OneEventFragment : Fragment(
) {

    private val viewModel: EventViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentOnePostBinding.inflate(
            inflater,
            container,
            false
        )
        val dialog = CheckRegistrationDialog()

        val manager = parentFragmentManager
        val adapter = EventsAdapter(object : EventOnInteractionListener {


            override fun onLike(event: Event) {
                if (!authViewModel.authorized) {
                    dialog.show(manager, "")
                } else {
                    if (event.likedByMe == false) {
                        viewModel.likeById(event)
                    }
                }
            }

            override fun onRepost(event: Event) {
                if (!authViewModel.authorized) {
                    dialog.show(manager, "")
                } else {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, event.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, "Repost post")
                    startActivity(shareIntent)
                }
            }

            override fun onOpen(event: Event) {
                findNavController().navigate(R.id.action_feedFragment_to_onePostFragment,
                    Bundle().apply {
                        val idArg = putLong("id", event.id)
                    })
            }


            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onEdit(event: Event) {
                viewModel.edit(event)
            }

            override fun onOpenImage(event: Event) {
                findNavController().navigate(R.id.action_eventFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = event.attachment?.url
                    })
            }

            override fun onPlay(event: Event, videoPlayer: VideoView, player: MediaPlayer) {
                when (event.attachment?.type) {
                    AttachmentType.VIDEO -> {
                        viewModel.playVideo(event, videoPlayer)
                    }
                    AttachmentType.AUDIO -> {
                        viewModel.playAudio(event, player)
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Invalid data type", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onLink(event: Event) {
                val uriPost = "https://" + event.link
                try {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriPost))
                    startActivity(linkIntent)
                } catch (e: Exception){ }

            }
        })

        binding.list.adapter = adapter

        viewModel.edited.observe(viewLifecycleOwner) { event ->
            if (event.id == 0L) {
                return@observe
            }
            findNavController().navigate(R.id.action_onePostFragment_to_editPostFragment,
                Bundle().apply {
                    textArg = event.content
                    arguments = bundleOf(
                        "authorId" to event.author,
                        "content" to event.content
                    )
                })
        }
        return binding.root
    }
}