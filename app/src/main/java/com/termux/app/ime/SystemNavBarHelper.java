package com.termux.app.ime;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Helper to query and control the global Android Navigation Bar keyboard button (down arrow).
 * Auto-detects root access (su) and WRITE_SECURE_SETTINGS permission.
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
     * Detects if root access (su) is available and functional.
     * Caches the result to prevent repeated process spawns.
     */
    public static boolean isRootAvailable() {
        if (sIsRootAvailable != null) {
            return sIsRootAvailable;
        }

        // Fast check for common su binaries
        boolean hasBinary = false;
        String[] paths = {
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/adb/ksu/bin/su", "/data/adb/ap/bin/su", "/data/adb/magisk/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                hasBinary = true;
                break;
            }
        }

        // Verify root execution
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int exitCode = process.waitFor();
            boolean rootOk = (exitCode == 0 && line != null && (line.contains("uid=0") || line.contains("root")));
            sIsRootAvailable = rootOk;
            return rootOk;
        } catch (Throwable t) {
            sIsRootAvailable = false;
            return false;
        }
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
            if (Settings.Secure.putInt(context.getContentResolver(), SETTING_KEY, value)) {
                return true;
            }
        } catch (SecurityException ignored) {
            // Permission not granted directly, fall back to root
        } catch (Throwable ignored) {}

        // 2. Fall back to root su
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "su", "-c", "settings put secure " + SETTING_KEY + " " + value
            });
            int exit = process.waitFor();
            return exit == 0;
        } catch (Throwable t) {
            return false;
        }
    }
}
