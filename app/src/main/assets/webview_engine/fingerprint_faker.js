(function() {
    if (window.__FUSION_FINGERPRINT_INJECTED__) return;

    var profileSeed = window.__FUSION_PROFILE__ || window.__FUSION_ACTIVE_WALLET_NAME__ || "default";
    var isDesktop = Boolean(window.__FUSION_IS_DESKTOP__) || (navigator.userAgent && (navigator.userAgent.indexOf("Windows") !== -1 || navigator.userAgent.indexOf("Macintosh") !== -1));
    var isMac = Boolean(navigator.userAgent && navigator.userAgent.indexOf("Macintosh") !== -1);
    var chromeVer = window.__FUSION_CHROME_VERSION__ || ((navigator.userAgent || "").match(/Chrome\/([0-9.]+)/) || [])[1] || "130.0.6723.102";
    var chromeMajor = chromeVer.split(".")[0] || "130";

    var dev = window.__FUSION_DEVICE__ || {
        name: "Samsung Galaxy S24 Ultra",
        model: "SM-S928B",
        brand: "Samsung",
        vendor: "Qualcomm",
        renderer: "Adreno (TM) 750",
        platform: "Linux armv8l",
        cores: 8,
        memory: 12,
        screenWidth: 1440,
        screenHeight: 3120,
        pixelRatio: 3.0
    };

    var effectiveUa = (dev && dev.userAgent) || navigator.userAgent || "";
    var effectiveAppVer = effectiveUa.indexOf("/") !== -1 ? effectiveUa.substring(effectiveUa.indexOf("/") + 1) : effectiveUa;
    var effectivePlatform = isDesktop ? (isMac ? "MacIntel" : "Win32") : (dev.platform || "Linux armv8l");
    var effectiveMemory = isDesktop ? 8 : (dev.memory || 8);
    var effectiveCores = dev.cores || 8;
    var effectiveVendor = isDesktop ? "Google Inc. (NVIDIA)" : (dev.vendor || "Qualcomm");
    var effectiveRenderer = isDesktop ? "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)" : (dev.renderer || "Adreno (TM) 750");

    // Registry untuk menyamarkan Function.prototype.toString seluruh fungsi/getter yang di-hook agar identik [native code]
    var nativeStringMap = typeof WeakMap === "function" ? new WeakMap() : null;
    function markNative(fn, nameOrOrig) {
        if (!nativeStringMap || typeof fn !== "function") return fn;
        try {
            var str = typeof nameOrOrig === "function"
                ? Function.prototype.toString.call(nameOrOrig)
                : ("function " + (nameOrOrig || fn.name || "") + "() { [native code] }");
            nativeStringMap.set(fn, str);
        } catch(_) {}
        return fn;
    }
    function hookFunctionToString(win) {
        try {
            if (!nativeStringMap || !win.Function || !win.Function.prototype) return;
            var origToStr = win.Function.prototype.toString;
            if (origToStr.__FUSION_TS__) return;
            var wrapped = function() {
                if (nativeStringMap.has(this)) return nativeStringMap.get(this);
                return origToStr.apply(this, arguments);
            };
            wrapped.__FUSION_TS__ = true;
            nativeStringMap.set(wrapped, origToStr.call(origToStr));
            Object.defineProperty(win.Function.prototype, "toString", {
                value: wrapped,
                writable: true,
                configurable: true
            });
        } catch(_) {}
    }

    // Hash deterministik per profil/wallet agar sidik jari stabil 100% konsisten tiap reload, worker & iframe
    var hash = 0x811c9dc5;
    for (var i = 0; i < profileSeed.length; i++) {
        hash = Math.imul(hash ^ profileSeed.charCodeAt(i), 0x01000193);
    }
    hash = hash >>> 0;
    var noise = ((hash % 10) + 1) * 0.0001;

    function buildBrands() {
        return [
            { brand: "Chromium", version: chromeMajor },
            { brand: "Google Chrome", version: chromeMajor },
            { brand: "Not_A Brand", version: "24" }
        ];
    }

    function buildHighEntropyValues() {
        var brands = buildBrands();
        return {
            architecture: isDesktop ? "x86" : "arm",
            bitness: "64",
            brands: brands,
            fullVersionList: brands.map(function(b) {
                return { brand: b.brand, version: b.brand.indexOf("Not") !== -1 ? "24.0.0.0" : chromeVer };
            }),
            mobile: !isDesktop,
            model: isDesktop ? "" : (dev.model || "SM-S928B"),
            platform: isDesktop ? (isMac ? "macOS" : "Windows") : "Android",
            platformVersion: isDesktop ? (isMac ? "14.0.0" : "10.0.0") : "14.0.0",
            uaFullVersion: chromeVer
        };
    }

    function applyDesktopPlugins(win) {
        if (!isDesktop) return;
        try {
            var NavProto = (win.Navigator && win.Navigator.prototype) || win.navigator;
            var PluginArrayProto = win.PluginArray && win.PluginArray.prototype;
            var PluginProto = win.Plugin && win.Plugin.prototype;
            var MimeArrayProto = win.MimeTypeArray && win.MimeTypeArray.prototype;
            var MimeProto = win.MimeType && win.MimeType.prototype;
            if (!PluginArrayProto || !PluginProto) return;

            var defs = [
                { name: "PDF Viewer", filename: "internal-pdf-viewer", description: "Portable Document Format" },
                { name: "Chrome PDF Viewer", filename: "internal-pdf-viewer", description: "Portable Document Format" },
                { name: "Chromium PDF Viewer", filename: "internal-pdf-viewer", description: "Portable Document Format" },
                { name: "Microsoft Edge PDF Viewer", filename: "internal-pdf-viewer", description: "Portable Document Format" },
                { name: "WebKit built-in PDF", filename: "internal-pdf-viewer", description: "Portable Document Format" }
            ];

            var mimeTypesArr = [];
            var pluginObjs = defs.map(function(d) {
                var p = Object.create(PluginProto);
                var m1 = MimeProto ? Object.create(MimeProto) : {};
                var m2 = MimeProto ? Object.create(MimeProto) : {};
                Object.defineProperties(m1, {
                    type: { value: "application/pdf", enumerable: true },
                    suffixes: { value: "pdf", enumerable: true },
                    description: { value: "Portable Document Format", enumerable: true },
                    enabledPlugin: { value: p, enumerable: true }
                });
                Object.defineProperties(m2, {
                    type: { value: "text/pdf", enumerable: true },
                    suffixes: { value: "pdf", enumerable: true },
                    description: { value: "Portable Document Format", enumerable: true },
                    enabledPlugin: { value: p, enumerable: true }
                });
                mimeTypesArr.push(m1, m2);
                Object.defineProperties(p, {
                    name: { value: d.name, enumerable: true },
                    filename: { value: d.filename, enumerable: true },
                    description: { value: d.description, enumerable: true },
                    length: { value: 2, enumerable: true },
                    0: { value: m1, enumerable: true },
                    1: { value: m2, enumerable: true }
                });
                p.item = markNative(function(idx) { return this[idx] || null; }, "item");
                p.namedItem = markNative(function(n) { return n === "application/pdf" ? m1 : (n === "text/pdf" ? m2 : null); }, "namedItem");
                return p;
            });

            var fakePlugins = Object.create(PluginArrayProto);
            pluginObjs.forEach(function(p, idx) {
                Object.defineProperty(fakePlugins, idx, { value: p, enumerable: true });
                Object.defineProperty(fakePlugins, p.name, { value: p, enumerable: false });
            });
            Object.defineProperty(fakePlugins, "length", { value: pluginObjs.length, enumerable: true });
            fakePlugins.item = markNative(function(idx) { return this[idx] || null; }, "item");
            fakePlugins.namedItem = markNative(function(n) { return this[n] || null; }, "namedItem");
            fakePlugins.refresh = markNative(function() {}, "refresh");

            try { delete win.navigator.plugins; } catch(_) {}
            Object.defineProperty(NavProto, "plugins", { get: markNative(function() { return fakePlugins; }, "get plugins"), configurable: true });

            if (MimeArrayProto) {
                var fakeMimes = Object.create(MimeArrayProto);
                var m0 = mimeTypesArr[0];
                var m1 = mimeTypesArr[1];
                Object.defineProperty(fakeMimes, 0, { value: m0, enumerable: true });
                Object.defineProperty(fakeMimes, 1, { value: m1, enumerable: true });
                Object.defineProperty(fakeMimes, "application/pdf", { value: m0, enumerable: false });
                Object.defineProperty(fakeMimes, "text/pdf", { value: m1, enumerable: false });
                Object.defineProperty(fakeMimes, "length", { value: 2, enumerable: true });
                fakeMimes.item = markNative(function(idx) { return this[idx] || null; }, "item");
                fakeMimes.namedItem = markNative(function(n) { return this[n] || null; }, "namedItem");
                try { delete win.navigator.mimeTypes; } catch(_) {}
                Object.defineProperty(NavProto, "mimeTypes", { get: markNative(function() { return fakeMimes; }, "get mimeTypes"), configurable: true });
            }
        } catch(_) {}
    }

    function hookWebGLOnScope(scope) {
        if (!scope) return;
        scope.__FUSION_WEBGL_STEALTH__ = true;
        var hookWebGL = function(proto) {
            if (!proto || !proto.getParameter) return;
            var origGetParam = proto.getParameter;
            proto.getParameter = markNative(function(param) {
                if (param === 37445) return effectiveVendor;
                if (param === 37446) return effectiveRenderer;
                return origGetParam.apply(this, arguments);
            }, origGetParam);
        };
        try {
            if (scope.WebGLRenderingContext) hookWebGL(scope.WebGLRenderingContext.prototype);
            if (scope.WebGL2RenderingContext) hookWebGL(scope.WebGL2RenderingContext.prototype);
        } catch(_) {}
    }

    function hookWebRtcOnWindow(win) {
        try {
            win.__FUSION_WEBRTC_STEALTH__ = true;
            var isPrivateOrHostCandidate = function(candStr) {
                if (!candStr) return false;
                var s = String(candStr);
                if (s.indexOf("typ host") !== -1) return true;
                return /(^|[\s:])(10\.\d+\.\d+\.\d+|192\.168\.\d+\.\d+|172\.(1[6-9]|2\d|3[01])\.\d+\.\d+|169\.254\.\d+\.\d+|127\.\d+\.\d+\.\d+|fe80::|fd[0-9a-f]{2}:)/i.test(s);
            };
            var scrubSdp = function(desc) {
                if (!desc || !desc.sdp) return desc;
                var cleaned = String(desc.sdp).split(/\r?\n/).filter(function(line) {
                    if (line.indexOf("a=candidate:") === 0 && isPrivateOrHostCandidate(line)) return false;
                    return true;
                }).join("\r\n");
                try { return { type: desc.type, sdp: cleaned }; } catch(_) { return desc; }
            };

            var wrapRtc = function(OrigRtc) {
                if (!OrigRtc) return OrigRtc;
                var Wrapped = markNative(function(config, constraints) {
                    var cfg = config ? Object.assign({}, config) : {};
                    if (win.__FUSION_WEBRTC_STEALTH__) {
                        cfg.iceServers = [];
                        cfg.iceTransportPolicy = "relay";
                    }
                    var pc = new OrigRtc(cfg, constraints);
                    if (!win.__FUSION_WEBRTC_STEALTH__) return pc;

                    var origAddEL = pc.addEventListener.bind(pc);
                    pc.addEventListener = markNative(function(type, listener, opts) {
                        if (type === "icecandidate" && typeof listener === "function") {
                            var wrappedListener = function(ev) {
                                if (ev && ev.candidate && isPrivateOrHostCandidate(ev.candidate.candidate)) {
                                    try {
                                        var emptyEv = typeof win.RTCPeerConnectionIceEvent === "function"
                                            ? new win.RTCPeerConnectionIceEvent("icecandidate", { candidate: null })
                                            : ev;
                                        listener.call(this, emptyEv);
                                    } catch(_) {}
                                    return;
                                }
                                return listener.apply(this, arguments);
                            };
                            return origAddEL(type, wrappedListener, opts);
                        }
                        return origAddEL(type, listener, opts);
                    }, pc.addEventListener);

                    var userOnIce = null;
                    var iceListenerAttached = false;
                    try {
                        Object.defineProperty(pc, "onicecandidate", {
                            get: markNative(function() { return userOnIce; }, "get onicecandidate"),
                            set: markNative(function(fn) {
                                userOnIce = fn;
                                if (!iceListenerAttached) {
                                    iceListenerAttached = true;
                                    pc.addEventListener("icecandidate", function(ev) {
                                        if (typeof userOnIce === "function") userOnIce.call(pc, ev);
                                    });
                                }
                            }, "set onicecandidate"),
                            configurable: true
                        });
                    } catch(_) {}

                    var origCreateOffer = pc.createOffer.bind(pc);
                    pc.createOffer = markNative(function() {
                        return origCreateOffer.apply(pc, arguments).then(scrubSdp);
                    }, pc.createOffer);
                    var origCreateAnswer = pc.createAnswer.bind(pc);
                    pc.createAnswer = markNative(function() {
                        return origCreateAnswer.apply(pc, arguments).then(scrubSdp);
                    }, pc.createAnswer);
                    return pc;
                }, OrigRtc);
                Wrapped.prototype = OrigRtc.prototype;
                return Wrapped;
            };
            if (win.RTCPeerConnection) win.RTCPeerConnection = wrapRtc(win.RTCPeerConnection);
            if (win.webkitRTCPeerConnection) win.webkitRTCPeerConnection = wrapRtc(win.webkitRTCPeerConnection);
        } catch(_) {}
    }

    function createSpoofedUAData(heValues, baseUAData) {
        var brands = heValues.brands;
        var obj = {};
        try {
            if (typeof NavigatorUAData !== "undefined" && NavigatorUAData.prototype) {
                obj = Object.create(NavigatorUAData.prototype);
            } else if (baseUAData && Object.getPrototypeOf(baseUAData)) {
                obj = Object.create(Object.getPrototypeOf(baseUAData));
            }
        } catch(_) {}
        Object.defineProperties(obj, {
            brands: { get: markNative(function() { return brands; }, "get brands"), enumerable: true, configurable: true },
            mobile: { get: markNative(function() { return heValues.mobile; }, "get mobile"), enumerable: true, configurable: true },
            platform: { get: markNative(function() { return heValues.platform; }, "get platform"), enumerable: true, configurable: true },
            getHighEntropyValues: {
                value: markNative(function(hints) {
                    if (!Array.isArray(hints) || hints.length === 0) return Promise.resolve(heValues);
                    var out = { brands: brands, mobile: heValues.mobile, platform: heValues.platform };
                    hints.forEach(function(h) { if (h in heValues) out[h] = heValues[h]; });
                    return Promise.resolve(out);
                }, "getHighEntropyValues"),
                enumerable: true,
                configurable: true
            },
            toJSON: {
                value: markNative(function() { return { brands: brands, mobile: heValues.mobile, platform: heValues.platform }; }, "toJSON"),
                enumerable: true,
                configurable: true
            }
        });
        return obj;
    }

    function buildWorkerStealthHeader() {
        var cfg = {
            ua: effectiveUa,
            appVer: effectiveAppVer,
            platform: effectivePlatform,
            cores: effectiveCores,
            memory: effectiveMemory,
            vendor: effectiveVendor,
            renderer: effectiveRenderer,
            isDesktop: isDesktop,
            spoofWebgl: (profileSeed !== "default" || isDesktop),
            he: buildHighEntropyValues()
        };
        return "(function(){try{" +
            "var C=" + JSON.stringify(cfg) + ";" +
            "var nm=typeof WeakMap==='function'?new WeakMap():null;" +
            "var mn=function(f,n){if(!nm||typeof f!=='function')return f;try{nm.set(f,typeof n==='function'?Function.prototype.toString.call(n):('function '+(n||f.name||'')+'() { [native code] }'));}catch(_){}return f;};" +
            "try{if(nm&&self.Function&&self.Function.prototype&&!self.Function.prototype.toString.__FUSION_TS__){var ot=self.Function.prototype.toString;var wt=function(){if(nm.has(this))return nm.get(this);return ot.apply(this,arguments);};wt.__FUSION_TS__=true;nm.set(wt,ot.call(ot));Object.defineProperty(self.Function.prototype,'toString',{value:wt,writable:true,configurable:true});}}catch(_){}" +
            "var np=(self.WorkerNavigator&&self.WorkerNavigator.prototype)||self.navigator;" +
            "if(np&&self.navigator){" +
            "try{delete self.navigator.webdriver;}catch(_){}" +
            "var def=function(k,v){try{Object.defineProperty(np,k,{get:mn(function(){return v;},'get '+k),configurable:true});}catch(_){}};" +
            "def('webdriver',false);" +
            "if(C.ua){def('userAgent',C.ua);def('appVersion',C.appVer);}" +
            "def('platform',C.platform);" +
            "def('hardwareConcurrency',C.cores);" +
            "def('deviceMemory',C.memory);" +
            "if(self.navigator.userAgentData){" +
            "var fakeUAD={};try{if(typeof NavigatorUAData!=='undefined'&&NavigatorUAData.prototype)fakeUAD=Object.create(NavigatorUAData.prototype);}catch(_){}" +
            "Object.defineProperties(fakeUAD,{brands:{get:mn(function(){return C.he.brands;},'get brands'),enumerable:true,configurable:true},mobile:{get:mn(function(){return !C.isDesktop;},'get mobile'),enumerable:true,configurable:true},platform:{get:mn(function(){return C.he.platform;},'get platform'),enumerable:true,configurable:true},getHighEntropyValues:{value:mn(function(h){if(!Array.isArray(h)||!h.length)return Promise.resolve(C.he);var o={brands:C.he.brands,mobile:!C.isDesktop,platform:C.he.platform};h.forEach(function(k){if(k in C.he)o[k]=C.he[k];});return Promise.resolve(o);},'getHighEntropyValues'),enumerable:true,configurable:true},toJSON:{value:mn(function(){return{brands:C.he.brands,mobile:!C.isDesktop,platform:C.he.platform};},'toJSON'),enumerable:true,configurable:true}});" +
            "def('userAgentData',fakeUAD);" +
            "}}" +
            "if(C.spoofWebgl){var hw=function(p){if(!p||!p.getParameter)return;var o=p.getParameter;p.getParameter=mn(function(k){if(k===37445)return C.vendor;if(k===37446)return C.renderer;return o.apply(this,arguments);},o);};" +
            "if(self.WebGLRenderingContext)hw(self.WebGLRenderingContext.prototype);" +
            "if(self.WebGL2RenderingContext)hw(self.WebGL2RenderingContext.prototype);}" +
            "}catch(_){}})();";
    }

    function hookWorkersAndServiceWorker(win) {
        try {
            var workerHeader = buildWorkerStealthHeader();
            var blobRegistry = new Map();
            if (win.URL && typeof win.URL.createObjectURL === "function" && !win.URL.createObjectURL.__FUSION_BLOB_TRACK__) {
                var origCreateObjUrl = win.URL.createObjectURL.bind(win.URL);
                var trackedCreateObjUrl = markNative(function(obj) {
                    var res = origCreateObjUrl(obj);
                    try {
                        if (obj && typeof win.Blob === "function" && obj instanceof win.Blob) {
                            if (blobRegistry.size > 64) blobRegistry.clear();
                            blobRegistry.set(res, obj);
                        }
                    } catch(_) {}
                    return res;
                }, win.URL.createObjectURL);
                trackedCreateObjUrl.__FUSION_BLOB_TRACK__ = true;
                win.URL.createObjectURL = trackedCreateObjUrl;
            }

            var wrapWorkerCtor = function(OrigCtor) {
                if (!OrigCtor || OrigCtor.__FUSION_WRAPPED__) return OrigCtor;
                var WrappedWorker = markNative(function(scriptUrl, options) {
                    try {
                        var urlStr = String(scriptUrl);
                        var isModule = options && options.type === "module";
                        var recordedBlob = blobRegistry.get(urlStr);
                        if (recordedBlob && !isModule) {
                            // Gabungkan header stealth langsung ke Blob tunggal tanpa importScripts agar aman 100% di strict CSP & revokeObjectURL
                            var merged = new win.Blob([workerHeader + "\n", recordedBlob], {
                                type: recordedBlob.type || "application/javascript"
                            });
                            var mergedUrl = win.URL.createObjectURL(merged);
                            var inst = new OrigCtor(mergedUrl, options);
                            setTimeout(function() { try { win.URL.revokeObjectURL(mergedUrl); } catch(_) {} }, 5000);
                            return inst;
                        }
                    } catch(_) {}
                    return new OrigCtor(scriptUrl, options);
                }, OrigCtor);
                WrappedWorker.prototype = OrigCtor.prototype;
                WrappedWorker.__FUSION_WRAPPED__ = true;
                return WrappedWorker;
            };
            if (win.Worker) win.Worker = wrapWorkerCtor(win.Worker);
            if (win.SharedWorker) win.SharedWorker = wrapWorkerCtor(win.SharedWorker);
        } catch(_) {}
    }

    function applyStealthToWindow(win) {
        if (!win) return;
        try {
            if (win.__FUSION_FINGERPRINT_INJECTED__) return;
            win.__FUSION_FINGERPRINT_INJECTED__ = true;
        } catch(_) {
            return;
        }
        hookFunctionToString(win);

        // 1. Scrub navigator.webdriver on prototype only (zero own-property trace for _.has(navigator, "webdriver"))
        try {
            try { delete win.navigator.webdriver; } catch(_) {}
            if (win.Navigator && win.Navigator.prototype) {
                Object.defineProperty(win.Navigator.prototype, 'webdriver', {
                    get: markNative(function() { return false; }, 'get webdriver'),
                    configurable: true
                });
            }
        } catch(_) {}

        // 2. WebRTC STUN + LAN/Host IP Leak Protection
        hookWebRtcOnWindow(win);

        // 3. Chrome LoadTimes / CSI Parity (tanpa membocorkan chrome.runtime extension stub di mobile)
        try {
            if (!win.chrome) win.chrome = {};
            if (isDesktop && !win.chrome.runtime) {
                win.chrome.runtime = {
                    OnInstalledReason: { CHROME_UPDATE: "chrome_update", INSTALL: "install", SHARED_MODULE_UPDATE: "shared_module_update", UPDATE: "update" },
                    OnRestartRequiredReason: { APP_UPDATE: "app_update", OS_UPDATE: "os_update", PERIODIC: "periodic" },
                    PlatformArch: { ARM: "arm", ARM64: "arm64", MIPS: "mips", MIPS64: "mips64", X86_32: "x86-32", X86_64: "x86-64" },
                    PlatformNaclArch: { ARM: "arm", MIPS: "mips", MIPS64: "mips64", X86_32: "x86-32", X86_64: "x86-64" },
                    PlatformOs: { ANDROID: "android", CROS: "cros", LINUX: "linux", MAC: "mac", OPENBSD: "openbsd", WIN: "win" },
                    RequestUpdateCheckStatus: { NO_UPDATE: "no_update", THROTTLED: "throttled", UPDATE_AVAILABLE: "update_available" },
                    connect: markNative(function() { return { onDisconnect: { addListener: function() {} }, onMessage: { addListener: function() {} }, postMessage: function() {} }; }, "connect"),
                    sendMessage: markNative(function(extId, msg, opt, cb) { if (typeof opt === 'function') cb = opt; if (cb) cb(); }, "sendMessage")
                };
            }
            if (!win.chrome.loadTimes) {
                win.chrome.loadTimes = markNative(function() {
                    var t = (win.performance && win.performance.timing) || {};
                    var start = (t.navigationStart || Date.now()) / 1000;
                    return {
                        requestTime: start,
                        startLoadTime: start,
                        commitLoadTime: (t.responseStart || Date.now()) / 1000,
                        finishDocumentLoadTime: (t.domContentLoadedEventEnd || Date.now()) / 1000,
                        finishLoadTime: (t.loadEventEnd || Date.now()) / 1000,
                        firstPaintTime: (t.domInteractive || Date.now()) / 1000,
                        navigationType: "Other"
                    };
                }, "loadTimes");
            }
            if (!win.chrome.csi) {
                win.chrome.csi = markNative(function() {
                    var t = (win.performance && win.performance.timing) || {};
                    var s = t.navigationStart || Date.now();
                    var e = t.loadEventEnd || Date.now();
                    return { startE: s, onloadT: e, pageT: e - s, tran: 15 };
                }, "csi");
            }
        } catch(_) {}

        // 4. Hardware Concurrency, Device Memory, Platform, Touch, Window Dimensions, Plugins & UserAgentData Parity
        try {
            var navProto = (win.Navigator && win.Navigator.prototype) || win.navigator;
            var defNav = function(prop, getter) {
                try { delete win.navigator[prop]; } catch(_) {}
                try { Object.defineProperty(navProto, prop, { get: markNative(getter, "get " + prop), configurable: true }); } catch(_) {}
            };
            defNav('hardwareConcurrency', function() { return effectiveCores; });
            defNav('deviceMemory', function() { return effectiveMemory; });
            defNav('platform', function() { return effectivePlatform; });
            if (isDesktop) {
                defNav('maxTouchPoints', function() { return 0; });
            }
            applyDesktopPlugins(win);

            // Headless outerWidth/outerHeight fix (native WebView headless returns 0, triggering 33% headless)
            if (!win.outerWidth || win.outerWidth === 0) {
                Object.defineProperty(win, 'outerWidth', {
                    get: markNative(function() { return win.innerWidth || (isDesktop ? 1920 : (dev.screenWidth || 480)); }, "get outerWidth"),
                    configurable: true
                });
            }
            if (!win.outerHeight || win.outerHeight === 0) {
                Object.defineProperty(win, 'outerHeight', {
                    get: markNative(function() {
                        var h = win.innerHeight || (isDesktop ? 1000 : (dev.screenHeight || 1067));
                        return isDesktop ? (h + 85) : h;
                    }, "get outerHeight"),
                    configurable: true
                });
            }

            if (isDesktop && win.screen) {
                Object.defineProperty(win.screen, 'width', { get: markNative(function() { return 1920; }, "get width"), configurable: true });
                Object.defineProperty(win.screen, 'height', { get: markNative(function() { return 1080; }, "get height"), configurable: true });
                Object.defineProperty(win.screen, 'availWidth', { get: markNative(function() { return 1920; }, "get availWidth"), configurable: true });
                Object.defineProperty(win.screen, 'availHeight', { get: markNative(function() { return 1040; }, "get availHeight"), configurable: true });
                Object.defineProperty(win, 'devicePixelRatio', { get: markNative(function() { return 1; }, "get devicePixelRatio"), configurable: true });
                if (win.screen.orientation) {
                    try { Object.defineProperty(win.screen.orientation, 'type', { get: markNative(function() { return 'landscape-primary'; }, "get type"), configurable: true }); } catch(_) {}
                    try { Object.defineProperty(win.screen.orientation, 'angle', { get: markNative(function() { return 0; }, "get angle"), configurable: true }); } catch(_) {}
                }
            }
            if (win.navigator.userAgentData) {
                var fakeUAD = createSpoofedUAData(buildHighEntropyValues(), win.navigator.userAgentData);
                defNav('userAgentData', function() { return fakeUAD; });
            }
        } catch(e) {}

        // 5. Canvas Noise Faker (Deterministic, non-mutating scratch pass, consistent top & iframe)
        if (profileSeed !== "default" && win.HTMLCanvasElement) {
            win.__FUSION_CANVAS_STEALTH__ = true;
            try {
                var originalToDataURL = win.HTMLCanvasElement.prototype.toDataURL;
                var originalToBlob = win.HTMLCanvasElement.prototype.toBlob;
                var withCanvasNoise = function(canvas, origFn, args) {
                    var ctx = canvas.getContext('2d');
                    if (ctx && canvas.width > 0 && canvas.height > 0) {
                        try {
                            var pX = hash % canvas.width;
                            var pY = (hash >>> 8) % canvas.height;
                            var origPx = ctx.getImageData(pX, pY, 1, 1);
                            var modPx = ctx.getImageData(pX, pY, 1, 1);
                            var shift = (hash % 5) + 1;
                            modPx.data[0] = (modPx.data[0] + shift) % 256;
                            modPx.data[3] = Math.max(1, modPx.data[3]);
                            ctx.putImageData(modPx, pX, pY);
                            try {
                                return origFn.apply(canvas, args);
                            } finally {
                                ctx.putImageData(origPx, pX, pY);
                            }
                        } catch(e) {}
                    }
                    return origFn.apply(canvas, args);
                };
                win.HTMLCanvasElement.prototype.toDataURL = markNative(function() {
                    return withCanvasNoise(this, originalToDataURL, arguments);
                }, originalToDataURL);
                if (originalToBlob) {
                    win.HTMLCanvasElement.prototype.toBlob = markNative(function() {
                        return withCanvasNoise(this, originalToBlob, arguments);
                    }, originalToBlob);
                }
            } catch(e) {}

            // 6. AudioContext Jitter Faker
            try {
                if (win.AudioBuffer) {
                    var origGetChannelData = win.AudioBuffer.prototype.getChannelData;
                    win.AudioBuffer.prototype.getChannelData = markNative(function() {
                        var channelData = origGetChannelData.apply(this, arguments);
                        if (channelData && channelData.length > 10) {
                            var idx = hash % Math.min(channelData.length, 20);
                            channelData[idx] += noise;
                        }
                        return channelData;
                    }, origGetChannelData);
                }
            } catch(e) {}
        }

        // 7. Context-Aware WebGL Vendor & Renderer Spoofing (WebGL 1 & WebGL 2)
        // Untuk profil default mobile, pertahankan GPU asli perangkat agar 100% selaras dengan kapabilitas/ekstensi WebGL
        if (profileSeed !== "default" || isDesktop) {
            hookWebGLOnScope(win);
        }

        // 8. Worker & ServiceWorker Stealth Propagation (prevents native ; wv / Mali GPU leak across threads)
        hookWorkersAndServiceWorker(win);

        // 9. Same-Origin Iframe Stealth Propagation (contentWindow, contentDocument, DocumentFragment, innerHTML & frames sweep)
        try {
            var sweepFrames = function() {
                try {
                    var flen = win.frames ? win.frames.length : 0;
                    for (var fi = 0; fi < flen; fi++) {
                        try {
                            var fw = win.frames[fi];
                            if (fw && !fw.__FUSION_FINGERPRINT_INJECTED__ && fw.navigator) {
                                applyStealthToWindow(fw);
                            }
                        } catch(_) {}
                    }
                } catch(_) {}
            };
            var patchIframeNode = function(node, container) {
                if (node && node.nodeType === 1) {
                    if (node.tagName === 'IFRAME') {
                        try {
                            var cw = origCwGetter ? origCwGetter.call(node) : node.contentWindow;
                            if (cw && cw.navigator) applyStealthToWindow(cw);
                        } catch(_) {}
                    } else if (node.querySelectorAll) {
                        try {
                            var frames = node.querySelectorAll('iframe');
                            for (var k = 0; k < frames.length; k++) patchIframeNode(frames[k], null);
                        } catch(_) {}
                    }
                } else if (node && node.nodeType === 11) {
                    // DocumentFragment: children move into container after DOM attach
                    if (container && container.querySelectorAll) {
                        try {
                            var cframes = container.querySelectorAll('iframe');
                            for (var c = 0; c < cframes.length; c++) patchIframeNode(cframes[c], null);
                        } catch(_) {}
                    }
                    sweepFrames();
                }
            };
            var origCwGetter = null;
            if (win.HTMLIFrameElement && win.HTMLIFrameElement.prototype) {
                var iframeProto = win.HTMLIFrameElement.prototype;
                var origCwDesc = Object.getOwnPropertyDescriptor(iframeProto, 'contentWindow');
                if (origCwDesc && origCwDesc.get) {
                    origCwGetter = origCwDesc.get;
                    Object.defineProperty(iframeProto, 'contentWindow', {
                        get: markNative(function() {
                            var cw = origCwDesc.get.call(this);
                            if (cw) {
                                try { if (cw.navigator) applyStealthToWindow(cw); } catch(_) {}
                            }
                            return cw;
                        }, origCwDesc.get),
                        configurable: true
                    });
                }
                var origCdDesc = Object.getOwnPropertyDescriptor(iframeProto, 'contentDocument');
                if (origCdDesc && origCdDesc.get) {
                    Object.defineProperty(iframeProto, 'contentDocument', {
                        get: markNative(function() {
                            var cd = origCdDesc.get.call(this);
                            if (cd) {
                                try {
                                    var cw = cd.defaultView;
                                    if (cw && cw.navigator) applyStealthToWindow(cw);
                                } catch(_) {}
                            }
                            return cd;
                        }, origCdDesc.get),
                        configurable: true
                    });
                }
            }
            if (win.Node && win.Node.prototype) {
                ['appendChild', 'insertBefore', 'replaceChild'].forEach(function(m) {
                    var orig = win.Node.prototype[m];
                    if (typeof orig === 'function') {
                        win.Node.prototype[m] = markNative(function(newNode) {
                            var res = orig.apply(this, arguments);
                            patchIframeNode(newNode, this);
                            return res;
                        }, orig);
                    }
                });
            }
            if (win.Element && win.Element.prototype) {
                ['append', 'prepend', 'after', 'before', 'replaceWith'].forEach(function(m) {
                    var orig = win.Element.prototype[m];
                    if (typeof orig === 'function') {
                        win.Element.prototype[m] = markNative(function() {
                            var res = orig.apply(this, arguments);
                            sweepFrames();
                            return res;
                        }, orig);
                    }
                });
                var innerDesc = Object.getOwnPropertyDescriptor(win.Element.prototype, 'innerHTML');
                if (innerDesc && innerDesc.set) {
                    Object.defineProperty(win.Element.prototype, 'innerHTML', {
                        get: innerDesc.get,
                        set: markNative(function(val) {
                            innerDesc.set.call(this, val);
                            if (typeof val === 'string' && val.toLowerCase().indexOf('iframe') !== -1) {
                                sweepFrames();
                            }
                        }, innerDesc.set),
                        configurable: true
                    });
                }
            }
        } catch(_) {}
    }

    applyStealthToWindow(window);
})();
