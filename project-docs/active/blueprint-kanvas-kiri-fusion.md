# Cetak Biru Arsitektur Kanvas Kiri Fusion (Full Android Launcher & Media Cockpit)

## 📌 1. Latar Belakang & Visi Baru
Kanvas Kiri pada Termux-Launcher berevolusi menjadi **"Full Android Hybrid Launcher & Media Cockpit"**:
- **Menu Pengaturan Khusus ("Kanvas Kiri")**: Seluruh konfigurasi Kanvas Kiri dipusatkan pada satu menu baru di Settings tanpa menyentuh pengaturan kanvas lain.
- **Dukungan Dual-Engine (Upstream vs Kustom Fusion)**:
  - Pengguna dapat memilih mode operasional dengan bebas:
    1. **Bawaan Upstream**: Menjalankan Widget Pane murni bawaan upstream Termux-Launcher (100% original AppWidgetHost).
    2. **Kustom Fusion**: Menjalankan arsitektur kustom (Homescreen hybrid 1x1 shortcut + widget, Lawnchair swipe-up drawer, 5 extra keys media & search, popup paritas dock).
- **Paritas Visual Ikon 100% Identik dengan Dock**:
  - Ikon shortcut aplikasi di grid Homescreen menggunakan mesin rendering, resolusi, masking squircle, label teks, dan artwork binder yang **100% identik dengan ikon di dock terminal** (`SuggestionBarView.bindRenderedIcon` & `LauncherIconStore.artworkDetailed`).
  - Menu popup context saat ditekan lama menggunakan **asset ikon bawaan yang sama persis** (`R.drawable.ic_dock_menu_*`).
- **Lawnchair-Grade App Drawer via Swipe-Up**: Membuka daftar seluruh aplikasi cukup dengan gestur **Swipe Up (geser ke atas)** dari area mana saja di Homescreen, dengan animasi fisika pegas (*physics Spring animation*) yang halus. **100% tanpa perlu fork repo Lawnchair**, karena mesin `com.termux.app.launcher.drawer` bawaan launcher kita sudah mengadopsi arsitektur Lawnchair 16 & AOSP Launcher3.
- **Extra Keys Statbar Kanvas Kiri (5 Tombol Khusus)**: Dilengkapi kontrol media terpadu dan navigasi cepat: `[ 🔍 SEARCH ]`, `[ ⏮ PREV ]`, `[ ⏯ PLAY ]`, `[ ⏭ NEXT ]`, dan `[ ⚙️ SETTING ]`.

---

## 🛡️ 2. Batas Isolasi Mutlak (Zero-Impact Guarantee)
- **Kanvas Tengah (Terminal Linux CLI / AGY / Zsh)**: 100% terkunci, zero disruption.
- **Kanvas Kanan (X11 Display / Kiosk)**: 100% terkunci, zero disruption.
- **Menu Settings**: Penambahan fragment `LeftCanvasPreferencesFragment` hanya beroperasi pada preferensi `left_canvas_*`. Pengaturan Terminal, Display, Keyboard, dan Status Bar lainnya tetap 100% steril.
- **Efisiensi RAM**: Berjalan murni di domain native Android Views (0 MB Chromium RAM).

---

## ⚙️ 3. Arsitektur Menu Pengaturan Baru: "Kanvas Kiri"

Menu baru ditambahkan pada Settings root di bawah kategori **Workspace** (bersanding dengan Terminal, Keyboard, dan Display):

```
Settings > Workspace
├── Terminal (Kanvas Tengah)
├── Keyboard & Input
├── Display (Kanvas Kanan)
└── Kanvas Kiri (Left Canvas Preferences)  <-- [BARU]
```

### Halaman Pengaturan `LeftCanvasPreferencesFragment`:

| Kunci Preferensi (`SharedPreferences`) | Tipe UI | Pilihan / Nilai | Keterangan |
| :--- | :--- | :--- | :--- |
| `left_canvas_engine_mode` | `ListPreference` | `custom` (Default) \| `upstream` | **Pemilih Engine Utama**: Beralih antara mode kustom Fusion atau widget bawaan upstream murni. |
| `left_canvas_quick_extra_keys` | `Preference` | `"SEARCH,PREV,PLAY,NEXT,SETTINGS"` | Membuka bottom sheet editor untuk kustomisasi 5 tombol statbar kanan kanvas kiri. |
| `left_canvas_grid_columns` | `ListPreference` | `4` (Default), `5`, `6` | Jumlah kolom grid Homescreen pada mode kustom. |
| `left_canvas_grid_rows` | `ListPreference` | `5` (Default), `6`, `7` | Jumlah baris grid Homescreen pada mode kustom. |
| `left_canvas_swipe_up_drawer` | `SwitchPreference`| `true` (Default) | Mengaktifkan/menonaktifkan gestur swipe-up untuk membuka App Drawer ala Lawnchair. |
| `left_canvas_enable_custom_tab` | `SwitchPreference`| `false` (Default) | Mengaktifkan tab tambahan ke-3 di statbar kiri (Web Portal, Notes, atau Monitor). |

