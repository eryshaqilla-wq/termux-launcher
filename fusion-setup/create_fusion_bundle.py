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

# Wrapper browser & web (Universal Autonomous Web Controller)
agent_web_src = "/usr/local/bin/agent-web"
if not os.path.exists(agent_web_src):
    agent_web_src = "/project/termux-launcher/fusion-setup/agent-web"
if os.path.exists(agent_web_src):
    shutil.copy2(agent_web_src, os.path.join(BUNDLE_DIR, "bin/agent-web"))
    os.chmod(os.path.join(BUNDLE_DIR, "bin/agent-web"), 0o755)
    print("✓ Added agent-web to bundle/bin")

with open(os.path.join(BUNDLE_DIR, "bin/web"), "w") as f:
    f.write("#!/usr/bin/env bash\nexec agent-web \"$@\"\n")
os.chmod(os.path.join(BUNDLE_DIR, "bin/web"), 0o755)

with open(os.path.join(BUNDLE_DIR, "bin/browser"), "w") as f:
    f.write("#!/usr/bin/env bash\nexec web \"$@\"\n")
os.chmod(os.path.join(BUNDLE_DIR, "bin/browser"), 0o755)

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

# Wrapper telegram (Telegram Swarm & Bot Orchestrator)
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

# Wrapper telegram-ui (Telegram Web UI Launcher)
with open(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), "w") as f:
    f.write("""#!/data/data/com.termux/files/usr/bin/bash
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

OTHER_PIDS=$(ps -eo pid,args 2>/dev/null | grep -E "(telegram\\.cjs|pemantau\\.cjs|pemantautelegram\\.py)" | grep -v -E "(grep|--ui|tmux)" | awk '{print $1}')
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
""")

# Wrapper telegram-relogin (Auto Re-login Tool)
with open(os.path.join(BUNDLE_DIR, "bin/telegram-relogin"), "w") as f:
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

tmux kill-session -t telegram_server >/dev/null 2>&1 || true
if [ -f "$TARGET_DIR/telegram.cjs" ]; then
    echo "🔍 Memeriksa akun yang terkena begal / sesi hangus..."
    node "$TARGET_DIR/telegram.cjs" --relogin auto
    echo "🚀 Menyalakan kembali server antarmuka..."
    tmux new-session -d -s telegram_server "node '$TARGET_DIR/telegram.cjs' --ui"
    echo "✨ Selesai! Sesi pemantau telah pulih dan aktif."
    exit 0
fi

echo "telegram.cjs tidak ditemukan di $TARGET_DIR"
exit 1
""")

# Wrapper wallet-ui (Wallet Web UI Launcher)
with open(os.path.join(BUNDLE_DIR, "bin/wallet-ui"), "w") as f:
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

if [ -f "$TARGET_DIR/wallet_ui_server.cjs" ]; then
    cd "$TARGET_DIR" && exec node wallet_ui_server.cjs "$@"
elif [ -f "$TARGET_DIR/wallet.cjs" ]; then
    cd "$TARGET_DIR" && exec node wallet.cjs ui "$@"
fi
echo "wallet_ui_server.cjs tidak ditemukan di $TARGET_DIR"
exit 1
""")

# Wrapper ai (Tri-Engine Autonomous Controller)
if os.path.exists("/usr/local/bin/ai"):
    shutil.copy2("/usr/local/bin/ai", os.path.join(BUNDLE_DIR, "bin/ai"))
    os.chmod(os.path.join(BUNDLE_DIR, "bin/ai"), 0o755)
elif os.path.exists("/root/wallet/auto_engine.cjs"):
    with open(os.path.join(BUNDLE_DIR, "bin/ai"), "w") as f:
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

if [ -f "$TARGET_DIR/auto_engine.cjs" ]; then
    cd "$TARGET_DIR" && exec node auto_engine.cjs "$@"
