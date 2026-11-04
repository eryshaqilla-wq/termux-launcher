package com.termux.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Airplane mode helper that auto-detects ROOT, SHIZUKU, or NONROOT.
 * If non-root and no Shizuku, operation is blocked because Android requires system privileges.
 */
public final class FusionAirplaneModeHelper {

    private static final String TAG = "FusionAirplaneMode";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public enum Mode {
        ROOT,
        SHIZUKU,
        NONROOT
    }

    private static Mode sCachedMode = null;
    private static long sLastCheckTime = 0L;
    private static final long CACHE_TTL_MS = 30_000L;

    private FusionAirplaneModeHelper() {}

    public static boolean isAirplaneModeOn(@NonNull Context context) {
        try {
            return Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0;
        } catch (Throwable t) {
            return false;
        }
    }

    @NonNull
    public static Mode detectMode(@NonNull Context context) {
        long now = System.currentTimeMillis();
        if (sCachedMode != null && (now - sLastCheckTime) < CACHE_TTL_MS) {
            return sCachedMode;
        }

        // 1. Check Root
        if (checkRoot()) {
            sCachedMode = Mode.ROOT;
            sLastCheckTime = now;
            return Mode.ROOT;
        }

        // 2. Check Shizuku
        if (checkShizuku()) {
            sCachedMode = Mode.SHIZUKU;
            sLastCheckTime = now;
            return Mode.SHIZUKU;
        }

        // 3. Non-root fallback
        sCachedMode = Mode.NONROOT;
        sLastCheckTime = now;
        return Mode.NONROOT;
    }

    private static boolean checkRoot() {
        String[] suPaths = {
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/su/bin/su", "/data/local/xbin/su", "/data/local/bin/su"
        };
        boolean suBinaryExists = false;
        for (String p : suPaths) {
            if (new File(p).exists()) {
                suBinaryExists = true;
                break;
            }
        }
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            p.waitFor();
            if (line != null && line.contains("uid=0")) {
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean checkShizuku() {
        try {
            if (rikka.shizuku.Shizuku.pingBinder()) {
                int perm = rikka.shizuku.Shizuku.checkSelfPermission();
                return perm == PackageManager.PERMISSION_GRANTED;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public interface ActionResultCallback {
        void onResult(boolean success, @NonNull Mode mode, @NonNull String message, boolean state);
    }

    public static void setAirplaneMode(@NonNull Context context, boolean enable, @Nullable ActionResultCallback callback) {
        EXECUTOR.execute(() -> {
            Mode mode = detectMode(context);
            if (mode == Mode.NONROOT) {
                String msg = "Mode Pesawat memerlukan akses Root atau Shizuku. Perangkat non-root tidak didukung!";
                if (context instanceof TermuxActivity) {
                    ((TermuxActivity) context).runOnUiThread(() ->
                        FusionToast.show(context, "⚠️ Mode Pesawat gagal: Memerlukan akses Root atau Shizuku!")
                    );
                }
                if (callback != null) {
                    callback.onResult(false, mode, msg, isAirplaneModeOn(context));
                }
                return;
            }

            String enableCmd = enable ? "enable" : "disable";
            String enableInt = enable ? "1" : "0";
            String enableBool = enable ? "true" : "false";

            String shellCmd = "cmd connectivity airplane-mode " + enableCmd
                + " || (settings put global airplane_mode_on " + enableInt
                + " && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + enableBool + ")";

            boolean success = false;
            if (mode == Mode.ROOT) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", shellCmd});
                    int code = p.waitFor();
                    success = (code == 0);
                } catch (Throwable t) {
                    Log.e(TAG, "Root airplane command failed", t);
                }
            } else if (mode == Mode.SHIZUKU) {
                try {
                    java.lang.reflect.Method newProcessMethod = rikka.shizuku.Shizuku.class.getDeclaredMethod(
                        "newProcess", String[].class, String[].class, String.class);
                    newProcessMethod.setAccessible(true);
                    Object procObj = newProcessMethod.invoke(null, new String[]{"sh", "-c", shellCmd}, null, null);
                    if (procObj instanceof Process) {
                        Process p = (Process) procObj;
                        int code = p.waitFor();
                        success = (code == 0);
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Shizuku airplane command failed", t);
                }
            }

            final boolean finalSuccess = success;
            final boolean finalState = isAirplaneModeOn(context);
            final String toastMsg = finalSuccess
                ? (enable ? "✈️ Mode Pesawat: AKTIF (" + mode.name() + ")" : "📶 Mode Pesawat: NONAKTIF (" + mode.name() + ")")
                : "⚠️ Gagal mengubah Mode Pesawat via " + mode.name();

            if (context instanceof TermuxActivity) {
                ((TermuxActivity) context).runOnUiThread(() ->
                    FusionToast.show(context, toastMsg)
                );
            }

            if (callback != null) {
                callback.onResult(finalSuccess, mode, toastMsg, finalState);
            }
        });
    }

    public static void toggle(@NonNull Context context, @Nullable ActionResultCallback callback) {
        boolean current = isAirplaneModeOn(context);
        setAirplaneMode(context, !current, callback);
    }
}
