package com.termux.app.ime;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    private String mAppliedSettingsSignature = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = TermuxAppSharedPreferences.build(this);
        mKeyEventHandler = new ImeKeyEventHandler(this);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        // Prevent fullscreen extract edit box in landscape so user can see what they type
        return false;
    }

    @Override
    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly);
        applyWindowStyling(win);
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        updateBackDisposition();
        applyWindowStyling(getWindow() != null ? getWindow().getWindow() : null);
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
        updateBackDisposition();
        return mInputContainer;
    }

    private void rebuildKeyboardView() {
        if (mInputContainer == null) return;
        mInputContainer.removeAllViews();

        Context context = this;
        String theme = mPreferences != null ? mPreferences.getInAppKeyboardTheme() : "system";
        mAppliedSettingsSignature = computeSettingsSignature();

        int bgColor = InAppKeyboardPaletteFactory.isSolidTheme(theme)
            ? InAppKeyboardPaletteFactory.getSolidBackgroundColor(context, theme)
            : 0xFF1F1F1F; // Default opaque dark for system IME window

        mInputContainer.setBackgroundColor(bgColor);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        Config.Builder configBuilder = new Config.Builder(getResources(), mKeyEventHandler);
        // Important: In a system IME, do not cap keyboard height to 42% of the input frame!
        configBuilder.maxKeyboardHeightFraction = 1.0f;
        // Standard Android keyboard row heights: 54dp in portrait, 44dp in landscape
        configBuilder.rowHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, landscape ? 44f : 54f, dm);
        // Comfortable chin padding at the bottom above gesture navigation bar
        int userBottomPadding = mPreferences != null ? mPreferences.getInAppKeyboardBottomPadding() : 0;
        configBuilder.bottomMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, Math.max(8f, userBottomPadding), dm);
        // Horizontal side insets matching Termux settings
        int horizontalInset = mPreferences != null ? mPreferences.getInAppKeyboardHorizontalInset() : 0;
        configBuilder.horizontalMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, Math.max(3f, horizontalInset), dm);

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

        // Sizing & Appearance synchronized with Termux preferences
        if (mPreferences != null) {
            float marginScale = mPreferences.getInAppKeyboardKeyMarginScale();
            mKeyboardView.setKeyMarginScale(marginScale);
            float radiusDp = mPreferences.getInAppKeyboardKeyCornerRadiusDp();
            if (radiusDp >= 0f) {
                mKeyboardView.setKeyCornerRadiusOverride(radiusDp * dm.density);
            }
            float heightScale = mPreferences.getInAppKeyboardHeightScale();
            if (heightScale > 0f) {
                mKeyboardView.setHeightScale(heightScale);
            }
            int keyOpacity = mPreferences.getInAppKeyboardKeyOpacity();
            mKeyboardView.setKeyOpacity(keyOpacity < 0 ? -1f : keyOpacity / 100f);
        }

        // Layout
        KeyboardData layout = KeyboardData.load(getResources(), juloo.keyboard2.R.xml.termux_launcher_qwerty);
        if (layout != null) {
            LayoutModifier.LayoutOptions options = new LayoutModifier.LayoutOptions(
                true, false, true,
                InAppKeyboardExtraKeys.resolve(mPreferences != null ? mPreferences.getInAppKeyboardExtraKeys() : ""));
            mKeyboardView.setKeyboard(LayoutModifier.modify(layout, options, getResources()));
        }

        FrameLayout.LayoutParams kbdLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        kbdLp.gravity = Gravity.BOTTOM;
        mInputContainer.addView(mKeyboardView, kbdLp);

        // Synchronize window navigation bar and background
        applyWindowStyling(getWindow() != null ? getWindow().getWindow() : null);
        updateBackDisposition();
    }

    private void updateBackDisposition() {
        boolean showButton = mPreferences != null
            ? mPreferences.isNavBarKeyboardButtonEnabled()
            : SystemNavBarHelper.isNavBarKeyboardButtonEnabled(this);
        setBackDisposition(showButton
            ? InputMethodService.BACK_DISPOSITION_DEFAULT
            : InputMethodService.BACK_DISPOSITION_ADJUST_NOTHING);
    }

    private void applyWindowStyling(@Nullable Window win) {
        if (win == null) return;
        Context context = this;
        String theme = mPreferences != null ? mPreferences.getInAppKeyboardTheme() : "system";
        int bgColor = InAppKeyboardPaletteFactory.isSolidTheme(theme)
            ? InAppKeyboardPaletteFactory.getSolidBackgroundColor(context, theme)
            : 0xFF1F1F1F;

        View decorView = win.getDecorView();
        if (decorView != null) {
            decorView.setBackgroundColor(bgColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = decorView.getSystemUiVisibility();
                boolean isLight = theme != null && theme.contains("light");
                if (isLight) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
                decorView.setSystemUiVisibility(flags);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            win.setNavigationBarColor(bgColor);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            win.setNavigationBarDividerColor(0);
        }
    }

    private String computeSettingsSignature() {
        if (mPreferences == null) return "";
        return mPreferences.getInAppKeyboardTheme() + "|"
            + mPreferences.getInAppKeyboardHeightScale() + "|"
            + mPreferences.getInAppKeyboardKeyMarginScale() + "|"
            + mPreferences.getInAppKeyboardKeyCornerRadiusDp() + "|"
            + mPreferences.getInAppKeyboardExtraKeys() + "|"
            + mPreferences.getInAppKeyboardFontPath() + "|"
            + mPreferences.getInAppKeyboardBottomPadding() + "|"
            + mPreferences.getInAppKeyboardHorizontalInset() + "|"
            + mPreferences.getInAppKeyboardKeyOpacity() + "|"
            + mPreferences.isNavBarKeyboardButtonEnabled() + "|"
            + mPreferences.isInAppKeyboardHapticsEnabled() + "|"
            + mPreferences.isInAppKeyboardKeySoundEnabled();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (mKeyEventHandler != null) {
            mKeyEventHandler.reset();
        }

        updateBackDisposition();

        // Refresh keyboard view if any keyboard preferences changed in Settings
        String currentSignature = computeSettingsSignature();
        if (!currentSignature.equals(mAppliedSettingsSignature)) {
            rebuildKeyboardView();
        } else {
            applyWindowStyling(getWindow() != null ? getWindow().getWindow() : null);
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
