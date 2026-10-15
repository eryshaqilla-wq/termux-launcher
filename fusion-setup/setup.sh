#!/data/data/com.termux/files/usr/bin/bash
# ==============================================================================
# 🚀 PROJECT FUSION & AGY UNIVERSAL SETUP (ROOT / NON-ROOT DUAL-PATH)
# ==============================================================================

set -e

# Warna Terminal Cyberpunk / Pastel
CYAN='\033[38;2;139;233;253m'
MAGENTA='\033[38;2;255;121;198m'
YELLOW='\033[38;2;241;250;140m'
GREEN='\033[38;2;80;250;123m'
RED='\033[38;2;255;85;85m'
RESET='\033[0m'
BOLD='\033[1m'
GRAY='\033[38;2;98;114;164m'

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
HOME="${HOME:-/data/data/com.termux/files/home}"
FUSION_HOME="$HOME/.fusion"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

clear
echo -e "${MAGENTA}${BOLD}"
cat << "EOF"
  ██████╗ ██████╗  ██████╗      ██╗███████╗ ██████╗████████╗    ███████╗██╗   ██╗███████╗██╗ ██████╗ ███╗   ██╗
  ██╔══██╗██╔══██╗██╔═══██╗     ██║██╔════╝██╔════╝╚══██╔══╝    ██╔════╝██║   ██║██╔════╝██║██╔═══██╗████╗  ██║
  ██████╔╝██████╔╝██║   ██║     ██║█████╗  ██║        ██║       █████╗  ██║   ██║███████╗██║██║   ██║██╔██╗ ██║
  ██╔═══╝ ██╔══██╗██║   ██║██   ██║██╔══╝  ██║        ██║       ██╔══╝  ██║   ██║╚════██║██║██║   ██║██║╚██╗██║
  ██║     ██║  ██║╚██████╔╝╚█████╔╝███████╗╚██████╗   ██║       ██║     ╚██████╔╝███████║██║╚██████╔╝██║ ╚████║
  ╚═╝     ╚═╝  ╚═╝ ╚═════╝  ╚════╝ ╚══════╝ ╚═════╝   ╚═╝       ╚═╝      ╚═════╝ ╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝
EOF
echo -e "${RESET}"
echo -e "         ${CYAN}${BOLD}Universal Setup: Dual-Path Engine (Root & Non-Root Edition)${RESET}\n"

# Cari payload tarball
PAYLOAD_TAR=""
if [ -f "$SCRIPT_DIR/fusion_payload.tar.gz" ]; then
    PAYLOAD_TAR="$SCRIPT_DIR/fusion_payload.tar.gz"
elif [ -f "$HOME/fusion_payload.tar.gz" ]; then
    PAYLOAD_TAR="$HOME/fusion_payload.tar.gz"
elif [ -f "$HOME/.fusion/fusion_payload.tar.gz" ]; then
    PAYLOAD_TAR="$HOME/.fusion/fusion_payload.tar.gz"
else
    BASE_APK=$(pm path com.termux 2>/dev/null | sed 's/package://' | head -n1 || echo "")
    if [ -n "$BASE_APK" ] && [ -f "$BASE_APK" ]; then
        if command -v unzip >/dev/null 2>&1; then
            unzip -p "$BASE_APK" assets/fusion/fusion_payload.tar.gz > "$HOME/fusion_payload.tar.gz" 2>/dev/null || true
            [ -s "$HOME/fusion_payload.tar.gz" ] && PAYLOAD_TAR="$HOME/fusion_payload.tar.gz"
        fi
    fi
fi

# ==============================================================================
# TAHAP 1: PEMILIHAN JALUR TAKDIR SISTEM (ROOT vs NON-ROOT)
# ==============================================================================
DETECTED_ROOT=0
if command -v su >/dev/null 2>&1; then
    if su -c "id" >/dev/null 2>&1; then
        DETECTED_ROOT=1
    fi
fi

echo -e "${MAGENTA}┌── [ TAHAP 1: PILIH JALUR TAKDIR SISTEM ] ─────────────────────────┐${RESET}"
echo -e "${CYAN}│${RESET} Tentukan arsitektur lingkungan untuk smartphone ini:"
echo -e "${CYAN}│${RESET}"
if [ $DETECTED_ROOT -eq 1 ]; then
    echo -e "${CYAN}│${RESET}  ${GREEN}✓ Sistem mendeteksi hak akses ROOT aktif di perangkat ini.${RESET}"
else
    echo -e "${CYAN}│${RESET}  ${YELLOW}ℹ Sistem mendeteksi perangkat STANDAR / NON-ROOT (Poco M3 / dll).${RESET}"
fi
echo -e "${CYAN}│${RESET}"
echo -e "${CYAN}│${RESET}  ${BOLD}[1] 👑  MODE ROOT${RESET}      (NetHunter / Droidspaces / Magisk Chroot)"
echo -e "${CYAN}│${RESET}  ${BOLD}[2] 🛡️  MODE NON-ROOT${RESET}  (Poco M3 / Android Standar / Pure Userland)"
echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}"

DEFAULT_MODE=$([ $DETECTED_ROOT -eq 1 ] && echo "1" || echo "2")
read -p "  Pilih Jalur Takdir [1/2, default: $DEFAULT_MODE]: " USER_MODE
[ -z "$USER_MODE" ] && USER_MODE="$DEFAULT_MODE"

if [ "$USER_MODE" = "1" ]; then
    IS_ROOT=1
    echo -e "  ${GREEN}➔ Mengaktifkan Jalur Takdir: ROOT ENGINE (Container & Kernel Access)${RESET}\n"
