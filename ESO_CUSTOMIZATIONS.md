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

### D. Dukungan Native Chroot Distro (NetHunter, Droidspaces, Custom Rootfs) & Integrasi X11 Canvas
- **Konteks & Kebutuhan**:
  - Di menu Setup Linux Apps sebelumnya hanya ada 2 opsi route: `Termux X11 repo` dan `proot distro`.
  - Tuan Eso membutuhkan opsi ke-3 yaitu `Chroot distro` yang tidak di-hardcode ke satu OS saja, melainkan dinamis mendukung **Kali NetHunter**, **Droidspaces**, maupun **Custom rootfs**.
- **Solusi & Arsitektur**:
  1. **Route Ketiga di Settings**: Menambahkan `Chroot distro` (`chroot`) di `arrays.xml`, `GuiAppsSetup.java`, dan `gui_apps_setup_preferences.xml`.
  2. **Non-Root Guard**:
     - `SegmentedPillPreference` diperluas dengan method `setSegmentEnabled(String value, boolean enabled)`.
     - Pada perangkat non-root (`!ChrootDistro.isRootAvailable()`), tombol `Chroot distro` otomatis menjadi abu-abu (alpha 0.38f) dan dinonaktifkan (tidak bisa diklik).
  3. **Auto-Scan `/data/local` (`ChrootDistro.java`)**:
     - Tombol **Scan /data/local** memindai container secara otomatis:
       - **Kali NetHunter**: `/data/local/nhsystem/kali-arm64` (runner `su -mm -c "chroot ..."`).
       - **Droidspaces**: `/data/local/Droidspaces/Containers/<Nama>/rootfs` (runner `su -c '/data/local/Droidspaces/bin/droidspaces --name="<Nama>" enter ...'`).
       - **Custom Rootfs**: Membuka input teks bebas untuk path rootfs dan custom command template (`%ROOTFS%`, `%COMMAND%`).
  4. **Koneksi Tombol Start di Canvas Kanan**:
     - `TermuxActivity.java` (`startEmbeddedDisplayNow`) dan `X11CliInstaller.java` (`forPrefix`) secara otomatis mengarahkan `-xstartup` ke launcher desktop chroot aktif saat route chroot dipilih.
  5. **Integrasi App Drawer**:
     - `LinuxAppCatalog.java` mendaftarkan `ChrootDistro.installed()` sehingga seluruh file `.desktop` dari NetHunter dan Droidspaces otomatis muncul di App Drawer Linux Apps.
  6. **Auto Bind-Mount Socket X11 & Peluncuran Sesi Desktop Penuh**:
     - `ChrootDistro.java` secara otomatis melakukan bind-mount socket X11 host `/data/data/com.termux/files/usr/tmp/.X11-unix` ke `<rootfs>/tmp/.X11-unix` sebelum chroot dijalankan (`mountpoint -q ... || mount -o bind ...`).
     - Peluncuran desktop chroot diarahkan ke sesi XFCE4 yang lengkap dengan `LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1` dan `dbus-launch --exit-with-session startxfce4` persis seperti `x11-start` bawaan Kali NetHunter.
     - `X11CliInstaller.writeWindowManagerScript()` otomatis dipanggil saat route diubah dan saat "Start display" ditekan agar `termux-x11-wm` selalu sinkron.
  7. **Perbaikan Sesi Manager & Indikator Baterai (NetHunter Proper Setup)**:
     - **Masalah**: Sesi manager dan widget baterai di panel XFCE NetHunter sempat tidak akurat / error karena UPower tidak dapat berkomunikasi dengan sistem bus.
     - **Solusi**:
       - Memulai daemon System D-Bus dengan `/etc/init.d/dbus start` sebelum session diluncurkan (tanpa ini, `/var/run/dbus/system_bus_socket` tidak tersedia, sehingga `upower` dan `xfce4-power-manager` putus).
       - Menetapkan environment lengkap: `DISPLAY=${DISPLAY:-:0}`, `PULSE_SERVER=tcp:127.0.0.1:4713`, `XKB_CONFIG_ROOT=/usr/share/X11/xkb`, dan `LD_PRELOAD=/usr/lib/aarch64-linux-gnu/libgcc_s.so.1`.
       - Memperbaiki visibilitas `X11CliInstaller.forPrefix(Context)` menjadi `public` agar dapat dipanggil dari `TermuxActivity` dan preferences fragment tanpa error kompilasi.
  8. **Tombol Emergency Stop & Dynamic DE Killer di Status Bar Canvas Kanan (Opsi A)**:
     - **Masalah**: Layar hitam dapat terjadi jika DE/WM hang atau crash sebelum sempat menggambar jendela X11. Saat itu, `mDisplayWindows` kosong sehingga status bar tidak memiliki tombol apapun untuk menghentikan display.
     - **Solusi**:
       - `TermuxActivity.syncWindowBarItems` selalu menyisipkan chip root display session (`NetHunter`, `Droidspaces`, `PRoot`, atau `X11 Display`) pada index 0 saat display aktif di canvas kanan.
       - **Two-Step Confirmation**: Klik pertama pada chip membuka tombol `[×]` (close). Klik pada `[×]` mengeksekusi pembersihan total. Klik di luar chip membatalkan (dismiss) tombol `[×]`.
       - **Dynamic DE Killer (`ChrootDistro.killDesktopEnvironmentAsync`)**: Menargetkan seluruh DE/WM populer (`xfce4`, `gnome`, `plasma`, `kwin`, `mate`, `lxde`, `lxqt`, `openbox`, `fluxbox`, `cinnamon`, `i3`, `bspwm`, `awesome`, dll.) secara dinamis tanpa di-hardcode ke XFCE saja.
       - **Proteksi Droidspaces**: Hanya membunuh proses DE di dalam container (`droidspaces enter -- pkill`), tanpa membunuh container Droidspaces itu sendiri.
       - **Pembersihan Bersih**: Menghentikan server `termux-x11`, menghapus socket & pid file, serta mereset tampilan canvas kanan seketika kembali ke state *"No display is running / Start display"*.

