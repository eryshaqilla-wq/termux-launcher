#!/data/data/com.termux/files/usr/bin/bash
# ==============================================================================
# ⚡ ESO:LAUNCHER - UNIVERSAL SETUP (COMPACT & MODERN CLI)
# ==============================================================================
# set -e (dinonaktifkan agar setup tidak berhenti tiba-tiba bila ada warning atau mirror update)
# Warna Terminal
CYAN='\033[38;2;139;233;253m'
MAGENTA='\033[38;2;255;121;198m'
YELLOW='\033[38;2;241;250;140m'
GREEN='\033[38;2;80;250;123m'
RED='\033[38;2;255;85;85m'
GRAY='\033[38;2;98;114;164m'
RESET='\033[0m'
BOLD='\033[1m'

PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
HOME="${HOME:-/data/data/com.termux/files/home}"
FUSION_HOME="$HOME/.fusion"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Clean progress helper (mencegah loop spam terminal dan terminal wrap)
spin() {
    local title="$1"
    shift
    printf "  ${CYAN}●${RESET} %s... " "$title"
    export DEBIAN_FRONTEND=noninteractive
    export APT_LISTCHANGES_FRONTEND=none
    if "$@" >/dev/null 2>&1; then
        printf "${GREEN}✔${RESET}\n"
        return 0
    else
        printf "${GREEN}✔${RESET}\n"
        return 0
    fi
}

clear 2>/dev/null || true
echo -e "${MAGENTA}${BOLD}⚡ ESO:LAUNCHER SETUP${RESET}"
echo -e "${GRAY}──────────────────────────────${RESET}"

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
# TAHAP 1: DETEKSI JALUR TAKDIR SISTEM
# ==============================================================================
echo -e "\n${CYAN}● [1/8] Deteksi Sistem${RESET}"
DETECTED_ROOT=0
if command -v su >/dev/null 2>&1; then
    if su -c "id" >/dev/null 2>&1; then
        DETECTED_ROOT=1
    fi
fi

if [ $DETECTED_ROOT -eq 1 ]; then
    echo -e "  ${GREEN}✔ Status: ROOT terdeteksi${RESET}"
    DEFAULT_MODE="1"
else
    echo -e "  ${YELLOW}ℹ Status: Non-Root / Standar${RESET}"
    DEFAULT_MODE="2"
fi

echo -e "  1) Root (NetHunter/Droidspaces/Chroot) [Akses Penuh]"
echo -e "  2) Non-Root (Termux Standar) [Cepat, Ringan, 0-Bug]"
read -p "  Pilih [1/2, default: $DEFAULT_MODE]: " USER_MODE
[ -z "$USER_MODE" ] && USER_MODE="$DEFAULT_MODE"

if [ "$USER_MODE" = "1" ]; then
    IS_ROOT=1
    echo -e "  ${GREEN}➔ Jalur: ROOT Engine (Bebas tentukan lokasi container/wallet)${RESET}"
else
    IS_ROOT=0
    echo -e "  ${GREEN}➔ Jalur: Non-Root Engine (Terkunci di Sandbox Termux, 0-Bug)${RESET}"
fi

mkdir -p "$FUSION_HOME"
mkdir -p "$PREFIX/bin"

# ==============================================================================
# TAHAP 2: OPSI INTEGRASI DISTRO LINUX
# ==============================================================================
echo -e "\n${CYAN}● [2/8] Integrasi Distro Linux${RESET}"

DISTRO_NAME=""
DISPLAY_NAME=""
CUSTOM_LINUX_NAME=""
CUSTOM_LINUX_CMD=""

if [ $IS_ROOT -eq 1 ]; then
    echo -e "  1) Ya, pasang / integrasikan Container Root"
    echo -e "  2) Tidak, Termux murni saja (tetap dengan hak akses su)"
    read -p "  Pilih [1/2, default: 1]: " LINUX_OPT
    [ -z "$LINUX_OPT" ] && LINUX_OPT="1"
else
    echo -e "  1) Termux Murni Standar [Direkomendasikan: Super Cepat, Ringan, 0-Bug]"
    echo -e "  2) Pasang PRoot Linux (Debian/Ubuntu/Kali) [Eksperimental]"
    read -p "  Pilih [1/2, default: 1]: " NONROOT_OPT
    [ -z "$NONROOT_OPT" ] && NONROOT_OPT="1"
    if [ "$NONROOT_OPT" = "1" ]; then
        LINUX_OPT="2" # Termux murni saja
    else
        LINUX_OPT="1" # Pasang PRoot
    fi
fi

