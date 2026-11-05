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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class FusionProxyHelper {
    private static final String TAG = "FusionProxyHelper";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final Executor sDirectExecutor = Runnable::run;

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

            // Cek apakah host di depan (host:port@user:pass) atau di belakang (user:pass@host:port)
            if (first.contains(":") && second.contains(":")) {
                // Tentukan mana yang host berdasarkan port angka
                String[] p1 = first.split(":", 2);
                String[] p2 = second.split(":", 2);
                boolean p1IsPort = isNumeric(p1[1]);
                boolean p2IsPort = isNumeric(p2[1]);

                if (p1IsPort && !p2IsPort) {
                    // host:port@user:pass
                    item.host = p1[0].trim();
                    try { item.port = Integer.parseInt(p1[1].trim()); } catch (Exception ignored) {}
                    item.user = p2[0].trim();
                    item.pass = p2[1].trim();
                } else {
                    // user:pass@host:port
                    item.user = p1[0].trim();
                    item.pass = p1[1].trim();
                    item.host = p2[0].trim();
                    try { item.port = Integer.parseInt(p2[1].trim()); } catch (Exception ignored) {}
                }
            } else if (second.contains(":")) {
                // user:pass@host:port atau user@host:port
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
            // host:port
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

                // Endpoint cepat tanpa HTTPS overhead untuk tes awal yang akurat
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
            String label = (country != null && !country.isEmpty()) ? " 🌐 " + country.toUpperCase() + " " : " 🌐 ON ";
            manager.mProxyBtn.setText(label);
            manager.mProxyBtn.setTextColor(0xFF89DCEB); // Cyan
            manager.mProxyBtn.setBackground(FusionUiHelper.createRounded(0x3389DCEB, 0xFF89DCEB, 4.0f));
        } else {
            manager.mProxyBtn.setText(" 🌐 OFF ");
            manager.mProxyBtn.setTextColor(0xFF6C7086); // Muted gray
            manager.mProxyBtn.setBackground(FusionUiHelper.createRounded(0x1A6C7086, 0x336C7086, 4.0f));
        }
    }

    /**
     * Dialog UI Utama Pengaturan Proxy Per-Tab
     */
    public static void showProxyDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null || manager == null) return;

        final FusionTab currentTab = FusionTabStateHelper.getActiveTab(manager);
        if (currentTab == null) {
            FusionToast.makeText(act, "Tidak ada tab aktif.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle("🌐 Pengaturan Proxy · Tab " + (manager.mActiveTab + 1));

        ScrollView scrollView = new ScrollView(act);
        LinearLayout root = new LinearLayout(act);
        root.setOrientation(1);
        root.setPadding(24, 16, 24, 16);

        // Status proxy saat ini
        TextView statusText = new TextView(act);
        if (currentTab.mIsProxyEnabled && currentTab.mProxyItem != null && currentTab.mProxyItem.isValid()) {
            statusText.setText("Status: 🟢 Aktif (" + currentTab.mProxyItem.getDisplayName() + ")");
            statusText.setTextColor(0xFFA6E3A1); // Green
        } else {
            statusText.setText("Status: ⚪ Direct (Tanpa Proxy)");
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

        // Hasil Pengujian
        final TextView testResultText = new TextView(act);
        testResultText.setTextSize(2, 11.5f);
        testResultText.setPadding(0, 8, 0, 8);
        testResultText.setVisibility(View.GONE);
        root.addView(testResultText);

        // Baris Tombol Aksi: Tes Proxy & Pilih File
        LinearLayout actionRow = new LinearLayout(act);
        actionRow.setOrientation(0);
        actionRow.setPadding(0, 10, 0, 10);

        Button testBtn = new Button(act);
        testBtn.setText("🔍 Tes Proxy");
        testBtn.setTextSize(2, 11.5f);
        testBtn.setOnClickListener(v -> {
            String pStr = inputProxy.getText().toString().trim();
            ProxyItem item = parse(pStr);
            if (item == null || !item.isValid()) {
                testResultText.setVisibility(View.VISIBLE);
                testResultText.setTextColor(0xFFF38BA8);
                testResultText.setText("❌ Format proxy tidak valid. Contoh: gw.dataimpulse.com:823:user:pass");
                return;
            }
            testResultText.setVisibility(View.VISIBLE);
            testResultText.setTextColor(0xFFF9E2AF);
            testResultText.setText("⏳ Sedang menguji koneksi proxy ke server...");

            testProxyAsync(item, (success, message, ip, country, latencyMs) -> {
                testResultText.setText(message);
                testResultText.setTextColor(success ? 0xFFA6E3A1 : 0xFFF38BA8);
            });
        });
        actionRow.addView(testBtn, new LinearLayout.LayoutParams(0, -2, 1.0f));

        Button fileBtn = new Button(act);
        fileBtn.setText("📂 File .txt");
        fileBtn.setTextSize(2, 11.5f);
        fileBtn.setOnClickListener(v -> {
            showSelectProxyFileDialog(act, manager, inputProxy, testResultText);
        });
        actionRow.addView(fileBtn, new LinearLayout.LayoutParams(0, -2, 1.0f));

        root.addView(actionRow);

        // Panduan Format
        TextView formatGuide = new TextView(act);
        formatGuide.setText("Format didukung:\n• DataImpulse: host:port:user:pass\n• URL: http://user:pass@host:port\n• SOCKS: socks5://user:pass@host:port\n• Direct: host:port");
        formatGuide.setTextColor(0xFF6C7086);
        formatGuide.setTextSize(2, 11.0f);
        formatGuide.setPadding(0, 6, 0, 12);
        root.addView(formatGuide);

        scrollView.addView(root);
        builder.setView(scrollView);

        // Tombol Dialog Utama
        builder.setPositiveButton("Terapkan ke Tab Ini", (dialog, which) -> {
            String pStr = inputProxy.getText().toString().trim();
            ProxyItem item = parse(pStr);
            if (item == null || !item.isValid()) {
                FusionToast.makeText(act, "❌ Format proxy tidak valid.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentTab.mProxyItem = item;
            currentTab.mIsProxyEnabled = true;
            applyProxyToEngine(currentTab);
            updateProxyIndicator(manager, currentTab);
            FusionToast.makeText(act, "✅ Proxy Aktif untuk Tab " + (manager.mActiveTab + 1) + " (" + item.toProxyRule() + ")", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Matikan Proxy Tab", (dialog, which) -> {
            currentTab.mIsProxyEnabled = false;
            applyProxyToEngine(currentTab);
            updateProxyIndicator(manager, currentTab);
            FusionToast.makeText(act, "⚪ Tab " + (manager.mActiveTab + 1) + " kembali ke Direct Network", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Tutup", null);

        builder.show();
    }

    /**
     * Dialog untuk memilih file .txt berisi proxy dari storage
     */
    private static void showSelectProxyFileDialog(final TermuxActivity act, final FusionWebViewManager manager, final EditText inputProxy, final TextView testResultText) {
        List<File> files = scanProxyFiles(act);
        if (files.isEmpty()) {
            // Jika tidak ada yang otomatis ketemu, cari semua .txt di /sdcard/Download
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

    /**
     * Dialog untuk memilih salah satu proxy dari baris-baris file .txt
     */
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
