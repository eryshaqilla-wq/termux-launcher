package com.termux.app.launcher.widget.builtin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.app.launcher.LauncherAppLauncher;
import com.termux.app.launcher.data.LauncherAppDataProvider;
import com.termux.app.launcher.model.AppRef;
import com.termux.app.launcher.model.LauncherAppEntry;

import java.util.Collections;
import java.util.List;

/**
 * A 1x1 Homescreen app shortcut on Kanvas Kiri.
 * Renders with 100% visual parity to the dock terminal icons (48dp artwork + label).
 * Clicking launches the app, and long-pressing opens context options or enables drag repositioning.
 */
public class AppShortcutWidgetView extends BuiltinWidgetView {

    @Nullable private LauncherAppEntry entry;
    @Nullable private ImageView iconView;
    @Nullable private TextView labelView;

    public AppShortcutWidgetView(@NonNull Context context,
                                 @NonNull BuiltinWidgetServices services,
                                 @NonNull BuiltinWidgetStyle style) {
        super(context, BuiltinWidgetKind.APP_SHORTCUT, services, style);
    }

    @Override
    protected boolean zoomsToFill(@NonNull BuiltinWidgetSpan span) {
        return false;
    }

    @NonNull
    @Override
    public List<ConfigField> configFields() {
        return Collections.emptyList();
    }

    private void resolveEntryIfNeeded() {
        if (entry != null) return;
        Bundle cfg = config();
        String pkg = cfg.getString("package_name");
        String act = cfg.getString("activity_name");
        long userId = cfg.getLong("user_id", 0L);
        if (pkg == null || pkg.isEmpty()) return;

        LauncherAppDataProvider provider = LauncherAppDataProvider.getInstance(getContext());
        AppRef ref = new AppRef(pkg, act != null ? act : "", (int) userId);
        entry = provider.findByRef(ref);
        if (entry == null) {
            entry = provider.findFirstByPackage(pkg);
        }
    }

    @Nullable
    public LauncherAppEntry getAppEntry() {
        resolveEntryIfNeeded();
        return entry;
    }

    @Override
    protected void onBuild(@NonNull FrameLayout frame, @NonNull BuiltinWidgetSpan span,
                           @NonNull BuiltinWidgetUi ui) {
        View bg = findViewById(android.R.id.background);
        if (bg != null) {
            bg.setBackgroundColor(Color.TRANSPARENT);
        }

        resolveEntryIfNeeded();

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);

        int iconSizePx = Math.round(48f * getResources().getDisplayMetrics().density);
        iconView = new ImageView(getContext());
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
        iconLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(iconView, iconLp);

        labelView = new TextView(getContext());
        labelView.setGravity(Gravity.CENTER);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        labelView.setTextColor(0xFFEEEEEE);
        labelView.setSingleLine(true);
        labelView.setEllipsize(TextUtils.TruncateAt.END);
        labelView.setPadding(0, Math.round(3f * getResources().getDisplayMetrics().density), 0, 0);
        labelView.setShadowLayer(4f, 0f, 2f, 0xCC000000);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.addView(labelView, labelLp);

        frame.removeAllViews();
        FrameLayout.LayoutParams rootLp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        frame.addView(root, rootLp);

        bindData();
    }

    private void bindData() {
        if (labelView == null || iconView == null) return;
        String fallbackLabel = configString("label", "App");
        labelView.setText(entry != null ? entry.label : fallbackLabel);

        if (entry != null) {
            LauncherAppDataProvider provider = LauncherAppDataProvider.getInstance(getContext());
            com.termux.app.launcher.icon.LauncherIconStore.Artwork artwork = provider.icons().artworkDetailed(entry);
            if (artwork != null && artwork.drawable != null) {
                iconView.setImageDrawable(artwork.drawable);
            } else {
                Drawable direct = provider.icons().artwork(entry);
                if (direct != null) {
                    iconView.setImageDrawable(direct);
                }
            }
        }
    }

    @Override
    protected void onTap() {
        resolveEntryIfNeeded();
        if (entry != null) {
            LauncherAppLauncher.launchEntry(getContext(), entry);
        } else {
            String pkg = config().getString("package_name");
            if (pkg != null && !pkg.isEmpty()) {
                Intent launch = getContext().getPackageManager().getLaunchIntentForPackage(pkg);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(launch);
                }
            }
        }
    }
}
