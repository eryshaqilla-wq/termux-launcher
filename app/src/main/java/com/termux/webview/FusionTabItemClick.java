package com.termux.webview;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabItemClick implements View.OnClickListener {
    public AlertDialog mDialog;
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionTabItemClick(FusionWebViewManager fusionWebViewManager, int i, AlertDialog alertDialog) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
        this.mDialog = alertDialog;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.switchTab(this.mIndex);
        }
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
