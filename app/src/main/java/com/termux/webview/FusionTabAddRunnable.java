package com.termux.webview;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabAddRunnable implements Runnable {
    public FusionWebViewManager mManager;
    public String mUrl;

    public FusionTabAddRunnable(FusionWebViewManager fusionWebViewManager, String str) {
        this.mManager = fusionWebViewManager;
        this.mUrl = str;
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.addNewTabDirect(this.mUrl);
        }
    }
}
