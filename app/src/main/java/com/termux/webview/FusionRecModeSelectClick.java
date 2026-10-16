package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecModeSelectClick implements View.OnClickListener {
    public FusionWebViewManager mManager;
    public String mMode;

    public FusionRecModeSelectClick(FusionWebViewManager fusionWebViewManager, String str) {
        this.mManager = fusionWebViewManager;
        this.mMode = str;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager == null) {
            return;
        }
        String str = this.mMode;
        if (str == null) {
            str = "dom";
        }
        fusionWebViewManager.closeAllDrawers();
        fusionWebViewManager.mIsRecording = true;
        FusionRecPillHelper.setStepCount(0);
        fusionWebViewManager.evalOnActiveTab("if(window.AgyRecorder){AgyRecorder.start('Resep_Termux','" + str + "');}else{window.__FUSION_START_REC__&&window.__FUSION_START_REC__();}");
        FusionRecPillHelper.updateRecUi(fusionWebViewManager);
    }
}
