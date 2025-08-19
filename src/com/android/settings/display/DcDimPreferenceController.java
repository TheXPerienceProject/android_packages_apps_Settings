/*
 * Copyright (C) 2025 Yet Another AOSP Project
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
package com.android.settings.display;

import static com.android.internal.util.yaap.AutoSettingConsts.MODE_DISABLED;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_NIGHT;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_TIME;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNSET;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNRISE;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class DcDimPreferenceController extends TogglePreferenceController implements LifecycleObserver {

    private final SettingsObserver mSettingsObserver = new SettingsObserver(
            new Handler(Looper.getMainLooper()));
    private final boolean mIsEnabled;
    private final String mNodePath;

    private Preference mPreference;

    public DcDimPreferenceController(Context context, String key) {
        super(context, key);

        mIsEnabled = context.getResources().getBoolean(R.bool.config_showDcDimSettings);
        mNodePath = context.getResources().getString(
                com.android.internal.R.string.config_dcdNodePath);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mIsEnabled || mNodePath == null || mNodePath.isEmpty()) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshSummary(preference);
    }

    @Override
    public boolean isSliceable() {
        return true;
    }

    @Override
    public boolean isPublicSlice() {
        return getAvailabilityStatus() == AVAILABLE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    @Override
    public boolean isChecked() {
        if (getAvailabilityStatus() != AVAILABLE) return false;
        return Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.DC_DIM_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (getAvailabilityStatus() != AVAILABLE) return false;
        Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.DC_DIM_ENABLED, isChecked ? 1 : 0, UserHandle.USER_CURRENT);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        final int mode = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DC_DIM_AUTO_MODE, MODE_DISABLED, UserHandle.USER_CURRENT);
        int resID = R.string.schedule_disabled;
        switch (mode) {
            case MODE_NIGHT:
                resID = R.string.night_display_auto_mode_twilight;
                break;
            case MODE_TIME:
                resID = R.string.night_display_auto_mode_custom;
                break;
            case MODE_MIXED_SUNSET:
                resID = R.string.always_on_display_schedule_mixed_sunset;
                break;
            case MODE_MIXED_SUNRISE:
                resID = R.string.always_on_display_schedule_mixed_sunrise;
                break;
            case MODE_DISABLED:
            default:
                // do nothing
                break;
        }
        return mContext.getText(resID);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        mSettingsObserver.observe();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mSettingsObserver.stop();
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.DC_DIM_ENABLED),
                    false, this);
        }

        void stop() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (mPreference == null) {
                return;
            }
            updateState(mPreference);
        }
    }
}
