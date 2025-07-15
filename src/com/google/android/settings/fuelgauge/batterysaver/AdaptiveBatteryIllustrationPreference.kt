package com.google.android.settings.fuelgauge.batterysaver

import android.content.Context
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.preference.PreferenceBinding
import com.android.settingslib.widget.IllustrationPreference

class AdaptiveBatteryIllustrationPreference : PreferenceMetadata, PreferenceBinding {

    override val key: String = "adaptive_battery_illustration"

    override val indexable: Boolean = false

    override fun createWidget(context: Context): IllustrationPreference {
        return IllustrationPreference(context).apply {
            setLottieAnimationResId(R.raw.lottie_adaptive_battery)
            applyDynamicColor()
        }
    }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
    }
}