### E. Pemilihan Desktop Environment Dinamis (Metode C) & Multi-DE Switching
- **Konteks & Kebutuhan**:
  - Tuan Eso menginginkan kemampuan memilih Desktop Environment / Window Manager (misal XFCE, GNOME, Openbox, KDE Plasma, MATE, LXDE, LXQt, i3, dll.) melalui dropdown menu di pengaturan (*Setup Linux apps*).
  - Menu dropdown tersebut **hanya membaca dan menampilkan DE/WM yang benar-benar ter-install** di environment aktif (NetHunter Chroot / Droidspaces / PRoot / Termux Host X11).
  - Ketika DE dipilih (misalnya beralih dari XFCE ke GNOME), peluncuran canvas kanan langsung mengeksekusi DE yang dipilih tersebut, dan Emergency Stop tetap mampu menghentikan seluruh prosesnya secara bersih.
- **Solusi & Arsitektur**:
  1. **Dynamic Scanner (`ChrootDistro.detectInstalledDesktops`)**:
     - Memindai biner eksekusi dari daftar katalog DE yang didukung (`SUPPORTED_DESKTOPS`):
       - `xfce4` (`startxfce4`, `xfce4-session`)
       - `gnome` (`gnome-session`)
       - `plasma` (`startplasma-x11`, `plasma-desktop`)
       - `mate` (`mate-session`)
       - `lxde` (`startlxde`, `lxsession`)
       - `lxqt` (`startlxqt`)
       - `cinnamon` (`cinnamon-session`)
       - `openbox` (`openbox-session`, `openbox`)
       - `i3` (`i3`)
       - `awesome` (`awesome`)
       - `bspwm` (`bspwm`)
       - `fluxbox` (`startfluxbox`, `fluxbox`)
     - Deteksi bekerja otomatis untuk semua rute: Chroot NetHunter / Custom (`<rootfs>/usr/bin`), Droidspaces container (`droidspaces enter -- which`), PRoot distro (`installed-rootfs/<distro>/usr/bin`), dan Termux host (`$PREFIX/bin`).
  2. **Dropdown Menu di Setup Linux Apps (`GuiAppsSetupPreferencesFragment.java` & `gui_apps_setup_preferences.xml`)**:
     - Ditambahkan `PreferenceCategory` *Desktop Environment* dengan `ListPreference` `gui_apps_selected_de`.
     - Dropdown hanya diisi oleh entri yang lolos deteksi (misal di Kali NetHunter Tuan Eso terdeteksi `XFCE 4`, `GNOME`, dan `Openbox`).
     - Jika rute, container chroot, atau distro proot diubah, dropdown otomatis melakukan re-scan di background thread (`probeExecutor`) dan memperbarui entri seketika.
  3. **Penyusunan Perintah Eksekusi Dinamis (`ChrootDistro.resolveActiveDesktopCommand`)**:
     - Menyatukan resolusi perintah desktop untuk `TermuxActivity.startEmbeddedDisplayNow`, `X11CliInstaller.forPrefix`, dan `X11LinuxAppRunner.configuredWindowManager`.
     - Setiap DE memiliki penanganan runner khusus:
       - **GNOME**: `export XDG_SESSION_TYPE=x11; exec dbus-launch --exit-with-session gnome-session` (mencegah GNOME mencoba Wayland dalam chroot).
       - **XFCE**: `exec dbus-launch --exit-with-session startxfce4` dengan D-Bus system bus fix untuk battery meter.
       - **Openbox**: `exec openbox-session` / `exec openbox`.
       - **Droidspaces**: Disesuaikan dengan session runner Droidspaces `dbus-run-session ...`.
     - Memanggil `X11CliInstaller.writeWindowManagerScript()` seketika saat pilihan DE diubah sehingga script `termux-x11-wm` selalu sinkron tanpa perlu restart aplikasi.

