package com.marcormz.jellyapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.marcormz.jellyapp.BuildConfig
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellySurface

private const val PAGE_URL = "https://marcormz.com/jellyapp/"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var progress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.setSupportMultipleWindows(false)
            settings.userAgentString = settings.userAgentString + " JellyAppQA/1.0"
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    isLoading = true
                    errorMessage = null
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    isLoading = false
                    progress = 100
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?,
                ) {
                    if (request?.isForMainFrame == true) {
                        isLoading = false
                        errorMessage = error?.description?.toString() ?: "No se pudo cargar la página."
                    }
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: android.webkit.WebResourceResponse?,
                ) {
                    if (request?.isForMainFrame == true) {
                        isLoading = false
                        errorMessage = "La página respondió con HTTP ${errorResponse?.statusCode}."
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    val scheme = request?.url?.scheme?.lowercase()
                    return scheme != "http" && scheme != "https"
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress
                    if (newProgress >= 100) isLoading = false
                }
            }
        }
    }

    LaunchedEffect(webView) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        webView.loadUrl(PAGE_URL)
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.removeAllViews()
            webView.destroy()
        }
    }

    val goBack = {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBack()
        }
    }
    BackHandler(onBack = goBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(
            title = "WebView",
            backButtonId = "btn_webview_back",
            onBack = goBack,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(JellyAmber, CircleShape),
                )
                Text(
                    text = PAGE_URL,
                    style = MaterialTheme.typography.bodySmall,
                    color = JellyMuted,
                    maxLines = 1,
                )
            }
            JellySecondaryButton(
                text = "Recargar",
                buttonId = "btn_webview_reload",
                onClick = {
                    errorMessage = null
                    webView.reload()
                },
            )
        }
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = JellyAmber,
                trackColor = JellySurface,
            )
        } else {
            Spacer(Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(JellySurface),
        ) {
            AndroidView(
                factory = { webView },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("webview_content"),
            )

            errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JellySurface)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    JellyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "La página no está disponible.",
                                style = MaterialTheme.typography.titleLarge,
                                color = JellyCream,
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = JellyMuted,
                            )
                            JellySecondaryButton(
                                text = "Reintentar",
                                buttonId = "btn_webview_retry",
                                onClick = {
                                    errorMessage = null
                                    webView.loadUrl(PAGE_URL)
                                },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
