package com.termux.app.fragments.settings.termux;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.termux.R;
import com.termux.app.fragments.settings.MaterialPreferenceFragment;
import com.termux.app.fragments.settings.SettingsLayoutUtils;
import com.termux.app.fragments.settings.StatusCardPreference;
import com.termux.webview.Fusion9RouterHelper;

/**
 * Halaman Pengaturan Utama 9Router & AI Routing di Termux-Launcher.
 * Menyediakan kontrol on/off background service, status 5 akun, autostart,
 * deteksi instalasi, auto-install (Root Chroot / Non-Root Native Termux),
 * shortcut WebUI dashboard di Webview, dan integrasi Root / Non-Root.
 */
@Keep
public final class Fusion9RouterPreferencesFragment extends MaterialPreferenceFragment {

    private static final String PREF_STATUS_CARD = "pref_9router_status_card";
    private static final String PREF_INSTALL = "pref_9router_install";
    private static final String PREF_SERVICE_SWITCH = "pref_9router_service_switch";
    private static final String PREF_AUTOSTART = "pref_9router_autostart";
    private static final String PREF_MITM_SWITCH = "pref_9router_mitm_switch";
    private static final String PREF_OPEN_WEBUI = "pref_9router_open_webui";
    private static final String PREF_SYNC_ACCOUNTS = "pref_9router_sync_accounts";
    private static final String PREF_COPY_ENDPOINT = "pref_9router_copy_endpoint";

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private boolean mIsUpdatingUi = false;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fusion_9router_preferences, rootKey);
        SettingsLayoutUtils.applyScreenLayout(this);
        bindPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.settings_destination_9router);
        }
        refreshState();
    }

    private void bindPreferences() {
        Context context = getContext();
        if (context == null) return;

        // 0. Tombol Pasang / Install 9Router jika belum terpasang
        Preference installPref = findPreference(PREF_INSTALL);
        if (installPref != null) {
            installPref.setOnPreferenceClickListener(preference -> {
                boolean isRoot = Fusion9RouterHelper.isRootAvailable();
                String targetEnv = isRoot ? "Linux Chroot" : "Native Termux";
                Toast.makeText(context, "Memulai instalasi 9Router di " + targetEnv + "...", Toast.LENGTH_LONG).show();
                installPref.setEnabled(false);
                installPref.setSummary("Sedang mengunduh dan memasang paket via npm...");
                Fusion9RouterHelper.installRouter(() -> {
                    Toast.makeText(context, "Instalasi 9Router berhasil!", Toast.LENGTH_SHORT).show();
                    installPref.setEnabled(true);
                    refreshState();
                });
                return true;
            });
        }

        // 1. Switch Service ON / OFF
        SwitchPreferenceCompat serviceSwitch = findPreference(PREF_SERVICE_SWITCH);
        if (serviceSwitch != null) {
            serviceSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if (mIsUpdatingUi) return true;
                boolean shouldStart = (Boolean) newValue;
                if (shouldStart) {
                    Toast.makeText(context, "Menjalankan 9Router di background...", Toast.LENGTH_SHORT).show();
                    Fusion9RouterHelper.startService(() -> {
                        Toast.makeText(context, "9Router aktif di port 20128!", Toast.LENGTH_SHORT).show();
                        refreshState();
                    });
                } else {
                    Toast.makeText(context, "Menghentikan service 9Router...", Toast.LENGTH_SHORT).show();
                    Fusion9RouterHelper.stopService(() -> {
                        Toast.makeText(context, "9Router telah dihentikan.", Toast.LENGTH_SHORT).show();
                        refreshState();
                    });
                }
                return true;
            });
        }

        // 2. MITM Port 443 Switch
        SwitchPreferenceCompat mitmSwitch = findPreference(PREF_MITM_SWITCH);
        if (mitmSwitch != null) {
            if (!Fusion9RouterHelper.isRootAvailable()) {
                mitmSwitch.setEnabled(false);
                mitmSwitch.setSummary("Memerlukan Hak Akses Root (NetHunter / Droidspaces)");
            }
        }

        // 3. Buka Dashboard WebUI di Webview
        Preference openWebUi = findPreference(PREF_OPEN_WEBUI);
        if (openWebUi != null) {
            openWebUi.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Membuka Dashboard 9Router...", Toast.LENGTH_SHORT).show();
                Fusion9RouterHelper.openDashboard(context);
                return true;
            });
        }

        // 4. Sinkronkan 5 Akun Antigravity
        Preference syncAccounts = findPreference(PREF_SYNC_ACCOUNTS);
        if (syncAccounts != null) {
            syncAccounts.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Menyinkronkan akun Antigravity...", Toast.LENGTH_SHORT).show();
                Fusion9RouterHelper.syncAccounts(() -> {
                    Toast.makeText(context, "5 Akun Antigravity berhasil disinkronkan!", Toast.LENGTH_SHORT).show();
                    refreshState();
                });
                return true;
            });
        }

        // 5. Salin Endpoint OpenAI
        Preference copyEndpoint = findPreference(PREF_COPY_ENDPOINT);
        if (copyEndpoint != null) {
            copyEndpoint.setOnPreferenceClickListener(preference -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("9Router Endpoint", Fusion9RouterHelper.LOCAL_BASE_URL);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Endpoint disalin: " + Fusion9RouterHelper.LOCAL_BASE_URL, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }

    /**
     * Memperbarui status kartu dan switch di latar belakang agar UI tetap responsif.
     */
    private void refreshState() {
        new Thread(() -> {
            final boolean isInstalled = Fusion9RouterHelper.isInstalled();
            final boolean isRunning = isInstalled && Fusion9RouterHelper.isServiceRunning();
            final boolean isMitm = isInstalled && Fusion9RouterHelper.isMitmRunning();
            final int accounts = Fusion9RouterHelper.getAccountCount();
            final String runnerName = Fusion9RouterHelper.getRunnerDisplayName();
            final boolean isRoot = Fusion9RouterHelper.isRootAvailable();

            final StringBuilder summary = new StringBuilder();
            if (!isInstalled) {
                summary.append("Status   : Belum Terpasang\n");
                summary.append("Target   : ").append(isRoot ? "Linux Container (Root)" : "Native Termux (Non-Root)").append("\n");
                summary.append("Panduan  : Tekan tombol pasang di bawah untuk instalasi otomatis.");
            } else if (isRunning) {
                summary.append("Engine   : 9Router Daemon (CJS)\n");
                summary.append("Port API : 20128 (Aktif)\n");
                summary.append("MITM 443 : ").append(isMitm ? "Aktif (SSL Intercept)\n" : "Nonaktif\n");
                summary.append("Akun     : ").append(accounts).append(" Akun Terhubung\n");
                summary.append("Runner   : ").append(runnerName);
            } else {
                summary.append("Status   : Siaga (Hemat Daya / 0% RAM)\n");
                summary.append("Port API : 20128 (Nonaktif)\n");
                summary.append("Runner   : ").append(runnerName);
            }

            mMainHandler.post(() -> {
                mIsUpdatingUi = true;

                StatusCardPreference statusCard = findPreference(PREF_STATUS_CARD);
                if (statusCard != null) {
                    String statusLabel = !isInstalled ? "Belum Terpasang" : (isRunning ? "Layanan Aktif" : "Layanan Nonaktif");
                    statusCard.setStatus(statusLabel, isRunning);
                    statusCard.setSummary(summary.toString());
                }

                Preference installPref = findPreference(PREF_INSTALL);
                if (installPref != null) {
                    if (!isInstalled) {
                        installPref.setVisible(true);
                        installPref.setTitle("Pasang 9Router (" + (isRoot ? "Root Chroot" : "Native Termux") + ")");
                        installPref.setSummary("Tekan untuk mengunduh & memasang 9router secara otomatis");
                    } else {
                        installPref.setVisible(false);
                    }
                }

                SwitchPreferenceCompat serviceSwitch = findPreference(PREF_SERVICE_SWITCH);
                if (serviceSwitch != null) {
                    serviceSwitch.setEnabled(isInstalled);
                    serviceSwitch.setChecked(isRunning);
                }

                Preference openWebUi = findPreference(PREF_OPEN_WEBUI);
                if (openWebUi != null) {
                    openWebUi.setEnabled(isInstalled);
                }

                Preference syncAccounts = findPreference(PREF_SYNC_ACCOUNTS);
                if (syncAccounts != null) {
                    syncAccounts.setEnabled(isInstalled);
                }

                mIsUpdatingUi = false;
            });
        }).start();
    }
}
