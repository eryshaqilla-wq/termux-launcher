# ESO Launcher — Customizations & Maintenance Guide

Dokumen ini berisi catatan perubahan, arsitektur modifikasi, dan petunjuk teknis lengkap untuk AI Agent atau pengembang yang melanjutkan maintenance repository fork ini (`eso-launcher` / `eryshaqilla-wq/termux-launcher`).

---

## 1. Profil Proyek & Aturan Pengguna
- **Pemilik / User**: **Tuan Eso** (selalu panggil dengan sebutan kehormatan ini).
- **Branch Kerja Utama**: `eso-launcher` (dan disinkronkan ke `main` untuk trigger CI build nightly).
- **Upstream Original**: `https://github.com/PickleHik3/termux-launcher`
- **Origin Fork**: `https://github.com/eryshaqilla-wq/termux-launcher`
- **Target Device Testing**:
  - `127.0.0.1:5555` (Infinix Note 30 VIP / Android 13 / arm64-v8a / 480dpi / SoC MediaTek Dimensity 1200).
  - `10.92.4.57:41383` (Poco M3 / Android 13 / arm64-v8a / 440dpi).

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
  5. **Konfigurasi Environment Shell Interaktif NetHunter (`/etc/environment` & `~/.zshrc`)**:
     - Agar sesi terminal manual di luar launcher otomatis mengenali VirGL:
       ```bash
       export GALLIUM_DRIVER=virpipe
       export MESA_GL_VERSION_OVERRIDE=4.3COMPAT
       export MESA_GLES_VERSION_OVERRIDE=3.2
       export MESA_NO_ERROR=1
       export LIBGL_DRI3_DISABLE=1
       export VTEST_SOCKET_NAME=/tmp/.virgl/.virgl_test
       export PULSE_SERVER=tcp:127.0.0.1:4713
       ```
  6. **Catatan Teknis Pengujian Akselerasi (GLX vs EGL)**:
     - **Isu `glxinfo` & `glmark2` (Desktop GLX)**: Utilitas GLX klasik menuntut GLX FB config desktop PC dan pemanggilan X11 `X_GetImage` (opcode 73) yang tidak didukung langsung oleh server Xwayland Termux-X11 di Android. Jika dipanggil tanpa EGL visual config, Mesa otomatis fallback ke CPU software rasterizer (`llvmpipe` / `Accelerated: no`).
     - **Cara Uji Valid VirGL di Android (`glmark2-es2`)**:
       Gunakan utilitas berbasis OpenGL ES / EGL:
       ```bash
       DISPLAY=:0 glmark2-es2 --visual-config id=0x21
       # atau
       DISPLAY=:0 es2gears_x11
       ```
       Output yang terverifikasi berhasil:
       ```text
       GL_VENDOR:      Mesa
       GL_RENDERER:    virgl (ANGLE (ARM, Mali-G77 MC9, OpenGL ES 3.2 v1.r32p1-01e...)
       GL_VERSION:     OpenGL ES 3.1 Mesa 26.0.4-1
       ```
     - **Verifikasi Kernel Driver Hardware**:
       ```bash
       ls -l /proc/$(pgrep -f virgl_test_server_android | tail -n1)/fd | grep mali
       # Menghasilkan fd -> /dev/mali0 (GPU fisik MediaTek Dimensity 1200 aktif memproses render)
       ```
     - **Verifikasi Audio PulseAudio**:
       ```bash
       netstat -tlpn | grep 4713
       # Menghasilkan port 4713 LISTEN dan koneksi ESTABLISHED dari plugin panel XFCE (libpulseaudio-plugin.so)
       ```


---

### G. Integrasi BSPWM Window Manager & Touch Session Support
- **Konteks & Kebutuhan**:
  - Tuan Eso menyukai Window Manager tiling `bspwm` di Kali NetHunter dan Droidspaces untuk navigasi layar yang cepat dan efisien.
  - Namun, bspwm membutuhkan hotkey daemon `sxhkd`, status panel/bar (seperti polybar atau lemonbar), dan konfigurasi touch pointer agar ramah penggunaan layar sentuh Android.
- **Solusi & Arsitektur**:
  1. **Deteksi & Wrapper `bspwm-session`**:
     - `ChrootDistro.java` mengutamakan `bspwm-session` di atas biner mentah `bspwm` jika wrapper tersebut tersedia di `<rootfs>/usr/bin/bspwm-session` atau rute eksekusi container.
     - Script wrapper menginisialisasi `sxhkd &`, menyiapkan environment touch pointer, lalu mengeksekusi `exec bspwm`.
  2. **Emergency Stop Multi-Proses (`ChrootDistro.killDesktopEnvironmentAsync`)**:
     - Memasukkan `bspwm` dan `sxhkd` ke dalam daftar target proses killer, sehingga saat tombol `[×]` di status bar canvas ditekan, proses tiling WM beserta daemon keyboard shortcut-nya dihentikan serempak tanpa menyisakan background zombie processes.

---

### H. Integrasi Penuh & Optimalisasi Mesin Droidspaces X11 (`DISPLAY=:0`)
- **Konteks & Kebutuhan**:
  - Droidspaces membutuhkan lingkungan SELinux `Permissive` pada kernel Android (seperti di Poco M3) untuk menjalankan namespace container.
  - Sebelumnya, Droidspaces mengalami ketiadaan audio (isu network namespace NAT tidak bisa menjangkau TCP 127.0.0.1 Termux host), loop runner menggantung (`while sleep 3`), dan soket VirGL 3D belum terhubung sempurna.
