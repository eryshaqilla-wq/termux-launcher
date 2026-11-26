package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.termux.app.FusionToast;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebResponse;

/**
 * Full-parity Mozilla GeckoView engine controller for the hybrid WebView architecture:
 * - Lazy GeckoRuntime initialization (0 MB RAM when only System WebView is used)
 * - Per-profile isolated contextId containers
 * - Synchronous @JavascriptInterface RPC over PromptDelegate (GhostBridge, FusionNative, ExtensionBridge)
 * - Automatic injection of fingerprint_faker.js, ghost_injected.js, userscripts & extension content scripts
 * - HTTP/HTTPS/SOCKS5 proxy + auth routing via built-in WebExtension background worker
 * - Native alerts/confirms/prompts, file upload chooser bridge, permissions, desktop/mobile & screenshot capture
 */
public final class FusionGeckoHelper {
    private static final String TAG = "FusionGeckoHelper";
    private static final String EXT_URI = "resource://android/assets/gecko_extension/";
    private static final String EXT_ID = "fusion@termux.com";
    private static final String EXT_CONTENT_APP_ID = "fusion";
    private static final String EXT_BG_APP_ID = "fusion_bg";
    private static final String BRIDGE_PROMPT_PREFIX = "__FUSION_BRIDGE__:";

    private static GeckoRuntime sRuntime;
    private static WebExtension sBridgeExtension;
    private static WebExtension.Port sBgPort;
    private static JSONObject sPendingProxyPayload;
    private static JSONObject sPendingBlockerPayload;
    private static volatile String sBridgeProxyJsCache;
    private static final java.util.concurrent.ExecutorService sConsoleLogExecutor =
        java.util.concurrent.Executors.newSingleThreadExecutor();

    private static final Map<GeckoSession, WebExtension.Port> sSessionPorts = new ConcurrentHashMap<>();
    private static final Map<GeckoSession, List<String>> sQueuedScripts = new ConcurrentHashMap<>();
    private static final Map<String, PendingEval> sPendingEvals = new ConcurrentHashMap<>();

    private static volatile GeckoResult<GeckoSession.PromptDelegate.PromptResponse> sPendingFileResult;
    private static volatile GeckoSession.PromptDelegate.FilePrompt sPendingFilePrompt;
    private static volatile Context sLastContext;

    private static final class PendingEval {
        final AtomicReference<String> value = new AtomicReference<>(null);
        final CountDownLatch latch = new CountDownLatch(1);
    }

    private FusionGeckoHelper() {
    }

    public static synchronized GeckoRuntime getOrCreateRuntime(Context context) {
        if (sRuntime == null && context != null) {
            Context appCtx = context.getApplicationContext();
            sLastContext = appCtx;
            boolean isNight = isNightMode(context);
            GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                .javaScriptEnabled(true)
                .remoteDebuggingEnabled(true)
                .consoleOutput(true)
                .doubleTapZoomingEnabled(true)
                .inputAutoZoomEnabled(true)
                .extensionsWebAPIEnabled(true)
                .preferredColorScheme(isNight ? GeckoRuntimeSettings.COLOR_SCHEME_DARK : GeckoRuntimeSettings.COLOR_SCHEME_LIGHT)
                .build();
            sRuntime = GeckoRuntime.create(appCtx, settings);
            ensureBridgeExtension(sRuntime);
        }
        return sRuntime;
    }

    public static void applyTheme(Context context) {
        GeckoRuntime rt = sRuntime;
        if (rt == null || context == null) return;
        try {
            boolean isNight = isNightMode(context);
            rt.getSettings().setPreferredColorScheme(
                isNight ? GeckoRuntimeSettings.COLOR_SCHEME_DARK : GeckoRuntimeSettings.COLOR_SCHEME_LIGHT
            );
        } catch (Throwable ignored) {}
    }

    private static boolean isNightMode(Context context) {
        try {
            if (context != null && context.getResources() != null && context.getResources().getConfiguration() != null) {
                int nightMask = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                return nightMask == Configuration.UI_MODE_NIGHT_YES;
            }
        } catch (Throwable ignored) {}
        return true;
    }

    private static void ensureBridgeExtension(GeckoRuntime runtime) {
        if (runtime == null) return;
        if (sBridgeExtension != null) {
            attachBackgroundDelegate(sBridgeExtension);
            return;
        }
        try {
            runtime.getWebExtensionController()
                .ensureBuiltIn(EXT_URI, EXT_ID)
                .accept(ext -> {
                    sBridgeExtension = ext;
                    attachBackgroundDelegate(ext);
                }, err -> Log.w(TAG, "Built-in Gecko extension init skipped", err));
        } catch (Throwable t) {
            Log.w(TAG, "Failed ensuring Gecko bridge extension", t);
        }
    }

