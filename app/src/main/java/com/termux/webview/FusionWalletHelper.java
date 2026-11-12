package com.termux.webview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWalletHelper {
    public static boolean sLoaded = false;
    public static String sCustomWalletPath;
    public static int sMasterIndex = 0;
    public static String[] sNames = {"Master .env"};
    public static String[] sAddresses = {"0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359"};

    public static int sSolMasterIndex = 0;
    public static String[] sSolNames = {"Master Solana"};
    public static String[] sSolAddresses = {"8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf"};

    public static int sTronMasterIndex = 0;
    public static String[] sTronNames = {"Master Tron"};
    public static String[] sTronAddresses = {"TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF"};

    public static int sSuiMasterIndex = 0;
    public static String[] sSuiNames = {"Master Sui"};
    public static String[] sSuiAddresses = {"0xe11bb239021e68c0494c91b5ef96dfc507d7a00f653d95c4dec56ac9c220a151"};

    public static int sAptosMasterIndex = 0;
    public static String[] sAptosNames = {"Master Aptos"};
    public static String[] sAptosAddresses = {"0xb531dfa998a53cfcb83cbd26eaa5c2fe5c192d6e79c00eb737fdf0ab9ea1c9ae"};

    public static int sIotaMasterIndex = 0;
    public static String[] sIotaNames = {"eso-1"};
    public static String[] sIotaAddresses = {"0xdcc1545d60471021e3aca3f8de472083eec2ca8cc6db9e5109ed8f5d6a6c55fd"};

    public static boolean hasSolanaWallets() {
        return sSolAddresses != null && sSolAddresses.length > 0 && !sSolAddresses[0].isEmpty();
    }

    public static boolean hasTronWallets() {
        return sTronAddresses != null && sTronAddresses.length > 0 && !sTronAddresses[0].isEmpty();
    }

    public static boolean hasSuiWallets() {
        return sSuiAddresses != null && sSuiAddresses.length > 0 && !sSuiAddresses[0].isEmpty();
    }

    public static boolean hasAptosWallets() {
        return sAptosAddresses != null && sAptosAddresses.length > 0 && !sAptosAddresses[0].isEmpty();
    }

    public static boolean hasIotaWallets() {
        return sIotaAddresses != null && sIotaAddresses.length > 0 && !sIotaAddresses[0].isEmpty();
    }

    public static String getMasterTronAddress() {
        if (sTronAddresses == null || sTronAddresses.length == 0) return "";
        int i = sTronMasterIndex;
        if (i < 0 || i >= sTronAddresses.length) return sTronAddresses[0];
        return sTronAddresses[i];
    }

    public static String getMasterTronName() {
        if (sTronNames == null || sTronNames.length == 0) return "Master Tron";
        int i = sTronMasterIndex;
        if (i < 0 || i >= sTronNames.length) return sTronNames[0];
        return sTronNames[i];
    }

    public static String getTronAddress(int i) {
        if (sTronAddresses == null || sTronAddresses.length == 0) return "";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty() && sTronNames != null && sTronAddresses != null) {
            for (int j = 0; j < sTronNames.length && j < sTronAddresses.length; j++) {
                if (evmName.equalsIgnoreCase(sTronNames[j])) {
                    return sTronAddresses[j];
                }
            }
        }
        if (i >= 0 && i < sTronAddresses.length && sTronAddresses[i] != null && !sTronAddresses[i].isEmpty()) {
            return sTronAddresses[i];
        }
        return getMasterTronAddress();
    }

    public static String getTronName(int i) {
        if (sTronNames == null || sTronNames.length == 0) return "Master Tron";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty()) {
            for (String tn : sTronNames) {
                if (evmName.equalsIgnoreCase(tn)) return tn;
            }
        }
        if (i >= 0 && i < sTronNames.length && sTronNames[i] != null && !sTronNames[i].isEmpty()) {
            return sTronNames[i];
        }
        return getMasterTronName();
    }

    public static String[] getTronNames() {
        return sTronNames;
    }

    public static String[] getTronAddresses() {
        return sTronAddresses;
    }

    public static int getTronMasterIndex() {
        return sTronMasterIndex;
    }

    public static String findTronAddressByName(String str) {
        if (str == null) return getMasterTronAddress();
        if (sTronNames != null && sTronAddresses != null) {
            for (int i = 0; i < sTronNames.length && i < sTronAddresses.length; i++) {
                if (str.equalsIgnoreCase(sTronNames[i])) {
                    return sTronAddresses[i];
                }
            }
        }
        return getMasterTronAddress();
    }

    public static String getMasterSuiAddress() {
        if (sSuiAddresses == null || sSuiAddresses.length == 0) return "";
        int i = sSuiMasterIndex;
        if (i < 0 || i >= sSuiAddresses.length) return sSuiAddresses[0];
        return sSuiAddresses[i];
    }
    public static String getMasterSuiName() {
        if (sSuiNames == null || sSuiNames.length == 0) return "Master Sui";
        int i = sSuiMasterIndex;
        if (i < 0 || i >= sSuiNames.length) return sSuiNames[0];
        return sSuiNames[i];
    }
    public static String getSuiAddress(int i) {
        if (sSuiAddresses == null || sSuiAddresses.length == 0) return "";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty() && sSuiNames != null && sSuiAddresses != null) {
            for (int j = 0; j < sSuiNames.length && j < sSuiAddresses.length; j++) {
                if (evmName.equalsIgnoreCase(sSuiNames[j])) {
                    return sSuiAddresses[j];
                }
            }
        }
        if (i >= 0 && i < sSuiAddresses.length && sSuiAddresses[i] != null && !sSuiAddresses[i].isEmpty()) {
            return sSuiAddresses[i];
        }
        return getMasterSuiAddress();
    }
    public static String getSuiName(int i) {
        if (sSuiNames == null || sSuiNames.length == 0) return "Master Sui";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty()) {
            for (String sn : sSuiNames) {
                if (evmName.equalsIgnoreCase(sn)) return sn;
            }
        }
        if (i >= 0 && i < sSuiNames.length && sSuiNames[i] != null && !sSuiNames[i].isEmpty()) {
            return sSuiNames[i];
        }
        return getMasterSuiName();
    }

    public static String getMasterAptosAddress() {
        if (sAptosAddresses == null || sAptosAddresses.length == 0) return "";
        int i = sAptosMasterIndex;
        if (i < 0 || i >= sAptosAddresses.length) return sAptosAddresses[0];
        return sAptosAddresses[i];
    }
    public static String getMasterAptosName() {
        if (sAptosNames == null || sAptosNames.length == 0) return "Master Aptos";
        int i = sAptosMasterIndex;
        if (i < 0 || i >= sAptosNames.length) return sAptosNames[0];
        return sAptosNames[i];
    }
    public static String getAptosAddress(int i) {
        if (sAptosAddresses == null || sAptosAddresses.length == 0) return "";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty() && sAptosNames != null && sAptosAddresses != null) {
            for (int j = 0; j < sAptosNames.length && j < sAptosAddresses.length; j++) {
                if (evmName.equalsIgnoreCase(sAptosNames[j])) {
                    return sAptosAddresses[j];
                }
            }
        }
        if (i >= 0 && i < sAptosAddresses.length && sAptosAddresses[i] != null && !sAptosAddresses[i].isEmpty()) {
            return sAptosAddresses[i];
        }
        return getMasterAptosAddress();
    }
    public static String getAptosName(int i) {
        if (sAptosNames == null || sAptosNames.length == 0) return "Master Aptos";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty()) {
            for (String an : sAptosNames) {
                if (evmName.equalsIgnoreCase(an)) return an;
            }
        }
        if (i >= 0 && i < sAptosNames.length && sAptosNames[i] != null && !sAptosNames[i].isEmpty()) {
            return sAptosNames[i];
        }
        return getMasterAptosName();
    }

    public static String getMasterIotaAddress() {
        if (sIotaAddresses == null || sIotaAddresses.length == 0) return "";
        int i = sIotaMasterIndex;
        if (i < 0 || i >= sIotaAddresses.length) return sIotaAddresses[0];
        return sIotaAddresses[i];
    }
    public static String getMasterIotaName() {
        if (sIotaNames == null || sIotaNames.length == 0) return "Master IOTA";
        int i = sIotaMasterIndex;
        if (i < 0 || i >= sIotaNames.length) return sIotaNames[0];
        return sIotaNames[i];
    }
    public static String getIotaAddress(int i) {
        if (sIotaAddresses == null || sIotaAddresses.length == 0) return "";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty() && sIotaNames != null && sIotaAddresses != null) {
            for (int j = 0; j < sIotaNames.length && j < sIotaAddresses.length; j++) {
                if (evmName.equalsIgnoreCase(sIotaNames[j])) {
                    return sIotaAddresses[j];
                }
            }
        }
        if (i >= 0 && i < sIotaAddresses.length && sIotaAddresses[i] != null && !sIotaAddresses[i].isEmpty()) {
            return sIotaAddresses[i];
        }
        return getMasterIotaAddress();
    }
    public static String getIotaName(int i) {
        if (sIotaNames == null || sIotaNames.length == 0) return "Master IOTA";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty()) {
            for (String in : sIotaNames) {
                if (evmName.equalsIgnoreCase(in)) return in;
            }
        }
        if (i >= 0 && i < sIotaNames.length && sIotaNames[i] != null && !sIotaNames[i].isEmpty()) {
            return sIotaNames[i];
        }
        return getMasterIotaName();
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
        if (sSolAddresses == null || sSolAddresses.length == 0) return "";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty() && sSolNames != null && sSolAddresses != null) {
            for (int j = 0; j < sSolNames.length && j < sSolAddresses.length; j++) {
                if (evmName.equalsIgnoreCase(sSolNames[j])) {
                    return sSolAddresses[j];
                }
            }
        }
        if (i >= 0 && i < sSolAddresses.length && sSolAddresses[i] != null && !sSolAddresses[i].isEmpty()) {
            return sSolAddresses[i];
        }
        return getMasterSolAddress();
    }

    public static String getSolName(int i) {
        if (sSolNames == null || sSolNames.length == 0) return "Master Solana";
        String evmName = getName(i);
        if (evmName != null && !evmName.isEmpty()) {
            for (String sn : sSolNames) {
                if (evmName.equalsIgnoreCase(sn)) return sn;
            }
        }
        if (i >= 0 && i < sSolNames.length && sSolNames[i] != null && !sSolNames[i].isEmpty()) {
            return sSolNames[i];
        }
        return getMasterSolName();
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
        return getStartWalletDaemonCommand(null);
    }

    public static String getStartWalletDaemonCommand(String activeUrl) {
        String detected = FusionChainHelper.detectChainFromUrl(activeUrl);
        String net = (detected != null && !detected.isEmpty()) ? detected : FusionChainHelper.getCurrentChainKey();
        String netFlag = (net != null && !net.isEmpty()) ? ("--" + net + " ") : "";
        return "if command -v wallet >/dev/null 2>&1; then " +
               "wallet " + netFlag + "--auto --ghostwallet 3333; " +
               "else " +
               "for d in \"$FUSION_WALLET_DIR\" \"$HOME/wallet\" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do " +
               "if [ -n \"$d\" ] && [ -d \"$d\" ]; then " +
               "cd \"$d\" && { if [ -f wallet.cjs ]; then node wallet.cjs " + netFlag + "--auto --ghostwallet 3333; elif [ -f wallet.js ]; then node wallet.js " + netFlag + "--auto --ghostwallet 3333; fi; } && break; " +
               "fi; done; " +
               "fi";
    }

    private static volatile boolean sIsReloading = false;

    public static File findDatabaseFile() {
        String custom = sCustomWalletPath;
        if (custom != null) {
            try {
                File cf = new File(custom);
                if (cf.isDirectory()) {
                    cf = new File(cf, "eso.db");
                }
                if (cf.exists() && cf.canRead()) {
                    return cf;
                }
            } catch (Exception ignored) {}
        }
        String[] searchPaths = new String[]{
            new File(getWalletDir(), "eso.db").getAbsolutePath(),
            "/data/data/com.termux/files/home/wallet/eso.db",
            "/wallet/eso.db",
            "/root/wallet/eso.db",
            "/data/local/nhsystem/kali-arm64/root/wallet/eso.db"
        };
        for (String p : searchPaths) {
            try {
                File f = new File(p);
                if (f.exists() && f.canRead()) {
                    return f;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static boolean loadFromDatabase(File dbFile) {
        if (dbFile == null || !dbFile.exists()) return false;
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            boolean hasChainCol = false;
            Cursor pragma = db.rawQuery("PRAGMA table_info(wallets)", null);
            if (pragma != null) {
                while (pragma.moveToNext()) {
                    String col = pragma.getString(1);
                    if ("chain".equalsIgnoreCase(col)) {
                        hasChainCol = true;
                        break;
                    }
                }
                pragma.close();
            }

            // 1. EVM Wallets
            String evmQuery = hasChainCol
                ? "SELECT name, address, is_master FROM wallets WHERE chain='evm' OR chain IS NULL ORDER BY is_master DESC, id ASC"
                : "SELECT name, address, is_master FROM wallets ORDER BY is_master DESC, id ASC";
            loadChainFromCursor(db, evmQuery, "evm");

            // 2. Multichain Wallets
            if (hasChainCol) {
                loadChainFromCursor(db, "SELECT name, address, is_master FROM wallets WHERE chain='solana' ORDER BY is_master DESC, id ASC", "solana");
                loadChainFromCursor(db, "SELECT name, address, is_master FROM wallets WHERE chain='tron' ORDER BY is_master DESC, id ASC", "tron");
                loadChainFromCursor(db, "SELECT name, address, is_master FROM wallets WHERE chain='sui' ORDER BY is_master DESC, id ASC", "sui");
                loadChainFromCursor(db, "SELECT name, address, is_master FROM wallets WHERE chain='aptos' ORDER BY is_master DESC, id ASC", "aptos");
                loadChainFromCursor(db, "SELECT name, address, is_master FROM wallets WHERE chain='iota' ORDER BY is_master DESC, id ASC", "iota");
            }
            sLoaded = true;
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null) {
                try { db.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static void loadChainFromCursor(SQLiteDatabase db, String query, String chain) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.getCount() > 0) {
                int count = cursor.getCount();
                String[] names = new String[count];
                String[] addresses = new String[count];
                int masterIdx = 0;
                int idx = 0;
                while (cursor.moveToNext()) {
                    names[idx] = cursor.getString(0);
                    addresses[idx] = cursor.getString(1);
                    int isMaster = cursor.getInt(2);
                    if (isMaster == 1 && masterIdx == 0) {
                        masterIdx = idx;
                    }
                    idx++;
                }
                if ("evm".equals(chain)) {
                    sNames = names;
                    sAddresses = addresses;
                    sMasterIndex = masterIdx;
                } else if ("solana".equals(chain)) {
                    sSolNames = names;
                    sSolAddresses = addresses;
                    sSolMasterIndex = masterIdx;
                } else if ("tron".equals(chain)) {
                    sTronNames = names;
                    sTronAddresses = addresses;
                    sTronMasterIndex = masterIdx;
                } else if ("sui".equals(chain)) {
                    sSuiNames = names;
                    sSuiAddresses = addresses;
                    sSuiMasterIndex = masterIdx;
                } else if ("aptos".equals(chain)) {
                    sAptosNames = names;
                    sAptosAddresses = addresses;
                    sAptosMasterIndex = masterIdx;
                } else if ("iota".equals(chain)) {
                    sIotaNames = names;
                    sIotaAddresses = addresses;
                    sIotaMasterIndex = masterIdx;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                try { cursor.close(); } catch (Exception ignored) {}
            }
        }
    }

    public static void init(Context context) {
        if (sLoaded) {
            return;
        }
        File dbFile = findDatabaseFile();
        if (dbFile != null && loadFromDatabase(dbFile)) {
            return;
        }
        if (sAddresses == null || sAddresses.length == 0 || sAddresses[0] == null || sAddresses[0].isEmpty()) {
            sMasterIndex = 0;
            sNames = new String[]{"eso-1"};
            sAddresses = new String[]{"0x79E0EE08c6516d1204821f083285Ab65660c26E4"};
            sSolNames = new String[]{"eso-1"};
            sSolAddresses = new String[]{"8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf"};
            sTronNames = new String[]{"eso-1"};
            sTronAddresses = new String[]{"TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF"};
            sSuiNames = new String[]{"eso-1"};
            sSuiAddresses = new String[]{"0xe11bb239021e68c0494c91b5ef96dfc507d7a00f653d95c4dec56ac9c220a151"};
            sAptosNames = new String[]{"eso-1"};
            sAptosAddresses = new String[]{"0xb531dfa998a53cfcb83cbd26eaa5c2fe5c192d6e79c00eb737fdf0ab9ea1c9ae"};
            sIotaNames = new String[]{"eso-1"};
            sIotaAddresses = new String[]{"0xdcc1545d60471021e3aca3f8de472083eec2ca8cc6db9e5109ed8f5d6a6c55fd"};
        }
        if (!sIsReloading) {
            sIsReloading = true;
            new Thread(() -> {
                try {
                    reload(context);
                } catch (Throwable ignored) {
                } finally {
                    sIsReloading = false;
                }
            }, "FusionWalletLoader").start();
        }
    }

    public static void reload(Context context) {
        File dbFile = findDatabaseFile();
        if (dbFile != null && loadFromDatabase(dbFile)) {
            sLoaded = true;
            return;
        }
        reloadEvm(context);
        reloadSolana(context);
        reloadTron(context);
        reloadSui(context);
        reloadAptos(context);
        reloadIota(context);
        sLoaded = true;
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
        if (sSolAddresses == null || sSolAddresses.length == 0 || sSolAddresses[0].isEmpty()) {
            if (sAddresses != null && sAddresses.length > 0) {
                int len = sAddresses.length;
                String[] solA = new String[len];
                String[] solN = new String[len];
                for (int j = 0; j < len; j++) {
                    solN[j] = (sNames != null && j < sNames.length) ? sNames[j] : ("Sol-" + (j + 1));
                }
                if (len > 0) {
                    try {
                        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                        byte[] hash = md.digest(("sol:" + sAddresses[0]).getBytes("UTF-8"));
                        solA[0] = encodeBase58(hash);
                    } catch (Exception ignored) {}
                }
                sSolNames = solN;
                sSolAddresses = solA;
                sSolMasterIndex = sMasterIndex;
            }
        }
    }

    public static void reloadTron(Context context) {
        InputStream tronInputStream = null;
        String str = sCustomWalletPath;
        if (str != null) {
            try {
                File file = new File(str);
                if (file.isDirectory()) {
                    file = new File(file, "wallets_tron_db.json");
                }
                if (file.exists()) {
                    tronInputStream = new FileInputStream(file);
                }
            } catch (Exception unused) {
            }
        }
        if (tronInputStream == null) {
            String[] tronSearchPaths = new String[]{
                new File(getWalletDir(), "wallets_tron_db.json").getAbsolutePath(),
                "/data/data/com.termux/files/home/wallet/wallets_tron_db.json",
                "/wallet/wallets_tron_db.json",
                "/root/wallet/wallets_tron_db.json",
                "/data/local/nhsystem/kali-arm64/root/wallet/wallets_tron_db.json"
            };
            for (String p : tronSearchPaths) {
                try {
                    File f = new File(p);
                    if (f.exists() && f.canRead()) {
                        tronInputStream = new FileInputStream(f);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (tronInputStream != null) {
            try {
                ByteArrayOutputStream tronBaos = new ByteArrayOutputStream();
                byte[] tronBArr = new byte[4096];
                while (true) {
                    int i = tronInputStream.read(tronBArr);
                    if (i == -1) break;
                    tronBaos.write(tronBArr, 0, i);
                }
                tronInputStream.close();
                JSONObject tronObj = new JSONObject(new String(tronBaos.toByteArray(), "UTF-8"));
                sTronMasterIndex = tronObj.optInt("master_index", 0);
                JSONArray tronWallets = tronObj.optJSONArray("wallets");
                if (tronWallets != null && tronWallets.length() > 0) {
                    int tronLen = tronWallets.length();
                    String[] tronN = new String[tronLen];
                    String[] tronA = new String[tronLen];
                    for (int j = 0; j < tronLen; j++) {
                        JSONObject wObj = tronWallets.getJSONObject(j);
                        tronN[j] = wObj.optString("name", "Tron-" + (j + 1));
                        tronA[j] = wObj.optString("address", "");
                    }
                    sTronNames = tronN;
                    sTronAddresses = tronA;
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // Auto-Derive Tron address 1:1 from EVM addresses (sAddresses) lazily
        if (sAddresses != null && sAddresses.length > 0) {
            int len = sAddresses.length;
            String[] tronA = new String[len];
            String[] tronN = new String[len];
            for (int j = 0; j < len; j++) {
                tronN[j] = (sNames != null && j < sNames.length) ? sNames[j] : ("Tron-" + (j + 1));
            }
            if (len > 0) {
                tronA[0] = evmToTronAddress(sAddresses[0]);
            }
            sTronNames = tronN;
            sTronAddresses = tronA;
            sTronMasterIndex = sMasterIndex;
        }
    }

    private static final String ALPHABET_58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    public static String evmToTronAddress(String evmAddress) {
        if (evmAddress == null || evmAddress.length() < 10) return "";
        try {
            String clean = evmAddress.startsWith("0x") || evmAddress.startsWith("0X") 
                ? evmAddress.substring(2) : evmAddress;
            if (clean.length() != 40) return "";
            byte[] raw = new byte[21];
            raw[0] = 0x41; // Tron mainnet prefix (0x41)
            for (int i = 0; i < 20; i++) {
                raw[i + 1] = (byte) ((Character.digit(clean.charAt(i * 2), 16) << 4)
                                   + Character.digit(clean.charAt(i * 2 + 1), 16));
            }
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h1 = md.digest(raw);
            byte[] h2 = md.digest(h1);
            byte[] full = new byte[25];
            System.arraycopy(raw, 0, full, 0, 21);
            System.arraycopy(h2, 0, full, 21, 4); // 4-byte checksum
            return encodeBase58(full);
        } catch (Exception e) {
            return "";
        }
    }

    public static String encodeBase58(byte[] input) {
        if (input == null || input.length == 0) return "";
        java.math.BigInteger num = new java.math.BigInteger(1, input);
        java.math.BigInteger bi58 = java.math.BigInteger.valueOf(58);
        StringBuilder sb = new StringBuilder();
        while (num.compareTo(java.math.BigInteger.ZERO) > 0) {
            java.math.BigInteger[] divRem = num.divideAndRemainder(bi58);
            sb.append(ALPHABET_58.charAt(divRem[1].intValue()));
            num = divRem[0];
        }
        for (byte b : input) {
            if (b == 0) sb.append('1');
            else break;
        }
        return sb.reverse().toString();
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hashHex(String prefix, String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(prefix.getBytes("UTF-8"));
            return bytesToHex(md.digest(input.getBytes("UTF-8")));
        } catch (Exception e) {
            return "";
        }
    }

    public static void reloadSui(Context context) {
        InputStream is = null;
        String[] searchPaths = new String[]{
            new File(getWalletDir(), "wallets_sui_db.json").getAbsolutePath(),
            "/data/data/com.termux/files/home/wallet/wallets_sui_db.json",
            "/wallet/wallets_sui_db.json",
            "/root/wallet/wallets_sui_db.json"
        };
        for (String p : searchPaths) {
            try {
                File f = new File(p);
                if (f.exists() && f.canRead()) {
                    is = new FileInputStream(f);
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (is != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[4096];
                while (true) {
                    int n = is.read(b);
                    if (n == -1) break;
                    baos.write(b, 0, n);
                }
                is.close();
                JSONObject obj = new JSONObject(new String(baos.toByteArray(), "UTF-8"));
                sSuiMasterIndex = obj.optInt("master_index", 0);
                JSONArray arr = obj.optJSONArray("wallets");
                if (arr != null && arr.length() > 0) {
                    int len = arr.length();
                    String[] nArr = new String[len];
                    String[] aArr = new String[len];
                    for (int j = 0; j < len; j++) {
                        JSONObject w = arr.getJSONObject(j);
                        nArr[j] = w.optString("name", "Sui-" + (j + 1));
                        aArr[j] = w.optString("address", "");
                    }
                    sSuiNames = nArr;
                    sSuiAddresses = aArr;
                    return;
                }
            } catch (Exception ignored) {}
        }
        if (sAddresses != null && sAddresses.length > 0) {
            int len = sAddresses.length;
            String[] aArr = new String[len];
            String[] nArr = new String[len];
            for (int j = 0; j < len; j++) {
                nArr[j] = (sNames != null && j < sNames.length) ? sNames[j] : ("Sui-" + (j + 1));
                if (sAddresses[j] != null && !sAddresses[j].isEmpty()) {
                    aArr[j] = "0x" + hashHex("sui:", sAddresses[j]);
                }
            }
            sSuiNames = nArr;
            sSuiAddresses = aArr;
            sSuiMasterIndex = sMasterIndex;
        }
    }

    public static void reloadAptos(Context context) {
        InputStream is = null;
        String[] searchPaths = new String[]{
            new File(getWalletDir(), "wallets_aptos_db.json").getAbsolutePath(),
            "/data/data/com.termux/files/home/wallet/wallets_aptos_db.json",
            "/wallet/wallets_aptos_db.json",
            "/root/wallet/wallets_aptos_db.json"
        };
        for (String p : searchPaths) {
            try {
                File f = new File(p);
                if (f.exists() && f.canRead()) {
                    is = new FileInputStream(f);
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (is != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[4096];
                while (true) {
                    int n = is.read(b);
                    if (n == -1) break;
                    baos.write(b, 0, n);
                }
                is.close();
                JSONObject obj = new JSONObject(new String(baos.toByteArray(), "UTF-8"));
                sAptosMasterIndex = obj.optInt("master_index", 0);
                JSONArray arr = obj.optJSONArray("wallets");
                if (arr != null && arr.length() > 0) {
                    int len = arr.length();
                    String[] nArr = new String[len];
                    String[] aArr = new String[len];
                    for (int j = 0; j < len; j++) {
                        JSONObject w = arr.getJSONObject(j);
                        nArr[j] = w.optString("name", "Aptos-" + (j + 1));
                        aArr[j] = w.optString("address", "");
                    }
                    sAptosNames = nArr;
                    sAptosAddresses = aArr;
                    return;
                }
            } catch (Exception ignored) {}
        }
        if (sAddresses != null && sAddresses.length > 0) {
            int len = sAddresses.length;
            String[] aArr = new String[len];
            String[] nArr = new String[len];
            for (int j = 0; j < len; j++) {
                nArr[j] = (sNames != null && j < sNames.length) ? sNames[j] : ("Aptos-" + (j + 1));
                if (sAddresses[j] != null && !sAddresses[j].isEmpty()) {
                    aArr[j] = "0x" + hashHex("aptos:", sAddresses[j]);
                }
            }
            sAptosNames = nArr;
            sAptosAddresses = aArr;
            sAptosMasterIndex = sMasterIndex;
        }
    }

    public static void reloadIota(Context context) {
        InputStream is = null;
        String[] searchPaths = new String[]{
            new File(getWalletDir(), "wallets_iota_db.json").getAbsolutePath(),
            "/data/data/com.termux/files/home/wallet/wallets_iota_db.json",
            "/wallet/wallets_iota_db.json",
            "/root/wallet/wallets_iota_db.json"
        };
        for (String p : searchPaths) {
            try {
                File f = new File(p);
                if (f.exists() && f.canRead()) {
                    is = new FileInputStream(f);
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (is != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[4096];
                while (true) {
                    int n = is.read(b);
                    if (n == -1) break;
                    baos.write(b, 0, n);
                }
                is.close();
                JSONObject obj = new JSONObject(new String(baos.toByteArray(), "UTF-8"));
                sIotaMasterIndex = obj.optInt("master_index", 0);
                JSONArray arr = obj.optJSONArray("wallets");
                if (arr != null && arr.length() > 0) {
                    int len = arr.length();
                    String[] nArr = new String[len];
                    String[] aArr = new String[len];
                    for (int j = 0; j < len; j++) {
                        JSONObject w = arr.getJSONObject(j);
                        nArr[j] = w.optString("name", "IOTA-" + (j + 1));
                        aArr[j] = w.optString("address", "");
                    }
                    sIotaNames = nArr;
                    sIotaAddresses = aArr;
                    return;
                }
            } catch (Exception ignored) {}
        }
        if (sAddresses != null && sAddresses.length > 0) {
            int len = sAddresses.length;
            String[] aArr = new String[len];
            String[] nArr = new String[len];
            for (int j = 0; j < len; j++) {
                nArr[j] = (sNames != null && j < sNames.length) ? sNames[j] : ("IOTA-" + (j + 1));
                if (sAddresses[j] != null && !sAddresses[j].isEmpty()) {
                    aArr[j] = "0x" + hashHex("iota:", sAddresses[j]);
                }
            }
            sIotaNames = nArr;
            sIotaAddresses = aArr;
            sIotaMasterIndex = sMasterIndex;
        }
    }

    public static void setCustomWalletPath(String str) {
        sCustomWalletPath = str;
    }
}
