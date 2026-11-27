package com.termux.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Vibrator;
import android.widget.Toast;
import com.termux.R;
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
            FusionToast.show(this.mContext, this.mText);
        }
    }

    static class Worker extends Thread {
        private final LocalSocket mSocket;

        Worker(LocalSocket localSocket) {
            this.mSocket = localSocket;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.mSocket.getInputStream()));
                OutputStream outputStream = this.mSocket.getOutputStream();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        this.mSocket.close();
                        return;
                    }
                    String resp = TermuxIpcServer.processCommand(line);
                    if (resp != null && !resp.isEmpty()) {
                        outputStream.write(resp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                }
            } catch (Throwable unused) {
            }
        }
    }

    public static String processCommand(String line) {
        if (line == null) return "";
        String strTrim = line.trim();
        if (strTrim.isEmpty()) return "";
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            OutputStream outputStream = baos;
            TermuxActivity activity;
            do {
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
                            } else if (strTrim.startsWith("TAB_RUN ")) {
                                String cmd = strTrim.substring(8).trim();
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act != null && !cmd.isEmpty()) {
                                    act.runOnUiThread(() -> act.launchProgramInNewTabString(cmd, null, "auto"));
                                }
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
                                    // "auto" (Default): Auto-detect OS & container via bashnh
                                    boolean hasBashnh = new java.io.File("/data/data/com.termux/files/usr/bin/bashnh").exists()
                                        || new java.io.File(act.getFilesDir(), "usr/bin/bashnh").exists();
                                    if (hasBashnh) {
                                        cmdList.add("bashnh");
                                        if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                            cmdList.add(cmdStr.trim());
                                        }
                                    } else if (cmdStr != null && !cmdStr.trim().isEmpty()) {
                                        cmdList.add("sh");
                                        cmdList.add("-c");
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
                                            int index = act.getWindowIndexOf(w);
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
                            } else if (strTrim.startsWith("LEFT_CANVAS_KEYS") || strTrim.startsWith("LEFT_CANVAS keys")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act != null) {
                                    String sub = strTrim.contains(" ") ? strTrim.substring(strTrim.indexOf(" ") + 1).trim() : "";
                                    if (sub.startsWith("keys ")) sub = sub.substring(5).trim();
                                    if (sub.equalsIgnoreCase("list") || sub.isEmpty()) {
                                        String json = act.getLeftCanvasQuickKeysList() != null
                                            ? com.termux.app.terminal.io.ExtraKeysLayoutModel.serializeKeyList(act.getLeftCanvasQuickKeysList())
                                            : "[]";
                                        outputStream.write((json + "\n").getBytes());
                                    } else if (sub.equalsIgnoreCase("reset")) {
                                        if (act.getPreferences() != null) {
                                            act.getPreferences().setLeftCanvasQuickExtraKeys(
                                                com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants.TERMUX_APP.DEFAULT_LEFT_CANVAS_QUICK_EXTRA_KEYS);
                                        }
                                        act.runOnUiThread(() -> {
                                            android.view.ViewGroup cluster = act.findViewById(R.id.left_canvas_quick_keys_cluster);
                                            if (cluster != null) act.populateLeftCanvasQuickKeys(cluster);
                                        });
                                        outputStream.write("{\"status\":\"ok\",\"action\":\"reset\"}\n".getBytes());
                                    } else if (sub.startsWith("set ")) {
                                        String val = sub.substring(4).trim();
                                        if (act.getPreferences() != null) {
                                            act.getPreferences().setLeftCanvasQuickExtraKeys(val);
                                        }
                                        act.runOnUiThread(() -> {
                                            android.view.ViewGroup cluster = act.findViewById(R.id.left_canvas_quick_keys_cluster);
                                            if (cluster != null) act.populateLeftCanvasQuickKeys(cluster);
                                        });
                                        outputStream.write("{\"status\":\"ok\",\"action\":\"set\"}\n".getBytes());
                                    } else if (sub.startsWith("add ")) {
                                        String keyJson = sub.substring(4).trim();
                                        java.util.List<com.termux.app.terminal.io.ExtraKeysLayoutModel.Key> list = act.getLeftCanvasQuickKeysList();
                                        java.util.List<com.termux.app.terminal.io.ExtraKeysLayoutModel.Key> toAdd =
                                            com.termux.app.terminal.io.ExtraKeysLayoutModel.parseKeyList(keyJson);
                                        if (!toAdd.isEmpty()) {
                                            list.addAll(toAdd);
                                            act.saveLeftCanvasQuickKeysList(list);
                                            act.runOnUiThread(() -> {
                                                android.view.ViewGroup cluster = act.findViewById(R.id.left_canvas_quick_keys_cluster);
                                                if (cluster != null) act.populateLeftCanvasQuickKeys(cluster);
                                            });
                                            outputStream.write(("{\"status\":\"ok\",\"action\":\"add\",\"count\":" + list.size() + "}\n").getBytes());
                                        } else {
                                            outputStream.write("{\"status\":\"error\",\"message\":\"Invalid key json\"}\n".getBytes());
                                        }
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Unknown keys action\"}\n".getBytes());
                                    }
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("AIRPLANE_MODE") || strTrim.startsWith("AIRPLANE")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                if (act != null) {
                                    String sub = strTrim.contains(" ") ? strTrim.substring(strTrim.indexOf(" ") + 1).trim().toLowerCase(java.util.Locale.ROOT) : "toggle";
                                    if (sub.equals("status") || sub.equals("check") || sub.equals("get")) {
                                        boolean on = FusionAirplaneModeHelper.isAirplaneModeOn(act);
                                        FusionAirplaneModeHelper.Mode mode = FusionAirplaneModeHelper.detectMode(act);
                                        String resp = "{\"status\":\"ok\",\"mode\":\"" + mode.name().toLowerCase(java.util.Locale.ROOT)
                                            + "\",\"airplane_mode\":" + on + "}\n";
                                        outputStream.write(resp.getBytes());
                                    } else {
                                        boolean targetOn;
                                        if (sub.equals("on") || sub.equals("enable") || sub.equals("1")) {
                                            targetOn = true;
                                        } else if (sub.equals("off") || sub.equals("disable") || sub.equals("0")) {
                                            targetOn = false;
                                        } else {
                                            targetOn = !FusionAirplaneModeHelper.isAirplaneModeOn(act);
                                        }

                                        final Object waitLock = new Object();
                                        final String[] resultJson = new String[1];
                                        FusionAirplaneModeHelper.setAirplaneMode(act, targetOn, (success, mode, message, state) -> {
                                            synchronized (waitLock) {
                                                resultJson[0] = "{\"status\":\"" + (success ? "ok" : "error")
                                                    + "\",\"mode\":\"" + mode.name().toLowerCase(java.util.Locale.ROOT)
                                                    + "\",\"airplane_mode\":" + state
                                                    + ",\"message\":\"" + message + "\"}\n";
                                                waitLock.notifyAll();
                                            }
                                        });

                                        synchronized (waitLock) {
                                            try {
                                                if (resultJson[0] == null) {
                                                    waitLock.wait(4000);
                                                }
                                            } catch (InterruptedException ignored) {}
                                        }

                                        if (resultJson[0] != null) {
                                            outputStream.write(resultJson[0].getBytes());
                                        } else {
                                            outputStream.write("{\"status\":\"error\",\"message\":\"Timeout toggling airplane mode\"}\n".getBytes());
                                        }
                                    }
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("BACKUP_SLOT") || strTrim.startsWith("SLOT")) {
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                String sub = strTrim.contains(" ") ? strTrim.substring(strTrim.indexOf(" ") + 1).trim() : "get";
                                String subLower = sub.toLowerCase(java.util.Locale.ROOT);

                                if (subLower.equals("get") || subLower.equals("status") || subLower.equals("current")) {
                                    String curr = FusionBackupSlotHelper.getCurrentSlotCached();
                                    outputStream.write(("{\"status\":\"ok\",\"current_slot\":\"" + curr + "\"}\n").getBytes());
                                } else if (subLower.equals("list") || subLower.equals("all")) {
                                    String curr = FusionBackupSlotHelper.getCurrentSlotCached();
                                    outputStream.write(("{\"status\":\"ok\",\"current_slot\":\"" + curr + "\"}\n").getBytes());
                                } else if (subLower.equals("next")) {
                                    if (act != null) {
                                        final Object waitLock = new Object();
                                        FusionBackupSlotHelper.nextSlot(act, () -> {
                                            act.refreshAllQuickKeysClusters();
                                            synchronized (waitLock) {
                                                waitLock.notifyAll();
                                            }
                                        });
                                        synchronized (waitLock) {
                                            try { waitLock.wait(4000); } catch (InterruptedException ignored) {}
                                        }
                                        String curr = FusionBackupSlotHelper.getCurrentSlotCached();
                                        outputStream.write(("{\"status\":\"ok\",\"action\":\"next\",\"current_slot\":\"" + curr + "\"}\n").getBytes());
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                    }
                                } else if (subLower.equals("prev") || subLower.equals("previous")) {
                                    if (act != null) {
                                        final Object waitLock = new Object();
                                        FusionBackupSlotHelper.prevSlot(act, () -> {
                                            act.refreshAllQuickKeysClusters();
                                            synchronized (waitLock) {
                                                waitLock.notifyAll();
                                            }
                                        });
                                        synchronized (waitLock) {
                                            try { waitLock.wait(4000); } catch (InterruptedException ignored) {}
                                        }
                                        String curr = FusionBackupSlotHelper.getCurrentSlotCached();
                                        outputStream.write(("{\"status\":\"ok\",\"action\":\"prev\",\"current_slot\":\"" + curr + "\"}\n").getBytes());
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                    }
                                } else if (subLower.equals("pick") || subLower.equals("picker")) {
                                    if (act != null) {
                                        act.runOnUiThread(() -> FusionBackupSlotHelper.showSlotPicker(act, (picked) -> act.refreshAllQuickKeysClusters()));
                                        outputStream.write("{\"status\":\"ok\",\"message\":\"Slot picker dialog opened\"}\n".getBytes());
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                    }
                                } else if (subLower.equals("app") || subLower.equals("backup-app")) {
                                    if (act != null) {
                                        FusionBackupSlotHelper.openBackupAppPicker(act);
                                        outputStream.write("{\"status\":\"ok\",\"message\":\"Launched .backup app picker activity\"}\n".getBytes());
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                    }
                                } else {
                                    String target = subLower.startsWith("set ") ? sub.substring(4).trim()
                                        : (subLower.startsWith("switch ") ? sub.substring(7).trim() : sub);
                                    if (act != null) {
                                        final Object waitLock = new Object();
                                        FusionBackupSlotHelper.switchToSlot(act, target, () -> {
                                            act.refreshAllQuickKeysClusters();
                                            synchronized (waitLock) {
                                                waitLock.notifyAll();
                                            }
                                        });
                                        synchronized (waitLock) {
                                            try { waitLock.wait(4000); } catch (InterruptedException ignored) {}
                                        }
                                        outputStream.write(("{\"status\":\"ok\",\"action\":\"set\",\"current_slot\":\"" + target + "\"}\n").getBytes());
                                    } else {
                                        outputStream.write("{\"status\":\"error\",\"message\":\"Activity not ready\"}\n".getBytes());
                                    }
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_SAVE_SESSION ") || strTrim.startsWith("WEBVIEW_SAVE ")) {
                                String prof = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                TermuxActivity act = TermuxIpcServer.getActivity();
                                String res = com.termux.webview.FusionProfileManager.exportProfileSessionToJson(act, prof);
                                if (res != null) {
                                    outputStream.write((res + "\n").getBytes());
                                } else {
                                    outputStream.write(("Gagal menyimpan sesi untuk profil [" + prof + "]\n").getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_DEFAULT_ENGINE") || strTrim.equals("WEBVIEW_GET_DEFAULT_ENGINE")
                                    || strTrim.startsWith("WEBVIEW_DEFAULT_ENGINE ") || strTrim.startsWith("WEBVIEW_ENGINE_DEFAULT ")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    int sp = strTrim.indexOf(' ');
                                    String arg = sp > 0 ? strTrim.substring(sp + 1).trim() : "";
                                    if (!arg.isEmpty() && !"status".equalsIgnoreCase(arg) && !"get".equalsIgnoreCase(arg)) {
                                        mgr.setDefaultEngine(arg);
                                    }
                                    outputStream.write((mgr.getDefaultEngine() + "\n").getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_LIST") || strTrim.equals("WEBVIEW_GET_TABS")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                String res = mgr != null ? mgr.getTabsJson() : "[]";
                                outputStream.write((res + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_ATTACH ") || strTrim.equals("WEBVIEW_ATTACH")) {
                                String idxStr = strTrim.length() > 14 ? strTrim.substring(14).trim() : "";
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    int idx = 0;
                                    try { if (!idxStr.isEmpty()) idx = Integer.parseInt(idxStr); } catch (Exception ignored) {}
                                    mgr.attachTab(idx);
                                    outputStream.write("OK\n".getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_DETACH ") || strTrim.equals("WEBVIEW_DETACH") || strTrim.startsWith("WEBVIEW_DETACH_TAB ") || strTrim.equals("WEBVIEW_DETACH_TAB")) {
                                String idxStr = "";
                                if (strTrim.startsWith("WEBVIEW_DETACH_TAB ")) idxStr = strTrim.substring(18).trim();
                                else if (strTrim.startsWith("WEBVIEW_DETACH ")) idxStr = strTrim.substring(14).trim();
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    int idx = -1;
                                    try { if (!idxStr.isEmpty()) idx = Integer.parseInt(idxStr); } catch (Exception ignored) {}
                                    mgr.detachTab(idx);
                                    outputStream.write("OK\n".getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_KILL_TAB ") || strTrim.equals("WEBVIEW_KILL_TAB")) {
                                String idxStr = strTrim.length() > 16 ? strTrim.substring(16).trim() : "";
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    if (idxStr.isEmpty()) {
                                        mgr.killActiveTab();
                                    } else {
                                        try {
                                            mgr.killTab(Integer.parseInt(idxStr));
                                        } catch (Exception ignored) {
                                            mgr.killActiveTab();
                                        }
                                    }
                                    outputStream.write("OK\n".getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_KILL_ALL")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    mgr.killAllTabs();
                                    outputStream.write("OK\n".getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_STATE") || strTrim.equals("WEBVIEW_GET_STATE")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                String res = mgr != null ? mgr.getWebviewStateJson() : "{}";
                                outputStream.write((res + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_EVAL_SYNC ")) {
                                String js = strTrim.substring(18);
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    String res = mgr.evaluateJsSync(js, 4000);
                                    outputStream.write(((res != null ? res : "null") + "\n").getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_TOUCH ") || strTrim.startsWith("WEBVIEW_NATIVE_CLICK ")) {
                                String coords = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                String[] parts = coords.split("[,\\s]+");
                                if (parts.length >= 2) {
                                    try {
                                        float x = Float.parseFloat(parts[0]);
                                        float y = Float.parseFloat(parts[1]);
                                        com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                        if (mgr != null) {
                                            mgr.dispatchNativeTouch(x, y);
                                            outputStream.write("OK\n".getBytes());
                                        } else {
                                            outputStream.write("ERROR\n".getBytes());
                                        }
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid coordinates\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WALLET_SET ") || strTrim.startsWith("WEBVIEW_SET_WALLET ")) {
                                String rest = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                String[] wParts = rest.split("\\s+");
                                String wName = wParts.length > 0 ? wParts[0] : "";
                                String wAddr = wParts.length > 1 ? wParts[1] : null;
                                String wChain = wParts.length > 2 ? wParts[2] : null;
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    boolean ok = (wAddr != null && !wAddr.isEmpty())
                                            ? mgr.switchWalletDirect(wName, wAddr, wChain)
                                            : mgr.switchWalletByName(wName);
                                    outputStream.write((ok ? "OK\n" : "ERROR: Wallet not found\n").getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WALLET_TAB ")) {
                                String[] wParts = strTrim.substring(19).trim().split("\\s+");
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && wParts.length >= 3) {
                                    try {
                                        int tabId = Integer.parseInt(wParts[0]);
                                        String wName = wParts[1];
                                        String wAddr = wParts[2];
                                        String wChain = wParts.length > 3 ? wParts[3] : null;
                                        boolean ok = mgr.switchWalletDirectOnTab(tabId, wName, wAddr, wChain);
                                        outputStream.write((ok ? "OK\n" : "ERROR: Tab not found\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId name address [chain])\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WALLET_DAEMON ")) {
                                String ep = strTrim.substring(22).trim();
                                if ("local".equalsIgnoreCase(ep) || "default".equalsIgnoreCase(ep) || "reset".equalsIgnoreCase(ep)) {
                                    com.termux.webview.GhostBridgeInterface.setWalletDaemonEndpoint("127.0.0.1", 3333);
                                } else {
                                    String clean = ep.replace("http://", "").replace("https://", "").split("/")[0];
                                    String[] hp = clean.split(":");
                                    String h = hp[0].trim();
                                    int p = (hp.length > 1) ? Integer.parseInt(hp[1].trim()) : 3333;
                                    com.termux.webview.GhostBridgeInterface.setWalletDaemonEndpoint(h, p);
                                }
                                outputStream.write(("{\"status\":\"ok\",\"host\":" + org.json.JSONObject.quote(com.termux.webview.GhostBridgeInterface.sWalletDaemonHost) + ",\"port\":" + com.termux.webview.GhostBridgeInterface.sWalletDaemonPort + "}\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_WALLET_NEXT")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    String nextW = mgr.switchToNextWallet();
                                    outputStream.write(((nextW != null ? nextW : "OK") + "\n").getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_CHAIN_SET ") || strTrim.startsWith("WEBVIEW_SET_CHAIN ")) {
                                String cKey = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    boolean ok = mgr.switchChainByKey(cKey);
                                    outputStream.write((ok ? "OK\n" : "ERROR: Failed to switch chain\n").getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_UPLOAD_FILE ") || strTrim.startsWith("WEBVIEW_FILE_UPLOAD ")) {
                                String filePath = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                boolean ok = com.termux.webview.FusionWebChromeClient.supplyFileToPendingChooser(filePath);
                                outputStream.write((ok ? "OK\n" : "ERROR: File not found or failed\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_SWIPE ")) {
                                String params = strTrim.substring(14).trim();
                                String[] parts = params.split("[,\\s]+");
                                if (parts.length >= 4) {
                                    try {
                                        float x1 = Float.parseFloat(parts[0]);
                                        float y1 = Float.parseFloat(parts[1]);
                                        float x2 = Float.parseFloat(parts[2]);
                                        float y2 = Float.parseFloat(parts[3]);
                                        int duration = parts.length >= 5 ? Integer.parseInt(parts[4]) : 300;
                                        com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                        if (mgr != null) {
                                            mgr.dispatchNativeSwipe(x1, y1, x2, y2, duration);
                                            outputStream.write("OK\n".getBytes());
                                        } else {
                                            outputStream.write("ERROR: Manager not ready\n".getBytes());
                                        }
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid swipe arguments (x1 y1 x2 y2 [duration])\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_LONG_PRESS ") || strTrim.startsWith("WEBVIEW_LONGPRESS ")) {
                                String params = strTrim.substring(strTrim.indexOf(" ") + 1).trim();
                                String[] parts = params.split("[,\\s]+");
                                if (parts.length >= 2) {
                                    try {
                                        float x = Float.parseFloat(parts[0]);
                                        float y = Float.parseFloat(parts[1]);
                                        int duration = parts.length >= 3 ? Integer.parseInt(parts[2]) : 800;
                                        com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                        if (mgr != null) {
                                            mgr.dispatchNativeLongPress(x, y, duration);
                                            outputStream.write("OK\n".getBytes());
                                        } else {
                                            outputStream.write("ERROR: Manager not ready\n".getBytes());
                                        }
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid long press arguments (x y [duration])\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_KEYBOARD_HIDE") || strTrim.equals("KEYBOARD_HIDE")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    mgr.hideSoftKeyboard();
                                }
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WAKELOCK")) {
                                String arg = strTrim.length() > 16 ? strTrim.substring(16).trim() : "on";
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    if ("off".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)) {
                                        mgr.releaseWakeLock();
                                    } else {
                                        mgr.acquireWakeLock();
                                    }
                                    outputStream.write("OK\n".getBytes());
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_LAST_DOWNLOAD") || strTrim.equals("WEBVIEW_DOWNLOAD_LAST")) {
                                String path = com.termux.webview.FusionDownloadListener.getLastDownloadedFilePath();
                                outputStream.write(((path != null ? path : "") + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_CLIPBOARD_GET") || strTrim.equals("CLIPBOARD_GET")) {
                                String clip = getClipboard();
                                outputStream.write(((clip != null ? clip : "") + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_CLIPBOARD_SET ") || strTrim.startsWith("CLIPBOARD_SET ")) {
                                String textToCopy = strTrim.substring(strTrim.indexOf(" ") + 1);
                                setClipboard(textToCopy);
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_KILL_TAB_ID ")) {
                                String idStr = strTrim.substring(20).trim();
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    try {
                                        mgr.killTabById(Integer.parseInt(idStr));
                                        outputStream.write("OK\n".getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Manager not ready\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_EVAL_TAB ")) {
                                String rest = strTrim.substring(17).trim();
                                int sp = rest.indexOf(' ');
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && sp > 0) {
                                    try {
                                        int tabId = Integer.parseInt(rest.substring(0, sp).trim());
                                        String js = rest.substring(sp + 1);
                                        String res = mgr.evaluateJsOnTabSync(tabId, js, 20000);
                                        outputStream.write(((res != null ? res : "null") + "\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId js)\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("ESOGRAM ") || strTrim.equals("ESOGRAM")
                                    || strTrim.startsWith("TELEGRAM ") || strTrim.equals("TELEGRAM")) {
                                String rest = strTrim.startsWith("ESOGRAM")
                                    ? (strTrim.length() > 7 ? strTrim.substring(7).trim() : "status")
                                    : (strTrim.length() > 8 ? strTrim.substring(8).trim() : "status");
                                String outJson = handleTelegramCommand(rest);
                                outputStream.write((outJson + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_ACTION_TAB ")) {
                                String rest = strTrim.substring(19).trim();
                                String[] parts = rest.split("\\s+", 3);
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && parts.length >= 2) {
                                    try {
                                        int tabId = Integer.parseInt(parts[0].trim());
                                        String action = parts[1].trim();
                                        String urlArg = parts.length >= 3 ? parts[2].trim() : null;
                                        boolean ok = mgr.navigateTabAction(tabId, action, urlArg);
                                        outputStream.write((ok ? "OK\n" : "ERROR: Tab not found\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId action [url])\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_ENGINE_RELOAD")) {
                                com.termux.webview.FusionWebViewManager.clearAssetScriptCache();
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && mgr.mActivity != null) {
                                    mgr.mActivity.runOnUiThread(() -> {
                                        if (mgr.mTabList != null) {
                                            for (Object obj : mgr.mTabList) {
                                                if (obj instanceof com.termux.webview.FusionTab) {
                                                    com.termux.webview.FusionTab t = (com.termux.webview.FusionTab) obj;
                                                    if (t.isGecko()) com.termux.webview.FusionGeckoHelper.injectScripts(mgr, t);
                                                    else if (t.mWebView != null) mgr.injectScripts(t.mWebView);
                                                }
                                            }
                                        }
                                    });
                                }
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_NET_BLOCK ")) {
                                String pattern = strTrim.substring(18).trim();
                                boolean ok = com.termux.webview.FusionNetworkInterceptor.addBlockRule(pattern);
                                outputStream.write((ok ? "OK\n" : "ERROR\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_NET_MIRROR ")) {
                                String rest = strTrim.substring(19).trim();
                                int sp = rest.indexOf(' ');
                                boolean ok = (sp > 0) && com.termux.webview.FusionNetworkInterceptor.addLocalMirrorRule(rest.substring(0, sp).trim(), rest.substring(sp + 1).trim());
                                outputStream.write((ok ? "OK\n" : "ERROR\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_NET_MOCK ")) {
                                try {
                                    org.json.JSONObject jo = new org.json.JSONObject(strTrim.substring(17).trim());
                                    boolean ok = com.termux.webview.FusionNetworkInterceptor.addMockRule(
                                        jo.getString("pattern"),
                                        jo.optString("body", ""),
                                        jo.optInt("statusCode", 200),
                                        jo.optString("mimeType", "application/json"),
                                        jo.optString("headers", "{}")
                                    );
                                    outputStream.write((ok ? "OK\n" : "ERROR\n").getBytes());
                                } catch (Exception e) {
                                    outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_NET_CLEAR")) {
                                com.termux.webview.FusionNetworkInterceptor.clearRules();
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_STRICT_SSL ")) {
                                String arg = strTrim.substring(19).trim();
                                com.termux.webview.FusionWebViewClient.sStrictSslCheck = "on".equalsIgnoreCase(arg) || "true".equalsIgnoreCase(arg) || "1".equals(arg);
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_PERM") || strTrim.equals("WEBVIEW_PERMISSIONS") || strTrim.startsWith("WEBVIEW_PERM ") || strTrim.startsWith("WEBVIEW_PERMISSIONS ")) {
                                String rest = "";
                                if (strTrim.startsWith("WEBVIEW_PERMISSIONS ")) rest = strTrim.substring(20).trim();
                                else if (strTrim.startsWith("WEBVIEW_PERM ")) rest = strTrim.substring(13).trim();
                                String[] p = rest.isEmpty() ? new String[0] : rest.split("\\s+");
                                if (p.length >= 2) {
                                    String scope = p[0].toLowerCase(java.util.Locale.ROOT);
                                    boolean enable = "on".equalsIgnoreCase(p[1]) || "true".equalsIgnoreCase(p[1]) || "1".equals(p[1]);
                                    if ("geo".equals(scope) || "geolocation".equals(scope) || "gps".equals(scope) || "all".equals(scope)) {
                                        com.termux.webview.FusionWebChromeClient.sAllowGeolocation = enable;
                                    }
                                    if ("drm".equals(scope) || "media-id".equals(scope) || "sensitive".equals(scope) || "all".equals(scope)) {
                                        com.termux.webview.FusionWebChromeClient.sAllowDrmId = enable;
                                    }
                                    if ("cam".equals(scope) || "camera".equals(scope) || "video".equals(scope) || "sensitive".equals(scope) || "all".equals(scope)) {
                                        com.termux.webview.FusionWebChromeClient.sAllowCamera = enable;
                                    }
                                    if ("mic".equals(scope) || "microphone".equals(scope) || "audio".equals(scope) || "sensitive".equals(scope) || "all".equals(scope)) {
                                        com.termux.webview.FusionWebChromeClient.sAllowMic = enable;
                                    }
                                    if ("sensitive".equals(scope) || "all".equals(scope)) {
                                        com.termux.webview.FusionWebChromeClient.sAllowSensitivePermissions = enable;
                                    }
                                } else if (p.length == 1 && ("on".equalsIgnoreCase(p[0]) || "off".equalsIgnoreCase(p[0]) || "true".equalsIgnoreCase(p[0]) || "false".equalsIgnoreCase(p[0]))) {
                                    boolean enable = "on".equalsIgnoreCase(p[0]) || "true".equalsIgnoreCase(p[0]);
                                    com.termux.webview.FusionWebChromeClient.sAllowGeolocation = enable;
                                    com.termux.webview.FusionWebChromeClient.sAllowSensitivePermissions = enable;
                                    com.termux.webview.FusionWebChromeClient.sAllowDrmId = enable;
                                    com.termux.webview.FusionWebChromeClient.sAllowCamera = enable;
                                    com.termux.webview.FusionWebChromeClient.sAllowMic = enable;
                                }
                                outputStream.write(("{\"geo\":" + com.termux.webview.FusionWebChromeClient.sAllowGeolocation
                                        + ",\"drm\":" + com.termux.webview.FusionWebChromeClient.sAllowDrmId
                                        + ",\"cam\":" + com.termux.webview.FusionWebChromeClient.sAllowCamera
                                        + ",\"mic\":" + com.termux.webview.FusionWebChromeClient.sAllowMic
                                        + ",\"sensitive\":" + com.termux.webview.FusionWebChromeClient.sAllowSensitivePermissions + "}\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_CDP_ON")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    int port = mgr.setDevCdpEnabled(true, false);
                                    outputStream.write(("{\"status\":\"ok\",\"cdp\":true,\"port\":" + port + "}\n").getBytes());
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Manager not ready\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_CDP_OFF")) {
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null) {
                                    mgr.setDevCdpEnabled(false, false);
                                    outputStream.write("{\"status\":\"ok\",\"cdp\":false}\n".getBytes());
                                } else {
                                    outputStream.write("{\"status\":\"error\",\"message\":\"Manager not ready\"}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_CDP_INFO")) {
                                boolean running = com.termux.webview.FusionCdpBridge.isRunning();
                                int port = com.termux.webview.FusionCdpBridge.getBoundPort();
                                outputStream.write(("{\"running\":" + running + ",\"port\":" + port + ",\"pid\":" + android.os.Process.myPid() + "}\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_TOUCH_TAB ")) {
                                String[] parts = strTrim.substring(18).trim().split("[,\\s]+");
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && parts.length >= 3) {
                                    try {
                                        int tabId = Integer.parseInt(parts[0]);
                                        float x = Float.parseFloat(parts[1]);
                                        float y = Float.parseFloat(parts[2]);
                                        boolean ok = mgr.dispatchNativeTouchOnTab(tabId, x, y);
                                        outputStream.write((ok ? "OK\n" : "ERROR: Tab not found\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId x y)\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_SWIPE_TAB ")) {
                                String[] parts = strTrim.substring(18).trim().split("\\s+");
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && parts.length >= 5) {
                                    try {
                                        int tabId = Integer.parseInt(parts[0]);
                                        float x1 = Float.parseFloat(parts[1]);
                                        float y1 = Float.parseFloat(parts[2]);
                                        float x2 = Float.parseFloat(parts[3]);
                                        float y2 = Float.parseFloat(parts[4]);
                                        int dur = parts.length >= 6 ? Integer.parseInt(parts[5]) : 300;
                                        boolean ok = mgr.dispatchNativeSwipeOnTab(tabId, x1, y1, x2, y2, dur);
                                        outputStream.write((ok ? "OK\n" : "ERROR: Tab not found\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId x1 y1 x2 y2 [dur])\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_SCREENSHOT_TAB ")) {
                                String rest = strTrim.substring(23).trim();
                                int sp = rest.indexOf(' ');
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && sp > 0) {
                                    try {
                                        int tabId = Integer.parseInt(rest.substring(0, sp).trim());
                                        String outPath = rest.substring(sp + 1).trim();
                                        String saved = mgr.captureTabScreenshot(tabId, outPath);
                                        outputStream.write(((saved != null ? saved : "ERROR: Failed") + "\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId path)\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_KEEPALIVE_TAB ")) {
                                String[] parts = strTrim.substring(22).trim().split("\\s+");
                                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                if (mgr != null && parts.length >= 2) {
                                    try {
                                        int tabId = Integer.parseInt(parts[0]);
                                        boolean keep = "on".equalsIgnoreCase(parts[1]) || "true".equalsIgnoreCase(parts[1]) || "1".equals(parts[1]);
                                        boolean ok = mgr.setTabKeepAlive(tabId, keep);
                                        outputStream.write((ok ? "OK\n" : "ERROR: Tab not found\n").getBytes());
                                    } catch (Exception e) {
                                        outputStream.write(("ERROR: " + e.getMessage() + "\n").getBytes());
                                    }
                                } else {
                                    outputStream.write("ERROR: Invalid arguments (tabId on|off)\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_PROFILE_CLEAR ")) {
                                String prof = strTrim.substring(22).trim();
                                com.termux.webview.FusionProfileManager.clearProfileData(TermuxIpcServer.getActivity(), prof);
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_PROFILE_WIPE ")) {
                                String prof = strTrim.substring(21).trim();
                                com.termux.webview.FusionProfileManager.wipeProfileCompletely(TermuxIpcServer.getActivity(), prof);
                                outputStream.write("OK\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WIFI_ON")) {
                                String rest = strTrim.length() > 15 ? strTrim.substring(15).trim() : "";
                                int port = com.termux.webview.FusionWifiRemoteServer.DEFAULT_PORT;
                                boolean saveAuto = true;
                                for (String tok : rest.split("\\s+")) {
                                    if (tok.isEmpty()) continue;
                                    if ("temp".equalsIgnoreCase(tok) || "no-autostart".equalsIgnoreCase(tok) || "--no-autostart".equalsIgnoreCase(tok)) {
                                        saveAuto = false;
                                    } else {
                                        try { port = Integer.parseInt(tok); } catch (Exception ignored) {}
                                    }
                                }
                                JSONObject st = com.termux.webview.FusionWifiRemoteServer.start(port);
                                if (saveAuto) {
                                    setWifiMarker("wifi_autostart", String.valueOf(port));
                                }
                                try { st.put("autostart", readWifiMarker("wifi_autostart") != null); } catch (Exception ignored) {}
                                outputStream.write((st.toString() + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_WIFI_OFF")) {
                                com.termux.webview.FusionWifiRemoteServer.stop();
                                setWifiMarker("wifi_autostart", null);
                                setWifiMarker("wifi_cdp_autostart", null);
                                outputStream.write("{\"status\":\"ok\",\"running\":false,\"autostart\":false}\n".getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WIFI_AUTOSTART")) {
                                String arg = strTrim.length() > 22 ? strTrim.substring(22).trim().toLowerCase(java.util.Locale.ROOT) : "status";
                                if (arg.equals("on") || arg.equals("true") || arg.equals("1")) {
                                    int p = com.termux.webview.FusionWifiRemoteServer.isRunning()
                                            ? com.termux.webview.FusionWifiRemoteServer.getPort()
                                            : com.termux.webview.FusionWifiRemoteServer.DEFAULT_PORT;
                                    setWifiMarker("wifi_autostart", String.valueOf(p));
                                } else if (arg.equals("off") || arg.equals("false") || arg.equals("0")) {
                                    setWifiMarker("wifi_autostart", null);
                                    setWifiMarker("wifi_cdp_autostart", null);
                                }
                                boolean autoOn = readWifiMarker("wifi_autostart") != null;
                                boolean cdpAutoOn = readWifiMarker("wifi_cdp_autostart") != null;
                                outputStream.write(("{\"status\":\"ok\",\"wifiAutostart\":" + autoOn + ",\"cdpLanAutostart\":" + cdpAutoOn + "}\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WIFI_CDP_AUTOSTART")) {
                                String arg = strTrim.length() > 26 ? strTrim.substring(26).trim().toLowerCase(java.util.Locale.ROOT) : "status";
                                if (arg.equals("on") || arg.equals("true") || arg.equals("1")) {
                                    int cp = com.termux.webview.FusionCdpBridge.isLanRunning() ? com.termux.webview.FusionCdpBridge.getLanBoundPort() : 9223;
                                    setWifiMarker("wifi_cdp_autostart", String.valueOf(cp));
                                } else if (arg.equals("off") || arg.equals("false") || arg.equals("0")) {
                                    setWifiMarker("wifi_cdp_autostart", null);
                                }
                                boolean cdpAutoOn = readWifiMarker("wifi_cdp_autostart") != null;
                                outputStream.write(("{\"status\":\"ok\",\"cdpLanAutostart\":" + cdpAutoOn + "}\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.equals("WEBVIEW_WIFI_STATUS")) {
                                JSONObject st = com.termux.webview.FusionWifiRemoteServer.getStatusJson();
                                try {
                                    st.put("wifiAutostart", readWifiMarker("wifi_autostart") != null);
                                    st.put("cdpLanAutostart", readWifiMarker("wifi_cdp_autostart") != null);
                                } catch (Exception ignored) {}
                                outputStream.write((st.toString() + "\n").getBytes());
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_WIFI_CDP ")) {
                                String arg = strTrim.substring(17).trim();
                                boolean on = "on".equalsIgnoreCase(arg) || "true".equalsIgnoreCase(arg) || "1".equals(arg);
                                if (on) {
                                    com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                    if (mgr != null) mgr.setDevCdpEnabled(true, false);
                                    int lanPort = com.termux.webview.FusionCdpBridge.startLan(android.os.Process.myPid(), 9223);
                                    setWifiMarker("wifi_cdp_autostart", String.valueOf(lanPort));
                                    String ip = com.termux.webview.FusionWifiRemoteServer.getLocalLanIp();
                                    outputStream.write(("{\"status\":\"ok\",\"cdpLan\":true,\"ip\":\"" + ip + "\",\"port\":" + lanPort + "}\n").getBytes());
                                } else {
                                    com.termux.webview.FusionCdpBridge.stopLan();
                                    setWifiMarker("wifi_cdp_autostart", null);
                                    outputStream.write("{\"status\":\"ok\",\"cdpLan\":false}\n".getBytes());
                                }
                                outputStream.flush();
                                continue;
                            } else if (strTrim.startsWith("WEBVIEW_CALL_NATIVE ")) {
                                try {
                                    org.json.JSONObject jo = new org.json.JSONObject(strTrim.substring(20).trim());
                                    com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                                    com.termux.webview.FusionNativeBridge bridge = new com.termux.webview.FusionNativeBridge(mgr, mgr != null ? mgr.mActiveWebView : null);
                                    String res = bridge.reflect(
                                        jo.getString("className"),
                                        jo.getString("methodName"),
                                        jo.optString("args", "[]")
                                    );
                                    outputStream.write((res + "\n").getBytes());
                                } catch (Exception e) {
                                    outputStream.write(("{\"success\":false,\"error\":" + org.json.JSONObject.quote(e.getMessage()) + "}\n").getBytes());
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
            } while (false);
            return new String(baos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return "ERROR: " + t.getMessage() + "\n";
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

    private static void setWifiMarker(String name, String value) {
        String[] dirs = new String[] { "/data/data/com.termux/files/home/.fusion", "/root/.fusion" };
        for (String d : dirs) {
            try {
                java.io.File f = new java.io.File(d, name);
                if (value == null) {
                    if (f.exists()) f.delete();
                } else {
                    if (f.getParentFile() != null && !f.getParentFile().exists()) f.getParentFile().mkdirs();
                    if (f.getParentFile() != null && f.getParentFile().exists()) {
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                            fos.write(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private static String readWifiMarker(String name) {
        String[] dirs = new String[] { "/data/data/com.termux/files/home/.fusion", "/root/.fusion" };
        for (String d : dirs) {
            try {
                java.io.File f = new java.io.File(d, name);
                if (f.exists() && f.length() > 0 && f.length() < 64) {
                    byte[] b = new byte[(int) f.length()];
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                        int n = fis.read(b);
                        if (n > 0) return new String(b, 0, n, java.nio.charset.StandardCharsets.UTF_8).trim();
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
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
                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                        String wifiPort = readWifiMarker("wifi_autostart");
                        if (wifiPort != null) {
                            int p = com.termux.webview.FusionWifiRemoteServer.DEFAULT_PORT;
                            try { p = Integer.parseInt(wifiPort); } catch (Exception ignored) {}
                            com.termux.webview.FusionWifiRemoteServer.start(p);
                        }
                        String cdpPort = readWifiMarker("wifi_cdp_autostart");
                        if (cdpPort != null) {
                            int cp = 9223;
                            try { cp = Integer.parseInt(cdpPort); } catch (Exception ignored) {}
                            if (sActivity != null) {
                                sActivity.runOnUiThread(() -> android.webkit.WebView.setWebContentsDebuggingEnabled(true));
                            }
                            com.termux.webview.FusionCdpBridge.startLan(android.os.Process.myPid(), cp);
                        }
                    } catch (Throwable ignored) {}
                }, "FusionWifiAutoStart").start();
            }
        } catch (Throwable th) {
        }
    }

    private static String handleTelegramCommand(String rest) {
        boolean hasOverride = false;
        try {
            String cmd = (rest == null || rest.trim().isEmpty()) ? "status" : rest.trim();
            if (cmd.startsWith("@acc:")) {
                int sp = cmd.indexOf(' ');
                String accId = sp > 0 ? cmd.substring(5, sp).trim() : cmd.substring(5).trim();
                cmd = sp > 0 ? cmd.substring(sp + 1).trim() : "status";
                if (sActivity != null) com.termux.telegram.FusionTelegramHelper.ensureStarted(sActivity);
                com.termux.telegram.FusionTelegramHelper.setThreadAccountOverride(accId);
                hasOverride = true;
            }
            String[] parts = cmd.split("\\s+", 2);
            String sub = parts[0].toLowerCase(java.util.Locale.ROOT);
            String arg = parts.length > 1 ? parts[1].trim() : "";

            if ("open".equals(sub) || "show".equals(sub)) {
                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                if (mgr != null) {
                    if (arg.isEmpty()) mgr.showTelegramPanel();
                    else mgr.showTelegramPanel(arg);
                    return "{\"ok\":true,\"panel\":\"open\",\"lock\":\"" + mgr.mTelegramCanvasLock + "\"}";
                }
                return "{\"ok\":false,\"error\":\"WebViewManager not ready\"}";
            }
            if ("close".equals(sub) || "hide".equals(sub)) {
                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                if (mgr != null) {
                    mgr.hideTelegramPanel();
                    return "{\"ok\":true,\"panel\":\"closed\"}";
                }
                return "{\"ok\":false,\"error\":\"WebViewManager not ready\"}";
            }
            if ("toggle".equals(sub)) {
                com.termux.webview.FusionWebViewManager mgr = com.termux.webview.FusionWebViewManager.getInstance();
                if (mgr != null) {
                    mgr.toggleTelegramPanel(arg);
                    return "{\"ok\":true,\"panel\":\"toggled\",\"lock\":\"" + mgr.mTelegramCanvasLock + "\"}";
                }
                return "{\"ok\":false,\"error\":\"WebViewManager not ready\"}";
            }

            if (sActivity != null) {
                com.termux.telegram.FusionTelegramHelper.ensureStarted(sActivity);
            }

            if ("status".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.getStatusJson().toString();
            }
            if ("accounts".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.listAccountsJson().toString();
            }
            if ("use".equals(sub) || "switch".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.switchAccount(sActivity, arg).toString();
            }
            if ("delete_recipe".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.deleteRecipeJson(arg).toString();
            }
            if ("reload_plugins".equals(sub) || "plugins".equals(sub) || "ui_reload".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.reloadPlugins().toString();
            }
            if ("bar_set".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.setLiveBarJson(arg).toString();
            }
            if ("bar_clear".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.clearLiveBar().toString();
            }
            if ("login_creds".equals(sub)) {
                String[] p = arg.split("\\s+", 2);
                if (p.length < 2 || sActivity == null) return "{\"ok\":false,\"error\":\"Usage: login_creds <apiId> <apiHash>\"}";
                com.termux.telegram.FusionTelegramHelper.setApiCredentials(sActivity, Integer.parseInt(p[0]), p[1]);
                return com.termux.telegram.FusionTelegramHelper.getStatusJson().toString();
            }
            if ("login_phone".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.sendPhoneNumber(arg).toString();
            }
            if ("login_code".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.sendAuthCode(arg).toString();
            }
            if ("login_pass".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.sendPassword(arg).toString();
            }
            if ("logout".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.logout().toString();
            }
            if ("chats".equals(sub)) {
                String filter = "all";
                int limit = 30;
                if (!arg.isEmpty()) {
                    String[] p = arg.split("\\s+");
                    filter = p[0];
                    if (p.length > 1) limit = Integer.parseInt(p[1]);
                }
                return com.termux.telegram.FusionTelegramHelper.getChatsJson(filter, limit).toString();
            }
            if ("history".equals(sub)) {
                String[] p = arg.split("\\s+");
                if (p.length < 1 || p[0].isEmpty()) return "{\"ok\":false,\"error\":\"Usage: history <chatId> [limit]\"}";
                long chatId = Long.parseLong(p[0]);
                int limit = p.length > 1 ? Integer.parseInt(p[1]) : 30;
                return com.termux.telegram.FusionTelegramHelper.getHistoryJson(chatId, 0, limit).toString();
            }
            if ("send".equals(sub)) {
                String[] p = arg.split("\\s+", 2);
                if (p.length < 2) return "{\"ok\":false,\"error\":\"Usage: send <chatId> <text>\"}";
                long chatId = Long.parseLong(p[0]);
                return com.termux.telegram.FusionTelegramHelper.sendMessageJson(chatId, p[1], 0).toString();
            }
            if ("click".equals(sub)) {
                String[] p = arg.split("\\s+", 3);
                if (p.length < 3) return "{\"ok\":false,\"error\":\"Usage: click <chatId> <msgId> <data>\"}";
                return com.termux.telegram.FusionTelegramHelper.clickInlineButtonJson(Long.parseLong(p[0]), Long.parseLong(p[1]), p[2]).toString();
            }
            if ("webapp".equals(sub)) {
                String[] p = arg.split("\\s+", 3);
                if (p.length < 2) return "{\"ok\":false,\"error\":\"Usage: webapp <chatId> <botUserId> [url]\"}";
                return com.termux.telegram.FusionTelegramHelper.openWebAppJson(Long.parseLong(p[0]), Long.parseLong(p[1]), p.length > 2 ? p[2] : "").toString();
            }
            if ("search".equals(sub)) {
                String[] p = arg.split("\\s+", 2);
                if (p.length < 2) return "{\"ok\":false,\"error\":\"Usage: search <chatId|0> <query>\"}";
                return com.termux.telegram.FusionTelegramHelper.searchMessagesJson(Long.parseLong(p[0]), p[1], 25).toString();
            }
            if ("join".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.joinChatJson(arg).toString();
            }
            if ("resolve".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.resolveChatJson(arg).toString();
            }
            if ("start_bot".equals(sub)) {
                String[] p = arg.split("\\s+", 2);
                if (p.length < 1 || p[0].isEmpty()) return "{\"ok\":false,\"error\":\"Usage: start_bot <bot> [param]\"}";
                return com.termux.telegram.FusionTelegramHelper.startBotJson(p[0], p.length > 1 ? p[1] : "").toString();
            }
            if ("recipes".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.listRecipesJson().toString();
            }
            if ("play".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.replayRecipeJson(arg, 0).toString();
            }
            if ("events".equals(sub)) {
                int lim = arg.isEmpty() ? 30 : Integer.parseInt(arg);
                return com.termux.telegram.FusionTelegramHelper.getRecentEventsJson(lim).toString();
            }
            if ("rec_start".equals(sub)) {
                com.termux.telegram.FusionTelegramHelper.startRecording(arg);
                return "{\"ok\":true,\"recording\":true}";
            }
            if ("rec_stop".equals(sub)) {
                return com.termux.telegram.FusionTelegramHelper.stopRecordingAndSave(arg).toString();
            }
            if ("rec_status".equals(sub)) {
                return "{\"ok\":true,\"recording\":" + com.termux.telegram.FusionTelegramHelper.isRecording()
                    + ",\"count\":" + com.termux.telegram.FusionTelegramHelper.getRecordedCount() + "}";
            }
            if ("tab_pos".equals(sub) && sActivity != null) {
                com.termux.telegram.FusionTelegramHelper.setTabPosition(sActivity, arg);
                return "{\"ok\":true,\"tabPos\":\"" + com.termux.telegram.FusionTelegramHelper.getTabPosition(sActivity) + "\"}";
            }
            return "{\"ok\":false,\"error\":\"Unknown telegram command: " + sub + "\"}";
        } catch (Throwable t) {
            return "{\"ok\":false,\"error\":\"" + String.valueOf(t.getMessage()).replace("\"", "'") + "\"}";
        } finally {
            if (hasOverride) {
                com.termux.telegram.FusionTelegramHelper.clearThreadAccountOverride();
            }
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
