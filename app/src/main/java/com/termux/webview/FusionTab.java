package com.termux.webview;

import android.webkit.WebView;
import com.termux.terminal.TerminalSession;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionTab {
    public int mId;
    public boolean mIsAuto;
    public boolean mIsDesktop;
    public boolean mIsGhost;
    public String mProfile;
    public String mUrl;
    public WebView mWebView;
    public TerminalSession mBoundSession = null;
    public String mTitle = "New Tab";
    public boolean mIsMinimized = false;
    public boolean mIsPillCollapsed = false;
    public int mOpacityLevel = 0;
    public boolean mIsRecording = false;

    public FusionTab(int i, String str, String str2, WebView webView, boolean z, boolean z2, boolean z3) {
        this.mId = i;
        this.mUrl = str;
        this.mProfile = str2;
        this.mWebView = webView;
        this.mIsGhost = z;
        this.mIsDesktop = z2;
        this.mIsAuto = z3;
    }
}
