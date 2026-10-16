package com.termux.webview;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecUpdateRunnable implements Runnable {
    public FusionWebViewManager mManager;

    public FusionRecUpdateRunnable(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            FusionRecPillHelper.updateRecUi(fusionWebViewManager);
        }
    }
}
