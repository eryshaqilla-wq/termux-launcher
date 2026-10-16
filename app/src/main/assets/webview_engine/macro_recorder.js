(function() {
    if (window.__FUSION_RECORDER_ATTACHED__) return;
    window.__FUSION_RECORDER_ATTACHED__ = true;

    window.__FUSION_RECORDING__ = false;
    window.__FUSION_MACRO_EVENTS__ = [];

    window.__FUSION_START_REC__ = function() {
        window.__FUSION_RECORDING__ = true;
        window.__FUSION_MACRO_EVENTS__ = [];
        try {
            localStorage.removeItem("__fusion_macro_events__");
        } catch(_) {}
        console.log("[FusionRecorder] Recording started");
        return "STARTED";
    };

    window.__FUSION_STOP_REC__ = function() {
        window.__FUSION_RECORDING__ = false;
        var json = JSON.stringify(window.__FUSION_MACRO_EVENTS__);
        try {
            localStorage.setItem("__fusion_macro_events__", json);
        } catch(_) {}
        console.log("[FusionRecorder] Recording stopped, total events: " + window.__FUSION_MACRO_EVENTS__.length);
        return json;
    };

    var getInteractiveTarget = function(el) {
        if (!el || el.nodeType !== 1) return null;
        var interactive = el.closest('button, a, input, textarea, select, [role="button"], [tabindex], [onclick]');
        return interactive || el;
    };

    var getCssSelector = function(el) {
        if (!el || el.nodeType !== 1) return "";
        if (el.id) return "#" + el.id;
        if (el.getAttribute("data-testid")) return "[data-testid='" + el.getAttribute("data-testid") + "']";
        var path = [];
        while (el && el.nodeType === 1) {
            var tag = el.nodeName.toLowerCase();
            if (el.id) {
                path.unshift("#" + el.id);
                break;
            } else {
                var sib = el, nth = 1;
                while ((sib = sib.previousElementSibling)) {
                    if (sib.nodeName.toLowerCase() === tag) nth++;
                }
                path.unshift(tag + ":nth-of-type(" + nth + ")");
            }
            el = el.parentNode;
        }
        return path.join(" > ");
    };

    document.addEventListener("click", function(e) { if (window.__AGY_ENGINE_INITIALIZED__) return;
        if (!window.__FUSION_RECORDING__) return;
        var target = getInteractiveTarget(e.target);
        if (!target) target = e.target;
        var ev = {
            type: "click",
            timestamp: Date.now(),
            x: e.clientX,
            y: e.clientY,
            selector: getCssSelector(target),
            tag: target.tagName,
            text: (target.innerText || target.value || "").substring(0, 30).trim()
        };
        window.__FUSION_MACRO_EVENTS__.push(ev);
        try {
            localStorage.setItem("__fusion_macro_events__", JSON.stringify(window.__FUSION_MACRO_EVENTS__));
        } catch(_) {}
        console.log("[FusionRecorder] Recorded click:", ev.selector, ev.text);
    }, true);

    document.addEventListener("input", function(e) { if (window.__AGY_ENGINE_INITIALIZED__) return;
        if (!window.__FUSION_RECORDING__) return;
        var target = e.target;
        var ev = {
            type: "input",
            timestamp: Date.now(),
            selector: getCssSelector(target),
            value: target.value
        };
        window.__FUSION_MACRO_EVENTS__.push(ev);
        try {
            localStorage.setItem("__fusion_macro_events__", JSON.stringify(window.__FUSION_MACRO_EVENTS__));
        } catch(_) {}
        console.log("[FusionRecorder] Recorded input:", ev.selector);
    }, true);

    window.__FUSION_PLAY_MACRO__ = function(eventsJson) {
        var events = null;
        if (eventsJson && typeof eventsJson === 'string' && eventsJson !== "null" && eventsJson.length > 2) {
            try { events = JSON.parse(eventsJson); } catch(_) {}
        }
        if (!events || events.length === 0) {
            events = window.__FUSION_MACRO_EVENTS__;
        }
        if (!events || events.length === 0) {
            try {
                var cached = localStorage.getItem("__fusion_macro_events__");
                if (cached) events = JSON.parse(cached);
            } catch(_) {}
        }
        if (!events || events.length === 0) {
            console.log("[FusionRecorder] No macro events to replay");
            return "No events";
        }
        console.log("[FusionRecorder] Replaying " + events.length + " events...");
        var baseTime = events[0].timestamp;
        events.forEach(function(ev, idx) {
            var delay = Math.max(0, ev.timestamp - baseTime);
            if (idx > 0 && delay === 0) delay = idx * 400;
            setTimeout(function() {
                try {
                    var el = ev.selector ? document.querySelector(ev.selector) : null;
                    if (!el && ev.x && ev.y) {
                        el = document.elementFromPoint(ev.x, ev.y);
                    }
                    if (el) {
                        var target = getInteractiveTarget(el) || el;
                        if (ev.type === "click") {
                            try { target.scrollIntoView({behavior: 'smooth', block: 'center'}); } catch(_) {}
                            try {
                                var opts = { bubbles: true, cancelable: true, view: window, clientX: ev.x, clientY: ev.y };
                                if (window.PointerEvent) {
                                    target.dispatchEvent(new PointerEvent('pointerdown', opts));
                                    target.dispatchEvent(new PointerEvent('pointerup', opts));
                                }
                                target.dispatchEvent(new MouseEvent('mousedown', opts));
                                target.dispatchEvent(new MouseEvent('mouseup', opts));
                                if (typeof target.click === 'function') {
                                    target.click();
                                } else {
                                    target.dispatchEvent(new MouseEvent('click', opts));
                                }
                            } catch(err) {
                                console.error("[FusionRecorder] Click dispatch error:", err);
                            }
                            console.log("[FusionRecorder] Replayed click on " + ev.selector);
                        } else if (ev.type === "input") {
                            target.value = ev.value;
                            target.dispatchEvent(new Event("input", {bubbles: true}));
                            target.dispatchEvent(new Event("change", {bubbles: true}));
                            console.log("[FusionRecorder] Replayed input on " + ev.selector + " = " + ev.value);
                        }
                    } else {
                        console.warn("[FusionRecorder] Target not found for: " + ev.selector);
                    }
                } catch(e) {
                    console.error("[FusionRecorder] Error replaying event: " + e);
                }
            }, delay);
        });
        return "Replaying " + events.length + " events";
    };

    window.__AI_DEVBRIDGE_INSPECT__ = function() {
        try {
            var forms = Array.from(document.forms).map(function(f) {
                return {
                    id: f.id || null,
                    action: f.action || null,
                    elements: Array.from(f.elements).map(function(e) {
                        return { name: e.name || e.id || null, type: e.type || null, value: (e.value || '').substring(0, 50) };
                    })
                };
            });
            var meta = {
                title: document.title,
                url: window.location.href,
                readyState: document.readyState,
                buttons: Array.from(document.querySelectorAll('button, input[type="button"], input[type="submit"], [role="button"]')).slice(0, 30).map(function(b) {
                    return { text: (b.innerText || b.value || '').trim().substring(0, 40), id: b.id || null, className: b.className || null };
                }),
                links: Array.from(document.querySelectorAll('a')).slice(0, 30).map(function(a) {
                    return { text: (a.innerText || '').trim().substring(0, 40), href: a.href };
                }),
                inputs: Array.from(document.querySelectorAll('input, textarea, select')).slice(0, 30).map(function(i) {
                    return { name: i.name || i.id || null, type: i.type || 'text', placeholder: i.placeholder || '', value: (i.value || '').substring(0, 50) };
                }),
                forms: forms,
                ethereum: (typeof window.ethereum !== 'undefined') ? {
                    hasEthereum: true,
                    selectedAddress: window.ethereum.selectedAddress || null,
                    chainId: window.ethereum.chainId || null
                } : { hasEthereum: false },
                timestamp: Date.now()
            };
            return JSON.stringify(meta);
        } catch(err) {
            return JSON.stringify({ error: String(err) });
        }
    };
})();