> [!NOTE]
> Jika `left_canvas_engine_mode` diubah ke **`upstream`**, Kanvas Kiri seketika kembali ke perilaku widget pane asli bawaan repo upstream tanpa merusak shortcut atau konfigurasi mode kustom yang tersimpan.

---

## 📐 4. Tata Letak Antarmuka & Kontrol

```
+-----------------------------------------------------------------------------------+
| [ 🏠 Home ]   [ 🧩 Widgets ]   ...   [ 🔍 SEARCH ] [ ⏮ ] [ ⏯ ] [ ⏭ ] [ ⚙️ SETTING ] |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [ WIDGET CLOCK / WEATHER (4x2) ]                                                 |
|                                                                                   |
|  [ 🪙 BINANCE ]   [ 💬 TELEGRAM ]   [ 🐦 X ]   [ 📷 CAMERA ]   (Shortcut 1x1)     |
|                                                                                   |
|  [ WIDGET BATTERY / GHOST WALLET (4x2) ]                                          |
|                                                                                   |
|  [ 🌐 CHROME ]    [ ⚙️ SETTINGS ]    [ 📁 FILES ]  [ 🎵 SPOTIFY ] (Shortcut 1x1)     |
|                                                                                   |
|                                        ▲                                          |
|                             [ SWIPE UP UNTUK ALL APPS ]                           |
+-----------------------------------------------------------------------------------+
```

---

## 🎛️ 5. Spesifikasi 5 Tombol Extra Key Kanvas Kiri

Dipasang pada bilah status Kanvas Kiri dengan fungsi presisi:

1. **`[ 🔍 SEARCH ]` (Quick App Finder)**:
   * **Aksi Otomatis**: Sekali tap, langsung **membuka App Drawer meluncur naik (slide-up)** dan **otomatis memfokuskan kursor ke Search Pill** sekaligus **memunculkan in-app keyboard**. Tuan bisa langsung mengetik nama aplikasi tanpa tap layar dua kali.
2. **`[ ⏮ PREV ]` (Media Previous)**:
   * Memanggil `com.termux.app.statusbar.TopPaneFeed.skipPrevious()`.
   * Mengembalikan lagu/track ke track sebelumnya pada pemutar media aktif (Spotify, YouTube Music, SoundCloud, dll.).
3. **`[ ⏯ PLAY / PAUSE ]` (Media Playback Toggle)**:
   * Memanggil `com.termux.app.statusbar.TopPaneFeed.togglePlayPause(isPlaying)`.
   * Ikon bersifat dinamis: berubah menjadi `⏸` saat lagu sedang berputar, dan `▶` saat dijeda.
4. **`[ ⏭ NEXT ]` (Media Next)**:
   * Memanggil `com.termux.app.statusbar.TopPaneFeed.skipNext()`.
   * Melompat ke lagu/track berikutnya secara instan.
5. **`[ ⚙️ SETTING ]` (Launcher & Canvas Preferences)**:
   * Membuka langsung menu preferensi Kanvas Kiri (`LeftCanvasPreferencesFragment`).

---

## 📲 6. Mekanisme Menambahkan Aplikasi dari Drawer ke Home

Mengadopsi 2 metode teruji:

### Metode 1: Long-Press & Drag to Home (Standar Android / Lawnchair)
1. Di dalam App Drawer, Tuan **tekan & tahan (long-press)** ikon aplikasi (disertai getaran haptik *tick*).
2. Begitu jari mulai bergerak (*drag*), App Drawer otomatis **meluncur turun (*slide down*)**, memperlihatkan grid Homescreen di baliknya.
3. Ikon melayang (*floating preview*) mengikuti posisi jari di atas kisi-kisi grid Homescreen.
4. Kotak grid kosong yang dilewati akan menyala (*drop highlight* 1x1).
5. Lepas jari $\rightarrow$ Ikon jatuh (*spring drop*) ke koordinat kotak yang dipilih.

### Metode 2: Context Menu Popup "Tambah ke Beranda" (One-Tap Instant)
1. Di dalam App Drawer, Tuan **tekan & tahan** ikon aplikasi tanpa menggeser jari.
2. Muncul menu popup mini *Frosted Glass* di dekat ikon:
   * **`[ ➕ Tambah ke Beranda ]`**
   * **`[ ℹ️ Info Aplikasi ]`**
3. Tap **`[ ➕ Tambah ke Beranda ]`**:
   * App Drawer otomatis menutup secara mulus.
   * Ikon aplikasi langsung dipasang pada slot grid kosong pertama di Homescreen dengan animasi jatuh (*drop bounce*).

---

## 📱 7. Interaksi Shortcut Aplikasi di Homescreen (Tekan-Lama)

### A. Jika Ditekan-Tahan lalu Digeser (Drag to Move)
* Ikon terangkat (*lift up*) dan mengikuti ujung jari.
* Tuan bebas memindahkan posisi shortcut ke sel kotak grid mana saja di Homescreen.

