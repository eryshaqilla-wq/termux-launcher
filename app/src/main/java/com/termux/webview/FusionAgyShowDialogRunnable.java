package com.termux.webview;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyShowDialogRunnable implements Runnable {
    public FusionWebViewManager mManager;

    public FusionAgyShowDialogRunnable(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.showAgyDialogDirect();
        }
    }
}
