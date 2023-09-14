package ru.netology.nikjob.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.databinding.FragmentAuthBinding
import ru.netology.nikjob.viewmodel.AuthViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentAuthBinding.inflate(
            inflater,
            container,
            false
        )
        binding.login.requestFocus()
        binding.buttonSignIn.setOnClickListener {
            viewModel.updateUser(binding.login.text.toString(), binding.password.text.toString())
            findNavController().navigateUp()
        }


        return binding.root
    }
}
