package com.termux.webview;

import android.content.DialogInterface;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabCancelDialogListener implements DialogInterface.OnClickListener {
    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
    }
}
