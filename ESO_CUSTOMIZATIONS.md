# ESO Launcher — Customizations & Maintenance Guide

Dokumen ini berisi catatan perubahan, arsitektur modifikasi, dan petunjuk teknis lengkap untuk AI Agent atau pengembang yang melanjutkan maintenance repository fork ini (`eso-launcher` / `eryshaqilla-wq/termux-launcher`).

---

## 1. Profil Proyek & Aturan Pengguna
- **Pemilik / User**: **Tuan Eso** (selalu panggil dengan sebutan kehormatan ini).
- **Branch Kerja Utama**: `eso-launcher` (dan disinkronkan ke `main` untuk trigger CI build nightly).
- **Upstream Original**: `https://github.com/PickleHik3/termux-launcher`
- **Origin Fork**: `https://github.com/eryshaqilla-wq/termux-launcher`
- **Lingkungan Kerja**: Linux / Termux zsh (`~/.zshrc`), folder kerja `/project` (symlink ke `/root/project`).
- **Target Device Testing**: `127.0.0.1:5555` (Android 13 / arm64-v8a).

---

## 2. Riwayat Modifikasi Utama (Kustomisasi Tuan Eso)

### A. Nonaktifkan Touch Tilt / Wobble & Optimasi Zero-Lag
- **Masalah**: Efek wobble / tilt default pada plank dock dan tombol extra keys menyebabkan stutter / lag saat mengetik cepat.
- **Solusi**:
  - Dibuatkan switch kontrol dan dinonaktifkan secara default untuk menjamin performa input 100% instan tanpa latency.
  - Memastikan listener touch dan layout traversal tidak melakukan re-render berlebihan saat input diketikkan.
- **File**: `app/src/main/java/com/termux/app/TermuxActivity.java` (seputar handling plank controller & wobble animation).

### B. Perbaikan Layar Hitam (Blackscreen Wallpaper)
- **Masalah**: Wallpaper kustom / managed wallpaper sempat menjadi hitam pekat (blackscreen) setelah reload atau perubahan setting glass.
- **Solusi**:
  - Memisahkan background dimming activity terminal dari sampling wallpaper managed blur source.
  - Memperbaiki penanganan `mManagedWallpaperSource` dan `SharedFrameDrawable` agar selalu merender frame wallpaper dengan fallback yang aman saat opacity bernilai 100%.
- **File**: `app/src/main/java/com/termux/app/TermuxActivity.java` (`applyUnifiedBackgroundDim`, `shouldUseManagedWallpaperBlurSource`, `updateDecorNavBarBackdrop`).

### C. Penutupan Celah Wallpaper di Batas Bawah Layar (Bottom Seam Leak Fix)
- **Masalah**: Di bagian paling bawah layar (bawah tombol `CTRL`, `TAB`, `PGDN` baris kedua extra keys), terdapat celah selebar ~8 piksel (`y = 2392..2399`) yang transparan tembus ke wallpaper asli.
- **Penyebab**:
  1. `accessory_stack_container` memiliki bottom padding dinamis `flushBottomInsetPx = 8px` untuk memusatkan tombol extra keys secara vertikal.
  2. `accessory_surface_host` (lapisan kaca & tint latar dock) berhenti di atas padding tersebut (`bottomMargin = 0`), sehingga 8 piksel di dasar container menjadi kosong melompong.
  3. Pada HP dengan gesture nav / inset 0 atau reporting decor view yang sedikit berbeda dari fisik, lapisan bawah tidak tertutup.
- **Solusi**:
  1. **`applyAccessoryLayerBounds`**: Untuk default dock (`viewId == R.id.accessory_surface_host && !capsuleSurface && bounds == null`), `targetBottomMargin` diatur negatif menembus padding container ditambah overscan aman (`-(containerPaddingBottom + bottomOverscan)`). Top margin tetap 0 sehingga tombol tidak bergeser, namun latar kaca/tint meluas ke bawah menutupi celah hingga melewati bezel.
  2. **`applyDecorNavBarSurfaceBounds`**: Menerapkan downward overscan `bottomOverscanPx = Math.max(12, Math.round(density * 4f))` dengan `targetHeight = baseHeight + bottomOverscanPx` dan `targetBottomMargin = -bottomOverscanPx`.
  3. **`measuredUnderPillStripHeightPx` & `buildDecorNavBarBackdropTargetRect`**: Menggunakan `realMetrics.heightPixels` dari display hardware agar perhitungan tinggi tidak terpotong oleh sistem insets.
  4. **`updateDecorNavBarBackdrop`**: Menambahkan overscan bawah ke `targetBackdropHeight` pada layout params backdrop.

