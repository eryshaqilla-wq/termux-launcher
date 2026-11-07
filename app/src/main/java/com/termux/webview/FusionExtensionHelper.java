package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.termux.app.FusionToast;
import com.termux.app.TermuxActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * FusionExtensionHelper
 * Kiwi-Style Chrome Extension Engine untuk Fusion WebView.
 * Mendukung instalasi paket ekstensi .zip & .crx (Manifest V2 & V3),
 * eksekusi headless background worker (service_worker & background scripts),
 * bi-directional port & message routing (chrome.runtime.connect & sendMessage),
 * isolated storage per ekstensi, injeksi content scripts (JS & CSS) level document_start,
 * skema url chrome-extension://, serta proteksi keamanan storage Android root & non-root.
 */
public class FusionExtensionHelper {
    private static final String TAG = "FusionExtension";
    public static final int REQUEST_CODE_PICK_EXTENSION = 4725;
    private static final String PREF_NAME = "fusion_extension_prefs";
    private static final String KEY_STORAGE_CACHE = "ext_storage_json";
    private static String sStorageCache = null;

    public static final Map<String, WebView> sBackgroundWebViews = new ConcurrentHashMap<>();
    public static final Map<String, ExtensionInfo> sBackgroundExtMap = new ConcurrentHashMap<>();
    public static final Map<String, List<Runnable>> sPendingBgActions = new ConcurrentHashMap<>();
    public static final Set<WebView> sActiveTabWebViews = Collections.newSetFromMap(new WeakHashMap<>());
    public static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static class ExtensionInfo {
        public String id;
        public File dir;
        public String name;
        public String version;
        public String description;
        public boolean enabled = true;
        public int manifestVersion = 3;
        public String popupHtml;
        public String iconPath;
        public String serviceWorker;                              // MV3 service worker
        public List<String> backgroundScripts = new ArrayList<>(); // MV2 scripts
        public String backgroundPage;                             // MV2 page
        public String backgroundType;                             // "module" or null
        public JSONObject manifestJson;                           // Raw parsed manifest
        public List<ContentScript> contentScripts = new ArrayList<>();

        public static class ContentScript {
            public List<String> matches = new ArrayList<>();
            public List<String> jsFiles = new ArrayList<>();
            public List<String> cssFiles = new ArrayList<>();
            public String runAt = "document_idle";
            public boolean allFrames = false;
            public String world = "ISOLATED";
        }

        public boolean hasBackground() {
            return (serviceWorker != null && !serviceWorker.isEmpty())
                    || (!backgroundScripts.isEmpty())
                    || (backgroundPage != null && !backgroundPage.isEmpty());
        }
    }

    public static class FusionExtensionBridge {
        private final Context mContext;
        private final WebView mWebView;
        private final String mBoundExtId;
        private final boolean mIsBackground;

        public FusionExtensionBridge(Context context) {
            this(context, null, null, false);
        }

        public FusionExtensionBridge(Context context, WebView webView) {
            this(context, webView, null, false);
        }

        public FusionExtensionBridge(Context context, String boundExtId, boolean isBackground) {
            this(context, null, boundExtId, isBackground);
        }

        public FusionExtensionBridge(Context context, WebView webView, String boundExtId, boolean isBackground) {
            this.mContext = context;
            this.mWebView = webView;
            this.mBoundExtId = boundExtId;
            this.mIsBackground = isBackground;
        }

        @JavascriptInterface
        public String getStorage() {
            return FusionExtensionHelper.getStorageJson(mContext, mBoundExtId);
        }

        @JavascriptInterface
        public String getStorage(String extId) {
            String target = (extId != null && !extId.isEmpty()) ? extId : mBoundExtId;
            return FusionExtensionHelper.getStorageJson(mContext, target);
        }

        @JavascriptInterface
        public void setStorage(String jsonStr) {
            setStorage(mBoundExtId, jsonStr);
        }

        @JavascriptInterface
        public void setStorage(String extId, String jsonStr) {
            String target = (extId != null && !extId.isEmpty()) ? extId : mBoundExtId;
            FusionExtensionHelper.saveStorageJson(mContext, target, jsonStr);
            sMainHandler.post(() -> {
                String script = "if (window.__fusionNotifyStorageChanged) { window.__fusionNotifyStorageChanged(" +
                        JSONObject.quote(target) + ", " + JSONObject.quote(jsonStr) + "); }";
                for (WebView wv : sActiveTabWebViews) {
                    if (wv != null && wv != mWebView) wv.evaluateJavascript(script, null);
                }
                WebView bgWv = findBackgroundWebView(target);
                if (bgWv != null && bgWv != mWebView) bgWv.evaluateJavascript(script, null);
            });
        }

        @JavascriptInterface
        public String getLayer() {
            return FusionExtensionHelper.getLayerConfig(mContext);
        }

        @JavascriptInterface
        public void setLayer(String layer) {
            FusionExtensionHelper.setLayerConfig(mContext, layer);
        }

        @JavascriptInterface
        public void log(String msg) {
            Log.i(TAG, "[Extension JS Log]: " + msg);
        }

        @JavascriptInterface
        public void connectPort(final String targetExtId, final String portId, final String connectInfoJson) {
            sMainHandler.post(() -> {
                try {
                    String extId = (targetExtId != null && !targetExtId.isEmpty()) ? targetExtId : mBoundExtId;
                    if (extId == null || extId.isEmpty()) return;

                    ensureBackgroundHostRunning(mContext, extId);
                    WebView bgWv = findBackgroundWebView(extId);
                    if (bgWv != null) {
                        String senderId = (mBoundExtId != null && !mBoundExtId.isEmpty()) ? mBoundExtId : extId;
                        String script = "if (window.__fusionIncomingPort) { window.__fusionIncomingPort(" +
                                JSONObject.quote(portId) + ", " +
                                JSONObject.quote(senderId) + ", " +
                                JSONObject.quote(connectInfoJson != null ? connectInfoJson : "{}") + "); }";
                        bgWv.evaluateJavascript(script, null);
                    } else {
                        List<Runnable> pending = sPendingBgActions.computeIfAbsent(extId, k -> new ArrayList<>());
                        pending.add(() -> connectPort(targetExtId, portId, connectInfoJson));
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error in connectPort: " + e.getMessage(), e);
                }
            });
        }

        @JavascriptInterface
        public void postPortMessage(final String extId, final String portId, final String msgJson, final boolean fromBg) {
            sMainHandler.post(() -> {
                try {
                    String script = "if (window.__fusionReceivePortMessage) { window.__fusionReceivePortMessage(" +
                            JSONObject.quote(portId) + ", " +
                            JSONObject.quote(msgJson != null ? msgJson : "{}") + "); }";
                    if (fromBg) {
                        for (WebView wv : sActiveTabWebViews) {
                            if (wv != null) {
                                wv.evaluateJavascript(script, null);
                            }
                        }
                    } else {
                        WebView bgWv = findBackgroundWebView(extId);
                        if (bgWv != null) {
                            bgWv.evaluateJavascript(script, null);
                        } else {
                            List<Runnable> pending = sPendingBgActions.computeIfAbsent(extId, k -> new ArrayList<>());
                            pending.add(() -> postPortMessage(extId, portId, msgJson, fromBg));
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error in postPortMessage: " + e.getMessage(), e);
                }
            });
        }

        @JavascriptInterface
        public void disconnectPort(final String extId, final String portId, final boolean fromBg) {
            sMainHandler.post(() -> {
                try {
                    String script = "if (window.__fusionDisconnectPort) { window.__fusionDisconnectPort(" +
                            JSONObject.quote(portId) + "); }";
                    if (fromBg) {
                        for (WebView wv : sActiveTabWebViews) {
                            if (wv != null) wv.evaluateJavascript(script, null);
                        }
                    } else {
                        WebView bgWv = findBackgroundWebView(extId);
                        if (bgWv != null) bgWv.evaluateJavascript(script, null);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error in disconnectPort: " + e.getMessage(), e);
                }
            });
        }

        @JavascriptInterface
        public void sendRuntimeMessage(final String targetExtId, final String msgId, final String msgJson) {
            sMainHandler.post(() -> {
                try {
                    String extId = (targetExtId != null && !targetExtId.isEmpty()) ? targetExtId : mBoundExtId;
                    if (extId == null || extId.isEmpty()) return;

                    ensureBackgroundHostRunning(mContext, extId);
                    String senderId = (mBoundExtId != null && !mBoundExtId.isEmpty()) ? mBoundExtId : extId;
                    String script = "if (window.__fusionIncomingMessage) { window.__fusionIncomingMessage(" +
                            JSONObject.quote(msgId) + ", " +
                            JSONObject.quote(senderId) + ", " +
                            JSONObject.quote(msgJson != null ? msgJson : "{}") + "); }";

                    WebView bgWv = findBackgroundWebView(extId);
                    if (bgWv != null && bgWv != mWebView) {
                        bgWv.evaluateJavascript(script, null);
                    }
                    for (WebView wv : sActiveTabWebViews) {
                        if (wv != null && wv != mWebView) {
                            wv.evaluateJavascript(script, null);
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error in sendRuntimeMessage: " + e.getMessage(), e);
                }
            });
        }

        @JavascriptInterface
        public void sendRuntimeResponse(final String extId, final String msgId, final String resJson) {
            sMainHandler.post(() -> {
                try {
                    String script = "if (window.__fusionReceiveRuntimeResponse) { window.__fusionReceiveRuntimeResponse(" +
                            JSONObject.quote(msgId) + ", " +
                            JSONObject.quote(resJson != null ? resJson : "null") + "); }";
                    for (WebView wv : sActiveTabWebViews) {
                        if (wv != null) wv.evaluateJavascript(script, null);
                    }
                    WebView bgWv = findBackgroundWebView(extId);
                    if (bgWv != null) bgWv.evaluateJavascript(script, null);
                } catch (Throwable e) {
                    Log.e(TAG, "Error in sendRuntimeResponse: " + e.getMessage(), e);
                }
            });
        }

        @JavascriptInterface
        public void openWindow(final String extId, final String url) {
            if (url == null || url.isEmpty()) return;
            sMainHandler.post(() -> {
                try {
                    FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                    if (mgr != null) {
                        String targetUrl = url;
                        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                            ExtensionInfo ext = findExtensionByIdOrHost(mContext, extId);
                            if (ext != null && ext.dir != null) {
                                targetUrl = "file://" + new File(ext.dir, url).getAbsolutePath();
                            }
                        }
                        mgr.addNewTab(targetUrl);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "Error in openWindow: " + e.getMessage(), e);
                }
            });
        }
    }

    public static WebView findBackgroundWebView(String extId) {
        if (extId == null || extId.isEmpty()) return null;
        WebView wv = sBackgroundWebViews.get(extId);
        if (wv != null) return wv;
        for (Map.Entry<String, WebView> entry : sBackgroundWebViews.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(extId) || entry.getKey().contains(extId) || extId.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static File getPrimaryExtensionsDir(Context context) {
        String[] candidatePaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/extensions",
            "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
            "/root/.fusion/extensions",
            "/data/data/com.termux/files/home/wallet/extensions",
            "/root/wallet/extensions"
        };
        for (String p : candidatePaths) {
            File d = new File(p);
            if (!d.exists()) {
                try { d.mkdirs(); } catch (Throwable ignored) {}
            }
            if (d.exists() && d.isDirectory()) {
                try {
                    d.setReadable(true, false);
                    d.setWritable(true, false);
                    d.setExecutable(true, false);
                } catch (Throwable ignored) {}
                if (d.canWrite()) {
                    return d;
                }
            }
        }
        if (context != null) {
            File fallback = new File(context.getFilesDir(), "fusion_extensions");
            if (!fallback.exists()) fallback.mkdirs();
            try {
                fallback.setReadable(true, false);
                fallback.setWritable(true, false);
                fallback.setExecutable(true, false);
            } catch (Throwable ignored) {}
            return fallback;
        }
        return new File("/data/data/com.termux/files/home/.fusion/extensions");
    }

    public static File getStorageFile(Context context) {
        return getStorageFile(context, null);
    }

    public static File getStorageFile(Context context, String extId) {
        File dir = getPrimaryExtensionsDir(context);
        if (extId == null || extId.isEmpty() || "global".equalsIgnoreCase(extId)
                || extId.toLowerCase().contains("google_ai_pro") || extId.toLowerCase().contains("gapt")) {
            return new File(dir, "storage.json");
        }
        String safeName = extId.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(dir, "storage_" + safeName + ".json");
    }

    public static synchronized String getStorageJson(Context context) {
        return getStorageJson(context, null);
    }

    public static synchronized String getStorageJson(Context context, String extId) {
        if (extId == null || extId.isEmpty() || "global".equalsIgnoreCase(extId)
                || extId.toLowerCase().contains("google_ai_pro") || extId.toLowerCase().contains("gapt")) {
            if (sStorageCache != null && !sStorageCache.isEmpty()) {
                return sStorageCache;
            }
        }
        File f = getStorageFile(context, extId);
        if (f.exists() && f.isFile()) {
            try {
                byte[] bytes = readFileBytesSafe(f);
                if (bytes != null && bytes.length > 0) {
                    String s = new String(bytes, StandardCharsets.UTF_8).trim();
                    if (!s.isEmpty()) {
                        if (f.getName().equals("storage.json")) sStorageCache = s;
                        return s;
                    }
                }
            } catch (Throwable ignored) {}
        }
        if (f.getName().equals("storage.json")) {
            if (context != null) {
                SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String saved = sp.getString(KEY_STORAGE_CACHE, null);
                if (saved != null && !saved.isEmpty()) {
                    sStorageCache = saved;
                    return sStorageCache;
                }
            }
            sStorageCache = "{\"gapt_layer\":\"12m\"}";
            saveStorageJson(context, extId, sStorageCache);
            return sStorageCache;
        }
        return "{}";
    }

    public static synchronized void saveStorageJson(Context context, String jsonStr) {
        saveStorageJson(context, null, jsonStr);
    }

    public static synchronized void saveStorageJson(Context context, String extId, String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            jsonStr = "{}";
        }
        File f = getStorageFile(context, extId);
        if (f.getName().equals("storage.json")) {
            sStorageCache = jsonStr;
        }
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            }
            try { f.setReadable(true, false); } catch (Throwable ignored) {}
            try { f.setWritable(true, false); } catch (Throwable ignored) {}
        } catch (Throwable e) {
            Log.w(TAG, "Standard save " + f.getName() + " failed: " + e.getMessage() + ", trying su write...");
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo '" + jsonStr.replace("'", "'\''") + "' > '" + f.getAbsolutePath() + "' && chmod 777 '" + f.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }
        if (context != null && f.getName().equals("storage.json")) {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_STORAGE_CACHE, jsonStr).apply();
            } catch (Throwable ignored) {}
        }
    }

    public static String getLayerConfig(Context context) {
        try {
            String raw = getStorageJson(context);
            JSONObject obj = new JSONObject(raw);
            return obj.optString("gapt_layer", "12m");
        } catch (Throwable ignored) {}
        return "12m";
    }

    public static void setLayerConfig(Context context, String layer) {
        if (layer == null || layer.isEmpty()) layer = "12m";
        try {
            String raw = getStorageJson(context);
            JSONObject obj = new JSONObject(raw);
            obj.put("gapt_layer", layer);
            saveStorageJson(context, obj.toString());
        } catch (Throwable e) {
            saveStorageJson(context, "{\"gapt_layer\":\"" + layer + "\"}");
        }
    }

    public static File getStateFile(Context context) {
        File dir = getPrimaryExtensionsDir(context);
        return new File(dir, "extensions_state.json");
    }

    public static Map<String, Boolean> loadExtensionsState(Context context) {
        Map<String, Boolean> stateMap = new HashMap<>();
        File f = getStateFile(context);
        if (f.exists() && f.isFile()) {
            try {
                byte[] bytes = readFileBytesSafe(f);
                if (bytes != null && bytes.length > 0) {
                    JSONObject obj = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        stateMap.put(k, obj.optBoolean(k, true));
                    }
                }
            } catch (Throwable ignored) {}
        }
        return stateMap;
    }

