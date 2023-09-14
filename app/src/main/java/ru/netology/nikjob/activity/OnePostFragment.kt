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
import ru.netology.nikjob.adapter.OnInteractionListener
import ru.netology.nikjob.adapter.PostsAdapter
import ru.netology.nikjob.databinding.FragmentOnePostBinding
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.viewmodel.PostViewModel

@AndroidEntryPoint
class OnePostFragment : Fragment(
) {

    private val viewModel: PostViewModel by activityViewModels()

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
        val passedId = arguments?.getLong("id")
        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRepost(post: Post) {
                viewModel.repostById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, "Repost post")
                startActivity(shareIntent)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onOpenImage(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }

            override fun onPlay(post: Post, videoPlayer: VideoView, player: MediaPlayer) {
                when (post.attachment?.type) {
                    AttachmentType.VIDEO -> {
                        viewModel.playVideo(post, videoPlayer)
                    }
                    AttachmentType.AUDIO -> {
                        viewModel.playAudio(post, player)
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Invalid data type", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onOpen(post: Post) {
            }

            override fun onLink(post: Post) {
                val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
                startActivity(linkIntent)
            }
        })

        binding.list.adapter = adapter
//        viewModel.data.observe(viewLifecycleOwner) { state ->
////            posts.posts.filter { it.id == passedId }
//            adapter.submitList(state.posts.filter { it.id == passedId })
//        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            findNavController().navigate(R.id.action_onePostFragment_to_editPostFragment,
                Bundle().apply {
                    textArg = post.content
                    arguments = bundleOf(
                        "authorId" to post.author,
                        "content" to post.content
                    )
                })
        }
        return binding.root
    }
}