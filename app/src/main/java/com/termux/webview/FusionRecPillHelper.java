package com.termux.webview;

import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionRecPillHelper {
    public static TextView sCancelBtn;
    public static TextView sCollapseBtn;
    public static TextView sDomBtn;
    public static boolean sIsCollapsed;
    public static TextView sRecBtn;
    public static LinearLayout sRecPill;
    public static TextView sSaveBtn;
    public static int sStepCount;

    public static void hideRecPill() {
        LinearLayout linearLayout = sRecPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
    }

    public static void hideRecPill(FusionWebViewManager fusionWebViewManager) {
        hideRecPill();
    }

    public static void initAndShowRecPill(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        FrameLayout frameLayout;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null || (frameLayout = fusionWebViewManager.mPaneHost) == null) {
            return;
        }
        LinearLayout linearLayout = sRecPill;
        if (linearLayout != null) {
            ViewParent parent = linearLayout.getParent();
            if (parent != frameLayout) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(linearLayout);
                }
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                if (layoutParams != null) {
                    frameLayout.addView(linearLayout, layoutParams);
                } else {
                    FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 8388691);
                    layoutParams2.setMargins(10, 0, 0, 10);
                    frameLayout.addView(linearLayout, layoutParams2);
                }
            }
            linearLayout.setVisibility(0);
            linearLayout.setElevation(160.0f);
            linearLayout.bringToFront();
            updateRecUi(fusionWebViewManager);
            return;
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sRecPill = linearLayout2;
        linearLayout2.setOrientation(0);
        FusionUiHelper.setSolidBox(linearLayout2);
        linearLayout2.setPadding(4, 4, 4, 4);
        TextView textView = new TextView(appCompatActivity);
        sCollapseBtn = textView;
        textView.setText(sIsCollapsed ? " > " : " < ");
        FusionUiHelper.setSolidBtn(textView);
        textView.setTextSize(2, 11.0f);
        textView.setPadding(8, 4, 8, 4);
        textView.setClickable(true);
        textView.setOnClickListener(new FusionRecCollapseClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.setMargins(2, 0, 2, 0);
        linearLayout2.addView(textView, layoutParams3);
        TextView textView2 = new TextView(appCompatActivity);
        sRecBtn = textView2;
        FusionUiHelper.setSolidBtn(textView2);
        textView2.setTextSize(2, 11.0f);
        textView2.setPadding(8, 4, 8, 4);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionFloatingRecClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams4.setMargins(2, 0, 2, 0);
        linearLayout2.addView(textView2, layoutParams4);
        TextView textView3 = new TextView(appCompatActivity);
        sDomBtn = textView3;
        textView3.setText(" DOM ");
        FusionUiHelper.setSolidAccentBtn(textView3, -8596290, -13947597);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(8, 4, 8, 4);
        textView3.setClickable(true);
        textView3.setOnClickListener(new FusionRecDomClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams5.setMargins(2, 0, 2, 0);
        linearLayout2.addView(textView3, layoutParams5);
        TextView textView4 = new TextView(appCompatActivity);
        sSaveBtn = textView4;
        textView4.setText(" Simpan ");
        FusionUiHelper.setSolidAccentBtn(textView4, -7686920, -13947597);
        textView4.setTextSize(2, 11.0f);
        textView4.setPadding(8, 4, 8, 4);
        textView4.setClickable(true);
        textView4.setOnClickListener(new FusionRecSaveClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams6.setMargins(2, 0, 2, 0);
        linearLayout2.addView(textView4, layoutParams6);
        TextView textView5 = new TextView(appCompatActivity);
        sCancelBtn = textView5;
        textView5.setText(" Batal ");
        FusionUiHelper.setSolidAccentBtn(textView5, -881790, -13947597);
        textView5.setTextSize(2, 11.0f);
        textView5.setPadding(8, 4, 8, 4);
        textView5.setClickable(true);
        textView5.setOnClickListener(new FusionRecCancelClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams7.setMargins(2, 0, 2, 0);
        linearLayout2.addView(textView5, layoutParams7);
        FrameLayout.LayoutParams layoutParams8 = new FrameLayout.LayoutParams(-2, -2, 8388691);
        layoutParams8.setMargins(10, 0, 0, 10);
        frameLayout.addView(linearLayout2, layoutParams8);
        linearLayout2.setElevation(160.0f);
        linearLayout2.bringToFront();
        updateRecUi(fusionWebViewManager);
    }

    public static void setStepCount(int i) {
        sStepCount = i;
    }

    public static void showRecPill(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null) {
            return;
        }
        WebView webView = fusionWebViewManager.mActiveWebView;
        if (webView == null) {
            hideRecPill();
            return;
        }
        if (webView.getVisibility() != 0) {
            hideRecPill();
            return;
        }
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
        if (activeTab == null) {
            hideRecPill();
            return;
        }
        if (activeTab.mIsMinimized) {
            hideRecPill();
            return;
        }
        LinearLayout linearLayout = sRecPill;
        if (linearLayout == null) {
            initAndShowRecPill(fusionWebViewManager);
            return;
        }
        linearLayout.setVisibility(0);
        linearLayout.bringToFront();
        updateRecUi(fusionWebViewManager);
    }

    public static void toggleCollapse(FusionWebViewManager fusionWebViewManager) {
        sIsCollapsed = !sIsCollapsed;
        updateRecUi(fusionWebViewManager);
    }

    public static void updateRecUi(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null || sRecPill == null) {
            return;
        }
        TextView textView = sCollapseBtn;
        TextView textView2 = sRecBtn;
        TextView textView3 = sDomBtn;
        TextView textView4 = sSaveBtn;
        TextView textView5 = sCancelBtn;
        if (sIsCollapsed) {
            if (textView != null) {
                textView.setText(" > ");
            }
            if (textView2 != null) {
                textView2.setVisibility(8);
            }
            if (textView3 != null) {
                textView3.setVisibility(8);
            }
            if (textView4 != null) {
                textView4.setVisibility(8);
            }
            if (textView5 != null) {
                textView5.setVisibility(8);
                return;
            }
            return;
        }
        if (textView != null) {
            textView.setText(" < ");
        }
        if (textView2 != null) {
            textView2.setVisibility(0);
        }
        if (!fusionWebViewManager.mIsRecording) {
            if (textView2 != null) {
                textView2.setText(" REC ( 0 ) ");
                FusionUiHelper.setSolidBtn(textView2);
            }
            if (textView3 != null) {
                textView3.setVisibility(8);
            }
            if (textView4 != null) {
                textView4.setVisibility(8);
            }
            if (textView5 != null) {
                textView5.setVisibility(8);
                return;
            }
            return;
        }
        if (textView2 != null) {
            textView2.setText(" ● REC ( " + sStepCount + " ) ");
            FusionUiHelper.setSolidAccentBtn(textView2, -881790, -13947597);
        }
        if (textView3 != null) {
            textView3.setVisibility(0);
        }
        if (textView4 != null) {
            textView4.setVisibility(0);
        }
        if (textView5 != null) {
            textView5.setVisibility(0);
        }
    }
}
