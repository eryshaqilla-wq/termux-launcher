package com.termux.webview;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.CookieManager;
import com.termux.app.FusionToast;
import com.termux.app.TermuxIpcServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal Native Bridge for Android WebView.
 * Provides dynamic reflection, runtime WebSettings updates, zero-rebuild network blackholes & mocks,
 * Phoenix Auto-Healing tuning, and performance lock control directly from Userscripts.
 */
public class FusionNativeBridge {
    private static final String TAG = "FusionNativeBridge";

    private final FusionWebViewManager mManager;
    private final WebView mWebView;
    private final Context mContext;

    public FusionNativeBridge(FusionWebViewManager manager, WebView webView) {
        this.mManager = manager;
        this.mWebView = webView;
        this.mContext = (manager != null && manager.mActivity != null)
                ? manager.mActivity
                : (webView != null ? webView.getContext() : null);
    }

    /**
     * ⚡ Java Reflection Gateway:
     * Allows Userscripts to dynamically call any Android SDK class or system service
     * without modifying Java code or rebuilding the APK.
     */
    @JavascriptInterface
    public String reflect(String className, String methodName, String jsonArgs) {
        JSONObject resp = new JSONObject();
        try {
            if (className == null || className.trim().isEmpty() || methodName == null || methodName.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("error", "className and methodName are required");
                return resp.toString();
            }

            // 1. Resolve target Class & Instance (with shorthand system service support)
            Class<?> targetClass = null;
            Object targetInstance = null;

            String lower = className.trim().toLowerCase();
            if (mContext != null) {
                if (lower.equals("vibrator") || className.equals("android.os.Vibrator")) {
                    targetInstance = mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    targetClass = Vibrator.class;
                } else if (lower.equals("batterymanager") || className.equals("android.os.BatteryManager")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        targetInstance = mContext.getSystemService(Context.BATTERY_SERVICE);
                        targetClass = BatteryManager.class;
                    }
                } else if (lower.equals("wifimanager") || className.equals("android.net.wifi.WifiManager")) {
                    targetInstance = mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    targetClass = WifiManager.class;
                } else if (lower.equals("activitymanager") || className.equals("android.app.ActivityManager")) {
                    targetInstance = mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    targetClass = ActivityManager.class;
                }
            }

            if (targetClass == null) {
                String trimmed = className.trim();
                if ("java.lang.Runtime".equals(trimmed)
                        || "java.lang.ProcessBuilder".equals(trimmed)
                        || "java.lang.System".equals(trimmed)
                        || "dalvik.system.DexClassLoader".equals(trimmed)
                        || "dalvik.system.PathClassLoader".equals(trimmed)) {
                    resp.put("success", false);
                    resp.put("error", "Blocked dangerous reflection class: " + trimmed);
                    return resp.toString();
                }
                targetClass = Class.forName(trimmed);
            }

            // 2. Parse arguments
            List<Object> argsList = new ArrayList<>();
            if (jsonArgs != null && !jsonArgs.trim().isEmpty() && !jsonArgs.equals("[]")) {
                JSONArray arr = new JSONArray(jsonArgs);
                for (int i = 0; i < arr.length(); i++) {
                    argsList.add(arr.get(i));
                }
            }

            // 3. Find matching method
            Method targetMethod = null;
            Method[] methods = targetClass.getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName.trim()) && m.getParameterTypes().length == argsList.size()) {
                    targetMethod = m;
                    break;
                }
            }

            if (targetMethod == null) {
                resp.put("success", false);
                resp.put("error", "Method not found: " + methodName + " with " + argsList.size() + " args on " + targetClass.getName());
                return resp.toString();
            }

            // 4. Coerce arguments to parameter types
            Class<?>[] paramTypes = targetMethod.getParameterTypes();
            Object[] finalArgs = new Object[argsList.size()];
            for (int i = 0; i < argsList.size(); i++) {
                Object val = argsList.get(i);
                Class<?> pt = paramTypes[i];
                if (pt == int.class || pt == Integer.class) {
                    finalArgs[i] = (val instanceof Number) ? ((Number) val).intValue() : Integer.parseInt(val.toString());
                } else if (pt == long.class || pt == Long.class) {
                    finalArgs[i] = (val instanceof Number) ? ((Number) val).longValue() : Long.parseLong(val.toString());
                } else if (pt == boolean.class || pt == Boolean.class) {
                    finalArgs[i] = (val instanceof Boolean) ? (Boolean) val : Boolean.parseBoolean(val.toString());
                } else if (pt == double.class || pt == Double.class) {
                    finalArgs[i] = (val instanceof Number) ? ((Number) val).doubleValue() : Double.parseDouble(val.toString());
                } else if (pt == String.class) {
                    finalArgs[i] = val.toString();
                } else {
                    finalArgs[i] = val;
                }
            }

            // 5. Invoke (static or instance)
            Object invTarget = Modifier.isStatic(targetMethod.getModifiers()) ? null : targetInstance;
            if (invTarget == null && !Modifier.isStatic(targetMethod.getModifiers())) {
                try {
                    invTarget = targetClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    // Instance could not be created with default constructor
                }
            }

            Object result = targetMethod.invoke(invTarget, finalArgs);
            resp.put("success", true);
            resp.put("result", (result != null) ? result.toString() : null);
            return resp.toString();
        } catch (Throwable t) {
            Log.e(TAG, "Reflection invocation error", t);
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
            return resp.toString();
        }
    }

    /**
     * ⚡ Dynamic WebSettings Updater:
     * Applies Chromium WebSettings on-the-fly at runtime from Userscripts.
     */
    @JavascriptInterface
    public String applySettings(final String jsonSettings) {
        JSONObject resp = new JSONObject();
        if (mWebView == null || jsonSettings == null) {
            try { resp.put("success", false); } catch (Exception ignored) {}
            return resp.toString();
        }

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = new JSONObject(jsonSettings);
                    WebSettings settings = mWebView.getSettings();
                    if (settings == null) return;

                    if (obj.has("safeBrowsing") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        settings.setSafeBrowsingEnabled(obj.getBoolean("safeBrowsing"));
                    }
                    if (obj.has("offscreenPreRaster") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        settings.setOffscreenPreRaster(obj.getBoolean("offscreenPreRaster"));
                    }
                    if (obj.has("hardwareLayer")) {
                        boolean hw = obj.getBoolean("hardwareLayer");
                        mWebView.setLayerType(hw ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
                    }
                    if (obj.has("cacheMode")) {
                        String mode = obj.getString("cacheMode").toUpperCase();
                        if ("LOAD_NO_CACHE".equals(mode)) settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                        else if ("LOAD_CACHE_ONLY".equals(mode)) settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                        else if ("LOAD_CACHE_ELSE_NETWORK".equals(mode)) settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        else settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                    }
                    if (obj.has("textZoom")) {
                        settings.setTextZoom(obj.getInt("textZoom"));
                    }
                    if (obj.has("userAgent")) {
                        settings.setUserAgentString(obj.getString("userAgent"));
                    }
                    if (obj.has("blockNetworkLoads")) {
                        settings.setBlockNetworkLoads(obj.getBoolean("blockNetworkLoads"));
                    }
                    if (obj.has("mixedContentMode") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.setMixedContentMode(obj.getInt("mixedContentMode"));
                    }
                    Log.i(TAG, "Applied dynamic WebSettings from Userscript: " + jsonSettings);
                } catch (Throwable t) {
                    Log.e(TAG, "Error applying dynamic WebSettings", t);
                }
            }
        });

        try { resp.put("success", true); } catch (Exception ignored) {}
        return resp.toString();
    }

    /**
     * ⚡ Dynamic Network Blackholes:
     * Drops tracker, analytics, or telemetry URLs directly in Java memory (0ms) without APK rebuild.
     */
    @JavascriptInterface
    public String setBlackholes(String jsonArrayRegex) {
        JSONObject resp = new JSONObject();
        try {
            JSONArray arr = new JSONArray(jsonArrayRegex);
            int count = 0;
            for (int i = 0; i < arr.length(); i++) {
                String pattern = arr.getString(i);
                if (FusionNetworkInterceptor.addBlockRule(pattern)) {
                    count++;
                }
            }
            resp.put("success", true);
            resp.put("count", count);
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * ⚡ Dynamic Mock Rules:
     * Injects mocked HTTP responses into WebViewClient.shouldInterceptRequest.
     */
    @JavascriptInterface
    public String setMockRules(String jsonRulesArray) {
        JSONObject resp = new JSONObject();
        try {
            JSONArray arr = new JSONArray(jsonRulesArray);
            int count = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject r = arr.getJSONObject(i);
                String pattern = r.getString("pattern");
                String body = r.optString("body", "");
                int status = r.optInt("statusCode", 200);
                String mime = r.optString("mimeType", "application/json");
                String headers = r.optString("headers", "{}");
                if (FusionNetworkInterceptor.addMockRule(pattern, body, status, mime, headers)) {
                    count++;
                }
            }
            resp.put("success", true);
            resp.put("count", count);
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * ⚡ Clear All Interception Rules & Reload External Engine Scripts:
     */
    @JavascriptInterface
    public String clearNetworkRules() {
        FusionNetworkInterceptor.clearRules();
        return "{\"success\":true}";
    }

    @JavascriptInterface
    public String addLocalMirror(String urlPattern, String localDirPath) {
        boolean ok = FusionNetworkInterceptor.addLocalMirrorRule(urlPattern, localDirPath);
        return ok ? "{\"success\":true}" : "{\"success\":false}";
    }

    @JavascriptInterface
    public String reloadEngineScripts() {
        FusionWebViewManager.clearAssetScriptCache();
        if (mManager != null && mManager.mActivity != null && mWebView != null) {
            mManager.mActivity.runOnUiThread(() -> mManager.injectScripts(mWebView));
        }
        return "{\"success\":true}";
    }

    /**
     * 🛡️ Phoenix Auto-Healing Policy:
     * Tunes crash tolerance, resurrection limit, and cooldown period.
     */
    @JavascriptInterface
    public String setPhoenixPolicy(String jsonPolicy) {
        JSONObject resp = new JSONObject();
        try {
            JSONObject obj = new JSONObject(jsonPolicy);
            if (obj.has("maxRetries")) {
                FusionWebViewClient.sMaxPhoenixRetries = obj.getInt("maxRetries");
            }
            if (obj.has("cooldownMs")) {
                FusionWebViewClient.sPhoenixCooldownMs = obj.getLong("cooldownMs");
            }
            if (obj.has("autoResurrect")) {
                FusionWebViewClient.sAutoResurrectEnabled = obj.getBoolean("autoResurrect");
            }
            if (obj.has("strictSsl")) {
                FusionWebViewClient.sStrictSslCheck = obj.getBoolean("strictSsl");
            }
            if (obj.has("builtinTrackerBlock")) {
                FusionWebViewClient.sEnableBuiltinTrackerBlock = obj.getBoolean("builtinTrackerBlock");
            }
            if (obj.has("allowGeolocation")) {
                FusionWebChromeClient.sAllowGeolocation = obj.getBoolean("allowGeolocation");
            }
            if (obj.has("allowSensitivePermissions")) {
                FusionWebChromeClient.sAllowSensitivePermissions = obj.getBoolean("allowSensitivePermissions");
            }
            if (obj.has("allowDrmId")) {
                FusionWebChromeClient.sAllowDrmId = obj.getBoolean("allowDrmId");
            }
            if (obj.has("allowCamera")) {
                FusionWebChromeClient.sAllowCamera = obj.getBoolean("allowCamera");
            }
            if (obj.has("allowMic")) {
                FusionWebChromeClient.sAllowMic = obj.getBoolean("allowMic");
            }
            resp.put("success", true);
            resp.put("maxRetries", FusionWebViewClient.sMaxPhoenixRetries);
            resp.put("cooldownMs", FusionWebViewClient.sPhoenixCooldownMs);
            resp.put("autoResurrect", FusionWebViewClient.sAutoResurrectEnabled);
            resp.put("strictSsl", FusionWebViewClient.sStrictSslCheck);
            resp.put("builtinTrackerBlock", FusionWebViewClient.sEnableBuiltinTrackerBlock);
            resp.put("allowGeolocation", FusionWebChromeClient.sAllowGeolocation);
            resp.put("allowSensitivePermissions", FusionWebChromeClient.sAllowSensitivePermissions);
            resp.put("allowDrmId", FusionWebChromeClient.sAllowDrmId);
            resp.put("allowCamera", FusionWebChromeClient.sAllowCamera);
            resp.put("allowMic", FusionWebChromeClient.sAllowMic);
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * ⚡ Performance & Lock Controller:
     * Toggles High-Perf Wi-Fi lock and CPU wake locks.
     */
    @JavascriptInterface
    public String setPerformanceMode(String jsonPerformance) {
        JSONObject resp = new JSONObject();
        try {
            JSONObject obj = new JSONObject(jsonPerformance);
            if (mManager != null) {
                if (obj.optBoolean("wakeLock", false)) {
                    mManager.acquireWakeLock();
                } else if (obj.has("wakeLock") && !obj.getBoolean("wakeLock")) {
                    mManager.releaseWakeLock();
                }
            }
            resp.put("success", true);
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * 📊 Device & Hardware Telemetry:
     * Reads battery status, memory metrics, and OS details for anti-sybil and debugging.
     */
    @JavascriptInterface
    public String getDeviceInfo() {
        JSONObject resp = new JSONObject();
        try {
            resp.put("osVersion", Build.VERSION.RELEASE);
            resp.put("sdkInt", Build.VERSION.SDK_INT);
            resp.put("model", Build.MODEL);
            resp.put("manufacturer", Build.MANUFACTURER);

            if (mContext != null) {
                // Battery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    BatteryManager bm = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
                    if (bm != null) {
                        resp.put("batteryLevel", bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
                    }
                }
                // Memory
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    resp.put("availMemMB", mi.availMem / (1024 * 1024));
                    resp.put("totalMemMB", mi.totalMem / (1024 * 1024));
                    resp.put("lowMemory", mi.lowMemory);
                }
            }
            resp.put("success", true);
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * 💻 IPC Terminal Dispatcher:
     * Directly sends commands to Termux IPC server from within the page context.
     */
    @JavascriptInterface
    public String execIpc(String command) {
        JSONObject resp = new JSONObject();
        try {
            if (command == null || command.trim().isEmpty()) {
                resp.put("success", false);
                resp.put("error", "Command is empty");
                return resp.toString();
            }
            com.termux.app.TermuxActivity act = (mManager != null && mManager.mActivity != null)
                    ? mManager.mActivity
                    : TermuxIpcServer.sActivity;
            if (act != null) {
                TermuxIpcServer.injectCommand(act, command, true);
                resp.put("success", true);
            } else {
                resp.put("success", false);
                resp.put("error", "TermuxActivity not active");
            }
        } catch (Throwable t) {
            try {
                resp.put("success", false);
                resp.put("error", t.getMessage());
            } catch (Exception ignored) {}
        }
        return resp.toString();
    }

    /**
     * 🍪 Native Cookie Management:
     * Directly interacts with Android's SQLite CookieManager (including HttpOnly).
     */
    @JavascriptInterface
    public String setCookie(String url, String cookie) {
        try {
            CookieManager cm = FusionProfileManager.getCookieManagerForWebView(this.mWebView);
            cm.setCookie(url, cookie);
            cm.flush();
            return "{\"success\":true}";
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String getCookie(String url) {
        try {
            CookieManager cm = FusionProfileManager.getCookieManagerForWebView(this.mWebView);
            String c = cm.getCookie(url);
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("cookie", (c != null) ? c : "");
            return resp.toString();
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String clearCookies() {
        try {
            CookieManager cm = FusionProfileManager.getCookieManagerForWebView(this.mWebView);
            cm.removeAllCookies(null);
            cm.flush();
            return "{\"success\":true}";
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    /**
     * 📋 Native System Clipboard:
     * Instant 0ms read and write to Android system clipboard.
     */
    @JavascriptInterface
    public String getClipboard() {
        try {
            String clip = TermuxIpcServer.getClipboard();
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("text", (clip != null) ? clip : "");
            return resp.toString();
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String setClipboard(String text) {
        try {
            TermuxIpcServer.setClipboard(text != null ? text : "");
            return "{\"success\":true}";
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    /**
     * 🔔 Native Floating Toast:
     */
    @JavascriptInterface
    public void showToast(final String message) {
        if (mContext != null && message != null) {
            if (mManager != null && mManager.mActivity != null) {
                mManager.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FusionToast.makeText(mContext, (CharSequence) message, 0).show();
                    }
                });
            } else {
                FusionToast.makeText(mContext, (CharSequence) message, 0).show();
            }
        }
    }

    /**
     * 📥 Download Tracker:
     */
    @JavascriptInterface
    public String getLastDownloadedFile() {
        try {
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("path", FusionDownloadListener.getLastDownloadedFilePath());
            return resp.toString();
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":\"" + t.getMessage() + "\"}";
        }
    }

    /**
     * 📑 Native Tab Management:
     */
    @JavascriptInterface
    public String listTabs() {
        return (mManager != null) ? mManager.getTabsJson() : "[]";
    }

    @JavascriptInterface
    public String switchTab(final int index) {
        if (mManager == null) return "{\"success\":false,\"error\":\"Manager is null\"}";
        if (mManager.mActivity != null) {
            mManager.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mManager.switchTab(index);
                }
            });
        } else {
            mManager.switchTab(index);
        }
        return "{\"success\":true}";
    }

    @JavascriptInterface
    public String closeTab(final int index) {
        if (mManager == null) return "{\"success\":false,\"error\":\"Manager is null\"}";
        if (mManager.mActivity != null) {
            mManager.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mManager.killTab(index);
                }
            });
        } else {
            mManager.killTab(index);
        }
        return "{\"success\":true}";
    }
}
