package com.termux.webview;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.termux.app.TermuxActivity;
import com.termux.shared.view.KeyboardUtils;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;
import java.util.ArrayList;
import java.util.UUID;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTabStateHelper {
    public static void applyOpacity(FusionWebViewManager fusionWebViewManager, FusionTab fusionTab) {
        float f;
        String str;
        if (fusionWebViewManager == null || fusionTab == null) {
            return;
        }
        WebView webView = fusionTab.mWebView;
        int i = fusionTab.mOpacityLevel;
        if (i == 0) {
            f = 1.0f;
            str = "Opacity: 100%";
        } else if (i == 1) {
            f = 0.75f;
            str = "Opacity: 75%";
        } else if (i == 2) {
            f = 0.4f;
            str = "Opacity: 40%";
        } else {
            f = 0.1f;
            str = "Opacity: 10% (Stealth)";
        }
        if (webView != null) {
            webView.setAlpha(f);
        }
        TextView textView = fusionWebViewManager.mOpacityBtn;
        if (textView != null) {
            textView.setText(str);
        }
    }

    public static void cycleOpacity(FusionWebViewManager fusionWebViewManager) {
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab == null) {
            return;
        }
        activeTab.mOpacityLevel = (activeTab.mOpacityLevel + 1) % 4;
        applyOpacity(fusionWebViewManager, activeTab);
    }

    public static FusionTab getActiveTab(FusionWebViewManager fusionWebViewManager) {
        ArrayList arrayList;
        TerminalSession currentSession;
        if (fusionWebViewManager == null || (arrayList = fusionWebViewManager.mTabList) == null) {
            return null;
        }
        int i = fusionWebViewManager.mActiveTab;
        if (i >= 0 && i < arrayList.size()) {
            return (FusionTab) arrayList.get(i);
        }
        TermuxActivity termuxActivity = fusionWebViewManager.mActivity;
        if (termuxActivity == null || (currentSession = termuxActivity.getCurrentSession()) == null) {
            return null;
        }
        int size = arrayList.size();
        for (int i2 = 0; i2 < size; i2++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i2);
            if (fusionTab != null && fusionTab.mBoundSession == currentSession) {
                fusionWebViewManager.mActiveTab = i2;
                return (FusionTab) arrayList.get(i2);
            }
        }
        return null;
    }

    public static void randomizeFingerprint(FusionWebViewManager fusionWebViewManager) {
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab == null) {
            return;
        }
        String str = "fp_" + UUID.randomUUID().toString().substring(0, 8);
        activeTab.mProfile = str;
        fusionWebViewManager.mCurrentProfile = str;
        AppCompatActivity appCompatActivity = fusionWebViewManager.mActivity;
        if (appCompatActivity != null) {
            Toast.makeText((Context) appCompatActivity, (CharSequence) ("Fingerprint Diacak: " + str), 0).show();
        }
    }

    public static void setControlPillCollapsed(FusionWebViewManager fusionWebViewManager, boolean z) {
        int childCount;
        if (fusionWebViewManager == null) {
            return;
        }
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab != null) {
            activeTab.mIsPillCollapsed = z;
        }
        LinearLayout linearLayout = fusionWebViewManager.mControlPill;
        if (linearLayout != null && (childCount = linearLayout.getChildCount()) >= 2) {
            if (z) {
                int i = childCount - 1;
                for (int i2 = 0; i2 < i; i2++) {
                    View childAt = linearLayout.getChildAt(i2);
                    if (childAt != null) {
                        childAt.setVisibility(8);
                    }
                }
                TextView textView = fusionWebViewManager.mCollapsePillBtn;
                if (textView != null) {
                    textView.setVisibility(0);
                    textView.setText(" < ");
                    return;
                }
                return;
            }
            int i3 = childCount - 1;
            for (int i4 = 0; i4 < i3; i4++) {
                View childAt2 = linearLayout.getChildAt(i4);
                if (childAt2 != null) {
                    childAt2.setVisibility(0);
                }
            }
            TextView textView2 = fusionWebViewManager.mCollapsePillBtn;
            if (textView2 != null) {
                textView2.setVisibility(0);
                textView2.setText(" > ");
            }
        }
    }

    public static void syncTabState(FusionWebViewManager fusionWebViewManager, FusionTab fusionTab) {
        if (fusionWebViewManager == null || fusionTab == null) {
            return;
        }
        updatePillCollapseUi(fusionWebViewManager, fusionTab);
        updateDesktopMenuText(fusionWebViewManager, fusionTab);
        applyOpacity(fusionWebViewManager, fusionTab);
        TextView textView = fusionWebViewManager.mMinPillBtn;
        if (textView != null) {
            if (fusionTab.mIsMinimized) {
                textView.setText(" MAX ");
            } else {
                textView.setText(" MIN ");
            }
        }
        String str = fusionTab.mProfile;
        if (str != null) {
            fusionWebViewManager.mCurrentProfile = str;
        }
    }

    public static void toggleCollapsePill(FusionWebViewManager fusionWebViewManager) {
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab == null) {
            return;
        }
        activeTab.mIsPillCollapsed = !activeTab.mIsPillCollapsed;
        updatePillCollapseUi(fusionWebViewManager, activeTab);
    }

    public static void toggleDesktop(FusionWebViewManager fusionWebViewManager) {
        WebView webView;
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab == null || (webView = activeTab.mWebView) == null) {
            return;
        }
        boolean z = !activeTab.mIsDesktop;
        activeTab.mIsDesktop = z;
        WebSettings settings = webView.getSettings();
        if (z) {
            settings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
        } else {
            settings.setUserAgentString("");
            settings.setUseWideViewPort(false);
            settings.setLoadWithOverviewMode(false);
        }
        webView.reload();
        updateDesktopMenuText(fusionWebViewManager, activeTab);
    }

    /* JADX WARN: Type inference failed for: r2v13, types: [android.app.Activity, android.content.Context, com.termux.app.TermuxActivity] */
    public static void toggleMinimize(FusionWebViewManager fusionWebViewManager) {
        WebView webView;
        TerminalView terminalView;
        FusionTab activeTab = getActiveTab(fusionWebViewManager);
        if (activeTab == null || (webView = activeTab.mWebView) == null) {
            return;
        }
        if (webView.getVisibility() == 0) {
            activeTab.mIsMinimized = true;
            webView.setVisibility(8);
            webView.onPause();
            LinearLayout linearLayout = fusionWebViewManager.mMenuCard;
            if (linearLayout != null) {
                linearLayout.setVisibility(8);
            }
            View view = fusionWebViewManager.mMenuBackdrop;
            if (view != null) {
                view.setVisibility(8);
            }
            FusionDrawerHelper.closeAllDrawers(fusionWebViewManager);
            FusionRecPillHelper.hideRecPill();
            TextView textView = fusionWebViewManager.mMinPillBtn;
            if (textView != null) {
                textView.setText(" MAX ");
            }
            View view2 = fusionWebViewManager.mTerminalPaneHost;
            if (view2 != null) {
                view2.setVisibility(0);
            }
            TermuxActivity r2 = fusionWebViewManager.mActivity;
            if (r2 != null && (terminalView = r2.getTerminalView()) != null) {
                terminalView.requestFocus();
                terminalView.requestFocusFromTouch();
            }
            if (r2 != null) {
                KeyboardUtils.clearDisableSoftKeyboardFlags((Activity) r2);
                Toast.makeText((Context) r2, "WebView diminimalkan", 0).show();
                return;
            }
        } else {
            activeTab.mIsMinimized = false;
            View view3 = fusionWebViewManager.mTerminalPaneHost;
            if (view3 != null) {
                view3.setVisibility(8);
            }
            webView.setVisibility(0);
            webView.onResume();
            webView.bringToFront();
            FrameLayout frameLayout = fusionWebViewManager.mPaneHost;
            if (frameLayout != null) {
                frameLayout.setVisibility(0);
                frameLayout.bringToFront();
            }
            LinearLayout linearLayout2 = fusionWebViewManager.mControlPill;
            if (linearLayout2 != null) {
                linearLayout2.bringToFront();
            }
            TextView textView2 = fusionWebViewManager.mMinPillBtn;
            if (textView2 != null) {
                textView2.setText(" MIN ");
            }
        }
        FusionRecPillHelper.showRecPill(fusionWebViewManager);
    }

    public static void updateDesktopMenuText(FusionWebViewManager fusionWebViewManager, FusionTab fusionTab) {
        TextView textView;
        if (fusionWebViewManager == null || fusionTab == null || (textView = fusionWebViewManager.mDesktopBtn) == null) {
            return;
        }
        if (fusionTab.mIsDesktop) {
            textView.setText("Mobile");
        } else {
            textView.setText("Desktop");
        }
    }

    public static void updatePillCollapseUi(FusionWebViewManager fusionWebViewManager, FusionTab fusionTab) {
        LinearLayout linearLayout;
        int childCount;
        if (fusionWebViewManager == null || fusionTab == null || (linearLayout = fusionWebViewManager.mControlPill) == null || (childCount = linearLayout.getChildCount()) < 2) {
            return;
        }
        if (fusionTab.mIsPillCollapsed) {
            int i = childCount - 1;
            for (int i2 = 0; i2 < i; i2++) {
                View childAt = linearLayout.getChildAt(i2);
                if (childAt != null) {
                    childAt.setVisibility(8);
                }
            }
            TextView textView = fusionWebViewManager.mCollapsePillBtn;
            if (textView != null) {
                textView.setVisibility(0);
                textView.setText(" < ");
                return;
            }
            return;
        }
        int i3 = childCount - 1;
        for (int i4 = 0; i4 < i3; i4++) {
            View childAt2 = linearLayout.getChildAt(i4);
            if (childAt2 != null) {
                childAt2.setVisibility(0);
            }
        }
        TextView textView2 = fusionWebViewManager.mCollapsePillBtn;
        if (textView2 != null) {
            textView2.setVisibility(0);
            textView2.setText(" > ");
        }
    }
}
