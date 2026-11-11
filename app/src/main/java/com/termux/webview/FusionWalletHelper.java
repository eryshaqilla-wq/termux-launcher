package com.termux.webview;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWalletHelper {
    public static String sCustomWalletPath;
    public static int sMasterIndex = 0;
    public static String[] sNames = {"Master .env"};
    public static String[] sAddresses = {"0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359"};

    public static int sSolMasterIndex = 0;
    public static String[] sSolNames = {"Master Solana"};
    public static String[] sSolAddresses = {""};

    public static boolean hasSolanaWallets() {
        return sSolAddresses != null && sSolAddresses.length > 0 && !sSolAddresses[0].isEmpty();
    }

    public static String getMasterSolAddress() {
        if (sSolAddresses == null || sSolAddresses.length == 0) return "";
        int i = sSolMasterIndex;
        if (i < 0 || i >= sSolAddresses.length) return sSolAddresses[0];
        return sSolAddresses[i];
    }

    public static String getMasterSolName() {
        if (sSolNames == null || sSolNames.length == 0) return "Master Solana";
        int i = sSolMasterIndex;
        if (i < 0 || i >= sSolNames.length) return sSolNames[0];
        return sSolNames[i];
    }

    public static String getSolAddress(int i) {
        if (sSolAddresses == null || i < 0 || i >= sSolAddresses.length) return "";
        return sSolAddresses[i];
    }

    public static String getSolName(int i) {
        if (sSolNames == null || i < 0 || i >= sSolNames.length) return "";
        return sSolNames[i];
    }

    public static String[] getSolNames() {
        return sSolNames;
    }

    public static String[] getSolAddresses() {
        return sSolAddresses;
    }

    public static int getSolMasterIndex() {
        return sSolMasterIndex;
    }

    public static String findSolAddressByName(String str) {
        if (str == null) return getMasterSolAddress();
        if (sSolNames != null && sSolAddresses != null) {
            for (int i = 0; i < sSolNames.length && i < sSolAddresses.length; i++) {
                if (str.equalsIgnoreCase(sSolNames[i])) {
                    return sSolAddresses[i];
                }
            }
        }
        return getMasterSolAddress();
    }

    public static String findAddressByName(String str) {
        if (str == null) {
            return getMasterAddress();
        }
        String[] strArr = sNames;
        for (int i = 0; i < strArr.length; i++) {
            if (str.equalsIgnoreCase(strArr[i])) {
                return sAddresses[i];
            }
        }
        return getMasterAddress();
    }

    public static int findIndexByName(String str) {
        if (str == null) {
            return sMasterIndex;
        }
        String[] strArr = sNames;
        for (int i = 0; i < strArr.length; i++) {
            if (str.equalsIgnoreCase(strArr[i])) {
                return i;
            }
        }
        return sMasterIndex;
    }

    public static String getAddress(int i) {
        String[] strArr = sAddresses;
        return (i < 0 || i >= strArr.length) ? "" : strArr[i];
    }

    public static String getMasterAddress() {
        String[] strArr = sAddresses;
        int i = sMasterIndex;
        if (i < 0 || i >= strArr.length) {
            return strArr.length > 0 ? strArr[0] : "";
        }
        return strArr[i];
    }

    public static int getMasterIndex() {
        return sMasterIndex;
    }

    public static String getMasterName() {
        String[] strArr = sNames;
        int i = sMasterIndex;
        if (i < 0 || i >= strArr.length) {
            return strArr.length > 0 ? strArr[0] : "Master .env";
        }
        return strArr[i];
    }

    public static String getName(int i) {
        String[] strArr = sNames;
        return (i < 0 || i >= strArr.length) ? "" : strArr[i];
    }

    public static String[] getNames() {
        return sNames;
    }

    public static String getWalletDir() {
        String str = sCustomWalletPath;
        if (str != null) {
            File file = new File(str);
            if (file.isDirectory()) {
                return str;
            }
            String parent = file.getParent();
            if (parent != null) {
                return parent;
            }
        }

        // 1. Baca konfigurasi dinamis dari .fusion_env jika tersedia
        try {
            File envFile = new File("/data/data/com.termux/files/home/.fusion_env");
            if (envFile.exists() && envFile.canRead()) {
                FileInputStream fis = new FileInputStream(envFile);
                byte[] b = new byte[(int) envFile.length()];
                int readBytes = fis.read(b);
                fis.close();
                if (readBytes > 0) {
                    String content = new String(b, 0, readBytes, "UTF-8");
                    for (String line : content.split("\n")) {
                        if (line.startsWith("export FUSION_WALLET_DIR=")) {
                            String dir = line.substring(25).replace("\"", "").replace("'", "").trim();
                            if (dir.length() > 0) {
                                return dir;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Utamakan folder wallet lokal Termux
        File localWallet = new File("/data/data/com.termux/files/home/wallet");
        if (localWallet.exists()) {
            return "/data/data/com.termux/files/home/wallet";
        }

        // 3. Cek symlink /wallet
        File directWallet = new File("/wallet");
        if (directWallet.exists()) {
            return "/wallet";
        }

        // 4. Cek Kali NetHunter Chroot (jika ada)
        File nhWallet = new File("/data/local/nhsystem/kali-arm64/root/wallet");
        if (nhWallet.exists()) {
            return "/data/local/nhsystem/kali-arm64/root/wallet";
        }

        // 5. Cek /root/wallet hanya jika ada dan bisa dibaca (root mode)
        File rootWallet = new File("/root/wallet");
        if (rootWallet.exists() && rootWallet.canRead()) {
            return "/root/wallet";
        }

        // Default mutlak non-root
        return "/data/data/com.termux/files/home/wallet";
    }

    public static String getDetectedEngine() {
        // 1. Cek dari .fusion_env terlebih dahulu
        try {
            File envFile = new File("/data/data/com.termux/files/home/.fusion_env");
            if (!envFile.exists() || !envFile.canRead()) {
                envFile = new File("/root/.fusion_env");
            }
            if (envFile.exists() && envFile.canRead()) {
                FileInputStream fis = new FileInputStream(envFile);
                byte[] b = new byte[(int) envFile.length()];
                int readBytes = fis.read(b);
                fis.close();
                if (readBytes > 0) {
                    String content = new String(b, 0, readBytes, "UTF-8");
                    for (String line : content.split("\n")) {
                        if (line.startsWith("export FUSION_ENGINE_MODE=")) {
                            String mode = line.substring(26).replace("\"", "").replace("'", "").trim();
                            if ("cjs".equalsIgnoreCase(mode)) return "CJS";
                            if ("js".equalsIgnoreCase(mode)) return "JS";
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Cek file fisik di direktori wallet yang terdeteksi
        String[] dirs = new String[]{
            getWalletDir(),
            "/data/data/com.termux/files/home/wallet",
            "/wallet",
            "/root/wallet",
            "/data/local/nhsystem/kali-arm64/root/wallet"
        };
        for (String d : dirs) {
            if (d == null) continue;
            try {
                File dirFile = new File(d);
                if (new File(dirFile, "wallet.cjs").exists()) {
                    return "CJS";
                }
                if (new File(dirFile, "wallet.js").exists()) {
                    return "JS";
                }
            } catch (Exception ignored) {
            }
        }
        return "CJS"; // Default suite baru
    }

    public static String getStartWalletDaemonCommand() {
        return "if command -v wallet >/dev/null 2>&1; then " +
               "wallet --auto --ghostwallet 3333; " +
               "else " +
               "for d in \"$FUSION_WALLET_DIR\" \"$HOME/wallet\" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do " +
               "if [ -n \"$d\" ] && [ -d \"$d\" ]; then " +
               "cd \"$d\" && { if [ -f wallet.cjs ]; then node wallet.cjs --auto --ghostwallet 3333; elif [ -f wallet.js ]; then node wallet.js --auto --ghostwallet 3333; fi; } && break; " +
               "fi; done; " +
               "fi";
    }

    public static void init(Context context) {
        reload(context);
    }

    public static void reload(Context context) {
        reloadEvm(context);
        reloadSolana(context);
    }

    public static void reloadEvm(Context context) {
        InputStream fileInputStream = null;
        String str = sCustomWalletPath;
        if (str != null) {
            try {
                File file = new File(str);
                if (file.isDirectory()) {
                    file = new File(file, "wallets_db.json");
                }
                if (file.exists()) {
                    fileInputStream = new FileInputStream(file);
                }
            } catch (Exception unused) {
            }
        }
        if (fileInputStream == null) {
            String[] searchPaths = new String[]{
                new File(getWalletDir(), "wallets_db.json").getAbsolutePath(),
                "/data/data/com.termux/files/home/wallet/wallets_db.json",
                "/wallet/wallets_db.json",
                "/root/wallet/wallets_db.json",
                "/data/local/nhsystem/kali-arm64/root/wallet/wallets_db.json"
            };
            for (String p : searchPaths) {
                try {
                    File f = new File(p);
                    if (f.exists() && f.canRead()) {
                        fileInputStream = new FileInputStream(f);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (fileInputStream == null && context != null) {
            try {
                fileInputStream = context.getAssets().open("webview_engine/wallets_db.json");
            } catch (Exception unused5) {
            }
        }
        if (fileInputStream == null) {
            return;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[4096];
            while (true) {
                int i = fileInputStream.read(bArr);
                if (i == -1) {
                    break;
                } else {
                    byteArrayOutputStream.write(bArr, 0, i);
                }
            }
            fileInputStream.close();
            JSONObject jSONObject = new JSONObject(new String(byteArrayOutputStream.toByteArray(), "UTF-8"));
            sMasterIndex = jSONObject.optInt("master_index", 0);
            JSONArray jSONArray = jSONObject.getJSONArray("wallets");
            int length = jSONArray.length();
            if (length == 0) {
                return;
            }
            String[] strArr = new String[length];
            String[] strArr2 = new String[length];
            for (int i2 = 0; i2 < length; i2++) {
                JSONObject jSONObject2 = jSONArray.getJSONObject(i2);
                strArr[i2] = jSONObject2.getString("name");
                strArr2[i2] = jSONObject2.getString("address");
            }
            sNames = strArr;
            sAddresses = strArr2;
        } catch (Exception unused6) {
        }
    }

    public static void reloadSolana(Context context) {
        InputStream solInputStream = null;
        String str = sCustomWalletPath;
        if (str != null) {
            try {
                File file = new File(str);
                if (file.isDirectory()) {
                    file = new File(file, "wallets_solana_db.json");
                }
                if (file.exists()) {
                    solInputStream = new FileInputStream(file);
                }
            } catch (Exception unused) {
            }
        }
        if (solInputStream == null) {
            String[] solSearchPaths = new String[]{
                new File(getWalletDir(), "wallets_solana_db.json").getAbsolutePath(),
                "/data/data/com.termux/files/home/wallet/wallets_solana_db.json",
                "/wallet/wallets_solana_db.json",
                "/root/wallet/wallets_solana_db.json",
                "/data/local/nhsystem/kali-arm64/root/wallet/wallets_solana_db.json"
            };
            for (String p : solSearchPaths) {
                try {
                    File f = new File(p);
                    if (f.exists() && f.canRead()) {
                        solInputStream = new FileInputStream(f);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (solInputStream == null) {
            return;
        }
        try {
            ByteArrayOutputStream solBaos = new ByteArrayOutputStream();
            byte[] solBArr = new byte[4096];
            while (true) {
                int i = solInputStream.read(solBArr);
                if (i == -1) break;
                solBaos.write(solBArr, 0, i);
            }
            solInputStream.close();
            JSONObject solObj = new JSONObject(new String(solBaos.toByteArray(), "UTF-8"));
            sSolMasterIndex = solObj.optInt("master_index", 0);
            JSONArray solWallets = solObj.optJSONArray("wallets");
            if (solWallets != null && solWallets.length() > 0) {
                int solLen = solWallets.length();
                String[] solN = new String[solLen];
                String[] solA = new String[solLen];
                for (int j = 0; j < solLen; j++) {
                    JSONObject wObj = solWallets.getJSONObject(j);
                    solN[j] = wObj.optString("name", "Sol-" + (j + 1));
                    solA[j] = wObj.optString("address", "");
                }
                sSolNames = solN;
                sSolAddresses = solA;
            }
        } catch (Exception ignored) {
        }
    }

    public static void setCustomWalletPath(String str) {
        sCustomWalletPath = str;
    }
}
