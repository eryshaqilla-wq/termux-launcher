package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.app.fragments.settings.MaterialPreferenceFragment;
import com.termux.app.fragments.settings.SettingsLayoutUtils;
import com.termux.app.terminal.io.ExtraKeysLayoutModel;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants;

import java.util.List;

/**
 * Dedicated settings hub for managing status bar shortcuts across all 3 canvases:
 * 1. Left Canvas (Eso Browser & Widgets)
 * 2. Middle Canvas (Terminal Linux)
 * 3. Right Canvas (Display X11 Desktop)
 */
@Keep
public class CanvasQuickKeysPreferencesFragment extends MaterialPreferenceFragment {

    private TermuxAppSharedPreferences mPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        mPreferences = TermuxAppSharedPreferences.build(context, true);
        setPreferencesFromResource(R.xml.canvas_quick_keys_preferences, rootKey);
        SettingsLayoutUtils.applyScreenLayout(this);

        // --- Kanvas Kiri ---
        Preference leftEdit = findPreference("pref_left_canvas_keys");
        if (leftEdit != null) {
            leftEdit.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    LeftCanvasQuickKeysEditorSheet.show(ctx, this::updateSummaries);
                }
                return true;
            });
        }

        Preference leftReset = findPreference("pref_left_canvas_reset");
        if (leftReset != null) {
            leftReset.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    new MaterialAlertDialogBuilder(ctx)
                        .setTitle("Reset Shortcut Kanvas Kiri?")
                        .setMessage("Apakah Tuan ingin mengembalikan shortcut statbar Kanvas Kiri ke pengaturan default bawaan?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Reset", (d, which) -> {
                            if (mPreferences != null) {
                                mPreferences.setLeftCanvasQuickExtraKeys(
                                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS);
                            }
                            notifyActivityRefresh();
                            updateSummaries();
                        })
                        .show();
                }
                return true;
            });
        }

        // --- Kanvas Tengah ---
        Preference termEdit = findPreference("pref_terminal_keys");
        if (termEdit != null) {
            termEdit.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    TerminalQuickKeysEditorSheet.show(ctx, this::updateSummaries);
                }
                return true;
            });
        }

        Preference termReset = findPreference("pref_terminal_reset");
        if (termReset != null) {
            termReset.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    new MaterialAlertDialogBuilder(ctx)
                        .setTitle("Reset Shortcut Kanvas Tengah?")
                        .setMessage("Apakah Tuan ingin mengembalikan shortcut statbar Terminal (Kanvas Tengah) ke pengaturan default?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Reset", (d, which) -> {
                            if (mPreferences != null) {
                                mPreferences.setTerminalQuickExtraKeys(
                                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_TERMINAL_QUICK_EXTRA_KEYS);
                            }
                            notifyActivityRefresh();
                            updateSummaries();
                        })
                        .show();
                }
                return true;
            });
        }

        // --- Kanvas Kanan ---
        Preference displayEdit = findPreference("pref_display_keys");
        if (displayEdit != null) {
            displayEdit.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    DisplayQuickKeysEditorSheet.show(ctx, this::updateSummaries);
                }
                return true;
            });
        }

        Preference displayReset = findPreference("pref_display_reset");
        if (displayReset != null) {
            displayReset.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    new MaterialAlertDialogBuilder(ctx)
                        .setTitle("Reset Shortcut Kanvas Kanan?")
                        .setMessage("Apakah Tuan ingin mengembalikan tombol statbar Display X11 (Kanvas Kanan) ke pengaturan default?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Reset", (d, which) -> {
                            if (mPreferences != null) {
                                mPreferences.setX11QuickExtraKeys(
                                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_QUICK_EXTRA_KEYS);
                            }
                            notifyActivityRefresh();
                            updateSummaries();
                        })
                        .show();
                }
                return true;
            });
        }

        updateSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummaries();
    }

    private void updateSummaries() {
        if (mPreferences == null) return;

        // Left Canvas Summary
        Preference leftEdit = findPreference("pref_left_canvas_keys");
        if (leftEdit != null) {
            String json = mPreferences.getLeftCanvasQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = ExtraKeysLayoutModel.parseKeyList(json);
            if (keys.isEmpty()) {
                keys = ExtraKeysLayoutModel.parseKeyList(
                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS);
            }
            leftEdit.setSummary(formatKeySummary(keys));
        }

        // Terminal Canvas Summary
        Preference termEdit = findPreference("pref_terminal_keys");
        if (termEdit != null) {
            String json = mPreferences.getTerminalQuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = ExtraKeysLayoutModel.parseKeyList(json);
            if (keys.isEmpty()) {
                keys = ExtraKeysLayoutModel.parseKeyList(
                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_TERMINAL_QUICK_EXTRA_KEYS);
            }
            termEdit.setSummary(formatKeySummary(keys));
        }

        // Display Canvas Summary
        Preference displayEdit = findPreference("pref_display_keys");
        if (displayEdit != null) {
            String json = mPreferences.getX11QuickExtraKeys();
            List<ExtraKeysLayoutModel.Key> keys = ExtraKeysLayoutModel.parseKeyList(json);
            if (keys.isEmpty()) {
                keys = ExtraKeysLayoutModel.parseKeyList(
                    TermuxPreferenceConstants.TERMUX_APP.DEFAULT_X11_QUICK_EXTRA_KEYS);
            }
            displayEdit.setSummary(formatKeySummary(keys));
        }
    }

    @NonNull
    private String formatKeySummary(@NonNull List<ExtraKeysLayoutModel.Key> keys) {
        if (keys.isEmpty()) return "Tidak ada shortcut aktif (Kosong)";
        StringBuilder sb = new StringBuilder();
        sb.append(keys.size()).append(" shortcut aktif: ");
        int max = Math.min(5, keys.size());
        for (int i = 0; i < max; i++) {
            ExtraKeysLayoutModel.Key k = keys.get(i);
            String label = k.display != null && !k.display.isEmpty() ? k.display : k.key;
            if (label != null) {
                if (label.endsWith("\n")) label = label.substring(0, label.length() - 1).trim();
                sb.append("[").append(label).append("] ");
            }
        }
        if (keys.size() > 5) sb.append("...");
        return sb.toString().trim();
    }

    private void notifyActivityRefresh() {
        if (getActivity() instanceof TermuxActivity) {
            ((TermuxActivity) getActivity()).refreshAllQuickKeysClusters();
        }
    }
}
