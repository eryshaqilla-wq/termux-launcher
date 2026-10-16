package com.termux.webview;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyActionClick implements View.OnClickListener {
    public int mActionId;
    public FusionWebViewManager mManager;
    public AlertDialog mDialog;

    public FusionAgyActionClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mActionId = i;
    }

    public FusionAgyActionClick(FusionWebViewManager fusionWebViewManager, int i, AlertDialog alertDialog) {
        this.mManager = fusionWebViewManager;
        this.mActionId = i;
        this.mDialog = alertDialog;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.executeAgyAction(this.mActionId);
            FusionDrawerHelper.closeNativeDrawersOnly(fusionWebViewManager);
        }
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
