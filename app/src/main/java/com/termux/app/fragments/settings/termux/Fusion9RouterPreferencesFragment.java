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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.R;
import com.termux.app.fragments.settings.MaterialPreferenceFragment;
import com.termux.app.fragments.settings.SettingsLayoutUtils;
import com.termux.app.fragments.settings.StatusCardPreference;
import com.termux.webview.Fusion9RouterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Halaman Pengaturan Utama 9Router & AI Routing di Termux-Launcher.
 * Menyediakan kontrol on/off background service, status akun dinamis, autostart,
 * deteksi dinamis chroot/container di /data/local, pilihan target runtime,
 * auto-install, shortcut WebUI dashboard di Webview, dan integrasi Root / Non-Root.
 */
@Keep
public final class Fusion9RouterPreferencesFragment extends MaterialPreferenceFragment {

    private static final String PREF_STATUS_CARD = "pref_9router_status_card";
    private static final String PREF_TARGET_RUNTIME = "pref_9router_target_runtime";
    private static final String PREF_INSTALL = "pref_9router_install";
    private static final String PREF_SERVICE_SWITCH = "pref_9router_service_switch";
    private static final String PREF_AUTOSTART = "pref_9router_autostart";
    private static final String PREF_MITM_SWITCH = "pref_9router_mitm_switch";
    private static final String PREF_OPEN_WEBUI = "pref_9router_open_webui";
    private static final String PREF_SYNC_ACCOUNTS = "pref_9router_sync_accounts";
    private static final String PREF_AUTOCONFIG_CLIENTS = "pref_9router_autoconfig_clients";
    private static final String PREF_MODELS_INFO = "pref_9router_models_info";
    private static final String PREF_COPY_ENDPOINT = "pref_9router_copy_endpoint";
    private static final String PREF_COPY_APIKEY = "pref_9router_copy_apikey";

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

