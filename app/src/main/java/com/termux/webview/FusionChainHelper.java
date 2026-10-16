package com.termux.webview;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
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

    public static void init(Context context) {
        reload(context);
    }

    public static void reload(Context context) {
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
        sCurrentChainKey = "chain-" + i;
        sCurrentChainName = "Chain " + i;
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
