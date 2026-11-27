package com.termux.telegram;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Native TDLib (Telegram Database Library) singleton engine & IPC JSON bridge.
 * - Lazy initialization (0 MB RAM when Telegram is not used)
 * - Single MTProto connection shared by UI & TermuxIpcServer/CLI (eliminates AUTH_KEY_DUPLICATED)
 * - Real-time event stream to UI & /tmp/tdlib_events.jsonl
 */
public final class FusionTelegramHelper {
    private static final String TAG = "FusionTelegramHelper";
    private static final String PREFS_NAME = "fusion_tdlib_prefs";
    private static final String KEY_API_ID = "api_id";
    private static final String KEY_API_HASH = "api_hash";
    private static final String KEY_TAB_POS = "tab_pos"; // "bottom" or "top"
    private static final String KEY_ACTIVE_ACC = "active_account";
    private static final String KEY_HIDDEN_TABS = "hidden_tabs";
    private static final int MAX_RECENT_EVENTS = 200;

    private static final class AccountSlot {
        final String id;
        volatile Client client;
        volatile String authState = "uninitialized";
        volatile TdApi.User meUser;
        volatile boolean initialized = false;
        final Map<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
        final List<TdApi.ChatFilterInfo> chatFilters = new CopyOnWriteArrayList<>();
        AccountSlot(String id) { this.id = id; }
    }

    private static final Map<String, AccountSlot> sAccounts = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> sTargetAccountOverride = new ThreadLocal<>();
    private static volatile String sActiveAccountId = "default";
    private static volatile Context sAppContext;

    private static final List<JSONObject> sRecentEvents = new CopyOnWriteArrayList<>();
    private static final List<TelegramUiListener> sListeners = new CopyOnWriteArrayList<>();
    private static final List<JSONObject> sRecordedSteps = new CopyOnWriteArrayList<>();
    private static volatile boolean sIsRecording = false;
    private static volatile String sRecordTarget = "";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final ExecutorService sEventIoExecutor = Executors.newSingleThreadExecutor();

    private static String normalizeAccountId(String id) {
        if (id == null || id.trim().isEmpty()) return "default";
        String clean = id.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.isEmpty() ? "default" : clean;
    }

    private static AccountSlot getOrCreateSlot(String accId) {
        String key = normalizeAccountId(accId);
        return sAccounts.computeIfAbsent(key, AccountSlot::new);
    }

    private static AccountSlot activeSlot() {
        String ov = sTargetAccountOverride.get();
        return getOrCreateSlot(ov != null ? ov : sActiveAccountId);
    }

    public static void setThreadAccountOverride(String accId) {
        if (accId == null || accId.trim().isEmpty()) {
            sTargetAccountOverride.remove();
        } else {
            String key = normalizeAccountId(accId);
            sTargetAccountOverride.set(key);
            if (sAppContext != null) ensureStartedFor(sAppContext, key);
        }
    }

    public static void clearThreadAccountOverride() {
        sTargetAccountOverride.remove();
    }

    public static String getActiveAccountId() {
        return sActiveAccountId;
    }

    public interface TelegramUiListener {
        void onAuthStateChanged(String state);
        void onChatsUpdated();
        void onNewMessage(long chatId, TdApi.Message message);
        void onFiltersUpdated();
    }

    private FusionTelegramHelper() {
    }

    public static void addListener(TelegramUiListener listener) {
        if (listener != null && !sListeners.contains(listener)) {
            sListeners.add(listener);
        }
    }

    public static void removeListener(TelegramUiListener listener) {
        sListeners.remove(listener);
    }

