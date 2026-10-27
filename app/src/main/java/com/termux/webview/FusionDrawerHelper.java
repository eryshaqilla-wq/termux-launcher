package com.termux.webview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionDrawerHelper {
    public static LinearLayout sAgyDrawer;
    public static LinearLayout sChainDrawer;
    public static View sDrawerBackdrop;
    public static LinearLayout sRecModeDrawer;
    public static LinearLayout sRecipeDrawer;
    public static LinearLayout sTabDrawer;
    public static LinearLayout sWalletDrawer;
    public static TextView sWalletPathBtn;

    public static void closeAllDrawers(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null) {
            return;
        }
        LinearLayout linearLayout = fusionWebViewManager.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        LinearLayout linearLayout2 = sTabDrawer;
        if (linearLayout2 != null) {
            linearLayout2.setVisibility(8);
        }
        LinearLayout linearLayout3 = sAgyDrawer;
        if (linearLayout3 != null) {
            linearLayout3.setVisibility(8);
        }
        LinearLayout linearLayout4 = sWalletDrawer;
        if (linearLayout4 != null) {
            linearLayout4.setVisibility(8);
        }
        LinearLayout linearLayout5 = sChainDrawer;
        if (linearLayout5 != null) {
            linearLayout5.setVisibility(8);
        }
        LinearLayout linearLayout6 = sRecipeDrawer;
        if (linearLayout6 != null) {
            linearLayout6.setVisibility(8);
        }
        LinearLayout linearLayout7 = sRecModeDrawer;
        if (linearLayout7 != null) {
            linearLayout7.setVisibility(8);
        }
        View view = fusionWebViewManager.mMenuBackdrop;
        if (view != null) {
            view.setVisibility(8);
        }
        View view2 = sDrawerBackdrop;
        if (view2 != null) {
            view2.setVisibility(8);
        }
        WebView webView = fusionWebViewManager.mActiveWebView;
        if (webView == null) {
            FusionRecPillHelper.hideRecPill();
            return;
        }
        if (webView.getVisibility() != 0) {
            FusionRecPillHelper.hideRecPill();
            return;
        }
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
        if (activeTab == null) {
            FusionRecPillHelper.hideRecPill();
        } else if (activeTab.mIsMinimized) {
            FusionRecPillHelper.hideRecPill();
        } else {
            FusionTabStateHelper.setControlPillCollapsed(fusionWebViewManager, false);
            FusionRecPillHelper.showRecPill(fusionWebViewManager);
        }
    }

    public static void closeNativeDrawersOnly(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null) {
            return;
        }
        LinearLayout linearLayout = fusionWebViewManager.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        LinearLayout linearLayout2 = sTabDrawer;
        if (linearLayout2 != null) {
            linearLayout2.setVisibility(8);
        }
        LinearLayout linearLayout3 = sAgyDrawer;
        if (linearLayout3 != null) {
            linearLayout3.setVisibility(8);
        }
        LinearLayout linearLayout4 = sWalletDrawer;
        if (linearLayout4 != null) {
            linearLayout4.setVisibility(8);
        }
        LinearLayout linearLayout5 = sChainDrawer;
        if (linearLayout5 != null) {
            linearLayout5.setVisibility(8);
        }
        LinearLayout linearLayout6 = sRecipeDrawer;
        if (linearLayout6 != null) {
            linearLayout6.setVisibility(8);
        }
        LinearLayout linearLayout7 = sRecModeDrawer;
        if (linearLayout7 != null) {
            linearLayout7.setVisibility(8);
        }
        View view = fusionWebViewManager.mMenuBackdrop;
        if (view != null) {
            view.setVisibility(8);
        }
        View view2 = sDrawerBackdrop;
        if (view2 != null) {
            view2.setVisibility(8);
        }
    }

    public static void prepareDrawer(FusionWebViewManager fusionWebViewManager, LinearLayout linearLayout) {
        AppCompatActivity appCompatActivity;
        FrameLayout frameLayout;
        if (fusionWebViewManager == null || linearLayout == null || (appCompatActivity = fusionWebViewManager.mActivity) == null || (frameLayout = fusionWebViewManager.mPaneHost) == null) {
            return;
        }
        ViewParent parent = linearLayout.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        View view = sDrawerBackdrop;
        if (view == null) {
            view = new View(appCompatActivity);
            sDrawerBackdrop = view;
            view.setClickable(true);
            view.setFocusable(true);
            view.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        }
        ViewParent parent2 = view.getParent();
        if (parent2 != frameLayout) {
            if (parent2 != null) {
                ((ViewGroup) parent2).removeView(view);
            }
            frameLayout.addView(view, new FrameLayout.LayoutParams(-1, -1));
        }
        view.setBackgroundColor(-2013265920);
        view.setElevation(165.0f);
        view.setVisibility(0);
        view.bringToFront();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FusionUiHelper.getScreenWidth70(appCompatActivity), -1, 8388659);
        layoutParams.setMargins(0, 0, 0, 0);
        linearLayout.setBackground(FusionUiHelper.createRounded(-15329252, -12762806, 4.0f));
        linearLayout.setElevation(170.0f);
        frameLayout.addView(linearLayout, layoutParams);
        LinearLayout linearLayout2 = fusionWebViewManager.mMenuCard;
        if (linearLayout2 != null && linearLayout2 != linearLayout) {
            linearLayout2.setVisibility(8);
        }
        LinearLayout linearLayout3 = sTabDrawer;
        if (linearLayout3 != null && linearLayout3 != linearLayout) {
            linearLayout3.setVisibility(8);
        }
        LinearLayout linearLayout4 = sAgyDrawer;
        if (linearLayout4 != null && linearLayout4 != linearLayout) {
            linearLayout4.setVisibility(8);
        }
        LinearLayout linearLayout5 = sWalletDrawer;
        if (linearLayout5 != null && linearLayout5 != linearLayout) {
            linearLayout5.setVisibility(8);
        }
        LinearLayout linearLayout6 = sChainDrawer;
        if (linearLayout6 != null && linearLayout6 != linearLayout) {
            linearLayout6.setVisibility(8);
        }
        LinearLayout linearLayout7 = sRecipeDrawer;
        if (linearLayout7 != null && linearLayout7 != linearLayout) {
            linearLayout7.setVisibility(8);
        }
        LinearLayout linearLayout8 = sRecModeDrawer;
        if (linearLayout8 != null && linearLayout8 != linearLayout) {
            linearLayout8.setVisibility(8);
        }
        linearLayout.setVisibility(0);
        linearLayout.bringToFront();
        linearLayout.requestLayout();
        FusionTabStateHelper.setControlPillCollapsed(fusionWebViewManager, true);
        FusionRecPillHelper.hideRecPill();
    }

    public static void showAgyDrawer(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        ViewParent parent;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        LinearLayout linearLayout = sAgyDrawer;
        if (linearLayout != null && (parent = linearLayout.getParent()) != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sAgyDrawer = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(16, 16, 16, 16);
        LinearLayout linearLayout3 = new LinearLayout(appCompatActivity);
        linearLayout3.setOrientation(0);
        TextView textView = new TextView(appCompatActivity);
        textView.setText("BOT & Otomasi");
        textView.setTextColor(-3097345);
        textView.setTextSize(2, 15.0f);
        textView.setTypeface(null, 1);
        textView.setGravity(19);
        linearLayout3.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(appCompatActivity);
        textView2.setText("  X  ");
        textView2.setTextColor(-881790);
        textView2.setTextSize(2, 14.0f);
        textView2.setPadding(6, 2, 6, 2);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        linearLayout3.addView(textView2);
        linearLayout2.addView(linearLayout3);
        TextView textView3 = new TextView(appCompatActivity);
        textView3.setText("Perintah cerdas & alur kerja otomatis");
        textView3.setTextColor(-6577749);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(0, 2, 0, 8);
        linearLayout2.addView(textView3);
        buildAiEngineSelectorRow(appCompatActivity, linearLayout2, fusionWebViewManager);
        LinearLayout linearLayout4 = new LinearLayout(appCompatActivity);
        linearLayout4.setOrientation(0);
        linearLayout4.setPadding(0, 4, 0, 10);
        EditText editText = new EditText(appCompatActivity);
        editText.setHint("Ketik perintah BOT...");
        editText.setHintTextColor(-7829368);
        editText.setTextColor(-1);
        editText.setTextSize(2, 12.0f);
        FusionUiHelper.setSolidBox(editText);
        editText.setPadding(10, 6, 10, 6);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.rightMargin = 6;
        linearLayout4.addView(editText, layoutParams);
        TextView textView4 = new TextView(appCompatActivity);
        textView4.setText(" Eksekusi ");
        FusionUiHelper.setSolidAccentBtn(textView4, -7686920, -13947597);
        textView4.setTextSize(2, 12.0f);
        textView4.setPadding(10, 6, 10, 6);
        textView4.setClickable(true);
        textView4.setOnClickListener(new FusionAgyPromptExecClick(fusionWebViewManager, editText));
        linearLayout4.addView(textView4);
        linearLayout2.addView(linearLayout4);
        View view = new View(appCompatActivity);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, 1);
        layoutParams2.setMargins(0, 6, 0, 12);
        view.setBackgroundColor(-12762806);
        linearLayout2.addView(view, layoutParams2);
        TextView textView5 = new TextView(appCompatActivity);
        textView5.setText("Jalankan Resep");
        FusionUiHelper.setSolidBtn(textView5);
        textView5.setTextSize(2, 13.0f);
        textView5.setPadding(14, 10, 14, 10);
        textView5.setClickable(true);
        textView5.setOnClickListener(new FusionAgyActionClick(fusionWebViewManager, 0));
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(0, 4, 0, 4);
        linearLayout2.addView(textView5, layoutParams3);
        TextView textView6 = new TextView(appCompatActivity);
        textView6.setText("Kelola Resep");
        FusionUiHelper.setSolidBtn(textView6);
        textView6.setTextSize(2, 13.0f);
        textView6.setPadding(14, 10, 14, 10);
        textView6.setClickable(true);
        textView6.setOnClickListener(new FusionAgyManageRecipeClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.setMargins(0, 4, 0, 4);
        linearLayout2.addView(textView6, layoutParams4);
        TextView textView7 = new TextView(appCompatActivity);
        textView7.setText("Inspeksi DOM");
        FusionUiHelper.setSolidBtn(textView7);
        textView7.setTextSize(2, 13.0f);
        textView7.setPadding(14, 10, 14, 10);
        textView7.setClickable(true);
        textView7.setOnClickListener(new FusionAgyActionClick(fusionWebViewManager, 2));
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams5.setMargins(0, 4, 0, 4);
        linearLayout2.addView(textView7, layoutParams5);
        TextView textViewDev = new TextView(appCompatActivity);
        textViewDev.setText("Developer");
        FusionUiHelper.setSolidBtn(textViewDev);
        textViewDev.setTextSize(2, 13.0f);
        textViewDev.setPadding(14, 10, 14, 10);
        textViewDev.setClickable(true);
        textViewDev.setOnClickListener(new FusionAgyActionClick(fusionWebViewManager, 5));
        LinearLayout.LayoutParams layoutParamsDev = new LinearLayout.LayoutParams(-1, -2);
        layoutParamsDev.setMargins(0, 4, 0, 4);
        linearLayout2.addView(textViewDev, layoutParamsDev);
        TextView textView8 = new TextView(appCompatActivity);
        textView8.setText("TUTUP");
        FusionUiHelper.setSolidBtn(textView8);
        textView8.setTextSize(2, 12.0f);
        textView8.setGravity(17);
        textView8.setPadding(14, 10, 14, 10);
        textView8.setClickable(true);
        textView8.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams6.setMargins(0, 12, 0, 0);
        linearLayout2.addView(textView8, layoutParams6);
        prepareDrawer(fusionWebViewManager, linearLayout2);
    }

    private static void buildAiEngineSelectorRow(final AppCompatActivity activity, LinearLayout parentLayout, final FusionWebViewManager manager) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(1);
        row.setPadding(0, 0, 0, 10);

        TextView lbl = new TextView(activity);
        lbl.setText("MESIN AI:");
        lbl.setTextColor(-7829368);
        lbl.setTextSize(2, 10.0f);
        lbl.setTypeface(null, 1);
        lbl.setPadding(0, 0, 0, 4);
        row.addView(lbl);

        LinearLayout chipsRow = new LinearLayout(activity);
        chipsRow.setOrientation(0);

        final TextView btnCustom = new TextView(activity);
        btnCustom.setText("DeepSeek");
        btnCustom.setTextSize(2, 11.0f);
        btnCustom.setGravity(17);
        btnCustom.setPadding(6, 6, 6, 6);
        btnCustom.setClickable(true);

        final TextView btn9Router = new TextView(activity);
        btn9Router.setText("9Router");
        btn9Router.setTextSize(2, 11.0f);
        btn9Router.setGravity(17);
        btn9Router.setPadding(6, 6, 6, 6);
        btn9Router.setClickable(true);

        final TextView btnAgy = new TextView(activity);
        btnAgy.setText("AGY Copilot");
        btnAgy.setTextSize(2, 11.0f);
        btnAgy.setGravity(17);
        btnAgy.setPadding(6, 6, 6, 6);
        btnAgy.setClickable(true);

        final Runnable updateStyles = new Runnable() {
            @Override
            public void run() {
                String cur = FusionAiHelper.getActiveEngine();
                if ("custom".equalsIgnoreCase(cur)) {
                    btnCustom.setBackground(FusionUiHelper.createRounded(-13408615, -7686920, 4.0f));
                    btnCustom.setTextColor(-1);
                } else {
                    btnCustom.setBackground(FusionUiHelper.createRounded(-14802907, -12762806, 4.0f));
                    btnCustom.setTextColor(-6577749);
                }

                if ("9router".equalsIgnoreCase(cur)) {
                    btn9Router.setBackground(FusionUiHelper.createRounded(-13408615, -7686920, 4.0f));
                    btn9Router.setTextColor(-1);
                } else {
                    btn9Router.setBackground(FusionUiHelper.createRounded(-14802907, -12762806, 4.0f));
                    btn9Router.setTextColor(-6577749);
                }

                if ("agy".equalsIgnoreCase(cur)) {
                    btnAgy.setBackground(FusionUiHelper.createRounded(-13408615, -7686920, 4.0f));
                    btnAgy.setTextColor(-1);
                } else {
                    btnAgy.setBackground(FusionUiHelper.createRounded(-14802907, -12762806, 4.0f));
                    btnAgy.setTextColor(-6577749);
                }
            }
        };

        updateStyles.run();

        btnCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusionAiHelper.setActiveEngine("custom");
                updateStyles.run();
                if (manager != null) {
                    manager.evalOnActiveTab("window.AgyBridge && window.AgyBridge.setActiveAiEngine && window.AgyBridge.setActiveAiEngine('custom');");
                }
                Toast.makeText(activity, "Mesin AI Aktif: DeepSeek (Custom)", Toast.LENGTH_SHORT).show();
            }
        });

        btn9Router.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusionAiHelper.setActiveEngine("9router");
                updateStyles.run();
                if (manager != null) {
                    manager.evalOnActiveTab("window.AgyBridge && window.AgyBridge.setActiveAiEngine && window.AgyBridge.setActiveAiEngine('9router');");
                }
                Toast.makeText(activity, "Mesin AI Aktif: 9Router (Port 20128)", Toast.LENGTH_SHORT).show();
            }
        });

        btnAgy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusionAiHelper.setActiveEngine("agy");
                updateStyles.run();
                if (manager != null) {
                    manager.evalOnActiveTab("window.AgyBridge && window.AgyBridge.setActiveAiEngine && window.AgyBridge.setActiveAiEngine('agy');");
                }
                Toast.makeText(activity, "Mesin AI Aktif: AGY Copilot", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        lp1.rightMargin = 4;
        chipsRow.addView(btnCustom, lp1);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        lp2.rightMargin = 4;
        chipsRow.addView(btn9Router, lp2);

        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        chipsRow.addView(btnAgy, lp3);

        row.addView(chipsRow);
        parentLayout.addView(row);
    }

    public static void showChainDrawer(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        ViewParent parent;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        LinearLayout linearLayout = sChainDrawer;
        if (linearLayout != null && (parent = linearLayout.getParent()) != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sChainDrawer = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(16, 16, 16, 16);
        LinearLayout linearLayout3 = new LinearLayout(appCompatActivity);
        linearLayout3.setOrientation(0);
        TextView textView = new TextView(appCompatActivity);
        textView.setText("Pilih Jaringan Web3");
        textView.setTextColor(-7686920);
        textView.setTextSize(2, 15.0f);
        textView.setTypeface(null, 1);
        textView.setGravity(19);
        linearLayout3.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(appCompatActivity);
        textView2.setText("  X  ");
        textView2.setTextColor(-881790);
        textView2.setTextSize(2, 14.0f);
        textView2.setPadding(6, 2, 6, 2);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        linearLayout3.addView(textView2);
        linearLayout2.addView(linearLayout3);
        TextView textView3 = new TextView(appCompatActivity);
        textView3.setText("Ganti rantai EVM (RPC 127.0.0.1:3333)");
        textView3.setTextColor(-6577749);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(0, 2, 0, 10);
        linearLayout2.addView(textView3);
        ScrollView scrollView = new ScrollView(appCompatActivity);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout4 = new LinearLayout(appCompatActivity);
        linearLayout4.setOrientation(1);
        String[] names = FusionChainHelper.getNames();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                String str = names[i];
                TextView textView4 = new TextView(appCompatActivity);
                String str2 = FusionChainHelper.sCurrentChainKey;
                String keyByIndex = FusionChainHelper.getKeyByIndex(i);
                if (str2 == null || !str2.equals(keyByIndex)) {
                    textView4.setText(str);
                    FusionUiHelper.setSolidBtn(textView4);
                } else {
                    textView4.setText("* " + str);
                    FusionUiHelper.setSolidAccentBtn(textView4, -7686920, -13947597);
                }
                textView4.setTextSize(2, 13.0f);
                textView4.setPadding(12, 10, 12, 10);
                textView4.setClickable(true);
                textView4.setOnClickListener(new FusionChainItemClick(fusionWebViewManager, i));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                layoutParams.setMargins(0, 3, 0, 3);
                linearLayout4.addView(textView4, layoutParams);
            }
        }
        scrollView.addView(linearLayout4);
        linearLayout2.addView(scrollView);
        TextView textView5 = new TextView(appCompatActivity);
        textView5.setText("TUTUP");
        FusionUiHelper.setSolidBtn(textView5);
        textView5.setTextSize(2, 12.0f);
        textView5.setGravity(17);
        textView5.setPadding(12, 10, 12, 10);
        textView5.setClickable(true);
        textView5.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, 12, 0, 0);
        linearLayout2.addView(textView5, layoutParams2);
        prepareDrawer(fusionWebViewManager, linearLayout2);
    }

    public static void showRecipeDrawer(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null) {
            return;
        }
        fusionWebViewManager.closeAllDrawers();
        fusionWebViewManager.evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showRecipePicker){window.AgyBridge.showRecipePicker();}else if(typeof showRecipePickerModal==='function'){showRecipePickerModal();}");
    }

    public static void showRecordModeDrawer(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        ViewParent parent;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        LinearLayout linearLayout = sRecModeDrawer;
        if (linearLayout != null && (parent = linearLayout.getParent()) != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sRecModeDrawer = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(16, 16, 16, 16);
        LinearLayout linearLayout3 = new LinearLayout(appCompatActivity);
        linearLayout3.setOrientation(0);
        TextView textView = new TextView(appCompatActivity);
        textView.setText("METODE REKAM RESEP");
        textView.setTextColor(-7686920);
        textView.setTextSize(2, 15.0f);
        textView.setTypeface(null, 1);
        textView.setGravity(19);
        linearLayout3.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(appCompatActivity);
        textView2.setText("  X  ");
        textView2.setTextColor(-881790);
        textView2.setTextSize(2, 14.0f);
        textView2.setPadding(6, 2, 6, 2);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        linearLayout3.addView(textView2);
        linearLayout2.addView(linearLayout3);
        TextView textView3 = new TextView(appCompatActivity);
        textView3.setText("Pilih jenis interaksi yang ingin direkam:");
        textView3.setTextColor(-6577749);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(0, 2, 0, 10);
        linearLayout2.addView(textView3);
        LinearLayout linearLayout4 = new LinearLayout(appCompatActivity);
        linearLayout4.setOrientation(1);
        FusionUiHelper.setSolidBox(linearLayout4);
        linearLayout4.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 4, 0, 4);
        TextView textView4 = new TextView(appCompatActivity);
        textView4.setText("🎯  Mode DOM (HTML Selector)");
        textView4.setTextColor(-8596290);
        textView4.setTextSize(2, 13.0f);
        textView4.setTypeface(null, 1);
        linearLayout4.addView(textView4);
        TextView textView5 = new TextView(appCompatActivity);
        textView5.setText("Merekam interaksi elemen, tombol & input form berbasis DOM");
        textView5.setTextColor(-6577749);
        textView5.setTextSize(2, 11.0f);
        linearLayout4.addView(textView5);
        linearLayout4.setClickable(true);
        linearLayout4.setOnClickListener(new FusionRecModeSelectClick(fusionWebViewManager, "dom"));
        linearLayout2.addView(linearLayout4, layoutParams);
        LinearLayout linearLayout5 = new LinearLayout(appCompatActivity);
        linearLayout5.setOrientation(1);
        FusionUiHelper.setSolidBox(linearLayout5);
        linearLayout5.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, 4, 0, 4);
        TextView textView6 = new TextView(appCompatActivity);
        textView6.setText("👆  Mode Touch (Gestur Layar)");
        textView6.setTextColor(-3097345);
        textView6.setTextSize(2, 13.0f);
        textView6.setTypeface(null, 1);
        linearLayout5.addView(textView6);
        TextView textView7 = new TextView(appCompatActivity);
        textView7.setText("Merekam koordinat sentuhan sentuh & gestur jari secara natural");
        textView7.setTextColor(-6577749);
        textView7.setTextSize(2, 11.0f);
        linearLayout5.addView(textView7);
        linearLayout5.setClickable(true);
        linearLayout5.setOnClickListener(new FusionRecModeSelectClick(fusionWebViewManager, "touch"));
        linearLayout2.addView(linearLayout5, layoutParams2);
        LinearLayout linearLayout6 = new LinearLayout(appCompatActivity);
        linearLayout6.setOrientation(1);
        FusionUiHelper.setSolidBox(linearLayout6);
        linearLayout6.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(0, 4, 0, 4);
        TextView textView8 = new TextView(appCompatActivity);
        textView8.setText("⚡  Mode Hybrid (Touch + DOM)");
        textView8.setTextColor(-881790);
        textView8.setTextSize(2, 13.0f);
        textView8.setTypeface(null, 1);
        linearLayout6.addView(textView8);
        TextView textView9 = new TextView(appCompatActivity);
        textView9.setText("Kombinasi sentuhan jari dengan selektor elemen HTML (Rekomendasi)");
        textView9.setTextColor(-6577749);
        textView9.setTextSize(2, 11.0f);
        linearLayout6.addView(textView9);
        linearLayout6.setClickable(true);
        linearLayout6.setOnClickListener(new FusionRecModeSelectClick(fusionWebViewManager, "hybrid"));
        linearLayout2.addView(linearLayout6, layoutParams3);
        TextView textView10 = new TextView(appCompatActivity);
        textView10.setText("BATAL");
        FusionUiHelper.setSolidBtn(textView10);
        textView10.setTextSize(2, 12.0f);
        textView10.setGravity(17);
        textView10.setPadding(0, 10, 0, 10);
        textView10.setClickable(true);
        textView10.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.topMargin = 14;
        linearLayout2.addView(textView10, layoutParams4);
        prepareDrawer(fusionWebViewManager, linearLayout2);
    }

    /* JADX WARN: Code duplicated, block: B:34:0x0151  */
    public static void showTabDrawer(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        ViewParent parent;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        LinearLayout linearLayout = sTabDrawer;
        if (linearLayout != null && (parent = linearLayout.getParent()) != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sTabDrawer = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(16, 16, 16, 16);
        LinearLayout linearLayout3 = new LinearLayout(appCompatActivity);
        linearLayout3.setOrientation(0);
        TextView textView = new TextView(appCompatActivity);
        textView.setText("TAB MANAGER");
        textView.setTextColor(-7686920);
        textView.setTextSize(2, 15.0f);
        textView.setTypeface(null, 1);
        textView.setGravity(19);
        linearLayout3.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(appCompatActivity);
        textView2.setText("  X  ");
        textView2.setTextColor(-881790);
        textView2.setTextSize(2, 14.0f);
        textView2.setPadding(6, 2, 6, 2);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        linearLayout3.addView(textView2);
        linearLayout2.addView(linearLayout3);
        TextView textView3 = new TextView(appCompatActivity);
        StringBuilder sb = new StringBuilder();
        sb.append("Active Sessions: ");
        ArrayList arrayList = fusionWebViewManager.mTabList;
        sb.append(arrayList != null ? arrayList.size() : 0);
        textView3.setText(sb.toString());
        textView3.setTextColor(-6577749);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(0, 2, 0, 10);
        linearLayout2.addView(textView3);
        ScrollView scrollView = new ScrollView(appCompatActivity);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout4 = new LinearLayout(appCompatActivity);
        linearLayout4.setOrientation(1);
        ArrayList arrayList2 = fusionWebViewManager.mTabList;
        if (arrayList2 != null) {
            for (int i = 0; i < arrayList2.size(); i++) {
                FusionTab fusionTab = (FusionTab) arrayList2.get(i);
                LinearLayout linearLayout5 = new LinearLayout(appCompatActivity);
                linearLayout5.setOrientation(0);
                FusionUiHelper.setSolidBox(linearLayout5);
                linearLayout5.setPadding(10, 6, 10, 6);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                layoutParams.setMargins(0, 3, 0, 3);
                TextView textView4 = new TextView(appCompatActivity);
                StringBuilder sb2 = new StringBuilder();
                if (i == fusionWebViewManager.mActiveTab) {
                    sb2.append("* Tab ");
                    sb2.append(i + 1);
                    sb2.append(": ");
                    textView4.setTextColor(-7686920);
                } else {
                    sb2.append("Tab ");
                    sb2.append(i + 1);
                    sb2.append(": ");
                    textView4.setTextColor(-1907994);
                }
                if (fusionTab == null) {
                    sb2.append("about:blank");
                } else {
                    String str = fusionTab.mTitle;
                    if (str == null || str.isEmpty()) {
                        String str2 = fusionTab.mUrl;
                        if (str2 != null) {
                            sb2.append(str2);
                        } else {
                            sb2.append("about:blank");
                        }
                    } else {
                        sb2.append(fusionTab.mTitle);
                    }
                }
                textView4.setText(sb2.toString());
                textView4.setTextSize(2, 12.0f);
                textView4.setGravity(19);
                linearLayout5.addView(textView4, new LinearLayout.LayoutParams(0, -2, 1.0f));
                TextView textView5 = new TextView(appCompatActivity);
                textView5.setText(" X ");
                textView5.setTextColor(-881790);
                textView5.setTextSize(2, 12.0f);
                textView5.setPadding(4, 2, 4, 2);
                textView5.setClickable(true);
                textView5.setOnClickListener(new FusionTabCloseItemClick(fusionWebViewManager, i));
                linearLayout5.addView(textView5);
                linearLayout5.setClickable(true);
                linearLayout5.setOnClickListener(new FusionTabSelectItemClick(fusionWebViewManager, i));
                linearLayout4.addView(linearLayout5, layoutParams);
            }
        }
        scrollView.addView(linearLayout4);
        linearLayout2.addView(scrollView);
        LinearLayout linearLayout6 = new LinearLayout(appCompatActivity);
        linearLayout6.setOrientation(0);
        linearLayout6.setPadding(0, 6, 0, 0);
        TextView textView6 = new TextView(appCompatActivity);
        textView6.setText("+ TAB BARU");
        FusionUiHelper.setSolidAccentBtn(textView6, -7686920, -13947597);
        textView6.setTextSize(2, 12.0f);
        textView6.setGravity(17);
        textView6.setPadding(8, 6, 8, 6);
        textView6.setClickable(true);
        textView6.setOnClickListener(new FusionTabNewClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams2.rightMargin = 4;
        linearLayout6.addView(textView6, layoutParams2);
        TextView textView7 = new TextView(appCompatActivity);
        textView7.setText("TUTUP TAB INI");
        FusionUiHelper.setSolidAccentBtn(textView7, -881790, -13947597);
        textView7.setTextSize(2, 12.0f);
        textView7.setGravity(17);
        textView7.setPadding(8, 6, 8, 6);
        textView7.setClickable(true);
        textView7.setOnClickListener(new FusionTabCloseCurrentClick(fusionWebViewManager));
        linearLayout6.addView(textView7, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(linearLayout6);
        prepareDrawer(fusionWebViewManager, linearLayout2);
    }

    public static void showWalletDrawer(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        ViewParent parent;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        FusionWalletHelper.reload(appCompatActivity);
        LinearLayout linearLayout = sWalletDrawer;
        if (linearLayout != null && (parent = linearLayout.getParent()) != null) {
            ((ViewGroup) parent).removeView(linearLayout);
        }
        LinearLayout linearLayout2 = new LinearLayout(appCompatActivity);
        sWalletDrawer = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(16, 16, 16, 16);
        LinearLayout linearLayout3 = new LinearLayout(appCompatActivity);
        linearLayout3.setOrientation(0);
        TextView textView = new TextView(appCompatActivity);
        textView.setText("Pilih Dompet Web3");
        textView.setTextColor(-7686920);
        textView.setTextSize(2, 15.0f);
        textView.setTypeface(null, 1);
        textView.setGravity(19);
        linearLayout3.addView(textView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(appCompatActivity);
        textView2.setText("  X  ");
        textView2.setTextColor(-881790);
        textView2.setTextSize(2, 14.0f);
        textView2.setPadding(6, 2, 6, 2);
        textView2.setClickable(true);
        textView2.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        linearLayout3.addView(textView2);
        linearLayout2.addView(linearLayout3);
        TextView textView3 = new TextView(appCompatActivity);
        textView3.setText("Ganti akun dompet aktif seketika");
        textView3.setTextColor(-6577749);
        textView3.setTextSize(2, 11.0f);
        textView3.setPadding(0, 2, 0, 10);
        linearLayout2.addView(textView3);
        ScrollView scrollView = new ScrollView(appCompatActivity);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayout4 = new LinearLayout(appCompatActivity);
        linearLayout4.setOrientation(1);
        String[] names = FusionWalletHelper.getNames();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                String str = names[i];
                TextView textView4 = new TextView(appCompatActivity);
                if (i == FusionWalletHelper.sMasterIndex) {
                    textView4.setText("* " + str);
                    FusionUiHelper.setSolidAccentBtn(textView4, -7686920, -13947597);
                } else {
                    textView4.setText(str);
                    FusionUiHelper.setSolidBtn(textView4);
                }
                textView4.setTextSize(2, 13.0f);
                textView4.setPadding(12, 10, 12, 10);
                textView4.setClickable(true);
                textView4.setOnClickListener(new FusionWalletItemClick(fusionWebViewManager, i));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                layoutParams.setMargins(0, 3, 0, 3);
                linearLayout4.addView(textView4, layoutParams);
            }
        }
        scrollView.addView(linearLayout4);
        linearLayout2.addView(scrollView);
        TextView textView5 = new TextView(appCompatActivity);
        textView5.setText("TUTUP");
        FusionUiHelper.setSolidBtn(textView5);
        textView5.setTextSize(2, 12.0f);
        textView5.setGravity(17);
        textView5.setPadding(12, 10, 12, 10);
        textView5.setClickable(true);
        textView5.setOnClickListener(new FusionDrawerCloseClick(fusionWebViewManager));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, 12, 0, 0);
        linearLayout2.addView(textView5, layoutParams2);
        prepareDrawer(fusionWebViewManager, linearLayout2);
    }

    public static void showWalletPathDialog(FusionWebViewManager fusionWebViewManager) {
        AppCompatActivity appCompatActivity;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle("Lokasi DB Dompet (wallets_db.json)");
        builder.setItems(new String[]{"1. /root/wallet/wallets_db.json (Chroot Default)", "2. /data/data/com.termux/files/home/wallet/wallets_db.json", "3. /sdcard/Download/wallets_db.json"}, new FusionWalletPathSelectClick(fusionWebViewManager));
        builder.setNegativeButton("Batal", (DialogInterface.OnClickListener) null);
        builder.show();
    }

    public static void toggleMenuDrawer(FusionWebViewManager fusionWebViewManager) {
        LinearLayout linearLayout;
        if (fusionWebViewManager == null || (linearLayout = fusionWebViewManager.mMenuCard) == null) {
            return;
        }
        if (linearLayout.getVisibility() == 0) {
            closeAllDrawers(fusionWebViewManager);
            return;
        }
        prepareDrawer(fusionWebViewManager, linearLayout);
        try {
            fusionWebViewManager.updateMenuTexts();
        } catch (Throwable unused) {
        }
    }
}