else
    IS_ROOT=0
    echo -e "  ${GREEN}➔ Mengaktifkan Jalur Takdir: NON-ROOT ENGINE (Pure Sandbox & PRoot)${RESET}\n"
fi

mkdir -p "$FUSION_HOME"
mkdir -p "$PREFIX/bin"

# ==============================================================================
# TAHAP 2: OPSI INTEGRASI DISTRO LINUX (CONTAINER / PROOT / TANPA LINUX)
# ==============================================================================
echo -e "${MAGENTA}┌── [ TAHAP 2: INTEGRASI DISTRO LINUX ] ────────────────────────────┐${RESET}"
echo -e "${CYAN}│${RESET} Apakah Tuan ingin memasang / mengintegrasikan Linux ke Termux ini?"
echo -e "${CYAN}│${RESET}"
echo -e "${CYAN}│${RESET}  ${BOLD}[1] 🐧  YA, Integrasikan Distro Linux${RESET}"
echo -e "${CYAN}│${RESET}  ${BOLD}[2] ⚡  TIDAK, Gunakan Termux Murni Saja (Super Cepat & Ringan)${RESET}"
echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}"

read -p "  Pilihan Integrasi Linux [1/2, default: 1]: " LINUX_OPT
[ -z "$LINUX_OPT" ] && LINUX_OPT="1"

DISTRO_NAME=""
DISPLAY_NAME=""
CUSTOM_LINUX_NAME=""
CUSTOM_LINUX_CMD=""

