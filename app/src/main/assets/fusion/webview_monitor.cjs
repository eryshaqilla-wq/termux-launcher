#!/usr/bin/env node

/**
 * ESO WEBVIEW LIVE MONITOR & REMOTE CONTROL (WLAN 30-60 FPS)
 * Arsitektur Scrcpy Pro: Singleton CDP Hub, Zero-Lag Event-Driven Screencast (0% Idle CPU).
 * UI Pro Catppuccin Mocha dengan Mobile Device Frame, Touch Ripple, Gestures & Direct Keyboard.
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

// 4. Placeholder SVG generator saat standby
function generatePlaceholderSvg(text, subtext) {
    return Buffer.from(`
    <svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#0b0b12"/>
          <stop offset="50%" stop-color="#11111b"/>
          <stop offset="100%" stop-color="#181825"/>
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="url(#bg)"/>
      <circle cx="540" cy="820" r="140" fill="#1e1e2e" stroke="#89b4fa" stroke-width="3" stroke-dasharray="10 6"/>
      <path d="M 470 820 Q 540 730 610 820 Q 540 910 470 820 Z" fill="none" stroke="#89b4fa" stroke-width="6"/>
      <circle cx="540" cy="820" r="32" fill="#a6e3a1"/>
      <text x="540" y="1040" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="40" font-weight="700" fill="#cdd6f4" text-anchor="middle">${text}</text>
      <text x="540" y="1100" font-family="-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif" font-size="26" fill="#a6adc8" text-anchor="middle">${subtext}</text>
      <text x="540" y="1180" font-family="monospace" font-size="22" fill="#6c7086" text-anchor="middle">web --headless &lt;url&gt;  |  web &lt;url&gt;</text>
    </svg>
    `.trim());
}

// 5. Template HTML Single Page Application (Scrcpy Pro UI)
function getDashboardHtml(reqHost, isDefaultControl) {
    return `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>ESO Scrcpy Web Controller</title>
  <style>
    :root {
      --bg: #09090e;
      --card: #11111a;
      --card-elevated: #161622;
      --surface: #1e1e2e;
      --surface-hover: #262638;
      --border: rgba(255, 255, 255, 0.08);
      --border-active: #89b4fa;
      --text: #cdd6f4;
      --subtext: #9399b2;
      --overlay: #6c7086;
      --blue: #89b4fa;
      --blue-glow: rgba(137, 180, 250, 0.35);
      --green: #a6e3a1;
      --green-glow: rgba(166, 227, 161, 0.4);
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
      user-select: none;
    }
    
    /* Top App Bar */
    header {
      background: var(--card);
      border-bottom: 1px solid var(--border);
      padding: 8px 18px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
      z-index: 100;
      backdrop-filter: blur(12px);
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand-logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 800;
      font-size: 1.05rem;
      letter-spacing: 0.5px;
      color: var(--blue);
    }
    .live-dot {
      width: 9px;
      height: 9px;
      background: var(--green);
      border-radius: 50%;
      box-shadow: 0 0 10px var(--green-glow);
      animation: pulse 1.8s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(0.9); opacity: 0.7; }
      50% { transform: scale(1.25); opacity: 1; box-shadow: 0 0 14px var(--green); }
      100% { transform: scale(0.9); opacity: 0.7; }
    }

    /* Mode Segmented Switch */
    .mode-switch {
      display: flex;
      align-items: center;
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 20px;
      padding: 2px;
    }
    .mode-btn {
      padding: 5px 14px;
      font-size: 0.78rem;
      font-weight: 700;
      cursor: pointer;
      border: none;
      background: transparent;
      color: var(--subtext);
      border-radius: 18px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .mode-btn.active.watch {
      background: rgba(148, 226, 213, 0.18);
      color: var(--teal);
    }
    .mode-btn.active.control {
      background: rgba(250, 179, 135, 0.22);
      color: var(--peach);
      box-shadow: 0 2px 8px rgba(250, 179, 135, 0.25);
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    select, button.btn {
      background: var(--surface);
      color: var(--text);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 6px 12px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      outline: none;
      transition: all 0.18s ease;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
    select:hover, button.btn:hover {
      background: var(--surface-hover);
      border-color: var(--border-active);
    }
    button.btn-primary {
      background: var(--blue);
      color: #0b0b12;
      border: none;
      font-weight: 700;
    }
    button.btn-primary:hover {
      background: #b4befe;
      box-shadow: 0 0 14px var(--blue-glow);
    }
    button.btn-warning {
      background: rgba(250, 179, 135, 0.15);
      color: var(--peach);
      border-color: rgba(250, 179, 135, 0.35);
    }
    button.btn-danger {
      background: rgba(243, 139, 168, 0.15);
      color: var(--red);
      border-color: rgba(243, 139, 168, 0.35);
    }
    button.btn-danger:hover {
      background: var(--red);
      color: #0b0b12;
    }

    /* Main Workspace */
    main {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      padding: 14px 10px;
      gap: 12px;
    }

    /* Scrcpy Device Stage */
    .device-stage {
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      max-width: 980px;
    }

    /* Phone Mockup Frame */
    .phone-mockup {
      position: relative;
      background: #000;
      border-radius: 28px;
      padding: 10px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.85), 0 0 0 1px rgba(255,255,255,0.1);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      width: 100%;
      max-width: 480px;
      transition: max-width 0.3s ease;
    }
    .phone-mockup.desktop-view {
      max-width: 960px;
      border-radius: 16px;
    }

    /* Status Notch Bar */
    .device-notch-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 4px 14px 8px 14px;
      font-size: 0.72rem;
      color: var(--subtext);
      background: #000;
      z-index: 10;
    }
    .device-notch-bar .title-chip {
      color: var(--text);
      font-weight: 700;
      max-width: 220px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .device-notch-bar .url-badge {
      font-family: monospace;
      font-size: 0.68rem;
      color: var(--overlay);
    }

    /* Display Screen Canvas */
    .screen-viewport {
      position: relative;
      background: #000;
      border-radius: 18px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 480px;
      max-height: 74vh;
      outline: none;
      touch-action: none;
      cursor: default;
    }
    .screen-viewport.control-active {
      cursor: crosshair;
    }
    .screen-viewport img {
      max-width: 100%;
      max-height: 74vh;
      object-fit: contain;
      display: block;
      touch-action: none;
      pointer-events: auto;
    }

    /* Visual Touch Ripple (Scrcpy Show Touches) */
    .touch-dot {
      position: absolute;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: rgba(137, 180, 250, 0.4);
      border: 2px solid var(--blue);
      transform: translate(-50%, -50%) scale(0.6);
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.15s ease, transform 0.12s ease;
      z-index: 50;
      box-shadow: 0 0 12px var(--blue-glow);
    }
    .touch-dot.active {
      opacity: 1;
      transform: translate(-50%, -50%) scale(1);
    }

    /* HUD Metrics Overlay */
    .hud-metrics {
      position: absolute;
      top: 12px;
      left: 12px;
      right: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      pointer-events: none;
      z-index: 25;
    }
    .hud-chip {
      background: rgba(15, 15, 23, 0.85);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 255, 255, 0.12);
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 0.72rem;
      color: var(--text);
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .hud-chip.fps-meter {
      font-weight: 700;
      color: var(--green);
    }

    /* Scrcpy Bottom Virtual Dock */
    .scrcpy-dock {
      position: absolute;
      bottom: 14px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(17, 17, 26, 0.88);
      backdrop-filter: blur(16px);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 26px;
      padding: 4px 8px;
      display: flex;
      align-items: center;
      gap: 4px;
      z-index: 30;
      box-shadow: 0 8px 30px rgba(0,0,0,0.6);
      transition: opacity 0.25s ease, transform 0.25s ease;
    }
    .dock-btn {
      background: transparent;
      border: none;
      color: var(--text);
      padding: 7px 11px;
      border-radius: 18px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      transition: all 0.15s ease;
    }
    .dock-btn:hover {
      background: rgba(255, 255, 255, 0.12);
      color: var(--blue);
      transform: translateY(-1px);
    }
    .dock-btn:active {
      transform: translateY(1px);
    }
    .dock-btn.highlight {
      background: var(--blue);
      color: #0b0b12;
      font-weight: 700;
    }

    /* Quick Keyboard Input Drawer */
    .keyboard-drawer {
      background: var(--card);
      border-top: 1px solid var(--border);
      padding: 10px 14px;
      border-radius: 0 0 20px 20px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      z-index: 15;
    }
    .drawer-row {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .type-input {
      flex: 1;
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 7px 12px;
      color: var(--text);
      font-size: 0.82rem;
      outline: none;
    }
    .type-input:focus {
      border-color: var(--blue);
    }
    .shortcut-row {
      display: flex;
      align-items: center;
      gap: 5px;
      flex-wrap: wrap;
    }
    .chip-btn {
      background: var(--surface);
      border: 1px solid var(--border);
      color: var(--subtext);
      padding: 4px 9px;
      border-radius: 6px;
      font-size: 0.72rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s;
    }
    .chip-btn:hover {
      background: var(--surface-hover);
      color: var(--text);
      border-color: var(--blue);
    }

    /* Fullscreen Immersion */
    .phone-mockup:fullscreen,
    .phone-mockup:-webkit-full-screen {
      max-width: 100vw !important;
      max-height: 100vh !important;
      width: 100vw !important;
      height: 100vh !important;
      border-radius: 0 !important;
      padding: 0 !important;
      background: #000 !important;
    }
    .phone-mockup:fullscreen .screen-viewport,
    .phone-mockup:-webkit-full-screen .screen-viewport {
      height: 100vh !important;
      max-height: 100vh !important;
      border-radius: 0 !important;
    }
    .phone-mockup:fullscreen .device-notch-bar,
    .phone-mockup:fullscreen .keyboard-drawer {
      display: none !important;
    }
    .phone-mockup:fullscreen .scrcpy-dock {
      bottom: 20px;
    }
    .phone-mockup.hud-hidden .scrcpy-dock,
    .phone-mockup.hud-hidden .hud-metrics {
      opacity: 0;
      pointer-events: none;
    }

    /* Quick Cheatsheet Footer */
    .stage-footer {
      max-width: 980px;
      width: 100%;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 12px 16px;
      font-size: 0.78rem;
      color: var(--subtext);
    }
    .stage-footer h4 {
      color: var(--blue);
      font-size: 0.84rem;
      margin-bottom: 6px;
    }
    .shortcut-list {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      font-size: 0.75rem;
    }
    .kbd-tag {
      background: var(--surface);
      border: 1px solid var(--border);
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      color: var(--green);
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="brand-logo">
        <div class="live-dot"></div>
        <span>ESO SCRCPY PRO</span>
      </div>
      <div class="mode-switch">
        <button id="btnWatch" class="mode-btn active watch" onclick="setMode('watch')">👁️ Spectator</button>
        <button id="btnControl" class="mode-btn control" onclick="setMode('control')">🎮 Takeover</button>
      </div>
    </div>

    <div class="header-actions">
      <select id="tabSelector" onchange="changeTab(this.value)">
        <option value="">Memuat tab...</option>
      </select>
      <button class="btn btn-primary" onclick="toggleFullscreen()" id="btnFullTop">⛶ Fullscreen</button>
      <button class="btn btn-warning" onclick="toggleAttach()">👁️ Attach</button>
      <button class="btn" onclick="toggleDetach()">🕶️ Detach</button>
      <button class="btn btn-danger" onclick="killCurrentTab()">❌ Kill</button>
    </div>
  </header>

  <main>
    <div class="device-stage">
      <div class="phone-mockup" id="phoneMockup">
        
        <!-- Status Bar -->
        <div class="device-notch-bar">
          <div style="display:flex; align-items:center; gap:8px; min-width:0;">
            <span class="title-chip" id="tabTitle">Menunggu WebView...</span>
            <span id="badgeMode" style="font-size:0.65rem; padding:1px 6px; border-radius:4px; background:rgba(203,166,247,0.2); color:var(--mauve); font-weight:700;">HEADLESS</span>
          </div>
          <span class="url-badge" id="tabUrl">-</span>
        </div>

        <!-- Touch Screen Viewport -->
        <div class="screen-viewport" id="screenViewport" tabindex="0">
          <img id="liveView" src="/api/placeholder.svg" alt="WebView Live Stream" draggable="false">
          
          <!-- Visual Touch Indicator -->
          <div class="touch-dot" id="touchDot"></div>

          <!-- Floating HUD Metrics -->
          <div class="hud-metrics" id="hudMetrics">
            <div class="hud-chip fps-meter">
              <span id="fpsVal">--</span> FPS • <span id="pingVal">4ms</span>
            </div>
            <div class="hud-chip" id="controlStateChip" style="color:var(--teal);">
              👁️ Watch Mode
            </div>
          </div>

          <!-- Bottom Navigation Dock -->
          <div class="scrcpy-dock" id="scrcpyDock">
            <button class="dock-btn" onclick="sendNav('back')" title="Kembali (Klik Kanan mouse juga berfungsi sebagai Back)">◀ Back</button>
            <button class="dock-btn" onclick="sendNav('home')" title="Beranda (Klik Tengah mouse)">◯ Home</button>
            <button class="dock-btn" onclick="sendNav('reload')" title="Muat Ulang Halaman">🔄 Reload</button>
            <button class="dock-btn" onclick="toggleKeyboardDrawer()" title="Keyboard Input Drawer">⌨ Ketik</button>
            <button class="dock-btn" onclick="pasteClipboard()" title="Paste Clipboard">📋 Paste</button>
            <button class="dock-btn highlight" onclick="toggleFullscreen()" title="Layar Penuh">⛶ Full</button>
          </div>
        </div>

        <!-- Keyboard Drawer -->
        <div class="keyboard-drawer" id="keyboardDrawer" style="display:none;">
          <div class="drawer-row">
            <input type="text" id="typeInput" class="type-input" placeholder="Ketik teks di sini lalu tekan Enter..." onkeydown="handleInputKey(event)">
            <button class="btn btn-primary" onclick="sendInputText()">⏎ Kirim</button>
          </div>
          <div class="shortcut-row">
            <button class="chip-btn" onclick="sendSpecialKey('Enter', 'Enter', 13)">⏎ Enter</button>
            <button class="chip-btn" onclick="sendSpecialKey('Backspace', 'Backspace', 8)">⌫ Del</button>
            <button class="chip-btn" onclick="sendSpecialKey('Tab', 'Tab', 9)">⇥ Tab</button>
            <button class="chip-btn" onclick="sendSpecialKey('Escape', 'Escape', 27)">⎋ Esc</button>
            <button class="chip-btn" onclick="sendScrollDelta(0, -350)">⬆ Scroll Up</button>
            <button class="chip-btn" onclick="sendScrollDelta(0, 350)">⬇ Scroll Down</button>
            <button class="chip-btn" onclick="toggleDesktopView()">🖥️ Switch Viewport</button>
          </div>
        </div>

      </div>
    </div>

    <!-- Cheatsheet & Gestures Info -->
    <div class="stage-footer">
      <h4>⚡ Scrcpy Gestur & Kontrol Native:</h4>
      <div class="shortcut-list">
        <span><span class="kbd-tag">Drag / Swipe</span> Geser layar</span>
        <span><span class="kbd-tag">Klik Kanan</span> Tombol Back (Kembali)</span>
        <span><span class="kbd-tag">Klik Tengah</span> Tombol Home</span>
        <span><span class="kbd-tag">Scroll Wheel</span> Scroll halaman</span>
        <span><span class="kbd-tag">Keyboard Fisik</span> Ketik langsung ke input aktif</span>
        <span><span class="kbd-tag">F11</span> Toggle Fullscreen</span>
      </div>
    </div>
  </main>

  <script>
    let currentTabId = '';
    let currentTabIndex = 0;
    let currentWsUrl = '';
    let tabsList = [];
    let mode = ${isDefaultControl ? "'control'" : "'watch'"};
    let wsStream = null;
    let isConnected = false;
    let hudTimer = null;

    // Measured FPS calculation
    let framesCounter = 0;
    let lastFpsStamp = performance.now();

    // DOM Elements
    const imgEl = document.getElementById('liveView');
    const viewport = document.getElementById('screenViewport');
    const mockup = document.getElementById('phoneMockup');
    const touchDot = document.getElementById('touchDot');
    const kbdDrawer = document.getElementById('keyboardDrawer');

    // 1. Load Tabs (Does NOT disconnect active WebSocket)
    async function loadTabs() {
      try {
        const res = await fetch('/api/tabs');
        const list = await res.json();
        if (!Array.isArray(list)) return;
        tabsList = list;

        const sel = document.getElementById('tabSelector');
        const prevVal = sel.value;
        sel.innerHTML = '';

        if (tabsList.length === 0) {
          sel.innerHTML = '<option value="">Tidak ada WebView aktif</option>';
          document.getElementById('tabTitle').innerText = 'Tidak ada WebView aktif';
          document.getElementById('tabUrl').innerText = 'Buka webview via terminal: web <url>';
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
          sel.appendChild(opt);
        });

        if (prevVal && tabsList.some(t => (t.id || t.index) == prevVal)) {
          sel.value = prevVal;
        } else if (!currentTabId && tabsList.length > 0) {
          currentTabId = tabsList[0].id || tabsList[0].index;
          currentTabIndex = tabsList[0].index;
          currentWsUrl = tabsList[0].wsUrl;
          sel.value = currentTabId;
          updateTabMetadata();
          connectWsStream();
        } else if (currentTabId) {
          sel.value = currentTabId;
          updateTabMetadata();
        }
      } catch (e) {
        console.warn('Gagal memuat tabs:', e);
      }
    }

    function updateTabMetadata() {
      const tab = tabsList.find(t => (t.id || t.index) == currentTabId) || tabsList[0];
      if (!tab) return;
      document.getElementById('tabTitle').innerText = tab.title || 'WebView Tab';
      document.getElementById('tabUrl').innerText = tab.url || 'about:blank';
      
      const badge = document.getElementById('badgeMode');
      if (tab.isHeadless) {
        badge.innerText = 'HEADLESS';
        badge.style.background = 'rgba(203,166,247,0.2)';
        badge.style.color = 'var(--mauve)';
      } else {
        badge.innerText = 'VISUAL';
        badge.style.background = 'rgba(166,227,161,0.2)';
        badge.style.color = 'var(--green)';
      }

      if (tab.isDesktop) {
        mockup.classList.add('desktop-view');
      } else {
        mockup.classList.remove('desktop-view');
      }
    }

    function changeTab(val) {
      if (val === currentTabId) return;
      currentTabId = val;
      const tab = tabsList.find(t => (t.id || t.index) == val);
      if (tab) {
        currentTabIndex = tab.index;
        currentWsUrl = tab.wsUrl;
      }
      updateTabMetadata();
      connectWsStream(true);
    }

    function setMode(newMode) {
      mode = newMode;
      const btnW = document.getElementById('btnWatch');
      const btnC = document.getElementById('btnControl');
      const chip = document.getElementById('controlStateChip');

      if (mode === 'control') {
        btnW.className = 'mode-btn';
        btnC.className = 'mode-btn active control';
        viewport.classList.add('control-active');
        chip.innerText = '🎮 Takeover Active';
        chip.style.color = 'var(--peach)';
        kbdDrawer.style.display = 'flex';
        viewport.focus();
      } else {
        btnW.className = 'mode-btn active watch';
        btnC.className = 'mode-btn';
        viewport.classList.remove('control-active');
        chip.innerText = '👁️ Spectator Mode';
        chip.style.color = 'var(--teal)';
        kbdDrawer.style.display = 'none';
      }
    }

    // 2. Persistent WebSocket Screencast Connection
    function connectWsStream(force = false) {
      if (!force && wsStream && (wsStream.readyState === WebSocket.OPEN || wsStream.readyState === WebSocket.CONNECTING)) {
        return;
      }
      stopWsStream();
      if (!currentTabId) return;

      const wsProto = location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = wsProto + '//' + location.host + '/ws?tab=' + encodeURIComponent(currentTabId);

      try {
        wsStream = new WebSocket(wsUrl);
        wsStream.binaryType = 'blob';

        wsStream.onopen = () => {
          isConnected = true;
          document.getElementById('pingVal').innerText = '0ms';
        };

        wsStream.onmessage = (event) => {
          if (event.data instanceof Blob) {
            const blobUrl = URL.createObjectURL(event.data);
            imgEl.onload = () => {
              URL.revokeObjectURL(blobUrl);
            };
            imgEl.src = blobUrl;

            // Frame rate counter
            framesCounter++;
            const now = performance.now();
            if (now - lastFpsStamp >= 1000) {
              const measured = Math.round((framesCounter * 1000) / (now - lastFpsStamp));
              document.getElementById('fpsVal').innerText = measured;
              framesCounter = 0;
              lastFpsStamp = now;
            }
          }
        };

        wsStream.onclose = () => {
          isConnected = false;
          document.getElementById('fpsVal').innerText = '--';
          setTimeout(() => {
            if (!isConnected) connectWsStream();
          }, 1500);
        };

        wsStream.onerror = () => {
          setTimeout(() => {
            if (!isConnected) connectWsStream();
          }, 1500);
        };
      } catch (e) {
        setTimeout(connectWsStream, 1500);
      }
    }

    function stopWsStream() {
      if (wsStream) {
        try { wsStream.close(); } catch (_) {}
        wsStream = null;
      }
      isConnected = false;
    }

    // 3. Pixel-Perfect Letterbox Calculation
    function getRenderBox() {
      const rect = imgEl.getBoundingClientRect();
      const naturalW = imgEl.naturalWidth || 1080;
      const naturalH = imgEl.naturalHeight || 1920;
      const naturalAspect = naturalW / naturalH;
      const elementAspect = rect.width / rect.height;

      let renderW, renderH, offsetX, offsetY;
      if (elementAspect > naturalAspect) {
        renderH = rect.height;
        renderW = renderH * naturalAspect;
        offsetX = (rect.width - renderW) / 2;
        offsetY = 0;
      } else {
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

    function toDeviceCoords(clientX, clientY) {
      const box = getRenderBox();
      const relX = clientX - box.left;
      const relY = clientY - box.top;
      const x = Math.round((relX / box.width) * box.naturalW);
      const y = Math.round((relY / box.height) * box.naturalH);

      return {
        x: Math.max(0, Math.min(x, box.naturalW)),
        y: Math.max(0, Math.min(y, box.naturalH)),
        screenRelX: clientX - viewport.getBoundingClientRect().left,
        screenRelY: clientY - viewport.getBoundingClientRect().top
      };
    }

    // 4. Pointer Gestures with Visual Touch Feedback
    let isPointerActive = false;
    let pointerId = null;

    viewport.addEventListener('pointerdown', (e) => {
      if (mode !== 'control') return;
      e.preventDefault();
      viewport.focus();

      // Right Click -> Android Back
      if (e.button === 2) {
        sendNav('back');
        return;
      }
      // Middle Click -> Android Home
      if (e.button === 1) {
        sendNav('home');
        return;
      }

      isPointerActive = true;
      pointerId = e.pointerId;
      try { viewport.setPointerCapture(e.pointerId); } catch (_) {}

      const coords = toDeviceCoords(e.clientX, e.clientY);
      updateTouchDot(coords.screenRelX, coords.screenRelY, true);

      sendPointer('pointerDown', coords.x, coords.y, 'left', e.buttons || 1);
    });

    viewport.addEventListener('pointermove', (e) => {
      if (mode !== 'control' || !isPointerActive) return;
      e.preventDefault();
      const coords = toDeviceCoords(e.clientX, e.clientY);
      updateTouchDot(coords.screenRelX, coords.screenRelY, true);

      sendPointer('pointerMove', coords.x, coords.y, 'none', e.buttons || 1);
    });

    function endPointer(e) {
      if (!isPointerActive) return;
      isPointerActive = false;
      const coords = toDeviceCoords(e.clientX, e.clientY);
      updateTouchDot(coords.screenRelX, coords.screenRelY, false);
      try { viewport.releasePointerCapture(pointerId); } catch (_) {}
      pointerId = null;

      sendPointer('pointerUp', coords.x, coords.y, 'left', 0);
    }

    viewport.addEventListener('pointerup', endPointer);
    viewport.addEventListener('pointercancel', endPointer);

    viewport.addEventListener('contextmenu', (e) => {
      if (mode === 'control') {
        e.preventDefault();
      }
    });

    function updateTouchDot(relX, relY, show) {
      if (show) {
        touchDot.style.left = relX + 'px';
        touchDot.style.top = relY + 'px';
        touchDot.classList.add('active');
      } else {
        touchDot.classList.remove('active');
      }
    }

    // Mouse Wheel Scrolling
    viewport.addEventListener('wheel', (e) => {
      if (mode !== 'control') return;
      e.preventDefault();
      const coords = toDeviceCoords(e.clientX, e.clientY);
      sendWheel(coords.x, coords.y, e.deltaX, e.deltaY);
    }, { passive: false });

    function sendPointer(action, x, y, button, buttons) {
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

    // 5. Direct Physical Keyboard Typing
    window.addEventListener('keydown', (e) => {
      if (mode !== 'control') return;
      if (document.activeElement === document.getElementById('typeInput')) return;

      if (e.key === 'F11') {
        e.preventDefault();
        toggleFullscreen();
        return;
      }
      if (e.key === 'Escape' && document.fullscreenElement) {
        return;
      }

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

    // 6. Navigation & Quick Inputs
    function sendNav(cmd) {
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({ action: 'nav', command: cmd }));
      }
    }

    function sendInputText() {
      const input = document.getElementById('typeInput');
      const val = input.value;
      if (!val) return;
      if (wsStream && wsStream.readyState === WebSocket.OPEN) {
        wsStream.send(JSON.stringify({ action: 'text', text: val }));
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
        wsStream.send(JSON.stringify({ action: 'keyDown', key, code, keyCode }));
        setTimeout(() => {
          wsStream.send(JSON.stringify({ action: 'keyUp', key, code, keyCode }));
        }, 30);
      }
    }

    async function pasteClipboard() {
      try {
        const text = await navigator.clipboard.readText();
        if (text && wsStream && wsStream.readyState === WebSocket.OPEN) {
          wsStream.send(JSON.stringify({ action: 'text', text }));
        }
      } catch (_) {
        const manual = prompt('Ketik / tempel teks untuk dikirim ke WebView:');
        if (manual && wsStream && wsStream.readyState === WebSocket.OPEN) {
          wsStream.send(JSON.stringify({ action: 'text', text: manual }));
        }
      }
    }

    function toggleKeyboardDrawer() {
      if (kbdDrawer.style.display === 'none') {
        kbdDrawer.style.display = 'flex';
        document.getElementById('typeInput').focus();
      } else {
        kbdDrawer.style.display = 'none';
      }
    }

    function toggleDesktopView() {
      mockup.classList.toggle('desktop-view');
    }

    // 7. Fullscreen & Auto-Hiding HUD
    function toggleFullscreen() {
      if (!document.fullscreenElement) {
        if (mockup.requestFullscreen) {
          mockup.requestFullscreen();
        } else if (mockup.webkitRequestFullscreen) {
          mockup.webkitRequestFullscreen();
        }
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
      }
    }

    document.addEventListener('fullscreenchange', () => {
      const isFull = Boolean(document.fullscreenElement);
      document.getElementById('btnFullTop').innerText = isFull ? '✕ Exit Full' : '⛶ Fullscreen';
      if (isFull) {
        triggerHud();
      } else {
        mockup.classList.remove('hud-hidden');
      }
    });

    function triggerHud() {
      mockup.classList.remove('hud-hidden');
      if (hudTimer) clearTimeout(hudTimer);
      hudTimer = setTimeout(() => {
        if (document.fullscreenElement) {
          mockup.classList.add('hud-hidden');
        }
      }, 3000);
    }

    mockup.addEventListener('mousemove', () => {
      if (document.fullscreenElement) {
        triggerHud();
      }
    });

    // Quick Actions
    async function toggleAttach() {
      try {
        const res = await fetch('/api/attach?idx=' + currentTabIndex, { method: 'POST' });
        const d = await res.json();
        alert(d.message || 'Attached ke layar visual!');
        setTimeout(loadTabs, 800);
      } catch (e) {
        alert('Gagal attach: ' + e.message);
      }
    }

    async function toggleDetach() {
      try {
        const res = await fetch('/api/detach?idx=' + currentTabIndex, { method: 'POST' });
        const d = await res.json();
        alert(d.message || 'Detached ke background (Headless)!');
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

    // Tab List Refresh Interval (Only refreshes select menu, NEVER interrupts WebSocket stream)
    setInterval(loadTabs, 4000);

    // Initial Load
    setMode(mode);
    loadTabs();
  </script>
</body>
</html>`;
}

// 6. Singleton CDP Hub per Tab (Never dropped on client reconnect)
const cdpHub = {
    sessions: new Map(),

    getSession(wsUrl) {
        if (!wsUrl) return null;
        const s = this.sessions.get(wsUrl);
        if (s && s.cdpWs && s.cdpWs.readyState === WebSocket.OPEN) {
            return s;
        }
        return null;
    },

    createSession(wsUrl) {
        const existing = this.sessions.get(wsUrl);
        if (existing && existing.cdpWs && (existing.cdpWs.readyState === WebSocket.OPEN || existing.cdpWs.readyState === WebSocket.CONNECTING)) {
            return existing;
        }

        const session = {
            wsUrl,
            cdpWs: null,
            clients: new Set(),
            lastFrame: null,
            reqId: 1,
            closeTimer: null
        };
        this.sessions.set(wsUrl, session);

        try {
            const cdp = new WebSocket(wsUrl);
            session.cdpWs = cdp;

            cdp.on('open', () => {
                cdp.send(JSON.stringify({ id: session.reqId++, method: 'Page.enable' }));
                cdp.send(JSON.stringify({
                    id: session.reqId++,
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

            cdp.on('message', (data) => {
                try {
                    const msg = JSON.parse(data.toString());
                    if (msg.method === 'Page.screencastFrame') {
                        if (msg.params && msg.params.data) {
                            const buf = Buffer.from(msg.params.data, 'base64');
                            session.lastFrame = buf;
                            for (const client of session.clients) {
                                if (client.readyState === WebSocket.OPEN) {
                                    client.send(buf);
                                }
                            }
                        }
                        cdp.send(JSON.stringify({
                            method: 'Page.screencastFrameAck',
                            params: { sessionId: msg.params.sessionId }
                        }));
                    }
                } catch (_) {}
            });

            const onCdpClose = () => {
                this.sessions.delete(wsUrl);
            };
            cdp.on('close', onCdpClose);
            cdp.on('error', onCdpClose);

        } catch (e) {
            this.sessions.delete(wsUrl);
            return null;
        }

        return session;
    },

    addClient(wsUrl, clientWs) {
        let session = this.getSession(wsUrl) || this.createSession(wsUrl);
        if (!session) return null;
        if (session.closeTimer) {
            clearTimeout(session.closeTimer);
            session.closeTimer = null;
        }
        session.clients.add(clientWs);

        // Send cached last frame immediately for 0ms visual feedback
        if (session.lastFrame && clientWs.readyState === WebSocket.OPEN) {
            clientWs.send(session.lastFrame);
        }

        return session;
    },

    removeClient(wsUrl, clientWs) {
        const session = this.sessions.get(wsUrl);
        if (!session) return;
        session.clients.delete(clientWs);
        if (session.clients.size === 0) {
            session.closeTimer = setTimeout(() => {
                if (session.clients.size === 0) {
                    try {
                        session.cdpWs.send(JSON.stringify({ id: session.reqId++, method: 'Page.stopScreencast' }));
                        session.cdpWs.close();
                    } catch (_) {}
                    this.sessions.delete(wsUrl);
                }
            }, 15000);
        }
    }
};

// 7. HTTP Server Setup
const server = http.createServer(async (req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    const reqHost = req.headers.host || `127.0.0.1:${PORT}`;

    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    if (pathname === '/' || pathname === '/index.html') {
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(getDashboardHtml(reqHost, DEFAULT_CONTROL));
        return;
    }

    if (pathname === '/api/placeholder.svg') {
        res.writeHead(200, {
            'Content-Type': 'image/svg+xml',
            'Cache-Control': 'no-cache'
        });
        res.end(generatePlaceholderSvg('Menunggu WebView Aktif', 'Buka webview di Termux untuk memulai watch mode'));
        return;
    }

    if (pathname === '/api/tabs') {
        const tabs = await getAllTabs();
        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        });
        res.end(JSON.stringify(tabs));
        return;
    }

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

    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not Found');
});

// 8. WebSocket Server & Scrcpy Hub Bridge
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

        const session = cdpHub.addClient(selected.wsUrl, clientWs);
        if (!session) {
            try { clientWs.close(); } catch (_) {}
            return;
        }

        const cleanup = () => {
            cdpHub.removeClient(selected.wsUrl, clientWs);
        };
        clientWs.on('close', cleanup);
        clientWs.on('error', cleanup);

        // Native Input Event Forwarding
        clientWs.on('message', (raw) => {
            if (!session.cdpWs || session.cdpWs.readyState !== WebSocket.OPEN) return;
            try {
                const ev = JSON.parse(raw.toString());
                const cdp = session.cdpWs;

                if (ev.action === 'pointerDown') {
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
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
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
                        method: 'Input.dispatchMouseEvent',
                        params: {
                            type: 'mouseMoved',
                            x: ev.x,
                            y: ev.y,
                            buttons: ev.buttons || 1
                        }
                    }));
                } else if (ev.action === 'pointerUp') {
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
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
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
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
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
                        method: 'Input.insertText',
                        params: { text: ev.text || '' }
                    }));
                } else if (ev.action === 'keyDown') {
                    const isChar = ev.key && ev.key.length === 1 && !ev.ctrlKey && !ev.altKey;
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
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
                        cdp.send(JSON.stringify({
                            id: session.reqId++,
                            method: 'Input.dispatchKeyEvent',
                            params: {
                                type: 'char',
                                text: ev.key,
                                unmodifiedText: ev.key
                            }
                        }));
                    }
                } else if (ev.action === 'keyUp') {
                    cdp.send(JSON.stringify({
                        id: session.reqId++,
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
                        cdp.send(JSON.stringify({
                            id: session.reqId++,
                            method: 'Runtime.evaluate',
                            params: { expression: 'window.history.back()' }
                        }));
                    } else if (ev.command === 'forward') {
                        cdp.send(JSON.stringify({
                            id: session.reqId++,
                            method: 'Runtime.evaluate',
                            params: { expression: 'window.history.forward()' }
                        }));
                    } else if (ev.command === 'reload') {
                        cdp.send(JSON.stringify({
                            id: session.reqId++,
                            method: 'Page.reload',
                            params: { ignoreCache: true }
                        }));
                    } else if (ev.command === 'home') {
                        cdp.send(JSON.stringify({
                            id: session.reqId++,
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
    console.log('\x1b[36m│\x1b[0m   \x1b[1m\x1b[32m🛸 ESO SCRCPY PRO - STREAM & REMOTE CONTROL\x1b[0m          \x1b[36m│\x1b[0m');
    console.log('\x1b[36m├────────────────────────────────────────────────────────┤\x1b[0m');
    console.log(`\x1b[36m│\x1b[0m  \x1b[1mLocal URL\x1b[0m : \x1b[33mhttp://127.0.0.1:${PORT}\x1b[0m                          \x1b[36m│\x1b[0m`);
    ips.forEach(i => {
        const line = `  WLAN (${i.iface}) : http://${i.ip}:${PORT}`;
        console.log(`\x1b[36m│\x1b[0m  \x1b[1m${line.padEnd(52)}\x1b[0m\x1b[36m│\x1b[0m`);
    });
    console.log(`\x1b[36m│\x1b[0m  \x1b[1mMode\x1b[0m      : ${DEFAULT_CONTROL ? '\x1b[33m🎮 Takeover Control (30-60 FPS)\x1b[0m' : '\x1b[32m👁️ Spectator Mode (30-60 FPS Stream)\x1b[0m'}  \x1b[36m│\x1b[0m`);
    console.log('\x1b[36m│\x1b[0m  \x1b[1mHub\x1b[0m       : \x1b[35mSingleton CDP Screencast Hub (Persistent 60Hz)\x1b[0m \x1b[36m│\x1b[0m');
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
    process.exit(0);
}
process.on('SIGINT', gracefulExit);
process.on('SIGTERM', gracefulExit);
process.on('SIGHUP', gracefulExit);

freePort(PORT);
server.listen(PORT, '0.0.0.0', onListening);
