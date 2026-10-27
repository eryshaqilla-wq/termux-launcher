package com.termux.webview;

import android.webkit.JavascriptInterface;
import com.termux.app.TermuxActivity;
import com.termux.app.TermuxIpcServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class GhostBridgeInterface implements Runnable {
    public static String sCurrentWalletName = "default";
    public static String sCurrentWalletAddress = "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359";
    public static String sLastTxHash = "";
    public static String sWcUri = "";
    public static boolean sIsAutoMode = false;

    public static boolean checkRpcAlive() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 3333), 200);
            socket.close();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public static String forwardToDaemon(String str) {
        if (str == null) {
            return "{\"error\":\"Empty payload\"}";
        }
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://127.0.0.1:3333").openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(1500);
            httpURLConnection.setReadTimeout(60000);
            byte[] bytes = str.getBytes("UTF-8");
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            int responseCode = httpURLConnection.getResponseCode();
            InputStream errorStream = (responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
            if (errorStream == null) {
                httpURLConnection.disconnect();
                return "{\"error\":\"No response from daemon\"}";
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int i = errorStream.read(bArr);
                if (i == -1) {
                    errorStream.close();
                    httpURLConnection.disconnect();
                    return byteArrayOutputStream.toString("UTF-8");
                }
                byteArrayOutputStream.write(bArr, 0, i);
            }
        } catch (Exception e) {
            return "{\"error\":\"Ghost RPC daemon offline. Jalankan GhostWallet terlebih dahulu\"}";
        }
    }

    private static String readFileDirect(File file) {
        if (file != null) {
            try {
                if (file.exists() && file.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bArr = new byte[4096];
                    while (true) {
                        int i = fileInputStream.read(bArr);
                        if (i == -1) {
                            fileInputStream.close();
                            return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                        }
                        byteArrayOutputStream.write(bArr, 0, i);
                    }
                }
            } catch (Exception unused) {
            }
        }
        return "";
    }

    public static String readFileFromWalletDir(String str) {
        if (str == null) {
            return "";
        }
        if (str.startsWith("/")) {
            return readFileDirect(new File(str));
        }
        for (String str2 : new String[]{"/root/wallet/", "/data/data/com.termux/files/home/wallet/", "/data/local/nhsystem/kali-arm64/root/wallet/", "/sdcard/AGY/", "/sdcard/Download/"}) {
            String fileDirect = readFileDirect(new File(str2 + str));
            if (fileDirect.length() > 0) {
                return fileDirect;
            }
        }
        return "";
    }

    public static void setAuto(boolean z) {
        sIsAutoMode = z;
    }

    public static void setWallet(String str, String str2) {
        sCurrentWalletName = str;
        sCurrentWalletAddress = str2;
    }

    public static void setWc(String str) {
        sWcUri = str;
    }

    @JavascriptInterface
    public void copyToClipboard(String str) {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager != null) {
            fusionWebViewManager.copyAddressToClipboard(str);
        }
    }

    @JavascriptInterface
    public String getActiveAiEngine() {
        return FusionAiHelper.getActiveEngine();
    }

    @JavascriptInterface
    public void setActiveAiEngine(String str) {
        FusionAiHelper.setActiveEngine(str);
    }

    @JavascriptInterface
    public String forwardToAgy(String str) {
        if (str == null) {
            return "{\"error\":\"Empty payload\"}";
        }
        if (!isAgyAlive()) {
            wakeUpAgy();
            try {
                Thread.sleep(800L);
            } catch (Exception unused) {
            }
        }
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://127.0.0.1:3334/task").openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(1500);
            httpURLConnection.setReadTimeout(60000);
            byte[] bytes = str.getBytes("UTF-8");
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            int responseCode = httpURLConnection.getResponseCode();
            InputStream errorStream = (responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
            if (errorStream == null) {
                httpURLConnection.disconnect();
                return "{\"error\":\"No response from AGY daemon\"}";
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int i = errorStream.read(bArr);
                if (i == -1) {
                    errorStream.close();
                    httpURLConnection.disconnect();
                    return byteArrayOutputStream.toString("UTF-8");
                }
                byteArrayOutputStream.write(bArr, 0, i);
            }
        } catch (Exception e) {
            return "{\"error\":\"offline\"}";
        }
    }

    @JavascriptInterface
    public String getChainIdDec() {
        return FusionChainHelper.getChainIdDec();
    }

    @JavascriptInterface
    public String getChainIdHex() {
        return FusionChainHelper.getChainIdHex();
    }

    @JavascriptInterface
    public String getChainKey() {
        return FusionChainHelper.getCurrentChainKey();
    }

    @JavascriptInterface
    public String getChainName() {
        return FusionChainHelper.getCurrentChainName();
    }

    @JavascriptInterface
    public String getLastTxHash() {
        return sLastTxHash;
    }

    @JavascriptInterface
    public String getWalletAddress() {
        return !checkRpcAlive() ? "" : sCurrentWalletAddress;
    }

    @JavascriptInterface
    public String getWalletName() {
        return sCurrentWalletName;
    }

    @JavascriptInterface
    public String getWcUri() {
        return sWcUri;
    }

    @JavascriptInterface
    public boolean isAgyAlive() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 3334), 300);
            socket.close();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean isAutoMode() {
        return sIsAutoMode;
    }

    @JavascriptInterface
    public boolean isDaemonAlive() {
        return checkRpcAlive();
    }

    @JavascriptInterface
    public String listDatabaseFiles(String str) {
        String[] list;
        if (str == null) {
            return "[]";
        }
        try {
            File file = new File(str);
            if (file.exists() && file.isDirectory() && (list = file.list()) != null) {
                JSONArray jSONArray = new JSONArray();
                for (String str2 : list) {
                    if (str2 != null && (str2.endsWith(".json") || str2.endsWith(".txt"))) {
                        jSONArray.put(str2);
                    }
                }
                return jSONArray.toString();
            }
        } catch (Exception unused) {
        }
        return "[]";
    }

    @JavascriptInterface
    public void notifyDaemonOffline() {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || fusionWebViewManager.mActivity == null) {
            return;
        }
        TermuxActivity r1 = fusionWebViewManager.mActivity;
        r1.runOnUiThread(new FusionNotifyOfflineRunnable(r1));
    }

    @JavascriptInterface
    public void notifyRecordingStep(int i) {
        TermuxActivity termuxActivity;
        FusionRecPillHelper.setStepCount(i);
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionRecUpdateRunnable(fusionWebViewManager));
    }

    @JavascriptInterface
    public void notifyRecordingState(boolean z) {
        TermuxActivity termuxActivity;
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        fusionWebViewManager.mIsRecording = z;
        termuxActivity.runOnUiThread(new FusionRecUpdateRunnable(fusionWebViewManager));
    }

    @JavascriptInterface
    public void notifyRecordingStep(String str) {
        if (str == null) {
            return;
        }
        try {
            notifyRecordingStep(Integer.parseInt(str));
        } catch (Throwable unused) {
        }
    }

    @JavascriptInterface
    public void notifyTxHash(String str) {
        sLastTxHash = str;
    }

    @JavascriptInterface
    public void onDrawerClose() {
        TermuxActivity termuxActivity;
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionDrawerSyncRunnable(fusionWebViewManager, false));
    }

    @JavascriptInterface
    public void onDrawerOpen() {
        TermuxActivity termuxActivity;
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionDrawerSyncRunnable(fusionWebViewManager, true));
    }

    @JavascriptInterface
    public void onTxSubmitted(String str) {
        if (str != null) {
            sLastTxHash = str;
        }
    }

    @JavascriptInterface
    public String postRpc(String str) {
        JSONObject jSONObjectOptJSONObject;
        if (str == null) {
            return "{\"error\":\"Empty payload\"}";
        }
        if (str.contains("eth_requestAccounts")) {
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return "{\"result\":[\"" + sCurrentWalletAddress + "\"]}";
        }
        if (str.contains("eth_accounts")) {
            if (!checkRpcAlive()) {
                return "{\"result\":[]}";
            }
            return "{\"result\":[\"" + sCurrentWalletAddress + "\"]}";
        }
        if (str.contains("eth_chainId")) {
            return "{\"result\":\"" + FusionChainHelper.getChainIdHex() + "\"}";
        }
        if (str.contains("net_version")) {
            return "{\"result\":\"" + FusionChainHelper.getChainIdDec() + "\"}";
        }
        if (!str.contains("wallet_switchEthereumChain") && !str.contains("wallet_addEthereumChain")) {
            if (str.contains("wallet_getCapabilities")) {
                return "{\"result\":{}}";
            }
            if (!str.contains("wallet_requestPermissions")) {
                return forwardToDaemon(str);
            }
            if (checkRpcAlive()) {
                return "{\"result\":[{\"caveats\":[],\"parentCapability\":\"eth_accounts\"}]}";
            }
            notifyDaemonOffline();
            return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
        }
        try {
            JSONArray jSONArrayOptJSONArray = new JSONObject(str).optJSONArray("params");
            if (jSONArrayOptJSONArray != null && jSONArrayOptJSONArray.length() > 0 && (jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(0)) != null) {
                String strOptString = jSONObjectOptJSONObject.optString("chainId", "");
                if (strOptString.length() > 0) {
                    switchChainByHex(strOptString);
                }
            }
        } catch (Exception unused) {
        }
        forwardToDaemon(str);
        return "{\"result\":null}";
    }

    @JavascriptInterface
    public String readWalletFile(String str) {
        return readFileFromWalletDir(str);
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager != null) {
            fusionWebViewManager.updateMenuTexts();
        }
    }

    @JavascriptInterface
    public void setWalletDirect(String str, String str2) {
        setWallet(str, str2);
    }

    @JavascriptInterface
    public String nextWallet() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            return mgr.switchToNextWallet();
        }
        return "";
    }

    @JavascriptInterface
    public void wipeCache() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.clearWebviewCache();
        }
    }

    @JavascriptInterface
    public void reloadPage() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.reloadActiveTab();
        }
    }

    @JavascriptInterface
    public String nextFingerprint() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            return mgr.rotateDeviceFingerprint();
        }
        return "";
    }

    @JavascriptInterface
    public void switchChainByHex(String str) {
        TermuxActivity termuxActivity;
        if (str == null) {
            return;
        }
        FusionChainHelper.setChainByHex(str);
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        termuxActivity.runOnUiThread(this);
    }

    @JavascriptInterface
    public void wakeUpAgy() {
        try {
            TermuxActivity activity = TermuxIpcServer.getActivity();
            if (activity != null) {
                TermuxIpcServer.injectCommand(activity, "copilot --bg", true);
            }
        } catch (Exception unused) {
        }
    }

    @JavascriptInterface
    public boolean writeWalletFile(String str, String str2) {
        File file;
        if (str != null && str2 != null) {
            try {
                if (str.startsWith("/")) {
                    file = new File(str);
                } else {
                    File file2 = new File("/root/wallet/");
                    file = new File(((file2.exists() && file2.canWrite()) ? "/root/wallet/" : "/data/data/com.termux/files/home/wallet/") + str);
                }
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(str2.getBytes("UTF-8"));
                fileOutputStream.flush();
                fileOutputStream.close();
                return true;
            } catch (Exception unused) {
            }
        }
        return false;
    }
}
