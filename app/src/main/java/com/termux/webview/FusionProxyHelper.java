package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import com.termux.app.FusionToast;
import com.termux.app.TermuxActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🌐 FUSION PROXY HELPER (Sticky Proxy per-Tab & Multi-Tab Concurrency Manager)
 * Mengadopsi arsitektur dari agenthansa.py & fluxa.py (Tuan Eso Edition):
 * 1. Sticky Proxy: Setiap Profil & Tab memiliki proxy lengket unik di ~/.fusion/proxy_sticky.json.
 * 2. Auto-Pool Assignment: Tab baru otomatis menyedot 1 proxy unik dari proxy.txt / dataimpulse.txt.
 * 3. Rotasi Sekali Klik: Tombol [🔄 Rotasi Proxy] untuk membuang proxy ampas dan menarik proxy baru dari antrean.
 * 4. Multi-Format: Mendukung penuh host:port:user:pass DataImpulse, user:pass@host:port, SOCKS5, & HTTP.
 * 5. Multi-Tab Isolation: Sinkronisasi instan saat berpindah tab & auto HTTP Auth tanpa popup.
 */
public class FusionProxyHelper {
    private static final String TAG = "FusionProxyHelper";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final Executor sDirectExecutor = Runnable::run;

    private static final String STICKY_FILE_PATH = "/root/.fusion/proxy_sticky.json";
    private static final String FALLBACK_STICKY_FILE_PATH = "/data/data/com.termux/files/home/.fusion/proxy_sticky.json";

    public static class ProxyItem {
        public String raw = "";
        public String host = "";
        public int port = 0;
        public String user = "";
        public String pass = "";
        public String scheme = "http"; // http or socks5
        public String country = "";
        public String ip = "";
        public long latency = -1;
        public boolean isTested = false;
        public boolean isSuccess = false;

        public boolean hasAuth() {
            return user != null && !user.isEmpty() && pass != null && !pass.isEmpty();
        }

        public boolean isValid() {
            return host != null && !host.isEmpty() && port > 0 && port <= 65535;
        }