    private static void attachBackgroundDelegate(WebExtension ext) {
        if (ext == null) return;
        try {
            ext.setMessageDelegate(new WebExtension.MessageDelegate() {
                @Override
                public void onConnect(@NonNull WebExtension.Port port) {
                    sBgPort = port;
                    if (sPendingProxyPayload != null) {
                        try {
                            port.postMessage(sPendingProxyPayload);
                        } catch (Throwable ignored) {}
                    }
                    if (sPendingBlockerPayload != null) {
                        try {
                            port.postMessage(sPendingBlockerPayload);
                        } catch (Throwable ignored) {}
                    }
                    port.setDelegate(new WebExtension.PortDelegate() {
                        @Override
                        public void onDisconnect(@NonNull WebExtension.Port p) {
                            if (sBgPort == p) {
                                sBgPort = null;
                            }
                        }
                    });
                }
            }, EXT_BG_APP_ID);
        } catch (Throwable t) {
            Log.w(TAG, "attachBackgroundDelegate error", t);
        }
    }

    public static boolean createGeckoTab(FusionWebViewManager manager, FusionTab tab, String profile, boolean isDesktop) {
        if (manager == null || manager.mActivity == null || tab == null) {
            return false;
        }
        AppCompatActivity activity = manager.mActivity;
        sLastContext = activity.getApplicationContext();
        GeckoRuntime runtime = getOrCreateRuntime(activity);
        if (runtime == null) {
            return false;
        }

        String profKey = (profile != null && !profile.trim().isEmpty()) ? profile.trim() : "default";
        String contextId = "fusion_gecko_" + profKey.replaceAll("[^a-zA-Z0-9._-]", "_");

        GeckoSessionSettings sessionSettings = new GeckoSessionSettings.Builder()
            .contextId(contextId)
            .useTrackingProtection(false)
            .userAgentMode(isDesktop ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP : GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
            .build();
        sessionSettings.setViewportMode(isDesktop ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP : GeckoSessionSettings.VIEWPORT_MODE_MOBILE);

        GeckoSession session = new GeckoSession(sessionSettings);
        tab.mEngine = FusionWebViewManager.ENGINE_GECKO;
        tab.mGeckoSession = session;

        attachDelegates(manager, tab, session);
        attachExtensionBridge(manager, tab, runtime, session);
        session.open(runtime);

        GeckoView geckoView = new GeckoView(activity);
        geckoView.coverUntilFirstPaint(isNightMode(activity) ? Color.parseColor("#1E1E2E") : Color.WHITE);
        geckoView.setSession(session);
        geckoView.setFocusable(true);
        geckoView.setFocusableInTouchMode(true);
        geckoView.setClickable(true);
        geckoView.setEnabled(true);

        tab.mGeckoView = geckoView;
        return true;
    }

    private static void attachDelegates(FusionWebViewManager manager, FusionTab tab, GeckoSession session) {
        final GhostBridgeInterface ghostBridge = new GhostBridgeInterface(manager, tab);
        final FusionNativeBridge nativeBridge = new FusionNativeBridge(manager, tab);
        final FusionExtensionHelper.FusionExtensionBridge extBridge =
            new FusionExtensionHelper.FusionExtensionBridge(manager.mActivity, null, null, false);

        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public void onLocationChange(@NonNull GeckoSession s, @Nullable String url,
                                         @NonNull List<GeckoSession.PermissionDelegate.ContentPermission> perms,
                                         @NonNull Boolean hasUserGesture) {
                if (url != null && !url.isEmpty()) {
                    tab.mUrl = url;
                    syncDetectedChain(manager, tab, url);
                }
            }

            @Override
            public void onCanGoBack(@NonNull GeckoSession s, boolean canGoBack) {
                tab.mGeckoCanGoBack = canGoBack;
            }

            @Override
            public void onCanGoForward(@NonNull GeckoSession s, boolean canGoForward) {
                tab.mGeckoCanGoForward = canGoForward;
            }

            @Nullable
            @Override
            public GeckoResult<AllowOrDeny> onLoadRequest(@NonNull GeckoSession s, @NonNull LoadRequest request) {
                if (request != null && request.uri != null) {
                    String uri = request.uri;
                    if (!uri.startsWith("http://") && !uri.startsWith("https://")
                        && !uri.startsWith("file://") && !uri.startsWith("about:")
                        && !uri.startsWith("data:") && !uri.startsWith("blob:")
                        && !uri.startsWith("javascript:")) {
                        if (manager.mActivity != null) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                manager.mActivity.startActivity(intent);
                            } catch (Throwable ignored) {}
                        }
                        return GeckoResult.deny();
                    }
                    if (uri.endsWith(".user.js") || uri.contains(".user.js?")) {
                        if (FusionUserscriptHelper.promptInstallUserscript(manager.mActivity, uri)) {
                            return GeckoResult.deny();
                        }
                    }
                    tab.mUrl = uri;
                    syncDetectedChain(manager, tab, uri);
                }
                return GeckoResult.allow();
            }

