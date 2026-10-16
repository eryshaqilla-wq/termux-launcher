package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecSaveClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionRecSaveClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.evalOnActiveTab("if(window.AgyRecorder && window.AgyRecorder.stopAndPromptSave){window.AgyRecorder.stopAndPromptSave();}else if(window.AgyBridge && window.AgyBridge.saveCurrentRecipeAs){window.AgyBridge.saveCurrentRecipeAs();}else{window.__FUSION_STOP_REC__&&window.__FUSION_STOP_REC__();}");
            fusionWebViewManager.mIsRecording = false;
            FusionRecPillHelper.updateRecUi(fusionWebViewManager);
        }
    }
}
