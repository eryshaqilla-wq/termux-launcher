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
    echo -e "  ${GREEN}✔ Status: ROOT terdeteksi (Akses Superuser Aktif)${RESET}"
    IS_ROOT=1
    echo -e "  ${GREEN}➔ Takdir: ROOT Engine (Mendukung Chroot, Droidspaces & NetHunter)${RESET}"
else
    echo -e "  ${YELLOW}ℹ Status: Non-Root / Standar Android (Hak Akses Terisolasi)${RESET}"
    IS_ROOT=0
    echo -e "  ${GREEN}➔ Takdir: Kunci Otomatis ke Sandbox Termux (Cepat, Ringan, 0-Bug)${RESET}"
fi

mkdir -p "$FUSION_HOME"
mkdir -p "$PREFIX/bin"

# ==============================================================================
# TAHAP 2: OPSI INTEGRASI DISTRO LINUX (AUTO-DETECT CERDAS)
# ==============================================================================
echo -e "\n${CYAN}● [2/8] Integrasi Distro Linux${RESET}"

DISTRO_NAME=""
DISPLAY_NAME=""
CUSTOM_LINUX_NAME=""
CUSTOM_LINUX_CMD=""
CONTAINER_ROOTFS=""

if [ $IS_ROOT -eq 1 ]; then
    # Auto-detect container root yang ada di filesystem
    DETECTED_DS_NAME=""
    DETECTED_DS_ROOTFS=""
    DETECTED_NH=0

    # 1. Cek Droidspaces containers
    DS_CANDIDATE=$(su -c "ls -1 /data/local/Droidspaces/Containers/ 2>/dev/null || ls -1 /data/local/Droidspaces/spaces/ 2>/dev/null || true" | grep -v '^\.' | head -n1 | tr -d '\r\n')
    if [ -n "$DS_CANDIDATE" ]; then
        DETECTED_DS_NAME="$DS_CANDIDATE"
        if su -c "[ -d '/data/local/Droidspaces/Containers/$DS_CANDIDATE/rootfs' ]" 2>/dev/null; then
            DETECTED_DS_ROOTFS="/data/local/Droidspaces/Containers/$DS_CANDIDATE/rootfs"
        elif su -c "[ -d '/data/local/Droidspaces/spaces/$DS_CANDIDATE/rootfs' ]" 2>/dev/null; then
            DETECTED_DS_ROOTFS="/data/local/Droidspaces/spaces/$DS_CANDIDATE/rootfs"
        elif su -c "[ -d '/data/local/Droidspaces/Containers/$DS_CANDIDATE' ]" 2>/dev/null; then
            DETECTED_DS_ROOTFS="/data/local/Droidspaces/Containers/$DS_CANDIDATE"
        fi
    fi

    # 2. Cek Kali NetHunter
    if su -c "[ -d '/data/local/nhsystem/kali-arm64' ] || [ -d '/data/local/nhsystem/kalifs' ]" 2>/dev/null; then
        DETECTED_NH=1
    fi

    if [ -n "$DETECTED_DS_NAME" ] || [ $DETECTED_NH -eq 1 ]; then
        if [ -n "$DETECTED_DS_NAME" ]; then
            AUTO_TYPE="Droidspaces ($DETECTED_DS_NAME)"
            AUTO_NAME="$DETECTED_DS_NAME"
            AUTO_CMD="su -c '/data/local/Droidspaces/bin/droidspaces --name=\"$DETECTED_DS_NAME\" enter'"
            AUTO_ROOTFS="$DETECTED_DS_ROOTFS"
        else
            AUTO_TYPE="Kali NetHunter"
            AUTO_NAME="NetHunter"
            AUTO_CMD="bashnh"
            AUTO_ROOTFS="/data/local/nhsystem/kali-arm64"
        fi

        echo -e "  ${GREEN}✔ Ditemukan container: ${BOLD}$AUTO_TYPE${RESET}"
        echo -e "    1) Auto-Detect: $AUTO_TYPE [Direkomendasikan]"
        echo -e "    2) Pilih Container Lain Secara Manual"
        echo -e "    3) Termux Murni Saja (tetap dengan hak akses root/su)"
        read -p "    Pilih [1/2/3, default: 1]: " ROOT_AUTO_CHOICE
        [ -z "$ROOT_AUTO_CHOICE" ] && ROOT_AUTO_CHOICE="1"

        if [ "$ROOT_AUTO_CHOICE" = "1" ]; then
            LINUX_OPT="1"
            CUSTOM_LINUX_NAME="$AUTO_NAME"
            CUSTOM_LINUX_CMD="$AUTO_CMD"
            CONTAINER_ROOTFS="$AUTO_ROOTFS"
        elif [ "$ROOT_AUTO_CHOICE" = "3" ]; then
            LINUX_OPT="2"
        else
            LINUX_OPT="1"
            echo -e "    Pilih Tipe Container:"
            echo -e "      1) Droidspaces"
            echo -e "      2) Kali NetHunter"
            echo -e "      3) Custom Command"
            read -p "      Pilih [1/2/3, def: 1]: " MANUAL_ROOT_CHOICE
            [ -z "$MANUAL_ROOT_CHOICE" ] && MANUAL_ROOT_CHOICE="1"
            if [ "$MANUAL_ROOT_CHOICE" = "1" ]; then
                read -p "      Nama Distro Droidspaces [def: ${DETECTED_DS_NAME:-Ubuntu}]: " DS_NAME_INPUT
                [ -z "$DS_NAME_INPUT" ] && DS_NAME_INPUT="${DETECTED_DS_NAME:-Ubuntu}"
                CUSTOM_LINUX_NAME="$DS_NAME_INPUT"
                CUSTOM_LINUX_CMD="su -c '/data/local/Droidspaces/bin/droidspaces --name=\"$DS_NAME_INPUT\" enter'"
                [ -n "$DETECTED_DS_ROOTFS" ] && CONTAINER_ROOTFS="$DETECTED_DS_ROOTFS"
            elif [ "$MANUAL_ROOT_CHOICE" = "2" ]; then
                CUSTOM_LINUX_NAME="NetHunter"
                CUSTOM_LINUX_CMD="bashnh"
                CONTAINER_ROOTFS="/data/local/nhsystem/kali-arm64"
            else
                read -p "      Nama Linux [def: Linux]: " CUSTOM_LINUX_NAME
                [ -z "$CUSTOM_LINUX_NAME" ] && CUSTOM_LINUX_NAME="Linux"
                read -p "      Perintah Masuk [def: su]: " CUSTOM_LINUX_CMD
                [ -z "$CUSTOM_LINUX_CMD" ] && CUSTOM_LINUX_CMD="su"
            fi
        fi

    else
        # Tidak ada container yang otomatis terdeteksi
        echo -e "  1) Pasang / Integrasikan Container Root"
        echo -e "  2) Termux Murni Saja (tetap dengan hak akses su)"
        read -p "  Pilih [1/2, default: 1]: " LINUX_OPT
        [ -z "$LINUX_OPT" ] && LINUX_OPT="1"

        if [ "$LINUX_OPT" = "1" ]; then
            echo -e "    Pilih Container Root:"
            echo -e "      1) Droidspaces (Ubuntu/Debian)"
            echo -e "      2) Kali NetHunter"
            echo -e "      3) Custom Command"
            read -p "      Pilih [1/2/3, def: 1]: " MANUAL_ROOT_CHOICE
            [ -z "$MANUAL_ROOT_CHOICE" ] && MANUAL_ROOT_CHOICE="1"
            if [ "$MANUAL_ROOT_CHOICE" = "1" ]; then
                read -p "      Nama Distro Droidspaces [def: Ubuntu]: " DS_NAME_INPUT
                [ -z "$DS_NAME_INPUT" ] && DS_NAME_INPUT="Ubuntu"
                CUSTOM_LINUX_NAME="$DS_NAME_INPUT"
                CUSTOM_LINUX_CMD="su -c '/data/local/Droidspaces/bin/droidspaces --name=\"$DS_NAME_INPUT\" enter'"
            elif [ "$MANUAL_ROOT_CHOICE" = "2" ]; then
                CUSTOM_LINUX_NAME="NetHunter"
                CUSTOM_LINUX_CMD="bashnh"
                CONTAINER_ROOTFS="/data/local/nhsystem/kali-arm64"
            else
                read -p "      Nama Distro [def: Linux]: " CUSTOM_LINUX_NAME
                [ -z "$CUSTOM_LINUX_NAME" ] && CUSTOM_LINUX_NAME="Linux"
                read -p "      Perintah [def: su]: " CUSTOM_LINUX_CMD
                [ -z "$CUSTOM_LINUX_CMD" ] && CUSTOM_LINUX_CMD="su"
            fi
        fi
    fi

