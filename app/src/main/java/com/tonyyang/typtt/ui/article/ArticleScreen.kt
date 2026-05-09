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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonyyang.typtt.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    articleUrl: String,
    modifier: Modifier = Modifier
) {
    val cookies by viewModel.cookies.collectAsStateWithLifecycle()

    LaunchedEffect(articleUrl) {
        viewModel.loadCookies(articleUrl)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                setBackgroundColor(0xFF222831.toInt())
                settings.apply {
                    cacheMode = WebSettings.LOAD_DEFAULT
                    domStorageEnabled = true
                    databaseEnabled = true
                    javaScriptEnabled = true
                }
            }
        },
        update = { webView ->
            // Load URL exactly once: when cookies arrive and page hasn't started loading yet
            if (cookies.isNotEmpty() && webView.url == null) {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.removeAllCookies(null)
                for ((key, value) in cookies) {
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
