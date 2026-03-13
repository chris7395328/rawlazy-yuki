package eu.kanade.tachiyomi.extension.all.rawlazy

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class RawLazy : ParsedHttpSource() {

    override val name = "RawLazy"

    override val baseUrl = "https://rawlazy.io"

    override val lang = "ja"

    override val supportsLatest = true

    override val headers: Headers by lazy {
        Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .add("Referer", baseUrl)
            .build()
    }

    override fun popularMangaSelector() = "div.top_sidebar div.entry"

    override fun popularMangaNextPageSelector(): String? = null

    override fun popularMangaRequest(page: Int): Request {
        return GET(baseUrl, headers)
    }

    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            title = element.select("h4.name a").text()
            setUrlWithoutDomain(element.select("h4.name a").attr("href"))
            thumbnail_url = element.select("img").attr("src")
        }
    }

    override fun latestUpdatesSelector() = "div.row-of-mangas div.entry-tag"

    override fun latestUpdatesNextPageSelector(): String? = null

    override fun latestUpdatesRequest(page: Int): Request {
        return GET(baseUrl, headers)
    }

    override fun latestUpdatesFromElement(element: Element): SManga {
        return SManga.create().apply {
            title = element.select("h2.name a").text()
            setUrlWithoutDomain(element.select("h2.name a").attr("href"))
            thumbnail_url = element.select("img").attr("src")
        }
    }

    override fun searchMangaSelector() = latestUpdatesSelector()

    override fun searchMangaNextPageSelector() = latestUpdatesNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/?s_manga=$query", headers)
    }

    override fun searchMangaFromElement(element: Element): SManga {
        return latestUpdatesFromElement(element)
    }

    override fun mangaDetailsParse(document: Document): SManga {
        return SManga.create().apply {
            title = document.select("h1.font-bold").text().ifEmpty {
                document.select("title").text().replace(" (Raw – Free)", "").replace(" | Manga Raw", "")
            }
            description = document.select("div.content-text").text().ifEmpty {
                document.select("meta[name=description]").attr("content")
            }
            genre = document.select("div.genres-wrap a").joinToString { it.text() }
            thumbnail_url = document.select("img.thumb").attr("src").ifEmpty {
                document.select("meta[property=og:image]").attr("content")
            }
        }
    }

    override fun chapterListSelector() = "div.chapters-list a"

    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            name = element.select("span").text().trim()
            setUrlWithoutDomain(element.attr("href"))
            date_upload = System.currentTimeMillis()
        }
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).reversed()
    }

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        
        try {
            val zingData = extractZingData(document)
            val chapterData = extractChapterData(document)
            
            if (zingData != null && chapterData != null) {
                pages.addAll(fetchImagesViaAjax(zingData, chapterData))
            }
            
            if (pages.isEmpty()) {
                document.select(".z_content img, article img, .entry-content img").forEachIndexed { i, img ->
                    val url = img.attr("src").ifEmpty { img.attr("data-src") }
                    if (url.isNotEmpty()) {
                        pages.add(Page(i, "", url))
                    }
                }
            }
        } catch (e: Exception) {
            document.select(".z_content img, article img, .entry-content img").forEachIndexed { i, img ->
                val url = img.attr("src").ifEmpty { img.attr("data-src") }
                if (url.isNotEmpty()) {
                    pages.add(Page(i, "", url))
                }
            }
        }
        
        return pages
    }

    private data class ZingData(
        val ajaxUrl: String,
        val nonce: String
    )

    private data class ChapterData(
        val p: String,
        val chapterId: String
    )

    private fun extractZingData(document: Document): ZingData? {
        val script = document.select("script:containsData(var zing)").firstOrNull()?.data() ?: return null
        
        val ajaxUrlPattern = Pattern.compile("\"ajax_url\":\"([^\"]+)\"")
        val noncePattern = Pattern.compile("\"nonce\":\"([^\"]+)\"")
        
        val ajaxUrlMatcher = ajaxUrlPattern.matcher(script)
        val nonceMatcher = noncePattern.matcher(script)
        
        if (ajaxUrlMatcher.find() && nonceMatcher.find()) {
            return ZingData(
                ajaxUrl = ajaxUrlMatcher.group(1),
                nonce = nonceMatcher.group(1)
            )
        }
        return null
    }

    private fun extractChapterData(document: Document): ChapterData? {
        val script = document.select("script:containsData(chapter_id)").firstOrNull()?.data() ?: return null
        
        val pPattern = Pattern.compile("p:\\s*(\\d+)")
        val chapterIdPattern = Pattern.compile("chapter_id:\\s*'([^']+)'")
        
        val pMatcher = pPattern.matcher(script)
        val chapterIdMatcher = chapterIdPattern.matcher(script)
        
        if (pMatcher.find() && chapterIdMatcher.find()) {
            return ChapterData(
                p = pMatcher.group(1),
                chapterId = chapterIdMatcher.group(1)
            )
        }
        return null
    }

    private fun fetchImagesViaAjax(zingData: ZingData, chapterData: ChapterData): List<Page> {
        val pages = mutableListOf<Page>()
        var imgIndex = 0
        var content = ""
        var going = 1
        var attempts = 0
        val maxAttempts = 10

        while (going == 1 && attempts < maxAttempts) {
            attempts++
            
            val formBody = FormBody.Builder()
                .add("action", "z_do_ajax")
                .add("_action", "decode_images")
                .add("p", chapterData.p)
                .add("chapter_id", chapterData.chapterId)
                .add("img_index", imgIndex.toString())
                .add("content", content)
                .add("nonce", zingData.nonce)
                .build()

            val request = POST(zingData.ajaxUrl, headers, formBody)
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) break

            val jsonString = response.body.string()
            val json = JSONObject(jsonString)

            if (json.has("mes")) {
                content += json.getString("mes")
            }

            going = if (json.has("going")) json.getInt("going") else 0
            imgIndex = if (json.has("img_index")) json.getInt("img_index") else imgIndex

            if (going != 1) break
        }

        if (content.isNotEmpty()) {
            val doc = Jsoup.parse(content)
            doc.select("img").forEachIndexed { i, img ->
                val url = img.attr("src").ifEmpty { img.attr("data-src") }
                if (url.isNotEmpty()) {
                    pages.add(Page(i, "", url))
                }
            }
        }

        return pages
    }

    override fun imageUrlParse(document: Document): String {
        throw UnsupportedOperationException("Not used")
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(baseUrl + manga.url, headers)
    }

    override fun pageListRequest(chapter: SChapter): Request {
        return GET(baseUrl + chapter.url, headers)
    }
}