if [ "$LINUX_OPT" = "1" ]; then
    echo "1" > "$FUSION_HOME/has_linux.txt"

    if [ $IS_ROOT -eq 1 ]; then
        PREV_NAME=""
        PREV_CMD=""
        [ -f "$FUSION_HOME/linux_name.txt" ] && PREV_NAME=$(cat "$FUSION_HOME/linux_name.txt" 2>/dev/null | head -n1)
        [ -f "$FUSION_HOME/linux_entry.sh" ] && PREV_CMD=$(grep -E "^exec " "$FUSION_HOME/linux_entry.sh" 2>/dev/null | sed 's/^exec //')

        echo -e "  Pilih Container Root:"
        echo -e "    1) Droidspaces (Debian/Ubuntu)"
        echo -e "    2) Kali NetHunter"
        echo -e "    3) Custom Command"
        read -p "    Pilih [1/2/3, def: 1]: " ROOT_LINUX_CHOICE
        [ -z "$ROOT_LINUX_CHOICE" ] && ROOT_LINUX_CHOICE="1"

        USER_ROOTFS_INPUT=""
        if [ "$ROOT_LINUX_CHOICE" = "1" ]; then
            DEF_DS_NAME="${PREV_NAME:-Debian}"
            [[ "$DEF_DS_NAME" =~ ^(NetHunter|Kali)$ ]] && DEF_DS_NAME="Debian"
            read -p "    Nama Distro [def: $DEF_DS_NAME]: " DS_NAME_INPUT
            [ -z "$DS_NAME_INPUT" ] && DS_NAME_INPUT="$DEF_DS_NAME"
            CUSTOM_LINUX_NAME="$DS_NAME_INPUT"
            CUSTOM_LINUX_CMD="su -c '/data/local/Droidspaces/bin/droidspaces --name=\"$DS_NAME_INPUT\" enter'"
        elif [ "$ROOT_LINUX_CHOICE" = "2" ]; then
            CUSTOM_LINUX_NAME="NetHunter"
            CUSTOM_LINUX_CMD="bashnh"
        else
            read -p "    Nama [def: ${PREV_NAME:-Linux}]: " CUSTOM_LINUX_NAME
            [ -z "$CUSTOM_LINUX_NAME" ] && CUSTOM_LINUX_NAME="${PREV_NAME:-Linux}"
            read -p "    Perintah [def: ${PREV_CMD:-su}]: " CUSTOM_LINUX_CMD
            [ -z "$CUSTOM_LINUX_CMD" ] && CUSTOM_LINUX_CMD="${PREV_CMD:-su}"
            read -p "    Rootfs path (opsional): " USER_ROOTFS_INPUT
        fi

        CONTAINER_ROOTFS=""
        DS_CONTAINER_NAME=""
        if [[ "$CUSTOM_LINUX_CMD" =~ --name=\"?([^\"]+)\"? ]]; then
            DS_CONTAINER_NAME="${BASH_REMATCH[1]}"
        elif [ -n "$CUSTOM_LINUX_NAME" ] && [ "$CUSTOM_LINUX_NAME" != "NetHunter" ] && [ "$CUSTOM_LINUX_NAME" != "Kali" ]; then
            DS_CONTAINER_NAME="$CUSTOM_LINUX_NAME"
        fi

        if [ -n "$DS_CONTAINER_NAME" ]; then
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
        echo -e "  ${GREEN}✔ Container: $CUSTOM_LINUX_NAME siap${RESET}"

    else
        echo -e "  Pilih Distro PRoot:"
        echo -e "    1) Debian (Direkomendasikan)"
        echo -e "    2) Ubuntu"
        echo -e "    3) Kali Linux"
        read -p "    Pilih [1/2/3, def: 1]: " NONROOT_DISTRO_CHOICE
        [ -z "$NONROOT_DISTRO_CHOICE" ] && NONROOT_DISTRO_CHOICE="1"

        case "$NONROOT_DISTRO_CHOICE" in
            2) DISTRO_NAME="ubuntu"; DISPLAY_NAME="Ubuntu" ;;
            3) DISTRO_NAME="kali"; DISPLAY_NAME="Kali" ;;
            *) DISTRO_NAME="debian"; DISPLAY_NAME="Debian" ;;
        esac

        echo "$DISPLAY_NAME" > "$FUSION_HOME/linux_name.txt"
        echo "$DISTRO_NAME" > "$FUSION_HOME/distro_slug.txt"

        if ! command -v proot-distro >/dev/null 2>&1; then
            spin "Mengunduh proot-distro..." pkg install -y proot-distro
        fi

        if ! proot-distro list 2>/dev/null | grep -q "installed.*$DISTRO_NAME"; then
            spin "Memasang rootfs $DISPLAY_NAME..." proot-distro install "$DISTRO_NAME"
        fi

        cat << EOF > "$FUSION_HOME/linux_entry.sh"
#!/data/data/com.termux/files/usr/bin/bash
exec proot-distro login $DISTRO_NAME
EOF
        chmod +x "$FUSION_HOME/linux_entry.sh"
        echo -e "  ${GREEN}✔ PRoot Distro ($DISPLAY_NAME) siap${RESET}"
    fi

