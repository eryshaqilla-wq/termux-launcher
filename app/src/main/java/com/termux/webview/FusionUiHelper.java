package com.termux.webview;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
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

    public static TextView createQuickActionBtn(Context context, String icon, View.OnClickListener listener) {
        TextView btn = new TextView(context);
        btn.setText(icon);
        btn.setGravity(Gravity.CENTER);
        btn.setTextSize(2, 16.0f);
        btn.setTextColor(0xFFCDD6F4);
        btn.setBackground(createRounded(0xFF1E1E2E, 0xFF313244, 8.0f));
        btn.setPadding(8, 10, 8, 10);
        btn.setClickable(true);
        btn.setFocusable(true);
        if (listener != null) {
            btn.setOnClickListener(listener);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        lp.setMargins(3, 0, 3, 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    public static LinearLayout createMenuRow(Context context, String icon, String title, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 11, 12, 11);
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(createRounded(0x00000000, 0, 6.0f));
        if (listener != null) {
            row.setOnClickListener(listener);
        }

        TextView iconView = new TextView(context);
        iconView.setText(icon);
        iconView.setTextSize(2, 15.0f);
        iconView.setTextColor(0xFF89B4FA);
        iconView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams((int) (28 * context.getResources().getDisplayMetrics().density), -2);
        row.addView(iconView, lpIcon);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(0xFFCDD6F4);
        titleView.setTextSize(2, 13.5f);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(titleView, new LinearLayout.LayoutParams(0, -2, 1.0f));

        return row;
    }

    public static LinearLayout createMenuRowWithCustomText(Context context, String icon, TextView customTextView, View rightWidget) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 11, 12, 11);
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackground(createRounded(0x00000000, 0, 6.0f));

        TextView iconView = new TextView(context);
        iconView.setText(icon);
        iconView.setTextSize(2, 15.0f);
        iconView.setTextColor(0xFF89B4FA);
        iconView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams((int) (28 * context.getResources().getDisplayMetrics().density), -2);
        row.addView(iconView, lpIcon);

        if (customTextView != null) {
            row.addView(customTextView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }

        if (rightWidget != null) {
            row.addView(rightWidget, new LinearLayout.LayoutParams(-2, -2));
        }

        return row;
    }

    public static View createSectionDivider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(0xFF313244);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (1 * context.getResources().getDisplayMetrics().density)
        );
        int margin = (int) (6 * context.getResources().getDisplayMetrics().density);
        lp.setMargins(0, margin, 0, margin);
        divider.setLayoutParams(lp);
        return divider;
    }

    public static TextView createSectionHeader(Context context, String title) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextColor(0xFFA6ADC8);
        header.setTextSize(2, 10.5f);
        header.setTypeface(null, 1);
        header.setPadding(12, 4, 12, 4);
        return header;
    }
}
