(function() {
  if (window.__FUSION_GECKO_BG_READY__) return;
  window.__FUSION_GECKO_BG_READY__ = true;

  let proxyConfig = { enabled: false, type: "direct", host: "", port: 0, user: "", pass: "" };
  let blockConfig = { media: false, fonts: false, css: false, trackers: true };

  const TRACKER_DOMAINS = [
    "google-analytics.com",
    "googletagmanager.com",
    "doubleclick.net",
    "sentry.io",
    "mixpanel.com",
    "clarity.ms",
    "hotjar.com",
    "datadoghq.com",
    "segment.io",
    "amplitude.com"
  ];

  try {
    const port = browser.runtime.connectNative("fusion_bg");
    port.onMessage.addListener((msg) => {
      if (!msg || typeof msg !== "object") return;
      if (msg.type === "setProxy") {
        proxyConfig = {
          enabled: !!msg.enabled && !!msg.host && Number(msg.port) > 0,
          type: (msg.scheme === "socks5" || msg.scheme === "socks") ? "socks" : "http",
          host: String(msg.host || ""),
          port: Number(msg.port) || 0,
          user: String(msg.user || ""),
          pass: String(msg.pass || "")
        };
      } else if (msg.type === "setBlockers") {
        blockConfig = {
          media: !!msg.media,
          fonts: !!msg.fonts,
          css: !!msg.css,
          trackers: msg.trackers !== undefined ? !!msg.trackers : true
        };
      }
    });
  } catch (_) {}

  try {
    if (browser.proxy && browser.proxy.onRequest) {
      browser.proxy.onRequest.addListener((requestInfo) => {
        if (!proxyConfig.enabled) {
          return { type: "direct" };
        }
        try {
          const u = new URL(requestInfo.url);
          if (u.hostname === "127.0.0.1" || u.hostname === "localhost" || u.hostname === "::1") {
            return { type: "direct" };
          }
        } catch (_) {}
        return {
          type: proxyConfig.type,
          host: proxyConfig.host,
          port: proxyConfig.port,
          proxyDNS: proxyConfig.type === "socks"
        };
      }, { urls: ["<all_urls>"] });
    }
  } catch (_) {}

  try {
    if (browser.webRequest && browser.webRequest.onBeforeRequest) {
      browser.webRequest.onBeforeRequest.addListener((details) => {
        if (!details || !details.url) return {};
        const urlLower = details.url.toLowerCase();
        if (blockConfig.trackers) {
          for (let i = 0; i < TRACKER_DOMAINS.length; i++) {
            if (urlLower.indexOf(TRACKER_DOMAINS[i]) !== -1) {
              return { cancel: true };
            }
          }
        }
        const rType = details.type || "";
        if (blockConfig.media && (rType === "image" || rType === "imageset" || rType === "media")) {
          return { cancel: true };
        }
        if (blockConfig.fonts && rType === "font") {
          return { cancel: true };
        }
        if (blockConfig.css && rType === "stylesheet") {
          return { cancel: true };
        }
        return {};
      }, { urls: ["<all_urls>"] }, ["blocking"]);
    }
  } catch (_) {}

  try {
    if (browser.webRequest && browser.webRequest.onAuthRequired) {
      browser.webRequest.onAuthRequired.addListener((details) => {
        if (details && details.isProxy && proxyConfig.enabled && proxyConfig.user && proxyConfig.pass) {
          return {
            authCredentials: {
              username: proxyConfig.user,
              password: proxyConfig.pass
            }
          };
        }
        return {};
      }, { urls: ["<all_urls>"] }, ["blocking"]);
    }
  } catch (_) {}
})();
