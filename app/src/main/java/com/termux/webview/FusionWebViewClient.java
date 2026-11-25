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
import androidx.webkit.ServiceWorkerClientCompat;
import androidx.webkit.ServiceWorkerControllerCompat;
import androidx.webkit.WebViewFeature;
import com.termux.app.FusionToast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
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
            FusionTab ownerTab = fusionWebViewManager.getTabForWebView(webView);
            if (ownerTab != null && url != null && !url.isEmpty()) {
                ownerTab.mUrl = url;
            }
            boolean isActiveVisual = fusionWebViewManager.isWebViewOpen() && fusionWebViewManager.mActiveWebView == webView;
            String autoChain = FusionChainHelper.detectChainFromUrl(url);
            if (autoChain != null) {
                if (ownerTab != null) {
                    ownerTab.mChainKey = autoChain;
                    int cid = FusionChainHelper.getChainIdByKey(autoChain);
                    if (cid > 0) ownerTab.mChainId = cid;
                }
                if (isActiveVisual) {
                    GhostBridgeInterface.handleDappChainDetected(autoChain, 0);
                }
            } else if (FusionChainHelper.isNonEvm() && !FusionChainHelper.isNonEvmUrl(url)) {
                String evm = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty()) ? FusionChainHelper.sLastEvmChainKey : "bsc";
                int evmId = FusionChainHelper.sLastEvmChainId > 0 ? FusionChainHelper.sLastEvmChainId : 56;
                if (ownerTab != null && FusionChainHelper.isNonEvmKey(ownerTab.mChainKey)) {
                    ownerTab.mChainKey = evm;
                    ownerTab.mChainId = evmId;
                }
                if (isActiveVisual) {
                    GhostBridgeInterface.handleDappChainDetected(evm, evmId);
                }
            }
            // Selalu injeksikan script untuk semua tab (termasuk headless & background multi-worker)
            fusionWebViewManager.injectScripts(webView);
            FusionExtensionHelper.injectMatchingScripts(webView, url, "document_start");

            // Injeksi viewport desktop sejak awal load jika tab dalam mode desktop
            boolean isDesktop = (ownerTab != null && ownerTab.mIsDesktop) || (isActiveVisual && fusionWebViewManager.mIsDesktopMode);
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
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        this.mCurrentUrl = str;
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            FusionTab ownerTab = fusionWebViewManager.getTabForWebView(webView);
            if (ownerTab != null && str != null && !str.isEmpty()) {
                ownerTab.mUrl = str;
            }
            boolean isActiveVisual = fusionWebViewManager.isWebViewOpen() && fusionWebViewManager.mActiveWebView == webView;
            String autoChain = FusionChainHelper.detectChainFromUrl(str);
            if (autoChain != null) {
                if (ownerTab != null) {
                    ownerTab.mChainKey = autoChain;
                    int cid = FusionChainHelper.getChainIdByKey(autoChain);
                    if (cid > 0) ownerTab.mChainId = cid;
                }
                if (isActiveVisual) {
                    GhostBridgeInterface.handleDappChainDetected(autoChain, 0);
                }
            } else if (FusionChainHelper.isNonEvm() && !FusionChainHelper.isNonEvmUrl(str)) {
                String evm = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty()) ? FusionChainHelper.sLastEvmChainKey : "bsc";
                int evmId = FusionChainHelper.sLastEvmChainId > 0 ? FusionChainHelper.sLastEvmChainId : 56;
                if (ownerTab != null && FusionChainHelper.isNonEvmKey(ownerTab.mChainKey)) {
                    ownerTab.mChainKey = evm;
                    ownerTab.mChainId = evmId;
                }
                if (isActiveVisual) {
                    GhostBridgeInterface.handleDappChainDetected(evm, evmId);
                }
            }
            fusionWebViewManager.injectScripts(webView);
            if (isActiveVisual) {
                fusionWebViewManager.onTabUrlLoaded(webView, str);
                if (ownerTab == null || !ownerTab.mIsHeadless) {
                    FusionToolPillHelper.showToolPill(fusionWebViewManager);
                    FusionToolPillHelper.bringToFront();
                } else {
                    FusionToolPillHelper.hideToolPill(fusionWebViewManager);
                }
            }
            FusionExtensionHelper.injectMatchingScripts(webView, str, "document_idle");

            // Injeksi viewport desktop jika tab dalam mode desktop
            boolean isDesktop = (ownerTab != null && ownerTab.mIsDesktop) || (isActiveVisual && fusionWebViewManager.mIsDesktopMode);
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
            FusionTab targetTab = this.mManager.getTabForWebView(webView);
            if (targetTab == null) {
                targetTab = FusionTabStateHelper.getActiveTab(this.mManager);
            }
            if (targetTab != null && targetTab.mIsProxyEnabled && targetTab.mProxyItem != null && targetTab.mProxyItem.hasAuth()) {
                handler.proceed(targetTab.mProxyItem.user, targetTab.mProxyItem.pass);
                return;
            }
        }
        super.onReceivedHttpAuthRequest(webView, handler, host, realm);
    }

    public static volatile boolean sStrictSslCheck = false;

    @Override // android.webkit.WebViewClient
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        if (sslErrorHandler == null) return;
        if (sStrictSslCheck) {
            String url = (sslError != null) ? sslError.getUrl() : null;
            boolean isLocal = url != null && (url.contains("://127.0.0.1") || url.contains("://localhost"));
            if (!isLocal) {
                android.util.Log.w("FusionWebViewClient", "Strict SSL blocked certificate error for: " + url);
                sslErrorHandler.cancel();
                return;
            }
        }
        sslErrorHandler.proceed();
    }

    public static volatile int sMaxPhoenixRetries = 3;
    public static volatile long sPhoenixCooldownMs = 15000;
    public static volatile boolean sAutoResurrectEnabled = true;

    private static final Map<Integer, Integer> sCrashCountMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<Integer, Long> sLastCrashTimeMap = new java.util.concurrent.ConcurrentHashMap<>();

    @Override // android.webkit.WebViewClient
    public boolean onRenderProcessGone(WebView webView, RenderProcessGoneDetail renderProcessGoneDetail) {
        if (this.mManager != null && this.mManager.mActivity != null) {
            this.mManager.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mManager != null) {
                            FusionTab tab = mManager.getTabForWebView(webView);
                            int tabId = tab != null ? tab.mId : -1;
                            long now = System.currentTimeMillis();
                            long lastCrash = sLastCrashTimeMap.containsKey(tabId) ? sLastCrashTimeMap.get(tabId) : 0;
                            long cooldown = sPhoenixCooldownMs;
                            int maxRetries = sMaxPhoenixRetries;
                            int count = (now - lastCrash < cooldown && sCrashCountMap.containsKey(tabId)) ? sCrashCountMap.get(tabId) + 1 : 1;
                            sLastCrashTimeMap.put(tabId, now);
                            sCrashCountMap.put(tabId, count);

                            // 🛡️ Phoenix Recovery: Auto-resurrect up to maxRetries before graceful shutdown
                            if (tab != null && sAutoResurrectEnabled && count <= maxRetries) {
                                android.util.Log.w("FusionWebViewClient", "🛡️ Phoenix Recovery triggered for tab #" + tab.mId + " (crash count: " + count + ")");
                                mManager.resurrectTab(tab);
                                FusionToast.makeText((Context) mManager.mActivity, (CharSequence) ("🛡️ Phoenix Recovery: Tab #" + tab.mId + " pulih otomatis"), 0).show();
                                return;
                            }

                            // If crash loop persists > 3x: hanya tutup tab yang bermasalah bila tab worker/non-aktif
                            FusionTab activeFg = FusionTabStateHelper.getActiveTab(mManager);
                            if (tab != null && activeFg != null && activeFg.mId != tab.mId) {
                                mManager.killTabById(tab.mId);
                                if (mManager.mActivity != null) {
                                    FusionToast.makeText((Context) mManager.mActivity, (CharSequence) ("⚠️ Worker Tab #" + tab.mId + " crash > batas & ditutup aman"), 0).show();
                                }
                                return;
                            }
                            mManager.closeWebViewInternal();
                            if (mManager.mTerminalPaneHost != null) {
                                mManager.mTerminalPaneHost.setVisibility(View.VISIBLE);
                            }
                            if (mManager.mActivity != null) {
                                mManager.mActivity.endWebViewExternalTextInput();
                                mManager.mActivity.refreshTerminalWindowBar();
                                FusionToast.makeText((Context) mManager.mActivity, (CharSequence) "⚠️ WebView render process terminated (limit reached)", 0).show();
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

    public static volatile boolean sEnableBuiltinTrackerBlock = true;
    private static final java.util.concurrent.atomic.AtomicBoolean sServiceWorkerHooked = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static void ensureServiceWorkerHooked() {
        if (!sServiceWorkerHooked.compareAndSet(false, true)) {
            return;
        }
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
                ServiceWorkerControllerCompat.getInstance().setServiceWorkerClient(new ServiceWorkerClientCompat() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                        if (request == null || request.getUrl() == null) return null;
                        String reqUrl = request.getUrl().toString();
                        if (isKnownTrackerOrTelemetry(reqUrl)) {
                            return new WebResourceResponse("text/plain", "UTF-8", 204, "No Content", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
                        }
                        return FusionNetworkInterceptor.intercept(reqUrl);
                    }
                });
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isKnownTrackerOrTelemetry(String url) {
        if (!sEnableBuiltinTrackerBlock || url == null || url.length() < 10) return false;
        String lower = url.toLowerCase();
        return lower.contains("google-analytics.com") ||
               lower.contains("googletagmanager.com") ||
               lower.contains("doubleclick.net") ||
               lower.contains("sentry.io") ||
               lower.contains("mixpanel.com") ||
               lower.contains("clarity.ms") ||
               lower.contains("hotjar.com") ||
               lower.contains("datadoghq.com") ||
               lower.contains("segment.io") ||
               lower.contains("amplitude.com");
    }

    @Override // android.webkit.WebViewClient
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        if (webResourceRequest == null || webResourceRequest.getUrl() == null) {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }
        try {
            String reqUrl = webResourceRequest.getUrl().toString();

            // ⚡ Native Fast-Path: Blackhole tracking, analytics & telemetry directly in Java memory (0ms)
            if (isKnownTrackerOrTelemetry(reqUrl)) {
                return new WebResourceResponse("text/plain", "UTF-8", 204, "No Content", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }

            WebResourceResponse wireMock = FusionNetworkInterceptor.intercept(reqUrl);
            if (wireMock != null) {
                return wireMock;
            }

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
