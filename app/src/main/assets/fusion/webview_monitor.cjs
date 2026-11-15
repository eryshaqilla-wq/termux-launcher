#!/usr/bin/env node

/**
 * ESO WEBVIEW LIVE MONITOR & REMOTE CONTROL (WLAN 30-60 FPS)
 * Memantau dan MENGAMBIL ALIH (Remote Control) WebView Termux secara real-time via Web/WLAN.
 * Mendukung Watch Mode (Read-Only) dan Control Mode (Klik, Scroll, Ketik) di kecepatan 30-60 FPS.
 * Zero-Lag Binary WebSocket Screencast Protocol.
 */

const http = require('http');
const url = require('url');
const os = require('os');
const fs = require('fs');
const path = require('path');
const { execSync, spawn } = require('child_process');

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
            const matchedCdp = pageCdpTabs[idx] || pageCdpTabs.find(c => c.url === tab.url) || pageCdpTabs[0];
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
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ESO WebView Live Monitor & Takeover</title>
  <style>
    :root {
      --bg: #11111b;
      --card: #181825;
      --surface: #1e1e2e;
      --border: #313244;
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
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .brand h1 {
      font-size: 1.1rem;
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
      animation: pulse 1.5s infinite;
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
      padding: 6px 12px;
      font-size: 0.78rem;
      font-weight: 700;
      cursor: pointer;
      border: none;
      background: transparent;
      color: var(--subtext);
      transition: all 0.2s;
    }
    .mode-btn.active.watch {
      background: rgba(148, 226, 213, 0.2);
      color: var(--teal);
    }
    .mode-btn.active.control {
      background: rgba(250, 179, 135, 0.25);
      color: var(--peach);
      box-shadow: inset 0 0 8px rgba(250, 179, 135, 0.3);
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
    }
    select:hover, button:hover {
      border-color: var(--blue);
    }
    button.primary {
      background: var(--blue);
      color: var(--bg);
      font-weight: 600;
      border: none;
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
      padding: 14px;
      gap: 12px;
    }
    .monitor-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      overflow: hidden;
      box-shadow: 0 10px 30px rgba(0,0,0,0.5);
      display: flex;
      flex-direction: column;
      max-width: 1050px;
      width: 100%;
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
      max-width: 480px;
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
      min-height: 480px;
      max-height: 72vh;
      overflow: hidden;
      user-select: none;
    }
    .viewer-area.control-mode {
      cursor: crosshair;
    }
    .viewer-area img {
      max-width: 100%;
      max-height: 72vh;
      object-fit: contain;
      display: block;
      pointer-events: auto;
    }
    .hud-overlay {
      position: absolute;
      bottom: 10px;
      left: 10px;
      right: 10px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      pointer-events: none;
    }
    .hud-pill {
      background: rgba(17, 17, 27, 0.85);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 5px 10px;
      border-radius: 999px;
      font-size: 0.72rem;
      color: var(--text);
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .hud-pill.control-banner {
      background: rgba(250, 179, 135, 0.25);
      border-color: rgba(250, 179, 135, 0.5);
      color: var(--peach);
      font-weight: 700;
    }
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
      padding: 10px 14px;
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
      max-width: 1050px;
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
      <select id="fpsSelector" onchange="setFps(this.value)">
        <option value="60">60 FPS (Ultra 60Hz)</option>
        <option value="30" selected>30 FPS (WLAN Fluid)</option>
        <option value="15">15 FPS (Fast)</option>
        <option value="5">5 FPS (Standard)</option>
        <option value="1">1 FPS (Eco)</option>
      </select>
      <button class="primary" onclick="toggleAttach()">👁️ Attach</button>
      <button class="warning" onclick="toggleDetach()">🕶️ Detach</button>
      <button class="danger" onclick="killCurrentTab()">❌ Kill Tab</button>
      <button class="danger" onclick="killAllTabs()">💥 Kill All</button>
    </div>
  </header>

  <main>
    <div class="monitor-card">
      <div class="card-bar">
        <div style="display:flex; align-items:center; gap:8px; min-width:0;">
          <span style="font-weight:700;" id="tabTitle">Menunggu WebView...</span>
          <span class="url-chip" id="tabUrl">-</span>
        </div>
        <div class="badge-group" id="badgeGroup">
          <span class="badge headless">HEADLESS</span>
        </div>
      </div>
      
      <div class="viewer-area" id="viewerArea">
        <img id="liveView" src="/api/placeholder.svg" alt="WebView Live Stream">
        <div class="hud-overlay">
          <div class="hud-pill" id="fpsPill">
            <span>FPS: <strong id="fpsVal">30</strong></span>
            <span>•</span>
            <span id="latencyVal">Latency: 12ms</span>
          </div>
          <div class="hud-pill control-banner" id="controlBanner" style="display:none;">
            <span>🎮 AMBIL ALIH AKTIF (Klik & Ketik Aktif)</span>
          </div>
          <div class="hud-pill">
            <span id="profileVal">Profile: default</span>
          </div>
        </div>
      </div>

      <div class="control-ribbon" id="controlRibbon" style="display:none;">
        <input type="text" id="typeInput" class="control-input" placeholder="Ketik teks di sini lalu tekan Enter untuk input ke webview..." onkeydown="handleInputKey(event)">
        <button class="primary" onclick="sendInputText()">⏎ Kirim Teks</button>
        <button onclick="sendSpecialKey('Enter', 'Enter', 13)">⏎ Enter</button>
        <button onclick="sendSpecialKey('Backspace', 'Backspace', 8)">⌫ Backspace</button>
        <button onclick="sendSpecialKey('Tab', 'Tab', 9)">⇥ Tab</button>
        <button onclick="sendScroll(0, -400)">⬆️ Scroll Up</button>
        <button onclick="sendScroll(0, 400)">⬇️ Scroll Down</button>
      </div>

      <div class="toolbar">
        <div class="tool-group">
          <span style="font-size:0.78rem; color:var(--subtext);">WLAN: <strong>http://${reqHost}</strong></span>
        </div>
        <div class="tool-group">
          <button onclick="reloadTab()">🔄 Reload</button>
          <button onclick="copyCurrentUrl()">📋 Salin URL</button>
        </div>
      </div>
    </div>

    <div class="info-footer">
      <h3>🚀 Cheatsheet Kontrol Terminal (Zsh):</h3>
      <div class="cmd-grid">
        <div class="cmd-box">webview --headless --monitor &lt;url&gt;</div>
        <div class="cmd-box">webview --monitor-control [port]</div>
        <div class="cmd-box">webview --list [--json]</div>
        <div class="cmd-box">webview --attach &lt;idx&gt;</div>
        <div class="cmd-box">webview --detach &lt;idx&gt;</div>
        <div class="cmd-box">webview --kill [idx]</div>
        <div class="cmd-box">webview --kill-all</div>
        <div class="cmd-box">webview --desktop &lt;url&gt;</div>
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
    let isFetching = false;
    let fallbackTimer = null;

    // Real FPS counter
    let framesThisSecond = 0;
    let lastFpsUpdate = performance.now();

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
      const vArea = document.getElementById('viewerArea');
      const banner = document.getElementById('controlBanner');
      const ribbon = document.getElementById('controlRibbon');

      if (mode === 'control') {
        btnW.className = 'mode-btn';
        btnC.className = 'mode-btn active control';
        vArea.classList.add('control-mode');
        banner.style.display = 'flex';
        ribbon.style.display = 'flex';
      } else {
        btnW.className = 'mode-btn active watch';
        btnC.className = 'mode-btn';
        vArea.classList.remove('control-mode');
        banner.style.display = 'none';
        ribbon.style.display = 'none';
      }
    }

    // WebSocket 30-60 FPS Screencast & Input Bridge
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
            const img = document.getElementById('liveView');
            img.onload = () => {
              URL.revokeObjectURL(blobUrl);
            };
            img.src = blobUrl;

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
          startFallbackPolling();
        };

        wsStream.onerror = () => {
          startFallbackPolling();
        };
      } catch (_) {
        startFallbackPolling();
      }
    }

    function stopWsStream() {
      if (wsStream) {
        try { wsStream.close(); } catch (_) {}
        wsStream = null;
      }
      if (fallbackTimer) {
        clearInterval(fallbackTimer);
        fallbackTimer = null;
      }
    }

    function startFallbackPolling() {
      if (fallbackTimer) return;
      const interval = Math.max(Math.round(1000 / Math.min(fps, 10)), 150);
      fallbackTimer = setInterval(async () => {
        if (!currentTabId || isFetching) return;
        isFetching = true;
        const t0 = performance.now();
        const img = document.getElementById('liveView');
        const testImg = new Image();
        testImg.onload = () => {
          img.src = testImg.src;
          const dt = Math.round(performance.now() - t0);
          document.getElementById('latencyVal').innerText = 'Latency: ' + dt + 'ms';
          isFetching = false;
        };
        testImg.onerror = () => { isFetching = false; };
        testImg.src = '/api/screenshot?tab=' + encodeURIComponent(currentTabId) + '&t=' + Date.now();
      }, interval);
    }

    function setFps(val) {
      fps = parseInt(val, 10);
      document.getElementById('fpsVal').innerText = fps;
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({ action: 'setFps', fps: fps }));
      } else {
        connectWsStream();
      }
    }

    // Remote Control Input Dispatchers
    const imgEl = document.getElementById('liveView');

    function getRelativeCoordinates(event) {
      const rect = imgEl.getBoundingClientRect();
      const naturalW = imgEl.naturalWidth || 1080;
      const naturalH = imgEl.naturalHeight || 1920;

      const clickX = event.clientX - rect.left;
      const clickY = event.clientY - rect.top;

      const scaledX = Math.round((clickX / rect.width) * naturalW);
      const scaledY = Math.round((clickY / rect.height) * naturalH);

      return {
        x: Math.max(0, Math.min(scaledX, naturalW)),
        y: Math.max(0, Math.min(scaledY, naturalH))
      };
    }

    imgEl.addEventListener('click', (e) => {
      if (mode !== 'control') return;
      const coords = getRelativeCoordinates(e);
      sendMouseEvent('click', coords.x, coords.y);
    });

    imgEl.addEventListener('wheel', (e) => {
      if (mode !== 'control') return;
      e.preventDefault();
      const coords = getRelativeCoordinates(e);
      sendScroll(coords.x, coords.y, e.deltaX, e.deltaY);
    }, { passive: false });

    function sendMouseEvent(action, x, y) {
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) {
        fetch('/api/input/mouse?tab=' + encodeURIComponent(currentTabId) + '&action=' + action + '&x=' + x + '&y=' + y, { method: 'POST' });
        return;
      }
      wsStream.send(JSON.stringify({
        action: 'mouse',
        type: action,
        x: x,
        y: y
      }));
    }

    function sendScroll(x, y, dx, dy) {
      if (typeof dx === 'undefined') {
        dx = x;
        dy = y;
        x = 540;
        y = 960;
      }
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) {
        fetch('/api/input/wheel?tab=' + encodeURIComponent(currentTabId) + '&x=' + x + '&y=' + y + '&dx=' + dx + '&dy=' + dy, { method: 'POST' });
        return;
      }
      wsStream.send(JSON.stringify({
        action: 'wheel',
        x: x,
        y: y,
        deltaX: dx,
        deltaY: dy
      }));
    }

    function sendInputText() {
      const input = document.getElementById('typeInput');
      const txt = input.value;
      if (!txt) return;
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) {
        fetch('/api/input/text?tab=' + encodeURIComponent(currentTabId) + '&text=' + encodeURIComponent(txt), { method: 'POST' });
      } else {
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
      if (!wsStream || wsStream.readyState !== WebSocket.OPEN) {
        fetch('/api/input/key?tab=' + encodeURIComponent(currentTabId) + '&key=' + encodeURIComponent(key) + '&code=' + encodeURIComponent(code) + '&keyCode=' + keyCode, { method: 'POST' });
        return;
      }
      wsStream.send(JSON.stringify({
        action: 'keyPress',
        key: key,
        code: code,
        keyCode: keyCode
      }));
    }

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
        alert(data.message || 'Detached ke background (Headless)!');
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

    async function reloadTab() {
      try {
        await fetch('/api/reload?idx=' + currentTabIndex, { method: 'POST' });
      } catch (_) {}
    }

    function copyCurrentUrl() {
      const urlText = document.getElementById('tabUrl').innerText;
      if (urlText && urlText !== '-') {
        navigator.clipboard.writeText(urlText);
        alert('URL disalin ke clipboard!');
      }
    }

    // Auto-refresh tab list every 3s
    setInterval(loadTabs, 3500);

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

    // API: Action Input Mouse (HTTP Fallback)
    if (pathname === '/api/input/mouse') {
        const tabs = await getAllTabs();
        const tabTarget = parsedUrl.query.tab;
        let selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget)) || tabs[0];
        const x = parseInt(parsedUrl.query.x || '0', 10);
        const y = parseInt(parsedUrl.query.y || '0', 10);
        if (selected && selected.wsUrl && WebSocket) {
            try {
                const cdp = new WebSocket(selected.wsUrl);
                cdp.on('open', () => {
                    cdp.send(JSON.stringify({ id: 1, method: 'Input.dispatchMouseEvent', params: { type: 'mousePressed', x, y, button: 'left', clickCount: 1 } }));
                    cdp.send(JSON.stringify({ id: 2, method: 'Input.dispatchMouseEvent', params: { type: 'mouseReleased', x, y, button: 'left', clickCount: 1 } }));
                    setTimeout(() => cdp.close(), 100);
                });
            } catch (_) {}
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: true }));
        return;
    }

    // API: Action Input Wheel (HTTP Fallback)
    if (pathname === '/api/input/wheel') {
        const tabs = await getAllTabs();
        const tabTarget = parsedUrl.query.tab;
        let selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget)) || tabs[0];
        const x = parseInt(parsedUrl.query.x || '540', 10);
        const y = parseInt(parsedUrl.query.y || '960', 10);
        const dx = parseInt(parsedUrl.query.dx || '0', 10);
        const dy = parseInt(parsedUrl.query.dy || '0', 10);
        if (selected && selected.wsUrl && WebSocket) {
            try {
                const cdp = new WebSocket(selected.wsUrl);
                cdp.on('open', () => {
                    cdp.send(JSON.stringify({ id: 1, method: 'Input.dispatchMouseEvent', params: { type: 'mouseWheel', x, y, deltaX: dx, deltaY: dy } }));
                    setTimeout(() => cdp.close(), 100);
                });
            } catch (_) {}
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: true }));
        return;
    }

    // API: Action Input Text (HTTP Fallback)
    if (pathname === '/api/input/text') {
        const tabs = await getAllTabs();
        const tabTarget = parsedUrl.query.tab;
        let selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget)) || tabs[0];
        const text = parsedUrl.query.text || '';
        if (selected && selected.wsUrl && WebSocket) {
            try {
                const cdp = new WebSocket(selected.wsUrl);
                cdp.on('open', () => {
                    cdp.send(JSON.stringify({ id: 1, method: 'Input.insertText', params: { text } }));
                    setTimeout(() => cdp.close(), 100);
                });
            } catch (_) {}
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: true }));
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

