package eu.kanade.tachiyomi.extension.es.olympusscans.DTO
import kotlinx.serialization.Serializable
@Serializable
data class PageDto(
    val pages: List<String>,
)
