package eu.kanade.tachiyomi.extension.es.olympusscans.DTO

import kotlinx.serialization.Serializable

@Serializable
data class PayloadPagesDto(
    val chapter: PageDto,
)
