#!/usr/bin/env node

/**
 * ESO WEBVIEW LIVE MONITOR & REMOTE CONTROL (WLAN 30-60 FPS)
 * Memantau dan MENGAMBIL ALIH (Remote Control) WebView Termux secara real-time via Web/WLAN.
 * Mendukung Watch Mode (Read-Only) dan Control Mode (Klik, Drag, Swipe, Scroll, Ketik Langsung)
 * Arsitektur Scrcpy-Style: Zero-Lag Binary WebSocket Screencast Stream (0% Idle CPU).
 * UI Modern Catppuccin Mocha dengan Fullscreen Mode & Virtual Scrcpy Navigation Bar.
 */

const http = require('http');
const url = require('url');
const os = require('os');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

let WebSocket;
try {
    WebSocket = require('ws');
} catch (e) {
    try {
        WebSocket = require('/root/node_modules/ws');
    } catch (_) {}
}

const args = process.argv.slice(2);
let PORT = 9223;
let DEFAULT_CONTROL = false;

for (let i = 0; i < args.length; i++) {
    const a = args[i];
    if (/^\d+$/.test(a)) {
        PORT = parseInt(a, 10);
    } else if (a === '--control' || a === '--monitor-control' || a === 'control' || a === '-c') {
        DEFAULT_CONTROL = true;
    }
}

if (process.env.MONITOR_PORT) PORT = parseInt(process.env.MONITOR_PORT, 10);
if (process.env.MONITOR_CONTROL === '1') DEFAULT_CONTROL = true;

const CDP_HTTP = 'http://127.0.0.1:9222';

// 1. Pastikan ADB Forward ke devtools socket aktif
function ensurePortForward() {
    try {
        const unixOut = execSync("adb -s 127.0.0.1:5555 shell 'cat /proc/net/unix | grep devtools' 2>/dev/null", { timeout: 2000 }).toString();
        const match = unixOut.match(/@(webview_devtools_remote_\d+)/);
        if (match) {
            execSync(`adb -s 127.0.0.1:5555 forward tcp:9222 localabstract:${match[1]}`, { timeout: 2000 });
            return true;
        }
    } catch (_) {}
    return false;
}

// 2. Deteksi semua IP LAN/WLAN lokal
function getNetworkIps() {
    const interfaces = os.networkInterfaces();
    const ips = [];
    for (const name of Object.keys(interfaces)) {
        for (const net of interfaces[name]) {
            if (net.family === 'IPv4' && !net.internal) {
                ips.push({ iface: name, ip: net.address });
            }
        }
    }
    return ips;
}

// 3. Ambil daftar tab dari IPC & CDP
function fetchIpcTabs() {
    try {
        const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
        const out = execSync(`${ipcBin} webview list --json 2>/dev/null`, { timeout: 2500, encoding: 'utf8' }).trim();
        if (out && (out.startsWith('[') || out.startsWith('{'))) {
            const parsed = JSON.parse(out);
            return Array.isArray(parsed) ? parsed : [parsed];
        }
    } catch (_) {}
    return [];
}

function fetchCdpTabs() {
    return new Promise((resolve) => {
        const req = http.get(`${CDP_HTTP}/json`, { timeout: 1500 }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(data);
                    resolve(Array.isArray(parsed) ? parsed : []);
                } catch (_) {
                    resolve([]);
                }
            });
        });
        req.on('error', () => resolve([]));
        req.on('timeout', () => { req.destroy(); resolve([]); });
    });
}

async function getAllTabs() {
    ensurePortForward();
    const [ipcTabs, cdpTabs] = await Promise.all([
        fetchIpcTabs(),
        fetchCdpTabs()
    ]);

    const pageCdpTabs = cdpTabs.filter(t => t.type === 'page' || !t.type);

    if (ipcTabs.length > 0) {
        return ipcTabs.map((tab, idx) => {
            const matchedCdp = pageCdpTabs.find(c => c.url === tab.url)
                || pageCdpTabs.find(c => tab.url && c.url && (c.url.startsWith(tab.url) || tab.url.startsWith(c.url)))
                || pageCdpTabs.find(c => tab.title && c.title && tab.title.length > 2 && (c.title.includes(tab.title) || tab.title.includes(c.title)))
                || pageCdpTabs[idx]
                || pageCdpTabs[0];
            return {
                index: tab.index !== undefined ? tab.index : idx,
                id: tab.id || (matchedCdp ? matchedCdp.id : `tab_${idx}`),
                url: tab.url || (matchedCdp ? matchedCdp.url : 'about:blank'),
                title: tab.title || (matchedCdp ? matchedCdp.title : 'WebView Tab'),
                profile: tab.profile || 'default',
                isHeadless: Boolean(tab.isHeadless || tab.isMinimized),
                isDesktop: Boolean(tab.isDesktop),
                isGhost: Boolean(tab.isGhost),
                proxy: tab.proxy || 'direct',
                active: Boolean(tab.active),
                wsUrl: matchedCdp ? matchedCdp.webSocketDebuggerUrl : null
            };
        });
    }

    if (pageCdpTabs.length > 0) {
        return pageCdpTabs.map((t, idx) => ({
            index: idx,
            id: t.id,
            url: t.url || 'about:blank',
            title: t.title || 'Page',
            profile: 'active',
            isHeadless: false,
            isDesktop: false,
            isGhost: true,
            proxy: 'direct',
            active: idx === 0,
            wsUrl: t.webSocketDebuggerUrl
        }));
    }

    return [];
}

// 4. Capture screenshot via CDP WebSocket (Fallback snapshot)
function captureCdpScreenshot(wsUrl, quality = 75) {
    return new Promise((resolve, reject) => {
        if (!WebSocket) return reject(new Error('WebSocket module (ws) tidak tersedia'));
        if (!wsUrl) return reject(new Error('WebSocket Debugger URL tidak tersedia'));

        let ws;
        try {
            ws = new WebSocket(wsUrl);
        } catch (e) {
            return reject(e);
        }

        const timer = setTimeout(() => {
            try { ws.close(); } catch (_) {}
            reject(new Error('CDP Screenshot timeout (2.5s)'));
        }, 2500);

        ws.on('open', () => {
            ws.send(JSON.stringify({ id: 1, method: 'Page.enable' }));
            ws.send(JSON.stringify({
                id: 101,
                method: 'Page.captureScreenshot',
                params: {
                    format: 'jpeg',
                    quality: Math.min(Math.max(quality, 30), 90),
                    fromSurface: false,
                    captureBeyondViewport: true
                }
            }));
        });

        ws.on('message', (msg) => {
            try {
                const res = JSON.parse(msg.toString());
                if (res.id === 101) {
                    clearTimeout(timer);
                    ws.close();
                    if (res.result && res.result.data) {
                        return resolve(Buffer.from(res.result.data, 'base64'));
                    }
                    return reject(new Error(res.error ? res.error.message : 'No data'));
                }
            } catch (_) {}
        });

        ws.on('error', (err) => {
            clearTimeout(timer);
            reject(err);
        });
    });
}