---

## 3. Petunjuk Sinkronisasi Update dari Upstream (Anti-Tertimpa)

Jika upstream (`PickleHik3/termux-launcher`) merilis update baru dan Tuan Eso meminta sync, **JANGAN PERNAH reset atau clone ulang dari 0**. Ikuti langkah standar berikut:

### Langkah 1: Fetch commit terbaru dari upstream
```bash
git fetch upstream main
```

### Langkah 2: Lakukan Rebase (Metode Terbaik)
```bash
git checkout eso-launcher
git rebase upstream/main
```
*Mengapa rebase?* Git akan mengangkat commit kustom Tuan Eso, memperbarui base code ke versi upstream terbaru, lalu menaruh kembali commit Tuan Eso di puncak git tree.

### Langkah 3: Jika Terjadi Conflict
Jika ada konflik pada file (misal di `TermuxActivity.java`):
1. Buka file yang berkonflik.
2. Pertahankan blok kode kustom Tuan Eso (khususnya logika overscan bottom margin di `applyAccessoryLayerBounds` dan `applyDecorNavBarSurfaceBounds`).
3. Lanjutkan rebase:
   ```bash
   git add app/src/main/java/com/termux/app/TermuxActivity.java
   git rebase --continue
   ```

### Langkah 4: Sinkronkan ke Branch `main` & Push ke `origin`
```bash
git checkout main
git reset --hard eso-launcher
git push origin eso-launcher main --force-with-lease
```
*Catatan: `--force-with-lease` aman digunakan setelah rebase untuk memperbarui commit history di remote fork Tuan Eso.*

---

## 4. Prosedur Build CI & Deploy ke Perangkat

### GitHub Actions CI
- Push ke branch `main` atau `eso-launcher` otomatis men-trigger workflow `.github/workflows/debug_build.yml` ("Build nightly").
- Token akses GitHub: Menggunakan token GitHub pengguna yang tersimpan di remote origin / environment secret.

### Script Otomatisasi Deploy
Script monitoring dan deployment otomatis tersedia di direktori scratch:
`/root/.gemini/antigravity-cli/brain/442dbc86-62d0-4dee-ac7a-db4a0a44c080/scratch/deploy.sh`

Langkah manual jika script perlu dijalankan ulang:
1. Pantau status run via GitHub API / CLI:
   ```bash
   curl -s -H "Authorization: token $TOKEN" "https://api.github.com/repos/eryshaqilla-wq/termux-launcher/actions/runs?per_page=1"
   ```
2. Unduh artifact zip `termux-app_v*arm64-v8a.apk`.
3. Pasang ke HP via ADB:
   ```bash
   adb -s 127.0.0.1:5555 install -r <nama-file-apk>.apk
   adb -s 127.0.0.1:5555 shell am force-stop com.termux
   adb -s 127.0.0.1:5555 shell am start -n com.termux/com.termux.app.TermuxActivity
   ```
4. Verifikasi piksel bawah:
   Pastikan baris `y = 2390..2399` konsisten tertutup latar gelap kaca dock dan tidak tembus warna wallpaper terang.

---

## 5. Hal Penting yang Tidak Boleh Diubah
1. **Jangan aktifkan animasi tilt/wobble per frame pada touch event**: Tuan Eso menyukai terminal yang responsif dan bebas lag.
2. **Jangan hapus penanganan overscan di `applyAccessoryLayerBounds`**: Jika `targetBottomMargin` dikembalikan ke `0`, celah wallpaper 8 piksel di dasar layar akan muncul kembali.
3. **Posisikan elemen visual tetap di dalam `terminal_root_container` / `accessory_stack_container`**: Jangan mengubah tata letak tombol keyboard / extra keys ke luar view hierarchy yang sudah rapi.
