/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.android.settings.deviceinfo.xperience;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.xperience.widget.DualColumnPreference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.deviceinfo.DeviceNamePreferenceController;
import com.android.settings.deviceinfo.StorageDashboardFragment;
import com.android.settings.widget.ValidatedEditTextPreference;

public class DeviceStorageController extends BasePreferenceController {

    private DualColumnPreference mPreference;
    private DeviceNamePreferenceController mDeviceNameController;
    private Fragment mFragment;
    private PreferenceScreen mScreen;

    public DeviceStorageController(Context context, String key) {
        super(context, key);
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
        // Inicializar el controller del nombre del dispositivo
        if (fragment != null) {
            mDeviceNameController = new DeviceNamePreferenceController(
                mContext, "device_name");
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mScreen = screen;
        mPreference = screen.findPreference(getPreferenceKey());

        if (mPreference != null) {
            updateState(mPreference);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof DualColumnPreference) {
            mPreference = (DualColumnPreference) preference;

            // Device name
            String deviceName = SystemProperties.get("ro.product.vendor.marketname", "");
            if (deviceName.isEmpty()) {
                deviceName = android.os.Build.MODEL;
            }

            // Storage info
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long totalBytes = stat.getTotalBytes();
            long freeBytes = stat.getFreeBytes();
            long usedBytes = totalBytes - freeBytes;

            String storageInfo = formatBytes(usedBytes) + " / " + formatBytes(totalBytes);

            // Actualizar las dos columnas
            mPreference.setLeftColumn(
                mContext.getString(R.string.device_name),
                deviceName,
                com.android.settings.R.drawable.ic_device_xpe
            );

            mPreference.setRightColumn(
                mContext.getString(R.string.storage),
                storageInfo,
                com.android.settings.R.drawable.ic_storage_xpe
            );

            // Configurar los listeners para cada card
            mPreference.setLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeviceNameDialog();
                }
            });

            mPreference.setRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStorageSettings();
                }
            });
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }

    private void openDeviceNameDialog() {
        if (mScreen == null || mDeviceNameController == null) return;

        ValidatedEditTextPreference deviceNamePref = mScreen.findPreference("device_name");

        if (deviceNamePref == null) {
            deviceNamePref = new ValidatedEditTextPreference(mContext);
            deviceNamePref.setKey("device_name");
            deviceNamePref.setVisible(false); // La mantenemos invisible
            mScreen.addPreference(deviceNamePref);
        }


        mDeviceNameController.updateState(deviceNamePref);

        if (deviceNamePref != null) {
            deviceNamePref.performClick();
        }
    }

    private void openStorageSettings() {
        if (mContext == null) return;

        final Intent intent = new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);

        if (!(mContext instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        mContext.startActivity(intent);
    }
}
