package com.termux.webview;

import org.json.JSONObject;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionWebViewDispatcher {
    public static void dispatch(String str) {
        FusionWebViewManager fusionWebViewManager;
        String str2;
        String str3;
        String str4;
        if (str == null || (fusionWebViewManager = FusionWebViewManager.getInstance()) == null) {
            return;
        }
        if (str.startsWith("WEBVIEW_CONFIG ")) {
            try {
                JSONObject jSONObject = new JSONObject(str.substring(15));
                fusionWebViewManager.openConfig(
                    jSONObject.getString("url"),
                    jSONObject.optString("profile", "default"),
                    jSONObject.optString("wallet", ""),
                    jSONObject.optString("chain", "base"),
                    jSONObject.optBoolean("isGhost", true),
                    jSONObject.optBoolean("isDesktop", false),
                    jSONObject.optBoolean("isAuto", false),
                    jSONObject.optString("wc", ""),
                    jSONObject.optString("proxy", ""),
                    jSONObject.optInt("interval", 0)
                );
                return;
            } catch (Exception unused) {
                return;
            }
        }
        if (str.startsWith("WEBVIEW_GHOST ")) {
            String strSubstring = str.substring(14);
            int iIndexOf = strSubstring.indexOf(" ");
            if (iIndexOf != -1) {
                String strSubstring2 = strSubstring.substring(0, iIndexOf);
                strSubstring = strSubstring.substring(iIndexOf + 1);
                str4 = strSubstring2;
            } else {
                str4 = "default";
            }
            fusionWebViewManager.openConfig(strSubstring, str4, "", "base", true, false, false, "");
            return;
        }
        if (str.startsWith("WEBVIEW_GHOST_WALLET ")) {
            String strSubstring3 = str.substring(21);
            int iIndexOf2 = strSubstring3.indexOf(" ");
            if (iIndexOf2 == -1) {
                return;
            }
            String strSubstring4 = strSubstring3.substring(0, iIndexOf2);
            String strSubstring5 = strSubstring3.substring(iIndexOf2 + 1);
            int iIndexOf3 = strSubstring5.indexOf(" ");
            if (iIndexOf3 == -1) {
                return;
            }
            fusionWebViewManager.openConfig(strSubstring5.substring(iIndexOf3 + 1), strSubstring4, strSubstring5.substring(0, iIndexOf3), "base", true, false, false, "");
            return;
        }
        if (str.startsWith("WEBVIEW_OPEN ")) {
            String strSubstring6 = str.substring(13);
            int iIndexOf4 = strSubstring6.indexOf(" ");
            if (iIndexOf4 != -1) {
                String strSubstring7 = strSubstring6.substring(0, iIndexOf4);
                strSubstring6 = strSubstring6.substring(iIndexOf4 + 1);
                str3 = strSubstring7;
            } else {
                str3 = "default";
            }
            fusionWebViewManager.openConfig(strSubstring6, str3, "", "base", false, false, false, "");
            return;
        }
        if (str.startsWith("WEBVIEW_DESKTOP ")) {
            String strSubstring8 = str.substring(16);
            int iIndexOf5 = strSubstring8.indexOf(" ");
            if (iIndexOf5 != -1) {
                String strSubstring9 = strSubstring8.substring(0, iIndexOf5);
                strSubstring8 = strSubstring8.substring(iIndexOf5 + 1);
                str2 = strSubstring9;
            } else {
                str2 = "default";
            }
            fusionWebViewManager.openConfig(strSubstring8, str2, "", "base", false, true, false, "");
            return;
        }
        if (str.equals("WEBVIEW_CLOSE")) {
            fusionWebViewManager.closeWebView();
            return;
        }
        if (str.equals("WEBVIEW_BACK")) {
            new FusionWebViewManager.BackClick(fusionWebViewManager).onClick(null);
            return;
        }
        if (str.equals("WEBVIEW_RELOAD")) {
            new FusionWebViewManager.ReloadClick(fusionWebViewManager).onClick(null);
            return;
        }
        if (str.startsWith("WEBVIEW_EVAL ")) {
            fusionWebViewManager.evaluateJs(str.substring(13));
            return;
        }
        if (str.equals("WEBVIEW_SYNC_DB")) {
            fusionWebViewManager.syncDbManual();
            return;
        }
        if (str.equals("WEBVIEW_MACRO_TOGGLE")) {
            fusionWebViewManager.toggleMacroRecording();
            return;
        }
        if (str.equals("WEBVIEW_MACRO_PLAY")) {
            fusionWebViewManager.runMacro();
            return;
        }
        if (str.equals("WEBVIEW_MACRO_STOP")) {
            fusionWebViewManager.stopMacro();
            return;
        }
        if (str.equals("WEBVIEW_RANDOM_FP")) {
            fusionWebViewManager.randomizeFingerprint();
            return;
        }
        if (str.equals("WEBVIEW_OPACITY")) {
            fusionWebViewManager.cycleOpacity();
            return;
        }
        if (str.equals("WEBVIEW_DEV_MODE")) {
            fusionWebViewManager.toggleDevMode();
            return;
        }
        if (str.equals("WEBVIEW_DEVELOPER") || str.equals("WEBVIEW_DEV_TOOLS")) {
            fusionWebViewManager.toggleDeveloperTools();
            return;
        }
        if (str.equals("WEBVIEW_BRIDGE")) {
            fusionWebViewManager.triggerAiDevBridge();
            return;
        }
        if (str.startsWith("WEBVIEW_PROXY_SET ")) {
            String proxyStr = str.substring(18).trim();
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
            if (activeTab != null) {
                FusionProxyHelper.ProxyItem item = FusionProxyHelper.parse(proxyStr);
                if (item != null && item.isValid()) {
                    activeTab.mProxyItem = item;
                    activeTab.mIsProxyEnabled = true;
                    String profKey = (activeTab.mProfile != null && !activeTab.mProfile.isEmpty()) ? activeTab.mProfile : "default";
                    FusionProxyHelper.savePersistentState(fusionWebViewManager.mActivity, profKey, true, item, activeTab.mAutoRotateIntervalSec, "custom", null);
                    if (!"default".equals(profKey)) {
                        FusionProxyHelper.savePersistentState(fusionWebViewManager.mActivity, "default", true, item, activeTab.mAutoRotateIntervalSec, "custom", null);
                    }
                    FusionProxyHelper.applyProxyToEngine(activeTab);
                    FusionProxyHelper.updateProxyIndicator(fusionWebViewManager, activeTab);
                }
            }
            return;
        }
        if (str.equals("WEBVIEW_PROXY_CLEAR") || str.equals("WEBVIEW_PROXY_OFF")) {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
            if (activeTab != null) {
                activeTab.mIsProxyEnabled = false;
                activeTab.mProxyItem = null;
                activeTab.mAutoRotateIntervalSec = 0;
                String profKey = (activeTab.mProfile != null && !activeTab.mProfile.isEmpty()) ? activeTab.mProfile : "default";
                FusionProxyHelper.savePersistentState(fusionWebViewManager.mActivity, profKey, false, null, 0, "direct", null);
                if (!"default".equals(profKey)) {
                    FusionProxyHelper.savePersistentState(fusionWebViewManager.mActivity, "default", false, null, 0, "direct", null);
                }
                FusionProxyHelper.applyProxyToEngine(activeTab);
                FusionProxyHelper.updateProxyIndicator(fusionWebViewManager, activeTab);
            }
            return;
        }
        if (str.equals("WEBVIEW_PROXY_DIALOG") || str.equals("WEBVIEW_PROXY")) {
            fusionWebViewManager.showProxyDialog();
            return;
        }
        if (str.startsWith("WEBVIEW_PROXY_ROTATE")) {
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
            if (activeTab != null) {
                String sub = str.length() > 20 ? str.substring(20).trim() : "";
                if (!sub.isEmpty()) {
                    try {
                        int mins = Integer.parseInt(sub);
                        activeTab.mAutoRotateIntervalSec = mins * 60;
                        activeTab.mLastProxyRotatedTime = System.currentTimeMillis();
                        if (mins > 0) {
                            FusionProxyHelper.startAutoRotateScheduler(fusionWebViewManager);
                        }
                    } catch (Exception ignored) {}
                }
                FusionProxyHelper.rotateProxyForTab(activeTab, fusionWebViewManager, null);
            }
            return;
        }
        if (str.startsWith("WEBVIEW_PROXY_INTERVAL ") || str.startsWith("WEBVIEW_PROXY_AUTO ")) {
            String valStr = str.substring(str.indexOf(" ") + 1).trim();
            FusionTab activeTab = FusionTabStateHelper.getActiveTab(fusionWebViewManager);
            if (activeTab != null) {
                try {
                    int mins = Integer.parseInt(valStr);
                    activeTab.mAutoRotateIntervalSec = mins * 60;
                    activeTab.mLastProxyRotatedTime = System.currentTimeMillis();
                    if (mins > 0) {
                        FusionProxyHelper.startAutoRotateScheduler(fusionWebViewManager);
                    }
                } catch (Exception ignored) {}
            }
            return;
        }
        if (str.startsWith("WEBVIEW_WORKER")) {
            fusionWebViewManager.openWorkerTab(str.length() > 15 ? str.substring(15) : "https://x.com");
            return;
        }
        if (str.equals("WEBVIEW_KILL_WORKER")) {
            fusionWebViewManager.killWorkerTab();
            return;
        }
        if (str.startsWith("WEBVIEW_SWITCH_TAB ")) {
            try {
                fusionWebViewManager.switchTab(Integer.parseInt(str.substring(19).trim()));
                return;
            } catch (Exception unused2) {
                return;
            }
        }
        if (str.startsWith("WEBVIEW_NEW_TAB")) {
            fusionWebViewManager.addNewTab(str.length() > 16 ? str.substring(16).trim() : "https://google.com");
            return;
        }
        if (str.startsWith("WEBVIEW_KILL_TAB")) {
            if (str.length() <= 17) {
                fusionWebViewManager.killActiveTab();
                return;
            } else {
                try {
                    fusionWebViewManager.killTab(Integer.parseInt(str.substring(17).trim()));
                    return;
                } catch (Exception unused3) {
                    return;
                }
            }
        }
        if (str.equals("WEBVIEW_SHOW_TABS") || str.equals("WEBVIEW_TABS")) {
            fusionWebViewManager.showTabManagerDialog();
            return;
        }
        if (str.startsWith("WEBVIEW_AGY_AUTOFILL")) {
            fusionWebViewManager.executeAgyAction(1);
            return;
        }
        if (str.startsWith("WEBVIEW_AGY_CLAIM")) {
            fusionWebViewManager.executeAgyAction(2);
            return;
        }
        if (str.startsWith("WEBVIEW_AGY_RECIPE_RUN")) {
            fusionWebViewManager.executeAgyAction(4);
            return;
        }
        if (str.startsWith("WEBVIEW_CLOUDFLARE_SOLVE")) {
            fusionWebViewManager.evalOnActiveTab("window.AgyBridge && window.AgyBridge.solveTurnstile && window.AgyBridge.solveTurnstile();");
        } else if (str.startsWith("WEBVIEW_CLEAR_CACHE")) {
            fusionWebViewManager.clearWebviewCache();
        } else if (str.startsWith("WEBVIEW_CLEAR_COOKIES")) {
            FusionProfileManager.clearAllCookiesAndStorage();
            fusionWebViewManager.clearWebviewCache();
        } else if (str.startsWith("WEBVIEW_SAVE_SESSION ") || str.startsWith("WEBVIEW_SAVE ")) {
            String prof = str.substring(str.indexOf(" ") + 1).trim();
            FusionProfileManager.exportProfileSessionToJson(fusionWebViewManager.mActivity, prof);
        } else if (str.startsWith("WEBVIEW_COLLAPSE_PILL")) {
            fusionWebViewManager.toggleCollapsePill();
        } else if (str.equals("WEBVIEW_EXTENSIONS") || str.equals("WEBVIEW_EXTENSION_DIALOG")) {
            fusionWebViewManager.showExtensionsDialog();
        } else if (str.startsWith("WEBVIEW_EXTENSION_INSTALL ")) {
            String path = str.substring(26).trim();
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                boolean ok = FusionExtensionHelper.installPackage(fusionWebViewManager.mActivity, f);
                if (ok && fusionWebViewManager.mActiveWebView != null) {
                    FusionExtensionHelper.setupWebView(fusionWebViewManager.mActiveWebView);
                }
            }
        } else if (str.startsWith("WEBVIEW_EXTENSION_LAYER ") || str.startsWith("WEBVIEW_EXT_LAYER ")) {
            String layer = str.substring(str.indexOf(" ") + 1).trim();
            FusionExtensionHelper.setLayerConfig(fusionWebViewManager.mActivity, layer);
            if (fusionWebViewManager.mActiveWebView != null) {
                fusionWebViewManager.mActiveWebView.evaluateJavascript(
                    "(function(){ if(window.chrome&&window.chrome.storage&&window.chrome.storage.local) { window.chrome.storage.local.set({gapt_layer:'" + layer + "'}); } window.postMessage({__gapt:1,type:'cfg',layer:'" + layer + "'},'*'); })();", null);
            }
        }
    }
}
