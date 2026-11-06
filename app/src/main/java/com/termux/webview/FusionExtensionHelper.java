package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.termux.app.FusionToast;
import com.termux.app.TermuxActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * FusionExtensionHelper
 * Kiwi-Style Chrome Extension Engine untuk Fusion WebView.
 * Mendukung instalasi paket ekstensi .zip & .crx (Manifest V2 & V3),
 * injeksi level document_start via WebViewCompat, polyfill API chrome.* (storage, runtime, tabs),
 * iframe multi-frame routing, setelan layer konfigurasi interaktif, serta penghapusan & pembacaan
 * berkas failsafe yang tahan terhadap pembatasan permission Android Scoped Storage / root chroot.
 */
public class FusionExtensionHelper {
    private static final String TAG = "FusionExtension";
    public static final int REQUEST_CODE_PICK_EXTENSION = 4725;
    private static final String PREF_NAME = "fusion_extension_prefs";
    private static final String KEY_STORAGE_CACHE = "ext_storage_json";
    private static String sStorageCache = null;

    public static class ExtensionInfo {
        public String id;
        public File dir;
        public String name;
        public String version;
        public String description;
        public boolean enabled = true;
        public int manifestVersion = 3;
        public String popupHtml;
        public String iconPath;
        public List<ContentScript> contentScripts = new ArrayList<>();

        public static class ContentScript {
            public List<String> matches = new ArrayList<>();
            public List<String> jsFiles = new ArrayList<>();
            public String runAt = "document_idle";
            public boolean allFrames = false;
            public String world = "ISOLATED";
        }
    }

    public static class FusionExtensionBridge {
        private final Context mContext;

        public FusionExtensionBridge(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public String getStorage() {
            return FusionExtensionHelper.getStorageJson(mContext);
        }

        @JavascriptInterface
        public void setStorage(String jsonStr) {
            FusionExtensionHelper.saveStorageJson(mContext, jsonStr);
        }

        @JavascriptInterface
        public String getLayer() {
            return FusionExtensionHelper.getLayerConfig(mContext);
        }

        @JavascriptInterface
        public void setLayer(String layer) {
            FusionExtensionHelper.setLayerConfig(mContext, layer);
        }

        @JavascriptInterface
        public void log(String msg) {
            Log.i(TAG, "[Extension JS Log]: " + msg);
        }
    }

    public static File getPrimaryExtensionsDir(Context context) {
        String[] candidatePaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/extensions",
            "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
            "/root/.fusion/extensions",
            "/data/data/com.termux/files/home/wallet/extensions",
            "/root/wallet/extensions"
        };
        for (String p : candidatePaths) {
            File d = new File(p);
            if (!d.exists()) {
                try { d.mkdirs(); } catch (Throwable ignored) {}
            }
            if (d.exists() && d.isDirectory()) {
                try {
                    d.setReadable(true, false);
                    d.setWritable(true, false);
                    d.setExecutable(true, false);
                } catch (Throwable ignored) {}
                if (d.canWrite()) {
                    return d;
                }
            }
        }
        if (context != null) {
            File fallback = new File(context.getFilesDir(), "fusion_extensions");
            if (!fallback.exists()) fallback.mkdirs();
            try {
                fallback.setReadable(true, false);
                fallback.setWritable(true, false);
                fallback.setExecutable(true, false);
            } catch (Throwable ignored) {}
            return fallback;
        }
        return new File("/data/data/com.termux/files/home/.fusion/extensions");
    }

    public static File getStorageFile(Context context) {
        File dir = getPrimaryExtensionsDir(context);
        return new File(dir, "storage.json");
    }

