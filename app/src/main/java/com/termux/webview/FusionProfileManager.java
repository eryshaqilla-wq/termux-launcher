package com.termux.webview;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;

import androidx.webkit.Profile;
import androidx.webkit.ProfileStore;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FusionProfileManager {
    private static final String TAG = "FusionProfileManager";
    private static String sCurrentActiveProfile = "default";
    private static final Set<String> sInitializedProfiles = new HashSet<>();

    public static class DeviceProfile {
        public final String name;
        public final String model;
        public final String brand;
        public final String userAgent;
        public final String platform;
        public final String vendor;
        public final String renderer;
        public final int cores;
        public final int memory;
        public final int screenWidth;
        public final int screenHeight;
        public final double pixelRatio;

        public DeviceProfile(String name, String model, String brand, String userAgent, String platform, String vendor, String renderer, int cores, int memory, int screenWidth, int screenHeight, double pixelRatio) {
            this.name = name;
            this.model = model;
            this.brand = brand;
            this.userAgent = userAgent;
            this.platform = platform;
            this.vendor = vendor;
            this.renderer = renderer;
            this.cores = cores;
            this.memory = memory;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.pixelRatio = pixelRatio;
        }

        public String toJsonString() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("model", model);
                obj.put("brand", brand);
                obj.put("userAgent", userAgent);
                obj.put("platform", platform);
                obj.put("vendor", vendor);
                obj.put("renderer", renderer);
                obj.put("cores", cores);
                obj.put("memory", memory);
                obj.put("screenWidth", screenWidth);
                obj.put("screenHeight", screenHeight);
                obj.put("pixelRatio", pixelRatio);
                return obj.toString();
            } catch (Exception e) {
                return "{}";
            }
        }

        public static DeviceProfile fromJsonObject(JSONObject obj) {
            return new DeviceProfile(
                obj.optString("name", "Samsung Galaxy S24 Ultra"),
                obj.optString("model", "SM-S928B"),
                obj.optString("brand", "Samsung"),
                obj.optString("userAgent", "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36"),
                obj.optString("platform", "Linux armv8l"),
                obj.optString("vendor", "Qualcomm"),
                obj.optString("renderer", "Adreno (TM) 750"),
                obj.optInt("cores", 8),
                obj.optInt("memory", 12),
                obj.optInt("screenWidth", 1440),
                obj.optInt("screenHeight", 3120),
                obj.optDouble("pixelRatio", 3.0)
            );
        }
    }

    private static final DeviceProfile[] DEVICE_POOL = new DeviceProfile[] {
        new DeviceProfile(
            "Samsung Galaxy S24 Ultra", "SM-S928B", "Samsung",
            "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 12, 1440, 3120, 3.0
        ),
        new DeviceProfile(
            "Google Pixel 8 Pro", "Pixel 8 Pro", "Google",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux aarch64", "ARM", "Mali-G715 Immortalis MC10", 8, 12, 1344, 2992, 2.8125
        ),
        new DeviceProfile(
            "Xiaomi 14 Pro", "23116PN5BC", "Xiaomi",
            "Mozilla/5.0 (Linux; Android 14; 23116PN5BC) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 12, 1440, 3200, 3.0
        ),
        new DeviceProfile(
            "OnePlus 12", "CPH2581", "OnePlus",
            "Mozilla/5.0 (Linux; Android 14; CPH2581) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16, 1440, 3168, 3.0
        ),
        new DeviceProfile(
            "Asus ROG Phone 8 Pro", "ASUS_AI2401", "Asus",
            "Mozilla/5.0 (Linux; Android 14; ASUS_AI2401) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16, 1080, 2400, 2.25
        ),
        new DeviceProfile(
            "Vivo X100 Pro", "V2324A", "vivo",
            "Mozilla/5.0 (Linux; Android 14; V2324A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "ARM", "Mali-G720 Immortalis MC12", 8, 16, 1260, 2800, 2.8125
        ),
        new DeviceProfile(
            "POCO F6 Pro", "23113RKC6G", "POCO",
            "Mozilla/5.0 (Linux; Android 14; 23113RKC6G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 740", 8, 12, 1440, 3200, 3.0
        ),
        new DeviceProfile(
            "Sony Xperia 1 V", "XQ-DQ72", "Sony",
            "Mozilla/5.0 (Linux; Android 14; XQ-DQ72) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 740", 8, 12, 1644, 3840, 3.5
        ),
        new DeviceProfile(
            "Infinix GT 20 Pro", "X6871", "Infinix",
            "Mozilla/5.0 (Linux; Android 14; X6871) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "ARM", "Mali-G610 MC6", 8, 8, 1080, 2436, 2.25
        ),
        new DeviceProfile(
            "Oppo Find X7 Ultra", "PHY110", "Oppo",
            "Mozilla/5.0 (Linux; Android 14; PHY110) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16, 1440, 3168, 3.0
        ),
        new DeviceProfile(
            "Realme GT 5 Pro", "RMX3888", "realme",
            "Mozilla/5.0 (Linux; Android 14; RMX3888) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16, 1264, 2780, 2.75
        ),
        new DeviceProfile(
            "Motorola Edge 50 Ultra", "XT2401-1", "Motorola",
            "Mozilla/5.0 (Linux; Android 14; XT2401-1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 735", 8, 12, 1220, 2712, 2.7
        ),
        new DeviceProfile(
            "Honor Magic 6 Pro", "BVL-AN16", "Honor",
            "Mozilla/5.0 (Linux; Android 14; BVL-AN16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 12, 1280, 2800, 2.8125
        ),
        new DeviceProfile(
            "iQOO 12 Pro", "V2329A", "iQOO",
            "Mozilla/5.0 (Linux; Android 14; V2329A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16, 1440, 3200, 3.0
        ),
        new DeviceProfile(
            "Nothing Phone (2)", "A065", "Nothing",
            "Mozilla/5.0 (Linux; Android 14; A065) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 730", 8, 12, 1080, 2412, 2.25
        ),
        new DeviceProfile(
            "Google Pixel 9 Pro XL", "Pixel 9 Pro XL", "Google",
            "Mozilla/5.0 (Linux; Android 14; Pixel 9 Pro XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux aarch64", "ARM", "Mali-G715", 8, 16, 1344, 2992, 2.8125
        )
    };

    public static File getSessionsDir() {
        String[] candidates = new String[] {
            System.getenv("FUSION_WALLET_DIR") != null ? System.getenv("FUSION_WALLET_DIR") + "/sessions" : null,
            "/data/data/com.termux/files/home/wallet/sessions",
            "/data/local/nhsystem/kali-arm64/root/wallet/sessions",
            "/root/wallet/sessions",
            "/wallet/sessions"
        };
        for (String path : candidates) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    return dir;
                }
            }
        }
        File def = new File("/data/data/com.termux/files/home/wallet/sessions");
        def.mkdirs();
        return def;
    }

    public static File getSessionFile(String profile) {
        File dir = getSessionsDir();
        return new File(dir, profile + ".json");
    }

    public static File getDeviceFile(String profile) {
        File dir = getSessionsDir();
        return new File(dir, profile + ".device.json");
    }

    public static void saveDeviceProfile(String profile, DeviceProfile dev) {
        if (profile == null || profile.isEmpty() || profile.equals("default") || dev == null) return;
        try {
            File f = getDeviceFile(profile);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(dev.toJsonString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            try { f.setReadable(true, false); f.setWritable(true, false); } catch(Exception ignored) {}
        } catch(Exception e) {
            Log.e(TAG, "Error saving device profile: " + e.getMessage());
        }
    }

    public static DeviceProfile getDeviceForProfile(String profile) {
        if (profile == null || profile.isEmpty() || profile.equals("default")) {
            return DEVICE_POOL[0];
        }
        String p = profile.toLowerCase().trim();

        // 1. Cek apakah identitas tersimpan permanen di file <profile>.device.json
        File devFile = getDeviceFile(p);
        if (devFile.exists() && devFile.length() > 0) {
            try {
                FileInputStream fis = new FileInputStream(devFile);
                byte[] b = new byte[(int) devFile.length()];
                fis.read(b);
                fis.close();
                JSONObject json = new JSONObject(new String(b, StandardCharsets.UTF_8));
                return DeviceProfile.fromJsonObject(json);
            } catch (Exception e) {
                Log.e(TAG, "Error loading saved device identity for " + p + ": " + e.getMessage());
            }
        }

        // 2. Pemetaan Eksklusif untuk Core Wallet Tuan Eso & Fallback Pool Luas
        DeviceProfile dev;
        if ("shaqilla".equals(p)) {
            dev = DEVICE_POOL[0]; // Samsung Galaxy S24 Ultra
        } else if ("eryziggy".equals(p)) {
            dev = DEVICE_POOL[1]; // Google Pixel 8 Pro
        } else if ("0xjamrud".equals(p)) {
            dev = DEVICE_POOL[3]; // OnePlus 12
        } else if ("wallet_1".equals(p)) {
            dev = DEVICE_POOL[2]; // Xiaomi 14 Pro
        } else {
            // Untuk semua profil baru, pilih model secara deterministik dari 16 pool flagship
            int hash = Math.abs(p.hashCode());
            int index = hash % DEVICE_POOL.length;
            dev = DEVICE_POOL[index];
        }

        // 3. Kunci dan simpan identitas ke disk agar permanen selamanya
        saveDeviceProfile(p, dev);
        return dev;
    }

    /**
     * Konfigurasi WebView dengan Native Chromium Profile & Document Start Stealth Script.
     * Harus dipanggil tepat saat WebView baru dibuat (sebelum navigasi URL).
     */
    public static void setupProfileForWebView(WebView webView, String profileName, boolean isDesktop) {
        if (webView == null) return;
        if (profileName == null || profileName.isEmpty()) {
            profileName = "default";
        }

        DeviceProfile device = getDeviceForProfile(profileName);
        WebSettings settings = webView.getSettings();
        if (isDesktop) {
            settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        } else {
            settings.setUserAgentString(device.userAgent);
        }

        // 1. Native Chromium Multi-Profile Isolation
        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            try {
                ProfileStore store = ProfileStore.getInstance();
                Profile profile = store.getOrCreateProfile(profileName);
                WebViewCompat.setProfile(webView, profileName);
                Log.i(TAG, "Assigned native Chromium profile: " + profileName);

                // Sinkronisasi sesi tersimpan ke CookieManager khusus profil ini (hanya sekali per session runtime)
                if (!sInitializedProfiles.contains(profileName)) {
                    File targetSession = getSessionFile(profileName);
                    if (targetSession.exists() && targetSession.length() > 0) {
                        restoreCookiesToProfile(profile.getCookieManager(), targetSession);
                        Log.i(TAG, "Restored cookies into native profile: " + profileName);
                    }
                    sInitializedProfiles.add(profileName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to set native profile " + profileName + ": " + e.getMessage());
            }
        } else {
            applyLegacyProfileFallback(profileName, webView);
        }

        // 2. Document Start Script Injection (100% Anti-Bot & Anti-Cloudflare Stealth)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            try {
                String fakerScript = getFingerprintFakerScript(webView.getContext(), profileName, device);
                WebViewCompat.addDocumentStartJavaScript(webView, fakerScript, Collections.singleton("*"));
                Log.i(TAG, "Injected document-start stealth script for: " + profileName);
            } catch (Exception e) {
                Log.e(TAG, "Failed to inject document-start script: " + e.getMessage());
            }
        }
    }

    public static String getFingerprintFakerScript(Context context, String profileName, DeviceProfile dev) {
        StringBuilder sb = new StringBuilder();
        sb.append("window.__FUSION_PROFILE__ = '").append(profileName).append("';\n");
        sb.append("window.__FUSION_DEVICE__ = ").append(dev.toJsonString()).append(";\n");
        try {
            InputStream is = context.getAssets().open("webview_engine/fingerprint_faker.js");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            is.close();
            sb.append(baos.toString(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            Log.e(TAG, "Error reading fingerprint_faker.js: " + e.getMessage());
        }
        return sb.toString();
    }

    /**
     * Kompatibilitas untuk pemanggilan legacy applyProfile.
     */
    public static void applyProfile(String targetProfile, WebView webView, boolean isDesktop) {
        if (targetProfile == null || targetProfile.isEmpty()) {
            targetProfile = "default";
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            sCurrentActiveProfile = targetProfile;
            DeviceProfile device = getDeviceForProfile(targetProfile);
            if (webView != null) {
                WebSettings settings = webView.getSettings();
                if (isDesktop) {
                    settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
                } else {
                    settings.setUserAgentString(device.userAgent);
                }
            }
            return;
        }

        applyLegacyProfileFallback(targetProfile, webView);
    }

    private static void applyLegacyProfileFallback(String targetProfile, WebView webView) {
        boolean profileChanged = !targetProfile.equals(sCurrentActiveProfile);
        DeviceProfile device = getDeviceForProfile(targetProfile);

        if (profileChanged || targetProfile.startsWith("fresh_")) {
            Log.i(TAG, "[LEGACY FALLBACK] Switching profile from " + sCurrentActiveProfile + " -> " + targetProfile);
            if (webView != null) {
                webView.loadUrl("about:blank");
                webView.clearCache(true);
                webView.clearHistory();
                webView.evaluateJavascript("try { localStorage.clear(); sessionStorage.clear(); } catch(e){}", null);
            }
            clearAllCookiesAndStorage();

            File targetSession = getSessionFile(targetProfile);
            if (targetSession.exists() && targetSession.length() > 0) {
                restoreCookiesFromSession(targetSession);
            }
            sCurrentActiveProfile = targetProfile;
        }
    }

    public static void clearAllCookiesAndStorage() {
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
            WebStorage.getInstance().deleteAllData();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cookies: " + e.getMessage());
        }
    }

    public static void restoreCookiesFromSession(File sessionFile) {
        restoreCookiesToProfile(CookieManager.getInstance(), sessionFile);
    }

    public static void restoreCookiesToProfile(CookieManager cookieManager, File sessionFile) {
        if (cookieManager == null || sessionFile == null || !sessionFile.exists()) return;
        try {
            FileInputStream fis = new FileInputStream(sessionFile);
            byte[] bytes = new byte[(int) sessionFile.length()];
            fis.read(bytes);
            fis.close();
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);
            JSONArray cookies = root.optJSONArray("cookies");
            if (cookies != null && cookies.length() > 0) {
                cookieManager.setAcceptCookie(true);
                for (int i = 0; i < cookies.length(); i++) {
                    JSONObject c = cookies.getJSONObject(i);
                    String name = c.getString("name");
                    String value = c.getString("value");
                    String domain = c.optString("domain", "");
                    String path = c.optString("path", "/");
                    boolean secure = c.optBoolean("secure", false);
                    boolean httpOnly = c.optBoolean("httpOnly", false);

                    if (domain.startsWith(".")) {
                        domain = domain.substring(1);
                    }
                    String url = (secure ? "https://" : "http://") + domain + path;

                    StringBuilder cookieVal = new StringBuilder();
                    cookieVal.append(name).append("=").append(value).append("; Domain=").append(domain).append("; Path=").append(path);
                    if (secure) cookieVal.append("; Secure");
                    if (httpOnly) cookieVal.append("; HttpOnly");

                    cookieManager.setCookie(url, cookieVal.toString());
                }
                cookieManager.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring cookies from " + sessionFile.getPath() + ": " + e.getMessage());
        }
    }

    public static String getCurrentActiveProfile() {
        return sCurrentActiveProfile;
    }
}
