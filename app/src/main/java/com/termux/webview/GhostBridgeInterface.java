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
    public static String sCurrentSolWalletName = "default";
    public static String sCurrentSolWalletAddress = "";
    public static String sCurrentTronWalletName = "default";
    public static String sCurrentTronWalletAddress = "";
    public static String sCurrentSuiWalletName = "default";
    public static String sCurrentSuiWalletAddress = "";
    public static String sCurrentAptosWalletName = "default";
    public static String sCurrentAptosWalletAddress = "";
    public static String sCurrentIotaWalletName = "default";
    public static String sCurrentIotaWalletAddress = "";
    public static String sLastTxHash = "";
    public static String sWcUri = "";
    public static boolean sIsAutoMode = false;
    private static long sLastRpcCheckTime = 0;
    private static boolean sLastRpcCheckResult = false;

    public static boolean checkRpcAlive() {
        long now = System.currentTimeMillis();
        if (now - sLastRpcCheckTime < 1500) {
            return sLastRpcCheckResult;
        }
        sLastRpcCheckTime = now;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 3333), 150);
            socket.close();
            sLastRpcCheckResult = true;
            return true;
        } catch (Exception unused) {
            sLastRpcCheckResult = false;
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

    public static void setSolWallet(String str, String str2) {
        sCurrentSolWalletName = str;
        sCurrentSolWalletAddress = str2;
    }

    public static void setTronWallet(String str, String str2) {
        sCurrentTronWalletName = str;
        sCurrentTronWalletAddress = str2;
    }

    public static void setSuiWallet(String str, String str2) {
        sCurrentSuiWalletName = str;
        sCurrentSuiWalletAddress = str2;
    }

    public static void setAptosWallet(String str, String str2) {
        sCurrentAptosWalletName = str;
        sCurrentAptosWalletAddress = str2;
    }

    public static void setIotaWallet(String str, String str2) {
        sCurrentIotaWalletName = str;
        sCurrentIotaWalletAddress = str2;
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
        if (sCurrentWalletAddress != null && !sCurrentWalletAddress.isEmpty()) {
            return sCurrentWalletAddress;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentWalletAddress != null && !mgr.mCurrentWalletAddress.isEmpty()) {
            return mgr.mCurrentWalletAddress;
        }
        return FusionWalletHelper.getMasterAddress();
    }

    @JavascriptInterface
    public String getWalletName() {
        if (sCurrentWalletName != null && !sCurrentWalletName.isEmpty()) {
            return sCurrentWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentWalletName != null && !mgr.mCurrentWalletName.isEmpty()) {
            return mgr.mCurrentWalletName;
        }
        return FusionWalletHelper.getMasterName();
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
        if (str.contains("sol_connect") || str.contains("solana_requestAccounts") || str.contains("sol_accounts") || str.contains("sol_getAccounts") || str.contains("solana_accounts")) {
            String solAddr = sCurrentSolWalletAddress;
            if (solAddr == null || solAddr.isEmpty()) {
                solAddr = FusionWalletHelper.getMasterSolAddress();
            }
            if (solAddr != null && !solAddr.isEmpty()) {
                return "{\"result\":{\"publicKey\":\"" + solAddr + "\"}}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("sol_signIn")) {
            String solAddr = sCurrentSolWalletAddress;
            if (solAddr == null || solAddr.isEmpty()) {
                solAddr = FusionWalletHelper.getMasterSolAddress();
            }
            if (solAddr != null && !solAddr.isEmpty()) {
                return "{\"result\":{\"account\":{\"address\":\"" + solAddr + "\"}}}";
            }
        }
        if (str.contains("tron_connect") || str.contains("tron_requestAccounts")) {
            String tronAddr = sCurrentTronWalletAddress;
            if (tronAddr == null || tronAddr.isEmpty()) {
                tronAddr = FusionWalletHelper.getMasterTronAddress();
            }
            if (tronAddr != null && !tronAddr.isEmpty()) {
                return "{\"result\":{\"code\":200,\"message\":\"success\",\"address\":\"" + tronAddr + "\"}}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("sui_connect") || str.contains("sui_getAccounts") || str.contains("sui_requestAccounts")) {
            String suiAddr = sCurrentSuiWalletAddress;
            if (suiAddr == null || suiAddr.isEmpty()) {
                suiAddr = FusionWalletHelper.getMasterSuiAddress();
            }
            if (suiAddr != null && !suiAddr.isEmpty()) {
                return "{\"result\":[{\"address\":\"" + suiAddr + "\",\"publicKey\":\"" + suiAddr + "\"}]}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("aptos_connect") || str.contains("aptos_account") || str.contains("aptos_getAccount")) {
            String aptosAddr = sCurrentAptosWalletAddress;
            if (aptosAddr == null || aptosAddr.isEmpty()) {
                aptosAddr = FusionWalletHelper.getMasterAptosAddress();
            }
            if (aptosAddr != null && !aptosAddr.isEmpty()) {
                return "{\"result\":{\"address\":\"" + aptosAddr + "\",\"publicKey\":\"" + aptosAddr + "\"}}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("iota_connect") || str.contains("iota_getAccounts") || str.contains("iota_requestAccounts") || str.contains("iota_accounts")) {
            String iotaAddr = sCurrentIotaWalletAddress;
            if (iotaAddr == null || iotaAddr.isEmpty()) {
                iotaAddr = FusionWalletHelper.getMasterIotaAddress();
            }
            if (iotaAddr != null && !iotaAddr.isEmpty()) {
                return "{\"result\":[{\"address\":\"" + iotaAddr + "\",\"publicKey\":\"" + iotaAddr + "\"}]}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("eth_requestAccounts")) {
            String ethAddr = sCurrentWalletAddress;
            if (ethAddr == null || ethAddr.isEmpty()) {
                ethAddr = FusionWalletHelper.getMasterAddress();
            }
            if (ethAddr != null && !ethAddr.isEmpty()) {
                return "{\"result\":[\"" + ethAddr + "\"]}";
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline();
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return "{\"result\":[\"" + sCurrentWalletAddress + "\"]}";
        }
        if (str.contains("eth_accounts")) {
            String ethAddr = sCurrentWalletAddress;
            if (ethAddr == null || ethAddr.isEmpty()) {
                ethAddr = FusionWalletHelper.getMasterAddress();
            }
            if (ethAddr != null && !ethAddr.isEmpty()) {
                return "{\"result\":[\"" + ethAddr + "\"]}";
            }
            return "{\"result\":[]}";
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
            return "{\"result\":[{\"caveats\":[],\"parentCapability\":\"eth_accounts\"}]}";
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

    @JavascriptInterface
    public String getSolWalletAddress() {
        if (sCurrentSolWalletAddress != null && !sCurrentSolWalletAddress.isEmpty()) {
            return sCurrentSolWalletAddress;
        }
        return FusionWalletHelper.getMasterSolAddress();
    }

    @JavascriptInterface
    public String getSolWalletName() {
        if (sCurrentSolWalletName != null && !sCurrentSolWalletName.isEmpty()) {
            return sCurrentSolWalletName;
        }
        return FusionWalletHelper.getMasterSolName();
    }

    @JavascriptInterface
    public boolean isSolanaActive() {
        return FusionWalletHelper.hasSolanaWallets();
    }

    @JavascriptInterface
    public void setSolWalletDirect(String str, String str2) {
        setSolWallet(str, str2);
    }

    @JavascriptInterface
    public String getTronWalletAddress() {
        if (sCurrentTronWalletAddress != null && !sCurrentTronWalletAddress.isEmpty()) {
            return sCurrentTronWalletAddress;
        }
        return FusionWalletHelper.getMasterTronAddress();
    }

    @JavascriptInterface
    public String getTronWalletName() {
        if (sCurrentTronWalletName != null && !sCurrentTronWalletName.isEmpty()) {
            return sCurrentTronWalletName;
        }
        return FusionWalletHelper.getMasterTronName();
    }

    @JavascriptInterface
    public boolean isTronActive() {
        return FusionWalletHelper.hasTronWallets();
    }

    @JavascriptInterface
    public void setTronWalletDirect(String str, String str2) {
        setTronWallet(str, str2);
    }

    @JavascriptInterface
    public String getSuiWalletAddress() {
        if (sCurrentSuiWalletAddress != null && !sCurrentSuiWalletAddress.isEmpty()) {
            return sCurrentSuiWalletAddress;
        }
        return FusionWalletHelper.getMasterSuiAddress();
    }

    @JavascriptInterface
    public String getSuiWalletName() {
        if (sCurrentSuiWalletName != null && !sCurrentSuiWalletName.isEmpty()) {
            return sCurrentSuiWalletName;
        }
        return FusionWalletHelper.getMasterSuiName();
    }

    @JavascriptInterface
    public boolean isSuiActive() {
        return FusionWalletHelper.hasSuiWallets();
    }

    @JavascriptInterface
    public void setSuiWalletDirect(String str, String str2) {
        setSuiWallet(str, str2);
    }

    @JavascriptInterface
    public String getAptosWalletAddress() {
        if (sCurrentAptosWalletAddress != null && !sCurrentAptosWalletAddress.isEmpty()) {
            return sCurrentAptosWalletAddress;
        }
        return FusionWalletHelper.getMasterAptosAddress();
    }

    @JavascriptInterface
    public String getAptosWalletName() {
        if (sCurrentAptosWalletName != null && !sCurrentAptosWalletName.isEmpty()) {
            return sCurrentAptosWalletName;
        }
        return FusionWalletHelper.getMasterAptosName();
    }

    @JavascriptInterface
    public boolean isAptosActive() {
        return FusionWalletHelper.hasAptosWallets();
    }

    @JavascriptInterface
    public void setAptosWalletDirect(String str, String str2) {
        setAptosWallet(str, str2);
    }

    @JavascriptInterface
    public String getIotaWalletAddress() {
        if (sCurrentIotaWalletAddress != null && !sCurrentIotaWalletAddress.isEmpty()) {
            return sCurrentIotaWalletAddress;
        }
        return FusionWalletHelper.getMasterIotaAddress();
    }

    @JavascriptInterface
    public String getIotaWalletName() {
        if (sCurrentIotaWalletName != null && !sCurrentIotaWalletName.isEmpty()) {
            return sCurrentIotaWalletName;
        }
        return FusionWalletHelper.getMasterIotaName();
    }

    @JavascriptInterface
    public boolean isIotaActive() {
        return FusionWalletHelper.hasIotaWallets();
    }

    @JavascriptInterface
    public void setIotaWalletDirect(String str, String str2) {
        setIotaWallet(str, str2);
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
        return writeWalletFileStatic(str, str2);
    }

    public static boolean writeWalletFileStatic(String str, String str2) {
        if (str != null && str2 != null) {
            boolean written = false;
            String[] targetDirs = str.startsWith("/")
                ? new String[]{""}
                : new String[]{"/data/data/com.termux/files/home/wallet/", "/data/local/nhsystem/kali-arm64/root/wallet/", "/root/wallet/"};
            for (String dir : targetDirs) {
                try {
                    File file = str.startsWith("/") ? new File(str) : new File(dir + str);
                    File parentFile = file.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(str2.getBytes("UTF-8"));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    written = true;
                } catch (Exception unused) {
                }
            }
            return written;
        }
        return false;
    }

    @JavascriptInterface
    public void acquireWakeLock() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.acquireWakeLock();
        }
    }

    @JavascriptInterface
    public void releaseWakeLock() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.releaseWakeLock();
        }
    }

    @JavascriptInterface
    public void showWebNotification(String title, String body, String iconUrl, String targetUrl) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.showWebNotification(title, body, iconUrl, targetUrl);
        }
    }
}
