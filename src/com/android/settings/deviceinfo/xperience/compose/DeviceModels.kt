/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.deviceinfo.xperience.compose

/**
 * DeviceStorageController, ProcessorSpecPreferenceController,
 * XperienceBatteryController, XperienceCameraController.
 */
data class DeviceSpecs(
    val deviceName: String,
    val storageSummary: String,
    val processor: String,
    val battery: String,
    val ram: String,
    val cameras: String,
    val software: SoftwareInfo
)

data class SoftwareInfo(
    val androidVersion: String,
    val buildNumber: String,
    val uptime: String
)
