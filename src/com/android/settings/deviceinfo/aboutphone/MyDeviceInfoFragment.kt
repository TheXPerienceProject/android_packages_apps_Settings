/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 * IMPORTANTE: reemplaza a MyDeviceInfoFragment.java (bórralo, no pueden
 * coexistir dos clases con el mismo nombre calificado).
 *
 * Este fragment YA NO usa PreferenceScreen/DashboardFragment: hostea la
 * pantalla completa de Jetpack Compose. my_device_info.xml se conserva
 * pero reducido (ver PATCH_NOTES.md) solo para que el buscador de Settings
 * siga indexando "build number", "uptime", etc.
 *
 * Nota de compatibilidad: si tu árbol usa una interfaz distinta a
 * Instrumentable para metricsCategory (varía un poco entre ramas de
 * Settings), ajusta el override según tu InstrumentedFragment real.
 */
package com.android.settings.deviceinfo.aboutphone

import android.app.settings.SettingsEnums
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.SystemProperties
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.core.InstrumentedPreferenceFragment
import com.android.settings.core.SubSettingLauncher
import com.android.settings.deviceinfo.BuildNumberPreferenceController
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionSettings
import com.android.settings.deviceinfo.hardwareinfo.HardwareInfoFragment
import com.android.settings.deviceinfo.hardwareinfo.TotalRAMPreferenceController
import com.android.settings.deviceinfo.xperience.DeviceStorageController
import com.android.settings.deviceinfo.xperience.ProcessorSpecPreferenceController
import com.android.settings.deviceinfo.xperience.XperienceBatteryController
import com.android.settings.deviceinfo.xperience.XperienceCameraController
import com.android.settings.deviceinfo.xperience.compose.AboutDeviceScreen
import com.android.settings.deviceinfo.xperience.compose.DeviceSpecs
import com.android.settings.deviceinfo.xperience.compose.SoftwareInfo
import com.android.settings.deviceinfo.xperience.compose.XperienceTheme
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable
import java.util.concurrent.TimeUnit

private const val KEY_UPDATE_AVAILABLE = "xpe_update_available"
private const val KEY_BUILD_NUMBER = "build_number"
private const val PROP_XPERIENCE_VERSION = "ro.xperience.build.version"

@SearchIndexable
class MyDeviceInfoFragment : InstrumentedPreferenceFragment() {

    private lateinit var buildNumberController: BuildNumberPreferenceController

    // Backs the "update available" card. Settings.Global reads are not
    // observable by default, so we bridge changes into Compose state
    // manually via a ContentObserver (registered in onStart, torn down
    // in onStop below).
    private lateinit var updateAvailableState: MutableState<Boolean>