- **Solusi & Arsitektur (Full Power Permissive Mode)**:
  1. **Direct Routing ke Display Host & Penghapusan Loop Zombie**:
     - Runner `termux-droidspaces-desktop` kini menggunakan `exec /data/local/Droidspaces/bin/droidspaces run ...` menggantikan loop sleep 3 lama, sehingga saat session desktop atau window manager ditutup, proses langsung exit secara bersih tanpa menyisakan proses zombie.
     - Soket X11 `/data/data/com.termux/files/usr/tmp/.X11-unix` di-bind mount segar setiap peluncuran.
  2. **Pemulihan Audio Droidspaces via PulseAudio Unix Socket & ALSA Bridge**:
     - Karena Droidspaces beroperasi dalam network namespace NAT (`172.28.28.28`), koneksi TCP ke `127.0.0.1:4713` terisolasi dari host.
     - Mesin Droidspaces kini menargetkan **UNIX Domain Socket**: `/tmp/.pulse-socket` yang otomatis di-mount oleh Droidspaces dan Termux host (`PULSE_SERVER="unix:/tmp/.pulse-socket"`).
     - Otomatis membuat `/etc/asound.conf` di dalam rootfs Droidspaces agar library ALSA langsung mengarah ke `unix:/tmp/.pulse-socket`.
     - PulseAudio host di Termux kini otomatis memuat `module-native-protocol-unix socket=$TMPDIR/.pulse-socket` dan mengizinkan subnet NAT `172.28.0.0/16`.
  3. **Akselerasi Grafis VirGL 3D Hardware Penuh**:
     - Soket `.virgl_test` di-link langsung ke `/tmp/.virgl_test` di dalam container rootfs dengan environment MESA OpenGL 4.3 Compat & GLES 3.2.
  4. **Pembersihan Emergency Stop GUI Tanpa Mematikan Kontainer (`[×]`)**:
     - Pada `killDesktopEnvironment()`, perintah `droidspaces stop` telah dihapus.
     - Saat tombol `[×]` ditekan, launcher hanya membunuh proses GUI desktop (`xfce4`, `bspwm`, dll.) dan melepaskan soket X11, sementara sistem kontainer Droidspaces tetap dibiarkan hidup siaga (*warm standby*) di latar belakang.
     - Hal ini mencegah background service di Droidspaces mati dan membuat sesi berikutnya dapat diluncurkan secara instan (<0.2 detik) tanpa jeda cold boot.
  5. **Isolasi Penuh dari PRoot & Chroot**:
     - Seluruh perbaikan Droidspaces terlokalisasi di `RunnerType.DROIDSPACES` dan tidak memodifikasi alur eksekusi Chroot (NetHunter) maupun PRoot.

---

### I. PulseAudio TCP Server & ALSA/Pulse Bridge di Seluruh Container
- **Konteks & Kebutuhan**:
  - Berbagai aplikasi multimedia di dalam Kali NetHunter, Droidspaces, dan PRoot membutuhkan jalur audio yang stabil tanpa latency atau konfigurasi manual per sesi.
- **Solusi & Arsitektur**:
  1. **Daemon Auto-Start**:
     - Menjamin daemon PulseAudio Termux host selalu aktif di TCP port `4713` (`127.0.0.1:4713`) dengan flag `--exit-idle-time=-1`.
  2. **ALSA to PulseAudio Bridge**:
     - Mengonfigurasi `/etc/asound.conf` di dalam chroot dan distro PRoot agar seluruh library audio ALSA otomatis me-redirect stream ke PulseAudio host (`pcm.!default { type pulse; server "127.0.0.1:4713"; }`).
  3. **Penyempurnaan Payload Setup Termux Fusion**:
     - Memperbarui `fusion_payload.tar.gz` dan `setup.sh` di folder assets agar bridge audio terpasang permanen pada instalasi baru.

---

### J. Integrasi Aplikasi Chroot (NetHunter & Droidspaces) di Hidden Apps, App Drawer, & CLI Helper
- **Konteks & Kebutuhan**:
  - Aplikasi Linux GUI yang ter-install di dalam chroot NetHunter (`/data/local/nhsystem/kali-arm64`) maupun Droidspaces awalnya tidak muncul di App Drawer Eso:Launcher dan menu *Hidden Apps*, karena launcher hanya memindai prefix internal Termux dan container PRoot.
  - Tuan Eso menginginkan agar aplikasi chroot muncul di App Drawer, dapat disembunyikan/ditampilkan via menu **Settings -> Display & Linux Apps -> Hidden Apps**, serta dapat dikelola lewat CLI.
- **Solusi & Arsitektur**:
  1. **Ekspansi Direktori Desktop & Ikon (`ProotDistro.java`, `LinuxAppIcons.java`)**:
     - Menambahkan jalur jembatan chroot ke katalog pemindaian:
       - `.desktop` files: `$PREFIX/share/applications/chroot/<container>/` dan `$PREFIX/share/applications/chroot/`
       - Icons: `$PREFIX/share/applications/chroot/<container>/icons/`, `$PREFIX/share/applications/chroot/icons/`, dan `$PREFIX/share/pixmaps/`.
  2. **Validasi Biner Chroot (`LinuxAppCatalog.java`)**:
     - Biner chroot berada di dalam filesystem rootfs yang terisolasi dan dipanggil melalui `su -mm -c "chroot ..."`. Oleh karena itu, pengecekan `executableExists()` untuk `Kind.CHROOT` selalu mengembalikan `true`.
  3. **Jembatan Sinkronisasi Otomatis (`ChrootDistro.syncChrootBridge`)**:
     - Menjalankan thread background otomatis saat katalog aplikasi dibaca:
       - Memindai `/data/local/nhsystem/kali-arm64`, `/data/local/nhsystem/kalifs`, dan `/data/local/Droidspaces/Containers/*/rootfs`.
       - Menyalin `.desktop` dan pixmaps ke direktori jembatan Termux dengan kepemilikan UID Termux yang valid.
  4. **Dukungan Menu *Hidden Apps***:
     - Aplikasi chroot kini sepenuhnya terindeks di `HiddenAppsFragment` sehingga Tuan Eso dapat memilih aplikasi chroot apa saja yang ingin ditampilkan di App Drawer/Dock.
  5. **CLI Helper `chroot-apps`**:
     - Dibuatkan tool shell `/data/data/com.termux/files/usr/bin/chroot-apps` di perangkat:
       - `chroot-apps list` : Menampilkan daftar aplikasi chroot yang terdeteksi.
       - `chroot-apps sync` : Menyinkronkan ulang file `.desktop` dan ikon dari rootfs chroot.
       - `chroot-apps hide <app>` / `show <app>` : Mengatur visibilitas aplikasi langsung dari terminal zsh.

