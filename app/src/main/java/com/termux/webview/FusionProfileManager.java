package com.termux.webview;

import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class FusionProfileManager {
    private static final String TAG = "FusionProfileManager";
    private static String sCurrentActiveProfile = "default";

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

        public DeviceProfile(String name, String model, String brand, String userAgent, String platform, String vendor, String renderer, int cores, int memory) {
            this.name = name;
            this.model = model;
            this.brand = brand;
            this.userAgent = userAgent;
            this.platform = platform;
            this.vendor = vendor;
            this.renderer = renderer;
            this.cores = cores;
            this.memory = memory;
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
                return obj.toString();
            } catch (Exception e) {
                return "{}";
            }
        }
    }

    private static final DeviceProfile[] DEVICE_POOL = new DeviceProfile[] {
        new DeviceProfile(
            "Samsung Galaxy S24 Ultra", "SM-S928B", "Samsung",
            "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 12
        ),
        new DeviceProfile(
            "Google Pixel 8 Pro", "Pixel 8 Pro", "Google",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux aarch64", "ARM", "Mali-G715 Immortalis MC10", 8, 12
        ),
        new DeviceProfile(
            "Xiaomi 14 Pro", "23116PN5BC", "Xiaomi",
            "Mozilla/5.0 (Linux; Android 14; 23116PN5BC) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 12
        ),
        new DeviceProfile(
            "OnePlus 12", "CPH2581", "OnePlus",
            "Mozilla/5.0 (Linux; Android 14; CPH2581) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 750", 8, 16
        ),
        new DeviceProfile(
            "Infinix GT 20 Pro", "X6871", "Infinix",
            "Mozilla/5.0 (Linux; Android 14; X6871) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "ARM", "Mali-G610 MC6", 8, 8
        ),
        new DeviceProfile(
            "POCO F6 Pro", "23113RKC6G", "POCO",
            "Mozilla/5.0 (Linux; Android 14; 23113RKC6G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 740", 8, 12
        ),
        new DeviceProfile(
            "Sony Xperia 1 V", "XQ-DQ72", "Sony",
            "Mozilla/5.0 (Linux; Android 14; XQ-DQ72) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "Qualcomm", "Adreno (TM) 740", 8, 12
        ),
        new DeviceProfile(
            "Vivo X100 Pro", "V2324A", "vivo",
            "Mozilla/5.0 (Linux; Android 14; V2324A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36",
            "Linux armv8l", "ARM", "Mali-G720 Immortalis MC12", 8, 16
        )
    };

    public static DeviceProfile getDeviceForProfile(String profile) {
        if (profile == null || profile.isEmpty() || profile.equals("default")) {
            return DEVICE_POOL[0];
        }
        int hash = Math.abs(profile.hashCode());
        int index = hash % DEVICE_POOL.length;
        return DEVICE_POOL[index];
    }

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

    /**
     * Terapkan profil, swap cookie terisolasi, dan set User-Agent model HP.
     */
    public static void applyProfile(String targetProfile, WebView webView, boolean isDesktop) {
        if (targetProfile == null || targetProfile.isEmpty()) {
            targetProfile = "default";
        }

        boolean profileChanged = !targetProfile.equals(sCurrentActiveProfile);
        DeviceProfile device = getDeviceForProfile(targetProfile);

        if (webView != null) {
            WebSettings settings = webView.getSettings();
            if (isDesktop) {
                settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
            } else {
                settings.setUserAgentString(device.userAgent);
            }
        }

        if (profileChanged || targetProfile.startsWith("fresh_")) {
            Log.i(TAG, "Switching profile from " + sCurrentActiveProfile + " -> " + targetProfile + " (Device: " + device.name + ")");
            
            if (webView != null) {
                // Segera kosongkan layar ke about:blank agar UI akun lama tidak muncul sekilas
                webView.loadUrl("about:blank");
                webView.clearCache(true);
                webView.clearHistory();
                webView.evaluateJavascript("try { localStorage.clear(); sessionStorage.clear(); } catch(e){}", null);
            }

            // Bersihkan cookies & storage lama agar 100% FRESH
            clearAllCookiesAndStorage();

            // Jika target profil memiliki berkas sesi tersimpan, restore cookies
            File targetSession = getSessionFile(targetProfile);
            if (targetSession.exists() && targetSession.length() > 0) {
                restoreCookiesFromSession(targetSession);
                Log.i(TAG, "Restored cookies for profile: " + targetProfile);
            } else {
                Log.i(TAG, "Profile " + targetProfile + " has NO saved session. Starting 100% fresh!");
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
        try {
            FileInputStream fis = new FileInputStream(sessionFile);
            byte[] bytes = new byte[(int) sessionFile.length()];
            fis.read(bytes);
            fis.close();
            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);
            JSONArray cookies = root.optJSONArray("cookies");
            if (cookies != null && cookies.length() > 0) {
                CookieManager cookieManager = CookieManager.getInstance();
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
