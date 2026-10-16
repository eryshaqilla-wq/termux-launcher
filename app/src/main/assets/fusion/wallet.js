const path = require('path');
process.chdir(path.dirname(__filename));
require('dotenv').config();
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0"; 
const dns = require('node:dns');
if (dns.setDefaultResultOrder) dns.setDefaultResultOrder('ipv4first'); 

// 🛡️ ANTI-WARNING: Sembunyikan teks kuning jelek di layar terminal
process.removeAllListeners('warning');

const { ethers } = require('ethers');
const readline = require('readline');
const fs = require('fs');

let CONNECTED_RPC_URL = "Memuat...";

// =================
// 1. UI COLORS & UTIL
// =================
const RESET = "\x1b[0m";
const RED = "\x1b[91m";
const GREEN = "\x1b[92m";
const YELLOW = "\x1b[93m";
const CYAN = "\x1b[96m";
const GRAY = "\x1b[90m";
const BOLD = "\x1b[1m";
// 🔥 KOSMETIK: BLOCK BADGES (Warna Background Solid)
const BG_GREEN = "\x1b[42m\x1b[30m\x1b[1m"; // Latar Hijau, Teks Hitam Tebal
const BG_RED = "\x1b[41m\x1b[30m\x1b[1m";   // Latar Merah, Teks Hitam Tebal
const BG_YELLOW = "\x1b[43m\x1b[30m\x1b[1m";// Latar Kuning, Teks Hitam Tebal

function clear() { 
    console.clear();
    process.stdout.write('\x1B[2J\x1B[3J\x1B[H'); 
}

function title() {
    console.log(`${CYAN} ███████╗███████╗ ██████╗  ${GRAY}WALLETCONNECT${RESET}`);
    console.log(`${CYAN} ██╔════╝██╔════╝██╔═══██╗ ${GRAY}GHOSTWALLET${RESET}`);
    console.log(`${CYAN} █████╗  ███████╗██║   ██║ ${GRAY}MULTI EVM${RESET}`);
    console.log(`${CYAN} ██╔══╝  ╚════██║██║   ██║ ${GRAY}TRADINGBOT${RESET}`);
    console.log(`${CYAN} ███████╗███████║╚██████╔╝ ${GRAY}SNIPERBOT${RESET}`);
    console.log(`${CYAN} ╚══════╝╚══════╝ ╚═════╝  ${GRAY}${RESET}`);
    console.log(`${RED} ██╗    ██╗  █████╗  ██║      ██║      ███████╗ ████████╗${RESET}`);
    console.log(`${RED} ██║    ██║ ██╔══██╗ ██║      ██║      ██╔════╝ ╚══██╔══╝${RESET}`);
    console.log(`${RED} ██║ █╗ ██║ ███████║ ██║      ██║      █████╗      ██║   ${RESET}`);
    console.log(`${RED} ██║███╗██║ ██╔══██║ ██║      ██║      ██╔══╝      ██║   ${RESET}`);
    console.log(`${RED} ╚███╔███╔╝ ██║  ██║ ███████╗ ███████╗ ███████╗    ██║   ${RESET}`);
    console.log(`${RED}  ╚══╝╚══╝  ╚═╝  ╚═╝ ╚══════╝ ╚══════╝ ╚══════╝    ╚═╝   ${RESET}`);
    console.log(`\n ${GRAY}${BOLD}Support patchright / playwright / cloakbrowser v146${RESET}\n`);
}

let rl;
let askQuestion;

if (require.main === module) {
    // Mode CLI: Nyalakan penangkap keyboard
    rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    rl.on('SIGINT', () => {
        console.log(`\n\n ${RED}❌ Penutupan paksa (Ctrl+C) terdeteksi. Mematikan bot...${RESET}`);
        process.exit(0);
    });
    askQuestion = (query) => new Promise(resolve => rl.question(query, resolve));
} else {
    // 🔥 FIX BUG DOUBLE KETIK: Matikan penangkap keyboard saat jadi modul In-Memory
    askQuestion = async (query) => { return 'y'; }; // Auto-yes jika ada prompt darurat di background
}

const pause = async () => {
    if (require.main === module) await askQuestion(`\n ${GRAY}Tekan ${GREEN}[ Enter ]${GRAY} untuk kembali ke dashboard...${RESET}`);
};

// =================
// 2. OMNI-CHAIN DATABASE & CONFIG
// =================
const CHAINS_FILE = 'chains_db.json';

function loadChainsDB() {
    if (!fs.existsSync(CHAINS_FILE)) {
        // Default Jaringan Awal (Biar kalau file kehapus, langsung dibikinin lagi)
        const defaultChains = {
            base: { name: "Base Mainnet", id: 8453, symbol: "ETH", cgId: "ethereum", weth: "0x4200000000000000000000000000000000000006", usdc: "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", defaultRpc: ["https://mainnet.base.org", "https://base-rpc.publicnode.com"], v2Router: "0x4752ba5DBc23f44D87826276BF6Fd6b1C372aD24", v2Factory: "0x8909Dc15e40173Ff4699343b6eB8132c65e18eC6", v3Router: "0x2626664c2603336E57B271c5C0b26F421741e481", v3Factory: "0x33128a8fC17869897dcE68Ed026d694621f6FDfD", v3Quoter: "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6", hasAero: true, aeroRouter: "0xcF77a3Ba9A5CA399B7c97c74d54e5b1Beb874E43", aeroFactory: "0x420DD381b31aEf6683db6B902084cB0FFECe40Da", v4Router: "0x3fC91A3afd70395Cd496C647d5a6CC9D4B2b7FAD" },
            bsc: { name: "Binance Smart Chain", id: 56, symbol: "BNB", cgId: "binancecoin", weth: "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c", usdc: "0x55d398326f99059fF775485246999027B3197955", defaultRpc: ["https://bsc-dataseed.binance.org", "https://bsc-rpc.publicnode.com"], v2Router: "0x10ED43C718714eb63d5aA57B78B54704E256024E", v2Factory: "0xca143ce32fe78f1f7019d7d551a6402fc5350c73", v3Router: "0x13f4EA83D0bd40E75C8222255bc855a974568Dd4", v3Factory: "0x0BFbCF9fa4f9C56B0F40a671Ad40E0805A091865", v3Quoter: "0xB048Bbc1Ee6b733FFfCFb9e9CeF7375518e25997", hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress },
            arbitrum: { name: "Arbitrum One", id: 42161, symbol: "ETH", cgId: "ethereum", weth: "0x82aF49447D8a07e3bd95BD0d56f35241523fBab1", usdc: "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9", defaultRpc: ["https://arb1.arbitrum.io/rpc", "https://arbitrum-one-rpc.publicnode.com"], v2Router: "0x1b02dA8Cb0d097eB8D57A175b88c7D8b47997506", v2Factory: "0xc35DADB65012eC5796536bD9864eD8773aBc74C4", v3Router: "0xE592427A0AEce92De3Edee1F18E0157C05861564", v3Factory: "0x1F98431c8aD98523631AE4a59f267346ea31F984", v3Quoter: "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6", hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress },
            polygon: { name: "Polygon (Matic)", id: 137, symbol: "POL", cgId: "matic-network", weth: "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270", usdc: "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174", defaultRpc: ["https://polygon-rpc.com", "https://polygon-bor-rpc.publicnode.com"], v2Router: "0xa5E0829CaCEd8fFDD4De3c43696c57F7D7A678ff", v2Factory: "0x5757371414417b8C6CAad45bAeF941aBc7d3AB32", v3Router: "0xE592427A0AEce92De3Edee1F18E0157C05861564", v3Factory: "0x1F98431c8aD98523631AE4a59f267346ea31F984", v3Quoter: "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6", hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress },
            morph: { name: "Morph L2", id: 2818, symbol: "ETH", cgId: "ethereum", weth: "0x5300000000000000000000000000000000000011", usdc: "0x09bc4E0D864854c6aFB6eB9A9cdF58aC190D0dF9", defaultRpc: ["https://rpc.morphl2.io", "https://morph.drpc.org"], v2Router: "0xAa2c3eb2aCC766d9532EE014022aE8a98Cea5FDE", v2Factory: "0x6A98f1a1d10B74d6A468aB9Bba1c8dD0A4e3412B", v3Router: ethers.ZeroAddress, v3Factory: ethers.ZeroAddress, v3Quoter: ethers.ZeroAddress, hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress }
        };
        fs.writeFileSync(CHAINS_FILE, JSON.stringify(defaultChains, null, 4));
        return defaultChains;
    }
    return JSON.parse(fs.readFileSync(CHAINS_FILE, 'utf8'));
}

function saveChainsDB() {
    fs.writeFile(CHAINS_FILE, JSON.stringify(CHAINS, null, 4), () => {}); // 🔥 Async Write Anti-Freeze
}

let CHAINS = loadChainsDB();

let ACTIVE_CHAIN = "base"; 
let WETH_ADDRESS, USDC_ADDRESS, V2_ROUTER, V2_FACTORY, V3_ROUTER, V3_FACTORY, V3_QUOTER, AERO_ROUTER, AERO_FACTORY, V4_ROUTER, HAS_AERO;
let CONFIG_FILE = 'config_rpc_base.json'; // Default awal

function applyNetwork(chainKey) {
    ACTIVE_CHAIN = chainKey;
    const c = CHAINS[chainKey];
    WETH_ADDRESS = c.weth;
    USDC_ADDRESS = c.usdc;
    V2_ROUTER = c.v2Router; V2_FACTORY = c.v2Factory;
    V3_ROUTER = c.v3Router; V3_FACTORY = c.v3Factory; V3_QUOTER = c.v3Quoter;
    AERO_ROUTER = c.aeroRouter; AERO_FACTORY = c.aeroFactory; HAS_AERO = c.hasAero;
    V4_ROUTER = c.v4Router;
    CONFIG_FILE = `config_rpc_${chainKey}.json`; // File config dipisah agar RPC tiap chain aman
    updateEthPrice();
}

function loadConfig() {
    if (!fs.existsSync(CONFIG_FILE)) {
        let initCfg = { 
            active_mode: "gratis", 
            mode_simulate: "gratis", mode_buy: "premium", mode_sell: "premium",
            mode_multi_buy: "premium", mode_multi_sell: "premium", // 🔥 Fitur Baru!
            premium_urls: [], free_urls: CHAINS[ACTIVE_CHAIN].defaultRpc, 
            active_premium_index: 0, active_free_index: 0,
            slip: { 
                simulate: { min: 15, max: 50, bump: 5 }, 
                buy: { min: 2, max: 20, bump: 2 }, 
                sell: { min: 2, max: 20, bump: 2 },
                multi_buy: { min: 5, max: 25, bump: 5 }, // 🔥 Fitur Baru!
                multi_sell: { min: 5, max: 25, bump: 5 } // 🔥 Fitur Baru!
            },
            gwei: { simulate: 100, buy: 100, sell: 100, multi_buy: 150, multi_sell: 150 } // 🔥 Fitur Baru!
        };
        fs.writeFileSync(CONFIG_FILE, JSON.stringify(initCfg, null, 4));
        return initCfg;
    }
    let cfg = JSON.parse(fs.readFileSync(CONFIG_FILE, 'utf8'));
    
    // 🛠️ MIGRATION: Suntik config baru ke user lama tanpa ngehapus data mereka
    if (!cfg.gwei) cfg.gwei = { simulate: 100, buy: 100, sell: 100, multi_buy: 150, multi_sell: 150 };
    if (!cfg.gwei.multi_buy) { cfg.gwei.multi_buy = 150; cfg.gwei.multi_sell = 150; }
    if (!cfg.mode_multi_buy) { cfg.mode_multi_buy = "premium"; cfg.mode_multi_sell = "premium"; }
    
    if (!cfg.slip || !cfg.slip.simulate) { 
        let oldSnipe = (cfg.slip && cfg.slip.snipe) ? cfg.slip.snipe : { min: 15, max: 50, bump: 5 };
        let oldBuy = (cfg.slip && cfg.slip.buy) ? cfg.slip.buy : { min: 2, max: 20, bump: 2 };
        cfg.slip = { simulate: oldSnipe, buy: oldBuy, sell: oldBuy, multi_buy: { min: 5, max: 25, bump: 5 }, multi_sell: { min: 5, max: 25, bump: 5 } };
    }
    if (!cfg.slip.multi_buy) { cfg.slip.multi_buy = { min: 5, max: 25, bump: 5 }; cfg.slip.multi_sell = { min: 5, max: 25, bump: 5 }; }
    
    saveConfig(cfg); 
    return cfg;
}

function saveConfig(cfg) { fs.writeFile(CONFIG_FILE, JSON.stringify(cfg, null, 4), () => {}); } // 🔥 Anti-Freeze Write

// -------------------------
// PROVIDER SUPERVISOR (DUAL ENGINE WSS & HTTP)
// -------------------------
let provider = null; 
let RPC_CONNECTED = false;
let DISCONNECT_COUNT = 0;
let reconnectTimer = null;
let OVERRIDE_RPC_MODE = null; 

function connectRPC() {
    clearTimeout(reconnectTimer);
    const cfg = loadConfig();
    let currentMode = OVERRIDE_RPC_MODE || cfg.active_mode;
    
    let url = "";
    if (currentMode === "premium") {
        if (!cfg.premium_urls || cfg.premium_urls.length === 0) { RPC_CONNECTED = false; CONNECTED_RPC_URL = "Tidak ada URL Premium"; return; }
        url = cfg.premium_urls[cfg.active_premium_index % cfg.premium_urls.length];
    } else {
        if (!cfg.free_urls || cfg.free_urls.length === 0) { RPC_CONNECTED = false; CONNECTED_RPC_URL = "Tidak ada URL Gratis"; return; }
        url = cfg.free_urls[cfg.active_free_index % cfg.free_urls.length];
    }
    
    CONNECTED_RPC_URL = url;
    const chainIdNum = CHAINS[ACTIVE_CHAIN].id;
    
    try {
        if (provider) {
            if (provider.websocket) { provider.websocket.removeAllListeners(); provider.websocket.terminate(); } 
            else if (provider.destroy) { provider.destroy(); }
        }
        
        if (url.startsWith("wss://")) {
            provider = new ethers.WebSocketProvider(url, chainIdNum);
            if(provider.websocket) {
                provider.websocket.on('close', handleRpcDrop);
                provider.websocket.on('error', handleRpcDrop);
            }
        } else {
            provider = new ethers.JsonRpcProvider(url, chainIdNum, { staticNetwork: true });
        }

        provider.getBlockNumber().then(() => { RPC_CONNECTED = true; })
        .catch((err) => { handleRpcDrop(); });
    } catch (e) { handleRpcDrop(); }
}

const handleRpcDrop = () => {
    if (RPC_CONNECTED) { RPC_CONNECTED = false; DISCONNECT_COUNT++; }
    const cfg = loadConfig();
    let currentMode = OVERRIDE_RPC_MODE || cfg.active_mode;
    if (currentMode === "premium" && cfg.premium_urls.length > 1) {
        cfg.active_premium_index = (cfg.active_premium_index + 1) % cfg.premium_urls.length;
    } else if (currentMode === "gratis" && cfg.free_urls.length > 1) {
        cfg.active_free_index = (cfg.active_free_index + 1) % cfg.free_urls.length;
    }
    saveConfig(cfg);
    clearTimeout(reconnectTimer);
    reconnectTimer = setTimeout(connectRPC, 2000);
};

const originalConsoleLog = console.log;
console.log = function(...args) {
    if (typeof args[0] === 'string' && (args[0].includes('failed to detect network') || args[0].includes('JsonRpcProvider'))) return;
    originalConsoleLog.apply(console, args);
};

process.on('uncaughtException', (err) => {});

// =================
// LIVE PRICE FETCHER (Dinamis Omni-Chain)
// =================
const https = require('https');
let ethPriceUsd = 0;
let ethPriceIdr = 0;

function updateEthPrice() {
    const c = CHAINS[ACTIVE_CHAIN];
    const sym = c ? c.symbol.toUpperCase() : "ETH";
    
    // 🔥 SMART DICTIONARY: Cocokin simbol koin native ke ID CoinGecko secara otomatis
    const coinMap = {
        "ETH": "ethereum", "BNB": "binancecoin", "POL": "matic-network", 
        "MATIC": "matic-network", "CELO": "celo", "AVAX": "avalanche-2", 
        "FTM": "fantom", "CRO": "crypto-com-chain", "XDAI": "xdai", "CORE": "coredaoorg",
        "USDC": "usd-coin", "USDT": "tether" // 🔥 Fix harga untuk L2 berbasis Stablecoin
    };
    
    // Prioritaskan dictionary. Kalau L2 (Base, Arb, Morph) dia akan otomatis pakai "ethereum"
    let cgId = coinMap[sym] || (c && c.cgId !== "ethereum" ? c.cgId : "ethereum");

    const options = {
        hostname: 'coins.llama.fi', path: `/prices/current/coingecko:${cgId}`,
        method: 'GET', rejectUnauthorized: false, headers: { 'Accept': 'application/json' }
    };
    https.get(options, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
            try {
                const parsed = JSON.parse(data);
                if (parsed && parsed.coins && parsed.coins[`coingecko:${cgId}`]) {
                    ethPriceUsd = parseFloat(parsed.coins[`coingecko:${cgId}`].price);
                    // Update kurs dinamis (Rp 15.500)
                    ethPriceIdr = ethPriceUsd * 15500;
                }
            } catch(e) {}
        });
    }).on('error', () => {});
}
setInterval(updateEthPrice, 60000); 
updateEthPrice(); 

// Helper untuk menghitung estimasi biaya gas (Default 300,000 Gas Limit untuk Swap Dex)
function formatGasFiat(gasPriceWei, gasLimit = 300000n) {
    if (ethPriceUsd === 0) return "(⏳ Memuat harga...)"; 
    const costEth = parseFloat(ethers.formatEther(gasPriceWei * gasLimit));
    const costUsd = (costEth * ethPriceUsd).toFixed(2);
    const costIdr = Math.round(costEth * ethPriceIdr).toLocaleString('id-ID');
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";
    const displayEth = Number(costEth.toFixed(7));
    return `(~ ${displayEth} ${sym} | $${costUsd} | Rp ${costIdr})`;
}

// ABI Standard Multi-EVM
const ERC20_ABI = [
    "function name() view returns (string)",
    "function symbol() view returns (string)",
    "function decimals() view returns (uint8)",
    "function balanceOf(address) view returns (uint256)",
    "function approve(address, uint256) returns (bool)",
    "function allowance(address, address) view returns (uint256)",
    "function transfer(address to, uint256 amount) returns (bool)"
];
const FACTORY_V3_ABI = ["function getPool(address,address,uint24) view returns (address)"];
const FACTORY_V2_ABI = ["function getPair(address,address) view returns (address)"];
const FACTORY_AERO_ABI = ["function getPool(address,address,bool) view returns (address)"];

const V3_ABI = ["function exactInputSingle(tuple(address tokenIn, address tokenOut, uint24 fee, address recipient, uint256 amountIn, uint256 amountOutMinimum, uint160 sqrtPriceLimitX96) params) external payable returns (uint256 amountOut)"];
const V2_ABI = [
    "function swapExactTokensForETHSupportingFeeOnTransferTokens(uint amountIn, uint amountOutMin, address[] calldata path, address to, uint deadline) external", 
    "function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts)"
];
const AERO_ABI = [
    "function swapExactETHForTokens(uint amountOutMin, tuple(address from, address to, bool stable, address factory)[] calldata routes, address to, uint deadline) external payable returns (uint[] memory amounts)",
    "function swapExactTokensForETHSupportingFeeOnTransferTokens(uint amountIn, uint amountOutMin, tuple(address from, address to, bool stable, address factory)[] calldata routes, address to, uint deadline) external"
];
const V4_ABI = ["function execute(bytes calldata commands, bytes[] calldata inputs, uint256 deadline) external payable"];

const v3Interface = new ethers.Interface(V3_ABI);
const v2Interface = new ethers.Interface(V2_ABI);
const aeroInterface = new ethers.Interface(AERO_ABI);
const v4Interface = new ethers.Interface(V4_ABI);

// =================
// 3. DATABASE WALLET MANAGER
// =================
const DB_FILE = 'wallets_db.json';

let memCacheDB = null;
function loadDB() {
    if (memCacheDB) return memCacheDB; // 🔥 Baca langsung dari RAM
    if (!fs.existsSync(DB_FILE)) {
        let initialWallets = [];
        let masterIdx = null;
        if (process.env.PRIVATE_KEY) {
            try {
                const w = new ethers.Wallet(process.env.PRIVATE_KEY);
                initialWallets.push({ name: "Master .env", address: w.address, pk: w.privateKey, seed: "Imported from .env" });
                masterIdx = 0;
            } catch(e) {}
        }
        const initialDB = { master_index: masterIdx, wallets: initialWallets };
        fs.writeFile(DB_FILE, JSON.stringify(initialDB, null, 4), () => {});
        memCacheDB = initialDB;
        return initialDB;
    }
    memCacheDB = JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
    return memCacheDB;
}

function saveDB(data) {
    memCacheDB = data;
    fs.writeFile(DB_FILE, JSON.stringify(data, null, 4), () => {}); // 🔥 Fire & Forget Async Write
}

function getMasterWalletObj() {
    const db = loadDB();
    if (db.wallets.length === 0) return null;
    if (OVERRIDE_WALLET_NAME) {
        const w = db.wallets.find(x => x.name.toLowerCase() === OVERRIDE_WALLET_NAME.toLowerCase());
        if (w) return w;
        console.log(`\n ${RED}❌ Wallet dengan nama "${OVERRIDE_WALLET_NAME}" tidak ditemukan di database!${RESET}`);
        process.exit(1);
    }
    if (db.master_index === null) return null;
    return db.wallets[db.master_index];
}

// =================
// 4. CORE SNIPER LOGIC
// =================
async function autoDetectLP(targetToken, silent = false) {
    let spinInt;
    if (!silent) {
        let frames = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'];
        let i = 0;
        // Animasi Muter saat scanning LP
        spinInt = setInterval(() => process.stdout.write(`\r ${CYAN}${frames[i++ % frames.length]}${RESET} ${GRAY}Auto-Detect: Memindai LP di ${CHAINS[ACTIVE_CHAIN].name}...${RESET}`), 100);
    }
    let detectedPools = [];
    let promises = [];
    
    if (V2_FACTORY !== ethers.ZeroAddress) {
        const v2Fac = new ethers.Contract(V2_FACTORY, FACTORY_V2_ABI, provider);
        promises.push(v2Fac.getPair(WETH_ADDRESS, targetToken).then(res => {
            if (res !== ethers.ZeroAddress) detectedPools.push({ type: "V2", id: "V2 (Native Pair)", to: V2_ROUTER, path: [WETH_ADDRESS, targetToken] });
        }).catch(()=>{}));
        promises.push(v2Fac.getPair(USDC_ADDRESS, targetToken).then(res => {
            if (res !== ethers.ZeroAddress) detectedPools.push({ type: "V2", id: "V2 (Stable Pair)", to: V2_ROUTER, path: [WETH_ADDRESS, USDC_ADDRESS, targetToken] });
        }).catch(()=>{}));
    }

    if (V3_FACTORY !== ethers.ZeroAddress) {
        const v3Fac = new ethers.Contract(V3_FACTORY, FACTORY_V3_ABI, provider);
        const fees = [10000, 3000, 500, 100]; 
        for (let f of fees) {
            promises.push(v3Fac.getPool(WETH_ADDRESS, targetToken, f).then(res => {
                if (res !== ethers.ZeroAddress) detectedPools.push({ type: "V3", fee: f, id: `V3 Native (${f/10000}%)`, to: V3_ROUTER });
            }).catch(()=>{}));
        }
    }
    
    if (HAS_AERO && AERO_FACTORY !== ethers.ZeroAddress) {
        const aeroFac = new ethers.Contract(AERO_FACTORY, FACTORY_AERO_ABI, provider);
        promises.push(aeroFac.getPool(WETH_ADDRESS, targetToken, false).then(res => {
            if (res !== ethers.ZeroAddress) detectedPools.push({ type: "AERO", stable: false, id: "Aerodrome (Native Vol)", to: AERO_ROUTER });
        }).catch(()=>{}));
        promises.push(aeroFac.getPool(USDC_ADDRESS, targetToken, false).then(res => {
            if (res !== ethers.ZeroAddress) detectedPools.push({ type: "AERO_USDC", stable: false, id: "Aerodrome (Stable Vol)", to: AERO_ROUTER });
        }).catch(()=>{}));
    }

    await Promise.all(promises);

    if (V4_ROUTER !== ethers.ZeroAddress) {
        detectedPools.push({ type: "V4", id: "Uniswap V4 (Universal)", to: V4_ROUTER });
    }

    if (!silent) {
        clearInterval(spinInt); // Matikan putaran spinner
        if (detectedPools.length > 0) {
            // Block Badge Hijau
            process.stdout.write(`\r\x1b[2K ${GREEN}✅ ${BG_GREEN} LP DETECTED ${RESET} ${GREEN}${detectedPools.length} Rute Aktif di ${CHAINS[ACTIVE_CHAIN].name}!${RESET}\n`);
        } else {
            // Block Badge Merah
            process.stdout.write(`\r\x1b[2K ${RED}❌ ${BG_RED} NO LP ${RESET} ${RED}LP Belum Dibuat / Token Kosong!${RESET}\n`);
        }
    }
    return detectedPools;
}

function buildPayloads(pools, targetToken, amount, isBuy, walletAddress, slippage = 20) {
    let payloads = [];
    const amountOutMin = slippage >= 50 ? 0n : 1n; 
    const deadline = Math.floor(Date.now() / 1000) + 60 * 10;
    if (pools.length === 0) return payloads;

    for (let p of pools) {
        if (isBuy) {
            if (p.type === "V2") {
                payloads.push({ id: p.id, to: p.to, data: v2Interface.encodeFunctionData("swapExactETHForTokens", [amountOutMin, p.path, walletAddress, deadline]) });
            } else if (p.type === "V3") {
                payloads.push({ id: p.id, to: p.to, data: v3Interface.encodeFunctionData("exactInputSingle", [{ tokenIn: WETH_ADDRESS, tokenOut: targetToken, fee: p.fee, recipient: walletAddress, amountIn: amount, amountOutMinimum: amountOutMin, sqrtPriceLimitX96: 0n }]) });
            } else if (p.type === "AERO") {
                const routes = [{ from: WETH_ADDRESS, to: targetToken, stable: p.stable, factory: AERO_FACTORY }];
                payloads.push({ id: p.id, to: p.to, data: aeroInterface.encodeFunctionData("swapExactETHForTokens", [amountOutMin, routes, walletAddress, deadline]) });
            } else if (p.type === "AERO_USDC") {
                const routes = [{ from: WETH_ADDRESS, to: USDC_ADDRESS, stable: false, factory: AERO_FACTORY }, { from: USDC_ADDRESS, to: targetToken, stable: p.stable, factory: AERO_FACTORY }];
                payloads.push({ id: p.id, to: p.to, data: aeroInterface.encodeFunctionData("swapExactETHForTokens", [amountOutMin, routes, walletAddress, deadline]) });
            } else if (p.type === "V4") {
                const commands = "0x0b00"; 
                const inputs = [ethers.AbiCoder.defaultAbiCoder().encode(["address","uint256","uint256","bytes","bool"], [V4_ROUTER, amount, amountOutMin, "0x", false])];
                payloads.push({ id: p.id, to: p.to, data: v4Interface.encodeFunctionData("execute", [commands, inputs, deadline]) });
            }
        } else {
            if (p.type === "V2") {
                const sellPath = [...p.path].reverse(); 
                payloads.push({ id: p.id, to: p.to, data: v2Interface.encodeFunctionData("swapExactTokensForETHSupportingFeeOnTransferTokens", [amount, 0n, sellPath, walletAddress, deadline]) });
            } else if (p.type === "V3") {
                payloads.push({ id: p.id, to: p.to, data: v3Interface.encodeFunctionData("exactInputSingle", [{ tokenIn: targetToken, tokenOut: WETH_ADDRESS, fee: p.fee, recipient: walletAddress, amountIn: amount, amountOutMinimum: 0n, sqrtPriceLimitX96: 0n }]) });
            } else if (p.type === "AERO") {
                const routes = [{ from: targetToken, to: WETH_ADDRESS, stable: p.stable, factory: AERO_FACTORY }];
                payloads.push({ id: p.id, to: p.to, data: aeroInterface.encodeFunctionData("swapExactTokensForETHSupportingFeeOnTransferTokens", [amount, 0n, routes, walletAddress, deadline]) });
            } else if (p.type === "AERO_USDC") {
                const routes = [{ from: targetToken, to: USDC_ADDRESS, stable: p.stable, factory: AERO_FACTORY }, { from: USDC_ADDRESS, to: WETH_ADDRESS, stable: false, factory: AERO_FACTORY }];
                payloads.push({ id: p.id, to: p.to, data: aeroInterface.encodeFunctionData("swapExactTokensForETHSupportingFeeOnTransferTokens", [amount, 0n, routes, walletAddress, deadline]) });
            } else if (p.type === "V4") {
                const commands = "0x00"; 
                const inputs = [ethers.AbiCoder.defaultAbiCoder().encode(["address","uint256","uint256","bytes","bool"], [V4_ROUTER, amount, 0n, "0x", false])];
                payloads.push({ id: p.id, to: p.to, data: v4Interface.encodeFunctionData("execute", [commands, inputs, deadline]) });
            }
        }
    }
    return payloads;
}

