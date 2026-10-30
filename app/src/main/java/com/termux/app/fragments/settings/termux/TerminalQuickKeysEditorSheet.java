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
import com.termux.R;
import com.termux.app.material.M3;
import com.termux.app.terminal.io.ExtraKeyDetailSheet;
import com.termux.app.terminal.io.ExtraKeysLayoutModel;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.font.NerdFontSpans;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants;

import java.util.List;

/**
 * Bottom sheet editor for Terminal (center canvas) quick status bar keys.
 * Lets users customize, add, remove, and reconfigure buttons with complete parity to extra keys.
 */
public final class TerminalQuickKeysEditorSheet {

    private TerminalQuickKeysEditorSheet() {}

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
        title.setText("Tombol Statbar Kanvas Tengah");
        M3.textAppearance(title, com.google.android.material.R.attr.textAppearanceTitleLarge);
        title.setTextColor(M3.onSurface(context));
        root.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Kustomisasi tombol aksi cepat di sisi kanan status bar terminal (maksimal 5 tombol). Ketuk tombol untuk mengedit perintah, label, ikon, atau warnanya.");
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
        addBtn.setIconResource(R.drawable.ic_symbol_close); // will be rotated or plus
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
            String json = preferences.getTerminalQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = ExtraKeysLayoutModel.parseKeyList(json);
            if (keys.isEmpty()) {
                keys = ExtraKeysLayoutModel.parseKeyList(
                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_TERMINAL_QUICK_EXTRA_KEYS);
            }

            final List<ExtraKeysLayoutModel.Key> currentKeys = keys;
            addBtn.setEnabled(currentKeys.size() < 5);

            for (int i = 0; i < currentKeys.size(); i++) {
                final int idx = i;
                final ExtraKeysLayoutModel.Key key = currentKeys.get(i);

                TextView btn = new TextView(context);
                String label = key.display;
                if (label == null || label.isEmpty()) {
                    if (key.key.endsWith("\n")) {
                        String cmd = key.key.substring(0, key.key.length() - 1).trim();
                        label = cmd.length() <= 4 ? cmd.toUpperCase(java.util.Locale.ROOT) : ">_";
                    } else {
                        label = key.key;
                    }
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
            String json = preferences.getTerminalQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = ExtraKeysLayoutModel.parseKeyList(json);
            if (keys.isEmpty()) {
                keys = ExtraKeysLayoutModel.parseKeyList(
                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_TERMINAL_QUICK_EXTRA_KEYS);
            }
            if (keys.size() >= 5) return;

            ExtraKeysLayoutModel.Key newKey = new ExtraKeysLayoutModel.Key("clear\n", false, "CLR", null);
            keys.add(newKey);
            preferences.setTerminalQuickExtraKeys(ExtraKeysLayoutModel.serializeKeyList(keys));

            sheet.hide();
            openKeySheet(context, newKey, keys, preferences, () -> {
                sheet.show();
                refreshChips[0].run();
                if (onUpdated != null) onUpdated.run();
            });
        });

        resetBtn.setOnClickListener(v -> {
            preferences.setTerminalQuickExtraKeys(
                TermuxPreferenceConstants.TERMUX_APP.DEFAULT_TERMINAL_QUICK_EXTRA_KEYS);
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
                    preferences.setTerminalQuickExtraKeys(
                        ExtraKeysLayoutModel.serializeKeyList(allKeys));
                }

                @Override
                public void onRemoveKey() {
                    allKeys.remove(key);
                    preferences.setTerminalQuickExtraKeys(
                        ExtraKeysLayoutModel.serializeKeyList(allKeys));
                }
            });

        detailSheet.setOnDismissListener(() -> {
            preferences.setTerminalQuickExtraKeys(
                ExtraKeysLayoutModel.serializeKeyList(allKeys));
            onDone.run();
        });
        detailSheet.show();
    }
}
