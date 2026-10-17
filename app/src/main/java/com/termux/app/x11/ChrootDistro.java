package com.termux.app.x11;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
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
            return "su -c '/data/local/Droidspaces/bin/droidspaces --name=\"" + cName
                + "\" enter -- /bin/sh -c " + ProotDistro.singleQuote(command) + "'";
        } else if (RunnerType.CUSTOM.name().equals(type) && container.customRunner != null
                && !container.customRunner.trim().isEmpty()) {
            return container.customRunner
                .replace("%ROOTFS%", rootPath)
                .replace("%COMMAND%", ProotDistro.singleQuote(command));
        }

        // Default: NetHunter chroot with mount namespace (-mm) and auto-mounted X11 unix socket
        return "su -mm -c \"mkdir -p " + rootPath + "/tmp/.X11-unix 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.X11-unix || mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix " + rootPath + "/tmp/.X11-unix 2>/dev/null); "
            + "chroot " + rootPath + " /usr/bin/env -i "
            + "HOME=/root "
            + "USER=root "
            + "LOGNAME=root "
            + "TERM=xterm-256color "
            + "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin "
            + "DISPLAY=${DISPLAY:-:0} "
            + "PULSE_SERVER=tcp:127.0.0.1:4713 "
            + "XKB_CONFIG_ROOT=/usr/share/X11/xkb "
            + "LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 "
            + "DISABLE_AUTO_TITLE=true "
            + "/bin/sh -c " + ProotDistro.singleQuote(command) + "\"";
    }

    /**
     * Builds the desktop startup command for the active Chroot environment (to run on Start display).
     */
    @NonNull
    public static String getDesktopCommand(@Nullable ChrootEnvironment env) {
        if (env != null && env.runnerType == RunnerType.DROIDSPACES) {
            return "su -c '/data/local/Droidspaces/bin/droidspaces --name=\"" + env.containerName
                + "\" --termux-x11 --virgl enter -- /bin/sh -c \\\"export DISPLAY=:0 && dbus-run-session xfce4-session\\\"'";
        } else if (env != null && env.runnerType == RunnerType.CUSTOM && !env.customRunner.trim().isEmpty()) {
            return env.customRunner
                .replace("%ROOTFS%", env.rootfs.getAbsolutePath())
                .replace("%COMMAND%", "'export DISPLAY=:0 && dbus-run-session xfce4-session'");
        }

        String rootPath = (env != null) ? env.rootfs.getAbsolutePath() : "/data/local/nhsystem/kali-arm64";
        return "su -mm -c \"mkdir -p " + rootPath + "/tmp/.X11-unix 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.X11-unix || mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix " + rootPath + "/tmp/.X11-unix 2>/dev/null); "
            + "chroot " + rootPath + " /usr/bin/env -i "
            + "HOME=/root "
            + "USER=root "
            + "TERM=xterm-256color "
            + "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin "
            + "DISPLAY=${DISPLAY:-:0} "
            + "PULSE_SERVER=tcp:127.0.0.1:4713 "
            + "XKB_CONFIG_ROOT=/usr/share/X11/xkb "
            + "LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 "
            + "/bin/bash -c '/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-launch --exit-with-session startxfce4'\"";
    }
}
