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
import androidx.core.app.NotificationCompat;
import java.net.HttpURLConnection;
import java.net.URL;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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
    public String mCurrentWalletName = "Master .env";
    public String mCurrentWalletAddress = "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359";
    public String mCurrentChainKey = "base";
    public String mWcUri = "";
    public boolean mIsGhostMode = false;
    public boolean mIsDesktopMode = false;
    public boolean mIsAutoMode = true;
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

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
            this.mManager = fusionWebViewManager;
            this.mUrl = str;
            this.mProfile = str2;
            this.mWalletName = str3;
            this.mChainKey = str4;
            this.mIsGhost = z;
            this.mIsDesktop = z2;
            this.mIsAuto = z3;
            this.mWcUri = str5;
        }

        @Override // java.lang.Runnable
        public void run() {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null) {
                return;
            }
            fusionWebViewManager.openUrlInternal(this.mUrl, this.mProfile, this.mWalletName, this.mChainKey, this.mIsGhost, this.mIsDesktop, this.mIsAuto, this.mWcUri);
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
            view.requestFocus();
            view.requestFocusFromTouch();
            FusionWebViewManager mgr = FusionWebViewManager.sInstance;
            if (mgr != null && mgr.mActivity != null) {
                mgr.mActivity.beginWebViewExternalTextInput();
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
            fusionWebViewManager.mActivity = termuxActivity;
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
            frameLayout.bringToFront();
        }
        if (str == null || str.length() == 0) {
            str = "https://google.com";
        }
        FusionTab fusionTabCreateTabInternal = createTabInternal(str, "default", "", "base", true, false, false, "");
        switchTab(this.mTabList.size() - 1);
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(0);
            linearLayout.setElevation(160.0f);
            linearLayout.bringToFront();
        }
        fusionTabCreateTabInternal.mWebView.loadUrl(str);
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Cache Tab Aktif Berhasil Dibersihkan!", 0).show();
        }
    }

    public String switchToNextWallet() {
        String[] names = FusionWalletHelper.getNames();
        if (names == null || names.length == 0) return "";
        int curIdx = FusionWalletHelper.findIndexByName(this.mCurrentWalletName);
        int nextIdx = (curIdx + 1) % names.length;
        String nextName = names[nextIdx];
        String nextAddr = FusionWalletHelper.getAddress(nextIdx);

        this.mCurrentWalletName = nextName;
        this.mCurrentWalletAddress = nextAddr;
        GhostBridgeInterface.setWallet(nextName, nextAddr);

        final String fNextAddr = nextAddr;
        final String fNextName = nextName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GhostBridgeInterface.forwardToDaemon("{\"method\":\"fusion_switchWallet\",\"params\":[\"" + fNextAddr + "\",\"" + fNextName + "\"]}");
                } catch (Throwable ignored) {}
            }
        }).start();

        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            appCompatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateContextUi();
                    Toast.makeText(appCompatActivity, "Dompet Diubah ke: " + nextName, Toast.LENGTH_SHORT).show();
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
                            mActiveWebView.getSettings().setUserAgentString(dev.userAgent);
                            mActiveWebView.evaluateJavascript(
                                "window.__FUSION_DEVICE__ = " + dev.toJsonString() + ";" +
                                "if(window.__applyFingerprintFaker) window.__applyFingerprintFaker();", null
                            );
                            Toast.makeText(appCompatActivity, "Fingerprint: " + dev.name, Toast.LENGTH_SHORT).show();
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
        FusionDrawerHelper.closeAllDrawers(this);
        FusionRecPillHelper.hideRecPill();
        View view2 = this.mTerminalPaneHost;
        if (view2 != null) {
            view2.setVisibility(0);
        }
        TermuxActivity r0 = this.mActivity;
        if (r0 != null) {
            r0.endWebViewExternalTextInput();
        }
    }

    public void copyAddressToClipboard(String str) {
        TermuxActivity r0 = this.mActivity;
        if (r0 == null) {
            return;
        }
        try {
            ((ClipboardManager) r0.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("wallet_address", str));
            Toast.makeText((Context) r0, "Alamat disalin: " + str, 0).show();
        } catch (Exception unused) {
        }
    }

    public FusionTab createTabInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
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
        if (z2) {
            settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
        } else {
            FusionProfileManager.applyProfile(str2, webView, false);
        }
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setClickable(true);
        webView.setEnabled(true);
        webView.setOnTouchListener(new TouchListener());
        webView.setWebChromeClient(new FusionWebChromeClient());
        webView.setWebViewClient(new FusionWebViewClient(this));
        webView.addJavascriptInterface(new GhostBridgeInterface(), "GhostBridge");
        FrameLayout frameLayout = this.mPaneHost;
        if (frameLayout != null) {
            frameLayout.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        }
        webView.setElevation(0.0f);
        int i = this.mNextTabId;
        this.mNextTabId = i + 1;
        FusionTab fusionTab = new FusionTab(i, str, str2, webView, z, z2, z3);
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity != null) {
            fusionTab.mBoundSession = termuxActivity.getCurrentSession();
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
        LinearLayout linearLayout2 = this.mMenuCard;
        if (linearLayout2 != null) {
            linearLayout2.setVisibility(8);
        }
        WebView webView = this.mActiveWebView;
        if (webView != null) {
            webView.setVisibility(8);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.content.Context, com.termux.app.TermuxActivity] */
    /* JADX WARN: Type inference failed for: r7v1 */
    public void initViews() {
        ViewParent parent;
        TermuxActivity r0 = this.mActivity;
        if (r0 == null) {
            return;
        }
        FusionWalletHelper.init(r0);
        FusionChainHelper.init(r0);
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            WebView.setWebContentsDebuggingEnabled(true);
        } catch (Exception unused) {
        }
        this.mPaneHost = (FrameLayout) r0.findViewById(com.termux.R.id.terminal_surface_host);
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
            LinearLayout linearLayout6 = new LinearLayout(this.mActivity);
            linearLayout6.setOrientation(0);
            linearLayout6.setPadding(0, 0, 0, 10);
            TextView textView7 = new TextView(this.mActivity);
            textView7.setText("FUSION WEB3");
            textView7.setTextColor(-1);
            textView7.setTextSize(2, 14.0f);
            textView7.setTypeface(null, 1);
            textView7.setGravity(19);
            linearLayout6.addView(textView7, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView textView8 = new TextView(this.mActivity);
            textView8.setText("  X  ");
            textView8.setTextColor(-13124);
            textView8.setTextSize(2, 14.0f);
            textView8.setTypeface(null, 1);
            textView8.setPadding(10, 6, 10, 6);
            textView8.setClickable(z);
            textView8.setOnClickListener(new CloseMenuClick(this));
            linearLayout6.addView(textView8);
            this.mMenuCard.addView(linearLayout6);

            ScrollView menuScrollView = new ScrollView(this.mActivity);
            menuScrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
            menuScrollView.setVerticalScrollBarEnabled(false);
            LinearLayout menuContent = new LinearLayout(this.mActivity);
            menuContent.setOrientation(1);

            LinearLayout linearLayout7 = new LinearLayout(this.mActivity);
            linearLayout7.setOrientation(0);
            linearLayout7.setPadding(0, 4, 0, 6);
            TextView textView9 = new TextView(this.mActivity);
            this.mStatusText = textView9;
            textView9.setText("* RPC: 127.0.0.1:3333");
            textView9.setTextColor(-5592406);
            textView9.setTextSize(2, 11.0f);
            textView9.setGravity(19);
            textView9.setClickable(z);
            textView9.setOnClickListener(this);
            linearLayout7.addView(textView9, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView textView10 = new TextView(this.mActivity);
            this.mStartDaemonBtn = textView10;
            textView10.setText("[Start Wallet (" + FusionWalletHelper.getDetectedEngine() + ")]");
            textView10.setBackground(FusionUiHelper.createRounded(-12285730, -12762806, 3.0f));
            textView10.setTextColor(-1);
            textView10.setTextSize(2, 11.5f);
            textView10.setGravity(17);
            textView10.setPadding(10, 8, 10, 8);
            textView10.setClickable(z);
            textView10.setOnClickListener(this);
            linearLayout7.addView(textView10);
            menuContent.addView(linearLayout7);
            TextView textView11 = new TextView(this.mActivity);
            this.mChainInfoText = textView11;
            textView11.setText("Network: Base (8453) [v]");
            textView11.setTextColor(-2258875);
            textView11.setTextSize(2, 11.0f);
            textView11.setPadding(0, 2, 0, 3);
            menuContent.addView(textView11);
            TextView textView12 = new TextView(this.mActivity);
            this.mWalletInfoText = textView12;
            textView12.setText("Active: Master\nAddress: 0x87A6...b359");
            textView12.setTextColor(-3355444);
            textView12.setTextSize(2, 11.0f);
            textView12.setPadding(0, 2, 0, 8);
            menuContent.addView(textView12);
            LinearLayout linearLayout8 = new LinearLayout(this.mActivity);
            linearLayout8.setOrientation(0);
            linearLayout8.setPadding(0, 3, 0, 3);
            TextView textView13 = new TextView(this.mActivity);
            this.mSwitchWalletBtn = textView13;
            textView13.setText("Ganti Dompet");
            textView13.setBackground(FusionUiHelper.createRounded(-12285730, -12762806, 3.0f));
            textView13.setTextColor(-1);
            textView13.setTextSize(2, 12.0f);
            textView13.setGravity(17);
            textView13.setPadding(10, 10, 10, 10);
            textView13.setClickable(z);
            textView13.setOnClickListener(new SwitchWalletClick(this));
            LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams9.rightMargin = 6;
            linearLayout8.addView(textView13, layoutParams9);
            TextView textView14 = new TextView(this.mActivity);
            this.mSwitchChainBtn = textView14;
            textView14.setText("Network");
            textView14.setBackground(FusionUiHelper.createRounded(-2258875, -12762806, 3.0f));
            textView14.setTextColor(-1);
            textView14.setTextSize(2, 12.0f);
            textView14.setGravity(17);
            textView14.setPadding(10, 10, 10, 10);
            textView14.setClickable(z);
            textView14.setOnClickListener(new SwitchChainClick(this));
            linearLayout8.addView(textView14, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout8);
            TextView textView15 = new TextView(this.mActivity);
            textView15.setText("Lokasi GhostWallet / Load DB");
            FusionUiHelper.setSolidBtn(textView15);
            textView15.setTextSize(2, 12.0f);
            textView15.setGravity(17);
            textView15.setPadding(10, 10, 10, 10);
            textView15.setClickable(true);
            textView15.setOnClickListener(new FusionWalletPathBtnClick(this));
            LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams10.setMargins(0, 5, 0, 5);
            menuContent.addView(textView15, layoutParams10);
            LinearLayout linearLayout9 = new LinearLayout(this.mActivity);
            linearLayout9.setOrientation(0);
            linearLayout9.setPadding(0, 3, 0, 3);
            TextView textView16 = new TextView(this.mActivity);
            textView16.setText("Sync DB");
            textView16.setBackground(FusionUiHelper.createRounded(-5605427, -12762806, 3.0f));
            textView16.setTextColor(-1);
            textView16.setTextSize(2, 12.0f);
            textView16.setGravity(17);
            textView16.setPadding(10, 10, 10, 10);
            textView16.setClickable(z);
            textView16.setOnClickListener(new SyncDbClick(this));
            LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams11.rightMargin = 6;
            linearLayout9.addView(textView16, layoutParams11);
            TextView textView17 = new TextView(this.mActivity);
            this.mCopyAddressBtn = textView17;
            textView17.setText("Salin Alamat");
            textView17.setBackground(FusionUiHelper.createRounded(-14013884, -12762806, 3.0f));
            textView17.setTextColor(-1);
            textView17.setTextSize(2, 12.0f);
            textView17.setGravity(17);
            textView17.setPadding(10, 10, 10, 10);
            textView17.setClickable(z);
            textView17.setOnClickListener(new CopyAddressClick(this));
            linearLayout9.addView(textView17, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout9);
            LinearLayout linearLayout10 = new LinearLayout(this.mActivity);
            linearLayout10.setOrientation(0);
            linearLayout10.setPadding(0, 3, 0, 3);
            TextView textView18 = new TextView(this.mActivity);
            this.mRecBtn = textView18;
            textView18.setText("Record Macro");
            textView18.setBackground(FusionUiHelper.createRounded(-7820595, -12762806, 3.0f));
            textView18.setTextColor(-1);
            textView18.setTextSize(2, 12.0f);
            textView18.setGravity(17);
            textView18.setPadding(10, 10, 10, 10);
            textView18.setClickable(z);
            textView18.setOnClickListener(new RecClick(this));
            LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams12.rightMargin = 6;
            linearLayout10.addView(textView18, layoutParams12);
            TextView textView19 = new TextView(this.mActivity);
            this.mRunMacroBtn = textView19;
            textView19.setText("Play Macro");
            textView19.setBackground(FusionUiHelper.createRounded(-2254404, -12762806, 3.0f));
            textView19.setTextColor(-1);
            textView19.setTextSize(2, 12.0f);
            textView19.setGravity(17);
            textView19.setPadding(10, 10, 10, 10);
            textView19.setClickable(z);
            textView19.setOnClickListener(this);
            linearLayout10.addView(textView19, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout10);
            LinearLayout linearLayout11 = new LinearLayout(this.mActivity);
            linearLayout11.setOrientation(0);
            linearLayout11.setPadding(0, 3, 0, 3);
            TextView textView20 = new TextView(this.mActivity);
            this.mDevModeBtn = textView20;
            textView20.setText("Dev Mode: CDP");
            textView20.setBackground(FusionUiHelper.createRounded(-4482645, -12762806, 3.0f));
            textView20.setTextColor(-1);
            textView20.setTextSize(2, 12.0f);
            textView20.setGravity(17);
            textView20.setPadding(10, 10, 10, 10);
            textView20.setClickable(z);
            textView20.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams13 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams13.rightMargin = 6;
            linearLayout11.addView(textView20, layoutParams13);
            TextView textView21 = new TextView(this.mActivity);
            this.mAiBridgeBtn = textView21;
            textView21.setText("AI DevBridge");
            textView21.setBackground(FusionUiHelper.createRounded(-5583735, -12762806, 3.0f));
            textView21.setTextColor(-1);
            textView21.setTextSize(2, 12.0f);
            textView21.setGravity(17);
            textView21.setPadding(10, 10, 10, 10);
            textView21.setClickable(z);
            textView21.setOnClickListener(this);
            linearLayout11.addView(textView21, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout11);
            LinearLayout linearLayout12 = new LinearLayout(this.mActivity);
            linearLayout12.setOrientation(0);
            linearLayout12.setPadding(0, 3, 0, 3);
            TextView textView22 = new TextView(this.mActivity);
            this.mRandomFpBtn = textView22;
            textView22.setText("Acak ID / FP");
            textView22.setBackground(FusionUiHelper.createRounded(-7820578, -12762806, 3.0f));
            textView22.setTextColor(-1);
            textView22.setTextSize(2, 12.0f);
            textView22.setGravity(17);
            textView22.setPadding(10, 10, 10, 10);
            textView22.setClickable(z);
            textView22.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams14 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams14.rightMargin = 6;
            linearLayout12.addView(textView22, layoutParams14);
            TextView textView23 = new TextView(this.mActivity);
            this.mOpacityBtn = textView23;
            textView23.setText("Opacity: 100%");
            textView23.setBackground(FusionUiHelper.createRounded(-4473958, -12762806, 3.0f));
            textView23.setTextColor(-1);
            textView23.setTextSize(2, 12.0f);
            textView23.setGravity(17);
            textView23.setPadding(10, 10, 10, 10);
            textView23.setClickable(z);
            textView23.setOnClickListener(this);
            linearLayout12.addView(textView23, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout12);
            LinearLayout linearLayout13 = new LinearLayout(this.mActivity);
            linearLayout13.setOrientation(0);
            linearLayout13.setPadding(0, 3, 0, 3);
            TextView textView24 = new TextView(this.mActivity);
            textView24.setText("Connect WC");
            textView24.setBackground(FusionUiHelper.createRounded(-3359880, -12762806, 3.0f));
            textView24.setTextColor(-1);
            textView24.setTextSize(2, 12.0f);
            textView24.setGravity(17);
            textView24.setPadding(10, 10, 10, 10);
            textView24.setClickable(z);
            textView24.setOnClickListener(new WcClick(this));
            LinearLayout.LayoutParams layoutParams15 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams15.rightMargin = 6;
            linearLayout13.addView(textView24, layoutParams15);
            TextView textView25 = new TextView(this.mActivity);
            this.mDesktopBtn = textView25;
            textView25.setText("Desktop");
            textView25.setBackground(FusionUiHelper.createRounded(-4482645, -12762806, 3.0f));
            textView25.setTextColor(-1);
            textView25.setTextSize(2, 12.0f);
            textView25.setGravity(17);
            textView25.setPadding(10, 10, 10, 10);
            textView25.setClickable(z);
            textView25.setOnClickListener(new DesktopClick(this));
            linearLayout13.addView(textView25, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout13);
            LinearLayout linearLayout14 = new LinearLayout(this.mActivity);
            linearLayout14.setOrientation(0);
            linearLayout14.setPadding(0, 10, 0, 6);
            TextView textView26 = new TextView(this.mActivity);
            textView26.setText("< Back");
            FusionUiHelper.setSolidBtn(textView26);
            textView26.setTextSize(2, 11.5f);
            textView26.setGravity(17);
            textView26.setPadding(6, 10, 6, 10);
            textView26.setClickable(z);
            textView26.setOnClickListener(new BackClick(this));
            LinearLayout.LayoutParams lpBack = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpBack.rightMargin = 4;
            linearLayout14.addView(textView26, lpBack);
            TextView textView27 = new TextView(this.mActivity);
            textView27.setText("Reload");
            FusionUiHelper.setSolidBtn(textView27);
            textView27.setTextSize(2, 11.5f);
            textView27.setGravity(17);
            textView27.setPadding(6, 10, 6, 10);
            textView27.setClickable(z);
            textView27.setOnClickListener(new ReloadClick(this));
            LinearLayout.LayoutParams lpReload = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpReload.rightMargin = 4;
            linearLayout14.addView(textView27, lpReload);
            TextView textView28 = new TextView(this.mActivity);
            textView28.setText("Cache");
            textView28.setBackground(FusionUiHelper.createRounded(-6702114, -12762806, 3.0f));
            textView28.setTextColor(-1);
            textView28.setTextSize(2, 11.5f);
            textView28.setGravity(17);
            textView28.setPadding(6, 10, 6, 10);
            textView28.setClickable(z);
            textView28.setOnClickListener(new ClearCacheClick(this));
            LinearLayout.LayoutParams lpCache = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lpCache.rightMargin = 4;
            linearLayout14.addView(textView28, lpCache);
            TextView textView29 = new TextView(this.mActivity);
            textView29.setText("X Tutup");
            FusionUiHelper.setSolidAccentBtn(textView29, -13124, -13947597);
            textView29.setTextSize(2, 11.5f);
            textView29.setGravity(17);
            textView29.setPadding(6, 10, 6, 10);
            textView29.setClickable(z);
            textView29.setOnClickListener(new CloseClick(this));
            linearLayout14.addView(textView29, new LinearLayout.LayoutParams(0, -2, 1.0f));
            menuContent.addView(linearLayout14);
            LinearLayout linearLayout15 = new LinearLayout(this.mActivity);
            linearLayout15.setOrientation(1);
            linearLayout15.setPadding(0, 8, 0, 4);
            TextView textView30 = new TextView(this.mActivity);
            this.mContextStatusText = textView30;
            textView30.setText("Konteks: 1 Aktif (shaqilla)");
            textView30.setTextColor(-8930321);
            textView30.setTextSize(2, 11.0f);
            textView30.setPadding(0, 0, 0, 4);
            linearLayout15.addView(textView30, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout linearLayout16 = new LinearLayout(this.mActivity);
            linearLayout16.setOrientation(0);
            linearLayout16.setPadding(0, 2, 0, 2);
            TextView textView31 = new TextView(this.mActivity);
            this.mNewWorkerBtn = textView31;
            textView31.setText("+ Worker (Guest)");
            textView31.setBackground(FusionUiHelper.createRounded(-4482645, -12762806, 3.0f));
            textView31.setTextColor(-1);
            textView31.setTextSize(2, 11.5f);
            textView31.setGravity(17);
            textView31.setPadding(8, 10, 8, 10);
            textView31.setClickable(true);
            textView31.setOnClickListener(this);
            LinearLayout.LayoutParams layoutParams16 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams16.rightMargin = 6;
            linearLayout16.addView(textView31, layoutParams16);
            TextView textView32 = new TextView(this.mActivity);
            this.mKillWorkerBtn = textView32;
            textView32.setText("Kill Worker");
            textView32.setBackground(FusionUiHelper.createRounded(-5583667, -12762806, 3.0f));
            textView32.setTextColor(-1);
            textView32.setTextSize(2, 11.5f);
            textView32.setGravity(17);
            textView32.setPadding(8, 10, 8, 10);
            textView32.setClickable(true);
            textView32.setOnClickListener(this);
            linearLayout16.addView(textView32, new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayout15.addView(linearLayout16, new LinearLayout.LayoutParams(-1, -2));
            menuContent.addView(linearLayout15);

            menuScrollView.addView(menuContent);
            this.mMenuCard.addView(menuScrollView);

            TextView menuCloseBtn = new TextView(this.mActivity);
            menuCloseBtn.setText("TUTUP");
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
            this.mMenuCard.setVisibility(8);
            FrameLayout frameLayout4 = this.mPaneHost;
            if (frameLayout4 != null) {
                frameLayout4.addView(this.mControlPill, layoutParams);
                this.mPaneHost.addView(this.mMenuCard, layoutParams8);
            }
        }
        updateMenuTexts();
        FusionRecPillHelper.initAndShowRecPill(this);
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

    public void injectScripts(WebView webView) {
        if (webView == null) {
            return;
        }
        FusionProfileManager.DeviceProfile dev = FusionProfileManager.getDeviceForProfile(this.mCurrentProfile);
        webView.evaluateJavascript("window.__FUSION_PROFILE__ = '" + this.mCurrentProfile + "'; window.__FUSION_DEVICE__ = " + dev.toJsonString() + "; window.__FUSION_WALLET_NAME__ = '" + this.mCurrentWalletName + "'; window.__FUSION_WALLET_ADDRESS__ = '" + this.mCurrentWalletAddress + "'; window.__ACTIVE_AI_ENGINE__ = '" + FusionAiHelper.getActiveEngine() + "';", null);
        injectAssetScript(webView, "fingerprint_faker.js");
        injectAssetScript(webView, "ghost_injected.js");
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

    public void killTabDirect(int i) {
        WebView webView;
        ArrayList arrayList = this.mTabList;
        if (arrayList == null) {
            return;
        }
        int size = arrayList.size();
        if (i < 0 || i >= size) {
            return;
        }
        FusionTab fusionTab = (FusionTab) arrayList.remove(i);
        if (fusionTab != null && (webView = fusionTab.mWebView) != null) {
            webView.setVisibility(8);
            FrameLayout frameLayout = this.mPaneHost;
            if (frameLayout != null) {
                frameLayout.removeView(webView);
            }
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
        if (i2 > i) {
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
        if (webView != null) {
            String chainIdHex = FusionChainHelper.getChainIdHex();
            webView.evaluateJavascript("if(window.ethereum){window.ethereum.chainId='" + chainIdHex + "';window.ethereum.networkVersion='" + FusionChainHelper.getChainIdDec() + "';window.dispatchEvent(new CustomEvent('chainChanged',{detail:'" + chainIdHex + "'}));}", null);
        }
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            Toast.makeText((Context) appCompatActivity, (CharSequence) ("Jaringan: " + FusionChainHelper.getCurrentChainName()), 0).show();
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
                FusionTab activeTab = FusionTabStateHelper.getActiveTab(FusionWebViewManager.this);
                if (activeTab != null && activeTab.mIsMinimized) {
                    FusionTabStateHelper.toggleMinimize(FusionWebViewManager.this);
                } else if (mActiveWebView != null) {
                    mActiveWebView.setVisibility(View.VISIBLE);
                    mActiveWebView.bringToFront();
                    if (mTerminalPaneHost != null) {
                        mTerminalPaneHost.setVisibility(View.GONE);
                    }
                    if (mPaneHost != null) {
                        mPaneHost.setVisibility(View.VISIBLE);
                        mPaneHost.bringToFront();
                    }
                    if (mControlPill != null) {
                        mControlPill.bringToFront();
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "* RPC: ONLINE (127.0.0.1:3333)", 0).show();
        } else {
            startWalletDaemon();
        }
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
        }
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new OpenRunnable(this, str, str2, str3, str4, z, z2, z3, str5));
    }

    public void openUrl(String str, String str2, boolean z, boolean z2) {
        openConfig(str, str2, "", "base", z, z2, false, "");
    }

    public void openUrl(String str, String str2, boolean z, boolean z2, String str3) {
        openConfig(str, str2, str3, "base", z, z2, false, "");
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        TerminalSession currentSession = null;
        initViews();
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
                    i = i2;
                    break;
                }
            }
        }
        if (i >= 0) {
            FusionTab fusionTab2 = (FusionTab) this.mTabList.get(i);
            fusionTab2.mUrl = str;
            fusionTab2.mProfile = str2;
            switchTab(i);
            FusionProfileManager.applyProfile(str2, fusionTab2.mWebView, z2);
            fusionTab2.mWebView.loadUrl(str);
        } else {
            FusionTab fusionTabCreateTabInternal = createTabInternal(str, str2, str3, str4, z, z2, z3, str5);
            switchTab(this.mTabList.size() - 1);
            FusionProfileManager.applyProfile(str2, fusionTabCreateTabInternal.mWebView, z2);
            fusionTabCreateTabInternal.mWebView.loadUrl(str);
        }
        syncVisibility();
        updateContextUi();
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

    public boolean onBackPressed() {
        if (this.mMenuCard != null && this.mMenuCard.getVisibility() == View.VISIBLE) {
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
        WebView webView;
        if (this.mActiveTab != 1 || (webView = this.mWorkerWebView) == null) {
            webView = this.mActiveWebView;
        }
        return (webView != null && webView.getVisibility() == View.VISIBLE)
            || (this.mMenuCard != null && this.mMenuCard.getVisibility() == View.VISIBLE);
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Buka Rekaman...", 0).show();
        }
    }

    public void stopMacro() {
        if (this.mIsRecording) {
            toggleMacroRecording();
        }
        evalOnActiveTab("if(window.AgyBridge && window.AgyBridge.stop){window.AgyBridge.stop();}if(window.AgyBatchRunner && window.AgyBatchRunner.stop){window.AgyBatchRunner.stop();}if(window.AgyRecorder && window.AgyRecorder.cancel){window.AgyRecorder.cancel();}");
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Menghentikan Makro...", 0).show();
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "AI DevBridge: DOM tersimpan di ~/.webview_bridge_dump.json & disalin!", 1).show();
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Makro Berhasil Disimpan & Disalin!", 1).show();
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
            Toast.makeText((Context) r1, "[OK] GhostWallet Aktif di Port 3333!", 0).show();
            return;
        }
        TermuxIpcServer.injectCommand(r1, FusionWalletHelper.getStartWalletDaemonCommand(), true);
        Toast.makeText((Context) r1, "Memulai GhostWallet di terminal (port 3333)...", 0).show();
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
        for (int i2 = 0; i2 < size; i2++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i2);
            if (i2 == i) {
                TermuxActivity termuxActivity = this.mActivity;
                if (termuxActivity != null) {
                    fusionTab.mBoundSession = termuxActivity.getCurrentSession();
                }
                WebView webView = fusionTab.mWebView;
                if (fusionTab.mIsMinimized) {
                    webView.setVisibility(8);
                    TextView textView = this.mMinPillBtn;
                    if (textView != null) {
                        textView.setText(" MAX ");
                    }
                } else {
                    webView.setVisibility(0);
                    webView.bringToFront();
                    TextView textView2 = this.mMinPillBtn;
                    if (textView2 != null) {
                        textView2.setText(" MIN ");
                    }
                    if (termuxActivity != null) {
                        KeyboardUtils.setSoftInputModeAdjustResize(termuxActivity);
                    }
                }
                this.mActiveWebView = webView;
                this.mCurrentProfile = fusionTab.mProfile;
            } else {
                fusionTab.mWebView.setVisibility(8);
            }
        }
        LinearLayout linearLayout = this.mControlPill;
        if (linearLayout != null) {
            linearLayout.setVisibility(0);
            linearLayout.setElevation(160.0f);
            linearLayout.bringToFront();
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
            this.mCurrentWalletAddress = strFindAddressByName;
            GhostBridgeInterface.setWallet(this.mCurrentWalletName, strFindAddressByName);
            updateMenuTexts();
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Database Dompet & Jaringan Disinkronkan!", 0).show();
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
        ArrayList arrayList = null;
        int i = -1;
        TerminalSession currentSession = termuxActivity.getCurrentSession();
        if (currentSession != null && (arrayList = this.mTabList) != null && (size = arrayList.size()) > 0) {
            for (int i2 = 0; i2 < size; i2++) {
                FusionTab fusionTab = (FusionTab) arrayList.get(i2);
                if (fusionTab != null && (terminalSession = fusionTab.mBoundSession) != null && terminalSession == currentSession) {
                    i = i2;
                    break;
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
                    TermuxActivity r5 = this.mActivity;
                    if (r5 != null) {
                        r5.endWebViewExternalTextInput();
                    }
                    LinearLayout linearLayout2 = this.mControlPill;
                    if (linearLayout2 != null) {
                        linearLayout2.setVisibility(0);
                        linearLayout2.setElevation(160.0f);
                        linearLayout2.setScaleX(1.0f);
                        linearLayout2.setScaleY(1.0f);
                        linearLayout2.setTranslationX(0.0f);
                        linearLayout2.setTranslationY(0.0f);
                        linearLayout2.bringToFront();
                    }
                    updateContextUi();
                    return;
                }
                TextView textView2 = this.mMinPillBtn;
                if (textView2 != null) {
                    textView2.setText(" MIN ");
                }
                if (this.mPaneHost == null) {
                    initViews();
                }
                FrameLayout frameLayout3 = this.mPaneHost;
                if (frameLayout3 != null) {
                    frameLayout3.setVisibility(0);
                    frameLayout3.bringToFront();
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
                    TermuxActivity act = this.mActivity;
                    if (act != null) {
                        act.beginWebViewExternalTextInput();
                    }
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
        if (this.mActiveWebView == null && arrayList != null && !arrayList.isEmpty()) {
            FusionTab fallbackTab = (FusionTab) arrayList.get(0);
            if (fallbackTab != null) {
                this.mActiveWebView = fallbackTab.mWebView;
            }
        }
        LinearLayout linearLayout4 = this.mControlPill;
        if (linearLayout4 != null) {
            linearLayout4.setVisibility(8);
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
        View view4 = this.mTerminalPaneHost;
        if (view4 != null) {
            view4.setVisibility(0);
        }
        TermuxActivity r0 = this.mActivity;
        if (r0 != null) {
            r0.endWebViewExternalTextInput();
        }
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) sb.toString(), 0).show();
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
                Toast.makeText((Context) appCompatActivity, (CharSequence) "CDP NONAKTIF (OFF)", 0).show();
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
            Toast.makeText((Context) appCompatActivity2, (CharSequence) ("CDP AKTIF!\nSocket: @" + str + "\nForward: adb forward tcp:9222 localabstract:" + str), 1).show();
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "Perekaman Makro Dimulai...", 0).show();
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
            Toast.makeText((Context) appCompatActivity, (CharSequence) "AI DevBridge: Mengekstrak DOM...", 0).show();
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
            sb.append("\nAddress: ");
            String str = this.mCurrentWalletAddress;
            if (str == null || str.length() <= 10) {
                sb.append(str);
            } else {
                sb.append(str.substring(0, 6));
                sb.append("...");
                sb.append(str.substring(str.length() - 4));
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
    }
}
