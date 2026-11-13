package com.termux.webview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionChainHelper {
    public static String sCurrentChainKey = "base";
    public static int sCurrentChainId = 8453;
    public static String sCurrentChainName = "Base Mainnet";
    public static String[] sChainKeys = {"base", "bsc", "arbitrum", "polygon", "morph", "arc-mainnet"};
    public static String[] sChainNames = {"Base Mainnet", "Binance Smart Chain", "Arbitrum One", "Polygon (Matic)", "Morph L2", "Arc"};
    public static String[] sChainDisplayNames = {"Base Mainnet (8453)", "Binance Smart Chain (56)", "Arbitrum One (42161)", "Polygon (Matic) (137)", "Morph L2 (2818)", "Arc (5042)"};
    public static int[] sChainIds = {8453, 56, 42161, 137, 2818, 5042};

    public static String[] getChainDisplayNames() {
        return sChainDisplayNames;
    }

    public static String[] getNames() {
        return sChainDisplayNames != null ? sChainDisplayNames : sChainNames;
    }

    public static String getKeyByIndex(int i) {
        String[] strArr = sChainKeys;
        if (strArr == null || i < 0 || i >= strArr.length) {
            return null;
        }
        return strArr[i];
    }

    public static String getChainIdDec() {
        return String.valueOf(sCurrentChainId);
    }

    public static String getChainIdHex() {
        return "0x" + Integer.toHexString(sCurrentChainId);
    }

    public static String[] getChainKeys() {
        return sChainKeys;
    }

    public static int getCurrentChainId() {
        return sCurrentChainId;
    }

    public static String getCurrentChainKey() {
        return sCurrentChainKey;
    }

    public static String getCurrentChainName() {
        return sCurrentChainName;
    }

    public static boolean isSolana() {
        return "solana".equalsIgnoreCase(sCurrentChainKey) || sCurrentChainId == 501;
    }

    public static boolean isTron() {
        return "tron".equalsIgnoreCase(sCurrentChainKey) || sCurrentChainId == 728126428 || "trx".equalsIgnoreCase(sCurrentChainKey);
    }

    public static boolean isSui() {
        return "sui".equalsIgnoreCase(sCurrentChainKey) || sCurrentChainId == 784;
    }

    public static boolean isAptos() {
        return "aptos".equalsIgnoreCase(sCurrentChainKey) || "apt".equalsIgnoreCase(sCurrentChainKey) || sCurrentChainId == 637;
    }

    public static boolean isIota() {
        return "iota".equalsIgnoreCase(sCurrentChainKey) || "iotaevm".equalsIgnoreCase(sCurrentChainKey) || sCurrentChainId == 4218 || sCurrentChainId == 8822;
    }

    public static String detectChainFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String lower = url.toLowerCase();
        if (lower.contains("jup.ag") || lower.contains("raydium") || lower.contains("pump.fun") || 
            lower.contains("solana") || lower.contains("magiceden") || lower.contains("tensor.trade") || 
            lower.contains("orca.so") || lower.contains("birdeye.so") || lower.contains("phantom.app") || 
            lower.contains("solscan") || lower.contains("solflare")) {
            return "solana";
        }
        if (lower.contains("iota.org") || lower.contains("iota.cafe") || lower.contains("magicsea") || 
            lower.contains("iotabee") || lower.contains("tangle") || lower.contains("iota")) {
            return "iota";
        }
        if (lower.contains("sui.io") || lower.contains("suiscan") || lower.contains("cetus.zone") || 
            lower.contains("turbos") || lower.contains("navi.protocol") || lower.contains("scallop") || 
            lower.contains("aftermath") || lower.contains("bluefin") || lower.contains("sui")) {
            return "sui";
        }
        if (lower.contains("aptoslabs") || lower.contains("aptoscan") || lower.contains("panora") || 
            lower.contains("thalas") || lower.contains("liquidswap") || lower.contains("amnis") || 
            lower.contains("aptos")) {
            return "aptos";
        }
        if (lower.contains("tronscan") || lower.contains("sunpump") || lower.contains("sun.io") || 
            lower.contains("justlend") || lower.contains("trondao") || lower.contains("tron")) {
            return "tron";
        }
        if (lower.contains("pancakeswap") || lower.contains("bscscan") || lower.contains("four.meme") || 
            lower.contains("binance") || lower.contains("bnbchain") || lower.contains("bsc")) {
            return "bsc";
        }
        if (lower.contains("arbiscan") || lower.contains("arbitrum") || lower.contains("gmx.io") || 
            lower.contains("camelot")) {
            return "arbitrum";
        }
        if (lower.contains("polygonscan") || lower.contains("polygon") || lower.contains("quickswap")) {
            return "polygon";
        }
        if (lower.contains("morphl2") || lower.contains("morphscan") || lower.contains("bulbaswap")) {
            return "morph";
        }
        if (lower.contains("etherscan") || lower.contains("uniswap") || lower.contains("curve.fi") || 
            lower.contains("lido.fi") || lower.contains("opensea")) {
            return "eth";
        }
        if (lower.contains("base.org") || lower.contains("basescan") || lower.contains("aerodrome") || 
            lower.contains("virtuals.io") || lower.contains("clanker")) {
            return "base";
        }
        return null;
    }

    public static void init(Context context) {
        reload(context);
    }

    public static boolean loadFromDatabase(File dbFile) {
        if (dbFile == null || !dbFile.exists() || !dbFile.canRead()) return false;
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c = db.rawQuery("SELECT key, name, chain_id FROM chains ORDER BY rowid ASC", null);
            if (c == null) return false;
            List<String> keys = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            while (c.moveToNext()) {
                String key = c.getString(0);
                String name = c.getString(1);
                int chainId = c.getInt(2);
                if (key != null && !key.trim().isEmpty()) {
                    keys.add(key.trim());
                    names.add(name != null ? name.trim() : key.trim());
                    ids.add(chainId);
                }
            }
            c.close();
            int size = keys.size();
            if (size == 0) return false;
            String[] strArr = new String[size];
            String[] strArr2 = new String[size];
            String[] strArr3 = new String[size];
            int[] iArr = new int[size];
            for (int i = 0; i < size; i++) {
                strArr[i] = keys.get(i);
                strArr2[i] = names.get(i);
                iArr[i] = ids.get(i);
                strArr3[i] = strArr2[i] + (iArr[i] > 0 ? " (" + iArr[i] + ")" : "");
            }
            sChainKeys = strArr;
            sChainNames = strArr2;
            sChainDisplayNames = strArr3;
            sChainIds = iArr;
            if (sCurrentChainKey != null) {
                setChain(sCurrentChainKey);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (db != null) {
                try { db.close(); } catch (Exception ignored) {}
            }
        }
    }

    public static void reload(Context context) {
        // 1. Try loading chains from central SQLite (eso.db) first (<1ms)
        try {
            File dbFile = FusionWalletHelper.findDatabaseFile();
            if (dbFile != null && loadFromDatabase(dbFile)) {
                return;
            }
        } catch (Exception ignored) {}

        // 2. Fallback to chains_db.json
        InputStream fileInputStream = null;
        try {
            File file = new File("/data/data/com.termux/files/home/wallet/chains_db.json");
            if (file.exists() && file.canRead()) {
                fileInputStream = new FileInputStream(file);
            }
        } catch (Exception unused) {
        }
        if (fileInputStream == null) {
            try {
                File file2 = new File("/data/local/nhsystem/kali-arm64/root/wallet/chains_db.json");
                if (file2.exists()) {
                    fileInputStream = new FileInputStream(file2);
                }
            } catch (Exception unused2) {
            }
            if (fileInputStream == null) {
                try {
                    File file3 = new File("/root/wallet/chains_db.json");
                    if (file3.exists()) {
                        fileInputStream = new FileInputStream(file3);
                    }
                } catch (Exception unused3) {
                }
            }
        }
        if (fileInputStream == null && context != null) {
            try {
                fileInputStream = context.getAssets().open("webview_engine/chains_db.json");
            } catch (Exception unused4) {
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
            ArrayList arrayList = new ArrayList();
            Iterator<String> itKeys = jSONObject.keys();
            while (itKeys.hasNext()) {
                arrayList.add(itKeys.next());
            }
            int size = arrayList.size();
            if (size == 0) {
                return;
            }
            String[] strArr = new String[size];
            String[] strArr2 = new String[size];
            String[] strArr3 = new String[size];
            int[] iArr = new int[size];
            for (int i2 = 0; i2 < size; i2++) {
                String str = (String) arrayList.get(i2);
                strArr[i2] = str;
                JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject(str);
                if (jSONObjectOptJSONObject != null) {
                    strArr2[i2] = jSONObjectOptJSONObject.optString("name");
                    iArr[i2] = jSONObjectOptJSONObject.optInt("id");
                } else {
                    strArr2[i2] = strArr[i2];
                    iArr[i2] = 1;
                }
                strArr3[i2] = strArr2[i2] + " (" + iArr[i2] + ")";
            }
            sChainKeys = strArr;
            sChainNames = strArr2;
            sChainDisplayNames = strArr3;
            sChainIds = iArr;
            String str2 = sCurrentChainKey;
            if (str2 != null) {
                setChain(str2);
            }
        } catch (Exception unused5) {
        }
    }

    public static void setChain(String str) {
        if (str == null) {
            str = "base";
        }
        if ("pol".equalsIgnoreCase(str) || "matic".equalsIgnoreCase(str)) {
            str = "polygon";
        } else if ("arb".equalsIgnoreCase(str)) {
            str = "arbitrum";
        } else if ("arc".equalsIgnoreCase(str)) {
            str = "arc-mainnet";
        } else if ("sol".equalsIgnoreCase(str)) {
            str = "solana";
        } else if ("trx".equalsIgnoreCase(str)) {
            str = "tron";
        } else if ("sui".equalsIgnoreCase(str)) {
            str = "sui";
        } else if ("apt".equalsIgnoreCase(str) || "aptos".equalsIgnoreCase(str)) {
            str = "aptos";
        } else if ("iota".equalsIgnoreCase(str) || "iotaevm".equalsIgnoreCase(str)) {
            str = "iota";
        }
        if (str.startsWith("0x") || str.startsWith("0X")) {
            setChainByHex(str);
            return;
        }
        String[] strArr = sChainKeys;
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                if (str.equalsIgnoreCase(strArr[i])) {
                    setChainByIndex(i);
                    return;
                }
            }
        }
        try {
            setChainById(Integer.parseInt(str));
        } catch (Exception unused) {
            String[] strArr2 = sChainKeys;
            if (strArr2 == null || strArr2.length <= 0) {
                return;
            }
            setChainByIndex(0);
        }
    }

    public static void setChainByHex(String str) {
        if (str == null || str.length() == 0) {
            return;
        }
        try {
            setChainById((str.startsWith("0x") || str.startsWith("0X")) ? (int) Long.parseLong(str.substring(2), 16) : (int) Long.parseLong(str, 10));
        } catch (Exception unused) {
        }
    }

    public static void setChainById(int i) {
        int[] iArr = sChainIds;
        if (iArr != null) {
            for (int i2 = 0; i2 < iArr.length; i2++) {
                if (iArr[i2] == i) {
                    setChainByIndex(i2);
                    return;
                }
            }
        }
        sCurrentChainId = i;
        switch (i) {
            case 8453:
                sCurrentChainKey = "base";
                sCurrentChainName = "Base Mainnet";
                break;
            case 56:
                sCurrentChainKey = "bsc";
                sCurrentChainName = "Binance Smart Chain";
                break;
            case 42161:
                sCurrentChainKey = "arbitrum";
                sCurrentChainName = "Arbitrum One";
                break;
            case 137:
                sCurrentChainKey = "polygon";
                sCurrentChainName = "Polygon (Matic)";
                break;
            case 2818:
                sCurrentChainKey = "morph";
                sCurrentChainName = "Morph L2";
                break;
            case 5042:
                sCurrentChainKey = "arc-mainnet";
                sCurrentChainName = "Arc";
                break;
            case 1:
                sCurrentChainKey = "eth";
                sCurrentChainName = "Ethereum Mainnet";
                break;
            case 501:
                sCurrentChainKey = "solana";
                sCurrentChainName = "Solana Mainnet";
                break;
            case 728126428:
                sCurrentChainKey = "tron";
                sCurrentChainName = "Tron Mainnet";
                break;
            case 784:
                sCurrentChainKey = "sui";
                sCurrentChainName = "Sui Mainnet";
                break;
            case 101:
            case 637:
                sCurrentChainKey = "aptos";
                sCurrentChainName = "Aptos Mainnet";
                break;
            case 4218:
            case 8822:
                sCurrentChainKey = "iota";
                sCurrentChainName = "IOTA Rebased Mainnet";
                break;
            default:
                sCurrentChainKey = "chain-" + i;
                sCurrentChainName = "Chain " + i;
                break;
        }
    }

    public static void setChainByIndex(int i) {
        String[] strArr = sChainKeys;
        if (strArr == null || i < 0 || i >= strArr.length) {
            return;
        }
        sCurrentChainKey = strArr[i];
        sCurrentChainName = sChainNames[i];
        sCurrentChainId = sChainIds[i];
    }
}
