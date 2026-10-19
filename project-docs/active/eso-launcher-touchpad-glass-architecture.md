# Eso Launcher: Catatan & Memori Teknis Arsitektur Touchpad & Frosted Glass

Dokumen ini mencatat seluruh rekam jejak investigasi, arsitektur, keputusan teknis, dan perbaikan bug pada **Eso Launcher** (`/project/termux-launcher`, branch `eso-launcher`).

---

## 1. Konteks Perangkat & Lingkungan

- **Perangkat Utama**:
  - **Infinix Note 30 VIP** (`Infinix X6739`, `127.0.0.1:5555`):
    - Runner: Kali NetHunter Chroot (`RunnerType.NETHUNTER`), dijalankan `su -mm` lewat `termux-chroot-*`.
  - **Poco M3** (`RunnerType.DROIDSPACES`):
    - Droidspaces Container Ubuntu, dijalankan `su -c` lewat `termux-droidspaces-*`, PulseAudio bridge ke `tcp:172.28.0.1:4713`.
- **Repositori**: `eryshaqilla-wq/termux-launcher` (branch: `eso-launcher`).
- **Alur Build**:
  - GitHub Actions Workflow `Build nightly`.
  - Download artifak `arm64-v8a` (`.apk`), install via `adb -s <device> install -r <apk>`.
- **Kebijakan Bersih**:
  - Repo dan perangkat sepenuhnya **bersih dari binary Golang** (kembali ke checkpoint bersih native Java/Android & shell).

---

## 2. Masalah yang Diselesaikan (The Touchpad Frosted Glass Bug)

### Gejala Masalah:
1. **Saat Keyboard Terbuka -> Mouse Mode ON**:
   - Touchpad tampil mulus, blur radius, wallpaper frost, dan glass grain mengikuti dock Extra Keys dengan sempurna.
2. **Saat Keyboard Tertutup -> Langsung Mouse Mode ON**:
   - Muncul bug warna touchpad abu-abu transparan, blur dan grain tidak mengikuti Extra Keys, serta terjadi flicker/wallpaper tembus (raw sharp wallpaper bocor beberapa frame).

---

## 3. Investigasi & Akar Masalah (Root Causes)

1. **`accessory_surface_host` Target Height Freeze (`applyAccessoryLayerBounds`)**:
   - `applyAccessoryLayerBounds` sebelumnya membaca `parentView.getHeight()` dan membekukannya ke dalam pixel (`targetHeight = parentView.getHeight()`).
   - Saat keyboard tertutup, tinggi dock hanya ~50px. Ketika mouse mode dipanggil langsung, `accessory_surface_host` terkunci di tinggi 62px (`50px + 12px overscan`), tidak bisa membesar ke 300px mengikuti touchpad frame.
   - Akibatnya `isUnifiedAccessorySurfaceLaidOut()` dan `isUnifiedAccessoryBackdropReady()` bernilai `false`, sehingga lapisan backdrop bersama tidak ada di belakang touchpad.
   - **Solusi**: Ganti ke `targetHeight = ViewGroup.LayoutParams.MATCH_PARENT` saat `bounds == null` agar RelativeLayout secara atomik memperluas host mengikuti `accessory_stack_container`.

2. **Bypass Pre-Draw Reveal Gate (`syncDisplayTouchpad`)**:
   - `syncDisplayTouchpad` sebelumnya langsung memanggil `container.setVisibility(View.VISIBLE)` sebelum memanggil `raiseDisplayFrameKeyboard()`.
   - Hal ini membuat `keyboardContainer.getVisibility() == View.GONE` menjadi `false` di dalam `KeyboardGeometryChoreographer.onVisibilityRequested(true)`.
   - Efeknya: `openingFromGone` bernilai `false`, sehingga `shouldDeferReveal` bernilai `false`. Kontainer digambar seketika sebelum unified backdrop siap, menyebabkan warna abu-abu default / wallpaper tembus terlihat oleh pengguna.
   - **Solusi**: Hanya set `container.setVisibility(View.VISIBLE)` jika `!willRaiseKeyboard`. Biarkan `KeyboardGeometryChoreographer` mengelola transisi `INVISIBLE -> reveal gate -> VISIBLE`.

