package com.termux.webview;

import android.os.Message;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebChromeClient extends WebChromeClient {
    @Override // android.webkit.WebChromeClient
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        if (resultMsg == null) {
            return false;
        }

        FusionWebViewManager manager = FusionWebViewManager.getInstance();

        // 1. Cek apakah ada target URL langsung dari link anchor yang diklik
        if (view != null) {
            WebView.HitTestResult result = view.getHitTestResult();
            if (result != null && result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                String extra = result.getExtra();
                if (extra != null && (extra.startsWith("http://") || extra.startsWith("https://"))) {
                    if (manager != null) {
                        manager.addNewTab(extra);
                    } else {
                        view.loadUrl(extra);
                    }
                    return false;
                }
            }
        }

        // 2. Untuk window.open() atau popup dynamic javascript, tangkap URL via temp WebView
        if (view == null || view.getContext() == null) {
            return false;
        }

        WebView tempWebView = new WebView(view.getContext());
        tempWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    openTarget(request.getUrl().toString());
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url != null) {
                    openTarget(url);
                }
                return true;
            }

            private void openTarget(String targetUrl) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr != null) {
                    mgr.addNewTab(targetUrl);
                } else if (view != null) {
                    view.loadUrl(targetUrl);
                }
                try {
                    tempWebView.destroy();
                } catch (Throwable ignored) {
                }
            }
        });

        try {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            if (transport != null) {
                transport.setWebView(tempWebView);
                resultMsg.sendToTarget();
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override // android.webkit.WebChromeClient
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage != null && consoleMessage.message() != null) {
            Log.d("FusionJS", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
        }
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public void onGeolocationPermissionsShowPrompt(String str, GeolocationPermissions.Callback callback) {
        if (callback != null) {
            callback.invoke(str, true, false);
        }
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsAlert(WebView webView, String str, String str2, JsResult jsResult) {
        if (jsResult == null) {
            return true;
        }
        jsResult.confirm();
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsBeforeUnload(WebView webView, String str, String str2, JsResult jsResult) {
        if (jsResult == null) {
            return true;
        }
        jsResult.confirm();
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsConfirm(WebView webView, String str, String str2, JsResult jsResult) {
        if (jsResult == null) {
            return true;
        }
        jsResult.confirm();
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public boolean onJsPrompt(WebView webView, String str, String str2, String str3, JsPromptResult jsPromptResult) {
        if (jsPromptResult == null) {
            return true;
        }
        jsPromptResult.confirm(str3);
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public void onPermissionRequest(PermissionRequest permissionRequest) {
        if (permissionRequest != null) {
            permissionRequest.grant(permissionRequest.getResources());
        }
    }

    @Override // android.webkit.WebChromeClient
    public void onReceivedTitle(WebView webView, String str) {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager != null) {
            fusionWebViewManager.onTabTitleReceived(webView, str);
        }
    }
}
