package com.termux.webview;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebViewClient extends WebViewClient {
    public FusionWebViewManager mManager;

    public FusionWebViewClient(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.injectScripts(webView);
            fusionWebViewManager.onTabUrlLoaded(webView, str);

            // Injeksi viewport desktop jika tab aktif dalam mode desktop
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
            boolean isDesktop = (activeTab != null && activeTab.mIsDesktop) || fusionWebViewManager.mIsDesktopMode;
            if (isDesktop && webView != null) {
                String desktopViewportJs =
                    "(function() {" +
                    "  try {" +
                    "    var meta = document.querySelector('meta[name=\"viewport\"]');" +
                    "    if (!meta) {" +
                    "      meta = document.createElement('meta');" +
                    "      meta.name = 'viewport';" +
                    "      document.head.appendChild(meta);" +
                    "    }" +
                    "    meta.setAttribute('content', 'width=1280, initial-scale=0.35, minimum-scale=0.1, maximum-scale=3.0, user-scalable=yes');" +
                    "  } catch(e) {}" +
                    "})();";
                webView.evaluateJavascript(desktopViewportJs, null);
            }
        }
    }

    @Override // android.webkit.WebViewClient
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        if (sslErrorHandler != null) {
            sslErrorHandler.proceed();
        }
    }

    @Override // android.webkit.WebViewClient
    public boolean onRenderProcessGone(WebView webView, RenderProcessGoneDetail renderProcessGoneDetail) {
        return true;
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
        if (webResourceRequest == null || webResourceRequest.getUrl() == null) {
            return false;
        }
        return handleUrlLoading(webView, webResourceRequest.getUrl().toString());
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        return handleUrlLoading(webView, str);
    }

    private boolean handleUrlLoading(WebView webView, String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Protokol web standar dimuat langsung di dalam WebView
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("about:") || url.startsWith("javascript:") || url.startsWith("data:")) {
            return false;
        }

        // Tangani skema intent:// dan skema aplikasi eksternal (tg://, twitter://, mailto:, tel:, dll.)
        if (webView != null && webView.getContext() != null) {
            Context context = webView.getContext();
            try {
                if (url.startsWith("intent://")) {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        try {
                            context.startActivity(intent);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (fallbackUrl != null && (fallbackUrl.startsWith("http://") || fallbackUrl.startsWith("https://"))) {
                                webView.loadUrl(fallbackUrl);
                                return true;
                            }
                        }
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return true;
    }
}