    public static void saveExtensionsState(Context context, Map<String, Boolean> stateMap) {
        File f = getStateFile(context);
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, Boolean> entry : stateMap.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
            String content = obj.toString(2);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            try { f.setReadable(true, false); } catch (Throwable ignored) {}
            try { f.setWritable(true, false); } catch (Throwable ignored) {}
        } catch (Throwable e) {
            Log.w(TAG, "Standard save extensions_state.json failed: " + e.getMessage());
            try {
                JSONObject obj = new JSONObject();
                for (Map.Entry<String, Boolean> entry : stateMap.entrySet()) {
                    obj.put(entry.getKey(), entry.getValue());
                }
                String content = obj.toString(2);
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo '" + content.replace("'", "'\''") + "' > '" + f.getAbsolutePath() + "' && chmod 777 '" + f.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }
    }

    public static List<ExtensionInfo> getInstalledExtensions(Context context) {
        List<ExtensionInfo> list = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        String[] scanPaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/extensions",
            "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
            "/root/.fusion/extensions"
        };
        if (context != null) {
            File localDir = new File(context.getFilesDir(), "fusion_extensions");
            if (localDir.exists()) {
                scanCandidateExtensionsInDir(context, localDir, list, seenIds);
            }
        }

        for (String p : scanPaths) {
            File d = new File(p);
            if (d.exists() && d.isDirectory()) {
                scanCandidateExtensionsInDir(context, d, list, seenIds);
            }
        }

        return list;
    }

    private static void scanCandidateExtensionsInDir(Context context, File extDir, List<ExtensionInfo> list, Set<String> seenIds) {
        Map<String, Boolean> stateMap = loadExtensionsState(context);
        File[] subdirs = extDir.listFiles();
        if (subdirs == null) return;

        for (File d : subdirs) {
            if (d.isDirectory() && !d.getName().startsWith(".")) {
                File manifest = new File(d, "manifest.json");
                if (manifest.exists() && manifest.isFile()) {
                    if (!seenIds.contains(d.getName())) {
                        ExtensionInfo info = parseExtensionDir(d);
                        if (info != null) {
                            info.enabled = stateMap.containsKey(info.id) ? Boolean.TRUE.equals(stateMap.get(info.id)) : true;
                            list.add(info);
                            seenIds.add(info.id);
                        }
                    }
                }
            }
        }
    }

    public static ExtensionInfo findExtensionById(Context context, String extId) {
        return findExtensionByIdOrHost(context, extId);
    }

    public static ExtensionInfo findExtensionByIdOrHost(Context context, String idOrHost) {
        if (idOrHost == null || idOrHost.trim().isEmpty()) return null;
        String needle = idOrHost.trim().toLowerCase();
        List<ExtensionInfo> exts = getInstalledExtensions(context);
        for (ExtensionInfo ext : exts) {
            if (ext.id != null && ext.id.equalsIgnoreCase(needle)) return ext;
            if (ext.dir != null && ext.dir.getName().equalsIgnoreCase(needle)) return ext;
        }
        for (ExtensionInfo ext : exts) {
            if (ext.name != null && ext.name.equalsIgnoreCase(needle)) return ext;
        }
        for (ExtensionInfo ext : exts) {
            if (ext.id != null && (ext.id.toLowerCase().contains(needle) || needle.contains(ext.id.toLowerCase()))) return ext;
            if (ext.name != null && (ext.name.toLowerCase().contains(needle) || needle.contains(ext.name.toLowerCase()))) return ext;
        }
        return null;
    }