fi
echo "auto_engine.cjs tidak ditemukan di $TARGET_DIR"
exit 1
""")

if os.path.exists("/usr/local/bin/agy-copilot"):
    shutil.copy2("/usr/local/bin/agy-copilot", os.path.join(BUNDLE_DIR, "bin/agy-copilot"))
if os.path.exists("/usr/local/bin/agy-switch"):
    shutil.copy2("/usr/local/bin/agy-switch", os.path.join(BUNDLE_DIR, "bin/agy-switch"))
    # Pasang symlinks praktis
    for alias_name in ["switch", "ganti", "agy-ganti"]:
        alias_path = os.path.join(BUNDLE_DIR, "bin", alias_name)
        if os.path.islink(alias_path) or os.path.exists(alias_path):
            os.remove(alias_path)
        os.symlink("agy-switch", alias_path)
elif os.path.exists("/usr/local/bin/agy-ganti"):
    shutil.copy2("/usr/local/bin/agy-ganti", os.path.join(BUNDLE_DIR, "bin/agy-switch"))
if os.path.exists("/usr/local/bin/agy-install"):
    shutil.copy2("/usr/local/bin/agy-install", os.path.join(BUNDLE_DIR, "bin/agy-install"))
elif os.path.exists("/project/termux-launcher/fusion-setup/agy-install"):
    shutil.copy2("/project/termux-launcher/fusion-setup/agy-install", os.path.join(BUNDLE_DIR, "bin/agy-install"))
if os.path.exists("/usr/local/bin/eso"):
    shutil.copy2("/usr/local/bin/eso", os.path.join(BUNDLE_DIR, "bin/eso"))
elif os.path.exists("/project/termux-launcher/fusion-setup/eso"):
    shutil.copy2("/project/termux-launcher/fusion-setup/eso", os.path.join(BUNDLE_DIR, "bin/eso"))
if os.path.exists("/usr/local/bin/pwd-vault"):
    shutil.copy2("/usr/local/bin/pwd-vault", os.path.join(BUNDLE_DIR, "bin/pwd-vault"))

for b in os.listdir(os.path.join(BUNDLE_DIR, "bin")):
    os.chmod(os.path.join(BUNDLE_DIR, "bin", b), 0o755)

print("--- 2. Menyiapkan Wallet, Sniper & Configs ke bundle/wallet ---")
# 1. Salin Standalone CJS & Code Suite beserta Template Konfigurasi
for f_cjs in [
    "wallet.cjs", "wallet_ui_server.cjs", "wallet_ui.html", "wallet_solana.cjs", "wallet_multichain.cjs", "auto_engine.cjs", "x_native.cjs",
    "x_webview.cjs", "pemantau.cjs", "telegram.cjs", "9router.cjs", "agy_ganti.cjs", "eso_db.cjs",
    "config_telegram.json", "tele_filter_config.json", "filter_config.json",
    "query_db.json", "module_db.json", "pantau.json", "webview_profiles.json",
    "ai_config.json", "ai_selectors.json", "pembantu_avatar.jpg", "blacklists.json",
    "modules.json", "queries.json", "watchlist.json", "tg_config.json",
    "scankomen_trigger.json", "loop_state.json", "tg_cache.json", "button_query_db.json",
    "leadloop_cards.json", "ka_agents_cards.json", "kantor_tokens.json", "ptah_poa_cards.json"
]:
    for src_dir in ["/root/wallet", "/project/node2cjs"]:
        src_path = os.path.join(src_dir, f_cjs)
        if os.path.exists(src_path):
            shutil.copy2(src_path, os.path.join(BUNDLE_DIR, "wallet", f_cjs))
            break
print("✓ Added CJS suite & templates to bundle")

# Salin Web UI assets jika ada (web/index.html, dll)
for w_dir in ["/root/wallet/web", "/project/node2cjs/web"]:
    if os.path.exists(w_dir):
        dest_web = os.path.join(BUNDLE_DIR, "wallet", "web")
        if os.path.exists(dest_web):
            shutil.rmtree(dest_web)
        shutil.copytree(w_dir, dest_web)
        print(f"✓ Added web assets from {w_dir} to bundle")
        break

# 2. Salin Regular JS/Python Suite & Public Chain DB
for f_js in ["wallet.js", "wallet_solana.js", "x_webview.js", "package.json", "chains_db.json"]:
    for src_candidate in [os.path.join("/root/wallet", f_js), os.path.join("/project/node2cjs", f_js)]:
        if os.path.exists(src_candidate):
            shutil.copy2(src_candidate, os.path.join(BUNDLE_DIR, "wallet", f_js))
            break

# 3. Salin RPC Configurations & Gate Configurations
for rpc in glob.glob("/root/wallet/config_rpc*.json") + glob.glob("/project/node2cjs/config_rpc*.json"):
    shutil.copy2(rpc, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(rpc)))

for g_conf in glob.glob("/project/node2cjs/gate*.json") + glob.glob("/root/wallet/gate*.json") + glob.glob("/project/node2cjs/www.gate*.json"):
    shutil.copy2(g_conf, os.path.join(BUNDLE_DIR, "wallet", os.path.basename(g_conf)))

# 4. Buat Template Kosong Standar untuk Database Dompet & Auxiliary (Steril dari data/private key pribadi)
dummy_wallet = {"master_index": None, "wallets": []}
for w_db in [
    "wallets_db.json",
    "wallets_solana_db.json",
    "wallets_tron_db.json",
    "wallets_sui_db.json",
    "wallets_aptos_db.json",
    "wallets_iota_db.json"
]:
    with open(os.path.join(BUNDLE_DIR, "wallet", w_db), "w") as f:
        json.dump(dummy_wallet, f, indent=2)

if not os.path.exists(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json")):
    with open(os.path.join(BUNDLE_DIR, "wallet/recipes_db.json"), "w") as f:
        f.write("{}\n")

if not os.path.exists(os.path.join(BUNDLE_DIR, "wallet/passwords_db.json")):
    with open(os.path.join(BUNDLE_DIR, "wallet/passwords_db.json"), "w") as f:
        f.write("{}\n")

# 4b. Buat Template Steril untuk eso.db (SQLite)
template_db_path = os.path.join(BUNDLE_DIR, "wallet", "eso.db")
if os.path.exists("/root/wallet/eso.db"):
    try:
        import sqlite3
        if os.path.exists(template_db_path):
            os.remove(template_db_path)
        s_conn = sqlite3.connect("/root/wallet/eso.db")
        d_conn = sqlite3.connect(template_db_path)
        for sql in s_conn.execute("SELECT sql FROM sqlite_master WHERE sql IS NOT NULL"):
            try:
                d_conn.execute(sql[0])
            except Exception:
                pass
        for t_name in ['chains', 'configs', 'queries', 'blacklists', 'modules', 'android_metadata']:
            cur = s_conn.execute(f"SELECT * FROM {t_name}")
            rows = cur.fetchall()
            if rows:
                qmarks = ','.join(['?'] * len(rows[0]))
                d_conn.executemany(f"INSERT INTO {t_name} VALUES ({qmarks})", rows)
        d_conn.commit()
        d_conn.execute("VACUUM;")
        d_conn.close()
        s_conn.close()
        print("✓ Created sterilized eso.db template for bundle")
    except Exception as e:
        print(f"Warning: gagal membuat template eso.db: {e}")

# Template kosong untuk tuyul & auxiliary
with open(os.path.join(BUNDLE_DIR, "wallet/tuyul_emails.json"), "w") as f:
    json.dump({"emails": ["tuyul1@gmail.com", "tuyul2@gmail.com"], "index": 0}, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/tuyul_refs.json"), "w") as f:
    json.dump({"refs": ["REF123", "ALPHA99"], "index": 0}, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/tuyul_fingerprints.json"), "w") as f:
    json.dump({"fingerprints": ["Samsung Galaxy S24 Ultra", "Google Pixel 8 Pro", "Xiaomi 14 Ultra"], "index": 0}, f, indent=2)

with open(os.path.join(BUNDLE_DIR, "wallet/ai_engine.json"), "w") as f:
    json.dump({"activeEngine": "auto", "updatedAt": 0}, f, indent=2)

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

librofi_src = "/project/termux-launcher/fusion-setup/librofitouch.c"
if os.path.exists(librofi_src):
    shutil.copy2(librofi_src, os.path.join(BUNDLE_DIR, "librofitouch.c"))
    print("✓ Added librofitouch.c to bundle")

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

print("--- 2e. Menyiapkan Preset Eso (Nerd Font, Colors, Properties, SharedPreferences) ke bundle ---")
preset_eso_src = "/project/termux-launcher/fusion-setup/presets/eso"
if os.path.exists(preset_eso_src):
    dest_preset = os.path.join(BUNDLE_DIR, "presets/eso")
    os.makedirs(dest_preset, exist_ok=True)
    for p_item in os.listdir(preset_eso_src):
        shutil.copy2(os.path.join(preset_eso_src, p_item), os.path.join(dest_preset, p_item))
    print("✓ Added Tuan Eso preset (Nerd Font, Base16 Isotope, UI Preferences) to bundle")

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
    os.path.join(ASSETS_DIR, "wallet_ui_server.cjs"),
    os.path.join(ASSETS_DIR, "wallet_ui.html"),
    os.path.join(ASSETS_DIR, "wallet_solana.cjs"),
    os.path.join(ASSETS_DIR, "wallet_solana.js"),
    os.path.join(ASSETS_DIR, "wallet_multichain.cjs"),
    os.path.join(ASSETS_DIR, "auto_engine.cjs"),
    os.path.join(ASSETS_DIR, "eso_db.cjs"),
    os.path.join(ASSETS_DIR, "eso.db"),
    os.path.join(ASSETS_DIR, "master_bot.cjs"),
    os.path.join(ASSETS_DIR, "mint_fountain.cjs"),
    os.path.join(ASSETS_DIR, "sniper_akita.cjs"),
    os.path.join(ASSETS_DIR, "agy_ganti.cjs"),
    os.path.join(ASSETS_DIR, "wallets_solana_db.json"),
    os.path.join(ASSETS_DIR, "solana_wallets.json"),
    os.path.join(ASSETS_DIR, "wallets_tron_db.json"),
    os.path.join(ASSETS_DIR, "wallets_sui_db.json"),
    os.path.join(ASSETS_DIR, "wallets_aptos_db.json"),
    os.path.join(ASSETS_DIR, "wallets_iota_db.json"),
    os.path.join(ASSETS_DIR, "tuyul_emails.json"),
    os.path.join(ASSETS_DIR, "tuyul_refs.json"),
    os.path.join(ASSETS_DIR, "tuyul_fingerprints.json"),
    os.path.join(ASSETS_DIR, "ai_engine.json"),
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
    os.path.join(ASSETS_DIR, "email.txt"),
    os.path.join(ASSETS_DIR, "ref.txt"),
]:
    if os.path.exists(target):
        os.remove(target)

# 2. Standalone files di assets yang aman (installer & CLI runner umum)
shutil.copy2(os.path.join(BUNDLE_DIR, "setup.sh"), os.path.join(ASSETS_DIR, "setup.sh"))
if os.path.exists(os.path.join(BUNDLE_DIR, "librofitouch.c")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "librofitouch.c"), os.path.join(ASSETS_DIR, "librofitouch.c"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/termux-ipc"), os.path.join(ASSETS_DIR, "termux-ipc"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/termux-ipc-bridge"), os.path.join(ASSETS_DIR, "termux-ipc-bridge"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/browser"), os.path.join(ASSETS_DIR, "browser"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/web")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/web"), os.path.join(ASSETS_DIR, "web"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/web"), os.path.join(FUSION_SETUP_DIR, "web"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/agent-web")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agent-web"), os.path.join(ASSETS_DIR, "agent-web"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agent-web"), os.path.join(FUSION_SETUP_DIR, "agent-web"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/x"), os.path.join(ASSETS_DIR, "x"))
shutil.copy2(os.path.join(BUNDLE_DIR, "bin/x"), os.path.join(FUSION_SETUP_DIR, "x"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/bashnh")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/bashnh"), os.path.join(ASSETS_DIR, "bashnh"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/wallet")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet"), os.path.join(ASSETS_DIR, "wallet"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet"), os.path.join(FUSION_SETUP_DIR, "wallet"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/wallet-ui")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet-ui"), os.path.join(ASSETS_DIR, "wallet-ui"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/wallet-ui"), os.path.join(FUSION_SETUP_DIR, "wallet-ui"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/ai")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/ai"), os.path.join(ASSETS_DIR, "ai"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/ai"), os.path.join(FUSION_SETUP_DIR, "ai"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/pemantau")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/pemantau"), os.path.join(ASSETS_DIR, "pemantau"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/pemantau"), os.path.join(FUSION_SETUP_DIR, "pemantau"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/telegram")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram"), os.path.join(ASSETS_DIR, "telegram"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram"), os.path.join(FUSION_SETUP_DIR, "telegram"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/telegram-ui")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), os.path.join(ASSETS_DIR, "telegram-ui"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-ui"), os.path.join(FUSION_SETUP_DIR, "telegram-ui"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/telegram-relogin")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-relogin"), os.path.join(ASSETS_DIR, "telegram-relogin"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/telegram-relogin"), os.path.join(FUSION_SETUP_DIR, "telegram-relogin"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/9router")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/9router"), os.path.join(ASSETS_DIR, "9router"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/9router"), os.path.join(FUSION_SETUP_DIR, "9router"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/agy-switch")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-switch"), os.path.join(ASSETS_DIR, "agy-switch"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-switch"), os.path.join(FUSION_SETUP_DIR, "agy-switch"))
    for d_target in [ASSETS_DIR, FUSION_SETUP_DIR]:
        for lk in ["switch", "ganti", "agy-ganti"]:
            lp = os.path.join(d_target, lk)
            if os.path.islink(lp) or os.path.exists(lp):
                os.remove(lp)
            with open(lp, "w") as f:
                f.write("#!/usr/bin/env bash\nexec agy-switch \"$@\"\n")
            os.chmod(lp, 0o755)
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/agy-copilot")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-copilot"), os.path.join(ASSETS_DIR, "agy-copilot"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-copilot"), os.path.join(FUSION_SETUP_DIR, "agy-copilot"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/agy-install")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-install"), os.path.join(ASSETS_DIR, "agy-install"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/agy-install"), os.path.join(FUSION_SETUP_DIR, "agy-install"))
if os.path.exists(os.path.join(BUNDLE_DIR, "bin/eso")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/eso"), os.path.join(ASSETS_DIR, "eso"))
    shutil.copy2(os.path.join(BUNDLE_DIR, "bin/eso"), os.path.join(FUSION_SETUP_DIR, "eso"))
if os.path.exists(os.path.join(BUNDLE_DIR, "termux.properties")):
    shutil.copy2(os.path.join(BUNDLE_DIR, "termux.properties"), os.path.join(ASSETS_DIR, "termux.properties"))

print(f"✓ Berhasil menyinkronkan seluruh aset terlindungi ke {ASSETS_DIR}")

