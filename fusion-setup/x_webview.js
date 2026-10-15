const { chromium } = require('patchright');
const fs = require('fs');
const readline = require('readline');
const path = require('path');
const { exec } = require('child_process'); // <--- TAMBAHAN UNTUK EKSEKUSI MODUL
const http = require('http'); // <--- MODUL SERVER NOTIFIKASI THANOX

const url = require('url');

// --- NATIVE ANDROID WAKE LOCK ---
function acquireNativeWakeLock() {
    try {
        if (fs.existsSync('/sys/power/wake_lock')) {
            fs.writeFileSync('/sys/power/wake_lock', 'x_radar_lock');
            console.log(` \x1b[92m✅ Native Android Wake Lock diaktifkan (CPU dijaga agar tidak tidur).\x1b[0m`);
        }
    } catch (e) {}
}

function releaseNativeWakeLock() {
    try {
        if (fs.existsSync('/sys/power/wake_unlock')) {
            fs.writeFileSync('/sys/power/wake_unlock', 'x_radar_lock');
        }
    } catch (e) {}
}

// --- PIXEL AGENTS EVENT HOOK (x_pixel.js HQ) ---
function sendPixelEvent(agent, action, text, extra = {}) {
    try {
        const payload = JSON.stringify({ agent, action, text, extra });
        const req = http.request({
            hostname: '127.0.0.1',
            port: 4041,
            path: '/api/event',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload)
            },
            timeout: 200
        }, () => {});
        req.on('error', () => {});
        req.on('timeout', () => { req.destroy(); });
        req.write(payload);
        req.end();
    } catch (e) {}
}


// ==========================================
// 1. UI COLORS & HEADER STYLE
// ==========================================
const RESET = "\x1b[0m";
const WHITE = "\x1b[97m";
const RED = "\x1b[91m";
const GREEN = "\x1b[92m";
const YELLOW = "\x1b[93m";
const CYAN = "\x1b[96m";
const GRAY = "\x1b[90m";
const BOLD = "\x1b[1m";

function clear() {
    console.clear();
    process.stdout.write('\x1B[2J\x1B[3J\x1B[H');
}

function title() {
    console.log(`${CYAN}███████╗███████╗ ██████╗ ${RESET}`);
    console.log(`${CYAN}██╔════╝██╔════╝██╔═══██╗${RESET}`);
    console.log(`${CYAN}█████╗  ███████╗██║   ██║${RESET}`);
    console.log(`${CYAN}██╔══╝  ╚════██║██║   ██║${RESET}`);
    console.log(`${CYAN}███████╗███████║╚██████╔╝${RESET}`);
    console.log(`${CYAN}╚══════╝╚══════╝ ╚═════╝ ${RESET}`);
    console.log(`${RED} ██╗  ██╗    ██████╗  █████╗ ██████╗  █████╗ ██████╗ ${RESET}`);
    console.log(`${RED} ╚██╗██╔╝    ██╔══██╗██╔══██╗██╔══██╗██╔══██╗██╔══██╗${RESET}`);
    console.log(`${RED}  ╚███╔╝     ██████╔╝███████║██║  ██║███████║██████╔╝${RESET}`);
    console.log(`${RED}  ██╔██╗     ██╔══██╗██╔══██║██║  ██║██╔══██║██╔══██╗${RESET}`);
    console.log(`${RED} ██╔╝ ██╗    ██║  ██║██║  ██║██████╔╝██║  ██║██║  ██║${RESET}`);
    console.log(`${RED} ╚═╝  ╚═╝    ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝${RESET}`);
    console.log(`\n ${GRAY}${BOLD}X RADAR WEBVIEW NATIVE AI POWERED${RESET}\n`);
}

// ==========================================
// 2. CONFIG & FILE PATHS
// ==========================================
const QUERY_FILE = path.join(__dirname, 'queries.json');
const MODULE_FILE = path.join(__dirname, 'modules.json'); // <--- TAMBAHAN
const BLACKLIST_FILE = path.join(__dirname, 'blacklists.json');
const currentProfileArg = process.argv.slice(2).findIndex(a => a === '--profile');
const currentProfileInit = currentProfileArg > -1 && process.argv.slice(2)[currentProfileArg + 1] && !process.argv.slice(2)[currentProfileArg + 1].startsWith('-') ? process.argv.slice(2)[currentProfileArg + 1] : null;
const SESSION_FILE = currentProfileInit ? path.join(__dirname, 'sessions', currentProfileInit + '.json') : path.join(__dirname, 'x_session.json'); 
const WATCHLIST_FILE = path.join(__dirname, 'watchlist.json'); // <--- DAFTAR AKUN PANTAUAN NOTIF
const FILTER_CONFIG_FILE = path.join(__dirname, 'filter_config.json');
const SCANKOMEN_TRIGGER_FILE = path.join(__dirname, 'scankomen_trigger.json'); // <--- CONFIG AUTO-SCAN TRIGGER

// Default konfigurasi Auto-Scan Komen Trigger
let scanKomenTriggerConfig = { active: false, tanpaAkun: false, keywords: [] };
if (fs.existsSync(SCANKOMEN_TRIGGER_FILE)) {
    try { scanKomenTriggerConfig = { ...scanKomenTriggerConfig, ...JSON.parse(fs.readFileSync(SCANKOMEN_TRIGGER_FILE, 'utf8')) }; } catch(e){}
}
const saveScanKomenTriggerConfig = () => fs.writeFileSync(SCANKOMEN_TRIGGER_FILE, JSON.stringify(scanKomenTriggerConfig, null, 2));

// Default konfigurasi filter terpadu
let filterConfig = {
    linkFilterActive: false,
    codeFilterActive: true,
    autoTranslateActive: false, // <--- TAMBAHAN AUTO-TRANSLATE
    translateTargets: [],       // <--- TAMBAHAN CETAKAN KODE BENAR
    sampleCodes: ["TONV4JHS"], // Menyimpan contoh-contoh kode acak
    codeRegex: ""
};
if (fs.existsSync(FILTER_CONFIG_FILE)) {
    try { filterConfig = { ...filterConfig, ...JSON.parse(fs.readFileSync(FILTER_CONFIG_FILE, 'utf8')) }; } catch(e){}
    // Backward compatibility dari versi digit lama
    if ((filterConfig.codeDigits || filterConfig.codeDigit) && (!filterConfig.sampleCodes || filterConfig.sampleCodes.length === 0)) {
        const legacyDigits = filterConfig.codeDigits || [filterConfig.codeDigit || 8];
        filterConfig.sampleCodes = legacyDigits.map(d => "A".repeat(d)); // Konversi otomatis
    }
}
const saveFilterConfig = () => fs.writeFileSync(FILTER_CONFIG_FILE, JSON.stringify(filterConfig, null, 2));

// Fungsi Smart Regex: Menghitung panjang asli & mendeteksi simbol khusus secara akurat
function parseSampleCodeToRegex(sampleCode) {
    const clean = sampleCode.trim();
    if (!clean) return null;
    
    // Ubah setiap tanda '?' menjadi persis 1 digit huruf/angka alfanumerik [a-zA-Z0-9]
    let regexStr = '';
    for (let char of clean) {
        if (char === '?') {
            regexStr += '[a-zA-Z0-9]';
        } else if (char === '-' || char === '_') {
            regexStr += '\\' + char;
        } else {
            regexStr += char.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
        }
    }
    
    return {
        code: clean,
        type: clean.includes('?') ? 'EXACT-WILDCARD' : 'STRUKTUR',
        length: `${clean.length} char`,
        regexStr: `(?:(?<=^|[^a-zA-Z0-9\\-_]))${regexStr}(?=(?:[^a-zA-Z0-9\\-_]|$))`
    };
}

// Fungsi menggabungkan seluruh contoh kode menjadi satu regex dinamis
function getCombinedCodeRegex() {
    const samples = (filterConfig.sampleCodes && filterConfig.sampleCodes.length > 0) 
        ? filterConfig.sampleCodes 
        : ["TONV4JHS"];
        
    const regexParts = samples.map(s => {
        const parsed = parseSampleCodeToRegex(s);
        return parsed ? parsed.regexStr : null;
    }).filter(Boolean);
    
    if (regexParts.length === 0) return "(?:(?<=^|[^a-zA-Z0-9\\-_]))[a-zA-Z0-9\\-_]{6,25}(?=(?:[^a-zA-Z0-9\\-_]|$))";
    return regexParts.join('|');
}

// Fungsi mengekstrak kode dari Teks Tweet + Link URL sekaligus
function extractCodesFromText(text, urls = []) {
    try {
        // Bersihkan link internal bawaan Twitter (t.co / x.com) DAN Mention (@username) agar tidak ikut tersedot
        const cleanText = text
            .replace(/https?:\/\/(www\.)?(t\.co|x\.com|twitter\.com)\/[^\s]+/gi, '')
            .replace(/@[a-zA-Z0-9_]+/g, ''); // Hapus semua mention username
            
        const fullTextToScan = cleanText + " " + urls.join(" ");
        
        const combinedRegexStr = getCombinedCodeRegex();
        const regex = new RegExp(combinedRegexStr, 'gi'); 
        const matches = fullTextToScan.match(regex) || [];
        
        // Filter nama domain / keyword umum agar tidak masuk sebagai kode
        const ignoredWords = ['https', 'http', 'gate.com', 'gatesites', 'campaigns', 'mid-autumn', 'signup', 'message', 'twitter', 'status', 't.co', 'x.com', 'www'];
        const cleanMatches = matches.filter(m => !ignoredWords.some(w => m.toLowerCase().includes(w)));
        
        const cleanCodes = [...new Set(cleanMatches)];
        if (cleanCodes.length > 0) {
            sendPixelEvent('cipher', 'code_found', `KODE: ${cleanCodes.join(', ')}`, { code: cleanCodes[0] });
        }
        return cleanCodes;
    } catch(e) {
        return [];
    }
}

// --- FITUR AUTO-TRANSLATE KODE ---
function applyAutoTranslate(extractedCodes) {
    if (!filterConfig.autoTranslateActive || !filterConfig.translateTargets || filterConfig.translateTargets.length === 0) {
        return extractedCodes.map(code => ({ original: code, translated: code, isTranslated: false }));
    }

    return extractedCodes.map(code => {
        let finalCode = code;
        let isTranslated = false;

        for (let target of filterConfig.translateTargets) {
            // Ambil huruf awalan sebelum simbol (Contoh dari GATE- diambil GATE)
            const targetPrefixMatch = target.match(/^[a-zA-Z0-9]+/);
            if (!targetPrefixMatch) continue;

            const targetPrefix = targetPrefixMatch[0];
            const targetSeparator = target.substring(targetPrefix.length); // Memisahkan "-"

            // Pecah kode tangkapan berdasarkan format: [Prefix][SimbolAneh][SisaKode]
            const codeRegex = new RegExp(`^(${targetPrefix})([^a-zA-Z0-9]+)([a-zA-Z0-9]+)$`, 'i');
            const match = finalCode.match(codeRegex);

            if (match) {
                // match[2] adalah simbol yang tertangkap (Misal: @, _, atau spasi)
                if (match[2] !== targetSeparator) {
                    finalCode = match[1] + targetSeparator + match[3]; // Di-setrika!
                    isTranslated = true;
                    break;
                }
            }
        }
        return { original: code, translated: finalCode, isTranslated: isTranslated };
    });
}

function evaluateUnifiedFilter(tweet) {
    const hasLink = tweet.urls && tweet.urls.length > 0;
    
    let hasCode = false;
    if (filterConfig.codeFilterActive) {
        const combinedRegexStr = getCombinedCodeRegex();
        // Tanpa flag 'g', pakai flag 'i' untuk besar-kecil huruf, sapu link t.co DAN username @
        const customRegex = new RegExp(combinedRegexStr, 'i');
        const cleanText = (tweet.text || "")
            .replace(/https?:\/\/(www\.)?(t\.co|x\.com|twitter\.com)\/[^\s]+/gi, '')
            .replace(/@[a-zA-Z0-9_]+/g, ''); // Hapus semua mention username
        const fullScan = cleanText + " " + (tweet.urls || []).join(" ");
        hasCode = customRegex.test(fullScan);
    }

    // Jika kedua filter dinonaktifkan (dimatikan), otomatis lolos
    if (!filterConfig.linkFilterActive && !filterConfig.codeFilterActive) {
        return { passed: true, reason: 'none' };
    }

    // [LOGIKA OR] Cek apakah Link Filter aktif DAN postingan punya link
    const passLink = filterConfig.linkFilterActive && hasLink;
    
    // [LOGIKA OR] Cek apakah Code Filter aktif DAN postingan punya kode yang cocok
    const passCode = filterConfig.codeFilterActive && hasCode;

    // Jika SALAH SATU terpenuhi, langsung nyatakan LOLOS (OR)
    if (passLink || passCode) {
        return { passed: true, reason: passLink ? 'link' : 'code' };
    }

    // Jika keduanya aktif tapi postingan tidak punya link maupun kode yang cocok, baru digagalkan
    return { passed: false, reason: 'failed' };
}

// --- EKSEKUSI MODUL INDEPENDEN (MIRIP PEMANTAUTELEGRAM.PY) ---
async function status_check() {
    let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
    let modules = fs.existsSync(MODULE_FILE) ? JSON.parse(fs.readFileSync(MODULE_FILE, 'utf-8')) : [];
    let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
    let accs = fs.existsSync(X_ACCOUNTS_FILE) ? JSON.parse(fs.readFileSync(X_ACCOUNTS_FILE, 'utf-8')) : [];
    let watchlists = fs.existsSync(WATCHLIST_FILE) ? JSON.parse(fs.readFileSync(WATCHLIST_FILE, 'utf-8')) : [];

    const tgInfo = await getCachedTelegramInfo();
    const botOk = Boolean(tgConfig.bot_token && tgConfig.chat_id && tgInfo.bot.id !== 'Unknown');
    const icBot = botOk ? `${GREEN}✅${RESET}` : `${RED}❌${RESET}`;

    console.log(`\n ${CYAN}STATUS CONFIGURATION${RESET}`);
    console.log(` │`);
    console.log(` ├─ [${icBot}] API Telegram : ${tgInfo.bot.username || tgInfo.bot.name}`);
    console.log(` ├─ [${icBot}] Bot Token    : ${tgInfo.bot.name}`);
    
    const chatIdsCount = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id.length : (tgConfig.chat_id ? 1 : 0);
    console.log(` ├─ [${icBot}] Chat ID      : ${chatIdsCount > 0 ? `${chatIdsCount} Terdaftar` : 'Kosong'}`);
    console.log(` │   └─ ${tgInfo.chat.name}`);
    console.log(` │`);
    
    // --- QUERY DIPANTAU (X SEARCH) ---
    console.log(` ├─ Query Dipantau : ${GREEN}${queries.length}${RESET} Terdaftar`);
    if (queries.length === 0) {
        console.log(` │   └─ [-] Belum ada query`);
    } else {
        queries.forEach((q, i) => {
            const prefix = i === queries.length - 1 ? ' │   └─' : ' │   ├─';
            console.log(`${prefix} ${q}`);
        });
    }
    console.log(` │`);

    // --- RELASI MODUL (INDEPENDEN) ---
    console.log(` ├─ Relasi Modul   : ${GREEN}${modules.length}${RESET} Siap Eksekusi`);
    if (modules.length === 0) {
        console.log(` │   └─ [-] Belum ada modul terikat`);
    } else {
        modules.forEach((m, i) => {
            const prefix = i === modules.length - 1 ? ' │   └─' : ' │   ├─';
            console.log(`${prefix} ${m.module} > ${m.query}`);
        });
    }
    console.log(` │`);

    // --- BLACKLIST & AKUN X ---
    console.log(` ├─ Blacklist Word : ${GREEN}${blacklists.length}${RESET} Kata`);
    console.log(` └─ Akun X Login   : ${GREEN}${accs.length}${RESET} Akun\n`);
}

const AI_CONFIG_FILE = path.join(__dirname, 'ai_config.json');
const TG_CONFIG_FILE = path.join(__dirname, 'tg_config.json');
const TG_CACHE_FILE = path.join(__dirname, 'tg_cache.json');

// --- INISIALISASI DATABASE SQLITE UNTUK SENT HISTORY (FIFO MAX 100) ---
const Database = require('better-sqlite3');
const SENT_HISTORY_DB = path.join(__dirname, 'sent_history.db');
const db = new Database(SENT_HISTORY_DB);

db.exec(`
    CREATE TABLE IF NOT EXISTS sent_history (
        tweet_id TEXT PRIMARY KEY,
        timestamp INTEGER
    )
`);

global.tgCacheRAM = null;

// Fungsi Caching Bot & User ID (Update otomatis tiap 7 hari)
async function getCachedTelegramInfo() {
    if (!global.tgCacheRAM) {
        if (fs.existsSync(TG_CACHE_FILE)) {
            try { global.tgCacheRAM = JSON.parse(fs.readFileSync(TG_CACHE_FILE, 'utf-8')); } catch(e){ global.tgCacheRAM = {}; }
        } else {
            global.tgCacheRAM = {};
        }
    }
    
    let cache = global.tgCacheRAM;
    const now = Date.now();
    const sevenDays = 7 * 24 * 60 * 60 * 1000;
    
    const chatIds = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id : [tgConfig.chat_id].filter(Boolean);
    const currentIdsString = chatIds.join(', ');

    if (cache.bot && cache.chat && cache.timestamp && (now - cache.timestamp < sevenDays)) {
        if (cache.chat.id === currentIdsString) return cache;
    }

    let botInfo = { id: 'Unknown', name: 'Unknown', username: 'Unknown' };
    let chatInfo = { id: currentIdsString, name: chatIds.length > 0 ? `${chatIds.length} Terdaftar` : 'Kosong' };
    
    const token = tgConfig.bot_token ? tgConfig.bot_token.trim() : '';

    if (token) {
        try {
            const resMe = await fetch(`https://api.telegram.org/bot${token}/getMe`);
            const dataMe = await resMe.json();
            if (dataMe.ok) botInfo = { id: dataMe.result.id, name: dataMe.result.first_name, username: dataMe.result.username ? `@${dataMe.result.username}` : dataMe.result.first_name };

            if (chatIds.length > 0) {
                let names = [];
                for (let cid of chatIds) {
                    try {
                        const resChat = await fetch(`https://api.telegram.org/bot${token}/getChat?chat_id=${cid}`);
                        const dataChat = await resChat.json();
                        if (dataChat.ok) {
                            const uname = dataChat.result.username ? `@${dataChat.result.username}` : null;
                            const fname = dataChat.result.first_name || dataChat.result.title;
                            names.push(uname || fname || cid);
                        } else names.push(cid);
                    } catch(e) { names.push(cid); }
                }
                const formattedNames = names.map(n => n.toString().startsWith('@') || n.toString().match(/^[0-9\-]+$/) ? n : `@${n.replace(/\s/g,'_')}`).join(', ');
                chatInfo = { id: chatIds.join(', '), name: formattedNames };
            }
        } catch(e) {}
    }

    global.tgCacheRAM = { bot: botInfo, chat: chatInfo, timestamp: now };
    fs.writeFile(TG_CACHE_FILE, JSON.stringify(global.tgCacheRAM, null, 2), () => {}); // ASYNC Write
    return global.tgCacheRAM;
}
const X_ACCOUNTS_FILE = path.join(__dirname, 'x_accounts.json');
const AI_SELECTORS_FILE = path.join(__dirname, 'ai_selectors.json');

global.cachedQueries = [];
global.cachedModules = [];
global.cachedBlacklists = [];
global.cachedWatchlists = [];

const loadCache = (file) => {
    try { return fs.existsSync(file) ? JSON.parse(fs.readFileSync(file, 'utf-8')) : []; } catch(e) { return []; }
};

global.cachedQueries = loadCache(QUERY_FILE);
global.cachedModules = loadCache(MODULE_FILE);
global.cachedBlacklists = loadCache(BLACKLIST_FILE);
global.cachedWatchlists = loadCache(WATCHLIST_FILE);

const watchFileToRAM = (file, globalVar) => {
    if (!fs.existsSync(file)) fs.writeFileSync(file, JSON.stringify([]));
    let timeout = null;
    fs.watch(file, (eventType) => {
        if (eventType === 'change') {
            if (timeout) clearTimeout(timeout);
            // Cegah multiple read I/O akibat duplikasi sinyal dari OS
            timeout = setTimeout(() => {
                fs.readFile(file, 'utf-8', (err, data) => {
                    if (!err) { try { global[globalVar] = JSON.parse(data); } catch(e){} }
                });
            }, 300); 
        }
    });
};

watchFileToRAM(QUERY_FILE, 'cachedQueries');
watchFileToRAM(MODULE_FILE, 'cachedModules');
watchFileToRAM(BLACKLIST_FILE, 'cachedBlacklists');
watchFileToRAM(WATCHLIST_FILE, 'cachedWatchlists');

const LOOP_STATE_FILE = path.join(__dirname, 'loop_state.json');
global.persistentLoop = 0;
if (fs.existsSync(LOOP_STATE_FILE)) {
    try { global.persistentLoop = JSON.parse(fs.readFileSync(LOOP_STATE_FILE, 'utf8')).totalLoops || 0; } catch(e){}
}

const PROCESSED_TWEETS = new Set();
const PROCESSED_CODES = new Set(); // <--- TAMBAHAN: Memori untuk mencegah eksekusi kode/link duplikat
const SCANNING_TWEET_IDS = new Set(); // <--- Memori penanda post yang sedang/sudah discan agar tidak bentrok

// --- SISTEM ANTREAN (QUEUE) AUTOSCAN KOMEN (SEQUENTIAL / ANTI-OVERLOAD HP) ---
const scanKomenQueue = [];
let isScanQueueProcessing = false;

function enqueueScanKomen(targetUrl, chatId, tanpaAkun, customMaxLoops = 60) {
    const match = targetUrl.match(/status\/(\d+)/);
    const tweetId = match ? match[1] : targetUrl;

    if (SCANNING_TWEET_IDS.has(tweetId)) {
        console.log(` \x1b[93m⚠️ [SKIP SCAN] Post ${tweetId} sedang dalam antrean/proses scan. Melewati agar tidak bentrok.\x1b[0m`);
        return;
    }

    SCANNING_TWEET_IDS.add(tweetId);
    scanKomenQueue.push({ targetUrl, chatId, tanpaAkun, tweetId, customMaxLoops });
    console.log(` \x1b[96m📥 [QUEUE SCAN] Post ${tweetId} masuk antrean scan komen. Posisi antrean: ${scanKomenQueue.length}\x1b[0m`);

    processScanKomenQueue();
}

