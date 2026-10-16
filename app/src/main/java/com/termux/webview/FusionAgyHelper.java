package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionAgyHelper {
    public static TextView createAgyOption(Context context, AlertDialog alertDialog, String str, int i) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(2, 13.0f);
        textView.setBackground(FusionUiHelper.createRounded(-14737370, -11180682, 10.0f));
        textView.setPadding(16, 14, 16, 14);
        textView.setClickable(true);
        textView.setOnClickListener(new FusionAgyActionClick(FusionWebViewManager.getInstance(), i, alertDialog));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 6, 0, 6);
        textView.setLayoutParams(layoutParams);
        return textView;
    }
}
