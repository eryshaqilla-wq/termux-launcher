package com.termux.webview;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Native TCP-to-LocalSocket bridge for Chromium WebView CDP.
 * Allows Termux/Node/Playwright to connect to 127.0.0.1:9222 directly without ADB forwarding.
 */
public class FusionCdpBridge {
    private static final String TAG = "FusionCdpBridge";
    private static volatile boolean sRunning = false;
    private static volatile int sBoundPort = -1;
    private static volatile int sPid = -1;
    private static ServerSocket sServerSocket = null;

    private static volatile boolean sLanRunning = false;
    private static volatile int sLanBoundPort = -1;
    private static ServerSocket sLanServerSocket = null;

    public static boolean isRunning() {
        return sRunning && sBoundPort > 0;
    }

    public static int getBoundPort() {
        return sBoundPort;
    }

    public static boolean isLanRunning() {
        return sLanRunning && sLanBoundPort > 0;
    }

    public static int getLanBoundPort() {
        return sLanBoundPort;
    }

    public static synchronized int startLan(final int pid, final int preferredPort) {
        if (sLanRunning && sLanServerSocket != null && !sLanServerSocket.isClosed()) {
            return sLanBoundPort;
        }
        stopLan();
        try {
            int port = (preferredPort > 0 && preferredPort <= 65535) ? preferredPort : 9223;
            ServerSocket ss;
            try {
                ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new java.net.InetSocketAddress("0.0.0.0", port), 50);
            } catch (Throwable busy) {
                ss = new ServerSocket(0, 50, InetAddress.getByName("0.0.0.0"));
            }
            sLanServerSocket = ss;
            sLanBoundPort = ss.getLocalPort();
            sLanRunning = true;

            final ServerSocket acceptSocket = ss;
            final String socketName = "webview_devtools_remote_" + pid;
            final String lanIp = FusionWifiRemoteServer.getLocalLanIp();
            final int boundLanPort = sLanBoundPort;
            Thread acceptThread = new Thread(() -> {
                while (sLanRunning && !acceptSocket.isClosed()) {
                    try {
                        final Socket client = acceptSocket.accept();
                        Thread connThread = new Thread(() -> handleLanConnection(client, socketName, lanIp, boundLanPort), "FusionCdpLanConn");
                        connThread.setDaemon(true);
                        connThread.start();
                    } catch (Throwable ignored) {
                        if (!sLanRunning) break;
                    }
                }
            }, "FusionCdpLanAccept");
            acceptThread.setDaemon(true);
            acceptThread.start();
            Log.i(TAG, "LAN CDP bridge active on 0.0.0.0:" + sLanBoundPort + " -> @" + socketName);
            return sLanBoundPort;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start LAN CDP bridge: " + t.getMessage());
            stopLan();
            return -1;
        }
    }

    public static synchronized void stopLan() {
        sLanRunning = false;
        sLanBoundPort = -1;
        if (sLanServerSocket != null) {
            try { sLanServerSocket.close(); } catch (Throwable ignored) {}
            sLanServerSocket = null;
        }
    }

    public static synchronized int start(final int pid) {
        if (sRunning && sPid == pid && sServerSocket != null && !sServerSocket.isClosed()) {
            return sBoundPort;
        }
        stop();

        try {
            InetAddress lo = InetAddress.getByName("127.0.0.1");
            ServerSocket ss;
            try {
                ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new java.net.InetSocketAddress(lo, 9222), 50);
            } catch (Throwable portBusy) {
                ss = new ServerSocket(0, 50, lo);
            }
            sServerSocket = ss;
            sBoundPort = ss.getLocalPort();
            sPid = pid;
            sRunning = true;

            writePortMarkers(sBoundPort);

            final ServerSocket acceptSocket = ss;
            final String socketName = "webview_devtools_remote_" + pid;
            Thread acceptThread = new Thread(() -> {
                while (sRunning && !acceptSocket.isClosed()) {
                    try {
                        final Socket client = acceptSocket.accept();
                        Thread connThread = new Thread(() -> handleConnection(client, socketName), "FusionCdpConn");
                        connThread.setDaemon(true);
                        connThread.start();
                    } catch (Throwable ignored) {
                        if (!sRunning) break;
                    }
                }
            }, "FusionCdpAccept");
            acceptThread.setDaemon(true);
            acceptThread.start();

            Log.i(TAG, "CDP bridge active on 127.0.0.1:" + sBoundPort + " -> @" + socketName);
            return sBoundPort;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start CDP bridge: " + t.getMessage());
            stop();
            return -1;
        }
    }

    public static synchronized void stop() {
        sRunning = false;
        sBoundPort = -1;
        sPid = -1;
        if (sServerSocket != null) {
            try { sServerSocket.close(); } catch (Throwable ignored) {}
            sServerSocket = null;
        }
        deletePortMarkers();
    }

    private static void handleConnection(final Socket tcpClient, final String abstractName) {
        LocalSocket localSocket = null;
        try {
            tcpClient.setTcpNoDelay(true);
            localSocket = new LocalSocket();
            localSocket.connect(new LocalSocketAddress(abstractName, LocalSocketAddress.Namespace.ABSTRACT));

            final InputStream tcpIn = tcpClient.getInputStream();
            final OutputStream tcpOut = tcpClient.getOutputStream();
            final InputStream unixIn = localSocket.getInputStream();
            final OutputStream unixOut = localSocket.getOutputStream();
            final LocalSocket finalUnix = localSocket;

            Thread up = new Thread(() -> pipe(tcpIn, unixOut, tcpClient, finalUnix), "CdpUp");
            up.setDaemon(true);
            up.start();

            pipe(unixIn, tcpOut, tcpClient, finalUnix);
        } catch (Throwable ignored) {
        } finally {
            try { tcpClient.close(); } catch (Throwable ignored) {}
            if (localSocket != null) {
                try { localSocket.close(); } catch (Throwable ignored) {}
            }
        }
    }

    /**
     * Menangani koneksi CDP dari jaringan Wi-Fi LAN (Infinix <-> Poco):
     * Menulis ulang Host header menjadi localhost:9222 agar DevTools Chromium mengizinkan akses,
     * serta me-rewrite ws://localhost/... menjadi ws://<lanIp>:<lanPort>/... pada response /json*.
     */
    private static void handleLanConnection(final Socket tcpClient, final String abstractName, final String lanIp, final int lanPort) {
        LocalSocket localSocket = null;
        try {
            tcpClient.setTcpNoDelay(true);
            tcpClient.setSoTimeout(15000);
            final InputStream tcpIn = tcpClient.getInputStream();
            final OutputStream tcpOut = tcpClient.getOutputStream();

            java.io.ByteArrayOutputStream reqHdrBuf = new java.io.ByteArrayOutputStream();
            int p3 = 0, p2 = 0, p1 = 0, cur;
            while (reqHdrBuf.size() < 32768 && (cur = tcpIn.read()) != -1) {
                reqHdrBuf.write(cur);
                if (p3 == '\r' && p2 == '\n' && p1 == '\r' && cur == '\n') break;
                p3 = p2; p2 = p1; p1 = cur;
            }
            if (reqHdrBuf.size() == 0) return;

            String rawReq = new String(reqHdrBuf.toByteArray(), StandardCharsets.ISO_8859_1);
            boolean isWebSocket = rawReq.toLowerCase(java.util.Locale.ROOT).contains("upgrade: websocket");
            String rewrittenReq = rawReq
                    .replaceAll("(?im)^Host:[^\\r\\n]*\\r\\n", "Host: localhost:9222\r\n")
                    .replaceAll("(?im)^Origin:[^\\r\\n]*\\r\\n", "");

            localSocket = new LocalSocket();
            localSocket.connect(new LocalSocketAddress(abstractName, LocalSocketAddress.Namespace.ABSTRACT));
            final InputStream unixIn = localSocket.getInputStream();
            final OutputStream unixOut = localSocket.getOutputStream();
            unixOut.write(rewrittenReq.getBytes(StandardCharsets.ISO_8859_1));
            unixOut.flush();

            if (!isWebSocket) {
                java.io.ByteArrayOutputStream respBuf = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = unixIn.read(buf)) != -1) {
                    respBuf.write(buf, 0, n);
                    String soFar = new String(respBuf.toByteArray(), StandardCharsets.UTF_8);
                    int hdrEnd = soFar.indexOf("\r\n\r\n");
                    if (hdrEnd > 0) {
                        String hdrPart = soFar.substring(0, hdrEnd);
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?im)^Content-Length:\\s*(\\d+)").matcher(hdrPart);
                        if (m.find()) {
                            int expectedLen = Integer.parseInt(m.group(1));
                            int bodyOffset = hdrEnd + 4;
                            if (respBuf.size() >= bodyOffset + expectedLen) break;
                        }
                    }
                }
                String fullResp = new String(respBuf.toByteArray(), StandardCharsets.UTF_8);
                int hdrEnd = fullResp.indexOf("\r\n\r\n");
                if (hdrEnd > 0) {
                    String hdrPart = fullResp.substring(0, hdrEnd);
                    String bodyPart = fullResp.substring(hdrEnd + 4);
                    String wsTarget = "ws://" + lanIp + ":" + lanPort + "/";
                    String fixedBody = bodyPart
                            .replace("ws://localhost:9222/", wsTarget)
                            .replace("ws://127.0.0.1:9222/", wsTarget)
                            .replace("ws://localhost/", wsTarget)
                            .replace("ws://127.0.0.1/", wsTarget);
                    byte[] bodyBytes = fixedBody.getBytes(StandardCharsets.UTF_8);
                    String fixedHdr = hdrPart.replaceAll("(?im)^Content-Length:\\s*\\d+", "Content-Length: " + bodyBytes.length);
                    tcpOut.write((fixedHdr + "\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
                    tcpOut.write(bodyBytes);
                    tcpOut.flush();
                } else {
                    tcpOut.write(respBuf.toByteArray());
                    tcpOut.flush();
                }
                return;
            }

            tcpClient.setSoTimeout(0);
            final LocalSocket finalUnix = localSocket;
            Thread up = new Thread(() -> pipe(tcpIn, unixOut, tcpClient, finalUnix), "CdpLanUp");
            up.setDaemon(true);
            up.start();
            pipe(unixIn, tcpOut, tcpClient, finalUnix);
        } catch (Throwable ignored) {
        } finally {
            try { tcpClient.close(); } catch (Throwable ignored) {}
            if (localSocket != null) {
                try { localSocket.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private static void pipe(InputStream in, OutputStream out, Socket tcp, LocalSocket unix) {
        byte[] buf = new byte[16384];
        try {
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (Throwable ignored) {
        } finally {
            try { tcp.close(); } catch (Throwable ignored) {}
            try { unix.close(); } catch (Throwable ignored) {}
        }
    }

    private static void writePortMarkers(int port) {
        String val = String.valueOf(port);
        String[] paths = new String[] {
            "/data/data/com.termux/files/home/.webview_cdp_port",
            "/data/local/nhsystem/kali-arm64/root/.webview_cdp_port",
            "/root/.webview_cdp_port"
        };
        for (String p : paths) {
            try {
                File f = new File(p);
                if (f.getParentFile() != null && f.getParentFile().exists()) {
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(val.getBytes(StandardCharsets.UTF_8));
                    }
                    try { f.setReadable(true, false); } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }
    }

    private static void deletePortMarkers() {
        String[] paths = new String[] {
            "/data/data/com.termux/files/home/.webview_cdp_port",
            "/data/local/nhsystem/kali-arm64/root/.webview_cdp_port",
            "/root/.webview_cdp_port"
        };
        for (String p : paths) {
            try { new File(p).delete(); } catch (Throwable ignored) {}
        }
    }
}