async function processScanKomenQueue() {
    if (isScanQueueProcessing || scanKomenQueue.length === 0) return;

    isScanQueueProcessing = true;
    const task = scanKomenQueue.shift();

    console.log(` \x1b[92m▶️ [QUEUE EXEC] Memulai scan komen berurutan untuk Post ID: ${task.tweetId} (Sisa antrean: ${scanKomenQueue.length})\x1b[0m`);

    let attempt = 0;
    const maxRetries = 3;
    let success = false;

    while (attempt < maxRetries && !success) {
        attempt++;
        try {
            if (attempt > 1) console.log(` \x1b[96m🔄 [SCAN KOMEN RETRY] Percobaan ke-${attempt}/${maxRetries} untuk Post ID: ${task.tweetId}\x1b[0m`);
            
            success = await runScanKomen(task.targetUrl, task.chatId, task.tanpaAkun, task.customMaxLoops);
            
            if (success) {
                await sendTelegramText(`✅ <b>Laporan Scan Komen: BERHASIL!</b>\n🎯 Target: <a href="${task.targetUrl}">Link Post</a>\n🔄 Diselesaikan pada percobaan ke-${attempt}.`);
            }
        } catch (err) {
            console.error(` \x1b[91m❌ [QUEUE ERROR] Error saat scan ${task.tweetId}:\x1b[0m`, err?.message || err);
        }

        if (!success && attempt < maxRetries) {
            if (!global.activeScanChats.get(task.chatId.toString())) {
                console.log(` \x1b[93m🛑 Dihentikan secara manual. Membatalkan antrean retry.\x1b[0m`);
                break;
            }
            console.log(` \x1b[93m⏳ Gagal/Timeout. Jeda 5 detik sebelum retry...\x1b[0m`);
            await new Promise(r => setTimeout(r, 5000));
        }
    }

    if (!success) {
        await sendTelegramText(`❌ <b>Laporan Scan Komen: GAGAL!</b>\n🎯 Target: <a href="${task.targetUrl}">Link Post</a>\n⚠️ Telah mencoba ${maxRetries}x namun gagal/timeout.`);
    }

    SCANNING_TWEET_IDS.delete(task.tweetId);
    isScanQueueProcessing = false;
    processScanKomenQueue();
}

// Load data awal dari SQLite ke Set RAM saat bot dinyalakan
const loadSentHistoryFromDB = () => {
    PROCESSED_TWEETS.clear();
    try {
        const rows = db.prepare('SELECT tweet_id FROM sent_history').all();
        rows.forEach(r => PROCESSED_TWEETS.add(r.tweet_id));
    } catch (e) {}
};
loadSentHistoryFromDB();

// Fungsi Simpan FIFO: Masukkan baru, pertahankan max 100, hapus yang terlama
const saveSentHistory = (id) => {
    if (PROCESSED_TWEETS.has(id)) return;
    
    try {
        const insertAndUpdate = db.transaction((tweetId) => {
            db.prepare('INSERT OR IGNORE INTO sent_history (tweet_id, timestamp) VALUES (?, ?)').run(tweetId, Date.now());
            db.prepare(`
                DELETE FROM sent_history WHERE tweet_id NOT IN (
                    SELECT tweet_id FROM sent_history ORDER BY timestamp DESC LIMIT 100
                )
            `).run();
        });
        insertAndUpdate(id);

        // Sinkronisasi ulang RAM Set
        loadSentHistoryFromDB();
    } catch (e) {}
};

// Karena SQLite langsung menulis secara aman per transaksi, fungsi commit disk JSON dikosongkan/dihapus
const commitSentHistoryToDisk = () => {};

let aiConfig = { baseUrl: 'https://api.deepseek.com/v1', apiKey: '', model: 'deepseek-chat' };
if (fs.existsSync(AI_CONFIG_FILE)) {
    try { aiConfig = { ...aiConfig, ...JSON.parse(fs.readFileSync(AI_CONFIG_FILE, 'utf8')) }; } catch(e){}
}

let tgConfig = { bot_token: '', chat_id: [] };
if (fs.existsSync(TG_CONFIG_FILE)) {
    try { 
        let loaded = JSON.parse(fs.readFileSync(TG_CONFIG_FILE, 'utf8'));
        if (loaded.chat_id && !Array.isArray(loaded.chat_id)) {
            loaded.chat_id = loaded.chat_id.toString().split(',').map(id => id.trim()).filter(Boolean);
        }
        tgConfig = { ...tgConfig, ...loaded }; 
    } catch(e){}
}
const saveTgConfig = () => fs.writeFileSync(TG_CONFIG_FILE, JSON.stringify(tgConfig, null, 2));

let aiSelectors = { username: null, password: null, email: null };
if (fs.existsSync(AI_SELECTORS_FILE)) {
    try { aiSelectors = { ...aiSelectors, ...JSON.parse(fs.readFileSync(AI_SELECTORS_FILE, 'utf8')) }; } catch(e){}
}
const saveAiSelectors = () => fs.writeFileSync(AI_SELECTORS_FILE, JSON.stringify(aiSelectors, null, 2));

global.uiLogs = { success: [], errors: [] };
global.isLogViewerOpen = false;

const args = process.argv.slice(2);
const hasArg = (flag) => args.includes(flag) || args.includes(flag.charAt(0) === '-' ? flag.substring(0, 2) : flag);
const getArg = (flag, def) => {
    const idx = args.findIndex(a => a === flag || a === (flag.startsWith('--') ? flag.substring(0, 2) : flag));
    return idx > -1 && args[idx + 1] && !args[idx + 1].startsWith('-') ? args[idx + 1] : def;
};
const currentProfile = getArg('--profile', null);

const extractMultiWords = (flag) => {
    const idx = args.findIndex(a => a === flag);
    if (idx === -1) return null;
    let parts = [];
    for(let i = idx + 1; i < args.length; i++) {
        if(args[i].startsWith('-')) break;
        parts.push(args[i]);
    }
    return parts.length > 0 ? parts.join(' ') : null;
};
const getQueryArg = () => extractMultiWords('--query');
const getBlacklistArg = () => extractMultiWords('--blacklist');

const ask = (query) => {
    const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    return new Promise(resolve => rl.question(query, ans => {
        rl.close();
        resolve(ans);
    }));
};

// Helper exec berbasis Promise agar eksekusi antri satu per satu (Anti-Tabrakan)
const execPromise = (cmd) => new Promise(resolve => exec(cmd, (error, stdout, stderr) => resolve({ error, stdout, stderr })));

// Helper cepat untuk cek pencocokan modul tanpa memblokir/menunggu eksekusi
function getMatchingModule(tweetData) {
    const modules = global.cachedModules || [];
    if (modules.length === 0) return null;
    const targetText = (tweetData.text + " " + (tweetData.urls || []).join(" ")).toLowerCase();
    return modules.find(m => m.query && targetText.includes(m.query.toLowerCase())) || null;
}

// --- EKSEKUSI MODUL INDEPENDEN (ANTRIAN SEQUENTIAL) ---
async function detectAndRunModule(tweetData) {
    const modules = global.cachedModules || [];
    if (modules.length === 0) return null;
    try {

        const targetText = (tweetData.text + " " + (tweetData.urls || []).join(" ")).toLowerCase();

        for (let m of modules) {
            if (!m.query) continue;
            const queryLower = m.query.toLowerCase();

            // Cek apakah postingan mengandung query modul
            if (targetText.includes(queryLower)) {
                
                // 1. Ambil semua kandidat kode dari teks & URL
                const extractedCodes = extractCodesFromText(tweetData.text, tweetData.urls || []);
                const translatedObj = applyAutoTranslate(extractedCodes);
                
                // 2. FILTER KETAT: Gunakan kode hasil Translate agar command modul valid di eksekusi
                let targets = translatedObj.map(obj => obj.translated).filter(code => code.toLowerCase().includes(queryLower));
                
                // Fallback 1: Jika tidak ada kode regex yang cocok dengan m.query, cek apakah ada link URL yang cocok dengan m.query
                if (targets.length === 0 && tweetData.urls && tweetData.urls.length > 0) {
                    targets = [...new Set(tweetData.urls)].filter(url => url.toLowerCase().includes(queryLower));
                }

                // Fallback 2: Jika m.query ada di dalam teks biasa, gunakan postLink X
                if (targets.length === 0) {
                    targets = [tweetData.postLink];
                }

                console.log(` \x1b[96m⚙️ [MODUL] Trigger "${m.query}" cocok! Ditemukan ${targets.length} target valid yang sesuai modul. Memulai eksekusi...\x1b[0m`);

                // 3. EKSEKUSI BERURUTAN (Hanya untuk target yang valid & lolos relasi modules.json)
                for (let i = 0; i < targets.length; i++) {
                    const argToSend = targets[i];

                    // --- FILTER DUPLIKASI KODE / LINK ---
                    if (PROCESSED_CODES.has(argToSend)) {
                        console.log(` \x1b[93m⚠️ [SKIP] Target '${argToSend}' sudah pernah dieksekusi sebelumnya. Mengabaikan spam/duplikat.\x1b[0m`);
                        continue;
                    }
                    PROCESSED_CODES.add(argToSend);
                    // ------------------------------------

                    const fullCmd = `zsh -ic "${m.module} '${argToSend}'"`;
                    
                    console.log(` \x1b[96m⏳ [ANTRIAN ${i + 1}/${targets.length}] Menjalankan: ${m.module} '${argToSend}'\x1b[0m`);

                    const { error, stdout } = await execPromise(fullCmd);

                    let resultMsg = `🤖 <b>HASIL EKSEKUSI MODUL [${i + 1}/${targets.length}]</b>\n`;
                    resultMsg += `📌 <b>Modul:</b> <code>${m.module}</code>\n`;
                    resultMsg += `🔑 <b>Argumen:</b> <code>${argToSend}</code>\n`;
                    resultMsg += `🔗 <b>Post:</b> <a href="${tweetData.postLink}">Buka di X</a>\n\n`;

                    if (error) {
                        console.log(` \x1b[91m❌ [MODUL ERROR] ${error.message}\x1b[0m`);
                        resultMsg += `❌ <b>Status: Gagal Error</b>\n<pre>${error.message}</pre>`;
                    } else {
                        if (stdout) console.log(`\n \x1b[92m[OUTPUT MODUL]\n${stdout}\x1b[0m`);
                        const cleanOutput = (stdout || "").trim() || "Eksekusi selesai tanpa output teks.";
                        resultMsg += `✅ <b>Status: Sukses</b>\n<pre>${cleanOutput}</pre>`;
                    }

                    await sendTelegramText(resultMsg);
                }

                return m;
            }
        }
    } catch (e) {
        console.log(` \x1b[91m❌ Gagal membaca modules.json: ${e.message}\x1b[0m`);
    }
    return null;
}

// Fungsi Ekstraksi Keyword 100% Stabil (Original x_beta.js Regex)
function extractKeywords(queryString) {
    const regex = /"([^"]+)"|'([^']+)'/g;
    let match;
    const keywords = [];
    while ((match = regex.exec(queryString)) !== null) {
        keywords.push(match[1] || match[2]);
    }
    if (keywords.length === 0) {
        return queryString.split(/\s+or\s+/i).map(s => s.replace(/[()]/g, '').trim()).filter(s => s);
    }
    return keywords;
}

function formatWIB(dateString) {
    if (!dateString) return "Waktu tidak diketahui";
    try {
        const date = new Date(dateString);
        return date.toLocaleString('id-ID', { 
            timeZone: 'Asia/Jakarta', weekday: 'long', year: 'numeric', month: 'long', 
            day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' 
        }) + ' WIB';
    } catch (e) { return dateString; }
}

// Helper jeda waktu anti-spam (anti-flood telegram)
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function sendTelegramText(text) {
    if (!tgConfig.bot_token || !tgConfig.chat_id) return;
    let chatIds = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id : [tgConfig.chat_id].filter(Boolean);
    
    for (let chatId of chatIds) {
        let maxRetries = 3;
        let attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            try {
                const res = await fetch(`https://api.telegram.org/bot${tgConfig.bot_token}/sendMessage`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        chat_id: chatId,
                        text: text,
                        parse_mode: 'HTML',
                        disable_web_page_preview: true
                    })
                });
                const data = await res.json();
                if (data.ok) break;

                // Penanganan otomatis jika terkena Limit 429 Telegram
                if (data.error_code === 429) {
                    const retrySec = data.parameters?.retry_after || 5;
                    console.log(` \x1b[91m⏳ [TELEGRAM LIMIT 429] Terkena rate limit! Delay ${retrySec}s sebelum retry...\x1b[0m`);
                    await delay((retrySec + 1) * 1000);
                }
            } catch(e) {
                if (attempt < maxRetries) await delay(2000);
            }
        }
        await delay(500); // Jeda anti flood antar user
    }
}

async function sendTelegramAlert(tweet, matchString, moduleNote = "") {
    if (!tgConfig.bot_token || !tgConfig.chat_id) return { success: false, error: 'Config Telegram Kosong' };
    let chatIds = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id : [tgConfig.chat_id].filter(Boolean);
    let lastResult = { success: false };

    const safeText = tweet.text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').substring(0, 1000);
    
    // Format link agar BISA DIKLIK di Telegram (Aman dari block Error 400 HTML Telegram)
    let uniqueUrls = [...new Set(tweet.urls)];
    // Filter cerdas: Buang link pendek/terpotong yang merupakan bagian dari link lain yang lebih lengkap
    uniqueUrls = uniqueUrls.filter(u => !uniqueUrls.some(other => other !== u && other.includes(u)));

    const linkText = uniqueUrls.length > 0 
        ? uniqueUrls.map(u => {
            const fullUrl = u.startsWith('http') ? u : `https://${u}`;
            // Mencegah API Telegram menolak pesan akibat simbol & atau < > di dalam URL
            const safeUrl = fullUrl.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            const safeDisplay = u.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            return `🔗 <a href="${safeUrl}">${safeDisplay}</a>`;
        }).join('\n') 
        : 'Tidak ada eksternal link';
    
    // Format kode dengan tag <code> agar BISA DI-COPY SEKALI KLIK di Telegram
    const extractedCodes = extractCodesFromText(tweet.text, tweet.urls || []);
    const translatedObj = applyAutoTranslate(extractedCodes);
    
    let codeText = 'Tidak ada kode terekstrak';
    if (translatedObj.length > 0) {
        codeText = translatedObj.map(obj => {
            if (obj.isTranslated) {
                return `❌ <s>${obj.original}</s>\n✅ <code>${obj.translated}</code>`;
            } else {
                return `<code>${obj.original}</code>`;
            }
        }).join('\n\n');
    }

    const message = `🚨 <b>X Search query cocok!</b>\n\n` +
                    `🎯 <b>Match:</b> ${matchString}\n` +
                    `🔗 <b>Post:</b> <a href="${tweet.postLink}">Buka di X</a>\n` +
                    `📝 <b>Teks:</b> \n<i>${safeText}...</i>\n\n` +
                    `🌐 <b>Link Terekstrak:</b>\n${linkText}\n\n` +
                    `🔑 <b>Kode Terekstrak (Klik untuk Copy):</b>\n${codeText}\n\n` +
                    `${moduleNote}` +
                    `🕒 <i>${formatWIB(tweet.createdAt)}</i>`;

    for (let chatId of chatIds) {
        let maxRetries = 3; // Maksimal percobaan ulang jika fetch failed
        let attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                let response, resJson;
                if (tweet.mediaUrls && tweet.mediaUrls.length > 0) {
                    if (tweet.mediaUrls.length === 1) {
                        const url = `https://api.telegram.org/bot${tgConfig.bot_token}/sendPhoto`;
                        response = await fetch(url, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                chat_id: chatId,
                                photo: tweet.mediaUrls[0],
                                caption: message,
                                parse_mode: 'HTML'
                            })
                        });
                    } else {
                        const url = `https://api.telegram.org/bot${tgConfig.bot_token}/sendMediaGroup`;
                        const mediaArray = tweet.mediaUrls.slice(0, 10).map((mUrl, idx) => ({
                            type: 'photo',
                            media: mUrl,
                            caption: idx === 0 ? message : undefined,
                            parse_mode: idx === 0 ? 'HTML' : undefined
                        }));
                        response = await fetch(url, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                chat_id: chatId,
                                media: mediaArray
                            })
                        });
                    }
                } else {
                    const url = `https://api.telegram.org/bot${tgConfig.bot_token}/sendMessage`;
                    response = await fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            chat_id: chatId,
                            text: message,
                            parse_mode: 'HTML',
                            disable_web_page_preview: true
                        })
                    });
                }

                resJson = await response.json();
                await delay(1000); // Jeda anti-spam

                const tgInfo = await getCachedTelegramInfo();
                if (resJson.ok) {
                    lastResult = { success: true, ...tgInfo };
                    break;
                } else {
                    const isRateLimit = resJson.error_code === 429 || (resJson.description && resJson.description.toLowerCase().includes('too many requests'));
                    if (isRateLimit) {
                        const retrySec = resJson.parameters?.retry_after || 5;
                        console.log(` \x1b[91m⏳ [TELEGRAM LIMIT 429] Terkena rate limit! Menunggu ${retrySec}s sebelum retry (Percobaan ${attempt}/${maxRetries})...\x1b[0m`);
                        await delay((retrySec + 1) * 1000);
                        continue; // Coba kirim ulang
                    }
                    lastResult = { success: false, error: resJson.description, ...tgInfo };
                    break;
                }
            } catch (e) {
                if (attempt < maxRetries) {
                    if (!global.isLogViewerOpen && hasArg('--debug')) console.log(` \x1b[93m⚠️ [TELEGRAM] Gagal kirim (Percobaan ${attempt}/${maxRetries}), mencoba ulang dalam 3 detik...\x1b[0m`);
                    await delay(3000); // Tunggu 3 detik sebelum retry
                } else {
                    const tgInfo = await getCachedTelegramInfo();
                    lastResult = { success: false, error: e.message, ...tgInfo };
                }
            }
        }
        await delay(500); // Jeda aman antar user
    }
    return lastResult;
}

// ==========================================
// TELEGRAM COMMAND LISTENER (REMOTE CONTROL)
// ==========================================


// ==========================================
// Variable to track if THIS script process launched the WebView instance
let launchedByThisScript = false;

// ==========================================
// ANDROID WEBVIEW CDP ENGINE MANAGER
// ==========================================
async function ensureWebViewCDP(options = {}) {
    const profile = options.profile !== undefined ? options.profile : currentProfile;
    const withoutAccount = options.withoutAccount || false;
    const targetUrl = options.url || 'https://x.com/home';
    const isHeadless = hasArg('--headless');

    // 1. Cek apakah ada halaman aktif di port 9222
    let hasActivePage = false;
    try {
        const pagesList = await fetch('http://127.0.0.1:9222/json/list', { timeout: 1500 }).then(r => r.json()).catch(() => []);
        if (Array.isArray(pagesList) && pagesList.some(p => p.type === 'page')) {
            hasActivePage = true;
        }
    } catch (e) {}

    // 2. Jika BELUM ada halaman aktif sama sekali di memori:
    // Panggil termux-ipc webview agar Activity menaikkan WebView ke layar HP secara visual!
    if (!hasActivePage) {
        launchedByThisScript = true;
        try {
            const { execSync } = require('child_process');
            const profFlag = (!withoutAccount && profile) ? `--profile ${profile}` : (withoutAccount ? '--fresh' : '');
            const ipcBin = fs.existsSync('/usr/local/termux-shims/termux-ipc') ? '/usr/local/termux-shims/termux-ipc' : 'termux-ipc';
            
            console.log(` \x1b[96m📱 [UI LAUNCH] Membuka tampilan WebView di layar HP (Profil: ${profile || 'Default'})...\x1b[0m`);
            execSync(`${ipcBin} webview ${profFlag} ${targetUrl}`, { stdio: 'ignore' });
            
            // Tunggu 2 detik agar Activity & WebView selesai render di layar
            await new Promise(r => setTimeout(r, 2000));

            // Pastikan port forwarding ADB aktif
            const unixOut = execSync("adb -s 127.0.0.1:5555 shell 'cat /proc/net/unix | grep devtools' 2>/dev/null").toString();
            const match = unixOut.match(/@(webview_devtools_remote_\d+)/);
            if (match) {
                execSync(`adb -s 127.0.0.1:5555 forward tcp:9222 localabstract:${match[1]}`);
            }
        } catch (err) {
            console.error(` \x1b[91m⚠️ [WEBVIEW LAUNCH] Gagal membuka WebView: ${err.message}\x1b[0m`);
        }
    } else {
        launchedByThisScript = false;
        console.log(` \x1b[96m📱 [ATTACH] Terhubung ke WebView yang sudah aktif berjalan (tidak di-clear)...\x1b[0m`);
    }

    // 3. Hubungkan Playwright/Patchright ke Android WebView via CDP
    let browser = global.activeBrowserInstance;
    if (!browser) {
        browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
        global.activeBrowserInstance = browser;
    }

    const contexts = browser.contexts();
    if (!contexts || contexts.length === 0) {
        throw new Error("Konteks browser WebView belum siap.");
    }
    const context = contexts[0];
    global.activeBrowserContext = context;

    // Tunggu sampai halaman aktif terdeteksi (maks 10 detik)
    let page = null;
    for (let i = 0; i < 20; i++) {
        const pages = context.pages();
        page = pages.find(p => p.url().includes('x.com')) || pages[0];
        if (page) break;
        await new Promise(r => setTimeout(r, 500));
    }

    if (!page) {
        throw new Error("Tidak ada tab WebView yang aktif di layar.");
    }
    global.activeCurrentPage = page;

    return { browser, context, page };
}

// Clean Exit Handler: HANYA bersihkan WebView jika skrip ini yang meluncurkannya!
const handleCleanExit = (sig) => {
    if (launchedByThisScript) {
        console.log(`\n \x1b[93m🧹 [CLEAN EXIT] Skrip peluncur ditutup (${sig}). Menghancurkan WebView di background...\x1b[0m`);
        try {
            const { execSync } = require('child_process');
            const ipcBin = fs.existsSync('/usr/local/termux-shims/termux-ipc') ? '/usr/local/termux-shims/termux-ipc' : 'termux-ipc';
            execSync(`${ipcBin} webview WEBVIEW_CLOSE`, { stdio: 'ignore' });
        } catch (e) {}
    } else {
        console.log(`\n \x1b[92mℹ️ [EXIT] Memutus CDP (${sig}). WebView tetap dibiarkan aktif untuk proses lain.\x1b[0m`);
    }
    process.exit(0);
};

process.on('SIGINT', () => handleCleanExit('SIGINT'));
process.on('SIGTERM', () => handleCleanExit('SIGTERM'));

// Global Map untuk menyimpan sinyal stop per chat
global.activeScanChats = global.activeScanChats || new Map();

