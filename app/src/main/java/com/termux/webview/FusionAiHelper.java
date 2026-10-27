package com.termux.webview;

import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Helper sinkronisasi status aktif 3 Mesin AI (Custom DeepSeek, 9Router, AGY Copilot)
 * antara UI Android Drawer, config file, dan Webview injected script.
 */
public class FusionAiHelper {

    private static String sCachedEngine = null;

    public static File getAiConfigFile() {
        String walletDir = FusionWalletHelper.getWalletDir();
        if (walletDir != null) {
            File f = new File(walletDir, "ai_config.json");
            if (f.exists()) {
                return f;
            }
        }
        File fRoot = new File("/root/wallet/ai_config.json");
        if (fRoot.exists()) return fRoot;
        File fSym = new File("/wallet/ai_config.json");
        if (fSym.exists()) return fSym;
        return new File("/data/data/com.termux/files/home/wallet/ai_config.json");
    }

    public static String getActiveEngine() {
        if (sCachedEngine != null) {
            return sCachedEngine;
        }
        try {
            File f = getAiConfigFile();
            if (f.exists() && f.canRead()) {
                FileInputStream fis = new FileInputStream(f);
                byte[] b = new byte[(int) f.length()];
                int n = fis.read(b);
                fis.close();
                if (n > 0) {
                    JSONObject json = new JSONObject(new String(b, 0, n, "UTF-8"));
                    String engine = json.optString("active_engine", "");
                    if (engine != null && engine.length() > 0) {
                        sCachedEngine = engine.toLowerCase();
                        return sCachedEngine;
                    }
                    if (json.optString("apiKey", "").length() > 0) {
                        sCachedEngine = "custom";
                        return sCachedEngine;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        sCachedEngine = "custom";
        return sCachedEngine;
    }

    public static void setActiveEngine(String engine) {
        if (engine == null) return;
        String clean = engine.trim().toLowerCase();
        sCachedEngine = clean;
        try {
            File f = getAiConfigFile();
            JSONObject json;
            if (f.exists() && f.canRead()) {
                FileInputStream fis = new FileInputStream(f);
                byte[] b = new byte[(int) f.length()];
                int n = fis.read(b);
                fis.close();
                json = n > 0 ? new JSONObject(new String(b, 0, n, "UTF-8")) : new JSONObject();
            } else {
                json = new JSONObject();
            }
            json.put("active_engine", clean);
            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(json.toString(4).getBytes("UTF-8"));
            fos.flush();
            fos.close();
        } catch (Exception ignored) {
        }
    }

    public static String getDisplayName(String engine) {
        if ("custom".equalsIgnoreCase(engine)) return "DeepSeek (Custom)";
        if ("9router".equalsIgnoreCase(engine)) return "9Router (Port 20128)";
        if ("agy".equalsIgnoreCase(engine)) return "AGY Copilot";
        return "DeepSeek (Custom)";
    }
}
