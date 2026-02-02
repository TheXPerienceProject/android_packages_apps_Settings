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
     * TYP  → typical capacity
     * NOM  → nominal / single cell
     * DUAL → dual-cell design
     * ========================= */
    private String getCapacityType() {
        try {
            PowerProfile profile = new PowerProfile(mContext);

            /*
             * Safe heuristics:
             *  - Dual cell almost always reports
             *    nominal voltage ~7.6V (2 × 3.8)
             *  - Single cell ~3.7-3.85V
             */
            double voltage = profile.getBatteryNominalVoltage(); // mV

            if (voltage >= 7000) {
                return "DUAL";
            }

            return "TYP";
        } catch (Throwable ignored) {
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
        if (TextUtils.isEmpty(tech)) return null;

        // Aesthetic standardisation
        return tech.toLowerCase();
    }
}
