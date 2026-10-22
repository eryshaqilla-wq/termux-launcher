package com.termux.app.ime;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.R;
import com.termux.app.terminal.inappkeyboard.InAppKeyboardColorScheme;
import com.termux.app.terminal.inappkeyboard.InAppKeyboardExtraKeys;
import com.termux.app.terminal.inappkeyboard.InAppKeyboardPaletteFactory;
import com.termux.app.terminal.inappkeyboard.KeyPopupController;
import com.termux.app.terminal.inappkeyboard.KeyPopupPalette;
import com.termux.app.terminal.inappkeyboard.LauncherKeyboardLayouts;
import com.termux.app.theme.TermuxThemeManager;
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
    private FrameLayout mKeyboardWrapper;
    private KeyPopupController mKeyPopup;
    private ImeKeyEventHandler mKeyEventHandler;
    private String mAppliedSettingsSignature = "";

    @Override
    public void onCreate() {
        setTheme(R.style.Theme_TermuxIME);
        super.onCreate();
        mPreferences = TermuxAppSharedPreferences.build(this);
        TermuxThemeManager.applyThemeOverlays(this);
        mKeyEventHandler = new ImeKeyEventHandler(this);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        // Prevent fullscreen extract edit box in landscape so user can see what they type
        return false;
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        super.onEvaluateInputViewShown();
        return true;
    }

    @Override
    public boolean onShowInputRequested(int flags, boolean configChange) {
        return true;
    }

    @Override
    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly);
        if (win != null) {
            win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
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
        mInputContainer.setClipChildren(false);
        mInputContainer.setClipToPadding(false);

        rebuildKeyboardView();
        updateBackDisposition();
        return mInputContainer;
    }

    private void rebuildKeyboardView() {
        if (mInputContainer == null) return;
        if (mKeyPopup != null) {
            mKeyPopup.destroy();
            mKeyPopup = null;
        }
        mInputContainer.removeAllViews();

        Context context = this;
        String theme = mPreferences != null ? mPreferences.getInAppKeyboardTheme() : "system";
        mAppliedSettingsSignature = computeSettingsSignature();

        int bgColor = InAppKeyboardPaletteFactory.isSolidTheme(theme)
            ? InAppKeyboardPaletteFactory.getSolidBackgroundColor(context, theme)
            : (InAppKeyboardPaletteFactory.resolveDockGlassBaseColor(context) | 0xFF000000);

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
            palette = InAppKeyboardPaletteFactory.createGlass(context, theme,
                com.termux.app.chrome.GlassLook.of(mPreferences));
        }

        InAppKeyboardColorScheme scheme = InAppKeyboardColorScheme.fromJson(context,
            mPreferences != null ? mPreferences.getInAppKeyboardColorScheme() : "");
        if (scheme.shouldApplyImportedPalette(theme)) {
            palette = scheme.applyToPalette(palette);
        }
        Integer customBg = scheme.resolvedKeyboardBackground();
        if (customBg != null) {
            bgColor = customBg;
        }

        mKeyboardView = new Keyboard2View(context, configBuilder.build(), palette);

        // Layout
        String activeLayoutId = mPreferences != null ? mPreferences.getInAppKeyboardActiveLayout() : null;
        LauncherKeyboardLayouts.Layout layoutDef = activeLayoutId != null
            ? LauncherKeyboardLayouts.find(getResources(), activeLayoutId) : null;
        int layoutResId = (layoutDef != null && layoutDef.xmlResId != 0)
            ? layoutDef.xmlResId : juloo.keyboard2.R.xml.termux_launcher_qwerty;
        KeyboardData layout = KeyboardData.load(getResources(), layoutResId);
        if (layout != null) {
            LayoutModifier.LayoutOptions options = new LayoutModifier.LayoutOptions(
                true, false, true,
                InAppKeyboardExtraKeys.resolve(mPreferences != null ? mPreferences.getInAppKeyboardExtraKeys() : ""));
            mKeyboardView.setKeyboard(LayoutModifier.modify(layout, options, getResources()));
        }

        // Apply custom color scheme & key color overrides (e.g. Enter key color) AFTER setKeyboard
        mKeyboardView.setKeyColorOverrides(scheme.resolvedOverrides());

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

        int popupSpacePx = Math.round(44f * dm.density);
        FrameLayout keyboardWrapper = new FrameLayout(context);
        keyboardWrapper.setBackgroundColor(bgColor);
        keyboardWrapper.setClipChildren(false);
        keyboardWrapper.setClipToPadding(false);

        FrameLayout.LayoutParams kbdLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        kbdLp.gravity = Gravity.BOTTOM;
        keyboardWrapper.addView(mKeyboardView, kbdLp);

        FrameLayout.LayoutParams wrapLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapLp.topMargin = popupSpacePx;
        wrapLp.gravity = Gravity.BOTTOM;
        mInputContainer.addView(keyboardWrapper, wrapLp);
        mKeyboardWrapper = keyboardWrapper;

        // Key popup preview overlay (identical to in-app keyboard popup)
        mKeyPopup = new KeyPopupController(mInputContainer, mKeyboardView);
        KeyPopupPalette defaultPopupPalette = KeyPopupPalette.resolve(context);
        int popupPrimary = (scheme.hasPinnedSwatches() && scheme.getSwatch(1) != 0)
            ? scheme.getSwatch(1)
            : (palette.actionKeyBackground != 0 ? (palette.actionKeyBackground | 0xFF000000) : defaultPopupPalette.primary);
        int popupInk = (scheme.hasPinnedSwatches() && scheme.getSwatch(3) != 0)
            ? scheme.getSwatch(3)
            : (palette.labelColor != 0 ? (palette.labelColor | 0xFF000000) : defaultPopupPalette.ink);
        mKeyPopup.overlay().setPalette(new KeyPopupPalette(popupPrimary, popupInk));
        mKeyPopup.overlay().setTypefaces(mKeyboardView.labelFont(), juloo.keyboard2.Theme.getKeyFont(context), NerdFontSpans.typeface(context));
        boolean popupEnabled = mPreferences == null || mPreferences.isInAppKeyboardKeyPopupEnabled();
        mKeyPopup.setEnabled(popupEnabled);

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
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        Context context = this;
        String theme = mPreferences != null ? mPreferences.getInAppKeyboardTheme() : "system";
        int bgColor = InAppKeyboardPaletteFactory.isSolidTheme(theme)
            ? InAppKeyboardPaletteFactory.getSolidBackgroundColor(context, theme)
            : (InAppKeyboardPaletteFactory.resolveDockGlassBaseColor(context) | 0xFF000000);

        if (mPreferences != null) {
            InAppKeyboardColorScheme scheme = InAppKeyboardColorScheme.fromJson(context,
                mPreferences.getInAppKeyboardColorScheme());
            Integer customBg = scheme.resolvedKeyboardBackground();
            if (customBg != null) {
                bgColor = customBg;
            }
        }

        View decorView = win.getDecorView();
        if (decorView != null) {
            decorView.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = decorView.getSystemUiVisibility();
                boolean isLight = (theme != null && theme.contains("light"))
                    || (Color.red(bgColor) * 0.299 + Color.green(bgColor) * 0.587 + Color.blue(bgColor) * 0.114 > 160);
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

    @Override
    public void onComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (mKeyboardWrapper != null) {
            int top = mKeyboardWrapper.getTop();
            outInsets.contentTopInsets = top;
            outInsets.visibleTopInsets = top;
            outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_CONTENT;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mKeyPopup != null) {
            mKeyPopup.destroy();
            mKeyPopup = null;
        }
    }

    private String computeSettingsSignature() {
        if (mPreferences == null) return "";
        return mPreferences.getInAppKeyboardTheme() + "|"
            + mPreferences.getInAppKeyboardColorScheme() + "|"
            + mPreferences.getInAppKeyboardActiveLayout() + "|"
            + InAppKeyboardPaletteFactory.signature(this) + "|"
            + mPreferences.isInAppKeyboardKeyPopupEnabled() + "|"
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

        mPreferences = TermuxAppSharedPreferences.build(this);
        TermuxThemeManager.applyThemeOverlays(this);
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
