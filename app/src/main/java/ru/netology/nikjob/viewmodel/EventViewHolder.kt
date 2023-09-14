package ru.netology.nikjob.viewmodel

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.media.MediaPlayer
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nikjob.R
import ru.netology.nikjob.adapter.EventOnInteractionListener
import ru.netology.nikjob.databinding.PostCardBinding
import ru.netology.nikjob.dto.Event
import ru.netology.nikjob.enumeration.AttachmentType

class EventViewHolder(
    private val binding: PostCardBinding,
    private val eventOnInteractionListener: EventOnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
    private val player = MediaPlayer()
    fun bind(post: Event) {
        binding.apply {
            authorName.text = post.author
            Glide.with(avatar)
                .load(post.authorAvatar)
                .fitCenter()
                .placeholder(R.drawable.ic_baseline_load_face_100)
                .error(R.drawable.ic_baseline_error_100)
                .timeout(10_000)
                .into(avatar)
            time.text = post.published.toString()
            content.text = post.content
            likes.isChecked = post.likedByMe
            likes.text = converter(post.likeOwnerIds!!.size)
            if (post.link != null) {
                url.visibility = View.VISIBLE
                url.text = post.link
            }
            if (post.attachment != null) {
                when (post.attachment.type) {
                    AttachmentType.IMAGE -> {
                        attachmentImage.visibility = View.VISIBLE
                        Glide.with(attachmentImage)
                            .load(post.attachment.url)
                            .fitCenter()
                            .timeout(10_000)
                            .error(R.drawable.ic_baseline_error_100)
                            .into(attachmentImage)
                    }
                    AttachmentType.VIDEO -> {
                        attachmentVideo.visibility = View.VISIBLE
                    }

                    AttachmentType.AUDIO -> {
                        attachmentAudio.visibility = View.VISIBLE
                    }
                }
            }


            root.setOnClickListener {
                eventOnInteractionListener.onOpen(post)
            }
            likes.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.5F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.5F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                    repeatCount = 100
                    interpolator = BounceInterpolator()
                }.start()
                eventOnInteractionListener.onLike(post)
            }

            reposts.setOnClickListener {
                eventOnInteractionListener.onRepost(post)

            }
            attachmentImage.setOnClickListener {
                eventOnInteractionListener.onOpenImage(post)
            }
            attachmentVideo.setOnClickListener {
                eventOnInteractionListener.onPlay(post, attachmentVideo, player)
            }
            attachmentAudio.setOnClickListener {
                eventOnInteractionListener.onPlay(post, attachmentVideo, player)
            }
            url.setOnClickListener {
                eventOnInteractionListener.onLink(post)
            }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.option_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                eventOnInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                eventOnInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }


    fun converter(amount: Int): String {
        val convert = when (amount) {
            in 0..999 -> amount.toString()
            in 1000..999999 -> ((amount / 1000).toString() + "k")
            else -> ((amount / 1000000).toString() + "M")
        }
        return convert
    }

}