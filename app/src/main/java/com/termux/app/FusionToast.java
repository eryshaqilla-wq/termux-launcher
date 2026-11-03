package com.termux.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * FusionToast: Universal adaptive Toast for Project Fusion / Eso Launcher.
 * Automatically detects System Day/Night (Dark/Light) theme:
 * - Dark Mode: Google Dark Surface (#1E1E1E) with crisp white text (#F4F3F7) & subtle glass border.
 * - Light Mode: Google Light Surface (#FFFFFF) with dark charcoal text (#1C1B1F) & soft border.
 * Fully safe on both non-root and root environments with thread-safe UI-thread dispatch.
 */
public final class FusionToast {

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private FusionToast() {
    }

    public static boolean isDarkMode(@Nullable Context context) {
        if (context == null) return true;
        try {
            int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == Configuration.UI_MODE_NIGHT_YES;
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * Drop-in replacement for Toast.makeText() that builds a styled adaptive Toast.
     */
    @NonNull
    public static Toast makeText(@NonNull Context context, @NonNull CharSequence message, int duration) {
        try {
            boolean isDark = isDarkMode(context);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(context, 22f));

            TextView tv = new TextView(context);
            tv.setText(message);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            tv.setGravity(Gravity.CENTER);

            int padH = (int) dpToPx(context, 18f);
            int padV = (int) dpToPx(context, 10f);
            tv.setPadding(padH, padV, padH, padV);

            if (isDark) {
                // Google Dark Surface & Frosted Glass aesthetic
                bg.setColor(Color.parseColor("#E61E1E1E")); // Rich dark surface with 90% opacity
                bg.setStroke((int) dpToPx(context, 1f), Color.parseColor("#33FFFFFF")); // Subtle glass border
                tv.setTextColor(Color.parseColor("#F4F3F7")); // High contrast crisp white
            } else {
                // Google Light Surface & Clean aesthetic
                bg.setColor(Color.parseColor("#F7FFFFFF")); // Clean white with 97% opacity
                bg.setStroke((int) dpToPx(context, 1f), Color.parseColor("#22000000")); // Soft border
                tv.setTextColor(Color.parseColor("#1C1B1F")); // High contrast charcoal text
            }

            tv.setBackground(bg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tv.setElevation(dpToPx(context, 4f));
            }

            Toast toast = new Toast(context);
            toast.setDuration(duration);
            toast.setView(tv);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, (int) dpToPx(context, 75f));
            return toast;
        } catch (Throwable t) {
            // Failsafe fallback to standard system Toast if OEM framework restricts setView
            return Toast.makeText(context, message, duration);
        }
    }

    @NonNull
    public static Toast makeText(@NonNull Context context, @StringRes int resId, int duration) {
        return makeText(context, context.getString(resId), duration);
    }

    public static void show(@Nullable final Context context, @StringRes final int resId) {
        show(context, resId, Toast.LENGTH_SHORT);
    }

    public static void show(@Nullable final Context context, @StringRes final int resId, final int duration) {
        if (context == null) return;
        try {
            show(context, context.getString(resId), duration);
        } catch (Throwable ignored) {
        }
    }

    public static void show(@Nullable final Context context, @Nullable final CharSequence message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public static void show(@Nullable final Context context, @Nullable final CharSequence message, final int duration) {
        if (context == null || message == null || message.length() == 0) return;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                makeText(context, message, duration).show();
            } catch (Throwable t) {
                try {
                    Toast.makeText(context, message, duration).show();
                } catch (Throwable ignored) {
                }
            }
        } else {
            sMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        makeText(context, message, duration).show();
                    } catch (Throwable t) {
                        try {
                            Toast.makeText(context, message, duration).show();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        }
    }

    private static float dpToPx(@NonNull Context context, float dp) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
}
