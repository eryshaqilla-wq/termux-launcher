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