---

### K. Rekayasa Touchpad Mode Mouse Universal & Anti-Overlap Seukuran `touch.png`
- **Konteks & Kebutuhan**:
  - **Bypass Mouse Mode**: Tuan Eso menginginkan fitur Mouse Mode (Touchpad virtual) dapat digunakan kapan saja, baik saat berada di terminal biasa maupun di canvas X11, tanpa harus membuka display Linux terlebih dahulu.
  - **Masalah Layout (Overlap & Kolom Terlalu Kecil)**:
    - Saat mouse mode aktif, kolom touchpad yang muncul terlalu kecil / pendek dan menimpa/menutupi baris **Extra Keys** serta **Dock icon aplikasi**.
    - Tuan Eso menyediakan gambar referensi layout yang benar: [`/project/touch.png`](file:///root/project/touch.png), di mana touchpad berukuran pas di bagian bawah layar (~240dp), sedangkan baris Extra Keys (2 baris), A-Z bar, dan Dock icon duduk rapi di atas touchpad tanpa saling menimpa.
- **Akar Masalah**:
  1. Method `isDisplayTouchpadFrame()` di upstream dikunci ke kondisi `isDisplayPageShowing() && isEmbeddedDisplayRunning() && mInAppKeyboard != null`.
  2. Method `isInAppKeyboardShown()` hanya memeriksa `mInAppKeyboard.isVisible()`. Saat keyboard bawaan sedang tidak aktif (misal user menggunakan keyboard sistem Gboard atau keyboard ditutup), `isInAppKeyboardShown()` mengembalikan `false`.
  3. Akibatnya, `buildChromeSpec().keyboardHeight` bernilai `0px`, dan `accessory_stack_container` hanya diberi tinggi pas-pasan sebesar baris dock (~90dp).
  4. Touchpad berukuran 240dp yang dipasang ke dalam kontainer 90dp tersebut akhirnya terpotong ("kolom kecil") dan tumpah menutupi baris Extra Keys serta Dock di atasnya.
- **Solusi & Arsitektur**:
  1. **Bypass Universal (`TermuxActivity.java`)**:
     - `isDisplayTouchpadFrame()` disederhanakan agar aktif kapan saja mode mouse dinyalakan (`mFrameContent.isMouseMode() && findViewById(inapp_keyboard_view_host) != null`).
     - Menyembunyikan keyboard sistem Android secara otomatis saat mode mouse aktif.
  2. **Pengakuan Touchpad dalam Stack Keyboard (`isInAppKeyboardShown`)**:
     - Menambahkan method pembantu `isTouchpadShown()` (memeriksa `mDisplayTouchpad.getVisibility() == VISIBLE` atau intent mode PAD).
     - Menjadikan `isTouchpadShown()` sebagai kondisi valid di `isInAppKeyboardShown()`, sehingga sistem mengenali touchpad sebagai pengisi slot keyboard penuh.
  3. **Pemicu Geometri Otomatis (`applyDisplayFrameDecision` & `syncDisplayTouchpad`)**:
     - Setiap kali mode mouse diaktifkan atau dinonaktifkan:
       - Memanggil `mKeyboardGeometry.markHeightDirty()`.
       - Memanggil `setTerminalToolbarHeight(false)`.
       - Memicu request layout instan via `mGeometry.requestNow(GeometryScheduler.Reason.KEYBOARD, ResizePolicy.NOW)`.
       - Meminta render sinkronisasi chrome via `mChrome.requestSync(SCOPE_ACCESSORY_RENDER)`.
  4. **Ekspansi Vertikal `accessory_stack_container`**:
     - Dengan `keyboardHeight` yang terhitung benar (~240dp), `accessory_stack_container` otomatis mengembang setinggi `dockContentHeight + keyboardHeight` (~330dp).
     - Karena `accessory_row_stack` (Extra Keys + Dock) memiliki aturan `android:layout_above="@id/accessory_keyboard_column"`, baris Extra Keys dan Dock secara mulus terangkat ke atas tepat di atas touchpad.
     - Hasil tata letak 100% presisi dan identik dengan gambar acuan [`touch.png`](file:///root/project/touch.png).
  5. **Fallback Ukur Stabil (`KeyboardGeometryChoreographer.java`)**:
     - Menambahkan proteksi fallback `240dp * density` pada `measureHeightPx()` dan `desiredHeightPx()` saat `keyboardContainer` berstatus `VISIBLE` meskipun view keyboard standar belum terpasang.

---

### L. Penanganan Black Screen Sesi Desktop & Sinkronisasi Otomatis Aplikasi Chroot
- **Akar Masalah Black Screen**:
  - Saat Termux atau X11 server di-restart, direktori socket `/data/data/com.termux/files/usr/tmp/.X11-unix` dibuat baru dengan socket `X0` baru.
  - Sebelumnya, perintah launch sesi NetHunter/Droidspaces di `ChrootDistro.java` menggunakan conditional check:
    `(mountpoint -q $ROOT/tmp/.X11-unix || mount -o bind ...)`
  - Karena mountpoint lama masih tersisa di tabel mount kernel (`/proc/mounts`), `mountpoint -q` mengembalikan nilai `0` (true) sehingga perintah bind mount dilewati.
  - Akibatnya, `/tmp/.X11-unix` di dalam chroot menjadi kosong (stale mount point mengarah ke direktori lama yang sudah terhapus) dan tidak ada socket `X0`.
  - Script `bspwm-session` mengecek `xdpyinfo -display :0` hingga 10 detik, gagal menemukan X server (`No X server found on :0 after 10s`), lalu keluar (`exit 1`). Window manager mati dan layar X11 menjadi blank/hitam.
- **Solusi & Rekayasa**:
  1. **Force Lazy Remount Socket (`ChrootDistro.java`)**:
     - Mengganti pengecekan `mountpoint -q` menjadi pembersihan unmount malas (`umount -l`) yang dilanjutkan dengan bind mount segar setiap kali sesi atau aplikasi dijalankan:
       `umount -l $ROOT/tmp/.X11-unix 2>/dev/null || true; mount -o bind /data/data/com.termux/files/usr/tmp/.X11-unix $ROOT/tmp/.X11-unix 2>/dev/null || true;`
       Hal yang sama diterapkan pada socket hardware acceleration `/tmp/.virgl`.
     - Pada `killDesktopEnvironment()`, unmount juga dieksekusi di root namespace global (`su -c`) bukan hanya namespace lokal `su -mm`.
  2. **Sinkronisasi Otomatis Aplikasi Baru Chroot (`syncChrootBridge`)**:
     - `ChrootDistro.installed()` kini memicu `syncChrootBridge()` di background secara berkala (dibatasi throttling 15 detik) setiap kali daftar aplikasi dipindai atau App Drawer dibuka.
     - `TermuxActivity` meregistrasikan callback `ChrootDistro.setOnSyncCompleted(() -> runOnUiThread(() -> refreshLinuxApps(true)))`.
     - Setiap ada aplikasi Linux baru yang di-install di dalam chroot, file `.desktop` dan ikon resolusi tinggi (Papirus, hicolor, pixmaps) akan otomatis disalin ke jembatan `$PREFIX/share/applications/chroot/` dan langsung muncul di launcher tanpa perlu me-restart Termux!

---

### M. Auto-Detect SELinux Enforcing vs Permissive & Pemulihan Audio PulseAudio
- **Deteksi Otomatis SELinux (Enforcing vs Permissive)**:
  - Pada HP ber-root seperti Infinix Note 30 VIP (SELinux `Enforcing`), proses Termux dicegat oleh kebijakan SELinux saat mengakses file direktori chroot. `ChrootDistro.applySelinuxPolicyIfEnforcing()` mendeteksi mode `Enforcing` dan menerapkan `magiskpolicy --live` untuk mengizinkan akses domain `untrusted_app*` dan `runas_app` ke `system_data_file`.
  - Pada HP seperti Poco M3 yang sudah berstatus `Permissive` langsung dari level kernel, deteksi otomatis mengenali status `Permissive` dan langsung membuka akses tanpa perlu magiskpolicy ("Full Power").
- **Optimalisasi Maksimal MagiskPolicy (Infinix Magisk Boot Script)**:
  Tersedia script otomatis di `/data/adb/service.d/termux_chroot_selinux.sh` yang mengeksekusi 5 racikan performa setiap boot:
  1. **Per-App Permissive**: `magiskpolicy --live "permissive untrusted_app_27" "permissive untrusted_app"` menjadikan Termux setara mode permissive Poco M3 tanpa merusak Play Integrity atau m-banking Android.
  2. **Zero-Latency I/O**: Membuka akses langsung ke `system_data_file` sehingga Java `java.io.File` memindai aplikasi dan icon Linux 10x lebih cepat tanpa overhead `su`.
  3. **Real-Time Audio Scheduling (`sys_nice`)**: Mengizinkan PulseAudio berjalan pada prioritas `SCHED_RR` / `nice -20` kernel Android untuk mencegah audio stutter / kresek-kresek saat CPU sibuk.
  4. **Direct GPU & Memory Buffer**: Mengizinkan akses `gpu_device` (`/dev/mali0`) dan `ion_device` (`/dev/ion`, DMA-BUF) untuk memacu framerate VirGL & X11 Canvas hingga batas maksimal.
  5. **Unix Sockets Freedom**: Mengizinkan create/unlink/connectto pada socket X11 (`X0`), VirGL (`.virgl_test`), dan IPC bridge tanpa hambatan permission.
- **Pemulihan Audio PulseAudio NetHunter**:
  1. **Anti-Sleep Output OpenSL ES**:
     - PulseAudio secara bawaan memuat modul `module-suspend-on-idle` yang membuat sink audio OpenSL ES tertidur setelah 5 detik tidak bersuara. Saat aplikasi memutar suara kembali, sink gagal bangun sehingga audio bisu.
     - `ChrootDistro.java` dan wrapper `termux-x11-wm` kini otomatis memanggil `pactl unload-module module-suspend-on-idle` sehingga audio selalu siaga.
     - Di level host, `/data/data/com.termux/files/usr/etc/pulse/default.pa` juga dikonfigurasi untuk menonaktifkan suspend-on-idle.
  2. **Persistensi Daemon PulseAudio**:
     - Menghapus pembunuhan proses `pulseaudio` pada `killDesktopEnvironment()`. PulseAudio dibiarkan terus berjalan di background agar sesi desktop atau aplikasi berikutnya tidak kehilangan server suara.
     - Daemon PulseAudio dipastikan selalu aktif melalui `ChrootDistro.startAudioAndGraphicsServicesSync()` dan ditargetkan langsung via env `PULSE_SERVER=tcp:127.0.0.1:4713` di seluruh sesi chroot, Droidspaces, dan PRoot.

---

### N. Single-App Kiosk Mode & Arsitektur Dynamic Window Manager (Anti-Hardcoded)
- **Visi & Tujuan**:
  Membuka aplikasi individual Linux (Firefox, Wireshark, GIMP, Alacritty, dll.) secara instan tanpa perlu memuat seluruh Desktop Environment (XFCE/GNOME) yang berat. Jendela aplikasi langsung mengisi seluruh layar (Monocle/Maximized) di atas dock bawah, dengan akselerasi 3D VirGL dan audio PulseAudio aktif 100%.
- **Penanganan Perangkat: Root (BSPWM) vs Non-Root (Openbox)**:
  1. **Rooted Devices (Droidspaces / NetHunter - Poco M3 & Infinix)**:
     - Menggunakan **BSPWM Kiosk Mode**:
       * Background monitor subshell mengonfigurasi `bspc config single_monocle true` dan `bspc desktop -l monocle`.
       * Sembunyikan bar atas (`polybar-msg cmd hide`) dan pertahankan dock bawah (`polybar -q dock` dengan modul apps, terminal, files, windows, layout, fullscreen, close).
       * Padding dock bawah secara otomatis diatur oleh strut EWMH polybar (`wm-restack = bspwm`).
       * Ukuran kanvas X11 presisi 100% mengikuti rasio viewport Webview / right pane frame.
     - Audio: `PULSE_SERVER="unix:/tmp/.pulse-socket"` (Droidspaces) / `tcp:127.0.0.1:4713` (Chroot NetHunter).
     - GPU: VirGL virpipe 3D hardware acceleration (`/tmp/.virgl_test`).
  2. **Non-Root Devices (PRoot Distro / Native Termux X11)**:
     - Menggunakan **Openbox (Single-App Fullscreen)**:
       * Menggunakan aturan `openbox-rc.xml` bawaan launcher: `<application class="*"><decor>no</decor><maximized>yes</maximized><focus>yes</focus></application>`.
       * Jendela otomatis maximized memenuhi kanvas X11 tanpa titlebar memakan layar.
       * Audio diarahkan ke PulseAudio TCP host (`127.0.0.1:4713`).
- **Dynamic Window Manager & Engine Settings (Anti-Hardcoded)**:
  - Menu Settings `X11DisplayLinuxAppsPreferencesFragment` dan `GuiAppsSetupPreferencesFragment` kini terhubung secara dinamis dengan engine aktif:
    * Judul kategori otomatis menampilkan identitas engine aktif: `Desktop & Window Manager (Droidspaces: Ubuntu)`, `Desktop & Window Manager (Kali NetHunter)`, `Desktop & Window Manager (PRoot debian)`, atau `Desktop & Window Manager (Native Termux)`.
    * Preferensi `gui_apps_selected_de` mendeteksi WM/DE yang benar-benar terpasang di rootfs/host aktif (`ChrootDistro.detectInstalledDesktops`), menyajikan opsi seperti `BSPWM Kiosk (Single-App + Dock Bawah)`, `Openbox (Single-App Fullscreen)`, `BSPWM (Full Desktop)`, atau `XFCE 4`.
    * Perubahan preferensi langsung disimpan ke SharedPreferences dan men-trigger pembaruan `writeWindowManagerScript()` secara instan.

---

### O. Rekayasa Standalone Kiosk Mode 1-Tab Bersih & One-Click Cleanup
- **Konteks & Kebutuhan**:
  - Saat aplikasi Linux dibuka langsung dari App Drawer (mode standalone), muncul 2 tab di status bar atas:
    1) Tab Root Display (misal `[ Firefox ESR ]`).
    2) Tab Jendela X11 Client (misal `[ Firefox-esr ]`).
  - Hal ini membingungkan dan memaksa pengguna menutup 2 tab secara manual agar layar X11 bersih kembali ke Termux.
  - Selain itu, status Desktop Environment sering memunculkan teks ambigu *"Belum disetel"*, serta mode kiosk belum tersinkronisasi otomatis dengan DE pilihan pengguna.
