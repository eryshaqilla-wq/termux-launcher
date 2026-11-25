# FUSION WEBVIEW ENGINE — PANDUAN KEMAMPUAN LENGKAP UNTUK AI AGENT

Dokumen ini adalah referensi utama kapabilitas **Project Fusion Android WebView**, **Multi-Worker Automation**, **Zero-Rebuild Hot-Override**, **Wi-Fi LAN Remote Control**, dan **MCP (`webview_mcp`)** agar AI Agent di perangkat baru langsung mengenali seluruh mesin yang tersedia.

---

## 1. Fondasi WebView Native & Multi-Akun Paralel
- **Native Multi-Profile Isolation (`FusionProfileManager` + `ProfileStore`)**:
  - Setiap profil (`--profile <nama>`) memakai storage Chromium terisolasi (cookies, IndexedDB, localStorage).
  - `GhostBridgeInterface` & `FusionNativeBridge` otomatis memakai `Profile.getCookieManager()` milik tab masing-masing (tidak bocor ke profil Default).
  - `WEBVIEW_PROFILE_CLEAR <profile>`: hapus cookies & `WebViewDatabase.clearHttpAuthUsernamePassword()`.
  - `WEBVIEW_PROFILE_WIPE <profile>`: reset penuh (cookies, auth cache, hapus `<profile>.json` & `<profile>.device.json`).
- **Multi-Tab Worker Isolation (`FusionTab` & `FusionWebViewClient`)**:
  - Setiap tab menyimpan identitas dompet sendiri (`mWallet*`, `mSol*`, `mTron*`, `mSui*`, `mAptos*`, `mIota*`), `mKeepAliveBackground` (anti-throttle saat tab headless offscreen), popup inheritance (`onCreateWindow`), dan per-tab HTTP proxy auth (`onReceivedHttpAuthRequest` + `GhostBridge.nativeFetch`).
  - `openUrlInternal` memakai `skipUiSwitch` + offscreen `translationX = -10000f` sehingga membuka `--headless` tidak mencuri fokus UI visual.
  - **Phoenix Auto-Healing (`onRenderProcessGone`)**: auto-resurrect tab crash; jika tab worker melewati batas retry, hanya tab worker tersebut yang ditutup (`killTabById`), tab foreground tetap aman.
- **Multi-Tunnel SOCKS5 (`FusionSocksTunnel`)**:
  - Mendukung banyak tunnel HTTP CONNECT -> SOCKS5 auth (RFC 1929) secara konkuren via `ConcurrentHashMap` sehingga worker paralel dengan proxy SOCKS5 berbeda tidak saling mematikan tunnel.
- **Anti-Sybil Permission Guard (`FusionWebChromeClient`)**:
  - Default aman untuk multi-akun (`false` semua agar GPS & ID hardware DRM tidak membocorkan 1 HP ke semua akun):
    - `sAllowGeolocation = false` (`onGeolocationPermissionsShowPrompt`)
    - `sAllowSensitivePermissions = false`, `sAllowDrmId = false` (`RESOURCE_PROTECTED_MEDIA_ID`), `sAllowCamera = false`, `sAllowMic = false` (`onPermissionRequest`)
  - Toggle instan via CLI/IPC kapan saja (mis. saat butuh kamera KYC):
    - `termux-ipc webview perm <geo|drm|cam|mic|sensitive|all> <on|off|status>`

---

## 2. Anti-Detect Stealth & Zero-Rebuild Hot-Override
- **0-Mismatch Fingerprint**:
  - Otomatis membaca versi WebView sistem (`WebViewCompat.getCurrentWebViewPackage`) untuk menyinkronkan seluruh User-Agent Mobile/Desktop dan `Sec-CH-UA` (`navigator.userAgentData`).
  - Menghapus header `X-Requested-With` (`REQUESTED_WITH_HEADER_ALLOW_LIST`).
  - `document_start` injeksi [`fingerprint_faker.js`] + `fusion_agent_webview.user.js` (144 profil HP flagship deterministik, seeded Canvas/WebGL per-wallet, Font Cloaking, deep iframe traversal).
- **Zero-Rebuild External Engine Override**:
  - Taruh file override di `$FUSION_ENGINE_DIR`, `~/.fusion/engine/`, `/root/.fusion/engine/`, atau `/root/wallet/webview_engine/`:
    - `fingerprint_faker.js`, `ghost_injected.js`, `home.html`, `devices.json`, `wifi_console.html`
  - Muat ulang seketika tanpa rebuild APK: `termux-ipc WEBVIEW_ENGINE_RELOAD`.
