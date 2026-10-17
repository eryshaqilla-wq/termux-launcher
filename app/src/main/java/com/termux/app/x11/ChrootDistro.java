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
        new DesktopDef("xfce4", "XFCE 4", Arrays.asList("startxfce4", "xfce4-session")),
        new DesktopDef("gnome", "GNOME", Arrays.asList("gnome-session")),
        new DesktopDef("plasma", "KDE Plasma", Arrays.asList("startplasma-x11", "plasma-desktop")),
        new DesktopDef("mate", "MATE", Arrays.asList("mate-session")),
        new DesktopDef("lxde", "LXDE", Arrays.asList("startlxde", "lxsession")),
        new DesktopDef("lxqt", "LXQt", Arrays.asList("startlxqt")),
        new DesktopDef("cinnamon", "Cinnamon", Arrays.asList("cinnamon-session")),
        new DesktopDef("openbox", "Openbox", Arrays.asList("openbox-session", "openbox")),
        new DesktopDef("i3", "i3 Window Manager", Arrays.asList("i3")),
        new DesktopDef("awesome", "Awesome WM", Arrays.asList("awesome")),
        new DesktopDef("bspwm", "bspwm", Arrays.asList("bspwm", "bspwm-session")),
        new DesktopDef("fluxbox", "Fluxbox", Arrays.asList("startfluxbox", "fluxbox"))
    ));

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
                case "bspwm":
                    return "[ -x /usr/local/bin/bspwm-session ] && exec /usr/local/bin/bspwm-session || (/etc/init.d/dbus start >/dev/null 2>&1 || true; exec dbus-run-session -- bspwm)";
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
                case "bspwm":
                    return "[ -x /usr/local/bin/bspwm-session ] && exec /usr/local/bin/bspwm-session || exec dbus-run-session bspwm";
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

    private static volatile boolean sBridgeSyncStarted = false;

    /**
     * Synchronizes chroot .desktop files and icons into Termux's accessible prefix bridge directory.
     */
    public static void syncChrootBridge() {
        if (!isRootAvailable()) return;
        new Thread(() -> {
            try {
                String cmd = "rm -rf /data/data/com.termux/files/usr/share/applications/chroot/kalifs 2>/dev/null; "
                    + "for root in /data/local/nhsystem/kali-arm64 /data/local/nhsystem/kalifs /data/local/Droidspaces/Containers/*/rootfs; do "
                    + "[ -L \"$root\" ] && continue; "
                    + "if [ -d \"$root/usr/share/applications\" ]; then "
                    + "cname=$(basename \"$(dirname \"$root\")\"); [ \"$cname\" = \"nhsystem\" ] && cname=$(basename \"$root\"); "
                    + "out=\"/data/data/com.termux/files/usr/share/applications/chroot/$cname\"; "
                    + "mkdir -p \"$out\" \"/data/data/com.termux/files/usr/share/applications/chroot/icons\"; "
                    + "cp -u \"$root\"/usr/share/applications/*.desktop \"$out/\" 2>/dev/null || true; "
                    + "cp -ru \"$root\"/usr/share/pixmaps/* \"/data/data/com.termux/files/usr/share/applications/chroot/icons/\" 2>/dev/null || true; "
                    + "uid=$(stat -c '%u' /data/data/com.termux/files/home 2>/dev/null || id -u); "
                    + "chown -R \"$uid:$uid\" \"/data/data/com.termux/files/usr/share/applications/chroot\" 2>/dev/null || true; "
                    + "chmod -R 755 \"/data/data/com.termux/files/usr/share/applications/chroot\" 2>/dev/null || true; "
                    + "fi; done";
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                p.waitFor();
            } catch (Exception ignored) {}
        }, "chroot-bridge-sync").start();
    }

    /**
     * Returns installed chroot environments as ProotDistro.Container items for LinuxAppCatalog scanning.
     */
    @NonNull
    public static List<ProotDistro.Container> installed() {
        if (!isRootAvailable()) return Collections.emptyList();
        if (!sBridgeSyncStarted) {
            sBridgeSyncStarted = true;
            syncChrootBridge();
        }
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
            String configPath = "/data/local/Droidspaces/Containers/" + cName + "/container.config";
            String runCmd = "export DISPLAY=:0 PULSE_SERVER=tcp:127.0.0.1:4713 GALLIUM_DRIVER=virpipe VTEST_SOCKET_NAME=/tmp/.virgl_test; " + command;
            return "su -c \"if ! /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw '" + cName + "'; then "
                + "sed -i 's/enable_termux_x11=1/enable_termux_x11=0/' '" + configPath + "' 2>/dev/null || true; "
                + "if [ -f '" + configPath + "' ]; then /data/local/Droidspaces/bin/droidspaces --config='" + configPath + "' -B /data/data/com.termux/files/usr/tmp/.X11-unix:/tmp/.X11-unix start; "
                + "else /data/local/Droidspaces/bin/droidspaces --name='" + cName + "' -B /data/data/com.termux/files/usr/tmp/.X11-unix:/tmp/.X11-unix start; fi; sleep 2; "
                + "else "
                + "mkdir -p /data/local/Droidspaces/Containers/'" + cName + "'/rootfs/tmp/.X11-unix 2>/dev/null || true; "
                + "(mountpoint /data/local/Droidspaces/Containers/'" + cName + "'/rootfs/tmp/.X11-unix >/dev/null 2>&1 || mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix /data/local/Droidspaces/Containers/'" + cName + "'/rootfs/tmp/.X11-unix 2>/dev/null || true); "
                + "fi; "
                + "/data/local/Droidspaces/bin/droidspaces --name='" + cName + "' run /bin/sh -c " + ProotDistro.singleQuote(runCmd) + "\"";
        } else if (RunnerType.CUSTOM.name().equals(type) && container.customRunner != null
                && !container.customRunner.trim().isEmpty()) {
            return container.customRunner
                .replace("%ROOTFS%", rootPath)
                .replace("%COMMAND%", ProotDistro.singleQuote(command));
        }

        // Default: NetHunter chroot with mount namespace (-mm) and auto-mounted X11 & VirGL unix sockets
        return "su -mm -c \"mkdir -p " + rootPath + "/tmp/.X11-unix 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.X11-unix || mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix " + rootPath + "/tmp/.X11-unix 2>/dev/null); "
            + "mkdir -p " + rootPath + "/tmp/.virgl 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.virgl || mount -o bind /data/data/com.termux/files/usr/tmp/.virgl " + rootPath + "/tmp/.virgl 2>/dev/null); "
            + "ln -sf /tmp/.virgl/.virgl_test " + rootPath + "/tmp/.virgl_test 2>/dev/null || true; "
            + "chroot " + rootPath + " /usr/bin/env -i "
            + "HOME=/root "
            + "USER=root "
            + "LOGNAME=root "
            + "TERM=xterm-256color "
            + "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin "
            + "DISPLAY=${DISPLAY:-:0} "
            + "PULSE_SERVER=tcp:127.0.0.1:4713 "
            + "GALLIUM_DRIVER=virpipe "
            + "MESA_GL_VERSION_OVERRIDE=4.3COMPAT "
            + "MESA_GLES_VERSION_OVERRIDE=3.2 "
            + "MESA_NO_ERROR=1 "
            + "LIBGL_DRI3_DISABLE=1 "
            + "VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test "
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
        String scriptContent = "#!/data/data/com.termux/files/usr/bin/bash\n"
            + "# Droidspaces Desktop Runner for Termux Launcher\n\n"
            + "CNAME=\"" + cName + "\"\n"
            + "X11_DIR=\"" + x11Dir + "\"\n\n"
            + "# 1. Auto-detect nama container jika belum diatur atau kosong\n"
            + "if [ -z \"$CNAME\" ] || [ ! -d \"/data/local/Droidspaces/Containers/$CNAME\" ]; then\n"
            + "    AUTO_C=$(ls -1 /data/local/Droidspaces/Containers/ 2>/dev/null | grep -v '^\\.' | head -n1)\n"
            + "    [ -n \"$AUTO_C\" ] && CNAME=\"$AUTO_C\"\n"
            + "fi\n\n"
            + "CONFIG_PATH=\"/data/local/Droidspaces/Containers/$CNAME/container.config\"\n\n"
            + "mkdir -p \"$X11_DIR\" 2>/dev/null || true\n"
            + "chmod 777 \"$X11_DIR\" 2>/dev/null || true\n\n"
            + "if [ -f \"$CONFIG_PATH\" ]; then\n"
            + "    sed -i 's/enable_termux_x11=1/enable_termux_x11=0/' \"$CONFIG_PATH\" 2>/dev/null || true\n"
            + "fi\n\n"
            + "# 2. Cek apakah container Droidspaces sudah berjalan. Jika belum, start dulu agar tidak layar hitam!\n"
            + "DS_RUNNING=0\n"
            + "if /data/local/Droidspaces/bin/droidspaces pid --name=\"$CNAME\" >/dev/null 2>&1; then\n"
            + "    DS_RUNNING=1\n"
            + "elif /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw \"$CNAME\"; then\n"
            + "    DS_RUNNING=1\n"
            + "fi\n\n"
            + "if [ $DS_RUNNING -eq 0 ]; then\n"
            + "    /data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" start >/dev/null 2>&1 || true\n"
            + "    sleep 2\n"
            + "fi\n\n"
            + "# 3. Pastikan socket X11 ter-mount ke rootfs Droidspaces\n"
            + "mkdir -p \"/data/local/Droidspaces/Containers/$CNAME/rootfs/tmp/.X11-unix\" 2>/dev/null || true\n"
            + "mountpoint \"/data/local/Droidspaces/Containers/$CNAME/rootfs/tmp/.X11-unix\" >/dev/null 2>&1 || mount -o bind \"$X11_DIR\" \"/data/local/Droidspaces/Containers/$CNAME/rootfs/tmp/.X11-unix\" 2>/dev/null || true\n\n"
            + "# 4. Pastikan Termux IPC bridge daemon aktif\n"
            + "if [ -x \"/data/data/com.termux/files/usr/bin/termux-ipc-bridge\" ]; then\n"
            + "    pgrep -f termux-ipc-bridge >/dev/null 2>&1 || /data/data/com.termux/files/usr/bin/termux-ipc-bridge </dev/null >/dev/null 2>&1 &\n"
            + "fi\n\n"
            + "# 5. Tunggu sampai socket X0 muncul\n"
            + "for i in $(seq 1 50); do\n"
            + "    [ -S \"$X11_DIR/X0\" ] && break\n"
            + "    sleep 0.1\n"
            + "done\n"
            + "chmod 777 \"$X11_DIR/X0\" 2>/dev/null || true\n\n"
            + "# 6. Jalankan Desktop Environment di dalam container Droidspaces\n"
            + "/data/local/Droidspaces/bin/droidspaces --name=\"$CNAME\" run /bin/sh -c '\n"
            + "export DISPLAY=:0\n"
            + "export PULSE_SERVER=tcp:127.0.0.1:4713\n"
            + "export GALLIUM_DRIVER=virpipe\n"
            + "export VTEST_SOCKET_NAME=/tmp/.virgl_test\n\n"
            + "for j in $(seq 1 20); do\n"
            + "    xdpyinfo >/dev/null 2>&1 && break\n"
            + "    sleep 0.1\n"
            + "done\n\n"
            + innerCmd + "\n"
            + "'\n\n"
            + "while /data/local/Droidspaces/bin/droidspaces show 2>/dev/null | grep -qw \"$CNAME\"; do\n"
            + "    sleep 3\n"
            + "done\n";

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

        return "su -mm -c \"mkdir -p " + rootPath + "/tmp/.X11-unix 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.X11-unix || mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix " + rootPath + "/tmp/.X11-unix 2>/dev/null); "
            + "mkdir -p " + rootPath + "/tmp/.virgl 2>/dev/null; "
            + "(mountpoint -q " + rootPath + "/tmp/.virgl || mount -o bind /data/data/com.termux/files/usr/tmp/.virgl " + rootPath + "/tmp/.virgl 2>/dev/null); "
            + "ln -sf /tmp/.virgl/.virgl_test " + rootPath + "/tmp/.virgl_test 2>/dev/null || true; "
            + "chroot " + rootPath + " /usr/bin/env -i "
            + "HOME=/root "
            + "USER=root "
            + "LOGNAME=root "
            + "TERM=xterm-256color "
            + "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin "
            + "DISPLAY=${DISPLAY:-:0} "
            + "PULSE_SERVER=tcp:127.0.0.1:4713 "
            + "GALLIUM_DRIVER=virpipe "
            + "MESA_GL_VERSION_OVERRIDE=4.3COMPAT "
            + "MESA_GLES_VERSION_OVERRIDE=3.2 "
            + "MESA_NO_ERROR=1 "
            + "LIBGL_DRI3_DISABLE=1 "
            + "VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test "
            + "XKB_CONFIG_ROOT=/usr/share/X11/xkb "
            + "LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1 "
            + "/bin/bash -c " + ProotDistro.singleQuote(inner) + "\"";
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
                    if ("xfce4".equals(entry.id)) {
                        selectedEntry = entry;
                        break;
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

            if (!isRunning) {
                try {
                    Logger.logInfo(LOG_TAG, "Starting PulseAudio TCP server on port 4713...");
                    String[] cmd = new String[]{
                        pulseBin.getAbsolutePath(),
                        "--start",
                        "--load=module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1",
                        "--exit-idle-time=-1"
                    };
                    String[] envp = new String[]{
                        "HOME=" + TermuxConstants.TERMUX_HOME_DIR_PATH,
                        "TMPDIR=" + TermuxConstants.TERMUX_TMP_PREFIX_DIR_PATH,
                        "PATH=" + TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + ":/system/bin"
                    };
                    Process p = Runtime.getRuntime().exec(cmd, envp, new File(TermuxConstants.TERMUX_HOME_DIR_PATH));
                    p.waitFor();
                } catch (Exception e) {
                    Logger.logWarn(LOG_TAG, "Failed to start PulseAudio: " + e.getMessage());
                }
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
        "xfce4|gnome|plasma|kwin|mate|lxde|lxqt|openbox|fluxbox|cinnamon|dbus-launch|dbus-run-session|polybar|sxhkd|picom|(\\<i3\\>)|(\\<bspwm\\>)|(\\<awesome\\>)";

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
                    // Stop Droidspaces container and kill lingering DE processes
                    String dsKillCmd = "/data/local/Droidspaces/bin/droidspaces --name=\"" + env.containerName
                        + "\" run /bin/sh -c 'pkill -9 -f \"" + DE_PROCESS_PATTERN + "\" 2>/dev/null || true'; "
                        + "/data/local/Droidspaces/bin/droidspaces --name=\"" + env.containerName + "\" stop 2>/dev/null || true";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", dsKillCmd}).waitFor();
                    } catch (Exception ignored) {}
                } else {
                    // NetHunter or custom chroot: kill DE inside chroot, stop dbus, unmount sockets
                    String rootPath = env.rootfs.getAbsolutePath();
                    String chrootKillCmd = "chroot " + rootPath + " /usr/bin/pkill -9 -f \"" + DE_PROCESS_PATTERN + "\" 2>/dev/null || true; "
                        + "if [ -x " + rootPath + "/etc/init.d/dbus ]; then chroot " + rootPath + " /etc/init.d/dbus stop 2>/dev/null || true; fi; "
                        + "(mountpoint -q " + rootPath + "/tmp/.virgl && umount " + rootPath + "/tmp/.virgl 2>/dev/null || true); "
                        + "(mountpoint -q " + rootPath + "/tmp/.X11-unix && umount " + rootPath + "/tmp/.X11-unix 2>/dev/null || true); "
                        + "rm -f " + rootPath + "/tmp/.virgl_test 2>/dev/null || true";
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-mm", "-c", chrootKillCmd}).waitFor();
                    } catch (Exception ignored) {}
                }
            }
            // Root-level cleanup of termux-x11, virgl, and lingering socket/pid
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c",
                    "killall -9 termux-x11 2>/dev/null || pkill -9 -f termux-x11 2>/dev/null; "
                    + "killall -9 virgl_test_server_android 2>/dev/null || pkill -9 -f virgl_test_server_android 2>/dev/null; "
                    + "rm -f /data/data/com.termux/files/usr/tmp/termux-x11-wm.pid 2>/dev/null; "
                    + "rm -rf /data/data/com.termux/files/usr/tmp/.virgl 2>/dev/null; "
                    + "rm -f /data/data/com.termux/files/usr/tmp/.virgl_test 2>/dev/null; "
                    + "rm -rf /data/data/com.termux/files/usr/tmp/.X11-unix 2>/dev/null"}).waitFor();
            } catch (Exception ignored) {}
        }

        // Host-level cleanup (PulseAudio, VirGL, PRoot / native termux x11)
        try {
            Runtime.getRuntime().exec(new String[]{
                TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/pulseaudio", "--kill"
            }).waitFor();
        } catch (Exception ignored) {}
        try {
            Runtime.getRuntime().exec(new String[]{
                TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/pkill", "-9", "-f", "virgl_test_server_android|pulseaudio|" + DE_PROCESS_PATTERN + "|termux-x11"
            }).waitFor();
        } catch (Exception ignored) {}
        try {
            Runtime.getRuntime().exec(new String[]{"pkill", "-9", "-f", "virgl_test_server_android|pulseaudio|" + DE_PROCESS_PATTERN + "|termux-x11"}).waitFor();
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