- **Solusi & Arsitektur**:
  1. **Eliminasi Tab Ganda (`TermuxActivity.java: syncWindowBarItems`)**:
     - Pada mode standalone, jika jendela aplikasi sudah ter-mapping (`!mDisplayWindows.isEmpty()`), tab root display otomatis disembunyikan. Yang tampil di bar hanyalah 1 tab bersih milik jendela aplikasi tersebut.
     - Jika pengguna membuka lebih dari 1 jendela, setiap jendela mendapat 1 tab masing-masing tanpa ada tab root tambahan.
  2. **One-Click Close & Auto-Cleanup (`closeWindowFromStatusBar` & `onDisplayWindowsChanged`)**:
     - Ketika tab aplikasi ditutup (klik tombol silang `x`) dan itu adalah jendela terakhir, sistem otomatis memanggil `stopAndKillAllDisplayServices()`. Jendela DAN display server langsung mati seketika.
     - Begitu pula saat aplikasi keluar sendiri (exit/quit), `onDisplayWindowsChanged` mendeteksi daftar jendela kosong dan langsung mematikan display server otomatis tanpa meninggalkan layar hitam.
  3. **Pembersihan Menu & Penghapusan "Belum disetel"**:
     - `GuiAppsSetupPreferencesFragment.java` dan `ChrootDistro.java` memastikan dropdown DE selalu sinkron dengan biner DE yang terpasang, mengeliminasi status "Belum disetel".
     - Menghubungkan pilihan DE pengguna (`KEY_SELECTED_DE`) langsung ke generator skrip kiosk (`writeChrootKioskScript` & `writeDroidspacesKioskScript`).
  4. **Aturan Monocle Fullscreen BSPWM**:
     - Menambahkan aturan tiling penuh: `bspc rule -a * state=tiled`, `bspc config single_monocle true`, `bspc config gapless_monocle true`, `bspc config borderless_monocle true`, serta `_JAVA_AWT_WM_NONREPARENTING=1` dan `XDG_RUNTIME_DIR=/tmp/runtime-root`.
     - Jendela aplikasi otomatis memenuhi 100% canvas layar tanpa border atau celah.
  5. **Hasil Pengujian di Infinix Note 30 VIP**:
     - Dikonfirmasi oleh Tuan Eso: **"perfecto bro"** (Komit `497ce9d`).

