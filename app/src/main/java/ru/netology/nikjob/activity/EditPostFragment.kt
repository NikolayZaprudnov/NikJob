package ru.netology.nikjob.activity


import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.databinding.FragmentNewPostBinding
import ru.netology.nikjob.viewmodel.PostViewModel
import util.StringArg


@AndroidEntryPoint
class EditPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        binding.editText.setText(arguments?.textArg.toString())
        binding.editText.requestFocus()
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.ok -> {
                        viewModel.changeContent(binding.editText.text.toString())
                        viewModel.save()
                        true
                    }
                    R.id.not -> {
                        findNavController().navigateUp()
                    }
                    else -> false

                }


        }, viewLifecycleOwner)
        return binding.root
    }
}

