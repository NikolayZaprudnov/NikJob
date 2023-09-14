package ru.netology.nikjob.activity

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.databinding.FragmentNewPostBinding
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.viewmodel.PostViewModel
import util.StringArg

@AndroidEntryPoint
class NewPostFragment : Fragment() {


    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(requireContext(),
                    "Error",
                    Toast.LENGTH_SHORT).show()
                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    val file = uri.toFile()
                    viewModel.changePhoto(PhotoModel(uri, file))
                }
            }
        }
    val SAVE_DRAFT_FILENAME = "Draft"
    val SETTING = "settings"
    var savedDraft: SharedPreferences? = null
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
        var draft: String?
        savedDraft = context?.getSharedPreferences(SAVE_DRAFT_FILENAME, 0)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            draft = binding.editText.text.toString()
            val editor = savedDraft!!.edit()
            editor.putString(SETTING, draft)
            editor.apply()
            Toast.makeText(context, R.string.draftText, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.feedFragment)
        }
        draft = savedDraft!!.getString(SETTING, "").toString()
        binding.editText.setText(draft)
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
                        val editor = savedDraft!!.edit()
                        draft = null
                        editor.putString(SETTING, draft)
                        editor.apply()
                        true
                    }
                    else -> false

                }


        }, viewLifecycleOwner)
        viewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel == null) {
                binding.photoContainer.isVisible = false
                return@observe
            }
            binding.photoContainer.isVisible = true
            binding.preview.setImageURI(photoModel.uri)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }
        binding.remotePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }
        return binding.root
    }
}
