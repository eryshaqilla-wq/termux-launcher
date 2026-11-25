package com.termux.webview;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local HTTP/CONNECT-to-SOCKS5 tunnel supporting RFC 1929 username/password authentication.
 * Supports multiple concurrent tunnels keyed by upstream host:port:user so multi-worker tabs never stomp each other.
 */
public class FusionSocksTunnel {
    private static final String TAG = "FusionSocksTunnel";

    private static final class TunnelEntry {
        volatile boolean running = true;
        final int localPort;
        final ServerSocket serverSocket;

        TunnelEntry(int localPort, ServerSocket serverSocket) {
            this.localPort = localPort;
            this.serverSocket = serverSocket;
        }
    }

    private static final ConcurrentHashMap<String, TunnelEntry> sTunnels = new ConcurrentHashMap<>();

    private static String buildKey(String host, int port, String user, String pass) {
        return (host != null ? host.trim() : "") + ":" + port + ":" + (user != null ? user : "") + ":" + (pass != null ? pass : "");
    }

    public static synchronized int ensureTunnel(final String host, final int port, final String user, final String pass) {
        String key = buildKey(host, port, user, pass);
        TunnelEntry existing = sTunnels.get(key);
        if (existing != null && existing.running && existing.serverSocket != null && !existing.serverSocket.isClosed()) {
            return existing.localPort;
        }
        if (existing != null) {
            stopEntry(key, existing);
        }

        try {
            final ServerSocket ss = new ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"));
            final int localPort = ss.getLocalPort();
            final TunnelEntry entry = new TunnelEntry(localPort, ss);
            sTunnels.put(key, entry);

            Thread t = new Thread(() -> {
                while (entry.running && !ss.isClosed()) {
                    try {
                        final Socket client = ss.accept();
                        Thread worker = new Thread(() -> handleClient(client, host, port, user, pass), "SocksTunnelWorker");
                        worker.setDaemon(true);
                        worker.start();
                    } catch (Throwable ignored) {
                        if (!entry.running) break;
                    }
                }
            }, "SocksTunnelAccept-" + localPort);
            t.setDaemon(true);
            t.start();

            Log.i(TAG, "SOCKS5 auth tunnel active on 127.0.0.1:" + localPort + " -> " + host + ":" + port);
            return localPort;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start SOCKS5 tunnel: " + t.getMessage());
            return -1;
        }
    }

    private static void stopEntry(String key, TunnelEntry entry) {
        if (entry == null) return;
        entry.running = false;
        if (entry.serverSocket != null) {
            try { entry.serverSocket.close(); } catch (Throwable ignored) {}
        }
        if (key != null) sTunnels.remove(key);
    }

    public static synchronized void stop() {
        for (Map.Entry<String, TunnelEntry> e : sTunnels.entrySet()) {
            stopEntry(e.getKey(), e.getValue());
        }
        sTunnels.clear();
    }

