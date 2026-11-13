package com.termux.webview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.termux.app.FusionToast;

public class FusionToolPillHelper {
    public static HorizontalScrollView sToolScroll;
    public static LinearLayout sToolPill;
    public static TextView sToggleBtn;
    public static TextView sWalletNextBtn;
    public static TextView sNetWalletBtn;
    public static TextView sCopyWalletBtn;
    public static TextView sReloadBtn;
    public static TextView sWipeBtn;
    public static TextView sFpBtn;
    public static TextView sCollapseBtn;

    // Default selalu menciut (collapsed)
    public static boolean sIsCollapsed = true;
    public static String sCurrentFpName = "S24 Ultra";
    public static boolean sHasFormFocus = false;

    public static void setFormFocus(FusionWebViewManager manager, boolean hasFocus) {
        sHasFormFocus = hasFocus;
        if (manager != null) {
            FusionTab tab = FusionTabStateHelper.getActiveTab(manager);
            if (tab != null) {
                tab.mHasFormFocus = hasFocus;
            }
            updateToolPillUi(manager);
        }
    }

    public static String getShortNetworkName() {
        String key = FusionChainHelper.getCurrentChainKey();
        if ("solana".equalsIgnoreCase(key)) return "Sol";
        if ("tron".equalsIgnoreCase(key)) return "Tron";
        if ("sui".equalsIgnoreCase(key)) return "Sui";
        if ("aptos".equalsIgnoreCase(key)) return "Aptos";
        if ("iota".equalsIgnoreCase(key)) return "IOTA";
        String name = FusionChainHelper.getCurrentChainName();
        if (name == null || name.isEmpty()) return "EVM";
        String lower = name.toLowerCase();
        if (lower.contains("base")) return "Base";
        if (lower.contains("bnb") || lower.contains("bsc")) return "BSC";
        if (lower.contains("arb")) return "Arb";
        if (lower.contains("op")) return "OP";
        if (lower.contains("poly")) return "Polygon";
        if (lower.contains("eth")) return "ETH";
        if (lower.contains("blast")) return "Blast";
        if (lower.contains("linea")) return "Linea";
        if (lower.contains("scroll")) return "Scroll";
        if (lower.contains("avax")) return "AVAX";
        if (name.length() > 6) {
            int spaceIdx = name.indexOf(' ');
            if (spaceIdx > 0) return name.substring(0, spaceIdx);
            return name.substring(0, 6);
        }
        return name;
    }

    public static String formatShortFpName(String name) {
        if (name == null || name.isEmpty()) return "Faker";
        String n = name.trim();
        if (n.startsWith("Samsung Galaxy ")) {
            return n.substring("Samsung Galaxy ".length());
        }
        if (n.startsWith("Google ")) {
            return n.substring("Google ".length());
        }
        if (n.length() > 14) {
            return n.substring(0, 14);
        }
        return n;
    }