- **Network Interception Native (`FusionNetworkInterceptor` + `ServiceWorkerControllerCompat`)**:
  - Mencegat request halaman maupun Service Worker untuk tracker block, API mock, dan disk mirror (`0ms`):
    - `WEBVIEW_NET_BLOCK <regex>` / `WEBVIEW_NET_MIRROR <pattern> <dir>` / `WEBVIEW_NET_MOCK <json>` / `WEBVIEW_NET_CLEAR` / `WEBVIEW_STRICT_SSL <on|off>`.

---

## 3. CLI (`web` / `agent-web`), ADB-Less CDP & Universal IPC
- **Direct Wallet & Multi-Chain Injection** (tanpa butuh `eso.db` di HP remote):
  - `WEBVIEW_WALLET_SET <name> [address] [chain]`
  - `WEBVIEW_WALLET_TAB <tabId> <name> <address> [chain]`
  - `WEBVIEW_WALLET_DAEMON <ip[:port]|local>` (arahkan RPC `wallet.cjs` WebView ke host LAN, mis. Infinix `0.0.0.0:3333`).
- **CDP Bridge Tanpa ADB (`FusionCdpBridge`)**:
  - `WEBVIEW_CDP_ON` / `WEBVIEW_CDP_OFF` / `WEBVIEW_CDP_INFO`
  - Membuka `127.0.0.1:9222` ↔ `@webview_devtools_remote_<pid>` dan menulis port aktif ke `~/.webview_cdp_port`.
- **Kontrol Tab Presisi**:
  - `WEBVIEW_EVAL_TAB <tabId> <js>` | `WEBVIEW_KILL_TAB_ID <tabId>` | `WEBVIEW_KEEPALIVE_TAB <tabId> <on|off>`
  - `WEBVIEW_TOUCH_TAB <tabId> <x> <y>` | `WEBVIEW_SWIPE_TAB <tabId> <x1> <y1> <x2> <y2> [dur]` | `WEBVIEW_SCREENSHOT_TAB <tabId> <path>`
  - `WEBVIEW_CALL_NATIVE <json>` (`FusionNativeBridge` Java reflection/system service dengan blocklist class berbahaya).
- **Perintah Automation (`web` / `agent-web`)**:
  - `web play <resep> [--wallets w1..wN] [-p N] [--p proxy.txt] [--turbo] [--headless] [--remote <ip:port>]`
  - `web gc [--all] [--json]` | `web snap` / `web restore` | `web sniper` | `web show` / `web hide`

---

## 4. Kontrol Jarak Jauh via Wi-Fi LAN Tanpa Token & MCP (`webview_mcp`)
- **Kontrol Auto-Start & Server Wi-Fi di Perangkat Target (`FusionWifiRemoteServer`)**:
  - `WEBVIEW_WIFI_ON [port] [no-autostart]` (default port `8765`, menjaga `WakeLock` + `WifiLock` `WIFI_MODE_FULL_HIGH_PERF` agar tetap hidup saat layar mati)
  - `WEBVIEW_WIFI_CDP <on|off>` (relay CDP LAN di `0.0.0.0:9223`)
  - `WEBVIEW_WIFI_AUTOSTART <on|off|status>` & `WEBVIEW_WIFI_CDP_AUTOSTART <on|off|status>` (matikan di HP utama jika tidak perlu, nyalakan di HP worker seperti Poco M3)
  - `WEBVIEW_WIFI_OFF` / `WEBVIEW_WIFI_STATUS`
- **Endpoint HTTP Langsung (`http://<ip-wifi>:8765`, tanpa token)**:
  - `GET /` (Remote Console + override `wifi_console.html`), `GET /ping`, `GET /tabs`, `GET /screenshot?tab=<id>`
  - `GET /file?path=<path>` (tarik file download/screenshot) & `POST /file?scope=uploads|engine|scripts|sessions&name=<file>` (dorong upload/override/sesi)
  - `POST /ipc`, `POST /eval`, `POST /touch`, `POST /swipe`, `POST /nav`, `POST /type`
- **Eksekusi Lintas HP dari Controller (mis. Infinix → Poco M3)**:
  - CLI: `web --remote <ip-poco> play <modul> ...` atau `FUSION_REMOTE=<ip-poco>:8765 termux-ipc webview ...`
  - MCP `webview_mcp` ([`termux_webview_mcp_server.cjs`]): mendukung `browser_remote_set(target="<ip-poco>:8765" | "local")`, auto relay file upload/download, serta `64` tools browser & Web3 (`tools/list` instan tanpa blok startup).
