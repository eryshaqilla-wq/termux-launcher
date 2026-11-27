package com.termux.telegram;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.termux.app.FusionToast;
import com.termux.app.TermuxActivity;
import com.termux.app.terminal.PaneGlass;
import com.termux.app.terminal.PaneGlassBackdropView;
import com.termux.app.terminal.PaneSurfaceStyle;
import com.termux.app.terminal.TerminalWindowBar;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.webview.FusionWebViewManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.drinkless.tdlib.TdApi;
import org.json.JSONObject;

/**
 * Native Android Telegram client UI built with Java Views inside mPaneHost (exact WebView canvas size).
 * - Mirrors Termux Light/Dark/Wallpaper colors, transparency, and square/rounded corner radius
 * - Bottom-docked (configurable top/bottom) customizable chat folder tabs
 * - Auth flow, chat list, chat room with inline bot buttons & WebApp/link handoff to WebView
 */
public final class FusionTelegramView extends FrameLayout implements FusionTelegramHelper.TelegramUiListener {
    private final TermuxActivity mActivity;
    private final FusionWebViewManager mManager;

    private PaneGlassBackdropView mGlassBackdrop;
    private LinearLayout mRootColumn;
    private LinearLayout mHeaderBar;
    private TextView mHeaderTitle;
    private TextView mBackBtn;
    private TextView mAccBtn;
    private TextView mRecBtn;
    private TextView mPosToggleBtn;
    private TextView mCloseBtn;

    private TerminalWindowBar mTabBar;
    private final List<TabEntry> mTabEntries = new ArrayList<>();

    private static final class TabEntry {
        final String label;
        final String builtInKey;
        final int folderId;
        TabEntry(String label, String builtInKey, int folderId) {
            this.label = label;
            this.builtInKey = builtInKey;
            this.folderId = folderId;
        }
    }

    private FrameLayout mContentHost;
    private ScrollView mAuthContainer;
    private LinearLayout mChatListContainer;
    private LinearLayout mRoomContainer;

    private EditText mSearchInput;
    private RecyclerView mChatRecycler;
    private ChatListAdapter mChatAdapter;

    private RecyclerView mMsgRecycler;
    private MessageListAdapter mMsgAdapter;
    private EditText mMsgInput;
    private TextView mSendBtn;

    // Auth views
    private EditText mApiIdInput;
    private EditText mApiHashInput;
    private EditText mPhoneInput;
    private EditText mCodeInput;
    private EditText mPassInput;
    private TextView mAuthStatusText;
    private Button mAuthActionBtn;

    private String mActiveBuiltInTab = "all";
    private int mActiveFolderId = 0;
    private long mActiveChatId = 0;
    private String mActiveChatTitle = "";
    private String mSearchQuery = "";

    private int mBgSurface;
    private int mBgCard;
    private int mBgHeader;
    private int mTextPrimary;
    private int mTextSecondary;
    private int mAccentColor;
    private int mBubbleOutColor;
    private int mBubbleInColor;
    private int mStrokeColor;
    private float mCornerPx;
    private boolean mIsDark;

    public FusionTelegramView(@NonNull TermuxActivity activity, @NonNull FusionWebViewManager manager) {
        super(activity);
        this.mActivity = activity;
        this.mManager = manager;
        resolveTheme();
        buildUi();
        FusionTelegramHelper.addListener(this);
        FusionTelegramHelper.ensureStarted(activity);
        syncUiState();
    }

    public void resolveTheme() {
        int nightMask = mActivity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        mIsDark = (nightMask == Configuration.UI_MODE_NIGHT_YES);

        int tintStrength = 78;
        float cornerDp = 0f;
        try {
            TermuxAppSharedPreferences prefs = TermuxAppSharedPreferences.build(mActivity, false);
            if (prefs != null) {
                int dockTint = prefs.getDockTintStrength();
                if (dockTint > 0) {
                    tintStrength = Math.max(40, Math.min(98, dockTint));
                }
            }
        } catch (Throwable ignored) {}

        mCornerPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerDp, getResources().getDisplayMetrics());

