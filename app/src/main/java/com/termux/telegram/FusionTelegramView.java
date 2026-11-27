package com.termux.telegram;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.drinkless.tdlib.TdApi;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Native Android EsoGram client UI matching modern Telegram Android layout:
 * - Termux PaneGlass transparent backdrop + TerminalWindowBar folder tabs
 * - Top header with EsoGram badge, REC indicator & dynamic JSON overflow menu
 * - Pill search bar, clean chat list with real avatars, pinned/muted/unread badges & FAB
 * - Floating bottom navigation pill (Obrolan, Kontak, Pengaturan, Profil + long-press account switcher)
 * - Chat room with floating header, date pills, checkmarks, "/" bot command popup, 2-col reply keyboard,
 *   scroll-down FAB, and live customizable tool/pemantau bars (zero rebuild needed via JSON)
 */
public final class FusionTelegramView extends FrameLayout implements FusionTelegramHelper.TelegramUiListener {
    private static final int[] AVATAR_PALETTE = {
        Color.parseColor("#E17076"),
        Color.parseColor("#7BC862"),
        Color.parseColor("#E5A93B"),
        Color.parseColor("#65AADD"),
        Color.parseColor("#A695E7"),
        Color.parseColor("#EE7AAE"),
        Color.parseColor("#6EC9CB")
    };

    private final TermuxActivity mActivity;
    private final FusionWebViewManager mManager;
    private final Map<String, Bitmap> mAvatarBitmapCache = new ConcurrentHashMap<>();

    private PaneGlassBackdropView mGlassBackdrop;
    private LinearLayout mRootColumn;
    private LinearLayout mLiveBarContainer;
    private LinearLayout mCustomBarContainer;

    // Header
    private LinearLayout mHeaderBar;
    private TextView mHeaderAvatar;
    private LinearLayout mHeaderTitleCol;
    private TextView mHeaderTitle;
    private TextView mHeaderSubtitle;
    private TextView mBackBtn;
    private TextView mRecBtn;
    private TextView mMoreBtn;

    private TerminalWindowBar mTabBar;
    private final List<TabEntry> mTabEntries = new ArrayList<>();

    private FrameLayout mContentHost;
    private ScrollView mAuthContainer;
    private FrameLayout mChatListWrapper;
    private LinearLayout mChatListContainer;
    private TextView mFabBtn;

    private FrameLayout mRoomWrapper;
    private LinearLayout mRoomContainer;
    private LinearLayout mReplyBar;
    private TextView mReplyBarText;
    private TextView mScrollDownFab;
    private LinearLayout mReplyKeyboardHost;

    private ScrollView mSettingsContainer;
    private LinearLayout mSettingsBody;

    // Floating bottom pill nav
    private LinearLayout mBottomPill;
    private TextView mNavChats;
    private TextView mNavContacts;
    private TextView mNavSettings;
    private TextView mNavProfile;

    private EditText mSearchInput;
    private RecyclerView mChatRecycler;
    private ChatListAdapter mChatAdapter;

    private RecyclerView mMsgRecycler;
    private MessageListAdapter mMsgAdapter;
    private TextView mBotSlashBtn;
    private TextView mReplyKbToggleBtn;
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

    private String mActiveSection = "chats"; // "chats", "contacts", "settings"
    private String mActiveBuiltInTab = "all";
    private int mActiveFolderId = 0;
    private long mActiveChatId = 0;
    private String mActiveChatTitle = "";
    private String mActiveChatSubtitle = "";
    private long mReplyToMsgId = 0;
    private String mReplyPreview = "";
    private String mSearchQuery = "";
    private boolean mReplyKbVisible = false;
    private TdApi.ReplyMarkupShowKeyboard mLastReplyKeyboard = null;

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
        try {
            TermuxAppSharedPreferences prefs = TermuxAppSharedPreferences.build(mActivity, false);
            if (prefs != null && prefs.getDockTintStrength() > 0) {
                tintStrength = Math.max(35, Math.min(98, prefs.getDockTintStrength()));
            }
        } catch (Throwable ignored) {}

        mCornerPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
        int alphaBg = Math.round(255 * (tintStrength / 100f));
        int alphaCard = Math.min(255, Math.max(225, alphaBg + 38));
        int alphaBubble = Math.min(255, Math.max(238, alphaBg + 48));