---

### P. Optimalisasi Audio & Standalone Kiosk Khusus Droidspaces Container (Poco M3)
- **Konteks & Kebutuhan**:
  - Pada Poco M3 yang menggunakan container Droidspaces ("Ubuntu"), mode standalone dan suara sempat tidak muncul.
- **Akar Masalah**:
  1. **Audio (PulseAudio)**:
     - Droidspaces berjalan di dalam isolated network namespace (`eth0 172.28.28.28/16`), dengan default gateway menuju host Android pada IP `172.28.0.1`.
     - Di dalam `/tmp` Ubuntu terdapat file socket mati sisa sesi lama (`/tmp/.pulse-socket` link count 0). Skrip lama memeriksa `[ -S /tmp/.pulse-socket ]` dan mengarahkan audio ke socket mati tersebut (`Connection refused`).
     - Fallback ke `tcp:127.0.0.1:4713` juga gagal karena `127.0.0.1` di dalam Droidspaces adalah loopback container sendiri, bukan host Termux.
  2. **Standalone Kiosk Droidspaces**:
     - `writeDroidspacesKioskScript` belum memiliki aturan monocle tiling lengkap (`bspc rule -a * state=tiled`, `_JAVA_AWT_WM_NONREPARENTING=1`, `XDG_RUNTIME_DIR=/tmp/runtime-root`).
     - `resolveStandaloneKioskCommand` belum meneruskan preferensi `selectedDe` ke `writeDroidspacesKioskScript`.
     - Terdapat sisa file socket `/tmp/bspwm*` di dalam container yang menyebabkan bspwm menolak binding socket baru (*"The server closed the connection: socket, pipe or stream error"*).
