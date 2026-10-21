package com.termux.webview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
     * Menghitung jumlah akun Antigravity yang tersimpan di disk.
     */
    public static int getAccountCount() {
        String[] paths = new String[]{
            "/root/.agy-accounts",
            "/data/local/nhsystem/kali-arm64/root/.agy-accounts",
            "/data/data/com.termux/files/home/.agy-accounts"
        };
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
        return 5;
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
                            "/data/local/Droidspaces/bin/droidspaces --name=\"" + dsName + "\" enter -- " + cmd});
                    } else {
                        p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                            "chroot " + finalTarget.rootfsPath + " /usr/bin/env -i HOME=/root USER=root " +
                            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                    }
                } else {
                    p = Runtime.getRuntime().exec(new String[]{
                        "/data/data/com.termux/files/usr/bin/bash", "-c", cmd});
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
     */
    public static void openDashboard(Context context) {
        try {
            FusionWebViewManager manager = FusionWebViewManager.getInstance();
            if (manager != null) {
                manager.addNewTab(DASHBOARD_URL);
            }
        } catch (Exception ignored) {
        }
    }
}
