package eu.kanade.tachiyomi.extension.es.olympusscans.DTO

import kotlinx.serialization.Serializable

@Serializable
data class PayloadChapterDto(
    var data: List<ChapterDto>,
    val meta: MetaDto,
)