if [ "$LINUX_OPT" = "1" ]; then
    echo "1" > "$FUSION_HOME/has_linux.txt"

    if [ $IS_ROOT -eq 1 ]; then
        # Menu Linux di HP ROOT
        PREV_NAME=""
        PREV_CMD=""
        [ -f "$FUSION_HOME/linux_name.txt" ] && PREV_NAME=$(cat "$FUSION_HOME/linux_name.txt" 2>/dev/null | head -n1)
        [ -f "$FUSION_HOME/linux_entry.sh" ] && PREV_CMD=$(grep -E "^exec " "$FUSION_HOME/linux_entry.sh" 2>/dev/null | sed 's/^exec //')

        echo -e "\n  ${YELLOW}Pilih Jenis Linux Container di HP Root:${RESET}"
        echo -e "    1) Droidspaces (Debian / Ubuntu / dll)"
        echo -e "    2) Kali NetHunter (bashnh / chroot)"
        echo -e "    3) Custom Command / Path Lain"
        if [ -n "$PREV_NAME" ]; then
            echo -e "    ${GRAY}(Konfigurasi aktif: $PREV_NAME -> $PREV_CMD)${RESET}"
        fi
        read -p "    Pilihan [1/2/3, default: 1]: " ROOT_LINUX_CHOICE
        [ -z "$ROOT_LINUX_CHOICE" ] && ROOT_LINUX_CHOICE="1"

        USER_ROOTFS_INPUT=""
        if [ "$ROOT_LINUX_CHOICE" = "1" ]; then
            DEF_DS_NAME="${PREV_NAME:-Debian}"
            [[ "$DEF_DS_NAME" =~ ^(NetHunter|Kali)$ ]] && DEF_DS_NAME="Debian"
            read -p "    Nama Distro Droidspaces [default: $DEF_DS_NAME]: " DS_NAME_INPUT
            [ -z "$DS_NAME_INPUT" ] && DS_NAME_INPUT="$DEF_DS_NAME"
            CUSTOM_LINUX_NAME="$DS_NAME_INPUT"
            CUSTOM_LINUX_CMD="su -c '/data/local/Droidspaces/bin/droidspaces --name=\"$DS_NAME_INPUT\" enter'"
        elif [ "$ROOT_LINUX_CHOICE" = "2" ]; then
            CUSTOM_LINUX_NAME="NetHunter"
            CUSTOM_LINUX_CMD="bashnh"
        else
            read -p "    Nama Tampilan Linux [default: ${PREV_NAME:-Linux}]: " CUSTOM_LINUX_NAME
            [ -z "$CUSTOM_LINUX_NAME" ] && CUSTOM_LINUX_NAME="${PREV_NAME:-Linux}"
            read -p "    Perintah Eksekusi [default: ${PREV_CMD:-su}]: " CUSTOM_LINUX_CMD
            [ -z "$CUSTOM_LINUX_CMD" ] && CUSTOM_LINUX_CMD="${PREV_CMD:-su}"
            read -p "    Folder rootfs Linux jika ada (Enter untuk auto-detect): " USER_ROOTFS_INPUT
        fi

        # Deteksi PREFIX Linux Rootfs
        CONTAINER_ROOTFS=""
        DS_CONTAINER_NAME=""
        if [[ "$CUSTOM_LINUX_CMD" =~ --name=\"?([^\"]+)\"? ]]; then
            DS_CONTAINER_NAME="${BASH_REMATCH[1]}"
        elif [ -n "$CUSTOM_LINUX_NAME" ] && [ "$CUSTOM_LINUX_NAME" != "NetHunter" ] && [ "$CUSTOM_LINUX_NAME" != "Kali" ]; then
            DS_CONTAINER_NAME="$CUSTOM_LINUX_NAME"
        fi

        if [ -n "$DS_CONTAINER_NAME" ]; then
            # Format resmi Droidspaces: /data/local/Droidspaces/Containers/<NAME>/rootfs
            if su -c "[ -d '/data/local/Droidspaces/Containers/$DS_CONTAINER_NAME/rootfs' ]" 2>/dev/null; then
                CONTAINER_ROOTFS="/data/local/Droidspaces/Containers/$DS_CONTAINER_NAME/rootfs"
            elif su -c "[ -d '/data/local/Droidspaces/spaces/$DS_CONTAINER_NAME/rootfs' ]" 2>/dev/null; then
                CONTAINER_ROOTFS="/data/local/Droidspaces/spaces/$DS_CONTAINER_NAME/rootfs"
            elif su -c "[ -d '/data/local/Droidspaces/Containers/$DS_CONTAINER_NAME' ]" 2>/dev/null; then
                CONTAINER_ROOTFS="/data/local/Droidspaces/Containers/$DS_CONTAINER_NAME"
            elif su -c "[ -d '/data/local/Droidspaces/spaces/$DS_CONTAINER_NAME' ]" 2>/dev/null; then
                CONTAINER_ROOTFS="/data/local/Droidspaces/spaces/$DS_CONTAINER_NAME"
            fi
        fi

        if [ -z "$CONTAINER_ROOTFS" ]; then
            if [[ "$CUSTOM_LINUX_CMD" =~ kali-arm64 ]] || [ "$CUSTOM_LINUX_NAME" = "NetHunter" ] || [ "$CUSTOM_LINUX_NAME" = "Kali" ]; then
                if su -c "[ -d '/data/local/nhsystem/kali-arm64' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/nhsystem/kali-arm64"
                fi
            fi
        fi

        if [ -z "$CONTAINER_ROOTFS" ] && [ -n "$USER_ROOTFS_INPUT" ]; then
            if su -c "[ -d '$USER_ROOTFS_INPUT' ]" 2>/dev/null; then
                CONTAINER_ROOTFS="$USER_ROOTFS_INPUT"
            fi
        fi

        echo "$CUSTOM_LINUX_NAME" > "$FUSION_HOME/linux_name.txt"
        [ -n "$CONTAINER_ROOTFS" ] && echo "$CONTAINER_ROOTFS" > "$FUSION_HOME/linux_rootfs.txt"

        cat << EOF > "$FUSION_HOME/linux_entry.sh"
#!/data/data/com.termux/files/usr/bin/bash
unset LD_PRELOAD
unset LD_LIBRARY_PATH
exec $CUSTOM_LINUX_CMD
EOF
        chmod +x "$FUSION_HOME/linux_entry.sh"
        echo -e "  ${GREEN}✓ Konfigurasi Container Root ($CUSTOM_LINUX_NAME) tersimpan!${RESET}"
        if [ -n "$CONTAINER_ROOTFS" ]; then
            echo -e "  ${GREEN}✓ Prefix rootfs terdeteksi: ${YELLOW}$CONTAINER_ROOTFS${RESET}\n"
        else
            echo -e "  ${YELLOW}ℹ Prefix rootfs akan di-resolve saat container berjalan.${RESET}\n"
        fi

    else
        # Menu Linux di HP NON-ROOT (PRoot)
        echo -e "\n  ${YELLOW}Pilih Distro PRoot untuk HP Non-Root (Tanpa su/root):${RESET}"
        echo -e "    1) Debian (Direkomendasikan - Stabil & Ringan)"
        echo -e "    2) Ubuntu"
        echo -e "    3) Kali Linux"
        read -p "    Pilihan [1/2/3, default: 1]: " NONROOT_DISTRO_CHOICE
        [ -z "$NONROOT_DISTRO_CHOICE" ] && NONROOT_DISTRO_CHOICE="1"

        case "$NONROOT_DISTRO_CHOICE" in
            2) DISTRO_NAME="ubuntu"; DISPLAY_NAME="Ubuntu" ;;
            3) DISTRO_NAME="kali"; DISPLAY_NAME="Kali" ;;
            *) DISTRO_NAME="debian"; DISPLAY_NAME="Debian" ;;
        esac

        echo "$DISPLAY_NAME" > "$FUSION_HOME/linux_name.txt"
        echo "$DISTRO_NAME" > "$FUSION_HOME/distro_slug.txt"

        echo -e "  ${CYAN}Memeriksa dependensi proot-distro...${RESET}"
        if ! command -v proot-distro >/dev/null 2>&1; then
            echo -e "  ${YELLOW}Mengunduh paket proot-distro via pkg...${RESET}"
            pkg update -y && pkg install -y proot-distro
        fi

        if ! proot-distro list 2>/dev/null | grep -q "installed.*$DISTRO_NAME"; then
            echo -e "  ${YELLOW}Mengunduh & memasang rootfs $DISPLAY_NAME...${RESET}"
            proot-distro install "$DISTRO_NAME"
        fi

        cat << EOF > "$FUSION_HOME/linux_entry.sh"
#!/data/data/com.termux/files/usr/bin/bash
exec proot-distro login $DISTRO_NAME
EOF
        chmod +x "$FUSION_HOME/linux_entry.sh"
        echo -e "  ${GREEN}✓ Distro PRoot ($DISPLAY_NAME) siap digunakan!${RESET}\n"
    fi

else
    # Tanpa Linux (Murni Termux)
    echo "0" > "$FUSION_HOME/has_linux.txt"
    echo -e "  ${GREEN}✓ Mode Murni Termux diaktifkan (Startup langsung masuk shell tanpa menu).${RESET}\n"
fi

# ==============================================================================
# TAHAP 3: EKSTRAKSI TOOLS & NATIVE BINARY KE TERMUX HOST
# ==============================================================================
echo -e "${MAGENTA}┌── [ TAHAP 3: INSTALASI TOOLS & IPC ENGINE DI HOST TERMUX ] ───────┐${RESET}"
# Cek apakah paket sistem dasar sudah lengkap
SYSTEM_PKGS_OK=1
for p in python node git curl; do
    if ! command -v "$p" >/dev/null 2>&1; then
        SYSTEM_PKGS_OK=0
        break
    fi