        public String toProxyRule() {
            if ("socks5".equalsIgnoreCase(scheme) || "socks".equalsIgnoreCase(scheme)) {
                return "socks://" + host + ":" + port;
            }
            return host + ":" + port;
        }

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder();
            if (country != null && !country.isEmpty()) {
                sb.append("[").append(country.toUpperCase()).append("] ");
            }
            sb.append(host).append(":").append(port);
            if (hasAuth()) {
                sb.append(" (").append(user).append(")");
            }
            return sb.toString();
        }
    }

    public interface TestCallback {
        void onResult(boolean success, String message, String ip, String country, long latencyMs);
    }

    /**
     * Parser cerdas yang mendukung seluruh varian format DataImpulse & proxy standar:
     * 1. host:port:user:pass (Format resmi DataImpulse)
     * 2. host:port@user:pass
     * 3. user:pass@host:port
     * 4. http://user:pass@host:port
     * 5. socks5://user:pass@host:port
     * 6. host:port (Direct tanpa auth)
     */
    public static ProxyItem parse(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty() || s.startsWith("#") || s.startsWith("//")) return null;

        ProxyItem item = new ProxyItem();
        item.raw = s;

        // Deteksi scheme (http vs socks5)
        if (s.startsWith("socks5://") || s.startsWith("socks://")) {
            item.scheme = "socks5";
            s = s.substring(s.indexOf("://") + 3);
        } else if (s.startsWith("http://") || s.startsWith("https://")) {
            item.scheme = "http";
            s = s.substring(s.indexOf("://") + 3);
        }

        // Format 1: user:pass@host:port
        if (s.contains("@")) {
            String[] parts = s.split("@", 2);
            String first = parts[0];
            String second = parts[1];

            if (first.contains(":") && second.contains(":")) {
                String[] p1 = first.split(":", 2);
                String[] p2 = second.split(":", 2);
                boolean p1IsPort = isNumeric(p1[1]);
                boolean p2IsPort = isNumeric(p2[1]);

                if (p1IsPort && !p2IsPort) {
                    item.host = p1[0].trim();
                    try { item.port = Integer.parseInt(p1[1].trim()); } catch (Exception ignored) {}
                    item.user = p2[0].trim();
                    item.pass = p2[1].trim();
                } else {
                    item.user = p1[0].trim();
                    item.pass = p1[1].trim();
                    item.host = p2[0].trim();
                    try { item.port = Integer.parseInt(p2[1].trim()); } catch (Exception ignored) {}
                }
            } else if (second.contains(":")) {
                if (first.contains(":")) {
                    String[] auth = first.split(":", 2);
                    item.user = auth[0].trim();
                    item.pass = auth[1].trim();
                } else {
                    item.user = first.trim();
                }
                String[] hp = second.split(":", 2);
                item.host = hp[0].trim();
                try { item.port = Integer.parseInt(hp[1].trim()); } catch (Exception ignored) {}
            }
            return item.isValid() ? item : null;
        }

        // Format 2: host:port:user:pass (Standar DataImpulse & Webshare)
        String[] colons = s.split(":");
        if (colons.length == 4) {
            item.host = colons[0].trim();
            try { item.port = Integer.parseInt(colons[1].trim()); } catch (Exception ignored) {}
            item.user = colons[2].trim();
            item.pass = colons[3].trim();
            return item.isValid() ? item : null;
        } else if (colons.length == 2) {
            item.host = colons[0].trim();
            try { item.port = Integer.parseInt(colons[1].trim()); } catch (Exception ignored) {}
            return item.isValid() ? item : null;
        }

        return null;
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /* =========================================================================
     * STICKY PROXY & AUTO-ROTATION STORAGE (SEPERTI FLUXA.PY & AGENTHANSA.PY)
     * ========================================================================= */

    private static File getStickyFile() {
        File f1 = new File(STICKY_FILE_PATH);
        if (f1.getParentFile() != null && !f1.getParentFile().exists()) {
            try { f1.getParentFile().mkdirs(); } catch (Exception ignored) {}
        }
        if (f1.getParentFile() != null && f1.getParentFile().canWrite()) {
            return f1;
        }
        File f2 = new File(FALLBACK_STICKY_FILE_PATH);
        if (f2.getParentFile() != null && !f2.getParentFile().exists()) {
            try { f2.getParentFile().mkdirs(); } catch (Exception ignored) {}
        }
        return f2;
    }

    public static synchronized void saveStickyProxy(String profileOrTab, ProxyItem item) {
        if (profileOrTab == null || profileOrTab.isEmpty()) return;
        try {
            File f = getStickyFile();
            JSONObject root = new JSONObject();
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    if (sb.length() > 0) root = new JSONObject(sb.toString());
                }
            }
            if (item != null && item.isValid()) {
                JSONObject pObj = new JSONObject();
                pObj.put("raw", item.raw);
                pObj.put("host", item.host);
                pObj.put("port", item.port);
                pObj.put("user", item.user);
                pObj.put("pass", item.pass);
                pObj.put("scheme", item.scheme);
                pObj.put("country", item.country);
                pObj.put("ip", item.ip);
                root.put(profileOrTab, pObj);
            } else {
                root.remove(profileOrTab);
            }
            try (FileOutputStream fos = new FileOutputStream(f, false)) {
                fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal saveStickyProxy: " + e.getMessage());
        }
    }

    public static synchronized ProxyItem loadStickyProxy(String profileOrTab) {
        if (profileOrTab == null || profileOrTab.isEmpty()) return null;
        try {
            File f = getStickyFile();
            if (!f.exists()) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                if (sb.length() == 0) return null;
                JSONObject root = new JSONObject(sb.toString());
                if (root.has(profileOrTab)) {
                    JSONObject pObj = root.getJSONObject(profileOrTab);
                    String raw = pObj.optString("raw", "");
                    ProxyItem item = parse(raw);
                    if (item != null) {
                        item.country = pObj.optString("country", "");
                        item.ip = pObj.optString("ip", "");
                        return item;
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal loadStickyProxy: " + e.getMessage());
        }
        return null;
    }

    public static File getPrimaryProxyFile() {
        String[] candidates = new String[] {
            "/sdcard/Download/proxy.txt",
            "/sdcard/Download/dataimpulse.txt",
            "/root/proxy.txt",
            "/project/proxy.txt",
            "/data/data/com.termux/files/home/proxy.txt",
            "/sdcard/proxy.txt"
        };
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists() && f.isFile() && f.length() > 0) return f;
        }
        return null;
    }

    /**
     * Mengambil 1 proxy berikutnya dari file antrean (Multi-process safe locking persis fluxa.py)
     */
    public static synchronized ProxyItem popNextProxyFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) return null;

        File lockFile = new File(file.getAbsolutePath() + ".lock");
        int waitCount = 0;
        while (lockFile.exists() && waitCount++ < 15) {
            try { Thread.sleep(100); } catch (Exception ignored) {}
        }
        try { lockFile.createNewFile(); } catch (Exception ignored) {}

        ProxyItem popped = null;
        try {
            List<String> remainingLines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
                    if (popped == null) {
                        ProxyItem parsed = parse(line);
                        if (parsed != null && parsed.isValid()) {
                            popped = parsed;
                            continue; // Ambil yang pertama (pop)
                        }
                    }
                    remainingLines.add(line);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                for (String rem : remainingLines) {
                    fos.write((rem + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal popNextProxyFromFile: " + e.getMessage());
        } finally {
            try { lockFile.delete(); } catch (Exception ignored) {}
        }

        return popped;
    }

    /**
     * Menginisialisasi sticky proxy saat tab baru dibuat
     */
    public static void initTabStickyProxy(FusionTab tab) {
        if (tab == null) return;
        String key = (tab.mProfile != null && !tab.mProfile.isEmpty() && !"default".equals(tab.mProfile))
                ? tab.mProfile
                : "tab_" + tab.mId;

        ProxyItem sticky = loadStickyProxy(key);
        if (sticky != null && sticky.isValid()) {
            tab.mProxyItem = sticky;
            tab.mIsProxyEnabled = true;
            return;
        }

        // Sedot otomatis 1 proxy dari pool proxy.txt (persis assign_proxy di fluxa.py)
        File poolFile = getPrimaryProxyFile();
        if (poolFile != null) {
            ProxyItem next = popNextProxyFromFile(poolFile);
            if (next != null && next.isValid()) {
                tab.mProxyItem = next;
                tab.mIsProxyEnabled = true;
                saveStickyProxy(key, next);
                Log.i(TAG, "Auto-assigned sticky proxy untuk [" + key + "] dari " + poolFile.getName() + ": " + next.toProxyRule());
            }
        }
    }

    /**
     * Rotasi proxy untuk tab: buang proxy ampas, ambil yang baru dari proxy.txt, tes, dan pasang langsung
     */
    public static void rotateProxyForTab(final FusionTab tab, final FusionWebViewManager manager, final TestCallback cb) {
        if (tab == null) {
            if (cb != null) cb.onResult(false, "Tab tidak valid", "", "", -1);
            return;
        }

        new Thread(() -> {
            File poolFile = getPrimaryProxyFile();
            if (poolFile == null) {
                sMainHandler.post(() -> {
                    if (cb != null) cb.onResult(false, "File proxy.txt tidak ditemukan", "", "", -1);
                });
                return;
            }

            ProxyItem newProxy = popNextProxyFromFile(poolFile);
            if (newProxy == null || !newProxy.isValid()) {
                sMainHandler.post(() -> {
                    if (cb != null) cb.onResult(false, "Antrean proxy di " + poolFile.getName() + " sudah habis!", "", "", -1);
                });
                return;
            }

            String key = (tab.mProfile != null && !tab.mProfile.isEmpty() && !"default".equals(tab.mProfile))
                    ? tab.mProfile
                    : "tab_" + tab.mId;

            tab.mProxyItem = newProxy;
            tab.mIsProxyEnabled = true;
            tab.mLastProxyRotatedTime = System.currentTimeMillis();
            saveStickyProxy(key, newProxy);

            sMainHandler.post(() -> {
                if (manager != null && FusionTabStateHelper.getActiveTab(manager) == tab) {
                    applyProxyToEngine(tab);
                    updateProxyIndicator(manager, tab);
                }
            });

            // Uji proxy baru
            testProxyAsync(newProxy, (success, message, ip, country, latencyMs) -> {
                if (success) {
                    saveStickyProxy(key, newProxy);
                    sMainHandler.post(() -> {
                        if (manager != null) updateProxyIndicator(manager, tab);
                    });
                }
                if (cb != null) cb.onResult(success, message, ip, country, latencyMs);
            });
        }).start();
    }

    private static ScheduledExecutorService sRotateScheduler = null;
    private static final Object sSchedulerLock = new Object();

    public static synchronized void startAutoRotateScheduler(final FusionWebViewManager manager) {
        synchronized (sSchedulerLock) {
            if (sRotateScheduler != null && !sRotateScheduler.isShutdown()) {
                return;
            }
            sRotateScheduler = Executors.newSingleThreadScheduledExecutor();
            sRotateScheduler.scheduleWithFixedDelay(() -> {
                try {
                    checkAndRotateTabs(manager);
                } catch (Throwable t) {
                    Log.e(TAG, "Error in auto-rotate scheduler: " + t.getMessage());
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    private static void checkAndRotateTabs(final FusionWebViewManager manager) {
        if (manager == null || manager.mTabList == null) return;
        List<?> tabs = new ArrayList<>(manager.mTabList);
        if (tabs.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (Object obj : tabs) {
            if (!(obj instanceof FusionTab)) continue;
            final FusionTab tab = (FusionTab) obj;
            if (tab.mIsProxyEnabled && tab.mAutoRotateIntervalSec > 0) {
                long elapsedSec = (now - tab.mLastProxyRotatedTime) / 1000L;
                if (elapsedSec >= tab.mAutoRotateIntervalSec) {
                    tab.mLastProxyRotatedTime = now;
                    Log.i(TAG, "Auto-rotating proxy for tab " + tab.mId + " (interval: " + tab.mAutoRotateIntervalSec + "s)");
                    rotateProxyForTab(tab, manager, (success, message, ip, country, latencyMs) -> {
                        sMainHandler.post(() -> {
                            TermuxActivity act = manager.mActivity;
                            if (act != null && success) {
                                String cStr = (country != null && !country.isEmpty()) ? country.toUpperCase() : "OK";
                                FusionToast.makeText(act, "🔄 Tab " + (tab.mId + 1) + " auto-rotate: " + cStr + " (" + ip + ")", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }
        }
    }

    /**
     * Konfigurasi proxy untuk tab secara spesifik atau auto-pool
     */
    public static void configureTabProxy(FusionTab tab, String proxyStr, int intervalSec, FusionWebViewManager manager) {
        if (tab == null) return;
        if (proxyStr == null || proxyStr.trim().isEmpty() || "direct".equalsIgnoreCase(proxyStr) || "off".equalsIgnoreCase(proxyStr)) {
            tab.mIsProxyEnabled = false;
            tab.mAutoRotateIntervalSec = 0;
            return;
        }

        String key = (tab.mProfile != null && !tab.mProfile.isEmpty() && !"default".equals(tab.mProfile))
                ? tab.mProfile
                : "tab_" + tab.mId;

        ProxyItem item = null;
        if ("auto".equalsIgnoreCase(proxyStr) || "pool".equalsIgnoreCase(proxyStr) || "1".equals(proxyStr) || "true".equalsIgnoreCase(proxyStr)) {
            ProxyItem sticky = loadStickyProxy(key);
            if (sticky != null && sticky.isValid()) {
                item = sticky;
            } else {
                File poolFile = getPrimaryProxyFile();
                if (poolFile != null) {
                    item = popNextProxyFromFile(poolFile);
                }
            }
        } else {
            item = parse(proxyStr);
        }

        if (item != null && item.isValid()) {
            tab.mProxyItem = item;
            tab.mIsProxyEnabled = true;
            tab.mLastProxyRotatedTime = System.currentTimeMillis();
            if (intervalSec > 0) {
                tab.mAutoRotateIntervalSec = intervalSec;
                startAutoRotateScheduler(manager);
            }
            saveStickyProxy(key, item);
            applyProxyToEngine(tab);
            updateProxyIndicator(manager, tab);

            testProxyAsync(item, (success, message, ip, country, latencyMs) -> {
                if (success) {
                    saveStickyProxy(key, item);
                    sMainHandler.post(() -> {
                        if (manager != null) updateProxyIndicator(manager, tab);
                    });
                }
            });
        }
    }

    /**
     * Membaca file .txt dan mengekstrak seluruh baris proxy
     */
    public static List<ProxyItem> loadProxiesFromFile(File file) {
        List<ProxyItem> list = new ArrayList<>();
        if (file == null || !file.exists() || !file.isFile()) return list;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                ProxyItem item = parse(line);
                if (item != null && item.isValid()) {
                    list.add(item);
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal membaca file proxy " + file.getAbsolutePath() + ": " + e.getMessage());
        }
        return list;
    }

    /**
     * Memindai file-file .txt di folder unduhan & workspace
     */
    public static List<File> scanProxyFiles(Context ctx) {
        List<File> result = new ArrayList<>();
        String[] candidateDirs = new String[] {
            "/sdcard/Download",
            "/sdcard",
            "/root",
            "/project",
            "/data/data/com.termux/files/home"
        };

        for (String dirPath : candidateDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().toLowerCase().endsWith(".txt")) {
                            if (f.getName().toLowerCase().contains("proxy") ||
                                f.getName().toLowerCase().contains("dataimpulse") ||
                                f.getName().toLowerCase().contains("ip")) {
                                result.add(f);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Uji koneksi proxy ke endpoint live secara asinkron
     */
    public static void testProxyAsync(final ProxyItem item, final TestCallback cb) {
        if (item == null || !item.isValid()) {
            if (cb != null) cb.onResult(false, "Format proxy tidak valid", "", "", -1);
            return;
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            try {
                Proxy.Type pType = "socks5".equalsIgnoreCase(item.scheme) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
                Proxy proxy = new Proxy(pType, new InetSocketAddress(item.host, item.port));

                URL testUrl = new URL("http://ip-api.com/json/?fields=status,country,countryCode,city,query,org");
                HttpURLConnection conn = (HttpURLConnection) testUrl.openConnection(proxy);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (item.hasAuth() && pType == Proxy.Type.HTTP) {
                    String auth = item.user + ":" + item.pass;
                    String encoded = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                    conn.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
                }
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0");

                int code = conn.getResponseCode();
                long latency = System.currentTimeMillis() - startTime;

                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    String ip = obj.optString("query", item.host);
                    String country = obj.optString("countryCode", obj.optString("country", "US"));
                    String city = obj.optString("city", "");
                    String isp = obj.optString("org", "");

                    item.ip = ip;
                    item.country = country;
                    item.latency = latency;
                    item.isTested = true;
                    item.isSuccess = true;

                    String resMsg = "✅ Sukses! IP: " + ip + " [" + country.toUpperCase() + (city.isEmpty() ? "" : " · " + city) + "] · " + latency + "ms";
                    sMainHandler.post(() -> {
                        if (cb != null) cb.onResult(true, resMsg, ip, country, latency);
                    });
                } else {
                    String err = "❌ HTTP " + code + " (Auth Gagal / Blocked)";
                    sMainHandler.post(() -> {
                        if (cb != null) cb.onResult(false, err, "", "", latency);
                    });
                }
                conn.disconnect();
            } catch (Throwable t) {
                long latency = System.currentTimeMillis() - startTime;
                String errMsg = "❌ Gagal: " + (t.getMessage() != null ? t.getMessage() : "Timeout");
                sMainHandler.post(() -> {
                    if (cb != null) cb.onResult(false, errMsg, "", "", latency);
                });
            }
        }).start();
    }

    /**
     * Menerapkan Proxy tab aktif ke engine Android WebView secara real-time
     */
    public static void applyProxyToEngine(FusionTab tab) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            Log.w(TAG, "WebViewFeature.PROXY_OVERRIDE tidak didukung pada versi Android ini.");
            return;
        }

        try {
            if (tab != null && tab.mIsProxyEnabled && tab.mProxyItem != null && tab.mProxyItem.isValid()) {
                ProxyConfig config = new ProxyConfig.Builder()
                        .addProxyRule(tab.mProxyItem.toProxyRule())
                        .build();

                ProxyController.getInstance().setProxyOverride(config, sDirectExecutor, () -> {
                    Log.i(TAG, "ProxyOverride aktif untuk tab " + tab.mId + ": " + tab.mProxyItem.toProxyRule());
                });
            } else {
                ProxyController.getInstance().clearProxyOverride(sDirectExecutor, () -> {
                    Log.i(TAG, "ProxyOverride dinonaktifkan (Direct)");
                });
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal mengaplikasikan proxy ke engine: " + e.getMessage());
        }
    }

    /**
     * Memperbarui label tombol indikator proxy di floating pill kontrol WebView
     */
    public static void updateProxyIndicator(FusionWebViewManager manager, FusionTab tab) {
        if (manager == null || manager.mProxyBtn == null) return;

        if (tab != null && tab.mIsProxyEnabled && tab.mProxyItem != null && tab.mProxyItem.isValid()) {
            String country = tab.mProxyItem.country;
            String cCode = (country != null && !country.trim().isEmpty()) ? country.trim().toUpperCase() : "ON";
            manager.mProxyBtn.setText(" PROXY : " + cCode + " ");
            manager.mProxyBtn.setTextColor(0xFF89DCEB); // Cyan
            manager.mProxyBtn.setBackground(FusionUiHelper.createRounded(0x3389DCEB, 0xFF89DCEB, 4.0f));
        } else {
            manager.mProxyBtn.setText(" PROXY : OFF ");
            manager.mProxyBtn.setTextColor(0xFF6C7086); // Muted gray
            manager.mProxyBtn.setBackground(FusionUiHelper.createRounded(0x1A6C7086, 0x336C7086, 4.0f));
        }
    }

    /**
     * Dialog UI Utama Pengaturan Proxy Per-Tab & Rotasi Otomatis
     */
    public static void showProxyDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null || manager == null) return;

        final FusionTab currentTab = FusionTabStateHelper.getActiveTab(manager);
        if (currentTab == null) {
            FusionToast.makeText(act, "Tidak ada tab aktif.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        String profLabel = (currentTab.mProfile != null && !currentTab.mProfile.isEmpty()) ? " (" + currentTab.mProfile + ")" : "";
        builder.setTitle("Proxy · Tab " + (manager.mActiveTab + 1) + profLabel);

        ScrollView scrollView = new ScrollView(act);
        LinearLayout root = new LinearLayout(act);
        root.setOrientation(1);
        root.setPadding(24, 16, 24, 16);

        // Status proxy saat ini
        TextView statusText = new TextView(act);
        if (currentTab.mIsProxyEnabled && currentTab.mProxyItem != null && currentTab.mProxyItem.isValid()) {
            statusText.setText("Status: Aktif (" + currentTab.mProxyItem.getDisplayName() + ")");
            statusText.setTextColor(0xFFA6E3A1); // Green
        } else {
            statusText.setText("Status: Nonaktif (Direct Network)");
            statusText.setTextColor(0xFFBAC2DE);
        }
        statusText.setTextSize(2, 13.0f);
        statusText.setPadding(0, 0, 0, 12);
        root.addView(statusText);

        // Input Field
        TextView labelInput = new TextView(act);
        labelInput.setText("Input Proxy (DataImpulse / HTTP / SOCKS5):");
        labelInput.setTextColor(0xFFCDD6F4);
        labelInput.setTextSize(2, 12.0f);
        root.addView(labelInput);

        final EditText inputProxy = new EditText(act);
        inputProxy.setHint("host:port:user:pass atau user:pass@host:port");
        inputProxy.setTextSize(2, 12.5f);
        inputProxy.setTextColor(0xFFCDD6F4);
        inputProxy.setHintTextColor(0xFF6C7086);
        inputProxy.setBackground(FusionUiHelper.createRounded(0xFF181825, 0xFF313244, 4.0f));
        inputProxy.setPadding(14, 12, 14, 12);
        if (currentTab.mProxyItem != null && currentTab.mProxyItem.raw != null) {
            inputProxy.setText(currentTab.mProxyItem.raw);
        }
        root.addView(inputProxy);

        // Baris Interval Rotasi Otomatis
        LinearLayout intervalRow = new LinearLayout(act);
        intervalRow.setOrientation(0);
        intervalRow.setPadding(0, 10, 0, 4);
        intervalRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView labelInterval = new TextView(act);
        labelInterval.setText("Interval Rotasi (Menit, 0 = Nonaktif):");
        labelInterval.setTextColor(0xFFCDD6F4);
        labelInterval.setTextSize(2, 12.0f);
        intervalRow.addView(labelInterval, new LinearLayout.LayoutParams(0, -2, 1.0f));

        final EditText inputInterval = new EditText(act);
        int currentMins = (currentTab.mAutoRotateIntervalSec > 0) ? (currentTab.mAutoRotateIntervalSec / 60) : 0;
        inputInterval.setText(String.valueOf(currentMins));
        inputInterval.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputInterval.setTextSize(2, 12.5f);
        inputInterval.setTextColor(0xFFCDD6F4);
        inputInterval.setGravity(android.view.Gravity.CENTER);
        inputInterval.setBackground(FusionUiHelper.createRounded(0xFF181825, 0xFF313244, 4.0f));
        inputInterval.setPadding(14, 8, 14, 8);
        intervalRow.addView(inputInterval, new LinearLayout.LayoutParams(120, -2));
        root.addView(intervalRow);

        // Hasil Pengujian
        final TextView testResultText = new TextView(act);
        testResultText.setTextSize(2, 11.5f);
        testResultText.setPadding(0, 8, 0, 8);
        testResultText.setVisibility(View.GONE);
        root.addView(testResultText);

        // Baris Tombol Aksi 1: Tes Proxy & Pilih File
        LinearLayout actionRow = new LinearLayout(act);
        actionRow.setOrientation(0);
        actionRow.setPadding(0, 10, 0, 6);

        Button testBtn = new Button(act);
        testBtn.setText("Tes Proxy");
        testBtn.setTextSize(2, 11.5f);
        testBtn.setOnClickListener(v -> {
            String pStr = inputProxy.getText().toString().trim();
            ProxyItem item = parse(pStr);
            if (item == null || !item.isValid()) {
                testResultText.setVisibility(View.VISIBLE);
                testResultText.setTextColor(0xFFF38BA8);
                testResultText.setText("Format proxy tidak valid. Contoh: gw.dataimpulse.com:823:user:pass");
                return;
            }
            testResultText.setVisibility(View.VISIBLE);
            testResultText.setTextColor(0xFFF9E2AF);
            testResultText.setText("Sedang menguji koneksi proxy ke server...");

            testProxyAsync(item, (success, message, ip, country, latencyMs) -> {
                testResultText.setText(message);
                testResultText.setTextColor(success ? 0xFFA6E3A1 : 0xFFF38BA8);
            });
        });
        actionRow.addView(testBtn, new LinearLayout.LayoutParams(0, -2, 1.0f));

        Button fileBtn = new Button(act);
        fileBtn.setText("Pilih File .txt");
        fileBtn.setTextSize(2, 11.5f);
        fileBtn.setOnClickListener(v -> {
            showSelectProxyFileDialog(act, manager, inputProxy, testResultText);
        });
        actionRow.addView(fileBtn, new LinearLayout.LayoutParams(0, -2, 1.0f));

        root.addView(actionRow);

        // Baris Tombol Aksi 2: Rotasi Otomatis (seperti fluxa.py)
        LinearLayout rotateRow = new LinearLayout(act);
        rotateRow.setOrientation(0);
        rotateRow.setPadding(0, 0, 0, 10);

        Button rotateBtn = new Button(act);
        rotateBtn.setText("Rotasi Proxy (Tarik Baru dari proxy.txt)");
        rotateBtn.setTextSize(2, 11.5f);
        rotateBtn.setOnClickListener(v -> {
            testResultText.setVisibility(View.VISIBLE);
            testResultText.setTextColor(0xFFF9E2AF);
            testResultText.setText("Mengambil proxy baru & menguji on-chain...");

            rotateProxyForTab(currentTab, manager, (success, message, ip, country, latencyMs) -> {
                testResultText.setText(message);
                testResultText.setTextColor(success ? 0xFFA6E3A1 : 0xFFF38BA8);
                if (currentTab.mProxyItem != null) {
                    inputProxy.setText(currentTab.mProxyItem.raw);
                }
            });
        });
        rotateRow.addView(rotateBtn, new LinearLayout.LayoutParams(-1, -2));
        root.addView(rotateRow);

        // Panduan Format
        TextView formatGuide = new TextView(act);
        formatGuide.setText("Format didukung:\n• DataImpulse: host:port:user:pass\n• URL: http://user:pass@host:port\n• SOCKS: socks5://user:pass@host:port\n• Sticky & Rotasi: Otomatis membaca antrean proxy.txt");
        formatGuide.setTextColor(0xFF6C7086);
        formatGuide.setTextSize(2, 11.0f);
        formatGuide.setPadding(0, 4, 0, 12);
        root.addView(formatGuide);

        scrollView.addView(root);
        builder.setView(scrollView);

        // Tombol Dialog Utama
        builder.setPositiveButton("Terapkan ke Tab Ini", (dialog, which) -> {
            String pStr = inputProxy.getText().toString().trim();
            ProxyItem item = parse(pStr);
            if (item == null || !item.isValid()) {
                FusionToast.makeText(act, "Format proxy tidak valid.", Toast.LENGTH_SHORT).show();
                return;
            }

            int mins = 0;
            try {
                mins = Integer.parseInt(inputInterval.getText().toString().trim());
            } catch (Exception ignored) {}
            if (mins < 0) mins = 0;

            String key = (currentTab.mProfile != null && !currentTab.mProfile.isEmpty() && !"default".equals(currentTab.mProfile))
                    ? currentTab.mProfile
                    : "tab_" + currentTab.mId;

            currentTab.mProxyItem = item;
            currentTab.mIsProxyEnabled = true;
            currentTab.mAutoRotateIntervalSec = mins * 60;
            currentTab.mLastProxyRotatedTime = System.currentTimeMillis();
            if (mins > 0) {
                startAutoRotateScheduler(manager);
            }
            saveStickyProxy(key, item);
            applyProxyToEngine(currentTab);
            updateProxyIndicator(manager, currentTab);

            String intMsg = mins > 0 ? " (Auto-rotate tiap " + mins + "m)" : "";
            FusionToast.makeText(act, "Proxy Aktif untuk Tab " + (manager.mActiveTab + 1) + intMsg, Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Matikan Proxy Tab", (dialog, which) -> {
            String key = (currentTab.mProfile != null && !currentTab.mProfile.isEmpty() && !"default".equals(currentTab.mProfile))
                    ? currentTab.mProfile
                    : "tab_" + currentTab.mId;

            currentTab.mIsProxyEnabled = false;
            currentTab.mAutoRotateIntervalSec = 0;
            saveStickyProxy(key, null);
            applyProxyToEngine(currentTab);
            updateProxyIndicator(manager, currentTab);
            FusionToast.makeText(act, "Tab " + (manager.mActiveTab + 1) + " kembali ke Direct Network (Proxy OFF)", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Tutup", null);

        builder.show();
    }

    private static void showSelectProxyFileDialog(final TermuxActivity act, final FusionWebViewManager manager, final EditText inputProxy, final TextView testResultText) {
        List<File> files = scanProxyFiles(act);
        if (files.isEmpty()) {
            File dl = new File("/sdcard/Download");
            if (dl.exists() && dl.isDirectory()) {
                File[] list = dl.listFiles();
                if (list != null) {
                    for (File f : list) {
                        if (f.isFile() && f.getName().toLowerCase().endsWith(".txt")) {
                            files.add(f);
                        }
                    }
                }
            }
        }

        if (files.isEmpty()) {
            FusionToast.makeText(act, "Tidak ditemukan file .txt di /sdcard/Download", Toast.LENGTH_LONG).show();
            return;
        }

        String[] names = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            names[i] = files.get(i).getName() + " (" + (files.get(i).length() / 1024) + " KB)";
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("📂 Pilih File Proxy (.txt)");
        b.setItems(names, (dialog, which) -> {
            if (which >= 0 && which < files.size()) {
                File selected = files.get(which);
                List<ProxyItem> proxies = loadProxiesFromFile(selected);
                if (proxies.isEmpty()) {
                    FusionToast.makeText(act, "File tidak berisi format proxy yang valid.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProxyListSelectorDialog(act, manager, proxies, inputProxy, testResultText);
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }

    private static void showProxyListSelectorDialog(final TermuxActivity act, final FusionWebViewManager manager, final List<ProxyItem> list, final EditText inputProxy, final TextView testResultText) {
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            items[i] = (i + 1) + ". " + list.get(i).getDisplayName();
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("📋 Pilih Proxy (" + list.size() + " Ditemukan)");
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < list.size()) {
                ProxyItem chosen = list.get(which);
                inputProxy.setText(chosen.raw);
                if (testResultText != null) {
                    testResultText.setVisibility(View.VISIBLE);
                    testResultText.setTextColor(0xFFF9E2AF);
                    testResultText.setText("⏳ Menguji otomatis " + chosen.host + ":" + chosen.port + "...");
                    testProxyAsync(chosen, (success, message, ip, country, latencyMs) -> {
                        testResultText.setText(message);
                        testResultText.setTextColor(success ? 0xFFA6E3A1 : 0xFFF38BA8);
                    });
                }
            }
        });
        b.setNegativeButton("Kembali", null);
        b.show();
    }
}
