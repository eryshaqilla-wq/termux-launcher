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
        return new File("/data/data/com.termux/files/home/wallet/wallets_db.json").exists() ? "/data/data/com.termux/files/home/wallet" : "/root/wallet";
    }

    public static void init(Context context) {
        reload(context);
    }

    public static void reload(Context context) {
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
            try {
                File file2 = new File("/data/data/com.termux/files/home/wallet/wallets_db.json");
                if (file2.exists() && file2.canRead()) {
                    fileInputStream = new FileInputStream(file2);
                }
            } catch (Exception unused2) {
            }
            if (fileInputStream == null) {
                try {
                    File file3 = new File("/data/local/nhsystem/kali-arm64/root/wallet/wallets_db.json");
                    if (file3.exists()) {
                        fileInputStream = new FileInputStream(file3);
                    }
                } catch (Exception unused3) {
                }
                if (fileInputStream == null) {
                    try {
                        File file4 = new File("/root/wallet/wallets_db.json");
                        if (file4.exists()) {
                            fileInputStream = new FileInputStream(file4);
                        }
                    } catch (Exception unused4) {
                    }
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

    public static void setCustomWalletPath(String str) {
        sCustomWalletPath = str;
    }
}
