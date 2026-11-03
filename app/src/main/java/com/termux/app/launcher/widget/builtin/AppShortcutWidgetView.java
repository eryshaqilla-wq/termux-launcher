package com.termux.app.launcher.widget.builtin;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.app.launcher.model.AppRef;
import com.termux.app.launcher.model.LauncherAppEntry;

public class AppShortcutWidgetView extends BuiltinWidgetView {

    @Nullable private LauncherAppEntry mEntry;
    @Nullable private ImageView mIconView;
    @Nullable private TextView mLabelView;

    public AppShortcutWidgetView(@NonNull Context context,
                                 @NonNull BuiltinWidgetServices services,
                                 @NonNull BuiltinWidgetStyle style) {
        super(context, BuiltinWidgetKind.APP_SHORTCUT, services, style);
        View bg = findViewById(android.R.id.background);
        if (bg != null) bg.setBackground(null);
    }

    @Override
    protected void onBuild(@NonNull FrameLayout frame, @NonNull BuiltinWidgetSpan span,
                           @NonNull BuiltinWidgetUi ui) {
        View bg = findViewById(android.R.id.background);
        if (bg != null) bg.setBackground(null);

        Bundle cfg = config();
        String pkg = cfg.getString("package", "");
        String act = cfg.getString("activity", "");
        int userId = cfg.getInt("userId", -1);
        String label = cfg.getString("label", "");

        AppRef ref = new AppRef(pkg, act, userId, -1L, false, "");
        mEntry = services.host().findAppByRef(ref);

        Context context = getContext();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setClickable(false);
        layout.setFocusable(false);

        int iconSizePx = ui.dp(36f);
        ImageView icon = new ImageView(context);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setAdjustViewBounds(true);
        icon.setClickable(false);
        icon.setFocusable(false);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        layout.addView(icon, iconParams);
        mIconView = icon;

        TextView labelText = new TextView(context);
        labelText.setSingleLine(true);
        labelText.setMaxLines(1);
        labelText.setClickable(false);
        labelText.setFocusable(false);
        labelText.setEllipsize(TextUtils.TruncateAt.END);
        labelText.setGravity(Gravity.CENTER_HORIZONTAL);
        labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        labelText.setTextColor(services.host().launcherTextColor());
        labelText.setShadowLayer(3f, 0f, 1.5f, 0xCC000000);
        labelText.setPadding(0, ui.dp(2f), 0, 0);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.gravity = Gravity.CENTER_HORIZONTAL;
        layout.addView(labelText, labelParams);
        mLabelView = labelText;

        if (mEntry != null) {
            labelText.setText(mEntry.label);
            services.host().bindAppIcon(icon, mEntry, iconSizePx);
            setContentDescription(mEntry.label);
        } else {
            labelText.setText(label.isEmpty() ? pkg : label);
            try {
                icon.setImageDrawable(context.getPackageManager().getApplicationIcon(pkg));
            } catch (Exception ignored) {
            }
            setContentDescription(labelText.getText());
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        frame.addView(layout, layoutParams);
    }

    @Override
    protected void onTap() {
        if (mEntry != null) {
            services.host().launchAppShortcut(mEntry, this);
        } else {
            Bundle cfg = config();
            String pkg = cfg.getString("package", "");
            String act = cfg.getString("activity", "");
            int userId = cfg.getInt("userId", -1);
            if (!pkg.isEmpty()) {
                AppRef ref = new AppRef(pkg, act, userId, -1L, false, "");
                LauncherAppEntry entry = services.host().findAppByRef(ref);
                if (entry != null) {
                    mEntry = entry;
                    services.host().launchAppShortcut(entry, this);
                }
            }
        }
    }

    @Nullable
    public LauncherAppEntry getAppEntry() {
        return mEntry;
    }
}
