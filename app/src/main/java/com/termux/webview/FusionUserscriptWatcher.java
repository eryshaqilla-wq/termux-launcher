package com.termux.webview;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import com.termux.app.FusionToast;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watches userscript directories for changes and automatically hot-reloads
 * modified scripts into the active WebView without page refresh.
 */
public class FusionUserscriptWatcher {
    private static final String TAG = "FusionScriptWatcher";

    private static final List<FileObserver> sObservers = new ArrayList<>();
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private static final ConcurrentHashMap<String, Runnable> sPendingDebounce = new ConcurrentHashMap<>();
    private static boolean sIsWatching = false;

    public static synchronized void startWatching(final Context context) {
        if (sIsWatching) return;
        sIsWatching = true;

        String[] candidatePaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/scripts",
            "/data/local/nhsystem/kali-arm64/root/.fusion/scripts",
            "/root/.fusion/scripts",
            "/data/data/com.termux/files/home/wallet/scripts",
            "/root/wallet/scripts"
        };

        for (final String path : candidatePaths) {
            final File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) continue;

            try {
                int mask = FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO | FileObserver.CREATE;
                FileObserver obs = new FileObserver(path, mask) {
                    @Override
                    public void onEvent(int event, final String pathName) {
                        if (pathName == null) return;
                        if (!pathName.endsWith(".user.js") && !pathName.endsWith(".js")) return;

                        final File scriptFile = new File(dir, pathName);
                        if (!scriptFile.exists() || !scriptFile.isFile()) return;

                        // Debounce by 250ms
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                hotReloadScript(scriptFile);
                            }
                        };

                        Runnable existing = sPendingDebounce.put(pathName, task);
                        if (existing != null) {
                            sHandler.removeCallbacks(existing);
                        }
                        sHandler.postDelayed(task, 250);
                    }
                };
                obs.startWatching();
                sObservers.add(obs);
                Log.i(TAG, "Watching for userscript changes: " + path);
            } catch (Throwable t) {
                Log.w(TAG, "Cannot watch path: " + path + ": " + t.getMessage());
            }
        }
    }

    public static synchronized void stopWatching() {
        for (FileObserver obs : sObservers) {
            try { obs.stopWatching(); } catch (Throwable ignored) {}
        }
        sObservers.clear();
        sPendingDebounce.clear();
        sIsWatching = false;
    }

    public static void hotReloadScript(final File scriptFile) {
        if (scriptFile == null || !scriptFile.exists()) return;
        try {
            byte[] bytes = new byte[(int) scriptFile.length()];
            try (FileInputStream fis = new FileInputStream(scriptFile)) {
                fis.read(bytes);
            }
            final String code = new String(bytes, StandardCharsets.UTF_8);
            final String name = FusionUserscriptHelper.parseScriptName(scriptFile);
            final String polyfill = FusionUserscriptHelper.getUserscriptPolyfill(name, "hot-reload");

            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                    if (mgr == null) return;
                    FusionTab activeTab = FusionTabStateHelper.getActiveTab(mgr);
                    if (mgr.mActiveWebView == null && (activeTab == null || !activeTab.isGecko())) return;

                    String wrapped = "(function() { try {\n" +
                            polyfill + "\n" +
                            code + "\n" +
                            "console.log('[Fusion Hot-Reload] Re-injected: " + scriptFile.getName() + "');\n" +
                            "} catch(err) { console.error('[Fusion Hot-Reload Error " + scriptFile.getName() + "]:', err); } })();";

                    mgr.evalOnActiveTab(wrapped);

                    if (mgr.mActivity != null) {
                        FusionToast.show(mgr.mActivity, "⚡ Hot-Reloaded: " + scriptFile.getName());
                    }
                    Log.i(TAG, "Hot-reloaded script: " + scriptFile.getName());
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Error hot-reloading script: " + scriptFile.getName(), t);
        }
    }
}
