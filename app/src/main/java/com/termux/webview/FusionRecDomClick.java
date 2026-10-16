package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecDomClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionRecDomClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            FusionTabStateHelper.setControlPillCollapsed(fusionWebViewManager, true);
            FusionRecPillHelper.hideRecPill();
            fusionWebViewManager.evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showDomInspector){window.AgyBridge.showDomInspector();}else if(window.showDomInspectorModal){window.showDomInspectorModal();}else{window.__AI_DEVBRIDGE_INSPECT__&&window.__AI_DEVBRIDGE_INSPECT__();}");
        }
    }
}
