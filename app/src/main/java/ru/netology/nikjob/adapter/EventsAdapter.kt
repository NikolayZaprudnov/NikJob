package ru.netology.nikjob.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.VideoView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nikjob.R
import ru.netology.nikjob.databinding.PostCardBinding
import ru.netology.nikjob.dto.Event
import ru.netology.nikjob.dto.FeedItem
import ru.netology.nikjob.viewmodel.EventViewHolder

interface EventOnInteractionListener {
    fun onLike(event: Event) {}
    fun onRepost(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEdit(event: Event) {}
    fun onOpenImage(event: Event)
    fun onPlay(event: Event, videoPlayer: VideoView, player: MediaPlayer)
    fun onOpen(event: Event)
    fun onLink(event: Event)
}

class EventsAdapter(
    private val eventOnInteractionListener: EventOnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(EventDiffCallBack()) {


    override fun getItemViewType(position: Int): Int = R.layout.post_card


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, eventOnInteractionListener)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val event = getItem(position)) {
            is Event -> (holder as? EventViewHolder)?.bind(event)
            null -> error("unknown item type")
            else -> {
                error("unknown item type")
            }
        }
    }



    class EventDiffCallBack : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }
    }
}