done

if [ $SYSTEM_PKGS_OK -eq 1 ]; then
    echo -e "${CYAN}│${RESET} ${GREEN}✓ Dependensi sistem (Python, Node.js, Git, Curl) sudah terinstall!${RESET}"
    echo -e "${CYAN}│${RESET} ${GREEN}➔ Mode Cepat (Re-run): Melewati pkg install, fokus memperbaiki Fusion & IPC...${RESET}"
else
    echo -e "${CYAN}│${RESET} ${YELLOW}Menginstall dependensi sistem via pkg (nodejs, python, git, curl)...${RESET}"
    pkg update -y && pkg install -y nodejs-lts python git curl
fi

if [ -n "$PAYLOAD_TAR" ] && [ -f "$PAYLOAD_TAR" ]; then
    echo -e "${CYAN}│${RESET} Ekstraksi aset dari payload tarball..."
    tar -xzf "$PAYLOAD_TAR" -C "$FUSION_HOME"
else
    echo -e "${CYAN}│${RESET} Menggunakan file lokal..."
fi

# Pasang binary ke $PREFIX/bin dengan shebang Termux yang valid
if [ -d "$FUSION_HOME/bin" ]; then
    cp -rf "$FUSION_HOME/bin/"* "$PREFIX/bin/"
    chmod +x "$PREFIX/bin/"* 2>/dev/null || true
fi

# Hapus file glibc lama yang memicu Bad system call
rm -f "$PREFIX/bin/termux-ipc-send" "/data/data/com.termux/files/usr/bin/termux-ipc-send" "$FUSION_HOME/bin/termux-ipc-send" 2>/dev/null || true

# Pastikan termux-ipc menggunakan shebang Termux yang benar
if [ -f "$PREFIX/bin/termux-ipc" ]; then
    sed -i '1s|.*|#!/data/data/com.termux/files/usr/bin/bash|' "$PREFIX/bin/termux-ipc" 2>/dev/null || true
    chmod +x "$PREFIX/bin/termux-ipc"
fi

# Buat wrapper browser untuk Termux host
cat << 'EOF' > "$PREFIX/bin/browser"
#!/data/data/com.termux/files/usr/bin/bash
exec termux-ipc webview "$@"
EOF
chmod +x "$PREFIX/bin/browser"

# Buat wrapper wallet CLI untuk Termux host
cat << 'EOF_W' > "$PREFIX/bin/wallet"
#!/data/data/com.termux/files/usr/bin/bash
WALLET_DIR="${HOME}/wallet"
if [ -f "$WALLET_DIR/wallet.js" ]; then
    cd "$WALLET_DIR" && exec node wallet.js "$@"
elif [ -f "/root/wallet/wallet.js" ]; then
    cd "/root/wallet" && exec node wallet.js "$@"
else
    echo "GhostWallet tidak ditemukan di $WALLET_DIR atau /root/wallet"
    exit 1
fi
EOF_W
chmod +x "$PREFIX/bin/wallet"

# Pasang alias symlink copilot
if [ -f "$PREFIX/bin/agy-copilot" ]; then
    ln -sf "$PREFIX/bin/agy-copilot" "$PREFIX/bin/copilot"
fi

# Pasang script login dari template
if [ -f "$FUSION_HOME/login.template" ]; then
    cp -f "$FUSION_HOME/login.template" "$PREFIX/bin/login"
elif [ -f "$SCRIPT_DIR/../login.template" ]; then
    cp -f "$SCRIPT_DIR/../login.template" "$PREFIX/bin/login"
fi
[ -f "$PREFIX/bin/login" ] && chmod +x "$PREFIX/bin/login"

# Pasang / perbarui script setup mandiri di $PREFIX/bin/setup dan $HOME/setup
if [ -f "$FUSION_HOME/setup.sh" ]; then
    cp -f "$FUSION_HOME/setup.sh" "$PREFIX/bin/setup"
    cp -f "$FUSION_HOME/setup.sh" "$HOME/setup"
elif [ -f "$0" ]; then
    cp -f "$0" "$PREFIX/bin/setup"
    cp -f "$0" "$HOME/setup"
fi
chmod +x "$PREFIX/bin/setup" "$HOME/setup" 2>/dev/null || true

# Pasang konfigurasi termux.properties milik Tuan Eso
mkdir -p "$HOME/.termux"
if [ -f "$FUSION_HOME/termux.properties" ]; then
    cp -f "$FUSION_HOME/termux.properties" "$HOME/.termux/termux.properties"
elif [ -f "$SCRIPT_DIR/termux.properties" ]; then
    cp -f "$SCRIPT_DIR/termux.properties" "$HOME/.termux/termux.properties"
elif [ -f "/root/wallet/termux.properties" ]; then
    cp -f "/root/wallet/termux.properties" "$HOME/.termux/termux.properties"
elif [ -f "/project/clean_build/termux.properties" ]; then
    cp -f "/project/clean_build/termux.properties" "$HOME/.termux/termux.properties"
fi
if [ -f "$HOME/.termux/termux.properties" ]; then
    grep -q "fullscreen" "$HOME/.termux/termux.properties" 2>/dev/null || echo "fullscreen=false" >> "$HOME/.termux/termux.properties"
    grep -q "terminal-onclick-url-open" "$HOME/.termux/termux.properties" 2>/dev/null || echo "terminal-onclick-url-open = true" >> "$HOME/.termux/termux.properties"
    echo -e "${CYAN}│${RESET} ${GREEN}✓ termux.properties milik Tuan Eso berhasil dipasang di ~/.termux/termux.properties!${RESET}"
