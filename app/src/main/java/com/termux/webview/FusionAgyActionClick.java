package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyActionClick implements View.OnClickListener {
    public int mActionId;
    public FusionWebViewManager mManager;

    public FusionAgyActionClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mActionId = i;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.executeAgyAction(this.mActionId);
            FusionDrawerHelper.closeNativeDrawersOnly(fusionWebViewManager);
        }
    }
}
