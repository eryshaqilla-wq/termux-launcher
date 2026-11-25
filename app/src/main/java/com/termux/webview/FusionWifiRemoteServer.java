package com.termux.webview;

import android.util.Log;
import com.termux.app.TermuxIpcServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Local Wi-Fi (LAN) HTTP Remote Control Server for Fusion WebView & Termux IPC.
 * Direct IP:Port access on the local Wi-Fi network without token friction. Disabled by default until started via IPC.
 */
public class FusionWifiRemoteServer {
    private static final String TAG = "FusionWifiRemote";
    public static final int DEFAULT_PORT = 8765;

    private static volatile boolean sRunning = false;
    private static volatile int sPort = -1;
    private static ServerSocket sServerSocket = null;
    private static ExecutorService sClientPool = null;

    public static boolean isRunning() {
        return sRunning && sServerSocket != null && !sServerSocket.isClosed();
    }

    public static int getPort() {
        return sPort;
    }

    public static synchronized JSONObject start(int requestedPort) {
        if (isRunning()) {
            return getStatusJson();
        }
        int targetPort = (requestedPort > 0 && requestedPort <= 65535) ? requestedPort : DEFAULT_PORT;

        try {
            ServerSocket ss;
            try {
                ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress("0.0.0.0", targetPort), 50);
            } catch (Throwable busy) {
                ss = new ServerSocket(0, 50, InetAddress.getByName("0.0.0.0"));
            }
            sServerSocket = ss;
            sPort = ss.getLocalPort();
            sRunning = true;
            sClientPool = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "FusionWifiWorker");
                t.setDaemon(true);
                return t;
            });

            writeEndpointFiles(getLocalLanIp(), sPort);
            try {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr != null) mgr.acquireWakeLock();
            } catch (Throwable ignored) {}

            final ServerSocket acceptSock = ss;
            Thread acceptThread = new Thread(() -> {
                while (sRunning && !acceptSock.isClosed()) {
                    try {
                        final Socket client = acceptSock.accept();
                        ExecutorService pool = sClientPool;
                        if (pool != null && !pool.isShutdown()) {
                            pool.execute(() -> handleClient(client));
                        } else {
                            try { client.close(); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {
                        if (!sRunning) break;
                    }
                }
            }, "FusionWifiAccept");
            acceptThread.setDaemon(true);
            acceptThread.start();

            Log.i(TAG, "Wi-Fi Remote Control started on http://" + getLocalLanIp() + ":" + sPort);
            return getStatusJson();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start Wi-Fi Remote Control: " + t.getMessage());
            stop();
            JSONObject err = new JSONObject();
            try {
                err.put("status", "error");
                err.put("running", false);
                err.put("error", t.getMessage() != null ? t.getMessage() : "Start failed");
            } catch (Exception ignored) {}
            return err;
        }
    }

    public static synchronized void stop() {
        sRunning = false;
        sPort = -1;
        if (sServerSocket != null) {
            try { sServerSocket.close(); } catch (Throwable ignored) {}
            sServerSocket = null;
        }
        if (sClientPool != null) {
            try { sClientPool.shutdownNow(); } catch (Throwable ignored) {}
            sClientPool = null;
        }
    }

    public static JSONObject getStatusJson() {
        JSONObject obj = new JSONObject();
        try {
            String ip = getLocalLanIp();
            obj.put("status", "ok");
            obj.put("running", isRunning());
            obj.put("ip", ip);
            obj.put("port", sPort);
            obj.put("url", isRunning() ? ("http://" + ip + ":" + sPort) : "");
            obj.put("cdpLocalPort", FusionCdpBridge.getBoundPort());
            obj.put("cdpLanPort", FusionCdpBridge.getLanBoundPort());
        } catch (Exception ignored) {}
        return obj;
    }

    public static String getLocalLanIp() {
        try {
            String fallback = null;
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces != null && ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                String name = iface.getName() != null ? iface.getName().toLowerCase() : "";
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String hostAddr = addr.getHostAddress();
                        if (name.startsWith("wlan") || name.startsWith("ap") || name.startsWith("eth")) {
                            return hostAddr;
                        }
                        if (fallback == null) fallback = hostAddr;
                    }
                }
            }
            if (fallback != null) return fallback;
        } catch (Throwable ignored) {}
        return "127.0.0.1";
    }

    private static void writeEndpointFiles(String ip, int port) {
        try {
            JSONObject ep = new JSONObject();
            ep.put("ip", ip);
            ep.put("port", port);
            ep.put("url", "http://" + ip + ":" + port);
            String raw = ep.toString(2);
            String[] paths = new String[] {
                "/data/data/com.termux/files/home/.fusion/wifi_endpoint.json",
                "/root/.fusion/wifi_endpoint.json"
            };
            for (String p : paths) {
                try {
                    File f = new File(p);
                    if (f.getParentFile() != null && !f.getParentFile().exists()) {
                        f.getParentFile().mkdirs();
                    }
                    if (f.getParentFile() != null && f.getParentFile().exists()) {
                        try (FileOutputStream fos = new FileOutputStream(f)) {
                            fos.write(raw.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private static void handleClient(Socket client) {
        try {
            client.setSoTimeout(15000);
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            byte[] rawHeaders = readHeaders(in);
            if (rawHeaders == null || rawHeaders.length == 0) return;
            String headerStr = new String(rawHeaders, StandardCharsets.ISO_8859_1);
            String[] lines = headerStr.split("\r\n");
            if (lines.length == 0) return;

            String[] reqParts = lines[0].split(" ");
            if (reqParts.length < 2) return;
            String method = reqParts[0].trim().toUpperCase();
            String fullTarget = reqParts[1].trim();

            String path = fullTarget;
            String query = "";
            int qIdx = fullTarget.indexOf('?');
            if (qIdx >= 0) {
                path = fullTarget.substring(0, qIdx);
                query = fullTarget.substring(qIdx + 1);
            }
            Map<String, String> queryParams = parseQuery(query);
            int contentLength = 0;
            for (int i = 1; i < lines.length; i++) {
                int col = lines[i].indexOf(':');
                if (col > 0) {
                    String k = lines[i].substring(0, col).trim().toLowerCase();
                    String v = lines[i].substring(col + 1).trim();
                    if ("content-length".equals(k)) {
                        try { contentLength = Math.min(4 * 1024 * 1024, Math.max(0, Integer.parseInt(v))); } catch (Exception ignored) {}
                    }
                }
            }

            if ("OPTIONS".equals(method)) {
                sendCorsPreflight(out);
                return;
            }

            if ("/ping".equals(path)) {
                sendJson(out, 200, "{\"ok\":true,\"service\":\"fusion-webview-wifi\"}");
                return;
            }

            byte[] bodyBytes = new byte[0];
            if (contentLength > 0) {
                bodyBytes = readFixedBytes(in, contentLength);
            }
            String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);

            if ("GET".equals(method) && "/".equals(path)) {
                sendHtml(out, 200, buildRemoteConsoleHtml());
                return;
            }

            if ("GET".equals(method) && "/tabs".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                JSONObject res = new JSONObject();
                res.put("ok", true);
                res.put("ip", getLocalLanIp());
                res.put("port", sPort);
                res.put("cdpLocalPort", FusionCdpBridge.getBoundPort());
                res.put("cdpLanPort", FusionCdpBridge.getLanBoundPort());
                res.put("tabs", mgr != null ? new JSONArray(mgr.getTabsJson()) : new JSONArray());
                sendJson(out, 200, res.toString());
                return;
            }

            if ("GET".equals(method) && "/screenshot".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                int tabId = -1;
                if (queryParams.containsKey("tab")) {
                    try { tabId = Integer.parseInt(queryParams.get("tab")); } catch (Exception ignored) {}
                }
                File tmp = File.createTempFile("fusion_wifi_shot_", ".png");
                try {
                    String saved = mgr.captureTabScreenshot(tabId, tmp.getAbsolutePath());
                    if (saved == null || !tmp.exists() || tmp.length() == 0) {
                        sendJson(out, 404, "{\"ok\":false,\"error\":\"Tab not found or empty screenshot\"}");
                        return;
                    }
                    byte[] png = readAllFile(tmp);
                    sendBytes(out, 200, "image/png", png);
                } finally {
                    try { tmp.delete(); } catch (Throwable ignored) {}
                }
                return;
            }

            if ("POST".equals(method) && "/ipc".equals(path)) {
                String cmd = bodyStr.trim();
                if (cmd.startsWith("{")) {
                    JSONObject jo = new JSONObject(cmd);
                    cmd = jo.optString("cmd", jo.optString("command", ""));
                }
                if (cmd.isEmpty()) {
                    sendJson(out, 400, "{\"ok\":false,\"error\":\"cmd is required\"}");
                    return;
                }
                String result = TermuxIpcServer.processCommand(cmd);
                JSONObject res = new JSONObject();
                res.put("ok", true);
                res.put("cmd", cmd);
                res.put("result", result != null ? result.trim() : "");
                sendJson(out, 200, res.toString());
                return;
            }

            if ("POST".equals(method) && "/eval".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                JSONObject jo = new JSONObject(bodyStr);
                String js = jo.optString("js", "");
                int tabId = jo.optInt("tabId", -1);
                String evalOut = (tabId >= 0) ? mgr.evaluateJsOnTabSync(tabId, js, 5000) : mgr.evaluateJsSync(js, 5000);
                JSONObject res = new JSONObject();
                res.put("ok", true);
                res.put("result", evalOut != null ? evalOut : JSONObject.NULL);
                sendJson(out, 200, res.toString());
                return;
            }

            if ("POST".equals(method) && "/touch".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                JSONObject jo = new JSONObject(bodyStr);
                int tabId = jo.optInt("tabId", -1);
                float x = (float) jo.optDouble("x", 0);
                float y = (float) jo.optDouble("y", 0);
                boolean ok = (tabId >= 0) ? mgr.dispatchNativeTouchOnTab(tabId, x, y) : mgr.dispatchNativeTouch(x, y);
                sendJson(out, ok ? 200 : 404, "{\"ok\":" + ok + "}");
                return;
            }

            if ("POST".equals(method) && "/swipe".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                JSONObject jo = new JSONObject(bodyStr);
                int tabId = jo.optInt("tabId", -1);
                float x1 = (float) jo.optDouble("x1", 540);
                float y1 = (float) jo.optDouble("y1", 1500);
                float x2 = (float) jo.optDouble("x2", 540);
                float y2 = (float) jo.optDouble("y2", 600);
                int dur = jo.optInt("durationMs", 300);
                boolean ok = (tabId >= 0) ? mgr.dispatchNativeSwipeOnTab(tabId, x1, y1, x2, y2, dur) : mgr.dispatchNativeSwipe(x1, y1, x2, y2, dur);
                sendJson(out, ok ? 200 : 404, "{\"ok\":" + ok + "}");
                return;
            }

            if ("POST".equals(method) && "/nav".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                JSONObject jo = new JSONObject(bodyStr);
                int tabId = jo.optInt("tabId", -1);
                String action = jo.optString("action", "reload");
                String url = jo.optString("url", "");
                boolean ok = mgr.navigateTabAction(tabId, action, url);
                sendJson(out, ok ? 200 : 400, "{\"ok\":" + ok + ",\"action\":" + JSONObject.quote(action) + "}");
                return;
            }

            if ("POST".equals(method) && "/type".equals(path)) {
                FusionWebViewManager mgr = FusionWebViewManager.getInstance();
                if (mgr == null) {
                    sendJson(out, 503, "{\"ok\":false,\"error\":\"WebViewManager not ready\"}");
                    return;
                }
                JSONObject jo = new JSONObject(bodyStr);
                int tabId = jo.optInt("tabId", -1);
                String text = jo.optString("text", "");
                boolean submit = jo.optBoolean("submit", false);
                String js = "(function(v,sub){try{var el=document.activeElement;if(!el||(!('value' in el)&&!el.isContentEditable)){el=document.querySelector('input:not([type=hidden]),textarea,[contenteditable=true]');if(el)el.focus();}if(!el)return 'no-input';if(el.isContentEditable){el.textContent=v;el.dispatchEvent(new Event('input',{bubbles:true}));}else{var p=el.tagName==='TEXTAREA'?HTMLTextAreaElement.prototype:HTMLInputElement.prototype;var d=Object.getOwnPropertyDescriptor(p,'value');if(d&&d.set)d.set.call(el,v);else el.value=v;el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));}if(sub){el.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',keyCode:13,bubbles:true}));if(el.form&&typeof el.form.requestSubmit==='function')el.form.requestSubmit();}return 'ok';}catch(e){return 'err:'+e.message;}})(" + JSONObject.quote(text) + "," + submit + ")";
                String evalOut = (tabId >= 0) ? mgr.evaluateJsOnTabSync(tabId, js, 5000) : mgr.evaluateJsSync(js, 5000);
                JSONObject res = new JSONObject();
                res.put("ok", true);
                res.put("result", evalOut != null ? evalOut : JSONObject.NULL);
                sendJson(out, 200, res.toString());
                return;
            }

            if ("GET".equals(method) && "/file".equals(path)) {
                String reqPath = queryParams.get("path");
                if (reqPath == null || reqPath.trim().isEmpty()) {
                    sendJson(out, 400, "{\"ok\":false,\"error\":\"path query is required\"}");
                    return;
                }
                File f = new File(reqPath.trim());
                if (!f.exists() || !f.isFile() || !f.canRead()) {
                    sendJson(out, 404, "{\"ok\":false,\"error\":\"File not found\"}");
                    return;
                }
                byte[] data = readAllFile(f);
                sendBytes(out, 200, "application/octet-stream", data);
                return;
            }

            if ("POST".equals(method) && "/file".equals(path)) {
                String scope = queryParams.containsKey("scope") ? queryParams.get("scope").trim().toLowerCase() : "uploads";
                if (!"uploads".equals(scope) && !"engine".equals(scope) && !"scripts".equals(scope) && !"sessions".equals(scope)) {
                    scope = "uploads";
                }
                String rawName = queryParams.containsKey("name") ? queryParams.get("name").trim() : ("upload_" + System.currentTimeMillis() + ".bin");
                String safeName = new File(rawName).getName();
                if (safeName.isEmpty() || ".".equals(safeName) || "..".equals(safeName)) {
                    safeName = "file_" + System.currentTimeMillis() + ".bin";
                }
                File primary = new File("/data/data/com.termux/files/home/.fusion/" + scope, safeName);
                if (primary.getParentFile() != null && !primary.getParentFile().exists()) {
                    primary.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(primary)) {
                    fos.write(bodyBytes);
                    fos.flush();
                }
                String mirrorPath = null;
                try {
                    File rootMirror = new File("/root/.fusion/" + scope, safeName);
                    if (rootMirror.getParentFile() != null && !rootMirror.getParentFile().exists()) {
                        rootMirror.getParentFile().mkdirs();
                    }
                    if (rootMirror.getParentFile() != null && rootMirror.getParentFile().exists()) {
                        try (FileOutputStream fos2 = new FileOutputStream(rootMirror)) {
                            fos2.write(bodyBytes);
                            fos2.flush();
                        }
                        mirrorPath = rootMirror.getAbsolutePath();
                    }
                } catch (Throwable ignored) {}
                JSONObject res = new JSONObject();
                res.put("ok", true);
                res.put("scope", scope);
                res.put("name", safeName);
                res.put("path", primary.getAbsolutePath());
                res.put("size", bodyBytes.length);
                if (mirrorPath != null) res.put("mirror", mirrorPath);
                sendJson(out, 200, res.toString());
                return;
            }

            sendJson(out, 404, "{\"ok\":false,\"error\":\"Not found\"}");
        } catch (Throwable t) {
            try {
                sendJson(client.getOutputStream(), 500, "{\"ok\":false,\"error\":" + JSONObject.quote(String.valueOf(t.getMessage())) + "}");
            } catch (Throwable ignored) {}
        } finally {
            try { client.close(); } catch (Throwable ignored) {}
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                try {
                    String k = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                    String v = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                    map.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static byte[] readHeaders(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int p3 = 0, p2 = 0, p1 = 0, cur;
            while (baos.size() < 32768 && (cur = in.read()) != -1) {
                baos.write(cur);
                if (p3 == '\r' && p2 == '\n' && p1 == '\r' && cur == '\n') {
                    return baos.toByteArray();
                }
                p3 = p2; p2 = p1; p1 = cur;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static byte[] readFixedBytes(InputStream in, int len) throws java.io.IOException {
        byte[] buf = new byte[len];
        int offset = 0;
        while (offset < len) {
            int n = in.read(buf, offset, len - offset);
            if (n <= 0) break;
            offset += n;
        }
        if (offset == len) return buf;
        byte[] trimmed = new byte[offset];
        System.arraycopy(buf, 0, trimmed, 0, offset);
        return trimmed;
    }

    private static byte[] readAllFile(File f) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try (FileInputStream fis = new FileInputStream(f)) {
            int n;
            while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    private static void sendCorsPreflight(OutputStream out) throws java.io.IOException {
        String resp = "HTTP/1.1 204 No Content\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type\r\n" +
                "Connection: close\r\n\r\n";
        out.write(resp.getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
    }

    private static void sendJson(OutputStream out, int code, String json) throws java.io.IOException {
        sendBytes(out, code, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendHtml(OutputStream out, int code, String html) throws java.io.IOException {
        sendBytes(out, code, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(OutputStream out, int code, String contentType, byte[] data) throws java.io.IOException {
        String status = (code == 200) ? "OK" : (code == 404 ? "Not Found" : "Error");
        String hdr = "HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Cache-Control: no-store\r\n" +
                "Connection: close\r\n\r\n";
        out.write(hdr.getBytes(StandardCharsets.ISO_8859_1));
        out.write(data);
        out.flush();
    }

    private static String buildRemoteConsoleHtml() {
        try {
            File ext = FusionProfileManager.resolveExternalEngineFile("wifi_console.html");
            if (ext != null && ext.exists() && ext.isFile() && ext.canRead()) {
                return new String(readAllFile(ext), StandardCharsets.UTF_8);
            }
        } catch (Throwable ignored) {}
        return "<!doctype html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>Fusion Wi-Fi Remote Console</title>" +
                "<style>body{margin:0;font-family:system-ui,sans-serif;background:#1e1e2e;color:#cdd6f4;padding:14px}" +
                ".row{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px}input,button,select{background:#313244;color:#cdd6f4;border:1px solid #45475a;border-radius:6px;padding:8px 10px}" +
                "button{cursor:pointer;background:#89b4fa;color:#11111b;font-weight:600;border:none}button.sec{background:#45475a;color:#cdd6f4}" +
                ".grid{display:grid;grid-template-columns:1fr 340px;gap:12px}@media(max-width:760px){.grid{grid-template-columns:1fr}}" +
                ".card{background:#181825;border:1px solid #313244;border-radius:10px;padding:12px}pre{white-space:pre-wrap;word-break:break-all;font-size:12px;max-height:220px;overflow:auto;background:#11111b;padding:8px;border-radius:6px}" +
                "img{width:100%;border-radius:8px;border:1px solid #45475a;cursor:crosshair}</style></head><body>" +
                "<h3>Fusion WebView Wi-Fi Remote</h3>" +
                "<div class='row'><input id='url' style='flex:1' placeholder='https://...'/><button onclick='openUrl(false)'>Open Tab</button><button class='sec' onclick='openUrl(true)'>Headless</button><button class='sec' onclick='refreshTabs()'>Refresh</button></div>" +
                "<div class='grid'><div class='card'><div class='row'><select id='tabSel' style='flex:1' onchange='refreshShot()'></select><button class='sec' onclick=\"navAct('back')\">Back</button><button class='sec' onclick=\"navAct('reload')\">Reload</button><button class='sec' onclick='killSel()'>Kill Tab</button></div>" +
                "<div class='row'><input id='txt' style='flex:1' placeholder='Ketik ke input aktif...'/><button onclick='sendType(false)'>Type</button><button class='sec' onclick='sendType(true)'>Type+Enter</button></div>" +
                "<div class='row'><input id='ipc' style='flex:1' placeholder='IPC command / JS...'/><button onclick='runIpc()'>Send IPC</button><button class='sec' onclick='runEval()'>Eval JS</button><button class='sec' onclick='swipeDir(1400,600)'>Scroll Down</button><button class='sec' onclick='swipeDir(600,1400)'>Scroll Up</button></div>" +
                "<pre id='out'>Ready.</pre></div>" +
                "<div class='card'><div style='font-size:12px;margin-bottom:6px'>Live Screen (klik gambar untuk native tap):</div><img id='shot' onclick='tapShot(event)'/></div></div>" +
                "<script>" +
                "async function api(p,o){const r=await fetch(p,o);return r.json();}" +
                "async function refreshTabs(){const d=await api('/tabs');const s=document.getElementById('tabSel');const cur=s.value;s.innerHTML='';" +
                "(d.tabs||[]).forEach(t=>{const o=document.createElement('option');o.value=t.id;o.textContent='#'+t.id+' ['+t.profile+'] '+(t.isHeadless?'(H) ':'')+t.url;s.appendChild(o);});" +
                "if(cur)s.value=cur;document.getElementById('out').textContent=JSON.stringify(d,null,2);refreshShot();}" +
                "function refreshShot(){const id=document.getElementById('tabSel').value||'-1';document.getElementById('shot').src='/screenshot?tab='+encodeURIComponent(id)+'&t='+Date.now();}" +
                "async function openUrl(h){const u=document.getElementById('url').value.trim();if(!u)return;const cmd=(h?'WEBVIEW_HEADLESS ':'WEBVIEW_NEW_TAB ')+u;const r=await api('/ipc',{method:'POST',body:JSON.stringify({cmd})});document.getElementById('out').textContent=JSON.stringify(r,null,2);setTimeout(refreshTabs,800);}" +
                "async function navAct(a){const tabId=parseInt(document.getElementById('tabSel').value||'-1',10);const r=await api('/nav',{method:'POST',body:JSON.stringify({tabId,action:a})});document.getElementById('out').textContent=JSON.stringify(r,null,2);setTimeout(refreshTabs,600);}" +
                "async function sendType(sub){const text=document.getElementById('txt').value;const tabId=parseInt(document.getElementById('tabSel').value||'-1',10);const r=await api('/type',{method:'POST',body:JSON.stringify({tabId,text,submit:sub})});document.getElementById('out').textContent=JSON.stringify(r,null,2);setTimeout(refreshShot,400);}" +
                "async function swipeDir(y1,y2){const tabId=parseInt(document.getElementById('tabSel').value||'-1',10);await api('/swipe',{method:'POST',body:JSON.stringify({tabId,x1:540,y1,x2:540,y2,durationMs:300})});setTimeout(refreshShot,500);}" +
                "async function killSel(){const id=document.getElementById('tabSel').value;if(!id)return;const r=await api('/ipc',{method:'POST',body:JSON.stringify({cmd:'WEBVIEW_KILL_TAB_ID '+id})});document.getElementById('out').textContent=JSON.stringify(r,null,2);setTimeout(refreshTabs,400);}" +
                "async function runIpc(){const cmd=document.getElementById('ipc').value.trim();if(!cmd)return;const r=await api('/ipc',{method:'POST',body:JSON.stringify({cmd})});document.getElementById('out').textContent=JSON.stringify(r,null,2);setTimeout(refreshTabs,400);}" +
                "async function runEval(){const js=document.getElementById('ipc').value.trim();const tabId=parseInt(document.getElementById('tabSel').value||'-1',10);const r=await api('/eval',{method:'POST',body:JSON.stringify({tabId,js})});document.getElementById('out').textContent=JSON.stringify(r,null,2);}" +
                "async function tapShot(e){const img=e.target;const r=img.getBoundingClientRect();const rx=img.naturalWidth?(e.offsetX/r.width)*img.naturalWidth:e.offsetX;const ry=img.naturalHeight?(e.offsetY/r.height)*img.naturalHeight:e.offsetY;const tabId=parseInt(document.getElementById('tabSel').value||'-1',10);await api('/touch',{method:'POST',body:JSON.stringify({tabId,x:Math.round(rx),y:Math.round(ry)})});setTimeout(refreshShot,500);}" +
                "refreshTabs();</script></body></html>";
    }
}
