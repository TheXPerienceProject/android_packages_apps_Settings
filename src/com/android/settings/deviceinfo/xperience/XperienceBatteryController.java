/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.deviceinfo.xperience;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.text.TextUtils;

import com.android.internal.os.PowerProfile;
import com.android.settings.core.BasePreferenceController;

public class XperienceBatteryController extends BasePreferenceController {

    private static final String FALLBACK_CAPACITY = "7300";
    private static final String FALLBACK_TECH = "silicon-carbon";

    public XperienceBatteryController(Context context) {
        super(context, "battery_spec");
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String capacity = getBatteryCapacity();
        String capacityType = getCapacityType();
        String tech = getBatteryTechnology();

        if (TextUtils.isEmpty(capacity)) {
            capacity = FALLBACK_CAPACITY;
        }

        if (TextUtils.isEmpty(tech)) {
            tech = FALLBACK_TECH;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(capacity)
                .append(" mAh (")
                .append(capacityType)
                .append(")")
                .append("\n")
                .append(tech)
                .append(" battery");

        return summary.toString();
    }

    /* =========================
     * Battery capacity (mAh)
     * ========================= */
    private String getBatteryCapacity() {
        try {
            PowerProfile profile = new PowerProfile(mContext);
            double capacity = profile.getBatteryCapacity();

            if (capacity > 0) {
                return String.valueOf((int) Math.round(capacity));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /* =========================
     * Capacity type detection
     *
     * TYP  -> typical capacity
     * NOM  -> nominal / single cell
     * DUAL -> dual-cell design
     * ========================= */
    private String getCapacityType() {
        try {
            // We use the standard battery Intent to read the actual voltage.
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = mContext.registerReceiver(null, filter);

            if (batteryStatus != null) {
                // The voltage is measured in millivolts (mV).
                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

                // Safe heuristic:
                // Single-cell batteries rarely exceed 4.5V (4500mV).
                // If the system reports more than 6V (6000mV), it is almost certainly a dual-cell configuration.
                if (voltage > 6000) {
                    return "DUAL";
                }
            }
        } catch (Throwable ignored) {
            // If something goes wrong, we assume the standard
        }

        return "TYP";
    }

    /* =========================
     * Battery technology
     * ========================= */
    private String getBatteryTechnology() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = mContext.registerReceiver(null, filter);
        if (battery == null) return null;

        String tech = battery.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        if (TextUtils.isEmpty(tech)) {
            return null;
        }

        // We convert to lowercase first to standardise (in case LI-ION is used).
        tech = tech.toLowerCase();

        // We capitalise the first letter (e.g. li-ion -> Li-ion)
        if (tech.length() > 1) {
            tech = tech.substring(0, 1).toUpperCase() + tech.substring(1);
        }

        return tech;
    }
}
