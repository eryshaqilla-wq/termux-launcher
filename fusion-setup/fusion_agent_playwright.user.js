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

    const FUSION_ENGINE_VERSION = '1.2.0';
    if (window.__FUSION_AGENT_ENGINE__ && window.__FUSION_AGENT_ENGINE__.version === FUSION_ENGINE_VERSION) return;

    // --- 1. WebRTC STUN Leak Protection (Stealth WebRTC) ---
    window.__FUSION_WEBRTC_STEALTH__ = true;
    if (window.RTCPeerConnection) {
        const OrigPeerConnection = window.RTCPeerConnection;
        window.RTCPeerConnection = function(config, constraints) {
            if (window.__FUSION_WEBRTC_STEALTH__ && config && config.iceServers) {
                config = Object.assign({}, config, { iceServers: [] });
            }
            return new OrigPeerConnection(config, constraints);
        };
        window.RTCPeerConnection.prototype = OrigPeerConnection.prototype;
    }

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
        const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT);
        let curr = walker.nextNode();
        while (curr) {
            if (curr.shadowRoot) {
                const deepFound = querySelectorDeep(selector, curr.shadowRoot);
                if (deepFound) return deepFound;
            }
            curr = walker.nextNode();
        }
        return null;
    }

    function resolveElement(target) {
        if (!target) return null;
        const str = String(target).trim();
        
        const cleanRef = str.startsWith('e') ? str : ('e' + str);
        if (window.__FUSION_ELEMENT_REFS__.has(cleanRef)) {
            const el = window.__FUSION_ELEMENT_REFS__.get(cleanRef);
            if (el && document.contains(el)) return el;
        }

        const byAttr = querySelectorDeep('[data-agy-ref="' + cleanRef + '"]') || querySelectorDeep('[data-agy-ref="' + str + '"]');
        if (byAttr) return byAttr;

        try {
            const bySelector = querySelectorDeep(str);
            if (bySelector) return bySelector;
        } catch (_) {}

        const candidates = Array.from(document.querySelectorAll('button, a, input[type=button], input[type=submit], [role=button]'));
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
    window.__FUSION_NATIVE__ = {
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

    console.log('[Fusion Agent] Webview Omnipotent Master Engine v3.5.0 (Stealth & Shadow Ready) Ready');
})();
