package com.termux.webview;

import android.view.View;
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

    // Native Standby Step-by-Step Pill
    public static LinearLayout sRecContainer;
    public static LinearLayout sStepPill;
    public static TextView sStepCollapseBtn;
    public static boolean sStepIsCollapsed;
    public static TextView sWalletBtn;
    public static TextView sEmailBtn;
    public static TextView sRefBtn;
    public static TextView sReloadBtn;
    public static TextView sWipeBtn;
    public static TextView sFpBtn;

    public static void hideRecPill() {
        LinearLayout container = sRecContainer;
        if (container != null) {
            container.setVisibility(8);
        }
        LinearLayout linearLayout = sRecPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
    }

    public static void hideRecPill(FusionWebViewManager fusionWebViewManager) {
        hideRecPill();
    }

    public static void initAndShowRecPill(final FusionWebViewManager fusionWebViewManager) {
        final AppCompatActivity appCompatActivity;
        FrameLayout frameLayout;
        if (fusionWebViewManager == null || (appCompatActivity = fusionWebViewManager.mActivity) == null || (frameLayout = fusionWebViewManager.mPaneHost) == null) {
            return;
        }
        LinearLayout container = sRecContainer;
        if (container != null) {
            ViewParent parent = container.getParent();
            if (parent != frameLayout) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(container);
                }
                ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
                if (layoutParams != null) {
                    frameLayout.addView(container, layoutParams);
                } else {
                    FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 8388691);
                    layoutParams2.setMargins(10, 0, 0, 10);
                    frameLayout.addView(container, layoutParams2);
                }
            }
            container.setVisibility(0);
            if (sRecPill != null) {
                sRecPill.setVisibility(0);
            }
            container.setElevation(160.0f);
            container.bringToFront();
            updateRecUi(fusionWebViewManager);
            return;
        }

        // 1. Kontainer Vertikal Native (Mengunci posisi sStepPill tepat di atas sRecPill)
        LinearLayout recContainer = new LinearLayout(appCompatActivity);
        sRecContainer = recContainer;
        recContainer.setOrientation(1); // VERTICAL

        // 2. Baris Atas: sStepPill (Smart Step Actions)
        LinearLayout stepPill = new LinearLayout(appCompatActivity);
        sStepPill = stepPill;
        stepPill.setOrientation(0); // HORIZONTAL
        FusionUiHelper.setSolidBox(stepPill);
        stepPill.setPadding(3, 3, 3, 3);
        LinearLayout.LayoutParams stepPillLp = new LinearLayout.LayoutParams(-2, -2);
        stepPillLp.bottomMargin = 3;
        recContainer.addView(stepPill, stepPillLp);

        // Child Step Buttons:
        // [ < ] / [ > ]
        TextView stepCollapse = new TextView(appCompatActivity);
        sStepCollapseBtn = stepCollapse;
        stepCollapse.setText(sStepIsCollapsed ? " > " : " < ");
        FusionUiHelper.setSolidBtn(stepCollapse);
        stepCollapse.setTextSize(2, 10.0f);
        stepCollapse.setPadding(6, 3, 6, 3);
        stepCollapse.setClickable(true);
        stepCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sStepIsCollapsed = !sStepIsCollapsed;
                updateRecUi(fusionWebViewManager);
            }
        });
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(-2, -2);
        scLp.setMargins(1, 0, 1, 0);
        stepPill.addView(stepCollapse, scLp);

        // WALLET >
        TextView walletBtn = new TextView(appCompatActivity);
        sWalletBtn = walletBtn;
        walletBtn.setText(" WALLET > ");
        FusionUiHelper.setSolidBtn(walletBtn);
        walletBtn.setTextSize(2, 10.0f);
        walletBtn.setPadding(6, 3, 6, 3);
        walletBtn.setClickable(true);
        walletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nextAddr = fusionWebViewManager.switchToNextWallet();
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'next_wallet', timestamp:Date.now(), text:'Ganti Wallet -> " + nextAddr + "'}); try { window.dispatchEvent(new CustomEvent('accountsChanged', {detail: ['" + nextAddr + "']})); } catch(_){}", null);
                }
                fusionWebViewManager.updateMenuTexts();
            }
        });
        LinearLayout.LayoutParams wbLp = new LinearLayout.LayoutParams(-2, -2);
        wbLp.setMargins(1, 0, 1, 0);
        stepPill.addView(walletBtn, wbLp);

        // EMAIL >
        TextView emailBtn = new TextView(appCompatActivity);
        sEmailBtn = emailBtn;
        emailBtn.setText(" EMAIL > ");
        FusionUiHelper.setSolidBtn(emailBtn);
        emailBtn.setTextSize(2, 10.0f);
        emailBtn.setPadding(6, 3, 6, 3);
        emailBtn.setClickable(true);
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("(function(){ var ne = (typeof AgyDB !== 'undefined' && AgyDB.nextEmail) ? AgyDB.nextEmail() : 'tuyul@gmail.com'; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'next_email', timestamp:Date.now(), text:'Ganti Email -> ' + ne, value:ne}); updateAgyHUD('Smart Email', 'Aktif: ' + ne, 'success'); return ne; })()", null);
                }
            }
        });
        LinearLayout.LayoutParams ebLp = new LinearLayout.LayoutParams(-2, -2);
        ebLp.setMargins(1, 0, 1, 0);
        stepPill.addView(emailBtn, ebLp);

        // REF
        TextView refBtn = new TextView(appCompatActivity);
        sRefBtn = refBtn;
        refBtn.setText(" REF ");
        FusionUiHelper.setSolidBtn(refBtn);
        refBtn.setTextSize(2, 10.0f);
        refBtn.setPadding(6, 3, 6, 3);
        refBtn.setClickable(true);
        refBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("(function(){ var nr = (typeof AgyDB !== 'undefined' && AgyDB.nextRef) ? AgyDB.nextRef() : 'REF123'; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'ref', timestamp:Date.now(), text:'Ref Code -> ' + nr, value:nr}); updateAgyHUD('Referral Code', 'Aktif: ' + nr, 'success'); return nr; })()", null);
                }
            }
        });
        LinearLayout.LayoutParams rbLp = new LinearLayout.LayoutParams(-2, -2);
        rbLp.setMargins(1, 0, 1, 0);
        stepPill.addView(refBtn, rbLp);

        // RELOAD
        TextView reloadBtn = new TextView(appCompatActivity);
        sReloadBtn = reloadBtn;
        reloadBtn.setText(" RELOAD ");
        FusionUiHelper.setSolidBtn(reloadBtn);
        reloadBtn.setTextSize(2, 10.0f);
        reloadBtn.setPadding(6, 3, 6, 3);
        reloadBtn.setClickable(true);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'reload', timestamp:Date.now(), text:'Reload Halaman'}); try{sessionStorage.setItem('__agy_rec_active__','true'); sessionStorage.setItem('__fusion_rec_active__','true');}catch(_){}", null);
                    webView.reload();
                }
            }
        });
        LinearLayout.LayoutParams relLp = new LinearLayout.LayoutParams(-2, -2);
        relLp.setMargins(1, 0, 1, 0);
        stepPill.addView(reloadBtn, relLp);

        // WIPE
        TextView wipeBtn = new TextView(appCompatActivity);
        sWipeBtn = wipeBtn;
        wipeBtn.setText(" WIPE ");
        FusionUiHelper.setSolidBtn(wipeBtn);
        wipeBtn.setTextSize(2, 10.0f);
        wipeBtn.setPadding(6, 3, 6, 3);
        wipeBtn.setClickable(true);
        wipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.clearCache(true);
                    webView.clearFormData();
                    try { android.webkit.WebStorage.getInstance().deleteAllData(); } catch(Throwable unused) {}
                    webView.evaluateJavascript("try{localStorage.clear(); sessionStorage.clear(); sessionStorage.setItem('__agy_rec_active__','true'); sessionStorage.setItem('__fusion_rec_active__','true');}catch(_){}; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'wipe_cache', timestamp:Date.now(), text:'Wipe Cache & Storage'});", null);
                }
            }
        });
        LinearLayout.LayoutParams wpLp = new LinearLayout.LayoutParams(-2, -2);
        wpLp.setMargins(1, 0, 1, 0);
        stepPill.addView(wipeBtn, wpLp);

        // FP
        TextView fpBtn = new TextView(appCompatActivity);
        sFpBtn = fpBtn;
        fpBtn.setText(" FP ");
        FusionUiHelper.setSolidBtn(fpBtn);
        fpBtn.setTextSize(2, 10.0f);
        fpBtn.setPadding(6, 3, 6, 3);
        fpBtn.setClickable(true);
        fpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = fusionWebViewManager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("(function(){ var fp = (window.GhostBridge && window.GhostBridge.nextFingerprint) ? window.GhostBridge.nextFingerprint() : 'Acak'; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'fingerprint', timestamp:Date.now(), text:'Fingerprint -> ' + fp, value:fp}); updateAgyHUD('Fingerprint Faker', 'Model HP: ' + fp, 'success'); return fp; })()", null);
                }
            }
        });
        LinearLayout.LayoutParams fpLp = new LinearLayout.LayoutParams(-2, -2);
        fpLp.setMargins(1, 0, 1, 0);
        stepPill.addView(fpBtn, fpLp);

        // 3. Baris Bawah: sRecPill (Ketua / Baris Utama)
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

        recContainer.addView(linearLayout2, new LinearLayout.LayoutParams(-2, -2));

        FrameLayout.LayoutParams layoutParams8 = new FrameLayout.LayoutParams(-2, -2, 8388691);
        layoutParams8.setMargins(10, 0, 0, 10);
        frameLayout.addView(recContainer, layoutParams8);
        recContainer.setElevation(160.0f);
        recContainer.bringToFront();
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
        if (webView == null || webView.getVisibility() != 0) {
            hideRecPill();
            return;
        }
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
        if (activeTab == null || activeTab.mIsMinimized) {
            hideRecPill();
            return;
        }
        LinearLayout container = sRecContainer;
        if (container == null) {
            initAndShowRecPill(fusionWebViewManager);
            return;
        }
        container.setVisibility(0);
        if (sRecPill != null) {
            sRecPill.setVisibility(0);
        }
        container.bringToFront();
        updateRecUi(fusionWebViewManager);
    }

    public static void toggleCollapse(FusionWebViewManager fusionWebViewManager) {
        sIsCollapsed = !sIsCollapsed;
        updateRecUi(fusionWebViewManager);
    }

    public static void updateRecUi(FusionWebViewManager fusionWebViewManager) {
        if (fusionWebViewManager == null || sRecContainer == null) {
            return;
        }
        boolean isRec = fusionWebViewManager.mIsRecording;

        // 1. Pastikan Ketua (sRecPill) SELALU VISIBLE saat container aktif!
        if (sRecPill != null) {
            sRecPill.setVisibility(0);
        }

        // 2. Kontrol Step Pill (Baris Atas)
        if (sStepPill != null) {
            if (!isRec || sIsCollapsed) {
                sStepPill.setVisibility(8);
            } else {
                sStepPill.setVisibility(0);
                if (sStepCollapseBtn != null) {
                    sStepCollapseBtn.setText(sStepIsCollapsed ? " > " : " < ");
                }
                int stepVisibility = sStepIsCollapsed ? 8 : 0;
                if (sWalletBtn != null) sWalletBtn.setVisibility(stepVisibility);
                if (sEmailBtn != null) sEmailBtn.setVisibility(stepVisibility);
                if (sRefBtn != null) sRefBtn.setVisibility(stepVisibility);
                if (sReloadBtn != null) sReloadBtn.setVisibility(stepVisibility);
                if (sWipeBtn != null) sWipeBtn.setVisibility(stepVisibility);
                if (sFpBtn != null) sFpBtn.setVisibility(stepVisibility);
            }
        }

        // Kontrol Rec Pill Utama (Baris Bawah)
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
        if (!isRec) {
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
