/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.android.settings.deviceinfo.xperience

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.SystemProperties
import android.util.Size
import androidx.preference.Preference
import com.android.settings.core.BasePreferenceController
import kotlin.math.roundToInt

/**
 * Preference controller used to display camera hardware information
 * in the Device Info section.
 *
 * Priority order:
 * 1. Read camera specs from system properties (OEM-defined, fastest path)
 * 2. Fallback to runtime Camera2 API detection if properties are missing
 *
 * Output format example:
 * Front 16MP
 * Rear 50+8MP
 */
class XperienceCameraController(
    context: Context,
    key: String
) : BasePreferenceController(context, key) {

    /** CameraManager used for runtime camera inspection */
    private val cameraManager =
    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /**
     * This preference is always available.
     */
    override fun getAvailabilityStatus(): Int = AVAILABLE

    /**
     * Updates the preference summary with detected camera information.
     */
    override fun updateState(preference: Preference?) {
        super.updateState(preference)
        preference?.summary = getCameraSummary()
    }

    /**
     * Builds a human-readable camera summary string.
     *
     * Strategy:
     * - Prefer system properties if both front and rear values exist
     * - Otherwise detect cameras at runtime using Camera2
     *
     * @return formatted camera summary or a fallback message
     */
    private fun getCameraSummary(): String {
        // 1. Try to read OEM-defined system properties
        val frontProp = SystemProperties.get("ro.device.front_cam", "")
        val rearProp = SystemProperties.get("ro.device.rear_cam", "")

        // If both properties exist, trust them and skip runtime detection
        if (frontProp.isNotEmpty() && rearProp.isNotEmpty()) {
            return "Front $frontProp\nRear $rearProp"
        }

        // 2. Runtime detection fallback
        val frontSpecs = mutableListOf<Int>()
        val rearSpecs = mutableListOf<Int>()

        try {
            for (cameraId in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(cameraId)

                // Skip logical multi-camera devices to avoid duplicates
                val capabilities =
                chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                val isLogical =
                capabilities?.contains(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
                ) ?: false

                if (isLogical) continue

                    val facing = chars.get(CameraCharacteristics.LENS_FACING)
                    val megapixels = getMaxMegapixels(chars)

                    if (megapixels > 0) {
                        when (facing) {
                            CameraCharacteristics.LENS_FACING_FRONT ->
                            frontSpecs.add(megapixels)

                            CameraCharacteristics.LENS_FACING_BACK ->
                            rearSpecs.add(megapixels)
                        }
                    }
            }
        } catch (e: Exception) {
            // If runtime detection fails and no properties were available
            return "Información no disponible"
        }

        // Remove duplicates and sort from highest to lowest
        val finalFront = frontSpecs.distinct().sortedDescending()
        val finalRear = rearSpecs.distinct().sortedDescending()

        // Prefer system properties if present, otherwise use runtime results
        val frontText =
        if (frontProp.isNotEmpty()) {
            "Front $frontProp"
        } else if (finalFront.isNotEmpty()) {
            "Front ${finalFront.joinToString("+")}MP"
        } else ""

            val rearText =
            if (rearProp.isNotEmpty()) {
                "Rear $rearProp"
            } else if (finalRear.isNotEmpty()) {
                "Rear ${finalRear.joinToString("+")}MP"
            } else ""

                return if (frontText.isNotEmpty() && rearText.isNotEmpty()) {
                    "$frontText\n$rearText"
                } else {
                    "$frontText$rearText"
                }
    }

    /**
     * Calculates the maximum megapixel value supported by a camera.
     *
     * It checks both high-resolution and standard JPEG output sizes
     * and picks the largest available resolution.
     *
     * @param chars Camera characteristics to inspect
     * @return megapixel count (rounded) or 0 if unavailable
     */
    private fun getMaxMegapixels(chars: CameraCharacteristics): Int {
        val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val highResSizes: Array<Size>? =
            map?.getHighResolutionOutputSizes(android.graphics.ImageFormat.JPEG)

        val normalSizes =
        map?.getOutputSizes(android.graphics.ImageFormat.JPEG)

        val allSizes =
        (highResSizes ?: emptyArray()) + (normalSizes ?: emptyArray())

        val largestSize =
        allSizes.maxByOrNull { it.width * it.height }

        return if (largestSize != null) {
            ((largestSize.width.toLong() * largestSize.height) / 1_000_000.0)
            .roundToInt()
        } else {
            0
        }
    }
}