    private static void handleClient(Socket client, String socksHost, int socksPort, String user, String pass) {
        Socket upstream = null;
        try {
            client.setSoTimeout(15000);
            InputStream clientIn = client.getInputStream();
            OutputStream clientOut = client.getOutputStream();

            byte[] headerBytes = readHttpHeader(clientIn);
            if (headerBytes == null || headerBytes.length == 0) {
                return;
            }
            String headerText = new String(headerBytes, StandardCharsets.ISO_8859_1);
            int firstEol = headerText.indexOf("\r\n");
            if (firstEol <= 0) return;
            String requestLine = headerText.substring(0, firstEol);
            String[] parts = requestLine.split(" ");
            if (parts.length < 3) return;

            String method = parts[0].trim().toUpperCase();
            String target = parts[1].trim();
            String targetHost;
            int targetPort;
            boolean isConnect = "CONNECT".equals(method);

            if (isConnect) {
                int colon = target.lastIndexOf(':');
                if (colon > 0) {
                    targetHost = target.substring(0, colon);
                    targetPort = Integer.parseInt(target.substring(colon + 1));
                } else {
                    targetHost = target;
                    targetPort = 443;
                }
            } else {
                URI uri = URI.create(target);
                targetHost = uri.getHost();
                targetPort = (uri.getPort() > 0) ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
                if (targetHost == null || targetHost.isEmpty()) return;
            }

            upstream = new Socket();
            upstream.connect(new InetSocketAddress(socksHost, socksPort), 10000);
            upstream.setSoTimeout(0);
            client.setSoTimeout(0);

            InputStream upIn = upstream.getInputStream();
            OutputStream upOut = upstream.getOutputStream();

            boolean hasAuth = user != null && !user.isEmpty() && pass != null;
            if (hasAuth) {
                upOut.write(new byte[]{0x05, 0x02, 0x00, 0x02});
            } else {
                upOut.write(new byte[]{0x05, 0x01, 0x00});
            }
            upOut.flush();

            int ver = upIn.read();
            int chosenMethod = upIn.read();
            if (ver != 0x05 || chosenMethod == 0xFF) {
                sendBadGateway(clientOut);
                return;
            }

            if (chosenMethod == 0x02) {
                byte[] uBytes = (user != null ? user : "").getBytes(StandardCharsets.UTF_8);
                byte[] pBytes = (pass != null ? pass : "").getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream authPkt = new ByteArrayOutputStream();
                authPkt.write(0x01);
                authPkt.write(uBytes.length & 0xFF);
                authPkt.write(uBytes);
                authPkt.write(pBytes.length & 0xFF);
                authPkt.write(pBytes);
                upOut.write(authPkt.toByteArray());
                upOut.flush();

                int authVer = upIn.read();
                int authStatus = upIn.read();
                if (authVer != 0x01 || authStatus != 0x00) {
                    sendBadGateway(clientOut);
                    return;
                }
            }

            byte[] hostBytes = targetHost.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream reqPkt = new ByteArrayOutputStream();
            reqPkt.write(0x05);
            reqPkt.write(0x01); // CONNECT
            reqPkt.write(0x00); // RSV
            reqPkt.write(0x03); // DOMAINNAME
            reqPkt.write(hostBytes.length & 0xFF);
            reqPkt.write(hostBytes);
            reqPkt.write((targetPort >> 8) & 0xFF);
            reqPkt.write(targetPort & 0xFF);
            upOut.write(reqPkt.toByteArray());
            upOut.flush();

            int rVer = upIn.read();
            int rRep = upIn.read();
            upIn.read(); // RSV
            int atyp = upIn.read();
            if (rVer != 0x05 || rRep != 0x00) {
                sendBadGateway(clientOut);
                return;
            }
            if (atyp == 0x01) {
                skipExact(upIn, 4 + 2);
            } else if (atyp == 0x03) {
                int len = upIn.read();
                if (len > 0) skipExact(upIn, len + 2);
            } else if (atyp == 0x04) {
                skipExact(upIn, 16 + 2);
            }

            if (isConnect) {
                clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
                clientOut.flush();
            } else {
                URI uri = URI.create(target);
                String path = (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) ? uri.getRawPath() : "/";
                if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();
                String rebuilt = method + " " + path + " " + parts[2] + headerText.substring(firstEol).replaceAll("(?i)Proxy-Connection:[^\r\n]*\r\n", "");
                upOut.write(rebuilt.getBytes(StandardCharsets.ISO_8859_1));
                upOut.flush();
            }

            final Socket finalUp = upstream;
            Thread up = new Thread(() -> pipe(clientIn, upOut, client, finalUp), "SocksPipeUp");
            up.setDaemon(true);
            up.start();

            pipe(upIn, clientOut, client, finalUp);
        } catch (Throwable ignored) {
        } finally {
            try { client.close(); } catch (Throwable ignored) {}
            if (upstream != null) {
                try { upstream.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private static byte[] readHttpHeader(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int prev3 = 0, prev2 = 0, prev1 = 0, cur;
            while (baos.size() < 16384 && (cur = in.read()) != -1) {
                baos.write(cur);
                if (prev3 == '\r' && prev2 == '\n' && prev1 == '\r' && cur == '\n') {
                    return baos.toByteArray();
                }
                prev3 = prev2;
                prev2 = prev1;
                prev1 = cur;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static void skipExact(InputStream in, int count) throws java.io.IOException {
        for (int i = 0; i < count; i++) {
            if (in.read() == -1) break;
        }
    }

    private static void sendBadGateway(OutputStream out) {
        try {
            out.write("HTTP/1.1 502 Bad Gateway\r\nContent-Length: 0\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
        } catch (Throwable ignored) {}
    }

    private static void pipe(InputStream in, OutputStream out, Socket a, Socket b) {
        byte[] buf = new byte[16384];
        try {
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (Throwable ignored) {
        } finally {
            try { a.close(); } catch (Throwable ignored) {}
            try { b.close(); } catch (Throwable ignored) {}
        }
    }
}
