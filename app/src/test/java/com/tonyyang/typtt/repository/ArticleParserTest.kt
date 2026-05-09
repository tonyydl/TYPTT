package com.tonyyang.typtt.repository

import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.data.PushType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleParserTest {

    private fun html(body: String) = """
        <html><body><div id="main-content">
        <div class="article-metaline"><span class="article-meta-tag">作者</span> <span class="article-meta-value">testuser (Test)</span></div>
        <div class="article-metaline-right"><span class="article-meta-tag">看板</span> <span class="article-meta-value">Gossiping</span></div>
        <div class="article-metaline"><span class="article-meta-tag">標題</span> <span class="article-meta-value">[問卦] Test Title</span></div>
        <div class="article-metaline"><span class="article-meta-tag">時間</span> <span class="article-meta-value">Fri May  9 12:00:00 2025</span></div>
        $body
        </div></body></html>
    """.trimIndent()

    @Test
    fun `parse returns header as first element with correct fields`() {
        val elements = ArticleParser.parse(html(""))
        val header = elements.first() as ArticleElement.Header
        assertEquals("testuser (Test)", header.author)
        assertEquals("[問卦] Test Title", header.title)
        assertEquals("Gossiping", header.board)
        // PTT uses double-space before single-digit days; Jsoup .text() normalises to single space
        assertEquals("Fri May 9 12:00:00 2025", header.date)
    }

    @Test
    fun `parse plain text becomes TextBlock`() {
        val elements = ArticleParser.parse(html("Hello world"))
        assertTrue(elements.any { it is ArticleElement.TextBlock && it.text.contains("Hello world") })
    }

    @Test
    fun `parse line starting with colon becomes QuoteBlock`() {
        val elements = ArticleParser.parse(html(": This is a quote"))
        val quote = elements.filterIsInstance<ArticleElement.QuoteBlock>().first()
        assertEquals("This is a quote", quote.text)
    }

    @Test
    fun `parse imgur url becomes ImageBlock`() {
        val elements = ArticleParser.parse(html("https://i.imgur.com/abc123.jpg"))
        val image = elements.filterIsInstance<ArticleElement.ImageBlock>().first()
        assertEquals("https://i.imgur.com/abc123.jpg", image.url)
    }

    @Test
    fun `parse direct jpg url becomes ImageBlock`() {
        val elements = ArticleParser.parse(html("https://example.com/photo.jpg"))
        assertTrue(elements.any { it is ArticleElement.ImageBlock })
    }

    @Test
    fun `parse double dash becomes Divider`() {
        val elements = ArticleParser.parse(html("--"))
        assertTrue(elements.any { it is ArticleElement.Divider })
    }

    @Test
    fun `parse push tag 推 produces PUSH type with correct fields`() {
        val pushHtml = """<div class="push"><span class="push-tag">推 </span><span class="push-userid">user1</span><span class="push-content">: Good!</span><span class="push-ipdatetime"> 05/09 12:01</span></div>"""
        val elements = ArticleParser.parse(html(pushHtml))
        val push = elements.filterIsInstance<ArticleElement.Push>().first()
        assertEquals(PushType.PUSH, push.type)
        assertEquals("user1", push.user)
        assertEquals("Good!", push.content)
        assertEquals("05/09 12:01", push.date)
    }

    @Test
    fun `parse push tag 噓 produces BOO type`() {
        val pushHtml = """<div class="push"><span class="push-tag">噓 </span><span class="push-userid">u</span><span class="push-content">: x</span><span class="push-ipdatetime"> 05/09</span></div>"""
        val push = ArticleParser.parse(html(pushHtml)).filterIsInstance<ArticleElement.Push>().first()
        assertEquals(PushType.BOO, push.type)
    }

    @Test
    fun `parse push tag arrow produces NEUTRAL type`() {
        val pushHtml = """<div class="push"><span class="push-tag">→  </span><span class="push-userid">u</span><span class="push-content">: x</span><span class="push-ipdatetime"> 05/09</span></div>"""
        val push = ArticleParser.parse(html(pushHtml)).filterIsInstance<ArticleElement.Push>().first()
        assertEquals(PushType.NEUTRAL, push.type)
    }

    @Test
    fun `parse returns empty list when main-content missing`() {
        assertTrue(ArticleParser.parse("<html><body></body></html>").isEmpty())
    }
}