    public static synchronized String getStorageJson(Context context) {
        if (sStorageCache != null && !sStorageCache.isEmpty()) {
            return sStorageCache;
        }
        File f = getStorageFile(context);
        if (f.exists() && f.isFile()) {
            try {
                byte[] bytes = readFileBytesSafe(f);
                if (bytes != null && bytes.length > 0) {
                    sStorageCache = new String(bytes, StandardCharsets.UTF_8).trim();
                    if (!sStorageCache.isEmpty()) return sStorageCache;
                }
            } catch (Throwable ignored) {}
        }
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String saved = sp.getString(KEY_STORAGE_CACHE, null);
            if (saved != null && !saved.isEmpty()) {
                sStorageCache = saved;
                return sStorageCache;
            }
        }
        // Default awal: layer 12m aktif untuk Google AI Pro 2Y Offer
        sStorageCache = "{\"gapt_layer\":\"12m\"}";
        saveStorageJson(context, sStorageCache);
        return sStorageCache;
    }

    public static synchronized void saveStorageJson(Context context, String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            jsonStr = "{}";
        }
        sStorageCache = jsonStr;
        File f = getStorageFile(context);
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            }
            try { f.setReadable(true, false); } catch (Throwable ignored) {}
            try { f.setWritable(true, false); } catch (Throwable ignored) {}
        } catch (Throwable e) {
            Log.w(TAG, "Standard save storage.json failed: " + e.getMessage() + ", trying shell write...");
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo '" + jsonStr.replace("'", "'\\''") + "' > '" + f.getAbsolutePath() + "' && chmod 777 '" + f.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }
        if (context != null) {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_STORAGE_CACHE, jsonStr).apply();
            } catch (Throwable ignored) {}
        }
    }

    public static String getLayerConfig(Context context) {
        try {
            String raw = getStorageJson(context);
            JSONObject obj = new JSONObject(raw);
            return obj.optString("gapt_layer", "12m");
        } catch (Throwable ignored) {}
        return "12m";
    }

    public static void setLayerConfig(Context context, String layer) {
        if (layer == null || layer.isEmpty()) layer = "12m";
        try {
            String raw = getStorageJson(context);
            JSONObject obj = new JSONObject(raw);
            obj.put("gapt_layer", layer);
            saveStorageJson(context, obj.toString());
        } catch (Throwable e) {
            saveStorageJson(context, "{\"gapt_layer\":\"" + layer + "\"}");
        }
    }

    public static File getStateFile(Context context) {
        File dir = getPrimaryExtensionsDir(context);
        return new File(dir, "extensions_state.json");
    }

    public static Map<String, Boolean> loadExtensionsState(Context context) {
        Map<String, Boolean> stateMap = new HashMap<>();
        File f = getStateFile(context);
        if (f.exists() && f.isFile()) {
            try {
                byte[] bytes = readFileBytesSafe(f);
                if (bytes != null && bytes.length > 0) {
                    JSONObject obj = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
                    java.util.Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        stateMap.put(k, obj.optBoolean(k, true));
                    }
                }
            } catch (Throwable ignored) {}
        }
        return stateMap;
    }

    public static void saveExtensionsState(Context context, Map<String, Boolean> stateMap) {
        File f = getStateFile(context);
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, Boolean> entry : stateMap.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
            String content = obj.toString(2);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            try { f.setReadable(true, false); } catch (Throwable ignored) {}
            try { f.setWritable(true, false); } catch (Throwable ignored) {}
        } catch (Throwable e) {
            Log.w(TAG, "Standard save extensions_state.json failed: " + e.getMessage());
            try {
                JSONObject obj = new JSONObject();
                for (Map.Entry<String, Boolean> entry : stateMap.entrySet()) {
                    obj.put(entry.getKey(), entry.getValue());
                }
                String content = obj.toString(2);
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo '" + content.replace("'", "'\\''") + "' > '" + f.getAbsolutePath() + "' && chmod 777 '" + f.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }
    }

    public static List<ExtensionInfo> getInstalledExtensions(Context context) {
        List<ExtensionInfo> list = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        String[] scanPaths = new String[] {
            "/data/data/com.termux/files/home/.fusion/extensions",
            "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
            "/root/.fusion/extensions"
        };
        if (context != null) {
            File localDir = new File(context.getFilesDir(), "fusion_extensions");
            if (localDir.exists()) {
                scanCandidateExtensionsInDir(context, localDir, list, seenIds);
            }
        }

        for (String p : scanPaths) {
            File d = new File(p);
            if (d.exists() && d.isDirectory()) {
                scanCandidateExtensionsInDir(context, d, list, seenIds);
            }
        }

        return list;
    }

    private static void scanCandidateExtensionsInDir(Context context, File extDir, List<ExtensionInfo> list, Set<String> seenIds) {
        Map<String, Boolean> stateMap = loadExtensionsState(context);
        File[] subdirs = extDir.listFiles();
        if (subdirs == null) return;

        for (File d : subdirs) {
            if (d.isDirectory() && !d.getName().startsWith(".")) {
                File manifest = new File(d, "manifest.json");
                if (manifest.exists() && manifest.isFile()) {
                    if (!seenIds.contains(d.getName())) {
                        ExtensionInfo info = parseExtensionDir(d);
                        if (info != null) {
                            info.enabled = stateMap.containsKey(info.id) ? Boolean.TRUE.equals(stateMap.get(info.id)) : true;
                            list.add(info);
                            seenIds.add(info.id);
                        }
                    }
                }
            }
        }
    }

    public static ExtensionInfo parseExtensionDir(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return null;
        File manifestFile = new File(dir, "manifest.json");
        if (!manifestFile.exists() || !manifestFile.isFile()) return null;

        try {
            byte[] bytes = readFileBytesSafe(manifestFile);
            if (bytes == null || bytes.length == 0) return null;

            String jsonStr = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);

            ExtensionInfo info = new ExtensionInfo();
            info.id = dir.getName();
            info.dir = dir;
            info.name = root.optString("name", dir.getName());
            info.version = root.optString("version", "1.0.0");
            info.description = root.optString("description", "");
            info.manifestVersion = root.optInt("manifest_version", 3);

            if (root.has("action")) {
                JSONObject action = root.optJSONObject("action");
                if (action != null) {
                    info.popupHtml = action.optString("default_popup", null);
                    Object defIcon = action.opt("default_icon");
                    if (defIcon instanceof String) {
                        info.iconPath = (String) defIcon;
                    } else if (defIcon instanceof JSONObject) {
                        JSONObject iconObj = (JSONObject) defIcon;
                        info.iconPath = iconObj.optString("128", iconObj.optString("48", iconObj.optString("32", iconObj.optString("16", null))));
                    }
                }
            } else if (root.has("browser_action")) {
                JSONObject ba = root.optJSONObject("browser_action");
                if (ba != null) {
                    info.popupHtml = ba.optString("default_popup", null);
                    Object defIcon = ba.opt("default_icon");
                    if (defIcon instanceof String) {
                        info.iconPath = (String) defIcon;
                    } else if (defIcon instanceof JSONObject) {
                        JSONObject iconObj = (JSONObject) defIcon;
                        info.iconPath = iconObj.optString("128", iconObj.optString("48", iconObj.optString("32", iconObj.optString("16", null))));
                    }
                }
            }

            if (info.iconPath == null && root.has("icons")) {
                JSONObject icons = root.optJSONObject("icons");
                if (icons != null) {
                    info.iconPath = icons.optString("128", icons.optString("48", icons.optString("32", icons.optString("16", null))));
                }
            }

            JSONArray csArray = root.optJSONArray("content_scripts");
            if (csArray != null) {
                for (int i = 0; i < csArray.length(); i++) {
                    JSONObject csObj = csArray.optJSONObject(i);
                    if (csObj == null) continue;
                    ExtensionInfo.ContentScript cs = new ExtensionInfo.ContentScript();
                    JSONArray matches = csObj.optJSONArray("matches");
                    if (matches != null) {
                        for (int j = 0; j < matches.length(); j++) {
                            String m = matches.optString(j, null);
                            if (m != null && !m.isEmpty()) cs.matches.add(m);
                        }
                    }
                    JSONArray jsArr = csObj.optJSONArray("js");
                    if (jsArr != null) {
                        for (int j = 0; j < jsArr.length(); j++) {
                            String jsPath = jsArr.optString(j, null);
                            if (jsPath != null && !jsPath.isEmpty()) cs.jsFiles.add(jsPath);
                        }
                    }
                    cs.runAt = csObj.optString("run_at", "document_idle");
                    cs.allFrames = csObj.optBoolean("all_frames", false);
                    cs.world = csObj.optString("world", "ISOLATED");
                    info.contentScripts.add(cs);
                }
            }
            return info;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal mem-parsing manifest di " + dir.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }
    }

    public static Bitmap getExtensionIcon(ExtensionInfo ext, int targetSizePx) {
        if (ext == null || ext.dir == null) return null;
        File iconFile = null;
        if (ext.iconPath != null && !ext.iconPath.isEmpty()) {
            File f = new File(ext.dir, ext.iconPath);
            if (f.exists() && f.isFile()) {
                iconFile = f;
            }
        }
        if (iconFile == null) {
            String[] commonNames = new String[]{"icon.png", "icon128.png", "icon48.png", "icon-128.png", "icon-48.png", "logo.png", "icon.webp"};
            for (String cn : commonNames) {
                File f = new File(ext.dir, cn);
                if (f.exists() && f.isFile()) {
                    iconFile = f;
                    break;
                }
                File sub = new File(new File(ext.dir, "images"), cn);
                if (sub.exists() && sub.isFile()) {
                    iconFile = sub;
                    break;
                }
                File sub2 = new File(new File(ext.dir, "icons"), cn);
                if (sub2.exists() && sub2.isFile()) {
                    iconFile = sub2;
                    break;
                }
            }
        }
        if (iconFile != null) {
            try {
                byte[] bytes = readFileBytesSafe(iconFile);
                if (bytes != null && bytes.length > 0) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                    opts.inSampleSize = 1;
                    if (targetSizePx > 0 && opts.outWidth > targetSizePx) {
                        opts.inSampleSize = Math.max(1, opts.outWidth / targetSizePx);
                    }
                    opts.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                    if (bmp != null) return bmp;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static byte[] readFileBytesSafe(File file) {
        if (file == null) return null;

        // 1. Baca standar via Java
        try {
            if (file.exists() && file.isFile()) {
                byte[] b = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    int total = 0;
                    while (total < b.length) {
                        int r = fis.read(b, total, b.length - total);
                        if (r == -1) break;
                        total += r;
                    }
                    if (total > 0) return b;
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Standard Java read failed for " + file.getAbsolutePath() + ": " + e.getMessage());
        }

        // 2. Baca via su cat untuk bypass Android Scoped Storage / FUSE permission
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat '" + file.getAbsolutePath() + "'"});
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = p.getInputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            p.waitFor();
            byte[] res = baos.toByteArray();
            if (res.length > 0) {
                Log.i(TAG, "Berhasil membaca " + res.length + " bytes via su cat fallback.");
                return res;
            }
        } catch (Throwable ignored) {}

        // 3. Baca via cat biasa
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"cat", file.getAbsolutePath()});
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = p.getInputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            p.waitFor();
            byte[] res = baos.toByteArray();
            if (res.length > 0) return res;
        } catch (Throwable ignored) {}

        // 4. Fallback jika nama file pro.zip, cari di lokasi alternatif yang bisa dibaca
        if (file.getName().toLowerCase().contains("pro.zip")) {
            String[] altPaths = new String[]{
                "/data/data/com.termux/files/home/pro.zip",
                "/data/data/com.termux/files/home/.fusion/pro.zip",
                "/root/pro.zip",
                "/project/pro.zip"
            };
            for (String alt : altPaths) {
                File altFile = new File(alt);
                if (altFile.exists() && altFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(altFile)) {
                        byte[] b = new byte[(int) altFile.length()];
                        int total = 0;
                        while (total < b.length) {
                            int r = fis.read(b, total, b.length - total);
                            if (r == -1) break;
                            total += r;
                        }
                        if (total > 0) return b;
                    } catch (Throwable ignored) {}
                }
            }
        }

        return null;
    }

    public static boolean installPackageFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return false;
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && !cacheDir.exists()) cacheDir.mkdirs();
        File tempFile = new File(cacheDir, "picked_ext_" + System.currentTimeMillis() + ".zip");
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (is == null) {
                Log.e(TAG, "Gagal membuka InputStream dari URI: " + uri);
                return false;
            }
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
            fos.flush();
        } catch (Throwable e) {
            Log.e(TAG, "Gagal menyalin paket ekstensi dari URI: " + e.getMessage(), e);
            if (tempFile.exists()) tempFile.delete();
            return false;
        }

        try {
            boolean ok = installPackage(context, tempFile);
            tempFile.delete();
            return ok;
        } catch (Throwable e) {
            if (tempFile.exists()) tempFile.delete();
            return false;
        }
    }

    public static void launchExtensionPicker(TermuxActivity act) {
        if (act == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = new String[]{
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream",
                "application/x-chrome-extension",
                "*/*"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            act.startActivityForResult(intent, REQUEST_CODE_PICK_EXTENSION);
        } catch (Throwable e) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                act.startActivityForResult(intent, REQUEST_CODE_PICK_EXTENSION);
            } catch (Throwable err) {
                FusionToast.makeText(act, "Gagal membuka File Manager: " + err.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void handlePickedExtensionUri(final TermuxActivity act, final FusionWebViewManager manager, Uri uri) {
        if (act == null || uri == null) return;
        boolean ok = installPackageFromUri(act, uri);
        if (ok) {
            FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang dari File Manager!", Toast.LENGTH_LONG).show();
            if (manager != null && manager.mActiveWebView != null) {
                setupWebView(manager.mActiveWebView);
            }
            showExtensionsDialog(act, manager);
        } else {
            FusionToast.makeText(act, "❌ Gagal memasang: berkas zip/crx tidak valid atau manifest.json tidak ditemukan.", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean installPackage(Context context, File packageFile) {
        if (packageFile == null) return false;
        File baseDir = getPrimaryExtensionsDir(context);
        String baseName = packageFile.getName().toLowerCase().replace(".crx", "").replace(".zip", "");
        String safeName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File tempExtractDir = new File(baseDir, ".temp_" + System.currentTimeMillis());

        try {
            boolean extracted = extractArchive(packageFile, tempExtractDir);
            if (!extracted) {
                deleteRecursive(tempExtractDir);
                return false;
            }

            File manifestFile = new File(tempExtractDir, "manifest.json");
            if (!manifestFile.exists()) {
                deleteRecursive(tempExtractDir);
                return false;
            }

            ExtensionInfo parsed = parseExtensionDir(tempExtractDir);
            String targetFolderName = (parsed != null && parsed.name != null && !parsed.name.isEmpty())
                    ? parsed.name.toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_")
                    : safeName;
            if (targetFolderName.isEmpty()) targetFolderName = "ext_" + (System.currentTimeMillis() % 10000);

            File finalDir = new File(baseDir, targetFolderName);
            if (finalDir.exists()) {
                deleteRecursive(finalDir);
            }

            boolean renamed = tempExtractDir.renameTo(finalDir);
            if (!renamed || !finalDir.exists()) {
                // Salin manual jika rename lintas filesystem
                copyDirectory(tempExtractDir, finalDir);
                deleteRecursive(tempExtractDir);
            }

            // Setel izin full 777 agar bisa dikelola/dihapus kapan saja
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod -R 777 '" + finalDir.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}

            // Aktifkan ekstensi di extensions_state.json
            Map<String, Boolean> stateMap = loadExtensionsState(context);
            stateMap.put(finalDir.getName(), true);
            saveExtensionsState(context, stateMap);

            Log.i(TAG, "Ekstensi berhasil diinstal ke: " + finalDir.getAbsolutePath());
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal menginstal paket ekstensi " + packageFile.getName() + ": " + e.getMessage(), e);
            deleteRecursive(tempExtractDir);
            return false;
        }
    }

    public static boolean extractArchive(File archiveFile, File targetDir) {
        if (archiveFile == null) return false;
        if (targetDir == null) return false;
        if (!targetDir.exists()) {
            targetDir.mkdirs();
            try {
                targetDir.setReadable(true, false);
                targetDir.setWritable(true, false);
                targetDir.setExecutable(true, false);
            } catch (Throwable ignored) {}
        }
        if (!targetDir.exists() || !targetDir.canWrite()) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "mkdir -p '" + targetDir.getAbsolutePath() + "' && chmod 777 '" + targetDir.getAbsolutePath() + "'"});
                p.waitFor();
            } catch (Throwable ignored) {}
        }

        try {
            byte[] allBytes = readFileBytesSafe(archiveFile);
            if (allBytes == null || allBytes.length == 0) {
                Log.e(TAG, "Gagal membaca bytes dari file arsip: " + archiveFile.getAbsolutePath());
                return false;
            }

            int zipOffset = 0;
            if (allBytes.length >= 4) {
                // Deteksi Cr24 (CRX package)
                if (allBytes[0] == 'C' && allBytes[1] == 'r' && allBytes[2] == '2' && allBytes[3] == '4') {
                    for (int i = 0; i <= allBytes.length - 4; i++) {
                        if (allBytes[i] == 0x50 && allBytes[i + 1] == 0x4B && allBytes[i + 2] == 0x03 && allBytes[i + 3] == 0x04) {
                            zipOffset = i;
                            break;
                        }
                    }
                } else if (allBytes[0] == 0x50 && allBytes[1] == 0x4B && allBytes[2] == 0x03 && allBytes[3] == 0x04) {
                    zipOffset = 0;
                } else {
                    for (int i = 0; i <= Math.min(allBytes.length - 4, 65536); i++) {
                        if (allBytes[i] == 0x50 && allBytes[i + 1] == 0x4B && allBytes[i + 2] == 0x03 && allBytes[i + 3] == 0x04) {
                            zipOffset = i;
                            break;
                        }
                    }
                }
            }

            // Deteksi jika seluruh file di dalam zip dibungkus dalam 1 root folder (misal: "my_ext/manifest.json")
            String rootPrefix = "";
            try (ZipInputStream zisDetect = new ZipInputStream(new ByteArrayInputStream(allBytes, zipOffset, allBytes.length - zipOffset))) {
                ZipEntry ze;
                while ((ze = zisDetect.getNextEntry()) != null) {
                    String name = ze.getName().replace('\\', '/');
                    if (name.equals("manifest.json")) {
                        rootPrefix = "";
                        break;
                    } else if (name.endsWith("/manifest.json")) {
                        rootPrefix = name.substring(0, name.indexOf("manifest.json"));
                        break;
                    }
                }
            } catch (Throwable ignored) {}

            // Ekstrak semua entri
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(allBytes, zipOffset, allBytes.length - zipOffset))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName().replace('\\', '/');
                    if (entryName.contains("..")) continue;
                    if (entryName.startsWith("__MACOSX/") || entryName.endsWith(".DS_Store")) continue;
                    if (!rootPrefix.isEmpty() && entryName.startsWith(rootPrefix)) {
                        entryName = entryName.substring(rootPrefix.length());
                    }
                    if (entryName.isEmpty()) continue;

                    File outFile = new File(targetDir, entryName);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        try {
                            outFile.setReadable(true, false);
                            outFile.setWritable(true, false);
                            outFile.setExecutable(true, false);
                        } catch (Throwable ignored) {}
                    } else {
                        File parent = outFile.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                            try {
                                parent.setReadable(true, false);
                                parent.setWritable(true, false);
                                parent.setExecutable(true, false);
                            } catch (Throwable ignored) {}
                        }
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        try {
                            outFile.setReadable(true, false);
                            outFile.setWritable(true, false);
                        } catch (Throwable ignored) {}
                    }
                }
            }

            File manifestCheck = new File(targetDir, "manifest.json");
            if (manifestCheck.exists() && manifestCheck.isFile()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod -R 777 '" + targetDir.getAbsolutePath() + "'"});
                    p.waitFor();
                } catch (Throwable ignored) {}
                return true;
            }

            // Fallback unzipping via su unzip jika penulisan Java terhalang izin
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "unzip -o '" + archiveFile.getAbsolutePath() + "' -d '" + targetDir.getAbsolutePath() + "' && chmod -R 777 '" + targetDir.getAbsolutePath() + "'"});
                p.waitFor();
                if (manifestCheck.exists() && manifestCheck.isFile()) return true;
            } catch (Throwable ignored) {}

            return false;
        } catch (Throwable e) {
            Log.e(TAG, "Gagal mengekstrak archive " + archiveFile.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Pelindung Keamanan Mutlak (Ironclad Whitelist Guard)
     * Memastikan tidak ada perintah penghapusan (terutama su rm -rf) yang menyentuh direktori
     * sistem atau direktori di luar sandbox ekstensi Fusion. Mencegah brick / mati total.
     */
    public static boolean isSafeExtensionPath(File f) {
        if (f == null) return false;
        try {
            String canonical = f.getCanonicalPath();
            if (canonical == null || canonical.trim().isEmpty() || canonical.length() < 20) {
                return false;
            }

            // 1. Blacklist Mutlak: Direktori root & partisi sistem vital Android/Linux
            String[] forbiddenPrefixes = new String[] {
                "/system", "/vendor", "/product", "/odm", "/apex", "/bin", "/sbin", "/etc",
                "/boot", "/recovery", "/persist", "/metadata", "/dev", "/proc", "/sys",
                "/storage", "/sdcard"
            };
            for (String fp : forbiddenPrefixes) {
                if (canonical.equals(fp) || canonical.startsWith(fp + "/")) {
                    Log.e(TAG, "⛔ BLOCKED DANGEROUS DELETE: Path berada di partisi sistem -> " + canonical);
                    return false;
                }
            }

            // 2. Blacklist Direktori Induk Kritis (Jangan pernah izinkan penghapusan folder induk ini)
            String[] forbiddenExact = new String[] {
                "/",
                "/root",
                "/data",
                "/data/data",
                "/data/user",
                "/data/user/0",
                "/data/data/com.termux",
                "/data/user/0/com.termux",
                "/data/data/com.termux/files",
                "/data/user/0/com.termux/files",
                "/data/data/com.termux/files/home",
                "/data/user/0/com.termux/files/home",
                "/data/data/com.termux/cache",
                "/data/user/0/com.termux/cache",
                "/data/local",
                "/data/local/nhsystem",
                "/data/local/nhsystem/kali-arm64",
                "/data/local/nhsystem/kali-arm64/root",
                "/data/data/com.termux/files/home/.fusion",
                "/data/user/0/com.termux/files/home/.fusion",
                "/data/data/com.termux/files/home/.fusion/extensions",
                "/data/user/0/com.termux/files/home/.fusion/extensions",
                "/data/local/nhsystem/kali-arm64/root/.fusion",
                "/data/local/nhsystem/kali-arm64/root/.fusion/extensions",
                "/root/.fusion",
                "/root/.fusion/extensions"
            };
            for (String fe : forbiddenExact) {
                if (canonical.equals(fe) || canonical.equals(fe + "/")) {
                    Log.e(TAG, "⛔ BLOCKED DANGEROUS DELETE: Folder induk kritis tidak boleh dihapus -> " + canonical);
                    return false;
                }
            }

            // 3. Whitelist Ketat: Wajib berada DI DALAM salah satu folder ekstensi atau cache Fusion
            String[] allowedParentPrefixes = new String[] {
                "/data/data/com.termux/files/home/.fusion/extensions/",
                "/data/user/0/com.termux/files/home/.fusion/extensions/",
                "/data/local/nhsystem/kali-arm64/root/.fusion/extensions/",
                "/root/.fusion/extensions/",
                "/data/data/com.termux/files/fusion_extensions/",
                "/data/user/0/com.termux/files/fusion_extensions/",
                "/data/data/com.termux/cache/",
                "/data/user/0/com.termux/cache/"
            };

            for (String prefix : allowedParentPrefixes) {
                if (canonical.startsWith(prefix) && canonical.length() > prefix.length()) {
                    String sub = canonical.substring(prefix.length());
                    if (!sub.isEmpty() && !sub.equals(".") && !sub.equals("..") && !sub.contains("../")) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal memverifikasi keamanan path: " + e.getMessage());
            return false;
        }
        return false;
    }

    public static boolean deleteRecursive(File f) {
        if (f == null || !f.exists()) return true;

        // VERIFIKASI KEAMANAN MUTLAK: Tolak jika tidak berada di whitelist sandbox ekstensi
        if (!isSafeExtensionPath(f)) {
            Log.e(TAG, "⛔ deleteRecursive DIBATALKAN: Path tidak lolos audit keamanan -> " + f.getAbsolutePath());
            return false;
        }

        // 1. Coba hapus rekursif menggunakan Java murni terlebih dahulu
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        boolean ok = f.delete();
        if (ok || !f.exists()) return true;

        // 2. Beri izin write lalu coba delete Java lagi
        try {
            f.setWritable(true, false);
            if (f.delete() || !f.exists()) return true;
        } catch (Throwable ignored) {}

        // 3. Fallback su rm -rf HANYA jika lolos validasi whitelist dan path canonical dijamin aman
        try {
            String safePath = f.getCanonicalPath();
            if (isSafeExtensionPath(f) && safePath != null && safePath.length() >= 25) {
                String escapedPath = safePath.replace("'", "'\\''");
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -rf '" + escapedPath + "'"});
                p.waitFor();
            }
        } catch (Throwable ignored) {}

        return !f.exists();
    }

    public static void copyDirectory(File source, File target) {
        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(target, child));
                }
            }
        } else {
            try (FileInputStream in = new FileInputStream(source);
                 FileOutputStream out = new FileOutputStream(target)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (Throwable ignored) {}
        }
    }

    public static String getChromePolyfillScript(Context context) {
        String currentStorageJson = getStorageJson(context).replace("\\", "\\\\").replace("'", "\\'");
        return "(function() {\n" +
                "  if (window.chrome && window.chrome.__is_fusion_polyfill__) return;\n" +
                "  var extStorage = {};\n" +
                "  try {\n" +
                "    var raw = window.__FusionExtensionBridge__ ? window.__FusionExtensionBridge__.getStorage() : null;\n" +
                "    if (raw) extStorage = JSON.parse(raw);\n" +
                "  } catch(e) {}\n" +
                "  if (!extStorage || !extStorage.gapt_layer) {\n" +
                "    try {\n" +
                "      var fallbackRaw = '" + currentStorageJson + "';\n" +
                "      if (fallbackRaw) extStorage = JSON.parse(fallbackRaw);\n" +
                "    } catch(e) { extStorage = { gapt_layer: '12m' }; }\n" +
                "  }\n" +
                "  var msgListeners = [];\n" +
                "  var storageListeners = [];\n" +
                "  var chromeShim = {\n" +
                "    __is_fusion_polyfill__: true,\n" +
                "    runtime: {\n" +
                "      id: 'fusion-extension-runtime',\n" +
                "      getURL: function(path) { return path; },\n" +
                "      onMessage: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && msgListeners.indexOf(fn) === -1) msgListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = msgListeners.indexOf(fn);\n" +
                "          if (idx !== -1) msgListeners.splice(idx, 1);\n" +
                "        }\n" +
                "      },\n" +
                "      sendMessage: function(msg, cb) {\n" +
                "        var handled = false;\n" +
                "        for (var i = 0; i < msgListeners.length; i++) {\n" +
                "          try {\n" +
                "            var res = msgListeners[i](msg, { id: 'fusion-extension' }, function(response) {\n" +
                "              if (cb) cb(response);\n" +
                "            });\n" +
                "            if (res === true) handled = true;\n" +
                "          } catch(err) { console.error('[Chrome Shim onMessage Error]:', err); }\n" +
                "        }\n" +
                "        if (!handled && cb) { setTimeout(function() { cb(null); }, 0); }\n" +
                "      }\n" +
                "    },\n" +
                "    storage: {\n" +
                "      local: {\n" +
                "        get: function(keys, cb) {\n" +
                "          var result = {};\n" +
                "          if (!keys) {\n" +
                "            result = Object.assign({}, extStorage);\n" +
                "          } else if (typeof keys === 'string') {\n" +
                "            result[keys] = extStorage[keys];\n" +
                "          } else if (Array.isArray(keys)) {\n" +
                "            for (var i = 0; i < keys.length; i++) result[keys[i]] = extStorage[keys[i]];\n" +
                "          } else if (typeof keys === 'object') {\n" +
                "            for (var k in keys) result[k] = (extStorage[k] !== undefined) ? extStorage[k] : keys[k];\n" +
                "          }\n" +
                "          if (cb) setTimeout(function() { cb(result); }, 0);\n" +
                "          return (typeof Promise !== 'undefined') ? Promise.resolve(result) : undefined;\n" +
                "        },\n" +
                "        set: function(items, cb) {\n" +
                "          var changes = {};\n" +
                "          if (items && typeof items === 'object') {\n" +
                "            for (var k in items) {\n" +
                "              var oldVal = extStorage[k];\n" +
                "              extStorage[k] = items[k];\n" +
                "              changes[k] = { oldValue: oldVal, newValue: items[k] };\n" +
                "              try { localStorage.setItem(k, items[k]); } catch(e) {}\n" +
                "            }\n" +
                "            try {\n" +
                "              if (window.__FusionExtensionBridge__) window.__FusionExtensionBridge__.setStorage(JSON.stringify(extStorage));\n" +
                "            } catch(e) {}\n" +
                "          }\n" +
                "          for (var i = 0; i < storageListeners.length; i++) {\n" +
                "            try { storageListeners[i](changes, 'local'); } catch(e) {}\n" +
                "          }\n" +
                "          if (cb) setTimeout(function() { cb(); }, 0);\n" +
                "          return (typeof Promise !== 'undefined') ? Promise.resolve() : undefined;\n" +
                "        },\n" +
                "        remove: function(keys, cb) {\n" +
                "          var list = Array.isArray(keys) ? keys : [keys];\n" +
                "          for (var i = 0; i < list.length; i++) {\n" +
                "            delete extStorage[list[i]];\n" +
                "            try { localStorage.removeItem(list[i]); } catch(e) {}\n" +
                "          }\n" +
                "          try {\n" +
                "            if (window.__FusionExtensionBridge__) window.__FusionExtensionBridge__.setStorage(JSON.stringify(extStorage));\n" +
                "          } catch(e) {}\n" +
                "          if (cb) setTimeout(cb, 0);\n" +
                "        },\n" +
                "        clear: function(cb) {\n" +
                "          extStorage = {};\n" +
                "          try {\n" +
                "            if (window.__FusionExtensionBridge__) window.__FusionExtensionBridge__.setStorage('{}');\n" +
                "          } catch(e) {}\n" +
                "          if (cb) setTimeout(cb, 0);\n" +
                "        }\n" +
                "      },\n" +
                "      onChanged: {\n" +
                "        addListener: function(fn) {\n" +
                "          if (typeof fn === 'function' && storageListeners.indexOf(fn) === -1) storageListeners.push(fn);\n" +
                "        },\n" +
                "        removeListener: function(fn) {\n" +
                "          var idx = storageListeners.indexOf(fn);\n" +
                "          if (idx !== -1) storageListeners.splice(idx, 1);\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    tabs: {\n" +
                "      sendMessage: function(tabId, msg, cb) {\n" +
                "        if (chromeShim.runtime && chromeShim.runtime.sendMessage) chromeShim.runtime.sendMessage(msg, cb);\n" +
                "      },\n" +
                "      query: function(info, cb) {\n" +
                "        if (cb) cb([{ id: 1, active: true, url: window.location.href }]);\n" +
                "      }\n" +
                "    }\n" +
                "  };\n" +
                "  window.chrome = Object.assign(window.chrome || {}, chromeShim);\n" +
                "})();\n";
    }

    public static String buildContentScriptWrapper(ExtensionInfo ext, ExtensionInfo.ContentScript cs, int scriptIndex) {
        if (ext == null || cs == null || cs.jsFiles.isEmpty()) return null;

        StringBuilder jsCodeBuilder = new StringBuilder();
        for (String jsFileRel : cs.jsFiles) {
            File f = new File(ext.dir, jsFileRel);
            if (f.exists() && f.isFile()) {
                try {
                    byte[] bytes = readFileBytesSafe(f);
                    if (bytes != null && bytes.length > 0) {
                        jsCodeBuilder.append("/* --- ").append(jsFileRel).append(" --- */\n");
                        jsCodeBuilder.append(new String(bytes, StandardCharsets.UTF_8)).append("\n;\n");
                    }
                } catch (Throwable ignored) {}
            }
        }
        String combinedScript = jsCodeBuilder.toString();
        if (combinedScript.trim().isEmpty()) return null;

        JSONArray matchesJson = new JSONArray();
        for (String m : cs.matches) {
            matchesJson.put(m);
        }

        String safeExtId = ext.id.replaceAll("[^a-zA-Z0-9_]", "_");
        String runKey = "__FUSION_EXT_" + safeExtId + "_" + scriptIndex + "__";

        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  try {\n");
        sb.append("    var href = window.location.href;\n");
        sb.append("    if (!href || href === 'about:blank') return;\n");
        sb.append("    var matches = ").append(matchesJson.toString()).append(";\n");
        sb.append("    var allFrames = ").append(cs.allFrames).append(";\n");
        sb.append("    var runAt = '").append(cs.runAt).append("';\n");
        sb.append("    function matchUrl(url, patterns) {\n");
        sb.append("      if (!url) return false;\n");
        sb.append("      for (var i = 0; i < patterns.length; i++) {\n");
        sb.append("        var p = patterns[i];\n");
        sb.append("        if (p === '<all_urls>' || p === '*://*/*' || p === '*') return true;\n");
        sb.append("        try {\n");
        sb.append("          var regStr = '^' + p.replace(/\\\\/g, '\\\\\\\\').replace(/\\./g, '\\\\.').replace(/\\*:\\/\\//g, 'https?:\\\\/\\\\/').replace(/http:\\/\\/\\*/g, 'http:\\\\/\\\\/.*').replace(/https:\\/\\/\\*/g, 'https:\\\\/\\\\/.*').replace(/\\*/g, '.*') + '$';\n");
        sb.append("          if (new RegExp(regStr).test(url)) return true;\n");
        sb.append("        } catch(e) {\n");
        sb.append("          if (url.indexOf(p) !== -1) return true;\n");
        sb.append("        }\n");
        sb.append("      }\n");
        sb.append("      return false;\n");
        sb.append("    }\n");
        sb.append("    if (!matchUrl(href, matches)) return;\n");
        sb.append("    if (!allFrames && window.top !== window) return;\n");
        sb.append("    var runKey = '").append(runKey).append("';\n");
        sb.append("    if (window[runKey]) return;\n");
        sb.append("    function executeCode() {\n");
        sb.append("      if (window[runKey]) return;\n");
        sb.append("      window[runKey] = true;\n");
        sb.append("      try {\n");
        sb.append(combinedScript).append("\n");
        sb.append("      } catch(e) { console.error('[Fusion Extension ").append(safeExtId).append(" Error]:', e); }\n");
        sb.append("    }\n");
        sb.append("    if (runAt === 'document_start') {\n");
        sb.append("      executeCode();\n");
        sb.append("    } else if (runAt === 'document_end') {\n");
        sb.append("      if (document.readyState === 'interactive' || document.readyState === 'complete') executeCode();\n");
        sb.append("      else document.addEventListener('DOMContentLoaded', executeCode);\n");
        sb.append("    } else {\n");
        sb.append("      if (document.readyState === 'complete') setTimeout(executeCode, 20);\n");
        sb.append("      else {\n");
        sb.append("        window.addEventListener('load', function() { setTimeout(executeCode, 20); });\n");
        sb.append("        setTimeout(executeCode, 1200);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  } catch(err) { console.error('[Fusion Extension Wrapper Error]:', err); }\n");
        sb.append("})();\n");
        return sb.toString();
    }

    public static void setupWebView(WebView webView) {
        if (webView == null) return;
        try {
            Context ctx = webView.getContext();
            String polyfill = getChromePolyfillScript(ctx);

            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                // Register polyfill chrome.* runtime
                WebViewCompat.addDocumentStartJavaScript(webView, polyfill, Collections.singleton("*"));

                // Register semua content script yang aktif
                List<ExtensionInfo> exts = getInstalledExtensions(ctx);
                for (ExtensionInfo ext : exts) {
                    if (!ext.enabled) continue;
                    for (int i = 0; i < ext.contentScripts.size(); i++) {
                        ExtensionInfo.ContentScript cs = ext.contentScripts.get(i);
                        String wrapped = buildContentScriptWrapper(ext, cs, i);
                        if (wrapped != null && !wrapped.isEmpty()) {
                            WebViewCompat.addDocumentStartJavaScript(webView, wrapped, Collections.singleton("*"));
                        }
                    }
                }
                Log.i(TAG, "Berhasil meregistrasikan DocumentStartJavaScript untuk " + exts.size() + " ekstensi.");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Gagal setupWebView ekstensi: " + e.getMessage());
        }
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

    public static void injectMatchingScripts(WebView webView, String currentUrl, String triggerTiming) {
        if (webView == null || currentUrl == null || currentUrl.isEmpty()) return;
        if (currentUrl.startsWith("file:///android_asset/") || currentUrl.startsWith("about:") || currentUrl.startsWith("javascript:")) {
            return;
        }

        Context ctx = webView.getContext();
        // Injeksi polyfill jika belum ada
        String polyfill = getChromePolyfillScript(ctx);
        webView.evaluateJavascript(polyfill, null);

        List<ExtensionInfo> exts = getInstalledExtensions(ctx);
        for (ExtensionInfo ext : exts) {
            if (!ext.enabled) continue;
            for (int i = 0; i < ext.contentScripts.size(); i++) {
                ExtensionInfo.ContentScript cs = ext.contentScripts.get(i);
                if (isUrlMatched(currentUrl, cs.matches)) {
                    String wrapped = buildContentScriptWrapper(ext, cs, i);
                    if (wrapped != null) {
                        webView.evaluateJavascript(wrapped, null);
                    }
                }
            }
        }
    }

    public static List<File> scanCandidateArchives() {
        List<File> archives = new ArrayList<>();
        String[] candidateDirs = new String[] {
            "/data/data/com.termux/files/home",
            "/data/data/com.termux/files/home/.fusion",
            "/sdcard/Download",
            "/sdcard",
            "/root",
            "/project"
        };
        Set<String> seen = new HashSet<>();

        String[] priorityFiles = new String[] {
            "/data/data/com.termux/files/home/pro.zip",
            "/data/data/com.termux/files/home/.fusion/pro.zip",
            "/sdcard/Download/pro.zip",
            "/root/pro.zip",
            "/project/pro.zip"
        };
        for (String pf : priorityFiles) {
            File f = new File(pf);
            if (f.exists() && f.isFile()) {
                archives.add(f);
                seen.add(f.getAbsolutePath());
                break;
            }
        }

        for (String dirPath : candidateDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            String name = f.getName().toLowerCase();
                            if ((name.endsWith(".zip") || name.endsWith(".crx")) && !name.contains("payload") && !name.contains("gradle")) {
                                if (!seen.contains(f.getAbsolutePath())) {
                                    seen.add(f.getAbsolutePath());
                                    archives.add(f);
                                }
                            }
                        }
                    }
                }
            }
        }
        return archives;
    }

    public static void showExtensionsDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (manager != null) {
            FusionDrawerHelper.showExtensionDrawer(manager);
            return;
        }
        if (act == null) return;
        final Context context = act;
        final List<ExtensionInfo> extensions = getInstalledExtensions(context);

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("🧩 Manajer Ekstensi Fusion (Kiwi-Style)");

        StringBuilder sb = new StringBuilder();
        sb.append("Engine Ekstensi Chrome (Manifest V2 & V3):\n");
        sb.append("📁 Folder: ~/.fusion/extensions/\n");
        sb.append("📊 Terpasang: ").append(extensions.size()).append(" ekstensi\n\n");

        if (extensions.isEmpty()) {
            sb.append("• Belum ada ekstensi .zip / .crx yang terpasang.\n");
            File proZip = new File("/sdcard/Download/pro.zip");
            File homeProZip = new File("/data/data/com.termux/files/home/pro.zip");
            if (proZip.exists() || homeProZip.exists()) {
                sb.append("💡 Ditemukan paket pro.zip! Klik '➕ Pasang .zip / .crx' untuk menginstal langsung.\n");
            } else {
                sb.append("• Tuan bisa memasang file .zip atau .crx ekstensi Chrome desktop langsung ke dalam WebView.\n");
            }
        } else {
            sb.append("Daftar Ekstensi Terpasang (Klik '⚙️ Kelola' untuk opsi/hapus):\n");
            for (int i = 0; i < extensions.size(); i++) {
                ExtensionInfo ext = extensions.get(i);
                sb.append(" [").append(i + 1).append("] ").append(ext.name)
                  .append(" (v").append(ext.version).append(")")
                  .append(ext.enabled ? " [AKTIF]" : " [NONAKTIF]").append("\n");
            }
        }
        b.setMessage(sb.toString());

        b.setPositiveButton("➕ Pasang .zip / .crx", (dialog, which) -> {
            showImportPackageDialog(act, manager);
        });

        if (!extensions.isEmpty()) {
            b.setNeutralButton("⚙️ Kelola Ekstensi", (dialog, which) -> {
                showManageExtensionsDialog(act, manager, extensions);
            });
        } else {
            b.setNeutralButton("📜 Userscript", (dialog, which) -> {
                FusionUserscriptHelper.showUserscriptsDialog(act, manager);
            });
        }

        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public static void showManageExtensionsDialog(final TermuxActivity act, final FusionWebViewManager manager, final List<ExtensionInfo> extensions) {
        if (act == null || extensions.isEmpty()) return;

        String[] items = new String[extensions.size()];
        for (int i = 0; i < extensions.size(); i++) {
            ExtensionInfo ext = extensions.get(i);
            items[i] = (i + 1) + ". " + ext.name + (ext.enabled ? " 🟢" : " ⚪");
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ Pilih Ekstensi untuk Dikelola");
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < extensions.size()) {
                showExtensionActionsDialog(act, manager, extensions.get(which));
            }
        });
        b.setNegativeButton("Kembali", (dialog, which) -> {
            showExtensionsDialog(act, manager);
        });
        b.show();
    }

    public static void showExtensionActionsDialog(final TermuxActivity act, final FusionWebViewManager manager, final ExtensionInfo ext) {
        if (act == null || ext == null) return;

        List<String> options = new ArrayList<>();
        options.add(ext.enabled ? "⚪ Matikan Ekstensi" : "🟢 Aktifkan Ekstensi");
        options.add("⚙️ Setelan Layer / Opsi Token (Google AI Pro)");
        if (ext.popupHtml != null && !ext.popupHtml.isEmpty()) {
            options.add("🌐 Buka Popup HTML di Tab Baru");
        }
        options.add("🗑️ Hapus Ekstensi");

        String[] itemArray = options.toArray(new String[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ " + ext.name);
        b.setItems(itemArray, (dialog, which) -> {
            String selected = itemArray[which];
            if (selected.contains("Matikan") || selected.contains("Aktifkan")) {
                ext.enabled = !ext.enabled;
                Map<String, Boolean> stateMap = loadExtensionsState(act);
                stateMap.put(ext.id, ext.enabled);
                saveExtensionsState(act, stateMap);
                FusionToast.makeText(act, ext.name + (ext.enabled ? " diaktifkan." : " dinonaktifkan."), Toast.LENGTH_SHORT).show();
                if (manager != null && manager.mActiveWebView != null) {
                    setupWebView(manager.mActiveWebView);
                }
                showExtensionsDialog(act, manager);
            } else if (selected.contains("Setelan Layer")) {
                showLayerConfigDialog(act, manager, ext);
            } else if (selected.contains("Buka Popup")) {
                if (manager != null && ext.popupHtml != null) {
                    File popupFile = new File(ext.dir, ext.popupHtml);
                    manager.addNewTab("file://" + popupFile.getAbsolutePath());
                    manager.closeAllDrawers();
                }
            } else if (selected.contains("Hapus")) {
                AlertDialog.Builder delConfirm = new AlertDialog.Builder(act);
                delConfirm.setTitle("Hapus Ekstensi?");
                delConfirm.setMessage("Yakin ingin menghapus ekstensi \"" + ext.name + "\"?");
                delConfirm.setPositiveButton("Hapus", (d, w) -> {
                    boolean deleted = deleteRecursive(ext.dir);
                    Map<String, Boolean> stateMap = loadExtensionsState(act);
                    stateMap.remove(ext.id);
                    saveExtensionsState(act, stateMap);

                    if (deleted) {
                        FusionToast.makeText(act, "Ekstensi \"" + ext.name + "\" berhasil dihapus.", Toast.LENGTH_SHORT).show();
                    } else {
                        FusionToast.makeText(act, "Gagal menghapus berkas ekstensi.", Toast.LENGTH_SHORT).show();
                    }
                    showExtensionsDialog(act, manager);
                });
                delConfirm.setNegativeButton("Batal", null);
                delConfirm.show();
            }
        });
        b.setNegativeButton("Kembali", (dialog, which) -> {
            showExtensionsDialog(act, manager);
        });
        b.show();
    }

    public static void showLayerConfigDialog(final TermuxActivity act, final FusionWebViewManager manager, final ExtensionInfo ext) {
        if (act == null) return;
        String currentLayer = getLayerConfig(act);

        final String[] layers = new String[] {
            "12m (🟢 12 Bulan Free $0.00 — AI Pro 2TB)",
            "6m_ss (🟡 6 Bulan Free — AI Pro SS)",
            "6m (🟡 6 Bulan Free — Tier 1)",
            "6m_2 (🟡 6 Bulan Free — Tier 2)",
            "off (🔴 Matikan Injeksi Offer Token)"
        };
        final String[] layerCodes = new String[] {
            "12m", "6m_ss", "6m", "6m_2", "off"
        };

        int selectedIdx = 0;
        for (int i = 0; i < layerCodes.length; i++) {
            if (layerCodes[i].equalsIgnoreCase(currentLayer)) {
                selectedIdx = i;
                break;
            }
        }

        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("⚙️ Setelan Penawaran Google AI Pro");
        b.setSingleChoiceItems(layers, selectedIdx, (dialog, which) -> {
            if (which >= 0 && which < layerCodes.length) {
                String chosen = layerCodes[which];
                setLayerConfig(act, chosen);
                FusionToast.makeText(act, "✅ Layer AI Pro disetel ke: " + chosen, Toast.LENGTH_SHORT).show();

                // Live postMessage ke halaman webview jika sedang membuka Google One
                if (manager != null && manager.mActiveWebView != null) {
                    manager.mActiveWebView.evaluateJavascript(
                        "(function() {\n" +
                        "  try {\n" +
                        "    if (window.chrome && window.chrome.storage && window.chrome.storage.local) {\n" +
                        "      window.chrome.storage.local.set({ gapt_layer: '" + chosen + "' });\n" +
                        "    }\n" +
                        "    window.postMessage({ __gapt: 1, type: 'cfg', layer: '" + chosen + "' }, '*');\n" +
                        "  } catch(e) {}\n" +
                        "})();", null);
                }
                dialog.dismiss();
            }
        });
        b.setNegativeButton("Tutup", null);
        b.show();
    }

    public static void showImportPackageDialog(final TermuxActivity act, final FusionWebViewManager manager) {
        if (act == null) return;
        final List<File> archives = scanCandidateArchives();

        List<String> displayNames = new ArrayList<>();
        // Opsi 1: Pemilih Berkas Resmi Android (SAF) - 100% Dukungan Non-Root & Akses SDCard Tanpa Batasan Scoped Storage
        displayNames.add("📁 Buka File Manager Android (SAF - Non-Root / SDCard)");

        for (File f : archives) {
            String sizeKb = (f.length() / 1024) + " KB";
            if (f.getName().equalsIgnoreCase("pro.zip")) {
                displayNames.add("⭐ pro.zip (" + f.getParent() + ") [" + sizeKb + "]");
            } else {
                displayNames.add("📦 " + f.getName() + " (" + f.getParent() + ") [" + sizeKb + "]");
            }
        }
        displayNames.add("✏️ Masukkan Path File Manual (.zip / .crx)");

        String[] items = displayNames.toArray(new String[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(act);
        b.setTitle("📦 Pilih Paket Ekstensi (.zip / .crx)");
        b.setItems(items, (dialog, which) -> {
            if (which == 0) {
                // Buka File Picker Bawaan Android via SAF
                launchExtensionPicker(act);
            } else if (which > 0 && which <= archives.size()) {
                File selected = archives.get(which - 1);
                boolean ok = installPackage(act, selected);
                if (ok) {
                    FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang dari " + selected.getName(), Toast.LENGTH_LONG).show();
                    if (manager != null && manager.mActiveWebView != null) {
                        setupWebView(manager.mActiveWebView);
                    }
                    showExtensionsDialog(act, manager);
                } else {
                    FusionToast.makeText(act, "❌ Gagal memasang ekstensi: berkas arsip tidak dapat diekstrak.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Input path manual
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(act);
                inputDialog.setTitle("Ketik Path Paket Ekstensi");
                final EditText input = new EditText(act);
                input.setHint("/data/data/com.termux/files/home/pro.zip");
                input.setText("/data/data/com.termux/files/home/pro.zip");
                inputDialog.setView(input);
                inputDialog.setPositiveButton("Pasang", (d, w) -> {
                    String p = input.getText().toString().trim();
                    File customFile = new File(p);
                    boolean ok = installPackage(act, customFile);
                    if (ok) {
                        FusionToast.makeText(act, "✅ Ekstensi berhasil dipasang!", Toast.LENGTH_LONG).show();
                        if (manager != null && manager.mActiveWebView != null) {
                            setupWebView(manager.mActiveWebView);
                        }
                        showExtensionsDialog(act, manager);
                    } else {
                        FusionToast.makeText(act, "❌ Gagal pasang: file bukan zip/crx ekstensi valid atau tidak dapat diakses.", Toast.LENGTH_LONG).show();
                    }
                });
                inputDialog.setNegativeButton("Batal", null);
                inputDialog.show();
            }
        });
        b.setNegativeButton("Batal", null);
        b.show();
    }
}
