package com.termux.webview;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionInstaller {
    public static void activateIfReady(Context context) {
        if (context == null) {
            return;
        }
        try {
            if (new File("/data/data/com.termux/files/usr/bin/bash").exists()) {
                File file = new File("/data/data/com.termux/files/usr/bin/login.termux_orig");
                if (!file.exists()) {
                    File file2 = new File("/data/data/com.termux/files/usr/bin/login");
                    if (file2.exists()) {
                        file2.renameTo(file);
                    }
                }
                AssetManager assets = context.getAssets();
                copyAssetFile(assets, "fusion/login", "/data/data/com.termux/files/usr/bin/login", true, false);
                copyAssetFile(assets, "fusion/bashnh", "/data/data/com.termux/files/usr/bin/bashnh", true, true);
                copyAssetFile(assets, "fusion/termux-ipc-send", "/data/data/com.termux/files/usr/bin/termux-ipc-send", true, true);
                copyAssetFile(assets, "fusion/deploy_fusion.sh", "/data/data/com.termux/files/usr/share/fusion/deploy_fusion.sh", true, true);
                copyAssetFile(assets, "fusion/fusion_core.tar.gz", "/data/data/com.termux/files/usr/share/fusion/fusion_core.tar.gz", false, false);
            }
        } catch (Throwable unused) {
        }
    }

    public static void copyAssetFile(AssetManager assetManager, String str, String str2, boolean z) {
        copyAssetFile(assetManager, str, str2, z, false);
    }

    public static void copyAssetFile(AssetManager assetManager, String str, String str2, boolean z, boolean overwrite) {
        try {
            File file = new File(str2);
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (file.exists() && file.length() > 0 && !overwrite) {
                if (z) {
                    file.setExecutable(true, false);
                    return;
                }
                return;
            }
            InputStream inputStreamOpen = assetManager.open(str);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[4096];
            while (true) {
                int i = inputStreamOpen.read(bArr);
                if (i == -1) {
                    break;
                } else {
                    fileOutputStream.write(bArr, 0, i);
                }
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStreamOpen.close();
            if (z) {
                file.setExecutable(true, false);
                file.setReadable(true, false);
            }
        } catch (Throwable unused) {
        }
    }

    public static void install(Context context) {
        activateIfReady(context);
    }
}
