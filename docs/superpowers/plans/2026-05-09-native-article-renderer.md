# Native Article Renderer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `AndroidView { WebView }` in `ArticleScreen` with a fully native Jetpack Compose renderer that parses PTT article HTML via Jsoup and displays header, body, images, and push comments as typed Compose elements.

**Architecture:** `ArticleRepository` fetches the article HTML (with over-18 cookies via Jsoup) and delegates to a pure `ArticleParser` object that converts HTML into `List<ArticleElement>`. `ArticleViewModel` exposes this list via `StateFlow<ArticleUiState>`. `ArticleScreen` renders a `LazyColumn` switching on element type — one dedicated composable per type.

**Tech Stack:** Jsoup (existing), Coil 2.6.0 (new — inline image loading via `AsyncImage`), Jetpack Compose Material 3 (existing)

---

## File Map

| File | Status | Responsibility |
|---|---|---|
| `data/ArticleElement.kt` | **New** | Sealed class + `PushType` enum |
| `repository/ArticleParser.kt` | **New** | Pure HTML → `List<ArticleElement>` parser |
| `repository/ArticleRepository.kt` | Modify | Replace `getArticleCookies` with `getArticleContent` |
| `ui/article/ArticleViewModel.kt` | Modify | Rename `loadCookies` → `loadArticle`, update `ArticleUiState` |
| `ui/article/ArticleHeaderItem.kt` | **New** | Header composable |
| `ui/article/ArticleTextItem.kt` | **New** | `TextBlock` composable with `SelectionContainer` |
| `ui/article/ArticleQuoteItem.kt` | **New** | `QuoteBlock` composable with left-border indicator |
| `ui/article/ArticleImageItem.kt` | **New** | `ImageBlock` composable via Coil `AsyncImage` |
| `ui/article/ArticlePushItem.kt` | **New** | `Push` composable (推/噓/→) |
| `ui/article/ArticleScreen.kt` | Modify | Replace WebView with `LazyColumn` of element composables |
| `gradle/libs.versions.toml` | Modify | Add Coil version + library alias |
| `app/build.gradle.kts` | Modify | Add Coil dependency |
| `app/src/test/.../ArticleParserTest.kt` | **New** | Unit tests for parser (TDD) |

---

### Task 1: Add ArticleElement Data Model

**Files:**
- Create: `app/src/main/java/com/tonyyang/typtt/data/ArticleElement.kt`

- [ ] **Step 1: Create the sealed class**

```kotlin
package com.tonyyang.typtt.data

sealed class ArticleElement {
    data class Header(
        val author: String,
        val title: String,
        val board: String,
        val date: String
    ) : ArticleElement()

    data class TextBlock(val text: String) : ArticleElement()
    data class QuoteBlock(val text: String) : ArticleElement()
    data class ImageBlock(val url: String) : ArticleElement()
    object Divider : ArticleElement()

    data class Push(
        val type: PushType,
        val user: String,
        val content: String,
        val date: String
    ) : ArticleElement()
}

enum class PushType { PUSH, BOO, NEUTRAL }
```

- [ ] **Step 2: Build to verify no compile errors**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tonyyang/typtt/data/ArticleElement.kt
git commit -m "feat: add ArticleElement sealed class and PushType enum"
```

---

### Task 2: Add Coil Dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Coil to version catalog**

In `gradle/libs.versions.toml`, under `[versions]` add:
```toml
coil = "2.6.0"
```

Under `[libraries]` add:
```toml
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

- [ ] **Step 2: Add to app dependencies**

In `app/build.gradle.kts` inside the `dependencies { }` block, add:
```kotlin
implementation(libs.coil.compose)
```

- [ ] **Step 3: Sync and verify**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat: add Coil 2.6.0 dependency for inline image loading"
```

---

### Task 3: ArticleParser (TDD)

**Files:**
- Create: `app/src/test/java/com/tonyyang/typtt/repository/ArticleParserTest.kt`
- Create: `app/src/main/java/com/tonyyang/typtt/repository/ArticleParser.kt`

PTT article HTML structure (for reference):
- `#main-content` — root container
- `.article-metaline` — 作者, 標題, 時間
- `.article-metaline-right` — 看板
- `.article-meta-tag` — label span inside metaline
- `.article-meta-value` — value span inside metaline
- `.push` — one per push comment, contains `.push-tag`, `.push-userid`, `.push-content`, `.push-ipdatetime`

- [ ] **Step 1: Write the failing tests**

