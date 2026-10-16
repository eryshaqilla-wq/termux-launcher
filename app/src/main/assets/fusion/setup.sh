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

# Node / Ora style spinner helper
spin() {
    local title="$1"
    shift
    if [ -t 1 ]; then
        local spinstr='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
        local delay=0.07
        "$@" >/dev/null 2>&1 &
        local pid=$!
        tput civis 2>/dev/null || true
        while kill -0 $pid 2>/dev/null; do
            local temp=${spinstr#?}
            printf "\r ${CYAN}%c${RESET} %s" "$spinstr" "$title"
            spinstr=$temp${spinstr%"$temp"}
            sleep $delay
        done
        wait $pid
        local code=$?
        tput cnorm 2>/dev/null || true
        if [ $code -eq 0 ]; then
            printf "\r ${GREEN}✔${RESET} %s\n" "$title"
        else
            printf "\r ${YELLOW}▲${RESET} %s (selesai)\n" "$title"
        fi
        return $code
    else
        printf " -> %s ... " "$title"
        if "$@" >/dev/null 2>&1; then
            printf "${GREEN}✔${RESET}\n"
        else
            printf "${YELLOW}▲${RESET}\n"
        fi
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
echo -e "\n${CYAN}● [1/7] Deteksi Sistem${RESET}"
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

echo -e "  1) Root (NetHunter/Chroot)"
echo -e "  2) Non-Root (Standar/PRoot)"
read -p "  Pilih [1/2, default: $DEFAULT_MODE]: " USER_MODE
[ -z "$USER_MODE" ] && USER_MODE="$DEFAULT_MODE"

if [ "$USER_MODE" = "1" ]; then
    IS_ROOT=1
    echo -e "  ${GREEN}➔ Jalur: ROOT Engine${RESET}"
else
    IS_ROOT=0
    echo -e "  ${GREEN}➔ Jalur: Non-Root Engine${RESET}"
fi

mkdir -p "$FUSION_HOME"
mkdir -p "$PREFIX/bin"

# ==============================================================================
# TAHAP 2: OPSI INTEGRASI DISTRO LINUX
# ==============================================================================
echo -e "\n${CYAN}● [2/7] Integrasi Distro Linux${RESET}"
echo -e "  1) Ya, pasang / integrasikan"
echo -e "  2) Tidak, Termux murni saja"
read -p "  Pilih [1/2, default: 1]: " LINUX_OPT
[ -z "$LINUX_OPT" ] && LINUX_OPT="1"

DISTRO_NAME=""
DISPLAY_NAME=""
CUSTOM_LINUX_NAME=""
CUSTOM_LINUX_CMD=""

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
# TAHAP 3: INSTALASI TOOLS & PAYLOAD DI TERMUX
# ==============================================================================
echo -e "\n${CYAN}● [3/7] Pasang Tools & Payload${RESET}"
SYSTEM_PKGS_OK=1
for pkg_cmd in node python git curl; do
    if ! command -v "$pkg_cmd" >/dev/null 2>&1; then
        SYSTEM_PKGS_OK=0
        break
    fi
done

if [ $SYSTEM_PKGS_OK -eq 0 ]; then
    spin "Memperbarui katalog paket..." pkg update -y || apt-get update -y || true
    spin "Menginstall nodejs, python, git, curl..." pkg install -y nodejs python git curl || pkg install -y nodejs-lts python git curl || true
else
    echo -e "  ${GREEN}✔ Paket sistem (Node, Python, Git, Curl) sudah lengkap${RESET}"
fi

if [ -n "$PAYLOAD_TAR" ] && [ -f "$PAYLOAD_TAR" ]; then
    spin "Mengekstrak aset payload tarball..." tar -xzf "$PAYLOAD_TAR" -C "$FUSION_HOME" || true
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
# TAHAP 4: INTEGRASI CONTAINER LINUX (JIKA DIAKTIFKAN)
# ==============================================================================
if [ "$LINUX_OPT" = "1" ]; then
    echo -e "\n${CYAN}● [4/7] Injeksi Shims ke Container${RESET}"
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
    echo -e "\n${CYAN}● [4/7] Injeksi Container dilewati (Mode Murni)${RESET}"
fi

# ==============================================================================
# TAHAP 5: KONFIGURASI PROPERTI & STATUS BAR TERMUX
# ==============================================================================
echo -e "\n${CYAN}● [5/7] Konfigurasi Properti & Status Bar${RESET}"
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
# TAHAP 6: MODUL NODE, WEB3 WALLET & X RADAR
# ==============================================================================
echo -e "\n${CYAN}● [6/7] Modul Node, Web3 Wallet & X Radar${RESET}"

TARGET_WALLET_DIR="$HOME/wallet"
if [ $IS_ROOT -eq 1 ] && [ -n "$CONTAINER_ROOTFS" ]; then
    TARGET_WALLET_DIR="$CONTAINER_ROOTFS/root/wallet"
fi

mkdir -p "$HOME/wallet"
if [ -d "$FUSION_HOME/wallet" ]; then
    cp -rf "$FUSION_HOME/wallet/"* "$HOME/wallet/"
fi

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
    [ -d "$FUSION_HOME/wallet" ] && su -c "cp -rf '$FUSION_HOME/wallet/'* '$TARGET_WALLET_DIR/'" 2>/dev/null || true
    [ -f "$HOME/wallet/x_webview.js" ] && su -c "cp -f '$HOME/wallet/x_webview.js' '$TARGET_WALLET_DIR/x_webview.js'" 2>/dev/null || true
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
# TAHAP 7: ALIAS & FINALISASI SHELL
# ==============================================================================
echo -e "\n${CYAN}● [7/7] Finalisasi Shell${RESET}"
ALIAS_SNIPPET='
# Project Fusion Aliases
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
alias wallet="wallet"
alias x="node $HOME/wallet/x_webview.js"
'
for RC_FILE in "$HOME/.bashrc" "$HOME/.zshrc"; do
    [ ! -f "$RC_FILE" ] && touch "$RC_FILE"
    if ! grep -q "Project Fusion Aliases" "$RC_FILE" 2>/dev/null; then
        echo "$ALIAS_SNIPPET" >> "$RC_FILE"
    fi
done

echo -e "  ${GREEN}✔ Alias shell (browser, ipc, wallet, copilot, x) terpasang${RESET}"

echo -e "\n${GREEN}${BOLD}✔ SETUP SELESAI DENGAN SUKSES!${RESET}"
echo -e "${GRAY}──────────────────────────────${RESET}"
echo -e "Ketik ${CYAN}browser https://google.com${RESET} untuk membuka WebView."
echo -e "Ketik ${CYAN}x${RESET} untuk menjalankan X Radar WebView."
echo -e "Ketik ${CYAN}login${RESET} untuk menu startup."