async function runScanKomen(targetUrl, chatId, tanpaAkun = false, customMaxLoops = 60) {
    try {
        const botToken = tgConfig.bot_token;
        const notify = async (msg) => {
            await fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ chat_id: chatId, text: msg, parse_mode: 'HTML', disable_web_page_preview: true })
            }).catch(()=>{});
        };

        global.activeScanChats.set(chatId.toString(), true);

        // In WebView engine: buka worker tab khusus di background via IPC agar Radar Tab 1 tidak terganggu
        const ipcBin = fs.existsSync('/usr/local/termux-shims/termux-ipc') ? '/usr/local/termux-shims/termux-ipc' : 'termux-ipc';
        try {
            require('child_process').execSync(`${ipcBin} webview WEBVIEW_WORKER ${targetUrl}`, { stdio: 'ignore' });
            await new Promise(r => setTimeout(r, 1500));
        } catch(e) {}

        const conn = await ensureWebViewCDP({ withoutAccount: tanpaAkun });
        let browser = conn.browser;
        let context = conn.context;
        const allPages = context.pages();
        let page = allPages.length > 1 ? allPages[1] : conn.page;

        // Jika Guest Mode (Tanpa Akun): isolasi request dengan membersihkan cookie autentikasi
        if (tanpaAkun) {
            try {
                await page.route('**/*', async (route) => {
                    const req = route.request();
                    const headers = { ...req.headers() };
                    if (headers['cookie']) {
                        headers['cookie'] = headers['cookie']
                            .replace(/auth_token=[^;]+;?/g, '')
                            .replace(/ct0=[^;]+;?/g, '');
                    }
                    delete headers['authorization'];
                    await route.continue({ headers });
                });
            } catch(e) {}
        }

        let foundCode = false;
        let loopCount = 0;
        const maxLoops = customMaxLoops === 0 ? Infinity : customMaxLoops;
        let scanStats = { komentar: 0 }; // <--- Variabel penyimpan khusus komentar

        let isScanKomenRateLimited = false;

        const attachResponseListener = (targetPage) => {
            targetPage.on('response', async (response) => {
                if (foundCode || isScanKomenRateLimited) return;
                const urlReq = response.url();

            // Cek Status 429 pada Scan Komen
            if (response.status() === 429 && urlReq.includes('graphql')) {
                console.log(` \x1b[91m⚠️ [SCAN KOMEN RATE LIMIT] Terkena HTTP 429 dari Twitter!\x1b[0m`);
                isScanKomenRateLimited = true;
                return;
            }

            // Sadap endpoint tempat komentar X dimuat (Mendukung TweetDetail & Guest Mode / Tanpa Akun)
            if (urlReq.includes('graphql')) {
                try {
                    const json = await response.json();

                    // Cek jika Twitter return status 200 tapi isinya Error 88 (Rate Limit)
                    if (json.errors && json.errors.some(e => e.code === 88 || (e.message && e.message.toLowerCase().includes('rate limit')))) {
                        console.log(` \x1b[91m⚠️ [SCAN KOMEN RATE LIMIT] GraphQL Error 88 terdeteksi!\x1b[0m`);
                        isScanKomenRateLimited = true;
                        return;
                    }
                    let tweetsData = [];
                    findTweets(json, tweetsData);
                    
                    const matchId = targetUrl.match(/status\/(\d+)/);
                    const mainTweetId = matchId ? matchId[1] : null;

                    // 1. Filter murni komentar (buang tweet utama)
                    let commentTweets = tweetsData.filter(t => t.id !== mainTweetId);

                    // 2. URUTKAN KOMENTAR TERBARU KE TERLAMA (Newest First)
                    commentTweets.sort((a, b) => {
                        try {
                            // Menggunakan BigInt Snowflake ID (ID Lebih Besar = Komentar Lebih Baru)
                            return BigInt(b.id) > BigInt(a.id) ? 1 : -1;
                        } catch(e) {
                            // Fallback jika ID gagal di-parse, urutkan berdasarkan Waktu (createdAt)
                            return new Date(b.createdAt) - new Date(a.createdAt);
                        }
                    });

                    scanStats.komentar += commentTweets.length;
                    let currentBlacklists = global.cachedBlacklists || [];

                    // 3. Iterasi array komentar yang SUDAH DIURUTKAN dari yang paling baru
                    for (let tweet of commentTweets) {

                        let lowerSearchString = (tweet.text || "").toLowerCase();

                        // 1. Cek Filter Terpadu (Link & Code Filter Config)
                        const filterCheck = evaluateUnifiedFilter(tweet);
                        if (!filterCheck.passed) continue;

                        // 2. Cek Blacklist Word
                        const matchedBlacklist = currentBlacklists.find(bw => bw && lowerSearchString.includes(bw.toLowerCase().trim()));
                        if (matchedBlacklist) continue;

                        // Gunakan filter bawaan sistem
                        const extractedCodes = extractCodesFromText(tweet.text, tweet.urls || []);
                        const translatedObj = applyAutoTranslate(extractedCodes);
                        const hasValidCode = translatedObj.length > 0;

                        if (hasValidCode) {
                            foundCode = true;
                            const matchString = `[Scan Komen] ` + translatedObj.map(o => o.translated).join(', ');
                            
                            const matchedMod = getMatchingModule(tweet);
                            let moduleNote = matchedMod ? `⚙️ <b>Modul Ter-trigger:</b> <code>${matchedMod.module}</code>\n⏳ <i>Output modul menyusul...</i>\n` : "";

                            console.log(` \x1b[92m🚨 [SCAN KOMEN] Kode ditemukan di balasan! Menjalankan eksekusi...\x1b[0m`);
                            
                            // Simpan ke riwayat database agar tidak ditangkap ganda oleh search utama
                            saveSentHistory(tweet.id);
                            if (mainTweetId) { saveSentHistory(mainTweetId); PROCESSED_TWEETS.add(mainTweetId); }

                            // Eksekusi fitur utama persis seperti search
                            await sendTelegramAlert(tweet, matchString, moduleNote);
                            detectAndRunModule(tweet);
                            notify(`✅ <b>Scan Komen Berhasil Selesai!</b>\n🎯 Tertangkap pada Loop: ${loopCount}\n📊 Total Komen Disadap: ${scanStats.komentar} balasan\nKode dieksekusi. Mematikan scanner...`);
                            break;
                        }
                    }
                } catch(e) {}
            }
        });
        };

        attachResponseListener(page);

        console.log(` \x1b[96m🔍 [SCAN KOMEN] Memulai pemantauan komentar di background: ${targetUrl}\x1b[0m`);
        
        while (!foundCode && loopCount < maxLoops) {
            // Cek jika terkena Rate Limit Twitter saat scan komen
            if (isScanKomenRateLimited) {
                notify(`⚠️ <b>Scan Komen Terhenti (Twitter Rate Limit)!</b>\nTarget: ${targetUrl}\n<i>Twitter membatasi request (HTTP 429 / Error 88). Scanner dihentikan otomatis.</i>`);
                console.log(` \x1b[91m🛑 [SCAN KOMEN] Dihentikan akibat Twitter Rate Limit.\x1b[0m`);
                break;
            }

            // Cek apakah user mengirim perintah /stopscankomen
            if (!global.activeScanChats.get(chatId.toString())) {
                console.log(` \x1b[93m🛑 [SCAN KOMEN] Dihentikan secara manual oleh user melalui Telegram.\x1b[0m`);
                break;
            }

            loopCount++;
            try {
                // Notifikasi instan saat loop ke-1
                if (loopCount === 1) {
                    sendTelegramText(`🚀 <b>Scan Komen Dimulai!</b>\n🎯 Target: ${targetUrl}\n🌐 Mode: ${tanpaAkun ? 'Tanpa Akun (Guest)' : 'Dengan Sesi'}\n<i>Mesin sedang memuat komentar pertama...</i>`).catch(() => {});
                }

                // Reset/refresh khusus Guest Mode tiap 5 loop agar token & IP tidak ter-limit
                if (tanpaAkun && loopCount > 1 && (loopCount - 1) % 5 === 0) {
                    console.log(` \x1b[96m🔄 [REFRESH GUEST] Me-reload halaman agar token guest fresh (Loop ${loopCount})...\x1b[0m`);
                    await page.reload({ waitUntil: 'domcontentloaded', timeout: 20000 });
                } else if (loopCount === 1) {
                    await page.goto(targetUrl, { waitUntil: 'domcontentloaded', timeout: 20000 });
                } else {
                    // Gunakan page.reload() agar SPA X memicu request GraphQL komentar secara penuh
                    await page.reload({ waitUntil: 'domcontentloaded', timeout: 20000 });
                }

                // Auto-scroll untuk memicu render & request GraphQL balasan
                await page.waitForTimeout(1500);
                await page.evaluate(() => window.scrollBy(0, 800)).catch(() => {});
                await page.waitForTimeout(2000); 
                
                if (foundCode || !global.activeScanChats.get(chatId.toString())) break;

                // --- 1. BYPASS POPUP MODAL GUEST MODE (TANPA AKUN) ---
                try {
                    await page.evaluate(() => {
                        const modal = document.querySelector('[data-interaction="app-store-obstruction"]');
                        if (modal) modal.remove();
                        const closeBtn = document.querySelector('button[aria-label="Tutup"]');
                        if (closeBtn) closeBtn.click();
                    });
                } catch(e) {}

                // --- 2. FALLBACK SCAN DOM KOMEN (BACA LANGSUNG DARI HTML SSR) ---
                if (!foundCode) {
                    try {
                        const domComments = await page.evaluate(() => {
                            const articles = Array.from(document.querySelectorAll('article'));
                            return articles.map(art => {
                                const textEl = art.querySelector('div[dir="auto"]');
                                const linkEl = art.querySelector('a[href*="/status/"]');
                                const text = textEl ? textEl.innerText : '';
                                const postLink = linkEl ? linkEl.href : '';
                                const matchId = postLink.match(/status\/(\d+)/);
                                const id = matchId ? matchId[1] : null;
                                return { id, text, postLink };
                            }).filter(a => a.id && a.text);
                        });

                        const matchId = targetUrl.match(/status\/(\d+)/);
                        const mainTweetId = matchId ? matchId[1] : null;
                        const filteredDomComments = domComments.filter(c => c.id !== mainTweetId && !PROCESSED_TWEETS.has(c.id));

                        scanStats.komentar += filteredDomComments.length;
                        let currentBlacklists = global.cachedBlacklists || [];

                        for (let item of filteredDomComments) {
                            const tweet = {
                                id: item.id,
                                text: item.text,
                                postLink: item.postLink || targetUrl,
                                createdAt: new Date().toISOString(),
                                urls: [],
                                mediaUrls: []
                            };

                            let lowerSearchString = (tweet.text || "").toLowerCase();
                            const filterCheck = evaluateUnifiedFilter(tweet);
                            if (!filterCheck.passed) continue;

                            const matchedBlacklist = currentBlacklists.find(bw => bw && lowerSearchString.includes(bw.toLowerCase().trim()));
                            if (matchedBlacklist) continue;

                            const extractedCodes = extractCodesFromText(tweet.text, tweet.urls || []);
                            const translatedObj = applyAutoTranslate(extractedCodes);
                            if (translatedObj.length > 0) {
                                foundCode = true;
                                const matchString = `[Scan Komen DOM] ` + translatedObj.map(o => o.translated).join(', ');
                                const matchedMod = getMatchingModule(tweet);
                                let moduleNote = matchedMod ? `⚙️ <b>Modul Ter-trigger:</b> <code>${matchedMod.module}</code>\n⏳ <i>Output modul menyusul...</i>\n` : "";

                                console.log(` \x1b[92m🚨 [SCAN KOMEN DOM] Kode ditemukan di HTML DOM! Menjalankan eksekusi...\x1b[0m`);
                                saveSentHistory(tweet.id);
                                if (mainTweetId) { saveSentHistory(mainTweetId); PROCESSED_TWEETS.add(mainTweetId); }
                                await sendTelegramAlert(tweet, matchString, moduleNote);
                                detectAndRunModule(tweet);
                                notify(`✅ <b>Scan Komen Berhasil Selesai (via DOM)!</b>\n🎯 Tertangkap pada Loop: ${loopCount}\n📊 Total Komen Disadap: ${scanStats.komentar} balasan\nKode dieksekusi. Mematikan scanner...`);
                                break;
                            }
                        }
                    } catch(domErr) {}
                }

                // --- PENGIRIMAN LIVE REPORT TIAP 5 LOOP ---
                if (loopCount % 5 === 0) {
                    const progressMsg = `🔄 <b>Status Scan Komen (${loopCount}/${maxLoops})</b>\n🎯 Target: ${targetUrl}\n📊 Total Komen Disadap: ${scanStats.komentar} balasan\n<i>Mesin masih terus memantau...</i>`;
                    sendTelegramText(progressMsg).catch(() => {});
                    console.log(` \x1b[96m🔄 [SCAN KOMEN] Loop ${loopCount}/${maxLoops} - Total komen disadap: ${scanStats.komentar}\x1b[0m`);
                }

                // --- DETEKSI LAYAR BLANK / KOSONG ---
                const bodyTextLength = await page.evaluate(() => document.body ? document.body.innerText.length : 0).catch(() => 0);
                if (loopCount >= 3 && bodyTextLength < 50) {
                    notify(`⚠️ <b>Peringatan Scan Komen: Layar Terdeteksi Blank / Kosong!</b>\n🎯 Target: ${targetUrl}\n<i>Halaman X gagal memuat konten atau tampilan kosong.</i>`);
                    console.log(` \x1b[91m⚠️ [SCAN KOMEN] Layar blank terdeteksi (panjang teks DOM < 50 karakter).\x1b[0m`);
                }

                // Tanpa jeda lama, hanya beri jeda mikro 300ms agar CPU tidak 100% nge-lock
                await page.waitForTimeout(300);
            } catch(e) {
                notify(`❌ <b>Scan Komen Error / Layar Bermasalah</b>\n🎯 Target: ${targetUrl}\nError: <code>${e.message}</code>`);
                await page.waitForTimeout(1000); 
            }
        }

        if (!foundCode) {
            notify(`🛑 <b>Scan Komen Dihentikan (Timeout)</b>\nTarget: ${targetUrl}\n📊 Total Refresh: ${loopCount}x\n📊 Total Komen Disadap: ${scanStats.komentar} balasan\n<i>Tidak ada kode yang muncul dan lolos filter.</i>`);
            console.log(` \x1b[93m🛑 [SCAN KOMEN] Timeout. Total komen disadap: ${scanStats.komentar} balasan.\x1b[0m`);
        }

        // Bersihkan Worker WebView saat scan komen selesai untuk menghemat RAM
        try {
            const ipcBin = fs.existsSync('/usr/local/termux-shims/termux-ipc') ? '/usr/local/termux-shims/termux-ipc' : 'termux-ipc';
            require('child_process').execSync(`${ipcBin} webview WEBVIEW_KILL_WORKER`, { stdio: 'ignore' });
        } catch(e) {}
        
        return foundCode;
    } catch (e) {
        console.log(` \x1b[91m❌ [SCAN KOMEN] Error: ${e.message}\x1b[0m`);
        try {
            const ipcBin = fs.existsSync('/usr/local/termux-shims/termux-ipc') ? '/usr/local/termux-shims/termux-ipc' : 'termux-ipc';
            require('child_process').execSync(`${ipcBin} webview WEBVIEW_KILL_WORKER`, { stdio: 'ignore' });
        } catch(err) {}
        return false;
    }
}

async function runFollowNotif(targetUrl, chatId) {
    try {
        const botToken = tgConfig.bot_token;
        const notify = async (msg) => {
            await fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ chat_id: chatId, text: msg, parse_mode: 'HTML', disable_web_page_preview: true })
            }).catch(()=>{});
        };

        if (!global.activeBrowserContext) {
            return notify(`❌ <b>Gagal:</b> Sesi browser utama belum aktif. Pastikan X Radar sedang berjalan.`);
        }

        // Ekstrak username dari URL X
        const match = targetUrl.match(/(?:x\.com|twitter\.com)\/([a-zA-Z0-9_]+)/i);
        if (!match || !match[1]) {
            return notify(`❌ <b>Gagal:</b> Tidak dapat menemukan username valid dari link tersebut.`);
        }
        
        const username = match[1];
        const profileUrl = `https://x.com/${username}`;

        notify(`⏳ <b>Memproses Target</b>\n👤 Handle: @${username}\n<i>Menyusup ke profil menggunakan sesi aktif...</i>`);
        console.log(` \x1b[96m🎯 [FOLLOW NOTIF] Membuka profil @${username}...\x1b[0m`);

        const page = global.activeCurrentPage || (await ensureWebViewCDP()).page;
        
        // Membuka profil X target
        await page.goto(profileUrl, { waitUntil: 'domcontentloaded', timeout: 30000 });
        await page.waitForTimeout(3000); // Tunggu UI termuat

        // 1. Eksekusi Tombol Follow
        const followBtn = await page.$('button[data-testid$="-follow"]');
        const unfollowBtn = await page.$('button[data-testid$="-unfollow"]');

        if (followBtn) {
            await followBtn.click();
            await page.waitForTimeout(2000);
            console.log(` \x1b[92m✅ [FOLLOW NOTIF] Berhasil klik Follow @${username}.\x1b[0m`);
        } else if (!unfollowBtn) {
            throw new Error("Tombol Follow tidak ditemukan di halaman profil.");
        }

        // 2. Eksekusi Tombol Lonceng Notifikasi
        const bellBtnOff = await page.$('button[aria-label*="Turn on notifications" i], button[aria-label*="Nyalakan notifikasi" i]');
        const bellBtnOn = await page.$('button[aria-label*="Turn off notifications" i], button[aria-label*="Matikan notifikasi" i]');

        if (bellBtnOff) {
            await bellBtnOff.click();
            await page.waitForTimeout(1000);
            notify(`✅ <b>Misi Sukses!</b>\nBerhasil mem-follow dan <b>MENYALAKAN SEMUA NOTIFIKASI</b> untuk @${username}.`);
            console.log(` \x1b[92m🔔 [FOLLOW NOTIF] Notifikasi untuk @${username} berhasil dinyalakan.\x1b[0m`);
        } else if (bellBtnOn) {
            notify(`✅ <b>Info:</b>\nKamu sudah mem-follow dan notifikasi <b>SUDAH MENYALA</b> sebelumnya untuk @${username}.`);
        } else {
            notify(`⚠️ <b>Peringatan:</b>\nBerhasil follow @${username}, tapi tombol lonceng notifikasi tidak ditemukan (mungkin tersembunyi).`);
        }

        // Tab WebView tetap standby melayani radar
    } catch (e) {
        console.log(` \x1b[91m❌ [FOLLOW NOTIF] Error: ${e.message}\x1b[0m`);
        await fetch(`https://api.telegram.org/bot${tgConfig.bot_token}/sendMessage`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ chat_id: chatId, text: `❌ <b>Error:</b> Gagal mengeksekusi aksi: ${e.message}`, parse_mode: 'HTML' })
        }).catch(()=>{});
    }
}

