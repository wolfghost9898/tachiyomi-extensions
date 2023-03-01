package eu.kanade.tachiyomi.extension.es.olympusscans.DTO
import kotlinx.serialization.Serializable
@Serializable
data class MangaDto(
    val id: Int,
    val name: String,
    val slug: String?,
    val cover: String?,
    val type: String?,
)