fi
termux-reload-settings >/dev/null 2>&1 || true

# Aktifkan sensor suhu baterai di status bar Termux (status_widget_weather)
PREF_FILE="/data/data/com.termux/shared_prefs/com.termux_preferences.xml"
if [ -f "$PREF_FILE" ]; then
    if grep -q "status_widget_weather" "$PREF_FILE"; then
        sed -i 's/name="status_widget_weather" value="false"/name="status_widget_weather" value="true"/' "$PREF_FILE" 2>/dev/null || true
    else
        sed -i 's|</map>|    <boolean name="status_widget_weather" value="true" />\n</map>|' "$PREF_FILE" 2>/dev/null || true
    fi
    am broadcast -a com.termux.app.reload_style >/dev/null 2>&1 || true
fi

echo -e "${CYAN}│${RESET} ${GREEN}✓ Tools IPC, wallet CLI, termux.properties & sensor baterai aktif di Termux!${RESET}"
echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}\n"

# ==============================================================================
# TAHAP 4: INTEGRASI LANGSUNG IPC & FUSION KE DALAM DISTRO LINUX
# ==============================================================================
if [ "$LINUX_OPT" = "1" ]; then
    echo -e "${MAGENTA}┌── [ TAHAP 4: INTEGRASI IPC & FUSION KE DALAM LINUX CONTAINER ] ──┐${RESET}"

    if [ $IS_ROOT -eq 0 ]; then
        # NON-ROOT (PRoot Distro Injection)
        CONTAINER_ROOTFS="$PREFIX/var/lib/proot-distro/installed-rootfs/$DISTRO_NAME"
        if [ -d "$CONTAINER_ROOTFS" ]; then
            echo -e "${CYAN}│${RESET} Menginjeksi binary IPC & Fusion ke dalam rootfs $DISPLAY_NAME..."
            mkdir -p "$CONTAINER_ROOTFS/usr/local/bin"

            # Salin termux-ipc-send (static native binary aarch64)
            if [ -f "$PREFIX/bin/termux-ipc-send" ]; then
                cp -f "$PREFIX/bin/termux-ipc-send" "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc-send"
                chmod +x "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc-send"
            fi

            # Salin termux-ipc dengan shebang Linux standard (/bin/bash)
            if [ -f "$PREFIX/bin/termux-ipc" ]; then
                (echo '#!/bin/bash'; tail -n +2 "$PREFIX/bin/termux-ipc") > "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
                chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
            fi

            # Buat browser command di dalam Linux container
            cat << 'EOF_B' > "$CONTAINER_ROOTFS/usr/local/bin/browser"
#!/bin/bash
exec termux-ipc webview "$@"
EOF_B
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/browser"

            # Buat wallet command di dalam Linux container
            cat << 'EOF_CW' > "$CONTAINER_ROOTFS/usr/local/bin/wallet"
#!/bin/bash
if [ -f "/root/wallet/wallet.js" ]; then
    cd /root/wallet && exec node wallet.js "$@"
elif [ -f "$HOME/wallet/wallet.js" ]; then
    cd "$HOME/wallet" && exec node wallet.js "$@"
else
    echo "GhostWallet tidak ditemukan di /root/wallet atau $HOME/wallet"
    exit 1
fi
EOF_CW
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/wallet"

            # Salin copilot jika ada
            if [ -f "$PREFIX/bin/agy-copilot" ]; then
                cp -f "$PREFIX/bin/agy-copilot" "$CONTAINER_ROOTFS/usr/local/bin/agy-copilot"
                chmod +x "$CONTAINER_ROOTFS/usr/local/bin/agy-copilot"
                ln -sf "$CONTAINER_ROOTFS/usr/local/bin/agy-copilot" "$CONTAINER_ROOTFS/usr/local/bin/copilot" 2>/dev/null || true
            fi

            # Injeksi alias ke .bashrc, .zshrc, dan etc/profile di dalam container
            CONTAINER_ALIASES='
# Project Fusion In-Container Aliases
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
alias wallet="wallet"
'
            for CFG in "$CONTAINER_ROOTFS/root/.bashrc" "$CONTAINER_ROOTFS/root/.zshrc" "$CONTAINER_ROOTFS/etc/bash.bashrc"; do
                if [ -f "$CFG" ] && ! grep -q "Project Fusion In-Container Aliases" "$CFG" 2>/dev/null; then
                    echo "$CONTAINER_ALIASES" >> "$CFG"
                elif [ ! -f "$CFG" ] && [[ "$CFG" == *".bashrc" ]]; then
                    echo "$CONTAINER_ALIASES" > "$CFG"
                fi
            done

            echo -e "${CYAN}│${RESET} ${GREEN}✓ Tools & Alias terinjeksi ke $DISPLAY_NAME (/usr/local/bin)!${RESET}"
        else
            echo -e "${CYAN}│${RESET} ${YELLOW}Rootfs $DISTRO_NAME belum terdeteksi di $CONTAINER_ROOTFS${RESET}"
        fi

    else
        # ROOT (Droidspaces / NetHunter / Chroot Injection)
        echo -e "${CYAN}│${RESET} Menyiapkan injeksi IPC & Tools ke Linux Container ($CUSTOM_LINUX_NAME)..."

        # 1. Siapkan universal in-container installer script di /sdcard/fusion-linux-setup.sh
        INSTALLER_SCRIPT="/sdcard/fusion-linux-setup.sh"
        cat << 'EOF_LINUX_INSTALL' > "$INSTALLER_SCRIPT"
