package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import com.termux.app.FusionToast;

import com.termux.app.TermuxActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FusionUserscriptHelper {
    private static final String TAG = "FusionUserscript";

    public static File getPrimaryScriptsDir(Context context) {
        String[] candidatePaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/scripts",
            "/data/local/nhsystem/kali-arm64/root/.fusion/scripts",
            "/root/.fusion/scripts",
            "/data/data/com.termux/files/home/wallet/scripts",
            "/root/wallet/scripts"
        };
        for (String p : candidatePaths) {
            File d = new File(p);
            if (d.exists() && d.isDirectory()) {
                return d;
            }
        }
        for (String p : candidatePaths) {
            File d = new File(p);
            try {
                if (d.mkdirs()) {
                    try { d.setReadable(true, false); } catch (Throwable ignored) {}
                    try { d.setWritable(true, false); } catch (Throwable ignored) {}
                    return d;
                }
            } catch (Throwable ignored) {}
        }
        if (context != null) {
            File fallback = new File(context.getFilesDir(), "fusion_scripts");
            if (!fallback.exists()) fallback.mkdirs();
            return fallback;
        }
        return new File("/data/data/com.termux/files/home/.fusion/scripts");
    }

    public static List<File> getInstalledScripts(Context context) {
        List<File> list = new ArrayList<>();
        java.util.Set<String> seenNames = new java.util.HashSet<>();
        String[] candidatePaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/scripts",
            "/data/local/nhsystem/kali-arm64/root/.fusion/scripts",
            "/root/.fusion/scripts",
            "/data/data/com.termux/files/home/wallet/scripts",
            "/root/wallet/scripts"
        };
        for (String p : candidatePaths) {
            File dir = new File(p);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && (f.getName().endsWith(".user.js") || f.getName().endsWith(".js"))) {
                            if (!seenNames.contains(f.getName())) {
                                seenNames.add(f.getName());
                                list.add(f);
                            }
                        }
                    }
                }
            }
        }
        if (list.isEmpty() && context != null) {
            File fallback = new File(context.getFilesDir(), "fusion_scripts");
            if (fallback.exists() && fallback.isDirectory()) {
                File[] files = fallback.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && (f.getName().endsWith(".user.js") || f.getName().endsWith(".js"))) {
                            if (!seenNames.contains(f.getName())) {
                                seenNames.add(f.getName());
                                list.add(f);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    public static String parseScriptName(File f) {
        if (f == null || !f.exists()) return "Unknown Script";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null && count++ < 50) {
                line = line.trim();
                if (line.startsWith("// @name ") || line.startsWith("// @name\t")) {
                    String name = line.substring(line.indexOf("@name") + 5).trim();
                    if (!name.isEmpty()) return name;
                }
            }
        } catch (Throwable ignored) {}
        return f.getName().replace(".user.js", "").replace(".js", "");
    }

    public static List<String> parseScriptMatches(File f) {
        List<String> matches = new ArrayList<>();
        if (f == null || !f.exists()) return matches;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null && count++ < 60) {
                line = line.trim();
                if (line.startsWith("// @match ") || line.startsWith("// @match\t")) {
                    String m = line.substring(line.indexOf("@match") + 6).trim();
                    if (!m.isEmpty()) matches.add(m);
                } else if (line.startsWith("// @include ") || line.startsWith("// @include\t")) {
                    String inc = line.substring(line.indexOf("@include") + 8).trim();
                    if (!inc.isEmpty()) matches.add(inc);
                }
            }
        } catch (Throwable ignored) {}
        return matches;
    }

    public static boolean isUrlMatched(String currentUrl, List<String> patterns) {
        if (currentUrl == null || currentUrl.isEmpty()) return false;
        if (patterns == null || patterns.isEmpty()) return true;

        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) continue;
            if (pattern.equals("<all_urls>") || pattern.equals("*://*/*") || pattern.equals("*")) {
                return true;
            }
            try {
                String regex = pattern
                        .replace("\\", "\\\\")
                        .replace(".", "\\.")
                        .replace("*://", "https?://")
                        .replace("http://*", "http://.*")
                        .replace("https://*", "https://.*")
                        .replace("*", ".*");
                if (currentUrl.matches(regex)) {
                    return true;
                }
            } catch (Throwable ignored) {
                if (currentUrl.contains(pattern)) return true;
            }
        }
        return false;
    }

    public static void injectMatchingScripts(WebView webView, String currentUrl) {
        if (webView == null || currentUrl == null || currentUrl.isEmpty()) return;
        if (currentUrl.startsWith("file:///android_asset/") || currentUrl.startsWith("about:") || currentUrl.startsWith("javascript:")) {
            return;
        }

        Context ctx = webView.getContext();
        List<File> scripts = getInstalledScripts(ctx);
        if (scripts.isEmpty()) return;

        for (File f : scripts) {
            try {
                List<String> matches = parseScriptMatches(f);
                if (isUrlMatched(currentUrl, matches)) {
                    byte[] bytes = new byte[(int) f.length()];
                    try (FileInputStream fis = new FileInputStream(f)) {
                        fis.read(bytes);
                    }
                    String scriptCode = new String(bytes, StandardCharsets.UTF_8);
                    String wrapped = "(function() { try {\n" + scriptCode + "\n} catch(err) { console.error('[Userscript Error " + f.getName() + "]:', err); } })();";
                    webView.evaluateJavascript(wrapped, null);
                    Log.i(TAG, "Injected userscript [" + f.getName() + "] into " + currentUrl);
                }
            } catch (Throwable e) {
                Log.e(TAG, "Gagal injeksi userscript " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    public static boolean promptInstallUserscript(final TermuxActivity act, final WebView webView, final String scriptUrl) {
        if (webView == null || scriptUrl == null || !scriptUrl.contains(".user.js")) return false;
        final Context context = (act != null) ? act : webView.getContext();
        if (context == null) return false;

        new Thread(() -> {
            try {
                URL u = new URL(scriptUrl);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36");

                if (conn.getResponseCode() != 200) return;

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder fullScript = new StringBuilder();
                String line;
                String scriptName = null;
                String scriptVersion = "1.0";
                String scriptAuthor = "Unknown";
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    fullScript.append(line).append("\n");
                    if (count++ < 60) {
                        String t = line.trim();
                        if (t.startsWith("// @name ") || t.startsWith("// @name\t")) {
                            scriptName = t.substring(t.indexOf("@name") + 5).trim();
                        } else if (t.startsWith("// @version ") || t.startsWith("// @version\t")) {
                            scriptVersion = t.substring(t.indexOf("@version") + 8).trim();
                        } else if (t.startsWith("// @author ") || t.startsWith("// @author\t")) {
                            scriptAuthor = t.substring(t.indexOf("@author") + 7).trim();
                        }
                    }
                }
                reader.close();
                conn.disconnect();

                final String finalName = (scriptName != null && !scriptName.isEmpty()) ? scriptName : "Userscript_" + (System.currentTimeMillis() % 10000);
                final String finalVer = scriptVersion;
                final String finalAuthor = scriptAuthor;
                final String scriptContent = fullScript.toString();

                Runnable showDialogRunnable = () -> {
                    Context dialogCtx = (act != null) ? act : context;
                    AlertDialog.Builder b = new AlertDialog.Builder(dialogCtx);
                    b.setTitle("🧩 Pasang Userscript Ekstensi");
                    b.setMessage("Ditemukan Userscript:\n\n" +
                            "• Nama   : " + finalName + "\n" +
                            "• Versi  : " + finalVer + "\n" +
                            "• Author : " + finalAuthor + "\n\n" +
                            "Pasang skrip ini ke dalam Fusion WebView?");
                    b.setPositiveButton("Pasang", (dialog, which) -> {
                        try {
                            File targetDir = getPrimaryScriptsDir(dialogCtx);
                            String safeFileName = finalName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".user.js";
                            File targetFile = new File(targetDir, safeFileName);
                            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                                fos.write(scriptContent.getBytes(StandardCharsets.UTF_8));
                            }
                            try { targetFile.setReadable(true, false); } catch (Throwable ignored) {}
                            try { targetFile.setWritable(true, false); } catch (Throwable ignored) {}

                            FusionToast.makeText(dialogCtx, "✅ Userscript Terpasang: " + finalName, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Userscript berhasil disimpan ke " + targetFile.getAbsolutePath());
                        } catch (Throwable err) {
                            FusionToast.makeText(dialogCtx, "❌ Gagal pasang script: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    b.setNegativeButton("Batal", null);
                    b.show();
                };

                if (act != null) {
                    act.runOnUiThread(showDialogRunnable);
                } else {
                    webView.post(showDialogRunnable);
                }

            } catch (Throwable e) {
                Log.e(TAG, "Gagal mengunduh userscript dari " + scriptUrl + ": " + e.getMessage());
            }
        }).start();

        return true;
    }

    public static void showUserscriptsDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null) return;
        List<File> scripts = getInstalledScripts(act);
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("🧩 Userscript Ekstensi");

        StringBuilder sb = new StringBuilder();
        sb.append("Engine Userscript Ekstensi (Tampermonkey Style):\n\n");
        sb.append("📁 Folder: ~/.fusion/scripts/\n");
        sb.append("📊 Terpasang: ").append(scripts.size()).append(" skrip aktif\n\n");

        if (scripts.isEmpty()) {
            sb.append("• Belum ada skrip .user.js yang terpasang.\n");
            sb.append("• Buka GreasyFork untuk mencari dan memasang ribuan skrip add-on (Adblock, Web3, Dark Mode, Auto-Bot, Bypass, dsb.).\n");
            sb.append("• Saat Tuan klik link .user.js di web, skrip akan otomatis terpasang.");
        } else {
            sb.append("Daftar Skrip Terpasang:\n");
            for (int i = 0; i < scripts.size(); i++) {
                File f = scripts.get(i);
                String name = parseScriptName(f);
                sb.append(" [").append(i + 1).append("] ").append(name).append("\n");
            }
        }
        b.setMessage(sb.toString());

        b.setPositiveButton("Buka GreasyFork", (dialog, which) -> {
            if (manager != null) {
                manager.closeAllDrawers();
                WebView wv = manager.mActiveWebView;
                if (wv != null) {
                    wv.loadUrl("https://greasyfork.org/id/scripts");
                }
            }
        });

        if (!scripts.isEmpty()) {
            b.setNeutralButton("Kelola Skrip", (dialog, which) -> {
                showManageScriptsDialog(act, manager);
            });
        }

        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public static void showManageScriptsDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null) return;
        final List<File> scripts = getInstalledScripts(act);
        if (scripts.isEmpty()) {
            FusionToast.makeText(act, "Tidak ada skrip yang terpasang.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[scripts.size()];
        for (int i = 0; i < scripts.size(); i++) {
            items[i] = (i + 1) + ". " + parseScriptName(scripts.get(i));
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("🗑️ Hapus Userscript");
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < scripts.size()) {
                final File target = scripts.get(which);
                final String name = parseScriptName(target);

                AlertDialog.Builder confirm = new AlertDialog.Builder(act);
                confirm.setTitle("Hapus Skrip?");
                confirm.setMessage("Yakin ingin menghapus userscript \"" + name + "\"?");
                confirm.setPositiveButton("Hapus", (d, w) -> {
                    if (target.delete()) {
                        FusionToast.makeText(act, "Skrip \"" + name + "\" berhasil dihapus.", Toast.LENGTH_SHORT).show();
                    } else {
                        FusionToast.makeText(act, "Gagal menghapus berkas skrip.", Toast.LENGTH_SHORT).show();
                    }
                });
                confirm.setNegativeButton("Batal", null);
                confirm.show();
            }
        });
        b.setNegativeButton("Kembali", (dialog, which) -> {
            showUserscriptsDialog(act, manager);
        });
        b.show();
    }
}
