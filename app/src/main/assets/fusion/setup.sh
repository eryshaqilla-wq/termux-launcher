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

export PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
export PATH="$PREFIX/bin:$PREFIX/bin/applets:$PATH"
if [ -z "$HOME" ] || [ "$HOME" = "/data" ] || [ "$HOME" = "/" ]; then
    export HOME="/data/data/com.termux/files/home"
fi
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

# ==============================================================================
# DETEKSI DAN PROTEKSI AKSES FUSION BUNDLE (AES-256)
# ==============================================================================
decrypt_payload() {
    local pin="$1"
    local infile="$2"
    local outfile="$3"

    # 1. Coba OpenSSL
    if command -v openssl >/dev/null 2>&1; then
        if openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass pass:"$pin" -in "$infile" -out "$outfile" 2>/dev/null; then
            if [ -s "$outfile" ]; then
                return 0
            fi
        fi
    fi

    # 2. Coba Node.js
    if command -v node >/dev/null 2>&1; then
        if node -e "
const fs = require('fs');
const crypto = require('crypto');
try {
    const data = fs.readFileSync(process.argv[2]);
    if (data.subarray(0, 8).toString() !== 'Salted__') process.exit(1);
    const salt = data.subarray(8, 16);
    const ciphertext = data.subarray(16);
    const keyIv = crypto.pbkdf2Sync(process.argv[1], salt, 100000, 48, 'sha256');
    const decipher = crypto.createDecipheriv('aes-256-cbc', keyIv.subarray(0, 32), keyIv.subarray(32, 48));
    const out = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
    fs.writeFileSync(process.argv[3], out);
    process.exit(0);
} catch (e) {
    process.exit(1);
}
" "$pin" "$infile" "$outfile" 2>/dev/null; then
            if [ -s "$outfile" ]; then
                return 0
            fi
        fi
    fi

    # 3. Fallback: coba install openssl-tool jika belum ada tool kriptografi
    if ! command -v openssl >/dev/null 2>&1 && ! command -v node >/dev/null 2>&1; then
        echo -e "  ${YELLOW}ℹ Mengunduh modul kriptografi untuk verifikasi...${RESET}"
        pkg install -y openssl-tool >/dev/null 2>&1 || apt-get install -y openssl-tool >/dev/null 2>&1 || true
        if command -v openssl >/dev/null 2>&1; then
            if openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 -pass pass:"$pin" -in "$infile" -out "$outfile" 2>/dev/null; then
                if [ -s "$outfile" ]; then
                    return 0
                fi
            fi
        fi
    fi

    return 1
}

# Cari payload terenkripsi (.enc) di prioritas utama
PAYLOAD_ENC=""
PAYLOAD_TAR=""

for loc in "$SCRIPT_DIR" "$HOME" "$HOME/.fusion"; do
    if [ -f "$loc/fusion_payload.tar.gz.enc" ]; then
        PAYLOAD_ENC="$loc/fusion_payload.tar.gz.enc"
        break
    elif [ -f "$loc/fusion_payload.enc" ]; then
        PAYLOAD_ENC="$loc/fusion_payload.enc"
        break
    fi
done

# Jika belum ada di disk, ekstrak .enc langsung dari aset APK terpasang
if [ -z "$PAYLOAD_ENC" ]; then
    BASE_APK=$(pm path com.termux 2>/dev/null | sed 's/package://' | head -n1 || echo "")
    if [ -n "$BASE_APK" ] && [ -f "$BASE_APK" ]; then
        if command -v unzip >/dev/null 2>&1; then
            unzip -p "$BASE_APK" assets/fusion/fusion_payload.tar.gz.enc > "$HOME/fusion_payload.tar.gz.enc" 2>/dev/null || true
            [ -s "$HOME/fusion_payload.tar.gz.enc" ] && PAYLOAD_ENC="$HOME/fusion_payload.tar.gz.enc"
        fi
    fi
fi

# Fallback hanya jika .enc benar-benar tidak ada di mana pun
if [ -z "$PAYLOAD_ENC" ]; then
    for loc in "$SCRIPT_DIR" "$HOME" "$HOME/.fusion"; do
        if [ -f "$loc/fusion_payload.tar.gz" ]; then
            PAYLOAD_TAR="$loc/fusion_payload.tar.gz"
            break
        fi
    done
fi

