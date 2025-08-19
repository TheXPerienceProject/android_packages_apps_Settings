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

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

/**
 * The main switch controller for DC dim
 */
public class DcDimBarController extends TogglePreferenceController implements LifecycleObserver {

    private final SettingsObserver mSettingsObserver = new SettingsObserver(
            new Handler(Looper.getMainLooper()));

    private Preference mPreference;
    private String mDcNode = null;

    public DcDimBarController(Context context, String key) {
        super(context, key);
    }

    @Override
    public void displayPreference(@NonNull PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public int getAvailabilityStatus() {
        final boolean isEnabled = mContext.getResources().getBoolean(
                R.bool.config_showDcDimSettings);
        if (!isEnabled) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (getNode() == null || getNode().isEmpty()) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
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
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        mSettingsObserver.observe();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mSettingsObserver.stop();
    }

    private String getNode() {
        if (mDcNode != null) {
            return mDcNode;
        }
        mDcNode = mContext.getResources().getString(
                com.android.internal.R.string.config_dcdNodePath);
        return mDcNode;
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
