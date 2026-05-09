package com.tonyyang.typtt.ui.article

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.ui.theme.Background

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    articleUrl: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(articleUrl) {
        viewModel.loadCookies(articleUrl)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                setBackgroundColor(Background.toArgb())
                settings.apply {
                    cacheMode = WebSettings.LOAD_DEFAULT
                    domStorageEnabled = true
                    databaseEnabled = true
                    javaScriptEnabled = true
                }
            }
        },
        update = { webView ->
            if (uiState.cookies.isNotEmpty() && webView.url == null) {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.removeAllCookies(null)
                for ((key, value) in uiState.cookies) {
                    cookieManager.setCookie(BuildConfig.DOMAIN, "$key=$value")
                }
                webView.loadUrl(
                    articleUrl,
                    mapOf("from" to BuildConfig.BASE_URL, "yes" to "yes")
                )
            }
        },
        modifier = modifier
    )
}