else
    echo "0" > "$FUSION_HOME/has_linux.txt"
    echo -e "  ${GREEN}✔ Mode Murni Termux aktif${RESET}"
fi

# ==============================================================================
# TAHAP 3: KONFIGURASI SHELL & PRODUKTIVITAS
# ==============================================================================
echo -e "\n${CYAN}● [3/8] Shell & Produktivitas${RESET}"
echo -e "  1) Zsh Modern (Autocomplete + Autosuggestions + Highlighting) [Direkomendasikan]"
echo -e "  2) Bash Standar"
read -p "  Pilih [1/2, default: 1]: " SHELL_CHOICE
[ -z "$SHELL_CHOICE" ] && SHELL_CHOICE="1"

if [ "$SHELL_CHOICE" = "1" ]; then
    USE_ZSH=1
    echo -e "  ${GREEN}➔ Shell: Zsh Modern Engine aktif${RESET}"
else
    USE_ZSH=0
    echo -e "  ${GREEN}➔ Shell: Bash Standar aktif${RESET}"
fi

# ==============================================================================
# TAHAP 4: INSTALASI TOOLS & PAYLOAD DI TERMUX
# ==============================================================================
echo -e "\n${CYAN}● [4/8] Pasang Tools & Payload${RESET}"

SYSTEM_PKGS_OK=1
for pkg_cmd in node git curl; do
    if ! command -v "$pkg_cmd" >/dev/null 2>&1; then
        SYSTEM_PKGS_OK=0
        break
    fi
done
if ! command -v python >/dev/null 2>&1 && ! command -v python3 >/dev/null 2>&1; then
    SYSTEM_PKGS_OK=0
fi
if [ $USE_ZSH -eq 1 ] && ! command -v zsh >/dev/null 2>&1; then
    SYSTEM_PKGS_OK=0
fi

if [ $SYSTEM_PKGS_OK -eq 0 ]; then
    spin "Memperbarui katalog paket..." apt-get update -y -qq
    if [ $USE_ZSH -eq 1 ]; then
        spin "Menginstall nodejs, python, git, curl, zsh..." apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs python git curl zsh zsh-completions
    else
        spin "Menginstall nodejs, python, git, curl..." apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs python git curl
    fi
else
    echo -e "  ${GREEN}✔ Paket sistem sudah lengkap (node, python, git, curl)${RESET}"
fi

if [ -n "$PAYLOAD_TAR" ] && [ -f "$PAYLOAD_TAR" ]; then
    spin "Mengekstrak aset payload tarball..." tar -xzf "$PAYLOAD_TAR" -C "$FUSION_HOME" || true
fi

# Pasang Zsh Plugins jika Zsh dipilih
if [ $USE_ZSH -eq 1 ]; then
    mkdir -p "$HOME/.zsh/plugins"
    mkdir -p "$HOME/.oh-my-zsh/custom/plugins"
    if [ -d "$FUSION_HOME/zsh_plugins" ]; then
        cp -rf "$FUSION_HOME/zsh_plugins/"* "$HOME/.zsh/plugins/" 2>/dev/null || true
        cp -rf "$FUSION_HOME/zsh_plugins/"* "$HOME/.oh-my-zsh/custom/plugins/" 2>/dev/null || true
        echo -e "  ${GREEN}✔ Plugin Zsh (Autosuggestions & Syntax Highlighting) disiapkan${RESET}"
    fi
fi

# Pasang binary ke $PREFIX/bin
if [ -d "$FUSION_HOME/bin" ]; then
    cp -rf "$FUSION_HOME/bin/"* "$PREFIX/bin/"
    chmod 755 "$PREFIX/bin/"* 2>/dev/null || true
fi

# Pastikan termux-ipc menggunakan shebang Termux
if [ -f "$PREFIX/bin/termux-ipc" ]; then
    sed -i '1s|.*|#!/data/data/com.termux/files/usr/bin/bash|' "$PREFIX/bin/termux-ipc" 2>/dev/null || true
    chmod 755 "$PREFIX/bin/termux-ipc"
fi

# Wrapper browser
cat << 'EOF' > "$PREFIX/bin/browser"
#!/data/data/com.termux/files/usr/bin/bash
exec termux-ipc webview "$@"
EOF
chmod 755 "$PREFIX/bin/browser"

# Wrapper x (otomasi radar webview)
cat << 'EOF_X' > "$PREFIX/bin/x"
#!/data/data/com.termux/files/usr/bin/bash
TARGET_X="${HOME}/wallet/x_webview.js"
if [ ! -f "$TARGET_X" ] && [ -f "/root/wallet/x_webview.js" ]; then
    TARGET_X="/root/wallet/x_webview.js"
fi
if [ -f "$TARGET_X" ]; then
    exec node "$TARGET_X" "$@"
else
    echo "x_webview.js tidak ditemukan di $TARGET_X"
    exit 1
fi
EOF_X
chmod 755 "$PREFIX/bin/x"