```kotlin
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
        assertEquals("Fri May  9 12:00:00 2025", header.date)
    }

    @Test
    fun `parse plain text becomes TextBlock`() {
        val elements = ArticleParser.parse(html("Hello world"))
        assertTrue(elements.any { it is ArticleElement.TextBlock && it.text.contains("Hello world") })
    }

    @Test
    fun `parse line starting with colon becomes QuoteBlock`() {
        val elements = ArticleParser.parse(html(": This is a quote"))
        assertTrue(elements.any { it is ArticleElement.QuoteBlock })
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
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./gradlew :app:test --tests "com.tonyyang.typtt.repository.ArticleParserTest" 2>&1 | tail -10
```
Expected: FAILED — `Unresolved reference: ArticleParser`

- [ ] **Step 3: Implement ArticleParser**

```kotlin
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
                    result.add(ArticleElement.QuoteBlock(trimmed))
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
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew :app:test --tests "com.tonyyang.typtt.repository.ArticleParserTest" 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`, all 9 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/tonyyang/typtt/repository/ArticleParserTest.kt \
        app/src/main/java/com/tonyyang/typtt/repository/ArticleParser.kt
git commit -m "feat: add ArticleParser with TDD — HTML to List<ArticleElement>"
```

---

### Task 4: Update ArticleRepository

**Files:**
- Modify: `app/src/main/java/com/tonyyang/typtt/repository/ArticleRepository.kt`

- [ ] **Step 1: Replace getArticleCookies with getArticleContent**

Replace the entire file:

```kotlin
package com.tonyyang.typtt.repository

import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.data.ArticleElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

object ArticleRepository {

    suspend fun getArticleContent(articleUrl: String): List<ArticleElement> =
        withContext(Dispatchers.IO) {
            val cookies = Jsoup.connect(articleUrl)
                .method(Connection.Method.GET)
                .execute()
                .cookies()
                .apply { this["over18"] = "1" }

            val html = Jsoup.connect(articleUrl)
                .data("from", articleUrl.removePrefix(BuildConfig.BASE_URL))
                .data("yes", "yes")
                .cookies(cookies)
                .post()
                .outerHtml()

            ArticleParser.parse(html)
        }
}
```

- [ ] **Step 2: Build to verify**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL` (ArticleScreen / ArticleViewModel will have compile errors — fixed in Task 5)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tonyyang/typtt/repository/ArticleRepository.kt
git commit -m "feat: update ArticleRepository — fetch HTML and parse to ArticleElement list"
```

---

### Task 5: Update ArticleViewModel and ArticleUiState

**Files:**
- Modify: `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleViewModel.kt`

- [ ] **Step 1: Replace entire file**

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ArticleUiState(
    val elements: List<ArticleElement> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ArticleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun loadArticle(articleUrl: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { ArticleRepository.getArticleContent(articleUrl) }
                .onSuccess { elements ->
                    _uiState.update { it.copy(elements = elements, isLoading = false) }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load article $articleUrl")
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
```

- [ ] **Step 2: Build to verify**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL` (ArticleScreen still calls old `loadCookies` — fixed in Task 7)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/tonyyang/typtt/ui/article/ArticleViewModel.kt
git commit -m "feat: update ArticleViewModel — loadArticle replaces loadCookies, state holds elements"
```

---

### Task 6: Create UI Item Composables

**Files (all new):**
- `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleHeaderItem.kt`
- `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleTextItem.kt`
- `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleQuoteItem.kt`
- `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleImageItem.kt`
- `app/src/main/java/com/tonyyang/typtt/ui/article/ArticlePushItem.kt`

