package ru.netology.nikjob.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nikjob.R
import ru.netology.nikjob.adapter.JobAdapter
import ru.netology.nikjob.adapter.OnJobInteractionListener
import ru.netology.nikjob.databinding.FragmentUserJobBinding
import ru.netology.nikjob.dto.Job
import ru.netology.nikjob.viewmodel.AuthViewModel
import ru.netology.nikjob.viewmodel.UserJobViewModel

class UserJobFragment : Fragment() {
    private val viewModel: UserJobViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentUserJobBinding.inflate(
            inflater,
            container,
            false
        )
        val adapter = JobAdapter(object : OnJobInteractionListener {
            override fun onLink(job: Job) {
                val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(job.link))
                startActivity(linkIntent)
            }

            override fun onDelete(job: Job) {
                viewModel.deleteJob(job.id)
            }
        })

        viewModel.loadJobData(arguments!!.getLong("authorId"))

        binding.list.adapter = adapter
        viewModel.jobs.observe(viewLifecycleOwner) { job ->
            adapter.submitList(job)
        }
        binding.newJob.isVisible = authViewModel.authorized
        binding.newJob.setOnClickListener {
            findNavController().navigate(R.id.action_userJobFragment_to_newJobFragment)
        }
        return binding.root
    }

}