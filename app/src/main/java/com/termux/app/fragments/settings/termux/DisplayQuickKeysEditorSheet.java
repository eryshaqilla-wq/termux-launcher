package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.R;
import com.termux.app.material.M3;
import com.termux.app.terminal.io.ExtraKeyDetailSheet;
import com.termux.app.terminal.io.ExtraKeysLayoutModel;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.font.NerdFontSpans;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet editor for X11 Display (right canvas) quick status bar keys.
 * Lets users customize, add, remove, and reconfigure buttons with complete parity to extra keys,
 * including native X11 mouse actions (left click, right click, middle click, scroll, etc.).
 */
public final class DisplayQuickKeysEditorSheet {

    private DisplayQuickKeysEditorSheet() {}

    @NonNull
    public static List<ExtraKeysLayoutModel.Key> parseDisplayKeys(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            raw = TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_QUICK_EXTRA_KEYS;
        }
        raw = raw.trim();
        List<ExtraKeysLayoutModel.Key> keys;
        if (raw.startsWith("[")) {
            keys = ExtraKeysLayoutModel.parseKeyList(raw);
        } else {
            keys = new ArrayList<>();
            String[] split = raw.split("[,;]");
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    keys.add(new ExtraKeysLayoutModel.Key(trimmed));
                    if (keys.size() >= 5) break;
                }
            }
        }
        if (keys.isEmpty()) {
            keys = ExtraKeysLayoutModel.parseKeyList(
                TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_QUICK_EXTRA_KEYS);
        }
        return keys;
    }

    public static void saveDisplayKeys(@NonNull TermuxAppSharedPreferences preferences,
                                       @NonNull List<ExtraKeysLayoutModel.Key> keys) {
        preferences.setX11QuickExtraKeys(ExtraKeysLayoutModel.serializeKeyList(keys));
    }

    @NonNull
    public static String resolveDisplayGlyph(@NonNull String key) {
        if ("FULLSCREEN".equalsIgnoreCase(key)) {
            return "\uf065";
        }
        if ("KEYBOARD".equalsIgnoreCase(key)) {
            return "\uDB82\uDD7B";
        }
        if ("tool:mouse.toggle".equalsIgnoreCase(key) || "MOUSE".equalsIgnoreCase(key)) {
            return "\uDB80\uDF7D";
        }
        if ("MOUSE_LEFT".equalsIgnoreCase(key) || "L_CLICK".equalsIgnoreCase(key) || "CLICK_LEFT".equalsIgnoreCase(key)) {
            return "\uDB80\uDF7D";
        }
        if ("MOUSE_RIGHT".equalsIgnoreCase(key) || "R_CLICK".equalsIgnoreCase(key) || "CLICK_RIGHT".equalsIgnoreCase(key)) {
            return "\uDB80\uDF7E";
        }
        if ("MOUSE_MIDDLE".equalsIgnoreCase(key) || "M_CLICK".equalsIgnoreCase(key) || "CLICK_MIDDLE".equalsIgnoreCase(key)) {
            return "\uDB80\uDF7F";
        }
        if ("MOUSE_DOUBLE".equalsIgnoreCase(key) || "DBL_CLICK".equalsIgnoreCase(key) || "DOUBLE_CLICK".equalsIgnoreCase(key)) {
            return "2x";
        }
        if ("SCROLL_UP".equalsIgnoreCase(key) || "WHEEL_UP".equalsIgnoreCase(key)) {
            return "\uf062";
        }
        if ("SCROLL_DOWN".equalsIgnoreCase(key) || "SCROLL_DN".equalsIgnoreCase(key) || "WHEEL_DOWN".equalsIgnoreCase(key)) {
            return "\uf063";
        }
        if ("BOOST".equalsIgnoreCase(key)) {
            return "\uf0e7";
        }
        if ("PASTE".equalsIgnoreCase(key)) {
            return "\uf0ea";
        }
        if ("DRAWER".equalsIgnoreCase(key)) {
            return "\uf0c9";
        }
        String alias = ExtraKeysConstants.CONTROL_CHARS_ALIASES.get(key);
        if (alias != null) return alias;
        alias = ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY.get(key);
        if (alias != null) return alias;
        return key;
    }

    public static void show(@NonNull Context context, @Nullable Runnable onUpdated) {
        TermuxAppSharedPreferences preferences = TermuxAppSharedPreferences.build(context, false);
        if (preferences == null) return;

        BottomSheetDialog sheet = new BottomSheetDialog(context);
        float density = context.getResources().getDisplayMetrics().density;
        int padH = Math.round(20 * density);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padH, 0, padH, Math.round(16 * density));

        root.addView(new BottomSheetDragHandleView(context), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(context);
        title.setText("Tombol Statbar Kanvas Kanan (Desktop X11)");
        M3.textAppearance(title, com.google.android.material.R.attr.textAppearanceTitleLarge);
        title.setTextColor(M3.onSurface(context));
        root.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Kustomisasi tombol aksi cepat di sisi kanan status bar Display (maksimal 5 tombol). Ketuk tombol untuk mengedit perintah, label, ikon, atau warnanya.");
        M3.textAppearance(subtitle, com.google.android.material.R.attr.textAppearanceBodyMedium);
        subtitle.setTextColor(M3.onSurfaceVariant(context));
        subtitle.setPadding(0, Math.round(4 * density), 0, Math.round(16 * density));
        root.addView(subtitle);

        // Preview container
        TextView previewLabel = new TextView(context);
        previewLabel.setText("Daftar Tombol Aktif");
        M3.textAppearance(previewLabel, com.google.android.material.R.attr.textAppearanceLabelMedium);
        previewLabel.setTextColor(M3.onSurfaceVariant(context));
        previewLabel.setPadding(0, 0, 0, Math.round(6 * density));
        root.addView(previewLabel);

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setClipToPadding(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout chipRow = new LinearLayout(context);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        chipRow.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(chipRow, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        // Action buttons container
        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER_VERTICAL);
        actionRow.setPadding(0, Math.round(18 * density), 0, 0);

        MaterialButton addBtn = new MaterialButton(context);
        addBtn.setText("Tambah Tombol");
        addBtn.setIconResource(R.drawable.ic_symbol_close);
        actionRow.addView(addBtn);

        View spacer = new View(context);
        actionRow.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1f));

        MaterialButton resetBtn = new MaterialButton(context, null,
            androidx.appcompat.R.attr.borderlessButtonStyle);
        resetBtn.setText("Reset Default");
        actionRow.addView(resetBtn);

        root.addView(actionRow);

        int primary = MaterialColors.getColor(context, com.termux.shared.R.attr.termuxColorPrimary, 0xFFCDD6F4);
        Typeface nerdFont = NerdFontSpans.typeface(context);

        final Runnable[] refreshChips = new Runnable[1];
        refreshChips[0] = () -> {
            chipRow.removeAllViews();
            String raw = preferences.getX11QuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = parseDisplayKeys(raw);

            final List<ExtraKeysLayoutModel.Key> currentKeys = keys;
            addBtn.setEnabled(currentKeys.size() < 5);

            for (int i = 0; i < currentKeys.size(); i++) {
                final ExtraKeysLayoutModel.Key key = currentKeys.get(i);

                TextView btn = new TextView(context);
                String label = key.display;
                if (label == null || label.isEmpty()) {
                    label = resolveDisplayGlyph(key.key);
                }
                btn.setText(NerdFontSpans.span(context, label));
                if (nerdFont != null) btn.setTypeface(nerdFont);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                btn.setGravity(Gravity.CENTER);

                int strokeColor = ColorUtils.setAlphaComponent(primary, 140);
                int bgFill = ColorUtils.setAlphaComponent(primary, 40);
                int textColor = primary;
                if (key.color != null) {
                    int roleBg = key.color.background(context);
                    int roleLabel = key.color.label(context);
                    strokeColor = ColorUtils.setAlphaComponent(roleBg, 200);
                    bgFill = ColorUtils.setAlphaComponent(roleBg, 90);
                    textColor = roleLabel;
                }
                btn.setTextColor(textColor);

                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.RECTANGLE);
                gd.setCornerRadius(Math.round(6 * density));
                gd.setColor(bgFill);
                gd.setStroke(Math.round(1 * density), strokeColor);
                btn.setBackground(gd);

                int padBtnH = Math.round(10 * density);
                int padBtnV = Math.round(8 * density);
                btn.setPadding(padBtnH, padBtnV, padBtnH, padBtnV);
                btn.setMinWidth(Math.round(36 * density));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(Math.round(8 * density));
                btn.setLayoutParams(lp);

                btn.setOnClickListener(v -> {
                    sheet.hide();
                    openKeySheet(context, key, currentKeys, preferences, () -> {
                        sheet.show();
                        refreshChips[0].run();
                        if (onUpdated != null) onUpdated.run();
                    });
                });

                chipRow.addView(btn);
            }
        };

        addBtn.setOnClickListener(v -> {
            String raw = preferences.getX11QuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = parseDisplayKeys(raw);
            if (keys.size() >= 5) return;

            final String[] actionNames = new String[]{
                "🖱️ Klik Kiri (Left Click)",
                "🖱️ Klik Kanan (Right Click / Context Menu)",
                "🖱️ Klik Tengah (Middle Click)",
                "⚡ Klik Ganda (Double Click)",
                " Scroll Roda Ke Atas (Wheel Up)",
                " Scroll Roda Ke Bawah (Wheel Down)",
                " Layar Penuh (Toggle Fullscreen)",
                " Keyboard Virtual (Toggle Soft Keyboard)",
                "󰍽 Mode Mouse (Toggle Touchpad)",
                " Bersihkan RAM (Drop Caches)",
                " Paste Clipboard ke Linux",
                "⌨️ Kustom (Ketik Tombol/Shortcut Sendiri...)"
            };
            final String[] actionKeys = new String[]{
                "MOUSE_LEFT",
                "MOUSE_RIGHT",
                "MOUSE_MIDDLE",
                "MOUSE_DOUBLE",
                "SCROLL_UP",
                "SCROLL_DOWN",
                "FULLSCREEN",
                "KEYBOARD",
                "MOUSE",
                "BOOST",
                "PASTE",
                "CUSTOM"
            };

            new MaterialAlertDialogBuilder(context)
                .setTitle("Pilih Aksi Display / Mouse")
                .setItems(actionNames, (dialog, which) -> {
                    String selectedAction = actionKeys[which];
                    if ("CUSTOM".equalsIgnoreCase(selectedAction)) {
                        ExtraKeysLayoutModel.Key newKey = new ExtraKeysLayoutModel.Key("ESC", false, "ESC", null);
                        keys.add(newKey);
                        saveDisplayKeys(preferences, keys);
                        sheet.hide();
                        openKeySheet(context, newKey, keys, preferences, () -> {
                            sheet.show();
                            refreshChips[0].run();
                            if (onUpdated != null) onUpdated.run();
                        });
                    } else {
                        ExtraKeysLayoutModel.Key newKey = new ExtraKeysLayoutModel.Key(selectedAction);
                        keys.add(newKey);
                        saveDisplayKeys(preferences, keys);
                        refreshChips[0].run();
                        if (onUpdated != null) onUpdated.run();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        });

        resetBtn.setOnClickListener(v -> {
            preferences.setX11QuickExtraKeys(
                TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_QUICK_EXTRA_KEYS);
            refreshChips[0].run();
            if (onUpdated != null) onUpdated.run();
        });

        refreshChips[0].run();

        sheet.setContentView(root);
        BottomSheetBehavior<?> behavior = sheet.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        sheet.show();
    }

    private static void openKeySheet(@NonNull Context context,
                                     @NonNull ExtraKeysLayoutModel.Key key,
                                     @NonNull List<ExtraKeysLayoutModel.Key> allKeys,
                                     @NonNull TermuxAppSharedPreferences preferences,
                                     @NonNull Runnable onDone) {
        ExtraKeysConstants.ExtraKeyDisplayMap displayMap =
            ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY;

        ExtraKeyDetailSheet detailSheet = new ExtraKeyDetailSheet(context, key, displayMap,
            new ExtraKeyDetailSheet.Host() {
                @Override
                public void onKeyChanged() {
                    saveDisplayKeys(preferences, allKeys);
                }

                @Override
                public void onRemoveKey() {
                    allKeys.remove(key);
                    saveDisplayKeys(preferences, allKeys);
                }
            });

        detailSheet.setOnDismissListener(() -> {
            saveDisplayKeys(preferences, allKeys);
            onDone.run();
        });
        detailSheet.show();
    }
}
