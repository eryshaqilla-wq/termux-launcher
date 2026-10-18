package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Keep;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;

import com.termux.R;
import com.termux.app.x11.ChrootDistro;
import com.termux.app.x11.GuiAppsSetup;
import com.termux.app.x11.X11CliInstaller;
import com.termux.shared.termux.TermuxConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Linux apps. Reuses {@link X11DisplayPreferencesFragment}'s controller with dynamic
 * Window Manager and Desktop Environment discovery adapted to the active engine
 * (Droidspaces, Chroot/NetHunter, PRoot, or Native Host).
 */
@Keep
public class X11DisplayLinuxAppsPreferencesFragment extends X11DisplayPreferencesFragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "x11-linux-apps-pref");
        t.setDaemon(true);
        return t;
    });
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected int preferencesXml() {
        return com.termux.R.xml.x11_display_linux_apps_preferences;
    }

    @Override
    protected int titleRes() {
        return com.termux.R.string.settings_x11_sub_linux_apps_title;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setupDynamicDesktopPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupDynamicDesktopPreferences();
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void setupDynamicDesktopPreferences() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
            Context.MODE_PRIVATE);
        String route = prefs.getString("gui_apps_route", "x11");

        PreferenceCategory desktopCat = findPreference("x11_group_desktop_cat");
        ListPreference dePref = findPreference(ChrootDistro.KEY_SELECTED_DE);
        EditTextPreference wmPref = findPreference("x11_window_manager");

        String activeEngineTitle;
        if (GuiAppsSetup.Route.CHROOT.key.equals(route)) {
            ChrootDistro.ChrootEnvironment env = ChrootDistro.getActiveEnvironment(context);
            if (env != null) {
                activeEngineTitle = (env.runnerType == ChrootDistro.RunnerType.DROIDSPACES)
                    ? "Droidspaces: " + env.containerName
                    : env.name;
            } else {
                activeEngineTitle = "Chroot / NetHunter";
            }
        } else if (GuiAppsSetup.Route.DISTRO.key.equals(route)) {
            String distro = prefs.getString("gui_apps_distro", "debian");
            activeEngineTitle = "PRoot (" + distro + ")";
        } else {
            activeEngineTitle = "Native Termux";
        }

        if (desktopCat != null) {
            desktopCat.setTitle(getString(R.string.settings_x11_group_desktop) + " (" + activeEngineTitle + ")");
        }

        if (wmPref != null) {
            wmPref.setSummaryProvider(null);
            if (GuiAppsSetup.Route.CHROOT.key.equals(route) || GuiAppsSetup.Route.DISTRO.key.equals(route)) {
                wmPref.setSummary("Active engine: " + activeEngineTitle + " (Managed dynamically)");
                wmPref.setEnabled(false);
            } else {
                wmPref.setEnabled(true);
                wmPref.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
            }
        }

        if (dePref != null) {
            dePref.setOnPreferenceChangeListener((pref, newVal) -> {
                String selectedId = String.valueOf(newVal);
                prefs.edit().putString(ChrootDistro.KEY_SELECTED_DE, selectedId).apply();
                X11CliInstaller.forPrefix(context).writeWindowManagerScript();
                return true;
            });

            executor.execute(() -> {
                List<ChrootDistro.DesktopEntry> des = ChrootDistro.detectInstalledDesktops(context);
                handler.post(() -> {
                    if (!isAdded()) return;
                    List<String> entries = new ArrayList<>();
                    List<String> values = new ArrayList<>();

                    if (des.isEmpty()) {
                        entries.add(getString(R.string.settings_gui_apps_selected_de_none));
                        values.add("none");
                        dePref.setEntries(entries.toArray(new CharSequence[0]));
                        dePref.setEntryValues(values.toArray(new CharSequence[0]));
                        dePref.setValue("none");
                        dePref.setEnabled(false);
                    } else {
                        for (ChrootDistro.DesktopEntry de : des) {
                            entries.add(de.displayName);
                            values.add(de.id);
                        }
                        dePref.setEntries(entries.toArray(new CharSequence[0]));
                        dePref.setEntryValues(values.toArray(new CharSequence[0]));
                        dePref.setEnabled(true);

                        String currentVal = dePref.getValue();
                        boolean matched = false;
                        for (String val : values) {
                            if (val.equals(currentVal)) {
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            String targetVal = values.contains("bspwm") ? "bspwm"
                                : (values.contains("bspwm_kiosk") ? "bspwm_kiosk"
                                : (values.contains("openbox") ? "openbox"
                                : (values.contains("xfce4") ? "xfce4" : values.get(0))));
                            dePref.setValue(targetVal);
                            prefs.edit().putString(ChrootDistro.KEY_SELECTED_DE, targetVal).apply();
                        }
                    }
                });
            });
        }
    }
}