#!/bin/bash
# Project Fusion - In-Container Setup Script for Linux / Droidspaces / NetHunter
mkdir -p /usr/local/bin

# 1. Pasang termux-ipc
cat << 'EOF_FUSION_IPC' > /usr/local/bin/termux-ipc
#!/bin/bash
# Project Fusion - Termux IPC In-Container Client
[ -x "$BASH" ] || BASH="$(command -v bash 2>/dev/null || echo /bin/bash)"
ACTION="$(echo "$1" | tr '[:upper:]' '[:lower:]' | tr '_' '-')"
shift

if [ -z "$ACTION" ]; then
    echo "Usage: termux-ipc <toast|vibrate|clip-get|clip-set|run|input|mouse-on|mouse-off|disable|enable|hide|show|api|x11-start|x11-stop|x11-launch|x11-audio|webview|ping> [args...]"
    exit 1
fi

send_cmd() {
    local payload="$1"
    local py_cmd=""
    if command -v python3 >/dev/null 2>&1; then
        py_cmd="python3"
    elif command -v python >/dev/null 2>&1; then
        py_cmd="python"
    fi

    if [ -n "$py_cmd" ]; then
        "$py_cmd" -c "
import socket, sys
try:
    s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    s.settimeout(2.0)
    s.connect('\0termux_ipc')
    s.sendall(sys.argv[1].encode() + b'\n')
    resp = s.recv(4096).decode(errors='ignore').strip()
    if resp:
        print(resp)
    s.close()
except Exception:
    pass
" "$payload"
    fi
}

case "$ACTION" in
    webview|browser)
        RAW_ARGS=""
        for arg in "$@"; do
            RAW_ARGS="$RAW_ARGS \"$arg\""
        done
        send_cmd "WEBVIEW $RAW_ARGS"
        ;;
    toast)
        send_cmd "TOAST $*"
        ;;
    vibrate)
        send_cmd "VIBRATE"
        ;;
    ping)
        send_cmd "PING"
        ;;
    *)
        send_cmd "$ACTION $*"
        ;;
esac
EOF_FUSION_IPC
chmod +x /usr/local/bin/termux-ipc

# 2. Pasang wrapper browser & wallet
cat << 'EOF_FUSION_BR' > /usr/local/bin/browser
#!/bin/bash
exec termux-ipc webview "$@"
EOF_FUSION_BR
chmod +x /usr/local/bin/browser

cat << 'EOF_FUSION_WL' > /usr/local/bin/wallet
#!/bin/bash
if [ -f "/root/wallet/wallet.js" ]; then
    cd /root/wallet && exec node wallet.js "$@"
elif [ -f "$HOME/wallet/wallet.js" ]; then
    cd "$HOME/wallet" && exec node wallet.js "$@"
else
    echo "GhostWallet tidak ditemukan di /root/wallet atau $HOME/wallet"
    exit 1
fi
EOF_FUSION_WL
chmod +x /usr/local/bin/wallet

# 3. Pasang alias di profile container
CONTAINER_ALIASES='
# Project Fusion In-Container Aliases
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
alias wallet="wallet"
'
for RC in /root/.bashrc /root/.zshrc /etc/bash.bashrc /etc/profile; do
    if [ -f "$RC" ] && ! grep -q "Project Fusion In-Container Aliases" "$RC" 2>/dev/null; then
        echo "$CONTAINER_ALIASES" >> "$RC"
    elif [ -f "$RC" ] && ! grep -q "alias wallet=" "$RC" 2>/dev/null; then
        echo 'alias wallet="wallet"' >> "$RC"
    fi
