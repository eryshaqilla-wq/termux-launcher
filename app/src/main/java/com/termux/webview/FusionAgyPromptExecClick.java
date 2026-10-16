package com.termux.webview;

import android.view.View;
import android.widget.EditText;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyPromptExecClick implements View.OnClickListener {
    public EditText mEditText;
    public FusionWebViewManager mManager;

    public FusionAgyPromptExecClick(FusionWebViewManager fusionWebViewManager, EditText editText) {
        this.mManager = fusionWebViewManager;
        this.mEditText = editText;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        EditText editText;
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager == null || (editText = this.mEditText) == null) {
            return;
        }
        fusionWebViewManager.executeAgyPrompt(editText.getText().toString());
        fusionWebViewManager.closeAllDrawers();
    }
}
