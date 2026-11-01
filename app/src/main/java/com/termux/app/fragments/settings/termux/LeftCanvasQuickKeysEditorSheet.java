package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.graphics.Color;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.termux.R;
import com.termux.app.terminal.io.ExtraKeyDetailSheet;
import com.termux.app.terminal.io.ExtraKeysLayoutModel;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.font.NerdFontSpans;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet editor for Left Canvas (Kanvas Kiri) status bar quick keys.
 * Allows customizing up to 5 quick buttons (Search, Media Prev/Play/Next, Settings, etc.).
 */
public final class LeftCanvasQuickKeysEditorSheet {

    private LeftCanvasQuickKeysEditorSheet() {}

    @NonNull
    public static List<ExtraKeysLayoutModel.Key> parseLeftCanvasKeys(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            raw = TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS;
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
                TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS);
        }
        return keys;
    }

    public static void saveLeftCanvasKeys(@NonNull TermuxAppSharedPreferences preferences,
                                          @NonNull List<ExtraKeysLayoutModel.Key> keys) {
        preferences.setLeftCanvasQuickExtraKeys(ExtraKeysLayoutModel.serializeKeyList(keys));
    }

    @NonNull
    public static String resolveLeftCanvasGlyph(@NonNull String key) {
        if ("SEARCH".equalsIgnoreCase(key) || "FIND".equalsIgnoreCase(key)) {
            return "🔍";
        }
        if ("PREV".equalsIgnoreCase(key) || "MEDIA_PREV".equalsIgnoreCase(key) || "SKIP_PREVIOUS".equalsIgnoreCase(key)) {
            return "⏮";
        }
        if ("PLAY".equalsIgnoreCase(key) || "PAUSE".equalsIgnoreCase(key) || "PLAY_PAUSE".equalsIgnoreCase(key) || "MEDIA_PLAY".equalsIgnoreCase(key)) {
            return "⏯";
        }
        if ("NEXT".equalsIgnoreCase(key) || "MEDIA_NEXT".equalsIgnoreCase(key) || "SKIP_NEXT".equalsIgnoreCase(key)) {
            return "⏭";
        }
        if ("SETTINGS".equalsIgnoreCase(key) || "SETTING".equalsIgnoreCase(key) || "PREFS".equalsIgnoreCase(key)) {
            return "⚙";
        }
        return key;
    }

    public static void show(@NonNull Context context, @Nullable Runnable onUpdated) {
        TermuxAppSharedPreferences preferences = TermuxAppSharedPreferences.build(context, false);
        if (preferences == null) return;

        BottomSheetDialog sheet = new BottomSheetDialog(context);
        float density = context.getResources().getDisplayMetrics().density;
        int padH = Math.round(20 * density);
        int padV = Math.round(12 * density);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padH, 0, padH, Math.round(16 * density));

        root.addView(new BottomSheetDragHandleView(context), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(context);
        title.setText("Tombol Statbar Kanvas Kiri");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.WHITE));
        title.setPadding(0, 0, 0, Math.round(4 * density));
        root.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Maksimal 5 tombol aksi cepat di bilah status Kanvas Kiri. Tap tombol untuk ubah/hapus, atau gunakan tombol Tambah.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        subtitle.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY));
        subtitle.setPadding(0, 0, 0, Math.round(16 * density));
        root.addView(subtitle);

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout rowContainer = new LinearLayout(context);
        rowContainer.setOrientation(LinearLayout.HORIZONTAL);
        rowContainer.setGravity(Gravity.CENTER_VERTICAL);
        rowContainer.setPadding(0, padV, 0, padV);
        scroll.addView(rowContainer, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        LinearLayout actionsRow = new LinearLayout(context);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.END);
        actionsRow.setPadding(0, Math.round(12 * density), 0, 0);

        MaterialButton resetBtn = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        resetBtn.setText("Reset Default");
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resetParams.setMarginEnd(Math.round(8 * density));
        actionsRow.addView(resetBtn, resetParams);

        MaterialButton addBtn = new MaterialButton(context);
        addBtn.setText("+ Tambah Tombol");
        actionsRow.addView(addBtn);

        root.addView(actionsRow);

        sheet.setContentView(root);

        Typeface nerdFont;
        try {
            nerdFont = Typeface.createFromAsset(context.getAssets(), "fonts/NerdFont.ttf");
        } catch (Throwable t) {
            nerdFont = null;
        }

        int primary = MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, Color.CYAN);

        Runnable[] renderRowHolder = new Runnable[1];
        renderRowHolder[0] = () -> {
            rowContainer.removeAllViews();
            String raw = preferences.getLeftCanvasQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = parseLeftCanvasKeys(raw);

            final List<ExtraKeysLayoutModel.Key> currentKeys = keys;
            addBtn.setEnabled(currentKeys.size() < 5);

            for (int i = 0; i < currentKeys.size(); i++) {
                final ExtraKeysLayoutModel.Key key = currentKeys.get(i);
                final int index = i;

                TextView btn = new TextView(context);
                String label = key.display;
                if (label == null || label.isEmpty()) {
                    label = resolveLeftCanvasGlyph(key.key);
                }
                btn.setText(NerdFontSpans.span(context, label));
                if (nerdFont != null) btn.setTypeface(nerdFont);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                btn.setGravity(Gravity.CENTER);

                int strokeColor = ColorUtils.setAlphaComponent(primary, 140);
                int bgFill = ColorUtils.setAlphaComponent(primary, 40);
                int textColor = primary;

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(bgFill);
                gd.setStroke(Math.round(1 * density), strokeColor);
                gd.setCornerRadius(6 * density);
                btn.setBackground(gd);
                btn.setTextColor(textColor);

                int btnW = Math.round(44 * density);
                int btnH = Math.round(36 * density);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(btnW, btnH);
                lp.setMarginEnd(Math.round(8 * density));

                btn.setOnClickListener(v -> {
                    openKeySheet(context, key, currentKeys, preferences, () -> {
                        renderRowHolder[0].run();
                        if (onUpdated != null) onUpdated.run();
                    });
                });

                rowContainer.addView(btn, lp);
            }
        };

        renderRowHolder[0].run();

        resetBtn.setOnClickListener(v -> {
            preferences.setLeftCanvasQuickExtraKeys(TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS);
            renderRowHolder[0].run();
            if (onUpdated != null) onUpdated.run();
        });

        addBtn.setOnClickListener(v -> {
            String raw = preferences.getLeftCanvasQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = parseLeftCanvasKeys(raw);
            if (keys.size() >= 5) return;

            final String[] actionNames = new String[]{
                "🔍 Cari Aplikasi (Search & Focus Pill)",
                "⏮ Lagu Sebelumnya (Media Previous)",
                "⏯ Putar / Jeda Lagu (Media Play/Pause)",
                "⏭ Lagu Berikutnya (Media Next)",
                "⚙️ Pengaturan Kanvas Kiri (Settings)",
                "⌨️ Kustom (Ketik Tombol/Perintah Sendiri...)"
            };
            final String[] actionKeys = new String[]{
                "SEARCH",
                "PREV",
                "PLAY",
                "NEXT",
                "SETTINGS",
                "CUSTOM"
            };
            final String[] actionDisplays = new String[]{
                "🔍",
                "⏮",
                "⏯",
                "⏭",
                "⚙",
                ""
            };

            new MaterialAlertDialogBuilder(context)
                .setTitle("Pilih Aksi Tombol Kanvas Kiri")
                .setItems(actionNames, (dialog, which) -> {
                    if (which == actionNames.length - 1) {
                        promptCustomKey(context, customKey -> {
                            keys.add(new ExtraKeysLayoutModel.Key(customKey));
                            saveLeftCanvasKeys(preferences, keys);
                            renderRowHolder[0].run();
                            if (onUpdated != null) onUpdated.run();
                        });
                        return;
                    }

                    String pickedKey = actionKeys[which];
                    String pickedDisplay = actionDisplays[which];
                    keys.add(new ExtraKeysLayoutModel.Key(pickedKey, false, pickedDisplay, null));
                    saveLeftCanvasKeys(preferences, keys);
                    renderRowHolder[0].run();
                    if (onUpdated != null) onUpdated.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        });

        sheet.show();
        BottomSheetBehavior<?> b = sheet.getBehavior();
        if (b != null) b.setState(BottomSheetBehavior.STATE_EXPANDED);
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
                    saveLeftCanvasKeys(preferences, allKeys);
                }

                @Override
                public void onRemoveKey() {
                    allKeys.remove(key);
                    saveLeftCanvasKeys(preferences, allKeys);
                }
            });

        detailSheet.setOnDismissListener(() -> {
            saveLeftCanvasKeys(preferences, allKeys);
            onDone.run();
        });
        detailSheet.show();
    }

    private static void promptCustomKey(@NonNull Context context, @NonNull ValueCallback<String> onDone) {
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Math.round(20 * density), Math.round(8 * density), Math.round(20 * density), 0);

        TextInputLayout inputLayout = new TextInputLayout(context);
        inputLayout.setHint("Nama Kunci / Perintah");
        TextInputEditText editText = new TextInputEditText(context);
        editText.setSingleLine(true);
        inputLayout.addView(editText);
        layout.addView(inputLayout);

        new MaterialAlertDialogBuilder(context)
            .setTitle("Tambah Tombol Kustom")
            .setView(layout)
            .setPositiveButton("Tambah", (d, w) -> {
                String text = editText.getText() != null ? editText.getText().toString().trim() : "";
                if (!text.isEmpty()) {
                    onDone.onValue(text);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    public interface ValueCallback<T> {
        void onValue(T value);
    }
}
