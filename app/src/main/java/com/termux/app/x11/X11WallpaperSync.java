package com.termux.app.x11;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants;
import com.termux.webview.Fusion9RouterHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

/**
 * Synchronizes Android system wallpaper to Linux X11/Desktop environment in real-time.
 * Generates pixel-perfect 1:1 display frame alignment for transparent frosted glass windows.
 */
public final class X11WallpaperSync {
    private static final String LOG_TAG = "X11WallpaperSync";

    public static final String SYNC_WALLPAPER_PATH = "/data/data/com.termux/files/home/.termux/android_wallpaper.png";
    public static final String SYNC_WALLPAPER_TMP_PATH = "/data/data/com.termux/files/home/.termux/.android_wallpaper.png.tmp";
    public static final String WALLPAPER_MODE_FILE_PATH = "/data/data/com.termux/files/home/.termux/x11_wallpaper_mode";

    public static final String SYNC_WALLPAPER_TMPDIR_PATH = "/data/data/com.termux/files/usr/tmp/.X11-unix/android_wallpaper.png";
    public static final String WALLPAPER_MODE_TMPDIR_PATH = "/data/data/com.termux/files/usr/tmp/.X11-unix/x11_wallpaper_mode";

    private X11WallpaperSync() {}

    /**
     * Syncs wallpaper configuration and exports Android wallpaper if mode is "sync".
     *
     * @param context the Android context
     * @param forceRefresh whether to immediately refresh X11 root window wallpaper if running
     */
    public static void syncWallpaper(@NonNull Context context, boolean forceRefresh) {
        TermuxAppSharedPreferences prefs = TermuxAppSharedPreferences.build(context, false);
        if (prefs == null) return;

        String mode = prefs.getX11WallpaperMode();
        saveWallpaperMode(mode);

        if (TermuxPreferenceConstants.TERMUX_APP.X11_WALLPAPER_MODE_DEFAULT.equals(mode)) {
            if (forceRefresh || X11DisplayServerState.isRunning()) {
                refreshX11Wallpaper(context);
            }
            return;
        }

        // Mode is "sync" (or default fallback)
        new Thread(() -> {
            try {
                exportAndroidWallpaper(context);
                if (forceRefresh || X11DisplayServerState.isRunning()) {
                    refreshX11Wallpaper(context);
                }
            } catch (Exception e) {
                Logger.logStackTraceWithMessage(LOG_TAG, "Failed to sync Android wallpaper to X11", e);
            }
        }, "X11WallpaperSyncWorker").start();
    }

    private static void saveWallpaperMode(String mode) {
        String val = mode != null ? mode : TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_WALLPAPER_MODE;
        writeStringToFile(WALLPAPER_MODE_FILE_PATH, val);
        writeStringToFile(WALLPAPER_MODE_TMPDIR_PATH, val);
    }

    private static void writeStringToFile(String path, String content) {
        try {
            File modeFile = new File(path);
            File parent = modeFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                parent.setReadable(true, false);
                parent.setExecutable(true, false);
            }
            try (FileWriter writer = new FileWriter(modeFile, false)) {
                writer.write(content);
            }
            modeFile.setReadable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write wallpaper mode file " + path + ": " + e.getMessage());
        }
    }

    private static void exportAndroidWallpaper(@NonNull Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Drawable wallpaper = null;
        try {
            wallpaper = wallpaperManager.getDrawable();
        } catch (SecurityException e) {
            Logger.logWarn(LOG_TAG, "Wallpaper read permission denied: " + e.getMessage());
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to get wallpaper drawable: " + e.getMessage());
        }
        if (wallpaper == null) {
            try {
                wallpaper = wallpaperManager.peekDrawable();
            } catch (Exception ignored) {}
        }
        if (wallpaper == null) {
            try {
                File managedFile = com.termux.app.chrome.WallpaperPictureReader.managedWallpaperExactFile(context);
                if (managedFile.isFile()) {
                    wallpaper = Drawable.createFromPath(managedFile.getAbsolutePath());
                }
            } catch (Exception ignored) {}
        }
        if (wallpaper == null) return;

        int frameWidth = 1080;
        int frameHeight = 2400;
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null && wm.getDefaultDisplay() != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getRealMetrics(metrics);
                if (metrics.widthPixels > 0 && metrics.heightPixels > 0) {
                    frameWidth = metrics.widthPixels;
                    frameHeight = metrics.heightPixels;
                }
            }
        } catch (Exception ignored) {}

        int intrinsicWidth = wallpaper.getIntrinsicWidth() > 0 ? wallpaper.getIntrinsicWidth() : frameWidth;
        int intrinsicHeight = wallpaper.getIntrinsicHeight() > 0 ? wallpaper.getIntrinsicHeight() : frameHeight;
        float scale = Math.max((float) frameWidth / intrinsicWidth, (float) frameHeight / intrinsicHeight);
        int drawWidth = Math.max(frameWidth, Math.round(intrinsicWidth * scale));
        int drawHeight = Math.max(frameHeight, Math.round(intrinsicHeight * scale));

        int offsetLeft = Math.round((frameWidth - drawWidth) / 2f);
        int offsetTop = Math.round((frameHeight - drawHeight) / 2f);

        Bitmap bitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = wallpaper.getConstantState() != null
            ? wallpaper.getConstantState().newDrawable().mutate()
            : wallpaper.mutate();
        drawable.setBounds(offsetLeft, offsetTop, offsetLeft + drawWidth, offsetTop + drawHeight);
        drawable.draw(canvas);

        try {
            wallpaperManager.forgetLoadedWallpaper();
        } catch (Exception ignored) {}

        File tmpFile = new File(SYNC_WALLPAPER_TMP_PATH);
        File targetFile = new File(SYNC_WALLPAPER_PATH);
        File altTargetFile = new File(SYNC_WALLPAPER_TMPDIR_PATH);
        try {
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                parent.setReadable(true, false);
                parent.setExecutable(true, false);
            }
            try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }
            if (tmpFile.renameTo(targetFile)) {
                targetFile.setReadable(true, false);
            }
            try {
                File altParent = altTargetFile.getParentFile();
                if (altParent != null && !altParent.exists()) {
                    altParent.mkdirs();
                }
                java.nio.file.Files.copy(targetFile.toPath(), altTargetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                altTargetFile.setReadable(true, false);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Logger.logStackTraceWithMessage(LOG_TAG, "Failed to save exported wallpaper PNG", e);
        } finally {
            bitmap.recycle();
        }
    }

    /**
     * Refreshes the active desktop wallpaper in X11 using wallpaper-render.sh or feh.
     */
    public static void refreshX11Wallpaper(@NonNull Context context) {
        String cmd = "DISPLAY=:0 [ -x \"$HOME/.config/bspwm/wallpaper-render.sh\" ] && \"$HOME/.config/bspwm/wallpaper-render.sh\" >/dev/null 2>&1 || DISPLAY=:0 [ -x /root/.config/bspwm/wallpaper-render.sh ] && /root/.config/bspwm/wallpaper-render.sh >/dev/null 2>&1 || DISPLAY=:0 feh --no-fehbg --bg-fill \"/data/data/com.termux/files/home/.termux/android_wallpaper.png\" >/dev/null 2>&1 || DISPLAY=:0 feh --no-fehbg --bg-fill \"/tmp/.X11-unix/android_wallpaper.png\" >/dev/null 2>&1 || true";
        Fusion9RouterHelper.runAsyncCommand(context, null, cmd, null);
    }
}
