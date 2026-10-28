package com.termux.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Vibrator;
import android.widget.Toast;
import com.termux.terminal.TerminalSession;
import com.termux.webview.FusionWebViewDispatcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class TermuxIpcServer {
    public static volatile boolean isRunning;
    public static TermuxActivity sActivity;

    static class KeyboardRunnable implements Runnable {
        private final int mAction;
        private final TermuxActivity mActivity;

        KeyboardRunnable(TermuxActivity termuxActivity, int i) {
            this.mActivity = termuxActivity;
            this.mAction = i;
        }

        @Override // java.lang.Runnable
        public void run() {
            TermuxActivity termuxActivity = this.mActivity;
            if (termuxActivity == null) {
                return;
            }
            int i = this.mAction;
            com.termux.app.terminal.TerminalHost host = termuxActivity.terminalHost();
            if (i == 0 || i == 2) {
                if (host != null && host.hideInAppKeyboard(false)) {
                    return;
                }
                com.termux.shared.view.KeyboardUtils.hideSoftKeyboard(termuxActivity, termuxActivity.getCurrentFocus());
            } else if (i == 1 || i == 3) {
                if (host != null && host.showInAppKeyboard(false)) {
                    return;
                }
                com.termux.shared.view.KeyboardUtils.showSoftKeyboard(termuxActivity, termuxActivity.getTerminalView());
            } else if (i == 4) {
                if (host != null && host.showInAppKeyboard(true)) {
                    return;
                }
            } else if (i == 5) {
                if (host != null && host.hideInAppKeyboard(true)) {
                    return;
                }
            }
        }
    }

    static class ToastRunnable implements Runnable {
        private final Context mContext;
        private final String mText;

        ToastRunnable(Context context, String str) {
            this.mContext = context;
            this.mText = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            Toast.makeText(this.mContext, this.mText, 0).show();
        }
    }

    static class Worker extends Thread {
        private final LocalSocket mSocket;

        Worker(LocalSocket localSocket) {
            this.mSocket = localSocket;
        }

        /* JADX WARN: Code duplicated, block: B:71:0x0172 A[Catch: Throwable -> 0x0188, TryCatch #0 {Throwable -> 0x0188, blocks: (B:2:0x0000, B:3:0x0016, B:5:0x001c, B:8:0x0027, B:10:0x002f, B:11:0x003c, B:13:0x0044, B:14:0x0064, B:16:0x006c, B:72:0x0175, B:17:0x0077, B:19:0x007f, B:20:0x0089, B:22:0x0091, B:23:0x0098, B:25:0x00a0, B:27:0x00ab, B:71:0x0172, B:28:0x00b1, B:30:0x00b9, B:32:0x00c4, B:33:0x00ca, B:35:0x00d2, B:37:0x00d8, B:38:0x00de, B:40:0x00e6, B:42:0x00ec, B:43:0x00f2, B:45:0x00fa, B:50:0x0112, B:52:0x011a, B:57:0x0132, B:59:0x013a, B:64:0x0152, B:66:0x015a, B:68:0x0162, B:70:0x0168, B:61:0x0142, B:63:0x0148, B:54:0x0122, B:56:0x0128, B:47:0x0102, B:49:0x0108, B:73:0x0183), top: B:76:0x0000 }] */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            TermuxActivity activity;
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.mSocket.getInputStream()));
                OutputStream outputStream = this.mSocket.getOutputStream();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        this.mSocket.close();
                        return;
                    }
                    String strTrim = line.trim();
                    if (!strTrim.isEmpty()) {
                        if (strTrim.equalsIgnoreCase("PING")) {
                            outputStream.write("PONG\n".getBytes());
                            outputStream.flush();
                        } else if (strTrim.equalsIgnoreCase("CLIP_GET")) {
                            outputStream.write((TermuxIpcServer.getClipboard() + "\n").getBytes());
                            outputStream.flush();
                        } else {
                            if (strTrim.startsWith("CLIP_SET ")) {
                                TermuxIpcServer.setClipboard(strTrim.substring(9));
                            } else if (strTrim.startsWith("TOAST ")) {
                                TermuxIpcServer.showToast(strTrim.substring(6));
                            } else if (strTrim.equalsIgnoreCase("VIBRATE")) {
                                TermuxIpcServer.vibrate(50);
                            } else if (strTrim.startsWith("RUN ")) {
                                String strSubstring = strTrim.substring(4);
                                TermuxActivity activity2 = TermuxIpcServer.getActivity();
                                if (activity2 != null) {
                                    TermuxIpcServer.injectCommand(activity2, strSubstring, true);
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.startsWith("INPUT ")) {
                                String strSubstring2 = strTrim.substring(6);
                                TermuxActivity activity3 = TermuxIpcServer.getActivity();
                                if (activity3 != null) {
                                    TermuxIpcServer.injectCommand(activity3, strSubstring2, false);
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("MOUSE_OFF")) {
                                TermuxActivity activity4 = TermuxIpcServer.getActivity();
                                if (activity4 != null) {
                                    activity4.setTouchMouseModeInternal(false);
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("MOUSE_ON")) {
                                TermuxActivity activity5 = TermuxIpcServer.getActivity();
                                if (activity5 != null) {
                                    activity5.setTouchMouseModeInternal(true);
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("KEYBOARD_DISABLE") || strTrim.equalsIgnoreCase("DISABLE")) {
                                TermuxActivity activity6 = TermuxIpcServer.getActivity();
                                if (activity6 != null) {
                                    activity6.runOnUiThread(new KeyboardRunnable(activity6, 0));
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("KEYBOARD_ENABLE") || strTrim.equalsIgnoreCase("ENABLE")) {
                                TermuxActivity activity7 = TermuxIpcServer.getActivity();
                                if (activity7 != null) {
                                    activity7.runOnUiThread(new KeyboardRunnable(activity7, 1));
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("KEYBOARD_HIDE") || strTrim.equalsIgnoreCase("HIDE")) {
                                TermuxActivity activity8 = TermuxIpcServer.getActivity();
                                if (activity8 != null) {
                                    activity8.runOnUiThread(new KeyboardRunnable(activity8, 2));
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("KEYBOARD_FOCUS_IN") || strTrim.equalsIgnoreCase("FOCUS_IN")) {
                                TermuxActivity actFocusIn = TermuxIpcServer.getActivity();
                                if (actFocusIn != null) {
                                    actFocusIn.runOnUiThread(new KeyboardRunnable(actFocusIn, 4));
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("KEYBOARD_FOCUS_OUT") || strTrim.equalsIgnoreCase("FOCUS_OUT")) {
                                TermuxActivity actFocusOut = TermuxIpcServer.getActivity();
                                if (actFocusOut != null) {
                                    actFocusOut.runOnUiThread(new KeyboardRunnable(actFocusOut, 5));
                                } else {
                                    FusionWebViewDispatcher.dispatch(strTrim);
                                }
                            } else if (strTrim.equalsIgnoreCase("LAUNCHERCTL_ON")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act != null) {
                                    com.termux.launcherctl.LauncherCtlApiServer.getInstance().setEnabled(act, true);
                                }
                            } else if (strTrim.equalsIgnoreCase("LAUNCHERCTL_OFF")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act != null) {
                                    com.termux.launcherctl.LauncherCtlApiServer.getInstance().setEnabled(act, false);
                                }
                            } else if (strTrim.equalsIgnoreCase("LAUNCHERCTL_STATUS")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                boolean on = act != null && com.termux.launcherctl.LauncherCtlApiServer.getInstance().isEnabled(act);
                                outputStream.write((on ? "ON\n" : "OFF\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("TAB_NEW ") || strTrim.equals("TAB_NEW")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act == null) {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"TermuxActivity not available\"}\n".getBytes());
                                    outputStream.flush();
                                    continue;
                                }
                                String argStr = strTrim.length() > 7 ? strTrim.substring(7).trim() : "";
                                String tabName = "";
                                String cmdStr = null;
                                String cwd = null;
                                String env = "auto";
                                boolean focus = true;
                                boolean stayBehind = true;

                                if (argStr.startsWith("{") && argStr.endsWith("}")) {
                                    try {
                                        JSONObject j = new JSONObject(argStr);
                                        tabName = j.optString("name", "");
                                        if (j.has("command")) cmdStr = j.optString("command", null);
                                        if (j.has("cwd")) cwd = j.optString("cwd", null);
                                        if (j.has("env")) env = j.optString("env", "auto");
                                        focus = j.optBoolean("focus", true);
                                        stayBehind = j.optBoolean("stayBehind", true);
                                    } catch (Exception ignored) {}
                                } else if (!argStr.isEmpty()) {
                                    // Parse flags: --chroot, --proot, --native, --env <target>
                                    if (argStr.contains("--chroot")) {
                                        env = "chroot";
                                        argStr = argStr.replace("--chroot", " ").trim();
                                    } else if (argStr.contains("--proot")) {
                                        env = "proot";
                                        argStr = argStr.replace("--proot", " ").trim();
                                    } else if (argStr.contains("--native") || argStr.contains("--termux")) {
                                        env = "native";
                                        argStr = argStr.replace("--native", " ").replace("--termux", " ").trim();
                                    } else if (argStr.contains("--env ")) {
                                        int eIdx = argStr.indexOf("--env ");
                                        String rem = argStr.substring(eIdx + 6).trim();
                                        int sp = rem.indexOf(" ");
                                        if (sp != -1) {
                                            env = rem.substring(0, sp).trim();
                                            argStr = (argStr.substring(0, eIdx) + " " + rem.substring(sp)).trim();
                                        } else {
                                            env = rem.trim();
                                            argStr = argStr.substring(0, eIdx).trim();
                                        }
                                    }

                                    if (argStr.startsWith("\"")) {
                                        int endQuote = argStr.indexOf("\"", 1);
                                        if (endQuote != -1) {
                                            tabName = argStr.substring(1, endQuote).trim();
                                            cmdStr = argStr.substring(endQuote + 1).trim();
                                        } else {
                                            tabName = argStr.substring(1).trim();
                                        }
                                    } else {
                                        int firstSpace = argStr.indexOf(" ");
                                        if (firstSpace != -1) {
                                            tabName = argStr.substring(0, firstSpace).trim();
                                            cmdStr = argStr.substring(firstSpace + 1).trim();
                                        } else {
                                            tabName = argStr.trim();
                                        }
                                    }
                                    if (cmdStr != null && cmdStr.isEmpty()) cmdStr = null;
                                }

                                final String finalName = tabName;
                                final List<String> cmdList = new ArrayList<>();
                                String cleanEnv = env.toLowerCase().trim();

                                if ("native".equals(cleanEnv) || "termux".equals(cleanEnv)) {
                                    if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add("sh");
                                        cmdList.add("-c");
                                        cmdList.add(cmdStr.trim());
                                    }
                                } else if ("chroot".equals(cleanEnv) || "nethunter".equals(cleanEnv)) {
                                    cmdList.add("bashnh");
                                    cmdList.add("chroot");
                                    if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add(cmdStr.trim());
                                    }
                                } else if ("droidspaces".equals(cleanEnv) || "ubuntu".equals(cleanEnv)) {
                                    cmdList.add("bashnh");
                                    cmdList.add("droidspaces");
                                    if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add(cmdStr.trim());
                                    }
                                } else if ("proot".equals(cleanEnv)) {
                                    cmdList.add("bashnh");
                                    cmdList.add("proot");
                                    if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add(cmdStr.trim());
                                    }
                                } else {
                                    // "auto" (Default): Jalankan bashnh agar otomatis masuk ke distro/chroot aktif
                                    cmdList.add("bashnh");
                                    if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add(cmdStr.trim());
                                    }
                                }
                                final String finalCwd = cwd;
                                final boolean finalFocus = focus;
                                final boolean finalStayBehind = stayBehind;

                                final AtomicReference<String> resultRef =
                                    new AtomicReference<>("{\"status\":\"error\",\"message\":\"timeout\"}");
                                final CountDownLatch latch = new CountDownLatch(1);

                                act.runOnUiThread(() -> {
                                    try {
                                        com.termux.app.terminal.TerminalPaneController.Window w =
                                            act.openNamedWindow(finalName, cmdList, finalCwd, finalFocus, finalStayBehind);
                                        if (w != null) {
                                            JSONObject res = new JSONObject();
                                            res.put("status", "ok");
                                            res.put("id", w.id);
                                            res.put("name", w.name != null ? w.name : "");
                                            int index = act.mCurrentWSession != null ? act.mCurrentWSession.windows.indexOf(w) : -1;
                                            res.put("index", index);
                                            resultRef.set(res.toString());
                                        } else {
                                            resultRef.set("{\"status\":\"error\",\"message\":\"Failed to open window\"}");
                                        }
                                    } catch (Exception e) {
                                        resultRef.set("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
                                    } finally {
                                        latch.countDown();
                                    }
                                });

                                try {
                                    latch.await(3, TimeUnit.SECONDS);
                                } catch (InterruptedException ignored) {}

                                outputStream.write((resultRef.get() + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equalsIgnoreCase("TAB_LIST") || strTrim.equalsIgnoreCase("TAB_LS")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act == null) {
                                    outputStream.write("[]\n".getBytes());
                                    outputStream.flush();
                                    continue;
                                }
                                final AtomicReference<String> resultRef = new AtomicReference<>("[]");
                                final CountDownLatch latch = new CountDownLatch(1);

                                act.runOnUiThread(() -> {
                                    try {
                                        resultRef.set(act.listWindowsJson().toString());
                                    } catch (Exception ignored) {
                                    } finally {
                                        latch.countDown();
                                    }
                                });

                                try {
                                    latch.await(2, TimeUnit.SECONDS);
                                } catch (InterruptedException ignored) {}

                                outputStream.write((resultRef.get() + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("TAB_CLOSE ") || strTrim.equalsIgnoreCase("TAB_CLOSE")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act == null) {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"TermuxActivity not available\"}\n".getBytes());
                                    outputStream.flush();
                                    continue;
                                }
                                final String target = strTrim.length() > 9 ? strTrim.substring(9).trim() : "current";
                                final AtomicBoolean closedRef = new AtomicBoolean(false);
                                final CountDownLatch latch = new CountDownLatch(1);

                                act.runOnUiThread(() -> {
                                    try {
                                        closedRef.set(act.closeNamedWindow(target));
                                    } catch (Exception ignored) {
                                    } finally {
                                        latch.countDown();
                                    }
                                });

                                try {
                                    latch.await(2, TimeUnit.SECONDS);
                                } catch (InterruptedException ignored) {}

                                if (closedRef.get()) {
                                    outputStream.write("{\"status\":\"ok\",\"closed\":true}\n".getBytes());
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Window not found or could not be closed\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("TAB_FOCUS ") || strTrim.startsWith("TAB_SWITCH ")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act == null) {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"TermuxActivity not available\"}\n".getBytes());
                                    outputStream.flush();
                                    continue;
                                }
                                final String target = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                final AtomicBoolean focusRef = new AtomicBoolean(false);
                                final CountDownLatch latch = new CountDownLatch(1);

                                act.runOnUiThread(() -> {
                                    try {
                                        focusRef.set(act.focusNamedWindow(target));
                                    } catch (Exception ignored) {
                                    } finally {
                                        latch.countDown();
                                    }
                                });

                                try {
                                    latch.await(2, TimeUnit.SECONDS);
                                } catch (InterruptedException ignored) {}

                                if (focusRef.get()) {
                                    outputStream.write("{\"status\":\"ok\",\"focused\":true}\n".getBytes());
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Window not found\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("TAB_RENAME ")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act == null) {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"TermuxActivity not available\"}\n".getBytes());
                                    outputStream.flush();
                                    continue;
                                }
                                String rem = strTrim.substring(11).trim();
                                int sp = rem.indexOf(" ");
                                final String target = sp != -1 ? rem.substring(0, sp).trim() : "current";
                                final String newName = sp != -1 ? rem.substring(sp + 1).trim() : rem;
                                final AtomicBoolean renameRef = new AtomicBoolean(false);
                                final CountDownLatch latch = new CountDownLatch(1);

                                act.runOnUiThread(() -> {
                                    try {
                                        renameRef.set(act.renameNamedWindow(target, newName));
                                    } catch (Exception ignored) {
                                    } finally {
                                        latch.countDown();
                                    }
                                });

                                try {
                                    latch.await(2, TimeUnit.SECONDS);
                                } catch (InterruptedException ignored) {}

                                if (renameRef.get()) {
                                    outputStream.write("{\"status\":\"ok\",\"renamed\":true}\n".getBytes());
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Window not found\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if ((strTrim.equalsIgnoreCase("KEYBOARD_SHOW") || strTrim.equalsIgnoreCase("SHOW")) && (activity = TermuxIpcServer.getActivity()) != null) {
                                activity.runOnUiThread(new KeyboardRunnable(activity, 3));
                            } else {
                                FusionWebViewDispatcher.dispatch(strTrim);
                            }
                            outputStream.write("OK\n".getBytes());
                            outputStream.flush();
                        }
                    }
                }
            } catch (Throwable unused) {
            }
        }
    }

    public static TermuxActivity getActivity() {
        return sActivity;
    }

    public static String getClipboard() {
        ClipboardManager clipboardManager;
        ClipData primaryClip;
        ClipData.Item itemAt;
        CharSequence text;
        try {
            TermuxActivity termuxActivity = sActivity;
            return (termuxActivity == null || (clipboardManager = (ClipboardManager) termuxActivity.getSystemService("clipboard")) == null || (primaryClip = clipboardManager.getPrimaryClip()) == null || primaryClip.getItemCount() <= 0 || (itemAt = primaryClip.getItemAt(0)) == null || (text = itemAt.getText()) == null) ? "" : text.toString();
        } catch (Throwable unused) {
            return "";
        }
    }

    public static void injectCommand(TermuxActivity termuxActivity, String str, boolean z) {
        if (termuxActivity == null || str == null) {
            return;
        }
        try {
            if (isCommandDestructive(str)) {
                TerminalSession currentSession = termuxActivity.getCurrentSession();
                if (currentSession != null) {
                    currentSession.write("\r\n[SECURITY GUARD] DILARANG: Command terdeteksi destruktif & diblokir!\r\n");
                    return;
                }
                return;
            }
            TerminalSession currentSession2 = termuxActivity.getCurrentSession();
            if (currentSession2 != null) {
                currentSession2.write(str);
                if (z) {
                    currentSession2.write("\n");
                }
            }
        } catch (Throwable th) {
        }
    }

    public static boolean isCommandDestructive(String str) {
        if (str == null) {
            return false;
        }
        String strTrim = str.toLowerCase().trim();
        return ((strTrim.contains("rm ") || strTrim.contains("rm\t")) && ((strTrim.contains("-r") || strTrim.contains("-f")) && (strTrim.contains(" /") || strTrim.contains(" ~") || strTrim.contains(" *") || strTrim.contains(" $prefix")))) || strTrim.contains("mkfs") || strTrim.contains("dd if=") || strTrim.contains(":(){ :|:& };:");
    }

    public static boolean isServerRunning() {
        return isRunning;
    }

    public static void setClipboard(String str) {
        ClipboardManager clipboardManager;
        try {
            TermuxActivity termuxActivity = sActivity;
            if (termuxActivity == null || str == null || (clipboardManager = (ClipboardManager) termuxActivity.getSystemService("clipboard")) == null) {
                return;
            }
            clipboardManager.setPrimaryClip(ClipData.newPlainText("termux", str));
        } catch (Throwable unused) {
        }
    }

    public static void showToast(String str) {
        try {
            TermuxActivity r0 = sActivity;
            if (r0 != null && str != null) {
                r0.runOnUiThread(new ToastRunnable(r0, str));
            }
        } catch (Throwable unused) {
        }
    }

    public static synchronized void startServer(TermuxActivity termuxActivity) {
        try {
            synchronized (TermuxIpcServer.class) {
                sActivity = termuxActivity;
                if (isRunning) {
                    return;
                }
                isRunning = true;
                new Thread() { // from class: com.termux.app.TermuxIpcServer.1
                    @Override // java.lang.Thread, java.lang.Runnable
                    public void run() {
                        try {
                            LocalServerSocket localServerSocket = new LocalServerSocket("termux_ipc");
                            while (TermuxIpcServer.isServerRunning()) {
                                LocalSocket localSocketAccept = localServerSocket.accept();
                                if (localSocketAccept != null) {
                                    new Worker(localSocketAccept).start();
                                }
                            }
                            localServerSocket.close();
                        } catch (Throwable unused) {
                        }
                    }
                }.start();
            }
        } catch (Throwable th) {
        }
    }

    public static void vibrate(int i) {
        Vibrator vibrator;
        try {
            TermuxActivity termuxActivity = sActivity;
            if (termuxActivity == null || (vibrator = (Vibrator) termuxActivity.getSystemService("vibrator")) == null) {
                return;
            }
            vibrator.vibrate(i);
        } catch (Throwable unused) {
        }
    }
}
