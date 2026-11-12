package com.termux.webview;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.termux.app.FusionToast;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebViewClient extends WebViewClient {
    public FusionWebViewManager mManager;
    private volatile String mCurrentUrl = null;

    public FusionWebViewClient(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    public FusionWebViewClient(FusionWebViewManager fusionWebViewManager, String initialUrl) {
        this.mManager = fusionWebViewManager;
        this.mCurrentUrl = initialUrl;
    }

    public void setCurrentUrl(String url) {
        this.mCurrentUrl = url;
    }

    public String getCurrentUrl() {
        return this.mCurrentUrl;
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String url, android.graphics.Bitmap favicon) {
        super.onPageStarted(webView, url, favicon);
        this.mCurrentUrl = url;
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.injectScripts(webView);
            FusionExtensionHelper.injectMatchingScripts(webView, url, "document_start");
        }
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        this.mCurrentUrl = str;
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.injectScripts(webView);
            fusionWebViewManager.onTabUrlLoaded(webView, str);
            FusionToolPillHelper.showToolPill(fusionWebViewManager);
            FusionToolPillHelper.bringToFront();
            FusionExtensionHelper.injectMatchingScripts(webView, str, "document_idle");

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
    public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler handler, String host, String realm) {
        if (this.mManager != null) {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(this.mManager);
            if (activeTab != null && activeTab.mIsProxyEnabled && activeTab.mProxyItem != null && activeTab.mProxyItem.hasAuth()) {
                handler.proceed(activeTab.mProxyItem.user, activeTab.mProxyItem.pass);
                return;
            }
        }
        super.onReceivedHttpAuthRequest(webView, handler, host, realm);
    }

    @Override // android.webkit.WebViewClient
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        if (sslErrorHandler != null) {
            sslErrorHandler.proceed();
        }
    }

    @Override // android.webkit.WebViewClient
    public boolean onRenderProcessGone(WebView webView, RenderProcessGoneDetail renderProcessGoneDetail) {
        if (this.mManager != null && this.mManager.mActivity != null) {
            this.mManager.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mManager != null) {
                            mManager.closeWebViewInternal();
                            if (mManager.mTerminalPaneHost != null) {
                                mManager.mTerminalPaneHost.setVisibility(View.VISIBLE);
                            }
                            if (mManager.mActivity != null) {
                                mManager.mActivity.endWebViewExternalTextInput();
                                mManager.mActivity.refreshTerminalWindowBar();
                                FusionToast.makeText((Context) mManager.mActivity, (CharSequence) "⚠️ WebView render process terminated / recovered", 0).show();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }
        return true;
    }

    private static String getFileMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }

    @Override // android.webkit.WebViewClient
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        if (webResourceRequest == null || webResourceRequest.getUrl() == null) {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }
        try {
            Context context = (this.mManager != null && this.mManager.mActivity != null)
                    ? this.mManager.mActivity
                    : webView.getContext();

            String currentUrl = this.mCurrentUrl;
            if (currentUrl == null || currentUrl.isEmpty()) {
                Map<String, String> headers = webResourceRequest.getRequestHeaders();
                if (headers != null) {
                    currentUrl = headers.get("Referer");
                    if (currentUrl == null) currentUrl = headers.get("referer");
                }
            }
            if (currentUrl == null || currentUrl.isEmpty()) {
                if (this.mManager != null) {
                    FusionTab tab = this.mManager.getTabForWebView(webView);
                    if (tab != null && tab.mUrl != null && !tab.mUrl.isEmpty()) {
                        currentUrl = tab.mUrl;
                    }
                }
            }

            WebResourceResponse intercepted = FusionExtensionHelper.handleExtensionResourceIntercept(
                    context, webResourceRequest.getUrl(), null, currentUrl);
            if (intercepted != null) {
                return intercepted;
            }
        } catch (Throwable t) {
            android.util.Log.e("FusionWebViewClient", "Error in shouldInterceptRequest", t);
        }
        return super.shouldInterceptRequest(webView, webResourceRequest);
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

        this.mCurrentUrl = url;

        // Navigasi skema chrome-extension:// dialihkan ke file lokal ekstensi
        if (url.startsWith("chrome-extension://")) {
            File localFile = FusionExtensionHelper.resolveExtensionUrlToFile(webView.getContext(), url);
            if (localFile != null && localFile.exists()) {
                this.mCurrentUrl = "file://" + localFile.getAbsolutePath();
                webView.loadUrl(this.mCurrentUrl);
                return true;
            }
        }

        // Intercept link pemasangan Userscript (.user.js) misal dari GreasyFork atau GitHub
        if (url.endsWith(".user.js") || url.contains(".user.js?")) {
            com.termux.app.TermuxActivity act = (this.mManager != null) ? this.mManager.mActivity : null;
            if (FusionUserscriptHelper.promptInstallUserscript(act, webView, url)) {
                return true;
            }
        }

        // Tangani root-relative file:// redirect pada halaman ekstensi (misal <meta refresh="0;url=/popup.html">)
        if (url.startsWith("file:///")) {
            Uri parsedUri = Uri.parse(url);
            String path = parsedUri.getPath();
            File targetFile = (path != null) ? new File(path) : null;
            if (targetFile != null && !targetFile.exists()) {
                String currentUrl = this.mCurrentUrl;
                if (currentUrl != null && currentUrl.startsWith("file://") && currentUrl.contains("/.fusion/extensions/")) {
                    try {
                        String currentPath = Uri.parse(currentUrl).getPath();
                        if (currentPath != null) {
                            File currentFile = new File(currentPath);
                            File extDir = currentFile.isDirectory() ? currentFile : currentFile.getParentFile();
                            while (extDir != null && extDir.exists() && !new File(extDir, "manifest.json").exists()) {
                                if (!extDir.getAbsolutePath().contains(".fusion/extensions")) break;
                                extDir = extDir.getParentFile();
                            }
                            if (extDir != null && new File(extDir, "manifest.json").exists()) {
                                String relName = path.startsWith("/") ? path.substring(1) : path;
                                File resolvedFile = new File(extDir, relName);
                                if (resolvedFile.exists()) {
                                    this.mCurrentUrl = "file://" + resolvedFile.getAbsolutePath();
                                    webView.loadUrl(this.mCurrentUrl);
                                    return true;
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
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
