/*
 * Copyright (C) 2019 RevengeOS
 * Copyright (C) 2023 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.fuelgauge.smartcharging;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.widget.ImageView;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import mx.xperience.framework.preference.CustomSeekBarPreference;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for Smart charging
 */
public class BatteryProtectionSettings extends DashboardFragment implements OnPreferenceChangeListener {
    private static final String TAG = "BatteryProtectionSettings";
    private static final String KEY_BATTERY_PROTECTION_LEVEL = "battery_protection_level";
    private static final String KEY_BATTERY_PROTECTION_RESUME_LEVEL = "battery_protection_resume_level";
    private CustomSeekBarPreference mBatteryProtectionLevel;
    private CustomSeekBarPreference mBatteryProtectionResumeLevel;
    private int mBatteryProtectionLevelDefaultConfig;
    private int mBatteryProtectionResumeLevelDefaultConfig;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mBatteryProtectionLevelDefaultConfig = getResources().getInteger(
                com.android.internal.R.integer.config_batteryProtectionBatteryLevel);
        mBatteryProtectionResumeLevelDefaultConfig = getResources().getInteger(
                com.android.internal.R.integer.config_batteryProtectionBatteryResumeLevel);
        mBatteryProtectionLevel = (CustomSeekBarPreference) findPreference(KEY_BATTERY_PROTECTION_LEVEL);
        int currentLevel = Settings.System.getInt(getContentResolver(),
            Settings.System.BATTERY_PROTECTION_LEVEL, mBatteryProtectionLevelDefaultConfig);
        mBatteryProtectionLevel.setValue(currentLevel);
        mBatteryProtectionLevel.setOnPreferenceChangeListener(this);
        mBatteryProtectionResumeLevel = (CustomSeekBarPreference) findPreference(KEY_BATTERY_PROTECTION_RESUME_LEVEL);
        int currentResumeLevel = Settings.System.getInt(getContentResolver(),
            Settings.System.BATTERY_PROTECTION_RESUME_LEVEL, mBatteryProtectionResumeLevelDefaultConfig);
        if (currentResumeLevel >= currentLevel) currentResumeLevel = currentLevel -1;
        mBatteryProtectionResumeLevel.setValue(currentResumeLevel);
        mBatteryProtectionResumeLevel.setOnPreferenceChangeListener(this);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.battery_protection;
    }
    @Override
    public int getMetricsCategory() {
        return MetricsEvent.RAINBOW_UNICORN;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mBatteryProtectionLevel) {
            int batteryProtectionLevel = (Integer) objValue;
            int mChargingResumeLevel = Settings.System.getInt(getContentResolver(),
                     Settings.System.BATTERY_PROTECTION_RESUME_LEVEL, mBatteryProtectionResumeLevelDefaultConfig);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_PROTECTION_LEVEL, batteryProtectionLevel);
            if (batteryProtectionLevel <= mChargingResumeLevel) {
                mBatteryProtectionResumeLevel.setValue(batteryProtectionLevel - 1);
                Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_PROTECTION_RESUME_LEVEL, batteryProtectionLevel - 1);
            }
            return true;
        } else if (preference == mBatteryProtectionResumeLevel) {
            int batteryProtectionResumeLevel = (Integer) objValue;
            int mChargingLevel = Settings.System.getInt(getContentResolver(),
                     Settings.System.BATTERY_PROTECTION_LEVEL, mBatteryProtectionLevelDefaultConfig);
               Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_PROTECTION_RESUME_LEVEL, batteryProtectionResumeLevel);
            return true;
        } else {
            return false;
        }
    }
}