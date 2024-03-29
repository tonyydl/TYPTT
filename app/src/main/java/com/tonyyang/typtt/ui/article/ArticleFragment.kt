package com.tonyyang.typtt.ui.article

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tonyyang.typtt.BuildConfig
import com.tonyyang.typtt.R
import com.tonyyang.typtt.databinding.FragmentArticleBinding
import com.tonyyang.typtt.setupActionBar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private lateinit var binding: FragmentArticleBinding

    private val viewModel by viewModels<ArticleViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
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
        }.orEmpty()
        binding.webView.apply {
            webViewClient = WebViewClient()
            setBackgroundColor(ContextCompat.getColor(activity as Context, R.color.bg_color))
        }
        with(binding.webView.settings) {
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            databaseEnabled = true
            javaScriptEnabled = true
        }
        viewModel.cookiesLiveData.observe(viewLifecycleOwner) { cookies ->
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.removeAllCookies {
                Timber.d(String.format(Locale.getDefault(), "Remove cookies: %b", it))
            }
            for (cookie in cookies) {
                val value = "${cookie.key}=${cookie.value}"
                cookieManager.setCookie(BuildConfig.DOMAIN, value)
            }
            binding.webView.loadUrl(url, HashMap<String, String>().apply {
                put("from", BuildConfig.BASE_URL)
                put("yes", "yes")
            })
        }
        viewModel.loadCookies(url)
    }
}