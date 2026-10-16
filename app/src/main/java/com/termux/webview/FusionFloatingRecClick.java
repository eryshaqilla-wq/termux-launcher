package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionFloatingRecClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionFloatingRecClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            if (!fusionWebViewManager.mIsRecording) {
                FusionDrawerHelper.showRecordModeDrawer(fusionWebViewManager);
            } else {
                fusionWebViewManager.toggleMacroRecording();
                FusionRecPillHelper.updateRecUi(fusionWebViewManager);
            }
        }
    }
}
