package com.termux.app.ime;

import android.content.Context;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.R;
import com.termux.app.terminal.inappkeyboard.InAppKeyboardExtraKeys;
import com.termux.app.terminal.inappkeyboard.InAppKeyboardPaletteFactory;
import com.termux.shared.termux.font.FileTypefaces;
import com.termux.shared.termux.font.NerdFontSpans;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

import java.io.File;

import juloo.keyboard2.Config;
import juloo.keyboard2.Keyboard2View;
import juloo.keyboard2.KeyboardData;
import juloo.keyboard2.LayoutModifier;
import juloo.keyboard2.Theme;

/**
 * All-in-One system-wide InputMethodService exposing the Termux keyboard across Android.
 * Shares the exact same solid themes, layouts, symbols, and preferences configured in Termux.
 */
public class TermuxInputMethodService extends InputMethodService {

    private TermuxAppSharedPreferences mPreferences;
    private Keyboard2View mKeyboardView;
    private FrameLayout mInputContainer;
    private ImeKeyEventHandler mKeyEventHandler;
    private String mAppliedTheme = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = TermuxAppSharedPreferences.build(this);
        mKeyEventHandler = new ImeKeyEventHandler(this);
    }

    @Override
    public View onCreateInputView() {
        Context context = this;
        if (mPreferences == null) {
            mPreferences = TermuxAppSharedPreferences.build(context);
        }

        mInputContainer = new FrameLayout(context);
        mInputContainer.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        rebuildKeyboardView();
        return mInputContainer;
    }

    private void rebuildKeyboardView() {
        if (mInputContainer == null) return;
        mInputContainer.removeAllViews();

        Context context = this;
        String theme = mPreferences != null ? mPreferences.getInAppKeyboardTheme() : "system";
        mAppliedTheme = theme;

        int bgColor = InAppKeyboardPaletteFactory.isSolidTheme(theme)
            ? InAppKeyboardPaletteFactory.getSolidBackgroundColor(context, theme)
            : 0xFF1F1F1F; // Default opaque dark for system IME window

        mInputContainer.setBackgroundColor(bgColor);

        Config.Builder configBuilder = new Config.Builder(getResources(), mKeyEventHandler);
        configBuilder.hapticEnabled = mPreferences != null && mPreferences.isInAppKeyboardHapticsEnabled();
        configBuilder.keySoundEnabled = mPreferences != null && mPreferences.isInAppKeyboardKeySoundEnabled();
        configBuilder.labelFont = loadLabelFont();
        configBuilder.symbolFont = NerdFontSpans.typeface(context);

        Theme.Palette palette;
        if (InAppKeyboardPaletteFactory.isSolidTheme(theme)) {
            palette = InAppKeyboardPaletteFactory.createSolid(context, theme);
        } else {
            // When default (frosted glass) is picked in Termux, outside Termux we use the Gboard Dark
            // solid palette so the keyboard is 100% readable and opaque on any external app wallpaper/view.
            palette = InAppKeyboardPaletteFactory.createSolid(context, "gboard_dark");
        }

        mKeyboardView = new Keyboard2View(context, configBuilder.build(), palette);

        // Sizing & Appearance
        if (mPreferences != null) {
            float marginScale = mPreferences.getInAppKeyboardKeyMarginScale();
            mKeyboardView.setKeyMarginScale(marginScale);
            float radiusDp = mPreferences.getInAppKeyboardKeyCornerRadiusDp();
            if (radiusDp >= 0f) {
                mKeyboardView.setKeyCornerRadiusOverride(radiusDp * getResources().getDisplayMetrics().density);
            }
        }

        // Layout
        KeyboardData layout = KeyboardData.load(getResources(), juloo.keyboard2.R.xml.termux_launcher_qwerty);
        if (layout != null) {
            LayoutModifier.LayoutOptions options = new LayoutModifier.LayoutOptions(
                true, false, true,
                InAppKeyboardExtraKeys.resolve(mPreferences != null ? mPreferences.getInAppKeyboardExtraKeys() : ""));
            mKeyboardView.setKeyboard(LayoutModifier.modify(layout, options, getResources()));
        }

        mInputContainer.addView(mKeyboardView, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (mKeyEventHandler != null) {
            mKeyEventHandler.reset();
        }

        // Refresh theme if changed in settings
        if (mPreferences != null) {
            String currentTheme = mPreferences.getInAppKeyboardTheme();
            if (!currentTheme.equals(mAppliedTheme)) {
                rebuildKeyboardView();
            }
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (mKeyEventHandler != null) {
            mKeyEventHandler.reset();
        }
    }

    @Nullable
    private Typeface loadLabelFont() {
        if (mPreferences == null) return null;
        String fontPath = mPreferences.getInAppKeyboardFontPath();
        if (fontPath == null || fontPath.isEmpty()) return null;
        File file = new File(fontPath);
        if (file.isFile()) {
            try {
                return FileTypefaces.load(file);
            } catch (RuntimeException ignored) {}
        }
        return null;
    }
}
