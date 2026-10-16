package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWalletItemClick implements View.OnClickListener {
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionWalletItemClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.onWalletSelected(this.mIndex);
            fusionWebViewManager.closeAllDrawers();
        }
    }
}
