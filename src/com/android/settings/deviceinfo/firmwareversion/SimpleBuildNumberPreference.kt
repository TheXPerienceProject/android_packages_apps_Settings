/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.Build
import android.text.BidiFormatter
import android.view.View.LAYOUT_DIRECTION_RTL
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

// LINT.IfChange
class SimpleBuildNumberPreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding {

    override val key: String
        get() = "os_build_number"

    override val title: Int
        get() = R.string.build_number

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun getSummary(context: Context): CharSequence? {
        val isRtl = context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL
        return BidiFormatter.getInstance(isRtl).unicodeWrap(Build.DISPLAY)
    }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
        preference.isCopyingEnabled = true
    }
}
// LINT.ThenChange(SimpleBuildNumberPreferenceController.java)
