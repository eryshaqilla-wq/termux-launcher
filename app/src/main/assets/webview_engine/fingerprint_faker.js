(function() {
    if (window.__FUSION_FINGERPRINT_INJECTED__) return;
    window.__FUSION_FINGERPRINT_INJECTED__ = true;

    // Ambil profile seed dari window.__FUSION_PROFILE__ atau default
    var profileSeed = window.__FUSION_PROFILE__ || "default";
    if (profileSeed === "default") return;

    // Hash string sederhana untuk pseudo-random seed deterministik
    var hash = 0;
    for (var i = 0; i < profileSeed.length; i++) {
        hash = ((hash << 5) - hash) + profileSeed.charCodeAt(i);
        hash |= 0;
    }
    var noise = ((Math.abs(hash) % 10) + 1) * 0.0001;

    // 1. Canvas Noise Faker
    var originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function() {
        var ctx = this.getContext('2d');
        if (ctx) {
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

    // 2. AudioContext Jitter Faker
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

    // 3. WebGL Vendor / Renderer Fake (Unmasked)
    var getParameterOrig = WebGLRenderingContext.prototype.getParameter;
    WebGLRenderingContext.prototype.getParameter = function(param) {
        // UNMASKED_VENDOR_WEBGL
        if (param === 37445) return "ARM";
        // UNMASKED_RENDERER_WEBGL
        if (param === 37446) return "Mali-G77 MC9 (" + profileSeed + ")";
        return getParameterOrig.apply(this, arguments);
    };
})();