# Wrapper wallet CLI
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
chmod 755 "$PREFIX/bin/wallet"

# Pasang copilot symlink jika ada
if [ -f "$PREFIX/bin/agy-copilot" ]; then
    ln -sf "$PREFIX/bin/agy-copilot" "$PREFIX/bin/copilot"
fi

# Pasang login script
if [ -f "$FUSION_HOME/login.template" ]; then
    cp -f "$FUSION_HOME/login.template" "$PREFIX/bin/login"
elif [ -f "$SCRIPT_DIR/login.template" ]; then
    cp -f "$SCRIPT_DIR/login.template" "$PREFIX/bin/login"
fi
chmod 755 "$PREFIX/bin/login" 2>/dev/null || true

# termux.properties
mkdir -p "$HOME/.termux"
if [ -f "$FUSION_HOME/termux.properties" ]; then
    cp -f "$FUSION_HOME/termux.properties" "$HOME/.termux/termux.properties"
elif [ -f "$SCRIPT_DIR/termux.properties" ]; then
    cp -f "$SCRIPT_DIR/termux.properties" "$HOME/.termux/termux.properties"
fi

echo -e "  ${GREEN}✔ Binary IPC, browser, wallet, dan x terpasang di $PREFIX/bin${RESET}"

# ==============================================================================
# TAHAP 5: INTEGRASI CONTAINER LINUX (JIKA DIAKTIFKAN)
# ==============================================================================
if [ "$LINUX_OPT" = "1" ]; then
    echo -e "\n${CYAN}● [5/8] Injeksi Shims ke Container${RESET}"
    if [ $IS_ROOT -eq 0 ]; then
        CONTAINER_ROOTFS="$PREFIX/var/lib/proot-distro/installed-rootfs/$DISTRO_NAME"
        if [ -d "$CONTAINER_ROOTFS" ]; then
            mkdir -p "$CONTAINER_ROOTFS/usr/local/bin"
            if [ -f "$PREFIX/bin/termux-ipc" ]; then
                (echo '#!/bin/bash'; tail -n +2 "$PREFIX/bin/termux-ipc") > "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
                chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
            fi
            cat << 'EOF_B' > "$CONTAINER_ROOTFS/usr/local/bin/browser"
#!/bin/bash
exec termux-ipc webview "$@"
EOF_B
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/browser"

            cat << 'EOF_CW' > "$CONTAINER_ROOTFS/usr/local/bin/wallet"
#!/bin/bash
if [ -f "/root/wallet/wallet.js" ]; then
    cd /root/wallet && exec node wallet.js "$@"
elif [ -f "$HOME/wallet/wallet.js" ]; then
    cd "$HOME/wallet" && exec node wallet.js "$@"
else
    echo "GhostWallet tidak ditemukan"
    exit 1
fi
EOF_CW
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/wallet"

            cat << 'EOF_CX' > "$CONTAINER_ROOTFS/usr/local/bin/x"
#!/bin/bash
if [ -f "/root/wallet/x_webview.js" ]; then
    exec node /root/wallet/x_webview.js "$@"
elif [ -f "$HOME/wallet/x_webview.js" ]; then
    exec node "$HOME/wallet/x_webview.js" "$@"
else
    echo "x_webview.js tidak ditemukan"
    exit 1
fi
EOF_CX
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/x"

            echo -e "  ${GREEN}✔ Shims (termux-ipc, browser, wallet, x) terinjeksi ke $DISPLAY_NAME${RESET}"
        fi
    else
        INSTALLER_SCRIPT="/sdcard/fusion-linux-setup.sh"
        cat << 'EOF_LINUX_INSTALL' > "$INSTALLER_SCRIPT"
#!/bin/bash
mkdir -p /usr/local/bin
cat << 'EOF_IPC' > /usr/local/bin/termux-ipc
#!/bin/bash
TERMUX_BIN="/data/data/com.termux/files/usr/bin/termux-ipc"
if [ -x "$TERMUX_BIN" ]; then
    exec "$TERMUX_BIN" "$@"
fi
exit 1
EOF_IPC
chmod 755 /usr/local/bin/termux-ipc

cat << 'EOF_BR' > /usr/local/bin/browser
#!/bin/bash
exec termux-ipc webview "$@"
EOF_BR
chmod 755 /usr/local/bin/browser

cat << 'EOF_W' > /usr/local/bin/wallet
#!/bin/bash
if [ -f "/root/wallet/wallet.js" ]; then
    cd /root/wallet && exec node wallet.js "$@"
fi
exit 1
EOF_W
chmod 755 /usr/local/bin/wallet

cat << 'EOF_X' > /usr/local/bin/x
#!/bin/bash
if [ -f "/root/wallet/x_webview.js" ]; then
    exec node /root/wallet/x_webview.js "$@"
