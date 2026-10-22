package com.termux.app.ime;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.termux.app.x11.ChrootDistro;

import java.io.File;

/**
 * Helper to query and control the global Android Navigation Bar keyboard button (down arrow).
 * Auto-detects root access (su) and WRITE_SECURE_SETTINGS permission safely without hanging CI runners.
 */
public final class SystemNavBarHelper {

    public static final String SETTING_KEY = "show_keyboard_button";

    private static volatile Boolean sIsRootAvailable = null;

    private SystemNavBarHelper() {}

    /**
     * Checks if the app has permission or root privilege to modify Settings.Secure.
     */
    public static boolean canControlNavBarKeyboardButton(@NonNull Context context) {
        if (hasWriteSecureSettingsPermission(context)) {
            return true;
        }
        return isRootAvailable();
    }

    /**
     * Checks if WRITE_SECURE_SETTINGS permission has been granted to Termux.
     */
    public static boolean hasWriteSecureSettingsPermission(@NonNull Context context) {
        try {
            return context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Detects if root access (su) is available.
     * Uses Android root binary locations and ChrootDistro check to avoid hanging on host/CI machines.
     */
    public static boolean isRootAvailable() {
        if (sIsRootAvailable != null) {
            return sIsRootAvailable;
        }

        // Avoid invoking su on host machines running Robolectric tests
        if ("robolectric".equalsIgnoreCase(Build.FINGERPRINT)
                || "robolectric".equalsIgnoreCase(Build.HARDWARE)
                || "robolectric".equalsIgnoreCase(Build.DEVICE)) {
            sIsRootAvailable = false;
            return false;
        }

        // Check well-known Android root binary paths
        String[] paths = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/ap/bin/su",
            "/data/adb/magisk/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                sIsRootAvailable = true;
                return true;
            }
        }

        boolean root = ChrootDistro.isRootAvailable();
        sIsRootAvailable = root;
        return root;
    }

    /**
     * Queries whether the navigation bar keyboard button is currently enabled.
     * Default on Android is enabled (1).
     */
    public static boolean isNavBarKeyboardButtonEnabled(@NonNull Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), SETTING_KEY, 1) != 0;
        } catch (Throwable t) {
            return true;
        }
    }

    /**
     * Updates the global setting for the navigation bar keyboard button.
     * Attempts direct Settings.Secure modification first, then falls back to root su execution.
     *
     * @return true if successfully written.
     */
    public static boolean setNavBarKeyboardButtonEnabled(@NonNull Context context, boolean enabled) {
        int value = enabled ? 1 : 0;

        // 1. Try directly via ContentResolver (works if WRITE_SECURE_SETTINGS is granted)
        try {
            Settings.Secure.putInt(context.getContentResolver(), SETTING_KEY, value);
        } catch (SecurityException ignored) {
            // Permission not granted directly, fall back to root
        } catch (Throwable ignored) {}

        try {
            Settings.Global.putInt(context.getContentResolver(), SETTING_KEY, value);
        } catch (Throwable ignored) {}

        // 2. Fall back to root su on Android device
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "su", "-c", "settings put secure " + SETTING_KEY + " " + value + " 2>/dev/null; settings put global " + SETTING_KEY + " " + value + " 2>/dev/null"
            });
            process.getOutputStream().close();
            int exit = process.waitFor();
            return exit == 0;
        } catch (Throwable t) {
            return false;
        }
    }

    @androidx.annotation.VisibleForTesting
    static void resetRootCacheForTesting() {
        sIsRootAvailable = null;
    }
}
