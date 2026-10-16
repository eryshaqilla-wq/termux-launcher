#!/usr/bin/env python3
import os, sys, shutil, glob, tarfile, json

BUNDLE_DIR = "/project/clean_build/fusion_bundle"
OUTPUT_TAR = "/project/clean_build/fusion_payload.tar.gz"

if os.path.exists(BUNDLE_DIR):
    shutil.rmtree(BUNDLE_DIR)
os.makedirs(os.path.join(BUNDLE_DIR, "bin"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "wallet"), exist_ok=True)

print("--- 1. Menyiapkan Binary & Tools ke bundle/bin ---")
# Cari termux-ipc
ipc_source = None
for candidate in ["/project/termux-launcher/fusion-setup/termux-ipc", "/project/termux-ipc", "/usr/local/bin/termux-ipc"]:
    if os.path.exists(candidate):
        ipc_source = candidate
        break
if ipc_source:
    shutil.copy2(ipc_source, os.path.join(BUNDLE_DIR, "bin/termux-ipc"))

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

print("--- 2c. Menyiapkan Plugin Zsh (Autosuggestions & Highlighting) ---")
zsh_plugin_dir = os.path.join(BUNDLE_DIR, "zsh_plugins")
os.makedirs(os.path.join(zsh_plugin_dir, "zsh-autosuggestions"), exist_ok=True)
os.makedirs(os.path.join(zsh_plugin_dir, "zsh-syntax-highlighting"), exist_ok=True)

auto_src = "/root/.oh-my-zsh/custom/plugins/zsh-autosuggestions"
if os.path.exists(auto_src):
    for f in glob.glob(os.path.join(auto_src, "*.zsh")):
        shutil.copy2(f, os.path.join(zsh_plugin_dir, "zsh-autosuggestions"))
    if os.path.exists(os.path.join(auto_src, "src")):
        shutil.copytree(os.path.join(auto_src, "src"), os.path.join(zsh_plugin_dir, "zsh-autosuggestions/src"), dirs_exist_ok=True)

syn_src = "/root/.oh-my-zsh/custom/plugins/zsh-syntax-highlighting"
if os.path.exists(syn_src):
    for f in glob.glob(os.path.join(syn_src, "*.zsh")):
        shutil.copy2(f, os.path.join(zsh_plugin_dir, "zsh-syntax-highlighting"))
    if os.path.exists(os.path.join(syn_src, "highlighters")):
        shutil.copytree(os.path.join(syn_src, "highlighters"), os.path.join(zsh_plugin_dir, "zsh-syntax-highlighting/highlighters"), dirs_exist_ok=True)

print("✓ Added zsh plugins to bundle")

print("--- 3. Mengemas menjadi fusion_payload.tar.gz ---")
with tarfile.open(OUTPUT_TAR, "w:gz") as tar:
    for item in os.listdir(BUNDLE_DIR):
        item_path = os.path.join(BUNDLE_DIR, item)
        tar.add(item_path, arcname=item)

print(f"✓ Payload berhasil dibuat: {OUTPUT_TAR} ({os.path.getsize(OUTPUT_TAR)} bytes)")

# Salin ke lokasi assets aplikasi Android
ASSETS_DIR = "/project/termux-launcher/app/src/main/assets/fusion"
FUSION_SETUP_DIR = "/project/termux-launcher/fusion-setup"
os.makedirs(ASSETS_DIR, exist_ok=True)

# 1. Payload tarball
shutil.copy2(OUTPUT_TAR, os.path.join(ASSETS_DIR, "fusion_payload.tar.gz"))
shutil.copy2(OUTPUT_TAR, os.path.join(FUSION_SETUP_DIR, "fusion_payload.tar.gz"))

# 2. Standalone files di assets agar bisa langsung disalin Java saat pertama buka
shutil.copy2(os.path.join(BUNDLE_DIR, "setup.sh"), os.path.join(ASSETS_DIR, "setup.sh"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/termux-ipc"), os.path.join(ASSETS_DIR, "termux-ipc"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/browser"), os.path.join(ASSETS_DIR, "browser"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/x"), os.path.join(ASSETS_DIR, "x"))
if os.path.exists(os.path.join(BUNDLE_DIR, "wallet/x_webview.js")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "wallet/x_webview.js"), os.path.join(ASSETS_DIR, "x_webview.js"))
if os.path.exists(os.path.join(BUNDLE_DIR, "termux.properties")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "termux.properties"), os.path.join(ASSETS_DIR, "termux.properties"))

print(f"✓ Berhasil menyinkronkan seluruh aset ke {ASSETS_DIR}")
