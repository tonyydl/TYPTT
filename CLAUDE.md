# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TYPTT is an Android app for browsing PTT (a Taiwanese BBS). It scrapes PTT's HTML directly using Jsoup — there is no REST API. The app is fully Jetpack Compose with no XML layouts or Fragments.

## Setup

The `.claude/settings.json` `SessionStart` hook automatically runs `git config core.hooksPath .githooks` when opening this project in Claude Code. If working outside Claude Code, run it manually:

```bash
git config core.hooksPath .githooks
```

This activates a `pre-push` hook that blocks push when Kotlin files changed but `CLAUDE.md` was not updated.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device
./gradlew lint                   # Run lint checks
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device)
```

## Required Local Configuration

Add to `local.properties` (not committed):
```
DOMAIN=www.ptt.cc
BASE_URL=https://www.ptt.cc
```

These are injected as `BuildConfig.DOMAIN` and `BuildConfig.BASE_URL`.

## Architecture

**Single Activity → Scaffold → NavHost → 3 Screens**

`MainActivity` extends `ComponentActivity` (not `AppCompatActivity` — no Fragments or XML). It wraps the entire UI in `TypttTheme`, sets up a `Scaffold` with a `TopAppBar` (title/back-button state derived from the current `NavBackStackEntry`), then delegates to `AppNavHost`.

### Navigation

Compose Navigation with URI-encoded query parameters — no SafeArgs. Routes are defined as constants in `AppNavHost.kt`. When navigating, args are encoded with `Uri.encode()` and decoded from `backStackEntry.arguments`.

```
hotboard  →  board?name={}&boardTitle={}&url={}  →  article?articleTitle={}&articleUrl={}
```

The TopAppBar title/subtitle in `MainApp` is read from `navBackStackEntry?.destination?.route` and `navBackStackEntry?.arguments` — so route names must be stable.

### Screen Pattern

Each screen follows the same structure:
- `XxxUiState` data class (defined in ViewModel file) — single source of truth
- `XxxViewModel` exposes `StateFlow<XxxUiState>`, uses `MutableStateFlow.update {}` for atomic transitions
- `XxxScreen` composable collects `uiState` via `collectAsStateWithLifecycle()`
- ViewModels are injected with `hiltViewModel()` inside `AppNavHost` composables

**Exception — Board:** Uses Paging 3's `LazyPagingItems.loadState` for loading/error state instead of a custom `UiState`; `BoardViewModel` exposes `Flow<PagingData<Articles>>` directly.

### Data Layer

All repositories are `object` singletons (not Hilt-injected). They perform blocking Jsoup HTTP calls inside `withContext(Dispatchers.IO)`.

- `HotBoardRepository` — scrapes `/bbs/hotboards.html`, parses board list
- `BoardRepository` — wraps `BoardPagingSource` in a `Pager`; each page = one PTT board page (~20 articles). PTT's most-recent board page may be partially filled, so `initialLoadSize = PAGE_SIZE` is set explicitly to avoid Paging 3 auto-appending.
- `ArticleRepository` — fetches cookies with over-18 confirmation, returns `Map<String, String>`

**Pagination key** = the URL of the previous PTT page (older articles). Scrolling down loads older content.

### PTT-specific Quirks

- Over-18 confirmation: all board requests POST `{from: url, yes: "yes"}` with `over18=1` cookie
- Deleted articles still appear as `r-ent` elements with no `.title a` link — their URL becomes `BASE_URL + ""`. These are included in lists as-is.
- `isTopArea` flag flips to `true` after `r-list-sep` element, marking subsequent articles as `Type.PINNED_ARTICLES`

### Article WebView

`ArticleScreen` uses `AndroidView { WebView }` since Compose has no native WebView. The `update` lambda guards with `webView.url == null` to load the URL exactly once (prevents reload on recomposition).

### Theming

All app colors are defined as named constants in `ui/theme/Color.kt` (`Background`, `Surface`, `Primary`, `TopBarColor`, `TextPrimary`, `TextSecondary`, `Pinned`). Never use raw `Color(0xFF...)` literals outside this file.

`TypttTheme` in `ui/theme/Theme.kt` wraps `MaterialTheme` with a `darkColorScheme` built from these constants. All screens are wrapped in `TypttTheme` at the `MainActivity` level.

### HotBoard Category Label

`CategoryLabel` in `HotBoardItem.kt` renders vertical text: CJK characters stack one-per-line in a `Column`; Latin/ASCII text is rotated 90° using a `layout` modifier that swaps width/height dimensions so the surrounding `Row` accounts for the rotated size correctly.

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | `navigation-compose` (no SafeArgs) |
| DI | Hilt (`@HiltViewModel`, `hiltViewModel()`) |
| Async | Coroutines + Flow (`viewModelScope`, `withContext`) |
| Paging | Paging 3 (`PagingSource`, `collectAsLazyPagingItems`) |
| HTML parsing | Jsoup |
| Logging | Timber (debug builds only) |
