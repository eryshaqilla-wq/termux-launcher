package com.termux.app;

/**
 * Standardized backdrop dim behind elevated glass surfaces (the app drawer plane). Each surface
 * feeds its own transition progress, so the tint breathes from transparent to dark and back with
 * the same spring that moves the surface — never a hard cut.
 */
public final class GlassBackdropTint {
    /** Dim strength with the surface fully open: ~96% black over the glass stack to eliminate bleed-through. */
    private static final int MAX_ALPHA = 0xF5;

    private GlassBackdropTint() {}

    /** ARGB scrim color for a transition progress in [0, 1]. */
    public static int colorFor(float progress) {
        float p = Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0f;
        // Curve the ramp so dim rises swiftly as soon as opening begins, preventing wallpaper bleed
        float curved = p <= 0f ? 0f : (p >= 1f ? 1f : (float) Math.pow(p, 0.7f));
        return ((int) (MAX_ALPHA * curved)) << 24;
    }
}
