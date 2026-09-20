/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SELinux
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.Utils
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding
import com.android.settingslib.widget.LayoutPreference

class XPerienceLogoPreference : PreferenceMetadata, PreferenceBinding {

    override val key: String
        get() = "xperience_logo"

    override val purpose: Int
        get() = R.string.firmware_version_purpose

    override val indexable
        get() = false

    override fun createWidget(context: Context): Preference =
        LayoutPreference(context, R.layout.xperience_logo)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
    }
}

class XPerienceVersionPreference :
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "xperience_version"

    override val purpose: Int
        get() = R.string.xperience_firmware_version

    override val title: Int
        get() = R.string.xperience_firmware_version

    override fun getSummary(context: Context): CharSequence =
        SystemProperties.get(
            "ro.xperience.build.version",
            context.getString(R.string.unknown),
        )

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)

        preference.isCopyingEnabled = true
        preference.setOnPreferenceClickListener {
            val context = preference.context

            if (Utils.isMonkeyRunning()) {
                return@setOnPreferenceClickListener false
            }

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.xperience_firmware_uri)),
                )

            if (intent.resolveActivity(context.packageManager) == null) {
                return@setOnPreferenceClickListener true
            }

            context.startActivity(intent)
            true
        }
    }
}

class SimpleBuildCodeNamePreference :
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "os_build_codename"

    override val purpose: Int
        get() = R.string.xperience_build_code

    override val title: Int
        get() = R.string.xperience_build_code

    override val indexable
        get() = false

    override fun getSummary(context: Context): CharSequence =
        SystemProperties.get(
            "ro.xpe.codename",
            context.getString(R.string.unknown),
        )

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }
}

class SimpleBuildChannelPreference :
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "os_build_channel"

    override val purpose: Int
        get() = R.string.build_channel

    override val title: Int
        get() = R.string.build_channel

    override val indexable
        get() = false

    override fun getSummary(context: Context): CharSequence =
        SystemProperties.get(
            "ro.xpe.channeltype",
            context.getString(R.string.unknown),
        )

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }
}

class SelinuxStatusPreference :
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "selinux_status"

    override val purpose: Int
        get() = R.string.selinux_status

    override val title: Int
        get() = R.string.selinux_status

    override val indexable
        get() = false

    override fun getSummary(context: Context): CharSequence =
        when {
            !SELinux.isSELinuxEnabled() ->
                context.getString(R.string.selinux_status_disabled)

            SELinux.isSELinuxEnforced() ->
                context.getString(R.string.selinux_status_enforcing)

            else ->
                context.getString(R.string.selinux_status_permissive)
        }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
        preference.isCopyingEnabled = false
    }
}