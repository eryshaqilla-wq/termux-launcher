package com.termux.webview;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabKillRunnable implements Runnable {
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionTabKillRunnable(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.killTabDirect(this.mIndex);
        }
    }
}