    private val updateAvailableObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            if (::updateAvailableState.isInitialized) {
                val newValue = isUpdateAvailable(requireContext())
                if (updateAvailableState.value != newValue) {
                    updateAvailableState.value = newValue
                }
            }
        }
    }

    override fun getMetricsCategory(): Int = SettingsEnums.DEVICEINFO

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.about_settings)
        buildNumberController = BuildNumberPreferenceController(requireContext(), KEY_BUILD_NUMBER)
        buildNumberController.setHost(this)

        //fixes development settings tap
        settingsLifecycle.addObserver(buildNumberController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        updateAvailableState = mutableStateOf(isUpdateAvailable(context))
        return ComposeView(context).apply {
            setContent {
                val updateAvailable by updateAvailableState
                XperienceTheme {
                    AboutDeviceScreen(
                        specs = buildDeviceSpecs(context),
                        updateAvailable = updateAvailable,
                        osName = getXperienceOsName(),
                        onBackClick = { activity?.onBackPressedDispatcher?.onBackPressed() },
                        onUpdateCardClick = { launchUpdater(context) },
                        onDeviceStatusClick = { launchHardwareInfo(context) },
                        onAndroidVersionClick = { launchFirmwareVersion(context) },
                        onBuildNumberClick = { handleBuildNumberClick(context) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireContext().contentResolver.registerContentObserver(
            Settings.Global.getUriFor(KEY_UPDATE_AVAILABLE),
            /* notifyForDescendants= */ true,
            updateAvailableObserver
        )
        // Handles the case where the flag changed while the fragment was
        // paused (e.g., you returned from the background after the Updater
        // wrote to it).
        if (::updateAvailableState.isInitialized) {
            updateAvailableState.value = isUpdateAvailable(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        if (::updateAvailableState.isInitialized) {
            updateAvailableState.value = isUpdateAvailable(requireContext())
        }
    }

    override fun onStop() {
        requireContext().contentResolver.unregisterContentObserver(updateAvailableObserver)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { act ->
            val isDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val bgColor = if (isDark) Color.BLACK else Color.WHITE

            act.window.statusBarColor = bgColor
            act.window.navigationBarColor = bgColor

            act.findViewById<View>(R.id.app_bar)?.setBackgroundColor(bgColor)
            act.findViewById<View>(R.id.action_bar)?.setBackgroundColor(bgColor)

            val collapsingToolbarId = resources.getIdentifier(
                "collapsing_toolbar", "id", act.packageName)
            if (collapsingToolbarId != 0) {
                val toolbar = act.findViewById<View>(collapsingToolbarId)
                if (toolbar is com.google.android.material.appbar.CollapsingToolbarLayout) {
                    toolbar.setContentScrimColor(bgColor)
                    toolbar.setStatusBarScrimColor(bgColor)
                } else {
                    toolbar?.setBackgroundColor(bgColor)
                }
            }
        }
    }

    /**
     * Routes Compose clicks to the native AOSP controller.
     * This controller handles accumulated taps, notification toasts,
     * and requests for the user's PIN or pattern if the device is locked.
     */
    private fun handleBuildNumberClick(context: Context) {
        val dummyPref = Preference(context).apply {
            key = KEY_BUILD_NUMBER
        }
        buildNumberController.handlePreferenceTreeClick(dummyPref)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Required if the device prompts for a PIN or pattern before unlocking developer mode
        if (::buildNumberController.isInitialized &&
            buildNumberController.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
    * Reflects xpe_update_available, written by mx.xperience.updater
    * (UpdatesCheckReceiver detects a new OTA; UpdatesActivity
    * clears it once the update has been installed and only a reboot is needed).
    * See Constants.SETTING_XPE_UPDATE_AVAILABLE in the Updater.
    *
    * NOTE: this is a plain synchronous read, not observable on its own.
    * updateAvailableObserver (onStart/onStop below) is what makes the
    * "update available" card react live while this screen is open.
    */
    private fun isUpdateAvailable(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, KEY_UPDATE_AVAILABLE, 0) == 1

     /**
     * Reads ro.xperience.build.version (e.g., "21.0", "22.1") and constructs the name
     * displayed in the header. If it ends with ".0", it is truncated ("21.0" ->
     * "v21"); if it includes a point release, it is kept as-is ("21.5" -> "v21.5").
     */
    private fun getXperienceOsName(): String {
        val rawVersion = SystemProperties.get(PROP_XPERIENCE_VERSION, "")
        if (rawVersion.isBlank()) {
            return "XPerience"
        }
        val trimmed = if (rawVersion.endsWith(".0")) {
            rawVersion.removeSuffix(".0")
        } else {
            rawVersion
        }
        return "XPerience v$trimmed"
    }

    private fun buildDeviceSpecs(context: Context): DeviceSpecs {
        val storage = DeviceStorageController(context, "device_storage_dual")
        val processor = ProcessorSpecPreferenceController(context, "processor_spec")
        val battery = XperienceBatteryController(context)
        val cameras = XperienceCameraController(context, "camera_spec")
        val ram = TotalRAMPreferenceController(context, "ram_spec")

        return DeviceSpecs(
            deviceName = storage.deviceName,
            storageSummary = storage.storageSummary,
            processor = processor.getDetectedChipset(),
            battery = battery.summary?.toString().orEmpty(),
            ram = ram.getSummary()?.toString().orEmpty(),
            cameras = cameras.getCameraSummary(),
            software = SoftwareInfo(
                androidVersion = Build.VERSION.RELEASE,
                buildNumber = Build.DISPLAY,
                uptime = formatUptime(SystemClock.elapsedRealtime())
            )
        )
    }

    private fun formatUptime(millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return "${days}d ${hours}h ${minutes}m"
    }

    private fun launchUpdater(context: Context) {
        runCatching {
            context.startActivity(Intent("android.settings.SYSTEM_UPDATE_SETTINGS"))
        }
    }

    private fun launchHardwareInfo(context: Context) {
        SubSettingLauncher(context)
            .setDestination(HardwareInfoFragment::class.java.name)
            .setSourceMetricsCategory(metricsCategory)
            .launch()
    }

    private fun launchFirmwareVersion(context: Context) {
        SubSettingLauncher(context)
            .setDestination(FirmwareVersionSettings::class.java.name)
            .setSourceMetricsCategory(metricsCategory)
            .launch()
    }

    companion object {
        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER: BaseSearchIndexProvider =
            object : BaseSearchIndexProvider(R.xml.my_device_info) {}
    }
}
