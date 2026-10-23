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
