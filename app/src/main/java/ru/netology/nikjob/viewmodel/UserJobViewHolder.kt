package ru.netology.nikjob.viewmodel


import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nikjob.adapter.OnJobInteractionListener
import ru.netology.nikjob.databinding.JobCardBinding
import ru.netology.nikjob.dto.Job


class UserJobViewHolder(
    private val binding: JobCardBinding,
    private val onJobInteractionListener: OnJobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            nameJob.text = job.name
            start.text = job.start
            finish.text = job.finish
            jobLink.text = job.link

            jobLink.setOnClickListener {
                onJobInteractionListener.onLink(job)
            }
            removeJob.isVisible = job.ownedByMe
            removeJob.setOnClickListener {
                onJobInteractionListener.onDelete(job)
            }

        }
    }

}