// 5. Placeholder SVG generator saat standby
function generatePlaceholderSvg(text, subtext) {
    return Buffer.from(`
    <svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#11111b"/>
          <stop offset="50%" stop-color="#181825"/>
          <stop offset="100%" stop-color="#1e1e2e"/>
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="url(#bg)"/>
      <circle cx="540" cy="800" r="160" fill="#313244" stroke="#89b4fa" stroke-width="4"/>
      <path d="M 460 800 Q 540 700 620 800 Q 540 900 460 800 Z" fill="none" stroke="#89b4fa" stroke-width="8"/>
      <circle cx="540" cy="800" r="40" fill="#a6e3a1"/>
      <text x="540" y="1040" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif" font-size="44" font-weight="bold" fill="#cdd6f4" text-anchor="middle">${text}</text>
      <text x="540" y="1110" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif" font-size="28" fill="#a6adc8" text-anchor="middle">${subtext}</text>
      <text x="540" y="1200" font-family="monospace" font-size="24" fill="#6c7086" text-anchor="middle">webview &lt;url&gt;  |  webview --headless &lt;url&gt;</text>
    </svg>
    `.trim());
}

// 6. Template HTML Single Page Application (Catppuccin Mocha Dark UI)
function getDashboardHtml(reqHost, isDefaultControl) {
    return `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>ESO WebView Live Stream & Takeover</title>
  <style>
    :root {
      --bg: #0f0f17;
      --card: #181825;
      --surface: #1e1e2e;
      --surface-hover: #313244;
      --border: rgba(255, 255, 255, 0.08);
      --border-focus: #89b4fa;
      --text: #cdd6f4;
      --subtext: #a6adc8;
      --overlay: #6c7086;
      --blue: #89b4fa;
      --green: #a6e3a1;
      --peach: #fab387;
      --red: #f38ba8;
      --mauve: #cba6f7;
      --teal: #94e2d5;
      --yellow: #f9e2af;
      --nav-height: 48px;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      overflow-x: hidden;
      user-select: none;
    }
    header {
      background-color: var(--card);
      border-bottom: 1px solid var(--border);
      padding: 10px 18px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
      z-index: 100;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand h1 {
      font-size: 1.05rem;
      font-weight: 700;
      letter-spacing: 0.5px;
      color: var(--blue);
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .live-dot {
      width: 10px;
      height: 10px;
      background: var(--green);
      border-radius: 50%;
      box-shadow: 0 0 10px var(--green);
      animation: pulse 1.6s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(0.95); opacity: 0.8; }
      50% { transform: scale(1.2); opacity: 1; box-shadow: 0 0 14px var(--green); }
      100% { transform: scale(0.95); opacity: 0.8; }
    }
    .mode-pill {
      display: flex;
      align-items: center;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      overflow: hidden;
    }
    .mode-btn {
      padding: 6px 14px;
      font-size: 0.8rem;
      font-weight: 700;
      cursor: pointer;
      border: none;
      background: transparent;
      color: var(--subtext);
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 5px;
    }
    .mode-btn.active.watch {
      background: rgba(148, 226, 213, 0.2);
      color: var(--teal);
    }
    .mode-btn.active.control {
      background: rgba(250, 179, 135, 0.25);
      color: var(--peach);
      box-shadow: inset 0 0 10px rgba(250, 179, 135, 0.3);
    }
    .header-controls {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    select, button {
      background: var(--surface);
      color: var(--text);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 7px 12px;
      font-size: 0.82rem;
      cursor: pointer;
      outline: none;
      transition: all 0.2s;
      display: inline-flex;
      align-items: center;
      gap: 5px;
    }
    select:hover, button:hover {
      background: var(--surface-hover);
      border-color: var(--blue);
    }
    button.primary {
      background: var(--blue);
      color: var(--bg);
      font-weight: 700;
      border: none;
    }
    button.primary:hover {
      background: #b4befe;
    }
    button.warning {
      background: rgba(250, 179, 135, 0.15);
      color: var(--peach);
      border-color: rgba(250, 179, 135, 0.4);
    }
    button.danger {
      background: rgba(243, 139, 168, 0.15);
      color: var(--red);
      border-color: rgba(243, 139, 168, 0.4);
    }
    button.danger:hover {
      background: var(--red);
      color: var(--bg);
    }
    main {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px;
      gap: 12px;
      position: relative;
    }
    .monitor-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      overflow: hidden;
      box-shadow: 0 12px 36px rgba(0,0,0,0.6);
      display: flex;
      flex-direction: column;
      max-width: 1100px;
      width: 100%;
      position: relative;
      transition: all 0.3s ease;
    }

    /* FULLSCREEN MODE (Scrcpy Immersion) */
    .monitor-card.fullscreen-mode {
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      right: 0 !important;
      bottom: 0 !important;
      width: 100vw !important;
      height: 100vh !important;
      max-width: 100vw !important;
      max-height: 100vh !important;
      border-radius: 0 !important;
      border: none !important;
      z-index: 99999 !important;
      background: #000 !important;
      margin: 0 !important;
    }
    .monitor-card.fullscreen-mode .viewer-area {
      height: 100vh !important;
      max-height: 100vh !important;
    }
    .monitor-card.fullscreen-mode .card-bar,
    .monitor-card.fullscreen-mode .toolbar,
    .monitor-card.fullscreen-mode .control-ribbon {
      position: absolute;
      left: 0;
      right: 0;
      z-index: 10001;
      opacity: 0;
      pointer-events: none;
      transition: opacity 0.25s ease, transform 0.25s ease;
      background: rgba(24, 24, 37, 0.92);
      backdrop-filter: blur(12px);
    }
    .monitor-card.fullscreen-mode .card-bar {
      top: 0;
      transform: translateY(-100%);
    }
    .monitor-card.fullscreen-mode .toolbar {
      bottom: 0;
      transform: translateY(100%);
    }
    .monitor-card.fullscreen-mode .control-ribbon {
      bottom: var(--nav-height);
      transform: translateY(100%);
    }
    .monitor-card.fullscreen-mode:hover .card-bar,
    .monitor-card.fullscreen-mode:hover .toolbar,
    .monitor-card.fullscreen-mode:hover .control-ribbon,
    .monitor-card.fullscreen-mode.hud-visible .card-bar,
    .monitor-card.fullscreen-mode.hud-visible .toolbar,
    .monitor-card.fullscreen-mode.hud-visible .control-ribbon {
      opacity: 1;
      pointer-events: auto;
      transform: translateY(0);
    }

    .card-bar {
      background: var(--surface);
      border-bottom: 1px solid var(--border);
      padding: 8px 14px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      font-size: 0.82rem;
    }
    .url-chip {
      color: var(--subtext);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 500px;
      font-family: monospace;
      font-size: 0.78rem;
    }
    .badge-group {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .badge {
      font-size: 0.68rem;
      padding: 2px 7px;
      border-radius: 6px;
      font-weight: 700;
    }
    .badge.headless { background: rgba(203, 166, 247, 0.2); color: var(--mauve); border: 1px solid rgba(203, 166, 247, 0.4); }
    .badge.visual { background: rgba(166, 227, 161, 0.2); color: var(--green); border: 1px solid rgba(166, 227, 161, 0.4); }
    .badge.desktop { background: rgba(137, 180, 250, 0.2); color: var(--blue); border: 1px solid rgba(137, 180, 250, 0.4); }
    .badge.profile { background: rgba(250, 179, 135, 0.2); color: var(--peach); border: 1px solid rgba(250, 179, 135, 0.4); }

    .viewer-area {
      background: #000;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 520px;
      max-height: 76vh;
      overflow: hidden;
      user-select: none;
      touch-action: none;
    }
    .viewer-area.control-mode {
      cursor: crosshair;
    }
    .viewer-area img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      display: block;
      pointer-events: auto;
      touch-action: none;
    }

    /* HUD Status Overlay */
    .hud-overlay {
      position: absolute;
      top: 10px;
      left: 10px;
      right: 10px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      pointer-events: none;
      z-index: 10;
    }
    .hud-pill {
      background: rgba(17, 17, 27, 0.85);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.12);
      padding: 5px 12px;
      border-radius: 999px;
      font-size: 0.72rem;
      color: var(--text);
      display: flex;
      align-items: center;
      gap: 6px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    }
    .hud-pill.control-banner {
      background: rgba(250, 179, 135, 0.28);
      border-color: rgba(250, 179, 135, 0.6);
      color: var(--peach);
      font-weight: 700;
    }

    /* SCRCPY VIRTUAL NAVIGATION BAR (Docked at Bottom of Viewer) */
    .scrcpy-nav-bar {
      position: absolute;
      bottom: 12px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(24, 24, 37, 0.88);
      backdrop-filter: blur(14px);
      border: 1px solid rgba(255, 255, 255, 0.15);
      border-radius: 30px;
      padding: 4px 10px;
      display: flex;
      align-items: center;
      gap: 6px;
      z-index: 20;
      box-shadow: 0 8px 24px rgba(0,0,0,0.5);
      transition: opacity 0.3s ease, transform 0.3s ease;
    }
    .scrcpy-nav-btn {
      background: transparent;
      border: none;
      color: var(--text);
      padding: 7px 12px;
      border-radius: 20px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 5px;
      transition: all 0.15s ease;
    }
    .scrcpy-nav-btn:hover {
      background: rgba(255, 255, 255, 0.12);
      color: var(--blue);
      transform: translateY(-1px);
    }
    .scrcpy-nav-btn:active {
      transform: translateY(1px);
    }
    .scrcpy-nav-btn.primary-action {
      background: var(--blue);
      color: var(--bg);
      font-weight: 700;
    }
    .scrcpy-nav-btn.primary-action:hover {
      background: #b4befe;
    }

    /* Physical Keyboard Focus Indicator */
    .keyboard-indicator {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      font-size: 0.72rem;
      color: var(--teal);
    }
    .keyboard-indicator .kbd-dot {
      width: 7px;
      height: 7px;
      background: var(--teal);
      border-radius: 50%;
    }

    /* Direct Input Ribbon */
    .control-ribbon {
      background: var(--surface);
      border-top: 1px solid var(--border);
      padding: 8px 14px;
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .control-input {
      flex: 1;
      min-width: 220px;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 7px 12px;
      color: var(--text);
      font-size: 0.82rem;
      outline: none;
    }
    .control-input:focus {
      border-color: var(--blue);
    }

    .toolbar {
      padding: 8px 14px;
      background: var(--card);
      border-top: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      flex-wrap: wrap;
    }
    .tool-group {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .info-footer {
      max-width: 1100px;
      width: 100%;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 12px 16px;
      font-size: 0.78rem;
      color: var(--subtext);
    }
    .info-footer h3 {
      color: var(--blue);
      font-size: 0.85rem;
      margin-bottom: 6px;
    }
    .cmd-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
      gap: 6px;
      margin-top: 6px;
    }
    .cmd-box {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 6px 10px;
      border-radius: 6px;
      font-family: monospace;
      color: var(--green);
      font-size: 0.75rem;
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="live-dot"></div>
      <h1>ESO WebView Stream</h1>
      <div class="mode-pill">
        <button id="btnWatch" class="mode-btn active watch" onclick="setMode('watch')">👁️ Watch</button>
        <button id="btnControl" class="mode-btn control" onclick="setMode('control')">🎮 Ambil Alih</button>
      </div>
    </div>
    <div class="header-controls">
      <select id="tabSelector" onchange="changeTab(this.value)">
        <option value="">Memuat tab...</option>
      </select>
      <button class="primary" onclick="toggleFullscreen()" id="btnFullscreenTop">⛶ Fullscreen</button>
      <button class="warning" onclick="toggleAttach()">👁️ Attach</button>
      <button onclick="toggleDetach()">🕶️ Detach</button>
      <button class="danger" onclick="killCurrentTab()">❌ Kill</button>
      <button class="danger" onclick="killAllTabs()">💥 Kill All</button>
    </div>
  </header>

  <main>
    <div class="monitor-card" id="monitorCard">
      <div class="card-bar">
        <div style="display:flex; align-items:center; gap:8px; min-width:0;">
          <span style="font-weight:700;" id="tabTitle">Menunggu WebView...</span>
          <span class="url-chip" id="tabUrl">-</span>
        </div>
        <div class="badge-group" id="badgeGroup">
          <span class="badge headless">HEADLESS</span>
        </div>
      </div>
      
      <div class="viewer-area" id="viewerArea" tabindex="0">
        <img id="liveView" src="/api/placeholder.svg" alt="WebView Live Stream" draggable="false">
        
        <div class="hud-overlay">
          <div class="hud-pill" id="fpsPill">
            <span>FPS: <strong id="fpsVal">30</strong></span>
            <span>•</span>
            <span id="streamState">Scrcpy-Stream</span>
          </div>
          <div class="hud-pill control-banner" id="controlBanner" style="display:none;">
            <span>🎮 AMBIL ALIH AKTIF (100% Native Gestures)</span>
          </div>
          <div class="hud-pill" id="profilePill">
            <span id="profileVal">Profile: default</span>
          </div>
        </div>

        <!-- Scrcpy Virtual Navigation Bar -->
        <div class="scrcpy-nav-bar" id="scrcpyNavBar">
          <button class="scrcpy-nav-btn" onclick="sendNav('back')" title="Kembali (Back)">◀ Back</button>
          <button class="scrcpy-nav-btn" onclick="sendNav('forward')" title="Maju (Forward)">▶ Forward</button>
          <button class="scrcpy-nav-btn" onclick="sendNav('home')" title="Beranda (Home)">🏠 Home</button>
          <button class="scrcpy-nav-btn" onclick="sendNav('reload')" title="Muat Ulang (Reload)">🔄 Reload</button>
          <button class="scrcpy-nav-btn" onclick="toggleRibbonInput()" title="Buka Input Keyboard">⌨ Ketik</button>
          <button class="scrcpy-nav-btn" onclick="pasteClipboard()" title="Paste Clipboard">📋 Paste</button>
          <button class="scrcpy-nav-btn primary-action" onclick="toggleFullscreen()" title="Layar Penuh (Fullscreen)">⛶ Full</button>
        </div>
      </div>

      <div class="control-ribbon" id="controlRibbon" style="display:none;">
        <input type="text" id="typeInput" class="control-input" placeholder="Ketik teks di sini lalu tekan Enter..." onkeydown="handleInputKey(event)">
        <button class="primary" onclick="sendInputText()">⏎ Kirim</button>
        <button onclick="sendSpecialKey('Enter', 'Enter', 13)">⏎ Enter</button>
        <button onclick="sendSpecialKey('Backspace', 'Backspace', 8)">⌫ Del</button>
        <button onclick="sendSpecialKey('Tab', 'Tab', 9)">⇥ Tab</button>
        <button onclick="sendSpecialKey('Escape', 'Escape', 27)">⎋ Esc</button>
        <button onclick="sendScrollDelta(0, -350)">⬆️ Up</button>
        <button onclick="sendScrollDelta(0, 350)">⬇️ Down</button>
      </div>

      <div class="toolbar">
        <div class="tool-group">
          <span style="font-size:0.78rem; color:var(--subtext);">WLAN Host: <strong>http://${reqHost}</strong></span>
          <span style="font-size:0.78rem; color:var(--overlay);">|</span>
          <span class="keyboard-indicator" id="kbdIndicator">
            <span class="kbd-dot"></span> Direct Keyboard Active
          </span>
        </div>
        <div class="tool-group">
          <button onclick="copyCurrentUrl()">📋 Salin URL</button>
          <button onclick="toggleFullscreen()">⛶ Layar Penuh</button>
        </div>
      </div>
    </div>

    <div class="info-footer">
      <h3>🚀 Kontrol Cepat & Shortcut Terminal:</h3>
      <div class="cmd-grid">
        <div class="cmd-box">webview --headless --monitor &lt;url&gt;</div>
        <div class="cmd-box">webview --monitor-control</div>
        <div class="cmd-box">webview --attach &lt;idx&gt;</div>
        <div class="cmd-box">webview --detach &lt;idx&gt;</div>
        <div class="cmd-box">webview --kill &lt;idx&gt;</div>
        <div class="cmd-box">webview --kill-all</div>
      </div>
    </div>
  </main>

  <script>
    let currentTabId = '';
    let currentTabIndex = 0;
    let tabsList = [];
    let fps = 30;
    let mode = ${isDefaultControl ? "'control'" : "'watch'"};
    let wsStream = null;
    let currentWsUrl = '';
    let hudHideTimer = null;

    // Real FPS counter
    let framesThisSecond = 0;
    let lastFpsUpdate = performance.now();

    // Elements
    const imgEl = document.getElementById('liveView');
    const viewerArea = document.getElementById('viewerArea');
    const monitorCard = document.getElementById('monitorCard');
    const controlRibbon = document.getElementById('controlRibbon');

    async function loadTabs() {
      try {
        const res = await fetch('/api/tabs');
        tabsList = await res.json();
        const sel = document.getElementById('tabSelector');
        sel.innerHTML = '';
        if (tabsList.length === 0) {
          sel.innerHTML = '<option value="">Tidak ada WebView aktif</option>';
          document.getElementById('tabTitle').innerText = 'Tidak ada WebView aktif';
          document.getElementById('tabUrl').innerText = 'Buka webview via terminal: webview <url>';
          document.getElementById('liveView').src = '/api/placeholder.svg';
          stopWsStream();
          return;
        }

        tabsList.forEach(t => {
          const opt = document.createElement('option');
          opt.value = t.id || t.index;
          const modeTag = t.isHeadless ? '[HEADLESS]' : '[VISUAL]';
          const viewTag = t.isDesktop ? '[DESKTOP]' : '[MOBILE]';
          opt.innerText = '#' + t.index + ' ' + modeTag + ' ' + viewTag + ' ' + (t.title || 'Tab');
          if (t.active && !currentTabId) {
            opt.selected = true;
            currentTabId = opt.value;
            currentTabIndex = t.index;
            currentWsUrl = t.wsUrl;
          }
          sel.appendChild(opt);
        });

        if (!currentTabId && tabsList.length > 0) {
          currentTabId = tabsList[0].id || tabsList[0].index;
          currentTabIndex = tabsList[0].index;
          currentWsUrl = tabsList[0].wsUrl;
        }

        updateTabMetadata();
        connectWsStream();
      } catch (err) {
        console.error('Gagal memuat tabs:', err);
      }
    }

    function updateTabMetadata() {
      const tab = tabsList.find(t => (t.id || t.index) == currentTabId) || tabsList[0];
      if (!tab) return;
      document.getElementById('tabTitle').innerText = tab.title || 'WebView Tab';
      document.getElementById('tabUrl').innerText = tab.url || 'about:blank';
      document.getElementById('profileVal').innerText = 'Profile: ' + (tab.profile || 'default');

      const bg = document.getElementById('badgeGroup');
      bg.innerHTML = '';
      if (tab.isHeadless) {
        bg.innerHTML += '<span class="badge headless">HEADLESS</span>';
      } else {
        bg.innerHTML += '<span class="badge visual">VISUAL</span>';
      }
      if (tab.isDesktop) {
        bg.innerHTML += '<span class="badge desktop">DESKTOP</span>';
      }
      if (tab.proxy && tab.proxy !== 'direct') {
        bg.innerHTML += '<span class="badge profile">' + tab.proxy + '</span>';
      }
    }

    function changeTab(val) {
      currentTabId = val;
      const tab = tabsList.find(t => (t.id || t.index) == val);
      if (tab) {
        currentTabIndex = tab.index;
        currentWsUrl = tab.wsUrl;
      }
      updateTabMetadata();
      connectWsStream();
    }

    function setMode(newMode) {
      mode = newMode;
      const btnW = document.getElementById('btnWatch');
      const btnC = document.getElementById('btnControl');
      const banner = document.getElementById('controlBanner');
      const kbd = document.getElementById('kbdIndicator');

      if (mode === 'control') {
        btnW.className = 'mode-btn';
        btnC.className = 'mode-btn active control';
        viewerArea.classList.add('control-mode');
        banner.style.display = 'flex';
        controlRibbon.style.display = 'flex';
        kbd.style.display = 'inline-flex';
        viewerArea.focus();
      } else {
        btnW.className = 'mode-btn active watch';
        btnC.className = 'mode-btn';
        viewerArea.classList.remove('control-mode');
        banner.style.display = 'none';
        controlRibbon.style.display = 'none';
        kbd.style.display = 'none';
      }
    }

    // WebSocket 30-60 FPS Screencast Stream
    function connectWsStream() {
      stopWsStream();
      if (!currentTabId) return;

      const wsProto = location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = wsProto + '//' + location.host + '/ws?tab=' + encodeURIComponent(currentTabId) + '&fps=' + fps;
      
      try {
        wsStream = new WebSocket(wsUrl);
        wsStream.binaryType = 'blob';

        wsStream.onmessage = (event) => {
          if (event.data instanceof Blob) {
            const blobUrl = URL.createObjectURL(event.data);
            imgEl.onload = () => {
              URL.revokeObjectURL(blobUrl);
            };
            imgEl.src = blobUrl;

            // Frame counter calculation
            framesThisSecond++;
            const now = performance.now();
            if (now - lastFpsUpdate >= 1000) {
              const measuredFps = Math.round((framesThisSecond * 1000) / (now - lastFpsUpdate));
              document.getElementById('fpsVal').innerText = measuredFps;
              framesThisSecond = 0;
              lastFpsUpdate = now;
            }
          }
        };

        wsStream.onclose = () => {
          setTimeout(connectWsStream, 1200);
        };

        wsStream.onerror = () => {
          setTimeout(connectWsStream, 1500);
        };
      } catch (_) {
        setTimeout(connectWsStream, 1500);
      }
    }

    function stopWsStream() {
      if (wsStream) {
        try { wsStream.close(); } catch (_) {}
        wsStream = null;
      }
    }

    // PIXEL-PERFECT COORDINATE CALCULATION (Letterbox & Pillarbox aware)
    function getRenderRect() {
      const rect = imgEl.getBoundingClientRect();
      const naturalW = imgEl.naturalWidth || 1080;
      const naturalH = imgEl.naturalHeight || 1920;
      const naturalAspect = naturalW / naturalH;
      const elementAspect = rect.width / rect.height;

      let renderW, renderH, offsetX, offsetY;
      if (elementAspect > naturalAspect) {
        // Pillarbox (bars on left/right)
        renderH = rect.height;
        renderW = renderH * naturalAspect;
        offsetX = (rect.width - renderW) / 2;
        offsetY = 0;
      } else {
        // Letterbox (bars on top/bottom)
        renderW = rect.width;
        renderH = renderW / naturalAspect;
        offsetX = 0;
        offsetY = (rect.height - renderH) / 2;
      }

      return {
        left: rect.left + offsetX,
        top: rect.top + offsetY,
        width: renderW,
        height: renderH,
        naturalW: naturalW,
        naturalH: naturalH
      };
    }

    function toImageCoords(clientX, clientY) {
      const box = getRenderRect();
      const relX = clientX - box.left;
      const relY = clientY - box.top;
      const x = Math.round((relX / box.width) * box.naturalW);
      const y = Math.round((relY / box.height) * box.naturalH);

      return {
        x: Math.max(0, Math.min(x, box.naturalW)),
        y: Math.max(0, Math.min(y, box.naturalH)),
        isInside: relX >= 0 && relX <= box.width && relY >= 0 && relY <= box.height
      };
    }

    // 100% SCRCPY POINTER GESTURES (Drag, Swipe, Click, Scroll)
    let isPointerDown = false;
    let activePointerId = null;

    imgEl.addEventListener('pointerdown', (e) => {
      if (mode !== 'control') return;
      e.preventDefault();
      viewerArea.focus();
      const coords = toImageCoords(e.clientX, e.clientY);
      isPointerDown = true;
      activePointerId = e.pointerId;
      try { imgEl.setPointerCapture(e.pointerId); } catch (_) {}

      sendPointerEvent('pointerDown', coords.x, coords.y, e.button === 2 ? 'right' : 'left', e.buttons || 1);
    });

    imgEl.addEventListener('pointermove', (e) => {
      if (mode !== 'control') return;
      if (!isPointerDown) return;
      e.preventDefault();
      const coords = toImageCoords(e.clientX, e.clientY);

      sendPointerEvent('pointerMove', coords.x, coords.y, 'none', e.buttons || 1);
    });

    function finishPointer(e) {
      if (!isPointerDown) return;
      isPointerDown = false;
      const coords = toImageCoords(e.clientX, e.clientY);
      try { imgEl.releasePointerCapture(activePointerId); } catch (_) {}
      activePointerId = null;

      sendPointerEvent('pointerUp', coords.x, coords.y, e.button === 2 ? 'right' : 'left', 0);
    }

    imgEl.addEventListener('pointerup', finishPointer);
    imgEl.addEventListener('pointercancel', finishPointer);

    // Prevent context menu to allow smooth right-click / gestures
    imgEl.addEventListener('contextmenu', (e) => {
      if (mode === 'control') {
        e.preventDefault();
      }
    });

    // Mouse Wheel / Trackpad Smooth Scrolling
    imgEl.addEventListener('wheel', (e) => {
      if (mode !== 'control') return;
      e.preventDefault();
      const coords = toImageCoords(e.clientX, e.clientY);
      sendWheel(coords.x, coords.y, e.deltaX, e.deltaY);
    }, { passive: false });

    function sendPointerEvent(action, x, y, button, buttons) {
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) return;
      wsStream.send(JSON.stringify({
        action: action,
        x: x,
        y: y,
        button: button,
        buttons: buttons
      }));
    }

    function sendWheel(x, y, dx, dy) {
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) return;
      wsStream.send(JSON.stringify({
        action: 'wheel',
        x: x,
        y: y,
        deltaX: Math.round(dx),
        deltaY: Math.round(dy)
      }));
    }

    function sendScrollDelta(dx, dy) {
      sendWheel(540, 960, dx, dy);
    }

    // DIRECT PHYSICAL KEYBOARD CAPTURE
    window.addEventListener('keydown', (e) => {
      if (mode !== 'control') return;
      // Jangan cegah jika fokus di input field ribbon
      if (document.activeElement === document.getElementById('typeInput')) return;

      // Handle shortcuts
      if (e.key === 'F11') {
        e.preventDefault();
        toggleFullscreen();
        return;
      }
      if (e.key === 'Escape' && document.fullscreenElement) {
        // Biarkan default fullscreen exit
        return;
      }

      // Forward key to CDP
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        e.preventDefault();
        wsStream.send(JSON.stringify({
          action: 'keyDown',
          key: e.key,
          code: e.code,
          keyCode: e.keyCode,
          modifiers: (e.altKey ? 1 : 0) | (e.ctrlKey ? 2 : 0) | (e.metaKey ? 4 : 0) | (e.shiftKey ? 8 : 0)
        }));
      }
    });

    window.addEventListener('keyup', (e) => {
      if (mode !== 'control') return;
      if (document.activeElement === document.getElementById('typeInput')) return;

      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        e.preventDefault();
        wsStream.send(JSON.stringify({
          action: 'keyUp',
          key: e.key,
          code: e.code,
          keyCode: e.keyCode
        }));
      }
    });

    // Direct Text Input Helpers
    function sendInputText() {
      const input = document.getElementById('typeInput');
      const txt = input.value;
      if (!txt) return;
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({ action: 'text', text: txt }));
      }
      input.value = '';
    }

    function handleInputKey(e) {
      if (e.key === 'Enter') {
        sendInputText();
      }
    }

    function sendSpecialKey(key, code, keyCode) {
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({
          action: 'keyDown',
          key: key,
          code: code,
          keyCode: keyCode
        }));
        setTimeout(() => {
          wsStream.send(JSON.stringify({
            action: 'keyUp',
            key: key,
            code: code,
            keyCode: keyCode
          }));
        }, 30);
      }
    }

    // Navigation Commands (Back, Forward, Home, Reload)
    function sendNav(command) {
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({ action: 'nav', command: command }));
      }
    }

    // Clipboard Paste into active element
    async function pasteClipboard() {
      try {
        const text = await navigator.clipboard.readText();
        if (text && wsStream && wsStream.readyState === WebSocket.OPEN) {
          wsStream.send(JSON.stringify({ action: 'text', text: text }));
        }
      } catch (e) {
        const fallback = prompt('Tempel teks ke WebView:');
        if (fallback && wsStream && wsStream.readyState === WebSocket.OPEN) {
          wsStream.send(JSON.stringify({ action: 'text', text: fallback }));
        }
      }
    }

    function toggleRibbonInput() {
      if (controlRibbon.style.display === 'none') {
        controlRibbon.style.display = 'flex';
        document.getElementById('typeInput').focus();
      } else {
        controlRibbon.style.display = 'none';
      }
    }

    // FULLSCREEN TOGGLE & AUTO-HIDING HUD
    function toggleFullscreen() {
      if (!document.fullscreenElement) {
        if (monitorCard.requestFullscreen) {
          monitorCard.requestFullscreen();
        } else if (monitorCard.webkitRequestFullscreen) {
          monitorCard.webkitRequestFullscreen();
        }
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
      }
    }

    document.addEventListener('fullscreenchange', onFullscreenChange);
    document.addEventListener('webkitfullscreenchange', onFullscreenChange);

    function onFullscreenChange() {
      const isFull = Boolean(document.fullscreenElement);
      if (isFull) {
        monitorCard.classList.add('fullscreen-mode');
        document.getElementById('btnFullscreenTop').innerText = '✕ Exit Full';
        triggerHudReveal();
      } else {
        monitorCard.classList.remove('fullscreen-mode');
        document.getElementById('btnFullscreenTop').innerText = '⛶ Fullscreen';
      }
    }

    function triggerHudReveal() {
      monitorCard.classList.add('hud-visible');
      if (hudHideTimer) clearTimeout(hudHideTimer);
      hudHideTimer = setTimeout(() => {
        if (document.fullscreenElement) {
          monitorCard.classList.remove('hud-visible');
        }
      }, 2500);
    }

    monitorCard.addEventListener('mousemove', () => {
      if (document.fullscreenElement) {
        triggerHudReveal();
      }
    });

    // Quick Actions
    async function toggleAttach() {
      try {
        const res = await fetch('/api/attach?idx=' + currentTabIndex, { method: 'POST' });
        const data = await res.json();
        alert(data.message || 'Attached ke layar!');
        setTimeout(loadTabs, 800);
      } catch (e) {
        alert('Gagal attach: ' + e.message);
      }
    }

    async function toggleDetach() {
      try {
        const res = await fetch('/api/detach?idx=' + currentTabIndex, { method: 'POST' });
        const data = await res.json();
        alert(data.message || 'Detached ke background (Headless Mode)!');
        setTimeout(loadTabs, 800);
      } catch (e) {
        alert('Gagal detach: ' + e.message);
      }
    }

    async function killCurrentTab() {
      if (!confirm('Tutup tab #' + currentTabIndex + '?')) return;
      try {
        await fetch('/api/kill?idx=' + currentTabIndex, { method: 'POST' });
        setTimeout(loadTabs, 500);
      } catch (e) {
        alert('Gagal menutup tab: ' + e.message);
      }
    }

    async function killAllTabs() {
      if (!confirm('Tutup SELURUH tab WebView?')) return;
      try {
        await fetch('/api/kill_all', { method: 'POST' });
        setTimeout(loadTabs, 500);
      } catch (e) {
        alert('Gagal menutup semua tab: ' + e.message);
      }
    }

    function copyCurrentUrl() {
      const urlText = document.getElementById('tabUrl').innerText;
      if (urlText && urlText !== '-') {
        navigator.clipboard.writeText(urlText);
        alert('URL disalin ke clipboard!');
      }
    }

    // Auto-refresh tab list every 4s
    setInterval(loadTabs, 4000);

    // Initial load
    setMode(mode);
    loadTabs();
  </script>
</body>
</html>`;
}

