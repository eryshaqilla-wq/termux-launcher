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

---

## 8. Panduan Penggunaan Floating Recorder & Native Step Toolbar

### 8.1 Arsitektur Native View Standby
Untuk menjamin stabilitas tinggi dan zero-flicker, toolbar recorder kini diimplementasikan sebagai native Android View (`com.termux.webview.FusionRecPillHelper`):
- Terkunci permanen pada layer native `mPaneHost` (`Gravity.BOTTOM | Gravity.START`, margin 10px).
- Tidak ikut tergeser atau bergoyang saat halaman web di-scroll.
- Ukuran kedua baris seragam secara simetris (Text 10sp, Padding Tombol 6x3, Padding Box 3x3, Margin 1px).

### 8.2 Daftar Tombol & Fungsi Operasional

#### A. Baris Bawah (Ketua REC)
- `[ < ] / [ > ]`: Toggle minimize seluruh floating recorder menjadi satu tombol kecil di sudut kiri bawah.
- `[ • REC ( n ) ]`: Mulai / Berhenti merekam alur kerja (menampilkan jumlah step `n`).
- `[ DOM ]`: Membuka modal visual DOM Picker untuk memilih elemen web secara presisi.
- `[ Simpan ]`: Menyimpan resep ke `recipes_db.json`.
- `[ Batal ]`: Membatalkan rekaman aktif dan mereset hitungan langkah.

#### B. Baris Atas (Smart Step Actions - Muncul Saat Rekam Berjalan)
- `[ < ] / [ > ]`: Toggle minimize baris smart steps.
- `[ WALLET > ]`: Berganti ke akun wallet berikutnya (`switchToNextWallet()`), memicu event Web3 `accountsChanged`, dan mencatat aksi `next_wallet`.
- `[ EMAIL > ]`: Mengambil email tuyul berikutnya dari `/root/wallet/email.txt` (`AgyDB.nextEmail()`).
- `[ REF ]`: Mengambil kode referral dari `/root/wallet/ref.txt` (`AgyDB.nextRef()`). Jika isi file hanya 1 kode, kode dapat diinjeksi berulang kali secara dinamis.
- `[ RELOAD ]`: Me-reload halaman dengan mempertahankan status perekaman via `sessionStorage`.
- `[ WIPE ]`: Menghapus cache, cookies, form data, dan web storage secara tuntas (`wipe_cache`).
- `[ FP ]`: Mengacak dan menyuntikkan model fingerprint perangkat baru (`GhostBridge.nextFingerprint()`).

---

## 9. Native Chromium Multi-Profile Isolation & Document-Start Stealth

Sistem tidak lagi menggunakan teknik pembersihan cache / tukar sesi legacy yang memicu kedipan layar (*screen wipe*). Kini setiap profil berjalan di lingkungan Chromium terisolasi secara bersamaan:

1. **Engine Chromium ProfileStore**:
   - Berbasis `androidx.webkit:webkit:1.12.0`.
   - Menggunakan `ProfileStore.getInstance().getOrCreateProfile(profileName)` dan `WebViewCompat.setProfile(webView, profileName)`.
   - Penyimpanan data, database IndexedDB, LocalStorage, dan CookieJar untuk setiap profil terpisah 100% pada tingkat kernel Chromium.

2. **Document-Start Stealth Injeksi (`DOCUMENT_START_SCRIPT`)**:
   - Skrip spoofing fingerprint disuntikkan langsung ke engine browser via `WebViewCompat.addDocumentStartJavaScript(webView, fakerScript, Collections.singleton("*"))`.
   - Dijalankan sebelum script `<head>` mana pun pada halaman web dimuat. Menghilangkan celah deteksi anti-bot, anti-tamper, dan proteksi Cloudflare Turnstile.

3. **Badge Tab Label Profil**:
   - Drawer tab Termux otomatis menampilkan nama profil aktif pada chip tab:
     `* Tab 1: [shaqilla] ...`

---

## 10. Persistent Hardware Identity per Profile (Anti-Detection 16 Flagship Pool)

Setiap profil browser memiliki identitas perangkat keras yang terkunci secara permanen di disk:
- Lokasi Penyimpanan: `/root/wallet/sessions/<profile>.device.json`
- **3 Wallet Utama Tuan Eso Terkunci Permanen**:
  - `shaqilla` $\rightarrow$ **Samsung Galaxy S24 Ultra** (`SM-S928B`, Snapdragon 8 Gen 3 / Adreno 750, 12GB RAM, layar 1440x3120).
  - `eryziggy` $\rightarrow$ **Google Pixel 8 Pro** (`Pixel 8 Pro`, Tensor G3 / Mali-G715 Immortalis, 12GB RAM, layar 1344x2992).
  - `0xjamrud` $\rightarrow$ **OnePlus 12** (`CPH2581`, Snapdragon 8 Gen 3 / Adreno 750, 16GB RAM, layar 1440x3168).