        // 0a. Target Lingkungan Runtime (Auto-Detected dari /data/local dan Native Termux)
        ListPreference targetPref = findPreference(PREF_TARGET_RUNTIME);
        if (targetPref != null) {
            List<Fusion9RouterHelper.TargetEnvironment> targets = Fusion9RouterHelper.scanAvailableTargets();
            CharSequence[] entries = new CharSequence[targets.size()];
            CharSequence[] entryValues = new CharSequence[targets.size()];
            for (int i = 0; i < targets.size(); i++) {
                entries[i] = targets.get(i).displayName;
                entryValues[i] = targets.get(i).id;
            }
            targetPref.setEntries(entries);
            targetPref.setEntryValues(entryValues);

            Fusion9RouterHelper.TargetEnvironment current = Fusion9RouterHelper.getSelectedTarget(context);
            targetPref.setValue(current.id);
            targetPref.setSummary(current.displayName);

            targetPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newId = (String) newValue;
                for (Fusion9RouterHelper.TargetEnvironment t : targets) {
                    if (t.id.equals(newId)) {
                        targetPref.setSummary(t.displayName);
                        Toast.makeText(context, "Target diubah: " + t.displayName, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                mMainHandler.post(this::refreshState);
                return true;
            });
        }

        // 0b. Tombol Pasang / Install 9Router di target terpilih
        Preference installPref = findPreference(PREF_INSTALL);
        if (installPref != null) {
            installPref.setOnPreferenceClickListener(preference -> {
                Fusion9RouterHelper.TargetEnvironment target = Fusion9RouterHelper.getSelectedTarget(context);
                Toast.makeText(context, "Memulai instalasi 9Router di " + target.displayName + "...", Toast.LENGTH_LONG).show();
                installPref.setEnabled(false);
                installPref.setSummary("Sedang mengunduh dan memasang paket via npm...");
                Fusion9RouterHelper.installRouter(context, target, () -> {
                    Toast.makeText(context, "Instalasi 9Router di " + target.displayName + " berhasil!", Toast.LENGTH_SHORT).show();
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
                    Fusion9RouterHelper.startService(context, () -> {
                        Toast.makeText(context, "9Router aktif di port 20128!", Toast.LENGTH_SHORT).show();
                        refreshState();
                    });
                } else {
                    Toast.makeText(context, "Menghentikan service 9Router...", Toast.LENGTH_SHORT).show();
                    Fusion9RouterHelper.stopService(context, () -> {
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
            if (!Fusion9RouterHelper.isRootAvailable(context)) {
                mitmSwitch.setEnabled(false);
                mitmSwitch.setSummary("Memerlukan Hak Akses Root (Chroot di /data/local)");
            }
        }

        // 3. Buka Dashboard WebUI di Webview
        Preference openWebUi = findPreference(PREF_OPEN_WEBUI);
        if (openWebUi != null) {
            openWebUi.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Mempersiapkan Dashboard 9Router...", Toast.LENGTH_SHORT).show();
                Fusion9RouterHelper.openDashboard(context, () -> {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().finish();
                    }
                });
                return true;
            });
        }

        // 4. Sinkronkan Akun Antigravity
        Preference syncAccounts = findPreference(PREF_SYNC_ACCOUNTS);
        if (syncAccounts != null) {
            syncAccounts.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Menyinkronkan akun Antigravity...", Toast.LENGTH_SHORT).show();
                Fusion9RouterHelper.syncAccounts(context, () -> {
                    int count = Fusion9RouterHelper.getAccountCount(context);
                    String msg = count > 0 ? (count + " Akun Antigravity berhasil disinkronkan!") : "Sinkronisasi akun selesai.";
                    mMainHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
                    refreshState();
                });
                return true;
            });
        }

        // 4b. Auto-Config Klien AI (OpenCode, OpenClaw, Hermes, Aider, dll)
        Preference autoConfigPref = findPreference(PREF_AUTOCONFIG_CLIENTS);
        if (autoConfigPref != null) {
            autoConfigPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Memindai klien AI terpasang...", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    final Fusion9RouterHelper.AutoConfigData data = Fusion9RouterHelper.getAutoConfigData(context);
                    mMainHandler.post(() -> {
                        if (data == null || data.clients.isEmpty()) {
                            new MaterialAlertDialogBuilder(context)
                                .setTitle("Tidak Ada Klien Terdeteksi")
                                .setMessage("Belum ada klien AI (OpenCode, OpenClaw, Aider, dll) yang terpasang di target lingkungan ini.\n\nPasang OpenCode terlebih dahulu untuk menghubungkannya ke 9Router.")
                                .setPositiveButton("OK", null)
                                .show();
                            return;
                        }

                        // Hanya tampilkan klien yang terpasang
                        final List<Fusion9RouterHelper.ClientConfigInfo> clients = data.clients;
                        final CharSequence[] items = new CharSequence[clients.size()];
                        final boolean[] checkedItems = new boolean[clients.size()];
                        final boolean[] selectedResults = new boolean[clients.size()];

                        for (int i = 0; i < clients.size(); i++) {
                            Fusion9RouterHelper.ClientConfigInfo c = clients.get(i);
                            String statusBadge = c.isConfigured ? "[🟢 Terhubung] " : "[⚪ Belum Terhubung] ";
                            items[i] = statusBadge + c.name + " (" + c.version + ")";
                            checkedItems[i] = c.isConfigured;
                            selectedResults[i] = c.isConfigured;
                        }

                        new MaterialAlertDialogBuilder(context)
                            .setTitle("Klien AI • Model: " + data.defaultModel)
                            .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                                selectedResults[which] = isChecked;
                            })
                            .setPositiveButton("Terapkan", (dialog, which) -> {
                                List<String> toApply = new ArrayList<>();
                                for (int i = 0; i < selectedResults.length; i++) {
                                    if (selectedResults[i]) {
                                        toApply.add(clients.get(i).id);
                                    }
                                }
                                if (toApply.isEmpty()) {
                                    Toast.makeText(context, "Tidak ada klien yang dipilih.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(context, "Menerapkan konfigurasi 9Router...", Toast.LENGTH_SHORT).show();
                                Fusion9RouterHelper.applyAutoConfig(context, toApply, () -> {
                                    Toast.makeText(context, "Konfigurasi 9Router berhasil diterapkan ke " + toApply.size() + " klien!", Toast.LENGTH_LONG).show();
                                    refreshState();
                                });
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                    });
                }).start();
                return true;
            });
        }