// 8. WebSocket 30-60 FPS Screencast & Remote Control Bridge
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
        let fpsRate = parseInt(parsed.query.fps || '30', 10);
        fpsRate = Math.min(Math.max(fpsRate, 1), 60);

        const tabs = await getAllTabs();
        const selected = tabs.find(t => t.id === tabTarget || String(t.index) === String(tabTarget)) || tabs[0];

        if (!selected || !selected.wsUrl) {
            try { clientWs.close(); } catch (_) {}
            return;
        }

        let cdpWs = null;
        let reqId = 1;
        let isClosed = false;
        let frameIntervalTimer = null;
        let inFlight = false;

        try {
            cdpWs = new WebSocket(selected.wsUrl);
        } catch (e) {
            try { clientWs.close(); } catch (_) {}
            return;
        }

        const cleanup = () => {
            isClosed = true;
            if (frameIntervalTimer) clearInterval(frameIntervalTimer);
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

            // Start High-FPS Screencast
            cdpWs.send(JSON.stringify({
                id: reqId++,
                method: 'Page.startScreencast',
                params: {
                    format: 'jpeg',
                    quality: 70,
                    maxWidth: 1080,
                    maxHeight: 1920,
                    everyNthFrame: 1
                }
            }));

            // Active Frame Pusher Loop (30-60 FPS)
            const msInterval = Math.max(Math.round(1000 / fpsRate), 16);
            frameIntervalTimer = setInterval(() => {
                if (isClosed || inFlight) return;
                inFlight = true;
                const shotId = reqId++;
                try {
                    cdpWs.send(JSON.stringify({
                        id: shotId,
                        method: 'Page.captureScreenshot',
                        params: { format: 'jpeg', quality: 65, fromSurface: false, captureBeyondViewport: true }
                    }));
                } catch (_) {
                    inFlight = false;
                }
            }, msInterval);
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
                } else if (msg.result && msg.result.data) {
                    inFlight = false;
                    const imgBuf = Buffer.from(msg.result.data, 'base64');
                    if (clientWs.readyState === WebSocket.OPEN) {
                        clientWs.send(imgBuf);
                    }
                } else {
                    inFlight = false;
                }
            } catch (_) {
                inFlight = false;
            }
        });

        // Client Input Handler (Remote Control Takeover)
        clientWs.on('message', (raw) => {
            if (isClosed || !cdpWs || cdpWs.readyState !== WebSocket.OPEN) return;
            try {
                const ev = JSON.parse(raw.toString());
                if (ev.action === 'setFps') {
                    fpsRate = Math.min(Math.max(parseInt(ev.fps || '30', 10), 1), 60);
                    if (frameIntervalTimer) clearInterval(frameIntervalTimer);
                    const msInterval = Math.max(Math.round(1000 / fpsRate), 16);
                    frameIntervalTimer = setInterval(() => {
                        if (isClosed || inFlight) return;
                        inFlight = true;
                        try {
                            cdpWs.send(JSON.stringify({
                                id: reqId++,
                                method: 'Page.captureScreenshot',
                                params: { format: 'jpeg', quality: 65, fromSurface: false, captureBeyondViewport: true }
                            }));
                        } catch (_) { inFlight = false; }
                    }, msInterval);
                } else if (ev.action === 'mouse') {
                    const x = ev.x || 0;
                    const y = ev.y || 0;
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: { type: 'mousePressed', x, y, button: 'left', clickCount: 1 }
                    }));
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: { type: 'mouseReleased', x, y, button: 'left', clickCount: 1 }
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
                } else if (ev.action === 'keyPress') {
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchKeyEvent',
                        params: { type: 'keyDown', key: ev.key, code: ev.code, windowsVirtualKeyCode: ev.keyCode }
                    }));
                    cdpWs.send(JSON.stringify({
                        id: reqId++,
                        method: 'Input.dispatchKeyEvent',
                        params: { type: 'keyUp', key: ev.key, code: ev.code, windowsVirtualKeyCode: ev.keyCode }
                    }));
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
    console.log('\x1b[36m│\x1b[0m  \x1b[1mSpeed\x1b[0m     : \x1b[35m30-60 FPS Zero-Lag Binary WebSocket\x1b[0m    \x1b[36m│\x1b[0m');
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
