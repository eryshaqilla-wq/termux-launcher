(function() {
    if (window.__FUSION_FINGERPRINT_INJECTED__) return;
    window.__FUSION_FINGERPRINT_INJECTED__ = true;

    var profileSeed = window.__FUSION_PROFILE__ || "default";
    var dev = window.__FUSION_DEVICE__ || {
        vendor: "Qualcomm",
        renderer: "Adreno (TM) 750",
        platform: "Linux armv8l",
        cores: 8,
        memory: 12
    };

    // Hash string sederhana untuk pseudo-random seed deterministik
    var hash = 0;
    for (var i = 0; i < profileSeed.length; i++) {
        hash = ((hash << 5) - hash) + profileSeed.charCodeAt(i);
        hash |= 0;
    }
    var noise = ((Math.abs(hash) % 10) + 1) * 0.0001;

    // 1. Hardware & Navigator Spoofing
    try {
        if (dev.cores) {
            Object.defineProperty(navigator, 'hardwareConcurrency', { get: function() { return dev.cores; }, configurable: true });
        }
        if (dev.memory) {
            Object.defineProperty(navigator, 'deviceMemory', { get: function() { return dev.memory; }, configurable: true });
        }
        if (dev.platform) {
            Object.defineProperty(navigator, 'platform', { get: function() { return dev.platform; }, configurable: true });
        }
        if (dev.userAgent) {
            Object.defineProperty(navigator, 'userAgent', { get: function() { return dev.userAgent; }, configurable: true });
            Object.defineProperty(navigator, 'appVersion', { get: function() { return dev.userAgent.replace(/^Mozilla\//, ''); }, configurable: true });
        }
    } catch(e) {}

    // 2. Canvas Noise Faker (Imperceptible Sub-pixel Shift)
    var originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function() {
        var ctx = this.getContext('2d');
        if (ctx && this.width > 0 && this.height > 0) {
            try {
                var imgData = ctx.getImageData(0, 0, Math.min(this.width, 10), Math.min(this.height, 10));
                for (var j = 0; j < imgData.data.length; j += 4) {
                    imgData.data[j] = (imgData.data[j] + (hash % 3)) & 255;
                }
                ctx.putImageData(imgData, 0, 0);
            } catch(e) {}
        }
        return originalToDataURL.apply(this, arguments);
    };

    // 3. AudioContext Jitter Faker
    if (window.AudioBuffer) {
        var origGetChannelData = AudioBuffer.prototype.getChannelData;
        AudioBuffer.prototype.getChannelData = function() {
            var channelData = origGetChannelData.apply(this, arguments);
            for (var k = 0; k < Math.min(channelData.length, 20); k++) {
                channelData[k] += noise;
            }
            return channelData;
        };
    }

    // 4. WebGL Vendor & Renderer Spoofing (WebGL 1 & WebGL 2)
    function hookWebGL(proto) {
        if (!proto || !proto.getParameter) return;
        var origGetParam = proto.getParameter;
        proto.getParameter = function(param) {
            // UNMASKED_VENDOR_WEBGL
            if (param === 37445) return dev.vendor || "Qualcomm";
            // UNMASKED_RENDERER_WEBGL
            if (param === 37446) return dev.renderer || "Adreno (TM) 750";
            return origGetParam.apply(this, arguments);
        };
    }

    try {
        if (window.WebGLRenderingContext) hookWebGL(WebGLRenderingContext.prototype);
        if (window.WebGL2RenderingContext) hookWebGL(WebGL2RenderingContext.prototype);
    } catch(e) {}
})();