async function startTelegramPolling() {
    if (!tgConfig.bot_token || !tgConfig.chat_id) return;
    let offset = 0;
    console.log(` \x1b[96m🤖 [TELEGRAM BOT] Remote Control Listener aktif di background (Anti-Hang Fixed)...\x1b[0m`);
    
    while (true) {
        let timeoutId = null;
        try {
            // Gunakan AbortController agar fetch getUpdates tidak 'hang' selamanya jika jaringan ngadat
            const controller = new AbortController();
            timeoutId = setTimeout(() => controller.abort(), 35000); // Batas maksimal 35 detik per request

            const res = await fetch(`https://api.telegram.org/bot${tgConfig.bot_token}/getUpdates?offset=${offset}&timeout=30`, {
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            const data = await res.json();
            if (data.ok && data.result) {
                for (let update of data.result) {
                    offset = update.update_id + 1;
                    
                    // Bungkus per-pesan dengan try-catch agar error di satu pesan tidak membekukan bot
                    try {
                        if (!update.message || !update.message.text) continue;
                        
                        const chatId = update.message.chat.id.toString();
                        
                        // Keamanan: Hanya respon dari Chat ID terdaftar (Mendukung Multi-ID)
                        let allowedIds = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id.map(id => id.toString()) : [tgConfig.chat_id.toString()];
                        if (!allowedIds.includes(chatId)) continue; 
                        
                        const text = update.message.text.trim();
                        const replyTo = update.message.reply_to_message;

                        let replyMsg = "";

                        // FITUR EDIT VIA REPLY: Jika user membalas pesan daftar query dari bot
                        if (replyTo && replyTo.text && replyTo.text.includes('DAFTAR QUERY YANG DIPANTAU')) {
                            const newQueries = text.split('\n').map(q => q.replace(/^\[\d+\]\s*/, '').trim()).filter(q => q.length > 0);
                            if (newQueries.length > 0) {
                                fs.writeFileSync(QUERY_FILE, JSON.stringify(newQueries, null, 2));
                                replyMsg = `✅ <b>Query Berhasil Diperbarui!</b>\nTotal aktif sekarang: ${newQueries.length} query.`;
                            } else {
                                replyMsg = `❌ Gagal memperbarui: Format teks kosong atau tidak valid.`;
                            }
                        } else {
                            const parts = text.split(' ');
                            const cmd = parts[0].toLowerCase().split('@')[0];
                            const argsText = parts.slice(1).join(' ');

                            if (cmd === '/status') {
                                let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
                                let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
                                replyMsg = `📊 <b>X RADAR STATUS BOT</b>\n\n` +
                                           `✅ Status: Berjalan (Headless)\n` +
                                           `📌 Total Query Aktif: ${queries.length}\n` +
                                           `❌ Total Blacklist: ${blacklists.length}\n` +
                                           `🕒 Server Time: ${formatWIB(new Date())}`;
                            } 
                            else if (cmd === '/query') {
                                let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
                                if (queries.length === 0) {
                                    replyMsg = `📂 <b>Daftar Query Kosong</b>\nBalas pesan ini atau masukkan query baru.`;
                                } else {
                                    let listStr = queries.map((q, i) => `[${i + 1}] ${q}`).join('\n');
                                    replyMsg = `📂 <b>DAFTAR QUERY YANG DIPANTAU:</b>\n\n` +
                                               `<i>(Balas/Reply pesan ini dengan daftar query yang sudah diedit untuk menyimpan perubahan)</i>\n\n` +
                                               `${listStr}`;
                                }
                            }
                            else if (cmd === '/blacklist') {
                                if (!argsText) {
                                    replyMsg = `❌ Format salah!\nGunakan: <code>/blacklist &lt;kata&gt;</code>`;
                                } else {
                                    let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
                                    if (!blacklists.includes(argsText)) {
                                        blacklists.push(argsText);
                                        fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2));
                                        replyMsg = `✅ <b>Blacklist Ditambahkan (Real-time):</b>\n👉 ${argsText}`;
                                    } else {
                                        replyMsg = `⚠️ Kata blacklist tersebut sudah ada!`;
                                    }
                                }
                            }
                            /* Command /restart telah dihapus untuk mencegah infinite loop */
                            else if (cmd === '/scankomen') {
                                // Sub-command saklar & manajemen
                                if (argsText.startsWith('--on')) {
                                    scanKomenTriggerConfig.active = true;
                                    saveScanKomenTriggerConfig();
                                    replyMsg = `✅ <b>Auto-Scan Komen Trigger DIAKTIFKAN (ON)</b>\nBot akan otomatis men-scan komentar jika postingan beranda mengandung kata trigger.`;
                                } else if (argsText.startsWith('--off')) {
                                    scanKomenTriggerConfig.active = false;
                                    saveScanKomenTriggerConfig();
                                    replyMsg = `🛑 <b>Auto-Scan Komen Trigger DIMATIKAN (OFF)</b>`;
                                } else if (argsText.startsWith('--tanpaakun-on')) {
                                    scanKomenTriggerConfig.tanpaAkun = true;
                                    saveScanKomenTriggerConfig();
                                    replyMsg = `🔓 <b>Mode Tanpa Akun DIAKTIFKAN (ON)</b>\nSetiap proses scan komentar akan berjalan tanpa memuat sesi login (Guest Mode).`;
                                } else if (argsText.startsWith('--tanpaakun-off')) {
                                    scanKomenTriggerConfig.tanpaAkun = false;
                                    saveScanKomenTriggerConfig();
                                    replyMsg = `🔐 <b>Mode Tanpa Akun DIMATIKAN (OFF)</b>\nProses scan komentar kembali menggunakan sesi login terdaftar (x_session.json).`;
                                } else if (argsText.startsWith('--status') || argsText === 'status') {
                                    let kwList = scanKomenTriggerConfig.keywords.length > 0 ? scanKomenTriggerConfig.keywords.map(k => `• <code>${k}</code>`).join('\n') : 'Kosong';
                                    replyMsg = `📊 <b>STATUS AUTO-SCAN KOMEN</b>\n` +
                                               `├─ Status Auto-Scan : ${scanKomenTriggerConfig.active ? '🟢 AKTIF (ON)' : '🔴 MATI (OFF)'}\n` +
                                               `├─ Mode Sesi        : ${scanKomenTriggerConfig.tanpaAkun ? '🔓 Tanpa Akun (Guest Mode)' : '🔐 Dengan Sesi Login'}\n` +
                                               `└─ Daftar Trigger:\n${kwList}`;
                                } else if (argsText.startsWith('--list')) {
                                    let kwList = scanKomenTriggerConfig.keywords.length > 0 ? scanKomenTriggerConfig.keywords.join(', ') : 'Kosong';
                                    replyMsg = `⚙️ <b>STATUS AUTO-SCAN KOMEN</b>\n` +
                                               `├─ Status Auto-Scan : ${scanKomenTriggerConfig.active ? '🟢 AKTIF (ON)' : '🔴 MATI (OFF)'}\n` +
                                               `├─ Mode Sesi        : ${scanKomenTriggerConfig.tanpaAkun ? '🔓 Tanpa Akun (Guest Mode)' : '🔐 Dengan Sesi Login'}\n` +
                                               `└─ Kata Trigger     : <code>${kwList}</code>`;
                                } else if (argsText.startsWith('--add')) {
                                    const keywordToAdd = argsText.replace('--add', '').trim();
                                    if (keywordToAdd) {
                                        if (!scanKomenTriggerConfig.keywords.includes(keywordToAdd)) {
                                            scanKomenTriggerConfig.keywords.push(keywordToAdd);
                                            saveScanKomenTriggerConfig();
                                            replyMsg = `✅ <b>Berhasil menambah kata trigger:</b> <code>${keywordToAdd}</code>`;
                                        } else {
                                            replyMsg = `⚠️ Kata trigger tersebut sudah ada di daftar.`;
                                        }
                                    } else {
                                        replyMsg = `❌ Format salah!\nGunakan: <code>/scankomen --add &lt;kata&gt;</code>`;
                                    }
                                } else if (argsText.startsWith('--remove')) {
                                    const keywordToRemove = argsText.replace('--remove', '').trim();
                                    if (keywordToRemove) {
                                        const idx = scanKomenTriggerConfig.keywords.indexOf(keywordToRemove);
                                        if (idx > -1) {
                                            scanKomenTriggerConfig.keywords.splice(idx, 1);
                                            saveScanKomenTriggerConfig();
                                            replyMsg = `✅ <b>Berhasil menghapus kata trigger:</b> <code>${keywordToRemove}</code>`;
                                        } else {
                                            replyMsg = `⚠️ Kata trigger tersebut tidak ditemukan di daftar.`;
                                        }
                                    } else {
                                        replyMsg = `❌ Format salah!\nGunakan: <code>/scankomen --remove &lt;kata&gt;</code>`;
                                    }
                                } else if (argsText.startsWith('--')) {
                                    const unknownFlag = argsText.split(' ')[0];
                                    replyMsg = `❌ <b>Argumen <code>${unknownFlag}</code> tidak ditemukan!</b>\nGunakan argumen yang valid:\n` +
                                               `- <code>--on</code> / <code>--off</code>\n` +
                                               `- <code>--tanpaakun-on</code> / <code>--tanpaakun-off</code>\n` +
                                               `- <code>--status</code> / <code>--list</code>\n` +
                                               `- <code>--add &lt;kata&gt;</code> / <code>--remove &lt;kata&gt;</code>`;
                                } else {
                                    // Pemrosesan Manual Scan Link (Reply / URL Teks)
                                    let targetUrl = "";
                                    let customLoop = 60; // Default

                                    const partsParams = argsText.trim().split(/\s+/);
                                    for (let param of partsParams) {
                                        if (param.match(/^https?:\/\//i)) {
                                            targetUrl = param;
                                        } else if (param.match(/^\d+$/)) {
                                            customLoop = parseInt(param, 10);
                                        }
                                    }

                                    let currentReply = replyTo;
                                    let depth = 0;
                                    while (!targetUrl && currentReply && depth < 5) {
                                        const replyText = currentReply.text || currentReply.caption || "";
                                        const urlMatch = replyText.match(/https?:\/\/(www\.)?(x\.com|twitter\.com)\/[^\s]+/i);
                                        
                                        if (urlMatch) {
                                            targetUrl = urlMatch[0];
                                            break;
                                        } else {
                                            const entities = currentReply.entities || currentReply.caption_entities || [];
                                            const linkEntity = entities.find(e => e.type === 'text_link' && e.url && e.url.match(/https?:\/\/(www\.)?(x\.com|twitter\.com)\/[^\s]+/i));
                                            if (linkEntity) {
                                                targetUrl = linkEntity.url;
                                                break;
                                            }
                                        }
                                        currentReply = currentReply.reply_to_message;
                                        depth++;
                                    }

                                    if (!targetUrl) {
                                        replyMsg = `❌ <b>Format salah!</b>\nGunakan:\n` +
                                                   `- <code>/scankomen &lt;link_x&gt; [loop]</code> atau reply pesan.\n` +
                                                   `- <code>/scankomen --on / --off</code>\n` +
                                                   `- <code>/scankomen --tanpaakun-on / --tanpaakun-off</code>\n` +
                                                   `- <code>/scankomen --status / --list</code>\n` +
                                                   `- <code>/scankomen --add / --remove</code>`;
                                    } else {
                                        const isTanpaAkunActive = scanKomenTriggerConfig.tanpaAkun;
                                        const modeText = isTanpaAkunActive ? 'Mobile (Tanpa Akun / Guest Mode)' : 'Mobile (Dengan Sesi Login)';
                                        
                                        const queueStatusText = (isScanQueueProcessing || scanKomenQueue.length > 0)
                                            ? `📥 <b>Dimasukkan ke antrean:</b> Posisi ke-${scanKomenQueue.length + (isScanQueueProcessing ? 1 : 0)}`
                                            : `🚀 <b>Langsung mengeksekusi scan...</b>`;

                                        const loopText = customLoop === 0 ? "Unlimited" : `${customLoop}x`;
                                        replyMsg = `🔍 <b>Scan Komen Diterima</b>\n🌐 Mode: ${modeText}\n🔄 Loop: ${loopText}\n🎯 Target: ${targetUrl}\n⚡ Status: ${queueStatusText}`;
                                        
                                        enqueueScanKomen(targetUrl, chatId, isTanpaAkunActive, customLoop);
                                    }
                                }
                            }

                            else if (cmd === '/follownotif') {
                                let targetUrl = argsText;
                                let currentReply = replyTo;
                                let depth = 0;
                                while (!targetUrl && currentReply && depth < 5) {
                                    const replyText = currentReply.text || currentReply.caption || "";
                                    const urlMatch = replyText.match(/https?:\/\/(www\.)?(x\.com|twitter\.com)\/[^\s]+/i);
                                    
                                    if (urlMatch) {
                                        targetUrl = urlMatch[0];
                                        break;
                                    } else {
                                        const entities = currentReply.entities || currentReply.caption_entities || [];
                                        const linkEntity = entities.find(e => e.type === 'text_link' && e.url && e.url.match(/https?:\/\/(www\.)?(x\.com|twitter\.com)\/[^\s]+/i));
                                        if (linkEntity) {
                                            targetUrl = linkEntity.url;
                                            break;
                                        }
                                    }
                                    currentReply = currentReply.reply_to_message;
                                    depth++;
                                }

                                if (!targetUrl) {
                                    replyMsg = `❌ Format salah!\nGunakan: <code>/follownotif &lt;link_x&gt;</code> atau reply pesan notif bot.`;
                                } else {
                                    runFollowNotif(targetUrl, chatId).catch(console.error);
                                }
                            }
                            else if (cmd === '/stopscankomen') {
                                global.activeScanChats.set(chatId.toString(), false);
                                replyMsg = `🛑 <b>Perintah diterima!</b>\nMematikan mesin Scan Komen di background...`;
                            }
                        }

                        if (replyMsg) {
                            await fetch(`https://api.telegram.org/bot${tgConfig.bot_token}/sendMessage`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ chat_id: chatId, text: replyMsg, parse_mode: 'HTML' })
                            }).catch(()=>{});
                        }
                    } catch (msgErr) {
                        // Abaikan error pada satu pesan spesifik agar loop bot Telegram tidak berhenti total
                    }
                }
            }
        } catch (err) {
            if (timeoutId) clearTimeout(timeoutId);
            await new Promise(r => setTimeout(r, 5000));
        }
    }
}

async function askAIDeepSeek(promptText) {
    if (!aiConfig.apiKey) throw new Error("API Key AI kosong di ai_config.json");
    const res = await fetch(`${aiConfig.baseUrl}/chat/completions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${aiConfig.apiKey}` },
        body: JSON.stringify({
            model: aiConfig.model || 'deepseek-chat',
            messages: [{ role: 'user', content: promptText }],
            response_format: { type: 'json_object' }
        })
    });
    const data = await res.json();
    return JSON.parse(data.choices[0].message.content);
}

// ==========================================
// 3. LOGIC & DATA EXTRACTION 
// ==========================================

