package eu.kanade.tachiyomi.extension.es.olympusscans

import eu.kanade.tachiyomi.extension.es.olympusscans.DTO.ChapterDto
import eu.kanade.tachiyomi.extension.es.olympusscans.DTO.PayloadChapterDto
import eu.kanade.tachiyomi.extension.es.olympusscans.DTO.PayloadMangaDto
import eu.kanade.tachiyomi.extension.es.olympusscans.DTO.PayloadPagesDto
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.injectLazy

class olympusscans : ParsedHttpSource() {

    override val baseUrl: String = "https://olympusscans.com"
    private val baseURLApi: String = "https://dashboard.olympusscans.com"
    override val lang: String = "es"
    override val name: String = "OlympusScans"
    override val supportsLatest: Boolean = true
    private val json: Json by injectLazy()

    // Search
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseURLApi/api/search".toHttpUrl().newBuilder()
        url.addQueryParameter("name", query)
        return GET(url.build().toString(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val jsonTemp = Json { ignoreUnknownKeys = true }
        val resultPayload = jsonTemp.decodeFromString<PayloadMangaDto>(response.body.string())
        val mangas = resultPayload.data.map {
            SManga.create().apply {
                setUrlWithoutDomain("$baseURLApi/${it.slug}")
                title = it.name
                thumbnail_url = it.cover
            }
        }
        return MangasPage(mangas, hasNextPage = false)
    }

    override fun searchMangaSelector(): String =
        throw UnsupportedOperationException("Not used.")

    override fun searchMangaFromElement(element: Element): SManga =
        throw UnsupportedOperationException("Not used.")

    override fun searchMangaNextPageSelector(): String? =
        null

    // Latest
    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException("Not used.")

    override fun latestUpdatesSelector(): String =
        ".mainpage-manga"

    override fun latestUpdatesFromElement(element: Element): SManga = throw Exception("Not used")

    override fun latestUpdatesNextPageSelector(): String? =
        null

    // Details
    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1")!!.text()
    }

    // Chapters

    override fun chapterListSelector() = "not using"

    override fun chapterFromElement(element: Element): SChapter = throw UnsupportedOperationException("Not used")

    override fun chapterListRequest(manga: SManga): Request {
        return paginatedChapterListRequest(manga.url, 1)
    }

    private fun paginatedChapterListRequest(mangaUrl: String, page: Int): Request {
        return GET(
            url = "$baseURLApi/api/series$mangaUrl/chapters?page=$page&direction=desc&type=comic",
            headers = headers,
        )
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val slug = response.request.url.toString().substringAfter("/api/series/").substringBefore("/chapters")
        var data = json.decodeFromString<PayloadChapterDto>(response.body.string())
        var resultSize = data.data.size
        var page = 2
        while (data.meta.total > resultSize) {
            val newRequest = paginatedChapterListRequest("/$slug", page)
            val newResponse = client.newCall(newRequest).execute()
            var newData = json.decodeFromString<PayloadChapterDto>(newResponse.body.string())
            data.data += newData.data
            resultSize += newData.data.size
            page += 1
        }
        return data.data.map { chap -> chapterFromObject(chap, slug) }
    }

    private fun chapterFromObject(chapter: ChapterDto, slug: String) = SChapter.create().apply {
        url = "/capitulo/${chapter.id}/comic-$slug"
        name = "Capitulo ${chapter.name}"
        chapter_number = chapter.name!!.toFloat()
    }
    // Pages

    override fun pageListParse(document: Document): List<Page> = throw UnsupportedOperationException("Not used")

    override fun pageListRequest(chapter: SChapter): Request {
        var id = chapter.url.toString().substringAfter("/capitulo/").substringBefore("/chapters").substringBefore("/comic")
        val slug = chapter.url.toString().substringAfter("comic-").substringBefore("/chapters").substringBefore("/comic")
        return GET("$baseURLApi/api/series/$slug/chapters/$id?type=comic")
    }

    override fun pageListParse(response: Response): List<Page> =
        json.decodeFromString<PayloadPagesDto>(response.body.string()).chapter.pages.mapIndexed { i, img ->
            Page(i, "", img)
        }

    override fun imageUrlParse(document: Document): String =
        throw UnsupportedOperationException("Not used.")

    // Other
    private fun String.toStatus(): String =
        throw UnsupportedOperationException("Not used.")

    // Popular
    override fun popularMangaRequest(page: Int): Request =
        throw UnsupportedOperationException("Not used.")

    override fun popularMangaSelector(): String =
        throw UnsupportedOperationException("Not used.")

    override fun popularMangaFromElement(element: Element): SManga =
        throw UnsupportedOperationException("Not used.")

    override fun popularMangaNextPageSelector(): String? =
        null
}
