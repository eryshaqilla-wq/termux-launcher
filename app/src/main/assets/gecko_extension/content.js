(function() {
  if (window.__FUSION_GECKO_CONTENT_READY__) return;
  window.__FUSION_GECKO_CONTENT_READY__ = true;

  let port = null;
  let pendingQueue = [];

  function serializeEvalVal(v) {
    if (v === undefined) return null;
    try {
      return JSON.stringify(v);
    } catch (_) {
      return String(v);
    }
  }

  function runInPageWorld(code, wantResult, onDone) {
    const target = document.documentElement || document.head;
    if (!target) {
      return { deferred: true };
    }
    if (!wantResult) {
      try {
        const s = document.createElement("script");
        s.textContent = code;
        target.appendChild(s);
        s.remove();
        const res = { done: true, value: null, error: null };
        if (onDone) onDone(res);
        return res;
      } catch (err) {
        const res = { done: false, error: String(err) };
        if (onDone) onDone(res);
        return res;
      }
    }

    const eventId = "__fg_eval_" + Math.random().toString(36).slice(2) + "_" + Date.now();
    const asyncEventId = eventId + "_async";
    let captured = { done: false, pendingAsync: false, value: null, error: null };
    const handler = (ev) => {
      if (ev && ev.detail) {
        captured = ev.detail;
      } else {
        captured = { done: true, value: null, error: null };
      }
    };
    window.addEventListener(eventId, handler, { once: true });

    let asyncFinished = false;
    let asyncTimer = null;
    const finishAsync = (resObj) => {
      if (asyncFinished) return;
      asyncFinished = true;
      if (asyncTimer) clearTimeout(asyncTimer);
      window.removeEventListener(asyncEventId, asyncHandler);
      if (onDone) onDone(resObj);
    };
    const asyncHandler = (ev) => {
      const detail = (ev && ev.detail) ? ev.detail : { done: true, value: null, error: null };
      finishAsync(detail);
    };
    window.addEventListener(asyncEventId, asyncHandler, { once: true });

    try {
      const s = document.createElement("script");
      s.textContent =
        "(function(){var v=null,e=null,pending=false;" +
        "function _ser(x){if(x===undefined)return null;try{return JSON.stringify(x);}catch(_){return String(x);}}" +
        "try{v=(0,eval)(" + JSON.stringify(code) + ");" +
        "if(v&&typeof v.then==='function'){pending=true;" +
        "v.then(function(r){window.dispatchEvent(new CustomEvent(" + JSON.stringify(asyncEventId) + ",{detail:{done:true,value:_ser(r),error:null}}));}," +
        "function(err){window.dispatchEvent(new CustomEvent(" + JSON.stringify(asyncEventId) + ",{detail:{done:true,value:null,error:String(err&&err.message?err.message:err)}}));});" +
        "}else{v=_ser(v);}" +
        "}catch(err){e=String(err&&err.message?err.message:err);}" +
        "window.dispatchEvent(new CustomEvent(" + JSON.stringify(eventId) + ",{detail:{done:true,pendingAsync:pending,value:v,error:e}}));})();";
      target.appendChild(s);
      s.remove();
    } catch (_) {}
    window.removeEventListener(eventId, handler);

    if (captured.pendingAsync) {
      asyncTimer = setTimeout(() => {
        finishAsync({ done: true, value: null, error: "Async eval timeout (25000ms)" });
      }, 25000);
      return captured;
    }

    window.removeEventListener(asyncEventId, asyncHandler);

    if (!captured.done) {
      try {
        let res = (0, eval)(code);
        if (res && typeof res.then === "function") {
          asyncTimer = setTimeout(() => {
            finishAsync({ done: true, value: null, error: "Async eval timeout (25000ms)" });
          }, 25000);
          res.then(
            (r) => finishAsync({ done: true, value: serializeEvalVal(r), error: null }),
            (err2) => finishAsync({ done: true, value: null, error: String(err2 && err2.message ? err2.message : err2) })
          );
          return { done: true, pendingAsync: true };
        }
        captured.done = true;
        captured.value = serializeEvalVal(res);
      } catch (err2) {
        captured.done = true;
        captured.error = String(err2 && err2.message ? err2.message : err2);
      }
    }
    if (onDone) onDone(captured);
    return captured;
  }

  window.addEventListener("__fusion_gecko_console__", (ev) => {
    if (!port || !ev || !ev.detail) return;
    try {
      port.postMessage({
        type: "console",
        level: String(ev.detail.level || "LOG"),
        text: String(ev.detail.text || "")
      });
    } catch (_) {}
  });

  function installConsoleMirror() {
    const target = document.documentElement || document.head;
    if (!target || window.__FUSION_GECKO_CONSOLE_INSTALLED__) return;
    window.__FUSION_GECKO_CONSOLE_INSTALLED__ = true;
    try {
      const s = document.createElement("script");
      s.textContent =
        "(function(){if(window.__FG_CONSOLE_HOOK__)return;window.__FG_CONSOLE_HOOK__=true;" +
        "['log','info','warn','error'].forEach(function(lv){var orig=console[lv];if(typeof orig!=='function')return;" +
        "console[lv]=function(){try{var parts=[];for(var i=0;i<arguments.length;i++){var a=arguments[i];" +
        "parts.push(typeof a==='object'?JSON.stringify(a):String(a));}" +
        "window.dispatchEvent(new CustomEvent('__fusion_gecko_console__',{detail:{level:lv.toUpperCase(),text:parts.join(' ').slice(0,2048)}}));}catch(_){}" +
        "return orig.apply(console,arguments);};});})();";
      target.appendChild(s);
      s.remove();
    } catch (_) {}
  }

  function flushPendingQueue() {
    installConsoleMirror();
    if (!pendingQueue.length) return;
    if (!(document.documentElement || document.head)) return;
    const copy = pendingQueue;
    pendingQueue = [];
    for (let i = 0; i < copy.length; i++) {
      handleIncomingMessage(copy[i]);
    }
  }

  function handleIncomingMessage(msg) {
    installConsoleMirror();
    if (!msg || typeof msg !== "object") return;
    if (msg.type === "bootstrap" && typeof msg.code === "string") {
      const res = runInPageWorld(msg.code, false);
      if (res.deferred) {
        pendingQueue.push(msg);
      }
      return;
    }
    if (msg.type === "eval" && typeof msg.code === "string") {
      if (!(document.documentElement || document.head)) {
        pendingQueue.push(msg);
        return;
      }
      runInPageWorld(msg.code, !!msg.reqId, (res) => {
        if (msg.reqId && port) {
          try {
            port.postMessage({
              type: "evalResult",
              reqId: msg.reqId,
              value: res.value,
              error: res.error
            });
          } catch (_) {}
        }
      });
    }
  }

  function observeRootAndFlush() {
    if (document.documentElement || document.head) {
      flushPendingQueue();
      return;
    }
    const obs = new MutationObserver(() => {
      if (document.documentElement || document.head) {
        obs.disconnect();
        flushPendingQueue();
      }
    });
    try {
      obs.observe(document, { childList: true, subtree: true });
    } catch (_) {}
  }

  function connectBridge() {
    try {
      port = browser.runtime.connectNative("fusion");
      port.onMessage.addListener((msg) => {
        if (!msg) return;
        if (!(document.documentElement || document.head)) {
          pendingQueue.push(msg);
          observeRootAndFlush();
          return;
        }
        handleIncomingMessage(msg);
      });
      port.onDisconnect.addListener(() => {
        port = null;
      });
      try {
        port.postMessage({ type: "hello", url: location.href });
      } catch (_) {}
    } catch (_) {
      port = null;
    }
  }

  window.addEventListener("DOMContentLoaded", () => {
    flushPendingQueue();
    if (port) {
      try {
        port.postMessage({ type: "domReady", url: location.href });
      } catch (_) {}
    }
  }, { once: true });

  connectBridge();
})();
