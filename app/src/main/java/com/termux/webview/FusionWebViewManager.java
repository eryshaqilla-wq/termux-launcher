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
import android.net.wifi.WifiManager;
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
import android.os.SystemClock;
import android.view.inputmethod.InputMethodManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
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
    public LinearLayout mGeckoAddonMenuRow;
    public TextView mMinBtn;
    public TextView mMinPillBtn;
    public TextView mNewWorkerBtn;
    public TextView mOpacityBtn;
    public FrameLayout mPaneHost;
    public TextView mQuickBackBtn;
    public TextView mReloadPillBtn;
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
    public String mCurrentSuiWalletAddress = "0x2d5cd38757a6423b513a54523d072ff737b3d5ddc87f56cb96556fbe09a37792";
    public String mCurrentAptosWalletName = "eso-1";
    public String mCurrentAptosWalletAddress = "0xf796fcce529ac9bbda4864320e80f8a016cc0b020ea745f9acde68db5ad63500";
    public String mCurrentIotaWalletName = "eso-1";
    public String mCurrentIotaWalletAddress = "0xf12a6e99d4da110e8e864ed6d3d8ea29eb78627b00c566982464410a8aa0bb0a";
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

    public static final String ENGINE_DEFAULT = "default";
    public static final String ENGINE_GECKO = "gecko";
    private static final String PREFS_WEBVIEW = "fusion_webview_prefs";
    private static final String KEY_DEFAULT_ENGINE = "default_webview_engine";

    public static String normalizeEngine(String engine) {
        if (engine == null) return ENGINE_DEFAULT;
        String clean = engine.trim().toLowerCase();
        if ("gecko".equals(clean) || "geckoview".equals(clean) || "firefox".equals(clean)) {
            return ENGINE_GECKO;
        }
        return ENGINE_DEFAULT;
    }

    public String getDefaultEngine() {
        TermuxActivity act = this.mActivity;
        if (act == null) return ENGINE_DEFAULT;
        try {
            String val = act.getSharedPreferences(PREFS_WEBVIEW, Context.MODE_PRIVATE)
                .getString(KEY_DEFAULT_ENGINE, ENGINE_DEFAULT);
            return normalizeEngine(val);
        } catch (Throwable ignored) {
            return ENGINE_DEFAULT;
        }
    }

    public void setDefaultEngine(String engine) {
        String normalized = normalizeEngine(engine);
        TermuxActivity act = this.mActivity;
        if (act != null) {
            try {
                act.getSharedPreferences(PREFS_WEBVIEW, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_DEFAULT_ENGINE, normalized)
                    .apply();
            } catch (Throwable ignored) {}
        }
    }

    public FusionTab findTabById(int tabId) {
        if (this.mTabList == null) return null;
        for (int i = 0; i < this.mTabList.size(); i++) {
            FusionTab t = (FusionTab) this.mTabList.get(i);
            if (t != null && t.mId == tabId) {
                return t;
            }
        }
        return null;
    }

    public View getTabView(FusionTab tab) {
        return tab != null ? tab.getRenderView() : null;
    }

    public View getActiveTabView() {
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null) {
            View rv = active.getRenderView();
            if (rv != null) return rv;
        }
        return this.mActiveWebView;
    }

    public void dispatchJsOnTab(FusionTab tab, String js) {
        if (tab == null || js == null) return;
        if (tab.isGecko()) {
            FusionGeckoHelper.evaluateJs(tab, js);
        } else if (tab.mWebView != null) {
            tab.mWebView.evaluateJavascript(js, null);
        }
    }

    public void loadUrlOnTab(FusionTab tab, String url) {
        if (tab == null || url == null || url.isEmpty()) return;
        tab.mUrl = url;
        if (tab.isGecko()) {
            FusionGeckoHelper.loadUrl(tab, url);
        } else if (tab.mWebView != null) {
            tab.mWebView.loadUrl(url);
        }
    }

    public void loadUrlOnActiveTab(String url) {
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null) {
            loadUrlOnTab(active, url);
        } else if (this.mActiveWebView != null && url != null && !url.isEmpty()) {
            this.mActiveWebView.loadUrl(url);
        }
    }

    public void reloadTab(FusionTab tab) {
        if (tab == null) return;
        if (tab.isGecko()) {
            FusionGeckoHelper.reload(tab);
        } else if (tab.mWebView != null) {
            tab.mWebView.reload();
        }
    }
    public boolean mIsDevMode = true;
    public PowerManager.WakeLock mMacroWakeLock;
    public WifiManager.WifiLock mMacroWifiLock;

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
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null) {
                return;
            }
            fusionWebViewManager.evalOnActiveTab(this.mJsCode);
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
        public final boolean mIsHeadless;
        public final boolean mIsNewTab;
        public final String mEngine;

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless, boolean isNewTab, String engine) {
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
            this.mIsHeadless = isHeadless;
            this.mIsNewTab = isNewTab;
            this.mEngine = engine;
        }

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless, boolean isNewTab) {
            this(fusionWebViewManager, str, str2, str3, str4, z, z2, z3, str5, str6, i, isHeadless, isNewTab, "");
        }

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless) {
            this(fusionWebViewManager, str, str2, str3, str4, z, z2, z3, str5, str6, i, isHeadless, false, "");
        }

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i) {
            this(fusionWebViewManager, str, str2, str3, str4, z, z2, z3, str5, str6, i, false, false, "");
        }

        public OpenRunnable(FusionWebViewManager fusionWebViewManager, String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
            this(fusionWebViewManager, str, str2, str3, str4, z, z2, z3, str5, "", 0, false, false, "");
        }

        @Override // java.lang.Runnable
        public void run() {
            FusionWebViewManager fusionWebViewManager = this.mManager;
            if (fusionWebViewManager == null) {
                return;
            }
            fusionWebViewManager.openUrlInternal(this.mUrl, this.mProfile, this.mWalletName, this.mChainKey, this.mIsGhost, this.mIsDesktop, this.mIsAuto, this.mWcUri, this.mProxy, this.mIntervalMins, this.mIsHeadless, this.mIsNewTab, this.mEngine);
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
            if (fusionWebViewManager == null) {
                return;
            }
            fusionWebViewManager.reloadActiveTab();
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
        if (termuxActivity != null) {
            FusionUserscriptWatcher.startWatching(termuxActivity);
        }
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
            if (termuxActivity != null) {
                FusionUserscriptWatcher.startWatching(termuxActivity);
            }
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
        if (fusionTabCreateTabInternal != null) {
            if (fusionTabCreateTabInternal.isGecko()) {
                FusionGeckoHelper.loadUrl(fusionTabCreateTabInternal, str);
            } else if (fusionTabCreateTabInternal.mWebView != null) {
                fusionTabCreateTabInternal.mWebView.loadUrl(str);
            }
        }
        updateContextUi();
    }

    private static volatile boolean sCachedRpcAlive = false;
    private static volatile long sLastRpcCheckTime = 0;
    private static volatile boolean sIsCheckingRpc = false;

    public boolean checkRpcDaemon() {
        long now = System.currentTimeMillis();
        if (now - sLastRpcCheckTime > 3000 && !sIsCheckingRpc) {
            sIsCheckingRpc = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress("127.0.0.1", 3333), 200);
                        socket.close();
                        sCachedRpcAlive = true;
                    } catch (Exception unused) {
                        sCachedRpcAlive = false;
                    } finally {
                        sLastRpcCheckTime = System.currentTimeMillis();
                        sIsCheckingRpc = false;
                    }
                }
            }).start();
        }
        return sCachedRpcAlive;
    }

    public void clearWebviewCache() {
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null && active.isGecko()) {
            FusionGeckoHelper.clearSessionData(active);
        }
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
        String activeChain = FusionChainHelper.getCurrentChainKey();
        String[] names = FusionWalletHelper.getNamesForChain(activeChain);
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
        return FusionWalletHelper.getAddressForChain(activeChain, nextIdx);
    }

    public boolean switchWalletByName(String targetName) {
        if (targetName == null || targetName.trim().isEmpty()) return false;
        String cleanTarget = targetName.trim();
        String activeChain = FusionChainHelper.getCurrentChainKey();
        String[] names = FusionWalletHelper.getNamesForChain(activeChain);
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i] != null && (names[i].equalsIgnoreCase(cleanTarget) || names[i].toLowerCase().contains(cleanTarget.toLowerCase()))) {
                    onWalletSelected(i);
                    return true;
                }
            }
        }
        int idx = FusionWalletHelper.findIndexByName(cleanTarget);
        if (idx >= 0) {
            onWalletSelected(idx);
            return true;
        }
        return false;
    }

    private void applyWalletToTab(FusionTab tab, String cleanName, String cleanAddr, String activeChain, boolean updateGlobals) {
        if (updateGlobals) {
            this.mCurrentWalletName = cleanName;
            GhostBridgeInterface.sActiveToolWalletName = cleanName;
            GhostBridgeInterface.sActiveToolAddress = cleanAddr;
            if ("sol".equals(activeChain) || "solana".equals(activeChain)) {
                this.mCurrentSolWalletName = cleanName;
                this.mCurrentSolWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentSolWalletName = cleanName;
                GhostBridgeInterface.sCurrentSolWalletAddress = cleanAddr;
            } else if ("tron".equals(activeChain) || "trx".equals(activeChain)) {
                this.mCurrentTronWalletName = cleanName;
                this.mCurrentTronWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentTronWalletName = cleanName;
                GhostBridgeInterface.sCurrentTronWalletAddress = cleanAddr;
            } else if ("sui".equals(activeChain)) {
                this.mCurrentSuiWalletName = cleanName;
                this.mCurrentSuiWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentSuiWalletName = cleanName;
                GhostBridgeInterface.sCurrentSuiWalletAddress = cleanAddr;
            } else if ("aptos".equals(activeChain) || "apt".equals(activeChain)) {
                this.mCurrentAptosWalletName = cleanName;
                this.mCurrentAptosWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentAptosWalletName = cleanName;
                GhostBridgeInterface.sCurrentAptosWalletAddress = cleanAddr;
            } else if ("iota".equals(activeChain)) {
                this.mCurrentIotaWalletName = cleanName;
                this.mCurrentIotaWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentIotaWalletName = cleanName;
                GhostBridgeInterface.sCurrentIotaWalletAddress = cleanAddr;
            } else {
                this.mCurrentWalletAddress = cleanAddr;
                GhostBridgeInterface.sCurrentWalletName = cleanName;
                GhostBridgeInterface.sCurrentWalletAddress = cleanAddr;
            }
        }
        if (tab != null) {
            tab.mWalletName = cleanName;
            if ("iota".equals(activeChain)) {
                tab.mIotaWalletName = cleanName;
                tab.mIotaWalletAddress = cleanAddr;
            } else if ("sol".equals(activeChain) || "solana".equals(activeChain)) {
                tab.mSolWalletName = cleanName;
                tab.mSolWalletAddress = cleanAddr;
            } else if ("sui".equals(activeChain)) {
                tab.mSuiWalletName = cleanName;
                tab.mSuiWalletAddress = cleanAddr;
            } else if ("aptos".equals(activeChain) || "apt".equals(activeChain)) {
                tab.mAptosWalletName = cleanName;
                tab.mAptosWalletAddress = cleanAddr;
            } else if ("tron".equals(activeChain) || "trx".equals(activeChain)) {
                tab.mTronWalletName = cleanName;
                tab.mTronWalletAddress = cleanAddr;
            } else {
                tab.mWalletAddress = cleanAddr;
            }
        }
    }

    private void pushWalletJsToWebView(final WebView wv, final String cleanName, final String cleanAddr) {
        if (this.mActivity == null || wv == null) return;
        this.mActivity.runOnUiThread(() -> {
            String qAddr = JSONObject.quote(cleanAddr);
            String qName = JSONObject.quote(cleanName);
            wv.evaluateJavascript(
                "(function(){window.__FUSION_WALLET_NAME__=" + qName + ";window.__FUSION_ACTIVE_WALLET_NAME__=" + qName + ";window.__FUSION_WALLET_ADDRESS__=" + qAddr + ";window.__FUSION_ADDRESS__=" + qAddr + ";try{if(window.ethereum&&window.ethereum.emit){window.ethereum.selectedAddress=" + qAddr + ";window.ethereum.emit('accountsChanged',[" + qAddr + "]);}}catch(e){}})();",
                null
            );
        });
    }

    public boolean switchWalletDirect(String targetName, String address, String chainKey) {
        if (address == null || address.trim().isEmpty()) {
            return switchWalletByName(targetName);
        }
        String cleanName = (targetName != null && !targetName.trim().isEmpty()) ? targetName.trim() : "remote";
        String cleanAddr = address.trim();
        String activeChain = (chainKey != null && !chainKey.trim().isEmpty()) ? chainKey.trim().toLowerCase() : FusionChainHelper.getCurrentChainKey();
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
        applyWalletToTab(activeTab, cleanName, cleanAddr, activeChain, true);
        if (this.mActivity != null) {
            this.mActivity.runOnUiThread(() -> {
                updateMenuTexts();
                FusionToolPillHelper.updateToolPillUi(FusionWebViewManager.this);
            });
            if (activeTab != null && activeTab.isGecko()) {
                String qAddr = JSONObject.quote(cleanAddr);
                String qName = JSONObject.quote(cleanName);
                FusionGeckoHelper.evaluateJs(activeTab,
                    "(function(){window.__FUSION_WALLET_NAME__=" + qName + ";window.__FUSION_ACTIVE_WALLET_NAME__=" + qName + ";window.__FUSION_WALLET_ADDRESS__=" + qAddr + ";window.__FUSION_ADDRESS__=" + qAddr + ";try{if(window.ethereum&&window.ethereum.emit){window.ethereum.selectedAddress=" + qAddr + ";window.ethereum.emit('accountsChanged',[" + qAddr + "]);}}catch(e){}})();"
                );
            } else {
                pushWalletJsToWebView(this.mActiveWebView, cleanName, cleanAddr);
            }
        }
        return true;
    }

    public boolean switchWalletDirectOnTab(int tabId, String targetName, String address, String chainKey) {
        if (address == null || address.trim().isEmpty()) return false;
        FusionTab targetTab = findTabById(tabId);
        if (targetTab == null) return false;
        String cleanName = (targetName != null && !targetName.trim().isEmpty()) ? targetName.trim() : "worker";
        String cleanAddr = address.trim();
        String activeChain = (chainKey != null && !chainKey.trim().isEmpty()) ? chainKey.trim().toLowerCase() : (targetTab.mChainKey != null ? targetTab.mChainKey : FusionChainHelper.getCurrentChainKey());
        FusionTab fg = FusionTabStateHelper.getActiveTab(this);
        boolean isFg = (fg != null && fg.mId == tabId);
        applyWalletToTab(targetTab, cleanName, cleanAddr, activeChain, isFg);
        if (isFg && this.mActivity != null) {
            this.mActivity.runOnUiThread(() -> {
                updateMenuTexts();
                FusionToolPillHelper.updateToolPillUi(FusionWebViewManager.this);
            });
        }
        if (targetTab.isGecko()) {
            String qAddr = JSONObject.quote(cleanAddr);
            String qName = JSONObject.quote(cleanName);
            FusionGeckoHelper.evaluateJs(targetTab,
                "(function(){window.__FUSION_WALLET_NAME__=" + qName + ";window.__FUSION_ACTIVE_WALLET_NAME__=" + qName + ";window.__FUSION_WALLET_ADDRESS__=" + qAddr + ";window.__FUSION_ADDRESS__=" + qAddr + ";try{if(window.ethereum&&window.ethereum.emit){window.ethereum.selectedAddress=" + qAddr + ";window.ethereum.emit('accountsChanged',[" + qAddr + "]);}}catch(e){}})();"
            );
        } else {
            pushWalletJsToWebView(targetTab.mWebView, cleanName, cleanAddr);
        }
        return true;
    }

    public boolean switchChainByKey(String chainKey) {
        if (chainKey == null || chainKey.trim().isEmpty()) return false;
        FusionChainHelper.setChain(chainKey.trim());
        this.mCurrentChainKey = FusionChainHelper.getCurrentChainKey();
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
        if (activeTab != null) {
            activeTab.mChainKey = FusionChainHelper.getCurrentChainKey();
            activeTab.mChainId = FusionChainHelper.getCurrentChainId();
        }
        updateMenuTexts();
        FusionToolPillHelper.updateToolPillUi(this);
        if (this.mActivity != null) {
            final int chainId = FusionChainHelper.getCurrentChainId();
            final String hexId = "0x" + Integer.toHexString(chainId);
            final String js = "(function(){ if(window.ethereum && window.ethereum.emit){ window.ethereum.emit('chainChanged', '" + hexId + "'); } window.postMessage({type:'FUSION_CHAIN_CHANGED', chainId:" + chainId + ", hexId:'" + hexId + "'}, '*'); })();";
            this.mActivity.runOnUiThread(() -> evalOnActiveTab(js));
        }
        return true;
    }

    public boolean dispatchNativeTouch(final float x, final float y) {
        View target = getActiveTabView();
        if (this.mActivity == null || target == null) return false;
        return dispatchNativeTouchOnView(target, x, y);
    }

    public boolean dispatchNativeTouchOnTab(final int tabId, final float x, final float y) {
        FusionTab tab = findTabById(tabId);
        View target = tab != null ? tab.getRenderView() : null;
        if (this.mActivity == null || target == null) return false;
        return dispatchNativeTouchOnView(target, x, y);
    }

    private WebView findWebViewByTabId(int tabId) {
        FusionTab t = findTabById(tabId);
        return t != null ? t.mWebView : null;
    }

    public boolean setTabKeepAlive(int tabId, boolean keepAlive) {
        FusionTab t = findTabById(tabId);
        if (t != null) {
            t.mKeepAliveBackground = keepAlive;
            return true;
        }
        return false;
    }

    public String captureTabScreenshot(final int tabId, final String outPath) {
        FusionTab targetTab = (tabId < 0) ? FusionTabStateHelper.getActiveTab(this) : findTabById(tabId);
        if (targetTab != null && targetTab.isGecko()) {
            return FusionGeckoHelper.captureScreenshot(targetTab, outPath, this.mActivity);
        }
        final View target = (targetTab != null) ? targetTab.getRenderView() : getActiveTabView();
        if (this.mActivity == null || target == null || outPath == null || outPath.trim().isEmpty()) {
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> resRef = new AtomicReference<>(null);
        this.mActivity.runOnUiThread(() -> {
            try {
                int w = target.getWidth() > 0 ? target.getWidth() : 1080;
                int h = target.getHeight() > 0 ? target.getHeight() : 1920;
                Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
                target.draw(canvas);
                File outFile = new File(outPath.trim());
                if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 92, fos);
                    fos.flush();
                }
                bmp.recycle();
                resRef.set(outFile.getAbsolutePath());
            } catch (Throwable ignored) {
            } finally {
                latch.countDown();
            }
        });
        try { latch.await(5000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
        return resRef.get();
    }

    private boolean dispatchNativeTouchOnView(final View targetView, final float x, final float y) {
        if (this.mActivity == null || targetView == null) return false;
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long now = SystemClock.uptimeMillis();
                MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
                targetView.dispatchTouchEvent(down);
                down.recycle();
                MotionEvent up = MotionEvent.obtain(now, now + 40, MotionEvent.ACTION_UP, x, y, 0);
                targetView.dispatchTouchEvent(up);
                up.recycle();
            }
        });
        return true;
    }

    public boolean dispatchNativeSwipe(final float x1, final float y1, final float x2, final float y2, final int durationMs) {
        return dispatchNativeSwipeOnView(getActiveTabView(), x1, y1, x2, y2, durationMs);
    }

    public boolean dispatchNativeSwipeOnTab(final int tabId, final float x1, final float y1, final float x2, final float y2, final int durationMs) {
        FusionTab tab = (tabId < 0) ? FusionTabStateHelper.getActiveTab(this) : findTabById(tabId);
        View target = (tab != null) ? tab.getRenderView() : getActiveTabView();
        return dispatchNativeSwipeOnView(target, x1, y1, x2, y2, durationMs);
    }

    private boolean dispatchNativeSwipeOnView(final View targetView, final float x1, final float y1, final float x2, final float y2, final int durationMs) {
        if (this.mActivity == null || targetView == null) return false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int duration = Math.max(50, Math.min(durationMs, 5000));
                    final int steps = Math.max(6, duration / 16);
                    final long startTime = SystemClock.uptimeMillis();

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MotionEvent down = MotionEvent.obtain(startTime, startTime, MotionEvent.ACTION_DOWN, x1, y1, 0);
                            targetView.dispatchTouchEvent(down);
                            down.recycle();
                        }
                    });

                    for (int i = 1; i <= steps; i++) {
                        Thread.sleep(duration / steps);
                        final float progress = (float) i / steps;
                        final float curX = x1 + (x2 - x1) * progress;
                        final float curY = y1 + (y2 - y1) * progress;
                        final long curTime = SystemClock.uptimeMillis();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MotionEvent move = MotionEvent.obtain(startTime, curTime, MotionEvent.ACTION_MOVE, curX, curY, 0);
                                targetView.dispatchTouchEvent(move);
                                move.recycle();
                            }
                        });
                    }

                    final long endTime = SystemClock.uptimeMillis();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MotionEvent up = MotionEvent.obtain(startTime, endTime, MotionEvent.ACTION_UP, x2, y2, 0);
                            targetView.dispatchTouchEvent(up);
                            up.recycle();
                        }
                    });
                } catch (Throwable ignored) {}
            }
        }).start();
        return true;
    }

    public boolean navigateTabAction(final int tabId, final String action, final String url) {
        final FusionTab targetTab = (tabId < 0) ? FusionTabStateHelper.getActiveTab(this) : findTabById(tabId);
        final View targetView = (targetTab != null) ? targetTab.getRenderView() : getActiveTabView();
        if (this.mActivity == null || targetView == null || action == null) return false;
        final String act = action.trim().toLowerCase();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(true);
        this.mActivity.runOnUiThread(() -> {
            try {
                if (targetTab != null && targetTab.isGecko()) {
                    if ("back".equals(act)) {
                        FusionGeckoHelper.goBack(targetTab);
                    } else if ("forward".equals(act)) {
                        FusionGeckoHelper.goForward(targetTab);
                    } else if ("reload".equals(act) || "refresh".equals(act)) {
                        FusionGeckoHelper.reload(targetTab);
                    } else if ("url".equals(act) && url != null && !url.trim().isEmpty()) {
                        FusionGeckoHelper.loadUrl(targetTab, url.trim());
                    } else {
                        result.set(false);
                    }
                } else if (targetView instanceof WebView) {
                    WebView target = (WebView) targetView;
                    if ("back".equals(act)) {
                        if (target.canGoBack()) target.goBack();
                        else result.set(false);
                    } else if ("forward".equals(act)) {
                        if (target.canGoForward()) target.goForward();
                        else result.set(false);
                    } else if ("reload".equals(act) || "refresh".equals(act)) {
                        target.reload();
                    } else if ("url".equals(act) && url != null && !url.trim().isEmpty()) {
                        target.loadUrl(url.trim());
                    } else {
                        result.set(false);
                    }
                } else {
                    result.set(false);
                }
            } catch (Throwable ignored) {
                result.set(false);
            } finally {
                latch.countDown();
            }
        });
        try { latch.await(3000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
        return result.get();
    }

    public boolean dispatchNativeLongPress(final float x, final float y, final int durationMs) {
        final View targetView = getActiveTabView();
        if (this.mActivity == null || targetView == null) return false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int duration = Math.max(200, Math.min(durationMs, 5000));
                    final long startTime = SystemClock.uptimeMillis();

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MotionEvent down = MotionEvent.obtain(startTime, startTime, MotionEvent.ACTION_DOWN, x, y, 0);
                            targetView.dispatchTouchEvent(down);
                            down.recycle();
                        }
                    });

                    Thread.sleep(duration);

                    final long endTime = SystemClock.uptimeMillis();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MotionEvent up = MotionEvent.obtain(startTime, endTime, MotionEvent.ACTION_UP, x, y, 0);
                            targetView.dispatchTouchEvent(up);
                            up.recycle();
                        }
                    });
                } catch (Throwable ignored) {}
            }
        }).start();
        return true;
    }

    public void hideSoftKeyboard() {
        if (this.mActivity == null) return;
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    View view = mActivity.getCurrentFocus();
                    if (view == null) {
                        view = getActiveTabView();
                    }
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    public String evaluateJsSync(final String str, long timeoutMs) {
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null && active.isGecko()) {
            return FusionGeckoHelper.evaluateJsSync(active, str, timeoutMs);
        }
        return evaluateJsOnWebViewSync(this.mActiveWebView, str, timeoutMs);
    }

    public String evaluateJsOnTabSync(int tabId, final String str, long timeoutMs) {
        FusionTab targetTab = findTabById(tabId);
        if (targetTab != null && targetTab.isGecko()) {
            return FusionGeckoHelper.evaluateJsSync(targetTab, str, timeoutMs);
        }
        WebView targetWv = targetTab != null ? targetTab.mWebView : null;
        if (targetWv == null) return null;
        return evaluateJsOnWebViewSync(targetWv, str, timeoutMs);
    }

    private String evaluateJsOnWebViewSync(final WebView targetWv, final String str, long timeoutMs) {
        if (this.mActivity == null || targetWv == null) {
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> resRef = new AtomicReference<>(null);
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                targetWv.evaluateJavascript(str, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        resRef.set(value);
                        latch.countDown();
                    }
                });
            }
        });
        try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}
        return resRef.get();
    }

    public String getWebviewStateJson() {
        JSONObject obj = new JSONObject();
        try {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
            String curUrl = "";
            String curTitle = "";
            if (activeTab != null) {
                if (activeTab.isGecko()) {
                    curUrl = activeTab.mUrl != null ? activeTab.mUrl : "";
                    curTitle = activeTab.mTitle != null ? activeTab.mTitle : "";
                } else if (activeTab.mWebView != null) {
                    curUrl = activeTab.mWebView.getUrl() != null ? activeTab.mWebView.getUrl() : (activeTab.mUrl != null ? activeTab.mUrl : "");
                    curTitle = activeTab.mWebView.getTitle() != null ? activeTab.mWebView.getTitle() : (activeTab.mTitle != null ? activeTab.mTitle : "");
                }
            }
            if (curUrl.isEmpty() && this.mActiveWebView != null) {
                curUrl = this.mActiveWebView.getUrl() != null ? this.mActiveWebView.getUrl() : "";
                curTitle = this.mActiveWebView.getTitle() != null ? this.mActiveWebView.getTitle() : "";
            }
            obj.put("url", curUrl);
            obj.put("title", curTitle);
            obj.put("engine", activeTab != null && activeTab.mEngine != null ? activeTab.mEngine : ENGINE_DEFAULT);
            obj.put("default_engine", getDefaultEngine());
            obj.put("active_tab", this.mActiveTab);
            obj.put("total_tabs", this.mTabList != null ? this.mTabList.size() : 0);
            obj.put("wallet_name", this.mCurrentWalletName != null ? this.mCurrentWalletName : "");
            obj.put("wallet_address", this.mCurrentWalletAddress != null ? this.mCurrentWalletAddress : "");
            obj.put("chain_key", FusionChainHelper.getCurrentChainKey());
            obj.put("chain_id", FusionChainHelper.getCurrentChainId());
            obj.put("chain_name", FusionChainHelper.getCurrentChainName());
            obj.put("chain_symbol", FusionChainHelper.getCurrentChainSymbol());
            if (activeTab != null) {
                obj.put("profile", activeTab.mProfile != null ? activeTab.mProfile : "default");
                obj.put("is_headless", activeTab.mIsHeadless);
                obj.put("is_proxy_enabled", activeTab.mIsProxyEnabled);
                if (activeTab.mProxyItem != null) {
                    obj.put("proxy", activeTab.mProxyItem.toString());
                }
            }
        } catch (Exception ignored) {}
        return obj.toString();
    }

    public void reloadActiveTab() {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity != null) {
            appCompatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FusionTab active = FusionTabStateHelper.getActiveTab(FusionWebViewManager.this);
                    if (active != null && active.isGecko()) {
                        FusionGeckoHelper.reload(active);
                    } else if (mActiveWebView != null) {
                        mActiveWebView.reload();
                    }
                }
            });
        }
    }

    public String rotateDeviceFingerprint() {
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
        if (mActiveWebView != null || (activeTab != null && activeTab.isGecko())) {
            String randProf = "fp_" + (System.currentTimeMillis() % 100000);
            FusionProfileManager.DeviceProfile dev = FusionProfileManager.getDeviceForProfile(randProf);
            AppCompatActivity appCompatActivity = this.mActivity;
            if (appCompatActivity != null) {
                appCompatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActiveWebView != null) {
                            mActiveWebView.getSettings().setUserAgentString(FusionProfileManager.getEffectiveUserAgent(appCompatActivity, dev, false));
                        }
                        evalOnActiveTab(
                            "window.__FUSION_DEVICE__ = " + dev.toJsonString() + ";" +
                            "if(window.__applyFingerprintFaker) window.__applyFingerprintFaker();"
                        );
                        FusionToast.makeText(appCompatActivity, "Fingerprint: " + dev.name, Toast.LENGTH_SHORT).show();
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

    public void pasteClipboardIntoActiveWebView() {
        TermuxActivity r0 = this.mActivity;
        if (r0 == null) return;
        try {
            ClipboardManager cm = (ClipboardManager) r0.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData clip = cm.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    ClipData.Item item = clip.getItemAt(0);
                    CharSequence text = item.getText();
                    if (text == null && item.getUri() != null) {
                        text = item.coerceToText(r0);
                    }
                    if (text != null) {
                        pasteTextIntoActiveWebView(text.toString());
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public void pasteTextIntoActiveWebView(final String text) {
        if (text == null || this.mActivity == null) return;
        this.mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String escaped = JSONObject.quote(text);
                String js = "(function(){" +
                    "  var el = document.activeElement;" +
                    "  if (!el) { try { document.execCommand('insertText', false, " + escaped + "); } catch(_) {} return; }" +
                    "  var tag = (el.tagName || '').toUpperCase();" +
                    "  if (tag === 'INPUT' || tag === 'TEXTAREA') {" +
                    "    var s = (typeof el.selectionStart === 'number') ? el.selectionStart : (el.value ? el.value.length : 0);" +
                    "    var e = (typeof el.selectionEnd === 'number') ? el.selectionEnd : s;" +
                    "    var v = el.value || '';" +
                    "    var ins = " + escaped + ";" +
                    "    var setter = (tag === 'INPUT' ? Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value') : Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value'));" +
                    "    var nextVal = v.slice(0, s) + ins + v.slice(e);" +
                    "    if (setter && setter.set) { setter.set.call(el, nextVal); } else { el.value = nextVal; }" +
                    "    try { el.selectionStart = el.selectionEnd = s + ins.length; } catch(_) {}" +
                    "    el.dispatchEvent(new Event('input', { bubbles: true }));" +
                    "    el.dispatchEvent(new Event('change', { bubbles: true }));" +
                    "  } else if (el.isContentEditable || (el.getAttribute && el.getAttribute('contenteditable') === 'true')) {" +
                    "    try { document.execCommand('insertText', false, " + escaped + "); } catch(_) {}" +
                    "  } else {" +
                    "    try { document.execCommand('insertText', false, " + escaped + "); } catch(_) {}" +
                    "  }" +
                    "})()";
                evalOnActiveTab(js);
            }
        });
    }

    public static void applyDesktopSettingsToWebView(WebView webView) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        if (settings == null) return;
        settings.setUserAgentString(FusionProfileManager.getEffectiveUserAgent(webView.getContext(), null, true));
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
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
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMixedContentMode(0);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // ⚡ Fusion Low-Latency Performance & Engine Turbo Settings
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false); // Eliminate Google Safe Browsing DNS/HTTP latency overhead
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settings.setOffscreenPreRaster(true); // Pre-raster offscreen/headless DOM tiles for instant layout/click
        }
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // Force GPU hardware acceleration layer

        if (isDesktop) {
            applyDesktopSettingsToWebView(webView);
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
        FusionWebViewClient.ensureServiceWorkerHooked();
        webView.setDownloadListener(new FusionDownloadListener((this.mActivity != null) ? this.mActivity : webView.getContext()));
        webView.addJavascriptInterface(new GhostBridgeInterface(this, webView), "GhostBridge");
        webView.addJavascriptInterface(new FusionExtensionHelper.FusionExtensionBridge((this.mActivity != null) ? this.mActivity : webView.getContext(), webView), "__FusionExtensionBridge__");
        webView.addJavascriptInterface(new FusionNativeBridge(this, webView), "__FUSION_NATIVE__");
        webView.addJavascriptInterface(new FusionNativeBridge(this, webView), "FusionNative");
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
            FusionGeckoHelper.applyTheme(this.mActivity);
        });
    }

    public FusionTab createTabInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        return createTabInternal(str, str2, str3, str4, z, z2, z3, str5, false, "");
    }

    public FusionTab createTabInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, boolean isHeadless) {
        return createTabInternal(str, str2, str3, str4, z, z2, z3, str5, isHeadless, "");
    }

    public FusionTab createTabInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, boolean isHeadless, String engine) {
        AppCompatActivity appCompatActivity = this.mActivity;
        if (appCompatActivity == null) {
            return null;
        }
        String resolvedEngine = (engine == null || engine.trim().isEmpty()) ? getDefaultEngine() : normalizeEngine(engine);
        int i = this.mNextTabId;
        this.mNextTabId = i + 1;
        FusionTab fusionTab;
        if (ENGINE_GECKO.equals(resolvedEngine)) {
            fusionTab = new FusionTab(i, str, str2, null, z, z2, z3, isHeadless);
            fusionTab.mEngine = ENGINE_GECKO;
            if (!FusionGeckoHelper.createGeckoTab(this, fusionTab, str2, z2)) {
                return null;
            }
            if (this.mPaneHost != null && fusionTab.mGeckoView != null) {
                this.mPaneHost.addView(fusionTab.mGeckoView, new FrameLayout.LayoutParams(-1, -1));
                fusionTab.mGeckoView.setElevation(0.0f);
            }
        } else {
            WebView webView = createNewConfiguredWebView(str2, z2);
            if (webView == null) {
                return null;
            }
            FrameLayout frameLayout = this.mPaneHost;
            if (frameLayout != null) {
                frameLayout.addView(webView, new FrameLayout.LayoutParams(-1, -1));
            }
            webView.setElevation(0.0f);
            fusionTab = new FusionTab(i, str, str2, webView, z, z2, z3, isHeadless);
            fusionTab.mEngine = ENGINE_DEFAULT;
        }
        String initialChain = (str4 != null && !str4.isEmpty()) ? str4 : "bsc";
        String autoChain = FusionChainHelper.detectChainFromUrl(str);
        if (autoChain != null && !autoChain.isEmpty()) {
            initialChain = autoChain;
        } else if (FusionChainHelper.isNonEvmKey(initialChain) && !FusionChainHelper.isNonEvmUrl(str)) {
            initialChain = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty()) ? FusionChainHelper.sLastEvmChainKey : "bsc";
        }
        fusionTab.mChainKey = initialChain;
        fusionTab.mChainId = FusionChainHelper.getChainIdByKey(initialChain);
        fusionTab.mWalletName = this.mCurrentWalletName != null ? this.mCurrentWalletName : "";
        fusionTab.mWalletAddress = this.mCurrentWalletAddress != null ? this.mCurrentWalletAddress : "";
        fusionTab.mIotaWalletName = this.mCurrentIotaWalletName != null ? this.mCurrentIotaWalletName : "";
        fusionTab.mIotaWalletAddress = this.mCurrentIotaWalletAddress != null ? this.mCurrentIotaWalletAddress : "";
        fusionTab.mSolWalletName = this.mCurrentSolWalletName != null ? this.mCurrentSolWalletName : "";
        fusionTab.mSolWalletAddress = this.mCurrentSolWalletAddress != null ? this.mCurrentSolWalletAddress : "";
        if (isHeadless) {
            fusionTab.mIsMinimized = true;
            fusionTab.mKeepAliveBackground = true;
            int virtW = z2 ? 1280 : 1080;
            int virtH = z2 ? 720 : 1920;
            View renderView = fusionTab.getRenderView();
            if (renderView != null) {
                renderView.measure(
                    View.MeasureSpec.makeMeasureSpec(virtW, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(virtH, View.MeasureSpec.EXACTLY)
                );
                renderView.layout(0, 0, virtW, virtH);
                renderView.setTranslationX(-10000.0f);
                if (!fusionTab.isGecko()) {
                    renderView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
                renderView.setVisibility(View.VISIBLE);
            }
            if (fusionTab.mWebView != null) {
                fusionTab.mWebView.resumeTimers();
            }
            if (fusionTab.isGecko()) {
                FusionGeckoHelper.setActive(fusionTab, true);
            }
            acquireWakeLock();
        }
        if (fusionTab.mWebView != null && fusionTab.mWebView.getWebViewClient() instanceof FusionWebViewClient) {
            ((FusionWebViewClient) fusionTab.mWebView.getWebViewClient()).setCurrentUrl(str);
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

    /**
     * 🛡️ Phoenix Auto-Healing: Resurrects a crashed or OOM-killed WebView tab
     * automatically without closing the tab or breaking the session.
     */
    public void resurrectTab(FusionTab tab) {
        if (tab == null || this.mActivity == null) return;
        try {
            WebView oldWv = tab.mWebView;
            if (oldWv != null) {
                ViewParent parent = oldWv.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(oldWv);
                }
                try { oldWv.destroy(); } catch (Throwable ignored) {}
            }

            WebView newWv = createNewConfiguredWebView(tab.mProfile, tab.mIsDesktop);
            if (newWv == null) return;

            tab.mWebView = newWv;
            if (this.mPaneHost != null) {
                this.mPaneHost.addView(newWv, new FrameLayout.LayoutParams(-1, -1));
            }

            if (tab.mIsHeadless) {
                tab.mIsMinimized = true;
                int virtW = tab.mIsDesktop ? 1280 : 1080;
                int virtH = tab.mIsDesktop ? 720 : 1920;
                newWv.measure(
                    View.MeasureSpec.makeMeasureSpec(virtW, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(virtH, View.MeasureSpec.EXACTLY)
                );
                newWv.layout(0, 0, virtW, virtH);
                newWv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                newWv.setVisibility(View.VISIBLE);
                newWv.resumeTimers();
                acquireWakeLock();
            }

            if (this.mActiveWebView == oldWv) {
                this.mActiveWebView = newWv;
            }

            if (tab.mProxyItem != null && tab.mIsProxyEnabled) {
                FusionProxyHelper.applyProxyToEngine(tab);
                FusionProxyHelper.updateProxyIndicator(this, tab);
            }

            if (tab.mUrl != null && !tab.mUrl.isEmpty()) {
                newWv.loadUrl(tab.mUrl);
            }
            Log.i("FusionWebViewManager", "🛡️ Phoenix Auto-Healing: Tab #" + tab.mId + " successfully resurrected for URL: " + tab.mUrl);
        } catch (Throwable t) {
            Log.e("FusionWebViewManager", "Failed to resurrect tab #" + tab.mId, t);
        }
    }

    public void cycleOpacity() {
        FusionTabStateHelper.cycleOpacity(this);
    }

    public void evalOnActiveTab(final String str) {
        if (str == null) return;
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper() && this.mActivity != null) {
            this.mActivity.runOnUiThread(() -> evalOnActiveTab(str));
            return;
        }
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null && active.isGecko()) {
            FusionGeckoHelper.evaluateJs(active, str);
            return;
        }
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
        View activeView = getActiveTabView();
        if (activeView != null) {
            activeView.setVisibility(8);
        }
        WebView webView = this.mActiveWebView;
        if (webView != null && webView != activeView) {
            webView.setVisibility(8);
        }
    }

    public void onTerminalWallOffsetChanged(float translationX, float alpha) {
        if (this.mPaneHost != null) {
            FusionTab active = FusionTabStateHelper.getActiveTab(this);
            if (active != null && active.mIsHeadless) {
                this.mPaneHost.setVisibility(View.INVISIBLE);
                this.mPaneHost.setTranslationX(-10000.0f);
                return;
            }
            this.mPaneHost.setTranslationX(translationX);
            this.mPaneHost.setAlpha(alpha);
            boolean onScreen = alpha > 0.05f;
            if (!onScreen) {
                if (this.mPaneHost.getVisibility() == View.VISIBLE) {
                    this.mPaneHost.setVisibility(View.GONE);
                }
            } else if (hasMiddleCanvasWebView() && this.mPaneHost.getVisibility() != View.VISIBLE) {
                this.mPaneHost.setVisibility(View.VISIBLE);
                this.mPaneHost.setTranslationX(0f);
                this.mPaneHost.setAlpha(1f);
                syncVisibility();
            }
        }
    }

    public void onTerminalOffScreenChanged(boolean offScreen) {
        if (this.mPaneHost != null) {
            FusionTab active = FusionTabStateHelper.getActiveTab(this);
            if (active != null && active.mIsHeadless) {
                this.mPaneHost.setVisibility(View.INVISIBLE);
                this.mPaneHost.setTranslationX(-10000.0f);
                return;
            }
            if (offScreen) {
                this.mPaneHost.setVisibility(View.GONE);
                if (this.mActivity != null) {
                    this.mActivity.endWebViewExternalTextInput();
                }
            } else if (hasMiddleCanvasWebView()) {
                this.mPaneHost.setVisibility(View.VISIBLE);
                this.mPaneHost.setTranslationX(0f);
                this.mPaneHost.setAlpha(1f);
                if (isMiddleCanvasWebViewActive() && this.mActivity != null) {
                    this.mActivity.beginWebViewExternalTextInput();
                }
                syncVisibility();
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
            FusionTab active = FusionTabStateHelper.getActiveTab(this);
            if (active != null && active.mIsHeadless) {
                if (this.mPaneHost != null) {
                    this.mPaneHost.setVisibility(View.INVISIBLE);
                    this.mPaneHost.setTranslationX(-10000.0f);
                }
            } else {
                if (this.mPaneHost != null) {
                    this.mPaneHost.setTranslationX(0f);
                    this.mPaneHost.setAlpha(1f);
                }
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
            if (tab != null && tab.mBoundSession == session && !tab.mIsMinimized && !tab.mIsHeadless && tab.getRenderView() != null) {
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
            if (tab != null && !tab.mIsHeadless && (tab.mBoundSession == session || tab.mBoundSession == null) && tab.getRenderView() != null) {
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
            FusionTab active = FusionTabStateHelper.getActiveTab(this);
            if (active != null && active.mIsHeadless) {
                frameLayout.setVisibility(View.INVISIBLE);
                frameLayout.setTranslationX(-10000.0f);
            } else {
                frameLayout.setPadding(0, 0, 0, 0);
                frameLayout.setAlpha(1.0f);
                frameLayout.setTranslationX(0.0f);
                frameLayout.bringToFront();
            }
        }

        // Re-parent active webviews / geckoviews if needed
        if (this.mPaneHost != null && this.mTabList != null) {
            for (int ti = 0; ti < this.mTabList.size(); ti++) {
                FusionTab tab = (FusionTab) this.mTabList.get(ti);
                View renderView = tab != null ? tab.getRenderView() : null;
                if (renderView != null) {
                    ViewParent p = renderView.getParent();
                    if (p != this.mPaneHost) {
                        if (p instanceof ViewGroup) {
                            ((ViewGroup) p).removeView(renderView);
                        }
                        this.mPaneHost.addView(renderView, new FrameLayout.LayoutParams(-1, -1));
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
            TextView reloadPillBtn = new TextView(this.mActivity);
            this.mReloadPillBtn = reloadPillBtn;
            reloadPillBtn.setText(" RELOAD ");
            FusionUiHelper.setSolidBtn(reloadPillBtn);
            reloadPillBtn.setTextSize(2, 11.0f);
            reloadPillBtn.setPadding(8, 4, 8, 4);
            reloadPillBtn.setClickable(true);
            reloadPillBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FusionWebViewManager.this.mIsRecording) {
                        FusionWebViewManager.this.evalOnActiveTab("if (window.AgyRecorder && window.AgyRecorder.isRecording) { window.AgyRecorder.recordEvent({type:'reload', timestamp:Date.now(), text:'Reload Halaman'}); try{sessionStorage.setItem('__agy_rec_active__','true'); sessionStorage.setItem('__fusion_rec_active__','true');}catch(_){} }");
                    } else {
                        FusionWebViewManager.this.evalOnActiveTab("try{sessionStorage.removeItem('__agy_rec_active__'); sessionStorage.removeItem('__fusion_rec_active__');}catch(_){}");
                    }
                    FusionWebViewManager.this.reloadActiveTab();
                    FusionToast.makeText(FusionWebViewManager.this.mActivity, "Reloading...", 0).show();
                }
            });
            LinearLayout.LayoutParams layoutParamsReload = new LinearLayout.LayoutParams(-2, -2);
            layoutParamsReload.setMargins(2, 0, 2, 0);
            this.mControlPill.addView(reloadPillBtn, layoutParamsReload);
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

            LinearLayout geckoAddonRow = FusionUiHelper.createMenuRow(this.mActivity, "🦊", "Firefox Add-ons (.xpi Native)", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllDrawers();
                    FusionGeckoHelper.showAddonsDialog(FusionWebViewManager.this);
                }
            });
            geckoAddonRow.setVisibility(View.GONE);
            this.mGeckoAddonMenuRow = geckoAddonRow;
            menuContent.addView(geckoAddonRow);

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

    private static final java.util.Map<String, String> sAssetScriptCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Long> sAssetScriptModCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearAssetScriptCache() {
        sAssetScriptCache.clear();
        sAssetScriptModCache.clear();
    }

    public String loadAssetScript(String str) {
        try {
            TermuxActivity termuxActivity = this.mActivity;
            if (termuxActivity == null || str == null) {
                return null;
            }
            File extFile = FusionProfileManager.resolveExternalEngineFile(str);
            String script = null;
            if (extFile != null) {
                long mod = extFile.lastModified();
                String cacheKey = "ext:" + extFile.getAbsolutePath();
                Long cachedMod = sAssetScriptModCache.get(cacheKey);
                if (cachedMod != null && cachedMod == mod) {
                    script = sAssetScriptCache.get(cacheKey);
                }
                if (script == null) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(extFile)) {
                        int n;
                        while ((n = fis.read(buf)) != -1) {
                            baos.write(buf, 0, n);
                        }
                    }
                    script = baos.toString("UTF-8");
                    sAssetScriptCache.put(cacheKey, script);
                    sAssetScriptModCache.put(cacheKey, mod);
                }
            } else {
                script = sAssetScriptCache.get(str);
                if (script == null) {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    try (InputStream inputStreamOpen = termuxActivity.getAssets().open("webview_engine/" + str)) {
                        int n;
                        while ((n = inputStreamOpen.read(buf)) != -1) {
                            baos.write(buf, 0, n);
                        }
                    }
                    script = baos.toString("UTF-8");
                    sAssetScriptCache.put(str, script);
                }
            }
            return script;
        } catch (Exception unused) {
            return null;
        }
    }

    public void injectAssetScript(WebView webView, String str) {
        if (webView == null || str == null) return;
        String script = loadAssetScript(str);
        if (script != null) {
            webView.evaluateJavascript(script, null);
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

    public String buildBaseInjectionJs(FusionTab ownerTab) {
        String tabProfile = (ownerTab != null && ownerTab.mProfile != null && !ownerTab.mProfile.isEmpty())
                ? ownerTab.mProfile : this.mCurrentProfile;
        if (tabProfile == null || tabProfile.isEmpty()) tabProfile = "default";
        FusionProfileManager.DeviceProfile dev = FusionProfileManager.getDeviceForProfile(tabProfile);
        String evmName = (ownerTab != null && ownerTab.mWalletName != null && !ownerTab.mWalletName.isEmpty())
                ? ownerTab.mWalletName : (this.mCurrentWalletName != null ? this.mCurrentWalletName : "");
        String evmAddr = (ownerTab != null && ownerTab.mWalletAddress != null && !ownerTab.mWalletAddress.isEmpty())
                ? ownerTab.mWalletAddress : (this.mCurrentWalletAddress != null ? this.mCurrentWalletAddress : "");
        String solName = (ownerTab != null && ownerTab.mSolWalletName != null && !ownerTab.mSolWalletName.isEmpty())
                ? ownerTab.mSolWalletName
                : ((this.mCurrentSolWalletName != null && !this.mCurrentSolWalletName.isEmpty()) ? this.mCurrentSolWalletName : FusionWalletHelper.getMasterSolName());
        String solAddr = (ownerTab != null && ownerTab.mSolWalletAddress != null && !ownerTab.mSolWalletAddress.isEmpty())
                ? ownerTab.mSolWalletAddress
                : ((this.mCurrentSolWalletAddress != null && !this.mCurrentSolWalletAddress.isEmpty()) ? this.mCurrentSolWalletAddress : FusionWalletHelper.getMasterSolAddress());
        String tronName = (ownerTab != null && ownerTab.mTronWalletName != null && !ownerTab.mTronWalletName.isEmpty())
                ? ownerTab.mTronWalletName
                : ((this.mCurrentTronWalletName != null && !this.mCurrentTronWalletName.isEmpty()) ? this.mCurrentTronWalletName : FusionWalletHelper.getMasterTronName());
        String tronAddr = (ownerTab != null && ownerTab.mTronWalletAddress != null && !ownerTab.mTronWalletAddress.isEmpty())
                ? ownerTab.mTronWalletAddress
                : ((this.mCurrentTronWalletAddress != null && !this.mCurrentTronWalletAddress.isEmpty()) ? this.mCurrentTronWalletAddress : FusionWalletHelper.getMasterTronAddress());
        String suiName = (ownerTab != null && ownerTab.mSuiWalletName != null && !ownerTab.mSuiWalletName.isEmpty())
                ? ownerTab.mSuiWalletName
                : ((this.mCurrentSuiWalletName != null && !this.mCurrentSuiWalletName.isEmpty()) ? this.mCurrentSuiWalletName : FusionWalletHelper.getMasterSuiName());
        String suiAddr = (ownerTab != null && ownerTab.mSuiWalletAddress != null && !ownerTab.mSuiWalletAddress.isEmpty())
                ? ownerTab.mSuiWalletAddress
                : ((this.mCurrentSuiWalletAddress != null && !this.mCurrentSuiWalletAddress.isEmpty()) ? this.mCurrentSuiWalletAddress : FusionWalletHelper.getMasterSuiAddress());
        String aptosName = (ownerTab != null && ownerTab.mAptosWalletName != null && !ownerTab.mAptosWalletName.isEmpty())
                ? ownerTab.mAptosWalletName
                : ((this.mCurrentAptosWalletName != null && !this.mCurrentAptosWalletName.isEmpty()) ? this.mCurrentAptosWalletName : FusionWalletHelper.getMasterAptosName());
        String aptosAddr = (ownerTab != null && ownerTab.mAptosWalletAddress != null && !ownerTab.mAptosWalletAddress.isEmpty())
                ? ownerTab.mAptosWalletAddress
                : ((this.mCurrentAptosWalletAddress != null && !this.mCurrentAptosWalletAddress.isEmpty()) ? this.mCurrentAptosWalletAddress : FusionWalletHelper.getMasterAptosAddress());
        String iotaName = (ownerTab != null && ownerTab.mIotaWalletName != null && !ownerTab.mIotaWalletName.isEmpty())
                ? ownerTab.mIotaWalletName
                : ((this.mCurrentIotaWalletName != null && !this.mCurrentIotaWalletName.isEmpty()) ? this.mCurrentIotaWalletName : FusionWalletHelper.getMasterIotaName());
        String iotaAddr = (ownerTab != null && ownerTab.mIotaWalletAddress != null && !ownerTab.mIotaWalletAddress.isEmpty())
                ? ownerTab.mIotaWalletAddress
                : ((this.mCurrentIotaWalletAddress != null && !this.mCurrentIotaWalletAddress.isEmpty()) ? this.mCurrentIotaWalletAddress : FusionWalletHelper.getMasterIotaAddress());

        String tabChain = (ownerTab != null && ownerTab.mChainKey != null && !ownerTab.mChainKey.isEmpty()) ? ownerTab.mChainKey : this.mCurrentChainKey;
        String activeWName = (!evmName.isEmpty()) ? evmName : tabProfile;
        String activeWAddr = (!evmAddr.isEmpty()) ? evmAddr : "";
        Context ctx = (ownerTab != null && ownerTab.mWebView != null) ? ownerTab.mWebView.getContext() : this.mActivity;
        String chromeVer = ctx != null ? FusionProfileManager.getSystemChromeVersion(ctx) : "136.0.0.0";
        String effectiveUa = ctx != null ? FusionProfileManager.getEffectiveUserAgent(ctx, dev, ownerTab != null && ownerTab.mIsDesktop) : "";
        String devJson;
        try {
            JSONObject devObj = new JSONObject(dev.toJsonString());
            if (!effectiveUa.isEmpty() && (ownerTab == null || !ownerTab.isGecko())) {
                devObj.put("userAgent", effectiveUa);
            }
            devJson = devObj.toString();
        } catch (Throwable ignored) {
            devJson = dev.toJsonString();
        }

        StringBuilder js = new StringBuilder();
        js.append("window.__FUSION_TAB_ID__ = ").append(ownerTab != null ? ownerTab.mId : -1).append(";");
        js.append("window.__FUSION_PROFILE__ = ").append(JSONObject.quote(tabProfile)).append(";");
        js.append("window.__FUSION_ACTIVE_PROFILE__ = ").append(JSONObject.quote(tabProfile)).append(";");
        js.append("window.__FUSION_CHROME_VERSION__ = ").append(JSONObject.quote(chromeVer)).append(";");
        js.append("window.__FUSION_IS_DESKTOP__ = ").append((ownerTab != null && ownerTab.mIsDesktop) ? "true" : "false").append(";");
        js.append("window.__FUSION_DEVICE__ = ").append(devJson).append(";");
        js.append("window.__FUSION_WALLET_NAME__ = ").append(JSONObject.quote(evmName)).append(";");
        js.append("window.__FUSION_WALLET_ADDRESS__ = ").append(JSONObject.quote(evmAddr)).append(";");
        js.append("window.__FUSION_ACTIVE_WALLET_NAME__ = window.__FUSION_ACTIVE_WALLET_NAME__ || ").append(JSONObject.quote(activeWName)).append(";");
        js.append("window.__FUSION_ACTIVE_WALLET_ADDRESS__ = window.__FUSION_ACTIVE_WALLET_ADDRESS__ || ").append(JSONObject.quote(activeWAddr)).append(";");
        js.append("window.__FUSION_ACTIVE_CHAIN__ = window.__FUSION_ACTIVE_CHAIN__ || ").append(JSONObject.quote(tabChain != null ? tabChain : "bsc")).append(";");
        js.append("window.__FUSION_SOL_WALLET_NAME__ = ").append(JSONObject.quote(solName != null ? solName : "")).append(";");
        js.append("window.__FUSION_SOL_WALLET_ADDRESS__ = ").append(JSONObject.quote(solAddr != null ? solAddr : "")).append(";");
        js.append("window.__FUSION_TRON_WALLET_NAME__ = ").append(JSONObject.quote(tronName != null ? tronName : "")).append(";");
        js.append("window.__FUSION_TRON_WALLET_ADDRESS__ = ").append(JSONObject.quote(tronAddr != null ? tronAddr : "")).append(";");
        js.append("window.__FUSION_SUI_WALLET_NAME__ = ").append(JSONObject.quote(suiName != null ? suiName : "")).append(";");
        js.append("window.__FUSION_SUI_WALLET_ADDRESS__ = ").append(JSONObject.quote(suiAddr != null ? suiAddr : "")).append(";");
        js.append("window.__FUSION_APTOS_WALLET_NAME__ = ").append(JSONObject.quote(aptosName != null ? aptosName : "")).append(";");
        js.append("window.__FUSION_APTOS_WALLET_ADDRESS__ = ").append(JSONObject.quote(aptosAddr != null ? aptosAddr : "")).append(";");
        js.append("window.__FUSION_IOTA_WALLET_NAME__ = ").append(JSONObject.quote(iotaName != null ? iotaName : "")).append(";");
        js.append("window.__FUSION_IOTA_WALLET_ADDRESS__ = ").append(JSONObject.quote(iotaAddr != null ? iotaAddr : "")).append(";");
        js.append("window.__ACTIVE_AI_ENGINE__ = ").append(JSONObject.quote(FusionAiHelper.getActiveEngine())).append(";");
        return js.toString();
    }

    public void injectScriptsForTab(FusionTab tab) {
        if (tab == null) return;
        if (tab.isGecko()) {
            FusionGeckoHelper.injectScripts(this, tab);
        } else if (tab.mWebView != null) {
            injectScripts(tab.mWebView);
        }
    }

    public void injectScripts(WebView webView) {
        if (webView == null) {
            return;
        }
        FusionTab ownerTab = getTabForWebView(webView);
        webView.evaluateJavascript(buildBaseInjectionJs(ownerTab), null);
        injectAssetScript(webView, "fingerprint_faker.js");
        injectAssetScript(webView, "ghost_injected.js");
        FusionUserscriptHelper.injectMatchingScripts(webView, webView.getUrl());
        FusionExtensionHelper.injectMatchingScripts(webView, webView.getUrl(), "document_idle");
    }

    public void killActiveTab() {
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper() && this.mActivity != null) {
            this.mActivity.runOnUiThread(this::killActiveTab);
            return;
        }
        ArrayList arrayList = this.mTabList;
        if (arrayList != null && arrayList.size() > 0) {
            killTabDirect(this.mActiveTab);
        }
    }

    public void killTabById(final int tabId) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) return;
        termuxActivity.runOnUiThread(() -> {
            if (this.mTabList == null) return;
            for (int i = 0; i < this.mTabList.size(); i++) {
                FusionTab t = (FusionTab) this.mTabList.get(i);
                if (t != null && t.mId == tabId) {
                    killTabDirect(i);
                    return;
                }
            }
        });
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
        if (fusionTab.mGeckoView != null || fusionTab.mGeckoSession != null) {
            FusionGeckoHelper.destroyTab(fusionTab, this.mPaneHost);
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
            if (!fusionTab.isGecko()) {
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
        }

        performDirectTabDestroy(fusionTab);
    }

    public void killWorkerTab() {
        killActiveTab();
    }

    public void killAllTabs() {
        final TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(() -> {
            if (this.mTabList != null) {
                while (!this.mTabList.isEmpty()) {
                    killTabDirect(0);
                }
            }
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.GONE);
                this.mPaneHost.removeAllViews();
            }
            if (this.mTerminalPaneHost != null) {
                this.mTerminalPaneHost.setVisibility(View.VISIBLE);
            }
            if (this.mControlPill != null) {
                this.mControlPill.setVisibility(View.GONE);
            }
            if (this.mToolScroll != null) {
                this.mToolScroll.setVisibility(View.GONE);
            }
            this.mActiveWebView = null;
            this.mActiveTab = 0;
            act.endWebViewExternalTextInput();
            act.refreshTerminalWindowBar();
            FusionToast.makeText(act, "All WebView Tabs Closed", Toast.LENGTH_SHORT).show();
        });
    }

    public void onChainSelected(int i) {
        FusionChainHelper.setChainByIndex(i);
        this.mCurrentChainKey = FusionChainHelper.getCurrentChainKey();
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
        if (activeTab != null) {
            activeTab.mChainKey = FusionChainHelper.getCurrentChainKey();
            activeTab.mChainId = FusionChainHelper.getCurrentChainId();
        }
        updateMenuTexts();
        FusionToolPillHelper.updateToolPillUi(this);
        LinearLayout linearLayout = this.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        View view = this.mMenuBackdrop;
        if (view != null) {
            view.setVisibility(8);
        }
        final String chainKey = FusionChainHelper.getCurrentChainKey();
        final String chainIdHex = FusionChainHelper.getChainIdHex();
        evalOnActiveTab("if(window.ethereum){window.ethereum.chainId='" + chainIdHex + "';window.ethereum.networkVersion='" + FusionChainHelper.getChainIdDec() + "';window.dispatchEvent(new CustomEvent('chainChanged',{detail:'" + chainIdHex + "'}));}");
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
                    if (mMacroWifiLock == null) {
                        WifiManager wm = (WifiManager) act.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wm != null) {
                            mMacroWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "termux:fusion-macro-wifi");
                        }
                    }
                    if (mMacroWifiLock != null && !mMacroWifiLock.isHeld()) {
                        mMacroWifiLock.acquire();
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
                    if (mMacroWifiLock != null && mMacroWifiLock.isHeld()) {
                        mMacroWifiLock.release();
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
                View activeView = activeTab != null ? activeTab.getRenderView() : mActiveWebView;
                if (activeTab != null && activeTab.mIsMinimized) {
                    FusionTabStateHelper.toggleMinimize(FusionWebViewManager.this);
                } else if (activeView != null) {
                    activeView.setVisibility(View.VISIBLE);
                    activeView.bringToFront();
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
                if (targetUrl != null && !targetUrl.trim().isEmpty()) {
                    loadUrlOnActiveTab(targetUrl.trim());
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
                String autoChain = FusionChainHelper.detectChainFromUrl(str);
                if (autoChain != null) {
                    GhostBridgeInterface.handleDappChainDetected(autoChain, 0);
                }
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
        String activeChain = FusionChainHelper.getCurrentChainKey();
        String name = FusionWalletHelper.getNameForChain(activeChain, i);
        String address = FusionWalletHelper.getAddressForChain(activeChain, i);
        this.mCurrentWalletName = name;
        this.mCurrentWalletAddress = address;
        GhostBridgeInterface.sActiveToolWalletName = name;
        GhostBridgeInterface.sActiveToolAddress = address;
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

        String subAddr = FusionWalletHelper.getAddressForChain("substrate", i);
        String dotAddr = FusionWalletHelper.getAddressForChain("polkadot", i);

        // Sinkronkan wallet terpilih ke tab aktif agar GhostBridge per-tab selalu akurat
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
        if (activeTab != null) {
            activeTab.mWalletName = name != null ? name : "";
            activeTab.mWalletAddress = address != null ? address : "";
            if (solAddr != null && !solAddr.isEmpty()) {
                activeTab.mSolWalletName = solName;
                activeTab.mSolWalletAddress = solAddr;
            }
            if (tronAddr != null && !tronAddr.isEmpty()) {
                activeTab.mTronWalletName = tronName;
                activeTab.mTronWalletAddress = tronAddr;
            }
            if (suiAddr != null && !suiAddr.isEmpty()) {
                activeTab.mSuiWalletName = suiName;
                activeTab.mSuiWalletAddress = suiAddr;
            }
            if (aptosAddr != null && !aptosAddr.isEmpty()) {
                activeTab.mAptosWalletName = aptosName;
                activeTab.mAptosWalletAddress = aptosAddr;
            }
            if (iotaAddr != null && !iotaAddr.isEmpty()) {
                activeTab.mIotaWalletName = iotaName;
                activeTab.mIotaWalletAddress = iotaAddr;
            }
        }

        updateMenuTexts();
        FusionToolPillHelper.updateToolPillUi(this);
        LinearLayout linearLayout = this.mMenuCard;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }

        final String finalAddr = address;
        final String finalName = name;
        FusionWalletHelper.writeActiveWalletMarker(finalName, finalAddr);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GhostBridgeInterface.forwardToDaemon("{\"method\":\"fusion_switchWallet\",\"params\":[\"" + finalAddr + "\",\"" + finalName + "\"]}");
                } catch (Throwable ignored) {}
            }
        }).start();

        String walletSwitchJs =
            "window.__FUSION_WALLET_NAME__=" + JSONObject.quote(name != null ? name : "") + ";" +
            "window.__FUSION_WALLET_ADDRESS__=" + JSONObject.quote(address != null ? address : "") + ";" +
            "window.__FUSION_ACTIVE_WALLET_NAME__=" + JSONObject.quote(name != null ? name : "") + ";" +
            "window.__FUSION_ACTIVE_WALLET_ADDRESS__=" + JSONObject.quote(address != null ? address : "") + ";" +
            "if(window.ethereum){window.ethereum.selectedAddress=" + JSONObject.quote(address != null ? address : "") + ";window.dispatchEvent(new CustomEvent('accountsChanged',{detail:[" + JSONObject.quote(address != null ? address : "") + "]}));}";
        if (solAddr != null && !solAddr.isEmpty()) {
            walletSwitchJs += "if(window.solana){window.__FUSION_SOL_WALLET_ADDRESS__='" + solAddr + "';if(window.solana.publicKey){window.solana.publicKey._val='" + solAddr + "';}window.solana.emit('accountChanged','" + solAddr + "');}";
        }
        if (tronAddr != null && !tronAddr.isEmpty()) {
            walletSwitchJs += "if(window.tronWeb){window.__FUSION_TRON_WALLET_ADDRESS__='" + tronAddr + "';if(window.tronWeb.defaultAddress){window.tronWeb.defaultAddress.base58='" + tronAddr + "';}window.postMessage({message:{action:'setAccount',data:{address:'" + tronAddr + "'}}},'*');}";
        }
        if (suiAddr != null && !suiAddr.isEmpty()) {
            walletSwitchJs += "if(window.__FUSION_SET_SUI_ACCOUNT__){window.__FUSION_SET_SUI_ACCOUNT__('" + suiAddr + "');}";
        }
        if (aptosAddr != null && !aptosAddr.isEmpty()) {
            walletSwitchJs += "if(window.__FUSION_SET_APTOS_ACCOUNT__){window.__FUSION_SET_APTOS_ACCOUNT__('" + aptosAddr + "');}";
        }
        if (iotaAddr != null && !iotaAddr.isEmpty()) {
            walletSwitchJs += "if(window.__FUSION_SET_IOTA_ACCOUNT__){window.__FUSION_SET_IOTA_ACCOUNT__('" + iotaAddr + "');}";
        }
        String curChainKey = FusionChainHelper.getCurrentChainKey();
        boolean isSubSite = "substrate".equalsIgnoreCase(curChainKey) || "flop".equalsIgnoreCase(curChainKey) || "polkadot".equalsIgnoreCase(curChainKey) || "dot".equalsIgnoreCase(curChainKey);
        if (isSubSite) {
            if (subAddr != null && !subAddr.isEmpty()) {
                walletSwitchJs += "if(window.__FUSION_SET_SUBSTRATE_ACCOUNT__){window.__FUSION_SET_SUBSTRATE_ACCOUNT__('" + subAddr + "');}try{window.dispatchEvent(new CustomEvent('substrateAccountChanged',{detail:'" + subAddr + "'}));}catch(_){}";
            }
            if (dotAddr != null && !dotAddr.isEmpty()) {
                walletSwitchJs += "if(window.__FUSION_SET_POLKADOT_ACCOUNT__){window.__FUSION_SET_POLKADOT_ACCOUNT__('" + dotAddr + "');}try{window.dispatchEvent(new CustomEvent('polkadotAccountChanged',{detail:'" + dotAddr + "'}));}catch(_){}";
            }
        }
        if (this.mIsRecording) {
            walletSwitchJs += "if(window.AgyRecorder && window.AgyRecorder.isRecording){window.AgyRecorder.recordEvent({type:'next_wallet',timestamp:Date.now(),text:'Ganti Wallet -> " + address + "'});}";
        }
        evalOnActiveTab(walletSwitchJs);
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless, boolean isNewTab, String engine) {
        TermuxActivity termuxActivity = this.mActivity;
        if (termuxActivity == null) {
            return;
        }
        termuxActivity.runOnUiThread(new OpenRunnable(this, str, str2, str3, str4, z, z2, z3, str5, str6, i, isHeadless, isNewTab, engine));
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless, boolean isNewTab) {
        openConfig(str, str2, str3, str4, z, z2, z3, str5, str6, i, isHeadless, isNewTab, "");
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i, boolean isHeadless) {
        openConfig(str, str2, str3, str4, z, z2, z3, str5, str6, i, isHeadless, false);
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String str6, int i) {
        openConfig(str, str2, str3, str4, z, z2, z3, str5, str6, i, false, false);
    }

    public void openConfig(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        openConfig(str, str2, str3, str4, z, z2, z3, str5, "", 0, false, false);
    }

    public java.util.List<FusionTab> getTabList() {
        return this.mTabList;
    }

    public String getTabsJson() {
        if (this.mTabList == null || this.mTabList.isEmpty()) {
            return "[]";
        }
        JSONArray arr = new JSONArray();
        int activeIdx = this.mActiveTab;
        for (int i = 0; i < this.mTabList.size(); i++) {
            FusionTab tab = (FusionTab) this.mTabList.get(i);
            if (tab == null) continue;
            try {
                JSONObject obj = new JSONObject();
                obj.put("index", i);
                obj.put("id", tab.mId);
                obj.put("url", tab.mUrl != null ? tab.mUrl : "about:blank");
                obj.put("title", tab.mTitle != null ? tab.mTitle : "New Tab");
                obj.put("profile", tab.mProfile != null ? tab.mProfile : "default");
                obj.put("engine", tab.mEngine != null ? tab.mEngine : ENGINE_DEFAULT);
                obj.put("isHeadless", tab.mIsHeadless);
                obj.put("isMinimized", tab.mIsMinimized);
                obj.put("isDesktop", tab.mIsDesktop);
                obj.put("isGhost", tab.mIsGhost);
                obj.put("active", i == activeIdx);
                if (tab.mProxyItem != null && tab.mProxyItem.isValid()) {
                    obj.put("proxy", tab.mProxyItem.getDisplayName());
                } else {
                    obj.put("proxy", "direct");
                }
                arr.put(obj);
            } catch (Exception ignored) {}
        }
        return arr.toString();
    }

    public void attachTab(final int index) {
        final TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(() -> {
            if (this.mTabList == null || this.mTabList.isEmpty()) return;
            int target = (index >= 0 && index < this.mTabList.size()) ? index : 0;
            FusionTab tab = (FusionTab) this.mTabList.get(target);
            if (tab != null) {
                tab.mIsHeadless = false;
                tab.mIsMinimized = false;
                View rv = tab.getRenderView();
                if (rv != null) {
                    rv.setVisibility(View.VISIBLE);
                    rv.bringToFront();
                }
                if (tab.mWebView != null) {
                    tab.mWebView.onResume();
                    tab.mWebView.resumeTimers();
                }
                if (tab.isGecko()) {
                    FusionGeckoHelper.setActive(tab, true);
                }
            }
            switchTab(target);
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.VISIBLE);
                this.mPaneHost.setTranslationX(0.0f);
                this.mPaneHost.setAlpha(1.0f);
                this.mPaneHost.bringToFront();
            }
            if (this.mTerminalPaneHost != null) {
                this.mTerminalPaneHost.setVisibility(View.GONE);
            }
            if (this.mControlPill != null) {
                this.mControlPill.setVisibility(View.VISIBLE);
            }
            if (this.mToolScroll != null) {
                this.mToolScroll.setVisibility(View.VISIBLE);
            }
            act.beginWebViewExternalTextInput();
            act.refreshTerminalWindowBar();
            FusionToast.makeText(act, "Attached to Tab " + (target + 1), Toast.LENGTH_SHORT).show();
        });
    }

    public void detachTab(final int index) {
        final TermuxActivity act = this.mActivity;
        if (act == null) return;
        act.runOnUiThread(() -> {
            if (this.mTabList == null || this.mTabList.isEmpty()) return;
            int target = (index >= 0 && index < this.mTabList.size()) ? index : this.mActiveTab;
            FusionTab tab = (FusionTab) this.mTabList.get(target);
            if (tab != null) {
                tab.mIsHeadless = true;
                tab.mIsMinimized = true;
                View rv = tab.getRenderView();
                if (rv != null) {
                    rv.setVisibility(View.VISIBLE);
                }
                if (tab.mWebView != null) {
                    tab.mWebView.resumeTimers();
                }
                if (tab.isGecko()) {
                    FusionGeckoHelper.setActive(tab, true);
                }
            }
            if (target == this.mActiveTab) {
                if (this.mPaneHost != null) {
                    this.mPaneHost.setVisibility(View.INVISIBLE);
                    this.mPaneHost.setTranslationX(-10000.0f);
                }
                if (this.mTerminalPaneHost != null) {
                    this.mTerminalPaneHost.setVisibility(View.VISIBLE);
                    this.mTerminalPaneHost.bringToFront();
                }
                if (this.mControlPill != null) {
                    this.mControlPill.setVisibility(View.GONE);
                }
                if (this.mToolScroll != null) {
                    this.mToolScroll.setVisibility(View.GONE);
                }
                act.endWebViewExternalTextInput();
                act.refreshTerminalWindowBar();
            }
            FusionToast.makeText(act, "Detached Tab " + (target + 1) + " (Headless)", Toast.LENGTH_SHORT).show();
        });
    }

    public void openUrl(String str, String str2, boolean z, boolean z2) {
        openConfig(str, str2, "", "base", z, z2, false, "");
    }

    public void openUrl(String str, String str2, boolean z, boolean z2, String str3) {
        openConfig(str, str2, str3, "base", z, z2, false, "");
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        openUrlInternal(str, str2, str3, str4, z, z2, z3, str5, "", 0, false, false);
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String proxyStr, int intervalMins) {
        openUrlInternal(str, str2, str3, str4, z, z2, z3, str5, proxyStr, intervalMins, false, false);
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String proxyStr, int intervalMins, boolean isHeadless) {
        openUrlInternal(str, str2, str3, str4, z, z2, z3, str5, proxyStr, intervalMins, isHeadless, false);
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String proxyStr, int intervalMins, boolean isHeadless, boolean isNewTab) {
        openUrlInternal(str, str2, str3, str4, z, z2, z3, str5, proxyStr, intervalMins, isHeadless, isNewTab, "");
    }

    public void openUrlInternal(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, String str5, String proxyStr, int intervalMins, boolean isHeadless, boolean isNewTab, String engine) {
        TerminalSession currentSession = null;
        initViews();
        String effectiveEngine = (engine == null || engine.trim().isEmpty()) ? getDefaultEngine() : normalizeEngine(engine);
        if (str == null || str.isEmpty() || "home".equalsIgnoreCase(str) || "about:home".equalsIgnoreCase(str) || "about:newtab".equalsIgnoreCase(str)) {
            File extHome = FusionProfileManager.resolveExternalEngineFile("home.html");
            str = (extHome != null) ? ("file://" + extHome.getAbsolutePath()) : "file:///android_asset/webview_engine/home.html";
        }
        if (str2 == null) {
            str2 = "default";
        }
        FusionTab prevActiveBeforeOpen = FusionTabStateHelper.getActiveTab(this);
        boolean hasVisibleActiveTab = prevActiveBeforeOpen != null && !prevActiveBeforeOpen.mIsHeadless && !prevActiveBeforeOpen.mIsMinimized && isWebViewVisible();
        boolean skipUiSwitch = isHeadless && hasVisibleActiveTab;
        if (!skipUiSwitch) {
            this.mCurrentProfile = str2;
            this.mIsGhostMode = z;
            this.mIsDesktopMode = z2;
            this.mIsAutoMode = z3;
            this.mWcUri = str5;
            this.mIsMinimized = isHeadless;
        }
        String autoFromUrl = FusionChainHelper.detectChainFromUrl(str);
        if (autoFromUrl != null && !autoFromUrl.isEmpty()) {
            str4 = autoFromUrl;
        } else if (str4 == null || str4.isEmpty()) {
            str4 = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty()) ? FusionChainHelper.sLastEvmChainKey : "bsc";
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
        if (!isNewTab && currentSession != null) {
            int size = this.mTabList.size();
            for (int i2 = 0; i2 < size; i2++) {
                FusionTab fusionTab = (FusionTab) this.mTabList.get(i2);
                if (fusionTab != null && fusionTab.mBoundSession == currentSession) {
                    String tabProf = (fusionTab.mProfile == null || fusionTab.mProfile.isEmpty()) ? "default" : fusionTab.mProfile;
                    if (str2.equals(tabProf)) {
                        String tabEngine = fusionTab.mEngine != null ? fusionTab.mEngine : ENGINE_DEFAULT;
                        if (effectiveEngine.equals(tabEngine)) {
                            i = i2;
                        } else {
                            performDirectTabDestroy(fusionTab);
                        }
                        break;
                    }
                }
            }
        }
        FusionTab targetTab = null;
        if (i >= 0) {
            FusionTab fusionTab2 = (FusionTab) this.mTabList.get(i);
            fusionTab2.mUrl = str;
            fusionTab2.mIsDesktop = z2;
            fusionTab2.mIsHeadless = isHeadless;
            fusionTab2.mIsMinimized = isHeadless;
            fusionTab2.mWalletName = str3;
            fusionTab2.mWalletAddress = strFindAddressByName;
            String autoChain2 = FusionChainHelper.detectChainFromUrl(str);
            if (autoChain2 != null && !autoChain2.isEmpty()) {
                fusionTab2.mChainKey = autoChain2;
                fusionTab2.mChainId = FusionChainHelper.getChainIdByKey(autoChain2);
            } else if (FusionChainHelper.isNonEvmKey(fusionTab2.mChainKey) && !FusionChainHelper.isNonEvmUrl(str)) {
                fusionTab2.mChainKey = (FusionChainHelper.sLastEvmChainKey != null && !FusionChainHelper.sLastEvmChainKey.isEmpty()) ? FusionChainHelper.sLastEvmChainKey : "bsc";
                fusionTab2.mChainId = FusionChainHelper.sLastEvmChainId;
            }
            targetTab = fusionTab2;
            switchTab(i);
            if (fusionTab2.isGecko()) {
                FusionGeckoHelper.setDesktopMode(fusionTab2, z2);
                FusionGeckoHelper.loadUrl(fusionTab2, str);
            } else {
                FusionProfileManager.applyProfile(str2, fusionTab2.mWebView, z2);
                if (fusionTab2.mWebView != null) {
                    if (z2) {
                        applyDesktopSettingsToWebView(fusionTab2.mWebView);
                    }
                    fusionTab2.mWebView.loadUrl(str);
                }
            }
        } else {
            FusionTab fusionTabCreateTabInternal = createTabInternal(str, str2, str3, str4, z, z2, z3, str5, isHeadless, effectiveEngine);
            targetTab = fusionTabCreateTabInternal;
            if (targetTab != null) {
                targetTab.mWalletName = str3;
                targetTab.mWalletAddress = strFindAddressByName;
                if (isHeadless) {
                    targetTab.mKeepAliveBackground = true;
                }
            }
            // Jangan bajak tab visual yang sedang aktif dilihat user jika tab baru adalah headless background worker
            if (!skipUiSwitch) {
                switchTab(this.mTabList.size() - 1);
            }
            if (fusionTabCreateTabInternal != null) {
                if (fusionTabCreateTabInternal.isGecko()) {
                    FusionGeckoHelper.loadUrl(fusionTabCreateTabInternal, str);
                } else if (fusionTabCreateTabInternal.mWebView != null) {
                    final WebView newWv = fusionTabCreateTabInternal.mWebView;
                    final String targetLoadUrl = str;
                    if (this.mIsDevMode && (str.startsWith("http://") || str.startsWith("https://"))) {
                        newWv.loadUrl("about:blank");
                        FusionProfileManager.DeviceProfile devProf = FusionProfileManager.getDeviceForProfile(str2);
                        FusionCdpBridge.applyUserAgentOverrideAsync(this.mActivity, str2, devProf, z2, () -> newWv.loadUrl(targetLoadUrl));
                    } else {
                        newWv.loadUrl(str);
                    }
                }
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
                    if (!skipUiSwitch) FusionProxyHelper.updateProxyIndicator(this, targetTab);
                } else {
                    FusionProxyHelper.configureTabProxy(targetTab, proxyStr, intervalMins * 60, this);
                }
            } else {
                if (targetTab.mIsProxyEnabled && targetTab.mProxyItem != null && targetTab.mProxyItem.isValid()) {
                    if (targetTab.mAutoRotateIntervalSec > 0) {
                        FusionProxyHelper.startAutoRotateScheduler(this);
                    }
                    FusionProxyHelper.applyProxyToEngine(targetTab);
                    if (!skipUiSwitch) FusionProxyHelper.updateProxyIndicator(this, targetTab);
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
                        if (!skipUiSwitch) FusionProxyHelper.updateProxyIndicator(this, targetTab);
                    } else {
                        targetTab.mIsProxyEnabled = false;
                        targetTab.mAutoRotateIntervalSec = 0;
                        FusionProxyHelper.applyProxyToEngine(targetTab);
                        if (!skipUiSwitch) FusionProxyHelper.updateProxyIndicator(this, targetTab);
                    }
                }
            }
        }
        if (!skipUiSwitch) {
            syncVisibility();
            updateContextUi();
            if (targetTab == null || !targetTab.mIsHeadless) {
                FusionToolPillHelper.showToolPill(this);
                FusionToolPillHelper.bringToFront();
            } else {
                FusionToolPillHelper.hideToolPill(this);
                if (this.mControlPill != null) {
                    this.mControlPill.setVisibility(View.GONE);
                }
                if (this.mToolScroll != null) {
                    this.mToolScroll.setVisibility(View.GONE);
                }
            }
        }
    }

    public void openWorkerTab(String str) {
        addNewTab(str);
    }

    public void performQuickBack() {
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null && active.isGecko()) {
            FusionGeckoHelper.goBack(active);
            return;
        }
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
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null && active.isGecko()) {
            FusionGeckoHelper.goForward(active);
            return;
        }
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
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        String url = null;
        String title = null;
        if (active != null && active.isGecko()) {
            url = active.mUrl;
            title = active.mTitle;
        } else {
            WebView webView = (this.mActiveTab != 1 || this.mWorkerWebView == null) ? this.mActiveWebView : this.mWorkerWebView;
            if (webView != null) {
                url = webView.getUrl();
                title = webView.getTitle();
            }
        }
        if (url == null || url.isEmpty()) {
            FusionToast.makeText(act, "Tidak ada halaman aktif untuk disimpan", Toast.LENGTH_SHORT).show();
            return;
        }
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
        View activeView = getActiveTabView();
        if (activeView != null && activeView.getVisibility() == View.VISIBLE) {
            return true;
        }
        WebView webView = (this.mActiveTab == 1 && this.mWorkerWebView != null) ? this.mWorkerWebView : this.mActiveWebView;
        return (webView != null && webView.getVisibility() == View.VISIBLE)
            || (this.mMenuCard != null && this.mMenuCard.getVisibility() == View.VISIBLE);
    }

    public boolean isWebViewOpen() {
        if (this.mTabList == null || this.mTabList.isEmpty()) {
            return false;
        }
        FusionTab active = FusionTabStateHelper.getActiveTab(this);
        if (active != null) {
            if (active.mIsHeadless) {
                return false;
            }
            if (active.mIsMinimized) {
                return true;
            }
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
            boolean terminalVisible = this.mTerminalPaneHost != null && this.mTerminalPaneHost.getVisibility() == View.VISIBLE;
            if (targetTab.mBoundSession == null) {
                targetTab.mBoundSession = termuxActivity.getCurrentSession();
            } else if (!terminalVisible && !targetTab.mIsHeadless && !targetTab.mIsMinimized
                    && termuxActivity.getTermuxTerminalSessionClient() != null
                    && termuxActivity.getCurrentSession() != targetTab.mBoundSession) {
                termuxActivity.getTermuxTerminalSessionClient().setCurrentSession(targetTab.mBoundSession);
            }
        }
        for (int i2 = 0; i2 < size; i2++) {
            FusionTab fusionTab = (FusionTab) arrayList.get(i2);
            View renderView = fusionTab != null ? fusionTab.getRenderView() : null;
            if (i2 == i) {
                if (renderView != null) {
                    if (fusionTab.mIsMinimized || fusionTab.mIsHeadless) {
                        renderView.setVisibility(View.VISIBLE);
                        renderView.setTranslationX(fusionTab.mIsHeadless ? -10000.0f : 0.0f);
                        if (fusionTab.mWebView != null) {
                            fusionTab.mWebView.resumeTimers();
                        }
                        if (fusionTab.isGecko()) {
                            FusionGeckoHelper.setActive(fusionTab, true);
                        }
                        TextView textView = this.mMinPillBtn;
                        if (textView != null) {
                            textView.setText(" MAX ");
                        }
                        if (this.mTerminalPaneHost != null) {
                            this.mTerminalPaneHost.setVisibility(View.VISIBLE);
                            this.mTerminalPaneHost.bringToFront();
                        }
                    } else {
                        renderView.setTranslationX(0.0f);
                        renderView.setVisibility(View.VISIBLE);
                        if (fusionTab.isGecko()) {
                            FusionGeckoHelper.setActive(fusionTab, true);
                        }
                        renderView.bringToFront();
                        TextView textView2 = this.mMinPillBtn;
                        if (textView2 != null) {
                            textView2.setText(" MIN ");
                        }
                        if (this.mTerminalPaneHost != null) {
                            this.mTerminalPaneHost.setVisibility(View.GONE);
                        }
                        if (termuxActivity != null) {
                            KeyboardUtils.setSoftInputModeAdjustResize(termuxActivity);
                        }
                    }
                }
                this.mActiveWebView = fusionTab.mWebView;
                this.mCurrentProfile = fusionTab.mProfile;
                FusionProxyHelper.applyProxyToEngine(fusionTab);
                FusionProxyHelper.updateProxyIndicator(this, fusionTab);
            } else {
                if (renderView != null) {
                    if (fusionTab.mIsHeadless || fusionTab.mKeepAliveBackground) {
                        renderView.setTranslationX(-10000.0f);
                        renderView.setVisibility(View.VISIBLE);
                        if (fusionTab.mWebView != null) {
                            fusionTab.mWebView.resumeTimers();
                        }
                        if (fusionTab.isGecko()) {
                            FusionGeckoHelper.setActive(fusionTab, true);
                        }
                    } else {
                        renderView.setTranslationX(0.0f);
                        renderView.setVisibility(View.GONE);
                        if (fusionTab.isGecko()) {
                            FusionGeckoHelper.setActive(fusionTab, false);
                        }
                    }
                }
            }
        }
        if (targetTab != null && targetTab.mIsHeadless) {
            if (this.mPaneHost != null) {
                this.mPaneHost.setVisibility(View.INVISIBLE);
                this.mPaneHost.setTranslationX(-10000.0f);
            }
            if (this.mTerminalPaneHost != null) {
                this.mTerminalPaneHost.setVisibility(View.VISIBLE);
                this.mTerminalPaneHost.bringToFront();
            }
            if (this.mControlPill != null) {
                this.mControlPill.setVisibility(View.GONE);
            }
            if (this.mToolScroll != null) {
                this.mToolScroll.setVisibility(View.GONE);
            }
            if (termuxActivity != null) {
                termuxActivity.endWebViewExternalTextInput();
                termuxActivity.refreshTerminalWindowBar();
            }
        } else {
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
        }
        if (targetTab != null) {
            FusionTabStateHelper.syncTabState(this, targetTab);
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
            int curIdx = FusionWalletHelper.findIndexByName(this.mCurrentWalletName);
            if (curIdx >= 0) {
                this.mCurrentSolWalletName = FusionWalletHelper.getSolName(curIdx);
                this.mCurrentSolWalletAddress = FusionWalletHelper.getSolAddress(curIdx);
                this.mCurrentTronWalletName = FusionWalletHelper.getTronName(curIdx);
                this.mCurrentTronWalletAddress = FusionWalletHelper.getTronAddress(curIdx);
                this.mCurrentSuiWalletName = FusionWalletHelper.getSuiName(curIdx);
                this.mCurrentSuiWalletAddress = FusionWalletHelper.getSuiAddress(curIdx);
                this.mCurrentAptosWalletName = FusionWalletHelper.getAptosName(curIdx);
                this.mCurrentAptosWalletAddress = FusionWalletHelper.getAptosAddress(curIdx);
                this.mCurrentIotaWalletName = FusionWalletHelper.getIotaName(curIdx);
                this.mCurrentIotaWalletAddress = FusionWalletHelper.getIotaAddress(curIdx);
            } else {
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
            }
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
            // 1. Prioritaskan tab visual / minimized (non-headless) yang bound ke currentSession
            if (activeIdx >= 0 && activeIdx < size) {
                FusionTab candidate = (FusionTab) arrayList.get(activeIdx);
                if (candidate != null && candidate.mBoundSession != null && candidate.mBoundSession == currentSession && !candidate.mIsHeadless) {
                    i = activeIdx;
                }
            }
            if (i < 0 && currentSession != null) {
                for (int i2 = 0; i2 < size; i2++) {
                    FusionTab fusionTab = (FusionTab) arrayList.get(i2);
                    if (fusionTab != null && fusionTab.mBoundSession == currentSession && !fusionTab.mIsHeadless) {
                        i = i2;
                        break;
                    }
                }
            }
            // 2. Fallback: jika hanya ada tab headless di sesi ini
            if (i < 0 && currentSession != null) {
                if (activeIdx >= 0 && activeIdx < size) {
                    FusionTab candidate = (FusionTab) arrayList.get(activeIdx);
                    if (candidate != null && candidate.mBoundSession != null && candidate.mBoundSession == currentSession) {
                        i = activeIdx;
                    }
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
            // 3. Bind newly created tab that has no session yet
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
                View activeView3 = fusionTab2.getRenderView();
                this.mActiveWebView = webView3;
                FusionTabStateHelper.syncTabState(this, fusionTab2);
                for (int i3 = 0; i3 < size; i3++) {
                    FusionTab fusionTab3 = (FusionTab) arrayList.get(i3);
                    View otherView = fusionTab3 != null ? fusionTab3.getRenderView() : null;
                    if (otherView != null && i3 != i) {
                        otherView.setVisibility(8);
                        if (fusionTab3.mWebView != null) {
                            fusionTab3.mWebView.resumeTimers();
                        }
                        if (fusionTab3.isGecko()) {
                            FusionGeckoHelper.setActive(fusionTab3, fusionTab3.mIsHeadless || fusionTab3.mKeepAliveBackground);
                        }
                    }
                }
                if (fusionTab2.mIsHeadless) {
                    if (this.mPaneHost != null) {
                        this.mPaneHost.setVisibility(View.INVISIBLE);
                        this.mPaneHost.setTranslationX(-10000.0f);
                    }
                    if (this.mTerminalPaneHost != null) {
                        this.mTerminalPaneHost.setVisibility(View.VISIBLE);
                        this.mTerminalPaneHost.bringToFront();
                    }
                    if (this.mControlPill != null) {
                        this.mControlPill.setVisibility(View.GONE);
                    }
                    if (this.mToolScroll != null) {
                        this.mToolScroll.setVisibility(View.GONE);
                    }
                    FusionDrawerHelper.closeAllDrawers(this);
                    FusionRecPillHelper.hideRecPill();
                    termuxActivity.endWebViewExternalTextInput();
                    termuxActivity.refreshTerminalWindowBar();
                    return;
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
                    if (activeView3 != null) {
                        activeView3.setVisibility(8);
                    }
                    if (fusionTab2.isGecko()) {
                        FusionGeckoHelper.setActive(fusionTab2, false);
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
                if (activeView3 != null) {
                    if (activeView3.getParent() == null && (frameLayout2 = this.mPaneHost) != null) {
                        frameLayout2.addView(activeView3, new FrameLayout.LayoutParams(-1, -1));
                    }
                    View view2 = this.mTerminalPaneHost;
                    if (view2 != null) {
                        view2.setVisibility(8);
                    }
                    activeView3.setVisibility(0);
                    activeView3.setElevation(0.0f);
                    if (fusionTab2.isGecko()) {
                        FusionGeckoHelper.setActive(fusionTab2, true);
                    }
                    activeView3.bringToFront();
                    activeView3.requestFocus();
                    activeView3.requestFocusFromTouch();
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
            View rv4 = fusionTab4 != null ? fusionTab4.getRenderView() : null;
            if (rv4 != null) {
                rv4.setVisibility(8);
                if (fusionTab4.mWebView != null) {
                    fusionTab4.mWebView.resumeTimers();
                }
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

    public int setDevCdpEnabled(final boolean enable, final boolean showToast) {
        if (!enable) {
            this.mIsDevMode = false;
            if (this.mActivity != null) {
                this.mActivity.runOnUiThread(() -> {
                    WebView.setWebContentsDebuggingEnabled(false);
                    TextView tv = this.mDevModeBtn;
                    if (tv != null) {
                        tv.setText("Dev: OFF");
                        tv.setBackgroundColor(-4482645);
                    }
                    if (showToast) {
                        FusionToast.makeText((Context) this.mActivity, (CharSequence) "CDP NONAKTIF (OFF)", 0).show();
                    }
                });
            }
            FusionCdpBridge.stop();
            try { new File("/data/data/com.termux/files/home/.webview_cdp_socket").delete(); } catch (Exception unused) {}
            try { new File("/data/local/nhsystem/kali-arm64/root/webview_cdp_socket").delete(); } catch (Exception unused2) {}
            return 0;
        }
        this.mIsDevMode = true;
        final int pid = Process.myPid();
        final int cdpPort = FusionCdpBridge.start(pid);
        final String str = "webview_devtools_remote_" + pid;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/data/data/com.termux/files/home/.webview_cdp_socket");
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception unused3) {}
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream("/data/local/nhsystem/kali-arm64/root/webview_cdp_socket");
            fileOutputStream2.write(str.getBytes());
            fileOutputStream2.close();
        } catch (Exception unused4) {}
        if (this.mActivity != null) {
            this.mActivity.runOnUiThread(() -> {
                WebView.setWebContentsDebuggingEnabled(true);
                TextView textView2 = this.mDevModeBtn;
                if (textView2 != null) {
                    textView2.setText("Dev: ON (CDP)");
                    textView2.setBackgroundColor(-2263091);
                }
                if (showToast) {
                    String portInfo = cdpPort > 0 ? ("\nLocal TCP: 127.0.0.1:" + cdpPort + " (Zero-ADB)") : "";
                    FusionToast.makeText((Context) this.mActivity, (CharSequence) ("CDP AKTIF!\nSocket: @" + str + portInfo), 1).show();
                }
            });
        }
        return cdpPort;
    }

    public void toggleDevMode() {
        setDevCdpEnabled(!this.mIsDevMode, true);
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
            String activeChain = FusionChainHelper.getCurrentChainKey();
            if (FusionChainHelper.isNonEvm()) {
                String sym = FusionToolPillHelper.getShortNetworkName();
                sb.append("\n").append(sym).append(": ");
                String curAddr = GhostBridgeInterface.sActiveToolAddress;
                if (curAddr == null || curAddr.isEmpty() || (GhostBridgeInterface.sActiveToolChain != null && !GhostBridgeInterface.sActiveToolChain.isEmpty() && !activeChain.equalsIgnoreCase(GhostBridgeInterface.sActiveToolChain))) {
                    int curIdx = FusionWalletHelper.findIndexByName(this.mCurrentWalletName);
                    curAddr = FusionWalletHelper.getAddressForChain(activeChain, curIdx);
                }
                if (curAddr != null && !curAddr.isEmpty()) {
                    if (curAddr.length() <= 12) {
                        sb.append(curAddr);
                    } else {
                        sb.append(curAddr.substring(0, 6)).append("...").append(curAddr.substring(curAddr.length() - 4));
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
        if (this.mGeckoAddonMenuRow != null) {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(this);
            this.mGeckoAddonMenuRow.setVisibility(activeTab != null && activeTab.isGecko() ? View.VISIBLE : View.GONE);
        }
        FusionToolPillHelper.updateToolPillUi(this);
    }
}