    public static void initToolPill(final FusionWebViewManager manager) {
        if (manager == null || manager.mActivity == null || manager.mPaneHost == null) {
            return;
        }
        final AppCompatActivity act = manager.mActivity;
        FrameLayout host = manager.mPaneHost;

        float density = act.getResources().getDisplayMetrics().density;

        // Container scroll horizontal responsif
        HorizontalScrollView scroll = new HorizontalScrollView(act);
        manager.mToolScroll = scroll;
        sToolScroll = scroll;
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setElevation(160.0f);

        // Box styling 100% identik dengan mControlPill
        LinearLayout pill = new LinearLayout(act);
        manager.mToolPill = pill;
        sToolPill = pill;
        pill.setLayerType(1, null);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        FusionUiHelper.setSolidBox(pill);
        pill.setPadding(6, 4, 6, 4);
        scroll.addView(pill, new ViewGroup.LayoutParams(-2, -2));

        // 0. Tombol saat menciut: [ TOOL ] (tanpa icon, style identik dengan bar atas)
        TextView toggleBtn = new TextView(act);
        manager.mToolToggleBtn = toggleBtn;
        sToggleBtn = toggleBtn;
        toggleBtn.setText(" TOOL ");
        FusionUiHelper.setSolidBtn(toggleBtn);
        toggleBtn.setTextSize(2, 11.0f);
        toggleBtn.setPadding(8, 4, 8, 4);
        toggleBtn.setClickable(true);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCollapse(manager);
            }
        });
        LinearLayout.LayoutParams toggleLp = new LinearLayout.LayoutParams(-2, -2);
        toggleLp.setMargins(2, 0, 2, 0);
        pill.addView(toggleBtn, toggleLp);

        // 1. Next Wallet: [ WALLET > ]
        TextView walletNextBtn = new TextView(act);
        manager.mToolWalletNextBtn = walletNextBtn;
        sWalletNextBtn = walletNextBtn;
        walletNextBtn.setText(" WALLET > ");
        FusionUiHelper.setSolidBtn(walletNextBtn);
        walletNextBtn.setTextSize(2, 11.0f);
        walletNextBtn.setPadding(8, 4, 8, 4);
        walletNextBtn.setClickable(true);
        walletNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nextAddr = manager.switchToNextWallet();
                updateToolPillUi(manager);
                WebView webView = manager.mActiveWebView;
                if (webView != null && nextAddr != null) {
                    webView.evaluateJavascript("if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'next_wallet', timestamp:Date.now(), text:'Ganti Wallet -> " + nextAddr + "'}); try { window.dispatchEvent(new CustomEvent('accountsChanged', {detail: ['" + nextAddr + "']})); } catch(_){}", null);
                }
            }
        });
        LinearLayout.LayoutParams wnLp = new LinearLayout.LayoutParams(-2, -2);
        wnLp.setMargins(2, 0, 2, 0);
        pill.addView(walletNextBtn, wnLp);

        // 2. Network + No Wallet: [ Base eso-1 ] -> Sekali klik memunculkan drawer
        TextView netWalletBtn = new TextView(act);
        manager.mToolNetWalletBtn = netWalletBtn;
        sNetWalletBtn = netWalletBtn;
        netWalletBtn.setText(" Base eso-1 ");
        FusionUiHelper.setSolidBtn(netWalletBtn);
        netWalletBtn.setTextSize(2, 11.0f);
        netWalletBtn.setPadding(8, 4, 8, 4);
        netWalletBtn.setClickable(true);
        netWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusionDrawerHelper.showWalletDrawer(manager);
            }
        });
        LinearLayout.LayoutParams nwLp = new LinearLayout.LayoutParams(-2, -2);
        nwLp.setMargins(2, 0, 2, 0);
        pill.addView(netWalletBtn, nwLp);

        // 3. Copy / Paste Dual Button: [ COPY ] or [ PASTE ]
        TextView copyWalletBtn = new TextView(act);
        manager.mToolCopyWalletBtn = copyWalletBtn;
        sCopyWalletBtn = copyWalletBtn;
        copyWalletBtn.setText(sHasFormFocus ? " PASTE " : " COPY ");
        FusionUiHelper.setSolidBtn(copyWalletBtn);
        copyWalletBtn.setTextSize(2, 11.0f);
        copyWalletBtn.setPadding(8, 4, 8, 4);
        copyWalletBtn.setClickable(true);
        copyWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chain = FusionChainHelper.getCurrentChainKey();
                String addr = manager.mCurrentWalletAddress;
                if ("solana".equalsIgnoreCase(chain)) {
                    addr = manager.mCurrentSolWalletAddress;
                } else if ("tron".equalsIgnoreCase(chain)) {
                    addr = manager.mCurrentTronWalletAddress;
                } else if ("sui".equalsIgnoreCase(chain)) {
                    addr = manager.mCurrentSuiWalletAddress;
                } else if ("aptos".equalsIgnoreCase(chain)) {
                    addr = manager.mCurrentAptosWalletAddress;
                } else if ("iota".equalsIgnoreCase(chain)) {
                    addr = manager.mCurrentIotaWalletAddress;
                }
                if (addr == null || addr.isEmpty()) {
                    addr = manager.mCurrentWalletAddress;
                }
                if (addr == null || addr.isEmpty()) {
                    addr = FusionWalletHelper.getMasterAddress();
                }

                try {
                    ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null && addr != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("wallet_address", addr));
                    }
                } catch (Throwable ignored) {}

                WebView wv = manager.mActiveWebView;
                if (sHasFormFocus) {
                    if (wv != null && addr != null) {
                        wv.evaluateJavascript("(function(){ if (window.__AGY_PASTE_WALLET__) return window.__AGY_PASTE_WALLET__('" + addr + "'); })()", null);
                    }
                    FusionToast.makeText((Context) act, (CharSequence) ("Alamat (" + getShortNetworkName() + ") ditempel ke form!"), 0).show();
                } else {
                    if (wv != null && addr != null) {
                        wv.evaluateJavascript("if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'copy_wallet', timestamp:Date.now(), text:'Salin Wallet -> " + addr + "', value:'" + addr + "'});", null);
                    }
                    String shortAddr = (addr != null && addr.length() > 14) ? (addr.substring(0, 6) + "..." + addr.substring(addr.length() - 4)) : addr;
                    FusionToast.makeText((Context) act, (CharSequence) ("Alamat (" + getShortNetworkName() + ") disalin: " + shortAddr), 0).show();
                }
            }
        });
        LinearLayout.LayoutParams cwLp = new LinearLayout.LayoutParams(-2, -2);
        cwLp.setMargins(2, 0, 2, 0);
        pill.addView(copyWalletBtn, cwLp);

        // 4. Reload: [ RELOAD ]
        TextView reloadBtn = new TextView(act);
        manager.mToolReloadBtn = reloadBtn;
        sReloadBtn = reloadBtn;
        reloadBtn.setText(" RELOAD ");
        FusionUiHelper.setSolidBtn(reloadBtn);
        reloadBtn.setTextSize(2, 11.0f);
        reloadBtn.setPadding(8, 4, 8, 4);
        reloadBtn.setClickable(true);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView webView = manager.mActiveWebView;
                if (webView != null) {
                    webView.evaluateJavascript("if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'reload', timestamp:Date.now(), text:'Reload Halaman'}); try{sessionStorage.setItem('__agy_rec_active__','true'); sessionStorage.setItem('__fusion_rec_active__','true');}catch(_){}", null);
                }
                manager.reloadActiveTab();
                FusionToast.makeText((Context) act, (CharSequence) "Reloading...", 0).show();
            }
        });
        LinearLayout.LayoutParams relLp = new LinearLayout.LayoutParams(-2, -2);
        relLp.setMargins(2, 0, 2, 0);
        pill.addView(reloadBtn, relLp);

        // 5. Wipe: [ WIPE ]
        TextView wipeBtn = new TextView(act);
        manager.mToolWipeBtn = wipeBtn;
        sWipeBtn = wipeBtn;
        wipeBtn.setText(" WIPE ");
        FusionUiHelper.setSolidBtn(wipeBtn);
        wipeBtn.setTextSize(2, 11.0f);
        wipeBtn.setPadding(8, 4, 8, 4);
        wipeBtn.setClickable(true);
        wipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebView wv = manager.mActiveWebView;
                if (wv != null) {
                    wv.clearCache(true);
                    wv.clearFormData();
                    try { WebStorage.getInstance().deleteAllData(); } catch(Throwable ignored) {}
                    wv.evaluateJavascript("try { localStorage.clear(); sessionStorage.clear(); sessionStorage.setItem('__agy_rec_active__','true'); sessionStorage.setItem('__fusion_rec_active__','true'); } catch(e){}; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'wipe_cache', timestamp:Date.now(), text:'Wipe Cache & Storage'});", null);
                }
                FusionProfileManager.clearAllCookiesAndStorage();
                manager.reloadActiveTab();
                FusionToast.makeText((Context) act, (CharSequence) "Cache & Storage Dibersihkan!", 0).show();
            }
        });
        LinearLayout.LayoutParams wpLp = new LinearLayout.LayoutParams(-2, -2);
        wpLp.setMargins(2, 0, 2, 0);
        pill.addView(wipeBtn, wpLp);

        // 6. FP Info & Rotate: [ FP: S24 Ultra ]
        TextView fpBtn = new TextView(act);
        manager.mToolFpBtn = fpBtn;
        sFpBtn = fpBtn;
        fpBtn.setText(" FP: S24 Ultra ");
        FusionUiHelper.setSolidBtn(fpBtn);
        fpBtn.setTextSize(2, 11.0f);
        fpBtn.setPadding(8, 4, 8, 4);
        fpBtn.setClickable(true);
        fpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDev = manager.rotateDeviceFingerprint();
                if (newDev != null && !newDev.isEmpty()) {
                    sCurrentFpName = newDev;
                }
                updateToolPillUi(manager);
                WebView wv = manager.mActiveWebView;
                if (wv != null) {
                    wv.evaluateJavascript("(function(){ var fp = '" + sCurrentFpName + "'; if (window.AgyRecorder) window.AgyRecorder.recordEvent({type:'fingerprint', timestamp:Date.now(), text:'Fingerprint -> ' + fp, value:fp}); try { updateAgyHUD('Fingerprint Faker', 'Model HP: ' + fp, 'success'); } catch(_){}; return fp; })()", null);
                }
            }
        });
        LinearLayout.LayoutParams fpLp = new LinearLayout.LayoutParams(-2, -2);
        fpLp.setMargins(2, 0, 2, 0);
        pill.addView(fpBtn, fpLp);

        // 7. Collapse: [ > ]
        TextView collapseBtn = new TextView(act);
        manager.mToolCollapseBtn = collapseBtn;
        sCollapseBtn = collapseBtn;
        collapseBtn.setText(" > ");
        FusionUiHelper.setSolidBtn(collapseBtn);
        collapseBtn.setTextSize(2, 11.0f);
        collapseBtn.setPadding(8, 4, 8, 4);
        collapseBtn.setClickable(true);
        collapseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse(manager);
            }
        });
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(-2, -2);
        colLp.setMargins(2, 0, 2, 0);
        pill.addView(collapseBtn, colLp);

        // Add to pane host
        FrameLayout.LayoutParams pillLp = new FrameLayout.LayoutParams(-2, -2, 8388661);
        int topMargin = (int) (58 * density);
        if (manager.mControlPill != null && manager.mControlPill.getBottom() > 0) {
            topMargin = manager.mControlPill.getBottom() + (int) (4 * density);
        }
        pillLp.setMargins(0, topMargin, 10, 0);
        host.addView(scroll, pillLp);

        if (manager.mControlPill != null) {
            manager.mControlPill.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    syncToolPillPosition(manager);
                }
            });
        }

        updateToolPillUi(manager);
    }

    public static void initAndShowToolPill(final FusionWebViewManager manager) {
        if (manager == null) return;
        if (manager.mToolScroll == null) {
            initToolPill(manager);
        }
        showToolPill(manager);
    }

    public static void toggleCollapse(FusionWebViewManager manager) {
        if (manager == null) return;
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(manager);
        if (activeTab != null) {
            activeTab.mIsToolPillCollapsed = !activeTab.mIsToolPillCollapsed;
            sIsCollapsed = activeTab.mIsToolPillCollapsed;
        } else {
            sIsCollapsed = !sIsCollapsed;
        }
        updateToolPillUi(manager);
    }

    public static void collapse(FusionWebViewManager manager) {
        if (manager == null) return;
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(manager);
        if (activeTab != null) {
            activeTab.mIsToolPillCollapsed = true;
            sIsCollapsed = true;
        } else {
            sIsCollapsed = true;
        }
        updateToolPillUi(manager);
    }

    public static void updateToolPillUi(FusionWebViewManager manager) {
        if (manager == null) return;
        if (!manager.isWebViewOpen()) {
            hideToolPill(manager);
            return;
        }
        HorizontalScrollView scroll = manager.mToolScroll != null ? manager.mToolScroll : sToolScroll;
        if (scroll == null) return;

        if (manager.mToolPill != null) {
            manager.mToolPill.setVisibility(View.VISIBLE);
        }
        if (sToolPill != null) {
            sToolPill.setVisibility(View.VISIBLE);
        }

        FusionTab activeTab = FusionTabStateHelper.getActiveTab(manager);
        boolean isCollapsed = activeTab != null ? activeTab.mIsToolPillCollapsed : sIsCollapsed;

        TextView toggleBtn = manager.mToolToggleBtn != null ? manager.mToolToggleBtn : sToggleBtn;
        TextView walletNextBtn = manager.mToolWalletNextBtn != null ? manager.mToolWalletNextBtn : sWalletNextBtn;
        TextView netWalletBtn = manager.mToolNetWalletBtn != null ? manager.mToolNetWalletBtn : sNetWalletBtn;
        TextView copyWalletBtn = manager.mToolCopyWalletBtn != null ? manager.mToolCopyWalletBtn : sCopyWalletBtn;
        TextView reloadBtn = manager.mToolReloadBtn != null ? manager.mToolReloadBtn : sReloadBtn;
        TextView wipeBtn = manager.mToolWipeBtn != null ? manager.mToolWipeBtn : sWipeBtn;
        TextView fpBtn = manager.mToolFpBtn != null ? manager.mToolFpBtn : sFpBtn;
        TextView collapseBtn = manager.mToolCollapseBtn != null ? manager.mToolCollapseBtn : sCollapseBtn;

        if (isCollapsed) {
            if (toggleBtn != null) toggleBtn.setVisibility(View.VISIBLE);
            if (walletNextBtn != null) walletNextBtn.setVisibility(View.GONE);
            if (netWalletBtn != null) netWalletBtn.setVisibility(View.GONE);
            if (copyWalletBtn != null) copyWalletBtn.setVisibility(View.GONE);
            if (reloadBtn != null) reloadBtn.setVisibility(View.GONE);
            if (wipeBtn != null) wipeBtn.setVisibility(View.GONE);
            if (fpBtn != null) fpBtn.setVisibility(View.GONE);
            if (collapseBtn != null) collapseBtn.setVisibility(View.GONE);
        } else {
            if (toggleBtn != null) toggleBtn.setVisibility(View.GONE);
            if (walletNextBtn != null) walletNextBtn.setVisibility(View.VISIBLE);
            if (netWalletBtn != null) netWalletBtn.setVisibility(View.VISIBLE);
            if (copyWalletBtn != null) copyWalletBtn.setVisibility(View.VISIBLE);
            if (reloadBtn != null) reloadBtn.setVisibility(View.VISIBLE);
            if (wipeBtn != null) wipeBtn.setVisibility(View.VISIBLE);
            if (fpBtn != null) fpBtn.setVisibility(View.VISIBLE);
            if (collapseBtn != null) collapseBtn.setVisibility(View.VISIBLE);
        }

        // Update Dual Copy / Paste button text
        if (copyWalletBtn != null) {
            copyWalletBtn.setText(sHasFormFocus ? " PASTE " : " COPY ");
        }

        // Update Network + Wallet Name Badge
        if (netWalletBtn != null) {
            String wName = manager.mCurrentWalletName;
            if (wName == null || wName.isEmpty()) {
                wName = "eso-1";
            }
            netWalletBtn.setText(" " + getShortNetworkName() + " " + wName + " ");
        }

        // Update Fingerprint Badge
        if (fpBtn != null) {
            fpBtn.setText(" FP: " + formatShortFpName(sCurrentFpName) + " ");
        }

        syncToolPillPosition(manager);
    }

    public static void syncToolPillPosition(FusionWebViewManager manager) {
        if (manager == null || manager.mActivity == null) return;
        if (!manager.isWebViewOpen()) {
            hideToolPill(manager);
            return;
        }
        HorizontalScrollView scroll = manager.mToolScroll != null ? manager.mToolScroll : sToolScroll;
        if (scroll == null) return;

        ViewGroup.LayoutParams lp = scroll.getLayoutParams();
        if (lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) lp;
            float density = manager.mActivity.getResources().getDisplayMetrics().density;
            int gap = (int) (4 * density);
            int targetTop = (int) (58 * density);
            if (manager.mControlPill != null && manager.mControlPill.getVisibility() == View.VISIBLE) {
                if (manager.mControlPill.getBottom() > 0) {
                    targetTop = manager.mControlPill.getBottom() + gap;
                } else {
                    manager.mControlPill.post(() -> syncToolPillPosition(manager));
                }
            }
            if (flp.topMargin != targetTop) {
                flp.topMargin = targetTop;
                scroll.setLayoutParams(flp);
            }
        }
    }

    public static void syncTabToolState(FusionWebViewManager manager, FusionTab tab) {
        if (manager == null || tab == null) return;
        if (!manager.isWebViewOpen()) {
            hideToolPill(manager);
            return;
        }
        sHasFormFocus = tab.mHasFormFocus;
        if (manager.mToolPill != null) {
            manager.mToolPill.setVisibility(View.VISIBLE);
        }
        if (sToolPill != null) {
            sToolPill.setVisibility(View.VISIBLE);
        }
        HorizontalScrollView scroll = manager.mToolScroll != null ? manager.mToolScroll : sToolScroll;
        if (scroll != null) {
            scroll.setVisibility(View.VISIBLE);
            scroll.setElevation(160.0f);
            scroll.bringToFront();
        }
        updateToolPillUi(manager);
        syncToolPillPosition(manager);
    }

    public static void showToolPill(FusionWebViewManager manager) {
        if (manager == null) return;
        if (!manager.isWebViewOpen()) {
            hideToolPill(manager);
            return;
        }
        HorizontalScrollView scroll = manager.mToolScroll != null ? manager.mToolScroll : sToolScroll;
        if (scroll == null) {
            initToolPill(manager);
            scroll = manager.mToolScroll != null ? manager.mToolScroll : sToolScroll;
            if (scroll == null) return;
        }
        if (scroll.getParent() == null && manager.mPaneHost != null && manager.mActivity != null) {
            float density = manager.mActivity.getResources().getDisplayMetrics().density;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2, 8388661);
            int targetTop = (int) (58 * density);
            if (manager.mControlPill != null && manager.mControlPill.getBottom() > 0) {
                targetTop = manager.mControlPill.getBottom() + (int) (4 * density);
            }
            lp.setMargins(0, targetTop, 10, 0);
            manager.mPaneHost.addView(scroll, lp);
        }
        if (manager.mToolPill != null) {
            manager.mToolPill.setVisibility(View.VISIBLE);
        }
        if (sToolPill != null) {
            sToolPill.setVisibility(View.VISIBLE);
        }
        scroll.setVisibility(View.VISIBLE);
        scroll.setElevation(160.0f);
        scroll.bringToFront();
        syncToolPillPosition(manager);
        updateToolPillUi(manager);
    }

    public static void hideToolPill(FusionWebViewManager manager) {
        if (manager != null && manager.mToolScroll != null) {
            manager.mToolScroll.setVisibility(View.GONE);
        }
        if (sToolScroll != null) {
            sToolScroll.setVisibility(View.GONE);
        }
    }

    public static void hideToolPill() {
        hideToolPill(null);
    }

    public static void bringToFront(FusionWebViewManager manager) {
        HorizontalScrollView scroll = (manager != null && manager.mToolScroll != null) ? manager.mToolScroll : sToolScroll;
        if (scroll != null && scroll.getVisibility() == View.VISIBLE) {
            scroll.bringToFront();
        }
    }

    public static void bringToFront() {
        bringToFront(null);
    }
}