---

### F. Otomatisasi VirGL (Hardware Acceleration) & PulseAudio (Audio) untuk Chroot, PRoot, & Native
- **Konteks & Kebutuhan**:
  - Tuan Eso menggunakan SoC **MediaTek Dimensity 1200 (MT6893)** dengan GPU **ARM Mali-G77 MC9**.
  - Menjalankan VirGL server (`virgl_test_server_android --angle-gl`) dan PulseAudio (`pulseaudio`) secara manual via terminal sangat merepotkan.
  - Tuan Eso menginginkan agar saat **Start display** ditekan di Canvas Kanan, VirGL server dan PulseAudio otomatis menyala di latar belakang, soketnya otomatis di-bind mount ke Chroot (NetHunter), serta env vars VirGL dan PulseAudio disuntikkan secara otomatis.
  - Saat tombol Emergency Stop `[×]` ditekan di Canvas Kanan, seluruh service X11, DE/WM, soket mounts, server VirGL, dan PulseAudio otomatis dimatikan dan dibersihkan secara tuntas.
- **Solusi & Arsitektur**:
  1. **Auto-Start Audio & Grafis (`ChrootDistro.startAudioAndGraphicsServices`)**:
     - **PulseAudio**: Mendeteksi `/data/data/com.termux/files/usr/bin/pulseaudio`. Jika belum berjalan, menyalakan server PulseAudio TCP di port `4713` dengan module native protocol (`--load="module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" --exit-idle-time=-1`).
     - **VirGL Server**: Mendeteksi `/data/data/com.termux/files/usr/bin/virgl_test_server_android`. Jika belum berjalan, membuat direktori soket `/data/data/com.termux/files/usr/tmp/.virgl/` dengan izin `0777`. Mendeteksi ketersediaan ANGLE (library `libEGL_angle.so`) untuk menyuntikkan flag `--angle-gl`, lalu menjalankan `virgl_test_server_android` dengan opsi `--socket-path /data/data/com.termux/files/usr/tmp/.virgl/.virgl_test` dan membuat symlink di `$TMPDIR/.virgl_test`.
     - Dipanggil otomatis pada `TermuxActivity.startEmbeddedDisplayNow()` sesaat sebelum display X11 dinyalakan.
  2. **Auto-Mount Soket & Inject Environment di Chroot (NetHunter)**:
     - Folder soket `.virgl` otomatis di-bind mount: `mkdir -p <rootfs>/tmp/.virgl && mount -o bind /data/data/com.termux/files/usr/tmp/.virgl <rootfs>/tmp/.virgl`.
     - Symlink `<rootfs>/tmp/.virgl_test -> /tmp/.virgl/.virgl_test` dibuat di dalam chroot.
     - Environment variabel VirGL dan PulseAudio otomatis disuntikkan:
       - `PULSE_SERVER=tcp:127.0.0.1:4713`
       - `GALLIUM_DRIVER=virpipe`
       - `MESA_GL_VERSION_OVERRIDE=4.3COMPAT`
       - `MESA_GLES_VERSION_OVERRIDE=3.2`
       - `MESA_NO_ERROR=1`
       - `LIBGL_DRI3_DISABLE=1`
       - `VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test`
     - Bekerja baik untuk peluncuran sesi desktop penuh (`getDesktopCommand`) maupun peluncuran aplikasi mandiri dari App Drawer (`buildRunner`).
  3. **Auto-Forward di PRoot & Native Route**:
     - PRoot (`resolveActiveDesktopCommand` route DISTRO): menambahkan `--shared-tmp` dan menyuntikkan env vars VirGL + `PULSE_SERVER=tcp:127.0.0.1:4713`.
     - Droidspaces: menyuntikkan `PULSE_SERVER=tcp:127.0.0.1:4713` dan flag `--virgl`.
     - Route Native: menyuntikkan env vars VirGL dan `PULSE_SERVER=tcp:127.0.0.1:4713`.
  4. **Pembersihan Bersih / Auto-Cleanup (`ChrootDistro.killDesktopEnvironment`)**:
     - Mematikan proses DE dan D-Bus di seluruh container dan chroot.
     - Melakukan unmount bersih untuk soket `.virgl` dan `.X11-unix` di dalam chroot (`umount <rootfs>/tmp/.virgl` dan `umount <rootfs>/tmp/.X11-unix`).
     - Menghentikan daemon `virgl_test_server_android` (`killall -9 virgl_test_server_android`).
     - Menghentikan daemon `pulseaudio` (`pulseaudio --kill` dan fallback `pkill -9 -f pulseaudio`).
     - Menghapus lingering sockets dan temporary files di Termux host tmp.
     - Dipanggil baik saat tombol Emergency Stop `[×]` ditekan (`stopAndKillAllDisplayServices`), maupun saat display server dimatikan secara normal (`killEmbeddedDisplayServer`).

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
