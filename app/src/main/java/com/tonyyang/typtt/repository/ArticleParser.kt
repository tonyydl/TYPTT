package com.tonyyang.typtt.repository

import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.data.PushType
import org.jsoup.Jsoup

object ArticleParser {

    private val IMAGE_REGEX = Regex(
        """https?://.*\.(jpg|jpeg|png|gif|webp)(\?.*)?|https?://i\.imgur\.com/.*""",
        RegexOption.IGNORE_CASE
    )
    private val DIVIDER_REGEX = Regex("""^[-─━═]{2,}$""")

    fun parse(html: String): List<ArticleElement> {
        val doc = Jsoup.parse(html)
        val main = doc.getElementById("main-content") ?: return emptyList()

        val result = mutableListOf<ArticleElement>()

        // Parse header: match by label text so order doesn't matter
        val metalines = main.select(".article-metaline, .article-metaline-right")
        fun metaValue(label: String) = metalines
            .firstOrNull { it.select(".article-meta-tag").text() == label }
            ?.select(".article-meta-value")?.text().orEmpty()

        result.add(
            ArticleElement.Header(
                author = metaValue("作者"),
                title  = metaValue("標題"),
                board  = metaValue("看板"),
                date   = metaValue("時間")
            )
        )

        // Save push nodes before removal
        val pushNodes = main.select(".push").toList()

        // Strip header + push nodes, leaving only article body
        main.select(".article-metaline, .article-metaline-right, .push").remove()

        // Parse body line by line; merge consecutive plain-text lines into one TextBlock
        val textBuffer = StringBuilder()

        fun flushText() {
            val text = textBuffer.toString().trim()
            if (text.isNotEmpty()) result.add(ArticleElement.TextBlock(text))
            textBuffer.clear()
        }

        for (line in main.wholeText().lines()) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith(":") || trimmed.startsWith("：") -> {
                    flushText()
                    result.add(ArticleElement.QuoteBlock(trimmed.removePrefix(":").removePrefix("：").trim()))
                }
                IMAGE_REGEX.matches(trimmed) -> {
                    flushText()
                    result.add(ArticleElement.ImageBlock(trimmed))
                }
                DIVIDER_REGEX.matches(trimmed) -> {
                    flushText()
                    result.add(ArticleElement.Divider)
                }
                else -> {
                    if (textBuffer.isNotEmpty()) textBuffer.append("\n")
                    textBuffer.append(line)
                }
            }
        }
        flushText()

        // Parse push comments
        for (el in pushNodes) {
            val tag = el.select(".push-tag").text().trim()
            val user = el.select(".push-userid").text().trim()
            val content = el.select(".push-content").text().trim().removePrefix(":").trim()
            val date = el.select(".push-ipdatetime").text().trim()
            val type = when {
                tag.startsWith("推") -> PushType.PUSH
                tag.startsWith("噓") -> PushType.BOO
                else -> PushType.NEUTRAL
            }
            result.add(ArticleElement.Push(type = type, user = user, content = content, date = date))
        }

        return result
    }
}
