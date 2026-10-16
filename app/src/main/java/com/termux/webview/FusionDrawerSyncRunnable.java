package com.termux.webview;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionDrawerSyncRunnable implements Runnable {
    public boolean mIsOpen;
    public FusionWebViewManager mManager;

    public FusionDrawerSyncRunnable(FusionWebViewManager fusionWebViewManager, boolean z) {
        this.mManager = fusionWebViewManager;
        this.mIsOpen = z;
    }

    @Override // java.lang.Runnable
    public void run() {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager == null) {
            return;
        }
        if (this.mIsOpen) {
            FusionTabStateHelper.setControlPillCollapsed(fusionWebViewManager, true);
            FusionRecPillHelper.hideRecPill();
        } else {
            FusionTabStateHelper.setControlPillCollapsed(fusionWebViewManager, false);
            FusionRecPillHelper.showRecPill(fusionWebViewManager);
        }
    }
}
