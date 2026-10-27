# Panduan & Arsitektur Eso:Launcher Agent Web Controller

Dokumen ini menjelaskan implementasi teknis dan panduan operasional integrasi **Agent Web Controller (`web` / `agent-web`)**, **UI Selector 3 Mesin AI**, automasi X / Twitter, serta jaminan kompatibilitas **Root & Non-Root** pada sistem **Eso:Launcher** (Project Fusion).

---

## 1. Arsitektur Komponen

```
               +---------------------------------------------------+
               |               Tuan Eso / AI Agent                 |
               +---------------------------------------------------+
                         |                               |
                 (Terminal / CLI)                  (WebView UI)
                         |                               |
                         v                               v
             +-----------------------+       +-----------------------+
             |    `web` / `browser`  |       |   Drawer AGY Copilot  |
             |   (Wrapper Zsh/Bash)  |       |  [DeepSeek|9Router|AGY]
             +-----------------------+       +-----------------------+
                         |                               |
                         v                               v
             +-----------------------+       +-----------------------+
             |      `agent-web`      |       |  FusionDrawerHelper   |
             | (Universal Controller)|       |    FusionAiHelper     |
             +-----------------------+       +-----------------------+
                         |                               |
                         +---------------+---------------+
                                         |
                                         v
                         +-------------------------------+
                         |          `termux-ipc`         |
                         |  (Linux Abstract Domain Sock) |
                         |       `\0termux_ipc`          |
                         +-------------------------------+
                                         |
                                         v
                         +-------------------------------+
                         |   Eso:Launcher Android Core   |
                         |   (Termux WebView Engine)     |
                         |      `ghost_injected.js`      |
                         +-------------------------------+
```

