package com.termux.webview;

import android.os.Message;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import java.io.File;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebChromeClient extends WebChromeClient {
    public static ValueCallback<Uri[]> sPendingFileChooser = null;
    public static String sPendingUploadFilePath = null;
    public static volatile boolean sAllowGeolocation = false;
    public static volatile boolean sAllowSensitivePermissions = false;
    public static volatile boolean sAllowDrmId = false;
    public static volatile boolean sAllowCamera = false;
    public static volatile boolean sAllowMic = false;
    private static final java.util.concurrent.ExecutorService sConsoleLogExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (sPendingUploadFilePath != null && !sPendingUploadFilePath.isEmpty()) {
            File file = new File(sPendingUploadFilePath);
            sPendingUploadFilePath = null;
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                filePathCallback.onReceiveValue(new Uri[]{uri});
                return true;
            }
        }
        sPendingFileChooser = filePathCallback;
        return true;
    }

    public static boolean supplyFileToPendingChooser(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            if (sPendingFileChooser != null) {
                sPendingFileChooser.onReceiveValue(null);
                sPendingFileChooser = null;
            }
            return false;
        }
        File file = new File(filePath);
        if (file.exists() && sPendingFileChooser != null) {
            Uri uri = Uri.fromFile(file);
            sPendingFileChooser.onReceiveValue(new Uri[]{uri});
            sPendingFileChooser = null;
            return true;
        }
        sPendingUploadFilePath = filePath;
        return true;
    }
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

        // 2. Untuk window.open() atau popup dynamic javascript, sambungkan langsung ke Tab WebView baru (mewarisi profile, chain & wallet induk)
        if (manager != null && manager.mActivity != null) {
            try {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                if (transport != null) {
                    FusionTab parentTab = manager.getTabForWebView(view);
                    String pProfile = (parentTab != null && parentTab.mProfile != null && !parentTab.mProfile.isEmpty()) ? parentTab.mProfile : "default";
                    String pChain = (parentTab != null && parentTab.mChainKey != null && !parentTab.mChainKey.isEmpty()) ? parentTab.mChainKey : "bsc";
                    boolean pGhost = parentTab != null ? parentTab.mIsGhost : true;
                    boolean pDesktop = parentTab != null && parentTab.mIsDesktop;
                    boolean pAuto = parentTab != null && parentTab.mIsAuto;
                    FusionTab newTab = manager.createTabInternal("about:blank", pProfile, "Popup", pChain, pGhost, pDesktop, pAuto, "");
                    if (newTab != null && newTab.mWebView != null) {
                        if (parentTab != null) {
                            newTab.mWalletName = parentTab.mWalletName;
                            newTab.mWalletAddress = parentTab.mWalletAddress;
                            newTab.mIotaWalletName = parentTab.mIotaWalletName;
                            newTab.mIotaWalletAddress = parentTab.mIotaWalletAddress;
                            newTab.mSolWalletName = parentTab.mSolWalletName;
                            newTab.mSolWalletAddress = parentTab.mSolWalletAddress;
                            newTab.mProxyItem = parentTab.mProxyItem;
                            newTab.mIsProxyEnabled = parentTab.mIsProxyEnabled;
                            newTab.mChainId = parentTab.mChainId;
                        }
                        transport.setWebView(newTab.mWebView);
                        resultMsg.sendToTarget();
                        manager.switchTab(manager.mTabList.size() - 1);
                        return true;
                    }
                }
            } catch (Throwable t) {
                Log.e("FusionPopup", "Failed to transport popup window to new tab", t);
            }
        }

        // 3. Fallback: tangkap URL via temp WebView
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
            final String msg = "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message();
            Log.d("FusionJS", msg + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
            final long ts = System.currentTimeMillis();
            sConsoleLogExecutor.execute(() -> {
                try {
                    java.io.File logFile = new java.io.File("/tmp/webview_console.log");
                    if (logFile.exists() && logFile.length() > 5 * 1024 * 1024) {
                        logFile.delete();
                    }
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(logFile, true)) {
                        String line = ts + " " + msg + "\n";
                        fos.write(line.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    }
                } catch (Throwable ignored) {}
            });
        }
        return true;
    }

    @Override // android.webkit.WebChromeClient
    public void onGeolocationPermissionsShowPrompt(String str, GeolocationPermissions.Callback callback) {
        if (callback != null) {
            callback.invoke(str, sAllowGeolocation, false);
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
        if (permissionRequest == null) return;
        if (sAllowSensitivePermissions) {
            permissionRequest.grant(permissionRequest.getResources());
            return;
        }
        String[] resources = permissionRequest.getResources();
        if (resources == null || resources.length == 0) {
            permissionRequest.deny();
            return;
        }
        java.util.List<String> allowed = new java.util.ArrayList<>();
        for (String r : resources) {
            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(r)) {
                if (sAllowDrmId) allowed.add(r);
            } else if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) {
                if (sAllowCamera) allowed.add(r);
            } else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) {
                if (sAllowMic) allowed.add(r);
            } else {
                allowed.add(r);
            }
        }
        if (allowed.isEmpty()) {
            permissionRequest.deny();
        } else {
            permissionRequest.grant(allowed.toArray(new String[0]));
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
