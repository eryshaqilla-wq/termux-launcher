// ==UserScript==
// @name         Fusion Agent Playwright Engine
// @namespace    https://eso.ai/
// @version      1.0.0
// @description  Semantic ARIA Accessibility Snapshot & Agentic Controller for Termux WebView (Playwright MCP)
// @match        *://*/*
// @match        file://*/*
// @run-at       document-end
// @grant        none
// ==/UserScript==

(() => {
    'use strict';

    const FUSION_ENGINE_VERSION = "2.4.0";
    if (window.__FUSION_AGENT_ENGINE__ && window.__FUSION_AGENT_ENGINE__.version === FUSION_ENGINE_VERSION) return;

    // --- 1. WebRTC STUN Leak Protection (Stealth WebRTC) ---
    window.__FUSION_WEBRTC_STEALTH__ = true;
    const wrapRtcClass = (OrigPeerConnection) => {
        if (!OrigPeerConnection) return OrigPeerConnection;
        const Wrapped = function(config, constraints) {
            if (window.__FUSION_WEBRTC_STEALTH__ && config && config.iceServers) {
                config = Object.assign({}, config, { iceServers: [] });
            }
            return new OrigPeerConnection(config, constraints);
        };
        Wrapped.prototype = OrigPeerConnection.prototype;
        return Wrapped;
    };
    if (window.RTCPeerConnection) {
        window.RTCPeerConnection = wrapRtcClass(window.RTCPeerConnection);
    }
    if (window.webkitRTCPeerConnection) {
        window.webkitRTCPeerConnection = wrapRtcClass(window.webkitRTCPeerConnection);
    }

    // --- 1B. NINJA STEALTH & ANTI-SYBIL SUITE (1.000 Unique Fingerprints & Parity) ---
    (() => {
        // Deterministic Seed per Wallet
        function getWalletSeed() {
            const raw = window.__FUSION_ACTIVE_WALLET_NAME__ || window.__FUSION_WALLET_NAME__ || window.__FUSION_ACTIVE_WALLET_ADDRESS__ || window.__FUSION_WALLET_ADDRESS__ || window.__FUSION_PROFILE__ || (window.location && window.location.hostname) || 'default';
            let h = 0x811c9dc5;
            for (let i = 0; i < raw.length; i++) {
                h = Math.imul(h ^ raw.charCodeAt(i), 0x01000193);
            }
            return (h >>> 0);
        }

        // --- DETERMINISTIC DEVICE PROFILE MATRIX (Seeded per Wallet) ---
        const DEVICE_PROFILES = [
            {
                name: "Samsung Galaxy S24 Ultra",
                brand: "Samsung",
                model: "SM-S928B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S928B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 384,
                screenHeight: 832,
                dpr: 3,
                memory: 12
            },
            {
                name: "Samsung Galaxy S24+",
                brand: "Samsung",
                model: "SM-S926U",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S926U Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 384,
                screenHeight: 832,
                dpr: 3,
                memory: 12
            },
            {
                name: "Samsung Galaxy S24",
                brand: "Samsung",
                model: "SM-S921U",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S921U Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 360,
                screenHeight: 780,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy S23 Ultra",
                brand: "Samsung",
                model: "SM-S918B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S918B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 384,
                screenHeight: 824,
                dpr: 3,
                memory: 12
            },
            {
                name: "Samsung Galaxy S23+",
                brand: "Samsung",
                model: "SM-S916B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S916B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 384,
                screenHeight: 824,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy S23",
                brand: "Samsung",
                model: "SM-S911B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S911B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 360,
                screenHeight: 780,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy S23 FE",
                brand: "Samsung",
                model: "SM-S711B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S711B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 384,
                screenHeight: 854,
                dpr: 2.8,
                memory: 8
            },
            {
                name: "Samsung Galaxy S22 Ultra",
                brand: "Samsung",
                model: "SM-S908B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S908B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 384,
                screenHeight: 824,
                dpr: 3,
                memory: 12
            },
            {
                name: "Samsung Galaxy S22+",
                brand: "Samsung",
                model: "SM-S906B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S906B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 384,
                screenHeight: 824,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy S22",
                brand: "Samsung",
                model: "SM-S901B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-S901B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 360,
                screenHeight: 780,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy S21 Ultra",
                brand: "Samsung",
                model: "SM-G998B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-G998B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G78 MP14",
                screenWidth: 384,
                screenHeight: 854,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Samsung Galaxy S20 FE",
                brand: "Samsung",
                model: "SM-G781B",
                ua: "Mozilla/5.0 (Linux; Android 13; SM-G781B Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 650",
                screenWidth: 360,
                screenHeight: 800,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy Z Fold 5",
                brand: "Samsung",
                model: "SM-F946B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-F946B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 373,
                screenHeight: 904,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Samsung Galaxy Z Flip 5",
                brand: "Samsung",
                model: "SM-F731B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-F731B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 360,
                screenHeight: 884,
                dpr: 3,
                memory: 8
            },
            {
                name: "Samsung Galaxy Z Fold 6",
                brand: "Samsung",
                model: "SM-F956B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-F956B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 412,
                screenHeight: 968,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Samsung Galaxy Z Flip 6",
                brand: "Samsung",
                model: "SM-F741B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-F741B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 360,
                screenHeight: 884,
                dpr: 3,
                memory: 12
            },
            {
                name: "Samsung Galaxy A55 5G",
                brand: "Samsung",
                model: "SM-A556B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-A556B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68",
                screenWidth: 384,
                screenHeight: 854,
                dpr: 2.8125,
                memory: 8
            },
            {
                name: "Samsung Galaxy A54 5G",
                brand: "Samsung",
                model: "SM-A546B",
                ua: "Mozilla/5.0 (Linux; Android 14; SM-A546B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68 MP5",
                screenWidth: 360,
                screenHeight: 800,
                dpr: 3,
                memory: 8
            },
            {
                name: "Google Pixel 9 Pro",
                brand: "Google",
                model: "Pixel 9 Pro",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 9 Pro Build/AD1A.240530.047; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715",
                screenWidth: 412,
                screenHeight: 923,
                dpr: 2.875,
                memory: 16
            },
            {
                name: "Google Pixel 9",
                brand: "Google",
                model: "Pixel 9",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 9 Build/AD1A.240530.047; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715",
                screenWidth: 412,
                screenHeight: 923,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Google Pixel 8 Pro",
                brand: "Google",
                model: "Pixel 8 Pro",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UD1A.230803.041; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.875,
                memory: 12
            },
            {
                name: "Google Pixel 8",
                brand: "Google",
                model: "Pixel 8",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UD1A.230803.041; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Google Pixel 8a",
                brand: "Google",
                model: "Pixel 8a",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 8a Build/UD2A.240505.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Google Pixel Fold",
                brand: "Google",
                model: "Pixel Fold",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel Fold Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710",
                screenWidth: 412,
                screenHeight: 892,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Google Pixel 7 Pro",
                brand: "Google",
                model: "Pixel 7 Pro",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 7 Pro Build/UQ1A.240205.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Google Pixel 7",
                brand: "Google",
                model: "Pixel 7",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 7 Build/UQ1A.240205.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Google Pixel 7a",
                brand: "Google",
                model: "Pixel 7a",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 7a Build/UQ1A.240205.004; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Google Pixel 6 Pro",
                brand: "Google",
                model: "Pixel 6 Pro",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 6 Pro Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G78 MP20",
                screenWidth: 412,
                screenHeight: 892,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Google Pixel 6",
                brand: "Google",
                model: "Pixel 6",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 6 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G78 MP20",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Google Pixel 6a",
                brand: "Google",
                model: "Pixel 6a",
                ua: "Mozilla/5.0 (Linux; Android 14; Pixel 6a Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G78 MP20",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 6
            },
            {
                name: "Xiaomi 14 Ultra",
                brand: "Xiaomi",
                model: "24030PN60G",
                ua: "Mozilla/5.0 (Linux; Android 14; 24030PN60G Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Xiaomi 14 Pro",
                brand: "Xiaomi",
                model: "23116PN5BC",
                ua: "Mozilla/5.0 (Linux; Android 14; 23116PN5BC Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 14",
                brand: "Xiaomi",
                model: "23127PN0CG",
                ua: "Mozilla/5.0 (Linux; Android 14; 23127PN0CG Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 13 Ultra",
                brand: "Xiaomi",
                model: "2304FPN6DC",
                ua: "Mozilla/5.0 (Linux; Android 14; 2304FPN6DC Build/UKQ1.230804.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Xiaomi 13 Pro",
                brand: "Xiaomi",
                model: "2210132G",
                ua: "Mozilla/5.0 (Linux; Android 13; 2210132G Build/TKQ1.221114.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 13",
                brand: "Xiaomi",
                model: "2211133G",
                ua: "Mozilla/5.0 (Linux; Android 13; 2211133G Build/TKQ1.221114.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "Xiaomi 13T Pro",
                brand: "Xiaomi",
                model: "23078PND5G",
                ua: "Mozilla/5.0 (Linux; Android 14; 23078PND5G Build/UKQ1.230917.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 13T",
                brand: "Xiaomi",
                model: "2306EPN60G",
                ua: "Mozilla/5.0 (Linux; Android 14; 2306EPN60G Build/UKQ1.230917.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "Xiaomi 12S Ultra",
                brand: "Xiaomi",
                model: "2203121C",
                ua: "Mozilla/5.0 (Linux; Android 13; 2203121C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 12 Pro",
                brand: "Xiaomi",
                model: "2201122G",
                ua: "Mozilla/5.0 (Linux; Android 13; 2201122G Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 12",
                brand: "Xiaomi",
                model: "2201123G",
                ua: "Mozilla/5.0 (Linux; Android 13; 2201123G Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "Xiaomi 12T Pro",
                brand: "Xiaomi",
                model: "22081212UG",
                ua: "Mozilla/5.0 (Linux; Android 13; 22081212UG Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Xiaomi 11T Pro",
                brand: "Xiaomi",
                model: "2107113SG",
                ua: "Mozilla/5.0 (Linux; Android 13; 2107113SG Build/TKQ1.220807.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 660",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Xiaomi 11 Ultra",
                brand: "Xiaomi",
                model: "M2102K1G",
                ua: "Mozilla/5.0 (Linux; Android 13; M2102K1G Build/TKQ1.220807.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 660",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "POCO F6 Pro",
                brand: "Xiaomi",
                model: "23113RKC6G",
                ua: "Mozilla/5.0 (Linux; Android 14; 23113RKC6G Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "POCO F6",
                brand: "Xiaomi",
                model: "24069PC21G",
                ua: "Mozilla/5.0 (Linux; Android 14; 24069PC21G Build/UKQ1.240227.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 735",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 8
            },
            {
                name: "POCO F5 Pro",
                brand: "Xiaomi",
                model: "23013PC75G",
                ua: "Mozilla/5.0 (Linux; Android 13; 23013PC75G Build/TKQ1.221013.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "POCO F5",
                brand: "Xiaomi",
                model: "23049PCD8G",
                ua: "Mozilla/5.0 (Linux; Android 13; 23049PCD8G Build/TKQ1.221013.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 725",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "POCO X6 Pro",
                brand: "Xiaomi",
                model: "2311DRK48G",
                ua: "Mozilla/5.0 (Linux; Android 14; 2311DRK48G Build/UKQ1.230917.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G615 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "POCO X6",
                brand: "Xiaomi",
                model: "23122PCD1G",
                ua: "Mozilla/5.0 (Linux; Android 14; 23122PCD1G Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 710",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "POCO X5 Pro",
                brand: "Xiaomi",
                model: "22101320G",
                ua: "Mozilla/5.0 (Linux; Android 13; 22101320G Build/TKQ1.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 642L",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "POCO F4 GT",
                brand: "Xiaomi",
                model: "21121210G",
                ua: "Mozilla/5.0 (Linux; Android 13; 21121210G Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "POCO F4",
                brand: "Xiaomi",
                model: "22021211RG",
                ua: "Mozilla/5.0 (Linux; Android 13; 22021211RG Build/TKQ1.220807.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 650",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "POCO F3",
                brand: "Xiaomi",
                model: "M2012K11AG",
                ua: "Mozilla/5.0 (Linux; Android 13; M2012K11AG Build/TKQ1.220807.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 650",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Redmi K70 Pro",
                brand: "Xiaomi",
                model: "23117RK66C",
                ua: "Mozilla/5.0 (Linux; Android 14; 23117RK66C Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Redmi K70",
                brand: "Xiaomi",
                model: "2311DRK48C",
                ua: "Mozilla/5.0 (Linux; Android 14; 2311DRK48C Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Redmi K60 Pro",
                brand: "Xiaomi",
                model: "22127RK46C",
                ua: "Mozilla/5.0 (Linux; Android 13; 22127RK46C Build/TKQ1.221114.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Redmi K60",
                brand: "Xiaomi",
                model: "23013RK75C",
                ua: "Mozilla/5.0 (Linux; Android 13; 23013RK75C Build/TKQ1.221013.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "Redmi Note 13 Pro+",
                brand: "Xiaomi",
                model: "23090RA98G",
                ua: "Mozilla/5.0 (Linux; Android 14; 23090RA98G Build/UKQ1.230917.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC4",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Redmi Note 13 Pro",
                brand: "Xiaomi",
                model: "2312DRA50G",
                ua: "Mozilla/5.0 (Linux; Android 14; 2312DRA50G Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 710",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 8
            },
            {
                name: "Redmi Note 12 Turbo",
                brand: "Xiaomi",
                model: "23049RAD8C",
                ua: "Mozilla/5.0 (Linux; Android 13; 23049RAD8C Build/TKQ1.221013.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 725",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Redmi Note 12 Pro+",
                brand: "Xiaomi",
                model: "22101316UG",
                ua: "Mozilla/5.0 (Linux; Android 13; 22101316UG Build/TKQ1.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68 MC4",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Redmi K50 Gaming",
                brand: "Xiaomi",
                model: "21121210C",
                ua: "Mozilla/5.0 (Linux; Android 13; 21121210C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Redmi Note 11 Pro+",
                brand: "Xiaomi",
                model: "21091116UG",
                ua: "Mozilla/5.0 (Linux; Android 12; 21091116UG Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68 MC4",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "OnePlus 12",
                brand: "OnePlus",
                model: "CPH2581",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2581 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "OnePlus 12R",
                brand: "OnePlus",
                model: "CPH2609",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2609 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "OnePlus 11 5G",
                brand: "OnePlus",
                model: "CPH2449",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2449 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 919,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "OnePlus 11R",
                brand: "OnePlus",
                model: "CPH2487",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2487 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "OnePlus Open",
                brand: "OnePlus",
                model: "CPH2551",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2551 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "OnePlus 10 Pro",
                brand: "OnePlus",
                model: "NE2213",
                ua: "Mozilla/5.0 (Linux; Android 14; NE2213 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 919,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "OnePlus 10T",
                brand: "OnePlus",
                model: "CPH2415",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2415 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 16
            },
            {
                name: "OnePlus 9 Pro",
                brand: "OnePlus",
                model: "LE2123",
                ua: "Mozilla/5.0 (Linux; Android 13; LE2123 Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 660",
                screenWidth: 412,
                screenHeight: 919,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "OnePlus Nord 3",
                brand: "OnePlus",
                model: "CPH2493",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2493 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710 MC10",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "OnePlus Nord CE 3",
                brand: "OnePlus",
                model: "CPH2569",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2569 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 642L",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Vivo X100 Pro",
                brand: "vivo",
                model: "V2324A",
                ua: "Mozilla/5.0 (Linux; Android 14; V2324A Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G720 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Vivo X100",
                brand: "vivo",
                model: "V2309A",
                ua: "Mozilla/5.0 (Linux; Android 14; V2309A Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G720 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Vivo X90 Pro+",
                brand: "vivo",
                model: "V2227A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2227A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Vivo X90 Pro",
                brand: "vivo",
                model: "V2218",
                ua: "Mozilla/5.0 (Linux; Android 13; V2218 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.8,
                memory: 12
            },
            {
                name: "Vivo X90",
                brand: "vivo",
                model: "V2241A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2241A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.8,
                memory: 8
            },
            {
                name: "Vivo X80 Pro",
                brand: "vivo",
                model: "V2185A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2185A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Vivo V30 Pro",
                brand: "vivo",
                model: "V2319",
                ua: "Mozilla/5.0 (Linux; Android 14; V2319 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.8,
                memory: 12
            },
            {
                name: "Vivo V29 Pro",
                brand: "vivo",
                model: "V2254",
                ua: "Mozilla/5.0 (Linux; Android 13; V2254 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.8,
                memory: 12
            },
            {
                name: "iQOO 12 Pro",
                brand: "vivo",
                model: "V2329A",
                ua: "Mozilla/5.0 (Linux; Android 14; V2329A Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "iQOO 12",
                brand: "vivo",
                model: "V2307A",
                ua: "Mozilla/5.0 (Linux; Android 14; V2307A Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "iQOO 11 Pro",
                brand: "vivo",
                model: "V2243A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2243A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "iQOO 11",
                brand: "vivo",
                model: "V2243A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2243A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "iQOO Neo 9 Pro",
                brand: "vivo",
                model: "V2339A",
                ua: "Mozilla/5.0 (Linux; Android 14; V2339A Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G720 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "iQOO Neo 8 Pro",
                brand: "vivo",
                model: "V2302A",
                ua: "Mozilla/5.0 (Linux; Android 13; V2302A Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Oppo Find X7 Ultra",
                brand: "OPPO",
                model: "PHY110",
                ua: "Mozilla/5.0 (Linux; Android 14; PHY110 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "Oppo Find X7",
                brand: "OPPO",
                model: "PHZ110",
                ua: "Mozilla/5.0 (Linux; Android 14; PHZ110 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G720 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Oppo Find X6 Pro",
                brand: "OPPO",
                model: "PGEM10",
                ua: "Mozilla/5.0 (Linux; Android 13; PGEM10 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 16
            },
            {
                name: "Oppo Find X6",
                brand: "OPPO",
                model: "PGFM10",
                ua: "Mozilla/5.0 (Linux; Android 13; PGFM10 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G715 Immortalis",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Oppo Find N3",
                brand: "OPPO",
                model: "CPH2499",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2499 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "Oppo Reno 11 Pro",
                brand: "OPPO",
                model: "CPH2607",
                ua: "Mozilla/5.0 (Linux; Android 14; CPH2607 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.8,
                memory: 12
            },
            {
                name: "Oppo Reno 10 Pro+",
                brand: "OPPO",
                model: "CPH2521",
                ua: "Mozilla/5.0 (Linux; Android 13; CPH2521 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Oppo Reno 9 Pro+",
                brand: "OPPO",
                model: "PGW110",
                ua: "Mozilla/5.0 (Linux; Android 13; PGW110 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 16
            },
            {
                name: "Oppo Find X5 Pro",
                brand: "OPPO",
                model: "CPH2305",
                ua: "Mozilla/5.0 (Linux; Android 13; CPH2305 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Oppo Reno 8 Pro+",
                brand: "OPPO",
                model: "PFZM10",
                ua: "Mozilla/5.0 (Linux; Android 13; PFZM10 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Realme GT 5 Pro",
                brand: "realme",
                model: "RMX3888",
                ua: "Mozilla/5.0 (Linux; Android 14; RMX3888 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Realme GT 5",
                brand: "realme",
                model: "RMX3781",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3781 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 16
            },
            {
                name: "Realme GT Neo 5",
                brand: "realme",
                model: "RMX3708",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3708 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 880,
                dpr: 2.75,
                memory: 16
            },
            {
                name: "Realme GT Neo 3",
                brand: "realme",
                model: "RMX3560",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3560 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Realme GT 2 Pro",
                brand: "realme",
                model: "RMX3301",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3301 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3.5,
                memory: 12
            },
            {
                name: "Realme 12 Pro+",
                brand: "realme",
                model: "RMX3840",
                ua: "Mozilla/5.0 (Linux; Android 14; RMX3840 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 710",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Realme 11 Pro+",
                brand: "realme",
                model: "RMX3740",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3740 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68 MC4",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Realme GT Master",
                brand: "realme",
                model: "RMX3363",
                ua: "Mozilla/5.0 (Linux; Android 13; RMX3363 Build/TP1A.220905.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 642L",
                screenWidth: 360,
                screenHeight: 800,
                dpr: 3,
                memory: 8
            },
            {
                name: "Motorola Edge 50 Ultra",
                brand: "motorola",
                model: "XT2401-2",
                ua: "Mozilla/5.0 (Linux; Android 14; XT2401-2 Build/U1UC34.1-107; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 735",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "Motorola Edge 50 Pro",
                brand: "motorola",
                model: "XT2403-2",
                ua: "Mozilla/5.0 (Linux; Android 14; XT2403-2 Build/U1UC34.1-107; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 710",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "Motorola Edge 40 Pro",
                brand: "motorola",
                model: "XT2301-4",
                ua: "Mozilla/5.0 (Linux; Android 14; XT2301-4 Build/U1TR34.8-30; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Motorola Razr 40 Ultra",
                brand: "motorola",
                model: "XT2321-1",
                ua: "Mozilla/5.0 (Linux; Android 14; XT2321-1 Build/U1TZ34.8-19; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 360,
                screenHeight: 880,
                dpr: 3,
                memory: 8
            },
            {
                name: "Motorola Edge 30 Ultra",
                brand: "motorola",
                model: "XT2241-2",
                ua: "Mozilla/5.0 (Linux; Android 13; XT2241-2 Build/T1SQ33.73-15; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Motorola ThinkPhone",
                brand: "motorola",
                model: "XT2309-1",
                ua: "Mozilla/5.0 (Linux; Android 14; XT2309-1 Build/U1TN34.8-21; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Asus ROG Phone 8 Pro",
                brand: "asus",
                model: "ASUS_AI2401_D",
                ua: "Mozilla/5.0 (Linux; Android 14; ASUS_AI2401_D Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 16
            },
            {
                name: "Asus ROG Phone 7 Ultimate",
                brand: "asus",
                model: "ASUS_AI2205_D",
                ua: "Mozilla/5.0 (Linux; Android 14; ASUS_AI2205_D Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 16
            },
            {
                name: "Asus ROG Phone 6",
                brand: "asus",
                model: "ASUS_AI2201_B",
                ua: "Mozilla/5.0 (Linux; Android 13; ASUS_AI2201_B Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Asus Zenfone 10",
                brand: "asus",
                model: "ASUS_AI2302",
                ua: "Mozilla/5.0 (Linux; Android 14; ASUS_AI2302 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Asus Zenfone 9",
                brand: "asus",
                model: "ASUS_AI2202",
                ua: "Mozilla/5.0 (Linux; Android 13; ASUS_AI2202 Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Asus ROG Phone 5",
                brand: "asus",
                model: "ASUS_I005D",
                ua: "Mozilla/5.0 (Linux; Android 13; ASUS_I005D Build/TKQ1.220807.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 660",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Sony Xperia 1 VI",
                brand: "Sony",
                model: "XQ-EC54",
                ua: "Mozilla/5.0 (Linux; Android 14; XQ-EC54 Build/69.0.A.2.28; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Sony Xperia 1 V",
                brand: "Sony",
                model: "XQ-DQ54",
                ua: "Mozilla/5.0 (Linux; Android 14; XQ-DQ54 Build/67.1.A.2.194; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 384,
                screenHeight: 960,
                dpr: 4,
                memory: 12
            },
            {
                name: "Sony Xperia 5 V",
                brand: "Sony",
                model: "XQ-DE54",
                ua: "Mozilla/5.0 (Linux; Android 14; XQ-DE54 Build/67.1.A.2.194; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 360,
                screenHeight: 800,
                dpr: 3,
                memory: 8
            },
            {
                name: "Sony Xperia 1 IV",
                brand: "Sony",
                model: "XQ-CT54",
                ua: "Mozilla/5.0 (Linux; Android 13; XQ-CT54 Build/64.1.A.0.891; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 384,
                screenHeight: 960,
                dpr: 4,
                memory: 12
            },
            {
                name: "Sony Xperia 5 IV",
                brand: "Sony",
                model: "XQ-CQ54",
                ua: "Mozilla/5.0 (Linux; Android 13; XQ-CQ54 Build/64.1.A.0.891; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 360,
                screenHeight: 800,
                dpr: 3,
                memory: 8
            },
            {
                name: "Nothing Phone (2)",
                brand: "Nothing",
                model: "A065",
                ua: "Mozilla/5.0 (Linux; Android 14; A065 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Nothing Phone (1)",
                brand: "Nothing",
                model: "A063",
                ua: "Mozilla/5.0 (Linux; Android 14; A063 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 642L",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 8
            },
            {
                name: "Nothing Phone (2a)",
                brand: "Nothing",
                model: "A142",
                ua: "Mozilla/5.0 (Linux; Android 14; A142 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC4",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Nothing Phone (2a) Plus",
                brand: "Nothing",
                model: "A142P",
                ua: "Mozilla/5.0 (Linux; Android 14; A142P Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC4",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 2.625,
                memory: 12
            },
            {
                name: "Honor Magic 6 Pro",
                brand: "HONOR",
                model: "BVL-AN16",
                ua: "Mozilla/5.0 (Linux; Android 14; BVL-AN16 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 750",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "Honor Magic 5 Pro",
                brand: "HONOR",
                model: "PGT-AN10",
                ua: "Mozilla/5.0 (Linux; Android 13; PGT-AN10 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "Honor Magic V2",
                brand: "HONOR",
                model: "VER-AN10",
                ua: "Mozilla/5.0 (Linux; Android 13; VER-AN10 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 16
            },
            {
                name: "Honor 100 Pro",
                brand: "HONOR",
                model: "MAA-AN10",
                ua: "Mozilla/5.0 (Linux; Android 14; MAA-AN10 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 740",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "Honor 90 Pro",
                brand: "HONOR",
                model: "REP-AN00",
                ua: "Mozilla/5.0 (Linux; Android 13; REP-AN00 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "Honor Magic 4 Pro",
                brand: "HONOR",
                model: "LGE-AN10",
                ua: "Mozilla/5.0 (Linux; Android 13; LGE-AN10 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 730",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 8
            },
            {
                name: "Honor 90",
                brand: "HONOR",
                model: "REA-NX9",
                ua: "Mozilla/5.0 (Linux; Android 13; REA-NX9 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 644",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Honor 70",
                brand: "HONOR",
                model: "FNE-AN00",
                ua: "Mozilla/5.0 (Linux; Android 12; FNE-AN00 Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "Qualcomm",
                gpuRenderer: "Adreno (TM) 642L",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Infinix Note 30 VIP",
                brand: "Infinix",
                model: "Infinix X6739",
                ua: "Mozilla/5.0 (Linux; Android 13; Infinix X6739 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 895,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Infinix Zero 30 5G",
                brand: "Infinix",
                model: "Infinix X6731",
                ua: "Mozilla/5.0 (Linux; Android 13; Infinix X6731 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Infinix GT 10 Pro",
                brand: "Infinix",
                model: "Infinix X6739",
                ua: "Mozilla/5.0 (Linux; Android 13; Infinix X6739 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G77 MC9",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Infinix GT 20 Pro",
                brand: "Infinix",
                model: "Infinix X6871",
                ua: "Mozilla/5.0 (Linux; Android 14; Infinix X6871 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Infinix Zero Ultra",
                brand: "Infinix",
                model: "Infinix X6820",
                ua: "Mozilla/5.0 (Linux; Android 12; Infinix X6820 Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G68 MC4",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 8
            },
            {
                name: "Tecno Camon 30 Premier",
                brand: "Tecno",
                model: "Tecno CL9",
                ua: "Mozilla/5.0 (Linux; Android 14; Tecno CL9 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G610 MC6",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 3,
                memory: 12
            },
            {
                name: "Tecno Phantom X2 Pro",
                brand: "Tecno",
                model: "Tecno AD9",
                ua: "Mozilla/5.0 (Linux; Android 13; Tecno AD9 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710 MC10",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            },
            {
                name: "Tecno Phantom V Fold",
                brand: "Tecno",
                model: "Tecno AD10",
                ua: "Mozilla/5.0 (Linux; Android 13; Tecno AD10 Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G710 MC10",
                screenWidth: 412,
                screenHeight: 915,
                dpr: 3,
                memory: 12
            },
            {
                name: "Tecno Pova 6 Pro",
                brand: "Tecno",
                model: "Tecno LI9",
                ua: "Mozilla/5.0 (Linux; Android 14; Tecno LI9 Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.102 Mobile Safari/537.36",
                gpuVendor: "ARM",
                gpuRenderer: "Mali-G57 MC2",
                screenWidth: 393,
                screenHeight: 873,
                dpr: 2.75,
                memory: 12
            }
        ];

        function getActiveDeviceProfile() {
            const actualChromeVer = window.__FUSION_CHROME_VERSION__ || ((navigator.userAgent || '').match(/Chrome\/([0-9.]+)/) || [])[1] || '130.0.6723.102';
            if (window.__FUSION_DEVICE__ && window.__FUSION_DEVICE__.userAgent) {
                const jd = window.__FUSION_DEVICE__;
                const normalizedUa = String(jd.userAgent).replace(/Chrome\/[0-9.]+/g, 'Chrome/' + actualChromeVer);
                return {
                    name: jd.name || 'Samsung Galaxy S24 Ultra',
                    brand: jd.brand || 'Samsung',
                    model: jd.model || 'SM-S928B',
                    ua: normalizedUa,
                    gpuVendor: jd.vendor || 'Qualcomm',
                    gpuRenderer: jd.renderer || 'Adreno (TM) 750',
                    screenWidth: jd.screenWidth ? Math.round(jd.screenWidth / (jd.pixelRatio || 3)) : 384,
                    screenHeight: jd.screenHeight ? Math.round(jd.screenHeight / (jd.pixelRatio || 3)) : 832,
                    dpr: jd.pixelRatio || 3,
                    memory: jd.memory || 12
                };
            }
            const seed = getWalletSeed();
            const picked = DEVICE_PROFILES[seed % DEVICE_PROFILES.length];
            return Object.assign({}, picked, {
                ua: String(picked.ua).replace(/Chrome\/[0-9.]+/g, 'Chrome/' + actualChromeVer)
            });
        }

        // 1. Scrub navigator.webdriver
        try {
            Object.defineProperty(navigator, 'webdriver', {
                get: () => undefined,
                configurable: true
            });
            delete Navigator.prototype.webdriver;
        } catch (_) {}

        // 2. Full Chrome Runtime Parity
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
                    const t = (window.performance && window.performance.timing) || {};
                    const start = (t.navigationStart || Date.now()) / 1000;
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
                    const t = (window.performance && window.performance.timing) || {};
                    const s = t.navigationStart || Date.now();
                    const e = t.loadEventEnd || Date.now();
                    return { startE: s, onloadT: e, pageT: e - s, tran: 15 };
                };
            }
        } catch (_) {}

        // 3. Permissions API Parity
        if (navigator.permissions && navigator.permissions.query) {
            try {
                const origQuery = navigator.permissions.query.bind(navigator.permissions);
                navigator.permissions.query = (param) => {
                    if (param && param.name === 'notifications') {
                        const perm = (typeof Notification !== 'undefined') ? Notification.permission : 'default';
                        return Promise.resolve({ state: perm === 'granted' ? 'granted' : 'prompt', onchange: null });
                    }
                    return origQuery(param);
                };
            } catch (_) {}
        }

        // 4. Dynamic Seeded Micro-Noise for 2D Canvas (Unique Fingerprint per Wallet, skip double-noise if document_start already hooked)
        if (!window.__FUSION_CANVAS_STEALTH__) try {
            window.__FUSION_CANVAS_STEALTH__ = true;
            const origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
            CanvasRenderingContext2D.prototype.getImageData = function(x, y, w, h) {
                const imgData = origGetImageData.apply(this, arguments);
                try {
                    const seed = getWalletSeed();
                    const d = imgData.data;
                    const len = d.length;
                    if (len >= 4) {
                        const step = Math.max(4, Math.floor(len / 16));
                        for (let i = 0; i < len; i += step) {
                            const bitShift = ((seed ^ (i * 31)) & 1) ? 1 : -1;
                            const newVal = d[i] + bitShift;
                            if (newVal >= 0 && newVal <= 255) {
                                d[i] = newVal;
                            }
                        }
                    }
                } catch (_) {}
                return imgData;
            };

            const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function(type, encoderOptions) {
                try {
                    const ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0) {
                        const seed = getWalletSeed();
                        const pX = (seed % this.width);
                        const pY = ((seed >>> 8) % this.height);
                        const px = ctx.getImageData(pX, pY, 1, 1);
                        const shift = ((seed % 5) + 1);
                        px.data[0] = (px.data[0] + shift) % 256;
                        px.data[3] = Math.max(1, px.data[3]);
                        ctx.putImageData(px, pX, pY);
                    }
                } catch (_) {}
                return origToDataURL.apply(this, arguments);
            };

            const origToBlob = HTMLCanvasElement.prototype.toBlob;
            HTMLCanvasElement.prototype.toBlob = function(callback, type, quality) {
                try {
                    const ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0) {
                        const seed = getWalletSeed();
                        const pX = (seed % this.width);
                        const pY = ((seed >>> 8) % this.height);
                        const px = ctx.getImageData(pX, pY, 1, 1);
                        const shift = ((seed % 5) + 1);
                        px.data[0] = (px.data[0] + shift) % 256;
                        px.data[3] = Math.max(1, px.data[3]);
                        ctx.putImageData(px, pX, pY);
                    }
                } catch (_) {}
                return origToBlob.apply(this, arguments);
            };
        } catch (_) {}

        // 5. Context-Aware WebGL Spoofing (Mobile vs Desktop)
        if (!window.__FUSION_WEBGL_STEALTH__) try {
            window.__FUSION_WEBGL_STEALTH__ = true;
            const hookWebGL = (proto) => {
                if (!proto || !proto.getParameter) return;
                const origGetParam = proto.getParameter;
                proto.getParameter = function(param) {
                    const isDesktopMode = (navigator.userAgent && navigator.userAgent.includes('Windows')) || (window.innerWidth >= 1200);
                    const devProfile = getActiveDeviceProfile();
                    // UNMASKED_VENDOR_WEBGL (0x9245)
                    if (param === 0x9245 || param === 37445) {
                        return isDesktopMode ? 'Google Inc. (NVIDIA)' : devProfile.gpuVendor;
                    }
                    // UNMASKED_RENDERER_WEBGL (0x9246)
                    if (param === 0x9246 || param === 37446) {
                        return isDesktopMode
                            ? 'ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)'
                            : devProfile.gpuRenderer;
                    }
                    return origGetParam.apply(this, arguments);
                };
            };
            if (window.WebGLRenderingContext) hookWebGL(WebGLRenderingContext.prototype);
            if (window.WebGL2RenderingContext) hookWebGL(WebGL2RenderingContext.prototype);
        } catch (_) {}

        // 6. AudioContext Micro-Jitter per Wallet
        try {
            if (window.OfflineAudioContext) {
                const origStart = OfflineAudioContext.prototype.startRendering;
                OfflineAudioContext.prototype.startRendering = function() {
                    return origStart.apply(this, arguments).then((buffer) => {
                        try {
                            const seed = getWalletSeed();
                            const channel = buffer.getChannelData(0);
                            if (channel && channel.length > 50) {
                                const pos = Math.floor(channel.length / 2);
                                channel[pos] += ((seed % 100) * 0.00000001);
                            }
                        } catch (_) {}
                        return buffer;
                    });
                };
            }
        } catch (_) {}

        // 7. Context-Aware Font Cloaking & Metric Alignment (Breaking Font Mismatch)
        try {
            const isDesktopWin = () => (navigator.userAgent && navigator.userAgent.includes('Windows'));
            const winFonts = new Set([
                'segoe ui', 'tahoma', 'arial', 'times new roman', 'consolas', 'calibri',
                'cambria', 'verdana', 'trebuchet ms', 'georgia', 'comic sans ms', 'impact'
            ]);

            if (document.fonts && document.fonts.check) {
                const origFontsCheck = document.fonts.check.bind(document.fonts);
                document.fonts.check = function(font, text) {
                    if (isDesktopWin()) {
                        const lower = String(font || '').toLowerCase();
                        for (const f of winFonts) {
                            if (lower.includes(f)) return true;
                        }
                    }
                    return origFontsCheck(font, text);
                };
            }

            const origMeasure = CanvasRenderingContext2D.prototype.measureText;
            CanvasRenderingContext2D.prototype.measureText = function(text) {
                const m = origMeasure.apply(this, arguments);
                try {
                    if (isDesktopWin() && this.font) {
                        const fontLower = this.font.toLowerCase();
                        for (const f of winFonts) {
                            if (fontLower.includes(f) && !fontLower.includes('roboto')) {
                                const originalWidth = m.width;
                                const fakeWidth = originalWidth * 1.042 + 0.35;
                                return new Proxy(m, {
                                    get(target, prop) {
                                        if (prop === 'width') return fakeWidth;
                                        return target[prop];
                                    }
                                });
                            }
                        }
                    }
                } catch (_) {}
                return m;
            };
        } catch (_) {}

        // 8. Hardware Concurrency, Device Memory & Full Identity Parity (Deterministic per Wallet)
        try {
            window.__FUSION_GET_ACTIVE_DEVICE_PROFILE__ = getActiveDeviceProfile;
            const isDesktop = Boolean(window.__FUSION_IS_DESKTOP__) || (navigator.userAgent && (navigator.userAgent.includes('Windows') || navigator.userAgent.includes('Macintosh')));
            const activeChromeVer = window.__FUSION_CHROME_VERSION__ || ((navigator.userAgent || '').match(/Chrome\/([0-9.]+)/) || [])[1] || '130.0.6723.102';
            const activeChromeMajor = activeChromeVer.split('.')[0] || '130';

            Object.defineProperty(navigator, 'hardwareConcurrency', {
                get: () => 8,
                configurable: true
            });
            Object.defineProperty(navigator, 'deviceMemory', {
                get: () => isDesktop ? 8 : (getActiveDeviceProfile().memory || 8),
                configurable: true
            });

            if (isDesktop) {
                const isMac = navigator.userAgent && navigator.userAgent.includes('Macintosh');
                Object.defineProperty(navigator, 'platform', {
                    get: () => isMac ? 'MacIntel' : 'Win32',
                    configurable: true
                });
                Object.defineProperty(screen, 'width', { get: () => 1920, configurable: true });
                Object.defineProperty(screen, 'height', { get: () => 1080, configurable: true });
                Object.defineProperty(screen, 'availWidth', { get: () => 1920, configurable: true });
                Object.defineProperty(screen, 'availHeight', { get: () => 1040, configurable: true });
                Object.defineProperty(window, 'devicePixelRatio', { get: () => 1, configurable: true });
                if (navigator.userAgentData) {
                    const desktopBrands = [
                        { brand: 'Chromium', version: activeChromeMajor },
                        { brand: 'Google Chrome', version: activeChromeMajor },
                        { brand: 'Not_A Brand', version: '24' }
                    ];
                    try { Object.defineProperty(navigator.userAgentData, 'brands', { get: () => desktopBrands, configurable: true }); } catch (_) {}
                    try { Object.defineProperty(navigator.userAgentData, 'mobile', { get: () => false, configurable: true }); } catch (_) {}
                    try { Object.defineProperty(navigator.userAgentData, 'platform', { get: () => isMac ? 'macOS' : 'Windows', configurable: true }); } catch (_) {}
                    navigator.userAgentData.getHighEntropyValues = () => Promise.resolve({
                        architecture: 'x86',
                        bitness: '64',
                        brands: desktopBrands,
                        fullVersionList: desktopBrands.map(b => ({ brand: b.brand, version: activeChromeVer })),
                        model: '',
                        platform: isMac ? 'macOS' : 'Windows',
                        platformVersion: isMac ? '14.0.0' : '10.0.0',
                        mobile: false,
                        uaFullVersion: activeChromeVer
                    });
                }
            } else {
                Object.defineProperty(navigator, 'platform', {
                    get: () => 'Linux armv8l',
                    configurable: true
                });
                // Hanya override navigator.userAgent jika belum diset konsisten oleh native Java WebSettings
                if (!window.__FUSION_DEVICE__) {
                    Object.defineProperty(navigator, 'userAgent', {
                        get: () => getActiveDeviceProfile().ua,
                        configurable: true
                    });
                    Object.defineProperty(navigator, 'appVersion', {
                        get: () => {
                            const ua = getActiveDeviceProfile().ua;
                            return ua.substring(ua.indexOf('/') + 1);
                        },
                        configurable: true
                    });
                }
                if (navigator.userAgentData) {
                    const mobileBrands = [
                        { brand: 'Chromium', version: activeChromeMajor },
                        { brand: 'Google Chrome', version: activeChromeMajor },
                        { brand: 'Not_A Brand', version: '24' }
                    ];
                    try { Object.defineProperty(navigator.userAgentData, 'brands', { get: () => mobileBrands, configurable: true }); } catch (_) {}
                    try { Object.defineProperty(navigator.userAgentData, 'mobile', { get: () => true, configurable: true }); } catch (_) {}
                    try { Object.defineProperty(navigator.userAgentData, 'platform', { get: () => 'Android', configurable: true }); } catch (_) {}
                    navigator.userAgentData.getHighEntropyValues = (hints) => {
                        const p = getActiveDeviceProfile();
                        return Promise.resolve({
                            architecture: 'arm64',
                            bitness: '64',
                            brands: mobileBrands,
                            fullVersionList: mobileBrands.map(b => ({ brand: b.brand, version: activeChromeVer })),
                            model: p.model,
                            platform: 'Android',
                            platformVersion: '14.0.0',
                            mobile: true,
                            uaFullVersion: activeChromeVer
                        });
                    };
                }
                Object.defineProperty(screen, 'width', { get: () => getActiveDeviceProfile().screenWidth, configurable: true });
                Object.defineProperty(screen, 'height', { get: () => getActiveDeviceProfile().screenHeight, configurable: true });
                Object.defineProperty(screen, 'availWidth', { get: () => getActiveDeviceProfile().screenWidth, configurable: true });
                Object.defineProperty(screen, 'availHeight', { get: () => getActiveDeviceProfile().screenHeight, configurable: true });
                Object.defineProperty(window, 'devicePixelRatio', { get: () => getActiveDeviceProfile().dpr, configurable: true });
            }
        } catch (_) {}
    })();

    // --- 2. Seamless Clipboard Interceptor ---
    if (navigator.clipboard) {
        try {
            const origRead = navigator.clipboard.readText ? navigator.clipboard.readText.bind(navigator.clipboard) : null;
            const origWrite = navigator.clipboard.writeText ? navigator.clipboard.writeText.bind(navigator.clipboard) : null;
            navigator.clipboard.readText = async () => {
                if (window.GhostBridge && typeof window.GhostBridge.getClipboard === 'function') {
                    const txt = window.GhostBridge.getClipboard();
                    if (txt) return txt;
                }
                if (origRead) {
                    try { return await origRead(); } catch (_) {}
                }
                return '';
            };
            navigator.clipboard.writeText = async (text) => {
                if (window.GhostBridge && typeof window.GhostBridge.copyToClipboard === 'function') {
                    window.GhostBridge.copyToClipboard(text);
                }
                if (origWrite) {
                    try { await origWrite(text); } catch (_) {}
                }
                return Promise.resolve();
            };
        } catch (_) {}
    }

    // --- 3. Virtual Passkey (W3C WebAuthn Platform Authenticator) ---
