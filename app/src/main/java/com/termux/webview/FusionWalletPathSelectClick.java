package com.termux.webview;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWalletPathSelectClick implements DialogInterface.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionWalletPathSelectClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager == null) {
            return;
        }
        String str = "/root/wallet/wallets_db.json";
        if (i != 0) {
            str = i == 1 ? "/data/data/com.termux/files/home/wallet/wallets_db.json" : "/sdcard/Download/wallets_db.json";
        }
        FusionWalletHelper.setCustomWalletPath(str);
        FusionWalletHelper.reload(fusionWebViewManager.mActivity);
        fusionWebViewManager.updateMenuTexts();
        Toast.makeText((Context) fusionWebViewManager.mActivity, (CharSequence) ("DB Dompet Dimuat: " + str), 1).show();
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
    }
}
