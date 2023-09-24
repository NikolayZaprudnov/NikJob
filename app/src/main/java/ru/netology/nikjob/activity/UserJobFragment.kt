package ru.netology.nikjob.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nikjob.R
import ru.netology.nikjob.adapter.JobAdapter
import ru.netology.nikjob.adapter.OnJobInteractionListener
import ru.netology.nikjob.databinding.FragmentUserJobBinding
import ru.netology.nikjob.dialog.removeJobDialog
import ru.netology.nikjob.dto.Job
import ru.netology.nikjob.viewmodel.UserJobViewModel

class UserJobFragment : Fragment(), removeJobDialog.DialogListener {
    private val viewModel: UserJobViewModel by activityViewModels()
    private var deleteOrNot: Boolean = false

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
        val dialog = removeJobDialog()
        val manager = parentFragmentManager
        val authorId = arguments!!.getLong("authorId")
        val userId = arguments!!.getLong("userId")

        val adapter = JobAdapter(object : OnJobInteractionListener {
            override fun onLink(job: Job) {
                val uriPost = "https://" + job.link
                try {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriPost))
                    startActivity(linkIntent)
                } catch (e: Exception){ }
            }

            override fun onDelete(job: Job) {
                if (userId != authorId) {
                    Toast.makeText(requireContext(),
                        getString(R.string.cant_remote_job),
                        Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.deleteJob(job.id)
                    viewModel.loadJobData(authorId)
                }
            }
        })

        viewModel.loadJobData(authorId)
        if (userId != authorId) binding.newJob.visibility = View.GONE

        binding.authorJobName.setText(arguments?.getString("authorName"))
        val authorAvatar = arguments?.getString("authorAvatar")
        Glide.with(binding.authorAvatar)
            .load(authorAvatar)
            .fitCenter()
            .placeholder(R.drawable.ic_baseline_load_face_100)
            .error(R.drawable.ic_baseline_error_100)
            .timeout(10_000)
            .into(binding.authorAvatar)


        binding.list.adapter = adapter
        viewModel.jobs.observe(viewLifecycleOwner) { job ->
            adapter.submitList(job)
        }
        binding.newJob.setOnClickListener {
            findNavController().navigate(R.id.action_userJobFragment_to_newJobFragment)
            viewModel.loadJobData(userId)
        }
        return binding.root
    }

    override fun ODialogPositiveClick(data: Boolean) {
        deleteOrNot = true
    }

}