    public static String getTabPosition(Context ctx) {
        if (ctx == null) return "bottom";
        SharedPreferences sp = ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_TAB_POS, "bottom");
    }

    public static void setTabPosition(Context ctx, String pos) {
        if (ctx == null) return;
        String clean = "top".equalsIgnoreCase(pos) ? "top" : "bottom";
        ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TAB_POS, clean).apply();
    }

    public static int getApiId(Context ctx) {
        if (ctx == null) return 0;
        return ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_API_ID, 0);
    }

    public static String getApiHash(Context ctx) {
        if (ctx == null) return "";
        return ctx.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_API_HASH, "");
    }

    public static void setApiCredentials(Context ctx, int apiId, String apiHash) {
        if (ctx == null) return;
        sAppContext = ctx.getApplicationContext();
        sAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_API_ID, apiId)
            .putString(KEY_API_HASH, apiHash != null ? apiHash.trim() : "")
            .apply();
        AccountSlot slot = activeSlot();
        if ("wait_credentials".equals(slot.authState) || "uninitialized".equals(slot.authState)) {
            if (slot.client == null) {
                ensureStartedFor(sAppContext, slot.id);
            } else {
                sendTdlibParameters(slot);
            }
        }
    }

    public static synchronized boolean ensureStarted(Context ctx) {
        if (ctx != null) {
            sAppContext = ctx.getApplicationContext();
            String saved = sAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACTIVE_ACC, "default");
            sActiveAccountId = normalizeAccountId(saved);
        }
        return ensureStartedFor(sAppContext, sActiveAccountId);
    }

    public static synchronized boolean ensureStartedFor(Context ctx, String accId) {
        if (ctx != null) {
            sAppContext = ctx.getApplicationContext();
        }
        AccountSlot slot = getOrCreateSlot(accId);
        if (slot.client != null) {
            return true;
        }
        if (sAppContext == null) {
            return false;
        }
        try {
            try {
                Client.execute(new TdApi.SetLogVerbosityLevel(1));
            } catch (Throwable ignored) {}

            slot.client = Client.create(
                obj -> onTdUpdate(slot, obj),
                e -> Log.w(TAG, "TDLib update exception (" + slot.id + ")", e),
                e -> Log.w(TAG, "TDLib default exception (" + slot.id + ")", e)
            );
            slot.initialized = true;
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to initialize TDLib client (" + slot.id + ")", t);
            slot.authState = "error: " + t.getMessage();
            notifyAuthChanged();
            return false;
        }
    }

    public static JSONObject switchAccount(Context ctx, String accId) {
        if (ctx != null) sAppContext = ctx.getApplicationContext();
        String key = normalizeAccountId(accId);
        sActiveAccountId = key;
        if (sAppContext != null) {
            sAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_ACTIVE_ACC, key).apply();
        }
        ensureStartedFor(sAppContext, key);
        notifyAuthChanged();
        notifyChatsUpdated();
        notifyFiltersUpdated();
        return getStatusJson();
    }

    public static JSONObject listAccountsJson() {
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            if (sAppContext != null) {
                File filesDir = sAppContext.getFilesDir();
                File defDir = new File(filesDir, "tdlib_db");
                if (defDir.exists()) getOrCreateSlot("default");
                File[] children = filesDir.listFiles();
                if (children != null) {
                    for (File f : children) {
                        if (f.isDirectory() && f.getName().startsWith("tdlib_db_")) {
                            getOrCreateSlot(f.getName().substring("tdlib_db_".length()));
                        }
                    }
                }
            }
            for (AccountSlot slot : sAccounts.values()) {
                JSONObject item = new JSONObject();
                item.put("id", slot.id);
                item.put("active", slot.id.equals(sActiveAccountId));
                item.put("authState", slot.authState);
                item.put("ready", "ready".equals(slot.authState));
                if (slot.meUser != null) {
                    String uname = (slot.meUser.usernames != null && slot.meUser.usernames.activeUsernames != null && slot.meUser.usernames.activeUsernames.length > 0)
                        ? slot.meUser.usernames.activeUsernames[0] : "";
                    item.put("username", uname);
                    item.put("phone", slot.meUser.phoneNumber);
                }
                arr.put(item);
            }
            out.put("ok", true);
            out.put("activeAccount", sActiveAccountId);
            out.put("accounts", arr);
        } catch (Throwable ignored) {}
        return out;
    }

    public static String getAuthState() {
        return activeSlot().authState;
    }

    public static boolean isReady() {
        AccountSlot slot = activeSlot();
        return "ready".equals(slot.authState) && slot.client != null;
    }

    private static void onTdUpdate(AccountSlot slot, TdApi.Object object) {
        if (object == null || slot == null) return;
        int ctor = object.getConstructor();
        switch (ctor) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR: {
                TdApi.AuthorizationState state = ((TdApi.UpdateAuthorizationState) object).authorizationState;
                handleAuthorizationState(slot, state);
                break;
            }
            case TdApi.UpdateNewChat.CONSTRUCTOR: {
                TdApi.Chat chat = ((TdApi.UpdateNewChat) object).chat;
                if (chat != null) {
                    slot.chats.put(chat.id, chat);
                    if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
                }
                break;
            }
            case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                TdApi.UpdateChatTitle u = (TdApi.UpdateChatTitle) object;
                TdApi.Chat c = slot.chats.get(u.chatId);
                if (c != null) {
                    c.title = u.title;
                    if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
                }
                break;
            }
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                TdApi.UpdateChatLastMessage u = (TdApi.UpdateChatLastMessage) object;
                TdApi.Chat c = slot.chats.get(u.chatId);
                if (c != null) {
                    c.lastMessage = u.lastMessage;
                    if (u.positions != null) c.positions = u.positions;
                    if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
                }
                break;
            }
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                TdApi.UpdateChatReadInbox u = (TdApi.UpdateChatReadInbox) object;
                TdApi.Chat c = slot.chats.get(u.chatId);
                if (c != null) {
                    c.lastReadInboxMessageId = u.lastReadInboxMessageId;
                    c.unreadCount = u.unreadCount;
                    if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
                }
                break;
            }
            case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                TdApi.UpdateChatPosition u = (TdApi.UpdateChatPosition) object;
                TdApi.Chat c = slot.chats.get(u.chatId);
                if (c != null && u.position != null) {
                    updateChatPosition(c, u.position);
                    if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
                }
                break;
            }
            case TdApi.UpdateChatFilters.CONSTRUCTOR: {
                TdApi.UpdateChatFilters u = (TdApi.UpdateChatFilters) object;
                slot.chatFilters.clear();
                if (u.chatFilters != null) {
                    Collections.addAll(slot.chatFilters, u.chatFilters);
                }
                if (slot.id.equals(sActiveAccountId)) notifyFiltersUpdated();
                break;
            }
            case TdApi.UpdateNewMessage.CONSTRUCTOR: {
                TdApi.UpdateNewMessage u = (TdApi.UpdateNewMessage) object;
                if (u.message != null) {
                    recordNewMessageEvent(slot, u.message);
                    if (slot.id.equals(sActiveAccountId)) notifyNewMessage(u.message.chatId, u.message);
                }
                break;
            }
            default:
                break;
        }
    }

    private static void updateChatPosition(TdApi.Chat chat, TdApi.ChatPosition newPos) {
        if (chat.positions == null) {
            chat.positions = new TdApi.ChatPosition[]{ newPos };
            return;
        }
        List<TdApi.ChatPosition> list = new ArrayList<>();
        boolean replaced = false;
        for (TdApi.ChatPosition p : chat.positions) {
            if (p.list != null && newPos.list != null && p.list.getConstructor() == newPos.list.getConstructor()) {
                if (newPos.order != 0) list.add(newPos);
                replaced = true;
            } else {
                list.add(p);
            }
        }
        if (!replaced && newPos.order != 0) {
            list.add(newPos);
        }
        chat.positions = list.toArray(new TdApi.ChatPosition[0]);
    }

    private static void handleAuthorizationState(AccountSlot slot, TdApi.AuthorizationState state) {
        if (state == null || slot == null) return;
        switch (state.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                sendTdlibParameters(slot);
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                slot.authState = "wait_phone";
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                slot.authState = "wait_code";
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR:
                slot.authState = "wait_password";
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                slot.authState = "ready";
                fetchMeAndInitialChats(slot);
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                slot.authState = "logging_out";
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                slot.authState = "closed";
                slot.client = null;
                slot.meUser = null;
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
                break;
            default:
                break;
        }
    }

    private static void sendTdlibParameters(AccountSlot slot) {
        if (slot == null || slot.client == null || sAppContext == null) return;
        int apiId = getApiId(sAppContext);
        String apiHash = getApiHash(sAppContext);
        if (apiId <= 0 || apiHash.isEmpty()) {
            slot.authState = "wait_credentials";
            if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
            return;
        }
        String suffix = "default".equals(slot.id) ? "" : ("_" + slot.id);
        File dbDir = new File(sAppContext.getFilesDir(), "tdlib_db" + suffix);
        File filesDir = new File(sAppContext.getFilesDir(), "tdlib_files" + suffix);
        if (!dbDir.exists()) dbDir.mkdirs();
        if (!filesDir.exists()) filesDir.mkdirs();

        TdApi.SetTdlibParameters params = new TdApi.SetTdlibParameters();
        params.databaseDirectory = dbDir.getAbsolutePath();
        params.filesDirectory = filesDir.getAbsolutePath();
        params.useMessageDatabase = true;
        params.useSecretChats = true;
        params.useChatInfoDatabase = true;
        params.useFileDatabase = true;
        params.apiId = apiId;
        params.apiHash = apiHash;
        params.systemLanguageCode = "en";
        params.deviceModel = Build.MODEL != null ? Build.MODEL : "Android";
        params.systemVersion = Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "14";
        params.applicationVersion = "1.0";

        slot.client.send(params, res -> {
            if (res instanceof TdApi.Error) {
                TdApi.Error err = (TdApi.Error) res;
                Log.e(TAG, "SetTdlibParameters error (" + slot.id + "): " + err.code + " " + err.message);
                slot.authState = "error: " + err.message;
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
            }
        });
    }

    private static void fetchMeAndInitialChats(AccountSlot slot) {
        if (slot == null || slot.client == null) return;
        slot.client.send(new TdApi.GetMe(), res -> {
            if (res instanceof TdApi.User) {
                slot.meUser = (TdApi.User) res;
                if (slot.id.equals(sActiveAccountId)) notifyAuthChanged();
            }
        });
        slot.client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), 50), res -> {
            if (slot.id.equals(sActiveAccountId)) notifyChatsUpdated();
        });
    }

    public static JSONObject sendPhoneNumber(String phone) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        return sendSync(new TdApi.SetAuthenticationPhoneNumber(phone != null ? phone.trim() : "", null), 10000);
    }

    public static JSONObject sendAuthCode(String code) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        return sendSync(new TdApi.CheckAuthenticationCode(code != null ? code.trim() : ""), 10000);
    }

    public static JSONObject sendPassword(String password) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        return sendSync(new TdApi.CheckAuthenticationPassword(password != null ? password : ""), 10000);
    }

    public static JSONObject logout() {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        return sendSync(new TdApi.LogOut(), 10000);
    }

    public static JSONObject getStatusJson() {
        AccountSlot slot = activeSlot();
        JSONObject jo = new JSONObject();
        try {
            jo.put("ok", true);
            jo.put("accountId", slot.id);
            jo.put("initialized", slot.initialized && slot.client != null);
            jo.put("authState", slot.authState);
            jo.put("chatCount", slot.chats.size());
            jo.put("filterCount", slot.chatFilters.size());
            if (slot.meUser != null) {
                JSONObject me = new JSONObject();
                me.put("id", slot.meUser.id);
                me.put("firstName", slot.meUser.firstName);
                me.put("lastName", slot.meUser.lastName);
                String uname = (slot.meUser.usernames != null && slot.meUser.usernames.activeUsernames != null && slot.meUser.usernames.activeUsernames.length > 0)
                    ? slot.meUser.usernames.activeUsernames[0] : "";
                me.put("username", uname);
                me.put("phone", slot.meUser.phoneNumber);
                jo.put("me", me);
            }
        } catch (Throwable ignored) {}
        return jo;
    }

    public static List<TdApi.ChatFilterInfo> getChatFilters() {
        return new ArrayList<>(activeSlot().chatFilters);
    }

    public static List<TdApi.Chat> getSortedChats(String builtInTab, int folderId) {
        List<TdApi.Chat> list = new ArrayList<>();
        for (TdApi.Chat c : activeSlot().chats.values()) {
            if (c == null) continue;
            if (folderId > 0) {
                if (!chatInFolder(c, folderId)) continue;
            } else if (builtInTab != null) {
                if ("unread".equalsIgnoreCase(builtInTab) && c.unreadCount <= 0) continue;
                if ("channels".equalsIgnoreCase(builtInTab) && !isChannelChat(c)) continue;
                if ("groups".equalsIgnoreCase(builtInTab) && !isGroupChat(c)) continue;
                if ("bots".equalsIgnoreCase(builtInTab) && !isBotOrPrivateChat(c)) continue;
            }
            list.add(c);
        }
        Collections.sort(list, (a, b) -> {
            long oa = getMainOrder(a);
            long ob = getMainOrder(b);
            return Long.compare(ob, oa);
        });
        return list;
    }

    private static boolean chatInFolder(TdApi.Chat c, int folderId) {
        if (c.positions == null) return false;
        for (TdApi.ChatPosition p : c.positions) {
            if (p.list instanceof TdApi.ChatListFolder) {
                if (((TdApi.ChatListFolder) p.list).chatFolderId == folderId && p.order != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long getMainOrder(TdApi.Chat c) {
        if (c == null || c.positions == null) return 0;
        for (TdApi.ChatPosition p : c.positions) {
            if (p.list instanceof TdApi.ChatListMain) {
                return p.order;
            }
        }
        return c.lastMessage != null ? c.lastMessage.date : 0;
    }

    public static boolean isChannelChat(TdApi.Chat c) {
        return c.type instanceof TdApi.ChatTypeSupergroup && ((TdApi.ChatTypeSupergroup) c.type).isChannel;
    }

    public static boolean isGroupChat(TdApi.Chat c) {
        if (c.type instanceof TdApi.ChatTypeBasicGroup) return true;
        return c.type instanceof TdApi.ChatTypeSupergroup && !((TdApi.ChatTypeSupergroup) c.type).isChannel;
    }

    public static boolean isBotOrPrivateChat(TdApi.Chat c) {
        return c.type instanceof TdApi.ChatTypePrivate || c.type instanceof TdApi.ChatTypeSecret;
    }

    public static void loadMoreChats(int limit) {
        Client c = activeSlot().client;
        if (c == null) return;
        c.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit), res -> notifyChatsUpdated());
    }

    public static JSONObject getChatsJson(String filter, int limit) {
        AccountSlot slot = activeSlot();
        if (slot.client != null && slot.chats.size() < limit) {
            sendSync(new TdApi.LoadChats(new TdApi.ChatListMain(), limit), 4000);
        }
        List<TdApi.Chat> sorted = getSortedChats(filter, 0);
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            int count = Math.min(sorted.size(), limit > 0 ? limit : 30);
            for (int i = 0; i < count; i++) {
                TdApi.Chat c = sorted.get(i);
                JSONObject item = new JSONObject();
                item.put("id", c.id);
                item.put("title", c.title != null ? c.title : "");
                item.put("unreadCount", c.unreadCount);
                item.put("type", isChannelChat(c) ? "channel" : (isGroupChat(c) ? "group" : "private"));
                if (c.lastMessage != null) {
                    item.put("lastDate", c.lastMessage.date);
                    item.put("lastText", extractMessageText(c.lastMessage));
                }
                arr.put(item);
            }
            out.put("ok", true);
            out.put("chats", arr);
        } catch (Throwable ignored) {}
        return out;
    }

    public static List<TdApi.Message> fetchHistory(long chatId, long fromMessageId, int limit) {
        if (activeSlot().client == null) return Collections.emptyList();
        TdApi.Object res = sendSyncRaw(new TdApi.GetChatHistory(chatId, fromMessageId, 0, limit > 0 ? limit : 40, false), 6000);
        List<TdApi.Message> list = new ArrayList<>();
        if (res instanceof TdApi.Messages && ((TdApi.Messages) res).messages != null) {
            Collections.addAll(list, ((TdApi.Messages) res).messages);
        }
        return list;
    }

    public static JSONObject getHistoryJson(long chatId, long fromMessageId, int limit) {
        List<TdApi.Message> msgs = fetchHistory(chatId, fromMessageId, limit);
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            for (TdApi.Message m : msgs) {
                arr.put(messageToJson(m));
            }
            out.put("ok", true);
            out.put("chatId", chatId);
            out.put("messages", arr);
        } catch (Throwable ignored) {}
        return out;
    }

    public static JSONObject sendMessageJson(long chatId, String text, long replyToMessageId) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        TdApi.InputMessageText content = new TdApi.InputMessageText(
            new TdApi.FormattedText(text != null ? text : "", null),
            null,
            false
        );
        TdApi.InputMessageReplyTo replyTo = replyToMessageId > 0
            ? new TdApi.InputMessageReplyToMessage(replyToMessageId, null, 0)
            : null;
        TdApi.SendMessage req = new TdApi.SendMessage(chatId, null, replyTo, null, null, content);
        TdApi.Object res = sendSyncRaw(req, 8000);
        if (res instanceof TdApi.Message) {
            JSONObject step = new JSONObject();
            try {
                step.put("action", "send_message");
                step.put("chatId", chatId);
                step.put("text", text != null ? text : "");
            } catch (Throwable ignored) {}
            recordStep(step);
            JSONObject out = new JSONObject();
            try {
                out.put("ok", true);
                out.put("message", messageToJson((TdApi.Message) res));
            } catch (Throwable ignored) {}
            return out;
        }
        return tdErrorOrOk(res);
    }

    public static JSONObject clickInlineButtonJson(long chatId, long messageId, String callbackData) {
        return clickInlineButtonJson(chatId, messageId, callbackData, "", -1);
    }

    public static JSONObject clickInlineButtonJson(long chatId, long messageId, String callbackData, String btnText, int btnIndex) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        byte[] dataBytes;
        try {
            dataBytes = Base64.decode(callbackData, Base64.DEFAULT);
        } catch (Throwable t) {
            dataBytes = callbackData != null ? callbackData.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }
        TdApi.GetCallbackQueryAnswer req = new TdApi.GetCallbackQueryAnswer(
            chatId,
            messageId,
            new TdApi.CallbackQueryPayloadData(dataBytes)
        );
        TdApi.Object res = sendSyncRaw(req, 8000);
        if (res instanceof TdApi.CallbackQueryAnswer) {
            JSONObject step = new JSONObject();
            try {
                step.put("action", "click_button");
                step.put("chatId", chatId);
                if (btnIndex >= 0) step.put("index", btnIndex);
                if (btnText != null && !btnText.isEmpty()) step.put("text", btnText);
                step.put("data", callbackData != null ? callbackData : "");
            } catch (Throwable ignored) {}
            recordStep(step);
            TdApi.CallbackQueryAnswer ans = (TdApi.CallbackQueryAnswer) res;
            JSONObject out = new JSONObject();
            try {
                out.put("ok", true);
                out.put("text", ans.text != null ? ans.text : "");
                out.put("showAlert", ans.showAlert);
                out.put("url", ans.url != null ? ans.url : "");
            } catch (Throwable ignored) {}
            return out;
        }
        return tdErrorOrOk(res);
    }

    public static JSONObject openWebAppJson(long chatId, long botUserId, String url) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        TdApi.WebAppOpenParameters params = new TdApi.WebAppOpenParameters(
            new TdApi.ThemeParameters(),
            "android",
            TdApi.WebAppOpenModeFullSize.CONSTRUCTOR == 0 ? null : new TdApi.WebAppOpenModeFullSize()
        );
        TdApi.OpenWebApp req = new TdApi.OpenWebApp(chatId, botUserId, url != null ? url : "", params);
        TdApi.Object res = sendSyncRaw(req, 8000);
        if (res instanceof TdApi.WebAppInfo) {
            TdApi.WebAppInfo info = (TdApi.WebAppInfo) res;
            JSONObject step = new JSONObject();
            try {
                step.put("action", "open_webapp");
                step.put("chatId", chatId);
                step.put("botUserId", botUserId);
                step.put("url", url != null ? url : "");
            } catch (Throwable ignored) {}
            recordStep(step);
            JSONObject out = new JSONObject();
            try {
                out.put("ok", true);
                out.put("launchId", info.launchId);
                out.put("url", info.url != null ? info.url : "");
            } catch (Throwable ignored) {}
            return out;
        }
        return tdErrorOrOk(res);
    }

    public static JSONObject searchMessagesJson(long chatId, String query, int limit) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        TdApi.Object res;
        int lim = limit > 0 ? limit : 25;
        if (chatId != 0) {
            res = sendSyncRaw(new TdApi.SearchChatMessages(chatId, null, query != null ? query : "", null, 0, 0, lim, null), 8000);
            if (res instanceof TdApi.FoundChatMessages) {
                return messagesArrayToJson(((TdApi.FoundChatMessages) res).messages);
            }
        } else {
            res = sendSyncRaw(new TdApi.SearchMessages(null, query != null ? query : "", "", lim, null, null, 0, 0), 8000);
            if (res instanceof TdApi.FoundMessages) {
                return messagesArrayToJson(((TdApi.FoundMessages) res).messages);
            }
        }
        return tdErrorOrOk(res);
    }

    public static JSONObject joinChatJson(String target) {
        AccountSlot slot = activeSlot();
        if (slot.client == null) return errorJson("TDLib not started");
        if (target == null || target.trim().isEmpty()) return errorJson("Missing chat/invite target");
        String clean = target.trim();
        if (clean.contains("t.me/+") || clean.contains("joinchat/")) {
            TdApi.Object res = sendSyncRaw(new TdApi.JoinChatByInviteLink(clean), 8000);
            if (res instanceof TdApi.Chat) {
                TdApi.Chat c = (TdApi.Chat) res;
                slot.chats.put(c.id, c);
                JSONObject step = new JSONObject();
                try {
                    step.put("action", "join_target");
                    step.put("target", clean);
                } catch (Throwable ignored) {}
                recordStep(step);
                JSONObject out = new JSONObject();
                try {
                    out.put("ok", true);
                    out.put("chatId", c.id);
                    out.put("title", c.title);
                } catch (Throwable ignored) {}
                return out;
            }
            return tdErrorOrOk(res);
        }
        String uname = clean.replace("https://t.me/", "").replace("http://t.me/", "").replace("@", "").split("[/?]")[0];
        TdApi.Object found = sendSyncRaw(new TdApi.SearchPublicChat(uname), 8000);
        if (found instanceof TdApi.Chat) {
            TdApi.Chat c = (TdApi.Chat) found;
            slot.chats.put(c.id, c);
            TdApi.Object joined = sendSyncRaw(new TdApi.JoinChat(c.id), 8000);
            if (joined instanceof TdApi.Ok) {
                JSONObject step = new JSONObject();
                try {
                    step.put("action", "join_target");
                    step.put("target", uname);
                } catch (Throwable ignored) {}
                recordStep(step);
                JSONObject out = new JSONObject();
                try {
                    out.put("ok", true);
                    out.put("chatId", c.id);
                    out.put("title", c.title);
                } catch (Throwable ignored) {}
                return out;
            }
            return tdErrorOrOk(joined);
        }
        return tdErrorOrOk(found);
    }

    public static JSONObject resolveChatJson(String target) {
        AccountSlot slot = activeSlot();
        if (slot.client == null) return errorJson("TDLib not started");
        if (target == null || target.trim().isEmpty()) return errorJson("Missing target");
        String uname = target.trim().replace("https://t.me/", "").replace("http://t.me/", "").replace("@", "").split("[/?]")[0];
        TdApi.Object found = sendSyncRaw(new TdApi.SearchPublicChat(uname), 8000);
        if (found instanceof TdApi.Chat) {
            TdApi.Chat c = (TdApi.Chat) found;
            slot.chats.put(c.id, c);
            JSONObject out = new JSONObject();
            try {
                out.put("ok", true);
                out.put("chatId", c.id);
                out.put("title", c.title != null ? c.title : "");
                if (c.type instanceof TdApi.ChatTypePrivate) {
                    out.put("userId", ((TdApi.ChatTypePrivate) c.type).userId);
                }
            } catch (Throwable ignored) {}
            return out;
        }
        return tdErrorOrOk(found);
    }

    public static JSONObject startBotJson(String botTarget, String startParam) {
        AccountSlot slot = activeSlot();
        if (slot.client == null) return errorJson("TDLib not started");
        if (botTarget == null || botTarget.trim().isEmpty()) return errorJson("Missing bot username");
        String uname = botTarget.trim().replace("https://t.me/", "").replace("http://t.me/", "").replace("@", "").split("[/?]")[0];
        TdApi.Object found = sendSyncRaw(new TdApi.SearchPublicChat(uname), 8000);
        if (!(found instanceof TdApi.Chat)) {
            return tdErrorOrOk(found);
        }
        TdApi.Chat chat = (TdApi.Chat) found;
        slot.chats.put(chat.id, chat);
        long botUserId = (chat.type instanceof TdApi.ChatTypePrivate)
            ? ((TdApi.ChatTypePrivate) chat.type).userId
            : chat.id;
        String param = startParam != null ? startParam.trim() : "";
        TdApi.Object res = sendSyncRaw(new TdApi.SendBotStartMessage(botUserId, chat.id, param), 8000);
        if (res instanceof TdApi.Message) {
            JSONObject step = new JSONObject();
            try {
                step.put("action", "start_bot");
                step.put("bot", uname);
                step.put("param", param);
                step.put("chatId", chat.id);
            } catch (Throwable ignored) {}
            recordStep(step);
            JSONObject out = new JSONObject();
            try {
                out.put("ok", true);
                out.put("chatId", chat.id);
                out.put("botUserId", botUserId);
                out.put("param", param);
                out.put("message", messageToJson((TdApi.Message) res));
            } catch (Throwable ignored) {}
            return out;
        }
        return tdErrorOrOk(res);
    }

    public static JSONObject listRecipesJson() {
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            if (sAppContext != null) {
                File f = new File(sAppContext.getFilesDir(), "tdlib_recipes.json");
                if (f.exists() && f.length() > 0) {
                    byte[] raw = java.nio.file.Files.readAllBytes(f.toPath());
                    arr = new JSONArray(new String(raw, StandardCharsets.UTF_8));
                }
            }
            out.put("ok", true);
            out.put("recipes", arr);
        } catch (Throwable t) {
            return errorJson("List recipes error: " + t.getMessage());
        }
        return out;
    }

    public static JSONObject deleteRecipeJson(String recipeName) {
        if (recipeName == null || recipeName.trim().isEmpty()) return errorJson("Missing recipe name");
        try {
            if (sAppContext == null) return errorJson("Context not ready");
            File f = new File(sAppContext.getFilesDir(), "tdlib_recipes.json");
            if (!f.exists() || f.length() == 0) return errorJson("No recipes file");
            byte[] raw = java.nio.file.Files.readAllBytes(f.toPath());
            JSONArray arr = new JSONArray(new String(raw, StandardCharsets.UTF_8));
            JSONArray next = new JSONArray();
            int removed = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject r = arr.optJSONObject(i);
                if (r != null && recipeName.trim().equalsIgnoreCase(r.optString("name", ""))) {
                    removed++;
                } else if (r != null) {
                    next.put(r);
                }
            }
            try (FileOutputStream fos = new FileOutputStream(f, false)) {
                fos.write(next.toString(2).getBytes(StandardCharsets.UTF_8));
            }
            JSONObject out = new JSONObject();
            out.put("ok", removed > 0);
            out.put("removed", removed);
            return out;
        } catch (Throwable t) {
            return errorJson("Delete recipe error: " + t.getMessage());
        }
    }

    public static JSONObject replayRecipeJson(String jsonOrName, long defaultChatId) {
        if (activeSlot().client == null) return errorJson("TDLib not started");
        try {
            JSONArray steps = null;
            long targetChatId = defaultChatId;
            String trimmed = jsonOrName != null ? jsonOrName.trim() : "";
            if (trimmed.startsWith("[")) {
                steps = new JSONArray(trimmed);
            } else if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                steps = obj.optJSONArray("steps");
                if (targetChatId == 0) targetChatId = obj.optLong("chatId", 0);
            } else if (!trimmed.isEmpty()) {
                JSONObject all = listRecipesJson();
                JSONArray arr = all.optJSONArray("recipes");
                if (arr != null) {
                    for (int i = arr.length() - 1; i >= 0; i--) {
                        JSONObject r = arr.optJSONObject(i);
                        if (r != null && trimmed.equalsIgnoreCase(r.optString("name", ""))) {
                            steps = r.optJSONArray("steps");
                            break;
                        }
                    }
                }
            }
            if (steps == null || steps.length() == 0) {
                return errorJson("Recipe not found or empty steps");
            }
            int executed = 0;
            for (int i = 0; i < steps.length(); i++) {
                JSONObject st = steps.optJSONObject(i);
                if (st == null) continue;
                String action = st.optString("action", "");
                long cId = st.optLong("chatId", targetChatId);
                if ("start_bot".equals(action)) {
                    JSONObject r = startBotJson(st.optString("bot", ""), st.optString("param", ""));
                    if (r.optBoolean("ok", false) && targetChatId == 0) {
                        targetChatId = r.optLong("chatId", 0);
                    }
                } else if ("send_message".equals(action) || "send_text".equals(action)) {
                    if (cId != 0) sendMessageJson(cId, st.optString("text", ""), 0);
                } else if ("join_target".equals(action)) {
                    joinChatJson(st.optString("target", ""));
                } else if ("click_button".equals(action)) {
                    if (cId != 0) {
                        List<TdApi.Message> hist = fetchHistory(cId, 0, 5);
                        if (!hist.isEmpty()) {
                            TdApi.Message latest = hist.get(0);
                            clickInlineButtonJson(cId, latest.id, st.optString("data", ""), st.optString("text", ""), st.optInt("index", -1));
                        }
                    }
                } else if ("open_webapp".equals(action)) {
                    if (cId != 0) openWebAppJson(cId, st.optLong("botUserId", 0), st.optString("url", ""));
                }
                executed++;
                try { Thread.sleep(650); } catch (InterruptedException ignored) {}
            }
            JSONObject out = new JSONObject();
            out.put("ok", true);
            out.put("executed", executed);
            return out;
        } catch (Throwable t) {
            return errorJson("Replay error: " + t.getMessage());
        }
    }

    public static void markChatRead(long chatId, long messageId) {
        Client c = activeSlot().client;
        if (c == null || chatId == 0 || messageId == 0) return;
        c.send(new TdApi.ViewMessages(chatId, new long[]{ messageId }, new TdApi.MessageSourceChatHistory(), true), res -> {});
    }

    public static boolean isRecording() {
        return sIsRecording;
    }

    public static int getRecordedCount() {
        return sRecordedSteps.size();
    }

    public static void startRecording(String targetBot) {
        sRecordedSteps.clear();
        sRecordTarget = targetBot != null ? targetBot : "";
        sIsRecording = true;
    }

    public static void recordStep(JSONObject step) {
        if (!sIsRecording || step == null) return;
        sRecordedSteps.add(step);
    }

    public static JSONObject stopRecordingAndSave(String recipeName) {
        sIsRecording = false;
        String name = (recipeName == null || recipeName.trim().isEmpty())
            ? ("tg_rec_" + (System.currentTimeMillis() / 1000L))
            : recipeName.trim();
        JSONObject out = new JSONObject();
        JSONArray stepsArr = new JSONArray();
        try {
            for (JSONObject s : sRecordedSteps) {
                stepsArr.put(s);
            }
            out.put("ok", true);
            out.put("name", name);
            out.put("target_bot", sRecordTarget);
            out.put("engine_type", "tdlib");
            out.put("steps", stepsArr);
            out.put("created_at", System.currentTimeMillis() / 1000L);

            if (sAppContext != null) {
                File dir = sAppContext.getFilesDir();
                File f = new File(dir, "tdlib_recipes.json");
                JSONArray dbArr = new JSONArray();
                if (f.exists() && f.length() > 0) {
                    try {
                        byte[] raw = java.nio.file.Files.readAllBytes(f.toPath());
                        dbArr = new JSONArray(new String(raw, StandardCharsets.UTF_8));
                    } catch (Throwable ignored) {}
                }
                dbArr.put(out);
                try (FileOutputStream fos = new FileOutputStream(f, false)) {
                    fos.write(dbArr.toString(2).getBytes(StandardCharsets.UTF_8));
                }
            }
            try (FileOutputStream fos = new FileOutputStream(new File("/tmp/tdlib_last_recipe.json"), false)) {
                fos.write(out.toString(2).getBytes(StandardCharsets.UTF_8));
            } catch (Throwable ignored) {}
        } catch (Throwable t) {
            return errorJson("Save recipe error: " + t.getMessage());
        }
        return out;
    }

    public static JSONObject getRecentEventsJson(int limit) {
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            int size = sRecentEvents.size();
            int start = Math.max(0, size - (limit > 0 ? limit : 30));
            for (int i = start; i < size; i++) {
                arr.put(sRecentEvents.get(i));
            }
            out.put("ok", true);
            out.put("events", arr);
        } catch (Throwable ignored) {}
        return out;
    }

    private static JSONObject messagesArrayToJson(TdApi.Message[] messages) {
        JSONObject out = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            if (messages != null) {
                for (TdApi.Message m : messages) {
                    arr.put(messageToJson(m));
                }
            }
            out.put("ok", true);
            out.put("messages", arr);
        } catch (Throwable ignored) {}
        return out;
    }

    public static JSONObject messageToJson(TdApi.Message m) {
        JSONObject jo = new JSONObject();
        if (m == null) return jo;
        try {
            jo.put("id", m.id);
            jo.put("chatId", m.chatId);
            jo.put("date", m.date);
            jo.put("isOutgoing", m.isOutgoing);
            jo.put("text", extractMessageText(m));
            if (m.senderId instanceof TdApi.MessageSenderUser) {
                jo.put("senderUserId", ((TdApi.MessageSenderUser) m.senderId).userId);
            } else if (m.senderId instanceof TdApi.MessageSenderChat) {
                jo.put("senderChatId", ((TdApi.MessageSenderChat) m.senderId).chatId);
            }
            if (m.replyMarkup instanceof TdApi.ReplyMarkupInlineKeyboard) {
                TdApi.ReplyMarkupInlineKeyboard kb = (TdApi.ReplyMarkupInlineKeyboard) m.replyMarkup;
                JSONArray rows = new JSONArray();
                if (kb.rows != null) {
                    for (TdApi.InlineKeyboardButton[] row : kb.rows) {
                        JSONArray rArr = new JSONArray();
                        for (TdApi.InlineKeyboardButton btn : row) {
                            JSONObject bObj = new JSONObject();
                            bObj.put("text", btn.text);
                            if (btn.type instanceof TdApi.InlineKeyboardButtonTypeCallback) {
                                bObj.put("type", "callback");
                                bObj.put("data", Base64.encodeToString(((TdApi.InlineKeyboardButtonTypeCallback) btn.type).data, Base64.NO_WRAP));
                            } else if (btn.type instanceof TdApi.InlineKeyboardButtonTypeUrl) {
                                bObj.put("type", "url");
                                bObj.put("url", ((TdApi.InlineKeyboardButtonTypeUrl) btn.type).url);
                            } else if (btn.type instanceof TdApi.InlineKeyboardButtonTypeWebApp) {
                                bObj.put("type", "webapp");
                                bObj.put("url", ((TdApi.InlineKeyboardButtonTypeWebApp) btn.type).url);
                            }
                            rArr.put(bObj);
                        }
                        rows.put(rArr);
                    }
                }
                jo.put("inlineKeyboard", rows);
            }
        } catch (Throwable ignored) {}
        return jo;
    }

    public static String extractMessageText(TdApi.Message m) {
        if (m == null || m.content == null) return "";
        if (m.content instanceof TdApi.MessageText) {
            TdApi.MessageText mt = (TdApi.MessageText) m.content;
            return (mt.text != null && mt.text.text != null) ? mt.text.text : "";
        }
        if (m.content instanceof TdApi.MessagePhoto) {
            TdApi.MessagePhoto mp = (TdApi.MessagePhoto) m.content;
            String cap = (mp.caption != null && mp.caption.text != null) ? mp.caption.text : "";
            return cap.isEmpty() ? "[Photo]" : "[Photo] " + cap;
        }
        if (m.content instanceof TdApi.MessageDocument) {
            TdApi.MessageDocument md = (TdApi.MessageDocument) m.content;
            String fname = md.document != null && md.document.fileName != null ? md.document.fileName : "file";
            String cap = (md.caption != null && md.caption.text != null) ? md.caption.text : "";
            return cap.isEmpty() ? "[File: " + fname + "]" : "[File: " + fname + "] " + cap;
        }
        if (m.content instanceof TdApi.MessageVideo) {
            TdApi.MessageVideo mv = (TdApi.MessageVideo) m.content;
            String cap = (mv.caption != null && mv.caption.text != null) ? mv.caption.text : "";
            return cap.isEmpty() ? "[Video]" : "[Video] " + cap;
        }
        if (m.content instanceof TdApi.MessageSticker) {
            TdApi.MessageSticker ms = (TdApi.MessageSticker) m.content;
            return ms.sticker != null && ms.sticker.emoji != null ? ms.sticker.emoji + " [Sticker]" : "[Sticker]";
        }
        return "[" + m.content.getClass().getSimpleName().replace("Message", "") + "]";
    }

    private static void recordNewMessageEvent(AccountSlot slot, TdApi.Message msg) {
        JSONObject jo = messageToJson(msg);
        TdApi.Chat chat = slot != null ? slot.chats.get(msg.chatId) : null;
        try {
            if (slot != null) jo.put("accountId", slot.id);
            if (chat != null) jo.put("chatTitle", chat.title);
        } catch (Throwable ignored) {}
        sRecentEvents.add(jo);
        while (sRecentEvents.size() > MAX_RECENT_EVENTS) {
            sRecentEvents.remove(0);
        }
        final String line = jo.toString() + "\n";
        sEventIoExecutor.execute(() -> {
            try {
                File tmpDir = new File("/data/data/com.termux/files/usr/tmp");
                File f = tmpDir.exists() ? new File(tmpDir, "tdlib_events.jsonl")
                    : (sAppContext != null ? new File(sAppContext.getCacheDir(), "tdlib_events.jsonl") : new File("/tmp/tdlib_events.jsonl"));
                if (f.exists() && f.length() > 4 * 1024 * 1024) {
                    f.delete();
                }
                try (FileOutputStream fos = new FileOutputStream(f, true)) {
                    fos.write(line.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Throwable ignored) {}
        });
    }

    private static TdApi.Object sendSyncRaw(TdApi.Function req, long timeoutMs) {
        Client c = activeSlot().client;
        if (c == null) return null;
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<TdApi.Object> ref = new AtomicReference<>(null);
        c.send(req, res -> {
            ref.set(res);
            latch.countDown();
        });
        try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}
        return ref.get();
    }

    private static JSONObject sendSync(TdApi.Function req, long timeoutMs) {
        return tdErrorOrOk(sendSyncRaw(req, timeoutMs));
    }

    private static JSONObject tdErrorOrOk(TdApi.Object res) {
        JSONObject out = new JSONObject();
        try {
            if (res == null) {
                out.put("ok", false);
                out.put("error", "TDLib timeout");
            } else if (res instanceof TdApi.Error) {
                TdApi.Error err = (TdApi.Error) res;
                out.put("ok", false);
                out.put("code", err.code);
                out.put("error", err.message != null ? err.message : "TDLib error");
            } else {
                out.put("ok", true);
            }
        } catch (Throwable ignored) {}
        return out;
    }

    private static JSONObject errorJson(String msg) {
        JSONObject out = new JSONObject();
        try {
            out.put("ok", false);
            out.put("error", msg);
        } catch (Throwable ignored) {}
        return out;
    }

    private static void notifyAuthChanged() {
        final String state = activeSlot().authState;
        sMainHandler.post(() -> {
            for (TelegramUiListener l : sListeners) {
                l.onAuthStateChanged(state);
            }
        });
    }

    private static void notifyChatsUpdated() {
        sMainHandler.post(() -> {
            for (TelegramUiListener l : sListeners) {
                l.onChatsUpdated();
            }
        });
    }

    private static void notifyFiltersUpdated() {
        sMainHandler.post(() -> {
            for (TelegramUiListener l : sListeners) {
                l.onFiltersUpdated();
            }
        });
    }

    private static void notifyNewMessage(long chatId, TdApi.Message msg) {
        sMainHandler.post(() -> {
            for (TelegramUiListener l : sListeners) {
                l.onNewMessage(chatId, msg);
            }
        });
    }
}