### 1.1 Penyelarasan Perintah (`web` & `browser`)
- Binary controller utama adalah [`agent-web`](file:///usr/local/bin/agent-web) (berbasis Node.js mandiri tanpa dependensi npm eksternal).
- Wrapper [`web`](file:///usr/local/bin/web) mengeksekusi `agent-web "$@"`.
- Alias lama [`browser`](file:///usr/local/bin/browser) tetap dipertahankan dan di-symlink / di-exec ke `web "$@"` untuk *backward compatibility*.
- Pada shell `zsh` Tuan Eso (`~/.zshrc`):
  ```bash
  alias web="agent-web"
  alias browser="web"
  ```

---

## 2. Seleksi 3 Mesin AI (WebView Drawer & IPC)

Tepat di atas kolom prompt BOT pada drawer WebView, disediakan segmented selector 3 mesin AI:

| Mesin AI | Target Backend | Deskripsi & Kegunaan |
| :--- | :--- | :--- |
| **DeepSeek** | Port 3334 / Local Proxy | Penalaran logika kompleks, analisa DOM mendalam, dan coding intent |
| **9Router** | Multi-LLM Gateway Router | Agregator model cerdas dengan load balancing |
| **AGY Copilot** | Antigravity AI Engine | Integrasi penuh dengan ekosistem asisten Antigravity |

### 2.1 Mekanisme Sinkronisasi Java <-> JS:
- Berkas Java [`FusionAiHelper.java`](file:///project/termux-launcher/app/src/main/java/com/termux/webview/FusionAiHelper.java) mengelola preferensi aktif dan membaca/menulis status ke konfigurasi universal (`ai_config.json`).
- Saat tab WebView di-load, [`FusionWebViewManager.java`](file:///project/termux-launcher/app/src/main/java/com/termux/webview/FusionWebViewManager.java) menginjeksi variabel runtime:
  ```javascript
  window.__ACTIVE_AI_ENGINE__ = "deepseek"; // atau "9router" / "agy"
  ```
- Tombol chips pada Drawer (`FusionDrawerHelper.java`) langsung meng-update `window.__ACTIVE_AI_ENGINE__` secara dinamis tanpa perlu me-reload halaman.

---

## 3. Eksekusi Multi-Step Chaining

Sistem mendukung eksekusi instruksi berantai (*multi-step chaining*) yang dapat dipicu baik melalui kolom input UI WebView maupun CLI terminal.

### 3.1 Pemisah Langkah yang Didukung
Kata sambung / pemisah berikut otomatis dipecah menjadi antrean langkah berurutan:
- `lalu`
- `kemudian`
- `dan <kata kerja>` (contoh: *dan swap*, *dan klik*)
- `->`
- `then`

### 3.2 Contoh Penggunaan Multi-Step:
```bash
# Melalui CLI terminal
web prompt "klik Connect Wallet lalu pilih Rabby kemudian klik Approve"

# Skenario Quest Web3
web prompt "klik Daily Quest lalu klik Claim Reward dan klik Confirm"
```

### 3.3 Penanganan DOM Lokal vs AI Fallback
1. **0ms Local DOM Heuristics**: Sistem pertama kali mencoba mencocokkan elemen berdasarkan teks tombol, `aria-label`, placeholder, sinonim Web3 (*login*, *connect*, *rabby*, *claim*, *swap*), dan ID elemen.
2. **AI Fallback**: Jika elemen tidak ditemukan di DOM lokal setelah beberapa kali scroll otomatis, perintah secara cerdas dialihkan ke Mesin AI aktif (`window.__ACTIVE_AI_ENGINE__`) untuk dianalisis lebih lanjut tanpa memunculkan notifikasi sukses palsu.

---

## 4. Automasi Posting X / Twitter (`web x-post`)

Agent AI dapat membuat postingan ke X (Twitter) secara independen tanpa campur tangan manual:

```bash
# Posting menggunakan profil default
web x-post "Hello world from Eso:Launcher Project Fusion!"

# Posting dengan profil spesifik
web x-post "Automated insight by AI Agent" --profile shaqi

# Menggunakan utilitas X CLI langsung
x --post "Update pasar kripto hari ini #EVM #Web3"
```

### 4.1 Mekanisme di Balik Layar:
1. `agent-web` memastikan URL `https://x.com/compose/post` terbuka di tab aktif.
2. Menunggu area input postingan (`[data-testid="tweetTextarea_0"]` atau `div[contenteditable="true"]`) siap.
3. Menginjeksi teks status menggunakan `document.execCommand('insertText')` dan `InputEvent` sintetis agar diakui oleh React engine Twitter.
4. Menemukan dan menekan tombol posting (`[data-testid="tweetButton"]`).

---

## 5. Jaminan Kompatibilitas Root & Non-Root

Arsitektur Project Fusion dirancang **100% agnostik lingkungan**, berjalan mulus di:
1. **Root Kali NetHunter Chroot** (Infinix Note 30 VIP - `su -mm`).
2. **Root Droidspaces Container** (Poco M3 - `su -c`).
3. **Non-Root Termux Native** (Tanpa akses root).

### 5.1 Abstract Linux Domain Socket (`\0termux_ipc`)
Komunikasi IPC tidak bergantung pada path file di `/tmp` atau `/data/local/tmp` yang sering terkendala permission Android `SELinux` atau UID mismatch. IPC menggunakan Linux abstract namespace:
```c
sun_path[0] = '\0';
strcpy(sun_path + 1, "termux_ipc");
```
Hal ini memungkinkan proses di dalam chroot / container root berbicara langsung dengan aplikasi Android Termux host.

### 5.2 Pencarian Path Dinamis (*Universal Fallback Array*)
Setiap utilitas script (`termux-ipc`, `agent-web`, `FusionWalletHelper`) tidak pernah mengasumsikan satu direktori kaku, melainkan menggunakan pencarian berjenjang:
```bash
# Urutan pelacakan direktori:
1. ${FUSION_WALLET_DIR} (Environment variable)
2. ${HOME}/wallet       (Home direktori user aktif)
3. /wallet              (Symlink root universal)
4. /root/wallet         (Direktori root Linux/Chroot)
5. /data/data/com.termux/files/home/wallet (Termux host non-root)
```

### 5.3 Standar Shebang Lintas Environment
Semua file eksekusi Linux menggunakan shebang standard:
```bash
#!/usr/bin/env bash
# atau
#!/usr/bin/env node
```
Didukung dengan symlink `/data/data/com.termux/files/usr/bin/` di dalam chroot, mencegah timbulnya galat `bad interpreter: No such file or directory`.

---

## 6. Ringkasan Perintah CLI (`agent-web` / `web`)

| Perintah | Contoh Sintaks | Keterangan |
| :--- | :--- | :--- |
| `web open <url>` | `web open https://app.uniswap.org` | Membuka URL di tab WebView aktif |
| `web click <target>` | `web click "Connect Wallet"` | Mencari dan mengeklik elemen di halaman |
| `web type <field> <teks>` | `web type "Search" "ETH"` | Mengisi teks pada input field tertentu |
| `web prompt <instruksi>` | `web prompt "klik swap lalu confirm"` | Eksekusi instruksi bahasa alami / multi-step |
| `web x-post <teks>` | `web x-post "Test tweet" --profile shaqi` | Otomasi posting ke X / Twitter |
| `web snapshot` | `web snapshot` | Mengambil struktur teks DOM untuk analisa AI Agent |
| `web eval <js>` | `web eval "document.title"` | Mengeksekusi kode JavaScript langsung |
| `web close` | `web close` | Menutup tab WebView aktif |

---

## 7. Status Pembuatan Bundle Payload

Bundle payload telah dikompilasi dan dienkripsi menggunakan PIN sistem Tuan Eso:
- File Output: `/project/clean_build/fusion_payload.tar.gz.enc`
- Mirror Aset APK: `/project/termux-launcher/app/src/main/assets/fusion/fusion_payload.tar.gz.enc`
- Ukuran: ~13.6 MB
- Isi Payload: `bin/` (`agent-web`, `web`, `browser`, `termux-ipc`), suite `wallet.cjs`, `x_webview.cjs`, `pemantau.cjs`, serta aset injeksi WebView.
