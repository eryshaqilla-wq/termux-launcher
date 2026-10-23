package com.termux.app.x11;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants;

import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages root-level Chroot and Container environments (Kali NetHunter, Droidspaces, Custom).
 * Detects installed distributions under /data/local, maps their desktop entries to the app drawer,
 * and formats execution commands using their native root runners.
 */
public final class ChrootDistro {

    private static final String LOG_TAG = "ChrootDistro";

    public static final String KEY_CHROOT_SELECTED = "gui_apps_chroot_selected";
    public static final String KEY_CHROOT_CUSTOM_PATH = "gui_apps_chroot_custom_path";
    public static final String KEY_CHROOT_CUSTOM_RUNNER = "gui_apps_chroot_custom_runner";
    public static final String KEY_SELECTED_DE = "gui_apps_selected_de";

    public static final class DesktopDef {
        @NonNull public final String id;
        @NonNull public final String displayName;
        @NonNull public final List<String> candidateBinaries;

        public DesktopDef(@NonNull String id, @NonNull String displayName, @NonNull List<String> candidateBinaries) {
            this.id = id;
            this.displayName = displayName;
            this.candidateBinaries = candidateBinaries;
        }
    }

    public static final List<DesktopDef> SUPPORTED_DESKTOPS = Collections.unmodifiableList(Arrays.asList(
        new DesktopDef("bspwm", "BSPWM", Arrays.asList("bspwm", "bspwm-session")),
        new DesktopDef("openbox", "Openbox", Arrays.asList("openbox-session", "openbox")),
        new DesktopDef("xfce4", "XFCE 4", Arrays.asList("startxfce4", "xfce4-session")),
        new DesktopDef("gnome", "GNOME", Arrays.asList("gnome-session")),
        new DesktopDef("plasma", "KDE Plasma", Arrays.asList("startplasma-x11", "plasma-desktop")),
        new DesktopDef("mate", "MATE", Arrays.asList("mate-session")),
        new DesktopDef("lxde", "LXDE", Arrays.asList("startlxde", "lxsession")),
        new DesktopDef("lxqt", "LXQt", Arrays.asList("startlxqt")),
        new DesktopDef("cinnamon", "Cinnamon", Arrays.asList("cinnamon-session")),
        new DesktopDef("i3", "i3 Window Manager", Arrays.asList("i3")),
        new DesktopDef("awesome", "Awesome WM", Arrays.asList("awesome")),
        new DesktopDef("fluxbox", "Fluxbox", Arrays.asList("startfluxbox", "fluxbox"))
    ));

    @NonNull
    public static String getDesktopDisplayName(@Nullable String deId) {
        if (deId == null || deId.trim().isEmpty() || "none".equals(deId)) return "None";
        String normalized = ("bspwm_kiosk".equals(deId)) ? "bspwm" : (("openbox_kiosk".equals(deId)) ? "openbox" : deId);
        for (DesktopDef def : SUPPORTED_DESKTOPS) {
            if (def.id.equals(normalized)) return def.displayName;
        }
        return deId.toUpperCase(java.util.Locale.ROOT);
    }

    public static final class DesktopEntry {
        @NonNull public final String id;
        @NonNull public final String displayName;
        @NonNull public final String detectedBinary;
        @NonNull public final List<String> candidateBinaries;

        public DesktopEntry(@NonNull String id, @NonNull String displayName,
                            @NonNull String detectedBinary,
                            @NonNull List<String> candidateBinaries) {
            this.id = id;
            this.displayName = displayName;
            this.detectedBinary = detectedBinary;
            this.candidateBinaries = candidateBinaries;
        }

        @NonNull
        public String getChrootInnerCommand() {
            switch (id) {
                case "bspwm_kiosk":
                case "bspwm":
                    return "[ -f /root/.config/bspwm/settings.conf ] && (grep -v '^BAR_TOP=' /root/.config/bspwm/settings.conf > /root/.config/bspwm/settings.conf.tmp 2>/dev/null && echo 'BAR_TOP=on' >> /root/.config/bspwm/settings.conf.tmp && mv /root/.config/bspwm/settings.conf.tmp /root/.config/bspwm/settings.conf 2>/dev/null || true); "
                        + "[ -x /usr/local/bin/bspwm-session ] && exec /usr/local/bin/bspwm-session || (/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-run-session -- bspwm)";
                case "openbox_kiosk":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec " + detectedBinary;
                case "gnome":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; export XDG_SESSION_TYPE=x11; exec dbus-launch --exit-with-session gnome-session";
                case "plasma":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session " + detectedBinary;
                case "mate":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session mate-session";
                case "lxde":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session " + detectedBinary;
                case "lxqt":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session startlxqt";
                case "cinnamon":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session cinnamon-session";
                case "openbox":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec " + detectedBinary;
                case "i3":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec i3";
                case "awesome":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec awesome";
                case "fluxbox":
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec " + detectedBinary;
                case "xfce4":
                default:
                    return "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session " + detectedBinary;
            }
        }

        @NonNull
        public String getDroidspacesInnerCommand() {
            switch (id) {
                case "bspwm_kiosk":
                case "bspwm":
                    return "[ -f \"$HOME/.config/bspwm/settings.conf\" ] && (grep -v '^BAR_TOP=' \"$HOME/.config/bspwm/settings.conf\" > \"$HOME/.config/bspwm/settings.conf.tmp\" 2>/dev/null && echo 'BAR_TOP=on' >> \"$HOME/.config/bspwm/settings.conf.tmp\" && mv \"$HOME/.config/bspwm/settings.conf.tmp\" \"$HOME/.config/bspwm/settings.conf\" 2>/dev/null || true); "
                        + "[ -x /usr/local/bin/bspwm-session ] && exec /usr/local/bin/bspwm-session || exec dbus-run-session bspwm";
                case "openbox_kiosk":
                    return "exec " + detectedBinary;
                case "gnome":
                    return "export XDG_SESSION_TYPE=x11 && exec dbus-run-session gnome-session";
                case "plasma":
                    return "exec dbus-run-session " + detectedBinary;
                case "mate":
                    return "exec dbus-run-session mate-session";
                case "lxde":
                    return "exec dbus-run-session " + detectedBinary;
                case "lxqt":
                    return "exec dbus-run-session startlxqt";
                case "cinnamon":
                    return "exec dbus-run-session cinnamon-session";
                case "openbox":
                    return "exec " + detectedBinary;
                case "i3":
                    return "exec i3";
                case "awesome":
                    return "exec awesome";
                case "fluxbox":
                    return "exec " + detectedBinary;
                case "xfce4":
                default:
                    return "exec dbus-run-session " + ("startxfce4".equals(detectedBinary) ? "xfce4-session" : detectedBinary);
            }
        }

