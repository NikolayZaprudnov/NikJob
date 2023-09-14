package ru.netology.nikjob.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.widget.VideoView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nikjob.R
import ru.netology.nikjob.activity.NewPostFragment.Companion.textArg
import ru.netology.nikjob.adapter.OnInteractionListener
import ru.netology.nikjob.adapter.PostLoadingStateAdapter
import ru.netology.nikjob.adapter.PostsAdapter
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.databinding.FragmentFeedBinding
import ru.netology.nikjob.dialog.CheckRegistrationDialog
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.viewmodel.AuthViewModel
import ru.netology.nikjob.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )
        val dialog = CheckRegistrationDialog()

        val manager = parentFragmentManager

        val adapter = PostsAdapter(object : OnInteractionListener {


            override fun onLike(post: Post) {
                if (!authViewModel.authorized) {
                    dialog.show(manager, "")
                } else {
                    if (post.likedByMe == false) {
                        viewModel.likeById(post.id)
                    } else {
                        viewModel.unlikeById(post.id)
                    }
                }
            }

            override fun onRepost(post: Post) {
                if (!authViewModel.authorized) {
                    dialog.show(manager, "")
                } else {
                    viewModel.repostById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, "Repost post")
                    startActivity(shareIntent)
                }
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_onePostFragment,
                    Bundle().apply {
                        val idArg = putLong("id", post.id)
                    })
            }


            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
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

            override fun onLink(post: Post) {
                val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
                startActivity(linkIntent)
            }
        })
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            },
        )
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.refresh.isRefreshing = it.refresh is LoadState.Loading
            }
        }

        binding.freshPosts.setOnClickListener {
            viewModel.showAll()
            val position = (binding.list.scrollState)
            binding.list.smoothScrollToPosition(position)
            binding.freshPosts.isVisible = false
        }

        binding.refresh.setOnRefreshListener {
            adapter.refresh()
            binding.refresh.isRefreshing = false
        }
        binding.eventsNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_eventFragment2)
        }


        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            findNavController().navigate(R.id.action_feedFragment_to_editPostFragment,
                Bundle().apply {
                    textArg = post.content
                    arguments = bundleOf(
                        "authorId" to post.author,
                        "content" to post.content
                    )
                })
        }
        val draftText = arguments?.textArg.toString()
        binding.newpost.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            Bundle().apply {
                textArg = draftText
            }
        }


        var menuProvider: MenuProvider? = null
        authViewModel.state.observe(viewLifecycleOwner) { authState ->
            if (!authState.token.isNullOrEmpty()) {
                adapter.refresh()
            }

            menuProvider?.let { requireActivity().removeMenuProvider(it) }
            requireActivity().addMenuProvider(
                object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.main_menu, menu)
                        menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                        menu.setGroupVisible(R.id.unAuthorized, !authViewModel.authorized)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                        return when (menuItem.itemId) {
                            R.id.signOut -> {
                                appAuth.clear()
                                adapter.refresh()
                                true
                            }

                            R.id.signIn -> {
                                findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                                true
                            }

                            R.id.signUp -> {
                                findNavController().navigate(R.id.action_feedFragment_to_registrationFragment)
                                true
                            }

                            else -> false
                        }
                    }

                }.apply { menuProvider = this },
                viewLifecycleOwner,
            )


        }; return binding.root
    }
}

