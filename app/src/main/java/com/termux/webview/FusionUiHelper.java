package com.termux.webview;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionUiHelper {
    public static GradientDrawable createRounded(int i, int i2, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(f);
        if (i2 != 0) {
            gradientDrawable.setStroke(2, i2);
        }
        return gradientDrawable;
    }

    public static int getDrawerWidth(Context context) {
        if (context == null) {
            return 880;
        }
        android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int widthPixels = dm.widthPixels;
        int width82 = (widthPixels * 82) / 100;
        int maxDpPixels = (int) (400 * dm.density);
        return Math.min(width82, maxDpPixels);
    }

    public static int getScreenWidth70(Context context) {
        if (context == null) {
            return 720;
        }
        return (context.getResources().getDisplayMetrics().widthPixels * 7) / 10;
    }

    public static void setDrawerCloseBtn(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.setBackground(createRounded(-13548988, -12234918, 10.0f)); // #313244, border #45475a, 10dp radius
        textView.setTextColor(-328965); // #cdd6f4
        textView.setTypeface(null, 1);
    }

    public static void setSolidAccentBtn(TextView textView, int i, int i2) {
        if (textView == null) {
            return;
        }
        textView.setBackground(createRounded(i2, i, 3.0f));
        textView.setTextColor(i);
    }

    public static void setSolidBox(View view) {
        if (view == null) {
            return;
        }
        view.setBackground(createRounded(-14802907, -12762806, 3.0f));
    }

    public static void setSolidBtn(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.setBackground(createRounded(-13947597, -12762806, 3.0f));
        textView.setTextColor(-1907994);
    }
}