        // 4c. Info & Penggantian Model 9Router
        Preference modelsPref = findPreference(PREF_MODELS_INFO);
        if (modelsPref != null) {
            modelsPref.setOnPreferenceClickListener(preference -> {
                Toast.makeText(context, "Memuat daftar model...", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    final Fusion9RouterHelper.AutoConfigData data = Fusion9RouterHelper.getAutoConfigData(context);
                    final int accCount = Fusion9RouterHelper.getAccountCount(context);
                    final String accLabel = accCount > 0 ? (accCount + " akun") : "multi-akun";
                    mMainHandler.post(() -> {
                        if (data == null || data.models.isEmpty()) {
                            Toast.makeText(context, "Gagal memuat model dari 9Router", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final List<String> models = data.models;
                        final CharSequence[] items = new CharSequence[models.size()];
                        int selectedIdx = 0;
                        String curModel = (data.defaultModel != null) ? data.defaultModel : "";

                        for (int i = 0; i < models.size(); i++) {
                            String m = models.get(i);
                            items[i] = m;
                            if (m.equalsIgnoreCase(curModel)) {
                                selectedIdx = i;
                            }
                        }

                        final int[] chosenIdx = new int[]{selectedIdx};
                        new MaterialAlertDialogBuilder(context)
                            .setTitle("Pilih Model 9Router (" + accLabel + ")")
                            .setSingleChoiceItems(items, selectedIdx, (dialog, which) -> {
                                chosenIdx[0] = which;
                            })
                            .setPositiveButton("Ganti Model", (dialog, which) -> {
                                String selectedModel = models.get(chosenIdx[0]);
                                Toast.makeText(context, "Mengganti model ke " + selectedModel + "...", Toast.LENGTH_SHORT).show();
                                Fusion9RouterHelper.applyModel(context, selectedModel, () -> {
                                    Toast.makeText(context, "Model aktif diubah ke: " + selectedModel, Toast.LENGTH_LONG).show();
                                    refreshState();
                                });
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                    });
                }).start();
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

        // 6. Salin 9Router API Key
        Preference copyApiKey = findPreference(PREF_COPY_APIKEY);
        if (copyApiKey != null) {
            copyApiKey.setOnPreferenceClickListener(preference -> {
                new Thread(() -> {
                    String key = Fusion9RouterHelper.getApiKey(context);
                    mMainHandler.post(() -> {
                        if (key != null && !key.isEmpty()) {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard != null) {
                                ClipData clip = ClipData.newPlainText("9Router API Key", key);
                                clipboard.setPrimaryClip(clip);
                                String preview = key.length() > 14 ? (key.substring(0, 10) + "...") : key;
                                Toast.makeText(context, "API Key disalin: " + preview, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "API Key belum tersedia di database 9Router", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
                return true;
            });
        }
    }

    /**
     * Memperbarui status kartu dan switch di latar belakang agar UI tetap responsif.
     */
    private void refreshState() {
        Context context = getContext();
        new Thread(() -> {
            final Fusion9RouterHelper.TargetEnvironment target = Fusion9RouterHelper.getSelectedTarget(context);
            final boolean isInstalled = target.isInstalled();
            final boolean isRunning = isInstalled && Fusion9RouterHelper.isServiceRunning();
            final boolean isMitm = isInstalled && Fusion9RouterHelper.isMitmRunning();
            final int accounts = Fusion9RouterHelper.getAccountCount(context);
            final String apiKey = Fusion9RouterHelper.getApiKey(context);
            final Fusion9RouterHelper.AutoConfigData autoConfigData = Fusion9RouterHelper.getAutoConfigData(context);
            final String defaultModel = autoConfigData != null && autoConfigData.defaultModel != null ? autoConfigData.defaultModel : "ag/gemini-3.8-flash-medium";

            final StringBuilder summary = new StringBuilder();
            if (!isInstalled) {
                summary.append("Status   : Belum Terpasang\n");
                summary.append("Target   : ").append(target.displayName).append("\n");
                summary.append("Panduan  : Tekan tombol pasang di bawah untuk instalasi otomatis.");
            } else if (isRunning) {
                summary.append("Engine   : 9Router Daemon (CJS On-Demand)\n");
                summary.append("Port API : 20128 (Aktif)\n");
                summary.append("MITM 443 : ").append(isMitm ? "Aktif (SSL Intercept)\n" : "Nonaktif\n");
                summary.append("Akun     : ").append(accounts > 0 ? (accounts + " Akun Terhubung\n") : "Belum Ada Akun\n");
                summary.append("Model    : ").append(defaultModel).append("\n");
                summary.append("Target   : ").append(target.displayName);
            } else {
                summary.append("Status   : Siaga (Hemat Daya / 0% RAM)\n");
                summary.append("Port API : 20128 (Nonaktif)\n");
                summary.append("Model    : ").append(defaultModel).append("\n");
                summary.append("Target   : ").append(target.displayName);
            }

            mMainHandler.post(() -> {
                mIsUpdatingUi = true;

                ListPreference targetPref = findPreference(PREF_TARGET_RUNTIME);
                if (targetPref != null) {
                    targetPref.setSummary(target.displayName);
                }

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
                        installPref.setTitle("Pasang 9Router");
                        installPref.setSummary("Pasang 9Router ke: " + target.displayName);
                    } else {
                        installPref.setVisible(false);
                    }
                }

                SwitchPreferenceCompat serviceSwitch = findPreference(PREF_SERVICE_SWITCH);
                if (serviceSwitch != null) {
                    serviceSwitch.setEnabled(isInstalled);
                    serviceSwitch.setChecked(isRunning);
                }

                SwitchPreferenceCompat mitmSwitch = findPreference(PREF_MITM_SWITCH);
                if (mitmSwitch != null) {
                    mitmSwitch.setEnabled(target.isRoot && isInstalled);
                    if (!target.isRoot) {
                        mitmSwitch.setSummary("Memerlukan Hak Akses Root (Chroot di /data/local)");
                    } else {
                        mitmSwitch.setSummary(R.string.settings_9router_mitm_summary);
                    }
                }

                Preference openWebUi = findPreference(PREF_OPEN_WEBUI);
                if (openWebUi != null) {
                    openWebUi.setEnabled(isInstalled);
                }

                Preference syncAccounts = findPreference(PREF_SYNC_ACCOUNTS);
                if (syncAccounts != null) {
                    syncAccounts.setEnabled(isInstalled);
                    if (accounts > 0) {
                        syncAccounts.setSummary("Perbarui dan sinkronkan " + accounts + " token akun dari .agy-accounts");
                    } else {
                        syncAccounts.setSummary(R.string.settings_9router_sync_accounts_summary);
                    }
                }

                Preference autoConfigPref = findPreference(PREF_AUTOCONFIG_CLIENTS);
                if (autoConfigPref != null) {
                    autoConfigPref.setEnabled(isInstalled);
                    if (autoConfigData == null || autoConfigData.clients.isEmpty()) {
                        autoConfigPref.setSummary("Tidak ada klien AI terdeteksi");
                    } else {
                        int confCount = 0;
                        for (Fusion9RouterHelper.ClientConfigInfo c : autoConfigData.clients) {
                            if (c.isConfigured) confCount++;
                        }
                        autoConfigPref.setSummary(confCount + "/" + autoConfigData.clients.size() + " klien AI terhubung • Ketuk untuk konfigurasi");
                    }
                }

                Preference modelsPref = findPreference(PREF_MODELS_INFO);
                if (modelsPref != null) {
                    modelsPref.setEnabled(isInstalled);
                    int variantCount = autoConfigData != null ? autoConfigData.models.size() : 0;
                    modelsPref.setSummary("Model Utama: " + defaultModel + " (" + variantCount + " varian tersedia)");
                }

                Preference copyApiKeyPref = findPreference(PREF_COPY_APIKEY);
                if (copyApiKeyPref != null) {
                    copyApiKeyPref.setEnabled(isInstalled);
                    if (apiKey != null && !apiKey.isEmpty()) {
                        String masked = apiKey.length() > 14 ? (apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4)) : apiKey;
                        copyApiKeyPref.setSummary(masked + " (Ketuk untuk menyalin)");
                    } else {
                        copyApiKeyPref.setSummary(R.string.settings_9router_copy_apikey_summary);
                    }
                }

                mIsUpdatingUi = false;
            });
        }).start();
    }
}
