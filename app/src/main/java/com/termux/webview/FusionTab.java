package com.termux.webview;

import android.view.View;
import android.webkit.WebView;
import com.termux.terminal.TerminalSession;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTab {
    public int mId;
    public boolean mIsAuto;
    public boolean mIsDesktop;
    public boolean mIsGhost;
    public String mProfile;
    public String mUrl;
    public WebView mWebView;
    public String mEngine = "default";
    public GeckoView mGeckoView;
    public GeckoSession mGeckoSession;
    public boolean mGeckoCanGoBack = false;
    public boolean mGeckoCanGoForward = false;
    public TerminalSession mBoundSession = null;
    public String mTitle = "New Tab";
    public boolean mIsMinimized = false;
    public boolean mIsHeadless = false;
    public boolean mKeepAliveBackground = false;
    public boolean mIsPillCollapsed = false;
    public boolean mIsToolPillCollapsed = true;
    public boolean mHasFormFocus = false;
    public int mOpacityLevel = 0;
    public boolean mIsRecording = false;
    public FusionProxyHelper.ProxyItem mProxyItem = null;
    public boolean mIsProxyEnabled = false;
    public int mAutoRotateIntervalSec = 0;
    public long mLastProxyRotatedTime = 0;
    public String mChainKey = "bsc";
    public int mChainId = 56;
    public String mWalletName = "";
    public String mWalletAddress = "";
    public String mSolWalletName = "";
    public String mSolWalletAddress = "";
    public String mTronWalletName = "";
    public String mTronWalletAddress = "";
    public String mSuiWalletName = "";
    public String mSuiWalletAddress = "";
    public String mAptosWalletName = "";
    public String mAptosWalletAddress = "";
    public String mIotaWalletName = "";
    public String mIotaWalletAddress = "";
    public final String mBridgeToken = java.util.UUID.randomUUID().toString();

    public FusionTab(int i, String str, String str2, WebView webView, boolean z, boolean z2, boolean z3, boolean isHeadless) {
        this.mId = i;
        this.mUrl = str;
        this.mProfile = str2;
        this.mWebView = webView;
        this.mIsGhost = z;
        this.mIsDesktop = z2;
        this.mIsAuto = z3;
        this.mIsHeadless = isHeadless;
    }

    public FusionTab(int i, String str, String str2, WebView webView, boolean z, boolean z2, boolean z3) {
        this(i, str, str2, webView, z, z2, z3, false);
    }

    public boolean isGecko() {
        return "gecko".equalsIgnoreCase(this.mEngine) && this.mGeckoView != null;
    }

    public View getRenderView() {
        return isGecko() ? this.mGeckoView : this.mWebView;
    }
}