/**
 * FUSION VIRTUAL PASSKEY (FIDO2 / WebAuthn Hardware Authenticator Virtualizer)
 * Emulates a hardware biometric platform authenticator (User-Verifying Platform Authenticator)
 * inside Termux WebView. Bypasses Android fingerprint/biometric prompts 100% and generates
 * authentic, standard W3C WebAuthn Level 3 compliant credentials with persistent storage.
 * 
 * Part of Project Fusion / Eso Launcher Suite
 */

(() => {
    'use strict';

    if (window.__FUSION_PASSKEY_ENGINE_ACTIVE__) return;
    window.__FUSION_PASSKEY_ENGINE_ACTIVE__ = true;

    // --- 1. Base64URL & Binary Utilities ---
    function bufferToBase64Url(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    }

    function base64UrlToUint8Array(str) {
        let b64 = str.replace(/-/g, '+').replace(/_/g, '/');
        while (b64.length % 4) b64 += '=';
        const binary = atob(b64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes;
    }

    function stringToUtf8Bytes(str) {
        return new TextEncoder().encode(str);
    }

    async function sha256(dataBytes) {
        return new Uint8Array(await window.crypto.subtle.digest('SHA-256', dataBytes));
    }

    // --- 2. IEEE P1363 to ASN.1 DER Signature Converter ---
    function rawSigToDer(rawSig) {
        if (rawSig.byteLength !== 64) throw new Error("Invalid raw signature length: " + rawSig.byteLength);
        let r = rawSig.subarray(0, 32);
        let s = rawSig.subarray(32, 64);

        while (r.length > 1 && r[0] === 0 && !(r[1] & 0x80)) r = r.subarray(1);
        if (r[0] & 0x80) {
            const temp = new Uint8Array(r.length + 1);
            temp[0] = 0x00;
            temp.set(r, 1);
            r = temp;
        }

        while (s.length > 1 && s[0] === 0 && !(s[1] & 0x80)) s = s.subarray(1);
        if (s[0] & 0x80) {
            const temp = new Uint8Array(s.length + 1);
            temp[0] = 0x00;
            temp.set(s, 1);
            s = temp;
        }

        const totalLen = 2 + r.length + 2 + s.length;
        const der = new Uint8Array(2 + totalLen);
        der[0] = 0x30; // SEQUENCE
        der[1] = totalLen;
        der[2] = 0x02; // INTEGER
        der[3] = r.length;
        der.set(r, 4);

        const sOffset = 4 + r.length;
        der[sOffset] = 0x02; // INTEGER
        der[sOffset + 1] = s.length;
        der.set(s, sOffset + 2);

        return der.buffer;
    }

    // --- 3. Deterministic COSE & Attestation Builder ---
    function buildCoseKeyP256(rawX, rawY) {
        // CBOR Map with 5 items: { 1: 2, 3: -7, -1: 1, -2: rawX, -3: rawY }
        const header = new Uint8Array([0xa5, 0x01, 0x02, 0x03, 0x26, 0x20, 0x01, 0x21, 0x58, 0x20]);
        const mid = new Uint8Array([0x22, 0x58, 0x20]);
        const out = new Uint8Array(header.length + rawX.length + mid.length + rawY.length);
        out.set(header, 0);
        out.set(rawX, header.length);
        out.set(mid, header.length + rawX.length);
        out.set(rawY, header.length + rawX.length + mid.length);
        return out;
    }

    async function buildAuthDataCreate(rpId, credentialId, rawX, rawY, signCount = 0) {
        const rpIdHash = await sha256(stringToUtf8Bytes(rpId));
        const flags = new Uint8Array([0x5d]); // UP (0x01) + UV (0x04) + BE (0x08) + BS (0x10) + AT (0x40)
        const countBuf = new Uint8Array(4);
        new DataView(countBuf.buffer).setUint32(0, signCount, false);

        const aaguid = new Uint8Array(16); // 16 zero bytes
        const idLen = new Uint8Array(2);
        new DataView(idLen.buffer).setUint16(0, credentialId.byteLength, false);
        const coseKey = buildCoseKeyP256(rawX, rawY);

        const totalLen = rpIdHash.length + flags.length + countBuf.length + aaguid.length + idLen.length + credentialId.byteLength + coseKey.length;
        const authData = new Uint8Array(totalLen);
        let offset = 0;

        authData.set(rpIdHash, offset); offset += rpIdHash.length;
        authData.set(flags, offset); offset += flags.length;
        authData.set(countBuf, offset); offset += countBuf.length;
        authData.set(aaguid, offset); offset += aaguid.length;
        authData.set(idLen, offset); offset += idLen.length;
        authData.set(new Uint8Array(credentialId), offset); offset += credentialId.byteLength;
        authData.set(coseKey, offset);

        return authData;
    }

    function buildAttestationObjectNone(authData) {
        // CBOR Map with 3 items: { "fmt": "none", "attStmt": {}, "authData": authData }
        const part1 = new Uint8Array([0xa3, 0x63, 0x66, 0x6d, 0x74, 0x64, 0x6e, 0x6f, 0x6e, 0x65, 0x67, 0x61, 0x74, 0x74, 0x53, 0x74, 0x6d, 0x74, 0xa0, 0x68, 0x61, 0x75, 0x74, 0x68, 0x44, 0x61, 0x74, 0x61]);
        let lenPrefix;
        if (authData.length < 256) {
            lenPrefix = new Uint8Array([0x58, authData.length]);
        } else {
            lenPrefix = new Uint8Array([0x59, (authData.length >> 8) & 0xff, authData.length & 0xff]);
        }
        const out = new Uint8Array(part1.length + lenPrefix.length + authData.length);
        out.set(part1, 0);
        out.set(lenPrefix, part1.length);
        out.set(authData, part1.length + lenPrefix.length);
        return out.buffer;
    }

    // --- 4. Passkey Vault Manager (Local + Bridge Sync) ---
    const VAULT_KEY = 'fusion_passkeys_vault';

    function loadLocalVault() {
        try {
            // Coba baca dari GhostBridge jika tersedia
            if (window.GhostBridge && typeof window.GhostBridge.readLocalFile === 'function') {
                const paths = [
                    '/root/wallet/passkeys_vault.json',
                    '/data/data/com.termux/files/home/wallet/passkeys_vault.json'
                ];
                for (const p of paths) {
                    const content = window.GhostBridge.readLocalFile(p);
                    if (content && content.trim().startsWith('{')) {
                        const parsed = JSON.parse(content);
                        if (Array.isArray(parsed.passkeys) && parsed.passkeys.length > 0) return parsed.passkeys;
                    }
                }
            }
        } catch (_) {}

        try {
            const raw = localStorage.getItem(VAULT_KEY);
            if (raw) return JSON.parse(raw);
        } catch (_) {}
        return [];
    }

    function saveToLocalVault(passkeyEntry) {
        let list = loadLocalVault();
        const existingIdx = list.findIndex(p => p.credentialId === passkeyEntry.credentialId);
        if (existingIdx >= 0) {
            list[existingIdx] = passkeyEntry;
        } else {
            list.push(passkeyEntry);
        }

        try {
            localStorage.setItem(VAULT_KEY, JSON.stringify(list));
        } catch (_) {}

        // 1. Simpan ke SQLite eso.db via dbExec
        try {
            if (window.GhostBridge && typeof window.GhostBridge.dbExec === 'function') {
                const sql = "INSERT INTO passkeys (profile, rp_id, credential_id, user_handle, user_name, private_key_jwk, public_key_cose, sign_count, last_used) VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now')) ON CONFLICT(credential_id) DO UPDATE SET sign_count = excluded.sign_count, last_used = datetime('now');";
                const params = JSON.stringify([
                    passkeyEntry.profile || 'default',
                    passkeyEntry.rpId,
                    passkeyEntry.credentialId,
                    passkeyEntry.userHandle || '',
                    passkeyEntry.userName || '',
                    JSON.stringify(passkeyEntry.privateKeyJwk),
                    passkeyEntry.publicKeyCose || '',
                    passkeyEntry.signCount || 0
                ]);
                window.GhostBridge.dbExec(sql, params, "/root/wallet/eso.db");
            }
        } catch (_) {}

        // 2. Simpan ke passkeys_vault.json via execShell
        try {
            if (window.GhostBridge && typeof window.GhostBridge.execShell === 'function') {
                const vaultJson = JSON.stringify({ passkeys: list }, null, 2);
                const b64 = btoa(unescape(encodeURIComponent(vaultJson)));
                window.GhostBridge.execShell("echo '" + b64 + "' | base64 -d > /root/wallet/passkeys_vault.json");
            }
        } catch (_) {}
    }

    function showPasskeyToast(msg) {
        try {
            if (window.GhostBridge && typeof window.GhostBridge.toast === 'function') {
                window.GhostBridge.toast(msg);
                return;
            }
        } catch (_) {}
        console.log('[Fusion Virtual Passkey]', msg);
    }

    // --- 5. Intercept navigator.credentials ---
    if (window.navigator && window.navigator.credentials) {
        const originalCreate = window.navigator.credentials.create ? window.navigator.credentials.create.bind(window.navigator.credentials) : null;
        const originalGet = window.navigator.credentials.get ? window.navigator.credentials.get.bind(window.navigator.credentials) : null;

        // A. CREATE PASSKEY (REGISTRATION)
        window.navigator.credentials.create = async function(options) {
            if (!options || !options.publicKey) {
                if (originalCreate) return originalCreate(options);
                throw new Error("Only publicKey credentials supported");
            }

            const pk = options.publicKey;
            const rpId = pk.rp ? (pk.rp.id || window.location.hostname) : window.location.hostname;
            const userHandle = pk.user && pk.user.id ? bufferToBase64Url(pk.user.id) : '';
            const userName = pk.user ? (pk.user.name || pk.user.displayName || 'user') : 'user';

            // 1. Generate P-256 Key Pair
            const keyPair = await window.crypto.subtle.generateKey(
                { name: "ECDSA", namedCurve: "P-256" },
                true,
                ["sign", "verify"]
            );

            const pubJwk = await window.crypto.subtle.exportKey("jwk", keyPair.publicKey);
            const privJwk = await window.crypto.subtle.exportKey("jwk", keyPair.privateKey);

            const rawX = base64UrlToUint8Array(pubJwk.x);
            const rawY = base64UrlToUint8Array(pubJwk.y);

            // 2. Buat Credential ID Unik (32 bytes random)
            const credIdBytes = new Uint8Array(32);
            window.crypto.getRandomValues(credIdBytes);
            const credIdStr = bufferToBase64Url(credIdBytes.buffer);

            // 3. Bangun ClientDataJSON
            const challengeStr = pk.challenge ? bufferToBase64Url(pk.challenge) : '';
            const clientDataObj = {
                type: "webauthn.create",
                challenge: challengeStr,
                origin: window.location.origin,
                crossOrigin: false
            };
            const clientDataJsonBytes = stringToUtf8Bytes(JSON.stringify(clientDataObj));

            // 4. Bangun AuthenticatorData & AttestationObject
            const authData = await buildAuthDataCreate(rpId, credIdBytes.buffer, rawX, rawY, 0);
            const attestationObject = buildAttestationObjectNone(authData);

            // 5. Simpan ke Vault Persisten
            const activeProfile = window.__FUSION_ACTIVE_PROFILE__ || window.__FUSION_ACTIVE_WALLET_NAME__ || 'default';
            const passkeyEntry = {
                profile: activeProfile,
                rpId,
                credentialId: credIdStr,
                userHandle,
                userName,
                privateKeyJwk: privJwk,
                publicKeyCose: bufferToBase64Url(buildCoseKeyP256(rawX, rawY)),
                signCount: 0,
                createdAt: new Date().toISOString()
            };
            saveToLocalVault(passkeyEntry);
            showPasskeyToast(`🔑 Passkey Dibuat (${rpId})`);

            // 6. Kembalikan Objek PublicKeyCredential Standar W3C
            const coseBuffer = buildCoseKeyP256(rawX, rawY).buffer;
            const credential = {
                id: credIdStr,
                rawId: credIdBytes.buffer,
                type: "public-key",
                authenticatorAttachment: "platform",
                response: {
                    clientDataJSON: clientDataJsonBytes.buffer,
                    attestationObject: attestationObject,
                    getTransports: () => ["internal"],
                    getAuthenticatorData: () => authData.buffer,
                    getPublicKey: () => coseBuffer,
                    getPublicKeyAlgorithm: () => -7
                },
                getClientExtensionResults: () => ({}),
                toJSON: function() {
                    return {
                        id: credIdStr,
                        rawId: credIdStr,
                        response: {
                            clientDataJSON: bufferToBase64Url(clientDataJsonBytes.buffer),
                            attestationObject: bufferToBase64Url(attestationObject),
                            transports: ["internal"],
                            publicKeyAlgorithm: -7,
                            publicKey: bufferToBase64Url(coseBuffer),
                            authenticatorData: bufferToBase64Url(authData.buffer)
                        },
                        authenticatorAttachment: "platform",
                        clientExtensionResults: {},
                        type: "public-key"
                    };
                }
            };

            if (typeof window.PublicKeyCredential !== 'undefined') {
                Object.setPrototypeOf(credential, window.PublicKeyCredential.prototype);
            }
            if (typeof window.AuthenticatorAttestationResponse !== 'undefined') {
                Object.setPrototypeOf(credential.response, window.AuthenticatorAttestationResponse.prototype);
            }

            return credential;
        };

        // B. GET PASSKEY (LOGIN ASSERTION)
        window.navigator.credentials.get = async function(options) {
            if (!options || !options.publicKey) {
                if (originalGet) return originalGet(options);
                throw new Error("Only publicKey credentials supported");
            }

            const pk = options.publicKey;
            const rpId = pk.rpId || window.location.hostname;
            const vault = loadLocalVault();

            // Cari kunci yang cocok untuk domain ini
            let matched = null;
            if (pk.allowCredentials && Array.isArray(pk.allowCredentials) && pk.allowCredentials.length > 0) {
                for (const allow of pk.allowCredentials) {
                    const allowId = bufferToBase64Url(allow.id);
                    const found = vault.find(p => p.credentialId === allowId);
                    if (found) {
                        matched = found;
                        break;
                    }
                }
            }

            // Jika tidak ada allowCredentials spesifik (Discoverable Credential / 1-click Passkey)
            if (!matched) {
                const currentProfile = window.__FUSION_ACTIVE_PROFILE__ || window.__FUSION_ACTIVE_WALLET_NAME__ || null;
                if (currentProfile) {
                    matched = vault.find(p => (p.rpId === rpId || rpId.endsWith(p.rpId) || p.rpId.endsWith(rpId)) && p.profile === currentProfile);
                }
                if (!matched) {
                    matched = vault.find(p => p.rpId === rpId || rpId.endsWith(p.rpId) || p.rpId.endsWith(rpId));
                }
            }

            if (!matched) {
                console.warn("[Fusion Virtual Passkey] Tidak ditemukan Passkey tersimpan untuk:", rpId);
                if (originalGet) return originalGet(options);
                throw new Error("No passkey available for " + rpId);
            }

            // 1. Re-import Private Key JWK
            const privKey = await window.crypto.subtle.importKey(
                "jwk",
                matched.privateKeyJwk,
                { name: "ECDSA", namedCurve: "P-256" },
                false,
                ["sign"]
            );

            // 2. Bangun ClientDataJSON
            const challengeStr = pk.challenge ? bufferToBase64Url(pk.challenge) : '';
            const clientDataObj = {
                type: "webauthn.get",
                challenge: challengeStr,
                origin: window.location.origin,
                crossOrigin: false
            };
            const clientDataJsonBytes = stringToUtf8Bytes(JSON.stringify(clientDataObj));
            const clientDataHash = await sha256(clientDataJsonBytes);

            // 3. Bangun AuthenticatorData (Login Assertion)
            const rpIdHash = await sha256(stringToUtf8Bytes(rpId));
            const flags = new Uint8Array([0x1d]); // UP (0x01) + UV (0x04) + BE (0x08) + BS (0x10)
            const countBuf = new Uint8Array(4);
            const currentCount = (matched.signCount || 0) + 1;
            new DataView(countBuf.buffer).setUint32(0, currentCount, false);

            const authData = new Uint8Array(37);
            authData.set(rpIdHash, 0);
            authData.set(flags, 32);
            authData.set(countBuf, 33);

            // 4. Tanda Tangani (authData + sha256(clientDataJSON))
            const toSign = new Uint8Array(authData.length + clientDataHash.length);
            toSign.set(authData, 0);
            toSign.set(clientDataHash, authData.length);

            const rawSig = await window.crypto.subtle.sign(
                { name: "ECDSA", hash: "SHA-256" },
                privKey,
                toSign
            );
            const derSig = rawSigToDer(new Uint8Array(rawSig));

            // 5. Update sign count di vault
            matched.signCount = currentCount;
            saveToLocalVault(matched);
            showPasskeyToast(`🔑 Passkey Login (${rpId})`);

            const credIdBytes = base64UrlToUint8Array(matched.credentialId);
            const userHandleBytes = matched.userHandle ? base64UrlToUint8Array(matched.userHandle) : new Uint8Array(0);

            // 6. Kembalikan Objek PublicKeyCredential Standar W3C
            const assertion = {
                id: matched.credentialId,
                rawId: credIdBytes.buffer,
                type: "public-key",
                authenticatorAttachment: "platform",
                response: {
                    clientDataJSON: clientDataJsonBytes.buffer,
                    authenticatorData: authData.buffer,
                    signature: derSig,
                    userHandle: userHandleBytes.length > 0 ? userHandleBytes.buffer : null
                },
                getClientExtensionResults: () => ({}),
                toJSON: function() {
                    return {
                        id: matched.credentialId,
                        rawId: matched.credentialId,
                        response: {
                            clientDataJSON: bufferToBase64Url(clientDataJsonBytes.buffer),
                            authenticatorData: bufferToBase64Url(authData.buffer),
                            signature: bufferToBase64Url(derSig),
                            userHandle: userHandleBytes.length > 0 ? bufferToBase64Url(userHandleBytes.buffer) : null
                        },
                        authenticatorAttachment: "platform",
                        clientExtensionResults: {},
                        type: "public-key"
                    };
                }
            };

            if (typeof window.PublicKeyCredential !== 'undefined') {
                Object.setPrototypeOf(assertion, window.PublicKeyCredential.prototype);
            }
            if (typeof window.AuthenticatorAssertionResponse !== 'undefined') {
                Object.setPrototypeOf(assertion.response, window.AuthenticatorAssertionResponse.prototype);
            }

            return assertion;
        };
    }

    // --- 6. Platform Authenticator Capabilities Shim ---
    if (typeof window.PublicKeyCredential !== 'undefined') {
        window.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable = async function() {
            return true;
        };
        window.PublicKeyCredential.isConditionalMediationAvailable = async function() {
            return true;
        };
        if (typeof window.PublicKeyCredential.getClientCapabilities === 'function') {
            window.PublicKeyCredential.getClientCapabilities = async function() {
                return {
                    conditionalCreate: true,
                    conditionalGet: true,
                    hybridTransport: true,
                    passkeyPlatformAuthenticator: true,
                    userVerificationMethod: true
                };
            };
        }
    }

    console.log('[Fusion Virtual Passkey] Engine Aktif (Platform Authenticator Ready)');
})();


    window.__FUSION_ELEMENT_REFS__ = window.__FUSION_ELEMENT_REFS__ || new Map();

    function isVisible(el) {
        if (!el || !(el instanceof Element)) return false;
        if (el.nodeType !== Node.ELEMENT_NODE) return false;

        const tag = el.tagName.toLowerCase();
        if (['script', 'style', 'noscript', 'template', 'svg', 'path', 'meta', 'link'].includes(tag)) {
            return false;
        }

        if (el.getAttribute('aria-hidden') === 'true') return false;

        const style = window.getComputedStyle(el);
        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') {
            return false;
        }

        const rect = el.getBoundingClientRect();
        if (rect.width === 0 && rect.height === 0) {
            let hasVisibleChild = false;
            for (let i = 0; i < el.children.length; i++) {
                if (isVisible(el.children[i])) {
                    hasVisibleChild = true;
                    break;
                }
            }
            return hasVisibleChild;
        }

        return true;
    }

    function getElementRole(el) {
        const explicitRole = el.getAttribute('role');
        if (explicitRole) return explicitRole.toLowerCase();

        const tag = el.tagName.toLowerCase();
        if (tag === 'button') return 'button';
        if (tag === 'a' && el.hasAttribute('href')) return 'link';
        if (tag === 'input') {
            const type = (el.getAttribute('type') || 'text').toLowerCase();
            if (['button', 'submit', 'reset'].includes(type)) return 'button';
            if (type === 'checkbox') return 'checkbox';
            if (type === 'radio') return 'radio';
            if (['text', 'password', 'email', 'number', 'search', 'tel', 'url'].includes(type)) return 'textbox';
            return 'textbox';
        }
        if (tag === 'textarea') return 'textbox';
        if (tag === 'select') return 'combobox';
        if (/^h[1-6]$/.test(tag)) return 'heading';
        if (tag === 'ul' || tag === 'ol') return 'list';
        if (tag === 'li') return 'listitem';
        if (tag === 'table') return 'table';
        if (tag === 'tr') return 'row';
        if (tag === 'th') return 'columnheader';
        if (tag === 'td') return 'cell';
        if (tag === 'dialog') return 'dialog';
        if (tag === 'nav') return 'navigation';
        if (tag === 'main') return 'main';
        if (tag === 'header') return 'banner';
        if (tag === 'footer') return 'contentinfo';
        if (tag === 'p') return 'paragraph';

        const isClickable = el.hasAttribute('onclick') || 
            window.getComputedStyle(el).cursor === 'pointer' || 
            el.getAttribute('tabindex') === '0';
        if (isClickable && el.children.length === 0) return 'button';

        return null;
    }

    function getAccessibleName(el) {
        if (!el) return '';

        const ariaLabel = el.getAttribute('aria-label');
        if (ariaLabel && ariaLabel.trim()) return ariaLabel.trim();

        const labelledBy = el.getAttribute('aria-labelledby');
        if (labelledBy) {
            const target = document.getElementById(labelledBy);
            if (target && target.innerText.trim()) return target.innerText.trim();
        }

        const tag = el.tagName.toLowerCase();
        if (tag === 'input' || tag === 'textarea') {
            if (el.placeholder && el.placeholder.trim()) return el.placeholder.trim();
            if (el.id) {
                const label = document.querySelector('label[for="' + el.id + '"]');
                if (label && label.innerText.trim()) return label.innerText.trim();
            }
        }

        if (el.title && el.title.trim()) return el.title.trim();
        if (el.alt && el.alt.trim()) return el.alt.trim();

        if (tag === 'button' || tag === 'a' || /^h[1-6]$/.test(tag) || tag === 'li' || tag === 'p' || tag === 'span') {
            const clone = el.cloneNode(true);
            const childButtons = clone.querySelectorAll('button, a, input, select, textarea');
            childButtons.forEach(c => c.remove());
            const text = (clone.textContent || '').replace(/\s+/g, ' ').trim();
            if (text) return text.length > 80 ? text.slice(0, 77) + '...' : text;
        }

        return '';
    }

    function isInteractiveRole(role) {
        return ['button', 'link', 'textbox', 'checkbox', 'radio', 'combobox', 'tab', 'menuitem'].includes(role);
    }

    function buildAriaSnapshot(rootEl = document.body, maxDepth = 12) {
        window.__FUSION_ELEMENT_REFS__.clear();
        let refCounter = 1;
        const lines = [];

        function walk(node, depth) {
            if (depth > maxDepth || !isVisible(node)) return;

            const role = getElementRole(node);
            const name = getAccessibleName(node);
            const tag = node.tagName.toLowerCase();

            let refStr = '';
            const isInteractive = isInteractiveRole(role) || node.hasAttribute('onclick') || window.getComputedStyle(node).cursor === 'pointer';

            if (isInteractive) {
                const refId = 'e' + refCounter++;
                window.__FUSION_ELEMENT_REFS__.set(refId, node);
                node.setAttribute('data-agy-ref', refId);
                refStr = ' [ref=' + refId + ']';
            }

            let attrs = '';
            if (role === 'heading') {
                const match = tag.match(/^h([1-6])$/);
                const level = match ? match[1] : (node.getAttribute('aria-level') || '2');
                attrs += ' [level=' + level + ']';
            }
            if (node.disabled || node.getAttribute('aria-disabled') === 'true') {
                attrs += ' [disabled]';
            }
            if (node.checked || node.getAttribute('aria-checked') === 'true') {
                attrs += ' [checked]';
            }
            if (node.getAttribute('aria-expanded') === 'true') {
                attrs += ' [expanded]';
            }
            if (role === 'textbox' && node.value && node.value.trim()) {
                attrs += ' [value="' + node.value.replace(/"/g, '\\"') + '"]';
            }

            const indent = '  '.repeat(depth);

            if (role) {
                const labelStr = name ? ' "' + name.replace(/"/g, '\\"') + '"' : '';
                lines.push(indent + '- ' + role + labelStr + attrs + refStr);
            } else if (node.children.length === 0) {
                const directText = (node.textContent || '').replace(/\s+/g, ' ').trim();
                if (directText && directText.length > 1) {
                    const shortText = directText.length > 80 ? directText.slice(0, 77) + '...' : directText;
                    lines.push(indent + '- text: ' + shortText + refStr);
                }
            }

            for (let i = 0; i < node.children.length; i++) {
                walk(node.children[i], depth + 1);
            }

            // Piercing: Shadow DOM (Open)
            if (node.shadowRoot) {
                lines.push(indent + '  - shadow-root (open):');
                for (let s = 0; s < node.shadowRoot.children.length; s++) {
                    walk(node.shadowRoot.children[s], depth + 2);
                }
            }

            // Piercing: Iframe content document
            if (tag === 'iframe') {
                try {
                    const idoc = node.contentDocument || node.contentWindow?.document;
                    if (idoc && idoc.body) {
                        lines.push(indent + '  - iframe "' + (node.title || node.name || 'embedded') + '":');
                        walk(idoc.body, depth + 2);
                    }
                } catch (_) {
                    lines.push(indent + '  - iframe [cross-origin]');
                }
            }
        }

        walk(rootEl, 0);

        const pageTitle = document.title || 'Untitled';
        const pageUrl = window.location.href;
        return [
            'Page: "' + pageTitle.replace(/"/g, '\\"') + '" (' + pageUrl + ')',
            'Interactive Elements: ' + (refCounter - 1),
            '',
            lines.join('\n')
        ].join('\n');
    }

    function querySelectorDeep(selector, root = document) {
        try {
            const found = root.querySelector(selector);
            if (found) return found;
        } catch (_) {}
        try {
            const doc = root.ownerDocument || (root.nodeType === 9 ? root : document);
            const walker = doc.createTreeWalker(root, NodeFilter.SHOW_ELEMENT);
            let curr = walker.nextNode();
            while (curr) {
                if (curr.shadowRoot) {
                    const deepFound = querySelectorDeep(selector, curr.shadowRoot);
                    if (deepFound) return deepFound;
                }
                if (curr.tagName === 'IFRAME') {
                    try {
                        const idoc = curr.contentDocument || (curr.contentWindow && curr.contentWindow.document);
                        if (idoc) {
                            const deepFound = querySelectorDeep(selector, idoc);
                            if (deepFound) return deepFound;
                        }
                    } catch (_) {}
                }
                curr = walker.nextNode();
            }
        } catch (_) {}
        return null;
    }

    function resolveElement(target) {
        if (!target) return null;
        const str = String(target).trim();
        
        const cleanRef = str.startsWith('e') ? str : ('e' + str);
        if (window.__FUSION_ELEMENT_REFS__.has(cleanRef)) {
            const el = window.__FUSION_ELEMENT_REFS__.get(cleanRef);
            if (el && (document.contains(el) || el.ownerDocument.contains(el))) return el;
        }

        const byAttr = querySelectorDeep('[data-agy-ref="' + cleanRef + '"]') || querySelectorDeep('[data-agy-ref="' + str + '"]');
        if (byAttr) return byAttr;

        try {
            const bySelector = querySelectorDeep(str);
            if (bySelector) return bySelector;
        } catch (_) {}

        function collectAllCandidates(base = document) {
            let list = Array.from(base.querySelectorAll('button, a, input[type=button], input[type=submit], [role=button]'));
            const frames = base.querySelectorAll('iframe');
            for (const f of frames) {
                try {
                    const idoc = f.contentDocument || (f.contentWindow && f.contentWindow.document);
                    if (idoc) list = list.concat(collectAllCandidates(idoc));
                } catch (_) {}
            }
            return list;
        }
        const candidates = collectAllCandidates(document);
        // 1. Exact match
        for (const c of candidates) {
            const txt = (c.innerText || c.value || c.getAttribute('aria-label') || '').trim();
            if (txt.toLowerCase() === str.toLowerCase()) return c;
        }
        // 2. Exact word / prefix match (avoid matching "Export Claims to CSV" when asking for "Claim")
        for (const c of candidates) {
            const txt = (c.innerText || c.value || c.getAttribute('aria-label') || '').trim();
            const low = txt.toLowerCase();
            const slow = str.toLowerCase();
            // Don't match Export / CSV buttons if asking for Claim
            if (slow === 'claim' && (low.includes('export') || low.includes('csv') || low.includes('analysis'))) continue;
            if (low.startsWith(slow) || new RegExp('\\b' + slow + '\\b', 'i').test(low)) return c;
        }

        return null;
    }

    window.__FUSION_AGENT_ENGINE__ = {
        version: FUSION_ENGINE_VERSION,
        snapshot: (maxDepth = 12) => buildAriaSnapshot(document.body, maxDepth),
        
        click: (target) => {
            const el = resolveElement(target);
            if (!el) return { error: 'Element not found for target: ' + target };
            
            try {
                el.scrollIntoView({ behavior: 'instant', block: 'center', inline: 'center' });
            } catch (_) {}

            const prevOutline = el.style.outline;
            el.style.outline = '2px solid #00ff66';
            setTimeout(() => { el.style.outline = prevOutline; }, 500);

            const opts = { bubbles: true, cancelable: true, view: window };
            el.dispatchEvent(new PointerEvent('pointerdown', opts));
            el.dispatchEvent(new MouseEvent('mousedown', opts));
            if (typeof el.focus === 'function') el.focus();
            el.dispatchEvent(new PointerEvent('pointerup', opts));
            el.dispatchEvent(new MouseEvent('mouseup', opts));
            el.dispatchEvent(new MouseEvent('click', opts));
            if (typeof el.click === 'function') el.click();

            return {
                success: true,
                target: target,
                tag: el.tagName.toLowerCase(),
                role: getElementRole(el),
                text: getAccessibleName(el)
            };
        },

        fill: (target, value) => {
            const el = resolveElement(target);
            if (!el) return { error: 'Element not found for target: ' + target };

            try {
                el.scrollIntoView({ behavior: 'instant', block: 'center', inline: 'center' });
            } catch (_) {}

            if (typeof el.focus === 'function') el.focus();

            const proto = Object.getPrototypeOf(el);
            const setter = Object.getOwnPropertyDescriptor(proto, 'value')?.set || 
                           Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
            if (setter) {
                setter.call(el, value);
            } else {
                el.value = value;
            }

            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));

            return {
                success: true,
                target: target,
                value: value
            };
        },

        press: (key) => {
            const el = document.activeElement || document.body;
            const keyOpts = { key, code: key, bubbles: true, cancelable: true };
            el.dispatchEvent(new KeyboardEvent('keydown', keyOpts));
            el.dispatchEvent(new KeyboardEvent('keypress', keyOpts));
            el.dispatchEvent(new KeyboardEvent('keyup', keyOpts));
            return { success: true, key: key, activeTag: el.tagName.toLowerCase() };
        },

        hover: (target) => {
            const el = resolveElement(target);
            if (!el) return { error: 'Element not found for target: ' + target };
            try {
                el.scrollIntoView({ behavior: 'instant', block: 'center', inline: 'center' });
            } catch (_) {}
            const opts = { bubbles: true, cancelable: true, view: window };
            el.dispatchEvent(new PointerEvent('pointerover', opts));
            el.dispatchEvent(new MouseEvent('mouseover', opts));
            el.dispatchEvent(new MouseEvent('mouseenter', opts));
            return { success: true, target: target };
        },

        getCoordinates: (target) => {
            const el = resolveElement(target);
            if (!el) return { error: 'Element not found for target: ' + target };
            try {
                el.scrollIntoView({ behavior: 'instant', block: 'center', inline: 'center' });
            } catch (_) {}
            const rect = el.getBoundingClientRect();
            return {
                success: true,
                target: target,
                x: Math.round(rect.left + rect.width / 2),
                y: Math.round(rect.top + rect.height / 2),
                width: Math.round(rect.width),
                height: Math.round(rect.height)
            };
        },

        selectOption: (target, value) => {
            const el = resolveElement(target);
            if (!el) return { error: 'Element not found for target: ' + target };
            if (el.tagName.toLowerCase() !== 'select') return { error: 'Target is not a <select> element' };

            let matched = false;
            for (let i = 0; i < el.options.length; i++) {
                const opt = el.options[i];
                if (opt.value === value || opt.text.trim() === value) {
                    el.selectedIndex = i;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                el.value = value;
            }
            el.dispatchEvent(new Event('change', { bubbles: true }));
            return { success: true, target: target, selectedValue: el.value };
        },

        waitFor: async (query, timeoutMs = 5000) => {
            const startTime = Date.now();
            while (Date.now() - startTime < timeoutMs) {
                const el = resolveElement(query);
                if (el && isVisible(el)) return { success: true, found: true };
                if (document.body.innerText.includes(query)) return { success: true, foundText: true };
                await new Promise(r => setTimeout(r, 200));
            }
            return { error: 'Timeout waiting for: ' + query };
        },

        exportStorage: async () => {
            try {
                const ls = {};
                for (let i = 0; i < localStorage.length; i++) {
                    const k = localStorage.key(i);
                    ls[k] = localStorage.getItem(k);
                }
                const ss = {};
                for (let i = 0; i < sessionStorage.length; i++) {
                    const k = sessionStorage.key(i);
                    ss[k] = sessionStorage.getItem(k);
                }
                return {
                    url: window.location.href,
                    origin: window.location.origin,
                    localStorage: ls,
                    sessionStorage: ss,
                    timestamp: Date.now()
                };
            } catch (err) {
                return { error: err.message };
            }
        },

        importStorage: async (state) => {
            if (!state) return { error: 'Invalid state' };
            try {
                let lsCount = 0;
                let ssCount = 0;
                if (state.localStorage && typeof state.localStorage === 'object') {
                    for (const [k, v] of Object.entries(state.localStorage)) {
                        try { localStorage.setItem(k, v); lsCount++; } catch (_) {}
                    }
                }
                if (state.sessionStorage && typeof state.sessionStorage === 'object') {
                    for (const [k, v] of Object.entries(state.sessionStorage)) {
                        try { sessionStorage.setItem(k, v); ssCount++; } catch (_) {}
                    }
                }
                return { success: true, localStorageRestored: lsCount, sessionStorageRestored: ssCount };
            } catch (err) {
                return { error: err.message };
            }
        },

        watchDom: (options) => {
            return new Promise((resolve) => {
                const opts = typeof options === 'string' ? { text: options } : (options || {});
                const { text, selector, timeoutMs = 15000 } = opts;
                const startTime = Date.now();

                if (text && document.body.innerText.includes(text)) {
                    return resolve({ success: true, matched: 'text', value: text, durationMs: 0 });
                }
                if (selector) {
                    const found = resolveElement(selector);
                    if (found && isVisible(found)) {
                        return resolve({ success: true, matched: 'selector', value: selector, durationMs: 0 });
                    }
                }

                const observer = new MutationObserver(() => {
                    if (text && document.body.innerText.includes(text)) {
                        observer.disconnect();
                        clearTimeout(timer);
                        return resolve({ success: true, matched: 'text', value: text, durationMs: Date.now() - startTime });
                    }
                    if (selector) {
                        const found = resolveElement(selector);
                        if (found && isVisible(found)) {
                            observer.disconnect();
                            clearTimeout(timer);
                            return resolve({ success: true, matched: 'selector', value: selector, durationMs: Date.now() - startTime });
                        }
                    }
                });

                observer.observe(document.body, {
                    childList: true,
                    subtree: true,
                    characterData: true
                });

                const timer = setTimeout(() => {
                    observer.disconnect();
                    resolve({ error: 'Timeout waiting for DOM event', options: opts, durationMs: Date.now() - startTime });
                }, timeoutMs);
            });
        },

        evaluate: (code) => {
            try {
                const fn = new Function('return (' + code + ')');
                const res = fn();
                return { success: true, result: res };
            } catch (err) {
                return { error: err.message };
            }
        },

        scrollNatural: (deltaX, deltaY, durationMs = 300) => {
            return new Promise((resolve) => {
                const startX = window.scrollX;
                const startY = window.scrollY;
                if (document.hidden) {
                    window.scrollBy(deltaX, deltaY);
                    return resolve({ success: true, scrolledX: deltaX, scrolledY: deltaY, method: 'direct_hidden' });
                }
                const startTime = performance.now();
                const timer = setTimeout(() => {
                    window.scrollTo(startX + deltaX, startY + deltaY);
                    resolve({ success: true, scrolledX: deltaX, scrolledY: deltaY, method: 'timeout_fallback' });
                }, durationMs + 100);

                function step(now) {
                    const progress = Math.min((now - startTime) / durationMs, 1);
                    const ease = 1 - Math.pow(1 - progress, 3);
                    window.scrollTo(startX + deltaX * ease, startY + deltaY * ease);
                    if (progress < 1) {
                        requestAnimationFrame(step);
                    } else {
                        clearTimeout(timer);
                        resolve({ success: true, scrolledX: deltaX, scrolledY: deltaY, method: 'natural' });
                    }
                }
                requestAnimationFrame(step);
            });
        },

        dragAndDrop: (sourceTarget, destTarget) => {
            const srcEl = resolveElement(sourceTarget);
            const dstEl = resolveElement(destTarget);
            if (!srcEl) return { error: 'Source element not found: ' + sourceTarget };
            if (!dstEl) return { error: 'Destination element not found: ' + destTarget };

            try {
                const dataTransfer = new DataTransfer();
                const fire = (el, type, related = null) => {
                    const ev = new DragEvent(type, {
                        bubbles: true,
                        cancelable: true,
                        dataTransfer: dataTransfer,
                        relatedTarget: related
                    });
                    el.dispatchEvent(ev);
                };
                fire(srcEl, 'dragstart');
                fire(dstEl, 'dragenter', srcEl);
                fire(dstEl, 'dragover', srcEl);
                fire(dstEl, 'drop', srcEl);
                fire(srcEl, 'dragend', dstEl);
                return { success: true, source: sourceTarget, destination: destTarget };
            } catch (err) {
                return { error: 'Drag and drop failed: ' + err.message };
            }
        },

        hideKeyboard: () => {
            if (window.GhostBridge && typeof window.GhostBridge.hideKeyboard === 'function') {
                window.GhostBridge.hideKeyboard();
                return { success: true };
            }
            if (document.activeElement && typeof document.activeElement.blur === 'function') {
                document.activeElement.blur();
            }
            return { success: true, blurred: true };
        },

        setWebRtcStealth: (enable) => {
            window.__FUSION_WEBRTC_STEALTH__ = !!enable;
            return { success: true, webrtcStealth: window.__FUSION_WEBRTC_STEALTH__ };
        }
    };

    // ==========================================
    // 👑 FUSION GOD-MODE: DYNAMIC TOOL REGISTRY
    // ==========================================
    window.__FUSION_DYNAMIC_TOOLS__ = window.__FUSION_DYNAMIC_TOOLS__ || new Map();

    window.registerFusionTool = function(name, toolDef) {
        if (!name || typeof toolDef !== 'object') return false;
        window.__FUSION_DYNAMIC_TOOLS__.set(name, {
            name: name,
            description: toolDef.description || ('Dynamic tool: ' + name),
            inputSchema: toolDef.parameters || toolDef.inputSchema || { type: 'object', properties: {} },
            execute: toolDef.execute || toolDef.handler || (async () => ({ error: 'No handler defined' }))
        });
        console.log('[Fusion God-Mode] Registered dynamic tool: ' + name);
        return true;
    };

    window.unregisterFusionTool = function(name) {
        return window.__FUSION_DYNAMIC_TOOLS__.delete(name);
    };

    window.listFusionTools = function() {
        const list = [];
        for (const [name, tool] of window.__FUSION_DYNAMIC_TOOLS__.entries()) {
            list.push({
                name: tool.name,
                description: tool.description,
                inputSchema: tool.inputSchema
            });
        }
        return list;
    };

    window.callFusionTool = async function(name, args) {
        const tool = window.__FUSION_DYNAMIC_TOOLS__.get(name);
        if (!tool) return { error: 'Dynamic tool not found: ' + name };
        try {
            const res = await tool.execute(args || {});
            return { success: true, result: res };
        } catch (err) {
            return { error: 'Tool execution error: ' + err.message };
        }
    };

    // ==========================================
    // ⚡ NATIVE GOD-MODE BRIDGE SHORTCUTS
    // ==========================================
    const _legacyGhostBridge = {
        ipc: (cmd) => window.GhostBridge && window.GhostBridge.dispatchIpc ? window.GhostBridge.dispatchIpc(cmd) : false,
        touch: (x, y) => window.GhostBridge && window.GhostBridge.dispatchTouch ? window.GhostBridge.dispatchTouch(x, y) : false,
        swipe: (x1, y1, x2, y2, dur = 300) => window.GhostBridge && window.GhostBridge.dispatchSwipe ? window.GhostBridge.dispatchSwipe(x1, y1, x2, y2, dur) : false,
        longPress: (x, y, dur = 1000) => window.GhostBridge && window.GhostBridge.dispatchLongPress ? window.GhostBridge.dispatchLongPress(x, y, dur) : false,
        uploadFile: (filePath) => window.GhostBridge && window.GhostBridge.supplyUploadFile ? window.GhostBridge.supplyUploadFile(filePath) : false,
        readFile: (path) => window.GhostBridge && window.GhostBridge.readLocalFile ? window.GhostBridge.readLocalFile(path) : '',
        writeFile: (path, data) => window.GhostBridge && window.GhostBridge.writeLocalFile ? window.GhostBridge.writeLocalFile(path, data) : false,
        newTab: (url) => window.GhostBridge && window.GhostBridge.openNewTab ? window.GhostBridge.openNewTab(url) : null,
        closeTab: () => window.GhostBridge && window.GhostBridge.closeCurrentTab ? window.GhostBridge.closeCurrentTab() : null,
        switchTab: (idx) => window.GhostBridge && window.GhostBridge.switchTab ? window.GhostBridge.switchTab(idx) : null,
        toast: (msg) => window.GhostBridge && window.GhostBridge.toast ? window.GhostBridge.toast(msg) : null,
        hideKeyboard: () => window.GhostBridge && window.GhostBridge.hideKeyboard ? window.GhostBridge.hideKeyboard() : null,
        getClipboard: () => window.GhostBridge && window.GhostBridge.getClipboard ? window.GhostBridge.getClipboard() : '',
        setClipboard: (txt) => window.GhostBridge && window.GhostBridge.copyToClipboard ? window.GhostBridge.copyToClipboard(txt) : null,
        wakeLock: (on) => {
            if (!window.GhostBridge) return;
            if (on) window.GhostBridge.acquireWakeLock();
            else window.GhostBridge.releaseWakeLock();
        },
        nativeFetch: (url, opts = {}) => {
            if (!window.GhostBridge || !window.GhostBridge.nativeFetch) return Promise.reject(new Error('nativeFetch not available'));
            const method = opts.method || 'GET';
            const headers = opts.headers ? JSON.stringify(opts.headers) : '{}';
            const body = typeof opts.body === 'object' ? JSON.stringify(opts.body) : (opts.body || '');
            try {
                const raw = window.GhostBridge.nativeFetch(url, method, headers, body);
                return Promise.resolve(JSON.parse(raw));
            } catch (e) {
                return Promise.reject(e);
            }
        },
        getCookies: (url = '') => window.GhostBridge && window.GhostBridge.getFullCookies ? window.GhostBridge.getFullCookies(url) : '',
        setCookie: (url, val) => window.GhostBridge && window.GhostBridge.setFullCookie ? window.GhostBridge.setFullCookie(url, val) : false,
        clearAllCookies: () => window.GhostBridge && window.GhostBridge.clearAllCookies ? window.GhostBridge.clearAllCookies() : false,
        execShell: (cmd) => {
            if (!window.GhostBridge || !window.GhostBridge.execShell) return { success: false, error: 'execShell not available' };
            try {
                return JSON.parse(window.GhostBridge.execShell(cmd));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        execShellAsync: (cmd) => {
            return new Promise((resolve) => {
                setTimeout(() => {
                    resolve(window.__FUSION_NATIVE__.execShell(cmd));
                }, 0);
            });
        },
        callJava: (target, method, ...args) => {
            if (!window.GhostBridge || !window.GhostBridge.callJava) return { success: false, error: 'callJava not available' };
            try {
                return JSON.parse(window.GhostBridge.callJava(target, method, JSON.stringify(args)));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        listJavaMethods: (target) => {
            if (!window.GhostBridge || !window.GhostBridge.listJavaMethods) return [];
            try {
                return JSON.parse(window.GhostBridge.listJavaMethods(target));
            } catch (e) {
                return [];
            }
        },
        askAI: async (prompt, options = {}) => {
            if (!window.GhostBridge || !window.GhostBridge.askAI) throw new Error('askAI not available');
            const raw = window.GhostBridge.askAI(prompt, JSON.stringify(options));
            const res = JSON.parse(raw);
            if (res.success) return res.result;
            throw new Error(res.error || 'askAI failed');
        },
        dbQuery: async (sql, params = [], dbPath = '') => {
            if (!window.GhostBridge || !window.GhostBridge.dbQuery) throw new Error('dbQuery not available');
            const raw = window.GhostBridge.dbQuery(sql, JSON.stringify(params), dbPath);
            const res = JSON.parse(raw);
            if (res.success) return res.rows;
            throw new Error(res.error || 'dbQuery failed');
        },
        dbExec: async (sql, params = [], dbPath = '') => {
            if (!window.GhostBridge || !window.GhostBridge.dbExec) throw new Error('dbExec not available');
            const raw = window.GhostBridge.dbExec(sql, JSON.stringify(params), dbPath);
            const res = JSON.parse(raw);
            if (res.success) return true;
            throw new Error(res.error || 'dbExec failed');
        },
        mockNetwork: (pattern, body, statusCode = 200, mimeType = 'application/json', headers = {}) => {
            if (!window.GhostBridge || !window.GhostBridge.mockNetwork) return false;
            const bodyStr = typeof body === 'object' ? JSON.stringify(body) : String(body || '');
            const hdrs = typeof headers === 'object' ? JSON.stringify(headers) : '{}';
            return window.GhostBridge.mockNetwork(pattern, bodyStr, statusCode, mimeType, hdrs);
        },
        blockNetwork: (pattern) => {
            return window.GhostBridge && window.GhostBridge.blockNetwork ? window.GhostBridge.blockNetwork(pattern) : false;
        },
        clearNetworkRules: () => {
            return window.GhostBridge && window.GhostBridge.clearNetworkRules ? window.GhostBridge.clearNetworkRules() : false;
        },
        broadcastTab: (channel, message) => {
            if (!window.GhostBridge || !window.GhostBridge.broadcastTab) return false;
            const str = typeof message === 'object' ? JSON.stringify(message) : String(message || '');
            return window.GhostBridge.broadcastTab(channel, str);
        },
        notify: (title, message) => {
            return window.GhostBridge && window.GhostBridge.notify ? window.GhostBridge.notify(title, message) : false;
        },
        reloadUserscripts: () => {
            return window.GhostBridge && window.GhostBridge.reloadUserscripts ? window.GhostBridge.reloadUserscripts() : false;
        }
    };

    if (window.__FUSION_NATIVE__ && typeof window.__FUSION_NATIVE__.reflect === 'function') {
        for (const k in _legacyGhostBridge) {
            if (typeof window.__FUSION_NATIVE__[k] === 'undefined') {
                window.__FUSION_NATIVE__[k] = _legacyGhostBridge[k];
            }
        }
    } else {
        window.__FUSION_NATIVE__ = Object.assign({}, _legacyGhostBridge);
    }

    // ==========================================
    // 📬 CLOUD MAIL & OTP IN-PAGE BRIDGE
    // ==========================================
    window.cloudMail = {
        newEmail: async function(options = {}) {
            try {
                let silent = true;
                let random = true;
                let customPrefix = '';
                let domain = '';
                let exclude = [];

                if (typeof options === 'boolean') {
                    silent = options;
                    customPrefix = arguments[1] || '';
                } else if (typeof options === 'object') {
                    if (options.silent !== undefined) silent = Boolean(options.silent);
                    if (options.random !== undefined) random = Boolean(options.random);
                    if (options.customPrefix) customPrefix = String(options.customPrefix);
                    if (options.domain) domain = String(options.domain);
                    if (Array.isArray(options.exclude)) exclude = options.exclude;
                }

                const prefixArg = customPrefix ? ` "${customPrefix}"` : '';
                const silentArg = silent ? ' -s' : '';
                const randomArg = random ? ' --random' : '';
                const domainArg = domain ? ` -d "${domain}"` : '';
                const siteArg = ` --site "${window.location.hostname}"`;
                const excludeArg = (exclude.length > 0) ? ` --exclude "${exclude.join(',')}"` : '';
                const cmd = `mail new -r${silentArg}${randomArg}${domainArg}${siteArg}${excludeArg}${prefixArg}`;
                const res = window.__FUSION_NATIVE__.execShell(cmd);
                return res ? res.trim() : '';
            } catch (e) {
                console.error('[CloudMail] newEmail error:', e);
                return '';
            }
        },
        waitOtp: async function(email = '', timeoutSec = 60) {
            try {
                const emailArg = email ? ` "${email}"` : '';
                const res = window.__FUSION_NATIVE__.execShell(`mail wait-otp${emailArg} -t ${timeoutSec} -r`);
                return res ? res.trim() : '';
            } catch (e) {
                console.error('[CloudMail] waitOtp error:', e);
                return '';
            }
        },
        waitLink: async function(email = '', timeoutSec = 60) {
            try {
                const emailArg = email ? ` "${email}"` : '';
                const res = window.__FUSION_NATIVE__.execShell(`mail wait-link${emailArg} -t ${timeoutSec} -r`);
                return res ? res.trim() : '';
            } catch (e) {
                console.error('[CloudMail] waitLink error:', e);
                return '';
            }
        },
        getInbox: async function(email = '') {
            try {
                const emailArg = email ? ` "${email}"` : '';
                const res = window.__FUSION_NATIVE__.execShell(`mail inbox${emailArg}`);
                return res ? res.trim() : '';
            } catch (e) {
                return '';
            }
        }
    };

    // ==========================================
    // ⚡ IN-PAGE DIRECT CDP WEBSOCKET CLIENT
    // ==========================================
    let cdpWs = null;
    let cdpMsgId = 1;
    const cdpPending = new Map();

    async function getCdpWsUrl() {
        try {
            const res = await window.__FUSION_NATIVE__.nativeFetch('http://127.0.0.1:9222/json');
            if (res && res.data) {
                const list = JSON.parse(res.data);
                const curUrl = window.location.href;
                const page = list.find(t => t.type === 'page' && t.url === curUrl && t.webSocketDebuggerUrl)
                          || list.find(t => t.type === 'page' && !t.url.includes('home.html') && t.webSocketDebuggerUrl)
                          || list[0];
                if (page && page.webSocketDebuggerUrl) return page.webSocketDebuggerUrl;
            }
        } catch (_) {}
        return null;
    }

    async function ensureCdpConnected() {
        if (cdpWs && cdpWs.readyState === WebSocket.OPEN) return true;
        const wsUrl = await getCdpWsUrl();
        if (!wsUrl) return false;
        return new Promise((resolve) => {
            try {
                cdpWs = new WebSocket(wsUrl);
                cdpWs.onopen = () => resolve(true);
                cdpWs.onmessage = (event) => {
                    try {
                        const msg = JSON.parse(event.data);
                        if (msg.id && cdpPending.has(msg.id)) {
                            const { resolve: resPromise, reject: rejPromise } = cdpPending.get(msg.id);
                            cdpPending.delete(msg.id);
                            if (msg.error) rejPromise(new Error(msg.error.message || JSON.stringify(msg.error)));
                            else resPromise(msg.result);
                        }
                    } catch (_) {}
                };
                cdpWs.onerror = () => resolve(false);
                cdpWs.onclose = () => { cdpWs = null; };
            } catch (_) {
                resolve(false);
            }
        });
    }

    window.__FUSION_NATIVE__.cdpSend = async function(method, params = {}) {
        const ok = await ensureCdpConnected();
        if (!ok || !cdpWs) throw new Error('Cannot connect to CDP on 127.0.0.1:9222');
        const id = ++cdpMsgId;
        return new Promise((resolve, reject) => {
            cdpPending.set(id, { resolve, reject });
            cdpWs.send(JSON.stringify({ id, method, params }));
            setTimeout(() => {
                if (cdpPending.has(id)) {
                    cdpPending.delete(id);
                    reject(new Error('CDP timeout: ' + method));
                }
            }, 8000);
        });
    };

    // ==========================================
    // ⚡ CROSS-TAB EVENT BUS DISPATCHER
    // ==========================================
    if (!window.__FUSION_TAB_BUS__) {
        window.__FUSION_TAB_BUS__ = new Map();
        window.__FUSION_DISPATCH_TAB_MSG__ = function(channel, msg) {
            const listeners = window.__FUSION_TAB_BUS__.get(channel);
            if (listeners) {
                let data = msg;
                try { data = JSON.parse(msg); } catch (_) {}
                listeners.forEach(cb => {
                    try { cb(data); } catch (err) { console.error(err); }
                });
            }
        };
    }

    // ==========================================
    // ⚡ TERMINAL ↔ WEBVIEW EVENT STREAM (Termux Bus)
    // ==========================================
    if (!window.__FUSION_TERMUX_EVENTS__) {
        window.__FUSION_TERMUX_EVENTS__ = new Map();
        window.__FUSION_DISPATCH_TERMUX_EVENT__ = function(eventName, data) {
            let parsed = data;
            try { parsed = JSON.parse(data); } catch (_) {}
            const listeners = window.__FUSION_TERMUX_EVENTS__.get(eventName);
            if (listeners) {
                listeners.forEach(cb => {
                    try { cb(parsed); } catch (err) { console.error(err); }
                });
            }
            try { window.dispatchEvent(new CustomEvent('termux_' + eventName, { detail: parsed })); } catch (_) {}
        };
    }

    // ==========================================
    // ⚡ GLOBAL SHORTCUTS FOR DEV & USERSCRIPTS
    // ==========================================
    window.callJava = (target, method, ...args) => window.__FUSION_NATIVE__.callJava(target, method, ...args);
    window.listJavaMethods = (target) => window.__FUSION_NATIVE__.listJavaMethods(target);
    window.execShell = (cmd) => window.__FUSION_NATIVE__.execShell(cmd);
    window.execShellAsync = (cmd) => window.__FUSION_NATIVE__.execShellAsync(cmd);
    window.nativeFetch = (url, opts) => window.__FUSION_NATIVE__.nativeFetch(url, opts);
    window.askAI = (prompt, opts) => window.__FUSION_NATIVE__.askAI(prompt, opts);
    window.db = {
        query: (sql, params, dbPath) => window.__FUSION_NATIVE__.dbQuery(sql, params, dbPath),
        exec: (sql, params, dbPath) => window.__FUSION_NATIVE__.dbExec(sql, params, dbPath)
    };
    window.mockNetwork = (pat, body, code, mime, hdrs) => window.__FUSION_NATIVE__.mockNetwork(pat, body, code, mime, hdrs);
    window.blockNetwork = (pat) => window.__FUSION_NATIVE__.blockNetwork(pat);
    window.clearNetworkRules = () => window.__FUSION_NATIVE__.clearNetworkRules();
    window.broadcastTab = (ch, msg) => window.__FUSION_NATIVE__.broadcastTab(ch, msg);
    window.onTabMessage = (ch, cb) => {
        if (!window.__FUSION_TAB_BUS__.has(ch)) window.__FUSION_TAB_BUS__.set(ch, new Set());
        window.__FUSION_TAB_BUS__.get(ch).add(cb);
        return () => window.__FUSION_TAB_BUS__.get(ch).delete(cb);
    };
    window.onTermuxEvent = (eventName, callback) => {
        if (!window.__FUSION_TERMUX_EVENTS__.has(eventName)) window.__FUSION_TERMUX_EVENTS__.set(eventName, new Set());
        window.__FUSION_TERMUX_EVENTS__.get(eventName).add(callback);
        return () => window.__FUSION_TERMUX_EVENTS__.get(eventName).delete(callback);
    };
    window.emitTermux = (eventName, payload) => {
        const str = typeof payload === 'object' ? JSON.stringify(payload) : String(payload || '');
        if (window.GhostBridge && window.GhostBridge.emitTermuxEvent) {
            return window.GhostBridge.emitTermuxEvent(eventName, str);
        }
        return false;
    };
    window.notify = (title, msg) => window.__FUSION_NATIVE__.notify(title, msg);
    window.setUserAgent = (ua) => window.GhostBridge && window.GhostBridge.setUserAgent ? window.GhostBridge.setUserAgent(ua) : false;
    window.getUserAgent = () => window.GhostBridge && window.GhostBridge.getUserAgent ? window.GhostBridge.getUserAgent() : navigator.userAgent;
    window.cdp = (method, params) => window.__FUSION_NATIVE__.cdpSend(method, params);
    window.cdpSend = (method, params) => window.__FUSION_NATIVE__.cdpSend(method, params);

    // Master Tab Manager
    window.tabs = {
        list: () => {
            if (!window.GhostBridge || !window.GhostBridge.getTabsJson) return [];
            try { return JSON.parse(window.GhostBridge.getTabsJson()); } catch (_) { return []; }
        },
        open: (url) => window.__FUSION_NATIVE__.newTab(url),
        close: (index) => {
            if (typeof index === 'number' && window.GhostBridge && window.GhostBridge.closeTab) {
                return window.GhostBridge.closeTab(index);
            }
            return window.__FUSION_NATIVE__.closeTab();
        },
        switch: (index) => window.__FUSION_NATIVE__.switchTab(index),
        eval: (index, code) => {
            if (window.GhostBridge && window.GhostBridge.evalTab) {
                return window.GhostBridge.evalTab(index, code);
            }
            return false;
        }
    };

    // Semantic ARIA Superpowers (Identical to AI Agent)
    window.aria = {
        snapshot: (maxDepth = 12) => {
            const builder = new AriaSnapshotBuilder(maxDepth);
            return builder.build();
        },
        click: async (target) => {
            const el = AriaSnapshotBuilder.resolveTarget(target);
            if (!el) throw new Error('Target not found: ' + target);
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.click();
            return true;
        },
        clickHuman: async (target) => {
            const el = AriaSnapshotBuilder.resolveTarget(target);
            if (!el) throw new Error('Target not found: ' + target);
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            const rect = el.getBoundingClientRect();
            const x = rect.left + rect.width / 2;
            const y = rect.top + rect.height / 2;
            return window.__FUSION_NATIVE__.touch(x, y);
        },
        fill: async (target, value) => {
            const el = AriaSnapshotBuilder.resolveTarget(target);
            if (!el) throw new Error('Target not found: ' + target);
            el.focus();
            el.value = value;
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
            return true;
        }
    };

    // Linux & Termux System Control Bridge
    window.termux = {
        sh: (cmd) => window.__FUSION_NATIVE__.execShell(cmd),
        shAsync: (cmd) => window.__FUSION_NATIVE__.execShellAsync(cmd),
        ipc: (cmd) => window.__FUSION_NATIVE__.ipc(cmd),
        toast: (msg) => window.__FUSION_NATIVE__.toast(msg),
        clipboardGet: () => window.__FUSION_NATIVE__.getClipboard(),
        clipboardSet: (txt) => window.__FUSION_NATIVE__.setClipboard(txt),
        readFile: (path) => window.__FUSION_NATIVE__.readFile(path),
        writeFile: (path, data) => window.__FUSION_NATIVE__.writeFile(path, data)
    };

    // ==========================================
    // ⚡ DEEP SHADOW-DOM PIERCING & MUTATION WAIT
    // ==========================================
    window.shadowQueryAll = function(selector, root = document) {
        const results = Array.from(root.querySelectorAll ? root.querySelectorAll(selector) : []);
        const pushNested = (target) => {
            window.shadowQueryAll(selector, target).forEach(el => {
                if (!results.includes(el)) results.push(el);
            });
        };
        if (root.shadowRoot) pushNested(root.shadowRoot);
        const all = root.querySelectorAll ? root.querySelectorAll('*') : [];
        for (const el of all) {
            if (el.shadowRoot) pushNested(el.shadowRoot);
        }
        return results;
    };

    window.shadowQuery = function(selector, root = document) {
        const all = window.shadowQueryAll(selector, root);
        return all.length > 0 ? all[0] : null;
    };

    window.waitFor = function(target, timeoutMs = 10000) {
        return new Promise((resolve, reject) => {
            const startTime = Date.now();
            const check = () => {
                let found = null;
                if (typeof target === 'function') {
                    found = target();
                } else if (typeof target === 'string') {
                    found = window.shadowQuery(target) || AriaSnapshotBuilder.resolveTarget(target);
                }
                if (found) return resolve(found);
                if (Date.now() - startTime >= timeoutMs) {
                    return reject(new Error('waitFor timeout: ' + target));
                }
                setTimeout(check, 100);
            };
            check();
        });
    };

    // ==========================================
    // ⚡ CLOUDFLARE TURNSTILE HARDWARE AUTO-SOLVER
    // ==========================================
    window.autoSolveTurnstile = async function(timeoutMs = 15000) {
        console.log('[Stealth] Scanning for Cloudflare Turnstile challenge widget...');
        const startTime = Date.now();
        while (Date.now() - startTime < timeoutMs) {
            const iframe = document.querySelector('iframe[src*="challenges.cloudflare.com"], iframe[src*="turnstile"]')
                        || window.shadowQuery('iframe[src*="challenges.cloudflare.com"]');
            if (iframe) {
                const rect = iframe.getBoundingClientRect();
                if (rect.width > 0 && rect.height > 0) {
                    const clickX = Math.round(rect.left + 32);
                    const clickY = Math.round(rect.top + rect.height / 2);
                    console.log(`[Stealth] Turnstile located at (${clickX}, ${clickY}). Dispatching authentic Android hardware MotionEvent...`);
                    window.__FUSION_NATIVE__.touch(clickX, clickY);
                    return { success: true, x: clickX, y: clickY };
                }
            }
            await new Promise(r => setTimeout(r, 600));
        }
        return { success: false, error: 'Turnstile widget not found within timeout' };
    };

    // ==========================================
    // ⚡ CLEARANCE COOKIE EXPORTER (Curl/Python Bridge)
    // ==========================================
    window.exportClearance = function(outPath = '/tmp/cf_clearance.json') {
        const fullCookie = window.__FUSION_NATIVE__.getCookies(window.location.href);
        const userAgent = navigator.userAgent;
        const payload = {
            url: window.location.href,
            domain: window.location.hostname,
            userAgent: userAgent,
            cookie: fullCookie,
            timestamp: Date.now()
        };
        window.__FUSION_NATIVE__.writeFile(outPath, JSON.stringify(payload, null, 2));
        console.log('[Clearance] Exported cookies & user-agent to ' + outPath);
        return payload;
    };

    // ==========================================
    // ⚡ EIP-6963 MULTI-WALLET DISCOVERY INJECTOR
    // ==========================================
    window.announceEIP6963 = function(provider = window.ethereum) {
        if (!provider) return false;
        const info = Object.freeze({
            uuid: '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d',
            name: 'Ghost Web3 Wallet',
            icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMDBmZmNjIiBzdHJva2Utd2lkdGg9IjIiPjxwYXRoIGQ9Ik05IDE4di02SDRsNy03djZoNWwtNyA3eiIvPjwvc3ZnPg==',
            rdns: 'io.ghostwallet'
        });
        const announce = () => {
            window.dispatchEvent(new CustomEvent('eip6963:announceProvider', {
                detail: Object.freeze({ info, provider })
            }));
        };
        window.addEventListener('eip6963:requestProvider', announce);
        announce();
        return true;
    };
    try { if (window.ethereum) window.announceEIP6963(); } catch (_) {}

    // ==========================================
    // ⚡ 1. SMART GATE & NETWORK IN-FLIGHT TRACKER
    // ==========================================
    window.__FUSION_INFLIGHT_COUNT__ = 0;
    const origFetch = window.fetch;
    if (origFetch) {
        window.fetch = async function(...args) {
            window.__FUSION_INFLIGHT_COUNT__++;
            try {
                return await origFetch.apply(this, args);
            } finally {
                window.__FUSION_INFLIGHT_COUNT__ = Math.max(0, window.__FUSION_INFLIGHT_COUNT__ - 1);
            }
        };
    }
    const origXhrOpen = XMLHttpRequest.prototype.open;
    const origXhrSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function(...args) {
        this.__fusion_tracked__ = true;
        return origXhrOpen.apply(this, args);
    };
    XMLHttpRequest.prototype.send = function(...args) {
        if (this.__fusion_tracked__) {
            window.__FUSION_INFLIGHT_COUNT__++;
            const onEnd = () => {
                if (this.__fusion_tracked__) {
                    this.__fusion_tracked__ = false;
                    window.__FUSION_INFLIGHT_COUNT__ = Math.max(0, window.__FUSION_INFLIGHT_COUNT__ - 1);
                }
            };
            this.addEventListener('loadend', onEnd);
            this.addEventListener('error', onEnd);
            this.addEventListener('abort', onEnd);
        }
        return origXhrSend.apply(this, args);
    };

    window.waitForReady = async function(selectorOrPredicate, options = {}) {
        const {
            timeoutMs = 5000,
            checkVisible = true,
            checkEnabled = true,
            waitForIdle = true,
            idleCooldownMs = 150
        } = options;

        const start = Date.now();
        const checkEl = () => {
            let el = null;
            if (typeof selectorOrPredicate === 'function') {
                return selectorOrPredicate();
            } else if (typeof selectorOrPredicate === 'string') {
                el = window.shadowQuery ? window.shadowQuery(selectorOrPredicate) : document.querySelector(selectorOrPredicate);
                if (!el && selectorOrPredicate.startsWith('//')) {
                    const res = document.evaluate(selectorOrPredicate, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                    el = res.singleNodeValue;
                }
            }
            if (!el) return null;
            if (checkVisible) {
                const style = window.getComputedStyle(el);
                if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return null;
                const rect = el.getBoundingClientRect();
                if (rect.width === 0 && rect.height === 0) return null;
            }
            if (checkEnabled) {
                if (el.disabled || el.getAttribute('aria-disabled') === 'true') return null;
            }
            return el;
        };

        return new Promise((resolve, reject) => {
            const firstTry = checkEl();
            if (firstTry && (!waitForIdle || window.__FUSION_INFLIGHT_COUNT__ === 0)) {
                return resolve(firstTry);
            }

            let idleStart = (window.__FUSION_INFLIGHT_COUNT__ === 0) ? Date.now() : 0;
            const observer = new MutationObserver(() => {
                const el = checkEl();
                if (el) {
                    if (waitForIdle && window.__FUSION_INFLIGHT_COUNT__ > 0) {
                        idleStart = 0;
                        return;
                    }
                    if (waitForIdle) {
                        if (!idleStart) idleStart = Date.now();
                        if (Date.now() - idleStart < idleCooldownMs) return;
                    }
                    cleanup();
                    resolve(el);
                }
            });

            const interval = setInterval(() => {
                const el = checkEl();
                if (el) {
                    if (waitForIdle && window.__FUSION_INFLIGHT_COUNT__ > 0) {
                        idleStart = 0;
                        return;
                    }
                    if (waitForIdle) {
                        if (!idleStart) idleStart = Date.now();
                        if (Date.now() - idleStart < idleCooldownMs) return;
                    }
                    cleanup();
                    resolve(el);
                    return;
                }
                if (Date.now() - start >= timeoutMs) {
                    cleanup();
                    reject(new Error(`[SmartGate] Timeout (${timeoutMs}ms) waiting for ready: ${selectorOrPredicate}`));
                }
            }, 50);

            try {
                observer.observe(document.documentElement || document.body, {
                    childList: true,
                    subtree: true,
                    attributes: true
                });
            } catch (_) {}

            function cleanup() {
                clearInterval(interval);
                try { observer.disconnect(); } catch (_) {}
            }
        });
    };

    window.waitForNetworkIdle = async function(idleCooldownMs = 300, timeoutMs = 8000) {
        const start = Date.now();
        let idleStart = (window.__FUSION_INFLIGHT_COUNT__ === 0) ? Date.now() : 0;
        return new Promise((resolve) => {
            const iv = setInterval(() => {
                if (window.__FUSION_INFLIGHT_COUNT__ === 0) {
                    if (!idleStart) idleStart = Date.now();
                    if (Date.now() - idleStart >= idleCooldownMs) {
                        clearInterval(iv);
                        resolve(true);
                    }
                } else {
                    idleStart = 0;
                }
                if (Date.now() - start >= timeoutMs) {
                    clearInterval(iv);
                    resolve(false);
                }
            }, 40);
        });
    };

    // ==========================================
    // ⚡ 2. IN-PAGE SINGLE-SHOT JIT RECIPE RUNNER
    // ==========================================
    window.__FUSION_RUN_RECIPE__ = async function(recipe, options = {}) {
        const startTime = Date.now();
        if (!recipe || !Array.isArray(recipe.events)) {
            return { success: false, error: 'Invalid recipe: missing events array' };
        }

        const events = recipe.events;
        const results = [];
        const activeWallet = window.__FUSION_ACTIVE_WALLET_NAME__ || window.__FUSION_ACTIVE_PROFILE__ || 'default';

        console.log(`[JIT Runner] Memulai eksekusi single-shot resep "${recipe.name || 'unnamed'}" (Events: ${events.length}) [${activeWallet}]`);

        for (let i = 0; i < events.length; i++) {
            const ev = events[i];
            const evType = ev.type || 'click';

            // Eksekusi API Call langsung di dalam JIT context
            if (evType === 'api_call' || ev.script) {
                try {
                    const scriptCode = ev.script;
                    const walletObj = {
                        address: window.__FUSION_ACTIVE_WALLET_ADDRESS__ || '',
                        name: activeWallet
                    };
                    const AsyncFn = Object.getPrototypeOf(async function(){}).constructor;
                    const fn = new AsyncFn('wallet', scriptCode);
                    const res = await fn(walletObj);
                    results.push({ step: i, type: 'api_call', ok: true, result: res });
                } catch (err) {
                    results.push({ step: i, type: 'api_call', ok: false, error: err.message });
                }
                continue;
            }

            // Eksekusi OTP Waiter langsung di JIT context
            if (evType === 'wait_otp' || evType === 'otp') {
                try {
                    let curEmail = window.__FUSION_ACTIVE_EMAIL__;
                    if (!curEmail && window.cloudMail) {
                        curEmail = await window.cloudMail.newEmail({ random: true, site: window.location.hostname, silent: true });
                        window.__FUSION_ACTIVE_EMAIL__ = curEmail;
                    }
                    const timeoutSec = ev.timeout || 45;
                    let otpCode = '';
                    if (window.cloudMail) {
                        otpCode = await window.cloudMail.waitOtp(curEmail, timeoutSec);
                    }
                    if (otpCode) {
                        let fillEl = null;
                        if (ev.selector) {
                            fillEl = await window.waitForReady(ev.selector, { timeoutMs: 2000, checkVisible: true }).catch(() => null);
                        }
                        if (!fillEl) {
                            const allInputs = Array.from(document.querySelectorAll('input'));
                            fillEl = allInputs.find(inp => {
                                const p = (inp.placeholder || '').toLowerCase();
                                const n = (inp.name || '').toLowerCase();
                                return p.includes('code') || p.includes('otp') || p.includes('验证码') || n.includes('code') || n.includes('otp') || inp.maxLength === 6 || inp.maxLength === 4;
                            });
                        }
                        if (fillEl) {
                            fillEl.focus();
                            fillEl.value = otpCode;
                            fillEl.dispatchEvent(new Event('input', { bubbles: true }));
                            fillEl.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        results.push({ step: i, type: 'wait_otp', ok: true, otp: otpCode });
                    } else {
                        results.push({ step: i, type: 'wait_otp', ok: false, error: 'OTP timeout' });
                    }
                } catch (err) {
                    results.push({ step: i, type: 'wait_otp', ok: false, error: err.message });
                }
                continue;
            }

            // Eksekusi Link Activation Waiter langsung di JIT context
            if (evType === 'wait_link') {
                try {
                    let curEmail = window.__FUSION_ACTIVE_EMAIL__;
                    const timeoutSec = ev.timeout || 60;
                    let link = '';
                    if (window.cloudMail) {
                        link = await window.cloudMail.waitLink(curEmail, timeoutSec);
                    }
                    if (link) {
                        if (ev.navigate !== false) {
                            window.location.href = link;
                        }
                        results.push({ step: i, type: 'wait_link', ok: true, link });
                    } else {
                        results.push({ step: i, type: 'wait_link', ok: false, error: 'Link timeout' });
                    }
                } catch (err) {
                    results.push({ step: i, type: 'wait_link', ok: false, error: err.message });
                }
                continue;
            }

            let targetEl = null;

            const selectorsToTry = [
                ev.selector,
                ev.ariaLabel ? `[aria-label="${ev.ariaLabel}"]` : null,
                ev.testid ? `[data-testid="${ev.testid}"]` : null,
                ev.id ? `#${ev.id}` : null
            ].filter(Boolean);

            for (const sel of selectorsToTry) {
                try {
                    targetEl = await window.waitForReady(sel, { timeoutMs: 2000, checkVisible: true }).catch(() => null);
                    if (targetEl) break;
                } catch (_) {}
            }

            if (!targetEl && ev.x && ev.y) {
                if (window.__FUSION_NATIVE__ && window.__FUSION_NATIVE__.touch) {
                    window.__FUSION_NATIVE__.touch(ev.x, ev.y);
                    results.push({ step: i, type: evType, method: 'hardware_touch', ok: true });
                    await new Promise(r => setTimeout(r, 60));
                    continue;
                }
            }

            if (!targetEl) {
                results.push({ step: i, type: evType, ok: false, error: `Element not found: ${ev.selector || ev.ariaLabel || 'unknown'}` });
                continue;
            }

            try {
                if (evType === 'input' || evType === 'change') {
                    let val = ev.value || '';
                    let curEmail = window.__FUSION_ACTIVE_EMAIL__;
                    if ((val.includes('{{EMAIL') || val.includes('{{OTP')) && !curEmail && window.cloudMail) {
                        curEmail = await window.cloudMail.newEmail({ random: true, site: window.location.hostname, silent: true });
                        window.__FUSION_ACTIVE_EMAIL__ = curEmail;
                    }
                    if (val.includes('{{WALLET_ADDRESS}}')) val = val.replace(/\{\{WALLET_ADDRESS\}\}/g, window.__FUSION_ACTIVE_WALLET_ADDRESS__ || '');
                    if (val.includes('{{WALLET_NAME}}')) val = val.replace(/\{\{WALLET_NAME\}\}/g, activeWallet || '');
                    if (curEmail) {
                        val = val.replace(/\{\{EMAIL\}\}/g, curEmail);
                        val = val.replace(/\{\{EMAIL_USER\}\}/g, curEmail.split('@')[0]);
                        val = val.replace(/\{\{EMAIL_PREFIX\}\}/g, curEmail.split('@')[0]);
                        val = val.replace(/\{\{EMAIL_DOMAIN\}\}/g, curEmail.split('@')[1] || '');
                    }
                    if (val.includes('{{OTP}}') && window.cloudMail) {
                        const otpCode = await window.cloudMail.waitOtp(curEmail, 45);
                        val = val.replace(/\{\{OTP\}\}/g, otpCode || '');
                    }

                    targetEl.focus();
                    targetEl.value = val;
                    targetEl.dispatchEvent(new Event('input', { bubbles: true }));
                    targetEl.dispatchEvent(new Event('change', { bubbles: true }));
                    results.push({ step: i, type: 'input', value: val, ok: true });
                } else {
                    targetEl.scrollIntoView({ behavior: 'instant', block: 'center' });
                    const rect = targetEl.getBoundingClientRect();
                    const cx = Math.round(rect.left + rect.width / 2);
                    const cy = Math.round(rect.top + rect.height / 2);

                    const opts = { bubbles: true, cancelable: true, view: window, clientX: cx, clientY: cy };
                    targetEl.dispatchEvent(new PointerEvent('pointerdown', opts));
                    targetEl.dispatchEvent(new MouseEvent('mousedown', opts));
                    targetEl.dispatchEvent(new PointerEvent('pointerup', opts));
                    targetEl.dispatchEvent(new MouseEvent('mouseup', opts));
                    targetEl.dispatchEvent(new MouseEvent('click', opts));
                    results.push({ step: i, type: 'click', ok: true });
                }
            } catch (err) {
                results.push({ step: i, type: evType, ok: false, error: err.message });
            }

            await new Promise(r => setTimeout(r, 50));
        }

        const elapsedMs = Date.now() - startTime;
        console.log(`[JIT Runner] Selesai dalam ${elapsedMs}ms. Steps: ${results.length}`);
        return {
            success: results.some(r => r.ok),
            stepsExecuted: results.length,
            elapsedMs,
            results
        };
    };

    // ==========================================
    // ⚡ 3. PER-WALLET ANTI-SYBIL MICRO-NOISE SHUFFLER
    // ==========================================
    function getWalletNoiseSeed() {
        const wName = window.__FUSION_ACTIVE_WALLET_NAME__ || window.__FUSION_ACTIVE_PROFILE__ || 'default';
        let h = 0x811c9dc5;
        for (let i = 0; i < wName.length; i++) {
            h ^= wName.charCodeAt(i);
            h = Math.imul(h, 0x01000193);
        }
        return Math.abs(h);
    }
    const WALLET_SEED = getWalletNoiseSeed();

    // AudioContext Noise (Deterministic per-wallet)
    if (window.AudioBuffer && AudioBuffer.prototype.getChannelData) {
        const origGetChannelData = AudioBuffer.prototype.getChannelData;
        AudioBuffer.prototype.getChannelData = function(...args) {
            const channel = origGetChannelData.apply(this, args);
            const seed = getWalletNoiseSeed();
            const factor = ((seed % 100) / 100000000);
            for (let i = 0; i < Math.min(channel.length, 32); i += 4) {
                channel[i] += factor;
            }
            return channel;
        };
    }

    // ==========================================
    // ⚡ 4. IN-PAGE TURBO TRACKER & BLOAT STRIPPER
    // ==========================================
    window.__FUSION_TURBO_ACTIVE__ = true;
    window.ga = window.ga || function() {};
    window.gtag = window.gtag || function() {};
    window.dataLayer = window.dataLayer || [];
    window.Sentry = window.Sentry || {
        init: () => {},
        captureException: () => {},
        captureMessage: () => {},
        setUser: () => {}
    };

    const origCreateElement = document.createElement.bind(document);
    document.createElement = function(tagName, options) {
        const el = origCreateElement(tagName, options);
        if (tagName && tagName.toLowerCase() === 'script') {
            const origSetAttr = el.setAttribute.bind(el);
            el.setAttribute = function(name, val) {
                if (name === 'src' && typeof val === 'string') {
                    if (val.includes('google-analytics.com') || val.includes('googletagmanager.com') ||
                        val.includes('sentry.io') || val.includes('clarity.ms') || val.includes('mixpanel.com') ||
                        val.includes('doubleclick.net') || val.includes('datadoghq.com')) {
                        console.log('[Turbo Blocker] Blocked tracking script:', val.slice(0, 60));
                        return origSetAttr('src', 'data:text/javascript;charset=utf-8,');
                    }
                }
                return origSetAttr(name, val);
            };
        }
        return el;
    };

    // ==========================================
    // ⚡ 5. UNIVERSAL NATIVE BRIDGE (FUSION HELPER)
    // ==========================================
    window.Fusion = {
        reflect: (className, methodName, args = []) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                const raw = bridge.reflect(className, methodName, JSON.stringify(args));
                return raw ? JSON.parse(raw) : null;
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        settings: (opts) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.applySettings(JSON.stringify(opts)));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        blackhole: (patterns) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                const list = Array.isArray(patterns) ? patterns : [patterns];
                return JSON.parse(bridge.setBlackholes(JSON.stringify(list)));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        mock: (pattern, body = '', statusCode = 200, mimeType = 'application/json', headers = {}) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.setMockRules(JSON.stringify([{
                    pattern,
                    body: typeof body === 'object' ? JSON.stringify(body) : String(body),
                    statusCode,
                    mimeType,
                    headers: JSON.stringify(headers)
                }])));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        clearNetworkRules: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.clearNetworkRules());
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        phoenix: (policy) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.setPhoenixPolicy(JSON.stringify(policy)));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        performance: (opts) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.setPerformanceMode(JSON.stringify(opts)));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        deviceInfo: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.getDeviceInfo());
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        deviceProfile: () => {
            if (typeof window.__FUSION_GET_ACTIVE_DEVICE_PROFILE__ === 'function') {
                return window.__FUSION_GET_ACTIVE_DEVICE_PROFILE__();
            }
            return window.__FUSION_ACTIVE_DEVICE_PROFILE__ || null;
        },
        haptic: (ms = 50) => {
            return window.Fusion.reflect('android.os.Vibrator', 'vibrate', [ms]);
        },
        ipc: (cmd) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.execIpc(cmd));
            } catch (e) {
                return { success: false, error: e.message };
            }
        },
        setCookie: (url, cookie) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.setCookie(url, cookie));
            } catch (e) { return { success: false, error: e.message }; }
        },
        getCookie: (url) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.getCookie(url));
            } catch (e) { return { success: false, error: e.message }; }
        },
        clearCookies: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.clearCookies());
            } catch (e) { return { success: false, error: e.message }; }
        },
        getClipboard: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.getClipboard());
            } catch (e) { return { success: false, error: e.message }; }
        },
        setClipboard: (text) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.setClipboard(text));
            } catch (e) { return { success: false, error: e.message }; }
        },
        toast: (message) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (bridge) bridge.showToast(message);
            } catch (_) {}
        },
        getLastDownload: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false, error: 'FusionNative bridge not injected' };
                return JSON.parse(bridge.getLastDownloadedFile());
            } catch (e) { return { success: false, error: e.message }; }
        },
        listTabs: () => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return [];
                return JSON.parse(bridge.listTabs());
            } catch (e) { return []; }
        },
        switchTab: (index) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false };
                return JSON.parse(bridge.switchTab(index));
            } catch (e) { return { success: false, error: e.message }; }
        },
        closeTab: (index) => {
            try {
                const bridge = window.FusionNative || window.__FUSION_NATIVE__;
                if (!bridge) return { success: false };
                return JSON.parse(bridge.closeTab(index));
            } catch (e) { return { success: false, error: e.message }; }
        }
    };

    console.log(`[Fusion Agent] Webview Omnipotent Master Engine v4.0.0 (Turbo, SmartGate, JIT & AntiSybil Ready) [Wallet Seed: ${WALLET_SEED}]`);
})();
