(function() {
    if (window.__FUSION_FINGERPRINT_INJECTED__) return;
    window.__FUSION_FINGERPRINT_INJECTED__ = true;

    // 1. Scrub navigator.webdriver at document_start before any <head> script runs
    try {
        Object.defineProperty(navigator, 'webdriver', { get: function() { return undefined; }, configurable: true });
        if (window.Navigator && Navigator.prototype && 'webdriver' in Navigator.prototype) {
            try { delete Navigator.prototype.webdriver; } catch(_) {}
        }
    } catch(_) {}

    // 2. WebRTC STUN / mDNS Leak Protection (RTCPeerConnection & webkitRTCPeerConnection)
    try {
        window.__FUSION_WEBRTC_STEALTH__ = true;
        var wrapRtc = function(OrigRtc) {
            if (!OrigRtc) return OrigRtc;
            var Wrapped = function(config, constraints) {
                if (window.__FUSION_WEBRTC_STEALTH__ && config && config.iceServers) {
                    config = Object.assign({}, config, { iceServers: [] });
                }
                return new OrigRtc(config, constraints);
            };
            Wrapped.prototype = OrigRtc.prototype;
            return Wrapped;
        };
        if (window.RTCPeerConnection) window.RTCPeerConnection = wrapRtc(window.RTCPeerConnection);
        if (window.webkitRTCPeerConnection) window.webkitRTCPeerConnection = wrapRtc(window.webkitRTCPeerConnection);
    } catch(_) {}

    // 3. Chrome Runtime / LoadTimes / CSI Parity
    try {
        if (!window.chrome) window.chrome = {};
        if (!window.chrome.runtime) {
            window.chrome.runtime = {
                OnInstalledReason: { CHROME_UPDATE: "chrome_update", INSTALL: "install", SHARED_MODULE_UPDATE: "shared_module_update", UPDATE: "update" },
                OnRestartRequiredReason: { APP_UPDATE: "app_update", OS_UPDATE: "os_update", PERIODIC: "periodic" },
                PlatformArch: { ARM: "arm", ARM64: "arm64", MIPS: "mips", MIPS64: "mips64", X86_32: "x86-32", X86_64: "x86-64" },
                PlatformNaclArch: { ARM: "arm", MIPS: "mips", MIPS64: "mips64", X86_32: "x86-32", X86_64: "x86-64" },
                PlatformOs: { ANDROID: "android", CROS: "cros", LINUX: "linux", MAC: "mac", OPENBSD: "openbsd", WIN: "win" },
                RequestUpdateCheckStatus: { NO_UPDATE: "no_update", THROTTLED: "throttled", UPDATE_AVAILABLE: "update_available" },
                connect: function() { return { onDisconnect: { addListener: function() {} }, onMessage: { addListener: function() {} }, postMessage: function() {} }; },
                sendMessage: function(extId, msg, opt, cb) { if (typeof opt === 'function') cb = opt; if (cb) cb(); }
            };
        }
        if (!window.chrome.loadTimes) {
            window.chrome.loadTimes = function() {
                var t = (window.performance && window.performance.timing) || {};
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
            };
        }
        if (!window.chrome.csi) {
            window.chrome.csi = function() {
                var t = (window.performance && window.performance.timing) || {};
                var s = t.navigationStart || Date.now();
                var e = t.loadEventEnd || Date.now();
                return { startE: s, onloadT: e, pageT: e - s, tran: 15 };
            };
        }
    } catch(_) {}

    var profileSeed = window.__FUSION_PROFILE__ || window.__FUSION_ACTIVE_WALLET_NAME__ || "default";
    var isDesktop = Boolean(window.__FUSION_IS_DESKTOP__) || (navigator.userAgent && (navigator.userAgent.indexOf("Windows") !== -1 || navigator.userAgent.indexOf("Macintosh") !== -1));
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

    // Hash deterministik per profil/wallet agar sidik jari stabil 100% konsisten tiap reload
    var hash = 0x811c9dc5;
    for (var i = 0; i < profileSeed.length; i++) {
        hash = Math.imul(hash ^ profileSeed.charCodeAt(i), 0x01000193);
    }
    hash = hash >>> 0;
    var noise = ((hash % 10) + 1) * 0.0001;

    // 4. Hardware Concurrency, Device Memory, Platform & UserAgentData Parity
    try {
        Object.defineProperty(navigator, 'hardwareConcurrency', { get: function() { return dev.cores || 8; }, configurable: true });
        Object.defineProperty(navigator, 'deviceMemory', { get: function() { return isDesktop ? 8 : (dev.memory || 8); }, configurable: true });
        Object.defineProperty(navigator, 'platform', {
            get: function() {
                if (isDesktop) return (navigator.userAgent && navigator.userAgent.indexOf('Macintosh') !== -1) ? 'MacIntel' : 'Win32';
                return dev.platform || 'Linux armv8l';
            },
            configurable: true
        });
        if (isDesktop) {
            Object.defineProperty(screen, 'width', { get: function() { return 1920; }, configurable: true });
            Object.defineProperty(screen, 'height', { get: function() { return 1080; }, configurable: true });
            Object.defineProperty(screen, 'availWidth', { get: function() { return 1920; }, configurable: true });
            Object.defineProperty(screen, 'availHeight', { get: function() { return 1040; }, configurable: true });
            Object.defineProperty(window, 'devicePixelRatio', { get: function() { return 1; }, configurable: true });
        }
        if (navigator.userAgentData) {
            var fakeBrands = [
                { brand: "Chromium", version: chromeMajor },
                { brand: "Google Chrome", version: chromeMajor },
                { brand: "Not_A Brand", version: "24" }
            ];
            try { Object.defineProperty(navigator.userAgentData, 'brands', { get: function() { return fakeBrands; }, configurable: true }); } catch(_) {}
            try { Object.defineProperty(navigator.userAgentData, 'mobile', { get: function() { return !isDesktop; }, configurable: true }); } catch(_) {}
            try { Object.defineProperty(navigator.userAgentData, 'platform', { get: function() { return isDesktop ? 'Windows' : 'Android'; }, configurable: true }); } catch(_) {}
            navigator.userAgentData.getHighEntropyValues = function() {
                return Promise.resolve({
                    architecture: isDesktop ? 'x86' : 'arm64',
                    bitness: '64',
                    brands: fakeBrands,
                    fullVersionList: fakeBrands.map(function(b) { return { brand: b.brand, version: chromeVer }; }),
                    mobile: !isDesktop,
                    model: isDesktop ? '' : (dev.model || 'SM-S928B'),
                    platform: isDesktop ? 'Windows' : 'Android',
                    platformVersion: isDesktop ? '10.0.0' : '14.0.0',
                    uaFullVersion: chromeVer
                });
            };
        }
    } catch(e) {}

    // 5. Canvas Noise Faker (Single-pass deterministic sub-pixel shift)
    if (profileSeed !== "default") {
        window.__FUSION_CANVAS_STEALTH__ = true;
        try {
            var originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function() {
                var ctx = this.getContext('2d');
                if (ctx && this.width > 0 && this.height > 0) {
                    try {
                        var pX = hash % this.width;
                        var pY = (hash >>> 8) % this.height;
                        var px = ctx.getImageData(pX, pY, 1, 1);
                        var shift = (hash % 5) + 1;
                        px.data[0] = (px.data[0] + shift) % 256;
                        px.data[3] = Math.max(1, px.data[3]);
                        ctx.putImageData(px, pX, pY);
                    } catch(e) {}
                }
                return originalToDataURL.apply(this, arguments);
            };
        } catch(e) {}

        // 6. AudioContext Jitter Faker
        try {
            if (window.AudioBuffer) {
                var origGetChannelData = AudioBuffer.prototype.getChannelData;
                AudioBuffer.prototype.getChannelData = function() {
                    var channelData = origGetChannelData.apply(this, arguments);
                    if (channelData && channelData.length > 10) {
                        var idx = hash % Math.min(channelData.length, 20);
                        channelData[idx] += noise;
                    }
                    return channelData;
                };
            }
        } catch(e) {}
    }

    // 7. Context-Aware WebGL Vendor & Renderer Spoofing (WebGL 1 & WebGL 2)
    window.__FUSION_WEBGL_STEALTH__ = true;
    function hookWebGL(proto) {
        if (!proto || !proto.getParameter) return;
        var origGetParam = proto.getParameter;
        proto.getParameter = function(param) {
            if (param === 37445) return isDesktop ? 'Google Inc. (NVIDIA)' : (dev.vendor || "Qualcomm");
            if (param === 37446) return isDesktop ? 'ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)' : (dev.renderer || "Adreno (TM) 750");
            return origGetParam.apply(this, arguments);
        };
    }

    try {
        if (window.WebGLRenderingContext) hookWebGL(WebGLRenderingContext.prototype);
        if (window.WebGL2RenderingContext) hookWebGL(WebGL2RenderingContext.prototype);
    } catch(e) {}
})();
