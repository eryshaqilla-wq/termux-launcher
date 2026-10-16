package com.termux.webview;

import android.content.DialogInterface;
import android.text.Editable;
import android.widget.EditText;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabOpenUrlListener implements DialogInterface.OnClickListener {
    public EditText mInput;
    public FusionWebViewManager mManager;

    public FusionTabOpenUrlListener(FusionWebViewManager fusionWebViewManager, EditText editText) {
        this.mManager = fusionWebViewManager;
        this.mInput = editText;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        Editable text;
        EditText editText = this.mInput;
        String str = "https://google.com";
        if (editText != null && (text = editText.getText()) != null) {
            String strTrim = text.toString().trim();
            if (strTrim.length() > 0) {
                if (strTrim.startsWith("http://") || strTrim.startsWith("https://") || strTrim.startsWith("about:") || strTrim.startsWith("file://")) {
                    str = strTrim;
                } else {
                    str = "https://" + strTrim;
                }
            }
        }
        FusionWebViewManager fusionWebViewManager = this.mManager;
        if (fusionWebViewManager != null) {
            fusionWebViewManager.addNewTab(str);
        }
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
    }
}
