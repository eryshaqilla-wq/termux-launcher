#!/usr/bin/env node

/**
 * ESO WEBVIEW LIVE MONITOR (Watch Mode Daemon)
 * Memantau aktivitas WebView Termux secara real-time via Web/WLAN (0.0.0.0:9223).
 * Menampilkan preview visual stream (Headless & Visual), info tab, profile, dan viewport.
 * Pure Read-Only Watch Mode: Aman, tidak mengganggu automasi/bot yang sedang berjalan.
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

const PORT = parseInt(process.argv[2] || process.env.MONITOR_PORT || '9223', 10);
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

// 4. Capture screenshot via CDP WebSocket
function captureCdpScreenshot(wsUrl, quality = 70) {
    return new Promise((resolve, reject) => {
        if (!WebSocket) {
            return reject(new Error('WebSocket module (ws) tidak tersedia'));
        }
        if (!wsUrl) {
            return reject(new Error('WebSocket Debugger URL tidak tersedia'));
        }

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
            ws.send(JSON.stringify({
                id: 101,
                method: 'Page.captureScreenshot',
                params: {
                    format: 'jpeg',
                    quality: Math.min(Math.max(quality, 30), 90)
                }
            }));
        });

        ws.on('message', (msg) => {
            clearTimeout(timer);
            try {
                const res = JSON.parse(msg.toString());
                if (res.id === 101) {
                    if (res.result && res.result.data) {
                        const imgBuf = Buffer.from(res.result.data, 'base64');
                        ws.close();
                        return resolve(imgBuf);
                    }
                    if (res.error) {
                        ws.close();
                        return reject(new Error(res.error.message || 'CDP Error'));
                    }
                }
            } catch (err) {
                try { ws.close(); } catch (_) {}
                reject(err);
            }
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
      <text x="540" y="1200" font-family="monospace" font-size="24" fill="#6c7086" text-anchor="middle">web &lt;url&gt;  |  web --headless &lt;url&gt;</text>
    </svg>
    `.trim());
}

// 6. Template HTML Single Page Application (Catppuccin Mocha Dark UI)
function getDashboardHtml(reqHost) {
    return `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ESO WebView Live Monitor (Watch Mode)</title>
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
      padding: 12px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      flex-wrap: wrap;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand h1 {
      font-size: 1.15rem;
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
    .watch-badge {
      background: rgba(148, 226, 213, 0.15);
      color: var(--teal);
      border: 1px solid rgba(148, 226, 213, 0.3);
      font-size: 0.72rem;
      padding: 3px 8px;
      border-radius: 999px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.6px;
    }
    .header-controls {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    select, button {
      background: var(--surface);
      color: var(--text);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 8px 14px;
      font-size: 0.85rem;
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
    button.primary:hover {
      opacity: 0.9;
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
      padding: 20px;
      gap: 16px;
    }
    .monitor-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      overflow: hidden;
      box-shadow: 0 10px 30px rgba(0,0,0,0.4);
      display: flex;
      flex-direction: column;
      max-width: 1000px;
      width: 100%;
    }
    .card-bar {
      background: var(--surface);
      border-bottom: 1px solid var(--border);
      padding: 10px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      font-size: 0.85rem;
    }
    .url-chip {
      color: var(--subtext);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 500px;
      font-family: monospace;
      font-size: 0.8rem;
    }
    .badge-group {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .badge {
      font-size: 0.7rem;
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
      max-height: 75vh;
      overflow: hidden;
    }
    .viewer-area img {
      max-width: 100%;
      max-height: 75vh;
      object-fit: contain;
      display: block;
      transition: opacity 0.15s;
    }
    .hud-overlay {
      position: absolute;
      bottom: 12px;
      left: 12px;
      right: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      pointer-events: none;
    }
    .hud-pill {
      background: rgba(17, 17, 27, 0.85);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 6px 12px;
      border-radius: 999px;
      font-size: 0.75rem;
      color: var(--text);
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .toolbar {
      padding: 12px 16px;
      background: var(--surface);
      border-top: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
    }
    .tool-group {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .info-footer {
      max-width: 1000px;
      width: 100%;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 16px;
      font-size: 0.8rem;
      color: var(--subtext);
    }
    .info-footer h3 {
      color: var(--blue);
      font-size: 0.9rem;
      margin-bottom: 8px;
    }
    .cmd-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 8px;
      margin-top: 8px;
    }
    .cmd-box {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 8px 12px;
      border-radius: 6px;
      font-family: monospace;
      color: var(--green);
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="live-dot"></div>
      <h1>ESO WebView Monitor</h1>
      <span class="watch-badge">👁️ Read-Only Watch Mode</span>
    </div>
    <div class="header-controls">
      <select id="tabSelector" onchange="changeTab(this.value)">
        <option value="">Memuat tab...</option>
      </select>
      <select id="fpsSelector" onchange="setFps(this.value)">
        <option value="4">4 FPS (Fluid)</option>
        <option value="2" selected>2 FPS (Normal)</option>
        <option value="1">1 FPS (Eco)</option>
        <option value="0">Paused</option>
      </select>
      <button onclick="takeSnapshot()">📸 Snapshot</button>
      <button class="primary" onclick="toggleAttach()">👁️ Attach to Screen</button>
      <button class="warning" onclick="toggleDetach()">🕶️ Detach (Headless)</button>
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
      
      <div class="viewer-area">
        <img id="liveView" src="/api/placeholder.svg" alt="WebView Live Stream">
        <div class="hud-overlay">
          <div class="hud-pill">
            <span>FPS: <strong id="fpsVal">2</strong></span>
            <span>•</span>
            <span id="latencyVal">Latency: 45ms</span>
          </div>
          <div class="hud-pill">
            <span id="profileVal">Profile: default</span>
          </div>
        </div>
      </div>

      <div class="toolbar">
        <div class="tool-group">
          <span style="font-size:0.8rem; color:var(--subtext);">Watch Mode aktif di LAN: <strong>http://${reqHost}</strong></span>
        </div>
        <div class="tool-group">
          <button onclick="reloadTab()">🔄 Reload Halaman</button>
          <button onclick="copyCurrentUrl()">📋 Salin URL</button>
        </div>
      </div>
    </div>

    <div class="info-footer">
      <h3>🚀 Cheatsheet Kontrol Terminal (Zsh):</h3>
      <div class="cmd-grid">
        <div class="cmd-box">webview --list [--json]</div>
        <div class="cmd-box">webview --attach &lt;idx&gt;</div>
        <div class="cmd-box">webview --detach &lt;idx&gt;</div>
        <div class="cmd-box">webview --kill [idx]</div>
        <div class="cmd-box">webview --kill-all</div>
        <div class="cmd-box">webview --monitor [port]</div>
        <div class="cmd-box">webview --headless &lt;url&gt;</div>
        <div class="cmd-box">webview --desktop &lt;url&gt;</div>
      </div>
    </div>
  </main>

  <script>
    let currentTabId = '';
    let currentTabIndex = 0;
    let tabsList = [];
    let fps = 2;
    let streamTimer = null;
    let isFetching = false;

    async function loadTabs() {
      try {
        const res = await fetch('/api/tabs');
        tabsList = await res.json();
        const sel = document.getElementById('tabSelector');
        sel.innerHTML = '';
        if (tabsList.length === 0) {
          sel.innerHTML = '<option value="">Tidak ada WebView aktif</option>';
          document.getElementById('tabTitle').innerText = 'Tidak ada WebView aktif';
          document.getElementById('tabUrl').innerText = 'Buka webview via terminal: web <url>';
          document.getElementById('liveView').src = '/api/placeholder.svg';
          return;
        }

        tabsList.forEach(t => {
          const opt = document.createElement('option');
          opt.value = t.id || t.index;
          const mode = t.isHeadless ? '[HEADLESS]' : '[VISUAL]';
          const view = t.isDesktop ? '[DESKTOP]' : '[MOBILE]';
          opt.innerText = '#' + t.index + ' ' + mode + ' ' + view + ' ' + (t.title || 'Tab');
          if (t.active && !currentTabId) {
            opt.selected = true;
            currentTabId = opt.value;
            currentTabIndex = t.index;
          }
          sel.appendChild(opt);
        });

        if (!currentTabId && tabsList.length > 0) {
          currentTabId = tabsList[0].id || tabsList[0].index;
          currentTabIndex = tabsList[0].index;
        }

        updateTabMetadata();
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
      if (tab) currentTabIndex = tab.index;
      updateTabMetadata();
      takeSnapshot();
    }

    async function takeSnapshot() {
      if (!currentTabId) return;
      const t0 = performance.now();
      const img = document.getElementById('liveView');
      try {
        const url = '/api/screenshot?tab=' + encodeURIComponent(currentTabId) + '&t=' + Date.now();
        const testImg = new Image();
        testImg.onload = () => {
          img.src = testImg.src;
          const dt = Math.round(performance.now() - t0);
          document.getElementById('latencyVal').innerText = 'Latency: ' + dt + 'ms';
        };
        testImg.src = url;
      } catch (_) {}
    }

    function startStream() {
      if (streamTimer) clearInterval(streamTimer);
      if (fps <= 0) return;
      const interval = Math.round(1000 / fps);
      streamTimer = setInterval(async () => {
        if (isFetching) return;
        isFetching = true;
        await takeSnapshot();
        isFetching = false;
      }, interval);
    }

    function setFps(val) {
      fps = parseInt(val, 10);
      document.getElementById('fpsVal').innerText = fps;
      startStream();
    }

    async function toggleAttach() {
      try {
        const res = await fetch('/api/attach?idx=' + currentTabIndex, { method: 'POST' });
        const data = await res.json();
        alert(data.message || 'Attached ke layar!');
        setTimeout(loadTabs, 1000);
      } catch (e) {
        alert('Gagal attach: ' + e.message);
      }
    }

    async function toggleDetach() {
      try {
        const res = await fetch('/api/detach?idx=' + currentTabIndex, { method: 'POST' });
        const data = await res.json();
        alert(data.message || 'Detached ke background (Headless)!');
        setTimeout(loadTabs, 1000);
      } catch (e) {
        alert('Gagal detach: ' + e.message);
      }
    }

    async function reloadTab() {
      try {
        await fetch('/api/reload?idx=' + currentTabIndex, { method: 'POST' });
        setTimeout(takeSnapshot, 1000);
      } catch (_) {}
    }

    function copyCurrentUrl() {
      const urlText = document.getElementById('tabUrl').innerText;
      if (urlText && urlText !== '-') {
        navigator.clipboard.writeText(urlText);
        alert('URL disalin ke clipboard!');
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

    // Auto-refresh tabs list every 3s
    setInterval(loadTabs, 3000);

    // Initial load
    loadTabs().then(() => {
      startStream();
    });
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
        res.end(getDashboardHtml(reqHost));
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

    // API: Screenshot single frame
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

server.listen(PORT, '0.0.0.0', () => {
    ensurePortForward();
    const ips = getNetworkIps();
    console.log('\n\x1b[36m┌────────────────────────────────────────────────────────┐\x1b[0m');
    console.log('\x1b[36m│\x1b[0m   \x1b[1m\x1b[32m🛸 ESO WEBVIEW LIVE MONITOR (WATCH MODE)\x1b[0m              \x1b[36m│\x1b[0m');
    console.log('\x1b[36m├────────────────────────────────────────────────────────┤\x1b[0m');
    console.log(`\x1b[36m│\x1b[0m  \x1b[1mLocal URL\x1b[0m : \x1b[33mhttp://127.0.0.1:${PORT}\x1b[0m                          \x1b[36m│\x1b[0m`);
    ips.forEach(i => {
        const line = `  WLAN (${i.iface}) : http://${i.ip}:${PORT}`;
        console.log(`\x1b[36m│\x1b[0m  \x1b[1m${line.padEnd(52)}\x1b[0m\x1b[36m│\x1b[0m`);
    });
    console.log('\x1b[36m│\x1b[0m  \x1b[1mMode\x1b[0m      : \x1b[32mPure Read-Only Watch Mode\x1b[0m              \x1b[36m│\x1b[0m');
    console.log('\x1b[36m│\x1b[0m  \x1b[1mTarget\x1b[0m    : \x1b[35mHeadless & Normal WebView Tabs\x1b[0m         \x1b[36m│\x1b[0m');
    console.log('\x1b[36m└────────────────────────────────────────────────────────┘\x1b[0m\n');
    console.log('Tekan Ctrl + C untuk menghentikan monitor.\n');
});
