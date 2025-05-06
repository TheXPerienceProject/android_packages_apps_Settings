package com.android.settings.accessibility;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.ListPreference;

public class HapticStyleListPreference extends ListPreference {
    private static final String PROP_KEY = "persist.sys.haptic_profile";
    private static final String SETTINGS_KEY = Settings.Secure.HAPTIC_EFFECTS_PROFILE;

    public HapticStyleListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        SystemProperties.set(PROP_KEY, value);
	Settings.Secure.putString(getContext().getContentResolver(), SETTINGS_KEY, value);
    }
}
