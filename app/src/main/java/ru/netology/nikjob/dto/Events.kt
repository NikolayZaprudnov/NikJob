package ru.netology.nikjob.dto

data class Event (
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: String,
    val likeOwnerIds : List<Long>? = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long>? = emptyList(),
    val participantsIds: List<Long>? = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,

): FeedItem