/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.xperience

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.SystemProperties
import android.util.Size
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import kotlin.math.roundToInt

/**
 * Preference controller that displays camera hardware information
 * in the Device Info section.
 *
 * Detection priority:
 * 1. OEM system properties (ro.device.front_cam / ro.device.rear_cam) — fastest,
 *    must be declared in device.mk via PRODUCT_PROPERTY_OVERRIDES
 * 2. Runtime Camera2 API detection — handles logical multi-camera by enumerating
 *    physical sub-cameras, with extra tolerance for MediaTek HALs
 *
 * Output example:
 *   Front 16MP
 *   Rear 50+8MP
 */
class XperienceCameraController(
    context: Context,
    key: String
) : BasePreferenceController(context, key) {

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun updateState(preference: Preference?) {
        super.updateState(preference)
        preference?.summary = getCameraSummary()
    }

    // 
    // Summary builder
    // 

    /**
     * Builds the human-readable camera summary.
     *
     * Returns immediately if both OEM properties are present.
     * Falls back to runtime detection otherwise, mixing props and
     * runtime results when only one side is available.
     */
    private fun getCameraSummary(): String {
        val frontProp = SystemProperties.get("ro.device.front_cam", "")
        val rearProp  = SystemProperties.get("ro.device.rear_cam", "")

        // Fast path — both OEM properties defined, no need to touch Camera2
        if (frontProp.isNotEmpty() && rearProp.isNotEmpty()) {
            return buildSummary("Front $frontProp", "Rear $rearProp")
        }

        // Runtime detection — returns sorted MP lists per facing
        val (frontSpecs, rearSpecs) = detectCamerasRuntime()

        val frontText = when {
            frontProp.isNotEmpty()   -> "Front $frontProp"
            frontSpecs.isNotEmpty()  -> "Front ${frontSpecs.joinToString("+")}MP"
            else                     -> ""
        }

        val rearText = when {
            rearProp.isNotEmpty()   -> "Rear $rearProp"
            rearSpecs.isNotEmpty()  -> "Rear ${rearSpecs.joinToString("+")}MP"
            else                    -> ""
        }

        return buildSummary(frontText, rearText).ifEmpty {
            mContext.getString(R.string.camera_info_unavailable)
        }
    }

    /** Joins non-empty parts with a newline. */
    private fun buildSummary(front: String, rear: String): String =
        listOf(front, rear).filter { it.isNotEmpty() }.joinToString("\n")

    // 
    // Runtime Camera2 detection
    // 

    /**
     * Enumerates cameras via Camera2 and returns two sorted MP lists:
     * first = front cameras, second = rear cameras.
     *
     * Logical multi-camera devices are expanded into their physical
     * sub-cameras rather than skipped outright, which is necessary for
     * MediaTek devices where the logical camera often wraps all sensors.
     */
    private fun detectCamerasRuntime(): Pair<List<Int>, List<Int>> {
        val frontSpecs = mutableListOf<Int>()
        val rearSpecs  = mutableListOf<Int>()

        try {
            for (cameraId in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(cameraId)
                val capabilities =
                    chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

                val isLogical = capabilities?.contains(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
                ) ?: false

                if (isLogical) {
                    // Expand logical camera into its physical sub-cameras
                    // instead of skipping — critical for MediaTek HALs
                    for (physicalId in chars.physicalCameraIds) {
                        val physicalChars =
                            cameraManager.getCameraCharacteristics(physicalId)
                        collectCamera(physicalChars, frontSpecs, rearSpecs)
                    }
                } else {
                    collectCamera(chars, frontSpecs, rearSpecs)
                }
            }
        } catch (e: Exception) {
            // Camera2 unavailable or HAL error — caller handles empty lists
            android.util.Log.w(TAG, "Runtime camera detection failed: ${e.message}")
        }

        // Deduplicate and sort highest MP first
        return Pair(
            frontSpecs.distinct().sortedDescending(),
            rearSpecs.distinct().sortedDescending()
        )
    }

    /**
     * Reads the facing and megapixel count from [chars] and appends
     * the result to the appropriate list.
     */
    private fun collectCamera(
        chars: CameraCharacteristics,
        frontSpecs: MutableList<Int>,
        rearSpecs: MutableList<Int>
    ) {
        val mp = getMaxMegapixels(chars)
        if (mp <= 0) return

        when (chars.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> frontSpecs.add(mp)
            CameraCharacteristics.LENS_FACING_BACK  -> rearSpecs.add(mp)
            // LENS_FACING_EXTERNAL and unknown facings are intentionally ignored
        }
    }

    // 
    // Megapixel calculation
    // 

    /**
     * Returns the maximum megapixel count supported by [chars].
     *
     * Checks high-resolution JPEG sizes first (available on sensors that
     * support SENSOR_PIXEL_MODE_MAXIMUM_RESOLUTION), then falls back to
     * standard output sizes.
     *
     * @return rounded MP count, or 0 if the map is unavailable
     */
    private fun getMaxMegapixels(chars: CameraCharacteristics): Int {
        val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: return 0

        val highResSizes: Array<Size> =
            map.getHighResolutionOutputSizes(android.graphics.ImageFormat.JPEG)
                ?: emptyArray()

        val normalSizes: Array<Size> =
            map.getOutputSizes(android.graphics.ImageFormat.JPEG)
                ?: emptyArray()

        val largest = (highResSizes + normalSizes)
            .maxByOrNull { it.width.toLong() * it.height }
            ?: return 0

        return ((largest.width.toLong() * largest.height) / 1_000_000.0).roundToInt()
    }

    companion object {
        private const val TAG = "XperienceCameraController"
    }
}