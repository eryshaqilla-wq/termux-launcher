package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionChainItemClick implements View.OnClickListener {
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionChainItemClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.onChainSelected(this.mIndex);
            fusionWebViewManager.closeAllDrawers();
        }
    }
}
