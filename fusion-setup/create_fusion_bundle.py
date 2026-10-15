#!/usr/bin/env python3
import os, sys, shutil, glob, tarfile, json

BUNDLE_DIR = "/project/clean_build/fusion_bundle"
OUTPUT_TAR = "/project/clean_build/fusion_payload.tar.gz"

if os.path.exists(BUNDLE_DIR):
    shutil.rmtree(BUNDLE_DIR)
os.makedirs(os.path.join(BUNDLE_DIR, "bin"), exist_ok=True)
os.makedirs(os.path.join(BUNDLE_DIR, "wallet"), exist_ok=True)

print("--- 1. Menyiapkan Binary & Tools ke bundle/bin ---")
shutil.copy2("/project/termux-ipc", os.path.join(BUNDLE_DIR, "bin/termux-ipc"))

with open(os.path.join(BUNDLE_DIR, "bin/browser"), "w") as f:
    f.write("#!/data/data/com.termux/files/usr/bin/bash\nexec termux-ipc webview \"$@\"\n")

if os.path.exists("/usr/local/bin/agy-copilot"):
    shutil.copy2("/usr/local/bin/agy-copilot", os.path.join(BUNDLE_DIR, "bin/agy-copilot"))

for b in os.listdir(os.path.join(BUNDLE_DIR, "bin")):
    os.chmod(os.path.join(BUNDLE_DIR, "bin", b), 0o755)

print("--- 2. Menyiapkan Wallet & Configs ke bundle/wallet ---")
shutil.copy2("/root/wallet/wallet.js", os.path.join(BUNDLE_DIR, "wallet/wallet.js"))
shutil.copy2("/root/wallet/package.json", os.path.join(BUNDLE_DIR, "wallet/package.json"))
if os.path.exists("/root/wallet/chains_db.json"):
    shutil.copy2("/root/wallet/chains_db.json", os.path.join(BUNDLE_DIR, "wallet/chains_db.json"))

for rpc in glob.glob("/root/wallet/config_rpc*.json"):
    shutil.copy2(rpc, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(rpc)))

if os.path.exists("/root/wallet/x_webview.js"):
    shutil.copy2("/root/wallet/x_webview.js", os.path.join(BUNDLE_DIR, "wallet/x_webview.js"))
    print("✓ Added x_webview.js to wallet bundle")

# Dummy template wallets_db.json dan recipes_db.json
dummy_wallet = {"master_index": None, "wallets": []}
with open(os.path.join(BUNDLE_DIR, "wallet/wallets_db.json"), "w") as f:
    json.dump(dummy_wallet, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json"), "w") as f:
    f.write("{}\n")

# Copy login.template, termux.properties, dan setup.sh
if os.path.exists("/root/project/login.template"):
    shutil.copy2("/root/project/login.template", os.path.join(BUNDLE_DIR, "login.template"))
if os.path.exists("/project/clean_build/termux.properties"):
    shutil.copy2("/project/clean_build/termux.properties", os.path.join(BUNDLE_DIR, "termux.properties"))
shutil.copy2("/project/clean_build/setup.sh", os.path.join(BUNDLE_DIR, "setup.sh"))
os.chmod(os.path.join(BUNDLE_DIR, "setup.sh"), 0o755)

print("--- 3. Mengemas menjadi fusion_payload.tar.gz ---")
with tarfile.open(OUTPUT_TAR, "w:gz") as tar:
    for item in os.listdir(BUNDLE_DIR):
        item_path = os.path.join(BUNDLE_DIR, item)
        tar.add(item_path, arcname=item)

print(f"✓ Payload berhasil dibuat: {OUTPUT_TAR} ({os.path.getsize(OUTPUT_TAR)} bytes)")
