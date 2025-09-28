package pmf.rma.cityexplorerosm.ui.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

/** Helper za čuvanje stanja onboarding-a i limited location moda. */
public final class OnboardingPrefs {
    private static final String PREFS = "onboarding_prefs";
    private static final String KEY_DONE = "onboarding_done";
    private static final String KEY_LIMITED_LOCATION = "onb_limited_location";

    private OnboardingPrefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isDone(Context c) { return sp(c).getBoolean(KEY_DONE, false); }
    public static void setDone(Context c, boolean v) { sp(c).edit().putBoolean(KEY_DONE, v).apply(); }

    public static void setLimitedLocation(Context c, boolean v) { sp(c).edit().putBoolean(KEY_LIMITED_LOCATION, v).apply(); }
    public static boolean isLimitedLocation(Context c) { return sp(c).getBoolean(KEY_LIMITED_LOCATION, false); }
}

