/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.xperience.widget;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * A {@link AppCompatTextView} that automatically populates itself with the
 * user-visible device name.
 *
 * <p>Resolution order:
 *   1. {@link Settings.Global#DEVICE_NAME} — the name the user may have
 *      customised in Settings → About Phone → Device name.
 *   2. {@link Build#MODEL} — the hardware model string as a fallback.
 *
 * <p>Usage in XML:
 * <pre>
 *   &lt;com.xperience.widget.DeviceNameTextView
 *       android:layout_width="wrap_content"
 *       android:layout_height="wrap_content"
 *       android:textColor="#C9C8C8"
 *       android:textSize="15sp" /&gt;
 * </pre>
 */
public class DeviceNameTextView extends AppCompatTextView {

    public DeviceNameTextView(Context context) {
        super(context);
        init(context);
    }

    public DeviceNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DeviceNameTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        String deviceName = Settings.Global.getString(
                context.getContentResolver(), Settings.Global.DEVICE_NAME);
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = Build.MODEL;
        }
        setText(deviceName);
    }
}
