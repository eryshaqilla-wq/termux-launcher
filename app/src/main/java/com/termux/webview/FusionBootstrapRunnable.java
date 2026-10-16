package com.termux.webview;

import android.content.Context;

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
        try {
            // Salin binary langsung ke $PREFIX/bin/ (executable 755)
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/termux-ipc", "/data/data/com.termux/files/usr/bin/termux-ipc", true, true);
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/browser", "/data/data/com.termux/files/usr/bin/browser", true, true);
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/x", "/data/data/com.termux/files/usr/bin/x", true, true);
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/setup.sh", "/data/data/com.termux/files/usr/bin/setup", true, true);
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/setup.sh", "/data/data/com.termux/files/home/setup", true, true);

            // Salin x_webview.js ke wallet
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/x_webview.js", "/data/data/com.termux/files/home/wallet/x_webview.js", true, true);

            // Salin payload tarball dan termux.properties
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/fusion_payload.tar.gz", "/data/data/com.termux/files/home/fusion_payload.tar.gz", false, false);
            FusionBootstrapHelper.copyAssetToFile(context, "fusion/termux.properties", "/data/data/com.termux/files/home/.termux/termux.properties", false, false);
        } catch (Exception unused) {
        }
    }
}
