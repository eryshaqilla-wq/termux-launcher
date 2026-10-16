package com.termux.webview;

import android.view.View;
import java.util.ArrayList;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabCloseCurrentClick implements View.OnClickListener {
    public FusionWebViewManager mManager;

    public FusionTabCloseCurrentClick(FusionWebViewManager fusionWebViewManager) {
        this.mManager = fusionWebViewManager;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.killActiveTab();
            ArrayList arrayList = fusionWebViewManager.mTabList;
            if (arrayList == null || arrayList.size() <= 0) {
                fusionWebViewManager.closeAllDrawers();
            } else {
                FusionDrawerHelper.showTabDrawer(fusionWebViewManager);
            }
        }
    }
}