- **Solusi & Rekayasa (`ChrootDistro.java`)**:
  1. **Audio Dynamic Host Routing**:
     - Di dalam skrip Droidspaces (`writeDroidspacesDesktopScript`, `writeDroidspacesKioskScript`, `writeDroidspacesAppScript`), IP host Android dideteksi dinamis via routing kernel:
       `HOST_IP=$(ip route 2>/dev/null | awk "/default/ {print \$3}" | head -n1)` (fallback `172.28.0.1`).
     - Set `export PULSE_SERVER="tcp:$HOST_IP:4713"`.
     - Bersihkan file stale socket: `rm -f /tmp/.pulse-socket 2>/dev/null || true`.
     - Sederhanakan `/etc/asound.conf` generic ALSA type pulse tanpa hardcode unix socket.
  2. **Penyempurnaan Kiosk Monocle & Stale Socket Cleaner**:
     - Meneruskan `selectedDe` ke `writeDroidspacesKioskScript(env.containerName, selectedDe)`.
     - Bersihkan stale bspwm socket: `rm -f /tmp/bspwm* 2>/dev/null || true`.
     - Terapkan aturan monocle lengkap: `bspc rule -a * state=tiled` dan environment runtime Java/XDG.
     - Tambahkan pembersihan socket bspwm dan pulse pada `killDesktopEnvironment()`.
  3. **Jaminan Isolasi Modular**:
     - Seluruh perbaikan Droidspaces terisolasi 100% di dalam blok `RunnerType.DROIDSPACES`.
     - Kali NetHunter Chroot (Infinix), PRoot Distro, dan Native Termux X11 tetap berjalan normal tanpa terpengaruh sedikit pun.
- **Status Komit**: `30ce53797`

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

### Q. Bundel Setup Mandiri (Eso Setup Bundle & Touch BSPWM Theme Suite)
- **Konteks & Kebutuhan**:
  - Tuan Eso menginginkan agar saat menjalankan `./setup` di perangkat baru yang di-root maupun non-root, seluruh ekosistem langsung siap pakai (*out-of-the-box*):
    1. Otomatis memasang `bspwm`, `sxhkd`, `rofi`, `polybar`, `picom`, `feh`, `pulseaudio`, dan `virgl` di Termux Native.
    2. Jika perangkat memiliki akses Root, otomatis memasang dependensi tersebut dan driver akselerasi 3D ke dalam rootfs container (NetHunter Chroot / Droidspaces Ubuntu). Jika non-root, terpasang ke Termux Native (dan PRoot jika dipilih).
    3. Memasukkan tema lengkap Touch BSPWM milik Tuan Eso (`~/.config/bspwm`, `polybar`, `rofi`, `picom`, `dunst`, `gtk-3.0`) dan koleksi wallpaper (`~/Pictures/wallpapers/`) ke dalam arsip payload.
    4. Mengintegrasikan `termux.properties` aktif milik Tuan Eso dari `$HOME/.termux/` ke dalam bundle.
    5. Membersihkan toggle `MOUSE_ON`/`MOUSE_OFF` dan `KEYBOARD_ENABLE`/`KEYBOARD_HIDE` dari `login.template` agar keyboard dan mouse tidak terkunci/berkedip saat menu startup login muncul.
- **Solusi & Arsitektur**:
  1. **Pembuatan Payload Bundle (`create_fusion_bundle.py`)**:
     - Membaca dan mengemas seluruh direktori tema Touch BSPWM (`fusion-setup/theme/`) ke dalam `fusion_payload.tar.gz` (~14 MB).
     - Menyinkronkan seluruh aset payload dan script ke direktori Android assets (`app/src/main/assets/fusion/`).
  2. **Penyempurnaan `setup.sh` (Multi-Destinasi)**:
     - Tahap 4: Memasang `x11-repo`, `bspwm`, `sxhkd`, `rofi`, `polybar`, `picom`, `feh`, `pulseaudio`, `virglrenderer-android`, mengekstrak tema ke `$HOME/.config/` & `$HOME/Pictures/wallpapers/`, serta membuat script wrapper `termux-bspwm`.
     - Tahap 5: Jika Root, menginjeksi paket GUI/Audio ke rootfs container (`$CONTAINER_ROOTFS`), menyalin konfigurasi tema ke `/root/.config/` dan `/root/Pictures/wallpapers/`, serta mengatur symlink wallpaper relatif secara otomatis.
     - Tahap 8: Menyediakan alias `bspwm="termux-bspwm"` pada shell `.zshrc` dan `.bashrc`.
  3. **Pembersihan `login.template`**:
     - Menghapus pemanggilan IPC `MOUSE_ON`, `MOUSE_OFF`, `KEYBOARD_ENABLE`, dan `KEYBOARD_HIDE` sehingga input pengguna tetap mulus dan normal saat memilih menu Linux/Termux.

---

### R. Antigravity IDE (GUI Desktop) Native ARM64 di NetHunter & Standalone Kiosk
- **Konteks & Kebutuhan**:
  - Tuan Eso ingin menjalankan Antigravity IDE versi GUI resmi (bukan versi CLI `agy`) langsung di dalam Kali NetHunter (Infinix Note 30 VIP) secara native tanpa lewat ADB.
  - Aplikasi harus terintegrasi dengan App Drawer Termux Launcher dan dapat diluncurkan dalam mode Standalone Kiosk (monocle fullscreen tiling).
