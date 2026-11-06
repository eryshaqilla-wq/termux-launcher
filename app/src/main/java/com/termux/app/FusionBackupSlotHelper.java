package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.termux.R;
import com.termux.app.material.M3;
import com.termux.shared.termux.font.NerdFontSpans;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Universal multi-slot / profile manager for Titanium Backup (.backup / com.ery.ziggy).
 * Provides 3 methods:
 * 1. Slot Sebelumnya (Previous Slot)
 * 2. Slot Default / Aktif (displays active slot number, opens picker dialog on click)
 * 3. Slot Berikutnya (Next Slot)
 *
 * Includes auto-clear RAM/Cache on switch (parity with ESC on login menu) and root validation.
 */
public final class FusionBackupSlotHelper {

    private static final String TAG = "FusionBackupSlot";

    public static final String ACTION_SLOT_PREV = "BACKUP_SLOT_PREV";
    public static final String ACTION_SLOT_NEXT = "BACKUP_SLOT_NEXT";
    public static final String ACTION_SLOT_PICK = "BACKUP_SLOT_PICK";
    public static final String ACTION_SLOT_CURRENT = "BACKUP_SLOT_CURRENT";

    public static final String PREFS_NAME = "fusion_slot_preferences";
    public static final String KEY_AUTO_CLEAR_CACHE = "slot_auto_clear_cache";

    /**
     * Natural numeric comparator for backup slots.
     * Sorts numeric strings by numeric value (e.g. "00", "01", ... "99", "100", "101"),
     * preserving ascending numerical order rather than lexicographical order ("100" after "99", not after "10").
     */
    public static final Comparator<String> SLOT_COMPARATOR = (a, b) -> {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        String s1 = a.trim();
        String s2 = b.trim();
        boolean aIsNum = s1.matches("^[0-9]+$");
        boolean bIsNum = s2.matches("^[0-9]+$");
        if (aIsNum && bIsNum) {
            try {
                long n1 = Long.parseLong(s1);
                long n2 = Long.parseLong(s2);
                int cmp = Long.compare(n1, n2);
                if (cmp != 0) return cmp;
                return Integer.compare(s1.length(), s2.length());
            } catch (NumberFormatException ignored) {}
        }
        if (aIsNum && !bIsNum) return -1;
        if (!aIsNum && bIsNum) return 1;
        return s1.compareToIgnoreCase(s2);
    };

    public interface OnSlotPicked {
        void onPicked(@NonNull String slot);
    }

    public interface OnSlotCreatedCallback {
        void onResult(boolean success, int createdCount, int skippedCount,
                      @NonNull List<String> createdNames, @Nullable String errorMsg);
    }

    /**
     * Model representing an application participating in multi-profile slots (.data_profiles).
     */
    public static final class ProfileAppInfo {
        @NonNull public final String packageName;
        @NonNull public final String appLabel;
        @NonNull public final String currentSlot;
        public final int profileCount;
        @Nullable public final Drawable icon;
        public final boolean isInstalled;

        public ProfileAppInfo(@NonNull String packageName, @NonNull String appLabel,
                              @NonNull String currentSlot, int profileCount,
                              @Nullable Drawable icon, boolean isInstalled) {
            this.packageName = packageName;
            this.appLabel = appLabel;
            this.currentSlot = currentSlot;
            this.profileCount = profileCount;
            this.icon = icon;
            this.isInstalled = isInstalled;
        }
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static String sCachedCurrentSlot = null;
    private static List<String> sCachedAllSlots = null;
    private static long sLastCacheUpdate = 0;
    private static final long CACHE_TTL_MS = 3000;

    private static List<ProfileAppInfo> sCachedProfileApps = null;
    private static long sLastProfileAppsUpdate = 0;

    private static Boolean sCachedRootAvailable = null;
    private static long sLastRootCheckTime = 0;

    private FusionBackupSlotHelper() {}

