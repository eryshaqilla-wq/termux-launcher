(function() {
  if (window.__FUSION_GECKO_BG_READY__) return;
  window.__FUSION_GECKO_BG_READY__ = true;

  let defaultProxyConfig = { enabled: false, type: "direct", host: "", port: 0, user: "", pass: "" };
  const tabProxies = new Map();
  const tabIdToKey = new Map();
  const cookieStoreToKey = new Map();
  let blockConfig = { media: false, fonts: false, css: false, trackers: true };

  function parseProxyMsg(msg) {
    return {
      enabled: !!msg.enabled && !!msg.host && Number(msg.port) > 0,
      type: (msg.scheme === "socks5" || msg.scheme === "socks") ? "socks" : "http",
      host: String(msg.host || ""),
      port: Number(msg.port) || 0,
      user: String(msg.user || ""),
      pass: String(msg.pass || "")
    };
  }

  function resolveProxyConfig(details) {
    if (details) {
      if (details.tabId !== undefined && details.tabId !== null && tabIdToKey.has(details.tabId)) {
        const key = tabIdToKey.get(details.tabId);
        if (tabProxies.has(key)) return tabProxies.get(key);
      }
      if (details.cookieStoreId && cookieStoreToKey.has(details.cookieStoreId)) {
        const key = cookieStoreToKey.get(details.cookieStoreId);
        if (tabProxies.has(key)) return tabProxies.get(key);
      }
    }
    return defaultProxyConfig;
  }

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
        const parsed = parseProxyMsg(msg);
        if (msg.tabKey) {
          tabProxies.set(String(msg.tabKey), parsed);
        }
        defaultProxyConfig = parsed;
      } else if (msg.type === "unbindTab" && msg.tabKey) {
        const key = String(msg.tabKey);
        tabProxies.delete(key);
        for (const [tid, k] of tabIdToKey.entries()) {
          if (k === key) tabIdToKey.delete(tid);
        }
        for (const [csid, k] of cookieStoreToKey.entries()) {
          if (k === key) cookieStoreToKey.delete(csid);
        }
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
    if (browser.runtime && browser.runtime.onMessage) {
      browser.runtime.onMessage.addListener((msg, sender) => {
        if (!msg || typeof msg !== "object") return;
        if (msg.type === "bindTab" && msg.tabKey) {
          const key = String(msg.tabKey);
          if (sender && sender.tab) {
            if (sender.tab.id !== undefined && sender.tab.id !== null) {
              tabIdToKey.set(sender.tab.id, key);
            }
            if (sender.tab.cookieStoreId) {
              cookieStoreToKey.set(sender.tab.cookieStoreId, key);
            }
          }
        }
      });
    }
  } catch (_) {}

  try {
    if (browser.proxy && browser.proxy.onRequest) {
      browser.proxy.onRequest.addListener((requestInfo) => {
        const cfg = resolveProxyConfig(requestInfo);
        if (!cfg.enabled) {
          return { type: "direct" };
        }
        try {
          const u = new URL(requestInfo.url);
          if (u.hostname === "127.0.0.1" || u.hostname === "localhost" || u.hostname === "::1") {
            return { type: "direct" };
          }
        } catch (_) {}
        return {
          type: cfg.type,
          host: cfg.host,
          port: cfg.port,
          proxyDNS: cfg.type === "socks"
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
        const cfg = resolveProxyConfig(details);
        if (details && details.isProxy && cfg.enabled && cfg.user && cfg.pass) {
          return {
            authCredentials: {
              username: cfg.user,
              password: cfg.pass
            }
          };
        }
        return {};
      }, { urls: ["<all_urls>"] }, ["blocking"]);
    }
  } catch (_) {}
})();
