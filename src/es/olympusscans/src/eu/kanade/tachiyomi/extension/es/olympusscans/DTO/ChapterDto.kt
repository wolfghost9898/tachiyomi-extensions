package eu.kanade.tachiyomi.extension.es.olympusscans.DTO
import kotlinx.serialization.Serializable
@Serializable
data class ChapterDto(
    val id: Int,
    val name: String,
)
