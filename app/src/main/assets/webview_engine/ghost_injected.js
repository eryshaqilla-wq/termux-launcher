(function() {
    if (window.__FUSION_GHOST_INJECTED__) return;
    window.__FUSION_GHOST_INJECTED__ = true;

    var isDaemon = function() {
        if (window.GhostBridge && window.GhostBridge.isDaemonAlive) {
            try { return window.GhostBridge.isDaemonAlive(); } catch(e) {}
        }
        return false;
    };
    var walletAddress = isDaemon() && (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : "";
    var initialChainHex = (window.GhostBridge && window.GhostBridge.getChainIdHex) ? window.GhostBridge.getChainIdHex() : "0x2105";
    var initialChainDec = (window.GhostBridge && window.GhostBridge.getChainIdDec) ? window.GhostBridge.getChainIdDec() : "8453";
    var maskIdentity = "omni";

    var listeners = {};

    var emitEvent = function(event, data) {
        if (listeners[event]) {
            listeners[event].forEach(function(fn) {
                try { fn(data); } catch(e) {}
            });
        }
    };

    window.addEventListener("accountsChanged", function(e) {
        var newAddr = (e.detail && e.detail[0]) ? e.detail[0] : (e.detail || "");
        if (newAddr) {
            walletAddress = newAddr;
            fakeProvider.selectedAddress = newAddr;
            emitEvent("accountsChanged", [newAddr]);
        }
    });

    window.addEventListener("chainChanged", function(e) {
        var newChain = (e.detail) ? e.detail : "";
        if (newChain) {
            fakeProvider.chainId = newChain;
            try {
                var dec = (newChain.indexOf("0x") === 0) ? parseInt(newChain, 16) : parseInt(newChain, 10);
                fakeProvider.networkVersion = String(dec);
            } catch(err) {}
            emitEvent("chainChanged", newChain);
            if (window.GhostBridge && window.GhostBridge.postRpc) {
                try { window.GhostBridge.postRpc(JSON.stringify({ method: 'wallet_switchEthereumChain', params: [{ chainId: newChain }] })); } catch(err) {}
            }
        }
    });

    var callBridge = async function(args) {
        if (!args) return null;
        var method = args.method || "";

        // Fast-path immediate responses
        if (method === 'eth_requestAccounts' || method === 'eth_accounts') {
            var alive = isDaemon();
            if (!alive) {
                if (method === 'eth_requestAccounts') {
                    if (window.GhostBridge && window.GhostBridge.notifyDaemonOffline) {
                        try { window.GhostBridge.notifyDaemonOffline(); } catch(e) {}
                    }
                    var err = new Error("Daemon wallet.js belum aktif! Harap start daemon terlebih dahulu.");
                    err.code = 4001;
                    throw err;
                }
                return [];
            }
            var activeAddr = (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : walletAddress;
            if (activeAddr) {
                walletAddress = activeAddr;
                fakeProvider.selectedAddress = activeAddr;
                return [activeAddr];
            }
        }
        if (method === 'eth_chainId') return (window.GhostBridge && window.GhostBridge.getChainIdHex) ? window.GhostBridge.getChainIdHex() : fakeProvider.chainId;
        if (method === 'net_version') return (window.GhostBridge && window.GhostBridge.getChainIdDec) ? window.GhostBridge.getChainIdDec() : fakeProvider.networkVersion;
        if (method === 'wallet_getCapabilities') return {};
        if (method === 'wallet_requestPermissions') {
            var alive = isDaemon();
            if (!alive) {
                if (window.GhostBridge && window.GhostBridge.notifyDaemonOffline) {
                    try { window.GhostBridge.notifyDaemonOffline(); } catch(e) {}
                }
                var err = new Error("Daemon wallet.js belum aktif! Harap start daemon terlebih dahulu.");
                err.code = 4001;
                throw err;
            }
            return [{ caveats: [], parentCapability: "eth_accounts" }];
        }

        // EIP-3326 & EIP-3085 Auto-Switch / Chain Switch Intercept
        if (method === 'wallet_switchEthereumChain' || method === 'wallet_addEthereumChain') {
            var targetHex = "";
            if (args.params && args.params[0]) {
                targetHex = args.params[0].chainId || "";
            }
            if (targetHex) {
                fakeProvider.chainId = targetHex;
                try {
                    var dec = (targetHex.indexOf("0x") === 0) ? parseInt(targetHex, 16) : parseInt(targetHex, 10);
                    fakeProvider.networkVersion = String(dec);
                } catch(e) {}
                if (window.GhostBridge && window.GhostBridge.switchChainByHex) {
                    try { window.GhostBridge.switchChainByHex(targetHex); } catch(e) {}
                }
                // Forward switch to daemon so wallet.js switches its active RPC/chain
                if (window.GhostBridge && window.GhostBridge.postRpc) {
                    try { window.GhostBridge.postRpc(JSON.stringify(args)); } catch(e) {}
                }
                emitEvent("chainChanged", targetHex);
                window.dispatchEvent(new CustomEvent("chainChanged", { detail: targetHex }));
            }
            return null;
        }

        if (window.GhostBridge && window.GhostBridge.postRpc) {
            try {
                var raw = window.GhostBridge.postRpc(JSON.stringify(args));
                var parsed = JSON.parse(raw);
                if (parsed.result !== undefined) {
                    if ((method === 'eth_requestAccounts' || method === 'eth_accounts') && Array.isArray(parsed.result) && parsed.result[0]) {
                        walletAddress = parsed.result[0];
                        fakeProvider.selectedAddress = parsed.result[0];
                        emitEvent("accountsChanged", [walletAddress]);
                    }
                    return parsed.result;
                }
                if (parsed.error) {
                    var errMsg = (typeof parsed.error === 'string') ? parsed.error : (parsed.error.message || JSON.stringify(parsed.error));
                    var errObj = new Error(errMsg);
                    errObj.code = (parsed.error && parsed.error.code) ? parsed.error.code : 4001;
                    throw errObj;
                }
            } catch(e) {
                throw e;
            }
        }

        // Fallback fetch ke daemon lokal port 3333
        try {
            var ctrl = new AbortController();
            var timeoutMs = (method === 'eth_sendTransaction' || method === 'eth_sign' || method === 'personal_sign' || method === 'eth_signTypedData_v4' || method === 'eth_estimateGas') ? 120000 : 10000;
            var tid = setTimeout(function() { ctrl.abort(); }, timeoutMs);
            var res = await fetch("http://127.0.0.1:3333", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(args),
                signal: ctrl.signal
            });
            clearTimeout(tid);
            var data = await res.json();
            if (data.error) throw new Error(data.error);
            return data.result;
        } catch(e) {
            throw new Error(e.message || "Ghost RPC daemon unreachable (Port 3333)");
        }
    };

    var fakeProvider = {
        isMetaMask: true,
        networkVersion: initialChainDec,
        chainId: initialChainHex,
        
        request: async function(args) { return await callBridge(args); },
        send: async function(method, params) { return await callBridge({ method: method, params: params || [] }); },
        sendAsync: function(payload, cb) {
            callBridge({ method: payload.method, params: payload.params || [] })
                .then(function(res) { cb(null, { id: payload.id, jsonrpc: "2.0", result: res }); })
                .catch(function(err) { cb(err); });
        },
        enable: async function() { return await callBridge({ method: 'eth_requestAccounts' }); },
        on: function(event, fn) {
            if (!listeners[event]) listeners[event] = [];
            listeners[event].push(fn);
        },
        removeListener: function(event, fn) {
            if (listeners[event]) {
                listeners[event] = listeners[event].filter(function(f) { return f !== fn; });
            }
        },
        removeAllListeners: function(event) {
            if (event) delete listeners[event];
            else listeners = {};
        }
    };

    Object.defineProperty(fakeProvider, 'selectedAddress', {
        get: function() {
            var alive = isDaemon();
            if (!alive) return null;
            return walletAddress || ((window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : null);
        },
        set: function(val) {
            walletAddress = val;
        },
        configurable: true,
        enumerable: true
    });
    window.ethereum = fakeProvider;
    window.ethereum.providers = [fakeProvider];

    var KTP_DB = [
        { id: "metamask", uuid: "11111111-90fa-40da-8d1e-089b3f3edcc2", name: "MetaMask", rdns: "io.metamask.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iI0U0NzYxQiIgZD0iTTIyLjcgMTEuN2wtMi0uNCAxLjEtMy4xYy4xLS40LS4xLS43LS41LS44bS0uMi4xIi8+PC9zdmc+" },
        { id: "trustwallet", uuid: "22222222-40da-40da-8d1e-089b3f3edcc2", name: "Trust Wallet", rdns: "com.trustwallet.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iTTEyIDJMMiA3bDExLjIgMTQuM2EuOC44IDAgMCAwIDEuNiAwTDIyIDdsLTEwLTV6IiBmaWxsPSIjMDUwMEZGIi8+PC9zdmc+" },
        { id: "uniswap", uuid: "33333333-40da-40da-8d1e-089b3f3edcc2", name: "Uniswap Wallet", rdns: "org.uniswap.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iTTEyIDIybC04LTgtMi0ydjh6IiBmaWxsPSIjRkYwMDdBIi8+PC9zdmc+" },
        { id: "rainbow", uuid: "44444444-40da-40da-8d1e-089b3f3edcc2", name: "Rainbow", rdns: "me.rainbow", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTEiIGZpbGw9IiMwMDFGNTciLz48L3N2Zz4=" },
        { id: "coinbase", uuid: "55555555-40da-40da-8d1e-089b3f3edcc2", name: "Coinbase Wallet", rdns: "com.coinbase.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTEiIGZpbGw9IiMwMDUyRkYiLz48L3N2Zz4=" },
        { id: "okx", uuid: "66666666-40da-40da-8d1e-089b3f3edcc2", name: "OKX Wallet", rdns: "com.okex.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzAwMDAwMCIgZD0iTTAgMGgyNHYyNEgweiIvPjwvc3ZnPg==" },
        { id: "rabby", uuid: "77777777-40da-40da-8d1e-089b3f3edcc2", name: "Rabby Wallet", rdns: "io.rabby", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzg2OTdGRiIgZD0iTTEyIDJMMiA3bDExLjIgMTQuM2EuOC44IDAgMCAwIDEuNiAwTDIyIDdsLTEwLTV6Ii8+PC9zdmc+" },
        { id: "bitget", uuid: "88888888-40da-40da-8d1e-089b3f3edcc2", name: "Bitget Wallet", rdns: "com.bitget.web3", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzAwRjZDNCIgZD0iTTEyIDBMMCAxMmwxMiAxMiAxMi0xMnoiLz48L3N2Zz4=" }
    ];

    var announce = function() {
        var activeMasks = (maskIdentity === "omni") ? KTP_DB : KTP_DB.filter(function(k) { return k.id === maskIdentity; });
        if (activeMasks.length === 0) activeMasks = KTP_DB;
        activeMasks.forEach(function(ktp) {
            window.dispatchEvent(new CustomEvent("eip6963:announceProvider", {
                detail: { info: { uuid: ktp.uuid, name: ktp.name, icon: ktp.icon, rdns: ktp.rdns }, provider: fakeProvider }
            }));
        });
    };

    announce();
    window.addEventListener("eip6963:requestProvider", announce);
})();


// ============================================================================
// AGY CO-PILOT HUD, IN-PAGE SLASH AUTOCOMPLETE & ACCURATE RECORDER ENGINE (v3.0)
// Enhancements & Fixes:
// 1. Instant DOM/HTML Click Recording: executeClickAction records immediately (0ms).
// 2. Full Native Hook: window.__FUSION_START_REC__ / STOP_REC__ synced with AgyRecorder.
// 3. Unbreakable Touch-Shield: 100vw/100vh backdrop with touch & pointer containment.
// 4. Persistent Static Tabs: Tab buttons are created ONCE, zero DOM node destruction.
// 5. Isolated Recorder: SlashAutocomplete.isOpen prevents ANY clicks from being recorded.
// ============================================================================

(function() {
    if (window.__AGY_ENGINE_INITIALIZED__) return;
    window.__AGY_ENGINE_INITIALIZED__ = true;

    window.__AGY_SIMULATED_CLICK__ = false;
    window.__AGY_SIMULATED_FILL__ = false;

    // --- 1. MINIMALIST BLACK-TRANSPARENT FLOATING HUD ---
    function updateAgyHUD(titleText, subText, statusType) {
        try {
            var doc = document.body || document.documentElement;
            if (!doc) return;

            var hud = document.getElementById("__agy_copilot_hud__");
            if (!hud) {
                hud = document.createElement("div");
                hud.id = "__agy_copilot_hud__";
                hud.style.position = "fixed";
                hud.style.top = "10px";
                hud.style.left = "10px";
                hud.style.background = "#1F1F24";
                hud.style.border = "1px solid #36373D";
                hud.style.borderRadius = "4px";
                hud.style.padding = "6px 10px";
                hud.style.color = "#E2E2E8";
                hud.style.zIndex = "2147483647";
                hud.style.fontFamily = "system-ui, -apple-system, sans-serif";
                hud.style.fontSize = "11.5px";
                hud.style.lineHeight = "1.35";
                hud.style.textAlign = "left";
                hud.style.maxWidth = "280px";
                hud.style.boxShadow = "0 4px 16px rgba(0, 0, 0, 0.7)";
                hud.style.transition = "opacity 0.2s ease, transform 0.2s ease";
                doc.appendChild(hud);
            }

            var icon = "⚡";
            var iconColor = "#8AB4F8";
            if (statusType === "success") { icon = "✓"; iconColor = "#7CD4BE"; }
            if (statusType === "error" || statusType === "warning") { icon = "✕"; iconColor = "#F28B82"; }
            if (statusType === "recording") { icon = "●"; iconColor = "#F28B82"; }
            if (statusType === "recording_step") { icon = "●"; iconColor = "#7CD4BE"; }
            if (statusType === "running") { icon = "▶"; iconColor = "#D0BCFF"; }

            var titleStr = (titleText || "").trim();
            var subStr = (subText || "").trim();

            var contentHtml = "<div style='display:flex; align-items:flex-start; gap:6px;'>" +
                              "<span style='color:" + iconColor + "; font-weight:900; font-size:12px; line-height:1.2; flex-shrink:0; margin-top:1px;'>" + icon + "</span>" +
                              "<div style='display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; word-break:break-word; font-size:11.5px; line-height:1.35; color:#E2E2E8;'>" +
                              "<strong style='color:#FFFFFF; font-weight:700;'>" + titleStr + "</strong>" +
                              (subStr.length > 0 ? "<span style='color:#9BA1AB; margin-left:4px;'>— " + subStr + "</span>" : "") +
                              "</div></div>";

            if (statusType === "running") {
                hud.style.pointerEvents = "auto";
                contentHtml += "<div style='margin-top:5px;'><button id='__agy_stop_btn__' style='background:#2B1B1B; color:#F28B82; border:1px solid #BA1A1A; border-radius:4px; padding:3px 8px; font-size:10.5px; font-weight:bold; cursor:pointer;'>⏹ STOP</button></div>";
            } else {
                hud.style.pointerEvents = "none";
            }

            hud.innerHTML = contentHtml;
            hud.style.opacity = "1";
            hud.style.transform = "translateY(0)";

            if (statusType === "running") {
                var stopBtn = document.getElementById("__agy_stop_btn__");
                if (stopBtn) {
                    stopBtn.onclick = function(e) {
                        e.stopPropagation();
                        if (window.AgyBridge && window.AgyBridge.stop) window.AgyBridge.stop();
                    };
                }
            }

            if (hud.__timer) clearTimeout(hud.__timer);
            if (statusType === "recording_step") {
                hud.__timer = setTimeout(function() {
                    hud.style.opacity = "0";
                    hud.style.transform = "translateY(-4px)";
                }, 1500);
            } else if (statusType === "recording") {
                hud.__timer = setTimeout(function() {
                    hud.style.opacity = "0";
                    hud.style.transform = "translateY(-4px)";
                }, 2000);
            } else if (statusType === "warning") {
                hud.__timer = setTimeout(function() {
                    hud.style.opacity = "0";
                    hud.style.transform = "translateY(-4px)";
                    setTimeout(function() { if (hud && hud.parentNode) hud.remove(); }, 300);
                }, 800);
            } else if (statusType === "success" || statusType === "error") {
                hud.__timer = setTimeout(function() {
                    hud.style.opacity = "0";
                    hud.style.transform = "translateY(-4px)";
                }, 2000);
            }
        } catch(e) {
            console.error("[AGY HUD Error]", e);
        }
    }

    // --- 2. CONFIGURATION & DATABASE PROVIDER (DYNAMIC CHROOT/TERMUX/ANDROID) ---
    var AgyConfig = {
        STORAGE_KEY: "__agy_automation_config__",
        get: function() {
            var defaults = {
                stepDelay: 1500,        // delay per langkah (ms)
                batchDelay: 3000,       // jeda antar akun (ms)
                waitMode: "strict",     // "strict" (complete) atau "fast" (interactive)
                elementTimeout: 8000,   // timeout tunggu elemen (ms)
                autoReload: false,      // auto reload tiap ganti akun
                dbFolder: "/root/wallet/",
                walletFile: "wallets_db.json",
                emailFile: "email.txt"
            };
            try {
                var stored = localStorage.getItem(this.STORAGE_KEY);
                if (stored) {
                    var parsed = JSON.parse(stored);
                    for (var k in parsed) {
                        if (parsed.hasOwnProperty(k)) defaults[k] = parsed[k];
                    }
                }
            } catch(_) {}
            return defaults;
        },
        save: function(cfg) {
            try {
                var cur = this.get();
                for (var k in cfg) {
                    if (cfg.hasOwnProperty(k)) cur[k] = cfg[k];
                }
                localStorage.setItem(this.STORAGE_KEY, JSON.stringify(cur));
                return cur;
            } catch(_) {
                return cfg;
            }
        }
    };

    var AgyDB = {
        getConfig: function() {
            return AgyConfig.get();
        },

        resolveFilePath: function(fileName) {
            var cfg = this.getConfig();
            var folder = cfg.dbFolder || "/root/wallet/";
            if (!folder.endsWith("/")) folder += "/";
            return folder + fileName;
        },

        getEmails: function() {
            var cfg = this.getConfig();
            var emails = [];
            var targetFile = this.resolveFilePath(cfg.emailFile || "email.txt");
            try {
                if (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") {
                    var raw = window.GhostBridge.readWalletFile(targetFile);
                    if (!raw || raw.trim().length === 0) {
                        raw = window.GhostBridge.readWalletFile(cfg.emailFile || "email.txt");
                    }
                    if (raw && raw.trim().length > 0) {
                        emails = raw.split(/[\r\n]+/).map(function(s) { return s.trim(); }).filter(function(s) { return s.includes("@"); });
                    }
                }
            } catch(_) {}
            if (emails.length === 0) {
                emails = ["eryziggy@gmail.com", "err24.es@gmail.com"];
            }
            return emails;
        },

        getWallets: function() {
            var cfg = this.getConfig();
            var list = [];
            var targetFile = this.resolveFilePath(cfg.walletFile || "wallets_db.json");
            try {
                if (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") {
                    var raw = window.GhostBridge.readWalletFile(targetFile);
                    if (!raw || raw.trim().length === 0) {
                        raw = window.GhostBridge.readWalletFile(cfg.walletFile || "wallets_db.json");
                    }
                    if (raw && raw.trim().length > 0) {
                        var parsed = JSON.parse(raw);
                        if (parsed && Array.isArray(parsed.wallets)) {
                            list = parsed.wallets.map(function(w) {
                                return { name: w.name || "Wallet", address: w.address || "" };
                            }).filter(function(w) { return w.address.startsWith("0x"); });
                        }
                    }
                }
            } catch(_) {}
            if (list.length === 0) {
                list = [
                    { name: "Master .env", address: "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359" },
                    { name: "tuyul-2", address: "0xCBF711D2668b03b4cF485626ff261Dcc80705Ab3" },
                    { name: "tuyul-3", address: "0x7a5bD0C7C686334b1d031C298a8417BFb3F1F4Da" }
                ];
            }
            return list;
        },

        getQueries: function() {
            var queries = [];
            try {
                if (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") {
                    var raw = window.GhostBridge.readWalletFile("query_db.json");
                    if (raw && raw.trim().length > 0) {
                        var parsed = JSON.parse(raw);
                        if (Array.isArray(parsed)) queries = queries.concat(parsed);
                    }
                    var rawQ = window.GhostBridge.readWalletFile("queries.json");
                    if (rawQ && rawQ.trim().length > 0) {
                        var parsedQ = JSON.parse(rawQ);
                        if (Array.isArray(parsedQ)) queries = queries.concat(parsedQ);
                    }
                }
            } catch(_) {}
            if (queries.length === 0) {
                queries = ["iota.gives", "whatpay.com/hongbao", "GATE-"];
            }
            return Array.from(new Set(queries));
        },

        activeEmailIndex: 0,
        nextEmail: function() {
            var emails = this.getEmails();
            if (emails.length === 0) return "user1@gmail.com";
            this.activeEmailIndex = (this.activeEmailIndex + 1) % emails.length;
            return emails[this.activeEmailIndex];
        },

        getRefs: function() {
            var refs = [];
            try {
                if (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") {
                    var rawRef = window.GhostBridge.readWalletFile("ref.txt");
                    if (rawRef && rawRef.trim().length > 0) {
                        refs = rawRef.split(/[\r\n]+/).map(function(s) { return s.trim(); }).filter(function(s) { return s.length > 0; });
                    }
                }
            } catch(_) {}
            if (refs.length === 0) {
                var q = this.getQueries();
                if (q && q.length > 0) refs = q;
            }
            if (refs.length === 0) {
                refs = ["REF123"];
            }
            return refs;
        },

        activeRefIndex: 0,
        nextRef: function() {
            var list = this.getRefs();
            if (list.length === 0) return "REF123";
            var code = list[this.activeRefIndex % list.length];
            this.activeRefIndex = (this.activeRefIndex + 1) % list.length;
            return code;
        },

        getActiveWallet: function() {
            var addr = (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : "";
            var name = (window.GhostBridge && window.GhostBridge.getWalletName) ? window.GhostBridge.getWalletName() : "Master";
            return { address: addr, name: name };
        },

        setActiveWallet: function(name, address) {
            if (window.GhostBridge && typeof window.GhostBridge.setWalletDirect === "function") {
                try { window.GhostBridge.setWalletDirect(name, address); } catch(_) {}
            }
        },

        addAccountDirect: function(name, address, email) {
            var cfg = this.getConfig();
            var success = false;
            try {
                var targetWalletFile = this.resolveFilePath(cfg.walletFile || "wallets_db.json");
                var rawJson = (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") ? window.GhostBridge.readWalletFile(targetWalletFile) : "";
                var dbObj = { wallets: [] };
                if (rawJson && rawJson.trim().length > 0) {
                    try { dbObj = JSON.parse(rawJson); } catch(_) {}
                }
                if (!Array.isArray(dbObj.wallets)) dbObj.wallets = [];
                dbObj.wallets.push({ name: name, address: address });
                if (window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                    window.GhostBridge.writeWalletFile(targetWalletFile, JSON.stringify(dbObj, null, 2));
                    success = true;
                }
            } catch(_) {}

            if (email && email.includes("@")) {
                try {
                    var targetEmailFile = this.resolveFilePath(cfg.emailFile || "email.txt");
                    var rawTxt = (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") ? window.GhostBridge.readWalletFile(targetEmailFile) : "";
                    if (!rawTxt) rawTxt = "";
                    if (!rawTxt.endsWith("\n") && rawTxt.length > 0) rawTxt += "\n";
                    rawTxt += email.trim() + "\n";
                    if (window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                        window.GhostBridge.writeWalletFile(targetEmailFile, rawTxt);
                        success = true;
                    }
                } catch(_) {}
            }
            return success;
        }
    };

    // --- TARGET EXPRESSION PARSER (1, 1-5, 1,4,5, *) ---
    function parseTargetExpression(expr, totalAccounts) {
        if (!expr || expr.trim() === "" || expr.trim() === "*" || expr.trim().toLowerCase() === "all") {
            var all = [];
            for (var i = 1; i <= totalAccounts; i++) all.push(i);
            return all;
        }
        var indices = new Set();
        var parts = expr.split(",").map(function(s) { return s.trim(); }).filter(Boolean);
        for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            if (part.includes("-")) {
                var rangeParts = part.split("-").map(function(n) { return parseInt(n.trim(), 10); });
                var start = rangeParts[0];
                var end = rangeParts[1];
                if (!isNaN(start) && !isNaN(end)) {
                    var low = Math.max(1, Math.min(start, end));
                    var high = Math.min(totalAccounts, Math.max(start, end));
                    for (var k = low; k <= high; k++) indices.add(k);
                }
            } else {
                var idx = parseInt(part, 10);
                if (!isNaN(idx) && idx >= 1 && idx <= totalAccounts) {
                    indices.add(idx);
                }
            }
        }
        var res = Array.from(indices).sort(function(a, b) { return a - b; });
        return res.length > 0 ? res : [1];
    }

    // --- AGY BATCH RUNNER & SAFETY ABORT CONTROLLER ---
    var AgyBatchRunner = {
        isRunning: false,
        isAborted: false,
        activeTimer: null,

        stop: function() {
            this.isAborted = true;
            this.isRunning = false;
            if (this.activeTimer) {
                clearTimeout(this.activeTimer);
                this.activeTimer = null;
            }
            updateAgyHUD("Otomatisasi Dihentikan", "Eksekusi dibatalkan oleh pengguna", "warning");
        },

        run: function(recipeEvents, recipeName, targetExpr, isBatch, onComplete, runMode, loopCount) {
            var self = this;
            if (!recipeEvents || recipeEvents.length === 0) {
                updateAgyHUD("Resep Kosong", "Tidak ada langkah untuk dieksekusi", "warning");
                return;
            }

            self.isRunning = true;
            self.isAborted = false;

            var wallets = AgyDB.getWallets();
            var emails = AgyDB.getEmails();
            var cfg = AgyConfig.get();

            var mode = runMode || "multi";
            var loops = parseInt(loopCount, 10);
            if (isNaN(loops) || loops <= 0) loops = 1;
            var isInfinite = (loopCount === -1 || loopCount === "inf" || loopCount === "infinity" || loops >= 999999);

            var queue = [];
            if (mode === "single") {
                var singleTarget = parseInt(targetExpr, 10);
                if (isNaN(singleTarget) || singleTarget < 1) singleTarget = 1;
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    queue.push(singleTarget);
                }
            } else if (mode === "standalone") {
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    queue.push(0); // 0 = Standalone Web Macro (Tanpa Wallet)
                }
            } else {
                queue = parseTargetExpression(targetExpr, wallets.length);
            }

            var successCount = 0;
            var failCount = 0;
            var currentIdxInQueue = 0;

            function runAccount() {
                if (self.isAborted) return;
                if (!isInfinite && currentIdxInQueue >= queue.length) {
                    self.isRunning = false;
                    updateAgyHUD("Batch Selesai!", "Total: " + queue.length + " (Sukses: " + successCount + ", Gagal: " + failCount + ")", "success");
                    if (onComplete) onComplete({ success: successCount, failed: failCount });
                    return;
                }

                var accNumber = queue[currentIdxInQueue % queue.length];
                var curWallet = null;
                var curEmail = "";
                var title = "";
                var sub = "";

                if (mode === "standalone") {
                    curWallet = { name: "Web Macro", address: "" };
                    curEmail = "macro@termux.local";
                    title = isInfinite ? (recipeName + " [Loop #" + (currentIdxInQueue + 1) + "]") : ((queue.length > 1) ? (recipeName + " [" + (currentIdxInQueue + 1) + "/" + queue.length + "]") : recipeName);
                    sub = "Mode: Web Macro Murni (Tanpa Wallet)";
                } else if (mode === "single") {
                    var walletIdx = (accNumber - 1) % (wallets.length || 1);
                    var emailIdx = (accNumber - 1) % (emails.length || 1);
                    curWallet = wallets[walletIdx] || { name: "Acc #" + accNumber, address: "0x..." };
                    curEmail = emails[emailIdx] || (curWallet.name.toLowerCase().replace(/[^a-z0-9]/g, "") + "@termux.local");
                    AgyDB.setActiveWallet(curWallet.name, curWallet.address);
                    title = isInfinite ? (recipeName + " [Loop #" + (currentIdxInQueue + 1) + "]") : (recipeName + " [" + (currentIdxInQueue + 1) + "/" + queue.length + "]");
                    sub = "Fokus: " + curWallet.name + " (" + curWallet.address.substring(0, 8) + "...)";
                } else {
                    var walletIdx = (accNumber - 1) % (wallets.length || 1);
                    var emailIdx = (accNumber - 1) % (emails.length || 1);
                    curWallet = wallets[walletIdx] || { name: "Acc #" + accNumber, address: "0x..." };
                    curEmail = emails[emailIdx] || (curWallet.name.toLowerCase().replace(/[^a-z0-9]/g, "") + "@termux.local");
                    AgyDB.setActiveWallet(curWallet.name, curWallet.address);
                    title = (queue.length > 1) ? ("Batch [" + (currentIdxInQueue + 1) + "/" + queue.length + "]") : recipeName;
                    sub = "Akun #" + accNumber + " (" + curWallet.name + " | " + curWallet.address.substring(0, 8) + "...)";
                }

                updateAgyHUD(title, sub, "running");

                self.executeStepList(recipeEvents, curWallet, curEmail, title, function(isSuccess) {
                    if (self.isAborted) return;
                    if (isSuccess) successCount++;
                    else failCount++;

                    currentIdxInQueue++;

                    var hasMore = isInfinite || (currentIdxInQueue < queue.length);
                    var shouldDelay = (mode === "multi" ? isBatch : true) && hasMore;

                    if (shouldDelay) {
                        var waitSec = (cfg.batchDelay / 1000).toFixed(1);
                        var nextLabel = (mode === "single" || mode === "standalone") ? ("Jeda Putaran (" + waitSec + "s)...") : ("Menuju Akun berikutnya (" + waitSec + "s)...");
                        updateAgyHUD("Jeda Antar Alur", nextLabel, "running");

                        self.activeTimer = setTimeout(function() {
                            if (self.isAborted) return;
                            runAccount();
                        }, cfg.batchDelay);
                    } else if (hasMore) {
                        runAccount();
                    } else {
                        self.isRunning = false;
                        updateAgyHUD("Selesai!", "Sukses: " + successCount + " | Gagal: " + failCount, "success");
                        if (onComplete) onComplete({ success: successCount, failed: failCount });
                    }
                });
            }

            runAccount();
        },

        executeStepList: function(eventsList, curWallet, curEmail, runTitle, doneCallback, startIdx) {
            var self = this;
            var cfg = AgyConfig.get();
            var stepDelay = cfg.stepDelay || 1500;
            var waitMode = cfg.waitMode || "strict";
            var maxTimeout = cfg.elementTimeout || 8000;

            function waitDocReady(next) {
                if (waitMode === "fast") {
                    if (document.readyState === "complete" || document.readyState === "interactive") {
                        setTimeout(next, 50);
                        return;
                    }
                }
                waitForDocumentReady(function() {
                    setTimeout(next, waitMode === "strict" ? 600 : 100);
                }, 5000);
            }

            waitDocReady(function() {
                if (self.isAborted) return;
                var idx = (typeof startIdx === "number") ? startIdx : 0;

                function runNextStep() {
                    if (self.isAborted) {
                        try { sessionStorage.removeItem("__agy_active_recipe_run__"); } catch(_) {}
                        return;
                    }
                    if (idx >= eventsList.length) {
                        try { sessionStorage.removeItem("__agy_active_recipe_run__"); } catch(_) {}
                        setTimeout(function() { doneCallback(true); }, 400);
                        return;
                    }

                    var ev = eventsList[idx];
                    var stepNum = idx + 1;

                    // Simpan progress sebelum eksekusi agar survive saat halaman reload / berpindah URL
                    try {
                        sessionStorage.setItem("__agy_active_recipe_run__", JSON.stringify({
                            runTitle: runTitle,
                            eventsList: eventsList,
                            idx: idx,
                            curWallet: curWallet,
                            curEmail: curEmail,
                            timestamp: Date.now()
                        }));
                    } catch(_) {}

                    idx++;

                    if (ev.type === "wait") {
                        var dur = ev.duration || stepDelay;
                        updateAgyHUD(runTitle, "⏳ Jeda langkah " + stepNum + "/" + eventsList.length + " (" + (dur / 1000).toFixed(1) + "s)", "running");
                        self.activeTimer = setTimeout(runNextStep, dur);
                        return;
                    }

                    if (ev.type === "next_wallet") {
                        updateAgyHUD(runTitle, "👛 Langkah " + stepNum + "/" + eventsList.length + ": Ganti Wallet", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.nextWallet) {
                                window.GhostBridge.nextWallet();
                            }
                        } catch(_) {}
                        self.activeTimer = setTimeout(runNextStep, 800);
                        return;
                    }

                    if (ev.type === "reload") {
                        updateAgyHUD(runTitle, "🔄 Langkah " + stepNum + "/" + eventsList.length + ": Reload Halaman...", "running");
                        try {
                            sessionStorage.setItem("__agy_active_recipe_run__", JSON.stringify({
                                runTitle: runTitle,
                                eventsList: eventsList,
                                idx: idx,
                                curWallet: curWallet,
                                curEmail: curEmail,
                                timestamp: Date.now()
                            }));
                        } catch(_) {}
                        setTimeout(function() {
                            location.reload();
                        }, 500);
                        return;
                    }

                    if (ev.type === "wipe_cache") {
                        updateAgyHUD(runTitle, "🧹 Langkah " + stepNum + "/" + eventsList.length + ": Wipe Cache", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.wipeCache) {
                                window.GhostBridge.wipeCache();
                            }
                        } catch(_) {}
                        try { localStorage.clear(); sessionStorage.clear(); } catch(_) {}
                        self.activeTimer = setTimeout(runNextStep, 600);
                        return;
                    }

                    if (ev.type === "next_email") {
                        var ne = ev.value || ((typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "tuyul@gmail.com");
                        curEmail = ne;
                        updateAgyHUD(runTitle, "📧 Langkah " + stepNum + "/" + eventsList.length + ": Ganti Email -> " + ne, "running");
                        self.activeTimer = setTimeout(runNextStep, 600);
                        return;
                    }

                    if (ev.type === "ref") {
                        var nr = ev.value || ((typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123");
                        updateAgyHUD(runTitle, "🔗 Langkah " + stepNum + "/" + eventsList.length + ": Ref Code -> " + nr, "running");
                        self.activeTimer = setTimeout(runNextStep, 600);
                        return;
                    }

                    if (ev.type === "fingerprint") {
                        updateAgyHUD(runTitle, "🎭 Langkah " + stepNum + "/" + eventsList.length + ": Ganti Fingerprint...", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.nextFingerprint) {
                                window.GhostBridge.nextFingerprint();
                            }
                        } catch(_) {}
                        self.activeTimer = setTimeout(runNextStep, 800);
                        return;
                    }

                    if (ev.type === "swipe") {
                        var dir = ev.direction || "down";
                        var distY = -ev.deltaY || (dir === "up" ? 300 : (dir === "down" ? -300 : 0));
                        var distX = -ev.deltaX || (dir === "left" ? 250 : (dir === "right" ? -250 : 0));
                        try {
                            window.scrollBy({ left: distX, top: distY, behavior: "smooth" });
                        } catch(_) {}
                        updateAgyHUD(runTitle, "👆 Langkah " + stepNum + "/" + eventsList.length + ": Swipe " + dir.toUpperCase(), "running");
                        self.activeTimer = setTimeout(function() {
                            waitDocReady(runNextStep);
                        }, stepDelay);
                        return;
                    }

                    smartWaitForElementWithTimeout(ev, stepNum, eventsList.length, maxTimeout, function(targetEl) {
                        if (self.isAborted) return;
                        if (!targetEl) {
                            self.activeTimer = setTimeout(runNextStep, 400);
                            return;
                        }

                        try { targetEl.scrollIntoView({ behavior: "smooth", block: "center" }); } catch(_) {}

                        if (ev.type === "fill" || ev.type === "input") {
                            var valToFill = ev.value || "";
                            if (valToFill.includes("{{WALLET_ADDRESS}}")) valToFill = valToFill.replace(/\{\{WALLET_ADDRESS\}\}/g, curWallet.address);
                            if (valToFill.includes("{{EMAIL}}")) valToFill = valToFill.replace(/\{\{EMAIL\}\}/g, curEmail);
                            if (valToFill.includes("{{NAME}}")) valToFill = valToFill.replace(/\{\{NAME\}\}/g, curWallet.name);

                            targetEl.focus();
                            var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                                         Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
                            if (setter) setter.call(targetEl, valToFill);
                            else targetEl.value = valToFill;

                            targetEl.dispatchEvent(new Event("input", { bubbles: true }));
                            targetEl.dispatchEvent(new Event("change", { bubbles: true }));
                            updateAgyHUD(runTitle, "✍️ Langkah " + stepNum + "/" + eventsList.length + ": Isi " + valToFill.substring(0, 16), "running");

                            self.activeTimer = setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay);
                        } else {
                            var commonOpts = { bubbles: true, cancelable: true, view: window };
                            if (window.PointerEvent) {
                                try {
                                    targetEl.dispatchEvent(new PointerEvent("pointerdown", commonOpts));
                                    targetEl.dispatchEvent(new PointerEvent("pointerup", commonOpts));
                                } catch(_) {}
                            }
                            try {
                                targetEl.dispatchEvent(new MouseEvent("mousedown", commonOpts));
                                targetEl.dispatchEvent(new MouseEvent("mouseup", commonOpts));
                                targetEl.dispatchEvent(new MouseEvent("click", commonOpts));
                            } catch(_) {}
                            if (typeof targetEl.click === "function") {
                                try { targetEl.click(); } catch(_) {}
                            }
                            updateAgyHUD(runTitle, "🖱️ Langkah " + stepNum + "/" + eventsList.length + ": Klik " + (ev.text || targetEl.tagName), "running");

                            self.activeTimer = setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay);
                        }
                    });
                }

                runNextStep();
            });
        }
    };

    function smartWaitForElementWithTimeout(ev, stepIdx, totalSteps, maxWaitMs, callback) {
        var interval = 250;
        var elapsed = 0;
        var timeoutLimit = maxWaitMs || 8000;

        function findTarget() {
            var targetEl = null;
            if (ev.selector) {
                try { targetEl = document.querySelector(ev.selector); } catch(_) {}
            }
            if (!targetEl && ev.id) targetEl = document.getElementById(ev.id);
            if (!targetEl && ev.testid) {
                try { targetEl = document.querySelector("[data-testid='" + ev.testid + "']"); } catch(_) {}
            }
            if (!targetEl && ev.name) {
                try { targetEl = document.querySelector("[name='" + ev.name + "']"); } catch(_) {}
            }
            if (!targetEl && ev.ariaLabel) {
                try { targetEl = document.querySelector("[aria-label='" + ev.ariaLabel + "']"); } catch(_) {}
            }
            if (!targetEl && ev.text) targetEl = findInteractiveElement(ev.text);
            if (!targetEl && ev.x && ev.y) targetEl = document.elementFromPoint(ev.x, ev.y);
            return targetEl;
        }

        var found = findTarget();
        if (found) {
            callback(found);
            return;
        }

        var label = ev.text || ev.name || ev.id || ev.selector || ev.type;
        var pollTimer = setInterval(function() {
            if (AgyBatchRunner.isAborted) {
                clearInterval(pollTimer);
                return;
            }
            elapsed += interval;
            var el = findTarget();
            if (el) {
                clearInterval(pollTimer);
                callback(el);
            } else if (elapsed >= timeoutLimit) {
                clearInterval(pollTimer);
                console.warn("[AgyReplay] Timeout waiting for element on step " + stepIdx + ":", ev);
                callback(null);
            }
        }, interval);
    }

    // --- 3. MULTI-RECIPE STORAGE MANAGER ---
    // --- 3. MULTI-RECIPE STORAGE MANAGER (GLOBAL DISK + HYBRID SYNC) ---
    var RecipeStore = {
        _cache: null,
        getAll: function() {
            var all = {};
            // 1. Baca dari database file global via GhostBridge (/root/wallet/recipes_db.json)
            try {
                if (window.GhostBridge && typeof window.GhostBridge.readWalletFile === "function") {
                    var fileRaw = window.GhostBridge.readWalletFile("recipes_db.json");
                    if (fileRaw && fileRaw.trim().length > 0) {
                        var parsed = JSON.parse(fileRaw);
                        if (parsed && typeof parsed === "object") {
                            all = parsed;
                        }
                    }
                }
            } catch(e) {
                console.warn("[RecipeStore] Gagal membaca recipes_db.json global:", e);
            }

            // 2. Baca dari localStorage untuk merge (apabila ada resep lama tersimpan di domain ini)
            try {
                var localRaw = localStorage.getItem("__agy_named_recipes__");
                if (localRaw) {
                    var localParsed = JSON.parse(localRaw);
                    if (localParsed && typeof localParsed === "object") {
                        var needSync = false;
                        for (var k in localParsed) {
                            if (!all[k]) {
                                all[k] = localParsed[k];
                                needSync = true;
                            }
                        }
                        if (needSync && window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                            try {
                                window.GhostBridge.writeWalletFile("recipes_db.json", JSON.stringify(all, null, 2));
                            } catch(_) {}
                        }
                    }
                }
            } catch(_) {}

            // 3. Simpan mirror di localStorage
            try {
                localStorage.setItem("__agy_named_recipes__", JSON.stringify(all));
            } catch(_) {}

            this._cache = all;
            return all;
        },
        save: function(name, events, targetUrl) {
            if (!name || name.trim().length === 0) {
                name = "Recipe_" + new Date().getHours() + "h" + new Date().getMinutes() + "m";
            }
            name = name.trim();
            var all = this.getAll();
            var finalTargetUrl = targetUrl;
            if (!finalTargetUrl && typeof window !== "undefined" && window.location) {
                try {
                    var curHref = window.location.href;
                    if (curHref && curHref.indexOf("about:") !== 0) {
                        finalTargetUrl = curHref.split("#")[0];
                    }
                } catch(_) {}
            }
            all[name] = {
                name: name,
                createdAt: (all[name] && all[name].createdAt) || Date.now(),
                targetUrl: finalTargetUrl || (all[name] && all[name].targetUrl) || "",
                events: events || []
            };

            // Simpan ke disk global
            try {
                if (window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                    window.GhostBridge.writeWalletFile("recipes_db.json", JSON.stringify(all, null, 2));
                }
            } catch(e) {
                console.warn("[RecipeStore] Gagal menulis recipes_db.json:", e);
            }

            // Simpan ke localStorage
            try {
                localStorage.setItem("__agy_named_recipes__", JSON.stringify(all));
                localStorage.setItem("__fusion_macro_events__", JSON.stringify(events || []));
            } catch(_) {}

            window.__FUSION_MACRO_EVENTS__ = events || [];
            this._cache = all;
            return name;
        },
        remove: function(name) {
            var all = this.getAll();
            if (all[name]) {
                delete all[name];
                try {
                    if (window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                        window.GhostBridge.writeWalletFile("recipes_db.json", JSON.stringify(all, null, 2));
                    }
                } catch(_) {}
                try {
                    localStorage.setItem("__agy_named_recipes__", JSON.stringify(all));
                } catch(_) {}
            }
            this._cache = all;
        },
        clear: function() {
            try {
                if (window.GhostBridge && typeof window.GhostBridge.writeWalletFile === "function") {
                    window.GhostBridge.writeWalletFile("recipes_db.json", "{}");
                }
            } catch(_) {}
            try {
                localStorage.removeItem("__agy_named_recipes__");
                localStorage.removeItem("__fusion_macro_events__");
            } catch(_) {}
            this._cache = {};
        },
        getLastEvents: function() {
            try {
                var raw = localStorage.getItem("__fusion_macro_events__");
                if (raw) return JSON.parse(raw);
            } catch(_) {}
            return (window.__FUSION_MACRO_EVENTS__ && window.__FUSION_MACRO_EVENTS__.length > 0) ? window.__FUSION_MACRO_EVENTS__ : [];
        }
    };

    // --- 3B. SMART RECIPE CODE SHARING & REPLICATION ENGINE ---
    function sanitizeEventsForExport(events) {
        if (!Array.isArray(events)) return [];
        return events.map(function(ev) {
            var item = { type: ev.type || "click" };
            if (ev.selector) item.selector = ev.selector;
            if (ev.xpath) item.xpath = ev.xpath;
            if (ev.text) item.text = ev.text;
            if (ev.value !== undefined) item.value = ev.value;
            if (ev.name) item.name = ev.name;
            if (ev.delay) item.delay = ev.delay;
            if (typeof ev.x === "number") item.x = Math.round(ev.x);
            if (typeof ev.y === "number") item.y = Math.round(ev.y);
            return item;
        });
    }

    function encodeRecipeCode(recipeObj) {
        var minEvents = sanitizeEventsForExport(recipeObj.events || []);
        var rawUrl = recipeObj.targetUrl || "";
        if (!rawUrl && typeof window !== "undefined" && window.location) {
            try {
                if (window.location.href.indexOf("about:") !== 0) rawUrl = window.location.href.split("#")[0];
            } catch(_) {}
        }
        var payload = {
            name: recipeObj.name || "Resep",
            targetUrl: rawUrl,
            events: minEvents,
            v: 1,
            t: Date.now()
        };
        var jsonStr = JSON.stringify(payload);
        var b64 = "";
        try {
            b64 = btoa(unescape(encodeURIComponent(jsonStr)));
        } catch(_) {
            try { b64 = btoa(jsonStr); } catch(__) { b64 = ""; }
        }
        var code = "AGY-RECIPE:v1:" + b64;
        var baseWeb = rawUrl || (window.location.href.indexOf("about:") !== 0 ? window.location.href.split("#")[0] : "https://termux.fusion");
        var shareUrl = baseWeb + "#agy_code=" + encodeURIComponent(b64);
        return {
            code: code,
            url: shareUrl,
            b64: b64,
            payload: payload
        };
    }

    function decodeRecipeCode(rawStr) {
        if (!rawStr) return null;
        var s = rawStr.trim();
        var matchUrl = s.match(/[#\?&]agy_(?:code|recipe)=([^&\s]+)/i);
        if (matchUrl && matchUrl[1]) {
            s = decodeURIComponent(matchUrl[1]);
        } else if (s.indexOf("AGY-RECIPE:") === 0) {
            var parts = s.split(":");
            s = parts[parts.length - 1].trim();
        }
        try {
            var jsonStr = "";
            try {
                jsonStr = decodeURIComponent(escape(atob(s)));
            } catch(_) {
                jsonStr = atob(s);
            }
            var parsed = JSON.parse(jsonStr);
            if (parsed && (parsed.name || parsed.events)) {
                return {
                    name: parsed.name || "Resep_Impor",
                    targetUrl: parsed.targetUrl || parsed.url || "",
                    events: Array.isArray(parsed.events) ? parsed.events : []
                };
            }
        } catch(_) {}
        try {
            var pJson = JSON.parse(rawStr);
            if (pJson && (pJson.name || pJson.events)) {
                return {
                    name: pJson.name || "Resep_Impor",
                    targetUrl: pJson.targetUrl || pJson.url || "",
                    events: Array.isArray(pJson.events) ? pJson.events : []
                };
            }
        } catch(_) {}
        return null;
    }

    function showShareRecipeModal(recipeName, events, targetUrl) {
        var existing = document.getElementById("__agy_share_modal__");
        if (existing) existing.remove();

        var shareObj = encodeRecipeCode({ name: recipeName, events: events, targetUrl: targetUrl });
        var domain = "";
        try { domain = new URL(shareObj.payload.targetUrl).hostname; } catch(_) { domain = shareObj.payload.targetUrl; }

        var modal = document.createElement("div");
        modal.id = "__agy_share_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.85)";
        modal.style.backdropFilter = "blur(8px)";
        modal.style.webkitBackdropFilter = "blur(8px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.alignItems = "center";
        modal.style.justifyContent = "center";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        modal.style.padding = "16px";
        modal.style.boxSizing = "border-box";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "1px solid rgba(203, 166, 247, 0.4)";
        card.style.borderRadius = "20px";
        card.style.width = "100%";
        card.style.maxWidth = "400px";
        card.style.padding = "22px 20px 20px 20px";
        card.style.boxShadow = "0 20px 50px rgba(0,0,0,0.85), 0 0 25px rgba(203, 166, 247, 0.15)";
        card.style.color = "#cdd6f4";
        card.style.boxSizing = "border-box";
        card.style.display = "flex";
        card.style.flexDirection = "column";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "14px";

        var titleBlock = document.createElement("div");
        titleBlock.style.display = "flex";
        titleBlock.style.alignItems = "center";
        titleBlock.style.gap = "8px";

        var icon = document.createElement("span");
        icon.innerText = "📤";
        icon.style.fontSize = "18px";
        titleBlock.appendChild(icon);

        var title = document.createElement("div");
        title.innerText = "Bagikan Rekaman";
        title.style.fontSize = "15px";
        title.style.fontWeight = "700";
        title.style.color = "#cdd6f4";
        titleBlock.appendChild(title);

        var badge = document.createElement("span");
        badge.innerText = recipeName;
        badge.style.fontSize = "12px";
        badge.style.fontWeight = "600";
        badge.style.color = "#cba6f7";
        badge.style.background = "#313244";
        badge.style.padding = "3px 8px";
        badge.style.borderRadius = "8px";
        titleBlock.appendChild(badge);
        titleRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function() { modal.remove(); };
        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        // Info pill
        var infoRow = document.createElement("div");
        infoRow.style.background = "#11111b";
        infoRow.style.border = "1px solid #313244";
        infoRow.style.borderRadius = "10px";
        infoRow.style.padding = "9px 12px";
        infoRow.style.marginBottom = "16px";
        infoRow.style.fontSize = "11px";
        infoRow.style.display = "flex";
        infoRow.style.justifyContent = "space-between";
        infoRow.innerHTML = "<span>⚡ <b style='color:#89dceb;'>" + (events ? events.length : 0) + " Langkah</b></span>" +
                            "<span style='color:#a6adc8; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:60%;'>🌐 " + (domain || "Web Target") + "</span>";
        card.appendChild(infoRow);

        var descText = document.createElement("div");
        descText.innerText = "Penerima di WhatsApp/Telegram tinggal buka link ini, rekaman otomatis tercipta sama persis di webview mereka!";
        descText.style.fontSize = "11px";
        descText.style.color = "#a6adc8";
        descText.style.lineHeight = "1.4";
        descText.style.marginBottom = "16px";
        card.appendChild(descText);

        var btnCol = document.createElement("div");
        btnCol.style.display = "flex";
        btnCol.style.flexDirection = "column";
        btnCol.style.gap = "10px";

        // Mode 1: WhatsApp
        var waBtn = document.createElement("button");
        waBtn.innerHTML = "<span style='font-size:16px;'>💬</span> <b>Kirim via WhatsApp</b>";
        waBtn.style.display = "flex";
        waBtn.style.alignItems = "center";
        waBtn.style.justifyContent = "center";
        waBtn.style.gap = "10px";
        waBtn.style.background = "linear-gradient(135deg, #25D366 0%, #128C7E 100%)";
        waBtn.style.color = "#ffffff";
        waBtn.style.border = "none";
        waBtn.style.borderRadius = "12px";
        waBtn.style.padding = "12px 14px";
        waBtn.style.fontSize = "12px";
        waBtn.style.fontWeight = "700";
        waBtn.style.cursor = "pointer";
        waBtn.style.boxShadow = "0 4px 14px rgba(37, 211, 102, 0.3)";
        waBtn.onclick = function() {
            var msg = "🤖 *[Resep AGY] " + recipeName + "* (" + (events ? events.length : 0) + " Langkah)\n" +
                      (shareObj.payload.targetUrl ? ("🌐 Target: " + shareObj.payload.targetUrl + "\n\n") : "\n") +
                      "⚡ *Buka link ini di Eso Browser untuk auto-generate:*\n" + shareObj.url + "\n\n" +
                      "Atau jalankan di CLI:\nweb \"" + shareObj.code + "\"";
            window.open("https://api.whatsapp.com/send?text=" + encodeURIComponent(msg), "_blank");
        };
        btnCol.appendChild(waBtn);

        // Mode 2: Telegram
        var tgBtn = document.createElement("button");
        tgBtn.innerHTML = "<span style='font-size:16px;'>✈️</span> <b>Kirim via Telegram</b>";
        tgBtn.style.display = "flex";
        tgBtn.style.alignItems = "center";
        tgBtn.style.justifyContent = "center";
        tgBtn.style.gap = "10px";
        tgBtn.style.background = "linear-gradient(135deg, #229ED9 0%, #0088cc 100%)";
        tgBtn.style.color = "#ffffff";
        tgBtn.style.border = "none";
        tgBtn.style.borderRadius = "12px";
        tgBtn.style.padding = "12px 14px";
        tgBtn.style.fontSize = "12px";
        tgBtn.style.fontWeight = "700";
        tgBtn.style.cursor = "pointer";
        tgBtn.style.boxShadow = "0 4px 14px rgba(34, 158, 217, 0.3)";
        tgBtn.onclick = function() {
            var msg = "🤖 [Resep AGY] " + recipeName + " (" + (events ? events.length : 0) + " Langkah)\nCode Rekaman:\n" + shareObj.code;
            window.open("https://t.me/share/url?url=" + encodeURIComponent(shareObj.url) + "&text=" + encodeURIComponent(msg), "_blank");
        };
        btnCol.appendChild(tgBtn);

        // Mode 3: Copy Code Rekaman
        var copyBtn = document.createElement("button");
        copyBtn.innerHTML = "<span style='font-size:16px;'>📋</span> <b>Salin Code Rekaman (Copy Code)</b>";
        copyBtn.style.display = "flex";
        copyBtn.style.alignItems = "center";
        copyBtn.style.justifyContent = "center";
        copyBtn.style.gap = "10px";
        copyBtn.style.background = "#313244";
        copyBtn.style.color = "#cba6f7";
        copyBtn.style.border = "1px solid rgba(203, 166, 247, 0.35)";
        copyBtn.style.borderRadius = "12px";
        copyBtn.style.padding = "12px 14px";
        copyBtn.style.fontSize = "12px";
        copyBtn.style.fontWeight = "700";
        copyBtn.style.cursor = "pointer";
        copyBtn.onclick = function() {
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(shareObj.code);
            }
            updateAgyHUD("Code Rekaman Disalin!", "Tempel di chat atau jalankan `web <code_rekaman>` di CLI", "success");
            copyBtn.innerHTML = "<span style='font-size:16px;'>✔</span> <b>Code Berhasil Disalin!</b>";
            setTimeout(function() {
                copyBtn.innerHTML = "<span style='font-size:16px;'>📋</span> <b>Salin Code Rekaman (Copy Code)</b>";
            }, 2500);
        };
        btnCol.appendChild(copyBtn);

        card.appendChild(btnCol);
        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
    }

    function showImportCodeModal(onImported) {
        var existing = document.getElementById("__agy_import_code_modal__");
        if (existing) existing.remove();

        var modal = document.createElement("div");
        modal.id = "__agy_import_code_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.85)";
        modal.style.backdropFilter = "blur(8px)";
        modal.style.webkitBackdropFilter = "blur(8px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.alignItems = "center";
        modal.style.justifyContent = "center";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        modal.style.padding = "16px";
        modal.style.boxSizing = "border-box";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "1px solid rgba(137, 220, 235, 0.35)";
        card.style.borderRadius = "20px";
        card.style.width = "100%";
        card.style.maxWidth = "400px";
        card.style.padding = "22px 20px 20px 20px";
        card.style.boxShadow = "0 20px 50px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.boxSizing = "border-box";
        card.style.display = "flex";
        card.style.flexDirection = "column";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "12px";

        var title = document.createElement("div");
        title.innerHTML = "<span style='color:#89dceb;'>📥</span> <b>Masukkan Code Rekaman</b>";
        title.style.fontSize = "15px";
        title.style.color = "#cdd6f4";
        titleRow.appendChild(title);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function() { modal.remove(); };
        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        var subDesc = document.createElement("div");
        subDesc.innerText = "Tempel Code Rekaman (AGY-RECIPE:...) atau tautan dari pengirim di bawah ini:";
        subDesc.style.fontSize = "11px";
        subDesc.style.color = "#a6adc8";
        subDesc.style.marginBottom = "10px";
        card.appendChild(subDesc);

        var textInp = document.createElement("textarea");
        textInp.rows = 4;
        textInp.placeholder = "Tempel Code Rekaman (AGY-RECIPE:...) atau URL tautan di sini...";
        textInp.style.width = "100%";
        textInp.style.boxSizing = "border-box";
        textInp.style.background = "#11111b";
        textInp.style.border = "1px solid #45475a";
        textInp.style.borderRadius = "12px";
        textInp.style.padding = "10px";
        textInp.style.color = "#cdd6f4";
        textInp.style.fontSize = "11px";
        textInp.style.marginBottom = "10px";
        textInp.style.outline = "none";
        card.appendChild(textInp);

        // Tombol Paste Cepat
        var pasteBtn = document.createElement("button");
        pasteBtn.innerText = "📋 Tempel dari Clipboard";
        pasteBtn.style.background = "#1e1e2e";
        pasteBtn.style.color = "#89dceb";
        pasteBtn.style.border = "1px solid #313244";
        pasteBtn.style.borderRadius = "8px";
        pasteBtn.style.padding = "6px 10px";
        pasteBtn.style.fontSize = "11px";
        pasteBtn.style.fontWeight = "600";
        pasteBtn.style.cursor = "pointer";
        pasteBtn.style.marginBottom = "14px";
        pasteBtn.onclick = function() {
            if (navigator.clipboard && navigator.clipboard.readText) {
                navigator.clipboard.readText().then(function(t) {
                    if (t) textInp.value = t.trim();
                }).catch(function(){});
            }
        };
        card.appendChild(pasteBtn);

        var actionBtn = document.createElement("button");
        actionBtn.innerText = "⚡ BUAT REKAMAN SAMA";
        actionBtn.style.background = "linear-gradient(135deg, #a6e3a1 0%, #89dceb 100%)";
        actionBtn.style.color = "#11111b";
        actionBtn.style.border = "none";
        actionBtn.style.borderRadius = "12px";
        actionBtn.style.padding = "12px 0";
        actionBtn.style.fontSize = "12px";
        actionBtn.style.fontWeight = "800";
        actionBtn.style.cursor = "pointer";
        actionBtn.style.boxShadow = "0 4px 16px rgba(166, 227, 161, 0.35)";
        actionBtn.onclick = function() {
            var raw = textInp.value.trim();
            if (!raw) return;
            var decoded = decodeRecipeCode(raw);
            if (!decoded || !decoded.name || !Array.isArray(decoded.events) || decoded.events.length === 0) {
                alert("❌ Format Code Rekaman tidak valid!");
                return;
            }
            RecipeStore.save(decoded.name, decoded.events, decoded.targetUrl);
            modal.remove();
            updateAgyHUD("✨ Rekaman Berhasil Dibuat!", "Resep '" + decoded.name + "' (" + decoded.events.length + " langkah) siap diputar!", "success");
            if (typeof onImported === "function") {
                onImported(decoded);
            } else {
                showRunConfigModal(decoded.name, decoded.events);
            }
        };
        card.appendChild(actionBtn);

        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
        textInp.focus();
    }

    function checkAndAutoImportRecipeFromUrl() {
        try {
            var hash = window.location.hash || "";
            var search = window.location.search || "";
            var match = hash.match(/[#\?&]agy_(?:code|recipe)=([^&\s]+)/i) || search.match(/[\?&]agy_(?:code|recipe)=([^&\s]+)/i);
            if (match && match[1]) {
                var rawCode = match[1];
                var data = decodeRecipeCode(rawCode);
                if (data && data.name && Array.isArray(data.events) && data.events.length > 0) {
                    RecipeStore.save(data.name, data.events, data.targetUrl || window.location.href.split("#")[0]);
                    try {
                        var cleanUrl = window.location.pathname + (window.location.search.replace(/[\?&]agy_(?:code|recipe)=[^&]+/, "") || "");
                        window.history.replaceState({}, document.title, cleanUrl || window.location.href.split("#")[0]);
                    } catch(_) {}

                    setTimeout(function() {
                        updateAgyHUD("✨ Rekaman Dibuat Otomatis!", "Resep '" + data.name + "' (" + data.events.length + " langkah) dikloning dari pengirim!", "success");
                        showRunConfigModal(data.name, data.events);
                    }, 1000);
                }
            }
        } catch(e) {
            console.warn("[AgyAutoImport] Error:", e);
        }
    }

    // --- 4. ACCURATE CSS SELECTOR HELPER ---
    function getCssSelector(el) {
        if (!el || el.nodeType !== 1) return "";
        if (el.id && !el.id.startsWith("__agy_") && !el.id.startsWith("__fusion_") && !el.id.startsWith("sRecPill")) return "#" + el.id;
        if (el.getAttribute("data-testid")) return "[data-testid='" + el.getAttribute("data-testid") + "']";
        if (el.getAttribute("name")) return el.tagName.toLowerCase() + "[name='" + el.getAttribute("name") + "']";
        if (el.getAttribute("aria-label")) return el.tagName.toLowerCase() + "[aria-label='" + el.getAttribute("aria-label") + "']";

        var path = [];
        var cur = el;
        while (cur && cur.nodeType === 1) {
            var tag = cur.nodeName.toLowerCase();
            if (cur.id && !cur.id.startsWith("__agy_") && !cur.id.startsWith("__fusion_") && !cur.id.startsWith("sRecPill")) {
                path.unshift("#" + cur.id);
                break;
            } else {
                var sib = cur, nth = 1;
                while ((sib = sib.previousElementSibling)) {
                    if (sib.nodeName.toLowerCase() === tag) nth++;
                }
                path.unshift(tag + ":nth-of-type(" + nth + ")");
            }
            cur = cur.parentNode;
        }
        return path.join(" > ");
    }

    // --- 5. DEBOUNCED & UNIFIED LIVE RECORDER ---
    var AgyRecorder = {
        isRecording: false,
        recordMode: "dom", // "dom" | "touch" | "hybrid"
        sessionEvents: [],
        sessionName: "",
        keeperTimer: null,
        isBarCollapsed: false,

        getEvents: function() {
            if (Array.isArray(this.sessionEvents) && this.sessionEvents.length > 0) {
                window.__FUSION_MACRO_EVENTS__ = this.sessionEvents;
                return this.sessionEvents;
            }
            if (Array.isArray(window.__FUSION_MACRO_EVENTS__) && window.__FUSION_MACRO_EVENTS__.length > 0) {
                this.sessionEvents = window.__FUSION_MACRO_EVENTS__;
                return this.sessionEvents;
            }
            try {
                var raw = localStorage.getItem("__fusion_macro_events__");
                if (raw) {
                    var parsed = JSON.parse(raw);
                    if (Array.isArray(parsed) && parsed.length > 0) {
                        this.sessionEvents = parsed;
                        window.__FUSION_MACRO_EVENTS__ = parsed;
                        return this.sessionEvents;
                    }
                }
            } catch(_) {}
            if (!this.sessionEvents) this.sessionEvents = [];
            return this.sessionEvents;
        },

        startKeeper: function() {
            this.stopKeeper();
            var self = this;
            this.keeperTimer = setInterval(function() {
                if (!self.isRecording) {
                    self.stopKeeper();
                    return;
                }
                var bar = document.getElementById("__agy_rec_bar__");
                if (!bar) {
                    self.showRecordingHUD();
                }
            }, 1000);
        },

        stopKeeper: function() {
            if (this.keeperTimer) {
                clearInterval(this.keeperTimer);
                this.keeperTimer = null;
            }
        },

        start: function(name, mode) {
            // Tutup semua modal AGY yang masih ada di DOM agar layar dan sentuhan bersih
            var openModals = [
                "__agy_dom_inspector_modal__",
                "__agy_rec_mode_modal__",
                "__agy_recipe_modal__",
                "__agy_save_modal__",
                "__agy_run_config_modal__",
                "__agy_hub_modal__",
                "__agy_custom_db_modal__",
                "__agy_slash_picker__",
                "__agy_slash_backdrop__"
            ];
            openModals.forEach(function(mId) {
                var el = document.getElementById(mId);
                if (el) el.remove();
            });

            this.isRecording = true;
            this.recordMode = mode || "dom";
            this.sessionName = name || ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");
            this.sessionEvents = [];
            this.isBarCollapsed = false;
            window.__FUSION_RECORDING__ = true;
            window.__FUSION_MACRO_EVENTS__ = [];
            try {
                localStorage.removeItem("__fusion_macro_events__");
                sessionStorage.removeItem("__agy_rec_bar_collapsed__");
                sessionStorage.setItem("__agy_rec_active__", "true");
                sessionStorage.setItem("__agy_rec_mode__", this.recordMode);
                sessionStorage.setItem("__agy_rec_name__", this.sessionName);
            } catch(_) {}
            this.showRecordingHUD();
            this.startKeeper();
            this.updateUi();
            var modeLabel = (this.recordMode === "touch") ? "Touch / Gestur" : ((this.recordMode === "hybrid") ? "Touch + DOM (Hybrid)" : "Hanya DOM");
            updateAgyHUD("Mode Rekam Aktif (" + modeLabel + ")", "Lakukan aksi yang ingin direkam di web", "recording");
        },

        updateUi: function() {
            var count = this.getEvents().length;
            var btn = document.getElementById("__agy_dom_rec_btn__");
            var cancelBtn = document.getElementById("__agy_dom_rec_cancel_btn__");
            var status = document.getElementById("__agy_dom_rec_status__");
            if (btn) {
                if (this.isRecording) {
                    btn.innerText = "⏹️ Selesai (" + count + ")";
                    btn.style.background = "#2E7D32";
                    if (cancelBtn) cancelBtn.style.display = "inline-block";
                } else {
                    btn.innerText = "🔴 Mulai Rekam";
                    btn.style.background = "#FF3B30";
                    if (cancelBtn) cancelBtn.style.display = "none";
                }
            }
            if (status) {
                if (this.isRecording) {
                    var modeLabel = (this.recordMode === "touch") ? "Touch" : ((this.recordMode === "hybrid") ? "Hybrid" : ((this.recordMode === "step_by_step") ? "Step by Step" : "DOM"));
                    status.style.color = "#FF3B30";
                    status.innerText = "🔴 Mode Rekam (" + modeLabel + "): " + count + " aksi";
                } else {
                    status.style.color = "#A0A8BF";
                    status.innerText = "⚪ Rekam Alur Tidak Aktif";
                }
            }
            this.updateCounter();
            if (typeof window.__agy_render_dom_list__ === "function") {
                try { window.__agy_render_dom_list__(); } catch(_) {}
            }
        },

        promptModeAndStart: function(defaultName) {
            showRecordModePickerModal(defaultName);
        },

        stopAndPromptSave: function() {
            this.isRecording = false;
            window.__FUSION_RECORDING__ = false;
            this.stopKeeper();
            try {
                sessionStorage.removeItem("__agy_rec_active__");
                sessionStorage.removeItem("__agy_rec_mode__");
                sessionStorage.removeItem("__agy_rec_name__");
            } catch(_) {}
            this.hideRecordingHUD();
            this.updateUi();
            var hud = document.getElementById("__agy_copilot_hud__");
            if (hud) hud.remove();
            var events = this.getEvents().slice();
            try {
                localStorage.setItem("__fusion_macro_events__", JSON.stringify(events));
            } catch(_) {}
            showSaveRecipeModal(this.sessionName, events);
        },

        cancel: function() {
            this.isRecording = false;
            window.__FUSION_RECORDING__ = false;
            this.sessionEvents = [];
            window.__FUSION_MACRO_EVENTS__ = [];
            this.stopKeeper();
            try {
                sessionStorage.removeItem("__agy_rec_active__");
                sessionStorage.removeItem("__agy_rec_mode__");
                sessionStorage.removeItem("__agy_rec_name__");
                localStorage.removeItem("__fusion_macro_events__");
            } catch(_) {}
            this.hideRecordingHUD();
            this.updateUi();
            // Hapus HUD seketika tanpa delay agar layar bersih total dan langsung tersinkron
            var hud = document.getElementById("__agy_copilot_hud__");
            if (hud) hud.remove();
            try {
                if (window.GhostBridge && window.GhostBridge.notifyRecordingStep) {
                    window.GhostBridge.notifyRecordingStep(0);
                }
            } catch(_) {}
        },

        recordEvent: function(ev) {
            var isRec = this.isRecording || window.__FUSION_RECORDING__;
            if (!isRec) return;
            var now = Date.now();

            var list = this.getEvents();
            // Strict Debounce: Ignore duplicate actions on same element within 400ms
            var last = list[list.length - 1];
            if (last) {
                var diff = now - (last.timestamp || 0);
                if (diff < 400) {
                    if (last.selector === ev.selector || (last.type === ev.type && last.text === ev.text) || (last.type === "click" && ev.type === "click" && diff < 350)) {
                        console.log("[AgyRecorder] Debounced duplicate event:", ev.selector);
                        return;
                    }
                }
            }

            ev.timestamp = now;
            list.push(ev);
            this.sessionEvents = list;
            window.__FUSION_MACRO_EVENTS__ = list;
            try {
                localStorage.setItem("__fusion_macro_events__", JSON.stringify(list));
            } catch(_) {}
            this.updateCounter();
            this.updateUi();

            // Notifikasi HUD akurat untuk setiap langkah terekam
            var stepNum = list.length;
            var title = "Langkah " + stepNum + " Terekam";
            var detail = "";
            if (ev.type === "click") {
                var lbl = ev.text || ev.ariaLabel || ev.name || ev.id || ev.tag || "Elemen";
                detail = "Klik: " + lbl;
            } else if (ev.type === "fill") {
                var inName = ev.name || ev.placeholder || ev.id || "input";
                detail = "Isi " + inName + ": " + (ev.value || "");
            } else if (ev.type === "touch") {
                detail = "Sentuh: (" + Math.round(ev.x) + ", " + Math.round(ev.y) + ")";
            } else if (ev.type === "swipe") {
                detail = "Geser: " + (ev.direction || "arah");
            } else {
                detail = ev.type || "Aksi";
            }
            updateAgyHUD(title, detail, "recording_step");
        },

        showRecordingHUD: function() {
            var existing = document.getElementById("__agy_rec_bar__");
            if (existing) existing.remove();
            try {
                if (window.GhostBridge && window.GhostBridge.notifyRecordingState) {
                    window.GhostBridge.notifyRecordingState(true);
                }
            } catch(_) {}
            this.updateCounter();
        },

        updateCounter: function() {
            var count = this.getEvents().length;
            try {
                if (window.GhostBridge && window.GhostBridge.notifyRecordingStep) {
                    window.GhostBridge.notifyRecordingStep(count);
                }
            } catch(e) {}
        },

        hideRecordingHUD: function() {
            var bar = document.getElementById("__agy_rec_bar__");
            if (bar) bar.remove();
            var st = document.getElementById("__agy_rec_style__");
            if (st) st.remove();
            try {
                if (window.GhostBridge && window.GhostBridge.notifyRecordingState) {
                    window.GhostBridge.notifyRecordingState(false);
                }
            } catch(_) {}
        }
    };

    // --- HOOK NATIVE FUSION RECORDER CALLS WITH AGY RECORDER ---
    var origFusionStart = window.__FUSION_START_REC__;
    window.__FUSION_START_REC__ = function() {
        var res = origFusionStart ? origFusionStart() : "STARTED";
        AgyRecorder.isRecording = true;
        window.__FUSION_RECORDING__ = true;
        AgyRecorder.sessionEvents = window.__FUSION_MACRO_EVENTS__ || [];
        AgyRecorder.showRecordingHUD();
        AgyRecorder.updateUi();
        updateAgyHUD("Mode Rekam Aktif", "Klik tombol atau pilih elemen DOM untuk merekam", "recording");
        return res;
    };

    var origFusionStop = window.__FUSION_STOP_REC__;
    window.__FUSION_STOP_REC__ = function() {
        AgyRecorder.isRecording = false;
        window.__FUSION_RECORDING__ = false;
        AgyRecorder.hideRecordingHUD();
        AgyRecorder.updateUi();
        var res = origFusionStop ? origFusionStop() : JSON.stringify(AgyRecorder.sessionEvents);
        return res;
    };

    window.__FUSION_RECORD_ACTION__ = function(actionObj) {
        if (!actionObj) return;
        AgyRecorder.recordEvent(actionObj);
    };

    window.__FUSION_PLAY_MACRO__ = function(eventsJson) {
        var events = null;
        if (eventsJson && typeof eventsJson === 'string' && eventsJson !== "null" && eventsJson.length > 2) {
            try { events = JSON.parse(eventsJson); } catch(_) {}
        }
        if (!events || events.length === 0) {
            events = AgyRecorder.getEvents();
        }
        if (!events || events.length === 0) {
            updateAgyHUD("Resep Kosong", "Tidak ada langkah untuk dijalankan", "warning");
            return "No events";
        }
        AgyBatchRunner.run(events, AgyRecorder.sessionName || "Resep", "1", false, null);
        return "PLAYING";
    };

    // --- AUTO-RESTORE ACTIVE RECORDER ON PAGE NAVIGATION / RELOAD ---
    try {
        if (sessionStorage.getItem("__agy_rec_active__") === "true") {
            var restoredEvents = [];
            try {
                var rawEv = localStorage.getItem("__fusion_macro_events__");
                if (rawEv) restoredEvents = JSON.parse(rawEv);
            } catch(_) {}

            AgyRecorder.isRecording = true;
            AgyRecorder.recordMode = sessionStorage.getItem("__agy_rec_mode__") || "dom";
            AgyRecorder.sessionName = sessionStorage.getItem("__agy_rec_name__") || "Resep";
            AgyRecorder.sessionEvents = restoredEvents;
            window.__FUSION_RECORDING__ = true;
            window.__FUSION_MACRO_EVENTS__ = restoredEvents;

            setTimeout(function() {
                AgyRecorder.showRecordingHUD();
                AgyRecorder.startKeeper();
                AgyRecorder.updateCounter();
                updateAgyHUD("Mode Rekam Berlanjut", "Rekaman alur berlanjut (" + restoredEvents.length + " aksi)", "recording");
            }, 300);
        }
    } catch(_) {}

    // --- 6B. GESTURE & SWIPE RECORDER LISTENER (TOUCH & HYBRID MODES) ---
    var __agyTouchStartX = 0, __agyTouchStartY = 0, __agyTouchStartTime = 0;

    document.addEventListener("touchstart", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;
        var mode = AgyRecorder.recordMode;
        if (mode !== "touch" && mode !== "hybrid") return;

        // Ignore touches inside AGY UI
        if (e.target.closest && e.target.closest("#__agy_slash_picker__, #__agy_slash_backdrop__, #__agy_rec_bar__, #__agy_dom_inspector_modal__, #__agy_save_modal__, #__agy_copilot_hud__, #__agy_recipe_modal__, #__agy_rec_mode_modal__, #__agy_run_config_modal__, #__agy_hub_modal__, #__agy_custom_db_modal__")) {
            return;
        }

        var t = e.touches[0];
        if (!t) return;

        // Perlindungan mutlak: cegah touchstart terekam jika berada di dalam area bounding box floating bar!
        var recBarEl = document.getElementById("__agy_rec_bar__");
        if (recBarEl) {
            var rB = recBarEl.getBoundingClientRect();
            if (t.clientX >= (rB.left - 8) && t.clientX <= (rB.right + 8) &&
                t.clientY >= (rB.top - 8) && t.clientY <= (rB.bottom + 8)) {
                return;
            }
        }

        __agyTouchStartX = t.clientX;
        __agyTouchStartY = t.clientY;
        __agyTouchStartTime = Date.now();
    }, { passive: true });

    document.addEventListener("touchend", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;
        var mode = AgyRecorder.recordMode;
        if (mode !== "touch" && mode !== "hybrid") return;

        if (e.target.closest && e.target.closest("#__agy_slash_picker__, #__agy_slash_backdrop__, #__agy_rec_bar__, #__agy_dom_inspector_modal__, #__agy_save_modal__, #__agy_copilot_hud__, #__agy_recipe_modal__, #__agy_rec_mode_modal__, #__agy_run_config_modal__, #__agy_hub_modal__, #__agy_custom_db_modal__")) {
            return;
        }

        var t = e.changedTouches[0];
        if (!t) return;

        var recBarEl = document.getElementById("__agy_rec_bar__");
        if (recBarEl) {
            var rB = recBarEl.getBoundingClientRect();
            if (t.clientX >= (rB.left - 8) && t.clientX <= (rB.right + 8) &&
                t.clientY >= (rB.top - 8) && t.clientY <= (rB.bottom + 8)) {
                return;
            }
        }

        var deltaX = t.clientX - __agyTouchStartX;
        var deltaY = t.clientY - __agyTouchStartY;
        var elapsed = Date.now() - __agyTouchStartTime;

        var absX = Math.abs(deltaX);
        var absY = Math.abs(deltaY);

        // Hanya simpan jika usapan jempol signifikan (>= 40px)
        if (absX >= 40 || absY >= 40) {
            var dir = "down";
            if (absY >= absX) {
                dir = deltaY < 0 ? "up" : "down";
            } else {
                dir = deltaX < 0 ? "left" : "right";
            }

            var ev = {
                type: "swipe",
                direction: dir,
                deltaX: Math.round(deltaX),
                deltaY: Math.round(deltaY),
                startX: Math.round(__agyTouchStartX),
                startY: Math.round(__agyTouchStartY),
                endX: Math.round(t.clientX),
                endY: Math.round(t.clientY),
                duration: Math.min(Math.max(elapsed, 150), 700),
                timestamp: Date.now()
            };

            AgyRecorder.recordEvent(ev);
            updateAgyHUD("Swipe Direkam", dir.toUpperCase() + " (" + Math.round(Math.max(absX, absY)) + "px)", "recording");
        }
    }, { passive: true });

    // Helper fungsi untuk mendeteksi elemen internal UI AGY, Fusion, atau Native Pill
    function isInternalUI(el) {
        if (!el || el.nodeType !== 1) return false;
        var id = el.id || "";
        if (id.startsWith("__agy_") || id.startsWith("__fusion_") || id.startsWith("sRecPill")) return true;
        if (el.closest) {
            if (el.closest("[id^='__agy_'], [id^='__fusion_'], #sRecPill, .fusion-rec-supplementary-bar, [class*='fusion-ui'], [class*='agy-ui']")) {
                return true;
            }
        }
        return false;
    }

    // Helper fungsi pencari target interaktif DOM sejati (Support Button, Link, Div SPA, SVG Icon, dApp)
    function findBestClickTarget(el) {
        if (!el || el.nodeType !== 1) return null;
        if (isInternalUI(el)) return null;

        var tag = el.tagName.toLowerCase();
        if (tag === "html" || tag === "body") return null;

        // 1. Elemen kontrol standar atau link/tombol terdekat
        var standard = el.closest('button, a, input, select, textarea, label, summary, [role="button"], [role="link"], [role="tab"], [role="menuitem"], [role="checkbox"], [role="radio"], [role="switch"]');
        if (standard && !isInternalUI(standard)) {
            var stag = standard.tagName.toLowerCase();
            if (stag !== "html" && stag !== "body") return standard;
        }

        // 2. Elemen dengan explicit handler onclick, data-action, atau tabindex interaktif
        var actionParent = el.closest('[onclick], [data-action], [data-click], [tabindex]');
        if (actionParent && !isInternalUI(actionParent)) {
            var atag = actionParent.tagName.toLowerCase();
            if (atag !== "html" && atag !== "body") return actionParent;
        }

        // 3. Elemen tombol kustom / dApp SPA (kelas penanda tombol seperti btn, button, claim, mint, submit, cursor-pointer)
        var cur = el;
        var depth = 0;
        while (cur && cur.nodeType === 1 && depth < 6) {
            if (isInternalUI(cur)) return null;
            var ctag = cur.tagName.toLowerCase();
            if (ctag === "html" || ctag === "body") break;

            var cls = (typeof cur.className === "string") ? cur.className.toLowerCase() : "";
            if (cls.includes("btn") || cls.includes("button") || cls.includes("clickable") || cls.includes("action") || cls.includes("cursor-pointer") || cls.includes("claim") || cls.includes("mint") || cls.includes("connect")) {
                return cur;
            }

            try {
                var cs = window.getComputedStyle(cur);
                if (cs && cs.cursor === "pointer") {
                    return cur;
                }
            } catch(_) {}

            cur = cur.parentElement;
            depth++;
        }

        // 4. Jika el berupa child icon/grafis (path, svg, img, i), naik ke container pembungkusnya
        if (tag === "path" || tag === "svg" || tag === "img" || tag === "i") {
            var container = el.closest("button, a, div, span");
            if (container && !isInternalUI(container)) {
                var ctag2 = container.tagName.toLowerCase();
                if (ctag2 !== "html" && ctag2 !== "body") return container;
            }
        }

        // 5. Fallback elemen yang disentuh user (pastikan bukan layout raksasa / background penuh)
        var rect = el.getBoundingClientRect();
        var vw = window.innerWidth || (document.documentElement ? document.documentElement.clientWidth : 360);
        var vh = window.innerHeight || (document.documentElement ? document.documentElement.clientHeight : 640);
        if (rect.width > 2 && rect.height > 2 && (rect.width < (vw * 0.92) || rect.height < (vh * 0.92))) {
            return el;
        }

        return null;
    }

    var __lastAgyRecordedClickTime = 0;

    // --- 6. GLOBAL LIVE RECORDER LISTENER (CLICK & FORM INPUT) ---
    document.addEventListener("click", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;
        if (AgyRecorder.recordMode === "touch") return; // Mode Touch Murni tidak merekam DOM Click!

        // Ignore synthetic click from executeClickAction to avoid double counting!
        if (window.__AGY_SIMULATED_CLICK__) return;

        // While Slash Autocomplete picker is open, NEVER record any click!
        if (SlashAutocomplete.isOpen) {
            return;
        }

        // Strict Debounce: Cegah event ganda atau 4x lipat dalam rentang 400ms!
        var now = Date.now();
        if (now - __lastAgyRecordedClickTime < 400) {
            return;
        }

        // Jangan pernah rekam klik di elemen internal UI AGY / Fusion / sRecPill!
        if (isInternalUI(e.target)) return;

        // Anti-Bleed Bounding Box Check: Jika klik mendarat di area floating recorder bar, JANGAN PERNAH REKAM!
        var recBarEl = document.getElementById("__agy_rec_bar__");
        if (recBarEl) {
            var rB = recBarEl.getBoundingClientRect();
            if (e.clientX >= (rB.left - 8) && e.clientX <= (rB.right + 8) &&
                e.clientY >= (rB.top - 8) && e.clientY <= (rB.bottom + 8)) {
                return;
            }
        }

        // While any AGY modal is open, NEVER record clicks!
        if (document.getElementById("__agy_dom_inspector_modal__") ||
            document.getElementById("__agy_rec_mode_modal__") ||
            document.getElementById("__agy_recipe_modal__") ||
            document.getElementById("__agy_save_modal__") ||
            document.getElementById("__agy_run_config_modal__") ||
            document.getElementById("__agy_hub_modal__") ||
            document.getElementById("__agy_custom_db_modal__")) {
            return;
        }

        // If node is detached from document (e.g. re-rendered items), NEVER record!
        if (!document.documentElement.contains(e.target)) {
            return;
        }

        // Target resolution: cari elemen interaktif terbaik atau target klik itu sendiri
        var target = findBestClickTarget(e.target);
        if (!target) return;
        if (isInternalUI(target)) return;

        __lastAgyRecordedClickTime = now;

        var ev = {
            type: "click",
            timestamp: now,
            x: e.clientX,
            y: e.clientY,
            selector: getCssSelector(target),
            tag: target.tagName.toLowerCase(),
            id: target.id || "",
            name: target.name || "",
            testid: target.getAttribute("data-testid") || "",
            ariaLabel: target.getAttribute("aria-label") || "",
            text: (target.innerText || target.value || "").substring(0, 30).trim()
        };
        AgyRecorder.recordEvent(ev);
    }, true);

    document.addEventListener("change", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;
        if (AgyRecorder.recordMode === "touch") return; // Mode Touch Murni tidak merekam Form Fill!
        if (window.__AGY_SIMULATED_FILL__) return; // Prevent double recording from selectValue!
        if (SlashAutocomplete.isOpen) return; // In autocomplete mode, selectValue handles recording!

        var t = e.target;
        if (!t || (t.tagName !== "INPUT" && t.tagName !== "TEXTAREA" && t.tagName !== "SELECT")) return;
        if (t.closest("#__agy_slash_picker__, #__agy_slash_backdrop__, #__agy_dom_inspector_modal__, #__agy_save_modal__") || (t.id && t.id.startsWith("__agy_"))) return;
        if (!document.documentElement.contains(t)) return;

        var val = t.value || "";
        var activeW = AgyDB.getActiveWallet();
        var isWallet = activeW.address && (val.toLowerCase() === activeW.address.toLowerCase());
        var isEmail = val.includes("@") && (AgyDB.getEmails().includes(val) || val.endsWith("@termux.local"));

        var recordedVal = val;
        var isDynamic = false;
        if (isWallet) {
            recordedVal = "{{WALLET_ADDRESS}}";
            isDynamic = true;
        } else if (isEmail) {
            recordedVal = "{{EMAIL}}";
            isDynamic = true;
        }

        var ev = {
            type: "fill",
            timestamp: Date.now(),
            selector: getCssSelector(t),
            tag: t.tagName.toLowerCase(),
            id: t.id || "",
            name: t.name || "",
            placeholder: t.placeholder || "",
            testid: t.getAttribute("data-testid") || "",
            value: recordedVal,
            isDynamic: isDynamic
        };
        AgyRecorder.recordEvent(ev);
    }, true);

    // --- 7. IN-PAGE SLASH AUTOCOMPLETE PICKER (/email, /wallet, /query, /?) ---
    var SlashAutocomplete = {
        activeInput: null,
        isOpen: false,

        init: function() {
            var self = this;
            document.addEventListener("input", function(e) { self.checkTrigger(e.target); }, true);
            document.addEventListener("keyup", function(e) { self.checkTrigger(e.target); }, true);
        },

        checkTrigger: function(target) {
            if (!target || (target.tagName !== "INPUT" && target.tagName !== "TEXTAREA" && !target.isContentEditable)) {
                return;
            }
            if (target.type === "password" || target.type === "hidden" || target.type === "file") return;
            if (target.closest("#__agy_slash_picker__, #__agy_dom_inspector_modal__, #__agy_save_modal__") || (target.id && target.id.startsWith("__agy_"))) return;

            var val = target.value || target.innerText || "";
            if (val.endsWith("/") || val === "/" || /\/(email|wallet|address|query|\?)/i.test(val)) {
                this.activeInput = target;
                this.showPicker(target, val);
            }
        },

        showPicker: function(inputEl, currentVal) {
            var self = this;
            self.isOpen = true;

            // Remove existing instances cleanly
            var existingP = document.getElementById("__agy_slash_picker__");
            if (existingP) existingP.remove();
            var existingB = document.getElementById("__agy_slash_backdrop__");
            if (existingB) existingB.remove();

            function closePicker() {
                self.isOpen = false;
                var p = document.getElementById("__agy_slash_picker__");
                if (p) p.remove();
                var b = document.getElementById("__agy_slash_backdrop__");
                if (b) b.remove();
            }

            // 1. Transparent Backdrop Shield (Prevents any touch fallthrough / "tembus"!)
            var backdrop = document.createElement("div");
            backdrop.id = "__agy_slash_backdrop__";
            backdrop.style.position = "fixed";
            backdrop.style.top = "0";
            backdrop.style.left = "0";
            backdrop.style.width = "100vw";
            backdrop.style.height = "100vh";
            backdrop.style.zIndex = "2147483645";
            backdrop.style.background = "rgba(0, 0, 0, 0.001)";
            backdrop.style.touchAction = "none";
            backdrop.style.pointerEvents = "auto";

            var handleBackdropClose = function(e) {
                e.stopPropagation();
                e.preventDefault();
                closePicker();
            };
            backdrop.onclick = handleBackdropClose;
            backdrop.ontouchend = handleBackdropClose;

            // 2. Main Picker Card
            var picker = document.createElement("div");
            picker.id = "__agy_slash_picker__";
            picker.style.position = "fixed";
            picker.style.bottom = "75px";
            picker.style.left = "50%";
            picker.style.transform = "translateX(-50%)";
            picker.style.width = "92%";
            picker.style.maxWidth = "420px";
            picker.style.background = "#14161E";
            picker.style.border = "1px solid #2B3040";
            picker.style.borderRadius = "14px";
            picker.style.boxShadow = "0 12px 40px rgba(0, 0, 0, 0.9)";
            picker.style.zIndex = "2147483646";
            picker.style.padding = "12px";
            picker.style.color = "#FFFFFF";
            picker.style.fontFamily = "system-ui, -apple-system, sans-serif";
            picker.style.pointerEvents = "auto";

            // Prevent clicks and touches from bubbling out of picker to webpage
            picker.onclick = function(e) { e.stopPropagation(); };
            picker.ontouchstart = function(e) { e.stopPropagation(); };
            picker.ontouchend = function(e) { e.stopPropagation(); };
            picker.onmousedown = function(e) { e.stopPropagation(); };
            picker.onpointerdown = function(e) { e.stopPropagation(); };

            var header = document.createElement("div");
            header.style.display = "flex";
            header.style.justifyContent = "space-between";
            header.style.alignItems = "center";
            header.style.marginBottom = "10px";

            var tabRow = document.createElement("div");
            tabRow.style.display = "flex";
            tabRow.style.gap = "6px";

            var tabs = [
                { id: "email", label: "📧 /email" },
                { id: "wallet", label: "👛 /wallet" },
                { id: "query", label: "🔍 /query" },
                { id: "dom", label: "🎯 /?" }
            ];

            var activeCategory = "email";
            if (currentVal.includes("/wallet") || currentVal.includes("/address")) activeCategory = "wallet";
            else if (currentVal.includes("/query")) activeCategory = "query";
            else if (currentVal.includes("/?")) activeCategory = "dom";

            var contentBox = document.createElement("div");
            contentBox.style.maxHeight = "190px";
            contentBox.style.overflowY = "auto";
            contentBox.style.display = "flex";
            contentBox.style.flexDirection = "column";
            contentBox.style.gap = "6px";
            contentBox.style.webkitOverflowScrolling = "touch";

            // PERSISTENT TAB BUTTONS (Created ONCE, NEVER destroyed or recreated on tab switch!)
            var tabButtons = {};
            tabs.forEach(function(tb) {
                var btn = document.createElement("button");
                btn.type = "button";
                btn.id = "__agy_tab_" + tb.id + "__";
                btn.innerText = tb.label;
                btn.style.border = "none";
                btn.style.borderRadius = "8px";
                btn.style.padding = "6px 10px";
                btn.style.fontSize = "11px";
                btn.style.fontWeight = "bold";
                btn.style.cursor = "pointer";
                btn.style.userSelect = "none";
                btn.style.webkitUserSelect = "none";
                btn.style.touchAction = "manipulation";

                var activate = function(e) {
                    if (e) {
                        e.stopPropagation();
                        e.preventDefault();
                    }
                    if (activeCategory === tb.id) return;
                    activeCategory = tb.id;
                    updateTabVisuals();
                    renderContent();
                };

                btn.onclick = activate;
                btn.ontouchend = activate;

                tabButtons[tb.id] = btn;
                tabRow.appendChild(btn);
            });

            function updateTabVisuals() {
                tabs.forEach(function(tb) {
                    var b = tabButtons[tb.id];
                    if (tb.id === activeCategory) {
                        b.style.background = "#1E88E5";
                        b.style.color = "#FFFFFF";
                    } else {
                        b.style.background = "#202433";
                        b.style.color = "#A0A8BF";
                    }
                });
            }

            function selectValue(valToSet, displayLabel, isDynamic) {
                closePicker();
                if (!inputEl) return;
                inputEl.focus();

                var valStr = String(valToSet || "");
                var orig = (inputEl.value !== undefined) ? inputEl.value : (inputEl.innerText || "");
                // Hapus slash dan karakter trailing
                var clean = orig.replace(/\/[\w\?]*\s*$/i, "").replace(/\/+$/, "");
                var finalVal = (clean ? (clean + " ") : "") + valStr;
                finalVal = finalVal.trim();

                // 1. Reset React _valueTracker jika ada
                try {
                    if (inputEl._valueTracker) {
                        inputEl._valueTracker.setValue("");
                    }
                } catch(_) {}

                // 2. Set value via prototype setter sesuai tipe elemen
                try {
                    var isTA = inputEl instanceof window.HTMLTextAreaElement || inputEl.tagName === "TEXTAREA";
                    var proto = isTA ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;
                    var descriptor = Object.getOwnPropertyDescriptor(proto, "value");
                    if (descriptor && descriptor.set) {
                        descriptor.set.call(inputEl, finalVal);
                    }
                } catch(_) {}

                // 3. Set value langsung sebagai kepastian
                inputEl.value = finalVal;

                // 4. Jika contentEditable
                if (inputEl.isContentEditable) {
                    inputEl.innerText = finalVal;
                    inputEl.textContent = finalVal;
                }

                // 5. Dispatch input & change events agar form mendeteksi
                window.__AGY_SIMULATED_FILL__ = true;
                try {
                    inputEl.dispatchEvent(new InputEvent("input", { bubbles: true, cancelable: true, data: valStr, inputType: "insertReplacementText" }));
                } catch(_) {}
                try {
                    inputEl.dispatchEvent(new Event("input", { bubbles: true, cancelable: true }));
                    inputEl.dispatchEvent(new Event("change", { bubbles: true, cancelable: true }));
                } catch(_) {}
                setTimeout(function() {
                    window.__AGY_SIMULATED_FILL__ = false;
                }, 200);

                if (AgyRecorder.isRecording) {
                    AgyRecorder.recordEvent({
                        type: "fill",
                        timestamp: Date.now(),
                        selector: getCssSelector(inputEl),
                        tag: inputEl.tagName.toLowerCase(),
                        id: inputEl.id || "",
                        name: inputEl.name || "",
                        placeholder: inputEl.placeholder || "",
                        value: isDynamic ? valStr : finalVal,
                        isDynamic: isDynamic
                    });
                }

                updateAgyHUD("Form Terisi", displayLabel || valStr, "success");
            }

            function renderContent() {
                contentBox.innerHTML = "";

                if (activeCategory === "email") {
                    createItem("✨ {{EMAIL}} (Dinamis Akun Aktif)", "Otomatis sesuai akun yang diputar", function() {
                        var activeW = AgyDB.getActiveWallet();
                        var wName = (activeW && activeW.name) ? activeW.name : "Master";
                        var email = wName.toLowerCase().replace(/[^a-z0-9]/g, "") + "@termux.local";
                        selectValue(email, "Dinamis: " + email, true);
                    }, true);

                    var emails = AgyDB.getEmails();
                    emails.forEach(function(em) {
                        createItem("📧 " + em, "Dari /wallet/email.txt", function() {
                            selectValue(em, em, false);
                        });
                    });
                } else if (activeCategory === "wallet") {
                    createItem("✨ {{WALLET_ADDRESS}} (Dinamis Akun Aktif)", "Otomatis address dompet yang aktif di Termux", function() {
                        var activeW = AgyDB.getActiveWallet();
                        var wAddr = (activeW && activeW.address) ? activeW.address : "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359";
                        var wName = (activeW && activeW.name) ? activeW.name : "Master";
                        selectValue(wAddr, "Dinamis: " + wName, true);
                    }, true);

                    var wallets = AgyDB.getWallets();
                    wallets.forEach(function(w) {
                        createItem("👛 " + w.name + ": " + w.address.substring(0, 10) + "..." + w.address.substring(w.address.length - 4), w.address, function() {
                            selectValue(w.address, w.name, false);
                        });
                    });
                } else if (activeCategory === "query") {
                    var queries = AgyDB.getQueries();
                    queries.forEach(function(q) {
                        createItem("🔍 " + q.substring(0, 38) + (q.length > 38 ? "..." : ""), "Dari /wallet/query_db.json", function() {
                            selectValue(q, q, false);
                        });
                    });
                } else if (activeCategory === "dom") {
                    createItem("🎯 Summon DOM & HTML Picker", "Pilih elemen tombol/form langsung dari halaman web", function() {
                        closePicker();
                        showDomInspectorModal();
                    }, true);
                }
            }

            function createItem(title, subtitle, onClick, isSpecial) {
                var item = document.createElement("div");
                item.style.background = isSpecial ? "#1A2E3B" : "#1B1E29";
                item.style.border = isSpecial ? "1px solid #00E5FF" : "1px solid #2B3040";
                item.style.borderRadius = "8px";
                item.style.padding = "8px 10px";
                item.style.cursor = "pointer";
                item.style.display = "flex";
                item.style.flexDirection = "column";
                item.style.transition = "background 0.15s ease";
                item.style.userSelect = "none";
                item.style.webkitUserSelect = "none";
                item.style.touchAction = "manipulation";

                item.innerHTML = "<div style='font-size:12px; font-weight:600; color:" + (isSpecial ? "#00E5FF" : "#FFF") + ";'>" + title + "</div>" +
                                 "<div style='font-size:10px; opacity:0.75; margin-top:2px;'>" + subtitle + "</div>";

                item.onmouseover = function() { item.style.background = "#283044"; };
                item.onmouseout = function() { item.style.background = isSpecial ? "#1A2E3B" : "#1B1E29"; };

                var triggered = false;
                var handleSelect = function(e) {
                    if (e) {
                        e.stopPropagation();
                        if (e.cancelable) e.preventDefault();
                    }
                    if (triggered) return;
                    triggered = true;
                    setTimeout(function() { triggered = false; }, 300);
                    onClick();
                };

                item.addEventListener("touchend", handleSelect, { passive: false });
                item.onclick = handleSelect;

                contentBox.appendChild(item);
            }

            var closeX = document.createElement("span");
            closeX.innerText = "✕";
            closeX.style.cursor = "pointer";
            closeX.style.color = "#888";
            closeX.style.fontSize = "16px";
            closeX.style.padding = "4px 8px";
            closeX.style.userSelect = "none";
            closeX.style.webkitUserSelect = "none";

            var handleClose = function(e) {
                if (e) {
                    e.stopPropagation();
                    e.preventDefault();
                }
                closePicker();
            };
            closeX.onclick = handleClose;
            closeX.ontouchend = handleClose;

            header.appendChild(tabRow);
            header.appendChild(closeX);

            picker.appendChild(header);
            picker.appendChild(contentBox);

            updateTabVisuals();
            renderContent();

            var root = document.documentElement || document.body;
            root.appendChild(backdrop);
            root.appendChild(picker);
        }
    };

    SlashAutocomplete.init();

    // --- 8. MODAL SIMPAN RESEP ---
    // --- 8. MODAL SIMPAN RESEP (MODERN SLEEK CYBER UI) ---
    function showSaveRecipeModal(defaultName, events) {
        if (!events || events.length === 0) return;

        var existing = document.getElementById("__agy_save_modal__");
        if (existing) existing.remove();

        var modal = document.createElement("div");
        modal.id = "__agy_save_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.82)";
        modal.style.backdropFilter = "blur(8px)";
        modal.style.webkitBackdropFilter = "blur(8px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.alignItems = "center";
        modal.style.justifyContent = "center";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        modal.style.padding = "16px";
        modal.style.boxSizing = "border-box";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "1px solid rgba(203, 166, 247, 0.3)";
        card.style.borderRadius = "20px";
        card.style.padding = "22px 20px 20px 20px";
        card.style.width = "100%";
        card.style.maxWidth = "380px";
        card.style.color = "#cdd6f4";
        card.style.boxShadow = "0 20px 50px rgba(0,0,0,0.8), 0 0 25px rgba(203,166,247,0.12)";
        card.style.boxSizing = "border-box";
        card.style.display = "flex";
        card.style.flexDirection = "column";

        // Header with icon badge
        var header = document.createElement("div");
        header.style.display = "flex";
        header.style.alignItems = "center";
        header.style.gap = "12px";
        header.style.marginBottom = "14px";

        var iconBox = document.createElement("div");
        iconBox.innerHTML = "💾";
        iconBox.style.width = "40px";
        iconBox.style.height = "40px";
        iconBox.style.borderRadius = "12px";
        iconBox.style.background = "linear-gradient(135deg, rgba(203, 166, 247, 0.2) 0%, rgba(137, 220, 235, 0.15) 100%)";
        iconBox.style.border = "1px solid rgba(203, 166, 247, 0.35)";
        iconBox.style.display = "flex";
        iconBox.style.alignItems = "center";
        iconBox.style.justifyContent = "center";
        iconBox.style.fontSize = "20px";
        header.appendChild(iconBox);

        var titleBlock = document.createElement("div");
        titleBlock.style.flex = "1";

        var title = document.createElement("div");
        title.innerText = "Simpan Resep Otomasi";
        title.style.fontSize = "16px";
        title.style.fontWeight = "700";
        title.style.color = "#cdd6f4";
        title.style.letterSpacing = "-0.2px";
        titleBlock.appendChild(title);

        var subTitle = document.createElement("div");
        subTitle.innerText = "Alur interaksi siap diputar di semua wallet";
        subTitle.style.fontSize = "11px";
        subTitle.style.color = "#a6adc8";
        subTitle.style.marginTop = "2px";
        titleBlock.appendChild(subTitle);
        header.appendChild(titleBlock);

        card.appendChild(header);

        // Badge Info Step Card
        var badgeRow = document.createElement("div");
        badgeRow.style.display = "flex";
        badgeRow.style.gap = "8px";
        badgeRow.style.marginBottom = "14px";

        var stepBadge = document.createElement("div");
        stepBadge.innerHTML = "<span style='color:#a6e3a1; font-weight:bold;'>✓ " + events.length + " Langkah</span> terekam";
        stepBadge.style.flex = "1";
        stepBadge.style.background = "#1e1e2e";
        stepBadge.style.border = "1px solid #313244";
        stepBadge.style.borderRadius = "10px";
        stepBadge.style.padding = "7px 10px";
        stepBadge.style.fontSize = "11px";
        stepBadge.style.color = "#bac2de";
        stepBadge.style.textAlign = "center";
        badgeRow.appendChild(stepBadge);

        var modeBadge = document.createElement("div");
        modeBadge.innerHTML = "<span style='color:#cba6f7; font-weight:bold;'>Global</span> Database";
        modeBadge.style.flex = "1";
        modeBadge.style.background = "#1e1e2e";
        modeBadge.style.border = "1px solid #313244";
        modeBadge.style.borderRadius = "10px";
        modeBadge.style.padding = "7px 10px";
        modeBadge.style.fontSize = "11px";
        modeBadge.style.color = "#bac2de";
        modeBadge.style.textAlign = "center";
        badgeRow.appendChild(modeBadge);

        card.appendChild(badgeRow);

        // Input Label
        var lbl = document.createElement("div");
        lbl.innerText = "NAMA RESEP:";
        lbl.style.fontSize = "10px";
        lbl.style.fontWeight = "700";
        lbl.style.color = "#a6adc8";
        lbl.style.letterSpacing = "0.6px";
        lbl.style.marginBottom = "6px";
        card.appendChild(lbl);

        // Input Box
        var inp = document.createElement("input");
        inp.type = "text";
        inp.value = defaultName || ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");
        inp.placeholder = "Masukkan nama resep...";
        inp.style.width = "100%";
        inp.style.boxSizing = "border-box";
        inp.style.background = "#11111b";
        inp.style.border = "1px solid #45475a";
        inp.style.borderRadius = "12px";
        inp.style.color = "#cdd6f4";
        inp.style.padding = "11px 14px";
        inp.style.fontSize = "13px";
        inp.style.fontWeight = "500";
        inp.style.marginBottom = "14px";
        inp.style.outline = "none";
        inp.onfocus = function() { inp.style.borderColor = "#cba6f7"; };
        inp.onblur = function() { inp.style.borderColor = "#45475a"; };
        card.appendChild(inp);

        // Preview Steps Box (Mini summary of steps)
        var previewBox = document.createElement("div");
        previewBox.style.background = "#11111b";
        previewBox.style.border = "1px solid #313244";
        previewBox.style.borderRadius = "10px";
        previewBox.style.padding = "8px 12px";
        previewBox.style.maxHeight = "72px";
        previewBox.style.overflowY = "auto";
        previewBox.style.marginBottom = "16px";
        previewBox.style.fontSize = "11px";
        previewBox.style.color = "#a6adc8";

        var previewLines = [];
        for (var i = 0; i < Math.min(events.length, 3); i++) {
            var ev = events[i];
            var desc = ev.desc || (ev.type ? (ev.type.toUpperCase() + " " + (ev.selector || "")) : "Langkah " + (i+1));
            previewLines.push("<div style='margin-bottom:3px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;'><span style='color:#cba6f7; font-weight:600;'>" + (i+1) + ".</span> " + desc + "</div>");
        }
        if (events.length > 3) {
            previewLines.push("<div style='font-style:italic; opacity:0.7;'>... dan " + (events.length - 3) + " langkah lainnya</div>");
        }
        previewBox.innerHTML = previewLines.join("");
        card.appendChild(previewBox);

        // Button Group
        var btnRow = document.createElement("div");
        btnRow.style.display = "flex";
        btnRow.style.gap = "8px";

        var cancelBtn = document.createElement("button");
        cancelBtn.innerText = "Batal";
        cancelBtn.style.flex = "1";
        cancelBtn.style.background = "#313244";
        cancelBtn.style.color = "#bac2de";
        cancelBtn.style.border = "1px solid rgba(255,255,255,0.06)";
        cancelBtn.style.borderRadius = "12px";
        cancelBtn.style.padding = "11px 0";
        cancelBtn.style.fontSize = "12px";
        cancelBtn.style.fontWeight = "600";
        cancelBtn.style.cursor = "pointer";
        cancelBtn.onclick = function() { modal.remove(); };
        btnRow.appendChild(cancelBtn);

        var editBtn = document.createElement("button");
        editBtn.innerText = "✏️ Edit";
        editBtn.style.flex = "1";
        editBtn.style.background = "#24273a";
        editBtn.style.color = "#fab387";
        editBtn.style.border = "1px solid rgba(250, 179, 135, 0.3)";
        editBtn.style.borderRadius = "12px";
        editBtn.style.padding = "11px 0";
        editBtn.style.fontSize = "12px";
        editBtn.style.fontWeight = "600";
        editBtn.style.cursor = "pointer";
        editBtn.onclick = function() {
            var rName = inp.value.trim() || defaultName;
            modal.remove();
            showRecipeEditorModal(rName, events);
        };
        btnRow.appendChild(editBtn);

        var saveBtn = document.createElement("button");
        saveBtn.innerText = "💾 Simpan";
        saveBtn.style.flex = "1.5";
        saveBtn.style.background = "linear-gradient(135deg, #cba6f7 0%, #b4befe 100%)";
        saveBtn.style.color = "#11111b";
        saveBtn.style.border = "none";
        saveBtn.style.borderRadius = "12px";
        saveBtn.style.padding = "11px 0";
        saveBtn.style.fontSize = "12px";
        saveBtn.style.fontWeight = "700";
        saveBtn.style.boxShadow = "0 4px 14px rgba(203, 166, 247, 0.35)";
        saveBtn.style.cursor = "pointer";
        saveBtn.onclick = function() {
            var rName = inp.value.trim() || defaultName;
            var curUrl = (window.location && window.location.href) ? window.location.href.split("#")[0] : "";
            RecipeStore.save(rName, events, curUrl);
            updateAgyHUD("Resep Tersimpan", rName + " (" + events.length + " langkah)", "success");
            modal.remove();
        };
        btnRow.appendChild(saveBtn);
        card.appendChild(btnRow);

        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
        inp.focus();
    }

    // --- 9. ICON & SEMANTIC THESAURUS ---
    var IconThesaurus = {
        search: ["search", "explore", "cari", "pencarian", "kaca pembesar", "magnif", "find", "query", "zoom"],
        home: ["home", "beranda", "feed", "timeline", "utama", "rumah"],
        notifications: ["notification", "notifikasi", "bell", "lonceng", "alert", "pemberitahuan"],
        messages: ["message", "pesan", "chat", "dm", "inbox", "surat", "mail"],
        profile: ["profile", "profil", "akun", "account", "user", "avatar", "foto", "saya"],
        settings: ["setting", "pengaturan", "gear", "roda gigi", "opsi", "preference"],
        post: ["post", "tweet", "compose", "tulis", "buat", "tambah", "plus", "new", "kirim"],
        close: ["close", "tutup", "cancel", "batal", "silang", "exit", "dismiss", "x"],
        like: ["like", "suka", "love", "heart", "hati", "favorit"],
        share: ["share", "bagikan", "retweet", "repost", "teruskan"],
        bookmark: ["bookmark", "simpan", "markah"],
        menu: ["menu", "burger", "garis tiga", "drawer", "sidebar", "hamburger"]
    };

    function expandKeywords(rawKeyword) {
        var clean = (rawKeyword || "").toLowerCase().trim()
            .replace(/^(klik|pencet|tekan|buka|tombol|button|tap|pilih|jalankan|icon|ikon)\s+/g, "")
            .replace(/\s+(dong|bro|ya|sekarang|aja|lah)$/g, "")
            .trim();
        if (!clean) clean = (rawKeyword || "").toLowerCase().trim();

        var expanded = [clean];
        var keys = Object.keys(IconThesaurus);
        for (var i = 0; i < keys.length; i++) {
            var category = keys[i];
            var synonyms = IconThesaurus[category];
            var matched = synonyms.some(function(syn) {
                return clean.includes(syn) || syn.includes(clean);
            });
            if (matched) {
                synonyms.forEach(function(s) { expanded.push(s); });
                expanded.push(category);
            }
        }
        return Array.from(new Set(expanded));
    }

    // --- 10. ACTIVE DOM ELEMENT INSPECTOR & SCANNER ---
    function scanActiveElements() {
        var items = [];
        var elements = Array.from(document.querySelectorAll(
            "button, a, [role='button'], input, select, textarea, [role='tab'], [role='link'], [data-testid], [aria-label]"
        ));

        elements.forEach(function(el, idx) {
            if (el.offsetWidth === 0 && el.offsetHeight === 0) return;
            var text = (el.innerText || el.textContent || el.value || "").trim().substring(0, 32);
            var aria = (el.getAttribute("aria-label") || el.getAttribute("title") || "").trim();
            var testid = (el.getAttribute("data-testid") || "").trim();
            var placeholder = (el.getAttribute("placeholder") || "").trim();
            var href = (el.getAttribute("href") || "").trim();
            var tag = el.tagName.toLowerCase();

            var label = text || aria || testid || placeholder || (tag + (el.id ? ("#" + el.id) : ""));
            if (!label) return;

            items.push({
                index: idx,
                tag: tag,
                label: label,
                text: text,
                aria: aria,
                testid: testid,
                placeholder: placeholder,
                href: href,
                el: el
            });
        });
        return items;
    }

    // --- 11. HIGH-ACCURACY ELEMENT SEARCH ---
    function findInteractiveElement(keyword) {
        if (!keyword) return null;
        var searchTokens = expandKeywords(keyword);
        var scanned = scanActiveElements();
        var candidates = [];

        scanned.forEach(function(item) {
            var el = item.el;
            var combinedMeta = (item.text + " " + item.aria + " " + item.testid + " " + item.placeholder + " " + item.href).toLowerCase();

            for (var t = 0; t < searchTokens.length; t++) {
                var token = searchTokens[t];
                if (!token || token.length < 2) continue;

                if (combinedMeta.includes(token)) {
                    var score = 0;
                    if (item.testid.toLowerCase().includes(token)) score += 80;
                    if (item.aria.toLowerCase().includes(token)) score += 75;
                    if (item.href.toLowerCase().includes(token)) score += 60;
                    if (item.placeholder.toLowerCase().includes(token)) score += 55;
                    if (item.text.toLowerCase() === token) score += 50;
                    else if (item.text.toLowerCase().includes(token)) score += 30;

                    if (item.tag === "button" || item.tag === "a" || el.getAttribute("role") === "button") score += 25;
                    if (item.text.length > 50) score -= 30;

                    candidates.push({
                        el: el,
                        score: score,
                        label: item.aria || item.testid || item.text || item.placeholder || token
                    });
                    break;
                }
            }
        });

        if (candidates.length > 0) {
            candidates.sort(function(a, b) { return b.score - a.score; });
            var bestMatch = candidates[0].el;
            return bestMatch.closest("button, a, [role='button'], [role='tab']") || bestMatch;
        }
        return null;
    }

    // --- 12. MODAL INSPEKSI DOM DENGAN SMART ACTION ---
    function showDomInspectorModal(onCloseCallback) {
        var existing = document.getElementById("__agy_dom_inspector_modal__");
        if (existing) existing.remove();

        var scanned = scanActiveElements();

        if (window.GhostBridge && window.GhostBridge.onDrawerOpen) {
            try { window.GhostBridge.onDrawerOpen(); } catch(_) {}
        }

        var modal = document.createElement("div");
        modal.id = "__agy_dom_inspector_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.65)";
        modal.style.backdropFilter = "blur(6px)";
        modal.style.webkitBackdropFilter = "blur(6px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.justifyContent = "flex-start";
        modal.style.alignItems = "stretch";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "none";
        card.style.borderRight = "1px solid #313244";
        card.style.borderRadius = "0px";
        card.style.width = "82%";
        card.style.maxWidth = "380px";
        card.style.height = "100%";
        card.style.padding = "20px 16px 16px 16px";
        card.style.boxShadow = "10px 0 36px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.display = "flex";
        card.style.flexDirection = "column";
        card.style.boxSizing = "border-box";

        var handleCloseModal = function(e) {
            if (e && e.stopPropagation) e.stopPropagation();
            if (e && e.cancelable) e.preventDefault();
            window.__agy_render_dom_list__ = null;
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) {
                try { window.GhostBridge.onDrawerClose(); } catch(_) {}
            }
            if (typeof onCloseCallback === "function") {
                onCloseCallback();
            } else if (AgyRecorder.isRecording) {
                AgyRecorder.showRecordingHUD();
                AgyRecorder.updateCounter();
                AgyRecorder.updateUi();
            }
        };

        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                handleCloseModal(e);
            }
        });

        // Header Title Row
        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "8px";
        titleRow.style.flexShrink = "0";

        var titleBlock = document.createElement("div");
        var title = document.createElement("h3");
        title.innerText = "🔍 Inspeksi DOM & Tombol (" + scanned.length + ")";
        title.style.margin = "0";
        title.style.fontSize = "15px";
        title.style.fontWeight = "700";
        title.style.color = "#89dceb";
        titleBlock.appendChild(title);
        titleRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = handleCloseModal;
        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        // Topbar Mode Rekam
        var recBar = document.createElement("div");
        recBar.style.background = "#1B1F2D";
        recBar.style.border = "1px solid #2A3144";
        recBar.style.borderRadius = "8px";
        recBar.style.padding = "6px 10px";
        recBar.style.display = "flex";
        recBar.style.justifyContent = "space-between";
        recBar.style.alignItems = "center";
        recBar.style.marginBottom = "10px";
        recBar.style.flexShrink = "0";

        var recStatus = document.createElement("span");
        recStatus.id = "__agy_dom_rec_status__";
        recStatus.style.fontSize = "11px";
        var isCurrentlyRec = AgyRecorder.isRecording;
        var modeLabel = (AgyRecorder.recordMode === "touch") ? "Touch" : ((AgyRecorder.recordMode === "hybrid") ? "Hybrid" : "DOM");
        var curCount = AgyRecorder.getEvents().length;
        recStatus.style.color = isCurrentlyRec ? "#FF3B30" : "#A0A8BF";
        recStatus.innerText = isCurrentlyRec ? ("🔴 Mode Rekam (" + modeLabel + "): " + curCount + " aksi") : "⚪ Rekam Alur Tidak Aktif";
        recBar.appendChild(recStatus);

        var recBtnGroup = document.createElement("div");
        recBtnGroup.style.display = "flex";
        recBtnGroup.style.alignItems = "center";
        recBtnGroup.style.gap = "6px";

        var recCancelBtn = document.createElement("button");
        recCancelBtn.id = "__agy_dom_rec_cancel_btn__";
        recCancelBtn.innerText = "✕ Batal";
        recCancelBtn.style.background = "#424242";
        recCancelBtn.style.color = "#FFF";
        recCancelBtn.style.border = "none";
        recCancelBtn.style.borderRadius = "6px";
        recCancelBtn.style.padding = "4px 8px";
        recCancelBtn.style.fontSize = "11px";
        recCancelBtn.style.fontWeight = "bold";
        recCancelBtn.style.cursor = "pointer";
        recCancelBtn.style.display = isCurrentlyRec ? "inline-block" : "none";
        recCancelBtn.onclick = function(e) {
            handleCloseModal(e);
            AgyRecorder.cancel();
        };
        recBtnGroup.appendChild(recCancelBtn);

        var recBtn = document.createElement("button");
        recBtn.id = "__agy_dom_rec_btn__";
        recBtn.innerText = isCurrentlyRec ? "⏹️ Selesai" : "🔴 Mulai Rekam";
        recBtn.style.background = isCurrentlyRec ? "#2E7D32" : "#FF3B30";
        recBtn.style.color = "#FFF";
        recBtn.style.border = "none";
        recBtn.style.borderRadius = "6px";
        recBtn.style.padding = "4px 10px";
        recBtn.style.fontSize = "11px";
        recBtn.style.fontWeight = "bold";
        recBtn.style.cursor = "pointer";
        recBtn.onclick = function(e) {
            handleCloseModal(e);
            if (AgyRecorder.isRecording) {
                AgyRecorder.stopAndPromptSave();
            } else {
                showRecordModePickerModal();
            }
        };
        recBtnGroup.appendChild(recBtn);
        recBar.appendChild(recBtnGroup);
        card.appendChild(recBar);

        var filterInput = document.createElement("input");
        filterInput.type = "text";
        filterInput.placeholder = "Cari elemen (misal: search, button, input, claim)...";
        filterInput.style.width = "100%";
        filterInput.style.boxSizing = "border-box";
        filterInput.style.flexShrink = "0";
        filterInput.style.background = "#0B0C10";
        filterInput.style.border = "1px solid #333";
        filterInput.style.borderRadius = "8px";
        filterInput.style.color = "#FFF";
        filterInput.style.padding = "7px 10px";
        filterInput.style.fontSize = "12px";
        filterInput.style.marginBottom = "10px";
        filterInput.style.outline = "none";
        card.appendChild(filterInput);

        // Container Elemen: Full Flex-1 agar slider scrollview leluasa penuh!
        var listContainer = document.createElement("div");
        listContainer.style.flex = "1";
        listContainer.style.minHeight = "0";
        listContainer.style.overflowY = "auto";
        listContainer.style.marginBottom = "10px";

        function renderList(query) {
            listContainer.innerHTML = "";
            var q = (query || "").toLowerCase().trim();
            var filtered = scanned.filter(function(item) {
                if (!q) return true;
                var meta = (item.label + " " + item.aria + " " + item.testid + " " + item.placeholder + " " + item.tag).toLowerCase();
                return meta.includes(q);
            });

            if (filtered.length === 0) {
                var empty = document.createElement("div");
                empty.innerText = "Tidak ada elemen yang cocok.";
                empty.style.color = "#777";
                empty.style.fontSize = "12px";
                empty.style.padding = "16px 0";
                empty.style.textAlign = "center";
                listContainer.appendChild(empty);
                return;
            }

            var isRec = AgyRecorder.isRecording;

            filtered.slice(0, 50).forEach(function(item) {
                var row = document.createElement("div");
                row.style.display = "flex";
                row.style.alignItems = "center";
                row.style.justifyContent = "space-between";
                row.style.background = "#1B1E29";
                row.style.borderRadius = "6px";
                row.style.padding = "6px 8px";
                row.style.marginBottom = "5px";

                var info = document.createElement("div");
                info.style.overflow = "hidden";
                info.style.textOverflow = "ellipsis";
                info.style.whiteSpace = "nowrap";
                info.style.maxWidth = "55%";

                var tagBadge = "<span style='font-size:10px; background:#2A3142; color:#00E5FF; padding:2px 4px; border-radius:4px; margin-right:4px;'>" + item.tag.toUpperCase() + "</span>";
                info.innerHTML = tagBadge + "<span style='font-size:11px; font-weight:500;'>" + (item.label || item.tag) + "</span>";
                row.appendChild(info);

                var actionGroup = document.createElement("div");
                actionGroup.style.display = "flex";
                actionGroup.style.gap = "4px";

                var mainBtn = document.createElement("button");
                if (isRec) {
                    mainBtn.innerText = "🔴 Klik & Rekam";
                    mainBtn.style.background = "#FF3B30";
                } else {
                    mainBtn.innerText = "▶ Klik";
                    mainBtn.style.background = "#1E88E5";
                }
                mainBtn.style.color = "#FFF";
                mainBtn.style.border = "none";
                mainBtn.style.borderRadius = "4px";
                mainBtn.style.padding = "5px 10px";
                mainBtn.style.fontSize = "11px";
                mainBtn.style.fontWeight = "bold";
                mainBtn.style.cursor = "pointer";
                var hasRun = false;
                var doExec = function(e) {
                    if (hasRun) return;
                    hasRun = true;
                    handleCloseModal(e);
                    var isRecordingNow = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
                    executeClickAction(item.el, item.label, "DOM Inspector", isRecordingNow);
                };
                mainBtn.onclick = doExec;
                actionGroup.appendChild(mainBtn);

                row.onclick = function(e) {
                    if (e.target.tagName === "BUTTON") return;
                    doExec(e);
                };

                if (item.tag === "input" || item.tag === "textarea" || item.tag === "select") {
                    var fillBtn = document.createElement("button");
                    fillBtn.innerText = isRec ? "✍️ Rekam Isi" : "✍️ Isi";
                    fillBtn.style.background = "#F57C00";
                    fillBtn.style.color = "#FFF";
                    fillBtn.style.border = "none";
                    fillBtn.style.borderRadius = "4px";
                    fillBtn.style.padding = "5px 8px";
                    fillBtn.style.fontSize = "11px";
                    fillBtn.style.cursor = "pointer";
                    var doFill = function(e) {
                        handleCloseModal(e);
                        SlashAutocomplete.showPicker(item.el, "/");
                    };
                    fillBtn.onclick = doFill;
                    actionGroup.appendChild(fillBtn);
                }

                row.appendChild(actionGroup);
                listContainer.appendChild(row);
            });
        }

        renderList("");
        window.__agy_render_dom_list__ = function() { renderList(filterInput.value); };
        filterInput.oninput = function() { renderList(filterInput.value); };
        card.appendChild(listContainer);

        // Footer Row: Tombol Salin & Tombol Solid TUTUP di Paling Bawah Seperti Tab
        var footerArea = document.createElement("div");
        footerArea.style.flexShrink = "0";
        footerArea.style.marginTop = "auto";
        footerArea.style.display = "flex";
        footerArea.style.flexDirection = "column";
        footerArea.style.gap = "8px";

        var copyBtn = document.createElement("button");
        copyBtn.innerText = "📋 Salin DOM JSON";
        copyBtn.style.width = "100%";
        copyBtn.style.background = "#242A38";
        copyBtn.style.color = "#00E5FF";
        copyBtn.style.border = "none";
        copyBtn.style.borderRadius = "6px";
        copyBtn.style.padding = "8px";
        copyBtn.style.fontSize = "11px";
        copyBtn.style.fontWeight = "bold";
        copyBtn.style.cursor = "pointer";
        copyBtn.onclick = function(e) {
            e.stopPropagation();
            var summary = scanned.map(function(s) {
                return { tag: s.tag, label: s.label, aria: s.aria, testid: s.testid, placeholder: s.placeholder };
            });
            try {
                navigator.clipboard.writeText(JSON.stringify(summary, null, 2));
                updateAgyHUD("DOM Disalin", summary.length + " elemen disalin ke clipboard", "success");
            } catch(_) {}
        };
        footerArea.appendChild(copyBtn);

        var closeBtn = document.createElement("button");
        closeBtn.innerText = "TUTUP";
        closeBtn.style.width = "100%";
        closeBtn.style.background = "#313244";
        closeBtn.style.color = "#cdd6f4";
        closeBtn.style.border = "1px solid rgba(255,255,255,0.08)";
        closeBtn.style.borderRadius = "12px";
        closeBtn.style.padding = "11px";
        closeBtn.style.fontSize = "12px";
        closeBtn.style.fontWeight = "700";
        closeBtn.style.cursor = "pointer";
        closeBtn.style.textAlign = "center";
        closeBtn.onclick = handleCloseModal;
        footerArea.appendChild(closeBtn);

        card.appendChild(footerArea);
        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
        filterInput.focus();
    }

    // --- 13. ROBUST TOUCH & CLICK DISPATCHER (UNIFIED CLICK & RECORD) ---
    function executeClickAction(targetEl, label, promptStr, shouldRecord) {
        if (!targetEl) return false;

        var isRecordingNow = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        var doRecord = (shouldRecord === true) || (shouldRecord !== false && isRecordingNow);

        updateAgyHUD("Mengeksekusi", "Menekan: " + label + (doRecord ? " (Direkam)" : ""), "loading");

        var rect = targetEl.getBoundingClientRect();
        var x = rect.left + rect.width / 2;
        var y = rect.top + rect.height / 2;

        // Catat ke alur resep LANGSUNG seketika (sebelum timeout atau navigasi)!
        if (doRecord) {
            AgyRecorder.recordEvent({
                type: "click",
                timestamp: Date.now(),
                x: x,
                y: y,
                selector: getCssSelector(targetEl),
                tag: targetEl.tagName.toLowerCase(),
                id: targetEl.id || "",
                name: targetEl.name || "",
                testid: targetEl.getAttribute("data-testid") || "",
                ariaLabel: targetEl.getAttribute("aria-label") || "",
                text: label
            });
            updateAgyHUD("Aksi Terekam", label, "success");
        } else {
            updateAgyHUD("Berhasil Diklik", label, "success");
        }

        try {
            targetEl.scrollIntoView({ behavior: "smooth", block: "center" });
        } catch(_) {}

        var origOutline = targetEl.style.outline;
        targetEl.style.outline = "2px solid #00E5FF";
        setTimeout(function() { targetEl.style.outline = origOutline; }, 800);

        // Aktifkan pelindung agar event sintetis tidak ditangkap dobel oleh global listener
        window.__AGY_SIMULATED_CLICK__ = true;

        setTimeout(function() {
            var commonOpts = { bubbles: true, cancelable: true, clientX: x, clientY: y, view: window };

            if (targetEl.tagName === "INPUT" || targetEl.tagName === "TEXTAREA") {
                targetEl.focus();
            }

            // Mobile Touch Events
            if (window.TouchEvent) {
                try {
                    var touch = new Touch({
                        identifier: Date.now(),
                        target: targetEl,
                        clientX: x,
                        clientY: y,
                        screenX: x,
                        screenY: y,
                        pageX: x + window.scrollX,
                        pageY: y + window.scrollY
                    });
                    targetEl.dispatchEvent(new TouchEvent("touchstart", {
                        bubbles: true, cancelable: true, touches: [touch], targetTouches: [touch], changedTouches: [touch]
                    }));
                    targetEl.dispatchEvent(new TouchEvent("touchend", {
                        bubbles: true, cancelable: true, touches: [], targetTouches: [], changedTouches: [touch]
                    }));
                } catch(_) {}
            }

            // Pointer & Mouse Events
            if (window.PointerEvent) {
                try {
                    targetEl.dispatchEvent(new PointerEvent("pointerdown", commonOpts));
                    targetEl.dispatchEvent(new PointerEvent("pointerup", commonOpts));
                } catch(_) {}
            }
            try {
                targetEl.dispatchEvent(new MouseEvent("mousedown", commonOpts));
                targetEl.dispatchEvent(new MouseEvent("mouseup", commonOpts));
                targetEl.dispatchEvent(new MouseEvent("click", commonOpts));
            } catch(_) {}

            // Native click
            if (typeof targetEl.click === "function") {
                try { targetEl.click(); } catch(_) {}
            }

            // Lepaskan pelindung setelah animasi klik selesai
            setTimeout(function() {
                window.__AGY_SIMULATED_CLICK__ = false;
            }, 300);
        }, 120);

        return true;
    }

    // --- 14. FORM AUTO-FILL WITH DYNAMIC TEMPLATE SUPPORT ---
    function executeFormAutoFill() {
        updateAgyHUD("AGY Co-Pilot", "Mendeteksi form & akun aktif...", "loading");
        var activeW = AgyDB.getActiveWallet();
        var addr = activeW.address;
        var name = activeW.name;
        var email = AgyDB.getEmails()[0] || (name.toLowerCase().replace(/[^a-z0-9]/g, "") + "@termux.local");

        var inputs = Array.from(document.querySelectorAll("input, textarea, select")).filter(function(el) {
            return el.offsetWidth > 0 && el.offsetHeight > 0 && el.type !== "hidden";
        });

        if (inputs.length === 0) {
            updateAgyHUD("Form Tidak Ditemukan", "Tidak ada kolom input aktif", "error");
            return 0;
        }

        var filledCount = 0;
        inputs.forEach(function(inp, idx) {
            setTimeout(function() {
                var type = (inp.type || "text").toLowerCase();
                var placeholder = (inp.placeholder || "").toLowerCase();
                var idname = (inp.name || inp.id || "").toLowerCase();

                var valToSet = null;
                var isDynamic = false;
                if (type === "email" || placeholder.includes("email") || idname.includes("email")) {
                    valToSet = email;
                    isDynamic = true;
                } else if (placeholder.includes("address") || placeholder.includes("wallet") || placeholder.includes("0x") || idname.includes("address") || idname.includes("wallet")) {
                    if (addr) { valToSet = addr; isDynamic = true; }
                } else if (type === "text" && (placeholder.includes("name") || idname.includes("name") || placeholder.includes("user") || idname.includes("user"))) {
                    valToSet = name;
                } else if (type === "password" || placeholder.includes("password") || idname.includes("password")) {
                    valToSet = "TermuxAgy#2026!";
                }

                if (valToSet !== null) {
                    inp.focus();
                    var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                                 Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
                    if (setter) setter.call(inp, valToSet);
                    else inp.value = valToSet;

                    inp.dispatchEvent(new Event("input", { bubbles: true }));
                    inp.dispatchEvent(new Event("change", { bubbles: true }));
                    filledCount++;

                    if (AgyRecorder.isRecording) {
                        AgyRecorder.recordEvent({
                            type: "fill",
                            timestamp: Date.now(),
                            selector: getCssSelector(inp),
                            tag: inp.tagName.toLowerCase(),
                            value: isDynamic ? (type === "email" ? "{{EMAIL}}" : "{{WALLET_ADDRESS}}") : valToSet,
                            isDynamic: isDynamic
                        });
                    }
                }

                if (idx === inputs.length - 1) {
                    updateAgyHUD("Form Terisi", filledCount + " kolom berhasil diisi", "success");
                }
            }, idx * 120);
        });

        return inputs.length;
    }

    // --- 15. MODAL RECIPE PICKER, RUN CONFIG & AGY HUB (DATABASE & SETTINGS) ---

    // --- MODAL PILIHAN 3 METODE REKAM ALUR (DOM, TOUCH, HYBRID) ---
    function showRecordModePickerModal(recipeName) {
        var existing = document.getElementById("__agy_rec_mode_modal__");
        if (existing) existing.remove();

        if (window.GhostBridge && window.GhostBridge.onDrawerOpen) { try { window.GhostBridge.onDrawerOpen(); } catch(_) {} }

        var modal = document.createElement("div");
        modal.id = "__agy_rec_mode_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.65)";
        modal.style.backdropFilter = "blur(6px)";
        modal.style.webkitBackdropFilter = "blur(6px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.justifyContent = "flex-start";
        modal.style.alignItems = "stretch";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "none";
        card.style.borderRight = "1px solid #313244";
        card.style.borderRadius = "0px";
        card.style.width = "82%";
        card.style.maxWidth = "380px";
        card.style.height = "100%";
        card.style.padding = "20px 16px 16px 16px";
        card.style.boxShadow = "10px 0 36px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.display = "flex";
        card.style.flexDirection = "column";
        card.style.boxSizing = "border-box";

        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                e.stopPropagation();
                modal.remove();
                if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
                AgyRecorder.updateUi();
            }
        });

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "14px";
        titleRow.style.flexShrink = "0";

        var titleBlock = document.createElement("div");
        var title = document.createElement("h3");
        title.innerText = "🔴 Metode Rekam";
        title.style.margin = "0";
        title.style.fontSize = "16px";
        title.style.fontWeight = "700";
        title.style.color = "#f38ba8";
        titleBlock.appendChild(title);

        var subTitle = document.createElement("div");
        subTitle.innerText = "Pilih cara interaksi yang ingin dicatat";
        subTitle.style.fontSize = "11px";
        subTitle.style.color = "#a6adc8";
        subTitle.style.marginTop = "2px";
        titleBlock.appendChild(subTitle);
        titleRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            AgyRecorder.updateUi();
        };
        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        var scrollContainer = document.createElement("div");
        scrollContainer.style.flex = "1";
        scrollContainer.style.overflowY = "auto";
        scrollContainer.style.paddingRight = "4px";

        var nameLabel = document.createElement("label");
        nameLabel.innerText = "Nama Resep:";
        nameLabel.style.fontSize = "11px";
        nameLabel.style.fontWeight = "bold";
        nameLabel.style.color = "#cdd6f4";
        nameLabel.style.display = "block";
        nameLabel.style.marginBottom = "4px";
        scrollContainer.appendChild(nameLabel);

        var nameInp = document.createElement("input");
        nameInp.type = "text";
        nameInp.value = recipeName || ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");
        nameInp.style.width = "100%";
        nameInp.style.boxSizing = "border-box";
        nameInp.style.background = "#1e1e2e";
        nameInp.style.border = "1px solid #313244";
        nameInp.style.borderRadius = "8px";
        nameInp.style.padding = "8px 10px";
        nameInp.style.color = "#cdd6f4";
        nameInp.style.fontSize = "12px";
        nameInp.style.marginBottom = "14px";
        scrollContainer.appendChild(nameInp);

        var modeLabel = document.createElement("div");
        modeLabel.innerText = "Pilih Jenis Aksi yang Direkam:";
        modeLabel.style.fontSize = "11px";
        modeLabel.style.fontWeight = "bold";
        modeLabel.style.color = "#89dceb";
        modeLabel.style.marginBottom = "8px";
        scrollContainer.appendChild(modeLabel);

        function createModeOption(id, titleText, descText, isDefault, colorHex) {
            var optBox = document.createElement("div");
            optBox.style.background = "#1e1e2e";
            optBox.style.border = isDefault ? ("1px solid " + colorHex) : "1px solid #313244";
            optBox.style.borderRadius = "10px";
            optBox.style.padding = "10px";
            optBox.style.marginBottom = "8px";
            optBox.style.cursor = "pointer";
            optBox.style.transition = "border 0.2s ease";

            var radio = document.createElement("input");
            radio.type = "radio";
            radio.name = "__rec_mode__";
            radio.value = id;
            radio.checked = isDefault;
            radio.style.marginRight = "8px";

            var oTitle = document.createElement("span");
            oTitle.innerText = titleText;
            oTitle.style.fontWeight = "bold";
            oTitle.style.fontSize = "12px";
            oTitle.style.color = colorHex;

            var oDesc = document.createElement("div");
            oDesc.innerText = descText;
            oDesc.style.fontSize = "10px";
            oDesc.style.color = "#a6adc8";
            oDesc.style.marginTop = "3px";
            oDesc.style.marginLeft = "22px";

            optBox.appendChild(radio);
            optBox.appendChild(oTitle);
            optBox.appendChild(oDesc);

            optBox.onclick = function() {
                radio.checked = true;
                var allBoxes = card.querySelectorAll("[data-opt-box='1']");
                allBoxes.forEach(function(b) { b.style.border = "1px solid #313244"; });
                optBox.style.border = "1px solid " + colorHex;
            };
            optBox.setAttribute("data-opt-box", "1");
            return { box: optBox, radio: radio };
        }

        var optStep = createModeOption("step_by_step", "👣 Mode Step by Step (DOM + Smart)", "Ambil elemen DOM bertahap + Smart Action (Wallet, Email, Ref, Reload, Wipe, Fingerprint)", true, "#cba6f7");
        var optDom = createModeOption("dom", "🎯 Hanya Rekam DOM (Form & Web3)", "Mencatat klik tombol & input form presisi. Auto-token wallet & email.", false, "#89dceb");
        var optTouch = createModeOption("touch", "👆 Rekam Touch (Gestur Layar)", "Murni merekam sentuhan & usapan jempol (Swipe, Drag, Geser).", false, "#f9e2af");
        var optHybrid = createModeOption("hybrid", "⚡ Rekam Touch + DOM (Hybrid)", "Gabungan cerdas: tombol dicatat sebagai elemen DOM, sentuhan sebagai Touch.", false, "#a6e3a1");

        scrollContainer.appendChild(optStep.box);
        scrollContainer.appendChild(optDom.box);
        scrollContainer.appendChild(optTouch.box);
        scrollContainer.appendChild(optHybrid.box);
        card.appendChild(scrollContainer);

        var actionRow = document.createElement("div");
        actionRow.style.display = "flex";
        actionRow.style.gap = "8px";
        actionRow.style.marginTop = "14px";
        actionRow.style.flexShrink = "0";

        var cancelBtn = document.createElement("button");
        cancelBtn.innerText = "BATAL";
        cancelBtn.style.flex = "1";
        cancelBtn.style.background = "#313244";
        cancelBtn.style.color = "#cdd6f4";
        cancelBtn.style.border = "1px solid rgba(255,255,255,0.08)";
        cancelBtn.style.borderRadius = "12px";
        cancelBtn.style.padding = "11px";
        cancelBtn.style.fontSize = "12px";
        cancelBtn.style.fontWeight = "700";
        cancelBtn.style.cursor = "pointer";
        cancelBtn.onclick = function() {
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            AgyRecorder.updateUi();
        };
        actionRow.appendChild(cancelBtn);

        var startBtn = document.createElement("button");
        startBtn.innerText = "▶ MULAI";
        startBtn.style.flex = "1.5";
        startBtn.style.background = "linear-gradient(135deg, #f38ba8 0%, #fab387 100%)";
        startBtn.style.color = "#11111b";
        startBtn.style.border = "none";
        startBtn.style.borderRadius = "12px";
        startBtn.style.padding = "11px";
        startBtn.style.fontSize = "12px";
        startBtn.style.fontWeight = "700";
        startBtn.style.cursor = "pointer";
        startBtn.onclick = function() {
            var selectedMode = "step_by_step";
            if (optDom && optDom.radio && optDom.radio.checked) selectedMode = "dom";
            if (optTouch && optTouch.radio && optTouch.radio.checked) selectedMode = "touch";
            if (optHybrid && optHybrid.radio && optHybrid.radio.checked) selectedMode = "hybrid";
            var rName = nameInp.value.trim() || ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");
            modal.remove();
            AgyRecorder.start(rName, selectedMode);
        };
        actionRow.appendChild(startBtn);

        card.appendChild(actionRow);
        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
    }

    // Modal Konfigurasi Run (Targeted Run & Batch Selector)
    // Modal Konfigurasi Run (Targeted Run & Batch Selector) - MODERN SLEEK CATPPUCCIN UI
    function showRunConfigModal(recipeName, events) {
        var existing = document.getElementById("__agy_run_config_modal__");
        if (existing) existing.remove();

        var cfg = (typeof AgyConfig !== "undefined" && AgyConfig.get) ? AgyConfig.get() : { stepDelay: 1200, batchDelay: 3000 };
        var wallets = (typeof AgyDB !== "undefined" && AgyDB.getWallets) ? AgyDB.getWallets() : [];
        var allRecs = (typeof RecipeStore !== "undefined" && RecipeStore.getAll) ? RecipeStore.getAll() : {};
        var recObj = allRecs[recipeName] || {};
        var targetUrl = recObj.targetUrl || "";

        var modal = document.createElement("div");
        modal.id = "__agy_run_config_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.85)";
        modal.style.backdropFilter = "blur(8px)";
        modal.style.webkitBackdropFilter = "blur(8px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.alignItems = "center";
        modal.style.justifyContent = "center";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        modal.style.padding = "16px";
        modal.style.boxSizing = "border-box";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "1px solid rgba(137, 220, 235, 0.3)";
        card.style.borderRadius = "20px";
        card.style.width = "100%";
        card.style.maxWidth = "400px";
        card.style.padding = "22px 20px 20px 20px";
        card.style.boxShadow = "0 20px 50px rgba(0,0,0,0.8), 0 0 25px rgba(137, 220, 235, 0.12)";
        card.style.color = "#cdd6f4";
        card.style.boxSizing = "border-box";
        card.style.display = "flex";
        card.style.flexDirection = "column";

        // Header Row
        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "12px";

        var titleBlock = document.createElement("div");
        titleBlock.style.display = "flex";
        titleBlock.style.alignItems = "center";
        titleBlock.style.gap = "8px";

        var runIcon = document.createElement("span");
        runIcon.innerText = "▶";
        runIcon.style.color = "#a6e3a1";
        runIcon.style.fontSize = "16px";
        titleBlock.appendChild(runIcon);

        var title = document.createElement("div");
        title.innerText = "Putar Resep:";
        title.style.fontSize = "15px";
        title.style.fontWeight = "700";
        title.style.color = "#cdd6f4";
        titleBlock.appendChild(title);

        var recipeBadge = document.createElement("span");
        recipeBadge.innerText = recipeName;
        recipeBadge.style.fontSize = "12px";
        recipeBadge.style.fontWeight = "600";
        recipeBadge.style.color = "#cba6f7";
        recipeBadge.style.background = "#313244";
        recipeBadge.style.padding = "3px 8px";
        recipeBadge.style.borderRadius = "8px";
        titleBlock.appendChild(recipeBadge);
        titleRow.appendChild(titleBlock);

        // Header Action Group (Share & Close)
        var rightHeadGroup = document.createElement("div");
        rightHeadGroup.style.display = "flex";
        rightHeadGroup.style.alignItems = "center";
        rightHeadGroup.style.gap = "6px";

        var shareIconBtn = document.createElement("div");
        shareIconBtn.innerText = "📤";
        shareIconBtn.title = "Bagikan Rekaman Ini";
        shareIconBtn.style.cursor = "pointer";
        shareIconBtn.style.color = "#cba6f7";
        shareIconBtn.style.fontSize = "13px";
        shareIconBtn.style.width = "28px";
        shareIconBtn.style.height = "28px";
        shareIconBtn.style.borderRadius = "8px";
        shareIconBtn.style.display = "flex";
        shareIconBtn.style.alignItems = "center";
        shareIconBtn.style.justifyContent = "center";
        shareIconBtn.style.background = "#1e1e2e";
        shareIconBtn.onclick = function() {
            showShareRecipeModal(recipeName, events, targetUrl);
        };
        rightHeadGroup.appendChild(shareIconBtn);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "16px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function() { modal.remove(); };
        rightHeadGroup.appendChild(closeX);
        titleRow.appendChild(rightHeadGroup);
        card.appendChild(titleRow);

        // Stats row (Steps & Wallets)
        var statsRow = document.createElement("div");
        statsRow.style.display = "flex";
        statsRow.style.gap = "8px";
        statsRow.style.marginBottom = "10px";

        var sSteps = document.createElement("div");
        sSteps.innerHTML = "<span style='color:#89dceb; font-weight:700;'>⚡ " + (events ? events.length : 0) + "</span> Langkah";
        sSteps.style.flex = "1";
        sSteps.style.background = "#1e1e2e";
        sSteps.style.border = "1px solid #313244";
        sSteps.style.borderRadius = "10px";
        sSteps.style.padding = "8px 10px";
        sSteps.style.fontSize = "11px";
        sSteps.style.color = "#bac2de";
        sSteps.style.textAlign = "center";
        statsRow.appendChild(sSteps);

        var sWallets = document.createElement("div");
        sWallets.innerHTML = "<span style='color:#a6e3a1; font-weight:700;'>👛 " + wallets.length + "</span> Akun Dompet";
        sWallets.style.flex = "1";
        sWallets.style.background = "#1e1e2e";
        sWallets.style.border = "1px solid #313244";
        sWallets.style.borderRadius = "10px";
        sWallets.style.padding = "8px 10px";
        sWallets.style.fontSize = "11px";
        sWallets.style.color = "#bac2de";
        sWallets.style.textAlign = "center";
        statsRow.appendChild(sWallets);
        card.appendChild(statsRow);

        // Web Rekamannya Info Row
        if (targetUrl) {
            var domain = "";
            try { domain = new URL(targetUrl).hostname; } catch(_) { domain = targetUrl; }
            var urlRow = document.createElement("div");
            urlRow.style.display = "flex";
            urlRow.style.alignItems = "center";
            urlRow.style.justifyContent = "space-between";
            urlRow.style.background = "#11111b";
            urlRow.style.border = "1px solid #313244";
            urlRow.style.borderRadius = "10px";
            urlRow.style.padding = "7px 12px";
            urlRow.style.marginBottom = "12px";
            urlRow.style.fontSize = "11px";

            var urlLeft = document.createElement("div");
            urlLeft.style.display = "flex";
            urlLeft.style.alignItems = "center";
            urlLeft.style.gap = "6px";
            urlLeft.style.overflow = "hidden";
            urlLeft.innerHTML = "<span style='color:#a6adc8;'>🌐 Web:</span> <span style='color:#89dceb; font-weight:600; text-overflow:ellipsis; overflow:hidden; white-space:nowrap;'>" + (domain || targetUrl) + "</span>";
            urlRow.appendChild(urlLeft);

            var curDomain = "";
            try { curDomain = window.location.hostname; } catch(_) {}
            if (curDomain && domain && curDomain !== domain) {
                var goBtn = document.createElement("button");
                goBtn.innerText = "🌐 Buka Web Asli";
                goBtn.style.background = "#313244";
                goBtn.style.color = "#a6e3a1";
                goBtn.style.border = "1px solid rgba(166, 227, 161, 0.3)";
                goBtn.style.borderRadius = "6px";
                goBtn.style.padding = "4px 8px";
                goBtn.style.fontSize = "10px";
                goBtn.style.fontWeight = "700";
                goBtn.style.cursor = "pointer";
                goBtn.onclick = function() {
                    window.location.href = targetUrl;
                };
                urlRow.appendChild(goBtn);
            }
            card.appendChild(urlRow);
        }

        // Tri-Mode Segmented Control
        var currentRunMode = "multi"; // "multi" | "single" | "standalone"

        var modeSegment = document.createElement("div");
        modeSegment.style.display = "flex";
        modeSegment.style.background = "#11111b";
        modeSegment.style.border = "1px solid #313244";
        modeSegment.style.borderRadius = "12px";
        modeSegment.style.padding = "3px";
        modeSegment.style.marginBottom = "14px";
        modeSegment.style.gap = "4px";

        var btnMulti = document.createElement("button");
        var btnSingle = document.createElement("button");
        var btnStandalone = document.createElement("button");

        var updateModeTabs = function() {
            var tabs = [
                { btn: btnMulti, id: "multi", label: "🔄 Rotasi Tuyul" },
                { btn: btnSingle, id: "single", label: "🎯 Fokus 1 Wallet" },
                { btn: btnStandalone, id: "standalone", label: "🌐 Tanpa Wallet" }
            ];
            tabs.forEach(function(t) {
                var active = (currentRunMode === t.id);
                t.btn.innerText = t.label;
                t.btn.style.flex = "1";
                t.btn.style.border = "none";
                t.btn.style.borderRadius = "9px";
                t.btn.style.padding = "7px 3px";
                t.btn.style.fontSize = "10px";
                t.btn.style.fontWeight = active ? "700" : "500";
                t.btn.style.cursor = "pointer";
                t.btn.style.background = active ? "linear-gradient(135deg, rgba(203, 166, 247, 0.3) 0%, rgba(137, 220, 235, 0.25) 100%)" : "transparent";
                t.btn.style.color = active ? "#cdd6f4" : "#a6adc8";
                t.btn.style.boxShadow = active ? "0 2px 8px rgba(0,0,0,0.4)" : "none";
            });

            if (currentRunMode === "multi") {
                targetSection.style.display = "block";
                targetLabel.innerText = "TARGET AKUN DOMPET:";
                targetInp.placeholder = "1  atau  1-5  atau  1,3,4  atau  *";
                quickRowMulti.style.display = "flex";
                quickRowSingle.style.display = "none";
                loopSection.style.display = "none";
                batchCard.style.display = "flex";
                sWallets.innerHTML = "<span style='color:#a6e3a1; font-weight:700;'>👛 " + wallets.length + "</span> Akun Dompet";
            } else if (currentRunMode === "single") {
                targetSection.style.display = "block";
                targetLabel.innerText = "TARGET DOMPET (FOKUS 1 AKUN):";
                targetInp.placeholder = "Nomor Akun (contoh: 1)";
                quickRowMulti.style.display = "none";
                quickRowSingle.style.display = "flex";
                loopSection.style.display = "block";
                batchCard.style.display = "none";
                sWallets.innerHTML = "<span style='color:#cba6f7; font-weight:700;'>🎯 Fokus</span> 1 Dompet";
            } else {
                targetSection.style.display = "none";
                quickRowMulti.style.display = "none";
                quickRowSingle.style.display = "none";
                loopSection.style.display = "block";
                batchCard.style.display = "none";
                sWallets.innerHTML = "<span style='color:#89dceb; font-weight:700;'>🌐 Standalone</span> (Tanpa Wallet)";
            }
        };

        btnMulti.onclick = function() { currentRunMode = "multi"; updateModeTabs(); };
        btnSingle.onclick = function() { currentRunMode = "single"; updateModeTabs(); };
        btnStandalone.onclick = function() { currentRunMode = "standalone"; updateModeTabs(); };

        modeSegment.appendChild(btnMulti);
        modeSegment.appendChild(btnSingle);
        modeSegment.appendChild(btnStandalone);
        card.appendChild(modeSegment);

        // Target Input Section
        var targetSection = document.createElement("div");
        var targetLabel = document.createElement("div");
        targetLabel.innerText = "TARGET AKUN DOMPET:";
        targetLabel.style.fontSize = "10px";
        targetLabel.style.fontWeight = "700";
        targetLabel.style.color = "#a6adc8";
        targetLabel.style.letterSpacing = "0.6px";
        targetLabel.style.marginBottom = "6px";
        targetSection.appendChild(targetLabel);

        var targetInp = document.createElement("input");
        targetInp.type = "text";
        targetInp.value = "1";
        targetInp.placeholder = "1  atau  1-5  atau  1,3,4  atau  *";
        targetInp.style.width = "100%";
        targetInp.style.boxSizing = "border-box";
        targetInp.style.background = "#11111b";
        targetInp.style.border = "1px solid #45475a";
        targetInp.style.borderRadius = "12px";
        targetInp.style.padding = "10px 14px";
        targetInp.style.color = "#cdd6f4";
        targetInp.style.fontSize = "13px";
        targetInp.style.fontWeight = "500";
        targetInp.style.marginBottom = "8px";
        targetInp.style.outline = "none";
        targetInp.onfocus = function() { targetInp.style.borderColor = "#89dceb"; };
        targetInp.onblur = function() { targetInp.style.borderColor = "#45475a"; };
        targetSection.appendChild(targetInp);

        // Quick Preset Chips (Multi)
        var quickRowMulti = document.createElement("div");
        quickRowMulti.style.display = "flex";
        quickRowMulti.style.gap = "6px";
        quickRowMulti.style.marginBottom = "12px";

        var makeChip = function(lbl, val) {
            var chip = document.createElement("button");
            chip.innerText = lbl;
            chip.style.flex = "1";
            chip.style.background = "#1e1e2e";
            chip.style.border = "1px solid #313244";
            chip.style.color = "#cdd6f4";
            chip.style.borderRadius = "8px";
            chip.style.padding = "6px 4px";
            chip.style.fontSize = "11px";
            chip.style.fontWeight = "600";
            chip.style.cursor = "pointer";
            chip.onclick = function() { targetInp.value = val; };
            return chip;
        };

        quickRowMulti.appendChild(makeChip("Akun #1", "1"));
        quickRowMulti.appendChild(makeChip("1 - " + Math.max(1, Math.min(5, wallets.length)), "1-" + Math.max(1, Math.min(5, wallets.length))));
        quickRowMulti.appendChild(makeChip("Semua (*)", "*"));
        targetSection.appendChild(quickRowMulti);

        // Quick Preset Chips (Single)
        var quickRowSingle = document.createElement("div");
        quickRowSingle.style.display = "none";
        quickRowSingle.style.gap = "6px";
        quickRowSingle.style.marginBottom = "12px";
        quickRowSingle.appendChild(makeChip("Akun #1", "1"));
        if (wallets.length >= 2) quickRowSingle.appendChild(makeChip("Akun #2", "2"));
        if (wallets.length >= 3) quickRowSingle.appendChild(makeChip("Akun #3", "3"));
        targetSection.appendChild(quickRowSingle);

        card.appendChild(targetSection);

        // Loop Count Section (For Single & Standalone Modes)
        var loopSection = document.createElement("div");
        loopSection.style.marginBottom = "14px";
        loopSection.style.display = "none";

        var loopLabel = document.createElement("div");
        loopLabel.innerText = "JUMLAH PUTARAN (LOOP COUNT):";
        loopLabel.style.fontSize = "10px";
        loopLabel.style.fontWeight = "700";
        loopLabel.style.color = "#a6adc8";
        loopLabel.style.letterSpacing = "0.6px";
        loopLabel.style.marginBottom = "6px";
        loopSection.appendChild(loopLabel);

        var loopInp = document.createElement("input");
        loopInp.type = "number";
        loopInp.value = "5";
        loopInp.style.width = "100%";
        loopInp.style.boxSizing = "border-box";
        loopInp.style.background = "#11111b";
        loopInp.style.border = "1px solid #45475a";
        loopInp.style.borderRadius = "12px";
        loopInp.style.padding = "10px 14px";
        loopInp.style.color = "#cdd6f4";
        loopInp.style.fontSize = "13px";
        loopInp.style.fontWeight = "600";
        loopInp.style.marginBottom = "8px";
        loopInp.style.outline = "none";
        loopInp.onfocus = function() { loopInp.style.borderColor = "#cba6f7"; };
        loopInp.onblur = function() { loopInp.style.borderColor = "#45475a"; };
        loopSection.appendChild(loopInp);

        var loopChipsRow = document.createElement("div");
        loopChipsRow.style.display = "flex";
        loopChipsRow.style.gap = "6px";

        var makeLoopChip = function(lbl, val) {
            var chip = document.createElement("button");
            chip.innerText = lbl;
            chip.style.flex = "1";
            chip.style.background = "#1e1e2e";
            chip.style.border = "1px solid #313244";
            chip.style.color = "#cdd6f4";
            chip.style.borderRadius = "8px";
            chip.style.padding = "6px 2px";
            chip.style.fontSize = "10px";
            chip.style.fontWeight = "600";
            chip.style.cursor = "pointer";
            chip.onclick = function() { loopInp.value = val; };
            return chip;
        };

        loopChipsRow.appendChild(makeLoopChip("1x", "1"));
        loopChipsRow.appendChild(makeLoopChip("5x", "5"));
        loopChipsRow.appendChild(makeLoopChip("10x", "10"));
        loopChipsRow.appendChild(makeLoopChip("25x", "25"));
        loopChipsRow.appendChild(makeLoopChip("♾️ Non-stop", "999999"));
        loopSection.appendChild(loopChipsRow);
        card.appendChild(loopSection);

        // Auto-Rotate Loop Section
        var batchCard = document.createElement("label");
        batchCard.style.display = "flex";
        batchCard.style.alignItems = "center";
        batchCard.style.justifyContent = "space-between";
        batchCard.style.background = "#11111b";
        batchCard.style.border = "1px solid #313244";
        batchCard.style.borderRadius = "12px";
        batchCard.style.padding = "10px 14px";
        batchCard.style.marginBottom = "14px";
        batchCard.style.cursor = "pointer";

        var batchTextCol = document.createElement("div");
        var bTitle = document.createElement("div");
        bTitle.innerText = "Auto-Rotate Loop Dompet";
        bTitle.style.fontSize = "12px";
        bTitle.style.fontWeight = "600";
        bTitle.style.color = "#cdd6f4";
        batchTextCol.appendChild(bTitle);

        var bSub = document.createElement("div");
        bSub.innerText = "Switch akun otomatis setelah tiap alur selesai";
        bSub.style.fontSize = "10px";
        bSub.style.color = "#a6adc8";
        batchTextCol.appendChild(bSub);
        batchCard.appendChild(batchTextCol);

        var batchCb = document.createElement("input");
        batchCb.type = "checkbox";
        batchCb.checked = true;
        batchCb.style.width = "18px";
        batchCb.style.height = "18px";
        batchCb.style.accentColor = "#a6e3a1";
        batchCb.style.cursor = "pointer";
        batchCard.appendChild(batchCb);
        card.appendChild(batchCard);

        updateModeTabs(); // Initialize mode view

        // Timing Note Pill
        var timingNote = document.createElement("div");
        timingNote.innerHTML = "⚡ Jeda step: <span style='color:#cba6f7;'>" + (cfg.stepDelay / 1000).toFixed(1) + "s</span> • Jeda batch: <span style='color:#89dceb;'>" + (cfg.batchDelay / 1000).toFixed(1) + "s</span>";
        timingNote.style.fontSize = "11px";
        timingNote.style.color = "#a6adc8";
        timingNote.style.marginBottom = "16px";
        timingNote.style.textAlign = "center";
        card.appendChild(timingNote);

        // Action Buttons Row
        var actionRow = document.createElement("div");
        actionRow.style.display = "flex";
        actionRow.style.gap = "8px";

        var cancelBtn = document.createElement("button");
        cancelBtn.innerText = "Batal";
        cancelBtn.style.flex = "1";
        cancelBtn.style.background = "#313244";
        cancelBtn.style.color = "#bac2de";
        cancelBtn.style.border = "1px solid rgba(255,255,255,0.06)";
        cancelBtn.style.borderRadius = "12px";
        cancelBtn.style.padding = "11px 0";
        cancelBtn.style.fontSize = "12px";
        cancelBtn.style.fontWeight = "600";
        cancelBtn.style.cursor = "pointer";
        cancelBtn.onclick = function() { modal.remove(); };
        actionRow.appendChild(cancelBtn);

        var startBtn = document.createElement("button");
        startBtn.innerText = "▶ GAS JALANKAN";
        startBtn.style.flex = "1.8";
        startBtn.style.background = "linear-gradient(135deg, #a6e3a1 0%, #89dceb 100%)";
        startBtn.style.color = "#11111b";
        startBtn.style.border = "none";
        startBtn.style.borderRadius = "12px";
        startBtn.style.padding = "11px 0";
        startBtn.style.fontSize = "12px";
        startBtn.style.fontWeight = "800";
        startBtn.style.letterSpacing = "0.3px";
        startBtn.style.boxShadow = "0 4px 16px rgba(166, 227, 161, 0.35)";
        startBtn.style.cursor = "pointer";
        startBtn.onclick = function() {
            var targetExpr = targetInp.value.trim() || "1";
            var isBatch = batchCb.checked;
            var lCount = parseInt(loopInp.value, 10);
            if (isNaN(lCount) || lCount <= 0) lCount = 1;

            modal.remove();
            if (typeof AgyBatchRunner !== "undefined" && AgyBatchRunner.run) {
                AgyBatchRunner.run(events, recipeName, targetExpr, isBatch, null, currentRunMode, lCount);
            } else {
                replayEventsSequentially(events, recipeName);
            }
        };
        actionRow.appendChild(startBtn);

        card.appendChild(actionRow);
        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
    }

    // Modal Recipe Manager (DRAWER KIRI MODERN CATPPUCCIN UI)
    function showRecipePickerModal() {
        var existing = document.getElementById("__agy_recipe_modal__");
        if (existing) existing.remove();

        var recipes = RecipeStore.getAll();
        var keys = Object.keys(recipes);
        if (window.GhostBridge && window.GhostBridge.onDrawerOpen) { try { window.GhostBridge.onDrawerOpen(); } catch(_) {} }

        var modal = document.createElement("div");
        modal.id = "__agy_recipe_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.65)";
        modal.style.backdropFilter = "blur(6px)";
        modal.style.webkitBackdropFilter = "blur(6px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.justifyContent = "flex-start";
        modal.style.alignItems = "stretch";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "none";
        card.style.borderRight = "1px solid #313244";
        card.style.borderRadius = "0px";
        card.style.width = "82%";
        card.style.maxWidth = "380px";
        card.style.height = "100%";
        card.style.padding = "20px 16px 16px 16px";
        card.style.boxShadow = "10px 0 36px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.display = "flex";
        card.style.flexDirection = "column";
        card.style.boxSizing = "border-box";

        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                e.stopPropagation();
                modal.remove();
                if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            }
        });

        // Header Title Row
        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "4px";

        var titleBlock = document.createElement("div");
        var title = document.createElement("h3");
        title.innerText = "📋 Kelola Resep AGY";
        title.style.margin = "0";
        title.style.fontSize = "16px";
        title.style.fontWeight = "700";
        title.style.color = "#cba6f7";
        titleBlock.appendChild(title);

        var subTitle = document.createElement("div");
        subTitle.innerText = "Database Resep Otomasi Global";
        subTitle.style.fontSize = "11px";
        subTitle.style.color = "#a6adc8";
        subTitle.style.marginTop = "2px";
        titleBlock.appendChild(subTitle);
        titleRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
        };
        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        // Action Quick Bar
        var topBtnRow = document.createElement("div");
        topBtnRow.style.display = "flex";
        topBtnRow.style.gap = "6px";
        topBtnRow.style.marginTop = "14px";
        topBtnRow.style.marginBottom = "14px";

        var addRecipeBtn = document.createElement("button");
        addRecipeBtn.innerText = "➕ Rekam";
        addRecipeBtn.style.flex = "1.1";
        addRecipeBtn.style.background = "linear-gradient(135deg, rgba(203, 166, 247, 0.2) 0%, rgba(137, 220, 235, 0.15) 100%)";
        addRecipeBtn.style.color = "#cba6f7";
        addRecipeBtn.style.border = "1px solid rgba(203, 166, 247, 0.4)";
        addRecipeBtn.style.borderRadius = "10px";
        addRecipeBtn.style.padding = "9px 8px";
        addRecipeBtn.style.fontSize = "12px";
        addRecipeBtn.style.fontWeight = "700";
        addRecipeBtn.style.cursor = "pointer";
        addRecipeBtn.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            showRecordModePickerModal("Resep_Baru");
        };
        topBtnRow.appendChild(addRecipeBtn);

        var importBtn = document.createElement("button");
        importBtn.innerText = "📥 Code";
        importBtn.style.flex = "0.9";
        importBtn.style.background = "#1e1e2e";
        importBtn.style.color = "#89dceb";
        importBtn.style.border = "1px solid #313244";
        importBtn.style.borderRadius = "10px";
        importBtn.style.padding = "9px 8px";
        importBtn.style.fontSize = "12px";
        importBtn.style.fontWeight = "700";
        importBtn.style.cursor = "pointer";
        importBtn.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            showImportCodeModal(function() {
                showRecipePickerModal();
            });
        };
        topBtnRow.appendChild(importBtn);

        var hubBtn = document.createElement("button");
        hubBtn.innerText = "⚙️ Hub";
        hubBtn.style.flex = "0.7";
        hubBtn.style.background = "#1e1e2e";
        hubBtn.style.color = "#fab387";
        hubBtn.style.border = "1px solid #313244";
        hubBtn.style.borderRadius = "10px";
        hubBtn.style.padding = "9px 8px";
        hubBtn.style.fontSize = "12px";
        hubBtn.style.fontWeight = "600";
        hubBtn.style.cursor = "pointer";
        hubBtn.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            showAgyHubModal();
        };
        topBtnRow.appendChild(hubBtn);
        card.appendChild(topBtnRow);

        // Recipe List Scroll Container
        var list = document.createElement("div");
        list.style.flex = "1";
        list.style.minHeight = "0";
        list.style.overflowY = "auto";
        list.style.marginBottom = "14px";
        list.style.paddingRight = "4px";

        if (keys.length === 0) {
            var empty = document.createElement("div");
            empty.style.background = "#1e1e2e";
            empty.style.border = "1px dashed #313244";
            empty.style.borderRadius = "14px";
            empty.style.padding = "24px 16px";
            empty.style.textAlign = "center";
            empty.style.marginTop = "10px";

            var emptyIcon = document.createElement("div");
            emptyIcon.innerHTML = "📜";
            emptyIcon.style.fontSize = "28px";
            emptyIcon.style.marginBottom = "8px";
            empty.appendChild(emptyIcon);

            var emptyTitle = document.createElement("div");
            emptyTitle.innerText = "Belum Ada Resep Tersimpan";
            emptyTitle.style.fontSize = "13px";
            emptyTitle.style.fontWeight = "700";
            emptyTitle.style.color = "#cdd6f4";
            emptyTitle.style.marginBottom = "4px";
            empty.appendChild(emptyTitle);

            var emptySub = document.createElement("div");
            emptySub.innerText = "Klik '+ Rekam Resep Baru' di atas atau tekan tombol rekam di pojok kiri bawah untuk merekam alur dApp.";
            emptySub.style.fontSize = "11px";
            emptySub.style.color = "#a6adc8";
            emptySub.style.lineHeight = "1.4";
            empty.appendChild(emptySub);

            list.appendChild(empty);
        } else {
            keys.forEach(function(k) {
                var rec = recipes[k] || {};
                var evts = rec.events || [];

                var item = document.createElement("div");
                item.style.background = "#1e1e2e";
                item.style.border = "1px solid #313244";
                item.style.borderRadius = "12px";
                item.style.padding = "12px 14px";
                item.style.marginBottom = "10px";
                item.style.display = "flex";
                item.style.flexDirection = "column";
                item.style.gap = "8px";

                var itemHeader = document.createElement("div");
                itemHeader.style.display = "flex";
                itemHeader.style.justifyContent = "space-between";
                itemHeader.style.alignItems = "center";

                var nameCol = document.createElement("div");
                nameCol.style.flex = "1";
                nameCol.style.overflow = "hidden";

                var nameSpan = document.createElement("div");
                nameSpan.innerText = k;
                nameSpan.style.fontSize = "13px";
                nameSpan.style.fontWeight = "700";
                nameSpan.style.color = "#cdd6f4";
                nameSpan.style.whiteSpace = "nowrap";
                nameSpan.style.overflow = "hidden";
                nameSpan.style.textOverflow = "ellipsis";
                nameCol.appendChild(nameSpan);

                var badgeSpan = document.createElement("span");
                var rDomain = "";
                if (rec.targetUrl) {
                    try { rDomain = new URL(rec.targetUrl).hostname; } catch(_) { rDomain = rec.targetUrl; }
                }
                var domainHtml = rDomain ? (" • <span style='color:#89dceb;'>🌐 " + rDomain + "</span>") : "";
                badgeSpan.innerHTML = "⚡ " + evts.length + " langkah" + domainHtml;
                badgeSpan.style.fontSize = "10px";
                badgeSpan.style.fontWeight = "600";
                badgeSpan.style.color = "#a6e3a1";
                badgeSpan.style.background = "#11111b";
                badgeSpan.style.padding = "2px 6px";
                badgeSpan.style.borderRadius = "6px";
                badgeSpan.style.marginTop = "2px";
                badgeSpan.style.display = "inline-block";
                nameCol.appendChild(badgeSpan);
                itemHeader.appendChild(nameCol);

                item.appendChild(itemHeader);

                // Action buttons row for this recipe
                var btnGroup = document.createElement("div");
                btnGroup.style.display = "flex";
                btnGroup.style.gap = "6px";
                btnGroup.style.marginTop = "4px";

                var runBtn = document.createElement("button");
                runBtn.innerText = "▶ Putar";
                runBtn.style.flex = "1.3";
                runBtn.style.background = "linear-gradient(135deg, #a6e3a1 0%, #89dceb 100%)";
                runBtn.style.color = "#11111b";
                runBtn.style.border = "none";
                runBtn.style.borderRadius = "8px";
                runBtn.style.padding = "7px 8px";
                runBtn.style.fontSize = "11px";
                runBtn.style.fontWeight = "700";
                runBtn.style.cursor = "pointer";
                runBtn.onclick = function(e) {
                    e.stopPropagation();
                    modal.remove();
                    if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
                    showRunConfigModal(k, evts);
                };
                btnGroup.appendChild(runBtn);

                var shareBtn = document.createElement("button");
                shareBtn.innerText = "📤";
                shareBtn.title = "Bagikan Rekaman Ini";
                shareBtn.style.flex = "0.6";
                shareBtn.style.background = "#313244";
                shareBtn.style.color = "#cba6f7";
                shareBtn.style.border = "1px solid rgba(203, 166, 247, 0.3)";
                shareBtn.style.borderRadius = "8px";
                shareBtn.style.padding = "7px 6px";
                shareBtn.style.fontSize = "11px";
                shareBtn.style.cursor = "pointer";
                shareBtn.onclick = function(e) {
                    e.stopPropagation();
                    showShareRecipeModal(k, evts, rec.targetUrl);
                };
                btnGroup.appendChild(shareBtn);

                if (rec.targetUrl) {
                    var curD = "";
                    try { curD = window.location.hostname; } catch(_) {}
                    if (rDomain && curD && curD !== rDomain) {
                        var openWebBtn = document.createElement("button");
                        openWebBtn.innerText = "🌐";
                        openWebBtn.title = "Buka Web Rekaman: " + rec.targetUrl;
                        openWebBtn.style.flex = "0.6";
                        openWebBtn.style.background = "#313244";
                        openWebBtn.style.color = "#89dceb";
                        openWebBtn.style.border = "1px solid rgba(137, 220, 235, 0.3)";
                        openWebBtn.style.borderRadius = "8px";
                        openWebBtn.style.padding = "7px 6px";
                        openWebBtn.style.fontSize = "11px";
                        openWebBtn.style.cursor = "pointer";
                        openWebBtn.onclick = function(e) {
                            e.stopPropagation();
                            window.location.href = rec.targetUrl;
                        };
                        btnGroup.appendChild(openWebBtn);
                    }
                }

                var editBtn = document.createElement("button");
                editBtn.innerText = "✏️";
                editBtn.title = "Edit Resep";
                editBtn.style.flex = "0.6";
                editBtn.style.background = "#313244";
                editBtn.style.color = "#fab387";
                editBtn.style.border = "1px solid rgba(250, 179, 135, 0.25)";
                editBtn.style.borderRadius = "8px";
                editBtn.style.padding = "7px 6px";
                editBtn.style.fontSize = "11px";
                editBtn.style.fontWeight = "600";
                editBtn.style.cursor = "pointer";
                editBtn.onclick = function(e) {
                    e.stopPropagation();
                    modal.remove();
                    if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
                    showRecipeEditorModal(k, evts);
                };
                btnGroup.appendChild(editBtn);

                var delBtn = document.createElement("button");
                delBtn.innerText = "🗑️";
                delBtn.style.flex = "0.5";
                delBtn.style.background = "rgba(243, 139, 168, 0.15)";
                delBtn.style.color = "#f38ba8";
                delBtn.style.border = "1px solid rgba(243, 139, 168, 0.3)";
                delBtn.style.borderRadius = "8px";
                delBtn.style.padding = "7px 6px";
                delBtn.style.fontSize = "11px";
                delBtn.style.cursor = "pointer";
                delBtn.onclick = function(e) {
                    e.stopPropagation();
                    RecipeStore.remove(k);
                    item.remove();
                    if (Object.keys(RecipeStore.getAll()).length === 0) {
                        showRecipePickerModal();
                    }
                };
                btnGroup.appendChild(delBtn);

                item.appendChild(btnGroup);
                list.appendChild(item);
            });
        }
        card.appendChild(list);

        var closeBtn = document.createElement("button");
        closeBtn.innerText = "TUTUP";
        closeBtn.style.width = "100%";
        closeBtn.style.background = "#313244";
        closeBtn.style.color = "#cdd6f4";
        closeBtn.style.border = "1px solid rgba(255,255,255,0.08)";
        closeBtn.style.borderRadius = "12px";
        closeBtn.style.padding = "11px";
        closeBtn.style.fontSize = "12px";
        closeBtn.style.fontWeight = "700";
        closeBtn.style.cursor = "pointer";
        closeBtn.style.textAlign = "center";
        closeBtn.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
        };
        card.appendChild(closeBtn);

        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
    }

    // Modal Terpadu: AGY Hub (Database & Settings)
    function showAgyHubModal(initialTab) {
        var existing = document.getElementById("__agy_hub_modal__");
        if (existing) existing.remove();
        if (window.GhostBridge && window.GhostBridge.onDrawerOpen) { try { window.GhostBridge.onDrawerOpen(); } catch(_) {} }

        var cfg = AgyConfig.get();
        var currentTab = initialTab === "settings" ? "settings" : "database";

        var modal = document.createElement("div");
        modal.id = "__agy_hub_modal__";
        modal.style.position = "fixed";
        modal.style.top = "0";
        modal.style.left = "0";
        modal.style.width = "100%";
        modal.style.height = "100%";
        modal.style.backgroundColor = "rgba(17, 17, 27, 0.65)";
        modal.style.backdropFilter = "blur(6px)";
        modal.style.webkitBackdropFilter = "blur(6px)";
        modal.style.zIndex = "2147483647";
        modal.style.display = "flex";
        modal.style.justifyContent = "flex-start";
        modal.style.alignItems = "stretch";
        modal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        modal.addEventListener("click", function(e) {
            if (e.target === modal) {
                e.stopPropagation();
                modal.remove();
                if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            }
        });

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "none";
        card.style.borderRight = "1px solid #313244";
        card.style.borderRadius = "0px";
        card.style.width = "82%";
        card.style.maxWidth = "380px";
        card.style.height = "100%";
        card.style.padding = "20px 16px 16px 16px";
        card.style.boxShadow = "10px 0 36px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.display = "flex";
        card.style.flexDirection = "column";
        card.style.boxSizing = "border-box";

        var headerRow = document.createElement("div");
        headerRow.style.display = "flex";
        headerRow.style.justifyContent = "space-between";
        headerRow.style.alignItems = "center";
        headerRow.style.marginBottom = "14px";
        headerRow.style.flexShrink = "0";

        var titleBlock = document.createElement("div");
        var hTitle = document.createElement("h3");
        hTitle.innerText = "🗄️ AGY Hub";
        hTitle.style.margin = "0";
        hTitle.style.fontSize = "16px";
        hTitle.style.fontWeight = "700";
        hTitle.style.color = "#f9e2af";
        titleBlock.appendChild(hTitle);

        var subTitle = document.createElement("div");
        subTitle.innerText = "Database & Pengaturan Global";
        subTitle.style.fontSize = "11px";
        subTitle.style.color = "#a6adc8";
        subTitle.style.marginTop = "2px";
        titleBlock.appendChild(subTitle);
        headerRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
        };
        headerRow.appendChild(closeX);
        card.appendChild(headerRow);

        // Tab Navigation
        var tabRow = document.createElement("div");
        tabRow.style.display = "flex";
        tabRow.style.borderBottom = "1px solid #313244";
        tabRow.style.marginBottom = "14px";
        tabRow.style.flexShrink = "0";

        var tabDb = document.createElement("button");
        tabDb.innerText = "🗄️ Database";
        tabDb.style.flex = "1";
        tabDb.style.background = "transparent";
        tabDb.style.border = "none";
        tabDb.style.padding = "8px";
        tabDb.style.fontSize = "12px";
        tabDb.style.fontWeight = "bold";
        tabDb.style.cursor = "pointer";

        var tabSettings = document.createElement("button");
        tabSettings.innerText = "⚙️ Settings";
        tabSettings.style.flex = "1";
        tabSettings.style.background = "transparent";
        tabSettings.style.border = "none";
        tabSettings.style.padding = "8px";
        tabSettings.style.fontSize = "12px";
        tabSettings.style.fontWeight = "bold";
        tabSettings.style.cursor = "pointer";

        tabRow.appendChild(tabDb);
        tabRow.appendChild(tabSettings);
        card.appendChild(tabRow);

        var tabContent = document.createElement("div");
        tabContent.style.flex = "1";
        tabContent.style.overflowY = "auto";
        tabContent.style.paddingRight = "4px";
        card.appendChild(tabContent);

        function renderTab() {
            tabContent.innerHTML = "";
            if (currentTab === "database") {
                tabDb.style.color = "#FFD54F";
                tabDb.style.borderBottom = "2px solid #FFD54F";
                tabSettings.style.color = "#888";
                tabSettings.style.borderBottom = "none";

                // Section Folder Database
                var fLbl = document.createElement("label");
                fLbl.innerText = "📁 Folder Database:";
                fLbl.style.fontSize = "11px";
                fLbl.style.fontWeight = "bold";
                fLbl.style.display = "block";
                fLbl.style.marginBottom = "4px";
                tabContent.appendChild(fLbl);

                // Quick Presets
                var presetRow = document.createElement("div");
                presetRow.style.display = "flex";
                presetRow.style.gap = "5px";
                presetRow.style.marginBottom = "6px";

                var pChroot = document.createElement("button");
                pChroot.innerText = "Chroot";
                pChroot.style.flex = "1";
                pChroot.style.background = "#242A38";
                pChroot.style.color = "#FFF";
                pChroot.style.border = "none";
                pChroot.style.borderRadius = "4px";
                pChroot.style.padding = "4px";
                pChroot.style.fontSize = "10px";
                pChroot.onclick = function() { folderInp.value = "/root/wallet/"; };
                presetRow.appendChild(pChroot);

                var pTermux = document.createElement("button");
                pTermux.innerText = "Termux";
                pTermux.style.flex = "1";
                pTermux.style.background = "#242A38";
                pTermux.style.color = "#FFF";
                pTermux.style.border = "none";
                pTermux.style.borderRadius = "4px";
                pTermux.style.padding = "4px";
                pTermux.style.fontSize = "10px";
                pTermux.onclick = function() { folderInp.value = "/data/data/com.termux/files/home/"; };
                presetRow.appendChild(pTermux);

                var pSdcard = document.createElement("button");
                pSdcard.innerText = "SDCard";
                pSdcard.style.flex = "1";
                pSdcard.style.background = "#242A38";
                pSdcard.style.color = "#FFF";
                pSdcard.style.border = "none";
                pSdcard.style.borderRadius = "4px";
                pSdcard.style.padding = "4px";
                pSdcard.style.fontSize = "10px";
                pSdcard.onclick = function() { folderInp.value = "/sdcard/AGY/"; };
                presetRow.appendChild(pSdcard);
                tabContent.appendChild(presetRow);

                var folderInp = document.createElement("input");
                folderInp.type = "text";
                folderInp.value = cfg.dbFolder || "/root/wallet/";
                folderInp.style.width = "100%";
                folderInp.style.boxSizing = "border-box";
                folderInp.style.background = "#1D202B";
                folderInp.style.border = "1px solid #37474F";
                folderInp.style.borderRadius = "6px";
                folderInp.style.padding = "7px";
                folderInp.style.color = "#FFF";
                folderInp.style.fontSize = "11px";
                folderInp.style.marginBottom = "10px";
                tabContent.appendChild(folderInp);

                // File Wallets & Emails
                var fileRow = document.createElement("div");
                fileRow.style.display = "flex";
                fileRow.style.gap = "8px";
                fileRow.style.marginBottom = "10px";

                var wCol = document.createElement("div");
                wCol.style.flex = "1";
                var wLbl = document.createElement("label");
                wLbl.innerText = "Wallet File:";
                wLbl.style.fontSize = "10px";
                wLbl.style.display = "block";
                var wInp = document.createElement("input");
                wInp.type = "text";
                wInp.value = cfg.walletFile || "wallets_db.json";
                wInp.style.width = "100%";
                wInp.style.boxSizing = "border-box";
                wInp.style.background = "#1D202B";
                wInp.style.border = "1px solid #37474F";
                wInp.style.borderRadius = "6px";
                wInp.style.padding = "6px";
                wInp.style.color = "#FFF";
                wInp.style.fontSize = "11px";
                wCol.appendChild(wLbl);
                wCol.appendChild(wInp);
                fileRow.appendChild(wCol);

                var eCol = document.createElement("div");
                eCol.style.flex = "1";
                var eLbl = document.createElement("label");
                eLbl.innerText = "Email File:";
                eLbl.style.fontSize = "10px";
                eLbl.style.display = "block";
                var eInp = document.createElement("input");
                eInp.type = "text";
                eInp.value = cfg.emailFile || "email.txt";
                eInp.style.width = "100%";
                eInp.style.boxSizing = "border-box";
                eInp.style.background = "#1D202B";
                eInp.style.border = "1px solid #37474F";
                eInp.style.borderRadius = "6px";
                eInp.style.padding = "6px";
                eInp.style.color = "#FFF";
                eInp.style.fontSize = "11px";
                eCol.appendChild(eLbl);
                eCol.appendChild(eInp);
                fileRow.appendChild(eCol);
                tabContent.appendChild(fileRow);

                // Stats Box
                var wallets = AgyDB.getWallets();
                var emails = AgyDB.getEmails();
                var statsBox = document.createElement("div");
                statsBox.style.background = "#181B24";
                statsBox.style.padding = "8px 10px";
                statsBox.style.borderRadius = "6px";
                statsBox.style.fontSize = "11px";
                statsBox.style.color = "#4CAF50";
                statsBox.style.marginBottom = "12px";
                statsBox.innerText = "✅ Terbaca: " + wallets.length + " Wallets | " + emails.length + " Emails";
                tabContent.appendChild(statsBox);

                // Add Account Form
                var addBox = document.createElement("div");
                addBox.style.border = "1px solid #2B3040";
                addBox.style.borderRadius = "8px";
                addBox.style.padding = "10px";
                addBox.style.marginBottom = "14px";
                addBox.style.background = "#161922";

                var addTitle = document.createElement("div");
                addTitle.innerText = "➕ Tambah Akun Cepat:";
                addTitle.style.fontSize = "11px";
                addTitle.style.fontWeight = "bold";
                addTitle.style.marginBottom = "6px";
                addBox.appendChild(addTitle);

                var addName = document.createElement("input");
                addName.placeholder = "Nama (misal: Tuyul-4)";
                addName.style.width = "100%";
                addName.style.boxSizing = "border-box";
                addName.style.background = "#1D202B";
                addName.style.border = "1px solid #37474F";
                addName.style.borderRadius = "4px";
                addName.style.padding = "5px";
                addName.style.color = "#FFF";
                addName.style.fontSize = "11px";
                addName.style.marginBottom = "4px";
                addBox.appendChild(addName);

                var addAddr = document.createElement("input");
                addAddr.placeholder = "Address (0x...)";
                addAddr.style.width = "100%";
                addAddr.style.boxSizing = "border-box";
                addAddr.style.background = "#1D202B";
                addAddr.style.border = "1px solid #37474F";
                addAddr.style.borderRadius = "4px";
                addAddr.style.padding = "5px";
                addAddr.style.color = "#FFF";
                addAddr.style.fontSize = "11px";
                addAddr.style.marginBottom = "4px";
                addBox.appendChild(addAddr);

                var addEmail = document.createElement("input");
                addEmail.placeholder = "Email (opsional)";
                addEmail.style.width = "100%";
                addEmail.style.boxSizing = "border-box";
                addEmail.style.background = "#1D202B";
                addEmail.style.border = "1px solid #37474F";
                addEmail.style.borderRadius = "4px";
                addEmail.style.padding = "5px";
                addEmail.style.color = "#FFF";
                addEmail.style.fontSize = "11px";
                addEmail.style.marginBottom = "8px";
                addBox.appendChild(addEmail);

                var addSubmit = document.createElement("button");
                addSubmit.innerText = "+ Simpan ke Database";
                addSubmit.style.width = "100%";
                addSubmit.style.background = "#2E7D32";
                addSubmit.style.color = "#FFF";
                addSubmit.style.border = "none";
                addSubmit.style.borderRadius = "6px";
                addSubmit.style.padding = "6px";
                addSubmit.style.fontSize = "11px";
                addSubmit.style.fontWeight = "bold";
                addSubmit.style.cursor = "pointer";
                addSubmit.onclick = function() {
                    var n = addName.value.trim() || "Wallet";
                    var a = addAddr.value.trim();
                    var em = addEmail.value.trim();
                    if (!a.startsWith("0x") || a.length < 20) {
                        alert("Masukkan address wallet valid diawali 0x!");
                        return;
                    }
                    var ok = AgyDB.addAccountDirect(n, a, em);
                    if (ok) {
                        updateAgyHUD("Akun Ditambahkan", n + " berhasil disimpan ke DB", "success");
                        renderTab();
                    } else {
                        updateAgyHUD("Info", "Akun dicatat ke memori", "success");
                    }
                };
                addBox.appendChild(addSubmit);
                tabContent.appendChild(addBox);

                // Save DB Config
                var saveDbBtn = document.createElement("button");
                saveDbBtn.innerText = "💾 Simpan Lokasi DB";
                saveDbBtn.style.width = "100%";
                saveDbBtn.style.background = "#1E88E5";
                saveDbBtn.style.color = "#FFF";
                saveDbBtn.style.border = "none";
                saveDbBtn.style.borderRadius = "8px";
                saveDbBtn.style.padding = "8px";
                saveDbBtn.style.fontSize = "12px";
                saveDbBtn.style.fontWeight = "bold";
                saveDbBtn.style.cursor = "pointer";
                saveDbBtn.onclick = function() {
                    AgyConfig.save({
                        dbFolder: folderInp.value.trim(),
                        walletFile: wInp.value.trim(),
                        emailFile: eInp.value.trim()
                    });
                    updateAgyHUD("Konfigurasi DB Disimpan", folderInp.value.trim(), "success");
                    renderTab();
                };
                tabContent.appendChild(saveDbBtn);

            } else {
                tabSettings.style.color = "#FFD54F";
                tabSettings.style.borderBottom = "2px solid #FFD54F";
                tabDb.style.color = "#888";
                tabDb.style.borderBottom = "none";

                // Step Delay Slider & Input
                var sLbl = document.createElement("label");
                sLbl.innerText = "⏱️ Delay Interval Tiap Langkah (Detik):";
                sLbl.style.fontSize = "11px";
                sLbl.style.fontWeight = "bold";
                sLbl.style.display = "block";
                sLbl.style.marginBottom = "4px";
                tabContent.appendChild(sLbl);

                var sValRow = document.createElement("div");
                sValRow.style.display = "flex";
                sValRow.style.alignItems = "center";
                sValRow.style.gap = "8px";
                sValRow.style.marginBottom = "12px";

                var sRange = document.createElement("input");
                sRange.type = "range";
                sRange.min = "0.5";
                sRange.max = "10.0";
                sRange.step = "0.5";
                sRange.value = ((cfg.stepDelay || 1500) / 1000).toFixed(1);
                sRange.style.flex = "1";

                var sText = document.createElement("span");
                sText.innerText = sRange.value + "s";
                sText.style.fontSize = "13px";
                sText.style.fontWeight = "bold";
                sText.style.color = "#00E5FF";
                sText.style.minWidth = "40px";

                sRange.oninput = function() { sText.innerText = sRange.value + "s"; };
                sValRow.appendChild(sRange);
                sValRow.appendChild(sText);
                tabContent.appendChild(sValRow);

                // Batch Delay
                var bLbl = document.createElement("label");
                bLbl.innerText = "⏳ Jeda Antar Akun saat Batch (Detik):";
                bLbl.style.fontSize = "11px";
                bLbl.style.fontWeight = "bold";
                bLbl.style.display = "block";
                bLbl.style.marginBottom = "4px";
                tabContent.appendChild(bLbl);

                var bInp = document.createElement("input");
                bInp.type = "number";
                bInp.min = "1";
                bInp.max = "30";
                bInp.value = ((cfg.batchDelay || 3000) / 1000).toFixed(1);
                bInp.style.width = "100%";
                bInp.style.boxSizing = "border-box";
                bInp.style.background = "#1D202B";
                bInp.style.border = "1px solid #37474F";
                bInp.style.borderRadius = "6px";
                bInp.style.padding = "7px";
                bInp.style.color = "#FFF";
                bInp.style.fontSize = "12px";
                bInp.style.marginBottom = "12px";
                tabContent.appendChild(bInp);

                // Smart Wait Mode
                var wModeLbl = document.createElement("label");
                wModeLbl.innerText = "🛡️ Mode Smart Wait:";
                wModeLbl.style.fontSize = "11px";
                wModeLbl.style.fontWeight = "bold";
                wModeLbl.style.display = "block";
                wModeLbl.style.marginBottom = "4px";
                tabContent.appendChild(wModeLbl);

                var wSelect = document.createElement("select");
                wSelect.style.width = "100%";
                wSelect.style.background = "#1D202B";
                wSelect.style.border = "1px solid #37474F";
                wSelect.style.borderRadius = "6px";
                wSelect.style.padding = "7px";
                wSelect.style.color = "#FFF";
                wSelect.style.fontSize = "12px";
                wSelect.style.marginBottom = "12px";

                var optStrict = document.createElement("option");
                optStrict.value = "strict";
                optStrict.innerText = "Strict Complete (Tunggu Halaman Selesai + Buffer)";
                if (cfg.waitMode === "strict") optStrict.selected = true;
                wSelect.appendChild(optStrict);

                var optFast = document.createElement("option");
                optFast.value = "fast";
                optFast.innerText = "Fast Interactive (Lanjut Segera saat DOM Siap)";
                if (cfg.waitMode === "fast") optFast.selected = true;
                wSelect.appendChild(optFast);
                tabContent.appendChild(wSelect);

                // Element Timeout
                var tLbl = document.createElement("label");
                tLbl.innerText = "⌛ Max Element Search Timeout (Detik):";
                tLbl.style.fontSize = "11px";
                tLbl.style.fontWeight = "bold";
                tLbl.style.display = "block";
                tLbl.style.marginBottom = "4px";
                tabContent.appendChild(tLbl);

                var tInp = document.createElement("input");
                tInp.type = "number";
                tInp.min = "3";
                tInp.max = "30";
                tInp.value = ((cfg.elementTimeout || 8000) / 1000).toFixed(0);
                tInp.style.width = "100%";
                tInp.style.boxSizing = "border-box";
                tInp.style.background = "#1D202B";
                tInp.style.border = "1px solid #37474F";
                tInp.style.borderRadius = "6px";
                tInp.style.padding = "7px";
                tInp.style.color = "#FFF";
                tInp.style.fontSize = "12px";
                tInp.style.marginBottom = "16px";
                tabContent.appendChild(tInp);

                // Save Settings Button
                var saveSetBtn = document.createElement("button");
                saveSetBtn.innerText = "💾 Simpan Semua Pengaturan";
                saveSetBtn.style.width = "100%";
                saveSetBtn.style.background = "#1E88E5";
                saveSetBtn.style.color = "#FFF";
                saveSetBtn.style.border = "none";
                saveSetBtn.style.borderRadius = "8px";
                saveSetBtn.style.padding = "9px";
                saveSetBtn.style.fontSize = "12px";
                saveSetBtn.style.fontWeight = "bold";
                saveSetBtn.style.cursor = "pointer";
                saveSetBtn.onclick = function() {
                    AgyConfig.save({
                        stepDelay: Math.round(parseFloat(sRange.value) * 1000),
                        batchDelay: Math.round(parseFloat(bInp.value) * 1000),
                        waitMode: wSelect.value,
                        elementTimeout: Math.round(parseInt(tInp.value, 10) * 1000)
                    });
                    updateAgyHUD("Pengaturan Disimpan", "Step Delay: " + sRange.value + "s | Batch: " + bInp.value + "s", "success");
                    modal.remove();
                if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
                };
                tabContent.appendChild(saveSetBtn);
            }
        }

        tabDb.onclick = function() { currentTab = "database"; renderTab(); };
        tabSettings.onclick = function() { currentTab = "settings"; renderTab(); };
        renderTab();

        var closeHubBtn = document.createElement("button");
        closeHubBtn.innerText = "TUTUP";
        closeHubBtn.style.width = "100%";
        closeHubBtn.style.background = "#313244";
        closeHubBtn.style.color = "#cdd6f4";
        closeHubBtn.style.border = "1px solid rgba(255,255,255,0.08)";
        closeHubBtn.style.borderRadius = "12px";
        closeHubBtn.style.padding = "11px";
        closeHubBtn.style.fontSize = "12px";
        closeHubBtn.style.fontWeight = "700";
        closeHubBtn.style.cursor = "pointer";
        closeHubBtn.style.textAlign = "center";
        closeHubBtn.style.marginTop = "10px";
        closeHubBtn.style.flexShrink = "0";
        closeHubBtn.onclick = function(e) {
            e.stopPropagation();
            modal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
        };
        card.appendChild(closeHubBtn);

        modal.appendChild(card);
        (document.body || document.documentElement).appendChild(modal);
    }

    // --- 15B. VISUAL RECIPE STEP EDITOR & DOM PICKER ---

    // 1. DOM Picker khusus untuk memilih elemen ke dalam langkah resep
    function showDomPickerForStep(onPickCallback) {
        var existing = document.getElementById("__agy_step_dom_picker__");
        if (existing) existing.remove();

        var scanned = scanActiveElements();

        var pickerModal = document.createElement("div");
        pickerModal.id = "__agy_step_dom_picker__";
        pickerModal.style.position = "fixed";
        pickerModal.style.top = "0";
        pickerModal.style.left = "0";
        pickerModal.style.width = "100%";
        pickerModal.style.height = "100%";
        pickerModal.style.backgroundColor = "rgba(0, 0, 0, 0.88)";
        pickerModal.style.zIndex = "2147483647";
        pickerModal.style.display = "flex";
        pickerModal.style.alignItems = "center";
        pickerModal.style.justifyContent = "center";
        pickerModal.style.fontFamily = "system-ui, -apple-system, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#14161E";
        card.style.border = "1px solid #2B3040";
        card.style.borderRadius = "16px";
        card.style.width = "90%";
        card.style.maxWidth = "420px";
        card.style.padding = "16px";
        card.style.boxShadow = "0 10px 40px rgba(0,0,0,0.85)";
        card.style.color = "#FFFFFF";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "8px";

        var title = document.createElement("h3");
        title.innerText = "🎯 Pilih Elemen Web untuk Langkah";
        title.style.margin = "0";
        title.style.fontSize = "14px";
        title.style.color = "#00E5FF";
        titleRow.appendChild(title);

        var closeX = document.createElement("span");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#888";
        closeX.style.fontSize = "16px";
        closeX.style.padding = "4px 8px";
        closeX.onclick = function() { pickerModal.remove(); };
        // closeX moved to bottom footer
        card.appendChild(titleRow);

        var filterInput = document.createElement("input");
        filterInput.type = "text";
        filterInput.placeholder = "Cari elemen (button, input, claim, follow)...";
        filterInput.style.width = "93%";
        filterInput.style.background = "#0B0C10";
        filterInput.style.border = "1px solid #333";
        filterInput.style.borderRadius = "8px";
        filterInput.style.color = "#FFF";
        filterInput.style.padding = "7px 10px";
        filterInput.style.fontSize = "12px";
        filterInput.style.marginBottom = "10px";
        filterInput.style.outline = "none";
        card.appendChild(filterInput);

        var listContainer = document.createElement("div");
        listContainer.style.maxHeight = "240px";
        listContainer.style.overflowY = "auto";
        listContainer.style.marginBottom = "10px";

        function renderPickerList(query) {
            listContainer.innerHTML = "";
            var q = (query || "").toLowerCase().trim();
            var filtered = scanned.filter(function(item) {
                if (!q) return true;
                var meta = (item.label + " " + item.aria + " " + item.testid + " " + item.placeholder + " " + item.tag).toLowerCase();
                return meta.includes(q);
            });

            if (filtered.length === 0) {
                var empty = document.createElement("div");
                empty.innerText = "Tidak ada elemen yang cocok.";
                empty.style.color = "#777";
                empty.style.fontSize = "12px";
                empty.style.padding = "16px 0";
                empty.style.textAlign = "center";
                listContainer.appendChild(empty);
                return;
            }

            filtered.slice(0, 50).forEach(function(item) {
                var row = document.createElement("div");
                row.style.display = "flex";
                row.style.alignItems = "center";
                row.style.justifyContent = "space-between";
                row.style.background = "#1B1E29";
                row.style.borderRadius = "6px";
                row.style.padding = "6px 8px";
                row.style.marginBottom = "5px";

                var info = document.createElement("div");
                info.style.overflow = "hidden";
                info.style.textOverflow = "ellipsis";
                info.style.whiteSpace = "nowrap";
                info.style.maxWidth = "65%";

                var tagBadge = "<span style='font-size:10px; background:#2A3142; color:#00E5FF; padding:2px 4px; border-radius:4px; margin-right:4px;'>" + item.tag.toUpperCase() + "</span>";
                info.innerHTML = tagBadge + "<span style='font-size:11px; font-weight:500;'>" + (item.label || item.tag) + "</span>";
                row.appendChild(info);

                var pickBtn = document.createElement("button");
                pickBtn.innerText = "✓ Pilih";
                pickBtn.style.background = "#1E88E5";
                pickBtn.style.color = "#FFF";
                pickBtn.style.border = "none";
                pickBtn.style.borderRadius = "4px";
                pickBtn.style.padding = "5px 12px";
                pickBtn.style.fontSize = "11px";
                pickBtn.style.fontWeight = "bold";
                pickBtn.style.cursor = "pointer";

                pickBtn.onclick = function(e) {
                    e.stopPropagation();
                    pickerModal.remove();
                    var meta = {
                        selector: getCssSelector(item.el),
                        tag: item.tag,
                        id: item.el.id || "",
                        name: item.el.name || "",
                        testid: item.el.getAttribute("data-testid") || "",
                        ariaLabel: item.el.getAttribute("aria-label") || "",
                        text: (item.el.innerText || item.el.value || item.label || "").substring(0, 30).trim(),
                        placeholder: item.el.getAttribute("placeholder") || ""
                    };
                    if (onPickCallback) onPickCallback(meta);
                };

                row.appendChild(pickBtn);
                listContainer.appendChild(row);
            });
        }

        renderPickerList("");
        filterInput.oninput = function() { renderPickerList(filterInput.value); };
        card.appendChild(listContainer);

        var cancelBtn = document.createElement("button");
        cancelBtn.innerText = "Batal";
        cancelBtn.style.width = "100%";
        cancelBtn.style.background = "#2B3040";
        cancelBtn.style.color = "#FFF";
        cancelBtn.style.border = "none";
        cancelBtn.style.borderRadius = "6px";
        cancelBtn.style.padding = "8px";
        cancelBtn.style.fontSize = "12px";
        cancelBtn.style.cursor = "pointer";
        cancelBtn.onclick = function() { pickerModal.remove(); };
        card.appendChild(cancelBtn);

        pickerModal.appendChild(card);
        (document.body || document.documentElement).appendChild(pickerModal);
    }

    // 2. Dialog Edit Detail Satu Langkah (Single Step)
    function showEditSingleStepDialog(step, onSave) {
        var existing = document.getElementById("__agy_single_step_dialog__");
        if (existing) existing.remove();

        var dialogModal = document.createElement("div");
        dialogModal.id = "__agy_single_step_dialog__";
        dialogModal.style.position = "fixed";
        dialogModal.style.top = "0";
        dialogModal.style.left = "0";
        dialogModal.style.width = "100%";
        dialogModal.style.height = "100%";
        dialogModal.style.backgroundColor = "rgba(0, 0, 0, 0.88)";
        dialogModal.style.zIndex = "2147483647";
        dialogModal.style.display = "flex";
        dialogModal.style.alignItems = "center";
        dialogModal.style.justifyContent = "center";
        dialogModal.style.fontFamily = "system-ui, -apple-system, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#14161E";
        card.style.border = "1px solid #2B3040";
        card.style.borderRadius = "16px";
        card.style.width = "90%";
        card.style.maxWidth = "380px";
        card.style.padding = "18px";
        card.style.color = "#FFFFFF";
        card.style.boxShadow = "0 10px 40px rgba(0,0,0,0.85)";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "12px";

        var title = document.createElement("h3");
        title.innerText = "✏️ Detail Langkah";
        title.style.margin = "0";
        title.style.fontSize = "15px";
        title.style.color = "#00E5FF";
        titleRow.appendChild(title);

        var closeX = document.createElement("span");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#888";
        closeX.style.fontSize = "16px";
        closeX.style.padding = "4px 8px";
        closeX.onclick = function() { dialogModal.remove(); };
        // closeX moved to bottom footer
        card.appendChild(titleRow);

        var currentStep = Object.assign({ type: "click" }, step || {});

        var typeLabel = document.createElement("div");
        typeLabel.innerText = "Tipe Aksi:";
        typeLabel.style.fontSize = "11px";
        typeLabel.style.color = "#A0A8BF";
        typeLabel.style.marginBottom = "4px";
        card.appendChild(typeLabel);

        var typeSelect = document.createElement("select");
        typeSelect.style.width = "100%";
        typeSelect.style.background = "#0B0C10";
        typeSelect.style.border = "1px solid #3B4254";
        typeSelect.style.borderRadius = "6px";
        typeSelect.style.color = "#FFF";
        typeSelect.style.padding = "8px";
        typeSelect.style.fontSize = "12px";
        typeSelect.style.marginBottom = "12px";
        typeSelect.style.outline = "none";

        var optClick = document.createElement("option");
        optClick.value = "click";
        optClick.innerText = "🖱️ Klik Elemen (Click)";
        typeSelect.appendChild(optClick);

        var optFill = document.createElement("option");
        optFill.value = "fill";
        optFill.innerText = "✍️ Isi Form / Teks (Fill)";
        typeSelect.appendChild(optFill);

        var optWait = document.createElement("option");
        optWait.value = "wait";
        optWait.innerText = "⏳ Smart Jeda / Wait (Delay)";
        typeSelect.appendChild(optWait);

        var optWallet = document.createElement("option");
        optWallet.value = "next_wallet";
        optWallet.innerText = "👛 Ganti Wallet (Next Wallet)";
        typeSelect.appendChild(optWallet);

        var optReload = document.createElement("option");
        optReload.value = "reload";
        optReload.innerText = "🔄 Muat Ulang Halaman (Reload)";
        typeSelect.appendChild(optReload);

        var optWipe = document.createElement("option");
        optWipe.value = "wipe_cache";
        optWipe.innerText = "🧹 Bersihkan Cache (Wipe Cache)";
        typeSelect.appendChild(optWipe);

        var optEmail = document.createElement("option");
        optEmail.value = "next_email";
        optEmail.innerText = "📧 Ganti Email (Next Email)";
        typeSelect.appendChild(optEmail);

        var optRef = document.createElement("option");
        optRef.value = "ref";
        optRef.innerText = "🔗 Ref / Referral Code";
        typeSelect.appendChild(optRef);

        var optFp = document.createElement("option");
        optFp.value = "fingerprint";
        optFp.innerText = "🎭 Ganti Fingerprint Profil";
        typeSelect.appendChild(optFp);

        typeSelect.value = (currentStep.type === "wait" || currentStep.type === "next_wallet" || currentStep.type === "reload" || currentStep.type === "wipe_cache" || currentStep.type === "next_email" || currentStep.type === "ref" || currentStep.type === "fingerprint") ? currentStep.type : (currentStep.type === "fill" || currentStep.type === "input" ? "fill" : "click");
        card.appendChild(typeSelect);

        var fieldsContainer = document.createElement("div");
        card.appendChild(fieldsContainer);

        function updateFields() {
            fieldsContainer.innerHTML = "";
            var selectedType = typeSelect.value;

            if (selectedType === "next_wallet") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #7C4DFF";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#d1c4e9";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "👛 <b>Ganti Wallet Otomatis:</b><br/>Langkah ini beralih ke dompet berikutnya di daftar <code>wallets_db.json</code> dan memperbarui Ghost provider.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "next_wallet",
                        text: "Ganti Wallet -> Next",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "next_email") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #FFA000";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#ffe082";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "📧 <b>Ganti Email:</b><br/>Langkah ini beralih ke akun email berikutnya dari <code>email.txt</code>.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "next_email",
                        text: "Ganti Email",
                        value: currentStep.value || "",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "ref") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #00E676";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#b9f6ca";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "🔗 <b>Referral Code:</b><br/>Langkah ini menyisipkan kode / link referral dari <code>query_db.json</code>.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "ref",
                        text: "Ref Code",
                        value: currentStep.value || "",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "fingerprint") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #E040FB";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#ea80fc";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "🎭 <b>Ganti Fingerprint:</b><br/>Langkah ini mengacak model HP Android, WebGL vendor, dan platform browser profil.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "fingerprint",
                        text: "Ganti Fingerprint",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "reload") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #00B0FF";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#b3e5fc";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "🔄 <b>Reload Halaman (Auto-Survive):</b><br/>Memuat ulang halaman web. Alur resep atau makro otomatis bertahan dan melanjutkan langkah berikutnya setelah halaman selesai dimuat.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "reload",
                        text: "Reload Halaman",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "wipe_cache") {
                var info = document.createElement("div");
                info.style.background = "#1D202B";
                info.style.border = "1px solid #FF9100";
                info.style.padding = "10px";
                info.style.borderRadius = "8px";
                info.style.fontSize = "12px";
                info.style.color = "#ffe0b2";
                info.style.marginBottom = "14px";
                info.style.lineHeight = "1.5";
                info.innerHTML = "🧹 <b>Wipe Cache & Storage:</b><br/>Membersihkan cookies, sessionStorage, dan localStorage agar sesi bersih tanpa menghentikan alur bot.";
                fieldsContainer.appendChild(info);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "wipe_cache",
                        text: "Wipe Cache & Storage",
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "wait") {
                var waitLbl = document.createElement("div");
                waitLbl.innerText = "Durasi Jeda (Milidetik):";
                waitLbl.style.fontSize = "11px";
                waitLbl.style.color = "#A0A8BF";
                waitLbl.style.marginBottom = "4px";
                fieldsContainer.appendChild(waitLbl);

                var durInput = document.createElement("input");
                durInput.type = "number";
                durInput.value = currentStep.duration || 2500;
                durInput.style.width = "94%";
                durInput.style.background = "#0B0C10";
                durInput.style.border = "1px solid #3B4254";
                durInput.style.borderRadius = "6px";
                durInput.style.color = "#FFF";
                durInput.style.padding = "8px";
                durInput.style.fontSize = "12px";
                durInput.style.marginBottom = "8px";
                fieldsContainer.appendChild(durInput);

                var presetsRow = document.createElement("div");
                presetsRow.style.display = "flex";
                presetsRow.style.gap = "6px";
                presetsRow.style.marginBottom = "14px";
                [
                    { label: "1s", val: 1000 },
                    { label: "2.5s", val: 2500 },
                    { label: "4s", val: 4000 },
                    { label: "7s", val: 7000 }
                ].forEach(function(p) {
                    var pBtn = document.createElement("button");
                    pBtn.innerText = p.label;
                    pBtn.style.flex = "1";
                    pBtn.style.background = "#242A38";
                    pBtn.style.color = "#00E5FF";
                    pBtn.style.border = "1px solid #3B4254";
                    pBtn.style.borderRadius = "4px";
                    pBtn.style.padding = "4px";
                    pBtn.style.fontSize = "11px";
                    pBtn.style.cursor = "pointer";
                    pBtn.onclick = function() { durInput.value = p.val; };
                    presetsRow.appendChild(pBtn);
                });
                fieldsContainer.appendChild(presetsRow);
                fieldsContainer.__getStep = function() {
                    return {
                        type: "wait",
                        duration: parseInt(durInput.value, 10) || 2500,
                        timestamp: Date.now()
                    };
                };
            } else if (selectedType === "fill") {
                var selLbl = document.createElement("div");
                selLbl.innerText = "Target Selector / Input:";
                selLbl.style.fontSize = "11px";
                selLbl.style.color = "#A0A8BF";
                selLbl.style.marginBottom = "4px";
                fieldsContainer.appendChild(selLbl);

                var selInp = document.createElement("input");
                selInp.type = "text";
                selInp.value = currentStep.selector || (currentStep.id ? "#" + currentStep.id : "");
                selInp.placeholder = "contoh: input[name='email'], #wallet_box";
                selInp.style.width = "94%";
                selInp.style.background = "#0B0C10";
                selInp.style.border = "1px solid #3B4254";
                selInp.style.borderRadius = "6px";
                selInp.style.color = "#FFF";
                selInp.style.padding = "8px";
                selInp.style.fontSize = "11px";
                selInp.style.marginBottom = "8px";
                fieldsContainer.appendChild(selInp);

                var valLbl = document.createElement("div");
                valLbl.innerText = "Nilai Form / Teks:";
                valLbl.style.fontSize = "11px";
                valLbl.style.color = "#A0A8BF";
                valLbl.style.marginBottom = "4px";
                fieldsContainer.appendChild(valLbl);

                var valInp = document.createElement("input");
                valInp.type = "text";
                valInp.value = currentStep.value || "";
                valInp.placeholder = "Ketik teks atau gunakan variabel template";
                valInp.style.width = "94%";
                valInp.style.background = "#0B0C10";
                valInp.style.border = "1px solid #3B4254";
                valInp.style.borderRadius = "6px";
                valInp.style.color = "#FFF";
                valInp.style.padding = "8px";
                valInp.style.fontSize = "12px";
                valInp.style.marginBottom = "8px";
                fieldsContainer.appendChild(valInp);

                var varsRow = document.createElement("div");
                varsRow.style.display = "flex";
                varsRow.style.gap = "4px";
                varsRow.style.marginBottom = "10px";
                [
                    { label: "+ {{WALLET_ADDRESS}}", code: "{{WALLET_ADDRESS}}" },
                    { label: "+ {{EMAIL}}", code: "{{EMAIL}}" },
                    { label: "+ {{NAME}}", code: "{{NAME}}" }
                ].forEach(function(item) {
                    var vBtn = document.createElement("button");
                    vBtn.innerText = item.label;
                    vBtn.style.flex = "1";
                    vBtn.style.background = "#242A38";
                    vBtn.style.color = "#FFB74D";
                    vBtn.style.border = "1px solid #4A3A28";
                    vBtn.style.borderRadius = "4px";
                    vBtn.style.padding = "4px 2px";
                    vBtn.style.fontSize = "10px";
                    vBtn.style.cursor = "pointer";
                    vBtn.onclick = function() {
                        valInp.value = item.code;
                    };
                    varsRow.appendChild(vBtn);
                });
                fieldsContainer.appendChild(varsRow);

                var pickDomBtn = document.createElement("button");
                pickDomBtn.innerText = "🔍 Pilih Input dari Halaman Web";
                pickDomBtn.style.width = "100%";
                pickDomBtn.style.background = "#1D202B";
                pickDomBtn.style.color = "#00E5FF";
                pickDomBtn.style.border = "1px dashed #00E5FF";
                pickDomBtn.style.borderRadius = "6px";
                pickDomBtn.style.padding = "7px";
                pickDomBtn.style.fontSize = "11px";
                pickDomBtn.style.marginBottom = "14px";
                pickDomBtn.style.cursor = "pointer";
                pickDomBtn.onclick = function() {
                    showDomPickerForStep(function(meta) {
                        selInp.value = meta.selector || (meta.id ? "#" + meta.id : "");
                        currentStep.selector = meta.selector;
                        currentStep.tag = meta.tag;
                        currentStep.id = meta.id;
                        currentStep.name = meta.name;
                    });
                };
                fieldsContainer.appendChild(pickDomBtn);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "fill",
                        selector: selInp.value.trim(),
                        value: valInp.value,
                        tag: currentStep.tag || "input",
                        id: currentStep.id || "",
                        name: currentStep.name || "",
                        placeholder: currentStep.placeholder || "",
                        timestamp: Date.now()
                    };
                };
            } else {
                var selLbl = document.createElement("div");
                selLbl.innerText = "Target Selector / Tombol:";
                selLbl.style.fontSize = "11px";
                selLbl.style.color = "#A0A8BF";
                selLbl.style.marginBottom = "4px";
                fieldsContainer.appendChild(selLbl);

                var selInp = document.createElement("input");
                selInp.type = "text";
                selInp.value = currentStep.selector || (currentStep.id ? "#" + currentStep.id : "");
                selInp.placeholder = "contoh: button.submit, #claim-btn";
                selInp.style.width = "94%";
                selInp.style.background = "#0B0C10";
                selInp.style.border = "1px solid #3B4254";
                selInp.style.borderRadius = "6px";
                selInp.style.color = "#FFF";
                selInp.style.padding = "8px";
                selInp.style.fontSize = "11px";
                selInp.style.marginBottom = "8px";
                fieldsContainer.appendChild(selInp);

                var textLbl = document.createElement("div");
                textLbl.innerText = "Teks Tombol / Label (Fallback):";
                textLbl.style.fontSize = "11px";
                textLbl.style.color = "#A0A8BF";
                textLbl.style.marginBottom = "4px";
                fieldsContainer.appendChild(textLbl);

                var textInp = document.createElement("input");
                textInp.type = "text";
                textInp.value = currentStep.text || currentStep.ariaLabel || "";
                textInp.placeholder = "contoh: Claim Reward, Submit, Next";
                textInp.style.width = "94%";
                textInp.style.background = "#0B0C10";
                textInp.style.border = "1px solid #3B4254";
                textInp.style.borderRadius = "6px";
                textInp.style.color = "#FFF";
                textInp.style.padding = "8px";
                textInp.style.fontSize = "11px";
                textInp.style.marginBottom = "10px";
                fieldsContainer.appendChild(textInp);

                var pickDomBtn = document.createElement("button");
                pickDomBtn.innerText = "🔍 Pilih Elemen dari Halaman Web";
                pickDomBtn.style.width = "100%";
                pickDomBtn.style.background = "#1D202B";
                pickDomBtn.style.color = "#00E5FF";
                pickDomBtn.style.border = "1px dashed #00E5FF";
                pickDomBtn.style.borderRadius = "6px";
                pickDomBtn.style.padding = "7px";
                pickDomBtn.style.fontSize = "11px";
                pickDomBtn.style.marginBottom = "14px";
                pickDomBtn.style.cursor = "pointer";
                pickDomBtn.onclick = function() {
                    showDomPickerForStep(function(meta) {
                        selInp.value = meta.selector || (meta.id ? "#" + meta.id : "");
                        textInp.value = meta.text || meta.ariaLabel || "";
                        currentStep.selector = meta.selector;
                        currentStep.tag = meta.tag;
                        currentStep.id = meta.id;
                        currentStep.name = meta.name;
                        currentStep.testid = meta.testid;
                        currentStep.ariaLabel = meta.ariaLabel;
                        currentStep.text = meta.text;
                    });
                };
                fieldsContainer.appendChild(pickDomBtn);

                fieldsContainer.__getStep = function() {
                    return {
                        type: "click",
                        selector: selInp.value.trim(),
                        text: textInp.value.trim(),
                        tag: currentStep.tag || "button",
                        id: currentStep.id || "",
                        name: currentStep.name || "",
                        testid: currentStep.testid || "",
                        ariaLabel: currentStep.ariaLabel || "",
                        x: currentStep.x,
                        y: currentStep.y,
                        timestamp: Date.now()
                    };
                };
            }
        }

        typeSelect.onchange = updateFields;
        updateFields();

        var btnRow = document.createElement("div");
        btnRow.style.display = "flex";
        btnRow.style.gap = "8px";

        var cancelBtn = document.createElement("button");
        cancelBtn.innerText = "Batal";
        cancelBtn.style.flex = "1";
        cancelBtn.style.background = "#2B3040";
        cancelBtn.style.color = "#FFF";
        cancelBtn.style.border = "none";
        cancelBtn.style.borderRadius = "6px";
        cancelBtn.style.padding = "8px";
        cancelBtn.style.fontSize = "12px";
        cancelBtn.style.cursor = "pointer";
        cancelBtn.onclick = function() { dialogModal.remove(); };
        btnRow.appendChild(cancelBtn);

        var saveBtn = document.createElement("button");
        saveBtn.innerText = "Simpan Langkah";
        saveBtn.style.flex = "1";
        saveBtn.style.background = "#1E88E5";
        saveBtn.style.color = "#FFF";
        saveBtn.style.border = "none";
        saveBtn.style.borderRadius = "6px";
        saveBtn.style.padding = "8px";
        saveBtn.style.fontSize = "12px";
        saveBtn.style.fontWeight = "bold";
        saveBtn.style.cursor = "pointer";
        saveBtn.onclick = function() {
            var updated = fieldsContainer.__getStep ? fieldsContainer.__getStep() : currentStep;
            dialogModal.remove();
            if (onSave) onSave(updated);
        };
        btnRow.appendChild(saveBtn);
        card.appendChild(btnRow);

        dialogModal.appendChild(card);
        (document.body || document.documentElement).appendChild(dialogModal);
    }

    // 3. Modal Utama Visual Recipe Step Editor
    function showRecipeEditorModal(recipeName, eventsList, onCloseCallback) {
        var existing = document.getElementById("__agy_recipe_editor_modal__");
        if (existing) existing.remove();
        if (window.GhostBridge && window.GhostBridge.onDrawerOpen) { try { window.GhostBridge.onDrawerOpen(); } catch(_) {} }

        var workingName = recipeName || ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");
        var workingEvents = (eventsList || []).slice();

        var editorModal = document.createElement("div");
        editorModal.id = "__agy_recipe_editor_modal__";
        editorModal.style.position = "fixed";
        editorModal.style.top = "0";
        editorModal.style.left = "0";
        editorModal.style.width = "100%";
        editorModal.style.height = "100%";
        editorModal.style.backgroundColor = "rgba(17, 17, 27, 0.65)";
        editorModal.style.backdropFilter = "blur(6px)";
        editorModal.style.webkitBackdropFilter = "blur(6px)";
        editorModal.style.zIndex = "2147483647";
        editorModal.style.display = "flex";
        editorModal.style.justifyContent = "flex-start";
        editorModal.style.alignItems = "stretch";
        editorModal.style.fontFamily = "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

        var card = document.createElement("div");
        card.style.background = "#181825";
        card.style.border = "none";
        card.style.borderRight = "1px solid #313244";
        card.style.borderRadius = "0px";
        card.style.width = "82%";
        card.style.maxWidth = "380px";
        card.style.height = "100%";
        card.style.display = "flex";
        card.style.flexDirection = "column";
        card.style.padding = "20px 16px 16px 16px";
        card.style.boxShadow = "10px 0 36px rgba(0,0,0,0.85)";
        card.style.color = "#cdd6f4";
        card.style.boxSizing = "border-box";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "8px";
        titleRow.style.flexShrink = "0";

        var titleBlock = document.createElement("div");
        var title = document.createElement("h3");
        title.innerText = "🛠️ Editor Langkah Resep";
        title.style.margin = "0";
        title.style.fontSize = "15px";
        title.style.fontWeight = "700";
        title.style.color = "#cba6f7";
        titleBlock.appendChild(title);
        titleRow.appendChild(titleBlock);

        var closeX = document.createElement("div");
        closeX.innerText = "✕";
        closeX.style.cursor = "pointer";
        closeX.style.color = "#a6adc8";
        closeX.style.fontSize = "15px";
        closeX.style.width = "28px";
        closeX.style.height = "28px";
        closeX.style.borderRadius = "8px";
        closeX.style.display = "flex";
        closeX.style.alignItems = "center";
        closeX.style.justifyContent = "center";
        closeX.style.background = "#1e1e2e";
        closeX.style.touchAction = "manipulation";
        closeX.style.userSelect = "none";
        closeX.style.webkitUserSelect = "none";
        closeX.style.touchAction = "manipulation";
        closeX.style.userSelect = "none";
        closeX.style.webkitUserSelect = "none";

        var handleCloseEditor = function(e) {
            if (e && e.stopPropagation) e.stopPropagation();
            if (e && e.cancelable) e.preventDefault();
            editorModal.remove();
            if (window.GhostBridge && window.GhostBridge.onDrawerClose) { try { window.GhostBridge.onDrawerClose(); } catch(_) {} }
            if (typeof onCloseCallback === "function") {
                onCloseCallback(workingEvents);
            }
        };
        closeX.addEventListener("touchend", handleCloseEditor, { passive: false });
        closeX.addEventListener("pointerup", handleCloseEditor);
        closeX.onclick = handleCloseEditor;

        titleRow.appendChild(closeX);
        card.appendChild(titleRow);

        var nameRow = document.createElement("div");
        nameRow.style.display = "flex";
        nameRow.style.alignItems = "center";
        nameRow.style.gap = "8px";
        nameRow.style.marginBottom = "10px";

        var nameLabel = document.createElement("span");
        nameLabel.innerText = "Nama:";
        nameLabel.style.fontSize = "11px";
        nameLabel.style.color = "#A0A8BF";
        nameRow.appendChild(nameLabel);

        var nameInp = document.createElement("input");
        nameInp.type = "text";
        nameInp.value = workingName;
        nameInp.style.flex = "1";
        nameInp.style.background = "#0B0C10";
        nameInp.style.border = "1px solid #3B4254";
        nameInp.style.borderRadius = "6px";
        nameInp.style.color = "#FFF";
        nameInp.style.padding = "6px 8px";
        nameInp.style.fontSize = "12px";
        nameInp.style.outline = "none";
        nameRow.appendChild(nameInp);
        card.appendChild(nameRow);

        var addBar = document.createElement("div");
        addBar.style.background = "#1B1F2D";
        addBar.style.border = "1px solid #2A3144";
        addBar.style.borderRadius = "8px";
        addBar.style.padding = "6px 8px";
        addBar.style.display = "flex";
        addBar.style.gap = "6px";
        addBar.style.marginBottom = "10px";

        var addWaitBtn = document.createElement("button");
        addWaitBtn.innerText = "⏳ + Jeda";
        addWaitBtn.style.flex = "1";
        addWaitBtn.style.background = "#4A148C";
        addWaitBtn.style.color = "#FFF";
        addWaitBtn.style.border = "none";
        addWaitBtn.style.borderRadius = "5px";
        addWaitBtn.style.padding = "6px 4px";
        addWaitBtn.style.fontSize = "11px";
        addWaitBtn.style.cursor = "pointer";
        addWaitBtn.onclick = function() {
            workingEvents.push({ type: "wait", duration: 2500, timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addWaitBtn);

        var addFillBtn = document.createElement("button");
        addFillBtn.innerText = "✍️ + Isi Form";
        addFillBtn.style.flex = "1";
        addFillBtn.style.background = "#E65100";
        addFillBtn.style.color = "#FFF";
        addFillBtn.style.border = "none";
        addFillBtn.style.borderRadius = "5px";
        addFillBtn.style.padding = "6px 4px";
        addFillBtn.style.fontSize = "11px";
        addFillBtn.style.cursor = "pointer";
        addFillBtn.onclick = function() {
            showEditSingleStepDialog({ type: "fill", value: "{{WALLET_ADDRESS}}" }, function(newStep) {
                workingEvents.push(newStep);
                renderStepList();
            });
        };
        addBar.appendChild(addFillBtn);

        var addClickBtn = document.createElement("button");
        addClickBtn.innerText = "🖱️ + Klik";
        addClickBtn.style.flex = "1";
        addClickBtn.style.background = "#1565C0";
        addClickBtn.style.color = "#FFF";
        addClickBtn.style.border = "none";
        addClickBtn.style.borderRadius = "5px";
        addClickBtn.style.padding = "6px 4px";
        addClickBtn.style.fontSize = "11px";
        addClickBtn.style.cursor = "pointer";
        addClickBtn.onclick = function() {
            showEditSingleStepDialog({ type: "click" }, function(newStep) {
                workingEvents.push(newStep);
                renderStepList();
            });
        };
        addBar.appendChild(addClickBtn);

        var addWalletBtn = document.createElement("button");
        addWalletBtn.innerText = "👛 + Wallet";
        addWalletBtn.style.flex = "1";
        addWalletBtn.style.background = "#7C4DFF";
        addWalletBtn.style.color = "#FFF";
        addWalletBtn.style.border = "none";
        addWalletBtn.style.borderRadius = "5px";
        addWalletBtn.style.padding = "6px 4px";
        addWalletBtn.style.fontSize = "11px";
        addWalletBtn.style.cursor = "pointer";
        addWalletBtn.onclick = function() {
            workingEvents.push({ type: "next_wallet", text: "Ganti Wallet", timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addWalletBtn);

        var addReloadBtn = document.createElement("button");
        addReloadBtn.innerText = "🔄 + Reload";
        addReloadBtn.style.flex = "1";
        addReloadBtn.style.background = "#00B0FF";
        addReloadBtn.style.color = "#000";
        addReloadBtn.style.border = "none";
        addReloadBtn.style.borderRadius = "5px";
        addReloadBtn.style.padding = "6px 4px";
        addReloadBtn.style.fontSize = "11px";
        addReloadBtn.style.fontWeight = "bold";
        addReloadBtn.style.cursor = "pointer";
        addReloadBtn.onclick = function() {
            workingEvents.push({ type: "reload", text: "Reload Halaman", timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addReloadBtn);

        var addWipeBtn = document.createElement("button");
        addWipeBtn.innerText = "🧹 + Wipe";
        addWipeBtn.style.flex = "1";
        addWipeBtn.style.background = "#FF9100";
        addWipeBtn.style.color = "#000";
        addWipeBtn.style.border = "none";
        addWipeBtn.style.borderRadius = "5px";
        addWipeBtn.style.padding = "6px 4px";
        addWipeBtn.style.fontSize = "11px";
        addWipeBtn.style.fontWeight = "bold";
        addWipeBtn.style.cursor = "pointer";
        addWipeBtn.onclick = function() {
            workingEvents.push({ type: "wipe_cache", text: "Wipe Cache", timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addWipeBtn);

        var addEmailBtn = document.createElement("button");
        addEmailBtn.innerText = "📧 + Email";
        addEmailBtn.style.flex = "1";
        addEmailBtn.style.background = "#FFA000";
        addEmailBtn.style.color = "#000";
        addEmailBtn.style.border = "none";
        addEmailBtn.style.borderRadius = "5px";
        addEmailBtn.style.padding = "6px 4px";
        addEmailBtn.style.fontSize = "11px";
        addEmailBtn.style.fontWeight = "bold";
        addEmailBtn.style.cursor = "pointer";
        addEmailBtn.onclick = function() {
            var ne = (typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "email@gmail.com";
            workingEvents.push({ type: "next_email", text: "Ganti Email -> " + ne, value: ne, timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addEmailBtn);

        var addRefBtn = document.createElement("button");
        addRefBtn.innerText = "🔗 + Ref";
        addRefBtn.style.flex = "1";
        addRefBtn.style.background = "#00E676";
        addRefBtn.style.color = "#000";
        addRefBtn.style.border = "none";
        addRefBtn.style.borderRadius = "5px";
        addRefBtn.style.padding = "6px 4px";
        addRefBtn.style.fontSize = "11px";
        addRefBtn.style.fontWeight = "bold";
        addRefBtn.style.cursor = "pointer";
        addRefBtn.onclick = function() {
            var nr = (typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123";
            workingEvents.push({ type: "ref", text: "Ref Code -> " + nr, value: nr, timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addRefBtn);

        var addFpBtn = document.createElement("button");
        addFpBtn.innerText = "🎭 + FP";
        addFpBtn.style.flex = "1";
        addFpBtn.style.background = "#E040FB";
        addFpBtn.style.color = "#FFF";
        addFpBtn.style.border = "none";
        addFpBtn.style.borderRadius = "5px";
        addFpBtn.style.padding = "6px 4px";
        addFpBtn.style.fontSize = "11px";
        addFpBtn.style.fontWeight = "bold";
        addFpBtn.style.cursor = "pointer";
        addFpBtn.onclick = function() {
            workingEvents.push({ type: "fingerprint", text: "Ganti Fingerprint", timestamp: Date.now() });
            renderStepList();
        };
        addBar.appendChild(addFpBtn);
        card.appendChild(addBar);

        var stepListContainer = document.createElement("div");
        stepListContainer.style.flex = "1";
        stepListContainer.style.overflowY = "auto";
        stepListContainer.style.maxHeight = "46vh";
        stepListContainer.style.marginBottom = "10px";
        card.appendChild(stepListContainer);

        function renderStepList() {
            stepListContainer.innerHTML = "";
            if (workingEvents.length === 0) {
                var empty = document.createElement("div");
                empty.innerText = "Belum ada langkah di resep ini. Gunakan tombol di atas untuk menambah langkah.";
                empty.style.color = "#777";
                empty.style.fontSize = "12px";
                empty.style.padding = "24px 0";
                empty.style.textAlign = "center";
                stepListContainer.appendChild(empty);
                return;
            }

            workingEvents.forEach(function(ev, index) {
                var row = document.createElement("div");
                row.style.display = "flex";
                row.style.alignItems = "center";
                row.style.justifyContent = "space-between";
                row.style.background = "#1D202B";
                row.style.border = "1px solid #2B3040";
                row.style.borderRadius = "8px";
                row.style.padding = "7px 9px";
                row.style.marginBottom = "6px";

                var infoCol = document.createElement("div");
                infoCol.style.overflow = "hidden";
                infoCol.style.textOverflow = "ellipsis";
                infoCol.style.whiteSpace = "nowrap";
                infoCol.style.maxWidth = "60%";

                var numBadge = "<span style='font-size:10px; font-weight:bold; background:#2A3142; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>#" + (index + 1) + "</span>";

                var typeBadge = "";
                var desc = "";
                if (ev.type === "wait") {
                    typeBadge = "<span style='font-size:10px; background:#7B1FA2; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>⏳ JEDA</span>";
                    desc = ((ev.duration || 2500) / 1000) + " detik";
                } else if (ev.type === "next_wallet") {
                    typeBadge = "<span style='font-size:10px; background:#7C4DFF; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>👛 WALLET</span>";
                    desc = ev.text || "Ganti ke wallet berikutnya";
                } else if (ev.type === "reload") {
                    typeBadge = "<span style='font-size:10px; background:#00B0FF; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold;'>🔄 RELOAD</span>";
                    desc = ev.text || "Muat ulang halaman & lanjut";
                } else if (ev.type === "wipe_cache") {
                    typeBadge = "<span style='font-size:10px; background:#FF9100; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold;'>🧹 WIPE</span>";
                    desc = ev.text || "Wipe Cache & Storage";
                } else if (ev.type === "next_email") {
                    typeBadge = "<span style='font-size:10px; background:#FFA000; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold;'>📧 EMAIL</span>";
                    desc = ev.text || "Ganti ke email berikutnya";
                } else if (ev.type === "ref") {
                    typeBadge = "<span style='font-size:10px; background:#00E676; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold;'>🔗 REF</span>";
                    desc = ev.text || "Ref Code";
                } else if (ev.type === "fingerprint") {
                    typeBadge = "<span style='font-size:10px; background:#E040FB; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold;'>🎭 FP</span>";
                    desc = ev.text || "Ganti Fingerprint";
                } else if (ev.type === "swipe") {
                    typeBadge = "<span style='font-size:10px; background:#9C27B0; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>👆 SWIPE</span>";
                    desc = (ev.direction || "DOWN").toUpperCase() + " (" + Math.round(Math.max(Math.abs(ev.deltaX || 0), Math.abs(ev.deltaY || 0))) + "px)";
                } else if (ev.type === "fill" || ev.type === "input") {
                    typeBadge = "<span style='font-size:10px; background:#F57C00; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>✍️ ISI</span>";
                    desc = (ev.value || "") + " ➔ " + (ev.placeholder || ev.name || ev.id || ev.selector || "input");
                } else {
                    typeBadge = "<span style='font-size:10px; background:#1E88E5; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px;'>🖱️ KLIK</span>";
                    desc = ev.text || ev.ariaLabel || ev.name || ev.id || ev.selector || "elemen";
                }

                infoCol.innerHTML = numBadge + typeBadge + "<span style='font-size:11px; font-weight:500; color:#EEE;'>" + desc + "</span>";
                row.appendChild(infoCol);

                var btnCol = document.createElement("div");
                btnCol.style.display = "flex";
                btnCol.style.alignItems = "center";
                btnCol.style.gap = "4px";

                if (index > 0) {
                    var upBtn = document.createElement("button");
                    upBtn.innerText = "▲";
                    upBtn.style.background = "#242A38";
                    upBtn.style.color = "#00E5FF";
                    upBtn.style.border = "none";
                    upBtn.style.borderRadius = "4px";
                    upBtn.style.padding = "4px 6px";
                    upBtn.style.fontSize = "10px";
                    upBtn.style.cursor = "pointer";
                    upBtn.onclick = function() {
                        var temp = workingEvents[index];
                        workingEvents[index] = workingEvents[index - 1];
                        workingEvents[index - 1] = temp;
                        renderStepList();
                    };
                    btnCol.appendChild(upBtn);
                }

                if (index < workingEvents.length - 1) {
                    var downBtn = document.createElement("button");
                    downBtn.innerText = "▼";
                    downBtn.style.background = "#242A38";
                    downBtn.style.color = "#00E5FF";
                    downBtn.style.border = "none";
                    downBtn.style.borderRadius = "4px";
                    downBtn.style.padding = "4px 6px";
                    downBtn.style.fontSize = "10px";
                    downBtn.style.cursor = "pointer";
                    downBtn.onclick = function() {
                        var temp = workingEvents[index];
                        workingEvents[index] = workingEvents[index + 1];
                        workingEvents[index + 1] = temp;
                        renderStepList();
                    };
                    btnCol.appendChild(downBtn);
                }

                var editStepBtn = document.createElement("button");
                editStepBtn.innerText = "✏️";
                editStepBtn.style.background = "#37474F";
                editStepBtn.style.color = "#FFF";
                editStepBtn.style.border = "none";
                editStepBtn.style.borderRadius = "4px";
                editStepBtn.style.padding = "4px 6px";
                editStepBtn.style.fontSize = "11px";
                editStepBtn.style.cursor = "pointer";
                editStepBtn.onclick = function() {
                    showEditSingleStepDialog(ev, function(updatedStep) {
                        workingEvents[index] = updatedStep;
                        renderStepList();
                    });
                };
                btnCol.appendChild(editStepBtn);

                var delStepBtn = document.createElement("button");
                delStepBtn.innerText = "✕";
                delStepBtn.style.background = "#C62828";
                delStepBtn.style.color = "#FFF";
                delStepBtn.style.border = "none";
                delStepBtn.style.borderRadius = "4px";
                delStepBtn.style.padding = "4px 6px";
                delStepBtn.style.fontSize = "11px";
                delStepBtn.style.cursor = "pointer";
                delStepBtn.onclick = function() {
                    workingEvents.splice(index, 1);
                    renderStepList();
                };
                btnCol.appendChild(delStepBtn);

                row.appendChild(btnCol);
                stepListContainer.appendChild(row);
            });
        }

        renderStepList();

        var footerRow = document.createElement("div");
        footerRow.style.display = "flex";
        footerRow.style.gap = "8px";

        var runBtn = document.createElement("button");
        runBtn.innerText = "▶ Test Run";
        runBtn.style.flex = "1";
        runBtn.style.background = "#2E7D32";
        runBtn.style.color = "#FFF";
        runBtn.style.border = "none";
        runBtn.style.borderRadius = "8px";
        runBtn.style.padding = "9px";
        runBtn.style.fontSize = "12px";
        runBtn.style.fontWeight = "bold";
        runBtn.style.cursor = "pointer";
        runBtn.onclick = function() {
            var saveName = nameInp.value.trim() || workingName;
            RecipeStore.save(saveName, workingEvents);
            editorModal.remove();
            if (typeof onCloseCallback === "function") {
                onCloseCallback(workingEvents);
            }
            replayEventsSequentially(workingEvents, saveName);
        };
        footerRow.appendChild(runBtn);

        var saveAllBtn = document.createElement("button");
        saveAllBtn.innerText = "💾 Simpan Perubahan";
        saveAllBtn.style.flex = "1.2";
        saveAllBtn.style.background = "#1E88E5";
        saveAllBtn.style.color = "#FFF";
        saveAllBtn.style.border = "none";
        saveAllBtn.style.borderRadius = "8px";
        saveAllBtn.style.padding = "9px";
        saveAllBtn.style.fontSize = "12px";
        saveAllBtn.style.fontWeight = "bold";
        saveAllBtn.style.cursor = "pointer";
        saveAllBtn.onclick = function() {
            var saveName = nameInp.value.trim() || workingName;
            RecipeStore.save(saveName, workingEvents);
            updateAgyHUD("Resep Disimpan", saveName + " (" + workingEvents.length + " langkah)", "success");
            editorModal.remove();
            if (typeof onCloseCallback === "function") {
                onCloseCallback(workingEvents);
            }
        };
        footerRow.appendChild(saveAllBtn);
        card.appendChild(footerRow);

        editorModal.appendChild(card);
        (document.body || document.documentElement).appendChild(editorModal);
    }

    // --- 16. SMART WAIT & ACCURATE SEQUENTIAL REPLAY ENGINE ---
    function waitForDocumentReady(callback, maxWaitMs) {
        var timeout = maxWaitMs || 5000;
        var start = Date.now();
        if (document.readyState === "complete" || document.readyState === "interactive") {
            setTimeout(callback, 50);
            return;
        }
        var checkTimer = setInterval(function() {
            if (document.readyState === "complete" || (Date.now() - start > timeout)) {
                clearInterval(checkTimer);
                callback();
            }
        }, 150);
    }

    function smartWaitForElement(ev, stepIdx, totalSteps, callback) {
        var cfg = AgyConfig.get();
        smartWaitForElementWithTimeout(ev, stepIdx, totalSteps, cfg.elementTimeout || 8000, callback);
    }

    function replayEventsSequentially(eventsList, recipeLabel) {
        AgyBatchRunner.run(eventsList, recipeLabel || "Resep", "1", false);
    }

    // --- 17. AGY BRIDGE INTERFACE ---
    window.AgyBridge = {
        updateHUD: updateAgyHUD,
        showRecipePicker: showRecipePickerModal,
        showRecipeEditor: function(name) {
            var recipes = RecipeStore.getAll();
            if (name && recipes[name]) {
                showRecipeEditorModal(name, recipes[name].events);
            } else {
                var last = RecipeStore.getLastEvents();
                showRecipeEditorModal(name || "Resep_Terbaru", last);
            }
        },
        editRecipe: function(name) {
            this.showRecipeEditor(name);
        },
        showDomInspector: showDomInspectorModal,
        scanElements: scanActiveElements,

        startRecorder: function(recipeName) {
            AgyRecorder.start(recipeName);
        },
        stopRecorder: function() {
            AgyRecorder.stop();
        },
        toggleRecorder: function(recipeName) {
            AgyRecorder.toggle(recipeName);
        },

        runRecipe: function(eventsJson, recipeName) {
            var evts = [];
            try {
                evts = (typeof eventsJson === "string") ? JSON.parse(eventsJson) : eventsJson;
            } catch(_) {}
            showRunConfigModal(recipeName || "Resep", evts);
        },

        runBatch: function(recipeName, targetExpr) {
            var recipes = RecipeStore.getAll();
            if (recipeName && recipes[recipeName]) {
                AgyBatchRunner.run(recipes[recipeName].events, recipeName, targetExpr || "*", true);
            } else {
                updateAgyHUD("Resep Tidak Ditemukan", recipeName, "warning");
            }
        },

        stop: function() {
            AgyBatchRunner.stop();
        },

        openHub: function(tab) {
            showAgyHubModal(tab);
        },

        openSettings: function() {
            showAgyHubModal("settings");
        },

        openDatabase: function() {
            showAgyHubModal("database");
        },

        autoFillForm: function() {
            return executeFormAutoFill();
        },

        smartClaim: function() {
            return executeSmartClaimAction();
        },

        solveTurnstile: function() {
            return executeTurnstileSolver();
        },

        getTmaInitData: function() {
            return (window.Telegram && window.Telegram.WebApp && window.Telegram.WebApp.initData) || "";
        },

        executeNaturalPrompt: function(promptStr) {
            return executeNaturalPrompt(promptStr);
        },

        executeCommand: function(cmdText) {
            return executeNaturalPrompt(cmdText);
        }
    };

    // --- 18. BOT NATURAL COMMAND PARSER & DOM ACTION RUNNER ---
    function findAndClickElement(targetText) {
        if (!targetText) return false;
        var rawText = targetText.trim();
        var q = rawText.toLowerCase();

        // 1. Bersihkan kata awalan (contoh: "klik tombol connect" -> "connect")
        var cleanQ = q.replace(/^(?:klik|click|pencet|tekan|tap|press|buka|open)\s+/i, "")
                      .replace(/^(?:tombol|button|link|tautan|icon|ikon|menu|opsi|tulisan|teks|tab)\s+/i, "")
                      .trim();
        if (!cleanQ) cleanQ = q;

        // 2. Pemetaan sinonim Web3 & tombol umum
        var searchTerms = [cleanQ];
        if (cleanQ.includes("login") || cleanQ.includes("masuk") || cleanQ.includes("sign in")) {
            searchTerms.push("connect", "connect wallet", "sign in", "login");
        } else if (cleanQ.includes("connect")) {
            searchTerms.push("connect wallet", "connect", "hubungkan");
        } else if (cleanQ.includes("swap") || cleanQ.includes("tukar")) {
            searchTerms.push("swap", "trade", "exchange", "tukar");
        } else if (cleanQ.includes("claim") || cleanQ.includes("klaim")) {
            searchTerms.push("claim", "redeem", "faucet", "klaim", "reward");
        }

        // 3. Kumpulkan elemen kandidat dari seluruh dokumen (tidak dibatasi hanya dalam viewport)
        var candidates = Array.from(document.querySelectorAll(
            "button, a, input[type=button], input[type=submit], [role=button], [role=tab], [role=menuitem], [tabindex], select, summary, div, span, li, p, h1, h2, h3, h4, h5, h6, label"
        )).filter(function(el) {
            if (!el || (el.id && el.id.startsWith("__agy_"))) return false;
            if (el.closest && el.closest("[id^='__agy_']")) return false;
            var r = el.getBoundingClientRect();
            return r.width > 0 && r.height > 0;
        });

        var bestEl = null;
        var bestScore = -1;
        var bestLabel = "";

        candidates.forEach(function(el) {
            var text = (el.innerText || el.value || "").trim();
            var aria = (el.getAttribute("aria-label") || "").trim();
            var testid = (el.getAttribute("data-testid") || "").trim();
            var id = (el.id || "").trim();
            var name = (el.name || "").trim();
            var title = (el.title || "").trim();
            var role = (el.getAttribute("role") || "").trim();

            var haystack = (text + " " + aria + " " + testid + " " + id + " " + name + " " + title).toLowerCase();

            searchTerms.forEach(function(term) {
                var isMatch = haystack.includes(term);
                if (!isMatch) {
                    var words = term.split(/\s+/).filter(function(w) { return w.length >= 3; });
                    var matchCount = 0;
                    words.forEach(function(w) { if (haystack.includes(w)) matchCount++; });
                    if (words.length > 0 && matchCount === words.length) isMatch = true;
                }
                if (!isMatch) return;

                var score = 0;
                var tag = el.tagName.toLowerCase();
                if (tag === "button" || tag === "a" || role === "button" || role === "tab") score += 80;
                if (tag === "input" && (el.type === "button" || el.type === "submit")) score += 80;

                if (text.toLowerCase() === term || aria.toLowerCase() === term) score += 150;
                else if (text.toLowerCase().startsWith(term) || aria.toLowerCase().startsWith(term)) score += 60;
                else if (haystack.includes(term)) score += 40;

                score -= Math.min(el.children.length * 4, 30);

                if (score > bestScore) {
                    bestScore = score;
                    bestEl = el;
                    bestLabel = text || aria || testid || id || name || rawText;
                }
            });
        });

        if (bestEl) {
            var clickable = bestEl.closest("button, a, [role=button], input[type=button], input[type=submit], [tabindex]") || bestEl;
            var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
            executeClickAction(clickable, bestLabel, "BOT: " + rawText, isRec);
            updateAgyHUD("BOT: Berhasil", "Menekan: " + bestLabel.substring(0, 30), "success");
            return true;
        }
        return false;
    }

    function findAndFillElement(fieldName, valueToSet) {
        if (!fieldName) return false;
        var q = fieldName.toLowerCase().trim();

        var inputs = Array.from(document.querySelectorAll("input, textarea, select"))
            .filter(function(el) {
                if (!el || el.type === "hidden") return false;
                if (el.id && el.id.startsWith("__agy_")) return false;
                if (el.closest && el.closest("[id^='__agy_']")) return false;
                var r = el.getBoundingClientRect();
                return r.width > 0 && r.height > 0;
            });

        var bestInp = null;
        var bestScore = -1;

        inputs.forEach(function(inp) {
            var ph = (inp.placeholder || "").toLowerCase();
            var name = (inp.name || "").toLowerCase();
            var id = (inp.id || "").toLowerCase();
            var aria = (inp.getAttribute("aria-label") || "").toLowerCase();
            var type = (inp.type || "text").toLowerCase();

            var score = 0;
            if (ph === q || name === q || id === q || aria === q) score += 100;
            else if (ph.includes(q) || name.includes(q) || id.includes(q) || aria.includes(q)) score += 50;
            else if (type.includes(q)) score += 30;

            if (score > bestScore) {
                bestScore = score;
                bestInp = inp;
            }
        });

        if (bestInp) {
            bestInp.focus();
            var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                         Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
            if (setter) setter.call(bestInp, valueToSet);
            else bestInp.value = valueToSet;

            bestInp.dispatchEvent(new Event("input", { bubbles: true }));
            bestInp.dispatchEvent(new Event("change", { bubbles: true }));

            var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
            if (isRec) {
                AgyRecorder.recordEvent({
                    type: "fill",
                    timestamp: Date.now(),
                    selector: getCssSelector(bestInp),
                    tag: bestInp.tagName.toLowerCase(),
                    id: bestInp.id || "",
                    name: bestInp.name || "",
                    placeholder: bestInp.placeholder || "",
                    value: valueToSet,
                    isDynamic: false
                });
            }
            var lbl = bestInp.name || bestInp.placeholder || bestInp.id || fieldName;
            updateAgyHUD("BOT: Form Diisi", lbl + " = " + valueToSet.substring(0, 25), "success");
            return true;
        }
        return false;
    }

    function executeSmartClaimAction() {
        updateAgyHUD("Smart Claim", "Mencari tombol klaim...", "loading");
        var claimKeywords = ["claim", "klaim", "collect", "reward", "airdrop", "check in", "checkin", "daily", "ambil"];
        var buttons = Array.from(document.querySelectorAll("button, a, input[type=button], input[type=submit], [role=button], div, span"))
            .filter(function(el) {
                if (!el || (el.id && el.id.startsWith("__agy_"))) return false;
                if (el.closest && el.closest("[id^='__agy_']")) return false;
                var r = el.getBoundingClientRect();
                return r.width > 0 && r.height > 0 && r.bottom >= 0 && r.top <= window.innerHeight;
            });

        var foundEl = null;
        var foundLabel = "";

        for (var i = 0; i < buttons.length; i++) {
            var el = buttons[i];
            var text = (el.innerText || el.value || el.getAttribute("aria-label") || "").trim().toLowerCase();
            for (var k = 0; k < claimKeywords.length; k++) {
                if (text.includes(claimKeywords[k])) {
                    foundEl = el;
                    foundLabel = (el.innerText || el.value || claimKeywords[k]).trim();
                    break;
                }
            }
            if (foundEl) break;
        }

        if (foundEl) {
            var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
            executeClickAction(foundEl, foundLabel, "Smart Claim", isRec);
            updateAgyHUD("Smart Claim Berhasil", "Tombol '" + foundLabel.substring(0, 25) + "' ditekan", "success");
            return true;
        } else {
            updateAgyHUD("Smart Claim", "Tidak ada tombol klaim aktif di halaman ini", "warning");
            return false;
        }
    }

    function executeTurnstileSolver() {
        updateAgyHUD("Turnstile Solver", "Memindai tantangan Cloudflare...", "loading");
        var iframes = document.querySelectorAll("iframe");
        var solved = false;
        iframes.forEach(function(f) {
            try {
                var src = f.src || "";
                if (src.includes("cloudflare") || src.includes("turnstile") || src.includes("challenges")) {
                    var rect = f.getBoundingClientRect();
                    var cx = rect.left + 30;
                    var cy = rect.top + rect.height / 2;
                    f.dispatchEvent(new MouseEvent("click", { bubbles: true, cancelable: true, clientX: cx, clientY: cy }));
                    solved = true;
                }
            } catch(_) {}
        });

        if (solved) {
            updateAgyHUD("Turnstile Solver", "Tantangan Cloudflare diklik", "success");
        } else {
            var cfBox = document.querySelector(".cf-turnstile, #cf-stage, [data-sitekey]");
            if (cfBox) {
                cfBox.click();
                updateAgyHUD("Turnstile Solver", "Wadah Turnstile ditekan", "success");
                solved = true;
            } else {
                updateAgyHUD("Turnstile Solver", "Tidak mendeteksi challenge Cloudflare", "warning");
            }
        }
        return solved;
    }

    function executeSingleStep(stepText, callback) {
        if (!stepText || !stepText.trim()) {
            if (typeof callback === "function") callback(false);
            return;
        }
        var s = stepText.trim();
        var sp = s.toLowerCase();

        // 0a. Ganti Wallet / Next Wallet
        if (sp === "next wallet" || sp === "ganti wallet" || sp === "next dompet" || sp === "dompet berikutnya" || sp === "switch wallet") {
            updateAgyHUD("AGY Agent: Wallet", "Beralih ke wallet berikutnya...", "loading");
            var nw = "";
            try {
                if (window.GhostBridge && window.GhostBridge.nextWallet) {
                    nw = window.GhostBridge.nextWallet();
                }
            } catch(_) {}
            try {
                var newAddr = (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : "";
                if (newAddr) window.dispatchEvent(new CustomEvent("accountsChanged", { detail: [newAddr] }));
            } catch(_) {}
            setTimeout(function() {
                updateAgyHUD("AGY Agent: Wallet", "Aktif: " + (nw || "Wallet Berikutnya"), "success");
                if (typeof callback === "function") callback(true);
            }, 800);
            return;
        }

        // 0b. Reload / Refresh Halaman
        if (sp === "reload" || sp === "refresh" || sp === "muat ulang") {
            updateAgyHUD("AGY Agent: Reload", "Memuat ulang halaman...", "loading");
            setTimeout(function() {
                location.reload();
            }, 400);
            return;
        }

        // 0c. Wipe Cache / Bersihkan Cache
        if (sp === "wipe cache" || sp === "wipe" || sp === "bersihkan cache" || sp === "hapus cache" || sp === "clear cookies") {
            updateAgyHUD("AGY Agent: Wipe Cache", "Membersihkan cache & cookies...", "loading");
            var savedPromptQueue = sessionStorage.getItem("__agy_pending_prompt_queue__");
            try {
                if (window.GhostBridge && window.GhostBridge.wipeCache) {
                    window.GhostBridge.wipeCache();
                }
            } catch(_) {}
            try { localStorage.clear(); sessionStorage.clear(); } catch(_) {}
            if (savedPromptQueue) {
                try { sessionStorage.setItem("__agy_pending_prompt_queue__", savedPromptQueue); } catch(_) {}
            }
            setTimeout(function() {
                updateAgyHUD("AGY Agent: Wipe Cache", "Cache berhasil dibersihkan", "success");
                if (typeof callback === "function") callback(true);
            }, 600);
            return;
        }

        // 0d. Connect / Konek Wallet
        if (sp.includes("konek wallet") || sp.includes("connect wallet") || sp.includes("hubungkan wallet") || sp.includes("koneksikan wallet")) {
            updateAgyHUD("AGY Agent: Wallet", "Mencari tombol Connect Wallet...", "loading");
            var cOk = findAndClickElement("connect wallet") ||
                      findAndClickElement("connect") ||
                      findAndClickElement("koneksikan") ||
                      findAndClickElement("hubungkan dompet");
            if (!cOk) {
                var cBtn = document.querySelector("button[data-testid*='connect'], [aria-label*='connect' i], [class*='connect-wallet' i]");
                if (cBtn) {
                    cBtn.click();
                    cOk = true;
                }
            }
            setTimeout(function() {
                findAndClickElement("metamask") || findAndClickElement("injected") || findAndClickElement("browser wallet") || findAndClickElement("ghost");
                if (typeof callback === "function") callback(true);
            }, 800);
            return;
        }

        // 0e. Next Email / Ganti Email
        if (sp === "next email" || sp === "ganti email" || sp === "email berikutnya") {
            var ne = (typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "email@gmail.com";
            updateAgyHUD("AGY Agent: Email", "Email: " + ne, "loading");
            setTimeout(function() {
                updateAgyHUD("AGY Agent: Email", "Aktif: " + ne, "success");
                if (typeof callback === "function") callback(true);
            }, 600);
            return;
        }

        // 0f. Ref Code / Next Ref
        if (sp === "next ref" || sp === "ganti ref" || sp === "ref" || sp.startsWith("ref ")) {
            var nr = (typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123";
            updateAgyHUD("AGY Agent: Ref", "Ref: " + nr, "loading");
            setTimeout(function() {
                updateAgyHUD("AGY Agent: Ref", "Aktif: " + nr, "success");
                if (typeof callback === "function") callback(true);
            }, 600);
            return;
        }

        // 0g. Fingerprint Faker / Ganti HP
        if (sp.includes("fingerprint") || sp.includes("ganti fp") || sp.includes("acak hp") || sp.includes("next fp")) {
            updateAgyHUD("AGY Agent: Fingerprint", "Mengacak fingerprint...", "loading");
            var fp = "";
            try {
                if (window.GhostBridge && window.GhostBridge.nextFingerprint) {
                    fp = window.GhostBridge.nextFingerprint();
                }
            } catch(_) {}
            setTimeout(function() {
                updateAgyHUD("AGY Agent: Fingerprint", "Model HP: " + (fp || "Acak"), "success");
                if (typeof callback === "function") callback(true);
            }, 800);
            return;
        }

        // 1. Pilih Wallet / Dompet N (Cth: "pilih wallet 3", "wallet 3", "pilih akun 2")
        var wMatch = sp.match(/(?:pilih|select|gunakan)?\s*(?:wallet|dompet|akun)\s*(\d+)/i);
        if (wMatch) {
            var wIdx = parseInt(wMatch[1], 10);
            updateAgyHUD("AGY Agent: Wallet", "Memilih Akun/Wallet #" + wIdx, "loading");
            // Coba switch akun via AgyDB jika ada
            try {
                var wallets = AgyDB.getWallets();
                if (wallets && wallets[wIdx - 1]) {
                    AgyDB.setActiveWallet(wallets[wIdx - 1].name || wallets[wIdx - 1].address);
                }
            } catch(_) {}
            // Coba klik elemen wallet di modal DOM jika modal terbuka
            var clicked = findAndClickElement("wallet " + wIdx) || findAndClickElement("account " + wIdx) || findAndClickElement("metamask") || findAndClickElement("rabby");
            setTimeout(function() { if (typeof callback === "function") callback(true); }, 500);
            return;
        }

        // 2. Aksi Swap Token (Cth: "swap token ini ke ini", "swap usdt ke eth", "swap")
        if (sp.includes("swap") || sp.includes("tukar")) {
            updateAgyHUD("AGY Agent: Swap", "Menjalankan form swap...", "loading");
            var swapTokenMatch = sp.match(/swap\s+([a-z0-9]+)\s+(?:ke|to|into)\s+([a-z0-9]+)/i);
            if (swapTokenMatch) {
                var fromToken = swapTokenMatch[1];
                var toToken = swapTokenMatch[2];
                findAndClickElement(fromToken);
                setTimeout(function() {
                    findAndClickElement(toToken);
                    setTimeout(function() {
                        findAndClickElement("swap") || findAndClickElement("tukar");
                        if (typeof callback === "function") callback(true);
                    }, 600);
                }, 600);
                return;
            } else {
                findAndClickElement("swap") || findAndClickElement("tukar");
                setTimeout(function() { if (typeof callback === "function") callback(true); }, 500);
                return;
            }
        }

        // 3. Captcha / Cloudflare Turnstile Solver
        if (sp.includes("captcha") || sp.includes("turnstile") || sp.includes("cf")) {
            updateAgyHUD("AGY Agent: Captcha", "Menyelesaikan Captcha / Cloudflare Turnstile...", "loading");
            executeTurnstileSolver();
            setTimeout(function() { if (typeof callback === "function") callback(true); }, 1200);
            return;
        }

        // 4. Quest / Smart Claim / Reward / Airdrop Task
        if (sp.includes("quest") || sp.includes("claim") || sp.includes("klaim") || sp.includes("reward") || sp.includes("airdrop") || sp.includes("daily") || sp.includes("tugas")) {
            updateAgyHUD("AGY Agent: Quest", "Mencari & menyelesaikan Quest / Reward...", "loading");
            executeSmartClaimAction();
            setTimeout(function() { if (typeof callback === "function") callback(true); }, 1000);
            return;
        }

        // 5. Auto-Fill Form
        if (sp.includes("auto fill") || sp.includes("autofill") || sp.includes("isi form") || sp.includes("isi data") || sp.includes("isi akun")) {
            updateAgyHUD("AGY Agent: AutoFill", "Mengisi form otomatis...", "loading");
            executeFormAutoFill();
            setTimeout(function() { if (typeof callback === "function") callback(true); }, 800);
            return;
        }

        // 6. Telegram Group/Channel Autonomous Join Task
        if (sp.includes("join telegram") || sp.includes("join grup") || sp.includes("join channel") || sp.includes("gabung telegram") || sp.includes("gabung grup") || sp.includes("t.me/")) {
            var tgUrlMatch = s.match(/(?:https?:\/\/)?(?:www\.)?t\.me\/[^\s]+|@[a-zA-Z0-9_]+/i);
            var tgTarget = tgUrlMatch ? tgUrlMatch[0] : "";
            if (tgTarget) {
                updateAgyHUD("Telegram Join", "Memerintahkan tuyul Telegram bergabung ke " + tgTarget + "...", "loading");
                try {
                    if (window.GhostBridge && window.GhostBridge.forwardToDaemon) {
                        window.GhostBridge.forwardToDaemon(JSON.stringify({
                            method: "telegram_join",
                            params: [tgTarget]
                        }));
                    }
                } catch(e) {}
                setTimeout(function() {
                    updateAgyHUD("Telegram Join", "Tugas join Telegram dikirim ke tuyul!", "success");
                    if (typeof callback === "function") callback(true);
                }, 1500);
                return;
            }
        }

        // 7. Klik Tombol / Elemen
        var clickMatch = s.match(/^(?:klik|click|pencet|tekan|tap|press)\s+(.+)$/i);
        if (clickMatch) {
            var target = clickMatch[1].trim();
            var ok = findAndClickElement(target);
            if (!ok) {
                updateAgyHUD("BOT: Elemen Tidak Ditemukan", "\"" + target + "\" tidak ada di halaman", "warning");
            }
            setTimeout(function() { if (typeof callback === "function") callback(ok); }, 500);
            return;
        }

        // 8. Isi Form
        var fillMatch = s.match(/^(?:isi|fill|ketik|type|input)\s+([^\s]+)\s+(.+)$/i);
        if (fillMatch) {
            var okFill = findAndFillElement(fillMatch[1].trim(), fillMatch[2].trim());
            if (!okFill) {
                updateAgyHUD("BOT: Kolom Tidak Ditemukan", "\"" + fillMatch[1] + "\" tidak ada di halaman", "warning");
            }
            setTimeout(function() { if (typeof callback === "function") callback(okFill); }, 500);
            return;
        }

        // 9. Fallback ke pencarian elemen atau sub-prompt
        var okFallback = findAndClickElement(s);
        if (!okFallback) {
            updateAgyHUD("BOT: Elemen Tidak Ditemukan", "\"" + s + "\" tidak ada di halaman", "warning");
        }
        setTimeout(function() { if (typeof callback === "function") callback(okFallback); }, 500);
    }

    function executeMultiStepPlan(steps, index) {
        if (!steps || index >= steps.length) {
            updateAgyHUD("AGY Agent Selesai", steps.length + " langkah alur berhasil diselesaikan", "success");
            try { sessionStorage.removeItem("__agy_pending_prompt_queue__"); } catch(_) {}
            return;
        }

        var currentStep = steps[index];
        var stepNum = index + 1;
        var total = steps.length;

        updateAgyHUD("AGY Agent [" + stepNum + "/" + total + "]", currentStep, "running");

        // Handle reload step within multi-step flow so it survives page reload
        var isReloadStep = /^(?:reload|refresh|muat\s*ulang)$/i.test(currentStep.trim());
        if (isReloadStep) {
            updateAgyHUD("AGY Agent [" + stepNum + "/" + total + "]", "Menyimpan antrean alur & memuat ulang halaman...", "running");
            try {
                sessionStorage.setItem("__agy_pending_prompt_queue__", JSON.stringify({
                    steps: steps,
                    index: index + 1,
                    timestamp: Date.now()
                }));
            } catch(_) {}
            setTimeout(function() {
                location.reload();
            }, 500);
            return;
        }

        executeSingleStep(currentStep, function(stepSuccess) {
            setTimeout(function() {
                executeMultiStepPlan(steps, index + 1);
            }, 1000);
        });
    }

    function executeNaturalPrompt(promptStr) {
        if (!promptStr || typeof promptStr !== "string" || !promptStr.trim()) {
            updateAgyHUD("BOT", "Ketik perintah yang ingin dijalankan", "warning");
            return;
        }
        var raw = promptStr.trim();
        var p = raw.toLowerCase();

        // 0. Multi-Step Chain Splitter (Cth: "klik A lalu klik B lalu selesaikan quest lalu swap dan solve captcha")
        if (/\s+(?:lalu|kemudian|then|->)\s+|\s+dan\s+(?=klik|swap|solve|selesaikan|klaim|claim|isi|fill)/i.test(raw)) {
            var rawChain = raw.replace(/\s+dan\s+(?=klik|swap|solve|selesaikan|klaim|claim|isi|fill)/gi, " lalu ");
            var chainSteps = rawChain.split(/\s+(?:lalu|kemudian|then|->)\s+/i).map(function(st) { return st.trim(); }).filter(Boolean);
            if (chainSteps.length > 1) {
                updateAgyHUD("AGY Multi-Step Flow", "Menjalankan " + chainSteps.length + " langkah berurutan...", "running");
                executeMultiStepPlan(chainSteps, 0);
                return;
            }
        }

        // 1. Panduan / Help
        if (p === "help" || p === "bantuan" || p === "?") {
            updateAgyHUD("Perintah BOT", "klik <nama>, isi <kolom> <nilai>, auto fill, klaim, resep, rekam, dom, scroll", "success");
            return;
        }

        // 2. Kontrol Rekaman & Eksekusi Batch
        if (p.includes("stop") || p.includes("batal") || p.includes("hentikan")) {
            if (AgyRecorder.isRecording) {
                AgyRecorder.cancel();
            } else {
                AgyBatchRunner.stop();
                updateAgyHUD("BOT", "Aktivitas bot dihentikan", "success");
            }
            return;
        }
        if (p === "selesai" || p.includes("simpan resep")) {
            if (AgyRecorder.isRecording) {
                AgyRecorder.stopAndPromptSave();
            } else {
                updateAgyHUD("BOT", "Tidak ada rekaman aktif", "warning");
            }
            return;
        }
        if (p.startsWith("rekam") || p.startsWith("record") || p === "mulai rekam") {
            var rName = raw.replace(/^(rekam|record|mulai rekam)\s*/i, "").trim();
            AgyRecorder.start(rName || null, "dom");
            return;
        }

        // 3. Modals Shortcut
        if (p.includes("setting") || p.includes("pengaturan") || p.includes("delay")) {
            showAgyHubModal("settings");
            updateAgyHUD("BOT", "Membuka Pengaturan", "success");
            return;
        }
        if (p.includes("db") || p.includes("database") || p.includes("wallet") || p.includes("dompet") || p.includes("akun") || p.includes("email")) {
            showAgyHubModal("database");
            updateAgyHUD("BOT", "Membuka Database Akun", "success");
            return;
        }
        if (p === "resep" || p === "recipe" || p === "makro" || p === "buka resep") {
            showRecipePickerModal();
            updateAgyHUD("BOT", "Membuka Daftar Resep", "success");
            return;
        }
        if (p.startsWith("jalankan resep") || p.startsWith("run recipe")) {
            var recName = raw.replace(/^(jalankan resep|run recipe)\s*/i, "").trim();
            var recipes = RecipeStore.getAll();
            if (recipes[recName]) {
                AgyBatchRunner.run(recipes[recName].events, recName, "1", false);
            } else {
                updateAgyHUD("BOT: Resep Tidak Ditemukan", recName, "warning");
            }
            return;
        }

        // 4. Otomasi Cepat DOM (Diprioritaskan sebelum fallback AI agar Webview langsung gerak)
        if (p.includes("dom") || p.includes("inspeksi") || p.includes("inspect") || p.includes("scan")) {
            showDomInspectorModal();
            updateAgyHUD("BOT", "Membuka Inspeksi DOM", "success");
            return;
        }
        if (p.includes("auto fill") || p.includes("autofill") || p === "isi form" || p === "isi akun") {
            executeFormAutoFill();
            return;
        }
        if (p.includes("claim") || p.includes("klaim") || p.includes("airdrop") || p.includes("daily") || p.includes("reward") || p.includes("check in")) {
            executeSmartClaimAction();
            return;
        }
        if (p.includes("cf") || p.includes("turnstile") || p.includes("captcha")) {
            executeTurnstileSolver();
            return;
        }

        // 5. Scroll & Navigasi
        if (p.includes("scroll down") || p.includes("scroll bawah") || p === "turun" || p === "bawah") {
            window.scrollBy({ top: Math.round(window.innerHeight * 0.7), behavior: "smooth" });
            updateAgyHUD("BOT", "Menggulir halaman ke bawah", "success");
            return;
        }
        if (p.includes("scroll up") || p.includes("scroll atas") || p === "naik" || p === "atas") {
            window.scrollBy({ top: -Math.round(window.innerHeight * 0.7), behavior: "smooth" });
            updateAgyHUD("BOT", "Menggulir halaman ke atas", "success");
            return;
        }
        if (p === "reload" || p === "refresh" || p === "muat ulang") {
            updateAgyHUD("BOT", "Memuat ulang halaman...", "loading");
            setTimeout(function() { location.reload(); }, 300);
            return;
        }
        if (p === "back" || p === "kembali") {
            history.back();
            updateAgyHUD("BOT", "Kembali ke halaman sebelumnya", "success");
            return;
        }

        // 6. Evaluasi JavaScript / Kode Langsung
        if (raw.startsWith("javascript:") || raw.startsWith("js:") || raw.startsWith("eval:") || raw.startsWith("document.") || raw.startsWith("window.") || raw.startsWith("alert(")) {
            var code = raw.replace(/^(javascript:|js:|eval:)\s*/i, "");
            try {
                var res = eval(code);
                var resStr = (typeof res === "object" ? JSON.stringify(res) : String(res));
                updateAgyHUD("BOT: JS Dieksekusi", (resStr || "Selesai").substring(0, 50), "success");
            } catch(e) {
                updateAgyHUD("BOT: JS Error", e.message, "error");
            }
            return;
        }

        // 7. Perintah Klik Spesifik: "klik <nama>" / "click <nama>" / "pencet <nama>" / "tekan <nama>"
        var clickMatch = raw.match(/^(?:klik|click|pencet|tekan|tap|press)\s+(.+)$/i);
        if (clickMatch) {
            var targetQuery = clickMatch[1].trim();
            if (findAndClickElement(targetQuery)) return;
        }

        // 8. Perintah Isi Spesifik: "isi <kolom> <nilai>" / "fill <kolom> <nilai>" / "ketik <kolom> <nilai>"
        var fillMatch = raw.match(/^(?:isi|fill|ketik|type|input)\s+([^\s]+)\s+(.+)$/i);
        if (fillMatch) {
            var fieldName = fillMatch[1].trim();
            var fieldVal = fillMatch[2].trim();
            if (findAndFillElement(fieldName, fieldVal)) return;
        }

        // 9. Klik Langsung Teks Tombol (Cth: "Connect Wallet", "Swap", "Approve", dll)
        if (findAndClickElement(raw)) {
            return;
        }

        // 10. AI REASONING ROUTER: JIKA DOM LOKAL BELUM MENEMUKAN ELEMEN, GUNAKAN MESIN AI AKTIF
        var activeEngine = window.__ACTIVE_AI_ENGINE__ || "custom";
        try {
            if (window.GhostBridge && window.GhostBridge.getActiveAiEngine) {
                activeEngine = window.GhostBridge.getActiveAiEngine();
            }
        } catch(_) {}

        if (activeEngine === "agy") {
            try {
                if (window.GhostBridge && window.GhostBridge.forwardToAgy) {
                    var isAlive = false;
                    try {
                        isAlive = window.GhostBridge.isAgyAlive ? window.GhostBridge.isAgyAlive() : false;
                    } catch(_) {}

                    if (!isAlive) {
                        updateAgyHUD("🤖 AGY Copilot", "Menyalakan Antigravity di background...", "running");
                        if (window.GhostBridge.wakeUpAgy) window.GhostBridge.wakeUpAgy();
                    } else {
                        updateAgyHUD("🤖 AGY Copilot", "Mengirim instruksi ke Antigravity...", "running");
                    }

                    setTimeout(function() {
                        updateAgyHUD("🤖 AGY Copilot", "Antigravity sedang memproses instruksi...", "running");
                        var payload = JSON.stringify({
                            prompt: raw,
                            url: window.location.href,
                            title: document.title,
                            timestamp: Date.now()
                        });
                        var respStr = window.GhostBridge.forwardToAgy(payload);
                        try {
                            var parsed = JSON.parse(respStr);
                            if (parsed && (parsed.status === "received" || parsed.status === "ok")) {
                                updateAgyHUD("🤖 AGY Copilot", "Antigravity sedang mengeksekusi di browser...", "running");
                            } else {
                                updateAgyHUD("🤖 AGY Copilot", "Tugas terkirim ke AGY Copilot", "success");
                            }
                        } catch(_) {
                            updateAgyHUD("🤖 AGY Copilot", "Tugas terkirim ke AGY Copilot", "success");
                        }
                    }, isAlive ? 50 : 900);
                    return;
                }
            } catch(_) {}
        } else if (activeEngine === "9router") {
            updateAgyHUD("9Router AI", "Menganalisis elemen via 9Router (Port 20128)...", "running");
            try {
                if (window.GhostBridge && window.GhostBridge.postRpc) {
                    window.GhostBridge.postRpc(JSON.stringify({
                        method: "ai_completion",
                        params: ["9router", raw, window.location.href]
                    }));
                }
            } catch(_) {}
            setTimeout(function() {
                updateAgyHUD("9Router AI", "Instruksi diproses oleh 9Router", "success");
            }, 1000);
            return;
        } else {
            // Default: Custom AI (DeepSeek)
            updateAgyHUD("DeepSeek AI", "Menganalisis halaman via DeepSeek API...", "running");
            try {
                if (window.GhostBridge && window.GhostBridge.postRpc) {
                    window.GhostBridge.postRpc(JSON.stringify({
                        method: "ai_completion",
                        params: ["custom", raw, window.location.href]
                    }));
                }
            } catch(_) {}
            setTimeout(function() {
                updateAgyHUD("DeepSeek AI", "Instruksi diproses oleh DeepSeek", "success");
            }, 1000);
            return;
        }

        updateAgyHUD("BOT: Elemen Tidak Ditemukan", "\"" + raw + "\" tidak cocok dengan tombol apapun di halaman", "warning");
    }

    // Auto-resume active recipe on page reload / navigation
    (function checkAndResumeActiveRecipe() {
        try {
            var raw = sessionStorage.getItem("__agy_active_recipe_run__");
            if (!raw) return;
            var st = JSON.parse(raw);
            if (Date.now() - st.timestamp > 120000) {
                sessionStorage.removeItem("__agy_active_recipe_run__");
                return;
            }
            if (st.eventsList && st.idx < st.eventsList.length) {
                console.log("[AGY Co-Pilot] Melanjutkan resep setelah reload halaman ke langkah " + (st.idx + 1));
                setTimeout(function() {
                    updateAgyHUD(st.runTitle, "🔄 Melanjutkan langkah " + (st.idx + 1) + "/" + st.eventsList.length + "...", "running");
                    AgyBatchRunner.executeStepList(st.eventsList, st.curWallet, st.curEmail, st.runTitle, function(ok) {
                        sessionStorage.removeItem("__agy_active_recipe_run__");
                        updateAgyHUD(st.runTitle, ok ? "✅ Resep Selesai!" : "⚠️ Resep Berhenti", ok ? "success" : "warning");
                    }, st.idx);
                }, 1000);
            } else {
                sessionStorage.removeItem("__agy_active_recipe_run__");
            }
        } catch(e) {}
    })();

    // Auto-resume pending multi-step prompt flow across page reload
    (function checkAndResumePendingPromptQueue() {
        try {
            var raw = sessionStorage.getItem("__agy_pending_prompt_queue__");
            if (!raw) return;
            sessionStorage.removeItem("__agy_pending_prompt_queue__");
            var q = JSON.parse(raw);
            if (!q || !Array.isArray(q.steps) || typeof q.index !== "number") return;
            if (Date.now() - (q.timestamp || 0) > 120000) return;

            if (q.index < q.steps.length) {
                console.log("[AGY Co-Pilot] Melanjutkan antrean langkah setelah reload ke langkah " + (q.index + 1) + "/" + q.steps.length);
                setTimeout(function() {
                    executeMultiStepPlan(q.steps, q.index);
                }, 1400);
            }
        } catch(e) {
            console.warn("[AGY Co-Pilot] Gagal resume pending prompt queue:", e);
        }
    })();

    // Auto-detect Code Rekaman dari Hash URL saat halaman dimuat atau hash berubah
    try {
        checkAndAutoImportRecipeFromUrl();
        window.addEventListener("hashchange", checkAndAutoImportRecipeFromUrl);
    } catch(_) {}

    window.AgyRecorder = AgyRecorder;
    console.log("[AGY Co-Pilot] Smart Shield, Step Editor, Batch Runner & Settings Hub v3.2 Initialized.");
})();
