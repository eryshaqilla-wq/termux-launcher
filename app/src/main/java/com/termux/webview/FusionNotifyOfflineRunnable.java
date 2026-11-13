package com.termux.webview;

import android.content.Context;
import com.termux.app.FusionToast;

public class FusionNotifyOfflineRunnable implements Runnable {
    public final Context mContext;
    public final String mMessage;

    public FusionNotifyOfflineRunnable(Context context) {
        this(context, "⚡ [GhostWallet] Transaksi butuh daemon aktif! Jalankan 'wallet' di terminal.");
    }

    public FusionNotifyOfflineRunnable(Context context, String message) {
        this.mContext = context;
        this.mMessage = message;
    }

    @Override
    public void run() {
        Context context = this.mContext;
        if (context != null) {
            String msg = (this.mMessage != null && !this.mMessage.isEmpty())
                ? this.mMessage
                : "⚡ [GhostWallet] Transaksi butuh daemon aktif! Jalankan 'wallet' di terminal.";
            FusionToast.makeText(context, msg, 1).show();
        }
    }
}
