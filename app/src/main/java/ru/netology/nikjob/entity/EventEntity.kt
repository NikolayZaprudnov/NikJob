package ru.netology.nikjob.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nikjob.dto.Coordinates
import ru.netology.nikjob.dto.Event
import util.Convertation


@Entity
class EventsEntity(
    @PrimaryKey
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: String,
    val likeOwnerIds: String?,
    val likedByMe: Boolean = false,
    val speakerIds: String?,
    val participantsIds: String?,
    val participatedByMe: Boolean = false,

    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,


    ) {

    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        coords,
        type,
        Convertation.toListDto(likeOwnerIds),
        likedByMe,
        Convertation.toListDto(speakerIds),
        Convertation.toListDto(participantsIds),
        participatedByMe,
        attachment?.toDto(),
        link,
        ownedByMe
    )

    companion object {
        fun fromDto(dto: Event) =
            EventsEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                dto.coords,
                dto.type,
                Convertation.fromListDto(dto.likeOwnerIds),
                dto.likedByMe,
                Convertation.fromListDto(dto.speakerIds),
                Convertation.fromListDto(dto.participantsIds),
                dto.participatedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.link,
                dto.ownedByMe
            )
    }
}

fun List<EventsEntity>.toDto(): List<Event> = map(EventsEntity::toDto)
fun List<Event>.toEntity(): List<EventsEntity> = map(EventsEntity::fromDto)