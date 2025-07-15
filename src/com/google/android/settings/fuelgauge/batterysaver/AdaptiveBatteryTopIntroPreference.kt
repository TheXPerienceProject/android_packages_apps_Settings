package com.google.android.settings.fuelgauge.batterysaver

import android.content.Context
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.preference.PreferenceBinding
import com.android.settingslib.widget.TopIntroPreference

class AdaptiveBatteryTopIntroPreference :
    PreferenceMetadata, PreferenceBinding, PreferenceAvailabilityProvider {

    override val key: String = "adaptive_battery_top_intro"

    override val title: Int = R.string.smart_battery_summary

    override val indexable: Boolean = false

    override fun createWidget(context: Context): TopIntroPreference {
        return TopIntroPreference(context)
    }

    override fun isAvailable(context: Context): Boolean {
        return AdaptiveBatteryScreen.isAdaptiveBatteryAvailable(context)
    }
}
