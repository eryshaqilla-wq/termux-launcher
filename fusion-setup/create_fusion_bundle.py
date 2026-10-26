#!/usr/bin/env python3
import os, sys, shutil, glob, tarfile, json

BUNDLE_DIR = "/project/clean_build/fusion_bundle"
OUTPUT_TAR = "/project/clean_build/fusion_payload.tar.gz"

if os.path.exists(BUNDLE_DIR):
    shutil.rmtree(BUNDLE_DIR)
os.makedirs(os.path.join(BUNDLE_DIR, "bin"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "wallet"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "configs"), exist_ok=True)

def copy_tree_clean(src, dst):
    if os.path.exists(src):
        if os.path.exists(dst):
            shutil.rmtree(dst)
        shutil.copytree(src, dst, ignore=shutil.ignore_patterns('.git', '.github', 'spec', 'tests', 'docs', 'images', '*.zwc'))

print("--- 1. Menyiapkan Binary & Tools ke bundle/bin ---")
# Cari termux-ipc
ipc_source = None
for candidate in ["/project/termux-launcher/fusion-setup/termux-ipc", "/project/termux-ipc", "/usr/local/bin/termux-ipc"]:
    if os.path.exists(candidate):
        ipc_source = candidate
        break
if ipc_source:
    shutil.copy2(ipc_source, os.path.join(BUNDLE_DIR, "bin/termux-ipc"))