// 7. HTTP Server Setup
const server = http.createServer(async (req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    const reqHost = req.headers.host || `127.0.0.1:${PORT}`;

    // CORS & Cache-Control headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    // Root UI
    if (pathname === '/' || pathname === '/index.html') {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(getDashboardHtml(reqHost, DEFAULT_CONTROL));
        return;
    }

    // Placeholder SVG
    if (pathname === '/api/placeholder.svg') {
        res.writeHead(200, {
            'Content-Type': 'image/svg+xml',
            'Cache-Control': 'no-cache'
        });
        res.end(generatePlaceholderSvg('Menunggu WebView Aktif', 'Buka webview di Termux untuk memulai watch mode'));
        return;
    }

    // API: Tabs
    if (pathname === '/api/tabs') {
        const tabs = await getAllTabs();
        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        });
        res.end(JSON.stringify(tabs));
        return;
    }

    // API: Screenshot single frame (Fallback)
    if (pathname === '/api/screenshot') {
        const tabs = await getAllTabs();
        const tabTarget = parsedUrl.query.tab;
        let selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget));
        if (!selected && tabs.length > 0) selected = tabs[0];

        if (!selected || !selected.wsUrl) {
            res.writeHead(200, { 'Content-Type': 'image/svg+xml' });
            res.end(generatePlaceholderSvg('Tab Tidak Tersambung', 'Menunggu WebSocket DevTools'));
            return;
        }

        try {
            const buf = await captureCdpScreenshot(selected.wsUrl, 75);
            res.writeHead(200, {
                'Content-Type': 'image/jpeg',
                'Content-Length': buf.length,
                'Cache-Control': 'no-store, no-cache, must-revalidate, max-age=0'
            });
            res.end(buf);
        } catch (err) {
            res.writeHead(200, { 'Content-Type': 'image/svg+xml' });
            res.end(generatePlaceholderSvg('Gagal Capture', err.message));
        }
        return;
    }

    // API: Action Attach
    if (pathname === '/api/attach') {
        const idx = parsedUrl.query.idx || '0';
        try {
            const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview attach ${idx}`, { timeout: 2000 });
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: true, message: `Tab ${idx} berhasil di-attach ke layar (Visual Mode)` }));
        } catch (e) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: false, error: e.message }));
        }
        return;
    }

    // API: Action Detach
    if (pathname === '/api/detach') {
        const idx = parsedUrl.query.idx || '-1';
        try {
            const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview detach ${idx}`, { timeout: 2000 });
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: true, message: `Tab ${idx} berhasil di-detach ke background (Headless Mode)` }));
        } catch (e) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: false, error: e.message }));
        }
        return;
    }

    // API: Action Reload
    if (pathname === '/api/reload') {
        try {
            const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview reload`, { timeout: 2000 });
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: true }));
        } catch (e) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: false, error: e.message }));
        }
        return;
    }

    // API: Action Kill Tab
    if (pathname === '/api/kill') {
        const idx = parsedUrl.query.idx || '';
        try {
            const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview kill ${idx}`, { timeout: 2000 });
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: true, message: `Tab ${idx} berhasil ditutup` }));
        } catch (e) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: false, error: e.message }));
        }
        return;
    }

    // API: Action Kill All Tabs
    if (pathname === '/api/kill_all') {
        try {
            const ipcBin = fs.existsSync('/usr/local/bin/termux-ipc') ? '/usr/local/bin/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview kill-all`, { timeout: 2000 });
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: true, message: `Seluruh tab WebView telah ditutup` }));
        } catch (e) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ ok: false, error: e.message }));
        }
        return;
    }

    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not Found');
});

