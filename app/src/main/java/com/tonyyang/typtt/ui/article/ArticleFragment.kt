package com.tonyyang.typtt.ui.article

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.R
import com.tonyyang.typtt.setupActionBar
import com.tonyyang.typtt.viewmodel.ArticleViewModel
import kotlinx.android.synthetic.main.fragment_article.*
import java.util.*
import kotlin.collections.HashMap


class ArticleFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(ArticleViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.run {
            val bundle = ArticleFragmentArgs.fromBundle(this)
            if (activity is AppCompatActivity) {
                (activity as AppCompatActivity).setupActionBar {
                    title = bundle.articleTitle
                    subtitle = null
                    setHomeButtonEnabled(true)
                    setDisplayHomeAsUpEnabled(true)
                }
            }
            bundle.articleUrl
        } ?: ""
        web_view.apply {
            webViewClient = WebViewClient()
            setBackgroundColor(ContextCompat.getColor(activity as Context, R.color.bg_color))
        }
        with(web_view.settings) {
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            databaseEnabled = true
            setAppCacheEnabled(true)
            setAppCachePath(this@ArticleFragment.activity?.getDir("cache", Context.MODE_PRIVATE)?.path)
            javaScriptEnabled = true
        }
        viewModel.cookiesLiveData.observe(viewLifecycleOwner, { cookies ->
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.removeAllCookies {
                    Log.d(TAG, String.format(Locale.getDefault(), "Remove cookies: %b", it))
                }
            } else {
                cookieManager.removeAllCookie()
            }
            for (cookie in cookies) {
                val value = "${cookie.key}=${cookie.value}"
                cookieManager.setCookie(BuildConfig.DOMAIN, value)
            }
            web_view.loadUrl(url, HashMap<String, String>().apply {
                put("from", BuildConfig.BASE_URL)
                put("yes", "yes")
            })
        })
        viewModel.loadCookies(url)
    }

    companion object {
        private val TAG = ArticleFragment::class.java.simpleName
    }
}