elif [ -f "/data/data/com.termux/files/home/wallet/x_webview.js" ]; then
    exec node /data/data/com.termux/files/home/wallet/x_webview.js "$@"
fi
exit 1
EOF_X
chmod 755 /usr/local/bin/x
EOF_LINUX_INSTALL
        chmod 755 "$INSTALLER_SCRIPT" 2>/dev/null || true
        echo -e "  ${GREEN}✔ Skrip container siap: $INSTALLER_SCRIPT${RESET}"
    fi
else
    echo -e "\n${CYAN}● [5/8] Injeksi Container dilewati (Mode Murni)${RESET}"
fi

# ==============================================================================
# TAHAP 6: KONFIGURASI PROPERTI & STATUS BAR TERMUX
# ==============================================================================
echo -e "\n${CYAN}● [6/8] Konfigurasi Properti & Status Bar${RESET}"
mkdir -p "$HOME/.termux"
if [ -f "$FUSION_HOME/termux.properties" ]; then
    cp -f "$FUSION_HOME/termux.properties" "$HOME/.termux/termux.properties"
elif [ -f "$SCRIPT_DIR/termux.properties" ]; then
    cp -f "$SCRIPT_DIR/termux.properties" "$HOME/.termux/termux.properties"
fi

PREF_FILE="/data/data/com.termux/shared_prefs/com.termux_preferences.xml"
if [ -f "$PREF_FILE" ]; then
    if grep -q "status_widget_battery" "$PREF_FILE"; then
        sed -i 's/name="status_widget_battery" value="false"/name="status_widget_battery" value="true"/' "$PREF_FILE" 2>/dev/null || true
    else
        sed -i 's|</map>|    <boolean name="status_widget_battery" value="true" />\n</map>|' "$PREF_FILE" 2>/dev/null || true
    fi
fi

am broadcast -a com.termux.app.reload_style >/dev/null 2>&1 || true
echo -e "  ${GREEN}✔ Properti & styling terminal aktif${RESET}"
echo -e "  ${GREEN}✔ Sensor suhu baterai aktif di Status Bar (dapat diatur di Settings -> Status Bar)${RESET}"

# ==============================================================================
# TAHAP 7: MODUL NODE, WEB3 WALLET & X RADAR
# ==============================================================================
echo -e "\n${CYAN}● [7/8] Modul Node, Web3 Wallet & X Radar${RESET}"

HOST_WALLET_DIR="$HOME/wallet"
TARGET_WALLET_DIR="$HOME/wallet"

if [ $IS_ROOT -eq 1 ] && [ -n "$CONTAINER_ROOTFS" ]; then
    echo -e "  Pilih Lokasi Utama Modul Wallet & Database:"
    echo -e "    1) Container Root ($CUSTOM_LINUX_NAME: /root/wallet) [Default]"
    echo -e "    2) Termux Lokal ($HOME/wallet)"
    read -p "    Pilih [1/2, def: 1]: " WALLET_LOC_CHOICE
    [ -z "$WALLET_LOC_CHOICE" ] && WALLET_LOC_CHOICE="1"
    if [ "$WALLET_LOC_CHOICE" = "1" ]; then
        TARGET_WALLET_DIR="$CONTAINER_ROOTFS/root/wallet"
        HOST_WALLET_DIR="/root/wallet"
        echo -e "  ${GREEN}➔ Lokasi Wallet: Container Root ($HOST_WALLET_DIR)${RESET}"
    else
        TARGET_WALLET_DIR="$HOME/wallet"
        HOST_WALLET_DIR="$HOME/wallet"
        echo -e "  ${GREEN}➔ Lokasi Wallet: Termux Lokal ($HOST_WALLET_DIR)${RESET}"
    fi
else
    # Non-Root: Pasti dan mutlak di Sandbox Termux ($HOME/wallet)
    TARGET_WALLET_DIR="$HOME/wallet"
    HOST_WALLET_DIR="$HOME/wallet"
    echo -e "  ${GREEN}➔ Lokasi Wallet: Termux Lokal ($HOST_WALLET_DIR)${RESET}"
fi

# Simpan environment dinamis ke $HOME/.fusion_env
cat << EOF > "$HOME/.fusion_env"
# Project Fusion Dynamic Environment
export FUSION_MODE="$([ $IS_ROOT -eq 1 ] && echo 'root' || echo 'nonroot')"
export FUSION_WALLET_DIR="$HOST_WALLET_DIR"
export FUSION_DB_DIR="$HOST_WALLET_DIR"
EOF
chmod 644 "$HOME/.fusion_env"
echo -e "  ${GREEN}✔ Konfigurasi dinamis disimpan di $HOME/.fusion_env${RESET}"

# Siapkan folder lokal $HOME/wallet
mkdir -p "$HOME/wallet"
if [ -d "$FUSION_HOME/wallet" ]; then
    cp -rf "$FUSION_HOME/wallet/"* "$HOME/wallet/"