- **Solusi & Arsitektur**:
  1. **Unduhan & Ekstraksi Biner Resmi**:
     - Mengunduh paket resmi Electron Linux ARM64: `https://storage.googleapis.com/antigravity-public/antigravity-hub/2.24.0-5780333019398144/linux-arm/Antigravity.tar.gz` (~181 MB).
     - Diekstrak ke `/opt/Antigravity/` di dalam rootfs Kali NetHunter (`/data/local/nhsystem/kali-arm64`).
  2. **Dependensi GUI & DBus**:
     - Memasang dependensi Electron via `apt-get`: `libgtk-3-0t64 libnotify4 libnss3 libxss1 libasound2t64 libsecret-1-0 libgbm1 libxkbcommon0 libx11-xcb1 libxcb-dri3-0 libdrm2 libxcomposite1 libxdamage1 libxrandr2 libcups2t64 xvfb`.
     - Memastikan layanan DBus `/etc/init.d/dbus start` aktif.
  3. **Wrapper Script (`/usr/local/bin/antigravity`)**:
     - Menjalankan Electron dengan flag kontainer & root:
       `exec /opt/Antigravity/antigravity --no-sandbox --disable-dev-shm-usage --disable-gpu-sandbox "$@"`
  4. **Ekstraksi Ikon & Desktop Entry**:
     - Ikon resmi 512x512 diekstrak dari header ASAR (`/opt/Antigravity/icon.png`) dan dipasang ke `/usr/share/pixmaps/antigravity.png` serta jembatan launcher `$PREFIX/share/applications/chroot/icons/antigravity.png`.
     - File `.desktop` dipasang di `/usr/share/applications/antigravity.desktop` dan disinkronkan ke `$PREFIX/share/applications/chroot/kali-arm64/antigravity.desktop`.
  5. **Verifikasi Headless & GUI**:
     - Berhasil diverifikasi menggunakan `xvfb-run`, Language Server dan Electron window aktif (`https://127.0.0.1:35939/`), siap dipakai pada Termux:X11 display `:0`.

### S. Enforce Character-based Input (Fix Gboard Backspace) & Flat Synchronized Touchpad
- **Konteks & Kebutuhan**:
  - Tombol Backspace/Hapus pada Gboard (Google Keyboard) tidak dapat menghapus teks di lingkungan Linux GUI (BSPWM/X11).
  - Tampilan touchpad pada Mouse Mode sebelumnya memiliki sudut membulat/lonjong (`panelRadius = 20dp`) serta inset samping (`inapp_keyboard_view_host` margin & padding), dan warnanya tidak serasi dengan barisan Extra Keys.
  - Tuan Eso meminta penambahan saklar pengaturan untuk Gboard Backspace Fix, serta touchpad yang solid datar rata tepi-ke-tepi (edge-to-edge, tanpa sudut melengkung) dengan warna latar yang selalu serasi dan dinamis mengikuti background `ExtraKeysView`.
- **Solusi & Arsitektur**:
  1. **Saklar Pengaturan X11 (`enforceCharBasedInput`) & Bedah Mikro Backspace**:
     - Menambahkan string sumber daya di `app/src/main/res/values/strings.xml` (`settings_x11_enforce_char_based_input_title` dan `_summary`).
     - Menambahkan `<SwitchPreferenceCompat app:key="enforceCharBasedInput" ... />` pada `app/src/main/res/xml/x11_display_input_preferences.xml` di bawah kategori Keyboard.
     - Terhubung langsung ke `X11DisplayPreferencesDataStore` dan preferensi inti X11 Server (`LorieHost.getPrefs().enforceCharBasedInput`).
     - **Bedah Mikro (`LorieView.java`)**: `inputType` tetap dipertahankan `TYPE_CLASS_TEXT` agar pengetikan huruf/angka di BSPWM dan semua window manager tetap lancar 100%. Saat saklar ON, sistem membypass filter spasi palsu di `deleteSurroundingText`, sehingga ketukan tombol Backspace pada Gboard langsung mengirim `sendKey(KeyEvent.KEYCODE_DEL)` ke X11 dan menghapus teks secara responsif.
  2. **Styling Touchpad Solid Flat Edge-to-Edge (`DisplayTouchpadView.java`)**:
     - Mengubah `panelRadius()` menjadi `0f` saat di luar split gap untuk menghasilkan sudut siku persegi penuh ("gausah lonjong2").
     - Menghapus inset batas panel di `layOutPanel()` (`mBounds.set(0f, 0f, w, h)`).
     - Menambahkan method `setSurfaceColor(int surfaceColor)` tanpa memaksakan alpha 255 sehingga dapat transparan murni atau semi-transparan mengikuti background Extra Keys.
  3. **Penyesuaian Host Margin & Sinkronisasi Warna Dinamis (`TermuxActivity.java`)**:
     - Pada `dressInAppKeyboardHost()`: Mengatur `horizontalMargin`, `topMargin`, `innerPadding`, dan `bottomMargin` menjadi `0` saat `mouseMode` aktif, sehingga touchpad menutupi seluruh lebar layar dari ujung ke ujung.
     - Pada `applyInAppKeyboardSurfaceClip()`: Menonaktifkan `setClipToOutline` saat `mouseMode` aktif agar tidak terpotong sudut kapsul.
     - Helper `resolveExtraKeysBackgroundColor()` mengambil nilai `mExtraKeysView.getButtonBackgroundColor()`. Jika Extra Keys bernilai `0` (transparan), maka touchpad ikut transparan murni (`Color.TRANSPARENT`), memperlihatkan kaca dock dan wallpaper secara konsisten.
     - Menyinkronkan warna touchpad di `syncDisplayTouchpad()`, `setExtraKeysView()`, dan `applyExtraKeysFeedbackAccent()`.

