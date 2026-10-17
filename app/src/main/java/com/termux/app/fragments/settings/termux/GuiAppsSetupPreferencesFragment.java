package com.termux.app.fragments.settings.termux;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;
import com.termux.app.fragments.settings.SegmentedPillPreference;
import com.termux.app.x11.ChrootDistro;

import com.termux.R;
import com.termux.app.activities.SettingsActivity;
import com.termux.app.fragments.settings.MaterialPreferenceFragment;
import com.termux.app.fragments.settings.SettingsLayoutUtils;
import com.termux.app.notice.AppNotice;
import com.termux.app.tour.TourEdition;
import com.termux.app.x11.DistroSetupStore;
import com.termux.app.x11.GuiAppsSetup;
import com.termux.app.x11.X11GpuProbe;
import com.termux.shared.interact.ShareUtils;
import com.termux.shared.termux.TermuxConstants;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "Get GUI apps": the ways to have an app with a window on this phone, and the one command
 * that takes whichever the user picked.
 *
 * <p>The screen installs nothing itself. It builds a command with {@link GuiAppsSetup}, puts it on
 * the clipboard and steps out of the way, and the user pastes it into the terminal.
 */
@Keep
public final class GuiAppsSetupPreferencesFragment extends MaterialPreferenceFragment {

    static final String KEY_HINT = "gui_apps_hint";
    static final String KEY_ROUTE = "gui_apps_route";
    static final String KEY_DISTRO = "gui_apps_distro";
    static final String KEY_CHROOT_CATEGORY = "gui_apps_chroot_category";
    static final String KEY_CHROOT_DETECT = "gui_apps_chroot_detect";
    static final String KEY_CHROOT_SELECTED = "gui_apps_chroot_selected";
    static final String KEY_CHROOT_CUSTOM_PATH = "gui_apps_chroot_custom_path";
    static final String KEY_CHROOT_CUSTOM_RUNNER = "gui_apps_chroot_custom_runner";
    static final String KEY_STARTERS_CATEGORY = "gui_apps_starters_category";
    static final String KEY_DESKTOP_CATEGORY = "gui_apps_desktop_category";
    static final String KEY_SELECTED_DE = ChrootDistro.KEY_SELECTED_DE;
    static final String KEY_COPY = "gui_apps_copy";
    static final String KEY_NIX_DOCS = "gui_apps_nix_docs";
    static final String KEY_GRAPHICS_CATEGORY = "gui_apps_graphics_category";
    static final String KEY_GPU = "x11_gpu";
    static final String KEY_GPU_COPY = "gui_apps_gpu_copy";

    /** How long the launcher is given to come forward before the notice is raised on it. */
    private static final long NOTICE_DELAY_MS = 350L;

