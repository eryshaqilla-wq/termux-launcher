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
        for (const c of candidates) {
            const txt = (c.innerText || c.value || c.getAttribute('aria-label') || '').trim();
            if (txt.toLowerCase() === str.toLowerCase()) return c;
        }
        for (const c of candidates) {
            const txt = (c.innerText || c.value || c.getAttribute('aria-label') || '').trim();
            if (txt.toLowerCase().includes(str.toLowerCase())) return c;
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
        }
    };

    console.log('[Fusion Agent] Playwright ARIA Engine v1.0.0 Ready');
})();
