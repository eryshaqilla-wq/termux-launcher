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

    public static void initAndShowToolPill(final FusionWebViewManager manager) {
        if (manager == null || manager.mActivity == null || manager.mPaneHost == null) {
            return;
        }
        final AppCompatActivity act = manager.mActivity;
        FrameLayout host = manager.mPaneHost;

        // Reset default ke menciut setiap inisialisasi
        sIsCollapsed = true;

        if (sToolScroll != null) {
            ViewParent parent = sToolScroll.getParent();
            if (parent != host) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(sToolScroll);
                }
                float density = act.getResources().getDisplayMetrics().density;
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2, 8388661);
                lp.setMargins(0, (int) (44 * density), 10, 0);
                host.addView(sToolScroll, lp);
            }
            sToolScroll.setVisibility(View.VISIBLE);
            sToolScroll.bringToFront();
            syncToolPillPosition(manager);
            updateToolPillUi(manager);
            return;
        }

        float density = act.getResources().getDisplayMetrics().density;

        // Container scroll horizontal responsif
        HorizontalScrollView scroll = new HorizontalScrollView(act);
        sToolScroll = scroll;
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setElevation(160.0f);

        // Box styling 100% identik dengan mControlPill
        LinearLayout pill = new LinearLayout(act);
        sToolPill = pill;
        pill.setLayerType(1, null);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        FusionUiHelper.setSolidBox(pill);
        pill.setPadding(6, 4, 6, 4);
        scroll.addView(pill, new ViewGroup.LayoutParams(-2, -2));

        // 0. Tombol saat menciut: [ TOOL ] (tanpa icon, style identik dengan bar atas)
        TextView toggleBtn = new TextView(act);
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
        sWalletNextBtn = walletNextBtn;
        walletNextBtn.setText(" WALLET > ");
        FusionUiHelper.setSolidBtn(walletNextBtn);
        walletNextBtn.setTextSize(2, 11.0f);
        walletNextBtn.setPadding(8, 4, 8, 4);
        walletNextBtn.setClickable(true);
        walletNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.switchToNextWallet();
                updateToolPillUi(manager);
            }
        });
        LinearLayout.LayoutParams wnLp = new LinearLayout.LayoutParams(-2, -2);
        wnLp.setMargins(2, 0, 2, 0);
        pill.addView(walletNextBtn, wnLp);

        // 2. Network + No Wallet: [ Base eso-1 ] -> Sekali klik memunculkan drawer
        TextView netWalletBtn = new TextView(act);
        sNetWalletBtn = netWalletBtn;
        netWalletBtn.setText(" Base eso-1 ");
        FusionUiHelper.setSolidBtn(netWalletBtn);
        netWalletBtn.setTextSize(2, 11.0f);
        netWalletBtn.setPadding(8, 4, 8, 4);
        netWalletBtn.setClickable(true);
        netWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusionDrawerHelper.showWalletDrawer(manager.mActivity, manager);
            }
        });
        LinearLayout.LayoutParams nwLp = new LinearLayout.LayoutParams(-2, -2);
        nwLp.setMargins(2, 0, 2, 0);
        pill.addView(netWalletBtn, nwLp);

        // 3. Copy Wallet: [ COPY WALLET ]
        TextView copyWalletBtn = new TextView(act);
        sCopyWalletBtn = copyWalletBtn;
        copyWalletBtn.setText(" COPY WALLET ");
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
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("wallet_address", addr));
                    }
                } catch (Throwable ignored) {}

                WebView wv = manager.mActiveWebView;
                if (wv != null && addr != null) {
                    String js = "(function() {" +
                        "  var val = '" + addr + "';" +
                        "  if (document.activeElement && (document.activeElement.tagName === 'INPUT' || document.activeElement.tagName === 'TEXTAREA')) {" +
                        "    document.activeElement.value = val;" +
                        "    document.activeElement.dispatchEvent(new Event('input', {bubbles: true}));" +
                        "    document.activeElement.dispatchEvent(new Event('change', {bubbles: true}));" +
                        "  }" +
                        "})();";
                    wv.evaluateJavascript(js, null);
                }

                String shortAddr = (addr != null && addr.length() > 14) ? (addr.substring(0, 6) + "..." + addr.substring(addr.length() - 4)) : addr;
                FusionToast.makeText((Context) act, (CharSequence) ("Alamat (" + getShortNetworkName() + ") disalin: " + shortAddr), 0).show();
            }
        });
        LinearLayout.LayoutParams cwLp = new LinearLayout.LayoutParams(-2, -2);
        cwLp.setMargins(2, 0, 2, 0);
        pill.addView(copyWalletBtn, cwLp);

        // 4. Reload: [ RELOAD ]
        TextView reloadBtn = new TextView(act);
        sReloadBtn = reloadBtn;
        reloadBtn.setText(" RELOAD ");
        FusionUiHelper.setSolidBtn(reloadBtn);
        reloadBtn.setTextSize(2, 11.0f);
        reloadBtn.setPadding(8, 4, 8, 4);
        reloadBtn.setClickable(true);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.reloadActiveTab();
                FusionToast.makeText((Context) act, (CharSequence) "Reloading...", 0).show();
            }
        });
        LinearLayout.LayoutParams relLp = new LinearLayout.LayoutParams(-2, -2);
        relLp.setMargins(2, 0, 2, 0);
        pill.addView(reloadBtn, relLp);

        // 5. Wipe: [ WIPE ]
        TextView wipeBtn = new TextView(act);
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
                    wv.evaluateJavascript("try { localStorage.clear(); sessionStorage.clear(); } catch(e){}", null);
                }
                FusionProfileManager.clearAllCookiesAndStorage();
                FusionToast.makeText((Context) act, (CharSequence) "Cache & Storage Dibersihkan!", 0).show();
            }
        });
        LinearLayout.LayoutParams wpLp = new LinearLayout.LayoutParams(-2, -2);
        wpLp.setMargins(2, 0, 2, 0);
        pill.addView(wipeBtn, wpLp);

        // 6. FP Info & Rotate: [ FP: S24 Ultra ]
        TextView fpBtn = new TextView(act);
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
            }
        });
        LinearLayout.LayoutParams fpLp = new LinearLayout.LayoutParams(-2, -2);
        fpLp.setMargins(2, 0, 2, 0);
        pill.addView(fpBtn, fpLp);

        // 7. Collapse: [ > ]
        TextView collapseBtn = new TextView(act);
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
        int topMargin = (int) (44 * density);
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

    public static void toggleCollapse(FusionWebViewManager manager) {
        sIsCollapsed = !sIsCollapsed;
        updateToolPillUi(manager);
    }

    public static void collapse(FusionWebViewManager manager) {
        sIsCollapsed = true;
        updateToolPillUi(manager);
    }

    public static void updateToolPillUi(FusionWebViewManager manager) {
        if (sToolScroll == null || manager == null) {
            return;
        }

        if (sIsCollapsed) {
            if (sToggleBtn != null) sToggleBtn.setVisibility(View.VISIBLE);
            if (sWalletNextBtn != null) sWalletNextBtn.setVisibility(View.GONE);
            if (sNetWalletBtn != null) sNetWalletBtn.setVisibility(View.GONE);
            if (sCopyWalletBtn != null) sCopyWalletBtn.setVisibility(View.GONE);
            if (sReloadBtn != null) sReloadBtn.setVisibility(View.GONE);
            if (sWipeBtn != null) sWipeBtn.setVisibility(View.GONE);
            if (sFpBtn != null) sFpBtn.setVisibility(View.GONE);
            if (sCollapseBtn != null) sCollapseBtn.setVisibility(View.GONE);
        } else {
            if (sToggleBtn != null) sToggleBtn.setVisibility(View.GONE);
            if (sWalletNextBtn != null) sWalletNextBtn.setVisibility(View.VISIBLE);
            if (sNetWalletBtn != null) sNetWalletBtn.setVisibility(View.VISIBLE);
            if (sCopyWalletBtn != null) sCopyWalletBtn.setVisibility(View.VISIBLE);
            if (sReloadBtn != null) sReloadBtn.setVisibility(View.VISIBLE);
            if (sWipeBtn != null) sWipeBtn.setVisibility(View.VISIBLE);
            if (sFpBtn != null) sFpBtn.setVisibility(View.VISIBLE);
            if (sCollapseBtn != null) sCollapseBtn.setVisibility(View.VISIBLE);
        }

        // Update Network + Wallet Name Badge
        if (sNetWalletBtn != null) {
            String wName = manager.mCurrentWalletName;
            if (wName == null || wName.isEmpty()) {
                wName = "eso-1";
            }
            sNetWalletBtn.setText(" " + getShortNetworkName() + " " + wName + " ");
        }

        // Update Fingerprint Badge
        if (sFpBtn != null) {
            sFpBtn.setText(" FP: " + formatShortFpName(sCurrentFpName) + " ");
        }

        syncToolPillPosition(manager);
    }

    public static void syncToolPillPosition(FusionWebViewManager manager) {
        if (sToolScroll == null || manager == null || manager.mActivity == null) {
            return;
        }
        ViewGroup.LayoutParams lp = sToolScroll.getLayoutParams();
        if (lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) lp;
            float density = manager.mActivity.getResources().getDisplayMetrics().density;
            int gap = (int) (4 * density);
            int targetTop = (int) (44 * density);
            if (manager.mControlPill != null && manager.mControlPill.getBottom() > 0) {
                targetTop = manager.mControlPill.getBottom() + gap;
            }
            if (flp.topMargin != targetTop) {
                flp.topMargin = targetTop;
                sToolScroll.setLayoutParams(flp);
            }
        }
    }

    public static void showToolPill(FusionWebViewManager manager) {
        if (manager == null) return;
        WebView webView = manager.mActiveWebView;
        if (webView == null || webView.getVisibility() != View.VISIBLE) {
            hideToolPill();
            return;
        }
        FusionTab activeTab = FusionTabStateHelper.getActiveTab(manager);
        if (activeTab == null || activeTab.mIsMinimized) {
            hideToolPill();
            return;
        }
        if (sToolScroll == null) {
            initAndShowToolPill(manager);
            return;
        }
        sToolScroll.setVisibility(View.VISIBLE);
        sToolScroll.bringToFront();
        syncToolPillPosition(manager);
        updateToolPillUi(manager);
    }

    public static void hideToolPill() {
        if (sToolScroll != null) {
            sToolScroll.setVisibility(View.GONE);
        }
    }

    public static void bringToFront() {
        if (sToolScroll != null && sToolScroll.getVisibility() == View.VISIBLE) {
            sToolScroll.bringToFront();
        }
    }
}
