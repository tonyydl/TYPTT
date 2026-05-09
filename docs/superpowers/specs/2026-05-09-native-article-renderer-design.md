# Native Article Renderer Design

**Date:** 2026-05-09
**Branch:** feature/native-article-renderer
**Goal:** Replace `AndroidView { WebView }` in `ArticleScreen` with a fully native Jetpack Compose renderer for PTT articles.

## Motivation

All four motivations apply:
- **UX consistency** — WebView scrolling, gestures, and fonts are inconsistent with the rest of the app
- **Dark mode control** — WebView cannot fully apply the app's dark theme
- **Performance** — Eliminate WebView initialization overhead and memory cost
- **Feature extensibility** — Enable long-press copy, image preview, and other interactions impossible in WebView

## Scope

All four article blocks will be rendered natively:
1. Article header (author, title, board, date)
2. Article body (text, quotes, dividers)
3. Inline images (via Coil)
4. Push/comment section

## Architecture

### Data Model

New file `data/ArticleElement.kt`:

```kotlin
sealed class ArticleElement {
    data class Header(
        val author: String,
        val title: String,
        val board: String,  // passed from nav args, not parsed from HTML
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

### ArticleUiState (modified)

```kotlin
data class ArticleUiState(
    val elements: List<ArticleElement> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
// cookies field removed — handled internally by Repository
```

### Data Flow

```
ArticleScreen
  → collectAsStateWithLifecycle
ArticleViewModel
  → loadArticle(url)
ArticleRepository
  → fetch cookies + HTML via Jsoup
  → ArticleParser.parse(html)
  → List<ArticleElement>
```

## Parsing Logic

New file `repository/ArticleParser.kt` — single responsibility: HTML → `List<ArticleElement>`.

**PTT HTML targets (Jsoup selectors):**
- `#main-content` — root container
- `.article-metaline` — header metadata (3 elements: author, title, date)
- `.push` — push comments

**Body parsing algorithm:**
1. Extract `.article-metaline` nodes → `Header`
2. Remove header nodes and push nodes from DOM
3. Get remaining text content, split by line, classify each line:
   - Starts with `>` → `QuoteBlock`
   - Matches image URL regex → `ImageBlock`
   - Matches `--` or `─` divider pattern → `Divider`
   - Otherwise → `TextBlock` (consecutive text lines merged into one block; merge resets on any non-text element)
4. Parse `.push` nodes → `Push` (push-tag / push-userid / push-content / push-date)

**Image URL regex:**
```
https?://.*\.(jpg|jpeg|png|gif|webp)(\?.*)?$
https?://i\.imgur\.com/.*
```

### ArticleRepository (modified)

```kotlin
suspend fun getArticleContent(url: String): List<ArticleElement> =
    withContext(Dispatchers.IO) {
        val cookies = fetchCookies(url)       // existing logic
        val html = fetchHtml(url, cookies)    // new: fetch with cookies
        ArticleParser.parse(html)
    }
```

## Compose UI Components

`ArticleScreen` replaces `AndroidView { WebView }` with:

```kotlin
LazyColumn {
    items(elements, key = { index }) { element ->
        when (element) {
            is Header    -> ArticleHeaderItem(element)
            is TextBlock -> ArticleTextItem(element)
            is QuoteBlock -> ArticleQuoteItem(element)
            is ImageBlock -> ArticleImageItem(element)
            is Divider   -> HorizontalDivider()
            is Push      -> ArticlePushItem(element)
        }
    }
}
```

**Component styles (aligned with existing theme constants):**

| Component | Style |
|---|---|
| `ArticleHeaderItem` | Dark card (`Surface` bg), `TextSecondary` labels, `TextPrimary` values |
| `ArticleTextItem` | `TextPrimary`, wrapped in `SelectionContainer` (long-press copy) |
| `ArticleQuoteItem` | Left border (`Primary` color), subtle tinted bg, `TextSecondary` text |
| `ArticleImageItem` | `AsyncImage` (Coil), fill width, tap opens URL in system browser |
| `HorizontalDivider` | `TextSecondary` color |
| `ArticlePushItem` | Push=blue / Boo=red / Neutral=gray prefix, single `Row` |

New files:
- `ui/article/ArticleHeaderItem.kt`
- `ui/article/ArticleTextItem.kt`
- `ui/article/ArticleQuoteItem.kt`
- `ui/article/ArticleImageItem.kt`
- `ui/article/ArticlePushItem.kt`

## Loading / Error States

```kotlin
when {
    uiState.isLoading -> CircularProgressIndicator() // centered
    uiState.errorMessage != null -> ErrorScreen(
        message = uiState.errorMessage,
        onRetry = { viewModel.loadArticle(url) }
    )
    else -> LazyColumn(elements)
}
```

`ArticleViewModel.loadCookies()` renamed to `loadArticle()` — same pattern, extended to call `getArticleContent()`.

## New Dependency

```kotlin
// app/build.gradle.kts
implementation(libs.coil.compose)
```

Also add `coil-compose` to `libs.versions.toml`.

## Files Changed

| File | Change |
|---|---|
| `data/ArticleElement.kt` | New |
| `repository/ArticleParser.kt` | New |
| `repository/ArticleRepository.kt` | Add `getArticleContent()` |
| `ui/article/ArticleScreen.kt` | Replace WebView with LazyColumn |
| `ui/article/ArticleViewModel.kt` | Rename + extend `loadArticle()` |
| `ui/article/ArticleHeaderItem.kt` | New |
| `ui/article/ArticleTextItem.kt` | New |
| `ui/article/ArticleQuoteItem.kt` | New |
| `ui/article/ArticleImageItem.kt` | New |
| `ui/article/ArticlePushItem.kt` | New |
| `app/build.gradle.kts` | Add Coil dependency |
| `gradle/libs.versions.toml` | Add Coil version |