// 8. WebSocket 30-60 FPS Screencast & 100% Remote Control Bridge (Scrcpy Protocol)
if (WebSocket && WebSocket.Server) {
    const wss = new WebSocket.Server({ noServer: true });

    server.on('upgrade', (request, socket, head) => {
        const parsed = url.parse(request.url, true);
        if (parsed.pathname === '/ws') {
            wss.handleUpgrade(request, socket, head, (clientWs) => {
                wss.emit('connection', clientWs, request);
            });
        } else {
            socket.destroy();
        }
    });

    wss.on('connection', async (clientWs, req) => {
        const parsed = url.parse(req.url, true);
        const tabTarget = parsed.query.tab;

        const tabs = await getAllTabs();
        const selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget)) || tabs[0];

        if (!selected || !selected.wsUrl) {
            try { clientWs.close(); } catch (_) {}
            return;
        }

        let cdpWs = null;
        let reqId = 1;
        let isClosed = false;

        try {
            cdpWs = new WebSocket(selected.wsUrl);
        } catch (e) {
            try { clientWs.close(); } catch (_) {}
            return;
        }

        const cleanup = () => {
            isClosed = true;
            if (cdpWs) {
                try {
                    cdpWs.send(JSON.stringify({ id: reqId++, method: 'Page.stopScreencast' }));
                    cdpWs.close();
                } catch (_) {}
                cdpWs = null;
            }
        };

        clientWs.on('close', cleanup);
        clientWs.on('error', cleanup);
        cdpWs.on('error', cleanup);

        cdpWs.on('open', () => {
            if (isClosed) return;
            cdpWs.send(JSON.stringify({ id: reqId++, method: 'Page.enable' }));

            // Start Event-Driven Screencast Stream (scrcpy-style: 0% CPU idle, push on compositor damage)
            cdpWs.send(JSON.stringify({
                id: reqId++,
                method: 'Page.startScreencast',
                params: {
                    format: 'jpeg',
                    quality: 75,
                    maxWidth: 1080,
                    maxHeight: 1920,
                    everyNthFrame: 1
                }
            }));
        });

        cdpWs.on('message', (data) => {
            if (isClosed) return;
            try {
                const msg = JSON.parse(data.toString());
                if (msg.method === 'Page.screencastFrame') {
                    if (msg.params && msg.params.data) {
                        const imgBuf = Buffer.from(msg.params.data, 'base64');
                        if (clientWs.readyState === WebSocket.OPEN) {
                            clientWs.send(imgBuf);
                        }
                    }
                    cdpWs.send(JSON.stringify({
                        method: 'Page.screencastFrameAck',
                        params: { sessionId: msg.params.sessionId }
                    }));
                }
            } catch (_) {}
        });

        // Client Input Handler (100% Scrcpy-Style Native Gestures & Key Takeover)
        clientWs.on('message', (raw) => {
            if (isClosed || !cdpWs || cdpWs.readyState !== WebSocket.OPEN) return;
            try {
                const ev = JSON.parse(raw.toString());

                // Pointer Events (Drag, Swipe, Click)
                if (ev.action === 'pointerDown') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: {
                            type: 'mousePressed',
                            x: ev.x,
                            y: ev.y,
                            button: ev.button || 'left',
                            buttons: ev.buttons || 1,
                            clickCount: 1
                        }
                    }));
                } else if (ev.action === 'pointerMove') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: {
                            type: 'mouseMoved',
                            x: ev.x,
                            y: ev.y,
                            buttons: ev.buttons || 1
                        }
                    }));
                } else if (ev.action === 'pointerUp') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: {
                            type: 'mouseReleased',
                            x: ev.x,
                            y: ev.y,
                            button: ev.button || 'left',
                            clickCount: 1
                        }
                    }));
                } else if (ev.action === 'wheel') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: {
                            type: 'mouseWheel',
                            x: ev.x || 540,
                            y: ev.y || 960,
                            deltaX: ev.deltaX || 0,
                            deltaY: ev.deltaY || 0
                        }
                    }));
                } else if (ev.action === 'text') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.insertText',
                        params: { text: ev.text || '' }
                    }));
                } else if (ev.action === 'keyDown') {
                    const isChar = ev.key && ev.key.length === 1 && !ev.ctrlKey && !ev.altKey;
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchKeyEvent',
                        params: {
                            type: 'rawKeyDown',
                            key: ev.key,
                            code: ev.code,
                            windowsVirtualKeyCode: ev.keyCode,
                            modifiers: ev.modifiers || 0,
                            text: isChar ? ev.key : undefined,
                            unmodifiedText: isChar ? ev.key : undefined
                        }
                    }));
                    if (isChar) {
                        cdpWs.send(JSON.stringify({
                            id: reqId++,
                            method: 'Input.dispatchKeyEvent',
                            params: {
                                type: 'char',
                                text: ev.key,
                                unmodifiedText: ev.key
                            }
                        }));
                    }
                } else if (ev.action === 'keyUp') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchKeyEvent',
                        params: {
                            type: 'keyUp',
                            key: ev.key,
                            code: ev.code,
                            windowsVirtualKeyCode: ev.keyCode
                        }
                    }));
                } else if (ev.action === 'nav') {
                    if (ev.command === 'back') {
                        cdpWs.send(JSON.stringify({
                            id: reqId++,
                            method: 'Runtime.evaluate',
                            params: { expression: 'window.history.back()' }
                        }));
                    } else if (ev.command === 'forward') {
                        cdpWs.send(JSON.stringify({
                            id: reqId++,
                            method: 'Runtime.evaluate',
                            params: { expression: 'window.history.forward()' }
                        }));
                    } else if (ev.command === 'reload') {
                        cdpWs.send(JSON.stringify({
                            id: reqId++,
                            method: 'Page.reload',
                            params: { ignoreCache: true }
                        }));
                    } else if (ev.command === 'home') {
                        cdpWs.send(JSON.stringify({
                            id: reqId++,
                            method: 'Page.navigate',
                            params: { url: 'https://www.google.com' }
                        }));
                    }
                }
            } catch (_) {}
        });
    });
}