        if (mIsDark) {
            mBgSurface = Color.argb(alphaBg, 17, 25, 36);
            mBgHeader = Color.argb(alphaCard, 27, 39, 53);
            mBgCard = Color.argb(alphaCard, 24, 34, 46);
            mTextPrimary = Color.parseColor("#F1F5F9");
            mTextSecondary = Color.parseColor("#8FA1B3");
            mAccentColor = Color.parseColor("#2EA6FF");
            mBubbleOutColor = Color.argb(alphaBubble, 43, 108, 176);
            mBubbleInColor = Color.argb(alphaBubble, 30, 42, 56);
            mStrokeColor = Color.argb(48, 255, 255, 255);
        } else {
            mBgSurface = Color.argb(alphaBg, 242, 244, 248);
            mBgHeader = Color.argb(alphaCard, 232, 236, 244);
            mBgCard = Color.argb(alphaCard, 255, 255, 255);
            mTextPrimary = Color.parseColor("#1E2030");
            mTextSecondary = Color.parseColor("#5C6378");
            mAccentColor = Color.parseColor("#1E66F5");
            mBubbleOutColor = Color.argb(alphaBubble, 212, 231, 255);
            mBubbleInColor = Color.argb(alphaBubble, 255, 255, 255);
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

        // Header Bar (Telegram modern style)
        mHeaderBar = new LinearLayout(mActivity);
        mHeaderBar.setOrientation(LinearLayout.HORIZONTAL);
        mHeaderBar.setGravity(Gravity.CENTER_VERTICAL);
        mHeaderBar.setPadding(dp(10), dp(8), dp(10), dp(6));

        mBackBtn = makeCircleBtn("←", mBgHeader, mTextPrimary);
        mBackBtn.setVisibility(View.GONE);
        mBackBtn.setOnClickListener(v -> exitChatRoom());
        mHeaderBar.addView(mBackBtn);

        mHeaderAvatar = new TextView(mActivity);
        mHeaderAvatar.setText("✈");
        mHeaderAvatar.setTextColor(Color.WHITE);
        mHeaderAvatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mHeaderAvatar.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderAvatar.setGravity(Gravity.CENTER);
        mHeaderAvatar.setBackground(makeBox(mAccentColor, dp(18), false));
        LinearLayout.LayoutParams hAvLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        hAvLp.setMarginStart(dp(4));
        hAvLp.setMarginEnd(dp(10));
        mHeaderBar.addView(mHeaderAvatar, hAvLp);

        mHeaderTitleCol = new LinearLayout(mActivity);
        mHeaderTitleCol.setOrientation(LinearLayout.VERTICAL);
        mHeaderTitle = new TextView(mActivity);
        mHeaderTitle.setText("EsoGram");
        mHeaderTitle.setTextColor(mTextPrimary);
        mHeaderTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        mHeaderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mHeaderTitle.setSingleLine(true);
        mHeaderTitle.setEllipsize(TextUtils.TruncateAt.END);
        mHeaderSubtitle = new TextView(mActivity);
        mHeaderSubtitle.setTextColor(mTextSecondary);
        mHeaderSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        mHeaderSubtitle.setVisibility(View.GONE);
        mHeaderTitleCol.addView(mHeaderTitle);
        mHeaderTitleCol.addView(mHeaderSubtitle);
        mHeaderBar.addView(mHeaderTitleCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        mRecBtn = makeHeaderPillBtn("● REC", mBgHeader, mTextPrimary);
        mRecBtn.setOnClickListener(v -> toggleTelegramRecording());
        LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recLp.setMarginEnd(dp(6));
        mHeaderBar.addView(mRecBtn, recLp);

        mMoreBtn = makeCircleBtn("⋮", mBgHeader, mTextPrimary);
        mMoreBtn.setOnClickListener(v -> showOverflowMenu());
        mHeaderBar.addView(mMoreBtn);

        // Live Pemantau & Custom Tool Bars (JSON driven, zero rebuild)
        mLiveBarContainer = new LinearLayout(mActivity);
        mLiveBarContainer.setOrientation(LinearLayout.VERTICAL);
        mLiveBarContainer.setVisibility(View.GONE);

        mCustomBarContainer = new LinearLayout(mActivity);
        mCustomBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        mCustomBarContainer.setVisibility(View.GONE);
        mCustomBarContainer.setPadding(dp(10), dp(2), dp(10), dp(4));

        // Content Host
        mContentHost = new FrameLayout(mActivity);
        buildAuthView();
        buildChatListView();
        buildRoomView();
        buildSettingsView();

        mContentHost.addView(mAuthContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentHost.addView(mChatListWrapper, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentHost.addView(mRoomWrapper, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentHost.addView(mSettingsContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Termux native TerminalWindowBar for folder tabs
        mTabBar = new TerminalWindowBar(mActivity, null);
        mTabBar.setCreateButtonShown(false);
        mTabBar.setPadding(dp(6), dp(2), dp(6), dp(2));
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

        // Floating Bottom Pill Navigation (Obrolan | Kontak | Pengaturan | Profil)
        buildBottomNavPill();

        rebuildFolderTabs();
        rebuildDynamicBars();
        layoutColumns();
    }

    private void buildBottomNavPill() {
        mBottomPill = new LinearLayout(mActivity);
        mBottomPill.setOrientation(LinearLayout.HORIZONTAL);
        mBottomPill.setGravity(Gravity.CENTER_VERTICAL);
        mBottomPill.setBackground(makeBox(mBgHeader, dp(24), true));
        mBottomPill.setPadding(dp(8), dp(5), dp(8), dp(5));

        mNavChats = makeNavTab("💬 Obrolan", "chats");
        mNavContacts = makeNavTab("👥 Grup/Ch", "contacts");
        mNavSettings = makeNavTab("⚙ Atur", "settings");
        mNavProfile = makeNavTab("👤 Profil", "profile");
        mNavProfile.setOnLongClickListener(v -> {
            showAccountSwitcherDialog();
            return true;
        });

        mBottomPill.addView(mNavChats, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mBottomPill.addView(mNavContacts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mBottomPill.addView(mNavSettings, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mBottomPill.addView(mNavProfile, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    private TextView makeNavTab(String label, String key) {
        TextView tv = new TextView(mActivity);
        tv.setText(label);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(4), dp(7), dp(4), dp(7));
        tv.setSingleLine(true);
        tv.setOnClickListener(v -> {
            if ("profile".equals(key)) {
                mActiveSection = "settings";
            } else if ("contacts".equals(key)) {
                mActiveSection = "chats";
                mActiveBuiltInTab = "groups";
                mActiveFolderId = 0;
                rebuildFolderTabs();
            } else {
                mActiveSection = key;
                if ("chats".equals(key) && "groups".equals(mActiveBuiltInTab)) {
                    mActiveBuiltInTab = "all";
                    rebuildFolderTabs();
                }
            }
            mActiveChatId = 0;
            syncUiState();
        });
        return tv;
    }

    private void layoutColumns() {
        mRootColumn.removeAllViews();
        String pos = FusionTelegramHelper.getTabPosition(mActivity);
        mRootColumn.addView(mHeaderBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRootColumn.addView(mLiveBarContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRootColumn.addView(mCustomBarContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        if ("top".equals(pos)) {
            mRootColumn.addView(mTabBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)));
            mRootColumn.addView(mContentHost, contentLp);
        } else {
            mRootColumn.addView(mContentHost, contentLp);
            mRootColumn.addView(mTabBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)));
        }
        LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navLp.setMargins(dp(12), dp(4), dp(12), dp(8));
        mRootColumn.addView(mBottomPill, navLp);
    }

    private TextView makeCircleBtn(String label, int bg, int fg) {
        TextView tv = new TextView(mActivity);
        tv.setText(label);
        tv.setTextColor(fg);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(makeBox(bg, dp(18), true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(34), dp(34));
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView makeHeaderPillBtn(String label, int bg, int fg) {
        TextView tv = new TextView(mActivity);
        tv.setText(label);
        tv.setTextColor(fg);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(6), dp(10), dp(6));
        tv.setBackground(makeBox(bg, dp(16), true));
        return tv;
    }

    private void applyTermuxGlassSurface() {
        try {
            PaneSurfaceStyle style = mActivity.getPaneSurfaceStyle();
            PaneGlass.apply(style, this, mGlassBackdrop, 0f);
        } catch (Throwable t) {
            setBackground(makeBox(mBgSurface, 0f, false));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mGlassBackdrop != null) {
            PaneGlass.followLayout(mGlassBackdrop);
        }
    }

    private void showOverflowMenu() {
        final List<String> labels = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();

        labels.add("🔍 Gabung / Chat @username");
        actions.add(this::showJoinOrNewChatDialog);

        labels.add("🔖 Pesan Tersimpan");
        actions.add(() -> {
            JSONObject st = FusionTelegramHelper.getStatusJson();
            JSONObject me = st.optJSONObject("me");
            if (me != null) {
                long myId = me.optLong("id", 0);
                if (myId != 0) {
                    mActiveChatId = myId;
                    mActiveChatTitle = "Pesan Tersimpan";
                    mActiveChatSubtitle = "Saved Messages";
                    syncUiState();
                    loadActiveRoomMessages();
                }
            }
        });

        labels.add("👤 Switch Akun (" + FusionTelegramHelper.getActiveAccountId() + ")");
        actions.add(this::showAccountSwitcherDialog);

        labels.add("⇅ Pindah Tab Folder (" + ("top".equals(FusionTelegramHelper.getTabPosition(mActivity)) ? "Ke Bawah" : "Ke Atas") + ")");
        actions.add(this::toggleTabBarPosition);

        labels.add("🔄 Reload Plugin & Custom Bar");
        actions.add(() -> {
            JSONObject r = FusionTelegramHelper.reloadPlugins();
            rebuildDynamicBars();
            FusionToast.makeText(mActivity, "Plugin dimuat: " + r.optInt("customBars", 0) + " bar, " + r.optInt("messageActions", 0) + " aksi", 0).show();
        });

        // Dynamic overflow items from JSON (no APK rebuild needed)
        for (JSONObject item : FusionTelegramHelper.getPluginOverflowMenu()) {
            String lbl = item.optString("label", "");
            String cmd = item.optString("command", "");
            if (!lbl.isEmpty() && !cmd.isEmpty()) {
                labels.add(lbl);
                actions.add(() -> FusionTelegramHelper.executePluginCommand(cmd, lbl, mActiveChatId, 0, "", FusionTelegramHelper.getActiveAccountId()));
            }
        }

        labels.add("✕ Tutup EsoGram");
        actions.add(mManager::hideTelegramPanel);

        new android.app.AlertDialog.Builder(mActivity)
            .setTitle("Menu EsoGram")
            .setItems(labels.toArray(new CharSequence[0]), (d, which) -> {
                if (which >= 0 && which < actions.size()) actions.get(which).run();
            })
            .show();
    }

    private void showJoinOrNewChatDialog() {
        final EditText input = makeInput("@username atau link t.me/...");
        new android.app.AlertDialog.Builder(mActivity)
            .setTitle("Buka / Gabung Chat")
            .setView(input)
            .setPositiveButton("Buka", (d, w) -> {
                String target = input.getText().toString().trim();
                if (target.isEmpty()) return;
                new Thread(() -> {
                    JSONObject res = FusionTelegramHelper.joinChatJson(target);
                    mActivity.runOnUiThread(() -> {
                        if (res.optBoolean("ok", false)) {
                            mActiveChatId = res.optLong("chatId", 0);
                            mActiveChatTitle = res.optString("title", target);
                            mActiveChatSubtitle = "";
                            syncUiState();
                            loadActiveRoomMessages();
                        } else {
                            FusionToast.makeText(mActivity, res.optString("error", "Gagal"), 1).show();
                        }
                    });
                }).start();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void rebuildDynamicBars() {
        if (mLiveBarContainer == null || mCustomBarContainer == null) return;
        mLiveBarContainer.removeAllViews();
        JSONObject live = FusionTelegramHelper.getLiveBar();
        if (live != null && !live.optString("text", "").isEmpty()) {
            mLiveBarContainer.setVisibility(View.VISIBLE);
            LinearLayout row = new LinearLayout(mActivity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(6), dp(10), dp(6));
            int barBg = Color.argb(215, 22, 101, 52);
            try {
                if (!live.optString("color", "").isEmpty()) barBg = Color.parseColor(live.optString("color"));
            } catch (Throwable ignored) {}
            row.setBackground(makeBox(barBg, dp(10), false));

            TextView tv = new TextView(mActivity);
            tv.setText(live.optString("text", ""));
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            JSONArray btns = live.optJSONArray("buttons");
            if (btns != null) {
                for (int i = 0; i < btns.length(); i++) {
                    JSONObject b = btns.optJSONObject(i);
                    if (b == null) continue;
                    String bLbl = b.optString("label", "Run");
                    String bCmd = b.optString("command", "");
                    TextView bTv = makeHeaderPillBtn(bLbl, Color.argb(80, 0, 0, 0), Color.WHITE);
                    LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    bLp.setMarginStart(dp(6));
                    bTv.setOnClickListener(v -> {
                        if ("__clear__".equals(bCmd)) {
                            FusionTelegramHelper.clearLiveBar();
                        } else {
                            FusionTelegramHelper.executePluginCommand(bCmd, bLbl, mActiveChatId, 0, "", FusionTelegramHelper.getActiveAccountId());
                        }
                    });
                    row.addView(bTv, bLp);
                }
            }
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLp.setMargins(dp(10), dp(2), dp(10), dp(4));
            mLiveBarContainer.addView(row, rLp);
        } else {
            mLiveBarContainer.setVisibility(View.GONE);
        }

        mCustomBarContainer.removeAllViews();
        List<JSONObject> cBars = FusionTelegramHelper.getPluginCustomBars();
        if (!cBars.isEmpty()) {
            mCustomBarContainer.setVisibility(View.VISIBLE);
            for (JSONObject item : cBars) {
                String lbl = item.optString("label", "Tool");
                String cmd = item.optString("command", "");
                TextView btn = makeHeaderPillBtn(lbl, mBgHeader, mTextPrimary);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(6));
                btn.setOnClickListener(v -> FusionTelegramHelper.executePluginCommand(cmd, lbl, mActiveChatId, 0, "", FusionTelegramHelper.getActiveAccountId()));
                mCustomBarContainer.addView(btn, lp);
            }
        } else {
            mCustomBarContainer.setVisibility(View.GONE);
        }
    }

    private void toggleTabBarPosition() {
        String cur = FusionTelegramHelper.getTabPosition(mActivity);
        String next = "top".equals(cur) ? "bottom" : "top";
        FusionTelegramHelper.setTabPosition(mActivity, next);
        layoutColumns();
        FusionToast.makeText(mActivity, "Tab EsoGram: " + ("top".equals(next) ? "Atas" : "Bawah"), 0).show();
    }

    private void showAccountSwitcherDialog() {
        JSONObject res = FusionTelegramHelper.listAccountsJson();
        JSONArray arr = res.optJSONArray("accounts");
        final List<String> ids = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "default");
                String uname = item.optString("username", "");
                String st = item.optString("authState", "");
                ids.add(id);
                labels.add(id + (uname.isEmpty() ? "" : " (@" + uname + ")") + " [" + st + "]");
            }
        }
        labels.add("+ Tambah Slot Akun Baru");
        new android.app.AlertDialog.Builder(mActivity)
            .setTitle("Akun EsoGram (" + FusionTelegramHelper.getActiveAccountId() + ")")
            .setItems(labels.toArray(new CharSequence[0]), (d, which) -> {
                if (which < ids.size()) {
                    mActiveChatId = 0;
                    FusionTelegramHelper.switchAccount(mActivity, ids.get(which));
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

    private void buildAuthView() {
        mAuthContainer = new ScrollView(mActivity);
        LinearLayout box = new LinearLayout(mActivity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(24), dp(20), dp(24));

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
        mAuthActionBtn.setText("Lanjut Login EsoGram");
        mAuthActionBtn.setTextColor(Color.WHITE);
        mAuthActionBtn.setBackground(makeBox(mAccentColor, dp(22), false));
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
        et.setPadding(dp(14), dp(10), dp(14), dp(10));
        et.setBackground(makeBox(mBgCard, dp(20), true));
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
        mChatListWrapper = new FrameLayout(mActivity);
        mChatListContainer = new LinearLayout(mActivity);
        mChatListContainer.setOrientation(LinearLayout.VERTICAL);

        // Pill Search Bar ("🔍 Cari Obrolan")
        mSearchInput = new EditText(mActivity);
        mSearchInput.setHint("🔍  Cari Obrolan");
        mSearchInput.setHintTextColor(mTextSecondary);
        mSearchInput.setTextColor(mTextPrimary);
        mSearchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mSearchInput.setPadding(dp(16), dp(9), dp(16), dp(9));
        mSearchInput.setBackground(makeBox(mBgHeader, dp(22), true));
        mSearchInput.setSingleLine(true);
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchQuery = s != null ? s.toString().trim().toLowerCase(Locale.ROOT) : "";
                refreshChats();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sLp.setMargins(dp(12), dp(4), dp(12), dp(6));
        mChatListContainer.addView(mSearchInput, sLp);

        mChatRecycler = new RecyclerView(mActivity);
        mChatRecycler.setLayoutManager(new LinearLayoutManager(mActivity));
        mChatAdapter = new ChatListAdapter();
        mChatRecycler.setAdapter(mChatAdapter);
        mChatListContainer.addView(mChatRecycler, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        mChatListWrapper.addView(mChatListContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Floating Action Button (+) bottom-right
        mFabBtn = new TextView(mActivity);
        mFabBtn.setText("+");
        mFabBtn.setTextColor(Color.WHITE);
        mFabBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        mFabBtn.setGravity(Gravity.CENTER);
        mFabBtn.setBackground(makeBox(mAccentColor, dp(26), false));
        mFabBtn.setElevation(dp(6));
        mFabBtn.setOnClickListener(v -> showJoinOrNewChatDialog());
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(dp(50), dp(50), Gravity.BOTTOM | Gravity.END);
        fabLp.setMargins(0, 0, dp(16), dp(14));
        mChatListWrapper.addView(mFabBtn, fabLp);
    }

    private void buildRoomView() {
        mRoomWrapper = new FrameLayout(mActivity);
        mRoomContainer = new LinearLayout(mActivity);
        mRoomContainer.setOrientation(LinearLayout.VERTICAL);

        FrameLayout msgArea = new FrameLayout(mActivity);
        mMsgRecycler = new RecyclerView(mActivity);
        LinearLayoutManager lm = new LinearLayoutManager(mActivity);
        lm.setStackFromEnd(true);
        mMsgRecycler.setLayoutManager(lm);
        mMsgAdapter = new MessageListAdapter();
        mMsgRecycler.setAdapter(mMsgAdapter);
        msgArea.addView(mMsgRecycler, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mScrollDownFab = new TextView(mActivity);
        mScrollDownFab.setText("⌄");
        mScrollDownFab.setTextColor(mTextPrimary);
        mScrollDownFab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mScrollDownFab.setGravity(Gravity.CENTER);
        mScrollDownFab.setBackground(makeBox(mBgHeader, dp(19), true));
        mScrollDownFab.setOnClickListener(v -> {
            int cnt = mMsgAdapter.getItemCount();
            if (cnt > 0) mMsgRecycler.smoothScrollToPosition(cnt - 1);
        });
        FrameLayout.LayoutParams sdLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.BOTTOM | Gravity.END);
        sdLp.setMargins(0, 0, dp(12), dp(10));
        msgArea.addView(mScrollDownFab, sdLp);
        mRoomContainer.addView(msgArea, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Reply preview bar
        mReplyBar = new LinearLayout(mActivity);
        mReplyBar.setOrientation(LinearLayout.HORIZONTAL);
        mReplyBar.setGravity(Gravity.CENTER_VERTICAL);
        mReplyBar.setPadding(dp(12), dp(6), dp(12), dp(6));
        mReplyBar.setBackground(makeBox(mBgHeader, dp(10), true));
        mReplyBar.setVisibility(View.GONE);
        mReplyBarText = new TextView(mActivity);
        mReplyBarText.setTextColor(mTextPrimary);
        mReplyBarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mReplyBarText.setSingleLine(true);
        mReplyBarText.setEllipsize(TextUtils.TruncateAt.END);
        mReplyBar.addView(mReplyBarText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView cancelReply = new TextView(mActivity);
        cancelReply.setText("✕");
        cancelReply.setTextColor(mTextSecondary);
        cancelReply.setPadding(dp(8), dp(2), dp(4), dp(2));
        cancelReply.setOnClickListener(v -> {
            mReplyToMsgId = 0;
            mReplyPreview = "";
            mReplyBar.setVisibility(View.GONE);
        });
        mReplyBar.addView(cancelReply);
        mRoomContainer.addView(mReplyBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Bottom Input Bar: [≡ /] [Pesan...] [⊞] [➤]
        LinearLayout inputRow = new LinearLayout(mActivity);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(dp(8), dp(6), dp(8), dp(6));

        mBotSlashBtn = makeCircleBtn("≡", mAccentColor, Color.WHITE);
        mBotSlashBtn.setOnClickListener(v -> showBotCommandsPopup());
        LinearLayout.LayoutParams slLp = new LinearLayout.LayoutParams(dp(36), dp(36));
        slLp.setMarginEnd(dp(6));
        inputRow.addView(mBotSlashBtn, slLp);

        LinearLayout pillInput = new LinearLayout(mActivity);
        pillInput.setOrientation(LinearLayout.HORIZONTAL);
        pillInput.setGravity(Gravity.CENTER_VERTICAL);
        pillInput.setBackground(makeBox(mBgHeader, dp(22), true));
        pillInput.setPadding(dp(12), dp(4), dp(8), dp(4));

        mMsgInput = new EditText(mActivity);
        mMsgInput.setHint("Pesan");
        mMsgInput.setHintTextColor(mTextSecondary);
        mMsgInput.setTextColor(mTextPrimary);
        mMsgInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mMsgInput.setBackgroundColor(Color.TRANSPARENT);
        mMsgInput.setMaxLines(4);
        mMsgInput.setPadding(dp(2), dp(6), dp(6), dp(6));
        pillInput.addView(mMsgInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        mReplyKbToggleBtn = new TextView(mActivity);
        mReplyKbToggleBtn.setText("⊞");
        mReplyKbToggleBtn.setTextColor(mTextSecondary);
        mReplyKbToggleBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mReplyKbToggleBtn.setPadding(dp(6), dp(4), dp(6), dp(4));
        mReplyKbToggleBtn.setVisibility(View.GONE);
        mReplyKbToggleBtn.setOnClickListener(v -> {
            mReplyKbVisible = !mReplyKbVisible;
            mReplyKeyboardHost.setVisibility(mReplyKbVisible ? View.VISIBLE : View.GONE);
        });
        pillInput.addView(mReplyKbToggleBtn);

        LinearLayout.LayoutParams pInLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        pInLp.setMarginEnd(dp(6));
        inputRow.addView(pillInput, pInLp);

        mSendBtn = makeCircleBtn("➤", mAccentColor, Color.WHITE);
        mSendBtn.setOnClickListener(v -> sendCurrentMessage());
        inputRow.addView(mSendBtn, new LinearLayout.LayoutParams(dp(38), dp(38)));

        mRoomContainer.addView(inputRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 2-column Reply Keyboard host (SS 4)
        mReplyKeyboardHost = new LinearLayout(mActivity);
        mReplyKeyboardHost.setOrientation(LinearLayout.VERTICAL);
        mReplyKeyboardHost.setPadding(dp(8), dp(4), dp(8), dp(6));
        mReplyKeyboardHost.setVisibility(View.GONE);
        mRoomContainer.addView(mReplyKeyboardHost, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mRoomWrapper.addView(mRoomContainer, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showBotCommandsPopup() {
        FusionTelegramHelper.fetchBotCommands(mActiveChatId, cmds -> {
            if (cmds == null || cmds.isEmpty()) {
                mMsgInput.setText("/");
                mMsgInput.setSelection(1);
                return;
            }
            LinearLayout shell = new LinearLayout(mActivity);
            shell.setOrientation(LinearLayout.VERTICAL);
            shell.setPadding(dp(8), dp(8), dp(8), dp(8));
            shell.setBackground(makeBox(mBgHeader, dp(16), true));
            final PopupWindow[] popRef = new PopupWindow[1];

            for (TdApi.BotCommand cmd : cmds) {
                LinearLayout row = new LinearLayout(mActivity);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(12), dp(9), dp(12), dp(9));
                TextView cTv = new TextView(mActivity);
                cTv.setText("/" + cmd.command);
                cTv.setTextColor(mAccentColor);
                cTv.setTypeface(Typeface.DEFAULT_BOLD);
                cTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                cTv.setPadding(0, 0, dp(10), 0);
                TextView dTv = new TextView(mActivity);
                dTv.setText(cmd.description != null ? cmd.description : "");
                dTv.setTextColor(mTextSecondary);
                dTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                dTv.setSingleLine(true);
                dTv.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(cTv);
                row.addView(dTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.setOnClickListener(v -> {
                    if (popRef[0] != null) popRef[0].dismiss();
                    sendBotCommandText("/" + cmd.command);
                });
                shell.addView(row);
            }
            PopupWindow pw = new PopupWindow(shell, Math.max(dp(260), getWidth() - dp(24)), ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popRef[0] = pw;
            pw.setOutsideTouchable(true);
            pw.showAtLocation(mRoomWrapper, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(96));
        });
    }

    private void sendBotCommandText(String cmdText) {
        if (mActiveChatId == 0) return;
        final long chatId = mActiveChatId;
        new Thread(() -> {
            JSONObject res = FusionTelegramHelper.sendMessageJson(chatId, cmdText, 0);
            if (res != null && res.optBoolean("ok", false)) {
                mActivity.runOnUiThread(this::updateRecButtonUi);
                loadActiveRoomMessages();
            }
        }).start();
    }

    private void buildSettingsView() {
        mSettingsContainer = new ScrollView(mActivity);
        mSettingsBody = new LinearLayout(mActivity);
        mSettingsBody.setOrientation(LinearLayout.VERTICAL);
        mSettingsBody.setPadding(dp(14), dp(10), dp(14), dp(18));
        mSettingsContainer.addView(mSettingsBody, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void populateSettingsView() {
        if (mSettingsBody == null) return;
        mSettingsBody.removeAllViews();

        JSONObject status = FusionTelegramHelper.getStatusJson();
        JSONObject me = status.optJSONObject("me");
        String myName = me != null ? (me.optString("firstName", "") + " " + me.optString("lastName", "")).trim() : FusionTelegramHelper.getActiveAccountId();
        String myUname = me != null ? me.optString("username", "") : "";
        String myPhone = me != null ? me.optString("phone", "") : "";

        LinearLayout hero = new LinearLayout(mActivity);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(0, dp(10), 0, dp(16));

        ImageView avImg = new ImageView(mActivity);
        bindAvatarView(avImg, null, myName.isEmpty() ? "E" : myName, FusionTelegramHelper.getMeAvatarPath(), 0, dp(72));
        hero.addView(avImg, new LinearLayout.LayoutParams(dp(72), dp(72)));

        TextView nameTv = new TextView(mActivity);
        nameTv.setText(myName.isEmpty() ? "EsoGram User" : myName);
        nameTv.setTextColor(mTextPrimary);
        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setPadding(0, dp(8), 0, dp(2));
        hero.addView(nameTv);

        TextView subTv = new TextView(mActivity);
        subTv.setText((myPhone.isEmpty() ? "" : "+" + myPhone) + (myUname.isEmpty() ? "" : " • @" + myUname));
        subTv.setTextColor(mTextSecondary);
        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hero.addView(subTv);
        mSettingsBody.addView(hero);

        // Card: Multi-Akun (SS 8)
        LinearLayout accCard = new LinearLayout(mActivity);
        accCard.setOrientation(LinearLayout.VERTICAL);
        accCard.setBackground(makeBox(mBgCard, dp(16), true));
        accCard.setPadding(dp(14), dp(12), dp(14), dp(8));
        TextView accHead = new TextView(mActivity);
        accHead.setText("Akun");
        accHead.setTextColor(mAccentColor);
        accHead.setTypeface(Typeface.DEFAULT_BOLD);
        accHead.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        accHead.setPadding(0, 0, 0, dp(6));
        accCard.addView(accHead);

        JSONArray accs = FusionTelegramHelper.listAccountsJson().optJSONArray("accounts");
        if (accs != null) {
            for (int i = 0; i < accs.length(); i++) {
                JSONObject a = accs.optJSONObject(i);
                if (a == null) continue;
                String id = a.optString("id", "default");
                String u = a.optString("username", "");
                boolean active = a.optBoolean("active", false);
                LinearLayout row = makeSettingRow("👤", id + (u.isEmpty() ? "" : " (@" + u + ")"), active ? "Aktif" : a.optString("authState", ""), () -> {
                    FusionTelegramHelper.switchAccount(mActivity, id);
                    syncUiState();
                });
                accCard.addView(row);
            }
        }
        accCard.addView(makeSettingRow("➕", "Tambah Slot Akun Baru", "Login akun lain tanpa bentrok", this::showAccountSwitcherDialog));
        mSettingsBody.addView(accCard, lpWithMargin(dp(12)));

        // Card: Pengaturan & Custom Settings (SS 8 + JSON)
        LinearLayout cfgCard = new LinearLayout(mActivity);
        cfgCard.setOrientation(LinearLayout.VERTICAL);
        cfgCard.setBackground(makeBox(mBgCard, dp(16), true));
        cfgCard.setPadding(dp(14), dp(12), dp(14), dp(8));
        cfgCard.addView(makeSettingRow("⇅", "Posisi Tab Folder", "Saat ini: " + ("top".equals(FusionTelegramHelper.getTabPosition(mActivity)) ? "Atas" : "Bawah"), this::toggleTabBarPosition));
        cfgCard.addView(makeSettingRow("🔄", "Reload Plugin & Custom Bar", "Muat ulang esogram_plugins.json", () -> {
            FusionTelegramHelper.reloadPlugins();
            rebuildDynamicBars();
            populateSettingsView();
        }));
        for (JSONObject cs : FusionTelegramHelper.getPluginCustomSettings()) {
            String lbl = cs.optString("label", "Custom");
            String desc = cs.optString("description", "");
            String cmd = cs.optString("command", "");
            cfgCard.addView(makeSettingRow("⚡", lbl, desc, () -> FusionTelegramHelper.executePluginCommand(cmd, lbl, 0, 0, "", FusionTelegramHelper.getActiveAccountId())));
        }
        cfgCard.addView(makeSettingRow("🚪", "Logout Akun Ini", "Keluar dari sesi " + FusionTelegramHelper.getActiveAccountId(), () -> {
            new Thread(() -> {
                FusionTelegramHelper.logout();
                mActivity.runOnUiThread(this::syncUiState);
            }).start();
        }));
        mSettingsBody.addView(cfgCard);
    }

    private LinearLayout makeSettingRow(String icon, String title, String subtitle, Runnable onClick) {
        LinearLayout row = new LinearLayout(mActivity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), dp(9), dp(2), dp(9));
        TextView ic = new TextView(mActivity);
        ic.setText(icon);
        ic.setTextColor(mAccentColor);
        ic.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(makeBox(mBgHeader, dp(16), false));
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        icLp.setMarginEnd(dp(12));
        row.addView(ic, icLp);

        LinearLayout mid = new LinearLayout(mActivity);
        mid.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(mActivity);
        t.setText(title);
        t.setTextColor(mTextPrimary);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mid.addView(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = new TextView(mActivity);
            s.setText(subtitle);
            s.setTextColor(mTextSecondary);
            s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            mid.addView(s);
        }
        row.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.setOnClickListener(v -> onClick.run());
        return row;
    }

    private void sendCurrentMessage() {
        final String text = mMsgInput.getText().toString().trim();
        if (text.isEmpty() || mActiveChatId == 0) return;
        mMsgInput.setText("");
        final long chatId = mActiveChatId;
        final long replyId = mReplyToMsgId;
        mReplyToMsgId = 0;
        mReplyPreview = "";
        mReplyBar.setVisibility(View.GONE);
        new Thread(() -> {
            JSONObject res = FusionTelegramHelper.sendMessageJson(chatId, text, replyId);
            if (res != null && res.optBoolean("ok", false)) {
                mActivity.runOnUiThread(this::updateRecButtonUi);
                loadActiveRoomMessages();
            }
        }).start();
    }

    private void rebuildFolderTabs() {
        if (mTabBar == null) return;
        mTabEntries.clear();
        mTabEntries.add(new TabEntry("Semua", "all", 0));
        mTabEntries.add(new TabEntry("Unread", "unread", 0));
        mTabEntries.add(new TabEntry("Channel", "channels", 0));
        mTabEntries.add(new TabEntry("Grup", "groups", 0));
        mTabEntries.add(new TabEntry("Pribadi", "bots", 0));
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
        mTabBar.setSurfaceStyle(true, mCornerPx);
        mTabBar.setWindows(winItems, selectedIdx);
    }

    public void syncUiState() {
        String st = FusionTelegramHelper.getAuthState();
        boolean ready = "ready".equals(st);
        updateRecButtonUi();
        rebuildDynamicBars();

        if (!ready) {
            mAuthContainer.setVisibility(View.VISIBLE);
            mChatListWrapper.setVisibility(View.GONE);
            mRoomWrapper.setVisibility(View.GONE);
            mSettingsContainer.setVisibility(View.GONE);
            mTabBar.setVisibility(View.GONE);
            mBottomPill.setVisibility(View.GONE);
            mBackBtn.setVisibility(View.GONE);
            mHeaderAvatar.setVisibility(View.VISIBLE);
            mHeaderTitle.setText("EsoGram Login");
            mHeaderSubtitle.setVisibility(View.GONE);

            mAuthStatusText.setText("Status EsoGram (" + FusionTelegramHelper.getActiveAccountId() + "): " + st);
            mApiIdInput.setVisibility(("wait_credentials".equals(st) || "uninitialized".equals(st) || st.startsWith("error:")) ? View.VISIBLE : View.GONE);
            mApiHashInput.setVisibility(mApiIdInput.getVisibility());
            mPhoneInput.setVisibility("wait_phone".equals(st) ? View.VISIBLE : View.GONE);
            mCodeInput.setVisibility("wait_code".equals(st) ? View.VISIBLE : View.GONE);
            mPassInput.setVisibility("wait_password".equals(st) ? View.VISIBLE : View.GONE);
            return;
        }

        mAuthContainer.setVisibility(View.GONE);
        if (mActiveChatId != 0) {
            mChatListWrapper.setVisibility(View.GONE);
            mSettingsContainer.setVisibility(View.GONE);
            mTabBar.setVisibility(View.GONE);
            mBottomPill.setVisibility(View.GONE);
            mRoomWrapper.setVisibility(View.VISIBLE);
            mBackBtn.setVisibility(View.VISIBLE);
            mHeaderAvatar.setVisibility(View.GONE);
            mHeaderTitle.setText(mActiveChatTitle);
            if (!mActiveChatSubtitle.isEmpty()) {
                mHeaderSubtitle.setText(mActiveChatSubtitle);
                mHeaderSubtitle.setVisibility(View.VISIBLE);
            } else {
                mHeaderSubtitle.setVisibility(View.GONE);
            }
            return;
        }

        mRoomWrapper.setVisibility(View.GONE);
        mBackBtn.setVisibility(View.GONE);
        mHeaderAvatar.setVisibility(View.VISIBLE);
        mHeaderSubtitle.setVisibility(View.GONE);
        mBottomPill.setVisibility(View.VISIBLE);
        updateBottomNavHighlight();

        if ("settings".equals(mActiveSection)) {
            mChatListWrapper.setVisibility(View.GONE);
            mTabBar.setVisibility(View.GONE);
            mSettingsContainer.setVisibility(View.VISIBLE);
            mHeaderTitle.setText("Profil & Pengaturan");
            populateSettingsView();
        } else {
            mSettingsContainer.setVisibility(View.GONE);
            mTabBar.setVisibility(View.VISIBLE);
            mChatListWrapper.setVisibility(View.VISIBLE);
            mHeaderTitle.setText("EsoGram");
            refreshChats();
        }
    }

    private void updateBottomNavHighlight() {
        boolean isSet = "settings".equals(mActiveSection);
        boolean isGrp = !isSet && "groups".equals(mActiveBuiltInTab);
        boolean isChat = !isSet && !isGrp;
        styleNavBtn(mNavChats, isChat);
        styleNavBtn(mNavContacts, isGrp);
        styleNavBtn(mNavSettings, isSet);
        styleNavBtn(mNavProfile, false);
    }

    private void styleNavBtn(TextView tv, boolean active) {
        if (tv == null) return;
        tv.setTextColor(active ? mAccentColor : mTextSecondary);
        tv.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setBackground(active ? makeBox(Color.argb(40, Color.red(mAccentColor), Color.green(mAccentColor), Color.blue(mAccentColor)), dp(18), false) : null);
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
        mActiveChatSubtitle = FusionTelegramHelper.getChatSubtitle(chat);
        mReplyKbVisible = false;
        mLastReplyKeyboard = null;
        mReplyKeyboardHost.setVisibility(View.GONE);
        mReplyKbToggleBtn.setVisibility(View.GONE);
        syncUiState();
        loadActiveRoomMessages();
    }

    private void exitChatRoom() {
        mActiveChatId = 0;
        mActiveChatTitle = "";
        mActiveChatSubtitle = "";
        syncUiState();
    }

    private void loadActiveRoomMessages() {
        final long chatId = mActiveChatId;
        if (chatId == 0) return;
        new Thread(() -> {
            List<TdApi.Message> msgs = FusionTelegramHelper.fetchHistory(chatId, 0, 50);
            Collections.reverse(msgs);
            if (!msgs.isEmpty()) {
                FusionTelegramHelper.markChatRead(chatId, msgs.get(msgs.size() - 1).id);
            }
            TdApi.ReplyMarkupShowKeyboard foundKb = null;
            for (int i = msgs.size() - 1; i >= 0; i--) {
                if (msgs.get(i).replyMarkup instanceof TdApi.ReplyMarkupShowKeyboard) {
                    foundKb = (TdApi.ReplyMarkupShowKeyboard) msgs.get(i).replyMarkup;
                    break;
                }
            }
            final TdApi.ReplyMarkupShowKeyboard kb = foundKb;
            mActivity.runOnUiThread(() -> {
                if (mActiveChatId == chatId) {
                    mMsgAdapter.submit(msgs);
                    renderReplyKeyboard(kb);
                    if (!msgs.isEmpty()) {
                        mMsgRecycler.scrollToPosition(msgs.size() - 1);
                    }
                }
            });
        }).start();
    }

    private void renderReplyKeyboard(TdApi.ReplyMarkupShowKeyboard kb) {
        mLastReplyKeyboard = kb;
        mReplyKeyboardHost.removeAllViews();
        if (kb == null || kb.rows == null || kb.rows.length == 0) {
            mReplyKbToggleBtn.setVisibility(View.GONE);
            mReplyKeyboardHost.setVisibility(View.GONE);
            return;
        }
        mReplyKbToggleBtn.setVisibility(View.VISIBLE);
        for (TdApi.KeyboardButton[] row : kb.rows) {
            LinearLayout rLayout = new LinearLayout(mActivity);
            rLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (TdApi.KeyboardButton btn : row) {
                TextView tv = new TextView(mActivity);
                tv.setText(btn.text);
                tv.setTextColor(mTextPrimary);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(dp(8), dp(10), dp(8), dp(10));
                tv.setBackground(makeBox(mBgHeader, dp(10), true));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(dp(3), dp(3), dp(3), dp(3));
                tv.setOnClickListener(v -> sendBotCommandText(btn.text));
                rLayout.addView(tv, lp);
            }
            mReplyKeyboardHost.addView(rLayout);
        }
    }

    private void bindAvatarView(ImageView iv, TextView fallbackTv, String title, String localPath, long idSeed, int sizePx) {
        if (localPath != null && !localPath.isEmpty()) {
            Bitmap bmp = mAvatarBitmapCache.get(localPath);
            if (bmp == null) {
                File f = new File(localPath);
                if (f.exists()) {
                    Bitmap raw = BitmapFactory.decodeFile(localPath);
                    if (raw != null) {
                        bmp = makeCircleBitmap(raw, sizePx);
                        mAvatarBitmapCache.put(localPath, bmp);
                    }
                }
            }
            if (bmp != null) {
                iv.setImageBitmap(bmp);
                iv.setVisibility(View.VISIBLE);
                if (fallbackTv != null) fallbackTv.setVisibility(View.GONE);
                return;
            }
        }
        if (fallbackTv != null) {
            iv.setVisibility(View.GONE);
            fallbackTv.setVisibility(View.VISIBLE);
            String ch = (title != null && !title.isEmpty()) ? title.substring(0, 1).toUpperCase(Locale.ROOT) : "?";
            fallbackTv.setText(ch);
            int color = AVATAR_PALETTE[(int) (Math.abs(idSeed) % AVATAR_PALETTE.length)];
            fallbackTv.setBackground(makeBox(color, sizePx / 2f, false));
        } else {
            int color = AVATAR_PALETTE[(int) (Math.abs(idSeed) % AVATAR_PALETTE.length)];
            iv.setImageDrawable(makeBox(color, sizePx / 2f, false));
        }
    }

    private static Bitmap makeCircleBitmap(Bitmap src, int sizePx) {
        Bitmap scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true);
        Bitmap out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float r = sizePx / 2f;
        canvas.drawCircle(r, r, r, p);
        return out;
    }

    public void destroy() {
        FusionTelegramHelper.removeListener(this);
    }

    @Override public void onAuthStateChanged(String state) { syncUiState(); }
    @Override public void onChatsUpdated() { if (mActiveChatId == 0 && "chats".equals(mActiveSection) && FusionTelegramHelper.isReady()) refreshChats(); }
    @Override public void onNewMessage(long chatId, TdApi.Message message) {
        if (mActiveChatId == chatId) loadActiveRoomMessages();
        else if (mActiveChatId == 0 && "chats".equals(mActiveSection) && FusionTelegramHelper.isReady()) refreshChats();
    }
    @Override public void onFiltersUpdated() { rebuildFolderTabs(); }
    @Override public void onUiConfigUpdated() { rebuildDynamicBars(); }

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
            LinearLayout wrapper = new LinearLayout(mActivity);
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout row = new LinearLayout(mActivity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(9), dp(14), dp(9));
            wrapper.addView(row);

            View divider = new View(mActivity);
            divider.setBackgroundColor(mStrokeColor);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            dLp.setMarginStart(dp(74));
            wrapper.addView(divider, dLp);
            return new VH(wrapper, row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            TdApi.Chat c = items.get(pos);
            String title = c.title != null && !c.title.isEmpty() ? c.title : ("Chat " + c.id);
            String avPath = FusionTelegramHelper.getChatAvatarPath(c);
            bindAvatarView(h.avatarImg, h.avatarText, title, avPath, c.id, dp(48));

            String muteMark = FusionTelegramHelper.isChatMuted(c) ? " 🔇" : "";
            h.title.setText(title + muteMark);
            h.preview.setText(c.lastMessage != null ? FusionTelegramHelper.extractMessageText(c.lastMessage) : "");

            String pinMark = FusionTelegramHelper.isChatPinned(c) ? "📌 " : "";
            String timeStr = c.lastMessage != null ? fmt.format(new Date(c.lastMessage.date * 1000L)) : "";
            h.time.setText(pinMark + timeStr);

            if (c.unreadCount > 0) {
                h.badge.setVisibility(View.VISIBLE);
                h.badge.setText(String.valueOf(c.unreadCount));
                h.badge.setBackground(makeBox(FusionTelegramHelper.isChatMuted(c) ? mTextSecondary : mAccentColor, dp(10), false));
            } else {
                h.badge.setVisibility(View.GONE);
            }
            h.itemView.setOnClickListener(v -> openChatRoom(c));
        }

        @Override public int getItemCount() { return items.size(); }

        final class VH extends RecyclerView.ViewHolder {
            final ImageView avatarImg;
            final TextView avatarText;
            final TextView title;
            final TextView preview;
            final TextView time;
            final TextView badge;

            VH(LinearLayout wrapper, LinearLayout root) {
                super(wrapper);
                FrameLayout avBox = new FrameLayout(mActivity);
                avatarImg = new ImageView(mActivity);
                avatarText = new TextView(mActivity);
                avatarText.setTextColor(Color.WHITE);
                avatarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                avatarText.setTypeface(Typeface.DEFAULT_BOLD);
                avatarText.setGravity(Gravity.CENTER);
                avBox.addView(avatarImg, new LayoutParams(dp(48), dp(48)));
                avBox.addView(avatarText, new LayoutParams(dp(48), dp(48)));
                LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(48), dp(48));
                avLp.setMarginEnd(dp(12));
                root.addView(avBox, avLp);

                LinearLayout mid = new LinearLayout(mActivity);
                mid.setOrientation(LinearLayout.VERTICAL);
                title = new TextView(mActivity);
                title.setTextColor(mTextPrimary);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                title.setSingleLine(true);
                title.setEllipsize(TextUtils.TruncateAt.END);
                preview = new TextView(mActivity);
                preview.setTextColor(mTextSecondary);
                preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
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
                badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                badge.setTypeface(Typeface.DEFAULT_BOLD);
                badge.setPadding(dp(7), dp(2), dp(7), dp(2));
                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bLp.topMargin = dp(4);
                right.addView(time);
                right.addView(badge, bLp);
                root.addView(right);
            }
        }
    }

    private final class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.VH> {
        private final List<TdApi.Message> items = new ArrayList<>();
        private final SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final SimpleDateFormat fmtDate = new SimpleDateFormat("d MMMM", new Locale("id", "ID"));

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
            String dayStr = fmtDate.format(new Date(m.date * 1000L));
            boolean showDate = (pos == 0) || !dayStr.equals(fmtDate.format(new Date(items.get(pos - 1).date * 1000L)));
            if (showDate) {
                h.datePill.setText(dayStr);
                h.datePill.setVisibility(View.VISIBLE);
            } else {
                h.datePill.setVisibility(View.GONE);
            }

            h.bubbleWrap.setGravity(m.isOutgoing ? Gravity.END : Gravity.START);
            h.bubble.setBackground(makeBox(m.isOutgoing ? mBubbleOutColor : mBubbleInColor, dp(16), true));

            final String msgText = FusionTelegramHelper.extractMessageText(m);
            h.body.setText(msgText);
            Linkify.addLinks(h.body, Linkify.WEB_URLS);
            h.body.setMovementMethod(LinkMovementMethod.getInstance());
            h.time.setText(fmtTime.format(new Date(m.date * 1000L)) + (m.isOutgoing ? " ✓✓" : ""));

            h.bubble.setOnLongClickListener(v -> {
                showMessageContextMenu(m, msgText);
                return true;
            });

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
                            bView.setPadding(dp(8), dp(7), dp(8), dp(7));
                            bView.setBackground(makeBox(mBgHeader, dp(10), true));
                            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                            bLp.setMargins(dp(2), dp(3), dp(2), dp(2));
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
                    pBtn.setPadding(dp(8), dp(6), dp(8), dp(6));
                    pBtn.setBackground(makeBox(mAccentColor, dp(10), false));
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

        private void showMessageContextMenu(TdApi.Message m, String msgText) {
            final String[] items = { "↩ Balas", "📋 Salin Teks" };
            new android.app.AlertDialog.Builder(mActivity)
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        mReplyToMsgId = m.id;
                        mReplyPreview = msgText;
                        mReplyBarText.setText("↩ " + msgText);
                        mReplyBar.setVisibility(View.VISIBLE);
                    } else if (which == 1) {
                        ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("EsoGram", msgText));
                        FusionToast.makeText(mActivity, "Disalin", 0).show();
                    }
                })
                .show();
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

        private void openUrlFromTelegram(String url) {
            if (FusionWebViewManager.TELEGRAM_CANVAS_LEFT.equals(mManager.mTelegramCanvasLock)) {
                mActivity.goToWallPage(com.termux.app.wall.PaneWallPage.TERMINAL);
            } else {
                mManager.hideTelegramPanel();
            }
            mManager.openUrl(url, "default", false, false);
        }

        @Override public int getItemCount() { return items.size(); }

        final class VH extends RecyclerView.ViewHolder {
            final TextView datePill;
            final LinearLayout bubbleWrap;
            final LinearLayout bubble;
            final TextView body;
            final TextView time;
            final LinearLayout inlineHost;

            VH(LinearLayout root) {
                super(root);
                datePill = new TextView(mActivity);
                datePill.setTextColor(mTextSecondary);
                datePill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                datePill.setTypeface(Typeface.DEFAULT_BOLD);
                datePill.setPadding(dp(12), dp(3), dp(12), dp(3));
                datePill.setBackground(makeBox(mBgHeader, dp(12), false));
                LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dLp.gravity = Gravity.CENTER_HORIZONTAL;
                dLp.topMargin = dp(6);
                dLp.bottomMargin = dp(6);
                root.addView(datePill, dLp);

                bubbleWrap = new LinearLayout(mActivity);
                bubbleWrap.setOrientation(LinearLayout.HORIZONTAL);
                root.addView(bubbleWrap, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                bubble = new LinearLayout(mActivity);
                bubble.setOrientation(LinearLayout.VERTICAL);
                bubble.setPadding(dp(13), dp(8), dp(13), dp(7));

                body = new TextView(mActivity);
                body.setTextColor(mTextPrimary);
                body.setLinkTextColor(mAccentColor);
                body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                bubble.addView(body);

                time = new TextView(mActivity);
                time.setTextColor(mTextSecondary);
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                time.setGravity(Gravity.END);
                bubble.addView(time);

                inlineHost = new LinearLayout(mActivity);
                inlineHost.setOrientation(LinearLayout.VERTICAL);
                bubble.addView(inlineHost);

                bubbleWrap.addView(bubble, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }
}
