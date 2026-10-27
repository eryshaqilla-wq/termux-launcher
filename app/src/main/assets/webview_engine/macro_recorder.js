(function() {
    if (window.__FUSION_RECORDER_ATTACHED__) return;
    window.__FUSION_RECORDER_ATTACHED__ = true;

    window.__FUSION_RECORDING__ = false;
    window.__FUSION_MACRO_EVENTS__ = [];

    function isGhostActive() {
        return !!(window.__FUSION_GHOST_INJECTED__ || window.AgyRecorder);
    }

    function saveEvents(evts) {
        window.__FUSION_MACRO_EVENTS__ = evts;
        try {
            localStorage.setItem("__fusion_macro_events__", JSON.stringify(evts));
        } catch(_) {}
    }

    function getEvents() {
        if (window.__FUSION_MACRO_EVENTS__ && window.__FUSION_MACRO_EVENTS__.length > 0) {
            return window.__FUSION_MACRO_EVENTS__;
        }
        try {
            var raw = localStorage.getItem("__fusion_macro_events__");
            if (raw) {
                var p = JSON.parse(raw);
                if (Array.isArray(p)) {
                    window.__FUSION_MACRO_EVENTS__ = p;
                    return p;
                }
            }
        } catch(_) {}
        return [];
    }

    // --- FLOATING RECORDER ACTION BAR ---
    function showRecorderBar() {
        if (isGhostActive()) return; // Ghost engine handles its own clean supplementary HUD!
        var existing = document.getElementById("__fusion_rec_bar__");
        if (existing) existing.remove();

        var bar = document.createElement("div");
        bar.id = "__fusion_rec_bar__";
        bar.style.position = "fixed";
        bar.style.bottom = "54px";
        bar.style.left = "10px";
        bar.style.background = "rgba(15, 18, 26, 0.96)";
        bar.style.backdropFilter = "blur(14px)";
        bar.style.border = "1.5px solid #7C4DFF";
        bar.style.borderRadius = "14px";
        bar.style.padding = "5px 7px";
        bar.style.display = "flex";
        bar.style.flexDirection = "column";
        bar.style.gap = "4px";
        bar.style.maxWidth = "calc(100vw - 20px)";
        bar.style.zIndex = "2147483647";
        bar.style.boxShadow = "0 8px 32px rgba(0,0,0,0.9), 0 0 15px rgba(124,77,255,0.35)";
        bar.style.fontFamily = "system-ui, sans-serif";
        bar.style.fontSize = "11px";
        bar.style.color = "#FFFFFF";
        bar.style.touchAction = "manipulation";
        bar.style.userSelect = "none";

        var btnStyle = "border:none; border-radius:10px; padding:4px 7px; font-size:10.5px; font-weight:700; cursor:pointer;";

        var count = getEvents().length;
        bar.innerHTML = "<div style='display:flex; align-items:center; gap:4px;'>" +
                        "<span style='display:inline-flex; align-items:center; gap:3px; font-weight:700; font-size:10.5px; color:#FF5252; margin-right:2px; flex-shrink:0;'>" +
                        "<span style='display:inline-block; width:7px; height:7px; background:#FF5252; border-radius:50%;'></span>" +
                        "<span id='__fusion_rec_count__'>REC (" + count + ")</span></span>" +
                        "<button id='__frec_next_wallet__' style='background:#7C4DFF; color:#FFF; " + btnStyle + "'>👛 Wallet</button>" +
                        "<button id='__frec_next_email__' style='background:#FFA000; color:#000; " + btnStyle + "'>📧 Email</button>" +
                        "<button id='__frec_next_ref__' style='background:#00E676; color:#000; " + btnStyle + "'>🔗 Ref</button>" +
                        "</div>" +
                        "<div style='display:flex; align-items:center; gap:4px;'>" +
                        "<button id='__frec_reload__' style='background:#00B0FF; color:#000; " + btnStyle + "'>🔄 Reload</button>" +
                        "<button id='__frec_wipe__' style='background:#FF5722; color:#FFF; " + btnStyle + "'>🧹 Wipe</button>" +
                        "<button id='__frec_fp__' style='background:#E040FB; color:#FFF; " + btnStyle + "'>🎭 FP</button>" +
                        "<button id='__frec_stop__' style='background:#2E7D32; color:#FFF; " + btnStyle + "'>⏹ Selesai</button>" +
                        "</div>";

        (document.body || document.documentElement).appendChild(bar);

        // Bind Tombol Khusus
        var bind = function(id, fn) {
            var el = document.getElementById(id);
            if (!el) return;
            el.addEventListener("click", function(e) {
                e.stopPropagation();
                e.preventDefault();
                fn();
            });
        };

        bind("__frec_next_wallet__", function() {
            var nw = "";
            try {
                if (window.GhostBridge && window.GhostBridge.nextWallet) {
                    nw = window.GhostBridge.nextWallet();
                }
            } catch(_) {}
            window.__FUSION_RECORD_ACTION__({
                type: "next_wallet",
                timestamp: Date.now(),
                text: "Ganti Wallet -> " + (nw || "Next")
            });
        });

        bind("__frec_next_email__", function() {
            var ne = (typeof AgyDB !== "undefined" && AgyDB.nextEmail) ? AgyDB.nextEmail() : "tuyul@gmail.com";
            window.__FUSION_RECORD_ACTION__({
                type: "next_email",
                timestamp: Date.now(),
                text: "Ganti Email -> " + ne,
                value: ne
            });
        });

        bind("__frec_next_ref__", function() {
            var nr = (typeof AgyDB !== "undefined" && AgyDB.nextRef) ? AgyDB.nextRef() : "REF123";
            window.__FUSION_RECORD_ACTION__({
                type: "ref",
                timestamp: Date.now(),
                text: "Ref Code -> " + nr,
                value: nr
            });
        });

        bind("__frec_reload__", function() {
            window.__FUSION_RECORD_ACTION__({
                type: "reload",
                timestamp: Date.now(),
                text: "Reload Halaman"
            });
            try {
                sessionStorage.setItem("__fusion_rec_active__", "true");
            } catch(_) {}
            setTimeout(function() {
                location.reload();
            }, 300);
        });

        bind("__frec_wipe__", function() {
            try {
                if (window.GhostBridge && window.GhostBridge.wipeCache) {
                    window.GhostBridge.wipeCache();
                }
            } catch(_) {}
            try { localStorage.clear(); sessionStorage.clear(); } catch(_) {}
            window.__FUSION_RECORD_ACTION__({
                type: "wipe_cache",
                timestamp: Date.now(),
                text: "Wipe Cache & Storage"
            });
        });

        bind("__frec_fp__", function() {
            var fp = "";
            try {
                if (window.GhostBridge && window.GhostBridge.nextFingerprint) {
                    fp = window.GhostBridge.nextFingerprint();
                }
            } catch(_) {}
            window.__FUSION_RECORD_ACTION__({
                type: "fingerprint",
                timestamp: Date.now(),
                text: "Fingerprint -> " + (fp || "Acak"),
                value: fp
            });
        });

        bind("__frec_stop__", function() {
            window.__FUSION_STOP_REC__();
        });
    }

    function hideRecorderBar() {
        var el = document.getElementById("__fusion_rec_bar__");
        if (el) el.remove();
    }

    function updateCounter() {
        var el = document.getElementById("__fusion_rec_count__");
        if (el) el.innerText = "REC (" + getEvents().length + ")";
    }

    window.__FUSION_START_REC__ = function() {
        window.__FUSION_RECORDING__ = true;
        saveEvents([]);
        try {
            sessionStorage.setItem("__fusion_rec_active__", "true");
        } catch(_) {}
        if (!isGhostActive()) {
            showRecorderBar();
        }
        console.log("[FusionRecorder] Recording started");
        return "STARTED";
    };

    window.__FUSION_STOP_REC__ = function() {
        window.__FUSION_RECORDING__ = false;
        try {
            sessionStorage.removeItem("__fusion_rec_active__");
        } catch(_) {}
        hideRecorderBar();
        var evts = getEvents();
        console.log("[FusionRecorder] Recording stopped, total events: " + evts.length);
        return JSON.stringify(evts);
    };

    window.__FUSION_RECORD_ACTION__ = function(actionObj) {
        if (!window.__FUSION_RECORDING__) return;
        var evts = getEvents();
        var now = Date.now();
        var last = evts[evts.length - 1];
        if (last && (now - (last.timestamp || 0) < 400)) {
            if (last.selector === actionObj.selector || (last.type === actionObj.type && last.text === actionObj.text)) {
                return;
            }
        }
        actionObj.timestamp = now;
        evts.push(actionObj);
        saveEvents(evts);
        updateCounter();
        console.log("[FusionRecorder] Action recorded:", actionObj.type, actionObj.text || actionObj.selector);
    };

    // Auto-resume recording across page reloads
    try {
        if (sessionStorage.getItem("__fusion_rec_active__") === "true") {
            window.__FUSION_RECORDING__ = true;
            if (!isGhostActive()) {
                setTimeout(showRecorderBar, 500);
            }
        }
    } catch(_) {}

    var getInteractiveTarget = function(el) {
        if (!el || el.nodeType !== 1) return null;
        if (el.id && (el.id.startsWith("__fusion_rec_") || el.id.startsWith("__agy_") || el.id.startsWith("sRecPill"))) return null;
        if (el.closest && el.closest("[id^='__fusion_rec_'], [id^='__agy_'], #sRecPill, [class*='fusion-ui'], [class*='agy-ui']")) return null;
        var interactive = el.closest('button, a, input, textarea, select, [role="button"], [tabindex], [onclick]');
        return interactive || el;
    };

    var getCssSelector = function(el) {
        if (!el || el.nodeType !== 1) return "";
        if (el.id && !el.id.startsWith("__fusion_") && !el.id.startsWith("__agy_")) return "#" + el.id;
        if (el.getAttribute("data-testid")) return "[data-testid='" + el.getAttribute("data-testid") + "']";
        var path = [];
        var cur = el;
        while (cur && cur.nodeType === 1) {
            var tag = cur.nodeName.toLowerCase();
            if (cur.id && !cur.id.startsWith("__fusion_") && !cur.id.startsWith("__agy_")) {
                path.unshift("#" + cur.id);
                break;
            } else {
                var sib = cur, nth = 1;
                while ((sib = sib.previousElementSibling)) {
                    if (sib.nodeName.toLowerCase() === tag) nth++;
                }
                path.unshift(tag + ":nth-of-type(" + nth + ")");
            }
            cur = cur.parentNode;
        }
        return path.join(" > ");
    };

    var lastMacroClickTime = 0;
    document.addEventListener("click", function(e) {
        if (isGhostActive()) return; // Ghost engine handles all recording!
        if (!window.__FUSION_RECORDING__) return;
        if (window.__AGY_SIMULATED_CLICK__) return;

        var now = Date.now();
        if (now - lastMacroClickTime < 400) return;

        var target = getInteractiveTarget(e.target);
        if (!target) return;

        lastMacroClickTime = now;
        var ev = {
            type: "click",
            timestamp: now,
            x: e.clientX,
            y: e.clientY,
            selector: getCssSelector(target),
            tag: target.tagName,
            text: (target.innerText || target.value || target.getAttribute("aria-label") || "").substring(0, 30).trim()
        };
        window.__FUSION_RECORD_ACTION__(ev);
    }, true);

    document.addEventListener("input", function(e) {
        if (isGhostActive()) return; // Ghost engine handles all recording!
        if (!window.__FUSION_RECORDING__) return;
        var target = e.target;
        if (!target || (target.id && (target.id.startsWith("__fusion_rec_") || target.id.startsWith("__agy_")))) return;
        var ev = {
            type: "input",
            timestamp: Date.now(),
            selector: getCssSelector(target),
            value: target.value
        };
        window.__FUSION_RECORD_ACTION__(ev);
    }, true);

    // --- FUSION STEP-BY-STEP MACRO REPLAYER WITH RELOAD PERSISTENCE ---
    window.__FUSION_PLAY_MACRO__ = function(eventsJson, startIndex) {
        var events = null;
        if (eventsJson && typeof eventsJson === 'string' && eventsJson !== "null" && eventsJson.length > 2) {
            try { events = JSON.parse(eventsJson); } catch(_) {}
        }
        if (!events || events.length === 0) {
            events = getEvents();
        }
        if (!events || events.length === 0) {
            console.log("[FusionRecorder] No macro events to replay");
            return "No events";
        }

        var idx = (typeof startIndex === "number") ? startIndex : 0;
        console.log("[FusionRecorder] Replaying step " + (idx + 1) + "/" + events.length);

        function playNext() {
            if (idx >= events.length) {
                try { sessionStorage.removeItem("__fusion_macro_replay_state__"); } catch(_) {}
                console.log("[FusionRecorder] Replay finished successfully!");
                return;
            }

            var ev = events[idx];
            idx++;

            // 1. Step Next Wallet
            if (ev.type === "next_wallet") {
                console.log("[FusionRecorder] Executing Next Wallet...");
                try {
                    if (window.GhostBridge && window.GhostBridge.nextWallet) {
                        window.GhostBridge.nextWallet();
                    }
                } catch(_) {}
                setTimeout(playNext, 1000);
                return;
            }

            // 2. Step Reload Halaman (Survive across reload)
            if (ev.type === "reload") {
                console.log("[FusionRecorder] Executing Reload & saving replay state...");
                try {
                    sessionStorage.setItem("__fusion_macro_replay_state__", JSON.stringify({
                        events: events,
                        nextIndex: idx,
                        timestamp: Date.now()
                    }));
                } catch(_) {}
                setTimeout(function() {
                    location.reload();
                }, 400);
                return;
            }

            // 3. Step Wipe Cache
            if (ev.type === "wipe_cache") {
                console.log("[FusionRecorder] Executing Wipe Cache...");
                try {
                    if (window.GhostBridge && window.GhostBridge.wipeCache) {
                        window.GhostBridge.wipeCache();
                    }
                } catch(_) {}
                try { localStorage.clear(); sessionStorage.clear(); } catch(_) {}
                setTimeout(playNext, 800);
                return;
            }

            // 3b. Step Next Email
            if (ev.type === "next_email") {
                console.log("[FusionRecorder] Executing Next Email ->", ev.value || "");
                setTimeout(playNext, 600);
                return;
            }

            // 3c. Step Ref Code
            if (ev.type === "ref") {
                console.log("[FusionRecorder] Executing Ref Code ->", ev.value || "");
                setTimeout(playNext, 600);
                return;
            }

            // 3d. Step Fingerprint
            if (ev.type === "fingerprint") {
                console.log("[FusionRecorder] Executing Fingerprint...");
                try {
                    if (window.GhostBridge && window.GhostBridge.nextFingerprint) {
                        window.GhostBridge.nextFingerprint();
                    }
                } catch(_) {}
                setTimeout(playNext, 800);
                return;
            }

            // 4. Step Klik DOM
            if (ev.type === "click") {
                var el = ev.selector ? document.querySelector(ev.selector) : null;
                if (!el && ev.text) {
                    var all = Array.from(document.querySelectorAll("button, a, [role=button], input[type=button]"));
                    el = all.find(function(b) {
                        var t = (b.innerText || b.value || b.getAttribute("aria-label") || "").trim().toLowerCase();
                        return t.includes(ev.text.toLowerCase());
                    });
                }
                if (!el && ev.x && ev.y) {
                    el = document.elementFromPoint(ev.x, ev.y);
                }

                if (el) {
                    try { el.scrollIntoView({ behavior: 'smooth', block: 'center' }); } catch(_) {}
                    try {
                        var opts = { bubbles: true, cancelable: true, view: window };
                        if (window.PointerEvent) {
                            el.dispatchEvent(new PointerEvent('pointerdown', opts));
                            el.dispatchEvent(new PointerEvent('pointerup', opts));
                        }
                        el.dispatchEvent(new MouseEvent('mousedown', opts));
                        el.dispatchEvent(new MouseEvent('mouseup', opts));
                        if (typeof el.click === 'function') el.click();
                        else el.dispatchEvent(new MouseEvent('click', opts));
                    } catch(e) {}
                    console.log("[FusionRecorder] Click executed on:", ev.text || ev.selector);
                } else {
                    console.warn("[FusionRecorder] Target not found for click:", ev.selector || ev.text);
                }
                setTimeout(playNext, 1200);
                return;
            }

            // 5. Step Input Form
            if (ev.type === "input" || ev.type === "fill") {
                var inp = ev.selector ? document.querySelector(ev.selector) : null;
                if (inp) {
                    inp.value = ev.value;
                    inp.dispatchEvent(new Event("input", { bubbles: true }));
                    inp.dispatchEvent(new Event("change", { bubbles: true }));
                }
                setTimeout(playNext, 800);
                return;
            }

            setTimeout(playNext, 500);
        }

        playNext();
    };

    // Auto-resume replay on page reload
    (function checkAndResumeReplay() {
        try {
            var raw = sessionStorage.getItem("__fusion_macro_replay_state__");
            if (!raw) return;
            sessionStorage.removeItem("__fusion_macro_replay_state__");
            var st = JSON.parse(raw);
            if (st && Array.isArray(st.events) && typeof st.nextIndex === "number") {
                console.log("[FusionRecorder] Resuming macro replay after reload at step " + (st.nextIndex + 1));
                setTimeout(function() {
                    window.__FUSION_PLAY_MACRO__(st.events, st.nextIndex);
                }, 1500);
            }
        } catch(_) {}
    })();
})();