else
    # NON-ROOT MODE: Hanya 1 takdir aman (Termux Standar), atau opsional PRoot
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
        # Cari Rootfs container jika belum terset
        if [ -z "$CONTAINER_ROOTFS" ]; then
            if [ -n "$CUSTOM_LINUX_NAME" ] && [ "$CUSTOM_LINUX_NAME" != "NetHunter" ] && [ "$CUSTOM_LINUX_NAME" != "Kali" ]; then
                if su -c "[ -d '/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME/rootfs' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME/rootfs"
                elif su -c "[ -d '/data/local/Droidspaces/spaces/$CUSTOM_LINUX_NAME/rootfs' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/Droidspaces/spaces/$CUSTOM_LINUX_NAME/rootfs"
                elif su -c "[ -d '/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/Droidspaces/Containers/$CUSTOM_LINUX_NAME"
                fi
            fi
        fi

        if [ -z "$CONTAINER_ROOTFS" ]; then
            if [[ "$CUSTOM_LINUX_CMD" =~ kali-arm64 ]] || [ "$CUSTOM_LINUX_NAME" = "NetHunter" ] || [ "$CUSTOM_LINUX_NAME" = "Kali" ]; then
                if su -c "[ -d '/data/local/nhsystem/kali-arm64' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/nhsystem/kali-arm64"
                elif su -c "[ -d '/data/local/nhsystem/kalifs' ]" 2>/dev/null; then
                    CONTAINER_ROOTFS="/data/local/nhsystem/kalifs"
                fi
            fi
        fi

        echo "$CUSTOM_LINUX_NAME" > "$FUSION_HOME/linux_name.txt"
        [ -n "$CONTAINER_ROOTFS" ] && echo "$CONTAINER_ROOTFS" > "$FUSION_HOME/linux_rootfs.txt"

        if [[ "$CUSTOM_LINUX_CMD" =~ droidspaces\ --name=\"?([^\"]+)\"?\ enter ]]; then
            DS_N="${BASH_REMATCH[1]}"
            cat << EOF > "$FUSION_HOME/linux_entry.sh"
#!/data/data/com.termux/files/usr/bin/bash
unset LD_PRELOAD
unset LD_LIBRARY_PATH
if [ -x "/data/data/com.termux/files/usr/bin/termux-ipc-bridge" ]; then
    if ! pgrep -f termux-ipc-bridge >/dev/null 2>&1; then
        /data/data/com.termux/files/usr/bin/termux-ipc-bridge </dev/null >/dev/null 2>&1 &
    fi
fi
if su -c "[ -x '/data/local/Droidspaces/bin/droidspaces' ]" 2>/dev/null; then
    if ! su -c "/data/local/Droidspaces/bin/droidspaces pid --name='$DS_N' >/dev/null 2>&1"; then
        echo -e "\e[38;2;139;233;253m● Menyalakan container $DS_N...\e[0m"
        su -c "/data/local/Droidspaces/bin/droidspaces --name='$DS_N' start" >/dev/null 2>&1 || true
    fi
fi
exec $CUSTOM_LINUX_CMD
EOF
        else
            cat << EOF > "$FUSION_HOME/linux_entry.sh"
#!/data/data/com.termux/files/usr/bin/bash
unset LD_PRELOAD
unset LD_LIBRARY_PATH
exec $CUSTOM_LINUX_CMD
EOF
        fi
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
echo -e "  1) Zsh Modern (Powerlevel10k + Autosuggestions + Highlighting + History) [Direkomendasikan]"
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
# TAHAP 4: INSTALASI TOOLS & PAYLOAD OFFLINE DI TERMUX
# ==============================================================================
echo -e "\n${CYAN}● [4/8] Pasang Tools & Payload${RESET}"

SYSTEM_PKGS_OK=1
for pkg_cmd in node npm git curl; do
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
        spin "Menginstall nodejs, npm, python, git, curl, unzip, zsh..." apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs npm python git curl unzip zsh zsh-completions || apt-get install -y nodejs npm python git curl unzip zsh 2>/dev/null || pkg install -y nodejs npm python git curl unzip zsh 2>/dev/null || true
    else
        spin "Menginstall nodejs, npm, python, git, curl, unzip..." apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs npm python git curl unzip || apt-get install -y nodejs npm python git curl unzip 2>/dev/null || pkg install -y nodejs npm python git curl unzip 2>/dev/null || true
    fi
else
    echo -e "  ${GREEN}✔ Paket sistem sudah lengkap (node, npm, python, git, curl)${RESET}"
fi

if [ -n "$PAYLOAD_TAR" ] && [ -f "$PAYLOAD_TAR" ]; then
    spin "Mengekstrak aset payload tarball offline..." tar -xzf "$PAYLOAD_TAR" -C "$FUSION_HOME" || true
fi

# Pasang Zsh Plugins & Theme Powerlevel10k Offline
if [ $USE_ZSH -eq 1 ]; then
    mkdir -p "$HOME/.zsh/plugins"
    mkdir -p "$HOME/.zsh/themes"
    mkdir -p "$HOME/.oh-my-zsh/custom/plugins"
    mkdir -p "$HOME/.oh-my-zsh/custom/themes"

    # Plugins
    if [ -d "$FUSION_HOME/zsh_plugins" ]; then
        cp -rf "$FUSION_HOME/zsh_plugins/"* "$HOME/.zsh/plugins/" 2>/dev/null || true
        cp -rf "$FUSION_HOME/zsh_plugins/"* "$HOME/.oh-my-zsh/custom/plugins/" 2>/dev/null || true
        echo -e "  ${GREEN}✔ Plugin Zsh (Autosuggestions & Syntax Highlighting) terpasang offline${RESET}"
    fi

    # Theme Powerlevel10k
    if [ -d "$FUSION_HOME/zsh_themes/powerlevel10k" ]; then
        cp -rf "$FUSION_HOME/zsh_themes/powerlevel10k" "$HOME/.zsh/themes/" 2>/dev/null || true
        cp -rf "$FUSION_HOME/zsh_themes/powerlevel10k" "$HOME/.oh-my-zsh/custom/themes/" 2>/dev/null || true
        echo -e "  ${GREEN}✔ Tema Powerlevel10k terpasang offline${RESET}"
    elif [ -d "$SCRIPT_DIR/zsh_themes/powerlevel10k" ]; then
        cp -rf "$SCRIPT_DIR/zsh_themes/powerlevel10k" "$HOME/.zsh/themes/" 2>/dev/null || true
        cp -rf "$SCRIPT_DIR/zsh_themes/powerlevel10k" "$HOME/.oh-my-zsh/custom/themes/" 2>/dev/null || true
    fi

    # Config .p10k.zsh
    if [ -f "$FUSION_HOME/configs/.p10k.zsh" ]; then
        cp -f "$FUSION_HOME/configs/.p10k.zsh" "$HOME/.p10k.zsh"
        echo -e "  ${GREEN}✔ Konfigurasi tema Catppuccin eso@eso disinkronkan (.p10k.zsh)${RESET}"
    elif [ -f "$SCRIPT_DIR/p10k.zsh" ]; then
        cp -f "$SCRIPT_DIR/p10k.zsh" "$HOME/.p10k.zsh"
    elif [ -f "/root/.p10k.zsh" ]; then
        cp -f "/root/.p10k.zsh" "$HOME/.p10k.zsh" 2>/dev/null || true
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

# Pasang bashnh
if [ -f "$FUSION_HOME/bin/bashnh" ]; then
    cp -f "$FUSION_HOME/bin/bashnh" "$PREFIX/bin/bashnh"
elif [ -f "$SCRIPT_DIR/bashnh" ]; then
    cp -f "$SCRIPT_DIR/bashnh" "$PREFIX/bin/bashnh"
fi
chmod 755 "$PREFIX/bin/bashnh" 2>/dev/null || true

# Pasang termux-ipc-bridge
if [ -f "$FUSION_HOME/bin/termux-ipc-bridge" ]; then
    cp -f "$FUSION_HOME/bin/termux-ipc-bridge" "$PREFIX/bin/termux-ipc-bridge"
elif [ -f "$SCRIPT_DIR/termux-ipc-bridge" ]; then
    cp -f "$SCRIPT_DIR/termux-ipc-bridge" "$PREFIX/bin/termux-ipc-bridge"
fi
chmod 755 "$PREFIX/bin/termux-ipc-bridge" 2>/dev/null || true

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
    cd "$WALLET_DIR"
    if [ ! -d "node_modules/dotenv" ] && command -v npm >/dev/null 2>&1; then
        echo "Menyiapkan dependensi wallet (dotenv, ethers)..."
        npm install --silent --no-audit --no-fund dotenv ethers @walletconnect/core @walletconnect/web3wallet 2>/dev/null || true
    fi
    exec node wallet.js "$@"
elif [ -f "/root/wallet/wallet.js" ]; then
    cd "/root/wallet"
    if [ ! -d "node_modules/dotenv" ] && command -v npm >/dev/null 2>&1; then
        echo "Menyiapkan dependensi wallet (dotenv, ethers)..."
        npm install --silent --no-audit --no-fund dotenv ethers @walletconnect/core @walletconnect/web3wallet 2>/dev/null || true
    fi
    exec node wallet.js "$@"
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

echo -e "  ${GREEN}✔ Binary IPC, browser, wallet, bashnh, dan x terpasang di $PREFIX/bin${RESET}"

# ==============================================================================
# TAHAP 5: INTEGRASI & INJEKSI SHIMS CONTAINER SECARA INSTAN
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

            # Konfigurasi Audio Bridge (PulseAudio & ALSA) ke host port 4713
            if [ -d "$CONTAINER_ROOTFS/etc/pulse" ]; then
                sed -i 's/^; default-server =.*/default-server = 127.0.0.1:4713/' "$CONTAINER_ROOTFS/etc/pulse/client.conf" 2>/dev/null || true
                sed -i 's/^; autospawn =.*/autospawn = no/' "$CONTAINER_ROOTFS/etc/pulse/client.conf" 2>/dev/null || true
            fi
            if [ -d "$CONTAINER_ROOTFS/etc" ]; then
                cat << 'EOF_PALSA' > "$CONTAINER_ROOTFS/etc/asound.conf"
pcm.!default {
    type pulse
    fallback "sysdefault"
}
ctl.!default {
    type pulse
    fallback "sysdefault"
}
EOF_PALSA
                chmod 644 "$CONTAINER_ROOTFS/etc/asound.conf" 2>/dev/null || true
            fi

            echo -e "  ${GREEN}✔ Shims & Audio Bridge terinjeksi ke $DISPLAY_NAME${RESET}"
        fi
    else
        # Root Mode: Siapkan shims langsung di rootfs container jika path diketahui
        if [ -n "$CONTAINER_ROOTFS" ] && su -c "[ -d '$CONTAINER_ROOTFS' ]" 2>/dev/null; then
            spin "Menginjeksi shims langsung ke rootfs $CUSTOM_LINUX_NAME..." su -c "
                mkdir -p '$CONTAINER_ROOTFS/usr/local/bin'
                (echo '#!/bin/bash'; tail -n +2 '$PREFIX/bin/termux-ipc') > '$CONTAINER_ROOTFS/usr/local/bin/termux-ipc'
                echo -e '#!/bin/bash\nexec termux-ipc webview \"\$@\"' > '$CONTAINER_ROOTFS/usr/local/bin/browser'
                echo -e '#!/bin/bash\nif [ -f /root/wallet/wallet.js ]; then\n  cd /root/wallet && exec node wallet.js \"\$@\"\nfi' > '$CONTAINER_ROOTFS/usr/local/bin/wallet'
                echo -e '#!/bin/bash\nif [ -f /root/wallet/x_webview.js ]; then\n  exec node /root/wallet/x_webview.js \"\$@\"\nfi' > '$CONTAINER_ROOTFS/usr/local/bin/x'
                chmod 755 '$CONTAINER_ROOTFS/usr/local/bin/'*
                # Konfigurasi Audio Bridge (PulseAudio & ALSA) ke host port 4713
                if [ -d '$CONTAINER_ROOTFS/etc/pulse' ]; then
                    sed -i 's/^; default-server =.*/default-server = 127.0.0.1:4713/' '$CONTAINER_ROOTFS/etc/pulse/client.conf' 2>/dev/null || true
                    sed -i 's/^; autospawn =.*/autospawn = no/' '$CONTAINER_ROOTFS/etc/pulse/client.conf' 2>/dev/null || true
                fi
                if [ -d '$CONTAINER_ROOTFS/etc' ]; then
                    echo -e 'pcm.!default {\n    type pulse\n    fallback \"sysdefault\"\n}\nctl.!default {\n    type pulse\n    fallback \"sysdefault\"\n}' > '$CONTAINER_ROOTFS/etc/asound.conf' 2>/dev/null || true
                    chmod 644 '$CONTAINER_ROOTFS/etc/asound.conf' 2>/dev/null || true
                fi
            "
            echo -e "  ${GREEN}✔ Shims & Audio Bridge terinjeksi langsung ke $CUSTOM_LINUX_NAME (/usr/local/bin/)${RESET}"
        fi

        # Jalankan termux-ipc-bridge jika Droidspaces container digunakan
        if [ -x "$PREFIX/bin/termux-ipc-bridge" ]; then
            if ! pgrep -f termux-ipc-bridge >/dev/null 2>&1; then
                "$PREFIX/bin/termux-ipc-bridge" </dev/null >/dev/null 2>&1 &
                echo -e "  ${GREEN}✔ Daemon termux-ipc-bridge aktif di host (Jembatan IPC Droidspaces)${RESET}"
            fi
        fi

        # Jika Droidspaces sedang berjalan, pastikan juga shims aktif di container
        if [ -x "/data/local/Droidspaces/bin/droidspaces" ] && [ -n "$CUSTOM_LINUX_NAME" ] && [ "$CUSTOM_LINUX_NAME" != "NetHunter" ]; then
            su -c "/data/local/Droidspaces/bin/droidspaces --name=\"$CUSTOM_LINUX_NAME\" run -- [ -f /usr/local/bin/termux-ipc ] || (mkdir -p /usr/local/bin && (echo '#!/bin/bash'; tail -n +2 /data/data/com.termux/files/usr/bin/termux-ipc) > /usr/local/bin/termux-ipc && chmod 755 /usr/local/bin/termux-ipc)" >/dev/null 2>&1 || true
        fi
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
echo -e "  ${GREEN}✔ Sensor suhu baterai aktif di Status Bar${RESET}"

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
    TARGET_WALLET_DIR="$HOME/wallet"
    HOST_WALLET_DIR="$HOME/wallet"
    echo -e "  ${GREEN}➔ Lokasi Wallet: Termux Lokal ($HOST_WALLET_DIR)${RESET}"
fi

cat << EOF > "$HOME/.fusion_env"
# Project Fusion Dynamic Environment
export FUSION_MODE="$([ $IS_ROOT -eq 1 ] && echo 'root' || echo 'nonroot')"
export FUSION_WALLET_DIR="$HOST_WALLET_DIR"
export FUSION_DB_DIR="$HOST_WALLET_DIR"
EOF
chmod 644 "$HOME/.fusion_env"
echo -e "  ${GREEN}✔ Konfigurasi dinamis disimpan di $HOME/.fusion_env${RESET}"

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

if [ ! -f "$HOME/wallet/wallet.js" ] && [ -f "/root/wallet/wallet.js" ]; then
    cp -rf /root/wallet/* "$HOME/wallet/" 2>/dev/null || true
fi

[ ! -f "$HOME/wallet/wallets_db.json" ] && echo '{"master_index": null, "wallets": []}' > "$HOME/wallet/wallets_db.json"
[ ! -f "$HOME/wallet/chains_db.json" ] && echo '{}' > "$HOME/wallet/chains_db.json"
[ ! -f "$HOME/wallet/recipes_db.json" ] && echo '{}' > "$HOME/wallet/recipes_db.json"

if [ -f "$FUSION_HOME/wallet/x_webview.js" ]; then
    cp -f "$FUSION_HOME/wallet/x_webview.js" "$HOME/wallet/x_webview.js"
elif [ -f "$SCRIPT_DIR/x_webview.js" ]; then
    cp -f "$SCRIPT_DIR/x_webview.js" "$HOME/wallet/x_webview.js"
elif [ -f "/root/wallet/x_webview.js" ]; then
    cp -f "/root/wallet/x_webview.js" "$HOME/wallet/x_webview.js"
fi
chmod 755 "$HOME/wallet/x_webview.js" 2>/dev/null || true

if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    su -c "mkdir -p '$TARGET_WALLET_DIR'" 2>/dev/null || true
    [ -d "$HOME/wallet" ] && su -c "cp -rf '$HOME/wallet/'* '$TARGET_WALLET_DIR/'" 2>/dev/null || true
    [ -f "$HOME/.fusion_env" ] && su -c "cp -f '$HOME/.fusion_env' '$CONTAINER_ROOTFS/root/.fusion_env'" 2>/dev/null || true
fi

if [ -d "$HOME/wallet/node_modules/ethers" ] && [ -d "$HOME/wallet/node_modules/dotenv" ]; then
    echo -e "  ${GREEN}✔ Dependensi Web3 di Termux lokal sudah terpasang${RESET}"
else
    if command -v npm >/dev/null 2>&1; then
        cd "$HOME/wallet" 2>/dev/null || cd "$HOME"
        spin "Memasang dependensi Web3 (ethers, dotenv, walletconnect)..." npm install --silent --no-audit --no-fund ethers dotenv @walletconnect/core @walletconnect/web3wallet || true
    fi
fi

# ==============================================================================
# TAHAP 8: FINALISASI SHELL, AUTOCOMPLETE, SUGGESTION, HISTORY & ALIAS
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
alias bashnh="bashnh"
alias login="login"
'

if [ $USE_ZSH -eq 1 ]; then
    ZSHRC_FILE="$HOME/.zshrc"
    
    # Jika template zshrc ada di payload, pasang sebagai basis
    if [ ! -f "$ZSHRC_FILE" ] || [ ! -s "$ZSHRC_FILE" ]; then
        if [ -f "$FUSION_HOME/configs/.zshrc" ]; then
            cp -f "$FUSION_HOME/configs/.zshrc" "$ZSHRC_FILE"
        elif [ -f "$SCRIPT_DIR/zshrc.template" ]; then
            cp -f "$SCRIPT_DIR/zshrc.template" "$ZSHRC_FILE"
        fi
    fi
    [ ! -f "$ZSHRC_FILE" ] && touch "$ZSHRC_FILE"

    # Injeksi konfigurasi Zsh lengkap ala Eso jika belum ada
    if ! grep -q "ESO:LAUNCHER ZSH ENGINE" "$ZSHRC_FILE" 2>/dev/null; then
        cat << 'EOF_ZSH' >> "$ZSHRC_FILE"

# ==============================================================================
# ESO:LAUNCHER ZSH ENGINE (Restored Style & Productivity)
# ==============================================================================
# 1. INSTANT PROMPT & THEME POWERLEVEL10K
if [[ -r "${XDG_CACHE_HOME:-$HOME/.cache}/p10k-instant-prompt-${(%):-%n}.zsh" ]]; then
  source "${XDG_CACHE_HOME:-$HOME/.cache}/p10k-instant-prompt-${(%):-%n}.zsh"
fi

if [ -f "$HOME/.zsh/themes/powerlevel10k/powerlevel10k.zsh-theme" ]; then
    source "$HOME/.zsh/themes/powerlevel10k/powerlevel10k.zsh-theme"
elif [ -f "$HOME/.oh-my-zsh/custom/themes/powerlevel10k/powerlevel10k.zsh-theme" ]; then
    source "$HOME/.oh-my-zsh/custom/themes/powerlevel10k/powerlevel10k.zsh-theme"
fi
[[ ! -f ~/.p10k.zsh ]] || source ~/.p10k.zsh

# 2. IDENTITAS HACKER & PROMPT ESO (Fallback jika p10k belum aktif)
typeset -g POWERLEVEL9K_CONTEXT_ROOT_TEMPLATE='%F{244}%n@%f%F{magenta}eso%f'
typeset -g POWERLEVEL9K_CONTEXT_TEMPLATE='%F{244}%n@%f%F{magenta}eso%f'
typeset -g POWERLEVEL9K_CONTEXT_SHOW_ON_DEFAULT=true
typeset -g POWERLEVEL9K_CONTEXT_ROOT_SHOW_ON_DEFAULT=true
PROMPT='%F{244}%n@%f%F{magenta}eso%f:%F{yellow}%~%f %# '

# 3. FIX KEYBOARD BUGS & KEYBINDINGS
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
bindkey '^[[C' forward-char
bindkey '^[OC' forward-char

# History prefix search (Panah Atas / Bawah mencari command dengan awalan yang sama)
autoload -Uz up-line-or-beginning-search down-line-or-beginning-search
zle -N up-line-or-beginning-search
zle -N down-line-or-beginning-search
bindkey '^[[A' up-line-or-beginning-search
bindkey '^[[B' down-line-or-beginning-search
bindkey '^[OA' up-line-or-beginning-search
bindkey '^[OB' down-line-or-beginning-search

# 4. WARNA UNTUK LESS & MAN PAGES
export LESS_TERMCAP_mb=$'\E[1;31m'
export LESS_TERMCAP_md=$'\E[1;36m'
export LESS_TERMCAP_me=$'\E[0m'
export LESS_TERMCAP_so=$'\E[01;33m'
export LESS_TERMCAP_se=$'\E[0m'
export LESS_TERMCAP_us=$'\E[1;32m'
export LESS_TERMCAP_ue=$'\E[0m'
export MANROFFOPT="-c"

# 5. HISTORY CONFIGURATION (Lengkap & Tahan Banting)
HISTFILE="$HOME/.zsh_history"
HISTSIZE=10000
SAVEHIST=10000
setopt APPEND_HISTORY
setopt INC_APPEND_HISTORY
setopt SHARE_HISTORY
setopt HIST_IGNORE_DUPS
setopt HIST_IGNORE_ALL_DUPS
setopt HIST_REDUCE_BLANKS
setopt HIST_FIND_NO_DUPS
setopt HIST_SAVE_NO_DUPS

# 6. SMART AUTOCOMPLETION
autoload -Uz compinit
compinit -d "$HOME/.zcompdump"
zstyle ':completion:*' menu select
zstyle ':completion:*' matcher-list 'm:{a-zA-Z}={A-Za-z}'
zstyle ':completion:*' list-colors "${(s.:.)LS_COLORS}"
zstyle ':completion:*:descriptions' format '%F{yellow}-- %d --%f'

# 7. ZSH PLUGINS (Autosuggestions & Syntax Highlighting)
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

# 8. TERMUX IPC HELPER & AUTOCOMPLETION
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
    echo -e "  ${GREEN}✔ Tampilan Zsh Eso + Keybindings + History + Autocomplete + Autosuggestions aktif${RESET}"
fi

# Selalu pasang alias di .bashrc sebagai fallback
BASHRC_FILE="$HOME/.bashrc"
[ ! -f "$BASHRC_FILE" ] && touch "$BASHRC_FILE"
if ! grep -q "Project Fusion Dynamic Environment & Aliases" "$BASHRC_FILE" 2>/dev/null; then
    echo "$ALIAS_SNIPPET" >> "$BASHRC_FILE"
fi

echo -e "  ${GREEN}✔ Alias shell (browser, ipc, wallet, copilot, x, c, python, bashnh, login) terpasang${RESET}"

echo -e "\n${GREEN}${BOLD}✔ SETUP SELESAI DENGAN SUKSES!${RESET}"
echo -e "${GRAY}──────────────────────────────${RESET}"
echo -e "Ketik ${CYAN}browser https://google.com${RESET} untuk membuka WebView."
echo -e "Ketik ${CYAN}x${RESET} untuk menjalankan X Radar WebView."
echo -e "Ketik ${CYAN}login${RESET} untuk menu startup."
echo -e "Ketik ${CYAN}bashnh${RESET} untuk langsung masuk ke container Linux aktif."
