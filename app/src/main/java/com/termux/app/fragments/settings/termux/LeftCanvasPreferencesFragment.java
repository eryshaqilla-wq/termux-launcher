package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.termux.R;
import com.termux.app.fragments.settings.MaterialPreferenceFragment;
import com.termux.app.fragments.settings.SettingsLayoutUtils;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

/**
 * Preferences page for Left Canvas (Kanvas Kiri).
 * Allows toggling between upstream widget mode and custom fusion homescreen,
 * editing status bar quick extra keys, grid dimensions, and swipe-up gestures.
 */
@Keep
public final class LeftCanvasPreferencesFragment extends MaterialPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        setPreferencesFromResource(R.xml.left_canvas_preferences, rootKey);
        SettingsLayoutUtils.applyScreenLayout(this);

        Preference editKeys = findPreference("edit_left_canvas_quick_extra_keys");
        if (editKeys != null) {
            editKeys.setOnPreferenceClickListener(preference -> {
                Context ctx = getContext();
                if (ctx != null) {
                    LeftCanvasQuickKeysEditorSheet.show(ctx, () -> updateQuickKeysSummary(editKeys));
                }
                return true;
            });
            updateQuickKeysSummary(editKeys);
        }
    }

    private static void updateQuickKeysSummary(@NonNull Preference preference) {
        Context context = preference.getContext();
        TermuxAppSharedPreferences preferences = TermuxAppSharedPreferences.build(context, false);
        if (preferences == null) return;
        String raw = preferences.getLeftCanvasQuickExtraKeys();
        preference.setSummary("Kunci aktif: " + raw);
    }
}
