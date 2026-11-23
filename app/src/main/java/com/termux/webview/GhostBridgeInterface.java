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
    public static String sActiveToolChain = "";
    public static String sActiveToolSymbol = "";
    public static String sActiveToolAddress = "";
    public static String sActiveToolWalletName = "";
    public static String sLastTxHash = "";
    public static String sWcUri = "";
    public static boolean sIsAutoMode = false;
    public static String sLastDetectedDappChainKey = "";
    public static int sLastDetectedDappChainId = 0;
    private static long sLastRpcCheckTime = 0;
    private static boolean sLastRpcCheckResult = false;

    @JavascriptInterface
    public void setToolWallet(final String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        try {
            JSONObject obj = new JSONObject(jsonStr);
            final String chain = obj.optString("chain", obj.optString("network", ""));
            final int chainId = obj.optInt("chainId", obj.optInt("id", 0));
            final String chainName = obj.optString("chainName", obj.optString("name", ""));
            final String symbol = obj.optString("symbol", "");
            final String walletName = obj.optString("name", obj.optString("walletName", "eso-1"));
            final String address = obj.optString("address", obj.optString("walletAddress", ""));
            final boolean isNonEvm = obj.optBoolean("isNonEvm", true);

            if (address.isEmpty()) return;

            final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if (mgr != null && mgr.mActiveWebView != null) {
                String curUrl = mgr.mActiveWebView.getUrl();
                String detected = FusionChainHelper.detectChainFromUrl(curUrl);
                if (detected != null && !detected.isEmpty()) {
                    boolean isMatching = detected.equalsIgnoreCase(chain);
                    if (!isMatching) {
                        boolean isSubGroupDetected = "substrate".equalsIgnoreCase(detected) || "flop".equalsIgnoreCase(detected) || "polkadot".equalsIgnoreCase(detected) || "dot".equalsIgnoreCase(detected);
                        boolean isSubGroupIncoming = "substrate".equalsIgnoreCase(chain) || "flop".equalsIgnoreCase(chain) || "polkadot".equalsIgnoreCase(chain) || "dot".equalsIgnoreCase(chain);
                        if (isSubGroupDetected && isSubGroupIncoming) {
                            isMatching = true;
                        }
                    }
                    if (!isMatching) {
                        // Dilarang membajak tool wallet jika chain yang diminta tidak sesuai dengan situs aktif
                        return;
                    }
                }
            }

            sActiveToolChain = chain;
            sActiveToolSymbol = !symbol.isEmpty() ? symbol : chain.toUpperCase();
            sActiveToolAddress = address;
            sActiveToolWalletName = walletName;

            if (!chain.isEmpty()) {
                FusionChainHelper.registerDynamicChain(chain, !chainName.isEmpty() ? chainName : chain, sActiveToolSymbol, chainId, isNonEvm, null);
                FusionChainHelper.setChain(chain);
                FusionWalletHelper.setDynamicWallet(chain, walletName, address, true);
            }

            final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if (mgr != null && mgr.mActivity != null) {
                mgr.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mgr.mCurrentWalletName = walletName;
                        mgr.mCurrentWalletAddress = address;
                        if (!chain.isEmpty()) {
                            mgr.mCurrentChainKey = chain;
                        }
                        mgr.updateMenuTexts();
                        FusionToolPillHelper.updateToolPillUi(mgr);
                        FusionRecPillHelper.updateRecUi(mgr);
                        if (FusionDrawerHelper.isWalletDrawerOpen()) {
                            FusionDrawerHelper.showWalletDrawer(mgr);
                        } else if (FusionDrawerHelper.isChainDrawerOpen()) {
                            FusionDrawerHelper.showChainDrawer(mgr);
                        }
                    }
                });
            }
        } catch (Throwable ignored) {}
    }

    @JavascriptInterface
    public void registerChain(final String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        try {
            JSONObject obj = new JSONObject(jsonStr);
            String key = obj.getString("key");
            String name = obj.optString("name", key);
            String symbol = obj.optString("symbol", key.toUpperCase());
            int chainId = obj.optInt("chainId", 0);
            boolean isNonEvm = obj.optBoolean("isNonEvm", true);
            JSONArray patternsArr = obj.optJSONArray("urlPatterns");
            String[] patterns = null;
            if (patternsArr != null) {
                patterns = new String[patternsArr.length()];
                for (int i = 0; i < patternsArr.length(); i++) {
                    patterns[i] = patternsArr.getString(i);
                }
            }
            FusionChainHelper.registerDynamicChain(key, name, symbol, chainId, isNonEvm, patterns);
        } catch (Throwable ignored) {}
    }

    @JavascriptInterface
    public String getActiveToolAddress() {
        if (sActiveToolAddress != null && !sActiveToolAddress.isEmpty()) {
            return sActiveToolAddress;
        }
        String curChain = FusionChainHelper.getCurrentChainKey();
        return FusionWalletHelper.getAddressForChain(curChain, 0);
    }

    @JavascriptInterface
    public String getActiveToolChain() {
        if (sActiveToolChain != null && !sActiveToolChain.isEmpty()) {
            return sActiveToolChain;
        }
        return FusionChainHelper.getCurrentChainKey();
    }

    @JavascriptInterface
    public String getActiveToolSymbol() {
        if (sActiveToolSymbol != null && !sActiveToolSymbol.isEmpty()) {
            return sActiveToolSymbol;
        }
        return FusionToolPillHelper.getShortNetworkName();
    }

    public static String getLastDetectedDappChainKey() {
        return sLastDetectedDappChainKey;
    }

    public static int getLastDetectedDappChainId() {
        return sLastDetectedDappChainId;
    }

    public static void handleDappChainDetected(String chainKey, int chainId) {
        if (chainKey == null || chainKey.trim().isEmpty()) {
            return;
        }
        final String key = chainKey.trim().toLowerCase();
        final int id = chainId;

        // Skip duplicate notifications to prevent UI thread thrashing
        if (key.equals(sLastDetectedDappChainKey) && (id == sLastDetectedDappChainId || id == 0)) {
            return;
        }
        sLastDetectedDappChainKey = key;
        sLastDetectedDappChainId = id;

        // Reset override tool wallet jika dApp yang terdeteksi berbeda chain
        if (sActiveToolChain != null && !sActiveToolChain.isEmpty() && !sActiveToolChain.equalsIgnoreCase(key)) {
            boolean matches = false;
            boolean isSubGroupKey = "substrate".equalsIgnoreCase(key) || "flop".equalsIgnoreCase(key) || "polkadot".equalsIgnoreCase(key) || "dot".equalsIgnoreCase(key);
            boolean isSubGroupTool = "substrate".equalsIgnoreCase(sActiveToolChain) || "flop".equalsIgnoreCase(sActiveToolChain) || "polkadot".equalsIgnoreCase(sActiveToolChain) || "dot".equalsIgnoreCase(sActiveToolChain);
            if (isSubGroupKey && isSubGroupTool) {
                matches = true;
            }
            if (!matches) {
                sActiveToolChain = "";
                sActiveToolSymbol = "";
                sActiveToolAddress = "";
                sActiveToolWalletName = "";
            }
        }

        if (id > 0) {
            FusionChainHelper.setChainById(id);
        } else {
            FusionChainHelper.setChain(key);
        }

        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(mgr);
            if (activeTab != null) {
                activeTab.mChainKey = FusionChainHelper.getCurrentChainKey();
                activeTab.mChainId = FusionChainHelper.getCurrentChainId();
            }
            if (mgr.mActivity != null) {
                mgr.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mgr.mCurrentChainKey = FusionChainHelper.getCurrentChainKey();
                        mgr.updateMenuTexts();
                        FusionToolPillHelper.updateToolPillUi(mgr);
                        if (FusionDrawerHelper.isWalletDrawerOpen()) {
                            FusionDrawerHelper.showWalletDrawer(mgr);
                        } else if (FusionDrawerHelper.isChainDrawerOpen()) {
                            FusionDrawerHelper.showChainDrawer(mgr);
                        }
                    }
                });
            }
        }
    }

    @JavascriptInterface
    public void reportDappChain(String chainKey, int chainId) {
        handleDappChainDetected(chainKey, chainId);
    }

    public static boolean checkRpcAlive() {
        long now = System.currentTimeMillis();
        if (now - sLastRpcCheckTime < 1500) {
            return sLastRpcCheckResult;
        }
        sLastRpcCheckTime = now;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 3333), 500);
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
    public String getClipboard() {
        return TermuxIpcServer.getClipboard();
    }

    @JavascriptInterface
    public String getLastDownload() {
        return FusionDownloadListener.getLastDownloadedFilePath();
    }

    @JavascriptInterface
    public void hideKeyboard() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.hideSoftKeyboard();
        }
    }

    @JavascriptInterface
    public boolean dispatchIpc(String command) {
        if (command == null || command.trim().isEmpty()) return false;
        FusionWebViewDispatcher.dispatch(command.trim());
        return true;
    }

    @JavascriptInterface
    public boolean dispatchTouch(float x, float y) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        return mgr != null && mgr.dispatchNativeTouch(x, y);
    }

    @JavascriptInterface
    public boolean dispatchSwipe(float x1, float y1, float x2, float y2, int durationMs) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        return mgr != null && mgr.dispatchNativeSwipe(x1, y1, x2, y2, durationMs);
    }

    @JavascriptInterface
    public boolean dispatchLongPress(float x, float y, int durationMs) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        return mgr != null && mgr.dispatchNativeLongPress(x, y, durationMs);
    }

    @JavascriptInterface
    public boolean supplyUploadFile(String filePath) {
        return FusionWebChromeClient.supplyFileToPendingChooser(filePath);
    }

    @JavascriptInterface
    public String readLocalFile(String path) {
        if (path == null || path.isEmpty()) return "";
        try {
            File f = new File(path);
            if (!f.exists() || !f.canRead()) return "";
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            return new String(data, "UTF-8");
        } catch (Throwable t) {
            return "";
        }
    }

    @JavascriptInterface
    public boolean writeLocalFile(String path, String content) {
        return writeWalletFileStatic(path, content);
    }

    @JavascriptInterface
    public void openNewTab(String url) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.addNewTab(url);
        }
    }

    @JavascriptInterface
    public void closeCurrentTab() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.killActiveTab();
        }
    }

    @JavascriptInterface
    public void switchTab(int index) {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null) {
            mgr.switchTab(index);
        }
    }

    @JavascriptInterface
    public void toast(final String message) {
        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mActivity != null && message != null) {
            mgr.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    com.termux.app.FusionToast.makeText(mgr.mActivity, message, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
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

    public static boolean sHasFormFocus = false;

    @JavascriptInterface
    public void onFormFocusChanged(final boolean z) {
        sHasFormFocus = z;
        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mActivity != null) {
            mgr.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FusionToolPillHelper.setFormFocus(mgr, z);
                }
            });
        }
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
    public boolean isRpcAlive() {
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
        notifyDaemonOffline("⚡ [GhostWallet] Transaksi/Signature butuh daemon aktif! Jalankan 'wallet' di terminal.");
    }

    public void notifyDaemonOffline(String message) {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || fusionWebViewManager.mActivity == null) {
            return;
        }
        TermuxActivity r1 = fusionWebViewManager.mActivity;
        r1.runOnUiThread(new FusionNotifyOfflineRunnable(r1, message));
    }

    public static boolean isTxOrSigningRequest(String str) {
        if (str == null) return false;
        return str.contains("eth_sendTransaction") || str.contains("eth_sendRawTransaction")
            || str.contains("personal_sign") || str.contains("eth_signTypedData") || str.contains("eth_sign")
            || str.contains("wallet_sendCalls") || str.contains("wallet_sign") || str.contains("eth_signTransaction")
            || str.contains("sol_signTransaction") || str.contains("sol_signAndSendTransaction")
            || str.contains("sol_signAllTransactions") || str.contains("sol_signMessage") || str.contains("sol_signIn")
            || str.contains("tron_signTransaction") || str.contains("tron_signMessage") || str.contains("tron_sendRawTransaction")
            || str.contains("sui_signAndExecuteTransactionBlock") || str.contains("sui_signAndExecuteTransaction")
            || str.contains("sui_signTransactionBlock") || str.contains("sui_signTransaction") || str.contains("sui_signMessage") || str.contains("sui_signPersonalMessage")
            || str.contains("aptos_signAndSubmitTransaction") || str.contains("aptos_signTransaction") || str.contains("aptos_signMessage")
            || str.contains("iota_signAndExecuteTransactionBlock") || str.contains("iota_signAndExecuteTransaction")
            || str.contains("iota_signTransactionBlock") || str.contains("iota_signTransaction") || str.contains("iota_signMessage") || str.contains("iota_signPersonalMessage")
            || str.contains("sub_signRaw") || str.contains("sub_signPayload") || str.contains("sub_sign") || str.contains("polkadot_sign");
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
        if (str.contains("sub_connect") || str.contains("sub_requestAccounts") || str.contains("sub_accounts") || str.contains("sub_getAccounts") || str.contains("polkadot_requestAccounts")) {
            boolean isDot = str.contains("polkadot") || (FusionChainHelper.getCurrentChainKey() != null && FusionChainHelper.getCurrentChainKey().contains("polkadot"));
            handleDappChainDetected(isDot ? "polkadot" : "substrate", isDot ? 0 : 42);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        if (jo.has("result")) {
                            Object res = jo.get("result");
                            if (res instanceof JSONArray && ((JSONArray) res).length() > 0) {
                                JSONObject first = ((JSONArray) res).optJSONObject(0);
                                if (first != null && first.has("address")) {
                                    sActiveToolAddress = first.getString("address");
                                }
                            } else if (res instanceof JSONObject && ((JSONObject) res).has("address")) {
                                sActiveToolAddress = ((JSONObject) res).getString("address");
                            }
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String subAddr = sActiveToolAddress;
            if (subAddr == null || subAddr.isEmpty()) {
                subAddr = isDot ? FusionWalletHelper.getAddressForChain("polkadot", 0) : FusionWalletHelper.getAddressForChain("substrate", 0);
            }
            if (subAddr == null || subAddr.isEmpty()) {
                subAddr = isDot ? "14Tr41gcoArkeeHpjpvvnEkpWTVX2AsEvvSLBrs7LBULgUJF" : "5FXYugRYwPbHD7HJnBsve5vfeqVsKsK6rRhr2Zskn6SpW3XR";
            }
            return "{\"result\":[{\"address\":\"" + subAddr + "\",\"name\":\"eso-1\",\"type\":\"ed25519\"}]}";
        }
        if (str.contains("sub_signRaw") || str.contains("sub_signPayload") || str.contains("sub_sign")) {
            if (!checkRpcAlive()) {
                notifyDaemonOffline("⚡ [GhostWallet] Transaksi/Signature Substrate butuh daemon aktif! Jalankan 'wallet' di terminal.");
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            return forwardToDaemon(str);
        }
        if (str.contains("sol_connect") || str.contains("solana_requestAccounts") || str.contains("sol_accounts") || str.contains("sol_getAccounts") || str.contains("solana_accounts")) {
            handleDappChainDetected("solana", 501);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONObject resObj = jo.optJSONObject("result");
                        if (resObj != null && resObj.has("publicKey")) {
                            sCurrentSolWalletAddress = resObj.getString("publicKey");
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String solAddr = sCurrentSolWalletAddress;
            if (solAddr == null || solAddr.isEmpty()) {
                solAddr = FusionWalletHelper.getMasterSolAddress();
            }
            if (solAddr != null && !solAddr.isEmpty()) {
                return "{\"result\":{\"publicKey\":\"" + solAddr + "\"}}";
            }
            return "{\"result\":{\"publicKey\":\"8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf\"}}";
        }
        if (str.contains("sol_signIn")) {
            handleDappChainDetected("solana", 501);
            if (!checkRpcAlive()) {
                notifyDaemonOffline("⚡ [GhostWallet] Sign-In Solana butuh daemon aktif! Jalankan 'wallet' di terminal.");
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.\"}}";
            }
            String daemonResp = forwardToDaemon(str);
            if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                return daemonResp;
            }
            String solAddr = sCurrentSolWalletAddress;
            if (solAddr == null || solAddr.isEmpty()) {
                solAddr = FusionWalletHelper.getMasterSolAddress();
            }
            if (solAddr != null && !solAddr.isEmpty()) {
                return "{\"result\":{\"account\":{\"address\":\"" + solAddr + "\"}}}";
            }
            return daemonResp;
        }
        if (str.contains("tron_connect") || str.contains("tron_requestAccounts") || str.contains("tron_accounts")) {
            handleDappChainDetected("tron", 728126428);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONObject resObj = jo.optJSONObject("result");
                        if (resObj != null && resObj.has("address")) {
                            sCurrentTronWalletAddress = resObj.getString("address");
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String tronAddr = sCurrentTronWalletAddress;
            if (tronAddr == null || tronAddr.isEmpty()) {
                tronAddr = FusionWalletHelper.getMasterTronAddress();
            }
            if (tronAddr != null && !tronAddr.isEmpty()) {
                return "{\"result\":{\"code\":200,\"message\":\"success\",\"address\":\"" + tronAddr + "\"}}";
            }
            return "{\"result\":{\"code\":200,\"message\":\"success\",\"address\":\"TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF\"}}";
        }
        if (str.contains("sui_connect") || str.contains("sui_getAccounts") || str.contains("sui_requestAccounts") || str.contains("sui_accounts") || str.contains("sui_signTransactionBlock") || str.contains("sui_signTransaction") || str.contains("sui_signAndExecuteTransactionBlock") || str.contains("sui_signAndExecuteTransaction")) {
            handleDappChainDetected("sui", 784);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONArray arr = jo.optJSONArray("result");
                        if (arr != null && arr.length() > 0) {
                            JSONObject first = arr.optJSONObject(0);
                            if (first != null && first.has("address")) {
                                sCurrentSuiWalletAddress = first.getString("address");
                            }
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String suiAddr = sCurrentSuiWalletAddress;
            if (suiAddr == null || suiAddr.isEmpty()) {
                suiAddr = FusionWalletHelper.getMasterSuiAddress();
            }
            if (suiAddr != null && !suiAddr.isEmpty()) {
                String pk = "0xea495ed3ad35427d2d26a50f88fc5659b4d4bd8250b7917336e130e71e1146a0";
                return "{\"result\":[{\"address\":\"" + suiAddr + "\",\"publicKey\":\"" + pk + "\"}]}";
            }
            return "{\"result\":[{\"address\":\"0x2d5cd38757a6423b513a54523d072ff737b3d5ddc87f56cb96556fbe09a37792\",\"publicKey\":\"0xea495ed3ad35427d2d26a50f88fc5659b4d4bd8250b7917336e130e71e1146a0\"}]}";
        }
        if (str.contains("aptos_connect") || str.contains("aptos_account") || str.contains("aptos_getAccount") || str.contains("aptos_accounts") || str.contains("aptos_signTransaction") || str.contains("aptos_signAndSubmitTransaction")) {
            handleDappChainDetected("aptos", 637);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONObject resObj = jo.optJSONObject("result");
                        if (resObj != null && resObj.has("address")) {
                            sCurrentAptosWalletAddress = resObj.getString("address");
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String aptosAddr = sCurrentAptosWalletAddress;
            if (aptosAddr == null || aptosAddr.isEmpty()) {
                aptosAddr = FusionWalletHelper.getMasterAptosAddress();
            }
            if (aptosAddr != null && !aptosAddr.isEmpty()) {
                return "{\"result\":{\"address\":\"" + aptosAddr + "\",\"publicKey\":\"" + aptosAddr + "\"}}";
            }
            return "{\"result\":{\"address\":\"0xf796fcce529ac9bbda4864320e80f8a016cc0b020ea745f9acde68db5ad63500\",\"publicKey\":\"0x99693ab8c1033510a178b17be990ba6a811d96ff07472c253e6a2de40d0f99d5\"}}";
        }
        if (str.contains("iota_connect") || str.contains("iota_getAccounts") || str.contains("iota_requestAccounts") || str.contains("iota_accounts") || str.contains("iota_signTransactionBlock") || str.contains("iota_signTransaction") || str.contains("iota_signAndExecuteTransactionBlock") || str.contains("iota_signAndExecuteTransaction")) {
            handleDappChainDetected("iota", 4218);
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONArray arr = jo.optJSONArray("result");
                        if (arr != null && arr.length() > 0) {
                            JSONObject first = arr.optJSONObject(0);
                            if (first != null && first.has("address")) {
                                sCurrentIotaWalletAddress = first.getString("address");
                            }
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String iotaAddr = sCurrentIotaWalletAddress;
            if (iotaAddr == null || iotaAddr.isEmpty()) {
                iotaAddr = FusionWalletHelper.getMasterIotaAddress();
            }
            if (iotaAddr != null && !iotaAddr.isEmpty()) {
                String pk = "0x51f382a80164927a5a6f8679df7a6bca01f0618141ee1729bb7ea07a4bea62d6";
                return "{\"result\":[{\"address\":\"" + iotaAddr + "\",\"publicKey\":\"" + pk + "\"}]}";
            }
            return "{\"result\":[{\"address\":\"0xf12a6e99d4da110e8e864ed6d3d8ea29eb78627b00c566982464410a8aa0bb0a\",\"publicKey\":\"0x51f382a80164927a5a6f8679df7a6bca01f0618141ee1729bb7ea07a4bea62d6\"}]}";
        }
        if (str.contains("eth_requestAccounts") || str.contains("eth_accounts") || str.contains("wallet_switchEthereumChain") || isTxOrSigningRequest(str)) {
            if (FusionChainHelper.isNonEvm()) {
                String evmKey = FusionChainHelper.sLastEvmChainKey;
                int evmId = FusionChainHelper.sLastEvmChainId;
                if (evmKey == null || evmKey.isEmpty() || FusionChainHelper.isNonEvmKey(evmKey)) {
                    evmKey = "bsc";
                    evmId = 56;
                }
                handleDappChainDetected(evmKey, evmId);
            }
        }
        if (str.contains("eth_requestAccounts")) {
            if (checkRpcAlive()) {
                String daemonResp = forwardToDaemon(str);
                if (daemonResp != null && !daemonResp.contains("\"error\"")) {
                    try {
                        JSONObject jo = new JSONObject(daemonResp);
                        JSONArray arr = jo.optJSONArray("result");
                        if (arr != null && arr.length() > 0) {
                            sCurrentWalletAddress = arr.getString(0);
                        }
                    } catch (Exception ignored) {}
                    return daemonResp;
                }
            }
            String ethAddr = sCurrentWalletAddress;
            if (ethAddr == null || ethAddr.isEmpty()) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr != null && mgr.mCurrentWalletAddress != null && !mgr.mCurrentWalletAddress.isEmpty()) {
                    ethAddr = mgr.mCurrentWalletAddress;
                }
            }
            if (ethAddr == null || ethAddr.isEmpty()) {
                ethAddr = FusionWalletHelper.getMasterAddress();
            }
            if (ethAddr != null && !ethAddr.isEmpty()) {
                return "{\"result\":[\"" + ethAddr + "\"]}";
            }
            return "{\"result\":[\"0x79E0EE08c6516d1204821f083285Ab65660c26E4\"]}";
        }
        if (str.contains("eth_accounts")) {
            String ethAddr = sCurrentWalletAddress;
            if (ethAddr == null || ethAddr.isEmpty()) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr != null && mgr.mCurrentWalletAddress != null && !mgr.mCurrentWalletAddress.isEmpty()) {
                    ethAddr = mgr.mCurrentWalletAddress;
                }
            }
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
            if (str.contains("wallet_requestPermissions")) {
                return "{\"result\":[{\"caveats\":[],\"parentCapability\":\"eth_accounts\"}]}";
            }
            if (isTxOrSigningRequest(str)) {
                if (!checkRpcAlive()) {
                    notifyDaemonOffline("⚡ [GhostWallet] Transaksi/Signature tertunda! Nyalakan daemon terlebih dahulu (jalankan 'wallet' di terminal).");
                    return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Silakan jalankan 'wallet' di terminal untuk mengeksekusi transaksi.\"}}";
                }
            }
            if (!checkRpcAlive()) {
                notifyDaemonOffline("⚠️ [GhostWallet] Request butuh daemon aktif! Jalankan 'wallet' di terminal.");
                return "{\"error\":{\"code\":4001,\"message\":\"Daemon GhostWallet belum aktif! Jalankan 'wallet' di terminal.\"}}";
            }
            return forwardToDaemon(str);
        }
        try {
            JSONArray jSONArrayOptJSONArray = new JSONObject(str).optJSONArray("params");
            if (jSONArrayOptJSONArray != null && jSONArrayOptJSONArray.length() > 0 && (jSONObjectOptJSONObject = jSONArrayOptJSONArray.optJSONObject(0)) != null) {
                String strOptString = jSONObjectOptJSONObject.optString("chainId", "");
                String chainName = jSONObjectOptJSONObject.optString("chainName", "");
                String symbol = "";
                JSONObject curr = jSONObjectOptJSONObject.optJSONObject("nativeCurrency");
                if (curr != null) {
                    symbol = curr.optString("symbol", "");
                }
                JSONArray rpcUrlsArr = jSONObjectOptJSONObject.optJSONArray("rpcUrls");
                String[] rpcPatterns = null;
                if (rpcUrlsArr != null && rpcUrlsArr.length() > 0) {
                    java.util.List<String> hosts = new java.util.ArrayList<>();
                    for (int ri = 0; ri < rpcUrlsArr.length(); ri++) {
                        String u = rpcUrlsArr.optString(ri, "");
                        try {
                            if (!u.isEmpty()) {
                                java.net.URI uri = new java.net.URI(u);
                                String host = uri.getHost();
                                if (host != null && !host.isEmpty()) hosts.add(host.replace("www.", ""));
                            }
                        } catch (Exception ignored) {}
                    }
                    if (!hosts.isEmpty()) rpcPatterns = hosts.toArray(new String[0]);
                }
                if (strOptString.length() > 0) {
                    int chainId = (strOptString.startsWith("0x") || strOptString.startsWith("0X"))
                            ? (int) Long.parseLong(strOptString.substring(2), 16)
                            : (int) Long.parseLong(strOptString, 10);
                    if (!chainName.isEmpty()) {
                        String key = !symbol.isEmpty() ? symbol.toLowerCase() : ("chain-" + chainId);
                        FusionChainHelper.registerDynamicChain(key, chainName, !symbol.isEmpty() ? symbol : key.toUpperCase(), chainId, false, rpcPatterns);
                    }
                    switchChainByHex(strOptString);
                }
            }
        } catch (Exception unused) {
        }
        forwardToDaemon(str);
        return "{\"result\":null}";
    }

    @JavascriptInterface
    public boolean addChainRpc(String chainKey, String rpcUrl) {
        if (chainKey == null || rpcUrl == null || chainKey.trim().isEmpty() || rpcUrl.trim().isEmpty()) {
            return false;
        }
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("id", System.currentTimeMillis());
            req.put("method", "ghost_addRpc");
            JSONArray params = new JSONArray();
            params.put(chainKey.trim().toLowerCase());
            params.put(rpcUrl.trim());
            req.put("params", params);
            postRpc(req.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
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
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentSolWalletAddress != null && !mgr.mCurrentSolWalletAddress.isEmpty()) {
            return mgr.mCurrentSolWalletAddress;
        }
        return FusionWalletHelper.getMasterSolAddress();
    }

    @JavascriptInterface
    public String getSolWalletName() {
        if (sCurrentSolWalletName != null && !sCurrentSolWalletName.isEmpty()) {
            return sCurrentSolWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentSolWalletName != null && !mgr.mCurrentSolWalletName.isEmpty()) {
            return mgr.mCurrentSolWalletName;
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
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentTronWalletAddress != null && !mgr.mCurrentTronWalletAddress.isEmpty()) {
            return mgr.mCurrentTronWalletAddress;
        }
        return FusionWalletHelper.getMasterTronAddress();
    }

    @JavascriptInterface
    public String getTronWalletName() {
        if (sCurrentTronWalletName != null && !sCurrentTronWalletName.isEmpty()) {
            return sCurrentTronWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentTronWalletName != null && !mgr.mCurrentTronWalletName.isEmpty()) {
            return mgr.mCurrentTronWalletName;
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
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentSuiWalletAddress != null && !mgr.mCurrentSuiWalletAddress.isEmpty()) {
            return mgr.mCurrentSuiWalletAddress;
        }
        return FusionWalletHelper.getMasterSuiAddress();
    }

    @JavascriptInterface
    public String getSuiAddress() {
        return getSuiWalletAddress();
    }

    @JavascriptInterface
    public String getSuiWalletName() {
        if (sCurrentSuiWalletName != null && !sCurrentSuiWalletName.isEmpty()) {
            return sCurrentSuiWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentSuiWalletName != null && !mgr.mCurrentSuiWalletName.isEmpty()) {
            return mgr.mCurrentSuiWalletName;
        }
        return FusionWalletHelper.getMasterSuiName();
    }

    @JavascriptInterface
    public boolean isSuiActive() {
        return FusionWalletHelper.hasSuiWallets();
    }

    @JavascriptInterface
    public String getSuiPublicKey() {
        return "0xea495ed3ad35427d2d26a50f88fc5659b4d4bd8250b7917336e130e71e1146a0";
    }

    @JavascriptInterface
    public String getAptosPublicKey() {
        return "0x99693ab8c1033510a178b17be990ba6a811d96ff07472c253e6a2de40d0f99d5";
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
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentAptosWalletAddress != null && !mgr.mCurrentAptosWalletAddress.isEmpty()) {
            return mgr.mCurrentAptosWalletAddress;
        }
        return FusionWalletHelper.getMasterAptosAddress();
    }

    @JavascriptInterface
    public String getAptosAddress() {
        return getAptosWalletAddress();
    }

    @JavascriptInterface
    public String getAptosWalletName() {
        if (sCurrentAptosWalletName != null && !sCurrentAptosWalletName.isEmpty()) {
            return sCurrentAptosWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentAptosWalletName != null && !mgr.mCurrentAptosWalletName.isEmpty()) {
            return mgr.mCurrentAptosWalletName;
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
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentIotaWalletAddress != null && !mgr.mCurrentIotaWalletAddress.isEmpty()) {
            return mgr.mCurrentIotaWalletAddress;
        }
        return FusionWalletHelper.getMasterIotaAddress();
    }

    @JavascriptInterface
    public String getIotaAddress() {
        return getIotaWalletAddress();
    }

    @JavascriptInterface
    public String getIotaWalletName() {
        if (sCurrentIotaWalletName != null && !sCurrentIotaWalletName.isEmpty()) {
            return sCurrentIotaWalletName;
        }
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr != null && mgr.mCurrentIotaWalletName != null && !mgr.mCurrentIotaWalletName.isEmpty()) {
            return mgr.mCurrentIotaWalletName;
        }
        return FusionWalletHelper.getMasterIotaName();
    }

    @JavascriptInterface
    public boolean isIotaActive() {
        return FusionWalletHelper.hasIotaWallets();
    }

    @JavascriptInterface
    public String getIotaPublicKey() {
        return "0x51f382a80164927a5a6f8679df7a6bca01f0618141ee1729bb7ea07a4bea62d6";
    }

    @JavascriptInterface
    public void setIotaWalletDirect(String str, String str2) {
        setIotaWallet(str, str2);
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager != null) {
            fusionWebViewManager.mCurrentChainKey = FusionChainHelper.getCurrentChainKey();
            fusionWebViewManager.updateMenuTexts();
            FusionToolPillHelper.updateToolPillUi(fusionWebViewManager);
            if (FusionDrawerHelper.isWalletDrawerOpen()) {
                FusionDrawerHelper.showWalletDrawer(fusionWebViewManager);
            } else if (FusionDrawerHelper.isChainDrawerOpen()) {
                FusionDrawerHelper.showChainDrawer(fusionWebViewManager);
            }
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
        String newKey = FusionChainHelper.getCurrentChainKey();
        int newId = FusionChainHelper.getCurrentChainId();
        if (newKey != null && newKey.equals(sLastDetectedDappChainKey) && newId == sLastDetectedDappChainId) {
            return;
        }
        sLastDetectedDappChainKey = newKey;
        sLastDetectedDappChainId = newId;
        FusionWebViewManager fusionWebViewManager = FusionWebViewManager.getInstance();
        if (fusionWebViewManager == null || (termuxActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        fusionWebViewManager.mCurrentChainKey = newKey;
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
        if (activeTab != null) {
            activeTab.mChainKey = newKey;
            activeTab.mChainId = newId;
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

    @JavascriptInterface
    public String nativeFetch(String targetUrl, String method, String headersJson, String body) {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            return "{\"ok\":false,\"error\":\"Target URL is required\"}";
        }
        try {
            java.net.URL url = new java.net.URL(targetUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            String httpMethod = (method != null && !method.trim().isEmpty()) ? method.trim().toUpperCase() : "GET";
            conn.setRequestMethod(httpMethod);

            if (headersJson != null && !headersJson.trim().isEmpty()) {
                try {
                    JSONObject hObj = new JSONObject(headersJson);
                    java.util.Iterator<String> keys = hObj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        conn.setRequestProperty(k, hObj.getString(k));
                    }
                } catch (Exception ignored) {}
            }

            if (body != null && !body.isEmpty() && !"GET".equals(httpMethod) && !"HEAD".equals(httpMethod)) {
                conn.setDoOutput(true);
                byte[] input = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                java.io.OutputStream os = conn.getOutputStream();
                os.write(input, 0, input.length);
                os.flush();
                os.close();
            }

            int status = conn.getResponseCode();
            java.io.InputStream is = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
            String responseText = "";
            if (is != null) {
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] data = new byte[4096];
                int nRead;
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                responseText = new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
                is.close();
            }

            JSONObject resObj = new JSONObject();
            resObj.put("ok", status >= 200 && status < 400);
            resObj.put("status", status);
            resObj.put("statusText", conn.getResponseMessage() != null ? conn.getResponseMessage() : "");
            resObj.put("data", responseText);

            JSONObject respHeaders = new JSONObject();
            java.util.Map<String, java.util.List<String>> map = conn.getHeaderFields();
            if (map != null) {
                for (java.util.Map.Entry<String, java.util.List<String>> entry : map.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                        respHeaders.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
            }
            resObj.put("headers", respHeaders);

            conn.disconnect();
            return resObj.toString();
        } catch (Throwable t) {
            JSONObject errObj = new JSONObject();
            try {
                errObj.put("ok", false);
                errObj.put("error", t.getMessage() != null ? t.getMessage() : t.toString());
            } catch (Exception ignored) {}
            return errObj.toString();
        }
    }

    @JavascriptInterface
    public String getFullCookies(String url) {
        if (url == null || url.trim().isEmpty()) {
            FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if (mgr != null && mgr.mActiveWebView != null) {
                url = mgr.mActiveWebView.getUrl();
            }
        }
        if (url == null || url.trim().isEmpty()) return "";
        try {
            android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
            String cookies = cm.getCookie(url);
            return cookies != null ? cookies : "";
        } catch (Throwable t) {
            return "";
        }
    }

    @JavascriptInterface
    public boolean setFullCookie(String url, String cookieValue) {
        if (url == null || cookieValue == null) return false;
        try {
            android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
            cm.setCookie(url, cookieValue);
            cm.flush();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean clearAllCookies() {
        try {
            android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
            cm.removeAllCookies(null);
            cm.flush();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @JavascriptInterface
    public String execShell(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "{\"success\":false,\"error\":\"Empty command\"}";
        }
        try {
            String[] cmdArray;
            File chrootBash = new File("/data/local/nhsystem/kali-arm64/bin/bash");
            File termuxBash = new File("/data/data/com.termux/files/usr/bin/bash");

            if (chrootBash.exists()) {
                cmdArray = new String[]{"su", "-mm", "-c", "chroot /data/local/nhsystem/kali-arm64 /bin/bash -c " + JSONObject.quote(command)};
            } else if (termuxBash.exists()) {
                cmdArray = new String[]{"/data/data/com.termux/files/usr/bin/bash", "-c", command};
            } else {
                cmdArray = new String[]{"/system/bin/sh", "-c", command};
            }

            Process process = Runtime.getRuntime().exec(cmdArray);

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() > 0) output.append("\n");
                output.append(line);
                if (output.length() > 65536) {
                    output.append("\n...[output truncated]");
                    break;
                }
            }
            reader.close();

            java.io.BufferedReader errReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
            StringBuilder errOutput = new StringBuilder();
            while ((line = errReader.readLine()) != null) {
                if (errOutput.length() > 0) errOutput.append("\n");
                errOutput.append(line);
            }
            errReader.close();

            int exitCode = process.waitFor();

            JSONObject res = new JSONObject();
            res.put("success", exitCode == 0);
            res.put("exitCode", exitCode);
            res.put("stdout", output.toString());
            res.put("stderr", errOutput.toString());
            return res.toString();
        } catch (Throwable t) {
            JSONObject err = new JSONObject();
            try {
                err.put("success", false);
                err.put("error", t.getMessage() != null ? t.getMessage() : t.toString());
            } catch (Exception ignored) {}
            return err.toString();
        }
    }

    @JavascriptInterface
    public String callJava(String target, String methodName, String argsJson) {
        if (target == null || methodName == null) {
            return "{\"success\":false,\"error\":\"Target and methodName required\"}";
        }
        try {
            Object targetObj = null;
            Class<?> targetClass = null;

            FusionWebViewManager mgr = FusionWebViewManager.getInstance();

            if ("activity".equalsIgnoreCase(target)) {
                targetObj = (mgr != null) ? mgr.mActivity : null;
                targetClass = (targetObj != null) ? targetObj.getClass() : null;
            } else if ("webview".equalsIgnoreCase(target)) {
                targetObj = (mgr != null) ? mgr.mActiveWebView : null;
                targetClass = (targetObj != null) ? targetObj.getClass() : null;
            } else if ("manager".equalsIgnoreCase(target)) {
                targetObj = mgr;
                targetClass = (targetObj != null) ? targetObj.getClass() : null;
            } else if ("context".equalsIgnoreCase(target)) {
                targetObj = (mgr != null && mgr.mActivity != null) ? mgr.mActivity.getApplicationContext() : null;
                targetClass = (targetObj != null) ? targetObj.getClass() : null;
            } else {
                targetClass = Class.forName(target);
            }

            if (targetClass == null) {
                return "{\"success\":false,\"error\":\"Target not found: " + target + "\"}";
            }

            JSONArray argsArr = (argsJson != null && !argsJson.trim().isEmpty()) ? new JSONArray(argsJson) : new JSONArray();
            int argCount = argsArr.length();

            java.lang.reflect.Method matchedMethod = null;
            for (java.lang.reflect.Method m : targetClass.getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterTypes().length == argCount) {
                    matchedMethod = m;
                    break;
                }
            }

            if (matchedMethod == null) {
                return "{\"success\":false,\"error\":\"Method " + methodName + " with " + argCount + " params not found on " + target + "\"}";
            }

            matchedMethod.setAccessible(true);
            Class<?>[] paramTypes = matchedMethod.getParameterTypes();
            Object[] invokeArgs = new Object[argCount];

            for (int i = 0; i < argCount; i++) {
                Class<?> pt = paramTypes[i];
                Object raw = argsArr.opt(i);
                if (raw == null || raw == JSONObject.NULL) {
                    invokeArgs[i] = null;
                } else if (pt == String.class) {
                    invokeArgs[i] = String.valueOf(raw);
                } else if (pt == int.class || pt == Integer.class) {
                    invokeArgs[i] = Integer.parseInt(String.valueOf(raw));
                } else if (pt == long.class || pt == Long.class) {
                    invokeArgs[i] = Long.parseLong(String.valueOf(raw));
                } else if (pt == boolean.class || pt == Boolean.class) {
                    invokeArgs[i] = Boolean.parseBoolean(String.valueOf(raw));
                } else if (pt == double.class || pt == Double.class || pt == float.class || pt == Float.class) {
                    invokeArgs[i] = Double.parseDouble(String.valueOf(raw));
                } else if (pt == JSONObject.class) {
                    invokeArgs[i] = (raw instanceof JSONObject) ? raw : new JSONObject(String.valueOf(raw));
                } else if (pt == JSONArray.class) {
                    invokeArgs[i] = (raw instanceof JSONArray) ? raw : new JSONArray(String.valueOf(raw));
                } else if (Context.class.isAssignableFrom(pt) && mgr != null && mgr.mActivity != null) {
                    invokeArgs[i] = mgr.mActivity;
                } else {
                    invokeArgs[i] = raw;
                }
            }

            final java.lang.reflect.Method finalMethod = matchedMethod;
            final Object finalTarget = targetObj;
            final Object[] finalArgs = invokeArgs;

            Object result = null;
            try {
                result = finalMethod.invoke(finalTarget, finalArgs);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause != null && cause.getMessage() != null && cause.getMessage().contains("CalledFromWrongThreadException") && mgr != null && mgr.mActivity != null) {
                    final Object[] asyncResult = new Object[1];
                    final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                    mgr.mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                asyncResult[0] = finalMethod.invoke(finalTarget, finalArgs);
                            } catch (Exception e) {
                                asyncResult[0] = "Error: " + e.getMessage();
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                    latch.await(3000, java.util.concurrent.TimeUnit.MILLISECONDS);
                    result = asyncResult[0];
                } else {
                    throw (cause != null ? cause : ite);
                }
            }

            JSONObject res = new JSONObject();
            res.put("success", true);
            if (result == null) {
                res.put("result", JSONObject.NULL);
            } else if (result instanceof Number || result instanceof Boolean || result instanceof String) {
                res.put("result", result);
            } else if (result instanceof JSONObject || result instanceof JSONArray) {
                res.put("result", result);
            } else {
                res.put("result", result.toString());
            }
            return res.toString();
        } catch (Throwable t) {
            JSONObject err = new JSONObject();
            try {
                err.put("success", false);
                err.put("error", t.getMessage() != null ? t.getMessage() : t.toString());
            } catch (Exception ignored) {}
            return err.toString();
        }
    }

    @JavascriptInterface
    public String listJavaMethods(String target) {
        try {
            Class<?> targetClass = null;
            FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if ("activity".equalsIgnoreCase(target)) targetClass = (mgr != null && mgr.mActivity != null) ? mgr.mActivity.getClass() : null;
            else if ("webview".equalsIgnoreCase(target)) targetClass = (mgr != null && mgr.mActiveWebView != null) ? mgr.mActiveWebView.getClass() : null;
            else if ("manager".equalsIgnoreCase(target)) targetClass = (mgr != null) ? mgr.getClass() : null;
            else targetClass = Class.forName(target);

            if (targetClass == null) return "[]";
            JSONArray arr = new JSONArray();
            for (java.lang.reflect.Method m : targetClass.getMethods()) {
                JSONObject obj = new JSONObject();
                obj.put("name", m.getName());
                obj.put("returnType", m.getReturnType().getSimpleName());
                JSONArray params = new JSONArray();
                for (Class<?> p : m.getParameterTypes()) params.put(p.getSimpleName());
                obj.put("params", params);
                arr.put(obj);
            }
            return arr.toString();
        } catch (Throwable t) {
            return "[]";
        }
    }

    // =========================================================================
    // ⚡ OMNIPOTENT GOD-MODE SUITE: AI, SQLITE, WIRE MOCK, TAB BUS & NOTIFY
    // =========================================================================

    @JavascriptInterface
    public String askAI(String prompt, String optionsJson) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "{\"success\":false,\"error\":\"Empty prompt\"}";
        }
        try {
            JSONObject opts = (optionsJson != null && !optionsJson.trim().isEmpty())
                    ? new JSONObject(optionsJson)
                    : new JSONObject();

            String customEndpoint = opts.optString("endpoint", "");
            if (!customEndpoint.isEmpty()) {
                String model = opts.optString("model", "deepseek-chat");
                String systemPrompt = opts.optString("systemPrompt", "You are an expert AI assistant embedded inside Android WebView userscript.");
                double temperature = opts.optDouble("temperature", 0.3);

                JSONObject body = new JSONObject();
                body.put("model", model);
                body.put("temperature", temperature);
                JSONArray messages = new JSONArray();

                if (!systemPrompt.isEmpty()) {
                    JSONObject sysMsg = new JSONObject();
                    sysMsg.put("role", "system");
                    sysMsg.put("content", systemPrompt);
                    messages.put(sysMsg);
                }

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                messages.put(userMsg);
                body.put("messages", messages);

                String resRaw = nativeFetch(customEndpoint, "POST", "{\"Content-Type\":\"application/json\"}", body.toString());
                JSONObject resJson = new JSONObject(resRaw);
                if (resJson.optBoolean("ok", false)) {
                    String respData = resJson.optString("data", "");
                    try {
                        JSONObject openaiResp = new JSONObject(respData);
                        JSONArray choices = openaiResp.optJSONArray("choices");
                        if (choices != null && choices.length() > 0) {
                            JSONObject first = choices.getJSONObject(0);
                            JSONObject msg = first.optJSONObject("message");
                            if (msg != null) {
                                String content = msg.optString("content", "");
                                JSONObject out = new JSONObject();
                                out.put("success", true);
                                out.put("result", content);
                                return out.toString();
                            }
                        }
                    } catch (Exception ignored) {}
                    JSONObject out = new JSONObject();
                    out.put("success", true);
                    out.put("result", respData);
                    return out.toString();
                } else {
                    return "{\"success\":false,\"error\":" + JSONObject.quote(resJson.optString("error", "AI request failed")) + "}";
                }
            }

            // ⚡ DEFAULT: PRIORITAS AGY (Google Antigravity Ephemeral Pool)
            String sysPrompt = opts.optString("systemPrompt", "You are an autonomous Web3 task agent. Answer directly, concisely and accurately.");
            String nodeScript = "const { queryTriEngineAi } = require('/root/wallet/auto_engine.cjs'); " +
                    "queryTriEngineAi(" + JSONObject.quote(prompt) + ", " + JSONObject.quote(sysPrompt) + ", 'agy').then(r => { " +
                    "  console.log(JSON.stringify({ success: true, result: r.text, engine: r.engine })); " +
                    "}).catch(e => { " +
                    "  console.log(JSON.stringify({ success: false, error: e.message })); " +
                    "});";

            String shellCmd = "node -e " + JSONObject.quote(nodeScript);
            String shellRes = execShell(shellCmd);
            JSONObject resJson = new JSONObject(shellRes);
            if (resJson.optBoolean("success", false)) {
                String stdout = resJson.optString("stdout", "").trim();
                if (stdout.startsWith("{") && stdout.endsWith("}")) {
                    return stdout;
                }
                JSONObject out = new JSONObject();
                out.put("success", true);
                out.put("result", stdout);
                out.put("engine", "AGY Ephemeral");
                return out.toString();
            } else {
                return "{\"success\":false,\"error\":" + JSONObject.quote(resJson.optString("stderr", "AGY query failed")) + "}";
            }
        } catch (Throwable t) {
            return "{\"success\":false,\"error\":" + JSONObject.quote(t.getMessage() != null ? t.getMessage() : t.toString()) + "}";
        }
    }

    private String resolveDefaultDbPath() {
        String[] cands = new String[] {
            "/data/data/com.termux/files/home/wallet/eso.db",
            "/root/wallet/eso.db",
            "/data/local/nhsystem/kali-arm64/root/wallet/eso.db"
        };
        for (String c : cands) {
            File f = new File(c);
            if (f.exists() && f.isFile()) return c;
        }
        return "/data/data/com.termux/files/home/wallet/eso.db";
    }

    @JavascriptInterface
    public String dbQuery(String sql, String paramsJson, String dbPath) {
        if (sql == null || sql.trim().isEmpty()) {
            return "{\"success\":false,\"error\":\"Empty SQL query\"}";
        }
        android.database.sqlite.SQLiteDatabase db = null;
        android.database.Cursor cursor = null;
        try {
            String path = (dbPath != null && !dbPath.trim().isEmpty()) ? dbPath.trim() : resolveDefaultDbPath();
            File f = new File(path);
            if (!f.exists()) {
                return "{\"success\":false,\"error\":\"Database file not found: " + path + "\"}";
            }
            db = android.database.sqlite.SQLiteDatabase.openDatabase(path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
            String[] args = null;
            if (paramsJson != null && !paramsJson.trim().isEmpty() && !paramsJson.trim().equals("[]")) {
                JSONArray arr = new JSONArray(paramsJson);
                args = new String[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    args[i] = arr.optString(i);
                }
            }
            cursor = db.rawQuery(sql, args);
            JSONArray rows = new JSONArray();
            String[] colNames = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                for (int c = 0; c < colNames.length; c++) {
                    int type = cursor.getType(c);
                    String name = colNames[c];
                    if (type == android.database.Cursor.FIELD_TYPE_NULL) {
                        row.put(name, JSONObject.NULL);
                    } else if (type == android.database.Cursor.FIELD_TYPE_INTEGER) {
                        row.put(name, cursor.getLong(c));
                    } else if (type == android.database.Cursor.FIELD_TYPE_FLOAT) {
                        row.put(name, cursor.getDouble(c));
                    } else if (type == android.database.Cursor.FIELD_TYPE_STRING) {
                        row.put(name, cursor.getString(c));
                    } else if (type == android.database.Cursor.FIELD_TYPE_BLOB) {
                        byte[] blob = cursor.getBlob(c);
                        row.put(name, android.util.Base64.encodeToString(blob, android.util.Base64.NO_WRAP));
                    }
                }
                rows.put(row);
            }
            JSONObject res = new JSONObject();
            res.put("success", true);
            res.put("count", rows.length());
            res.put("rows", rows);
            return res.toString();
        } catch (Throwable t) {
            JSONObject err = new JSONObject();
            try {
                err.put("success", false);
                err.put("error", t.getMessage() != null ? t.getMessage() : t.toString());
            } catch (Exception ignored) {}
            return err.toString();
        } finally {
            if (cursor != null) try { cursor.close(); } catch (Exception ignored) {}
            if (db != null) try { db.close(); } catch (Exception ignored) {}
        }
    }

    @JavascriptInterface
    public String dbExec(String sql, String paramsJson, String dbPath) {
        if (sql == null || sql.trim().isEmpty()) {
            return "{\"success\":false,\"error\":\"Empty SQL statement\"}";
        }
        android.database.sqlite.SQLiteDatabase db = null;
        try {
            String path = (dbPath != null && !dbPath.trim().isEmpty()) ? dbPath.trim() : resolveDefaultDbPath();
            File f = new File(path);
            if (!f.exists()) {
                return "{\"success\":false,\"error\":\"Database file not found: " + path + "\"}";
            }
            db = android.database.sqlite.SQLiteDatabase.openDatabase(path, null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
            Object[] bindArgs = null;
            if (paramsJson != null && !paramsJson.trim().isEmpty() && !paramsJson.trim().equals("[]")) {
                JSONArray arr = new JSONArray(paramsJson);
                bindArgs = new Object[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    bindArgs[i] = arr.get(i);
                }
            }
            if (bindArgs != null) {
                db.execSQL(sql, bindArgs);
            } else {
                db.execSQL(sql);
            }
            JSONObject res = new JSONObject();
            res.put("success", true);
            return res.toString();
        } catch (Throwable t) {
            JSONObject err = new JSONObject();
            try {
                err.put("success", false);
                err.put("error", t.getMessage() != null ? t.getMessage() : t.toString());
            } catch (Exception ignored) {}
            return err.toString();
        } finally {
            if (db != null) try { db.close(); } catch (Exception ignored) {}
        }
    }

    @JavascriptInterface
    public boolean mockNetwork(String pattern, String responseBody, int statusCode, String mimeType, String headersJson) {
        return FusionNetworkInterceptor.addMockRule(pattern, responseBody, statusCode, mimeType, headersJson);
    }

    @JavascriptInterface
    public boolean blockNetwork(String pattern) {
        return FusionNetworkInterceptor.addBlockRule(pattern);
    }

    @JavascriptInterface
    public boolean clearNetworkRules() {
        FusionNetworkInterceptor.clearRules();
        return true;
    }

    @JavascriptInterface
    public boolean broadcastTab(String channel, String message) {
        if (channel == null) return false;
        try {
            final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if (mgr == null || mgr.mActivity == null) return false;

            final String safeMsg = (message != null) ? JSONObject.quote(message) : "null";
            final String safeChannel = JSONObject.quote(channel);
            final String script = "(function() { try {\n" +
                    "  if (window.__FUSION_DISPATCH_TAB_MSG__) window.__FUSION_DISPATCH_TAB_MSG__(" + safeChannel + ", " + safeMsg + ");\n" +
                    "} catch(e) {} })();";

            mgr.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        java.util.List<FusionTab> tabs = mgr.getTabList();
                        if (tabs != null) {
                            for (FusionTab t : tabs) {
                                if (t != null && t.mWebView != null) {
                                    t.mWebView.evaluateJavascript(script, null);
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            });
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean notify(String title, String message) {
        if (title == null) title = "Fusion WebView";
        if (message == null) message = "";
        try {
            FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            Context ctx = (mgr != null && mgr.mActivity != null) ? mgr.mActivity : null;
            if (ctx == null) return false;

            String channelId = "fusion_webview_notify";
            android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return false;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel chan = new android.app.NotificationChannel(
                        channelId,
                        "Fusion WebView Notifications",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                );
                chan.setDescription("Notifications triggered by WebView userscripts and dApp automation");
                nm.createNotificationChannel(chan);
            }

            android.app.Notification.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new android.app.Notification.Builder(ctx, channelId);
            } else {
                builder = new android.app.Notification.Builder(ctx);
            }

            int icon = ctx.getApplicationInfo().icon;
            builder.setSmallIcon(icon != 0 ? icon : android.R.drawable.ic_dialog_info)
                   .setContentTitle(title)
                   .setContentText(message)
                   .setAutoCancel(true)
                   .setWhen(System.currentTimeMillis());

            nm.notify((int) (System.currentTimeMillis() % 100000), builder.build());
            return true;
        } catch (Throwable t) {
            android.util.Log.e("GhostBridge", "Error in notify", t);
            return false;
        }
    }

    @JavascriptInterface
    public boolean reloadUserscripts() {
        try {
            final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
            if (mgr == null || mgr.mActivity == null || mgr.mActiveWebView == null) return false;
            mgr.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FusionUserscriptHelper.injectMatchingScripts(mgr.mActiveWebView, mgr.mActiveWebView.getUrl());
                    } catch (Throwable ignored) {}
                }
            });
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @JavascriptInterface
    public String getTabsJson() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        return mgr != null ? mgr.getTabsJson() : "[]";
    }

    @JavascriptInterface
    public boolean closeTab(final int tabIndex) {
        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr == null || mgr.mActivity == null) return false;
        mgr.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mgr.killTabDirect(tabIndex);
                } catch (Throwable ignored) {}
            }
        });
        return true;
    }

    @JavascriptInterface
    public boolean evalTab(final int tabIndex, final String jsCode) {
        if (jsCode == null) return false;
        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr == null || mgr.mActivity == null) return false;
        mgr.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.util.List<FusionTab> tabs = mgr.getTabList();
                    if (tabs != null && tabIndex >= 0 && tabIndex < tabs.size()) {
                        FusionTab t = tabs.get(tabIndex);
                        if (t != null && t.mWebView != null) {
                            t.mWebView.evaluateJavascript(jsCode, null);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });
        return true;
    }

    @JavascriptInterface
    public boolean emitTermuxEvent(String eventName, String eventData) {
        if (eventName == null) return false;
        try {
            String safeEvent = eventName.trim();
            String safeData = (eventData != null) ? eventData : "{}";
            JSONObject obj = new JSONObject();
            obj.put("timestamp", System.currentTimeMillis());
            obj.put("event", safeEvent);
            obj.put("data", safeData);

            // 1. Log to Linux event stream in /tmp/fusion_events.jsonl
            File f = new File("/tmp/fusion_events.jsonl");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f, true)) {
                fos.write((obj.toString() + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Throwable ignored) {}

            // 2. Broadcast across tabs so any other tab can react
            broadcastTab("termux_" + safeEvent, safeData);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @JavascriptInterface
    public boolean setUserAgent(final String ua) {
        if (ua == null || ua.trim().isEmpty()) return false;
        final FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr == null || mgr.mActivity == null || mgr.mActiveWebView == null) return false;
        mgr.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mgr.mActiveWebView.getSettings().setUserAgentString(ua.trim());
                } catch (Throwable ignored) {}
            }
        });
        return true;
    }

    @JavascriptInterface
    public String getUserAgent() {
        FusionWebViewManager mgr = FusionWebViewManager.getInstance();
        if (mgr == null || mgr.mActiveWebView == null) return "";
        return mgr.mActiveWebView.getSettings().getUserAgentString();
    }
}