        int alphaBg = Math.round(255 * (tintStrength / 100f));
        int alphaCard = Math.min(255, Math.max(235, alphaBg + 42));
        int alphaBubble = Math.min(255, Math.max(242, alphaBg + 50));
        if (mIsDark) {
            mBgSurface = Color.argb(alphaBg, 22, 24, 34);
            mBgHeader = Color.argb(alphaCard, 28, 30, 44);
            mBgCard = Color.argb(alphaCard, 36, 39, 56);
            mTextPrimary = Color.parseColor("#CDD6F4");
            mTextSecondary = Color.parseColor("#A6ADC8");
            mAccentColor = Color.parseColor("#89B4FA");
            mBubbleOutColor = Color.argb(alphaBubble, 49, 68, 102);
            mBubbleInColor = Color.argb(alphaBubble, 40, 43, 62);
            mStrokeColor = Color.argb(55, 255, 255, 255);
        } else {
            mBgSurface = Color.argb(alphaBg, 242, 244, 248);     // #f2f4f8
            mBgHeader = Color.argb(alphaCard, 232, 236, 244);   // #e8ecf4
            mBgCard = Color.argb(alphaCard, 255, 255, 255);     // #ffffff
            mTextPrimary = Color.parseColor("#1E2030");         // #1e2030
            mTextSecondary = Color.parseColor("#5C6378");       // #5c6378
            mAccentColor = Color.parseColor("#1E66F5");         // #1e66f5
            mBubbleOutColor = Color.argb(alphaBubble, 212, 231, 255); // #d4e7ff
            mBubbleInColor = Color.argb(alphaBubble, 255, 255, 255);  // #ffffff
            mStrokeColor = Color.argb(38, 30, 32, 48);
        }
        if (mGlassBackdrop != null) {
            applyTermuxGlassSurface();
        }
    }

    private GradientDrawable makeBox(int fill, float radiusPx, boolean withStroke) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fill);
        gd.setCornerRadius(radiusPx);
        if (withStroke) {
            gd.setStroke(dp(1), mStrokeColor);
        }
        return gd;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    private void buildUi() {
        mGlassBackdrop = new PaneGlassBackdropView(mActivity);
        addView(mGlassBackdrop, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        applyTermuxGlassSurface();
        setClickable(true);

        mRootColumn = new LinearLayout(mActivity);
        mRootColumn.setOrientation(LinearLayout.VERTICAL);
        addView(mRootColumn, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Header
        mHeaderBar = new LinearLayout(mActivity);
        mHeaderBar.setOrientation(LinearLayout.HORIZONTAL);
        mHeaderBar.setGravity(Gravity.CENTER_VERTICAL);
        mHeaderBar.setPadding(dp(12), dp(8), dp(10), dp(8));
        mHeaderBar.setBackground(makeBox(mBgHeader, mCornerPx * 0.8f, false));

        mBackBtn = makeHeaderBtn("‹");
        mBackBtn.setVisibility(View.GONE);
        mBackBtn.setOnClickListener(v -> exitChatRoom());
        mHeaderBar.addView(mBackBtn);

        mHeaderTitle = new TextView(mActivity);
        mHeaderTitle.setText("EsoGram");
        mHeaderTitle.setTextColor(mTextPrimary);
        mHeaderTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mHeaderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderTitle.setSingleLine(true);
        mHeaderTitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleLp.setMarginStart(dp(4));
        mHeaderBar.addView(mHeaderTitle, titleLp);

        mAccBtn = makeHeaderBtn("@" + FusionTelegramHelper.getActiveAccountId());
        mAccBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        mAccBtn.setOnClickListener(v -> showAccountSwitcherDialog());
        mHeaderBar.addView(mAccBtn);

        mRecBtn = makeHeaderBtn("● REC");
        mRecBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        mRecBtn.setOnClickListener(v -> toggleTelegramRecording());
        mHeaderBar.addView(mRecBtn);

        mPosToggleBtn = makeHeaderBtn("⇅");
        mPosToggleBtn.setOnClickListener(v -> toggleTabBarPosition());
        mHeaderBar.addView(mPosToggleBtn);

        mCloseBtn = makeHeaderBtn("✕");
        mCloseBtn.setOnClickListener(v -> mManager.hideTelegramPanel());
        mHeaderBar.addView(mCloseBtn);

        // Content host
        mContentHost = new FrameLayout(mActivity);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        buildAuthView();
        buildChatListView();
        buildRoomView();

        mContentHost.addView(mAuthContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentHost.addView(mChatListContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentHost.addView(mRoomContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Tab bar (native Termux TerminalWindowBar — 100% identical Termux tab chips & animation)
        mTabBar = new TerminalWindowBar(mActivity, null);
        mTabBar.setCreateButtonShown(false);
        mTabBar.setBackground(makeBox(mBgHeader, mCornerPx * 0.7f, true));
        mTabBar.setPadding(dp(6), dp(4), dp(6), dp(4));
        mTabBar.setChromeInk(mActivity.getChromeInk());
        mTabBar.setPlaceAccent(mActivity.getActivePlaceAccent());
        mTabBar.setSurfaceStyle(mCornerPx > 0f, mCornerPx);
        mTabBar.setOnWindowSelectedListener(index -> {
            if (index >= 0 && index < mTabEntries.size()) {
                TabEntry entry = mTabEntries.get(index);
                mActiveBuiltInTab = entry.builtInKey != null ? entry.builtInKey : "all";
                mActiveFolderId = entry.folderId;
                if (mActiveChatId != 0) exitChatRoom();
                rebuildFolderTabs();
                refreshChats();
            }
        });

        rebuildFolderTabs();
        layoutColumns(contentLp);
    }

    private void applyTermuxGlassSurface() {
        try {
            PaneSurfaceStyle style = mActivity.getPaneSurfaceStyle();
            PaneGlass.apply(style, this, mGlassBackdrop, mCornerPx);
        } catch (Throwable t) {
            setBackground(makeBox(mBgSurface, mCornerPx, true));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mGlassBackdrop != null) {
            PaneGlass.followLayout(mGlassBackdrop);
        }
    }

    private void layoutColumns(LinearLayout.LayoutParams contentLp) {
        mRootColumn.removeAllViews();
        String pos = FusionTelegramHelper.getTabPosition(mActivity);
        mRootColumn.addView(mHeaderBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int barH = dp(40);
        if ("top".equals(pos)) {
            mRootColumn.addView(mTabBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barH));
            mRootColumn.addView(mContentHost, contentLp);
        } else {
            mRootColumn.addView(mContentHost, contentLp);
            mRootColumn.addView(mTabBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barH));
        }
    }

    private void toggleTabBarPosition() {
        String cur = FusionTelegramHelper.getTabPosition(mActivity);
        String next = "top".equals(cur) ? "bottom" : "top";
        FusionTelegramHelper.setTabPosition(mActivity, next);
        layoutColumns(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        FusionToast.makeText(mActivity, "Tab EsoGram: " + ("top".equals(next) ? "Atas" : "Bawah"), 0).show();
    }

    private void showAccountSwitcherDialog() {
        org.json.JSONObject res = FusionTelegramHelper.listAccountsJson();
        org.json.JSONArray arr = res.optJSONArray("accounts");
        final java.util.List<String> ids = new java.util.ArrayList<>();
        final java.util.List<String> labels = new java.util.ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject item = arr.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "default");
                String uname = item.optString("username", "");
                String st = item.optString("authState", "");
                ids.add(id);
                labels.add(id + (uname.isEmpty() ? "" : " (@" + uname + ")") + " [" + st + "]");
            }
        }
        labels.add("+ Tambah Akun Baru");
        new android.app.AlertDialog.Builder(mActivity)
            .setTitle("Akun EsoGram (" + FusionTelegramHelper.getActiveAccountId() + ")")
            .setItems(labels.toArray(new CharSequence[0]), (d, which) -> {
                if (which < ids.size()) {
                    mActiveChatId = 0;
                    FusionTelegramHelper.switchAccount(mActivity, ids.get(which));
                    if (mAccBtn != null) mAccBtn.setText("@" + FusionTelegramHelper.getActiveAccountId());
                    syncUiState();
                } else {
                    final EditText in = makeInput("ID akun baru (misal: tuyul1)");
                    new android.app.AlertDialog.Builder(mActivity)
                        .setTitle("Tambah Slot Akun EsoGram")
                        .setView(in)
                        .setPositiveButton("Buat", (d2, w2) -> {
                            String newId = in.getText().toString().trim();
                            if (!newId.isEmpty()) {
                                mActiveChatId = 0;
                                FusionTelegramHelper.switchAccount(mActivity, newId);
                                if (mAccBtn != null) mAccBtn.setText("@" + FusionTelegramHelper.getActiveAccountId());
                                syncUiState();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                }
            })
            .show();
    }

    private void toggleTelegramRecording() {
        if (!FusionTelegramHelper.isRecording()) {
            FusionTelegramHelper.startRecording(mActiveChatTitle);
            updateRecButtonUi();
            FusionToast.makeText(mActivity, "Rekam aksi bot EsoGram dimulai", 0).show();
        } else {
            final EditText input = makeInput("Nama resep rekaman...");
            input.setText("esogram_" + System.currentTimeMillis() / 1000L);
            new android.app.AlertDialog.Builder(mActivity)
                .setTitle("Simpan Rekaman EsoGram (" + FusionTelegramHelper.getRecordedCount() + " langkah)")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    JSONObject saved = FusionTelegramHelper.stopRecordingAndSave(input.getText().toString());
                    updateRecButtonUi();
                    int cnt = saved.has("steps") ? saved.optJSONArray("steps").length() : 0;
                    FusionToast.makeText(mActivity, "Resep tersimpan (" + cnt + " langkah)", 0).show();
                })
                .setNegativeButton("Batal Rekam", (d, w) -> {
                    FusionTelegramHelper.stopRecordingAndSave("");
                    updateRecButtonUi();
                })
                .show();
        }
    }

    private void updateRecButtonUi() {
        if (mRecBtn == null) return;
        if (FusionTelegramHelper.isRecording()) {
            mRecBtn.setText("■ " + FusionTelegramHelper.getRecordedCount());
            mRecBtn.setTextColor(Color.parseColor("#EF4444"));
        } else {
            mRecBtn.setText("● REC");
            mRecBtn.setTextColor(mTextPrimary);
        }
    }

    private TextView makeHeaderBtn(String label) {
        TextView tv = new TextView(mActivity);
        tv.setText(label);
        tv.setTextColor(mTextPrimary);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(4), dp(10), dp(4));
        return tv;
    }

    private void buildAuthView() {
        mAuthContainer = new ScrollView(mActivity);
        LinearLayout box = new LinearLayout(mActivity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(20), dp(18), dp(20));

        mAuthStatusText = new TextView(mActivity);
        mAuthStatusText.setTextColor(mTextPrimary);
        mAuthStatusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mAuthStatusText.setPadding(0, 0, 0, dp(12));
        box.addView(mAuthStatusText);

        mApiIdInput = makeInput("API ID (my.telegram.org)");
        mApiHashInput = makeInput("API Hash");
        mPhoneInput = makeInput("Nomor HP (+628...)");
        mCodeInput = makeInput("Kode OTP EsoGram");
        mPassInput = makeInput("Password 2FA");

        int savedId = FusionTelegramHelper.getApiId(mActivity);
        String savedHash = FusionTelegramHelper.getApiHash(mActivity);
        if (savedId > 0) mApiIdInput.setText(String.valueOf(savedId));
        if (!savedHash.isEmpty()) mApiHashInput.setText(savedHash);

        box.addView(mApiIdInput, lpWithMargin(dp(8)));
        box.addView(mApiHashInput, lpWithMargin(dp(8)));
        box.addView(mPhoneInput, lpWithMargin(dp(8)));
        box.addView(mCodeInput, lpWithMargin(dp(8)));
        box.addView(mPassInput, lpWithMargin(dp(12)));

        mAuthActionBtn = new Button(mActivity);
        mAuthActionBtn.setText("Lanjut Login TDLib");
        mAuthActionBtn.setTextColor(Color.WHITE);
        mAuthActionBtn.setBackground(makeBox(mAccentColor, mCornerPx * 0.7f, false));
        mAuthActionBtn.setOnClickListener(v -> submitAuthStep());
        box.addView(mAuthActionBtn);

        mAuthContainer.addView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private LinearLayout.LayoutParams lpWithMargin(int bottomDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = bottomDp;
        return lp;
    }

    private EditText makeInput(String hint) {
        EditText et = new EditText(mActivity);
        et.setHint(hint);
        et.setHintTextColor(mTextSecondary);
        et.setTextColor(mTextPrimary);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        et.setBackground(makeBox(mBgCard, mCornerPx * 0.7f, true));
        et.setSingleLine(true);
        return et;
    }

    private void submitAuthStep() {
        String st = FusionTelegramHelper.getAuthState();
        if ("wait_credentials".equals(st) || "uninitialized".equals(st) || st.startsWith("error:")) {
            try {
                int id = Integer.parseInt(mApiIdInput.getText().toString().trim());
                String hash = mApiHashInput.getText().toString().trim();
                FusionTelegramHelper.setApiCredentials(mActivity, id, hash);
            } catch (Throwable t) {
                FusionToast.makeText(mActivity, "API ID harus angka", 0).show();
            }
            return;
        }
        new Thread(() -> {
            JSONObject res;
            if ("wait_phone".equals(st)) {
                res = FusionTelegramHelper.sendPhoneNumber(mPhoneInput.getText().toString());
            } else if ("wait_code".equals(st)) {
                res = FusionTelegramHelper.sendAuthCode(mCodeInput.getText().toString());
            } else if ("wait_password".equals(st)) {
                res = FusionTelegramHelper.sendPassword(mPassInput.getText().toString());
            } else {
                return;
            }
            if (res != null && !res.optBoolean("ok", false)) {
                String err = res.optString("error", "Auth error");
                mActivity.runOnUiThread(() -> FusionToast.makeText(mActivity, err, 1).show());
            }
        }).start();
    }

    private void buildChatListView() {
        mChatListContainer = new LinearLayout(mActivity);
        mChatListContainer.setOrientation(LinearLayout.VERTICAL);

        mSearchInput = makeInput("Cari chat / channel...");
        mSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            mSearchQuery = mSearchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
            refreshChats();
            return true;
        });
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.setMargins(dp(10), dp(8), dp(10), dp(6));
        mChatListContainer.addView(mSearchInput, sLp);

        mChatRecycler = new RecyclerView(mActivity);
        mChatRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
        mChatAdapter = new ChatListAdapter();
        mChatRecycler.setAdapter(mChatAdapter);
        mChatListContainer.addView(mChatRecycler, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private void buildRoomView() {
        mRoomContainer = new LinearLayout(mActivity);
        mRoomContainer.setOrientation(LinearLayout.VERTICAL);

        mMsgRecycler = new RecyclerView(mActivity);
        LinearLayoutManager lm = new LinearLayoutManager(mActivity);
        lm.setStackFromEnd(true);
        mMsgRecycler.setLayoutManager(lm);
        mMsgAdapter = new MessageListAdapter();
        mMsgRecycler.setAdapter(mMsgAdapter);
        mRoomContainer.addView(mMsgRecycler, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout inputRow = new LinearLayout(mActivity);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(dp(8), dp(6), dp(8), dp(6));
        inputRow.setBackground(makeBox(mBgHeader, mCornerPx * 0.7f, true));

        mMsgInput = makeInput("Tulis pesan...");
        mMsgInput.setSingleLine(false);
        mMsgInput.setMaxLines(4);
        LinearLayout.LayoutParams inLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        inLp.setMarginEnd(dp(8));
        inputRow.addView(mMsgInput, inLp);

        mSendBtn = new TextView(mActivity);
        mSendBtn.setText("➤");
        mSendBtn.setTextColor(Color.WHITE);
        mSendBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mSendBtn.setGravity(Gravity.CENTER);
        mSendBtn.setPadding(dp(14), dp(8), dp(14), dp(8));
        mSendBtn.setBackground(makeBox(mAccentColor, mCornerPx * 0.7f, false));
        mSendBtn.setOnClickListener(v -> sendCurrentMessage());
        inputRow.addView(mSendBtn);

        mRoomContainer.addView(inputRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void sendCurrentMessage() {
        final String text = mMsgInput.getText().toString().trim();
        if (text.isEmpty() || mActiveChatId == 0) return;
        mMsgInput.setText("");
        final long chatId = mActiveChatId;
        new Thread(() -> {
            JSONObject res = FusionTelegramHelper.sendMessageJson(chatId, text, 0);
            if (res != null && res.optBoolean("ok", false)) {
                mActivity.runOnUiThread(this::updateRecButtonUi);
                loadActiveRoomMessages();
            }
        }).start();
    }

    private void rebuildFolderTabs() {
        if (mTabBar == null) return;
        mTabEntries.clear();
        mTabEntries.add(new TabEntry("All", "all", 0));
        mTabEntries.add(new TabEntry("Unread", "unread", 0));
        mTabEntries.add(new TabEntry("Channels", "channels", 0));
        mTabEntries.add(new TabEntry("Groups", "groups", 0));
        mTabEntries.add(new TabEntry("Bots", "bots", 0));
        for (TdApi.ChatFolderInfo fi : FusionTelegramHelper.getChatFilters()) {
            if (fi != null && fi.name != null && fi.name.text != null) {
                mTabEntries.add(new TabEntry(fi.name.text.text, null, fi.id));
            }
        }
        List<TerminalWindowBar.WindowItem> winItems = new ArrayList<>();
        int selectedIdx = 0;
        for (int i = 0; i < mTabEntries.size(); i++) {
            TabEntry e = mTabEntries.get(i);
            boolean active = (e.folderId > 0 && mActiveFolderId == e.folderId)
                || (e.folderId == 0 && mActiveFolderId == 0 && e.builtInKey != null && e.builtInKey.equals(mActiveBuiltInTab));
            if (active) selectedIdx = i;
            winItems.add(new TerminalWindowBar.WindowItem(e.label, e.label));
        }
        mTabBar.setChromeInk(mActivity.getChromeInk());
        mTabBar.setPlaceAccent(mActivity.getActivePlaceAccent());
        mTabBar.setSurfaceStyle(mCornerPx > 0f, mCornerPx);
        mTabBar.setWindows(winItems, selectedIdx);
    }

    public void syncUiState() {
        String st = FusionTelegramHelper.getAuthState();
        boolean ready = "ready".equals(st);
        if (!ready) {
            mAuthContainer.setVisibility(View.VISIBLE);
            mChatListContainer.setVisibility(View.GONE);
            mRoomContainer.setVisibility(View.GONE);
            mBackBtn.setVisibility(View.GONE);
            mHeaderTitle.setText("EsoGram Login");

            mAuthStatusText.setText("Status EsoGram: " + st);
            mApiIdInput.setVisibility(("wait_credentials".equals(st) || "uninitialized".equals(st) || st.startsWith("error:")) ? View.VISIBLE : View.GONE);
            mApiHashInput.setVisibility(mApiIdInput.getVisibility());
            mPhoneInput.setVisibility("wait_phone".equals(st) ? View.VISIBLE : View.GONE);
            mCodeInput.setVisibility("wait_code".equals(st) ? View.VISIBLE : View.GONE);
            mPassInput.setVisibility("wait_password".equals(st) ? View.VISIBLE : View.GONE);
            return;
        }

        mAuthContainer.setVisibility(View.GONE);
        if (mActiveChatId != 0) {
            mChatListContainer.setVisibility(View.GONE);
            mRoomContainer.setVisibility(View.VISIBLE);
            mBackBtn.setVisibility(View.VISIBLE);
            mHeaderTitle.setText(mActiveChatTitle);
        } else {
            mChatListContainer.setVisibility(View.VISIBLE);
            mRoomContainer.setVisibility(View.GONE);
            mBackBtn.setVisibility(View.GONE);
            mHeaderTitle.setText("EsoGram");
            refreshChats();
        }
    }

    private void refreshChats() {
        List<TdApi.Chat> list = FusionTelegramHelper.getSortedChats(mActiveBuiltInTab, mActiveFolderId);
        if (!mSearchQuery.isEmpty()) {
            List<TdApi.Chat> filtered = new ArrayList<>();
            for (TdApi.Chat c : list) {
                String t = c.title != null ? c.title.toLowerCase(Locale.ROOT) : "";
                if (t.contains(mSearchQuery)) filtered.add(c);
            }
            list = filtered;
        }
        mChatAdapter.submit(list);
    }

    private void openChatRoom(TdApi.Chat chat) {
        if (chat == null) return;
        mActiveChatId = chat.id;
        mActiveChatTitle = chat.title != null ? chat.title : ("Chat " + chat.id);
        syncUiState();
        loadActiveRoomMessages();
    }

    private void exitChatRoom() {
        mActiveChatId = 0;
        mActiveChatTitle = "";
        syncUiState();
    }

    private void loadActiveRoomMessages() {
        final long chatId = mActiveChatId;
        if (chatId == 0) return;
        new Thread(() -> {
            List<TdApi.Message> msgs = FusionTelegramHelper.fetchHistory(chatId, 0, 45);
            java.util.Collections.reverse(msgs);
            if (!msgs.isEmpty()) {
                FusionTelegramHelper.markChatRead(chatId, msgs.get(msgs.size() - 1).id);
            }
            mActivity.runOnUiThread(() -> {
                if (mActiveChatId == chatId) {
                    mMsgAdapter.submit(msgs);
                    if (!msgs.isEmpty()) {
                        mMsgRecycler.scrollToPosition(msgs.size() - 1);
                    }
                }
            });
        }).start();
    }

    public void destroy() {
        FusionTelegramHelper.removeListener(this);
    }

    @Override
    public void onAuthStateChanged(String state) {
        syncUiState();
    }

    @Override
    public void onChatsUpdated() {
        if (mActiveChatId == 0 && FusionTelegramHelper.isReady()) {
            refreshChats();
        }
    }

    @Override
    public void onNewMessage(long chatId, TdApi.Message message) {
        if (mActiveChatId == chatId) {
            loadActiveRoomMessages();
        } else if (mActiveChatId == 0 && FusionTelegramHelper.isReady()) {
            refreshChats();
        }
    }

    @Override
    public void onFiltersUpdated() {
        rebuildFolderTabs();
    }

    private final class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.VH> {
        private final List<TdApi.Chat> items = new ArrayList<>();
        private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

        void submit(List<TdApi.Chat> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(mActivity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            row.setBackground(makeBox(mBgCard, mCornerPx * 0.7f, false));
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(8), dp(3), dp(8), dp(3));
            row.setLayoutParams(lp);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            TdApi.Chat c = items.get(pos);
            String title = c.title != null && !c.title.isEmpty() ? c.title : ("Chat " + c.id);
            h.avatar.setText(title.substring(0, 1).toUpperCase(Locale.ROOT));
            h.title.setText(title);
            h.preview.setText(c.lastMessage != null ? FusionTelegramHelper.extractMessageText(c.lastMessage) : "");
            h.time.setText(c.lastMessage != null ? fmt.format(new Date(c.lastMessage.date * 1000L)) : "");
            if (c.unreadCount > 0) {
                h.badge.setVisibility(View.VISIBLE);
                h.badge.setText(String.valueOf(c.unreadCount));
            } else {
                h.badge.setVisibility(View.GONE);
            }
            h.itemView.setOnClickListener(v -> openChatRoom(c));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class VH extends RecyclerView.ViewHolder {
            final TextView avatar;
            final TextView title;
            final TextView preview;
            final TextView time;
            final TextView badge;

            VH(LinearLayout root) {
                super(root);
                avatar = new TextView(mActivity);
                avatar.setTextColor(Color.WHITE);
                avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                avatar.setTypeface(Typeface.DEFAULT_BOLD);
                avatar.setGravity(Gravity.CENTER);
                avatar.setBackground(makeBox(mAccentColor, mCornerPx, false));
                LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(40), dp(40));
                avLp.setMarginEnd(dp(10));
                root.addView(avatar, avLp);

                LinearLayout mid = new LinearLayout(mActivity);
                mid.setOrientation(LinearLayout.VERTICAL);
                title = new TextView(mActivity);
                title.setTextColor(mTextPrimary);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                title.setSingleLine(true);
                title.setEllipsize(TextUtils.TruncateAt.END);
                preview = new TextView(mActivity);
                preview.setTextColor(mTextSecondary);
                preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                preview.setSingleLine(true);
                preview.setEllipsize(TextUtils.TruncateAt.END);
                mid.addView(title);
                mid.addView(preview);
                root.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                LinearLayout right = new LinearLayout(mActivity);
                right.setOrientation(LinearLayout.VERTICAL);
                right.setGravity(Gravity.END);
                time = new TextView(mActivity);
                time.setTextColor(mTextSecondary);
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                badge = new TextView(mActivity);
                badge.setTextColor(Color.WHITE);
                badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                badge.setPadding(dp(6), dp(2), dp(6), dp(2));
                badge.setBackground(makeBox(mAccentColor, mCornerPx, false));
                right.addView(time);
                right.addView(badge);
                root.addView(right);
            }
        }
    }

    private final class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.VH> {
        private final List<TdApi.Message> items = new ArrayList<>();
        private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

        void submit(List<TdApi.Message> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(mActivity);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(10), dp(3), dp(10), dp(3));
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            TdApi.Message m = items.get(pos);
            h.row.setGravity(m.isOutgoing ? Gravity.END : Gravity.START);
            h.bubble.setBackground(makeBox(m.isOutgoing ? mBubbleOutColor : mBubbleInColor, mCornerPx * 0.85f, true));
            final String msgText = FusionTelegramHelper.extractMessageText(m);
            h.body.setText(msgText);
            Linkify.addLinks(h.body, Linkify.WEB_URLS);
            h.body.setMovementMethod(LinkMovementMethod.getInstance());
            h.time.setText(fmt.format(new Date(m.date * 1000L)));

            h.inlineHost.removeAllViews();
            boolean hasButtons = false;
            if (m.replyMarkup instanceof TdApi.ReplyMarkupInlineKeyboard) {
                TdApi.ReplyMarkupInlineKeyboard kb = (TdApi.ReplyMarkupInlineKeyboard) m.replyMarkup;
                if (kb.rows != null) {
                    hasButtons = true;
                    int btnIdx = 0;
                    for (TdApi.InlineKeyboardButton[] btnRow : kb.rows) {
                        LinearLayout rLayout = new LinearLayout(mActivity);
                        rLayout.setOrientation(LinearLayout.HORIZONTAL);
                        for (TdApi.InlineKeyboardButton btn : btnRow) {
                            final int capturedIdx = btnIdx++;
                            TextView bView = new TextView(mActivity);
                            bView.setText(btn.text);
                            bView.setTextColor(mTextPrimary);
                            bView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            bView.setGravity(Gravity.CENTER);
                            bView.setPadding(dp(8), dp(6), dp(8), dp(6));
                            bView.setBackground(makeBox(mBgHeader, mCornerPx * 0.5f, true));
                            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                            bLp.setMargins(dp(2), dp(2), dp(2), dp(2));
                            bView.setOnClickListener(v -> handleInlineClick(m, btn, capturedIdx));
                            rLayout.addView(bView, bLp);
                        }
                        h.inlineHost.addView(rLayout);
                    }
                }
            }
            List<JSONObject> pluginActs = FusionTelegramHelper.getMatchingMessageActions(msgText);
            if (!pluginActs.isEmpty()) {
                hasButtons = true;
                LinearLayout pRow = new LinearLayout(mActivity);
                pRow.setOrientation(LinearLayout.HORIZONTAL);
                for (JSONObject pAct : pluginActs) {
                    String label = pAct.optString("label", "Run");
                    String cmd = pAct.optString("command", "");
                    TextView pBtn = new TextView(mActivity);
                    pBtn.setText(label);
                    pBtn.setTextColor(Color.WHITE);
                    pBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    pBtn.setGravity(Gravity.CENTER);
                    pBtn.setPadding(dp(8), dp(5), dp(8), dp(5));
                    pBtn.setBackground(makeBox(mAccentColor, mCornerPx * 0.5f, false));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    pLp.setMargins(dp(2), dp(3), dp(2), dp(2));
                    pBtn.setOnClickListener(v -> {
                        FusionTelegramHelper.executePluginCommand(cmd, label, m.chatId, m.id, msgText, FusionTelegramHelper.getActiveAccountId());
                        FusionToast.makeText(mActivity, "Plugin: " + label, 0).show();
                    });
                    pRow.addView(pBtn, pLp);
                }
                h.inlineHost.addView(pRow);
            }
            h.inlineHost.setVisibility(hasButtons ? View.VISIBLE : View.GONE);
        }

        private void openUrlFromTelegram(String url) {
            if (FusionWebViewManager.TELEGRAM_CANVAS_LEFT.equals(mManager.mTelegramCanvasLock)) {
                mActivity.goToWallPage(com.termux.app.wall.PaneWallPage.TERMINAL);
            } else {
                mManager.hideTelegramPanel();
            }
            mManager.openUrl(url, "default", false, false);
        }

        private void handleInlineClick(TdApi.Message m, TdApi.InlineKeyboardButton btn, int btnIdx) {
            if (btn.type instanceof TdApi.InlineKeyboardButtonTypeUrl) {
                String url = ((TdApi.InlineKeyboardButtonTypeUrl) btn.type).url;
                if (FusionTelegramHelper.isRecording()) {
                    JSONObject step = new JSONObject();
                    try {
                        step.put("action", "join_target");
                        step.put("target", url);
                        step.put("text", btn.text);
                    } catch (Throwable ignored) {}
                    FusionTelegramHelper.recordStep(step);
                    updateRecButtonUi();
                }
                openUrlFromTelegram(url);
            } else if (btn.type instanceof TdApi.InlineKeyboardButtonTypeWebApp) {
                String url = ((TdApi.InlineKeyboardButtonTypeWebApp) btn.type).url;
                long botId = (m.senderId instanceof TdApi.MessageSenderUser) ? ((TdApi.MessageSenderUser) m.senderId).userId : 0;
                new Thread(() -> {
                    JSONObject res = FusionTelegramHelper.openWebAppJson(m.chatId, botId, url);
                    String target = (res != null && res.optBoolean("ok", false)) ? res.optString("url", url) : url;
                    mActivity.runOnUiThread(() -> {
                        updateRecButtonUi();
                        openUrlFromTelegram(target);
                    });
                }).start();
            } else if (btn.type instanceof TdApi.InlineKeyboardButtonTypeCallback) {
                String b64 = android.util.Base64.encodeToString(((TdApi.InlineKeyboardButtonTypeCallback) btn.type).data, android.util.Base64.NO_WRAP);
                new Thread(() -> {
                    JSONObject res = FusionTelegramHelper.clickInlineButtonJson(m.chatId, m.id, b64, btn.text, btnIdx);
                    mActivity.runOnUiThread(() -> {
                        updateRecButtonUi();
                        if (res != null && !res.optString("text", "").isEmpty()) {
                            FusionToast.makeText(mActivity, res.optString("text"), 0).show();
                        }
                    });
                }).start();
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class VH extends RecyclerView.ViewHolder {
            final LinearLayout row;
            final LinearLayout bubble;
            final TextView body;
            final TextView time;
            final LinearLayout inlineHost;

            VH(LinearLayout root) {
                super(root);
                row = root;
                bubble = new LinearLayout(mActivity);
                bubble.setOrientation(LinearLayout.VERTICAL);
                bubble.setPadding(dp(12), dp(8), dp(12), dp(6));

                body = new TextView(mActivity);
                body.setTextColor(mTextPrimary);
                body.setLinkTextColor(mAccentColor);
                body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                bubble.addView(body);

                time = new TextView(mActivity);
                time.setTextColor(mTextSecondary);
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                time.setGravity(Gravity.END);
                bubble.addView(time);

                inlineHost = new LinearLayout(mActivity);
                inlineHost.setOrientation(LinearLayout.VERTICAL);
                bubble.addView(inlineHost);

                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                root.addView(bubble, bLp);
            }
        }
    }
}