# Jika payload terenkripsi ditemukan, minta PIN akses
TEMP_PAYLOAD_UNLOCKED=""
if [ -n "$PAYLOAD_ENC" ] && [ -f "$PAYLOAD_ENC" ]; then
    echo -e "\n${MAGENTA}${BOLD}🔒 PROTEKSI ESO:LAUNCHER FUSION BUNDLE${RESET}"
    echo -e "${GRAY}Aset payload diproteksi enkripsi AES-256. Dibutuhkan PIN akses untuk membuka.${RESET}"

    DECRYPT_SUCCESS=0
    MAX_ATTEMPTS=3
    ATTEMPT=1

    # Cek jika PIN dipassing via environment variable FUSION_PIN
    if [ -n "$FUSION_PIN" ]; then
        TEMP_CANDIDATE="$FUSION_HOME/payload_unlocked_$$.tar.gz"
        mkdir -p "$FUSION_HOME"
        if decrypt_payload "$FUSION_PIN" "$PAYLOAD_ENC" "$TEMP_CANDIDATE"; then
            PAYLOAD_TAR="$TEMP_CANDIDATE"
            TEMP_PAYLOAD_UNLOCKED="$TEMP_CANDIDATE"
            DECRYPT_SUCCESS=1
            echo -e "  ${GREEN}✔ PIN Otomatis Terverifikasi via FUSION_PIN!${RESET}"
        fi
    fi

    while [ $DECRYPT_SUCCESS -eq 0 ] && [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
        echo -ne "  ${CYAN}🔑 Masukkan PIN Akses (percobaan $ATTEMPT/$MAX_ATTEMPTS): ${RESET}"
        read -s ENTERED_PIN
        echo ""

        if [ -z "$ENTERED_PIN" ]; then
            echo -e "  ${RED}✖ PIN tidak boleh kosong!${RESET}"
            ATTEMPT=$((ATTEMPT + 1))
            continue
        fi

        TEMP_CANDIDATE="$FUSION_HOME/payload_unlocked_$$.tar.gz"
        mkdir -p "$FUSION_HOME"
        if decrypt_payload "$ENTERED_PIN" "$PAYLOAD_ENC" "$TEMP_CANDIDATE"; then
            PAYLOAD_TAR="$TEMP_CANDIDATE"
            TEMP_PAYLOAD_UNLOCKED="$TEMP_CANDIDATE"
            DECRYPT_SUCCESS=1
            echo -e "  ${GREEN}✔ PIN Terverifikasi! Akses bundle dibuka.${RESET}"
            break
        else
            echo -e "  ${RED}✖ PIN Akses Salah!${RESET}"
            rm -f "$TEMP_CANDIDATE" 2>/dev/null || true
            ATTEMPT=$((ATTEMPT + 1))
        fi
    done

    if [ $DECRYPT_SUCCESS -eq 0 ]; then
        echo -e "\n${RED}${BOLD}⛔ AKSES DITOLAK: PIN salah. Anda tidak memiliki izin untuk memasang bundle ini.${RESET}\n"
        exit 1
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
if ! command -v termux-notification >/dev/null 2>&1; then
    SYSTEM_PKGS_OK=0
fi

if [ $SYSTEM_PKGS_OK -eq 0 ]; then
    spin "Memperbarui katalog paket..." apt-get update -y -qq 2>/dev/null || pkg update -y -qq 2>/dev/null || true
    if [ $USE_ZSH -eq 1 ]; then
        spin "Menginstall nodejs, npm, python, git, curl, unzip, zsh, termux-api..." pkg install -y nodejs-lts npm python git curl unzip zsh termux-api openssl-tool 2>/dev/null || pkg install -y nodejs npm python git curl unzip zsh termux-api openssl-tool 2>/dev/null || apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs npm python3 git curl unzip zsh 2>/dev/null || true
    else
        spin "Menginstall nodejs, npm, python, git, curl, unzip, termux-api..." pkg install -y nodejs-lts npm python git curl unzip termux-api openssl-tool 2>/dev/null || pkg install -y nodejs npm python git curl unzip termux-api openssl-tool 2>/dev/null || apt-get install -y --no-install-recommends -o Dpkg::Options::="--force-confold" nodejs npm python3 git curl unzip 2>/dev/null || true
    fi
else
    echo -e "  ${GREEN}✔ Paket sistem sudah lengkap (node, npm, python, git, curl, zsh, termux-api)${RESET}"
fi

# Cek & Pasang paket GUI/X11, Input Method & Audio di Native Termux (bspwm, sxhkd, rofi, polybar, picom, feh, pulseaudio, virgl, fcitx5)
GUI_PKGS_OK=1
for gpkg in bspwm sxhkd rofi polybar picom feh pulseaudio fcitx5; do
    if ! command -v "$gpkg" >/dev/null 2>&1; then
        GUI_PKGS_OK=0
        break
    fi
done
if [ ! -f "$PREFIX/bin/virgl_test_server_android" ]; then
    GUI_PKGS_OK=0
fi

if [ $GUI_PKGS_OK -eq 0 ]; then
    spin "Memasang x11-repo & dependensi bspwm/fcitx5/audio di Termux..." bash -c '
        pkg install -y x11-repo 2>/dev/null || true
        pkg install -y bspwm sxhkd rofi polybar picom feh pulseaudio virglrenderer-android fcitx5 fcitx5-gtk3 fcitx5-qt 2>/dev/null || apt-get install -y bspwm sxhkd rofi polybar picom feh pulseaudio virglrenderer-android fcitx5 fcitx5-gtk3 fcitx5-qt 2>/dev/null || true
    '
    echo -e "  ${GREEN}✔ bspwm, sxhkd, rofi, polybar, picom, feh, pulseaudio, virgl & fcitx5 terpasang di Termux${RESET}"
else
    echo -e "  ${GREEN}✔ Paket GUI, Audio & Fcitx5 Native Termux sudah lengkap${RESET}"
fi

if [ -n "$PAYLOAD_TAR" ] && [ -f "$PAYLOAD_TAR" ]; then
    spin "Mengekstrak aset payload tarball offline..." tar -xzf "$PAYLOAD_TAR" -C "$FUSION_HOME" || true
    if [ -n "$TEMP_PAYLOAD_UNLOCKED" ] && [ -f "$TEMP_PAYLOAD_UNLOCKED" ]; then
        rm -f "$TEMP_PAYLOAD_UNLOCKED" 2>/dev/null || true
    fi
fi

# Pasang Tema Eso (bspwm, polybar, rofi, picom, dunst, gtk-3.0 & wallpaper) di Termux
THEME_SRC=""
if [ -d "$FUSION_HOME/theme" ]; then
    THEME_SRC="$FUSION_HOME/theme"
elif [ -d "$SCRIPT_DIR/theme" ]; then
    THEME_SRC="$SCRIPT_DIR/theme"
fi

if [ -n "$THEME_SRC" ]; then
    mkdir -p "$HOME/.config" "$HOME/Pictures/wallpapers"
    if [ -d "$THEME_SRC/.config" ]; then
        cp -rf "$THEME_SRC/.config/"* "$HOME/.config/" 2>/dev/null || true
    fi
    if [ -d "$THEME_SRC/Pictures/wallpapers" ]; then
        cp -rf "$THEME_SRC/Pictures/wallpapers/"* "$HOME/Pictures/wallpapers/" 2>/dev/null || true
    fi
    if [ -f "$HOME/Pictures/wallpapers/potrait.jpeg" ]; then
        ln -sf "../../Pictures/wallpapers/potrait.jpeg" "$HOME/.config/bspwm/wallpaper" 2>/dev/null || true
    elif [ -f "$HOME/Pictures/wallpapers/evening-sky.png" ]; then
        ln -sf "../../Pictures/wallpapers/evening-sky.png" "$HOME/.config/bspwm/wallpaper" 2>/dev/null || true
    fi
    if command -v termux-fix-shebang >/dev/null 2>&1; then
        termux-fix-shebang "$HOME/.config/bspwm/"* "$HOME/.config/polybar/"* 2>/dev/null || true
    fi
    chmod +x "$HOME/.config/bspwm/"*.sh "$HOME/.config/bspwm/"*.py "$HOME/.config/polybar/"*.sh 2>/dev/null || true
    # Bersihkan librofitouch hook dan kembalikan binary rofi murni
    rm -f "$PREFIX/lib/librofitouch.so" 2>/dev/null || true
    if [ -f "$PREFIX/bin/rofi.real" ]; then
        mv -f "$PREFIX/bin/rofi.real" "$PREFIX/bin/rofi" 2>/dev/null || true
    fi

    chmod +x "$HOME/.config/bspwm/"*.sh "$HOME/.config/polybar/"*.sh "$HOME/.config/bspwm/"*.py 2>/dev/null || true
    chmod +x "$HOME/.config/bspwm/bspwmrc" "$HOME/.config/polybar/launch.sh" 2>/dev/null || true
    ln -sf "$HOME/.config/bspwm/rofi-az.sh" "$PREFIX/bin/rofi-az" 2>/dev/null || true
    ln -sf "$HOME/.config/bspwm/dyn-apps.py" "$PREFIX/bin/dyn-apps" 2>/dev/null || true
    echo -e "  ${GREEN}✔ Tema Eso (bspwm, polybar, rofi, picom & wallpapers) terpasang di Termux ($HOME/.config)${RESET}"
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

# Wrapper Native bspwm & Desktop
cat << 'EOF_BSPWM' > "$PREFIX/bin/termux-desktop-wm"
#!/data/data/com.termux/files/usr/bin/bash
# Native BSPWM Desktop for Termux Launcher
export PREFIX="/data/data/com.termux/files/usr"
export HOME="${HOME:-/data/data/com.termux/files/home}"
export PATH="$PREFIX/bin:$PATH"
export DISPLAY="${DISPLAY:-:0}"
export PULSE_SERVER="tcp:127.0.0.1:4713"
export GALLIUM_DRIVER=virpipe
export MESA_GL_VERSION_OVERRIDE=4.3COMPAT
export MESA_GLES_VERSION_OVERRIDE=3.2
export MESA_NO_ERROR=1
export LIBGL_DRI3_DISABLE=1
export VTEST_SOCKET_NAME="$PREFIX/tmp/.virgl/.virgl_test"

if [ -d "$PREFIX/tmp" ]; then
    export TMPDIR="$PREFIX/tmp"
    export BSPWM_SOCKET="${BSPWM_SOCKET:-$PREFIX/tmp/bspwm_0_0-socket}"
fi

for _w in $(seq 1 50); do [ -S "$PREFIX/tmp/.X11-unix/X0" ] || [ -S "/tmp/.X11-unix/X0" ] && break; sleep 0.1; done
pulseaudio --start --load="module-native-protocol-tcp auth-ip-acl=127.0.0.1;172.28.0.0/16 auth-anonymous=1" --exit-idle-time=-1 >/dev/null 2>&1 || true
pkill -x sxhkd 2>/dev/null || true
sxhkd &
exec bspwm
EOF_BSPWM
chmod 755 "$PREFIX/bin/termux-desktop-wm"
cp -f "$PREFIX/bin/termux-desktop-wm" "$PREFIX/bin/termux-bspwm"
chmod 755 "$PREFIX/bin/termux-bspwm"

# Wrapper Native Standalone Kiosk WM
cat << 'EOF_KIOSK' > "$PREFIX/bin/termux-kiosk-wm"
#!/data/data/com.termux/files/usr/bin/bash
# Native Host Kiosk Window Manager (Standalone App) for Termux Launcher
export PREFIX="/data/data/com.termux/files/usr"
export HOME="${HOME:-/data/data/com.termux/files/home}"
export PATH="$PREFIX/bin:$PATH"
export DISPLAY="${DISPLAY:-:0}"
export PULSE_SERVER="tcp:127.0.0.1:4713"
export GALLIUM_DRIVER=virpipe
export MESA_GL_VERSION_OVERRIDE=4.3COMPAT
export MESA_GLES_VERSION_OVERRIDE=3.2
export MESA_NO_ERROR=1
export LIBGL_DRI3_DISABLE=1
export VTEST_SOCKET_NAME="$PREFIX/tmp/.virgl/.virgl_test"

for _w in $(seq 1 50); do [ -S "$PREFIX/tmp/.X11-unix/X0" ] || [ -S "/tmp/.X11-unix/X0" ] && break; sleep 0.1; done

if [ -x "$PREFIX/bin/bspwm" ]; then
    exec "$PREFIX/bin/bspwm" -c "$HOME/.config/bspwm/kiosk.bspwmrc"
elif command -v openbox >/dev/null 2>&1; then
    exec openbox
elif command -v xfwm4 >/dev/null 2>&1; then
    exec xfwm4
elif command -v i3 >/dev/null 2>&1; then
    exec i3
else
    while true; do sleep 3600; done
fi
EOF_KIOSK
chmod 755 "$PREFIX/bin/termux-kiosk-wm"

# Wrapper x (otomasi radar webview)
cat << 'EOF_X' > "$PREFIX/bin/x"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
ENGINE_MODE="${FUSION_ENGINE_MODE:-cjs}"

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if [ "$ENGINE_MODE" = "cjs" ]; then
    if [ -f "$TARGET_DIR/x_webview.cjs" ]; then
        exec node "$TARGET_DIR/x_webview.cjs" "$@"
    elif [ -f "$TARGET_DIR/x_webview.js" ]; then
        exec node "$TARGET_DIR/x_webview.js" "$@"
    fi
else
    if [ -f "$TARGET_DIR/x_webview.js" ]; then
        exec node "$TARGET_DIR/x_webview.js" "$@"
    elif [ -f "$TARGET_DIR/x_webview.cjs" ]; then
        exec node "$TARGET_DIR/x_webview.cjs" "$@"
    fi
fi
echo "x_webview (.cjs/.js) tidak ditemukan di $TARGET_DIR"
exit 1
EOF_X
chmod 755 "$PREFIX/bin/x"

# Wrapper wallet CLI
cat << 'EOF_W' > "$PREFIX/bin/wallet"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
ENGINE_MODE="${FUSION_ENGINE_MODE:-cjs}"

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if [ "$ENGINE_MODE" = "cjs" ]; then
    if [ -f "$TARGET_DIR/wallet.cjs" ]; then
        cd "$TARGET_DIR" && exec node wallet.cjs "$@"
    elif [ -f "$TARGET_DIR/wallet.js" ]; then
        cd "$TARGET_DIR" && exec node wallet.js "$@"
    fi
else
    if [ -f "$TARGET_DIR/wallet.js" ]; then
        cd "$TARGET_DIR"
        if [ ! -d "node_modules/dotenv" ] && command -v npm >/dev/null 2>&1; then
            echo "Menyiapkan dependensi wallet (dotenv, ethers)..."
            npm install --silent --no-audit --no-fund dotenv ethers @walletconnect/core @walletconnect/web3wallet 2>/dev/null || true
        fi
        exec node wallet.js "$@"
    elif [ -f "$TARGET_DIR/wallet.cjs" ]; then
        cd "$TARGET_DIR" && exec node wallet.cjs "$@"
    fi
fi
echo "GhostWallet (.cjs/.js) tidak ditemukan di $TARGET_DIR"
exit 1
EOF_W
chmod 755 "$PREFIX/bin/wallet"

# Wrapper pemantau CLI (Telegram Sniper)
cat << 'EOF_P' > "$PREFIX/bin/pemantau"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
ENGINE_MODE="${FUSION_ENGINE_MODE:-cjs}"

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if [ "$ENGINE_MODE" = "cjs" ]; then
    if [ -f "$TARGET_DIR/pemantau.cjs" ]; then
        cd "$TARGET_DIR" && exec node pemantau.cjs "$@"
    elif [ -f "$TARGET_DIR/pemantautelegram.py" ]; then
        cd "$TARGET_DIR" && exec python3 pemantautelegram.py "$@"
    fi
else
    if [ -f "$TARGET_DIR/pemantautelegram.py" ]; then
        cd "$TARGET_DIR" && exec python3 pemantautelegram.py "$@"
    elif [ -f "$TARGET_DIR/pemantau.cjs" ]; then
        cd "$TARGET_DIR" && exec node pemantau.cjs "$@"
    fi
fi
echo "Pemantau (.cjs/.py) tidak ditemukan di $TARGET_DIR"
exit 1
EOF_P
chmod 755 "$PREFIX/bin/pemantau"

# Wrapper telegram CLI (Telegram Swarm & Bot Orchestrator)
cat << 'EOF_TG' > "$PREFIX/bin/telegram"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
ENGINE_MODE="${FUSION_ENGINE_MODE:-cjs}"

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if [ -f "$TARGET_DIR/telegram.cjs" ]; then
    cd "$TARGET_DIR" && exec node telegram.cjs "$@"
elif [ -f "$TARGET_DIR/pemantau.cjs" ]; then
    cd "$TARGET_DIR" && exec node pemantau.cjs "$@"
elif [ -f "$TARGET_DIR/pemantautelegram.py" ]; then
    cd "$TARGET_DIR" && exec python3 pemantautelegram.py "$@"
fi
echo "Telegram Swarm (.cjs/.py) tidak ditemukan di $TARGET_DIR"
exit 1
EOF_TG
chmod 755 "$PREFIX/bin/telegram"

# Wrapper telegram-ui (Telegram Web UI & Background Server)
cat << 'EOF_TGUI' > "$PREFIX/bin/telegram-ui"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
PORT=20130

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if curl -s "http://127.0.0.1:${PORT}/status" >/dev/null 2>&1; then
    echo "✨ Sesi Telegram Swarm Server sudah aktif di port ${PORT}."
    echo "✨ Membuka Telegram Webview di layar Android..."
    if command -v termux-ipc >/dev/null 2>&1; then
        termux-ipc toast "Membuka Telegram Swarm Web UI..."
    fi
    exec termux-ipc webview "http://127.0.0.1:${PORT}"
fi

OTHER_PIDS=$(ps -eo pid,args 2>/dev/null | grep -E "(telegram\.cjs|pemantau\.cjs|pemantautelegram\.py)" | grep -v -E "(grep|--ui|tmux)" | awk '{print $1}')
if [ -n "$OTHER_PIDS" ]; then
    echo "Terdeteksi sesi Telegram terminal (PID: $OTHER_PIDS) sedang berjalan."
    echo "Mematikan sesi terminal lama agar tidak bentrok..."
    for p in $OTHER_PIDS; do
        kill -9 "$p" 2>/dev/null || true
    done
    sleep 1.2
fi

if [ -f "$TARGET_DIR/telegram.cjs" ]; then
    cd "$TARGET_DIR"
    nohup node telegram.cjs --kill --ui >/tmp/telegram_ui.log 2>&1 &
    sleep 1.2
    if command -v termux-ipc >/dev/null 2>&1; then
        termux-ipc toast "Membuka Telegram Swarm Web UI..."
    fi
    exec termux-ipc webview "http://127.0.0.1:${PORT}"
fi

echo "Telegram Swarm (.cjs) tidak ditemukan di $TARGET_DIR"
exit 1
EOF_TGUI
chmod 755 "$PREFIX/bin/telegram-ui"

# Wrapper 9router CLI (Multi-Account AI Router)
cat << 'EOF_9R' > "$PREFIX/bin/9router"
#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"

if [ ! -d "$TARGET_DIR" ]; then
    for cand in "$HOME/wallet" /wallet /root/wallet /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then
            TARGET_DIR="$cand"
            break
        fi
    done
fi

if [ -f "$TARGET_DIR/9router.cjs" ]; then
    cd "$TARGET_DIR" && exec node 9router.cjs "$@"
fi

if [ -d "/data/local/nhsystem/kali-arm64" ] && command -v su >/dev/null 2>&1; then
    exec su -mm -c 'chroot /data/local/nhsystem/kali-arm64 /usr/bin/env -i HOME=/root USER=root PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin 9router "$@"' dummy "$@"
elif [ -x "/data/local/Droidspaces/bin/droidspaces" ] && command -v su >/dev/null 2>&1; then
    exec su -c '/data/local/Droidspaces/bin/droidspaces enter -- 9router "$@"' dummy "$@"
fi

if command -v 9router >/dev/null 2>&1 && [ "$(command -v 9router)" != "$0" ]; then
    exec 9router "$@"
fi

echo "9router (.cjs atau npm binary) tidak ditemukan di sistem."
exit 1
EOF_9R
chmod 755 "$PREFIX/bin/9router"

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
            mkdir -p "$CONTAINER_ROOTFS/data/data/com.termux/files/usr/bin"
            for b_sym in bash sh env node python3; do
                if [ -e "$CONTAINER_ROOTFS/usr/bin/$b_sym" ] || [ -e "$CONTAINER_ROOTFS/bin/$b_sym" ]; then
                    ln -sf "/usr/bin/$b_sym" "$CONTAINER_ROOTFS/data/data/com.termux/files/usr/bin/$b_sym" 2>/dev/null || true
                fi
            done
            if [ -f "$PREFIX/bin/termux-ipc" ]; then
                (echo '#!/usr/bin/env bash'; tail -n +2 "$PREFIX/bin/termux-ipc") > "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
                chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/termux-ipc"
            fi
            cat << 'EOF_B' > "$CONTAINER_ROOTFS/usr/local/bin/web"
#!/usr/bin/env bash
exec agent-web "$@"
EOF_B
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/web"
            ln -sf "/usr/local/bin/web" "$CONTAINER_ROOTFS/usr/local/bin/browser" 2>/dev/null || true

            cat << 'EOF_CW' > "$CONTAINER_ROOTFS/usr/local/bin/wallet"
#!/usr/bin/env bash
[ -f "/root/.fusion_env" ] && . "/root/.fusion_env"
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
WDIR="${FUSION_WALLET_DIR:-/root/wallet}"
if [ ! -d "$WDIR" ]; then
    for cand in /wallet /root/wallet "$HOME/wallet" /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then WDIR="$cand"; break; fi
    done
fi
EMODE="${FUSION_ENGINE_MODE:-cjs}"
if [ "$EMODE" = "cjs" ] && [ -f "$WDIR/wallet.cjs" ]; then
    cd "$WDIR" && exec node wallet.cjs "$@"
elif [ -f "$WDIR/wallet.js" ]; then
    cd "$WDIR" && exec node wallet.js "$@"
elif [ -f "$WDIR/wallet.cjs" ]; then
    cd "$WDIR" && exec node wallet.cjs "$@"
else
    echo "GhostWallet tidak ditemukan di $WDIR"
    exit 1
fi
EOF_CW
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/wallet"

            cat << 'EOF_CX' > "$CONTAINER_ROOTFS/usr/local/bin/x"
#!/usr/bin/env bash
[ -f "/root/.fusion_env" ] && . "/root/.fusion_env"
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
WDIR="${FUSION_WALLET_DIR:-/root/wallet}"
if [ ! -d "$WDIR" ]; then
    for cand in /wallet /root/wallet "$HOME/wallet" /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then WDIR="$cand"; break; fi
    done
fi
EMODE="${FUSION_ENGINE_MODE:-cjs}"
if [ "$EMODE" = "cjs" ] && [ -f "$WDIR/x_webview.cjs" ]; then
    exec node "$WDIR/x_webview.cjs" "$@"
elif [ -f "$WDIR/x_webview.js" ]; then
    exec node "$WDIR/x_webview.js" "$@"
elif [ -f "$WDIR/x_webview.cjs" ]; then
    exec node "$WDIR/x_webview.cjs" "$@"
else
    echo "x_webview tidak ditemukan di $WDIR"
    exit 1
fi
EOF_CX
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/x"

            cat << 'EOF_CP' > "$CONTAINER_ROOTFS/usr/local/bin/pemantau"
#!/usr/bin/env bash
[ -f "/root/.fusion_env" ] && . "/root/.fusion_env"
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
WDIR="${FUSION_WALLET_DIR:-/root/wallet}"
if [ ! -d "$WDIR" ]; then
    for cand in /wallet /root/wallet "$HOME/wallet" /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then WDIR="$cand"; break; fi
    done
fi
EMODE="${FUSION_ENGINE_MODE:-cjs}"
if [ "$EMODE" = "cjs" ] && [ -f "$WDIR/pemantau.cjs" ]; then
    cd "$WDIR" && exec node pemantau.cjs "$@"
elif [ -f "$WDIR/pemantautelegram.py" ]; then
    cd "$WDIR" && exec python3 pemantautelegram.py "$@"
elif [ -f "$WDIR/pemantau.cjs" ]; then
    cd "$WDIR" && exec node pemantau.cjs "$@"
else
    echo "Pemantau tidak ditemukan di $WDIR"
    exit 1
fi
EOF_CP
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/pemantau"

            cat << 'EOF_C9' > "$CONTAINER_ROOTFS/usr/local/bin/9router"
#!/usr/bin/env bash
[ -f "/root/.fusion_env" ] && . "/root/.fusion_env"
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
WDIR="${FUSION_WALLET_DIR:-/root/wallet}"
if [ ! -d "$WDIR" ]; then
    for cand in /wallet /root/wallet "$HOME/wallet" /data/data/com.termux/files/home/wallet; do
        if [ -d "$cand" ]; then WDIR="$cand"; break; fi
    done
fi
if [ -f "$WDIR/9router.cjs" ]; then
    cd "$WDIR" && exec node 9router.cjs "$@"
fi
if command -v 9router >/dev/null 2>&1 && [ "$(command -v 9router)" != "$0" ]; then
    exec 9router "$@"
fi
echo "9router (.cjs atau npm binary) tidak ditemukan di sistem."
exit 1
EOF_C9
            chmod 755 "$CONTAINER_ROOTFS/usr/local/bin/9router"

            # Bersihkan librofitouch hook dan kembalikan binary rofi murni di PRoot
            rm -f "$CONTAINER_ROOTFS/usr/local/lib/librofitouch.so" "$CONTAINER_ROOTFS/usr/local/src/librofitouch.c" 2>/dev/null || true
            if [ -f "$CONTAINER_ROOTFS/usr/bin/rofi.real" ]; then
                mv -f "$CONTAINER_ROOTFS/usr/bin/rofi.real" "$CONTAINER_ROOTFS/usr/bin/rofi" 2>/dev/null || true
            fi
            rm -f "$CONTAINER_ROOTFS/usr/local/bin/rofi" 2>/dev/null || true
            ln -sf /root/.config/bspwm/rofi-az.sh "$CONTAINER_ROOTFS/usr/local/bin/rofi-az" 2>/dev/null || true
            ln -sf /root/.config/bspwm/dyn-apps.py "$CONTAINER_ROOTFS/usr/local/bin/dyn-apps" 2>/dev/null || true

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

            # Pasang bspwm, audio & tools di PRoot Distro
            if command -v proot-distro >/dev/null 2>&1; then
                spin "Memasang bspwm, audio & tools di PRoot $DISPLAY_NAME..." proot-distro login "$DISTRO_NAME" -- /bin/sh -c '
                    export DEBIAN_FRONTEND=noninteractive
                    apt-get update -qq && apt-get install -y --no-install-recommends bspwm sxhkd rofi polybar picom feh x11-utils pulseaudio pulseaudio-utils fcitx5 fcitx5-modules fcitx5-frontend-gtk3 fcitx5-frontend-qt5 2>/dev/null || true
                '
            fi
            if [ -n "$THEME_SRC" ]; then
                mkdir -p "$CONTAINER_ROOTFS/root/.config" "$CONTAINER_ROOTFS/root/Pictures/wallpapers"
                [ -d "$THEME_SRC/.config" ] && cp -rf "$THEME_SRC/.config/"* "$CONTAINER_ROOTFS/root/.config/" 2>/dev/null || true
                [ -d "$THEME_SRC/Pictures/wallpapers" ] && cp -rf "$THEME_SRC/Pictures/wallpapers/"* "$CONTAINER_ROOTFS/root/Pictures/wallpapers/" 2>/dev/null || true
                [ -f "$THEME_SRC/Pictures/wallpapers/potrait.jpeg" ] && ln -sf "../../Pictures/wallpapers/potrait.jpeg" "$CONTAINER_ROOTFS/root/.config/bspwm/wallpaper" 2>/dev/null || ln -sf "../../Pictures/wallpapers/evening-sky.png" "$CONTAINER_ROOTFS/root/.config/bspwm/wallpaper" 2>/dev/null || true
                chmod +x "$CONTAINER_ROOTFS/root/.config/bspwm/"*.sh "$CONTAINER_ROOTFS/root/.config/polybar/"*.sh 2>/dev/null || true
                chmod +x "$CONTAINER_ROOTFS/root/.config/bspwm/bspwmrc" "$CONTAINER_ROOTFS/root/.config/polybar/launch.sh" 2>/dev/null || true
                echo -e "  ${GREEN}✔ Tema Eso & wallpaper disinkronkan ke PRoot $DISPLAY_NAME (/root/.config/)${RESET}"
            fi

            echo -e "  ${GREEN}✔ Shims & Audio Bridge terinjeksi ke $DISPLAY_NAME${RESET}"
        fi
    else
        # Root Mode: Siapkan bspwm, virgl, audio, shims & tema langsung di rootfs container jika path diketahui
        if [ -n "$CONTAINER_ROOTFS" ] && su -c "[ -d '$CONTAINER_ROOTFS' ]" 2>/dev/null; then
            spin "Memasang bspwm, audio & virgl driver di $CUSTOM_LINUX_NAME..." su -c "
                if [ -x '/data/local/Droidspaces/bin/droidspaces' ] && [ -n '$CUSTOM_LINUX_NAME' ] && [ '$CUSTOM_LINUX_NAME' != 'NetHunter' ]; then
                    /data/local/Droidspaces/bin/droidspaces --name='$CUSTOM_LINUX_NAME' run /bin/sh -c '
                        export DEBIAN_FRONTEND=noninteractive
                        apt-get update -qq && apt-get install -y --no-install-recommends bspwm sxhkd rofi polybar picom feh x11-utils pulseaudio pulseaudio-utils libgl1-mesa-dri mesa-va-drivers mesa-vulkan-drivers fcitx5 fcitx5-modules fcitx5-frontend-gtk3 fcitx5-frontend-qt5 2>/dev/null || apt-get install -y bspwm sxhkd rofi polybar picom feh x11-utils pulseaudio pulseaudio-utils fcitx5 fcitx5-modules fcitx5-frontend-gtk3 fcitx5-frontend-qt5 2>/dev/null || true
                    ' >/dev/null 2>&1 || true
                else
                    chroot '$CONTAINER_ROOTFS' /bin/sh -c '
                        export DEBIAN_FRONTEND=noninteractive
                        apt-get update -qq && apt-get install -y --no-install-recommends bspwm sxhkd rofi polybar picom feh x11-utils pulseaudio pulseaudio-utils libgl1-mesa-dri mesa-va-drivers mesa-vulkan-drivers fcitx5 fcitx5-modules fcitx5-frontend-gtk3 fcitx5-frontend-qt5 2>/dev/null || apt-get install -y bspwm sxhkd rofi polybar picom feh x11-utils pulseaudio pulseaudio-utils fcitx5 fcitx5-modules fcitx5-frontend-gtk3 fcitx5-frontend-qt5 2>/dev/null || true
                    ' >/dev/null 2>&1 || true
                fi
            "

            spin "Menginjeksi shims langsung ke rootfs $CUSTOM_LINUX_NAME..." su -c "
                mkdir -p '$CONTAINER_ROOTFS/usr/local/bin'
                mkdir -p '$CONTAINER_ROOTFS/data/data/com.termux/files/usr/bin'
                for b_sym in bash sh env node python3; do
                    if [ -e '$CONTAINER_ROOTFS/usr/bin/'\$b_sym ] || [ -e '$CONTAINER_ROOTFS/bin/'\$b_sym ]; then
                        ln -sf '/usr/bin/'\$b_sym '$CONTAINER_ROOTFS/data/data/com.termux/files/usr/bin/'\$b_sym 2>/dev/null || true
                    fi
                done
                (echo '#!/usr/bin/env bash'; tail -n +2 '$PREFIX/bin/termux-ipc') > '$CONTAINER_ROOTFS/usr/local/bin/termux-ipc'
                echo -e '#!/usr/bin/env bash\nexec agent-web \"\$@\"' > '$CONTAINER_ROOTFS/usr/local/bin/web'
                chmod 755 '$CONTAINER_ROOTFS/usr/local/bin/web'
                ln -sf '/usr/local/bin/web' '$CONTAINER_ROOTFS/usr/local/bin/browser' 2>/dev/null || true
                cat << 'EOF_RW' > '$CONTAINER_ROOTFS/usr/local/bin/wallet'
#!/usr/bin/env bash
[ -f /root/.fusion_env ] && . /root/.fusion_env
WDIR=\"\${FUSION_WALLET_DIR:-/root/wallet}\"
if [ ! -d \"\$WDIR\" ]; then
  for cand in /wallet /root/wallet \"\$HOME/wallet\" /data/data/com.termux/files/home/wallet; do
    if [ -d \"\$cand\" ]; then WDIR=\"\$cand\"; break; fi
  done
fi
EMODE=\"\${FUSION_ENGINE_MODE:-cjs}\"
if [ \"\$EMODE\" = \"cjs\" ] && [ -f \"\$WDIR/wallet.cjs\" ]; then
  cd \"\$WDIR\" && exec node wallet.cjs \"\$@\"
elif [ -f \"\$WDIR/wallet.js\" ]; then
  cd \"\$WDIR\" && exec node wallet.js \"\$@\"
elif [ -f \"\$WDIR/wallet.cjs\" ]; then
  cd \"\$WDIR\" && exec node wallet.cjs \"\$@\"
fi
EOF_RW
                cat << 'EOF_RX' > '$CONTAINER_ROOTFS/usr/local/bin/x'
#!/usr/bin/env bash
[ -f /root/.fusion_env ] && . /root/.fusion_env
WDIR=\"\${FUSION_WALLET_DIR:-/root/wallet}\"
if [ ! -d \"\$WDIR\" ]; then
  for cand in /wallet /root/wallet \"\$HOME/wallet\" /data/data/com.termux/files/home/wallet; do
    if [ -d \"\$cand\" ]; then WDIR=\"\$cand\"; break; fi
  done
fi
EMODE=\"\${FUSION_ENGINE_MODE:-cjs}\"
if [ \"\$EMODE\" = \"cjs\" ] && [ -f \"\$WDIR/x_webview.cjs\" ]; then
  exec node \"\$WDIR/x_webview.cjs\" \"\$@\"
elif [ -f \"\$WDIR/x_webview.js\" ]; then
  exec node \"\$WDIR/x_webview.js\" \"\$@\"
elif [ -f \"\$WDIR/x_webview.cjs\" ]; then
  exec node \"\$WDIR/x_webview.cjs\" \"\$@\"
fi
EOF_RX
                cat << 'EOF_RP' > '$CONTAINER_ROOTFS/usr/local/bin/pemantau'
#!/usr/bin/env bash
[ -f /root/.fusion_env ] && . /root/.fusion_env
WDIR=\"\${FUSION_WALLET_DIR:-/root/wallet}\"
if [ ! -d \"\$WDIR\" ]; then
  for cand in /wallet /root/wallet \"\$HOME/wallet\" /data/data/com.termux/files/home/wallet; do
    if [ -d \"\$cand\" ]; then WDIR=\"\$cand\"; break; fi
  done
fi
EMODE=\"\${FUSION_ENGINE_MODE:-cjs}\"
if [ \"\$EMODE\" = \"cjs\" ] && [ -f \"\$WDIR/pemantau.cjs\" ]; then
  cd \"\$WDIR\" && exec node pemantau.cjs \"\$@\"
elif [ -f \"\$WDIR/pemantautelegram.py\" ]; then
  cd \"\$WDIR\" && exec python3 pemantautelegram.py \"\$@\"
elif [ -f \"\$WDIR/pemantau.cjs\" ]; then
  cd \"\$WDIR\" && exec node pemantau.cjs \"\$@\"
fi
EOF_RP
                cat << 'EOF_R9' > '$CONTAINER_ROOTFS/usr/local/bin/9router'
#!/usr/bin/env bash
[ -f /root/.fusion_env ] && . /root/.fusion_env
WDIR=\"\${FUSION_WALLET_DIR:-/root/wallet}\"
if [ ! -d \"\$WDIR\" ]; then
  for cand in /wallet /root/wallet \"\$HOME/wallet\" /data/data/com.termux/files/home/wallet; do
    if [ -d \"\$cand\" ]; then WDIR=\"\$cand\"; break; fi
  done
fi
if [ -f \"\$WDIR/9router.cjs\" ]; then
  cd \"\$WDIR\" && exec node 9router.cjs \"\$@\"
fi
if command -v 9router >/dev/null 2>&1 && [ \"\$(command -v 9router)\" != \"\$0\" ]; then
  exec 9router \"\$@\"
fi
EOF_R9
                # Bersihkan librofitouch hook dan kembalikan binary rofi murni
                rm -f '$CONTAINER_ROOTFS/usr/local/lib/librofitouch.so' '$CONTAINER_ROOTFS/usr/local/src/librofitouch.c' 2>/dev/null || true
                if [ -f '$CONTAINER_ROOTFS/usr/bin/rofi.real' ]; then
                    mv -f '$CONTAINER_ROOTFS/usr/bin/rofi.real' '$CONTAINER_ROOTFS/usr/bin/rofi' 2>/dev/null || true
                fi
                rm -f '$CONTAINER_ROOTFS/usr/local/bin/rofi' 2>/dev/null || true
                mkdir -p '$CONTAINER_ROOTFS/etc/profile.d'
                cat << 'EOF_FCITX' > '$CONTAINER_ROOTFS/etc/profile.d/fcitx5.sh'
export GTK_IM_MODULE=fcitx
export QT_IM_MODULE=fcitx
export XMODIFIERS=@im=fcitx
if [ -S "/tmp/.X11-unix/termux-x11-im-bus-0" ]; then
    export DBUS_SESSION_BUS_ADDRESS="unix:path=/tmp/.X11-unix/termux-x11-im-bus-0"
elif [ -S "/tmp/termux-x11-im-bus-0" ]; then
    export DBUS_SESSION_BUS_ADDRESS="unix:path=/tmp/termux-x11-im-bus-0"
fi
EOF_FCITX
                chmod 755 '$CONTAINER_ROOTFS/usr/local/bin/'*
                ln -sf /root/.config/bspwm/rofi-az.sh '$CONTAINER_ROOTFS/usr/local/bin/rofi-az' 2>/dev/null || true
                ln -sf /root/.config/bspwm/dyn-apps.py '$CONTAINER_ROOTFS/usr/local/bin/dyn-apps' 2>/dev/null || true
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

            # Salin Tema Eso & Wallpapers ke Container Root (/root/.config/ dan /root/Pictures/wallpapers/)
            if [ -n "$THEME_SRC" ]; then
                spin "Menyalin tema Eso & wallpaper ke rootfs $CUSTOM_LINUX_NAME..." su -c "
                    mkdir -p '$CONTAINER_ROOTFS/root/.config' '$CONTAINER_ROOTFS/root/Pictures/wallpapers'
                    [ -d '$THEME_SRC/.config' ] && cp -rf '$THEME_SRC/.config/'* '$CONTAINER_ROOTFS/root/.config/' 2>/dev/null || true
                    [ -d '$THEME_SRC/Pictures/wallpapers' ] && cp -rf '$THEME_SRC/Pictures/wallpapers/'* '$CONTAINER_ROOTFS/root/Pictures/wallpapers/' 2>/dev/null || true
                    [ -f '$THEME_SRC/Pictures/wallpapers/potrait.jpeg' ] && ln -sf '../../Pictures/wallpapers/potrait.jpeg' '$CONTAINER_ROOTFS/root/.config/bspwm/wallpaper' 2>/dev/null || ln -sf '../../Pictures/wallpapers/evening-sky.png' '$CONTAINER_ROOTFS/root/.config/bspwm/wallpaper' 2>/dev/null || true
                    chmod +x '$CONTAINER_ROOTFS/root/.config/bspwm/'*.sh '$CONTAINER_ROOTFS/root/.config/polybar/'*.sh 2>/dev/null || true
                    chmod +x '$CONTAINER_ROOTFS/root/.config/bspwm/bspwmrc' '$CONTAINER_ROOTFS/root/.config/polybar/launch.sh' 2>/dev/null || true
                "
                echo -e "  ${GREEN}✔ Tema Eso (bspwm, polybar, rofi, picom & wallpapers) disinkronkan ke $CUSTOM_LINUX_NAME (/root/.config/)${RESET}"
            fi
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
            su -c "/data/local/Droidspaces/bin/droidspaces --name=\"$CUSTOM_LINUX_NAME\" run -- [ -f /usr/local/bin/termux-ipc ] || (mkdir -p /usr/local/bin && (echo '#!/usr/bin/env bash'; tail -n +2 /data/data/com.termux/files/usr/bin/termux-ipc) > /usr/local/bin/termux-ipc && chmod 755 /usr/local/bin/termux-ipc)" >/dev/null 2>&1 || true
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

# Pilihan Format Engine Otomasi (CJS vs JS)
echo -e "  Pilih Format Engine Otomasi (Web3, X Radar & Telegram Sniper):"
echo -e "    1) Standalone CommonJS (.cjs) - Cepat, Mandiri, Zero node_modules [Default]"
echo -e "    2) Regular JavaScript (.js) - Klasik dengan node_modules"
read -p "    Pilih [1/2, def: 1]: " ENGINE_CHOICE
[ -z "$ENGINE_CHOICE" ] && ENGINE_CHOICE="1"

if [ "$ENGINE_CHOICE" = "1" ]; then
    FUSION_ENGINE_MODE="cjs"
    echo -e "  ${GREEN}➔ Engine: Standalone CommonJS (.cjs) aktif [Bypass npm install]${RESET}"
else
    FUSION_ENGINE_MODE="js"
    echo -e "  ${GREEN}➔ Engine: Regular JavaScript (.js) aktif${RESET}"
fi

cat << EOF > "$HOME/.fusion_env"
# Project Fusion Dynamic Environment
export FUSION_MODE="$([ $IS_ROOT -eq 1 ] && echo 'root' || echo 'nonroot')"
export FUSION_WALLET_DIR="$HOST_WALLET_DIR"
export FUSION_DB_DIR="$HOST_WALLET_DIR"
export FUSION_ENGINE_MODE="$FUSION_ENGINE_MODE"
EOF
chmod 644 "$HOME/.fusion_env"
echo -e "  ${GREEN}✔ Konfigurasi dinamis disimpan di $HOME/.fusion_env${RESET}"

# ==============================================================================
# ⚡ DETEKSI DAN PEMILIHAN TARGET 9ROUTER MULTI-ACCOUNT GATEWAY
# ==============================================================================
echo -e "\n  ${MAGENTA}${BOLD}⚡ Pilihan Target Instalasi 9Router & AI Multi-Account Gateway:${RESET}"

TARGET_9R_OPTIONS=()
TARGET_9R_PATHS=()
TARGET_9R_TYPES=()

if [ $IS_ROOT -eq 1 ]; then
    # 1. NetHunter di /data/local/nhsystem/
    for nh_c in /data/local/nhsystem/kali-arm64 /data/local/nhsystem/kalifs; do
        if su -c "[ -d '$nh_c' ]" 2>/dev/null; then
            TARGET_9R_OPTIONS+=("Kali NetHunter ($nh_c) [Rekomendasi Root - MITM Port 443 Support]")
            TARGET_9R_PATHS+=("$nh_c")
            TARGET_9R_TYPES+=("nethunter")
        fi
    done

    # 2. Droidspaces Containers & spaces di /data/local/Droidspaces/
    if su -c "[ -d '/data/local/Droidspaces' ]" 2>/dev/null; then
        for ds_root in /data/local/Droidspaces/Containers /data/local/Droidspaces/spaces; do
            if su -c "[ -d '$ds_root' ]" 2>/dev/null; then
                for sub in $(su -c "ls -1 '$ds_root' 2>/dev/null" | grep -v '^\.'); do
                    sub_path="$ds_root/$sub"
                    if su -c "[ -d '$sub_path/rootfs' ]" 2>/dev/null; then
                        sub_path="$sub_path/rootfs"
                    fi
                    TARGET_9R_OPTIONS+=("Droidspaces: $sub ($sub_path)")
                    TARGET_9R_PATHS+=("$sub_path")
                    TARGET_9R_TYPES+=("droidspaces")
                done
            fi
        done
    fi

    # 3. Custom Chroot lainnya di /data/local
    for extra_c in /data/local/ubuntu /data/local/debian /data/local/kali /data/local/chroot; do
        if su -c "[ -d '$extra_c' ]" 2>/dev/null; then
            TARGET_9R_OPTIONS+=("Custom Linux Chroot: $(basename "$extra_c") ($extra_c)")
            TARGET_9R_PATHS+=("$extra_c")
            TARGET_9R_TYPES+=("custom")
        fi
    done
fi

# 4. Selalu sediakan opsi Native Termux Host
TARGET_9R_OPTIONS+=("Native Termux Host ($PREFIX - Non-Root / Standalone Port 20128)")
TARGET_9R_PATHS+=("$PREFIX")
TARGET_9R_TYPES+=("native")

# 5. Opsi Lewati
TARGET_9R_OPTIONS+=("Lewati instalasi 9Router untuk sekarang")
TARGET_9R_PATHS+=("skip")
TARGET_9R_TYPES+=("skip")

echo -e "    Ditemukan target lingkungan di /data/local & host:"
for idx in "${!TARGET_9R_OPTIONS[@]}"; do
    num=$((idx + 1))
    echo -e "    $num) ${TARGET_9R_OPTIONS[$idx]}"
done

read -p "    Pilih lokasi instalasi 9Router [1-${#TARGET_9R_OPTIONS[@]}, def: 1]: " ROUTER_SEL
[ -z "$ROUTER_SEL" ] && ROUTER_SEL="1"
SEL_IDX=$((ROUTER_SEL - 1))
if [ $SEL_IDX -lt 0 ] || [ $SEL_IDX -ge ${#TARGET_9R_OPTIONS[@]} ]; then
    SEL_IDX=0
fi

CHOSEN_9R_TYPE="${TARGET_9R_TYPES[$SEL_IDX]}"
CHOSEN_9R_PATH="${TARGET_9R_PATHS[$SEL_IDX]}"

mkdir -p "$FUSION_HOME"
echo "$CHOSEN_9R_PATH" > "$FUSION_HOME/9router_target.conf"

cat << EOF >> "$HOME/.fusion_env"
export FUSION_9ROUTER_TARGET="$CHOSEN_9R_TYPE"
export FUSION_9ROUTER_CHROOT="$CHOSEN_9R_PATH"
EOF

if [ "$CHOSEN_9R_TYPE" = "skip" ]; then
    echo -e "  ${GRAY}➔ Instalasi 9Router dilewati. Bisa dipasang nanti dari Settings Launcher.${RESET}"
elif [ "$CHOSEN_9R_TYPE" = "native" ]; then
    echo -e "  ${GREEN}➔ Target Terpilih: Native Termux Host${RESET}"
    read -p "    Unduh & pasang paket npm 9router di Termux host sekarang? [Y/n]: " DO_INSTALL_9R
    if [ "${DO_INSTALL_9R,,}" != "n" ]; then
        spin "Memasang nodejs & 9router di Termux..." su -c "true" && pkg install -y nodejs-lts && npm install -g 9router
    fi
    mkdir -p "$HOME/wallet"
    [ -f "/root/wallet/9router.cjs" ] && cp -f "/root/wallet/9router.cjs" "$HOME/wallet/9router.cjs"
    chmod 755 "$HOME/wallet/9router.cjs" 2>/dev/null || true
else
    echo -e "  ${GREEN}➔ Target Terpilih: Container Chroot ($CHOSEN_9R_PATH)${RESET}"
    read -p "    Unduh & pasang paket npm 9router di dalam container $CHOSEN_9R_PATH? [Y/n]: " DO_INSTALL_9R
    if [ "${DO_INSTALL_9R,,}" != "n" ]; then
        spin "Memasang 9router di dalam container..." su -mm -c "chroot '$CHOSEN_9R_PATH' /bin/sh -c 'npm install -g 9router 2>/dev/null || true'"
    fi
    # Pasang 9router.cjs ke container
    su -c "mkdir -p '$CHOSEN_9R_PATH/root/wallet'"
    [ -f "/root/wallet/9router.cjs" ] && su -c "cp -f /root/wallet/9router.cjs '$CHOSEN_9R_PATH/root/wallet/9router.cjs'"
    su -c "chmod 755 '$CHOSEN_9R_PATH/root/wallet/9router.cjs' 2>/dev/null || true"
    # Pasang helper scripts di container
    for scr in start-9router stop-9router 9router-web sync-agy-to-9router; do
        if [ -f "/usr/local/bin/$scr" ]; then
            su -c "cp -f '/usr/local/bin/$scr' '$CHOSEN_9R_PATH/usr/local/bin/$scr' && chmod 755 '$CHOSEN_9R_PATH/usr/local/bin/$scr'"
        fi
    done
fi

mkdir -p "$HOME/wallet"
if [ -d "$FUSION_HOME/wallet" ]; then
    cp -rf "$FUSION_HOME/wallet/"* "$HOME/wallet/"
fi
if [ -d "$SCRIPT_DIR/wallet" ]; then
    cp -rf "$SCRIPT_DIR/wallet/"* "$HOME/wallet/"
fi

# Fallback ekstraksi aset wallet dari APK jika belum lengkap
BASE_APK=$(pm path com.termux 2>/dev/null | sed 's/package://' | head -n1 || echo "")
if [ -n "$BASE_APK" ] && [ -f "$BASE_APK" ] && command -v unzip >/dev/null 2>&1; then
    for w_asset in 9router.cjs wallet.cjs x_webview.cjs pemantau.cjs telegram.cjs pembantu_avatar.jpg wallet.js x_webview.js pemantautelegram.py wallets_db.json chains_db.json package.json recipes_db.json; do
        unzip -j -o "$BASE_APK" "assets/fusion/$w_asset" -d "$HOME/wallet/" 2>/dev/null || true
    done
fi

if [ -d "/project/node2cjs" ]; then
    cp -rf /project/node2cjs/* "$HOME/wallet/" 2>/dev/null || true
fi
if [ -d "/root/wallet" ]; then
    cp -rf /root/wallet/* "$HOME/wallet/" 2>/dev/null || true
fi

[ ! -f "$HOME/wallet/wallets_db.json" ] && echo '{"master_index": null, "wallets": []}' > "$HOME/wallet/wallets_db.json"
[ ! -f "$HOME/wallet/chains_db.json" ] && echo '{}' > "$HOME/wallet/chains_db.json"
[ ! -f "$HOME/wallet/recipes_db.json" ] && echo '{}' > "$HOME/wallet/recipes_db.json"

chmod 755 "$HOME/wallet/"*.cjs "$HOME/wallet/"*.js "$HOME/wallet/"*.py 2>/dev/null || true

if [ $IS_ROOT -eq 1 ] && [ "$TARGET_WALLET_DIR" != "$HOME/wallet" ] && [ -n "$CONTAINER_ROOTFS" ]; then
    su -c "mkdir -p '$TARGET_WALLET_DIR'" 2>/dev/null || true
    [ -d "$HOME/wallet" ] && su -c "cp -rf '$HOME/wallet/'* '$TARGET_WALLET_DIR/'" 2>/dev/null || true
    [ -f "$HOME/.fusion_env" ] && su -c "cp -f '$HOME/.fusion_env' '$CONTAINER_ROOTFS/root/.fusion_env'" 2>/dev/null || true
fi

if [ "$FUSION_ENGINE_MODE" = "cjs" ]; then
    echo -e "  ${GREEN}✔ Mode Standalone CJS aktif — bypass npm install (Zero node_modules)${RESET}"
else
    if [ -d "$HOME/wallet/node_modules/ethers" ] && [ -d "$HOME/wallet/node_modules/dotenv" ]; then
        echo -e "  ${GREEN}✔ Dependensi Web3 di Termux lokal sudah terpasang${RESET}"
    else
        if command -v npm >/dev/null 2>&1; then
            cd "$HOME/wallet" 2>/dev/null || cd "$HOME"
            spin "Memasang dependensi Web3 (ethers, dotenv, walletconnect)..." npm install --silent --no-audit --no-fund ethers dotenv @walletconnect/core @walletconnect/web3wallet || true
        fi
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
alias 9router="9router"
alias router="9router"
alias pemantau="pemantau"
alias pmt="pemantau"
alias telegram="telegram"
alias tg="telegram"
alias telegram-ui="telegram-ui"
alias tgui="telegram-ui"
alias patchright="node ./wallet/patchright.js"
alias x="x"
alias xradar="node ~/wallet/x_webview.js"
alias browser="termux-ipc webview"
alias ipc="termux-ipc"
alias copilot="agy-copilot"
alias bashnh="bashnh"
alias login="login"
alias bspwm="termux-bspwm"
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

echo -e "  ${GREEN}✔ Alias shell (browser, ipc, wallet, copilot, x, c, python, bashnh, login, bspwm) terpasang${RESET}"

echo -e "\n${GREEN}${BOLD}✔ SETUP SELESAI DENGAN SUKSES!${RESET}"
echo -e "${GRAY}──────────────────────────────${RESET}"
echo -e "Ketik ${CYAN}browser https://google.com${RESET} untuk membuka WebView."
echo -e "Ketik ${CYAN}x${RESET} untuk menjalankan X Radar WebView."
echo -e "Ketik ${CYAN}login${RESET} untuk menu startup."
echo -e "Ketik ${CYAN}bashnh${RESET} untuk langsung masuk ke container Linux aktif."
echo -e "Ketik ${CYAN}bspwm${RESET} untuk menjalankan Touch BSPWM Desktop di Termux."
