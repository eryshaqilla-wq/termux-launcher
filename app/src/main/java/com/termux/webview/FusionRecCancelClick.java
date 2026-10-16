package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecCancelClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionRecCancelClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.evalOnActiveTab("if(window.AgyRecorder&&window.AgyRecorder.cancel){window.AgyRecorder.cancel();}else{try{window.__FUSION_RECORDING__=false;localStorage.removeItem('__fusion_macro_events__');}catch(_){}}var h=document.getElementById('__agy_copilot_hud__');if(h)h.remove();");
            fusionWebViewManager.mIsRecording = false;
            FusionRecPillHelper.setStepCount(0);
            FusionRecPillHelper.updateRecUi(fusionWebViewManager);
        }
    }
}
