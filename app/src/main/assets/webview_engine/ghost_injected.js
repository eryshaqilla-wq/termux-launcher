(function() {
    if (window.__FUSION_GHOST_INJECTED__) return;
    window.__FUSION_GHOST_INJECTED__ = true;

    var GHOST_WALLET_SVG = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjggMTI4Ij48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjMGIxMDFkIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjMDQwNzExIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImJvZHkiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiMwMEYwRkYiLz48c3RvcCBvZmZzZXQ9IjUwJSIgc3RvcC1jb2xvcj0iIzAwQTNGRiIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzcwMDBGRiIvPjwvbGluZWFyR3JhZGllbnQ+PGZpbHRlciBpZD0iZ2xvdyIgeD0iLTIwJSIgeT0iLTIwJSIgd2lkdGg9IjE0MCUiIGhlaWdodD0iMTQwJSI+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMyIgcmVzdWx0PSJibHVyIi8+PGZlQ29tcG9zaXRlIGluPSJTb3VyY2VHcmFwaGljIiBpbjI9ImJsdXIiIG9wZXJhdG9yPSJvdmVyIi8+PC9maWx0ZXI+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiByeD0iMjgiIGZpbGw9InVybCgjYmcpIi8+PHJlY3Qgd2lkdGg9IjEyNiIgaGVpZ2h0PSIxMjYiIHg9IjEiIHk9IjEiIHJ4PSIyNyIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBGMEZGIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1vcGFjaXR5PSIwLjM1Ii8+PGcgZmlsdGVyPSJ1cmwoI2dsb3cpIj48cGF0aCBkPSJNNjQgMjQgQzQ0IDI0IDMwIDM5IDMwIDYwIEMzMCA3NiAzMCA5MiAzMCA5NiBDMzAgOTkgMzQgMTAxIDM3IDk4IEM0MSA5NCA0NiA5NCA1MCA5OCBDNTQgMTAyIDYwIDEwMiA2NCA5OCBDNjggOTQgNzQgOTQgNzggOTggQzgyIDEwMiA4OCAxMDIgOTEgOTggQzk0IDk0IDk4IDk3IDk4IDk2IEM5OCA5MiA5OCA3NiA5OCA2MCBDOTggMzkgODQgMjQgNjQgMjQgWiIgZmlsbD0idXJsKCNib2R5KSIvPjxlbGxpcHNlIGN4PSI1MCIgY3k9IjU2IiByeD0iNSIgcnk9IjgiIGZpbGw9IiMwNjA5MTMiLz48ZWxsaXBzZSBjeD0iNzgiIGN5PSI1NiIgcng9IjUiIHJ5PSI4IiBmaWxsPSIjMDYwOTEzIi8+PGVsbGlwc2UgY3g9IjUxLjUiIGN5PSI1NCIgcng9IjIiIHJ5PSIzLjUiIGZpbGw9IiNGRkZGRkYiLz48ZWxsaXBzZSBjeD0iNzkuNSIgY3k9IjU0IiByeD0iMiIgcnk9IjMuNSIgZmlsbD0iI0ZGRkZGRiIvPjwvZz48L3N2Zz4=";
    window.__GHOST_WALLET_SVG__ = GHOST_WALLET_SVG;

    // --- ACCESSIBILITY SKIP-LINK & SCROLL TRAP SHIELD ---
    try {
        var shieldCss = ".skip-link, a[href='#main'], .skip-to-content, [class*='skip-link'], [class*='skipToContent'] { display: none !important; opacity: 0 !important; pointer-events: none !important; visibility: hidden !important; height: 0 !important; width: 0 !important; }";
        var shieldStyle = document.createElement("style");
        shieldStyle.id = "__fusion_skip_link_shield__";
        shieldStyle.textContent = shieldCss;
        (document.head || document.documentElement).appendChild(shieldStyle);

        var blurAnchorIfFocused = function() {
            var active = document.activeElement;
            if (active && active !== document.body) {
                var tag = (active.tagName || "").toUpperCase();
                var href = active.getAttribute ? (active.getAttribute("href") || "") : "";
                var cls = active.className ? String(active.className) : "";
                if (tag === "A" || href.startsWith("#") || cls.indexOf("skip-link") !== -1 || cls.indexOf("skipToContent") !== -1) {
                    active.blur();
                }
            }
        };

        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", function() {
                if (!document.getElementById("__fusion_skip_link_shield__")) {
                    (document.head || document.documentElement).appendChild(shieldStyle);
                }
                blurAnchorIfFocused();
            }, { once: true });
        } else {
            blurAnchorIfFocused();
        }

        window.addEventListener("touchstart", blurAnchorIfFocused, { capture: true, passive: true });
    } catch(_) {}

    // --- CONTINUOUS BACKGROUND EXECUTION & VISIBILITY SHIELD ---
    try {
        Object.defineProperty(document, "hidden", {
            get: function() { return false; },
            configurable: true
        });
        Object.defineProperty(document, "visibilityState", {
            get: function() { return "visible"; },
            configurable: true
        });
        Object.defineProperty(document, "webkitVisibilityState", {
            get: function() { return "visible"; },
            configurable: true
        });
    } catch(_) {}

    // --- STORAGE SHIELD (PROTECT AGY STATE FROM STORAGE WIPES) ---
    try {
        if (window.sessionStorage && typeof window.sessionStorage.clear === "function") {
            var origSessionClear = window.sessionStorage.clear.bind(window.sessionStorage);
            window.sessionStorage.clear = function() {
                var preserved = {};
                try {
                    for (var i = 0; i < window.sessionStorage.length; i++) {
                        var k = window.sessionStorage.key(i);
                        if (k && (k.startsWith("__agy_") || k.startsWith("__fusion_") || k.startsWith("agy_") || k.startsWith("fusion_"))) {
                            preserved[k] = window.sessionStorage.getItem(k);
                        }
                    }
                } catch(_) {}
                origSessionClear();
                try {
                    for (var pk in preserved) {
                        window.sessionStorage.setItem(pk, preserved[pk]);
                    }
                } catch(_) {}
            };
        }
        if (window.localStorage && typeof window.localStorage.clear === "function") {
            var origLocalClear = window.localStorage.clear.bind(window.localStorage);
            window.localStorage.clear = function() {
                var preserved = {};
                try {
                    for (var i = 0; i < window.localStorage.length; i++) {
                        var k = window.localStorage.key(i);
                        if (k && (k.startsWith("__agy_") || k.startsWith("__fusion_") || k.startsWith("agy_") || k.startsWith("fusion_"))) {
                            preserved[k] = window.localStorage.getItem(k);
                        }
                    }
                } catch(_) {}
                origLocalClear();
                try {
                    for (var pk in preserved) {
                        window.localStorage.setItem(pk, preserved[pk]);
                    }
                } catch(_) {}
            };
        }
    } catch(_) {}

    // --- WEB NOTIFICATION SHIELD & X.COM PUSH ENGINE ---
    (function setupWebNotifications() {
        function MockNotification(title, options) {
            options = options || {};
            var body = options.body || "";
            var icon = options.icon || options.image || "";
            var targetUrl = (options.data && options.data.url) ? options.data.url : window.location.href;

            try {
                if (window.GhostBridge && window.GhostBridge.showWebNotification) {
                    window.GhostBridge.showWebNotification(String(title), String(body), String(icon), String(targetUrl));
                }
            } catch(_) {}

            this.title = title;
            this.body = body;
            this.icon = icon;
            this.tag = options.tag || "";
            this.data = options.data || {};
            this.onclick = null;
            this.onclose = null;
            this.onerror = null;
            this.onshow = null;
            this.close = function() {};

            var self = this;
            setTimeout(function() {
                if (typeof self.onshow === "function") {
                    try { self.onshow(new Event("show")); } catch(_) {}
                }
            }, 50);
        }

        MockNotification.permission = "granted";
        MockNotification.requestPermission = function(callback) {
            if (typeof callback === "function") {
                try { callback("granted"); } catch(_) {}
            }
            return Promise.resolve("granted");
        };
        MockNotification.maxActions = 2;

        try {
            window.Notification = MockNotification;
        } catch(_) {}

        // 1. Shims navigator.permissions.query for "notifications" & "push"
        try {
            if (navigator.permissions && navigator.permissions.query) {
                var origQuery = navigator.permissions.query.bind(navigator.permissions);
                navigator.permissions.query = function(param) {
                    if (param && (param.name === "notifications" || param.name === "push")) {
                        return Promise.resolve({
                            state: "granted",
                            name: param.name,
                            onchange: null,
                            addEventListener: function() {},
                            removeEventListener: function() {},
                            dispatchEvent: function() { return true; }
                        });
                    }
                    return origQuery(param);
                };
            }
        } catch(_) {}

        // 2. Shims ServiceWorkerRegistration.prototype.showNotification
        try {
            if (typeof ServiceWorkerRegistration !== "undefined" && ServiceWorkerRegistration.prototype) {
                ServiceWorkerRegistration.prototype.showNotification = function(title, options) {
                    new MockNotification(title, options);
                    return Promise.resolve();
                };
            }
        } catch(_) {}
    })();

    // --- SMART CHAIN AUTO-DETECTION FROM LIVE WEB DATA ---
    function mapChainIdToKey(chainId) {
        var id = (typeof chainId === "string" && chainId.indexOf("0x") === 0) ? parseInt(chainId, 16) : parseInt(chainId, 10);
        if (isNaN(id)) return null;
        switch (id) {
            case 8453: return "base";
            case 56: return "bsc";
            case 42161: return "arbitrum";
            case 137: return "polygon";
            case 2818: return "morph";
            case 5042: return "arc-mainnet";
            case 1: return "eth";
            case 501: return "solana";
            case 784: return "sui";
            case 101:
            case 637: return "aptos";
            case 4218: return "iota";
            case 8822: return "iotaevm";
            case 728126428: return "tron";
            default: return null;
        }
    }

    function reportDappChainToBridge(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    }
    window.reportDappChainToBridge = reportDappChainToBridge;

    function scanDappChainFromWebData() {
        try {
            // 0. Direct URL / Hostname detection for fast switching
            try {
                var loc = (window.location && window.location.href) ? window.location.href.toLowerCase() : "";
                if (loc.indexOf("jup.ag") !== -1 || loc.indexOf("raydium.io") !== -1 || loc.indexOf("pump.fun") !== -1 || loc.indexOf("orca.so") !== -1 || loc.indexOf("solflare.com") !== -1 || loc.indexOf("phantom.app") !== -1) {
                    reportDappChainToBridge("solana", 501);
                    return;
                }
                if (loc.indexOf("magicsea.finance") !== -1 || loc.indexOf("tangle.tools") !== -1 || loc.indexOf("iotabee.com") !== -1) {
                    reportDappChainToBridge("iotaevm", 8822);
                    return;
                }
                if (loc.indexOf("iota.org") !== -1 || loc.indexOf("iota.cafe") !== -1 || loc.indexOf("iota") !== -1) {
                    reportDappChainToBridge("iota", 4218);
                    return;
                }
                if (loc.indexOf("tronlink.org") !== -1 || loc.indexOf("sunpump.meme") !== -1 || loc.indexOf("sunswap.com") !== -1 || loc.indexOf("tronscan.org") !== -1) {
                    reportDappChainToBridge("tron", 728126428);
                    return;
                }
                if (loc.indexOf("suins.io") !== -1 || loc.indexOf("cetus.zone") !== -1 || loc.indexOf("turbos.finance") !== -1 || loc.indexOf("navi.protocol") !== -1 || loc.indexOf("sui") !== -1) {
                    reportDappChainToBridge("sui", 784);
                    return;
                }
                if (loc.indexOf("liquidswap.com") !== -1 || loc.indexOf("thalaswap") !== -1 || loc.indexOf("aptoslabs.com") !== -1 || loc.indexOf("amnis.finance") !== -1 || loc.indexOf("aptos") !== -1) {
                    reportDappChainToBridge("aptos", 637);
                    return;
                }
            } catch(_) {}

            // 1. Wagmi / Web3Modal / AppKit store in localStorage
            if (window.localStorage) {
                var wagmiStore = localStorage.getItem("wagmi.store");
                if (wagmiStore) {
                    try {
                        var parsed = JSON.parse(wagmiStore);
                        var state = parsed.state || parsed;
                        var cId = state.chainId || state.current;
                        if (cId) {
                            var cKey = mapChainIdToKey(cId);
                            if (cKey) {
                                reportDappChainToBridge(cKey, cId);
                                return;
                            }
                        }
                    } catch(_) {}
                }
                var directCid = localStorage.getItem("@w3m/chain_id") || 
                                localStorage.getItem("appkit_chain_id") || 
                                localStorage.getItem("connected_chain_id");
                if (directCid) {
                    var cidNum = parseInt(directCid, 10);
                    if (!isNaN(cidNum) && cidNum > 0) {
                        var cKey = mapChainIdToKey(cidNum);
                        if (cKey) {
                            reportDappChainToBridge(cKey, cidNum);
                            return;
                        }
                    }
                }
            }

            // 2. Global JS configuration (Wagmi, Next.js, Web3 clients)
            if (window.wagmiConfig && window.wagmiConfig.state && window.wagmiConfig.state.chainId) {
                var cId = window.wagmiConfig.state.chainId;
                var cKey = mapChainIdToKey(cId);
                if (cKey) {
                    reportDappChainToBridge(cKey, cId);
                    return;
                }
            }
            if (window.__wagmi && window.__wagmi.state && window.__wagmi.state.chainId) {
                var cId = window.__wagmi.state.chainId;
                var cKey = mapChainIdToKey(cId);
                if (cKey) {
                    reportDappChainToBridge(cKey, cId);
                    return;
                }
            }

            // 3. Next.js pageProps data
            if (window.__NEXT_DATA__ && window.__NEXT_DATA__.props && window.__NEXT_DATA__.props.pageProps) {
                var pp = window.__NEXT_DATA__.props.pageProps;
                var cId = pp.chainId || pp.initialChainId || pp.defaultChainId || (pp.initialState && pp.initialState.chainId);
                if (cId) {
                    var cKey = mapChainIdToKey(cId);
                    if (cKey) {
                        reportDappChainToBridge(cKey, cId);
                        return;
                    }
                }
            }

            // 4. Multi-chain SDK globals
            if (window.solanaWeb3 || window.__SOLANA_WEB3__) {
                reportDappChainToBridge("solana", 501);
                return;
            }
            if (window.IotaClient || window.__IOTA__) {
                reportDappChainToBridge("iota", 4218);
                return;
            }
            if (window.SuiClient || window.__SUI__) {
                reportDappChainToBridge("sui", 784);
                return;
            }
            if (window.AptosClient) {
                reportDappChainToBridge("aptos", 637);
                return;
            }
            if (window.TronWeb) {
                reportDappChainToBridge("tron", 728126428);
                return;
            }

            // 5. Meta tags in DOM
            var metaChain = document.querySelector('meta[name="chain-id"]') || 
                            document.querySelector('meta[name="chain"]') || 
                            document.querySelector('meta[name="network"]');
            if (metaChain && metaChain.content) {
                var val = metaChain.content.trim().toLowerCase();
                var cId = parseInt(val, 10);
                if (!isNaN(cId) && cId > 0) {
                    var cKey = mapChainIdToKey(cId);
                    if (cKey) {
                        reportDappChainToBridge(cKey, cId);
                        return;
                    }
                } else if (val.length > 0) {
                    reportDappChainToBridge(val, 0);
                    return;
                }
            }
        } catch(_) {}
    }
    window.__fusionScanDappChain = scanDappChainFromWebData;

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", function() {
            scanDappChainFromWebData();
            setTimeout(scanDappChainFromWebData, 600);
            setTimeout(scanDappChainFromWebData, 1800);
        }, { once: true });
    } else {
        scanDappChainFromWebData();
        setTimeout(scanDappChainFromWebData, 600);
        setTimeout(scanDappChainFromWebData, 1800);
    }

    window.__ghostIsDaemonAlive = function() {
        if (window.GhostBridge && window.GhostBridge.isDaemonAlive) {
            try { return window.GhostBridge.isDaemonAlive(); } catch(e) {}
        }
        return false;
    };
    window.__ghostRequireDaemon = function() {
        if (!window.__ghostIsDaemonAlive()) {
            if (window.GhostBridge && window.GhostBridge.notifyDaemonOffline) {
                try { window.GhostBridge.notifyDaemonOffline(); } catch(e) {}
            }
            var err = new Error("Daemon wallet.js belum aktif! Harap start daemon terlebih dahulu.");
            err.code = 4001;
            throw err;
        }
    };
    var isDaemon = window.__ghostIsDaemonAlive;
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
            scanDappChainFromWebData();
            var activeAddr = (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : walletAddress;
            if (activeAddr) {
                walletAddress = activeAddr;
                fakeProvider.selectedAddress = activeAddr;
                return [activeAddr];
            }
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
        }
        if (method === 'eth_chainId') return (window.GhostBridge && window.GhostBridge.getChainIdHex) ? window.GhostBridge.getChainIdHex() : fakeProvider.chainId;
        if (method === 'net_version') return (window.GhostBridge && window.GhostBridge.getChainIdDec) ? window.GhostBridge.getChainIdDec() : fakeProvider.networkVersion;
        if (method === 'wallet_getCapabilities') return {};
        if (method === 'wallet_requestPermissions') {
            var activeAddr = (window.GhostBridge && window.GhostBridge.getWalletAddress) ? window.GhostBridge.getWalletAddress() : walletAddress;
            if (!activeAddr) {
                var alive = isDaemon();
                if (!alive) {
                    if (window.GhostBridge && window.GhostBridge.notifyDaemonOffline) {
                        try { window.GhostBridge.notifyDaemonOffline(); } catch(e) {}
                    }
                    var err = new Error("Daemon wallet.js belum aktif! Harap start daemon terlebih dahulu.");
                    err.code = 4001;
                    throw err;
                }
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
                    var k = mapChainIdToKey(dec);
                    if (k) {
                        reportDappChainToBridge(k, dec);
                    }
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
        isTrust: true,
        isTrustWallet: true,
        isOkxWallet: true,
        isBitget: true,
        isBitKeep: true,
        isBinance: true,
        isBaw: true,
        isRabby: true,
        isCoinbaseWallet: true,
        isPhantom: true,
        isTokenPocket: true,
        isGhost: true,
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
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.ethereum = fakeProvider;
    window.bitkeep = window.bitkeep || {};
    window.bitkeep.ethereum = fakeProvider;
    window.trustwallet = window.trustwallet || {};
    window.trustwallet.ethereum = fakeProvider;
    window.coinbaseWalletExtension = fakeProvider;
    window.binance = fakeProvider;
    window.BinanceChain = fakeProvider;
    window.rabby = fakeProvider;

    var KTP_DB = [
        { id: "metamask", uuid: "11111111-90fa-40da-8d1e-089b3f3edcc2", name: "MetaMask", rdns: "io.metamask.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyOCIgaGVpZ2h0PSIyOCIgZmlsbD0ibm9uZSIgdmlld0JveD0iMCAwIDI4IDI4Ij48cGF0aCBmaWxsPSIjZmZmIiBkPSJNMCAwaDI4djI4SDB6Ii8+PGcgY2xpcC1wYXRoPSJ1cmwoI2EpIj48cGF0aCBmaWxsPSIjZmY1YzE2IiBkPSJtMjQuMDI0IDIzLjgyNC00Ljg0Ni0xLjQzNC0zLjY1NSAyLjE3Mi0yLjU1LS4wMDEtMy42NTYtMi4xNzEtNC44NDQgMS40MzRMMyAxOC44OGwxLjQ3My01LjQ4OEwzIDguNzUxIDQuNDczIDNsNy41NjkgNC40OTZoNC40MTNMMjQuMDI0IDNsMS40NzMgNS43NTEtMS40NzMgNC42NCAxLjQ3MyA1LjQ4OHoiLz48cGF0aCBmaWxsPSIjZmY1YzE2IiBkPSJtNC40NzQgMyA3LjU3IDQuNDk5LS4zMDIgMy4wODd6bTQuODQ0IDE1Ljg4MSAzLjMzIDIuNTIyLTMuMzMuOTg3em0zLjA2NC00LjE3LS42NC00LjEyMy00LjA5NyAyLjgwNGgtLjAwMnYuMDAxbC4wMTMgMi44ODYgMS42NjEtMS41Njd6TTI0LjAyNCAzbC03LjU3IDQuNDk5LjMgMy4wODd6TTE5LjE4IDE4Ljg4MWwtMy4zMyAyLjUyMiAzLjMzLjk4N3ptMS42NzQtNS40ODh2LS4wMDJ6bC00LjA5Ny0yLjgwNC0uNjQgNC4xMjRoMy4wNjRsMS42NjIgMS41Njd6Ii8+PHBhdGggZmlsbD0iI2UzNDgwNyIgZD0ibTkuMzE3IDIyLjM5LTQuODQ0IDEuNDM0TDMgMTguODgxaDYuMzE3em0zLjA2NC03LjY4LjkyNSA1Ljk2Mi0xLjI4Mi0zLjMxNS00LjM3LTEuMDc4IDEuNjYyLTEuNTY4em02Ljc5OSA3LjY4IDQuODQ0IDEuNDM0IDEuNDczLTQuOTQzSDE5LjE4em0tMy4wNjQtNy42OC0uOTI1IDUuOTYyIDEuMjgyLTMuMzE1IDQuMzctMS4wNzgtMS42NjMtMS41Njh6Ii8+PHBhdGggZmlsbD0iI2ZmOGQ1ZCIgZD0ibTMgMTguODggMS40NzMtNS40ODloMy4xNjlsLjAxMiAyLjg4NyA0LjM3IDEuMDc4IDEuMjgyIDMuMzE0LS42NTkuNzMtMy4zMy0yLjUyMkgzem0yMi40OTcgMC0xLjQ3My01LjQ4OWgtMy4xN2wtLjAxIDIuODg3LTQuMzcxIDEuMDc4LTEuMjgyIDMuMzE0LjY1OS43MyAzLjMzLTIuNTIyaDYuMzE3ek0xNi40NTUgNy40OTVoLTQuNDEzbC0uMyAzLjA4NyAxLjU2NSAxMC4wODRoMS44ODRsMS41NjUtMTAuMDg0eiIvPjxwYXRoIGZpbGw9IiM2NjE4MDAiIGQ9Ik00LjQ3MyAzIDMgOC43NTFsMS40NzMgNC42NGgzLjE2OWw0LjEtMi44MDV6bTYuOTkyIDEyLjkwOEgxMC4wM2wtLjc4MS43NjEgMi43NzYuNjg1LS41Ni0xLjQ0N00yNC4wMjQgM2wxLjQ3MyA1Ljc1MS0xLjQ3MyA0LjY0aC0zLjE3bC00LjA5OC0yLjgwNXptLTYuOTkgMTIuOTA4aDEuNDM3bC43ODIuNzYyLTIuNzguNjg2LjU2LTEuNDV6bS0xLjUxMiA2LjY4Ny4zMjgtMS4xOTMtLjY2LS43M2gtMS44ODVsLS42NTkuNzMuMzI3IDEuMTkyIi8+PHBhdGggZmlsbD0iI2MwYzRjZCIgZD0iTTE1LjUyMiAyMi41OTR2MS45NjloLTIuNTQ4di0xLjk2OXoiLz48cGF0aCBmaWxsPSIjZTdlYmY2IiBkPSJtOS4zMTggMjIuMzg4IDMuNjU4IDIuMTc0di0xLjk2OWwtLjMyOC0xLjE5MnptOS44NjIgMC0zLjY1OCAyLjE3NHYtMS45NjlsLjMyOC0xLjE5MnoiLz48L2c+PGRlZnM+PGNsaXBQYXRoIGlkPSJhIj48cGF0aCBmaWxsPSIjZmZmIiBkPSJNMyAzaDIyLjV2MjEuNTYzSDN6Ii8+PC9jbGlwUGF0aD48L2RlZnM+PC9zdmc+" },
        { id: "trustwallet", uuid: "22222222-40da-40da-8d1e-089b3f3edcc2", name: "Trust Wallet", rdns: "com.trustwallet.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHdpZHRoPSIyOCIgaGVpZ2h0PSIyOCIgdmlld0JveD0iMCAwIDI4IDI4Ij48cGF0aCBmaWxsPSIjZmZmIiBkPSJNMCAwaDI4djI4SDB6Ii8+PHBhdGggZmlsbD0iIzA1MDBGRiIgZD0iTTYgNy41ODMgMTMuNTMgNXYxNy44ODJDOC4xNSAyMC40OTggNiAxNS45MjggNiAxMy4zNDVWNy41ODNaIi8+PHBhdGggZmlsbD0idXJsKCNhKSIgZD0iTTIyIDcuNTgzIDEzLjUzIDV2MTcuODgyYzYuMDUtMi4zODQgOC40Ny02Ljk1NCA4LjQ3LTkuNTM3VjcuNTgzWiIvPjxkZWZzPjxsaW5lYXJHcmFkaWVudCBpZD0iYSIgeDE9IjE5Ljc2OCIgeDI9IjE0LjA3MiIgeTE9IjMuNzUzIiB5Mj0iMjIuODUzIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHN0b3Agb2Zmc2V0PSIuMDIiIHN0b3AtY29sb3I9IiMwMEYiLz48c3RvcCBvZmZzZXQ9Ii4wOCIgc3RvcC1jb2xvcj0iIzAwOTRGRiIvPjxzdG9wIG9mZnNldD0iLjE2IiBzdG9wLWNvbG9yPSIjNDhGRjkxIi8+PHN0b3Agb2Zmc2V0PSIuNDIiIHN0b3AtY29sb3I9IiMwMDk0RkYiLz48c3RvcCBvZmZzZXQ9Ii42OCIgc3RvcC1jb2xvcj0iIzAwMzhGRiIvPjxzdG9wIG9mZnNldD0iLjkiIHN0b3AtY29sb3I9IiMwNTAwRkYiLz48L2xpbmVhckdyYWRpZW50PjwvZGVmcz48L3N2Zz4K" },
        { id: "uniswap", uuid: "33333333-40da-40da-8d1e-089b3f3edcc2", name: "Uniswap Wallet", rdns: "org.uniswap.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyOCIgaGVpZ2h0PSIyOCIgZmlsbD0ibm9uZSI+PHBhdGggZmlsbD0iI0ZGRDhFQSIgZD0iTTAgMGgyOHYyOEgweiIvPjxnIGZpbGw9IiNGRjAwN0EiIGNsaXAtcGF0aD0idXJsKCNhKSI+PHBhdGggZD0iTTkuNjM1IDYuMDY4Yy0uMjU1LS4wMzktLjI2Ni0uMDQzLS4xNDUtLjA2LjIzLS4wMzUuNzczLjAxMiAxLjE0OC4wOTkuODc0LjIwMiAxLjY3LjcyMiAyLjUxOSAxLjY0NmwuMjI1LjI0Ni4zMjMtLjA1MWMxLjM2LS4yMTMgMi43NDEtLjA0NCAzLjg5OS40NzcuMzE5LjE0NC44MjEuNDMuODgyLjUwMy4wMjIuMDI0LjA1OC4xNzMuMDgyLjMzNS4wODYuNTU3LjA0NC45ODItLjEzIDEuMzAxLS4wOTYuMTczLS4xMDEuMjI4LS4wMzcuMzc3YS4zNjcuMzY3IDAgMCAwIC4zMzQuMjA2Yy4yODcgMCAuNTk3LS40NTQuNzQtMS4wODZsLjA1Ni0uMjUyLjExMy4xMjZjLjYyLjY4NSAxLjEwNiAxLjYxOCAxLjE4OSAyLjI4MmwuMDIxLjE3NC0uMTAzLS4xNThhMS45NjYgMS45NjYgMCAwIDAtLjU5LS42MDRjLS40MTUtLjI2OC0uODU1LS4zNi0yLjAxNy0uNDItMS4wNS0uMDUzLTEuNjQ1LS4xNDEtMi4yMzQtLjMyOS0xLjAwMy0uMzItMS41MDgtLjc0NC0yLjY5OS0yLjI3LS41My0uNjc4LS44NTUtMS4wNTMtMS4xOC0xLjM1NC0uNzQtLjY4Ny0xLjQ2Ni0xLjA0Ny0yLjM5Ni0xLjE4N3YtLjAwMVoiLz48cGF0aCBkPSJNMTguNzI1IDcuNTgxYy4wMjYtLjQ1My4wODgtLjc1My4yMTYtMS4wMjYuMDI4LS4wNy4wNjMtLjEzNi4xMDQtLjE5OGEuNjkuNjkgMCAwIDEtLjA0OS4xNzhjLS4wOTIuMjY2LS4xMDguNjI5LS4wNDMgMS4wNTIuMDgxLjUzNi4xMjYuNjEzLjcwNiAxLjE5Mi4yNzMuMjcyLjU4OS42MTMuNzA0Ljc2MWwuMjEuMjY3LS4yMS0uMTkxYy0uMjU1LS4yMzMtLjg0MS0uNjktLjk3Mi0uNzU1LS4wODYtLjA0Mi0uMS0uMDQyLS4xNTMuMDEtLjA1LjA0OC0uMDYuMTItLjA2OC40NjEtLjAxLjUzMi0uMDg0Ljg3My0uMjYzIDEuMjEzLS4wOTYuMTg3LS4xMTEuMTQ3LS4wMjUtLjA2Mi4wNjYtLjE1NS4wNzItLjIyNC4wNzItLjczOSAwLTEuMDMzLS4xMjctMS4yODMtLjg2NC0xLjcwN2E4LjE2OSA4LjE2OSAwIDAgMC0uNjg0LS4zNDYgMi42MTQgMi42MTQgMCAwIDEtLjMzNC0uMTZjLjAyLS4wMi43NC4xODUgMS4wMjkuMjkzLjQzMS4xNjMuNTAyLjE4My41NTUuMTY0LjAzNC0uMDEzLjA1MS0uMTEzLjA2OC0uNDA3aC4wMDFabS04LjU5NSAxLjc3Yy0uNTE3LS42OTYtLjgzOC0xLjc2NC0uNzY4LTIuNTYzbC4wMi0uMjQ3LjExOS4wMmMuMjIyLjA0LjYwMy4xNzkuNzgzLjI4Ni40OS4yOS43MDMuNjczLjkxOCAxLjY1Ny4wNjQuMjg4LjE0Ny42MTMuMTg1LjcyNS4wNjEuMTc4LjI5NC41OTIuNDgzLjg2Mi4xMzYuMTk0LjA0Ni4yODYtLjI1NS4yNi0uNDYtLjA0LTEuMDgzLS40Ni0xLjQ4NS0xWm03Ljk2NSA1LjE4MmMtMi40Mi0uOTUtMy4yNzMtMS43NzctMy4yNzMtMy4xNyAwLS4yMDYuMDA3LS4zNzQuMDE2LS4zNzQuMDA4IDAgLjEwMi4wNjcuMjA4LjE1MS40OTEuMzg0IDEuMDQuNTQ4IDIuNTYzLjc2NS44OTYuMTI4IDEuMzk4LjIzMSAxLjg2NC4zODIgMS40OC40NzggMi4zOTIgMS40NDkgMi42MSAyLjc3My4wNjMuMzg0LjAyOCAxLjEwNC0uMDc2IDEuNDg0LS4wODIuMy0uMzI5Ljg0LS4zOTQuODYxLS4wMTkuMDA3LS4wMzctLjA2MS0uMDQtLjE1NC0uMDI1LS40OTYtLjI4My0uOTgtLjcxNS0xLjM0My0uNDktLjQxMi0xLjE1LS43NDEtMi43NjMtMS4zNzVabS0xLjY5OS4zOTZhNC4xMyA0LjEzIDAgMCAwLS4xMTctLjVsLS4wNjItLjE4LjExNS4xMjdjLjE2LjE3My4yODUuMzk2LjM5MS42OTMuMDgyLjIyNy4wOS4yOTQuMDkuNjYyIDAgLjM2MS0uMDExLjQzNy0uMDg3LjY0LS4xMDQuMy0uMjguNTcyLS41MS43OTQtLjQ0My40NC0xLjAxMS42ODMtMS44MzMuNzg0LS4xNDIuMDE4LS41NTcuMDQ3LS45MjIuMDY2LS45MjIuMDQ2LTEuNTI5LjE0NC0yLjA3My4zM2EuNDQ2LjQ0NiAwIDAgMS0uMTU2LjAzNmMtLjAyMS0uMDIxLjM1LS4yMzcuNjU2LS4zODEuNDMxLS4yMDQuODYxLS4zMTUgMS44MjUtLjQ3LjQ3NS0uMDc4Ljk2OC0uMTczIDEuMDkzLS4yMSAxLjE3OC0uMzUyIDEuNzg0LTEuMjYzIDEuNTktMi4zOVoiLz48cGF0aCBkPSJNMTcuNTA2IDE2Ljg1NWMtLjMyMS0uNjc1LS4zOTYtMS4zMjctLjIyLTEuOTM1LjAyLS4wNjcuMDUtLjExOS4wNjgtLjExOWEuNjQuNjQgMCAwIDEgLjE2OS4wOWMuMTQ5LjA5Ny40NDUuMjYxIDEuMjM3LjY4Mi45ODguNTI3IDEuNTUzLjkzNCAxLjkzNiAxLjQuMzM1LjQwNy41NDIuODcyLjY0MyAxLjQzOC4wNTcuMzIuMDIzIDEuMDkzLS4wNjEgMS40MTYtLjI2NiAxLjAxOC0uODgyIDEuODE4LTEuNzY0IDIuMjg1YTIuMTI5IDIuMTI5IDAgMCAxLS4yNTguMTI0Yy0uMDEzIDAgLjAzNS0uMTE2LjEwNC0uMjU5LjI5Ny0uNjA1LjMzMS0xLjE5My4xMDctMS44NDgtLjEzOC0uNDAxLS40MTgtLjg5LS45ODItMS43MTgtLjY1Ny0uOTYtLjgxNy0xLjIxOC0uOTc5LTEuNTU2Wm0tOS4wOTUgMy42NDRjLjktLjc0MiAyLjAxNi0xLjI2NyAzLjAzNi0xLjQzYTQuODUgNC44NSAwIDAgMSAxLjU3NS4wNmMuNjUyLjE2MyAxLjIzNS41MjggMS41MzguOTYyLjI5Ni40MjUuNDIzLjc5NC41NTYgMS42MTguMDUyLjMyNC4xMDkuNjUxLjEyNS43MjQuMS40MjcuMjkzLjc2OC41MzIuOTM5LjM4LjI3MiAxLjAzNy4yOSAxLjY4Mi4wNDRhLjg1My44NTMgMCAwIDEgLjIxLS4wNjRjLjAyMy4wMjMtLjMwMS4yMzUtLjUzLjM0Ny0uMjcuMTQxLS41NzIuMjEzLS44NzkuMjA4LS41OSAwLTEuMDgtLjI5NC0xLjQ5LS44OTFhNy4wMjcgNy4wMjcgMCAwIDEtLjQwMi0uNzg0Yy0uNDMxLS45NjEtLjY0NC0xLjI1My0xLjE0NS0xLjU3NS0uNDM3LS4yOC0xLS4zMjktMS40MjQtLjEyNi0uNTU2LjI2Ni0uNzEuOTYtLjMxMiAxLjQuMTg3LjE4OS40My4zMTMuNjk1LjM1NGEuNzUuNzUgMCAwIDAgLjU4OS0uMTc0LjcyOC43MjggMCAwIDAgLjI1MS0uNTUxYzAtLjI5LS4xMTQtLjQ1My0uNC0uNTgxLS4zOTItLjE3Mi0uODEyLjAyOS0uODEuMzg4IDAgLjE1My4wNjkuMjQ5LjIyNi4zMi4xLjA0NC4xMDMuMDQ2LjAyLjAzLS4zNTgtLjA3My0uNDQyLS40OTYtLjE1NC0uNzc2LjM0Ny0uMzM2IDEuMDY0LS4xODggMS4zMS4yNzIuMTAzLjE5NC4xMTUuNTc4LjAyNS44MS0uMjAzLjUyLS43OS43OTMtMS4zODcuNjQ0LS40MDctLjEwMi0uNTcxLS4yMTEtMS4wNi0uNzAzLS44NTItLjg1Ni0xLjE4Mi0xLjAyMy0yLjQwOC0xLjIxbC0uMjM3LS4wMzUuMjY4LS4yMloiLz48cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik00LjQxOCA1LjE3OWMyLjg0MiAzLjM2OCA0LjggNC43NTcgNS4wMTcgNS4wNS4xOC4yNDMuMTEyLjQ2Mi0uMTk1LjYzMmExLjg5OSAxLjg5OSAwIDAgMS0uNjk5LjE5MWMtLjIgMC0uMjY5LS4wNzUtLjI2OS0uMDc1LS4xMTUtLjEwNi0uMTgtLjA4OC0uNzczLTEuMTE2QTE3Ni45MTEgMTc2LjkxMSAwIDAgMCA1Ljk2OCA3LjU2Yy0uMDQ0LS4wNC0uMDQyLS4wNCAxLjQ0OCAyLjU2NC4yNC41NDMuMDQ3Ljc0MS4wNDcuODE5IDAgLjE1Ny0uMDQ0LjI0LS4yNDIuNDU3LS4zMzIuMzYtLjQ4Ljc2NS0uNTg3IDEuNjA0LS4xMi45NC0uNDU2IDEuNjA0LTEuMzkgMi43NDEtLjU0NS42NjYtLjYzNS43ODctLjc3MyAxLjA1Ni0uMTc0LjMzOC0uMjIxLjUyNy0uMjQuOTUyYTIuOSAyLjkgMCAwIDAgLjE2IDEuMTc0Yy4xMjIuMzc4LjI1Mi42MjYuNTgyIDEuMTI1LjI4NS40My40NDguNzUuNDQ4Ljg3NiAwIC4wOTkuMDE5LjA5OS40NjEuMDAxIDEuMDU0LS4yMzIgMS45MS0uNjQgMi4zOTItMS4xNDIuMjk4LS4zMTEuMzY3LS40ODIuMzctLjkwNy4wMDItLjI3Ny0uMDA4LS4zMzYtLjA4NS0uNDk2LS4xMjUtLjI2LS4zNTMtLjQ3Ny0uODU1LS44MTMtLjY2LS40NC0uOTQyLS43OTQtMS4wMTgtMS4yOC0uMDY1LS40LjAxLS42ODIuMzczLTEuNDI3LjM3Ny0uNzczLjQ3LTEuMTAzLjUzNS0xLjg4LjA0LS41MDMuMDk2LS43MDEuMjQ0LS44NjEuMTU1LS4xNjYuMjkzLS4yMjIuNjc2LS4yNzIuNjIzLS4wODQgMS4wMTgtLjI0IDEuMzQzLS41MzRhMS4xNCAxLjE0IDAgMCAwIC40MjEtLjg2OWwuMDE0LS4yOC0uMTYtLjE3OUM5LjU2MyA5LjM0IDQuMDM2IDQuNjY3IDQgNC42NjdjLS4wMDcgMCAuMTguMjMuNDE4LjUxMlptMS4zMjUgMTMuMDg2YS40ODYuNDg2IDAgMCAwLS4xNTctLjY1MmMtLjIwMy0uMTMzLS41MjItLjA2OS0uNTIyLjEwMyAwIC4wNTMuMDMuMDkyLjA5OC4xMjUuMTE0LjA1OC4xMjIuMTIyLjAzMi4yNTRzLS4wODIuMjQ4LjAyLjMyOGMuMTY4LjEyNi40MDQuMDU3LjUyOS0uMTU4Wk0xMC42ODMgMTJjLS4yOS4wODYtLjU3NS4zODktLjY2NC43MDYtLjA1My4xOTQtLjAyMi41MzQuMDU4LjYzOC4xMy4xNjkuMjU1LjIxMy41OTYuMjEyLjY2NS0uMDA0IDEuMjQzLS4yODMgMS4zMTEtLjYzMi4wNTQtLjI4Ni0uMi0uNjgtLjU1LS44NTVhMS4zMyAxLjMzIDAgMCAwLS43NS0uMDdabS43OC41OTNjLjEwMi0uMTQzLjA1Ny0uMjk2LS4xMTgtLjQtLjMzMS0uMTk5LS44MzUtLjAzNS0uODM1LjI3MiAwIC4xNTMuMjYyLjMyLjUwMi4zMi4xNiAwIC4zOC0uMDkzLjQ1LS4xOTJaIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiLz48L2c+PGRlZnM+PGNsaXBQYXRoIGlkPSJhIj48cGF0aCBmaWxsPSIjZmZmIiBkPSJNNCA0aDE5djIwSDR6Ii8+PC9jbGlwUGF0aD48L2RlZnM+PC9zdmc+" },
        { id: "rainbow", uuid: "44444444-40da-40da-8d1e-089b3f3edcc2", name: "Rainbow", rdns: "me.rainbow", icon: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMjAiIGhlaWdodD0iMTIwIiBmaWxsPSJ1cmwoI3BhaW50MF9saW5lYXJfNjJfMzI5KSIvPgo8cGF0aCBkPSJNMjAgMzhIMjZDNTYuOTI3OSAzOCA4MiA2My4wNzIxIDgyIDk0VjEwMEg5NEM5Ny4zMTM3IDEwMCAxMDAgOTcuMzEzNyAxMDAgOTRDMTAwIDUzLjEzMDkgNjYuODY5MSAyMCAyNiAyMEMyMi42ODYzIDIwIDIwIDIyLjY4NjMgMjAgMjZWMzhaIiBmaWxsPSJ1cmwoI3BhaW50MV9yYWRpYWxfNjJfMzI5KSIvPgo8cGF0aCBkPSJNODQgOTRIMTAwQzEwMCA5Ny4zMTM3IDk3LjMxMzcgMTAwIDk0IDEwMEg4NFY5NFoiIGZpbGw9InVybCgjcGFpbnQyX2xpbmVhcl82Ml8zMjkpIi8+CjxwYXRoIGQ9Ik0yNiAyMEwyNiAzNkgyMEwyMCAyNkMyMCAyMi42ODYzIDIyLjY4NjMgMjAgMjYgMjBaIiBmaWxsPSJ1cmwoI3BhaW50M19saW5lYXJfNjJfMzI5KSIvPgo8cGF0aCBkPSJNMjAgMzZIMjZDNTguMDMyNSAzNiA4NCA2MS45Njc1IDg0IDk0VjEwMEg2NlY5NEM2NiA3MS45MDg2IDQ4LjA5MTQgNTQgMjYgNTRIMjBWMzZaIiBmaWxsPSJ1cmwoI3BhaW50NF9yYWRpYWxfNjJfMzI5KSIvPgo8cGF0aCBkPSJNNjggOTRIODRWMTAwSDY4Vjk0WiIgZmlsbD0idXJsKCNwYWludDVfbGluZWFyXzYyXzMyOSkiLz4KPHBhdGggZD0iTTIwIDUyTDIwIDM2TDI2IDM2TDI2IDUySDIwWiIgZmlsbD0idXJsKCNwYWludDZfbGluZWFyXzYyXzMyOSkiLz4KPHBhdGggZD0iTTIwIDYyQzIwIDY1LjMxMzcgMjIuNjg2MyA2OCAyNiA2OEM0MC4zNTk0IDY4IDUyIDc5LjY0MDYgNTIgOTRDNTIgOTcuMzEzNyA1NC42ODYzIDEwMCA1OCAxMDBINjhWOTRDNjggNzAuODA0IDQ5LjE5NiA1MiAyNiA1MkgyMFY2MloiIGZpbGw9InVybCgjcGFpbnQ3X3JhZGlhbF82Ml8zMjkpIi8+CjxwYXRoIGQ9Ik01MiA5NEg2OFYxMDBINThDNTQuNjg2MyAxMDAgNTIgOTcuMzEzNyA1MiA5NFoiIGZpbGw9InVybCgjcGFpbnQ4X3JhZGlhbF82Ml8zMjkpIi8+CjxwYXRoIGQ9Ik0yNiA2OEMyMi42ODYzIDY4IDIwIDY1LjMxMzcgMjAgNjJMMjAgNTJMMjYgNTJMMjYgNjhaIiBmaWxsPSJ1cmwoI3BhaW50OV9yYWRpYWxfNjJfMzI5KSIvPgo8ZGVmcz4KPGxpbmVhckdyYWRpZW50IGlkPSJwYWludDBfbGluZWFyXzYyXzMyOSIgeDE9IjYwIiB5MT0iMCIgeDI9IjYwIiB5Mj0iMTIwIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+CjxzdG9wIHN0b3AtY29sb3I9IiMxNzQyOTkiLz4KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjMDAxRTU5Ii8+CjwvbGluZWFyR3JhZGllbnQ+CjxyYWRpYWxHcmFkaWVudCBpZD0icGFpbnQxX3JhZGlhbF82Ml8zMjkiIGN4PSIwIiBjeT0iMCIgcj0iMSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIGdyYWRpZW50VHJhbnNmb3JtPSJ0cmFuc2xhdGUoMjYgOTQpIHJvdGF0ZSgtOTApIHNjYWxlKDc0KSI+CjxzdG9wIG9mZnNldD0iMC43NzAyNzciIHN0b3AtY29sb3I9IiNGRjQwMDAiLz4KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjODc1NEM5Ii8+CjwvcmFkaWFsR3JhZGllbnQ+CjxsaW5lYXJHcmFkaWVudCBpZD0icGFpbnQyX2xpbmVhcl82Ml8zMjkiIHgxPSI4MyIgeTE9Ijk3IiB4Mj0iMTAwIiB5Mj0iOTciIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj4KPHN0b3Agc3RvcC1jb2xvcj0iI0ZGNDAwMCIvPgo8c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiM4NzU0QzkiLz4KPC9saW5lYXJHcmFkaWVudD4KPGxpbmVhckdyYWRpZW50IGlkPSJwYWludDNfbGluZWFyXzYyXzMyOSIgeDE9IjIzIiB5MT0iMjAiIHgyPSIyMyIgeTI9IjM3IiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+CjxzdG9wIHN0b3AtY29sb3I9IiM4NzU0QzkiLz4KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjRkY0MDAwIi8+CjwvbGluZWFyR3JhZGllbnQ+CjxyYWRpYWxHcmFkaWVudCBpZD0icGFpbnQ0X3JhZGlhbF82Ml8zMjkiIGN4PSIwIiBjeT0iMCIgcj0iMSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIGdyYWRpZW50VHJhbnNmb3JtPSJ0cmFuc2xhdGUoMjYgOTQpIHJvdGF0ZSgtOTApIHNjYWxlKDU4KSI+CjxzdG9wIG9mZnNldD0iMC43MjM5MjkiIHN0b3AtY29sb3I9IiNGRkY3MDAiLz4KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjRkY5OTAxIi8+CjwvcmFkaWFsR3JhZGllbnQ+CjxsaW5lYXJHcmFkaWVudCBpZD0icGFpbnQ1X2xpbmVhcl82Ml8zMjkiIHgxPSI2OCIgeTE9Ijk3IiB4Mj0iODQiIHkyPSI5NyIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgo8c3RvcCBzdG9wLWNvbG9yPSIjRkZGNzAwIi8+CjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI0ZGOTkwMSIvPgo8L2xpbmVhckdyYWRpZW50Pgo8bGluZWFyR3JhZGllbnQgaWQ9InBhaW50Nl9saW5lYXJfNjJfMzI5IiB4MT0iMjMiIHkxPSI1MiIgeDI9IjIzIiB5Mj0iMzYiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj4KPHN0b3Agc3RvcC1jb2xvcj0iI0ZGRjcwMCIvPgo8c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNGRjk5MDEiLz4KPC9saW5lYXJHcmFkaWVudD4KPHJhZGlhbEdyYWRpZW50IGlkPSJwYWludDdfcmFkaWFsXzYyXzMyOSIgY3g9IjAiIGN5PSIwIiByPSIxIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgZ3JhZGllbnRUcmFuc2Zvcm09InRyYW5zbGF0ZSgyNiA5NCkgcm90YXRlKC05MCkgc2NhbGUoNDIpIj4KPHN0b3Agb2Zmc2V0PSIwLjU5NTEzIiBzdG9wLWNvbG9yPSIjMDBBQUZGIi8+CjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzAxREE0MCIvPgo8L3JhZGlhbEdyYWRpZW50Pgo8cmFkaWFsR3JhZGllbnQgaWQ9InBhaW50OF9yYWRpYWxfNjJfMzI5IiBjeD0iMCIgY3k9IjAiIHI9IjEiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIiBncmFkaWVudFRyYW5zZm9ybT0idHJhbnNsYXRlKDUxIDk3KSBzY2FsZSgxNyA0NS4zMzMzKSI+CjxzdG9wIHN0b3AtY29sb3I9IiMwMEFBRkYiLz4KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjMDFEQTQwIi8+CjwvcmFkaWFsR3JhZGllbnQ+CjxyYWRpYWxHcmFkaWVudCBpZD0icGFpbnQ5X3JhZGlhbF82Ml8zMjkiIGN4PSIwIiBjeT0iMCIgcj0iMSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIGdyYWRpZW50VHJhbnNmb3JtPSJ0cmFuc2xhdGUoMjMgNjkpIHJvdGF0ZSgtOTApIHNjYWxlKDE3IDMyMi4zNykiPgo8c3RvcCBzdG9wLWNvbG9yPSIjMDBBQUZGIi8+CjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzAxREE0MCIvPgo8L3JhZGlhbEdyYWRpZW50Pgo8L2RlZnM+Cjwvc3ZnPgo=" },
        { id: "coinbase", uuid: "55555555-40da-40da-8d1e-089b3f3edcc2", name: "Coinbase Wallet", rdns: "com.coinbase.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjgiIGhlaWdodD0iMjgiIHZpZXdCb3g9IjAgMCAyOCAyOCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjI4IiBoZWlnaHQ9IjI4IiBmaWxsPSIjMkM1RkY2Ii8+CjxwYXRoIGZpbGwtcnVsZT0iZXZlbm9kZCIgY2xpcC1ydWxlPSJldmVub2RkIiBkPSJNMTQgMjMuOEMxOS40MTI0IDIzLjggMjMuOCAxOS40MTI0IDIzLjggMTRDMjMuOCA4LjU4NzYxIDE5LjQxMjQgNC4yIDE0IDQuMkM4LjU4NzYxIDQuMiA0LjIgOC41ODc2MSA0LjIgMTRDNC4yIDE5LjQxMjQgOC41ODc2MSAyMy44IDE0IDIzLjhaTTExLjU1IDEwLjhDMTEuMTM1OCAxMC44IDEwLjggMTEuMTM1OCAxMC44IDExLjU1VjE2LjQ1QzEwLjggMTYuODY0MiAxMS4xMzU4IDE3LjIgMTEuNTUgMTcuMkgxNi40NUMxNi44NjQyIDE3LjIgMTcuMiAxNi44NjQyIDE3LjIgMTYuNDVWMTEuNTVDMTcuMiAxMS4xMzU4IDE2Ljg2NDIgMTAuOCAxNi40NSAxMC44SDExLjU1WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+Cg==" },
        { id: "okx", uuid: "66666666-40da-40da-8d1e-089b3f3edcc2", name: "OKX Wallet", rdns: "com.okex.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PHBhdGggZmlsbD0iIzAwMCIgZD0iTTAgMGgyOHYyOEgweiIvPjxwYXRoIGZpbGw9IiNmZmYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgZD0iTTEwLjgxOSA1LjU1Nkg1LjkzYS4zNzYuMzc2IDAgMCAwLS4zNzUuMzc1djQuODg4YzAgLjIwNy4xNjguMzc1LjM3NS4zNzVoNC44ODhhLjM3Ni4zNzYgMCAwIDAgLjM3NS0uMzc2VjUuOTMyYS4zNzYuMzc2IDAgMCAwLS4zNzYtLjM3NVptNS42NCA1LjYzOGgtNC44ODZhLjM3Ni4zNzYgMCAwIDAtLjM3Ni4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc2LjM3Nmg0Ljg4N2EuMzc2LjM3NiAwIDAgMCAuMzc2LS4zNzVWMTEuNTdhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc3Wm0uNzUtNS42MzhoNC44ODdjLjIwOCAwIC4zNzYuMTY4LjM3Ni4zNzV2NC44ODhhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzVIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjUuOTMzYzAtLjIwOC4xNjktLjM3Ni4zNzYtLjM3NlptLTYuMzkgMTEuMjc3SDUuOTNhLjM3Ni4zNzYgMCAwIDAtLjM3NS4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc1LjM3Nmg0Ljg4OGEuMzc2LjM3NiAwIDAgMCAuMzc1LS4zNzZWMTcuMjFhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc2Wm02LjM5IDBoNC44ODdjLjIwOCAwIC4zNzYuMTY5LjM3Ni4zNzZ2NC44ODdhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzZIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjE3LjIxYzAtLjIwNy4xNjktLjM3Ni4zNzYtLjM3NloiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==" },
        { id: "rabby", uuid: "77777777-40da-40da-8d1e-089b3f3edcc2", name: "Rabby Wallet", rdns: "io.rabby", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PGcgY2xpcC1wYXRoPSJ1cmwoI2EpIj48cGF0aCBmaWxsPSIjODY5N0ZGIiBkPSJNMjggMEgwdjI4aDI4VjBaIi8+PHBhdGggZmlsbD0idXJsKCNiKSIgZD0iTTIyLjU0IDE1LjA3OGMuNjc3LTEuNTE0LTIuNjczLTUuNzQ0LTUuODc0LTcuNTA2LTIuMDE3LTEuMzY1LTQuMTItMS4xNzgtNC41NDUtLjU3OS0uOTM1IDEuMzE2IDMuMDk0IDIuNDMgNS43ODggMy43MzEtLjU4LjI1Mi0xLjEyNS43MDMtMS40NDYgMS4yOC0xLjAwNC0xLjA5Ni0zLjIwOS0yLjA0LTUuNzk2LTEuMjgtMS43NDMuNTEzLTMuMTkxIDEuNzIxLTMuNzUxIDMuNTQ2YTEuMDk3IDEuMDk3IDAgMSAwLS40NDUgMi4xYy4xMTIgMCAuNDYzLS4wNzUuNDYzLS4wNzVsNS42MTIuMDQxYy0yLjI0NCAzLjU2LTQuMDE4IDQuMDgxLTQuMDE4IDQuNjk4czEuNjk3LjQ1IDIuMzM1LjIyYzMuMDUtMS4xIDYuMzI3LTQuNTMxIDYuODktNS41MTkgMi4zNi4yOTUgNC4zNDUuMzMgNC43ODYtLjY1N1oiLz48cGF0aCBmaWxsPSJ1cmwoI2MpIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Im0xNy44ODUgMTAuNzEzLjAyNS4wMWMuMTI1LS4wNDkuMTA1LS4yMzMuMDctLjM3OC0uMDc4LS4zMzMtMS40MzgtMS42NzYtMi43MTUtMi4yNzctMS43NDMtLjgyLTMuMDI1LS43NzctMy4yMTItLjM5OC4zNTYuNzI2IDEuOTk4IDEuNDA4IDMuNzE0IDIuMTIuNzIzLjMgMS40Ni42MDYgMi4xMTguOTIzWiIgY2xpcC1ydWxlPSJldmVub2RkIi8+PHBhdGggZmlsbD0idXJsKCNkKSIgZmlsbC1ydWxlPSJldmVub2RkIiBkPSJNMTUuNzAxIDE4LjAzNmExMC4yOTYgMTAuMjk2IDAgMCAwLTEuMi0uMzdjLjQ4Mi0uODYyLjU4My0yLjEzOC4xMjgtMi45NDUtLjYzOS0xLjEzMy0xLjQ0LTEuNzM2LTMuMzA0LTEuNzM2LTEuMDI0IDAtMy43ODMuMzQ2LTMuODMyIDIuNjQ4LS4wMDUuMjQyIDAgLjQ2NC4wMTcuNjY3bDUuMDM2LjAzN2ExNy4yNjQgMTcuMjY0IDAgMCAxLTEuODcxIDIuNDgzYy42NjkuMTcyIDEuMjIxLjMxNiAxLjcyOC40NDguNDguMTI1LjkyLjI0IDEuMzguMzU3YTIxLjAwMyAyMS4wMDMgMCAwIDAgMS45MTgtMS41OVoiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjxwYXRoIGZpbGw9InVybCgjZSkiIGQ9Ik02Ljg0OCAxNi4wNjNjLjIwNiAxLjc1IDEuMiAyLjQzNSAzLjIzMiAyLjYzOCAyLjAzMi4yMDMgMy4xOTcuMDY3IDQuNzQ5LjIwOCAxLjI5Ni4xMTggMi40NTMuNzc4IDIuODgyLjU1LjM4Ni0uMjA1LjE3LS45NDctLjM0Ny0xLjQyMy0uNjctLjYxNy0xLjU5Ny0xLjA0Ni0zLjIyOS0xLjE5OS4zMjUtLjg5LjIzNC0yLjEzOC0uMjctMi44MTctLjczMS0uOTgyLTIuMDc5LTEuNDI2LTMuNzg1LTEuMjMyLTEuNzgyLjIwMi0zLjQ5IDEuMDgtMy4yMzIgMy4yNzVaIi8+PC9nPjxkZWZzPjxsaW5lYXJHcmFkaWVudCBpZD0iYiIgeDE9IjEwLjQ2NCIgeDI9IjIyLjM5NCIgeTE9IjEzLjczNyIgeTI9IjE3LjEyIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHN0b3Agc3RvcC1jb2xvcj0iI2ZmZiIvPjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI2ZmZiIvPjwvbGluZWFyR3JhZGllbnQ+PGxpbmVhckdyYWRpZW50IGlkPSJjIiB4MT0iMjAuMzg2IiB4Mj0iMTEuNzc5IiB5MT0iMTMuNTA5IiB5Mj0iNC44NzkiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjNzI1OERDIi8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjNzk3REVBIiBzdG9wLW9wYWNpdHk9IjAiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0iZCIgeDE9IjE1Ljk0IiB4Mj0iNy42NzMiIHkxPSIxOC4zMzciIHkyPSIxMy41ODQiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjNzQ2MUVBIi8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjQkZDMkZGIiBzdG9wLW9wYWNpdHk9IjAiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0iZSIgeDE9IjExLjE3NyIgeDI9IjE2Ljc2NSIgeTE9IjEzLjY0OCIgeTI9IjIwLjc0OSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiLz48c3RvcCBvZmZzZXQ9Ii45ODQiIHN0b3AtY29sb3I9IiNENUNFRkYiLz48L2xpbmVhckdyYWRpZW50PjxjbGlwUGF0aCBpZD0iYSI+PHBhdGggZmlsbD0iI2ZmZiIgZD0iTTAgMGgyOHYyOEgweiIvPjwvY2xpcFBhdGg+PC9kZWZzPjwvc3ZnPg==" },
        { id: "bitget", uuid: "88888888-40da-40da-8d1e-089b3f3edcc2", name: "Bitget Wallet", rdns: "com.bitget.web3", icon: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgdmlld0JveD0iMCAwIDUxMiA1MTIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI1MTIiIGhlaWdodD0iNTEyIiBmaWxsPSIjMDAxRjI5Ii8+CjxwYXRoIGQ9Ik0yMTkuOTQ4IDk1LjcwMjJDMjAxLjYyMyA5NS42OTI5IDE4My4zMyA5NS42ODM1IDE2NC45NDEgOTUuNzExNkMxNTMuODIyIDk1LjcxMTYgMTQ5LjY1MSAxMDkuNjcxIDE1Ny45MjEgMTE3LjkzOUwyODMuMDk4IDI0My4xMTdDMjg3LjAwNCAyNDYuNjkgMjg5LjQ0MSAyNTAuNTc0IDI4OS41MyAyNTUuNjkzQzI4OS40NDEgMjYwLjgxMiAyODcuMDA0IDI2NC42OTYgMjgzLjA5OCAyNjguMjY5TDE1Ny45MjEgMzkzLjQ0NkMxNDkuNjUxIDQwMS43MTUgMTUzLjgyMiA0MTUuNjc0IDE2NC45NDEgNDE1LjY3NEMxODMuMzMgNDE1LjcwMiAyMDEuNjIzIDQxNS42OTMgMjE5Ljk0OCA0MTUuNjgzQzIyOS4xMjIgNDE1LjY3OSAyMzguMzA1IDQxNS42NzQgMjQ3LjUxMSA0MTUuNjc0QzI1OS41NTUgNDE1LjY3NCAyNjYuNzIgNDA5LjI0IDI3My4xNTQgNDAyLjgwNUwzODYuMDQ3IDI4OS45MTJDMzk1LjA1NyAyODAuOTAyIDQwMy4xMTkgMjY4LjkzOSA0MDMuMDA5IDI1NS42OTNDNDAzLjExOSAyNDIuNDQ3IDM5NS4wNTcgMjMwLjQ4NCAzODYuMDQ3IDIyMS40NzRMMjczLjE1NCAxMDguNThDMjY2LjcyIDEwMi4xNDYgMjU5LjU1NSA5NS43MTE2IDI0Ny41MTEgOTUuNzExNkMyMzguMzA1IDk1LjcxMTYgMjI5LjEyMiA5NS43MDY5IDIxOS45NDggOTUuNzAyMloiIGZpbGw9IiMwMEYwRkYiLz4KPC9zdmc+Cg==" },
        { id: "binance", uuid: "99999999-40da-40da-8d1e-089b3f3edcc2", name: "Binance Web3 Wallet", rdns: "com.binance.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMzAiIGhlaWdodD0iMTMwIiBmaWxsPSJub25lIj48cGF0aCBmaWxsPSIjMDAwIiBkPSJNMCAwaDEzMHYxMzBIMHoiLz48cGF0aCBmaWxsPSIjRjNCQTJGIiBkPSJNNDUuNTg3IDU3LjAyIDY1LjAxIDM3LjYwNmwxOS40MyAxOS40MyAxMS4yOTUtMTEuMzAzTDY1LjAxIDE1IDM0LjI4NCA0NS43MjV6TTE1IDY1LjAwNGwxMS4yOTktMTEuMjk5IDExLjI5OCAxMS4yOTlMMjYuMyA3Ni4zMDJ6TTQ1LjU4NyA3Mi45ODMgNjUuMDEgOTIuNDA2bDE5LjQzLTE5LjQzIDExLjMwMyAxMS4yODctLjAwOC4wMDctMzAuNzI1IDMwLjczNC0zMC43MjUtMzAuNzE4LS4wMTYtLjAxNnpNOTIuNDAzIDY1LjAwNiAxMDMuNyA1My43MDggMTE1IDY1LjAwNmwtMTEuMjk5IDExLjI5OXoiLz48cGF0aCBmaWxsPSIjRjNCQTJGIiBkPSJtNzYuNDcxIDY0Ljk5OC0xMS40Ni0xMS40NjktOC40NzYgOC40NzUtLjk4Ljk3Mi0yLjAwNSAyLjAwNi0uMDE2LjAxNi4wMTYuMDI0IDExLjQ2IDExLjQ1MyAxMS40NjEtMTEuNDcuMDA4LS4wMDd6Ii8+PC9zdmc+" },
        { id: "tokenpocket", uuid: "aaaaaaaa-40da-40da-8d1e-089b3f3edcc2", name: "TokenPocket", rdns: "vip.mytokenpocket", icon: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjhweCIgaGVpZ2h0PSIyOHB4IiB2aWV3Qm94PSIwIDAgMjggMjgiIHZlcnNpb249IjEuMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayI+PHRpdGxlPnRva2VucG9ja2V0PC90aXRsZT48ZGVmcz48bGluZWFyR3JhZGllbnQgeDE9IjEwNy41MTE0MjUlIiB5MT0iNTAuMDE0NzQyNyUiIHgyPSIwLjA0NTk1NzA1NTclIiB5Mj0iNTAuMDE0NzQyNyUiIGlkPSJsaW5lYXJHcmFkaWVudC0xIj48c3RvcCBzdG9wLWNvbG9yPSIjRkZGRkZGIiBvZmZzZXQ9IjAlIj48L3N0b3A+PHN0b3Agc3RvcC1jb2xvcj0iI0ZGRkZGRiIgc3RvcC1vcGFjaXR5PSIwLjMyMzMiIG9mZnNldD0iOTYuNjclIj48L3N0b3A+PHN0b3Agc3RvcC1jb2xvcj0iI0ZGRkZGRiIgc3RvcC1vcGFjaXR5PSIwLjMiIG9mZnNldD0iMTAwJSI+PC9zdG9wPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPjxnIGlkPSJwMSIgc3Ryb2tlPSJub25lIiBzdHJva2Utd2lkdGg9IjEiIGZpbGw9Im5vbmUiIGZpbGwtcnVsZT0iZXZlbm9kZCI+PGcgaWQ9InRva2VucG9ja2V0IiBmaWxsLXJ1bGU9Im5vbnplcm8iPjxwb2x5Z29uIGlkPSJwYXRoIiBmaWxsPSIjMjk4MEZFIiBwb2ludHM9IjI3Ljk4NzQyNzUgMCAwIDAgMCAyOCAyNy45ODc0Mjc1IDI4Ij48L3BvbHlnb24+PGcgaWQ9Imdyb3VwIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSg1LjEwNzU3NywgNy41NzQyMTkpIj48cGF0aCBkPSJNNi4yODY3ODIwOSw0LjQ1MTg2NzE5IEw2LjI5OTg4MjA5LDQuNDUxODY3MTkgQzYuMjg2NzgyMDksNC40MjAyODgyNCA2LjI4Njc4MjA5LDQuMzgzNDQ2MTMgNi4yODY3ODIwOSw0LjM1MTg2NzE5IEw2LjI4Njc4MjA5LDQuNDUxODY3MTkgWiIgaWQ9InBhdGgiIGZpbGw9IiMyOUFFRkYiPjwvcGF0aD48cGF0aCBkPSJNMTMuMDg1OTI3LDUuMTAwNTExNzIgTDkuMzA0OTMxNzEsNS4xMDA1MTE3MiBMOS4zMDQ5MzE3MSwxMi4yMjQ3MzQ0IEM5LjMwNDkzMTcxLDEyLjU2MTQxOCA5LjU2NTY4MDA3LDEyLjgzMzY1MjMgOS44ODgxOTA4MywxMi44MzM2NTIzIEwxMi41MDI2NDE3LDEyLjgzMzY1MjMgQzEyLjgyNTE3ODcsMTIuODMzNjUyMyAxMy4wODU5MjcsMTIuNTYxNDE4IDEzLjA4NTkyNywxMi4yMjQ3MzQ0IEwxMy4wODU5MjcsNS4xMDA1MTE3MiBaIiBpZD0icGF0aCIgZmlsbD0iI0ZGRkZGRiI+PC9wYXRoPjxwYXRoIGQ9Ik03LjQ3OTY2Njk4LDAgTDcuMzUyNzEwOTQsMCBMMC41ODMyODUzMTMsMCBDMC4yNjA3NDgzNjMsMCAwLDAuMjcyMjA3MDMxIDAsMC42MDg5MTc5NjkgTDAsMy4wODAzNTU0NyBDMCwzLjQxNzA2NjQxIDAuMjYwNzQ4MzYzLDMuNjg5MjczNDQgMC41ODMyODUzMTMsMy42ODkyNzM0NCBMMi4xNzE4NDY1OSwzLjY4OTI3MzQ0IEwyLjgwMzE2OTMyLDMuNjg5MjczNDQgTDIuODAzMTY5MzIsNC40MTk5NTMxMyBMMi44MDMxNjkzMiwxMi4yNDI2NDQ1IEMyLjgwMzE2OTMyLDEyLjU3OTM1NTUgMy4wNjM5MTc2OCwxMi44NTE1NjI1IDMuMzg2NDI4NDQsMTIuODUxNTYyNSBMNS44NzA1MTgyNCwxMi44NTE1NjI1IEM2LjE5MzAyOSwxMi44NTE1NjI1IDYuNDUzNzc3MzYsMTIuNTc5MzU1NSA2LjQ1Mzc3NzM2LDEyLjI0MjY0NDUgTDYuNDUzNzc3MzYsNC40MTk5NTMxMyBMNi40NTM3NzczNiw0LjM1MTkyMTg4IEw2LjQ1Mzc3NzM2LDMuNjg5MjczNDQgTDcuMDg1MTAwMDksMy42ODkyNzM0NCBMNy4zNDI0MTcyMSwzLjY4OTI3MzQ0IEw3LjQ2OTM3MzI1LDMuNjg5MjczNDQgQzguNDQzNzk0MiwzLjY4OTI3MzQ0IDkuMjM2MzU5MjEsMi44NjE4Nzg5MSA5LjIzNjM1OTIxLDEuODQ0NjM2NzIgQzkuMjQ2NjUyOTUsMC44MjczOTQ1MzEgOC40NTQwODc5MywwIDcuNDc5NjY2OTgsMCBaIiBpZD0icGF0aCIgZmlsbD0iI0ZGRkZGRiI+PC9wYXRoPjxwYXRoIGQ9Ik0xMy4wODk0MTA3LDUuMTAwNTExNzIgTDEzLjA4OTQxMDcsMTAuMDcyMDcwMyBDMTMuMjE5Nzk3OSwxMC4xMDQzMDg2IDEzLjM1MzU5MDMsMTAuMTI5MzgyOCAxMy40OTA4NCwxMC4xNTA4NzUgQzEzLjY4Mjk4OTcsMTAuMTc5NTMxMyAxMy44ODE5NzU3LDEwLjE5NzQ0MTQgMTQuMDgwOTg3OCwxMC4yMDEwMjM0IEMxNC4wOTEyODE2LDEwLjIwMTAyMzQgMTQuMTAxNTc1MywxMC4yMDEwMjM0IDE0LjExNTMwMDMsMTAuMjAxMDIzNCBMMTQuMTE1MzAwMyw2LjI0NjY3OTY5IEMxMy41NDIzMDg3LDYuMjA3Mjc3MzQgMTMuMDg5NDEwNyw1LjcwOTQwMjM0IDEzLjA4OTQxMDcsNS4xMDA1MTE3MiBaIiBpZD0icGF0aCIgZmlsbD0idXJsKCNsaW5lYXJHcmFkaWVudC0xKSI+PC9wYXRoPjxwYXRoIGQ9Ik0xNC4xOTA3MDkxLDAgQzExLjQ5MzkzNDUsMCA5LjMwNDkzMTcxLDIuMjg1MTk5MjIgOS4zMDQ5MzE3MSw1LjEwMDUxMTcyIEM5LjMwNDkzMTcxLDcuNTIxODI4MTIgMTAuOTIwOTQyOSw5LjU0OTEyMTA5IDEzLjA4OTM1ODMsMTAuMDcyMDcwMyBMMTMuMDg5MzU4Myw1LjEwMDUxMTcyIEMxMy4wODkzNTgzLDQuNDY2NTE5NTMgMTMuNTgzNDMxMiwzLjk1MDczNDM4IDE0LjE5MDcwOTEsMy45NTA3MzQzOCBDMTQuNzk4MDEzMSwzLjk1MDczNDM4IDE1LjI5MjA4NjEsNC40NjY1MTk1MyAxNS4yOTIwODYxLDUuMTAwNTExNzIgQzE1LjI5MjA4NjEsNS42MzQyMDcwMyAxNC45NDU1NTY2LDYuMDgxOTMzNTkgMTQuNDcyMDcxMSw2LjIxMDg1OTM4IEMxNC4zODI4NTg3LDYuMjM1OTMzNTkgMTQuMjg2NzgzOSw2LjI1MDI2MTcyIDE0LjE5MDcwOTEsNi4yNTAyNjE3MiBMMTQuMTkwNzA5MSwxMC4yMDEwMjM0IEMxNC4yODY3ODM5LDEwLjIwMTAyMzQgMTQuMzc5NDI3NSwxMC4xOTc0NDE0IDE0LjQ3MjA3MTEsMTAuMTkzODU5NCBDMTcuMDM4NDg0NiwxMC4wMzk4MzIgMTkuMDc2NTE2Nyw3LjgxOTEwOTM4IDE5LjA3NjUxNjcsNS4xMDA1MTE3MiBDMTkuMDc5OTQzOSwyLjI4NTE5OTIyIDE2Ljg5MDk0MTEsMCAxNC4xOTA3MDkxLDAgWiIgaWQ9InBhdGgiIGZpbGw9IiNGRkZGRkYiPjwvcGF0aD48cGF0aCBkPSJNMTQuMjExNzkwNSwxMC4yMDEwMjM0IEwxNC4yMTE3OTA1LDYuMjUwMjYxNzIgQzE0LjE3NzAyOTUsNi4yNTAyNjE3MiAxNC4xNDY1ODQ2LDYuMjUwMjYxNzIgMTQuMTExNzkwNSw2LjI0NjY3OTY5IEwxNC4xMTE3OTA1LDEwLjIwMTAyMzQgQzE0LjE0NjU4NDYsMTAuMjAxMDIzNCAxNC4xODEzNzg4LDEwLjIwMTAyMzQgMTQuMjExNzkwNSwxMC4yMDEwMjM0IFoiIGlkPSJwYXRoIiBmaWxsPSIjRkZGRkZGIj48L3BhdGg+PC9nPjwvZz48L2c+PC9zdmc+" },
        { id: "phantom", uuid: "bbbbbbbb-40da-40da-8d1e-089b3f3edcc2", name: "Phantom", rdns: "app.phantom", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyOCIgaGVpZ2h0PSIyOCIgZmlsbD0ibm9uZSI+PGcgY2xpcC1wYXRoPSJ1cmwoI2EpIj48cGF0aCBmaWxsPSIjQUI5RkYyIiBkPSJNMjggMEgwdjI4aDI4VjBaIi8+PHBhdGggZmlsbD0iI0ZGRkRGOCIgZmlsbC1ydWxlPSJldmVub2RkIiBkPSJNMTIuMDYzIDE4LjEyOGMtMS4xNzMgMS43OTYtMy4xMzcgNC4wNy01Ljc1IDQuMDctMS4yMzYgMC0yLjQyNC0uNTEtMi40MjQtMi43MTkgMC01LjYyNyA3LjY4Mi0xNC4zMzcgMTQuODEtMTQuMzM3IDQuMDU2IDAgNS42NzEgMi44MTMgNS42NzEgNi4wMDggMCA0LjEwMS0yLjY2IDguNzktNS4zMDYgOC43OS0uODQgMC0xLjI1Mi0uNDYtMS4yNTItMS4xOTIgMC0uMTkuMDMyLS4zOTcuMDk1LS42Mi0uOTAyIDEuNTQyLTIuNjQ1IDIuOTczLTQuMjc2IDIuOTczLTEuMTg4IDAtMS43OS0uNzQ3LTEuNzktMS43OTcgMC0uMzgxLjA3OS0uNzc4LjIyMi0xLjE3NlptOS42My03LjA4OWMwIC45MzEtLjU0OSAxLjM5Ny0xLjE2MyAxLjM5Ny0uNjI0IDAtMS4xNjQtLjQ2Ni0xLjE2NC0xLjM5NyAwLS45My41NC0xLjM5NiAxLjE2NC0xLjM5Ni42MTQgMCAxLjE2NC40NjUgMS4xNjQgMS4zOTZabS0zLjQ5IDBjMCAuOTMxLS41NSAxLjM5Ny0xLjE2NCAxLjM5Ny0uNjI0IDAtMS4xNjQtLjQ2Ni0xLjE2NC0xLjM5NyAwLS45My41NC0xLjM5NiAxLjE2NC0xLjM5Ni42MTQgMCAxLjE2NC40NjUgMS4xNjQgMS4zOTZaIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiLz48L2c+PGRlZnM+PGNsaXBQYXRoIGlkPSJhIj48cGF0aCBmaWxsPSIjZmZmIiBkPSJNMCAwaDI4djI4SDB6Ii8+PC9jbGlwUGF0aD48L2RlZnM+PC9zdmc+" }
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
// GHOST SOLANA WEB3 INJECTION (THE ELITE 6 KTP & MULTI-IDENTITY SPOOFER)
// ============================================================================
(function() {
    if (window.__GHOST_SOLANA_INITIALIZED__) return;
    window.__GHOST_SOLANA_INITIALIZED__ = true;

    var reportDappChainToBridge = window.reportDappChainToBridge || function(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    };

    var GHOST_URL = "http://127.0.0.1:3333";

    async function callGhostRpc(method, params) {
        params = params || [];
        if (window.GhostBridge && window.GhostBridge.postRpc) {
            var raw = window.GhostBridge.postRpc(JSON.stringify({ jsonrpc: "2.0", id: Date.now(), method: method, params: params }));
            var parsed = JSON.parse(raw);
            if (parsed.error) throw new Error(typeof parsed.error === "string" ? parsed.error : (parsed.error.message || JSON.stringify(parsed.error)));
            return parsed.result;
        }
        if (window.__ghostRequireDaemon) {
            window.__ghostRequireDaemon();
        }
        try {
            var res = await fetch(GHOST_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ jsonrpc: "2.0", id: Date.now(), method: method, params: params })
            });
            var data = await res.json();
            if (data.error) throw new Error(typeof data.error === "string" ? data.error : (data.error.message || JSON.stringify(data.error)));
            return data.result;
        } catch (err) {
            console.error("[GhostRpc] Error:", err);
            throw err;
        }
    }
    window.callGhostRpc = callGhostRpc;

    var B58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    function decodeBase58(str) {
        if (!str || typeof str !== "string") return new Uint8Array(32);
        var bytes = [0];
        for (var i = 0; i < str.length; i++) {
            var c = str[i];
            var val = B58_CHARS.indexOf(c);
            if (val === -1) continue;
            for (var j = 0; j < bytes.length; j++) {
                val += bytes[j] * 58;
                bytes[j] = val & 0xff;
                val >>= 8;
            }
            while (val > 0) {
                bytes.push(val & 0xff);
                val >>= 8;
            }
        }
        for (var i = 0; i < str.length && str[i] === '1'; i++) {
            bytes.push(0);
        }
        var arr = bytes.reverse();
        var res = new Uint8Array(Math.max(32, arr.length));
        res.set(arr, res.length - arr.length);
        return res.subarray(res.length - 32);
    }

    function createGhostPublicKey(val) {
        var str = (typeof val === "string") ? val : (val && val.toBase58 ? val.toBase58() : "");
        var bytes = decodeBase58(str);
        var pk = new Uint8Array(bytes);
        pk._val = str;
        pk.toBase58 = function() { return this._val; };
        pk.toString = function() { return this._val; };
        pk.toJSON = function() { return this._val; };
        pk.toBytes = function() { return new Uint8Array(this); };
        pk.toBuffer = function() { return this; };
        pk.equals = function(other) { return this._val === (other ? other.toString() : ""); };
        return pk;
    }

    function GhostPublicKey(val) {
        return createGhostPublicKey(val);
    }
    GhostPublicKey.prototype = Object.create(Uint8Array.prototype);
    GhostPublicKey.prototype.toBase58 = function() { return this._val; };
    GhostPublicKey.prototype.toString = function() { return this._val; };
    GhostPublicKey.prototype.toJSON = function() { return this._val; };
    GhostPublicKey.prototype.equals = function(other) { return this._val === (other ? other.toString() : ""); };
    GhostPublicKey.prototype.toBytes = function() { return new Uint8Array(this); };
    GhostPublicKey.prototype.toBuffer = function() { return this; };

    function GhostSolanaProvider() {
        this.isGhost = true;
        this.isPhantom = true;
        this.isSolflare = true;
        this.isBackpack = true;
        this.isOkxWallet = true;
        this.isCoinbaseWallet = true;
        this.isTrustWallet = true;
        this.isConnected = false;
        this.publicKey = null;
        this._events = {};
    }

    GhostSolanaProvider.prototype.on = function(event, handler) {
        if (!this._events[event]) this._events[event] = [];
        this._events[event].push(handler);
        return this;
    };
    GhostSolanaProvider.prototype.addListener = GhostSolanaProvider.prototype.on;

    GhostSolanaProvider.prototype.off = function(event, handler) {
        if (this._events[event]) {
            this._events[event] = this._events[event].filter(function(h) { return h !== handler; });
        }
        return this;
    };
    GhostSolanaProvider.prototype.removeListener = GhostSolanaProvider.prototype.off;

    GhostSolanaProvider.prototype.removeAllListeners = function(event) {
        if (event) delete this._events[event];
        else this._events = {};
        return this;
    };

    GhostSolanaProvider.prototype.emit = function(event) {
        var args = Array.prototype.slice.call(arguments, 1);
        if (this._events[event]) {
            this._events[event].forEach(function(h) {
                try { h.apply(null, args); } catch(e) {}
            });
        }
    };

    GhostSolanaProvider.prototype.connect = async function(opts) {
        reportDappChainToBridge("solana", 501);
        var addr = window.__FUSION_SOL_WALLET_ADDRESS__ || (window.GhostBridge && window.GhostBridge.getSolWalletAddress ? window.GhostBridge.getSolWalletAddress() : "");
        if (!addr && window.callGhostRpc) {
            try {
                var res = await callGhostRpc("sol_connect", [opts || {}]);
                if (res && res.publicKey) addr = res.publicKey;
            } catch(e) {}
        }
        if (!addr) addr = "8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf";
        this.publicKey = createGhostPublicKey(addr);
        this.isConnected = true;
        this.emit("connect", this.publicKey);
        this.emit("accountChanged", this.publicKey);
        if (typeof solStandardWallet !== "undefined") {
            this.emit("change", { accounts: solStandardWallet.accounts });
        }
        return { publicKey: this.publicKey };
    };

    GhostSolanaProvider.prototype.disconnect = async function() {
        this.isConnected = false;
        this.publicKey = null;
        this.emit("disconnect");
        this.emit("accountChanged", null);
        this.emit("change", { accounts: [] });
    };

    GhostSolanaProvider.prototype.signIn = async function(input) {
        var connectRes = await this.connect();
        return {
            account: {
                address: connectRes.publicKey.toBase58(),
                publicKey: connectRes.publicKey.toBytes(),
                chains: ["solana:mainnet"]
            },
            signedMessage: new Uint8Array(64),
            signature: new Uint8Array(64)
        };
    };

    GhostSolanaProvider.prototype.request = async function(args) {
        if (!args) return null;
        var method = args.method;
        var params = args.params || {};
        if (method === "connect") {
            return await this.connect(params);
        }
        if (method === "disconnect") {
            return await this.disconnect();
        }
        if (method === "signIn") {
            return await this.signIn(params);
        }
        if (method === "signTransaction") {
            return await this.signTransaction(params.transaction);
        }
        if (method === "signAllTransactions") {
            return await this.signAllTransactions(params.transactions);
        }
        if (method === "signAndSendTransaction") {
            return await this.signAndSendTransaction(params.transaction, params.options);
        }
        if (method === "signMessage") {
            return await this.signMessage(params.message, params.display);
        }
        return await callGhostRpc("sol_" + method, [params]);
    };

    GhostSolanaProvider.prototype.signTransaction = async function(transaction) {
        var wire = transaction.serialize ? transaction.serialize({ requireAllSignatures: false }) : transaction;
        var base64Tx = btoa(String.fromCharCode.apply(null, new Uint8Array(wire)));
        var signedBase64 = await callGhostRpc("sol_signTransaction", [base64Tx]);
        var binary = atob(signedBase64);
        var bytes = new Uint8Array(binary.length);
        for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
        return transaction.constructor && transaction.constructor.from ? transaction.constructor.from(bytes) : bytes;
    };

    GhostSolanaProvider.prototype.signAllTransactions = async function(transactions) {
        var results = [];
        for (var i = 0; i < transactions.length; i++) {
            results.push(await this.signTransaction(transactions[i]));
        }
        return results;
    };

    GhostSolanaProvider.prototype.signAndSendTransaction = async function(transaction, options) {
        var wire = transaction.serialize ? transaction.serialize({ requireAllSignatures: false }) : transaction;
        var base64Tx = btoa(String.fromCharCode.apply(null, new Uint8Array(wire)));
        var signature = await callGhostRpc("sol_signAndSendTransaction", [base64Tx, options || {}]);
        return { signature: signature };
    };

    GhostSolanaProvider.prototype.signMessage = async function(message, display) {
        var base64Msg = btoa(String.fromCharCode.apply(null, new Uint8Array(message)));
        var res = await callGhostRpc("sol_signMessage", [base64Msg, display || "hex"]);
        var sigBytes = atob(res.signature).split("").map(function(c) { return c.charCodeAt(0); });
        return { signature: new Uint8Array(sigBytes) };
    };

    var solProvider = new GhostSolanaProvider();

    window.solana = solProvider;
    window.phantom = window.phantom || {};
    window.phantom.solana = solProvider;
    window.phantom.ethereum = (typeof fakeProvider !== "undefined") ? fakeProvider : null;
    window.isPhantomInstalled = true;
    window.solflare = solProvider;
    window.backpack = solProvider;
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.solana = solProvider;
    window.coinbaseSolana = solProvider;
    window.trustwallet = window.trustwallet || {};
    window.trustwallet.solana = solProvider;

    // Solana Wallet Standard Register & Event Handling
    function createStandardSolWallet(name, icon) {
        var _connected = false;
        var _listeners = [];

        function getCurrentAccount() {
            reportDappChainToBridge("solana", 501);
            var addr = solProvider.publicKey ? solProvider.publicKey.toBase58() : "";
            if (!addr) {
                addr = window.__FUSION_SOL_WALLET_ADDRESS__ || (window.GhostBridge && window.GhostBridge.getSolWalletAddress ? window.GhostBridge.getSolWalletAddress() : "");
            }
            if (!addr) addr = "8B63qh1St7qVLVM3fNXCaK1jET6Pbmw96AuTMC2BYXaf";
            var pubBytes = decodeBase58(addr);
            if (!pubBytes || pubBytes.length !== 32) {
                var tmp = new Uint8Array(32);
                if (pubBytes) tmp.set(pubBytes.slice(0, 32));
                pubBytes = tmp;
            }
            return {
                address: addr,
                publicKey: pubBytes,
                chains: ["solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp", "solana:mainnet", "solana:mainnet-beta", "solana:devnet", "solana:testnet", "solana:localnet"],
                features: ["solana:signAndSendTransaction", "solana:signTransaction", "solana:signMessage", "solana:signIn"]
            };
        }

        var walletObj = {
            version: "1.0.0",
            name: name,
            icon: icon,
            url: "https://phantom.app",
            chains: ["solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp", "solana:mainnet", "solana:mainnet-beta", "solana:devnet", "solana:testnet", "solana:localnet"],
            get accounts() {
                if (!_connected && !solProvider.isConnected) return [];
                return [getCurrentAccount()];
            }
        };
        walletObj.features = {
            "standard:connect": {
                version: "1.0.0",
                connect: async function(input) {
                    try { await solProvider.connect(input); } catch(_) {}
                    _connected = true;
                    var acc = getCurrentAccount();
                    var accs = [acc];
                    try { solProvider.emit("change", { accounts: accs }); } catch(_) {}
                    for (var i = 0; i < _listeners.length; i++) {
                        try { _listeners[i]({ accounts: accs }); } catch(_) {}
                    }
                    return { accounts: accs };
                }
            },
            "standard:events": {
                version: "1.0.0",
                on: function(event, listener) {
                    if (event === "change" && typeof listener === "function") {
                        _listeners.push(listener);
                    }
                    solProvider.on(event, listener);
                    return function() {
                        var idx = _listeners.indexOf(listener);
                        if (idx !== -1) _listeners.splice(idx, 1);
                        solProvider.off(event, listener);
                    };
                }
            },
            "standard:disconnect": {
                version: "1.0.0",
                disconnect: async function() {
                    try { await solProvider.disconnect(); } catch(_) {}
                    _connected = false;
                    try { solProvider.emit("change", { accounts: [] }); } catch(_) {}
                    for (var i = 0; i < _listeners.length; i++) {
                        try { _listeners[i]({ accounts: [] }); } catch(_) {}
                    }
                    return {};
                }
            },
            "solana:signAndSendTransaction": {
                version: "1.0.0",
                supportedTransactionVersions: ['legacy', 0],
                signAndSendTransaction: async function(...inputs) {
                    var outputs = [];
                    for (var i = 0; i < inputs.length; i++) {
                        var item = inputs[i];
                        var tx = item.transaction || item;
                        var opts = item.options || {};
                        var sig = await solProvider.signAndSendTransaction(tx, opts);
                        var sigBytes = (typeof sig === "string") ? decodeBase58(sig) : (sig && sig.signature ? (typeof sig.signature === "string" ? decodeBase58(sig.signature) : sig.signature) : new Uint8Array(64));
                        outputs.push({ signature: sigBytes });
                    }
                    return outputs;
                }
            },
            "solana:signTransaction": {
                version: "1.0.0",
                supportedTransactionVersions: ['legacy', 0],
                signTransaction: async function(...inputs) {
                    var outputs = [];
                    for (var i = 0; i < inputs.length; i++) {
                        var item = inputs[i];
                        var tx = item.transaction || item;
                        var signed = await solProvider.signTransaction(tx);
                        outputs.push({ signedTransaction: signed });
                    }
                    return outputs;
                }
            },
            "solana:signMessage": {
                version: "1.0.0",
                signMessage: async function(...inputs) {
                    var outputs = [];
                    for (var i = 0; i < inputs.length; i++) {
                        var item = inputs[i];
                        var msg = item.message || item;
                        var res = await solProvider.signMessage(msg);
                        var sigBytes = (res && res.signature) ? (typeof res.signature === "string" ? decodeBase58(res.signature) : res.signature) : new Uint8Array(64);
                        outputs.push({ signedMessage: msg, signature: sigBytes });
                    }
                    return outputs;
                }
            },
            "solana:signIn": {
                version: "1.0.0",
                signIn: async function(input) { return await solProvider.signIn(input); }
            }
        };
        return walletObj;
    }

    var ghostWalletSvg = window.__GHOST_WALLET_SVG__ || "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjggMTI4Ij48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjMGIxMDFkIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjMDQwNzExIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImJvZHkiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiMwMEYwRkYiLz48c3RvcCBvZmZzZXQ9IjUwJSIgc3RvcC1jb2xvcj0iIzAwQTNGRiIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzcwMDBGRiIvPjwvbGluZWFyR3JhZGllbnQ+PGZpbHRlciBpZD0iZ2xvdyIgeD0iLTIwJSIgeT0iLTIwJSIgd2lkdGg9IjE0MCUiIGhlaWdodD0iMTQwJSI+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMyIgcmVzdWx0PSJibHVyIi8+PGZlQ29tcG9zaXRlIGluPSJTb3VyY2VHcmFwaGljIiBpbjI9ImJsdXIiIG9wZXJhdG9yPSJvdmVyIi8+PC9maWx0ZXI+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiByeD0iMjgiIGZpbGw9InVybCgjYmcpIi8+PHJlY3Qgd2lkdGg9IjEyNiIgaGVpZ2h0PSIxMjYiIHg9IjEiIHk9IjEiIHJ4PSIyNyIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBGMEZGIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1vcGFjaXR5PSIwLjM1Ii8+PGcgZmlsdGVyPSJ1cmwoI2dsb3cpIj48cGF0aCBkPSJNNjQgMjQgQzQ0IDI0IDMwIDM5IDMwIDYwIEMzMCA3NiAzMCA5MiAzMCA5NiBDMzAgOTkgMzQgMTAxIDM3IDk4IEM0MSA5NCA0NiA5NCA1MCA5OCBDNTQgMTAyIDYwIDEwMiA2NCA5OCBDNjggOTQgNzQgOTQgNzggOTggQzgyIDEwMiA4OCAxMDIgOTEgOTggQzk0IDk0IDk4IDk3IDk4IDk2IEM5OCA5MiA5OCA3NiA5OCA2MCBDOTggMzkgODQgMjQgNjQgMjQgWiIgZmlsbD0idXJsKCNib2R5KSIvPjxlbGxpcHNlIGN4PSI1MCIgY3k9IjU2IiByeD0iNSIgcnk9IjgiIGZpbGw9IiMwNjA5MTMiLz48ZWxsaXBzZSBjeD0iNzgiIGN5PSI1NiIgcng9IjUiIHJ5PSI4IiBmaWxsPSIjMDYwOTEzIi8+PGVsbGlwc2UgY3g9IjUxLjUiIGN5PSI1NCIgcng9IjIiIHJ5PSIzLjUiIGZpbGw9IiNGRkZGRkYiLz48ZWxsaXBzZSBjeD0iNzkuNSIgY3k9IjU0IiByeD0iMiIgcnk9IjMuNSIgZmlsbD0iI0ZGRkZGRiIvPjwvZz48L3N2Zz4=";
    var solStandardWallet = createStandardSolWallet("Phantom", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDgiIGhlaWdodD0iMTA4IiB2aWV3Qm94PSIwIDAgMTA4IDEwOCIgZmlsbD0ibm9uZSI+CjxyZWN0IHdpZHRoPSIxMDgiIGhlaWdodD0iMTA4IiByeD0iMjYiIGZpbGw9IiNBQjlGRjIiLz4KPHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik00Ni41MjY3IDY5LjkyMjlDNDIuMDA1NCA3Ni44NTA5IDM0LjQyOTIgODUuNjE4MiAyNC4zNDggODUuNjE4MkMxOS41ODI0IDg1LjYxODIgMTUgODMuNjU2MyAxNSA3NS4xMzQyQzE1IDUzLjQzMDUgNDQuNjMyNiAxOS44MzI3IDcyLjEyNjggMTkuODMyN0M4Ny43NjggMTkuODMyNyA5NCAzMC42ODQ2IDk0IDQzLjAwNzlDOTQgNTguODI1OCA4My43MzU1IDc2LjkxMjIgNzMuNTMyMSA3Ni45MTIyQzcwLjI5MzkgNzYuOTEyMiA2OC43MDUzIDc1LjEzNDIgNjguNzA1MyA3Mi4zMTRDNjguNzA1MyA3MS41NzgzIDY4LjgyNzUgNzAuNzgxMiA2OS4wNzE5IDY5LjkyMjlDNjUuNTg5MyA3NS44Njk5IDU4Ljg2ODUgODEuMzg3OCA1Mi41NzU0IDgxLjM4NzhDNDcuOTkzIDgxLjM4NzggNDUuNjcxMyA3OC41MDYzIDQ1LjY3MTMgNzQuNDU5OEM0NS42NzEzIDcyLjk4ODQgNDUuOTc2OCA3MS40NTU2IDQ2LjUyNjcgNjkuOTIyOVpNODMuNjc2MSA0Mi41Nzk0QzgzLjY3NjEgNDYuMTcwNCA4MS41NTc1IDQ3Ljk2NTggNzkuMTg3NSA0Ny45NjU4Qzc2Ljc4MTYgNDcuOTY1OCA3NC42OTg5IDQ2LjE3MDQgNzQuNjk4OSA0Mi41Nzk0Qzc0LjY5ODkgMzguOTg4NSA3Ni43ODE2IDM3LjE5MzEgNzkuMTg3NSAzNy4xOTMxQzgxLjU1NzUgMzcuMTkzMSA4My42NzYxIDM4Ljk4ODUgODMuNjc2MSA0Mi41Nzk0Wk03MC4yMTAzIDQyLjU3OTVDNzAuMjEwMyA0Ni4xNzA0IDY4LjA5MTYgNDcuOTY1OCA2NS43MjE2IDQ3Ljk2NThDNjMuMzE1NyA0Ny45NjU4IDYxLjIzMyA0Ni4xNzA0IDYxLjIzMyA0Mi41Nzk1QzYxLjIzMyAzOC45ODg1IDYzLjMxNTcgMzcuMTkzMSA2NS43MjE2IDM3LjE5MzFDNjguMDkxNiAzNy4xOTMxIDcwLjIxMDMgMzguOTg4NSA3MC4yMTAzIDQyLjU3OTVaIiBmaWxsPSIjRkZGREY4Ii8+Cjwvc3ZnPg==");
    var solflareStandardWallet = createStandardSolWallet("Solflare", "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c3ZnIGlkPSJTIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MCA1MCI+PGRlZnM+PHN0eWxlPi5jbHMtMXtmaWxsOiMwMjA1MGE7c3Ryb2tlOiNmZmVmNDY7c3Ryb2tlLW1pdGVybGltaXQ6MTA7c3Ryb2tlLXdpZHRoOi41cHg7fS5jbHMtMntmaWxsOiNmZmVmNDY7fTwvc3R5bGU+PC9kZWZzPjxyZWN0IGNsYXNzPSJjbHMtMiIgeD0iMCIgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiByeD0iMTIiIHJ5PSIxMiIvPjxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTI0LjIzLDI2LjQybDIuNDYtMi4zOCw0LjU5LDEuNWMzLjAxLDEsNC41MSwyLjg0LDQuNTEsNS40MywwLDEuOTYtLjc1LDMuMjYtMi4yNSw0LjkzbC0uNDYuNS4xNy0xLjE3Yy42Ny00LjI2LS41OC02LjA5LTQuNzItNy40M2wtNC4zLTEuMzhoMFpNMTguMDUsMTEuODVsMTIuNTIsNC4xNy0yLjcxLDIuNTktNi41MS0yLjE3Yy0yLjI1LS43NS0zLjAxLTEuOTYtMy4zLTQuNTF2LS4wOGgwWk0xNy4zLDMzLjA2bDIuODQtMi43MSw1LjM0LDEuNzVjMi44LjkyLDMuNzYsMi4xMywzLjQ2LDUuMThsLTExLjY1LTQuMjJoMFpNMTMuNzEsMjAuOTVjMC0uNzkuNDItMS41NCwxLjEzLTIuMTcuNzUsMS4wOSwyLjA1LDIuMDUsNC4wOSwyLjcxbDQuNDIsMS40Ni0yLjQ2LDIuMzgtNC4zNC0xLjQyYy0yLS42Ny0yLjg0LTEuNjctMi44NC0yLjk2TTI2LjgyLDQyLjg3YzkuMTgtNi4wOSwxNC4xMS0xMC4yMywxNC4xMS0xNS4zMiwwLTMuMzgtMi01LjI2LTYuNDMtNi43MmwtMy4zNC0xLjEzLDkuMTQtOC43Ny0xLjg0LTEuOTYtMi43MSwyLjM4LTEyLjgxLTQuMjJjLTMuOTcsMS4yOS04Ljk3LDUuMDktOC45Nyw4Ljg5LDAsLjQyLjA0LjgzLjE3LDEuMjktMy4zLDEuODgtNC42MywzLjYzLTQuNjMsNS44LDAsMi4wNSwxLjA5LDQuMDksNC41NSw1LjIybDIuNzUuOTItOS41Miw5LjE0LDEuODQsMS45NiwyLjk2LTIuNzEsMTQuNzMsNS4yMmgwWiIvPjwvc3ZnPg==");
    var backpackStandardWallet = createStandardSolWallet("Backpack", "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNDAgMjQwIj4KICA8cmVjdCB3aWR0aD0iMjQwIiBoZWlnaHQ9IjI0MCIgc3R5bGU9ImZpbGw6IHJnYigyNTUsIDI1NSwgMjU1KTsiLz4KICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xMjguOTA1IDYyLjc1YzQuOTY5IDAgOS42MzEuNjY3IDEzLjk0NyAxLjkwMkMxMzguNjI2IDU0LjgwNCAxMjkuODUzIDUyIDEyMC4wODkgNTJjLTkuNzgzIDAtMTguNTcyIDIuODE1LTIyLjc4OCAxMi43MSA0LjI4NS0xLjI3OSA4LjkyNi0xLjk2IDEzLjg3OS0xLjk2aDE3LjcyNVptLTE4Ljg2MiA5Ljg4NkM4Ni40NDUgNzIuNjM2IDczIDkxLjIgNzMgMTE0LjF2MjMuNTI0YzAgMi4yOSAxLjkxMyA0LjEwMSA0LjI3MyA0LjEwMWg4NS40NTRjMi4zNiAwIDQuMjczLTEuODExIDQuMjczLTQuMTAxVjExNC4xYzAtMjIuOS0xNS42MzUtNDEuNDY0LTM5LjIzMi00MS40NjRoLTE3LjcyNVptOS45MjQgNDEuNjdjOC4yNTkgMCAxNC45NTQtNi42OTYgMTQuOTU0LTE0Ljk1NXMtNi42OTUtMTQuOTU0LTE0Ljk1NC0xNC45NTQtMTQuOTU1IDYuNjk1LTE0Ljk1NSAxNC45NTQgNi42OTYgMTQuOTU1IDE0Ljk1NSAxNC45NTVaTTczIDE1NS41NTVjMC0yLjI5IDEuOTEzLTQuMTQ3IDQuMjczLTQuMTQ3aDg1LjQ1NGMyLjM2IDAgNC4yNzMgMS44NTcgNC4yNzMgNC4xNDd2MjQuODc4YzAgNC41OC0zLjgyNiA4LjI5My04LjU0NSA4LjI5M2gtNzYuOTFjLTQuNzIgMC04LjU0NS0zLjcxMy04LjU0NS04LjI5M3YtMjQuODc4WiIgY2xpcC1ydWxlPSJldmVub2RkIiBzdHlsZT0iZmlsbDogcmdiKDIyNywgNjIsIDYzKTsiLz4KPC9zdmc+");
    var okxSolStandardWallet = createStandardSolWallet("OKX Wallet", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PHBhdGggZmlsbD0iIzAwMCIgZD0iTTAgMGgyOHYyOEgweiIvPjxwYXRoIGZpbGw9IiNmZmYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgZD0iTTEwLjgxOSA1LjU1Nkg1LjkzYS4zNzYuMzc2IDAgMCAwLS4zNzUuMzc1djQuODg4YzAgLjIwNy4xNjguMzc1LjM3NS4zNzVoNC44ODhhLjM3Ni4zNzYgMCAwIDAgLjM3NS0uMzc2VjUuOTMyYS4zNzYuMzc2IDAgMCAwLS4zNzYtLjM3NVptNS42NCA1LjYzOGgtNC44ODZhLjM3Ni4zNzYgMCAwIDAtLjM3Ni4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc2LjM3Nmg0Ljg4N2EuMzc2LjM3NiAwIDAgMCAuMzc2LS4zNzVWMTEuNTdhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc3Wm0uNzUtNS42MzhoNC44ODdjLjIwOCAwIC4zNzYuMTY4LjM3Ni4zNzV2NC44ODhhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzVIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjUuOTMzYzAtLjIwOC4xNjktLjM3Ni4zNzYtLjM3NlptLTYuMzkgMTEuMjc3SDUuOTNhLjM3Ni4zNzYgMCAwIDAtLjM3NS4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc1LjM3Nmg0Ljg4OGEuMzc2LjM3NiAwIDAgMCAuMzc1LS4zNzZWMTcuMjFhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc2Wm02LjM5IDBoNC44ODdjLjIwOCAwIC4zNzYuMTY5LjM3Ni4zNzZ2NC44ODdhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzZIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjE3LjIxYzAtLjIwNy4xNjktLjM3Ni4zNzYtLjM3NloiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==");

    var solWalletsList = [
        createStandardSolWallet("GhostWallet", ghostWalletSvg),
        solStandardWallet,
        solflareStandardWallet,
        backpackStandardWallet,
        okxSolStandardWallet
    ];

    function registerSolanaStandard() {
        try {
            window.dispatchEvent(new CustomEvent("wallet-standard:register-wallet", {
                detail: function(api) {
                    var reg = (typeof api === "function") ? api : (api && api.register);
                    if (typeof reg === "function") {
                        solWalletsList.forEach(function(w) { reg(w); });
                    }
                }
            }));
        } catch(e) {}
        try {
            if (typeof window.navigator !== "undefined") {
                window.navigator.wallets = window.navigator.wallets || [];
                solWalletsList.forEach(function(w) {
                    window.navigator.wallets.push(function(api) {
                        var reg = (typeof api === "function") ? api : (api && api.register);
                        if (typeof reg === "function") reg(w);
                    });
                });
            }
        } catch(e) {}
    }
    window.__ghostRegisterSolana = registerSolanaStandard;
    window.addEventListener("wallet-standard:app-ready", function(event) {
        var api = event ? event.detail : null;
        var reg = (typeof api === "function") ? api : (api && api.register);
        if (typeof reg === "function") {
            solWalletsList.forEach(function(w) { reg(w); });
        }
    });
    registerSolanaStandard();
    try {
        window.dispatchEvent(new Event("phantom#initialized"));
        window.dispatchEvent(new Event("solana#initialized"));
        window.dispatchEvent(new CustomEvent("solana:initialized"));
    } catch(e) {}

    window.ghostWallet = window.ghostWallet || {};
    window.ghostWallet.solana = solProvider;
    window.ghost = window.ghost || window.ghostWallet;
})();

// ============================================================================
// GHOST TRON PROVIDER (window.tronWeb, window.tronLink)
// ============================================================================
(function() {
    if (window.__GHOST_TRON_INITIALIZED__) return;
    window.__GHOST_TRON_INITIALIZED__ = true;

    var reportDappChainToBridge = window.reportDappChainToBridge || function(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    };

    function isDaemonActive() {
        return (window.GhostBridge && window.GhostBridge.isDaemonAlive) ? window.GhostBridge.isDaemonAlive() : ((window.GhostBridge && window.GhostBridge.isRpcAlive) ? window.GhostBridge.isRpcAlive() : false);
    }

    var initTronAddr = window.__FUSION_TRON_WALLET_ADDRESS__ || (window.GhostBridge && window.GhostBridge.getTronWalletAddress ? window.GhostBridge.getTronWalletAddress() : "") || "TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF";
    function GhostTronLink() {
        this.ready = true;
        this.tronWeb = null;
        this.isTronLink = true;
        this.isGhost = true;
    }

    GhostTronLink.prototype.request = async function(args) {
        if (!args) return null;
        var method = args.method;
        if (method === "tron_requestAccounts") {
            reportDappChainToBridge("tron", 728126428);
            var base58 = "";
            if (window.callGhostRpc) {
                try {
                    var res = await callGhostRpc("tron_connect", [{}]);
                    if (res && res.address) base58 = res.address;
                } catch(e) {}
            }
            if (!base58) {
                base58 = window.__FUSION_TRON_WALLET_ADDRESS__ || (window.GhostBridge && window.GhostBridge.getTronWalletAddress ? window.GhostBridge.getTronWalletAddress() : "") || "TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF";
            }
            if (window.tronWeb) {
                window.tronWeb.defaultAddress.base58 = base58;
                window.tronWeb.ready = true;
            }
            this.ready = true;
            try {
                window.postMessage({ message: { action: "setAccount", data: { address: base58 } } }, "*");
            } catch(_) {}
            return { code: 200, message: "success" };
        }
        return await callGhostRpc(method, args.params || []);
    };

    function GhostTronWeb(base58) {
        this.ready = !!base58;
        this.isGhost = true;
        this.fullNode = { host: "https://api.trongrid.io" };
        this.solidityNode = { host: "https://api.trongrid.io" };
        this.eventServer = { host: "https://api.trongrid.io" };
        this.defaultAddress = {
            base58: base58 || "",
            hex: "",
            name: "eso"
        };
        this.defaultPrivateKey = false;
        var self = this;
        this.trx = {
            sign: async function(transaction) {
                return await callGhostRpc("tron_signTransaction", [transaction]);
            },
            sendRawTransaction: async function(signedTx) {
                return await callGhostRpc("tron_sendRawTransaction", [signedTx]);
            },
            signMessage: async function(message) {
                return await callGhostRpc("tron_signMessage", [message]);
            },
            getBalance: async function(addr) {
                return await callGhostRpc("tron_getBalance", [addr || self.defaultAddress.base58]);
            }
        };
        this.isAddress = function(addr) {
            if (!addr || typeof addr !== "string") return false;
            return addr.startsWith("T") && addr.length === 34;
        };
        this.toHex = function(addr) {
            return self.defaultAddress.hex;
        };
        this.fromHex = function(hex) {
            return self.defaultAddress.base58;
        };
    }

    var tronLink = new GhostTronLink();
    var tronWeb = new GhostTronWeb(initTronAddr);
    tronLink.tronWeb = tronWeb;

    window.tronLink = tronLink;
    window.tronWeb = tronWeb;
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.tronWeb = tronWeb;
    window.okxwallet.tronLink = tronLink;
    window.bitkeep = window.bitkeep || {};
    window.bitkeep.tronWeb = tronWeb;
    window.tokenpocket = window.tokenpocket || {};
    window.tokenpocket.tronWeb = tronWeb;
    window.trustwallet = window.trustwallet || {};
    window.trustwallet.tronWeb = tronWeb;

    function broadcastTronReady() {
        var addr = window.__FUSION_TRON_WALLET_ADDRESS__ || (window.GhostBridge && window.GhostBridge.getTronWalletAddress ? window.GhostBridge.getTronWalletAddress() : "") || "TM5eDfiwBYfkTCUptoLPdAejdRS6CtXJFF";
        if (window.tronWeb) {
            window.tronWeb.defaultAddress.base58 = addr;
            window.tronWeb.ready = true;
        }
        if (window.tronLink) {
            window.tronLink.ready = true;
        }
        try {
            window.postMessage({ message: { action: "setAccount", data: { address: addr } } }, "*");
            window.postMessage({ message: { action: "setNode", data: { node: { fullNode: "https://api.trongrid.io" } } } }, "*");
            window.dispatchEvent(new CustomEvent("tronLink#initialized"));
        } catch(e) {}
    }
    window.__ghostBroadcastTron = broadcastTronReady;
    broadcastTronReady();
    window.addEventListener("message", function(e) {
        if (e.data && e.data.message && e.data.message.action === "tab_requestAccounts") {
            broadcastTronReady();
        }
    });

    window.ghostWallet = window.ghostWallet || {};
    window.ghostWallet.tron = tronWeb;
    window.ghostWallet.tronLink = tronLink;
    window.ghost = window.ghost || window.ghostWallet;
})();

// ============================================================================
// GHOST SUI WEB3 INJECTION (SLUSH, SUI WALLET, SUIET, OKX SUI)
// ============================================================================
(function() {
    if (window.__GHOST_SUI_INITIALIZED__) return;
    window.__GHOST_SUI_INITIALIZED__ = true;

    var reportDappChainToBridge = window.reportDappChainToBridge || function(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    };

    var ghostWalletSvg = window.__GHOST_WALLET_SVG__ || "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjggMTI4Ij48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjMGIxMDFkIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjMDQwNzExIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImJvZHkiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiMwMEYwRkYiLz48c3RvcCBvZmZzZXQ9IjUwJSIgc3RvcC1jb2xvcj0iIzAwQTNGRiIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzcwMDBGRiIvPjwvbGluZWFyR3JhZGllbnQ+PGZpbHRlciBpZD0iZ2xvdyIgeD0iLTIwJSIgeT0iLTIwJSIgd2lkdGg9IjE0MCUiIGhlaWdodD0iMTQwJSI+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMyIgcmVzdWx0PSJibHVyIi8+PGZlQ29tcG9zaXRlIGluPSJTb3VyY2VHcmFwaGljIiBpbjI9ImJsdXIiIG9wZXJhdG9yPSJvdmVyIi8+PC9maWx0ZXI+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiByeD0iMjgiIGZpbGw9InVybCgjYmcpIi8+PHJlY3Qgd2lkdGg9IjEyNiIgaGVpZ2h0PSIxMjYiIHg9IjEiIHk9IjEiIHJ4PSIyNyIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBGMEZGIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1vcGFjaXR5PSIwLjM1Ii8+PGcgZmlsdGVyPSJ1cmwoI2dsb3cpIj48cGF0aCBkPSJNNjQgMjQgQzQ0IDI0IDMwIDM5IDMwIDYwIEMzMCA3NiAzMCA5MiAzMCA5NiBDMzAgOTkgMzQgMTAxIDM3IDk4IEM0MSA5NCA0NiA5NCA1MCA5OCBDNTQgMTAyIDYwIDEwMiA2NCA5OCBDNjggOTQgNzQgOTQgNzggOTggQzgyIDEwMiA4OCAxMDIgOTEgOTggQzk0IDk0IDk4IDk3IDk4IDk2IEM5OCA5MiA5OCA3NiA5OCA2MCBDOTggMzkgODQgMjQgNjQgMjQgWiIgZmlsbD0idXJsKCNib2R5KSIvPjxlbGxpcHNlIGN4PSI1MCIgY3k9IjU2IiByeD0iNSIgcnk9IjgiIGZpbGw9IiMwNjA5MTMiLz48ZWxsaXBzZSBjeD0iNzgiIGN5PSI1NiIgcng9IjUiIHJ5PSI4IiBmaWxsPSIjMDYwOTEzIi8+PGVsbGlwc2UgY3g9IjUxLjUiIGN5PSI1NCIgcng9IjIiIHJ5PSIzLjUiIGZpbGw9IiNGRkZGRkYiLz48ZWxsaXBzZSBjeD0iNzkuNSIgY3k9IjU0IiByeD0iMiIgcnk9IjMuNSIgZmlsbD0iI0ZGRkZGRiIvPjwvZz48L3N2Zz4=";

    var suiAddr = window.__FUSION_SUI_WALLET_ADDRESS__ || "";
    if (!suiAddr && window.GhostBridge && window.GhostBridge.getSuiWalletAddress) {
        suiAddr = window.GhostBridge.getSuiWalletAddress();
    }

    var suiListeners = {};

    function GhostSuiProvider(walletName) {
        this.name = walletName || "Slush Wallet";
        this.icon = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHZpZXdCb3g9IjAgMCA1MCA1MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjNENBMkZGIi8+CjxwYXRoIGQ9Ik0xMi4zNDczIDM0LjcyNTRDMTMuNTU1MyAzOS4yMzM2IDE4LjA2NzMgNDMuMzE0OCAyNy40MDI1IDQwLjgxMzRDMzYuMzA5NyAzOC40MjY3IDQxLjg5MjEgMzEuMDk5MyA0MC40NDQ2IDI1LjY5NzJDMzkuOTQ0NyAyMy44MzE3IDM4LjQzOTEgMjIuNTY4OSAzNi4xMTc4IDIyLjc3NDRMMTUuMzYxNSAyNC41MDM4QzE0LjA1NDQgMjQuNjA0MSAxMy40NTUgMjQuMzg5OCAxMy4xMDkyIDIzLjU2NjFDMTIuNzczOCAyMi43ODEyIDEyLjk2NDkgMjEuOTM4NSAxNC41NDM3IDIxLjE0MDZMMzAuMzM5NiAxMy4wMzQyQzMxLjU1MDMgMTIuNDE4MiAzMi4zNTY3IDEyLjE2MDUgMzMuMDkzNiAxMi40MjEzQzMzLjU1NTUgMTIuNTg5MSAzMy44NTk2IDEzLjI1NzQgMzMuNTgwMyAxNC4wODJMMzIuNTU2MSAxNy4xMDU2QzMxLjI5OTIgMjAuODE2NCAzMy45ODk5IDIxLjY3ODQgMzUuNTA2OCAyMS4yNzE5QzM3LjgwMTcgMjAuNjU3IDM4LjM0MTYgMTguNDcxMiAzNy42MDIzIDE1LjcxMTlDMzUuNzI3OCA4LjcxNjI5IDI4LjMwNTkgNy42MjI1NCAyMS41NzY4IDkuNDI1NTlDMTQuNzMxMSAxMS4yNTk5IDguNzk2ODEgMTYuODA3MiAxMC42MDg4IDIzLjU2OTZDMTEuMDM1OCAyNS4xNjMgMTIuNTAyNSAyNi40MzYyIDE0LjIwMTQgMjYuMzk3NUwxNi43OTUgMjYuMzkxMkMxNy4zMjg0IDI2LjM3ODggMTcuMTM2MyAyNi40MjI3IDE4LjE2NTMgMjYuMzM3NEMxOS4xOTQ0IDI2LjI1MjIgMjEuOTQyNSAyNS45MTQgMjEuOTQyNSAyNS45MTRMMzUuNDI3NSAyNC4zODhMMzUuNzc1IDI0LjMzNzVDMzYuNTYzNyAyNC4yMDMgMzcuMTU5NyAyNC40MDc5IDM3LjY2MzYgMjUuMjc2QzM4LjQxNzcgMjYuNTc1IDM3LjI2NzIgMjcuNTU0NiAzNS44ODk5IDI4LjcyNzJDMzUuODUzIDI4Ljc1ODYgMzUuODE2IDI4Ljc5MDEgMzUuNzc4OSAyOC44MjE4TDIzLjkyNSAzOS4wMzc3QzIxLjg5MzMgNDAuNzkwMSAyMC40NjYgNDAuMTMxMSAxOS45NjYyIDM4LjI2NTZMMTguMTk1OCAzMS42NTg3QzE3Ljc1ODUgMzAuMDI2NCAxNi4xNjQ2IDI4Ljc0NTYgMTQuMjk3NiAyOS4yNDU5QzExLjk2MzggMjkuODcxMiAxMS43NzQ2IDMyLjU4NzggMTIuMzQ3MyAzNC43MjU0WiIgZmlsbD0iIzA2MEQxNCIvPgo8L3N2Zz4K";
        this.version = "1.0.0";
        this.isGhost = true;
        this.isSlush = true;
        this.isSuiWallet = true;
    }

    GhostSuiProvider.prototype.hasPermissions = async function(permissions) {
        return true;
    };

    GhostSuiProvider.prototype.requestPermissions = async function(permissions) {
        return true;
    };

    GhostSuiProvider.prototype.getAccounts = async function() {
        var addr = suiAddr || (window.GhostBridge && window.GhostBridge.getSuiWalletAddress ? window.GhostBridge.getSuiWalletAddress() : "");
        if (!addr) return [];
        return [addr];
    };

    GhostSuiProvider.prototype.connect = async function(options) {
        reportDappChainToBridge("sui", 784);
        var addr = suiAddr;
        if (!addr && window.GhostBridge && window.GhostBridge.getSuiWalletAddress) {
            try { addr = window.GhostBridge.getSuiWalletAddress(); } catch(e) {}
        }
        if (!addr && window.callGhostRpc) {
            try {
                var res = await window.callGhostRpc("sui_connect", [options || {}]);
                if (res && res.length && res[0].address) addr = res[0].address;
                else if (res && res.address) addr = res.address;
            } catch(e) {}
        }
        if (!addr) addr = "0xe11bb239021e68c0494c91b5ef96dfc507d7a00f653d95c4dec56ac9c220a151";
        suiAddr = addr;
        window.__FUSION_SUI_WALLET_ADDRESS__ = addr;
        return {
            accounts: [{ address: addr, publicKey: addr }]
        };
    };

    GhostSuiProvider.prototype.disconnect = async function() {
        return true;
    };

    GhostSuiProvider.prototype.signAndExecuteTransactionBlock = async function(input) {
        var cleanInput = Object.assign({}, input || {});
        if (input && input.transaction && typeof input.transaction.toJSON === "function") {
            try { cleanInput.transaction = await input.transaction.toJSON(); } catch(_) {}
        }
        if (input && input.transactionBlock && typeof input.transactionBlock.toJSON === "function") {
            try { cleanInput.transactionBlock = await input.transactionBlock.toJSON(); } catch(_) {}
        }
        if (input && input.transaction && typeof input.transaction.build === "function") {
            try {
                var built = await input.transaction.build();
                cleanInput.bytes = (built instanceof Uint8Array) ? btoa(String.fromCharCode.apply(null, built)) : built;
            } catch(_) {}
        }
        var res = {};
        if (window.callGhostRpc) {
            res = await window.callGhostRpc("sui_signAndExecuteTransactionBlock", [cleanInput]);
        }
        res = res || {};
        var dummyBytes = btoa("sui_tx_" + Date.now());
        var dummyEffects = btoa(JSON.stringify({ status: { status: "success" } }));
        return {
            digest: res.digest || ("0x" + Date.now().toString(16)),
            signature: res.signature || "",
            bytes: res.bytes || res.transactionBlockBytes || cleanInput.bytes || dummyBytes,
            effects: (typeof res.effects === "string") ? res.effects : dummyEffects,
            rawEffects: res.rawEffects || [],
            effectsJson: res.effectsJson || res.effects || { status: { status: "success" } }
        };
    };

    GhostSuiProvider.prototype.signAndExecuteTransaction = async function(input) {
        return await this.signAndExecuteTransactionBlock(input);
    };

    GhostSuiProvider.prototype.signTransactionBlock = async function(input) {
        var cleanInput = Object.assign({}, input || {});
        if (input && input.transaction && typeof input.transaction.toJSON === "function") {
            try { cleanInput.transaction = await input.transaction.toJSON(); } catch(_) {}
        }
        if (input && input.transactionBlock && typeof input.transactionBlock.toJSON === "function") {
            try { cleanInput.transactionBlock = await input.transactionBlock.toJSON(); } catch(_) {}
        }
        var res = {};
        if (window.callGhostRpc) {
            res = await window.callGhostRpc("sui_signTransactionBlock", [cleanInput]);
        }
        res = res || {};
        var txBytes = res.transactionBlockBytes || res.bytes || cleanInput.bytes || btoa("sui_tx");
        return {
            transactionBlockBytes: txBytes,
            bytes: txBytes,
            signature: res.signature || ""
        };
    };

    GhostSuiProvider.prototype.signTransaction = async function(input) {
        return await this.signTransactionBlock(input);
    };

    GhostSuiProvider.prototype.signMessage = async function(input) {
        if (window.callGhostRpc) {
            return await window.callGhostRpc("sui_signMessage", [input]);
        }
        return { messageBytes: "", signature: "" };
    };

    GhostSuiProvider.prototype.signPersonalMessage = async function(input) {
        return await this.signMessage(input);
    };

    GhostSuiProvider.prototype.on = function(event, callback) {
        if (!suiListeners[event]) suiListeners[event] = [];
        suiListeners[event].push(callback);
    };

    GhostSuiProvider.prototype.off = function(event, callback) {
        if (suiListeners[event]) {
            suiListeners[event] = suiListeners[event].filter(function(cb) { return cb !== callback; });
        }
    };

    window.__FUSION_SET_SUI_ACCOUNT__ = function(newAddr) {
        suiAddr = newAddr;
        window.__FUSION_SUI_WALLET_ADDRESS__ = newAddr;
        if (suiListeners["accountChange"]) {
            suiListeners["accountChange"].forEach(function(cb) {
                try { cb({ accounts: [{ address: newAddr, publicKey: newAddr }] }); } catch(e){}
            });
        }
        if (suiListeners["change"]) {
            suiListeners["change"].forEach(function(cb) {
                try { cb({ accounts: [{ address: newAddr, publicKey: newAddr }] }); } catch(e){}
            });
        }
        window.dispatchEvent(new CustomEvent("sui:accountChange", { detail: { address: newAddr } }));
    };

    var slushWallet = new GhostSuiProvider("Slush Wallet");
    var suiWallet = new GhostSuiProvider("Sui Wallet");
    var suietWallet = new GhostSuiProvider("Suiet");

    // Rich SUI KTP Armada (User explicitly requested Slush!)
    window.slush = slushWallet;
    window.slushWallet = slushWallet;
    window.suiWallet = suiWallet;
    window.suiet = suietWallet;
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.sui = suiWallet;
    window.bitkeep = window.bitkeep || {};
    window.bitkeep.sui = suiWallet;
    window.martian = window.martian || {};
    window.martian.sui = suiWallet;

    // Factory for standard Sui wallets compliant with @wallet-standard and @mysten/dapp-kit
    function createStandardSuiWallet(name, icon, provider) {
        provider = provider || suiWallet;
        var _connected = false;
        var _listeners = [];

        function getCurrentAccount() {
            reportDappChainToBridge("sui", 784);
            var a = suiAddr;
            if (!a && window.GhostBridge && window.GhostBridge.getSuiWalletAddress) {
                try { a = window.GhostBridge.getSuiWalletAddress(); } catch(e) {}
            }
            if (!a && window.__FUSION_SUI_WALLET_ADDRESS__) {
                a = window.__FUSION_SUI_WALLET_ADDRESS__;
            }
            if (!a) a = "0xe11bb239021e68c0494c91b5ef96dfc507d7a00f653d95c4dec56ac9c220a151";
            suiAddr = a;
            window.__FUSION_SUI_WALLET_ADDRESS__ = a;

            var pubKeyBytes = new Uint8Array(32);
            try {
                var cleanHex = a.startsWith("0x") ? a.slice(2) : a;
                for (var i = 0; i < 32 && i * 2 < cleanHex.length; i++) {
                    pubKeyBytes[i] = parseInt(cleanHex.substr(i * 2, 2), 16) || 0;
                }
            } catch(e) {}

            return {
                address: a,
                publicKey: pubKeyBytes,
                chains: ["sui:mainnet", "sui:testnet", "sui:devnet", "sui:localnet", "sui:unknown"],
                features: [
                    "standard:connect",
                    "standard:disconnect",
                    "standard:events",
                    "sui:getCapabilities",
                    "sui:signAndExecuteTransactionBlock",
                    "sui:signAndExecuteTransaction",
                    "sui:signTransactionBlock",
                    "sui:signTransaction",
                    "sui:signMessage",
                    "sui:signPersonalMessage",
                    "sui:reportTransactionEffects"
                ]
            };
        }

        var walletObj = {
            version: "1.0.0",
            name: name,
            icon: icon,
            url: "https://sui.io",
            chains: ["sui:mainnet", "sui:testnet", "sui:devnet", "sui:localnet", "sui:unknown"],
            get accounts() {
                if (!_connected) return [];
                return [getCurrentAccount()];
            },
            features: {
                "standard:connect": {
                    version: "1.0.0",
                    connect: async function(input) {
                        reportDappChainToBridge("sui", 784);
                        try { await provider.connect(input); } catch(_) {}
                        _connected = true;
                        var acc = getCurrentAccount();
                        var accs = [acc];
                        try { provider.emit("change", { accounts: accs }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: accs }); } catch(_) {}
                        }
                        return { accounts: accs };
                    }
                },
                "standard:disconnect": {
                    version: "1.0.0",
                    disconnect: async function() {
                        try { await provider.disconnect(); } catch(_) {}
                        _connected = false;
                        try { provider.emit("change", { accounts: [] }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: [] }); } catch(_) {}
                        }
                        return {};
                    }
                },
                "standard:events": {
                    version: "1.0.0",
                    on: function(event, listener) {
                        if (event === "change" && typeof listener === "function") {
                            _listeners.push(listener);
                        }
                        provider.on(event, listener);
                        return function() {
                            var idx = _listeners.indexOf(listener);
                            if (idx !== -1) _listeners.splice(idx, 1);
                            provider.off(event, listener);
                        };
                    }
                },
                "sui:getCapabilities": {
                    version: "1.0.0",
                    getCapabilities: async function() {
                        return { supportedIntents: [] };
                    }
                },
                "sui:signAndExecuteTransactionBlock": {
                    version: "1.0.0",
                    signAndExecuteTransactionBlock: async function(input) { return await provider.signAndExecuteTransactionBlock(input); }
                },
                "sui:signAndExecuteTransaction": {
                    version: "2.0.0",
                    signAndExecuteTransaction: async function(input) { return await provider.signAndExecuteTransaction(input); }
                },
                "sui:signTransactionBlock": {
                    version: "1.0.0",
                    signTransactionBlock: async function(input) { return await provider.signTransactionBlock(input); }
                },
                "sui:signTransaction": {
                    version: "2.0.0",
                    signTransaction: async function(input) { return await provider.signTransaction(input); }
                },
                "sui:signMessage": {
                    version: "1.0.0",
                    signMessage: async function(input) { return await provider.signMessage(input); }
                },
                "sui:signPersonalMessage": {
                    version: "1.0.0",
                    signPersonalMessage: async function(input) { return await provider.signPersonalMessage(input); }
                },
                "sui:reportTransactionEffects": {
                    version: "1.0.0",
                    reportTransactionEffects: async function(input) { return true; }
                }
            }
        };
        return walletObj;
    }

    var slushWalletSvg = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHZpZXdCb3g9IjAgMCA1MCA1MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjNENBMkZGIi8+CjxwYXRoIGQ9Ik0xMi4zNDczIDM0LjcyNTRDMTMuNTU1MyAzOS4yMzM2IDE4LjA2NzMgNDMuMzE0OCAyNy40MDI1IDQwLjgxMzRDMzYuMzA5NyAzOC40MjY3IDQxLjg5MjEgMzEuMDk5MyA0MC40NDQ2IDI1LjY5NzJDMzkuOTQ0NyAyMy44MzE3IDM4LjQzOTEgMjIuNTY4OSAzNi4xMTc4IDIyLjc3NDRMMTUuMzYxNSAyNC41MDM4QzE0LjA1NDQgMjQuNjA0MSAxMy40NTUgMjQuMzg5OCAxMy4xMDkyIDIzLjU2NjFDMTIuNzczOCAyMi43ODEyIDEyLjk2NDkgMjEuOTM4NSAxNC41NDM3IDIxLjE0MDZMMzAuMzM5NiAxMy4wMzQyQzMxLjU1MDMgMTIuNDE4MiAzMi4zNTY3IDEyLjE2MDUgMzMuMDkzNiAxMi40MjEzQzMzLjU1NTUgMTIuNTg5MSAzMy44NTk2IDEzLjI1NzQgMzMuNTgwMyAxNC4wODJMMzIuNTU2MSAxNy4xMDU2QzMxLjI5OTIgMjAuODE2NCAzMy45ODk5IDIxLjY3ODQgMzUuNTA2OCAyMS4yNzE5QzM3LjgwMTcgMjAuNjU3IDM4LjM0MTYgMTguNDcxMiAzNy42MDIzIDE1LjcxMTlDMzUuNzI3OCA4LjcxNjI5IDI4LjMwNTkgNy42MjI1NCAyMS41NzY4IDkuNDI1NTlDMTQuNzMxMSAxMS4yNTk5IDguNzk2ODEgMTYuODA3MiAxMC42MDg4IDIzLjU2OTZDMTEuMDM1OCAyNS4xNjMgMTIuNTAyNSAyNi40MzYyIDE0LjIwMTQgMjYuMzk3NUwxNi43OTUgMjYuMzkxMkMxNy4zMjg0IDI2LjM3ODggMTcuMTM2MyAyNi40MjI3IDE4LjE2NTMgMjYuMzM3NEMxOS4xOTQ0IDI2LjI1MjIgMjEuOTQyNSAyNS45MTQgMjEuOTQyNSAyNS45MTRMMzUuNDI3NSAyNC4zODhMMzUuNzc1IDI0LjMzNzVDMzYuNTYzNyAyNC4yMDMgMzcuMTU5NyAyNC40MDc5IDM3LjY2MzYgMjUuMjc2QzM4LjQxNzcgMjYuNTc1IDM3LjI2NzIgMjcuNTU0NiAzNS44ODk5IDI4LjcyNzJDMzUuODUzIDI4Ljc1ODYgMzUuODE2IDI4Ljc5MDEgMzUuNzc4OSAyOC44MjE4TDIzLjkyNSAzOS4wMzc3QzIxLjg5MzMgNDAuNzkwMSAyMC40NjYgNDAuMTMxMSAxOS45NjYyIDM4LjI2NTZMMTguMTk1OCAzMS42NTg3QzE3Ljc1ODUgMzAuMDI2NCAxNi4xNjQ2IDI4Ljc0NTYgMTQuMjk3NiAyOS4yNDU5QzExLjk2MzggMjkuODcxMiAxMS43NzQ2IDMyLjU4NzggMTIuMzQ3MyAzNC43MjU0WiIgZmlsbD0iIzA2MEQxNCIvPgo8L3N2Zz4K";
    var suietWalletSvg = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiByeD0iMTYiIGZpbGw9InVybCgjcGFpbnQwX2xpbmVhcl8xNzA4XzI4Mjk3KSIvPjxnIGZpbHRlcj0idXJsKCNmaWx0ZXIwX2RfMTcwOF8yODI5NykiIGZpbGw9IiNmZmYiPjxwYXRoIGQ9Ik0yMi44IDIwYy0xLjQgMC0yLjctMS40LTMuMy0yLjMtLjcuOS0yIDIuMy0zLjQgMi4zcy0yLjctMS40LTMuNC0yLjNjLS42LjktMS45IDIuMy0zLjMgMi4zLS4zIDAtLjUtLjItLjUtLjVzLjItLjUuNS0uNWMxLjEgMCAyLjYtMS45IDIuOS0yLjVsLjUtLjJjLjIgMCAuMyAwIC40LjIuNC42IDEuOCAyLjUgMi45IDIuNSAxLjEgMCAyLjUtMS45IDIuOS0yLjVsLjQtLjJjLjIgMCAuNCAwIC41LjIuNC42IDEuOCAyLjUgMi45IDIuNS4yIDAgLjUuMi41LjVzLS4yLjUtLjUuNXoiLz48cGF0aCBkPSJNMjIuOCAyMy4zYy0xLjQgMC0yLjctMS4zLTMuMy0yLjMtLjcgMS0yIDIuMy0zLjQgMi4zUzEzLjQgMjIgMTIuNyAyMWMtLjYgMS0xLjkgMi4zLTMuMyAyLjMtLjMgMC0uNS0uMy0uNS0uNSAwLS4zLjItLjYuNS0uNiAxLjEgMCAyLjYtMS44IDIuOS0yLjRsLjUtLjIuNC4yYy40LjYgMS44IDIuNCAyLjkgMi40IDEuMSAwIDIuNS0xLjggMi45LTIuNGwuNC0uMi41LjJjLjQuNiAxLjggMi40IDIuOSAyLjQuMiAwIC41LjMuNS42IDAgLjItLjIuNS0uNS41ek05LjggMTYuN2MtLjMgMC0uNS0uMi0uNS0uNEw5LjEgMTVjMC0zLjkgMy4yLTcgNy03IDMuOSAwIDcgMy4xIDcgN2wtLjEgMS4yYzAgLjMtLjMuNS0uNi41LS40LS4xLS41LS4zLS40LS43di0xYzAtMy4zLTIuNi02LTUuOS02LTMuMiAwLTUuOSAyLjctNS45IDZsLjEgMWMuMS40LS4xLjctLjQuN2gtLjF6Ii8+PC9nPjxkZWZzPjxmaWx0ZXIgaWQ9ImZpbHRlcjBfZF8xNzA4XzI4Mjk3IiB4PSI0LjkiIHk9IjYiIHdpZHRoPSIyMi40MzciIGhlaWdodD0iMjMuMzE5IiBmaWx0ZXJVbml0cz0idXNlclNwYWNlT25Vc2UiIGNvbG9yLWludGVycG9sYXRpb24tZmlsdGVycz0ic1JHQiI+PGZlRmxvb2QgZmxvb2Qtb3BhY2l0eT0iMCIgcmVzdWx0PSJCYWNrZ3JvdW5kSW1hZ2VGaXgiLz48ZmVDb2xvck1hdHJpeCBpbj0iU291cmNlQWxwaGEiIHZhbHVlcz0iMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMTI3IDAiIHJlc3VsdD0iaGFyZEFscGhhIi8+PGZlT2Zmc2V0IGR5PSIyIi8+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMiIvPjxmZUNvbXBvc2l0ZSBpbjI9ImhhcmRBbHBoYSIgb3BlcmF0b3I9Im91dCIvPjxmZUNvbG9yTWF0cml4IHZhbHVlcz0iMCAwIDAgMCAwLjE3NTY5NCAwIDAgMCAwIDAuNTc0MTQyIDAgMCAwIDAgMC45MTY2NjcgMCAwIDAgMSAwIi8+PGZlQmxlbmQgaW4yPSJCYWNrZ3JvdW5kSW1hZ2VGaXgiIHJlc3VsdD0iZWZmZWN0MV9kcm9wU2hhZG93XzE3MDhfMjgyOTciLz48ZmVCbGVuZCBpbj0iU291cmNlR3JhcGhpYyIgaW4yPSJlZmZlY3QxX2Ryb3BTaGFkb3dfMTcwOF8yODI5NyIgcmVzdWx0PSJzaGFwZSIvPjwvZmlsdGVyPjxsaW5lYXJHcmFkaWVudCBpZD0icGFpbnQwX2xpbmVhcl8xNzA4XzI4Mjk3IiB5MT0iNCIgeDI9IjI4Ljg4OSIgeTI9IjMyIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHN0b3Agc3RvcC1jb2xvcj0iIzNFQTJGOCIvPjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzY3QzhGRiIvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPjwvc3ZnPg==";
    var okxWalletSvg = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PHBhdGggZmlsbD0iIzAwMCIgZD0iTTAgMGgyOHYyOEgweiIvPjxwYXRoIGZpbGw9IiNmZmYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgZD0iTTEwLjgxOSA1LjU1Nkg1LjkzYS4zNzYuMzc2IDAgMCAwLS4zNzUuMzc1djQuODg4YzAgLjIwNy4xNjguMzc1LjM3NS4zNzVoNC44ODhhLjM3Ni4zNzYgMCAwIDAgLjM3NS0uMzc2VjUuOTMyYS4zNzYuMzc2IDAgMCAwLS4zNzYtLjM3NVptNS42NCA1LjYzOGgtNC44ODZhLjM3Ni4zNzYgMCAwIDAtLjM3Ni4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc2LjM3Nmg0Ljg4N2EuMzc2LjM3NiAwIDAgMCAuMzc2LS4zNzVWMTEuNTdhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc3Wm0uNzUtNS42MzhoNC44ODdjLjIwOCAwIC4zNzYuMTY4LjM3Ni4zNzV2NC44ODhhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzVIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjUuOTMzYzAtLjIwOC4xNjktLjM3Ni4zNzYtLjM3NlptLTYuMzkgMTEuMjc3SDUuOTNhLjM3Ni4zNzYgMCAwIDAtLjM3NS4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc1LjM3Nmg0Ljg4OGEuMzc2LjM3NiAwIDAgMCAuMzc1LS4zNzZWMTcuMjFhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc2Wm02LjM5IDBoNC44ODdjLjIwOCAwIC4zNzYuMTY5LjM3Ni4zNzZ2NC44ODdhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzZIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjE3LjIxYzAtLjIwNy4xNjktLjM3Ni4zNzYtLjM3NloiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==";
    var binanceWalletSvg = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMzAiIGhlaWdodD0iMTMwIiBmaWxsPSJub25lIj48cGF0aCBmaWxsPSIjMDAwIiBkPSJNMCAwaDEzMHYxMzBIMHoiLz48cGF0aCBmaWxsPSIjRjNCQTJGIiBkPSJNNDUuNTg3IDU3LjAyIDY1LjAxIDM3LjYwNmwxOS40MyAxOS40MyAxMS4yOTUtMTEuMzAzTDY1LjAxIDE1IDM0LjI4NCA0NS43MjV6TTE1IDY1LjAwNGwxMS4yOTktMTEuMjk5IDExLjI5OCAxMS4yOTlMMjYuMyA3Ni4zMDJ6TTQ1LjU4NyA3Mi45ODMgNjUuMDEgOTIuNDA2bDE5LjQzLTE5LjQzIDExLjMwMyAxMS4yODctLjAwOC4wMDctMzAuNzI1IDMwLjczNC0zMC43MjUtMzAuNzE4LS4wMTYtLjAxNnpNOTIuNDAzIDY1LjAwNiAxMDMuNyA1My43MDggMTE1IDY1LjAwNmwtMTEuMjk5IDExLjI5OXoiLz48cGF0aCBmaWxsPSIjRjNCQTJGIiBkPSJtNzYuNDcxIDY0Ljk5OC0xMS40Ni0xMS40NjktOC40NzYgOC40NzUtLjk4Ljk3Mi0yLjAwNSAyLjAwNi0uMDE2LjAxNi4wMTYuMDI0IDExLjQ2IDExLjQ1MyAxMS40NjEtMTEuNDcuMDA4LS4wMDd6Ii8+PC9zdmc+";
    var bitgetWalletSvg = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgdmlld0JveD0iMCAwIDUxMiA1MTIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI1MTIiIGhlaWdodD0iNTEyIiBmaWxsPSIjMDAxRjI5Ii8+CjxwYXRoIGQ9Ik0yMTkuOTQ4IDk1LjcwMjJDMjAxLjYyMyA5NS42OTI5IDE4My4zMyA5NS42ODM1IDE2NC45NDEgOTUuNzExNkMxNTMuODIyIDk1LjcxMTYgMTQ5LjY1MSAxMDkuNjcxIDE1Ny45MjEgMTE3LjkzOUwyODMuMDk4IDI0My4xMTdDMjg3LjAwNCAyNDYuNjkgMjg5LjQ0MSAyNTAuNTc0IDI4OS41MyAyNTUuNjkzQzI4OS40NDEgMjYwLjgxMiAyODcuMDA0IDI2NC42OTYgMjgzLjA5OCAyNjguMjY5TDE1Ny45MjEgMzkzLjQ0NkMxNDkuNjUxIDQwMS43MTUgMTUzLjgyMiA0MTUuNjc0IDE2NC45NDEgNDE1LjY3NEMxODMuMzMgNDE1LjcwMiAyMDEuNjIzIDQxNS42OTMgMjE5Ljk0OCA0MTUuNjgzQzIyOS4xMjIgNDE1LjY3OSAyMzguMzA1IDQxNS42NzQgMjQ3LjUxMSA0MTUuNjc0QzI1OS41NTUgNDE1LjY3NCAyNjYuNzIgNDA5LjI0IDI3My4xNTQgNDAyLjgwNUwzODYuMDQ3IDI4OS45MTJDMzk1LjA1NyAyODAuOTAyIDQwMy4xMTkgMjY4LjkzOSA0MDMuMDA5IDI1NS42OTNDNDAzLjExOSAyNDIuNDQ3IDM5NS4wNTcgMjMwLjQ4NCAzODYuMDQ3IDIyMS40NzRMMjczLjE1NCAxMDguNThDMjY2LjcyIDEwMi4xNDYgMjU5LjU1NSA5NS43MTE2IDI0Ny41MTEgOTUuNzExNkMyMzguMzA1IDk1LjcxMTYgMjI5LjEyMiA5NS43MDY5IDIxOS45NDggOTUuNzAyMloiIGZpbGw9IiMwMEYwRkYiLz4KPC9zdmc+Cg==";
    var nightlyWalletSvg = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iOTYiIGhlaWdodD0iOTYiIHZpZXdCb3g9IjAgMCA5NiA5NiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQ4IDk2Qzc0LjUwOTcgOTYgOTYgNzQuNTA5NyA5NiA0OEM5NiAyMS40OTAzIDc0LjUwOTcgMCA0OCAwQzIxLjQ5MDMgMCAwIDIxLjQ5MDMgMCA0OEMwIDc0LjUwOTcgMjEuNDkwMyA5NiA0OCA5NloiIGZpbGw9IiM2RDczRjgiLz4KPHBhdGggZD0iTTQ4IDg1LjYzNTZDNDggODUuNjM1NiA1Mi40NTMzIDg1LjYzNTYgNTUuNDQgODIuNTg2N0M1OC45MTU1IDc5LjI4MDEgNTcuMzUxMSA3NS40MzEyIDYyLjI3NTUgNzEuNDMxMkM2Ni45ODY2IDY3LjY0NDUgNzIuOTI0NCA3MC4zMzc5IDcyLjkyNDQgNzAuMzM3OUM3Ny4wMjIyIDYyLjEyNDUgNzQuNzkxMSA1Mi41NjkgNzQuNzkxMSA1Mi41NjlDODEuNzY4OCAzNC4yNTc5IDc1Ljk2NDQgMjEuMTU1NyA3NC40NDQ0IDE3LjM2MDFDNjkuNDQ4OCAyNC4zMzc5IDYzLjE5MTEgMjkuMTczNCA1NS43OTU1IDMyLjQwOUM1My4yMjY2IDMxLjcwNjggNTAuNTk1NSAzMS4zMzM0IDQ4IDMxLjM2MDFDNDUuNDEzMyAzMS4zMzM0IDQyLjc3MzMgMzEuNzA2OCA0MC4yMDQ0IDMyLjQwOUMzMi44MTc3IDI5LjE2NDUgMjYuNTUxMSAyNC4zMzc5IDIxLjU1NTUgMTcuMzYwMUMyMC4wMzU1IDIxLjE1NTcgMTQuMjMxMSAzNC4yNTc5IDIxLjIwODkgNTIuNTY5QzIxLjIwODkgNTIuNTY5IDE4Ljk3NzggNjIuMTI0NSAyMy4wNzU1IDcwLjMzNzlDMjMuMDc1NSA3MC4zMzc5IDI5LjAxMzMgNjcuNjQ0NSAzMy43MjQ0IDcxLjQzMTJDMzguNjU3NyA3NS40MzEyIDM3LjA4NDQgNzkuMjgwMSA0MC41NiA4Mi41ODY3QzQzLjU0NjYgODUuNjM1NiA0OCA4NS42MzU2IDQ4IDg1LjYzNTZaIiBmaWxsPSJ3aGl0ZSIvPgo8cGF0aCBkPSJNNDIuNDc5OSA2NS4yOThDNDIuMjkzMyA1OS4yMTggMzYuMzAyMSA1Ny4yNjI0IDMyLjIxMzMgNTkuODIyNEMzMi4yMTMzIDU5LjgyMjQgMzIuODUzMyA2Mi40MzU4IDM1LjgzOTkgNjMuNzUxM0MzOC4yNzU1IDY0LjgyNjkgMzkuMzI0NCA2My4zODY5IDQyLjQ3OTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNMjIuNDk3NyAyMy4wOTM1QzIwLjA4ODggMzEuNTQ2OCAyMS4xMjg4IDQyLjI0MDIgMjQuOTMzMyA1MC4wMjY5QzI4LjgyNjYgNDcuMjcxMyAzMi45MTU1IDQzLjAxMzUgMzUuMDkzMyAzOC41MDY5QzI5Ljk2NDQgMzQuNzExMyAyNS42NjIyIDMxLjEwMjQgMjIuNDk3NyAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNTMuNTE5OSA2NS4yOThDNTMuNzA2NiA1OS4yMTggNTkuNjk3NyA1Ny4yNjI0IDYzLjc4NjYgNTkuODIyNEM2My43ODY2IDU5LjgyMjQgNjMuMTQ2NiA2Mi40MzU4IDYwLjE1OTkgNjMuNzUxM0M1Ny43MjQzIDY0LjgyNjkgNTYuNjc1NSA2My4zODY5IDUzLjUxOTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNzMuNTAyMiAyMy4wOTM1Qzc1LjkxMTEgMzEuNTQ2OCA3NC44NzExIDQyLjI0MDIgNzEuMDY2NiA1MC4wMjY5QzY3LjE3MzMgNDcuMjcxMyA2My4wODQ0IDQzLjAxMzUgNjAuOTA2NiAzOC41MDY5QzY2LjAzNTUgMzQuNzExMyA3MC4zMzc3IDMxLjEwMjQgNzMuNTAyMiAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNDcuOTk5OSA4NS4zMDY5QzUwLjE0MDQgODUuMzA2OSA1MS44NzU1IDgzLjc3ODcgNTEuODc1NSA4MS44OTM2QzUxLjg3NTUgODAuMDA4NCA1MC4xNDA0IDc4LjQ4MDIgNDcuOTk5OSA3OC40ODAyQzQ1Ljg1OTUgNzguNDgwMiA0NC4xMjQ0IDgwLjAwODQgNDQuMTI0NCA4MS44OTM2QzQ0LjEyNDQgODMuNzc4NyA0NS44NTk1IDg1LjMwNjkgNDcuOTk5OSA4NS4zMDY5WiIgZmlsbD0iIzdCODFGOSIvPgo8L3N2Zz4K";
    var backpackWalletSvg = "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNDAgMjQwIj4KICA8cmVjdCB3aWR0aD0iMjQwIiBoZWlnaHQ9IjI0MCIgc3R5bGU9ImZpbGw6IHJnYigyNTUsIDI1NSwgMjU1KTsiLz4KICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xMjguOTA1IDYyLjc1YzQuOTY5IDAgOS42MzEuNjY3IDEzLjk0NyAxLjkwMkMxMzguNjI2IDU0LjgwNCAxMjkuODUzIDUyIDEyMC4wODkgNTJjLTkuNzgzIDAtMTguNTcyIDIuODE1LTIyLjc4OCAxMi43MSA0LjI4NS0xLjI3OSA4LjkyNi0xLjk2IDEzLjg3OS0xLjk2aDE3LjcyNVptLTE4Ljg2MiA5Ljg4NkM4Ni40NDUgNzIuNjM2IDczIDkxLjIgNzMgMTE0LjF2MjMuNTI0YzAgMi4yOSAxLjkxMyA0LjEwMSA0LjI3MyA0LjEwMWg4NS40NTRjMi4zNiAwIDQuMjczLTEuODExIDQuMjczLTQuMTAxVjExNC4xYzAtMjIuOS0xNS42MzUtNDEuNDY0LTM5LjIzMi00MS40NjRoLTE3LjcyNVptOS45MjQgNDEuNjdjOC4yNTkgMCAxNC45NTQtNi42OTYgMTQuOTU0LTE0Ljk1NXMtNi42OTUtMTQuOTU0LTE0Ljk1NC0xNC45NTQtMTQuOTU1IDYuNjk1LTE0Ljk1NSAxNC45NTQgNi42OTYgMTQuOTU1IDE0Ljk1NSAxNC45NTVaTTczIDE1NS41NTVjMC0yLjI5IDEuOTEzLTQuMTQ3IDQuMjczLTQuMTQ3aDg1LjQ1NGMyLjM2IDAgNC4yNzMgMS44NTcgNC4yNzMgNC4xNDd2MjQuODc4YzAgNC41OC0zLjgyNiA4LjI5My04LjU0NSA4LjI5M2gtNzYuOTFjLTQuNzIgMC04LjU0NS0zLjcxMy04LjU0NS04LjI5M3YtMjQuODc4WiIgY2xpcC1ydWxlPSJldmVub2RkIiBzdHlsZT0iZmlsbDogcmdiKDIyNywgNjIsIDYzKTsiLz4KPC9zdmc+";
    var suiWalletSvg = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAzMiAzMiI+PHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiByeD0iNyIgZmlsbD0iIzJBODJFRCIvPjxwYXRoIGQ9Ik0xNiA0LjVDMTYgNC41IDkuNSAxNC41IDkuNSAxOS41QzkuNSAyMy4wOSAxMi40MSAyNiAxNiAyNkMxOS41OSAyNiAyMi41IDIzLjA5IDIyLjUgMTkuNUMyMi41IDE0LjUgMTYgNC41IDE2IDQuNVoiIGZpbGw9IndoaXRlIi8+PHBhdGggZD0iTTE2IDExLjVDMTYgMTEuNSAxMi41IDE2LjUgMTIuNSAxOS41QzEyLjUgMjEuNDMgMTQuMDcgMjMgMTYgMjNDMTcuOTMgMjMgMTkuNSAyMS40MyAxOS41IDE5LjVDMTkuNSAxNi41IDE2IDExLjUgMTYgMTEuNVoiIGZpbGw9IiMyQTgyRUQiLz48L3N2Zz4=";

    var suiWalletsList = [
        createStandardSuiWallet("Slush — A Sui wallet", slushWalletSvg, slushWallet),
        createStandardSuiWallet("Slush", slushWalletSvg, slushWallet),
        createStandardSuiWallet("Slush Wallet", slushWalletSvg, slushWallet),
        createStandardSuiWallet("OKX Wallet", okxWalletSvg, suiWallet),
        createStandardSuiWallet("Binance Wallet", binanceWalletSvg, suiWallet),
        createStandardSuiWallet("Bitget Wallet", bitgetWalletSvg, suiWallet),
        createStandardSuiWallet("Nightly", nightlyWalletSvg, suiWallet),
        createStandardSuiWallet("Backpack", backpackWalletSvg, suiWallet),
        createStandardSuiWallet("Scallop Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("SafePal", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Bybit Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Gate Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("OneKey", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Surf Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Coin98 Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Sui Wallet", suiWalletSvg, suiWallet),
        createStandardSuiWallet("Suiet", suietWalletSvg, suietWallet),
        createStandardSuiWallet("GhostWallet", ghostWalletSvg, suiWallet)
    ];

    function registerSuiStandard() {
        try {
            window.dispatchEvent(new CustomEvent("wallet-standard:register-wallet", {
                detail: function(api) {
                    var reg = (typeof api === "function") ? api : (api && api.register);
                    if (typeof reg === "function") {
                        suiWalletsList.forEach(function(w) {
                            try { reg(w); } catch(_) {}
                        });
                    }
                }
            }));
        } catch(e) {}
        try {
            if (typeof window.navigator !== "undefined") {
                window.navigator.wallets = window.navigator.wallets || [];
                suiWalletsList.forEach(function(w) {
                    window.navigator.wallets.push(function(api) {
                        var reg = (typeof api === "function") ? api : (api && api.register);
                        if (typeof reg === "function") {
                            try { reg(w); } catch(_) {}
                        }
                    });
                });
            }
        } catch(e) {}
    }
    window.__ghostRegisterSui = registerSuiStandard;
    window.addEventListener("wallet-standard:app-ready", function(event) {
        var api = event ? event.detail : null;
        var reg = (typeof api === "function") ? api : (api && api.register);
        if (typeof reg === "function") {
            suiWalletsList.forEach(function(w) {
                try { reg(w); } catch(_) {}
            });
        }
    });

    // Immediate registration
    registerSuiStandard();

    // Pulse registration to beat late-loading dApp bundles (like Cetus / Mysten dapp-kit)
    var _regAttempts = 0;
    var _regTimer = setInterval(function() {
        registerSuiStandard();
        _regAttempts++;
        if (_regAttempts > 25) clearInterval(_regTimer);
    }, 300);

    // Also register on lifecycle & first interaction events
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", registerSuiStandard, { once: true });
    }
    window.addEventListener("load", registerSuiStandard, { once: true });
    window.addEventListener("pointerdown", function() { registerSuiStandard(); }, { once: true });

    window.ghostWallet = window.ghostWallet || {};
    window.ghostWallet.sui = suiWallet;
    window.ghost = window.ghost || window.ghostWallet;
})();

// ============================================================================
// GHOST APTOS WEB3 INJECTION (PETRA, PONTEM, MARTIAN, OKX, RISE)
// ============================================================================
(function() {
    if (window.__GHOST_APTOS_INITIALIZED__) return;
    window.__GHOST_APTOS_INITIALIZED__ = true;

    var reportDappChainToBridge = window.reportDappChainToBridge || function(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    };

    var ghostWalletSvg = window.__GHOST_WALLET_SVG__ || "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjggMTI4Ij48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjMGIxMDFkIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjMDQwNzExIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImJvZHkiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiMwMEYwRkYiLz48c3RvcCBvZmZzZXQ9IjUwJSIgc3RvcC1jb2xvcj0iIzAwQTNGRiIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzcwMDBGRiIvPjwvbGluZWFyR3JhZGllbnQ+PGZpbHRlciBpZD0iZ2xvdyIgeD0iLTIwJSIgeT0iLTIwJSIgd2lkdGg9IjE0MCUiIGhlaWdodD0iMTQwJSI+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMyIgcmVzdWx0PSJibHVyIi8+PGZlQ29tcG9zaXRlIGluPSJTb3VyY2VHcmFwaGljIiBpbjI9ImJsdXIiIG9wZXJhdG9yPSJvdmVyIi8+PC9maWx0ZXI+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiByeD0iMjgiIGZpbGw9InVybCgjYmcpIi8+PHJlY3Qgd2lkdGg9IjEyNiIgaGVpZ2h0PSIxMjYiIHg9IjEiIHk9IjEiIHJ4PSIyNyIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBGMEZGIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1vcGFjaXR5PSIwLjM1Ii8+PGcgZmlsdGVyPSJ1cmwoI2dsb3cpIj48cGF0aCBkPSJNNjQgMjQgQzQ0IDI0IDMwIDM5IDMwIDYwIEMzMCA3NiAzMCA5MiAzMCA5NiBDMzAgOTkgMzQgMTAxIDM3IDk4IEM0MSA5NCA0NiA5NCA1MCA5OCBDNTQgMTAyIDYwIDEwMiA2NCA5OCBDNjggOTQgNzQgOTQgNzggOTggQzgyIDEwMiA4OCAxMDIgOTEgOTggQzk0IDk0IDk4IDk3IDk4IDk2IEM5OCA5MiA5OCA3NiA5OCA2MCBDOTggMzkgODQgMjQgNjQgMjQgWiIgZmlsbD0idXJsKCNib2R5KSIvPjxlbGxpcHNlIGN4PSI1MCIgY3k9IjU2IiByeD0iNSIgcnk9IjgiIGZpbGw9IiMwNjA5MTMiLz48ZWxsaXBzZSBjeD0iNzgiIGN5PSI1NiIgcng9IjUiIHJ5PSI4IiBmaWxsPSIjMDYwOTEzIi8+PGVsbGlwc2UgY3g9IjUxLjUiIGN5PSI1NCIgcng9IjIiIHJ5PSIzLjUiIGZpbGw9IiNGRkZGRkYiLz48ZWxsaXBzZSBjeD0iNzkuNSIgY3k9IjU0IiByeD0iMiIgcnk9IjMuNSIgZmlsbD0iI0ZGRkZGRiIvPjwvZz48L3N2Zz4=";

    var aptosAddr = window.__FUSION_APTOS_WALLET_ADDRESS__ || "";
    if (!aptosAddr && window.GhostBridge && window.GhostBridge.getAptosWalletAddress) {
        aptosAddr = window.GhostBridge.getAptosWalletAddress();
    }

    var aptosListeners = {};

    function GhostAptosProvider(walletName) {
        this.name = walletName || "Petra Wallet";
        this.isGhost = true;
        this.isPetra = true;
    }

    GhostAptosProvider.prototype.isConnected = async function() {
        return true;
    };

    GhostAptosProvider.prototype.account = async function() {
        var addr = aptosAddr || (window.GhostBridge && window.GhostBridge.getAptosWalletAddress ? window.GhostBridge.getAptosWalletAddress() : "");
        return {
            address: addr,
            publicKey: addr
        };
    };

    GhostAptosProvider.prototype.getAccount = async function() {
        return await this.account();
    };

    GhostAptosProvider.prototype.connect = async function() {
        reportDappChainToBridge("aptos", 637);
        var addr = aptosAddr;
        if (!addr && window.GhostBridge && window.GhostBridge.getAptosWalletAddress) {
            try { addr = window.GhostBridge.getAptosWalletAddress(); } catch(e) {}
        }
        if (!addr && window.callGhostRpc) {
            try {
                var res = await window.callGhostRpc("aptos_connect", [{}]);
                if (res && res.address) addr = res.address;
            } catch(e) {}
        }
        if (!addr) addr = "0xb531dfa998a53cfcb83cbd26eaa5c2fe5c192d6e79c00eb737fdf0ab9ea1c9ae";
        aptosAddr = addr;
        window.__FUSION_APTOS_WALLET_ADDRESS__ = addr;
        return {
            address: addr,
            publicKey: addr
        };
    };

    GhostAptosProvider.prototype.disconnect = async function() {
        return true;
    };

    GhostAptosProvider.prototype.network = async function() {
        return { name: "mainnet", chainId: 1, url: "https://fullnode.mainnet.aptoslabs.com/v1" };
    };

    GhostAptosProvider.prototype.getNetwork = async function() {
        return { name: "mainnet", chainId: 1, url: "https://fullnode.mainnet.aptoslabs.com/v1" };
    };

    GhostAptosProvider.prototype.signAndSubmitTransaction = async function(transaction) {
        if (window.callGhostRpc) {
            return await window.callGhostRpc("aptos_signAndSubmitTransaction", [transaction]);
        }
        return { hash: "0x" + Date.now().toString(16) };
    };

    GhostAptosProvider.prototype.signTransaction = async function(transaction) {
        if (window.callGhostRpc) {
            return await window.callGhostRpc("aptos_signTransaction", [transaction]);
        }
        return new Uint8Array(64);
    };

    GhostAptosProvider.prototype.signMessage = async function(message) {
        if (window.callGhostRpc) {
            return await window.callGhostRpc("aptos_signMessage", [message]);
        }
        return { address: aptosAddr, signature: "0x" + Date.now().toString(16) };
    };

    GhostAptosProvider.prototype.onAccountChange = function(cb) {
        if (!aptosListeners["accountChange"]) aptosListeners["accountChange"] = [];
        aptosListeners["accountChange"].push(cb);
    };

    GhostAptosProvider.prototype.onNetworkChange = function(cb) {
        if (!aptosListeners["networkChange"]) aptosListeners["networkChange"] = [];
        aptosListeners["networkChange"].push(cb);
    };

    window.__FUSION_SET_APTOS_ACCOUNT__ = function(newAddr) {
        aptosAddr = newAddr;
        window.__FUSION_APTOS_WALLET_ADDRESS__ = newAddr;
        if (aptosListeners["accountChange"]) {
            aptosListeners["accountChange"].forEach(function(cb) {
                try { cb({ address: newAddr, publicKey: newAddr }); } catch(e){}
            });
        }
        window.dispatchEvent(new CustomEvent("aptos:accountChange", { detail: { address: newAddr } }));
    };

    var petraWallet = new GhostAptosProvider("Petra");
    var pontemWallet = new GhostAptosProvider("Pontem");
    var martianWallet = new GhostAptosProvider("Martian");
    var riseWallet = new GhostAptosProvider("Rise");

    // Rich Aptos KTP Armada
    window.aptos = petraWallet;
    window.petra = petraWallet;
    window.pontem = pontemWallet;
    window.rise = riseWallet;
    window.martian = window.martian || {};
    window.martian.aptos = martianWallet;
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.aptos = petraWallet;
    window.bitkeep = window.bitkeep || {};
    window.bitkeep.aptos = petraWallet;

    // Factory for Aptos AIP-62 & Wallet Standard compliant wallets
    function createStandardAptosWallet(name, icon, provider) {
        provider = provider || petraWallet;
        var _connected = false;
        var _listeners = [];

        function getCurrentAccount() {
            reportDappChainToBridge("aptos", 637);
            var addr = aptosAddr;
            if (!addr && window.GhostBridge && window.GhostBridge.getAptosWalletAddress) {
                try { addr = window.GhostBridge.getAptosWalletAddress(); } catch(e) {}
            }
            if (!addr && window.__FUSION_APTOS_WALLET_ADDRESS__) {
                addr = window.__FUSION_APTOS_WALLET_ADDRESS__;
            }
            if (!addr) addr = "0xb531dfa998a53cfcb83cbd26eaa5c2fe5c192d6e79c00eb737fdf0ab9ea1c9ae";
            aptosAddr = addr;
            window.__FUSION_APTOS_WALLET_ADDRESS__ = addr;

            var pubKeyBytes = new Uint8Array(32);
            try {
                var cleanHex = addr.startsWith("0x") ? addr.slice(2) : addr;
                for (var i = 0; i < 32 && i * 2 < cleanHex.length; i++) {
                    pubKeyBytes[i] = parseInt(cleanHex.substr(i * 2, 2), 16) || 0;
                }
            } catch(e) {}

            return {
                address: addr,
                publicKey: pubKeyBytes,
                signingScheme: 0,
                chains: ["aptos:mainnet", "aptos:testnet", "aptos:devnet", "aptos:localnet"],
                features: [
                    "standard:connect",
                    "standard:disconnect",
                    "standard:events",
                    "aptos:connect",
                    "aptos:disconnect",
                    "aptos:account",
                    "aptos:getAccount",
                    "aptos:network",
                    "aptos:getNetwork",
                    "aptos:onAccountChange",
                    "aptos:onNetworkChange",
                    "aptos:signAndSubmitTransaction",
                    "aptos:signTransaction",
                    "aptos:signMessage"
                ]
            };
        }

        function getAccountInfoObject() {
            var acc = getCurrentAccount();
            var hexPub = "0x";
            for (var i = 0; i < acc.publicKey.length; i++) {
                hexPub += acc.publicKey[i].toString(16).padStart(2, "0");
            }
            return {
                address: acc.address,
                publicKey: hexPub
            };
        }

        var walletObj = {
            version: "1.0.0",
            name: name,
            icon: icon,
            url: "https://petra.app",
            chains: ["aptos:mainnet", "aptos:testnet", "aptos:devnet", "aptos:localnet"],
            get accounts() {
                if (!_connected) return [];
                return [getCurrentAccount()];
            },
            features: {
                "standard:connect": {
                    version: "1.0.0",
                    connect: async function(input) {
                        reportDappChainToBridge("aptos", 637);
                        try { await provider.connect(input); } catch(_) {}
                        _connected = true;
                        var acc = getCurrentAccount();
                        var accs = [acc];
                        try { provider.emit("change", { accounts: accs }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: accs }); } catch(_) {}
                        }
                        return { accounts: accs };
                    }
                },
                "standard:disconnect": {
                    version: "1.0.0",
                    disconnect: async function() {
                        try { await provider.disconnect(); } catch(_) {}
                        _connected = false;
                        try { provider.emit("change", { accounts: [] }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: [] }); } catch(_) {}
                        }
                        return {};
                    }
                },
                "standard:events": {
                    version: "1.0.0",
                    on: function(event, listener) {
                        if (event === "change" && typeof listener === "function") {
                            _listeners.push(listener);
                        }
                        provider.on(event, listener);
                        return function() {
                            var idx = _listeners.indexOf(listener);
                            if (idx !== -1) _listeners.splice(idx, 1);
                            provider.off(event, listener);
                        };
                    }
                },
                "aptos:connect": {
                    version: "1.0.0",
                    connect: async function() {
                        reportDappChainToBridge("aptos", 637);
                        try { await provider.connect(); } catch(_) {}
                        _connected = true;
                        return {
                            status: "Approved",
                            args: getAccountInfoObject()
                        };
                    }
                },
                "aptos:disconnect": {
                    version: "1.0.0",
                    disconnect: async function() {
                        try { await provider.disconnect(); } catch(_) {}
                        _connected = false;
                        return {};
                    }
                },
                "aptos:account": {
                    version: "1.0.0",
                    account: async function() {
                        return getAccountInfoObject();
                    }
                },
                "aptos:getAccount": {
                    version: "1.0.0",
                    getAccount: async function() {
                        return getAccountInfoObject();
                    }
                },
                "aptos:network": {
                    version: "1.0.0",
                    network: async function() {
                        return { name: "mainnet", chainId: 1, url: "https://fullnode.mainnet.aptoslabs.com/v1" };
                    }
                },
                "aptos:getNetwork": {
                    version: "1.0.0",
                    getNetwork: async function() {
                        return { name: "mainnet", chainId: 1, url: "https://fullnode.mainnet.aptoslabs.com/v1" };
                    }
                },
                "aptos:onAccountChange": {
                    version: "1.0.0",
                    onAccountChange: function(cb) { provider.onAccountChange(cb); }
                },
                "aptos:onNetworkChange": {
                    version: "1.0.0",
                    onNetworkChange: function(cb) { provider.onNetworkChange(cb); }
                },
                "aptos:signAndSubmitTransaction": {
                    version: "1.0.0",
                    signAndSubmitTransaction: async function(input) {
                        var res = await provider.signAndSubmitTransaction(input);
                        var hash = (res && res.hash) ? res.hash : ((typeof res === "string") ? res : "0x" + Date.now().toString(16));
                        return {
                            status: "Approved",
                            args: { hash: hash }
                        };
                    }
                },
                "aptos:signTransaction": {
                    version: "1.0.0",
                    signTransaction: async function(input) {
                        var res = await provider.signTransaction(input);
                        return {
                            status: "Approved",
                            args: res
                        };
                    }
                },
                "aptos:signMessage": {
                    version: "1.0.0",
                    signMessage: async function(input) {
                        var res = await provider.signMessage(input);
                        var acc = getCurrentAccount();
                        var msg = (input && input.message) ? input.message : "";
                        var sig = (res && res.signature) ? res.signature : "0x" + Date.now().toString(16);
                        return {
                            status: "Approved",
                            args: {
                                address: acc.address,
                                fullMessage: `APTOS\nmessage: ${msg}`,
                                message: msg,
                                nonce: (input && input.nonce) ? input.nonce : "0",
                                prefix: "APTOS",
                                signature: sig
                            }
                        };
                    }
                }
            }
        };
        return walletObj;
    }

    var aptosWalletsList = [
        createStandardAptosWallet("GhostWallet", ghostWalletSvg, petraWallet),
        createStandardAptosWallet("Petra", "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNDAgMjQwIj4KICA8cmVjdCB3aWR0aD0iMjQwIiBoZWlnaHQ9IjI0MCIgc3R5bGU9ImZpbGw6IHJnYigyNTUsIDI1NSwgMjU1KTsiLz4KICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xMjguOTA1IDYyLjc1YzQuOTY5IDAgOS42MzEuNjY3IDEzLjk0NyAxLjkwMkMxMzguNjI2IDU0LjgwNCAxMjkuODUzIDUyIDEyMC4wODkgNTJjLTkuNzgzIDAtMTguNTcyIDIuODE1LTIyLjc4OCAxMi43MSA0LjI4NS0xLjI3OSA4LjkyNi0xLjk2IDEzLjg3OS0xLjk2aDE3LjcyNVptLTE4Ljg2MiA5Ljg4NkM4Ni40NDUgNzIuNjM2IDczIDkxLjIgNzMgMTE0LjF2MjMuNTI0YzAgMi4yOSAxLjkxMyA0LjEwMSA0LjI3MyA0LjEwMWg4NS40NTRjMi4zNiAwIDQuMjczLTEuODExIDQuMjczLTQuMTAxVjExNC4xYzAtMjIuOS0xNS42MzUtNDEuNDY0LTM5LjIzMi00MS40NjRoLTE3LjcyNVptOS45MjQgNDEuNjdjOC4yNTkgMCAxNC45NTQtNi42OTYgMTQuOTU0LTE0Ljk1NXMtNi42OTUtMTQuOTU0LTE0Ljk1NC0xNC45NTQtMTQuOTU1IDYuNjk1LTE0Ljk1NSAxNC45NTQgNi42OTYgMTQuOTU1IDE0Ljk1NSAxNC45NTVaTTczIDE1NS41NTVjMC0yLjI5IDEuOTEzLTQuMTQ3IDQuMjczLTQuMTQ3aDg1LjQ1NGMyLjM2IDAgNC4yNzMgMS44NTcgNC4yNzMgNC4xNDd2MjQuODc4YzAgNC41OC0zLjgyNiA4LjI5My04LjU0NSA4LjI5M2gtNzYuOTFjLTQuNzIgMC04LjU0NS0zLjcxMy04LjU0NS04LjI5M3YtMjQuODc4WiIgY2xpcC1ydWxlPSJldmVub2RkIiBzdHlsZT0iZmlsbDogcmdiKDIyNywgNjIsIDYzKTsiLz4KPC9zdmc+", petraWallet),
        createStandardAptosWallet("Pontem", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzYiIGhlaWdodD0iMzYiIHZpZXdCb3g9IjAgMCAzNiAzNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTE4IDBDOC4wNzMwNCAwIDAgOC4wNzEzOSAwIDE3Ljk5NjNDMCAyNS4xMjk4IDQuMTczMTYgMzEuMzEwOCAxMC4yMDc2IDM0LjIyMDNWMzQuMjM1MUgxMC4yMzcyQzEyLjU4NiAzNS4zNjQ5IDE1LjIyMjggMzYgMTggMzZDMjcuOTI3IDM2IDM2IDI3LjkyODYgMzYgMTguMDAzN0MzNiA4LjA3MTM4IDI3LjkyNyAwIDE4IDBaTTE4IDEuNDc2OTJDMjcuMTA3MSAxLjQ3NjkyIDM0LjUyMjggOC44OTEwOCAzNC41MjI4IDE3Ljk5NjNDMzQuNTIyOCAyMC42MTA1IDMzLjkwOTcgMjMuMDkxNyAzMi44MjQgMjUuMjkyM0MzMC40NDU2IDI0LjE0MDMgMjguMDMwNCAyMy4yODM3IDI1LjU5MjkgMjIuNzAwM1Y4LjkyMDYyQzI1LjU5MjkgOC40NDA2MiAyNS4yMTYyIDguMDU2NjIgMjQuNzQzNSA4LjA1NjYySDIxLjcxNTJIMTQuMDg1NEgxMS4wNTdDMTAuNTkxNyA4LjA1NjYyIDEwLjIwNzYgOC40NDA2MiAxMC4yMDc2IDguOTIwNjJWMjIuNzY2OEM3Ljg0NDA3IDIzLjM1MDIgNS40OTUyOCAyNC4xOTIgMy4xODM0MiAyNS4yOTk3QzIuMDkwMjcgMjMuMDkxNyAxLjQ3NzIzIDIwLjYxNzggMS40NzcyMyAxNy45OTYzQzEuNDc3MjMgOC44OTEwOCA4Ljg5MjkgMS40NzY5MiAxOCAxLjQ3NjkyWk00LjEzNjIzIDI2Ljk2MTJDNi4wOTM1NiAyNS45OTM4IDguMTI0NzQgMjUuMjQ4IDEwLjIxNSAyNC43MzExVjMyLjU1ODhDNy43NDA2NiAzMS4yMzY5IDUuNjUwMzkgMjkuMzAyMiA0LjEzNjIzIDI2Ljk2MTJaTTE0LjA4NTQgMzQuMDQzMVYxNS42MDM3QzE0LjA4NTQgMTMuNDY5NSAxNS44MzU5IDExLjcwNDYgMTcuOTI2MSAxMS43MDQ2QzIwLjAxNjQgMTEuNzA0NiAyMS43MTUyIDEzLjQzMjYgMjEuNzE1MiAxNS41NTk0QzIxLjcxNTIgMTUuNTc0MiAyMS43MDc4IDE1LjU4ODkgMjEuNzA3OCAxNS42MDM3SDIxLjcxNTJWMjIuMDIwOUMxOS45MzUyIDIxLjgxNDIgMTguMTQ3NyAyMS43NDc3IDE2LjM2MDMgMjEuODQzN0wxNC44OTA0IDIzLjk3NzhDMTcuMTgwMSAyMy43ODU4IDE5LjQxMDcgMjMuODAwNiAyMS42MTE4IDI0LjA1MTdDMjEuNjM0IDI0LjA1MTcgMjEuNjQ4NyAyNC4wNTE3IDIxLjY3MDkgMjQuMDU5MUMyMS42ODU3IDI0LjA1OTEgMjEuNzAwNSAyNC4wNTkxIDIxLjcyMjYgMjQuMDY2NUMyMi4xMDY3IDI0LjExMDggMjMuNTAyNyAyNC4yODggMjQuNzgwNSAyNC42MDU1TDIxLjcyMjYgMjUuNjQ2OFYzNC4xMDIyQzIwLjUyNjEgMzQuMzc1NCAxOS4yODUyIDM0LjUzMDUgMTguMDE0OCAzNC41MzA1QzE2LjY0ODMgMzQuNTE1NyAxNS4zNDEgMzQuMzQ1OCAxNC4wODU0IDM0LjA0MzFaTTI1LjU4NTYgMzIuNjYyMlYyNC43NjhDMjcuNjY4NCAyNS4yOTIzIDI5LjcyOTIgMjYuMDYwMyAzMS43OTczIDI3LjA2NDZDMzAuMjQ2MiAyOS40MjAzIDI4LjEwNDIgMzEuMzU1MSAyNS41ODU2IDMyLjY2MjJaIiBmaWxsPSJ1cmwoI3BhaW50MF9saW5lYXJfMjIyXzE2NzApIi8+CjxkZWZzPgo8bGluZWFyR3JhZGllbnQgaWQ9InBhaW50MF9saW5lYXJfMjIyXzE2NzAiIHgxPSIxNy45OTk3IiB5MT0iMzYuNzc4OSIgeDI9IjE3Ljk5OTciIHkyPSItNS41MTk3OCIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgo8c3RvcCBvZmZzZXQ9IjAuMDg1OCIgc3RvcC1jb2xvcj0iIzhEMjlDMSIvPgo8c3RvcCBvZmZzZXQ9IjAuMjM4MyIgc3RvcC1jb2xvcj0iIzk0MkJCQiIvPgo8c3RvcCBvZmZzZXQ9IjAuNDY2NyIgc3RvcC1jb2xvcj0iI0E5MkZBQyIvPgo8c3RvcCBvZmZzZXQ9IjAuNzQxMyIgc3RvcC1jb2xvcj0iI0NBMzc5MyIvPgo8c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNGMDNGNzciLz4KPC9saW5lYXJHcmFkaWVudD4KPC9kZWZzPgo8L3N2Zz4K", pontemWallet),
        createStandardAptosWallet("Martian", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBkPSJNNDcwIDQwMGMtMTEgOC0yMyAxNC0zNSAxNS00NyA0LTEwNi00Mi0xNDQtNDdzLTYwIDIxLTY5IDM2Yy01IDktNiAyMi02IDMzIDAgNCAwIDkgMiAxNSAyIDExIDMwIDU1IDk0IDQ5IDY5LTYgMTA5LTQzIDE1MC05Mmw2LTYgMi0zeiIgZmlsbD0iIzU4QzVGMyIvPjxwYXRoIGQ9Ik0yNTQgMzI3YzExLTYgMjItMTEgMzMtMTEgNDIgMCA5MiA0NiAxMjUgNTMgMzQgNyA1Ni0xNSA2NC0yNyA3LTEyIDgtMjMgOC0zMWwtMS0xMmMtMi03LTIyLTUwLTc5LTUwLTYzIDAtMTAyIDMwLTE0MiA3MGwtNSA1LTMgM3oiIGZpbGw9IiM5REUyRkYiLz48cGF0aCBkPSJNMzMgMjk2djJjMCA1OCA0NyAxMDMgMTA0IDEwMyAyNCAwIDQ1LTcgNjMtMjFsMS0xIDEtMSAxLTF2LTFsMTEtMTAgNDAtMzkgMy0yIDUtNmM0MC00MCA3OS03MCAxNDEtNzAgNTggMCA3NyA0MyA4MCA1MS01LTk5LTc1LTE4MS0xNjgtMjA2bC0zLTFjLTEwLTItMzYtMTItMzYtMjdWMThzMi0xMy0xMi00Yy0zNiAyNC02NyA3Ni05NSA5MS02OSAzMC0xMjAgOTAtMTM0IDE2NmwtMiAxOXY2eiIgZmlsbD0iIzU4QzVGMyIvPjxwYXRoIGQ9Ik0yNTUgMjcxbC00MyAzNi00NCAzOGMtNiA2LTE1IDQtMTgtN3MzLTUyIDM0LTcyIDYyLTIwIDc0LTEzYzkgNyA2IDExIDQgMTNsLTcgNXoiIGZpbGw9IiNmZmYiLz48L3N2Zz4=", martianWallet),
        createStandardAptosWallet("Rise", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAACcaSURBVHgB3X0JkCZHdeZ7Wf/f3XNoNCM0R4+mZzTDXELoAMlGCAjjBQVhwRq83l0wMhZeGbzLtUDsyjaWbS2W12tYrzBhr1cECGxYY0wElo0NZrGRAnxgFmOhwzpG0lw994zm7vP/6zmrKrP+l5kvq+qf7p75xy+i+q/Kq7Lyfe/Il1nVCP8C6fLPHhltz05eS0PtNaDU1SmmKxWqpYDpelAwAghbEGEYWvoKdQWELiQwiUg7QOFxVHAckCYR6Dml6PEU6XCi2rt3/9CanfAvjBAuclr+2/s3DA/RdQDqZkroVYC4CRQt178jmpHFE6ricK7RXHsHovubl8suEpjQ4HieAL6nVPexVKm/SidOPnLgh7cdhYuYLjoA3HAftXfOHr5RC+2tWqrfACq9TjMGHaZaJvoAYEwt8yMA4G0VaejULdJpSv/+f0zUtzHBz+++ftU/wkVGFw0AVn1k3yu6bfUzqOj1mhkrjep2mcaZLjD5nAGQ/2KvHL+v0xaOa1A8oFrqiztftOqbcBHQQANg8b27RhdPt2+jBN+hB3cLZhbbY7APgCotIJoEJdSV2vEAEDUnRVoXiB5OE/WFsxPtTxy/8bKTMKA0kAC4/Nf2vbSL+Et6UG/V4z4EngQGUs4BIEomyIxKhLox8EgAkMpyLZOXo2kC9WlMhj6xe8uKgTMRAwWAFR/e9wo90h/SvXqNHtBhl8EoS7LADNGZExgcAEDSAOU5yuYj0qfQTOAEUfer+uIze7aO/hkMCA0EAJb94sGbknbnl/Qo3tpzuoA5W1AJgCqGiOneIan9UHNgCLLYtQgAsKOd6mf5hvYV7tq1cc3fwwWmCwqApf/9wMr2NH0SCP61mZF7TAeQtIAv1TLDeL2Ksh4oMAqoegA490NwAMUPA4qujjPcp32Fj+zctHY3XCC6IABYfvfO5ens4rcqSH9dS8KyQHUK0oPOoKI78BJTUT530pKwLkZB0ocGaAaA4iA6ra8/tHPDmt/J5pVwnum8A2DZnXt/UEHyKT1ILw7VPOuVJ02B3S+CMy4DfGY4Egwhs2xbSdM2UAadAZNsuqAaAAWWSfP+e9Oq9dZ961Y+DeeRzhsALnvvsWWdkem79Xj8Z0Cj8PkA+lIvaYE6APgM95keY24imwGxnDQb8cvGAMCfB8AFBGQogA6kdM+uDaP/Dc4TnRcAXPbe8XXdIfVH+vTlkn2PMVxSo71Bx7iD1+C6yg/g1+EsAqsBF9FYDrO5pkMJFPS1s9OTtx/etOkQLDApWGBa/r79P9ZN8G+17/tyMBaOst+6I5XTq+pS07LQ4P68LPTOKWs01hZ4dby8xtJG+LolI4u/s+ngwdfCAtOCAmDZu/fflQJ9QT/QmD9o1AfTA0ZUMLgvEECDchXMJQARJFLZsGINEaynFB7YtO/gf4AFpAUxAaPvpMVnW/vu00uxt+k7oG/XHZUP4KrpWDnRDEDgmEVVvKCqxWlc5Jf7G5IZ4I6k5LP4pkx0CoGlQ/nb1cf/2DC6+u6HEDswzzTvAFj+fj3Fmx7+sm75laLts3eMDYwEAiWUCQY6BIJk8ys99xgIHOZiCCLhPACqD2qf+XEA2LQvz87Ovn18bOx5mEeaVxMwcsf+Dd3J4b/T6vSVjdRpxBwQV8kAsopOobn69k1C2muDmtbxTQenyHM4/YdmbURJB8vardYfbHzuudUwjzRvAFiumd9G+Ibu6PaYLabI4EBNnlSGPCCUzpkPjFQuH2VwrH8AUAtocPlIEOb7z0USKGJgQHwdLF789fkEwbwAIJd8oAf16aa6gSXywBCT5FQol4LHdL8OOfVjEk4RgFYCgrVVVzbQGBYcNs8j4idCPi+orcE1uGjRn287cmQtzAPNGQCLfubYuhbBHxPgxiZeecAIAOehnQGi/g6az/K8L04eNQMMfwbvGYP0KqZLdRFv6KTp/VcSjcAcaU4AWP22g0uSzsyf6w6+pLFEeFJVShaTcPKlnWuJNCK9aXju3Me266dJ5WNp5bPIIKh8dvDSAJoB36cSnPQ6dejQ/a9+8MEWzIHmBIAJTO/VPbkWvIcUBwI8BlSBoirf3iNl7XFQxBy7VG4nAJoHPqrxGWLgpnNwUuv8AQzT3rJn27ZfhTnQOQNgyU8d/Hndj3fUPozHAIoxQigbOHASI+sihmnIMPIYLdp2T+tAUIegCVNjAAaJ2Zz8fLmsXlVR/2XjoUM/BedI5xQHWHrbvjfrn89pW9QKAhsA8XluLC7gz/8xEkTxygdzbCG4wzd0RjeD2ECOHz9I3POqeT8mGF8fiMUuziE2gBLHEE7qaNutz65e/bfQJ/UNgEW3ja9TpB7WNV+AAsP7AoHA0CgIhOCKCI4YCHj0LsiD6k0iiZCXQCRyiCLAgn75zx8TEgAZACHnnpw+0XnlvqvWHYM+qG8ToLrqS1odvaC0wzH1XWUOpLKSDRbaoxRqTQEJdSibIgpTQ4r0MTAHvsnwHVUS8nwfosq0CeYhmIUAyDOTgrYPr2j91uYdO4ahD+rLg1zy5r0f0T34gRx+2Y3NBhbbB2R/fSIhJ09LoScVnLLBU72CvFw+qMqk+demC879FPQGvqJv7nO4bRCax01ZHhb3RtOHsqxi/eV1VHGOykuz900hGCR7H965DASiKSC4LV22/Bl9djc0JGxacPGbd70U0/Z3dQ2sUvdgO5z9iak1Kc1X6QCh+m9gEqptMIYqPsvzN4T0YwpYur8mIbbDfQAF9T4Av4Zw3EsG9k5OdJFeuntNs/cYm5mAG6iNndbnNcIwUNeCaiffS46pbV9tcpUtqHfRg0/d+uRP4Zx2KTQ1qVc2du7fm+WRUE9qx7nmzxYZF6gxEUDgWYG8wHLtmX/s8iefvAQaUCMALNm4/4NEuJUqOhLtZOqmkTBQMYDEpn/kg6YJCDwmSsGmKAgo0l+BiX5AyA9n9x08ApDHG8I69khT+tFLll76I9CAak3AyJsOXKmy16aRWmUNq3oEdc7L5KcRU+CoeqsWAQIV708PHZWpILJ9PFSxvWlexVaymOpOGpqCMg0rTYnYP98MCs9bjjkI481/i/MDp1vJtqMrV56GCqrVAAjdX9WwavmIr422lRJBENMWvofsSzWX4tiiEKXxOpXquNu8bLYlw0nrQtSM0DmaABI0SUy7RNceXF6MLpnp/hrU8reChn/00C0Jdr5GyMDnI9CiladlxKXbaIHKoJB/KIgGS4L4gS/5rI4o3Yj1khlxDJ3yVc5jriWw11ceN/A1j6TppOf0x1jSDMDyACZn2+q68VWrdkCEKjWAou49ZB0/I8hQZbM4GtNIuRSiGkHSMlKdYG6dunXEezqSmBlKEq4J+L4Cikg5RI5Ag1mn0+uDOB6R8XI0QUTixWXmIm9Reyb9r1BBUQ2w6A15uPcPRVsDEA1XitrA8wOwiRaQbL1kFwUt4EhPhcQFkh1Lj0q4d20k3b515LbRQOtwyS+vUfYNJL6ge2loNk3S63avXfsECBQNBGkAvQPNSdkqO8+DERDmEXgdQJZKABX3CxLKdlJw1WDaO89bthKbDabJswGasqzVHGDSrVSx+/fezCKmbahkCDHmle0bxttgVL6FM+ndNw8SJS5TKUGACHDztnV+/hxmfaEYHOyNO+cFH2fOqx61k1S9U/9+AARCKXHRG8ZfrkNfD+nTIacUejWjEs/yvetePRTTKzUD1wSehFe+RiZIPk/PGY8ZowtUUMY9zYE8PUur0gS+NPvnzPbzWYIU1Or5FkprFHO0VH5daBCUx0XgjSuEdGQW2i8ZH7t8H3gka4COepd+8CELPPBR50u8hEiTSbJa0llk0kQMymSkPK9lJY5pA4hIv5V8sBoCWF0yTNYXabcL2QfDsje0luqI+iXDVlIpvuIomZekVyZY3CoevhAAqy7L8S3SOlrrHO90oJMBgLIF1+xInKGqHHfwNDHhyhZ23q3PPuSXC0f/Rw6sHKH0IKAZVoYw9GsxBKKExpg0A/SvDVREG/i2k59HJMydMWSjn+ltfaSz+nIW7viB5fCum1bC6qWtngabK/XRxowG5T+cmoD3PbUfjqsEcLgFqt3uaQJpXIX7OH0n2LPz6PhmuPHGWX6vQAOMzHbfp22P4ui0v2RBEBh6KGcIWPGgpf0K6hYNVo5Rau7P6juLLlby+TmBO30C8xzKPFL+PEbtpx1ddAbues1KeP+rVsGFpOzTKK9esRR+YcPlcOdzh3V/NTusFsqICUGpZTHEWLmAVdD6jSvXv2knwBd5GQVBJXyjPx3h0w5xOdKbukhTOnH656RRGSxxtnt55YIASSrcQwquiEcx7aOuvtAq99pVQ/C2Gy6DQaGrloxoH15rJm2a8j5GxlwcewBniljkpbf793AAMPKafa/ViHkxb6D85UyIMIfnU1OAOIyksB2BwcF6vt83HhdIIVgPAJ7WzQDQzY/r1ozACxbPaY/lvNKR6dkiRtE1R0oVAlRzQD5Wr9707MFr+D0cABCm/x6sVqy5CZHMnGigg7VXSjlBfHFIkvqIREuhXD9c22M4uBogl35tAvRx9egiGCR69OQEWM2Y/wYCA5XCIRxLtMa7nd/DgTuSemNxYhoz58GHS6xv0PtxHURWnqCZ/9Nrh9Xw5/t+JWb/g1gBgDgbcDZjZOVTfZGBQP9uXdnXZpoFp2dPT+cPno9/PkPA0gSXvpi94DxCCBlj+aXgX/F7lMPVfu34TbrQKknqqB5ZlWXEkK6PYqcsiRIfLLRIWsU49GU5YSGHq//CBOg5v/69Zu3gaICOHoOdZ2f0WFjGo/tcBKFpY2YwGkJO8SXrnz64yd6nBIDqqp9oyuwq29MECNRIrRNI5iW6Vy+NDI4HEH5d/Ka5KVi5NIHlA2T/T2rn78DkDBSBDz1DstLv8aB2vEHIU1B+eKIEAGb7fmoYH2wI8X+rgJDGOx99AP9dPx8ssfoxKeFAyO0/lQtB21cNz8t0f77o6HQHDk9mnwPIGK/ABol8geDPKvpiqSd4+lql8Hp7nwIANx9cpQfiZVADAOdGpjGKMNdnQiCVXodjWiFjkD1CJgJI5gGiTAfHFIDxsDMgXL9uCQwSPXZ8wmisYpKfx01TDKfKknYUQOCMdRdeNvrUqcuz++Q6b7jVvV5ntHyHIXAuvGx+7QSIygTvHMF1YgSKBYvyjiP1FnIU9gI/5iAeGfMdwxR6u3JNBJCK/VO5E/iitXN+z3Je6elTk+A8mDEBfgg8H97ULYpeAC8ghNVDndNj+uxorgF0mR8O1DSAK/0QSWMqv4kGcex3JL/KfPTseKG6A63CVF2gaQJtUUh/tl6zaeVgAeCpE1O545ep/1z6BQ0sOsQxzeyPp2rfnN0n1wCYZv9xow8LGEOWT75G8LRC/sPzvLJ+6LdslpiUgwkkp2GMnMC9LkPDmfSDkX59LB1BGLtsCAaFssWgJzQA0KykoWFqOXxMo/LQd3lt8vJxYWPnsCx/v8PGAbQJtJVE1eHPK33m+/Viv3XEzUU8yc20YMqXbs2A+fk8lmDVJfQcwBWLElh56QBFAKc6cHI6c1LMJgRSRrNRAfQ+TADFBpDg2uynNfzqw5thduYykxhSU+YBxH0B3gnvvFxgkhpjix0oSHfwJk1aqAZi2gS5L4DMTwAqzcjV6xZDS/WhAReYxvX8/8yM7vRQUmiBXG0X/SOuyTLCcHzKJXimDUrqpa0Z/e6+xS2cml6nV5viITByKhUMMefSp43jgPP64INCCY2Q0K5nIkr1Xp4b+2Cl3qpG5dW1JkD7ANvXDlYEcOepqSw6DSq3/4qBAIq+830N1gSAMD7eOCLnH6XL22lnYytN1WiAEOqd8muf39wqOOkIjbVG2U4K4VIyl3D2YNE2ymwqJMbXGNw2UOEAImUAWAyDRE88P2kCPyYAlJotYlZLpUxzcpvPfR2mBct8hyc4Qmrkiha08OpcFQpE9k9k0MtyBOHUI6YGsCLbY7KEaGlDKfjqL9cIprCREDtopbZICw2AWkVcu2GwFoG+f3SyZH45BczfYyBn00sBajagVuvxMWQA8Ug3hWMtHXS+0pGsmINn81A4ByZhTZy/inZ4mXL7V6SqSKVTCAA2bsB8gFxdUmpmAASrL23D5ZcMjgOYdf/p56fMw6tCk9n+29mAA2rqvYwL4I49MwMoDFyK6UYd/MFF4aZykAERYygripUJjbJYD0HUGKVm8uy6OGU03nKZnBZTg3y3rz6uuKwNK5YmMCh0XM8AjkzMAg4NF5E/Qtc8oqf+OdgBXdXPzkkAgTYrq1oE6WVoR7mKaphvqRFjbVlWuBI4fCbAH8yfBfj1Y+o/K2CWgDetGoJkgGYAjx2dKFcA83+rYMLBmQnoxTGYxuXmj5g28PLsGHL/Tp+uaWmULXd64Eu+ueZKAiX1LXnslaAhqHIwY3z123RwYqd9nGzolBcCYwK0etg6YCHgHcenDafYDMDO+QmcUDCpcJyo/ItlAvpawZ4SLm9pVG3NUyRGRhjoWwWJihvwApbTZHploUi9Mp6ay0/RTQffwfHSQAqKcB9AjyCRXmVLu7kvcP3GwZkBzGiT9I3dpyDbV46G+aUJMGUsT+y0loTt52BMgpVUZ9MoY5g2hFdpEwDDJT+Q3aQo4ZKgHco3hASw9IoRyDs0qddZAJeRlokA0ZdPyGe+YlqCHU6QpNgJok9mcw2wfWwwNECqx+d3/+EQ/PX4Gd3Pdi79GXdtiJsoNAEOU8WDMYe8Ovk5Xpq99p17QKVjxX8RGlHU7uegoGLQy/esO/nvEj3zumQxBkx1Hoi34z8oCMBAuWxZThXaB40IDbUUjLQVHDo+G94LoDqtYXmsqZv1aP/pGfjC48fg/kePASXZewAZS5LC9rMVT/LbZFvhyWiEIOBlgFA6iMTllNqtJo5drzxAtc6XEs3cJWM8zcD6UQUfv3Md3HLTUmglDRG2QPSdJ8/AC2//HpTOYgkeBP4KWT52/ncEEZyXT2o/GpG/LYTB20R5WjYOGePb2aQsm5IWbwMVHDYOoPVjrNoHKDUC9/BLdnpCQUbbos00Wje7Sweo4dfCDIJK8syG+F/v7EY9rXK3blDwJx/bCJvWDcbK2+O77Jo757S9ZB9F8O0sgKtdWHVeLvy+AQZlinf+ivcAQWk2JK38N3AATejXWd1DpuEtqLoAojmw5fJfsrya1HfCLOqwFCSqigGgV8bzMO0Z5ovz3fzNm/e8Zc3AMD+jR56bKMRJJYUIqwSc6CH/B5VlGgTSH/0HlNGvi7LP1GRvAucaQBVaQLFXiS0AAHpjy9cBrAmw2sFOd/OCIIPW/JrsExoAlH1X7npxhBjamsQJgnhS7vsZDaC97lte1ujDVeeNntyTaYBsRNv5gZkElgBAkF4E5V/8wJi6Z8x3zm29/BdLMJQgyHY5ZZqHighgziS26mfJeUXPTA8DzcL9Bh8IYPPw8UzXTFXadXArBXkoXNsOWc9fg6DdSrX9HxzpPzOZwoFjs7kPnL15izkIDFctAHw16oHBSfe1hHK1hPNCalkWWV3LMex5/OTN/c1wBu842DzluWK+ViZw31AGOp55G3v1xU3BCEmg6MdhtL03uxe3bRiGVgthUOjkmS6MH868f814sJOhzBUybpIRM+KDFqES76acp4XFss55PkwYyFdZjmWU0VAj+Y7Xbx3BiDbw29IAPKAjgTQhPmEVs5uahhzFlEfdrtkyWLtun9k3BWenqPgAg/Wg+OgQmy+x9fjcBnPpBCgnO+XaBZ+6GWkOpNq2B6YNNK9+5auAUE6e8iwr6aYrwbcPfGYbs0EA8pY7KPt5QAeC1H40xruJgEc/SuDDmkl/dn7dtsFacn1851nIlSAUMXe74wbYdKsXvKRSI4DHzJIxEDLGiVPEQMDVPLs12r4YkIB1/qwDx9tW4FpktkZShou5SjH9SaG7u6VjI4+zZ42TbTDCfHkK2DMDV79wsADw2M4pAPbalSPJ9pzbV+/abtIsp7/SwTUCazZ2DlBA0v4tAzvmnFSP8c6KoG1IgbtLytbjGgLKfFKE49o1o2b/oLjGISSeRibaZtT/4hGEdWsGxwHM+vr4c2YGYEeNSXhZyF6T0QJW/ErNUEilBYEjyciu0ZP4NO4n9NiP7vzfagPOePvLpoEg2H3bV3ttQHRGm/8jrQ61Hm1BJ9OH9Ua6yvZLpsFogGVLFay/YnAAcPpMB3YdnIGCMywEyPpfMsymV51z9a169r0cE24mshNuIsg9t3JTLgAx+18yW5oJYHgEm2lNBwow0OnW2nS3gsdWH9KJJ4GTr8rq0qU89k7Ylg0jOu4/OJsu9hyagVNnC3Gy374nQlmNB88k55F0bpdAUvnaLo8Er7aZ19Zi+f65dPj5btvZLz49fvPYZKE8KN0RZSxAH0znecVIZAtBm9cP1q7b/UdnYWrayqMRFeHZyGcyAPgagOpAwCS6Dix8kVQEE2uHv+lcB0joySL3zR/NHqEAQIp/AwDVDGUDEz2Ad8B+dpXgmq2DNgOYhHLqB54JqDmoSX4KzRjtMdMyx5fWOukHScJNADaWl6b4cPbIZhGIvuVGCBhJIIgU7ZXlUCN48YAB4JFn9BpAKfnoSLjjYBG4Xr4/K2B2OyYI3D/Iyc4iiLULrJy17bYsS4/ZfjJ9Lu29SXdiEtDrr36mTkLT386ScgB0WviY9gb1ei02WxWkSDp3bQ28sxnA5vWDte3q0WcngfLZP/eY2I99Pjtg4DqEAfM4822wiM3FuRaIzfd5cKjn/VOx7duWleIMPI1A3DbvvCmVzyJo58hs+py5taZH1o3rzO81UYH15sGOQqEB1un4//Jlg+MAdroET+0uYgBkIygN1DzFVLrHfN8U+G85k3Qvrt55O3l5Aoj8qxtel5uL2m8ldPH7u35641Q2HoqNzZedkWrC9Mq8oocbRluwZJGCQaGM+bP5BiBlQCDPAPLH4c9lfqV/gEExQETA4Z8H/gEJv2ReZuXlKhguzQhsXUXJV+x4lJzBVP1FpXQDGwiqyiPgL6xfpSOAdYsp55P+qXQAWegswvAYUy0IiIOD4owkieG8jAcmn/mu908yw32wefdnbKGzOPOndjxKAMw+NfqP+sHGndHyES7kiaChXqXrtw/We3cPPzWRSz2SNwWMMRwAqiQ7+DgDa4d8EKRCPSaxDsM8oNhtFcU1ifN7+wW0ACC8nS588/Ttvf8uynRztpmI/sJ5EAj5Wq0dqCdKZiS2bhqwL2/syUwfk35LzoP2rskHgvkltloY8xnCAYy0XQWwKsBJWoRpDR9kRoP8CeuR4wOAUvgpLR0dkcfEniHWYV4gi53jYMUAjp7UIeD9WQjYfHbF0wCSk+Y+V80hlHOk2GecbxpSgWmCHbdfOXPa99sVfQQ8mAypv+Rj4gBg9ol139YIeaT24SACbGIAgez7BggjI4PjADyi1f+ze6fB2baTkf8w3nlMC0RDwwCNgSLafO867ihSYQ542QpNoo8HD/30mkeBUeieK/ok1JHbqEDFeta09rb/+rtnYRDo9Nku/NzH98LEVGb/e/vumzLKOXcOEvOwghHSLMBhMMmaQPQhDBDqpn959K/b/bQ/LqF4bnr+0qQ1uUc3vAyqSBRs26PsrRstaekMrFlJ8LFfHoOrtyzKdj83o36VRkX57EsbDz85Ab/52QN6CXhGB1Oyf7yg1yaSIbMJVJUuAQ+euP+4CYT9fO51sOffizQ7G0bttdeek17VB17enttxEF4MNZ/Fe/roe0e3A7o7N8ShS7bsy5DydmhCQQvWJc1AMJMfqP3LxXrMMSG5Lla0yc5jr4gF7XhpE/nr9ipnfs707Is4+f77pFgN9AbZGdgIc+Qdw25bKAFAAoYHQIyAzCkjXZcHumOh8rn/e458YNXvgEdi6Lc7O/MbSXv4x/Ww1e/j5jxFdses6fLfy3fh7FQXkOtTziSInFelYf158a9oMGcM5jt+s8fNNoGafdp8EwgKz2PTzW8eamV55XWZz77iBdAL/Sr3vFTtymsDvD4YF8P5OKT1XXm42bTTA4D70Qjs0mFS+CUQSI7979r4JGzZ92f67j8BdYR+xw0c8/+X1obe67nZf+MqAIASM6X2/DLIkhDqQZAx3/bFvKlBaKTeTAHLt2095eQsAgH0GAaMYSk4m0SLZCq/71cy0PcjPCA4XzD1F3uUe/+SUui9DZRCMG5FP01vEe87+oGVB0Cg6OJPt9v9X4lSb4G6OJ59MAcIdtDRG+HiyLtVBQIpnf2W9QOG+9dGwm1fzAgXm7t6THKkm2sCK/0QOcfykZw8Z/sYmLY5GMCrY8thL59rBgd0PlkQgtdvM/SI6fM67f9ChCqZm2w+8If6Dm+GfsmRaOFJWRmMSb8EABTqSiAwRw9kyA4vnzlQwXt+gtMl2WbxP5Tl6Rj3H2xZwRnEmvuLffLzetf/59jPrf1PEKHK5V/V7f56muCPO+XkeZ/LScem2p7I1colTFsPIJQw9Nq1UguRZrmtRjfdKWYljisDrw/lZUXfiDzJ5vfzbT2Aaxq4OQBwJZqfk9ts3hXmA5R9cQFyXC9/3gMVVDkxm9257vu6yc8Xd6U486V8vygBiI4OgLzOAJE0rw2iins0Sfd+ya/DVHJQp+oQyopLwfYQYgPOuRDyJakub7cLHz5211jw30I51c7Mu50l79OtHYOmxIFQx4zYOS8r/XrnJICLpDpSd6oY692LYkxucvAYvccsSuPlHab6AaE0bA965R7rdLq/DzVUCwDYteKEbvSD0C/5EbIg3z0nP72KgVRxHssDoRwfXJ5dxVwAkEAr1ald7EmhnukeeCrbSMv+TKXU/eVTd489DzVUDwDIdhAvekC3/JCTSA0q+ibBr9OEWX4+yWmSKYgyMta+x9DoGgAI9wMQNQRRBTgAmkl+JM+T+PJcT0O/fOJXxv4YGlCz4OwzLzjVVcm79A0myk77nY+RZPgivkJZinq/wf14Qf9WEKbbtoKCVaAgoU3bfQCZOeJBYdmMGLP8haA66abI2gA7js7CqXdBQ2oGgIx2rH1CR5M+Hs2vBIGUVlWhj2IUOffSqAasVb4AgZfmtc3zyiKxshSp693Tl5k6rVD4EjRDhHeevnvbUWhIzQGQ3Wsq/bCeZzwQLVAHgn6Y6VeNMUiq6zObhCoULyd3AEQGUC3TqLaslWx/FxDXErUgKMrfd+Ke0U9DH9QXAGB8bLKLkz+r41xPw7kSzUMaNShbdX+qSBeAhjXlYlNRcfrmMT3WHsXK880eLA9T+rvh2eQXoE/qDwAZPbv5sFLpz+pbnhbzmzCDKgpHmBML+NSCQZJ0KU9qIwY4m8TSo5rAa8fXZIGjWSXlwn0MKA4rGL7j0P9c0/fmi/4BoKnzzNhDoNIPAuW70UOSJIJqyknp5heFNJE5EUmsvIchjDAbpfZSkJkGkV9mBkBgeCnxgraI2Xt2fRa7nX977COXPwHnQOcEgIy6z4x9Use3PxYd5SrJipUXruUPTzRMk/ri90s6l5gllKEq5gcHQXQLmWGmLVYJAp6WfeMR1fuP37v+W3COdM4AyO8+dsXP68DzH8GcKM45bFqUpVMTBkv1Sc4nKZ+DgBWMOnm8DlB1GWoMgmx9/aMnf3P1p2AONCcAwEPY6U7T2/XZX/ZdNxgYCBhAMEci+dphKghpMQbxNj2mxZgpqGwo3pyGQAM4e/6osj+ERPednNj/KxEd2ZgQ5oO2HVmrZmf+FIFuaFTeW9YtzlHMF1f1qpaBeVNV5fiqWUaK1a1aZlWsvJAXvJwpLQejuRFrL8gX2sLevf/fqTOjb4TP4BTMkeamASw9tXJ/StOv11B8pK96NMd8bFC+qXxUaIeoBhCknio0QZ2mCDSCbwbS/F8EfelUu/um+WB+RvMDgIx2bjqU4tQtuptfrSw3PzonpBjD/DKSao61xfLF6ZpUloT7AVSvJAp9E/qaaubfm6qT74R7xyZhnmj+2bF+94qknXxSd/rHxPYxcs71vVAGq0yAl9ZsuxiEZoCnNdhtE6hnFNqAsK1e+yi3HZqFDiTw0dOjo3fB3fwLA3OnBZLHB1utTZvv0hHDXwS+mwilu8u2P5aGUjnJBxCAAb7NtwPP8tDLD7aMNQRALI2Doty5KwGgTCMd3FHvOX3/mt+DOTp8Ei0QAApKNu79SUL83/oml4h38vebNgCAU62GyU00AQrp/ey5CxgO4DGweVsYfqB6T4LqjpOfXtP/LKshzZ8PIFB359jn0kS9QRuw52CeKIpYkpPORWQoltDEoWPlxDUCyU8QymhZ/66C1msXkvkZLSgActqx9pvdYfxB/VQPNGbHvCu6eNv5eAtMiUX5qKJdktqvYnwArKwR7Git+RtLR0Z/6NRnVu2ABaYFNQE+tV64707tzH5Y37b4cGDMBPBzyQeI5UdUPkrlMpLsOysTdQZtmaqYAFak+yajLIu7EfGjZ7as+d35dvZidF4BkNP2A1cmnfT3NOJfAQhJtEfCL9aVi5z3FRgCIY0zFCDKUM50KSCUNy/5CZnsJ/iJVqf7oVNfrN/HN590/gFgKNm89w49S7gHAddArEc+I6VyDYCAkfQmAIhKL8bbiWkQoU72H2KeJpV+YNWhzoO7Hto4L8GdfuiCASCnrXuvaKX4bm35PgiZWfB70wQAQrkoAHh+jKF+GYBwjm/L+CocoJLh6GgQep70auqqpVd8dNc8RfXOhS4sACxt2X9VAuk9Wh5u1T3qfVSIM0ECR+z3XADg11XevRmTm0h/yXAO4OLl0rOg1G8jTv/WxJeuFF/YPJ80GAAw1Nq+7+a0i2/Tw/RW3bFlUQBI5xEARGMGvvQLdaOSXMf8jPzFHMDdWtXfBx31B1NfWbsbBoQGCgAlbdq7pdWCnyRQ/1H3cFWWtKAA8NO9a6wDTIUG0Gb+UUK4v92h3z/1tfPr4DWhwQSApRc9PpR0l/077TLepm3mKwDMZ2vmEQAiMwGizHfajTE+s++ovpIgffbsV9d+HRYghDtfNNgAYNTetv9G7TK/XscX36hH+CVlRgWDsSZ/LgBw6ha+gVZY+C3985COdXxu+utjCx7EmQ+6aADg0AufGVNDi/+NArpFe9LX6pDJFZoJBRuaAsCcO8GYKhAFQKCs7h4g9Tc6mvEV1Zr5q4mvXXinrl+6OAHAaXTf4vaKma0AQ69KFV2NhNeTwisB00s0bxeXS66WqlQ5z7dHtvdOwaQ+PUEJ7UFQj2q7/vctlXynBcPjJx5acQIuYrr4ASDRtiOXDA1NXZkquFwHG9ehoo0EtIZIB52QLtOrblfr2Nulmvtts+Q6qxl7PIX0n3Qo9oS24Yd0ZO6grrOLMNmdYHff9PjsHti18YLN1xeK/hm9zK/tydEQ8AAAAABJRU5ErkJggg==", riseWallet),
        createStandardAptosWallet("OKX Wallet", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PHBhdGggZmlsbD0iIzAwMCIgZD0iTTAgMGgyOHYyOEgweiIvPjxwYXRoIGZpbGw9IiNmZmYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgZD0iTTEwLjgxOSA1LjU1Nkg1LjkzYS4zNzYuMzc2IDAgMCAwLS4zNzUuMzc1djQuODg4YzAgLjIwNy4xNjguMzc1LjM3NS4zNzVoNC44ODhhLjM3Ni4zNzYgMCAwIDAgLjM3NS0uMzc2VjUuOTMyYS4zNzYuMzc2IDAgMCAwLS4zNzYtLjM3NVptNS42NCA1LjYzOGgtNC44ODZhLjM3Ni4zNzYgMCAwIDAtLjM3Ni4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc2LjM3Nmg0Ljg4N2EuMzc2LjM3NiAwIDAgMCAuMzc2LS4zNzVWMTEuNTdhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc3Wm0uNzUtNS42MzhoNC44ODdjLjIwOCAwIC4zNzYuMTY4LjM3Ni4zNzV2NC44ODhhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzVIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjUuOTMzYzAtLjIwOC4xNjktLjM3Ni4zNzYtLjM3NlptLTYuMzkgMTEuMjc3SDUuOTNhLjM3Ni4zNzYgMCAwIDAtLjM3NS4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc1LjM3Nmg0Ljg4OGEuMzc2LjM3NiAwIDAgMCAuMzc1LS4zNzZWMTcuMjFhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc2Wm02LjM5IDBoNC44ODdjLjIwOCAwIC4zNzYuMTY5LjM3Ni4zNzZ2NC44ODdhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzZIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjE3LjIxYzAtLjIwNy4xNjktLjM3Ni4zNzYtLjM3NloiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==", petraWallet),
        createStandardAptosWallet("Bitget Wallet", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgdmlld0JveD0iMCAwIDUxMiA1MTIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI1MTIiIGhlaWdodD0iNTEyIiBmaWxsPSIjMDAxRjI5Ii8+CjxwYXRoIGQ9Ik0yMTkuOTQ4IDk1LjcwMjJDMjAxLjYyMyA5NS42OTI5IDE4My4zMyA5NS42ODM1IDE2NC45NDEgOTUuNzExNkMxNTMuODIyIDk1LjcxMTYgMTQ5LjY1MSAxMDkuNjcxIDE1Ny45MjEgMTE3LjkzOUwyODMuMDk4IDI0My4xMTdDMjg3LjAwNCAyNDYuNjkgMjg5LjQ0MSAyNTAuNTc0IDI4OS41MyAyNTUuNjkzQzI4OS40NDEgMjYwLjgxMiAyODcuMDA0IDI2NC42OTYgMjgzLjA5OCAyNjguMjY5TDE1Ny45MjEgMzkzLjQ0NkMxNDkuNjUxIDQwMS43MTUgMTUzLjgyMiA0MTUuNjc0IDE2NC45NDEgNDE1LjY3NEMxODMuMzMgNDE1LjcwMiAyMDEuNjIzIDQxNS42OTMgMjE5Ljk0OCA0MTUuNjgzQzIyOS4xMjIgNDE1LjY3OSAyMzguMzA1IDQxNS42NzQgMjQ3LjUxMSA0MTUuNjc0QzI1OS41NTUgNDE1LjY3NCAyNjYuNzIgNDA5LjI0IDI3My4xNTQgNDAyLjgwNUwzODYuMDQ3IDI4OS45MTJDMzk1LjA1NyAyODAuOTAyIDQwMy4xMTkgMjY4LjkzOSA0MDMuMDA5IDI1NS42OTNDNDAzLjExOSAyNDIuNDQ3IDM5NS4wNTcgMjMwLjQ4NCAzODYuMDQ3IDIyMS40NzRMMjczLjE1NCAxMDguNThDMjY2LjcyIDEwMi4xNDYgMjU5LjU1NSA5NS43MTE2IDI0Ny41MTEgOTUuNzExNkMyMzguMzA1IDk1LjcxMTYgMjI5LjEyMiA5NS43MDY5IDIxOS45NDggOTUuNzAyMloiIGZpbGw9IiMwMEYwRkYiLz4KPC9zdmc+Cg==", petraWallet),
        createStandardAptosWallet("Nightly", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iOTYiIGhlaWdodD0iOTYiIHZpZXdCb3g9IjAgMCA5NiA5NiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQ4IDk2Qzc0LjUwOTcgOTYgOTYgNzQuNTA5NyA5NiA0OEM5NiAyMS40OTAzIDc0LjUwOTcgMCA0OCAwQzIxLjQ5MDMgMCAwIDIxLjQ5MDMgMCA0OEMwIDc0LjUwOTcgMjEuNDkwMyA5NiA0OCA5NloiIGZpbGw9IiM2RDczRjgiLz4KPHBhdGggZD0iTTQ4IDg1LjYzNTZDNDggODUuNjM1NiA1Mi40NTMzIDg1LjYzNTYgNTUuNDQgODIuNTg2N0M1OC45MTU1IDc5LjI4MDEgNTcuMzUxMSA3NS40MzEyIDYyLjI3NTUgNzEuNDMxMkM2Ni45ODY2IDY3LjY0NDUgNzIuOTI0NCA3MC4zMzc5IDcyLjkyNDQgNzAuMzM3OUM3Ny4wMjIyIDYyLjEyNDUgNzQuNzkxMSA1Mi41NjkgNzQuNzkxMSA1Mi41NjlDODEuNzY4OCAzNC4yNTc5IDc1Ljk2NDQgMjEuMTU1NyA3NC40NDQ0IDE3LjM2MDFDNjkuNDQ4OCAyNC4zMzc5IDYzLjE5MTEgMjkuMTczNCA1NS43OTU1IDMyLjQwOUM1My4yMjY2IDMxLjcwNjggNTAuNTk1NSAzMS4zMzM0IDQ4IDMxLjM2MDFDNDUuNDEzMyAzMS4zMzM0IDQyLjc3MzMgMzEuNzA2OCA0MC4yMDQ0IDMyLjQwOUMzMi44MTc3IDI5LjE2NDUgMjYuNTUxMSAyNC4zMzc5IDIxLjU1NTUgMTcuMzYwMUMyMC4wMzU1IDIxLjE1NTcgMTQuMjMxMSAzNC4yNTc5IDIxLjIwODkgNTIuNTY5QzIxLjIwODkgNTIuNTY5IDE4Ljk3NzggNjIuMTI0NSAyMy4wNzU1IDcwLjMzNzlDMjMuMDc1NSA3MC4zMzc5IDI5LjAxMzMgNjcuNjQ0NSAzMy43MjQ0IDcxLjQzMTJDMzguNjU3NyA3NS40MzEyIDM3LjA4NDQgNzkuMjgwMSA0MC41NiA4Mi41ODY3QzQzLjU0NjYgODUuNjM1NiA0OCA4NS42MzU2IDQ4IDg1LjYzNTZaIiBmaWxsPSJ3aGl0ZSIvPgo8cGF0aCBkPSJNNDIuNDc5OSA2NS4yOThDNDIuMjkzMyA1OS4yMTggMzYuMzAyMSA1Ny4yNjI0IDMyLjIxMzMgNTkuODIyNEMzMi4yMTMzIDU5LjgyMjQgMzIuODUzMyA2Mi40MzU4IDM1LjgzOTkgNjMuNzUxM0MzOC4yNzU1IDY0LjgyNjkgMzkuMzI0NCA2My4zODY5IDQyLjQ3OTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNMjIuNDk3NyAyMy4wOTM1QzIwLjA4ODggMzEuNTQ2OCAyMS4xMjg4IDQyLjI0MDIgMjQuOTMzMyA1MC4wMjY5QzI4LjgyNjYgNDcuMjcxMyAzMi45MTU1IDQzLjAxMzUgMzUuMDkzMyAzOC41MDY5QzI5Ljk2NDQgMzQuNzExMyAyNS42NjIyIDMxLjEwMjQgMjIuNDk3NyAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNTMuNTE5OSA2NS4yOThDNTMuNzA2NiA1OS4yMTggNTkuNjk3NyA1Ny4yNjI0IDYzLjc4NjYgNTkuODIyNEM2My43ODY2IDU5LjgyMjQgNjMuMTQ2NiA2Mi40MzU4IDYwLjE1OTkgNjMuNzUxM0M1Ny43MjQzIDY0LjgyNjkgNTYuNjc1NSA2My4zODY5IDUzLjUxOTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNzMuNTAyMiAyMy4wOTM1Qzc1LjkxMTEgMzEuNTQ2OCA3NC44NzExIDQyLjI0MDIgNzEuMDY2NiA1MC4wMjY5QzY3LjE3MzMgNDcuMjcxMyA2My4wODQ0IDQzLjAxMzUgNjAuOTA2NiAzOC41MDY5QzY2LjAzNTUgMzQuNzExMyA3MC4zMzc3IDMxLjEwMjQgNzMuNTAyMiAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNDcuOTk5OSA4NS4zMDY5QzUwLjE0MDQgODUuMzA2OSA1MS44NzU1IDgzLjc3ODcgNTEuODc1NSA4MS44OTM2QzUxLjg3NTUgODAuMDA4NCA1MC4xNDA0IDc4LjQ4MDIgNDcuOTk5OSA3OC40ODAyQzQ1Ljg1OTUgNzguNDgwMiA0NC4xMjQ0IDgwLjAwODQgNDQuMTI0NCA4MS44OTM2QzQ0LjEyNDQgODMuNzc4NyA0NS44NTk1IDg1LjMwNjkgNDcuOTk5OSA4NS4zMDY5WiIgZmlsbD0iIzdCODFGOSIvPgo8L3N2Zz4K", petraWallet),
        createStandardAptosWallet("Backpack", "data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNDAgMjQwIj4KICA8cmVjdCB3aWR0aD0iMjQwIiBoZWlnaHQ9IjI0MCIgc3R5bGU9ImZpbGw6IHJnYigyNTUsIDI1NSwgMjU1KTsiLz4KICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xMjguOTA1IDYyLjc1YzQuOTY5IDAgOS42MzEuNjY3IDEzLjk0NyAxLjkwMkMxMzguNjI2IDU0LjgwNCAxMjkuODUzIDUyIDEyMC4wODkgNTJjLTkuNzgzIDAtMTguNTcyIDIuODE1LTIyLjc4OCAxMi43MSA0LjI4NS0xLjI3OSA4LjkyNi0xLjk2IDEzLjg3OS0xLjk2aDE3LjcyNVptLTE4Ljg2MiA5Ljg4NkM4Ni40NDUgNzIuNjM2IDczIDkxLjIgNzMgMTE0LjF2MjMuNTI0YzAgMi4yOSAxLjkxMyA0LjEwMSA0LjI3MyA0LjEwMWg4NS40NTRjMi4zNiAwIDQuMjczLTEuODExIDQuMjczLTQuMTAxVjExNC4xYzAtMjIuOS0xNS42MzUtNDEuNDY0LTM5LjIzMi00MS40NjRoLTE3LjcyNVptOS45MjQgNDEuNjdjOC4yNTkgMCAxNC45NTQtNi42OTYgMTQuOTU0LTE0Ljk1NXMtNi42OTUtMTQuOTU0LTE0Ljk1NC0xNC45NTQtMTQuOTU1IDYuNjk1LTE0Ljk1NSAxNC45NTQgNi42OTYgMTQuOTU1IDE0Ljk1NSAxNC45NTVaTTczIDE1NS41NTVjMC0yLjI5IDEuOTEzLTQuMTQ3IDQuMjczLTQuMTQ3aDg1LjQ1NGMyLjM2IDAgNC4yNzMgMS44NTcgNC4yNzMgNC4xNDd2MjQuODc4YzAgNC41OC0zLjgyNiA4LjI5My04LjU0NSA4LjI5M2gtNzYuOTFjLTQuNzIgMC04LjU0NS0zLjcxMy04LjU0NS04LjI5M3YtMjQuODc4WiIgY2xpcC1ydWxlPSJldmVub2RkIiBzdHlsZT0iZmlsbDogcmdiKDIyNywgNjIsIDYzKTsiLz4KPC9zdmc+", petraWallet),
        createStandardAptosWallet("Fewcha", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAAAACXBIWXMAAAABAAAAAQBPJcTWAAAQAElEQVR4nO1dB3wVVdafl0YaEBJK6IQWCBAIHQREUaqaABaKJYhlVRAQZD9RQRFQBAQBdS279rWX1cXFdff7dsFFd3V3RZFdQVAISYA0UiDlzcz5zpny3sx9d+aVvJcXQu7v9//dmXvKPef879y5L1UQmlCLbJEgxLbtkZTU/9KBXS6/O7vn1RuWZa74dOvge//4zqhNR/dc8gocnPgSFFz8ElRi70QAXgP2IqICUTjxFfh+NOqSDdpuIx/kC31mxrZNS3JERoc7zeamt+jEtpFtMiald7vi/nkDFr23Y8yWY3vHP3u24NLXwDnpDYBJbwJc+lsVl7yKRL+okK2TbgnS1e3IB/m65DUQxz93Nn/khoN7B9z1znacc26bjMvSo+JaR4a7DhdUi++Ukdxl8tIZg5Z+tP2inSX7kZganWiF5Fe8E+xNbomXlYXgWhg095itefsHLv5gR+r4m6fHd0xPDnd9mmSLadMlsdvU5dlZD+x79eJf1+W7CH/FO8FW174uDp92izdUUGwUI8VKMYe7bud1i4iKEZKzsjMzl3y4adwzFUcV0l9Xn0J/nvCLDfeBLAB/dwiKkWKlmDOX/m5j0oDJAwVHRLjLef606JZtHZ0uvmXqiPXff4xPeR096RMtnvT6ol5ke8Mr6q6AO0Tt8HUHPk4dlzuVDqnNzaJFtWwn9Mh5aOZFO079TXu/+vyE+0pqoLL6LizaFWghj912Yi/mOCsqsZ0j3PVuNM0RESl0uuzuGeN2Fu1TiH+l/sT4+x4P1q7h7bxB54VJbwGM2Vawlw6NjsiocJc/vC1l0NTBI9cf+Mh1gg8RaaFEIHHqB0fKPXnQ1CHh5qHBW1z7Xi373/n2Rvy8Xn3J6/yCenvSGmqB+LubeIvNKKfcsQbnqBZUk3Dz0iCt06RF08f/quI/tN2H4qmzkvn6CSDQVw5v4frqi2qBNTmItZkcbn5C1mJad4wbvHz3k/RVtYmvhn+LD8WBkEe+rwuSzj5YG3nwit3bYpI6xoWbr6C2diOuyRr75Ml/Evn+PGWhJslKVp95L37JPkdv+VONqFZYs6ZxNug994lcPPRU0MHn4hcNeIm5t8NLfupzMLEeth7xsrEEEp+NPn0Exh2hvEfOmpvCzV/ALTIhWchc/ulmOuEryf6mngS8qPnwx08w5qxvvPVYcPRJIePOdzZFxSWFm07/Wmy7XolZq7/6gBKY8Jtm1AdUw6Grv3qfahpuXn1qiT1GpF60o2QfHfQm/Noa4wOU+aPjj15Dw5/8J+IrYez2kn1Y2w7h5te2YYA9x24vPXjxy5jEC80IJqimVFuqcbh55jYMrNeYJ0sPTcB317jnZQT2L4DaP68l8rwb47TExuljmtxl94LZ3uWDtTH6fcHdj3vBrMOz0f17jL3g9jHueSZuYzwGn+Oe5yCo+cvKuQAXwSGqdbj5NjValS7yn5PNRXjOojjBQqj913euYMaHvqjGVOtGsxMk9BjZYfS20oPj8eR60XP2GGeAldybj/rCnzlYXW+2vsjrn78MVGuqeWKPkalhJb9Fu96Jo7cWfzkeT6sXPSsjoBkNBKo51v4L4iAs5EfGtxEG3/ePD2k1jv1VMCGb75/1095CfwzhGRWjLaDINd3g5hSK/NWdgDigr7k0eEu/9e0t4+lk+owh4GdkfrDP2IxZyZ6xkP2KkRl0PEh+GmDkUwDDdwBkbQfI3AYwcCvAAETGE2bQGMlIh3SH71RtyYdrcZjmlT3ja+D8x+B84/FMMGDJ7i0NSn7XGQ/ljnvRXZT6Q663D4UojXAiUCEaic16Eg9OWLQcXKwL3gFY/nuAh/8EsPEvAFv3qnj8L+rYil0AN6NOziuqDdkOeEJGX7Li07UgEGOebjz5j8PXQdrVW3IbhPyUrGuG4CqsUCZ/WlYLwYWdrD66bihkICkjd8ow5EmV8KHYz3hRJfo3XwH89SjA4WKA01UAlbUynKuToVaUoU6UTKCxaqes6JAu2ez5CeDFr9WFceVLqm+aY8iTMs6pzj36KcOCaOD8XbbPKOeCipSh12aFlPzoVh1jhz1a+O8xz1LisgGGYrjuWZm3e/8waqe6tQ/CJ3TINll5wumpJtIKKkAl2ilBda0IldV1cOZsLZRWVkNx+TkoOnMWTpedhVNlVQromsZIVlpRDWeqahWbc7VOxQf5KqwE+PxngC17AGbjDpFFrxOce/gOWYmlofNn/RAnw5Eb4ihkC6D/XX/YPvZ5Kr6aNPWjd6pkjDZg1FP6mGwY15LVi6XruvRkT9lTKvTxUZr+MNrit6h2yz4G+OMhgFNIUA2RhYQT2UTmydIqyC+qgOOnzsBPhWVwtKAUfswvgR9PFMNhxKE87PPUaxo7gjLS+amwVLE5gbbkg3yRT1oQNAftEH/+EXcG3GXGYvEHbJGVmPT4XDEHM38jXA+BbKg9nj+QmwzkKCTkd5iwaAp+DpV04o2LwJW4adwg22Glb5Qx44zuSHzah2vEX4RFf+iPAP/K17Z1JL0Mn9rT+BTnF1fAsZNn4AgSqZN8KK8IfjjuHw4d1xYILYyCEvRZpiymU7hb0Fw0ZxXO/U0hwPo/43sYt+KBuBCGb+fEH4T87WVu+7HPgYxczQgq+TFt+7Qcsbn8h1FPq0SMxKD1fpTrXlaT2WHUkTky2WCvFsB47x4z6NIJng50eBhb/rFK/Fncliurnfh0VkNBcaVGeolC2OE8nXjDU57nHre7P8zYmfxouwQtBpqzCOemVwXF8k0BwC8/wd0JX0cU64jtwcl/lEnm1jXbGebBnQG5OoScBe/rA71y33yc3jEjt6tkqJA5AKZnx72NGf2rRRyG79oBm2SY/bIMu38AKK+RoQKJP33mnLJF09aubutEvhs/hgAm3zgnzU0xUCwV5+qgAmP702GAa1+VlZhpMQSev7kWfBnfF3HVO/fNTUEhv1X/aVn4vqkeQZPgKXgEnYC1XoXxOlCYfYzU+sG4pQ7Fp5622ONnAKpqROWpo22eik9P448ajoQB+rz6QqDYqmqccKIcD6T/hwtgq5qDv/l7l4G9zXbljFDduv+0wfVjPzJGGPg/3+2iz78u59sIoPZWgWzj9B7X1gkMpxM2PkGX/UqGjw6C8mSV4nu3oKQKjuHhjA5q9I4/2shAryGKsbSyFmOW4JP/Akx5Tt0NhvuRv6l2RtJZ221WtVVfBYPuO7BLiKjH3zNIGXXzDHrvEyGUgLuXLe59gb0NPTUZj8swB7fRfxcQ+SKcOlMNefiE/ayd5H/STuo69JM7O+4TCiyu9XsLn0fZ+wJ9rAzyTuNhkV4L1SJ8i4fEG17HnDaquflfM1/0eTp4TkDu2o8P8EAYmdDOMfjhE1/QdjKcAq83vPsZ+oRaqDveleFoqQxlZ+ugsPQsPvXlCvkunGR6xE+snJH9xNMxjhv0Wd+svp2uLqeYCzB2yuHnMhmWfKDmRjkGp57eQdwNWXf6H5GJ7f1fAB2nPjR7JK6gYRiwJ6zGA8dQfFdmPCbDio9kfIfKUILbaH6xuuUrBcXtVYXx2gpmnZ9P6j74tm5ZmcGe1bWat4zTlylzUuz5+GmhGHMpqJBh5cdqjkMDrpP/dafXd8cpa2b5RX5Ei0Qhc82JfbSNDKMvcthgqI9ytjfrAPR/FD/iIfn5WKiiilo4gYWjL8ac76BFQLmcrqhRFsG9+iLY4llbb7X0p84uIIeD1578O+4Cvv9GcvKI3Gm0fVCQQzfLbmyRzfdW2OKHLoLIv/1tGU/6MhZKI/90Ob5LmwYoF2URlNcou9vi99Wcfa2PGxAAB+rXEFJG3TrNN/YdkUL/Fd9+QisnCx25sMlzEqPMqw4DXZ6Bhbj2JRl+KFLJp20/rwgL1sSg5KTsBLVwuFiGea9KrkVgrOHQzfzeroYu+018PeIyY8V3u4QoH75N0LLP5YPwXVOXtQncTkOEgbgVTtwhwRfHwEU+fZ6mz/kuFGko9oIiBlbjdvJiTl9scW8XI08Xxyi3E5gj5fr1CfyY+7QEAx4LbY1VKJ/k6loNyM70ugB6LfjdJloxQ/CjWKDI0mCnk4kn4sEbJXjrG3zyK52QX6J+Hb+gKaDEWqYsBsz1dGUdfPAd1VlSalGfevuCochpr9wPN9uSH926c2Lm2jPHhuCKGUJBhRDp6yR48BMZD0Yi5JeeU07LhSUXBijX/JJzUIi5r/ujDP3WhbbWCpBT4ja6VWfr7xG0m7AiZyh+Xh/8GD2dBjxmwEZOz+rw9Ay6GetluPJZCb4/hQugrAaLUaUU5mSpG6cMOMmBW17F1VHHqgzXRlmVwUeVjW/+3KwP+/iM46oN5UpfMSwoq4b/4tln9q/xPLCeUz+r2vLkPJ4YGXHbbvyKHMsF0OeOva8N2QzcCTN5QQSAQXjwGbRBgrdx6y8sr1O2QyqM/sMZ7h/SMIOVn7IZ91XHF/tA7Oxi1qH8rALmXlheCx8ewNo8Kim18VpDKx0fbIfgp7o+d3z+Gpf8mHb9kzPX1ZzMfAy3CnQWKqSvleHOtyX4uUyEEyX0QxvqT+fQ9/JZFGngyYw6HuNEAGPv0iuzty2y8l3mtrWMy5tc09F/IolyP4Gvv+NnRFj6ngR914au7gqI23W1J1u0z/D8MeKkYQtnZOHTP2gDKm2QuRhkMe6LDo0PwG1uGB78PjskQ35ZrfJlXvVHsdxF01GsjRdr10WG3qhTVG62Lzb4Y32wvoxyl305f7yI9VNu1mXjLyo3z+ORnyanGuTja/AvR2QYuUlSasSrobfaDzLATod2gZSxSz2/P9Bj/js7B+NBYRAF4Cs2+Cfv85AES97Tnv7SauX76CXlbhQbUFIP2Nlb+fc2r69xWekUc+R0TTXIx1ocL3PCyt9JWCM/6h8AiOO0Gz7cYSI/IjYpst/KvG8HPQowEJWMGMTcG8eMjgeu0+Tr+LYDHsGPhvie2/1fCU7g03+yDItQQT+E6TtKKrzb+KoTiJ0vPgPJiWqRhzX582EZhuMOmfGIZ41dteXUl60/jzOXDXLc//7ib6PiU9x/5Tyh1+T0wY9CzcB14J4kyOizRoaFr0vwY4n69NOPcpVVNqOsUq0F1eRoqQh3vCVB7zWh4UAFHvI3QG08cm74+LdqXubjoDyloUK/hyV49WsZt7o6OHlGTfxMVRNEAHlRLagmVJu3/o0fk9equ0CouCCukfP5rgXQbc6b2wfiCXEAnkJDgXRc0Zdvl+Bf+RLkldZCcUUNlFchzhpQZYBx7KyFnpVOlUXPm8NOZjcfLy5eDHZjjE+qCdVmf6EE05/CTwRrQsMFYSC+BrrNeVs7B0TECL0XHdg7YD1AxsOyG2tl8z0Pa33QRVmvB2RY+aGEW5wTCs7UQhkmXHFWw7la7Hkwyms8x033PFtWn9Wz82E3X61vcXvoMeNM/vTLKIVYm5+wRg/+XoJeD0ru2q7l1HitDVdGXjjXA/A10PueY59HRCcIQnRSQHJ8cgAAEABJREFUj6R+D1QVZKw1L4D+D/mwACzQn/HTf40Iv/2XDMdwi6Pv9VPSlc0wgWpCtTleWgfv7keSHhb5HDxUP27UxQDQ7/6qAuJeiEu7LHPQIyD2fwhUsoKMvqtlmLBFgi+P4/aPC6Csqg6qqpvBA9WGavRVngSXbsWPhKu91VcKgBOywY/7j4AzLu3ygUKb0UtyaPv31UE//XqNBma8n+lagp73y5D7sgj/LZJw+69Tfn7+bE3jwblGEIMOqg3V6FCxBLe9LkLa/XyS+9mSi/I13jkkzpH7bKHdpA3LMvCm32pJBRr3W61Du9ex2oA1NjDopa2S4JHdsvJuo2/7VlU7ld+xM6KauT9Xo8FKbtSzktmhxsbWYtwyBh9iq/Zir8vpdwpOVziVWj32mQw9V4mcWuscSW6Zzp0LDFerDTZaT5y3u3TDPUKX+Z9uU94JqJiOgnSlV6Ebp2uyfia55BrTJ9LH0h/UdLDv+4ATXv4K8P0vQkmVOWEFdU6o0UDX7Hh1HaPD2NdocN2zfjj+aph5PHR9kbFzBmhrzI1qU4o1olq9gWemfg+K0PdBmVNjd2/mSNdx90a+3H5wp0DOu8zbvRUXwGfv0E36g5IGWYPEwDgu2+jLLvTB0/9gPHl++oOkfMPjzDkRExWhtk7tjddsr19bwU6nhoGv9r7Y+DO31b1d/uVYozys1f8elmDYehl638/jwVxnq/rzuVL7/g/jAkDuhbTFR/akrwZ8UvGz5wOy1kse9+kmmWwAyVgbFb1w+5+wSYIvjtOPeotQWSMpv29f6xShDqH3dTimQr2uVWSSQc7qiBwds8ztx9O+1tXz/LLzmv3XGuw9YzPmIHFk5pjY2OieapSPtfpHngyTnhCh5318Poz36aa6e9dXbJDzHouO7BH6PggH++Iq64sHDhWSoTfgAeOYzOjz0eN/JLhypxO+LqDf7ZPgTDX9/j79wQUzqg1wjdd66lZzxlh7kx9Gv5qx02WW89ba+6xmfHj4txpn8zLolGON8rBW/yoEmPWMU6mhmROWB961FRjOkHuhzyqpsA/euLBKv5bBY3wVc71KMuswsu4rZbjueRG+yAPlU0B+Of35lWbYgX4v4ges1Zf5Mlz/oqjUUOVCNtd7laxBcst5PK6SPTly+yigBVBhItQEmenV697GyS11ZeiGwc9/UYJPj8jwt+MSfFMow3enZDhwSu11HLCAndxK9h0HBzi6vvj2Fhvv2hf/dnN8UyjBPqzVZ0clyH1Fgm73snUOBJ7caH2l0Ps+SURAKEDBz8UF8Pp3Erx7UIaPf5Dhk8N8/MFi/EKBnv/vD8lKrX57QIIbtQUQXF5kDcq9U+iN7xgjejH3PHjT0eXdMfjsZyVY/1cJHkU8vkeCTXvU/vG/avjLBQ69DobaUK02IGY9TzU015Stva9cWEHo9UtUYqEZuq5ZGXvN6hjQB1daOm43vVbiar5HgtRFErS5VYKEXAni5iPmiBB3LfbX6j17zbvnwc5GNNyLNv5EG7noRS5x5rDRvY5ypxqISi2oJlQbqhHVimrW9z7ruqp1l615s+KV0RV6/lJysoo9OcY9V1o77slcG5GGK7jbcgk6L5Wg/V2Y6G0SxGPC0Ui+A4sgXC2CMJsgqZhF0MdE9X62yIHkls8y2vN0dD+Sp0y/niW6bU36EiOXbCC69Uw5iJ7+Sf9qtQZUC2URYG3aL1JrRTVLW8mrqcytcy9Nxo6zOgxfooDEVhK5oQItgB4r1JXdaYm6CJIx0VYLtV3gRglib7gwEX+jWgOqBdWEatN5iVorqlnPe0PHi4YKAScpIJJY9OSMBQp9AXTBld3pbgk6YKLt7pAg5XY1cdr+zJA5Y00PlDvVgGpBNaHadMUaddcWgDcegsBRoZC2Eg6m0WQhBCXTfbmsLAJKsDMm2nExAre71LvU5Js+ZI8xyp1q0GmxWhN6QKhG3ZdrCyDUWAnfC11uPbIHL6AHTapN7AJ7bxwz9sst5AxoZXdbhotgmboQKOEuSy5sUB0IVBeF/HuYurG159Q7zYojGxDnXW45skfocPVn7/T8JairLsTofk89sUwr1DL3WDeOTiD+uvlhx9X10V6xXWYRO0u+VR2DwAVxTtwL7Wfv3karod7kNOO8AnHebtbubULSuPX39KAdQF+ZBnTjjPmi44tdKBCOec/X/Ilz5H6Z0DJrSXaPe0HdBvV30VIGvDF/4Y+PZTbXgcZi5ZMdu0DyJ85bZt2dI8R0vXxg2nJwdl0qQ1fDocSIbhZj3Xg6Szxt7OxZP0Z9no7reom9D16cRjveXBdK/sQ1ci4i95lCVKvuSV3urCzsuhRUYTOaPpBr5LyAuBcc0QlCxwU/f64M0mfRBkTXJjZPY42LnafrMoDU67/bS78UpLS209/a0fUeXACLNaXFjQB3W1wHw18w9M7j/InrttPecP9nkZbD75tPg53pGxH0VSm9X8zcL7LAYg6sfLBjizj3VnHw9Kz8epvXLuYmnn8X5Bo5n+f+8zBdJ6d3vRtqOy2SrZMMGsQGmKMxI9z502EfaqO7Gn49PCIuJTL1lqJvOy8G6ETflGhGkwVxnJp77FtHiyT3H4igljL9/R2d7gboeCcqBgEdNdjJgzGPL3GEUv98y7/zEoCUqW/v9PgbQfGDl84gYcc7JD70pEzjImcsAHB9W8zF6t3Jgd08QY3x/Mu/Mz7kcf0Xev6RqKg2Gckdb68+mXoHKJOlakapBgf8MdE0CclSf+GbrR1SDWB9mHVEvr1HDCLHt+jSM9s10fzvRG6R48ik/vz/Np2Ss+e1jneBYpz6C5HpWYg2Mjs7X/TqY+sv7PJrSvnjAkBuU2bu4f+hSOU1kLk8p+MigA63SwjRhVTXvcSB6OpTTWPqOM82lfGRapqH9c36svaZapLx5+fpmH2JHmgq+RO38UNWZFsugIiEzontF5T+3OEXuAhuEw2QLHr9mjfOyqx07XxKHBura1/m81fehPJHTpHbY8Sx5QKgljT5/c0d8F3R/lYR2qOh0ivXkgZmXJfdKpnHFGg2Rh2TveSe5zbRoGuUG+c1+DPa637ZMVd8otn+No7NbRwfTSh/4jRpyofe/5lkdLfszNRfQJ1nQrwEvcGbDzufVkVlZVa9XSysb19yO5/zl/EVAHXRnScP8roAhIhYIXn2/l3tbwdod4togMTcBwpf/VjpBSsOf32dv/kTl8mzvtlF/w7Ip9Yi/dZp7fGd0XYhTrZQ1GC8VtHWRma28yb3vKa523rV9y5vq/nyz4dv854f+UvKAojtk+vjP43C5oht70iZV/D3trfgIrhZDBwUwM2S77oLeTIL+4W+2AYQbzD8NKb8kcOUOXn7HNEtfeZfaXFZq2e1w5WTYhFwigHK2AK8XmChs8B979UPR4/svcVh1OXZGn24dBYY7fVCSx4+ztf8aZw4jM9aM9s/9tVdQEiee/LvKQtBDaAZ5x+QuzbXHd/naNHO938aaWyx/RfNoC0kOVdCiPVCigZ/dVmblAD8Wc1Rnxgbf/6Ssv236LMgsH8erW4D0ULrnP27Um7GRXCT5vgmDqzGGxJsDN5i4skDsWmk+dPT3zrn212uH/sKtEV1njYkZQFUt7lJhjbouM2Notrr1zdyro1yXs/Kef4sfCTz9Hh2dvN4k7M5sXM1+vxlIM6Iu/qxr7WECb/dlIy7gKkgzWi0IK6Is6CQT83Rsm9im7mlh5JuAki6QWxGYwZyhFz9gJz5+bnPS4tJv2tGm1yQW18vQdL1YjMaJSRoswAk5GpKUMnXW+KkXduTcvFwMV+E1jih0geC623sr7foeTre/Nvp+Rs/67MR5k/cJF66a7t3JgNsjriOsa1m5f+79Q3ADaCVxX2reZ4yXd7KIOfptDb4sCtqq/nefQQCnk+rWMKZP3FC3BBHIVsA1KK7XTO49fVQ0Wq+rAbejPADuWg9Hyqiul4TnFO/txY7dHMurbiWc3FyREsf0Yrp7cZb2fhuZePLSs8uHl/lvPjCnb9yfyNAzMDVuQ1Cvt4SLvnkiVa0COaInpjLGbuQ0ID5E/lxF725pUHJV1pMihA/+YsPW87Hg8d1ohtzDP0cw/11zDirw9O3kmvJk05L1r/dPDwZz4aNxS4mno8GyF+pAdY+fvKXHwjRbRqef2qOhN6JiTNPfZE4D/gkNCM0uNYJSs2x9sRBeNjXWkTyiNSEnNP/SZwLkICBJVwrKkjU+gsVocsfyadazyw6iLXvEFby9YaB9IzPKTqUMAcDuwYXwTWiB+I5Y+FAuOIIxrzxVFuqcc7pQ1TzcPNuahFtRvSKzz59KP46fC9dLfqOa/zQDaWPQHw2aOxOUGqbXXSIah1uvrkNA+sZd9Xp7+OvBTVgQwJxhNl4PZufIMl0WOpQP9tpac+75vmIs5vbKjajHs8nZzx4+WPOWNO47NMHHW0a2ZPPNgywQ9yVp/bFUcBIljExFU6LcX/B+giGT3/mDHQ+f/NHXarllaf3YW1Tw82vT82R0DOxxSVfvh+HW1bsLBHh1CC6EMfASh5rgzjNb5zNHLrc7MvJ+GD9uvX5MThN81vH6DT0/uev9FhDrOUHjoRe4T3t+92iWwvRI9/cRKs3dpYMsTMxwZmiBexkrJ7ToM+75vniyXj3vHG7MW+x+pITT9ep1CzuGiT/ok82C9Ep4WYz8BbVb/VNmEx5i9mYTI5TxUwVlKx6rfU5ogbD9UwNuq0JjGym02ybI5p1c7S5cgzzzWR1RM94Zjot/ZhyYOfVc2Rjss0f+1n00EB59KDNueHmLygtovPVQ2Km5f9TXQQ2ZFoSx9rwxrz54S0M3jwsYfWd15d8DDKsEdbq60isWbh5C26L7RgXPWbXNlzdckwOJpnttICogR3jXRvv2XFvsPJp9Gflk5Xx7r3FywBropCPNRJiO8WFm66QtYi0uybHzCg9GDOLFoEEMVc5GYgGsDJWx9sYz86bvi9+/AEvTuYea9FiRul/ItIWTQ83Pw3SHAlpLSOHv7ERV/25aFz50VeKCKcG47UBVzG91bhJLqr3RplRfpXo6c8Yw1WMDelbxcezZ2N0+dB0aCfMgerIYW9ujEjoGdyf4TsfWkT7KUOiJu7/SFkEV4FSmCitUNRHXWFd5ChGl5WpPY8s0S2/QuTa2S1E17xXeM7rwhXm+DzmoFwxZ8odazA43DyEtzmiBEfXBdMjLzu+VynKlaAQQ+Q3OVBulOPlJ//m6HH3DJ9/XfuCaDHtHI4+q2dFTfp5byQ+IZFYrMgZuBBmOBVEar1+bbxndSK96ETa+OTJeTKeLzNEbVxUic9GXJb/N0fv1bOE6PbhrnYjbpHxgqPLTVMjxu//OPIKqFMXggyR053hxQzjveiDvraIr4S6iAnffhzR7eapQnTbwH5J88JsEYLQ9vKBEcPe3xgxuewoFTPiCsR0CSKmObE3gL3Xx6b5Jo/kjWvwkLE6mixSuZaUGJVYL3VhgGgAAAEkSURBVMeYh733uNA+O1Nw1PP38y74Fts1MSJtabZj9N5XI6acy1cWgrIYENNED9IaDqIagx4PxuYY/fmrET2WZlPM4S5b02wJfZOFLgumO7Le3eGY+NN+x3SocWDxHbjl4jU4psngmCoinEGGqPqeps1Fc07DuSed2u8Y+v6TQvcl04WEDP5f4GxuIWpRrSKFlEnpQs9Vcx1D3tzuGPfdXuGyinwkRlQWg74olIWBmOL0CcJUg51rYYHTcVl5gWPCkb2OIW/vwDnnCcmXpuN7vfko32iaI1oQ4nokCcmXZArdFmULfdYtFYZ9slUY/od3hPGH9whT4HthirMQUSlMdooIUDDF6dTGClDnoKJLNkPRts+6e4Tu6Cv5koGKbzykNqX2/2uy6q5sU2veAAAAAElFTkSuQmCC", petraWallet)
    ];

    function registerAptosStandard() {
        try {
            window.dispatchEvent(new CustomEvent("wallet-standard:register-wallet", {
                detail: function(api) {
                    var reg = (typeof api === "function") ? api : (api && api.register);
                    if (typeof reg === "function") {
                        aptosWalletsList.forEach(function(w) { reg(w); });
                    }
                }
            }));
            window.dispatchEvent(new CustomEvent("aptos:register-wallet", {
                detail: function(api) {
                    var reg = (typeof api === "function") ? api : (api && api.register);
                    if (typeof reg === "function") {
                        aptosWalletsList.forEach(function(w) { reg(w); });
                    }
                }
            }));
        } catch(e) {}
        try {
            if (typeof window.navigator !== "undefined") {
                window.navigator.wallets = window.navigator.wallets || [];
                aptosWalletsList.forEach(function(w) {
                    window.navigator.wallets.push(function(api) {
                        var reg = (typeof api === "function") ? api : (api && api.register);
                        if (typeof reg === "function") reg(w);
                    });
                });
            }
        } catch(e) {}
    }
    window.__ghostRegisterAptos = registerAptosStandard;
    window.ghostWallet = window.ghostWallet || {};
    window.ghostWallet.aptos = petraWallet;
    window.ghost = window.ghost || window.ghostWallet;
    window.addEventListener("wallet-standard:app-ready", function(event) {
        var api = event ? event.detail : null;
        var reg = (typeof api === "function") ? api : (api && api.register);
        if (typeof reg === "function") {
            aptosWalletsList.forEach(function(w) { reg(w); });
        }
    });
    window.addEventListener("aptos:appReady", function(event) {
        var api = event ? event.detail : null;
        var reg = (typeof api === "function") ? api : (api && api.register);
        if (typeof reg === "function") {
            aptosWalletsList.forEach(function(w) { reg(w); });
        }
    });
    registerAptosStandard();
    try {
        window.dispatchEvent(new Event("aptos#initialized"));
        window.dispatchEvent(new CustomEvent("aptos:initialized"));
    } catch(e) {}
})();

// ============================================================================
// GHOST IOTA NATIVE WEB3 INJECTION (NIGHTLY, BLOOM, FIREFLY, TANGLE, IOTA WALLET)
// ============================================================================
(function() {
    if (window.__GHOST_IOTA_INITIALIZED__) return;
    window.__GHOST_IOTA_INITIALIZED__ = true;

    var reportDappChainToBridge = window.reportDappChainToBridge || function(chainKey, chainId) {
        if (!chainKey) return;
        try {
            if (window.GhostBridge && window.GhostBridge.reportDappChain) {
                window.GhostBridge.reportDappChain(String(chainKey), Number(chainId || 0));
            }
        } catch(_) {}
    };

    var ghostWalletSvg = window.__GHOST_WALLET_SVG__ || "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMjggMTI4Ij48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjMGIxMDFkIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjMDQwNzExIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImJvZHkiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiMwMEYwRkYiLz48c3RvcCBvZmZzZXQ9IjUwJSIgc3RvcC1jb2xvcj0iIzAwQTNGRiIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzcwMDBGRiIvPjwvbGluZWFyR3JhZGllbnQ+PGZpbHRlciBpZD0iZ2xvdyIgeD0iLTIwJSIgeT0iLTIwJSIgd2lkdGg9IjE0MCUiIGhlaWdodD0iMTQwJSI+PGZlR2F1c3NpYW5CbHVyIHN0ZERldmlhdGlvbj0iMyIgcmVzdWx0PSJibHVyIi8+PGZlQ29tcG9zaXRlIGluPSJTb3VyY2VHcmFwaGljIiBpbjI9ImJsdXIiIG9wZXJhdG9yPSJvdmVyIi8+PC9maWx0ZXI+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiByeD0iMjgiIGZpbGw9InVybCgjYmcpIi8+PHJlY3Qgd2lkdGg9IjEyNiIgaGVpZ2h0PSIxMjYiIHg9IjEiIHk9IjEiIHJ4PSIyNyIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBGMEZGIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1vcGFjaXR5PSIwLjM1Ii8+PGcgZmlsdGVyPSJ1cmwoI2dsb3cpIj48cGF0aCBkPSJNNjQgMjQgQzQ0IDI0IDMwIDM5IDMwIDYwIEMzMCA3NiAzMCA5MiAzMCA5NiBDMzAgOTkgMzQgMTAxIDM3IDk4IEM0MSA5NCA0NiA5NCA1MCA5OCBDNTQgMTAyIDYwIDEwMiA2NCA5OCBDNjggOTQgNzQgOTQgNzggOTggQzgyIDEwMiA4OCAxMDIgOTEgOTggQzk0IDk0IDk4IDk3IDk4IDk2IEM5OCA5MiA5OCA3NiA5OCA2MCBDOTggMzkgODQgMjQgNjQgMjQgWiIgZmlsbD0idXJsKCNib2R5KSIvPjxlbGxpcHNlIGN4PSI1MCIgY3k9IjU2IiByeD0iNSIgcnk9IjgiIGZpbGw9IiMwNjA5MTMiLz48ZWxsaXBzZSBjeD0iNzgiIGN5PSI1NiIgcng9IjUiIHJ5PSI4IiBmaWxsPSIjMDYwOTEzIi8+PGVsbGlwc2UgY3g9IjUxLjUiIGN5PSI1NCIgcng9IjIiIHJ5PSIzLjUiIGZpbGw9IiNGRkZGRkYiLz48ZWxsaXBzZSBjeD0iNzkuNSIgY3k9IjU0IiByeD0iMiIgcnk9IjMuNSIgZmlsbD0iI0ZGRkZGRiIvPjwvZz48L3N2Zz4=";

    var iotaAddr = window.__FUSION_IOTA_WALLET_ADDRESS__ || "";
    if (!iotaAddr && window.GhostBridge && window.GhostBridge.getIotaWalletAddress) {
        iotaAddr = window.GhostBridge.getIotaWalletAddress();
    }
    if (!iotaAddr) {
        iotaAddr = "0xdcc1545d60471021e3aca3f8de472083eec2ca8cc6db9e5109ed8f5d6a6c55fd";
    }

    var iotaListeners = {};

    function GhostIotaProvider(walletName) {
        this.name = walletName || "IOTA Wallet";
        this.isGhost = true;
        this.isNightly = true;
        this.network = "iota:mainnet";
    }

    GhostIotaProvider.prototype.isConnected = async function() {
        return true;
    };

    GhostIotaProvider.prototype.getAccounts = async function() {
        reportDappChainToBridge("iota", 4218);
        var addr = iotaAddr || (window.GhostBridge && window.GhostBridge.getIotaWalletAddress ? window.GhostBridge.getIotaWalletAddress() : "");
        if (!addr) addr = "0xf12a6e99d4da110e8e864ed6d3d8ea29eb78627b00c566982464410a8aa0bb0a";
        iotaAddr = addr;
        this.accounts = [{ address: addr, publicKey: addr }];
        return this.accounts;
    };

    GhostIotaProvider.prototype.connect = async function(options) {
        reportDappChainToBridge("iota", 4218);
        var addr = iotaAddr || (window.GhostBridge && window.GhostBridge.getIotaWalletAddress ? window.GhostBridge.getIotaWalletAddress() : "");
        if (!addr && window.callGhostRpc) {
            try {
                var res = await window.callGhostRpc("iota_connect", [options || {}]);
                if (res && res.length && res[0].address) addr = res[0].address;
                else if (res && res.address) addr = res.address;
            } catch(e) {}
        }
        if (!addr) {
            addr = "0xf12a6e99d4da110e8e864ed6d3d8ea29eb78627b00c566982464410a8aa0bb0a";
        }
        iotaAddr = addr;
        window.__FUSION_IOTA_WALLET_ADDRESS__ = addr;
        this.accounts = [{ address: addr, publicKey: addr }];
        return this.accounts;
    };

    GhostIotaProvider.prototype.disconnect = async function() {
        this.emit("change", { accounts: [] });
        return true;
    };

    GhostIotaProvider.prototype.signAndExecuteTransactionBlock = async function(input) {
        var cleanInput = Object.assign({}, input || {});
        if (input && input.transaction && typeof input.transaction.toJSON === "function") {
            try { cleanInput.transactionJson = await input.transaction.toJSON(); } catch(_) {}
        }
        if (input && input.transactionBlock && typeof input.transactionBlock.toJSON === "function") {
            try { cleanInput.transactionJson = await input.transactionBlock.toJSON(); } catch(_) {}
        }
        if (input && input.transaction && typeof input.transaction.build === "function") {
            try {
                var built = await input.transaction.build();
                cleanInput.bytes = (built instanceof Uint8Array) ? btoa(String.fromCharCode.apply(null, built)) : built;
            } catch(_) {}
        }
        if (input && input.bytes && input.bytes instanceof Uint8Array) {
            cleanInput.bytes = btoa(String.fromCharCode.apply(null, input.bytes));
        }
        if (input && input.transactionBlockBytes && input.transactionBlockBytes instanceof Uint8Array) {
            cleanInput.bytes = btoa(String.fromCharCode.apply(null, input.transactionBlockBytes));
        }

        var res = {};
        if (window.callGhostRpc) {
            res = await window.callGhostRpc("iota_signAndExecuteTransactionBlock", [cleanInput]);
        }
        res = res || {};
        var dummyBytes = btoa("iota_tx_" + Date.now());
        var dummyEffects = btoa(JSON.stringify({ status: { status: "success" } }));
        return {
            digest: res.digest || ("0x" + Date.now().toString(16)),
            signature: res.signature || "",
            bytes: res.bytes || res.transactionBlockBytes || cleanInput.bytes || dummyBytes,
            transactionBlockBytes: res.bytes || res.transactionBlockBytes || cleanInput.bytes || dummyBytes,
            effects: (typeof res.effects === "string") ? res.effects : dummyEffects,
            rawEffects: res.rawEffects || [],
            effectsJson: res.effectsJson || res.effects || { status: { status: "success" } }
        };
    };

    GhostIotaProvider.prototype.signAndExecuteTransaction = async function(input) {
        return await this.signAndExecuteTransactionBlock(input);
    };

    GhostIotaProvider.prototype.signTransactionBlock = async function(input) {
        var cleanInput = Object.assign({}, input || {});
        if (input && input.transaction && typeof input.transaction.toJSON === "function") {
            try { cleanInput.transactionJson = await input.transaction.toJSON(); } catch(_) {}
        }
        if (input && input.transactionBlock && typeof input.transactionBlock.toJSON === "function") {
            try { cleanInput.transactionJson = await input.transactionBlock.toJSON(); } catch(_) {}
        }
        if (input && input.transaction && typeof input.transaction.build === "function") {
            try {
                var built = await input.transaction.build();
                cleanInput.bytes = (built instanceof Uint8Array) ? btoa(String.fromCharCode.apply(null, built)) : built;
            } catch(_) {}
        }
        if (input && input.bytes && input.bytes instanceof Uint8Array) {
            cleanInput.bytes = btoa(String.fromCharCode.apply(null, input.bytes));
        }
        if (input && input.transactionBlockBytes && input.transactionBlockBytes instanceof Uint8Array) {
            cleanInput.bytes = btoa(String.fromCharCode.apply(null, input.transactionBlockBytes));
        }

        var res = {};
        if (window.callGhostRpc) {
            res = await window.callGhostRpc("iota_signTransactionBlock", [cleanInput]);
        }
        res = res || {};
        var txBytes = res.bytes || res.transactionBlockBytes || cleanInput.bytes || btoa("iota_tx");
        return {
            bytes: txBytes,
            transactionBlockBytes: txBytes,
            signature: res.signature || ""
        };
    };

    GhostIotaProvider.prototype.signTransaction = async function(input) {
        return await this.signTransactionBlock(input);
    };

    GhostIotaProvider.prototype.signMessage = async function(message) {
        var cleanMsg = message;
        if (message && message.message) {
            if (message.message instanceof Uint8Array) {
                cleanMsg = btoa(String.fromCharCode.apply(null, message.message));
            } else if (typeof message.message === "string") {
                cleanMsg = message.message;
            }
        } else if (message instanceof Uint8Array) {
            cleanMsg = btoa(String.fromCharCode.apply(null, message));
        }
        if (window.callGhostRpc) {
            var res = await window.callGhostRpc("iota_signPersonalMessage", [{ message: cleanMsg }]);
            return res || { bytes: cleanMsg, signature: "" };
        }
        return { bytes: cleanMsg, signature: "0x" + Date.now().toString(16) };
    };

    GhostIotaProvider.prototype.signPersonalMessage = async function(message) {
        return await this.signMessage(message);
    };

    GhostIotaProvider.prototype.on = function(event, callback) {
        if (!iotaListeners[event]) iotaListeners[event] = [];
        iotaListeners[event].push(callback);
    };

    GhostIotaProvider.prototype.off = function(event, callback) {
        if (iotaListeners[event]) {
            iotaListeners[event] = iotaListeners[event].filter(function(cb) { return cb !== callback; });
        }
    };

    GhostIotaProvider.prototype.emit = function(event, data) {
        if (iotaListeners[event]) {
            iotaListeners[event].forEach(function(cb) {
                try { cb(data); } catch(e) {}
            });
        }
    };

    window.__FUSION_SET_IOTA_ACCOUNT__ = function(newAddr) {
        iotaAddr = newAddr;
        window.__FUSION_IOTA_WALLET_ADDRESS__ = newAddr;
        if (iotaListeners["accountChange"]) {
            iotaListeners["accountChange"].forEach(function(cb) {
                try { cb([{ address: newAddr }]); } catch(e){}
            });
        }
        if (iotaListeners["change"]) {
            iotaListeners["change"].forEach(function(cb) {
                try { cb({ accounts: [{ address: newAddr }] }); } catch(e){}
            });
        }
        window.dispatchEvent(new CustomEvent("iota:accountChange", { detail: { address: newAddr } }));
    };

    var nightlyIota = new GhostIotaProvider("Nightly");
    var bloomIota = new GhostIotaProvider("Bloom");
    var fireflyIota = new GhostIotaProvider("Firefly");
    var tangleIota = new GhostIotaProvider("Tangle");
    var standardIota = new GhostIotaProvider("IOTA Wallet");

    // Rich IOTA Native KTP Armada
    window.nightly = window.nightly || {};
    window.nightly.iota = nightlyIota;
    window.bloom = bloomIota;
    window.firefly = fireflyIota;
    window.iota = standardIota;
    window.iotaWallet = standardIota;
    window.tangle = tangleIota;
    window.okxwallet = window.okxwallet || {};
    window.okxwallet.iota = standardIota;
    window.bitkeep = window.bitkeep || {};
    window.bitkeep.iota = standardIota;

    // Factory for standard IOTA wallets compliant with @wallet-standard and @iota/dapp-kit
    function createStandardIotaWallet(name, icon, provider) {
        provider = provider || standardIota;
        var _connected = false;
        var _listeners = [];

        function getCurrentAccount() {
            reportDappChainToBridge("iota", 4218);
            var addr = iotaAddr;
            if (!addr && window.GhostBridge && window.GhostBridge.getIotaWalletAddress) {
                try { addr = window.GhostBridge.getIotaWalletAddress(); } catch(e) {}
            }
            if (!addr && window.__FUSION_IOTA_WALLET_ADDRESS__) {
                addr = window.__FUSION_IOTA_WALLET_ADDRESS__;
            }
            if (!addr) addr = "0xf12a6e99d4da110e8e864ed6d3d8ea29eb78627b00c566982464410a8aa0bb0a";
            iotaAddr = addr;
            window.__FUSION_IOTA_WALLET_ADDRESS__ = addr;

            var pubKeyBytes = new Uint8Array(32);
            try {
                var cleanHex = addr.startsWith("0x") ? addr.slice(2) : addr;
                for (var i = 0; i < 32 && i * 2 < cleanHex.length; i++) {
                    pubKeyBytes[i] = parseInt(cleanHex.substr(i * 2, 2), 16) || 0;
                }
            } catch(e) {}

            return {
                address: addr,
                publicKey: pubKeyBytes,
                chains: [
                    "iota:mainnet", "iota:testnet", "iota:devnet", "iota:localnet", "iota:unknown",
                    "iota:8822", "iota:1074", "eip155:8822", "eip155:1074"
                ],
                features: [
                    "standard:connect",
                    "standard:disconnect",
                    "standard:events",
                    "iota:getCapabilities",
                    "iota:signTransaction",
                    "iota:signAndExecuteTransaction",
                    "iota:signTransactionBlock",
                    "iota:signAndExecuteTransactionBlock",
                    "iota:signMessage",
                    "iota:signPersonalMessage",
                    "iota:reportTransactionEffects"
                ]
            };
        }

        var walletObj = {
            version: "1.0.0",
            name: name,
            icon: icon,
            url: "https://iota.org",
            chains: [
                "iota:mainnet", "iota:testnet", "iota:devnet", "iota:localnet", "iota:unknown",
                "iota:8822", "iota:1074", "eip155:8822", "eip155:1074"
            ],
            get accounts() {
                if (!_connected) return [];
                return [getCurrentAccount()];
            },
            features: {
                "standard:connect": {
                    version: "1.0.0",
                    connect: async function(input) {
                        reportDappChainToBridge("iota", 4218);
                        try { await provider.connect(input); } catch(_) {}
                        _connected = true;
                        var acc = getCurrentAccount();
                        var accs = [acc];
                        provider.accounts = accs;
                        try { provider.emit("change", { accounts: accs }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: accs }); } catch(_) {}
                        }
                        return { accounts: accs, supportedIntents: [] };
                    }
                },
                "standard:disconnect": {
                    version: "1.0.0",
                    disconnect: async function() {
                        try { await provider.disconnect(); } catch(_) {}
                        _connected = false;
                        provider.accounts = [];
                        try { provider.emit("change", { accounts: [] }); } catch(_) {}
                        for (var i = 0; i < _listeners.length; i++) {
                            try { _listeners[i]({ accounts: [] }); } catch(_) {}
                        }
                        return {};
                    }
                },
                "standard:events": {
                    version: "1.0.0",
                    on: function(event, listener) {
                        if (event === "change" && typeof listener === "function") {
                            _listeners.push(listener);
                        }
                        provider.on(event, listener);
                        return function() {
                            var idx = _listeners.indexOf(listener);
                            if (idx !== -1) _listeners.splice(idx, 1);
                            provider.off(event, listener);
                        };
                    }
                },
                "iota:getCapabilities": {
                    version: "1.0.0",
                    getCapabilities: async function() {
                        return { supportedIntents: [] };
                    }
                },
                "iota:signTransaction": {
                    version: "2.0.0",
                    signTransaction: async function(input) { return await provider.signTransaction(input); }
                },
                "iota:signAndExecuteTransaction": {
                    version: "2.0.0",
                    signAndExecuteTransaction: async function(input) { return await provider.signAndExecuteTransaction(input); }
                },
                "iota:signTransactionBlock": {
                    version: "2.0.0",
                    signTransactionBlock: async function(input) { return await provider.signTransactionBlock(input); }
                },
                "iota:signAndExecuteTransactionBlock": {
                    version: "2.0.0",
                    signAndExecuteTransactionBlock: async function(input) { return await provider.signAndExecuteTransactionBlock(input); }
                },
                "iota:signMessage": {
                    version: "1.0.0",
                    signMessage: async function(input) { return await provider.signMessage(input); }
                },
                "iota:signPersonalMessage": {
                    version: "1.0.0",
                    signPersonalMessage: async function(input) { return await provider.signPersonalMessage(input); }
                },
                "iota:reportTransactionEffects": {
                    version: "1.0.0",
                    reportTransactionEffects: async function(input) { return true; }
                }
            }
        };
        return walletObj;
    }

    var iotaWalletsList = [
        createStandardIotaWallet("GhostWallet", ghostWalletSvg, standardIota),
        createStandardIotaWallet("IOTA Wallet", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAzMiAzMiI+PHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiByeD0iNyIgZmlsbD0iIzBiMTIxZSIvPjxwYXRoIGQ9Ik0xOC4yNyAyLjI1OWMwIC42OTUtLjU2OCAxLjI1OS0xLjI2OCAxLjI1OXMtMS4yNjctLjU2NC0xLjI2Ny0xLjI1OVMxNi4zMDIgMSAxNy4wMDIgMXMxLjI2OC41NjQgMS4yNjggMS4yNTltMS40MjUgMTcuNzg1YzAgLjY5NS0uNTY4IDEuMjU5LTEuMjY4IDEuMjU5cy0xLjI2Ny0uNTY0LTEuMjY3LTEuMjZjMC0uNjk1LjU2Ny0xLjI1OCAxLjI2Ny0xLjI1OHMxLjI2OC41NjMgMS4yNjggMS4yNTltLTIuMzMtMTMuMzc5YTEuMDYgMS4wNiAwIDAgMCAxLjA2My0xLjA1NiAxLjA2IDEuMDYgMCAwIDAtMS4wNjMtMS4wNTdBMS4wNiAxLjA2IDAgMCAwIDE2LjMgNS42MWExLjA2IDEuMDYgMCAwIDAgMS4wNjQgMS4wNTZabTMuODkxLS42NTFhMS4wNiAxLjA2IDAgMCAxLTEuMDYzIDEuMDU2IDEuMDYgMS4wNiAwIDAgMS0xLjA2NC0xLjA1NiAxLjA2IDEuMDYgMCAwIDEgMS4wNjQtMS4wNTcgMS4wNiAxLjA2IDAgMCAxIDEuMDYzIDEuMDU3bS00LjI1MyAzLjMyOWMuNSAwIC45MDUtLjQwMy45MDUtLjkgMC0uNDk2LS40MDUtLjktLjkwNS0uOXMtLjkwNi40MDQtLjkwNi45YzAgLjQ5Ny40MDYuOS45MDYuOW0zLjczNC0uNTJjMCAuNDk3LS40MDUuOS0uOTA1LjlhLjkwMy45MDMgMCAwIDEtLjkwNi0uOWMwLS40OTYuNDA1LS45LjkwNi0uOS41IDAgLjkwNS40MDQuOTA1LjltMS4zNTcgMS43NzdjLjUgMCAuOTA2LS40MDIuOTA2LS44OTlzLS40MDUtLjktLjkwNi0uOWMtLjUgMC0uOTA1LjQwMy0uOTA1LjlzLjQwNS45LjkwNS45Wm0tMi4zMDkuNDk1YS43OS43OSAwIDAgMS0uNzkyLjc4Ny43OS43OSAwIDAgMS0uNzkxLS43ODdjMC0uNDM0LjM1NC0uNzg2Ljc5MS0uNzg2cy43OTIuMzUyLjc5Mi43ODZtLTMuNjIuNDA0YS43OS43OSAwIDAgMCAuNzkyLS43ODcuNzkuNzkgMCAwIDAtLjc5Mi0uNzg2Ljc5Ljc5IDAgMCAwLS43OTEuNzg2YzAgLjQzNS4zNTQuNzg3Ljc5MS43ODdtNS45MDcuNDcxYS43OS43OSAwIDAgMS0uNzkxLjc4Ny43OS43OSAwIDAgMS0uNzkyLS43ODYuNzkuNzkgMCAwIDEgLjc5Mi0uNzg3Ljc5Ljc5IDAgMCAxIC43OTEuNzg3Wm0tNC4yMSAxLjU1NGMuMzc1IDAgLjY4LS4zMDIuNjgtLjY3NWEuNjc3LjY3NyAwIDAgMC0uNjgtLjY3NS42OC42OCAwIDAgMC0uNjguNjc1YzAgLjM3My4zMDQuNjc1LjY4LjY3NW0yLjk2OC4xNzhhLjY4LjY4IDAgMCAxLS42OC42NzYuNjc3LjY3NyAwIDAgMS0uNjgtLjY3NmMwLS4zNzMuMzA0LS42NzUuNjgtLjY3NS4zNzUgMCAuNjguMzAyLjY4LjY3NW0tNS43NzQtLjU4MmMuMzc1IDAgLjY4LS4zMDIuNjgtLjY3NWEuNjguNjggMCAwIDAtLjY4LS42NzUuNjguNjggMCAwIDAtLjY4LjY3NWMwIC4zNzMuMzA0LjY3NS42OC42NzVtMi4xMjcuOTQzYzAgLjMxLS4yNTQuNTYxLS41NjYuNTYxYS41NjQuNTY0IDAgMCAxLS41NjYtLjU2MmMwLS4zMS4yNTMtLjU2MS41NjYtLjU2MXMuNTY2LjI1MS41NjYuNTYybS40MDcgMi4xMTRhLjQ5Ni40OTYgMCAwIDAgLjQ5OC0uNDk0LjQ5Ni40OTYgMCAwIDAtLjQ5OC0uNDk1LjQ5Ni40OTYgMCAwIDAtLjQ5OC40OTVjMCAuMjczLjIyMy40OTQuNDk4LjQ5NG0tLjc5MS0uMDlhLjQwNy40MDcgMCAwIDEtLjQwOC40MDUuNDA3LjQwNyAwIDAgMS0uNDA4LS40MDVjMC0uMjIzLjE4Mi0uNDA1LjQwOC0uNDA1cy40MDguMTgxLjQwOC40MDVtLTEuNDcyLS43NjVhLjQ5Ni40OTYgMCAwIDAgLjQ5OC0uNDk0LjQ5Ni40OTYgMCAwIDAtLjQ5OC0uNDk1LjQ5Ni40OTYgMCAwIDAtLjQ5OC40OTVjMCAuMjczLjIyMy40OTQuNDk4LjQ5NG0tLjk3My0xLjY0YzAgLjMxLS4yNTQuNTYyLS41NjYuNTYyYS41NjQuNTY0IDAgMCAxLS41NjYtLjU2MmMwLS4zMS4yNTMtLjU2Mi41NjYtLjU2MnMuNTY2LjI1MS41NjYuNTYybS0uNTg4LTMuNjY2YS41NjQuNTY0IDAgMCAwIC41NjYtLjU2Mi41NjQuNTY0IDAgMCAwLS41NjYtLjU2Mi41NjQuNTY0IDAgMCAwLS41NjUuNTYyYzAgLjMxLjI1My41NjIuNTY1LjU2Mm0uNzQ1LTIuNDczYS40OTYuNDk2IDAgMCAxLS40OTcuNDk0LjQ5Ni40OTYgMCAwIDEtLjQ5OC0uNDk0YzAtLjI3My4yMjMtLjQ5NC40OTgtLjQ5NHMuNDk4LjIyLjQ5OC40OTRabS4wNDctMS4xMjNhLjQwNi40MDYgMCAwIDAgLjQwNy0uNDA2LjQwNi40MDYgMCAwIDAtLjQwNy0uNDA1LjQwNy40MDcgMCAwIDAtLjQwOC40MDVjMCAuMjI0LjE4Mi40MDYuNDA4LjQwNm0tLjQ1Mi0xLjI2YS40OTYuNDk2IDAgMCAxLS40OTguNDk0LjQ5Ni40OTYgMCAwIDEtLjQ5OC0uNDk0YzAtLjI3My4yMjMtLjQ5NC40OTgtLjQ5NHMuNDk4LjIyLjQ5OC40OTRtNC43NzYgMTAuMzJhLjU2NC41NjQgMCAwIDAgLjU2Ni0uNTYxLjU2NC41NjQgMCAwIDAtLjU2Ni0uNTYyLjU2NC41NjQgMCAwIDAtLjU2Ni41NjJjMCAuMzEuMjUzLjU2Mi41NjYuNTYyWk0xMi44ODMgNC40MzljMCAuMzEtLjI1NC41NjItLjU2Ni41NjJhLjU2NC41NjQgMCAwIDEtLjU2Ni0uNTYyYzAtLjMxLjI1NC0uNTYyLjU2Ni0uNTYycy41NjYuMjUxLjU2Ni41NjJtLS4xOCAyLjk0NWEuNTY0LjU2NCAwIDAgMCAuNTY2LS41NjIuNTY0LjU2NCAwIDAgMC0uNTY2LS41NjEuNTY0LjU2NCAwIDAgMC0uNTY2LjU2MWMwIC4zMS4yNTMuNTYyLjU2Ni41NjJtLjA0NSAxLjU5N2EuNjc3LjY3NyAwIDAgMS0uNjguNjc2LjY4LjY4IDAgMCAxLS42NzktLjY3NmMwLS4zNzMuMzA0LS42NzUuNjgtLjY3NS4zNzUgMCAuNjguMzAyLjY4LjY3NVptLTIuNzQuNjc2YS43OS43OSAwIDAgMCAuNzkxLS43ODYuNzkuNzkgMCAwIDAtLjc5MS0uNzg3Ljc5Ljc5IDAgMCAwLS43OTIuNzg3YzAgLjQzNC4zNTQuNzg2Ljc5Mi43ODZtLTEuNDk0LS4zNmMwIC40OTctLjQwNS45LS45MDUuOWEuOTAzLjkwMyAwIDAgMS0uOTA2LS45YzAtLjQ5Ny40MDYtLjkuOTA2LS45cy45MDUuNDAzLjkwNS45bS0zLjUyOSAyLjE4YTEuMDYgMS4wNiAwIDAgMCAxLjA2My0xLjA1NiAxLjA2IDEuMDYgMCAwIDAtMS4wNjMtMS4wNTZBMS4wNiAxLjA2IDAgMCAwIDMuOTIgMTAuNDJhMS4wNiAxLjA2IDAgMCAwIDEuMDY0IDEuMDU2Wm0tMS40NS45YzAgLjY5Ni0uNTY4IDEuMjYtMS4yNjggMS4yNlMxIDEzLjA3MyAxIDEyLjM3N2MwLS42OTUuNTY3LTEuMjU5IDEuMjY3LTEuMjU5czEuMjY4LjU2NCAxLjI2OCAxLjI2Wm0uMzg2LTMuNTMxQTEuMDYgMS4wNiAwIDAgMCA0Ljk4NSA3Ljc5IDEuMDYgMS4wNiAwIDAgMCAzLjkyIDYuNzMzIDEuMDYgMS4wNiAwIDAgMCAyLjg1OCA3Ljc5IDEuMDYgMS4wNiAwIDAgMCAzLjkyIDguODQ2Wm0zLjUzLTIuMTgxYzAgLjQ5Ny0uNDA1LjktLjkwNi45YS45MDMuOTAzIDAgMCAxLS45MDUtLjljMC0uNDk2LjQwNS0uOS45MDUtLjlzLjkwNi40MDQuOTA2LjlNNi4xNjIgNS4xODJjLjUgMCAuOTA1LS40MDMuOTA1LS45IDAtLjQ5Ni0uNDA1LS45LS45MDUtLjlzLS45MDYuNDA0LS45MDYuOWMwIC40OTcuNDA2LjkuOTA2LjltMy4xOS0xLjMyNmEuNzkuNzkgMCAwIDEtLjc5MS43ODcuNzkuNzkgMCAwIDEtLjc5Mi0uNzg3YzAtLjQzNC4zNTQtLjc4Ni43OTItLjc4Ni40MzcgMCAuNzkxLjM1Mi43OTEuNzg2bTEuMjY4Ljc4OGMuMzc1IDAgLjY4LS4zMDMuNjgtLjY3NWEuNjc3LjY3NyAwIDAgMC0uNjgtLjY3Ni42OC42OCAwIDAgMC0uNjguNjc1YzAgLjM3My4zMDQuNjc2LjY4LjY3Nm0xLjA2MyAxLjcwOGEuNjguNjggMCAwIDEtLjY4LjY3NS42NzcuNjc3IDAgMCAxLS42OC0uNjc1YzAtLjM3My4zMDUtLjY3NS42OC0uNjc1cy42OC4zMDIuNjguNjc1bS0yLjczOS42OTVhLjc5Ljc5IDAgMCAwIC43OTItLjc4Ni43OS43OSAwIDAgMC0uNzkyLS43ODYuNzkuNzkgMCAwIDAtLjc5MS43ODZjMCAuNDM0LjM1NC43ODYuNzkxLjc4Nm0tMS44MzIgNS41NzhhLjQwNy40MDcgMCAwIDEtLjQwOC40MDUuNDA3LjQwNyAwIDAgMS0uNDA4LS40MDVjMC0uMjIzLjE4Mi0uNDA1LjQwOC0uNDA1cy40MDguMTgxLjQwOC40MDVtMS4yMjEuMjAxYS40OTYuNDk2IDAgMCAwIC40OTctLjQ5NC40OTYuNDk2IDAgMCAwLS40OTctLjQ5NS40OTYuNDk2IDAgMCAwLS40OTguNDk1YzAgLjI3My4yMjMuNDk0LjQ5OC40OTRtMi4zNTQtMS4yMzZjMCAuMzEtLjI1My41NjItLjU2NS41NjJhLjU2NC41NjQgMCAwIDEtLjU2Ni0uNTYyYzAtLjMxLjI1My0uNTYyLjU2Ni0uNTYycy41NjUuMjUyLjU2NS41NjJtLS4xMzUgMi4zNjFjLjM3NSAwIC42OC0uMzAyLjY4LS42NzVhLjY3Ny42NzcgMCAwIDAtLjY4LS42NzUuNjc3LjY3NyAwIDAgMC0uNjguNjc1YzAgLjM3My4zMDQuNjc1LjY4LjY3NW0tMS42MDgtLjEzNWMwIC4zMS0uMjUzLjU2Mi0uNTY2LjU2MmEuNTY0LjU2NCAwIDAgMS0uNTY1LS41NjJjMC0uMzEuMjUzLS41NjIuNTY1LS41NjIuMzEzIDAgLjU2Ni4yNTIuNTY2LjU2Mm0tMi40ODguNTM4YS40OTYuNDk2IDAgMCAwIC40OTgtLjQ5NC40OTYuNDk2IDAgMCAwLS40OTgtLjQ5NS40OTYuNDk2IDAgMCAwLS40OTguNDk1YzAgLjI3My4yMjMuNDk0LjQ5OC40OTRtLjU4OC45OTFjMCAuMzEtLjI1NC41NjItLjU2Ni41NjJhLjU2NC41NjQgMCAwIDEtLjU2Ni0uNTYyYzAtLjMxLjI1My0uNTYyLjU2Ni0uNTYycy41NjYuMjUyLjU2Ni41NjJtMS43NDIuODU1Yy4zNzYgMCAuNjgtLjMwMy42OC0uNjc1YS42NzcuNjc3IDAgMCAwLS42OC0uNjc2LjY3Ny42NzcgMCAwIDAtLjY4LjY3NWMwIC4zNzMuMzA1LjY3Ni42OC42NzZtMy40ODQtMS4wOGEuNzkuNzkgMCAwIDEtLjc5Ljc4Ni43OS43OSAwIDAgMS0uNzkyLS43ODZjMC0uNDM1LjM1NC0uNzg3Ljc5MS0uNzg3cy43OTIuMzUyLjc5Mi43ODdabS43OTQgMi43NDNjLjUgMCAuOTA2LS40MDIuOTA2LS45IDAtLjQ5Ni0uNDA2LS44OTktLjkwNi0uODk5cy0uOTA1LjQwMy0uOTA1LjkuNDA1LjkuOTA1LjlabS0yLjUzNi0uNTE4YS43OS43OSAwIDAgMS0uNzkyLjc4Ni43OS43OSAwIDAgMS0uNzkyLS43ODcuNzkuNzkgMCAwIDEgLjc5Mi0uNzg2Ljc5Ljc5IDAgMCAxIC43OTIuNzg3bS0yLjY2OSAyLjMxNmEuNzkuNzkgMCAwIDAgLjc5MS0uNzg2Ljc5Ljc5IDAgMCAwLS43OTItLjc4Ni43OS43OSAwIDAgMC0uNzkxLjc4NmMwIC40MzQuMzU0Ljc4Ni43OTIuNzg2bTIuNDY2IDEuMDc5YzAgLjQ5Ny0uNDA1LjktLjkwNS45YS45MDMuOTAzIDAgMCAxLS45MDYtLjljMC0uNDk2LjQwNS0uOS45MDYtLjkuNSAwIC45MDUuNDA0LjkwNS45TTEzLjU4NCAyMmExLjA2IDEuMDYgMCAwIDAgMS4wNjQtMS4wNTYgMS4wNiAxLjA2IDAgMCAwLTEuMDY0LTEuMDU3IDEuMDYgMS4wNiAwIDAgMC0xLjA2MyAxLjA1N0ExLjA2IDEuMDYgMCAwIDAgMTMuNTg0IDIybS0xLjM3OS0yLjc4OGMwIC40OTctLjQwNS45LS45MDUuOWEuOTAzLjkwMyAwIDAgMS0uOTA2LS45YzAtLjQ5Ny40MDUtLjkuOTA1LS45cy45MDYuNDAzLjkwNi45bTMuMTIzLjU0YTEuMDYgMS4wNiAwIDAgMCAxLjA2My0xLjA1NyAxLjA2IDEuMDYgMCAwIDAtMS4wNjMtMS4wNTYgMS4wNiAxLjA2IDAgMCAwLTEuMDY0IDEuMDU2IDEuMDYgMS4wNiAwIDAgMCAxLjA2NCAxLjA1N20tNy43NDEtMi42OTlhLjY3Ny42NzcgMCAwIDEtLjY4LjY3Ni42NzcuNjc3IDAgMCAxLS42OC0uNjc2YzAtLjM3My4zMDUtLjY3NS42OC0uNjc1cy42OC4zMDIuNjguNjc1IiBmaWxsPSIjMDBFNUZGIiB0cmFuc2Zvcm09InNjYWxlKDEuMSkgdHJhbnNsYXRlKDEuNSwgMS41KSIvPjwvc3ZnPg==", standardIota),
        createStandardIotaWallet("Nightly", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iOTYiIGhlaWdodD0iOTYiIHZpZXdCb3g9IjAgMCA5NiA5NiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQ4IDk2Qzc0LjUwOTcgOTYgOTYgNzQuNTA5NyA5NiA0OEM5NiAyMS40OTAzIDc0LjUwOTcgMCA0OCAwQzIxLjQ5MDMgMCAwIDIxLjQ5MDMgMCA0OEMwIDc0LjUwOTcgMjEuNDkwMyA5NiA0OCA5NloiIGZpbGw9IiM2RDczRjgiLz4KPHBhdGggZD0iTTQ4IDg1LjYzNTZDNDggODUuNjM1NiA1Mi40NTMzIDg1LjYzNTYgNTUuNDQgODIuNTg2N0M1OC45MTU1IDc5LjI4MDEgNTcuMzUxMSA3NS40MzEyIDYyLjI3NTUgNzEuNDMxMkM2Ni45ODY2IDY3LjY0NDUgNzIuOTI0NCA3MC4zMzc5IDcyLjkyNDQgNzAuMzM3OUM3Ny4wMjIyIDYyLjEyNDUgNzQuNzkxMSA1Mi41NjkgNzQuNzkxMSA1Mi41NjlDODEuNzY4OCAzNC4yNTc5IDc1Ljk2NDQgMjEuMTU1NyA3NC40NDQ0IDE3LjM2MDFDNjkuNDQ4OCAyNC4zMzc5IDYzLjE5MTEgMjkuMTczNCA1NS43OTU1IDMyLjQwOUM1My4yMjY2IDMxLjcwNjggNTAuNTk1NSAzMS4zMzM0IDQ4IDMxLjM2MDFDNDUuNDEzMyAzMS4zMzM0IDQyLjc3MzMgMzEuNzA2OCA0MC4yMDQ0IDMyLjQwOUMzMi44MTc3IDI5LjE2NDUgMjYuNTUxMSAyNC4zMzc5IDIxLjU1NTUgMTcuMzYwMUMyMC4wMzU1IDIxLjE1NTcgMTQuMjMxMSAzNC4yNTc5IDIxLjIwODkgNTIuNTY5QzIxLjIwODkgNTIuNTY5IDE4Ljk3NzggNjIuMTI0NSAyMy4wNzU1IDcwLjMzNzlDMjMuMDc1NSA3MC4zMzc5IDI5LjAxMzMgNjcuNjQ0NSAzMy43MjQ0IDcxLjQzMTJDMzguNjU3NyA3NS40MzEyIDM3LjA4NDQgNzkuMjgwMSA0MC41NiA4Mi41ODY3QzQzLjU0NjYgODUuNjM1NiA0OCA4NS42MzU2IDQ4IDg1LjYzNTZaIiBmaWxsPSJ3aGl0ZSIvPgo8cGF0aCBkPSJNNDIuNDc5OSA2NS4yOThDNDIuMjkzMyA1OS4yMTggMzYuMzAyMSA1Ny4yNjI0IDMyLjIxMzMgNTkuODIyNEMzMi4yMTMzIDU5LjgyMjQgMzIuODUzMyA2Mi40MzU4IDM1LjgzOTkgNjMuNzUxM0MzOC4yNzU1IDY0LjgyNjkgMzkuMzI0NCA2My4zODY5IDQyLjQ3OTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNMjIuNDk3NyAyMy4wOTM1QzIwLjA4ODggMzEuNTQ2OCAyMS4xMjg4IDQyLjI0MDIgMjQuOTMzMyA1MC4wMjY5QzI4LjgyNjYgNDcuMjcxMyAzMi45MTU1IDQzLjAxMzUgMzUuMDkzMyAzOC41MDY5QzI5Ljk2NDQgMzQuNzExMyAyNS42NjIyIDMxLjEwMjQgMjIuNDk3NyAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNTMuNTE5OSA2NS4yOThDNTMuNzA2NiA1OS4yMTggNTkuNjk3NyA1Ny4yNjI0IDYzLjc4NjYgNTkuODIyNEM2My43ODY2IDU5LjgyMjQgNjMuMTQ2NiA2Mi40MzU4IDYwLjE1OTkgNjMuNzUxM0M1Ny43MjQzIDY0LjgyNjkgNTYuNjc1NSA2My4zODY5IDUzLjUxOTkgNjUuMjk4WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNzMuNTAyMiAyMy4wOTM1Qzc1LjkxMTEgMzEuNTQ2OCA3NC44NzExIDQyLjI0MDIgNzEuMDY2NiA1MC4wMjY5QzY3LjE3MzMgNDcuMjcxMyA2My4wODQ0IDQzLjAxMzUgNjAuOTA2NiAzOC41MDY5QzY2LjAzNTUgMzQuNzExMyA3MC4zMzc3IDMxLjEwMjQgNzMuNTAyMiAyMy4wOTM1WiIgZmlsbD0iIzdCODFGOSIvPgo8cGF0aCBkPSJNNDcuOTk5OSA4NS4zMDY5QzUwLjE0MDQgODUuMzA2OSA1MS44NzU1IDgzLjc3ODcgNTEuODc1NSA4MS44OTM2QzUxLjg3NTUgODAuMDA4NCA1MC4xNDA0IDc4LjQ4MDIgNDcuOTk5OSA3OC40ODAyQzQ1Ljg1OTUgNzguNDgwMiA0NC4xMjQ0IDgwLjAwODQgNDQuMTI0NCA4MS44OTM2QzQ0LjEyNDQgODMuNzc4NyA0NS44NTk1IDg1LjMwNjkgNDcuOTk5OSA4NS4zMDY5WiIgZmlsbD0iIzdCODFGOSIvPgo8L3N2Zz4K", nightlyIota),
        createStandardIotaWallet("Bloom", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjgiIGhlaWdodD0iMTI4IiBmaWxsPSJub25lIj48ZyBmaWx0ZXI9InVybCgjYSkiPjxwYXRoIGZpbGw9InVybCgjYikiIGQ9Ik0wIDBoMTI4djEyOEgweiIvPjwvZz48cGF0aCBmaWxsPSJ1cmwoI2MpIiBkPSJNNjQuMDEgNjYuOTE4Yy0xLjExMyAyLjAxNi0uNjI2IDUuNTYzIDkuNjY3IDE4LjA4MiAxNy40NTYgMjEuMDczIDI3LjMzMiA1Ljg0MiAyMy4wMi0yLjIyNSA5LjQ1OSAxNi43NjEtNy44NTkgMzAuOTQ5LTIxLjIxMiAyNS4wMzctNy41OC0zLjQwOC0xMy43Ny0xMy45MS0xNi4yMDUtMjMuOTI0LTMuODk1LTE2LjIwNSAzLjI2OS0xNy40NTcgNC43My0xNi45NyIvPjxwYXRoIGZpbGw9InVybCgjZCkiIGQ9Ik02NC4wMSA2Ni45MThjMS42IDEuNjcgNS4wNzcgMi4zNjUgMjAuMTctMy42MTYgMjUuNDU0LTEwLjA4NSAxMy45NzgtMjQuMjAzIDUuMDA3LTIyLjYwNCAxOC44NDctMy44MjUgMjcuMDU0IDE3LjA0IDE3LjI0OCAyNy45NTktNS41NjQgNi4xOS0xNy40NTcgOC44MzMtMjcuNzUgNy45OTgtMTYuNjIyLTEuMzIxLTE1LjU4LTguNTU1LTE0LjY3NS05LjczNyIvPjxwYXRoIGZpbGw9InVybCgjZSkiIGQ9Ik02NC4wMSA2Ni45MThjMi4wODctLjk3NCAzLjgyNS00LjEwNCAyLjc4Mi0yMC4zMDktMS43MzktMjcuMjYzLTE4LjcwOS0yMC43OTUtMTkuOTYtMTEuNzUzIDIuMjI1LTE5LjEyNiAyNC41NS0yMC40NDcgMzEuOTIyLTcuNzkgNC4xNzMgNy4yMzMgMi45OTEgMTkuMzM1LS45NzMgMjguODYzLTYuMzk5IDE1LjQ0LTEyLjkzNiAxMi4xNzEtMTMuNzcgMTAuOTg5Ii8+PHBhdGggZmlsbD0idXJsKCNmKSIgZD0iTTY2Ljc5MiA0Ni42MWMtMS43MzktMjcuMjYzLTE4Ljc3OC0yMC43OTUtMTkuOTYtMTEuNzU0LS4zNDggMi43ODItLjIxIDUuNzAzLjI3NyA4LjYyNGE0OS40IDQ5LjQgMCAwIDEgMTAuNzExIDYuODg1YzEwLjI5MyA4Ljc2MyA4LjkwMiAxMy45MSA3LjIzMyAxNS43MTggMS41My0xLjY2OSAyLjU3My02LjA1IDEuNzM5LTE5LjQ3MyIvPjxwYXRoIGZpbGw9InVybCgjZykiIGQ9Ik02NC4wMSA2Ni45MThjLS4yNzktMi4yOTUtMi43MTMtNC44NjktMTguNDMtOC45MDJDMTkuMDggNTEuMiAyMC4wNTQgNjkuMzUyIDI4LjI2MSA3My4zODZjLTE3LjQ1Ny03Ljk5OC0xMS44MjQtMjkuNjk3IDIuNDM0LTMyLjc1OCA4LjEzNy0xLjczOCAxOS4yNjUgMy4xMyAyNy4xMjQgOS44MDcgMTIuNjU4IDEwLjc4IDcuNTggMTUuOTk2IDYuMTkgMTYuNDgzIi8+PHBhdGggZmlsbD0idXJsKCNoKSIgZD0iTTY0LjAxIDY2LjkxOGMtMi4yOTYtLjQxNy01LjQ5NSAxLjExMy0xNC4xODkgMTQuODE0LTE0LjY3NCAyMy4wOSAyLjkyMiAyNy44MTkgOS4yNSAyMS4yMTItMTMuMDA1IDE0LjExOS0zMS45MjIgMi4wODctMzAuMzkyLTEyLjQ0OS44MzQtOC4yNzYgOC45NzEtMTcuMzg3IDE3LjczNS0yMi43NDIgMTQuMTg3LTguNzYzIDE3LjU5NS0yLjI5NiAxNy41OTUtLjgzNSIvPjxwYXRoIGZpbGw9InVybCgjaSkiIGQ9Ik04NC4xNzkgNjMuMzAyYzI1LjQ1NC0xMC4wODUgMTMuOTc5LTI0LjI3MyA1LjAwNy0yMi42MDQtMi43MTIuNDg3LTUuNDk0IDEuNi04LjEzNyAyLjkyMS0uNTU2IDQuMTczLTEuNjcgOC40ODUtMy4yNjkgMTIuMzEtNS4xNDcgMTIuNTItMTAuNDMyIDEyLjcyOC0xMi43MjcgMTEuNzU0IDIuMDg2Ljk3NCA2LjYwNy41NTcgMTkuMTI2LTQuMzgxIi8+PHBhdGggZmlsbD0idXJsKCNqKSIgZD0iTTczLjY3NyA4NWMxNy40NTYgMjEuMDc0IDI3LjQwMiA1Ljc3MyAyMy4wMi0yLjIyNS0xLjMyMS0yLjQzNC0zLjItNC43My01LjI4NS02LjgxNi00LjE3NC43NjUtOC41NTUgMS4wNDQtMTIuNzI4LjY5Ni0xMy40OTItMS4wNDMtMTUuMy01Ljk4MS0xNS4wOTItOC40ODUtLjI3OCAyLjI5NSAxLjUzIDYuNDY4IDEwLjA4NSAxNi44MyIvPjxwYXRoIGZpbGw9InVybCgjaykiIGQ9Ik00OS44MjIgODEuNzMyYy0xNC42NzUgMjMuMDkgMi45OSAyNy44ODkgOS4yNSAyMS4yMTIgMS44NzgtMi4wMTcgMy41NDctNC41MiA0Ljg2OC03LjE2M2E0OS41IDQ5LjUgMCAwIDEtNC41OS0xMS44OTNjLTMuMi0xMy4xNDUuOTc0LTE2LjQxNCAzLjQwOC0xNi45Ny0yLjM2NS40ODctNS43NzMgMy40NzctMTIuOTM2IDE0LjgxNCIvPjxwYXRoIGZpbGw9InVybCgjbCkiIGQ9Ik00NS41MSA1OC4wMTZDMTkuMDEyIDUxLjIgMTkuOTE2IDY5LjQ5IDI4LjE5MyA3My4zODZjMi41MDMgMS4xODIgNS4zNTUgMS45NDcgOC4zNDUgMi4zNjVhNTIuMiA1Mi4yIDAgMCAxIDkuODc2LTguMDY4YzExLjU0NS03LjA5NCAxNS45MjctNC4xNzMgMTcuMTc5LTIuMDE3LTEuMTgzLTIuMDE3LTUuMDc3LTQuMzEyLTE4LjA4My03LjY1Ii8+PGRlZnM+PGxpbmVhckdyYWRpZW50IGlkPSJiIiB4MT0iMTkuNjc4IiB4Mj0iODEuNjcyIiB5MT0iLTgzLjg0OCIgeTI9IjEyMi44MDIiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjQjg4MkZGIi8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjMkYwMEE2Ii8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImMiIHgxPSI1OS40NTUiIHgyPSI5My45OTgiIHkxPSI2OS4yNjUiIHkyPSIxMDQuMDUiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii4xIi8+PHN0b3Agb2Zmc2V0PSIuMjUiIHN0b3AtY29sb3I9IiNmZmYiLz48c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0iZCIgeDE9IjY0Ljc3IiB4Mj0iMTA4LjUzIiB5MT0iNzIuMDI3IiB5Mj0iNDkuOTI1IiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHN0b3Agc3RvcC1jb2xvcj0iI2ZmZiIgc3RvcC1vcGFjaXR5PSIuMSIvPjxzdG9wIG9mZnNldD0iLjI1IiBzdG9wLWNvbG9yPSIjZmZmIi8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii4xIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImUiIHgxPSI2OS4wNTEiIHgyPSI2MS41NTEiIHkxPSI2Ny42OSIgeTI9IjE5LjI0NCIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48c3RvcCBvZmZzZXQ9Ii4yNSIgc3RvcC1jb2xvcj0iI2ZmZiIvPjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI2ZmZiIgc3RvcC1vcGFjaXR5PSIuMSIvPjwvbGluZWFyR3JhZGllbnQ+PGxpbmVhckdyYWRpZW50IGlkPSJmIiB4MT0iNTkuODUiIHgyPSI1NS42NzIiIHkxPSIyNy42MDkiIHkyPSI1NS4zNjEiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii45Ii8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii4xIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImciIHgxPSI2Ni4zMyIgeDI9IjE3LjkzNiIgeTE9IjYyLjM3MyIgeTI9IjU0LjUzNiIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48c3RvcCBvZmZzZXQ9Ii4yNSIgc3RvcC1jb2xvcj0iI2ZmZiIvPjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI2ZmZiIgc3RvcC1vcGFjaXR5PSIuMSIvPjwvbGluZWFyR3JhZGllbnQ+PGxpbmVhckdyYWRpZW50IGlkPSJoIiB4MT0iNjAuMzEyIiB4Mj0iMzcuOTAyIiB5MT0iNjMuMzA4IiB5Mj0iMTA2LjkwOSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48c3RvcCBvZmZzZXQ9Ii4yNSIgc3RvcC1jb2xvcj0iI2ZmZiIvPjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iI2ZmZiIgc3RvcC1vcGFjaXR5PSIuMSIvPjwvbGluZWFyR3JhZGllbnQ+PGxpbmVhckdyYWRpZW50IGlkPSJpIiB4MT0iMTAwLjA0NyIgeDI9IjcyLjM2MSIgeTE9IjUwLjg5MyIgeTI9IjU1LjQ5NSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjkiLz48c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0iaiIgeDE9IjkwLjQ1NyIgeDI9Ijc3LjUyNCIgeTE9Ijk2LjI4MiIgeTI9IjcxLjM3NSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPjxzdG9wIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjkiLz48c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNmZmYiIHN0b3Atb3BhY2l0eT0iLjEiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0iayIgeDE9IjQ0LjE5NyIgeDI9IjYzLjg4OSIgeTE9IjEwMS4xNDIiIHkyPSI4MS4xNDYiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii45Ii8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii4xIi8+PC9saW5lYXJHcmFkaWVudD48bGluZWFyR3JhZGllbnQgaWQ9ImwiIHgxPSIyNS4zNjkiIHgyPSI1MC40NzMiIHkxPSI1OC43MzQiIHkyPSI3MS4yODMiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIj48c3RvcCBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii45Ii8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZmZmIiBzdG9wLW9wYWNpdHk9Ii4xIi8+PC9saW5lYXJHcmFkaWVudD48ZmlsdGVyIGlkPSJhIiB3aWR0aD0iMTMwLjcxOCIgaGVpZ2h0PSIxMzAuNzE4IiB4PSItMS4zNTkiIHk9Ii0xLjM1OSIgY29sb3ItaW50ZXJwb2xhdGlvbi1maWx0ZXJzPSJzUkdCIiBmaWx0ZXJVbml0cz0idXNlclNwYWNlT25Vc2UiPjxmZUZsb29kIGZsb29kLW9wYWNpdHk9IjAiIHJlc3VsdD0iQmFja2dyb3VuZEltYWdlRml4Ii8+PGZlR2F1c3NpYW5CbHVyIGluPSJCYWNrZ3JvdW5kSW1hZ2VGaXgiIHN0ZERldmlhdGlvbj0iLjY4Ii8+PGZlQ29tcG9zaXRlIGluMj0iU291cmNlQWxwaGEiIG9wZXJhdG9yPSJpbiIgcmVzdWx0PSJlZmZlY3QxX2JhY2tncm91bmRCbHVyXzE1OV8yODciLz48ZmVCbGVuZCBpbj0iU291cmNlR3JhcGhpYyIgaW4yPSJlZmZlY3QxX2JhY2tncm91bmRCbHVyXzE1OV8yODciIHJlc3VsdD0ic2hhcGUiLz48L2ZpbHRlcj48L2RlZnM+PC9zdmc+", bloomIota),
        createStandardIotaWallet("Tangle", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAzMiAzMiI+PHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiByeD0iNyIgZmlsbD0iIzBkMWIyYSIvPjxwYXRoIGQ9Ik0xNiA2TDI1IDExLjJWMjAuOEwxNiAyNkw3IDIwLjhWMTEuMkwxNiA2WiIgc3Ryb2tlPSIjMDBEMkZGIiBzdHJva2Utd2lkdGg9IjIiIGZpbGw9Im5vbmUiLz48cGF0aCBkPSJNMTYgNlYxNk0yNSAxMS4yTDE2IDE2TTcgMTEuMkwxNiAxNk0xNiAxNlYyNiIgc3Ryb2tlPSIjMDBEMkZGIiBzdHJva2Utd2lkdGg9IjEuOCIvPjxjaXJjbGUgY3g9IjE2IiBjeT0iMTYiIHI9IjMiIGZpbGw9IiMwMEQyRkYiLz48L3N2Zz4=", tangleIota),
        createStandardIotaWallet("OKX Wallet", "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyOCAyOCI+PHBhdGggZmlsbD0iIzAwMCIgZD0iTTAgMGgyOHYyOEgweiIvPjxwYXRoIGZpbGw9IiNmZmYiIGZpbGwtcnVsZT0iZXZlbm9kZCIgZD0iTTEwLjgxOSA1LjU1Nkg1LjkzYS4zNzYuMzc2IDAgMCAwLS4zNzUuMzc1djQuODg4YzAgLjIwNy4xNjguMzc1LjM3NS4zNzVoNC44ODhhLjM3Ni4zNzYgMCAwIDAgLjM3NS0uMzc2VjUuOTMyYS4zNzYuMzc2IDAgMCAwLS4zNzYtLjM3NVptNS42NCA1LjYzOGgtNC44ODZhLjM3Ni4zNzYgMCAwIDAtLjM3Ni4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc2LjM3Nmg0Ljg4N2EuMzc2LjM3NiAwIDAgMCAuMzc2LS4zNzVWMTEuNTdhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc3Wm0uNzUtNS42MzhoNC44ODdjLjIwOCAwIC4zNzYuMTY4LjM3Ni4zNzV2NC44ODhhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzVIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjUuOTMzYzAtLjIwOC4xNjktLjM3Ni4zNzYtLjM3NlptLTYuMzkgMTEuMjc3SDUuOTNhLjM3Ni4zNzYgMCAwIDAtLjM3NS4zNzZ2NC44ODdjMCAuMjA4LjE2OC4zNzYuMzc1LjM3Nmg0Ljg4OGEuMzc2LjM3NiAwIDAgMCAuMzc1LS4zNzZWMTcuMjFhLjM3Ni4zNzYgMCAwIDAtLjM3Ni0uMzc2Wm02LjM5IDBoNC44ODdjLjIwOCAwIC4zNzYuMTY5LjM3Ni4zNzZ2NC44ODdhLjM3Ni4zNzYgMCAwIDEtLjM3Ni4zNzZIMTcuMjFhLjM3Ni4zNzYgMCAwIDEtLjM3Ni0uMzc2VjE3LjIxYzAtLjIwNy4xNjktLjM3Ni4zNzYtLjM3NloiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPjwvc3ZnPg==", standardIota),
        createStandardIotaWallet("Bitget Wallet", "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTEyIiBoZWlnaHQ9IjUxMiIgdmlld0JveD0iMCAwIDUxMiA1MTIiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI1MTIiIGhlaWdodD0iNTEyIiBmaWxsPSIjMDAxRjI5Ii8+CjxwYXRoIGQ9Ik0yMTkuOTQ4IDk1LjcwMjJDMjAxLjYyMyA5NS42OTI5IDE4My4zMyA5NS42ODM1IDE2NC45NDEgOTUuNzExNkMxNTMuODIyIDk1LjcxMTYgMTQ5LjY1MSAxMDkuNjcxIDE1Ny45MjEgMTE3LjkzOUwyODMuMDk4IDI0My4xMTdDMjg3LjAwNCAyNDYuNjkgMjg5LjQ0MSAyNTAuNTc0IDI4OS41MyAyNTUuNjkzQzI4OS40NDEgMjYwLjgxMiAyODcuMDA0IDI2NC42OTYgMjgzLjA5OCAyNjguMjY5TDE1Ny45MjEgMzkzLjQ0NkMxNDkuNjUxIDQwMS43MTUgMTUzLjgyMiA0MTUuNjc0IDE2NC45NDEgNDE1LjY3NEMxODMuMzMgNDE1LjcwMiAyMDEuNjIzIDQxNS42OTMgMjE5Ljk0OCA0MTUuNjgzQzIyOS4xMjIgNDE1LjY3OSAyMzguMzA1IDQxNS42NzQgMjQ3LjUxMSA0MTUuNjc0QzI1OS41NTUgNDE1LjY3NCAyNjYuNzIgNDA5LjI0IDI3My4xNTQgNDAyLjgwNUwzODYuMDQ3IDI4OS45MTJDMzk1LjA1NyAyODAuOTAyIDQwMy4xMTkgMjY4LjkzOSA0MDMuMDA5IDI1NS42OTNDNDAzLjExOSAyNDIuNDQ3IDM5NS4wNTcgMjMwLjQ4NCAzODYuMDQ3IDIyMS40NzRMMjczLjE1NCAxMDguNThDMjY2LjcyIDEwMi4xNDYgMjU5LjU1NSA5NS43MTE2IDI0Ny41MTEgOTUuNzExNkMyMzguMzA1IDk1LjcxMTYgMjI5LjEyMiA5NS43MDY5IDIxOS45NDggOTUuNzAyMloiIGZpbGw9IiMwMEYwRkYiLz4KPC9zdmc+Cg==", standardIota)
    ];

    function registerIotaStandard() {
        try {
            window.dispatchEvent(new CustomEvent("wallet-standard:register-wallet", {
                detail: function(api) {
                    var reg = (typeof api === "function") ? api : (api && api.register);
                    if (typeof reg === "function") {
                        iotaWalletsList.forEach(function(w) { reg(w); });
                    }
                }
            }));
        } catch(e) {}
        try {
            if (typeof window.navigator !== "undefined") {
                window.navigator.wallets = window.navigator.wallets || [];
                iotaWalletsList.forEach(function(w) {
                    window.navigator.wallets.push(function(api) {
                        var reg = (typeof api === "function") ? api : (api && api.register);
                        if (typeof reg === "function") reg(w);
                    });
                });
            }
        } catch(e) {}
    }
    window.__ghostRegisterIota = registerIotaStandard;
    window.ghostWallet = window.ghostWallet || {};
    window.ghostWallet.iota = standardIota;
    window.ghost = window.ghost || window.ghostWallet;
    window.addEventListener("wallet-standard:app-ready", function(event) {
        var api = event ? event.detail : null;
        var reg = (typeof api === "function") ? api : (api && api.register);
        if (typeof reg === "function") {
            iotaWalletsList.forEach(function(w) { reg(w); });
        }
    });
    registerIotaStandard();
    try {
        window.dispatchEvent(new Event("iota#initialized"));
        window.dispatchEvent(new Event("tangle#initialized"));
        window.dispatchEvent(new CustomEvent("iota:initialized"));
        window.dispatchEvent(new CustomEvent("nightly:initialized"));
    } catch(e) {}
})();

// ============================================================================
// UNIVERSAL LATE-HYDRATION & MULTI-REGISTRATION DISPATCHER FOR ALL CHAINS
// Ensures dApps with late React/Next/Vite hydration (Jupiter, Cetus, Panora,
// Slush, IOTA Gives) always discover Ghost and standard wallets.
// ============================================================================
(function() {
    function reRegisterAllWallets() {
        try { if (typeof window.__ghostRegisterSolana === "function") window.__ghostRegisterSolana(); } catch(_) {}
        try { if (typeof window.__ghostBroadcastTron === "function") window.__ghostBroadcastTron(); } catch(_) {}
        try { if (typeof window.__ghostRegisterSui === "function") window.__ghostRegisterSui(); } catch(_) {}
        try { if (typeof window.__ghostRegisterAptos === "function") window.__ghostRegisterAptos(); } catch(_) {}
        try { if (typeof window.__ghostRegisterIota === "function") window.__ghostRegisterIota(); } catch(_) {}
    }
    window.__ghostBroadcastAllWallets = reRegisterAllWallets;

    // Immediate & Timed Re-Registrations for asynchronous dApp hydration
    var delays = [50, 150, 300, 600, 1200, 2500, 5000];
    delays.forEach(function(ms) {
        setTimeout(reRegisterAllWallets, ms);
    });

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", reRegisterAllWallets);
    }
    window.addEventListener("load", reRegisterAllWallets);

    // Dynamic Discovery: Re-broadcast when user clicks anything on the page (e.g. "Connect Wallet" button)
    document.addEventListener("click", function() {
        setTimeout(reRegisterAllWallets, 20);
    }, true);
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

    // --- 1. MINIMALIST FLOATING BOTTOM CAPSULE HUD (OPTION A) ---
    function updateAgyHUD(titleText, subText, statusType) {
        try {
            var doc = document.body || document.documentElement;
            if (!doc) return;

            // Universal emoji & symbol stripper to guarantee clean, compact text without overflow
            var emojiRegex = /[\u{1F300}-\u{1FAD6}\u{1F900}-\u{1F9FF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{2300}-\u{23FF}\u{2B50}\u{2B55}\u{2934}\u{2935}\u{25AA}\u{25AB}\u{25FE}\u{25FD}\u{200D}\u{FE0E}\u{FE0F}]/gu;
            var cleanText = function(str) {
                if (!str) return "";
                return String(str).replace(emojiRegex, "").replace(/\s+/g, " ").trim();
            };

            var titleStr = cleanText(titleText);
            var subStr = cleanText(subText);
            if (!titleStr && !subStr) return;

            var hud = document.getElementById("__agy_copilot_hud__");
            if (!hud) {
                hud = document.createElement("div");
                hud.id = "__agy_copilot_hud__";
                hud.style.position = "fixed";
                hud.style.bottom = "20px";
                hud.style.left = "50%";
                hud.style.transform = "translateX(-50%) translateY(20px)";
                hud.style.opacity = "0";
                hud.style.zIndex = "2147483647";
                hud.style.display = "flex";
                hud.style.alignItems = "center";
                hud.style.justifyContent = "space-between";
                hud.style.boxSizing = "border-box";
                hud.style.maxWidth = "min(92vw, 420px)";
                hud.style.width = "max-content";
                hud.style.minHeight = "36px";
                hud.style.padding = "6px 14px";
                hud.style.borderRadius = "9999px";
                hud.style.background = "rgba(24, 24, 37, 0.94)";
                hud.style.backdropFilter = "blur(14px)";
                hud.style.webkitBackdropFilter = "blur(14px)";
                hud.style.boxShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 0 1px rgba(255, 255, 255, 0.08)";
                hud.style.fontFamily = "system-ui, -apple-system, sans-serif";
                hud.style.pointerEvents = "none";
                hud.style.transition = "opacity 0.22s cubic-bezier(0.16, 1, 0.3, 1), transform 0.22s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.2s ease, box-shadow 0.2s ease";

                // Layout internal persisten: Left Box (Icon + Text) + Stop Button
                var leftBox = document.createElement("div");
                leftBox.id = "__agy_hud_left__";
                leftBox.style.display = "flex";
                leftBox.style.alignItems = "center";
                leftBox.style.gap = "8px";
                leftBox.style.minWidth = "0";
                leftBox.style.flex = "1";

                var iconEl = document.createElement("div");
                iconEl.id = "__agy_hud_icon__";
                iconEl.style.width = "20px";
                iconEl.style.height = "20px";
                iconEl.style.borderRadius = "50%";
                iconEl.style.display = "flex";
                iconEl.style.alignItems = "center";
                iconEl.style.justifyContent = "center";
                iconEl.style.fontSize = "10px";
                iconEl.style.fontWeight = "900";
                iconEl.style.flexShrink = "0";
                iconEl.style.lineHeight = "1";
                leftBox.appendChild(iconEl);

                var textBox = document.createElement("div");
                textBox.style.display = "flex";
                textBox.style.flexDirection = "column";
                textBox.style.minWidth = "0";
                textBox.style.flex = "1";
                textBox.style.overflow = "hidden";

                var titleEl = document.createElement("span");
                titleEl.id = "__agy_hud_title__";
                titleEl.style.fontSize = "11.5px";
                titleEl.style.fontWeight = "700";
                titleEl.style.color = "#cdd6f4";
                titleEl.style.whiteSpace = "nowrap";
                titleEl.style.overflow = "hidden";
                titleEl.style.textOverflow = "ellipsis";
                titleEl.style.lineHeight = "1.25";
                textBox.appendChild(titleEl);

                var subEl = document.createElement("span");
                subEl.id = "__agy_hud_sub__";
                subEl.style.fontSize = "10px";
                subEl.style.color = "#a6adc8";
                subEl.style.whiteSpace = "nowrap";
                subEl.style.overflow = "hidden";
                subEl.style.textOverflow = "ellipsis";
                subEl.style.lineHeight = "1.2";
                subEl.style.marginTop = "1px";
                textBox.appendChild(subEl);

                leftBox.appendChild(textBox);
                hud.appendChild(leftBox);

                var stopBtn = document.createElement("button");
                stopBtn.id = "__agy_stop_btn__";
                stopBtn.innerText = "⏹ STOP";
                stopBtn.style.background = "rgba(243, 139, 168, 0.22)";
                stopBtn.style.color = "#f38ba8";
                stopBtn.style.border = "1px solid #f38ba8";
                stopBtn.style.borderRadius = "9999px";
                stopBtn.style.padding = "4px 10px";
                stopBtn.style.fontSize = "10px";
                stopBtn.style.fontWeight = "800";
                stopBtn.style.cursor = "pointer";
                stopBtn.style.flexShrink = "0";
                stopBtn.style.marginLeft = "10px";
                stopBtn.style.outline = "none";
                stopBtn.style.letterSpacing = "0.4px";
                stopBtn.style.display = "none";
                stopBtn.style.touchAction = "manipulation";

                var handleStop = function(e) {
                    if (e) {
                        try { e.preventDefault(); } catch(_) {}
                        try { e.stopPropagation(); } catch(_) {}
                    }
                    if (window.AgyBridge && window.AgyBridge.stop) {
                        window.AgyBridge.stop();
                    } else if (typeof AgyBatchRunner !== "undefined" && AgyBatchRunner.stop) {
                        AgyBatchRunner.stop();
                    }
                };

                // Respon kilat 0ms pada sentuhan pertama di mobile
                stopBtn.addEventListener("pointerdown", handleStop, { passive: false });
                stopBtn.addEventListener("touchstart", handleStop, { passive: false });
                stopBtn.addEventListener("click", handleStop);

                hud.appendChild(stopBtn);
                doc.appendChild(hud);
            }

            // Status color and iconography themes
            var icon = "⚡";
            var iconColor = "#89b4fa";
            var iconBg = "rgba(137, 180, 250, 0.15)";
            var borderColor = "#313244";
            var glowShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 0 1px rgba(255, 255, 255, 0.08)";

            if (statusType === "running") {
                icon = "▶";
                iconColor = "#cba6f7";
                iconBg = "rgba(203, 166, 247, 0.18)";
                borderColor = "#cba6f7";
                glowShadow = "0 8px 24px rgba(0, 0, 0, 0.7), 0 0 10px rgba(203, 166, 247, 0.3)";
            } else if (statusType === "success") {
                icon = "✓";
                iconColor = "#a6e3a1";
                iconBg = "rgba(166, 227, 161, 0.15)";
                borderColor = "#a6e3a1";
                glowShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 8px rgba(166, 227, 161, 0.25)";
            } else if (statusType === "error") {
                icon = "✕";
                iconColor = "#f38ba8";
                iconBg = "rgba(243, 139, 168, 0.18)";
                borderColor = "#f38ba8";
                glowShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 8px rgba(243, 139, 168, 0.25)";
            } else if (statusType === "warning") {
                icon = "!";
                iconColor = "#f9e2af";
                iconBg = "rgba(249, 226, 175, 0.15)";
                borderColor = "#f9e2af";
                glowShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 8px rgba(249, 226, 175, 0.25)";
            } else if (statusType === "recording" || statusType === "recording_step") {
                icon = "●";
                iconColor = "#fab387";
                iconBg = "rgba(250, 179, 135, 0.2)";
                borderColor = "#fab387";
                glowShadow = "0 8px 24px rgba(0, 0, 0, 0.65), 0 0 8px rgba(250, 179, 135, 0.3)";
            }

            // Update isi elemen persisten tanpa pernah me-destroy DOM node
            var iconEl = document.getElementById("__agy_hud_icon__");
            var titleEl = document.getElementById("__agy_hud_title__");
            var subEl = document.getElementById("__agy_hud_sub__");
            var stopBtn = document.getElementById("__agy_stop_btn__");

            if (iconEl) {
                iconEl.innerText = icon;
                iconEl.style.background = iconBg;
                iconEl.style.color = iconColor;
            }
            if (titleEl) {
                titleEl.innerText = titleStr;
            }
            if (subEl) {
                subEl.innerText = subStr;
                subEl.style.display = subStr ? "block" : "none";
            }
            if (stopBtn) {
                stopBtn.style.display = (statusType === "running") ? "inline-flex" : "none";
            }

            hud.style.border = "1px solid " + borderColor;
            hud.style.boxShadow = glowShadow;
            hud.style.pointerEvents = (statusType === "running") ? "auto" : "none";
            hud.style.opacity = "1";
            hud.style.transform = "translateX(-50%) translateY(0)";

            if (hud.__timer) {
                clearTimeout(hud.__timer);
                hud.__timer = null;
            }

            var hideHUD = function(delay) {
                hud.__timer = setTimeout(function() {
                    hud.style.opacity = "0";
                    hud.style.transform = "translateX(-50%) translateY(16px)";
                    hud.style.pointerEvents = "none";
                }, delay);
            };

            // Persistence rules: running is 100% persistent until finished or stopped
            if (statusType === "running") {
                // Keep visible indefinitely while process is executing
            } else if (statusType === "recording_step") {
                hideHUD(1800);
            } else if (statusType === "recording") {
                hideHUD(2400);
            } else if (statusType === "warning") {
                hideHUD(2400);
            } else if (statusType === "error") {
                hideHUD(3000);
            } else if (statusType === "success") {
                hideHUD(2200);
            } else {
                hideHUD(2500);
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
                elementTimeout: 5000,   // timeout tunggu elemen (ms)
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
        activeTimers: [],

        registerTimer: function(t) {
            if (t) this.activeTimers.push(t);
            return t;
        },

        clearAllTimers: function() {
            if (this.activeTimer) {
                try { clearTimeout(this.activeTimer); } catch(_) {}
                this.activeTimer = null;
            }
            if (Array.isArray(this.activeTimers)) {
                for (var i = 0; i < this.activeTimers.length; i++) {
                    try {
                        clearTimeout(this.activeTimers[i]);
                        clearInterval(this.activeTimers[i]);
                    } catch(_) {}
                }
                this.activeTimers = [];
            }
        },

        stop: function() {
            this.isAborted = true;
            this.isRunning = false;
            window.__AGY_ABORT_RUN__ = true;
            this.clearAllTimers();
            try {
                sessionStorage.removeItem("__agy_batch_queue_state__");
                sessionStorage.removeItem("__agy_active_recipe_run__");
                sessionStorage.removeItem("__agy_pending_prompt_queue__");
                localStorage.removeItem("__agy_batch_queue_state__");
                localStorage.removeItem("__agy_active_recipe_run__");
                localStorage.removeItem("__agy_pending_prompt_queue__");
                window.__AGY_BATCH_BACKUP__ = null;
            } catch(_) {}
            try {
                if (window.GhostBridge && window.GhostBridge.releaseWakeLock) {
                    window.GhostBridge.releaseWakeLock();
                }
            } catch(_) {}
            updateAgyHUD("Otomatisasi Dihentikan", "Eksekusi dibatalkan oleh pengguna", "warning");
        },

        resumeBatch: function(batchState) {
            if (!batchState || !batchState.events) return;
            this.run(
                batchState.events,
                batchState.recipeName,
                batchState.targetExpr,
                batchState.isBatch,
                null,
                batchState.runMode,
                batchState.loopCount,
                batchState.targetUrl,
                batchState,
                batchState.reloadBetweenLoops
            );
        },

        run: function(recipeEvents, recipeName, targetExpr, isBatch, onComplete, runMode, loopCount, startUrl, resumedBatchState, reloadBetweenLoopsOpt) {
            var self = this;
            if (!recipeEvents || recipeEvents.length === 0) {
                updateAgyHUD("Resep Kosong", "Tidak ada langkah untuk dieksekusi", "warning");
                return;
            }

            // Hentikan runner aktif sebelumnya agar tidak terjadi tumpang tindih
            if (self.isRunning) {
                self.clearAllTimers();
            }

            self.isRunning = true;
            self.isAborted = false;
            window.__AGY_ABORT_RUN__ = false;

            try {
                if (window.GhostBridge && window.GhostBridge.acquireWakeLock) {
                    window.GhostBridge.acquireWakeLock();
                }
            } catch(_) {}

            var wallets = AgyDB.getWallets();
            var emails = AgyDB.getEmails();
            var cfg = AgyConfig.get();

            // Default mode adalah "raw" (100% Mengikuti Rekaman Asli)
            var mode = runMode || (resumedBatchState && resumedBatchState.runMode) || "raw";
            var loops = parseInt(loopCount, 10);
            if (isNaN(loops) || loops <= 0) loops = (resumedBatchState && resumedBatchState.loopCount) || 1;
            var isInfinite = (loopCount === -1 || loopCount === "inf" || loopCount === "infinity" || loops >= 999999 || (resumedBatchState && resumedBatchState.isInfinite));
            var reloadBetweenLoops = (typeof reloadBetweenLoopsOpt === "boolean") ? reloadBetweenLoopsOpt : ((resumedBatchState && typeof resumedBatchState.reloadBetweenLoops === "boolean") ? resumedBatchState.reloadBetweenLoops : true);

            var queue = [];
            if (resumedBatchState && Array.isArray(resumedBatchState.queue) && resumedBatchState.queue.length > 0) {
                queue = resumedBatchState.queue;
            } else if (mode === "raw") {
                // 100% Ikuti Rekaman Asli: Tidak ada rotasi dompet, loop murni rekaman apa adanya
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    queue.push(0);
                }
            } else if (mode === "single") {
                // Fokus 1 Wallet
                var singleTarget = parseInt(targetExpr, 10);
                if (isNaN(singleTarget) || singleTarget < 1) singleTarget = 1;
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    queue.push(singleTarget);
                }
            } else if (mode === "standalone") {
                // Tanpa Wallet (Web Macro Murni)
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    queue.push(0);
                }
            } else {
                // Rotasi Tuyul (Multi-Wallet): dukung multi-loop siklus rotasi
                var baseTargets = parseTargetExpression(targetExpr, wallets.length);
                if (!baseTargets || baseTargets.length === 0) baseTargets = [1];
                var totalL = isInfinite ? 999999 : loops;
                for (var l = 0; l < totalL; l++) {
                    for (var b = 0; b < baseTargets.length; b++) {
                        queue.push(baseTargets[b]);
                    }
                }
            }

            var successCount = (resumedBatchState && typeof resumedBatchState.successCount === "number") ? resumedBatchState.successCount : 0;
            var failCount = (resumedBatchState && typeof resumedBatchState.failCount === "number") ? resumedBatchState.failCount : 0;
            var currentIdxInQueue = (resumedBatchState && typeof resumedBatchState.currentIdxInQueue === "number") ? resumedBatchState.currentIdxInQueue : 0;
            var finalTargetUrl = startUrl || (resumedBatchState && resumedBatchState.targetUrl) || ((window.location && window.location.href) ? window.location.href.split("#")[0] : "");

            var persistBatchState = function() {
                try {
                    var data = {
                        recipeName: recipeName,
                        events: recipeEvents,
                        targetExpr: targetExpr,
                        isBatch: isBatch,
                        runMode: mode,
                        loopCount: loops,
                        isInfinite: isInfinite,
                        reloadBetweenLoops: reloadBetweenLoops,
                        queue: queue,
                        currentIdxInQueue: currentIdxInQueue,
                        successCount: successCount,
                        failCount: failCount,
                        targetUrl: finalTargetUrl,
                        timestamp: Date.now()
                    };
                    var jsonStr = JSON.stringify(data);
                    sessionStorage.setItem("__agy_batch_queue_state__", jsonStr);
                    localStorage.setItem("__agy_batch_queue_state__", jsonStr);
                    window.__AGY_BATCH_BACKUP__ = data;
                } catch(_) {}
            };

            persistBatchState();

            function runAccount() {
                if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                if (!isInfinite && currentIdxInQueue >= queue.length) {
                    self.isRunning = false;
                    try {
                        if (window.GhostBridge && window.GhostBridge.releaseWakeLock) {
                            window.GhostBridge.releaseWakeLock();
                        }
                    } catch(_) {}
                    try {
                        sessionStorage.removeItem("__agy_batch_queue_state__");
                        sessionStorage.removeItem("__agy_active_recipe_run__");
                        localStorage.removeItem("__agy_batch_queue_state__");
                        localStorage.removeItem("__agy_active_recipe_run__");
                        window.__AGY_BATCH_BACKUP__ = null;
                    } catch(_) {}
                    updateAgyHUD("Selesai!", "Total: " + queue.length + " (Sukses: " + successCount + ", Gagal: " + failCount + ")", "success");
                    if (onComplete) onComplete({ success: successCount, failed: failCount });
                    return;
                }

                persistBatchState();

                var accNumber = queue[currentIdxInQueue % queue.length];
                var curWallet = null;
                var curEmail = "";
                var title = "";
                var sub = "";

                if (mode === "raw") {
                    // 100% Mengikuti Rekaman: pertahankan dompet aktif apa adanya
                    curWallet = AgyDB.getActiveWallet() || { name: "Aktif", address: "" };
                    curEmail = (curWallet.name ? curWallet.name.toLowerCase().replace(/[^a-z0-9]/g, "") : "user") + "@termux.local";
                    title = isInfinite ? (recipeName + " [Loop #" + (currentIdxInQueue + 1) + "]") : ((queue.length > 1) ? (recipeName + " [" + (currentIdxInQueue + 1) + "/" + queue.length + "]") : recipeName);
                    sub = "100% Sesuai Rekaman (" + (curWallet.name || "Dompet Aktif") + ")";
                } else if (mode === "standalone") {
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

                self.executeStepList(recipeEvents, curWallet, curEmail, title, function(isSuccess, stopTriggered) {
                    if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                    if (isSuccess) successCount++;
                    else failCount++;

                    if (stopTriggered) {
                        self.isRunning = false;
                        self.clearAllTimers();
                        try {
                            if (window.GhostBridge && window.GhostBridge.releaseWakeLock) {
                                window.GhostBridge.releaseWakeLock();
                            }
                        } catch(_) {}
                        try {
                            sessionStorage.removeItem("__agy_batch_queue_state__");
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_batch_queue_state__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                            window.__AGY_BATCH_BACKUP__ = null;
                        } catch(_) {}
                        window.__AGY_RECIPE_STATUS__ = "COMPLETED";
                        updateAgyHUD("Alur Selesai (Auto-Stop)", "Kondisi berhenti terpenuhi pada putaran " + (currentIdxInQueue + 1) + ". Eksekusi selesai!", "success");
                        if (onComplete) onComplete({ success: successCount, failed: failCount, stoppedByCondition: true });
                        return;
                    }

                    currentIdxInQueue++;
                    persistBatchState();

                    var hasMore = isInfinite || (currentIdxInQueue < queue.length);
                    if (!hasMore) {
                        self.isRunning = false;
                        try {
                            if (window.GhostBridge && window.GhostBridge.releaseWakeLock) {
                                window.GhostBridge.releaseWakeLock();
                            }
                        } catch(_) {}
                        try {
                            sessionStorage.removeItem("__agy_batch_queue_state__");
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_batch_queue_state__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                            window.__AGY_BATCH_BACKUP__ = null;
                        } catch(_) {}
                        updateAgyHUD("Selesai!", "Total: " + queue.length + " (Sukses: " + successCount + ", Gagal: " + failCount + ")", "success");
                        if (onComplete) onComplete({ success: successCount, failed: failCount });
                        return;
                    }

                    var waitSec = (cfg.batchDelay / 1000).toFixed(1);
                    var nextLoopNum = currentIdxInQueue + 1;
                    var totalLoopsStr = isInfinite ? "♾️" : queue.length;
                    var nextLabel = (mode === "raw" || mode === "single" || mode === "standalone") ? ("Jeda Putaran " + nextLoopNum + "/" + totalLoopsStr + " (" + waitSec + "s)...") : ("Menuju Akun berikutnya (" + waitSec + "s)...");
                    updateAgyHUD("Jeda Antar Alur", nextLabel, "running");

                    self.registerTimer(setTimeout(function() {
                        if (self.isAborted || window.__AGY_ABORT_RUN__) return;

                        var curUrl = (window.location && window.location.href) ? window.location.href.split("#")[0] : "";
                        var isDifferentUrl = (finalTargetUrl && curUrl && finalTargetUrl !== curUrl && finalTargetUrl.indexOf("http") === 0);

                        // Cek apakah resep dimulai dengan langkah reload mandiri (misal: step 1-5 ada reload)
                        var recipeHasInitialReload = false;
                        if (Array.isArray(recipeEvents)) {
                            for (var ri = 0; ri < Math.min(recipeEvents.length, 5); ri++) {
                                if (recipeEvents[ri] && recipeEvents[ri].type === "reload") {
                                    recipeHasInitialReload = true;
                                    break;
                                }
                            }
                        }

                        if (isDifferentUrl) {
                            updateAgyHUD("Mereset Halaman", "Kembali ke URL awal alur...", "running");
                            window.location.href = finalTargetUrl;
                            return;
                        }

                        if (reloadBetweenLoops && !recipeHasInitialReload) {
                            updateAgyHUD("Mereset Halaman", "Memuat ulang halaman untuk putaran " + nextLoopNum + "/" + totalLoopsStr + "...", "running");
                            window.location.reload();
                            return;
                        }

                        runAccount();
                    }, cfg.batchDelay));
                }, 0, mode);
            }

            runAccount();
        },

        executeStepList: function(eventsList, curWallet, curEmail, runTitle, doneCallback, startIdx, runMode) {
            var self = this;
            var cfg = AgyConfig.get();
            var stepDelay = cfg.stepDelay || 1500;
            var waitMode = cfg.waitMode || "strict";
            var maxTimeout = cfg.elementTimeout || 8000;
            var activeMode = runMode || "raw";

            function waitDocReady(next) {
                if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                if (waitMode === "fast") {
                    if (document.readyState === "complete" || document.readyState === "interactive") {
                        self.registerTimer(setTimeout(next, 50));
                        return;
                    }
                }
                waitForDocumentReady(function() {
                    if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                    self.registerTimer(setTimeout(next, waitMode === "strict" ? 600 : 100));
                }, 5000);
            }

            waitDocReady(function() {
                if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                var idx = (typeof startIdx === "number") ? startIdx : 0;

                function runNextStep() {
                    if (self.isAborted || window.__AGY_ABORT_RUN__) {
                        try {
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                        } catch(_) {}
                        return;
                    }
                    if (idx >= eventsList.length) {
                        try {
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                        } catch(_) {}
                        try {
                            var autoRew = extractRewardFromDom({});
                            if (autoRew && (autoRew.amount || autoRew.status !== "UNKNOWN")) {
                                window.__AGY_LAST_REWARD__ = autoRew;
                                if (autoRew.status === "EMPTY" || autoRew.status === "ALREADY_CLAIMED") {
                                    var autoStopReason = (autoRew.status === "EMPTY") ? "Kuota Habis" : "Sudah Pernah Klaim";
                                    updateAgyHUD("🛑 Auto-Stop Terpicu (" + autoStopReason + ")", (autoRew.text || "Mengakhiri putaran..."), "warning");
                                    self.registerTimer(setTimeout(function() {
                                        if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                                        doneCallback(true, true); // stop loop queue!
                                    }, 400));
                                    return;
                                }
                            }
                        } catch(_) {}
                        self.registerTimer(setTimeout(function() {
                            if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                            doneCallback(true);
                        }, 400));
                        return;
                    }

                    var ev = eventsList[idx];
                    var stepNum = idx + 1;

                    // Simpan progress sebelum eksekusi agar survive saat halaman reload / berpindah URL
                    try {
                        var actPayload = JSON.stringify({
                            runTitle: runTitle,
                            eventsList: eventsList,
                            idx: idx,
                            curWallet: curWallet,
                            curEmail: curEmail,
                            runMode: activeMode,
                            timestamp: Date.now()
                        });
                        sessionStorage.setItem("__agy_active_recipe_run__", actPayload);
                        localStorage.setItem("__agy_active_recipe_run__", actPayload);
                    } catch(_) {}

                    idx++;

                    if (ev.type === "wait") {
                        var dur = ev.duration || stepDelay;
                        updateAgyHUD(runTitle, "Jeda langkah " + stepNum + "/" + eventsList.length + " (" + (dur / 1000).toFixed(1) + "s)", "running");
                        self.registerTimer(setTimeout(runNextStep, dur));
                        return;
                    }

                    if (ev.type === "next_wallet") {
                        // Jika mode Fokus 1 Wallet atau Tanpa Wallet: LEWATI pergantian dompet!
                        if (activeMode === "single" || activeMode === "standalone") {
                            updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Lewati Ganti Wallet (" + (activeMode === "single" ? "Fokus 1 Wallet" : "Tanpa Wallet") + ")", "running");
                            self.registerTimer(setTimeout(runNextStep, 350));
                            return;
                        }
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Ganti Wallet", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.nextWallet) {
                                window.GhostBridge.nextWallet();
                            }
                        } catch(_) {}
                        self.registerTimer(setTimeout(runNextStep, 800));
                        return;
                    }

                    if (ev.type === "reload") {
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Reload Halaman...", "running");
                        try {
                            var relPayload = JSON.stringify({
                                runTitle: runTitle,
                                eventsList: eventsList,
                                idx: idx,
                                curWallet: curWallet,
                                curEmail: curEmail,
                                runMode: activeMode,
                                timestamp: Date.now()
                            });
                            sessionStorage.setItem("__agy_active_recipe_run__", relPayload);
                            localStorage.setItem("__agy_active_recipe_run__", relPayload);
                        } catch(_) {}
                        self.registerTimer(setTimeout(function() {
                            if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                            location.reload();
                        }, 500));
                        return;
                    }

                    if (ev.type === "wipe_cache") {
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Wipe Cache", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.wipeCache) {
                                window.GhostBridge.wipeCache();
                            }
                        } catch(_) {}
                        try {
                            var sPreserved = {};
                            for (var si = 0; si < sessionStorage.length; si++) {
                                var sk = sessionStorage.key(si);
                                if (sk && (sk.startsWith("__agy_") || sk.startsWith("__fusion_") || sk.startsWith("agy_") || sk.startsWith("fusion_"))) {
                                    sPreserved[sk] = sessionStorage.getItem(sk);
                                }
                            }
                            sessionStorage.clear();
                            for (var psk in sPreserved) {
                                sessionStorage.setItem(psk, sPreserved[psk]);
                            }

                            var lPreserved = {};
                            for (var li = 0; li < localStorage.length; li++) {
                                var lk = localStorage.key(li);
                                if (lk && (lk.startsWith("__agy_") || lk.startsWith("__fusion_") || lk.startsWith("agy_") || lk.startsWith("fusion_"))) {
                                    lPreserved[lk] = localStorage.getItem(lk);
                                }
                            }
                            localStorage.clear();
                            for (var plk in lPreserved) {
                                localStorage.setItem(plk, lPreserved[plk]);
                            }
                        } catch(_) {}
                        self.registerTimer(setTimeout(runNextStep, 600));
                        return;
                    }

                    if (ev.type === "next_email") {
                        var ne = ev.value || ((typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "tuyul@gmail.com");
                        curEmail = ne;
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Ganti Email -> " + ne, "running");
                        self.registerTimer(setTimeout(runNextStep, 600));
                        return;
                    }

                    if (ev.type === "ref") {
                        var nr = ev.value || ((typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123");
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Ref Code -> " + nr, "running");
                        self.registerTimer(setTimeout(runNextStep, 600));
                        return;
                    }

                    if (ev.type === "fingerprint") {
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Ganti Fingerprint...", "running");
                        try {
                            if (window.GhostBridge && window.GhostBridge.nextFingerprint) {
                                window.GhostBridge.nextFingerprint();
                            }
                        } catch(_) {}
                        self.registerTimer(setTimeout(runNextStep, 800));
                        return;
                    }

                    if (ev.type === "touch") {
                        var tx = ev.x || 100;
                        var ty = ev.y || 100;
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Sentuh (" + Math.round(tx) + ", " + Math.round(ty) + ")", "running");
                        var targetAtPt = document.elementFromPoint(tx, ty) || document.body;
                        if (targetAtPt) {
                            var tOpts = { bubbles: true, cancelable: true, clientX: tx, clientY: ty, view: window };
                            if (window.TouchEvent) {
                                try {
                                    var touchObj = new Touch({ identifier: Date.now(), target: targetAtPt, clientX: tx, clientY: ty, screenX: tx, screenY: ty, pageX: tx + window.scrollX, pageY: ty + window.scrollY });
                                    targetAtPt.dispatchEvent(new TouchEvent("touchstart", { bubbles: true, cancelable: true, touches: [touchObj], targetTouches: [touchObj], changedTouches: [touchObj], view: window }));
                                    targetAtPt.dispatchEvent(new TouchEvent("touchend", { bubbles: true, cancelable: true, touches: [], targetTouches: [], changedTouches: [touchObj], view: window }));
                                } catch(_) {}
                            }
                            try { targetAtPt.dispatchEvent(new MouseEvent("click", tOpts)); } catch(_) {}
                            if (typeof targetAtPt.click === "function") {
                                try { targetAtPt.click(); } catch(_) {}
                            }
                        }
                        self.registerTimer(setTimeout(function() {
                            waitDocReady(runNextStep);
                        }, stepDelay));
                        return;
                    }

                    if (ev.type === "swipe") {
                        var dir = ev.direction || "down";
                        var distY = -ev.deltaY || (dir === "up" ? 300 : (dir === "down" ? -300 : 0));
                        var distX = -ev.deltaX || (dir === "left" ? 250 : (dir === "right" ? -250 : 0));
                        try {
                            window.scrollBy({ left: distX, top: distY, behavior: "smooth" });
                        } catch(_) {}
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Swipe " + dir.toUpperCase(), "running");
                        self.registerTimer(setTimeout(function() {
                            waitDocReady(runNextStep);
                        }, stepDelay));
                        return;
                    }

                    if (ev.type === "stop_if" || ev.type === "stop_not") {
                        var kw = (ev.text || ev.keyword || ev.value || "").trim();
                        var condition = (ev.type === "stop_not" || ev.condition === "not_contains" || ev.condition === "not_found") ? "not_found" : "found";
                        var labelAction = condition === "not_found" ? ("Berhenti jika Hilang: \"" + kw + "\"") : ("Berhenti jika Ada: \"" + kw + "\"");
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": " + labelAction, "running");

                        var scanLimitMs = ev.timeout || 2500;
                        var checkIntervalMs = 250;
                        var elapsedScan = 0;

                        function isMatchPresent() {
                            if (!kw) return false;
                            var needle = kw.toLowerCase();
                            if (ev.selector) {
                                try {
                                    var el = document.querySelector(ev.selector);
                                    if (el) {
                                        var elTxt = (el.innerText || el.textContent || "").toLowerCase();
                                        var elHtm = (el.innerHTML || "").toLowerCase();
                                        return elTxt.indexOf(needle) !== -1 || elHtm.indexOf(needle) !== -1;
                                    }
                                } catch(_) {}
                            }
                            var bodyTxt = ((document.body && (document.body.innerText || document.body.textContent)) || "").toLowerCase();
                            if (bodyTxt.indexOf(needle) !== -1) return true;
                            var bodyHtm = ((document.body && document.body.innerHTML) || "").toLowerCase();
                            return bodyHtm.indexOf(needle) !== -1;
                        }

                        var scanTimer = setInterval(function() {
                            if (self.isAborted || window.__AGY_ABORT_RUN__) {
                                clearInterval(scanTimer);
                                return;
                            }
                            elapsedScan += checkIntervalMs;
                            var foundNow = isMatchPresent();
                            var stopSatisfied = (condition === "found" && foundNow) || (condition === "not_found" && !foundNow && elapsedScan >= 1000);

                            if (stopSatisfied) {
                                clearInterval(scanTimer);
                                updateAgyHUD("🛑 Auto-Stop Terpicu", "Kondisi (\"" + kw + "\") terpenuhi. Mengakhiri alur...", "success");
                                try {
                                    sessionStorage.removeItem("__agy_active_recipe_run__");
                                    localStorage.removeItem("__agy_active_recipe_run__");
                                } catch(_) {}
                                self.registerTimer(setTimeout(function() {
                                    doneCallback(true, true);
                                }, 400));
                                return;
                            }

                            if (elapsedScan >= scanLimitMs) {
                                clearInterval(scanTimer);
                                self.registerTimer(setTimeout(runNextStep, 200));
                            }
                        }, checkIntervalMs);
                        return;
                    }

                    if (ev.type === "solve_challenge") {
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Memecahkan Captcha/Tantangan...", "running");

                        var chEl = ev.challengeSelector ? document.querySelector(ev.challengeSelector) : null;
                        var inEl = ev.inputSelector ? document.querySelector(ev.inputSelector) : null;
                        var isImgOrCanvas = (ev.solverType === "ocr_vision") || (chEl && (chEl.tagName === "IMG" || chEl.tagName === "CANVAS" || chEl.querySelector("img, canvas")));

                        function fillSolution(answer) {
                            if (inEl && answer !== undefined && answer !== null && answer !== "") {
                                inEl.focus();
                                var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                                             Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
                                if (setter) setter.call(inEl, String(answer));
                                else inEl.value = String(answer);
                                inEl.dispatchEvent(new Event("input", { bubbles: true }));
                                inEl.dispatchEvent(new Event("change", { bubbles: true }));
                            }
                            updateAgyHUD("✅ Solver Sukses", "Jawaban: " + answer, "success");
                            self.registerTimer(setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay));
                        }

                        // 1. Coba Vision OCR AI jika elemen gambar/canvas
                        if (isImgOrCanvas) {
                            var imgData = extractElementImageData(chEl);
                            if (imgData) {
                                requestAiVisionSolve(imgData, function(vRes) {
                                    if (vRes && vRes.success && vRes.answer) {
                                        fillSolution(vRes.answer);
                                    } else {
                                        showManualSolvePill("Vision AI gagal membaca captcha. Silakan isi manual:", function() {
                                            updateAgyHUD("Melanjutkan", "Melanjutkan langkah berikutnya...", "running");
                                            self.registerTimer(setTimeout(function() {
                                                waitDocReady(runNextStep);
                                            }, 400));
                                        });
                                    }
                                });
                                return;
                            }
                        }

                        var chText = chEl ? (chEl.innerText || chEl.textContent || chEl.value || "").trim() : (ev.challengeSample || "");

                        // 2. Coba Solver Matematika Lokal Cepat (0ms, tanpa kuota AI)
                        var mathSol = solveSimpleMathLocally(chText);
                        if (mathSol) {
                            fillSolution(mathSol.answer);
                            return;
                        }

                        // 3. Coba Solver AI Teks (DeepSeek / 9Router)
                        requestAiChallengeSolve(chText, function(aiRes) {
                            if (aiRes && aiRes.success && aiRes.answer) {
                                fillSolution(aiRes.answer);
                            } else {
                                // 4. FALLBACK MANUAL: Jika AI gagal, minta user solve manual di layar
                                showManualSolvePill("AI gagal memecahkan. Silakan isi jawaban manual:", function() {
                                    updateAgyHUD("Melanjutkan", "Melanjutkan langkah berikutnya...", "running");
                                    self.registerTimer(setTimeout(function() {
                                        waitDocReady(runNextStep);
                                    }, 400));
                                });
                            }
                        });
                        return;
                    }

                    if (ev.type === "reward_extractor") {
                        updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Memeriksa Hasil Klaim / Reward...", "running");

                        function handleParsedReward(finalRew, newRegex) {
                            if (finalRew && (finalRew.amount || finalRew.status !== "UNKNOWN")) {
                                window.__AGY_LAST_REWARD__ = finalRew;
                                updateAgyHUD("🎁 Hasil Klaim", finalRew.text || (finalRew.amount + " " + finalRew.token), "success");
                            }
                            if (newRegex && !ev.regex) {
                                ev.regex = newRegex; // Simpan rumus regex buatan AI ke resep untuk putaran berikutnya!
                            }

                            // 🔥 HASIL BERTINDAK SEBAGAI STOP-IF CERDAS:
                            var shouldAutoStop = (finalRew && (finalRew.status === "EMPTY" || finalRew.status === "ALREADY_CLAIMED")) || (ev.stopOnSuccess && finalRew && finalRew.status === "SUCCESS");
                            if (shouldAutoStop) {
                                var stopLabel = (finalRew.status === "EMPTY") ? "Kuota Habis / Sold Out" : ((finalRew.status === "ALREADY_CLAIMED") ? "Sudah Pernah Klaim" : "Target Sukses Tercapai");
                                updateAgyHUD("🛑 Auto-Stop Terpicu (" + stopLabel + ")", (finalRew.text || "Mengakhiri seluruh putaran loop..."), "warning");
                                try {
                                    sessionStorage.removeItem("__agy_active_recipe_run__");
                                    localStorage.removeItem("__agy_active_recipe_run__");
                                } catch(_) {}
                                self.registerTimer(setTimeout(function() {
                                    doneCallback(true, true);
                                }, 500));
                                return;
                            }

                            self.registerTimer(setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay));
                        }

                        var rew = extractRewardFromDom(ev);
                        // Jika lokal regex sudah berhasil (0ms), langsung proses tanpa panggil AI!
                        if (rew && (rew.amount || rew.status === "EMPTY" || rew.status === "ALREADY_CLAIMED" || rew.status === "SUCCESS")) {
                            handleParsedReward(rew);
                            return;
                        }

                        // AI First-Run Fallback: Jika teks asing / status UNKNOWN, panggil AI sekali saja
                        var sampleText = ((document.body && (document.body.innerText || document.body.textContent)) || "").substring(0, 1000);
                        requestAiRewardParse(sampleText, function(aiRes) {
                            if (aiRes && aiRes.success) {
                                handleParsedReward(aiRes.reward, aiRes.generatedRegex);
                            } else {
                                handleParsedReward(rew);
                            }
                        });
                        return;
                    }

                    function executeResolvedTarget(targetEl) {
                        if (!targetEl) {
                            self.registerTimer(setTimeout(runNextStep, 400));
                            return;
                        }

                        try { targetEl.scrollIntoView({ behavior: "smooth", block: "center" }); } catch(_) {}

                        if (ev.type === "fill" || ev.type === "input") {
                            var valToFill = ev.value || "";
                            if (activeMode === "raw") {
                                // 100% Asli Mengikuti Rekaman: hanya ganti jika ada variabel template
                                if (valToFill.includes("{{WALLET_ADDRESS}}")) valToFill = valToFill.replace(/\{\{WALLET_ADDRESS\}\}/g, curWallet.address || "");
                                if (valToFill.includes("{{EMAIL}}")) valToFill = valToFill.replace(/\{\{EMAIL\}\}/g, curEmail || "");
                                if (valToFill.includes("{{NAME}}")) valToFill = valToFill.replace(/\{\{NAME\}\}/g, curWallet.name || "");
                                if (valToFill.includes("{{PASSWORD}}") || valToFill.includes("{{PWD}}")) valToFill = valToFill.replace(/\{\{(?:PASSWORD|PWD)\}\}/g, window.__AGY_ACTIVE_PASSWORD__ || "");
                            } else if (activeMode === "standalone") {
                                if (valToFill.includes("{{WALLET_ADDRESS}}")) valToFill = valToFill.replace(/\{\{WALLET_ADDRESS\}\}/g, "");
                                if (valToFill.includes("{{EMAIL}}")) valToFill = valToFill.replace(/\{\{EMAIL\}\}/g, curEmail || "");
                                if (valToFill.includes("{{NAME}}")) valToFill = valToFill.replace(/\{\{NAME\}\}/g, "User");
                                if (valToFill.includes("{{PASSWORD}}") || valToFill.includes("{{PWD}}")) valToFill = valToFill.replace(/\{\{(?:PASSWORD|PWD)\}\}/g, window.__AGY_ACTIVE_PASSWORD__ || "");
                            } else {
                                if (valToFill.includes("{{WALLET_ADDRESS}}")) valToFill = valToFill.replace(/\{\{WALLET_ADDRESS\}\}/g, curWallet.address);
                                if (valToFill.includes("{{EMAIL}}")) valToFill = valToFill.replace(/\{\{EMAIL\}\}/g, curEmail);
                                if (valToFill.includes("{{NAME}}")) valToFill = valToFill.replace(/\{\{NAME\}\}/g, curWallet.name);
                                if (valToFill.includes("{{PASSWORD}}") || valToFill.includes("{{PWD}}")) valToFill = valToFill.replace(/\{\{(?:PASSWORD|PWD)\}\}/g, window.__AGY_ACTIVE_PASSWORD__ || "");
                            }

                            targetEl.focus();
                            var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                                         Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
                            if (setter) setter.call(targetEl, valToFill);
                            else targetEl.value = valToFill;

                            targetEl.dispatchEvent(new Event("input", { bubbles: true }));
                            targetEl.dispatchEvent(new Event("change", { bubbles: true }));
                            updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Isi " + valToFill.substring(0, 16), "running");

                            self.registerTimer(setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay));
                        } else {
                            var now = Date.now();
                            if (targetEl.__agy_last_sim_click__ && (now - targetEl.__agy_last_sim_click__) < 800) {
                                // Debounce: hindari pemicu klik dobel pada elemen yang sama dalam 800ms
                            } else {
                                targetEl.__agy_last_sim_click__ = now;
                                try { targetEl.focus(); } catch(_) {}

                                var rect = targetEl.getBoundingClientRect();
                                var cx = rect.left + rect.width / 2;
                                var cy = rect.top + rect.height / 2;
                                var commonOpts = { bubbles: true, cancelable: true, clientX: cx, clientY: cy, view: window };

                                // Mobile Touch Events Dispatch (support dApp dengan bindTap / FastClick / touchend listener)
                                if (window.TouchEvent) {
                                    try {
                                        var touchObj = new Touch({
                                            identifier: Date.now(),
                                            target: targetEl,
                                            clientX: cx,
                                            clientY: cy,
                                            screenX: cx,
                                            screenY: cy,
                                            pageX: cx + window.scrollX,
                                            pageY: cy + window.scrollY
                                        });
                                        var tOpts = {
                                            bubbles: true,
                                            cancelable: true,
                                            touches: [touchObj],
                                            targetTouches: [touchObj],
                                            changedTouches: [touchObj],
                                            view: window
                                        };
                                        targetEl.dispatchEvent(new TouchEvent("touchstart", tOpts));
                                        targetEl.dispatchEvent(new TouchEvent("touchend", tOpts));
                                    } catch(_) {}
                                }

                                if (window.PointerEvent) {
                                    try {
                                        targetEl.dispatchEvent(new PointerEvent("pointerdown", commonOpts));
                                        targetEl.dispatchEvent(new PointerEvent("pointerup", commonOpts));
                                    } catch(_) {}
                                }
                                try {
                                    targetEl.dispatchEvent(new MouseEvent("mousedown", commonOpts));
                                    targetEl.dispatchEvent(new MouseEvent("mouseup", commonOpts));
                                } catch(_) {}
                                // Single-fire execution: Gunakan native .click() ATAU dispatchEvent, TIDAK PERNAH keduanya!
                                if (typeof targetEl.click === "function") {
                                    try { targetEl.click(); } catch(_) {
                                        try { targetEl.dispatchEvent(new MouseEvent("click", commonOpts)); } catch(_) {}
                                    }
                                } else {
                                    try { targetEl.dispatchEvent(new MouseEvent("click", commonOpts)); } catch(_) {}
                                }
                            }
                            updateAgyHUD(runTitle, "Langkah " + stepNum + "/" + eventsList.length + ": Klik " + (ev.text || targetEl.tagName), "running");

                            self.registerTimer(setTimeout(function() {
                                waitDocReady(runNextStep);
                            }, stepDelay));
                        }
                    }

                    sweepBlockingPopups(ev, function() {
                        smartWaitForElementWithTimeout(ev, stepNum, eventsList.length, maxTimeout, function(targetEl) {
                            if (self.isAborted || window.__AGY_ABORT_RUN__) return;
                            if (!targetEl) {
                                var aiEngine = getAIEngine();
                                if (aiEngine !== "off") {
                                    updateAgyHUD("🤖 AI-Heal (" + aiEngine.toUpperCase() + ")", "Langkah " + stepNum + " macet. Menganalisis layar...", "running");
                                    tryAiSelfHealing(ev, eventsList, idx, runTitle, function(healedEl, newSel) {
                                        if (healedEl) {
                                            updateAgyHUD("✨ AI-Heal Sukses!", "Langkah " + stepNum + " sembuh ➔ " + newSel, "success");
                                            ev.selector = newSel;
                                            ev.healed = true;
                                            try {
                                                var rName = sessionStorage.getItem("__agy_active_recipe_name__");
                                                if (rName && typeof RecipeStore !== "undefined") {
                                                    RecipeStore.save(rName, eventsList, window.location.href);
                                                }
                                            } catch(_) {}
                                            executeResolvedTarget(healedEl);
                                        } else {
                                            showManualHealPill(ev, function(manualEl) {
                                                if (manualEl) {
                                                    var manSel = getCssSelector(manualEl);
                                                    ev.selector = manSel;
                                                    ev.healed = true;
                                                    try {
                                                        var rName = sessionStorage.getItem("__agy_active_recipe_name__");
                                                        if (rName && typeof RecipeStore !== "undefined") {
                                                            RecipeStore.save(rName, eventsList, window.location.href);
                                                        }
                                                    } catch(_) {}
                                                    executeResolvedTarget(manualEl);
                                                } else {
                                                    self.registerTimer(setTimeout(runNextStep, 400));
                                                }
                                            });
                                        }
                                    });
                                    return;
                                }
                                self.registerTimer(setTimeout(runNextStep, 400));
                                return;
                            }

                            executeResolvedTarget(targetEl);
                        });
                    });
                }

                runNextStep();
            });
        }
    };

    function smartWaitForElementWithTimeout(ev, stepIdx, totalSteps, maxWaitMs, callback) {
        var interval = 250;
        var elapsed = 0;
        var timeoutLimit = (ev && ev.optional) ? (ev.timeout || 1500) : (maxWaitMs || 5000);

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
        var lastHudUpdate = 0;
        var pollTimer = setInterval(function() {
            if (AgyBatchRunner.isAborted || window.__AGY_ABORT_RUN__) {
                clearInterval(pollTimer);
                return;
            }
            elapsed += interval;
            var el = findTarget();
            if (el) {
                clearInterval(pollTimer);
                if (AgyBatchRunner.isAborted || window.__AGY_ABORT_RUN__) return;
                callback(el);
            } else if (elapsed >= timeoutLimit) {
                clearInterval(pollTimer);
                if (AgyBatchRunner.isAborted || window.__AGY_ABORT_RUN__) return;
                console.warn("[AgyReplay] Timeout waiting for element on step " + stepIdx + ":", ev);
                callback(null);
            } else if (elapsed - lastHudUpdate >= 1000) {
                lastHudUpdate = elapsed;
                var remainSec = Math.ceil((timeoutLimit - elapsed) / 1000);
                var elemName = (label && label.length > 20) ? (label.substring(0, 18) + "...") : (label || "elemen");
                updateAgyHUD("Menunggu Elemen (" + remainSec + "s)", "Langkah " + stepIdx + "/" + totalSteps + ": " + elemName, "running");
            }
        }, interval);
        AgyBatchRunner.registerTimer(pollTimer);
    }

    function getAgySitePrefix(rawUrl) {
        try {
            var host = "";
            if (rawUrl) {
                host = new URL(rawUrl).hostname;
            } else if (typeof window !== "undefined" && window.location && window.location.hostname) {
                host = window.location.hostname;
            }
            host = host.replace(/^www\./i, "").toLowerCase();
            var parts = host.split(".");
            if (parts.length >= 3 && ["app", "dapp", "m", "mobile", "claim", "web"].indexOf(parts[0]) !== -1) {
                parts.shift();
                host = parts.join(".");
            }
            if (parts.length >= 2) {
                if (parts.length === 2 && ["com", "net", "org", "io", "xyz", "app", "me", "co"].indexOf(parts[1]) === -1) {
                    return host.replace(/[^a-zA-Z0-9_-]/g, "_");
                }
                return parts[0].replace(/[^a-zA-Z0-9_-]/g, "_");
            }
            return host.replace(/[^a-zA-Z0-9_-]/g, "_");
        } catch(_) {
            return "web";
        }
    }

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
            var finalTargetUrl = targetUrl;
            if (!finalTargetUrl && typeof window !== "undefined" && window.location) {
                try {
                    var curHref = window.location.href;
                    if (curHref && curHref.indexOf("about:") !== 0) {
                        finalTargetUrl = curHref.split("#")[0];
                    }
                } catch(_) {}
            }
            var sitePrefix = getAgySitePrefix(finalTargetUrl);
            if (!name || name.trim().length === 0) {
                name = (sitePrefix ? sitePrefix + "_" : "") + "makro_" + new Date().getHours() + "h" + new Date().getMinutes() + "m";
            }
            name = name.trim().replace(/[^a-zA-Z0-9_-]/g, "_");
            if (sitePrefix && name.toLowerCase().indexOf(sitePrefix.toLowerCase()) !== 0) {
                name = sitePrefix + "_" + name.replace(/^(resep_|makro_)/i, "");
            }
            var all = this.getAll();
            all[name] = {
                name: name,
                site: sitePrefix,
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
        card.style.overflowX = "hidden";
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
        card.style.overflowX = "hidden";
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
            updateAgyHUD("Rekaman Berhasil Dibuat", "Resep '" + decoded.name + "' (" + decoded.events.length + " langkah) siap diputar", "success");
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
                        updateAgyHUD("Rekaman Dibuat Otomatis", "Resep '" + data.name + "' (" + data.events.length + " langkah) dikloning dari pengirim", "success");
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

    // --- 4B. SMART CHALLENGE & REWARD SOLVER HELPERS ---
    function solveSimpleMathLocally(text) {
        if (!text) return null;
        var clean = text.replace(/[\?？\=＝]/g, " ").trim();
        var m = clean.match(/(\d+)\s*([\+\-\*\/xX]|—)\s*(\d+)/);
        if (m) {
            var a = parseInt(m[1], 10);
            var op = m[2];
            var b = parseInt(m[3], 10);
            var ans = null;
            if (op === "+") ans = a + b;
            else if (op === "-" || op === "—") ans = a - b;
            else if (op === "*" || op === "x" || op === "X") ans = a * b;
            else if (op === "/") ans = Math.floor(a / b);
            if (ans !== null && !isNaN(ans)) {
                return {
                    answer: String(ans),
                    formula: "math:" + op
                };
            }
        }
        return null;
    }

    function showManualSolvePill(promptText, onContinue) {
        var old = document.getElementById("__agy_manual_solve_pill__");
        if (old) old.remove();

        var pill = document.createElement("div");
        pill.id = "__agy_manual_solve_pill__";
        pill.style.position = "fixed";
        pill.style.top = "14px";
        pill.style.left = "50%";
        pill.style.transform = "translateX(-50%)";
        pill.style.zIndex = "99999999";
        pill.style.background = "rgba(24, 24, 37, 0.95)";
        pill.style.backdropFilter = "blur(10px)";
        pill.style.webkitBackdropFilter = "blur(10px)";
        pill.style.border = "1.5px solid #fab387";
        pill.style.borderRadius = "24px";
        pill.style.padding = "8px 16px";
        pill.style.boxShadow = "0 8px 24px rgba(0,0,0,0.6)";
        pill.style.display = "flex";
        pill.style.alignItems = "center";
        pill.style.gap = "10px";
        pill.style.color = "#cdd6f4";
        pill.style.fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        pill.style.fontSize = "12px";

        var icon = document.createElement("span");
        icon.innerText = "⚠️";
        icon.style.fontSize = "14px";
        pill.appendChild(icon);

        var label = document.createElement("span");
        label.innerText = promptText || "Isi captcha manual:";
        label.style.fontWeight = "600";
        label.style.color = "#fab387";
        pill.appendChild(label);

        var contBtn = document.createElement("button");
        contBtn.innerText = "Lanjut ▶";
        contBtn.style.background = "#fab387";
        contBtn.style.color = "#11111b";
        contBtn.style.border = "none";
        contBtn.style.borderRadius = "14px";
        contBtn.style.padding = "5px 12px";
        contBtn.style.fontWeight = "bold";
        contBtn.style.fontSize = "11px";
        contBtn.style.cursor = "pointer";
        contBtn.onclick = function() {
            pill.remove();
            if (onContinue) onContinue();
        };
        pill.appendChild(contBtn);

        var skipBtn = document.createElement("button");
        skipBtn.innerText = "Lewati";
        skipBtn.style.background = "#313244";
        skipBtn.style.color = "#bac2de";
        skipBtn.style.border = "none";
        skipBtn.style.borderRadius = "14px";
        skipBtn.style.padding = "5px 10px";
        skipBtn.style.fontSize = "11px";
        skipBtn.style.cursor = "pointer";
        skipBtn.onclick = function() {
            pill.remove();
            if (onContinue) onContinue();
        };
        pill.appendChild(skipBtn);

        (document.body || document.documentElement).appendChild(pill);
    }

    function extractRewardFromDom(ev) {
        var result = { amount: "", token: "", status: "UNKNOWN", text: "", isBusy: false };
        try {
            var bodyTxt = (document.body && (document.body.innerText || document.body.textContent)) || "";

            // 1. Cek SIBUK (Busy) - Pola 100% persis gate.js
            if (bodyTxt.includes('当前抢红包人数太多啦') || bodyTxt.includes('稍后重试') || bodyTxt.includes('Server busy') || bodyTxt.includes('Too many requests')) {
                result.text = '当前抢红包人数太多啦，请稍后重试 (Server Sibuk)';
                result.status = 'BUSY';
                result.isBusy = true;
                return result;
            }

            // 2. Cek HABIS (Empty) - Pola 100% persis gate.js
            if (bodyTxt.includes('红包已领完') || bodyTxt.includes('Sold out') || bodyTxt.includes('Habis') || bodyTxt.includes('All claimed')) {
                result.text = '红包已领完，下次早点哦 (Habis)';
                result.status = 'EMPTY';
                return result;
            }

            // 3. Cek Custom Regex jika pengguna tentukan (di mode text regex)
            if (ev && (ev.regex || ev.pattern)) {
                try {
                    var customReg = new RegExp(ev.regex || ev.pattern, "i");
                    var cm = bodyTxt.match(customReg);
                    if (cm) {
                        result.amount = cm[1] || cm[0];
                        if (cm[2]) result.token = cm[2].toUpperCase();
                        result.status = 'SUCCESS';
                        result.text = cm[0];
                        return result;
                    }
                } catch(_) {}
            }

            // 4. Cek SUKSES & Ekstrak Token dari Body Text (Pola gate.js: "领取成功" + USDT/token)
            if ((bodyTxt.includes('领取成功') && bodyTxt.includes('奖励在')) || bodyTxt.includes('领取成功') || bodyTxt.includes('Claim successful') || bodyTxt.includes('Berhasil klaim') || bodyTxt.includes('Congratulations')) {
                var matchGate = bodyTxt.match(/([0-9]+(?:\.[0-9]+)?)\s*(USDT|BEM|USDC|BNB|BTC|ETH|SOL|TON|DOGE|TRX|[A-Za-z0-9_]{2,10})/i);
                if (matchGate) {
                    result.amount = matchGate[1];
                    result.token = matchGate[2].toUpperCase();
                    result.text = "领取成功 | " + matchGate[0];
                } else {
                    result.text = "领取成功";
                }
                result.status = 'SUCCESS';
                return result;
            }

            // 5. Cek Elemen DOM Spesifik jika ada (misal Tapexo: #got-amt, #got-sym)
            if (ev && ev.amountSelector) {
                var el = document.querySelector(ev.amountSelector);
                if (el) {
                    var txt = (el.innerText || el.textContent || "").trim();
                    var m = txt.match(/([0-9]+(?:\.[0-9]+)?)\s*([A-Za-z0-9_]+)?/);
                    if (m) {
                        result.amount = m[1];
                        if (m[2]) result.token = m[2];
                    } else {
                        result.amount = txt;
                    }
                }
            }

            var gotAmtEl = document.getElementById("got-amt");
            var gotSymEl = document.getElementById("got-sym");
            var payStateEl = document.getElementById("pay-state");
            var hintEl = document.getElementById("hint");

            if (gotAmtEl && (gotAmtEl.innerText || "").trim()) {
                result.amount = (gotAmtEl.innerText || "").trim();
                if (gotSymEl) result.token = (gotSymEl.innerText || "").trim();
                if (payStateEl && (payStateEl.innerText || "").trim()) result.status = payStateEl.innerText.trim();
                else result.status = "SUCCESS";
                result.text = result.amount + " " + (result.token || "") + (result.status ? (" (" + result.status + ")") : "");
                return result;
            }

            if (hintEl && (hintEl.innerText || "").trim()) {
                var hTxt = hintEl.innerText.trim();
                if (hTxt.includes("你已领过") || hTxt.includes("已领过") || hTxt.includes("Sudah diklaim") || hTxt.includes("Already claimed")) {
                    result.status = "ALREADY_CLAIMED";
                    result.text = hTxt;
                    return result;
                }
            }

            // 6. Universal Web3 / Crypto Token Pattern Scanner di Text
            var genericMatch = bodyTxt.match(/(?:Reward|Claimed|You received|Bonus|Received|Dapat|Hadiah)[:\s]*([0-9]+(?:\.[0-9]+)?)\s*([A-Za-z0-9_]{2,10})/i);
            if (genericMatch) {
                result.status = "SUCCESS";
                result.amount = genericMatch[1];
                result.token = genericMatch[2].toUpperCase();
                result.text = genericMatch[0];
                return result;
            }

            // 7. Fallback: Toast / Popup Scanner dari bawah ke atas (100% Persis gate.js lines 507-528)
            var elements = Array.from(document.querySelectorAll('div, span, p'));
            for (var i = elements.length - 1; i >= 0; i--) {
                var tEl = elements[i];
                if (!tEl.innerText || tEl.innerText.trim() === '' || tEl.offsetParent === null) continue;
                var text = tEl.innerText.trim();
                if (text.length > 120 || text.includes('买币') || text.includes('福利中心')) continue;

                var style = window.getComputedStyle(tEl);
                var isRed = style.color === 'rgb(255, 0, 0)' || style.color === 'rgb(239, 68, 68)' || style.color.includes('245, 63, 63');
                var isToast = (style.position === 'fixed' || style.position === 'absolute') && parseInt(style.zIndex || '0') > 10;

                if (isRed || isToast) {
                    if (text.length >= 2) {
                        var tm = text.match(/([0-9]+(?:\.[0-9]+)?)\s*([A-Za-z0-9_]{2,10})/);
                        if (tm) {
                            result.amount = tm[1];
                            result.token = tm[2].toUpperCase();
                        }
                        result.text = text;
                        result.status = isRed ? 'ERROR' : 'TOAST';
                        return result;
                    }
                }
            }
        } catch(err) {
            console.warn("[RewardExtractor] Error:", err);
        }
        return result;
    }

    // --- 4C. TRI-ENGINE AI CASCADE & GLOBAL HEAL ENGINE ---
    function getAIEngine() {
        try {
            var local = localStorage.getItem("__agy_ai_engine__");
            if (local) return local.toLowerCase();
            if (typeof GhostBridge !== "undefined" && GhostBridge.readWalletFile) {
                var raw = GhostBridge.readWalletFile("ai_engine.json");
                if (raw) {
                    var obj = JSON.parse(raw);
                    if (obj.activeEngine) return obj.activeEngine.toLowerCase();
                }
            }
        } catch(_) {}
        return "auto";
    }

    function setAIEngineGlobal(engine) {
        var clean = (engine || "auto").toLowerCase();
        try {
            localStorage.setItem("__agy_ai_engine__", clean);
            if (typeof GhostBridge !== "undefined" && GhostBridge.writeWalletFile) {
                GhostBridge.writeWalletFile("ai_engine.json", JSON.stringify({ activeEngine: clean, updatedAt: Date.now() }));
            }
        } catch(_) {}
        updateAgyHUD("🤖 AI Engine", "Aktif: " + clean.toUpperCase() + " (Permanen)", "success");
    }

    function queryTriEngineCascade(promptText, systemPrompt, callback) {
        var engine = getAIEngine();
        if (engine === "off") {
            if (callback) callback({ success: false, text: null });
            return;
        }

        // 1. Coba 9Router (Local Port 20128)
        function try9Router(next) {
            try {
                var xhr = new XMLHttpRequest();
                xhr.open("POST", "http://127.0.0.1:20128/v1/chat/completions", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.timeout = 4000;
                xhr.onload = function() {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            var resp = JSON.parse(xhr.responseText);
                            var content = resp.choices && resp.choices[0] && resp.choices[0].message && resp.choices[0].message.content;
                            if (content) {
                                callback({ success: true, text: content.trim(), engine: "9Router" });
                                return;
                            }
                        } catch(_) {}
                    }
                    next();
                };
                xhr.onerror = next;
                xhr.ontimeout = next;
                xhr.send(JSON.stringify({
                    model: "antigravity",
                    messages: [
                        { role: "system", content: systemPrompt },
                        { role: "user", content: promptText }
                    ],
                    max_tokens: 150
                }));
            } catch(_) { next(); }
        }

        // 2. Coba DeepSeek (Direct via ai_config.json)
        function tryDeepSeek(next) {
            var cfg = null;
            try {
                if (typeof GhostBridge !== "undefined" && GhostBridge.readWalletFile) {
                    var rawCfg = GhostBridge.readWalletFile("ai_config.json");
                    if (rawCfg) cfg = JSON.parse(rawCfg);
                }
            } catch(_) {}

            if (!cfg || !cfg.apiKey) {
                next();
                return;
            }

            try {
                var base = (cfg.baseUrl || "https://api.deepseek.com").replace(/\/+$/, "");
                var url = base.endsWith("/chat/completions") ? base : (base + "/chat/completions");
                var xhr = new XMLHttpRequest();
                xhr.open("POST", url, true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.setRequestHeader("Authorization", "Bearer " + cfg.apiKey);
                xhr.timeout = 5000;
                xhr.onload = function() {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            var resp = JSON.parse(xhr.responseText);
                            var content = resp.choices && resp.choices[0] && resp.choices[0].message && resp.choices[0].message.content;
                            if (content) {
                                callback({ success: true, text: content.trim(), engine: "DeepSeek" });
                                return;
                            }
                        } catch(_) {}
                    }
                    next();
                };
                xhr.onerror = next;
                xhr.ontimeout = next;
                xhr.send(JSON.stringify({
                    model: cfg.model || "deepseek-flash",
                    messages: [
                        { role: "system", content: systemPrompt },
                        { role: "user", content: promptText }
                    ],
                    max_tokens: 150
                }));
            } catch(_) { next(); }
        }

        // 3. Coba AGY Copilot (Wake up via GhostBridge)
        function tryAgyCopilot() {
            try {
                if (typeof GhostBridge !== "undefined" && GhostBridge.wakeUpAgy) {
                    GhostBridge.wakeUpAgy();
                }
            } catch(_) {}
            if (callback) callback({ success: false, text: null, engine: "None" });
        }

        // Jalankan sesuai preferensi Tuan
        if (engine === "deepseek") {
            tryDeepSeek(function() { try9Router(tryAgyCopilot); });
        } else if (engine === "9router") {
            try9Router(function() { tryDeepSeek(tryAgyCopilot); });
        } else {
            // Mode AUTO (Cascade: 9Router lokal -> DeepSeek -> AGY Copilot)
            try9Router(function() { tryDeepSeek(tryAgyCopilot); });
        }
    }

    function requestAiChallengeSolve(chText, callback) {
        if (!chText) {
            if (callback) callback({ success: false, error: "Empty challenge" });
            return;
        }
        queryTriEngineCascade(
            "Solve this captcha/math puzzle: " + chText + ". Output strictly ONLY the single final answer without explanation.",
            "You are a captcha and math solver. Output strictly ONLY the answer value.",
            function(res) {
                if (res && res.success && res.text) {
                    var cleanAns = res.text.replace(/^["'`]|["'`]$/g, "").trim();
                    callback({ success: true, answer: cleanAns, engine: res.engine });
                } else {
                    callback({ success: false, error: "AI failed" });
                }
            }
        );
    }

    function requestAiRewardParse(sampleText, callback) {
        if (!sampleText || sampleText.length < 5) {
            if (callback) callback({ success: false });
            return;
        }
        queryTriEngineCascade(
            "Analyze this page text after claim:\n" + sampleText + "\nOutput strictly JSON:\n{\"status\":\"SUCCESS\"|\"EMPTY\"|\"ALREADY_CLAIMED\"|\"UNKNOWN\",\"amount\":\"number or empty\",\"token\":\"symbol or empty\",\"regex\":\"regex pattern to extract amount and token next time\",\"reason\":\"short explanation\"}",
            "You are a web3 claim reward analyzer. Output strictly JSON.",
            function(res) {
                if (res && res.success && res.text) {
                    try {
                        var jsonMatch = res.text.match(/\{[\s\S]*\}/);
                        if (jsonMatch) {
                            var parsed = JSON.parse(jsonMatch[0]);
                            callback({
                                success: true,
                                reward: {
                                    amount: parsed.amount || "",
                                    token: (parsed.token || "").toUpperCase(),
                                    status: parsed.status || "SUCCESS",
                                    text: parsed.reason || (parsed.amount + " " + parsed.token)
                                },
                                generatedRegex: parsed.regex || null,
                                engine: res.engine
                            });
                            return;
                        }
                    } catch(_) {}
                }
                callback({ success: false });
            }
        );
    }

    function tryAiSelfHealing(ev, eventsList, stepIdx, runTitle, callback) {
        var candidates = [];
        try {
            var allInter = Array.from(document.querySelectorAll("button, a, input, [role='button'], [tabindex='0']"));
            for (var i = 0; i < allInter.length; i++) {
                var el = allInter[i];
                if (isInternalUI(el)) continue;
                if (el.offsetParent === null) continue;
                var rect = el.getBoundingClientRect();
                if (rect.width === 0 || rect.height === 0) continue;
                var txt = (el.innerText || el.value || el.placeholder || el.getAttribute("aria-label") || "").trim();
                if (!txt && !el.id && !el.className) continue;
                candidates.push({
                    tag: el.tagName.toLowerCase(),
                    text: txt.substring(0, 30),
                    id: el.id || "",
                    selector: getCssSelector(el)
                });
                if (candidates.length >= 20) break;
            }
        } catch(_) {}

        // Cek popup modal penutup
        var modalCloseBtn = null;
        try {
            var popups = Array.from(document.querySelectorAll("[class*='modal'], [class*='popup'], [class*='dialog'], [role='dialog']"));
            for (var pi = 0; pi < popups.length; pi++) {
                var pEl = popups[pi];
                if (pEl.offsetParent !== null) {
                    var cBtn = pEl.querySelector("button, [class*='close'], [aria-label*='close'], [aria-label*='Close']");
                    if (cBtn && cBtn.offsetParent !== null) {
                        modalCloseBtn = cBtn;
                        break;
                    }
                }
            }
        } catch(_) {}

        if (modalCloseBtn) {
            updateAgyHUD("🛡️ AI-Heal: Menutup Popup...", "Mengeklik penutup modal penghalang...", "running");
            try { modalCloseBtn.click(); } catch(_) {}
            setTimeout(function() {
                var retryEl = ev.selector ? document.querySelector(ev.selector) : null;
                if (retryEl) {
                    callback(retryEl, ev.selector);
                    return;
                }
                askAiForCandidate();
            }, 600);
            return;
        }

        askAiForCandidate();

        function askAiForCandidate() {
            if (!candidates.length) {
                callback(null);
                return;
            }
            var promptMsg = "Langkah makro gagal mencari target: type=" + ev.type + ", teks='" + (ev.text || "") + "', selector='" + (ev.selector || "") + "'.\n" +
                "Kandidat elemen tombol/input yang tampak di layar saat ini:\n" +
                JSON.stringify(candidates, null, 2) + "\n" +
                "Tentukan selector yang paling tepat menggantikan target yang gagal. Balas HANYA JSON:\n" +
                "{\"found\":true,\"selector\":\"<selector_tepat>\"}\n" +
                "Jika tidak ada yang cocok, balas: {\"found\":false}";

            queryTriEngineCascade(promptMsg, "You are a web automation self-healing copilot. Output strictly JSON.", function(aiAns) {
                if (aiAns && aiAns.text) {
                    try {
                        var jsonM = aiAns.text.match(/\{[\s\S]*\}/);
                        if (jsonM) {
                            var parsed = JSON.parse(jsonM[0]);
                            if (parsed.found && parsed.selector) {
                                var newTarget = document.querySelector(parsed.selector);
                                if (newTarget) {
                                    callback(newTarget, parsed.selector);
                                    return;
                                }
                            }
                        }
                    } catch(_) {}
                }
                callback(null);
            });
        }
    }

    function showManualHealPill(ev, onChosen) {
        var old = document.getElementById("__agy_manual_heal_pill__");
        if (old) old.remove();

        var pill = document.createElement("div");
        pill.id = "__agy_manual_heal_pill__";
        pill.style.position = "fixed";
        pill.style.top = "14px";
        pill.style.left = "50%";
        pill.style.transform = "translateX(-50%)";
        pill.style.zIndex = "99999999";
        pill.style.background = "rgba(24, 24, 37, 0.95)";
        pill.style.backdropFilter = "blur(10px)";
        pill.style.webkitBackdropFilter = "blur(10px)";
        pill.style.border = "1.5px solid #89dceb";
        pill.style.borderRadius = "24px";
        pill.style.padding = "8px 16px";
        pill.style.boxShadow = "0 8px 24px rgba(0,0,0,0.6)";
        pill.style.display = "flex";
        pill.style.alignItems = "center";
        pill.style.gap = "10px";
        pill.style.color = "#cdd6f4";
        pill.style.fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        pill.style.fontSize = "12px";

        var icon = document.createElement("span");
        icon.innerText = "🩹";
        icon.style.fontSize = "14px";
        pill.appendChild(icon);

        var label = document.createElement("span");
        label.innerText = "Sentuh tombol/elemen pengganti di layar:";
        label.style.fontWeight = "600";
        label.style.color = "#89dceb";
        pill.appendChild(label);

        var skipBtn = document.createElement("button");
        skipBtn.innerText = "Lewati";
        skipBtn.style.background = "#313244";
        skipBtn.style.color = "#bac2de";
        skipBtn.style.border = "none";
        skipBtn.style.borderRadius = "14px";
        skipBtn.style.padding = "5px 10px";
        skipBtn.style.fontSize = "11px";
        skipBtn.style.cursor = "pointer";
        skipBtn.onclick = function() {
            document.removeEventListener("click", onTouchTarget, true);
            pill.remove();
            if (onChosen) onChosen(null);
        };
        pill.appendChild(skipBtn);

        function onTouchTarget(e) {
            if (isInternalUI(e.target)) return;
            e.preventDefault();
            e.stopPropagation();
            document.removeEventListener("click", onTouchTarget, true);
            pill.remove();
            if (onChosen) onChosen(e.target);
        }

        document.addEventListener("click", onTouchTarget, true);
        (document.body || document.documentElement).appendChild(pill);
    }

    // --- 4D. MULTIMODAL OCR & VISION CAPTCHA SOLVER ---
    function extractElementImageData(el) {
        if (!el) return null;
        var targetImg = (el.tagName === "IMG" || el.tagName === "CANVAS") ? el : el.querySelector("img, canvas");
        if (!targetImg) return null;
        try {
            if (targetImg.tagName === "CANVAS") {
                return targetImg.toDataURL("image/png");
            }
            if (targetImg.tagName === "IMG") {
                if (targetImg.src && targetImg.src.startsWith("data:image/")) {
                    return targetImg.src;
                }
                var c = document.createElement("canvas");
                c.width = targetImg.naturalWidth || targetImg.width || 120;
                c.height = targetImg.naturalHeight || targetImg.height || 40;
                var ctx = c.getContext("2d");
                ctx.drawImage(targetImg, 0, 0);
                return c.toDataURL("image/png");
            }
        } catch(err) {
            console.warn("[OCR Vision] Error extracting canvas/img data:", err);
        }
        return null;
    }

    function queryTriEngineVision(imageDataUrl, promptText, callback) {
        var engine = getAIEngine();
        if (engine === "off" || !imageDataUrl) {
            if (callback) callback({ success: false, text: null });
            return;
        }

        var defaultPrompt = "Read the captcha, distorted digits, or characters in this image. Output strictly ONLY the answer value without quotes or explanation.";
        var userPrompt = promptText || defaultPrompt;

        // 1. Coba 9Router (Local Vision via 20128)
        function try9RouterVision(next) {
            try {
                var xhr = new XMLHttpRequest();
                xhr.open("POST", "http://127.0.0.1:20128/v1/chat/completions", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.timeout = 7000;
                xhr.onload = function() {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            var resp = JSON.parse(xhr.responseText);
                            var content = resp.choices && resp.choices[0] && resp.choices[0].message && resp.choices[0].message.content;
                            if (content) {
                                callback({ success: true, text: content.trim(), engine: "9Router-Vision" });
                                return;
                            }
                        } catch(_) {}
                    }
                    next();
                };
                xhr.onerror = next;
                xhr.ontimeout = next;
                xhr.send(JSON.stringify({
                    model: "antigravity",
                    messages: [
                        { role: "system", content: "You are an expert OCR and captcha visual reader. Output strictly ONLY the answer string." },
                        {
                            role: "user",
                            content: [
                                { type: "text", text: userPrompt },
                                { type: "image_url", image_url: { url: imageDataUrl } }
                            ]
                        }
                    ],
                    max_tokens: 60
                }));
            } catch(_) { next(); }
        }

        // 2. Coba Gemini / AGY Ephemeral via GhostBridge
        function tryAgyVision() {
            try {
                if (typeof GhostBridge !== "undefined" && GhostBridge.wakeUpAgy) {
                    GhostBridge.wakeUpAgy();
                }
            } catch(_) {}
            if (callback) callback({ success: false, text: null });
        }

        try9RouterVision(tryAgyVision);
    }

    function requestAiVisionSolve(imageDataUrl, callback) {
        if (!imageDataUrl) {
            if (callback) callback({ success: false, error: "Empty image data" });
            return;
        }
        updateAgyHUD("👁️ Vision AI", "Menganalisis gambar/canvas captcha...", "running");
        queryTriEngineVision(imageDataUrl, "Read the digits or text in this captcha image. Output strictly ONLY the single final answer without explanation.", function(res) {
            if (res && res.success && res.text) {
                var cleanAns = res.text.replace(/^["'`]|["'`]$/g, "").trim();
                callback({ success: true, answer: cleanAns, engine: res.engine });
            } else {
                callback({ success: false, error: "Vision AI failed" });
            }
        });
    }

    // --- 4E. PROACTIVE POPUP & CONSENT SWEEPER ---
    function sweepBlockingPopups(currentEv, onDone) {
        try {
            var modalCandidates = document.querySelectorAll(
                'div[role="dialog"], div[aria-modal="true"], .modal, [class*="modal-content"], ' +
                '[class*="backdrop"], [class*="overlay"], [class*="cookie" i], [id*="cookie" i], ' +
                '[class*="consent" i], [id*="consent" i], [class*="banner" i]'
            );
            var blocker = null;
            for (var i = 0; i < modalCandidates.length; i++) {
                var m = modalCandidates[i];
                if (!m || (m.offsetParent === null && m.offsetWidth === 0)) continue;
                var style = window.getComputedStyle(m);
                if (style.display === "none" || style.visibility === "hidden" || parseFloat(style.opacity) === 0) continue;
                var z = parseInt(style.zIndex, 10);
                var rect = m.getBoundingClientRect();
                if ((style.position === "fixed" || style.position === "absolute" || z >= 50) &&
                    rect.width > window.innerWidth * 0.25 && rect.height > window.innerHeight * 0.12) {
                    var txt = (m.innerText || "").toLowerCase();
                    if (txt.match(/cookie|consent|persetujuan|terms|privacy|kebijakan|subscribe|newsletter|adblock|turn off|notification|whitelist|agree/)) {
                        if (currentEv && currentEv.selector) {
                            try {
                                if (m.querySelector(currentEv.selector)) continue;
                            } catch(_) {}
                        }
                        blocker = m;
                        break;
                    }
                }
            }

            if (blocker) {
                var closeBtn = blocker.querySelector(
                    'button[aria-label*="close" i], button[aria-label*="tutup" i], .close, .btn-close, ' +
                    '[data-dismiss="modal"], [data-bs-dismiss="modal"], [class*="close-btn" i], [class*="closeBtn" i]'
                );
                if (!closeBtn) {
                    var btns = blocker.querySelectorAll('button, a, div[role="button"], span[role="button"]');
                    for (var b = 0; b < btns.length; b++) {
                        var bEl = btns[b];
                        var bTxt = (bEl.innerText || bEl.textContent || "").trim();
                        if (bTxt.match(/^(accept all|accept|agree|i agree|got it|understand|allow all|allow|ok|tutup|saya setuju|lanjutkan|close|×|✕)$/i)) {
                            closeBtn = bEl;
                            break;
                        }
                    }
                }
                if (!closeBtn) {
                    var svgs = blocker.querySelectorAll('button svg, div[role="button"] svg');
                    if (svgs.length > 0) {
                        closeBtn = svgs[svgs.length - 1].closest('button, div[role="button"]');
                    }
                }

                if (closeBtn) {
                    try {
                        closeBtn.focus();
                        closeBtn.click();
                        updateAgyHUD("🛡️ Auto-Sweeper", "Popup / Consent ditutup otomatis", "info");
                        setTimeout(onDone, 300);
                        return;
                    } catch(_) {}
                }
            }
        } catch(err) {
            console.warn("[AutoSweeper] Error:", err);
        }
        onDone();
    }

    // --- 4F. AUTONOMOUS RECIPE SYNTHESIZER DOM SUMMARIZER ---
    window.__AGY_GET_PAGE_SUMMARY__ = function() {
        try {
            var buttons = [];
            var inputs = [];
            var seenSel = new Set();

            var allBtns = document.querySelectorAll("button, a, [role='button'], input[type='button'], input[type='submit']");
            for (var i = 0; i < allBtns.length; i++) {
                var el = allBtns[i];
                if (isInternalUI(el)) continue;
                if (el.offsetParent === null && el.offsetWidth === 0) continue;
                var rect = el.getBoundingClientRect();
                if (rect.width === 0 || rect.height === 0) continue;
                var txt = (el.innerText || el.value || el.getAttribute("aria-label") || "").trim();
                var sel = getCssSelector(el);
                if (!sel || seenSel.has(sel)) continue;
                seenSel.add(sel);

                buttons.push({
                    text: txt.substring(0, 40),
                    selector: sel,
                    tag: el.tagName.toLowerCase(),
                    id: el.id || "",
                    className: (el.className && typeof el.className === "string") ? el.className.substring(0, 50) : ""
                });
                if (buttons.length >= 30) break;
            }

            var allInputs = document.querySelectorAll("input:not([type='hidden']):not([type='button']):not([type='submit']), textarea");
            for (var j = 0; j < allInputs.length; j++) {
                var inp = allInputs[j];
                if (isInternalUI(inp)) continue;
                if (inp.offsetParent === null && inp.offsetWidth === 0) continue;
                var inpSel = getCssSelector(inp);
                if (!inpSel || seenSel.has(inpSel)) continue;
                seenSel.add(inpSel);

                inputs.push({
                    selector: inpSel,
                    type: inp.type || "text",
                    placeholder: (inp.placeholder || "").substring(0, 40),
                    name: inp.name || "",
                    id: inp.id || "",
                    value: (inp.value || "").substring(0, 30)
                });
                if (inputs.length >= 15) break;
            }

            return JSON.stringify({
                title: document.title || "",
                url: window.location.href,
                buttons: buttons,
                inputs: inputs
            });
        } catch(err) {
            return JSON.stringify({ error: err.message });
        }
    };

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
            } else if (ev.type === "stop_if" || ev.type === "stop_not") {
                detail = "Auto-Stop jika " + (ev.type === "stop_not" ? "hilang: " : "ada: ") + (ev.text || "");
            } else {
                detail = ev.type || "Aksi";
            }
            updateAgyHUD(title, detail, "recording_step");
        },

        promptStopCondition: function() {
            var existing = document.getElementById("__agy_stop_modal__");
            if (existing) existing.remove();

            var modal = document.createElement("div");
            modal.id = "__agy_stop_modal__";
            modal.style.position = "fixed";
            modal.style.top = "0";
            modal.style.left = "0";
            modal.style.width = "100%";
            modal.style.height = "100%";
            modal.style.background = "rgba(10, 10, 18, 0.75)";
            modal.style.backdropFilter = "blur(6px)";
            modal.style.webkitBackdropFilter = "blur(6px)";
            modal.style.display = "flex";
            modal.style.alignItems = "center";
            modal.style.justifyContent = "center";
            modal.style.zIndex = "99999999";
            modal.style.padding = "16px";
            modal.style.boxSizing = "border-box";

            var card = document.createElement("div");
            card.style.background = "#181825";
            card.style.border = "1px solid #fab387";
            card.style.borderRadius = "16px";
            card.style.padding = "20px";
            card.style.width = "100%";
            card.style.maxWidth = "360px";
            card.style.boxShadow = "0 10px 30px rgba(0,0,0,0.6)";
            card.style.color = "#cdd6f4";
            card.style.fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

            card.innerHTML = "<div style='display:flex; align-items:center; gap:8px; margin-bottom:12px;'>" +
                "<span style='font-size:20px;'>🛑</span>" +
                "<div>" +
                    "<div style='font-weight:bold; font-size:15px; color:#fab387;'>Sisipkan Auto-Stop Loop</div>" +
                    "<div style='font-size:11px; color:#a6adc8;'>Hentikan putaran loop jika teks terdeteksi</div>" +
                "</div>" +
            "</div>" +
            "<div style='font-size:11px; color:#bac2de; margin-bottom:8px;'>KATA KUNCI PEMICU (TEXT / HTML DOM):</div>" +
            "<input id='__agy_stop_input__' type='text' placeholder='Misal: Sold out / Limit reached / Sukses' " +
                "style='width:100%; box-sizing:border-box; background:#11111b; border:1px solid #45475a; border-radius:10px; color:#cdd6f4; padding:10px 12px; font-size:13px; margin-bottom:14px; outline:none;' />" +
            "<div style='display:flex; gap:8px;'>" +
                "<button id='__agy_stop_cancel__' style='flex:1; background:#313244; color:#bac2de; border:none; border-radius:10px; padding:10px; font-weight:600; font-size:12px; cursor:pointer;'>Batal</button>" +
                "<button id='__agy_stop_ok__' style='flex:1; background:#fab387; color:#11111b; border:none; border-radius:10px; padding:10px; font-weight:bold; font-size:12px; cursor:pointer;'>Sisipkan Step</button>" +
            "</div>";

            modal.appendChild(card);
            document.body.appendChild(modal);

            var inp = card.querySelector("#__agy_stop_input__");
            setTimeout(function() { try { inp.focus(); } catch(_) {} }, 150);

            card.querySelector("#__agy_stop_cancel__").onclick = function() { modal.remove(); };
            var self = this;
            card.querySelector("#__agy_stop_ok__").onclick = function() {
                var textVal = (inp.value || "").trim();
                if (!textVal) {
                    alert("Masukkan kata kunci terlebih dahulu!");
                    return;
                }
                modal.remove();
                self.recordEvent({
                    type: "stop_if",
                    text: textVal,
                    condition: "contains",
                    timestamp: Date.now(),
                    desc: "Auto-Stop jika ada: " + textVal
                });
                updateAgyHUD("Auto-Stop Tersisip", "Loop berhenti jika ada: \"" + textVal + "\"", "success");
            };
        },

        startChallengeSelector: function() {
            var self = this;
            var isRec = this.isRecording || window.__FUSION_RECORDING__;
            if (!isRec) {
                alert("Nyalakan mode REC terlebih dahulu!");
                return;
            }

            updateAgyHUD("🎯 Mode Solver (1/2)", "Sentuh elemen Soal / Pertanyaan / Captcha di layar", "recording");

            var pickedChallenge = null;
            var origBorder = "";

            function onPickStep(e) {
                if (isInternalUI(e.target)) return;
                e.preventDefault();
                e.stopPropagation();

                var target = e.target;
                if (!pickedChallenge) {
                    pickedChallenge = target;
                    origBorder = target.style.outline;
                    target.style.outline = "3px dashed #cba6f7";
                    updateAgyHUD("🎯 Mode Solver (2/2)", "Soal terpilih! Sekarang sentuh kolom INPUT jawaban", "recording");
                } else {
                    var inputTarget = target;
                    if (pickedChallenge) {
                        pickedChallenge.style.outline = origBorder;
                    }
                    var inSel = getCssSelector(inputTarget);
                    var chSel = getCssSelector(pickedChallenge);
                    var isImgOrCanvas = pickedChallenge && (pickedChallenge.tagName === "IMG" || pickedChallenge.tagName === "CANVAS" || pickedChallenge.querySelector("img, canvas"));
                    var chText = isImgOrCanvas ? "[Gambar/Canvas Captcha]" : (pickedChallenge.innerText || pickedChallenge.textContent || "").substring(0, 60).trim();
                    var solverType = isImgOrCanvas ? "ocr_vision" : "ai_math_regex";

                    document.removeEventListener("click", onPickStep, true);

                    self.recordEvent({
                        type: "solve_challenge",
                        challengeSelector: chSel,
                        inputSelector: inSel,
                        challengeSample: chText,
                        solverType: solverType,
                        timestamp: Date.now(),
                        desc: (isImgOrCanvas ? "👁️ Vision OCR: " : "AI/Regex Solver: ") + (chText || chSel).substring(0, 20) + " ➔ " + (inSel || "input")
                    });

                    updateAgyHUD("✅ Solver Tersisip", isImgOrCanvas ? "Captcha gambar akan di-solve oleh Vision AI!" : "Tantangan akan di-solve otomatis oleh AI/Regex!", "success");
                }
            }

            document.addEventListener("click", onPickStep, true);
        },

        startRewardSelector: function() {
            var self = this;
            var isRec = this.isRecording || window.__FUSION_RECORDING__;
            if (!isRec) {
                alert("Nyalakan mode REC terlebih dahulu!");
                return;
            }

            var existing = document.getElementById("__agy_reward_modal__");
            if (existing) existing.remove();

            var modal = document.createElement("div");
            modal.id = "__agy_reward_modal__";
            modal.style.position = "fixed";
            modal.style.top = "0";
            modal.style.left = "0";
            modal.style.width = "100%";
            modal.style.height = "100%";
            modal.style.background = "rgba(10, 10, 18, 0.75)";
            modal.style.backdropFilter = "blur(6px)";
            modal.style.webkitBackdropFilter = "blur(6px)";
            modal.style.display = "flex";
            modal.style.alignItems = "center";
            modal.style.justifyContent = "center";
            modal.style.zIndex = "99999999";
            modal.style.padding = "16px";
            modal.style.boxSizing = "border-box";

            var card = document.createElement("div");
            card.style.background = "#181825";
            card.style.border = "1px solid #a6e3a1";
            card.style.borderRadius = "16px";
            card.style.padding = "20px";
            card.style.width = "100%";
            card.style.maxWidth = "360px";
            card.style.boxShadow = "0 10px 30px rgba(0,0,0,0.6)";
            card.style.color = "#cdd6f4";
            card.style.fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";

            card.innerHTML = "<div style='display:flex; align-items:center; gap:8px; margin-bottom:12px;'>" +
                "<span style='font-size:20px;'>🎁</span>" +
                "<div>" +
                    "<div style='font-weight:bold; font-size:15px; color:#a6e3a1;'>Ekstrak Hasil Klaim / Reward</div>" +
                    "<div style='font-size:11px; color:#a6adc8;'>Tangkap token dari Teks (gate.js) atau Elemen DOM</div>" +
                "</div>" +
            "</div>" +
            "<div style='font-size:11px; color:#bac2de; margin-bottom:6px;'>REGEX PENCARI TOKEN (KOSONGKAN UNTUK AUTO-SCAN):</div>" +
            "<input id='__agy_reward_regex_input__' type='text' placeholder='Misal: ([0-9.]+)\\s*USDT atau kosongkan' " +
                "style='width:100%; box-sizing:border-box; background:#11111b; border:1px solid #45475a; border-radius:10px; color:#cdd6f4; padding:10px 12px; font-size:13px; margin-bottom:14px; outline:none;' />" +
            "<div style='display:flex; flex-direction:column; gap:8px;'>" +
                "<button id='__agy_reward_btn_text__' style='background:#a6e3a1; color:#11111b; border:none; border-radius:10px; padding:11px; font-weight:bold; font-size:12px; cursor:pointer;'>⚡ Simpan Auto-Text Scanner (gate.js)</button>" +
                "<button id='__agy_reward_btn_dom__' style='background:#313244; color:#cdd6f4; border:1px solid #45475a; border-radius:10px; padding:10px; font-weight:600; font-size:12px; cursor:pointer;'>🎯 Sentuh Elemen di Layar</button>" +
                "<button id='__agy_reward_btn_cancel__' style='background:transparent; color:#6c7086; border:none; padding:6px; font-size:11px; cursor:pointer;'>Batal</button>" +
            "</div>";

            modal.appendChild(card);
            (document.body || document.documentElement).appendChild(modal);

            card.querySelector("#__agy_reward_btn_cancel__").onclick = function() { modal.remove(); };

            // OPSI 1: Auto-Text / Regex Scanner (Pola gate.js)
            card.querySelector("#__agy_reward_btn_text__").onclick = function() {
                var regexVal = (card.querySelector("#__agy_reward_regex_input__").value || "").trim();
                modal.remove();
                self.recordEvent({
                    type: "reward_extractor",
                    mode: "text_regex",
                    regex: regexVal || "([0-9]+(?:\\.[0-9]+)?)\\s*([A-Za-z0-9_]{2,10})",
                    timestamp: Date.now(),
                    desc: "Scan Text/Toast: " + (regexVal || "Auto-Token (gate.js)")
                });
                updateAgyHUD("✅ Text Reward Tersisip", "Mesin akan scan teks halaman & toast secara otomatis (gate.js)!", "success");
            };

            // OPSI 2: Sentuh Elemen DOM
            card.querySelector("#__agy_reward_btn_dom__").onclick = function() {
                modal.remove();
                updateAgyHUD("🎁 Mode Sentuh Elemen", "Sentuh elemen NOMINAL / TOKEN di layar", "recording");

                function onPickReward(e) {
                    if (isInternalUI(e.target)) return;
                    e.preventDefault();
                    e.stopPropagation();

                    var target = e.target;
                    var rSel = getCssSelector(target);
                    var sample = (target.innerText || target.textContent || "").substring(0, 40).trim();

                    document.removeEventListener("click", onPickReward, true);

                    self.recordEvent({
                        type: "reward_extractor",
                        mode: "dom_selector",
                        amountSelector: rSel,
                        sampleText: sample,
                        timestamp: Date.now(),
                        desc: "Ekstrak Elemen: " + (sample || rSel).substring(0, 24)
                    });

                    updateAgyHUD("✅ Reward Tersisip", "Nominal (" + (sample || rSel) + ") akan dicatat tiap loop!", "success");
                }

                document.addEventListener("click", onPickReward, true);
            };
        },

        setAiEngine: function(eng) {
            setAIEngineGlobal(eng);
        },

        cycleAiEngine: function() {
            var cur = getAIEngine();
            var next = "auto";
            if (cur === "auto") next = "deepseek";
            else if (cur === "deepseek") next = "9router";
            else if (cur === "9router") next = "agy";
            else if (cur === "agy") next = "off";
            else next = "auto";
            setAIEngineGlobal(next);
            return next;
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
        AgyBatchRunner.run(events, AgyRecorder.sessionName || "Resep", "1", false, null, "raw", 1);
        return "PLAYING";
    };

    // --- 5B. SMART WALLET ADDRESS PASTER (ADDR > BUTTON DARI WALLET JSON) ---
    window.__AGY_PASTE_WALLET__ = function(passedAddr) {
        var wAddr = (typeof passedAddr === "string" && passedAddr.trim().length > 0) ? passedAddr.trim() : "";
        if (!wAddr) {
            var activeW = (typeof AgyDB !== "undefined" && AgyDB.getActiveWallet) ? AgyDB.getActiveWallet() : null;
            if (activeW && activeW.address) wAddr = activeW.address;
        }
        if (!wAddr) {
            try {
                if (window.GhostBridge && window.GhostBridge.getWalletAddress) {
                    wAddr = window.GhostBridge.getWalletAddress();
                }
            } catch(_) {}
        }
        if (!wAddr) {
            var wList = (typeof AgyDB !== "undefined" && AgyDB.getWallets) ? AgyDB.getWallets() : [];
            if (wList && wList.length > 0 && wList[0].address) {
                wAddr = wList[0].address;
            }
        }
        if (!wAddr) {
            wAddr = "0x87A6cD8c67649cb1c2221D87FedC3fCf2b22b359";
        }

        // 1. Deteksi input yang sedang aktif / fokus
        var targetInp = null;
        var act = document.activeElement;
        if (act && (act.tagName === "INPUT" || act.tagName === "TEXTAREA") && !isInternalUI(act)) {
            targetInp = act;
        }

        // 2. Jika tidak ada yang fokus, cari kandidat kolom alamat di DOM/Modal
        if (!targetInp) {
            var selectors = [
                "input#inp-wallet",
                "input#w",
                "input#wallet",
                "input#address",
                "input[name*='wallet' i]",
                "input[name*='address' i]",
                "input[name*='recipient' i]",
                "input[placeholder*='0x' i]",
                "input[placeholder*='wallet' i]",
                "input[placeholder*='address' i]",
                "input[placeholder*='收款' i]",
                "input[placeholder*='地址' i]",
                "input[type='text']:not([id^='__agy_'])"
            ];
            for (var i = 0; i < selectors.length; i++) {
                var el = document.querySelector(selectors[i]);
                if (el && !isInternalUI(el)) {
                    var r = el.getBoundingClientRect();
                    if (r.width > 20 && r.height > 15) {
                        targetInp = el;
                        break;
                    }
                }
            }
        }

        if (!targetInp) {
            updateAgyHUD("ADDR >", "Klik kolom input di web dulu, lalu tekan ADDR >", "warning");
            return false;
        }

        try { targetInp.focus(); } catch(_) {}

        var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                     Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
        if (setter) setter.call(targetInp, wAddr);
        else targetInp.value = wAddr;

        targetInp.dispatchEvent(new Event("input", { bubbles: true }));
        targetInp.dispatchEvent(new Event("change", { bubbles: true }));

        highlightRecordedElement(targetInp);

        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (isRec) {
            AgyRecorder.recordEvent({
                type: "fill",
                timestamp: Date.now(),
                selector: getCssSelector(targetInp),
                tag: targetInp.tagName.toLowerCase(),
                id: targetInp.id || "",
                name: targetInp.name || "",
                placeholder: targetInp.placeholder || "",
                value: "{{WALLET_ADDRESS}}",
                isDynamic: true,
                text: "ADDR >: {{WALLET_ADDRESS}}"
            });
            updateAgyHUD("ADDR > Terekam", "Ditempel: {{WALLET_ADDRESS}} (" + wAddr.substring(0, 6) + "..." + wAddr.substring(38) + ")", "success");
        } else {
            updateAgyHUD("ADDR > Berhasil", "Ditempel: " + wAddr.substring(0, 10) + "...", "success");
        }
        return true;
    };
    window.__FUSION_PASTE_WALLET__ = window.__AGY_PASTE_WALLET__;

    // --- 5C. SMART PASSWORD PASTER (PWD > BUTTON UNTUK RED PACKET / KODE KLAIM) ---
    window.__AGY_PASTE_PWD__ = function(passedPwd) {
        var pwdVal = passedPwd || window.__AGY_ACTIVE_PASSWORD__ || "";
        if (!pwdVal) {
            pwdVal = prompt("Masukkan Password / 口令 Klaim:", "");
            if (pwdVal) {
                window.__AGY_ACTIVE_PASSWORD__ = pwdVal;
            }
        }
        if (!pwdVal) return false;

        var targetInp = null;
        var act = document.activeElement;
        if (act && (act.tagName === "INPUT" || act.tagName === "TEXTAREA") && !isInternalUI(act)) {
            targetInp = act;
        }

        if (!targetInp) {
            var selectors = [
                "input#inp-pwd",
                "input#pwd",
                "input#password",
                "input[name*='pwd' i]",
                "input[name*='password' i]",
                "input[placeholder*='口令' i]",
                "input[placeholder*='password' i]",
                "input[placeholder*='kode' i]",
                "input[placeholder*='sandi' i]",
                "input[type='password']"
            ];
            for (var i = 0; i < selectors.length; i++) {
                var el = document.querySelector(selectors[i]);
                if (el && !isInternalUI(el)) {
                    var r = el.getBoundingClientRect();
                    if (r.width > 20 && r.height > 15) {
                        targetInp = el;
                        break;
                    }
                }
            }
        }

        if (!targetInp) {
            updateAgyHUD("PWD >", "Klik kolom password dulu, lalu tekan PWD >", "warning");
            return false;
        }

        try { targetInp.focus(); } catch(_) {}

        var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set ||
                     Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
        if (setter) setter.call(targetInp, pwdVal);
        else targetInp.value = pwdVal;

        targetInp.dispatchEvent(new Event("input", { bubbles: true }));
        targetInp.dispatchEvent(new Event("change", { bubbles: true }));

        highlightRecordedElement(targetInp);

        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (isRec) {
            AgyRecorder.recordEvent({
                type: "fill",
                timestamp: Date.now(),
                selector: getCssSelector(targetInp),
                tag: targetInp.tagName.toLowerCase(),
                id: targetInp.id || "",
                name: targetInp.name || "",
                placeholder: targetInp.placeholder || "",
                value: "{{PASSWORD}}",
                isDynamic: true,
                optional: true,
                timeout: 1500,
                text: "PWD >: {{PASSWORD}}"
            });
            updateAgyHUD("PWD > Terekam", "Ditempel: {{PASSWORD}}", "success");
        } else {
            updateAgyHUD("PWD > Berhasil", "Ditempel: " + pwdVal.substring(0, 10) + "...", "success");
        }
        return true;
    };
    window.__FUSION_PASTE_PWD__ = window.__AGY_PASTE_PWD__;

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
    var __agyTouchStartX = 0, __agyTouchStartY = 0, __agyTouchStartTime = 0, __agyTouchMoved = false;
    var __lastAgyRecordedClickTime = 0;
    var __agyPendingTap = null;
    var __agyPendingTapTimer = null;

    // Helper visual feedback highlight saat elemen berhasil direkam
    function highlightRecordedElement(el) {
        if (!el || !el.style) return;
        try {
            var origOutline = el.style.outline;
            var origBoxShadow = el.style.boxShadow;
            var origTransition = el.style.transition;
            el.style.transition = "box-shadow 0.2s ease, outline 0.2s ease";
            el.style.outline = "2px solid #00E5FF";
            el.style.boxShadow = "0 0 16px rgba(0, 229, 255, 0.75)";
            setTimeout(function() {
                try {
                    el.style.outline = origOutline || "";
                    el.style.boxShadow = origBoxShadow || "";
                    el.style.transition = origTransition || "";
                } catch(_) {}
            }, 600);
        } catch(_) {}
    }

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

    // Pemeriksaan ketat: apakah elemen memiliki alasan kuat sebagai elemen interaktif?
    function isActionableElement(el) {
        if (!el || el.nodeType !== 1) return false;
        if (isInternalUI(el)) return false;

        var tag = el.tagName.toLowerCase();
        if (tag === "html" || tag === "body") return false;

        // 1. Tag interaktif standar HTML
        if (tag === "button" || tag === "a" || tag === "input" || tag === "select" || tag === "textarea" || tag === "label" || tag === "summary") {
            return true;
        }

        // 2. ARIA role interaktif
        var role = el.getAttribute("role");
        if (role && /^(button|link|tab|menuitem|checkbox|radio|switch|treeitem|option)$/i.test(role)) {
            return true;
        }

        // 3. Handler event atau atribut framework
        if (el.hasAttribute("onclick") || el.hasAttribute("data-action") || el.hasAttribute("data-click") ||
            el.hasAttribute("@click") || el.hasAttribute("v-on:click") || el.hasAttribute("ng-click")) {
            return true;
        }

        // 4. Tabindex interaktif
        var tabIndex = el.getAttribute("tabindex");
        if (tabIndex !== null && tabIndex !== "" && !isNaN(tabIndex) && parseInt(tabIndex, 10) >= 0) {
            return true;
        }

        // 5. Penamaan Class / ID / TestID aksi (khusus dApp, button kustom, red packet / open-btn dsb)
        var id = (typeof el.id === "string") ? el.id.toLowerCase() : "";
        var cls = (typeof el.className === "string") ? el.className.toLowerCase() : "";
        var testid = (el.getAttribute("data-testid") || "").toLowerCase();
        var ariaLabel = (el.getAttribute("aria-label") || "").toLowerCase();

        var actionRegex = /(?:^|[\s_\-])(btn|button|open-btn|clickable|action|touchable|pressable|claim|mint|connect|submit|close-btn|menu-item|tap-target)(?:[\s_\-]|$)/i;
        if (actionRegex.test(cls) || actionRegex.test(id) || actionRegex.test(testid)) {
            return true;
        }

        // Khusus open button di red packet atau envelope
        if (id === "open" || cls.includes("open-btn") || ariaLabel.includes("打开") || ariaLabel.includes("红包") || ariaLabel.includes("open")) {
            return true;
        }

        // 6. CSS Cursor Pointer
        try {
            var cs = window.getComputedStyle(el);
            if (cs && cs.cursor === "pointer") {
                return true;
            }
        } catch(_) {}

        return false;
    }

    // Helper fungsi pencari target interaktif DOM sejati (Support Button, Link, Div SPA, SVG Icon, dApp)
    function findBestClickTarget(el) {
        if (!el || el.nodeType !== 1) return null;
        if (isInternalUI(el)) return null;

        var tag = el.tagName.toLowerCase();
        if (tag === "html" || tag === "body") return null;

        // 1. Periksa elemen itu sendiri atau ancestor terdekat hingga 6 tingkatan
        var cur = el;
        var depth = 0;
        while (cur && cur.nodeType === 1 && depth < 6) {
            if (isInternalUI(cur)) return null;
            var ctag = cur.tagName.toLowerCase();
            if (ctag === "html" || ctag === "body") break;

            if (isActionableElement(cur)) {
                return cur;
            }
            cur = cur.parentElement;
            depth++;
        }

        // 2. Jika elemen berupa grafis/icon (svg, path, img, i), cari pembungkus tombol atau link
        if (tag === "path" || tag === "svg" || tag === "img" || tag === "i") {
            var container = el.closest("button, a, [role='button'], div, span");
            if (container && !isInternalUI(container) && isActionableElement(container)) {
                return container;
            }
        }

        // 3. Fallback elemen daun (leaf element): Jika elemen bukan kontainer layout (tidak membungkus kontrol
        // interaktif lain dan ukurannya wajar), izinkan elemen seperti chip, tab pill, atau span kustom agar
        // kompatibel 100% dengan berbagai situs web unik tanpa merusak logika DOM asli.
        var rect = el.getBoundingClientRect();
        var vw = window.innerWidth || (document.documentElement ? document.documentElement.clientWidth : 360);

        var hasInteractiveChildren = false;
        try {
            if (el.querySelector("button, a, input, select, textarea, [role='button'], [role='link']")) {
                hasInteractiveChildren = true;
            }
        } catch(_) {}

        // Elemen kontainer/kartu/amplop besar (membungkus tombol atau tinggi > 140px) DITOLAK KERAS!
        if (!hasInteractiveChildren && rect.width > 4 && rect.height > 4 && rect.width <= (vw * 0.85) && rect.height <= 140) {
            return el;
        }

        return null;
    }

    // Penetrasi titik layar: mencari elemen interaktif di bawah koordinat (x, y) melalui seluruh layer DOM
    function findActionableAtPoint(x, y) {
        if (typeof document.elementsFromPoint !== "function") return null;
        try {
            var stack = document.elementsFromPoint(x, y);
            if (!stack || stack.length === 0) return null;
            for (var i = 0; i < stack.length; i++) {
                var el = stack[i];
                if (isInternalUI(el)) continue;
                var target = findBestClickTarget(el);
                if (target) return target;
            }
        } catch(_) {}
        return null;
    }

    // Fungsi terpusat perekam DOM Click dengan validasi dan visual highlight
    function recordActionableClick(target, clientX, clientY, sourceTag) {
        if (!target) return;
        if (isInternalUI(target)) return;
        if (!document.documentElement.contains(target)) return;

        var now = Date.now();
        // Strict Debounce: cegah event ganda atau rangkap dari touch+click dalam 350ms
        if (now - __lastAgyRecordedClickTime < 350) {
            return;
        }
        __lastAgyRecordedClickTime = now;

        var ev = {
            type: "click",
            timestamp: now,
            x: clientX,
            y: clientY,
            selector: getCssSelector(target),
            tag: target.tagName.toLowerCase(),
            id: target.id || "",
            name: target.name || "",
            testid: target.getAttribute("data-testid") || "",
            ariaLabel: target.getAttribute("aria-label") || "",
            text: (target.innerText || target.value || target.getAttribute("aria-label") || "").substring(0, 30).trim()
        };

        highlightRecordedElement(target);
        AgyRecorder.recordEvent(ev);
    }

    document.addEventListener("touchstart", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;

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
        __agyTouchMoved = false;
    }, { capture: true, passive: true });

    document.addEventListener("touchmove", function(e) {
        var t = e.touches[0];
        if (t) {
            var dX = Math.abs(t.clientX - __agyTouchStartX);
            var dY = Math.abs(t.clientY - __agyTouchStartY);
            if (dX > 12 || dY > 12) {
                __agyTouchMoved = true;
            }
        }
    }, { capture: true, passive: true });

    document.addEventListener("touchend", function(e) {
        var isRec = AgyRecorder.isRecording || window.__FUSION_RECORDING__;
        if (!isRec) return;

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
        var mode = AgyRecorder.recordMode;

        // 1. Deteksi Gestur Usapan / Swipe (Mode Touch & Hybrid)
        if ((mode === "touch" || mode === "hybrid") && (absX >= 40 || absY >= 40)) {
            var dir = "down";
            if (absY >= absX) {
                dir = deltaY < 0 ? "up" : "down";
            } else {
                dir = deltaX < 0 ? "left" : "right";
            }

            var swipeEv = {
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

            AgyRecorder.recordEvent(swipeEv);
            updateAgyHUD("Swipe Direkam", dir.toUpperCase() + " (" + Math.round(Math.max(absX, absY)) + "px)", "recording");
            return;
        }

        // 2. Deteksi Tap Jempol (Pergerakan < 15px dan durasi < 550ms)
        if (!__agyTouchMoved && absX < 15 && absY < 15 && elapsed < 550) {
            // Mode Touch Murni: rekam sentuhan layar koordinat
            if (mode === "touch") {
                var touchEv = {
                    type: "touch",
                    x: Math.round(t.clientX),
                    y: Math.round(t.clientY),
                    timestamp: Date.now()
                };
                AgyRecorder.recordEvent(touchEv);
                return;
            }

            // Mode DOM atau Hybrid: cari target interaktif sejati
            var target = findBestClickTarget(e.target) || findActionableAtPoint(t.clientX, t.clientY);
            if (target && !isInternalUI(target)) {
                if (__agyPendingTapTimer) {
                    clearTimeout(__agyPendingTapTimer);
                    __agyPendingTapTimer = null;
                }

                __agyPendingTap = {
                    target: target,
                    x: t.clientX,
                    y: t.clientY,
                    time: Date.now(),
                    recorded: false
                };

                // Fallback Timer: Jika dApp (seperti bindTap / FastClick) memanggil e.preventDefault() pada touchend,
                // browser Chromium TIDAK AKAN PERNAH menembakkan event click!
                // Timer ini akan mensintesis klik setelah 45ms jika native click tidak muncul.
                __agyPendingTapTimer = setTimeout(function() {
                    if (__agyPendingTap && !__agyPendingTap.recorded) {
                        __agyPendingTap.recorded = true;
                        recordActionableClick(__agyPendingTap.target, __agyPendingTap.x, __agyPendingTap.y, "touch-synthesizer");
                    }
                    __agyPendingTap = null;
                    __agyPendingTapTimer = null;
                }, 45);
            }
        }
    }, { capture: true, passive: true });

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

        // Tandai pending tap sudah tercover oleh native click agar timer tidak menembak dobel
        if (__agyPendingTap) {
            __agyPendingTap.recorded = true;
        }
        if (__agyPendingTapTimer) {
            clearTimeout(__agyPendingTapTimer);
            __agyPendingTapTimer = null;
        }

        // Target resolution: cari elemen interaktif terbaik atau penetrasi stack layer
        var target = findBestClickTarget(e.target) || findActionableAtPoint(e.clientX, e.clientY);
        if (!target) return;
        if (isInternalUI(target)) return;

        recordActionableClick(target, e.clientX, e.clientY, "native-click");
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

    // --- 6B. DYNAMIC FORM FOCUS DETECTOR FOR TOOL BAR (COPY <-> PASTE) ---
    function notifyFormFocusState() {
        var el = document.activeElement;
        var hasFocus = false;
        if (el) {
            var tag = (el.tagName || "").toUpperCase();
            if (tag === "INPUT" || tag === "TEXTAREA" || el.isContentEditable || (el.getAttribute && el.getAttribute("contenteditable") === "true")) {
                var type = (el.type || "").toLowerCase();
                if (type !== "button" && type !== "submit" && type !== "reset" && type !== "checkbox" && type !== "radio" && type !== "file") {
                    hasFocus = true;
                }
            }
        }
        if (window.GhostBridge && window.GhostBridge.onFormFocusChanged) {
            try { window.GhostBridge.onFormFocusChanged(hasFocus); } catch(_) {}
        }
    }
    document.addEventListener("focusin", function(e) { notifyFormFocusState(); }, true);
    document.addEventListener("focusout", function(e) { setTimeout(notifyFormFocusState, 80); }, true);
    document.addEventListener("click", function(e) { setTimeout(notifyFormFocusState, 80); }, true);

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
        card.style.overflowX = "hidden";
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

        var sitePrefix = getAgySitePrefix();
        var defaultGenerated = sitePrefix ? (sitePrefix + "_makro") : ("Resep_" + new Date().getHours() + "h" + new Date().getMinutes() + "m");

        // Input Box
        var inp = document.createElement("input");
        inp.type = "text";
        inp.value = defaultName || defaultGenerated;
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

        // Input Label Auto-Stop
        var lblStop = document.createElement("div");
        lblStop.innerText = "AUTO-STOP JIKA TERDETEKSI KATA (OPSIONAL):";
        lblStop.style.fontSize = "10px";
        lblStop.style.fontWeight = "700";
        lblStop.style.color = "#fab387";
        lblStop.style.letterSpacing = "0.6px";
        lblStop.style.marginBottom = "6px";
        card.appendChild(lblStop);

        var inpStop = document.createElement("input");
        inpStop.type = "text";
        inpStop.placeholder = "Misal: Sold out / Limit reached / Sukses...";
        inpStop.style.width = "100%";
        inpStop.style.boxSizing = "border-box";
        inpStop.style.background = "#11111b";
        inpStop.style.border = "1px solid #45475a";
        inpStop.style.borderRadius = "12px";
        inpStop.style.color = "#cdd6f4";
        inpStop.style.padding = "10px 14px";
        inpStop.style.fontSize = "12px";
        inpStop.style.fontWeight = "500";
        inpStop.style.marginBottom = "14px";
        inpStop.style.outline = "none";
        inpStop.onfocus = function() { inpStop.style.borderColor = "#fab387"; };
        inpStop.onblur = function() { inpStop.style.borderColor = "#45475a"; };
        card.appendChild(inpStop);

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
            var rName = (inp.value || "").trim() || defaultGenerated;
            var clean = rName.replace(/[^a-zA-Z0-9_-]/g, "_");
            if (sitePrefix && clean.toLowerCase().indexOf(sitePrefix.toLowerCase()) !== 0) {
                clean = sitePrefix + "_" + clean.replace(/^(resep_|makro_)/i, "");
            }
            var stopKeyword = (inpStop.value || "").trim();
            if (stopKeyword) {
                var hasSame = events.some(function(e) { return (e.type === "stop_if" || e.type === "stop_not") && e.text === stopKeyword; });
                if (!hasSame) {
                    events.push({
                        type: "stop_if",
                        text: stopKeyword,
                        condition: "contains",
                        desc: "Auto-Stop jika ada: " + stopKeyword,
                        timestamp: Date.now()
                    });
                }
            }
            var curUrl = (window.location && window.location.href) ? window.location.href.split("#")[0] : "";
            RecipeStore.save(clean, events, curUrl);
            updateAgyHUD("Resep Tersimpan", clean + " (" + events.length + " langkah)", "success");
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
        card.style.overflowX = "hidden";

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

            // Pointer & Mouse Events (Mousedown & mouseup untuk active visual state)
            if (window.PointerEvent) {
                try {
                    targetEl.dispatchEvent(new PointerEvent("pointerdown", commonOpts));
                    targetEl.dispatchEvent(new PointerEvent("pointerup", commonOpts));
                } catch(_) {}
            }
            try {
                targetEl.dispatchEvent(new MouseEvent("mousedown", commonOpts));
                targetEl.dispatchEvent(new MouseEvent("mouseup", commonOpts));
            } catch(_) {}

            // Single-fire execution: Panggil native .click() ATAU dispatchEvent, TIDAK PERNAH keduanya!
            if (typeof targetEl.click === "function") {
                try {
                    targetEl.click();
                } catch(_) {
                    try { targetEl.dispatchEvent(new MouseEvent("click", commonOpts)); } catch(_) {}
                }
            } else {
                try { targetEl.dispatchEvent(new MouseEvent("click", commonOpts)); } catch(_) {}
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
        card.style.overflowX = "hidden";

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
        card.style.overflowX = "hidden";
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

        // Deteksi dompet aktif saat ini untuk fokus 1 wallet
        var activeW = (typeof AgyDB !== "undefined" && AgyDB.getActiveWallet) ? AgyDB.getActiveWallet() : { name: "Master", address: "" };
        var activeWName = activeW.name || "Master";
        var activeWAddr = activeW.address || "";
        var activeIdx = 1;
        for (var wi = 0; wi < wallets.length; wi++) {
            if (wallets[wi].address && activeWAddr && wallets[wi].address.toLowerCase() === activeWAddr.toLowerCase()) {
                activeIdx = wi + 1;
                break;
            }
        }

        // 4-Mode Segmented Control (Default: ⚡ 100% Asli Mengikuti Rekaman)
        var currentRunMode = "raw"; // "raw" | "single" | "standalone" | "multi"

        var modeSegment = document.createElement("div");
        modeSegment.style.display = "flex";
        modeSegment.style.background = "#11111b";
        modeSegment.style.border = "1px solid #313244";
        modeSegment.style.borderRadius = "12px";
        modeSegment.style.padding = "3px";
        modeSegment.style.marginBottom = "8px";
        modeSegment.style.gap = "4px";

        var btnRaw = document.createElement("button");
        var btnSingle = document.createElement("button");
        var btnStandalone = document.createElement("button");
        var btnMulti = document.createElement("button");

        var modeDesc = document.createElement("div");
        modeDesc.style.fontSize = "10.5px";
        modeDesc.style.color = "#bac2de";
        modeDesc.style.marginBottom = "14px";
        modeDesc.style.padding = "6px 10px";
        modeDesc.style.background = "#11111b";
        modeDesc.style.borderRadius = "8px";
        modeDesc.style.border = "1px solid #313244";
        modeDesc.style.lineHeight = "1.35";

        var updateModeTabs = function() {
            var tabs = [
                { btn: btnRaw, id: "raw", label: "⚡ 100% Asli" },
                { btn: btnSingle, id: "single", label: "🎯 Fokus 1" },
                { btn: btnStandalone, id: "standalone", label: "🌐 Tanpa Wallet" },
                { btn: btnMulti, id: "multi", label: "🔄 Rotasi" }
            ];
            tabs.forEach(function(t) {
                var active = (currentRunMode === t.id);
                t.btn.innerText = t.label;
                t.btn.style.flex = "1";
                t.btn.style.border = "none";
                t.btn.style.borderRadius = "9px";
                t.btn.style.padding = "7px 2px";
                t.btn.style.fontSize = "9.5px";
                t.btn.style.fontWeight = active ? "700" : "500";
                t.btn.style.cursor = "pointer";
                t.btn.style.background = active ? "linear-gradient(135deg, rgba(203, 166, 247, 0.3) 0%, rgba(137, 220, 235, 0.25) 100%)" : "transparent";
                t.btn.style.color = active ? "#cdd6f4" : "#a6adc8";
                t.btn.style.boxShadow = active ? "0 2px 8px rgba(0,0,0,0.4)" : "none";
            });

            if (currentRunMode === "raw") {
                modeDesc.innerHTML = "⚡ <b style='color:#a6e3a1;'>100% Ikuti Rekaman:</b> Putar alur persis apa adanya langkah demi langkah tanpa aturan atau pergantian dompet.";
                targetSection.style.display = "none";
                quickRowMulti.style.display = "none";
                quickRowSingle.style.display = "none";
                loopSection.style.display = "block";
                loopLabel.innerText = "JUMLAH PUTARAN (LOOP COUNT):";
                batchCard.style.display = "none";
                sWallets.innerHTML = "<span style='color:#a6e3a1; font-weight:700;'>⚡ 100% Murni</span> Sesuai Rekaman";
            } else if (currentRunMode === "single") {
                modeDesc.innerHTML = "🎯 <b style='color:#cba6f7;'>Fokus 1 Wallet:</b> Hanya berjalan untuk 1 dompet (aksi ganti dompet di rekaman otomatis dilewati).";
                targetSection.style.display = "block";
                targetLabel.innerText = "DOMPET FOKUS:";
                targetInp.placeholder = "Nomor Akun (contoh: " + activeIdx + ")";
                targetInp.value = String(activeIdx);
                quickRowMulti.style.display = "none";
                quickRowSingle.style.display = "flex";
                loopSection.style.display = "block";
                loopLabel.innerText = "JUMLAH PUTARAN (LOOP COUNT):";
                batchCard.style.display = "none";
                sWallets.innerHTML = "<span style='color:#cba6f7; font-weight:700;'>🎯 Fokus</span> 1 Dompet";
            } else if (currentRunMode === "standalone") {
                modeDesc.innerHTML = "🌐 <b style='color:#89dceb;'>Tanpa Wallet:</b> Macro web browser murni tanpa dompet/web3 (aksi ganti dompet dilewati).";
                targetSection.style.display = "none";
                quickRowMulti.style.display = "none";
                quickRowSingle.style.display = "none";
                loopSection.style.display = "block";
                loopLabel.innerText = "JUMLAH PUTARAN (LOOP COUNT):";
                batchCard.style.display = "none";
                sWallets.innerHTML = "<span style='color:#89dceb; font-weight:700;'>🌐 Standalone</span> (Tanpa Wallet)";
            } else {
                modeDesc.innerHTML = "🔄 <b style='color:#f9e2af;'>Rotasi Tuyul:</b> Putar resep bergantian secara otomatis ke banyak dompet (batch loop).";
                targetSection.style.display = "block";
                targetLabel.innerText = "TARGET AKUN DOMPET:";
                targetInp.placeholder = "1  atau  1-5  atau  1,3,4  atau  *";
                targetInp.value = "1";
                quickRowMulti.style.display = "flex";
                quickRowSingle.style.display = "none";
                loopSection.style.display = "block";
                loopLabel.innerText = "PUTARAN ROTASI (LOOP COUNT):";
                batchCard.style.display = "flex";
                sWallets.innerHTML = "<span style='color:#a6e3a1; font-weight:700;'>👛 " + wallets.length + "</span> Akun Dompet";
            }
        };

        btnRaw.onclick = function() { currentRunMode = "raw"; updateModeTabs(); };
        btnSingle.onclick = function() { currentRunMode = "single"; updateModeTabs(); };
        btnStandalone.onclick = function() { currentRunMode = "standalone"; updateModeTabs(); };
        btnMulti.onclick = function() { currentRunMode = "multi"; updateModeTabs(); };

        modeSegment.appendChild(btnRaw);
        modeSegment.appendChild(btnSingle);
        modeSegment.appendChild(btnStandalone);
        modeSegment.appendChild(btnMulti);
        card.appendChild(modeSegment);
        card.appendChild(modeDesc);

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
        targetInp.value = String(activeIdx);
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
            chip.type = "button";
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
            chip.style.outline = "none";
            chip.style.touchAction = "manipulation";
            var handleTargetChip = function(e) {
                if (e) {
                    try { e.preventDefault(); } catch(_) {}
                    try { e.stopPropagation(); } catch(_) {}
                }
                targetInp.value = val;
            };
            chip.addEventListener("pointerdown", handleTargetChip, { passive: false });
            chip.addEventListener("touchstart", handleTargetChip, { passive: false });
            chip.addEventListener("click", handleTargetChip);
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
        quickRowSingle.appendChild(makeChip("🎯 Aktif (#" + activeIdx + ")", String(activeIdx)));
        if (wallets.length >= 1 && activeIdx !== 1) quickRowSingle.appendChild(makeChip("Master (#1)", "1"));
        if (wallets.length >= 2 && activeIdx !== 2) quickRowSingle.appendChild(makeChip("Tuyul 2 (#2)", "2"));
        if (wallets.length >= 4 && activeIdx !== 4) quickRowSingle.appendChild(makeChip("Tuyul 4 (#4)", "4"));
        targetSection.appendChild(quickRowSingle);

        card.appendChild(targetSection);

        // Loop Count Section (Universal untuk semua 4 mode)
        var loopSection = document.createElement("div");
        loopSection.style.marginBottom = "14px";
        loopSection.style.display = "block";

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
        loopInp.min = "1";
        loopInp.value = "1"; // Default 1x
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

        var loopChips = [];
        var updateChipStyles = function(curVal) {
            for (var c = 0; c < loopChips.length; c++) {
                var item = loopChips[c];
                var isActive = (String(curVal) === String(item.val));
                item.btn.style.background = isActive ? "linear-gradient(135deg, rgba(203, 166, 247, 0.35) 0%, rgba(137, 220, 235, 0.3) 100%)" : "#1e1e2e";
                item.btn.style.borderColor = isActive ? "#cba6f7" : "#313244";
                item.btn.style.color = isActive ? "#ffffff" : "#a6adc8";
                item.btn.style.boxShadow = isActive ? "0 2px 8px rgba(203, 166, 247, 0.3)" : "none";
            }
        };

        var makeLoopChip = function(lbl, val) {
            var chip = document.createElement("button");
            chip.type = "button";
            chip.innerText = lbl;
            chip.style.flex = "1";
            chip.style.background = "#1e1e2e";
            chip.style.border = "1px solid #313244";
            chip.style.color = "#a6adc8";
            chip.style.borderRadius = "8px";
            chip.style.padding = "7px 2px";
            chip.style.fontSize = "10px";
            chip.style.fontWeight = "600";
            chip.style.cursor = "pointer";
            chip.style.outline = "none";
            chip.style.touchAction = "manipulation";

            var handleSelect = function(e) {
                if (e) {
                    try { e.preventDefault(); } catch(_) {}
                    try { e.stopPropagation(); } catch(_) {}
                }
                loopInp.value = val;
                updateChipStyles(val);
            };

            chip.addEventListener("pointerdown", handleSelect, { passive: false });
            chip.addEventListener("touchstart", handleSelect, { passive: false });
            chip.addEventListener("click", handleSelect);

            loopChips.push({ btn: chip, val: val });
            return chip;
        };

        loopChipsRow.appendChild(makeLoopChip("1x", "1"));
        loopChipsRow.appendChild(makeLoopChip("5x", "5"));
        loopChipsRow.appendChild(makeLoopChip("10x", "10"));
        loopChipsRow.appendChild(makeLoopChip("25x", "25"));
        loopChipsRow.appendChild(makeLoopChip("♾️ Non-stop", "999999"));
        loopSection.appendChild(loopChipsRow);
        card.appendChild(loopSection);

        loopInp.oninput = function() {
            updateChipStyles(loopInp.value);
        };
        updateChipStyles("1");

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

        // Auto-Reset / Reload Halaman Antar Putaran
        var reloadCard = document.createElement("label");
        reloadCard.style.display = "flex";
        reloadCard.style.alignItems = "center";
        reloadCard.style.justifyContent = "space-between";
        reloadCard.style.background = "#11111b";
        reloadCard.style.border = "1px solid #313244";
        reloadCard.style.borderRadius = "12px";
        reloadCard.style.padding = "10px 14px";
        reloadCard.style.marginBottom = "14px";
        reloadCard.style.cursor = "pointer";

        var reloadTextCol = document.createElement("div");
        var rTitle = document.createElement("div");
        rTitle.innerText = "Muat Ulang Halaman Tiap Putaran";
        rTitle.style.fontSize = "12px";
        rTitle.style.fontWeight = "600";
        rTitle.style.color = "#cdd6f4";
        reloadTextCol.appendChild(rTitle);

        var rSub = document.createElement("div");
        rSub.innerText = "Reset halaman agar tombol & form segar kembali";
        rSub.style.fontSize = "10px";
        rSub.style.color = "#a6adc8";
        reloadTextCol.appendChild(rSub);
        reloadCard.appendChild(reloadTextCol);

        var reloadCb = document.createElement("input");
        reloadCb.type = "checkbox";
        reloadCb.checked = true;
        reloadCb.style.width = "18px";
        reloadCb.style.height = "18px";
        reloadCb.style.accentColor = "#89dceb";
        reloadCb.style.cursor = "pointer";
        reloadCard.appendChild(reloadCb);
        card.appendChild(reloadCard);

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
        cancelBtn.type = "button";
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
        startBtn.type = "button";
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
            var targetExpr = targetInp.value.trim() || String(activeIdx);
            var isBatch = batchCb.checked;
            var shouldReload = reloadCb.checked;
            var lCount = parseInt(loopInp.value, 10);
            if (isNaN(lCount) || lCount <= 0) lCount = 1;

            modal.remove();
            if (typeof AgyBatchRunner !== "undefined" && AgyBatchRunner.run) {
                AgyBatchRunner.run(events, recipeName, targetExpr, isBatch, null, currentRunMode, lCount, targetUrl, null, shouldReload);
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
        card.style.overflowX = "hidden";

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
        card.style.overflowX = "hidden";

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
        title.innerText = "Detail Langkah";
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
        optClick.innerText = "Klik Elemen (Click)";
        typeSelect.appendChild(optClick);

        var optFill = document.createElement("option");
        optFill.value = "fill";
        optFill.innerText = "Isi Form / Teks (Fill)";
        typeSelect.appendChild(optFill);

        var optWait = document.createElement("option");
        optWait.value = "wait";
        optWait.innerText = "Jeda Waktu / Delay";
        typeSelect.appendChild(optWait);

        var optWallet = document.createElement("option");
        optWallet.value = "next_wallet";
        optWallet.innerText = "Ganti Wallet (Next Wallet)";
        typeSelect.appendChild(optWallet);

        var optReload = document.createElement("option");
        optReload.value = "reload";
        optReload.innerText = "Muat Ulang Halaman (Reload)";
        typeSelect.appendChild(optReload);

        var optWipe = document.createElement("option");
        optWipe.value = "wipe_cache";
        optWipe.innerText = "Bersihkan Cache (Wipe Cache)";
        typeSelect.appendChild(optWipe);

        var optEmail = document.createElement("option");
        optEmail.value = "next_email";
        optEmail.innerText = "Ganti Email (Next Email)";
        typeSelect.appendChild(optEmail);

        var optRef = document.createElement("option");
        optRef.value = "ref";
        optRef.innerText = "Kode Referral (Ref Code)";
        typeSelect.appendChild(optRef);

        var optFp = document.createElement("option");
        optFp.value = "fingerprint";
        optFp.innerText = "Ganti Fingerprint (Rotate Device)";
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
        card.style.overflowX = "hidden";

        var titleRow = document.createElement("div");
        titleRow.style.display = "flex";
        titleRow.style.justifyContent = "space-between";
        titleRow.style.alignItems = "center";
        titleRow.style.marginBottom = "8px";
        titleRow.style.flexShrink = "0";

        var titleBlock = document.createElement("div");
        var title = document.createElement("h3");
        title.innerText = "Editor Langkah Resep";
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
        addBar.style.padding = "6px";
        addBar.style.display = "grid";
        addBar.style.gridTemplateColumns = "repeat(3, 1fr)";
        addBar.style.gap = "5px";
        addBar.style.marginBottom = "10px";
        addBar.style.boxSizing = "border-box";

        function makeAddStepBtn(label, bg, color, onClick) {
            var b = document.createElement("button");
            b.innerText = label;
            b.style.background = bg;
            b.style.color = color || "#FFF";
            b.style.border = "none";
            b.style.borderRadius = "5px";
            b.style.padding = "7px 3px";
            b.style.fontSize = "11px";
            b.style.fontWeight = "600";
            b.style.cursor = "pointer";
            b.style.textAlign = "center";
            b.style.whiteSpace = "nowrap";
            b.style.boxSizing = "border-box";
            b.onclick = onClick;
            return b;
        }

        // Row 1: Core Web Actions
        addBar.appendChild(makeAddStepBtn("+ Klik", "#1565C0", "#FFF", function() {
            showEditSingleStepDialog({ type: "click" }, function(newStep) {
                workingEvents.push(newStep);
                renderStepList();
            });
        }));

        addBar.appendChild(makeAddStepBtn("+ Form", "#E65100", "#FFF", function() {
            showEditSingleStepDialog({ type: "fill", value: "{{WALLET_ADDRESS}}" }, function(newStep) {
                workingEvents.push(newStep);
                renderStepList();
            });
        }));

        addBar.appendChild(makeAddStepBtn("+ Jeda", "#4A148C", "#FFF", function() {
            workingEvents.push({ type: "wait", duration: 2500, timestamp: Date.now() });
            renderStepList();
        }));

        // Row 2: Identity & Data
        addBar.appendChild(makeAddStepBtn("+ Wallet", "#7C4DFF", "#FFF", function() {
            workingEvents.push({ type: "next_wallet", text: "Ganti Wallet", timestamp: Date.now() });
            renderStepList();
        }));

        addBar.appendChild(makeAddStepBtn("+ Email", "#FFA000", "#000", function() {
            var ne = (typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "email@gmail.com";
            workingEvents.push({ type: "next_email", text: "Ganti Email -> " + ne, value: ne, timestamp: Date.now() });
            renderStepList();
        }));

        addBar.appendChild(makeAddStepBtn("+ Ref", "#00E676", "#000", function() {
            var nr = (typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123";
            workingEvents.push({ type: "ref", text: "Ref Code -> " + nr, value: nr, timestamp: Date.now() });
            renderStepList();
        }));

        // Row 3: Maintenance & Security
        addBar.appendChild(makeAddStepBtn("+ Reload", "#00B0FF", "#000", function() {
            workingEvents.push({ type: "reload", text: "Reload Halaman", timestamp: Date.now() });
            renderStepList();
        }));

        addBar.appendChild(makeAddStepBtn("+ Wipe", "#FF9100", "#000", function() {
            workingEvents.push({ type: "wipe_cache", text: "Wipe Cache", timestamp: Date.now() });
            renderStepList();
        }));

        addBar.appendChild(makeAddStepBtn("+ FP", "#E040FB", "#FFF", function() {
            workingEvents.push({ type: "fingerprint", text: "Ganti Fingerprint", timestamp: Date.now() });
            renderStepList();
        }));

        card.appendChild(addBar);

        var stepListContainer = document.createElement("div");
        stepListContainer.style.flex = "1";
        stepListContainer.style.overflowY = "auto";
        stepListContainer.style.maxHeight = "46vh";
        stepListContainer.style.marginBottom = "10px";
        stepListContainer.style.boxSizing = "border-box";
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
                row.style.padding = "6px 8px";
                row.style.marginBottom = "6px";
                row.style.boxSizing = "border-box";

                var infoCol = document.createElement("div");
                infoCol.style.display = "flex";
                infoCol.style.alignItems = "center";
                infoCol.style.overflow = "hidden";
                infoCol.style.textOverflow = "ellipsis";
                infoCol.style.whiteSpace = "nowrap";
                infoCol.style.flex = "1";
                infoCol.style.minWidth = "0";
                infoCol.style.marginRight = "6px";

                var numBadge = "<span style='font-size:10px; font-weight:bold; background:#2A3142; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>#" + (index + 1) + "</span>";

                var typeBadge = "";
                var desc = "";
                if (ev.type === "wait") {
                    typeBadge = "<span style='font-size:10px; background:#7B1FA2; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>JEDA</span>";
                    desc = ((ev.duration || 2500) / 1000) + " detik";
                } else if (ev.type === "next_wallet") {
                    typeBadge = "<span style='font-size:10px; background:#7C4DFF; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>WALLET</span>";
                    desc = ev.text || "Ganti ke wallet berikutnya";
                } else if (ev.type === "reload") {
                    typeBadge = "<span style='font-size:10px; background:#00B0FF; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>RELOAD</span>";
                    desc = ev.text || "Muat ulang halaman & lanjut";
                } else if (ev.type === "wipe_cache") {
                    typeBadge = "<span style='font-size:10px; background:#FF9100; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>WIPE</span>";
                    desc = ev.text || "Wipe Cache & Storage";
                } else if (ev.type === "next_email") {
                    typeBadge = "<span style='font-size:10px; background:#FFA000; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>EMAIL</span>";
                    desc = ev.text || "Ganti ke email berikutnya";
                } else if (ev.type === "ref") {
                    typeBadge = "<span style='font-size:10px; background:#00E676; color:#000; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>REF</span>";
                    desc = ev.text || "Ref Code";
                } else if (ev.type === "fingerprint") {
                    typeBadge = "<span style='font-size:10px; background:#E040FB; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>FP</span>";
                    desc = ev.text || "Ganti Fingerprint";
                } else if (ev.type === "swipe") {
                    typeBadge = "<span style='font-size:10px; background:#9C27B0; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>SWIPE</span>";
                    desc = (ev.direction || "DOWN").toUpperCase() + " (" + Math.round(Math.max(Math.abs(ev.deltaX || 0), Math.abs(ev.deltaY || 0))) + "px)";
                } else if (ev.type === "fill" || ev.type === "input") {
                    typeBadge = "<span style='font-size:10px; background:#F57C00; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>ISI</span>";
                    desc = (ev.value || "") + " ➔ " + (ev.placeholder || ev.name || ev.id || ev.selector || "input");
                } else if (ev.type === "stop_if" || ev.type === "stop_not") {
                    typeBadge = "<span style='font-size:10px; background:#D32F2F; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>STOP-IF</span>";
                    desc = "Berhenti jika " + (ev.type === "stop_not" ? "hilang: " : "ada: ") + "\"" + (ev.text || "teks") + "\"";
                } else if (ev.type === "solve_challenge") {
                    typeBadge = "<span style='font-size:10px; background:#7C4DFF; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>SOLVER</span>";
                    desc = "AI/Regex: " + (ev.challengeSample || ev.challengeSelector || "soal") + " ➔ " + (ev.inputSelector || "input");
                } else if (ev.type === "reward_extractor") {
                    typeBadge = "<span style='font-size:10px; background:#00E676; color:#111; padding:2px 5px; border-radius:4px; margin-right:4px; font-weight:bold; flex-shrink:0;'>REWARD</span>";
                    desc = "Ekstrak: " + (ev.sampleText || ev.amountSelector || "hasil");
                } else {
                    typeBadge = "<span style='font-size:10px; background:#1E88E5; color:#FFF; padding:2px 5px; border-radius:4px; margin-right:4px; flex-shrink:0;'>KLIK</span>";
                    desc = ev.text || ev.ariaLabel || ev.name || ev.id || ev.selector || "elemen";
                }

                infoCol.innerHTML = numBadge + typeBadge + "<span style='font-size:11px; font-weight:500; color:#EEE; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;'>" + desc + "</span>";
                row.appendChild(infoCol);

                var btnCol = document.createElement("div");
                btnCol.style.display = "flex";
                btnCol.style.alignItems = "center";
                btnCol.style.gap = "4px";
                btnCol.style.flexShrink = "0";

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
                editStepBtn.innerText = "✎";
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
        runBtn.innerText = "Test Run";
        runBtn.style.flex = "1";
        runBtn.style.background = "#2E7D32";
        runBtn.style.color = "#FFF";
        runBtn.style.border = "none";
        runBtn.style.borderRadius = "8px";
        runBtn.style.padding = "10px";
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
        saveAllBtn.innerText = "Simpan Resep";
        saveAllBtn.style.flex = "1.2";
        saveAllBtn.style.background = "#1E88E5";
        saveAllBtn.style.color = "#FFF";
        saveAllBtn.style.border = "none";
        saveAllBtn.style.borderRadius = "8px";
        saveAllBtn.style.padding = "10px";
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
        AgyBatchRunner.run(eventsList, recipeLabel || "Resep", "1", false, null, "raw", 1);
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
                        updateAgyHUD("AGY Copilot", "Menyalakan Antigravity di background...", "running");
                        if (window.GhostBridge.wakeUpAgy) window.GhostBridge.wakeUpAgy();
                    } else {
                        updateAgyHUD("AGY Copilot", "Mengirim instruksi ke Antigravity...", "running");
                    }

                    setTimeout(function() {
                        updateAgyHUD("AGY Copilot", "Antigravity sedang memproses instruksi...", "running");
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
                                updateAgyHUD("AGY Copilot", "Antigravity sedang mengeksekusi di browser...", "running");
                            } else {
                                updateAgyHUD("AGY Copilot", "Tugas terkirim ke AGY Copilot", "success");
                            }
                        } catch(_) {
                            updateAgyHUD("AGY Copilot", "Tugas terkirim ke AGY Copilot", "success");
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
            var rawBatch = sessionStorage.getItem("__agy_batch_queue_state__") || localStorage.getItem("__agy_batch_queue_state__");
            var batchState = null;
            if (rawBatch) {
                try {
                    var parsedB = JSON.parse(rawBatch);
                    if (parsedB && Date.now() - (parsedB.timestamp || 0) < 300000) {
                        batchState = parsedB;
                    } else {
                        sessionStorage.removeItem("__agy_batch_queue_state__");
                        localStorage.removeItem("__agy_batch_queue_state__");
                    }
                } catch(_) {}
            }

            var rawStep = sessionStorage.getItem("__agy_active_recipe_run__") || localStorage.getItem("__agy_active_recipe_run__");
            if (rawStep) {
                var st = JSON.parse(rawStep);
                if (Date.now() - st.timestamp > 120000) {
                    sessionStorage.removeItem("__agy_active_recipe_run__");
                    localStorage.removeItem("__agy_active_recipe_run__");
                    st = null;
                }
                if (st && st.eventsList && st.idx < st.eventsList.length) {
                    console.log("[AGY Co-Pilot] Melanjutkan resep setelah reload halaman ke langkah " + (st.idx + 1));
                    setTimeout(function() {
                        if (window.__AGY_ABORT_RUN__) {
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            sessionStorage.removeItem("__agy_batch_queue_state__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_batch_queue_state__");
                            return;
                        }
                        updateAgyHUD(st.runTitle, "Melanjutkan langkah " + (st.idx + 1) + "/" + st.eventsList.length + "...", "running");
                        AgyBatchRunner.executeStepList(st.eventsList, st.curWallet, st.curEmail, st.runTitle, function(ok) {
                            sessionStorage.removeItem("__agy_active_recipe_run__");
                            localStorage.removeItem("__agy_active_recipe_run__");
                            if (batchState) {
                                // Putaran saat ini selesai pasca reload, lanjutkan antrean batch!
                                batchState.currentIdxInQueue = (batchState.currentIdxInQueue || 0) + 1;
                                if (ok) batchState.successCount = (batchState.successCount || 0) + 1;
                                else batchState.failCount = (batchState.failCount || 0) + 1;
                                batchState.timestamp = Date.now();
                                var bStr = JSON.stringify(batchState);
                                sessionStorage.setItem("__agy_batch_queue_state__", bStr);
                                localStorage.setItem("__agy_batch_queue_state__", bStr);
                                if (typeof AgyBatchRunner !== "undefined" && AgyBatchRunner.resumeBatch) {
                                    AgyBatchRunner.resumeBatch(batchState);
                                }
                            } else {
                                updateAgyHUD(st.runTitle, ok ? "Resep Selesai" : "Resep Berhenti", ok ? "success" : "warning");
                            }
                        }, st.idx, st.runMode || "raw");
                    }, 1000);
                    return;
                } else {
                    sessionStorage.removeItem("__agy_active_recipe_run__");
                    localStorage.removeItem("__agy_active_recipe_run__");
                }
            }

            // Jika tidak ada step tertunda tetapi ada batchState tertunda (misalnya setelah reset halaman antar-putaran)
            if (batchState && !window.__AGY_ABORT_RUN__) {
                console.log("[AGY Co-Pilot] Melanjutkan batch queue setelah navigasi/reset ke putaran " + ((batchState.currentIdxInQueue || 0) + 1));
                setTimeout(function() {
                    if (window.__AGY_ABORT_RUN__) return;
                    if (typeof AgyBatchRunner !== "undefined" && AgyBatchRunner.resumeBatch) {
                        AgyBatchRunner.resumeBatch(batchState);
                    }
                }, 900);
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
