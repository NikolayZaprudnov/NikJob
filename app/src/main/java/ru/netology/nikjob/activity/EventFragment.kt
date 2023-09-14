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
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nikjob.R
import ru.netology.nikjob.activity.NewPostFragment.Companion.textArg
import ru.netology.nikjob.adapter.*
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.databinding.FragmentEventBinding
import ru.netology.nikjob.databinding.FragmentFeedBinding
import ru.netology.nikjob.dialog.CheckRegistrationDialog
import ru.netology.nikjob.dto.Event
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.enumeration.AttachmentType
import ru.netology.nikjob.viewmodel.AuthViewModel
import ru.netology.nikjob.viewmodel.PostViewModel
import javax.inject.Inject

class EventFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentEventBinding.inflate(
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
                        viewModel.likeById(event.id)
                    } else {
                        viewModel.unlikeById(event.id)
                    }
                }
            }

            override fun onRepost(event: Event) {
                if (!authViewModel.authorized) {
                    dialog.show(manager, "")
                } else {
                    viewModel.repostById(event.id)
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
//                viewModel.edit(event)
            }

            override fun onOpenImage(event: Event) {
                findNavController().navigate(R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = event.attachment?.url
                    })
            }

            override fun onPlay(event: Event, videoPlayer: VideoView, player: MediaPlayer) {
                when (event.attachment?.type) {
                    AttachmentType.VIDEO -> {
//                        viewModel.playVideo(event, videoPlayer)
                    }
                    AttachmentType.AUDIO -> {
//                        viewModel.playAudio(event, player)
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Invalid data type", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onLink(event: Event) {
                val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
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