- [ ] **Step 1: Create ArticleHeaderItem.kt**

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextPrimary
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleHeaderItem(header: ArticleElement.Header, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (header.author.isNotEmpty()) {
            Text(text = "作者", color = TextSecondary, fontSize = 11.sp)
            Text(text = header.author, color = TextPrimary, fontSize = 14.sp)
        }
        if (header.title.isNotEmpty()) {
            Text(text = "標題", color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp))
            Text(text = header.title, color = TextPrimary, fontSize = 14.sp)
        }
        if (header.date.isNotEmpty()) {
            Text(text = "時間", color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp))
            Text(text = header.date, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
```

- [ ] **Step 2: Create ArticleTextItem.kt**

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextPrimary

@Composable
fun ArticleTextItem(block: ArticleElement.TextBlock, modifier: Modifier = Modifier) {
    SelectionContainer(modifier = modifier) {
        Text(
            text = block.text,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}
```

- [ ] **Step 3: Create ArticleQuoteItem.kt**

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.Primary
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleQuoteItem(block: ArticleElement.QuoteBlock, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Primary.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Primary)
        )
        Text(
            text = block.text,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
```

- [ ] **Step 4: Create ArticleImageItem.kt**

```kotlin
package com.tonyyang.typtt.ui.article

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.tonyyang.typtt.data.ArticleElement

@Composable
fun ArticleImageItem(block: ArticleElement.ImageBlock, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AsyncImage(
        model = block.url,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(block.url))
                )
            }
    )
}
```

- [ ] **Step 5: Create ArticlePushItem.kt**

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.data.PushType
import com.tonyyang.typtt.ui.theme.TextPrimary
import com.tonyyang.typtt.ui.theme.TextSecondary

private val PushBlue = Color(0xFF4A9EFF)
private val BooRed = Color(0xFFFF4444)
private val NeutralGray = Color(0xFF888888)

@Composable
fun ArticlePushItem(push: ArticleElement.Push, modifier: Modifier = Modifier) {
    val (tagText, tagColor) = when (push.type) {
        PushType.PUSH -> "推" to PushBlue
        PushType.BOO -> "噓" to BooRed
        PushType.NEUTRAL -> "→" to NeutralGray
    }
    Row(modifier = modifier.padding(vertical = 2.dp)) {
        Text(text = tagText, color = tagColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = push.user, color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = push.content, color = TextPrimary, fontSize = 13.sp,
            modifier = Modifier.weight(1f))
        Text(text = push.date, color = TextSecondary, fontSize = 11.sp)
    }
}
```

- [ ] **Step 6: Build to verify no compile errors**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/tonyyang/typtt/ui/article/ArticleHeaderItem.kt \
        app/src/main/java/com/tonyyang/typtt/ui/article/ArticleTextItem.kt \
        app/src/main/java/com/tonyyang/typtt/ui/article/ArticleQuoteItem.kt \
        app/src/main/java/com/tonyyang/typtt/ui/article/ArticleImageItem.kt \
        app/src/main/java/com/tonyyang/typtt/ui/article/ArticlePushItem.kt
git commit -m "feat: add article element composables — header, text, quote, image, push"
```

---

### Task 7: Update ArticleScreen

**Files:**
- Modify: `app/src/main/java/com/tonyyang/typtt/ui/article/ArticleScreen.kt`

- [ ] **Step 1: Replace WebView with native LazyColumn**

Replace the entire file:

```kotlin
package com.tonyyang.typtt.ui.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.data.ArticleElement
import com.tonyyang.typtt.ui.theme.TextSecondary

@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    articleUrl: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(articleUrl) {
        viewModel.loadArticle(articleUrl)
    }

    when {
        uiState.isLoading -> Box(
            modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }

        uiState.errorMessage != null -> Box(
            modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        ) {
            Button(onClick = { viewModel.loadArticle(articleUrl) }) {
                Text("重試")
            }
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(
                items = uiState.elements,
                key = { index, _ -> index }
            ) { _, element ->
                when (element) {
                    is ArticleElement.Header ->
                        ArticleHeaderItem(element)
                    is ArticleElement.TextBlock ->
                        ArticleTextItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.QuoteBlock ->
                        ArticleQuoteItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.ImageBlock ->
                        ArticleImageItem(element, modifier = Modifier.padding(vertical = 4.dp))
                    is ArticleElement.Divider ->
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = TextSecondary.copy(alpha = 0.3f)
                        )
                    is ArticleElement.Push ->
                        ArticlePushItem(element)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build full project**

```bash
./gradlew assembleDebug 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run all unit tests**

```bash
./gradlew :app:test 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`, all tests pass

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/tonyyang/typtt/ui/article/ArticleScreen.kt
git commit -m "feat: replace WebView with native Compose article renderer"
```

---

## Notes

- `ArticleParser.wholeText()` preserves whitespace from PTT's preformatted content. If PTT HTML uses `<br>` tags instead of newlines in some articles, the parser may need to process child nodes individually — adjust if body text appears collapsed.
- The `IMAGE_REGEX` and `DIVIDER_REGEX` patterns in `ArticleParser` may need tuning based on real article content encountered during manual testing.
- Push comment colors (`PushBlue`, `BooRed`) in `ArticlePushItem` use raw hex values intentionally — they are push-specific and not part of the general app theme system.
