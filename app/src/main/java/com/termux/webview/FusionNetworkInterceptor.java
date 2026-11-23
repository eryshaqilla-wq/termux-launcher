package com.termux.webview;

import android.webkit.WebResourceResponse;
import android.util.Log;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * High-performance wire-level network request interceptor and response mocker.
 * Operates at WebViewClient.shouldInterceptRequest level before network packets are dispatched.
 */
public class FusionNetworkInterceptor {
    private static final String TAG = "FusionNetInterceptor";

    public static class MockRule {
        public String patternStr;
        public Pattern regex;
        public byte[] body;
        public int statusCode;
        public String mimeType;
        public Map<String, String> headers;

        public MockRule(String patternStr, String bodyStr, int statusCode, String mimeType, String headersJson) {
            this.patternStr = patternStr;
            this.regex = compilePattern(patternStr);
            this.body = (bodyStr != null) ? bodyStr.getBytes(StandardCharsets.UTF_8) : new byte[0];
            this.statusCode = (statusCode > 0) ? statusCode : 200;
            this.mimeType = (mimeType != null && !mimeType.isEmpty()) ? mimeType : "application/json";
            this.headers = new HashMap<>();
            this.headers.put("Access-Control-Allow-Origin", "*");
            this.headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            this.headers.put("Access-Control-Allow-Headers", "*");
            if (headersJson != null && !headersJson.isEmpty() && !headersJson.equals("{}")) {
                try {
                    JSONObject obj = new JSONObject(headersJson);
                    java.util.Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        this.headers.put(k, obj.optString(k));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public static class BlockRule {
        public String patternStr;
        public Pattern regex;

        public BlockRule(String patternStr) {
            this.patternStr = patternStr;
            this.regex = compilePattern(patternStr);
        }
    }

    private static final Map<String, MockRule> sMockRules = new ConcurrentHashMap<>();
    private static final Map<String, BlockRule> sBlockRules = new ConcurrentHashMap<>();

    private static Pattern compilePattern(String globOrRegex) {
        if (globOrRegex == null || globOrRegex.isEmpty()) return Pattern.compile(".*");
        try {
            if (globOrRegex.startsWith("^") || globOrRegex.endsWith("$")) {
                return Pattern.compile(globOrRegex, Pattern.CASE_INSENSITIVE);
            }
            String regexStr = globOrRegex
                    .replace(".", "\\.")
                    .replace("?", ".")
                    .replace("*", ".*");
            return Pattern.compile(".*" + regexStr + ".*", Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            return Pattern.compile(".*");
        }
    }

    public static boolean addMockRule(String pattern, String body, int statusCode, String mimeType, String headersJson) {
        if (pattern == null || pattern.trim().isEmpty()) return false;
        MockRule rule = new MockRule(pattern.trim(), body, statusCode, mimeType, headersJson);
        sMockRules.put(pattern.trim(), rule);
        Log.i(TAG, "Added mock rule for: " + pattern);
        return true;
    }

    public static boolean addBlockRule(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) return false;
        BlockRule rule = new BlockRule(pattern.trim());
        sBlockRules.put(pattern.trim(), rule);
        Log.i(TAG, "Added block rule for: " + pattern);
        return true;
    }

    public static boolean removeRule(String pattern) {
        if (pattern == null) return false;
        sMockRules.remove(pattern.trim());
        sBlockRules.remove(pattern.trim());
        return true;
    }

    public static void clearRules() {
        sMockRules.clear();
        sBlockRules.clear();
        Log.i(TAG, "Cleared all network interception rules");
    }

    public static WebResourceResponse intercept(String url) {
        if (url == null || url.isEmpty()) return null;

        // 1. Check block rules first
        for (BlockRule br : sBlockRules.values()) {
            if (br.regex.matcher(url).find()) {
                Log.i(TAG, "Blocked wire request: " + url + " by rule " + br.patternStr);
                return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked by Fusion", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
        }

        // 2. Check mock rules
        for (MockRule mr : sMockRules.values()) {
            if (mr.regex.matcher(url).find()) {
                Log.i(TAG, "Mocked wire request: " + url + " by rule " + mr.patternStr);
                return new WebResourceResponse(mr.mimeType, "UTF-8", mr.statusCode, "OK", mr.headers, new ByteArrayInputStream(mr.body));
            }
        }

        return null;
    }
}
