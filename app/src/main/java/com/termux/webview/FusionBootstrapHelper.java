package com.termux.webview;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FusionBootstrapHelper {
    public static void copyAssetToFile(Context context, String str, String str2, boolean z) {
        copyAssetToFile(context, str, str2, z, false);
    }

    public static void copyAssetToFile(Context context, String str, String str2, boolean z, boolean forceOverwrite) {
        try {
            File file = new File(str2);
            if (forceOverwrite || !file.exists() || file.length() <= 0) {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                InputStream inputStreamOpen = context.getAssets().open(str);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i <= 0) {
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
            } else if (z && !file.canExecute()) {
                file.setExecutable(true, false);
                file.setReadable(true, false);
            }
        } catch (Exception unused) {
        }
    }

    public static void extractSetupIfNeeded(Context context) {
        if (context == null) {
            return;
        }
        new Thread(new FusionBootstrapRunnable(context)).start();
    }

    public static void writeStringToFile(String str, String str2, boolean z) {
        try {
            File file = new File(str2);
            if (file.exists()) {
                return;
            }
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(str.getBytes("UTF-8"));
            fileOutputStream.flush();
            fileOutputStream.close();
            if (z) {
                file.setExecutable(true, false);
                file.setReadable(true, false);
            }
        } catch (Exception unused) {
        }
    }
}
