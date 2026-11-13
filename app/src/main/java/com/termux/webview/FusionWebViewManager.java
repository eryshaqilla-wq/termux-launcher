package com.termux.webview;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.os.StrictMode;
import android.os.PowerManager;
import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.net.HttpURLConnection;
import java.net.URL;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.app.FusionToast;
import androidx.appcompat.app.AppCompatActivity;
import com.termux.app.TermuxActivity;
import com.termux.app.TermuxIpcServer;
import com.termux.shared.view.KeyboardUtils;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebViewManager implements View.OnClickListener, ValueCallback<String> {
    public static FusionWebViewManager sInstance;
    public WebView mActiveWebView;
    public TermuxActivity mActivity;
    public TextView mAgyBtn;
    public TextView mAiBridgeBtn;
    public TextView mAutoSignBtn;
    public TerminalSession mBoundSession;
    public TextView mBurgerBtn;
    public TextView mChainInfoText;
    public TextView mCollapsePillBtn;
    public TextView mContextStatusText;
    public LinearLayout mControlPill;
    public android.widget.HorizontalScrollView mToolScroll;
    public LinearLayout mToolPill;
    public TextView mToolToggleBtn;
    public TextView mToolWalletNextBtn;
    public TextView mToolNetWalletBtn;
    public TextView mToolCopyWalletBtn;
    public TextView mToolReloadBtn;
    public TextView mToolWipeBtn;
    public TextView mToolFpBtn;
    public TextView mToolCollapseBtn;
    public TextView mCopyAddressBtn;
    public TextView mDesktopBtn;
    public TextView mDevModeBtn;
    public boolean mIsRecording;
    public TextView mKillWorkerBtn;
    public View mMenuBackdrop;
    public LinearLayout mMenuCard;
    public TextView mMinBtn;
    public TextView mMinPillBtn;
    public TextView mNewWorkerBtn;
    public TextView mOpacityBtn;
    public FrameLayout mPaneHost;
    public TextView mQuickBackBtn;
    public TextView mProxyBtn;
    public TextView mRandomFpBtn;
    public TextView mRecBtn;
    public TextView mRunMacroBtn;
    public TextView mStartDaemonBtn;
    public TextView mStatusText;
    public TextView mSwitchChainBtn;
    public TextView mSwitchWalletBtn;
    public TextView mTabBtn;
    public View mTerminalPaneHost;
    public TextView mWalletInfoText;
    public WebView mWorkerWebView;
    public String mCurrentProfile = "default";
    public String mCurrentWalletName = "eso-1";
    public String mCurrentWalletAddress = "0x79E0EE08c6516d1204821f083285Ab65660c26E4";
    public String mCurrentSolWalletName = "eso-1";
    public String mCurrentSolWalletAddress = "8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf";
    public String mCurrentTronWalletName = "eso-1";
    public String mCurrentTronWalletAddress = "TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF";
    public String mCurrentSuiWalletName = "eso-1";
    public String mCurrentSuiWalletAddress = "0xe11bb239021e68c0494c91b5ef96dfc507d7a00f653d95c4dec56ac9c220a151";
    public String mCurrentAptosWalletName = "eso-1";
    public String mCurrentAptosWalletAddress = "0xb531dfa998a53cfcb83cbd26eaa5c2fe5c192d6e79c00eb737fdf0ab9ea1c9ae";
    public String mCurrentIotaWalletName = "eso-1";
    public String mCurrentIotaWalletAddress = "0xdcc1545d60471021e3aca3f8de472083eec2ca8cc6db9e5109ed8f5d6a6c55fd";
    public String mCurrentChainKey = "base";
    public String mWcUri = "";
    public boolean mIsGhostMode = false;
    public boolean mIsDesktopMode = false;
    public boolean mIsAutoMode = true;
    public boolean mViewsInitialized = false;
    public int mCurrentOpacityLevel = 0;
    public boolean mIsMinimized = false;
    public int mActiveTab = 0;
    public ArrayList mTabList = new ArrayList();
    public int mNextTabId = 1;
    public boolean mIsDevMode = true;
    public PowerManager.WakeLock mMacroWakeLock;

    public static class AutoSignClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public AutoSignClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.toggleAutoSign();
            }
        }
    }

    public static class BackClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public BackClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.performQuickBack();
            }
        }
    }

    public static class BurgerClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public BurgerClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.toggleMenuCard();
            }
        }
    }

    public static class ChainSelectListener implements DialogInterface.OnClickListener {
        public final FusionWebViewManager mManager;

        public ChainSelectListener(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.onChainSelected(i);
            }
        }
    }

    public static class ClearCacheClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public ClearCacheClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.clearWebviewCache();
            }
        }
    }

    public static class CloseClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public CloseClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.killActiveTab();
            }
        }
    }

    public static class CloseMenuClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public CloseMenuClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.closeAllDrawers();
            }
        }
    }

    public static class CloseRunnable implements Runnable {
        public final FusionWebViewManager mManager;

        public CloseRunnable(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // java.lang.Runnable
        public void run() {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.closeWebViewInternal();
            }
        }
    }

    public static class CopyAddressClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public CopyAddressClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            String str;
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null || (str = fusionWebViewManager.mCurrentWalletAddress) == null) {
                return;
            }
            this.mManager.copyAddressToClipboard(str);
        }
    }

    public static class DesktopClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public DesktopClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.toggleDesktopMode();
            }
        }
    }

    public static class EvalRunnable implements Runnable {
        public final String mJsCode;
        public final FusionWebViewManager mManager;

        public EvalRunnable(FusionWebViewManager fusionWebViewManager, String str) {
            this.mManager = fusionWebViewManager;
            this.mJsCode = str;
        }

        @Override // java.lang.Runnable
        public void run() {
            WebView webView;
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null || (webView = fusionWebViewManager.mActiveWebView) == null) {
                return;
            }
            webView.evaluateJavascript(this.mJsCode, null);
        }
    }

    public static class MacroValueCallback implements ValueCallback<String> {
        public final FusionWebViewManager mManager;

        public MacroValueCallback(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.webkit.ValueCallback
        public void onReceiveValue(String str) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.saveMacroResult(str);
            }
        }
    }

    public static class OpenRunnable implements Runnable {
        public final String mChainKey;
        public final boolean mIsAuto;
        public final boolean mIsDesktop;
        public final boolean mIsGhost;
        public final FusionWebViewManager mManager;
        public final String mProfile;
        public final String mUrl;
        public final String mWalletName;
        public final String mWcUri;
        public final String mProxy;
        public final int mIntervalMins;

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i) {
            this.mManager = fusionWebViewManager;
            this.mUrl = str;
            this.mProfile = str2;
            this.mWalletName = str3;
            this.mChainKey = str4;
            this.mIsGhost = z;
            this.mIsDesktop = z2;
            this.mIsAuto = z3;
            this.mWcUri = str5;
            this.mProxy = str6;
            this.mIntervalMins = i;
        }

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
            this(fusionWebViewManager, str, str2, str3, str4, z, z2, z3, str5, "", 0);
        }

        @Override // java.lang.Runnable
        public void run() {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null) {
                return;
            }
            fusionWebViewManager.openUrlInternal(this.mUrl, this.mProfile, this.mWalletName, this.mChainKey, this.mIsGhost, this.mIsDesktop, this.mIsAuto, this.mWcUri, this.mProxy, this.mIntervalMins);
        }
    }

    public static class RecClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public RecClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.toggleMacroRecording();
            }
        }
    }

    public static class ReloadClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public ReloadClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null || fusionWebViewManager.mActiveWebView == null) {
                return;
            }
            this.mManager.mActiveWebView.reload();
        }
    }

    public static class SwitchChainClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public SwitchChainClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.showChainSwitcherDialog();
            }
        }
    }

    public static class SwitchWalletClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public SwitchWalletClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.showWalletSwitcherDialog();
            }
        }
    }

    public static class SyncDbClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public SyncDbClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.syncDbManual();
            }
        }
    }

    public static class TouchListener implements View.OnTouchListener {
        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view == null) {
                return false;
            }
            ViewParent parent = view.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            if (motionEvent == null || motionEvent.getAction() != 0) {
                return false;
            }
            if (!view.hasFocus()) {
                view.requestFocus();
                FusionWebViewManager mgr = FusionWebViewManager.sInstance;
                if (mgr != null && mgr.mActivity != null) {
                    mgr.mActivity.beginWebViewExternalTextInput();
                }
            }
            return false;
        }
    }

    public static class WalletSelectListener implements DialogInterface.OnClickListener {
        public final FusionWebViewManager mManager;

        public WalletSelectListener(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.onWalletSelected(i);
            }
        }
    }

    public static class WcClick implements View.OnClickListener {
        public final FusionWebViewManager mManager;

        public WcClick(FusionWebViewManager fusionWebViewManager) {
            this.mManager = fusionWebViewManager;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager != null) {
                fusionWebViewManager.showWcDialog();
            }
        }
    }

    public FusionWebViewManager(TermuxActivity termuxActivity) {
        this.mActivity = termuxActivity;
    }

    public static FusionWebViewManager getInstance() {
        return sInstance;
    }

    public static void init(TermuxActivity termuxActivity) {
        FusionWebViewManager fusionWebViewManager = sInstance;
        if (fusionWebViewManager == null) {
            sInstance = new FusionWebViewManager(termuxActivity);
        } else {
            boolean activityChanged = (fusionWebViewManager.mActivity != termuxActivity);
            fusionWebViewManager.mActivity = termuxActivity;
            if (activityChanged && termuxActivity != null) {
                fusionWebViewManager.mViewsInitialized = false;
                fusionWebViewManager.mPaneHost = null;
                fusionWebViewManager.mTerminalPaneHost = null;
                termuxActivity.runOnUiThread(fusionWebViewManager::initViews);
            }
        }
    }

    public void addNewTab(String str) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionTabAddRunnable(this, str));
    }

    public void addNewTabDirect(String str) {
        initViews();
        FrameLayout frameLayout = this.mPaneHost;
        if (frameLayout != null) {
            frameLayout.setVisibility(0);
            frameLayout.setAlpha(1.0f);
            frameLayout.setTranslationX(0.0f);
            frameLayout.bringToFront();
        }
        if (str == null || str.length() == 0 || "home".equalsIgnoreCase(str) || "about:home".equalsIgnoreCase(str) || "about:newtab".equalsIgnoreCase(str)) {
            str = "file:///android_asset/webview_engine/home.html";
        }
        FusionTab fusionTabCreateTabInternal = createTabInternal(str, "default", "", "base", true, false, false, "");
        switchTab(this.mTabList.size() - 1);
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(0);
            linearLayout.setElevation(160.0f);
            linearLayout.bringToFront();
        }
        if (this.mToolScroll != null) {
            this.mToolScroll.setVisibility(0);
            this.mToolScroll.setElevation(160.0f);
            this.mToolScroll.bringToFront();
            FusionToolPillHelper.syncToolPillPosition(this);
            FusionToolPillHelper.updateToolPillUi(this);
        }
        if (fusionTabCreateTabInternal != null && fusionTabCreateTabInternal.mWebView != null) {
            fusionTabCreateTabInternal.mWebView.loadUrl(str);
        }
        updateContextUi();
    }

    public boolean checkRpcDaemon() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 3333), 200);
            socket.close();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public void clearWebviewCache() {
        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearHistory();
            webView.evaluateJavascript("(function(){ try { var sPreserved={}; for(var i=0;i<sessionStorage.length;i++){ var k=sessionStorage.key(i); if(k&&(k.startsWith('__agy_')||k.startsWith('__fusion_'))) sPreserved[k]=sessionStorage.getItem(k); } sessionStorage.clear(); for(var pk in sPreserved) sessionStorage.setItem(pk, sPreserved[pk]); var lPreserved={}; for(var j=0;j<localStorage.length;j++){ var lk=localStorage.key(j); if(lk&&(lk.startsWith('__agy_')||lk.startsWith('__fusion_')||lk.startsWith('fusion_')||lk.startsWith('agy_'))) lPreserved[lk]=localStorage.getItem(lk); } localStorage.clear(); for(var plk in lPreserved) localStorage.setItem(plk, lPreserved[plk]); } catch(e){} })();", null);
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Cache Tab Aktif Berhasil Dibersihkan!", 0).show();
        }
    }

    public String switchToNextWallet() {
        String[] names = FusionWalletHelper.getNames();
        if (names == null || names.length == 0) return "";
        int curIdx = FusionWalletHelper.findIndexByName(this.mCurrentWalletName);
        int nextIdx = (curIdx + 1) % names.length;
        onWalletSelected(nextIdx);

        final String nextName = this.mCurrentWalletName;
        final AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            appCompatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateContextUi();
                    FusionToast.makeText(appCompatActivity, "Dompet Diubah ke: " + nextName, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return nextName;
    }

    public void reloadActiveTab() {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            appCompatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActiveWebView != null) {
                        mActiveWebView.reload();
                    }
                }
            });
        }
    }

    public String rotateDeviceFingerprint() {
        if (mActiveWebView != null) {
            String randProf = "fp_" + (System.currentTimeMillis() % 100000);
            FusionProfileManager.DeviceProfile dev = FusionProfileManager.getDeviceForProfile(randProf);
            AppCompatActivity appCompatActivity = this.mActivity;
            if (appCompatActivity != null) {
                appCompatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActiveWebView != null) {
                            mActiveWebView.getSettings().setUserAgentString(FusionProfileManager.getEffectiveUserAgent(dev, false));
                            mActiveWebView.evaluateJavascript(
                                "window.__FUSION_DEVICE__ = " + dev.toJsonString() + ";" +
                                "if(window.__applyFingerprintFaker) window.__applyFingerprintFaker();", null
                            );
                            FusionToast.makeText(appCompatActivity, "Fingerprint: " + dev.name, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            return dev.name;
        }
        return "";
    }

    public void closeAllDrawers() {
        FusionDrawerHelper.closeAllDrawers(this);
    }

    public void closeWebView() {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new CloseRunnable(this));
    }

    /* JADX WARN: Type inference failed for: r0v7, types: [android.app.Activity, com.termux.app.TermuxActivity] */
    public void closeWebViewInternal() {
        ArrayList arrayList = this.mTabList;
        if (arrayList != null && arrayList.size() > 0) {
            killActiveTab();
            return;
        }
        this.mBoundSession = null;
        LinearLayout linearLayout = this.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        View view = this.mMenuBackdrop;
        if (view != null) {
            view.setVisibility(8);
        }
        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.setVisibility(8);
            FrameLayout frameLayout = this.mPaneHost;
            if (frameLayout != null) {
                frameLayout.removeView(webView);
            }
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.stopLoading();
            webView.destroy();
            this.mActiveWebView = null;
        }
        WebView webView2 = this.mWorkerWebView;
        if (webView2 != null) {
            webView2.setVisibility(8);
            FrameLayout frameLayout2 = this.mPaneHost;
            if (frameLayout2 != null) {
                frameLayout2.removeView(webView2);
            }
            webView2.setWebViewClient(null);
            webView2.setWebChromeClient(null);
            webView2.stopLoading();
            webView2.destroy();
            this.mWorkerWebView = null;
        }
        this.mActiveTab = 0;
        System.gc();
        LinearLayout linearLayout2 = this.mControlPill;
        if (linearLayout2 != null) {
            linearLayout2.setVisibility(8);
        }
        if (this.mToolScroll != null) {
            this.mToolScroll.setVisibility(8);
        }
        FusionDrawerHelper.closeAllDrawers(this);
        FusionRecPillHelper.hideRecPill();
        FusionToolPillHelper.hideToolPill(this);
        if (this.mPaneHost != null) {
            this.mPaneHost.setVisibility(View.GONE);
        }
        View view2 = this.mTerminalPaneHost;
        if (view2 != null) {
            view2.setVisibility(0);
        }
        TermuxActivity r0 = this.mActivity;
        if (r0 != null) {
            r0.endWebViewExternalTextInput();
            r0.refreshTerminalWindowBar();
        }
    }

    public void copyAddressToClipboard(String str) {
        TermuxActivity r0 = this.mActivity;
        if (r0 == null) {
            return;
        }
        try {
            ((ClipboardManager) r0.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("wallet_address", str));
            FusionToast.makeText((Context) r0, "Alamat disalin: " + str, 0).show();
        } catch (Exception unused) {
        }
    }

    public WebView createNewConfiguredWebView(String profileName, boolean isDesktop) {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return null;
        }
        WebView webView = new WebView(appCompatActivity);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(0);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (isDesktop) {
            settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
        }
        try {
            android.webkit.CookieManager globalCm = android.webkit.CookieManager.getInstance();
            globalCm.setAcceptCookie(true);
            globalCm.setAcceptThirdPartyCookies(webView, true);
        } catch (Exception ignored) {}
        FusionProfileManager.setupProfileForWebView(webView, profileName, isDesktop);
        applyThemeToWebView(webView, appCompatActivity);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setClickable(true);
        webView.setEnabled(true);
        webView.setOnTouchListener(new TouchListener());
        webView.setWebChromeClient(new FusionWebChromeClient());
        webView.setWebViewClient(new FusionWebViewClient(this));
        webView.addJavascriptInterface(new GhostBridgeInterface(), "GhostBridge");
        webView.addJavascriptInterface(new FusionExtensionHelper.FusionExtensionBridge((this.mActivity != null) ? this.mActivity : webView.getContext(), webView), "__FusionExtensionBridge__");
        FusionExtensionHelper.setupWebView(webView);
        return webView;
    }

    /**
     * Menerapkan tema gelap / terang sistem Android secara dinamis ke WebView.
     * Mengatur background solid Catppuccin, FORCE_DARK, FORCE_DARK_STRATEGY, dan Algorithmic Darkening.
     */
    public static void applyThemeToWebView(WebView webView, Context context) {
        if (webView == null || context == null) return;
        try {
            boolean isNight = false;
            if (context.getResources() != null && context.getResources().getConfiguration() != null) {
                int nightMask = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                isNight = (nightMask == Configuration.UI_MODE_NIGHT_YES);
            }

            webView.setBackgroundColor(isNight ? 0xFF1E1E2E : 0xFFFFFFFF);

            WebSettings settings = webView.getSettings();
            if (settings != null) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(settings, isNight ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
                }
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                    WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    settings.setAlgorithmicDarkeningAllowed(isNight);
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isNight);
                }
            }
        } catch (Throwable t) {
            Log.w("FusionWebViewManager", "Gagal menerapkan tema pada WebView: " + t.getMessage());
        }
    }

    /**
     * Sinkronisasi tema seluruh tab dan WebView yang sedang aktif.
     */
    public void applyThemeToAllTabs() {
        if (this.mActivity == null) return;
        this.mActivity.runOnUiThread(() -> {
            if (this.mTabList != null) {
                for (int i = 0; i < this.mTabList.size(); i++) {
                    Object item = this.mTabList.get(i);
                    if (item instanceof FusionTab) {
                        FusionTab tab = (FusionTab) item;
                        if (tab.mWebView != null) {
                            applyThemeToWebView(tab.mWebView, this.mActivity);
                        }
                    }
                }
            }
            if (this.mActiveWebView != null) {
                applyThemeToWebView(this.mActiveWebView, this.mActivity);
            }
        });
    }

    public FusionTab createTabInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return null;
        }
        WebView webView = createNewConfiguredWebView(str2, z2);
        if (webView == null) {
            return null;
        }
        FrameLayout frameLayout = this.mPaneHost;
        if (frameLayout != null) {
            frameLayout.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        }
        webView.setElevation(0.0f);
        int i = this.mNextTabId;
        this.mNextTabId = i + 1;
        FusionTab fusionTab = new FusionTab(i, str, str2, webView, z, z2, z3);
        if (webView != null && webView.getWebViewClient() instanceof FusionWebViewClient) {
            ((FusionWebViewClient) webView.getWebViewClient()).setCurrentUrl(str);
        }
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity != null) {
            fusionTab.mBoundSession = termuxActivity.getCurrentSession();
        }
        fusionTab.mIsProxyEnabled = false;
        fusionTab.mProxyItem = null;
        fusionTab.mAutoRotateIntervalSec = 0;

        if (this.mActivity != null) {
            String pKey = (str2 != null && !str2.isEmpty()) ? str2 : "default";
            FusionProxyHelper.PersistentProxyState saved = FusionProxyHelper.loadPersistentState(this.mActivity, pKey);
            if (saved != null && saved.enabled && saved.item != null && saved.item.isValid()) {
                fusionTab.mProxyItem = saved.item;
                fusionTab.mIsProxyEnabled = true;
                fusionTab.mAutoRotateIntervalSec = saved.intervalSec;
                fusionTab.mLastProxyRotatedTime = System.currentTimeMillis();
            }
        }
        this.mTabList.add(fusionTab);
        return fusionTab;
    }

    public void cycleOpacity() {
        FusionTabStateHelper.cycleOpacity(this);
    }

    public void evalOnActiveTab(String str) {
        WebView webView = this.mActiveWebView;
        if (webView == null) {
            return;
        }
        webView.evaluateJavascript(str, null);
    }

    public void evaluateJs(String str) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new EvalRunnable(this, str));
    }

    public void executeAgyAction(int i) {
        if (i == 0) {
            evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showRecipePicker){window.AgyBridge.showRecipePicker();}else{window.__FUSION_PLAY_MACRO__&&window.__FUSION_PLAY_MACRO__();}");
            return;
        }
        if (i == 1) {
            evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.autoFillForm){window.AgyBridge.autoFillForm();}else if(window.AgyBridge && window.AgyBridge.autoFill){window.AgyBridge.autoFill();}");
            return;
        }
        if (i == 2) {
            evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showDomInspector){window.AgyBridge.showDomInspector();}else{window.__AI_DEVBRIDGE_INSPECT__&&window.__AI_DEVBRIDGE_INSPECT__();}");
            return;
        }
        if (i == 3) {
            toggleMacroRecording();
            evalOnActiveTab("if(!window.__FUSION_RECORDING__) { window.AgyBridge && window.AgyBridge.saveCurrentRecipeAs && window.AgyBridge.saveCurrentRecipeAs(); }");
            return;
        }
        if (i == 4) {
            evalOnActiveTab("window.AgyBridge && window.AgyBridge.showRecipePicker && window.AgyBridge.showRecipePicker();");
            return;
        }
        if (i == 5) {
            toggleDeveloperTools();
            return;
        }
        if (i == 6) {
            evalOnActiveTab("window.AgyBridge && window.AgyBridge.showRecipePicker && window.AgyBridge.showRecipePicker();");
        } else if (i == 7) {
            evalOnActiveTab("window.AgyBridge && window.AgyBridge.clearRecipes && window.AgyBridge.clearRecipes();");
        } else if (i == 8) {
            evalOnActiveTab("window.AgyBridge && window.AgyBridge.showDomInspector && window.AgyBridge.showDomInspector();");
        }
    }

    public void toggleDeveloperTools() {
        toggleDevMode();
        evalOnActiveTab("(function(){"
            + "if(window.eruda){"
            + "  if(!window.eruda._isInit){try{window.eruda.init();}catch(e){}}"
            + "  try{window.eruda.show();}catch(e){}"
            + "  return;"
            + "}"
            + "var s=document.createElement('script');"
            + "s.src='https://cdn.jsdelivr.net/npm/eruda';"
            + "s.onload=function(){"
            + "  try{"
            + "    eruda.init();"
            + "    eruda.show();"
            + "    if(window.AgyBridge){"
            + "      eruda.add({"
            + "        name:'antigravity',"
            + "        init:function($el){"
            + "          $el.html('<div style=\"padding:12px;background:#101418;color:#00ffaa;font-family:monospace;font-size:12px;\">'+"
            + "            '<h3 style=\"margin:0 0 8px 0;color:#60a5fa;\">🪐 Antigravity Co-Pilot</h3>'+"
            + "            '<p style=\"color:#a0aec0;margin:0 0 10px 0;\">Status: Connected to Termux IPC</p>'+"
            + "            '<button id=\"btn-dom-ai\" style=\"background:#2563eb;color:#fff;border:none;padding:6px 12px;border-radius:4px;cursor:pointer;\">🔍 Inspect DOM</button>'+"
            + "            '</div>');"
            + "          $el.find('#btn-dom-ai').on('click', function(){"
            + "            if(window.AgyBridge && window.AgyBridge.showDomInspector){ window.AgyBridge.showDomInspector(); }"
            + "          });"
            + "        }"
            + "      });"
            + "    }"
            + "  }catch(e){}"
            + "};"
            + "document.body.appendChild(s);"
            + "})();");
    }

    public void executeAgyPrompt(String str) {
        if (str == null) {
            return;
        }
        evalOnActiveTab("window.AgyBridge && window.AgyBridge.executeNaturalPrompt && window.AgyBridge.executeNaturalPrompt(" + JSONObject.quote(str) + ");");
    }

    public void hideOverlayForDeparture() {
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        if (this.mToolScroll != null) {
            this.mToolScroll.setVisibility(8);
        }
        FusionToolPillHelper.hideToolPill(this);
        LinearLayout linearLayout2 = this.mMenuCard;
        if (linearLayout2 != null) {
            linearLayout2.setVisibility(8);
        }
        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.setVisibility(8);
        }
    }

    public void onTerminalWallOffsetChanged(float translationX, float alpha) {
        if (this.mPaneHost != null) {
            this.mPaneHost.setTranslationX(translationX);
            this.mPaneHost.setAlpha(alpha);
            boolean onScreen = alpha > 0.05f;
            if (!onScreen) {
                if (this.mPaneHost.getVisibility() == View.VISIBLE) {
                    this.mPaneHost.setVisibility(View.GONE);
                }
            } else if (hasMiddleCanvasWebView() && this.mPaneHost.getVisibility() != View.VISIBLE) {
                this.mPaneHost.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onTerminalOffScreenChanged(boolean offScreen) {
        if (this.mPaneHost != null) {
            if (offScreen) {
                this.mPaneHost.setVisibility(View.GONE);
                if (this.mActivity != null) {
                    this.mActivity.endWebViewExternalTextInput();
                }
            } else if (hasMiddleCanvasWebView()) {
                this.mPaneHost.setVisibility(View.VISIBLE);
                if (isMiddleCanvasWebViewActive() && this.mActivity != null) {
                    this.mActivity.beginWebViewExternalTextInput();
                }
            }
        }
    }

    public void onWallPageChanged(@NonNull com.termux.app.wall.PaneWallPage page) {
        if (page != com.termux.app.wall.PaneWallPage.TERMINAL) {
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.GONE);
            }
            if (this.mActivity != null) {
                this.mActivity.endWebViewExternalTextInput();
            }
        }
    }

    public void onWallPageSettled(@NonNull com.termux.app.wall.PaneWallPage page) {
        if (page != com.termux.app.wall.PaneWallPage.TERMINAL) {
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.GONE);
                this.mPaneHost.setTranslationX(0f);
            }
            if (this.mActivity != null) {
                this.mActivity.endWebViewExternalTextInput();
            }
        } else {
            if (this.mPaneHost != null) {
                this.mPaneHost.setTranslationX(0f);
                this.mPaneHost.setAlpha(1f);
            }
            syncVisibility();
        }
    }

    public boolean isMiddleCanvasWebViewActive() {
        if (this.mActivity == null) return false;
        TerminalSession session = this.mActivity.getCurrentSession();
        if (session == null || this.mTabList == null) return false;
        for (int i = 0; i < this.mTabList.size(); i++) {
            FusionTab tab = (FusionTab) this.mTabList.get(i);
            if (tab != null && tab.mBoundSession == session && !tab.mIsMinimized && tab.mWebView != null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMiddleCanvasWebView() {
        if (this.mActivity == null) return false;
        TerminalSession session = this.mActivity.getCurrentSession();
        if (session == null || this.mTabList == null) return false;
        for (int i = 0; i < this.mTabList.size(); i++) {
            FusionTab tab = (FusionTab) this.mTabList.get(i);
            if (tab != null && (tab.mBoundSession == session || tab.mBoundSession == null) && tab.mWebView != null) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.content.Context, com.termux.app.TermuxActivity] */
    /* JADX WARN: Type inference failed for: r7v1 */
    public void initViews() {
        TermuxActivity r0 = this.mActivity;
        if (r0 == null) {
            return;
        }
        if (this.mViewsInitialized && this.mPaneHost != null && this.mPaneHost.getContext() == r0) {
            return;
        }
        ViewParent parent;
        this.mViewsInitialized = true;
        FusionWalletHelper.init(r0);
        FusionChainHelper.init(r0);
        String mName = FusionWalletHelper.getMasterName();
        String mAddr = FusionWalletHelper.getMasterAddress();
        if (mAddr != null && !mAddr.isEmpty()) {
            this.mCurrentWalletName = mName;
            this.mCurrentWalletAddress = mAddr;
            GhostBridgeInterface.setWallet(mName, mAddr);
        }
        String mSolName = FusionWalletHelper.getMasterSolName();
        String mSolAddr = FusionWalletHelper.getMasterSolAddress();
        if (mSolAddr != null && !mSolAddr.isEmpty()) {
            this.mCurrentSolWalletName = mSolName;
            this.mCurrentSolWalletAddress = mSolAddr;
            GhostBridgeInterface.setSolWallet(mSolName, mSolAddr);
        }
        String mTronName = FusionWalletHelper.getMasterTronName();
        String mTronAddr = FusionWalletHelper.getMasterTronAddress();
        if (mTronAddr != null && !mTronAddr.isEmpty()) {
            this.mCurrentTronWalletName = mTronName;
            this.mCurrentTronWalletAddress = mTronAddr;
            GhostBridgeInterface.setTronWallet(mTronName, mTronAddr);
        }
        String mSuiName = FusionWalletHelper.getMasterSuiName();
        String mSuiAddr = FusionWalletHelper.getMasterSuiAddress();
        if (mSuiAddr != null && !mSuiAddr.isEmpty()) {
            this.mCurrentSuiWalletName = mSuiName;
            this.mCurrentSuiWalletAddress = mSuiAddr;
            GhostBridgeInterface.setSuiWallet(mSuiName, mSuiAddr);
        }
        String mAptosName = FusionWalletHelper.getMasterAptosName();
        String mAptosAddr = FusionWalletHelper.getMasterAptosAddress();
        if (mAptosAddr != null && !mAptosAddr.isEmpty()) {
            this.mCurrentAptosWalletName = mAptosName;
            this.mCurrentAptosWalletAddress = mAptosAddr;
            GhostBridgeInterface.setAptosWallet(mAptosName, mAptosAddr);
        }
        String mIotaName = FusionWalletHelper.getMasterIotaName();
        String mIotaAddr = FusionWalletHelper.getMasterIotaAddress();
        if (mIotaAddr != null && !mIotaAddr.isEmpty()) {
            this.mCurrentIotaWalletName = mIotaName;
            this.mCurrentIotaWalletAddress = mIotaAddr;
            GhostBridgeInterface.setIotaWallet(mIotaName, mIotaAddr);
        }
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            WebView.setWebContentsDebuggingEnabled(true);
        } catch (Exception unused) {
        }
        this.mPaneHost = (FrameLayout) r0.findViewById(com.termux.R.id.terminal_webview_overlay_host);
        if (this.mPaneHost == null) {
            this.mPaneHost = (FrameLayout) r0.findViewById(com.termux.R.id.terminal_surface_host);
        }
        if (this.mPaneHost == null) {
            this.mPaneHost = (FrameLayout) this.mActivity.findViewById(android.R.id.content);
        }
        View viewFindViewById = this.mActivity.findViewById(com.termux.R.id.terminal_pane_host);
        this.mTerminalPaneHost = viewFindViewById;
        if (viewFindViewById != null) {
            viewFindViewById.setLayerType(1, null);
        }
        FrameLayout frameLayout = this.mPaneHost;
        if (frameLayout != null) {
            frameLayout.setPadding(0, 0, 0, 0);
            frameLayout.setAlpha(1.0f);
            frameLayout.setTranslationX(0.0f);
            frameLayout.bringToFront();
        }

        // Re-parent active webviews if needed
        if (this.mPaneHost != null && this.mTabList != null) {
            for (int ti = 0; ti < this.mTabList.size(); ti++) {
                FusionTab tab = (FusionTab) this.mTabList.get(ti);
                if (tab != null && tab.mWebView != null) {
                    ViewParent p = tab.mWebView.getParent();
                    if (p != this.mPaneHost) {
                        if (p instanceof ViewGroup) {
                            ((ViewGroup) p).removeView(tab.mWebView);
                        }
                        this.mPaneHost.addView(tab.mWebView, new FrameLayout.LayoutParams(-1, -1));
                    }
                }
            }
        }
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            FrameLayout frameLayout2 = this.mPaneHost;
            if (frameLayout2 != null && (parent = linearLayout.getParent()) != frameLayout2) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(linearLayout);
                }
                frameLayout2.addView(linearLayout, linearLayout.getLayoutParams());
            }
            if (this.mToolScroll != null) {
                FrameLayout frameLayoutTool = this.mPaneHost;
                if (frameLayoutTool != null && (parent = this.mToolScroll.getParent()) != frameLayoutTool) {
                    if (parent != null) {
                        ((ViewGroup) parent).removeView(this.mToolScroll);
                    }
                    frameLayoutTool.addView(this.mToolScroll, this.mToolScroll.getLayoutParams());
                }
            } else {
                FusionToolPillHelper.initToolPill(this);
            }
        } else {
            LinearLayout linearLayout2 = new LinearLayout(this.mActivity);
            this.mControlPill = linearLayout2;
            linearLayout2.setLayerType(1, null);
            LinearLayout linearLayout3 = this.mControlPill;
            linearLayout3.setOrientation(0);
            FusionUiHelper.setSolidBox(linearLayout3);
            linearLayout3.setPadding(6, 4, 6, 4);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
            layoutParams.setMargins(0, 8, 10, 0);
            TextView textView = new TextView(this.mActivity);
            this.mQuickBackBtn = textView;
            textView.setText(" BACK ");
            FusionUiHelper.setSolidBtn(textView);
            textView.setTextSize(2, 11.0f);
            textView.setPadding(8, 4, 8, 4);
            textView.setClickable(true);
            textView.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView, layoutParams2);
            TextView textView2 = new TextView(this.mActivity);
            this.mTabBtn = textView2;
            textView2.setText(" TAB 1 ");
            FusionUiHelper.setSolidBtn(textView2);
            textView2.setTextSize(2, 11.0f);
            textView2.setPadding(8, 4, 8, 4);
            textView2.setClickable(true);
            textView2.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams3.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView2, layoutParams3);
            TextView proxyBtn = new TextView(this.mActivity);
            this.mProxyBtn = proxyBtn;
            proxyBtn.setText(" PROXY : OFF ");
            FusionUiHelper.setSolidBtn(proxyBtn);
            proxyBtn.setTextSize(2, 11.0f);
            proxyBtn.setPadding(8, 4, 8, 4);
            proxyBtn.setClickable(true);
            proxyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FusionProxyHelper.showProxyDialog(FusionWebViewManager.this.mActivity, FusionWebViewManager.this);
                }
            });
            LinearLayout.LayoutParams layoutParamsProxy = new LinearLayout.LayoutParams(-2, -2);
            layoutParamsProxy.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(proxyBtn, layoutParamsProxy);
            TextView textView3 = new TextView(this.mActivity);
            this.mAgyBtn = textView3;
            textView3.setText(" BOT ");
            FusionUiHelper.setSolidBtn(textView3);
            textView3.setTextSize(2, 11.0f);
            textView3.setPadding(8, 4, 8, 4);
            textView3.setClickable(true);
            textView3.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams4.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView3, layoutParams4);
            TextView textView4 = new TextView(this.mActivity);
            this.mBurgerBtn = textView4;
            textView4.setText(" MENU ");
            FusionUiHelper.setSolidBtn(textView4);
            textView4.setTextSize(2, 11.0f);
            textView4.setPadding(8, 4, 8, 4);
            textView4.setClickable(true);
            textView4.setOnClickListener(new BurgerClick(this));
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams5.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView4, layoutParams5);
            TextView textView5 = new TextView(this.mActivity);
            this.mMinPillBtn = textView5;
            textView5.setText(" MIN ");
            FusionUiHelper.setSolidBtn(textView5);
            textView5.setTextSize(2, 11.0f);
            textView5.setPadding(8, 4, 8, 4);
            textView5.setClickable(true);
            textView5.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams6.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView5, layoutParams6);
            TextView textView6 = new TextView(this.mActivity);
            this.mCollapsePillBtn = textView6;
            textView6.setText(" > ");
            FusionUiHelper.setSolidBtn(textView6);
            textView6.setTextSize(2, 11.0f);
            textView6.setPadding(8, 4, 8, 4);
            textView6.setClickable(true);
            textView6.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams7.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(textView6, layoutParams7);
            this.mControlPill.setElevation(160.0f);
            FusionToolPillHelper.initToolPill(this);
            boolean z = true;
            LinearLayout linearLayout4 = new LinearLayout(this.mActivity);
            this.mMenuCard = linearLayout4;
            linearLayout4.setLayerType(1, null);
            linearLayout4.setClickable(true);
            linearLayout4.setFocusable(true);
            linearLayout4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Prevent touch bleeding to backdrop underneath
                }
            });
            LinearLayout linearLayout5 = this.mMenuCard;
            linearLayout5.setOrientation(1);
            linearLayout5.setBackgroundColor(-300016092);
            linearLayout5.setPadding(16, 16, 16, 16);
            FrameLayout.LayoutParams layoutParams8 = new FrameLayout.LayoutParams(FusionUiHelper.getDrawerWidth(this.mActivity), -1, 8388659);
            layoutParams8.setMargins(0, 0, 0, 0);
            this.mMenuCard.setElevation(170.0f);
            this.mMenuCard.setBackground(FusionUiHelper.createRounded(-15198171, -13548988, 0.0f));
            View view = new View(this.mActivity);
            this.mMenuBackdrop = view;
            view.setVisibility(8);
            view.setElevation(165.0f);
            view.setBackgroundColor(0);
            view.setOnClickListener(new FusionMenuBackdropClick(this));
            FrameLayout frameLayout3 = this.mPaneHost;
            if (frameLayout3 != null) {
                frameLayout3.addView(view, new FrameLayout.LayoutParams(-1, -1));
            }
            LinearLayout headerRow = new LinearLayout(this.mActivity);
            headerRow.setOrientation(0);
            headerRow.setPadding(4, 4, 4, 12);
            headerRow.setGravity(19);

            TextView titleText = new TextView(this.mActivity);
            titleText.setText("FUSION BROWSER");
            titleText.setTextColor(0xFFCDD6F4);
            titleText.setTextSize(2, 14.5f);
            titleText.setTypeface(null, 1);
            headerRow.addView(titleText, new LinearLayout.LayoutParams(0, -2, 1.0f));

            TextView closeBtn = new TextView(this.mActivity);
            closeBtn.setText("  ✕  ");
            closeBtn.setTextColor(0xFFF38BA8);
            closeBtn.setTextSize(2, 15.0f);
            closeBtn.setTypeface(null, 1);
            closeBtn.setPadding(10, 6, 10, 6);
            closeBtn.setClickable(true);
            closeBtn.setOnClickListener(new CloseMenuClick(this));
            headerRow.addView(closeBtn);
            this.mMenuCard.addView(headerRow);

            // ==================== BARIS TOP QUICK ACTION (KIWI / CHROME STYLE) ====================
            LinearLayout quickActionRow = new LinearLayout(this.mActivity);
            quickActionRow.setOrientation(0);
            quickActionRow.setPadding(0, 0, 0, 10);

            // 1. Forward (➡️)
            TextView fwdBtn = FusionUiHelper.createQuickActionBtn(this.mActivity, "➡️", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performQuickForward();
                }
            });
            quickActionRow.addView(fwdBtn);

            // 2. Bookmark (⭐)
            TextView starBtn = FusionUiHelper.createQuickActionBtn(this.mActivity, "⭐", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleBookmarkCurrentPage();
                }
            });
            quickActionRow.addView(starBtn);

            // 3. Save Session (💾)
            TextView saveBtn = FusionUiHelper.createQuickActionBtn(this.mActivity, "💾", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCurrentSessionManual();
                }
            });
            quickActionRow.addView(saveBtn);

            // 4. Site Info (ℹ️)
            TextView infoBtn = FusionUiHelper.createQuickActionBtn(this.mActivity, "ℹ️", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSiteInfoDialog();
                }
            });
            quickActionRow.addView(infoBtn);

            // 5. Reload (🔄)
            TextView reloadBtn = FusionUiHelper.createQuickActionBtn(this.mActivity, "🔄", new ReloadClick(this));
            quickActionRow.addView(reloadBtn);

            this.mMenuCard.addView(quickActionRow);

            ScrollView menuScrollView = new ScrollView(this.mActivity);
            menuScrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
            menuScrollView.setVerticalScrollBarEnabled(false);
            LinearLayout menuContent = new LinearLayout(this.mActivity);
            menuContent.setOrientation(1);

            // ==================== SECTION 1: NAVIGASI & TAB ====================
            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "➕", "Tab baru", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllDrawers();
                    promptNewTabUrlDirect();
                }
            }));

            TextView workerBtn = new TextView(this.mActivity);
            this.mNewWorkerBtn = workerBtn;
            workerBtn.setText("Tab Samaran (Worker)");
            workerBtn.setTextColor(0xFFCDD6F4);
            workerBtn.setTextSize(2, 13.5f);
            workerBtn.setGravity(19);
            workerBtn.setClickable(true);
            workerBtn.setOnClickListener(this);
            menuContent.addView(FusionUiHelper.createMenuRowWithCustomText(this.mActivity, "🕵️", workerBtn, null));

            LinearLayout ctxRow = new LinearLayout(this.mActivity);
            ctxRow.setOrientation(0);
            ctxRow.setPadding(12, 4, 12, 4);
            ctxRow.setGravity(19);

            TextView ctxText = new TextView(this.mActivity);
            this.mContextStatusText = ctxText;
            ctxText.setText("Konteks: 1 Aktif (shaqilla)");
            ctxText.setTextColor(0xFF777DA7);
            ctxText.setTextSize(2, 11.0f);
            ctxRow.addView(ctxText, new LinearLayout.LayoutParams(0, -2, 1.0f));

            TextView killBtn = new TextView(this.mActivity);
            this.mKillWorkerBtn = killBtn;
            killBtn.setText("Tutup Worker");
            killBtn.setTextColor(0xFFF38BA8);
            killBtn.setBackground(FusionUiHelper.createRounded(0x33F38BA8, 0xFFF38BA8, 4.0f));
            killBtn.setTextSize(2, 10.5f);
            killBtn.setPadding(8, 4, 8, 4);
            killBtn.setClickable(true);
            killBtn.setOnClickListener(this);
            ctxRow.addView(killBtn);
            menuContent.addView(ctxRow);

            menuContent.addView(FusionUiHelper.createSectionDivider(this.mActivity));

            // ==================== SECTION 2: ALAT BROWSER & EKSTENSI ====================
            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🕒", "Histori", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHistoryDialog();
                }
            }));

            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🧩", "Ekstensi (.zip/.crx & Script)", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllDrawers();
                    showExtensionsDialog();
                }
            }));

            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🌐", "Pengaturan Proxy (Per-Tab)", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllDrawers();
                    showProxyDialog();
                }
            }));

            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "📁", "Bookmark", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBookmarksDialog();
                }
            }));

            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🗂️", "Kelola Tab (Tab Manager)", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTabManagerDialogDirect();
                }
            }));

            menuContent.addView(FusionUiHelper.createSectionDivider(this.mActivity));

            // ==================== SECTION 3: FUSION WEB3 ENGINE & OTOMASI ====================
            menuContent.addView(FusionUiHelper.createSectionHeader(this.mActivity, "FUSION WEB3 & WALLET"));

            LinearLayout web3Card = new LinearLayout(this.mActivity);
            web3Card.setOrientation(1);
            web3Card.setBackground(FusionUiHelper.createRounded(-15724528, -13548988, 8.0f)); // #11111b, border #313244
            web3Card.setPadding(12, 10, 12, 10);
            LinearLayout.LayoutParams lpWeb3 = new LinearLayout.LayoutParams(-1, -2);
            lpWeb3.setMargins(0, 4, 0, 8);
            web3Card.setLayoutParams(lpWeb3);

            // Baris 1: Status RPC + Tombol Start Daemon
            LinearLayout rpcRow = new LinearLayout(this.mActivity);
            rpcRow.setOrientation(0);
            rpcRow.setGravity(19);

            TextView rpcStatus = new TextView(this.mActivity);
            this.mStatusText = rpcStatus;
            rpcStatus.setText("* RPC: 127.0.0.1:3333");
            rpcStatus.setTextColor(-5592406);
            rpcStatus.setTextSize(2, 11.0f);
            rpcStatus.setClickable(true);
            rpcStatus.setOnClickListener(this);
            rpcRow.addView(rpcStatus, new LinearLayout.LayoutParams(0, -2, 1.0f));

            TextView startDaemon = new TextView(this.mActivity);
            this.mStartDaemonBtn = startDaemon;
            startDaemon.setText("[Start Wallet (" + FusionWalletHelper.getDetectedEngine() + ")]");
            startDaemon.setBackground(FusionUiHelper.createRounded(-12285730, -12762806, 4.0f));
            startDaemon.setTextColor(-1);
            startDaemon.setTextSize(2, 11.0f);
            startDaemon.setPadding(8, 6, 8, 6);
            startDaemon.setClickable(true);
            startDaemon.setOnClickListener(this);
            rpcRow.addView(startDaemon);
            web3Card.addView(rpcRow);

            // Baris 2: Info Jaringan (Network)
            TextView chainText = new TextView(this.mActivity);
            this.mChainInfoText = chainText;
            chainText.setText("Network: Base (8453) [v]");
            chainText.setTextColor(-2258875);
            chainText.setTextSize(2, 11.0f);
            chainText.setPadding(0, 4, 0, 2);
            chainText.setClickable(true);
            chainText.setOnClickListener(new SwitchChainClick(this));
            web3Card.addView(chainText);

            // Baris 3: Info Dompet (Active Wallet & Address)
            TextView walletText = new TextView(this.mActivity);
            this.mWalletInfoText = walletText;
            walletText.setText("Active: Master\nAddress: 0x87A6...b359");
            walletText.setTextColor(-3355444);
            walletText.setTextSize(2, 11.0f);
            walletText.setPadding(0, 2, 0, 6);
            web3Card.addView(walletText);

            // Baris 4: Tombol Ganti Dompet, Network, Salin Alamat
            LinearLayout walletBtnRow = new LinearLayout(this.mActivity);
            walletBtnRow.setOrientation(0);
            walletBtnRow.setPadding(0, 2, 0, 2);

            TextView switchWallet = new TextView(this.mActivity);
            this.mSwitchWalletBtn = switchWallet;
            switchWallet.setText("Ganti Dompet");
            switchWallet.setBackground(FusionUiHelper.createRounded(-12285730, -12762806, 4.0f));
            switchWallet.setTextColor(-1);
            switchWallet.setTextSize(2, 11.5f);
            switchWallet.setGravity(17);
            switchWallet.setPadding(6, 8, 6, 8);
            switchWallet.setClickable(true);
            switchWallet.setOnClickListener(new SwitchWalletClick(this));
            LinearLayout.LayoutParams lpW1 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpW1.rightMargin = 4;
            walletBtnRow.addView(switchWallet, lpW1);

            TextView switchChain = new TextView(this.mActivity);
            this.mSwitchChainBtn = switchChain;
            switchChain.setText("Network");
            switchChain.setBackground(FusionUiHelper.createRounded(-2258875, -12762806, 4.0f));
            switchChain.setTextColor(-1);
            switchChain.setTextSize(2, 11.5f);
            switchChain.setGravity(17);
            switchChain.setPadding(6, 8, 6, 8);
            switchChain.setClickable(true);
            switchChain.setOnClickListener(new SwitchChainClick(this));
            LinearLayout.LayoutParams lpW2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpW2.rightMargin = 4;
            walletBtnRow.addView(switchChain, lpW2);

            TextView copyAddr = new TextView(this.mActivity);
            this.mCopyAddressBtn = copyAddr;
            copyAddr.setText("Salin Alamat");
            copyAddr.setBackground(FusionUiHelper.createRounded(-14013884, -12762806, 4.0f));
            copyAddr.setTextColor(-1);
            copyAddr.setTextSize(2, 11.5f);
            copyAddr.setGravity(17);
            copyAddr.setPadding(6, 8, 6, 8);
            copyAddr.setClickable(true);
            copyAddr.setOnClickListener(new CopyAddressClick(this));
            walletBtnRow.addView(copyAddr, new LinearLayout.LayoutParams(0, -2, 1.0f));
            web3Card.addView(walletBtnRow);

            // Baris 5: Lokasi DB GhostWallet & Sync DB
            LinearLayout dbBtnRow = new LinearLayout(this.mActivity);
            dbBtnRow.setOrientation(0);
            dbBtnRow.setPadding(0, 4, 0, 0);

            TextView pathBtn = new TextView(this.mActivity);
            pathBtn.setText("Lokasi GhostWallet / DB");
            pathBtn.setBackground(FusionUiHelper.createRounded(-13947597, -12762806, 4.0f));
            pathBtn.setTextColor(-1907994);
            pathBtn.setTextSize(2, 11.0f);
            pathBtn.setGravity(17);
            pathBtn.setPadding(6, 7, 6, 7);
            pathBtn.setClickable(true);
            pathBtn.setOnClickListener(new FusionWalletPathBtnClick(this));
            LinearLayout.LayoutParams lpDb1 = new LinearLayout.LayoutParams(0, -2, 1.2f);
            lpDb1.rightMargin = 4;
            dbBtnRow.addView(pathBtn, lpDb1);

            TextView syncDb = new TextView(this.mActivity);
            syncDb.setText("Sync DB");
            syncDb.setBackground(FusionUiHelper.createRounded(-5605427, -12762806, 4.0f));
            syncDb.setTextColor(-1);
            syncDb.setTextSize(2, 11.0f);
            syncDb.setGravity(17);
            syncDb.setPadding(6, 7, 6, 7);
            syncDb.setClickable(true);
            syncDb.setOnClickListener(new SyncDbClick(this));
            dbBtnRow.addView(syncDb, new LinearLayout.LayoutParams(0, -2, 0.8f));
            web3Card.addView(dbBtnRow);

            menuContent.addView(web3Card);

            // Baris Otomasi Makro
            LinearLayout macroRow = new LinearLayout(this.mActivity);
            macroRow.setOrientation(0);
            macroRow.setPadding(0, 3, 0, 3);

            TextView recBtnView = new TextView(this.mActivity);
            this.mRecBtn = recBtnView;
            recBtnView.setText("Record Macro");
            recBtnView.setBackground(FusionUiHelper.createRounded(-7820595, -12762806, 4.0f));
            recBtnView.setTextColor(-1);
            recBtnView.setTextSize(2, 12.0f);
            recBtnView.setGravity(17);
            recBtnView.setPadding(8, 10, 8, 10);
            recBtnView.setClickable(true);
            recBtnView.setOnClickListener(new RecClick(this));
            LinearLayout.LayoutParams lpM1 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpM1.rightMargin = 6;
            macroRow.addView(recBtnView, lpM1);

            TextView runMacro = new TextView(this.mActivity);
            this.mRunMacroBtn = runMacro;
            runMacro.setText("Play Macro");
            runMacro.setBackground(FusionUiHelper.createRounded(-2254404, -12762806, 4.0f));
            runMacro.setTextColor(-1);
            runMacro.setTextSize(2, 12.0f);
            runMacro.setGravity(17);
            runMacro.setPadding(8, 10, 8, 10);
            runMacro.setClickable(true);
            runMacro.setOnClickListener(this);
            macroRow.addView(runMacro, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(macroRow);

            // Acak FP
            TextView randFp = new TextView(this.mActivity);
            this.mRandomFpBtn = randFp;
            randFp.setText("Acak ID / Anti-Sybil FP");
            randFp.setTextColor(0xFFCDD6F4);
            randFp.setTextSize(2, 13.5f);
            randFp.setGravity(19);
            randFp.setClickable(true);
            randFp.setOnClickListener(this);
            menuContent.addView(FusionUiHelper.createMenuRowWithCustomText(this.mActivity, "🥷", randFp, null));

            // Desktop Mode
            TextView desktopBtnView = new TextView(this.mActivity);
            this.mDesktopBtn = desktopBtnView;
            desktopBtnView.setText(this.mIsDesktopMode ? "Mobile" : "Desktop");
            desktopBtnView.setBackground(FusionUiHelper.createRounded(this.mIsDesktopMode ? 0xFF2E7D32 : -4482645, -12762806, 4.0f));
            desktopBtnView.setTextColor(0xFFFFFFFF);
            desktopBtnView.setTextSize(2, 12.0f);
            desktopBtnView.setGravity(17);
            desktopBtnView.setPadding(8, 10, 8, 10);
            desktopBtnView.setClickable(true);
            desktopBtnView.setOnClickListener(new DesktopClick(this));

            LinearLayout desktopRow = new LinearLayout(this.mActivity);
            desktopRow.setOrientation(0);
            desktopRow.setPadding(0, 3, 0, 3);
            desktopRow.addView(desktopBtnView, new LinearLayout.LayoutParams(-1, -2));
            menuContent.addView(desktopRow);

            // WalletConnect
            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🔗", "Connect WalletConnect (WC)", new WcClick(this)));

            menuContent.addView(FusionUiHelper.createSectionDivider(this.mActivity));

            // ==================== SECTION 4: SISTEM & PENGEMBANG ====================
            TextView opacityBtnView = new TextView(this.mActivity);
            this.mOpacityBtn = opacityBtnView;
            opacityBtnView.setText("Opacity Layar: 100%");
            opacityBtnView.setTextColor(0xFFCDD6F4);
            opacityBtnView.setTextSize(2, 13.5f);
            opacityBtnView.setGravity(19);
            opacityBtnView.setClickable(true);
            opacityBtnView.setOnClickListener(this);
            menuContent.addView(FusionUiHelper.createMenuRowWithCustomText(this.mActivity, "🌙", opacityBtnView, null));

            TextView devModeBtnView = new TextView(this.mActivity);
            this.mDevModeBtn = devModeBtnView;
            devModeBtnView.setText("Dev Mode: CDP (9222)");
            devModeBtnView.setTextColor(0xFFCDD6F4);
            devModeBtnView.setTextSize(2, 13.5f);
            devModeBtnView.setGravity(19);
            devModeBtnView.setClickable(true);
            devModeBtnView.setOnClickListener(this);
            menuContent.addView(FusionUiHelper.createMenuRowWithCustomText(this.mActivity, "⌨️", devModeBtnView, null));

            TextView aiBridge = new TextView(this.mActivity);
            this.mAiBridgeBtn = aiBridge;
            aiBridge.setText("AI DevBridge (Inspect DOM)");
            aiBridge.setTextColor(0xFFCDD6F4);
            aiBridge.setTextSize(2, 13.5f);
            aiBridge.setGravity(19);
            aiBridge.setClickable(true);
            aiBridge.setOnClickListener(this);
            menuContent.addView(FusionUiHelper.createMenuRowWithCustomText(this.mActivity, "🤖", aiBridge, null));

            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "🗑️", "Hapus Data & Cache", new ClearCacheClick(this)));
            menuContent.addView(FusionUiHelper.createMenuRow(this.mActivity, "❌", "Tutup Tab Webview", new CloseClick(this)));

            menuScrollView.addView(menuContent);
            this.mMenuCard.addView(menuScrollView);

            TextView menuCloseBtn = new TextView(this.mActivity);
            menuCloseBtn.setText("TUTUP MENU");
            FusionUiHelper.setDrawerCloseBtn(menuCloseBtn);
            menuCloseBtn.setTextSize(2, 12.0f);
            menuCloseBtn.setGravity(17);
            menuCloseBtn.setPadding(12, 12, 12, 12);
            menuCloseBtn.setClickable(true);
            menuCloseBtn.setOnClickListener(new CloseMenuClick(this));
            LinearLayout.LayoutParams lpMenuClose = new LinearLayout.LayoutParams(-1, -2);
            lpMenuClose.setMargins(0, 10, 0, 0);
            this.mMenuCard.addView(menuCloseBtn, lpMenuClose);

            this.mControlPill.setVisibility(8);
            if (this.mToolScroll != null) {
                this.mToolScroll.setVisibility(8);
            }
            this.mMenuCard.setVisibility(8);
            FrameLayout frameLayout4 = this.mPaneHost;
            if (frameLayout4 != null) {
                frameLayout4.addView(this.mControlPill, layoutParams);
                this.mPaneHost.addView(this.mMenuCard, layoutParams8);
            }
        }
        updateMenuTexts();
        FusionRecPillHelper.initAndShowRecPill(this);
        FusionToolPillHelper.initToolPill(this);
    }

    public void injectAssetScript(WebView webView, String str) {
        try {
            TermuxActivity termuxActivity = this.mActivity;
            if (termuxActivity == null) {
                return;
            }
            InputStream inputStreamOpen = termuxActivity.getAssets().open("webview_engine/" + str);
            byte[] bArr = new byte[inputStreamOpen.available()];
            inputStreamOpen.read(bArr);
            inputStreamOpen.close();
            webView.evaluateJavascript(new String(bArr, "UTF-8"), null);
        } catch (Exception unused) {
        }
    }

    public FusionTab getTabForWebView(WebView webView) {
        if (webView == null || this.mTabList == null) {
            return null;
        }
        for (int i = 0; i < this.mTabList.size(); i++) {
            FusionTab tab = (FusionTab) this.mTabList.get(i);
            if (tab != null && tab.mWebView == webView) {
                return tab;
            }
        }
        return null;
    }

    public void injectScripts(WebView webView) {
        if (webView == null) {
            return;
        }
        FusionTab ownerTab = getTabForWebView(webView);
        String tabProfile = (ownerTab != null && ownerTab.mProfile != null && !ownerTab.mProfile.isEmpty())
                ? ownerTab.mProfile : this.mCurrentProfile;
        FusionProfileManager.DeviceProfile dev = FusionProfileManager.getDeviceForProfile(tabProfile);
        String solName = (this.mCurrentSolWalletName != null && !this.mCurrentSolWalletName.isEmpty())
                ? this.mCurrentSolWalletName : FusionWalletHelper.getMasterSolName();
        String solAddr = (this.mCurrentSolWalletAddress != null && !this.mCurrentSolWalletAddress.isEmpty())
                ? this.mCurrentSolWalletAddress : FusionWalletHelper.getMasterSolAddress();
        String tronName = (this.mCurrentTronWalletName != null && !this.mCurrentTronWalletName.isEmpty())
                ? this.mCurrentTronWalletName : FusionWalletHelper.getMasterTronName();
        String tronAddr = (this.mCurrentTronWalletAddress != null && !this.mCurrentTronWalletAddress.isEmpty())
                ? this.mCurrentTronWalletAddress : FusionWalletHelper.getMasterTronAddress();
        String suiName = (this.mCurrentSuiWalletName != null && !this.mCurrentSuiWalletName.isEmpty())
                ? this.mCurrentSuiWalletName : FusionWalletHelper.getMasterSuiName();
        String suiAddr = (this.mCurrentSuiWalletAddress != null && !this.mCurrentSuiWalletAddress.isEmpty())
                ? this.mCurrentSuiWalletAddress : FusionWalletHelper.getMasterSuiAddress();
        String aptosName = (this.mCurrentAptosWalletName != null && !this.mCurrentAptosWalletName.isEmpty())
                ? this.mCurrentAptosWalletName : FusionWalletHelper.getMasterAptosName();
        String aptosAddr = (this.mCurrentAptosWalletAddress != null && !this.mCurrentAptosWalletAddress.isEmpty())
                ? this.mCurrentAptosWalletAddress : FusionWalletHelper.getMasterAptosAddress();
        String iotaName = (this.mCurrentIotaWalletName != null && !this.mCurrentIotaWalletName.isEmpty())
                ? this.mCurrentIotaWalletName : FusionWalletHelper.getMasterIotaName();
        String iotaAddr = (this.mCurrentIotaWalletAddress != null && !this.mCurrentIotaWalletAddress.isEmpty())
                ? this.mCurrentIotaWalletAddress : FusionWalletHelper.getMasterIotaAddress();

        webView.evaluateJavascript("window.__FUSION_PROFILE__ = '" + tabProfile + "'; window.__FUSION_DEVICE__ = " + dev.toJsonString() + "; window.__FUSION_WALLET_NAME__ = '" + this.mCurrentWalletName + "'; window.__FUSION_WALLET_ADDRESS__ = '" + this.mCurrentWalletAddress + "'; window.__FUSION_SOL_WALLET_NAME__ = '" + solName + "'; window.__FUSION_SOL_WALLET_ADDRESS__ = '" + solAddr + "'; window.__FUSION_TRON_WALLET_NAME__ = '" + tronName + "'; window.__FUSION_TRON_WALLET_ADDRESS__ = '" + tronAddr + "'; window.__FUSION_SUI_WALLET_NAME__ = '" + suiName + "'; window.__FUSION_SUI_WALLET_ADDRESS__ = '" + suiAddr + "'; window.__FUSION_APTOS_WALLET_NAME__ = '" + aptosName + "'; window.__FUSION_APTOS_WALLET_ADDRESS__ = '" + aptosAddr + "'; window.__FUSION_IOTA_WALLET_NAME__ = '" + iotaName + "'; window.__FUSION_IOTA_WALLET_ADDRESS__ = '" + iotaAddr + "'; window.__ACTIVE_AI_ENGINE__ = '" + FusionAiHelper.getActiveEngine() + "';", null);
        injectAssetScript(webView, "fingerprint_faker.js");
        injectAssetScript(webView, "ghost_injected.js");
        FusionUserscriptHelper.injectMatchingScripts(webView, webView.getUrl());
        FusionExtensionHelper.injectMatchingScripts(webView, webView.getUrl(), "document_idle");
    }

    public void killActiveTab() {
        ArrayList arrayList = this.mTabList;
        if (arrayList != null && arrayList.size() > 0) {
            killTabDirect(this.mActiveTab);
        }
    }

    public void killTab(int i) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionTabKillRunnable(this, i));
    }

    public void performDirectTabDestroy(FusionTab fusionTab) {
        if (fusionTab == null || this.mTabList == null) {
            return;
        }
        int i = this.mTabList.indexOf(fusionTab);
        if (i != -1) {
            this.mTabList.remove(i);
        }
        WebView webView = fusionTab.mWebView;
        if (webView != null) {
            webView.setVisibility(8);
            FrameLayout frameLayout = this.mPaneHost;
            if (frameLayout != null) {
                frameLayout.removeView(webView);
            }
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.stopLoading();
            webView.destroy();
        }
        int size2 = this.mTabList.size();
        if (size2 == 0) {
            this.mActiveWebView = null;
            this.mActiveTab = 0;
            closeWebViewInternal();
            return;
        }
        int i2 = this.mActiveTab;
        if (i2 > i && i != -1) {
            this.mActiveTab = i2 - 1;
        }
        if (this.mActiveTab >= size2) {
            this.mActiveTab = size2 - 1;
        }
        if (this.mActiveTab < 0) {
            this.mActiveTab = 0;
        }
        updateContextUi();
        switchTab(this.mActiveTab);
        System.gc();
    }

    public void destroyTabForSession(TerminalSession session) {
        if (session == null || this.mTabList == null) return;
        TermuxActivity act = this.mActivity;
        if (act != null) {
            act.runOnUiThread(() -> {
                if (this.mTabList == null) return;
                for (int i = this.mTabList.size() - 1; i >= 0; i--) {
                    if (i < this.mTabList.size()) {
                        FusionTab tab = (FusionTab) this.mTabList.get(i);
                        if (tab != null && (tab.mBoundSession == session || (this.mBoundSession == session && tab.mBoundSession == null))) {
                            final String profile = tab.mProfile;
                            if (profile != null && !profile.isEmpty() && !"default".equals(profile) && !profile.startsWith("fresh_")) {
                                // 1. Flush cookies native Android seketika (1ms)
                                FusionProfileManager.flushCookies(profile);

                                // 2. Auto-export ke JSON di background thread (fire-and-forget, ~15ms)
                                final Context context = act != null ? act : (tab.mWebView != null ? tab.mWebView.getContext() : null);
                                new Thread(() -> {
                                    FusionProfileManager.exportProfileSessionToJson(context, profile);
                                }).start();
                            }
                            performDirectTabDestroy(tab);
                        }
                    }
                }
                syncVisibility();
                updateContextUi();
            });
        }
    }

    public void killTabDirect(int i) {
        ArrayList arrayList = this.mTabList;
        if (arrayList == null) {
            return;
        }
        int size = arrayList.size();
        if (i < 0 || i >= size) {
            return;
        }
        final FusionTab fusionTab = (FusionTab) arrayList.get(i);
        if (fusionTab == null) {
            return;
        }

        final String profile = fusionTab.mProfile;
        final TerminalSession boundSession = fusionTab.mBoundSession != null ? fusionTab.mBoundSession : (this.mActivity != null ? this.mActivity.getCurrentSession() : null);
        if (profile != null && !profile.isEmpty() && !"default".equals(profile) && !profile.startsWith("fresh_")) {
            // 1. Flush cookies native Android seketika (1ms)
            FusionProfileManager.flushCookies(profile);

            // 2. Auto-export ke JSON di background thread (fire-and-forget, zero UI block, ~15ms)
            final Context context = this.mActivity != null ? this.mActivity : (fusionTab.mWebView != null ? fusionTab.mWebView.getContext() : null);
            final TermuxActivity act = this.mActivity;
            new Thread(() -> {
                String saveMsg = FusionProfileManager.exportProfileSessionToJson(context, profile);
                if (saveMsg != null && boundSession != null && act != null) {
                    FusionProfileManager.printToTerminalSession(act, boundSession, "\r\n\033[32m[Fusion] " + saveMsg + "\033[0m\r\n");
                }
            }).start();
        }

        performDirectTabDestroy(fusionTab);
    }

    public void killWorkerTab() {
        killActiveTab();
    }

    public void onChainSelected(int i) {
        FusionChainHelper.setChainByIndex(i);
        this.mCurrentChainKey = FusionChainHelper.getCurrentChainKey();
        updateMenuTexts();
        LinearLayout linearLayout = this.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        View view = this.mMenuBackdrop;
        if (view != null) {
            view.setVisibility(8);
        }
        WebView webView = this.mActiveWebView;
        final String chainKey = FusionChainHelper.getCurrentChainKey();
        final String chainIdHex = FusionChainHelper.getChainIdHex();
        if (webView != null) {
            webView.evaluateJavascript("if(window.ethereum){window.ethereum.chainId='" + chainIdHex + "';window.ethereum.networkVersion='" + FusionChainHelper.getChainIdDec() + "';window.dispatchEvent(new CustomEvent('chainChanged',{detail:'" + chainIdHex + "'}));}", null);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (GhostBridgeInterface.checkRpcAlive()) {
                        GhostBridgeInterface.forwardToDaemon("{\"jsonrpc\":\"2.0\",\"id\":" + System.currentTimeMillis() + ",\"method\":\"wallet_switchEthereumChain\",\"params\":[{\"chainId\":\"" + chainIdHex + "\",\"chain\":\"" + chainKey + "\"}]}");
                    }
                } catch (Exception ignored) {}
            }
        }).start();
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) ("Jaringan: " + FusionChainHelper.getCurrentChainName()), 0).show();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == null) {
            return;
        }
        if (view == this.mQuickBackBtn) {
            performQuickBack();
            return;
        }
        if (view == this.mCollapsePillBtn) {
            toggleCollapsePill();
            return;
        }
        if (view == this.mTabBtn) {
            showTabManagerDialog();
            return;
        }
        if (view == this.mAgyBtn) {
            showAgyDialog();
            return;
        }
        if (view == this.mBurgerBtn) {
            toggleMenuCard();
            return;
        }
        if (view == this.mTabBtn) {
            if (this.mActiveTab == 0) {
                switchTab(1);
                return;
            } else {
                switchTab(0);
                return;
            }
        }
        if (view == this.mKillWorkerBtn) {
            killWorkerTab();
            return;
        }
        if (view == this.mNewWorkerBtn) {
            openWorkerTab("https://x.com");
            switchTab(1);
            return;
        }
        if (view == this.mMinBtn) {
            toggleMinimize();
            return;
        }
        if (view == this.mMinPillBtn) {
            toggleMinimize();
            return;
        }
        if (view == this.mStartDaemonBtn) {
            startWalletDaemon();
            return;
        }
        if (view == this.mDevModeBtn) {
            toggleDevMode();
            return;
        }
        if (view == this.mAiBridgeBtn) {
            triggerAiDevBridge();
            return;
        }
        if (view == this.mRandomFpBtn) {
            randomizeFingerprint();
            return;
        }
        if (view == this.mRunMacroBtn) {
            runMacro();
        } else if (view == this.mOpacityBtn) {
            cycleOpacity();
        } else if (view == this.mStatusText) {
            onStatusTextClicked();
        }
    }

    public void onPause() {
        // Continuous background execution: do NOT pause WebView so macros,
        // timers, Web3 RPCs, and auto-runners keep executing when Termux is minimized.
        if (this.mTabList != null) {
            for (int i = 0; i < this.mTabList.size(); i++) {
                FusionTab tab = (FusionTab) this.mTabList.get(i);
                if (tab != null && tab.mWebView != null) {
                    tab.mWebView.resumeTimers();
                }
            }
        }
    }

    public void acquireWakeLock() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMacroWakeLock == null) {
                        PowerManager pm = (PowerManager) act.getSystemService(Context.POWER_SERVICE);
                        if (pm != null) {
                            mMacroWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "termux:fusion-macro-runner");
                        }
                    }
                    if (mMacroWakeLock != null && !mMacroWakeLock.isHeld()) {
                        mMacroWakeLock.acquire(4 * 60 * 60_000L);
                    }
                } catch (Exception unused) {
                }
            }
        });
    }

    public void releaseWakeLock() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMacroWakeLock != null && mMacroWakeLock.isHeld()) {
                        mMacroWakeLock.release();
                    }
                } catch (Exception unused) {
                }
            }
        });
    }

    public static final String WEB_NOTIFICATION_CHANNEL_ID = "termux_fusion_web_notifications";
    private static int sNextNotificationId = 7000;

    public void maximizeWebView() {
        maximizeWebView(null);
    }

    public void maximizeWebView(final String targetUrl) {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (act.currentWallPage() != com.termux.app.wall.PaneWallPage.TERMINAL) {
                    act.goToTerminalCanvas(true);
                }
                FusionTab activeTab = FusionTabStateHelper.getActiveTab(FusionWebViewManager.this);
                if (activeTab != null && activeTab.mIsMinimized) {
                    FusionTabStateHelper.toggleMinimize(FusionWebViewManager.this);
                } else if (mActiveWebView != null) {
                    mActiveWebView.setVisibility(View.VISIBLE);
                    mActiveWebView.bringToFront();
                    if (mTerminalPaneHost != null) {
                        mTerminalPaneHost.setVisibility(View.GONE);
                    }
                    if (mPaneHost != null && mPaneHost.getVisibility() != View.VISIBLE) {
                        mPaneHost.setVisibility(View.VISIBLE);
                    }
                    if (mControlPill != null) {
                        mControlPill.bringToFront();
                    }
                    if (mToolScroll != null) {
                        mToolScroll.bringToFront();
                    }
                }
                if (targetUrl != null && !targetUrl.trim().isEmpty() && mActiveWebView != null) {
                    mActiveWebView.loadUrl(targetUrl.trim());
                }
            }
        });
    }

    public void showWebNotification(final String title, final String body, final String iconUrl, final String targetUrl) {
        final TermuxActivity act = this.mActivity;
        if (act == null) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap avatar = null;
                if (iconUrl != null && (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))) {
                    try {
                        URL url = new URL(iconUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(2500);
                        conn.setReadTimeout(2500);
                        conn.setInstanceFollowRedirects(true);
                        conn.connect();
                        InputStream input = conn.getInputStream();
                        avatar = BitmapFactory.decodeStream(input);
                        if (input != null) input.close();
                    } catch (Exception ignored) {
                    }
                }
                final Bitmap finalAvatar = avatar;

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NotificationManager notificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager == null) return;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = notificationManager.getNotificationChannel(WEB_NOTIFICATION_CHANNEL_ID);
                                if (channel == null) {
                                    channel = new NotificationChannel(
                                        WEB_NOTIFICATION_CHANNEL_ID,
                                        "Fusion Web Notifications",
                                        NotificationManager.IMPORTANCE_HIGH
                                    );
                                    channel.setDescription("Notifikasi real-time dari situs web & x.com di Fusion WebView");
                                    channel.enableLights(true);
                                    channel.enableVibration(true);
                                    notificationManager.createNotificationChannel(channel);
                                }
                            }

                            String cleanTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : "X Notification";
                            String cleanBody = (body != null) ? body.trim() : "";

                            Intent tapIntent = TermuxActivity.newInstance(act);
                            tapIntent.setAction("com.termux.FUSION_OPEN_WEBVIEW");
                            tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            if (targetUrl != null && !targetUrl.trim().isEmpty()) {
                                tapIntent.putExtra("target_url", targetUrl.trim());
                            }

                            int reqCode = (int) (System.currentTimeMillis() & 0xFFFFFF);
                            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                flags |= PendingIntent.FLAG_IMMUTABLE;
                            }
                            PendingIntent pendingIntent = PendingIntent.getActivity(act, reqCode, tapIntent, flags);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(act, WEB_NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(com.termux.R.drawable.ic_service_notification)
                                .setContentTitle(cleanTitle)
                                .setContentText(cleanBody)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(cleanBody))
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setContentIntent(pendingIntent);

                            if (finalAvatar != null) {
                                builder.setLargeIcon(finalAvatar);
                            }

                            int notifId = sNextNotificationId++;
                            if (sNextNotificationId > 8999) sNextNotificationId = 7000;
                            notificationManager.notify(notifId, builder.build());
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        }).start();
    }

    @Override // android.webkit.ValueCallback
    public void onReceiveValue(String str) {
        saveBridgeResult(str);
    }

    public void onResume() {
        if (this.mTabList != null) {
            for (int i = 0; i < this.mTabList.size(); i++) {
                FusionTab tab = (FusionTab) this.mTabList.get(i);
                if (tab != null && tab.mWebView != null) {
                    tab.mWebView.onResume();
                    tab.mWebView.resumeTimers();
                }
            }
        }
        syncVisibility();
    }

    public void onStatusTextClicked() {
        boolean zCheckRpcDaemon = checkRpcDaemon();
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return;
        }
        if (zCheckRpcDaemon) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "* RPC: ONLINE (127.0.0.1:3333)", 0).show();
        } else {
            startWalletDaemon();
        }
    }

    public static String cleanSiteTitle(String title) {
        if (title == null) return null;
        title = title.trim();
        if (title.isEmpty() || "New Tab".equalsIgnoreCase(title) || "about:blank".equalsIgnoreCase(title) || "undefined".equalsIgnoreCase(title)) {
            return null;
        }

        if ("Beranda".equalsIgnoreCase(title) || "Fusion Browser".equalsIgnoreCase(title)) {
            return "Beranda";
        }

        // Khusus X / Twitter
        if (title.contains("/ X") || title.endsWith(" / X") || title.equals("X") || title.startsWith("X (") || title.startsWith("(X)")) {
            return "X";
        }
        String lower = title.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("twitter")) {
            return "X";
        }

        // Pisahkan pemisah judul umum (misal: "Title - SiteName", "Page | Brand")
        String[] separators = new String[]{" - ", " | ", " — ", " : ", " • ", " · "};
        for (String sep : separators) {
            int idx = title.indexOf(sep);
            if (idx > 0) {
                String candidate = title.substring(0, idx).trim();
                if (candidate.length() >= 2) {
                    title = candidate;
                    break;
                }
            }
        }

        // Pembersihan ekstensi jika judulnya berupa nama domain
        if (title.equalsIgnoreCase("google.com")) return "Google";
        if (title.equalsIgnoreCase("x.com")) return "X";
        if (title.equalsIgnoreCase("github.com")) return "GitHub";

        if (title.length() > 16) {
            return title.substring(0, 15) + "…";
        }
        return title;
    }

    public static String extractDomainName(String urlStr) {
        if (urlStr == null || urlStr.isEmpty() || "about:blank".equalsIgnoreCase(urlStr)) return null;
        if (urlStr.contains("home.html")) return "Beranda";
        try {
            android.net.Uri uri = android.net.Uri.parse(urlStr);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) host = host.substring(4);
                if ("x.com".equalsIgnoreCase(host) || "twitter.com".equalsIgnoreCase(host)) {
                    return "X";
                }
                if ("google.com".equalsIgnoreCase(host)) {
                    return "Google";
                }
                if ("github.com".equalsIgnoreCase(host)) {
                    return "GitHub";
                }
                if (host.length() > 16) {
                    return host.substring(0, 15) + "…";
                }
                return host;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public String getSiteNameForSession(TerminalSession session) {
        if (session == null || this.mTabList == null || this.mTabList.isEmpty()) {
            return null;
        }

        FusionTab targetTab = null;
        int size = this.mTabList.size();
        if (this.mActiveTab >= 0 && this.mActiveTab < size) {
            FusionTab candidate = (FusionTab) this.mTabList.get(this.mActiveTab);
            if (candidate != null && (candidate.mBoundSession == session || (candidate.mBoundSession == null && this.mActivity != null && this.mActivity.getCurrentSession() == session))) {
                targetTab = candidate;
            }
        }
        if (targetTab == null) {
            for (int i = size - 1; i >= 0; i--) {
                FusionTab tab = (FusionTab) this.mTabList.get(i);
                if (tab != null && (tab.mBoundSession == session || (tab.mBoundSession == null && this.mActivity != null && this.mActivity.getCurrentSession() == session))) {
                    targetTab = tab;
                    break;
                }
            }
        }

        if (targetTab == null || targetTab.mIsMinimized) {
            return null;
        }

        // 1. Title dari DOM
        String siteTitle = cleanSiteTitle(targetTab.mTitle);
        if (siteTitle != null && !siteTitle.isEmpty()) {
            return siteTitle;
        }

        // 2. Domain dari URL
        String domain = extractDomainName(targetTab.mUrl);
        if (domain != null && !domain.isEmpty()) {
            return domain;
        }

        return "Web";
    }

    public void onTabTitleReceived(WebView webView, String str) {
        ArrayList arrayList;
        if (webView == null || str == null || (arrayList = this.mTabList) == null) {
            return;
        }
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i);
            if (fusionTab.mWebView == webView) {
                fusionTab.mTitle = str;
                if (this.mActivity != null) {
                    this.mActivity.runOnUiThread(this::updateContextUi);
                } else {
                    updateContextUi();
                }
                return;
            }
        }
    }

    public void onTabUrlLoaded(WebView webView, String str) {
        ArrayList arrayList;
        if (webView == null || str == null || (arrayList = this.mTabList) == null) {
            return;
        }
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i);
            if (fusionTab.mWebView == webView) {
                fusionTab.mUrl = str;
                if (this.mActivity != null) {
                    this.mActivity.runOnUiThread(this::updateContextUi);
                } else {
                    updateContextUi();
                }
                return;
            }
        }
    }

    public void onWalletSelected(int i) {
        String name = FusionWalletHelper.getName(i);
        String address = FusionWalletHelper.getAddress(i);
        this.mCurrentWalletName = name;
        this.mCurrentWalletAddress = address;
        GhostBridgeInterface.setWallet(name, address);

        String solName = FusionWalletHelper.getSolName(i);
        String solAddr = FusionWalletHelper.getSolAddress(i);
        if (solAddr != null && !solAddr.isEmpty()) {
            this.mCurrentSolWalletName = solName;
            this.mCurrentSolWalletAddress = solAddr;
            GhostBridgeInterface.setSolWallet(solName, solAddr);
        }

        String tronName = FusionWalletHelper.getTronName(i);
        String tronAddr = FusionWalletHelper.getTronAddress(i);
        if (tronAddr != null && !tronAddr.isEmpty()) {
            this.mCurrentTronWalletName = tronName;
            this.mCurrentTronWalletAddress = tronAddr;
            GhostBridgeInterface.setTronWallet(tronName, tronAddr);
        }

        String suiName = FusionWalletHelper.getSuiName(i);
        String suiAddr = FusionWalletHelper.getSuiAddress(i);
        if (suiAddr != null && !suiAddr.isEmpty()) {
            this.mCurrentSuiWalletName = suiName;
            this.mCurrentSuiWalletAddress = suiAddr;
            GhostBridgeInterface.setSuiWallet(suiName, suiAddr);
        }

        String aptosName = FusionWalletHelper.getAptosName(i);
        String aptosAddr = FusionWalletHelper.getAptosAddress(i);
        if (aptosAddr != null && !aptosAddr.isEmpty()) {
            this.mCurrentAptosWalletName = aptosName;
            this.mCurrentAptosWalletAddress = aptosAddr;
            GhostBridgeInterface.setAptosWallet(aptosName, aptosAddr);
        }

        String iotaName = FusionWalletHelper.getIotaName(i);
        String iotaAddr = FusionWalletHelper.getIotaAddress(i);
        if (iotaAddr != null && !iotaAddr.isEmpty()) {
            this.mCurrentIotaWalletName = iotaName;
            this.mCurrentIotaWalletAddress = iotaAddr;
            GhostBridgeInterface.setIotaWallet(iotaName, iotaAddr);
        }

        updateMenuTexts();
        LinearLayout linearLayout = this.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }

        final String finalAddr = address;
        final String finalName = name;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GhostBridgeInterface.forwardToDaemon("{\"method\":\"fusion_switchWallet\",\"params\":[\"" + finalAddr + "\",\"" + finalName + "\"]}");
                } catch (Throwable ignored) {}
            }
        }).start();

        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.evaluateJavascript("if(window.ethereum){window.ethereum.selectedAddress='" + address + "';window.dispatchEvent(new CustomEvent('accountsChanged',{detail:['" + address + "']}));}", null);
            if (solAddr != null && !solAddr.isEmpty()) {
                webView.evaluateJavascript("if(window.solana){window.__FUSION_SOL_WALLET_ADDRESS__='" + solAddr + "';if(window.solana.publicKey){window.solana.publicKey._val='" + solAddr + "';}window.solana.emit('accountChanged','" + solAddr + "');}", null);
            }
            if (tronAddr != null && !tronAddr.isEmpty()) {
                webView.evaluateJavascript("if(window.tronWeb){window.__FUSION_TRON_WALLET_ADDRESS__='" + tronAddr + "';if(window.tronWeb.defaultAddress){window.tronWeb.defaultAddress.base58='" + tronAddr + "';}window.postMessage({message:{action:'setAccount',data:{address:'" + tronAddr + "'}}},'*');}", null);
            }
            if (suiAddr != null && !suiAddr.isEmpty()) {
                webView.evaluateJavascript("if(window.__FUSION_SET_SUI_ACCOUNT__){window.__FUSION_SET_SUI_ACCOUNT__('" + suiAddr + "');}", null);
            }
            if (aptosAddr != null && !aptosAddr.isEmpty()) {
                webView.evaluateJavascript("if(window.__FUSION_SET_APTOS_ACCOUNT__){window.__FUSION_SET_APTOS_ACCOUNT__('" + aptosAddr + "');}", null);
            }
            if (iotaAddr != null && !iotaAddr.isEmpty()) {
                webView.evaluateJavascript("if(window.__FUSION_SET_IOTA_ACCOUNT__){window.__FUSION_SET_IOTA_ACCOUNT__('" + iotaAddr + "');}", null);
            }
            if (this.mIsRecording) {
                webView.evaluateJavascript("if(window.AgyRecorder && window.AgyRecorder.isRecording){window.AgyRecorder.recordEvent({type:'next_wallet',timestamp:Date.now(),text:'Ganti Wallet -> " + address + "'});}", null);
            }
        }
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new OpenRunnable(this, str, str2, str3, str4, z, z2, z3, str5, str6, i));
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        openConfig(str, str2, str3, str4, z, z2, z3, str5, "", 0);
    }

    public java.util.List<FusionTab> getTabList() {
        return this.mTabList;
    }

    public void openUrl(String str, String str2, boolean z, boolean z2) {
        openConfig(str, str2, "", "base", z, z2, false, "");
    }

    public void openUrl(String str, String str2, boolean z, boolean z2, String str3) {
        openConfig(str, str2, str3, "base", z, z2, false, "");
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        openUrlInternal(str, str2, str3, str4, z, z2, z3, str5, "", 0);
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String proxyStr, int intervalMins) {
        TerminalSession currentSession = null;
        initViews();
        if (str == null || str.isEmpty() || "home".equalsIgnoreCase(str) || "about:home".equalsIgnoreCase(str) || "about:newtab".equalsIgnoreCase(str)) {
            str = "file:///android_asset/webview_engine/home.html";
        }
        if (str2 == null) {
            str2 = "default";
        }
        this.mCurrentProfile = str2;
        this.mIsGhostMode = z;
        this.mIsDesktopMode = z2;
        this.mIsAutoMode = z3;
        this.mWcUri = str5;
        this.mIsMinimized = false;
        if (str4 == null) {
            str4 = "base";
        }
        this.mCurrentChainKey = str4;
        FusionChainHelper.setChain(str4);
        if (str3 == null || str3.length() == 0) {
            str3 = FusionWalletHelper.getMasterName();
        }
        this.mCurrentWalletName = str3;
        String strFindAddressByName = FusionWalletHelper.findAddressByName(str3);
        if (strFindAddressByName == null || strFindAddressByName.length() == 0) {
            strFindAddressByName = "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359";
        }
        this.mCurrentWalletAddress = strFindAddressByName;
        GhostBridgeInterface.setWallet(str3, strFindAddressByName);
        GhostBridgeInterface.setAuto(z3);
        GhostBridgeInterface.setWc(str5);
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity != null) {
            currentSession = termuxActivity.getCurrentSession();
            this.mBoundSession = currentSession;
        }
        if (this.mTabList == null) {
            this.mTabList = new ArrayList();
        }
        int i = -1;
        if (currentSession != null) {
            int size = this.mTabList.size();
            for (int i2 = 0; i2 < size; i2++) {
                FusionTab fusionTab = (FusionTab) this.mTabList.get(i2);
                if (fusionTab != null && fusionTab.mBoundSession == currentSession) {
                    String tabProf = (fusionTab.mProfile == null || fusionTab.mProfile.isEmpty()) ? "default" : fusionTab.mProfile;
                    if (str2.equals(tabProf)) {
                        i = i2;
                        break;
                    }
                }
            }
        }
        FusionTab targetTab = null;
        if (i >= 0) {
            FusionTab fusionTab2 = (FusionTab) this.mTabList.get(i);
            fusionTab2.mUrl = str;
            fusionTab2.mIsMinimized = false;
            targetTab = fusionTab2;
            switchTab(i);
            FusionProfileManager.applyProfile(str2, fusionTab2.mWebView, z2);
            if (fusionTab2.mWebView != null) {
                fusionTab2.mWebView.loadUrl(str);
            }
        } else {
            FusionTab fusionTabCreateTabInternal = createTabInternal(str, str2, str3, str4, z, z2, z3, str5);
            targetTab = fusionTabCreateTabInternal;
            switchTab(this.mTabList.size() - 1);
            if (fusionTabCreateTabInternal != null && fusionTabCreateTabInternal.mWebView != null) {
                fusionTabCreateTabInternal.mWebView.loadUrl(str);
            }
        }
        if (targetTab != null) {
            String profKey = (str2 != null && !str2.isEmpty()) ? str2 : "default";

            if (proxyStr != null && !proxyStr.trim().isEmpty()) {
                if ("direct".equalsIgnoreCase(proxyStr) || "off".equalsIgnoreCase(proxyStr) || "none".equalsIgnoreCase(proxyStr)) {
                    targetTab.mIsProxyEnabled = false;
                    targetTab.mAutoRotateIntervalSec = 0;
                    FusionProxyHelper.savePersistentState(this.mActivity, profKey, false, null, 0, "direct", null);
                    FusionProxyHelper.applyProxyToEngine(targetTab);
                    FusionProxyHelper.updateProxyIndicator(this, targetTab);
                } else {
                    FusionProxyHelper.configureTabProxy(targetTab, proxyStr, intervalMins * 60, this);
                }
            } else {
                if (targetTab.mIsProxyEnabled && targetTab.mProxyItem != null && targetTab.mProxyItem.isValid()) {
                    if (targetTab.mAutoRotateIntervalSec > 0) {
                        FusionProxyHelper.startAutoRotateScheduler(this);
                    }
                    FusionProxyHelper.applyProxyToEngine(targetTab);
                    FusionProxyHelper.updateProxyIndicator(this, targetTab);
                } else {
                    FusionProxyHelper.PersistentProxyState saved = FusionProxyHelper.loadPersistentState(this.mActivity, profKey);
                    if (saved != null && saved.enabled && saved.item != null && saved.item.isValid()) {
                        targetTab.mProxyItem = saved.item;
                        targetTab.mIsProxyEnabled = true;
                        targetTab.mAutoRotateIntervalSec = saved.intervalSec;
                        targetTab.mLastProxyRotatedTime = System.currentTimeMillis();
                        if (saved.intervalSec > 0) {
                            FusionProxyHelper.startAutoRotateScheduler(this);
                        }
                        FusionProxyHelper.applyProxyToEngine(targetTab);
                        FusionProxyHelper.updateProxyIndicator(this, targetTab);
                    } else {
                        targetTab.mIsProxyEnabled = false;
                        targetTab.mAutoRotateIntervalSec = 0;
                        FusionProxyHelper.applyProxyToEngine(targetTab);
                        FusionProxyHelper.updateProxyIndicator(this, targetTab);
                    }
                }
            }
        }
        syncVisibility();
        updateContextUi();
        FusionToolPillHelper.showToolPill(this);
        FusionToolPillHelper.bringToFront();
    }

    public void openWorkerTab(String str) {
        addNewTab(str);
    }

    public void performQuickBack() {
        WebView webView;
        if (this.mActiveTab != 1 || (webView = this.mWorkerWebView) == null) {
            webView = this.mActiveWebView;
        }
        if (webView == null) {
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            webView.evaluateJavascript("window.history.back();", null);
        }
    }

    public void performQuickForward() {
        WebView webView;
        if (this.mActiveTab != 1 || (webView = this.mWorkerWebView) == null) {
            webView = this.mActiveWebView;
        }
        if (webView == null) {
            return;
        }
        if (webView.canGoForward()) {
            webView.goForward();
        } else {
            webView.evaluateJavascript("window.history.forward();", null);
        }
    }

    public void toggleBookmarkCurrentPage() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        WebView webView = (this.mActiveTab != 1 || this.mWorkerWebView == null) ? this.mActiveWebView : this.mWorkerWebView;
        if (webView == null || webView.getUrl() == null || webView.getUrl().isEmpty()) {
            FusionToast.makeText(act, "Tidak ada halaman aktif untuk disimpan", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = webView.getUrl();
        String title = webView.getTitle();
        if (title == null || title.isEmpty()) {
            title = url;
        }
        FusionBookmarkHelper.addBookmark(act, title, url);
        FusionToast.makeText(act, "⭐ Disimpan ke Bookmark!", Toast.LENGTH_SHORT).show();
    }

    public void showBookmarksDialog() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        FusionBookmarkHelper.showBookmarksDialog(act, this);
    }

    public void saveCurrentSessionManual() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        FusionTab tab = FusionTabStateHelper.getActiveTab(this);
        final String profile = tab != null ? tab.mProfile : this.mCurrentProfile;
        if (profile != null && !profile.isEmpty() && !"default".equals(profile) && !profile.startsWith("fresh_")) {
            FusionProfileManager.flushCookies(profile);
            new Thread(() -> {
                String saveMsg = FusionProfileManager.exportProfileSessionToJson(act, profile);
                TerminalSession boundSession = tab != null && tab.mBoundSession != null ? tab.mBoundSession : act.getCurrentSession();
                if (saveMsg != null && boundSession != null) {
                    FusionProfileManager.printToTerminalSession(act, boundSession, "\r\n\033[32m[Fusion] " + saveMsg + "\033[0m\r\n");
                }
                act.runOnUiThread(() -> {
                    FusionToast.makeText(act, "[Fusion] Sesi profile '" + profile + "' berhasil disimpan!", Toast.LENGTH_SHORT).show();
                });
            }).start();
        } else {
            FusionToast.makeText(act, "[Fusion] Sesi default (tanpa profile aktif). Gunakan --profile <nama>", Toast.LENGTH_SHORT).show();
        }
    }

    public void showSiteInfoDialog() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        WebView webView = (this.mActiveTab != 1 || this.mWorkerWebView == null) ? this.mActiveWebView : this.mWorkerWebView;
        final String url = webView != null ? webView.getUrl() : "Tidak ada URL";
        final String title = webView != null ? webView.getTitle() : "-";
        boolean isHttps = url != null && url.startsWith("https://");

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle(isHttps ? "🔒 Info Situs (Aman)" : "ℹ️ Info Situs");
        b.setMessage("Judul:\n" + title + "\n\nURL:\n" + url + "\n\nProfile: " + this.mCurrentProfile + "\nMode: " + (this.mIsDesktopMode ? "Desktop" : "Mobile") + "\nDevTools CDP: " + (this.mIsDevMode ? "Aktif (9222)" : "Nonaktif"));
        b.setPositiveButton("Salin URL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null && url != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("URL", url));
                        FusionToast.makeText(act, "URL berhasil disalin!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Throwable unused) {}
            }
        });
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public void showHistoryDialog() {
        TermuxActivity act = this.mActivity;
        if (act == null) return;
        final WebView webView = (this.mActiveTab != 1 || this.mWorkerWebView == null) ? this.mActiveWebView : this.mWorkerWebView;
        if (webView == null) {
            FusionToast.makeText(act, "WebView tidak aktif", Toast.LENGTH_SHORT).show();
            return;
        }
        WebBackForwardList list = webView.copyBackForwardList();
        int size = list != null ? list.getSize() : 0;
        if (size == 0) {
            FusionToast.makeText(act, "Belum ada riwayat penjelajahan", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] items = new String[size];
        final int currentIndex = list.getCurrentIndex();
        for (int i = 0; i < size; i++) {
            WebHistoryItem item = list.getItemAtIndex(i);
            String itemTitle = (item != null && item.getTitle() != null && !item.getTitle().isEmpty()) ? item.getTitle() : (item != null ? item.getUrl() : "-");
            if (i == currentIndex) {
                items[i] = "▶ " + itemTitle;
            } else {
                items[i] = "   " + itemTitle;
            }
        }
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("🕒 Riwayat Navigasi (" + size + ")");
        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int step = which - currentIndex;
                if (step != 0) {
                    webView.goBackOrForward(step);
                    closeAllDrawers();
                }
            }
        });
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public void showUserscriptsDialog() {
        FusionUserscriptHelper.showUserscriptsDialog(this.mActivity, this);
    }

    public void showProxyDialog() {
        FusionProxyHelper.showProxyDialog(this.mActivity, this);
    }

    public FusionTab getActiveFusionTab() {
        return FusionTabStateHelper.getActiveTab(this);
    }

    public void showExtensionsDialog() {
        FusionExtensionHelper.showExtensionsDialog(this.mActivity, this);
    }

    public boolean onBackPressed() {
        if ((this.mMenuCard != null && this.mMenuCard.getVisibility() == View.VISIBLE) ||
            (FusionDrawerHelper.sExtensionDrawer != null && FusionDrawerHelper.sExtensionDrawer.getVisibility() == View.VISIBLE)) {
            closeAllDrawers();
            return true;
        }
        WebView webView;
        if (this.mActiveTab != 1 || (webView = this.mWorkerWebView) == null) {
            webView = this.mActiveWebView;
        }
        if (webView != null && webView.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                toggleMinimize();
                return true;
            }
        }
        return false;
    }

    public boolean isWebViewVisible() {
        if (this.mActivity != null && this.mActivity.currentWallPage() != com.termux.app.wall.PaneWallPage.TERMINAL) {
            return false;
        }
        WebView webView;
        if (this.mActiveTab != 1 || (webView = this.mWorkerWebView) == null) {
            webView = this.mActiveWebView;
        }
        return (webView != null && webView.getVisibility() == View.VISIBLE)
            || (this.mMenuCard != null && this.mMenuCard.getVisibility() == View.VISIBLE);
    }

    public boolean isWebViewOpen() {
        if (this.mTabList == null || this.mTabList.isEmpty()) {
            return false;
        }
        if (this.mPaneHost != null && this.mPaneHost.getVisibility() == View.VISIBLE) {
            return true;
        }
        if (this.mControlPill != null && this.mControlPill.getVisibility() == View.VISIBLE) {
            return true;
        }
        return isWebViewVisible();
    }

    public void promptNewTabUrl() {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionTabPromptRunnable(this));
    }

    public void promptNewTabUrlDirect() {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle("[ New Tab ]");
        builder.setMessage("Masukkan URL atau alamat website:");
        EditText editText = new EditText(appCompatActivity);
        editText.setHint("https://...");
        editText.setSingleLine(true);
        FrameLayout frameLayout = new FrameLayout(appCompatActivity);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(40, 0, 40, 0);
        editText.setLayoutParams(layoutParams);
        frameLayout.addView(editText);
        builder.setView(frameLayout);
        builder.setPositiveButton("[Open]", new FusionTabOpenUrlListener(this, editText));
        builder.setNegativeButton("[Cancel]", new FusionTabCancelDialogListener());
        builder.show();
    }

    public void randomizeFingerprint() {
        FusionTabStateHelper.randomizeFingerprint(this);
    }

    public void runMacro() {
        WebView webView = this.mActiveWebView;
        if (webView == null) {
            return;
        }
        webView.evaluateJavascript("if(window.AgyBridge && window.AgyBridge.showRecipePicker){window.AgyBridge.showRecipePicker();}else if(window.__FUSION_PLAY_MACRO__){window.__FUSION_PLAY_MACRO__();}", null);
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Buka Rekaman...", 0).show();
        }
    }

    public void stopMacro() {
        if (this.mIsRecording) {
            toggleMacroRecording();
        }
        evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.stop){window.AgyBridge.stop();}if(window.AgyBatchRunner && window.AgyBatchRunner.stop){window.AgyBatchRunner.stop();}if(window.AgyRecorder && window.AgyRecorder.cancel){window.AgyRecorder.cancel();}");
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Menghentikan Makro...", 0).show();
        }
    }

    public void saveBridgeResult(String str) {
        if (str == null || str.equals("null")) {
            return;
        }
        copyAddressToClipboard(str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/data/data/com.termux/files/home/.webview_bridge_dump.json");
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception unused) {
        }
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream("/data/local/nhsystem/kali-arm64/root/webview_bridge_dump.json");
            fileOutputStream2.write(str.getBytes());
            fileOutputStream2.close();
        } catch (Exception unused2) {
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "AI DevBridge: DOM tersimpan di ~/.webview_bridge_dump.json & disalin!", 1).show();
        }
    }

    public void saveMacroResult(String str) {
        if (str == null || str.equals("null")) {
            return;
        }
        copyAddressToClipboard(str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/data/local/nhsystem/kali-arm64/root/macro_" + this.mCurrentProfile + ".json");
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception unused) {
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Makro Berhasil Disimpan & Disalin!", 1).show();
        }
    }

    public void showAgyDialog() {
        if (this.mActivity == null) {
            return;
        }
        this.mActivity.runOnUiThread(new FusionAgyShowDialogRunnable(this));
    }

    public void showAgyDialogDirect() {
        FusionDrawerHelper.showAgyDrawer(this);
    }

    public void showChainSwitcherDialog() {
        FusionDrawerHelper.showChainDrawer(this);
    }

    public void showRecipeManagerDialog() {
        if (this.mActivity == null) {
            return;
        }
        this.mActivity.runOnUiThread(new FusionRecipeManagerShowRunnable(this));
    }

    public void showRecipeManagerDialogDirect() {
        closeAllDrawers();
        evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.showRecipePicker){window.AgyBridge.showRecipePicker();}else if(typeof showRecipePickerModal==='function'){showRecipePickerModal();}");
    }

    public void showTabManagerDialog() {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new FusionTabShowDialogRunnable(this));
    }

    public void showTabManagerDialogDirect() {
        FusionDrawerHelper.showTabDrawer(this);
    }

    public void showWalletSwitcherDialog() {
        FusionDrawerHelper.showWalletDrawer(this);
    }

    public void showWcDialog() {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle("WalletConnect URI");
        String str = this.mWcUri;
        if (str == null || str.length() <= 0) {
            builder.setMessage("Tidak ada URI WalletConnect aktif.\nJalankan dengan: termux-ipc webview --wc 'wc:...' <url>");
        } else {
            builder.setMessage(str);
        }
        builder.setPositiveButton("OK", (DialogInterface.OnClickListener) null);
        builder.show();
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [android.content.Context, com.termux.app.TermuxActivity] */
    public void startWalletDaemon() {
        boolean zCheckRpcDaemon = checkRpcDaemon();
        TermuxActivity r1 = this.mActivity;
        if (r1 == null) {
            return;
        }
        if (zCheckRpcDaemon) {
            FusionToast.makeText((Context) r1, "[OK] GhostWallet Aktif di Port 3333!", 0).show();
            return;
        }
        String cmd = FusionWalletHelper.getStartWalletDaemonCommand();
        TermuxIpcServer.injectCommand(r1, cmd, true);
        FusionToast.makeText((Context) r1, "Memulai GhostWallet di terminal (port 3333)...", 0).show();
        updateMenuTexts();
        toggleMinimize();
    }

    public void switchTab(int i) {
        ArrayList arrayList = this.mTabList;
        if (arrayList == null) {
            return;
        }
        int size = arrayList.size();
        if (i < 0 || i >= size) {
            return;
        }
        this.mActiveTab = i;
        FusionToolPillHelper.collapse(this);
        FusionTab targetTab = (FusionTab) arrayList.get(i);
        TermuxActivity termuxActivity = this.mActivity;
        if (targetTab != null && termuxActivity != null) {
            if (targetTab.mBoundSession == null) {
                targetTab.mBoundSession = termuxActivity.getCurrentSession();
            } else if (termuxActivity.getTermuxTerminalSessionClient() != null && termuxActivity.getCurrentSession() != targetTab.mBoundSession) {
                termuxActivity.getTermuxTerminalSessionClient().setCurrentSession(targetTab.mBoundSession);
            }
        }
        for (int i2 = 0; i2 < size; i2++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i2);
            if (i2 == i) {
                WebView webView = fusionTab.mWebView;
                if (fusionTab.mIsMinimized) {
                    webView.setVisibility(8);
                    TextView textView = this.mMinPillBtn;
                    if (textView != null) {
                        textView.setText(" MAX ");
                    }
                    if (this.mTerminalPaneHost != null) {
                        this.mTerminalPaneHost.setVisibility(0);
                    }
                } else {
                    webView.setVisibility(0);
                    webView.bringToFront();
                    TextView textView2 = this.mMinPillBtn;
                    if (textView2 != null) {
                        textView2.setText(" MIN ");
                    }
                    if (this.mTerminalPaneHost != null) {
                        this.mTerminalPaneHost.setVisibility(8);
                    }
                    if (termuxActivity != null) {
                        KeyboardUtils.setSoftInputModeAdjustResize(termuxActivity);
                    }
                }
                this.mActiveWebView = webView;
                this.mCurrentProfile = fusionTab.mProfile;
                FusionProxyHelper.applyProxyToEngine(fusionTab);
                FusionProxyHelper.updateProxyIndicator(this, fusionTab);
            } else {
                if (fusionTab != null && fusionTab.mWebView != null) {
                    fusionTab.mWebView.setVisibility(8);
                }
            }
        }
        if (this.mPaneHost != null) {
            this.mPaneHost.setVisibility(View.VISIBLE);
            this.mPaneHost.setAlpha(1.0f);
            this.mPaneHost.setTranslationX(0.0f);
            this.mPaneHost.bringToFront();
        }
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            if (linearLayout.getParent() == null && this.mPaneHost != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                layoutParams.setMargins(0, 8, 12, 0);
                this.mPaneHost.addView(linearLayout, layoutParams);
            }
            linearLayout.setVisibility(0);
            linearLayout.setElevation(160.0f);
            linearLayout.bringToFront();
        }
        if (this.mToolScroll == null) {
            FusionToolPillHelper.initToolPill(this);
        }
        if (this.mToolScroll != null) {
            if (this.mToolScroll.getParent() == null && this.mPaneHost != null) {
                float density = this.mActivity.getResources().getDisplayMetrics().density;
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                layoutParams.setMargins(0, (int) (58 * density), 10, 0);
                this.mPaneHost.addView(this.mToolScroll, layoutParams);
            }
            if (this.mToolPill != null) {
                this.mToolPill.setVisibility(View.VISIBLE);
            }
            this.mToolScroll.setVisibility(0);
            this.mToolScroll.setElevation(160.0f);
            this.mToolScroll.bringToFront();
            FusionToolPillHelper.syncToolPillPosition(this);
            FusionToolPillHelper.updateToolPillUi(this);
        }
        LinearLayout linearLayout2 = this.mMenuCard;
        if (linearLayout2 != null && linearLayout2.getVisibility() == 0) {
            linearLayout2.setElevation(170.0f);
            linearLayout2.bringToFront();
        }
        updateContextUi();
    }

    public void syncDbManual() {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionWalletHelper.reload(appCompatActivity);
            FusionChainHelper.reload(appCompatActivity);
            String strFindAddressByName = FusionWalletHelper.findAddressByName(this.mCurrentWalletName);
            if (strFindAddressByName == null || strFindAddressByName.isEmpty()) {
                this.mCurrentWalletName = FusionWalletHelper.getMasterName();
                this.mCurrentWalletAddress = FusionWalletHelper.getMasterAddress();
            } else {
                this.mCurrentWalletAddress = strFindAddressByName;
            }
            this.mCurrentSolWalletName = FusionWalletHelper.getMasterSolName();
            this.mCurrentSolWalletAddress = FusionWalletHelper.getMasterSolAddress();
            this.mCurrentTronWalletName = FusionWalletHelper.getMasterTronName();
            this.mCurrentTronWalletAddress = FusionWalletHelper.getMasterTronAddress();
            this.mCurrentSuiWalletName = FusionWalletHelper.getMasterSuiName();
            this.mCurrentSuiWalletAddress = FusionWalletHelper.getMasterSuiAddress();
            this.mCurrentAptosWalletName = FusionWalletHelper.getMasterAptosName();
            this.mCurrentAptosWalletAddress = FusionWalletHelper.getMasterAptosAddress();
            this.mCurrentIotaWalletName = FusionWalletHelper.getMasterIotaName();
            this.mCurrentIotaWalletAddress = FusionWalletHelper.getMasterIotaAddress();
            GhostBridgeInterface.setWallet(this.mCurrentWalletName, this.mCurrentWalletAddress);
            GhostBridgeInterface.setSolWallet(this.mCurrentSolWalletName, this.mCurrentSolWalletAddress);
            GhostBridgeInterface.setTronWallet(this.mCurrentTronWalletName, this.mCurrentTronWalletAddress);
            GhostBridgeInterface.setSuiWallet(this.mCurrentSuiWalletName, this.mCurrentSuiWalletAddress);
            GhostBridgeInterface.setAptosWallet(this.mCurrentAptosWalletName, this.mCurrentAptosWalletAddress);
            GhostBridgeInterface.setIotaWallet(this.mCurrentIotaWalletName, this.mCurrentIotaWalletAddress);
            updateMenuTexts();
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Database Dompet & Jaringan Disinkronkan!", 0).show();
        }
    }

    /* JADX WARN: Type inference failed for: r5v26, types: [android.app.Activity, com.termux.app.TermuxActivity] */
    public void syncVisibility() {
        WebView webView;
        int size;
        FrameLayout frameLayout;
        FrameLayout frameLayout2;
        WebView webView2;
        TerminalSession terminalSession;
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        if (termuxActivity.currentWallPage() != com.termux.app.wall.PaneWallPage.TERMINAL) {
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.GONE);
            }
            return;
        }
        ArrayList arrayList = null;
        int i = -1;
        TerminalSession currentSession = termuxActivity.getCurrentSession();
        if ((arrayList = this.mTabList) != null && (size = arrayList.size()) > 0) {
            int activeIdx = this.mActiveTab;
            if (activeIdx >= 0 && activeIdx < size) {
                FusionTab candidate = (FusionTab) arrayList.get(activeIdx);
                if (candidate != null && candidate.mBoundSession != null && candidate.mBoundSession == currentSession) {
                    i = activeIdx;
                }
            }
            if (i < 0 && currentSession != null) {
                for (int i2 = 0; i2 < size; i2++) {
                    FusionTab fusionTab = (FusionTab) arrayList.get(i2);
                    if (fusionTab != null && fusionTab.mBoundSession == currentSession) {
                        i = i2;
                        break;
                    }
                }
            }
            // Bind newly created tab that has no session yet
            if (i < 0 && currentSession != null) {
                for (int i2 = 0; i2 < size; i2++) {
                    FusionTab fusionTab = (FusionTab) arrayList.get(i2);
                    if (fusionTab != null && fusionTab.mBoundSession == null) {
                        fusionTab.mBoundSession = currentSession;
                        i = i2;
                        break;
                    }
                }
            }
            if (i >= 0) {
                this.mActiveTab = i;
                FusionTab fusionTab2 = (FusionTab) arrayList.get(i);
                WebView webView3 = fusionTab2.mWebView;
                this.mActiveWebView = webView3;
                FusionTabStateHelper.syncTabState(this, fusionTab2);
                for (int i3 = 0; i3 < size; i3++) {
                    FusionTab fusionTab3 = (FusionTab) arrayList.get(i3);
                    if (fusionTab3 != null && (webView2 = fusionTab3.mWebView) != null && i3 != i) {
                        webView2.setVisibility(8);
                        webView2.resumeTimers();
                    }
                }
                if (this.mPaneHost == null) {
                    initViews();
                }
                FrameLayout frameLayout3 = this.mPaneHost;
                if (frameLayout3 != null) {
                    frameLayout3.setVisibility(View.VISIBLE);
                    frameLayout3.setAlpha(1.0f);
                    frameLayout3.setTranslationX(0.0f);
                    frameLayout3.bringToFront();
                }
                if (fusionTab2.mIsMinimized) {
                    if (webView3 != null) {
                        webView3.setVisibility(8);
                    }
                    TextView textView = this.mMinPillBtn;
                    if (textView != null) {
                        textView.setText(" MAX ");
                    }
                    LinearLayout linearLayout = this.mMenuCard;
                    if (linearLayout != null) {
                        linearLayout.setVisibility(8);
                    }
                    FusionDrawerHelper.closeAllDrawers(this);
                    FusionRecPillHelper.hideRecPill();
                    View view = this.mTerminalPaneHost;
                    if (view != null) {
                        view.setVisibility(0);
                    }
                    termuxActivity.endWebViewExternalTextInput();
                    LinearLayout linearLayout2 = this.mControlPill;
                    if (linearLayout2 != null) {
                        if (linearLayout2.getParent() == null && this.mPaneHost != null) {
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                            layoutParams.setMargins(0, 8, 12, 0);
                            this.mPaneHost.addView(linearLayout2, layoutParams);
                        }
                        linearLayout2.setVisibility(0);
                        linearLayout2.setElevation(160.0f);
                        linearLayout2.setScaleX(1.0f);
                        linearLayout2.setScaleY(1.0f);
                        linearLayout2.setTranslationX(0.0f);
                        linearLayout2.setTranslationY(0.0f);
                        linearLayout2.bringToFront();
                    }
                    // RULE SAMA DENGAN mControlPill (floating kanan atas): mToolScroll TETAP AKTIF & VISIBLE!
                    if (this.mToolScroll == null) {
                        FusionToolPillHelper.initToolPill(this);
                    }
                    if (this.mToolScroll != null) {
                        if (this.mToolScroll.getParent() == null && this.mPaneHost != null) {
                            float density = this.mActivity.getResources().getDisplayMetrics().density;
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                            layoutParams.setMargins(0, (int) (58 * density), 10, 0);
                            this.mPaneHost.addView(this.mToolScroll, layoutParams);
                        }
                        if (this.mToolPill != null) {
                            this.mToolPill.setVisibility(View.VISIBLE);
                        }
                        this.mToolScroll.setVisibility(0);
                        this.mToolScroll.setElevation(160.0f);
                        this.mToolScroll.bringToFront();
                        FusionToolPillHelper.syncToolPillPosition(this);
                        FusionToolPillHelper.updateToolPillUi(this);
                    }
                    updateContextUi();
                    return;
                }
                TextView textView2 = this.mMinPillBtn;
                if (textView2 != null) {
                    textView2.setText(" MIN ");
                }
                if (webView3 != null) {
                    if (webView3.getParent() == null && (frameLayout2 = this.mPaneHost) != null) {
                        frameLayout2.addView(webView3, new FrameLayout.LayoutParams(-1, -1));
                    }
                    View view2 = this.mTerminalPaneHost;
                    if (view2 != null) {
                        view2.setVisibility(8);
                    }
                    webView3.setVisibility(0);
                    webView3.setElevation(0.0f);
                    webView3.bringToFront();
                    webView3.requestFocus();
                    webView3.requestFocusFromTouch();
                    termuxActivity.beginWebViewExternalTextInput();
                }
                LinearLayout linearLayout3 = this.mControlPill;
                if (linearLayout3 != null) {
                    if (linearLayout3.getParent() == null && (frameLayout = this.mPaneHost) != null) {
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                        layoutParams.setMargins(0, 8, 12, 0);
                        frameLayout.addView(linearLayout3, layoutParams);
                    }
                    linearLayout3.setVisibility(0);
                    linearLayout3.setElevation(160.0f);
                    linearLayout3.setScaleX(1.0f);
                    linearLayout3.setScaleY(1.0f);
                    linearLayout3.setTranslationX(0.0f);
                    linearLayout3.setTranslationY(0.0f);
                    linearLayout3.bringToFront();
                }
                if (this.mToolScroll == null) {
                    FusionToolPillHelper.initToolPill(this);
                }
                if (this.mToolScroll != null) {
                    if (this.mToolScroll.getParent() == null && this.mPaneHost != null) {
                        float density = this.mActivity.getResources().getDisplayMetrics().density;
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388661);
                        layoutParams.setMargins(0, (int) (58 * density), 10, 0);
                        this.mPaneHost.addView(this.mToolScroll, layoutParams);
                    }
                    if (this.mToolPill != null) {
                        this.mToolPill.setVisibility(View.VISIBLE);
                    }
                    this.mToolScroll.setVisibility(0);
                    this.mToolScroll.setElevation(160.0f);
                    this.mToolScroll.bringToFront();
                    FusionToolPillHelper.syncToolPillPosition(this);
                    FusionToolPillHelper.updateToolPillUi(this);
                }
                updateContextUi();
                FusionRecPillHelper.showRecPill(this);
                return;
            }
        }
        for (int i4 = 0; arrayList != null && i4 < arrayList.size(); i4++) {
            FusionTab fusionTab4 = (FusionTab) arrayList.get(i4);
            if (fusionTab4 != null && (webView = fusionTab4.mWebView) != null) {
                webView.setVisibility(8);
                webView.resumeTimers();
            }
        }
        this.mActiveWebView = null;
        if (this.mTerminalPaneHost != null) {
            this.mTerminalPaneHost.setVisibility(0);
        }
        LinearLayout linearLayout4 = this.mControlPill;
        if (linearLayout4 != null) {
            linearLayout4.setVisibility(8);
        }
        if (this.mToolScroll != null) {
            this.mToolScroll.setVisibility(8);
        }
        LinearLayout linearLayout5 = this.mMenuCard;
        if (linearLayout5 != null) {
            linearLayout5.setVisibility(8);
        }
        View view3 = this.mMenuBackdrop;
        if (view3 != null) {
            view3.setVisibility(8);
        }
        FusionDrawerHelper.closeAllDrawers(this);
        FusionRecPillHelper.hideRecPill();
        FusionToolPillHelper.hideToolPill(this);
        if (this.mPaneHost != null) {
            this.mPaneHost.setVisibility(View.GONE);
        }
        View view4 = this.mTerminalPaneHost;
        if (view4 != null) {
            view4.setVisibility(0);
        }
        termuxActivity.endWebViewExternalTextInput();
    }

    public void toggleAutoSign() {
        boolean z = !this.mIsAutoMode;
        this.mIsAutoMode = z;
        GhostBridgeInterface.setAuto(z);
        updateMenuTexts();
        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.evaluateJavascript("if(window.ethereum){window.ethereum.isAuto=" + z + ";}", null);
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Auto-Sign: ");
            sb.append(z ? "AKTIF" : "NONAKTIF");
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) sb.toString(), 0).show();
        }
    }

    public void toggleCollapsePill() {
        FusionTabStateHelper.toggleCollapsePill(this);
    }

    public void toggleDesktopMode() {
        FusionTabStateHelper.toggleDesktop(this);
    }

    public void toggleDevMode() {
        if (this.mIsDevMode) {
            this.mIsDevMode = false;
            WebView.setWebContentsDebuggingEnabled(false);
            try {
                new File("/data/data/com.termux/files/home/.webview_cdp_socket").delete();
            } catch (Exception unused) {
            }
            try {
                new File("/data/local/nhsystem/kali-arm64/root/webview_cdp_socket").delete();
            } catch (Exception unused2) {
            }
            TextView textView = this.mDevModeBtn;
            if (textView != null) {
                textView.setText("Dev: OFF");
                textView.setBackgroundColor(-4482645);
            }
            AppCompatActivity appCompatActivity = this.mActivity;
            if (appCompatActivity != null) {
                FusionToast.makeText((Context) appCompatActivity, (CharSequence) "CDP NONAKTIF (OFF)", 0).show();
                return;
            }
            return;
        }
        this.mIsDevMode = true;
        WebView.setWebContentsDebuggingEnabled(true);
        String str = "webview_devtools_remote_" + Process.myPid();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/data/data/com.termux/files/home/.webview_cdp_socket");
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception unused3) {
        }
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream("/data/local/nhsystem/kali-arm64/root/webview_cdp_socket");
            fileOutputStream2.write(str.getBytes());
            fileOutputStream2.close();
        } catch (Exception unused4) {
        }
        TextView textView2 = this.mDevModeBtn;
        if (textView2 != null) {
            textView2.setText("Dev: ON (CDP)");
            textView2.setBackgroundColor(-2263091);
        }
        AppCompatActivity appCompatActivity2 = this.mActivity;
        if (appCompatActivity2 != null) {
            FusionToast.makeText((Context) appCompatActivity2, (CharSequence) ("CDP AKTIF!\nSocket: @" + str + "\nForward: adb forward tcp:9222 localabstract:" + str), 1).show();
        }
    }

    public void toggleMacroRecording() {
        WebView webView = this.mActiveWebView;
        if (webView == null) {
            return;
        }
        if (this.mIsRecording) {
            this.mIsRecording = false;
            webView.evaluateJavascript("window.__FUSION_STOP_REC__ ? window.__FUSION_STOP_REC__() : '[]';", new MacroValueCallback(this));
            TextView textView = this.mRecBtn;
            if (textView != null) {
                textView.setText("Record Macro");
                textView.setBackgroundColor(-7820595);
                return;
            }
            return;
        }
        this.mIsRecording = true;
        webView.evaluateJavascript("window.__FUSION_START_REC__ && window.__FUSION_START_REC__();", null);
        TextView textView2 = this.mRecBtn;
        if (textView2 != null) {
            textView2.setText("Stop Macro");
            textView2.setBackgroundColor(-12255232);
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "Perekaman Makro Dimulai...", 0).show();
        }
    }

    public void toggleMenuCard() {
        FusionDrawerHelper.toggleMenuDrawer(this);
    }

    public void toggleMinimize() {
        FusionTabStateHelper.toggleMinimize(this);
    }

    public void triggerAiDevBridge() {
        WebView webView = this.mActiveWebView;
        if (webView == null) {
            return;
        }
        webView.evaluateJavascript("window.__AI_DEVBRIDGE_INSPECT__ ? window.__AI_DEVBRIDGE_INSPECT__() : JSON.stringify({url: window.location.href, title: document.title});", this);
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            FusionToast.makeText((Context) appCompatActivity, (CharSequence) "AI DevBridge: Mengekstrak DOM...", 0).show();
        }
    }

    public void updateContextUi() {
        int size;
        ArrayList arrayList = this.mTabList;
        if (arrayList == null || (size = arrayList.size()) <= 0) {
            TextView textView = this.mTabBtn;
            if (textView != null) {
                textView.setText(" TAB 1/1 ");
            }
            TextView textView2 = this.mContextStatusText;
            if (textView2 != null) {
                textView2.setText("Konteks: Tab 1");
            }
        } else {
            int i = this.mActiveTab;
            if (i < 0) {
                i = 0;
                this.mActiveTab = 0;
            } else if (i >= size) {
                i = size - 1;
                this.mActiveTab = i;
        FusionToolPillHelper.collapse(this);
            }
            int i2 = i + 1;
            TextView textView3 = this.mTabBtn;
            if (textView3 != null) {
                textView3.setText(" TAB " + i2 + "/" + size + " ");
            }
            TextView textView4 = this.mContextStatusText;
            if (textView4 != null) {
                textView4.setText("Konteks: Tab " + i2 + " (" + size + " Aktif)");
            }
        }
        updateMenuTexts();
    }

    /* JADX WARN: Code duplicated, block: B:22:0x009a  */
    /* JADX WARN: Code duplicated, block: B:24:0x009e  */
    /* JADX WARN: Code duplicated, block: B:25:0x00a4  */
    /* JADX WARN: Code duplicated, block: B:27:0x00ad  */
    /* JADX WARN: Code duplicated, block: B:29:0x00b1  */
    /* JADX WARN: Code duplicated, block: B:30:0x00bd  */
    /* JADX WARN: Code duplicated, block: B:42:? A[RETURN, SYNTHETIC] */
    public void updateMenuTexts() {
        TextView textView;
        TextView textView2;
        TextView textView3 = this.mChainInfoText;
        if (textView3 != null) {
            textView3.setText("Network: " + FusionChainHelper.getCurrentChainName() + " (" + FusionChainHelper.getCurrentChainId() + ") [v]");
        }
        TextView textView4 = this.mWalletInfoText;
        if (textView4 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Active: ");
            sb.append(this.mCurrentWalletName);
            boolean isSol = FusionChainHelper.isSolana();
            boolean isTron = FusionChainHelper.isTron();
            boolean isSui = FusionChainHelper.isSui();
            boolean isAptos = FusionChainHelper.isAptos();
            boolean isIota = FusionChainHelper.isIota();
            if (isSol) {
                sb.append("\nSOL: ");
                String sol = this.mCurrentSolWalletAddress;
                if (sol == null || sol.isEmpty()) {
                    sol = FusionWalletHelper.getMasterSolAddress();
                }
                if (sol != null && !sol.isEmpty()) {
                    if (sol.length() <= 8) {
                        sb.append(sol);
                    } else {
                        sb.append(sol.substring(0, 4));
                        sb.append("...");
                        sb.append(sol.substring(sol.length() - 4));
                    }
                } else {
                    sb.append("-");
                }
            } else if (isTron) {
                sb.append("\nTRX: ");
                String tron = this.mCurrentTronWalletAddress;
                if (tron == null || tron.isEmpty()) {
                    tron = FusionWalletHelper.getMasterTronAddress();
                }
                if (tron != null && !tron.isEmpty()) {
                    if (tron.length() <= 8) {
                        sb.append(tron);
                    } else {
                        sb.append(tron.substring(0, 4));
                        sb.append("...");
                        sb.append(tron.substring(tron.length() - 4));
                    }
                } else {
                    sb.append("-");
                }
            } else if (isSui) {
                sb.append("\nSUI: ");
                String sui = this.mCurrentSuiWalletAddress;
                if (sui == null || sui.isEmpty()) {
                    sui = FusionWalletHelper.getMasterSuiAddress();
                }
                if (sui != null && !sui.isEmpty()) {
                    if (sui.length() <= 10) {
                        sb.append(sui);
                    } else {
                        sb.append(sui.substring(0, 6));
                        sb.append("...");
                        sb.append(sui.substring(sui.length() - 4));
                    }
                } else {
                    sb.append("-");
                }
            } else if (isAptos) {
                sb.append("\nAPT: ");
                String apt = this.mCurrentAptosWalletAddress;
                if (apt == null || apt.isEmpty()) {
                    apt = FusionWalletHelper.getMasterAptosAddress();
                }
                if (apt != null && !apt.isEmpty()) {
                    if (apt.length() <= 10) {
                        sb.append(apt);
                    } else {
                        sb.append(apt.substring(0, 6));
                        sb.append("...");
                        sb.append(apt.substring(apt.length() - 4));
                    }
                } else {
                    sb.append("-");
                }
            } else if (isIota) {
                sb.append("\nIOTA: ");
                String iota = this.mCurrentIotaWalletAddress;
                if (iota == null || iota.isEmpty()) {
                    iota = FusionWalletHelper.getMasterIotaAddress();
                }
                if (iota != null && !iota.isEmpty()) {
                    if (iota.length() <= 12) {
                        sb.append(iota);
                    } else {
                        sb.append(iota.substring(0, 8));
                        sb.append("...");
                        sb.append(iota.substring(iota.length() - 4));
                    }
                } else {
                    sb.append("-");
                }
            } else {
                sb.append("\nAddress: ");
                String str = this.mCurrentWalletAddress;
                if (str == null || str.length() <= 10) {
                    sb.append(str);
                } else {
                    sb.append(str.substring(0, 6));
                    sb.append("...");
                    sb.append(str.substring(str.length() - 4));
                }
            }
            textView4.setText(sb.toString());
        }
        TextView textView5 = this.mAutoSignBtn;
        if (textView5 != null) {
            if (this.mIsAutoMode) {
                textView5.setText("Auto-Sign: AKTIF");
                textView5.setBackgroundColor(-13730510);
            } else {
                textView5.setText("Auto-Sign: MATI");
                textView5.setBackgroundColor(-5592406);
            }
        }
        TextView desktopBtn = this.mDesktopBtn;
        if (desktopBtn != null) {
            if (this.mIsDesktopMode) {
                desktopBtn.setText("Mobile");
                desktopBtn.setBackgroundColor(0xFF2E7D32);
                desktopBtn.setTextColor(0xFFFFFFFF);
            } else {
                desktopBtn.setText("Desktop");
                desktopBtn.setBackgroundColor(-4482645);
                desktopBtn.setTextColor(0xFFFFFFFF);
            }
        }
        TextView recBtn = this.mRecBtn;
        if (recBtn != null) {
            if (this.mIsRecording) {
                recBtn.setText("Stop Macro");
                recBtn.setBackgroundColor(-12255232);
            } else {
                recBtn.setText("Record Macro");
                recBtn.setBackgroundColor(-7820595);
            }
        }
        boolean zCheckRpcDaemon = checkRpcDaemon();
        TextView textView6 = this.mStatusText;
        if (textView6 != null) {
            if (zCheckRpcDaemon) {
                textView6.setText("* RPC: ONLINE (3333)");
                textView6.setTextColor(-16718218);
            } else {
                textView6.setText("* RPC: OFFLINE");
                textView6.setTextColor(-5592406);
            }
        }
        TextView textView7 = this.mStartDaemonBtn;
        if (textView7 != null) {
            if (zCheckRpcDaemon) {
                textView7.setText("[OK] RPC Aktif");
                textView7.setBackgroundColor(-13730510);
            } else {
                textView7.setText("[Start Wallet (" + FusionWalletHelper.getDetectedEngine() + ")]");
                textView7.setBackgroundColor(-12285730);
            }
        }
        FusionToolPillHelper.updateToolPillUi(this);
        if (this.mActivity != null) {
            this.mActivity.refreshTerminalWindowBar();
        }
    }
}
