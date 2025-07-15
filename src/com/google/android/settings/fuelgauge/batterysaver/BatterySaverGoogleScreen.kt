package com.google.android.settings.fuelgauge.batterysaver

import android.content.Context
import com.android.settings.fuelgauge.batterysaver.BatterySaverPreference
import com.android.settings.fuelgauge.batterysaver.BatterySaverScreen
import com.android.settingslib.metadata.PreferenceHierarchy
import com.android.settingslib.metadata.preferenceHierarchy
import com.android.settingslib.widget.UntitledPreferenceCategoryMetadata
import kotlinx.coroutines.CoroutineScope

class BatterySaverGoogleScreen : BatterySaverScreen() {

    override fun getPreferenceHierarchy(
        context: Context,
        coroutineScope: CoroutineScope,
    ): PreferenceHierarchy =
        preferenceHierarchy(context) {
            +BatterySaverPreference() order 10

            if (FlipendoUtils.isFlipendoInstalled(context) == true) {
                +UntitledPreferenceCategoryMetadata("battery_saver_group") order 30 += {
                    val dataStore = BatterySaverModeDataStore(context)
                    +BasicBatterySaverPreference(dataStore)
                    +ExtremeBatterySaverPreference(dataStore)
                }
            }

            +UntitledPreferenceCategoryMetadata("battery_saver_schedule_category") order 50 += {
                +"battery_saver_schedule"
            }

            +UntitledPreferenceCategoryMetadata("adaptive_battery_category") order 70 += {
                +"adaptive_battery_entry"
            }
        }
}