    /**
     * Check if root (su) access is available, cached with 30s TTL.
     */
    public static synchronized boolean isRootAvailable() {
        long now = System.currentTimeMillis();
        if (sCachedRootAvailable != null && (now - sLastRootCheckTime) < 30_000L) {
            return sCachedRootAvailable;
        }

        boolean hasSu = false;
        String[] suPaths = {
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/su/bin/su", "/data/local/xbin/su", "/data/local/bin/su"
        };
        for (String p : suPaths) {
            if (new File(p).exists()) {
                hasSu = true;
                break;
            }
        }
        if (hasSu) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                int code = p.waitFor();
                hasSu = (code == 0);
            } catch (Throwable ignored) {
                hasSu = false;
            }
        }
        sCachedRootAvailable = hasSu;
        sLastRootCheckTime = now;
        return hasSu;
    }

    /**
     * Check if auto clear cache (ESC login parity) is enabled on slot switch.
     */
    public static boolean isAutoClearCacheEnabled(@NonNull Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_AUTO_CLEAR_CACHE, false);
    }

    /**
     * Set auto clear cache on slot switch.
     */
    public static void setAutoClearCacheEnabled(@NonNull Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_AUTO_CLEAR_CACHE, enabled).apply();
    }

    /**
     * Fast non-blocking synchronous getter for current cached slot (e.g. "01").
     */
    @NonNull
    public static synchronized String getCurrentSlotCached() {
        if (sCachedCurrentSlot != null) {
            return sCachedCurrentSlot;
        }
        return "01";
    }

    /**
     * Refresh cache asynchronously from DataProfilesCache.xml and notify completion.
     */
    public static void refreshCache(@Nullable Context context, @Nullable Runnable onDone) {
        EXECUTOR.execute(() -> {
            loadSlotDataFromDisk();
            if (onDone != null) {
                MAIN_HANDLER.post(onDone);
            }
        });
    }

    /**
     * Load current slot and all slots from Titanium Backup's SharedPreferences.
     */
    private static synchronized void loadSlotDataFromDisk() {
        String curr = null;
        List<String> all = new ArrayList<>();
        if (isRootAvailable()) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "cat /data/data/com.ery.ziggy/shared_prefs/DataProfilesCache.xml 2>/dev/null"
                });
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                Pattern currPat = Pattern.compile("<string name=\"currentProfile\">([^<]+)</string>");
                Pattern profPat = Pattern.compile("<string name=\"profile\\.([^\"]+)\"");

                while ((line = reader.readLine()) != null) {
                    Matcher mCurr = currPat.matcher(line);
                    if (mCurr.find()) {
                        curr = mCurr.group(1).trim();
                    }
                    Matcher mProf = profPat.matcher(line);
                    if (mProf.find()) {
                        all.add(mProf.group(1).trim());
                    }
                }
                reader.close();
                p.waitFor();
            } catch (Throwable t) {
                Log.w(TAG, "Failed reading DataProfilesCache.xml via root", t);
            }

            // Fallback to reading Kiwi Browser .current if needed
            if (curr == null || curr.isEmpty()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{
                        "su", "-c", "readlink /data/data/com.kiwibrowser.browser/.data_profiles/.current 2>/dev/null"
                    });
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        curr = line.trim();
                    }
                    reader.close();
                    p.waitFor();
                } catch (Throwable ignored) {}
            }
        }

        if (curr == null || curr.isEmpty()) {
            curr = "01";
        }
        sCachedCurrentSlot = curr;

        if (all.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                all.add(String.format(Locale.ROOT, "%02d", i));
            }
        } else {
            Collections.sort(all, SLOT_COMPARATOR);
        }
        sCachedAllSlots = all;
        sLastCacheUpdate = System.currentTimeMillis();
    }

    /**
     * Scans all installed packages that have .data_profiles directory.
     * Returns a list of ProfileAppInfo containing app label, package, current active slot,
     * total profile directories, and app icon.
     */
    @NonNull
    public static List<ProfileAppInfo> scanProfileApps(@NonNull Context context) {
        long now = System.currentTimeMillis();
        synchronized (FusionBackupSlotHelper.class) {
            if (sCachedProfileApps != null && (now - sLastProfileAppsUpdate < 4000L)) {
                return new ArrayList<>(sCachedProfileApps);
            }
        }

        List<ProfileAppInfo> result = new ArrayList<>();
        if (!isRootAvailable()) {
            return result;
        }

        PackageManager pm = context.getPackageManager();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/sh"});
            String script = "for pdir in $(find /data/data -maxdepth 2 -name .data_profiles 2>/dev/null); do\n" +
                "  [ -d \"$pdir\" ] || continue\n" +
                "  pkg=$(basename \"$(dirname \"$pdir\")\")\n" +
                "  curr=$(readlink \"$pdir/.current\" 2>/dev/null)\n" +
                "  [ -z \"$curr\" ] && [ -f \"$pdir/.current\" ] && curr=$(cat \"$pdir/.current\" 2>/dev/null)\n" +
                "  [ -z \"$curr\" ] && curr=\"?\"\n" +
                "  cnt=$(ls -1 \"$pdir\" 2>/dev/null | grep -v '^\\.' | wc -l)\n" +
                "  echo \"$pkg|$curr|$cnt\"\n" +
                "done\n";
            java.io.OutputStream os = p.getOutputStream();
            os.write(script.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 1) {
                    String pkg = parts[0].trim();
                    String curr = parts.length > 1 ? parts[1].trim() : "?";
                    int count = 0;
                    if (parts.length > 2) {
                        try {
                            count = Integer.parseInt(parts[2].trim());
                        } catch (Exception ignored) {}
                    }

                    String label = pkg;
                    Drawable icon = null;
                    boolean installed = false;
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                        CharSequence l = pm.getApplicationLabel(ai);
                        if (l != null) label = l.toString();
                        icon = pm.getApplicationIcon(ai);
                        installed = true;
                    } catch (PackageManager.NameNotFoundException ignored) {}

                    result.add(new ProfileAppInfo(pkg, label, curr, count, icon, installed));
                }
            }
            reader.close();
            p.waitFor();
        } catch (Throwable t) {
            Log.e(TAG, "Error scanning profile apps", t);
        }

        Collections.sort(result, (a, b) -> a.appLabel.compareToIgnoreCase(b.appLabel));

        synchronized (FusionBackupSlotHelper.class) {
            sCachedProfileApps = result;
            sLastProfileAppsUpdate = System.currentTimeMillis();
        }
        return result;
    }

    /**
     * Switch to target profile slot.
     */
    public static void switchToSlot(@NonNull Context context, @NonNull String targetSlot,
                                    @Nullable Runnable onComplete) {
        final Context appCtx = context.getApplicationContext();
        if (!isRootAvailable()) {
            FusionToast.show(appCtx, "⚠️ Akses root diperlukan untuk mengganti slot .backup!");
            return;
        }

        final boolean clearCache = isAutoClearCacheEnabled(appCtx);
        EXECUTOR.execute(() -> {
            boolean success = executeSlotSwitch(targetSlot, clearCache);
            synchronized (FusionBackupSlotHelper.class) {
                sCachedCurrentSlot = targetSlot;
            }
            MAIN_HANDLER.post(() -> {
                if (success) {
                    String msg = "Slot Aktif: " + targetSlot + (clearCache ? ", Dan Cache Dibersihkan!" : "");
                    FusionToast.show(appCtx, msg);
                } else {
                    FusionToast.show(appCtx, "⚠️ Gagal mengganti slot: " + targetSlot);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    /**
     * Switch to previous slot.
     */
    public static void prevSlot(@NonNull Context context, @Nullable Runnable onComplete) {
        final Context appCtx = context.getApplicationContext();
        if (!isRootAvailable()) {
            FusionToast.show(appCtx, "⚠️ Akses root diperlukan untuk mengganti slot .backup!");
            return;
        }

        final boolean clearCache = isAutoClearCacheEnabled(appCtx);
        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            String curr = sCachedCurrentSlot;
            List<String> all = sCachedAllSlots;
            int idx = (all != null) ? all.indexOf(curr) : -1;
            String target;
            if (all != null && !all.isEmpty()) {
                int prevIdx = (idx <= 0) ? (all.size() - 1) : (idx - 1);
                target = all.get(prevIdx);
            } else {
                target = "00";
            }

            boolean success = executeSlotSwitch(target, clearCache);
            synchronized (FusionBackupSlotHelper.class) {
                sCachedCurrentSlot = target;
            }
            MAIN_HANDLER.post(() -> {
                if (success) {
                    String msg = "Slot Sebelumnya: " + target + (clearCache ? ", Dan Cache Dibersihkan!" : "");
                    FusionToast.show(appCtx, msg);
                } else {
                    FusionToast.show(appCtx, "⚠️ Gagal pindah ke slot sebelumnya: " + target);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    /**
     * Switch to next slot.
     */
    public static void nextSlot(@NonNull Context context, @Nullable Runnable onComplete) {
        final Context appCtx = context.getApplicationContext();
        if (!isRootAvailable()) {
            FusionToast.show(appCtx, "⚠️ Akses root diperlukan untuk mengganti slot .backup!");
            return;
        }

        final boolean clearCache = isAutoClearCacheEnabled(appCtx);
        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            String curr = sCachedCurrentSlot;
            List<String> all = sCachedAllSlots;
            int idx = (all != null) ? all.indexOf(curr) : -1;
            String target;
            if (all != null && !all.isEmpty()) {
                int nextIdx = (idx >= all.size() - 1 || idx < 0) ? 0 : (idx + 1);
                target = all.get(nextIdx);
            } else {
                target = "01";
            }

            boolean success = executeSlotSwitch(target, clearCache);
            synchronized (FusionBackupSlotHelper.class) {
                sCachedCurrentSlot = target;
            }
            MAIN_HANDLER.post(() -> {
                if (success) {
                    String msg = "Slot Berikutnya: " + target + (clearCache ? ", Dan Cache Dibersihkan!" : "");
                    FusionToast.show(appCtx, msg);
                } else {
                    FusionToast.show(appCtx, "⚠️ Gagal pindah ke slot berikutnya: " + target);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private static void ensureCacheLoaded() {
        if (sCachedCurrentSlot == null || sCachedAllSlots == null
            || (System.currentTimeMillis() - sLastCacheUpdate > CACHE_TTL_MS)) {
            loadSlotDataFromDisk();
        }
    }

    /**
     * Executes root shell sequence to switch slots for all apps with .data_profiles,
     * optionally running clear RAM/Cache (ESC login parity).
     */
    private static boolean executeSlotSwitch(@NonNull String targetSlot, boolean clearCache) {
        StringBuilder script = new StringBuilder();
        script.append("TARGET=").append(targetSlot).append("\n");
        script.append("PREF='/data/data/com.ery.ziggy/shared_prefs/DataProfilesCache.xml'\n");
        script.append("CURR=$(grep 'name=\"currentProfile\"' \"$PREF\" 2>/dev/null | sed -e 's/.*<string name=\"currentProfile\">//' -e 's/<\\/string>.*//')\n");
        script.append("[ -z \"$CURR\" ] && [ -L '/data/data/com.kiwibrowser.browser/.data_profiles/.current' ] && CURR=$(readlink /data/data/com.kiwibrowser.browser/.data_profiles/.current 2>/dev/null)\n");
        script.append("[ -f \"$PREF\" ] && sed -i \"s/<string name=\\\"currentProfile\\\">.*<\\/string>/<string name=\\\"currentProfile\\\">$TARGET<\\/string>/\" \"$PREF\"\n");
        script.append("for pdir in $(find /data/data -maxdepth 2 -name .data_profiles 2>/dev/null); do\n");
        script.append("  [ -d \"$pdir\" ] || continue\n");
        script.append("  pkg=$(basename \"$(dirname \"$pdir\")\")\n");
        script.append("  pkgdir=$(dirname \"$pdir\")\n");
        script.append("  /system/bin/am force-stop \"$pkg\" >/dev/null 2>&1\n");
        script.append("  pkg_curr=\"\"\n");
        script.append("  if [ -L \"$pdir/.current\" ]; then\n");
        script.append("    pkg_curr=$(readlink \"$pdir/.current\")\n");
        script.append("  elif [ -f \"$pdir/.current\" ]; then\n");
        script.append("    pkg_curr=$(cat \"$pdir/.current\")\n");
        script.append("  fi\n");
        script.append("  [ -z \"$pkg_curr\" ] && pkg_curr=\"$CURR\"\n");
        script.append("  if [ -n \"$pkg_curr\" ] && [ \"$pkg_curr\" != \"$TARGET\" ]; then\n");
        script.append("    mkdir -p \"$pdir/$pkg_curr\"\n");
        script.append("    for item in \"$pkgdir\"/*; do\n");
        script.append("      [ -e \"$item\" ] || continue\n");
        script.append("      bname=$(basename \"$item\")\n");
        script.append("      [ \"$bname\" = \"lib\" ] && continue\n");
        script.append("      [ \"$bname\" = \".data_profiles\" ] && continue\n");
        script.append("      mv \"$item\" \"$pdir/$pkg_curr/\" 2>/dev/null\n");
        script.append("    done\n");
        script.append("    if [ -d \"$pdir/$TARGET\" ]; then\n");
        script.append("      for item in \"$pdir/$TARGET\"/*; do\n");
        script.append("        [ -e \"$item\" ] || continue\n");
        script.append("        mv \"$item\" \"$pkgdir/\" 2>/dev/null\n");
        script.append("      done\n");
        script.append("    else\n");
        script.append("      mkdir -p \"$pdir/$TARGET\"\n");
        script.append("    fi\n");
        script.append("    rm -f \"$pdir/.current\"\n");
        script.append("    ln -s \"$TARGET\" \"$pdir/.current\"\n");
        script.append("  fi\n");
        script.append("done\n");
        script.append("/system/bin/am broadcast -a android.appwidget.action.APPWIDGET_UPDATE -n com.ery.ziggy/com.myrefucek.FufuFu.MyDataProfileWidget >/dev/null 2>&1\n");

        if (clearCache) {
            // Parity dengan shortcut ESC di login dashboard menu
            script.append("sync\n");
            script.append("echo 3 > /proc/sys/vm/drop_caches\n");
            script.append("am kill-all >/dev/null 2>&1 || true\n");
            script.append("sysctl -w vm.shrink_memory=1 >/dev/null 2>&1 || true\n");
        }

        return executeRootScript(script.toString());
    }

    private static boolean executeRootScript(@NonNull String script) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/sh"});
            java.io.OutputStream os = p.getOutputStream();
            os.write(script.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();
            int code = p.waitFor();
            return code == 0;
        } catch (Throwable t) {
            try {
                Process p2 = Runtime.getRuntime().exec(new String[]{"su", "-c", script});
                return p2.waitFor() == 0;
            } catch (Throwable t2) {
                Log.e(TAG, "Root execution failed", t2);
                return false;
            }
        }
    }

    /**
     * Launch original Titanium Backup profile selection activity.
     */
    public static void openBackupAppPicker(@NonNull Context context) {
        if (!isRootAvailable()) {
            FusionToast.show(context, "⚠️ Akses root diperlukan untuk membuka .backup!");
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "/system/bin/am start -a ACTION_SELECT_DATA_PROFILE -n 'com.ery.ziggy/o.ḟ'"
                }).waitFor();
            } catch (Throwable t) {
                Log.e(TAG, "Failed launching .backup picker activity", t);
            }
        });
    }

    /**
     * Finds highest numeric slot index and returns next integer (e.g., if max is 99 -> returns 100).
     */
    public static int getNextNumericSlotIndex(@NonNull List<String> allSlots) {
        int max = -1;
        for (String s : allSlots) {
            if (s == null) continue;
            try {
                int val = Integer.parseInt(s.trim());
                if (val > max) {
                    max = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    /**
     * Formats integer to standard slot naming: 2-digit zero-padded for < 100 ("00", "01", ... "99"),
     * natural string for >= 100 ("100", "101", ...).
     */
    @NonNull
    public static String formatSlotName(int num) {
        if (num < 100) {
            return String.format(Locale.ROOT, "%02d", num);
        } else {
            return String.valueOf(num);
        }
    }

    /**
     * Automatically creates N next slots in sequence (e.g. 1, 10, or 20 slots).
     * Automatically skips any slot that already exists to prevent overwriting existing data.
     */
    public static void createSlotsAutoNext(@NonNull Context context, int count,
                                           @Nullable OnSlotCreatedCallback callback) {
        final Context appCtx = context.getApplicationContext();
        if (!isRootAvailable()) {
            if (callback != null) {
                callback.onResult(false, 0, 0, Collections.emptyList(),
                    "Akses root (su) diperlukan untuk membuat slot .backup!");
            }
            return;
        }

        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            List<String> existing = (sCachedAllSlots != null) ? new ArrayList<>(sCachedAllSlots) : new ArrayList<>();
            int nextIdx = getNextNumericSlotIndex(existing);

            List<String> toCreate = new ArrayList<>();
            int curr = nextIdx;
            while (toCreate.size() < count) {
                String candidate = formatSlotName(curr);
                if (!existing.contains(candidate)) {
                    toCreate.add(candidate);
                }
                curr++;
            }

            boolean ok = executeCreateSlots(toCreate);
            if (ok) {
                synchronized (FusionBackupSlotHelper.class) {
                    existing.addAll(toCreate);
                    Collections.sort(existing, SLOT_COMPARATOR);
                    sCachedAllSlots = existing;
                    sLastCacheUpdate = System.currentTimeMillis();
                }
            }

            final boolean success = ok;
            final List<String> createdList = toCreate;
            MAIN_HANDLER.post(() -> {
                if (callback != null) {
                    callback.onResult(success, success ? createdList.size() : 0, 0, createdList,
                        success ? null : "Gagal mengeksekusi shell pembuatan slot");
                }
            });
        });
    }

    /**
     * Creates a single custom named slot with duplicate protection.
     */
    public static void createCustomSlot(@NonNull Context context, @NonNull String slotName,
                                        @Nullable OnSlotCreatedCallback callback) {
        final Context appCtx = context.getApplicationContext();
        final String cleanName = slotName.trim();
        if (cleanName.isEmpty()) {
            if (callback != null) {
                callback.onResult(false, 0, 0, Collections.emptyList(), "Nama slot tidak boleh kosong!");
            }
            return;
        }

        if (!cleanName.matches("^[a-zA-Z0-9_\\-]+$")) {
            if (callback != null) {
                callback.onResult(false, 0, 0, Collections.emptyList(),
                    "Format karakter tidak valid! Hanya diperbolehkan huruf, angka, tanda hubung (-), dan garis bawah (_).");
            }
            return;
        }

        if (!isRootAvailable()) {
            if (callback != null) {
                callback.onResult(false, 0, 0, Collections.emptyList(),
                    "Akses root (su) diperlukan untuk membuat slot .backup!");
            }
            return;
        }

        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            List<String> existing = (sCachedAllSlots != null) ? new ArrayList<>(sCachedAllSlots) : new ArrayList<>();
            if (existing.contains(cleanName)) {
                MAIN_HANDLER.post(() -> {
                    if (callback != null) {
                        callback.onResult(false, 0, 1, Collections.emptyList(),
                            "Slot '" + cleanName + "' sudah terdaftar! Proteksi aktif.");
                    }
                });
                return;
            }

            List<String> toCreate = Collections.singletonList(cleanName);
            boolean ok = executeCreateSlots(toCreate);
            if (ok) {
                synchronized (FusionBackupSlotHelper.class) {
                    existing.add(cleanName);
                    Collections.sort(existing, SLOT_COMPARATOR);
                    sCachedAllSlots = existing;
                    sLastCacheUpdate = System.currentTimeMillis();
                }
            }

            final boolean success = ok;
            MAIN_HANDLER.post(() -> {
                if (callback != null) {
                    callback.onResult(success, success ? 1 : 0, 0, toCreate,
                        success ? null : "Gagal mengeksekusi pembuatan slot");
                }
            });
        });
    }

    /**
     * Executes root shell sequence to register slots into DataProfilesCache.xml and create
     * corresponding directories in all installed packages with .data_profiles.
     */
    private static boolean executeCreateSlots(@NonNull List<String> slotNames) {
        if (slotNames.isEmpty()) return true;

        StringBuilder sb = new StringBuilder();
        sb.append("PREF='/data/data/com.ery.ziggy/shared_prefs/DataProfilesCache.xml'\n");
        sb.append("if [ -f \"$PREF\" ]; then\n");
        sb.append("  P_OWNER=$(stat -c \"%u:%g\" \"$PREF\" 2>/dev/null || stat -c \"%u:%g\" \"$(dirname \"$PREF\")\")\n");
        for (String slot : slotNames) {
            sb.append("  grep -q 'name=\"profile.").append(slot).append("\"' \"$PREF\" 2>/dev/null || sed -i 's/<\\/map>/    <string name=\"profile.").append(slot).append("\"><\\/string>\\n<\\/map>/' \"$PREF\"\n");
        }
        sb.append("  chown \"$P_OWNER\" \"$PREF\" 2>/dev/null || true\n");
        sb.append("  chmod 660 \"$PREF\" 2>/dev/null || true\n");
        sb.append("fi\n");

        sb.append("for pdir in $(find /data/data -maxdepth 2 -name .data_profiles 2>/dev/null); do\n");
        sb.append("  [ -d \"$pdir\" ] || continue\n");
        sb.append("  dir_owner=$(stat -c \"%u:%g\" \"$pdir\" 2>/dev/null || stat -c \"%u:%g\" \"$(dirname \"$pdir\")\")\n");
        for (String slot : slotNames) {
            sb.append("  sdir=\"$pdir/").append(slot).append("\"\n");
            sb.append("  if [ ! -d \"$sdir\" ]; then\n");
            sb.append("    mkdir -p \"$sdir\" 2>/dev/null\n");
            sb.append("    chown \"$dir_owner\" \"$sdir\" 2>/dev/null || true\n");
            sb.append("    chmod 755 \"$sdir\" 2>/dev/null || true\n");
            sb.append("  fi\n");
        }
        sb.append("done\n");
        sb.append("/system/bin/am broadcast -a android.appwidget.action.APPWIDGET_UPDATE -n com.ery.ziggy/com.myrefucek.FufuFu.MyDataProfileWidget >/dev/null 2>&1 || true\n");
        sb.append("/system/bin/am force-stop com.ery.ziggy >/dev/null 2>&1 || am force-stop com.ery.ziggy >/dev/null 2>&1 || true\n");

        return executeRootScript(sb.toString());
    }

    /**
     * Material 3 Bottom Sheet Dialog to pick from all available slots.
     */
    public static void showSlotPicker(@NonNull Context context, @Nullable OnSlotPicked onPicked) {
        final boolean rootOk = isRootAvailable();

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        float density = context.getResources().getDisplayMetrics().density;
        int padH = Math.round(20 * density);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padH, 0, padH, Math.round(16 * density));

        root.addView(new BottomSheetDragHandleView(context), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Header: Title + Open in .backup button
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(context);
        title.setText("Pilih Slot Profile");
        M3.textAppearance(title, com.google.android.material.R.attr.textAppearanceTitleLarge);
        title.setTextColor(M3.onSurface(context));
        titleCol.addView(title);

        final String activeSlot = getCurrentSlotCached();
        TextView subtitle = new TextView(context);
        subtitle.setText("Slot aktif: " + activeSlot + " (.backup)");
        M3.textAppearance(subtitle, com.google.android.material.R.attr.textAppearanceBodyMedium);
        subtitle.setTextColor(M3.onSurfaceVariant(context));
        titleCol.addView(subtitle);

        headerRow.addView(titleCol);

        MaterialButton backupAppBtn = new MaterialButton(context, null,
            androidx.appcompat.R.attr.borderlessButtonStyle);
        backupAppBtn.setText("📱 .backup");
        backupAppBtn.setEnabled(rootOk);
        if (!rootOk) {
            backupAppBtn.setAlpha(0.38f);
        }
        backupAppBtn.setOnClickListener(v -> {
            dialog.dismiss();
            openBackupAppPicker(context);
        });
        headerRow.addView(backupAppBtn);

        root.addView(headerRow);

        // Auto Clear RAM & Cache (ESC Parity) Menu Card
        MaterialCardView clearCard = M3.clickableCard(context, false);
        LinearLayout.LayoutParams clearCardLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearCardLp.setMargins(0, Math.round(8 * density), 0, Math.round(8 * density));
        clearCard.setLayoutParams(clearCardLp);

        LinearLayout clearRow = new LinearLayout(context);
        clearRow.setOrientation(LinearLayout.HORIZONTAL);
        clearRow.setGravity(Gravity.CENTER_VERTICAL);
        clearRow.setPadding(Math.round(14 * density), Math.round(10 * density), Math.round(14 * density), Math.round(10 * density));

        TextView clearIcon = new TextView(context);
        clearIcon.setText("🧹");
        clearIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        clearRow.addView(clearIcon);

        LinearLayout clearCol = new LinearLayout(context);
        clearCol.setOrientation(LinearLayout.VERTICAL);
        clearCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        clearCol.setPadding(Math.round(12 * density), 0, Math.round(8 * density), 0);

        TextView clearTitle = new TextView(context);
        clearTitle.setText("Clear RAM & Cache (ESC)");
        M3.textAppearance(clearTitle, com.google.android.material.R.attr.textAppearanceTitleSmall);
        clearTitle.setTextColor(M3.onSurface(context));
        clearCol.addView(clearTitle);

        TextView clearDesc = new TextView(context);
        clearDesc.setText("Auto drop_caches & optimasi RAM saat berganti slot");
        M3.textAppearance(clearDesc, com.google.android.material.R.attr.textAppearanceBodySmall);
        clearDesc.setTextColor(M3.onSurfaceVariant(context));
        clearCol.addView(clearDesc);

        clearRow.addView(clearCol);

        MaterialSwitch clearSwitch = new MaterialSwitch(context);
        clearSwitch.setChecked(isAutoClearCacheEnabled(context));
        clearSwitch.setEnabled(rootOk);
        clearSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            setAutoClearCacheEnabled(context, isChecked);
            FusionToast.show(context, isChecked ? "🧹 Auto Clear Cache: AKTIF" : "Auto Clear Cache: NONAKTIF");
        });
        clearRow.addView(clearSwitch);
        clearCard.addView(clearRow);

        if (!rootOk) {
            clearCard.setAlpha(0.38f);
            clearCard.setEnabled(false);
        }
        root.addView(clearCard);

        // Non-root Warning Banner
        if (!rootOk) {
            MaterialCardView warnCard = M3.clickableCard(context, false);
            LinearLayout.LayoutParams warnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            warnLp.setMargins(0, 0, 0, Math.round(8 * density));
            warnCard.setLayoutParams(warnLp);

            LinearLayout warnRow = new LinearLayout(context);
            warnRow.setOrientation(LinearLayout.HORIZONTAL);
            warnRow.setGravity(Gravity.CENTER_VERTICAL);
            warnRow.setPadding(Math.round(12 * density), Math.round(8 * density), Math.round(12 * density), Math.round(8 * density));

            TextView warnText = new TextView(context);
            warnText.setText("⚠️ Akses root tidak terdeteksi. Fitur rotasi slot (.backup) wajib memiliki akses root (su).");
            M3.textAppearance(warnText, com.google.android.material.R.attr.textAppearanceBodySmall);
            warnText.setTextColor(M3.error(context));
            warnRow.addView(warnText);

            warnCard.addView(warnRow);
            root.addView(warnCard);
        }

        // Expandable Dropdown: List App yang masuk Profile Slot (.data_profiles)
        MaterialCardView appsDropdownCard = M3.clickableCard(context, false);
        LinearLayout.LayoutParams appsCardLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appsCardLp.setMargins(0, 0, 0, Math.round(8 * density));
        appsDropdownCard.setLayoutParams(appsCardLp);

        LinearLayout appsCardCol = new LinearLayout(context);
        appsCardCol.setOrientation(LinearLayout.VERTICAL);

        // Header Row (Clickable to toggle dropdown)
        LinearLayout appsHeaderRow = new LinearLayout(context);
        appsHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        appsHeaderRow.setGravity(Gravity.CENTER_VERTICAL);
        appsHeaderRow.setPadding(Math.round(14 * density), Math.round(10 * density), Math.round(14 * density), Math.round(10 * density));

        TextView appsIcon = new TextView(context);
        appsIcon.setText("📦");
        appsIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        appsHeaderRow.addView(appsIcon);

        LinearLayout appsTitleCol = new LinearLayout(context);
        appsTitleCol.setOrientation(LinearLayout.VERTICAL);
        appsTitleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        appsTitleCol.setPadding(Math.round(12 * density), 0, Math.round(8 * density), 0);

        TextView appsTitle = new TextView(context);
        appsTitle.setText("Aplikasi Terdaftar Profile");
        M3.textAppearance(appsTitle, com.google.android.material.R.attr.textAppearanceTitleSmall);
        appsTitle.setTextColor(M3.onSurface(context));
        appsTitleCol.addView(appsTitle);

        TextView appsSubtitle = new TextView(context);
        appsSubtitle.setText(rootOk ? "Memuat aplikasi terisolasi slot..." : "Akses root diperlukan");
        M3.textAppearance(appsSubtitle, com.google.android.material.R.attr.textAppearanceBodySmall);
        appsSubtitle.setTextColor(M3.onSurfaceVariant(context));
        appsTitleCol.addView(appsSubtitle);

        appsHeaderRow.addView(appsTitleCol);

        TextView appsChevron = new TextView(context);
        appsChevron.setText("▼");
        appsChevron.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        appsChevron.setTextColor(M3.onSurfaceVariant(context));
        appsChevron.setPadding(Math.round(8 * density), 0, Math.round(4 * density), 0);
        appsHeaderRow.addView(appsChevron);

        appsCardCol.addView(appsHeaderRow);

        // Dropdown expandable content container (initially GONE)
        LinearLayout appsDropdownContent = new LinearLayout(context);
        appsDropdownContent.setOrientation(LinearLayout.VERTICAL);
        appsDropdownContent.setPadding(Math.round(14 * density), 0, Math.round(14 * density), Math.round(10 * density));
        appsDropdownContent.setVisibility(View.GONE);

        View divApp = new View(context);
        divApp.setBackgroundColor(ColorUtils.setAlphaComponent(M3.onSurface(context), 25));
        LinearLayout.LayoutParams divAppLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, Math.round(1 * density));
        divAppLp.setMargins(0, 0, 0, Math.round(8 * density));
        appsDropdownContent.addView(divApp, divAppLp);

        LinearLayout appsItemsList = new LinearLayout(context);
        appsItemsList.setOrientation(LinearLayout.VERTICAL);
        appsDropdownContent.addView(appsItemsList);

        appsCardCol.addView(appsDropdownContent);
        appsDropdownCard.addView(appsCardCol);

        if (!rootOk) {
            appsDropdownCard.setAlpha(0.38f);
        } else {
            appsHeaderRow.setClickable(true);
            appsHeaderRow.setOnClickListener(v -> {
                boolean isExpanded = (appsDropdownContent.getVisibility() == View.VISIBLE);
                appsDropdownContent.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                appsChevron.setText(isExpanded ? "▼" : "▲");
            });
        }
        root.addView(appsDropdownCard);

        // Quick Creation Card: +1, +10, +20, Kustom
        MaterialCardView addCard = M3.clickableCard(context, false);
        LinearLayout.LayoutParams addCardLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addCardLp.setMargins(0, 0, 0, Math.round(10 * density));
        addCard.setLayoutParams(addCardLp);

        LinearLayout addCol = new LinearLayout(context);
        addCol.setOrientation(LinearLayout.VERTICAL);
        addCol.setPadding(Math.round(14 * density), Math.round(10 * density), Math.round(14 * density), Math.round(10 * density));

        TextView addTitle = new TextView(context);
        addTitle.setText("BUAT SLOT BARU (AUTO NEXT):");
        M3.textAppearance(addTitle, com.google.android.material.R.attr.textAppearanceLabelMedium);
        addTitle.setTextColor(M3.onSurfaceVariant(context));
        addTitle.setPadding(0, 0, 0, Math.round(8 * density));
        addCol.addView(addTitle);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER_VERTICAL);

        MaterialButton btnAdd1 = new MaterialButton(context);
        btnAdd1.setText("+1");
        btnAdd1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        btnAdd1.setPadding(Math.round(4 * density), 0, Math.round(4 * density), 0);
        btnAdd1.setEnabled(rootOk);

        MaterialButton btnAdd10 = new MaterialButton(context);
        btnAdd10.setText("+10");
        btnAdd10.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        btnAdd10.setPadding(Math.round(4 * density), 0, Math.round(4 * density), 0);
        btnAdd10.setEnabled(rootOk);

        MaterialButton btnAdd20 = new MaterialButton(context);
        btnAdd20.setText("+20");
        btnAdd20.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        btnAdd20.setPadding(Math.round(4 * density), 0, Math.round(4 * density), 0);
        btnAdd20.setEnabled(rootOk);

        MaterialButton btnAddCustom = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        btnAddCustom.setText("✏️ Kustom");
        btnAddCustom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        btnAddCustom.setPadding(Math.round(4 * density), 0, Math.round(4 * density), 0);
        btnAddCustom.setEnabled(rootOk);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, Math.round(38 * density), 1f);
        btnLp.setMargins(Math.round(2 * density), 0, Math.round(2 * density), 0);

        btnRow.addView(btnAdd1, btnLp);
        btnRow.addView(btnAdd10, btnLp);
        btnRow.addView(btnAdd20, btnLp);
        btnRow.addView(btnAddCustom, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Math.round(38 * density)));

        addCol.addView(btnRow);
        addCard.addView(addCol);
        if (!rootOk) {
            addCard.setAlpha(0.38f);
            addCard.setEnabled(false);
        }
        root.addView(addCard);

        // Search / Filter Input
        TextInputLayout searchLayout = new TextInputLayout(context);
        searchLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        searchLayout.setHint("Cari slot (misal: 01, 15, 99)...");
        searchLayout.setPadding(0, 0, 0, Math.round(10 * density));
        TextInputEditText searchInput = new TextInputEditText(searchLayout.getContext());
        searchInput.setSingleLine(true);
        searchLayout.addView(searchInput, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(searchLayout);

        // Grid Scroller
        ScrollView scroller = new ScrollView(context);
        scroller.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, Math.round(320 * density)));
        scroller.setOverScrollMode(View.OVER_SCROLL_NEVER);

        final GridLayout grid = new GridLayout(context);
        grid.setColumnCount(5);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroller.addView(grid);
        root.addView(scroller);

        dialog.setContentView(root);

        int primaryColor = MaterialColors.getColor(context, com.termux.shared.R.attr.termuxColorPrimary, 0xFFCDD6F4);
        int surfaceVariant = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, 0x1FFFFFFF);
        int onSurface = M3.onSurface(context);
        int chipMargin = Math.round(4 * density);
        int chipHeight = Math.round(44 * density);
        Typeface nerdFont = NerdFontSpans.typeface(context);

        final Runnable refreshAppsList = () -> {
            if (rootOk) {
                EXECUTOR.execute(() -> {
                    List<ProfileAppInfo> apps = scanProfileApps(context);
                    MAIN_HANDLER.post(() -> {
                        if (apps.isEmpty()) {
                            appsSubtitle.setText("0 aplikasi (.data_profiles belum dibuat)");
                        } else {
                            appsSubtitle.setText(apps.size() + " aplikasi terisolasi multi-slot");
                        }
                        renderProfileAppsList(context, appsItemsList, apps, dialog, density);
                    });
                });
            }
        };

        final Runnable refreshGrid = () -> {
            String currentFilter = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
            List<String> all = (sCachedAllSlots != null) ? sCachedAllSlots : Collections.singletonList(activeSlot);
            renderGridChips(context, grid, all, currentFilter, activeSlot, primaryColor, surfaceVariant,
                onSurface, chipMargin, chipHeight, nerdFont, dialog, onPicked, rootOk);
            subtitle.setText("Slot aktif: " + activeSlot + " (" + all.size() + " slot terdaftar)");
            refreshAppsList.run();
        };

        if (rootOk) {
            btnAdd1.setOnClickListener(v -> {
                createSlotsAutoNext(context, 1, (success, createdCount, skippedCount, createdNames, errorMsg) -> {
                    if (success && !createdNames.isEmpty()) {
                        FusionToast.show(context, "✔ Slot " + createdNames.get(0) + " berhasil dibuat!");
                        refreshGrid.run();
                    } else {
                        FusionToast.show(context, "⚠️ " + (errorMsg != null ? errorMsg : "Gagal membuat slot"));
                    }
                });
            });

            btnAdd10.setOnClickListener(v -> {
                List<String> current = (sCachedAllSlots != null) ? sCachedAllSlots : Collections.emptyList();
                int startIdx = getNextNumericSlotIndex(current);
                String startName = formatSlotName(startIdx);
                String endName = formatSlotName(startIdx + 9);
                new MaterialAlertDialogBuilder(context)
                    .setTitle("Buat 10 Slot Baru?")
                    .setMessage("Sistem akan otomatis membuat 10 slot berurutan:\n[" + startName + "] s/d [" + endName + "]\n\nProteksi aktif: slot lama 100% aman dan tidak akan tertimpa.")
                    .setPositiveButton("Buat", (d, which) -> {
                        createSlotsAutoNext(context, 10, (success, createdCount, skippedCount, createdNames, errorMsg) -> {
                            if (success && !createdNames.isEmpty()) {
                                FusionToast.show(context, "✔ Berhasil membuat 10 slot baru (" + createdNames.get(0) + " - " + createdNames.get(createdNames.size() - 1) + ")!");
                                refreshGrid.run();
                            } else {
                                FusionToast.show(context, "⚠️ " + (errorMsg != null ? errorMsg : "Gagal membuat slot"));
                            }
                        });
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            });

            btnAdd20.setOnClickListener(v -> {
                List<String> current = (sCachedAllSlots != null) ? sCachedAllSlots : Collections.emptyList();
                int startIdx = getNextNumericSlotIndex(current);
                String startName = formatSlotName(startIdx);
                String endName = formatSlotName(startIdx + 19);
                new MaterialAlertDialogBuilder(context)
                    .setTitle("Buat 20 Slot Baru?")
                    .setMessage("Sistem akan otomatis membuat 20 slot berurutan:\n[" + startName + "] s/d [" + endName + "]\n\nProteksi aktif: slot lama 100% aman dan tidak akan tertimpa.")
                    .setPositiveButton("Buat", (d, which) -> {
                        createSlotsAutoNext(context, 20, (success, createdCount, skippedCount, createdNames, errorMsg) -> {
                            if (success && !createdNames.isEmpty()) {
                                FusionToast.show(context, "✔ Berhasil membuat 20 slot baru (" + createdNames.get(0) + " - " + createdNames.get(createdNames.size() - 1) + ")!");
                                refreshGrid.run();
                            } else {
                                FusionToast.show(context, "⚠️ " + (errorMsg != null ? errorMsg : "Gagal membuat slot"));
                            }
                        });
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            });

            btnAddCustom.setOnClickListener(v -> {
                TextInputLayout customLayout = new TextInputLayout(context);
                customLayout.setHint("Nama / Nomor Slot (misal: 100, tuyul_01)");
                TextInputEditText customInput = new TextInputEditText(customLayout.getContext());
                customInput.setSingleLine(true);
                customLayout.addView(customInput);
                customLayout.setPadding(Math.round(20 * density), Math.round(12 * density), Math.round(20 * density), 0);

                new MaterialAlertDialogBuilder(context)
                    .setTitle("Buat Slot Kustom")
                    .setView(customLayout)
                    .setPositiveButton("Buat", (d, which) -> {
                        String name = (customInput.getText() != null) ? customInput.getText().toString().trim() : "";
                        createCustomSlot(context, name, (success, createdCount, skippedCount, createdNames, errorMsg) -> {
                            if (success) {
                                FusionToast.show(context, "✔ Slot '" + name + "' berhasil dibuat!");
                                refreshGrid.run();
                            } else {
                                FusionToast.show(context, "⚠️ " + (errorMsg != null ? errorMsg : "Gagal membuat slot"));
                            }
                        });
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            });
        }

        // Populate slots in background if cache empty
        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            MAIN_HANDLER.post(() -> {
                refreshGrid.run();

                searchInput.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        refreshGrid.run();
                    }
                });
            });
        });

        View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) {
            BottomSheetBehavior<?> behavior = dialog.getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        dialog.show();
    }

    private static void renderGridChips(@NonNull Context context, @NonNull GridLayout grid,
                                        @NonNull List<String> slots, @NonNull String filter,
                                        @NonNull String activeSlot, int primaryColor, int surfaceVariant,
                                        int onSurface, int chipMargin, int chipHeight,
                                        @Nullable Typeface nerdFont, @NonNull BottomSheetDialog dialog,
                                        @Nullable OnSlotPicked onPicked, boolean rootOk) {
        grid.removeAllViews();
        String needle = filter.toLowerCase(Locale.ROOT);

        for (String slot : slots) {
            if (!needle.isEmpty() && !slot.toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }

            final String slotTarget = slot;
            boolean isActive = slot.equals(activeSlot);

            TextView chip = new TextView(context);
            chip.setText(isActive ? ("✓ " + slot) : slot);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            if (nerdFont != null) chip.setTypeface(nerdFont);

            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            gd.setCornerRadius(chipHeight / 2.5f);

            if (!rootOk) {
                gd.setColor(ColorUtils.setAlphaComponent(surfaceVariant, 60));
                gd.setStroke(1, ColorUtils.setAlphaComponent(onSurface, 30));
                chip.setTextColor(ColorUtils.setAlphaComponent(onSurface, 90));
                chip.setEnabled(false);
            } else if (isActive) {
                gd.setColor(ColorUtils.setAlphaComponent(primaryColor, 220));
                gd.setStroke(0, Color.TRANSPARENT);
                chip.setTextColor(Color.BLACK);
                chip.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                gd.setColor(surfaceVariant);
                gd.setStroke(1, ColorUtils.setAlphaComponent(primaryColor, 80));
                chip.setTextColor(onSurface);
            }
            chip.setBackground(gd);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = chipHeight;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(chipMargin, chipMargin, chipMargin, chipMargin);

            if (rootOk) {
                chip.setOnClickListener(v -> {
                    dialog.dismiss();
                    switchToSlot(context, slotTarget, () -> {
                        if (onPicked != null) {
                            onPicked.onPicked(slotTarget);
                        }
                    });
                });
            } else {
                chip.setAlpha(0.38f);
            }

            grid.addView(chip, params);
        }
    }

    private static void renderProfileAppsList(@NonNull Context context,
                                              @NonNull LinearLayout container,
                                              @NonNull List<ProfileAppInfo> apps,
                                              @NonNull BottomSheetDialog dialog,
                                              float density) {
        container.removeAllViews();
        if (apps.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("Belum ada aplikasi yang memiliki .data_profiles");
            empty.setTextColor(M3.onSurfaceVariant(context));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            empty.setPadding(0, Math.round(4 * density), 0, Math.round(4 * density));
            container.addView(empty);
            return;
        }

        int onSurface = M3.onSurface(context);
        int onSurfaceVar = M3.onSurfaceVariant(context);
        int primary = MaterialColors.getColor(context, com.termux.shared.R.attr.termuxColorPrimary, 0xFFCDD6F4);

        for (int i = 0; i < apps.size(); i++) {
            ProfileAppInfo app = apps.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, Math.round(6 * density), 0, Math.round(6 * density));

            // App Icon
            ImageView iconView = new ImageView(context);
            if (app.icon != null) {
                iconView.setImageDrawable(app.icon);
            } else {
                iconView.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                Math.round(36 * density), Math.round(36 * density));
            iconLp.setMargins(0, 0, Math.round(10 * density), 0);
            row.addView(iconView, iconLp);

            // App Name & Package Info Col
            LinearLayout infoCol = new LinearLayout(context);
            infoCol.setOrientation(LinearLayout.VERTICAL);
            infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView nameView = new TextView(context);
            nameView.setText(app.appLabel);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            nameView.setTypeface(Typeface.DEFAULT_BOLD);
            nameView.setTextColor(onSurface);
            nameView.setSingleLine(true);
            infoCol.addView(nameView);

            // Sub row: Package name + slot badge
            LinearLayout badgeRow = new LinearLayout(context);
            badgeRow.setOrientation(LinearLayout.HORIZONTAL);
            badgeRow.setGravity(Gravity.CENTER_VERTICAL);
            badgeRow.setPadding(0, Math.round(2 * density), 0, 0);

            TextView slotBadge = new TextView(context);
            slotBadge.setText("Slot: " + app.currentSlot);
            slotBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            slotBadge.setTextColor(Color.BLACK);
            slotBadge.setTypeface(Typeface.DEFAULT_BOLD);
            slotBadge.setPadding(Math.round(6 * density), Math.round(1 * density), Math.round(6 * density), Math.round(1 * density));

            GradientDrawable slotBg = new GradientDrawable();
            slotBg.setShape(GradientDrawable.RECTANGLE);
            slotBg.setCornerRadius(Math.round(4 * density));
            slotBg.setColor(ColorUtils.setAlphaComponent(primary, 220));
            slotBadge.setBackground(slotBg);
            badgeRow.addView(slotBadge);

            TextView countBadge = new TextView(context);
            countBadge.setText(app.profileCount + " profil");
            countBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
            countBadge.setTextColor(onSurfaceVar);
            countBadge.setPadding(Math.round(6 * density), 0, 0, 0);
            badgeRow.addView(countBadge);

            infoCol.addView(badgeRow);
            row.addView(infoCol);

            // Action Button: Buka App
            if (app.isInstalled) {
                MaterialButton openBtn = new MaterialButton(context, null,
                    androidx.appcompat.R.attr.borderlessButtonStyle);
                openBtn.setText("🚀 Buka");
                openBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                openBtn.setPadding(Math.round(8 * density), 0, Math.round(8 * density), 0);
                openBtn.setOnClickListener(v -> {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialog.dismiss();
                        context.startActivity(launchIntent);
                    } else {
                        FusionToast.show(context, "Tidak dapat membuka " + app.appLabel);
                    }
                });
                row.addView(openBtn);

                row.setClickable(true);
                row.setOnClickListener(v -> {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialog.dismiss();
                        context.startActivity(launchIntent);
                    } else {
                        FusionToast.show(context, "Tidak dapat membuka " + app.appLabel);
                    }
                });
            }

            container.addView(row);

            if (i < apps.size() - 1) {
                View sep = new View(context);
                sep.setBackgroundColor(ColorUtils.setAlphaComponent(onSurface, 15));
                LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.round(0.75f * density));
                sepLp.setMargins(Math.round(46 * density), 0, 0, 0);
                container.addView(sep, sepLp);
            }
        }
    }
}
