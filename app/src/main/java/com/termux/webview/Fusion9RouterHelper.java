package com.termux.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Helper pengelola background service 9Router, multi-account rotation,
 * dan deteksi lingkungan (Root Kali NetHunter / Root Droidspaces / Non-Root Termux).
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
     * Memeriksa apakah binary/paket 9router sudah terpasang di sistem yang terdeteksi.
     */
    public static boolean isInstalled() {
        RunnerType r = getDetectedRunner();
        if (r == RunnerType.NETHUNTER) {
            return new File("/data/local/nhsystem/kali-arm64/usr/local/bin/9router").exists()
                || new File("/data/local/nhsystem/kali-arm64/usr/bin/9router").exists()
                || new File("/usr/local/bin/9router").exists();
        } else if (r == RunnerType.DROIDSPACES) {
            return new File("/data/local/Droidspaces/ubuntu/usr/local/bin/9router").exists()
                || new File("/data/local/Droidspaces/ubuntu/usr/bin/9router").exists()
                || new File("/usr/local/bin/9router").exists();
        } else {
            return new File("/data/data/com.termux/files/usr/bin/9router").exists()
                || new File("/data/data/com.termux/files/usr/lib/node_modules/9router").exists();
        }
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

    /**
     * Deteksi lingkungan runtime perangkat.
     */
    public static RunnerType getDetectedRunner() {
        if (new File("/data/local/nhsystem/kali-arm64").exists()) {
            return RunnerType.NETHUNTER;
        }
        if (new File("/data/local/Droidspaces/bin/droidspaces").exists()) {
            return RunnerType.DROIDSPACES;
        }
        return RunnerType.NATIVE_TERMUX;
    }

    public static boolean isRootAvailable() {
        RunnerType r = getDetectedRunner();
        return r == RunnerType.NETHUNTER || r == RunnerType.DROIDSPACES;
    }

    public static String getRunnerDisplayName() {
        RunnerType r = getDetectedRunner();
        switch (r) {
            case NETHUNTER:
                return "Kali NetHunter (Infinix Root)";
            case DROIDSPACES:
                return "Droidspaces (Poco Root)";
            default:
                return "Native Termux (Non-Root)";
        }
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
        return 5; // Default pool yang terpasang
    }

    /**
     * Menjalankan perintah di background thread sesuai runner yang terdeteksi.
     */
    public static void runAsyncCommand(final String cmd, final Runnable onDone) {
        new Thread(() -> {
            try {
                RunnerType r = getDetectedRunner();
                Process p;
                if (r == RunnerType.NETHUNTER) {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c",
                        "chroot /data/local/nhsystem/kali-arm64 /usr/bin/env -i HOME=/root USER=root " +
                        "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " + cmd});
                } else if (r == RunnerType.DROIDSPACES) {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                        "/data/local/Droidspaces/bin/droidspaces enter -- " + cmd});
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

    public static void startService(final Runnable onDone) {
        runAsyncCommand("/usr/local/bin/start-9router", onDone);
    }

    public static void stopService(final Runnable onDone) {
        runAsyncCommand("/usr/local/bin/stop-9router", onDone);
    }

    public static void installRouter(final Runnable onDone) {
        RunnerType r = getDetectedRunner();
        String cmd;
        if (r == RunnerType.NETHUNTER) {
            cmd = "npm install -g 9router";
        } else if (r == RunnerType.DROIDSPACES) {
            cmd = "npm install -g 9router";
        } else {
            cmd = "pkg update -y && pkg install -y nodejs-lts && npm install -g 9router";
        }
        runAsyncCommand(cmd, onDone);
    }

    public static void syncAccounts(final Runnable onDone) {
        runAsyncCommand("/usr/local/bin/sync-agy-to-9router", onDone);
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