# Salin termux-ipc-bridge (Bridge untuk Droidspaces IPC)
bridge_src = "/project/termux-launcher/fusion-setup/termux-ipc-bridge"
if os.path.exists(bridge_src):
    shutil.copy2(bridge_src, os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge"))
    os.chmod(os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge"), 0o755)
    print("✓ Added termux-ipc-bridge to bundle/bin")

# Salin bashnh (Universal Linux Launcher)
bashnh_src = "/project/termux-launcher/fusion-setup/bashnh"
if not os.path.exists(bashnh_src):
    bashnh_src = "/root/project/bashnh"
if os.path.exists(bashnh_src):
    shutil.copy2(bashnh_src, os.path.join(BUNDLE_DIR, "bin/bashnh"))
    os.chmod(os.path.join(BUNDLE_DIR, "bin/bashnh"), 0o755)
    print("✓ Added universal bashnh to bundle/bin")

# Wrapper browser
with open(os.path.join(BUNDLE_DIR, "bin/browser"), "w") as f:
    f.write("#!/data/data/com.termux/files/usr/bin/bash\nexec termux-ipc webview \"$@\"\n")

# Wrapper x (otomasi X Radar)
with open(os.path.join(BUNDLE_DIR, "bin/x"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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
""")

# Wrapper wallet (Ghost Web3 Wallet Daemon)
with open(os.path.join(BUNDLE_DIR, "bin/wallet"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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
""")

# Wrapper pemantau (Telegram Sniper Engine)
with open(os.path.join(BUNDLE_DIR, "bin/pemantau"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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
    elif [ -f "$TARGET_DIR/telegram.cjs" ]; then
        cd "$TARGET_DIR" && exec node telegram.cjs "$@"
    elif [ -f "$TARGET_DIR/pemantautelegram.py" ]; then
        cd "$TARGET_DIR" && exec python3 pemantautelegram.py "$@"
    fi
else
    if [ -f "$TARGET_DIR/pemantautelegram.py" ]; then
        cd "$TARGET_DIR" && exec python3 pemantautelegram.py "$@"
    elif [ -f "$TARGET_DIR/pemantau.cjs" ]; then
        cd "$TARGET_DIR" && exec node pemantau.cjs "$@"
    elif [ -f "$TARGET_DIR/telegram.cjs" ]; then
        cd "$TARGET_DIR" && exec node telegram.cjs "$@"
    fi
fi
echo "Pemantau (.cjs/.py) tidak ditemukan di $TARGET_DIR"
exit 1
""")

# Wrapper telegram CLI (Telegram Swarm & Bot Orchestrator)
with open(os.path.join(BUNDLE_DIR, "bin/telegram"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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
""")

# Wrapper telegram-ui (Telegram Web UI & Background Server)
with open(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), "w") as f:
    f.write(r"""#!/data/data/com.termux/files/usr/bin/bash
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

# 1. Cek apakah server sudah aktif di port 20130
if curl -s "http://127.0.0.1:${PORT}/status" >/dev/null 2>&1; then
    echo "✨ Sesi Telegram Swarm Server sudah aktif di port ${PORT}."
    echo "✨ Membuka Telegram Webview di layar Android..."
    if command -v termux-ipc >/dev/null 2>&1; then
        termux-ipc toast "Membuka Telegram Swarm Web UI..."
    fi
    exec termux-ipc webview "http://127.0.0.1:${PORT}"
fi

# 2. Matikan sesi terminal lama jika ada
OTHER_PIDS=$(ps -eo pid,args 2>/dev/null | grep -E "(telegram\.cjs|pemantau\.cjs|pemantautelegram\.py)" | grep -v -E "(grep|--ui|tmux)" | awk '{print $1}')
if [ -n "$OTHER_PIDS" ]; then
    echo "Terdeteksi sesi Telegram terminal (PID: $OTHER_PIDS) sedang berjalan."
    echo "Mematikan sesi terminal lama agar tidak bentrok..."
    for p in $OTHER_PIDS; do
        kill -9 "$p" 2>/dev/null || true
    done
    sleep 1.2
fi

# 3. Jalankan server Swarm di latar belakang
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
""")

# Wrapper 9router (Multi-Account AI Gateway)
with open(os.path.join(BUNDLE_DIR, "bin/9router"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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
""")

if os.path.exists("/usr/local/bin/agy-copilot"):
    shutil.copy2("/usr/local/bin/agy-copilot", os.path.join(BUNDLE_DIR, "bin/agy-copilot"))

for b in os.listdir(os.path.join(BUNDLE_DIR, "bin")):
    os.chmod(os.path.join(BUNDLE_DIR, "bin", b), 0o755)

print("--- 2. Menyiapkan Wallet, Sniper & Configs ke bundle/wallet ---")
# 1. Salin Standalone CJS Suite (dari /project/node2cjs/ atau /root/wallet/)
for f_cjs in [
    "wallet.cjs", "x_webview.cjs", "pemantau.cjs", "telegram.cjs", "9router.cjs",
    "query_db.json", "module_db.json",
    "tele_filter_config.json", "webview_profiles.json",
    "ai_selectors.json", "pembantu_avatar.jpg"
]:
    for src_dir in ["/project/node2cjs", "/root/wallet"]:
        src_path = os.path.join(src_dir, f_cjs)
        if os.path.exists(src_path):
            shutil.copy2(src_path, os.path.join(BUNDLE_DIR, "wallet", f_cjs))
            break
print("✓ Added CJS suite (wallet.cjs, x_webview.cjs, pemantau.cjs, telegram.cjs, 9router.cjs) to bundle")

# Copy web/ directory (Telegram Web UI)
for src_web in ["/project/node2cjs/web", "/root/wallet/web"]:
    if os.path.exists(src_web):
        copy_tree_clean(src_web, os.path.join(BUNDLE_DIR, "wallet/web"))
        print("✓ Added Telegram Web UI (web/index.html) to bundle")
        break

# Siapkan direktori tele_data/avatars bersih TANPA database atau riwayat akun Telegram
os.makedirs(os.path.join(BUNDLE_DIR, "wallet/tele_data/avatars"), exist_ok=True)
print("✓ Created clean tele_data/avatars directory (zero telegram.db)")

# 2. Salin Regular JS/Python Suite (dari /root/wallet/ atau /project/node2cjs/)
for f_js in ["wallet.js", "x_webview.js", "pemantautelegram.py", "package.json", "chains_db.json"]:
    for src_candidate in [os.path.join("/root/wallet", f_js), os.path.join("/project/node2cjs", f_js)]:
        if os.path.exists(src_candidate):
            shutil.copy2(src_candidate, os.path.join(BUNDLE_DIR, "wallet", f_js))
            break

for rpc in glob.glob("/root/wallet/config_rpc*.json") + glob.glob("/project/node2cjs/config_rpc*.json"):
    shutil.copy2(rpc, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(rpc)))

for g_conf in glob.glob("/project/node2cjs/gate*.json") + glob.glob("/root/wallet/gate*.json") + glob.glob("/project/node2cjs/www.gate*.json"):
    shutil.copy2(g_conf, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(g_conf)))

# Template bersih & sanitize untuk konfigurasi akun pribadi (MENCEGAH KEBOCORAN AKUN)
clean_tele_config = {
    "api_id": 0,
    "api_hash": "",
    "bot_token": "",
    "target_chat_id": [],
    "whitelist": [],
    "account_name": "",
    "phone": "",
    "bot_name": "Pembantu Tuan Eso"
}
with open(os.path.join(BUNDLE_DIR, "wallet/config_telegram.json"), "w") as f:
    json.dump(clean_tele_config, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/pantau.json"), "w") as f:
    json.dump([], f, indent=2)

clean_ai_config = {
    "baseUrl": "https://api.deepseek.com",
    "apiKey": "",
    "model": "deepseek-flash",
    "browser_mode": "720p",
    "headless_mode": "auto"
}
with open(os.path.join(BUNDLE_DIR, "wallet/ai_config.json"), "w") as f:
    json.dump(clean_ai_config, f, indent=2)

# Dummy template wallets_db.json dan recipes_db.json steril
dummy_wallet = {"master_index": None, "wallets": []}
with open(os.path.join(BUNDLE_DIR, "wallet/wallets_db.json"), "w") as f:
    json.dump(dummy_wallet, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json"), "w") as f:
    f.write("{}\n")

# Guard pembersihan ketat: hapus file-file database dan sesi akun pribadi dari bundle
for root, dirs, files in os.walk(os.path.join(BUNDLE_DIR, "wallet")):
    for file in files:
        if file.endswith(('.db', '.db-wal', '.db-shm', '.session', '.session-journal')) or file in ('tele_session_string.txt', '.env'):
            file_path = os.path.join(root, file)
            os.remove(file_path)
            print(f"⚠ Stripped sensitive file from bundle: {file}")

# Copy login.template, termux.properties, dan setup.sh
login_tmpl = "/project/termux-launcher/fusion-setup/login.template"
if not os.path.exists(login_tmpl):
    login_tmpl = "/root/project/login.template"
if os.path.exists(login_tmpl):
    shutil.copy2(login_tmpl, os.path.join(BUNDLE_DIR, "login.template"))

props = "/project/termux-launcher/fusion-setup/termux.properties"
if not os.path.exists(props):
    props = "/project/clean_build/termux.properties"
if os.path.exists(props):
    shutil.copy2(props, os.path.join(BUNDLE_DIR, "termux.properties"))

setup_src = "/project/termux-launcher/fusion-setup/setup.sh"
shutil.copy2(setup_src, os.path.join(BUNDLE_DIR, "setup.sh"))
os.chmod(os.path.join(BUNDLE_DIR, "setup.sh"), 0o755)

print("--- 2c. Menyiapkan Plugin Zsh & Powerlevel10k Offline ---")
zsh_plugin_dir = os.path.join(BUNDLE_DIR, "zsh_plugins")
zsh_theme_dir = os.path.join(BUNDLE_DIR, "zsh_themes")
os.makedirs(zsh_plugin_dir, exist_ok=True)
os.makedirs(zsh_theme_dir, exist_ok=True)

def copy_tree_clean(src, dst):
    if os.path.exists(src):
        if os.path.exists(dst):
            shutil.rmtree(dst)
        shutil.copytree(src, dst, ignore=shutil.ignore_patterns('.git', '.github', 'spec', 'tests', 'docs', 'images', '*.zwc'))

# Plugins: Autosuggestions & Syntax Highlighting
copy_tree_clean("/root/.oh-my-zsh/custom/plugins/zsh-autosuggestions", os.path.join(zsh_plugin_dir, "zsh-autosuggestions"))
copy_tree_clean("/root/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting", os.path.join(zsh_plugin_dir, "zsh-syntax-highlighting"))

# Theme: Powerlevel10k
p10k_src = "/root/.oh-my-zsh/custom/themes/powerlevel10k"
if os.path.exists(p10k_src):
    copy_tree_clean(p10k_src, os.path.join(zsh_theme_dir, "powerlevel10k"))
    print("✓ Added Powerlevel10k theme to bundle")

# Configs: .p10k.zsh & zshrc.template
p10k_conf = "/project/termux-launcher/fusion-setup/p10k.zsh"
if not os.path.exists(p10k_conf):
    p10k_conf = "/root/.p10k.zsh"
if os.path.exists(p10k_conf):
    shutil.copy2(p10k_conf, os.path.join(BUNDLE_DIR, "configs/.p10k.zsh"))

zshrc_tmpl = "/project/termux-launcher/fusion-setup/zshrc.template"
if os.path.exists(zshrc_tmpl):
    shutil.copy2(zshrc_tmpl, os.path.join(BUNDLE_DIR, "configs/.zshrc"))

print("✓ Added full zsh offline plugins, theme & configs to bundle")

print("--- 2d. Menyiapkan Tema Eso (bspwm, polybar, rofi, picom, dunst, wallpapers) ke bundle ---")
theme_src = "/project/termux-launcher/fusion-setup/theme"
if os.path.exists(theme_src):
    copy_tree_clean(theme_src, os.path.join(BUNDLE_DIR, "theme"))
    print("✓ Added touch bspwm theme & wallpapers to bundle")

print("--- 3. Mengemas menjadi fusion_payload.tar.gz ---")
with tarfile.open(OUTPUT_TAR, "w:gz") as tar:
    for item in os.listdir(BUNDLE_DIR):
        item_path = os.path.join(BUNDLE_DIR, item)
        tar.add(item_path, arcname=item)

print(f"✓ Payload berhasil dibuat: {OUTPUT_TAR} ({os.path.getsize(OUTPUT_TAR)} bytes)")

# Encrypt payload with AES-256-CBC PBKDF2 using secure dynamic PIN
PIN = os.environ.get("FUSION_PIN")
if not PIN and os.path.exists("/root/.fusion_pin"):
    try:
        with open("/root/.fusion_pin", "r") as pf:
            PIN = pf.read().strip()
    except Exception:
        pass

if not PIN:
    import getpass
    PIN = getpass.getpass("Masukkan PIN untuk mengenkripsi payload: ")

if not PIN:
    print("Error: PIN enkripsi tidak boleh kosong!")
    sys.exit(1)

OUTPUT_ENC = OUTPUT_TAR + ".enc"
print(f"--- 3b. Mengenkripsi payload dengan AES-256-CBC ---")
import subprocess
res = subprocess.run([
    "openssl", "enc", "-aes-256-cbc", "-pbkdf2", "-iter", "100000",
    "-pass", f"pass:{PIN}",
    "-in", OUTPUT_TAR,
    "-out", OUTPUT_ENC
], capture_output=True)
if res.returncode != 0:
    print(f"Error encrypting payload: {res.stderr.decode()}")
    sys.exit(1)
print(f"✓ Payload terenkripsi berhasil dibuat: {OUTPUT_ENC} ({os.path.getsize(OUTPUT_ENC)} bytes)")

# Salin ke lokasi assets aplikasi Android
ASSETS_DIR = "/project/termux-launcher/app/src/main/assets/fusion"
FUSION_SETUP_DIR = "/project/termux-launcher/fusion-setup"
os.makedirs(ASSETS_DIR, exist_ok=True)

# 1. Salin payload terenkripsi (.enc)
shutil.copy2(OUTPUT_ENC, os.path.join(ASSETS_DIR, "fusion_payload.tar.gz.enc"))
shutil.copy2(OUTPUT_ENC, os.path.join(FUSION_SETUP_DIR, "fusion_payload.tar.gz.enc"))

# Hapus payload mentah unencrypted agar tidak ter-commit ke GitHub
for target in [
    os.path.join(ASSETS_DIR, "fusion_payload.tar.gz"),
    os.path.join(FUSION_SETUP_DIR, "fusion_payload.tar.gz"),
    os.path.join(ASSETS_DIR, "wallet.js"),
    os.path.join(ASSETS_DIR, "wallet.cjs"),
    os.path.join(ASSETS_DIR, "x_webview.js"),
    os.path.join(ASSETS_DIR, "x_webview.cjs"),
    os.path.join(ASSETS_DIR, "pemantau.cjs"),
    os.path.join(ASSETS_DIR, "telegram.cjs"),
    os.path.join(ASSETS_DIR, "9router.cjs"),
    os.path.join(ASSETS_DIR, "pemantautelegram.py"),
    os.path.join(ASSETS_DIR, "wallets_db.json"),
    os.path.join(ASSETS_DIR, "chains_db.json"),
    os.path.join(ASSETS_DIR, "package.json"),
    os.path.join(ASSETS_DIR, "recipes_db.json"),
]:
    if os.path.exists(target):
        os.remove(target)

# 2. Standalone files di assets yang aman (installer & CLI runner umum)
shutil.copy2(os.path.join(BUNDLE_DIR, "setup.sh"), os.path.join(ASSETS_DIR, "setup.sh"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/termux-ipc"), os.path.join(ASSETS_DIR, "termux-ipc"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge"), os.path.join(ASSETS_DIR, "termux-ipc-bridge"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/browser"), os.path.join(ASSETS_DIR, "browser"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/x"), os.path.join(ASSETS_DIR, "x"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/x"), os.path.join(FUSION_SETUP_DIR, "x"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/bashnh")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/bashnh"), os.path.join(ASSETS_DIR, "bashnh"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/wallet")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet"), os.path.join(ASSETS_DIR, "wallet"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet"), os.path.join(FUSION_SETUP_DIR, "wallet"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/pemantau")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/pemantau"), os.path.join(ASSETS_DIR, "pemantau"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/pemantau"), os.path.join(FUSION_SETUP_DIR, "pemantau"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/telegram")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram"), os.path.join(ASSETS_DIR, "telegram"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram"), os.path.join(FUSION_SETUP_DIR, "telegram"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/telegram-ui")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), os.path.join(ASSETS_DIR, "telegram-ui"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), os.path.join(FUSION_SETUP_DIR, "telegram-ui"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/9router")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/9router"), os.path.join(ASSETS_DIR, "9router"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/9router"), os.path.join(FUSION_SETUP_DIR, "9router"))
if os.path.exists(os.path.join(BUNDLE_DIR, "termux.properties")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "termux.properties"), os.path.join(ASSETS_DIR, "termux.properties"))

print(f"✓ Berhasil menyinkronkan seluruh aset terlindungi ke {ASSETS_DIR}")