function findTweets(obj, tweetsArray) {
    if (!obj || typeof obj !== 'object') return;
    
    if (obj.legacy && obj.legacy.full_text && (obj.rest_id || obj.id_str || obj.legacy.id_str)) {
        let id = obj.rest_id || obj.id_str || obj.legacy.id_str;
        let text = obj.legacy.full_text;
        let createdAt = obj.legacy.created_at;
        
        let noteTweetResult = obj.note_tweet?.note_tweet_results?.result;
        if (noteTweetResult && noteTweetResult.text) text = noteTweetResult.text; 
        
        let authorHandle = "i"; 
        try {
            let userResult = obj.core?.user_results?.result || obj.author_results?.result;
            if (userResult) {
                let userLegacy = userResult.legacy || userResult;
                if (userLegacy.screen_name) authorHandle = userLegacy.screen_name;
            }
        } catch(e) {}
        
        let postLink = `https://x.com/${authorHandle}/status/${id}`;

        let mediaUrls = [];
        try {
            let mediaEntities = obj.legacy.extended_entities?.media || obj.legacy.entities?.media || [];
            if (noteTweetResult?.extended_entities?.media) mediaEntities = mediaEntities.concat(noteTweetResult.extended_entities.media);
            mediaEntities.forEach(m => { if (m.type === 'photo' && m.media_url_https) mediaUrls.push(m.media_url_https); });
        } catch(e) {}

        let urls = [];
        try {
            let urlEntities = obj.legacy.entities?.urls || [];
            if (noteTweetResult?.entity_set?.urls) urlEntities = urlEntities.concat(noteTweetResult.entity_set.urls);
            urlEntities.forEach(u => {
                if (u.expanded_url) {
                    urls.push(u.expanded_url);
                    if (u.url && text.includes(u.url)) text = text.replace(u.url, (u.display_url || u.expanded_url));
                }
            });
        } catch(e) {}

        const LINK_REGEX = /(?:https?:\/\/)?(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z]{2,}\b(?:[-a-zA-Z0-9()@:%_\+.~#?&//=]*)/gi;
        const foundLinks = text.match(LINK_REGEX) || [];
        urls.push(...foundLinks);

        // --- FILTER DIPERKUAT: Buang link terpotong & duplikat ---
        urls = [...new Set(urls)].filter(link => {
            if (!link || typeof link !== 'string') return false;
            // Abaikan link internal X/Twitter/t.co
            if (link.match(/^(https?:\/\/)?(www\.)?(twitter\.com|x\.com|t\.co)($|\/)/i)) return false;
            // Abaikan link yang terpotong oleh karakter elipsis (...) di ujungnya
            if (link.endsWith('…') || link.endsWith('...')) return false;
            return true;
        });

        // Jika ada versi pendek/terpotong dan versi panjang dari domain yang sama, prioritaskan yang lengkap
        if (urls.length > 1) {
            urls.sort((a, b) => b.length - a.length);
            let cleanUrls = [];
            let seenDomains = new Set();
            for (let u of urls) {
                try {
                    let domainObj = new URL(u.startsWith('http') ? u : `https://${u}`);
                    let baseKey = domainObj.hostname + domainObj.pathname;
                    if (!seenDomains.has(baseKey)) {
                        seenDomains.add(baseKey);
                        cleanUrls.push(u);
                    }
                } catch(e) {
                    cleanUrls.push(u);
                }
            }
            urls = cleanUrls;
        }

        tweetsArray.push({ id, text, postLink, createdAt, urls, mediaUrls });
    }
    
    for (let key in obj) {
        if (obj[key] !== null && typeof obj[key] === 'object') findTweets(obj[key], tweetsArray);
    }
}

// ==========================================
// ASCII TIMER HELPER
// ==========================================
const ASCII_CHARS = {
    '0': [" ██████╗ ", "██╔═══██╗", "██║   ██║", "██║   ██║", "╚██████╔╝", " ╚═════╝ "],
    '1': [" ███╗    ", "████║    ", "██╔██║   ", "╚═╝██║   ", "███████╗ ", "╚══════╝ "],
    '2': ["██████╗  ", "╚════██╗ ", " █████╔╝ ", "██╔═══╝  ", "███████╗ ", "╚══════╝ "],
    '3': ["██████╗  ", "╚════██╗ ", " █████╗  ", " ╚═══██╗ ", "██████╔╝ ", "╚═════╝  "],
    '4': ["██╗  ██╗ ", "██║  ██║ ", "███████║ ", "╚════██║ ", "     ██║ ", "     ╚═╝ "],
    '5': ["███████╗ ", "██╔════╝ ", "███████╗ ", "╚════██║ ", "███████║ ", "╚══════╝ "],
    '6': [" ██████╗ ", "██╔════╝ ", "███████╗ ", "██╔═══██╗", "╚██████╔╝", " ╚═════╝ "],
    '7': ["███████╗ ", "╚════██║ ", "    ██╔╝ ", "   ██╔╝  ", "   ██║   ", "   ╚═╝   "],
    '8': [" ██████╗ ", "██╔═══██╗", "╚██████╔╝", "██╔═══██╗", "╚██████╔╝", " ╚═════╝ "],
    '9': [" ██████╗ ", "██╔═══██╗", "╚███████║", " ╚════██║", " ██████╔╝", " ╚═════╝ "],
    'M': ["███╗   ███╗", "████╗ ████║", "██╔████╔██║", "██║╚██╔╝██║", "██║ ╚═╝ ██║", "╚═╝     ╚═╝"],
    'O': [" ██████╗ ", "██╔═══██╗", "██║   ██║", "██║   ██║", "╚██████╔╝", " ╚═════╝ "],
    'D': ["██████╗ ", "██╔══██╗", "██║  ██║", "██║  ██║", "██████╔╝", "╚═════╝ "],
    'U': ["██╗  ██╗", "██║  ██║", "██║  ██║", "██║  ██║", "╚██████╔╝", " ╚═════╝ "],
    'L': ["██╗     ", "██║     ", "██║     ", "██║     ", "███████╗", "╚══════╝"],
    'H': ["██╗  ██╗", "██║  ██║", "███████║", "██╔══██║", "██║  ██║", "╚═╝  ╚═╝"],
    'I': ["██╗", "██║", "██║", "██║", "██║", "╚═╝"],
    'E': ["███████╗", "██╔════╝", "█████╗  ", "██╔══╝  ", "███████╗", "╚══════╝"],
    '-': ["         ", "         ", "  █████╗ ", "  ╚════╝ ", "         ", "         "],
    ' ': ["   ", "   ", "   ", "   ", "   ", "   "]
};

function getAsciiLines(text) {
    const lines = ["", "", "", "", "", ""];
    for (let char of text.toString().toUpperCase()) {
        const font = ASCII_CHARS[char] || ASCII_CHARS[' '];
        for (let i = 0; i < 6; i++) {
            lines[i] += font[i] + " ";
        }
    }
    return lines;
}

function updateAsciiTimer(sec, isPaused) {
    if (!global.isLogViewerOpen) {
        const secStr = isPaused ? "--" : Math.min(9999, sec).toString();
        const color = isPaused ? "\x1b[93m" : "\x1b[92m";
        
        for (let i = 0; i < 6; i++) {
            let rowStr = "";
            for (let j = 0; j < secStr.length; j++) {
                const charASCII = ASCII_CHARS[secStr[j]] || ASCII_CHARS[' '];
                rowStr += charASCII[i] + " ";
            }
            process.stdout.write(`\x1b[${i + 1};44H${color}${rowStr}\x1b[K\x1b[0m`);
        }
        process.stdout.write(`\x1b[45;1H\x1b[?25l`);
    }
}

// ==========================================
// 4. MAIN SCRAPER (ULTIMATE ARCHITECTURE)
// ==========================================

const startScraping = async (selectedQueriesArray) => {
    // Jalankan listener Telegram command di background secara asynchronous
    startTelegramPolling();
    
    // Status global untuk jeda countdown utama saat mode notif berjalan
    global.isNotifRunning = false;
    global.activeCurrentPage = null; // Pointer halaman utama yang aktif

    const isSimpleUI = !hasArg('--debug'); // Default Simple-UI AKTIF, kecuali dijalankan dengan --debug
    global.isModuleHidden = true; // Default: Sembunyikan modul (Tampilkan tombol SHOW)
    
    // 1. STATE MANAGER UNTUK UI 1 HALAMAN (SIMPLE UI)
    global.uiState = {
        loop: 1, maxLoop: 0, queryId: 1,
        notif: '-', notifTime: '-',
        statsNotif: { found: 0, history: 0, blacklist: 0, linkFilter: 0, match: 0 },
        statsLoop:  { found: 0, history: 0, blacklist: 0, linkFilter: 0, match: 0 },
        statsTotal: { found: 0, history: 0, blacklist: 0, linkFilter: 0, match: 0 },
        status: 'Menyiapkan sistem...'
    };

    const printAt = (x, y, text) => process.stdout.write(`\x1b[${y};${x}H${text}`);

    const renderDashboard = () => {
        if (!isSimpleUI || global.isLogViewerOpen) return;
        clear(); title();
        
        let tgCache = global.tgCacheRAM || {bot:{username:'',name:''},chat:{name:''}};
        let modulesCache = global.cachedModules || [];
        let wlCache = global.cachedWatchlists || [];

        const isMobileTxt = hasArg('--mobile') ? 'Mobile' : 'Desktop';
        const displayTxt = hasArg('--headless') ? 'Headless' : 'Windowed';
        const apiTxt = tgCache.bot.username || 'Kosong';
        const botTxt = tgCache.bot.name || 'Kosong';
        const chatIdsCount = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id.length : (tgConfig.chat_id ? 1 : 0);
        
        const wlStr = wlCache.length > 0 
            ? wlCache.map(w => {
                const hw = typeof w === 'object' ? w.handle.replace(/^@/, '') : w.replace(/^@/, '');
                const nm = typeof w === 'object' ? w.name : null;
                return nm ? `@${hw}=${GREEN}${nm}${RESET}` : `@${hw}`;
            }).join(', ') 
            : 'Kosong';

        console.log(` ${CYAN}[ ENGINE SETUP ]${RESET}`);
        console.log(` ${WHITE}├─${RESET} Engine     : Android WebView Native (CDP: 9222)`);
        console.log(` ${WHITE}├─${RESET} Profil     : ${currentProfile ? currentProfile : 'Default / Guest'}`);
        console.log(` ${WHITE}└─${RESET} Mode       : ${displayTxt}\n`);

        console.log(` ${CYAN}[ TELEGRAM SETUP ]${RESET}`);
        console.log(` ${WHITE}├─ [${tgConfig.bot_token?GREEN+'✅'+WHITE:RED+'❌'+WHITE}]${RESET} API Telegram : ${apiTxt}`);
        console.log(` ${WHITE}├─ [${tgConfig.bot_token?GREEN+'✅'+WHITE:RED+'❌'+WHITE}]${RESET} Bot Token    : ${botTxt}`);
        console.log(` ${WHITE}└─ [${chatIdsCount > 0 ? GREEN+'✅'+WHITE:RED+'❌'+WHITE}]${RESET} Chat ID      : ${chatIdsCount > 0 ? `${chatIdsCount} Terdaftar` : 'Kosong'}`);
        console.log(` ${WHITE}    └─${RESET} ${tgCache.chat.name || 'Kosong'}\n`);

        console.log(``);
        console.log(`\n`);

        if (global.isModuleHidden) {
            // Tombol ASCII Dinamis: Misal "5 MODUL" (Hijau)
            const asciiLines = getAsciiLines(`${modulesCache.length} MODUL`);
            asciiLines.forEach(line => console.log(` ${GREEN}${line}${RESET}`));
            console.log();
        } else {
            // Tombol ASCII Dinamis: "HIDE" (Merah)
            const asciiLines = getAsciiLines("HIDE");
            asciiLines.forEach(line => console.log(` ${RED}${line}${RESET}`));
            console.log();

            if (modulesCache.length === 0) {
                console.log(`   ${GRAY}[-] Belum ada modul terikat${RESET}\n`);
            } else {
                modulesCache.forEach((m, i) => {
                    console.log(`${WHITE}[${i + 1}]${RESET} ${CYAN}${m.module}${RESET} > ${GREEN}${m.query}${RESET}`);
                });
                console.log();
            }
        }

        const curPing = global.thanoxPing || 0;
        const pingFormatted = curPing < 1 ? curPing.toFixed(2) : Math.round(curPing);
        const pingColorTag = curPing > 100 ? RED : GREEN;
        console.log(` ${CYAN}[ Android X Notif > ${GREEN}Nethunter${RESET}${CYAN} Via THANOX ] ${pingColorTag}[PING: ${pingFormatted}ms]${RESET}\n`);
        console.log(` ${CYAN}[ WATCHLIST X NOTIF ANDROID ]${RESET}`);
        console.log(` ${WHITE}└─${RESET} ${wlStr}\n`);
        
        console.log(` ${CYAN}[ LIVE DASHBOARD & NOTIFIKASI ]${RESET}`);
        console.log(` ${WHITE}${RESET}`);
        console.log(` ${CYAN}[ THANOX NOTIF (LIVE) ]${RESET}`);
        console.log(` ${WHITE}├─${RESET} 🐦 Target : ${global.uiState.notif}`);
        console.log(` ${WHITE}├─${RESET} 🕒 Waktu  : ${global.uiState.notifTime}`);
        const sn = global.uiState.statsNotif;
        console.log(` ${WHITE}└─${RESET} 📊 Stats  : 🔍 ${sn.found} Post | 🕒 ${sn.history} History`);
        console.log(` ${WHITE}               ${GREEN}✅ ${sn.match} Cocok${RESET} | ${RED}❌ ${sn.blacklist} Blacklist${RESET} | 🔗 ${sn.linkFilter} Filter`);
        console.log(` ${CYAN}${RESET}`);
        console.log(` ${CYAN}[ ENGINE STATUS ]${RESET}`);
        console.log(` ${WHITE}├─${RESET} 🔄 Loop   : ${global.uiState.loop} / ${global.uiState.maxLoop === 0 ? 'Unlimited' : global.uiState.maxLoop}`);
        console.log(` ${WHITE}└─${RESET} 🎯 Target : Query [#${global.uiState.queryId}]`);
        console.log(` ${WHITE}${RESET}`);
        console.log(` ${CYAN}[ STATISTIK SEARCH ]${RESET}`);
        const sl = global.uiState.statsLoop;
        console.log(` ${WHITE}├─${RESET} 📊 Loop : 🔍 ${sl.found} Post | 🕒 ${sl.history} History`);
        console.log(` ${WHITE}│            ${GREEN}✅ ${sl.match} Cocok${RESET} | ${RED}❌ ${sl.blacklist} Blacklist${RESET} | 🔗 ${sl.linkFilter} Filter`);
        console.log(` ${WHITE}│${RESET}`);
        const st = global.uiState.statsTotal;
        console.log(` ${WHITE}└─${RESET} 📈 Total: 🔍 ${st.found} Post | 🕒 ${st.history} History`);
        console.log(` ${WHITE}             ${GREEN}✅ ${st.match} Cocok${RESET} | ${RED}❌ ${st.blacklist} Blacklist${RESET} | 🔗 ${st.linkFilter} Filter`);
        console.log(` ${WHITE}${RESET}`);
        
        // Tombol LOG & STOP dipindah ke kanan atas (sebelah ENGINE SETUP)
        printAt(44, 16, `\x1b[92m██╗      ██████╗  ██████╗\x1b[0m`);
        printAt(44, 17, `\x1b[92m██║     ██╔═══██╗██╔════╝\x1b[0m`);
        printAt(44, 18, `\x1b[92m██║     ██║   ██║██║  ███╗\x1b[0m`);
        printAt(44, 19, `\x1b[92m██║     ██║   ██║██║   ██║\x1b[0m`);
        printAt(44, 20, `\x1b[92m███████╗╚██████╔╝╚██████╔╝\x1b[0m`);
        printAt(44, 21, `\x1b[92m╚══════╝ ╚═════╝  ╚═════╝\x1b[0m`);

        printAt(44, 23, `\x1b[91m███████╗████████╗ ██████╗ ██████╗\x1b[0m`);
        printAt(44, 24, `\x1b[91m██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗\x1b[0m`);
        printAt(44, 25, `\x1b[91m███████╗   ██║   ██║   ██║██████╔╝\x1b[0m`);
        printAt(44, 26, `\x1b[91m╚════██║   ██║   ██║   ██║██╔═══╝ \x1b[0m`);
        printAt(44, 27, `\x1b[91m███████║   ██║   ╚██████╔╝██║     \x1b[0m`);
        printAt(44, 28, `\x1b[91m╚══════╝   ╚═╝    ╚═════╝ ╚═╝     \x1b[0m`);
        
        // Timer teks di bawah dihapus, diganti ASCII raksasa di atas
        process.stdout.write(`\x1b[K`);
    };

    const renderLogViewer = () => {
        clear(); title();
        console.log(` ${CYAN}[ LOG VIEWER & ERROR REPORT ]${RESET}`);
        console.log(` ${WHITE}├─${RESET} Status : ${GREEN}Aktif${RESET}`);
        console.log(` ${WHITE}└─${RESET} Aksi   : Log sent dan error\n`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}${RESET}`);
        console.log(` ${GREEN}[ BERHASIL TERKIRIM ]${RESET}`);
        if (global.uiLogs.success.length === 0) console.log(` ${WHITE}└─${RESET} [-] Belum ada log sukses.`);
        global.uiLogs.success.slice(-5).forEach(log => console.log(log));
        
        let sEmpty = 5 - Math.min(global.uiLogs.success.length, 5);
        for(let i=0; i<sEmpty; i++) console.log('');

        console.log(`\n ${RED}[ ERROR / RATE LIMIT ]${RESET}`);
        if (global.uiLogs.errors.length === 0) console.log(` ${WHITE}└─${RESET} [-] Aman, tidak ada error.`);
        global.uiLogs.errors.slice(-5).forEach(log => console.log(log));
        
        let eEmpty = 5 - Math.min(global.uiLogs.errors.length, 5);
        for(let i=0; i<eEmpty; i++) console.log('');

        // Tombol KEMBALI (Dashboard) & STOP di Log Viewer dipindah ke kanan atas
        printAt(44, 16, `\x1b[92m██╗  ██╗ ██████╗ ███╗   ███╗███████╗\x1b[0m`);
        printAt(44, 17, `\x1b[92m██║  ██║██╔═══██╗████╗ ████║██╔════╝\x1b[0m`);
        printAt(44, 18, `\x1b[92m███████║██║   ██║██╔████╔██║█████╗\x1b[0m`);
        printAt(44, 19, `\x1b[92m██╔══██║██║   ██║██║╚██╔╝██║██╔══╝\x1b[0m`);
        printAt(44, 20, `\x1b[92m██║  ██║╚██████╔╝██║ ╚═╝ ██║███████╗\x1b[0m`);
        printAt(44, 21, `\x1b[92m╚═╝  ╚═╝ ╚═════╝ ╚═╝     ╚═╝╚══════╝\x1b[0m`);

        printAt(44, 23, `\x1b[91m███████╗████████╗ ██████╗ ██████╗\x1b[0m`);
        printAt(44, 24, `\x1b[91m██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗\x1b[0m`);
        printAt(44, 25, `\x1b[91m███████╗   ██║   ██║   ██║██████╔╝\x1b[0m`);
        printAt(44, 26, `\x1b[91m╚════██║   ██║   ██║   ██║██╔═══╝ \x1b[0m`);
        printAt(44, 27, `\x1b[91m███████║   ██║   ╚██████╔╝██║     \x1b[0m`);
        printAt(44, 28, `\x1b[91m╚══════╝   ╚═╝    ╚═════╝ ╚═╝     \x1b[0m\n`);
    };

    if (isSimpleUI) {
        if (process.stdin.isTTY) process.stdin.setRawMode(true);

        process.stdout.write('\x1b[?1000h\x1b[?1015h\x1b[?1006h'); 

        process.stdin.on('data', (buffer) => {
            const key = buffer.toString();
            if (key === '\u0003') { 
                process.stdout.write('\x1b[?1000l\x1b[?1015l\x1b[?1006l\x1b[?25h');
                process.exit(0); 
            }
            
            const match = key.match(/\x1b\[<(\d+);(\d+);(\d+)([Mm])/);
            if (match && match[4] === 'M') {
                const x = parseInt(match[2]);
                const y = parseInt(match[3]);

                if (!global.isLogViewerOpen) {
                    // Hitbox Tombol ASCII Modul (Y: 29-36, X: 1-60)
                    if (y >= 29 && y <= 36 && x >= 1 && x <= 60) {
                        global.isModuleHidden = !global.isModuleHidden;
                        renderDashboard();
                    }
                    // Hitbox Tombol LOG (Dashboard) (Y: 16-21, X: 44-72)
                    else if (y >= 16 && y <= 21 && x >= 44 && x <= 72) { 
                        global.isLogViewerOpen = true;
                        renderLogViewer();
                    } 
                    // Hitbox Tombol STOP (Dashboard) (Y: 23-28, X: 44-76)
                    else if (y >= 23 && y <= 28 && x >= 44 && x <= 76) {
                        process.stdout.write('\x1b[?1000l\x1b[?1015l\x1b[?1006l\x1b[?25h');
                        console.log(`\n\n\n\n\n ${RED}🛑 Dihentikan dari Dashboard.${RESET}`);
                        process.exit(0);
                    }
                } else {
                    // Hitbox Tombol KEMBALI ke Dashboard di Log Viewer (Y: 16-21, X: 44-74)
                    if (y >= 16 && y <= 21 && x >= 44 && x <= 74) {
                        global.isLogViewerOpen = false;
                        renderDashboard();
                    } 
                    // Hitbox Tombol STOP di Log Viewer (Y: 23-28, X: 44-76)
                    else if (y >= 23 && y <= 28 && x >= 44 && x <= 76) {
                        process.stdout.write('\x1b[?1000l\x1b[?1015l\x1b[?1006l\x1b[?25h');
                        console.log(`\n\n\n\n\n ${RED}🛑 Dihentikan dari Log Viewer.${RESET}`);
                        process.exit(0);
                    }
                }
            }
        });
    }

    // ==========================================
    // LOKAL SERVER HTTP UNTUK MODE NOTIFIKASI
    // Berjalan paralel dengan loop scraping interval
    // ==========================================
    if (!global.notifServerRunning) {
        const server = http.createServer(async (req, res) => {
            const parsedUrl = url.parse(req.url, true);
            
            if (parsedUrl.pathname === '/hide-keyboard') {
                res.writeHead(200, { 'Content-Type': 'text/plain' });
                res.end('KEYBOARD_HIDE_SIGNAL_SENT');
                return;
            }

            if (parsedUrl.pathname === '/notif') {
                const rawData = parsedUrl.query.data || '';
                const decodedData = decodeURIComponent(rawData);
                const senderParts = decodedData.split('|');
                
                let senderHandleRaw = senderParts[0].trim();
                let senderHandle = senderHandleRaw.split(':')[0].trim();
                let msgContent = senderParts.slice(1).join('|').trim();

                const startPing = process.hrtime();
                const pingTime = process.hrtime(startPing);
                const pingMs = (pingTime[0] * 1000) + (pingTime[1] / 1000000);
                global.thanoxPing = pingMs;
                let pingColor = GREEN;
                if (pingMs > 50 && pingMs <= 150) pingColor = YELLOW;
                else if (pingMs > 150) pingColor = RED;

                // Pembersih emoji, simbol khusus, dan spasi
                const cleanName = (str) => (str || '').toLowerCase().replace(/[^a-z0-9_]/g, '');

                let watchlists = global.cachedWatchlists || [];
                
                // Pencocokan fleksibel: Cek ke w.name maupun w.handle
                const matchedTarget = watchlists.find(w => {
                    const nameVal = typeof w === 'object' ? w.name : w;
                    const handleVal = typeof w === 'object' ? w.handle : w;
                    
                    const cleanWatchName = cleanName(nameVal);
                    const cleanWatchHandle = cleanName(handleVal);
                    const cleanSender = cleanName(senderHandle);
                    
                    if (!cleanSender) return false;
                    return cleanSender === cleanWatchName || 
                           cleanSender.includes(cleanWatchName) || 
                           cleanSender === cleanWatchHandle ||
                           cleanSender.includes(cleanWatchHandle);
                });

                const isTarget = Boolean(matchedTarget);

                // Ambil handle asli tanpa '@' untuk membuka profil X
                let cleanHandle = senderHandle;
                if (matchedTarget && typeof matchedTarget === 'object' && matchedTarget.handle) {
                    cleanHandle = matchedTarget.handle.replace(/^@/, '').trim();
                }

                // UPDATE UI STATE & RENDER
                global.uiState.notif = `${senderHandle} (@${cleanHandle}) | ${msgContent} (${pingMs}ms)`;
                global.uiState.notifTime = formatWIB(new Date());
                
                if (isSimpleUI && !global.isLogViewerOpen) {
                    renderDashboard();
                } else if (!isSimpleUI) {
                    process.stdout.write('\r\x1b[K'); 
                    console.log(`\n 🐦 ${CYAN}Notif:${RESET} ${GREEN}@${senderHandle}${RESET} | ${msgContent} ${GRAY}[Ping: ${pingColor}${pingMs}ms${GRAY}]${RESET}`);
                    console.log(`\n ${GRAY}${global.uiState.notifTime}${RESET} `);
                }

                if (isTarget && global.activeBrowserContext) {
                    const targetUrl = `https://x.com/${cleanHandle}/with_replies`;
                    
                    // PAUSE COUNTDOWN UTAMA
                    global.isNotifRunning = true; 
                    process.stdout.write('\r\x1b[K');
                    
                    console.log(` ${GREEN}🎯 Target Notif (@${senderHandle}) memposting!${RESET}`);
                    console.log(` ${CYAN}⏳ [PAUSE] Menjeda countdown utama demi fokus scraping...${RESET}`);
                    console.log(` ${CYAN}🔗 Membuka Profil : ${GREEN}${targetUrl}${RESET}`);
                    console.log(` ${GRAY}⏳ [LIVE] Menavigasi & memuat grafik GraphQL secara penuh...${RESET}`);
                    
                    try {
                        let profilePage = global.activeCurrentPage;
                        let dataIntercepted = false; // Penanda sukses
                        
                        profilePage.on('response', async (response) => {
                            const urlReq = response.url();
                            // MENANGKAP SEMUA ENDPOINT GRAPHQL (Mencegah lolos data)
                            if (urlReq.includes('graphql')) {
                                try {
                                    const json = await response.json();
                                    let tweetsData = [];
                                    findTweets(json, tweetsData);
                                    
                                    if (tweetsData.length > 0) {
                                        dataIntercepted = true; // Tandai sukses dapat data
                                        let top10 = tweetsData.slice(0, 10);
                                        let unseenTweets = top10.filter(t => !PROCESSED_TWEETS.has(t.id));
                                        
                                        let notifStats = { found: top10.length, history: (top10.length - unseenTweets.length), blacklist: 0, linkFilter: 0, match: 0 };

                                        if (unseenTweets.length > 0) {
                                            process.stdout.write('\r\x1b[K'); // Anti tumpang tindih
                                            if (!isSimpleUI) console.log(` ${GREEN}📥 [LIVE] Data profil disadap! Menganalisis ${unseenTweets.length} post terbaru...${RESET}`);
                                            let currentQueries = global.cachedQueries || [];
                                            let currentBlacklists = global.cachedBlacklists || [];

                                            for (let tweet of unseenTweets) {
                                                let lowerSearchString = tweet.text.toLowerCase();
                                                
                                                const filterCheck = evaluateUnifiedFilter(tweet);
                                                if (!filterCheck.passed) {
                                                    notifStats.linkFilter++;
                                                    saveSentHistory(tweet.id);
                                                    continue;
                                                }

                                                const matchedBlacklist = currentBlacklists.find(bw => bw && lowerSearchString.includes(bw.toLowerCase().trim()));
                                                if (matchedBlacklist) {
                                                    notifStats.blacklist++;
                                                    saveSentHistory(tweet.id);
                                                    continue;
                                                }

                                                let isMatched = currentQueries.length === 0;
                                                let matchString = "Target Notif (Tanpa Keyword)";
                                                
                                                if (currentQueries.length > 0) {
                                                    for (let q of currentQueries) {
                                                        const activeKw = extractKeywords(q);
                                                        const matchedWords = activeKw.filter(kw => {
                                                            const words = kw.split(/\s+/).filter(w => w.length > 2);
                                                            if (words.length === 0) return lowerSearchString.includes(kw.toLowerCase());
                                                            return words.some(w => lowerSearchString.includes(w.toLowerCase()));
                                                        });
                                                        if (matchedWords.length > 0) {
                                                            isMatched = true;
                                                            matchString = matchedWords.join(', ');
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (isMatched) {
                                                    notifStats.match++;
                                                    saveSentHistory(tweet.id);
                                                    
                                                    const matchedMod = getMatchingModule(tweet);
                                                    let moduleNote = matchedMod ? `⚙️ <b>Modul Ter-trigger:</b> <code>${matchedMod.module}</code>\n⏳ <i>Output modul menyusul...</i>\n` : "";
                                                    
                                                    if (isSimpleUI) {
                                                        global.uiLogs.success.push(` ├─ [${formatWIB(tweet.createdAt)}] 🐦 Notif: @${senderHandle}\n │   └─ Match: "${matchString}"\n └─ Link: ${tweet.postLink}`);
                                                        process.stdout.write('\r\x1b[K');
                                                        if (!global.isLogViewerOpen) console.log(` ${GREEN}├─ 🚨 Match Notif ditemukan! (Tersimpan di Log Viewer)${RESET}`);
                                                    } else {
                                                        console.log(`\n ${GREEN}🚨 [NOTIF MODE] Postingan @${senderHandle} match query!${RESET}`);
                                                    }
                                                    
                                                    // 1. Kirim alert Telegram LANGSUNG (Instant)
                                                    await sendTelegramAlert(tweet, matchString + ` [Notif Android]`, moduleNote);
                                                    
                                                    // 2. Jalankan modul di background (Tanpa await)
                                                    detectAndRunModule(tweet);
                                                } else {
                                                    PROCESSED_TWEETS.add(tweet.id);
                                                }
                                            }
                                            
                                            // UPDATE STATS NOTIF
                                            global.uiState.statsNotif = notifStats;
                                            
                                            if (isSimpleUI && !global.isLogViewerOpen) {
                                                renderDashboard();
                                            } else if (!isSimpleUI) {
                                                console.log(`\n ${CYAN}📊 [STATISTIK NOTIF @${senderHandle}]${RESET}`);
                                                console.log(` ├─ Ditemukan     : ${notifStats.found} post`);
                                                console.log(` ├─ Masuk History : ${notifStats.history} post`);
                                                console.log(` ├─ Blacklist     : ${notifStats.blacklist} post`);
                                                if (global.linkFilterActive) console.log(` ├─ Link Filter   : ${notifStats.linkFilter} post`);
                                                console.log(` └─ Target Cocok  : ${GREEN}${notifStats.match} post${RESET}\n`);
                                            }

                                        } else {
                                            process.stdout.write('\r\x1b[K');
                                            console.log(` ${YELLOW}✉️ [NOTIF MODE] Postingan @${senderHandle} sudah di-scrap (History Aman).${RESET}`);
                                        }
                                    } // End if tweetsData.length > 0
                                } catch(e) {}
                            }
                        });

                        try {
                            await profilePage.goto(targetUrl, { waitUntil: 'domcontentloaded', timeout: 30000 });
                            process.stdout.write('\r\x1b[K');
                            console.log(` ${GRAY}⏳ [LIVE] Halaman terbuka. Memicu auto-scroll untuk memuat data GraphQL...${RESET}`);
                            
                            // AUTO-SCROLL KECIL UNTUK MEMICU RENDER TWEET
                            await profilePage.evaluate(() => window.scrollBy(0, 400));
                            await profilePage.waitForTimeout(2000);
                            await profilePage.evaluate(() => window.scrollBy(0, -400));
                        } catch(e) {
                            process.stdout.write('\r\x1b[K');
                            console.log(` ${RED}❌ [NOTIF MODE] Gagal memuat UI profil: ${e.message}${RESET}`);
                        }
                        
                        // Diperpanjang menjadi 25 detik agar proses render & tangkap data lebih stabil
                        setTimeout(async () => {
                            try {
                                if (!dataIntercepted) {
                                    process.stdout.write('\r\x1b[K');
                                    console.log(` ${RED}❌ [NOTIF MODE] Waktu habis (25s). Tidak ada data GraphQL tweet yang berhasil disadap.${RESET}`);
                                }
                                
                                process.stdout.write('\r\x1b[K');
                                console.log(` ${GREEN}✅ [NOTIF MODE] Selesai. Menutup tab profil & melanjutkan countdown...${RESET}`);
                                
                                global.isNotifRunning = false; 
                                // Di WebView engine, halaman tetap standby melayani radar
                            } catch(e) {}
                        }, 25000);

                    } catch(e) {
                        process.stdout.write('\r\x1b[K');
                        console.log(` ${RED}❌ [NOTIF MODE] Gagal membuat tab baru: ${e.message}${RESET}`);
                    }
                } else if (!isTarget) {
                    console.log(` ${YELLOW}ℹ️ [NOTIF MODE] Handle (${senderHandle}) tidak masuk watchlist.${RESET}`);
                }

                res.writeHead(200, { 'Content-Type': 'text/plain' });
                res.end('OK');
            } else {
                res.writeHead(404);
                res.end('Not Found');
            }
        });
        
        server.on('error', (err) => {
            if (err.code === 'EADDRINUSE') {
                global.notifServerRunning = true;
            }
        });
        server.listen(5000, '127.0.0.1');
        global.notifServerRunning = true; // Kunci agar tidak EADDRINUSE saat browser restart

        // Auto-ping cek kesehatan tiap 2 menit (menghitung RTT/Ping aktual)
        const checkThanoxHealth = async () => {
            const start = performance.now();
            try {
                await fetch('http://127.0.0.1:5000/hide-keyboard');
                global.thanoxPing = performance.now() - start;
            } catch(e) {}
        };
        checkThanoxHealth(); // Jalankan langsung saat awal startup
        setInterval(checkThanoxHealth, 120000);
    }
    // ==========================================

    const intervalMinutes = parseFloat(getArg('--interval', 0));
    const loopCount = parseInt(getArg('--loop', 0));
    const isMobile = hasArg('--mobile');
    
    // Integrasi argumen terminal --filter atau fallback ke filter_config.json
    const cliFilter = hasArg('--filter');
    if (cliFilter) {
        filterConfig.linkFilterActive = true;
        filterConfig.codeFilterActive = true;
        saveFilterConfig();
    }
    const linkFilterActive = filterConfig.linkFilterActive; 
    global.linkFilterActive = linkFilterActive; 
    const isDryRun = hasArg('--dry-run'); // <--- FITUR DRY RUN (TES UI)
    const hasSession = fs.existsSync(SESSION_FILE);
    let currentLoop = 0;
    
    if (isDryRun) {
        global.uiState.maxLoop = loopCount;
        while (loopCount === 0 || currentLoop < loopCount) {
            currentLoop++;
            global.uiState.loop = currentLoop;
            global.uiState.queryId = ((currentLoop - 1) % selectedQueriesArray.length) + 1;
            
            // Simulasi Data Stats untuk Tes UI
            const f = Math.floor(Math.random() * 20) + 10;
            global.uiState.statsLoop = { found: f, history: Math.floor(f/2), blacklist: 1, linkFilter: 0, match: 2 };
            global.uiState.statsTotal.found += global.uiState.statsLoop.found;
            global.uiState.statsTotal.history += global.uiState.statsLoop.history;
            global.uiState.statsTotal.blacklist += global.uiState.statsLoop.blacklist;
            global.uiState.statsTotal.match += global.uiState.statsLoop.match;
            
            let sec = 5; // Cepat, tiap rotasi 5 detik aja
            while (sec > 0) {
                global.uiState.status = `${sec} detik (DRY RUN)`;
                if (isSimpleUI && !global.isLogViewerOpen) {
                    // Animasi ASCII angka jalan di Y: 1-6
                    updateAsciiTimer(sec, false);
                } else if (!isSimpleUI && !global.isLogViewerOpen) {
                    process.stdout.write(`\r ${CYAN}⏳ Countdown loop berikutnya : ${sec} detik... (DRY RUN)${RESET}   \x1b[K`);
                }
                await new Promise(r => setTimeout(r, 1000));
                sec--;
            }
            if (!isSimpleUI) process.stdout.write('\n');
            if (isSimpleUI && !global.isLogViewerOpen) renderDashboard();
        }
        console.log(`\n ${GREEN}🎉 [DRY RUN] Tes UI Selesai!${RESET}`);
        return;
    }

    const isHeadless = hasArg('--headless');
    
    let isShuttingDown = false;
    acquireNativeWakeLock(); // <--- Nyalakan wake lock saat scraper jalan

    process.once('SIGINT', () => {
        isShuttingDown = true;
        releaseNativeWakeLock(); // <--- Lepaskan wake lock saat keluar aman
        process.stdout.write('\x1b[?1000l\x1b[?1015l\x1b[?1006l\x1b[?25h'); // Matikan sensor mouse & panggil kursor
        console.log(`\n\n ${RED}🛑 [STOP] CTRL+C ditekan. Mematikan X RADAR dengan aman...${RESET}`);
        process.exit(0);
    });

    // FITUR 7: Outer Loop untuk Auto-Healing & Auto-Restart
    while (loopCount === 0 || currentLoop < loopCount) {
        let browser, context, page;
        try {
            if (!global.isLogViewerOpen) { clear(); title(); }
            const tgInfo = await getCachedTelegramInfo();
            const isTgReady = tgConfig.bot_token && tgConfig.chat_id && tgInfo.bot.id !== 'Unknown';
            let modules = fs.existsSync(MODULE_FILE) ? JSON.parse(fs.readFileSync(MODULE_FILE, 'utf-8')) : [];
            let watchlists = fs.existsSync(WATCHLIST_FILE) ? JSON.parse(fs.readFileSync(WATCHLIST_FILE, 'utf-8')) : [];

            if (!global.isLogViewerOpen && !isSimpleUI) {
                console.log(`\n ${CYAN}[ ENGINE SETUP ]${RESET}`);
                console.log(` ├─ Engine   : Android WebView Native (CDP: 9222)`);
                console.log(` ├─ Profil   : ${currentProfile ? currentProfile : 'Default / Guest'}`);
                console.log(` └─ Mode     : ${isHeadless ? 'Background / Minimized' : 'Active Screen'}`);

                console.log(`\n ${CYAN}[ TELEGRAM SETUP ]${RESET}`);
                console.log(` ├─ [${isTgReady ? '✅' : '❌'}] API Telegram : ${tgInfo.bot.username || tgInfo.bot.name}`);
                console.log(` ├─ [${tgConfig.bot_token ? '✅' : '❌'}] Bot Token    : ${tgInfo.bot.name}`);
                console.log(` ├─ [${tgConfig.chat_id ? '✅' : '❌'}] Chat ID      : ${tgConfig.chat_id ? '1 Terdaftar' : 'Kosong'}`);
                console.log(` │   └─ ${tgInfo.chat.name}`);

                console.log(`\n ${CYAN}[ MODUL EKSEKUSI ]${RESET}`);
                console.log(` ├─ Relasi Modul   : ${GREEN}${modules.length}${RESET} Siap Eksekusi`);
                if (modules.length === 0) console.log(` │   └─ [-] Belum ada modul terikat`);
                else modules.forEach((m, i) => console.log(`${i === modules.length - 1 ? ' │   └─' : ' │   ├─'} ${m.module} > ${m.query}`));

                const logPingVal = global.thanoxPing || 0;
                const logPingFormatted = logPingVal < 1 ? logPingVal.toFixed(2) : Math.round(logPingVal);
                const logPingCol = logPingVal > 100 ? RED : GREEN;
                console.log(`\n ${CYAN}[ THANOX ] Android X Notif > Nethunter Via 127.0.0.1:5000 ${logPingCol}[PING: ${logPingFormatted}ms]${RESET}`);
                console.log(`\n ${CYAN}[ WATCHLIST X NOTIF ANDROID ]${RESET}`);
                if (watchlists.length === 0) console.log(` └─ [-] Belum ada handle yang dipantau via notif`);
                else watchlists.forEach((w, i) => {
                    const hw = typeof w === 'object' ? w.handle.replace(/^@/, '') : w.replace(/^@/, '');
                    const nm = typeof w === 'object' ? w.name : null;
                    const displayStr = nm ? `@${hw}=${GREEN}${nm}${RESET}` : `@${hw}`;
                    console.log(`${i === watchlists.length - 1 ? ' └─' : ' ├─'} ${displayStr}`);
                });
            }

            console.log(` \x1b[96m🌐 [WEBVIEW ENGINE] Menghubungkan ke Android WebView Native via CDP (Port 9222)...${currentProfile ? ` [Profile: ${currentProfile}]` : ''}\x1b[0m`);
            const conn = await ensureWebViewCDP({ profile: currentProfile });
            browser = conn.browser;
            context = conn.context;
            page = conn.page;
            global.activeBrowserContext = context; 
            global.activeCurrentPage = page; 
            if (isMobile) {
                await page.route('**/*', (route) => {
                    const req = route.request();
                    const type = req.resourceType();
                    const url = req.url().toLowerCase();

                    // 1. Blokir tipe resource bawaan (gambar, video, font)
                    if (['image', 'media', 'font'].includes(type)) {
                        return route.abort();
                    }
                    
                    // 2. Blokir stream video Twitter yang lolos via Fetch/XHR
                    if (url.includes('video.twimg.com') || url.includes('.m3u8') || url.includes('.mp4')) {
                        return route.abort();
                    }

                    route.continue();
                });
            }

            console.log(`\n ${CYAN}⏳ Memeriksa status sesi X...${RESET}`);
            await page.goto("https://x.com/home", { waitUntil: 'domcontentloaded' });
            await page.waitForTimeout(3000); 

            if (page.url().includes('login') || page.url() === 'https://x.com/') {
                console.log(`\n ${YELLOW}⚠️ Sesi kedaluwarsa. Memulai Auto-Login dengan Bantuan Mesin...${RESET}`);
                let accs = fs.existsSync(X_ACCOUNTS_FILE) ? JSON.parse(fs.readFileSync(X_ACCOUNTS_FILE, 'utf-8')) : [];
                if (accs.length === 0) {
                    console.log(` ${RED}❌ Tidak ada akun X tersimpan. Tambahkan di Menu Kelola Akun X!${RESET}`);
                    await browser.close(); process.exit(1);
                }
                
                const acc = accs[0]; 
                console.log(` ${CYAN}🔑 Mencoba login dengan akun: ${acc.username}${RESET}`);
                
                let successLogin = false;

                try {
                    await page.goto("https://x.com/i/flow/login");
                    const selUser = aiSelectors.username || 'input[autocomplete="username"]';
                    await page.waitForSelector(selUser, { timeout: 10000 });
                    await page.fill(selUser, acc.username);
                    await page.keyboard.press('Enter');
                    
                    const passOrEmail = await Promise.race([
                        page.waitForSelector(aiSelectors.password || 'input[name="password"]', { timeout: 8000 }).then(() => 'password'),
                        page.waitForSelector(aiSelectors.email || 'input[data-testid="ocfEnterTextTextInput"]', { timeout: 8000 }).then(() => 'email')
                    ]).catch(() => 'unknown');

                    if (passOrEmail === 'email') {
                        console.log(` ${YELLOW}❌ Terdeteksi verifikasi keamanan (Unusual Login), mem-bypass dengan email...${RESET}`);
                        const selEmail = aiSelectors.email || 'input[data-testid="ocfEnterTextTextInput"]';
                        await page.fill(selEmail, acc.email || acc.username);
                        await page.keyboard.press('Enter');
                        await page.waitForSelector(aiSelectors.password || 'input[name="password"]', { timeout: 10000 });
                    }

                    const selPass = aiSelectors.password || 'input[name="password"]';
                    await page.fill(selPass, acc.password);
                    await page.keyboard.press('Enter');
                    
                    await page.waitForURL('https://x.com/home', { timeout: 20000 });
                    successLogin = true;
                } catch (e) {
                    console.log(` ${YELLOW}⚠️ Auto-login standar gagal. Memanggil AI untuk menganalisa form struktur baru...${RESET}`);
                    try {
                        const htmlStr = await page.evaluate(() => document.body.innerHTML.substring(0, 10000));
                        const prompt = `Ini sebagian HTML dari halaman login Twitter/X yang terus update: ${htmlStr}\n\nTolong analisa dan berikan CSS selector yang PALING TEPAT untuk:\n1. Input username\n2. Input password\n3. Input email (jika ditanya unusual activity)\nBalas HANYA dengan JSON murni: {"username": "selector", "password": "selector", "email": "selector"}.`;
                        
                        const aiRes = await askAIDeepSeek(prompt);
                        if (aiRes && aiRes.username && aiRes.password) {
                            console.log(` ${GREEN}🤖 AI berhasil memecahkan form! Menyimpan data selector ke sistem...${RESET}`);
                            aiSelectors = { username: aiRes.username, password: aiRes.password, email: aiRes.email };
                            saveAiSelectors();
                            
                            await page.goto("https://x.com/i/flow/login");
                            await page.waitForSelector(aiSelectors.username, { timeout: 15000 });
                            await page.fill(aiSelectors.username, acc.username);
                            await page.keyboard.press('Enter');
                            await page.waitForTimeout(3000);
                            await page.fill(aiSelectors.password, acc.password);
                            await page.keyboard.press('Enter');
                            
                            await page.waitForURL('https://x.com/home', { timeout: 20000 });
                            successLogin = true;
                        } else {
                            throw new Error("AI memberikan respon invalid.");
                        }
                    } catch (aiErr) {
                        console.log(` ${RED}❌ AI Fallback gagal menembus keamanan X: ${aiErr.message}${RESET}`);
                        console.log(`\n ${YELLOW}⚠️ Sesi X kadaluwarsa dan auto-login gagal.${RESET}`);
                        console.log(` ${CYAN}💡 Harap jalankan perintah berikut di terminal untuk login manual:${RESET}`);
                        console.log(`    ${GREEN}node x.js --login${RESET}\n`);
                        await browser.close(); process.exit(1);
                    }
                }

                if (successLogin) {
                    console.log(` ${GREEN}✅ Login berhasil! Menyimpan sesi baru...${RESET}`);
                    await context.storageState({ path: SESSION_FILE });
                }
            } else {
                console.log(` ${GREEN}✅ Sesi masih aktif. Melanjutkan Radar...${RESET}`);
            }

            let isProcessingResponse = false;
            let rateLimitHit = false;
            let loopStats = { found: 0, history: 0, blacklist: 0, linkFilter: 0, match: 0 };
            let activeKeywords = [];

            page.on('response', async (response) => {
                const url = response.url();

                // 1. Deteksi Rate Limit Twitter via HTTP 429
                if (response.status() === 429 && url.includes('graphql')) {
                    rateLimitHit = true;
                    return;
                }

                // 2. Deteksi Rate Limit Twitter via GraphQL JSON Body (Status 200 tapi berisi Error Code 88)
                if (url.includes('graphql') && (url.includes('SearchTimeline') || url.includes('TweetResultByRestId'))) {
                    try {
                        const cloneRes = response.clone();
                        const jsonCheck = await cloneRes.json().catch(() => null);
                        if (jsonCheck && jsonCheck.errors && jsonCheck.errors.some(e => e.code === 88 || (e.message && e.message.toLowerCase().includes('rate limit')))) {
                            console.log(` \x1b[91m⚠️ [TWITTER RATE LIMIT] Terdeteksi Error 88 (Rate limit exceeded) pada GraphQL X!\x1b[0m`);
                            rateLimitHit = true;
                            return;
                        }
                    } catch(e) {}

                    try {
                        isProcessingResponse = true;
                        
                        let currentBlacklists = global.cachedBlacklists || [];

                        const json = await response.json();
                        let tweetsData = [];
                        findTweets(json, tweetsData);

                        loopStats.found += tweetsData.length;

                        let unseenTweets = tweetsData.filter(t => !PROCESSED_TWEETS.has(t.id));
                        loopStats.history += (tweetsData.length - unseenTweets.length);

                        if (unseenTweets.length > 0) {
                            if (!isSimpleUI) console.log(` ${CYAN}💻 [PROSES] Memproses ${unseenTweets.length} post baru untuk Regex & Blacklist...${RESET}`);
                        }

                        for (let tweet of unseenTweets) {
                            let fullSearchString = tweet.text;
                            let lowerSearchString = fullSearchString.toLowerCase();

                            // Evaluasi menggunakan filter terpadu (Logika OR)
                            const filterCheck = evaluateUnifiedFilter(tweet);
                            if (!filterCheck.passed) {
                                loopStats.linkFilter++;
                                saveSentHistory(tweet.id);
                                continue; 
                            }

                            const matchedBlacklist = currentBlacklists.find(bw => bw && lowerSearchString.includes(bw.toLowerCase().trim()));
                            if (matchedBlacklist) {
                                loopStats.blacklist++;
                                saveSentHistory(tweet.id);
                                if (!isSimpleUI) console.log(` ${RED}⚠️ [BLACKLIST] Post dilewati (${tweet.postLink}) -> Kata: "${matchedBlacklist}"${RESET}`);
                                continue; 
                            }

                            const matchedWords = activeKeywords.filter(kw => {
                                const words = kw.split(/\s+/).filter(w => w.length > 2);
                                if (words.length === 0) return lowerSearchString.includes(kw.toLowerCase());
                                return words.some(w => lowerSearchString.includes(w.toLowerCase()));
                            });
                            
                            if (matchedWords.length > 0 || activeKeywords.length === 0) {
                                loopStats.match++;
                                const matchString = matchedWords.length > 0 ? matchedWords.join(', ') : 'Algoritma X (Tanpa Keyword)';
                                saveSentHistory(tweet.id); 
                                
                                const matchedMod = getMatchingModule(tweet);
                                let moduleNote = matchedMod ? `⚙️ <b>Modul Ter-trigger:</b> <code>${matchedMod.module}</code>\n⏳ <i>Output modul menyusul...</i>\n` : "";
                                
                                if (isSimpleUI) {
                                    global.uiLogs.success.push(` ├─ [${formatWIB(tweet.createdAt)}] 🔍 Search\n │   └─ Match: "${matchString}"\n └─ Link: ${tweet.postLink}`);
                                } else {
                                    console.log(`\n ${GREEN}🚨 X RADAR QUERY MATCH!${RESET}`);
                                    console.log(` ${CYAN}├─ Post   : ${GREEN}${tweet.postLink}${RESET}`);
                                    console.log(` ${CYAN}├─ Teks   : ${RESET}${tweet.text.replace(/\n/g, ' ').substring(0, 150)}...`);
                                    if (tweet.mediaUrls && tweet.mediaUrls.length > 0) {
                                        console.log(` ${CYAN}├─ Media  : ${CYAN}${tweet.mediaUrls.length} Foto terlampir${RESET}`);
                                    }
                                    console.log(` ${CYAN}├─ Match  : ${GREEN}${matchString}${RESET}`);
                                    console.log(` ${CYAN}├─ Link   : ${GREEN}${[...new Set(tweet.urls)].join(', ') || 'Tidak ada eksternal link'}${RESET}`);
                                }
                                
                                // 1. Kirim Telegram Alert LANGSUNG (Instant)
                                const tgStatus = await sendTelegramAlert(tweet, matchString, moduleNote);

                                // 2. Jalankan modul di background (Tanpa await)
                                detectAndRunModule(tweet);

                                // --- 3. AUTO-TRIGGER SCAN KOMEN (MULTI-KATA DENGAN LOGIKA AND) ---
                                if (scanKomenTriggerConfig.active && scanKomenTriggerConfig.keywords.length > 0) {
                                    // PENGAMAN ANTI-BENTROK: Cek apakah post/kode sudah pernah dieksekusi sebelumnya
                                    const extractedCodes = extractCodesFromText(tweet.text, tweet.urls || []);
                                    const translatedObj = applyAutoTranslate(extractedCodes);
                                    const hasExecutedCode = translatedObj.some(o => PROCESSED_CODES.has(o.translated) || PROCESSED_CODES.has(o.original));
                                    
                                    // Hapus cek PROCESSED_TWEETS karena tweet.id baru saja di-save ke riwayat sesaat sebelum blok ini
                                    if (hasExecutedCode) {
                                        console.log(` \x1b[93m⚠️ [SKIP AUTOSCAN] Kode di dalam post ${tweet.id} sudah pernah dieksekusi. Auto-scan dilewati agar tidak bentrok.\x1b[0m`);
                                    } else {
                                        const tweetTextLower = (tweet.text || "").toLowerCase();
                                        
                                        // Cari apakah ada trigger yang memenuhi syarat (Semua kata dalam 1 trigger harus ada)
                                        let matchedTriggerKw = null;
                                        
                                        for (let rawKw of scanKomenTriggerConfig.keywords) {
                                            if (!rawKw) continue;
                                            // Pecah berdasarkan simbol pemisah, misalnya spasi, '&', atau '+'
                                            // Contoh format trigger: "Gate & 评论区" atau "airdrop giveaway"
                                            const subKeywords = rawKw.split(/[\s&+\|]+/).filter(Boolean).map(k => k.toLowerCase());
                                            
                                            if (subKeywords.length > 0) {
                                                // Cek apakah SEMUA sub-kata ada di dalam teks tweet (Logika AND)
                                                const allMatched = subKeywords.every(sub => tweetTextLower.includes(sub));
                                                if (allMatched) {
                                                    matchedTriggerKw = rawKw;
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        if (matchedTriggerKw) {
                                            console.log(` \x1b[96m⚡ [AUTO-SCAN KOMEN] Trigger multi-kata "${matchedTriggerKw}" cocok di post ${tweet.postLink}! Memasukkan ke antrean scan...\x1b[0m`);
                                            const activeChatId = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id[0] : tgConfig.chat_id;
                                            
                                            await sendTelegramText(`⚡ <b>Auto-Scan Komen Dipicu!</b>\n🎯 Trigger (Wajib Semua): <code>${matchedTriggerKw}</code>\n🔄 Limit: Max 200 Loop\n🔗 Target: <a href="${tweet.postLink}">Buka di X</a>\n<i>Post dimasukkan ke antrean scan berurutan...</i>`);

                                            enqueueScanKomen(tweet.postLink, activeChatId, scanKomenTriggerConfig.tanpaAkun, 200);
                                        }
                                    }
                                }

                                if (!isSimpleUI) {

                                    if (tgStatus && tgStatus.success) {
                                        console.log(` ${CYAN}├─ Telegram: ${GREEN}Sukses Terkirim ✅${RESET}`);
                                    } else if (tgStatus) {
                                        console.log(` ${CYAN}├─ Telegram: ${RED}Gagal ❌ (${tgStatus.error})${RESET}`);
                                        console.log(` ${CYAN}│   ├─ Bot Info : ${YELLOW}${tgStatus.bot.name} (ID: ${tgStatus.bot.id})${RESET}`);
                                        console.log(` ${CYAN}│   └─ Target ID: ${YELLOW}${tgStatus.chat.name} (ID: ${tgStatus.chat.id})${RESET}`);
                                    }
                                    console.log(` ${CYAN}└─ Waktu  : ${GRAY}${formatWIB(tweet.createdAt)}${RESET}`);
                                }
                            } else {
                                PROCESSED_TWEETS.add(tweet.id); 
                            }
                        }
                        commitSentHistoryToDisk();
                    } catch (e) { 
                    } finally {
                        isProcessingResponse = false;
                    }
                }
            });

            while (loopCount === 0 || currentLoop < loopCount) {
                currentLoop++;
                global.persistentLoop++;
                fs.writeFile(LOOP_STATE_FILE, JSON.stringify({ totalLoops: global.persistentLoop }), () => {});

                if (currentLoop > 1 && currentLoop % 50 === 0) {
                    console.log(`\n ${CYAN}🧹 [AUTO-RESTART] Membersihkan cache RAM. Merestart Browser...${RESET}`);
                    await browser.close();
                    break; 
                }

                const qIndex = (currentLoop - 1) % selectedQueriesArray.length;
                const currentQuery = selectedQueriesArray[qIndex];
                activeKeywords = extractKeywords(currentQuery);
                const targetUrl = `https://x.com/search?q=${encodeURIComponent(currentQuery)}&src=typed_query&f=live`;

                loopStats.found = 0; loopStats.history = 0; loopStats.blacklist = 0; loopStats.linkFilter = 0; loopStats.match = 0; 
                
                global.uiState.loop = currentLoop;
                global.uiState.maxLoop = loopCount;
                global.uiState.queryId = qIndex + 1;
                if (isSimpleUI && !global.isLogViewerOpen) renderDashboard();

                const fullLoopTimestamp = formatWIB(new Date());
                
                if (!global.isLogViewerOpen && !isSimpleUI) {
                    console.log(`\n ${CYAN}🔄 [LOOP ${currentLoop}${loopCount > 0 ? `/${loopCount}` : ''}]${RESET}${GRAY}${fullLoopTimestamp}${RESET}`);
                    console.log(` ${CYAN} └─ Memantau : ${RESET}${currentQuery}`);
                    console.log(` ${GRAY}⏳ Sinkronisasi data API X...${RESET}`);
                }
                
                let navSuccess = false;
                let navAttempt = 0;
                const maxNavRetries = 3;

                while (navAttempt < maxNavRetries && !navSuccess) {
                    navAttempt++;
                    try { 
                        const responsePromise = page.waitForResponse(res => res.url().includes('graphql') && res.url().includes('SearchTimeline'), { timeout: 15000 }).catch(() => null);
                        await page.goto(targetUrl, { waitUntil: 'domcontentloaded', timeout: 60000 }); 
                        await responsePromise; 
                        navSuccess = true; // Berhasil, keluar dari loop retry
                    } 
                    catch (err) { 
                        if (navAttempt < maxNavRetries) {
                            console.log(` ${YELLOW}⚠️ [NAVIGASI] Timeout/Gagal (Percobaan ${navAttempt}/${maxNavRetries}), mencoba ulang dalam 5 detik...${RESET}`);
                            await page.waitForTimeout(5000);
                        } else {
                            global.uiLogs.errors.push(` ├─ [${formatWIB(new Date())}] Terjadi kendala (Gagal setelah ${maxNavRetries}x coba): ${err.message}`);
                            if (!global.isLogViewerOpen) console.log(` ${RED}❌ [ERROR] Gagal memuat halaman setelah ${maxNavRetries}x percobaan: ${err.message}${RESET}`); 
                        }
                    }
                }

                while (isProcessingResponse) {
                    await page.waitForTimeout(500);
                }

                global.uiState.statsLoop = loopStats;
                global.uiState.statsTotal.found += loopStats.found;
                global.uiState.statsTotal.history += loopStats.history;
                global.uiState.statsTotal.blacklist += loopStats.blacklist;
                global.uiState.statsTotal.linkFilter += loopStats.linkFilter;
                global.uiState.statsTotal.match += loopStats.match;

                if (isSimpleUI && !global.isLogViewerOpen) {
                    renderDashboard();
                } else if (!global.isLogViewerOpen && !isSimpleUI) {
                    console.log(`\n ${CYAN}📊 [STATISTIK LOOP ${currentLoop}]${RESET}`);
                    console.log(` ├─ Ditemukan     : ${loopStats.found} post`);
                    console.log(` ├─ Masuk History : ${loopStats.history} post`);
                    console.log(` ├─ Blacklist     : ${loopStats.blacklist} post`);
                    if (linkFilterActive) console.log(` ├─ Link Filter   : ${loopStats.linkFilter} post`);
                    console.log(` └─ Target Cocok  : ${GREEN}${loopStats.match} post${RESET}\n`);
                }

                if (rateLimitHit) {
                    // Kirim notifikasi peringatan ke Telegram
                    const rateLimitMsg = `⚠️ <b>TWITTER RATE LIMIT TERDETEKSI! (HTTP 429 / Error 88)</b>\n\n🛡️ <b>Mode Hibernasi Otomatis:</b> 15 Menit\n<i>Akun diistirahatkan sejenak untuk keamanan. Bot akan otomatis melanjutkan pemantauan setelah timer hibernasi selesai.</i>`;
                    await sendTelegramText(rateLimitMsg).catch(() => {});

                    global.uiLogs.errors.push(` ├─ [${formatWIB(new Date())}] Rate Limit 429/88! Hibernasi 15 menit aktif.`);
                    if (!global.isLogViewerOpen) {
                        console.log(` ${YELLOW}⚠️ [RATE LIMIT] X API merespons 429 / Error 88 (Rate limit exceeded).${RESET}`);
                        console.log(` ${YELLOW}❌ Memasuki Mode Hibernasi 15 Menit untuk mengamankan akun...${RESET}`);
                    }
                    for (let sec = 900; sec > 0; sec--) {
                        if (!global.isLogViewerOpen) process.stdout.write(`\r ${YELLOW}⏳ Hibernasi tersisa ${sec} detik...${RESET}   `);
                        await page.waitForTimeout(1000);
                    }
                    if (!global.isLogViewerOpen) process.stdout.write('\n');
                    
                    // Notifikasi selesai hibernasi
                    await sendTelegramText(`✅ <b>Masa Hibernasi Twitter Selesai!</b>\n<i>Melanjutkan pemantauan X Radar...</i>`).catch(() => {});
                    rateLimitHit = false;
                    continue; 
                }

                const baseWait = intervalMinutes > 0 ? (intervalMinutes * 60) : 10;
                const jitter = Math.floor(Math.random() * 11) - 5; 
                const waitTotalSeconds = Math.max(5, baseWait + jitter); 
                
                let sec = waitTotalSeconds;
                while (sec > 0) {
                    if (global.isNotifRunning) {
                        global.uiState.status = '[PAUSED] Fokus memproses Notifikasi...';
                    } else {
                        global.uiState.status = `${sec} detik`;
                    }
                    
                    if (isSimpleUI && !global.isLogViewerOpen) {
                        // Animasi ASCII angka jalan di Y: 1-6
                        updateAsciiTimer(sec, global.isNotifRunning);
                    } else if (!isSimpleUI && !global.isLogViewerOpen) {
                        if (global.isNotifRunning) process.stdout.write(`\r ${YELLOW}⏲️ [COUNTDOWN PAUSED] Menunggu proses Notif Mode...${RESET}   \x1b[K`);
                        else process.stdout.write(`\r ${CYAN}⏳ Countdown loop berikutnya : ${sec} detik...${RESET}   \x1b[K`);
                    }
                    await page.waitForTimeout(1000);
                    sec--;
                }
                // Hilangkan \n di SimpleUI agar layout tidak terguling ke bawah
                if (!isSimpleUI) process.stdout.write('\n');
            }

            if (loopCount !== 0 && currentLoop >= loopCount) {
                console.log(`\n ${GREEN}🎉 [INFO] Selesai mencapai batas loop!${RESET}`);
                if (browser) await browser.close();
                break; 
            }

        } catch (fatalError) {
            if (isShuttingDown) return;
            console.log(`\n ${RED}💥 [CRASH DETECTED] Browser crash atau jaringan putus: ${fatalError.message}${RESET}`);
            console.log(` ${YELLOW}🔄 Mencoba Auto-Healing (Respawn) dalam 10 detik...${RESET}`);
            if (browser) await browser.close().catch(()=>{});
            global.activeBrowserContext = null; 
            await new Promise(res => setTimeout(res, 10000));
        }
    }
}; // <--- PENUTUP FUNGSI startScraping YANG KURANG
// ==========================================
// MANUAL LOGIN MODE (--login)
// ==========================================
async function runLoginMode() {
    clear(); title();
    console.log(` ${CYAN}[ SESI LOGIN ANDROID WEBVIEW (--login) ]${RESET}\n`);

    const prof = currentProfile || 'default';
    console.log(` ${CYAN}🌐 Membuka WebView Android untuk Login X (Profil: ${prof})...${RESET}\n`);
    const { execSync } = require('child_process');
    try {
        execSync(`/usr/local/termux-shims/termux-ipc webview --profile ${prof} https://x.com/i/flow/login`, { stdio: 'inherit' });
    } catch (e) {}

    console.log(`\n ${GREEN}=======================================================${RESET}`);
    console.log(` ${GREEN}👉 Silakan login langsung di WebView Android HP kamu.${RESET}`);
    console.log(` ${YELLOW}💾 Sesi login otomatis tersimpan di WebView pada profil: ${prof}${RESET}`);
    console.log(` ${GREEN}=======================================================${RESET}\n`);
    process.exit(0);
}

async function menuQuery() {
    while (true) {
        clear(); title();
        let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
        
        console.log(` ${CYAN}[ KELOLA QUERY SEARCH X ]${RESET}`);
        console.log(` [1] Tambah Query Search X Baru`);
        console.log(` [2] Lihat Daftar Query X (${queries.length} tersimpan)`);
        console.log(` [3] Hapus Query X`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const q = await ask(` ╰─ Masukkan Query Search X (Contoh: "hongbao" or "iota.gives"): `);
            if (q && !queries.includes(q)) { 
                queries.push(q); 
                fs.writeFileSync(QUERY_FILE, JSON.stringify(queries, null, 2)); 
                console.log(` ${GREEN}✅ Query X berhasil ditambahkan!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '2') {
            console.log(`\n ${CYAN}📂 DAFTAR QUERY SEARCH X TERSIMPAN:${RESET}`);
            if (queries.length === 0) console.log(` [-] Belum ada query.`);
            queries.forEach((q, i) => console.log(`   [${i + 1}] ${q}`));
            await ask(`\n ENTER untuk kembali...`);
        } else if (ans === '3') {
            if (queries.length === 0) {
                console.log(` ${RED}❌ Tidak ada query.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            queries.forEach((q, i) => console.log(`   [${i + 1}] ${q}`));
            const idx = parseInt(await ask(` ╰─ Nomor query yang dihapus: `)) - 1;
            if (!isNaN(idx) && queries[idx]) {
                queries.splice(idx, 1);
                fs.writeFileSync(QUERY_FILE, JSON.stringify(queries, null, 2));
                console.log(` ${GREEN}✅ Query X berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuModule() {
    while (true) {
        clear(); title();
        let modules = fs.existsSync(MODULE_FILE) ? JSON.parse(fs.readFileSync(MODULE_FILE, 'utf-8')) : [];
        
        console.log(` ${CYAN}[ KELOLA MODUL EKSEKUSI ]${RESET}`);
        console.log(` [1] Tambah Modul Baru (Trigger & Command)`);
        console.log(` [2] Lihat Daftar Modul (${modules.length} tersimpan)`);
        console.log(` [3] Hapus Modul`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const trig = await ask(` ╰─ Masukkan Keyword/Domain Trigger (Contoh: iota.gives): `);
            const cmd = await ask(` ╰─ Masukkan Command Eksekusi (Contoh: python3 ~/iota.py): `);
            if (trig && cmd) {
                modules.push({ query: trig, module: cmd });
                fs.writeFileSync(MODULE_FILE, JSON.stringify(modules, null, 2));
                console.log(` ${GREEN}✅ Modul berhasil ditambahkan!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '2') {
            console.log(`\n ${CYAN}📂 DAFTAR MODUL TERSIMPAN:${RESET}`);
            if (modules.length === 0) console.log(` [-] Belum ada modul terikat.`);
            modules.forEach((m, i) => {
                console.log(`   [${i + 1}] Trigger : ${m.query}\n       Modul   : ${GREEN}${m.module}${RESET}`);
            });
            await ask(`\n ENTER untuk kembali...`);
        } else if (ans === '3') {
            if (modules.length === 0) {
                console.log(` ${RED}❌ Tidak ada modul.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            modules.forEach((m, i) => console.log(`   [${i + 1}] Trigger: ${m.query} ➔ Modul: ${m.module}`));
            const idx = parseInt(await ask(` ╰─ Nomor modul yang dihapus: `)) - 1;
            if (!isNaN(idx) && modules[idx]) {
                modules.splice(idx, 1);
                fs.writeFileSync(MODULE_FILE, JSON.stringify(modules, null, 2));
                console.log(` ${GREEN}✅ Modul berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuQueryBlacklist() {
    while (true) {
        clear(); title();
        let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
        
        console.log(` ${CYAN}[ KELOLA BLACKLIST ]${RESET}`);
        console.log(` [1] Tambah Blacklist Word`);
        console.log(` [2] Hapus Blacklist (${blacklists.length} tersimpan)`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const b = await ask(` ╰─ Masukkan Kata Blacklist (Contoh: deposit): `);
            if (b && !blacklists.includes(b)) { blacklists.push(b); fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2)); }
        } else if (ans === '2') {
            blacklists.forEach((b, i) => console.log(`   [${i + 1}] ${b}`));
            const idx = parseInt(await ask(` ╰─ Nomor yang dihapus: `)) - 1;
            if (!isNaN(idx) && blacklists[idx]) { blacklists.splice(idx, 1); fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2)); }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuTelegram() {
    while (true) {
        clear(); title();
        let chatIdsList = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id : [tgConfig.chat_id].filter(Boolean);
        console.log(` ${CYAN}[ KELOLA AKUN TELEGRAM (MULTI ID) ]${RESET}`);
        console.log(` ├─ Token Bot Saat Ini : ${tgConfig.bot_token ? tgConfig.bot_token.substring(0,10)+'...' : 'KOSONG'}`);
        console.log(` ├─ Chat ID Terdaftar  :`);
        if (chatIdsList.length === 0) {
            console.log(` │   └─ KOSONG`);
        } else {
            chatIdsList.forEach((id, i) => {
                console.log(` │   [${i + 1}] ${id}`);
            });
        }
        console.log(` │`);
        console.log(` [1] Set Token Bot`);
        console.log(` [2] Tambah Chat ID Baru`);
        console.log(` [3] Hapus Chat ID Terdaftar`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const t = await ask(` ╰─ Masukkan Bot Token: `);
            if (t) { tgConfig.bot_token = t.trim(); saveTgConfig(); }
        } else if (ans === '2') {
            const c = await ask(` ╰─ Masukkan Chat ID baru yang ingin ditambahkan: `);
            if (c && c.trim()) {
                if (!Array.isArray(tgConfig.chat_id)) tgConfig.chat_id = [tgConfig.chat_id].filter(Boolean);
                if (!tgConfig.chat_id.includes(c.trim())) {
                    tgConfig.chat_id.push(c.trim());
                    saveTgConfig();
                    console.log(` \x1b[92m✅ Chat ID berhasil ditambahkan!\x1b[0m`);
                } else {
                    console.log(` \x1b[93m⚠️ Chat ID tersebut sudah ada.\x1b[0m`);
                }
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '3') {
            if (chatIdsList.length === 0) {
                console.log(` \x1b[91m❌ Tidak ada Chat ID terdaftar.\x1b[0m`);
                await ask(` ENTER untuk lanjut...`);
                continue;
            }
            chatIdsList.forEach((id, i) => console.log(`   [${i + 1}] ${id}`));
            const idx = parseInt(await ask(` ╰─ Nomor Chat ID yang ingin dihapus: `)) - 1;
            if (!isNaN(idx) && chatIdsList[idx]) {
                tgConfig.chat_id = chatIdsList.filter((_, i) => i !== idx);
                saveTgConfig();
                console.log(` \x1b[92m✅ Chat ID berhasil dihapus.\x1b[0m`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuXAccounts() {
    while (true) {
        clear(); title();
        let accs = fs.existsSync(X_ACCOUNTS_FILE) ? JSON.parse(fs.readFileSync(X_ACCOUNTS_FILE, 'utf-8')) : [];
        console.log(` ${CYAN}[ KELOLA AKUN X (AUTO-LOGIN) ]${RESET}`);
        console.log(` [1] Tambah Akun X Baru`);
        console.log(` [2] Hapus Akun (${accs.length} tersimpan)`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const u = await ask(` ╰─ Username X (Handle): `);
            const p = await ask(` ╰─ Password: `);
            const e = await ask(` ╰─ Email/Phone (Untuk bypass kunci keamanan): `);
            if (u && p) { accs.push({ username: u, password: p, email: e }); fs.writeFileSync(X_ACCOUNTS_FILE, JSON.stringify(accs, null, 2)); }
        } else if (ans === '2') {
            accs.forEach((a, i) => console.log(`   [${i + 1}] ${a.username} (${a.email})`));
            const idx = parseInt(await ask(` ╰─ Nomor yang dihapus: `)) - 1;
            if (!isNaN(idx) && accs[idx]) { accs.splice(idx, 1); fs.writeFileSync(X_ACCOUNTS_FILE, JSON.stringify(accs, null, 2)); }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuQueryModule() {
    while (true) {
        clear(); title();
        let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
        let modules = fs.existsSync(MODULE_FILE) ? JSON.parse(fs.readFileSync(MODULE_FILE, 'utf-8')) : [];
        
        console.log(` ${CYAN}[ KELOLA QUERY & MODUL ]${RESET}`);
        console.log(` [1] Tambah Query X Baru`);
        console.log(` [2] Lihat Daftar Query & Relasi Modul`);
        console.log(` [3] Hubungkan Query ke Modul / Script`);
        console.log(` [4] Hapus Relasi Modul`);
        console.log(` [5] Hapus Query`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const q = await ask(` ╰─ Masukkan Keyword Query X: `);
            if (q && !queries.includes(q)) { 
                queries.push(q); 
                fs.writeFileSync(QUERY_FILE, JSON.stringify(queries, null, 2)); 
                console.log(` ${GREEN}✅ Query berhasil ditambahkan!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '2') {
            console.log(`\n ${CYAN}📂 DAFTAR QUERY & MODUL:${RESET}`);
            queries.forEach((q, i) => {
                const foundMod = modules.find(m => m.query.toLowerCase() === q.toLowerCase());
                const modText = foundMod ? `${GREEN}${foundMod.module}${RESET}` : `${YELLOW}Tanpa Modul${RESET}`;
                console.log(`   [${i + 1}] Query : ${q}\n       Modul : ${modText}`);
            });
            await ask(`\n ENTER untuk kembali...`);
        } else if (ans === '3') {
            if (queries.length === 0) {
                console.log(` ${RED}❌ Belum ada query. Tambahkan query dulu!${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            queries.forEach((q, i) => console.log(`   [${i + 1}] ${q}`));
            const qIdx = parseInt(await ask(` ╰─ Pilih nomor query: `)) - 1;
            if (!isNaN(qIdx) && queries[qIdx]) {
                const selectedQuery = queries[qIdx];
                const modulPath = await ask(` ╰─ Masukkan Command / Script Modul (Contoh: python3 ~/iota.py --proxy): `);
                if (modulPath) {
                    let filteredModules = modules.filter(m => m.query.toLowerCase() !== selectedQuery.toLowerCase());
                    filteredModules.push({ query: selectedQuery, module: modulPath });
                    fs.writeFileSync(MODULE_FILE, JSON.stringify(filteredModules, null, 2));
                    console.log(` ${GREEN}✅ Berhasil menghubungkan query ke modul!${RESET}`);
                    await ask(` ENTER untuk lanjut...`);
                }
            }
        } else if (ans === '4') {
            if (modules.length === 0) {
                console.log(` ${RED}❌ Belum ada modul terikat.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            modules.forEach((m, i) => console.log(`   [${i + 1}] Query: ${m.query} ➔ Modul: ${m.module}`));
            const mIdx = parseInt(await ask(` ╰─ Pilih nomor modul yang dihapus: `)) - 1;
            if (!isNaN(mIdx) && modules[mIdx]) {
                modules.splice(mIdx, 1);
                fs.writeFileSync(MODULE_FILE, JSON.stringify(modules, null, 2));
                console.log(` ${GREEN}✅ Relasi modul dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '5') {
            queries.forEach((q, i) => console.log(`   [${i + 1}] ${q}`));
            const idx = parseInt(await ask(` ╰─ Nomor query yang dihapus: `)) - 1;
            if (!isNaN(idx) && queries[idx]) { 
                const deletedQ = queries[idx];
                queries.splice(idx, 1); 
                fs.writeFileSync(QUERY_FILE, JSON.stringify(queries, null, 2)); 
                let filteredModules = modules.filter(m => m.query.toLowerCase() !== deletedQ.toLowerCase());
                fs.writeFileSync(MODULE_FILE, JSON.stringify(filteredModules, null, 2));
                console.log(` ${GREEN}✅ Query & modul terkait dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') break;
    }
}

async function menuBlacklist() {
    while (true) {
        clear(); title();
        let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
        console.log(` ${CYAN}[ KELOLA BLACKLIST WORD ]${RESET}`);
        console.log(` [1] Tambah Kata Blacklist`);
        console.log(` [2] Hapus Kata Blacklist (${blacklists.length} tersimpan)`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const b = await ask(` ╰─ Masukkan kata blacklist: `);
            if (b && !blacklists.includes(b)) { blacklists.push(b); fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2)); }
        } else if (ans === '2') {
            blacklists.forEach((b, i) => console.log(`   [${i + 1}] ${b}`));
            const idx = parseInt(await ask(` ╰─ Nomor yang dihapus: `)) - 1;
            if (!isNaN(idx) && blacklists[idx]) { blacklists.splice(idx, 1); fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2)); }
        } else if (ans === '0' || ans === '') break;
    }
}

// --- AUTO FETCH DISPLAY NAME VIA MOBILE MODE ---
async function fetchDisplayNameFromHandle(handleInput) {
    const cleanHandle = handleInput.replace(/^@/, '').trim();
    let displayName = cleanHandle; // Fallback jika gagal
    let browser = null;

    try {
        console.log(`\n \x1b[96m⏳ [MOBILE FETCH] Membuka profil @${cleanHandle} untuk mengambil nama tampilan 100% akurat...\x1b[0m`);
        const hasSession = fs.existsSync(SESSION_FILE);
        
        const conn = await ensureWebViewCDP({ profile: currentProfile });
        browser = conn.browser;
        const page = conn.page;
        
        let graphqlName = null;
        page.on('response', async (response) => {
            const u = response.url();
            if (u.includes('graphql') && (u.includes('UserByScreenName') || u.includes('UserByRestId') || u.includes('ProfileTimeline'))) {
                try {
                    const json = await response.json();
                    const findName = (obj) => {
                        if (!obj || typeof obj !== 'object') return null;
                        if (obj.legacy && obj.legacy.name) return obj.legacy.name;
                        for (let k in obj) {
                            if (obj[k] && typeof obj[k] === 'object') {
                                const res = findName(obj[k]);
                                if (res) return res;
                            }
                        }
                        return null;
                    };
                    const found = findName(json);
                    if (found) graphqlName = found;
                } catch(e) {}
            }
        });

        await page.goto(`https://x.com/${cleanHandle}`, { waitUntil: 'domcontentloaded', timeout: 20000 });
        await page.waitForTimeout(3000);

        if (graphqlName) {
            displayName = graphqlName;
        } else {
            const pageTitle = await page.title();
            const match = pageTitle.match(/^(.+?)\s*\(@/);
            if (match && match[1]) {
                displayName = match[1].trim();
            }
        }
    } catch (e) {
        console.log(` \x1b[93m⚠️ Gagal mengambil nama otomatis (${e.message}), menggunakan handle sebagai fallback.\x1b[0m`);
    } finally {
        // Tab WebView tetap standby
    }

    return { name: displayName, handle: `@${cleanHandle}` };
}

async function menuWatchlist() {
    while (true) {
        clear(); title();
        let watchlists = fs.existsSync(WATCHLIST_FILE) ? JSON.parse(fs.readFileSync(WATCHLIST_FILE, 'utf-8')) : [];
        console.log(` ${CYAN}[ KELOLA WATCHLIST NOTIF THANOX ]${RESET}`);
        console.log(` [1] Tambah Target via Handle (Auto Fetch Name Mobile)`);
        console.log(` [2] Lihat Daftar Target (${watchlists.length} tersimpan)`);
        console.log(` [3] Hapus Target Notif`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            const handleInput = await ask(` ╰─ Masukkan Handle X Target (Contoh: @0xjamrud atau 0xjamrud): `);
            if (handleInput && handleInput.trim()) {
                const targetData = await fetchDisplayNameFromHandle(handleInput.trim());
                
                const exists = watchlists.some(w => {
                    const h = typeof w === 'object' ? w.handle : w;
                    return h.toLowerCase() === targetData.handle.toLowerCase();
                });

                if (!exists) {
                    watchlists.push(targetData);
                    fs.writeFileSync(WATCHLIST_FILE, JSON.stringify(watchlists, null, 2));
                    console.log(`\n ${GREEN}✅ Target Berhasil Disimpan 100% Akurat!${RESET}`);
                    console.log(`    ├─ Nama Display : ${GREEN}${targetData.name}${RESET}`);
                    console.log(`    └─ Handle X     : ${CYAN}${targetData.handle}${RESET}`);
                } else {
                    console.log(` ${YELLOW}⚠️ Target dengan handle tersebut sudah ada di watchlist.${RESET}`);
                }
                await ask(`\n ENTER untuk lanjut...`);
            }
        } else if (ans === '2') {
            console.log(`\n ${CYAN}📂 DAFTAR WATCHLIST NOTIF:${RESET}`);
            if (watchlists.length === 0) console.log(` [-] Belum ada target dipantau.`);
            watchlists.forEach((w, i) => {
                if (typeof w === 'object') {
                    console.log(`   [${i + 1}] Nama Display : ${GREEN}${w.name}${RESET}\n       Handle X     : ${CYAN}${w.handle}${RESET}`);
                } else {
                    console.log(`   [${i + 1}] Target       : ${YELLOW}${w}${RESET}`);
                }
            });
            await ask(`\n ENTER untuk kembali...`);

        } else if (ans === '3') {
            if (watchlists.length === 0) {
                console.log(` ${RED}❌ Watchlist masih kosong.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            watchlists.forEach((w, i) => {
                const disp = typeof w === 'object' ? `${w.name} (${w.handle})` : w;
                console.log(`   [${i + 1}] ${disp}`);
            });
            const idx = parseInt(await ask(` ╰─ Nomor target yang dihapus: `)) - 1;
            if (!isNaN(idx) && watchlists[idx]) {
                const removed = watchlists[idx];
                const dispRemoved = typeof removed === 'object' ? `${removed.name} (${removed.handle})` : removed;
                watchlists.splice(idx, 1);
                fs.writeFileSync(WATCHLIST_FILE, JSON.stringify(watchlists, null, 2));
                console.log(` ${GREEN}✅ Target "${dispRemoved}" berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') break;
    }
}

async function status_check() {
    let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
    let modules = fs.existsSync(MODULE_FILE) ? JSON.parse(fs.readFileSync(MODULE_FILE, 'utf-8')) : [];
    let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
    let accs = fs.existsSync(X_ACCOUNTS_FILE) ? JSON.parse(fs.readFileSync(X_ACCOUNTS_FILE, 'utf-8')) : [];
    let watchlists = fs.existsSync(WATCHLIST_FILE) ? JSON.parse(fs.readFileSync(WATCHLIST_FILE, 'utf-8')) : [];

    const tgInfo = await getCachedTelegramInfo();
    const botOk = Boolean(tgConfig.bot_token && tgConfig.chat_id && tgInfo.bot.id !== 'Unknown');
    const icBot = botOk ? `${GREEN}✅${RESET}` : `${RED}❌${RESET}`;

    console.log(`\n ${CYAN}STATUS CONFIGURATION${RESET}`);
    console.log(` │`);
    console.log(` ├─ [${icBot}] API Telegram : ${tgInfo.bot.username || tgInfo.bot.name}`);
    console.log(` ├─ [${icBot}] Bot Token    : ${tgInfo.bot.name}`);
    
    const chatIdsCount = Array.isArray(tgConfig.chat_id) ? tgConfig.chat_id.length : (tgConfig.chat_id ? 1 : 0);
    console.log(` ├─ [${icBot}] Chat ID      : ${chatIdsCount > 0 ? `${chatIdsCount} Terdaftar` : 'Kosong'}`);
    console.log(` │   └─ ${tgInfo.chat.name}`);
    console.log(` │`);

    // 2. Relasi Modul / Trigger (modules.json)
    console.log(` ├─ Relasi Modul   : ${GREEN}${modules.length}${RESET} Siap Eksekusi`);
    if (modules.length === 0) {
        console.log(` │   └─ [-] Belum ada modul terikat`);
    } else {
        modules.forEach((m, i) => {
            const prefix = i === modules.length - 1 ? ' │   └─' : ' │   ├─';
            console.log(`${prefix} ${m.module} > ${m.query}`);
        });
    }
    console.log(` │`);

    // 3. Blacklist & Akun X (Terpisah rapi di bawah)
    console.log(` ├─ Blacklist Word : ${GREEN}${blacklists.length}${RESET} Kata`);
    console.log(` ├─ Watchlist Notif: ${GREEN}${watchlists.length}${RESET} Akun Dipantau`);
    console.log(` └─ Akun X Login   : ${GREEN}${accs.length}${RESET} Akun\n`);
}

async function interactiveDashboard() {
    while (true) {
        clear(); title();
        await status_check();
        
        let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
        const isTgReady = tgConfig.bot_token && tgConfig.chat_id;
        const statusText = (queries.length > 0 && isTgReady) ? `${GREEN}System Ready ✅${RESET}` : `${YELLOW}Setup Required ⚠️${RESET}`;

        console.log(` [ CONTROL PANEL ${statusText} ]`);
        console.log(` ├─ [0] Kelola Query Search X`);
        console.log(` ├─ [1] Kelola Modul Eksekusi`);
        console.log(` ├─ [2] Kelola Blacklist`);
        console.log(` ├─ [3] Kelola Watchlist Notif (Thanox)`);
        console.log(` ├─ [4] Kelola Akun Telegram`);
        console.log(` ├─ [5] Kelola Akun X (Auto-Login)`);
        console.log(` ├─ [6] Kelola Filter (Link & Code 8 Digit)`);
        console.log(` ├─ [7] Kelola Auto-Scan Komen Trigger`);
        console.log(` ├─ [8] Lihat Log Aktivitas`);
        console.log(` │`);
        console.log(` ├─ [9] Jalankan 1 Query Target`);
        console.log(` └─ [${GREEN}ENTER${RESET}] ${GREEN}MULAI MENCARI MANGSA (Round-Robin)${RESET}\n`);

        const ans = await ask(` ╰─ Pilih menu: `);
        if (ans === '0') await menuQuery();
        else if (ans === '1') await menuModule();
        else if (ans === '2') await menuBlacklist();
        else if (ans === '3') await menuWatchlist();
        else if (ans === '4') await menuTelegram();
        else if (ans === '5') await menuXAccounts();
        else if (ans === '6') await menuFilterSettings();
        else if (ans === '7') await menuScanKomenTrigger();
        else if (ans === '8') {
            clear(); title();
            console.log(` ${CYAN}[ SENT HISTORY / LOG (DATABASE SQLITE) ]${RESET}`);
            let dbHistory = [];
            try {
                dbHistory = db.prepare('SELECT tweet_id, timestamp FROM sent_history ORDER BY timestamp DESC LIMIT 20').all();
            } catch(e){}

            if (dbHistory.length === 0) console.log(` [-] Belum ada riwayat terkirim.`);
            dbHistory.forEach((row, i) => console.log(` [${i+1}] ID: ${row.tweet_id} | Waktu: ${formatWIB(row.timestamp)}`));
            await ask(`\n ENTER untuk kembali...`);
        }
        else if (ans === '9') {
            if (queries.length === 0) {
                console.log(`\n ${RED}❌ List query masih kosong! Tambahkan minimal 1 query.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            console.log(`\n ${CYAN}[ 🎯 PILIH TARGET QUERY ]${RESET}`);
            queries.forEach((q, i) => console.log(`   [${i + 1}] ${q}`));
            const qAns = await ask(`\n ╰─ Masukkan nomor query: `);
            const idx = parseInt(qAns) - 1;
            if (!isNaN(idx) && queries[idx]) {
                await startScraping([queries[idx]]); 
                break;
            }
        }
        else if (ans === '') {
            if (queries.length === 0) {
                console.log(`\n ${RED}❌ List query masih kosong! Tambahkan minimal 1 query.${RESET}`);
                await ask(` ENTER untuk kembali...`);
                continue;
            }
            await startScraping(queries);
            break;
        }
    }
}

async function main() {
    if (hasArg('--login')) {
        await runLoginMode();
        return;
    }

    if (hasArg('--help') || hasArg('-h')) {
        clear(); title();
        console.log(` ${CYAN}📖 PANDUAN PENGGUNAAN CLI X WEBVIEW${RESET}\n`);
        console.log(`   node x_webview.js            👉 Buka Menu Interaktif (Control Panel)`);
        console.log(`   --profile <nama>             👉 Gunakan profil sesi akun tertentu (contoh: --profile shaqilla)`);
        console.log(`   --headless                   👉 Mode background / hemat daya`);
        console.log(`   --query 'text'               👉 Menambahkan query baru via CLI`);
        console.log(`   --blacklist 'text'           👉 Menambahkan kata blacklist via CLI`);
        console.log(`   --manage                     👉 Melihat dan menghapus list query/blacklist via CLI`);
        console.log(`   --interval N                 👉 Jeda waktu (dalam menit) antar refresh. (Dilengkapi Jitter Acak)`);
        console.log(`   --loop N                     👉 Jumlah pengulangan (0 = Unlimited). (Dilengkapi Auto-Restart tiap 50 loop)`);
        console.log(`   --filter                     👉 Aktifkan filter ganda (Link ATAU Code 8 Digit) via CLI`);
        console.log(`   --run N                      👉 Langsung jalankan query index ke-N tanpa prompt`);
        console.log(`   --run-all                    👉 Langsung jalankan SEMUA query dalam mode Rotasi (Round-Robin)`);
        console.log(`   --debug                      👉 Aktifkan mode Debug / Verbose Log (Mematikan Simple-UI Dashboard)`);
        console.log(`   --dry-run                    👉 Tes simulasi UI tanpa internet & browser\n`);
        process.exit(0);
    }

    if (args.length === 0) {
        await interactiveDashboard();
        return;
    }

    let queries = fs.existsSync(QUERY_FILE) ? JSON.parse(fs.readFileSync(QUERY_FILE, 'utf-8')) : [];
    let blacklists = fs.existsSync(BLACKLIST_FILE) ? JSON.parse(fs.readFileSync(BLACKLIST_FILE, 'utf-8')) : [];
    
    const saveQ = () => fs.writeFileSync(QUERY_FILE, JSON.stringify(queries, null, 2));
    const saveB = () => fs.writeFileSync(BLACKLIST_FILE, JSON.stringify(blacklists, null, 2));

    const newQuery = getQueryArg();
    if (newQuery) {
        if (!queries.includes(newQuery)) { queries.push(newQuery); saveQ(); console.log(` ${GREEN}✅ Query ditambahkan.${RESET}`); }
        return;
    }

    const newBlacklist = getBlacklistArg();
    if (newBlacklist) {
        if (!blacklists.includes(newBlacklist)) { blacklists.push(newBlacklist); saveB(); console.log(` ${GREEN}✅ Blacklist ditambahkan.${RESET}`); }
        return;
    }

    if (hasArg('--manage')) {
        await menuQueryModule(); // ✅ Mengarah ke menu kelola query & modul yang baru
        return;
    }
    
    if (hasArg('--run-all')) {
        if (queries.length === 0) {
            console.log(`\n ${RED}❌ List query masih kosong!${RESET}`);
            process.exit(1);
        }
        await startScraping(queries);
        return;
    }

    const runIndex = getArg('--run', null);
    if (runIndex && !isNaN(runIndex) && queries[runIndex - 1]) {
        await startScraping([queries[runIndex - 1]]);
    } else {
        await interactiveDashboard();
    }
}
// --- MENU INTERAKTIF KELOLA AUTO-SCAN KOMEN TRIGGER ---
async function menuScanKomenTrigger() {
    while (true) {
        clear(); title();
        let keywords = scanKomenTriggerConfig.keywords || [];

        console.log(` ${CYAN}[ KELOLA AUTO-SCAN KOMEN TRIGGER ]${RESET}`);
        console.log(` ├─ Status Auto-Scan : ${scanKomenTriggerConfig.active ? GREEN + 'AKTIF (ON)' : RED + 'MATI (OFF)'}${RESET}`);
        console.log(` ├─ Mode Sesi        : ${scanKomenTriggerConfig.tanpaAkun ? YELLOW + 'Tanpa Akun (Guest Mode)' : GREEN + 'Dengan Sesi Login'}${RESET}`);
        console.log(` └─ Daftar Kata Trigger (Multi-Kata / AND) :`);
        if (keywords.length === 0) {
            console.log(`     [-] Belum ada kata trigger terdaftar.`);
        } else {
            keywords.forEach((kw, idx) => {
                console.log(`     [${idx + 1}] ${GREEN}${kw}${RESET}`);
            });
        }
        console.log();
        console.log(` [ INFO ] Jika diisi multi-kata (misal: airdrop & giveaway), wajib ada semua di post.\n`);

        console.log(` [1] Toggle Saklar Auto-Scan Komen (ON/OFF)`);
        console.log(` [2] Toggle Saklar Mode Sesi (Dengan Sesi / Tanpa Akun)`);
        console.log(` [3] Tambah Kata Trigger Baru`);
        console.log(` [4] Hapus Kata Trigger`);
        console.log(` [0] Kembali ke Control Panel`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            scanKomenTriggerConfig.active = !scanKomenTriggerConfig.active;
            saveScanKomenTriggerConfig();
        } else if (ans === '2') {
            scanKomenTriggerConfig.tanpaAkun = !scanKomenTriggerConfig.tanpaAkun;
            saveScanKomenTriggerConfig();
        } else if (ans === '3') {
            const kwInput = await ask(` ╰─ Masukkan kata trigger baru (Contoh: airdrop giveaway): `);
            if (kwInput && kwInput.trim().length > 0) {
                const cleanKw = kwInput.trim();
                if (!scanKomenTriggerConfig.keywords.includes(cleanKw)) {
                    scanKomenTriggerConfig.keywords.push(cleanKw);
                    saveScanKomenTriggerConfig();
                    console.log(` ${GREEN}✅ Berhasil menambahkan trigger: ${cleanKw}${RESET}`);
                } else {
                    console.log(` ${YELLOW}⚠️ Trigger tersebut sudah ada dalam daftar!${RESET}`);
                }
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '4') {
            if (keywords.length === 0) {
                console.log(` ${RED}❌ Daftar trigger kosong!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
                continue;
            }
            keywords.forEach((kw, idx) => console.log(`   [${idx + 1}] ${kw}`));
            const delIdx = parseInt(await ask(` ╰─ Nomor trigger yang dihapus: `)) - 1;
            if (!isNaN(delIdx) && keywords[delIdx]) {
                scanKomenTriggerConfig.keywords.splice(delIdx, 1);
                saveScanKomenTriggerConfig();
                console.log(` ${GREEN}✅ Trigger berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') {
            break;
        }
    }
}

async function menuFilterSettings() {
    while (true) {
        clear(); title();
        const samples = filterConfig.sampleCodes || ["TONV4JHS"];
        const targets = filterConfig.translateTargets || [];
        const targetStr = targets.length > 0 ? targets.join(', ') : 'Kosong';

        console.log(` ${CYAN}[ KELOLA FILTER (SMART CODE & LINK) ]${RESET}`);
        console.log(` ├─ Status Link Filter : ${filterConfig.linkFilterActive ? GREEN + 'AKTIF (ON)' : RED + 'MATI (OFF)'}${RESET}`);
        console.log(` ├─ Status Code Filter : ${filterConfig.codeFilterActive ? GREEN + 'AKTIF (ON)' : RED + 'MATI (OFF)'}${RESET}`);
        console.log(` ├─ Auto-Translate     : ${filterConfig.autoTranslateActive ? GREEN + 'AKTIF (ON)' : RED + 'MATI (OFF)'}${RESET} (Target Cetakan: ${YELLOW}${targetStr}${RESET})`);
        console.log(` └─ Contoh Kode Aktif (Jaring Penangkap) :`);
        samples.forEach((sc, idx) => {
            const info = parseSampleCodeToRegex(sc);
            const badgeType = info.type.includes('WILDCARD') ? CYAN + '[' + info.type + ']' : YELLOW + '[' + info.type + ']';
            console.log(`     [${idx + 1}] ${GREEN}${sc}${RESET} ${badgeType}${RESET} (Info: ${info.length} ➔ Regex: ${GRAY}${info.regexStr}${RESET})`);
        });
        console.log();
        
        console.log(` [ INFO ] Mode Logika Filter: \x1b[96mOR (Cukup memenuhi salah satu link ATAU kode, maka lolos)\x1b[0m\n`);

        console.log(` [1] Toggle Saklar Link Filter (ON/OFF)`);
        console.log(` [2] Toggle Saklar Code Filter (ON/OFF)`);
        console.log(` [3] Tambah Contoh Kode Baru (Jaring Penangkap)`);
        console.log(` [4] Hapus Contoh Kode`);
        console.log(` [5] Kelola Auto-Translate (Ganti Simbol Otomatis)`);
        console.log(` [0] Kembali ke Control Panel`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            filterConfig.linkFilterActive = !filterConfig.linkFilterActive;
            saveFilterConfig();
        } else if (ans === '2') {
            filterConfig.codeFilterActive = !filterConfig.codeFilterActive;
            saveFilterConfig();
        } else if (ans === '3') {
            const sampleInput = await ask(` ╰─ Masukkan contoh kode (Contoh: GATE?): `);
            if (sampleInput && sampleInput.trim().length > 0) {
                const cleanSample = sampleInput.trim();
                if (!filterConfig.sampleCodes) filterConfig.sampleCodes = [];
                if (!filterConfig.sampleCodes.includes(cleanSample)) {
                    filterConfig.sampleCodes.push(cleanSample);
                    saveFilterConfig();
                    const generated = parseSampleCodeToRegex(cleanSample);
                    console.log(` ${GREEN}✅ Berhasil! Regex otomatis diperbarui: ${generated.regexStr}${RESET}`);
                } else {
                    console.log(` ${YELLOW}⚠️ Contoh kode tersebut sudah ada dalam daftar!${RESET}`);
                }
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '4') {
            if (samples.length <= 1) {
                console.log(` ${RED}❌ Minimal harus menyisakan 1 contoh kode!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
                continue;
            }
            samples.forEach((sc, idx) => console.log(`   [${idx + 1}] ${sc}`));
            const delIdx = parseInt(await ask(` ╰─ Nomor contoh kode yang dihapus: `)) - 1;
            if (!isNaN(delIdx) && samples[delIdx]) {
                filterConfig.sampleCodes.splice(delIdx, 1);
                saveFilterConfig();
                console.log(` ${GREEN}✅ Contoh kode berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '5') {
            await menuAutoTranslate();
        } else if (ans === '0' || ans === '') {
            break;
        }
    }
}

async function menuAutoTranslate() {
    while (true) {
        clear(); title();
        const targets = filterConfig.translateTargets || [];
        console.log(` ${CYAN}[ KELOLA AUTO-TRANSLATE KODE ]${RESET}`);
        console.log(` ├─ Status : ${filterConfig.autoTranslateActive ? GREEN + 'AKTIF (ON)' : RED + 'MATI (OFF)'}${RESET}`);
        console.log(` └─ Daftar Format Output (Kode Benar) :`);
        if (targets.length === 0) console.log(`     [-] Belum ada format output.`);
        targets.forEach((t, idx) => {
            console.log(`     [${idx + 1}] Target: ${GREEN}${t}${RESET} (Simbol pemisah akan ditukar menyesuaikan format ini)`);
        });
        console.log();
        
        console.log(` [1] Toggle Saklar Auto-Translate`);
        console.log(` [2] Tambah Format Kode yang Benar (Contoh: GATE-)`);
        console.log(` [3] Hapus Format`);
        console.log(` [0] Kembali`);

        const ans = await ask(`\n ╰─ Pilih menu: `);
        if (ans === '1') {
            filterConfig.autoTranslateActive = !filterConfig.autoTranslateActive;
            saveFilterConfig();
        } else if (ans === '2') {
            const tInput = await ask(` ╰─ Masukkan format kode yang BENAR (Contoh: GATE-): `);
            if (tInput && tInput.trim().length > 0) {
                const cleanT = tInput.trim();
                if (!filterConfig.translateTargets) filterConfig.translateTargets = [];
                if (!filterConfig.translateTargets.includes(cleanT)) {
                    filterConfig.translateTargets.push(cleanT);
                    saveFilterConfig();
                    console.log(` ${GREEN}✅ Berhasil! Auto-Translate akan menggunakan cetakan: ${cleanT}${RESET}`);
                } else {
                    console.log(` ${YELLOW}⚠️ Format tersebut sudah ada dalam daftar!${RESET}`);
                }
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '3') {
            if (targets.length === 0) {
                console.log(` ${RED}❌ Daftar format kosong!${RESET}`);
                await ask(` ENTER untuk lanjut...`);
                continue;
            }
            targets.forEach((t, idx) => console.log(`   [${idx + 1}] ${t}`));
            const delIdx = parseInt(await ask(` ╰─ Nomor format yang dihapus: `)) - 1;
            if (!isNaN(delIdx) && targets[delIdx]) {
                filterConfig.translateTargets.splice(delIdx, 1);
                saveFilterConfig();
                console.log(` ${GREEN}✅ Format berhasil dihapus.${RESET}`);
                await ask(` ENTER untuk lanjut...`);
            }
        } else if (ans === '0' || ans === '') {
            break;
        }
    }
}

main().catch(err => {
    console.error(`\n ${RED}❌ [FATAL ERROR] ${err.message}${RESET}`);
    process.exit(1);
});