done
echo "✓ Project Fusion tools (termux-ipc, browser, wallet) berhasil dipasang di /usr/local/bin!"
EOF_LINUX_INSTALL
        chmod +x "$INSTALLER_SCRIPT" 2>/dev/null || true
        cp -f "$INSTALLER_SCRIPT" "$HOME/fusion-linux-setup.sh" 2>/dev/null || true

        # 2. Cek rootfs container (prioritas hasil resolve Tahap 2)
        if [ -z "$CONTAINER_ROOTFS" ] && [ -f "$FUSION_HOME/linux_rootfs.txt" ]; then
            CONTAINER_ROOTFS=$(cat "$FUSION_HOME/linux_rootfs.txt" 2>/dev/null | tr -d '[:space:]')
        fi
        if [ -z "$CONTAINER_ROOTFS" ]; then
            CANDIDATES=(
                "$USER_ROOTFS_INPUT"
                "/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME/rootfs"
                "/data/local/Droidspaces/spaces/$CUSTOM_LINUX_NAME/rootfs"
                "/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME"
                "/data/local/Droidspaces/spaces/$CUSTOM_LINUX_NAME"
                "/data/local/nhsystem/kali-arm64"
                "/data/local/nhsystem/$CUSTOM_LINUX_NAME"
            )
            for cand in "${CANDIDATES[@]}"; do
                [ -z "$cand" ] && continue
                if su -c "[ -d '$cand/bin' ] || [ -d '$cand/usr/bin' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="$cand"
                    break
                fi
            done
        fi

        INJECT_SUCCESS=0
        if [ -n "$CONTAINER_ROOTFS" ]; then
            echo -e "${CYAN}│${RESET} Rootfs container terdeteksi di: ${YELLOW}$CONTAINER_ROOTFS${RESET}"
            su -c "
                mkdir -p '$CONTAINER_ROOTFS/usr/local/bin'
                cp -f '$INSTALLER_SCRIPT' '$CONTAINER_ROOTFS/tmp/fusion_setup.sh'
                chroot '$CONTAINER_ROOTFS' /bin/bash /tmp/fusion_setup.sh 2>/dev/null || {
                    (echo '#!/bin/bash'; tail -n +2 '$PREFIX/bin/termux-ipc') > '$CONTAINER_ROOTFS/usr/local/bin/termux-ipc'
                    echo '#!/bin/bash' > '$CONTAINER_ROOTFS/usr/local/bin/browser'
                    echo 'exec termux-ipc webview \"\$@\"' >> '$CONTAINER_ROOTFS/usr/local/bin/browser'
                    chmod 755 '$CONTAINER_ROOTFS/usr/local/bin/termux-ipc' '$CONTAINER_ROOTFS/usr/local/bin/browser'
                }
            " 2>/dev/null && INJECT_SUCCESS=1
        fi

        # 3. Coba runner Droidspaces jika executable
        if [ $INJECT_SUCCESS -eq 0 ] && [ -x "/data/local/Droidspaces/bin/droidspaces" ]; then
            echo -e "${CYAN}│${RESET} Mencoba injeksi otomatis via Droidspaces CLI..."
            su -c "/data/local/Droidspaces/bin/droidspaces --name=\"$CUSTOM_LINUX_NAME\" run -- /bin/bash /sdcard/fusion-linux-setup.sh" >/dev/null 2>&1 && INJECT_SUCCESS=1 || true
        fi

        if [ $INJECT_SUCCESS -eq 1 ]; then
            echo -e "${CYAN}│${RESET} ${GREEN}✓ Tools IPC & Browser terinjeksi ke container $CUSTOM_LINUX_NAME (/usr/local/bin)!${RESET}"
        else
            echo -e "${CYAN}│${RESET} ${YELLOW}ℹ Script installer container disiapkan di: /sdcard/fusion-linux-setup.sh${RESET}"
            echo -e "${CYAN}│${RESET} ${YELLOW}  Saat masuk ke $CUSTOM_LINUX_NAME, jalankan: bash /sdcard/fusion-linux-setup.sh${RESET}"
        fi
    fi

    echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}\n"
fi

# ==============================================================================
# TAHAP 5: LIVE TESTING & INTEGRASI DUA ARAH (IPC & ANDROID RUNTIME)
# ==============================================================================
echo -e "${MAGENTA}┌── [ TAHAP 5: LIVE VERIFIKASI IPC & RUNTIME ANDROID ] ─────────────┐${RESET}"
echo -e "${CYAN}│${RESET} Menguji komunikasi Abstract Domain Socket (@termux_ipc)..."

# 1. Tes dari Host Termux
HOST_TEST_RESP=""
if command -v termux-ipc >/dev/null 2>&1; then
    HOST_TEST_RESP=$(termux-ipc ping 2>/dev/null || true)
fi

if echo "$HOST_TEST_RESP" | grep -qi "PONG"; then
    echo -e "${CYAN}│${RESET}  ${GREEN}✓ Host Termux IPC        : TERHUBUNG KE RUNTIME ANDROID (PONG)${RESET}"
    # Kirim umpan balik visual & haptik ke layar Android
    termux-ipc toast "🔥 Project Fusion: Host Termux Terhubung!" 2>/dev/null || true
    termux-ipc vibrate 2>/dev/null || true
else
    echo -e "${CYAN}│${RESET}  ${YELLOW}ℹ Host Termux IPC        : Socket standby (Aktif otomatis saat GUI dibuka)${RESET}"
fi

# 2. Tes dari Dalam Linux Container (jika diintegrasikan)
if [ "$LINUX_OPT" = "1" ]; then
    if [ $IS_ROOT -eq 0 ] && [ -n "$DISTRO_NAME" ]; then
        CONTAINER_TEST_RESP=$(proot-distro login "$DISTRO_NAME" -- termux-ipc ping 2>/dev/null || true)
        if echo "$CONTAINER_TEST_RESP" | grep -qi "PONG"; then
            echo -e "${CYAN}│${RESET}  ${GREEN}✓ In-Container ($DISPLAY_NAME) IPC: TERHUBUNG KE RUNTIME ANDROID (PONG)${RESET}"
            proot-distro login "$DISTRO_NAME" -- termux-ipc toast "🐧 Distro $DISPLAY_NAME: IPC Berhasil Terintegrasi!" 2>/dev/null || true
        else
            echo -e "${CYAN}│${RESET}  ${YELLOW}ℹ In-Container ($DISPLAY_NAME) IPC: Siap di /usr/local/bin (Standby)${RESET}"
        fi
    elif [ $IS_ROOT -eq 1 ]; then
        ROOT_TEST_RESP=""
        if [ -n "$CONTAINER_ROOTFS" ]; then
            ROOT_TEST_RESP=$(chroot "$CONTAINER_ROOTFS" /usr/local/bin/termux-ipc ping 2>/dev/null || true)
        elif [ -x "/data/local/Droidspaces/bin/droidspaces" ]; then
            ROOT_TEST_RESP=$(su -c "/data/local/Droidspaces/bin/droidspaces --name=\"$CUSTOM_LINUX_NAME\" run -- /usr/local/bin/termux-ipc ping" 2>/dev/null || true)
        fi
        if echo "$ROOT_TEST_RESP" | grep -qi "PONG"; then
            echo -e "${CYAN}│${RESET}  ${GREEN}✓ In-Container Root IPC  : TERHUBUNG KE RUNTIME ANDROID (PONG)${RESET}"
        else
            echo -e "${CYAN}│${RESET}  ${YELLOW}ℹ In-Container Root IPC  : Siap di /usr/local/bin (Standby)${RESET}"
        fi
    fi
