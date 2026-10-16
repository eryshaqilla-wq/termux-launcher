package com.termux.webview;

import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyManageRecipeClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionAgyManageRecipeClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.closeAllDrawers();
            fusionWebViewManager.evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showRecipePicker){window.AgyBridge.showRecipePicker();}else if(typeof showRecipePickerModal==='function'){showRecipePickerModal();}");
        }
    }
}