        @NonNull
        public String getHostCommand() {
            File dbusBin = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "dbus-launch");
            boolean hasDbus = dbusBin.canExecute();
            switch (id) {
                case "bspwm_kiosk":
                    return "(for k in $(seq 1 50); do bspc query -M >/dev/null 2>&1 && break; sleep 0.1; done; "
                        + "bspc config single_monocle true; "
                        + "bspc config gapless_monocle true; "
                        + "bspc config borderless_monocle true; "
                        + "bspc config window_gap 0; "
                        + "bspc config top_padding 0; "
                        + "for d in $(bspc query -D); do bspc desktop \"$d\" -l monocle; done) & bspwm";
                case "openbox_kiosk":
                    File rck = new File(TermuxConstants.TERMUX_PREFIX_DIR_PATH + "/etc/xdg/openbox/rc.xml");
                    return rck.exists() ? "openbox --config-file " + rck.getAbsolutePath() : "openbox";
                case "gnome":
                    return hasDbus ? "env XDG_SESSION_TYPE=x11 dbus-launch --exit-with-session gnome-session"
                                   : "env XDG_SESSION_TYPE=x11 gnome-session";
                case "plasma":
                    return hasDbus ? "dbus-launch --exit-with-session " + detectedBinary : detectedBinary;
                case "mate":
                    return hasDbus ? "dbus-launch --exit-with-session mate-session" : "mate-session";
                case "lxde":
                    return hasDbus ? "dbus-launch --exit-with-session " + detectedBinary : detectedBinary;
                case "lxqt":
                    return hasDbus ? "dbus-launch --exit-with-session startlxqt" : "startlxqt";
                case "cinnamon":
                    return hasDbus ? "dbus-launch --exit-with-session cinnamon-session" : "cinnamon-session";
                case "openbox":
                    File rc = new File(TermuxConstants.TERMUX_PREFIX_DIR_PATH + "/etc/xdg/openbox/rc.xml");
                    return rc.exists() ? "openbox --config-file " + rc.getAbsolutePath() : "openbox";
                case "i3":
                    return "i3";
                case "awesome":
                    return "awesome";
                case "bspwm":
                    return "[ -x /usr/local/bin/bspwm-session ] && exec /usr/local/bin/bspwm-session || "
                        + (hasDbus ? "exec dbus-run-session -- bspwm" : "exec bspwm");
                case "fluxbox":
                    return detectedBinary;
                case "xfce4":
                default:
                    return hasDbus ? "dbus-launch --exit-with-session " + detectedBinary : detectedBinary;
            }
        }
    }

    public static final String ID_NETHUNTER = "nethunter";
    public static final String ID_CUSTOM = "custom";

    public enum RunnerType {
        NETHUNTER,
        DROIDSPACES,
        CUSTOM
    }

    public static final class ChrootEnvironment {
        @NonNull public final String id;
        @NonNull public final String name;
        @NonNull public final File rootfs;
        @NonNull public final RunnerType runnerType;
        @NonNull public final String containerName;
        @NonNull public final String customRunner;

        public ChrootEnvironment(@NonNull String id, @NonNull String name, @NonNull File rootfs,
                                 @NonNull RunnerType runnerType, @NonNull String containerName,
                                 @NonNull String customRunner) {
            this.id = id;
            this.name = name;
            this.rootfs = rootfs;
            this.runnerType = runnerType;
            this.containerName = containerName;
            this.customRunner = customRunner;
        }

        @NonNull
        public ProotDistro.Container toContainer() {
            return new ProotDistro.Container(ProotDistro.Container.Kind.CHROOT,
                containerName.isEmpty() ? id : containerName,
                rootfs, "root", "/root", null, runnerType.name(), customRunner);
        }
    }

    private ChrootDistro() {}

    /**
     * Checks whether root access (su binary) is available on the device.
     */
    public static boolean isRootAvailable() {
        String[] paths = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            if (process.waitFor() == 0) return true;
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Scans /data/local for NetHunter, Droidspaces, and custom rootfs containers.
     */
    @NonNull
    public static List<ChrootEnvironment> scan() {
        List<ChrootEnvironment> envs = new ArrayList<>();

        // 1. Check Kali NetHunter
        File nh1 = new File("/data/local/nhsystem/kali-arm64");
        File nh2 = new File("/data/local/nhsystem/kalifs");
        if (nh1.isDirectory() || nh2.isDirectory()) {
            File root = nh1.isDirectory() ? nh1 : nh2;
            envs.add(new ChrootEnvironment(
                ID_NETHUNTER,
                "Kali NetHunter (" + root.getName() + ")",
                root,
                RunnerType.NETHUNTER,
                "kali-arm64",
                ""
            ));
        }

        // 2. Check Droidspaces
        File dsContainers = new File("/data/local/Droidspaces/Containers");
        if (dsContainers.isDirectory()) {
            File[] list = dsContainers.listFiles();
            if (list != null) {
                for (File dir : list) {
                    if (!dir.isDirectory()) continue;
                    File rootfs = new File(dir, "rootfs");
                    File effective = rootfs.isDirectory() ? rootfs : dir;
                    String cName = dir.getName();
                    envs.add(new ChrootEnvironment(
                        "droidspaces_" + cName,
                        "Droidspaces: " + cName,
                        effective,
                        RunnerType.DROIDSPACES,
                        cName,
                        ""
                    ));
                }
            }
        }

        // Also check if any active container is reported by droidspaces CLI via root
        if (isRootAvailable()) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c",
                    "ls -d /data/local/Droidspaces/Containers/*/rootfs /data/local/Droidspaces/Containers/* 2>/dev/null"});
                try (java.io.BufferedReader r = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        File f = new File(line);
                        String parentName = f.getName().equals("rootfs") && f.getParentFile() != null
                            ? f.getParentFile().getName() : f.getName();
                        String id = "droidspaces_" + parentName;
                        boolean exists = false;
                        for (ChrootEnvironment e : envs) {
                            if (e.id.equals(id)) { exists = true; break; }
                        }
                        if (!exists) {
                            envs.add(new ChrootEnvironment(
                                id,
                                "Droidspaces: " + parentName,
                                f,
                                RunnerType.DROIDSPACES,
                                parentName,
                                ""
                            ));
                        }
                    }
                }
                p.waitFor();
            } catch (Exception ignored) {}
        }

        return envs;
    }

    private static volatile long sLastBridgeSync = 0;
    private static volatile Runnable sOnSyncCompleted = null;

    /**
     * Registers a callback invoked whenever chroot bridge sync finishes.
     */
    public static void setOnSyncCompleted(@Nullable Runnable callback) {
        sOnSyncCompleted = callback;
    }

    /**
     * Checks whether SELinux is actively Enforcing on this device.
     */
    public static boolean isSelinuxEnforcing() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"getenforce"});
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                String line = r.readLine();
                if (line != null) {
                    return line.trim().equalsIgnoreCase("Enforcing");
                }
            }
        } catch (Exception ignored) {}
        try {
            File enforce = new File("/sys/fs/selinux/enforce");
            if (enforce.canRead()) {
                byte[] b = new byte[1];
                try (java.io.FileInputStream fis = new java.io.FileInputStream(enforce)) {
                    if (fis.read(b) > 0 && b[0] == '1') return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Auto-detects SELinux status on rooted devices.
     * If Enforcing: applies magiskpolicy dynamic allow rules for Termux app domains.
     * If Permissive (e.g. kernel-level permissive on Poco M3): bypasses magiskpolicy ("full power").
     */
    public static void applySelinuxPolicyIfEnforcing() {
        if (!isRootAvailable()) return;
        new Thread(() -> {
            try {
                if (isSelinuxEnforcing()) {
                    Logger.logInfo(LOG_TAG, "SELinux is Enforcing: applying magiskpolicy allow rules for Termux...");
                    String magiskPolicyCmd = "if command -v magiskpolicy >/dev/null 2>&1; then "
                        + "magiskpolicy --live \"allow { untrusted_app untrusted_app_27 untrusted_app_29 untrusted_app_30 untrusted_app_32 untrusted_app_all runas_app } system_data_file dir { read open search getattr }\" "
                        + "\"allow { untrusted_app untrusted_app_27 untrusted_app_29 untrusted_app_30 untrusted_app_32 untrusted_app_all runas_app } system_data_file file { read open getattr }\" 2>/dev/null || true; "
                        + "fi";
                    Runtime.getRuntime().exec(new String[]{"su", "-c", magiskPolicyCmd}).waitFor();
                } else {
                    Logger.logInfo(LOG_TAG, "SELinux is Permissive (full power): direct rootfs access enabled without magiskpolicy.");
                }
            } catch (Exception ignored) {}
        }, "selinux-policy-check").start();
    }

    private static String getSyncBridgeCommand() {
        return "sel=$(getenforce 2>/dev/null || cat /sys/fs/selinux/enforce 2>/dev/null); "
            + "if [ \"$sel\" = \"Enforcing\" ] || [ \"$sel\" = \"1\" ]; then "
            + "if command -v magiskpolicy >/dev/null 2>&1; then "
            + "magiskpolicy --live \"allow { untrusted_app untrusted_app_27 untrusted_app_29 untrusted_app_30 untrusted_app_32 untrusted_app_all runas_app } system_data_file dir { read open search getattr }\" "
            + "\"allow { untrusted_app untrusted_app_27 untrusted_app_29 untrusted_app_30 untrusted_app_32 untrusted_app_all runas_app } system_data_file file { read open getattr }\" 2>/dev/null || true; "
            + "fi; fi; "
            + "rm -rf /data/data/com.termux/files/usr/share/applications/chroot/kalifs 2>/dev/null; "
            + "icons_dir=\"/data/data/com.termux/files/usr/share/applications/chroot/icons\"; "
            + "mkdir -p \"$icons_dir\"; "
            + "for root in /data/local/nhsystem/kali-arm64 /data/local/nhsystem/kalifs /data/local/Droidspaces/Containers/*/rootfs; do "
            + "[ -L \"$root\" ] && continue; "
            + "if [ -d \"$root/usr/share/applications\" ]; then "
            + "cname=$(basename \"$(dirname \"$root\")\"); [ \"$cname\" = \"nhsystem\" ] && cname=$(basename \"$root\"); "
            + "out=\"/data/data/com.termux/files/usr/share/applications/chroot/$cname\"; "
            + "mkdir -p \"$out\"; "
            + "cp -u \"$root\"/usr/share/applications/*.desktop \"$out/\" 2>/dev/null || true; "
            + "for tdir in \"$root\"/usr/share/icons/hicolor/scalable/apps "
            + "\"$root\"/usr/share/icons/hicolor/256x256/apps "
            + "\"$root\"/usr/share/icons/hicolor/128x128/apps "
            + "\"$root\"/usr/share/icons/hicolor/64x64/apps "
            + "\"$root\"/usr/share/icons/hicolor/48x48/apps "
            + "\"$root\"/usr/share/icons/Papirus/128x128/apps "
            + "\"$root\"/usr/share/pixmaps; do "
            + "[ -d \"$tdir\" ] && cp -unL \"$tdir\"/* \"$icons_dir/\" 2>/dev/null || true; "
            + "done; "
            + "fi; done; "
            + "uid=$(stat -c '%u' /data/data/com.termux/files/home 2>/dev/null || id -u); "
            + "chown -R \"$uid:$uid\" \"/data/data/com.termux/files/usr/share/applications/chroot\" 2>/dev/null || true; "
            + "chmod -R 755 \"/data/data/com.termux/files/usr/share/applications/chroot\" 2>/dev/null || true; ";
    }

    /**
     * Synchronously synchronizes chroot .desktop files and icons into Termux's accessible prefix bridge directory.
     * Can be safely called from background loader threads.
     */
    public static void syncChrootBridgeBlocking() {
        if (!isRootAvailable()) return;
        try {
            String cmd = getSyncBridgeCommand();
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            int exit = p.waitFor();
            if (exit == 0 && sOnSyncCompleted != null) {
                sOnSyncCompleted.run();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Synchronizes chroot .desktop files and icons into Termux's accessible prefix bridge directory.
     */
    public static void syncChrootBridge() {
        syncChrootBridge(null);
    }

    /**
     * Synchronizes chroot .desktop files and icons asynchronously and invokes callback upon completion.
     */
    public static void syncChrootBridge(@Nullable Runnable onFinished) {
        if (!isRootAvailable()) {
            if (onFinished != null) onFinished.run();
            return;
        }
        new Thread(() -> {
            try {
                String cmd = getSyncBridgeCommand();
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                int exit = p.waitFor();
                if (exit == 0 && sOnSyncCompleted != null) {
                    sOnSyncCompleted.run();
                }
            } catch (Exception ignored) {
            } finally {
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        }, "chroot-bridge-sync").start();
    }

    /**
     * Returns installed chroot environments as ProotDistro.Container items for LinuxAppCatalog scanning.
     */
    @NonNull
    public static List<ProotDistro.Container> installed() {
        if (!isRootAvailable()) return Collections.emptyList();
        List<ProotDistro.Container> containers = new ArrayList<>();
        List<ChrootEnvironment> envs = scan();
        for (ChrootEnvironment env : envs) {
            containers.add(env.toContainer());
        }
        return containers;
    }

    /**
     * Resolves the active chroot environment according to user preferences.
     */
    @Nullable
    public static ChrootEnvironment getActiveEnvironment(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
            Context.MODE_PRIVATE);
        String selected = prefs.getString(KEY_CHROOT_SELECTED, ID_NETHUNTER);
        if (ID_CUSTOM.equals(selected)) {
            String path = prefs.getString(KEY_CHROOT_CUSTOM_PATH, "");
            String runner = prefs.getString(KEY_CHROOT_CUSTOM_RUNNER, "");
            if (path.isEmpty()) return null;
            return new ChrootEnvironment(
                ID_CUSTOM,
                "Custom Chroot (" + new File(path).getName() + ")",
                new File(path),
                RunnerType.CUSTOM,
                "custom",
                runner
            );
        }
        List<ChrootEnvironment> scanned = scan();
        for (ChrootEnvironment env : scanned) {
            if (env.id.equals(selected)) return env;
        }
        // Fallback to first available scanned environment if selected is not matched
        return scanned.isEmpty() ? null : scanned.get(0);
    }

    /**
     * Builds the runner shell line for an application inside a Chroot container.
     */
    @NonNull
    public static String buildRunner(@NonNull ProotDistro.Container container, @NonNull String command) {
        String type = container.runnerType != null ? container.runnerType : RunnerType.NETHUNTER.name();
        String rootPath = container.root.getAbsolutePath();

        if (RunnerType.DROIDSPACES.name().equals(type)) {
            String cName = container.name;
            writeDroidspacesAppScript(cName);
            return "su -c " + ProotDistro.singleQuote(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_APP_SCRIPT_NAME + " " + command);
        } else if (RunnerType.CUSTOM.name().equals(type) && container.customRunner != null
                && !container.customRunner.trim().isEmpty()) {
            return container.customRunner
                .replace("%ROOTFS%", rootPath)
                .replace("%COMMAND%", ProotDistro.singleQuote(command));
        }

        // Default: NetHunter chroot with mount namespace (-mm) and auto-mounted X11 & VirGL unix sockets
        writeChrootAppScript(rootPath);
        return "su -mm -c " + ProotDistro.singleQuote(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_APP_SCRIPT_NAME + " " + command);
    }

    /**
     * Builds the desktop startup command for the active Chroot environment (to run on Start display).
     */
    @NonNull
    public static String getDesktopCommand(@Nullable ChrootEnvironment env) {
        return getDesktopCommand(env, (DesktopEntry) null);
    }

    public static final String DROIDSPACES_DESKTOP_SCRIPT_NAME = "termux-droidspaces-desktop";

    @NonNull
    public static String getDroidspacesAutoDesktopCommand() {
        return "if [ -x /usr/bin/startxfce4 ] || [ -x /usr/bin/xfce4-session ]; then\n"
            + "    exec dbus-run-session xfce4-session\n"
            + "elif [ -x /usr/local/bin/bspwm-session ]; then\n"
            + "    exec /usr/local/bin/bspwm-session\n"
            + "elif [ -x /usr/bin/bspwm ]; then\n"
            + "    exec dbus-run-session bspwm\n"
            + "elif [ -x /usr/bin/mate-session ]; then\n"
            + "    exec dbus-run-session mate-session\n"
            + "elif [ -x /usr/bin/startlxde ]; then\n"
            + "    exec dbus-run-session startlxde\n"
            + "elif [ -x /usr/bin/startlxqt ]; then\n"
            + "    exec dbus-run-session startlxqt\n"
            + "elif [ -x /usr/bin/gnome-session ]; then\n"
            + "    export XDG_SESSION_TYPE=x11 && exec dbus-run-session gnome-session\n"
            + "elif [ -x /usr/bin/startplasma-x11 ]; then\n"
            + "    exec dbus-run-session startplasma-x11\n"
            + "elif [ -x /usr/bin/cinnamon-session ]; then\n"
            + "    exec dbus-run-session cinnamon-session\n"
            + "elif [ -x /usr/bin/openbox ]; then\n"
            + "    exec openbox\n"
            + "elif [ -x /usr/bin/i3 ]; then\n"
            + "    exec i3\n"
            + "elif [ -x /usr/bin/awesome ]; then\n"
            + "    exec awesome\n"
            + "elif [ -x /usr/bin/fluxbox ]; then\n"
            + "    exec fluxbox\n"
            + "else\n"
            + "    exec xterm\n"
            + "fi";
    }

    public static void writeDroidspacesDesktopScript(@NonNull String cName, @NonNull String innerCmd) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_DESKTOP_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";
        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Droidspaces Desktop Runner for Termux Launcher (Full Power Permissive Mode)\n\n"
            + "CNAME=\"" + cName + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "# 1. Auto-detect nama container jika belum diatur atau kosong\n"
            + "if [ -z \"$CNAME\" ] || [ ! -d \"/data/local/Droidspaces/Containers/$CNAME\" ]; then\n"
            + "    AUTO_C=$(ls -1 /data/local/Droidspaces/Containers/ 2>/dev/null | grep -v '^\\.' | head -n1)\n"
            + "    [ -n \"$AUTO_C\" ] && CNAME=\"$AUTO_C\"\n"
            + "fi\n\n"
            + "ROOTFS=\"/data/local/Droidspaces/Containers/$CNAME/rootfs\"\n"
            + "CONFIG_PATH=\"/data/local/Droidspaces/Containers/$CNAME/container.config\"\n\n"
            + "mkdir -p \"$X11_DIR\" 2>/dev/null || true\n"
            + "chmod 777 \"$X11_DIR\" 2>/dev/null || true\n\n"
            + "if [ -f \"$CONFIG_PATH\" ]; then\n"
            + "    sed -i 's/enable_termux_x11=1/enable_termux_x11=0/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "    sed -i 's/enable_pulseaudio=0/enable_pulseaudio=1/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# 2. Konfigurasi jembatan ALSA ke PulseAudio di dalam rootfs Droidspaces\n"
            + "if [ -d \"$ROOTFS/etc\" ]; then\n"
            + "    cat << 'EOF' > \"$ROOTFS/etc/asound.conf\"\n"
            + "pcm.!default {\n"
            + "    type pulse\n"
            + "}\n"
            + "ctl.!default {\n"
            + "    type pulse\n"
            + "}\n"
            + "EOF\n"
            + "    chmod 644 \"$ROOTFS/etc/asound.conf\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# 3. Cek apakah container Droidspaces sudah berjalan. Jika belum, start dulu agar tidak layar hitam!\n"
            + "DS_RUNNING=0\n"
            + "if /data/local/Droidspaces/bin/droidspaces pid --name=\"$CNAME\" >/dev/null 2>&1; then\n"
            + "    DS_RUNNING=1\n"
            + "elif /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw \"$CNAME\"; then\n"
            + "    DS_RUNNING=1\n"
            + "fi\n\n"
            + "if [ $DS_RUNNING -eq 0 ]; then\n"
            + "    /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" start >/dev/null 2>&1 || true\n"
            + "    sleep 1.5\n"
            + "fi\n\n"
            + "# 4. Pastikan socket X11 dan VirGL ter-mount segar ke rootfs Droidspaces\n"
            + "mkdir -p \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "if [ -S \"$VIRGL_DIR/.virgl_test\" ]; then\n"
            + "    rm -f \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "    ln -sf \"$VIRGL_DIR/.virgl_test\" \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# Share fcitx5 session bus with Droidspaces\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTFS/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTFS/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "# 5. Pastikan Termux IPC bridge daemon aktif\n"
            + "if [ -x \"/data/data/com.termux/files/usr/bin/termux-ipc-bridge\" ]; then\n"
            + "    pgrep -f termux-ipc-bridge >/dev/null 2>&1 || /data/data/com.termux/files/usr/bin/termux-ipc-bridge </dev/null >/dev/null 2>&1 &\n"
            + "fi\n\n"
            + "# 6. Tunggu sampai socket X0 muncul\n"
            + "for i in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "# 7. Jalankan Desktop Environment di dalam container Droidspaces (Full Power Permissive Mode)\n"
            + "exec /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" run /bin/sh -c '\n"
            + "export DISPLAY=:0\n"
            + "export GTK_IM_MODULE=fcitx\n"
            + "export QT_IM_MODULE=fcitx\n"
            + "export XMODIFIERS=@im=fcitx\n"
            + "export DBUS_SESSION_BUS_ADDRESS=\"unix:path=/tmp/termux-x11-im-bus-0\"\n"
            + "HOST_IP=$(ip route 2>/dev/null | awk \"/default/ {print \\$3}\" | head -n1)\n"
            + "[ -z \"$HOST_IP\" ] && HOST_IP=\"172.28.0.1\"\n"
            + "export PULSE_SERVER=\"tcp:$HOST_IP:4713\"\n"
            + "rm -f /tmp/.pulse-socket 2>/dev/null || true\n"
            + "export GALLIUM_DRIVER=virpipe\n"
            + "export MESA_GL_VERSION_OVERRIDE=4.3COMPAT\n"
            + "export MESA_GLES_VERSION_OVERRIDE=3.2\n"
            + "export MESA_NO_ERROR=1\n"
            + "export LIBGL_DRI3_DISABLE=1\n"
            + "export VTEST_SOCKET_NAME=/tmp/.virgl_test\n"
            + "export XDG_RUNTIME_DIR=/tmp/runtime-root\n"
            + "export _JAVA_AWT_WM_NONREPARENTING=1\n"
            + "export DISABLE_AUTO_TITLE=true\n"
            + "mkdir -p /tmp/runtime-root && chmod 700 /tmp/runtime-root 2>/dev/null || true\n\n"
            + "for j in $(seq 1 20); do\n"
            + "    xdpyinfo >/dev/null 2>&1 && break\n"
            + "    sleep 0.1\n"
            + "done\n\n"
            + innerCmd + "\n"
            + "'\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write droidspaces desktop script: " + e.getMessage());
        }
    }

    public static final String CHROOT_DESKTOP_SCRIPT_NAME = "termux-chroot-desktop";

    public static void writeChrootDesktopScript(@NonNull String rootPath, @NonNull String innerCmd) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_DESKTOP_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";
        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Chroot Desktop Runner for Termux Launcher\n\n"
            + "ROOTPATH=\"" + rootPath + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "mount -o bind \"$VIRGL_DIR\" \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "ln -sf /tmp/.virgl/.virgl_test \"$ROOTPATH/tmp/.virgl_test\" 2>/dev/null || true\n\n"
            + "# Tunggu socket X0 siap\n"
            + "for _w in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "# Share fcitx5 session bus socket with chroot\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTPATH/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTPATH/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "exec chroot \"$ROOTPATH\" /usr/bin/env -i \\\n"
            + "    HOME=/root \\\n"
            + "    USER=root \\\n"
            + "    LOGNAME=root \\\n"
            + "    TERM=xterm-256color \\\n"
            + "    PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \\\n"
            + "    DISPLAY=${DISPLAY:-:0} \\\n"
            + "    GTK_IM_MODULE=fcitx \\\n"
            + "    QT_IM_MODULE=fcitx \\\n"
            + "    XMODIFIERS=@im=fcitx \\\n"
            + "    DBUS_SESSION_BUS_ADDRESS=unix:path=/tmp/termux-x11-im-bus-0 \\\n"
            + "    PULSE_SERVER=tcp:127.0.0.1:4713 \\\n"
            + "    GALLIUM_DRIVER=virpipe \\\n"
            + "    MESA_GL_VERSION_OVERRIDE=4.3COMPAT \\\n"
            + "    MESA_GLES_VERSION_OVERRIDE=3.2 \\\n"
            + "    MESA_NO_ERROR=1 \\\n"
            + "    LIBGL_DRI3_DISABLE=1 \\\n"
            + "    VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test \\\n"
            + "    XKB_CONFIG_ROOT=/usr/share/X11/xkb \\\n"
            + "    LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 \\\n"
            + "    DISABLE_AUTO_TITLE=true \\\n"
            + "    /bin/bash -c " + ProotDistro.singleQuote(innerCmd) + "\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write chroot desktop script: " + e.getMessage());
        }
    }

    public static final String CHROOT_APP_SCRIPT_NAME = "termux-chroot-app";
    public static final String DROIDSPACES_APP_SCRIPT_NAME = "termux-droidspaces-app";
    public static final String CHROOT_KIOSK_SCRIPT_NAME = "termux-chroot-kiosk";
    public static final String DROIDSPACES_KIOSK_SCRIPT_NAME = "termux-droidspaces-kiosk";

    public static void writeChrootKioskScript(@NonNull String rootPath) {
        writeChrootKioskScript(rootPath, null);
    }

    public static void writeChrootKioskScript(@NonNull String rootPath, @Nullable String preferredDe) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_KIOSK_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";

        String pref = (preferredDe != null) ? preferredDe.trim().toLowerCase(java.util.Locale.ROOT) : "";
        if ("bspwm_kiosk".equals(pref)) pref = "bspwm";
        if ("openbox_kiosk".equals(pref)) pref = "openbox";

        String wmSelection;
        if ("openbox".equals(pref)) {
            wmSelection = "if command -v openbox >/dev/null 2>&1; then "
                + "  exec openbox; "
                + "elif command -v bspwm >/dev/null 2>&1; then "
                + "  mkdir -p /root/.config/bspwm 2>/dev/null || true; "
                + "  printf \"#!/bin/sh\\nbspc monitor -d 1\\nbspc config border_width 0\\nbspc config window_gap 0\\nbspc config top_padding 0\\nbspc config bottom_padding 0\\nbspc config left_padding 0\\nbspc config right_padding 0\\nbspc config single_monocle true\\nbspc config gapless_monocle true\\nbspc config borderless_monocle true\\nbspc rule -a * state=tiled\\nbspc desktop 1 -l monocle\\n\" > /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  chmod +x /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  exec dbus-run-session -- bspwm -c /root/.config/bspwm/kiosk.bspwmrc; ";
        } else {
            wmSelection = "if command -v bspwm >/dev/null 2>&1; then "
                + "  mkdir -p /root/.config/bspwm 2>/dev/null || true; "
                + "  printf \"#!/bin/sh\\nbspc monitor -d 1\\nbspc config border_width 0\\nbspc config window_gap 0\\nbspc config top_padding 0\\nbspc config bottom_padding 0\\nbspc config left_padding 0\\nbspc config right_padding 0\\nbspc config single_monocle true\\nbspc config gapless_monocle true\\nbspc config borderless_monocle true\\nbspc rule -a * state=tiled\\nbspc desktop 1 -l monocle\\n\" > /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  chmod +x /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  exec dbus-run-session -- bspwm -c /root/.config/bspwm/kiosk.bspwmrc; "
                + "elif command -v openbox >/dev/null 2>&1; then "
                + "  exec openbox; ";
        }
        wmSelection += "elif command -v xfwm4 >/dev/null 2>&1; then "
            + "  exec xfwm4; "
            + "elif command -v i3 >/dev/null 2>&1; then "
            + "  exec i3; "
            + "else "
            + "  while true; do sleep 3600; done; "
            + "fi";

        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Chroot Kiosk Window Manager (Standalone App) for Termux Launcher\n\n"
            + "ROOTPATH=\"" + rootPath + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "mount -o bind \"$VIRGL_DIR\" \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "ln -sf /tmp/.virgl/.virgl_test \"$ROOTPATH/tmp/.virgl_test\" 2>/dev/null || true\n\n"
            + "for _w in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "# Share fcitx5 session bus socket with chroot\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTPATH/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTPATH/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "exec chroot \"$ROOTPATH\" /usr/bin/env -i \\\n"
            + "    HOME=/root \\\n"
            + "    USER=root \\\n"
            + "    LOGNAME=root \\\n"
            + "    TERM=xterm-256color \\\n"
            + "    PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \\\n"
            + "    DISPLAY=${DISPLAY:-:0} \\\n"
            + "    GTK_IM_MODULE=fcitx \\\n"
            + "    QT_IM_MODULE=fcitx \\\n"
            + "    XMODIFIERS=@im=fcitx \\\n"
            + "    DBUS_SESSION_BUS_ADDRESS=unix:path=/tmp/termux-x11-im-bus-0 \\\n"
            + "    PULSE_SERVER=tcp:127.0.0.1:4713 \\\n"
            + "    GALLIUM_DRIVER=virpipe \\\n"
            + "    MESA_GL_VERSION_OVERRIDE=4.3COMPAT \\\n"
            + "    MESA_GLES_VERSION_OVERRIDE=3.2 \\\n"
            + "    MESA_NO_ERROR=1 \\\n"
            + "    LIBGL_DRI3_DISABLE=1 \\\n"
            + "    VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test \\\n"
            + "    XKB_CONFIG_ROOT=/usr/share/X11/xkb \\\n"
            + "    LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 \\\n"
            + "    XDG_RUNTIME_DIR=/tmp/runtime-root \\\n"
            + "    XDG_CURRENT_DESKTOP=" + ("openbox".equals(pref) ? "openbox" : "bspwm") + " \\\n"
            + "    _JAVA_AWT_WM_NONREPARENTING=1 \\\n"
            + "    DISABLE_AUTO_TITLE=true \\\n"
            + "    /bin/bash -c 'for _w in $(seq 1 50); do [ -S /tmp/.X11-unix/X0 ] && break; sleep 0.1; done; "
            + "mkdir -p /tmp/runtime-root && chmod 700 /tmp/runtime-root; "
            + "/etc/init.d/dbus start >/dev/null 2>&1 || true; "
            + wmSelection + "'\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write chroot kiosk script: " + e.getMessage());
        }
    }

    public static void writeDroidspacesKioskScript(@NonNull String containerName) {
        writeDroidspacesKioskScript(containerName, null);
    }

    public static void writeDroidspacesKioskScript(@NonNull String containerName, @Nullable String preferredDe) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_KIOSK_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";
        String configPath = "/data/local/Droidspaces/Containers/" + containerName + "/container.config";

        String pref = (preferredDe != null) ? preferredDe.trim().toLowerCase(java.util.Locale.ROOT) : "";
        if ("bspwm_kiosk".equals(pref)) pref = "bspwm";
        if ("openbox_kiosk".equals(pref)) pref = "openbox";

        String wmSelection;
        if ("openbox".equals(pref)) {
            wmSelection = "if command -v openbox >/dev/null 2>&1; then "
                + "  exec openbox; "
                + "elif command -v bspwm >/dev/null 2>&1; then "
                + "  rm -f /tmp/bspwm* 2>/dev/null || true; "
                + "  mkdir -p \"$HOME/.config/bspwm\" 2>/dev/null || true; "
                + "  printf \"#!/bin/sh\\nbspc monitor -d 1\\nbspc config border_width 0\\nbspc config window_gap 0\\nbspc config top_padding 0\\nbspc config bottom_padding 0\\nbspc config left_padding 0\\nbspc config right_padding 0\\nbspc config single_monocle true\\nbspc config gapless_monocle true\\nbspc config borderless_monocle true\\nbspc rule -a * state=tiled\\nbspc desktop 1 -l monocle\\n\" > \"$HOME/.config/bspwm/kiosk.bspwmrc\" 2>/dev/null || true; "
                + "  chmod +x \"$HOME/.config/bspwm/kiosk.bspwmrc\" 2>/dev/null || true; "
                + "  exec dbus-run-session -- bspwm -c \"$HOME/.config/bspwm/kiosk.bspwmrc\"; ";
        } else {
            wmSelection = "if command -v bspwm >/dev/null 2>&1; then "
                + "  rm -f /tmp/bspwm* 2>/dev/null || true; "
                + "  mkdir -p \"$HOME/.config/bspwm\" 2>/dev/null || true; "
                + "  printf \"#!/bin/sh\\nbspc monitor -d 1\\nbspc config border_width 0\\nbspc config window_gap 0\\nbspc config top_padding 0\\nbspc config bottom_padding 0\\nbspc config left_padding 0\\nbspc config right_padding 0\\nbspc config single_monocle true\\nbspc config gapless_monocle true\\nbspc config borderless_monocle true\\nbspc rule -a * state=tiled\\nbspc desktop 1 -l monocle\\n\" > \"$HOME/.config/bspwm/kiosk.bspwmrc\" 2>/dev/null || true; "
                + "  chmod +x \"$HOME/.config/bspwm/kiosk.bspwmrc\" 2>/dev/null || true; "
                + "  exec dbus-run-session -- bspwm -c \"$HOME/.config/bspwm/kiosk.bspwmrc\"; "
                + "elif command -v openbox >/dev/null 2>&1; then "
                + "  exec openbox; ";
        }
        wmSelection += "elif command -v xfwm4 >/dev/null 2>&1; then "
            + "  exec xfwm4; "
            + "elif command -v i3 >/dev/null 2>&1; then "
            + "  exec i3; "
            + "else "
            + "  while true; do sleep 3600; done; "
            + "fi";

        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Droidspaces Kiosk Window Manager (Standalone App) for Termux Launcher\n\n"
            + "CNAME=\"" + containerName + "\"\n"
            + "ROOTFS=\"/data/local/Droidspaces/Containers/$CNAME/rootfs\"\n"
            + "CONFIG_PATH=\"" + configPath + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "for _w in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "DS_RUNNING=0\n"
            + "if /data/local/Droidspaces/bin/droidspaces pid --name=\"$CNAME\" >/dev/null 2>&1; then\n"
            + "    DS_RUNNING=1\n"
            + "elif /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw \"$CNAME\"; then\n"
            + "    DS_RUNNING=1\n"
            + "fi\n\n"
            + "if [ $DS_RUNNING -eq 0 ]; then\n"
            + "    sed -i 's/enable_termux_x11=1/enable_termux_x11=0/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "    sed -i 's/enable_pulseaudio=0/enable_pulseaudio=1/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "    if [ -f \"$CONFIG_PATH\" ]; then\n"
            + "        /data/local/Droidspaces/bin/droidspaces --config=\"$CONFIG_PATH\" -B \"$X11_DIR:/tmp/.X11-unix\" start >/dev/null 2>&1 || true\n"
            + "    else\n"
            + "        /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" -B \"$X11_DIR:/tmp/.X11-unix\" start >/dev/null 2>&1 || true\n"
            + "    fi\n"
            + "    sleep 1\n"
            + "fi\n\n"
            + "mkdir -p \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "if [ -S \"$VIRGL_DIR/.virgl_test\" ]; then\n"
            + "    rm -f \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "    ln -sf \"$VIRGL_DIR/.virgl_test\" \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# Share fcitx5 session bus with Droidspaces\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTFS/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTFS/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "exec /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" run /bin/sh -c '\n"
            + "export DISPLAY=:0\n"
            + "export GTK_IM_MODULE=fcitx\n"
            + "export QT_IM_MODULE=fcitx\n"
            + "export XMODIFIERS=@im=fcitx\n"
            + "export DBUS_SESSION_BUS_ADDRESS=\"unix:path=/tmp/termux-x11-im-bus-0\"\n"
            + "for _w in $(seq 1 50); do [ -S /tmp/.X11-unix/X0 ] && break; sleep 0.1; done\n"
            + "HOST_IP=$(ip route 2>/dev/null | awk \"/default/ {print \\$3}\" | head -n1)\n"
            + "[ -z \"$HOST_IP\" ] && HOST_IP=\"172.28.0.1\"\n"
            + "export PULSE_SERVER=\"tcp:$HOST_IP:4713\"\n"
            + "rm -f /tmp/.pulse-socket 2>/dev/null || true\n"
            + "export GALLIUM_DRIVER=virpipe\n"
            + "export MESA_GL_VERSION_OVERRIDE=4.3COMPAT\n"
            + "export MESA_GLES_VERSION_OVERRIDE=3.2\n"
            + "export MESA_NO_ERROR=1\n"
            + "export LIBGL_DRI3_DISABLE=1\n"
            + "export VTEST_SOCKET_NAME=/tmp/.virgl_test\n"
            + "export XDG_RUNTIME_DIR=/tmp/runtime-root\n"
            + "export _JAVA_AWT_WM_NONREPARENTING=1\n"
            + "export DISABLE_AUTO_TITLE=true\n"
            + "export XDG_CURRENT_DESKTOP=" + ("openbox".equals(pref) ? "openbox" : "bspwm") + "\n"
            + "mkdir -p /tmp/runtime-root && chmod 700 /tmp/runtime-root 2>/dev/null || true\n"
            + "/etc/init.d/dbus start >/dev/null 2>&1 || true\n"
            + wmSelection + "\n"
            + "'\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write droidspaces kiosk script: " + e.getMessage());
        }
    }

    public static void writeChrootAppScript(@NonNull String rootPath) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_APP_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";
        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Chroot App Runner (Single-App Kiosk) for Termux Launcher\n\n"
            + "ROOTPATH=\"" + rootPath + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTPATH/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "mkdir -p \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "mount -o bind \"$VIRGL_DIR\" \"$ROOTPATH/tmp/.virgl\" 2>/dev/null || true\n"
            + "ln -sf /tmp/.virgl/.virgl_test \"$ROOTPATH/tmp/.virgl_test\" 2>/dev/null || true\n\n"
            + "# Tunggu socket X0 siap\n"
            + "for _w in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "# Share fcitx5 session bus socket with chroot\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTPATH/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTPATH/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "exec chroot \"$ROOTPATH\" /usr/bin/env -i \\\n"
            + "    HOME=/root \\\n"
            + "    USER=root \\\n"
            + "    LOGNAME=root \\\n"
            + "    TERM=xterm-256color \\\n"
            + "    PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \\\n"
            + "    DISPLAY=${DISPLAY:-:0} \\\n"
            + "    GTK_IM_MODULE=fcitx \\\n"
            + "    QT_IM_MODULE=fcitx \\\n"
            + "    XMODIFIERS=@im=fcitx \\\n"
            + "    DBUS_SESSION_BUS_ADDRESS=unix:path=/tmp/termux-x11-im-bus-0 \\\n"
            + "    PULSE_SERVER=tcp:127.0.0.1:4713 \\\n"
            + "    GALLIUM_DRIVER=virpipe \\\n"
            + "    MESA_GL_VERSION_OVERRIDE=4.3COMPAT \\\n"
            + "    MESA_GLES_VERSION_OVERRIDE=3.2 \\\n"
            + "    MESA_NO_ERROR=1 \\\n"
            + "    LIBGL_DRI3_DISABLE=1 \\\n"
            + "    VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test \\\n"
            + "    XKB_CONFIG_ROOT=/usr/share/X11/xkb \\\n"
            + "    LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 \\\n"
            + "    DISABLE_AUTO_TITLE=true \\\n"
            + "    /bin/bash -c \"for _w in \\$(seq 1 50); do [ -S /tmp/.X11-unix/X0 ] && break; sleep 0.1; done; for _wm in \\$(seq 1 40); do bspc query -M >/dev/null 2>&1 && break; xprop -root _NET_SUPPORTING_WM_CHECK >/dev/null 2>&1 && break; sleep 0.1; done; \\\"\\$@\\\"\" dummy \"$@\"\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write chroot app script: " + e.getMessage());
        }
    }

    public static void writeDroidspacesAppScript(@NonNull String containerName) {
        String scriptPath = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_APP_SCRIPT_NAME;
        String x11Dir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.X11-unix";
        String virglDir = TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.virgl";
        String configPath = "/data/local/Droidspaces/Containers/" + containerName + "/container.config";
        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Droidspaces App Runner (Single-App Kiosk) for Termux Launcher\n\n"
            + "CNAME=\"" + containerName + "\"\n"
            + "ROOTFS=\"/data/local/Droidspaces/Containers/$CNAME/rootfs\"\n"
            + "CONFIG_PATH=\"" + configPath + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n"
            + "VIRGL_DIR=\"" + virglDir + "\"\n\n"
            + "for _w in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "DS_RUNNING=0\n"
            + "if /data/local/Droidspaces/bin/droidspaces pid --name=\"$CNAME\" >/dev/null 2>&1; then\n"
            + "    DS_RUNNING=1\n"
            + "elif /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw \"$CNAME\"; then\n"
            + "    DS_RUNNING=1\n"
            + "fi\n\n"
            + "if [ $DS_RUNNING -eq 0 ]; then\n"
            + "    sed -i 's/enable_termux_x11=1/enable_termux_x11=0/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "    sed -i 's/enable_pulseaudio=0/enable_pulseaudio=1/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "    if [ -f \"$CONFIG_PATH\" ]; then\n"
            + "        /data/local/Droidspaces/bin/droidspaces --config=\"$CONFIG_PATH\" -B \"$X11_DIR:/tmp/.X11-unix\" start >/dev/null 2>&1 || true\n"
            + "    else\n"
            + "        /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" -B \"$X11_DIR:/tmp/.X11-unix\" start >/dev/null 2>&1 || true\n"
            + "    fi\n"
            + "    sleep 1\n"
            + "fi\n\n"
            + "mkdir -p \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "umount -l \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mount -o bind \"$X11_DIR\" \"$ROOTFS/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "if [ -S \"$VIRGL_DIR/.virgl_test\" ]; then\n"
            + "    rm -f \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "    ln -sf \"$VIRGL_DIR/.virgl_test\" \"$ROOTFS/tmp/.virgl_test\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# Share fcitx5 session bus with Droidspaces\n"
            + "for _im_bus in \"$PREFIX/tmp\"/termux-x11-im-bus-*; do\n"
            + "    if [ -S \"$_im_bus\" ]; then\n"
            + "        _im_name=$(basename \"$_im_bus\")\n"
            + "        ln -sf \"$_im_bus\" \"$ROOTFS/tmp/$_im_name\" 2>/dev/null || true\n"
            + "        ln -sf \"${_im_bus}.pid\" \"$ROOTFS/tmp/${_im_name}.pid\" 2>/dev/null || true\n"
            + "    fi\n"
            + "done\n\n"
            + "exec /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" run /bin/sh -c '\n"
            + "export DISPLAY=:0\n"
            + "export GTK_IM_MODULE=fcitx\n"
            + "export QT_IM_MODULE=fcitx\n"
            + "export XMODIFIERS=@im=fcitx\n"
            + "export DBUS_SESSION_BUS_ADDRESS=\"unix:path=/tmp/termux-x11-im-bus-0\"\n"
            + "for _w in $(seq 1 50); do [ -S /tmp/.X11-unix/X0 ] && break; sleep 0.1; done\n"
            + "HOST_IP=$(ip route 2>/dev/null | awk \"/default/ {print \\$3}\" | head -n1)\n"
            + "[ -z \"$HOST_IP\" ] && HOST_IP=\"172.28.0.1\"\n"
            + "export PULSE_SERVER=\"tcp:$HOST_IP:4713\"\n"
            + "rm -f /tmp/.pulse-socket 2>/dev/null || true\n"
            + "export GALLIUM_DRIVER=virpipe\n"
            + "export MESA_GL_VERSION_OVERRIDE=4.3COMPAT\n"
            + "export MESA_GLES_VERSION_OVERRIDE=3.2\n"
            + "export MESA_NO_ERROR=1\n"
            + "export LIBGL_DRI3_DISABLE=1\n"
            + "export VTEST_SOCKET_NAME=/tmp/.virgl_test\n"
            + "export XDG_RUNTIME_DIR=/tmp/runtime-root\n"
            + "export _JAVA_AWT_WM_NONREPARENTING=1\n"
            + "export DISABLE_AUTO_TITLE=true\n"
            + "for _wm in $(seq 1 40); do bspc query -M >/dev/null 2>&1 && break; xprop -root _NET_SUPPORTING_WM_CHECK >/dev/null 2>&1 && break; sleep 0.1; done\n"
            + "\"$@\"\n"
            + "' dummy \"$@\"\n";

        File scriptFile = new File(scriptPath);
        try {
            File parent = scriptFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            java.nio.file.Files.write(scriptFile.toPath(), scriptContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            scriptFile.setReadable(true, false);
            scriptFile.setWritable(true, true);
            scriptFile.setExecutable(true, false);
        } catch (Exception e) {
            Logger.logWarn(LOG_TAG, "Failed to write droidspaces app script: " + e.getMessage());
        }
    }

    @NonNull
    public static String getDesktopCommand(@Nullable ChrootEnvironment env, @Nullable DesktopEntry de) {
        if (env != null && env.runnerType == RunnerType.DROIDSPACES) {
            String cName = env.containerName;
            String inner = (de != null) ? de.getDroidspacesInnerCommand() : getDroidspacesAutoDesktopCommand();
            writeDroidspacesDesktopScript(cName, inner);
            return "su -c " + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_DESKTOP_SCRIPT_NAME;
        } else if (env != null && env.runnerType == RunnerType.CUSTOM && !env.customRunner.trim().isEmpty()) {
            String inner = (de != null) ? de.getChrootInnerCommand() : "exec dbus-launch --exit-with-session startxfce4";
            return env.customRunner
                .replace("%ROOTFS%", env.rootfs.getAbsolutePath())
                .replace("%COMMAND%", "'export DISPLAY=:0 PULSE_SERVER=tcp:127.0.0.1:4713 GALLIUM_DRIVER=virpipe MESA_GL_VERSION_OVERRIDE=4.3COMPAT MESA_GLES_VERSION_OVERRIDE=3.2 MESA_NO_ERROR=1 LIBGL_DRI3_DISABLE=1 VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test && " + inner + "'");
        }

        String rootPath = (env != null) ? env.rootfs.getAbsolutePath() : "/data/local/nhsystem/kali-arm64";
        String inner = (de != null) ? de.getChrootInnerCommand()
            : "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session startxfce4";

        writeChrootDesktopScript(rootPath, inner);
        return "su -mm -c " + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_DESKTOP_SCRIPT_NAME;
    }

    /**
     * Resolves the startup command for the active desktop environment across
     * Chroot, PRoot, or native Termux X11.
     */
    @NonNull
    public static String resolveActiveDesktopCommand(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
            Context.MODE_PRIVATE);
        String route = prefs.getString("gui_apps_route", "x11");
        String selectedDeId = prefs.getString(KEY_SELECTED_DE, "auto");

        List<DesktopEntry> installed = detectInstalledDesktops(context);
        DesktopEntry selectedEntry = null;

        if (!installed.isEmpty()) {
            for (DesktopEntry entry : installed) {
                if (entry.id.equals(selectedDeId)) {
                    selectedEntry = entry;
                    break;
                }
            }
            if (selectedEntry == null) {
                for (DesktopEntry entry : installed) {
                    if ("bspwm".equals(entry.id)) {
                        selectedEntry = entry;
                        break;
                    }
                }
                if (selectedEntry == null) {
                    for (DesktopEntry entry : installed) {
                        if ("openbox".equals(entry.id)) {
                            selectedEntry = entry;
                            break;
                        }
                    }
                }
                if (selectedEntry == null) {
                    for (DesktopEntry entry : installed) {
                        if ("xfce4".equals(entry.id)) {
                            selectedEntry = entry;
                            break;
                        }
                    }
                }
                if (selectedEntry == null) {
                    selectedEntry = installed.get(0);
                }
            }
        }

        if (GuiAppsSetup.Route.CHROOT.key.equals(route)) {
            ChrootEnvironment env = getActiveEnvironment(context);
            return getDesktopCommand(env, selectedEntry);
        } else if (GuiAppsSetup.Route.DISTRO.key.equals(route)) {
            String distro = prefs.getString("gui_apps_distro", "debian");
            String inner = (selectedEntry != null) ? selectedEntry.getChrootInnerCommand()
                : "/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session startxfce4";
            return "proot-distro login " + distro + " --shared-x11 --shared-tmp"
                + " -e DISPLAY=${DISPLAY:-:0}"
                + " -e PULSE_SERVER=tcp:127.0.0.1:4713"
                + " -e GALLIUM_DRIVER=virpipe"
                + " -e MESA_GL_VERSION_OVERRIDE=4.3COMPAT"
                + " -e MESA_GLES_VERSION_OVERRIDE=3.2"
                + " -e MESA_NO_ERROR=1"
                + " -e LIBGL_DRI3_DISABLE=1"
                + " -e VTEST_SOCKET_NAME=/tmp/.virgl_test"
                + " -- /bin/bash -c " + ProotDistro.singleQuote(inner);
        } else {
            // Route X11 (native)
            String baseCmd;
            if (selectedEntry != null) {
                baseCmd = selectedEntry.getHostCommand();
            } else {
                TermuxAppSharedPreferences appPrefs = TermuxAppSharedPreferences.build(context);
                String customWm = appPrefs != null ? appPrefs.getX11WindowManager() : null;
                baseCmd = (customWm != null && !customWm.trim().isEmpty()) ? customWm : "openbox";
            }
            return "env PULSE_SERVER=tcp:127.0.0.1:4713 GALLIUM_DRIVER=virpipe MESA_GL_VERSION_OVERRIDE=4.3COMPAT MESA_GLES_VERSION_OVERRIDE=3.2 MESA_NO_ERROR=1 LIBGL_DRI3_DISABLE=1 VTEST_SOCKET_NAME=/data/data/com.termux/files/usr/tmp/.virgl/.virgl_test " + baseCmd;
        }
    }

    /**
     * Resolves the startup command for Standalone App Kiosk Mode.
     * Starts a minimal Window Manager (BSPWM monocle with padding 0, or Openbox)
     * without desktop bloat (no polybar, no desktop wallpapers, no heavy session daemons).
     */
    @NonNull
    public static String resolveStandaloneKioskCommand(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
            Context.MODE_PRIVATE);
        String route = prefs.getString("gui_apps_route", "x11");

        if (GuiAppsSetup.Route.CHROOT.key.equals(route)) {
            ChrootEnvironment env = getActiveEnvironment(context);
            if (env != null && env.runnerType == RunnerType.DROIDSPACES) {
                String selectedDe = prefs.getString(KEY_SELECTED_DE, "bspwm");
                writeDroidspacesKioskScript(env.containerName, selectedDe);
                return "su -c " + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + DROIDSPACES_KIOSK_SCRIPT_NAME;
            } else if (env != null) {
                String selectedDe = prefs.getString(KEY_SELECTED_DE, "bspwm");
                writeChrootKioskScript(env.rootfs.getAbsolutePath(), selectedDe);
                return "su -mm -c " + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/" + CHROOT_KIOSK_SCRIPT_NAME;
            }
        } else if (GuiAppsSetup.Route.DISTRO.key.equals(route)) {
            String distro = prefs.getString("gui_apps_distro", "debian");
            String inner = "/etc/init.d/dbus start >/dev/null 2>&1 || true; "
                + "for _w in $(seq 1 50); do [ -S /tmp/.X11-unix/X0 ] && break; sleep 0.1; done; "
                + "if command -v bspwm >/dev/null 2>&1; then "
                + "  mkdir -p /root/.config/bspwm 2>/dev/null || true; "
                + "  printf '#!/bin/sh\\nbspc monitor -d 1\\nbspc config border_width 0\\nbspc config window_gap 0\\nbspc config top_padding 0\\nbspc config bottom_padding 0\\nbspc config left_padding 0\\nbspc config right_padding 0\\nbspc config single_monocle true\\nbspc config gapless_monocle true\\nbspc config borderless_monocle true\\nbspc desktop 1 -l monocle\\n' > /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  chmod +x /root/.config/bspwm/kiosk.bspwmrc 2>/dev/null || true; "
                + "  exec dbus-run-session -- bspwm -c /root/.config/bspwm/kiosk.bspwmrc; "
                + "elif command -v openbox >/dev/null 2>&1; then "
                + "  exec openbox; "
                + "elif command -v xfwm4 >/dev/null 2>&1; then "
                + "  exec xfwm4; "
                + "elif command -v i3 >/dev/null 2>&1; then "
                + "  exec i3; "
                + "else "
                + "  while true; do sleep 3600; done; "
                + "fi";
            return "proot-distro login " + distro + " --shared-x11 --shared-tmp"
                + " -e DISPLAY=${DISPLAY:-:0}"
                + " -e PULSE_SERVER=tcp:127.0.0.1:4713"
                + " -e GALLIUM_DRIVER=virpipe"
                + " -e MESA_GL_VERSION_OVERRIDE=4.3COMPAT"
                + " -e MESA_GLES_VERSION_OVERRIDE=3.2"
                + " -e MESA_NO_ERROR=1"
                + " -e LIBGL_DRI3_DISABLE=1"
                + " -e VTEST_SOCKET_NAME=/tmp/.virgl_test"
                + " -- /bin/bash -c " + ProotDistro.singleQuote(inner);
        }

        // Host / Native X11 fallback
        File bspwmBin = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "bspwm");
        if (bspwmBin.canExecute()) {
            return "(for k in $(seq 1 50); do bspc query -M >/dev/null 2>&1 && break; sleep 0.1; done; "
                + "bspc config single_monocle true; "
                + "bspc config gapless_monocle true; "
                + "bspc config borderless_monocle true; "
                + "bspc config window_gap 0; "
                + "bspc config top_padding 0; "
                + "bspc config bottom_padding 0; "
                + "bspc config left_padding 0; "
                + "bspc config right_padding 0; "
                + "for d in $(bspc query -D); do bspc desktop \"$d\" -l monocle; done) & bspwm";
        }
        return "openbox";
    }

    /**
     * Scans and returns ONLY the Desktop Environments / Window Managers that are actually installed
     * in the currently active environment (Chroot / PRoot / Host X11).
     */
    @NonNull
    public static List<DesktopEntry> detectInstalledDesktops(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION,
            Context.MODE_PRIVATE);
        String route = prefs.getString("gui_apps_route", "x11");

        List<DesktopEntry> results = new ArrayList<>();

        if (GuiAppsSetup.Route.CHROOT.key.equals(route)) {
            ChrootEnvironment env = getActiveEnvironment(context);
            if (env != null) {
                if (env.runnerType == RunnerType.DROIDSPACES) {
                    detectDesktopsDroidspaces(env, results);
                } else {
                    detectDesktopsChroot(env, results);
                }
            }
        } else if (GuiAppsSetup.Route.DISTRO.key.equals(route)) {
            String distro = prefs.getString("gui_apps_distro", "debian");
            detectDesktopsProot(distro, results);
        } else {
            detectDesktopsHost(results);
        }

        return results;
    }

    private static void detectDesktopsChroot(@NonNull ChrootEnvironment env,
                                            @NonNull List<DesktopEntry> results) {
        File rootfs = env.rootfs;
        String rootPath = rootfs.getAbsolutePath();

        for (DesktopDef def : SUPPORTED_DESKTOPS) {
            for (String bin : def.candidateBinaries) {
                if (new File(rootfs, "usr/bin/" + bin).exists()
                        || new File(rootfs, "bin/" + bin).exists()
                        || new File(rootfs, "usr/local/bin/" + bin).exists()) {
                    results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                    break;
                }
            }
        }

        if (results.isEmpty() && isRootAvailable()) {
            StringBuilder checkCmd = new StringBuilder("for b in");
            for (DesktopDef def : SUPPORTED_DESKTOPS) {
                for (String bin : def.candidateBinaries) {
                    checkCmd.append(" ").append(bin);
                }
            }
            checkCmd.append("; do if [ -x ").append(rootPath).append("/usr/bin/$b ] || [ -x ")
                    .append(rootPath).append("/bin/$b ] || [ -x ").append(rootPath).append("/usr/local/bin/$b ]; then echo $b; fi; done");

            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", checkCmd.toString()});
                List<String> foundBins = new ArrayList<>();
                try (java.io.BufferedReader r = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) foundBins.add(line);
                    }
                }
                p.waitFor();

                for (DesktopDef def : SUPPORTED_DESKTOPS) {
                    for (String bin : def.candidateBinaries) {
                        if (foundBins.contains(bin)) {
                            results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private static void detectDesktopsDroidspaces(@NonNull ChrootEnvironment env,
                                                 @NonNull List<DesktopEntry> results) {
        File rootfs = env.rootfs;
        for (DesktopDef def : SUPPORTED_DESKTOPS) {
            for (String bin : def.candidateBinaries) {
                if (new File(rootfs, "usr/bin/" + bin).exists()
                        || new File(rootfs, "bin/" + bin).exists()
                        || new File(rootfs, "usr/local/bin/" + bin).exists()) {
                    results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                    break;
                }
            }
        }

        if (results.isEmpty() && isRootAvailable()) {
            StringBuilder checkCmd = new StringBuilder(
                "/data/local/Droidspaces/bin/droidspaces --name=\"" + env.containerName + "\" run /bin/sh -c \"for b in");
            for (DesktopDef def : SUPPORTED_DESKTOPS) {
                for (String bin : def.candidateBinaries) {
                    checkCmd.append(" ").append(bin);
                }
            }
            checkCmd.append("; do if [ -x /usr/bin/\\$b ] || [ -x /bin/\\$b ] || [ -x /usr/local/bin/\\$b ]; then echo \\$b; fi; done\"");

            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", checkCmd.toString()});
                List<String> foundBins = new ArrayList<>();
                try (java.io.BufferedReader r = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) foundBins.add(line);
                    }
                }
                p.waitFor();

                for (DesktopDef def : SUPPORTED_DESKTOPS) {
                    for (String bin : def.candidateBinaries) {
                        if (foundBins.contains(bin)) {
                            results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private static void detectDesktopsProot(@NonNull String distro,
                                           @NonNull List<DesktopEntry> results) {
        File rootfs = new File("/data/data/com.termux/files/usr/var/lib/proot-distro/installed-rootfs/" + distro);
        if (!rootfs.isDirectory()) return;

        for (DesktopDef def : SUPPORTED_DESKTOPS) {
            for (String bin : def.candidateBinaries) {
                if (new File(rootfs, "usr/bin/" + bin).exists()
                        || new File(rootfs, "bin/" + bin).exists()
                        || new File(rootfs, "usr/local/bin/" + bin).exists()) {
                    results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                    break;
                }
            }
        }
    }

    private static void detectDesktopsHost(@NonNull List<DesktopEntry> results) {
        File binDir = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH);
        if (!binDir.isDirectory()) return;

        for (DesktopDef def : SUPPORTED_DESKTOPS) {
            for (String bin : def.candidateBinaries) {
                if (new File(binDir, bin).exists()) {
                    results.add(new DesktopEntry(def.id, def.displayName + " (" + bin + ")", bin, def.candidateBinaries));
                    break;
                }
            }
        }
    }

    /**
     * Starts background audio (PulseAudio TCP server on 127.0.0.1:4713) and graphics
     * hardware acceleration (VirGL test server with ANGLE fallback) if their binaries are installed in Termux.
     */
    public static void startAudioAndGraphicsServices(@NonNull Context context) {
        new Thread(() -> {
            startAudioAndGraphicsServicesSync(context);
        }, "Media-Graphics-Starter").start();
    }

    public static void startAudioAndGraphicsServicesSync(@NonNull Context context) {
        File pulseBin = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "pulseaudio");
        File virglBin = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "virgl_test_server_android");
        File tmpDir = new File(TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH);
        File virglDir = new File(tmpDir, ".virgl");

        // 1. Start PulseAudio if installed and not running
        if (pulseBin.exists()) {
            pulseBin.setExecutable(true);
            boolean isRunning = false;
            try (java.net.Socket s = new java.net.Socket("127.0.0.1", 4713)) {
                isRunning = true;
            } catch (Exception ignored) {}

            String[] envp = new String[]{
                "HOME=" + TermuxConstants.TERMUX_HOME_DIR_PATH,
                "TMPDIR=" + TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH,
                "PATH=" + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + ":/system/bin"
            };

            if (!isRunning) {
                try {
                    Logger.logInfo(LOG_TAG, "Starting PulseAudio TCP server on port 4713...");
                    String[] cmd = new String[]{
                        pulseBin.getAbsolutePath(),
                        "--start",
                        "--load=module-native-protocol-tcp auth-ip-acl=127.0.0.1;172.28.0.0/16 auth-anonymous=1",
                        "--load=module-native-protocol-unix socket=" + TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.pulse-socket auth-anonymous=1",
                        "--exit-idle-time=-1"
                    };
                    Process p = Runtime.getRuntime().exec(cmd, envp, new File(TermuxConstants.TERMUX_HOME_DIR_PATH));
                    p.waitFor();
                } catch (Exception e) {
                    Logger.logWarn(LOG_TAG, "Failed to start PulseAudio: " + e.getMessage());
                }
            }

            // Always ensure module-suspend-on-idle is unloaded so Android OpenSL ES audio output never sleeps
            File pactlBin = new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "pactl");
            if (pactlBin.exists()) {
                try {
                    Runtime.getRuntime().exec(new String[]{
                        pactlBin.getAbsolutePath(),
                        "unload-module",
                        "module-suspend-on-idle"
                    }, envp, new File(TermuxConstants.TERMUX_HOME_DIR_PATH)).waitFor();
                    Runtime.getRuntime().exec(new String[]{
                        pactlBin.getAbsolutePath(),
                        "load-module",
                        "module-native-protocol-unix",
                        "socket=" + TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH + "/.pulse-socket",
                        "auth-anonymous=1"
                    }, envp, new File(TermuxConstants.TERMUX_HOME_DIR_PATH)).waitFor();
                } catch (Exception ignored) {}
            }
        }

        // 2. Start VirGL Server if installed and not running
        if (virglBin.exists()) {
            virglBin.setExecutable(true);
            try {
                if (!virglDir.exists()) {
                    virglDir.mkdirs();
                }
                virglDir.setReadable(true, false);
                virglDir.setWritable(true, false);
                virglDir.setExecutable(true, false);

                Process checkV = Runtime.getRuntime().exec(new String[]{
                    TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/pgrep", "-f", "virgl_test_server_android"
                });
                if (checkV.waitFor() != 0) {
                    Logger.logInfo(LOG_TAG, "Starting VirGL renderer server...");
                    File socketFile = new File(virglDir, ".virgl_test");
                    if (socketFile.exists()) {
                        socketFile.delete();
                    }

                    boolean hasAngle = new File(TermuxConstants.TERMUX_PREFIX_DIR_PATH + "/opt/angle-android/gl/libEGL_angle.so").exists()
                        || new File(TermuxConstants.TERMUX_PREFIX_DIR_PATH + "/lib/libEGL_angle.so").exists();

                    List<String> virglArgs = new ArrayList<>();
                    virglArgs.add(virglBin.getAbsolutePath());
                    if (hasAngle) {
                        virglArgs.add("--angle-gl");
                    }
                    virglArgs.add("--socket-path");
                    virglArgs.add(socketFile.getAbsolutePath());

                    String[] envp = new String[]{
                        "HOME=" + TermuxConstants.TERMUX_HOME_DIR_PATH,
                        "TMPDIR=" + TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH,
                        "PATH=" + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + ":/system/bin"
                    };
                    Runtime.getRuntime().exec(virglArgs.toArray(new String[0]), envp, new File(TermuxConstants.TERMUX_HOME_DIR_PATH));

                    // Create symlink in /tmp/.virgl_test as well for backward compatibility
                    try {
                        File legacySocket = new File(tmpDir, ".virgl_test");
                        if (!legacySocket.exists()) {
                            Runtime.getRuntime().exec(new String[]{
                                "ln", "-sf", socketFile.getAbsolutePath(), legacySocket.getAbsolutePath()
                            }).waitFor();
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                Logger.logWarn(LOG_TAG, "Failed to start VirGL: " + e.getMessage());
            }
        }
    }

    public static final String DE_PROCESS_PATTERN =
        "xfce4|gnome|plasma|kwin|mate|lxde|lxqt|openbox|fluxbox|cinnamon|dbus-launch|dbus-run-session|dbus-daemon|gvfsd|at-spi|polybar|sxhkd|picom|(\\<i3\\>)|(\\<bspwm\\>)|(\\<awesome\\>)";

    /**
     * Dynamically stops and kills desktop environments and window managers across
     * NetHunter, Droidspaces, PRoot, and custom chroot distributions,
     * without killing the Droidspaces container itself.
     * Also stops VirGL and PulseAudio servers and cleans up their socket bindings.
     */
    public static void killDesktopEnvironment(@NonNull Context context) {
        if (isRootAvailable()) {
            List<ChrootEnvironment> envs = scan();
            for (ChrootEnvironment env : envs) {
                if (env.runnerType == RunnerType.DROIDSPACES) {
                    // Kill lingering DE processes inside Droidspaces container without stopping the container itself (warm standby)
                    String dsKillCmd = "/data/local/Droidspaces/bin/droidspaces --name=\"" + env.containerName
                        + "\" run /bin/sh -c 'pkill -9 -f \"" + DE_PROCESS_PATTERN + "\" 2>/dev/null || true; rm -f /tmp/bspwm* /tmp/.pulse-socket 2>/dev/null || true'; "
                        + "umount -l /data/local/Droidspaces/Containers/\"" + env.containerName + "\"/rootfs/tmp/.X11-unix 2>/dev/null || true; "
                        + "rm -f /data/local/Droidspaces/Containers/\"" + env.containerName + "\"/rootfs/tmp/.virgl_test 2>/dev/null || true";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", dsKillCmd}).waitFor();
                    } catch (Exception ignored) {}
                } else {
                    // NetHunter or custom chroot: kill DE inside chroot, stop dbus, unmount sockets
                    String rootPath = env.rootfs.getAbsolutePath();
                    String chrootKillCmd = "chroot " + rootPath + " /usr/bin/pkill -9 -f \"" + DE_PROCESS_PATTERN + "\" 2>/dev/null || true; "
                        + "if [ -x " + rootPath + "/etc/init.d/dbus ]; then chroot " + rootPath + " /etc/init.d/dbus stop 2>/dev/null || true; fi; "
                        + "umount -l " + rootPath + "/tmp/.virgl 2>/dev/null || true; "
                        + "umount -l " + rootPath + "/tmp/.X11-unix 2>/dev/null || true; "
                        + "rm -f " + rootPath + "/tmp/.virgl_test 2>/dev/null || true";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", chrootKillCmd}).waitFor();
                    } catch (Exception ignored) {}
                }
            }
            // Root-level cleanup of termux-x11, virgl, and lingering socket/pid
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c",
                    "killall -9 termux-x11 2>/dev/null || pkill -9 -f termux-x11 2>/dev/null; "
                    + "killall -9 virgl_test_server_android 2>/dev/null || pkill -9 -f virgl_test_server_android 2>/dev/null; "
                    + "rm -f /data/data/com.termux/files/usr/tmp/termux-x11-wm.pid 2>/dev/null; "
                    + "umount -l /data/local/nhsystem/kali-arm64/tmp/.X11-unix 2>/dev/null || true; "
                    + "umount -l /data/local/Droidspaces/Containers/*/rootfs/tmp/.X11-unix 2>/dev/null || true; "
                    + "umount -l /data/local/nhsystem/kali-arm64/tmp/.virgl 2>/dev/null || true; "
                    + "rm -rf /data/data/com.termux/files/usr/tmp/.virgl 2>/dev/null; "
                    + "rm -f /data/data/com.termux/files/usr/tmp/.virgl_test 2>/dev/null; "
                    + "rm -rf /data/data/com.termux/files/usr/tmp/.X11-unix 2>/dev/null"}).waitFor();
            } catch (Exception ignored) {}
        }

        // Host-level cleanup (VirGL, PRoot / native termux x11 - PulseAudio stays running for continuous audio)
        try {
            Runtime.getRuntime().exec(new String[]{
                TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/pkill", "-9", "-f", "virgl_test_server_android|" + DE_PROCESS_PATTERN + "|termux-x11"
            }).waitFor();
        } catch (Exception ignored) {}
        try {
            Runtime.getRuntime().exec(new String[]{"pkill", "-9", "-f", "virgl_test_server_android|" + DE_PROCESS_PATTERN + "|termux-x11"}).waitFor();
        } catch (Exception ignored) {}
    }

    /**
     * Executes {@link #killDesktopEnvironment(Context)} asynchronously off the main thread.
     */
    public static void killDesktopEnvironmentAsync(@NonNull Context context, @Nullable Runnable onComplete) {
        new Thread(() -> {
            killDesktopEnvironment(context);
            if (onComplete != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(onComplete);
            }
        }, "DE-Killer").start();
    }
}