async function getGasConfig(multiplier) {
    let baseGasPriceWei = ethers.parseUnits("0.005", "gwei"); // Fallback super murah
    let priorityFeeWei = ethers.parseUnits("0.0001", "gwei");
    
    try {
        const rawGasPrice = await provider.send("eth_gasPrice", []);
        baseGasPriceWei = BigInt(rawGasPrice);
        
        const feeData = await provider.getFeeData();
        if (feeData.maxPriorityFeePerGas) {
            priorityFeeWei = feeData.maxPriorityFeePerGas;
        }
    } catch(e) {}

    const maxFeePerGasConfig = (baseGasPriceWei * multiplier) / 100n; 
    let maxPriorityFeePerGasConfig = (priorityFeeWei * multiplier) / 100n;
    
    if (maxPriorityFeePerGasConfig > maxFeePerGasConfig) maxPriorityFeePerGasConfig = maxFeePerGasConfig;
    
    return { maxFeePerGasConfig, maxPriorityFeePerGasConfig, baseGasPriceWei };
}

async function autoApprove(targetToken, routerAddress, wallet, explicitNonce = null) {
    if (routerAddress === ethers.ZeroAddress) return false;
    try {
        const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, wallet);
        const allowance = await tokenContract.allowance(wallet.address, routerAddress);
        if (allowance < ethers.MaxUint256 / 2n) {
            console.log(` ${GRAY}⏳ Mengirim transaksi Auto-Approve ke jaringan...${RESET}`);
            let txSuccessFlag = false;
            while (!txSuccessFlag) {
                try {
                    const txParams = explicitNonce !== null ? { nonce: explicitNonce } : {};
                    const tx = await tokenContract.approve(routerAddress, ethers.MaxUint256, txParams);
                    // Block Badge Hijau
                    console.log(` ${GREEN}✅ ${BG_GREEN} APPROVE SUKSES ${RESET} ${GREEN}Hash: ${tx.hash}${RESET}`);
                    await tx.wait(); 
                    txSuccessFlag = true;
                    return true;
                } catch (e) {
                    const errMsg = (e.message || "").toLowerCase();
                    if (errMsg.includes("insufficient funds")) {
                        console.log(`\n ${RED}❌ Gagal Auto-Approve: ${e.shortMessage || errMsg}${RESET}`);
                        const activeCfg = loadConfig();
                        const gasConfig = await getGasConfig(BigInt(activeCfg.gwei.sell || 100));
                        const requiredGasWei = 65000n * gasConfig.maxFeePerGasConfig;
                        const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                        if (!isFunded) return false;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            console.log(` ${GREEN}✅ Token sudah di-Approve sebelumnya.${RESET}`);
            return false;
        }
    } catch (e) {
        return false;
    }
}

async function getEstimatedTokens(pool, amountIn, isBuy, targetToken) {
    try {
        if (pool.type === "V2") {
            const r = new ethers.Contract(V2_ROUTER, ["function getAmountsOut(uint amountIn, address[] memory path) view returns (uint[] memory amounts)"], provider);
            const path = isBuy ? pool.path : [...pool.path].reverse();
            const amounts = await r.getAmountsOut(amountIn, path);
            return amounts[amounts.length - 1];
        } else if (pool.type === "V3" && V3_QUOTER !== ethers.ZeroAddress) {
            const q = new ethers.Contract(V3_QUOTER, ["function quoteExactInputSingle(address tokenIn, address tokenOut, uint24 fee, uint256 amountIn, uint160 sqrtPriceLimitX96) external returns (uint256 amountOut)"], provider);
            const tokenIn = isBuy ? WETH_ADDRESS : targetToken;
            const tokenOut = isBuy ? targetToken : WETH_ADDRESS;
            const amtOut = await q.quoteExactInputSingle.staticCall(tokenIn, tokenOut, pool.fee, amountIn, 0n);
            return amtOut;
        } else if ((pool.type === "AERO" || pool.type === "AERO_USDC") && HAS_AERO) {
            const r = new ethers.Contract(AERO_ROUTER, ["function getAmountsOut(uint amountIn, tuple(address from, address to, bool stable, address factory)[] memory routes) view returns (uint[] memory amounts)"], provider);
            let routes = [];
            if (pool.type === "AERO") {
                routes = [{ from: isBuy ? WETH_ADDRESS : targetToken, to: isBuy ? targetToken : WETH_ADDRESS, stable: pool.stable, factory: AERO_FACTORY }];
            } else {
                if (isBuy) { routes = [{ from: WETH_ADDRESS, to: USDC_ADDRESS, stable: false, factory: AERO_FACTORY }, { from: USDC_ADDRESS, to: targetToken, stable: pool.stable, factory: AERO_FACTORY }]; } 
                else { routes = [{ from: targetToken, to: USDC_ADDRESS, stable: pool.stable, factory: AERO_FACTORY }, { from: USDC_ADDRESS, to: WETH_ADDRESS, stable: false, factory: AERO_FACTORY }]; }
            }
            const amounts = await r.getAmountsOut(amountIn, routes);
            return amounts[amounts.length - 1];
        }
    } catch(e) {}
    return 0n; 
}

// =================
// 4.5 EMERGENCY GAS STATION (MUTEX LOCK)
// =================
let emergencyGasMutex = false;
async function handleEmergencyGas(failedWalletAddress, requiredGasWei) {
    while (emergencyGasMutex) await new Promise(r => setTimeout(r, 500)); // Antre rapi!
    emergencyGasMutex = true;
    try {
        const db = loadDB();
        if (db.master_index === null || db.wallets.length === 0) {
            console.log(`\n ${RED}❌ Gagal Emergency Gas: Master Wallet belum diset!${RESET}`);
            return false;
        }
        const masterData = db.wallets[db.master_index];
        if (masterData.address.toLowerCase() === failedWalletAddress.toLowerCase()) return false;

        const tuyulData = db.wallets.find(w => w.address.toLowerCase() === failedWalletAddress.toLowerCase());
        const tuyulName = tuyulData ? tuyulData.name : "Wallet";
        const masterWallet = new ethers.Wallet(masterData.pk, provider);
        let fundWei = 0n;

        if (AUTO_MODE) {
            fundWei = (requiredGasWei * 150n) / 100n;
            const autoFundUsd = parseFloat(ethers.formatEther(fundWei)) * ethPriceUsd;
            if (autoFundUsd > 1) { fundWei = 0n; } 
            else { console.log(`\n ${YELLOW}⛽ [ AUTO-EMERGENCY GAS ] Mengirim bensin ke ${tuyulName}...${RESET}`); }
        }

        if (fundWei === 0n) {
            let mBal = 0n;
            try { mBal = await provider.getBalance(masterWallet.address); } catch(e) {}
            console.log(`\n ${YELLOW}⛽ [ EMERGENCY GAS STATION ] Saldo ${tuyulName} tidak kuat bayar gas!${RESET}`);
            const inputTx = await askQuestion(` ${CYAN}╰─ Butuh suntikan untuk berapa TX? (Contoh: 1, 2, 5): ${RESET}`);
            let parsedTx = parseInt(inputTx.trim());
            if (isNaN(parsedTx) || parsedTx < 1) return false;
            fundWei = requiredGasWei * BigInt(parsedTx);
            
            const conf = await askQuestion(` ${CYAN}╰─ Tarik ${parseFloat(ethers.formatEther(fundWei)).toFixed(6)} ETH dari Master ke ${tuyulName}? [ Enter = GAS / 'n' = Batal ] : ${RESET}`);
            if (conf.trim().toLowerCase() === 'n') return false;
        }

        let isMasterFunded = false;
        while (!isMasterFunded) {
            let currentMasterBal = 0n;
            try { currentMasterBal = await provider.getBalance(masterWallet.address); } catch(e) {}
            if (currentMasterBal < fundWei) {
                console.log(`    ${RED}❌ SALDO MASTER KOSONG! Deposit ke (${masterData.address})${RESET}`);
                const retryConf = await askQuestion(`    ${CYAN}╰─ Sudah deposit? [ Enter = Lanjut / 'n' = Batal ] : ${RESET}`);
                if (retryConf.trim().toLowerCase() === 'n') return false;
            } else { isMasterFunded = true; }
        }

        console.log(`    ${GREEN}🚀 Mengirim bensin dari Master...${RESET}`);
        let tuyulBalBefore = 0n;
        try { tuyulBalBefore = await provider.getBalance(failedWalletAddress); } catch(e) {}
        const tx = await masterWallet.sendTransaction({ to: failedWalletAddress, value: fundWei });
        console.log(`    ${GREEN}✅ Bensin masuk! Hash: ${tx.hash}${RESET}`);
        
        await tx.wait(1);
        for(let k = 0; k < 10; k++) {
            await new Promise(r => setTimeout(r, 1000));
            let newBal = await provider.getBalance(failedWalletAddress);
            if (newBal > tuyulBalBefore) break; 
        }
        console.log(`\r    ${CYAN}🔄 MENGULANG TRANSAKSI SEBELUMNYA...                 ${RESET}`);
        return true;
    } catch (e) {
        console.log(`\n    ${RED}❌ Gagal mengirim bensin: ${e.shortMessage || e.message}${RESET}`);
        return false;
    } finally {
        emergencyGasMutex = false; // Buka gembok antrean!
    }
}

// =================
// 5. SNIPER MENU & EXECUTION
// =================

const HISTORY_FILE = 'history.json';
function loadHistory() {
    if (!fs.existsSync(HISTORY_FILE)) return [];
    return JSON.parse(fs.readFileSync(HISTORY_FILE, 'utf8'));
}
function addHistory(address, name, symbol) {
    if (!name || name === "Unknown") return; 
    let h = loadHistory();
    h = h.filter(x => x.address.toLowerCase() !== address.toLowerCase()); 
    h.unshift({ address, name, symbol });
    if (h.length > 5) h.pop(); 
    fs.writeFileSync(HISTORY_FILE, JSON.stringify(h, null, 4));
}

async function executeSniperMenu() {
    if (!RPC_CONNECTED || !provider) {
        console.log(`\n ${RED}❌ Koneksi RPC sedang OFFLINE. Tunggu terhubung kembali!${RESET}`);
        await pause(); return;
    }

    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj) {
        console.log(`\n ${RED}❌ Master Wallet belum diset! Silakan ke menu Kelola Dompet [6].${RESET}`);
        await pause(); return;
    }
    
    let wallet = new ethers.Wallet(mWalletObj.pk, provider);

    while(true) {
        clear(); title();
        
        wallet = new ethers.Wallet(mWalletObj.pk, provider);
        
        let balanceWei = 0n;
        let balanceEth = "0.0";
        let liveGweiDisplay = "Memuat...";
        try {
            balanceWei = await provider.getBalance(wallet.address);
            balanceEth = ethers.formatEther(balanceWei);
            
            // 🔥 BYPASS ETHERS.JS: Tanya langsung harga bensin mentah ke Node tanpa markup!
            const rawGasPrice = await provider.send("eth_gasPrice", []);
            const currentGweiWei = BigInt(rawGasPrice);
            
            const gweiNum = parseFloat(ethers.formatUnits(currentGweiWei, "gwei")).toFixed(5);
            liveGweiDisplay = `${gweiNum} Gwei ${GRAY}${formatGasFiat(currentGweiWei)}${RESET}`;
        } catch(e) {}

        let cfg = loadConfig();
        let currentEngine = OVERRIDE_RPC_MODE || cfg.active_mode;
        
        let rpcModeDisplay = currentEngine === 'premium' ? CYAN + `✅ PREMIUM (WSS)` : GREEN + `✅ GRATISAN (HTTP)`;

        // 🔥 IDE VIP: Tampilan Cyber Trade Panel yang dipisah antara Solo & Multi-Wallet
        console.log(` ${GREEN}[ STATUS CONFIGURATION ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ Mode RPC      : ${rpcModeDisplay}${RESET}`);
        console.log(` ├─ Status        : ${RPC_CONNECTED ? GREEN + "✅ ONLINE" : RED + "❌ OFFLINE"}${RESET}`);
        console.log(` ├─ RPC Dipakai   : ${GREEN}${CONNECTED_RPC_URL}${RESET}`);
        console.log(` ├─ Putus Koneksi : ${RED}${DISCONNECT_COUNT}x${RESET}`);
        console.log(` ├─ Master Wallet : ${mWalletObj.address}`);
        console.log(` ├─ Saldo ETH     : ${GREEN}${balanceEth} ETH${RESET}`);
        console.log(` ├─ Live Gwei     : ${CYAN}${liveGweiDisplay}`);
        console.log(` ├─ Slip Snipe    : ${CYAN}Min ${cfg.slip.simulate.min}% | Max ${cfg.slip.simulate.max}% | Bump +${cfg.slip.simulate.bump}%${RESET}`);
        console.log(` └─ Slip Buy/Sell : ${CYAN}Min ${cfg.slip.buy.min}% | Max ${cfg.slip.buy.max}% | Bump +${cfg.slip.buy.bump}%${RESET}\n`);

        let simColor = cfg.mode_simulate === 'premium' ? CYAN : GREEN;
        let buyColor = cfg.mode_buy === 'premium' ? CYAN : GREEN;
        let sellColor = cfg.mode_sell === 'premium' ? CYAN : GREEN;
        let multiBuyColor = cfg.mode_multi_buy === 'premium' ? CYAN : GREEN;
        let multiSellColor = cfg.mode_multi_sell === 'premium' ? CYAN : GREEN;

        console.log(` ${CYAN}[ CYBER TRADE PANEL ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ [1] 🎯 Sniper Mode`);
        console.log(` │     ├─ Ketik '1a' ganti RPC  : ${simColor}${cfg.mode_simulate.toUpperCase()}${RESET}`);
        console.log(` │     ├─ Ketik '1b' ganti Gwei : ${cfg.gwei.simulate}%`);
        console.log(` │     └─ Ketik '1c' ganti Slip : ${cfg.slip.simulate.min}% - ${cfg.slip.simulate.max}%`);
        console.log(` │`);
        console.log(` ├─ [2] ⚡ Buy`);
        console.log(` │     ├─ Ketik '2a' ganti RPC  : ${buyColor}${cfg.mode_buy.toUpperCase()}${RESET}`);
        console.log(` │     ├─ Ketik '2b' ganti Gwei : ${cfg.gwei.buy}%`);
        console.log(` │     └─ Ketik '2c' ganti Slip : ${cfg.slip.buy.min}% - ${cfg.slip.buy.max}%`);
        console.log(` │`);
        console.log(` ├─ [3] 💰 Sell`);
        console.log(` │     ├─ Ketik '3a' ganti RPC  : ${sellColor}${cfg.mode_sell.toUpperCase()}${RESET}`);
        console.log(` │     ├─ Ketik '3b' ganti Gwei : ${cfg.gwei.sell}%`);
        console.log(` │     └─ Ketik '3c' ganti Slip : ${cfg.slip.sell.min}% - ${cfg.slip.sell.max}%`);
        console.log(` │`);
        console.log(` ├─ [4] 🚀 Multi-Wallet Buy`);
        console.log(` │     ├─ Ketik '4a' ganti RPC  : ${multiBuyColor}${cfg.mode_multi_buy.toUpperCase()}${RESET}`);
        console.log(` │     ├─ Ketik '4b' ganti Gwei : ${cfg.gwei.multi_buy}%`);
        console.log(` │     └─ Ketik '4c' ganti Slip : ${cfg.slip.multi_buy.min}% - ${cfg.slip.multi_buy.max}%`);
        console.log(` │`);
        console.log(` ├─ [5] 💥 Multi-Wallet Sell`);
        console.log(` │     ├─ Ketik '5a' ganti RPC  : ${multiSellColor}${cfg.mode_multi_sell.toUpperCase()}${RESET}`);
        console.log(` │     ├─ Ketik '5b' ganti Gwei : ${cfg.gwei.multi_sell}%`);
        console.log(` │     └─ Ketik '5c' ganti Slip : ${cfg.slip.multi_sell.min}% - ${cfg.slip.multi_sell.max}%`);
        console.log(` │`);
        console.log(` ├─ [6] 🔓 Pre-Approve Token`);
        console.log(` └─ [0] Kembali\n`);

        const action = await askQuestion(` ╰─ Pilih aksi: `);
        
        if (action === '0') {
            if (OVERRIDE_RPC_MODE) { OVERRIDE_RPC_MODE = null; connectRPC(); }
            return;
        }

        // Toggle Saklar Masing-Masing
        if (action === '1a') { cfg.mode_simulate = cfg.mode_simulate === 'gratis' ? 'premium' : 'gratis'; saveConfig(cfg); continue; }
        if (action === '2a') { cfg.mode_buy = cfg.mode_buy === 'gratis' ? 'premium' : 'gratis'; saveConfig(cfg); continue; }
        if (action === '3a') { cfg.mode_sell = cfg.mode_sell === 'gratis' ? 'premium' : 'gratis'; saveConfig(cfg); continue; }
        if (action === '4a') { cfg.mode_multi_buy = cfg.mode_multi_buy === 'gratis' ? 'premium' : 'gratis'; saveConfig(cfg); continue; }
        if (action === '5a') { cfg.mode_multi_sell = cfg.mode_multi_sell === 'gratis' ? 'premium' : 'gratis'; saveConfig(cfg); continue; }

        // Ganti Gwei Masing-Masing
        if (['1b', '2b', '3b', '4b', '5b'].includes(action)) {
            let mode = action === '1b' ? 'simulate' : (action === '2b' ? 'buy' : (action === '3b' ? 'sell' : (action === '4b' ? 'multi_buy' : 'multi_sell')));
            console.log(`\n ${CYAN}[ SETTING GWEI MULTIPLIER (${mode.toUpperCase()}) ]${RESET}`);
            console.log(` ${GRAY}Panduan: 25, 50, 100 (Default), 150, 200 s/d 100000${RESET}`);
            let newGwei = await askQuestion(` ╰─ Masukkan Gwei Multiplier (%): `);
            let parsedGwei = parseInt(newGwei);
            if (!isNaN(parsedGwei) && parsedGwei > 0 && parsedGwei <= 100000) {
                cfg.gwei[mode] = parsedGwei;
                saveConfig(cfg);
                console.log(` ${GREEN}✅ Gwei ${mode.toUpperCase()} tersimpan: ${cfg.gwei[mode]}%${RESET}`);
            } else {
                console.log(` ${RED}❌ Input tidak valid! Harus angka 1 s/d 100000.${RESET}`);
            }
            await pause(); continue;
        }

        // Ganti Slippage Masing-Masing
        if (['1c', '2c', '3c', '4c', '5c'].includes(action)) {
            let mode = action === '1c' ? 'simulate' : (action === '2c' ? 'buy' : (action === '3c' ? 'sell' : (action === '4c' ? 'multi_buy' : 'multi_sell')));
            console.log(`\n ${CYAN}[ SETTING SLIPPAGE (${mode.toUpperCase()}) ]${RESET}`);
            console.log(` ${GRAY}*Tekan Enter saja jika tidak mengubah angka${RESET}\n`);
            const pMin = await askQuestion(` ├─ Min Slippage (${cfg.slip[mode].min}%): `);
            if(pMin) cfg.slip[mode].min = parseInt(pMin);
            const pMax = await askQuestion(` ├─ Max Slippage (${cfg.slip[mode].max}%): `);
            if(pMax) cfg.slip[mode].max = parseInt(pMax);
            const pBmp = await askQuestion(` └─ Bump / Kenaikan (${cfg.slip[mode].bump}%): `);
            if(pBmp) cfg.slip[mode].bump = parseInt(pBmp);
            saveConfig(cfg);
            console.log(` ${GREEN}✅ Slippage ${mode.toUpperCase()} tersimpan!${RESET}`);
            await pause(); continue;
        }

        if (!['1','2','3','4','5','6'].includes(action)) continue;

        if (balanceWei === 0n && action !== '6') {
            console.log(`\n ${RED}❌ Saldo ETH kosong.${RESET}`);
            await pause(); continue;
        }

        // 🔥 LOGIKA AUTO-SWITCH RPC SEBELUM PERANG
        let targetMode = null;
        if (action === '1') targetMode = cfg.mode_simulate;
        if (action === '2') targetMode = cfg.mode_buy;
        if (action === '3') targetMode = cfg.mode_sell;
        if (action === '4') targetMode = cfg.mode_multi_buy;
        if (action === '5') targetMode = cfg.mode_multi_sell;
        
        let engineNow = OVERRIDE_RPC_MODE || cfg.active_mode;
        if (targetMode && engineNow !== targetMode) {
            OVERRIDE_RPC_MODE = targetMode;
            process.stdout.write(`\n ${GRAY}⏳ Mengoper Gigi Mesin RPC ke ${targetMode.toUpperCase()}...${RESET}`);
            connectRPC();
            let waitCheck = 0;
            while(!RPC_CONNECTED && waitCheck < 15) { await new Promise(r => setTimeout(r, 200)); waitCheck++; }
            console.log(`\r ${GREEN}✅ Engine RPC siap! (${targetMode.toUpperCase()})                           ${RESET}`);
            wallet = new ethers.Wallet(mWalletObj.pk, provider);
        }

        // 🔥 TAMPILKAN PERINGATAN RPC TEPAT SETELAH ENGINE SIAP!
        let currentEngineStr = OVERRIDE_RPC_MODE || cfg.active_mode;
        console.log(""); 
        if (currentEngineStr === 'gratis') {
            console.log(` ${GRAY}Menggunakan RPC GRATIS (${CONNECTED_RPC_URL})${RESET}`);
            console.log(` ${GRAY}Mungkin mengalami delay... ${RESET}\n`);
        } else {
            console.log(` ${CYAN}Menggunakan RPC PREMIUM (${CONNECTED_RPC_URL})${RESET}`);
            console.log(` ${CYAN}   Kecepatan maksimal aktif!${RESET}\n`);
        }

        const history = loadHistory();
        if (history.length > 0) {
            console.log(` ${CYAN}[ RIWAYAT KONTRAK TERAKHIR ]${RESET}`);
            history.forEach((h, i) => {
                console.log(` ${GRAY}[${i + 1}]${RESET} ${h.name} (${h.symbol}) - ${h.address}`);
            });
            console.log(` ${GRAY}*Ketik angka (1-${history.length}) atau Paste kontrak baru.${RESET}`);
        }

        const inputTokenRaw = await askQuestion(`\n ${CYAN}╰─ Masukkan Kontrak Token Target: ${RESET}`);
        let targetToken = inputTokenRaw.trim();
        
        const historyIdx = parseInt(targetToken) - 1;
        if (!isNaN(historyIdx) && historyIdx >= 0 && historyIdx < history.length) {
            targetToken = history[historyIdx].address;
        }

        if(targetToken.length !== 42) {
            console.log(` ${RED}❌ Address tidak valid!${RESET}`);
            await pause(); continue;
        }

        // 🚀 MULTI-WALLET OPS (SERANGAN FAJAR & DUMP MASSAL)
        if (action === '4' || action === '5') {
            const db = loadDB(); // 🔥 FIX: Panggil database dulu sebelum ngegambar radar!
            const isBuy = action === '4';
            const modeName = isBuy ? "🚀 MULTI-WALLET BUY (Serangan Fajar)" : "💥 MULTI-WALLET SELL (Dump Massal)";
            
            // 1. Radar Dompet Vertikal Dinamis
            console.log(`\n ${CYAN}[ 📓 PILIH DOMPET TUYUL ]${RESET}`);
            let cols = 3;
            let rows = Math.ceil(db.wallets.length / cols);
            for (let r = 0; r < rows; r++) {
                let rowStr = "";
                for (let c = 0; c < cols; c++) {
                    let idx = (c * rows) + r;
                    if (idx < db.wallets.length) {
                        let w = db.wallets[idx];
                        let shortAddr = w.address.substring(w.address.length - 4);
                        let idxStr = idx.toString().padStart(2, ' ');
                        let maxName = 9;
                        let entryName = w.name.length > maxName ? w.name.substring(0, 7) + ".." : w.name;
                        let paddedName = entryName.padEnd(maxName, ' ');
                        let plainEntry = `  [${idxStr}] ${paddedName} (..${shortAddr})`;
                        if (idx === db.master_index) plainEntry = `👑[${idxStr}] ${paddedName} (..${shortAddr})`;
                        rowStr += plainEntry.padEnd(28, ' ');
                    }
                }
                if (rowStr.trim().length > 0) console.log(rowStr);
            }

            const idxInput = await askQuestion(`\n ${CYAN}╰─ Pilih Pasukan Tuyul (Contoh: 1-5, 8, 10-12 atau ketik 'all'): ${RESET}`);
            let selectedIndexes = new Set();
            if (idxInput.trim().toLowerCase() === 'all') {
                for (let i = 0; i < db.wallets.length; i++) {
                    if (i !== db.master_index) selectedIndexes.add(i); // Master gak ikut nimbrung
                }
            } else {
                const parts = idxInput.split(',');
                for (let p of parts) {
                    p = p.trim();
                    if (p.includes('-')) {
                        const [start, end] = p.split('-').map(Number);
                        if (!isNaN(start) && !isNaN(end)) {
                            for (let i = Math.min(start, end); i <= Math.max(start, end); i++) {
                                if (i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
                            }
                        }
                    } else {
                        const i = parseInt(p);
                        if (!isNaN(i) && i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
                    }
                }
            }

            const targetWalletsData = Array.from(selectedIndexes).map(i => db.wallets[i]);
            if (targetWalletsData.length === 0) {
                console.log(`\n ${RED}❌ Tidak ada dompet valid yang dipilih!${RESET}`);
                await pause(); continue;
            }

            const detectedPools = await autoDetectLP(targetToken);
            if (detectedPools.length === 0) {
                console.log(`\n ${RED}❌ GAGAL: LP tidak terdeteksi.${RESET}`); await pause(); continue;
            }

            let activeCfg = loadConfig();
            let targetModeName = isBuy ? 'multi_buy' : 'multi_sell';
            let currentSlippage = activeCfg.slip[targetModeName].min;
            let currentGasMultiplier = BigInt(activeCfg.gwei[targetModeName] || 150);
            let gasConfig = await getGasConfig(currentGasMultiplier);

            // ================= MULTI BUY =================
            if (isBuy) {
                const inputAmount = await askQuestion(` ${CYAN}╰─ Berapa ETH yang ingin dibeli PER TUYUL? (Contoh: 0.01): ${RESET}`);
                let amountToBuyWei = 0n;
                try { amountToBuyWei = ethers.parseEther(inputAmount.trim() || "0"); } catch(e) {}
                if (amountToBuyWei <= 0n) { console.log(` ${RED}❌ Nominal tidak valid!${RESET}`); await pause(); continue; }

                console.log(`\n ${GRAY}⏳ Pre-Flight: Mengecek logistik bensin pasukan...${RESET}`);
                let readyWallets = [];
                for (let wData of targetWalletsData) {
                    const w = new ethers.Wallet(wData.pk, provider);
                    const bal = await provider.getBalance(w.address);
                    // Estimasi barbar batas aman gas
                    const estGasCost = 650000n * gasConfig.maxFeePerGasConfig;
                    const totalCostSafe = amountToBuyWei + estGasCost;
                    
                    if (bal < totalCostSafe) {
                        const isFunded = await handleEmergencyGas(w.address, totalCostSafe);
                        if (isFunded) readyWallets.push({ wallet: w, name: wData.name });
                        else console.log(` ${GRAY}⏩ Lewati ${wData.name} (Batal diselamatkan)${RESET}`);
                    } else {
                        readyWallets.push({ wallet: w, name: wData.name });
                    }
                }

                if (readyWallets.length === 0) {
                    console.log(`\n ${RED}❌ Semua pasukan batal maju (Tidak cukup modal/gas)!${RESET}`);
                    await pause(); continue;
                }

                const conf = await askQuestion(`\n ${GREEN}🚀 Eksekusi SERANGAN FAJAR dengan ${readyWallets.length} pasukan? [ Enter = GAS / 'n' = Batal ] : ${RESET}`);
                if (conf.trim().toLowerCase() === 'n') continue;

                console.log(`\n ${RED}🚨 MENEMBAKKAN ${readyWallets.length} TRANSAKSI BERSAMAAN KE MEMPOOL!${RESET}\n`);
                
                // 🔥 PROMISE.ALL: Pemicu Eksekusi Milidetik!
                const promises = readyWallets.map(async (rw) => {
                    try {
                        let routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, rw.wallet.address, currentSlippage);
                        let successRouter = routers[0]; 
                        
                        const nonce = await provider.getTransactionCount(rw.wallet.address, "pending");
                        const tx = await rw.wallet.sendTransaction({
                            to: successRouter.to, data: successRouter.data, value: amountToBuyWei, gasLimit: 650000n,
                            maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                        });
                        console.log(`    ${GREEN}[${rw.name}] 🚀 SUKSES! Hash: ${tx.hash}${RESET}`);
                        
                        // Auto-Approve di background tanpa nunggu blok (biar ngebut)
                        autoApprove(targetToken, successRouter.to, rw.wallet).catch(()=>{});
                        return tx.hash;
                    } catch (e) {
                        console.log(`    ${RED}[${rw.name}] ❌ GAGAL: ${e.shortMessage || e.message}${RESET}`);
                        return null;
                    }
                });

                await Promise.all(promises);
                console.log(`\n ${GREEN}✅ Serangan Fajar Selesai! Pasukan berhasil mendarat.${RESET}`);
                await pause(); continue;

            } 
            // ================= MULTI SELL =================
            else {
                console.log(`\n ${GRAY}⏳ Pre-Flight: Mengecek tas koin pasukan...${RESET}`);
                let readyWallets = [];
                for (let wData of targetWalletsData) {
                    const w = new ethers.Wallet(wData.pk, provider);
                    const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, w);
                    try {
                        const tBal = await tokenContract.balanceOf(w.address);
                        if (tBal > 0n) {
                            const estGasCost = 650000n * gasConfig.maxFeePerGasConfig;
                            const bal = await provider.getBalance(w.address);
                            if (bal < estGasCost) {
                                const isFunded = await handleEmergencyGas(w.address, estGasCost);
                                if (isFunded) readyWallets.push({ wallet: w, name: wData.name, tBal });
                                else console.log(` ${GRAY}⏩ Lewati ${wData.name} (Batal diselamatkan)${RESET}`);
                            } else {
                                readyWallets.push({ wallet: w, name: wData.name, tBal });
                            }
                        } else {
                            console.log(` ${GRAY}⏩ Lewati ${wData.name} (Tidak punya token)${RESET}`);
                        }
                    } catch (e) {}
                }

                if (readyWallets.length === 0) {
                    console.log(`\n ${RED}❌ Semua dompet yang dipilih kosong dari token ini!${RESET}`);
                    await pause(); continue;
                }

                const conf = await askQuestion(`\n ${GREEN}🚀 Eksekusi DUMP MASSAL dengan ${readyWallets.length} pasukan? [ Enter = GAS / 'n' = Batal ] : ${RESET}`);
                if (conf.trim().toLowerCase() === 'n') continue;

                process.stdout.write(`\n ${GRAY}⏳ Memastikan Approve untuk ${readyWallets.length} pasukan...${RESET}`);
                const uniqueRouters = [...new Set(detectedPools.map(p => p.to))].filter(r => r !== ethers.ZeroAddress);
                
                const approvePromises = readyWallets.map(async (rw) => {
                    const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, rw.wallet);
                    for (const r of uniqueRouters) {
                        try {
                            const allow = await tokenContract.allowance(rw.wallet.address, r);
                            if (allow < rw.tBal) {
                                const nonce = await provider.getTransactionCount(rw.wallet.address, "pending");
                                const tx = await tokenContract.approve(r, ethers.MaxUint256, { nonce });
                                await tx.wait(1);
                            }
                        } catch(e) {}
                    }
                });
                await Promise.all(approvePromises);
                process.stdout.write(`\r\x1b[2K`);
                console.log(` ${GREEN}✅ Semua senjata (Approve) siap ditembakkan!${RESET}`);

                console.log(`\n ${RED}🚨 MENGIRIM DUMP SERENTAK KE MEMPOOL!${RESET}\n`);
                
                // 🔥 PROMISE.ALL: Sell serentak di milidetik yang sama
                const sellPromises = readyWallets.map(async (rw) => {
                    try {
                        let sellPayloads = buildPayloads(detectedPools, targetToken, rw.tBal, false, rw.wallet.address, currentSlippage);
                        let successPayload = sellPayloads[0];
                        
                        const nonce = await provider.getTransactionCount(rw.wallet.address, "pending");
                        const tx = await rw.wallet.sendTransaction({
                            to: successPayload.to, data: successPayload.data, gasLimit: 650000n,
                            maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                        });
                        console.log(`    ${GREEN}[${rw.name}] 💥 DUMP SUKSES! Hash: ${tx.hash}${RESET}`);
                        return tx.hash;
                    } catch (e) {
                        console.log(`    ${RED}[${rw.name}] ❌ GAGAL: ${e.shortMessage || e.message}${RESET}`);
                        return null;
                    }
                });

                await Promise.all(sellPromises);
                console.log(`\n ${GREEN}✅ Dump Massal Selesai!${RESET}`);
                await pause(); continue;
            }
        }

        // MODE APPROVE
        if (action === '6') {
            console.log(`\n ${GRAY}⏳ Melakukan Approve tanpa batas (Smart Batching)...${RESET}`);
            const routersToTest = [V4_ROUTER, V3_ROUTER, V2_ROUTER, AERO_ROUTER].filter(r => r !== ethers.ZeroAddress);
            const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, wallet);
            
            try {
                let routersToApprove = [];
                // 🔥 Baca satu per satu dengan ngebut (Anti Rate-Limit RPC Gratis)
                for (const r of routersToTest) {
                    try {
                        const allow = await tokenContract.allowance(wallet.address, r);
                        if (allow < ethers.MaxUint256 / 2n) routersToApprove.push(r);
                    } catch(e) {}
                }
                
                if (routersToApprove.length > 0) {
                    let currentNonce = await provider.getTransactionCount(wallet.address, "pending");
                    let pendingTxs = [];
                    
                    // 🔥 Kirim transaksi satu per satu biar nonce sinkron & RPC gak kaget
                    for (const router of routersToApprove) {
                        let txSuccessFlag = false;
                        while (!txSuccessFlag) {
                            try {
                                const tx = await tokenContract.approve(router, ethers.MaxUint256, { nonce: currentNonce });
                                console.log(` ${GREEN}✅ [MENGIRIM APPROVE] Hash: ${tx.hash}${RESET}`);
                                currentNonce++;
                                pendingTxs.push(tx.wait(1)); // Kumpulkan semua resinya
                                txSuccessFlag = true;
                            } catch(e) {
                                const errMsg = (e.message || "").toLowerCase();
                                if (errMsg.includes("insufficient funds")) {
                                    console.log(`\n ${RED}❌ Gagal Approve: ${e.shortMessage || errMsg}${RESET}`);
                                    const activeCfg = loadConfig();
                                    const gasConfig = await getGasConfig(BigInt(activeCfg.gwei.sell || 100));
                                    const requiredGasWei = 65000n * gasConfig.maxFeePerGasConfig; // Estimasi gas approve
                                    const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                                    if (isFunded) {
                                        continue; // Bensin masuk, coba lagi kirim approve!
                                    } else {
                                        throw new Error("Pemberian bensin dibatalkan oleh pengguna");
                                    }
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }
                    
                    if (pendingTxs.length > 0) {
                        process.stdout.write(` ${GRAY}⏳ Menunggu konfirmasi blok (Parallel)...${RESET}`);
                        await Promise.all(pendingTxs); // 🔥 Tunggu konfirmasi blok SEKALIGUS
                        console.log(`\r ${GREEN}✅ Semua approve berhasil dikonfirmasi!           ${RESET}`);
                    }
                } else {
                    console.log(` ${GREEN}✅ Semua Router sudah di-Approve sebelumnya.${RESET}`);
                }
                console.log(`\n ${GREEN}🎯 Pre-Approve Selesai! Kamu siap sell kapan saja.${RESET}`);
            } catch(e) {
                console.log(`\n ${RED}❌ Pre-Approve Gagal: ${e.message}${RESET}`);
            }
            
            await pause(); continue;
        }

        // MODE SELL
        if (action === '3') {
            const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, wallet);
            let tName = "Unknown", tSymbol = "???", tDec = 18n, tBal = 0n;
            try {
                tName = await tokenContract.name(); tSymbol = await tokenContract.symbol();
                tDec = await tokenContract.decimals(); tBal = await tokenContract.balanceOf(wallet.address);
                addHistory(targetToken, tName, tSymbol); 
            } catch (e) {
                console.log(`\n ${RED}❌ Gagal membaca kontrak token!${RESET}`); await pause(); continue;
            }

            const balFormatted = ethers.formatUnits(tBal, tDec);
            console.log(`\n ${GRAY}├─ Token: ${tName} (${tSymbol})${RESET}`);
            console.log(` ${GRAY}└─ Saldo: ${GREEN}${balFormatted} ${tSymbol}${RESET}\n`);

            if (tBal === 0n) {
                console.log(` ${RED}❌ Kamu tidak punya saldo token ini.${RESET}`); await pause(); continue;
            }

            const sellInput = await askQuestion(` ${CYAN}╰─ Berapa token yang ingin dijual? (Ketik 'max'): ${RESET}`);
            let amountToSellWei = 0n;
            try { 
                amountToSellWei = sellInput.trim().toLowerCase() === 'max' ? tBal : ethers.parseUnits(sellInput.trim(), tDec); 
            } catch(e) { 
                console.log(` ${RED}❌ Angka tidak valid!${RESET}`); await pause(); continue; 
            }

            // 🔥 FIX: Tolak jika input lebih besar dari saldo
            if (amountToSellWei > tBal) {
                console.log(`\n ${RED}❌ Saldo Token tidak cukup! Saldo maksimal kamu: ${balFormatted} ${tSymbol}${RESET}`);
                await pause(); continue;
            }

            const detectedPools = await autoDetectLP(targetToken);
            if (detectedPools.length === 0) {
                console.log(`\n ${RED}❌ SELL GAGAL: LP tidak terdeteksi.${RESET}`); await pause(); continue;
            }
            
            // 🔥 Tampilkan Semua LP yang discan
            let lpList = detectedPools.map(p => p.id).join(', ');

            console.log(`\n ${GRAY}⏳ Memastikan Status Approve (Smart Batch Mode)...${RESET}`);
            const uniqueRouters = [...new Set(detectedPools.map(p => p.to))].filter(r => r !== ethers.ZeroAddress);
            
            try {
                let routersToApprove = [];
                // 🔥 Nanya status RPC secara cepat tanpa melanggar Rate-Limit
                for (const r of uniqueRouters) {
                    try {
                        const allow = await tokenContract.allowance(wallet.address, r);
                        if (allow < amountToSellWei) routersToApprove.push(r);
                    } catch(e) {}
                }
                
                if (routersToApprove.length > 0) {
                    let approvalNonce = await provider.getTransactionCount(wallet.address, "pending");
                    let pendingTxs = [];
                    for (const router of routersToApprove) {
                        let txSuccessFlag = false;
                        while (!txSuccessFlag) {
                            try {
                                const tx = await tokenContract.approve(router, ethers.MaxUint256, { nonce: approvalNonce });
                                approvalNonce++;
                                pendingTxs.push(tx.wait(1));
                                txSuccessFlag = true;
                            } catch(e) {
                                const errMsg = (e.message || "").toLowerCase();
                                if (errMsg.includes("insufficient funds")) {
                                    console.log(`\n ${RED}❌ Gagal Approve Router: ${e.shortMessage || errMsg}${RESET}`);
                                    const requiredGasWei = 65000n * gasConfig.maxFeePerGasConfig;
                                    const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                                    if (isFunded) continue;
                                }
                                approvalNonce = await provider.getTransactionCount(wallet.address, "pending");
                                break;
                            }
                        }
                    }
                    if (pendingTxs.length > 0) {
                        process.stdout.write(` ${GRAY}⏳ Menunggu konfirmasi blok Approve...${RESET}`);
                        await Promise.all(pendingTxs); // 🔥 Tunggu konfirmasi SEKALIGUS
                        process.stdout.write(`\r\x1b[2K`); 
                    }
                }
            } catch(e) {}
            
            console.log(` ${GREEN}✅ Ready.${RESET}`);

            let activeCfg = loadConfig();
            let currentGasMultiplier = BigInt(activeCfg.gwei.sell || 100);
            let gasConfig = await getGasConfig(currentGasMultiplier);
            
            let currentSlippage = activeCfg.slip.sell.min; 
            let maxSlippage = activeCfg.slip.sell.max;
            let slipBump = activeCfg.slip.sell.bump;

            const sellPayloads = buildPayloads(detectedPools, targetToken, amountToSellWei, false, wallet.address, currentSlippage);
            let successPayload = null;
            for (let p of sellPayloads) {
                try {
                    await provider.call({ to: p.to, data: p.data, from: wallet.address });
                    successPayload = p; break; 
                } catch (e) {}
            }

            if (!successPayload) {
                console.log(`\n ${RED}❌ SELL REVERTED: Simulasi di semua LP Gagal.${RESET}`); await pause(); continue;
            }

            let txSuccess = false;

            process.stdout.write(`\n ${GRAY}⏳ Mengkalkulasi estimasi ETH yang didapat...${RESET}`);
            let usedPoolSell = detectedPools.find(p => p.id === successPayload.id) || detectedPools[0];
            let estOutputWei = await getEstimatedTokens(usedPoolSell, amountToSellWei, false, targetToken);
            let estOutputEth = estOutputWei > 0n ? ethers.formatEther(estOutputWei) : "?";
            process.stdout.write(`\r\x1b[2K`); 

            console.log(`\n ${CYAN}[ 🧾 STRUK KASIR (SELL) ]${RESET}`);
            console.log(` │`);
            console.log(` ├─ Token Target : ${GREEN}${tName} (${tSymbol})${RESET}`);
            console.log(` ├─ LP Scan      : ${CYAN}${lpList}${RESET}`);
            console.log(` ├─ Token Jual   : ${YELLOW}${parseFloat(ethers.formatUnits(amountToSellWei, tDec)).toFixed(4)} ${tSymbol}${RESET}`);
            console.log(` ├─ Est. Terima  : ${GREEN}${estOutputEth !== "?" ? parseFloat(estOutputEth).toFixed(6) : "?"} ETH${RESET}`);
            console.log(` ├─ Rute DEX     : ${successPayload.id}`);
            console.log(` └─ Est. Gas     : ${YELLOW}${parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5)} Gwei${RESET}\n`);

            const confirmSell = await askQuestion(` ${GREEN}Eksekusi Jual Ini? [ Enter = 'y' / 'n' = Batal ] : ${RESET}`);
            if (confirmSell.trim().toLowerCase() === 'n') {
                console.log(` ${RED}❌ Transaksi dibatalkan.${RESET}`);
                await pause(); continue;
            }

            console.log(` ${GREEN}🚀 Mengeksekusi Sell...${RESET}`);
            
            while (!txSuccess && currentSlippage <= maxSlippage) {
                gasConfig = await getGasConfig(currentGasMultiplier);
                let txHashStr = ""; // 🔥 TANGKAP HASH SEBELUM DIBLOKIR RPC GRATIS
                try {
                    const ethBefore = await provider.getBalance(wallet.address);

                    const tx = await wallet.sendTransaction({
                        to: successPayload.to, data: successPayload.data, gasLimit: 650000n,
                        maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: await provider.getTransactionCount(wallet.address)
                    });
                    txHashStr = tx.hash;
                    
                    process.stdout.write(`\n ${GRAY}⏳ Menunggu konfirmasi on-chain (Tx: ${tx.hash})...${RESET}`);
                    const receipt = await tx.wait(); 
                    
                    await new Promise(resolve => setTimeout(resolve, 2500));
                    const ethAfter = await provider.getBalance(wallet.address);
                    
                    const tokenSpent = ethers.formatUnits(amountToSellWei, tDec);
                    const gasCost = receipt.gasUsed * receipt.gasPrice;
                    const ethGainedWei = (ethAfter - ethBefore) + gasCost;
                    const ethGained = ethers.formatEther(ethGainedWei);
                    const actualGwei = parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5);

                    console.log(`\r ${GREEN}🚀 ${BG_GREEN} SELL BERHASIL ${RESET} ${GREEN}Hash: ${tx.hash}           ${RESET}`);
                    console.log(`    ${CYAN}⚙️ Eksekusi: ${currentSlippage}% Slip | ${actualGwei} Gwei (${currentGasMultiplier}% Network)${RESET}`);
                    console.log(`    ${RED}📉 -${parseFloat(tokenSpent).toFixed(5)} ${tSymbol}${RESET}  |  ${GREEN}📈 +${parseFloat(ethGained).toFixed(6)} ETH${RESET}\n`);
                    
                    txSuccess = true;
                } catch (err) {
                    const errMsg = (err.message || "").toLowerCase();
                    
                    // 🔥 JARING EMERGENCY GAS
                    if (errMsg.includes("insufficient funds")) {
                        console.log(`\n ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                        const requiredGasWei = 650000n * gasConfig.maxFeePerGasConfig;
                        const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                        if (isFunded) {
                            continue; // Bensin masuk, putar ulang tembakan tanpa ganti slip!
                        } else {
                            break; // Dibatalkan user
                        }
                    }

                    if (txHashStr && (errMsg.includes("forbidden") || errMsg.includes("403") || errMsg.includes("429") || errMsg.includes("rate limit") || errMsg.includes("fetch") || errMsg.includes("destroyed"))) {
                        console.log(`\r ${GREEN}✅ [TERKIRIM KE MEMPOOL] Hash: ${txHashStr}           ${RESET}`);
                        console.log(`    ${YELLOW}⚠️ RPC Limit (403/429): Transaksi sukses di-broadcast, namun RPC Gratis menolak memberikan resi konfirmasi.${RESET}`);
                        console.log(`    ${YELLOW}   Silakan cek saldo secara manual. Speed & verifikasi maksimal? Gunakan RPC Premium!${RESET}\n`);
                        txSuccess = true;
                        break;
                    }

                    if (errMsg.includes("underpriced") || errMsg.includes("fee too low")) {
                        currentGasMultiplier += 50n;
                        console.log(`\n ${YELLOW}⚠️ Gwei terlalu rendah! Auto-Bump Gas Sell ke ${currentGasMultiplier}%...${RESET}`);
                    } else if (errMsg.includes("slippage") || errMsg.includes("amountoutminimum")) {
                        currentSlippage += slipBump;
                        console.log(`\n ${YELLOW}⚠️ Terbentur Slippage! Auto-Bump Slippage ke ${currentSlippage}%...${RESET}`);
                        const sellPayloads = buildPayloads(detectedPools, targetToken, amountToSellWei, false, wallet.address, currentSlippage);
                        successPayload = sellPayloads.find(p => p.id === successPayload.id) || sellPayloads[0];
                    } else if (errMsg.includes("coalesce") || errMsg.includes("network") || errMsg.includes("timeout")) {
                        currentSlippage += slipBump;
                        console.log(`\n ${YELLOW}⚠️ Gangguan RPC (Coalesce/Slippage). Auto-Bump & Mengulang otomatis ke ${currentSlippage}%...${RESET}`);
                        const sellPayloads = buildPayloads(detectedPools, targetToken, amountToSellWei, false, wallet.address, currentSlippage);
                        successPayload = sellPayloads.find(p => p.id === successPayload.id) || sellPayloads[0];
                        await new Promise(resolve => setTimeout(resolve, 1000)); 
                    } else {
                        console.log(`\n ${RED}❌ GAGAL: ${err.shortMessage || errMsg}${RESET}`); break;
                    }
                }
            }

            await pause(); continue;
        }

        // MODE BUY & SIMULATE
        const tokenContract = new ethers.Contract(targetToken, ERC20_ABI, provider);
        let tokenName = "Unknown", tokenSymbol = "???", tDec = 18n;
        
        process.stdout.write(` ${GRAY}⏳ Mengecek kontrak token...${RESET}`);
        try { 
            tokenName = await tokenContract.name(); 
            tokenSymbol = await tokenContract.symbol(); 
            tDec = await tokenContract.decimals();
            addHistory(targetToken, tokenName, tokenSymbol); 
        } catch (e) {}
        process.stdout.write(`\r ${GREEN}✅ Token Target : ${BOLD}${tokenName} (${tokenSymbol})${RESET}         \n`);

        const inputAmount = await askQuestion(` ${CYAN}╰─ Berapa ETH yang ingin dibeli? (Max: ${balanceEth}): ${RESET}`);
        let amountToBuyWei = 0n;
        try { 
            amountToBuyWei = ethers.parseEther(inputAmount.trim() || "0"); 
        } catch(e) { 
            console.log(` ${RED}❌ Angka tidak valid!${RESET}`); await pause(); continue; 
        }
        
        if (amountToBuyWei <= 0n) continue;
        
        // 🔥 FIX: Tolak jika input lebih besar atau sama dengan saldo (karena butuh bayar Gas Fee)
        if (amountToBuyWei >= balanceWei) {
            console.log(`\n ${RED}❌ Saldo tidak cukup untuk membeli + bayar gas! Saldo kamu: ${balanceEth}${RESET}`);
            await pause(); continue;
        }

        let detectedPools = await autoDetectLP(targetToken);
        
        let activeCfg = loadConfig();
        let targetModeName = action === '1' ? 'simulate' : 'buy';
        
        let currentSlippage = activeCfg.slip[targetModeName].min; 
        let maxSlippage = activeCfg.slip[targetModeName].max;
        let slipBump = activeCfg.slip[targetModeName].bump;
        
        // 🔥 Engine Gwei Sekarang 100% Baca dari Config yang Kita Setting (Bukan Hype)
        let currentGasMultiplier = BigInt(activeCfg.gwei[targetModeName] || 100);
        let gasConfig = await getGasConfig(currentGasMultiplier);
        let routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);

        let lpList = detectedPools.length > 0 ? detectedPools.map(p => p.id).join(', ') : "Belum Tersedia";

        if (action === '2') { // BUY INSTAN
            if(detectedPools.length === 0) {
                console.log(` ${RED}❌ BUY Dibatalkan: LP belum ada sama sekali! (Gas Aman)${RESET}`);
                await pause(); continue;
            }
            let successRouter = null;
            for(let r of routers) {
                try {
                    await provider.call({ to: r.to, data: r.data, value: amountToBuyWei, from: wallet.address });
                    successRouter = r; break;
                } catch(e) {}
            }
            if(!successRouter) {
                console.log(` ${RED}❌ BUY GAGAL: Transaksi direvert kontrak di semua rute. Gas diselamatkan!${RESET}`);
                await pause(); continue;
            }

            process.stdout.write(`\n ${GRAY}⏳ Mengkalkulasi estimasi Token yang didapat...${RESET}`);
            let usedPoolBuy = detectedPools.find(p => p.id === successRouter.id) || detectedPools[0];
            let estOutputWeiToken = await getEstimatedTokens(usedPoolBuy, amountToBuyWei, true, targetToken);
            let estOutputToken = estOutputWeiToken > 0n ? ethers.formatUnits(estOutputWeiToken, tDec) : "?";
            process.stdout.write(`\r\x1b[2K`); 

            console.log(` ${CYAN}[ 🧾 STRUK KASIR (BUY INSTAN) ]${RESET}`);
            console.log(` │`);
            console.log(` ├─ Token Target : ${GREEN}${tokenName} (${tokenSymbol})${RESET}`);
            console.log(` ├─ LP Scan      : ${CYAN}${lpList}${RESET}`);
            console.log(` ├─ Modal Beli   : ${YELLOW}${inputAmount.trim()} ETH${RESET}`);
            console.log(` ├─ Est. Terima  : ${GREEN}${estOutputToken !== "?" ? parseFloat(estOutputToken).toFixed(4) : "?"} ${tokenSymbol}${RESET}`);
            console.log(` ├─ Rute DEX     : ${successRouter.id}`);
            console.log(` └─ Est. Gas     : ${YELLOW}${parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5)} Gwei${RESET}\n`);

            const confirmBuy = await askQuestion(` ${GREEN}Eksekusi Pembelian Ini? [ Enter = 'y' / 'n' = Batal ] : ${RESET}`);
            if (confirmBuy.trim().toLowerCase() === 'n') {
                console.log(` ${RED}❌ Transaksi dibatalkan.${RESET}`);
                await pause(); continue;
            }

            let txSuccess = false;
            while (!txSuccess && currentSlippage <= maxSlippage) {
                gasConfig = await getGasConfig(currentGasMultiplier);
                let txHashStr = "";
                try {
                    const nonce = await provider.getTransactionCount(wallet.address);
                    const tx = await wallet.sendTransaction({ 
                        to: successRouter.to, data: successRouter.data, value: amountToBuyWei, gasLimit: 650000n,
                        maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                    });
                    txHashStr = tx.hash;
                    
                    process.stdout.write(`\n ${GRAY}⏳ Menunggu konfirmasi on-chain (Tx: ${tx.hash})...${RESET}`);
                    const receipt = await tx.wait(); 
                    
                    let tokenGainedWei = 0n;
                    for (const log of receipt.logs) {
                        try {
                            if (log.address.toLowerCase() === targetToken.toLowerCase()) {
                                const parsedLog = tokenContract.interface.parseLog(log);
                                if (parsedLog && parsedLog.name === 'Transfer' && parsedLog.args[1].toLowerCase() === wallet.address.toLowerCase()) {
                                    tokenGainedWei += parsedLog.args[2];
                                }
                            }
                        } catch (e) {}
                    }
                    
                    const ethSpent = ethers.formatEther(amountToBuyWei);
                    const tokenGained = ethers.formatUnits(tokenGainedWei, tDec);
                    const actualGwei = parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5);

                    console.log(`\r ${GREEN}✅ ${BG_GREEN} BUY BERHASIL ${RESET} ${GREEN}Hash: ${tx.hash}           ${RESET}`);                    console.log(`    ${CYAN}⚙️ Eksekusi: ${currentSlippage}% Slip | ${actualGwei} Gwei (${currentGasMultiplier}% Network)${RESET}`);
                    console.log(`    ${RED}📉 -${parseFloat(ethSpent).toFixed(6)} ETH${RESET}  |  ${GREEN}📈 +${parseFloat(tokenGained).toFixed(5)} ${tokenSymbol}${RESET}\n`);

                    await autoApprove(targetToken, successRouter.to, wallet);
                    txSuccess = true;
                } catch (err) {
                    const errMsg = (err.message || "").toLowerCase();

                    if (txHashStr && (errMsg.includes("forbidden") || errMsg.includes("403") || errMsg.includes("429") || errMsg.includes("rate limit") || errMsg.includes("fetch") || errMsg.includes("destroyed"))) {
                        console.log(`\r ${GREEN}✅ [TERKIRIM KE MEMPOOL] Hash: ${txHashStr}           ${RESET}`);
                        console.log(`    ${YELLOW}⚠️ RPC Limit (403/429): Transaksi sukses di-broadcast, namun RPC Gratis menolak memberikan resi konfirmasi.${RESET}`);
                        console.log(`    ${YELLOW}   Silakan cek saldo secara manual. Speed & verifikasi maksimal? Gunakan RPC Premium!${RESET}\n`);
                        await autoApprove(targetToken, successRouter.to, wallet); 
                        txSuccess = true;
                        break;
                    }

                    if (errMsg.includes("insufficient funds")) {
                        console.log(`\n ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                        const requiredGasWei = 650000n * gasConfig.maxFeePerGasConfig;
                        const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                        if (isFunded) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    if (errMsg.includes("underpriced") || errMsg.includes("fee too low")) {
                        currentGasMultiplier += 50n;
                        console.log(`\n ${YELLOW}⚠️ Gwei dibantai! Naikkan tawaran Gas ke ${currentGasMultiplier}%...${RESET}`);
                    } else if (errMsg.includes("slippage") || errMsg.includes("amountoutminimum")) {
                        currentSlippage += slipBump;
                        console.log(`\n ${YELLOW}⚠️ Terbentur Slippage! Auto-Bump Slippage ke ${currentSlippage}%...${RESET}`);
                        routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);
                        successRouter = routers.find(r => r.id === successRouter.id);
                    } else if (errMsg.includes("coalesce") || errMsg.includes("network") || errMsg.includes("timeout")) {
                        currentSlippage += slipBump;
                        console.log(`\n ${YELLOW}⚠️ Gangguan RPC (Coalesce/Slippage). Auto-Bump & Mengulang otomatis ke ${currentSlippage}%...${RESET}`);
                        routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);
                        successRouter = routers.find(r => r.id === successRouter.id);
                        await new Promise(resolve => setTimeout(resolve, 1000));
                    } else {
                        console.log(`\n ${RED}❌ Gagal Eksekusi: ${err.shortMessage || errMsg}${RESET}`); break;
                    }
                }
            }
            await pause(); continue;
        } else if (action === '1') { // SIMULATE & STRIKE
            console.log(` ${CYAN}[ RINGKASAN SNIPER MODE ]${RESET}`);
            console.log(` │`);
            console.log(` ├─ Target       : ${GREEN}${tokenName} (${tokenSymbol})${RESET}`);
            console.log(` ├─ LP Scan      : ${CYAN}${lpList}${RESET}`);
            console.log(` ├─ Modal Beli   : ${YELLOW}${inputAmount.trim()} ETH${RESET}`);
            console.log(` └─ Slippage     : Min ${currentSlippage}% | Max ${maxSlippage}% | Bump +${slipBump}%\n`);

            let isLive = false;
            let attempt = 0;

            while (!isLive) {
                attempt++;
                let successRouter = null;
                let pingMs = 0;
                let pStart = Date.now();

                for (let r of routers) {
                    try {
                        pStart = Date.now();
                        const callPromise = provider.call({ to: r.to, data: r.data, value: amountToBuyWei, from: wallet.address });
                        const timeoutPromise = new Promise((_, reject) => setTimeout(() => reject(new Error("Timeout RPC")), 1500));
                        await Promise.race([callPromise, timeoutPromise]);
                        pingMs = Date.now() - pStart;
                        successRouter = r; break;
                    } catch (error) {
                        pingMs = Date.now() - pStart;
                    }
                }

                let pingColor = pingMs < 300 ? GREEN : (pingMs < 800 ? YELLOW : RED);

                if (successRouter) {
                    isLive = true;
                    process.stdout.write(`\r └─ 📡 Simulasi ke-${attempt} [${pingColor}${pingMs}ms${RESET}]: ${GREEN}SUKSES DI ${successRouter.id}! TRADE DIBUKA!${RESET}         \n`);
                    console.log(`\n${RED}🚨 MENEMBAK KE MEMPOOL ROUTER ${successRouter.id}!${RESET}`);
                    
                    let txSuccess = false;

                    while (!txSuccess && currentSlippage <= maxSlippage) {
                        let txHashStr = ""; 
                        try {
                            gasConfig = await getGasConfig(currentGasMultiplier);
                            const nonce = await provider.getTransactionCount(wallet.address);

                            const tx = await wallet.sendTransaction({
                                to: successRouter.to, data: successRouter.data, value: amountToBuyWei, gasLimit: 650000n,
                                maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                            });
                            txHashStr = tx.hash;
                            
                            process.stdout.write(`\n ${GRAY}⏳ Menunggu konfirmasi on-chain (Tx: ${tx.hash})...${RESET}`);
                            const receipt = await tx.wait(); 
                            
                            let tokenGainedWei = 0n;
                            for (const log of receipt.logs) {
                                try {
                                    if (log.address.toLowerCase() === targetToken.toLowerCase()) {
                                        const parsedLog = tokenContract.interface.parseLog(log);
                                        if (parsedLog && parsedLog.name === 'Transfer' && parsedLog.args[1].toLowerCase() === wallet.address.toLowerCase()) {
                                            tokenGainedWei += parsedLog.args[2];
                                        }
                                    }
                                } catch (e) {}
                            }
                            
                            const ethSpent = ethers.formatEther(amountToBuyWei);
                            const tokenGained = ethers.formatUnits(tokenGainedWei, tDec);
                            const actualGwei = parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5);

                            console.log(`\r ${GREEN}✅ ${BG_GREEN} SNIPE BERHASIL ${RESET} ${GREEN}Hash: ${tx.hash}           ${RESET}`);                            console.log(`    ${CYAN}⚙️ Eksekusi: ${currentSlippage}% Slip | ${actualGwei} Gwei (${currentGasMultiplier}% Network)${RESET}`);
                            console.log(`    ${RED}📉 -${parseFloat(ethSpent).toFixed(6)} ETH${RESET}  |  ${GREEN}📈 +${parseFloat(tokenGained).toFixed(5)} ${tokenSymbol}${RESET}\n`);

                            await autoApprove(targetToken, successRouter.to, wallet);
                            txSuccess = true;
                        } catch (err) {
                            const errMsg = (err.message || "").toLowerCase();

                            if (txHashStr && (errMsg.includes("forbidden") || errMsg.includes("403") || errMsg.includes("429") || errMsg.includes("rate limit") || errMsg.includes("fetch") || errMsg.includes("destroyed"))) {
                                console.log(`\r ${GREEN}✅ [TERKIRIM KE MEMPOOL] Hash: ${txHashStr}           ${RESET}`);
                                console.log(`    ${YELLOW}⚠️ RPC Limit (403/429): Transaksi sukses di-broadcast, namun RPC Gratis menolak memberikan resi konfirmasi.${RESET}`);
                                console.log(`    ${YELLOW}   Silakan cek saldo secara manual. Speed & verifikasi maksimal? Gunakan RPC Premium!${RESET}\n`);
                                await autoApprove(targetToken, successRouter.to, wallet); 
                                txSuccess = true;
                                break;
                            }

                            if (errMsg.includes("insufficient funds")) {
                                console.log(`\n ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                                const requiredGasWei = 650000n * gasConfig.maxFeePerGasConfig;
                                const isFunded = await handleEmergencyGas(wallet.address, requiredGasWei);
                                if (isFunded) {
                                    continue;
                                } else {
                                    break;
                                }
                            } else if (errMsg.includes("underpriced") || errMsg.includes("fee too low")) {
                                currentGasMultiplier += 50n;
                                console.log(`\n ${YELLOW}⚠️ Gwei dibantai! Naikkan tawaran Gas ke ${currentGasMultiplier}%...${RESET}`);
                            } else if (errMsg.includes("insufficient") || errMsg.includes("slippage") || errMsg.includes("amountoutminimum")) {
                                currentSlippage += slipBump;
                                console.log(`\n ${YELLOW}⚠️ Terbentur Slippage! Auto-Bump Slippage ke ${currentSlippage}%...${RESET}`);
                                routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);
                                successRouter = routers.find(r => r.id === successRouter.id);
                            } else if (errMsg.includes("coalesce") || errMsg.includes("network") || errMsg.includes("timeout")) {
                                currentSlippage += slipBump;
                                console.log(`\n ${YELLOW}⚠️ Gangguan RPC (Coalesce/Slippage). Auto-Bump & Mengulang otomatis ke ${currentSlippage}%...${RESET}`);
                                routers = buildPayloads(detectedPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);
                                successRouter = routers.find(r => r.id === successRouter.id);
                                await new Promise(resolve => setTimeout(resolve, 1000));
                            } else {
                                console.log(`\n ${RED}❌ Gagal Execute: ${err.shortMessage || errMsg}${RESET}`); break;
                            }
                        }
                    }
                    break; 
                } else {
                    let statusMsg = routers.length === 0 ? `${YELLOW}Mencari Kolam LP (Scanning V2/V3)...` : `${RED}Gagal (Terkunci/Revert). Retrying...`;
                    const logText = ` └─ 📡 Simulasi ke-${attempt} [${pingColor}${pingMs}ms${RESET}]: ${statusMsg}${RESET}`;
                    process.stdout.write(`\r${logText.padEnd(85, " ")}`);
                    
                    if (attempt % 20 === 0) {
                        const newPools = await autoDetectLP(targetToken, true); 
                        if(newPools.length > 0) {
                            detectedPools = newPools; 
                            routers = buildPayloads(newPools, targetToken, amountToBuyWei, true, wallet.address, currentSlippage);
                        }
                    }
                    const delayTime = (OVERRIDE_RPC_MODE || activeCfg.active_mode) === 'gratis' ? 1500 : 150;
                    await new Promise(resolve => setTimeout(resolve, delayTime));
                }
            }
            await pause(); continue;
        }
    }
}

// =================
// 6. WALLET MANAGER MENU
// =================
async function executeWalletManager() {
    while(true) {
        clear(); title();
        const db = loadDB();
        const masterAddr = db.master_index !== null ? db.wallets[db.master_index].address : "Belum di-set";

        console.log(` ${CYAN}[ MANAJEMEN DOMPET / WALLET DATABASE ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ Total Dompet Tersimpan : ${YELLOW}${db.wallets.length} Wallets${RESET}`);
        console.log(` ├─ Master Wallet Aktif    : ${GREEN}${masterAddr}${RESET}`);
        console.log(` │`);
        console.log(` ├─ [1] 🛠️ Generate Wallets Baru (Max 1000)`);
        console.log(` ├─ [2] 📥 Import Massal dari File (.txt / .json)`);
        console.log(` ├─ [3] 📤 Export Seluruh Database ke File (.txt)`);
        console.log(` ├─ [4] 👑 Set Master Wallet`);
        console.log(` ├─ [5] 🗑️ Hapus Database (Reset)`);
        console.log(` ├─ [6] ➕ Tambah 1 Wallet Manual (Private Key)`);
        console.log(` ├─ [7] 📋 List Detail (Export 1, Hapus 1, Info)`);
        console.log(` │`);
        console.log(` └─ [0] Kembali ke Menu Utama\n`);

        const action = await askQuestion(` ╰─ Pilih aksi: `);
        
        if (action === '0') return;

        if (action === '1') {
            const countStr = await askQuestion(`\n ${CYAN}╰─ Berapa wallet yang ingin di-generate? (Max 1000): ${RESET}`);
            let count = parseInt(countStr);
            if(isNaN(count) || count <= 0) { await pause(); continue; }
            if(count > 1000) count = 1000;

            console.log(` ${GRAY}⏳ Generating ${count} wallets...${RESET}`);
            const prefixName = await askQuestion(` ${CYAN}╰─ Masukkan nama awalan (Contoh: Tuyul): ${RESET}`) || "Wallet";
            
            let newWallets = [];
            let exportText = "";

            for (let i = 0; i < count; i++) {
                const w = ethers.Wallet.createRandom();
                const name = `${prefixName}-${db.wallets.length + 1}`;
                newWallets.push({ name: name, address: w.address, pk: w.privateKey, seed: w.mnemonic.phrase });
                
                exportText += `Name: ${name}\n`;
                exportText += `Address: ${w.address}\n`;
                exportText += `Private Key: ${w.privateKey}\n`;
                exportText += `Seed Phrase: ${w.mnemonic.phrase}\n\n`;
                
                db.wallets.push(newWallets[i]);
            }
            
            if (db.master_index === null) db.master_index = 0;
            saveDB(db);

            const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
            const filename = `wallet_${timestamp}.txt`;
            fs.writeFileSync(filename, exportText);

            console.log(`\n ${GREEN}✅ Berhasil mencetak ${count} wallet!${RESET}`);
            console.log(` ${GREEN}✅ Data disimpan ke database dan diekspor ke: ${filename}${RESET}`);
            await pause();
        }

        if (action === '2') {
            const filename = await askQuestion(`\n ${CYAN}╰─ Masukkan nama file (contoh: keys.txt atau data.json): ${RESET}`);
            if (!fs.existsSync(filename)) {
                console.log(` ${RED}❌ File tidak ditemukan!${RESET}`);
                await pause(); continue;
            }

            console.log(` ${GRAY}⏳ Membaca file...${RESET}`);
            const content = fs.readFileSync(filename, 'utf8');
            let addedCount = 0;

            if (filename.endsWith('.json')) {
                try {
                    const parsed = JSON.parse(content);
                    const arr = Array.isArray(parsed) ? parsed : (parsed.wallets || []);
                    for (let item of arr) {
                        if (item.privateKey || item.pk) {
                            const pk = item.privateKey || item.pk;
                            const w = new ethers.Wallet(pk);
                            const exists = db.wallets.find(x => x.address === w.address);
                            if (!exists) {
                                db.wallets.push({ name: item.name || `Imported-${db.wallets.length+1}`, address: w.address, pk: w.privateKey, seed: item.seed || item.mnemonic || "" });
                                addedCount++;
                            }
                        }
                    }
                } catch(e) {
                    console.log(` ${RED}❌ Format JSON tidak valid!${RESET}`);
                }
            } else {
                const lines = content.split('\n').map(l => l.trim()).filter(l => l.length > 0);
                for (let line of lines) {
                    try {
                        let w = null;
                        if (/^(0x)?[0-9a-fA-F]{64}$/.test(line)) {
                            w = new ethers.Wallet(line.startsWith('0x') ? line : '0x' + line);
                        } 
                        else if (line.split(' ').length === 12 || line.split(' ').length === 24) {
                            w = ethers.Wallet.fromPhrase(line);
                        }
                        
                        if (w) {
                            const exists = db.wallets.find(x => x.address === w.address);
                            if (!exists) {
                                db.wallets.push({ name: `Imported-${db.wallets.length+1}`, address: w.address, pk: w.privateKey, seed: w.mnemonic ? w.mnemonic.phrase : "" });
                                addedCount++;
                            }
                        }
                    } catch(e) {}
                }
            }

            if (db.master_index === null && db.wallets.length > 0) db.master_index = 0;
            saveDB(db);
            console.log(`\n ${GREEN}✅ Berhasil mengimpor ${addedCount} wallet baru (Anti-Duplikat)!${RESET}`);
            await pause();
        }

        if (action === '3') {
            if (db.wallets.length === 0) {
                console.log(`\n ${RED}❌ Database kosong!${RESET}`);
                await pause(); continue;
            }
            let exportText = "";
            db.wallets.forEach(w => {
                exportText += `Name: ${w.name}\nAddress: ${w.address}\nPrivate Key: ${w.pk}\nSeed Phrase: ${w.seed}\n\n`;
            });
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
            const filename = `export_all_${timestamp}.txt`;
            fs.writeFileSync(filename, exportText);
            console.log(`\n ${GREEN}✅ ${db.wallets.length} wallet berhasil diekspor ke: ${filename}${RESET}`);
            await pause();
        }

        if (action === '4') {
            if (db.wallets.length === 0) {
                console.log(`\n ${RED}❌ Database kosong!${RESET}`);
                await pause(); continue;
            }
            console.log(`\n ${CYAN}[ DAFTAR WALLET ]${RESET}`);
            for(let i=0; i<Math.min(db.wallets.length, 20); i++) {
                console.log(` [${i}] ${db.wallets[i].name} - ${db.wallets[i].address}`);
            }
            if(db.wallets.length > 20) console.log(` ... dan ${db.wallets.length - 20} lainnya.`);
            
            const idxStr = await askQuestion(`\n ${CYAN}╰─ Pilih Index [0 - ${db.wallets.length-1}] untuk Master: ${RESET}`);
            const idx = parseInt(idxStr);
            if (!isNaN(idx) && idx >= 0 && idx < db.wallets.length) {
                db.master_index = idx;
                saveDB(db);
                console.log(` ${GREEN}✅ Master Wallet diubah ke: ${db.wallets[idx].name}${RESET}`);
            }
            await pause();
        }

        if (action === '5') {
            const confirm = await askQuestion(`\n ${RED}⚠️ YAKIN INGIN MENGHAPUS SEMUA DATA WALLET? (y/n): ${RESET}`);
            if (confirm.toLowerCase() === 'y') {
                if(fs.existsSync(DB_FILE)) fs.unlinkSync(DB_FILE);
                console.log(` ${GREEN}✅ Database dibersihkan!${RESET}`);
            }
            await pause();
        }

        if (action === '6') {
            const pkInput = await askQuestion(`\n ${CYAN}╰─ Masukkan Private Key: ${RESET}`);
            try {
                const w = new ethers.Wallet(pkInput.trim());
                const exists = db.wallets.find(x => x.address === w.address);
                if (exists) {
                    console.log(` ${RED}❌ Wallet sudah ada di database!${RESET}`);
                } else {
                    const name = await askQuestion(` ${CYAN}╰─ Beri nama wallet ini: ${RESET}`) || `Manual-${db.wallets.length + 1}`;
                    db.wallets.push({ name: name, address: w.address, pk: w.privateKey, seed: w.mnemonic ? w.mnemonic.phrase : "Imported PK" });
                    if (db.master_index === null) db.master_index = 0;
                    saveDB(db);
                    console.log(` ${GREEN}✅ Wallet ${name} berhasil ditambahkan!${RESET}`);
                }
            } catch(e) {
                console.log(` ${RED}❌ Private Key tidak valid!${RESET}`);
            }
            await pause();
        }

        if (action === '7') {
            if (db.wallets.length === 0) {
                console.log(`\n ${RED}❌ Database kosong!${RESET}`);
                await pause(); continue;
            }
            console.log(`\n ${CYAN}[ DAFTAR DETAIL WALLET ]${RESET}`);
            for(let i=0; i<db.wallets.length; i++) {
                const isMaster = i === db.master_index ? ` ${GREEN}(👑 MASTER)${RESET}` : "";
                console.log(` [${i}] ${db.wallets[i].name} - ${db.wallets[i].address}${isMaster}`);
            }
            const idxStr = await askQuestion(`\n ${CYAN}╰─ Pilih Index Wallet [0 - ${db.wallets.length-1}]: ${RESET}`);
            const idx = parseInt(idxStr);
            if (!isNaN(idx) && idx >= 0 && idx < db.wallets.length) {
                console.log(`\n ${YELLOW}Aksi untuk Wallet [${db.wallets[idx].name}]:${RESET}`);
                console.log(` [1] 📤 Export (Lihat Private Key)`);
                console.log(` [2] 🗑️ Hapus Wallet Ini`);
                const subAct = await askQuestion(` ╰─ Pilih aksi: `);
                
                if (subAct === '1') {
                    console.log(`\n ${CYAN}--- EXPORT WALLET ---${RESET}`);
                    console.log(` Name    : ${db.wallets[idx].name}`);
                    console.log(` Address : ${db.wallets[idx].address}`);
                    console.log(` PK      : ${db.wallets[idx].pk}`);
                    console.log(` Seed    : ${db.wallets[idx].seed || "N/A"}`);
                    console.log(` ${CYAN}---------------------${RESET}`);
                } else if (subAct === '2') {
                    const conf = await askQuestion(` ${RED}Yakin hapus ${db.wallets[idx].name}? (y/n): ${RESET}`);
                    if(conf.toLowerCase() === 'y') {
                        db.wallets.splice(idx, 1);
                        if (db.master_index === idx) db.master_index = db.wallets.length > 0 ? 0 : null;
                        else if (db.master_index > idx) db.master_index--;
                        saveDB(db);
                        console.log(` ${GREEN}✅ Wallet dihapus!${RESET}`);
                    }
                }
            }
            await pause();
        }
    }
}

// =================
// 7. RPC MANAGER MENU
// =================
async function executeRpcManager() {
    while(true) {
        clear(); title();
        const cfg = loadConfig();
        
        let activeArr = cfg.active_mode === "premium" ? cfg.premium_urls : cfg.free_urls;
        let activeIdx = cfg.active_mode === "premium" ? cfg.active_premium_index : cfg.active_free_index;
        let activeUrlDisplay = (activeArr && activeArr.length > 0) ? activeArr[activeIdx % activeArr.length] : "Belum diset";

        console.log(` ${CYAN}[ KELOLA NODE RPC (PREMIUM & GRATIS) ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ Mode RPC  : ${cfg.active_mode === "premium" ? CYAN + "⚡ PREMIUM (WSS)" : GREEN + "✅ GRATISAN (HTTP)"}${RESET}`);
        console.log(` ├─ RPC Aktif : ${GREEN}${activeUrlDisplay}${RESET}`);
        console.log(` ├─ Status    : ${RPC_CONNECTED ? GREEN + "✅ ONLINE" : RED + "❌ OFFLINE"}${RESET}`);
        console.log(` │`);
        console.log(` ├─ [1] 🔄 Ganti Mode RPC (Premium <-> Gratis)`);
        console.log(` ├─ [2] ➕ Tambah Node RPC Premium (WSS)`);
        console.log(` ├─ [3] ➕ Tambah Node RPC Gratis (HTTPS)`);
        console.log(` ├─ [4] 📋 Lihat Daftar Node RPC`);
        console.log(` ├─ [5] 🎯 Pilih Node RPC Aktif`);
        console.log(` ├─ [6] 🗑️ Hapus Node RPC`);
        console.log(` │`);
        console.log(` └─ [0] Kembali ke Menu Utama\n`);

        const action = await askQuestion(` ╰─ Pilih aksi: `);
        if (action === '0') return;

        if (action === '1') {
            cfg.active_mode = cfg.active_mode === "premium" ? "gratis" : "premium";
            saveConfig(cfg);
            console.log(` ${GREEN}✅ Mode diubah ke: ${cfg.active_mode.toUpperCase()}${RESET}`);
            connectRPC();
            await pause();
        }
        if (action === '2') {
            const newRpc = await askQuestion(`\n ${CYAN}╰─ Masukkan WSS URL baru: ${RESET}`);
            if (newRpc.trim().startsWith("wss://")) {
                cfg.premium_urls.push(newRpc.trim());
                saveConfig(cfg);
                console.log(` ${GREEN}✅ RPC Premium berhasil ditambahkan!${RESET}`);
            } else {
                console.log(` ${RED}❌ URL harus diawali dengan wss://${RESET}`);
            }
            await pause();
        }
        if (action === '3') {
            const newRpc = await askQuestion(`\n ${CYAN}╰─ Masukkan HTTP/HTTPS URL baru: ${RESET}`);
            if (newRpc.trim().startsWith("http")) {
                cfg.free_urls.push(newRpc.trim());
                saveConfig(cfg);
                console.log(` ${GREEN}✅ RPC Gratis berhasil ditambahkan!${RESET}`);
            } else {
                console.log(` ${RED}❌ URL harus diawali dengan http:// atau https://${RESET}`);
            }
            await pause();
        }
        if (action === '4') {
            console.log(`\n ${CYAN}[ DAFTAR NODE RPC ]${RESET}`);
            console.log(`\n ${YELLOW}--- PREMIUM (WSS) ---${RESET}`);
            if (!cfg.premium_urls || cfg.premium_urls.length === 0) console.log(` ${GRAY}Kosong${RESET}`);
            else cfg.premium_urls.forEach((r, i) => {
                const activeStr = (cfg.active_mode === "premium" && i === cfg.active_premium_index) ? ` ${GREEN}(Aktif)${RESET}` : "";
                console.log(` [P${i}] ${r}${activeStr}`);
            });
            console.log(`\n ${YELLOW}--- GRATIS (HTTP) ---${RESET}`);
            if (!cfg.free_urls || cfg.free_urls.length === 0) console.log(` ${GRAY}Kosong${RESET}`);
            else cfg.free_urls.forEach((r, i) => {
                const activeStr = (cfg.active_mode === "gratis" && i === cfg.active_free_index) ? ` ${GREEN}(Aktif)${RESET}` : "";
                console.log(` [G${i}] ${r}${activeStr}`);
            });
            await pause();
        }
        if (action === '5') {
            console.log(`\n ${CYAN}[ PILIH NODE RPC AKTIF ]${RESET}`);
            const modeChoice = await askQuestion(` ╰─ Pilih kategori (1 = Premium, 2 = Gratis): `);
            let arr = modeChoice === '1' ? cfg.premium_urls : cfg.free_urls;
            let typeStr = modeChoice === '1' ? "premium" : "gratis";
            
            if (!arr || arr.length === 0) {
                console.log(` ${RED}❌ Daftar kosong!${RESET}`);
            } else {
                arr.forEach((r, i) => console.log(` [${i}] ${r}`));
                const idxStr = await askQuestion(`\n ${CYAN}╰─ Pilih Index [0 - ${arr.length-1}]: ${RESET}`);
                const idx = parseInt(idxStr);
                if (!isNaN(idx) && idx >= 0 && idx < arr.length) {
                    cfg.active_mode = typeStr;
                    if (typeStr === "premium") cfg.active_premium_index = idx;
                    else cfg.active_free_index = idx;
                    saveConfig(cfg);
                    console.log(` ${GRAY}⏳ Menyambungkan ulang ke RPC baru...${RESET}`);
                    connectRPC();
                    console.log(` ${GREEN}✅ RPC Aktif diubah!${RESET}`);
                }
            }
            await pause();
        }
        if (action === '6') {
            console.log(`\n ${CYAN}[ HAPUS NODE RPC ]${RESET}`);
            const modeChoice = await askQuestion(` ╰─ Pilih kategori (1 = Premium, 2 = Gratis): `);
            let arr = modeChoice === '1' ? cfg.premium_urls : cfg.free_urls;
            let typeStr = modeChoice === '1' ? "premium" : "gratis";
            
            if (!arr || arr.length === 0) {
                console.log(` ${RED}❌ Daftar kosong!${RESET}`);
            } else {
                arr.forEach((r, i) => console.log(` [${i}] ${r}`));
                const idxStr = await askQuestion(`\n ${CYAN}╰─ Pilih Index yang ingin dihapus: ${RESET}`);
                const idx = parseInt(idxStr);
                if (!isNaN(idx) && idx >= 0 && idx < arr.length) {
                    arr.splice(idx, 1);
                    if (typeStr === "premium" && cfg.active_premium_index >= idx) cfg.active_premium_index = Math.max(0, cfg.active_premium_index - 1);
                    if (typeStr === "gratis" && cfg.active_free_index >= idx) cfg.active_free_index = Math.max(0, cfg.active_free_index - 1);
                    saveConfig(cfg);
                    console.log(` ${GREEN}✅ RPC berhasil dihapus!${RESET}`);
                }
            }
            await pause();
        }
    }
}

// =================
// FITUR WALLET MANAGER (MENU 2, 3, 4, 5)
// =================
async function executeTransfer() {
    clear(); title();
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";
    console.log(` ${CYAN}[ 💸 KIRIM DANA / ASET (${sym} / TOKEN) ]${RESET}\n`);
    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj) { console.log(` ${RED}❌ Master Wallet belum diset!${RESET}`); await pause(); return; }
    const wallet = new ethers.Wallet(mWalletObj.pk, provider);

    // 🔥 INTEGRASI BUKU RIWAYAT KONTRAK (HISTORY)
    const history = loadHistory();
    if (history.length > 0) {
        console.log(` ${CYAN}[ RIWAYAT KONTRAK TERAKHIR ]${RESET}`);
        history.forEach((h, i) => {
            console.log(` ${GRAY}[${i + 1}]${RESET} ${h.name} (${h.symbol}) - ${h.address}`);
        });
        console.log("");
    }

    // ⚠️ PERHATIAN: 0 untuk transfer koin native, karena angka 1 dipakai untuk riwayat
    console.log(` ${GRAY}Ketik ${GREEN}0${GRAY} untuk transfer ${sym}, Pilih Riwayat (1-${history.length}), atau Paste ${GREEN}Kontrak${GRAY}.${RESET}`);
    const assetInputRaw = await askQuestion(` ${CYAN}╰─ Pilihan (0 / Angka / Kontrak): ${RESET}`);
    let assetInput = assetInputRaw.trim();
    
    let isEth = assetInput === '0';
    let tokenContract = null;
    let tDec = 18n;
    let tSymbol = sym;
    let balWei = 0n;
    let targetToken = "";

    if (!isEth) {
        const historyIdx = parseInt(assetInput) - 1;
        if (!isNaN(historyIdx) && historyIdx >= 0 && historyIdx < history.length) {
            targetToken = history[historyIdx].address;
        } else if (assetInput.length === 42) {
            targetToken = assetInput;
        } else {
            console.log(` ${RED}❌ Pilihan tidak valid!${RESET}`); await pause(); return;
        }

        tokenContract = new ethers.Contract(targetToken, ERC20_ABI, wallet);
        try {
            let tName = await tokenContract.name();
            tSymbol = await tokenContract.symbol();
            tDec = await tokenContract.decimals();
            balWei = await tokenContract.balanceOf(wallet.address);
            addHistory(targetToken, tName, tSymbol); // 🔥 SAVE KE DATABASE HISTORY (SINERGI)
        } catch(e) {
            console.log(` ${RED}❌ Gagal membaca data Token! Pastikan kontrak benar.${RESET}`); await pause(); return;
        }
    } else {
        balWei = await provider.getBalance(wallet.address);
    }

    console.log(`\n ${GRAY}├─ Saldo Anda: ${GREEN}${ethers.formatUnits(balWei, tDec)} ${tSymbol}${RESET}`);

    // 🔥 SMART ADDRESS BOOK & PAGINATION
    const db = loadDB();
    let page = 0;
    const limit = 21; // 7 baris x 3 kolom biar presisi
    let targetAddress = "";
    let resolvedName = "";

    while (true) {
        console.log(`\n ${CYAN}[ 📓 BUKU ALAMAT LOKAL (Hal. ${page + 1}) ]${RESET}`);
        let start = page * limit;
        let end = Math.min(start + limit, db.wallets.length);
        
        let rows = 7; // Statis 7 baris ke bawah per halaman
        for (let r = 0; r < rows; r++) {
            let rowStr = "";
            for (let c = 0; c < 3; c++) {
                let idx = start + (c * rows) + r; // 🔥 Logika Vertical Sorting
                if (idx < end) {
                    let w = db.wallets[idx];
                    let shortAddr = w.address.substring(w.address.length - 4);
                    
                    let idxStr = idx.toString().padStart(2, ' '); 
                    let maxName = 9;
                    let entryName = w.name.length > maxName ? w.name.substring(0, 7) + ".." : w.name;
                    let paddedName = entryName.padEnd(maxName, ' ');
                    
                    // Hilangkan ├─ dan rapikan spasi
                    let entry = `  [${idxStr}] ${paddedName} (..${shortAddr})`;
                    rowStr += entry.padEnd(28, ' '); // Lebar kolom statis 28 karakter
                }
            }
            if (rowStr.trim().length > 0) console.log(rowStr);
        }

        if (end < db.wallets.length) {
            console.log(` └─ ${GRAY}*Ketik '${GREEN}next${GRAY}' untuk melihat halaman selanjutnya. (Total: ${db.wallets.length} dompet)${RESET}`);
        } else {
            console.log(` └─ ${GRAY}*Total ${db.wallets.length} dompet tersimpan.${RESET}`);
        }

        const targetInput = await askQuestion(`\n ${CYAN}╰─ Alamat Tujuan (Paste 0x... / Nama / Nomor [0-${db.wallets.length - 1}] / 'next') : ${RESET}`);
        let inputClean = targetInput.trim();

        if (inputClean.toLowerCase() === 'next') {
            if (end < db.wallets.length) {
                page++;
            } else {
                page = 0; // Balik ke halaman 1 kalau udah habis
                console.log(` ${YELLOW}⚠️ Ini halaman terakhir. Kembali ke halaman 1...${RESET}`);
            }
            continue;
        }

        // Deteksi Address 0x Eksternal
        if (inputClean.startsWith('0x') && inputClean.length === 42) {
            targetAddress = inputClean;
            break;
        }

        // Deteksi Nomor Index (Angka Murni)
        if (/^\d+$/.test(inputClean)) {
            let idx = parseInt(inputClean);
            if (idx >= 0 && idx < db.wallets.length) {
                targetAddress = db.wallets[idx].address;
                resolvedName = db.wallets[idx].name;
                console.log(` ${GREEN}✅ Ditemukan: ${resolvedName} (${targetAddress})${RESET}`);
                break;
            }
        }

        // Deteksi Nama Database (Tuyul)
        const foundWallet = db.wallets.find(w => w.name.toLowerCase() === inputClean.toLowerCase());
        if (foundWallet) {
            targetAddress = foundWallet.address;
            resolvedName = foundWallet.name;
            console.log(` ${GREEN}✅ Ditemukan: ${resolvedName} (${targetAddress})${RESET}`);
            break;
        }

        console.log(` ${RED}❌ Input tidak valid! Alamat salah, nomor di luar batas, atau nama tidak ditemukan.${RESET}`);
    }
    
    const amountInput = await askQuestion(`\n ${CYAN}╰─ Jumlah ${tSymbol} (Ketik 'max' atau angka): ${RESET}`);
    let isMax = amountInput.trim().toLowerCase() === 'max';
    let sendWei = 0n;

    try {
        if (!isMax) {
            sendWei = isEth ? ethers.parseEther(amountInput.trim() || "0") : ethers.parseUnits(amountInput.trim() || "0", tDec);
            if (sendWei > balWei) {
                console.log(`\n ${RED}❌ Saldo tidak cukup! Maksimal yang bisa dikirim: ${ethers.formatUnits(balWei, isEth ? 18 : tDec)} ${tSymbol}${RESET}`);
                await pause(); return;
            }
        }

        if (isEth) {
            // 🔥 REQUEST VIP: Ikuti aturan murni blockchain (seperti Gas Darurat)
            // Biarkan node ethers.js yang membaca fee L2 + L1 secara otomatis.
            let estimatedGas = 21000n;
            try { estimatedGas = await provider.estimateGas({ to: targetAddress, value: 1n }); } catch(e) {}
            
            let feeData = await provider.getFeeData();
            let currentGasPrice = feeData.maxFeePerGas || feeData.gasPrice || ethers.parseUnits("0.005", "gwei");
            
            // Ini ongkos gas asli jaringan detik ini
            let actualCost = estimatedGas * currentGasPrice;
            
            // Rule: Simpan tepat 50% dari gas fee saat ini sebagai remahan di dompet
            let remahanDiWallet = (actualCost * 50n) / 100n; 
            
            let totalCostSafe = actualCost + remahanDiWallet;

            if (isMax) {
                sendWei = balWei - totalCostSafe;
                if (sendWei <= 0n) { console.log(`\n ${RED}❌ Saldo ${sym} tidak cukup untuk bayar Gas + Remahan 50%!${RESET}`); await pause(); return; }
            } else {
                sendWei = ethers.parseEther(amountInput.trim() || "0");
            }

            console.log(`\n ${GRAY}⏳ Mengirim ${ethers.formatEther(sendWei)} ${sym}...${RESET}`);
            // 🔥 Eksekusi natural! Hapus hardcode gas parameter agar blockchain leluasa mengatur fee
            const tx = await wallet.sendTransaction({ 
                to: targetAddress, 
                value: sendWei
            });
            console.log(` ${GREEN}✅ Tx Broadcasted! Hash: ${tx.hash}${RESET}`);
            
            try {
                const receipt = await tx.wait();
                if (receipt && receipt.status === 0) {
                    console.log(` ${RED}❌ Gagal On-Chain: Transaksi direvert (Cek Explorer).${RESET}`);
                } else {
                    console.log(` ${GREEN}✅ Konfirmasi On-Chain Sukses! Dana telah masuk.${RESET}`);
                }
            } catch(err) {
                console.log(` ${GREEN}✅ Transaksi terkirim ke jaringan! (Abaikan pesan error jika dana sudah masuk)${RESET}`);
            }

        } else {
            if (isMax) { sendWei = balWei; } 
            else { sendWei = ethers.parseUnits(amountInput.trim() || "0", tDec); }
            
            if (sendWei <= 0n) { console.log(`\n ${RED}❌ Saldo Token kosong atau jumlah 0!${RESET}`); await pause(); return; }

            console.log(`\n ${GRAY}⏳ Mengirim ${ethers.formatUnits(sendWei, tDec)} ${tSymbol}...${RESET}`);
            const tx = await tokenContract.transfer(targetAddress, sendWei);
            console.log(` ${GREEN}✅ Berhasil! Hash: ${tx.hash}${RESET}`);
            await tx.wait();
        }
    } catch (e) { console.log(`\n ${RED}❌ Gagal: ${e.shortMessage || e.message}${RESET}`); }
    await pause();
}

async function executeDisperseETH(isGasMode = false) {
    clear(); title();
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";
    const titleName = isGasMode ? "DISTRIBUSI GAS (RECEHAN)" : "MASS DISPERSE (MODAL TEMPUR)";
    console.log(` ${CYAN}[ ${titleName} ]${RESET}\n`);
    const db = loadDB();
    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj || db.wallets.length < 2) { console.log(` ${RED}❌ Butuh Master Wallet & Minimal 1 Dompet Tuyul!${RESET}`); await pause(); return; }
    const wallet = new ethers.Wallet(mWalletObj.pk, provider);

    // 🔥 IDE VIP: Radar Dompet Vertikal Dinamis (Tanpa Pagination)
    console.log(`\n ${CYAN}[ 📓 PILIH DOMPET TUYUL ]${RESET}`);
    let cols = 3;
    let rows = Math.ceil(db.wallets.length / cols);
    for (let r = 0; r < rows; r++) {
        let rowStr = "";
        for (let c = 0; c < cols; c++) {
            let idx = (c * rows) + r; // Logika urut ke bawah
            if (idx < db.wallets.length) {
                let w = db.wallets[idx];
                let shortAddr = w.address.substring(w.address.length - 4);
                let idxStr = idx.toString().padStart(2, ' ');
                let maxName = 9;
                let entryName = w.name.length > maxName ? w.name.substring(0, 7) + ".." : w.name;
                let paddedName = entryName.padEnd(maxName, ' ');
                
                // Susun plain text dulu biar padding rata kanan-kiri gak rusak
                let plainEntry = `  [${idxStr}] ${paddedName} (..${shortAddr})`;
                // Kasih Mahkota buat Master biar gak salah tembak!
                if (idx === db.master_index) plainEntry = `👑[${idxStr}] ${paddedName} (..${shortAddr})`;
                
                rowStr += plainEntry.padEnd(28, ' ');
            }
        }
        if (rowStr.trim().length > 0) console.log(rowStr);
    }

    console.log(`\n ${GRAY}Total Dompet: ${db.wallets.length} | Master Index: [${db.master_index}]${RESET}`);
    const idxInput = await askQuestion(` ${CYAN}╰─ Pilih Tuyul (Contoh: 1-5, 8, 10-12 atau ketik 'all'): ${RESET}`);

    let selectedIndexes = new Set();
    if (idxInput.trim().toLowerCase() === 'all') {
        for (let i = 0; i < db.wallets.length; i++) {
            if (i !== db.master_index) selectedIndexes.add(i);
        }
    } else {
        const parts = idxInput.split(',');
        for (let p of parts) {
            p = p.trim();
            if (p.includes('-')) {
                const [start, end] = p.split('-').map(Number);
                if (!isNaN(start) && !isNaN(end)) {
                    for (let i = Math.min(start, end); i <= Math.max(start, end); i++) {
                        if (i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
                    }
                }
            } else {
                const i = parseInt(p);
                if (!isNaN(i) && i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
            }
        }
    }

    const targetWallets = Array.from(selectedIndexes).map(i => db.wallets[i]);
    if (targetWallets.length === 0) {
        console.log(`\n ${RED}❌ Tidak ada dompet valid yang dipilih! Pastikan index benar.${RESET}`);
        await pause(); return;
    }

    let valPerWallet = 0n;
    let gasMultiplierString = "";

    const feeData = await provider.getFeeData();
    const baseGasPrice = feeData.gasPrice || feeData.maxFeePerGas || ethers.parseUnits("0.005", "gwei");

    if (isGasMode) {
        const inputTx = await askQuestion(` ${CYAN}╰─ Berapa kali TX gas yang ingin dikirim per dompet? (Max 5): ${RESET}`);
        let txCount = parseInt(inputTx);
        if (isNaN(txCount) || txCount < 1) txCount = 1;
        if (txCount > 5) txCount = 5;

        const multiplier = 50n + (BigInt(txCount) * 100n); 
        gasMultiplierString = `${multiplier}% Gwei`;

        const erc20GasLimit = 65000n; 
        valPerWallet = (erc20GasLimit * baseGasPrice * multiplier) / 100n;
    } else {
        const amount = await askQuestion(` ${CYAN}╰─ Jumlah ${sym} PER DOMPET (Contoh: 0.05): ${RESET}`);
        try { valPerWallet = ethers.parseEther(amount || "0"); } 
        catch(e) { console.log(` ${RED}❌ Nominal tidak valid!${RESET}`); await pause(); return; }
        if (valPerWallet <= 0n) return;
    }

    // Biarkan node murni membaca ongkos asli
    const masterGasLimit = 21000n;
    const currentGasPrice = feeData.maxFeePerGas || feeData.gasPrice || ethers.parseUnits("0.005", "gwei"); 
    const gasPerSend = masterGasLimit * currentGasPrice;

    const totalTarget = BigInt(targetWallets.length);
    const totalValueToSend = valPerWallet * totalTarget;
    const totalGasCost = gasPerSend * totalTarget;
    const totalRequired = totalValueToSend + totalGasCost;

    const masterBalWei = await provider.getBalance(wallet.address);

    console.log(`\n ${CYAN}[ 🧾 STRUK KALKULASI ]${RESET}`);
    console.log(` │`);
    console.log(` ├─ Target Tuyul   : ${YELLOW}${targetWallets.length} Dompet${RESET}`);
    console.log(` ├─ ${sym.padEnd(8, ' ')} / Tuyul : ${GREEN}${ethers.formatEther(valPerWallet)} ${sym}${isGasMode ? ' ('+gasMultiplierString+')' : ''}${RESET}`);
    console.log(` ├─ Total Kirim    : ${YELLOW}${ethers.formatEther(totalValueToSend)} ${sym}${RESET}`);
    console.log(` ├─ Est. Gas Admin : ${YELLOW}${ethers.formatEther(totalGasCost)} ${sym} (${parseFloat(ethers.formatUnits(currentGasPrice, "gwei")).toFixed(5)} Gwei)${RESET}`);
    console.log(` └─ Total Potongan : ${GREEN}${ethers.formatEther(totalRequired)} ${sym}${RESET}\n`);

    if (masterBalWei < totalRequired) {
        console.log(` ${RED}❌ Saldo Master (${ethers.formatEther(masterBalWei)} ${sym}) TIDAK CUKUP untuk eksekusi ini!${RESET}`);
        await pause(); return;
    }

    const confirm = await askQuestion(` ${GREEN}Eksekusi Pengiriman Ini? [ Enter = 'y' / 'n' = Batal ] : ${RESET}`);
    if (confirm.trim().toLowerCase() === 'n') {
        console.log(` ${RED}❌ Transaksi dibatalkan.${RESET}`);
        await pause(); return;
    }

    console.log(`\n ${GREEN}🚀 Memulai distribusi ke ${targetWallets.length} dompet...${RESET}`);

    let sent = 0;
    let nonce = await provider.getTransactionCount(wallet.address);

    for (const target of targetWallets) {
        try {
            process.stdout.write(` ${GRAY}├─ Mengirim ke ${target.name} [${target.address.substring(0,6)}...]... ${RESET}`);
            // 🔥 Eksekusi natural!
            const tx = await wallet.sendTransaction({
                to: target.address,
                value: valPerWallet,
                nonce: nonce
            });
            nonce++; 
            console.log(`${GREEN}SUKSES (${tx.hash})${RESET}`);
            sent++;
        } catch(e) {
            console.log(`${RED}GAGAL: ${e.shortMessage || e.message}${RESET}`);
        }
    }

    console.log(`\n ${GREEN}✅ Selesai membagikan ${sym} ke ${sent} dompet!${RESET}`);
    await pause();
}

async function executeSweep() {
    clear(); title();
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";
    console.log(` ${CYAN}[ SMART SWEEP (Tarik Dana / Token Massal) ]${RESET}\n`);
    const db = loadDB();
    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj || db.wallets.length < 2) { console.log(` ${RED}❌ Butuh Master Wallet & Minimal 1 Tuyul!${RESET}`); await pause(); return; }
    
    console.log(` ${GRAY}Ketik ${GREEN}1${GRAY} untuk sweep ${sym}, atau Paste ${GREEN}Smart Contract${GRAY} untuk sweep Token.${RESET}`);
    const assetInput = await askQuestion(` ${CYAN}╰─ Pilihan Aset (1 / Kontrak): ${RESET}`);

    let isEth = assetInput.trim() === '1';
    let tokenAddress = null;

    if (!isEth) {
        tokenAddress = assetInput.trim();
        if (tokenAddress.length !== 42) {
            console.log(` ${RED}❌ Kontrak Token tidak valid!${RESET}`); await pause(); return;
        }
    }

    console.log(`\n ${GRAY}Ketik ${GREEN}1${GRAY} untuk tarik dana ke Master (${mWalletObj.address}), atau Paste ${GREEN}Address Manual${GRAY}.${RESET}`);
    const targetInput = await askQuestion(` ${CYAN}╰─ Alamat Penerima (1 / Address): ${RESET}`);
    
    let targetAddress = mWalletObj.address;
    if (targetInput.trim() !== '1') {
        targetAddress = targetInput.trim();
        if (targetAddress.length !== 42) {
            console.log(` ${RED}❌ Alamat Penerima tidak valid!${RESET}`); await pause(); return;
        }
    }

    let minSweepAmountWei = 0n;
    if (!isEth) {
        console.log(`\n ${GRAY}Hindari rugi bensin L2! Abaikan dompet jika tokennya hanya sisa remahan (debu).${RESET}`);
        const minAmountStr = await askQuestion(` ${CYAN}╰─ Minimal Token untuk disapu (Enter = 0 / ketik angka, misal 0.5): ${RESET}`);
        
        let tDec = 18n;
        try {
            const tokenDummy = new ethers.Contract(tokenAddress, ERC20_ABI, provider);
            tDec = await tokenDummy.decimals();
        } catch(e) {}

        const cleanInput = minAmountStr.trim() || "0";
        try { minSweepAmountWei = ethers.parseUnits(cleanInput, tDec); } 
        catch(e) { console.log(` ${RED}❌ Nominal minimum tidak valid!${RESET}`); await pause(); return; }
    }

    // 🔥 PETA TARGET TUYUL DENGAN PAGINATION (HALAMAN NEXT)
    let page = 0;
    const limit = 21; 
    let selectedIndexes = new Set();
    let selectionDone = false;

    while (!selectionDone) {
        console.log(`\n ${CYAN}[ 📓 PILIH DOMPET UNTUK DI-SWEEP (Hal. ${page + 1}) ]${RESET}`);
        let start = page * limit;
        let end = Math.min(start + limit, db.wallets.length);
        
        let rows = 7; 
        for (let r = 0; r < rows; r++) {
            let rowStr = "";
            for (let c = 0; c < 3; c++) {
                let idx = start + (c * rows) + r; 
                if (idx < end) {
                    let w = db.wallets[idx];
                    let shortAddr = w.address.substring(w.address.length - 4);
                    let idxStr = idx.toString().padStart(2, ' ');
                    let maxName = 9;
                    let entryName = w.name.length > maxName ? w.name.substring(0, 7) + ".." : w.name;
                    let paddedName = entryName.padEnd(maxName, ' ');
                    
                    let plainEntry = `  [${idxStr}] ${paddedName} (..${shortAddr})`;
                    if (idx === db.master_index) plainEntry = `👑[${idxStr}] ${paddedName} (..${shortAddr})`;
                    rowStr += plainEntry.padEnd(28, ' ');
                }
            }
            if (rowStr.trim().length > 0) console.log(rowStr);
        }

        if (end < db.wallets.length) {
            console.log(` └─ ${GRAY}*Ketik '${GREEN}next${GRAY}' untuk halaman selanjutnya. (Total: ${db.wallets.length} dompet)${RESET}`);
        }

        const idxInput = await askQuestion(`\n ${CYAN}╰─ Pilih Tuyul yang disapu (Contoh: 1-5, 8 | 'all' | 'next'): ${RESET}`);
        let inputClean = idxInput.trim().toLowerCase();

        if (inputClean === 'next') {
            if (end < db.wallets.length) page++;
            else { page = 0; console.log(` ${YELLOW}⚠️ Ini halaman terakhir. Kembali ke halaman 1...${RESET}`); }
            continue;
        }

        if (inputClean === 'all') {
            for (let i = 0; i < db.wallets.length; i++) {
                if (i !== db.master_index) selectedIndexes.add(i);
            }
            selectionDone = true;
        } else {
            const parts = inputClean.split(',');
            for (let p of parts) {
                p = p.trim();
                if (p.includes('-')) {
                    const [startIdx, endIdx] = p.split('-').map(Number);
                    if (!isNaN(startIdx) && !isNaN(endIdx)) {
                        for (let i = Math.min(startIdx, endIdx); i <= Math.max(startIdx, endIdx); i++) {
                            if (i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
                        }
                    }
                } else {
                    const i = parseInt(p);
                    if (!isNaN(i) && i >= 0 && i < db.wallets.length && i !== db.master_index) selectedIndexes.add(i);
                }
            }
            if (selectedIndexes.size > 0) selectionDone = true;
            else console.log(` ${RED}❌ Input tidak valid!${RESET}`);
        }
    }

    const targetWallets = Array.from(selectedIndexes).map(i => db.wallets[i]);
    if (targetWallets.length === 0) {
        console.log(`\n ${RED}❌ Tidak ada dompet valid yang dipilih!${RESET}`);
        await pause(); return;
    }

    let batchSize = 1;
    if (targetWallets.length > 1) {
        const batchInput = await askQuestion(` ${CYAN}╰─ Eksekusi berapa wallet BERSAMAAN per gelombang? (Max 50): ${RESET}`);
        batchSize = parseInt(batchInput);
        if (isNaN(batchSize) || batchSize < 1) batchSize = 1;
        if (batchSize > 50) batchSize = 50; 
    }

    // 🔥 MULTICALL3 PRE-FETCHER (CEK SEMUA SALDO 1X REQUEST)
    let preloadedBalances = [];
    process.stdout.write(`\n ${GRAY}📡 [Multicall3] Mengintip saldo ${targetWallets.length} dompet dalam 1x Request...${RESET}`);
    try {
        const multicall = new ethers.Contract("0xcA11bde05977b3631167028862bE2a173976CA11", ["function aggregate3(tuple(address target, bool allowFailure, bytes callData)[] calls) view returns (tuple(bool success, bytes returnData)[] returnData)", "function getEthBalance(address) view returns (uint256)"], provider);
        const tokenIface = new ethers.Interface(ERC20_ABI);
        
        let calls = targetWallets.map(w => {
            if (isEth) return { target: "0xcA11bde05977b3631167028862bE2a173976CA11", allowFailure: true, callData: multicall.interface.encodeFunctionData("getEthBalance", [w.address]) };
            return { target: tokenAddress, allowFailure: true, callData: tokenIface.encodeFunctionData("balanceOf", [w.address]) };
        });

        const results = await multicall.aggregate3(calls);
        preloadedBalances = results.map((res) => {
            if (res.success) {
                if (isEth) return multicall.interface.decodeFunctionResult("getEthBalance", res.returnData)[0];
                return tokenIface.decodeFunctionResult("balanceOf", res.returnData)[0];
            }
            return 0n;
        });
        console.log(`\r ${GREEN}✅ [Multicall3] Data saldo terkunci! Siap gasak!             ${RESET}\n`);
    } catch(e) {
        preloadedBalances = targetWallets.map(() => -1n); 
        console.log(`\r ${YELLOW}⚠️ [Multicall3] Gagal. Beralih ke scan manual...           ${RESET}\n`);
    }

    // ==========================================
    // 🧾 1. KALKULASI & FILTER DOMPET KOSONG
    // ==========================================
    let totalAvailableWei = 0n;
    let eligibleWallets = []; // 🔥 Menyimpan dompet yang benar-benar ADA SALDO
    let estGasPerWalletWei = 0n;
    let currentGasPrice = ethers.parseUnits("0.005", "gwei");

    try {
        const feeData = await provider.getFeeData();
        currentGasPrice = feeData.maxFeePerGas || feeData.gasPrice || currentGasPrice;
        let estimatedGas = isEth ? 21000n : 65000n;
        
        const actualCost = estimatedGas * currentGasPrice; 
        const remahanDiWallet = (actualCost * 50n) / 100n;
        estGasPerWalletWei = actualCost + remahanDiWallet;

        for (let i = 0; i < targetWallets.length; i++) {
            const bal = preloadedBalances[i];
            const wData = targetWallets[i];
            
            if (bal !== -1n) {
                if (isEth) {
                    if (bal > estGasPerWalletWei) {
                        totalAvailableWei += (bal - estGasPerWalletWei);
                        eligibleWallets.push({ ...wData, preloadedBal: bal }); 
                    }
                } else {
                    if (bal > 0n && bal >= minSweepAmountWei) {
                        totalAvailableWei += bal;
                        eligibleWallets.push({ ...wData, preloadedBal: bal });
                    }
                }
            } else {
                eligibleWallets.push({ ...wData, preloadedBal: -1n }); // Fallback RPC manual
            }
        }
    } catch (e) {}

    let skippedCount = targetWallets.length - eligibleWallets.length;

    // 🔥 CEGAT INSTAN JIKA SEMUA TUYUL KOSONG (Mencegah Struk Ambigu)
    if (eligibleWallets.length === 0) {
        console.log(`\n ${RED}⚠️ Dibatalkan, dari ${targetWallets.length} dompet, tidak ada satupun yang memiliki saldo.${RESET}`);
        await pause(); return;
    }

    let totalDisplay = preloadedBalances.includes(-1n) ? "?" : (isEth ? ethers.formatEther(totalAvailableWei) : ethers.formatUnits(totalAvailableWei, tDec));
    let gasDisplay = ethers.formatEther(estGasPerWalletWei);
    
    // 🔥 TOTAL GAS UNTUK DOMPET YANG AKTIF SAJA
    let totalGasWei = estGasPerWalletWei * BigInt(eligibleWallets.length);
    let totalGasDisplay = ethers.formatEther(totalGasWei);
    
    let gweiDisplay = parseFloat(ethers.formatUnits(currentGasPrice, "gwei")).toFixed(2);
    let displaySymbol = isEth ? sym : "Token";

    console.log(` ${CYAN}[ 🧾 KALKULASI SWEEP ]${RESET}`);
    console.log(` │`);
    console.log(` ├─ Target Eksekusi : ${YELLOW}${eligibleWallets.length} Dompet Aktif ${GRAY}(Diabaikan: ${skippedCount})${RESET}`);
    console.log(` ├─ Est. Total Aset : ${GREEN}${parseFloat(totalDisplay).toFixed(6)} ${displaySymbol}${RESET}`);
    console.log(` ├─ Est. Gas / Tx   : ${YELLOW}${parseFloat(gasDisplay).toFixed(8)} ${sym} (${gweiDisplay} Gwei)${RESET}`);
    console.log(` ├─ Est. Total Gas  : ${YELLOW}${parseFloat(totalGasDisplay).toFixed(8)} ${sym} (Untuk ${eligibleWallets.length} Tx)${RESET}`);
    console.log(` └─ Mode Pacing     : ${CYAN}Gelombang ${batchSize}x Bersamaan${RESET}\n`);

    const confirm = await askQuestion(` ${GREEN}Lanjutkan Sweep Massal? [ Enter = GAS / 'n' = Batal ] : ${RESET}`);
    if (confirm.trim().toLowerCase() === 'n') {
        console.log(` ${RED}❌ Transaksi dibatalkan.${RESET}`);
        await pause(); return;
    }

    if (skippedCount > 0 && !preloadedBalances.includes(-1n)) {
        console.log(`\n ${GRAY}⏩ Menghapus ${skippedCount} dompet kosong dari antrean...${RESET}`);
    }

    // 🔥 LAPIS 1: PRE-FLIGHT REFUEL (FAST-FIRE NONCE BUMP)
    if (!isEth && eligibleWallets.length > 0) {
        console.log(`\n ${GRAY}⏳ [Pre-Flight] Memeriksa ketersediaan bensin ETH untuk ${eligibleWallets.length} pasukan...${RESET}`);
        let needyWallets = [];
        try {
            const multicall = new ethers.Contract("0xcA11bde05977b3631167028862bE2a173976CA11", ["function aggregate3(tuple(address target, bool allowFailure, bytes callData)[] calls) view returns (tuple(bool success, bytes returnData)[] returnData)", "function getEthBalance(address) view returns (uint256)"], provider);
            let ethCalls = eligibleWallets.map(w => ({ target: "0xcA11bde05977b3631167028862bE2a173976CA11", allowFailure: true, callData: multicall.interface.encodeFunctionData("getEthBalance", [w.address]) }));
            const ethResults = await multicall.aggregate3(ethCalls);
            
            ethResults.forEach((res, idx) => {
                if (res.success) {
                    const ethBal = multicall.interface.decodeFunctionResult("getEthBalance", res.returnData)[0];
                    if (ethBal < estGasPerWalletWei) needyWallets.push({ ...eligibleWallets[idx], deficit: estGasPerWalletWei - ethBal });
                }
            });
        } catch(e) {}

        if (needyWallets.length > 0) {
            const totalDeficit = needyWallets.reduce((acc, w) => acc + w.deficit, 0n);
            console.log(` ${YELLOW}⚠️ Ditemukan ${needyWallets.length} dompet kekurangan bensin! (Butuh: ${parseFloat(ethers.formatEther(totalDeficit)).toFixed(6)} ETH)${RESET}`);
            
            const masterWallet = new ethers.Wallet(mWalletObj.pk, provider);
            const masterBal = await provider.getBalance(masterWallet.address);

            if (masterBal < totalDeficit) {
                console.log(` ${RED}❌ Saldo Master tidak cukup menalangi massal. Beralih ke Lapis 2...${RESET}`);
            } else {
                let proceedRefuel = AUTO_MODE;
                if (!proceedRefuel) {
                    const ans = await askQuestion(` ${CYAN}╰─ Isi bensin massal sekarang? [ Enter = 'y' / 'n' = Tolak ] : ${RESET}`);
                    proceedRefuel = ans.trim().toLowerCase() !== 'n';
                }
                
                if (proceedRefuel) {
                    console.log(` ${GREEN}🚀 Menembakkan bensin ke ${needyWallets.length} dompet serentak...${RESET}`);
                    let masterNonce = await provider.getTransactionCount(masterWallet.address, "pending");
                    let refuelPromises = needyWallets.map(async (nw) => {
                        try {
                            const tx = await masterWallet.sendTransaction({ to: nw.address, value: nw.deficit, nonce: masterNonce++ });
                            return tx.wait(1);
                        } catch(e) { return null; }
                    });
                    await Promise.all(refuelPromises);
                    console.log(` ${GREEN}✅ Pre-Flight Refuel Selesai! Pasukan siap tempur.${RESET}`);
                }
            }
        } else {
            console.log(` ${GREEN}✅ Semua pasukan telah diisi bensin penuh.${RESET}`);
        }
    }

    console.log(`\n ${GRAY}⏳ Memulai eksekusi Sweep untuk ${eligibleWallets.length} dompet aktif...${RESET}\n`);
    let swept = 0;
    
    // ==========================================
    // 🚀 2. SISTEM LOOP BATCH (HANYA DOMPET AKTIF)
    // ==========================================
    for (let i = 0; i < eligibleWallets.length; i += batchSize) {
        const batchChunk = eligibleWallets.slice(i, i + batchSize);
        
        const batchPromises = batchChunk.map(async (walletData) => {
            try {
                const w = new ethers.Wallet(walletData.pk, provider);
                let cachedBal = walletData.preloadedBal; 

                if (isEth) {
                    const bal = cachedBal === -1n ? await provider.getBalance(w.address) : cachedBal;
                    
                    const feeData = await provider.getFeeData();
                    const currentGasPrice = feeData.maxFeePerGas || feeData.gasPrice || ethers.parseUnits("0.005", "gwei");
                    let estimatedGas = 21000n;
                    const actualCost = estimatedGas * currentGasPrice; 
                    const totalCostSafe = actualCost + (actualCost * 50n) / 100n;
                    
                    const displayBal = parseFloat(ethers.formatEther(bal)).toFixed(5);

                    if (bal > totalCostSafe) {
                        console.log(`  ├─ [${walletData.name}] Saldo: ${displayBal} ${sym} ${GRAY}➜ Menyapu...${RESET}`);
                        try {
                            const tx = await w.sendTransaction({ to: targetAddress, value: bal - totalCostSafe });
                            console.log(`  │   └─ ${GREEN}SUKSES (${tx.hash})${RESET}`);
                            return 1;
                        } catch(err) {
                            console.log(`  │   └─ ${YELLOW}TERKIRIM (Cek Explorer)${RESET}`);
                            return 1;
                        }
                    } else {
                        console.log(`  ├─ [${walletData.name}] Saldo: ${displayBal} ${sym} ${GRAY}➜ Lewati (Gas Kurang)${RESET}`);
                        return 0;
                    }
                } else {
                    const tokenContract = new ethers.Contract(tokenAddress, ERC20_ABI, w);
                    const tBal = cachedBal === -1n ? await tokenContract.balanceOf(w.address) : cachedBal;
                    const displayBal = parseFloat(ethers.formatUnits(tBal, tDec)).toFixed(4);

                    if (tBal > 0n && tBal >= minSweepAmountWei) {
                        const feeData = await provider.getFeeData();
                        const currentGasPrice = feeData.maxFeePerGas || feeData.gasPrice || ethers.parseUnits("0.005", "gwei");
                        let estimatedGas = 65000n;
                        const actualCost = estimatedGas * currentGasPrice; 
                        const totalCostSafe = actualCost + (actualCost * 50n) / 100n;

                        let txSuccessFlag = false;
                        while (!txSuccessFlag) {
                            const currentEthBal = await provider.getBalance(w.address);
                            if (currentEthBal < totalCostSafe) {
                                console.log(`  ├─ [${walletData.name}] Saldo: ${displayBal} Token ${YELLOW}➜ Bensin kurang! Memanggil Master...${RESET}`);
                                const isFunded = await handleEmergencyGas(w.address, totalCostSafe);
                                if (!isFunded) {
                                    console.log(`  │   └─ ${GRAY}Lewati (Batal diselamatkan)${RESET}`);
                                    return 0;
                                }
                                continue; 
                            }

                            console.log(`  ├─ [${walletData.name}] Saldo: ${displayBal} Token ${GRAY}➜ Menyapu...${RESET}`);
                            try {
                                const tx = await tokenContract.transfer(targetAddress, tBal);
                                console.log(`  │   └─ ${GREEN}SUKSES (${tx.hash})${RESET}`);
                                txSuccessFlag = true;
                                return 1;
                            } catch(err) {
                                const errMsg = (err.message || "").toLowerCase();
                                if (errMsg.includes("insufficient funds")) {
                                    console.log(`  │   └─ ${RED}❌ Gagal Eksekusi: Gas Kurang!${RESET}`);
                                    const isFunded = await handleEmergencyGas(w.address, totalCostSafe);
                                    if (!isFunded) {
                                        console.log(`  │       └─ ${GRAY}Lewati (Batal diselamatkan)${RESET}`);
                                        return 0;
                                    }
                                } else {
                                    console.log(`  │   └─ ${YELLOW}TERKIRIM (Cek Explorer)${RESET}`);
                                    txSuccessFlag = true;
                                    return 1;
                                }
                            }
                        }
                    } else {
                        console.log(`  ├─ [${walletData.name}] Saldo: ${displayBal} Token ${GRAY}➜ Lewati (Kosong/Debu)${RESET}`);
                        return 0;
                    }
                }
            } catch(e) { 
                console.log(`  ├─ [${walletData.name}] ${RED}❌ GAGAL (${e.shortMessage || "Error"})${RESET}`); 
                return 0;
            }
        });

        const results = await Promise.all(batchPromises);
        swept += results.reduce((acc, val) => acc + val, 0);

        if (i + batchSize < eligibleWallets.length) {
            await new Promise(resolve => setTimeout(resolve, 1500));
        }
    }
    
    console.log(`\n ${GREEN}✅ Selesai menyapu ${swept} dompet!${RESET}`);
    await pause();
}

// =================
// 9. DASHBOARD UTAMA
// =================
async function startDashboard() {
    while(true) {
        clear(); title();
        const db = loadDB();
        const mWallet = getMasterWalletObj();
        const cfg = loadConfig();
        
        let masterBal = "0.0";
        let mAddr = "Belum di-set";
        let liveGweiDisplay = "Memuat...";
        if (mWallet && RPC_CONNECTED) {
            mAddr = mWallet.address;
            try {
                const w = new ethers.Wallet(mWallet.pk, provider);
                masterBal = ethers.formatEther(await provider.getBalance(w.address));
                const rawGasPrice = await provider.send("eth_gasPrice", []);
                const currentGweiWei = BigInt(rawGasPrice);
                const gweiNum = parseFloat(ethers.formatUnits(currentGweiWei, "gwei")).toFixed(5);
                liveGweiDisplay = `${gweiNum} Gwei ${GRAY}${formatGasFiat(currentGweiWei)}${RESET}`;
            } catch(e) {}
        } else if (mWallet) { mAddr = mWallet.address; }

        const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";

        // ✂️ LOGIKA PEMOTONG TULISAN PANJANG (TRUNCATE)
        let displayRpc = CONNECTED_RPC_URL.length > 35 ? CONNECTED_RPC_URL.substring(0, 32) + "..." : CONNECTED_RPC_URL;
        let displayWallet = mAddr.length > 20 ? mAddr.substring(0, 6) + "..." + mAddr.substring(mAddr.length - 4) : mAddr;

        console.log(` ${GREEN}[ STATUS CONFIGURATION ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ Mode RPC      : ${cfg.active_mode === 'premium' ? CYAN + "⚡ PREMIUM (WSS)" : GREEN + "✅ GRATISAN (HTTP)"}${RESET}`);
        if (RPC_CONNECTED) {
            console.log(` ├─ Status        : ${GREEN}✅ ONLINE${RESET}`);
            console.log(` ├─ RPC Dipakai   : ${GREEN}${displayRpc}${RESET}`);
        } else {
            console.log(` ├─ Status        : ${RED}❌ OFFLINE${RESET}`);
            console.log(` ├─ Target RPC    : ${RED}${displayRpc}${RESET}`);
        }
        console.log(` ├─ Putus Koneksi : ${RED}${DISCONNECT_COUNT}x${RESET}`);
        console.log(` ├─ Master Wallet : ${displayWallet}`);
        console.log(` ├─ Saldo ${sym.padEnd(8, ' ')}: ${GREEN}${masterBal} ${sym}${RESET}`);
        console.log(` ├─ Live Gwei     : ${CYAN}${liveGweiDisplay}`);
        console.log(` └─ Database      : ${db.wallets.length} Dompet Tersimpan`);

        console.log(`\n ${CYAN}[ CONTROL PANEL ]${RESET}`);
        console.log(` │`);
        console.log(` ├─ [1] 🎯 Trade Menu`);
        console.log(` │`);
        console.log(` ├─ [2] 💸 Transfer ${sym} / Token`);
        console.log(` ├─ [3] 🚀 Mass Disperse`);
        console.log(` ├─ [4] 🧹 Smart Sweep`);
        console.log(` ├─ [5] ⛽ Distribusi Gas`);
        console.log(` │`);
        console.log(` ├─ [6] 🔐 Kelola Dompet`);
        console.log(` ├─ [7] 🌐 Kelola Node RPC`);
        console.log(` ├─ [8] 🔗 WalletConnect (Scan / Tempel URI)`);
        console.log(` ├─ [9] 👻 Omni Ghost (Browser Bypass / Spoofing)`);
        console.log(` │`);
        console.log(` └─ [0] Keluar\n`);
        
        const act = await askQuestion(` ╰─ Pilih menu: `);
        
        try {
            if (act === '1') await executeSniperMenu();
            else if (act === '2') await executeTransfer();
            else if (act === '3') await executeDisperseETH(false);
            else if (act === '4') await executeSweep();
            else if (act === '5') await executeDisperseETH(true);
            else if (act === '6') await executeWalletManager();
            else if (act === '7') await executeRpcManager();
            else if (act === '8') {
                const wcUri = await askQuestion(`\n ${CYAN}╰─ Masukkan/Paste URI WalletConnect (Mulai dari wc:...) : ${RESET}`);
                if (wcUri.trim().startsWith('wc:')) await startWalletConnectDaemon(wcUri.trim());
                else { console.log(` ${RED}❌ URI tidak valid!${RESET}`); await pause(); }
            }
            else if (act === '9') {
                console.log(`\n ${CYAN}[ MODE OMNI GHOST (SPOOFING IDENTITAS) ]${RESET}`);
                console.log(` ├─ [1] 🌐 Buka Browser & Injeksi (Default)`);
                console.log(` └─ [2] 🤖 Hanya jalankan Daemon API (Background)`);
                const gMode = await askQuestion(` ╰─ Pilih mode: `);
                
                let port = 3333; // Port Default
                
                console.log(`\n ${CYAN}[ PILIH TOPENG IDENTITAS ]${RESET}`);
                console.log(` ${GRAY}0. 🎭 OMNI MODE (Suntik Semua 8 Dompet Sekaligus!)${RESET}`);
                console.log(` 1. 🦊 MetaMask         5. 🔵 Coinbase Wallet`);
                console.log(` 2. 🛡️ Trust Wallet     6. ⬛ OKX Wallet`);
                console.log(` 3. 🦄 Uniswap Wallet   7. 🐰 Rabby Wallet`);
                console.log(` 4. 🌈 Rainbow          8. 🟢 Bitget Wallet`);
                const idChoice = await askQuestion(` ╰─ Masukkan angka pilihan [0-8] (Default 0): `);
                
                // Ubah variabel global identitas sesuai pilihan user
                const idMap = {
                    '1': 'metamask', '2': 'trustwallet', '3': 'uniswap', '4': 'rainbow',
                    '5': 'coinbase', '6': 'okx', '7': 'rabby', '8': 'bitget'
                };
                GHOST_IDENTITY = idMap[idChoice.trim()] || 'ghostwallet';
                
                if (gMode === '2') {
                    const customPort = await askQuestion(`\n ${CYAN}╰─ Masukkan Port (Default 3333): ${RESET}`);
                    if (customPort.trim()) port = parseInt(customPort);
                    await startGhostDaemon(port);
                    console.log(`\n ${YELLOW}⚠️ Mode Daemon API aktif. Menunggu koneksi dari script luar... (CTRL+C untuk keluar)${RESET}\n`);
                    await new Promise(() => {}); // Tahan terminal
                } else {
                    const targetUrl = await askQuestion(`\n ${CYAN}╰─ Masukkan URL dApp Target (contoh: https://bibo.trade) : ${RESET}`);
                    if (targetUrl.trim().startsWith('http')) {
                        const daemon = await startGhostDaemon(port);
                        if (daemon) await startGhostBrowser(targetUrl, daemon.port, daemon.walletAddress);
                    } else { console.log(` ${RED}❌ URL wajib menggunakan http:// atau https://${RESET}`); await pause(); }
                }
            }
            else if (act === '0') process.exit(0);
        } catch (err) {
            console.log(`\n ${RED}❌ Sistem Terhenti Akibat Fatal Error: ${err.message || err}${RESET}`);
            await pause();
        }
    }
}

// =================
// 9. WALLETCONNECT DAEMON (DAPP LISTENER)
// =================

// 🔥 LAKBAN TERMINAL: Cegat & bungkam paksa log JSON berisik dari mesin WalletConnect
const origStdout = process.stdout.write.bind(process.stdout);
process.stdout.write = (chunk, encoding, callback) => {
    if (typeof chunk === 'string' && chunk.includes('Record was recently deleted')) return true; 
    return origStdout(chunk, encoding, callback);
};

// ========================================================
// 👻 IN-MEMORY BRIDGE (PLAYWRIGHT DIRECT CONNECTION)
// ========================================================
async function handleInMemoryRequest(reqData, workerWalletObj) {
    const { method, params } = reqData;
    
    // Gunakan wallet spesifik worker jika ada, jika tidak pakai master
    const targetWalletObj = workerWalletObj || getMasterWalletObj();
    if (!targetWalletObj) return { error: "Wallet belum diset!" };
    const wallet = new ethers.Wallet(targetWalletObj.pk, provider);

    if (method !== 'eth_call' && method !== 'eth_estimateGas' && method !== 'eth_getBlockByNumber' && method !== 'eth_chainId' && method !== 'net_version' && method !== 'wallet_getCapabilities' && method !== 'eth_accounts') {
        console.log(` 📡 [In-Memory] Memanggil: ${method} dari ${targetWalletObj.name}`);
    }

    try {
        let result;
        if (method === 'eth_requestAccounts' || method === 'eth_accounts') {
            result = [wallet.address];
        }
        else if (method === 'wallet_requestPermissions') result = [{ caveats: [], parentCapability: "eth_accounts" }];
        else if (method === 'eth_chainId') result = "0x" + CHAINS[ACTIVE_CHAIN].id.toString(16);
        else if (method === 'net_version') result = CHAINS[ACTIVE_CHAIN].id.toString();
        else if (method === 'wallet_getCapabilities') result = {};
        
        // 🔄 SWITCH NETWORK
        else if (method === 'wallet_switchEthereumChain' || method === 'wallet_addEthereumChain') {
            const targetChainIdHex = params[0]?.chainId || "0x0";
            const targetChainIdDec = parseInt(targetChainIdHex, 16);
            let newChainKey = null;
            for (const [key, chainData] of Object.entries(CHAINS)) {
                if (chainData.id === targetChainIdDec) { newChainKey = key; break; }
            }
            if (newChainKey) {
                console.log(` ${GRAY}⏳ [In-Memory] Pindah jaringan ke: ${GREEN}${CHAINS[newChainKey].name}${RESET}`);
                applyNetwork(newChainKey); connectRPC();
                result = null;
            } else {
                throw new Error("Unrecognized chain ID");
            }
        }

        // 💸 EKSEKUSI TRANSAKSI LENGKAP DENGAN ANTI-DRAINER & BENSIN
        else if (method === 'eth_sendTransaction') {
            const txData = params[0];
            let activeCfg = loadConfig();
            let gasConfig = await getGasConfig(BigInt(activeCfg.gwei.sell || 100));

            let txValue = txData.value ? BigInt(txData.value) : 0n;
            let gasLimit = txData.gas ? BigInt(txData.gas) : (txData.gasLimit ? BigInt(txData.gasLimit) : 650000n);

            console.log(` ${GRAY}⏳ [In-Memory] Eksekusi Transaksi Web3...${RESET}`);
            
            let txSuccessFlag = false;
            let tx;
            while (!txSuccessFlag) {
                try {
                    const nonce = await provider.getTransactionCount(wallet.address, "pending");
                    const txPayload = {
                        to: txData.to, data: txData.data || "0x", value: txValue, gasLimit: gasLimit,
                        maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                    };
                    tx = await wallet.sendTransaction(txPayload);
                    console.log(` ${GREEN}✅ [In-Memory] Tx Terkirim! Hash: ${tx.hash}${RESET}`);
                    txSuccessFlag = true;
                } catch (err) {
                    const errMsg = (err.message || "").toLowerCase();
                    if (errMsg.includes("insufficient funds")) {
                        console.log(` ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                        const isFunded = await handleEmergencyGas(wallet.address, gasLimit * gasConfig.maxFeePerGasConfig);
                        if (!isFunded) throw err;
                    } else throw err;
                }
            }
            
            // Background proses untuk nge-print mutasi masuk/keluar
            tx.wait(1).then(() => console.log(` ${CYAN}📊 [In-Memory] Resi on-chain dikonfirmasi: ${tx.hash}${RESET}`)).catch(()=>{});
            return { result: tx.hash };
        }

        // 🛡️ OTO-TANDA TANGAN (AUTO-SIGN)
        else if (method === 'personal_sign') {
            const message = ethers.toUtf8String(params[0]);
            console.log(` ${GREEN}✅ [In-Memory] Auto-Sign Message Selesai.${RESET}`);
            result = await wallet.signMessage(message);
        } 
        else if (method === 'eth_signTypedData_v4') {
            const payload = JSON.parse(params[1]);
            if (payload.types && payload.types.EIP712Domain) delete payload.types.EIP712Domain;
            console.log(` ${GREEN}✅ [In-Memory] Auto-Sign EIP-712 Selesai.${RESET}`);
            result = await wallet.signTypedData(payload.domain, payload.types, payload.message);
        } 
        else {
            result = await provider.send(method, params || []);
        }
        
        return { result };

    } catch (error) {
        console.log(` ❌ [In-Memory] Error: ${error.message}`);
        return { error: error.message };
    }
}

// ========================================================
// 👻 OMNI GHOST DAEMON (API SERVER LOKAL)
// ========================================================
async function startGhostDaemon(port = 3333) {
    const http = require('http');
    clear(); title();
    console.log(` ${CYAN}[ OMNI GHOST DAEMON AKTIF ]${RESET}\n`);

    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj) {
        console.log(` ${RED}❌ Master Wallet belum diset! Silakan ke menu Kelola Dompet terlebih dahulu.${RESET}`);
        await pause(); return null;
    }
    const wallet = new ethers.Wallet(mWalletObj.pk, provider);
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";

    console.log(`\n ${CYAN}⚠️ PERSIAPAN DAEMON LOKAL!${RESET}`);
    console.log(` ├─ Port API  : ${GREEN}http://127.0.0.1:${port}${RESET}`);
    console.log(` ├─ Jaringan  : ${GREEN}${CHAINS[ACTIVE_CHAIN].name} (ID: ${CHAINS[ACTIVE_CHAIN].id})${RESET}`);
    console.log(` └─ Wallet    : ${GRAY}${wallet.address}${RESET}\n`);
    
    let isAccepted = false;
    if (AUTO_MODE) {
        console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Mengizinkan daemon berjalan otomatis...${RESET}`);
        isAccepted = true;
    } else {
        const accept = await askQuestion(` ${YELLOW}Izinkan Daemon berjalan dan mengontrol Master Wallet? [ y / n ] : ${RESET}`);
        isAccepted = accept.toLowerCase() === 'y';
    }

    if (!isAccepted) {
        console.log(` ${RED}❌ Ditolak. Membatalkan Ghost Daemon...${RESET}`);
        return null;
    }

    // 🌐 HTTP SERVER: Menerima request JSON-RPC dari mana saja (Script Python/Browser)
    const server = http.createServer(async (req, res) => {
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
        if (req.method === 'OPTIONS') { res.writeHead(200); return res.end(); }

        if (req.method === 'POST') {
            let body = '';
            req.on('data', chunk => body += chunk.toString());
            req.on('end', async () => {
                try {
                    const reqData = JSON.parse(body);
                    const { method, params } = reqData;
                    
                    // 🔥 SILENT BYPASS: Sembunyikan method spam murni dari log terminal
                    if (method !== 'eth_call' && method !== 'eth_getBlockByNumber' && method !== 'eth_chainId' && method !== 'net_version' && method !== 'wallet_getCapabilities' && method !== 'eth_accounts') {
                        console.log(` 📡 Klien API Memanggil : ${method}`);
                    }

                    let result;
                    if (method === 'eth_requestAccounts' || method === 'eth_accounts') {
                        if (method === 'eth_requestAccounts') {
                            console.log(`\n ${GREEN}🔗 DApp Terhubung! Wallet: ${wallet.address} | Jaringan: ${CHAINS[ACTIVE_CHAIN].name}${RESET}`);
                        }
                        result = [wallet.address];
                    }
                    else if (method === 'wallet_requestPermissions') result = [{ caveats: [], parentCapability: "eth_accounts" }];
                    else if (method === 'eth_chainId') result = "0x" + CHAINS[ACTIVE_CHAIN].id.toString(16);
                    else if (method === 'net_version') result = CHAINS[ACTIVE_CHAIN].id.toString();
                    else if (method === 'wallet_getCapabilities') result = {}; // 🔥 FIX: Bypass mematikan bawelnya Uniswap!
                    
                    // 🔄 SWITCH NETWORK
                    else if (method === 'wallet_switchEthereumChain' || method === 'wallet_addEthereumChain') {
                        console.log(` │`);
                        console.log(` ├─ Jenis     : ${YELLOW}Switch / Add Network (${method})${RESET}`);
                        const targetChainIdHex = params[0]?.chainId || "0x0";
                        const targetChainIdDec = parseInt(targetChainIdHex, 16);
                        console.log(` └─ Chain ID  : ${GRAY}${targetChainIdHex} (${targetChainIdDec})${RESET}\n`);
                        
                        let newChainKey = null;
                        for (const [key, chainData] of Object.entries(CHAINS)) {
                            if (chainData.id === targetChainIdDec) { newChainKey = key; break; }
                        }

                        if (!newChainKey) {
                            try {
                                const fetchRes = await fetch("https://chainid.network/chains.json");
                                const allChains = await fetchRes.json();
                                const foundChain = allChains.find(c => c.chainId === targetChainIdDec);
                                if (foundChain && foundChain.rpc.length > 0) {
                                    newChainKey = (foundChain.shortName || `chain_${targetChainIdDec}`).toLowerCase();
                                    const safeRpcs = foundChain.rpc.filter(r => r.startsWith('http') && !r.includes('${'));
                                    CHAINS[newChainKey] = {
                                        name: foundChain.name, id: foundChain.chainId, symbol: foundChain.nativeCurrency?.symbol || "ETH",
                                        cgId: "ethereum", weth: ethers.ZeroAddress, usdc: ethers.ZeroAddress, defaultRpc: safeRpcs,
                                        v2Router: ethers.ZeroAddress, v2Factory: ethers.ZeroAddress, v3Router: ethers.ZeroAddress, v3Factory: ethers.ZeroAddress, v3Quoter: ethers.ZeroAddress,
                                        hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress
                                    };
                                    saveChainsDB(); 
                                }
                            } catch (e) {}
                        }

                        if (newChainKey) {
                            console.log(` ${GRAY}⏳ Mengalihkan jaringan internal ke: ${GREEN}${CHAINS[newChainKey].name}${RESET}`);
                            applyNetwork(newChainKey); connectRPC();
                            result = null;
                        } else {
                            throw new Error("Unrecognized chain ID");
                        }
                    }

                    // 💸 EKSEKUSI TRANSAKSI
                    else if (method === 'eth_sendTransaction') {
                        console.log(`\n ${CYAN}[ 🧾 STRUK KASIR DAPP (GHOST METAMASK) ]${RESET}`);
                        const txData = params[0];
                        const valueEth = txData.value ? ethers.formatEther(txData.value) : "0.0";
                        const targetAddress = txData.to || "Kontrak Baru";
                        
                        const activeWallet = new ethers.Wallet(mWalletObj.pk, provider);
                        let activeCfg = loadConfig();
                        let gasConfig = await getGasConfig(BigInt(activeCfg.gwei.sell || 100));

                        let txValue = 0n;
                        if (txData.value) txValue = BigInt(txData.value);

                        let gasLimit;
                        if (txData.gas) gasLimit = BigInt(txData.gas);
                        else if (txData.gasLimit) gasLimit = BigInt(txData.gasLimit);
                        else {
                            try {
                                gasLimit = await provider.estimateGas({ to: txData.to, data: txData.data || "0x", value: txValue, from: activeWallet.address });
                                gasLimit = (gasLimit * 120n) / 100n; 
                            } catch(e) { gasLimit = 650000n; }
                        }

                        let gasCostWei = gasLimit * gasConfig.maxFeePerGasConfig;
                        let totalCostEth = parseFloat(ethers.formatEther(gasCostWei));
                        let totalCostUsd = totalCostEth * ethPriceUsd;
                        let isValueSending = txValue > 0n;
                        let isExpensive = totalCostUsd >= 1;

                        // 🧠 KAMUS WEB3 (Method ID Translator)
                        const METHOD_DICTIONARY = {
                            "0x095ea7b3": "🔓 APPROVE TOKEN (Memberi Akses Kontrak)",
                            "0xa9059cbb": "💸 TRANSFER TOKEN (Mengirim Aset)",
                            "0x23b872dd": "💸 TRANSFER FROM (Menarik Aset)",
                            "0x38ed1739": "🔄 SWAP TOKENS FOR TOKENS",
                            "0x18cbafe5": "🔄 SWAP TOKENS FOR ETH",
                            "0x7ff36ab5": "🔄 SWAP ETH FOR TOKENS",
                            "0x791ac947": "🔄 SWAP TOKENS FOR ETH (With Fee)",
                            "0xb6f9de95": "🔄 SWAP ETH FOR TOKENS (With Fee)",
                            "0x5ae401dc": "🎯 MULTICALL (Kumpulan Eksekusi)",
                            "0x3593564c": "⚡ EXECUTE (Universal Router)"
                        };

                        let methodId = (txData.data && txData.data.length >= 10) ? txData.data.substring(0, 10) : "0x";
                        let actionName = METHOD_DICTIONARY[methodId.toLowerCase()] || `⚙️ CUSTOM EXECUTION (Method: ${methodId})`;

                        // 🔥 Auto-Dekode parameter khusus untuk Approve / Transfer
                        let decodedInfo = "";
                        if ((methodId.toLowerCase() === "0x095ea7b3" || methodId.toLowerCase() === "0xa9059cbb") && txData.data.length >= 138) {
                            try {
                                const spender = "0x" + txData.data.substring(34, 74);
                                const amountHex = "0x" + txData.data.substring(74, 138);
                                const rawAmount = BigInt(amountHex);
                                
                                if (rawAmount > (2n**200n)) {
                                    decodedInfo = `\n ├─ Tembusan  : ${GRAY}${spender}${RESET}\n ├─ Jumlah    : ${RED}UNLIMITED (Tak Terbatas)${RESET}`;
                                } else {
                                    const ERC20_MINI_ABI = ["function symbol() view returns (string)", "function decimals() view returns (uint8)"];
                                    const tContract = new ethers.Contract(targetAddress, ERC20_MINI_ABI, provider);
                                    const [tSymbol, tDec] = await Promise.all([tContract.symbol(), tContract.decimals()]);
                                    const parsedAmount = parseFloat(ethers.formatUnits(rawAmount, tDec)).toFixed(4);
                                    
                                    // 🔥 FIX: Spasi diluruskan persis 10 karakter sebelum titik dua
                                    decodedInfo = `\n ├─ Tembusan  : ${GRAY}${spender}${RESET}\n ├─ Jumlah    : ${RED}-${parsedAmount} ${tSymbol}${RESET}`;
                                }
                            } catch(e) {
                                decodedInfo = `\n ├─ Tembusan  : 0x${txData.data.substring(34, 74)}\n ├─ Jumlah    : ${YELLOW}Gagal membaca detail token dari RPC${RESET}`;
                            }
                        }

                        console.log(` │`);
                        console.log(` ├─ Jenis     : ${YELLOW}Smart Contract Execution (eth_sendTransaction)${RESET}`);
                        console.log(` ├─ Aksi      : ${CYAN}${BOLD}${actionName}${RESET}${decodedInfo}`);
                        console.log(` ├─ Target    : ${targetAddress}`);
                        console.log(` ├─ Value     : ${GREEN}${valueEth} ${sym}${RESET}`);
                        console.log(` ├─ Est. Gas  : ${YELLOW}$${totalCostUsd.toFixed(4)} (${parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5)} Gwei)${RESET}`);
                        console.log(` └─ Data Hex  : ${GRAY}${txData.data.substring(0, 40)}...${RESET}\n`);
                        
                        let proceedTx = false;
                        if (AUTO_MODE) {
                            console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Mengeksekusi transaksi secara otomatis...${RESET}`);
                            proceedTx = true;
                        } else {
                            const confirm = await askQuestion(` ${YELLOW}Eksekusi Transaksi Ini? [ Enter = GAS 🚀 / 'n' = Tolak ] : ${RESET}`);
                            proceedTx = confirm.trim().toLowerCase() !== 'n';
                        }
                        
                        if (proceedTx) {
                            console.log(` ${GRAY}⏳ Mengeksekusi transaksi API...${RESET}`);
                            
                            let txSuccessFlag = false;
                            let tx;
                            while (!txSuccessFlag) {
                                try {
                                    const nonce = await provider.getTransactionCount(activeWallet.address, "pending");
                                    const txPayload = {
                                        to: txData.to, data: txData.data || "0x", value: txValue, gasLimit: gasLimit,
                                        maxFeePerGas: gasConfig.maxFeePerGasConfig, maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig, nonce: nonce
                                    }; // 🔥 FIX: Hapus type: 0 biar Ethers.js otomatis milih EIP-1559 murni!

                                    tx = await activeWallet.sendTransaction(txPayload);
                                    console.log(` ${GREEN}✅ Terkirim! Hash: ${tx.hash}${RESET}`);
                                    txSuccessFlag = true;
                                } catch (err) {
                                    const errMsg = (err.message || "").toLowerCase();
                                    if (errMsg.includes("insufficient funds")) {
                                        console.log(` ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                                        const requiredGasWei = gasLimit * gasConfig.maxFeePerGasConfig;
                                        const isFunded = await handleEmergencyGas(activeWallet.address, requiredGasWei);
                                        if (!isFunded) {
                                            throw err; // Dibatalkan, lempar error ke frontend dApp
                                        }
                                    } else {
                                        throw err;
                                    }
                                }
                            }
                            
                            // Kirim resi sukses ke pemanggil API secepat mungkin biar browser nggak macet
                            res.writeHead(200, { 'Content-Type': 'application/json' });
                            res.end(JSON.stringify({ result: tx.hash }));

                            // 2. Fitur Bedah Resi (Menunggu konfirmasi blok untuk melihat token In/Out)
                            process.stdout.write(` ${GRAY}⏳ Membedah mutasi token di blockchain...${RESET}`);
                            try {
                                const receipt = await tx.wait(1);
                                let mutations = [];
                                const ERC20_MINI_ABI = ["function symbol() view returns (string)", "function decimals() view returns (uint8)"];
                                const TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
                                const myAddr = activeWallet.address.toLowerCase();

                                for (let log of receipt.logs) {
                                    if (log.topics[0] === TRANSFER_TOPIC && log.topics.length === 3) {
                                        const fromAddr = ("0x" + log.topics[1].substring(26)).toLowerCase();
                                        const toAddr = ("0x" + log.topics[2].substring(26)).toLowerCase();
                                        
                                        if (fromAddr === myAddr || toAddr === myAddr) {
                                            const tContract = new ethers.Contract(log.address, ERC20_MINI_ABI, provider);
                                            let sym = "TOKEN", dec = 18n;
                                            try {
                                                [sym, dec] = await Promise.all([tContract.symbol(), tContract.decimals()]);
                                            } catch(e) {}
                                            
                                            const rawAmount = BigInt(log.data);
                                            const amount = parseFloat(ethers.formatUnits(rawAmount, dec)).toFixed(4);
                                            
                                            if (amount > 0) {
                                                if (fromAddr === myAddr) mutations.push(`    ${RED}📉 KELUAR : -${amount} ${sym}${RESET}`);
                                                if (toAddr === myAddr) mutations.push(`    ${GREEN}📈 MASUK  : +${amount} ${sym}${RESET}`);
                                            }
                                        }
                                    }
                                }

                                process.stdout.write(`\r\x1b[2K`); // Hapus tulisan loading
                                if (mutations.length > 0) {
                                    console.log(` ${CYAN}[ 📊 HASIL MUTASI ASET ]${RESET}`);
                                    mutations.forEach(m => console.log(m));
                                    console.log("");
                                } else {
                                    console.log(` ${GRAY} ├─ Transaksi sukses (Tidak ada mutasi token / Hanya Approve).${RESET}\n`);
                                }

                            } catch (err) {
                                process.stdout.write(`\r\x1b[2K`);
                                console.log(` ${YELLOW}⚠️ Transaksi sukses, tapi gagal membaca resi (RPC Delay).${RESET}\n`);
                            }
                            return; // Selesai
                        } else {
                            console.log(` ${RED}❌ Transaksi ditolak.${RESET}`);
                            throw new Error("User rejected the transaction");
                        }
                    }

                    // 🛡️ SIGNATURES
                    else if (method === 'personal_sign') {
                        const message = ethers.toUtf8String(params[0]);
                        console.log(`\n ${CYAN}⚠️ Permintaan Sign Message: ${GRAY}${message}${RESET}\n`);
                        let proceedSign = AUTO_MODE;
                        if (!proceedSign) { const confirm = await askQuestion(` ${YELLOW}Tanda Tangani? [ Enter = YES / 'n' = Tolak ] : ${RESET}`); proceedSign = confirm.trim().toLowerCase() !== 'n'; }
                        if (proceedSign) result = await wallet.signMessage(message);
                        else throw new Error("User rejected");
                    } 
                    else if (method === 'eth_signTypedData_v4') {
                        console.log(`\n ${CYAN}⚠️ Permintaan Sign Typed Data (EIP-712)${RESET}\n`);
                        let proceedSignTyped = AUTO_MODE;
                        if (!proceedSignTyped) { const confirm = await askQuestion(` ${YELLOW}Tanda Tangani? [ Enter = YES / 'n' = Tolak ] : ${RESET}`); proceedSignTyped = confirm.trim().toLowerCase() !== 'n'; }
                        if (proceedSignTyped) {
                            const payload = JSON.parse(params[1]);
                            if (payload.types && payload.types.EIP712Domain) delete payload.types.EIP712Domain;
                            result = await wallet.signTypedData(payload.domain, payload.types, payload.message);
                        } else throw new Error("User rejected");
                    } 
                    else {
                        result = await provider.send(method, params || []);
                    }
                    
                    res.writeHead(200, { 'Content-Type': 'application/json' });
                    res.end(JSON.stringify({ result }));

                } catch (error) {
                    console.log(` ❌ Ditolak/Error: ${error.message}`);
                    res.writeHead(200, { 'Content-Type': 'application/json' });
                    res.end(JSON.stringify({ error: error.message }));
                }
            });
        }
    });

    server.listen(port, () => {
        console.log(` ${GREEN}✅ Daemon Ghost berjalan di port ${port}!${RESET}`);
    });

    return { port, walletAddress: wallet.address };
}

// ========================================================
// 🌐 GHOST BROWSER (KLIEN DARI DAEMON LOKAL)
// ========================================================
async function startGhostBrowser(targetUrl, daemonPort, walletAddress, engine = "patchright") {
    let chromium;
    let launchOptions = { headless: false };

    if (engine === "cloakbrowser") {
        console.log(` ${GRAY}⏳ Membangkitkan CloakBrowser v146 Lokal (Offline Bypass)...${RESET}`);
        try {
            // Gunakan playwright-core murni agar tidak terinterupsi wrapper cloakbrowser
            chromium = require('playwright').chromium;
        } catch(e) {
            try { chromium = require('patchright').chromium; } catch(err) {}
        }
        
        // Path lokal v146 arm64 tanpa network check
        const cloakBinaryPath = "/root/.cloakbrowser/chromium-146.0.7680.177.4/chrome";
        
        launchOptions = {
            headless: false,
            executablePath: cloakBinaryPath,
            args: [
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--mute-audio",
                "--fingerprint=14607680",
                "--fingerprint-platform=windows"
            ]
        };
    } else {
        console.log(` ${GRAY}⏳ Membangkitkan Patchright Browser...${RESET}`);
        try { chromium = require('patchright').chromium; } 
        catch(e) { console.log(`\n ${RED}❌ Patchright belum di-install!${RESET}`); return; }
    }

    const browser = await chromium.launch(launchOptions);
    const context = await browser.newContext();

    // 🌉 REVERSE PROXY: Bypassing CSP Website dengan mengelabui rute ke Localhost!
    await context.route('**/__ghost_bridge__', async (route) => {
        const reqData = route.request().postData();
        try {
            // Tembak dari dalam bot ke Daemon lokal
            const fetchRes = await fetch(`http://127.0.0.1:${daemonPort}`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: reqData
            });
            const data = await fetchRes.json();
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(data) });
        } catch(e) {
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ error: e.message }) });
        }
    });

    // 💉 INJEKSI KLIEN (DINAS KEPENDUDUKAN WEB3)
    await context.addInitScript((setupData) => {
        const callBridge = async (args) => {
            const res = await fetch('/__ghost_bridge__', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(args) });
            const data = await res.json();
            if (data.error) throw new Error(data.error); return data.result;
        };

        const fakeProvider = {
            isMetaMask: true, networkVersion: setupData.dec, chainId: setupData.hex, selectedAddress: setupData.address,
            request: async (args) => { return await callBridge(args); },
            send: async (method, params) => { return await callBridge({ method, params: params || [] }); },
            sendAsync: async (payload, cb) => { callBridge({ method: payload.method, params: payload.params || [] }).then(res => cb(null, { id: payload.id, jsonrpc: "2.0", result: res })).catch(err => cb(err)); },
            enable: async () => { return await callBridge({ method: 'eth_requestAccounts' }); },
            on: () => {}, removeListener: () => {}, removeAllListeners: () => {}
        };
        window.ethereum = fakeProvider; window.ethereum.providers = [fakeProvider];

        // 🔥 DATABASE THE ELITE 8 WALLETS
        const KTP_DB = [
            { id: "metamask", uuid: "11111111-90fa-40da-8d1e-089b3f3edcc2", name: "MetaMask", rdns: "io.metamask.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iI0U0NzYxQiIgZD0iTTIyLjcgMTEuN2wtMi0uNCAxLjEtMy4xYy4xLS40LS4xLS43LS41LS44bS0uMi4xIi8+PC9zdmc+" },
            { id: "trustwallet", uuid: "22222222-40da-40da-8d1e-089b3f3edcc2", name: "Trust Wallet", rdns: "com.trustwallet.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iTTEyIDJMMiA3bDExLjIgMTQuM2EuOC44IDAgMCAwIDEuNiAwTDIyIDdsLTEwLTV6IiBmaWxsPSIjMDUwMEZGIi8+PC9zdmc+" },
            { id: "uniswap", uuid: "33333333-40da-40da-8d1e-089b3f3edcc2", name: "Uniswap Wallet", rdns: "org.uniswap.app", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iTTEyIDIybC04LTgtMi0ydjh6IiBmaWxsPSIjRkYwMDdBIi8+PC9zdmc+" },
            { id: "rainbow", uuid: "44444444-40da-40da-8d1e-089b3f3edcc2", name: "Rainbow", rdns: "me.rainbow", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTEiIGZpbGw9IiMwMDFGNTciLz48L3N2Zz4=" },
            { id: "coinbase", uuid: "55555555-40da-40da-8d1e-089b3f3edcc2", name: "Coinbase Wallet", rdns: "com.coinbase.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTEiIGZpbGw9IiMwMDUyRkYiLz48L3N2Zz4=" },
            { id: "okx", uuid: "66666666-40da-40da-8d1e-089b3f3edcc2", name: "OKX Wallet", rdns: "com.okex.wallet", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzAwMDAwMCIgZD0iTTAgMGgyNHYyNEgweiIvPjwvc3ZnPg==" },
            { id: "rabby", uuid: "77777777-40da-40da-8d1e-089b3f3edcc2", name: "Rabby Wallet", rdns: "io.rabby", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzg2OTdGRiIgZD0iTTEyIDJMMiA3bDExLjIgMTQuM2EuOC44IDAgMCAwIDEuNiAwTDIyIDdsLTEwLTV6Ii8+PC9zdmc+" },
            { id: "bitget", uuid: "88888888-40da-40da-8d1e-089b3f3edcc2", name: "Bitget Wallet", rdns: "com.bitget.web3", icon: "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iIzAwRjZDNCIgZD0iTTEyIDBMMCAxMmwxMiAxMiAxMi0xMnoiLz48L3N2Zz4=" }
        ];

        const announce = () => {
            // 🔥 JURUS KAGEBUNSHIN: Filter KTP sesuai argumen, atau keluarin semuanya!
            let activeMasks = setupData.identity === "ghostwallet" ? KTP_DB : KTP_DB.filter(k => k.id === setupData.identity);
            if (activeMasks.length === 0) activeMasks = KTP_DB; // Fallback jika typo argumen
            
            activeMasks.forEach(ktp => {
                window.dispatchEvent(new CustomEvent("eip6963:announceProvider", { 
                    detail: { info: { uuid: ktp.uuid, name: ktp.name, icon: ktp.icon, rdns: ktp.rdns }, provider: fakeProvider } 
                }));
            });
        };
        announce(); window.addEventListener("eip6963:requestProvider", announce);
        
        // 🔥 UPDATE OTOMATIS: Dengar perubahan Chain dari Daemon
        window.addEventListener('chainChanged', (e) => {
            window.ethereum.chainId = e.detail;
            window.ethereum.networkVersion = parseInt(e.detail, 16).toString();
        });
    }, { dec: CHAINS[ACTIVE_CHAIN].id.toString(), hex: "0x"+CHAINS[ACTIVE_CHAIN].id.toString(16), address: walletAddress, identity: GHOST_IDENTITY });

    console.log(`\n ${CYAN}🎭 Identitas Ghost : ${YELLOW}[ ${GHOST_IDENTITY.toUpperCase()} MODE ]${RESET}`);
    console.log(` ${GRAY}🌐 Meluncur ke ${targetUrl}...${RESET}`);
    const page = await context.newPage();
    await page.goto(targetUrl);

    console.log(` ${GREEN}✅ Browser Siluman berhasil masuk ke ${targetUrl}!${RESET}`);
    console.log(` ${GRAY}(Silakan klik "Connect Wallet" -> pilih "MetaMask" di website tersebut)${RESET}\n`);
    console.log(` ${YELLOW}⚠️ Mode Daemon aktif. Tekan CTRL+C untuk keluar dari bot jika sudah selesai.${RESET}\n`);
    
    // Auto-update ChainID di Browser kalau Daemon diganti
    setInterval(() => {
        const hexChain = "0x"+CHAINS[ACTIVE_CHAIN].id.toString(16);
        page.evaluate((h) => { window.dispatchEvent(new CustomEvent('chainChanged', { detail: h })); }, hexChain).catch(()=>{});
    }, 2000);

    // 🔥 FIX: Deteksi saat browser ditutup manual oleh pengguna!
    await new Promise((resolve) => {
        browser.on('disconnected', () => {
            console.log(`\n ${RED}❌ BROWSER DITUTUP!${RESET}`);
            console.log(` ${GRAY}Mendeteksi pemutusan sesi Ghost Browser...${RESET}`);
            console.log(` ${GREEN}✅ Mematikan Daemon API dan membersihkan memori. Sampai jumpa!${RESET}\n`);
            process.exit(0);
        });
    });
}

async function startWalletConnectDaemon(uri) {
    const { Core } = require('@walletconnect/core');
    const { Web3Wallet } = require('@walletconnect/web3wallet');
    
    clear(); title();
    console.log(` ${CYAN}[ WALLETCONNECT DAEMON AKTIF ]${RESET}\n`);

    const mWalletObj = getMasterWalletObj();
    if (!mWalletObj) {
        console.log(` ${RED}❌ Master Wallet belum diset! Silakan ke menu Kelola Dompet terlebih dahulu.${RESET}`);
        process.exit(1);
    }
    const wallet = new ethers.Wallet(mWalletObj.pk, provider);
    const sym = CHAINS[ACTIVE_CHAIN] ? CHAINS[ACTIVE_CHAIN].symbol : "ETH";
    const eip155ChainId = `eip155:${CHAINS[ACTIVE_CHAIN].id}`;

    console.log(` ${GRAY}⏳ Membangun jembatan ke dApp Server...${RESET}`);

    // 🔥 LOGIKA PROJECT ID DINAMIS
    const WC_CFG_FILE = 'wc_config.json';
    let PROJECT_ID = process.env.WC_PROJECT_ID;
    
    if (!PROJECT_ID && fs.existsSync(WC_CFG_FILE)) {
        try {
            const wcCfg = JSON.parse(fs.readFileSync(WC_CFG_FILE, 'utf8'));
            PROJECT_ID = wcCfg.projectId;
        } catch(e) {}
    }

    if (!PROJECT_ID) {
        console.log(`\n ${YELLOW}⚠️ Project ID WalletConnect belum diatur!${RESET}`);
        console.log(` ${GRAY}Dapatkan API Key gratis di: https://cloud.walletconnect.com/${RESET}`);
        const inputId = await askQuestion(` ${CYAN}╰─ Masukkan Project ID Anda: ${RESET}`);
        PROJECT_ID = inputId.trim();
        
        if (!PROJECT_ID || PROJECT_ID.length < 10) {
            console.log(` ${RED}❌ Project ID tidak valid! Mematikan bot...${RESET}`);
            process.exit(1);
        }
        fs.writeFileSync(WC_CFG_FILE, JSON.stringify({ projectId: PROJECT_ID }, null, 4));
        console.log(` ${GREEN}✅ Project ID tersimpan di ${WC_CFG_FILE}!${RESET}\n`);
    }

    try {
        // 🔥 MULTI-ACCOUNT ISOLATION: Pisahkan database cache WalletConnect tiap tuyul agar tidak tabrakan!
        const sanitizedWalletName = mWalletObj.name.replace(/[^a-zA-Z0-9]/g, '_');
        const core = new Core({ 
            projectId: PROJECT_ID,
            logger: 'fatal',
            storageOptions: { database: `.wc_db_${sanitizedWalletName}` } 
        });
        const web3wallet = await Web3Wallet.init({
            core,
            metadata: {
                name: 'Base Sniper Pro',
                description: 'Ultimate CLI Wallet Manager',
                url: 'https://basesniper.pro',
                icons: ['https://avatars.githubusercontent.com/u/37784886']
            }
        });

        console.log(` ${GREEN}✅ Mesin WalletConnect siap!${RESET}\n`);
        console.log(` ${GRAY}⏳ Melakukan Pairing dengan URI yang diberikan...${RESET}`);

        // Handle Session Proposal (Saat pertama kali dApp nge-scan QR/konek)
        web3wallet.on('session_proposal', async proposal => {
            const { id, params } = proposal;
            const dappName = params.proposer.metadata.name;
            const dappUrl = params.proposer.metadata.url;
            
            console.log(`\n ${CYAN}⚠️ PERMINTAAN KONEKSI DARI DAPP!${RESET}`);
            console.log(` ├─ Nama dApp : ${GREEN}${dappName}${RESET}`);
            console.log(` └─ URL dApp  : ${GRAY}${dappUrl}${RESET}\n`);
            
            let isAccepted = false;
            if (AUTO_MODE) {
                console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Mengizinkan koneksi secara otomatis...${RESET}`);
                isAccepted = true;
            } else {
                const accept = await askQuestion(` ${YELLOW}Izinkan dApp ini mengakses Master Wallet? [ y / n ] : ${RESET}`);
                isAccepted = accept.toLowerCase() === 'y';
            }

            if (isAccepted) {
                try {
                    // 🔥 HACK WALLETCONNECT: Setujui semua jaringan & metode yang diminta dApp agar tidak nyangkut/ditolak SDK
                    let reqChains = params.requiredNamespaces?.eip155?.chains || [];
                    let optChains = params.optionalNamespaces?.eip155?.chains || [];
                    
                    // 🌐 INJEKSI MULTICHAIN: Beritahu dApp bahwa bot kita adalah dompet sakti yang mendukung 100+ jaringan!
                    let dbChains = Object.values(CHAINS).map(c => `eip155:${c.id}`);
                    let extraChains = ['eip155:1', 'eip155:10', 'eip155:56', 'eip155:137', 'eip155:250', 'eip155:42161', 'eip155:43114', 'eip155:42220'];
                    
                    let allChains = [...new Set([...reqChains, ...optChains, eip155ChainId, ...dbChains, ...extraChains])];
                    let approvedAccounts = allChains.map(chain => `${chain}:${wallet.address}`);
                    
                    let reqMethods = params.requiredNamespaces?.eip155?.methods || [];
                    let optMethods = params.optionalNamespaces?.eip155?.methods || [];
                    let allMethods = [...new Set([...reqMethods, ...optMethods, 'eth_sendTransaction', 'personal_sign', 'eth_signTypedData_v4', 'wallet_switchEthereumChain', 'wallet_addEthereumChain', 'eth_sign'])];

                    await web3wallet.approveSession({
                        id,
                        namespaces: {
                            eip155: {
                                accounts: approvedAccounts,
                                methods: allMethods,
                                events: ['chainChanged', 'accountsChanged']
                            }
                        }
                    });
                    console.log(` ${GREEN}✅ Terkoneksi dengan ${dappName}! Menunggu transaksi...${RESET}`);
                } catch(e) {
                    console.log(` ${RED}❌ Gagal menyetujui sesi: ${e.message}${RESET}`);
                }
            } else {
                await web3wallet.rejectSession({ id, reason: { code: 4001, message: 'User rejected the request' } });
                console.log(` ${RED}❌ Koneksi Ditolak.${RESET}`);
            }
        });

        // Handle Transaksi / Signature (Struk Kasir)
        web3wallet.on('session_request', async event => {
            const { topic, params, id } = event;
            const { request, chainId: reqChainId } = params;
            
            // 🔥 SILENT BYPASS: Cegat method spam dari dApp
            if (request.method === 'wallet_getCapabilities' || request.method === 'eth_accounts') {
                await web3wallet.respondSessionRequest({ topic, response: { id, result: request.method === 'eth_accounts' ? [wallet.address] : {}, jsonrpc: '2.0' } });
                return;
            }

            // 🌐 SMART ROUTER: Deteksi jika dApp diam-diam pindah jaringan di UI!
            if (reqChainId) {
                const targetChainIdDec = parseInt(reqChainId.split(':')[1]); // Ambil angka ID (misal: 56)
                if (targetChainIdDec !== CHAINS[ACTIVE_CHAIN].id) {
                    console.log(`\n ${YELLOW}⚠️ [SMART ROUTER] dApp meminta eksekusi di jaringan baru (Chain ID: ${targetChainIdDec})${RESET}`);
                    
                    let newChainKey = null;
                    for (const [key, chainData] of Object.entries(CHAINS)) {
                        if (chainData.id === targetChainIdDec) { newChainKey = key; break; }
                    }

                    // 🚀 AUTO-FETCH CHAINLIST
                    if (!newChainKey) {
                        console.log(` ${GRAY}⏳ Jaringan belum terdaftar. Mengunduh data dari Chainlist API...${RESET}`);
                        try {
                            const res = await fetch("https://chainid.network/chains.json");
                            const allChains = await res.json();
                            const foundChain = allChains.find(c => c.chainId === targetChainIdDec);
                            if (foundChain) {
                                const safeRpcs = foundChain.rpc.filter(r => r.startsWith('http') && !r.includes('${'));
                                if (safeRpcs.length > 0) {
                                    // 🔥 FIX: Jadikan huruf kecil dan simpan permanen ke file database!
                                    newChainKey = (foundChain.shortName || `chain_${targetChainIdDec}`).toLowerCase();
                                    CHAINS[newChainKey] = {
                                        name: foundChain.name, id: foundChain.chainId, symbol: foundChain.nativeCurrency?.symbol || "ETH",
                                        cgId: "ethereum", weth: ethers.ZeroAddress, usdc: ethers.ZeroAddress, defaultRpc: safeRpcs,
                                        v2Router: ethers.ZeroAddress, v2Factory: ethers.ZeroAddress, v3Router: ethers.ZeroAddress, v3Factory: ethers.ZeroAddress, v3Quoter: ethers.ZeroAddress,
                                        hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress, v4Router: ethers.ZeroAddress
                                    };
                                    saveChainsDB(); // 🔥 WAJIB SAVE BIAR MUNCUL DI MENU -H
                                    console.log(` ${GREEN}✅ Berhasil mengunduh data ${foundChain.name}! Argumen aktif: --${newChainKey}${RESET}`);
                                }
                            }
                        } catch(e) {}
                    }

                    if (newChainKey) {
                        console.log(` ${GRAY}⏳ Mengalihkan RPC bot ke: ${GREEN}${CHAINS[newChainKey].name}${RESET}`);
                        applyNetwork(newChainKey); // Setel Variabel Global Bot
                        connectRPC(); // Restart Mesin Node
                        
                        // Tunggu mesin RPC nyala
                        let waitCheck = 0;
                        while(!RPC_CONNECTED && waitCheck < 15) { await new Promise(r => setTimeout(r, 200)); waitCheck++; }
                        console.log(` ${GREEN}✅ Bot siap! Meneruskan eksekusi transaksi...${RESET}`);
                    } else {
                        console.log(` ${RED}❌ Gagal: Jaringan tidak didukung.${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4902, message: 'Unrecognized chain ID' }, jsonrpc: '2.0' } });
                        return;
                    }
                }
            }
            
            console.log(`\n ${CYAN}[ 🧾 STRUK KASIR DAPP (WALLETCONNECT) ]${RESET}`);
            
            if (request.method === 'eth_sendTransaction') {
                const txData = request.params[0];
                const valueEth = txData.value ? ethers.formatEther(txData.value) : "0.0";
                const targetAddress = txData.to || "Kontrak Baru";
                
                // 🔥 Persiapan Data & Kalkulasi Keamanan Auto-Mode
                const activeWallet = new ethers.Wallet(mWalletObj.pk, provider);
                let activeCfg = loadConfig();
                let gasConfig = await getGasConfig(BigInt(activeCfg.gwei.sell || 100));

                let txValue = 0n;
                if (txData.value) txValue = BigInt(txData.value);

                let gasLimit;
                if (txData.gas) gasLimit = BigInt(txData.gas);
                else if (txData.gasLimit) gasLimit = BigInt(txData.gasLimit);
                else {
                    try {
                        gasLimit = await provider.estimateGas({ to: txData.to, data: txData.data || "0x", value: txValue, from: activeWallet.address });
                        gasLimit = (gasLimit * 120n) / 100n; 
                    } catch(e) { gasLimit = 650000n; }
                }

                // 🔥 Kalkulasi Biaya USD
                let gasCostWei = gasLimit * gasConfig.maxFeePerGasConfig;
                let totalCostEth = parseFloat(ethers.formatEther(gasCostWei));
                let totalCostUsd = totalCostEth * ethPriceUsd;
                let isValueSending = txValue > 0n;
                let isExpensive = totalCostUsd >= 1;

                // 🧠 KAMUS WEB3 (Method ID Translator)
                const METHOD_DICTIONARY = {
                    "0x095ea7b3": "🔓 APPROVE TOKEN (Memberi Akses Kontrak)",
                    "0xa9059cbb": "💸 TRANSFER TOKEN (Mengirim Aset)",
                    "0x23b872dd": "💸 TRANSFER FROM (Menarik Aset)",
                    "0x38ed1739": "🔄 SWAP TOKENS FOR TOKENS",
                    "0x18cbafe5": "🔄 SWAP TOKENS FOR ETH",
                    "0x7ff36ab5": "🔄 SWAP ETH FOR TOKENS",
                    "0x791ac947": "🔄 SWAP TOKENS FOR ETH (With Fee)",
                    "0xb6f9de95": "🔄 SWAP ETH FOR TOKENS (With Fee)",
                    "0x5ae401dc": "🎯 MULTICALL (Kumpulan Eksekusi)",
                    "0x3593564c": "⚡ EXECUTE (Universal Router)"
                };

                let methodId = (txData.data && txData.data.length >= 10) ? txData.data.substring(0, 10) : "0x";
                let actionName = METHOD_DICTIONARY[methodId.toLowerCase()] || `⚙️ CUSTOM EXECUTION (Method: ${methodId})`;

                // 🔥 Auto-Dekode parameter khusus untuk Approve / Transfer
                let decodedInfo = "";
                if ((methodId.toLowerCase() === "0x095ea7b3" || methodId.toLowerCase() === "0xa9059cbb") && txData.data.length >= 138) {
                    try {
                        const spender = "0x" + txData.data.substring(34, 74);
                        const amountHex = "0x" + txData.data.substring(74, 138);
                        const rawAmount = BigInt(amountHex);
                        
                        if (rawAmount > (2n**200n)) {
                            decodedInfo = `\n ├─ Tembusan  : ${GRAY}${spender}${RESET}\n ├─ Jumlah    : ${RED}UNLIMITED (Tak Terbatas)${RESET}`;
                        } else {
                            // 🌐 HACK: Interogasi Kontrak di Blockchain untuk tahu Desimal & Simbolnya!
                            const ERC20_MINI_ABI = ["function symbol() view returns (string)", "function decimals() view returns (uint8)"];
                            const tContract = new ethers.Contract(targetAddress, ERC20_MINI_ABI, provider);
                            
                            // Tarik data secara paralel biar nggak lemot
                            const [tSymbol, tDec] = await Promise.all([tContract.symbol(), tContract.decimals()]);
                            const parsedAmount = parseFloat(ethers.formatUnits(rawAmount, tDec)).toFixed(4);
                            
                            // 🔥 FIX: Spasi diluruskan persis 10 karakter sebelum titik dua, warna jadi MERAH MINUS
                            decodedInfo = `\n ├─ Tembusan  : ${GRAY}${spender}${RESET}\n ├─ Jumlah    : ${RED}-${parsedAmount} ${tSymbol}${RESET}`;
                        }
                    } catch(e) {
                        decodedInfo = `\n ├─ Tembusan  : 0x${txData.data.substring(34, 74)}\n ├─ Jumlah    : ${YELLOW}Gagal membaca detail token dari RPC${RESET}`;
                    }
                }

                console.log(` │`);
                console.log(` ├─ Jenis     : ${YELLOW}Smart Contract Execution (eth_sendTransaction)${RESET}`);
                console.log(` ├─ Aksi      : ${CYAN}${BOLD}${actionName}${RESET}${decodedInfo}`);
                console.log(` ├─ Target    : ${targetAddress}`);
                console.log(` ├─ Value     : ${GREEN}${valueEth} ${sym}${RESET}`);
                console.log(` ├─ Est. Gas  : ${YELLOW}$${totalCostUsd.toFixed(4)} (${parseFloat(ethers.formatUnits(gasConfig.maxFeePerGasConfig, "gwei")).toFixed(5)} Gwei)${RESET}`);
                console.log(` └─ Data Hex  : ${GRAY}${txData.data.substring(0, 40)}...${RESET}\n`);
                
                let proceedTx = false;
                if (AUTO_MODE) {
                    if (isValueSending || isExpensive) {
                        console.log(` ${YELLOW}⚠️ AUTO-MODE TERTAHAN: Transaksi mengirim saldo ETH atau Gas Mahal (>= $1). Butuh izin manual!${RESET}`);
                        const confirm = await askQuestion(` ${YELLOW}Eksekusi Transaksi Ini? [ Enter = GAS 🚀 / 'n' = Tolak ] : ${RESET}`);
                        proceedTx = confirm.trim().toLowerCase() !== 'n';
                    } else {
                        console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Mengeksekusi transaksi secara otomatis...${RESET}`);
                        proceedTx = true;
                    }
                } else {
                    const confirm = await askQuestion(` ${YELLOW}Eksekusi Transaksi Ini? [ Enter = GAS 🚀 / 'n' = Tolak ] : ${RESET}`);
                    proceedTx = confirm.trim().toLowerCase() !== 'n';
                }
                
                if (proceedTx) {
                    console.log(` ${GRAY}⏳ Mengeksekusi transaksi...${RESET}`);
                    try {
                        let txSuccessFlag = false;
                        let tx;
                        while (!txSuccessFlag) {
                            try {
                                const nonce = await provider.getTransactionCount(activeWallet.address, "pending");
                                const txPayload = {
                                    to: txData.to,
                                    data: txData.data || "0x",
                                    value: txValue,
                                    gasLimit: gasLimit,
                                    maxFeePerGas: gasConfig.maxFeePerGasConfig,
                                    maxPriorityFeePerGas: gasConfig.maxPriorityFeePerGasConfig,
                                    chainId: CHAINS[ACTIVE_CHAIN].id,
                                    nonce: nonce
                                }; // 🔥 FIX: Samakan dengan Ghost Daemon, pakai standar bensin modern!

                                tx = await activeWallet.sendTransaction(txPayload);
                                console.log(` ${GREEN}✅ Terkirim! Hash: ${tx.hash}${RESET}`);
                                txSuccessFlag = true;
                            } catch (err) {
                                const errMsg = (err.message || "").toLowerCase();
                                if (errMsg.includes("insufficient funds")) {
                                    console.log(` ${RED}❌ Gagal: ${err.shortMessage || errMsg}${RESET}`);
                                    const requiredGasWei = gasLimit * gasConfig.maxFeePerGasConfig;
                                    const isFunded = await handleEmergencyGas(activeWallet.address, requiredGasWei);
                                    if (!isFunded) {
                                        throw err;
                                    }
                                } else {
                                    throw err;
                                }
                            }
                        }
                        
                        // 1. Balas dApp secepat kilat agar UI website tidak macet/loading lama
                        await web3wallet.respondSessionRequest({ topic, response: { id, result: tx.hash, jsonrpc: '2.0' } });

                        // 2. Fitur Bedah Resi (Menunggu konfirmasi blok untuk melihat token In/Out)
                        process.stdout.write(` ${GRAY}⏳ Membedah mutasi token di blockchain...${RESET}`);
                        try {
                            const receipt = await tx.wait(1);
                            let mutations = [];
                            const ERC20_MINI_ABI = ["function symbol() view returns (string)", "function decimals() view returns (uint8)"];
                            const TRANSFER_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
                            const myAddr = activeWallet.address.toLowerCase();

                            for (let log of receipt.logs) {
                                // Cari Event Transfer (Topic 0 = Transfer Signature, length = 3)
                                if (log.topics[0] === TRANSFER_TOPIC && log.topics.length === 3) {
                                    const fromAddr = ("0x" + log.topics[1].substring(26)).toLowerCase();
                                    const toAddr = ("0x" + log.topics[2].substring(26)).toLowerCase();
                                    
                                    // Jika alamat kita terlibat dalam transfer
                                    if (fromAddr === myAddr || toAddr === myAddr) {
                                        const tContract = new ethers.Contract(log.address, ERC20_MINI_ABI, provider);
                                        let sym = "TOKEN", dec = 18n;
                                        try {
                                            [sym, dec] = await Promise.all([tContract.symbol(), tContract.decimals()]);
                                        } catch(e) {}
                                        
                                        const rawAmount = BigInt(log.data);
                                        const amount = parseFloat(ethers.formatUnits(rawAmount, dec)).toFixed(4);
                                        
                                        if (amount > 0) {
                                            if (fromAddr === myAddr) mutations.push(`    ${RED}📉 KELUAR : -${amount} ${sym}${RESET}`);
                                            if (toAddr === myAddr) mutations.push(`    ${GREEN}📈 MASUK  : +${amount} ${sym}${RESET}`);
                                        }
                                    }
                                }
                            }

                            process.stdout.write(`\r\x1b[2K`); // Hapus tulisan loading
                            if (mutations.length > 0) {
                                console.log(` ${CYAN}[ 📊 HASIL MUTASI ASET ]${RESET}`);
                                mutations.forEach(m => console.log(m));
                                console.log("");
                            } else {
                                console.log(` ${GRAY} ├─ Transaksi sukses (Tidak ada mutasi token / Hanya Approve).${RESET}\n`);
                            }

                        } catch (err) {
                            process.stdout.write(`\r\x1b[2K`);
                            console.log(` ${YELLOW}⚠️ Transaksi sukses, tapi gagal membaca resi (RPC Delay).${RESET}\n`);
                        }

                    } catch (e) {
                        console.log(` ${RED}❌ Gagal mengeksekusi: ${e.message}${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'Transaction Failed' }, jsonrpc: '2.0' } });
                    }
                } else {
                    console.log(` ${RED}❌ Transaksi ditolak.${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'User rejected the transaction' }, jsonrpc: '2.0' } });
                }

            } else if (request.method === 'personal_sign') {
                const message = ethers.toUtf8String(request.params[0]);
                console.log(` │`);
                console.log(` ├─ Jenis     : ${YELLOW}Sign Message (personal_sign)${RESET}`);
                console.log(` └─ Pesan     : ${GRAY}${message}${RESET}\n`);
                
                let proceedSign = false;
                if (AUTO_MODE) {
                    console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Menandatangani pesan secara otomatis...${RESET}`);
                    proceedSign = true;
                } else {
                    const confirm = await askQuestion(` ${YELLOW}Tanda Tangani Pesan Ini? [ Enter = YES ✍️ / 'n' = Tolak ] : ${RESET}`);
                    proceedSign = confirm.trim().toLowerCase() !== 'n';
                }
                
                if (proceedSign) {
                    try {
                        const signature = await wallet.signMessage(message);
                        console.log(` ${GREEN}✅ Signed!${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, result: signature, jsonrpc: '2.0' } });
                    } catch(e) {
                        console.log(` ${RED}❌ Gagal: ${e.message}${RESET}`);
                    }
                } else {
                    console.log(` ${RED}❌ Tanda tangan ditolak.${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'User rejected the signature' }, jsonrpc: '2.0' } });
                }
            } else if (request.method === 'eth_signTypedData_v4') {
                console.log(` │`);
                console.log(` ├─ Jenis     : ${YELLOW}Sign Typed Data (EIP-712)${RESET}`);
                console.log(` └─ Data JSON : ${GRAY}Terdapat payload data Permit/Swap.${RESET}\n`);
                
                let proceedSignTyped = false;
                if (AUTO_MODE) {
                    console.log(` ${GREEN}🤖 AUTO-MODE AKTIF: Menandatangani data secara otomatis...${RESET}`);
                    proceedSignTyped = true;
                } else {
                    const confirm = await askQuestion(` ${YELLOW}Tanda Tangani Data Ini? [ Enter = YES ✍️ / 'n' = Tolak ] : ${RESET}`);
                    proceedSignTyped = confirm.trim().toLowerCase() !== 'n';
                }
                
                if (proceedSignTyped) {
                    try {
                        const payload = JSON.parse(request.params[1]);
                        // 🔥 FIX ETHERS V6: Hapus EIP712Domain dari types, kalau nggak bakal error "ambiguous primary types"
                        if (payload.types && payload.types.EIP712Domain) {
                            delete payload.types.EIP712Domain;
                        }
                        const signature = await wallet.signTypedData(payload.domain, payload.types, payload.message);
                        console.log(` ${GREEN}✅ Signed!${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, result: signature, jsonrpc: '2.0' } });
                    } catch(e) {
                        console.log(` ${RED}❌ Gagal: ${e.message}${RESET}`);
                    }
                } else {
                    console.log(` ${RED}❌ Tanda tangan ditolak.${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'User rejected the signature' }, jsonrpc: '2.0' } });
                }
            } else if (request.method === 'eth_sign') {
                console.log(` │`);
                console.log(` ├─ Jenis     : ${RED}${BOLD}🚨 BAHAYA: BLIND SIGNATURE (eth_sign)${RESET}`);
                const blindMsg = request.params[1] || request.params[0] || "0x";
                console.log(` └─ Data Hex  : ${GRAY}${blindMsg.substring(0, 40)}...${RESET}\n`);
                
                console.log(` ${RED}⚠️ [SATPAM ANTI-DRAINER AKTIF]${RESET}`);
                console.log(` ${RED}   dApp ini mencoba meminta tanda tangan buta (Blind Sign).${RESET}`);
                console.log(` ${RED}   Metode ini 99% digunakan oleh Web Phishing / Drainer untuk MENCURI SELURUH ASET ANDA!${RESET}\n`);
                
                let proceedBlindSign = false;
                // 🔥 Auto-Mode DITANGGUHKAN! Tidak boleh tembus otomatis.
                const confirm = await askQuestion(` ${YELLOW}Apakah Anda YAKIN ingin membuka gembok? [ Ketik 'SAYA YAKIN' untuk eksekusi / Enter = Tolak ] : ${RESET}`);
                proceedBlindSign = confirm.trim() === 'SAYA YAKIN';
                
                if (proceedBlindSign) {
                    console.log(` ${YELLOW}⚠️ Gembok dibuka. Mengeksekusi Blind Sign...${RESET}`);
                    try {
                        let signature;
                        try { signature = await wallet.signMessage(ethers.getBytes(blindMsg)); } 
                        catch(e) { signature = await wallet.signMessage(blindMsg); }
                        console.log(` ${GREEN}✅ Signed (Risiko ditanggung sendiri)!${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, result: signature, jsonrpc: '2.0' } });
                    } catch(e) {
                        console.log(` ${RED}❌ Gagal: ${e.message}${RESET}`);
                        await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'Signature failed' }, jsonrpc: '2.0' } });
                    }
                } else {
                    console.log(` ${GREEN}🛡️ Satpam beraksi: Tanda tangan diblokir! Aset Anda aman.${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4001, message: 'User rejected the signature (Anti-Drainer Protection)' }, jsonrpc: '2.0' } });
                }
            } else if (request.method === 'wallet_switchEthereumChain' || request.method === 'wallet_addEthereumChain') {
                console.log(` │`);
                console.log(` ├─ Jenis     : ${YELLOW}Switch / Add Network (${request.method})${RESET}`);
                const targetChainIdHex = request.params[0]?.chainId || "0x0";
                const targetChainIdDec = parseInt(targetChainIdHex, 16);
                console.log(` └─ Chain ID  : ${GRAY}${targetChainIdHex} (${targetChainIdDec})${RESET}\n`);
                
                // 🔥 Cari apakah chain yang diminta dApp ada di database bot kita
                let newChainKey = null;
                for (const [key, chainData] of Object.entries(CHAINS)) {
                    if (chainData.id === targetChainIdDec) {
                        newChainKey = key;
                        break;
                    }
                }

                // 🌐 AUTO-FETCH DARI CHAINLIST JIKA TIDAK ADA DI DATABASE
                if (!newChainKey) {
                    console.log(` ${GRAY}⏳ Jaringan ${targetChainIdDec} belum terdaftar. Mencari di Chainlist API...${RESET}`);
                    try {
                        const res = await fetch("https://chainid.network/chains.json");
                        const allChains = await res.json();
                        const foundChain = allChains.find(c => c.chainId === targetChainIdDec);

                        if (foundChain) {
                            const safeRpcs = foundChain.rpc.filter(r => r.startsWith('http') && !r.includes('${'));
                            if (safeRpcs.length > 0) {
                                // 🔥 Gunakan shortName dari API (eth, op, avax, mon) agar argumen pendek!
                                newChainKey = (foundChain.shortName || `chain_${targetChainIdDec}`).toLowerCase();
                                
                                CHAINS[newChainKey] = {
                                    name: foundChain.name,
                                    id: foundChain.chainId,
                                    symbol: foundChain.nativeCurrency?.symbol || "ETH",
                                    cgId: "ethereum", 
                                    weth: ethers.ZeroAddress, 
                                    usdc: ethers.ZeroAddress, 
                                    defaultRpc: safeRpcs,
                                    v2Router: ethers.ZeroAddress, v2Factory: ethers.ZeroAddress,
                                    v3Router: ethers.ZeroAddress, v3Factory: ethers.ZeroAddress, v3Quoter: ethers.ZeroAddress,
                                    hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress,
                                    v4Router: ethers.ZeroAddress
                                };
                                saveChainsDB(); // 🔥 SIMPAN PERMANEN KE FILE JSON!
                                console.log(` ${GREEN}✅ Berhasil mengunduh data ${foundChain.name}! Argumen aktif: --${newChainKey}${RESET}`);
                            }
                        }
                    } catch (e) {
                        console.log(` ${RED}❌ Gagal menghubungi Chainlist API.${RESET}`);
                    }
                }

                if (newChainKey) {
                    console.log(` ${GRAY}⏳ Mengalihkan jaringan internal ke: ${GREEN}${CHAINS[newChainKey].name}${RESET}`);
                    applyNetwork(newChainKey); // Update variabel global (WETH, Router, dll)
                    connectRPC(); // Restart Provider Ethers.js ke RPC chain baru
                    
                    console.log(` ${GREEN}✅ Auto-Approve Network Switch selesai!${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, result: null, jsonrpc: '2.0' } });
                    
                    // Notifikasi balik ke dApp biar UI mereka update
                    try {
                        await web3wallet.emitSessionEvent({
                            topic,
                            event: { name: 'chainChanged', data: targetChainIdHex },
                            chainId: `eip155:${targetChainIdDec}`
                        });
                    } catch(e) {}

                } else {
                    console.log(` ${RED}❌ Gagal: Jaringan tidak didukung oleh bot (Tambahkan dulu di config CHAINS).${RESET}`);
                    await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4902, message: 'Unrecognized chain ID' }, jsonrpc: '2.0' } });
                }
            } else if (request.method === 'wallet_getCapabilities') {
                // 🔥 SILENT BYPASS: Biar log terminal gak dispam sama Uniswap
                await web3wallet.respondSessionRequest({ topic, response: { id, result: {}, jsonrpc: '2.0' } });
            } else {
                console.log(` ${RED}⚠️ Metode tidak didukung: ${request.method}${RESET}`);
                await web3wallet.respondSessionRequest({ topic, response: { id, error: { code: 4200, message: 'Method unsupported' }, jsonrpc: '2.0' } });
            }
        });

        // 🚀 Eksekusi Pairing
        await web3wallet.core.pairing.pair({ uri });
        
        console.log(` ${GREEN}✅ Sinyal terkirim ke Relay! Menunggu dApp merespons...${RESET}`);
        console.log(` ${GRAY}(Jika tidak ada popup setelah 10 detik, link mungkin expired. Refresh dApp & ambil link baru!)${RESET}\n`);

        // 🔥 OBAT TAMPILAN ACAK-ACAKAN: Tahan terminal di mode Daemon agar tidak balik ke Dashboard!
        console.log(` ${YELLOW}⚠️ Mode Daemon aktif. Tekan CTRL+C untuk keluar dari bot jika sudah selesai.${RESET}\n`);
        await new Promise(() => {}); // Loop abadi menahan layar terminal

    } catch (e) {
        console.log(`\n ${RED}❌ Gagal inisialisasi WalletConnect: ${e.message}${RESET}`);
        await pause();
    }
}

// =================
// 10. INIT (OMNI-CHAIN CLI BOOTLOADER)
// =================
let AUTO_MODE = false;
let DEBUG_MODE = false;
let OVERRIDE_WALLET_NAME = null;
let GHOST_IDENTITY = "ghostwallet"; // 🔥 GLOBAL IDENTITY STATE

async function init() { // 🔥 WAJIB TAMBAH 'async' DI SINI
    const args = process.argv.slice(2);

    // ================================================================
    // 🔥 LOGIKA TAMBAH CHAIN OTOMATIS (VIA ID ATAU NAMA)
    // ================================================================
    const addChainIdx = args.indexOf('--add-chain');
    if (addChainIdx !== -1 && args[addChainIdx + 1]) {
        const query = args[addChainIdx + 1].toLowerCase();
        const isId = /^\d+$/.test(query); // Cek apakah murni angka (ID)

        console.log(`\n ${GRAY}⏳ Mengunduh data dari Chainlist API...${RESET}`);
        try {
            const res = await fetch("https://chainid.network/chains.json");
            const allChains = await res.json();
            let selectedChainData = null;

            if (isId) {
                // JIKA INPUT BERUPA ID
                selectedChainData = allChains.find(c => c.chainId === parseInt(query));
                if (!selectedChainData) {
                    console.log(`\n ${RED}❌ Chain ID ${query} tidak ditemukan di Chainlist API.${RESET}`);
                    process.exit(1);
                }
            } else {
                // JIKA INPUT BERUPA NAMA
                const matches = allChains.filter(c => 
                    c.name.toLowerCase().includes(query) || 
                    (c.shortName && c.shortName.toLowerCase().includes(query))
                );

                if (matches.length === 0) {
                    console.log(`\n ${RED}❌ Tidak ada jaringan yang mengandung nama "${query}".${RESET}`);
                    process.exit(1);
                }

                // Tampilkan hasil pencarian
                console.log(`\n ${CYAN}[ HASIL PENCARIAN JARINGAN: "${query.toUpperCase()}" ]${RESET}`);
                matches.forEach((m, i) => {
                    console.log(` ${GRAY}[${i + 1}]${RESET} ${m.name} ${GRAY}(ID: ${m.chainId} | Koin: ${m.nativeCurrency?.symbol || 'ETH'})${RESET}`);
                });

                // Minta user memilih
                const pickStr = await askQuestion(`\n ${CYAN}╰─ Pilih angka jaringan yang ingin ditambah [1 - ${matches.length}] : ${RESET}`);
                const pickIdx = parseInt(pickStr) - 1;

                if (isNaN(pickIdx) || pickIdx < 0 || pickIdx >= matches.length) {
                    console.log(`\n ${RED}❌ Pilihan tidak valid. Dibatalkan.${RESET}`);
                    process.exit(1);
                }
                selectedChainData = matches[pickIdx];
            }

            // FILTER RPC & SIMPAN KE DATABASE
            const safeRpcs = selectedChainData.rpc.filter(r => typeof r === 'string' && r.startsWith('http') && !r.includes('${'));
            if (safeRpcs.length === 0) {
                console.log(`\n ${RED}❌ Tidak ditemukan RPC HTTP publik yang aman untuk jaringan ini.${RESET}`);
                process.exit(1);
            }

            const key = (selectedChainData.shortName || `chain_${selectedChainData.chainId}`).toLowerCase();
            CHAINS[key] = {
                name: selectedChainData.name,
                id: selectedChainData.chainId,
                symbol: selectedChainData.nativeCurrency?.symbol || "ETH",
                cgId: "ethereum", 
                weth: ethers.ZeroAddress,
                usdc: ethers.ZeroAddress,
                defaultRpc: safeRpcs,
                v2Router: ethers.ZeroAddress, v2Factory: ethers.ZeroAddress,
                v3Router: ethers.ZeroAddress, v3Factory: ethers.ZeroAddress, v3Quoter: ethers.ZeroAddress,
                hasAero: false, aeroRouter: ethers.ZeroAddress, aeroFactory: ethers.ZeroAddress,
                v4Router: ethers.ZeroAddress
            };

            // 🔥 Tulis Synchronous (Mencegah file korup / SyntaxError)
            fs.writeFileSync('chains_db.json', JSON.stringify(CHAINS, null, 4));
            
            console.log(`\n ${GREEN}✅ Berhasil menambahkan ${selectedChainData.name} (${selectedChainData.nativeCurrency?.symbol || 'ETH'}) ke chains_db.json!${RESET}`);
            console.log(` ${GREEN}👉 Jalankan bot dengan: node wallet_3.js --${key}${RESET}\n`);
            process.exit(0);

        } catch (e) {
            console.log(`\n ${RED}❌ Gagal mengambil data Chainlist: ${e.message}${RESET}`);
            process.exit(1);
        }
    }
    // ================================================================

    if (args.includes('--help') || args.includes('-h')) {
        clear(); title();
        console.log(` ${CYAN}📖 PANDUAN PENGGUNAAN CLI (OMNI-CHAIN)${RESET}\n`);
        
        // 🔥 TAMBAHAN INFO MENU ADD-CHAIN
        console.log(` ${GREEN}--add-chain [ID/Nama]${RESET}   ${GRAY}👉 Tambah jaringan baru otomatis dari Chainlist${RESET}\n`);
        
        for (const [key, data] of Object.entries(CHAINS)) {
            let paddedKey = `--${key}`.padEnd(10, ' ');
            console.log(` ${GREEN}node wallet.js ${paddedKey}${RESET} ${GRAY}👉 Masuk ke ${data.name}${RESET}`);
        }

        console.log(`\n ${CYAN}🤖 FITUR OTOMASI DAPP / SCRIPTING${RESET}\n`);

        console.log(` ${GREEN}--wc "wc:..."${RESET}               ${GRAY}👉 Jalur WalletConnect${RESET}`);
        console.log(` ${GREEN}--ghostwallet [port]${RESET}        ${GRAY}👉 Jalankan API Daemon OMNI MODE (Default 3333)${RESET}`);
        console.log(` ${GREEN}--[wallet] [port]${RESET}           ${GRAY}👉 Daemon Spesifik (Cth: --bitget 3333, --metamask)${RESET}`);
        console.log(` ${GREEN}--ghostwallet-browser "url"${RESET} ${GRAY}👉 Buka Browser (Dengan 8 Ghostwallet!)${RESET}`);
        console.log(` ${GREEN}--[wallet]-browser "url"${RESET}    ${GRAY}👉 Browser Spesifik (Cth: --trustwallet-browser)${RESET}`);
        console.log(`                             ${GRAY}   [wallet]: metamask, trustwallet, uniswap,${RESET}`);
        console.log(`                             ${GRAY}   rainbow, coinbase, okx, rabby, bitget${RESET}`);
        console.log(` ${GREEN}--wallet "nama"${RESET}             ${GRAY}👉 Pakai dompet spesifik (Abaikan Master)${RESET}`);
        console.log(` ${GREEN}--auto${RESET}                      ${GRAY}👉 Eksekusi transaksi otomatis${RESET}`);
        console.log(` ${GREEN}--debug${RESET}                     ${GRAY}👉 Tampilkan LOG rahasia Jembatan Wagmi${RESET}`);
        console.log(` ${GREEN}--create${RESET}                    ${GRAY}👉 Buat wallet baru massal (Format: Random+WIB)${RESET}`);
        console.log(` ${GREEN}--batch [jumlah]${RESET}            ${GRAY}👉 Jumlah eksekusi / wallet yang akan dibuat${RESET}`);
        console.log(` ${GREEN}--job [jumlah]${RESET}              ${GRAY}👉 Limit worker concurrent (Multi-Browser)${RESET}\n`);
        process.exit(0);
    }

    clear(); title();
    let selectedChain = "base"; 
    let isChainSpecified = false;
    let wcUri = null;
    let runDaemonOnly = false;
    let daemonPort = 3333;
    let browserUrl = null;
    
    // VARIABEL BATCH & CREATE
    let isCreateWallet = false;
    let batchCount = 1;

    // 🔥 THE ELITE 8 WALLETS DICTIONARY ARGUMENTS & BROWSER ENGINE SELECTION
    let SELECTED_BROWSER_ENGINE = "patchright"; // Default: Patchright murni
    const eliteArgs = ['--ghostwallet', '--metamask', '--trustwallet', '--uniswap', '--rainbow', '--coinbase', '--okx', '--rabby', '--bitget'];
    const eliteBrowserArgs = eliteArgs.map(a => a + '-browser');
    // Tambahkan '--add-chain' di dalam array bypassArgs di bawah ini:
    const bypassArgs = ['--wc', '--auto', '--debug', '--wallet', '--create', '--batch', '--job', '--cloakbrowser', '--patchright', '--add-chain', ...eliteArgs, ...eliteBrowserArgs];

    for (let i = 0; i < args.length; i++) {
        if (args[i] === '--cloakbrowser') SELECTED_BROWSER_ENGINE = "cloakbrowser";
        if (args[i] === '--patchright') SELECTED_BROWSER_ENGINE = "patchright";
        const arg = args[i];
        if (arg.startsWith('--') && !bypassArgs.includes(arg)) {
            let potentialChain = arg.replace('--', '').toLowerCase();
            if (potentialChain === 'pol') potentialChain = 'polygon';
            if (typeof CHAINS !== 'undefined' && CHAINS[potentialChain]) {
                selectedChain = potentialChain;
                isChainSpecified = true;
            }
        }
        if (arg === '--wc' && args[i+1] && !args[i+1].startsWith('--')) wcUri = args[i+1];
        if (arg === '--wallet' && args[i+1] && !args[i+1].startsWith('--')) OVERRIDE_WALLET_NAME = args[i+1];
        
        // Tangkap argumen batch & create
        if (arg === '--create') isCreateWallet = true;
        if (arg === '--batch' && args[i+1] && !args[i+1].startsWith('--')) {
            batchCount = parseInt(args[i+1]) || 1;
        }
        
        // Mode 1: HANYA DAEMON (Background API)
        if (eliteArgs.includes(arg)) {
            runDaemonOnly = true;
            GHOST_IDENTITY = arg.replace('--', '');
            if (args[i+1] && !args[i+1].startsWith('--')) daemonPort = parseInt(args[i+1]) || 3333;
        }
        
        // Mode 2: DAEMON + BROWSER (Suntik KTP)
        if (eliteBrowserArgs.includes(arg)) {
            GHOST_IDENTITY = arg.replace('--', '').replace('-browser', '');
            if (args[i+1] && !args[i+1].startsWith('--')) browserUrl = args[i+1];
        }
        
        if (arg === '--auto') AUTO_MODE = true;
        if (arg === '--debug') DEBUG_MODE = true;
    }

    // 🔥 EKSEKUSI --CREATE DARI CLI
    if (isCreateWallet) {
        console.log(`\n ${CYAN}[ GENERATOR WALLET OTOMATIS ]${RESET}`);
        console.log(` ${GRAY}⏳ Memproses pembuatan ${batchCount} wallet baru...${RESET}`);
        
        const db = loadDB();
        let newWallets = [];
        let exportText = "";

        const getWibTimestamp = () => {
            const d = new Date();
            const utc = d.getTime() + (d.getTimezoneOffset() * 60000);
            const wib = new Date(utc + (3600000 * 7));
            const pad = (n) => n.toString().padStart(2, '0');
            return `${pad(wib.getHours())}${pad(wib.getMinutes())}${pad(wib.getSeconds())}`;
        };

        const randomStr = () => Math.random().toString(36).substring(2, 6).toUpperCase();

        for (let i = 0; i < batchCount; i++) {
            const w = ethers.Wallet.createRandom();
            const name = `Tuyul_${randomStr()}_${getWibTimestamp()}`;
            newWallets.push({ name: name, address: w.address, pk: w.privateKey, seed: w.mnemonic.phrase });
            
            exportText += `Name: ${name}\n`;
            exportText += `Address: ${w.address}\n`;
            exportText += `Private Key: ${w.privateKey}\n`;
            exportText += `Seed Phrase: ${w.mnemonic.phrase}\n\n`;
            
            db.wallets.push(newWallets[i]);
        }
        
        if (db.master_index === null) db.master_index = 0;
        saveDB(db);

        const filename = `wallet_batch_${getWibTimestamp()}.txt`;
        fs.writeFileSync(filename, exportText);

        console.log(` ${GREEN}✅ Berhasil mencetak ${batchCount} wallet!${RESET}`);
        console.log(` ${GREEN}✅ Data disimpan ke database dan diekspor ke: ${filename}${RESET}\n`);
        
        if (!runDaemonOnly && !browserUrl && !wcUri) {
            process.exit(0);
        }
    }

    if (!isChainSpecified) console.log(` ${YELLOW}⚠️ Mode Argumen: Jaringan tidak dipilih. Memuat BASE MAINNET secara otomatis...${RESET}`);
    else console.log(` ${GREEN}✅ Argumen Terbaca: Memasuki Jaringan ${selectedChain.toUpperCase()}!${RESET}`);
    
    if (AUTO_MODE) console.log(` ${GREEN}🤖 GLOBAL AUTO-MODE Diaktifkan!${RESET}`);
    console.log("");

    if (typeof applyNetwork === 'function') applyNetwork(selectedChain);
    connectRPC(); 

    let spinnerFrames = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'];
    let sIdx = 0;
    let checkCount = 0;
    const checkInterval = setInterval(() => {
        // 🔥 HACKER SPINNER (Pakai \r biar animasi muter di tempat)
        process.stdout.write(`\r ${CYAN}${spinnerFrames[sIdx++ % spinnerFrames.length]}${RESET} ${GRAY}Menghubungi RPC Node... [${checkCount}/50]${RESET}`);
        checkCount++;
        if (RPC_CONNECTED || checkCount >= 50) {
            clearInterval(checkInterval);
            process.stdout.write(`\r\x1b[2K`); // Bersihkan spinner saat selesai
            
            if (wcUri) {
                startWalletConnectDaemon(wcUri).catch(console.error);
            } 
            else if (browserUrl) {
                // Eksekusi Mode Kemarin (Terminal + Browser)
                startGhostDaemon(daemonPort).then(daemon => {
                    if (daemon) startGhostBrowser(browserUrl, daemon.port, daemon.walletAddress, SELECTED_BROWSER_ENGINE).catch(console.error);
                }).catch(console.error);
            } 
            else if (runDaemonOnly) {
                // Eksekusi Mode API Murni
                startGhostDaemon(daemonPort).then(daemon => {
                    if (daemon) {
                        console.log(`\n ${YELLOW}⚠️ Mode Daemon API aktif (Tanpa Browser). Menunggu eksekusi dari script luar...${RESET}`);
                        console.log(` ${GRAY}Tekan CTRL+C untuk keluar.${RESET}\n`);
                    }
                }).catch(console.error);
            } 
            else {
                startDashboard().catch(console.error);
            }
        }
    }, 200);
}

// ==========================================
// 🔥 MULTICALL3 ENGINE (ANTI RATE-LIMIT)
// ==========================================
const MULTICALL3_ADDRESS = "0xcA11bde05977b3631167028862bE2a173976CA11";
const MULTICALL3_ABI = [
    "function aggregate3(tuple(address target, bool allowFailure, bytes callData)[] calls) view returns (tuple(bool success, bytes returnData)[] returnData)",
    "function getEthBalance(address addr) view returns (uint256 balance)"
];

async function getBatchBalances(addresses) {
    if (!provider || addresses.length === 0) return addresses.map(() => "0.0");
    try {
        const multicall = new ethers.Contract(MULTICALL3_ADDRESS, MULTICALL3_ABI, provider);
        const calls = addresses.map(addr => ({
            target: MULTICALL3_ADDRESS,
            allowFailure: true,
            callData: multicall.interface.encodeFunctionData("getEthBalance", [addr])
        }));
        const results = await multicall.aggregate3(calls);
        return results.map(res => {
            if (res.success) {
                const bal = multicall.interface.decodeFunctionResult("getEthBalance", res.returnData)[0];
                return ethers.formatEther(bal);
            }
            return "0.0";
        });
    } catch (e) {
        return addresses.map(() => "0.0");
    }
}

if (require.main === module) {
    init(); // Dijalankan manual via terminal
} else {

    // Diexport agar bisa dipanggil langsung dari memori oleh patchright.js
    module.exports = {
        getBatchBalances, // 🔥 EXPORT MULTICALL ENGINE
        initModule: (chainKey = 'base') => {
            if (typeof applyNetwork === 'function') applyNetwork(chainKey);
            connectRPC();
            AUTO_MODE = true; // Wajib otomatis tanda tangan kalau dari In-Memory
        },
        handleInMemoryRequest,
        getWalletsDB: () => loadDB(),
        isConnected: () => RPC_CONNECTED
    };
}