### T. Fix Background Task Leak on Display Stop & Complete Gboard Backspace Routing
- **Konteks & Kebutuhan**:
  - **Bug Task Menumpuk di Notifikasi (`task.jpg`)**: Setiap kali X11 Display dimulai lalu dimatikan, jumlah background task di notifikasi Termux bertambah (`1 session, 2 tasks (wake lock held)` ... hingga 10 tasks). Hal ini menyebabkan pemborosan daya baterai, thread leak, dan penahanan wakelock permanen.
  - **Gboard Backspace di BSPWM**: Penekanan Backspace pada Gboard masih belum menghapus teks di BSPWM karena Gboard mengirim event tombol mentah (`sendKeyEvent(KEYCODE_DEL)`) yang diabaikan oleh `dispatchKeyEvent` di `LorieView`.
- **Solusi & Arsitektur**:
  1. **Solusi Anti-Deadlock Pipa Output (`AppShell.java`)**:
     - Proses daemon di dalam chroot (`dbus-daemon`, `gvfsd`, `at-spi`) mewarisi pipa stdout/stderr dari proses `termux-x11`. Saat display dimatikan, daemon tersebut menahan ujung pipa terbuka sehingga thread pembaca (`StreamGobbler`) terkena *infinite block* di `pipe_read`.
     - Ditambahkan penutupan paksa stream: `mProcess.getInputStream().close()` dan `getErrorStream().close()` setelah `mProcess.waitFor()`.
     - `STDOUT.join()` dan `STDERR.join()` kini dibatasi dengan batas aman timeout (`500ms`) dan interrupt. `AppShell` dijamin selalu menyelesaikan eksekusi dan memanggil `onAppShellExited()`, menghapus task dari `mTermuxTasks`.
  2. **Pembersihan Bersih Daemon Chroot (`ChrootDistro.java`)**:
     - `DE_PROCESS_PATTERN` diperluas dengan menambahkan `dbus-daemon`, `gvfsd`, dan `at-spi`, sehingga daemon sesi chroot/container dibasmi tuntas saat display ditutup.
  3. **Pembersihan Task Display Server (`TermuxService.java` & `TermuxActivity.java`)**:
     - Ditambahkan method `killTermuxTasksForShellName(String name)` di `TermuxService`.
     - Pada `killEmbeddedDisplayServer()` di `TermuxActivity`, sistem otomatis membasmi task `termux-x11` yang sedang aktif di TermuxService dan menggunakan pola `pkill -f termux-x11`.
  4. **Penyaluran Lengkap Backspace Gboard (`LorieView.java`)**:
     - Saat saklar `enforceCharBasedInput` aktif (ON):
       - `mConnection.sendKeyEvent(event)` mendeteksi `KEYCODE_DEL` dan langsung menginjeksikannya ke X11 engine via `LorieView.this.sendKeyEvent(0, KEYCODE_DEL, down)`. Event kunci lain dialihkan ke `activity.handleKey(event)`.
       - `dispatchKeyEvent(event)` menginjeksi `KEYCODE_DEL` ke X11 dan mengalihkan event ke `activity.handleKey(event)` alih-alih dibuang oleh `super.dispatchKeyEvent`.
     - Saat saklar nonaktif (OFF):
       - Perilaku kembali 100% ke standar upstream Termux-X11.

---

## 4. Prosedur Build CI & Deploy ke Perangkat

### GitHub Actions CI
- Push ke branch `main` atau `eso-launcher` otomatis men-trigger workflow `.github/workflows/debug_build.yml` ("Build nightly").
- Token akses GitHub: Menggunakan token GitHub pengguna yang tersimpan di remote origin / environment secret.

### Script Otomatisasi Deploy Multi-Device
Script monitoring dan deployment otomatis tersedia di `/project/monitor_build.py` yang secara otomatis memantau workflow CI GitHub Actions, mengunduh artifact APK `arm64-v8a`, dan memasangnya ke seluruh target perangkat aktif:
- **Infinix**: `127.0.0.1:5555`
- **Poco M3**: `10.92.4.57:41383`

Langkah manual jika script perlu dijalankan ulang:
1. Pantau status run via GitHub API / CLI:
   ```bash
   curl -s -H "Authorization: token $TOKEN" "https://api.github.com/repos/eryshaqilla-wq/termux-launcher/actions/runs?per_page=1"
   ```
2. Unduh artifact zip `termux-app_v*arm64-v8a.apk`.
3. Pasang ke HP via ADB:
   ```bash
   adb -s 127.0.0.1:5555 install -r <nama-file-apk>.apk
   adb -s 10.92.4.57:41383 install -r <nama-file-apk>.apk
   adb -s 127.0.0.1:5555 shell am force-stop com.termux
   adb -s 127.0.0.1:5555 shell am start -n com.termux/com.termux.app.TermuxActivity
   ```
4. Verifikasi piksel bawah & Touchpad:
   - Pastikan baris `y = 2390..2399` konsisten tertutup latar gelap kaca dock dan tidak tembus warna wallpaper terang.
   - Aktifkan Mouse Mode dan pastikan touchpad berukuran penuh (~240dp) dengan Extra Keys dan Dock berada di atasnya persis seperti `touch.png`.

---

## 5. Hal Penting yang Tidak Boleh Diubah
1. **Jangan aktifkan animasi tilt/wobble per frame pada touch event**: Tuan Eso menyukai terminal yang responsif dan bebas lag.
2. **Jangan hapus penanganan overscan di `applyAccessoryLayerBounds`**: Jika `targetBottomMargin` dikembalikan ke `0`, celah wallpaper 8 piksel di dasar layar akan muncul kembali.
3. **Posisikan elemen visual tetap di dalam `terminal_root_container` / `accessory_stack_container`**: Jangan mengubah tata letak tombol keyboard / extra keys ke luar view hierarchy yang sudah rapi.
4. **Pertahankan `isTouchpadShown()` di dalam `isInAppKeyboardShown()`**: Ini adalah kunci agar `accessory_stack_container` mengembang dan tidak menimpa Extra Keys serta Dock saat mouse mode aktif.
5. **Pertahankan fallback 240dp di `KeyboardGeometryChoreographer`**: Mencegah layout feedback loop pada touchpad saat mode mouse dibuka tanpa in-app keyboard terpasang.
6. **Pertahankan jembatan Chroot `$PREFIX/share/applications/chroot`**: Direktori ini menghubungkan katalog `.desktop` NetHunter/Droidspaces dengan sistem Android launcher.