3. **Premature Veil Switch (`dressInAppKeyboardHost`)**:
   - Pada baris pengecekan `if (isUnifiedAccessoryBackdropReady(state) || isWholeFrameTouchpadShown())`, kondisi `|| isWholeFrameTouchpadShown()` membuat Termux langsung menganggap backdrop sudah siap dan memasang veil-only (`buildInAppKeyboardVeilOnly`).
   - Padahal `buildInAppKeyboardVeilOnly` mengembalikan `null` untuk whole-frame touchpad. Saat unified backdrop masih dalam proses render, background `surfaceHost` menjadi kosong (`null`), menyebabkan transparansi bolong ke wallpaper asli.
   - **Solusi**: Hapus `|| isWholeFrameTouchpadShown()`. `surfaceHost` hanya melepas background lokalnya jika `isUnifiedAccessoryBackdropReady(state)` benar-benar sudah siap (`true`). Selama belum siap, background lokal tetap aktif sebagai fallback penutup.

4. **Karakteristik Material Touchpad Meniru Extra Keys Dock 100%**:
   - **Blur Radius**: `obtainInAppKeyboardBackdropFrame` dan `buildInAppKeyboardSurfaceBackground` disetel menggunakan `isWholeFrameTouchpadShown() ? getEffectiveExtraKeysBlurRadius() : getEffectiveInAppKeyboardBlurRadius()`.
   - **Glass Grain**: Mengambil `mPreferences.getDockGlassGrain()` (bukan preferensi keyboard terpisah).
   - **Glass Tint & Rim**: Menggunakan `mPreferences.getDockTintStrength()`, tanpa rim garis pemisah keycap (`withRim(false)`), dan seams `ALL`.
   - **Decor Nav Bar Strip**: `navStripBlurRadiusDp`, `navStripBlurEnabled`, dan `buildDecorNavBarTint` diselaraskan agar saat whole-frame touchpad aktif, bilah gesture navigasi di bawah mengikuti blur & grain dock Extra Keys.

---

## 4. File-File Kunci yang Dimodifikasi

- `app/src/main/java/com/termux/app/TermuxActivity.java`:
  - `applyAccessoryLayerBounds`: `targetHeight = ViewGroup.LayoutParams.MATCH_PARENT`.
  - `navStripBlurRadiusDp` & `navStripBlurEnabled`: Guard `isWholeFrameTouchpadShown()`.
  - `buildDecorNavBarTint`: Guard `isWholeFrameTouchpadShown()`.
  - `dressInAppKeyboardHost`: Hapus `|| isWholeFrameTouchpadShown()` dari percabangan `isUnifiedAccessoryBackdropReady(state)`.
  - `buildInAppKeyboardSurfaceBackground`: Spec specifier fallback untuk `wholePad` dengan blur & grain Extra Keys.
  - `obtainInAppKeyboardBackdropFrame`: Radius blur memakai `getEffectiveExtraKeysBlurRadius()` saat `isWholeFrameTouchpadShown()`.
  - `paintsNoInAppKeyboardBackdrop`: Hapus bypass prematur `isWholeFrameTouchpadShown()`.
  - `syncDisplayTouchpad`: `container.setVisibility(View.VISIBLE)` dijaga dengan `!willRaiseKeyboard`.

---

## 5. Ringkasan Status Saat Ini

- **Commit Terakhir**: `4cc4aa2b5` (*"fix(touchpad): unify blur and grain with extra keys when opening mouse mode from closed keyboard"*).
- **CI Build**: Berhasil (Run `#39338427761`).
- **Instalasi**: Berhasil di-deploy dan terinstal di Infinix Note 30 VIP.