### B. Jika Ditekan-Tahan Diam (100% Paritas Visual & Aset Ikon dengan Popup Dock)
Menggunakan mesin komponen popup bawaan yang sama: `com.termux.app.launcher.popup.AnchoredMenu` & `MenuRowFactory` (seperti pada `SuggestionBarView.showAppContextPopup`), dengan tampilan Frosted Glass, typography 12sp bold, dan **resource drawable asli**:

**Kecuali fitur Pin/Unpin (karena di Home tidak dibutuhkan), digantikan dengan aksi "Hapus dari Home"**.

Susunan baris popup menu 100% identik dengan dock terminal:
1. **Header**: Ikon asli aplikasi (`resolveMenuHeaderIcon`) + Nama Aplikasi (`context.entry.label`).
2. **`Uninstall`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_uninstall` (Minta uninstall ke sistem Android).
3. **`App info`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_info` (Buka halaman Android App Info).
4. **`Hapus dari Home`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_uninstall` / `ic_symbol_delete` (Mencopot shortcut dari grid Homescreen).
5. **`Notifications`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_info` (Muncul otomatis jika ada badge notifikasi aktif).
6. **`Change icon`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_change_icon` (Buka picker icon pack kustom).
7. **`Reset icon`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_reset` (Muncul jika ikon sedang di-kustom untuk reset ke default).
8. **Divider Line** $\rightarrow$ `menuRows.addDivider(shell)`.
9. **`Shortcuts`** $\rightarrow$ Icon: `R.drawable.ic_dock_menu_shortcuts` + panah `R.drawable.ic_dock_menu_chevron` (Membuka sub-menu samping Android Dynamic App Shortcuts).

---

## 🎨 8. Paritas Visual Ikon Shortcut di Grid Homescreen
Agar pengalaman visual benar-benar seragam dan menyatu:
- **Ukuran & Proporsi Ikon**: Mengikuti ukuran ikon dock terminal (`SuggestionBarView.dp(48)` / `ICON_SIZE`).
- **Rendering Pipeline**: Memakai `LauncherIconStore.Artwork` dan cache bawaan (`LauncherAppDataProvider.getInstance(context).icons().artworkDetailed(entry)`).
- **Label Teks**: Font, warna (`resolveLauncherTextColor()`), dan bayangan teks mengikuti preset tampilan dock terminal.

---

## 🔍 9. Diagnosa & Perbaikan Bug Search & Built-in Keyboard

### Akar Masalah yang Ditemukan:
1. **Routing Keyboard Keliru (`DisplayKeyboardRoute.java:48`)**:
   `requestAppDrawerSearchKeyboard()` mengembalikan `DrawerSearch.SYSTEM_ASK` karena Kanvas Kiri bukan `onDisplayPlace`. Akibatnya, launcher memanggil system IME Android yang diarahkan ke terminal non-aktif, sementara built-in keyboard (`mInAppKeyboard`) tidak pernah dinaikkan.
2. **Perebutan Sentuhan (*Touch Claiming*) pada Search Pill (`AppDrawerContentView:1458`)**:
   Search Pill dianggap sebagai `CHROME`. Kontainer luar `AppDrawerPlaneView` menganggap sentuhan jari pada pill sebagai tarikan penutup drawer (`DRAWER_DRAG`). Sedikit saja pergeseran mikro pada jari langsung membatalkan event tap (`ACTION_CANCEL`).

### Rencana Solusi:
1. **Routing Cerdas**: Jika built-in keyboard aktif (`mInAppKeyboard.isEnabled()`), drawer search wajib langsung menaikkan `mInAppKeyboard` dan mengikat inputnya ke `AppDrawerSearchController`.
2. **Proteksi Sentuhan**: Daftarkan Search Pill sebagai wilayah konten (`AppDrawerTouchRegions.Region.PILL`) agar `ownsPoint()` bernilai `true` dan `AppDrawerPlaneView` tidak membajak event tap.

---

## 📊 10. Status Rencana
- [x] Cetak biru diperbarui & disimpan.
- [x] Menu baru "Settings > Kanvas Kiri" terdefinisi lengkap dengan switcher Default Upstream vs Kustom Fusion.
- [x] Batasan isolasi mutlak ditegaskan (hanya mengatur Kanvas Kiri, zero-impact ke kanvas lain).
- [x] Metode penambahan app (1 & 2) diformalkan.
- [x] 5 Tombol Extra Key (Search auto-focus, Media Prev, Play/Pause dinamis, Next, Setting) dipetakan.
- [x] Long-press shortcut di Home: Pindah posisi (geser) & Popup Menu 100% PARITAS DOCK DENGAN ASSET DRAWABLE ASLI.
- [x] Ikon shortcut di grid Homescreen 100% identik dengan dock (artwork detailed & ukuran).
- [x] Diagnosa bug tap search & built-in keyboard selesai 100%.
- [x] **Status Eksekusi Kode: SELESAI (Implementasi Kanvas Kiri siap, menunggu konfirmasi Tuan Eso sebelum push/build CI).**
