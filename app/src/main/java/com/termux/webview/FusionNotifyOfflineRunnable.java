package com.termux.webview;

import android.content.Context;
import android.widget.Toast;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionNotifyOfflineRunnable implements Runnable {
    public final Context mContext;

    public FusionNotifyOfflineRunnable(Context context) {
        this.mContext = context;
    }

    @Override // java.lang.Runnable
    public void run() {
        Context context = this.mContext;
        if (context != null) {
            Toast.makeText(context, "[RPC Daemon] Daemon GhostWallet belum aktif! Start daemon terlebih dahulu.", 1).show();
        }
    }
}
