package ru.netology.nikjob.dto

import ru.netology.nikjob.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)