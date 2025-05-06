package com.android.settings.accessibility;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

public class HapticStylePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String PROP_KEY = "debug.haptic_profile";
    private static final String SETTINGS_KEY = Settings.Secure.HAPTIC_EFFECTS_PROFILE;
    private ListPreference mPreference;

    public HapticStylePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_supportHapticProfiles) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof ListPreference) {
            mPreference = (ListPreference) pref;
            mPreference.setOnPreferenceChangeListener(this);
            updateState(mPreference);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            String currentValue = Settings.Secure.getString(mContext.getContentResolver(), SETTINGS_KEY);

            // If the property is empty, use the first entry value as default
            if (currentValue == null || currentValue.isEmpty()) {
                CharSequence[] entryValues = listPref.getEntryValues();
                if (entryValues != null && entryValues.length > 0) {
                    currentValue = entryValues[0].toString();
                    listPref.setValue(currentValue);
                    SystemProperties.set(PROP_KEY, currentValue);
		    Settings.Secure.putString(mContext.getContentResolver(), SETTINGS_KEY, currentValue);
                }
            } else {
                listPref.setValue(currentValue);
            }

            int index = listPref.findIndexOfValue(currentValue);
            if (index >= 0) {
                preference.setSummary(listPref.getEntries()[index]);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof ListPreference)) return false;
        String value = (String) newValue;
        SystemProperties.set(PROP_KEY, value);
        Settings.Secure.putString(mContext.getContentResolver(), SETTINGS_KEY, value);
        int index = mPreference.findIndexOfValue(value);
        if (index >= 0) {
            mPreference.setSummary(mPreference.getEntries()[index]);
        }
        return true;
    }
}
