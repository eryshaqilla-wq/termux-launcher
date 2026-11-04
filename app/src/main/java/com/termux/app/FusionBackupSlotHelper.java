package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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

    public interface OnSlotPicked {
        void onPicked(@NonNull String slot);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static String sCachedCurrentSlot = null;
    private static List<String> sCachedAllSlots = null;
    private static long sLastCacheUpdate = 0;
    private static final long CACHE_TTL_MS = 3000;

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
            Collections.sort(all);
        }
        sCachedAllSlots = all;
        sLastCacheUpdate = System.currentTimeMillis();
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
                    String msg = "🔄 Slot Aktif: " + targetSlot + (clearCache ? " ⚡ (RAM Bersih)" : "");
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
                    String msg = "◀ Slot Sebelumnya: " + target + (clearCache ? " ⚡ (RAM Bersih)" : "");
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
                    String msg = "Slot Berikutnya ▶: " + target + (clearCache ? " ⚡ (RAM Bersih)" : "");
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

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", script.toString()});
            int code = p.waitFor();
            return code == 0;
        } catch (Throwable t) {
            Log.e(TAG, "Root execution failed for slot switch", t);
            return false;
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
        clearIcon.setText("⚡");
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
            FusionToast.show(context, isChecked ? "⚡ Auto Clear Cache: AKTIF" : "Auto Clear Cache: NONAKTIF");
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
        int surfaceVariant = M3.color(context, com.google.android.material.R.attr.colorSurfaceVariant, 0x1FFFFFFF);
        int onSurface = M3.onSurface(context);
        int chipMargin = Math.round(4 * density);
        int chipHeight = Math.round(44 * density);
        Typeface nerdFont = NerdFontSpans.typeface(context);

        // Populate slots in background if cache empty
        EXECUTOR.execute(() -> {
            ensureCacheLoaded();
            final List<String> slots = (sCachedAllSlots != null && !sCachedAllSlots.isEmpty())
                ? sCachedAllSlots
                : Collections.singletonList(activeSlot);

            MAIN_HANDLER.post(() -> {
                renderGridChips(context, grid, slots, "", activeSlot, primaryColor, surfaceVariant, onSurface,
                    chipMargin, chipHeight, nerdFont, dialog, onPicked, rootOk);

                searchInput.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        String filter = s != null ? s.toString().trim() : "";
                        renderGridChips(context, grid, slots, filter, activeSlot, primaryColor, surfaceVariant,
                            onSurface, chipMargin, chipHeight, nerdFont, dialog, onPicked, rootOk);
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
}