- **Pool 16 Flagship untuk Profil Baru**: Profil baru otomatis mendapat alokasi deterministik dari 16 model flagship ternama (Xiaomi 14 Pro, Asus ROG 8 Pro, Poco F6 Pro, Vivo X100 Pro, Sony Xperia 1 V, dsb.) dan identitas tersebut disimpan selamanya.
- **Tampilan Launch Minimalis**: Bersih tanpa icon alay dan tanpa kotak-kotak border:
  ```text
  [ESO BROWSER] Profil: shaqilla | Samsung Galaxy S24 Ultra (SM-S928B)
    Hardware : Adreno (TM) 750 | 12GB RAM | 8 Cores
    Layar    : 1440x3120 (Ratio 3.0)
    Stealth  : Native Multi-Profile | Zero-Leak Client Hints
  ```

---

## 11. Multi-Profile Macro Execution (`--all-profiles`, `--profiles`, `--parallel`)

Otomasi makro webview kini dapat dieksekusi untuk banyak profil sekaligus baik secara bergantian maupun bersamaan:

1. **Eksekusi Sekuensial (1 per 1 - Bergantian)**:
   ```bash
   # Menjalankan makro untuk 3 akun utama secara berurutan:
   web --all-profiles --modul klaim_airdrop --loop 1

   # Menjalankan untuk daftar akun tertentu:
   web --profiles shaqilla,0xjamrud --modul daily_quest
   ```
   Menampilkan ringkasan status (`OK`/`FAILED`) dan durasi eksekusi per-profil di akhir putaran.

2. **Eksekusi Paralel (Bersamaan)**:
   ```bash
   # Meluncurkan 3 profil bersamaan di worker tab mandiri:
   web --all-profiles --parallel --modul klaim_angpao "https://tapexo.app/c/rp_123"

   # Kombinasi dengan GhostWallet:
   web --all-profiles --parallel --wallet --modul mint_nft "https://nft.dapp/free"
   ```
   Setiap profil berjalan di tab worker background (`worker_<profile>_<id>`), memuat identitas dan sesi masing-masing, menjalankan makro, melakukan auto-save sesi, lalu menutup tab secara mandiri.

---

## 12. Auto-Save Sesi Wajib Sebelum Tab / Menu Ditutup atau Di-Kill

Sistem menjamin sesi akun tidak akan pernah hilang ketika tab ditutup atau proses dihentikan:
1. **Langkah Terakhir Wajib (`web save` Otomatis)**:
   - Sebelum tab dihancurkan (`webView.destroy()`), engine CDP Playwright wajib mengekstrak seluruh cookies & storage ke `/root/wallet/sessions/<profile>.json`.
   - Berlaku saat:
     - Tutup tab via tombol silang `[ ✕ ]` di drawer/chip tab Android UI.
     - Tutup dari tombol menu close WebView.
     - Tutup via CLI: `web close`, `web kill-tab`.
     - Penghentian paksa / exit signal (Ctrl+C / `SIGINT` / `SIGTERM`).
     - Selesainya makro worker tab.
2. **Format Info Sesi Persis Seperti `web save`**:
   - Di terminal:
     ```text
     Menyimpan sesi website untuk profil [0xjamrud]...
     Sesi web untuk profil [0xjamrud] berhasil disimpan (16 cookies).
     Sesi website berhasil disimpan ke profil [0xjamrud].
     WebView ditutup.
     ```
   - Di Android UI: Muncul Toast notifikasi langsung ke layar:  
     `Sesi web untuk profil [0xjamrud] berhasil disimpan (16 cookies).`
3. **Browsing Default Tanpa Profil**: Ditutup instan (0 milidetik) tanpa memicu dump JSON, menjaga navigasi harian tetap ringan.

---

## 13. Interactive 4-Step Wizard di X Radar & Telegram Swarm

Menu relasi modul pada CLI `x` (X Radar) dan `pemantau` / `pmt` (Telegram Swarm) telah diperbarui dengan wizard 4 langkah:
- **[WIZARD 1/4] Profil Browser**:
  - `[1]` 3 Profil Utama Sekuensial (1/1) $\rightarrow$ `--all-profiles`
  - `[2]` 3 Profil Utama Paralel (Bersamaan) $\rightarrow$ `--all-profiles --parallel`
  - `[3]` 1 Profil Tertentu (Tunggal) $\rightarrow$ `--profile <nama>`
  - `[4]` Multi-Profil Custom $\rightarrow$ `--profiles <list> [--parallel]`
  - `[5]` Tanpa Profil (Default / Bersih)
- **[WIZARD 2/4] GhostWallet**: Konek daemon port 3333 default, wallet tertentu, atau tanpa wallet.
- **[WIZARD 3/4] Total Loop**: Jumlah putaran (default 1x putaran).
- **[WIZARD 4/4] Kata Kunci Auto-Stop**: Kata pemicu henti seketika (`--stop-if "<kata>"`).
Perintah lengkap otomatis dirangkai dan tersimpan ke `modules.json`.