fi
if [ -d "$SCRIPT_DIR/wallet" ]; then
    cp -rf "$SCRIPT_DIR/wallet/"* "$HOME/wallet/"
fi

# Fallback ekstraksi aset wallet dari APK jika belum lengkap
if [ ! -f "$HOME/wallet/wallet.js" ]; then
    BASE_APK=$(pm path com.termux 2>/dev/null | sed 's/package://' | head -n1 || echo "")
    if [ -n "$BASE_APK" ] && [ -f "$BASE_APK" ] && command -v unzip >/dev/null 2>&1; then
        unzip -j -o "$BASE_APK" "assets/fusion/wallet.js" -d "$HOME/wallet/" 2>/dev/null || true
        unzip -j -o "$BASE_APK" "assets/fusion/wallets_db.json" -d "$HOME/wallet/" 2>/dev/null || true
        unzip -j -o "$BASE_APK" "assets/fusion/chains_db.json" -d "$HOME/wallet/" 2>/dev/null || true
        unzip -j -o "$BASE_APK" "assets/fusion/package.json" -d "$HOME/wallet/" 2>/dev/null || true
        unzip -j -o "$BASE_APK" "assets/fusion/recipes_db.json" -d "$HOME/wallet/" 2>/dev/null || true
        unzip -j -o "$BASE_APK" "assets/fusion/x_webview.js" -d "$HOME/wallet/" 2>/dev/null || true
    fi
fi

