package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabSelectItemClick implements View.OnClickListener {
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionTabSelectItemClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.switchTab(this.mIndex);
            fusionWebViewManager.closeAllDrawers();
        }
    }
}
