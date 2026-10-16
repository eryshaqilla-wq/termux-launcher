package com.termux.webview;

import android.app.AlertDialog;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyPromptSendClick implements View.OnClickListener {
    public AlertDialog mDialog;
    public EditText mInput;
    public FusionWebViewManager mManager;

    public FusionAgyPromptSendClick(FusionWebViewManager fusionWebViewManager, EditText editText, AlertDialog alertDialog) {
        this.mManager = fusionWebViewManager;
        this.mInput = editText;
        this.mDialog = alertDialog;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Editable text;
        FusionWebViewManager fusionWebViewManager;
        EditText editText = this.mInput;
        if (editText != null && (text = editText.getText()) != null) {
            String strTrim = text.toString().trim();
            if (strTrim.length() > 0 && (fusionWebViewManager = this.mManager) != null) {
                fusionWebViewManager.executeAgyPrompt(strTrim);
            }
        }
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
