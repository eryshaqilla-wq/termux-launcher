package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWalletPathBtnClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionWalletPathBtnClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            FusionDrawerHelper.showWalletPathDialog(fusionWebViewManager);
        }
    }
}
