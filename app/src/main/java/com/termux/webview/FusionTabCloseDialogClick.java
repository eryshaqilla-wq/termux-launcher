package com.termux.webview;

import android.app.AlertDialog;
import android.view.View;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabCloseDialogClick implements View.OnClickListener {
    public AlertDialog mDialog;

    public FusionTabCloseDialogClick(AlertDialog alertDialog) {
        this.mDialog = alertDialog;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
