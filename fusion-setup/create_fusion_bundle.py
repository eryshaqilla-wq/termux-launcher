#!/usr/bin/env python3
import os, sys, shutil, glob, tarfile, json

BUNDLE_DIR = "/project/clean_build/fusion_bundle"
OUTPUT_TAR = "/project/clean_build/fusion_payload.tar.gz"

if os.path.exists(BUNDLE_DIR):
    shutil.rmtree(BUNDLE_DIR)
os.makedirs(os.path.join(BUNDLE_DIR, "bin"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "wallet"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "configs"), exist_ok=True)

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
TARGET_X="${FUSION_WALLET_DIR:-$HOME/wallet}/x_webview.js"
if [ ! -f "$TARGET_X" ]; then
    if [ -f "$HOME/wallet/x_webview.js" ]; then
        TARGET_X="$HOME/wallet/x_webview.js"
    elif [ -f "/root/wallet/x_webview.js" ]; then
        TARGET_X="/root/wallet/x_webview.js"
    fi
fi
if [ -f "$TARGET_X" ]; then
    exec node "$TARGET_X" "$@"
else
    echo "x_webview.js tidak ditemukan di $TARGET_X"
    exit 1
fi
""")

# Wrapper wallet (Ghost Web3 Wallet Daemon)
with open(os.path.join(BUNDLE_DIR, "bin/wallet"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
[ -f "$HOME/.fusion_env" ] && . "$HOME/.fusion_env"
TARGET_DIR="${FUSION_WALLET_DIR:-$HOME/wallet}"
if [ ! -f "$TARGET_DIR/wallet.js" ]; then
    if [ -f "$HOME/wallet/wallet.js" ]; then
        TARGET_DIR="$HOME/wallet"
    elif [ -f "/root/wallet/wallet.js" ]; then
        TARGET_DIR="/root/wallet"
    fi
fi
if [ -f "$TARGET_DIR/wallet.js" ]; then
    cd "$TARGET_DIR" && exec node wallet.js "$@"
else
    echo "GhostWallet (wallet.js) tidak ditemukan di $TARGET_DIR"
    exit 1
fi
""")

if os.path.exists("/usr/local/bin/agy-copilot"):
    shutil.copy2("/usr/local/bin/agy-copilot", os.path.join(BUNDLE_DIR, "bin/agy-copilot"))

for b in os.listdir(os.path.join(BUNDLE_DIR, "bin")):
    os.chmod(os.path.join(BUNDLE_DIR, "bin", b), 0o755)

print("--- 2. Menyiapkan Wallet & Configs ke bundle/wallet ---")
if os.path.exists("/root/wallet/wallet.js"):
    shutil.copy2("/root/wallet/wallet.js", os.path.join(BUNDLE_DIR, "wallet/wallet.js"))
if os.path.exists("/root/wallet/package.json"):
    shutil.copy2("/root/wallet/package.json", os.path.join(BUNDLE_DIR, "wallet/package.json"))
if os.path.exists("/root/wallet/chains_db.json"):
    shutil.copy2("/root/wallet/chains_db.json", os.path.join(BUNDLE_DIR, "wallet/chains_db.json"))

for rpc in glob.glob("/root/wallet/config_rpc*.json"):
    shutil.copy2(rpc, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(rpc)))

# Cari x_webview.js
x_source = None
for candidate in ["/project/termux-launcher/fusion-setup/x_webview.js", "/root/wallet/x_webview.js", "/project/clean_build/fusion_bundle/wallet/x_webview.js"]:
    if os.path.exists(candidate):
        x_source = candidate
        break
if x_source:
    shutil.copy2(x_source, os.path.join(BUNDLE_DIR, "wallet/x_webview.js"))
    os.chmod(os.path.join(BUNDLE_DIR, "wallet/x_webview.js"), 0o755)
    print(f"✓ Added x_webview.js from {x_source} to wallet bundle")

# Dummy template wallets_db.json dan recipes_db.json jika belum ada
dummy_wallet = {"master_index": None, "wallets": []}
if not os.path.exists(os.path.join(BUNDLE_DIR, "wallet/wallets_db.json")):
    with open(os.path.join(BUNDLE_DIR, "wallet/wallets_db.json"), "w") as f:
        json.dump(dummy_wallet, f, indent=2)

if not os.path.exists(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json")):
    with open(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json"), "w") as f:
        f.write("{}\n")

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
    os.path.join(ASSETS_DIR, "x_webview.js"),
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
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/bashnh")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/bashnh"), os.path.join(ASSETS_DIR, "bashnh"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/wallet")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet"), os.path.join(ASSETS_DIR, "wallet"))
if os.path.exists(os.path.join(BUNDLE_DIR, "termux.properties")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "termux.properties"), os.path.join(ASSETS_DIR, "termux.properties"))

print(f"✓ Berhasil menyinkronkan seluruh aset terlindungi ke {ASSETS_DIR}")

