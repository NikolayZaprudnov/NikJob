package ru.netology.nikjob.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nikjob.dto.Job
import ru.netology.nikjob.databinding.JobCardBinding
import ru.netology.nikjob.viewmodel.UserJobViewHolder

interface OnJobInteractionListener{
    fun onLink(job: Job){}
}

class JobAdapter(
    private val onJobInteractionListener: OnJobInteractionListener
): ListAdapter<Job, UserJobViewHolder>(JobDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserJobViewHolder {
        val binding = JobCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserJobViewHolder(binding, onJobInteractionListener)
    }

    override fun onBindViewHolder(holder: UserJobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }


}
class JobDiffCallBack: DiffUtil.ItemCallback<Job>(){
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return  oldItem == newItem
    }
}