function freePort(port) {
    try {
        const out = execSync(`fuser ${port}/tcp 2>/dev/null || true`, { encoding: 'utf8' }).trim();
        if (out) {
            execSync(`fuser -k ${port}/tcp 2>/dev/null || true`);
            const start = Date.now();
            while (Date.now() - start < 150) {}
        }
    } catch (_) {}
}

function onListening() {
    ensurePortForward();
    const ips = getNetworkIps();
    console.log('\n\x1b[36m┌────────────────────────────────────────────────────────┐\x1b[0m');
    console.log('\x1b[36m│\x1b[0m   \x1b[1m\x1b[32m🛸 ESO WEBVIEW STREAM & REMOTE CONTROL\x1b[0m               \x1b[36m│\x1b[0m');
    console.log('\x1b[36m├────────────────────────────────────────────────────────┤\x1b[0m');
    console.log(`\x1b[36m│\x1b[0m  \x1b[1mLocal URL\x1b[0m : \x1b[33mhttp://127.0.0.1:${PORT}\x1b[0m                          \x1b[36m│\x1b[0m`);
    ips.forEach(i => {
        const line = `  WLAN (${i.iface}) : http://${i.ip}:${PORT}`;
        console.log(`\x1b[36m│\x1b[0m  \x1b[1m${line.padEnd(52)}\x1b[0m\x1b[36m│\x1b[0m`);
    });
    console.log(`\x1b[36m│\x1b[0m  \x1b[1mMode\x1b[0m      : ${DEFAULT_CONTROL ? '\x1b[33m🎮 Remote Control (Ambil Alih 30-60 FPS)\x1b[0m' : '\x1b[32m👁️ Watch Mode (30-60 FPS Stream)\x1b[0m'}  \x1b[36m│\x1b[0m`);
    console.log('\x1b[36m│\x1b[0m  \x1b[1mEngine\x1b[0m    : \x1b[35mScrcpy-Style Event-Driven Stream (0% Idle CPU)\x1b[0m\x1b[36m│\x1b[0m');
    console.log('\x1b[36m│\x1b[0m  \x1b[1mFeatures\x1b[0m  : \x1b[36mFullscreen Mode, Drag/Swipe Gestures, Virtual Nav\x1b[0m\x1b[36m│\x1b[0m');
    console.log('\x1b[36m└────────────────────────────────────────────────────────┘\x1b[0m\n');
    console.log('Tekan Ctrl + C untuk menghentikan monitor.\n');
}

server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.warn(`[WebView Monitor] Port ${PORT} sedang dipakai, membebaskan port...`);
        try {
            execSync(`fuser -k ${PORT}/tcp 2>/dev/null || true`);
            setTimeout(() => {
                server.listen(PORT, '0.0.0.0', onListening);
            }, 300);
            return;
        } catch (_) {}
    }
    console.error(`[WebView Monitor] Server error:`, err.message);
    process.exit(1);
});

function gracefulExit() {
    try { server.close(); } catch (_) {}
    try { wss.close(); } catch (_) {}
    process.exit(0);
}
process.on('SIGINT', gracefulExit);
process.on('SIGTERM', gracefulExit);
process.on('SIGHUP', gracefulExit);

freePort(PORT);
server.listen(PORT, '0.0.0.0', onListening);
