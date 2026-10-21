package com.termux.webview;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper pengelola background service 9Router, multi-account rotation,
 * dan deteksi dinamis lingkungan chroot/container di /data/local serta Native Termux.
 */
public class Fusion9RouterHelper {

    public static final int PORT_API = 20128;
    public static final int PORT_MITM = 443;
    public static final String LOCAL_BASE_URL = "http://127.0.0.1:20128/v1";
    public static final String DASHBOARD_URL = "http://127.0.0.1:20128";

    public enum RunnerType {
        NETHUNTER,
        DROIDSPACES,
        NATIVE_TERMUX
    }

    /**
     * Objek representasi target eksekusi runtime (Root Chroot / Container / Native).
     */
    public static class TargetEnvironment {
        public final String id;
        public final String displayName;
        public final String rootfsPath;
        public final boolean isRoot;
        public final RunnerType type;

        public TargetEnvironment(String id, String displayName, String rootfsPath, boolean isRoot, RunnerType type) {
            this.id = id;
            this.displayName = displayName;
            this.rootfsPath = rootfsPath;
            this.isRoot = isRoot;
            this.type = type;
        }

        public boolean isInstalled() {
            if (isRoot && rootfsPath != null) {
                return new File(rootfsPath + "/usr/local/bin/9router").exists()
                    || new File(rootfsPath + "/usr/bin/9router").exists()
                    || new File(rootfsPath + "/root/wallet/9router.cjs").exists();
            } else {
                return new File("/data/data/com.termux/files/usr/bin/9router").exists()
                    || new File("/data/data/com.termux/files/home/wallet/9router.cjs").exists()
                    || new File("/data/data/com.termux/files/usr/lib/node_modules/9router").exists();
            }
        }
    }

    /**
     * Memindai semua container di /data/local dan lingkungan host secara dinamis.
     */
    public static List<TargetEnvironment> scanAvailableTargets() {
        List<TargetEnvironment> list = new ArrayList<>();

        // 1. Scan NetHunter di /data/local/nhsystem/
        String[] nhCandidates = new String[]{"/data/local/nhsystem/kali-arm64", "/data/local/nhsystem/kalifs"};
        for (String p : nhCandidates) {
            File f = new File(p);
            if (f.exists() && f.isDirectory()) {
                list.add(new TargetEnvironment("nh_" + f.getName(), "Kali NetHunter (" + p + ")", p, true, RunnerType.NETHUNTER));
            }
        }

        // 2. Scan Droidspaces Containers & spaces di /data/local/Droidspaces/
        String[] dsRoots = new String[]{"/data/local/Droidspaces/Containers", "/data/local/Droidspaces/spaces"};
        for (String dsRoot : dsRoots) {
            File rootDir = new File(dsRoot);
            if (rootDir.exists() && rootDir.isDirectory()) {
                File[] subs = rootDir.listFiles(File::isDirectory);
                if (subs != null) {
                    for (File sub : subs) {
                        File subRootfs = new File(sub, "rootfs");
                        String p = subRootfs.exists() ? subRootfs.getAbsolutePath() : sub.getAbsolutePath();
                        list.add(new TargetEnvironment("ds_" + sub.getName(), "Droidspaces: " + sub.getName() + " (" + p + ")", p, true, RunnerType.DROIDSPACES));
                    }
                }
            }
        }

        // 3. Scan Custom Linux Chroot di /data/local/
        String[] customChroots = new String[]{"/data/local/ubuntu", "/data/local/debian", "/data/local/kali", "/data/local/chroot"};
        for (String cp : customChroots) {
            File cf = new File(cp);
            if (cf.exists() && cf.isDirectory() && (new File(cf, "bin/bash").exists() || new File(cf, "usr/bin/bash").exists())) {
                list.add(new TargetEnvironment("custom_" + cf.getName(), "Custom Chroot: " + cf.getName() + " (" + cp + ")", cp, true, RunnerType.NETHUNTER));
            }
        }

        // 4. Selalu sediakan Native Termux Host
        list.add(new TargetEnvironment("native_termux", "Native Termux Host (Non-Root / Sandbox)", "/data/data/com.termux/files/usr", false, RunnerType.NATIVE_TERMUX));

        return list;
    }