    /** The GPU probe builds a throwaway GL context, so it is never run on the main thread. */
    private final ExecutorService probeExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gui-apps-gpu-probe");
        thread.setDaemon(true);
        return thread;
    });
    private final Handler probeHandler = new Handler(Looper.getMainLooper());
    /** The last probe's answer, kept so the copy row has something to build a command from. */
    @Nullable private X11GpuProbe.Result gpuResult;

    /** Where nix's "Read the docs" row sends the user; nix has no command for this screen to build. */
    private static final String NIX_DOCS_URL =
        "https://github.com/PickleHik3/termux-launcher/blob/dev/docs/en/Nix_Package_Management.md";

    /** The Intent that opens this screen from outside Settings. */
    @NonNull
    public static Intent intent(@NonNull Context context) {
        return SettingsActivity.createFragmentIntent(context,
            GuiAppsSetupPreferencesFragment.class, R.string.settings_gui_apps_title);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION);
        setPreferencesFromResource(R.xml.gui_apps_setup_preferences, rootKey);
        SettingsLayoutUtils.applyScreenLayout(this);

        TourEdition edition = TourEdition.of(context.getPackageName());
        List<GuiAppsSetup.Route> routes = GuiAppsSetup.routesFor(edition);
        SegmentedPillPreference route = findPreference(KEY_ROUTE);
        if (routes.isEmpty()) {
            // Nix: graphical apps are a nixpkgs and home.nix matter, not a command this screen
            // builds. Nothing here to pick or copy, so every row but the hint and the docs link
            // goes away.
            if (route != null) route.setVisible(false);
            Preference distro = findPreference(KEY_DISTRO);
            if (distro != null) distro.setVisible(false);
            Preference starters = findPreference(KEY_STARTERS_CATEGORY);
            if (starters != null) starters.setVisible(false);
            Preference desktopCat = findPreference(KEY_DESKTOP_CATEGORY);
            if (desktopCat != null) desktopCat.setVisible(false);
            Preference copy = findPreference(KEY_COPY);
            if (copy != null) copy.setVisible(false);
            Preference graphics = findPreference(KEY_GRAPHICS_CATEGORY);
            if (graphics != null) graphics.setVisible(false);
            Preference hint = findPreference(KEY_HINT);
            if (hint != null) hint.setSummary(R.string.settings_gui_apps_nix_card);
            Preference docs = findPreference(KEY_NIX_DOCS);
            if (docs != null) {
                docs.setVisible(true);
                docs.setOnPreferenceClickListener(preference -> {
                    openNixDocs(preference.getContext());
                    return true;
                });
            }
            return;
        }
        boolean rootAvailable = ChrootDistro.isRootAvailable();
        if (route != null) {
            route.setSegmentEnabled(GuiAppsSetup.Route.CHROOT.key, rootAvailable);
            if (!rootAvailable && GuiAppsSetup.Route.CHROOT.key.equals(route.getValue())) {
                route.setValue(GuiAppsSetup.Route.X11_REPO.key);
            }
        }

        if (routes.size() == 1) {
            // Nothing to choose: hide the row, and force the stored value to the one route this
            // edition offers, so a choice stored before the gate existed cannot build a command
            // for a route the screen no longer even shows.
            String only = routes.get(0).key;
            if (route != null) {
                route.setVisible(false);
                route.setValue(only);
            }
            applyRouteRows(only, edition);
            Preference hint = findPreference(KEY_HINT);
            if (hint != null) hint.setSummary(R.string.settings_gui_apps_intro_distro_only);
        } else if (route != null) {
            applyRouteRows(route.getValue(), edition);
            route.setOnPreferenceChangeListener((preference, value) -> {
                applyRouteRows(String.valueOf(value), edition);
                Context ctx = getContext();
                if (ctx != null) com.termux.app.x11.X11CliInstaller.forPrefix(ctx).writeWindowManagerScript();
                populateInstalledDesktops();
                return true;
            });
        }
        // The Linux to install changes which browser the command fetches, so the hints follow it.
        SegmentedPillPreference distro = findPreference(KEY_DISTRO);
        if (distro != null) {
            distro.setOnPreferenceChangeListener((preference, value) -> {
                SegmentedPillPreference current = findPreference(KEY_ROUTE);
                applyStarterHints(current == null ? null : current.getValue(),
                    String.valueOf(value));
                populateInstalledDesktops();
                Context ctx = getContext();
                if (ctx != null) com.termux.app.x11.X11CliInstaller.forPrefix(ctx).writeWindowManagerScript();
                return true;
            });
        }
        setupDesktopPreference();
        Preference copy = findPreference(KEY_COPY);
        if (copy != null) copy.setOnPreferenceClickListener(preference -> {
            copyCommand(preference.getContext());
            return true;
        });
        Preference gpuCopy = findPreference(KEY_GPU_COPY);
        if (gpuCopy != null) gpuCopy.setOnPreferenceClickListener(preference -> {
            copyGraphicsCommand(preference.getContext());
            return true;
        });
        probeGpu(context);
    }

    @Override
    public void onDestroy() {
        probeExecutor.shutdownNow();
        super.onDestroy();
    }

    /** nix's one extra row: there is no command here, only a page that explains its own way. */
    private void openNixDocs(@NonNull Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(NIX_DOCS_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // No browser to hand this to; nothing else sensible to do.
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) getActivity().setTitle(R.string.settings_gui_apps_title);
    }

    /** Everything on the screen that the chosen source decides, in one place. */
    private void applyRouteRows(@Nullable String route, @NonNull TourEdition edition) {
        boolean isChroot = GuiAppsSetup.Route.CHROOT.key.equals(route);
        applyDistroRow(route);
        applyChrootCategory(isChroot);
        applyBrowserRowVisibility(route, edition);
        Preference starters = findPreference(KEY_STARTERS_CATEGORY);
        if (starters != null) starters.setVisible(!isChroot);
        Preference copy = findPreference(KEY_COPY);
        if (copy != null) copy.setVisible(!isChroot);
        Preference hint = findPreference(KEY_HINT);
        if (hint != null) {
            if (isChroot) {
                boolean root = ChrootDistro.isRootAvailable();
                hint.setSummary(root ? R.string.settings_gui_apps_chroot_detect_summary
                    : R.string.settings_gui_apps_chroot_non_root_hint);
            } else {
                hint.setSummary(R.string.settings_gui_apps_hint);
            }
        }
        SegmentedPillPreference distro = findPreference(KEY_DISTRO);
        applyStarterHints(route, distro == null ? null : distro.getValue());
        populateInstalledDesktops();
    }

    private void setupDesktopPreference() {
        ListPreference dePref = findPreference(KEY_SELECTED_DE);
        if (dePref == null) return;
        dePref.setOnPreferenceChangeListener((pref, newVal) -> {
            String selectedId = String.valueOf(newVal);
            Context c = getContext();
            if (c != null) {
                android.content.SharedPreferences prefs = c.getSharedPreferences(
                    TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
                    Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_SELECTED_DE, selectedId).commit();
                com.termux.app.x11.X11CliInstaller.forPrefix(c).writeWindowManagerScript();
            }
            return true;
        });
        populateInstalledDesktops();
    }

    private void populateInstalledDesktops() {
        ListPreference dePref = findPreference(KEY_SELECTED_DE);
        if (dePref == null) return;
        Context ctx = getContext();
        if (ctx == null) return;

        probeExecutor.execute(() -> {
            List<ChrootDistro.DesktopEntry> des = ChrootDistro.detectInstalledDesktops(ctx);
            probeHandler.post(() -> {
                if (!isAdded()) return;
                List<String> entries = new java.util.ArrayList<>();
                List<String> values = new java.util.ArrayList<>();

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
                        String targetVal = values.contains("xfce4") ? "xfce4" : values.get(0);
                        dePref.setValue(targetVal);
                        currentVal = targetVal;
                        android.content.SharedPreferences prefs = ctx.getSharedPreferences(
                            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
                            Context.MODE_PRIVATE);
                        prefs.edit().putString(KEY_SELECTED_DE, currentVal).apply();
                    }
                }
            });
        });
    }

    private void applyChrootCategory(boolean visible) {
        Preference category = findPreference(KEY_CHROOT_CATEGORY);
        if (category != null) category.setVisible(visible);
        if (!visible) return;

        ListPreference selected = findPreference(KEY_CHROOT_SELECTED);
        Preference detect = findPreference(KEY_CHROOT_DETECT);

        refreshChrootList(selected);

        if (detect != null) {
            detect.setOnPreferenceClickListener(pref -> {
                Context ctx = getContext();
                if (ctx != null) {
                    AppNotice.show(ctx.getApplicationContext(), R.string.settings_gui_apps_chroot_detect_scanning);
                }
                probeExecutor.execute(() -> {
                    List<ChrootDistro.ChrootEnvironment> envs = ChrootDistro.scan();
                    probeHandler.post(() -> {
                        if (!isAdded()) return;
                        refreshChrootList(selected);
                        populateInstalledDesktops();
                        if (ctx != null) {
                            AppNotice.show(ctx.getApplicationContext(), "Scan complete. Found " + envs.size() + " environment(s).");
                        }
                    });
                });
                return true;
            });
        }

        if (selected != null) {
            applyCustomChrootVisibility(selected.getValue());
            selected.setOnPreferenceChangeListener((pref, val) -> {
                applyCustomChrootVisibility(String.valueOf(val));
                Context c = getContext();
                if (c != null) com.termux.app.x11.X11CliInstaller.forPrefix(c).writeWindowManagerScript();
                populateInstalledDesktops();
                return true;
            });
        }
    }

    private void refreshChrootList(@Nullable ListPreference selected) {
        if (selected == null) return;
        List<ChrootDistro.ChrootEnvironment> envs = ChrootDistro.scan();
        List<String> entries = new java.util.ArrayList<>();
        List<String> values = new java.util.ArrayList<>();

        for (ChrootDistro.ChrootEnvironment env : envs) {
            entries.add(env.name);
            values.add(env.id);
        }
        entries.add("Custom Rootfs Path");
        values.add(ChrootDistro.ID_CUSTOM);

        selected.setEntries(entries.toArray(new CharSequence[0]));
        selected.setEntryValues(values.toArray(new CharSequence[0]));

        if (selected.getValue() == null || selected.getValue().isEmpty()) {
            selected.setValue(values.get(0));
        }
    }

    private void applyCustomChrootVisibility(@Nullable String value) {
        boolean isCustom = ChrootDistro.ID_CUSTOM.equals(value);
        Preference customPath = findPreference(KEY_CHROOT_CUSTOM_PATH);
        Preference customRunner = findPreference(KEY_CHROOT_CUSTOM_RUNNER);
        if (customPath != null) {
            customPath.setVisible(isCustom);
            customPath.setOnPreferenceChangeListener((pref, val) -> {
                probeHandler.postDelayed(this::populateInstalledDesktops, 200);
                return true;
            });
        }
        if (customRunner != null) customRunner.setVisible(isCustom);
    }

    /**
     * The Starter Apps rows say which app each tick would fetch, because that is the difference
     * between the sources and between the distros: a browser on Debian is Firefox ESR and on
     * Ubuntu it is Falkon.
     */
    private void applyStarterHints(@Nullable String route, @Nullable String distro) {
        GuiAppsSetup.Route resolvedRoute = GuiAppsSetup.Route.of(route);
        GuiAppsSetup.Distro resolvedDistro = GuiAppsSetup.Distro.of(distro);
        for (GuiAppsSetup.StarterApp app : GuiAppsSetup.StarterApp.values()) {
            Preference row = findPreference("gui_apps_starter_" + app.key);
            // Only the rows the screen still shows; the editor and the terminal have none.
            if (row == null) continue;
            row.setSummary(GuiAppsSetup.starterAppName(resolvedRoute, resolvedDistro, app));
        }
    }

    /** What this phone's GPU can do for Linux apps, worked out off the main thread. */
    private void probeGpu(@NonNull Context context) {
        Context app = context.getApplicationContext();
        probeExecutor.execute(() -> {
            X11GpuProbe.Result result = X11GpuProbe.probe(app);
            probeHandler.post(() -> {
                if (!isAdded()) return;
                gpuResult = result;
                Preference gpu = findPreference(KEY_GPU);
                if (gpu == null) return;
                gpu.setSummary(result.recommended() == null
                    ? getString(R.string.settings_x11_gpu_none)
                    : getString(R.string.settings_x11_gpu_summary, result.headline()));
            });
        });
    }

    /**
     * The graphics command onto the clipboard, the same way the setup command goes: Settings
     * closes so the terminal is in front, and the notice follows it there.
     *
     * <p>Nothing happens while the probe is still running - the row has no command to give yet,
     * and a wrong one would install the wrong driver.
     */
    private void copyGraphicsCommand(@NonNull Context context) {
        X11GpuProbe.Result result = gpuResult;
        if (result == null) return;
        Context appContext = context.getApplicationContext();
        ShareUtils.copyTextToClipboard(appContext,
            getString(R.string.settings_gui_apps_gpu_clipboard_label), result.toCommand(), null);
        Activity activity = getActivity();
        if (activity != null) activity.finish();
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> AppNotice.show(appContext, R.string.settings_gui_apps_gpu_copied),
            NOTICE_DELAY_MS);
    }

    /** Which Linux to put inside is only a question for the route that puts one inside. */
    private void applyDistroRow(@Nullable String route) {
        Preference distro = findPreference(KEY_DISTRO);
        if (distro != null) {
            distro.setVisible(GuiAppsSetup.Route.of(route) == GuiAppsSetup.Route.DISTRO);
        }
    }

    /**
     * VAJ ships no browser at all, so its X11 route has nothing for the Browser row to tick; the
     * row returns once the route switches to a distro, where a real browser exists.
     */
    private void applyBrowserRowVisibility(@Nullable String route, @NonNull TourEdition edition) {
        Preference browser = findPreference("gui_apps_starter_" + GuiAppsSetup.StarterApp.BROWSER.key);
        if (browser == null) return;
        boolean hide = edition == TourEdition.VAJ
            && GuiAppsSetup.Route.of(route) == GuiAppsSetup.Route.X11_REPO;
        browser.setVisible(!hide);
    }

    /**
     * The one action: the command goes on the clipboard, Settings closes so the terminal is in
     * front, and the notice follows it there.
     *
     * <p>The notice is raised after the close rather than before it, and against the application
     * rather than this activity, so the pill lands on the launcher the user is being sent to
     * instead of flashing on a screen that is already going away.
     */
    private void copyCommand(@NonNull Context context) {
        String command = command();
        // The row that calls this is hidden whenever there is no command to build (nix); this is
        // just the same defence the rest of the class gives a stale or unreachable state.
        if (command == null) return;
        Context appContext = context.getApplicationContext();
        ShareUtils.copyTextToClipboard(appContext,
            getString(R.string.settings_gui_apps_clipboard_label), command, null);
        rememberTheOfferWasAnswered(appContext);
        Activity activity = getActivity();
        if (activity != null) activity.finish();
        // A handler of its own, not the fragment's: finish() above can run onDestroy() inside
        // the delay, and the fragment's handler is cleared there, which would drop the notice.
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> AppNotice.show(appContext, R.string.settings_gui_apps_copied), NOTICE_DELAY_MS);
    }

    /**
     * Copying is what answers the Display place's offer, so the place stops making it.
     *
     * <p>It is recorded here rather than when the screen opened: coming this far is the user
     * having what they came for, and someone who looked at the screen and backed out of it should
     * still be offered it. The situation is read fresh, so a change to it since the offer was made
     * is what gets remembered, and any later change is a new situation and is offered once more.
     */
    private void rememberTheOfferWasAnswered(@NonNull Context appContext) {
        try {
            DistroSetupStore.dismissCurrent(appContext);
        } catch (RuntimeException e) {
            // Nothing the user asked for depends on this; the command is already on the clipboard.
        }
    }

    /**
     * What the screen currently adds up to, or {@code null} when the edition offers no route at
     * all (nix) and so has nothing for this to build.
     */
    @Nullable
    String command() {
        Context context = getContext();
        TourEdition edition = TourEdition.of(context == null ? null : context.getPackageName());
        SegmentedPillPreference route = findPreference(KEY_ROUTE);
        GuiAppsSetup.Route resolvedRoute = routeFromKey(route == null ? null : route.getValue());
        if (resolvedRoute == null) resolvedRoute = GuiAppsSetup.defaultRoute(edition);
        if (resolvedRoute == null) return null;
        SegmentedPillPreference distro = findPreference(KEY_DISTRO);
        return GuiAppsSetup.command(resolvedRoute,
            GuiAppsSetup.Distro.of(distro == null ? null : distro.getValue()),
            starters(), edition);
    }

    /**
     * The route named by {@code key}, or {@code null} when it names none. Deliberately not
     * {@link GuiAppsSetup.Route#of}, whose fallback is a fixed route rather than this edition's
     * own {@link GuiAppsSetup#defaultRoute}.
     */
    @Nullable
    private static GuiAppsSetup.Route routeFromKey(@Nullable String key) {
        if (key == null) return null;
        for (GuiAppsSetup.Route candidate : GuiAppsSetup.Route.values()) {
            if (candidate.key.equals(key)) return candidate;
        }
        return null;
    }

    /** The ticked apps, read off their rows. */
    @NonNull
    private Set<GuiAppsSetup.StarterApp> starters() {
        Set<GuiAppsSetup.StarterApp> ticked = EnumSet.noneOf(GuiAppsSetup.StarterApp.class);
        for (GuiAppsSetup.StarterApp app : GuiAppsSetup.StarterApp.values()) {
            Preference row = findPreference("gui_apps_starter_" + app.key);
            if (row instanceof TwoStatePreference && ((TwoStatePreference) row).isChecked()) {
                ticked.add(app);
            }
        }
        return ticked;
    }
}
