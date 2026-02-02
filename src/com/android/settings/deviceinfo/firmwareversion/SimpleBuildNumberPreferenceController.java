/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.Build;
import android.text.BidiFormatter;

import com.android.settings.core.BasePreferenceController;

// LINT.IfChange
public class SimpleBuildNumberPreferenceController extends BasePreferenceController {

    public SimpleBuildNumberPreferenceController(Context context,
            String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE_UNSEARCHABLE;
    }

    @Override
    public CharSequence getSummary() {
        return BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY);
    }
}
// LINT.ThenChange(SimpleBuildNumberPreference.kt)
