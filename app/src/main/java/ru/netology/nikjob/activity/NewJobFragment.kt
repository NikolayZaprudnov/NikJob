package ru.netology.nikjob.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nikjob.databinding.FragmentNewJobBinding
import ru.netology.nikjob.dto.Job
import ru.netology.nikjob.viewmodel.UserJobViewModel

class NewJobFragment : Fragment() {

    private val viewModel: UserJobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )
        binding.textNameJob.requestFocus()
        binding.newpost.setOnClickListener {
            val newJob = Job(
                name = binding.textNameJob.text.toString(),
                position = binding.textPositionJob.text.toString(),
                start = binding.textStartJob.text.toString(),
                finish = binding.textFinishJob.text.toString(),
                link = binding.textLinkJob.text.toString(),
                id = 0
            )
            viewModel.createJob(newJob)
            findNavController().navigateUp()
        }
        return binding.root
    }
}