fi
echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}\n"

# ==============================================================================
# TAHAP 6: SETUP GHOST WALLET & DEPENDENCIES
# ==============================================================================
echo -e "${MAGENTA}┌── [ TAHAP 6: GHOST WALLET & WEB3 ENGINE ] ────────────────────────┐${RESET}"
if [ $IS_ROOT -eq 1 ]; then
    echo -e "${CYAN}│${RESET} Pilih lokasi Ghost Wallet:"
    echo -e "${CYAN}│${RESET}   1) $HOME/wallet (Termux Lokal - Direkomendasikan)"
    echo -e "${CYAN}│${RESET}   2) Linux Container ($CUSTOM_LINUX_NAME /root/wallet)"
    read -p "  Pilihan [1/2, default: 1]: " W_CHOICE
    [ -z "$W_CHOICE" ] && W_CHOICE="1"
    if [ "$W_CHOICE" = "2" ] && [ -n "$CONTAINER_ROOTFS" ]; then
        TARGET_WALLET_DIR="$CONTAINER_ROOTFS/root/wallet"
    else
        TARGET_WALLET_DIR="$HOME/wallet"
    fi
else
    TARGET_WALLET_DIR="$HOME/wallet"
fi

echo -e "${CYAN}│${RESET} Lokasi Target: ${YELLOW}$TARGET_WALLET_DIR${RESET}"
if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    su -c "mkdir -p '$TARGET_WALLET_DIR'" 2>/dev/null || true
    [ -d "$FUSION_HOME/wallet" ] && su -c "cp -rf '$FUSION_HOME/wallet/'* '$TARGET_WALLET_DIR/'" 2>/dev/null || true
    # Backup juga ke Termux lokal
    mkdir -p "$HOME/wallet"
    [ -d "$FUSION_HOME/wallet" ] && cp -rf "$FUSION_HOME/wallet/"* "$HOME/wallet/"
else
    mkdir -p "$TARGET_WALLET_DIR"
    [ -d "$FUSION_HOME/wallet" ] && cp -rf "$FUSION_HOME/wallet/"* "$TARGET_WALLET_DIR/"
fi

# Dependensi Web3 di Termux lokal
if [ -d "$HOME/wallet/node_modules/ethers" ]; then
    echo -e "${CYAN}│${RESET} ${GREEN}✓ Dependensi Web3 di Termux lokal sudah terpasang! Melewati npm install.${RESET}"
elif [ -f "$HOME/wallet/package.json" ]; then
    echo -e "${CYAN}│${RESET} Menginstall dependensi Web3 di Termux lokal..."
    cd "$HOME/wallet" 2>/dev/null || cd "$HOME"
    npm install --silent ethers dotenv @walletconnect/core @walletconnect/web3wallet 2>/dev/null || true
    echo -e "${CYAN}│${RESET} ${GREEN}✓ Dependensi Web3 wallet terpasang di Termux!${RESET}"
fi

# Dependensi Web3 di Linux Container jika dipilih
if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    if su -c "[ -d '$TARGET_WALLET_DIR/node_modules/ethers' ]" 2>/dev/null; then
        echo -e "${CYAN}│${RESET} ${GREEN}✓ Dependensi Web3 di Linux container ($CUSTOM_LINUX_NAME) sudah terpasang!${RESET}"
    else
        echo -e "${CYAN}│${RESET} Menginstall dependensi Web3 di Linux container ($CUSTOM_LINUX_NAME)..."
        su -c "chroot '$CONTAINER_ROOTFS' /bin/bash -c 'cd /root/wallet && npm install --silent ethers dotenv @walletconnect/core @walletconnect/web3wallet'" 2>/dev/null || true
        echo -e "${CYAN}│${RESET} ${GREEN}✓ Dependensi Web3 wallet terpasang di $CUSTOM_LINUX_NAME!${RESET}"
    fi
fi
echo -e "${MAGENTA}└───────────────────────────────────────────────────────────────────┘${RESET}\n"

# ==============================================================================
# TAHAP 7: ALIAS & FINALISASI SHELL HOST
# ==============================================================================
ALIAS_SNIPPET='
# Project Fusion Aliases
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
alias wallet="wallet"
'
for RC_FILE in "$HOME/.bashrc" "$HOME/.zshrc"; do
    if [ ! -f "$RC_FILE" ]; then
        touch "$RC_FILE"
    fi
    if ! grep -q "Project Fusion Aliases" "$RC_FILE" 2>/dev/null; then
        echo "$ALIAS_SNIPPET" >> "$RC_FILE"
    elif ! grep -q "alias wallet=" "$RC_FILE" 2>/dev/null; then
        echo 'alias wallet="wallet"' >> "$RC_FILE"
    fi
done

echo -e "${GREEN}${BOLD}================================================================${RESET}"
echo -e "${GREEN}${BOLD}🎉 SETUP PROJECT FUSION SELESAI DENGAN SUKSES!${RESET}"
echo -e "${GREEN}${BOLD}================================================================${RESET}"
echo -e "• Jalankan ${CYAN}browser https://google.com${RESET} untuk membuka WebView."
echo -e "• Jalankan ${CYAN}node $TARGET_WALLET_DIR/wallet.js${RESET} untuk Ghost Wallet."
if [ "$LINUX_OPT" = "1" ]; then
    echo -e "• Perintah ${CYAN}ipc${RESET} dan ${CYAN}browser${RESET} kini aktif langsung di Termux dan di dalam Linux!"
    echo -e "• Ketik ${CYAN}login${RESET} kapan saja untuk memilih masuk Linux atau Termux."
fi
echo ""
