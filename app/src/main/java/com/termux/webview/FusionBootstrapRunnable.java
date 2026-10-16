package com.termux.webview;

import android.content.Context;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionBootstrapRunnable implements Runnable {
    public Context mContext;

    public FusionBootstrapRunnable(Context context) {
        this.mContext = context;
    }

    @Override // java.lang.Runnable
    public void run() {
        Context context = this.mContext;
        if (context == null) {
            return;
        }
        FusionBootstrapHelper.copyAssetToFile(context, "fusion/setup.sh", "/data/data/com.termux/files/usr/bin/setup", true);
        FusionBootstrapHelper.copyAssetToFile(context, "fusion/setup.sh", "/data/data/com.termux/files/home/setup", true);
        FusionBootstrapHelper.copyAssetToFile(context, "fusion/fusion_payload.tar.gz", "/data/data/com.termux/files/home/fusion_payload.tar.gz", false);
        FusionBootstrapHelper.writeStringToFile("#!/bin/bash\nexec termux-ipc webview \"$@\"\n", "/data/data/com.termux/files/usr/bin/browser", true);
    }
}