    public static ExtensionInfo findExtensionFromUrl(Context context, String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("chrome-extension://")) {
            try {
                String host = Uri.parse(url).getHost();
                return findExtensionByIdOrHost(context, host);
            } catch (Throwable ignored) {}
        }
        if (url.startsWith("file://") && url.contains(".fusion/extensions/")) {
            try {
                String path = Uri.parse(url).getPath();
                if (path != null) {
                    File f = new File(path);
                    File extDir = f.isDirectory() ? f : f.getParentFile();
                    while (extDir != null && extDir.exists() && !new File(extDir, "manifest.json").exists()) {
                        if (!extDir.getAbsolutePath().contains(".fusion/extensions")) break;
                        extDir = extDir.getParentFile();
                    }
                    if (extDir != null && new File(extDir, "manifest.json").exists()) {
                        return parseExtensionDir(extDir);
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static String resolveI18nString(File dir, String raw, String defaultLocale) {
        if (raw == null || raw.isEmpty()) return raw;
        String trimmed = raw.trim();
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("__msg_") || !lower.endsWith("__") || trimmed.length() <= 8) {
            return raw;
        }

        String key = trimmed.substring(6, trimmed.length() - 2).trim();
        if (key.isEmpty() || dir == null || !dir.exists()) return raw;

        File localesDir = new File(dir, "_locales");
        if (!localesDir.exists() || !localesDir.isDirectory()) return raw;

        List<String> localeCandidates = new ArrayList<>();
        if (defaultLocale != null && !defaultLocale.isEmpty()) {
            localeCandidates.add(defaultLocale);
        }
        if (!localeCandidates.contains("en")) localeCandidates.add("en");
        if (!localeCandidates.contains("en_US")) localeCandidates.add("en_US");
        if (!localeCandidates.contains("id")) localeCandidates.add("id");

        File messagesFile = null;
        for (String loc : localeCandidates) {
            File f = new File(new File(localesDir, loc), "messages.json");
            if (f.exists() && f.isFile()) {
                messagesFile = f;
                break;
            }
        }

        if (messagesFile == null) {
            File[] subDirs = localesDir.listFiles();
            if (subDirs != null) {
                for (File sd : subDirs) {
                    if (sd.isDirectory()) {
                        File f = new File(sd, "messages.json");
                        if (f.exists() && f.isFile()) {
                            messagesFile = f;
                            break;
                        }
                    }
                }
            }
        }

        if (messagesFile == null) return raw;

        try {
            byte[] bytes = readFileBytesSafe(messagesFile);
            if (bytes == null || bytes.length == 0) return raw;
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);

            if (root.has(key)) {
                JSONObject msgObj = root.optJSONObject(key);
                if (msgObj != null && msgObj.has("message")) {
                    String msg = msgObj.optString("message", "");
                    if (!msg.isEmpty()) return msg;
                }
            }

            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                if (k.equalsIgnoreCase(key)) {
                    JSONObject msgObj = root.optJSONObject(k);
                    if (msgObj != null && msgObj.has("message")) {
                        String msg = msgObj.optString("message", "");
                        if (!msg.isEmpty()) return msg;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return raw;
    }

    public static ExtensionInfo parseExtensionDir(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return null;
        File manifestFile = new File(dir, "manifest.json");
        if (!manifestFile.exists() || !manifestFile.isFile()) return null;

        try {
            byte[] bytes = readFileBytesSafe(manifestFile);
            if (bytes == null || bytes.length == 0) return null;

            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);

            ExtensionInfo info = new ExtensionInfo();
            info.id = dir.getName();
            info.dir = dir;
            info.manifestJson = root;
            String defaultLocale = root.optString("default_locale", "en");
            info.name = resolveI18nString(dir, root.optString("name", dir.getName()), defaultLocale);
            info.version = root.optString("version", "1.0.0");
            info.description = resolveI18nString(dir, root.optString("description", ""), defaultLocale);
            info.manifestVersion = root.optInt("manifest_version", 3);

            String[] iconSizes = new String[]{"512", "256", "128", "64", "48", "38", "32", "19", "16"};

            if (root.has("action")) {
                JSONObject action = root.optJSONObject("action");
                if (action != null) {
                    info.popupHtml = action.optString("default_popup", null);
                    Object defIcon = action.opt("default_icon");
                    if (defIcon instanceof String) {
                        info.iconPath = (String) defIcon;
                    } else if (defIcon instanceof JSONObject) {
                        JSONObject iconObj = (JSONObject) defIcon;
                        for (String sz : iconSizes) {
                            if (iconObj.has(sz)) {
                                info.iconPath = iconObj.optString(sz, null);
                                break;
                            }
                        }
                    }
                }
            } else if (root.has("browser_action")) {
                JSONObject ba = root.optJSONObject("browser_action");
                if (ba != null) {
                    info.popupHtml = ba.optString("default_popup", null);
                    Object defIcon = ba.opt("default_icon");
                    if (defIcon instanceof String) {
                        info.iconPath = (String) defIcon;
                    } else if (defIcon instanceof JSONObject) {
                        JSONObject iconObj = (JSONObject) defIcon;
                        for (String sz : iconSizes) {
                            if (iconObj.has(sz)) {
                                info.iconPath = iconObj.optString(sz, null);
                                break;
                            }
                        }
                    }
                }
            }

            if (info.iconPath == null && root.has("icons")) {
                JSONObject icons = root.optJSONObject("icons");
                if (icons != null) {
                    for (String sz : iconSizes) {
                        if (icons.has(sz)) {
                            info.iconPath = icons.optString(sz, null);
                            break;
                        }
                    }
                }
            }

            // Normalisasi popupHtml jika mengarah ke popup-init.html atau berkas tidak ada
            if ("popup-init.html".equalsIgnoreCase(info.popupHtml) && new File(dir, "popup.html").exists()) {
                info.popupHtml = "popup.html";
            } else if (info.popupHtml == null || info.popupHtml.isEmpty() || !new File(dir, info.popupHtml).exists()) {
                if (new File(dir, "popup.html").exists()) {
                    info.popupHtml = "popup.html";
                } else if (new File(dir, "home.html").exists()) {
                    info.popupHtml = "home.html";
                } else if (new File(dir, "index.html").exists()) {
                    info.popupHtml = "index.html";
                }
            }

            // Parse background worker / scripts
            if (root.has("background")) {
                Object bgObj = root.opt("background");
                if (bgObj instanceof JSONObject) {
                    JSONObject bg = (JSONObject) bgObj;
                    info.serviceWorker = bg.optString("service_worker", null);
                    info.backgroundType = bg.optString("type", null);
                    info.backgroundPage = bg.optString("page", null);
                    JSONArray scripts = bg.optJSONArray("scripts");
                    if (scripts != null) {
                        for (int i = 0; i < scripts.length(); i++) {
                            String s = scripts.optString(i, null);
                            if (s != null && !s.isEmpty()) info.backgroundScripts.add(s);
                        }
                    }
                }
            }

            JSONArray csArray = root.optJSONArray("content_scripts");
            if (csArray != null) {
                for (int i = 0; i < csArray.length(); i++) {
                    JSONObject csObj = csArray.optJSONObject(i);
                    if (csObj == null) continue;
                    ExtensionInfo.ContentScript cs = new ExtensionInfo.ContentScript();
                    JSONArray matches = csObj.optJSONArray("matches");
                    if (matches != null) {
                        for (int j = 0; j < matches.length(); j++) {
                            String m = matches.optString(j, null);
                            if (m != null && !m.isEmpty()) cs.matches.add(m);
                        }
                    }
                    JSONArray jsArr = csObj.optJSONArray("js");
                    if (jsArr != null) {
                        for (int j = 0; j < jsArr.length(); j++) {
                            String jsPath = jsArr.optString(j, null);
                            if (jsPath != null && !jsPath.isEmpty()) cs.jsFiles.add(jsPath);
                        }
                    }
                    JSONArray cssArr = csObj.optJSONArray("css");
                    if (cssArr != null) {
                        for (int j = 0; j < cssArr.length(); j++) {
                            String cssPath = cssArr.optString(j, null);
                            if (cssPath != null && !cssPath.isEmpty()) cs.cssFiles.add(cssPath);
                        }
                    }
                    cs.runAt = csObj.optString("run_at", "document_idle");
                    cs.allFrames = csObj.optBoolean("all_frames", false);
                    cs.world = csObj.optString("world", "ISOLATED");
                    info.contentScripts.add(cs);
                }
            }
            return info;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal mem-parsing manifest di " + dir.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }
    }

    public static Bitmap getExtensionIcon(ExtensionInfo ext, int targetSizePx) {
        if (ext == null || ext.dir == null) return null;
        File iconFile = null;
        if (ext.iconPath != null && !ext.iconPath.isEmpty()) {
            File f = new File(ext.dir, ext.iconPath);
            if (f.exists() && f.isFile()) {
                iconFile = f;
            }
        }
        if (iconFile == null) {
            String[] commonNames = new String[]{"icon.png", "icon128.png", "icon48.png", "icon-128.png", "icon-48.png", "logo.png", "icon.webp"};
            for (String cn : commonNames) {
                File f = new File(ext.dir, cn);
                if (f.exists() && f.isFile()) {
                    iconFile = f;
                    break;
                }
                File sub = new File(new File(ext.dir, "images"), cn);
                if (sub.exists() && sub.isFile()) {
                    iconFile = sub;
                    break;
                }
                File sub2 = new File(new File(ext.dir, "icons"), cn);
                if (sub2.exists() && sub2.isFile()) {
                    iconFile = sub2;
                    break;
                }
            }
        }
        if (iconFile != null) {
            try {
                byte[] bytes = readFileBytesSafe(iconFile);
                if (bytes != null && bytes.length > 0) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                    opts.inSampleSize = 1;
                    if (targetSizePx > 0 && opts.outWidth > targetSizePx) {
                        opts.inSampleSize = Math.max(1, opts.outWidth / targetSizePx);
                    }
                    opts.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                    if (bmp != null) return bmp;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static byte[] readFileBytesSafe(File file) {
        if (file == null) return null;

        try {
            if (file.exists() && file.isFile()) {
                byte[] b = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    int total = 0;
                    while (total < b.length) {
                        int r = fis.read(b, total, b.length - total);
                        if (r == -1) break;
                        total += r;
                    }
                    if (total > 0) return b;
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Standard Java read failed for " + file.getAbsolutePath() + ": " + e.getMessage());
        }

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat '" + file.getAbsolutePath() + "'"});
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = p.getInputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            p.waitFor();
            byte[] res = baos.toByteArray();
            if (res.length > 0) {
                return res;
            }
        } catch (Throwable ignored) {}

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"cat", file.getAbsolutePath()});
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = p.getInputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            p.waitFor();
            byte[] res = baos.toByteArray();
            if (res.length > 0) return res;
        } catch (Throwable ignored) {}

        return null;
    }

    public static boolean installPackageFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return false;
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && !cacheDir.exists()) cacheDir.mkdirs();
        File tempFile = new File(cacheDir, "picked_ext_" + System.currentTimeMillis() + ".zip");
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (is == null) {
                Log.e(TAG, "Gagal membuka InputStream dari URI: " + uri);
                return false;
            }
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
            fos.flush();
        } catch (Throwable e) {
            Log.e(TAG, "Gagal menyalin paket ekstensi dari URI: " + e.getMessage(), e);
            if (tempFile.exists()) tempFile.delete();
            return false;
        }

        try {
            boolean ok = installPackage(context, tempFile);
            tempFile.delete();
            return ok;
        } catch (Throwable e) {
            if (tempFile.exists()) tempFile.delete();
            return false;
        }
    }

    public static void launchExtensionPicker(TermuxActivity act) {
        if (act == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = new String[]{
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream",
                "application/x-chrome-extension",
                "*/*"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            act.startActivityForResult(intent, REQUEST_CODE_PICK_EXTENSION);
        } catch (Throwable e) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                act.startActivityForResult(intent, REQUEST_CODE_PICK_EXTENSION);
            } catch (Throwable err) {
                FusionToast.makeText(act, "Gagal membuka File Manager: " + err.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void handlePickedExtensionUri(final TermuxActivity act, final FusionWebViewManager manager, Uri uri) {
        if (act == null || uri == null) return;
        boolean ok = installPackageFromUri(act, uri);
        if (ok) {
            FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang dari File Manager!", Toast.LENGTH_LONG).show();
            if (manager != null && manager.mActiveWebView != null) {
                setupWebView(manager.mActiveWebView);
            }
            showExtensionsDialog(act, manager);
        } else {
            FusionToast.makeText(act, "❌ Gagal memasang: berkas zip/crx tidak valid atau manifest.json tidak ditemukan.", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean installPackage(Context context, File packageFile) {
        if (packageFile == null) return false;
        File baseDir = getPrimaryExtensionsDir(context);
        String baseName = packageFile.getName().toLowerCase().replace(".crx", "").replace(".zip", "");
        String safeName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File tempExtractDir = new File(baseDir, ".temp_" + System.currentTimeMillis());

        try {
            boolean extracted = extractArchive(packageFile, tempExtractDir);
            if (!extracted) {
                deleteRecursive(tempExtractDir);
                return false;
            }

            File manifestFile = new File(tempExtractDir, "manifest.json");
            if (!manifestFile.exists()) {
                deleteRecursive(tempExtractDir);
                return false;
            }

            ExtensionInfo parsed = parseExtensionDir(tempExtractDir);
            String rawClean = (parsed != null && parsed.name != null && !parsed.name.isEmpty())
                    ? parsed.name.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("^_+|_+$", "").replaceAll("_+", "_")
                    : safeName;
            String targetFolderName = (rawClean != null && !rawClean.isEmpty()) ? rawClean : safeName;
            if (targetFolderName.isEmpty()) targetFolderName = "ext_" + (System.currentTimeMillis() % 10000);

            File finalDir = new File(baseDir, targetFolderName);
            if (finalDir.exists()) {
                deleteRecursive(finalDir);
            }

            boolean renamed = tempExtractDir.renameTo(finalDir);
            if (!renamed || !finalDir.exists()) {
                copyDirectory(tempExtractDir, finalDir);
                deleteRecursive(tempExtractDir);
            }

            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod -R 777 '" + finalDir.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}

            Map<String, Boolean> stateMap = loadExtensionsState(context);
            stateMap.put(finalDir.getName(), true);
            saveExtensionsState(context, stateMap);

            ExtensionInfo installedExt = parseExtensionDir(finalDir);
            if (installedExt != null && installedExt.hasBackground()) {
                ensureBackgroundHostRunning(context, installedExt);
            }

            Log.i(TAG, "Ekstensi berhasil diinstal ke: " + finalDir.getAbsolutePath());
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal menginstal paket ekstensi " + packageFile.getName() + ": " + e.getMessage(), e);
            deleteRecursive(tempExtractDir);
            return false;
        }
    }

    public static boolean extractArchive(File archiveFile, File targetDir) {
        if (archiveFile == null || targetDir == null) return false;
        if (!targetDir.exists()) {
            targetDir.mkdirs();
            try {
                targetDir.setReadable(true, false);
                targetDir.setWritable(true, false);
                targetDir.setExecutable(true, false);
            } catch (Throwable ignored) {}
        }
        if (!targetDir.exists() || !targetDir.canWrite()) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "mkdir -p '" + targetDir.getAbsolutePath() + "' && chmod 777 '" + targetDir.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }

        try {
            byte[] allBytes = readFileBytesSafe(archiveFile);
            if (allBytes == null || allBytes.length == 0) {
                Log.e(TAG, "Gagal membaca bytes dari file arsip: " + archiveFile.getAbsolutePath());
                return false;
            }

            int zipOffset = 0;
            if (allBytes.length >= 4) {
                if (allBytes[0] == 'C' && allBytes[1] == 'r' && allBytes[2] == '2' && allBytes[3] == '4') {
                    for (int i = 0; i <= allBytes.length - 4; i++) {
                        if (allBytes[i] == 0x50 && allBytes[i + 1] == 0x4B && allBytes[i + 2] == 0x03 && allBytes[i + 3] == 0x04) {
                            zipOffset = i;
                            break;
                        }
                    }
                } else if (allBytes[0] == 0x50 && allBytes[1] == 0x4B && allBytes[2] == 0x03 && allBytes[3] == 0x04) {
                    zipOffset = 0;
                } else {
                    for (int i = 0; i <= Math.min(allBytes.length - 4, 65536); i++) {
                        if (allBytes[i] == 0x50 && allBytes[i + 1] == 0x4B && allBytes[i + 2] == 0x03 && allBytes[i + 3] == 0x04) {
                            zipOffset = i;
                            break;
                        }
                    }
                }
            }

            String rootPrefix = "";
            try (ZipInputStream zisDetect = new ZipInputStream(new ByteArrayInputStream(allBytes, zipOffset, allBytes.length - zipOffset))) {
                ZipEntry ze;
                while ((ze = zisDetect.getNextEntry()) != null) {
                    String name = ze.getName().replace('\\', '/');
                    if (name.equals("manifest.json")) {
                        rootPrefix = "";
                        break;
                    } else if (name.endsWith("/manifest.json")) {
                        rootPrefix = name.substring(0, name.indexOf("manifest.json"));
                        break;
                    }
                }
            } catch (Throwable ignored) {}

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(allBytes, zipOffset, allBytes.length - zipOffset))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName().replace('\\', '/');
                    if (entryName.contains("..")) continue;
                    if (entryName.startsWith("__MACOSX/") || entryName.endsWith(".DS_Store")) continue;
                    if (!rootPrefix.isEmpty() && entryName.startsWith(rootPrefix)) {
                        entryName = entryName.substring(rootPrefix.length());
                    }
                    if (entryName.isEmpty()) continue;

                    File outFile = new File(targetDir, entryName);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        try {
                            outFile.setReadable(true, false);
                            outFile.setWritable(true, false);
                            outFile.setExecutable(true, false);
                        } catch (Throwable ignored) {}
                    } else {
                        File parent = outFile.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                            try {
                                parent.setReadable(true, false);
                                parent.setWritable(true, false);
                                parent.setExecutable(true, false);
                            } catch (Throwable ignored) {}
                        }
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        try {
                            outFile.setReadable(true, false);
                            outFile.setWritable(true, false);
                        } catch (Throwable ignored) {}
                    }
                }
            }

            File manifestCheck = new File(targetDir, "manifest.json");
            if (manifestCheck.exists() && manifestCheck.isFile()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod -R 777 '" + targetDir.getAbsolutePath() + "'"});
                    p.waitFor();
                } catch (Throwable ignored) {}
                return true;
            }

            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "unzip -o '" + archiveFile.getAbsolutePath() + "' -d '" + targetDir.getAbsolutePath() + "' && chmod -R 777 '" + targetDir.getAbsolutePath() + "'"});
                p.waitFor();
                if (manifestCheck.exists() && manifestCheck.isFile()) return true;
            } catch (Throwable ignored) {}

            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal mengekstrak archive " + archiveFile.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean isSafeExtensionPath(File f) {
        if (f == null) return false;
        try {
            String canonical = f.getCanonicalPath();
            if (canonical == null || canonical.trim().isEmpty() || canonical.length() < 20) {
                return false;
            }

            String[] forbiddenPrefixes = new String[] {
                "/system", "/vendor", "/product", "/odm", "/apex", "/bin", "/sbin", "/etc",
                "/boot", "/recovery", "/persist", "/metadata", "/dev", "/proc", "/sys",
                "/storage", "/sdcard"
            };
            for (String fp : forbiddenPrefixes) {
                if (canonical.equals(fp) || canonical.startsWith(fp + "/")) {
                    Log.e(TAG, "⛔ BLOCKED DANGEROUS DELETE: Path berada di partisi sistem -> " + canonical);
                    return false;
                }
            }

            String[] forbiddenExact = new String[] {
                "/", "/root", "/data", "/data/data", "/data/user", "/data/user/0",
                "/data/data/com.termux", "/data/user/0/com.termux",
                "/data/data/com.termux/files", "/data/user/0/com.termux/files",
                "/data/data/com.termux/files/home", "/data/user/0/com.termux/files/home",
                "/data/data/com.termux/cache", "/data/user/0/com.termux/cache",
                "/data/local", "/data/local/nhsystem", "/data/local/nhsystem/kali-arm64",
                "/data/local/nhsystem/kali-arm64/root",
                "/data/data/com.termux/files/home/.fusion", "/data/user/0/com.termux/files/home/.fusion",
                "/data/data/com.termux/files/home/.fusion/extensions", "/data/user/0/com.termux/files/home/.fusion/extensions",
                "/data/local/nhsystem/kali-arm64/root/.fusion", "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
                "/root/.fusion", "/root/.fusion/extensions"
            };
            for (String fe : forbiddenExact) {
                if (canonical.equals(fe) || canonical.equals(fe + "/")) {
                    Log.e(TAG, "⛔ BLOCKED DANGEROUS DELETE: Folder induk kritis tidak boleh dihapus -> " + canonical);
                    return false;
                }
            }

            String[] allowedParentPrefixes = new String[] {
                "/data/data/com.termux/files/home/.fusion/extensions/",
                "/data/user/0/com.termux/files/home/.fusion/extensions/",
                "/data/local/nhsystem/kali-arm64/root/.fusion/extensions/",
                "/root/.fusion/extensions/",
                "/data/data/com.termux/files/fusion_extensions/",
                "/data/user/0/com.termux/files/fusion_extensions/",
                "/data/data/com.termux/cache/",
                "/data/user/0/com.termux/cache/"
            };

            for (String prefix : allowedParentPrefixes) {
                if (canonical.startsWith(prefix) && canonical.length() > prefix.length()) {
                    String sub = canonical.substring(prefix.length());
                    if (!sub.isEmpty() && !sub.equals(".") && !sub.equals("..") && !sub.contains("../")) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal memverifikasi keamanan path: " + e.getMessage());
            return false;
        }
        return false;
    }

    public static boolean deleteRecursive(File f) {
        if (f == null || !f.exists()) return true;

        if (!isSafeExtensionPath(f)) {
            Log.e(TAG, "⛔ deleteRecursive DIBATALKAN: Path tidak lolos audit keamanan -> " + f.getAbsolutePath());
            return false;
        }

        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        boolean ok = f.delete();
        if (ok || !f.exists()) return true;

        try {
            f.setWritable(true, false);
            if (f.delete() || !f.exists()) return true;
        } catch (Throwable ignored) {}

        try {
            String safePath = f.getCanonicalPath();
            if (isSafeExtensionPath(f) && safePath != null && safePath.length() >= 25) {
                String escapedPath = safePath.replace("'", "'\''");
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -rf '" + escapedPath + "'"});
                p.waitFor();
            }
        } catch (Throwable ignored) {}

        return !f.exists();
    }

    public static void copyDirectory(File source, File target) {
        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(target, child));
                }
            }
        } else {
            try (FileInputStream in = new FileInputStream(source);
                 FileOutputStream out = new FileOutputStream(target)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void ensureBackgroundHostRunning(final Context context, final String extId) {
        if (context == null || extId == null || extId.isEmpty()) return;
        if (sBackgroundWebViews.containsKey(extId)) return;
        ExtensionInfo ext = findExtensionByIdOrHost(context, extId);
        if (ext != null) {
            ensureBackgroundHostRunning(context, ext);
        }
    }

    public static void ensureBackgroundHostRunning(final Context context, final ExtensionInfo ext) {
        if (context == null || ext == null || !ext.enabled || !ext.hasBackground()) return;
        if (sBackgroundWebViews.containsKey(ext.id)) return;

        sMainHandler.post(() -> {
            if (sBackgroundWebViews.containsKey(ext.id)) return;
            try {
                WebView bgWv = new WebView(context);
                WebSettings ws = bgWv.getSettings();
                ws.setJavaScriptEnabled(true);
                ws.setDomStorageEnabled(true);
                ws.setDatabaseEnabled(true);
                ws.setAllowFileAccess(true);
                ws.setAllowContentAccess(true);
                ws.setAllowFileAccessFromFileURLs(true);
                ws.setAllowUniversalAccessFromFileURLs(true);

                bgWv.addJavascriptInterface(new FusionExtensionBridge(context, bgWv, ext.id, true), "__FusionExtensionBridge__");

                bgWv.setWebViewClient(new WebViewClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if (request != null && request.getUrl() != null) {
                            WebResourceResponse resp = handleExtensionResourceIntercept(context, request.getUrl(), ext, null);
                            if (resp != null) return resp;
                        }
                        return super.shouldInterceptRequest(view, request);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        Log.i(TAG, "Background host ready for extension: " + ext.id);
                        List<Runnable> pending = sPendingBgActions.remove(ext.id);
                        if (pending != null) {
                            for (Runnable r : pending) {
                                try { r.run(); } catch (Throwable ignored) {}
                            }
                        }
                    }
                });

                String polyfill = getChromePolyfillScript(context, ext, true);
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Fusion BG: ")
                    .append(ext.name).append("</title></head><body>\n");
                html.append("<script>\n").append(polyfill).append("\n</script>\n");

                html.append("<script>\n");
                html.append("if (typeof importScripts === 'undefined' || !window.__is_fusion_import_scripts__) {\n");
                html.append("  window.importScripts = function() {\n");
                html.append("    for (var i = 0; i < arguments.length; i++) {\n");
                html.append("      var src = arguments[i];\n");
                html.append("      if (!src) continue;\n");
                html.append("      try {\n");
                html.append("        var xhr = new XMLHttpRequest();\n");
                html.append("        xhr.open('GET', src, false);\n");
                html.append("        xhr.send(null);\n");
                html.append("        if (xhr.status === 200 || xhr.status === 0) {\n");
                html.append("          (1, eval)(xhr.responseText + '\\n//# sourceURL=' + src);\n");
                html.append("        } else {\n");
                html.append("          console.error('[Fusion BG] importScripts failed HTTP ' + xhr.status + ' for: ' + src);\n");
                html.append("        }\n");
                html.append("      } catch(err) {\n");
                html.append("        console.error('[Fusion BG] Exception loading ' + src + ':', err);\n");
                html.append("      }\n");
                html.append("    }\n");
                html.append("  };\n");
                html.append("  window.importScripts.__is_fusion_import_scripts__ = true;\n");
                html.append("}\n");
                html.append("if (typeof self.clients === 'undefined') {\n");
                html.append("  self.clients = {\n");
                html.append("    claim: function() { return Promise.resolve(); },\n");
                html.append("    matchAll: function() { return Promise.resolve([]); },\n");
                html.append("    openWindow: function(u) {\n");
                html.append("      try { if (window.__FusionExtensionBridge__) window.__FusionExtensionBridge__.openWindow('").append(ext.id).append("', u); } catch(e) {}\n");
                html.append("      return Promise.resolve(null);\n");
                html.append("    }\n");
                html.append("  };\n");
                html.append("}\n");
                html.append("if (typeof self.skipWaiting === 'undefined') {\n");
                html.append("  self.skipWaiting = function() { return Promise.resolve(); };\n");
                html.append("}\n");
                html.append("if (typeof self.serviceWorker === 'undefined') {\n");
                html.append("  self.serviceWorker = {\n");
                html.append("    state: 'activated',\n");
                html.append("    scriptURL: 'file://").append(ext.dir.getAbsolutePath()).append("/service-worker.js',\n");
                html.append("    addEventListener: function() {},\n");
                html.append("    removeEventListener: function() {}\n");
                html.append("  };\n");
                html.append("}\n");
                html.append("if (typeof self.registration === 'undefined') {\n");
                html.append("  self.registration = {\n");
                html.append("    scope: 'chrome-extension://").append(ext.id).append("/',\n");
                html.append("    active: self.serviceWorker,\n");
                html.append("    showNotification: function() { return Promise.resolve(); },\n");
                html.append("    getNotifications: function() { return Promise.resolve([]); },\n");
                html.append("    unregister: function() { return Promise.resolve(true); }\n");
                html.append("  };\n");
                html.append("}\n");
                html.append("</script>\n");

                if (ext.serviceWorker != null && !ext.serviceWorker.isEmpty()) {
                    String moduleAttr = "module".equalsIgnoreCase(ext.backgroundType) ? " type=\"module\"" : "";
                    html.append("<script").append(moduleAttr).append(" src=\"").append(ext.serviceWorker).append("\"></script>\n");
                } else if (!ext.backgroundScripts.isEmpty()) {
                    for (String s : ext.backgroundScripts) {
                        html.append("<script src=\"").append(s).append("\"></script>\n");
                    }
                } else if (ext.backgroundPage != null && !ext.backgroundPage.isEmpty()) {
                    File bgPageFile = new File(ext.dir, ext.backgroundPage);
                    if (bgPageFile.exists()) {
                        bgWv.loadUrl("file://" + bgPageFile.getAbsolutePath());
                        sBackgroundWebViews.put(ext.id, bgWv);
                        sBackgroundExtMap.put(ext.id, ext);
                        return;
                    }
                }
                html.append("</body></html>");

                String baseUrl = "file://" + ext.dir.getAbsolutePath() + "/";
                bgWv.loadDataWithBaseURL(baseUrl, html.toString(), "text/html", "utf-8", null);

                sBackgroundWebViews.put(ext.id, bgWv);
                sBackgroundExtMap.put(ext.id, ext);
                Log.i(TAG, "Headless background host launched for extension: " + ext.name + " (" + ext.id + ")");
            } catch (Throwable e) {
                Log.e(TAG, "Gagal meluncurkan background host untuk " + ext.id + ": " + e.getMessage(), e);
            }
        });
    }

    public static void startAllBackgroundHosts(final Context context) {
        if (context == null) return;
        List<ExtensionInfo> exts = getInstalledExtensions(context);
        for (ExtensionInfo ext : exts) {
            if (ext.enabled && ext.hasBackground()) {
                ensureBackgroundHostRunning(context, ext);
            }
        }
    }

    public static void stopBackgroundHost(String extId) {
        if (extId == null) return;
        WebView wv = sBackgroundWebViews.remove(extId);
        sBackgroundExtMap.remove(extId);
        sPendingBgActions.remove(extId);
        if (wv != null) {
            sMainHandler.post(() -> {
                try {
                    wv.stopLoading();
                    wv.loadUrl("about:blank");
                    wv.destroy();
                } catch (Throwable ignored) {}
            });
        }
    }

    public static void stopAllBackgroundHosts() {
        for (String id : new ArrayList<>(sBackgroundWebViews.keySet())) {
            stopBackgroundHost(id);
        }
    }

    public static File resolveExtensionUrlToFile(Context context, String url) {
        if (url == null || !url.startsWith("chrome-extension://")) return null;
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            ExtensionInfo ext = findExtensionByIdOrHost(context, host);
            if (ext != null && ext.dir != null && ext.dir.exists()) {
                String path = uri.getPath();
                String rel = (path != null && path.startsWith("/")) ? path.substring(1) : (path != null ? path : "");
                File target = new File(ext.dir, rel);
                if (target.exists()) return target;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static WebResourceResponse handleExtensionResourceIntercept(Context context, Uri uri, ExtensionInfo forcedExt, String currentUrl) {
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if (!"file".equalsIgnoreCase(scheme) && !"chrome-extension".equalsIgnoreCase(scheme)) {
            return null;
        }

        String path = uri.getPath();
        if (path == null) return null;

        File targetFile = null;

        if ("file".equalsIgnoreCase(scheme)) {
            File directFile = new File(path);
            if (directFile.exists() && directFile.isFile()) {
                targetFile = directFile;
            }
        }

        if (targetFile == null && "chrome-extension".equalsIgnoreCase(scheme)) {
            String host = uri.getHost();
            ExtensionInfo ext = (forcedExt != null) ? forcedExt : findExtensionByIdOrHost(context, host);
            if (ext != null && ext.dir != null && ext.dir.exists()) {
                String relPath = path.startsWith("/") ? path.substring(1) : path;
                File candidate = new File(ext.dir, relPath);
                if (candidate.exists() && candidate.isFile()) {
                    targetFile = candidate;
                }
            }
        }

        if (targetFile == null) {
            ExtensionInfo ext = forcedExt;
            if (ext == null && currentUrl != null) {
                ext = findExtensionFromUrl(context, currentUrl);
            }
            if (ext != null && ext.dir != null && ext.dir.exists()) {
                String relPath = path.startsWith("/") ? path.substring(1) : path;
                File candidate = new File(ext.dir, relPath);
                if (candidate.exists() && candidate.isFile()) {
                    targetFile = candidate;
                }
            }
        }

        if (targetFile != null && targetFile.exists() && targetFile.isFile()) {
            String mimeType = getFileMimeType(targetFile.getName());
            String encoding = (mimeType.startsWith("text/") || mimeType.contains("json") || mimeType.contains("javascript")) ? "utf-8" : null;

            if ("text/css".equalsIgnoreCase(mimeType)) {
                try {
                    byte[] cssBytes = readFileBytesSafe(targetFile);
                    if (cssBytes != null && cssBytes.length > 0) {
                        String cssStr = new String(cssBytes, StandardCharsets.UTF_8);
                        if (cssStr.contains("__MSG_")) {
                            File extDir = targetFile.getParentFile();
                            while (extDir != null && extDir.exists() && !new File(extDir, "manifest.json").exists()) {
                                extDir = extDir.getParentFile();
                            }
                            if (extDir != null && new File(extDir, "manifest.json").exists()) {
                                String defaultLocale = "en";
                                File mf = new File(extDir, "manifest.json");
                                byte[] mfBytes = readFileBytesSafe(mf);
                                if (mfBytes != null && mfBytes.length > 0) {
                                    try {
                                        JSONObject root = new JSONObject(new String(mfBytes, StandardCharsets.UTF_8));
                                        defaultLocale = root.optString("default_locale", "en");
                                    } catch (Throwable ignored) {}
                                }
                                cssStr = cssStr.replace("__MSG_@@bidi_dir__", "ltr");
                                Matcher matcher = Pattern.compile("__MSG_([a-zA-Z0-9_]+)__").matcher(cssStr);
                                StringBuffer sb = new StringBuffer();
                                while (matcher.find()) {
                                    String k = matcher.group(1);
                                    String resolved = resolveI18nString(extDir, "__MSG_" + k + "__", defaultLocale);
                                    if (resolved != null && !resolved.startsWith("__MSG_")) {
                                        matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
                                    } else {
                                        matcher.appendReplacement(sb, "");
                                    }
                                }
                                matcher.appendTail(sb);
                                byte[] processed = sb.toString().getBytes(StandardCharsets.UTF_8);
                                WebResourceResponse cssResp = new WebResourceResponse(mimeType, encoding, new ByteArrayInputStream(processed));
                                Map<String, String> corsHeaders = new HashMap<>();
                                corsHeaders.put("Access-Control-Allow-Origin", "*");
                                corsHeaders.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                                corsHeaders.put("Access-Control-Allow-Headers", "*");
                                cssResp.setResponseHeaders(corsHeaders);
                                return cssResp;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            try {
                WebResourceResponse fileResp = new WebResourceResponse(mimeType, encoding, new FileInputStream(targetFile));
                Map<String, String> corsHeaders = new HashMap<>();
                corsHeaders.put("Access-Control-Allow-Origin", "*");
                corsHeaders.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                corsHeaders.put("Access-Control-Allow-Headers", "*");
                fileResp.setResponseHeaders(corsHeaders);
                return fileResp;
            } catch (Throwable ignored) {}
        }

        return null;
    }

    public static String getFileMimeType(String fileName) {
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

    public static String getChromePolyfillScript(Context context) {
        return getChromePolyfillScript(context, null, false);
    }

    public static String getChromePolyfillScript(Context context, ExtensionInfo ext, boolean isBackground) {
        String extId = (ext != null && ext.id != null) ? ext.id : "";
        String manifestJsonStr = (ext != null && ext.manifestJson != null)
                ? ext.manifestJson.toString()
                : "{\"name\":\"Fusion Extension\",\"version\":\"1.0.0\",\"manifest_version\":3}";

        return "(function() {\n" +
                "  var root = (typeof window !== 'undefined') ? window : ((typeof self !== 'undefined') ? self : globalThis);\n" +
                "  var detectedExtId = '" + extId + "';\n" +
                "  if (!detectedExtId) {\n" +
                "    try {\n" +
                "      var h = (root.location && root.location.href) || '';\n" +
                "      if (h.indexOf('chrome-extension://') === 0) {\n" +
                "        var sIdx = h.indexOf('/', 19);\n" +
                "        detectedExtId = (sIdx !== -1) ? h.substring(19, sIdx) : h.substring(19);\n" +
                "      } else if (h.indexOf('.fusion/extensions/') !== -1) {\n" +
                "        var parts = h.split('.fusion/extensions/')[1].split('/');\n" +
                "        if (parts && parts[0]) detectedExtId = parts[0];\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "  }\n" +
                "  if (!detectedExtId) detectedExtId = 'fusion-extension-runtime';\n" +
                "  if (root.chrome && root.chrome.__is_fusion_polyfill__ && root.chrome.__ext_id__ === detectedExtId) return;\n" +
                "  var extId = detectedExtId;\n" +
                "  var isBackground = " + isBackground + ";\n" +
                "  var manifestObj = " + manifestJsonStr + ";\n" +
                "  var extStorage = {};\n" +
                "  try {\n" +
                "    var raw = root.__FusionExtensionBridge__ ? root.__FusionExtensionBridge__.getStorage(extId) : null;\n" +
                "    if (raw) extStorage = JSON.parse(raw);\n" +
                "  } catch(e) {}\n" +
                "  if (!extStorage) extStorage = {};\n" +
                "  var storageSession = {};\n" +
                "  var storageListeners = [];\n" +
                "  var connectListeners = [];\n" +
                "  var messageListeners = [];\n" +
                "  var activePorts = {};\n" +
                "  var responseCallbacks = {};\n" +
                "  root.__fusionNotifyStorageChanged = function(changedExtId, newJsonStr) {\n" +
                "    if (changedExtId !== extId) return;\n" +
                "    try {\n" +
                "      var newStorage = JSON.parse(newJsonStr);\n" +
                "      var changes = {};\n" +
                "      for (var k in newStorage) {\n" +
                "        if (extStorage[k] !== newStorage[k]) {\n" +
                "          changes[k] = { oldValue: extStorage[k], newValue: newStorage[k] };\n" +
                "        }\n" +
                "      }\n" +
                "      for (var k2 in extStorage) {\n" +
                "        if (newStorage[k2] === undefined) {\n" +
                "          changes[k2] = { oldValue: extStorage[k2], newValue: undefined };\n" +
                "        }\n" +
                "      }\n" +
                "      extStorage = newStorage;\n" +
                "      for (var i = 0; i < storageListeners.length; i++) {\n" +
                "        try { storageListeners[i](changes, 'local'); } catch(e) {}\n" +
                "      }\n" +
                "    } catch(e) {}\n" +
                "  };\n" +
                "  root.__fusionIncomingPort = function(portId, senderId, connectInfoJson) {\n" +
                "    try {\n" +
                "      var connectInfo = {};\n" +
                "      try { connectInfo = JSON.parse(connectInfoJson); } catch(e) {}\n" +
                "      var portMsgListeners = [];\n" +
                "      var portDisconnectListeners = [];\n" +
                "      var port = {\n" +
                "        name: connectInfo.name || '',\n" +
                "        sender: { id: senderId || extId, url: 'chrome-extension://' + (senderId || extId) },\n" +
                "        onMessage: {\n" +
                "          addListener: function(fn) {\n" +
                "            if (typeof fn === 'function' && portMsgListeners.indexOf(fn) === -1) portMsgListeners.push(fn);\n" +
                "          },\n" +
                "          removeListener: function(fn) {\n" +
                "            var idx = portMsgListeners.indexOf(fn);\n" +
                "            if (idx !== -1) portMsgListeners.splice(idx, 1);\n" +
                "          },\n" +
                "          hasListener: function(fn) { return portMsgListeners.indexOf(fn) !== -1; }\n" +
                "        },\n" +
                "        onDisconnect: {\n" +
                "          addListener: function(fn) {\n" +
                "            if (typeof fn === 'function' && portDisconnectListeners.indexOf(fn) === -1) portDisconnectListeners.push(fn);\n" +
                "          },\n" +
                "          removeListener: function(fn) {\n" +
                "            var idx = portDisconnectListeners.indexOf(fn);\n" +
                "            if (idx !== -1) portDisconnectListeners.splice(idx, 1);\n" +
                "          },\n" +
                "          hasListener: function(fn) { return portDisconnectListeners.indexOf(fn) !== -1; }\n" +
                "        },\n" +
                "        postMessage: function(msg) {\n" +
                "          try {\n" +
                "            if (root.__FusionExtensionBridge__) {\n" +
                "              root.__FusionExtensionBridge__.postPortMessage(extId, portId, JSON.stringify(msg), isBackground);\n" +
                "            }\n" +
                "          } catch(err) { console.error('[Fusion Port postMessage Error]:', err); }\n" +
                "        },\n" +
                "        disconnect: function() {\n" +
                "          try {\n" +
                "            if (root.__FusionExtensionBridge__) {\n" +
                "              root.__FusionExtensionBridge__.disconnectPort(extId, portId, isBackground);\n" +
                "            }\n" +
                "          } catch(err) {}\n" +
                "          delete activePorts[portId];\n" +
                "          for (var i = 0; i < portDisconnectListeners.length; i++) {\n" +
                "            try { portDisconnectListeners[i](port); } catch(e) {}\n" +
                "          }\n" +
                "        },\n" +
                "        __portMsgListeners: portMsgListeners,\n" +
                "        __portDisconnectListeners: portDisconnectListeners\n" +
                "      };\n" +
                "      activePorts[portId] = port;\n" +
                "      for (var i = 0; i < connectListeners.length; i++) {\n" +
                "        try { connectListeners[i](port); } catch(err) { console.error('[Fusion onConnect Error]:', err); }\n" +
                "      }\n" +
                "    } catch(err) { console.error('[Fusion __fusionIncomingPort Error]:', err); }\n" +
                "  };\n" +
                "  root.__fusionReceivePortMessage = function(portId, msgJson) {\n" +
                "    try {\n" +
                "      var port = activePorts[portId];\n" +
                "      if (!port) return;\n" +
                "      var msg = typeof msgJson === 'string' ? JSON.parse(msgJson) : msgJson;\n" +
                "      var listeners = port.__portMsgListeners || [];\n" +
                "      for (var i = 0; i < listeners.length; i++) {\n" +
                "        try { listeners[i](msg, port); } catch(err) { console.error('[Fusion port.onMessage Error]:', err); }\n" +
                "      }\n" +
                "    } catch(err) { console.error('[Fusion __fusionReceivePortMessage Error]:', err); }\n" +
                "  };\n" +
                "  root.__fusionDisconnectPort = function(portId) {\n" +
                "    try {\n" +
                "      var port = activePorts[portId];\n" +
                "      if (!port) return;\n" +
                "      delete activePorts[portId];\n" +
                "      var listeners = port.__portDisconnectListeners || [];\n" +
                "      for (var i = 0; i < listeners.length; i++) {\n" +
                "        try { listeners[i](port); } catch(err) {}\n" +
                "      }\n" +
                "    } catch(err) {}\n" +
                "  };\n" +
                "  root.__fusionIncomingMessage = function(msgId, senderId, msgJson) {\n" +
                "    try {\n" +
                "      var msg = typeof msgJson === 'string' ? JSON.parse(msgJson) : msgJson;\n" +
                "      var sender = { id: senderId || extId, url: 'chrome-extension://' + (senderId || extId) };\n" +
                "      var sendResponseCalled = false;\n" +
                "      var sendResponse = function(resp) {\n" +
                "        if (sendResponseCalled) return;\n" +
                "        sendResponseCalled = true;\n" +
                "        try {\n" +
                "          if (root.__FusionExtensionBridge__) {\n" +
                "            root.__FusionExtensionBridge__.sendRuntimeResponse(extId, msgId, JSON.stringify(resp));\n" +
                "          }\n" +
                "        } catch(err) {}\n" +
                "      };\n" +
                "      for (var i = 0; i < messageListeners.length; i++) {\n" +
                "        try {\n" +
                "          var willRespondAsync = messageListeners[i](msg, sender, sendResponse);\n" +
                "        } catch(err) { console.error('[Fusion onMessage Error]:', err); }\n" +
                "      }\n" +
                "    } catch(err) { console.error('[Fusion __fusionIncomingMessage Error]:', err); }\n" +
                "  };\n" +
                "  root.__fusionReceiveRuntimeResponse = function(msgId, respJson) {\n" +
                "    try {\n" +
                "      var cb = responseCallbacks[msgId];\n" +
                "      if (typeof cb === 'function') {\n" +
                "        delete responseCallbacks[msgId];\n" +
                "        var resp = typeof respJson === 'string' ? JSON.parse(respJson) : respJson;\n" +
                "        cb(resp);\n" +
                "      }\n" +
                "    } catch(err) {}\n" +
                "  };\n" +
                "  function createClientPort(targetId, connectInfo) {\n" +
                "    var name = (connectInfo && connectInfo.name) || '';\n" +
                "    var portId = 'p_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);\n" +
                "    var portMsgListeners = [];\n" +
                "    var portDisconnectListeners = [];\n" +
                "    var port = {\n" +
                "      name: name,\n" +
                "      sender: { id: extId, url: (root.location && root.location.href) || '' },\n" +
                "      onMessage: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && portMsgListeners.indexOf(fn) === -1) portMsgListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = portMsgListeners.indexOf(fn);\n" +
                "          if (idx !== -1) portMsgListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return portMsgListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      onDisconnect: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && portDisconnectListeners.indexOf(fn) === -1) portDisconnectListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = portDisconnectListeners.indexOf(fn);\n" +
                "          if (idx !== -1) portDisconnectListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return portDisconnectListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      postMessage: function(msg) {\n" +
                "        try {\n" +
                "          if (root.__FusionExtensionBridge__) {\n" +
                "            root.__FusionExtensionBridge__.postPortMessage(targetId, portId, JSON.stringify(msg), isBackground);\n" +
                "          }\n" +
                "        } catch(err) { console.error('[Fusion Port postMessage Error]:', err); }\n" +
                "      },\n" +
                "      disconnect: function() {\n" +
                "        try {\n" +
                "          if (root.__FusionExtensionBridge__) {\n" +
                "            root.__FusionExtensionBridge__.disconnectPort(targetId, portId, isBackground);\n" +
                "          }\n" +
                "        } catch(err) {}\n" +
                "        delete activePorts[portId];\n" +
                "        for (var i = 0; i < portDisconnectListeners.length; i++) {\n" +
                "          try { portDisconnectListeners[i](port); } catch(e) {}\n" +
                "        }\n" +
                "      },\n" +
                "      __portMsgListeners: portMsgListeners,\n" +
                "      __portDisconnectListeners: portDisconnectListeners\n" +
                "    };\n" +
                "    activePorts[portId] = port;\n" +
                "    try {\n" +
                "      if (root.__FusionExtensionBridge__) {\n" +
                "        root.__FusionExtensionBridge__.connectPort(targetId, portId, JSON.stringify(connectInfo || {}));\n" +
                "      }\n" +
                "    } catch(err) { console.error('[Fusion connectPort Error]:', err); }\n" +
                "    return port;\n" +
                "  }\n" +
                "  var chromeShim = {\n" +
                "    __is_fusion_polyfill__: true,\n" +
                "    __ext_id__: extId,\n" +
                "    runtime: {\n" +
                "      id: extId,\n" +
                "      lastError: null,\n" +
                "      getURL: function(path) {\n" +
                "        if (!path) return '';\n" +
                "        if (path.indexOf('://') !== -1) return path;\n" +
                "        var p = (path.charAt(0) === '/') ? path.substring(1) : path;\n" +
                "        return 'chrome-extension://' + extId + '/' + p;\n" +
                "      },\n" +
                "      getManifest: function() {\n" +
                "        return manifestObj;\n" +
                "      },\n" +
                "      reload: function() {\n" +
                "        if (root.location) root.location.reload();\n" +
                "      },\n" +
                "      getPlatformInfo: function(cb) {\n" +
                "        var info = { os: 'android', arch: 'arm64', nacl_arch: 'arm' };\n" +
                "        if (cb) cb(info);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(info) : undefined;\n" +
                "      },\n" +
                "      getContexts: function(filter, cb) {\n" +
                "        var ctxs = [{ contextType: isBackground ? 'BACKGROUND' : 'TAB', documentUrl: (root.location && root.location.href) || '' }];\n" +
                "        if (cb) cb(ctxs);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(ctxs) : undefined;\n" +
                "      },\n" +
                "      connect: function(arg1, arg2) {\n" +
                "        var targetId = extId;\n" +
                "        var connectInfo = null;\n" +
                "        if (typeof arg1 === 'string') {\n" +
                "          targetId = arg1;\n" +
                "          connectInfo = arg2 || {};\n" +
                "        } else if (typeof arg1 === 'object' && arg1 !== null) {\n" +
                "          connectInfo = arg1;\n" +
                "        } else {\n" +
                "          connectInfo = {};\n" +
                "        }\n" +
                "        return createClientPort(targetId, connectInfo);\n" +
                "      },\n" +
                "      onConnect: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && connectListeners.indexOf(fn) === -1) connectListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = connectListeners.indexOf(fn);\n" +
                "          if (idx !== -1) connectListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return connectListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      onConnectExternal: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && connectListeners.indexOf(fn) === -1) connectListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = connectListeners.indexOf(fn);\n" +
                "          if (idx !== -1) connectListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return connectListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      onMessage: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && messageListeners.indexOf(fn) === -1) messageListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = messageListeners.indexOf(fn);\n" +
                "          if (idx !== -1) messageListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return messageListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      onMessageExternal: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && messageListeners.indexOf(fn) === -1) messageListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = messageListeners.indexOf(fn);\n" +
                "          if (idx !== -1) messageListeners.splice(idx, 1);\n" +
                "        },\n" +
                "        hasListener: function(fn) { return messageListeners.indexOf(fn) !== -1; }\n" +
                "      },\n" +
                "      sendMessage: function(arg1, arg2, arg3) {\n" +
                "        var targetId = extId;\n" +
                "        var msg = arg1;\n" +
                "        var cb = arg2;\n" +
                "        if (typeof arg1 === 'string' && typeof arg2 !== 'function') {\n" +
                "          targetId = arg1;\n" +
                "          msg = arg2;\n" +
                "          cb = arg3;\n" +
                "        }\n" +
                "        var msgId = 'm_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);\n" +
                "        if (typeof cb === 'function') {\n" +
                "          responseCallbacks[msgId] = cb;\n" +
                "        }\n" +
                "        try {\n" +
                "          if (root.__FusionExtensionBridge__) {\n" +
                "            root.__FusionExtensionBridge__.sendRuntimeMessage(targetId, msgId, JSON.stringify(msg));\n" +
                "          }\n" +
                "        } catch(err) { console.error('[Fusion sendMessage Error]:', err); }\n" +
                "        return (typeof Promise !== 'undefined') ? new Promise(function(resolve) {\n" +
                "          responseCallbacks[msgId] = resolve;\n" +
                "        }) : undefined;\n" +
                "      },\n" +
                "      onInstalled: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onStartup: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      setUninstallURL: function(url, cb) { if (cb) cb(); return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined; }\n" +
                "    },\n" +
                "    i18n: {\n" +
                "      getMessage: function(key, substitutions) {\n" +
                "        if (!key) return '';\n" +
                "        var msg = null;\n" +
                "        if (root.__fusionI18n && root.__fusionI18n[key]) {\n" +
                "          var entry = root.__fusionI18n[key];\n" +
                "          msg = (typeof entry === 'object' && entry.message) ? entry.message : entry;\n" +
                "        }\n" +
                "        if (!msg && root.__fusionI18n) {\n" +
                "          var lower = key.toLowerCase();\n" +
                "          for (var k in root.__fusionI18n) {\n" +
                "            if (k.toLowerCase() === lower) {\n" +
                "              var ent = root.__fusionI18n[k];\n" +
                "              msg = (typeof ent === 'object' && ent.message) ? ent.message : ent;\n" +
                "              break;\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "        if (!msg) return key;\n" +
                "        if (substitutions) {\n" +
                "          var subs = Array.isArray(substitutions) ? substitutions : [substitutions];\n" +
                "          for (var i = 0; i < subs.length; i++) {\n" +
                "            msg = msg.replace('$' + (i + 1), subs[i]);\n" +
                "          }\n" +
                "        }\n" +
                "        return msg;\n" +
                "      },\n" +
                "      getUILanguage: function() { return (typeof navigator !== 'undefined' && (navigator.language || navigator.userLanguage)) || 'en'; },\n" +
                "      getAcceptLanguages: function(cb) {\n" +
                "        var langs = ['en-US', 'en', 'id'];\n" +
                "        if (cb) cb(langs);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(langs) : undefined;\n" +
                "      }\n" +
                "    },\n" +
                "    storage: {\n" +
                "      local: {\n" +
                "        get: function(keys, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              var result = {};\n" +
                "              if (!keys) {\n" +
                "                result = Object.assign({}, extStorage);\n" +
                "              } else if (typeof keys === 'string') {\n" +
                "                result[keys] = extStorage[keys];\n" +
                "              } else if (Array.isArray(keys)) {\n" +
                "                for (var i = 0; i < keys.length; i++) result[keys[i]] = extStorage[keys[i]];\n" +
                "              } else if (typeof keys === 'object' && keys !== null) {\n" +
                "                for (var k in keys) result[k] = (extStorage[k] !== undefined) ? extStorage[k] : keys[k];\n" +
                "              }\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(result); } catch(e) { console.error('[Chrome Shim storage get Error]:', e); }\n" +
                "              }\n" +
                "              resolve(result);\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        set: function(items, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              var changes = {};\n" +
                "              if (items && typeof items === 'object') {\n" +
                "                for (var k in items) {\n" +
                "                  var oldVal = extStorage[k];\n" +
                "                  extStorage[k] = items[k];\n" +
                "                  changes[k] = { oldValue: oldVal, newValue: items[k] };\n" +
                "                  try { if (typeof localStorage !== 'undefined') localStorage.setItem(k, items[k]); } catch(e) {}\n" +
                "                }\n" +
                "                try {\n" +
                "                  if (root.__FusionExtensionBridge__) root.__FusionExtensionBridge__.setStorage(extId, JSON.stringify(extStorage));\n" +
                "                } catch(e) {}\n" +
                "              }\n" +
                "              for (var i = 0; i < storageListeners.length; i++) {\n" +
                "                try { storageListeners[i](changes, 'local'); } catch(e) {}\n" +
                "              }\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        remove: function(keys, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              var list = Array.isArray(keys) ? keys : [keys];\n" +
                "              for (var i = 0; i < list.length; i++) {\n" +
                "                delete extStorage[list[i]];\n" +
                "                try { if (typeof localStorage !== 'undefined') localStorage.removeItem(list[i]); } catch(e) {}\n" +
                "              }\n" +
                "              try {\n" +
                "                if (root.__FusionExtensionBridge__) root.__FusionExtensionBridge__.setStorage(extId, JSON.stringify(extStorage));\n" +
                "              } catch(e) {}\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        clear: function(cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              extStorage = {};\n" +
                "              try {\n" +
                "                if (root.__FusionExtensionBridge__) root.__FusionExtensionBridge__.setStorage(extId, '{}');\n" +
                "              } catch(e) {}\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        }\n" +
                "      },\n" +
                "      sync: {\n" +
                "        get: function(keys, cb) { return chromeShim.storage.local.get(keys, cb); },\n" +
                "        set: function(items, cb) { return chromeShim.storage.local.set(items, cb); },\n" +
                "        remove: function(keys, cb) { return chromeShim.storage.local.remove(keys, cb); },\n" +
                "        clear: function(cb) { return chromeShim.storage.local.clear(cb); }\n" +
                "      },\n" +
                "      session: {\n" +
                "        get: function(keys, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              var result = {};\n" +
                "              if (!keys) {\n" +
                "                result = Object.assign({}, storageSession);\n" +
                "              } else if (typeof keys === 'string') {\n" +
                "                result[keys] = storageSession[keys];\n" +
                "              } else if (Array.isArray(keys)) {\n" +
                "                for (var i = 0; i < keys.length; i++) result[keys[i]] = storageSession[keys[i]];\n" +
                "              } else if (typeof keys === 'object' && keys !== null) {\n" +
                "                for (var k in keys) result[k] = (storageSession[k] !== undefined) ? storageSession[k] : keys[k];\n" +
                "              }\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(result); } catch(e) {}\n" +
                "              }\n" +
                "              resolve(result);\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        set: function(items, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              if (items && typeof items === 'object') {\n" +
                "                for (var k in items) storageSession[k] = items[k];\n" +
                "              }\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        remove: function(keys, cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              var list = Array.isArray(keys) ? keys : [keys];\n" +
                "              for (var i = 0; i < list.length; i++) delete storageSession[list[i]];\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        clear: function(cb) {\n" +
                "          return new Promise(function(resolve) {\n" +
                "            setTimeout(function() {\n" +
                "              storageSession = {};\n" +
                "              if (typeof cb === 'function') {\n" +
                "                try { cb(); } catch(e) {}\n" +
                "              }\n" +
                "              resolve();\n" +
                "            }, 0);\n" +
                "          });\n" +
                "        },\n" +
                "        getBytesInUse: function(keys, cb) {\n" +
                "          if (cb) cb(0);\n" +
                "          return (typeof Promise !== 'undefined') ? Promise.resolve(0) : undefined;\n" +
                "        }\n" +
                "      },\n" +
                "      managed: {\n" +
                "        get: function(keys, cb) {\n" +
                "          var res = {};\n" +
                "          if (cb) cb(res);\n" +
                "          return (typeof Promise !== 'undefined') ? Promise.resolve(res) : undefined;\n" +
                "        },\n" +
                "        getBytesInUse: function(keys, cb) {\n" +
                "          if (cb) cb(0);\n" +
                "          return (typeof Promise !== 'undefined') ? Promise.resolve(0) : undefined;\n" +
                "        }\n" +
                "      },\n" +
                "      onChanged: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && storageListeners.indexOf(fn) === -1) storageListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = storageListeners.indexOf(fn);\n" +
                "          if (idx !== -1) storageListeners.splice(idx, 1);\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    tabs: {\n" +
                "      create: function(props, cb) {\n" +
                "        var url = props ? props.url : '';\n" +
                "        if (url && typeof window !== 'undefined' && window.open) window.open(url, '_blank');\n" +
                "        var tab = { id: Date.now(), active: true, url: url };\n" +
                "        if (cb) {\n" +
                "          try { cb(tab); } catch(e) {}\n" +
                "        }\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(tab) : undefined;\n" +
                "      },\n" +
                "      update: function(tabId, props, cb) {\n" +
                "        if (props && props.url && root.location) {\n" +
                "          root.location.href = props.url;\n" +
                "        }\n" +
                "        var tab = { id: tabId || 1, active: true, url: (props && props.url) || (root.location && root.location.href) || '' };\n" +
                "        if (cb) {\n" +
                "          try { cb(tab); } catch(e) {}\n" +
                "        }\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(tab) : undefined;\n" +
                "      },\n" +
                "      get: function(tabId, cb) {\n" +
                "        var tab = { id: tabId || 1, active: true, url: (root.location && root.location.href) || '' };\n" +
                "        if (cb) cb(tab);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(tab) : undefined;\n" +
                "      },\n" +
                "      remove: function(tabIds, cb) {\n" +
                "        if (cb) cb();\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined;\n" +
                "      },\n" +
                "      sendMessage: function(tabId, msg, cb) {\n" +
                "        if (chromeShim.runtime && chromeShim.runtime.sendMessage) return chromeShim.runtime.sendMessage(msg, cb);\n" +
                "      },\n" +
                "      query: function(info, cb) {\n" +
                "        var list = [{ id: 1, active: true, url: (root.location && root.location.href) || '' }];\n" +
                "        return new Promise(function(resolve) {\n" +
                "          setTimeout(function() {\n" +
                "            if (typeof cb === 'function') { try { cb(list); } catch(e) {} }\n" +
                "            resolve(list);\n" +
                "          }, 0);\n" +
                "        });\n" +
                "      },\n" +
                "      onUpdated: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onActivated: { addListener: function(fn) {}, removeListener: function(fn) {} }\n" +
                "    },\n" +
                "    windows: {\n" +
                "      getCurrent: function(getInfo, cb) {\n" +
                "        if (typeof getInfo === 'function') { cb = getInfo; getInfo = null; }\n" +
                "        var win = { id: 1, focused: true, top: 0, left: 0, width: (typeof window !== 'undefined' && window.innerWidth) || 1080, height: (typeof window !== 'undefined' && window.innerHeight) || 1920, type: 'normal', state: 'normal' };\n" +
                "        if (cb) cb(win);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(win) : undefined;\n" +
                "      },\n" +
                "      create: function(createData, cb) {\n" +
                "        var url = createData ? createData.url : '';\n" +
                "        if (url && root.__FusionExtensionBridge__) {\n" +
                "          root.__FusionExtensionBridge__.openWindow(extId, url);\n" +
                "        } else if (url && typeof window !== 'undefined' && window.open) {\n" +
                "          window.open(url, '_blank');\n" +
                "        }\n" +
                "        var win = { id: Date.now(), focused: true, top: 0, left: 0, width: (typeof window !== 'undefined' && window.innerWidth) || 1080, height: (typeof window !== 'undefined' && window.innerHeight) || 1920, type: 'popup', state: 'normal' };\n" +
                "        if (cb) cb(win);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(win) : undefined;\n" +
                "      },\n" +
                "      update: function(windowId, updateInfo, cb) {\n" +
                "        var win = { id: windowId || 1, focused: true };\n" +
                "        if (cb) cb(win);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(win) : undefined;\n" +
                "      },\n" +
                "      remove: function(windowId, cb) {\n" +
                "        if (cb) cb();\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(win) : undefined;\n" +
                "      }\n" +
                "    },\n" +
                "    management: {\n" +
                "      getSelf: function(cb) {\n" +
                "        var info = { id: extId, enabled: true, installType: 'development', type: 'extension' };\n" +
                "        if (cb) cb(info);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(info) : undefined;\n" +
                "      }\n" +
                "    },\n" +
                "    permissions: {\n" +
                "      contains: function(p, cb) {\n" +
                "        if (cb) cb(true);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(true) : undefined;\n" +
                "      },\n" +
                "      request: function(p, cb) {\n" +
                "        if (cb) cb(true);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve(true) : undefined;\n" +
                "      }\n" +
                "    },\n" +
                "    alarms: {\n" +
                "      create: function(name, alarmInfo) {},\n" +
                "      get: function(name, cb) { if (cb) cb(null); return (typeof Promise !== 'undefined') ? Promise.resolve(null) : undefined; },\n" +
                "      getAll: function(cb) { if (cb) cb([]); return (typeof Promise !== 'undefined') ? Promise.resolve([]) : undefined; },\n" +
                "      clear: function(name, cb) { if (cb) cb(true); return (typeof Promise !== 'undefined') ? Promise.resolve(true) : undefined; },\n" +
                "      clearAll: function(cb) { if (cb) cb(true); return (typeof Promise !== 'undefined') ? Promise.resolve(true) : undefined; },\n" +
                "      onAlarm: { addListener: function(fn) {}, removeListener: function(fn) {} }\n" +
                "    },\n" +
                "    action: {\n" +
                "      setTitle: function(details, cb) { if (cb) cb(); },\n" +
                "      getTitle: function(details, cb) { if (cb) cb(''); },\n" +
                "      setIcon: function(details, cb) { if (cb) cb(); },\n" +
                "      setBadgeText: function(details, cb) { if (cb) cb(); },\n" +
                "      getBadgeText: function(details, cb) { if (cb) cb(''); },\n" +
                "      setBadgeBackgroundColor: function(details, cb) { if (cb) cb(); },\n" +
                "      enable: function(tabId, cb) { if (cb) cb(); },\n" +
                "      disable: function(tabId, cb) { if (cb) cb(); },\n" +
                "      onClicked: { addListener: function(fn) {}, removeListener: function(fn) {} }\n" +
                "    },\n" +
                "    browserAction: {\n" +
                "      setTitle: function(details, cb) { if (cb) cb(); },\n" +
                "      getTitle: function(details, cb) { if (cb) cb(''); },\n" +
                "      setIcon: function(details, cb) { if (cb) cb(); },\n" +
                "      setBadgeText: function(details, cb) { if (cb) cb(); },\n" +
                "      getBadgeText: function(details, cb) { if (cb) cb(''); },\n" +
                "      setBadgeBackgroundColor: function(details, cb) { if (cb) cb(); },\n" +
                "      enable: function(tabId, cb) { if (cb) cb(); },\n" +
                "      disable: function(tabId, cb) { if (cb) cb(); },\n" +
                "      onClicked: { addListener: function(fn) {}, removeListener: function(fn) {} }\n" +
                "    },\n" +
                "    webNavigation: {\n" +
                "      onBeforeNavigate: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onCommitted: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onDOMContentLoaded: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onCompleted: { addListener: function(fn) {}, removeListener: function(fn) {} },\n" +
                "      onErrorOccurred: { addListener: function(fn) {}, removeListener: function(fn) {} }\n" +
                "    },\n" +
                "    scripting: {\n" +
                "      executeScript: function(injection, cb) {\n" +
                "        if (cb) cb([]);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve([]) : undefined;\n" +
                "      },\n" +
                "      insertCSS: function(injection, cb) {\n" +
                "        if (cb) cb();\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined;\n" +
                "      },\n" +
                "      removeCSS: function(injection, cb) {\n" +
                "        if (cb) cb();\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined;\n" +
                "      }\n" +
                "    },\n" +
                "    declarativeNetRequest: {\n" +
                "      updateDynamicRules: function(options, cb) {\n" +
                "        if (cb) cb();\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined;\n" +
                "      },\n" +
                "      getDynamicRules: function(cb) {\n" +
                "        if (cb) cb([]);\n" +
                "        return (typeof Promise !== 'undefined') ? Promise.resolve([]) : undefined;\n" +
                "      }\n" +
                "    }\n" +
                "  };\n" +
                "  root.chrome = Object.assign(root.chrome || {}, chromeShim);\n" +
                "  if (typeof window !== 'undefined') window.chrome = root.chrome;\n" +
                "  if (typeof self !== 'undefined') self.chrome = root.chrome;\n" +
                "  if (typeof globalThis !== 'undefined') globalThis.chrome = root.chrome;\n" +
                "})();\n";
    }

    public static String buildContentScriptWrapper(ExtensionInfo ext, ExtensionInfo.ContentScript cs, int scriptIndex) {
        if (ext == null || cs == null) return null;
        if (cs.jsFiles.isEmpty() && cs.cssFiles.isEmpty()) return null;

        StringBuilder jsCodeBuilder = new StringBuilder();

        for (String cssFileRel : cs.cssFiles) {
            File f = new File(ext.dir, cssFileRel);
            if (f.exists() && f.isFile()) {
                try {
                    byte[] bytes = readFileBytesSafe(f);
                    if (bytes != null && bytes.length > 0) {
                        String cssText = new String(bytes, StandardCharsets.UTF_8);
                        String defaultLocale = "en";
                        File mf = new File(ext.dir, "manifest.json");
                        byte[] mfBytes = readFileBytesSafe(mf);
                        if (mfBytes != null && mfBytes.length > 0) {
                            try {
                                JSONObject root = new JSONObject(new String(mfBytes, StandardCharsets.UTF_8));
                                defaultLocale = root.optString("default_locale", "en");
                            } catch (Throwable ignored) {}
                        }
                        cssText = cssText.replace("__MSG_@@bidi_dir__", "ltr");
                        Matcher m = Pattern.compile("__MSG_([a-zA-Z0-9_]+)__").matcher(cssText);
                        StringBuffer sb = new StringBuffer();
                        while (m.find()) {
                            String k = m.group(1);
                            String resolved = resolveI18nString(ext.dir, "__MSG_" + k + "__", defaultLocale);
                            if (resolved != null && !resolved.startsWith("__MSG_")) {
                                m.appendReplacement(sb, Matcher.quoteReplacement(resolved));
                            } else {
                                m.appendReplacement(sb, "");
                            }
                        }
                        m.appendTail(sb);
                        String processedCss = sb.toString();

                        jsCodeBuilder.append("/* --- CSS: ").append(cssFileRel).append(" --- */\n");
                        jsCodeBuilder.append("(function() {\n");
                        jsCodeBuilder.append("  try {\n");
                        jsCodeBuilder.append("    var st = document.createElement('style');\n");
                        jsCodeBuilder.append("    st.setAttribute('data-fusion-ext', ").append(JSONObject.quote(ext.id)).append(");\n");
                        jsCodeBuilder.append("    st.textContent = ").append(JSONObject.quote(processedCss)).append(";\n");
                        jsCodeBuilder.append("    (document.head || document.documentElement).appendChild(st);\n");
                        jsCodeBuilder.append("  } catch(e) {}\n");
                        jsCodeBuilder.append("})();\n;\n");
                    }
                } catch (Throwable ignored) {}
            }
        }

        for (String jsFileRel : cs.jsFiles) {
            File f = new File(ext.dir, jsFileRel);
            if (f.exists() && f.isFile()) {
                try {
                    byte[] bytes = readFileBytesSafe(f);
                    if (bytes != null && bytes.length > 0) {
                        jsCodeBuilder.append("/* --- JS: ").append(jsFileRel).append(" --- */\n");
                        jsCodeBuilder.append(new String(bytes, StandardCharsets.UTF_8)).append("\n;\n");
                    }
                } catch (Throwable ignored) {}
            }
        }

        String combinedScript = jsCodeBuilder.toString();
        if (combinedScript.trim().isEmpty()) return null;

        JSONArray matchesJson = new JSONArray();
        for (String m : cs.matches) {
            matchesJson.put(m);
        }

        String safeExtId = ext.id.replaceAll("[^a-zA-Z0-9_]", "_");
        String runKey = "__FUSION_EXT_" + safeExtId + "_" + scriptIndex + "__";

        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  try {\n");
        sb.append("    var href = window.location.href;\n");
        sb.append("    if (!href || href === 'about:blank') return;\n");
        sb.append("    var matches = ").append(matchesJson.toString()).append(";\n");
        sb.append("    var allFrames = ").append(cs.allFrames).append(";\n");
        sb.append("    var runAt = '").append(cs.runAt).append("';\n");
        sb.append("    function matchUrl(url, patterns) {\n");
        sb.append("      if (!url) return false;\n");
        sb.append("      for (var i = 0; i < patterns.length; i++) {\n");
        sb.append("        var p = patterns[i];\n");
        sb.append("        if (p === '<all_urls>' || p === '*://*/*' || p === '*') return true;\n");
        sb.append("        try {\n");
        sb.append("          var regStr = '^' + p.replace(/\\\\/g, '\\\\\\\').replace(/\\./g, '\\\\.').replace(/\\*:\\/\\//g, 'https?:\\\\/\\\\/').replace(/http:\\/\\/\\*/g, 'http:\\\\/\\\\/.*').replace(/https:\\/\\/\\*/g, 'https:\\\\/\\\\/.*').replace(/\\*/g, '.*') + '$';\n");
        sb.append("          if (new RegExp(regStr).test(url)) return true;\n");
        sb.append("        } catch(e) {\n");
        sb.append("          if (url.indexOf(p) !== -1) return true;\n");
        sb.append("        }\n");
        sb.append("      }\n");
        sb.append("      return false;\n");
        sb.append("    }\n");
        sb.append("    if (!matchUrl(href, matches)) return;\n");
        sb.append("    if (!allFrames && window.top !== window) return;\n");
        sb.append("    var runKey = '").append(runKey).append("';\n");
        sb.append("    if (window[runKey]) return;\n");
        sb.append("    function executeCode() {\n");
        sb.append("      if (window[runKey]) return;\n");
        sb.append("      window[runKey] = true;\n");
        sb.append("      try {\n");
        sb.append(combinedScript).append("\n");
        sb.append("      } catch(e) { console.error('[Fusion Extension ").append(safeExtId).append(" Error]:', e); }\n");
        sb.append("    }\n");
        sb.append("    if (runAt === 'document_start') {\n");
        sb.append("      executeCode();\n");
        sb.append("    } else if (runAt === 'document_end') {\n");
        sb.append("      if (document.readyState === 'interactive' || document.readyState === 'complete') executeCode();\n");
        sb.append("      else document.addEventListener('DOMContentLoaded', executeCode);\n");
        sb.append("    } else {\n");
        sb.append("      if (document.readyState === 'complete') setTimeout(executeCode, 20);\n");
        sb.append("      else {\n");
        sb.append("        window.addEventListener('load', function() { setTimeout(executeCode, 20); });\n");
        sb.append("        setTimeout(executeCode, 1200);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  } catch(err) { console.error('[Fusion Extension Wrapper Error]:', err); }\n");
        sb.append("})();\n");
        return sb.toString();
    }

    public static void setupWebView(WebView webView) {
        if (webView == null) return;
        try {
            Context ctx = webView.getContext();
            sActiveTabWebViews.add(webView);

            String polyfill = getChromePolyfillScript(ctx, null, false);
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                WebViewCompat.addDocumentStartJavaScript(webView, polyfill, Collections.singleton("*"));

                List<ExtensionInfo> exts = getInstalledExtensions(ctx);
                for (ExtensionInfo ext : exts) {
                    if (!ext.enabled) continue;
                    for (int i = 0; i < ext.contentScripts.size(); i++) {
                        ExtensionInfo.ContentScript cs = ext.contentScripts.get(i);
                        String wrapped = buildContentScriptWrapper(ext, cs, i);
                        if (wrapped != null && !wrapped.isEmpty()) {
                            WebViewCompat.addDocumentStartJavaScript(webView, wrapped, Collections.singleton("*"));
                        }
                    }
                }
                Log.i(TAG, "Berhasil meregistrasikan DocumentStartJavaScript untuk " + exts.size() + " ekstensi.");
            }

            startAllBackgroundHosts(ctx);
        } catch (Throwable e) {
            Log.e(TAG, "Gagal setupWebView ekstensi: " + e.getMessage());
        }
    }

    public static boolean isUrlMatched(String currentUrl, List<String> patterns) {
        if (currentUrl == null || currentUrl.isEmpty()) return false;
        if (patterns == null || patterns.isEmpty()) return true;

        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) continue;
            if (pattern.equals("<all_urls>") || pattern.equals("*://*/*") || pattern.equals("*")) {
                return true;
            }
            try {
                String regex = pattern
                        .replace("\\", "\\\\")
                        .replace(".", "\\.")
                        .replace("*://", "https?://")
                        .replace("http://*", "http://.*")
                        .replace("https://*", "https://.*")
                        .replace("*", ".*");
                if (currentUrl.matches(regex)) {
                    return true;
                }
            } catch (Throwable ignored) {
                if (currentUrl.contains(pattern)) return true;
            }
        }
        return false;
    }

    public static void injectMatchingScripts(WebView webView, String currentUrl, String triggerTiming) {
        if (webView == null || currentUrl == null || currentUrl.isEmpty()) return;
        if (currentUrl.startsWith("file:///android_asset/") || currentUrl.startsWith("about:") || currentUrl.startsWith("javascript:")) {
            return;
        }

        Context ctx = webView.getContext();
        sActiveTabWebViews.add(webView);

        boolean docStartSupported = WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT);

        // Jika DocumentStartScript didukung oleh Chromium WebView engine, polyfill dan scripts
        // ber-timing document_start sudah otomatis dieksekusi sebelum script halaman berjalan.
        if (docStartSupported && "document_start".equalsIgnoreCase(triggerTiming)) {
            return;
        }

        String polyfill = getChromePolyfillScript(ctx, null, false);
        webView.evaluateJavascript(polyfill, null);

        if (currentUrl.startsWith("file://") && currentUrl.contains("/.fusion/extensions/")) {
            String i18nJs = getI18nInjectionScript(currentUrl);
            if (i18nJs != null && !i18nJs.isEmpty()) {
                webView.evaluateJavascript(i18nJs, null);
            }
        }

        List<ExtensionInfo> exts = getInstalledExtensions(ctx);
        for (ExtensionInfo ext : exts) {
            if (!ext.enabled) continue;
            for (int i = 0; i < ext.contentScripts.size(); i++) {
                ExtensionInfo.ContentScript cs = ext.contentScripts.get(i);
                if (isUrlMatched(currentUrl, cs.matches)) {
                    // Jangan injeksi ulang script document_start jika engine sudah menanganinya
                    if (docStartSupported && "document_start".equalsIgnoreCase(cs.runAt)) {
                        continue;
                    }
                    String wrapped = buildContentScriptWrapper(ext, cs, i);
                    if (wrapped != null) {
                        webView.evaluateJavascript(wrapped, null);
                    }
                }
            }
        }
    }

    public static List<File> scanCandidateArchives() {
        List<File> archives = new ArrayList<>();
        String[] candidateDirs = new String[] {
            "/data/data/com.termux/files/home",
            "/data/data/com.termux/files/home/.fusion",
            "/sdcard/Download",
            "/sdcard",
            "/root",
            "/project"
        };
        Set<String> seen = new HashSet<>();

        String[] priorityFiles = new String[] {
            "/data/data/com.termux/files/home/pro.zip",
            "/data/data/com.termux/files/home/.fusion/pro.zip",
            "/sdcard/Download/pro.zip",
            "/root/pro.zip",
            "/project/pro.zip"
        };
        for (String pf : priorityFiles) {
            File f = new File(pf);
            if (f.exists() && f.isFile()) {
                archives.add(f);
                seen.add(f.getAbsolutePath());
                break;
            }
        }

        for (String dirPath : candidateDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            String name = f.getName().toLowerCase();
                            if ((name.endsWith(".zip") || name.endsWith(".crx")) && !name.contains("payload") && !name.contains("gradle")) {
                                if (!seen.contains(f.getAbsolutePath())) {
                                    seen.add(f.getAbsolutePath());
                                    archives.add(f);
                                }
                            }
                        }
                    }
                }
            }
        }
        return archives;
    }

    public static void showExtensionsDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (manager != null) {
            FusionDrawerHelper.showExtensionDrawer(manager);
            return;
        }
        if (act == null) return;
        final Context context = act;
        final List<ExtensionInfo> extensions = getInstalledExtensions(context);

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("🧩 Manajer Ekstensi Fusion (Kiwi-Style)");

        StringBuilder sb = new StringBuilder();
        sb.append("Engine Ekstensi Chrome (Manifest V2 & V3):\n");
        sb.append("📁 Folder: ~/.fusion/extensions/\n");
        sb.append("📊 Terpasang: ").append(extensions.size()).append(" ekstensi\n\n");

        if (extensions.isEmpty()) {
            sb.append("• Belum ada ekstensi .zip / .crx yang terpasang.\n");
            File proZip = new File("/sdcard/Download/pro.zip");
            File homeProZip = new File("/data/data/com.termux/files/home/pro.zip");
            if (proZip.exists() || homeProZip.exists()) {
                sb.append("💡 Ditemukan paket pro.zip! Klik '➕ Pasang .zip / .crx' untuk menginstal langsung.\n");
            } else {
                sb.append("• Tuan bisa memasang file .zip atau .crx ekstensi Chrome desktop langsung ke dalam WebView.\n");
            }
        } else {
            sb.append("Daftar Ekstensi Terpasang (Klik '⚙️ Kelola' untuk opsi/hapus):\n");
            for (int i = 0; i < extensions.size(); i++) {
                ExtensionInfo ext = extensions.get(i);
                sb.append(" [").append(i + 1).append("] ").append(ext.name)
                  .append(" (v").append(ext.version).append(")")
                  .append(ext.enabled ? " [AKTIF]" : " [NONAKTIF]").append("\n");
            }
        }
        b.setMessage(sb.toString());

        b.setPositiveButton("➕ Pasang .zip / .crx", (dialog, which) -> {
            showImportPackageDialog(act, manager);
        });

        if (!extensions.isEmpty()) {
            b.setNeutralButton("⚙️ Kelola Ekstensi", (dialog, which) -> {
                showManageExtensionsDialog(act, manager, extensions);
            });
        } else {
            b.setNeutralButton("📜 Userscript", (dialog, which) -> {
                FusionUserscriptHelper.showUserscriptsDialog(act, manager);
            });
        }

        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public static void showManageExtensionsDialog(final TermuxActivity act, final FusionWebViewManager manager, final List<ExtensionInfo> extensions) {
        if (act == null || extensions.isEmpty()) return;

        String[] items = new String[extensions.size()];
        for (int i = 0; i < extensions.size(); i++) {
            ExtensionInfo ext = extensions.get(i);
            items[i] = (i + 1) + ". " + ext.name + (ext.enabled ? " 🟢" : " ⚪");
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ Pilih Ekstensi untuk Dikelola");
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < extensions.size()) {
                showExtensionActionsDialog(act, manager, extensions.get(which));
            }
        });
        b.setNegativeButton("Kembali", (dialog, which) -> {
            showExtensionsDialog(act, manager);
        });
        b.show();
    }

    public static String getI18nInjectionScript(String currentUrl) {
        if (currentUrl == null || !currentUrl.startsWith("file://")) return null;
        try {
            Uri uri = Uri.parse(currentUrl);
            String path = uri.getPath();
            if (path == null) return null;
            File file = new File(path);
            File extDir = file.isDirectory() ? file : file.getParentFile();
            while (extDir != null && extDir.exists() && !new File(extDir, "manifest.json").exists()) {
                if (!extDir.getAbsolutePath().contains(".fusion/extensions")) break;
                extDir = extDir.getParentFile();
            }
            if (extDir == null || !new File(extDir, "manifest.json").exists()) return null;

            File localesDir = new File(extDir, "_locales");
            if (!localesDir.exists() || !localesDir.isDirectory()) return null;

            String defaultLocale = "en";
            File mf = new File(extDir, "manifest.json");
            byte[] mfBytes = readFileBytesSafe(mf);
            if (mfBytes != null && mfBytes.length > 0) {
                try {
                    JSONObject root = new JSONObject(new String(mfBytes, StandardCharsets.UTF_8));
                    defaultLocale = root.optString("default_locale", "en");
                } catch (Throwable ignored) {}
            }

            File messagesFile = new File(new File(localesDir, defaultLocale), "messages.json");
            if (!messagesFile.exists()) {
                messagesFile = new File(new File(localesDir, "en"), "messages.json");
            }
            if (!messagesFile.exists()) {
                File[] subs = localesDir.listFiles();
                if (subs != null) {
                    for (File s : subs) {
                        File mfCandidate = new File(s, "messages.json");
                        if (mfCandidate.exists()) {
                            messagesFile = mfCandidate;
                            break;
                        }
                    }
                }
            }

            if (messagesFile != null && messagesFile.exists()) {
                byte[] b = readFileBytesSafe(messagesFile);
                if (b != null && b.length > 0) {
                    String jsonStr = new String(b, StandardCharsets.UTF_8);
                    return "(function() { try { window.__fusionI18n = " + jsonStr + "; } catch(e) {} })();";
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void showExtensionActionsDialog(final TermuxActivity act, final FusionWebViewManager manager, final ExtensionInfo ext) {
        if (act == null || ext == null) return;

        boolean isGapt = (ext.id != null && (ext.id.toLowerCase().contains("google_ai_pro") || ext.id.toLowerCase().contains("gapt") || ext.name.toLowerCase().contains("google ai pro")));

        List<String> options = new ArrayList<>();
        options.add(ext.enabled ? "⚪ Matikan Ekstensi" : "🟢 Aktifkan Ekstensi");
        if (isGapt) {
            options.add("⚙️ Setelan Layer / Opsi Token (Google AI Pro)");
        }
        if (ext.popupHtml != null && !ext.popupHtml.isEmpty()) {
            options.add("🌐 Buka Popup HTML (" + ext.popupHtml + ")");
        }
        File homeHtmlFile = new File(ext.dir, "home.html");
        if (homeHtmlFile.exists() && !"home.html".equalsIgnoreCase(ext.popupHtml)) {
            options.add("🏠 Buka Halaman Penuh Ekstensi (home.html)");
        }
        options.add("🗑️ Hapus Ekstensi");

        String[] itemArray = options.toArray(new String[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ " + ext.name);
        b.setItems(itemArray, (dialog, which) -> {
            String selected = itemArray[which];
            if (selected.contains("Matikan") || selected.contains("Aktifkan")) {
                ext.enabled = !ext.enabled;
                Map<String, Boolean> stateMap = loadExtensionsState(act);
                stateMap.put(ext.id, ext.enabled);
                saveExtensionsState(act, stateMap);
                FusionToast.makeText(act, ext.name + (ext.enabled ? " diaktifkan." : " dinonaktifkan."), Toast.LENGTH_SHORT).show();
                if (ext.enabled && ext.hasBackground()) {
                    ensureBackgroundHostRunning(act, ext);
                } else if (!ext.enabled) {
                    stopBackgroundHost(ext.id);
                }
                if (manager != null && manager.mActiveWebView != null) {
                    setupWebView(manager.mActiveWebView);
                }
                showExtensionsDialog(act, manager);
            } else if (selected.contains("Setelan Layer")) {
                showLayerConfigDialog(act, manager, ext);
            } else if (selected.contains("Buka Popup")) {
                if (manager != null && ext.popupHtml != null) {
                    File popupFile = new File(ext.dir, ext.popupHtml);
                    manager.addNewTab("file://" + popupFile.getAbsolutePath());
                    manager.closeAllDrawers();
                }
            } else if (selected.contains("Buka Halaman Penuh")) {
                if (manager != null) {
                    File homeFile = new File(ext.dir, "home.html");
                    if (homeFile.exists()) {
                        manager.addNewTab("file://" + homeFile.getAbsolutePath());
                        manager.closeAllDrawers();
                    }
                }
            } else if (selected.contains("Hapus")) {
                AlertDialog.Builder delConfirm = new AlertDialog.Builder(act);
                delConfirm.setTitle("Hapus Ekstensi?");
                delConfirm.setMessage("Yakin ingin menghapus ekstensi \"" + ext.name + "\"?");
                delConfirm.setPositiveButton("Hapus", (d, w) -> {
                    stopBackgroundHost(ext.id);
                    boolean deleted = deleteRecursive(ext.dir);
                    Map<String, Boolean> stateMap = loadExtensionsState(act);
                    stateMap.remove(ext.id);
                    saveExtensionsState(act, stateMap);

                    if (deleted) {
                        FusionToast.makeText(act, "Ekstensi \"" + ext.name + "\" berhasil dihapus.", Toast.LENGTH_SHORT).show();
                    } else {
                        FusionToast.makeText(act, "Gagal menghapus berkas ekstensi.", Toast.LENGTH_SHORT).show();
                    }
                    showExtensionsDialog(act, manager);
                });
                delConfirm.setNegativeButton("Batal", null);
                delConfirm.show();
            }
        });
        b.setNegativeButton("Kembali", (dialog, which) -> {
            showExtensionsDialog(act, manager);
        });
        b.show();
    }

    public static void showLayerConfigDialog(final TermuxActivity act, final FusionWebViewManager manager, final ExtensionInfo ext) {
        if (act == null) return;
        String currentLayer = getLayerConfig(act);

        final String[] layers = new String[] {
            "12m (🟢 12 Bulan Free $0.00 — AI Pro 2TB)",
            "6m_ss (🟡 6 Bulan Free — AI Pro SS)",
            "6m (🟡 6 Bulan Free — Tier 1)",
            "6m_2 (🟡 6 Bulan Free — Tier 2)",
            "off (🔴 Matikan Injeksi Offer Token)"
        };
        final String[] layerCodes = new String[] {
            "12m", "6m_ss", "6m", "6m_2", "off"
        };

        int selectedIdx = 0;
        for (int i = 0; i < layerCodes.length; i++) {
            if (layerCodes[i].equalsIgnoreCase(currentLayer)) {
                selectedIdx = i;
                break;
            }
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ Setelan Penawaran Google AI Pro");
        b.setSingleChoiceItems(layers, selectedIdx, (dialog, which) -> {
            if (which >= 0 && which < layerCodes.length) {
                String chosen = layerCodes[which];
                setLayerConfig(act, chosen);
                FusionToast.makeText(act, "✅ Layer AI Pro disetel ke: " + chosen, Toast.LENGTH_SHORT).show();

                if (manager != null && manager.mActiveWebView != null) {
                    manager.mActiveWebView.evaluateJavascript(
                        "(function() {\n" +
                        "  try {\n" +
                        "    if (window.chrome && window.chrome.storage && window.chrome.storage.local) {\n" +
                        "      window.chrome.storage.local.set({ gapt_layer: '" + chosen + "' });\n" +
                        "    }\n" +
                        "    window.postMessage({ __gapt: 1, type: 'cfg', layer: '" + chosen + "' }, '*');\n" +
                        "  } catch(e) {}\n" +
                        "})();", null);
                }
                dialog.dismiss();
            }
        });
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public static void showImportPackageDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null) return;
        final List<File> archives = scanCandidateArchives();

        List<String> displayNames = new ArrayList<>();
        displayNames.add("📁 Buka File Manager Android (SAF - Non-Root / SDCard)");

        for (File f : archives) {
            String sizeKb = (f.length() / 1024) + " KB";
            if (f.getName().equalsIgnoreCase("pro.zip")) {
                displayNames.add("⭐ pro.zip (" + f.getParent() + ") [" + sizeKb + "]");
            } else {
                displayNames.add("📦 " + f.getName() + " (" + f.getParent() + ") [" + sizeKb + "]");
            }
        }
        displayNames.add("✏️ Masukkan Path File Manual (.zip / .crx)");

        String[] items = displayNames.toArray(new String[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("📦 Pilih Paket Ekstensi (.zip / .crx)");
        b.setItems(items, (dialog, which) -> {
            if (which == 0) {
                launchExtensionPicker(act);
            } else if (which > 0 && which <= archives.size()) {
                File selected = archives.get(which - 1);
                boolean ok = installPackage(act, selected);
                if (ok) {
                    FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang dari " + selected.getName(), Toast.LENGTH_LONG).show();
                    if (manager != null && manager.mActiveWebView != null) {
                        setupWebView(manager.mActiveWebView);
                    }
                    showExtensionsDialog(act, manager);
                } else {
                    FusionToast.makeText(act, "❌ Gagal memasang ekstensi: berkas arsip tidak dapat diekstrak.", Toast.LENGTH_LONG).show();
                }
            } else {
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(act);
                inputDialog.setTitle("Ketik Path Paket Ekstensi");
                final EditText input = new EditText(act);
                input.setHint("/data/data/com.termux/files/home/pro.zip");
                input.setText("/data/data/com.termux/files/home/pro.zip");
                inputDialog.setView(input);
                inputDialog.setPositiveButton("Pasang", (d, w) -> {
                    String p = input.getText().toString().trim();
                    File customFile = new File(p);
                    boolean ok = installPackage(act, customFile);
                    if (ok) {
                        FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang!", Toast.LENGTH_LONG).show();
                        if (manager != null && manager.mActiveWebView != null) {
                            setupWebView(manager.mActiveWebView);
                        }
                        showExtensionsDialog(act, manager);
                    } else {
                        FusionToast.makeText(act, "❌ Gagal pasang: file bukan zip/crx ekstensi valid atau tidak dapat diakses.", Toast.LENGTH_LONG).show();
                    }
                });
                inputDialog.setNegativeButton("Batal", null);
                inputDialog.show();
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }
}
