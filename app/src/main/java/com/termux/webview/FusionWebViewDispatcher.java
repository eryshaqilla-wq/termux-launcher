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
                fusionWebViewManager.openConfig(jSONObject.getString("url"), jSONObject.optString("profile", "default"), jSONObject.optString("wallet", ""), jSONObject.optString("chain", "base"), jSONObject.optBoolean("isGhost", true), jSONObject.optBoolean("isDesktop", false), jSONObject.optBoolean("isAuto", false), jSONObject.optString("wc", ""));
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
        } else if (str.startsWith("WEBVIEW_COLLAPSE_PILL")) {
            fusionWebViewManager.toggleCollapsePill();
        }
    }
}
