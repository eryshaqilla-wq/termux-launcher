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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionChainHelper {
    public static class DynamicChainInfo {
        public String key;
        public String name;
        public String symbol;
        public int chainId;
        public boolean isNonEvm;
        public String[] urlPatterns;

        public DynamicChainInfo(String key, String name, String symbol, int chainId, boolean isNonEvm, String[] urlPatterns) {
            this.key = key;
            this.name = name;
            this.symbol = symbol;
            this.chainId = chainId;
            this.isNonEvm = isNonEvm;
            this.urlPatterns = urlPatterns != null ? urlPatterns : new String[0];
        }
    }

    public static final ConcurrentHashMap<String, DynamicChainInfo> sDynamicChains = new ConcurrentHashMap<>();

    static {
        registerDynamicChain("solana", "Solana Mainnet", "Sol", 501, true, new String[]{"jup.ag", "raydium", "pump.fun", "solana", "magiceden", "tensor.trade", "orca.so", "birdeye.so", "phantom.app", "solscan", "solflare"});
        registerDynamicChain("tron", "Tron Mainnet", "Tron", 728126428, true, new String[]{"tronscan", "sunpump", "sun.io", "justlend", "trondao", "tron"});
        registerDynamicChain("sui", "Sui Mainnet", "Sui", 784, true, new String[]{"sui.io", "suiscan", "cetus.zone", "turbos", "navi.protocol", "scallop", "aftermath", "bluefin", "sui"});
        registerDynamicChain("aptos", "Aptos Mainnet", "Aptos", 637, true, new String[]{"aptoslabs", "aptoscan", "panora", "thalas", "liquidswap", "amnis", "aptos"});
        registerDynamicChain("iota", "IOTA Rebased Mainnet", "IOTA", 4218, true, new String[]{"iota.org", "iota.cafe", "magicsea", "iotabee", "tangle", "iota"});
        registerDynamicChain("substrate", "FLOP Devnet", "FLOP", 42, true, new String[]{"flop.network", "faucet.flop.network", "zkverify.io", "substrate"});
        registerDynamicChain("polkadot", "Polkadot Mainnet", "DOT", 0, true, new String[]{"polkadot.js.org", "polkadot.network", "subscan.io", "talisman.xyz", "subwallet.app", "kusama", "polkadot"});
    }

    public static void registerDynamicChain(String key, String name, String symbol, int chainId, boolean isNonEvm, String[] urlPatterns) {
        if (key == null || key.trim().isEmpty()) return;
        String k = key.trim().toLowerCase();
        DynamicChainInfo info = new DynamicChainInfo(k, name != null ? name : k, symbol != null ? symbol : k.toUpperCase(), chainId, isNonEvm, urlPatterns);
        sDynamicChains.put(k, info);

        if ("substrate".equals(k)) {
            sDynamicChains.put("flop", info);
        } else if ("polkadot".equals(k)) {
            sDynamicChains.put("dot", info);
            sDynamicChains.put("ksm", info);
        } else if ("solana".equals(k)) {
            sDynamicChains.put("sol", info);
        } else if ("tron".equals(k)) {
            sDynamicChains.put("trx", info);
        } else if ("aptos".equals(k)) {
            sDynamicChains.put("apt", info);
        }
        ensureInChainArrays(info);
    }

    private static synchronized void ensureInChainArrays(DynamicChainInfo info) {
        if (sChainKeys == null) return;
        for (String k : sChainKeys) {
            if (info.key.equalsIgnoreCase(k)) return;
        }
        int oldLen = sChainKeys.length;
        String[] newKeys = new String[oldLen + 1];
        String[] newNames = new String[oldLen + 1];
        String[] newDisp = new String[oldLen + 1];
        int[] newIds = new int[oldLen + 1];

        System.arraycopy(sChainKeys, 0, newKeys, 0, oldLen);
        System.arraycopy(sChainNames, 0, newNames, 0, oldLen);
        System.arraycopy(sChainDisplayNames, 0, newDisp, 0, oldLen);
        System.arraycopy(sChainIds, 0, newIds, 0, oldLen);

        newKeys[oldLen] = info.key;
        newNames[oldLen] = info.name;
        newDisp[oldLen] = info.name + (info.chainId > 0 ? " (" + info.chainId + ")" : "");
        newIds[oldLen] = info.chainId;

        sChainKeys = newKeys;
        sChainNames = newNames;
        sChainDisplayNames = newDisp;
        sChainIds = newIds;
    }

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

    public static String getCurrentChainSymbol() {
        if (sDynamicChains != null && sCurrentChainKey != null) {
            DynamicChainInfo info = sDynamicChains.get(sCurrentChainKey.toLowerCase());
            if (info != null && info.symbol != null && !info.symbol.isEmpty()) {
                return info.symbol;
            }
        }
        if (isSolana()) return "SOL";
        if (isTron()) return "TRX";
        if (isSui()) return "SUI";
        if (isAptos()) return "APT";
        if (isIota()) return "IOTA";
        return sCurrentChainKey != null ? sCurrentChainKey.toUpperCase() : "ETH";
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

    public static String sLastEvmChainKey = "bsc";
    public static int sLastEvmChainId = 56;

    public static boolean isNonEvm() {
        return isNonEvmKey(sCurrentChainKey);
    }

    public static boolean isNonEvmKey(String key) {
        if (key == null) return false;
        String k = key.trim().toLowerCase();
        DynamicChainInfo info = sDynamicChains.get(k);
        if (info != null) {
            return info.isNonEvm;
        }
        return "solana".equals(k) || "sol".equals(k) || "tron".equals(k) || "trx".equals(k) ||
               "sui".equals(k) || "aptos".equals(k) || "apt".equals(k) || "iota".equals(k) ||
               "substrate".equals(k) || "flop".equals(k) || "polkadot".equals(k) || "dot".equals(k);
    }

    public static boolean isNonEvmUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lower = url.toLowerCase();
        for (DynamicChainInfo info : sDynamicChains.values()) {
            if (info != null && info.isNonEvm && info.urlPatterns != null) {
                for (String p : info.urlPatterns) {
                    if (p != null && !p.isEmpty() && lower.contains(p.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        if (lower.contains("flop.network") || lower.contains("polkadot.js.org") || lower.contains("subscan.io") ||
            lower.contains("talisman.xyz") || lower.contains("subwallet.app") || lower.contains("zkverify.io") ||
            lower.contains("kusama") || lower.contains("polkadot") || lower.contains("substrate")) {
            return true;
        }
        if (lower.contains("jup.ag") || lower.contains("raydium") || lower.contains("pump.fun") ||
            lower.contains("solana") || lower.contains("magiceden") || lower.contains("tensor.trade") ||
            lower.contains("orca.so") || lower.contains("birdeye.so") || lower.contains("phantom.app") ||
            lower.contains("solscan") || lower.contains("solflare")) {
            return true;
        }
        if (lower.contains("sui.io") || lower.contains("suiscan") || lower.contains("cetus.zone") ||
            lower.contains("turbos") || lower.contains("navi.protocol") || lower.contains("scallop") ||
            lower.contains("aftermath") || lower.contains("bluefin") || lower.contains("sui")) {
            return true;
        }
        if (lower.contains("aptoslabs") || lower.contains("aptoscan") || lower.contains("panora") ||
            lower.contains("thalas") || lower.contains("liquidswap") || lower.contains("amnis") ||
            lower.contains("aptos")) {
            return true;
        }
        if (lower.contains("tronscan") || lower.contains("sunpump") || lower.contains("sun.io") ||
            lower.contains("justlend") || lower.contains("trondao") || lower.contains("tron")) {
            return true;
        }
        if (lower.contains("iota.org") || lower.contains("iota.cafe") || lower.contains("magicsea") ||
            lower.contains("iotabee") || lower.contains("tangle") || lower.contains("iota")) {
            return true;
        }
        return false;
    }

    public static int getChainIdByKey(String key) {
        if (key == null || key.isEmpty()) return 56;
        String[] keys = sChainKeys;
        int[] ids = sChainIds;
        if (keys != null && ids != null) {
            for (int i = 0; i < keys.length; i++) {
                if (key.equalsIgnoreCase(keys[i])) {
                    return ids[i];
                }
            }
        }
        if ("solana".equalsIgnoreCase(key) || "sol".equalsIgnoreCase(key)) return 501;
        if ("sui".equalsIgnoreCase(key)) return 784;
        if ("tron".equalsIgnoreCase(key) || "trx".equalsIgnoreCase(key)) return 728126428;
        if ("aptos".equalsIgnoreCase(key) || "apt".equalsIgnoreCase(key)) return 637;
        if ("iota".equalsIgnoreCase(key)) return 4218;
        if ("iotaevm".equalsIgnoreCase(key)) return 8822;
        if ("bsc".equalsIgnoreCase(key)) return 56;
        if ("base".equalsIgnoreCase(key)) return 8453;
        if ("eth".equalsIgnoreCase(key)) return 1;
        if ("arbitrum".equalsIgnoreCase(key) || "arb".equalsIgnoreCase(key)) return 42161;
        if ("polygon".equalsIgnoreCase(key) || "pol".equalsIgnoreCase(key) || "matic".equalsIgnoreCase(key)) return 137;
        if ("morph".equalsIgnoreCase(key)) return 2818;
        if ("arc-mainnet".equalsIgnoreCase(key) || "arc".equalsIgnoreCase(key)) return 5042;
        return 56;
    }

    public static String getChainKeyById(int id) {
        int[] ids = sChainIds;
        String[] keys = sChainKeys;
        if (ids != null && keys != null) {
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] == id) {
                    return keys[i];
                }
            }
        }
        switch (id) {
            case 8453: return "base";
            case 56: return "bsc";
            case 42161: return "arbitrum";
            case 137: return "polygon";
            case 2818: return "morph";
            case 5042: return "arc-mainnet";
            case 1: return "eth";
            case 501: return "solana";
            case 784: return "sui";
            case 101:
            case 637: return "aptos";
            case 4218: return "iota";
            case 8822: return "iotaevm";
            case 728126428: return "tron";
            default: return "bsc";
        }
    }

    public static String detectChainFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String lower = url.toLowerCase();
        for (DynamicChainInfo info : sDynamicChains.values()) {
            if (info != null && info.urlPatterns != null) {
                for (String p : info.urlPatterns) {
                    if (p != null && !p.isEmpty() && lower.contains(p.toLowerCase())) {
                        return info.key;
                    }
                }
            }
        }
        if (lower.contains("flop.network") || lower.contains("zkverify.io")) {
            return "substrate";
        }
        if (lower.contains("polkadot.js.org") || lower.contains("subscan.io") || lower.contains("polkadot") || lower.contains("kusama")) {
            return "polkadot";
        }
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
                    String cleanKey = key.trim().toLowerCase();
                    keys.add(cleanKey);
                    names.add(name != null ? name.trim() : cleanKey);
                    ids.add(chainId);
                    if (!sDynamicChains.containsKey(cleanKey)) {
                        boolean isNonEvm = isNonEvmKey(cleanKey) || chainId == 0 || chainId == 501 || chainId == 784 || chainId == 728126428 || chainId == 42;
                        registerDynamicChain(cleanKey, name, cleanKey.toUpperCase(), chainId, isNonEvm, null);
                    }
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
        String k = str.trim().toLowerCase();
        if ("pol".equalsIgnoreCase(k) || "matic".equalsIgnoreCase(k)) {
            str = "polygon";
        } else if ("arb".equalsIgnoreCase(k)) {
            str = "arbitrum";
        } else if ("arc".equalsIgnoreCase(k)) {
            str = "arc-mainnet";
        } else if ("sol".equalsIgnoreCase(k)) {
            str = "solana";
        } else if ("trx".equalsIgnoreCase(k)) {
            str = "tron";
        } else if ("sui".equalsIgnoreCase(k)) {
            str = "sui";
        } else if ("apt".equalsIgnoreCase(k) || "aptos".equalsIgnoreCase(k)) {
            str = "aptos";
        } else if ("iota".equalsIgnoreCase(k)) {
            str = "iota";
        } else if ("iotaevm".equalsIgnoreCase(k)) {
            str = "iotaevm";
        } else if ("flop".equalsIgnoreCase(k)) {
            str = "substrate";
        } else if ("dot".equalsIgnoreCase(k)) {
            str = "polkadot";
        }

        DynamicChainInfo dInfo = sDynamicChains.get(str.toLowerCase());
        if (dInfo != null) {
            sCurrentChainKey = dInfo.key;
            sCurrentChainName = dInfo.name;
            sCurrentChainId = dInfo.chainId;
            if (!dInfo.isNonEvm) {
                sLastEvmChainKey = dInfo.key;
                sLastEvmChainId = dInfo.chainId;
            }
            return;
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
        for (DynamicChainInfo info : sDynamicChains.values()) {
            if (info != null && info.chainId == i) {
                sCurrentChainId = info.chainId;
                sCurrentChainKey = info.key;
                sCurrentChainName = info.name;
                if (!info.isNonEvm) {
                    sLastEvmChainKey = info.key;
                    sLastEvmChainId = info.chainId;
                }
                return;
            }
        }
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
                sLastEvmChainKey = "base";
                sLastEvmChainId = 8453;
                break;
            case 56:
                sCurrentChainKey = "bsc";
                sCurrentChainName = "Binance Smart Chain";
                sLastEvmChainKey = "bsc";
                sLastEvmChainId = 56;
                break;
            case 42161:
                sCurrentChainKey = "arbitrum";
                sCurrentChainName = "Arbitrum One";
                sLastEvmChainKey = "arbitrum";
                sLastEvmChainId = 42161;
                break;
            case 137:
                sCurrentChainKey = "polygon";
                sCurrentChainName = "Polygon (Matic)";
                sLastEvmChainKey = "polygon";
                sLastEvmChainId = 137;
                break;
            case 2818:
                sCurrentChainKey = "morph";
                sCurrentChainName = "Morph L2";
                sLastEvmChainKey = "morph";
                sLastEvmChainId = 2818;
                break;
            case 5042:
                sCurrentChainKey = "arc-mainnet";
                sCurrentChainName = "Arc";
                sLastEvmChainKey = "arc-mainnet";
                sLastEvmChainId = 5042;
                break;
            case 1:
                sCurrentChainKey = "eth";
                sCurrentChainName = "Ethereum Mainnet";
                sLastEvmChainKey = "eth";
                sLastEvmChainId = 1;
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
                sCurrentChainKey = "iota";
                sCurrentChainName = "IOTA Rebased Mainnet";
                break;
            case 8822:
                sCurrentChainKey = "iotaevm";
                sCurrentChainName = "IOTA EVM Mainnet";
                sLastEvmChainKey = "iotaevm";
                sLastEvmChainId = 8822;
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
        sLastEvmChainKey = sCurrentChainKey;
        sLastEvmChainId = sCurrentChainId;
    }
}