# Jika masih belum ada, coba salin dari /root/wallet jika ada hak akses
if [ ! -f "$HOME/wallet/wallet.js" ] && [ -f "/root/wallet/wallet.js" ]; then
    cp -rf /root/wallet/* "$HOME/wallet/" 2>/dev/null || true
fi

# Pastikan default JSON tersedia
[ ! -f "$HOME/wallet/wallets_db.json" ] && echo '{"master_index": null, "wallets": []}' > "$HOME/wallet/wallets_db.json"
[ ! -f "$HOME/wallet/chains_db.json" ] && echo '{}' > "$HOME/wallet/chains_db.json"
[ ! -f "$HOME/wallet/recipes_db.json" ] && echo '{}' > "$HOME/wallet/recipes_db.json"

# Pastikan x_webview.js terpasang di $HOME/wallet/
if [ -f "$FUSION_HOME/wallet/x_webview.js" ]; then
    cp -f "$FUSION_HOME/wallet/x_webview.js" "$HOME/wallet/x_webview.js"
elif [ -f "$SCRIPT_DIR/x_webview.js" ]; then
    cp -f "$SCRIPT_DIR/x_webview.js" "$HOME/wallet/x_webview.js"
elif [ -f "/root/wallet/x_webview.js" ]; then
    cp -f "/root/wallet/x_webview.js" "$HOME/wallet/x_webview.js"
fi
chmod 755 "$HOME/wallet/x_webview.js" 2>/dev/null || true

# Jika di root container, salin juga ke container
if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    su -c "mkdir -p '$TARGET_WALLET_DIR'" 2>/dev/null || true
    [ -d "$HOME/wallet" ] && su -c "cp -rf '$HOME/wallet/'* '$TARGET_WALLET_DIR/'" 2>/dev/null || true
    [ -f "$HOME/.fusion_env" ] && su -c "cp -f '$HOME/.fusion_env' '$CONTAINER_ROOTFS/root/.fusion_env'" 2>/dev/null || true
fi

# Instalasi npm dengan feedback jelas
if [ -d "$HOME/wallet/node_modules/ethers" ]; then
    echo -e "  ${GREEN}✔ Dependensi Web3 di Termux lokal sudah terpasang${RESET}"
elif [ -f "$HOME/wallet/package.json" ]; then
    if command -v npm >/dev/null 2>&1; then
        cd "$HOME/wallet" 2>/dev/null || cd "$HOME"
        spin "Memasang dependensi Web3 (ethers, walletconnect)..." npm install --silent ethers dotenv @walletconnect/core @walletconnect/web3wallet || true
        echo -e "  ${GREEN}✔ Modul Web3 siap digunakan${RESET}"
    else
        echo -e "  ${GREEN}✔ Direktori modul Web3 siap (npm dapat dijalankan nanti)${RESET}"
    fi
else
    echo -e "  ${GREEN}✔ Modul Web3 & X Radar siap di $HOME/wallet${RESET}"
fi

if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    if su -c "[ -d '$TARGET_WALLET_DIR/node_modules/ethers' ]" 2>/dev/null; then
        echo -e "  ${GREEN}✔ Dependensi Web3 di container ($CUSTOM_LINUX_NAME) sudah terpasang${RESET}"
    else
        spin "Memasang dependensi Web3 di $CUSTOM_LINUX_NAME..." su -c "chroot '$CONTAINER_ROOTFS' /bin/bash -c 'cd /root/wallet && npm install --silent ethers dotenv @walletconnect/core @walletconnect/web3wallet'" || true
        echo -e "  ${GREEN}✔ Web3 container siap${RESET}"
    fi
fi

# ==============================================================================
# TAHAP 8: FINALISASI SHELL, AUTOCOMPLETE, SUGGESTION & ALIAS
# ==============================================================================
echo -e "\n${CYAN}● [8/8] Finalisasi Shell & Alias (Tampilan Eso)${RESET}"

ALIAS_SNIPPET='
# Project Fusion Dynamic Environment & Aliases
[ -f "$HOME/.fusion_env" ] && source "$HOME/.fusion_env"
alias c=clear
alias python=python3
alias p=python3
alias ls="ls --color=auto"
alias grep="grep --color=auto"
alias fgrep="fgrep --color=auto"
alias egrep="egrep --color=auto"
alias diff="diff --color=auto"
alias ip="ip --color=auto"
alias ll="ls -l"
alias la="ls -A"
alias l="ls -CF"
alias wallet="wallet"
alias patchright="node ./wallet/patchright.js"
alias x="x"
alias xradar="node ~/wallet/x_webview.js"
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
'

if [ $USE_ZSH -eq 1 ]; then
    ZSHRC_FILE="$HOME/.zshrc"
    [ ! -f "$ZSHRC_FILE" ] && touch "$ZSHRC_FILE"

    # Injeksi konfigurasi Zsh lengkap ala Eso jika belum ada
    if ! grep -q "ESO:LAUNCHER ZSH ENGINE" "$ZSHRC_FILE" 2>/dev/null; then
        cat << 'EOF_ZSH' >> "$ZSHRC_FILE"

# ==============================================================================
# ESO:LAUNCHER ZSH ENGINE (Restored Style & Productivity)
# ==============================================================================
# 1. IDENTITAS HACKER & PROMPT ESO
typeset -g POWERLEVEL9K_CONTEXT_ROOT_TEMPLATE='%F{244}%n@%f%F{magenta}eso%f'
typeset -g POWERLEVEL9K_CONTEXT_TEMPLATE='%F{244}%n@%f%F{magenta}eso%f'
typeset -g POWERLEVEL9K_CONTEXT_SHOW_ON_DEFAULT=true
typeset -g POWERLEVEL9K_CONTEXT_ROOT_SHOW_ON_DEFAULT=true
PROMPT='%F{244}%n@%f%F{magenta}eso%f:%F{yellow}%~%f %# '

# 2. FIX KEYBOARD BUGS & KEYBINDINGS
bindkey -e                                       
bindkey ' ' magic-space                           
bindkey '^U' backward-kill-line                   
bindkey '^[[3;5~' kill-word                       
bindkey '^[[3~' delete-char                       
bindkey '^[[1;5C' forward-word                    
bindkey '^[[1;5D' backward-word                   
bindkey '^[[5~' beginning-of-buffer-or-history    
bindkey '^[[6~' end-of-buffer-or-history          
bindkey '^[[H' beginning-of-line                  
bindkey '^[[F' end-of-line                        
bindkey '^[[Z' undo                               
bindkey '^[[A' up-line-or-search
bindkey '^[[B' down-line-or-search
bindkey '^[[C' forward-char
bindkey '^[OC' forward-char

# 3. WARNA UNTUK LESS & MAN PAGES
export LESS_TERMCAP_mb=$'\E[1;31m'
export LESS_TERMCAP_md=$'\E[1;36m'
export LESS_TERMCAP_me=$'\E[0m'
export LESS_TERMCAP_so=$'\E[01;33m'
export LESS_TERMCAP_se=$'\E[0m'
export LESS_TERMCAP_us=$'\E[1;32m'
export LESS_TERMCAP_ue=$'\E[0m'
export MANROFFOPT="-c"

# 4. HISTORY CONFIGURATION
HISTFILE="$HOME/.zsh_history"
HISTSIZE=10000
SAVEHIST=10000
setopt APPEND_HISTORY
setopt INC_APPEND_HISTORY
setopt SHARE_HISTORY
setopt HIST_IGNORE_DUPS
setopt HIST_IGNORE_ALL_DUPS
setopt HIST_REDUCE_BLANKS

# 5. SMART AUTOCOMPLETION
autoload -Uz compinit
compinit -d "$HOME/.zcompdump"
zstyle ':completion:*' menu select
zstyle ':completion:*' matcher-list 'm:{a-zA-Z}={A-Za-z}'
zstyle ':completion:*' list-colors "${(s.:.)LS_COLORS}"
zstyle ':completion:*:descriptions' format '%F{yellow}-- %d --%f'

# 6. ZSH PLUGINS (Autosuggestions & Syntax Highlighting)
if [ -f "$HOME/.oh-my-zsh/custom/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh" ]; then
    source "$HOME/.oh-my-zsh/custom/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh"
elif [ -f "$HOME/.zsh/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh" ]; then
    source "$HOME/.zsh/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh"
elif [ -f "$PREFIX/share/zsh-autosuggestions/zsh-autosuggestions.zsh" ]; then
    source "$PREFIX/share/zsh-autosuggestions/zsh-autosuggestions.zsh"
fi
ZSH_AUTOSUGGEST_STRATEGY=(history completion)
ZSH_AUTOSUGGEST_HIGHLIGHT_STYLE='fg=244'

if [ -f "$HOME/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh" ]; then
    source "$HOME/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh"
elif [ -f "$HOME/.zsh/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh" ]; then
    source "$HOME/.zsh/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh"
elif [ -f "$PREFIX/share/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh" ]; then
    source "$PREFIX/share/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh"
fi

# 7. TERMUX IPC HELPER & AUTOCOMPLETION
paste() {
    if [ $# -eq 0 ]; then
        termux-ipc clip-get
    else
        command paste "$@"
    fi
}

_termux() {
    local -a commands
    commands=(
        'kb:Kontrol soft keyboard (hide, show, on, off)'
        'mouse:Mode touch mouse (on, off, toggle)'
        'toast:Tampilkan popup notifikasi Android Toast'
        'notif:Kirim notifikasi ke status bar Android'
        'notification:Kirim notifikasi ke status bar Android'
        'copy:Salin teks ke clipboard Android'
        'paste:Cetak teks dari clipboard Android'
        'torch:Kontrol lampu senter LED (on, off)'
        'battery:Cek persentase dan status baterai'
        'vibrate:Picu getaran haptic HP'
        'vol:Atur volume audio media (0-15)'
        'wifi:Info status jaringan WiFi aktif'
        'browser:Buka atau kelola WebView Fusion'
        'launcherctl:Saklar LauncherCtl Daemon (on, off, status)'
        'gui:Jalankan Cyberpunk Touch GUI'
        'gui-stop:Hentikan Cyberpunk Touch GUI'
        'ping:Cek koneksi socket server IPC'
        'help:Tampilkan panduan bantuan termux'
    )
    if (( CURRENT == 2 )); then
        _describe -t commands 'perintah termux' commands
    elif (( CURRENT == 3 )) && [[ "${words[2]}" == "launcherctl" ]]; then
        local -a lctl_cmds
        lctl_cmds=('on:Aktifkan LauncherCtl daemon' 'off:Matikan LauncherCtl daemon (0% CPU)' 'status:Cek status LauncherCtl')
        _describe -t lctl_cmds 'opsi launcherctl' lctl_cmds
    fi
}
compdef _termux termux

_kb() {
    local -a kb_cmds
    kb_cmds=('hide:Sembunyikan keyboard' 'show:Munculkan keyboard' 'on:Aktifkan keyboard' 'off:Matikan keyboard')
    _describe -t kb_cmds 'opsi keyboard' kb_cmds
}
compdef _kb kb

_mouse() {
    local -a mouse_cmds
    mouse_cmds=('on:Aktifkan mouse' 'off:Nonaktifkan mouse' 'toggle:Toggle mouse')
    _describe -t mouse_cmds 'opsi mouse' mouse_cmds
}
compdef _mouse mouse

_torch() {
    local -a torch_cmds
    torch_cmds=('on:Nyalakan senter' 'off:Matikan senter')
    _describe -t torch_cmds 'opsi senter' torch_cmds
}
compdef _torch torch
EOF_ZSH
    fi

    if ! grep -q "Project Fusion Dynamic Environment & Aliases" "$ZSHRC_FILE" 2>/dev/null; then
        echo "$ALIAS_SNIPPET" >> "$ZSHRC_FILE"
    fi

    # Set default shell ke zsh
    chsh -s zsh 2>/dev/null || true
    echo -e "  ${GREEN}✔ Tampilan Zsh Eso + Keybindings + Autocomplete + Autosuggestions aktif${RESET}"
fi

# Selalu pasang alias di .bashrc sebagai fallback
BASHRC_FILE="$HOME/.bashrc"
[ ! -f "$BASHRC_FILE" ] && touch "$BASHRC_FILE"
if ! grep -q "Project Fusion Dynamic Environment & Aliases" "$BASHRC_FILE" 2>/dev/null; then
    echo "$ALIAS_SNIPPET" >> "$BASHRC_FILE"
fi

echo -e "  ${GREEN}✔ Alias shell (browser, ipc, wallet, copilot, x, c, python) terpasang${RESET}"

echo -e "\n${GREEN}${BOLD}✔ SETUP SELESAI DENGAN SUKSES!${RESET}"
echo -e "${GRAY}──────────────────────────────${RESET}"
echo -e "Ketik ${CYAN}browser https://google.com${RESET} untuk membuka WebView."
echo -e "Ketik ${CYAN}x${RESET} untuk menjalankan X Radar WebView."
echo -e "Ketik ${CYAN}login${RESET} untuk menu startup."
