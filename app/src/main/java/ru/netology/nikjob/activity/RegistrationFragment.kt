package ru.netology.nikjob.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.databinding.FragmentRegistrationBinding
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()
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
                    avatarImage = PhotoModel(uri, file)
                }
            }
        }
    private var avatarImage: PhotoModel? = null
    private var availabilityPhoto = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )
        viewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel == null) {
                return@observe
            }
            binding.avatar.setImageURI(photoModel.uri)
            availabilityPhoto = true
        }

        binding.login.requestFocus()


        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        binding.buttonRegistration.setOnClickListener {
            val name = binding.name.text.toString()
            val pass = binding.password.text.toString()
            val repeatPass = binding.textRepeatPassword.editText?.text.toString()
            val login = binding.login.text.toString()
            if (pass == repeatPass && pass != "") {
                if (!availabilityPhoto) {
                    viewModel.reggistrationUser(login, pass, name)
                } else {
                    viewModel.registrationUserWithPhoto(login, pass, name, avatarImage!!)
                }
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(),
                    "\"Password\" and \"Repeat password\" fields do not match",
                    Toast.LENGTH_SHORT).show()
                binding.password.setBackgroundColor(Color.RED)
                binding.textRepeatPassword.setBackgroundColor(Color.RED)
            }
        }


        return binding.root
    }
}