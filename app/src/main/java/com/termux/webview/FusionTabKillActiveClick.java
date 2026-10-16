package com.termux.webview;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabKillActiveClick implements View.OnClickListener {
    public AlertDialog mDialog;
    public FusionWebViewManager mManager;

    public FusionTabKillActiveClick(FusionWebViewManager fusionWebViewManager, AlertDialog alertDialog) {
        this.mManager = fusionWebViewManager;
        this.mDialog = alertDialog;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.killActiveTab();
        }
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
