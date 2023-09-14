package ru.netology.nikjob.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nikjob.dto.Attachment
import ru.netology.nikjob.dto.Coordinates
import ru.netology.nikjob.dto.Post
import ru.netology.nikjob.enumeration.AttachmentType
import util.Convertation

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val likedByMe: Boolean = false,
    val likeOwnerIds: String?,

    @Embedded
    val coords: CoordinatesEmbeddable?,
    val link: String? = null,
    val sharedByMe: Boolean = false,
    val countShared: Int = 999,
    val mentionIds: String?,
    val mentionedMe: Boolean = false,

    @Embedded
    var attachment: AttachmentEmbeddable?,
    val hidden: Boolean = false,
    var likes: Int = 0,
) {
    fun toDto() = Post(id,
        authorId,
        author,
        authorAvatar,
        content,
        published,
        likedByMe,
        Convertation.toListDto(likeOwnerIds),
        coords?.toDto(),
        link,
        sharedByMe,
        countShared,
        Convertation.toListDto(mentionIds),
        mentionedMe,
        attachment?.toDto()
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                Convertation.fromListDto(dto.likeOwnerIds),
                CoordinatesEmbeddable.fromDto(dto.coords),
                dto.link,
                dto.sharedByMe,
                dto.countShared,
                Convertation.fromListDto(dto.mentionIds),
                dto.mentionedMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
            )
    }

}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

data class CoordinatesEmbeddable(
    val latitude: String?,
    val longitude: String?,
) {
    fun toDto() =
        Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

