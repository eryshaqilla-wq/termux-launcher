package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecCollapseClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionRecCollapseClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionRecPillHelper.toggleCollapse(this.mManager);
    }
}