            @Nullable
            @Override
            public GeckoResult<GeckoSession> onNewSession(@NonNull GeckoSession s, @NonNull String uri) {
                if (uri != null && !uri.isEmpty()) {
                    tab.mUrl = uri;
                    s.loadUri(uri);
                }
                return null;
            }
        });

        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(@NonNull GeckoSession s, @Nullable String title) {
                if (title != null && !title.trim().isEmpty()) {
                    tab.mTitle = title.trim();
                }
            }

            @Override
            public void onExternalResponse(@NonNull GeckoSession s, @NonNull WebResponse response) {
                if (response == null || response.uri == null || response.uri.isEmpty()) return;
                Context ctx = manager.mActivity != null ? manager.mActivity : sLastContext;
                if (ctx == null) return;
                String cd = "";
                String ct = "";
                if (response.headers != null) {
                    for (Map.Entry<String, String> e : response.headers.entrySet()) {
                        String k = e.getKey() != null ? e.getKey().toLowerCase() : "";
                        if ("content-disposition".equals(k)) cd = e.getValue();
                        else if ("content-type".equals(k)) ct = e.getValue();
                    }
                }
                new FusionDownloadListener(ctx).onDownloadStart(response.uri, GeckoSession.getDefaultUserAgent(), cd, ct, -1);
            }

            @Override
            public void onCrash(@NonNull GeckoSession s) {
                Log.w(TAG, "GeckoSession crashed for tab #" + tab.mId);
                GeckoRuntime rt = sRuntime;
                if (rt != null && !s.isOpen()) {
                    try {
                        s.open(rt);
                        if (tab.mUrl != null && !tab.mUrl.isEmpty()) {
                            s.loadUri(tab.mUrl);
                        }
                    } catch (Throwable ignored) {}
                }
            }

            @Override
            public void onKill(@NonNull GeckoSession s) {
                Log.w(TAG, "GeckoSession killed by OS for tab #" + tab.mId);
            }
        });

        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(@NonNull GeckoSession s, @NonNull String url) {
                if (url != null && !url.isEmpty()) {
                    tab.mUrl = url;
                    syncDetectedChain(manager, tab, url);
                }
                injectScripts(manager, tab, true);
            }

            @Override
            public void onPageStop(@NonNull GeckoSession s, boolean success) {
                if (tab.mTitle == null || tab.mTitle.isEmpty()) {
                    tab.mTitle = tab.mUrl;
                }
                injectScripts(manager, tab, false);
                if (manager.isWebViewOpen() && FusionTabStateHelper.getActiveTab(manager) == tab) {
                    if (!tab.mIsHeadless) {
                        FusionToolPillHelper.showToolPill(manager);
                        FusionToolPillHelper.bringToFront();
                    } else {
                        FusionToolPillHelper.hideToolPill(manager);
                    }
                }
            }
        });

        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override
            public void onAndroidPermissionsRequest(@NonNull GeckoSession s, @Nullable String[] permissions, @NonNull Callback callback) {
                callback.grant();
            }

            @Nullable
            @Override
            public GeckoResult<Integer> onContentPermissionRequest(@NonNull GeckoSession s, @NonNull ContentPermission perm) {
                int type = perm.permission;
                boolean allow = false;
                if (type == PERMISSION_GEOLOCATION) {
                    allow = FusionWebChromeClient.sAllowGeolocation;
                } else if (type == PERMISSION_DESKTOP_NOTIFICATION || type == PERMISSION_PERSISTENT_STORAGE) {
                    allow = true;
                } else if (type == PERMISSION_AUTOPLAY_INAUDIBLE || type == PERMISSION_AUTOPLAY_AUDIBLE) {
                    allow = true;
                } else if (type == PERMISSION_MEDIA_KEY_SYSTEM_ACCESS) {
                    allow = FusionWebChromeClient.sAllowDrmId;
                } else {
                    allow = FusionWebChromeClient.sAllowSensitivePermissions;
                }
                return GeckoResult.fromValue(allow ? ContentPermission.VALUE_ALLOW : ContentPermission.VALUE_DENY);
            }

            @Override
            public void onMediaPermissionRequest(@NonNull GeckoSession s, @NonNull String uri,
                                                 @Nullable MediaSource[] video, @Nullable MediaSource[] audio,
                                                 @NonNull MediaCallback callback) {
                boolean camOk = (video == null || video.length == 0) || FusionWebChromeClient.sAllowCamera;
                boolean micOk = (audio == null || audio.length == 0) || FusionWebChromeClient.sAllowMic;
                if (camOk && micOk) {
                    callback.grant(video != null && video.length > 0 ? video[0] : null,
                        audio != null && audio.length > 0 ? audio[0] : null);
                } else {
                    callback.reject();
                }
            }
        });

        session.setPromptDelegate(new GeckoSession.PromptDelegate() {
            @Nullable
            @Override
            public GeckoResult<PromptResponse> onTextPrompt(@NonNull GeckoSession s, @NonNull TextPrompt prompt) {
                if (prompt.message != null && prompt.message.startsWith(BRIDGE_PROMPT_PREFIX)) {
                    String payload = prompt.message.substring(BRIDGE_PROMPT_PREFIX.length());
                    String out = invokeBridgeSync(payload, ghostBridge, nativeBridge, extBridge);
                    return GeckoResult.fromValue(prompt.confirm(out != null ? out : ""));
                }
                AppCompatActivity act = manager.mActivity;
                if (act == null || act.isFinishing()) {
                    return GeckoResult.fromValue(prompt.dismiss());
                }
                GeckoResult<PromptResponse> res = new GeckoResult<>();
                final EditText input = new EditText(act);
                if (prompt.defaultValue != null) {
                    input.setText(prompt.defaultValue);
                }
                new AlertDialog.Builder(act)
                    .setTitle(prompt.title != null && !prompt.title.isEmpty() ? prompt.title : "Input")
                    .setMessage(prompt.message != null ? prompt.message : "")
                    .setView(input)
                    .setPositiveButton("OK", (d, w) -> res.complete(prompt.confirm(input.getText().toString())))
                    .setNegativeButton("Batal", (d, w) -> res.complete(prompt.dismiss()))
                    .setOnCancelListener(d -> res.complete(prompt.dismiss()))
                    .show();
                return res;
            }

            @Nullable
            @Override
            public GeckoResult<PromptResponse> onAlertPrompt(@NonNull GeckoSession s, @NonNull AlertPrompt prompt) {
                AppCompatActivity act = manager.mActivity;
                if (act == null || act.isFinishing()) {
                    return GeckoResult.fromValue(prompt.dismiss());
                }
                GeckoResult<PromptResponse> res = new GeckoResult<>();
                new AlertDialog.Builder(act)
                    .setTitle(prompt.title != null && !prompt.title.isEmpty() ? prompt.title : "Alert")
                    .setMessage(prompt.message != null ? prompt.message : "")
                    .setPositiveButton("OK", (d, w) -> res.complete(prompt.dismiss()))
                    .setOnCancelListener(d -> res.complete(prompt.dismiss()))
                    .show();
                return res;
            }

            @Nullable
            @Override
            public GeckoResult<PromptResponse> onButtonPrompt(@NonNull GeckoSession s, @NonNull ButtonPrompt prompt) {
                AppCompatActivity act = manager.mActivity;
                if (act == null || act.isFinishing()) {
                    return GeckoResult.fromValue(prompt.dismiss());
                }
                GeckoResult<PromptResponse> res = new GeckoResult<>();
                new AlertDialog.Builder(act)
                    .setTitle(prompt.title != null && !prompt.title.isEmpty() ? prompt.title : "Konfirmasi")
                    .setMessage(prompt.message != null ? prompt.message : "")
                    .setPositiveButton("OK", (d, w) -> res.complete(prompt.confirm(ButtonPrompt.Type.POSITIVE)))
                    .setNegativeButton("Batal", (d, w) -> res.complete(prompt.confirm(ButtonPrompt.Type.NEGATIVE)))
                    .setOnCancelListener(d -> res.complete(prompt.dismiss()))
                    .show();
                return res;
            }

            @Nullable
            @Override
            public GeckoResult<PromptResponse> onAuthPrompt(@NonNull GeckoSession s, @NonNull AuthPrompt prompt) {
                if (tab.mIsProxyEnabled && tab.mProxyItem != null && tab.mProxyItem.hasAuth()) {
                    return GeckoResult.fromValue(prompt.confirm(tab.mProxyItem.user, tab.mProxyItem.pass));
                }
                return GeckoResult.fromValue(prompt.dismiss());
            }

            @Nullable
            @Override
            public GeckoResult<PromptResponse> onFilePrompt(@NonNull GeckoSession s, @NonNull FilePrompt prompt) {
                Context ctx = manager.mActivity != null ? manager.mActivity : sLastContext;
                if (FusionWebChromeClient.sPendingUploadFilePath != null && !FusionWebChromeClient.sPendingUploadFilePath.isEmpty() && ctx != null) {
                    File f = new File(FusionWebChromeClient.sPendingUploadFilePath);
                    FusionWebChromeClient.sPendingUploadFilePath = null;
                    if (f.exists()) {
                        return GeckoResult.fromValue(prompt.confirm(ctx, Uri.fromFile(f)));
                    }
                }
                GeckoResult<PromptResponse> res = new GeckoResult<>();
                sPendingFilePrompt = prompt;
                sPendingFileResult = res;
                return res;
            }
        });
    }

    public static boolean supplyPendingFile(String filePath) {
        GeckoSession.PromptDelegate.FilePrompt prompt = sPendingFilePrompt;
        GeckoResult<GeckoSession.PromptDelegate.PromptResponse> res = sPendingFileResult;
        if (prompt == null || res == null) {
            return false;
        }
        sPendingFilePrompt = null;
        sPendingFileResult = null;
        if (filePath == null || filePath.isEmpty() || sLastContext == null) {
            res.complete(prompt.dismiss());
            return false;
        }
        File f = new File(filePath);
        if (f.exists()) {
            res.complete(prompt.confirm(sLastContext, Uri.fromFile(f)));
            return true;
        }
        res.complete(prompt.dismiss());
        return false;
    }

    private static void syncDetectedChain(FusionWebViewManager manager, FusionTab tab, String url) {
        if (manager == null || tab == null || url == null) return;
        boolean isActiveVisual = manager.isWebViewOpen() && FusionTabStateHelper.getActiveTab(manager) == tab;
        String autoChain = FusionChainHelper.detectChainFromUrl(url);
        if (autoChain != null) {
            tab.mChainKey = autoChain;
            int cid = FusionChainHelper.getChainIdByKey(autoChain);
            if (cid > 0) tab.mChainId = cid;
            if (isActiveVisual) {
                GhostBridgeInterface.handleDappChainDetected(autoChain, 0);
            }
        } else if (FusionChainHelper.isNonEvm() && !FusionChainHelper.isNonEvmUrl(url)) {
            String evm = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty())
                ? FusionChainHelper.sLastEvmChainKey : "bsc";
            int evmId = FusionChainHelper.sLastEvmChainId > 0 ? FusionChainHelper.sLastEvmChainId : 56;
            if (FusionChainHelper.isNonEvmKey(tab.mChainKey)) {
                tab.mChainKey = evm;
                tab.mChainId = evmId;
            }
            if (isActiveVisual) {
                GhostBridgeInterface.handleDappChainDetected(evm, evmId);
            }
        }
    }

    private static String invokeBridgeSync(String payloadJson,
                                           GhostBridgeInterface ghostBridge,
                                           FusionNativeBridge nativeBridge,
                                           FusionExtensionHelper.FusionExtensionBridge extBridge) {
        try {
            JSONObject obj = new JSONObject(payloadJson);
            String targetName = obj.optString("target", "");
            String methodName = obj.optString("method", "");
            JSONArray argsArr = obj.optJSONArray("args");
            Object target = null;
            if ("GhostBridge".equals(targetName)) {
                target = ghostBridge;
            } else if ("FusionNative".equals(targetName) || "__FUSION_NATIVE__".equals(targetName)) {
                target = nativeBridge;
            } else if ("__FusionExtensionBridge__".equals(targetName)) {
                target = extBridge;
            }
            if (target == null || methodName.isEmpty()) return "";
            int argc = argsArr != null ? argsArr.length() : 0;
            for (Method m : target.getClass().getMethods()) {
                if (!m.isAnnotationPresent(JavascriptInterface.class)) continue;
                if (!m.getName().equals(methodName)) continue;
                Class<?>[] pts = m.getParameterTypes();
                if (pts.length != argc) continue;
                Object[] invokeArgs = new Object[argc];
                boolean match = true;
                for (int i = 0; i < argc; i++) {
                    Object rawVal = argsArr.isNull(i) ? null : argsArr.get(i);
                    Class<?> pt = pts[i];
                    if (pt == String.class) {
                        invokeArgs[i] = rawVal != null ? String.valueOf(rawVal) : null;
                    } else if (pt == int.class || pt == Integer.class) {
                        invokeArgs[i] = rawVal instanceof Number ? ((Number) rawVal).intValue() : Integer.parseInt(String.valueOf(rawVal));
                    } else if (pt == long.class || pt == Long.class) {
                        invokeArgs[i] = rawVal instanceof Number ? ((Number) rawVal).longValue() : Long.parseLong(String.valueOf(rawVal));
                    } else if (pt == float.class || pt == Float.class) {
                        invokeArgs[i] = rawVal instanceof Number ? ((Number) rawVal).floatValue() : Float.parseFloat(String.valueOf(rawVal));
                    } else if (pt == double.class || pt == Double.class) {
                        invokeArgs[i] = rawVal instanceof Number ? ((Number) rawVal).doubleValue() : Double.parseDouble(String.valueOf(rawVal));
                    } else if (pt == boolean.class || pt == Boolean.class) {
                        invokeArgs[i] = rawVal instanceof Boolean ? rawVal : Boolean.parseBoolean(String.valueOf(rawVal));
                    } else {
                        match = false;
                        break;
                    }
                }
                if (!match) continue;
                Object res = m.invoke(target, invokeArgs);
                return res == null ? "" : String.valueOf(res);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Bridge RPC error: " + t.getMessage());
        }
        return "";
    }

    private static String getBridgeProxyJs() {
        if (sBridgeProxyJsCache != null) return sBridgeProxyJsCache;
        StringBuilder defs = new StringBuilder();
        defs.append("(function(){");
        defs.append("if(window.__FUSION_GECKO_BRIDGES__)return;");
        defs.append("window.__FUSION_GECKO_BRIDGES__=true;");
        defs.append("function _mk(t,ms){var o={};for(var i=0;i<ms.length;i++){(function(n,r){");
        defs.append("o[n]=function(){var a=Array.prototype.slice.call(arguments);");
        defs.append("var raw=window.prompt(").append(JSONObject.quote(BRIDGE_PROMPT_PREFIX)).append("+JSON.stringify({target:t,method:n,args:a}));");
        defs.append("if(r==='v')return undefined;");
        defs.append("if(r==='b')return raw==='true';");
        defs.append("if(r==='n')return Number(raw)||0;");
        defs.append("return raw==null?'':String(raw);};");
        defs.append("})(ms[i][0],ms[i][1]);}return o;}");
        defs.append("window.GhostBridge=_mk('GhostBridge',").append(describeBridgeMethods(GhostBridgeInterface.class)).append(");");
        defs.append("window.FusionNative=_mk('FusionNative',").append(describeBridgeMethods(FusionNativeBridge.class)).append(");");
        defs.append("window.__FUSION_NATIVE__=window.FusionNative;");
        defs.append("window.__FusionExtensionBridge__=_mk('__FusionExtensionBridge__',").append(describeBridgeMethods(FusionExtensionHelper.FusionExtensionBridge.class)).append(");");
        defs.append("})();");
        sBridgeProxyJsCache = defs.toString();
        return sBridgeProxyJsCache;
    }

    private static String describeBridgeMethods(Class<?> clazz) {
        JSONArray arr = new JSONArray();
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.isAnnotationPresent(JavascriptInterface.class)) continue;
                Class<?> rt = m.getReturnType();
                String code = "s";
                if (rt == void.class || rt == Void.class) code = "v";
                else if (rt == boolean.class || rt == Boolean.class) code = "b";
                else if (rt == int.class || rt == long.class || rt == float.class || rt == double.class || Number.class.isAssignableFrom(rt)) code = "n";
                JSONArray pair = new JSONArray();
                pair.put(m.getName());
                pair.put(code);
                arr.put(pair);
            }
        } catch (Throwable ignored) {}
        return arr.toString();
    }

    private static void attachExtensionBridge(FusionWebViewManager manager, FusionTab tab, GeckoRuntime runtime, GeckoSession session) {
        if (runtime == null || session == null) return;
        try {
            if (sBridgeExtension != null) {
                bindSessionPortDelegate(manager, tab, session, sBridgeExtension);
            } else {
                runtime.getWebExtensionController()
                    .ensureBuiltIn(EXT_URI, EXT_ID)
                    .accept(ext -> {
                        sBridgeExtension = ext;
                        if (ext != null) {
                            attachBackgroundDelegate(ext);
                            bindSessionPortDelegate(manager, tab, session, ext);
                        }
                    }, err -> Log.w(TAG, "Deferred Gecko extension bind failed", err));
            }
        } catch (Throwable t) {
            Log.w(TAG, "attachExtensionBridge error", t);
        }
    }

    private static void bindSessionPortDelegate(FusionWebViewManager manager, FusionTab tab, GeckoSession session, WebExtension ext) {
        if (session == null || ext == null) return;
        session.getWebExtensionController().setMessageDelegate(ext, new WebExtension.MessageDelegate() {
            @Override
            public void onConnect(@NonNull WebExtension.Port port) {
                sSessionPorts.put(session, port);
                flushQueuedScripts(session, port);
                injectScripts(manager, tab, true);
                port.setDelegate(new WebExtension.PortDelegate() {
                    @Override
                    public void onPortMessage(@NonNull Object message, @NonNull WebExtension.Port p) {
                        if (!(message instanceof JSONObject)) return;
                        JSONObject obj = (JSONObject) message;
                        String type = obj.optString("type", "");
                        if ("evalResult".equals(type)) {
                            String reqId = obj.optString("reqId", "");
                            PendingEval pending = sPendingEvals.remove(reqId);
                            if (pending != null) {
                                if (!obj.isNull("error") && obj.optString("error", "").length() > 0) {
                                    pending.value.set(JSONObject.quote("ERROR: " + obj.optString("error")));
                                } else {
                                    pending.value.set(obj.isNull("value") ? "null" : obj.optString("value", "null"));
                                }
                                pending.latch.countDown();
                            }
                        } else if ("domReady".equals(type)) {
                            injectScripts(manager, tab, false);
                        } else if ("console".equals(type)) {
                            final String level = obj.optString("level", "LOG");
                            final String text = obj.optString("text", "");
                            appendConsoleLog(level, text);
                        }
                    }

                    @Override
                    public void onDisconnect(@NonNull WebExtension.Port p) {
                        sSessionPorts.remove(session, p);
                    }
                });
            }
        }, EXT_CONTENT_APP_ID);
    }

    private static void postBootstrap(FusionTab tab, String code) {
        if (tab == null || tab.mGeckoSession == null || code == null || code.isEmpty()) return;
        WebExtension.Port port = sSessionPorts.get(tab.mGeckoSession);
        if (port != null) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", "bootstrap");
                payload.put("code", code);
                port.postMessage(payload);
                return;
            } catch (Throwable ignored) {}
        }
        sQueuedScripts.computeIfAbsent(tab.mGeckoSession, k -> new ArrayList<>()).add(code);
    }

    private static void flushQueuedScripts(GeckoSession session, WebExtension.Port port) {
        List<String> queued = sQueuedScripts.remove(session);
        if (queued == null || queued.isEmpty() || port == null) return;
        for (String code : queued) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", "bootstrap");
                payload.put("code", code);
                port.postMessage(payload);
            } catch (Throwable ignored) {}
        }
    }

    public static void injectScripts(FusionWebViewManager manager, FusionTab tab) {
        injectScripts(manager, tab, false);
    }

    public static void injectScripts(FusionWebViewManager manager, FusionTab tab, boolean isStart) {
        if (manager == null || tab == null || tab.mGeckoSession == null || manager.mActivity == null) return;
        String url = tab.mUrl != null ? tab.mUrl : "";

        StringBuilder bundle = new StringBuilder();
        bundle.append(getBridgeProxyJs()).append("\n");
        bundle.append(manager.buildBaseInjectionJs(tab)).append("\n");

        String fpScript = manager.loadAssetScript("fingerprint_faker.js");
        if (fpScript != null && !fpScript.isEmpty()) {
            bundle.append(fpScript).append("\n");
        }
        String ghostScript = manager.loadAssetScript("ghost_injected.js");
        if (ghostScript != null && !ghostScript.isEmpty()) {
            bundle.append(ghostScript).append("\n");
        }
        postBootstrap(tab, bundle.toString());

        Context ctx = manager.mActivity;
        if (!url.isEmpty()) {
            FusionExtensionHelper.injectMatchingScripts(ctx, url, isStart ? "document_start" : "document_idle",
                js -> postBootstrap(tab, js));
            if (!isStart) {
                FusionUserscriptHelper.injectMatchingScripts(ctx, url, js -> postBootstrap(tab, js));
            }
        }
    }

    private static void appendConsoleLog(String level, String text) {
        if (text == null || text.isEmpty()) return;
        final String msg = "[" + level + "] " + text;
        final long ts = System.currentTimeMillis();
        Log.d("FusionJS", msg + " (Gecko)");
        sConsoleLogExecutor.execute(() -> {
            try {
                File logFile = new File("/tmp/webview_console.log");
                if (logFile.exists() && logFile.length() > 5 * 1024 * 1024) {
                    logFile.delete();
                }
                try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                    String line = ts + " " + msg + "\n";
                    fos.write(line.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            } catch (Throwable ignored) {}
        });
    }

    public static void applyBlockers(boolean blockMedia, boolean blockFonts, boolean blockCss, boolean blockTrackers) {
        GeckoRuntime rt = sRuntime;
        if (rt != null) {
            ensureBridgeExtension(rt);
        }
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "setBlockers");
            payload.put("media", blockMedia);
            payload.put("fonts", blockFonts);
            payload.put("css", blockCss);
            payload.put("trackers", blockTrackers);
            sPendingBlockerPayload = payload;
            WebExtension.Port bg = sBgPort;
            if (bg != null) {
                bg.postMessage(payload);
            }
        } catch (Throwable ignored) {}
    }

    public static void applyProxy(FusionTab tab) {
        GeckoRuntime rt = sRuntime;
        if (rt != null) {
            ensureBridgeExtension(rt);
        }
        JSONObject payload = new JSONObject();
        try {
            if (tab != null && tab.mIsProxyEnabled && tab.mProxyItem != null && tab.mProxyItem.isValid()) {
                String host = tab.mProxyItem.host;
                int port = tab.mProxyItem.port;
                String scheme = tab.mProxyItem.scheme != null ? tab.mProxyItem.scheme : "http";
                String user = tab.mProxyItem.user != null ? tab.mProxyItem.user : "";
                String pass = tab.mProxyItem.pass != null ? tab.mProxyItem.pass : "";
                if ("socks5".equalsIgnoreCase(scheme) && tab.mProxyItem.hasAuth()) {
                    int localPort = FusionSocksTunnel.ensureTunnel(host, port, user, pass);
                    if (localPort > 0) {
                        host = "127.0.0.1";
                        port = localPort;
                        scheme = "http";
                        user = "";
                        pass = "";
                    }
                }
                payload.put("type", "setProxy");
                payload.put("enabled", true);
                payload.put("scheme", scheme);
                payload.put("host", host);
                payload.put("port", port);
                payload.put("user", user);
                payload.put("pass", pass);
            } else {
                payload.put("type", "setProxy");
                payload.put("enabled", false);
            }
            sPendingProxyPayload = payload;
            WebExtension.Port bg = sBgPort;
            if (bg != null) {
                bg.postMessage(payload);
            }
        } catch (Throwable t) {
            Log.w(TAG, "applyProxy error", t);
        }
    }

    public static void loadUrl(FusionTab tab, String url) {
        if (tab == null || tab.mGeckoSession == null || url == null || url.isEmpty()) return;
        tab.mUrl = url;
        tab.mGeckoSession.loadUri(url);
    }

    public static void reload(FusionTab tab) {
        if (tab == null || tab.mGeckoSession == null) return;
        tab.mGeckoSession.reload();
    }

    public static void goBack(FusionTab tab) {
        if (tab == null || tab.mGeckoSession == null) return;
        if (tab.mGeckoCanGoBack) {
            tab.mGeckoSession.goBack();
        } else {
            evaluateJs(tab, "window.history.back();");
        }
    }

    public static void goForward(FusionTab tab) {
        if (tab == null || tab.mGeckoSession == null) return;
        if (tab.mGeckoCanGoForward) {
            tab.mGeckoSession.goForward();
        } else {
            evaluateJs(tab, "window.history.forward();");
        }
    }

    public static void setActive(FusionTab tab, boolean active) {
        if (tab == null || tab.mGeckoSession == null) return;
        try {
            tab.mGeckoSession.setActive(active);
        } catch (Throwable ignored) {}
    }

    public static void setDesktopMode(FusionTab tab, boolean isDesktop) {
        if (tab == null || tab.mGeckoSession == null) return;
        tab.mIsDesktop = isDesktop;
        GeckoSessionSettings settings = tab.mGeckoSession.getSettings();
        settings.setUserAgentMode(isDesktop ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP : GeckoSessionSettings.USER_AGENT_MODE_MOBILE);
        settings.setViewportMode(isDesktop ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP : GeckoSessionSettings.VIEWPORT_MODE_MOBILE);
        tab.mGeckoSession.reload();
    }

    public static void clearSessionData(FusionTab tab) {
        if (tab == null || tab.mGeckoSession == null) return;
        evaluateJs(tab,
            "(function(){try{var sPreserved={};for(var i=0;i<sessionStorage.length;i++){var k=sessionStorage.key(i);if(k&&(k.startsWith('__agy_')||k.startsWith('__fusion_')))sPreserved[k]=sessionStorage.getItem(k);}sessionStorage.clear();for(var pk in sPreserved)sessionStorage.setItem(pk,sPreserved[pk]);var lPreserved={};for(var j=0;j<localStorage.length;j++){var lk=localStorage.key(j);if(lk&&(lk.startsWith('__agy_')||lk.startsWith('__fusion_')||lk.startsWith('fusion_')||lk.startsWith('agy_')))lPreserved[lk]=localStorage.getItem(lk);}localStorage.clear();for(var plk in lPreserved)localStorage.setItem(plk,lPreserved[plk]);var cookies=document.cookie.split(';');for(var c=0;c<cookies.length;c++){var eq=cookies[c].indexOf('=');var name=eq>-1?cookies[c].substr(0,eq).trim():cookies[c].trim();if(name)document.cookie=name+'=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/';}}catch(e){}})();"
        );
    }

    public static String captureScreenshot(FusionTab tab, String outPath, AppCompatActivity activity) {
        if (tab == null || tab.mGeckoView == null || activity == null || outPath == null || outPath.trim().isEmpty()) {
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> resRef = new AtomicReference<>(null);
        activity.runOnUiThread(() -> {
            try {
                tab.mGeckoView.capturePixels().accept(bmp -> {
                    try {
                        if (bmp != null) {
                            File outFile = new File(outPath.trim());
                            if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                                outFile.getParentFile().mkdirs();
                            }
                            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                                bmp.compress(Bitmap.CompressFormat.PNG, 92, fos);
                                fos.flush();
                            }
                            resRef.set(outFile.getAbsolutePath());
                        }
                    } catch (Throwable ignored) {
                    } finally {
                        latch.countDown();
                    }
                }, err -> latch.countDown());
            } catch (Throwable ignored) {
                latch.countDown();
            }
        });
        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}
        return resRef.get();
    }

    public static void evaluateJs(FusionTab tab, String code) {
        if (tab == null || tab.mGeckoSession == null || code == null) return;
        WebExtension.Port port = sSessionPorts.get(tab.mGeckoSession);
        if (port != null) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", "eval");
                payload.put("code", code);
                port.postMessage(payload);
                return;
            } catch (Throwable ignored) {}
        }
        sQueuedScripts.computeIfAbsent(tab.mGeckoSession, k -> new ArrayList<>()).add(code);
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (sSessionPorts.get(tab.mGeckoSession) == null) {
                    tab.mGeckoSession.loadUri("javascript:(function(){" + code + "})();");
                }
            } catch (Throwable ignored) {}
        });
    }

    public static String evaluateJsSync(FusionTab tab, String code, long timeoutMs) {
        if (tab == null || tab.mGeckoSession == null || code == null) return null;
        WebExtension.Port port = sSessionPorts.get(tab.mGeckoSession);
        if (port == null) {
            return null;
        }
        String reqId = UUID.randomUUID().toString();
        PendingEval pending = new PendingEval();
        sPendingEvals.put(reqId, pending);
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "eval");
            payload.put("reqId", reqId);
            payload.put("code", code);
            port.postMessage(payload);
            pending.latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Throwable ignored) {
        } finally {
            sPendingEvals.remove(reqId);
        }
        return pending.value.get();
    }

    public static void destroyTab(FusionTab tab, ViewGroup host) {
        if (tab == null) return;
        GeckoView gv = tab.mGeckoView;
        if (gv != null) {
            try {
                gv.setVisibility(View.GONE);
                gv.releaseSession();
            } catch (Throwable ignored) {}
            ViewParent p = gv.getParent();
            if (p instanceof ViewGroup) {
                ((ViewGroup) p).removeView(gv);
            } else if (host != null) {
                host.removeView(gv);
            }
            tab.mGeckoView = null;
        }
        GeckoSession session = tab.mGeckoSession;
        if (session != null) {
            sSessionPorts.remove(session);
            sQueuedScripts.remove(session);
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (Throwable ignored) {}
            tab.mGeckoSession = null;
        }
    }

    public static void notifyEngineToast(AppCompatActivity act, String msg) {
        if (act != null && msg != null) {
            act.runOnUiThread(() -> FusionToast.makeText(act, msg, 0).show());
        }
    }
}