    /**
     * Membaca konfigurasi target yang disimpan saat ./setup.
     */
    private static String readSavedConfigTarget() {
        String[] configPaths = new String[]{
            "/data/data/com.termux/files/home/.fusion/9router_target.conf",
            "/root/.fusion/9router_target.conf"
        };
        for (String p : configPaths) {
            try {
                File f = new File(p);
                if (f.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        String line = br.readLine();
                        if (line != null && !line.trim().isEmpty()) {
                            return line.trim();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Mendapatkan target yang sedang dipilih pengguna dari Settings atau konfigurasi.
     */
    public static TargetEnvironment getSelectedTarget(Context context) {
        List<TargetEnvironment> targets = scanAvailableTargets();
        if (context != null) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String savedId = prefs.getString("pref_9router_target_runtime", null);
                if (savedId != null) {
                    for (TargetEnvironment t : targets) {
                        if (t.id.equals(savedId)) return t;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String configPath = readSavedConfigTarget();
        if (configPath != null) {
            for (TargetEnvironment t : targets) {
                if (t.rootfsPath != null && t.rootfsPath.equals(configPath)) return t;
                if (t.id.contains(configPath)) return t;
            }
        }

        // Default: Utamakan container root pertama jika ada
        for (TargetEnvironment t : targets) {
            if (t.isRoot) return t;
        }
        return targets.get(targets.size() - 1);
    }

    /**
     * Memeriksa apakah binary/paket 9router sudah terpasang di target aktif.
     */
    public static boolean isInstalled(Context context) {
        return getSelectedTarget(context).isInstalled();
    }

    public static boolean isInstalled() {
        return isInstalled(null);
    }

    /**
     * Memeriksa apakah port server API 9router (20128) sedang aktif.
     */
    public static boolean isServiceRunning() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", PORT_API), 200);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Memeriksa apakah port MITM SSL 9router (443) sedang aktif.
     */
    public static boolean isMitmRunning() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", PORT_MITM), 200);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static RunnerType getDetectedRunner() {
        return getSelectedTarget(null).type;
    }

    public static boolean isRootAvailable(Context context) {
        return getSelectedTarget(context).isRoot;
    }

    public static boolean isRootAvailable() {
        return isRootAvailable(null);
    }

    public static String getRunnerDisplayName(Context context) {
        return getSelectedTarget(context).displayName;
    }

    public static String getRunnerDisplayName() {
        return getRunnerDisplayName(null);
    }

    /**
     * Menghitung jumlah akun Antigravity yang tersimpan di disk atau database SQLite 9Router.
     * Mengembalikan 0 jika belum ada akun (100% dinamis).
     */
    public static int getAccountCount(Context context) {
        TargetEnvironment target = getSelectedTarget(context);
        List<String> paths = new ArrayList<>();
        if (target != null && target.rootfsPath != null && !target.rootfsPath.isEmpty()) {
            paths.add(target.rootfsPath + "/root/.agy-accounts");
        }
        paths.add("/root/.agy-accounts");
        paths.add("/data/local/nhsystem/kali-arm64/root/.agy-accounts");
        paths.add("/data/data/com.termux/files/home/.agy-accounts");

        for (String p : paths) {
            try {
                File dir = new File(p);
                if (dir.exists() && dir.isDirectory()) {
                    File[] jsonFiles = dir.listFiles((d, name) -> name != null && name.endsWith(".json"));
                    if (jsonFiles != null && jsonFiles.length > 0) {
                        return jsonFiles.length;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Coba baca dari tabel providerConnections di database SQLite 9Router
        int dbCount = getDbAccountCount(context);
        if (dbCount > 0) {
            return dbCount;
        }

        // Jika akses file dibatasi permission, coba hitung via su
        if (target != null && target.isRoot && target.rootfsPath != null) {
            try {
                Process p;
                if (target.type == RunnerType.DROIDSPACES) {
                    String dsName = target.id.replace("ds_", "");
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                        "/data/local/Droidspaces/bin/droidspaces --name=\"" + dsName + "\" enter -- /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin /bin/sh -c 'ls -1 /root/.agy-accounts/*.json 2>/dev/null | wc -l'"});
                } else {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                        "chroot " + target.rootfsPath + " /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin /bin/sh -c 'ls -1 /root/.agy-accounts/*.json 2>/dev/null | wc -l'"});
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    int c = Integer.parseInt(line.trim());
                    if (c > 0) return c;
                }
            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    public static int getAccountCount() {
        return getAccountCount(null);
    }

    /**
     * Membaca jumlah akun aktif langsung dari tabel providerConnections data.sqlite.
     */
    public static int getDbAccountCount(Context context) {
        TargetEnvironment target = getSelectedTarget(context);
        List<String> paths = new ArrayList<>();
        if (target != null && target.rootfsPath != null && !target.rootfsPath.isEmpty()) {
            paths.add(target.rootfsPath + "/root/.9router/db/data.sqlite");
        }
        paths.add("/root/.9router/db/data.sqlite");
        paths.add("/data/local/nhsystem/kali-arm64/root/.9router/db/data.sqlite");
        paths.add("/data/data/com.termux/files/home/.9router/db/data.sqlite");

        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    db = SQLiteDatabase.openDatabase(p, null, SQLiteDatabase.OPEN_READONLY);
                    cursor = db.rawQuery("SELECT count(*) FROM providerConnections WHERE provider = 'antigravity'", null);
                    if (cursor != null && cursor.moveToFirst()) {
                        return cursor.getInt(0);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (cursor != null) try { cursor.close(); } catch (Exception ignored) {}
                    if (db != null) try { db.close(); } catch (Exception ignored) {}
                }
            }
        }
        return 0;
    }

    /**
     * Mengambil API Key 9Router dari database SQLite.
     */
    public static String getApiKey(Context context) {
        TargetEnvironment target = getSelectedTarget(context);
        List<String> paths = new ArrayList<>();
        if (target != null && target.rootfsPath != null && !target.rootfsPath.isEmpty()) {
            paths.add(target.rootfsPath + "/root/.9router/db/data.sqlite");
        }
        paths.add("/root/.9router/db/data.sqlite");
        paths.add("/data/local/nhsystem/kali-arm64/root/.9router/db/data.sqlite");
        paths.add("/data/data/com.termux/files/home/.9router/db/data.sqlite");

        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    db = SQLiteDatabase.openDatabase(p, null, SQLiteDatabase.OPEN_READONLY);
                    cursor = db.rawQuery("SELECT key FROM apiKeys WHERE isActive = 1 LIMIT 1", null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String k = cursor.getString(0);
                        if (k != null && !k.trim().isEmpty()) {
                            return k.trim();
                        }
                    }
                    if (cursor != null) cursor.close();
                    cursor = db.rawQuery("SELECT key FROM apiKeys LIMIT 1", null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String k = cursor.getString(0);
                        if (k != null && !k.trim().isEmpty()) {
                            return k.trim();
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    if (cursor != null) try { cursor.close(); } catch (Exception ignored) {}
                    if (db != null) try { db.close(); } catch (Exception ignored) {}
                }
            }
        }

        // Coba dari AutoConfigData dulu karena 9router-autoconfig --json sudah membaca langsung
        AutoConfigData auto = getAutoConfigData(context);
        if (auto != null && auto.apiKey != null && !auto.apiKey.isEmpty()) {
            return auto.apiKey;
        }

        // Fallback jika file direct dibatasi permission
        if (target != null && target.isRoot && target.rootfsPath != null) {
            try {
                Process p;
                String cmd = "/usr/local/bin/9router-autoconfig apikey 2>/dev/null";
                if (target.type == RunnerType.DROIDSPACES) {
                    String dsName = target.id.replace("ds_", "");
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                        "/data/local/Droidspaces/bin/droidspaces --name=\"" + dsName + "\" enter -- /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                } else {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                        "chroot " + target.rootfsPath + " /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            } catch (Exception ignored) {
            }
        } else {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{
                    "/data/data/com.termux/files/usr/bin/bash", "-c",
                    "export HOME=/data/data/com.termux/files/home PATH=/data/data/com.termux/files/usr/bin:$PATH && /usr/local/bin/9router-autoconfig apikey 2>/dev/null"});
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public static String getApiKey() {
        return getApiKey(null);
    }

    /**
     * Menjalankan perintah secara asinkron di target lingkungan yang ditentukan.
     */
    public static void runAsyncCommand(Context context, TargetEnvironment target, final String cmd, final Runnable onDone) {
        if (target == null) target = getSelectedTarget(context);
        final TargetEnvironment finalTarget = target;
        new Thread(() -> {
            try {
                Process p;
                if (finalTarget.isRoot) {
                    if (finalTarget.type == RunnerType.DROIDSPACES) {
                        String dsName = finalTarget.id.replace("ds_", "");
                        p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                            "/data/local/Droidspaces/bin/droidspaces --name=\"" + dsName + "\" enter -- /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                    } else {
                        p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                            "chroot " + finalTarget.rootfsPath + " /usr/bin/env -i HOME=/root USER=root " +
                            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                    }
                } else {
                    p = Runtime.getRuntime().exec(new String[]{
                        "/data/data/com.termux/files/usr/bin/bash", "-c",
                        "export HOME=/data/data/com.termux/files/home PATH=/data/data/com.termux/files/usr/bin:$PATH && " + cmd});
                }
                p.waitFor();
            } catch (Exception ignored) {
            } finally {
                if (onDone != null) {
                    new Handler(Looper.getMainLooper()).post(onDone);
                }
            }
        }).start();
    }

    public static void runAsyncCommand(final String cmd, final Runnable onDone) {
        runAsyncCommand(null, null, cmd, onDone);
    }

    public static void startService(Context context, final Runnable onDone) {
        runAsyncCommand(context, null, "/usr/local/bin/start-9router", onDone);
    }

    public static void startService(final Runnable onDone) {
        startService(null, onDone);
    }

    public static void stopService(Context context, final Runnable onDone) {
        runAsyncCommand(context, null, "/usr/local/bin/stop-9router", onDone);
    }

    public static void stopService(final Runnable onDone) {
        stopService(null, onDone);
    }

    public static void installRouter(Context context, TargetEnvironment target, final Runnable onDone) {
        if (target == null) target = getSelectedTarget(context);
        String cmd;
        if (target.isRoot) {
            cmd = "npm install -g 9router";
        } else {
            cmd = "pkg update -y && pkg install -y nodejs-lts && npm install -g 9router";
        }
        runAsyncCommand(context, target, cmd, onDone);
    }

    public static void installRouter(Context context, final Runnable onDone) {
        installRouter(context, null, onDone);
    }

    public static void installRouter(final Runnable onDone) {
        installRouter(null, null, onDone);
    }

    public static void syncAccounts(Context context, final Runnable onDone) {
        runAsyncCommand(context, null, "/usr/local/bin/sync-agy-to-9router", onDone);
    }

    public static void syncAccounts(final Runnable onDone) {
        syncAccounts(null, onDone);
    }

    /**
     * Membuka halaman visual WebUI 9router di Webview Termux.
     * Otomatis mengaktifkan background service jika belum berjalan dan menunggu port 20128 siap
     * sebelum membuka tab Webview serta memanggil callback onReady (misal menutup halaman pengaturan).
     */
    public static void openDashboard(Context context, final Runnable onReady) {
        new Thread(() -> {
            boolean wasRunning = isServiceRunning();
            if (!wasRunning) {
                startService(context, null);
            }

            // Tunggu/polling hingga port 20128 siap merespons (maksimal 8 detik)
            long startTime = System.currentTimeMillis();
            long timeout = 8000;
            boolean ready = false;

            while (System.currentTimeMillis() - startTime < timeout) {
                if (isServiceRunning()) {
                    ready = true;
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }
            }

            // Beri jeda penyangga ekstra (400ms) agar HTTP server internal 9Router siap menerima HTTP request
            if (ready) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {
                }
            }

            final boolean isReady = ready;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isReady) {
                    boolean opened = false;
                    try {
                        FusionWebViewManager manager = FusionWebViewManager.getInstance();
                        if (manager != null) {
                            manager.addNewTab(DASHBOARD_URL);
                            opened = true;
                        }
                    } catch (Exception ignored) {
                    }
                    if (!opened && context != null) {
                        try {
                            runAsyncCommand(context, null, "termux-ipc webview " + DASHBOARD_URL, null);
                        } catch (Exception ignored) {
                        }
                    }
                    if (onReady != null) {
                        onReady.run();
                    }
                } else {
                    if (context != null) {
                        Toast.makeText(context, "Layanan 9Router belum siap di port 20128. Silakan periksa service 9Router.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }).start();
    }

    public static void openDashboard(Context context) {
        openDashboard(context, null);
    }

    public static class ClientConfigInfo {
        public String id;
        public String name;
        public String version;
        public boolean isConfigured;
        public String configPath;
    }

    public static class AutoConfigData {
        public String apiKey = null;
        public String endpoint = LOCAL_BASE_URL;
        public String defaultModel = "ag/gemini-3.8-flash-medium";
        public List<String> models = new ArrayList<>();
        public List<ClientConfigInfo> clients = new ArrayList<>();
    }

    /**
     * Mengambil model default dari 9router catalog atau database.
     */
    public static String getDefaultModel(Context context) {
        AutoConfigData data = getAutoConfigData(context);
        return data != null && data.defaultModel != null ? data.defaultModel : "ag/gemini-3.8-flash-medium";
    }

    /**
     * Membaca data klien AI yang terpasang dan model aktif via 9router-autoconfig --json.
     */
    public static AutoConfigData getAutoConfigData(Context context) {
        TargetEnvironment target = getSelectedTarget(context);
        AutoConfigData result = new AutoConfigData();
        String jsonStr = null;

        try {
            Process p;
            String cmd = "/usr/local/bin/9router-autoconfig --json 2>/dev/null || node /root/wallet/9router.cjs autoconfig --json 2>/dev/null";
            if (target != null && target.isRoot && target.rootfsPath != null) {
                if (target.type == RunnerType.DROIDSPACES) {
                    String dsName = target.id.replace("ds_", "");
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                        "/data/local/Droidspaces/bin/droidspaces --name=\"" + dsName + "\" enter -- /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                } else {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                        "chroot " + target.rootfsPath + " /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                }
            } else {
                p = Runtime.getRuntime().exec(new String[]{
                    "/data/data/com.termux/files/usr/bin/bash", "-c",
                    "export HOME=/data/data/com.termux/files/home PATH=/data/data/com.termux/files/usr/bin:$PATH && " + cmd});
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            p.waitFor();
            jsonStr = sb.toString().trim();
        } catch (Exception ignored) {
        }

        if (jsonStr != null && jsonStr.startsWith("{")) {
            try {
                JSONObject obj = new JSONObject(jsonStr);
                if (obj.has("apiKey")) {
                    result.apiKey = obj.getString("apiKey");
                }
                if (obj.has("defaultModel")) {
                    result.defaultModel = obj.getString("defaultModel");
                }
                if (obj.has("endpoint")) {
                    result.endpoint = obj.getString("endpoint");
                }
                if (obj.has("models")) {
                    JSONArray mArr = obj.getJSONArray("models");
                    for (int i = 0; i < mArr.length(); i++) {
                        result.models.add(mArr.getString(i));
                    }
                }
                if (obj.has("clients")) {
                    JSONArray cArr = obj.getJSONArray("clients");
                    for (int i = 0; i < cArr.length(); i++) {
                        JSONObject cObj = cArr.getJSONObject(i);
                        ClientConfigInfo info = new ClientConfigInfo();
                        info.id = cObj.getString("id");
                        info.name = cObj.getString("name");
                        info.version = cObj.optString("version", "");
                        info.isConfigured = cObj.optBoolean("isConfigured", false);
                        info.configPath = cObj.optString("configPath", "");
                        result.clients.add(info);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public static void applyAutoConfig(Context context, List<String> clientIds, final Runnable onDone) {
        if (clientIds == null || clientIds.isEmpty()) {
            if (onDone != null) onDone.run();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String id : clientIds) {
            sb.append(" ").append(id);
        }
        String cmd = "/usr/local/bin/9router-autoconfig apply" + sb.toString();
        runAsyncCommand(context, null, cmd, onDone);
    }
}
