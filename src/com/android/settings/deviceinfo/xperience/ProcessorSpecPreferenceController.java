/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.xperience;

import android.content.Context;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Preference controller that detects the device SoC (chipset)
 * and displays a human-readable name in the "Processor" field
 * on the About Phone screen.
 *
 * Detection order:
 *  1. ro.xpe.chipset (manual override)
 *  2. ro.boot.product.vendor.sku (runtime detection)
 *  3. Fallback string if unknown
 */
public class ProcessorSpecPreferenceController extends BasePreferenceController {

    private static final String FALLBACK_CHIPSET = "Unknown processor";

    private static final Map<String, String> SKU_TO_CHIPSET = new HashMap<>();

    static {
        /* Mediatek */
        SKU_TO_CHIPSET.put("shennron", "MediaTek Dimensity 9000");
        SKU_TO_CHIPSET.put("mt6899", "Dimensity 8400 Ultra");
        /* Qualcomm */
        SKU_TO_CHIPSET.put("bengal", "Snapdragon® 460 / 662 / 678");
        SKU_TO_CHIPSET.put("trinket", "Snapdragon® 665 / 675");
        SKU_TO_CHIPSET.put("atoll", "Snapdragon® 720G");
        SKU_TO_CHIPSET.put("lito", "Snapdragon® 765 / 768G");
        SKU_TO_CHIPSET.put("yupik", "Snapdragon® 778G");
        SKU_TO_CHIPSET.put("diwali", "Snapdragon® 7 Gen 1");
        SKU_TO_CHIPSET.put("austin", "Snapdragon® 7 Gen 3");
        SKU_TO_CHIPSET.put("tangor", "Snapdragon® 7 Gen 3 Elite");
        SKU_TO_CHIPSET.put("volcano", "Snapdragon® 7s Gen 3");
        SKU_TO_CHIPSET.put("msmnile", "Snapdragon® 860");
        SKU_TO_CHIPSET.put("lahaina", "Snapdragon® 888 / 888+");
        SKU_TO_CHIPSET.put("waipio", "Snapdragon® 8 Gen 1");
        SKU_TO_CHIPSET.put("taro", "Snapdragon® 8 Gen 1");
        SKU_TO_CHIPSET.put("cape", "Snapdragon® 8+ Gen 1");
        SKU_TO_CHIPSET.put("kalama", "Snapdragon® 8 Gen 2");
        SKU_TO_CHIPSET.put("pineapple", "Snapdragon® 8 Gen 3");
        SKU_TO_CHIPSET.put("sun", "Snapdragon® 8 Elite");


    }

    public ProcessorSpecPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        preference.setSummary(getDetectedChipset());
    }

    /**
     * Detects the chipset name using system properties.
     */
    private String getDetectedChipset() {
        // 1. Manual override
        final String manual = SystemProperties.get("ro.xpe.chipset", "");
        if (!manual.isEmpty()) {
            return manual;
        }

        // 2. Runtime SKU detection
        final String sku = SystemProperties
                .get("ro.boot.product.vendor.sku", "")
                .toLowerCase(Locale.US);

        return SKU_TO_CHIPSET.getOrDefault(sku, FALLBACK_CHIPSET);
    }
}
