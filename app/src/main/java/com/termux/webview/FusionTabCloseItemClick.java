package com.termux.webview;

import android.view.View;
import java.util.ArrayList;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabCloseItemClick implements View.OnClickListener {
    public int mIndex;
    public FusionWebViewManager mManager;

    public FusionTabCloseItemClick(FusionWebViewManager fusionWebViewManager, int i) {
        this.mManager = fusionWebViewManager;
        this.mIndex = i;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.killTabDirect(this.mIndex);
            ArrayList arrayList = fusionWebViewManager.mTabList;
            if (arrayList == null || arrayList.size() <= 0) {
                fusionWebViewManager.closeAllDrawers();
            } else {
                FusionDrawerHelper.showTabDrawer(fusionWebViewManager);
            }
        }
    }
}
